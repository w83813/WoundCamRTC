package org.itri.woundcamrtc.jsbridge;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

import org.itri.woundcamrtc.WebviewActivity;
import org.itri.woundcamrtc.R;


public class JSBridgeWebChromeClient extends WebChromeClient {

    private WebviewActivity mainActivity;

    public void init(WebviewActivity mActivity) {
        mainActivity = mActivity;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        request.grant(request.getResources());
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        result.confirm(JSBridge.callJava(view, message));
        return true;
    }

    //处理javascript中的alert
    public boolean onJsAlert(WebView webView, String url, String message, final JsResult result) { //构建一个Builder来显示网页中的对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(webView.getContext());
        builder.setTitle(R.string.alert_title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok,
                new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
        builder.setCancelable(false);
        builder.create();
        builder.show();
        return true;
    }

    //处理javascript中的confirm
    public boolean onJsConfirm(final WebView webView, String url, final String message, final JsResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(webView.getContext());
        builder.setTitle(R.string.confirm_title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok,
                new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
//                            result.confirm(JSBridge.callJava(view, message));
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
        builder.setCancelable(false);
        builder.create();
        builder.show();
        return true;
    }

    //擴充緩存的容量
    public void onReachedMaxAppCacheSize(long spaceNeeded,
                                         long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
        quotaUpdater.updateQuota(spaceNeeded * 2);
    }

    public void onExceededDatabaseQuota(String url, String databaseIdentifier,
                                        long currentQuota, long estimatedSize, long totalUsedQuota,
                                        WebStorage.QuotaUpdater quotaUpdater) {
        quotaUpdater.updateQuota(estimatedSize * 2);
    }

    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (consoleMessage.message().startsWith("Not allowed to load local resource: file:///android_asset/webkit/android-weberror.png")) {

        }
        return super.onConsoleMessage(consoleMessage);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (newProgress >= 50) {
            if (mainActivity.mRedirectURL != null && !mainActivity.mRedirectURL.equals("")) {
                mainActivity.mWebView.loadUrl(mainActivity.mRedirectURL);
                mainActivity.mRedirectURL = "";
            }
        }
    }

    //设置网页加载的进度条
//    public void onProgressChanged(WebView view, int newProgress) {
//        MainActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress * 100);
//        super.onProgressChanged(view, newProgress);
//    }
//
//    //设置应用程序的标题title
//    public void onReceivedTitle(WebView view, String title) {
//        MainActivity.this.setTitle(title);
//        super.onReceivedTitle(view, title);
//    }
}

