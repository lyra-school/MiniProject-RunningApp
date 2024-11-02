package com.example.miniproject_stepcounter;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;

// modified from the prototype on moodle; https://stackoverflow.com/questions/9276858/how-to-add-a-countup-timer-on-android
public abstract class CountUpTimer extends CountDownTimer {
    private static final long INTERVAL_MS = 1000;
    private final long duration;
    private Button view;

    // edited to store a reference to a button
    protected CountUpTimer(long durationMs, Button view) {
        super(durationMs, INTERVAL_MS);
        this.duration = durationMs;
        this.view = view;
    }

    public abstract void onTick(int second);

    @Override
    public void onTick(long msUntilFinished) {
        int second = (int) ((duration - msUntilFinished) / 1000);
        onTick(second);
    }

    // edited to perform the button's function once the timer runs out
    @Override
    public void onFinish() {
        onTick(duration / 1000);
        view.callOnClick();
    }

}
