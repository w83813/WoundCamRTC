package org.itri.woundcamrtc.ocr.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

final class AutoFocusCallback implements Camera.AutoFocusCallback {
    private static final String TAG = AutoFocusCallback.class.getName();
    private static final long AUTO_FOCUS_INTERVAL_MS = 500L; //自动对焦时间

    private Handler mAutoFocusHandler;
    private int mAutoFocusMessage;

    void setHandler(Handler autoFocusHandler, int autoFocusMessage) {
        this.mAutoFocusHandler = autoFocusHandler;
        this.mAutoFocusMessage = autoFocusMessage;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (mAutoFocusHandler != null) {
            Message message = mAutoFocusHandler.obtainMessage(mAutoFocusMessage, success);
            mAutoFocusHandler.sendMessageDelayed(message, AUTO_FOCUS_INTERVAL_MS);
            mAutoFocusHandler = null;
        } else {
            Log.v(TAG, "Got auto-focus callback, but no handler for it");
        }
    }
}
