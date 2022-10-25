package org.itri.woundcamrtc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.itri.woundcamrtc.preview.CustWebChromeClient;
import org.itri.woundcamrtc.preview.CustWebClient;
import org.itri.woundcamrtc.preview.WebViewJavaScriptInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.webviewlocalserver.WebViewLocalServer;

import org.itri.woundcamrtc.R;

import java.util.Locale;

public class WebviewActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private static final String BASE_URL = "file:///android_asset/html/three.js-r105/index.html";
    public static final String REQUESTED_3D_PATH = "requested_3d_path";
    public static final String REQUESTED_3D_FILE = "requested_3d_file";

    public boolean isShowLogoutHint = true; //顯示登出提醒
    //
    public final static int REQUEST_BODY_PART_PICKER = 1;
    public final static int REQUEST_ANALYSIS = 2;
    //
    private CustWebClient webViewClient;
    private CustWebChromeClient webChromeClient;
    private WebView webView;
    private WebViewJavaScriptInterface jsInterface;
    //
    private String app_url;
    private String language;


    public String mRedirectURL;
    public WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        //
        webViewClient = new CustWebClient();
        webChromeClient = new CustWebChromeClient(this);
        webView = initWebView();


        language = Locale.getDefault().getLanguage();

        Intent intent = getIntent();
        String path = intent.getStringExtra(REQUESTED_3D_PATH);
        String file = intent.getStringExtra(REQUESTED_3D_FILE);

        if (savedInstanceState == null) {
            app_url = AppResultReceiver.mAssetServerDetails.getHttpPrefix().buildUpon().appendPath("html/three.js-r105/index.html").toString();

            webView.loadUrl(app_url.replace("%2F","/") + "?PATH=" + path + "&FILE=" + file);

            String script = "getGen3DColorImage('" + path + "','" + file + "')";

            webView.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        isShowLogoutHint = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isShowLogoutHint) {
            AppResultReceiver.logoutPreExecute(WebviewActivity.this);
            isShowLogoutHint = false;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        this.webView.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        isShowLogoutHint = true;

        AppResultReceiver.vibrating(this);  isShowLogoutHint = false;
            this.webView.destroy();
            this.finish();
    }


    public WebViewJavaScriptInterface getJSInterface() {
        return this.jsInterface;
    }

    /**
     * 初始化WebView物件
     *
     * @return WebView
     */
    private WebView initWebView() {
        WebView webView = (WebView) findViewById(R.id.webview);
        // 如果需要用戶輸入帳號密碼，必須設置支持手勢焦點
        webView.requestFocusFromTouch();
        // 取消滾動條
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        //
        //webView.setInitialScale(100);
        //
        WebSettings webSettings = webView.getSettings();
        // 設置支持Java Script
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 將圖片調整到適合WebView大小
        webSettings.setUseWideViewPort(true);
        // 是否以概述模式加載頁面
        webSettings.setLoadWithOverviewMode(true);
        // 允許顯示網路圖片
        webSettings.setBlockNetworkImage(false);
        // 設置自動加載圖片
        webSettings.setLoadsImagesAutomatically(true);
        // BASE64 圖片顯示
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        // 設置支援縮放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        //是否顯示屏幕縮放控件
        webSettings.setDisplayZoomControls(false);
        // 設置緩存模式
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //
        // Enable DOM storage, and tell Android where to save the Database
        // Database paths are managed by the implementation
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        //是否支持文件訪問
        webSettings.setAllowFileAccess(true);
        //是否支持內容URL訪問
        webSettings.setAllowContentAccess(true);
        //JavaScript是否可以訪問任何來源的內容
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        //JavaScript是否可以訪問其他文件方案URL的內容
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setGeolocationEnabled(false);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        // 指定字體大小為100%
        webSettings.setTextZoom(100);
        webSettings.setDefaultTextEncodingName("utf-8");
        // 處理一般事件
        webView.setWebViewClient(webViewClient);
        webView.setClickable(true);
        //webView.setLongClickable(true);
        // 處理進階事件
        webView.setWebChromeClient(webChromeClient);
        //
        webView.setPadding(0, 0, 0, 0);
        //
        jsInterface = new WebViewJavaScriptInterface(this, webView, null, null, this);
        webView.addJavascriptInterface(jsInterface, "app");

        if (AppResultReceiver.DEBUG_LEVEL>0)
            webView.setWebContentsDebuggingEnabled(true); //for remote debug with chrome://inspect

        return webView;
    }

}
