package org.itri.woundcamrtc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.longdo.mjpegviewer.MjpegView;

import org.itri.woundcamrtc.view.ToggleButtonGroupTableLayout;
import org.json.JSONObject;

public class BodyPartActivity extends Activity {
    private final String TAG = getClass().getSimpleName();
    private ToggleButtonGroupTableLayout grp_btn;
    private BodyPartActivity bActivity;
    private Context bContext;
    private boolean isShowLogoutHint = true; //顯示登出提醒
    private String part = "";
    public String params = "";
    private String where = "";
    private JSONObject txtData;
    MainActivity mainActivity;
    private LinearLayout mjview_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bContext = this;
        setContentView(R.layout.activity_bodypart);
        bActivity = this;

        Intent intent = this.getIntent();
        part = intent.getStringExtra("bodyPart");
        params = intent.getStringExtra("params");
        where = intent.getStringExtra("where");

        TabHost tabs = (TabHost) findViewById(R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec tab1 = tabs.newTabSpec("front");
        tab1.setContent(R.id.front_scroll);
        tab1.setIndicator(getString(R.string.front_body));

        TabHost.TabSpec tab2 = tabs.newTabSpec("back");
        tab2.setContent(R.id.back_scroll);
        tab2.setIndicator(getString(R.string.back_body));

        TabHost.TabSpec tab3 = tabs.newTabSpec("left");
        tab3.setContent(R.id.left_scroll);
        tab3.setIndicator(getString(R.string.left_body));

        TabHost.TabSpec tab4 = tabs.newTabSpec("left");
        tab4.setContent(R.id.right_scroll);
        tab4.setIndicator(getString(R.string.right_body));

        tabs.addTab(tab1);
        tabs.addTab(tab2);
        tabs.addTab(tab3);
        tabs.addTab(tab4);

        grp_btn = (ToggleButtonGroupTableLayout) findViewById(R.id.front_layout);
        grp_btn.startBodyPartChoose(bActivity);

        //更新Tab樣式
        updateTabStyle(tabs);

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                int i = tabs.getCurrentTab();
                switch (i) {
                    case 0:
                        grp_btn = (ToggleButtonGroupTableLayout) findViewById(R.id.front_layout);
                        break;
                    case 1:
                        grp_btn = (ToggleButtonGroupTableLayout) findViewById(R.id.back_layout);
                        break;
                    case 2:
                        grp_btn = (ToggleButtonGroupTableLayout) findViewById(R.id.left_layout);
                        break;
                    case 3:
                        grp_btn = (ToggleButtonGroupTableLayout) findViewById(R.id.right_layout);
                        break;
                }

                grp_btn.startBodyPartChoose(bActivity);
            }
        });

        //detectOrientation(this.getResources().getConfiguration().orientation);
    }

    private void updateTabStyle(TabHost tabs) {
        TabWidget tw = tabs.getTabWidget();
        for (int i = 0; i < tw.getChildCount(); i++) {
            TextView tv = (TextView) tw.getChildAt(i).findViewById(android.R.id.title);
            tv.setTextSize(18);
        }
    }

    public void checkedBodyPartBtn(final String bodyStr) {
        Intent intent = new Intent(BodyPartActivity.this, MainActivity.class);
        if (where.equals("main")) {
            intent.putExtra("bodyChoose", bodyStr);
            setResult(AppResultReceiver.CHOOSE_BODY_RESULT_OK, intent);
        } else {
            try {
                txtData = new JSONObject(params);
                txtData.put("bodyPart", bodyStr);
            } catch (Exception e) {
                Log.v(TAG, "選擇部位錯誤: " + e.getMessage());
            }

            intent.putExtra("url", "demo");
            intent.putExtra("params", txtData.toString());
            setResult(Activity.RESULT_OK, intent);
        }
        isShowLogoutHint = false;
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isShowLogoutHint = true;

    }

    //暫停接收串流
    @Override
    protected void onPause() {
        //view2.stopStream();
        super.onPause();
    }

    //停止接收串流
    @Override
    protected void onStop() {
        //view2.stopStream();
        super.onStop();
        if (isShowLogoutHint) {
            AppResultReceiver.logoutPreExecute(BodyPartActivity.this);
            isShowLogoutHint = false;
        }
    }

    @Override
    public void onBackPressed() {
        isShowLogoutHint = false;
        finish();
    }

    /*@Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        detectOrientation(newConfig.orientation);
    }

    private void detectOrientation(int orientation) {

        // 檢查目前手機畫面為橫向或直向
        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            mjview_title.setVisibility(View.INVISIBLE);
        } else if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT){
            mjview_title.setVisibility(View.VISIBLE);
        }

    }*/
}