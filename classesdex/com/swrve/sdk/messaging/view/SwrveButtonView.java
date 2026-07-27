package com.swrve.sdk.messaging.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveButtonView extends ImageView {
    private static int clickColor = Color.argb(100, 0, 0, 0);

    public SwrveButtonView(Context context) {
        super(context);
    }

    public SwrveButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwrveButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                setColorFilter(clickColor);
                invalidate();
                break;
            case 1:
                clearColorFilter();
                break;
        }
        return super.onTouchEvent(event);
    }
}
