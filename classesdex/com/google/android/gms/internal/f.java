package com.google.android.gms.internal;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public final class f extends Drawable implements Drawable.Callback {
    private boolean aP;
    private int aR;
    private long aS;
    private int aT;
    private int aU;
    private int aV;
    private int aW;
    private int aX;
    private boolean aY;
    private b aZ;
    private Drawable ba;
    private Drawable bb;
    private boolean bc;
    private boolean bd;
    private boolean be;
    private int bf;

    private static final class a extends Drawable {
        private static final a bg = new a();
        private static final C0020a bh = new C0020a();

        /* JADX INFO: renamed from: com.google.android.gms.internal.f$a$a, reason: collision with other inner class name */
        private static final class C0020a extends Drawable.ConstantState {
            private C0020a() {
            }

            @Override // android.graphics.drawable.Drawable.ConstantState
            public int getChangingConfigurations() {
                return 0;
            }

            @Override // android.graphics.drawable.Drawable.ConstantState
            public Drawable newDrawable() {
                return a.bg;
            }
        }

        private a() {
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
        }

        @Override // android.graphics.drawable.Drawable
        public Drawable.ConstantState getConstantState() {
            return bh;
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -2;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter cf) {
        }
    }

    static final class b extends Drawable.ConstantState {
        int bi;
        int bj;

        b(b bVar) {
            if (bVar != null) {
                this.bi = bVar.bi;
                this.bj = bVar.bj;
            }
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            return this.bi;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new f(this);
        }
    }

    public f(Drawable drawable, Drawable drawable2) {
        this(null);
        drawable = drawable == null ? a.bg : drawable;
        this.ba = drawable;
        drawable.setCallback(this);
        this.aZ.bj |= drawable.getChangingConfigurations();
        drawable2 = drawable2 == null ? a.bg : drawable2;
        this.bb = drawable2;
        drawable2.setCallback(this);
        this.aZ.bj |= drawable2.getChangingConfigurations();
    }

    f(b bVar) {
        this.aR = 0;
        this.aV = MotionEventCompat.ACTION_MASK;
        this.aX = 0;
        this.aP = true;
        this.aZ = new b(bVar);
    }

    public boolean canConstantState() {
        if (!this.bc) {
            this.bd = (this.ba.getConstantState() == null || this.bb.getConstantState() == null) ? false : true;
            this.bc = true;
        }
        return this.bd;
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        boolean z = false;
        switch (this.aR) {
            case 1:
                this.aS = SystemClock.uptimeMillis();
                this.aR = 2;
                break;
            case 2:
                if (this.aS >= 0) {
                    float fUptimeMillis = (SystemClock.uptimeMillis() - this.aS) / this.aW;
                    z = fUptimeMillis >= 1.0f;
                    if (z) {
                        this.aR = 0;
                    }
                    this.aX = (int) ((Math.min(fUptimeMillis, 1.0f) * (this.aU - this.aT)) + this.aT);
                    break;
                }
            default:
                z = z;
                break;
        }
        int i = this.aX;
        boolean z2 = this.aP;
        Drawable drawable = this.ba;
        Drawable drawable2 = this.bb;
        if (z) {
            if (!z2 || i == 0) {
                drawable.draw(canvas);
            }
            if (i == this.aV) {
                drawable2.setAlpha(this.aV);
                drawable2.draw(canvas);
                return;
            }
            return;
        }
        if (z2) {
            drawable.setAlpha(this.aV - i);
        }
        drawable.draw(canvas);
        if (z2) {
            drawable.setAlpha(this.aV);
        }
        if (i > 0) {
            drawable2.setAlpha(i);
            drawable2.draw(canvas);
            drawable2.setAlpha(this.aV);
        }
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.aZ.bi | this.aZ.bj;
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        if (!canConstantState()) {
            return null;
        }
        this.aZ.bi = getChangingConfigurations();
        return this.aZ;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return Math.max(this.ba.getIntrinsicHeight(), this.bb.getIntrinsicHeight());
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return Math.max(this.ba.getIntrinsicWidth(), this.bb.getIntrinsicWidth());
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        if (!this.be) {
            this.bf = Drawable.resolveOpacity(this.ba.getOpacity(), this.bb.getOpacity());
            this.be = true;
        }
        return this.bf;
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void invalidateDrawable(Drawable who) {
        Drawable.Callback callback;
        if (!as.an() || (callback = getCallback()) == null) {
            return;
        }
        callback.invalidateDrawable(this);
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.aY && super.mutate() == this) {
            if (!canConstantState()) {
                throw new IllegalStateException("One or more children of this LayerDrawable does not have constant state; this drawable cannot be mutated.");
            }
            this.ba.mutate();
            this.bb.mutate();
            this.aY = true;
        }
        return this;
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect bounds) {
        this.ba.setBounds(bounds);
        this.bb.setBounds(bounds);
    }

    public Drawable r() {
        return this.bb;
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        Drawable.Callback callback;
        if (!as.an() || (callback = getCallback()) == null) {
            return;
        }
        callback.scheduleDrawable(this, what, when);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        if (this.aX == this.aV) {
            this.aX = alpha;
        }
        this.aV = alpha;
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter cf) {
        this.ba.setColorFilter(cf);
        this.bb.setColorFilter(cf);
    }

    public void startTransition(int durationMillis) {
        this.aT = 0;
        this.aU = this.aV;
        this.aX = 0;
        this.aW = durationMillis;
        this.aR = 1;
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void unscheduleDrawable(Drawable who, Runnable what) {
        Drawable.Callback callback;
        if (!as.an() || (callback = getCallback()) == null) {
            return;
        }
        callback.unscheduleDrawable(this, what);
    }
}
