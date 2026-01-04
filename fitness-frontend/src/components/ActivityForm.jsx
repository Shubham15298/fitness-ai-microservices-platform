import React from "react"
import { Box } from "@mui/material";
import { FormControl, InputLabel, Select, MenuItem } from "@mui/material";
import { TextField } from "@mui/material";
import { useState } from "react";
import { addActivity } from '../services/api'

const ActivityForm = ({ onActivityAdded }) => {

    const [activity, setActivity] = useState({
        type: "RUNNING", duration: '', caloriesBurned: '',
        additionalMetrics:{}
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try { 
            await addActivity(activity);
            onActivityAdded();
            setActivity({
                type: "RUNNING", duration: '', caloriesBurned: '',
                additionalMetrics:{}
            });
        } catch (error) {
            console.error(error);
        }   
    }       

    return (
        <Box component="form" sx={{ mb: 2 }} onSubmit={handleSubmit}>
            <FormControl fullWidth sx={{ mb: 2 }}>
                <InputLabel>Activity Type</InputLabel>
                <Select
                    
                    value={activity.type}
                    onChange={(e)=>{setActivity({...activity, type: e.target.value})}}
                >
                    <MenuItem value="RUNNING">Running</MenuItem>
                    <MenuItem value="WALKING">Walking</MenuItem>
                    <MenuItem value="CYCLING">Cycling</MenuItem>
                </Select>
            </FormControl>

            <TextField fullWidth
                label="Duration"
                type="number"
                sx={{ mb: 2 }}
                value={activity.duration}
                onChange={(e) => setActivity({ ...activity, duration: e.target.value })}
                />

            <TextField fullWidth
                label="Calories Burned"
                type="number"
                sx={{ mb: 2 }}
                value={activity.caloriesBurned}
                onChange={(e) => setActivity({ ...activity, caloriesBurned: e.target.value })}
                />


            <button type="submit" value='contained '>
                Add Activity
            </button>   

        </Box>
    )
}

export default ActivityForm