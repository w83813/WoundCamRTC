package org.itri.woundcamrtc.preview;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.List;

import org.itri.woundcamrtc.HistoryActivity;
import org.itri.woundcamrtc.PreviewActivity;
import org.itri.woundcamrtc.WebviewActivity;

/**
 * 客制化WebChromeClient
 * Created by schung on 2017/10/17.
 */

public class CustWebChromeClient extends WebChromeClient {

    private static String TAG = "CustWebChromeClient";
    //
    private PreviewActivity ctx;
    private HistoryActivity ctx2;
    private WebviewActivity ctx3;
    private ValueCallback<Uri[]> mFilePathListCallback;

    public CustWebChromeClient(PreviewActivity ctx) {
        super();
        this.ctx = ctx;
    }

    public CustWebChromeClient(HistoryActivity ctx) {
        super();
        this.ctx2 = ctx;
    }

    public CustWebChromeClient(WebviewActivity ctx) {
        super();
        this.ctx3 = ctx;
    }

    // 改寫顯示圖片的對話窗
    // For Android >= 5.0
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        mFilePathListCallback = filePathCallback;
        return openImageChooserActivity();
    }

    private boolean openImageChooserActivity() {
        Log.d(TAG, "onShowFileChooser");
        //
        final String mimeType = "image/*";
        final PackageManager packageManager = ctx.getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            Log.d(TAG, "啟動當 ImageChooser 開啟後的處理程式");
        }
        return true;
    }

    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        Log.i(TAG, "onPermissionRequest");
        this.ctx.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    Log.i(TAG, "onPermissionRequest run");
                    request.grant(request.getResources());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

}
