package com.popcap.SexyAppFramework;

import android.opengl.GLES20;
import android.util.Log;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class GLES20Renderer extends GLESRenderer {
    private int colorRenderbuffer;
    private int defaultFramebuffer;

    GLES20Renderer() {
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public int getGLESVersion() {
        return 2;
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public boolean Startup() {
        if (!super.Startup()) {
            return false;
        }
        this.defaultFramebuffer = -1;
        this.colorRenderbuffer = 0;
        if (this.defaultFramebuffer != -1) {
            GLES20.glBindFramebuffer(36160, this.defaultFramebuffer);
            GLES20.glBindRenderbuffer(36161, this.colorRenderbuffer);
            GLES20.glFramebufferRenderbuffer(36160, 36064, 36161, this.colorRenderbuffer);
        } else {
            GLES20.glBindFramebuffer(36160, 0);
        }
        resizeFromLayer();
        return true;
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public void Shutdown() {
        if (this.defaultFramebuffer != -1) {
            GLES20.glDeleteFramebuffers(1, new int[]{this.defaultFramebuffer}, 0);
            this.defaultFramebuffer = -1;
        }
        if (this.colorRenderbuffer != 0) {
            GLES20.glDeleteRenderbuffers(1, new int[]{this.colorRenderbuffer}, 0);
            this.colorRenderbuffer = 0;
        }
        super.Shutdown();
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public boolean resizeFromLayer() {
        GLES20.glBindRenderbuffer(36161, this.colorRenderbuffer);
        if (GLES20.glCheckFramebufferStatus(36160) == 36053) {
            return true;
        }
        Log.e("GLES20", String.format("Failed to make complete framebuffer object %x", Integer.valueOf(GLES20.glCheckFramebufferStatus(36160))));
        return false;
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public int getSysFBO() {
        return -1;
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public boolean setCurrentContext() {
        boolean success = this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext);
        if (!success) {
            return false;
        }
        GLES20.glBindFramebuffer(36160, 0);
        return true;
    }

    @Override // com.popcap.SexyAppFramework.GLESRenderer
    public boolean setLoadingContext() {
        boolean success = this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mLoadingContext);
        return success;
    }
}
