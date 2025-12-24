package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GeminiService geminiService;

    public Recommendation   generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getReccommendations(prompt);
        log.info("RESPONSE FROM AI {} " , geminiService.getReccommendations(prompt));
        return processAIResponse(activity, aiResponse);
    }

    private Recommendation processAIResponse(Activity activity, String aiResponse) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .get("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n","")
                    .replaceAll("\\n```", "")
                    .trim();

 //           log.info("RESPONSE FROM CLEAN AI {}" , jsonContent);

            JsonNode analysisJson = objectMapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall:");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate:");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories:");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafety(analysisJson.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .type(activity.getType().toString())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        String defaultRecommendation = "Overall: Good job on completing your activity! Keep up the great work.\n\n" +
                "Pace: Your pace was consistent throughout the activity. Aim to gradually increase your speed over time.\n\n" +
                "Heart Rate: Your heart rate was within a healthy range. Continue to monitor it during future workouts.\n\n" +
                "Calories: You burned a significant amount of calories. Maintain a balanced diet to support your fitness goals.";

        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getType().toString())
                .recommendation(defaultRecommendation)
                .improvements(Collections.singletonList("Continue with your current routine"))
                .suggestions(Collections.singletonList("Consider consulting a fitness consultant"))
                .safety(Arrays.asList("Stay hydrated",
                        "Warm up before exercises",
                        "Cool down after exercises"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafety(JsonNode safetyNode) {
        List<String> safetyList = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(safetyPoint -> safetyList.add(safetyPoint.asText()));
        }
        return safetyList.isEmpty()?
                Collections.singletonList("Follow general specific safety guidelines provided.") :
                safetyList;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestionList = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestionList.add(String.format("%s: %s", workout, description));
            });
        }
        return suggestionList.isEmpty()?
                Collections.singletonList("No specific workout suggestions provided.") :
                suggestionList;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvementList = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String detail = improvement.path("recommendation").asText();
                improvementList.add(String.format("%s: %s", area, detail));
            });
        }
        return improvementList.isEmpty()?
                Collections.singletonList("No specific improvements suggested.") :
                improvementList;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if(!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
        {
          "analysis": {
            "overall": "Overall analysis here",
            "pace": "Pace analysis here",
            "heartRate": "Heart rate analysis here",
            "caloriesBurned": "Calories analysis here"
          },
          "improvements": [
            {
              "area": "Area name",
              "recommendation": "Detailed recommendation"
            }
          ],
          "suggestions": [
            {
              "workout": "Workout name",
              "description": "Detailed workout description"
            }
          ],
          "safety": [
            "Safety point 1",
            "Safety point 2"
          ]
        }

        Analyze this activity:
        Activity Type: %s
        Duration: %d minutes
        Calories Burned: %d
        Additional Metrics: %s
        
        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
        Ensure the response follows the EXACT JSON format shown above.
        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}
