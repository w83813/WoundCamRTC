package org.itri.woundcamrtc.preview;

import android.annotation.TargetApi;
import android.content.res.AssetManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.common.util.IOUtils;

import org.itri.woundcamrtc.AppResultReceiver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustWebClient extends WebViewClient {

    private static String TAG = "CustWebClient";

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if (AppResultReceiver.ALLOW_XSSL) {
            handler.proceed();//**重点**接受所有证书验证
        } else {
            super.onReceivedSslError(view, handler, error);
        }
    }

    @Deprecated
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return AppResultReceiver.mAssetServer.shouldInterceptRequest(url);
    }

    // For Lollipop and above.
    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return AppResultReceiver.mAssetServer.shouldInterceptRequest(request);
    }
}
