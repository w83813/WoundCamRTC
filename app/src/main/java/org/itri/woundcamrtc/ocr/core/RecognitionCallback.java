package org.itri.woundcamrtc.ocr.core;

public interface RecognitionCallback {

    void succeed(String result);

    void fail();
}
