package com.swrve.sdk.messaging.view;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.swrve.sdk.messaging.ISwrveButtonListener;
import com.swrve.sdk.messaging.SwrveActionType;
import com.swrve.sdk.messaging.SwrveButton;
import com.swrve.sdk.messaging.SwrveImage;
import com.swrve.sdk.messaging.SwrveMessage;
import com.swrve.sdk.messaging.SwrveMessageFormat;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class SwrveMessageView extends RelativeLayout {
    protected static final String LOG_TAG = "SwrveMessagingSDK";
    protected static final String MARKET_PROTOCOL = "market://details?id=";
    protected static final String MARKET_URL = "http://play.google.com/store/apps/details?id=";
    protected Set<WeakReference<Bitmap>> bitmapCache;
    protected ISwrveButtonListener buttonListener;
    protected WeakReference<Context> contextRef;
    protected int dismissAnimation;
    protected boolean firstDraw;
    protected SwrveMessageFormat format;
    protected SwrveMessage message;
    protected float scale;
    protected int showAnimation;

    public SwrveMessageView(Context context) {
        super(context);
        this.firstDraw = true;
    }

    public SwrveMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.firstDraw = true;
    }

    public SwrveMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.firstDraw = true;
    }

    public SwrveMessageView(Context context, SwrveMessage message, SwrveMessageFormat format) {
        super(context);
        this.firstDraw = true;
        this.message = message;
        this.format = format;
        initializeLayout(context, message, format);
    }

    protected void initializeLayout(Context context, final SwrveMessage message, SwrveMessageFormat format) {
        try {
            this.contextRef = new WeakReference<>(context);
            this.bitmapCache = new HashSet();
            this.scale = format.getScale();
            setMinimumWidth(format.getSize().x);
            setMinimumHeight(format.getSize().y);
            setBackgroundColor(-16777216);
            setGravity(17);
            setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
            for (SwrveImage image : format.getImages()) {
                Bitmap backgroundImage = BitmapFactory.decodeFile(message.getCacheDir().getAbsolutePath() + "/" + image.getFile());
                if (backgroundImage == null) {
                }
                SwrveImageView imageView = createSwrveImage(context);
                this.bitmapCache.add(new WeakReference<>(backgroundImage));
                RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(backgroundImage.getWidth(), backgroundImage.getHeight());
                lparams.leftMargin = image.getPosition().x;
                lparams.topMargin = image.getPosition().y;
                imageView.setLayoutParams(lparams);
                imageView.setImageBitmap(backgroundImage);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                addView(imageView);
            }
            for (final SwrveButton button : format.getButtons()) {
                Bitmap backgroundImage2 = BitmapFactory.decodeFile(message.getCacheDir().getAbsolutePath() + "/" + button.getImage());
                if (backgroundImage2 == null) {
                }
                SwrveButtonView buttonView = createSwrveButton(context);
                this.bitmapCache.add(new WeakReference<>(backgroundImage2));
                RelativeLayout.LayoutParams lparams2 = new RelativeLayout.LayoutParams(backgroundImage2.getWidth(), backgroundImage2.getHeight());
                lparams2.leftMargin = button.getPosition().x;
                lparams2.topMargin = button.getPosition().y;
                buttonView.setLayoutParams(lparams2);
                buttonView.setImageBitmap(backgroundImage2);
                buttonView.setScaleType(ImageView.ScaleType.FIT_XY);
                buttonView.setOnClickListener(new View.OnClickListener() { // from class: com.swrve.sdk.messaging.view.SwrveMessageView.1
                    @Override // android.view.View.OnClickListener
                    public void onClick(View buttonView2) {
                        message.getMessageController().buttonWasPressedByUser(button);
                        boolean freeEvent = true;
                        if (SwrveMessageView.this.buttonListener != null) {
                            freeEvent = SwrveMessageView.this.buttonListener.onAction(button.getActionType(), button.getAction(), button.getGameId());
                        }
                        if (freeEvent) {
                            if (button.getActionType() == SwrveActionType.Dismiss) {
                                SwrveMessageView.this.dismiss();
                                return;
                            }
                            if (button.getActionType() == SwrveActionType.Install) {
                                SwrveMessageView.this.dismiss();
                                Context ctxt = SwrveMessageView.this.contextRef.get();
                                if (ctxt != null) {
                                    String appName = message.getMessageController().getAppStoreURLForGame(button.getGameId());
                                    try {
                                        ctxt.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(SwrveMessageView.MARKET_PROTOCOL + appName)));
                                    } catch (ActivityNotFoundException e) {
                                        ctxt.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(SwrveMessageView.MARKET_URL + appName)));
                                    }
                                }
                            }
                        }
                    }
                });
                addView(buttonView);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while initializing SwrveMessageView layout", e);
        }
    }

    public SwrveMessageFormat getFormat() {
        return this.format;
    }

    public int getShowAnimation() {
        return this.showAnimation;
    }

    public void setShowAnimation(int showAnimation) {
        this.showAnimation = showAnimation;
    }

    public int getDismissAnimation() {
        return this.dismissAnimation;
    }

    public void setDismissAnimation(int dismissAnimation) {
        this.dismissAnimation = dismissAnimation;
    }

    @Override // android.widget.RelativeLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        try {
            int count = getChildCount();
            int centerx = (int) (((double) l) + (((double) (r - l)) / 2.0d));
            int centery = (int) (((double) t) + (((double) (b - t)) / 2.0d));
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != 8) {
                    RelativeLayout.LayoutParams st = (RelativeLayout.LayoutParams) child.getLayoutParams();
                    int cCenterX = st.width / 2;
                    int cCenterY = st.height / 2;
                    if (this.scale != 1.0f) {
                        child.layout(((int) (this.scale * (st.leftMargin - cCenterX))) + centerx, ((int) (this.scale * (st.topMargin - cCenterY))) + centery, ((int) (this.scale * (st.leftMargin + cCenterX))) + centerx, ((int) (this.scale * (st.topMargin + cCenterY))) + centery);
                    } else {
                        child.layout((st.leftMargin - cCenterX) + centerx, (st.topMargin - cCenterY) + centery, st.leftMargin + cCenterX + centerx, st.topMargin + cCenterY + centery);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while onLayout in SwrveMessageView", e);
        }
    }

    protected SwrveImageView createSwrveImage(Context context) {
        return new SwrveImageView(context);
    }

    protected SwrveButtonView createSwrveButton(Context context) {
        return new SwrveButtonView(context);
    }

    public void setButtonListener(ISwrveButtonListener buttonListener) {
        this.buttonListener = buttonListener;
    }

    public void startAnimation() {
        try {
            if (this.showAnimation != 0) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), this.showAnimation);
                animation.setStartOffset(0L);
                startAnimation(animation);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while showing message", e);
        }
    }

    public void dismiss() {
        try {
            final ViewParent parent = getParent();
            if (this.dismissAnimation != 0) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), this.dismissAnimation);
                animation.setStartOffset(0L);
                animation.setAnimationListener(new Animation.AnimationListener() { // from class: com.swrve.sdk.messaging.view.SwrveMessageView.2
                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation anim) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation anim) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation anim) {
                        if (parent instanceof ViewGroup) {
                            SwrveMessageView.this.removeView(parent);
                        }
                    }
                });
                startAnimation(animation);
            } else {
                removeView(parent);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while dismissing message", e);
        }
    }

    protected void removeView(ViewParent parent) {
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }
        destroy();
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
        try {
            if (this.firstDraw) {
                this.firstDraw = false;
                this.message.getMessageController().messageWasShownToUser(this.format);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while processing first impression", e);
        }
    }

    public void destroy() {
        if (this.bitmapCache != null) {
            for (WeakReference<Bitmap> weakBitmap : this.bitmapCache) {
                Bitmap b = weakBitmap.get();
                if (b != null) {
                    b.recycle();
                }
            }
            this.bitmapCache.clear();
            this.bitmapCache = null;
        }
        System.gc();
    }

    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }
}
