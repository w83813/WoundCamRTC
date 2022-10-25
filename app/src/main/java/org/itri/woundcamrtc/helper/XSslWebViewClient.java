package org.itri.woundcamrtc.helper;

import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.itri.woundcamrtc.AppResultReceiver;

public class XSslWebViewClient extends WebViewClient {
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if (AppResultReceiver.ALLOW_XSSL) {
            handler.proceed();//**重点**接受所有证书验证
        } else {
            super.onReceivedSslError(view, handler, error);
        }
    }
}