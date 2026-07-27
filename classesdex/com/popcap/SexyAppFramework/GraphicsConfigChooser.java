package com.popcap.SexyAppFramework;

import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GraphicsConfigChooser implements GLSurfaceView.EGLConfigChooser {
    private int mAlphaSize;
    private int mBlueSize;
    private int mDepthSize;
    private int mGLESVersion;
    private int mGreenSize;
    private int mRedSize;
    private int mStencilSize;
    private int[] mValue = new int[1];
    private static int EGL_OPENGL_ES_BIT = 1;
    private static int EGL_OPENGL_ES2_BIT = 4;

    public GraphicsConfigChooser(int InRedSize, int InGreenSize, int InBlueSize, int InAlphaSize, int InDepthSize, int InStencilSize, int InGLESVersion) {
        this.mRedSize = 0;
        this.mGreenSize = 0;
        this.mBlueSize = 0;
        this.mAlphaSize = 0;
        this.mDepthSize = 0;
        this.mStencilSize = 0;
        this.mGLESVersion = 0;
        this.mRedSize = InRedSize;
        this.mGreenSize = InGreenSize;
        this.mBlueSize = InBlueSize;
        this.mAlphaSize = InAlphaSize;
        this.mDepthSize = InDepthSize;
        this.mStencilSize = InStencilSize;
        this.mGLESVersion = InGLESVersion;
    }

    public EGLConfig chooseConfigFromList(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
        EGLConfig closestConfig = null;
        int closestDistance = 1000;
        for (EGLConfig config : configs) {
            int r = findConfigAttrib(egl, display, config, 12324, 0);
            int g = findConfigAttrib(egl, display, config, 12323, 0);
            int b = findConfigAttrib(egl, display, config, 12322, 0);
            int a = findConfigAttrib(egl, display, config, 12321, 0);
            int d = findConfigAttrib(egl, display, config, 12325, 0);
            int s = findConfigAttrib(egl, display, config, 12326, 0);
            int distance = Math.abs(r - this.mRedSize) + Math.abs(g - this.mGreenSize) + Math.abs(b - this.mBlueSize) + Math.abs(a - this.mAlphaSize) + Math.abs(d - this.mDepthSize) + Math.abs(s - this.mStencilSize);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestConfig = config;
            }
        }
        return closestConfig;
    }

    @Override // android.opengl.GLSurfaceView.EGLConfigChooser
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int RenderableType;
        switch (this.mGLESVersion) {
            case 1:
                RenderableType = EGL_OPENGL_ES_BIT;
                break;
            case 2:
                RenderableType = EGL_OPENGL_ES2_BIT;
                break;
            default:
                RenderableType = -1;
                break;
        }
        int[] configParams = {12324, this.mRedSize, 12323, this.mGreenSize, 12322, this.mBlueSize, 12321, this.mAlphaSize, 12325, this.mDepthSize, 12326, this.mStencilSize, 12352, RenderableType, 12339, 4, 12344};
        int[] num_configs = new int[1];
        if (!egl.eglChooseConfig(display, configParams, null, 0, num_configs)) {
            return null;
        }
        int iConfigCount = num_configs[0];
        EGLConfig[] configs = new EGLConfig[iConfigCount];
        if (egl.eglChooseConfig(display, configParams, configs, iConfigCount, null)) {
            return chooseConfigFromList(egl, display, configs);
        }
        return null;
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
        if (egl.eglGetConfigAttrib(display, config, attribute, this.mValue)) {
            int defaultValue2 = this.mValue[0];
            return defaultValue2;
        }
        return defaultValue;
    }
}
