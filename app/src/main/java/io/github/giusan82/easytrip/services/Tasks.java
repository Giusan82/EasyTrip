package io.github.giusan82.easytrip.services;

import android.content.Context;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

public class Tasks {
    private static final String JOB_TAG_WEATHER = "weather";
    //this is set to 5 seconds just check if the service is working
    public static final int STARTING_UPDATE_WEATHER = 5; //seconds
    private static final int SYNC_FLEXTIME = 10; //seconds

    synchronized public static void sheduleUpdateWeather(@NonNull final Context context, boolean isActive) {
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job newJob = dispatcher.newJobBuilder()
                .setService(UpdateWeatherService.class)
                .setTag(JOB_TAG_WEATHER)
                .setConstraints(Constraint.ON_UNMETERED_NETWORK)  //only run when the device use WIFI
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true) //repeat the job
                .setTrigger(Trigger.executionWindow(STARTING_UPDATE_WEATHER, STARTING_UPDATE_WEATHER + SYNC_FLEXTIME))
                .setReplaceCurrent(true) //overwrite an existing job with the same tag
                .build();
        if (isActive) {
            dispatcher.schedule(newJob);
        } else {
            dispatcher.cancel(JOB_TAG_WEATHER);
        }
    }
}
