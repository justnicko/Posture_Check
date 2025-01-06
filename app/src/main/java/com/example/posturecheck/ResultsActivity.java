package com.example.posturecheck;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

public class ResultsActivity extends AppCompatActivity {
    int angleSelected = 0;
    final int ID_ANGLE_NECK_TO_BACK = 0;
    final int ID_ANGLE_ELBOW = 1;
    final int ID_ANGLE_BACK_TO_THIGH = 2;
    final int ID_ANGLE_KNEES = 3;

    final int[] angleDescriptions = {
            R.string.desc_head_to_back,
            R.string.desc_elbow,
            R.string.desc_back_to_thigh,
            R.string.desc_knees
    };

    TextView descView;
    LineChart lineChart;
    ImageButton buttonAnglePrev,buttonAngleNext;

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

        descView = findViewById(R.id.desc_view);
        lineChart = findViewById(R.id.line_chart);
        buttonAnglePrev = findViewById(R.id.angle_previous);
        buttonAngleNext = findViewById(R.id.angle_next);

        lineChart.post(new Runnable() {
            @Override
            public void run() {
                Paint paint = lineChart.getRenderer().getPaintRender();
                paint.setShader(new LinearGradient(0, 0, 0, lineChart.getHeight(), new int[] {Color.RED, ResourcesCompat.getColor(getResources(), R.color.cozy_green, null), Color.RED}, new float[] {0,.5f,1}, Shader.TileMode.REPEAT));
            }
        });

        lineChart.addOnLayoutChangeListener( new LineChart.OnLayoutChangeListener()
        {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Paint paint = lineChart.getRenderer().getPaintRender();
                paint.setShader(new LinearGradient(0, 0, 0, lineChart.getHeight(), new int[] {Color.RED, ResourcesCompat.getColor(getResources(), R.color.cozy_green, null), Color.RED}, new float[] {0,.5f,1}, Shader.TileMode.REPEAT));
            }
        });

        XAxis xAxis = lineChart.getXAxis();
        YAxis rightAxis = lineChart.getAxisRight();
        YAxis leftAxis = lineChart.getAxisLeft();
        xAxis.setDrawGridLines(false);
        rightAxis.setDrawGridLines(false);
        leftAxis.setDrawGridLines(false);
        rightAxis.setDrawTopYLabelEntry(false);
        leftAxis.setDrawAxisLine(false);
        rightAxis.setDrawAxisLine(false);
        xAxis.setDrawAxisLine(false);
//        xAxis.setLabelCount(2);
        rightAxis.setDrawLabels(false);
        leftAxis.setAxisMinimum(-30f);
        leftAxis.setAxisMaximum(30f);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        buttonAnglePrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angleSelected = Math.max(0,Math.min(angleSelected - 1,3));
                angleSelect(angleSelected);
            }
        });

        buttonAngleNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angleSelected = Math.max(0,Math.min(angleSelected + 1,3));
                angleSelect(angleSelected);
            }
        });

        angleSelect(0);

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());


    }
    public void angleSelect(int angleId) {
        if (MainActivity.trackTime >= 12) {
            descView.setText(angleDescriptions[angleId]);
            LineData angleData = AngleData.getAngleData(angleId);
            lineChart.notifyDataSetChanged();
            lineChart.setData(angleData);
            lineChart.invalidate();
            lineChart.setVisibleXRangeMaximum(MainActivity.trackTime);
            lineChart.setVisibleXRangeMinimum(MainActivity.trackTime);
            lineChart.setVisibleYRangeMaximum(60,lineChart.getAxisLeft().getAxisDependency());
            lineChart.setVisibleYRangeMinimum(60,lineChart.getAxisLeft().getAxisDependency());
        } else {
            descView.setText(R.string.desc_empty);
        }
    }
    public void startMainActivity(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void startSettingsActivity(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}