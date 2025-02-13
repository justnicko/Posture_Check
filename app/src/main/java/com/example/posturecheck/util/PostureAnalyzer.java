package com.example.posturecheck.util;

import android.animation.ArgbEvaluator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.posturecheck.activity.SettingsActivity;
import com.example.posturecheck.util.vector.MathVector;
import com.example.posturecheck.util.vector.Point3D;
import com.example.posturecheck.widget.Display;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PostureAnalyzer implements ImageAnalysis.Analyzer {
    PoseDetectorOptions options =
            new PoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                    .build();
    PoseDetector poseDetector = PoseDetection.getClient(options);
    Canvas canvas, maskCanvas;
    Display display;
    Paint mPaint = new Paint();
    Paint maskPaint;
    public static Boolean paused = true;

    int[] lmsToCheck_r = {
            PoseLandmark.RIGHT_EAR,
            PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.RIGHT_ELBOW,
            PoseLandmark.RIGHT_WRIST,
            PoseLandmark.RIGHT_HIP,
            PoseLandmark.RIGHT_KNEE,
            PoseLandmark.RIGHT_ANKLE
    };

    int[] lmsToCheck_l = {
            PoseLandmark.LEFT_EAR,
            PoseLandmark.LEFT_SHOULDER,
            PoseLandmark.LEFT_ELBOW,
            PoseLandmark.LEFT_WRIST,
            PoseLandmark.LEFT_HIP,
            PoseLandmark.LEFT_KNEE,
            PoseLandmark.LEFT_ANKLE
    };

    int[] lmsToCheck = lmsToCheck_r;

    int[][] lmsToJoin;

    int[][] anglesToDraw;

    public static boolean badPosture = false;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    public static ArrayList<Float> anglesAnalyzed;

    boolean turnedLeft;
    Bitmap bitmap, maskBitmap, bufferBitmap, rotatedBitmap;

    public void clearMask(Canvas maskCanvas) {
        mPaint.setColor(Color.WHITE);
        maskCanvas.drawRect(0,0,maskCanvas.getWidth(),maskCanvas.getHeight(),mPaint);
    }
    public PostureAnalyzer(Display display) {
        super();
        this.display = display;
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(25);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeWidth(7);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        maskPaint = new Paint();
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    public static void pauseToggle() {
        paused = !paused;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
        ByteBuffer byteBuffer = imageProxy.getImage().getPlanes()[0].getBuffer();
        byteBuffer.rewind();

        bitmap = bitmap == null ? Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888) : bitmap;

        bitmap.copyPixelsFromBuffer(byteBuffer);

        Matrix matrix = new Matrix();
        if (SettingsActivity.usingFrontCamera) {
            matrix.postRotate(270);
            matrix.postScale(-1,1);
        } else {
            matrix.postRotate(90);
        }
        rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,imageProxy.getWidth(),imageProxy.getHeight(), matrix, false);

        if (bufferBitmap == null) {
            bufferBitmap = Bitmap.createBitmap(rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            maskBitmap = bufferBitmap.copy(Bitmap.Config.ARGB_8888, true);
        } else {
            bufferBitmap.eraseColor(Color.TRANSPARENT);
        }


        canvas = new Canvas(rotatedBitmap);
        maskCanvas = new Canvas(maskBitmap);
        Canvas bufferCanvas = new Canvas(bufferBitmap);
        bufferCanvas.drawBitmap(rotatedBitmap,0,0, null);
        bufferCanvas.drawBitmap(maskBitmap, 0, 0, maskPaint);

        Image mediaImage = imageProxy.getImage();
        if (rotatedBitmap != null && !paused) {
            InputImage image = InputImage.fromBitmap(bufferBitmap, 0);
            Task<Pose> result =
                    poseDetector.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<Pose>() {
                                        @Override
                                        public void onSuccess(Pose pose) {
                                            maskBitmap.eraseColor(Color.TRANSPARENT);
                                            badPosture = false;
                                            anglesAnalyzed = new ArrayList<>();
                                            lmsToJoin = new int[][] {
                                                    {lmsToCheck[0], lmsToCheck[1]},
                                                    {lmsToCheck[1], lmsToCheck[2]},
                                                    {lmsToCheck[2], lmsToCheck[3]},
                                                    {lmsToCheck[1], lmsToCheck[4]},
                                                    {lmsToCheck[4], lmsToCheck[5]},
                                                    {lmsToCheck[5], lmsToCheck[6]}
                                            };

                                            anglesToDraw = new int[][] {
                                                    {lmsToCheck[0], lmsToCheck[1], lmsToCheck[4], 155},
                                                    {lmsToCheck[1], lmsToCheck[2], lmsToCheck[3], SettingsActivity.trackingElbows? 95 : -1},
                                                    {lmsToCheck[1], lmsToCheck[4], lmsToCheck[5], 100},
                                                    {lmsToCheck[6], lmsToCheck[5], lmsToCheck[4], 95}
                                            };

                                            for (int[] lm_index : lmsToJoin) {
                                                if (pose.getPoseLandmark(lm_index[0]) == null || pose.getPoseLandmark(lm_index[0]).getInFrameLikelihood() < 0.5) {
                                                    canvas.drawText("Не всі частини тіла в межах камери",canvas.getWidth()/2,canvas.getHeight()/2, mPaint);
                                                    clearMask(maskCanvas);
                                                    return;
                                                }
                                            }

                                            MathVector vector = new MathVector(new Point3D(pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition3D()), new Point3D(pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE).getPosition3D()));
                                            if (vector.width() < 0) {
                                                lmsToCheck = lmsToCheck_l;
                                                turnedLeft = true;
                                            } else {
                                                lmsToCheck = lmsToCheck_r;
                                                turnedLeft = false;
                                            }


                                            MathVector v = new MathVector(
                                                    new Point3D(pose.getPoseLandmark(lmsToCheck[turnedLeft? 5 : 4]).getPosition3D()),
                                                    new Point3D(pose.getPoseLandmark(lmsToCheck[turnedLeft? 4 : 5]).getPosition3D())
                                            );
                                            MathVector.OrientationZ = v.ZRotation();

                                            for (int i = 0; i < lmsToJoin.length; i++) {
                                                int[] lm_index = lmsToJoin[i];
                                                PointF p1 = pose.getPoseLandmark(lm_index[0]).getPosition();
                                                PointF p2 = pose.getPoseLandmark(lm_index[1]).getPosition();
                                                Paint buf = new Paint(mPaint);
                                                buf.setColor(Color.BLACK);
                                                buf.setStrokeWidth(10);
                                                canvas.drawLine(p1.x,p1.y,p2.x,p2.y,buf);
                                                canvas.drawLine(p1.x,p1.y,p2.x,p2.y,mPaint);
                                                buf.setStrokeWidth(150);
                                                maskCanvas.drawLine(p1.x,p1.y,p2.x,p2.y,buf);
                                            }

                                            for (int i = 0; i < 4; i++) {
                                                int[] corner_lms = anglesToDraw[i];
                                                Point3D p1 = new Point3D(pose.getPoseLandmark(corner_lms[turnedLeft ? 2 : 0]).getPosition3D());
                                                Point3D p2 = new Point3D(pose.getPoseLandmark(corner_lms[1]).getPosition3D());
                                                Point3D p3 = new Point3D(pose.getPoseLandmark(corner_lms[turnedLeft ? 0 : 2]).getPosition3D());
                                                MathVector v1 = new MathVector(p2,p1);
                                                MathVector v2 = new MathVector(p2,p3);
                                                float angle = Math.round(v1.vectorsAngle(v2));
                                                float difference = corner_lms[3] != -1 ? corner_lms[3] - angle : 0;
                                                if (Math.abs(difference) >= SettingsActivity.possibleAngleDifference) {
                                                    badPosture = true;
                                                }
                                                anglesAnalyzed.add(Math.max(Math.min(corner_lms[3] - angle,30),-30));
                                                float a = 20;
                                                float b = a * MathVector.OrientationZ;
                                                Paint buf = new Paint(mPaint);
                                                buf.setStyle(Paint.Style.STROKE);
                                                buf.setStrokeWidth(10);
                                                buf.setColor(Color.BLACK);
                                                canvas.drawArc(p2.x - b, p2.y - a, p2.x + b, p2.y + a, v1.vectorAngle(),angle,true,buf);
                                                buf.setStrokeWidth(7);
                                                if (corner_lms[3] != -1) {
                                                    buf.setColor((int)argbEvaluator.evaluate(Math.min(1, Math.abs(difference) / (float) SettingsActivity.possibleAngleDifference),Color.GREEN,Color.RED));
                                                } else {
                                                    buf.setColor(Color.WHITE);
                                                }
                                                canvas.drawArc(p2.x - b, p2.y - a, p2.x + b, p2.y + a, v1.vectorAngle(),angle,true,buf);
                                                buf.setColor(Color.BLACK);
                                                buf.setStyle(Paint.Style.FILL_AND_STROKE);
                                                buf.setStrokeWidth(2);
                                                canvas.drawText(String.valueOf(angle),p2.x,p2.y,buf);
                                                buf.setColor(Color.WHITE);
                                                buf.setStyle(Paint.Style.FILL);
                                                canvas.drawText(String.valueOf(angle),p2.x,p2.y,buf);
                                            }

                                            Paint buf = new Paint(mPaint);
                                            buf.setColor(Color.BLACK);
                                            buf.setAlpha(100);
                                            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), buf);
                                            canvas.drawBitmap(bufferBitmap,0,0, null);
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                        }
                                    })
                            .addOnCompleteListener(
                                    new OnCompleteListener<Pose>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Pose> task) {
                                            display.getBitmap(rotatedBitmap);
                                            imageProxy.close();
                                        }
                                    }
                            );
        } else if (paused) {
            display.getBitmap(rotatedBitmap);
            imageProxy.close();
        }
    }
};
