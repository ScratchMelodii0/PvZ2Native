package com.popcap.SexyAppFramework;

import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class AndroidUIEventManager implements GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
    static final /* synthetic */ boolean $assertionsDisabled;
    public static final int EVENT_BACK_BUTTON = 5;
    private static final int EVENT_FLICK = 4;
    private static final int EVENT_KEY = 1;
    private static final int EVENT_LONGPRESS = 2;
    private static final int EVENT_PINCH = 3;
    private static final int EVENT_TEXT_INPUT = 6;
    private static final int EVENT_TOUCH = 0;
    private static final float SCALE_THRESHOLD = 5.0E-4f;
    public static final int TEXTINPUT_BACKSPACE = 3;
    public static final int TEXTINPUT_COMMIT = 0;
    public static final int TEXTINPUT_COMPOSE = 1;
    public static final int TEXTINPUT_FINISH = 2;
    private static final int TouchPhase_TOUCH_BEGAN = 0;
    private static final int TouchPhase_TOUCH_CANCELLED = 4;
    private static final int TouchPhase_TOUCH_ENDED = 3;
    private static final int TouchPhase_TOUCH_MOVED = 1;
    private static final int TouchPhase_TOUCH_STATIONARY = 2;
    private float endScale;
    private float startScale;
    private float prevScaleMag = BitmapDescriptorFactory.HUE_RED;
    boolean mbAppHasFocus = false;
    Map<Integer, UITouchTracker> mTouches = new TreeMap();
    LinkedList<UIEvent> mEvents = new LinkedList<>();

    static {
        $assertionsDisabled = !AndroidUIEventManager.class.desiredAssertionStatus();
    }

    class UITouchTracker {
        float X;
        float Y;
        int mPointerId;
        int mTapCount;
        double mTimestampMS;
        int phase;
        float previousX;
        float previousY;

        UITouchTracker() {
        }

        void InitializeFromMotionEvent(MotionEvent event, int inPointerIdx, int inPhase, int actionIdx) {
            this.mPointerId = inPointerIdx;
            float x = event.getX(actionIdx);
            this.X = x;
            this.previousX = x;
            float y = event.getY(actionIdx);
            this.Y = y;
            this.previousY = y;
            this.mTapCount = 1;
            this.mTimestampMS = AndroidUIEventManager.this.GetTimeStamp();
            this.phase = inPhase;
        }

        void UpdateFromMotionEvent(MotionEvent event, int inPhase, int actionIdx) {
            this.previousX = this.X;
            this.previousY = this.Y;
            this.X = event.getX(actionIdx);
            this.Y = event.getY(actionIdx);
            this.mTimestampMS = AndroidUIEventManager.this.GetTimeStamp();
            this.phase = inPhase;
            if (inPhase == 1 && this.X == this.previousX && this.Y == this.previousY) {
                this.phase = 2;
            }
        }

        void MarkAsCancelled() {
            this.previousX = this.X;
            this.previousY = this.Y;
            this.mTimestampMS = AndroidUIEventManager.this.GetTimeStamp();
            this.phase = 4;
        }
    }

    abstract class UIEvent {
        public abstract boolean Serialize(ByteBuffer byteBuffer);

        UIEvent() {
        }
    }

    class UITouchEvent extends UIEvent {
        static final /* synthetic */ boolean $assertionsDisabled;
        int X;
        int Y;
        int ident;
        int phase;
        int previousX;
        int previousY;
        int tapCount;
        double timestamp;

        static {
            $assertionsDisabled = !AndroidUIEventManager.class.desiredAssertionStatus();
        }

        public UITouchEvent(UITouchTracker InTouch) {
            super();
            this.ident = InTouch.mPointerId + 1;
            if (!$assertionsDisabled && this.ident == 0) {
                throw new AssertionError();
            }
            this.X = (int) InTouch.X;
            this.Y = (int) InTouch.Y;
            this.previousX = (int) InTouch.previousX;
            this.previousY = (int) InTouch.previousY;
            this.timestamp = InTouch.mTimestampMS;
            this.phase = InTouch.phase;
        }

        @Override // com.popcap.SexyAppFramework.AndroidUIEventManager.UIEvent
        public boolean Serialize(ByteBuffer buf) {
            try {
                buf.putInt(0);
                buf.putInt(this.ident);
                buf.putInt(this.X);
                buf.putInt(this.Y);
                buf.putInt(this.previousX);
                buf.putInt(this.previousY);
                buf.putInt(1);
                buf.putDouble(this.timestamp);
                buf.putInt(this.phase);
                buf.putInt(-559038737);
                buf.putInt(-559038737);
                return true;
            } catch (BufferOverflowException e) {
                return false;
            }
        }
    }

    public class UIKeyEvent extends UIEvent {
        int mKeyCode;
        int mKeyEvent;
        int mRepeatCount;
        double mTimestampMS;
        int mUnicodeChar;

        public UIKeyEvent(KeyEvent event) {
            super();
            this.mKeyCode = event.getKeyCode();
            this.mUnicodeChar = event.getUnicodeChar();
            this.mKeyEvent = event.getAction();
            this.mRepeatCount = event.getRepeatCount();
            this.mTimestampMS = AndroidUIEventManager.this.GetTimeStamp();
        }

        @Override // com.popcap.SexyAppFramework.AndroidUIEventManager.UIEvent
        public boolean Serialize(ByteBuffer buf) {
            try {
                buf.putInt(1);
                buf.putInt(this.mKeyCode);
                buf.putInt(this.mUnicodeChar);
                buf.putInt(this.mKeyEvent);
                buf.putDouble(this.mTimestampMS);
                buf.putInt(this.mRepeatCount);
                buf.putInt(-559038737);
                return true;
            } catch (BufferOverflowException e) {
                return false;
            }
        }
    }

    public class UILongPressEvent extends UIEvent {
        int X;
        int Y;

        public UILongPressEvent(MotionEvent event) {
            super();
            int actionIdx = event.getActionIndex();
            this.X = (int) event.getX(actionIdx);
            this.Y = (int) event.getY(actionIdx);
        }

        @Override // com.popcap.SexyAppFramework.AndroidUIEventManager.UIEvent
        public boolean Serialize(ByteBuffer buf) {
            try {
                buf.putInt(2);
                buf.putInt(this.X);
                buf.putInt(this.Y);
                buf.putInt(-559038737);
                return true;
            } catch (BufferOverflowException e) {
                return false;
            }
        }
    }

    public class UIPinchEvent extends UIEvent {
        int X;
        int Y;
        float scaleDelta;
        float scaleDistSq;

        public UIPinchEvent(int X, int Y, float scaleDistSq, float scaleDelta) {
            super();
            this.X = X;
            this.Y = Y;
            this.scaleDistSq = scaleDistSq;
            this.scaleDelta = scaleDelta;
        }

        @Override // com.popcap.SexyAppFramework.AndroidUIEventManager.UIEvent
        public boolean Serialize(ByteBuffer buf) {
            try {
                buf.putInt(3);
                buf.putInt(this.X);
                buf.putInt(this.Y);
                buf.putFloat(this.scaleDistSq);
                buf.putFloat(this.scaleDelta);
                buf.putInt(-559038737);
                buf.putInt(-559038737);
                buf.putInt(-559038737);
                return true;
            } catch (BufferOverflowException e) {
                return false;
            }
        }
    }

    public class UIFlickEvent extends UIEvent {
        int X;
        int Y;
        double velocityX;
        double velocityY;

        public UIFlickEvent(int X, int Y, float velocityX, float velocityY) {
            super();
            this.X = X;
            this.Y = Y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        @Override // com.popcap.SexyAppFramework.AndroidUIEventManager.UIEvent
        public boolean Serialize(ByteBuffer buf) {
            try {
                buf.putInt(4);
                buf.putInt(this.X);
                buf.putInt(this.Y);
                buf.putDouble(this.velocityX);
                buf.putDouble(this.velocityY);
                buf.putInt(-559038737);
                return true;
            } catch (BufferOverflowException e) {
                return false;
            }
        }
    }

    public class UIBackButtonEvent extends UIEvent {
        public UIBackButtonEvent() {
            super();
        }

        @Override // com.popcap.SexyAppFramework.AndroidUIEventManager.UIEvent
        public boolean Serialize(ByteBuffer buf) {
            try {
                buf.putInt(5);
                buf.putInt(-559038737);
                buf.putInt(-559038737);
                buf.putInt(-559038737);
                return true;
            } catch (BufferOverflowException e) {
                return false;
            }
        }
    }

    public class UITextInputEvent extends UIEvent {
        int action;
        byte[] textUTF8Bytes;

        public UITextInputEvent(int action, byte[] textUTF8Bytes) {
            super();
            this.action = action;
            this.textUTF8Bytes = textUTF8Bytes;
        }

        @Override // com.popcap.SexyAppFramework.AndroidUIEventManager.UIEvent
        public boolean Serialize(ByteBuffer buf) {
            try {
                buf.putInt(6);
                buf.putInt(this.action);
                if (this.textUTF8Bytes != null) {
                    int len = this.textUTF8Bytes.length;
                    buf.putInt(len);
                    buf.put(this.textUTF8Bytes);
                    int nPadBytes = (4 - (len % 4)) % 4;
                    for (int i = 0; i < nPadBytes; i++) {
                        buf.put((byte) 0);
                    }
                } else {
                    buf.putInt(0);
                }
                return true;
            } catch (BufferOverflowException e) {
                return false;
            }
        }
    }

    public void HandleGotFocus() {
        this.mbAppHasFocus = true;
    }

    public void HandleLostFocus() {
        this.mbAppHasFocus = false;
        for (UITouchTracker tracker : this.mTouches.values()) {
            tracker.MarkAsCancelled();
            EnqueueEvent(new UITouchEvent(tracker));
        }
        this.mTouches.clear();
    }

    public boolean HandleTouchEvent(MotionEvent event) {
        UITouchTracker touch;
        if (!this.mbAppHasFocus) {
            return false;
        }
        int iPtrCount = event.getPointerCount();
        int action = event.getActionMasked();
        switch (action) {
            case 0:
            case 5:
                int actionIdx = event.getActionIndex();
                int pointerId = event.getPointerId(actionIdx);
                if (this.mTouches.containsKey(Integer.valueOf(pointerId))) {
                    this.mTouches.remove(Integer.valueOf(pointerId));
                    if (!$assertionsDisabled) {
                        throw new AssertionError();
                    }
                }
                UITouchTracker newUITouch = new UITouchTracker();
                newUITouch.InitializeFromMotionEvent(event, pointerId, 0, actionIdx);
                this.mTouches.put(Integer.valueOf(pointerId), newUITouch);
                EnqueueEvent(new UITouchEvent(newUITouch));
                break;
            case 1:
            case 6:
                int actionIdx2 = event.getActionIndex();
                int pointerId2 = event.getPointerId(actionIdx2);
                if (this.mTouches.containsKey(Integer.valueOf(pointerId2))) {
                    UITouchTracker touch2 = this.mTouches.remove(Integer.valueOf(pointerId2));
                    if (touch2 != null) {
                        touch2.UpdateFromMotionEvent(event, 3, actionIdx2);
                        EnqueueEvent(new UITouchEvent(touch2));
                    }
                } else if (!$assertionsDisabled) {
                    throw new AssertionError();
                }
                break;
            case 2:
                for (int actionIdx3 = 0; actionIdx3 < iPtrCount; actionIdx3++) {
                    int pointerId3 = event.getPointerId(actionIdx3);
                    if (this.mTouches.containsKey(Integer.valueOf(pointerId3))) {
                        UITouchTracker touch3 = this.mTouches.get(Integer.valueOf(pointerId3));
                        touch3.UpdateFromMotionEvent(event, 1, actionIdx3);
                        EnqueueEvent(new UITouchEvent(touch3));
                    } else if (!$assertionsDisabled) {
                        throw new AssertionError();
                    }
                }
                break;
            case 3:
                int actionIdx4 = event.getActionIndex();
                int pointerId4 = event.getPointerId(actionIdx4);
                if (this.mTouches.containsKey(Integer.valueOf(pointerId4)) && (touch = this.mTouches.remove(Integer.valueOf(pointerId4))) != null) {
                    touch.UpdateFromMotionEvent(event, 4, actionIdx4);
                    EnqueueEvent(new UITouchEvent(touch));
                }
                break;
        }
        return true;
    }

    public boolean HandleKeyEvent(KeyEvent InEvent) {
        if (!this.mbAppHasFocus) {
            return false;
        }
        EnqueueEvent(new UIKeyEvent(InEvent));
        return true;
    }

    public boolean HandleTextInputEvent(int action, byte[] textUTF8Bytes) {
        EnqueueEvent(new UITextInputEvent(action, textUTF8Bytes));
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public double GetTimeStamp() {
        return System.nanoTime() / 1.0E9d;
    }

    private synchronized void EnqueueEvent(UIEvent event) {
        this.mEvents.addLast(event);
    }

    public boolean ProcessEvents(ByteBuffer outData) {
        int iEventCount = 0;
        if (outData == null) {
            return false;
        }
        outData.order(ByteOrder.LITTLE_ENDIAN);
        outData.rewind();
        outData.position(16);
        boolean bMoreResults = false;
        synchronized (this) {
            while (true) {
                if (!this.mEvents.isEmpty()) {
                    UIEvent evt = this.mEvents.peek();
                    outData.mark();
                    if (evt.Serialize(outData)) {
                        this.mEvents.remove();
                        iEventCount++;
                    } else {
                        outData.reset();
                        bMoreResults = true;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        outData.putInt(0, iEventCount);
        return bMoreResults;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d("AndroidUIEventManager", "Fling! VelX: " + velocityX + " VelY: " + velocityY);
        if (this.mbAppHasFocus) {
            EnqueueEvent(new UIFlickEvent((int) e1.getX(), (int) e1.getY(), velocityX, velocityY));
        }
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onLongPress(MotionEvent e) {
        Log.d("AndroidUIEventManager", "Long Press!");
        EnqueueEvent(new UILongPressEvent(e));
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onShowPress(MotionEvent e) {
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScale(ScaleGestureDetector detector) {
        if (!this.mbAppHasFocus) {
            return false;
        }
        Log.d("AndroidUIEventManager", "Scale!");
        this.endScale = detector.getScaleFactor();
        Log.d("AndroidUIEventManager", "StartCur: " + this.startScale + " EndPrev: " + this.endScale);
        if (this.startScale >= this.endScale) {
            float mag = this.startScale - this.endScale;
            if (mag < this.prevScaleMag || mag - this.prevScaleMag < SCALE_THRESHOLD) {
                return false;
            }
            Log.d("AndroidUIEventManager", "Pinch Detected!");
            this.prevScaleMag = mag;
            onPinch(detector);
        } else if (this.startScale < this.endScale) {
            onZoom(detector);
            Log.d("AndroidUIEventManager", "Zoom detected!");
        }
        return true;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if (!this.mbAppHasFocus) {
            return false;
        }
        Log.d("AndroidUIEventManager", "ScaleBegin!");
        this.startScale = detector.getScaleFactor();
        return true;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public void onScaleEnd(ScaleGestureDetector detector) {
        Log.d("AndroidUIEventManager", "ScaleEnd!");
        this.prevScaleMag = BitmapDescriptorFactory.HUE_RED;
    }

    public void onZoom(ScaleGestureDetector detector) {
        onPinch(detector);
    }

    public void onPinch(ScaleGestureDetector detector) {
        Log.d("AndroidUIEventManager", "Pinched!!");
        float scaleDelta = this.endScale - this.startScale;
        UITouchTracker[] touches = (UITouchTracker[]) this.mTouches.values().toArray(new UITouchTracker[this.mTouches.size()]);
        if (touches.length < 2) {
            Log.d("AndroidUIEventManager", "Detected a malformed pinch action. Skipping.");
            return;
        }
        UITouchTracker touch1 = touches[0];
        UITouchTracker touch2 = touches[1];
        float distX = touch1.X - touch2.X;
        float distY = touch1.Y - touch2.Y;
        Log.d("AndroidUIEventManager", "distX: " + distX + " DistY: " + distY);
        float scaleDistSq = (distX * distX) + (distY * distY);
        int X = ((int) (touch1.X + touch2.X)) / 2;
        int Y = ((int) (touch1.Y + touch2.Y)) / 2;
        EnqueueEvent(new UIPinchEvent(X, Y, scaleDistSq, scaleDelta));
    }

    public void onBackButtonPressed() {
        Log.d("AndroidUIEventManager", "Back Button pressed.");
        EnqueueEvent(new UIBackButtonEvent());
    }
}
