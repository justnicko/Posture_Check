package com.example.posturecheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    Switch timerSwitch, frontCameraSwitch;
    SeekBar timerSeekbar, angleSeekbar;
    static Boolean usingFrontCamera = true;
    static int possibleAngleDifference = 10;
    static int breakAlarmTimerInterval = 3*1000*60*10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        timerSwitch = findViewById(R.id.switch_timer);
        timerSeekbar = findViewById(R.id.timer_seekbar);
        angleSeekbar = findViewById(R.id.angle_seekbar);
        angleSeekbar.setProgress(possibleAngleDifference/10 - 1);
        timerSeekbar.setProgress(breakAlarmTimerInterval/1000/60/10 - 1);
        frontCameraSwitch = findViewById(R.id.switch_front_camera);

        frontCameraSwitch.setChecked(usingFrontCamera);
        frontCameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                usingFrontCamera = frontCameraSwitch.isChecked();
            }
        });

        angleSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                possibleAngleDifference = (angleSeekbar.getProgress() + 1) * 10;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        timerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                breakAlarmTimerInterval = (timerSeekbar.getProgress()+1)*1000*60*10;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void startResultsActivity(View v) {
        Intent intent = new Intent(this, ResultsActivity.class);
        startActivity(intent);
    }

    public void startMainActivity(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void startActivity(View v) {
        Intent intent = new Intent(this, ResultsActivity.class);
        startActivity(intent);
    }
}