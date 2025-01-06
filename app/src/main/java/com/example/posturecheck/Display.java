package com.example.posturecheck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

public class Display extends MaterialCardView {

    Rect dstRect;
    Rect srcRect = new Rect(0,0,480,640);

    Bitmap b;
    public Display(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void getBitmap(Bitmap bitmap) {
        this.b = bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        invalidate();
        dstRect = new Rect(0,0,getRight(),getRight()*4/3);
        if (b != null) {
//            Log.d("POSTURE", String.valueOf(b.getWidth())+' '+String.valueOf(b.getHeight()));
            canvas.drawBitmap(b,srcRect,dstRect,null);
        }
    }
}
