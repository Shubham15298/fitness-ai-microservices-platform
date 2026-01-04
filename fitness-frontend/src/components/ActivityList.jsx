import React, { useEffect, useState } from "react"
import { useNavigate } from "react-router";
import { Grid } from "@mui/material";
import { Card, CardContent, Typography } from "@mui/material";
import { getActivities } from "../services/api";
import axios from "axios";
import { Box } from "@mui/material";


const ActivityList = () => {

    const [activities, setActivity] = useState([]);
    const navigate = useNavigate();

    const fetchActivities = async () => {
        try {
            const response = await getActivities();
            setActivity(response.data);
        } catch (error) {
            console.error(error);
        }
    }
    useEffect(() => {
        fetchActivities();
    }, []);
    return (
        <Grid container spacing={2}>
            {activities.map((activity) => (
                <Grid container spacing={{xs: 2, md: 3}} columns={{xs: 4, md:12, sm:8}}  >
                    <Card sx={{cursor: 'pointer'}}
                        onClick={() => navigate(`/activities/${activity.id}`)}>
                        <CardContent>
                            <Typography variant="h6">{activity.type}</Typography>
                            <Typography>Duration:{activity.duration}</Typography>
                            <Typography>Calories:{activity.caloriesBurned}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
            ))}
        </Grid>
    )
}

export default ActivityList
