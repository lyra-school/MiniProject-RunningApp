package com.example.miniproject_stepcounter;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class ResultsActivity extends AppCompatActivity {
    // references to used widgets
    TextView dateTv;
    TextView metresTv;
    TextView caloriesTv;
    TextView timeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_results);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // set header
        getSupportActionBar().setTitle(R.string.header2);

        // get references to necessary widgets
        dateTv = (TextView)findViewById(R.id.textView5);
        metresTv = (TextView)findViewById(R.id.textView6);
        caloriesTv = (TextView)findViewById(R.id.textView7);
        timeTv = (TextView)findViewById(R.id.textView8);

        int stepsMeasured = getIntent().getIntExtra("steps", 0);

        // formatting settings for display
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);

        // calculate and/or set information in the relevant textview
        dateTv.setText(getIntent().getStringExtra("runDate"));
        metresTv.setText(df.format((double)stepsMeasured * 0.8) + " m");
        caloriesTv.setText(df.format((double)stepsMeasured * 0.04) + " kcal");
        timeTv.setText(getIntent().getStringExtra("formattedTime"));
    }

    public void returnClick(View view) {
        finish();
    }
}