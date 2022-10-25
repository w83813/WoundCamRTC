package org.itri.woundcamrtc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.itri.woundcamrtc.preview.CustWebChromeClient;
import org.itri.woundcamrtc.preview.WebViewJavaScriptInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

  private final String TAG = getClass().getSimpleName();


  public boolean isShowLogoutHint = true; //顯示登出提醒
  //
  public final static int REQUEST_BODY_PART_PICKER = 1;
  public final static int REQUEST_ANALYSIS = 2;
  public final static int REQUEST_Main = 3;
  //
  private WebViewClient webViewClient;
  private CustWebChromeClient webChromeClient;
  private WebView webView;
  private WebViewJavaScriptInterface jsInterface;
  private MainActivity activity;
  //
  private String app_url;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preview);
    //

    try{
        Intent intent = this.getIntent();
        String roleId = intent.getStringExtra("roleid");
        String ownerId = intent.getStringExtra("ownerId");
        String language = Locale.getDefault().getLanguage();

        if(language.equals("zh")) {
            app_url = "file:///android_asset/zh/historyList.html?roleId=" + roleId + "&ownerId=" + ownerId;
        }
        else{
            app_url = "file:///android_asset/en/historyList.html?roleId=" + roleId + "&ownerId=" + ownerId;
        }
        Log.v(TAG, "app_url:" + app_url);
    }
    catch(Exception e){
      Log.v(TAG, "intent error:" + e.getMessage());
    }

    webViewClient = new WebViewClient();
    webChromeClient = new CustWebChromeClient(this);
    webView = initWebView();

    webView.loadUrl(app_url);
  }

  @Override
  protected void onResume() {
    Log.d(TAG, "onResume()");
    super.onResume();

    isShowLogoutHint = true;
  }

  @Override
  public void onStop() {
    super.onStop();
    if(isShowLogoutHint) {
      AppResultReceiver.logoutPreExecute(HistoryActivity.this);
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
    Log.d(TAG, "WebView: " + this.webView.getUrl() + " press Back.");
    if (this.webView.getUrl().equalsIgnoreCase(app_url)) {

      isShowLogoutHint = false;
      this.webView.destroy();
      this.finish();
    } else if (this.webView.getUrl().startsWith("file:///android_asset/historyList.html?")) {
      this.webView.goBack();
    }
  }

  public void showTakePicInfo(String ownerId, String bodyPart) {
    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra("ownerId", ownerId);
    intent.putExtra("bodyPart", bodyPart);
    setResult(AppResultReceiver.SELECT_TAKEPICINFO_OK, intent);

  }
    /*
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
            } else if (requestCode == REQUEST_Main) {


                String params = data.getExtras().getString("params");
                Log.d("lagi", "params=" + params);
                setResult(AppResultReceiver.CHOOSE_BODY_RESULT_OK, data);

            }else{
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void gotoDemo(String params) {
        try {
            StringBuilder sb = new StringBuilder();
            JSONObject data = new JSONObject(params);
            JSONArray names = data.names();
            for (int i=0; i<names.length(); i++) {
                String name = names.get(i).toString();
                String value = data.getString(name);
                sb.append(name).append("=").append(value).append("&");
            }
            Log.d(TAG, "Params=" + sb.toString());
            webView.loadUrl("file:///android_asset/demo.html?" + sb.toString());
        } catch (Exception ex) {
            Log.e(TAG, "Parse Params Error", ex);
        }
    }*/

  public WebViewJavaScriptInterface getJSInterface() {
    return this.jsInterface;
  }

  /**
   * 初始化WebView物件
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
    //
    webSettings.setAllowFileAccess(true);
    webSettings.setAllowContentAccess(true);
    webSettings.setAllowUniversalAccessFromFileURLs(true);
    webSettings.setAllowFileAccessFromFileURLs(true);
    webSettings.setGeolocationEnabled(false);
    webSettings.setMediaPlaybackRequiresUserGesture(false);
    webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    webSettings.setDefaultTextEncodingName("utf-8");
    //webSettings.setMediaPlaybackRequiresUserGesture(WebSettings.RenderPriority.HIGH);
    // 指定字體大小為100%
    webSettings.setTextZoom(100);
    // 處理一般事件
    webView.setWebViewClient(webViewClient);
    webView.setClickable(true);
    //webView.setLongClickable(true);
    // 處理進階事件
    webView.setWebChromeClient(webChromeClient);
    //
    webView.setPadding(0, 0, 0, 0);
    //
    jsInterface = new WebViewJavaScriptInterface(this, webView, null, this, null);
    webView.addJavascriptInterface(jsInterface, "app");
//    if (AppResultReceiver.DEBUG_LEVEL>0)
      webView.setWebContentsDebuggingEnabled(true); //for remote debug with chrome://inspect

    //
    return webView;
  }

}
