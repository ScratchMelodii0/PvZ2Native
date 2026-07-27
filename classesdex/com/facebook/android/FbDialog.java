package com.facebook.android;

import android.content.Context;
import android.os.Bundle;
import com.facebook.FacebookDialogException;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.android.Facebook;
import com.facebook.widget.WebDialog;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
@Deprecated
public class FbDialog extends WebDialog {
    private Facebook.DialogListener mListener;

    public FbDialog(Context context, String url, Facebook.DialogListener listener) {
        this(context, url, listener, 16973840);
    }

    public FbDialog(Context context, String url, Facebook.DialogListener listener, int theme) {
        super(context, url, theme);
        setDialogListener(listener);
    }

    public FbDialog(Context context, String action, Bundle parameters, Facebook.DialogListener listener) {
        super(context, action, parameters, 16973840, null);
        setDialogListener(listener);
    }

    public FbDialog(Context context, String action, Bundle parameters, Facebook.DialogListener listener, int theme) {
        super(context, action, parameters, theme, null);
        setDialogListener(listener);
    }

    private void setDialogListener(Facebook.DialogListener listener) {
        this.mListener = listener;
        setOnCompleteListener(new WebDialog.OnCompleteListener() { // from class: com.facebook.android.FbDialog.1
            @Override // com.facebook.widget.WebDialog.OnCompleteListener
            public void onComplete(Bundle values, FacebookException error) {
                FbDialog.this.callDialogListener(values, error);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callDialogListener(Bundle values, FacebookException error) {
        if (this.mListener != null) {
            if (values != null) {
                this.mListener.onComplete(values);
                return;
            }
            if (error instanceof FacebookDialogException) {
                FacebookDialogException facebookDialogException = (FacebookDialogException) error;
                DialogError dialogError = new DialogError(facebookDialogException.getMessage(), facebookDialogException.getErrorCode(), facebookDialogException.getFailingUrl());
                this.mListener.onError(dialogError);
            } else if (error instanceof FacebookOperationCanceledException) {
                this.mListener.onCancel();
            } else {
                FacebookError facebookError = new FacebookError(error.getMessage());
                this.mListener.onFacebookError(facebookError);
            }
        }
    }
}
