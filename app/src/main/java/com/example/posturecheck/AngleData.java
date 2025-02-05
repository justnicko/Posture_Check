package com.example.posturecheck;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class AngleData {
    static List<List<Float>> tempAngleDataSets = new ArrayList<>();
    public static List<List<Entry>> angleDataSets = new ArrayList<>(List.of(
            new ArrayList<Entry>(),
            new ArrayList<Entry>(),
            new ArrayList<Entry>(),
            new ArrayList<Entry>()
    ));


    public static LineData getAngleData(int angleId) {
        LineDataSet lineDataSet = new LineDataSet(angleDataSets.get(angleId), "Кут між спиною та ногами");
        Log.d("log",String.valueOf(angleDataSets.get(angleId)));
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setLineWidth(10f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawCircles(false);
        LineData lineData = new LineData(lineDataSet);
        lineData.setDrawValues(false);
        return lineData;
    }

    public static void updateAngleData() {

        if (PostureAnalyzer.anglesAnalyzed.isEmpty()) {
            return;
        }

        tempAngleDataSets.add(PostureAnalyzer.anglesAnalyzed);
        if (tempAngleDataSets.size() >= 10) {
            for (int i = 0; i < 4; i++) {
                Float angleSum = 0f;
                for (int j = 0; j < 10; j++) {
                    angleSum += tempAngleDataSets.get(j).get(i);
                }
                angleDataSets.get(i).add(new Entry(MainActivity.trackTime,angleSum/10));
                Log.d("log",String.valueOf(angleDataSets));
            }
                tempAngleDataSets.clear();
        }
    }
}
