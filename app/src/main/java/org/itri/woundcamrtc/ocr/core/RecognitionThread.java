package org.itri.woundcamrtc.ocr.core;

import org.opencv.core.Mat;

public class RecognitionThread implements Runnable {
    private Mat mMat;
    private RecognitionCallback mCallback;

    public RecognitionThread(Mat mat, RecognitionCallback callback) {
        this.mMat = mat;
        this.mCallback = callback;
    }

    @Override
    public void run() {
        if (mMat == null && null != mCallback) {
            mCallback.fail();
            return;
        }
        mCallback.succeed(RecognitionEngine.Generate().detectText(mMat));
    }
}
