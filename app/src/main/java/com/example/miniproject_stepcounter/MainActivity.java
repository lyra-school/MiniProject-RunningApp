package com.example.miniproject_stepcounter;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // references to used widgets
    TextView steps;
    TextView timer;

    Button start;
    Button stop;
    Button reset;
    Button show;

    // object that powers the timer
    CountUpTimer functionalTimer;

    // storing device sensors
    private SensorManager sensorManager;
    private Sensor sensor;

    // variables used internally within MainActivity
    boolean isRunning = false;
    boolean runFinished = false;
    String currentTime = "00:00";
    int stepCounter = 0;
    int animationDuration;
    String formattedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // set header
        getSupportActionBar().setTitle(R.string.header);


        // get references to necessary widgets
        steps = (TextView)findViewById(R.id.stepTextView);
        timer = (TextView)findViewById(R.id.timeTextView);

        start = (Button)findViewById(R.id.startBtn);
        stop = (Button)findViewById(R.id.stopBtn);
        reset = (Button)findViewById(R.id.resetBtn);
        show = (Button)findViewById(R.id.showBtn);

        // get reference to step detector in the device
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // get Android's default "medium animation time" for button fade-ins
        animationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        // initialize timer that ticks up until 5 minutes, and a reference to the stop button so
        // it calls its clickable listener once it reaches the end
        functionalTimer = new CountUpTimer(300000, stop) {
            // set the timer text every second, also formats to minutes for better UX + stores
            // the text for use in the second activity
            @Override
            public void onTick(int second) {

                // calculate minutes elapsed
                int mins = 0;
                while(second >= 60) {
                    second = second - 60;
                    mins++;
                }

                /* add a leading zero to the seconds part of the formatted time if the remainder of
                 seconds is a single-digit number

                 minutes always have a leading zero because timer ends on 5 minutes */
                if(second >= 10) {
                    currentTime = "0" + mins + ":" + second;
                    timer.setText(currentTime);
                } else {
                    currentTime = "0" + mins + ":0" + second;
                    timer.setText(currentTime);
                }
            }
        };
    }

    /* update step counter every time when step detector reports an appropriate event. program
       automatically ends on 99999 steps to preserve UI as the program lacks abbreviations for large
       numbers that may distort it. this shouldn't be possible to reach in 5 minutes anyway!
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        /* according to Android docs, a step detector can have either a value of 1 or 0. on the off
           chance that this event is triggered when a step detector falls back to 0 (no step) from 1
           (step detected), verify that the event value is 1 so that the number of steps does not get
           double counted
         */
        float verifyStep = event.values[0];
        if(verifyStep == 1.0f) {
            // increment the count of steps both internally and in the text view
            stepCounter++;
            steps.setText(String.valueOf(stepCounter));

            // emulate pressing the stop button in case of a large number of steps
            if(stepCounter >= 99999) {
                stop.callOnClick();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // unused
    }

    // when the START button is clicked
    public void startClick(View view) {
        // the lab requirements imply that the RESET button is the intended way to reset a run,
        // therefore the start button must not do anything if clicked after a run
        if(isRunning) {
            Toast.makeText(this, "You already started a run!", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if(runFinished) {
            Toast.makeText(this, "You've already completed a run. Press Reset before starting a new one.", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // start timer
        functionalTimer.start();

        // start listening for step detections
        // note: listener registration is handled within buttons instead of onPause/onResume because
        // a running app is normally expected to run in the background as well
        sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        // toggle boolean to let the rest of the program know that the timer is ongoing
        isRunning = true;
    }

    // when the STOP button is clicked
    public void stopClick(View view) {
        // check that the timer is ongoing, don't do anything if not
        if(!isRunning) {
            Toast.makeText(this, "The timer is not running right now!", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // end timer
        functionalTimer.cancel();

        // show hidden RESET and SHOW buttons
        // however don't remove START and STOP buttons despite being obsolete because replacing
        // buttons with those that have a different function is considered bad UX, with few exceptions
        crossfade(reset);
        crossfade(show);
        reset.setClickable(true);
        show.setClickable(true);

        // stop listening for step detections
        // note: listener registration is handled within buttons instead of onPause/onResume because
        // a running app is normally expected to run in the background as well
        sensorManager.unregisterListener(this);

        // timer is no longer ongoing, and there is a recorded run now
        isRunning = false;
        runFinished = true;
        // https://stackoverflow.com/questions/8654990/how-can-i-get-current-date-in-android
        // cannot use more modern solutions with Time classes because I'm working on API 24
        formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    // when the RESET button is clicked
    public void resetClick(View view) {
        // the button should be inaccessible while the timer is running, but this check exists just in
        // case
        if(isRunning) {
            Toast.makeText(this, "You can't reset a running timer!", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // reset the UI and time/step count to what it is at the beginning
        stepCounter = 0;
        steps.setText(String.valueOf(stepCounter));
        currentTime = "00:00";
        timer.setText(currentTime);
        formattedDate = "";
        reset.setClickable(false);
        show.setClickable(false);
        reset.setVisibility(View.INVISIBLE);
        show.setVisibility(View.INVISIBLE);

        // forget that a run was finished
        runFinished = false;
    }

    // when the SHOW button is clicked
    public void showClick(View view) {
        // the button should be inaccessible while the timer is running, but this check exists just in
        // case
        if(isRunning) {
            Toast.makeText(this, "You need to finish your new run before you see your results!", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // create and pass in necessary data to the results page
        Intent resultsActivity = new Intent(view.getContext(), ResultsActivity.class);
        resultsActivity.putExtra("formattedTime", currentTime);
        resultsActivity.putExtra("steps", stepCounter);
        resultsActivity.putExtra("runDate", formattedDate);

        // start the new page
        startActivity(resultsActivity);
    }

    // create the fade in effect for buttons
    // https://developer.android.com/develop/ui/views/animations/reveal-or-hide-view#java
    private void crossfade(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.animate().alpha(1f).setDuration(animationDuration).setListener(null);
    }
}