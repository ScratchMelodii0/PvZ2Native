package com.popcap.SexyAppFramework;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import java.io.UnsupportedEncodingException;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
class EditInputConnection extends BaseInputConnection {
    private CharSequence mComposingText;
    private AndroidUIEventManager mUIEventManager;
    private AndroidSurfaceView mView;

    public EditInputConnection(AndroidSurfaceView targetView, boolean fullEditor, AndroidUIEventManager uiEventManager) {
        super(targetView, fullEditor);
        this.mView = targetView;
        this.mUIEventManager = uiEventManager;
    }

    private void sendEvent(CharSequence text, int action) {
        if (text != null) {
            try {
                byte[] textUTF8Bytes = text.toString().getBytes("UTF-8");
                this.mUIEventManager.HandleTextInputEvent(action, textUTF8Bytes);
            } catch (UnsupportedEncodingException e) {
            }
        }
    }

    private boolean commitComposing(String commitText) {
        CharSequence composingText = this.mComposingText;
        this.mComposingText = null;
        if (composingText == null || !composingText.toString().equals(commitText)) {
            return false;
        }
        sendEvent("", 2);
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean beginBatchEdit() {
        super.beginBatchEdit();
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean endBatchEdit() {
        super.endBatchEdit();
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean clearMetaKeyStates(int states) {
        super.clearMetaKeyStates(states);
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean commitCompletion(CompletionInfo completionInfo) {
        super.commitCompletion(completionInfo);
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        super.commitCorrection(correctionInfo);
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean commitText(CharSequence text, int newCursorPosition) {
        super.commitText(text, newCursorPosition);
        if (!commitComposing(text.toString()) && text.length() > 0 && (text.length() != 1 || !Character.isDigit(text.charAt(0)))) {
            sendEvent(text, 0);
        }
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        super.deleteSurroundingText(beforeLength, afterLength);
        for (int i = 0; i < beforeLength; i++) {
            sendEvent("", 3);
        }
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean finishComposingText() {
        super.finishComposingText();
        if (this.mComposingText != null) {
            this.mComposingText = null;
            sendEvent("", 2);
            return true;
        }
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public int getCursorCapsMode(int reqModes) {
        int r = super.getCursorCapsMode(reqModes);
        return r;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        ExtractedText r = super.getExtractedText(request, flags);
        return r;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public CharSequence getSelectedText(int flags) {
        super.getSelectedText(flags);
        return null;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public CharSequence getTextAfterCursor(int length, int flags) {
        super.getTextAfterCursor(length, flags);
        return "";
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public CharSequence getTextBeforeCursor(int length, int flags) {
        super.getTextBeforeCursor(length, flags);
        return "";
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean performContextMenuAction(int id) {
        super.performContextMenuAction(id);
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean performEditorAction(int actionCode) {
        super.performEditorAction(actionCode);
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean performPrivateCommand(String action, Bundle data) {
        super.performPrivateCommand(action, data);
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean reportFullscreenMode(boolean enabled) {
        super.reportFullscreenMode(enabled);
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean sendKeyEvent(KeyEvent event) {
        super.sendKeyEvent(event);
        switch (event.getKeyCode()) {
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE /* 14 */:
            case IDownloaderClient.STATE_FAILED_UNLICENSED /* 15 */:
            case 16:
                if (commitComposing(Character.toString((char) event.getUnicodeChar()))) {
                    return true;
                }
                break;
            case 66:
            case 67:
                break;
            default:
                return true;
        }
        this.mUIEventManager.HandleKeyEvent(event);
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean setComposingRegion(int start, int end) {
        super.setComposingRegion(start, end);
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        super.setComposingText(text, newCursorPosition);
        if (text.length() > 0) {
            this.mComposingText = text;
            sendEvent(text, 1);
        } else {
            this.mComposingText = null;
            sendEvent("", 3);
        }
        return true;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean setSelection(int start, int end) {
        super.setSelection(start, end);
        return true;
    }
}
