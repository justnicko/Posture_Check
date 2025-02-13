package com.example.posturecheck.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.posturecheck.widget.Display;
import com.example.posturecheck.util.MainActivityHandler;
import com.example.posturecheck.util.PostureAnalyzer;
import com.example.posturecheck.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    int PERMISSION_REQUESTS = 1;
    PreviewView previewView;

    PoseDetectorOptions options =
            new PoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                    .build();
    PoseDetector poseDetector = PoseDetection.getClient(options);
    Canvas canvas;
    Paint mPaint = new Paint();
    Display display;
    TextView timeView;
    ImageButton pause_btn;
    public static int trackTime = 0;
    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN;

    ImageAnalysis.Analyzer imageAnalyzer;
    MainActivityHandler h;

    long millisFromStart;
    static boolean pendingWrongPostureMessage = false;

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
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

        display = findViewById(R.id.displayOverlay);
        timeView = findViewById(R.id.time);

        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(10);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));


        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());

        pause_btn = findViewById(R.id.button_pause);
        if (PostureAnalyzer.paused) {
            pause_btn.setImageResource(R.drawable.pause);
        } else {
            pause_btn.setImageResource(R.drawable.play);
        }

        h = MainActivityHandler.getMainActivityHandler(this);

        pause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostureAnalyzer.pauseToggle();
                if (PostureAnalyzer.paused) {
                    pause_btn.setImageResource(R.drawable.pause);
                    h.removeAll();
                    PostureAnalyzer.badPosture = false;
                } else {
                    pause_btn.setImageResource(R.drawable.play);
                    h.postDelayedAll();
                }
            }
        });

        if (PostureAnalyzer.paused) {
            pause_btn.setImageResource(R.drawable.pause);
        } else {
            h.postDelayedAll();
            pause_btn.setImageResource(R.drawable.play);
        }

        imageAnalyzer = new PostureAnalyzer(display);
        timeTextViewUpdate();
    }
    public void timeTextViewUpdate() {
        int seconds = trackTime;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        Log.d("tracktime",String.format("%d:%02d:%02d", hours, minutes, seconds));
        timeView.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!PostureAnalyzer.paused) {
            PostureAnalyzer.pauseToggle();
        }
        h.removeAll();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void startResultsActivity(View v) {
        Intent intent = new Intent(this, ResultsActivity.class);
        startActivity(intent);
        h.removeAll();
    }

    public void startSettingsActivity(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        h.removeAll();
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(SettingsActivity.usingFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                .build();


        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        // enable the following line if RGBA output is needed.
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//                        .setTargetResolution(new Size(480, 640))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ActivityCompat.getMainExecutor(this), imageAnalyzer);

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis);

//        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector);
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }


}