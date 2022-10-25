package org.itri.woundcamrtc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import android.support.v7.app.AppCompatActivity;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.litesuits.http.LiteHttp;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.impl.huc.HttpUrlClient;
import com.litesuits.http.listener.HttpListener;
import com.litesuits.http.request.StringRequest;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;

import org.itri.woundcamrtc.helper.ShellHelper;
import org.itri.woundcamrtc.helper.StringUtils;
import org.itri.woundcamrtc.helper.XSslHttpURLConnection;
import org.itri.woundcamrtc.helper.XSslLiteHttp;
import org.itri.woundcamrtc.job.JobQueueFindInfoJob;
import org.itri.woundcamrtc.job.JobQueueFindPatientNoJob;
import org.itri.woundcamrtc.job.JobQueueFindRecordJob;
import org.itri.woundcamrtc.job.JobQueueFindVisitDateJob;
import org.itri.woundcamrtc.preview.FileUtility;
import org.itri.woundcamrtc.preview.TxtData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static org.itri.woundcamrtc.AppResultReceiver.ZONE;

public class CaseMgntActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    public float scale = 0f;

    private Context bContext;
    public static CaseMgntActivity activity;

    private boolean isShowLogoutHint = true; //顯示登出提醒
    private String ownerId = "";
    private String roleId = "";
    private Boolean firstClickBodypart = false;
    public List<String> keyNoList = new ArrayList<String>();
    ;
    public List<QryObj> qryObjList = new ArrayList<QryObj>();
    private Handler tabHandler = new Handler();

    private AutoCompleteTextView searchInput;
    public ProgressBar loadingBar;
    private ImageButton searchBtn;
    private ImageButton editInfo;
    private ImageButton showInfoBtn;
    private ImageButton showWoundBtn;
    private TextView idNo;
    private TextView patientInfo;
    private TextView medicalHistory;
    private TextView memo;
    private TextView noWoundData;
    private TextView noRecord;
    public ViewFlipper imageViewer;
    public TextView imgPageNum;
    public TextView heightData;
    public TextView widthData;
    public TextView depthData;
    public TextView areaData;
    public TextView epitheliumData;
    public TextView granularData;
    public TextView sloughData;
    public TextView escharData;
    public ImageView previous;
    public ImageView next;
    private GridLayout infoLayout;
    private GridLayout woundLayout;
    private HorizontalScrollView scrollList;
    private LinearLayout woundImgScroll;
    private LinearLayout recordLayout;
    public android.support.design.widget.TabLayout dateTabs;
    public ViewPager recordPager;

    private LiteHttp mLiteHttp = null;
    public JobManager jobManagerRelax = null;
    public Runnable mRecordRunner;
    public Handler mRecordHandler = new Handler();
    public ArrayList<View> viewContainer = new ArrayList<View>();
    public JSONArray patientNoList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_casemgnt);
        bContext = this;
        activity = this;

        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                Log.d(TAG, "queueIdle");
                onInit();
                return false; //run once
            }
        });
    }

    protected void onInit() {
        scale = getBaseContext().getResources().getDisplayMetrics().density;

        Intent intent = this.getIntent();
        ownerId = intent.getStringExtra("patientNo");
        roleId = intent.getStringExtra("roleId");

        try {
            patientNoList = new JSONArray(intent.getStringExtra("patientNoList"));
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.v(TAG, "ownerId is " + ownerId);
        Log.v(TAG, "roleId is " + roleId);

        initLiteHttp();
        generateViews();
        generateButtons();
        setupAdapter(patientNoList);
        ConnectivityManager mConnectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            if (StringUtils.isNotBlank(ownerId)) {
                loadingBar.setVisibility(View.VISIBLE);
                searchInput.setText(ownerId);
                searchInput.setSelection(ownerId.length());
                getPtInfo(ownerId, roleId);
            }
        }else{

            showToast(bContext.getString(R.string.open_internet_or_wifi));
        }
        //需先判斷主畫面是否有輸入病歷號，若有則直接帶入搜尋

    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        isShowLogoutHint = true;
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();

        if (isShowLogoutHint) {
            AppResultReceiver.logoutPreExecute(CaseMgntActivity.this);
            isShowLogoutHint = false;
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "press Back");
        isShowLogoutHint = false;
        keyNoList.clear();
        qryObjList.clear();
        //
        //update();
        //
        this.finish();
    }

    public void generateViews() {
        jobManagerRelax = configureJobQueueManagerRelax();

        searchInput = (AutoCompleteTextView) findViewById(R.id.patientIdInput);
        loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
        idNo = (TextView) findViewById(R.id.idNo);
        patientInfo = (TextView) findViewById(R.id.patientInfo);
        medicalHistory = (TextView) findViewById(R.id.medicalHistory);
        memo = (TextView) findViewById(R.id.memo);
        noWoundData = (TextView) findViewById(R.id.noWoundData);
        scrollList = (HorizontalScrollView) findViewById(R.id.scrollList);
        woundImgScroll = (LinearLayout) findViewById(R.id.woundImgScroll);
        noRecord = (TextView) findViewById(R.id.noRecord);
        recordLayout = (LinearLayout) findViewById(R.id.recordLayout);
        recordPager = (ViewPager) findViewById(R.id.viewpager);
        dateTabs = (android.support.design.widget.TabLayout) findViewById(R.id.date_tabs);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if(text.length() >= 4){
                    //setupAdapter();
                }
                else{

                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void generateButtons() {
        //搜尋病患資料按鈕
        searchBtn = (ImageButton) findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String ptNo = searchInput.getText().toString();
                if (StringUtils.isNotBlank(ptNo)) {

                    ConnectivityManager mConnectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                    if (mNetworkInfo != null) {
                        loadingBar.setVisibility(View.VISIBLE);
                        scrollList.setVisibility(View.GONE);
                        noRecord.setVisibility(View.VISIBLE);
                        recordLayout.setVisibility(View.GONE);
                        getPtInfo(ptNo, roleId);
                    }else{

                        showToast(bContext.getString(R.string.open_internet_or_wifi));
                    }
                }
            }
        });
        //編輯病患資訊
        editInfo = (ImageButton) findViewById(R.id.editInfo);
        editInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog("提醒", "編輯按鈕被點擊", 1);
            }
        });
        //選擇是否顯示基本資料
        showInfoBtn = (ImageButton) findViewById(R.id.showInfoBtn);
        infoLayout = (GridLayout) findViewById(R.id.infoLayout);
        toggleBtn(showInfoBtn, infoLayout);
        //選擇是否顯示傷口位置
        showWoundBtn = (ImageButton) findViewById(R.id.showWoundBtn);
        woundLayout = (GridLayout) findViewById(R.id.woundLayout);
        toggleBtn(showWoundBtn, woundLayout);

    }

    public void setupAdapter(JSONArray objList) {
        try {
            List<String> infos = new ArrayList<String>();
            for(int i = 0; i < objList.length(); i++){
                infos.add(objList.getString(i));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, infos);
            searchInput.setAdapter(adapter);
            searchInput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchInput.showDropDown();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void toggleBtn(ImageButton btn, GridLayout layout) {
        btn.setOnClickListener(new View.OnClickListener() {
            Boolean showContent = true;

            public void onClick(View v) {
                if (showContent) {
                    btn.setBackgroundResource(R.mipmap.arrow_up_48);
                    layout.setVisibility(View.GONE);
                    showContent = false;
                } else {
                    btn.setBackgroundResource(R.mipmap.arrow_down_48);
                    layout.setVisibility(View.VISIBLE);
                    showContent = true;
                }
            }
        });
    }

    //刪除所有TAB及PAGE
    public void removeDateTabs() {
        dateTabs.removeAllTabs();
        recordPager.setAdapter(null);
    }

    //設置看診日期資訊
    public void setDateTabInfo(String id, String evalDate) {
        QryObj obj = new QryObj(id, evalDate);
        qryObjList.add(obj);
    }

    //新增看診日期TAB與啟動相關功能
    public void addDateTabs() {
        try {
            for (int i = 0; i < qryObjList.size(); i++) {
                String evalDate = qryObjList.get(i).getEvalDate();
                dateTabs.addTab(dateTabs.newTab().setText(evalDate));
            }
            generateTabs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateTabs() {
        dateTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //Toast.makeText(CaseMgntActivity.this, tab.getPosition() + "/" + tab.getText(), Toast.LENGTH_SHORT).show();
                String keyNo = qryObjList.get(tab.getPosition()).getId();
                int position = tab.getPosition();
                //Toast.makeText(CaseMgntActivity.this, tab.getPosition() + "/" + keyNo, Toast.LENGTH_SHORT).show();
                jobManagerRelax.addJobInBackground(new JobQueueFindRecordJob(jobManagerRelax, activity, keyNo, position));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        recordPager.setAdapter(new recordPagerAdapter());
        recordPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(dateTabs));
        dateTabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(recordPager));
    }

    public void showAllTabs() {

        tabHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeDateTabs();
                addDateTabs();
            }
        }, 100);
    }

    public void getPtInfo(String ptNo, String roleNo) {
        try {

            jobManagerRelax.addJobInBackground(new JobQueueFindInfoJob(jobManagerRelax, activity, ptNo, roleNo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPtInfo(JSONObject infoObj, JSONArray woundList) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String charNoStr = infoObj.getString("charNo");
                    String idNoStr = infoObj.getString("idNo");
                    String nameStr = infoObj.getString("name");
                    String genderStr = infoObj.getString("gender");
                    String ageStr = infoObj.getString("age");
                    String medicalHistoryStr = infoObj.getString("medicalHistory");
                    String memoStr = infoObj.getString("memo");

                    idNo.setText(charNoStr + "/" + idNoStr);
                    patientInfo.setText(nameStr + "/" + genderStr + "/" + ageStr);
                    medicalHistory.setText(medicalHistoryStr);
                    memo.setText(memoStr);

                    if (woundList.length() > 0) {
                        keyNoList.clear();
                        woundImgScroll.removeAllViews();
                        noWoundData.setVisibility(View.GONE);
                        scrollList.setVisibility(View.VISIBLE);

                        for (int i = 0; i < woundList.length(); i++) {
                            JSONObject imgObj = woundList.getJSONObject(i);
                            String urlStr = "";
                            String keyNoStr = imgObj.getString("keyNo");
                            String bodyPartStr = imgObj.getString("bodypart");

                            if (!imgObj.getString("imgUrl").equals("")) {
                                urlStr = AppResultReceiver.DEFAULT_LOADIMG_PATH + imgObj.getString("imgUrl") + "&q=128&location=" + ZONE;
                                Log.v(TAG, "檔案網址為：" + urlStr);
                            }
                          //  if(bodyPartStr!=null||bodyPartStr.equals(""))
                            createWoundObj(keyNoStr, bodyPartStr, urlStr, i);
                        }
                    } else {
                        scrollList.setVisibility(View.GONE);
                        noWoundData.setVisibility(View.VISIBLE);
                    }

                    loadingBar.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setPtInfobyNowound(JSONObject infoObj) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String charNoStr = infoObj.getString("charNo");
                    String idNoStr = infoObj.getString("idNo");
                    String nameStr = infoObj.getString("name");
                    String genderStr = infoObj.getString("gender");
                    String ageStr = infoObj.getString("age");
                    String medicalHistoryStr = infoObj.getString("medicalHistory");
                    String memoStr = infoObj.getString("memo");

                    idNo.setText(charNoStr + "/" + idNoStr);
                    patientInfo.setText(nameStr + "/" + genderStr + "/" + ageStr);
                    medicalHistory.setText(medicalHistoryStr);
                    memo.setText(memoStr);
                        scrollList.setVisibility(View.GONE);
                        noWoundData.setVisibility(View.VISIBLE);


                    loadingBar.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void setRecordInfo(JSONObject obj, int position) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.v(TAG, "個案管理接收數據:" + obj.getString("height") + "," + obj.getString("width") + "," + obj.getString("depth") +
                            "," + obj.getString("area") + "," + obj.getString("epithelium") + "," + obj.getString("granular") + "," + obj.getString("slough") + "," + obj.getString("eschar"));

                    TextView height = recordPager.findViewWithTag("height" + position);

                    TextView width = recordPager.findViewWithTag("width" + position);
                    TextView depth = recordPager.findViewWithTag("depth" + position);
                    TextView area = recordPager.findViewWithTag("area" + position);
                    TextView epithelium = recordPager.findViewWithTag("epithelium" + position);
                    TextView granular = recordPager.findViewWithTag("granular" + position);
                    TextView slough = recordPager.findViewWithTag("slough" + position);
                    TextView eschar = recordPager.findViewWithTag("eschar" + position);

                    height.setText(obj.getString("height") + getString(R.string.cm));
                    width.setText(obj.getString("width") + getString(R.string.cm));
                    depth.setText(obj.getString("depth") + getString(R.string.cm));
                    area.setText(obj.getString("area") + getString(R.string.cm2));
                    epithelium.setText(obj.getString("epithelium") + getString(R.string.prop));
                    granular.setText(obj.getString("granular") + getString(R.string.prop));
                    slough.setText(obj.getString("slough") + getString(R.string.prop));
                    eschar.setText(obj.getString("eschar") + getString(R.string.prop));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setWoundImage(JSONObject obj, int position, int imgNum, int totalImgNum) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String itemId = obj.getString("itemId");
                    String url = AppResultReceiver.DEFAULT_LOADIMG_PATH + obj.getString("url") + "&location=" + ZONE;

                    ViewFlipper viewer = recordPager.findViewWithTag("imageViewer" + position);
                    TextView imgPageNum = recordPager.findViewWithTag("imgPager" + position);
                    ImageView previous = recordPager.findViewWithTag("prev" + position);
                    ImageView next = recordPager.findViewWithTag("next" + position);

                    if (viewer.getChildCount() < totalImgNum) {
                        ImageView newImgView = new ImageView(CaseMgntActivity.this);
                        LinearLayout.LayoutParams ivParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        newImgView.setLayoutParams(ivParam);
                        newImgView.setScaleType(ImageView.ScaleType.FIT_XY);
                        newImgView.setBackgroundColor(getColor(R.color.img_background));
                        if (!url.equals("")) {
                            loadImage(newImgView, url, itemId);
                        }
                        viewer.addView(newImgView);
                    }

                    if (imgNum == (totalImgNum - 1)) {
                        imgPageNum.setText("1/" + totalImgNum);
                        imgPageNum.setShadowLayer(1, 0, 0, Color.BLACK);
                        previous.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                try {
                                    showPrev(v, viewer, imgPageNum);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        next.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                try {
                                    showNext(v, viewer, imgPageNum);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void showDialog(final String title, final String message, final int type) {
        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(CaseMgntActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setIcon(R.mipmap.color_light_48);
                //按到旁邊的空白處AlertDialog也不會消失
                builder.setCancelable(false);

                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (type == 1) {
                            dialog.cancel();
                        }
                    }
                });

                builder.show();
            }
        });
    }

    public void createWoundObj(String keyNo, String bodyPart, String url, int serialNo) {
        int padding = (int) (5 * scale + 0.5f);
        int imgPadding = (int) (10 * scale + 0.5f);
        int width = (int) (100 * scale + 0.5f);
        int height = (int) (80 * scale + 0.5f);
        keyNoList.add(keyNo);
        //建立linearLayout
        LinearLayout newLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        newLayout.setLayoutParams(params);
        newLayout.setOrientation(LinearLayout.VERTICAL);
        newLayout.setPadding(padding, 0, padding, 0);
        newLayout.setClickable(true);
        newLayout.setBackgroundResource(R.drawable.bodypart_selector);
        //newLayout.setBackground(getDrawable(R.drawable.bodypart_selector));
        newLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idx = getId(v);
                resetLinearLayout(woundImgScroll);
                v.setSelected(true);
                String keyNoValue = keyNoList.get(idx);
                jobManagerRelax.addJobInBackground(new JobQueueFindVisitDateJob(jobManagerRelax, activity, keyNoValue));
                noRecord.setVisibility(View.GONE);
                recordLayout.setVisibility(View.VISIBLE);
            }
        });
        int layoutId = 100 + serialNo;
        newLayout.setId(serialNo);
        //建立ImageView
        ImageView newImgView = new ImageView(this);
        LinearLayout.LayoutParams ivParam = new LinearLayout.LayoutParams(width, height);
        //ivParam.setMargins(margin,margin,margin,0);
        newImgView.setLayoutParams(ivParam);
        newImgView.setBackgroundColor(getColor(R.color.img_background));
        if (StringUtils.isNotBlank(url)) {
            loadImage(newImgView, url, bodyPart);
            //newImgView.setImageDrawable(loadImage(url, bodyPart));
        } else {
            newImgView.setPadding(imgPadding, imgPadding, imgPadding, imgPadding);
            newImgView.setImageResource(R.mipmap.no_image_64);
        }
        //建立TextView
        TextView newTextView = new TextView(this);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //tvParams.setMargins(margin,0,margin,0);
        newTextView.setLayoutParams(tvParams);
        newTextView.setBackgroundColor(getColor(R.color.bodypart));
        newTextView.setTextSize(16f);
        newTextView.setTextColor(getColor(R.color.bodypart_text));
        newTextView.setText(bodyPart);
        newTextView.setGravity(Gravity.CENTER);

        newLayout.addView(newImgView);
        newLayout.addView(newTextView);
        woundImgScroll.addView(newLayout);
    }

    public void loadImage(final ImageView imageView, String url, String srcName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.v(TAG, "個案管理抓到圖檔位址:" + url);
                    InputStream is = (InputStream) (new XSslHttpURLConnection()).getImageStream(url, 5, 10);
                    //InputStream is = (InputStream) new URL(url).getContent();
                    Drawable d = Drawable.createFromStream(is, srcName);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            imageView.setImageDrawable(d);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void showNoFoundInfo(){
        loadingBar.setVisibility(View.INVISIBLE);
        showDialog(getString(R.string.alert_error),getString(R.string.qryInfo_failed),1);
    }

    public int getId(View view) {
        int targetId = 0;
        if (view.getId() != View.NO_ID) {
            targetId = view.getId();
        }
        return targetId;
    }

    // 上一個
    public void showPrev(View source, ViewFlipper viewer, TextView imgPageNum) {
        // 顯示上一個元件
        viewer.showPrevious();
        // 停止自動播放
        viewer.stopFlipping();
        imgPageNum.setText((viewer.getDisplayedChild() + 1) + "/" + viewer.getChildCount());
        imgPageNum.setShadowLayer(1, 0, 0, Color.BLACK);
    }

    // 下一個
    public void showNext(View source, ViewFlipper viewer, TextView imgPageNum) {
        // 顯示下一個元件
        viewer.showNext();
        // 停止自動播放
        viewer.stopFlipping();
        imgPageNum.setText((viewer.getDisplayedChild() + 1) + "/" + viewer.getChildCount());
        imgPageNum.setShadowLayer(1, 0, 0, Color.BLACK);
    }

    @NonNull
    protected synchronized void initLiteHttp() {
        if (mLiteHttp == null) {
//            mLiteHttp = LiteHttp.build(bContext)
//                    .setHttpClient(new HttpUrlClient())       // http client
//                    .setContext(bContext)
//                    .setDebugged(true)                     // log output when debugged
//                    .setDoStatistics(false)                // statistics of time and traffic
//                    .setDetectNetwork(false)              // detect network before connect
//                    //.setDefaultMaxRetryTimes(0)
//                    .setSocketTimeout(360000)           // socket timeout: 60s
//                    .setConnectTimeout(7000)         // connect timeout: 10s
//                    //.setConcurrentSize(1)
//                    //.setDefaultMaxRetryTimes(0)
//                    //.setDefaultMaxRedirectTimes(1)
//                    //.setWaitingQueueSize(20)
//                    //.setMaxMemCacheBytesSize(5242880L)
//                    .create();
            mLiteHttp = (new XSslLiteHttp(bContext, 7, 360)).getLiteHttp();
        } else {
            clearLiteHttp();
            initLiteHttp();
        }
    }

    public synchronized void clearLiteHttp() {
        if (mLiteHttp != null) {
            this.mLiteHttp.clearMemCache();
            this.mLiteHttp = null;
        }
    }

    public JobManager configureJobQueueManagerRelax() {
        //3. JobManager的配置器，利用Builder模式
        com.birbit.android.jobqueue.config.Configuration configuration = new com.birbit.android.jobqueue.config.Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBR";

                    @Override
                    public boolean isDebugEnabled() {
                        return false;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        //Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }

                    @Override
                    public void v(String text, Object... args) {
                        //Log.v(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(0)//always keep at least one consumer alive
                .maxConsumerCount(2)//up to 2 consumers at a time
                .loadFactor(1)//1 jobs per consumer
                .consumerKeepAlive(10)//wait 0.1 minute
                .build();
        jobManagerRelax = new JobManager(configuration);
        return jobManagerRelax;
    }

    public void resetLinearLayout(LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            layout.getChildAt(i).setSelected(false);
        }
    }

    public void cleanAllLinearLayout(LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            //layout.re
        }
    }

    public class recordPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return dateTabs.getTabCount();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Item " + (position + 1);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.pager_item, container, false);
            container.addView(view);

            TextView height = (TextView) view.findViewById(R.id.height_data);
            TextView width = (TextView) view.findViewById(R.id.width_data);
            TextView depth = (TextView) view.findViewById(R.id.depth_data);
            TextView area = (TextView) view.findViewById(R.id.area_data);
            TextView epithelium = (TextView) view.findViewById(R.id.epithelium_data);
            TextView granular = (TextView) view.findViewById(R.id.granular_data);
            TextView slough = (TextView) view.findViewById(R.id.slough_data);
            TextView eschar = (TextView) view.findViewById(R.id.eschar_data);
            ViewFlipper imageViewer = (ViewFlipper) view.findViewById(R.id.imageViewer);
            TextView imgPager = (TextView) view.findViewById(R.id.imgPageNum);
            ImageView prev = (ImageView) view.findViewById(R.id.previous);
            ImageView next = (ImageView) view.findViewById(R.id.next);

            height.setTag("height" + position);
            width.setTag("width" + position);
            depth.setTag("depth" + position);
            area.setTag("area" + position);
            epithelium.setTag("epithelium" + position);
            granular.setTag("granular" + position);
            slough.setTag("slough" + position);
            eschar.setTag("eschar" + position);
            imageViewer.setTag("imageViewer" + position);
            imgPager.setTag("imgPager" + position);
            prev.setTag("prev" + position);
            next.setTag("next" + position);

            if (!firstClickBodypart) {
                String keyNo = qryObjList.get(position).getId();
                jobManagerRelax.addJobInBackground(new JobQueueFindRecordJob(jobManagerRelax, activity, keyNo, position));
                firstClickBodypart = true;
            }

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public static class QryObj {
        String id;
        String evalDate;

        public QryObj(String id, String evalDate) {
            this.id = id;
            this.evalDate = evalDate;
        }

        public String getId() {
            return id;
        }

        public String getEvalDate() {
            return evalDate;
        }
    }


    /*
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

    */
    public void showToast(String context) {


        try {
            Toast toast = Toast.makeText(this.activity, context, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 250);
            toast.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
