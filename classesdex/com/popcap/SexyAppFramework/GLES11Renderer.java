package com.popcap.SexyAppFramework;

import android.opengl.GLES11Ext;
import android.util.Log;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GLES11Renderer extends GLESRenderer {
    private int colorRenderbuffer;
    private int defaultFramebuffer;

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public int getGLESVersion() {
        return 1;
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public boolean Startup() {
        if (!super.Startup()) {
            return false;
        }
        this.defaultFramebuffer = -1;
        this.colorRenderbuffer = 0;
        if (this.defaultFramebuffer != -1) {
            GLES11Ext.glBindFramebufferOES(36160, this.defaultFramebuffer);
            GLES11Ext.glBindRenderbufferOES(36161, this.colorRenderbuffer);
            GLES11Ext.glFramebufferRenderbufferOES(36160, 36064, 36161, this.colorRenderbuffer);
        } else {
            GLES11Ext.glBindFramebufferOES(36160, 0);
        }
        resizeFromLayer();
        return true;
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public void Shutdown() {
        if (this.defaultFramebuffer != -1) {
            GLES11Ext.glDeleteFramebuffersOES(1, new int[]{this.defaultFramebuffer}, 0);
            this.defaultFramebuffer = -1;
        }
        if (this.colorRenderbuffer != 0) {
            GLES11Ext.glDeleteRenderbuffersOES(1, new int[]{this.colorRenderbuffer}, 0);
            this.colorRenderbuffer = 0;
        }
        super.Shutdown();
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public boolean resizeFromLayer() {
        GLES11Ext.glBindRenderbufferOES(36161, this.colorRenderbuffer);
        int width = this.mGLView.getWidth();
        int height = this.mGLView.getHeight();
        GLES11Ext.glRenderbufferStorageOES(36161, 36194, width, height);
        if (GLES11Ext.glCheckFramebufferStatusOES(36160) == 36053) {
            return true;
        }
        Log.e("GLES11", String.format("Failed to make complete framebuffer object %x", Integer.valueOf(GLES11Ext.glCheckFramebufferStatusOES(36160))));
        return false;
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public int getSysFBO() {
        return this.defaultFramebuffer;
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public boolean setCurrentContext() {
        boolean success = this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext);
        if (!success) {
            return false;
        }
        GLES11Ext.glBindFramebufferOES(36160, 0);
        return true;
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public boolean setLoadingContext() {
        boolean success = this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mLoadingContext);
        return success;
    }
}
