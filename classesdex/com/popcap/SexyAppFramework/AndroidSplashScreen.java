package com.popcap.SexyAppFramework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class AndroidSplashScreen extends RelativeLayout {

    public static class SplashImage {
        private Rect mIntendedDisplayRect;
        private int mResourceID;

        SplashImage(int aResourceID, int aIntendedDisplayWidth, int aIntendedDisplayHeight) {
            this.mResourceID = aResourceID;
            this.mIntendedDisplayRect = new Rect(0, 0, aIntendedDisplayWidth, aIntendedDisplayHeight);
        }

        public int GetResourceID() {
            return this.mResourceID;
        }

        public Rect GetIntendedDisplayRect() {
            return this.mIntendedDisplayRect;
        }

        public Integer GetIntendedArea() {
            return Integer.valueOf(this.mIntendedDisplayRect.width() * this.mIntendedDisplayRect.height());
        }
    }

    public AndroidSplashScreen(Context context) {
        super(context);
        setVerticalGravity(16);
        setHorizontalGravity(1);
        View aSplashSurface = new AndroidSplashSurface(context);
        addView(aSplashSurface);
        ProgressBar aProgressBar = new ProgressBar(context);
        aProgressBar.setIndeterminate(true);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-2, -2);
        lp.addRule(13);
        addView(aProgressBar, lp);
    }

    private class AndroidSplashSurface extends SurfaceView implements SurfaceHolder.Callback {
        private Bitmap mBGBitmap;
        private int mHeight;
        private int mWidth;

        public AndroidSplashSurface(Context aContext) {
            super(aContext);
            getHolder().addCallback(this);
            getHolder().setFormat(1);
            setWillNotDraw(false);
            setVisibility(0);
            this.mBGBitmap = null;
            this.mWidth = -1;
            this.mHeight = -1;
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            invalidate();
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceCreated(SurfaceHolder holder) {
            invalidate();
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceDestroyed(SurfaceHolder holder) {
        }

        @Override // android.view.SurfaceView, android.view.View
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            this.mWidth = View.MeasureSpec.getSize(widthMeasureSpec);
            this.mHeight = View.MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(this.mWidth, this.mHeight);
            BitmapFactory.Options bfOptions = new BitmapFactory.Options();
            bfOptions.inScaled = false;
            this.mBGBitmap = BitmapFactory.decodeResource(getContext().getResources(), SexyAppFrameworkActivity.GetBestFitSplashImage(this.mWidth, this.mHeight), bfOptions);
            if (this.mBGBitmap != null) {
                this.mBGBitmap.setDensity(0);
            } else {
                Log.e("SexyAppFramework", String.format("Could not load splash resource for size: %dx%d", Integer.valueOf(this.mWidth), Integer.valueOf(this.mHeight)));
            }
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            if (this.mBGBitmap != null) {
                canvas.drawColor(-16777216);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setFilterBitmap(true);
                paint.setDither(true);
                int cx = (this.mWidth - this.mBGBitmap.getWidth()) / 2;
                int cy = 0;
                int cyWhitespace = ((int) (((double) this.mHeight) * 0.45d)) - this.mBGBitmap.getHeight();
                if (cyWhitespace > 0) {
                    cy = cyWhitespace / 2;
                }
                canvas.drawBitmap(this.mBGBitmap, cx, cy, (Paint) null);
            }
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent InEvent) {
            return true;
        }
    }
}
