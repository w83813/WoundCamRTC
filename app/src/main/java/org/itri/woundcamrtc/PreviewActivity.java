package org.itri.woundcamrtc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

public class PreviewActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        //
        webViewClient = new CustWebClient();
        webChromeClient = new CustWebChromeClient(this);
        webView = initWebView();

        AppResultReceiver.app_url_base = "";
        language = Locale.getDefault().getLanguage();

         try {
	        if (AppResultReceiver.mAssetServer == null) {
	            AppResultReceiver.mAssetServer = new WebViewLocalServer(this);
	        }
	        if (AppResultReceiver.mAssetServerDetails == null) {
	            AppResultReceiver.mAssetServerDetails = AppResultReceiver.mAssetServer.hostAssets("", "/", true, true);
	        }
	        AppResultReceiver.app_url_base = AppResultReceiver.mAssetServerDetails.getHttpPrefix().buildUpon().appendPath("").toString();
	
	        if (language.equals("zh")) {
	            app_url = AppResultReceiver.mAssetServerDetails.getHttpPrefix().buildUpon().appendPath("zh/index.html").toString();
	        } else {
	            app_url = AppResultReceiver.mAssetServerDetails.getHttpPrefix().buildUpon().appendPath("en/index.html").toString();
	        } 
		} catch (Exception ex) {
			if (language.equals("zh")) {
	            app_url = "file:///android_asset/zh/index.html";
	        } else {
	            app_url = "file:///android_asset/en/index.html";
	        }
		}

        if (savedInstanceState == null) {
            webView.loadUrl(app_url);
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
            AppResultReceiver.logoutPreExecute(PreviewActivity.this);
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

        AppResultReceiver.vibrating(this);
        Log.d(TAG, "WebView: " + this.webView.getUrl() + " press Back.");
//        if (this.webView.getUrl().equalsIgnoreCase(app_url)) {
//            this.webView.destroy();
//            this.finish();
//        }else if(this.webView.getUrl().startsWith("file:///android_asset/demo.html?")){
//            this.webView.loadUrl("javascript:goback()");
//            //this.webView.loadUrl(app_url);
//            //this.webView.goBack();
//            //super.onBackPressed();
//        }else if (this.webView.getUrl().startsWith("file:///android_asset/openImage.html?")) {
//            this.webView.goBack();
//        }
        String webViewUrl = this.webView.getUrl();
        if (webViewUrl.equalsIgnoreCase(app_url) || webViewUrl.indexOf("zh/index.html") >= 0 || webViewUrl.indexOf("en/index.html") >= 0) {
            isShowLogoutHint = false;
            this.webView.destroy();
            this.finish();
        } else if (webViewUrl.indexOf("demo.html?") >= 0) {
            this.webView.loadUrl("javascript:goback()");
            //app_url = this.webView.getUrl();
            //this.webView.loadUrl(app_url);
            //this.webView.goBack();
            //super.onBackPressed();
        } else if (webViewUrl.indexOf("openImage.html?") >= 0) {
            this.webView.goBack();
//            app_url = this.webView.getUrl();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, " onActivityResult: " + requestCode + ", " + (resultCode == Activity.RESULT_OK));
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_BODY_PART_PICKER || requestCode == REQUEST_ANALYSIS) {
                String url = data.getExtras().getString("url");
                String params = data.getExtras().getString("params");
                Log.d(TAG, "params=" + params);
                Log.d(TAG, "url=" + url);
                if (url.equals("demo")) {
                    gotoDemo(params);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void gotoDemo(String params) {
//        try {
//            StringBuilder sb = new StringBuilder();
//            JSONObject data = new JSONObject(params);
//            JSONArray names = data.names();
//            for (int i=0; i<names.length(); i++) {
//                String name = names.get(i).toString();
//                String value = data.getString(name);
//                sb.append(name).append("=").append(value).append("&");
//            }
//            Log.d(TAG, "Params=" + sb.toString());
//            webView.loadUrl("file:///android_asset/demo.html?" + sb.toString());
//        } catch (Exception ex) {
//            Log.e(TAG, "Parse Params Error", ex);
//        }
        try {
            StringBuilder sb = new StringBuilder();
            JSONObject data = new JSONObject(params);
            JSONArray names = data.names();
            for (int i = 0; i < names.length(); i++) {
                String name = names.get(i).toString();
                String value = data.getString(name);
                sb.append(name).append("=").append(value).append("&");
            }
            Log.d(TAG, "Params=" + sb.toString());

            if (AppResultReceiver.app_url_base.equals("")) {
                if (language.equals("zh")) {
                    app_url = "file:///android_asset/zh/demo.html";
                } else {
                    app_url = "file:///android_asset/en/demo.html";
                }
            } else {
                if (language.equals("zh")) {
                    app_url = AppResultReceiver.mAssetServerDetails.getHttpPrefix().buildUpon().appendPath("zh/demo.html").toString();
                } else {
                    app_url = AppResultReceiver.mAssetServerDetails.getHttpPrefix().buildUpon().appendPath("en/demo.html").toString();
                }
            }
            webView.loadUrl(app_url + "?" + sb.toString());
        } catch (Exception ex) {
            Log.e(TAG, "Parse Params Error", ex);
        }
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
        jsInterface = new WebViewJavaScriptInterface(this, webView, this, null, null);
        webView.addJavascriptInterface(jsInterface, "app");

        //if (AppResultReceiver.DEBUG_LEVEL>0)

        webView.setWebContentsDebuggingEnabled(true); //for remote debug with chrome://inspect

        return webView;
    }

}
