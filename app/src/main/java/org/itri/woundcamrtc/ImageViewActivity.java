package org.itri.woundcamrtc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import android.support.v7.app.AppCompatActivity;

import org.itri.woundcamrtc.preview.FileUtility;
import org.itri.woundcamrtc.preview.TxtData;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageViewActivity extends AppCompatActivity {

  private final String TAG = getClass().getSimpleName();
  public final static int REQUEST_BODY_PART_PICKER = 1;

  private String evlId;
  private String itemId;
  private String ownerId;
  private String bodyPart;
  private String imagePath;

  private WebViewClient webViewClient;
  private WebChromeClient webChromeClient;
  private WebView webView;

  private String app_url;

  private EditText ownerIdInput;
  private EditText bodyPartInput;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_view);
    //
    ownerIdInput = (EditText) findViewById(R.id.patientIdInput);
    bodyPartInput = (EditText) findViewById(R.id.bodyPartInput);
    //
    try {
      Intent intent = this.getIntent();
      String params = intent.getStringExtra("params");
      JSONObject json = new JSONObject(params);
      //
      evlId = json.getString("evlId");
      itemId = json.getString("itemId");
      //
      if (!json.isNull("ownerId")) {
        ownerId = json.getString("ownerId");
        ownerIdInput.setText(ownerId);
      }
      if (!json.isNull("bodyPart")) {
        bodyPart = json.getString("bodyPart");
        bodyPartInput.setText(bodyPart);
      }
      imagePath = json.getString("imagePath");
      Log.v("照片路徑", imagePath);
      //
      app_url = "file:///android_asset/demo.html?img=" + imagePath;
    } catch (JSONException je) {
      Log.e(TAG, "Parse JSON Error", je);
    }
    //
    webViewClient = new WebViewClient();
    webChromeClient = new WebChromeClient();
    webView = initWebView();
    webView.loadUrl(app_url);
  }

  @Override
  public void onBackPressed() {
    Log.d(TAG, "press Back");
    //
    update();
    //
    this.finish();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, " onActivityResult: " + requestCode + ", " + (resultCode == Activity.RESULT_OK));
    if (requestCode == REQUEST_BODY_PART_PICKER && resultCode == Activity.RESULT_OK) {
      String bodyPart = data.getExtras().getString("bodyPart");
      Log.d(TAG, "bodyPart=" + bodyPart);
      if (bodyPart != null) {
        bodyPartInput.setText(bodyPart);
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  public void searchBodyPart(View view) {
    // 跳轉到 BodyPartActivity
    Log.d(TAG, "跳轉到 BodyPartActivity");
    Intent intent = new Intent(this, BodyPartActivity.class);
    startActivityForResult(intent, REQUEST_BODY_PART_PICKER);
  }

  public void startAnalysis(View view) {
    // 跳轉到 AnalysisActivity
    Log.d(TAG, "跳轉到 GrabcutActivity");
    Intent intent = new Intent(this, GrabcutActivity.class);
    intent.putExtra("fileName", imagePath.replace("file:///", "/").replace("%20", " "));
    startActivity(intent);
  }

  private void update() {
    try {
      TxtData data = FileUtility.getTxtDataFromDownload(this, evlId);
      //
      ownerId = ownerIdInput.getText().toString();
      bodyPart = bodyPartInput.getText().toString();
      //
      data.setOwnerId(ownerId);
      //
      JSONObject info = data.getInfo().get(itemId);
      info.put("bodyPart", bodyPart);
      // 將資料寫入TxtData檔
      boolean result = FileUtility.saveTxtDataToDownload(this, data);
      if (result) {
        Log.i(TAG, "更新 Txt 資料成功");
      } else {
        Log.i(TAG, "更新 Txt 資料失敗");
      }
    } catch (Exception ex) {
      Log.e(TAG, "更新 Txt 資料錯誤", ex);
    }
  }

  /**
   * 初始化WebView物件
   * @return WebView
   */
  private WebView initWebView() {
    WebView webView = (WebView) findViewById(R.id.imageWebview);
    // 如果需要用戶輸入帳號密碼，必須設置支持手勢焦點
    webView.requestFocusFromTouch();
    // 取消滾動條
    webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
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
    return webView;
  }

}
