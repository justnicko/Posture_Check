package com.example.posturecheck.util;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.example.posturecheck.R;
import com.example.posturecheck.activity.MainActivity;
import com.example.posturecheck.activity.SettingsActivity;

import java.util.Map;

public class MainActivityHandler extends Handler {
    static MainActivityHandler handler;
    Boolean pendingWrongPostureMessage = false;
    MediaPlayer notificationSound, alarmSound;
    MainActivity mainActivity;
    Runnable timeIncrement = new Runnable() {

        @Override
        public void run() {
            if (PostureAnalyzer.anglesAnalyzed != null && !PostureAnalyzer.anglesAnalyzed.isEmpty() ) {
                MainActivity.trackTime++;
                mainActivity.timeTextViewUpdate();
            }
//            Log.d("tracktime",String.valueOf(MainActivity.trackTime));
            handler.postDelayed(this, 1000);
        }
    };

    Runnable angleDataUpdate = new Runnable() {

        @Override
        public void run() {
            AngleData.updateAngleData();
            handler.postDelayed(this, 1000);
        }
    };



    Runnable wrongPostureMessage = new Runnable() {
        @Override
        public void run() {
            if (notificationSound == null) {
                notificationSound = MediaPlayer.create(mainActivity, R.raw.notification);
            }
            notificationSound.start();
            pendingWrongPostureMessage = false;
        }
    };


    Runnable checkAngleDifference = new Runnable() {
        @Override
        public void run() {
            if (PostureAnalyzer.badPosture && !pendingWrongPostureMessage) {
                handler.postDelayed(wrongPostureMessage,10000);
                pendingWrongPostureMessage = true;
            } else if (!PostureAnalyzer.badPosture && pendingWrongPostureMessage) {
                handler.removeCallbacks(wrongPostureMessage);
                pendingWrongPostureMessage = false;
            }
            handler.postDelayed(this,1000);
        }
    };

    Runnable breakAlarm = new Runnable() {
        @Override
        public void run() {
            if (alarmSound == null) {
                alarmSound = MediaPlayer.create(mainActivity, R.raw.alarm);
            }
            alarmSound.start();
            handler.postDelayed(this,4000);
        }
    };

    Map<Runnable,Integer> runnables = Map.of(
            breakAlarm, SettingsActivity.breakAlarmTimerInterval,
            timeIncrement, 1000,
            checkAngleDifference, 1000);

    public static MainActivityHandler getMainActivityHandler(MainActivity mainActivity) {
        if (handler == null) {
            handler = new MainActivityHandler();
        }
        handler.mainActivity = mainActivity;
        return handler;
    }

    private MainActivityHandler() {
        super();
    }

    public void postDelayedAll() {
        runnables.forEach((k,v) -> {
            this.postDelayed(k,v);
        });
    }

    public void removeAll() {
        runnables.forEach((k,v) -> {
            this.removeCallbacks(k);
        });
    }

}
