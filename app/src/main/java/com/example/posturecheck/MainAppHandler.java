package com.example.posturecheck;

import android.os.Handler;

import java.util.List;
import java.util.Map;

public class MainAppHandler extends Handler {
    static MainAppHandler handler;

    public static MainAppHandler getMainAppHandler() {
        if (handler == null) {
            handler = new MainAppHandler();
        }
        return handler;
    }

    private MainAppHandler() {
        super();
    }

    public void postDelayedAll(Map<Runnable,Integer> runnables) {
        runnables.forEach((k,v) -> {
            this.postDelayed(k,v);
        });
    }

    public void removeAll(Map<Runnable,Integer> runnables) {
        runnables.forEach((k,v) -> {
            this.removeCallbacks(k);
        });
    }

}
