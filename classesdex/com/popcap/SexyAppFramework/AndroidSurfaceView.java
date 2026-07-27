package com.popcap.SexyAppFramework;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class AndroidSurfaceView extends GLSurfaceView {
    public static final int DEBUG_CHECK_GL_ERROR = 1;
    public static final int DEBUG_LOG_GL_CALLS = 2;
    private AndroidRenderer mAndroidRenderer;
    private EGL10 mEgl;
    private int mGLESVersionSupported;
    private GestureDetector mGestureDetector;
    private EditInputConnection mInputConnection;
    private int mOrigAppHeight;
    private int mOrigAppWidth;
    GLESRenderer mRenderer;
    private ScaleGestureDetector mScaleGestureDetector;
    private AndroidUIEventManager mUIEventManager;
    private float mViewScaleFactor;
    private WindowManager mWindowManager;
    public boolean mbDrawingFrames;
    public boolean mbFocusState;
    private boolean mbIsInitialized;
    public Object mbNotifyOnDrawEventObject;
    static boolean DEBUG = false;
    static String LogTAG = "SexyEGL";

    public interface AndroidEGLConfigChooserInterface {
        EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay);
    }

    public interface GLWrapper {
        GL wrap(GL gl);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public native void Native_onDrawFrame();

    /* JADX INFO: Access modifiers changed from: private */
    public native void Native_onSurfaceChanged(int i, int i2);

    /* JADX INFO: Access modifiers changed from: private */
    public native void Native_onSurfaceCreated();

    public AndroidSurfaceView(Context InContext) {
        super(InContext);
        this.mGLESVersionSupported = 0;
        this.mbDrawingFrames = false;
        this.mbFocusState = false;
        this.mViewScaleFactor = 0.5f;
    }

    public AndroidSurfaceView(Context InContext, WindowManager InWindowManager, AndroidUIEventManager InEventManager, int InGLESVersionSupported) {
        super(InContext);
        this.mGLESVersionSupported = 0;
        this.mbDrawingFrames = false;
        this.mbFocusState = false;
        this.mViewScaleFactor = 0.5f;
        this.mWindowManager = InWindowManager;
        this.mUIEventManager = InEventManager;
        this.mGLESVersionSupported = InGLESVersionSupported;
        this.mbIsInitialized = false;
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.mOrigAppWidth = 0;
        this.mOrigAppHeight = 0;
        this.mGestureDetector = new GestureDetector(InContext, this.mUIEventManager);
        this.mGestureDetector.setIsLongpressEnabled(true);
        this.mScaleGestureDetector = new ScaleGestureDetector(InContext, this.mUIEventManager);
        setOnTouchListener(new View.OnTouchListener() { // from class: com.popcap.SexyAppFramework.AndroidSurfaceView.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (!SexyAppFrameworkActivity.instance().SplashScreenShowing()) {
                    boolean GestureDetected = AndroidSurfaceView.this.mScaleGestureDetector.onTouchEvent(event) | AndroidSurfaceView.this.mGestureDetector.onTouchEvent(event);
                    return AndroidSurfaceView.this.mUIEventManager.HandleTouchEvent(event) | GestureDetected;
                }
                return false;
            }
        });
    }

    public void init(int InRedBits, int InGreenBits, int InBlueBits, int InAlphaBits, int InDepthBits, int InStencilBits) {
        if (Graphics_IsOpenGLES20()) {
            this.mRenderer = new GLES20Renderer();
        }
        if (this.mRenderer == null) {
            this.mRenderer = new GLES11Renderer();
        }
        if (InAlphaBits > 0) {
            SurfaceHolder holder = getHolder();
            holder.setFormat(-3);
        }
        setEGLContextFactory(new AndroidEGLContextFactory(this));
        setEGLConfigChooser(new GraphicsConfigChooser(InRedBits, InGreenBits, InBlueBits, InAlphaBits, InDepthBits, InStencilBits, this.mGLESVersionSupported));
        this.mAndroidRenderer = new AndroidRenderer(this);
        setRenderer(this.mAndroidRenderer);
        this.mbIsInitialized = true;
    }

    private boolean EglContextIsValid() {
        EGLContext currentContext = this.mEgl.eglGetCurrentContext();
        return currentContext == this.mRenderer.mEglContext || this.mRenderer.setCurrentContext();
    }

    private int Graphics_GetGLViewSysFBO() {
        return this.mRenderer.getSysFBO();
    }

    public boolean Graphics_IsOpenGLES20() {
        return this.mGLESVersionSupported >= 2;
    }

    private float Graphics_GetGLViewScaleFactor() {
        return this.mViewScaleFactor;
    }

    private void Graphics_SetGLViewScaleFactor(float InScaleFactor) {
        this.mViewScaleFactor = InScaleFactor;
    }

    private boolean Graphics_CanSetGLViewScaleFactor() {
        return true;
    }

    private void Graphics_GetScreenSizeInPixels(int[] outWidthHeight) {
        synchronized (this) {
            outWidthHeight[0] = this.mOrigAppWidth;
            outWidthHeight[1] = this.mOrigAppHeight;
        }
    }

    public void SetScreenSizeInPixels(int width, int height) {
        this.mOrigAppWidth = width;
        this.mOrigAppHeight = height;
    }

    private void Graphics_GetScreenSizeInPoints(int[] outWidthHeight) {
        synchronized (this) {
            Display disp = this.mWindowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            disp.getMetrics(metrics);
            float width = getWidth();
            float height = getHeight();
            float f = metrics.density;
            outWidthHeight[0] = Math.round(width * 1.0f);
            outWidthHeight[1] = Math.round(height * 1.0f);
        }
    }

    private float Graphics_GetPointSizeInPixels() {
        float f;
        synchronized (this) {
            Display disp = this.mWindowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            disp.getMetrics(metrics);
            f = metrics.density;
        }
        return f;
    }

    public final boolean IsInitailized() {
        return this.mbIsInitialized;
    }

    private static class AndroidEGLContextFactory implements GLSurfaceView.EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 12440;
        private AndroidSurfaceView mView;

        public AndroidEGLContextFactory(AndroidSurfaceView InView) {
            this.mView = null;
            this.mView = InView;
        }

        @Override // android.opengl.GLSurfaceView.EGLContextFactory
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            int GLESVers = this.mView.mRenderer.getGLESVersion();
            int[] attrib_list = null;
            if (GLESVers > 0) {
                attrib_list = new int[]{EGL_CONTEXT_CLIENT_VERSION, GLESVers, 12344};
            }
            checkEglError("Before eglCreateContext", egl);
            EGLContext newContext = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            if (newContext == EGL10.EGL_NO_CONTEXT) {
                checkEglError("After eglCreateContext", egl);
            }
            return newContext;
        }

        @Override // android.opengl.GLSurfaceView.EGLContextFactory
        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext deadContext) {
            if (deadContext != EGL10.EGL_NO_CONTEXT) {
                egl.eglDestroyContext(display, deadContext);
            }
        }

        private static void checkEglError(String prompt, EGL10 egl) {
            while (true) {
                int error = egl.eglGetError();
                if (error != 12288) {
                    Log.e(AndroidSurfaceView.LogTAG, String.format("%s: EGL error: 0x%x", prompt, Integer.valueOf(error)));
                } else {
                    return;
                }
            }
        }
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int InKeyCode, KeyEvent InEvent) {
        if (InKeyCode == 82) {
            return true;
        }
        return super.onKeyDown(InKeyCode, InEvent);
    }

    @Override // android.view.SurfaceView, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override // android.opengl.GLSurfaceView, android.view.SurfaceView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override // android.opengl.GLSurfaceView, android.view.SurfaceView, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override // android.view.SurfaceView, android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override // android.view.SurfaceView, android.view.View
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
    }

    @Override // android.opengl.GLSurfaceView
    public void onPause() {
        super.onPause();
        this.mAndroidRenderer.onPause();
    }

    public void SetFocusState(boolean inFocusState) {
        this.mbFocusState = inFocusState;
    }

    public boolean HasDrawnFirstFrame() {
        return !AndroidRenderer.mFirstDraw;
    }

    @Override // android.view.View
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override // android.view.View
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (this.mInputConnection == null) {
            this.mInputConnection = new EditInputConnection(this, false, this.mUIEventManager);
        }
        outAttrs.actionLabel = null;
        outAttrs.inputType = 1;
        outAttrs.imeOptions = 268435456;
        return this.mInputConnection;
    }

    @Override // android.view.View
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == 4) {
            if (event.getAction() == 0) {
                onKeyDown(keyCode, event);
            } else if (event.getAction() == 1) {
                onKeyUp(keyCode, event);
                SexyAppFrameworkActivity.instance().onKeyboardDismissedWithBackButton();
            }
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    private static class AndroidRenderer implements GLSurfaceView.Renderer {
        static boolean mFirstDraw = true;
        AndroidSurfaceView mGLView;

        AndroidRenderer(AndroidSurfaceView InView) {
            this.mGLView = null;
            this.mGLView = InView;
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onDrawFrame(GL10 gl) {
            this.mGLView.mbDrawingFrames = true;
            if (this.mGLView.mbNotifyOnDrawEventObject != null) {
                synchronized (this.mGLView.mbNotifyOnDrawEventObject) {
                    this.mGLView.mbNotifyOnDrawEventObject.notifyAll();
                    this.mGLView.mbNotifyOnDrawEventObject = null;
                }
            } else if (!this.mGLView.mbFocusState) {
                return;
            }
            this.mGLView.Native_onDrawFrame();
            if (mFirstDraw) {
                mFirstDraw = false;
                SexyAppFrameworkActivity.instance().RemoveSplashScreen();
            }
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.i("Surface", "surfaceCreated");
            this.mGLView.Native_onSurfaceCreated();
        }

        @Override // android.opengl.GLSurfaceView.Renderer
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.i("Surface", String.format("surfaceChanged %d %d", Integer.valueOf(width), Integer.valueOf(height)));
            this.mGLView.Native_onSurfaceChanged(width, height);
        }

        public void onPause() {
        }
    }
}
