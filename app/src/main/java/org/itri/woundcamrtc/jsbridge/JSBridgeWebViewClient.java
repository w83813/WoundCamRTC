package org.itri.woundcamrtc.jsbridge;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class JSBridgeWebViewClient extends WebViewClient {
    /**
     * 多頁面在同一個 WebView中打開,就是不新建activity或者調用系統瀏覽器打開
     */
    public boolean shouldOverrideUrl(WebView view, String url) {
        return shouldOverrideUrlLoading(view, url);
    }

    @Override
    @JavascriptInterface
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        if(url.startsWith("http://") && getRespStatus(url)==404) {
//            view.stopLoading();
//            view.loadUrl("file:///android_asset/html/404.html");
//        }else{
        view.loadUrl(url);
//        }
        return true;
//        return false; // system process
    }

    @Override
    @JavascriptInterface
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        // / Handle the error
        Toast.makeText(view.getContext(), "网络连接失败 ,请连接网络。", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);


//            view.addJavascriptInterface(new JSCallbackInterface());
//        mWebview.evaluateJavascript(script, new ValueCallback<String>() {
//
//            @Override
//            public void onReceiveValue(String value) {
//                Log.d(TAG, "onReceiveValue value=" + value);
//
//                if(value!=null){
//                    flag_get_deviceid=true;
//                }
//            }});
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

//    private int getRespStatus(String url) {
//        int status = -1;
//        try {
//            HttpHead head = new HttpHead(url);
//            HttpClient client = new DefaultHttpClient();
//            HttpResponse resp = client.execute(head);
//            status = resp.getStatusLine().getStatusCode();
//        } catch (IOException e) {}
//        return status;
//    }
}