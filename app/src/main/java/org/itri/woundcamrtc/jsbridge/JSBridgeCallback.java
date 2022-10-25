package org.itri.woundcamrtc.jsbridge;

import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;

import org.json.JSONObject;

import java.lang.ref.WeakReference;


public class JSBridgeCallback {
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private String mPort;
    private WeakReference<WebView> mWebViewRef;

    public enum CALLBACK {
        ONESHOT("javascript:JSBridge.onFinish('%s', %s);"),
        RECEIVING("javascript:JSBridge.onReceiving('%s', %s);"),
        STOP("javascript:JSBridge.onStop('%s', %s);");

        private String value;

        private CALLBACK(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public JSBridgeCallback(WebView view, String port) {
        mWebViewRef = new WeakReference<>(view);
        mPort = port;
    }

    public void apply(CALLBACK cb, JSONObject jsonObject) {
        final String execJs = String.format(cb.getValue(), mPort, String.valueOf(jsonObject));
        if (mWebViewRef != null && mWebViewRef.get() != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWebViewRef.get().loadUrl(execJs);
                }
            });
        }
    }
}