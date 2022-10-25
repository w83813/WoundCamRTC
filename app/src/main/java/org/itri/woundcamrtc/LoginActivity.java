package org.itri.woundcamrtc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.litesuits.http.LiteHttp;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.listener.HttpListener;
import com.litesuits.http.request.StringRequest;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;

//import net.sqlcipher.database;

import net.sqlcipher.database.SQLiteDatabase;

import org.itri.woundcamrtc.data.EvalGroup;
import org.itri.woundcamrtc.data.EvalItem;
import org.itri.woundcamrtc.helper.DBTableHelper;
import org.itri.woundcamrtc.helper.FileHelper;
import org.itri.woundcamrtc.helper.SecretDbHelper;
import org.itri.woundcamrtc.helper.PermissionChecker;
import org.itri.woundcamrtc.helper.ServiceHelper;
import org.itri.woundcamrtc.helper.XSslLiteHttp;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.itri.woundcamrtc.AppResultReceiver.IS_FOR_MIIS_MPDA;
import static org.itri.woundcamrtc.AppResultReceiver.SAVE_DIR;
import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;
import static org.itri.woundcamrtc.AppResultReceiver.mMainActivity;


//import static com.hitomi.tilibrary.transfer.TransferConfig.build;

public class LoginActivity extends Activity {
    private final String TAG = getClass().getSimpleName();
    public SimpleDateFormat nowEvalTime = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
    private LoginActivity activity;
    private Context bContext;

    public int loginPeriod = 0;
    private int periodthis = 0;

    public String params = "";
    private String loginDate = "";
    private String historyUser = "";
    private String historyAccount = "";
    private String historyPwd = "";
    private String userId = "";
    private String roleId = "";
    private String username = "";
    public static String PREFS_NAME = "woundcare";
    public static String PREF_USERNAME = "username";
    public static String PREF_PASSWORD = "password";

    private boolean showPass = false;
    private boolean checkLogin = false;
    private boolean focusUser = false;
    private boolean isShowLogoutHint = true; //顯示登出提醒

    private JSONObject txtData;
    private TextView version;
    private AutoCompleteTextView user;
    private EditText pass;
    private Button Button_login;
    //private Button Button_updatePwd;

    ServiceHelper serviceHelper = new ServiceHelper();
    private static final String[] RequiredPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,  Manifest.permission.ACCESS_COARSE_LOCATION}; //權限允許(照相功能與錄音功能)
    public File mainDir;
    public DBTableHelper database;
    public SQLiteDatabase Sercretdb;
    public SecretDbHelper sqllitesecret;
    private Intent intent;
    protected PermissionChecker permissionChecker = new PermissionChecker();
    private LiteHttp mLiteHttp = null;
    private  MainActivity mMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate executes ...");
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON  //防止螢幕自動關閉
//                        | LayoutParams.FLAG_DISMISS_KEYGUARD    //解鎖螢幕
                        //               | LayoutParams.FLAG_SHOW_WHEN_LOCKED    //螢幕鎖定時也可以顯示
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra("depth_status", "depth_on");
        intent.putExtra("thermal_status", "thermal_on");
        ComponentName componentName = new ComponentName("com.example.mpda_gpio_ctrl", "com.example.mpda_gpio_ctrl.MainActivity");
        intent.setComponent(componentName);
        startActivity(intent);

        activity = this;
        bContext = this;
        setContentView(R.layout.activity_login);
        pass = (EditText) findViewById(R.id.password);
        user = (AutoCompleteTextView) findViewById(R.id.username);
        if(IS_FOR_MIIS_MPDA){
            GPIOON();
        }

        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                Log.d(TAG, "queueIdle");
                onInit();
                return false; //run once
            }
        });

        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSSID() != "<unknown ssid>"){
                save_systemtime();
            }
            File file_time = new File("/storage/emulated/0/Download/WoundCamRtc/time.txt");
            if (file_time.exists()){
                Long tsLong = System.currentTimeMillis()/1000;
                if (Integer.parseInt(tsLong.toString()) < LoginActivity.read_systemtime()){

                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(R.string.time_error);
                    builder.setMessage(R.string.please_connect_to_wifi_to_adjust_the_time);
                    builder.setIcon(R.mipmap.color_light_48);
                    builder.setCancelable(false);

                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    builder.show();
                }
            }
        } catch (Exception e) {

        }

    }

    public void GPIOON() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.putExtra("depth_status", "depth_on");
            intent.putExtra("thermal_status", "thermal_on");
            ComponentName componentName = new ComponentName("com.example.mpda_gpio_ctrl", "com.example.mpda_gpio_ctrl.MainActivity");
            intent.setComponent(componentName);
            startActivity(intent);
        } catch (Exception e) {

        }
    }

    protected void onInit() {
        Log.d(TAG, "onInit");


        try {
            checkPermissions();
            if(dataEncrypt==false){
                mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                database = DBTableHelper.getInstance(getApplicationContext(), mainDir.getAbsolutePath() + File.separator + AppResultReceiver.PROJECT_NAME + ".db");

                if (DBTableHelper.OLD_VERSION == 1) {
                    try {
                        database.upgradeDatabase(database.getWritableDatabase());
                    } catch (Exception ex) {
                    }
                    gotoNexActivity();
                } else if (DBTableHelper.OLD_VERSION < DBTableHelper.NEW_VERSION) {
                    Log.v(TAG,"DBTableHelper.OLD_VERSION"+DBTableHelper.OLD_VERSION);
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("更新APP資料庫?");
                    builder.setMessage("將會清除未上傳的資料，確定嗎?");
                    builder.setIcon(R.mipmap.color_light_48);
                    builder.setCancelable(true);

                    builder.setPositiveButton(getString(R.string.clean), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                database.upgradeDatabase(database.getWritableDatabase());
                            } catch (Exception ex) {
                            }
                            gotoNexActivity();
                        }
                    });

                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            gotoNexActivity();
                        }
                    });
                    builder.show();
                }
            }else{
                SQLiteDatabase.loadLibs(this);
                sqllitesecret=new SecretDbHelper(activity);
                Sercretdb = SecretDbHelper.getInstance(this).getWritableDatabase("MIIS");
                if (sqllitesecret.OLD_VERSION == 1) {
                    try {
                        sqllitesecret.upgradeDatabase(Sercretdb);
                    } catch (Exception ex) {
                    }
                    gotoNexActivity();
                } else if (sqllitesecret.OLD_VERSION < sqllitesecret.NEW_VERSION) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("更新APP資料庫?");
                    builder.setMessage("將會清除未上傳的資料，確定嗎?");
                    builder.setIcon(R.mipmap.color_light_48);
                    builder.setCancelable(true);

                    builder.setPositiveButton(getString(R.string.clean), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                sqllitesecret.upgradeDatabase(Sercretdb);
                            } catch (Exception ex) {
                            }
                            gotoNexActivity();
                        }
                    });

                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            gotoNexActivity();
                        }
                    });
                    builder.show();
                }
            }
            /*else{
                if (sqllitesecret.OLD_VERSION == 1) {
                    try {
                        sqllitesecret.upgradeDatabase(Sercretdb);
                    } catch (Exception ex) {
                    }
 gotoNexActivity();
                } else if (sqllitesecret.OLD_VERSION < sqllitesecret.NEW_VERSION) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("更新APP資料庫?");
                    builder.setMessage("將會清除未上傳的資料，確定嗎?");
                    builder.setIcon(R.mipmap.color_light_48);
                    builder.setCancelable(true);

                    builder.setPositiveButton(getString(R.string.clean), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                sqllitesecret.upgradeDatabase(Sercretdb);
                            } catch (Exception ex) {
                            }
                            gotoNexActivity();
                        }
                    });

                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            gotoNexActivity();
                        }
                    });
                    builder.show();
                }
            }

*/


            //   database.deleteRaw("table_picNumber", null, null);

            initLiteHttp();

            RadioGroup rg = (RadioGroup) findViewById(R.id.radioBtnGrp);

            Intent intent = this.getIntent();

//            user.setOnKeyListener(new View.OnKeyListener() {
//                public boolean onKey(View v, int keyCode, KeyEvent event) {
//                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
//                        (new Handler()).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                focusUser = true;
//                            }
//                        }, 150);
//                        pass.requestFocus();
//                        return true;
//                    }
//                    return false;
//                }
//            });



            Button_login = (Button) findViewById(R.id.login);
            //Button_updatePwd = (Button) findViewById(R.id.updatepwd);
            version = (TextView) findViewById(R.id.version);

            setupAdapter();
            setVersion();

            Button_login.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    AppResultReceiver.vibrating(activity);

                    try {
                        String account = user.getText().toString();
                        String password = pass.getText().toString();

                        //依選取項目顯示不同訊息
                        switch (rg.getCheckedRadioButtonId()) {
                            case R.id.eighthour:
                                periodthis = 8;
                                break;
                            case R.id.oneday:
                                periodthis = 24;
                                break;
                            case R.id.threeday:
                                periodthis = 72;
                                break;
                        }

                        if (account.isEmpty() || password.isEmpty()) {
                            showDialog(getString(R.string.remind_title), getString(R.string.logInfo_not_filled), 2);
                        } else if (account.toLowerCase().equals("demo") && password.toLowerCase().equals("demo")) {
                            pass.setText("");
                            try {
                                List<Map<String, Object>> listMeasure = null;

                                ContentValues initialValues = new ContentValues();
                                initialValues.put("userid", 2);
                                initialValues.put("username", "Tester");
                                initialValues.put("account", account);
                                initialValues.put("password", password);
                                initialValues.put("roleid", 2);
                                initialValues.put("period", 24);
                                initialValues.put("evalDate", nowEvalTime.format(new Date()));
                                if(dataEncrypt==false){
                                    database.addOrUpdateRaw("loginInfo", initialValues, "", new String[]{});
                                }else{
                                    sqllitesecret.addOrUpdateRaw(Sercretdb,"loginInfo", initialValues, "", new String[]{});
                                }


                            } catch (Exception ex) {

                            }
                            isShowLogoutHint = false;

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userName", "Tester");
                            intent.putExtra("userId", "2");
                            intent.putExtra("roleId", "2");
                            startActivity(intent);
/*
                            File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
                            String filename_sercret ="/2020-12-12 14-01-14-773_2020-12-12_13_datax.txt";
                            File target_sercret = new File(mPicDir, filename_sercret);

                            try {
                                FileHelper.txt_decrypt(target_sercret.getAbsolutePath(),18);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
*/

                        } else {

                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                            StrictMode.setThreadPolicy(policy);
                            ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

                            if (mNetworkInfo == null) {
                                List<Map<String, Object>> listMeasure = null;
                                if(dataEncrypt==false){
                                    listMeasure = database.querySQLData("loginInfoHistory", null, "account=?", new String[]{user.getText().toString()}, "");
                                }else{
                                    listMeasure = sqllitesecret.querySQLData(Sercretdb,"loginInfoHistory", null, "account=?", new String[]{user.getText().toString()}, "");
                                }

                                String no_internet_pwd="";
                                if (listMeasure.size() > 0) {
                                    for (Map<String, Object> map : listMeasure) {
                                        no_internet_pwd=(String) map.get("password");
                                        historyUser = (String) map.get("username");
                                        userId = String.valueOf((Integer) map.get("userid"));
                                        roleId = String.valueOf((Integer) map.get("roleid"));
                                    }
                                }

                                if(no_internet_pwd.equals(pass.getText().toString())){
                                    ContentValues initialValues = new ContentValues();
                                    initialValues.put("period", periodthis);
                                    initialValues.put("evalDate", nowEvalTime.format(new Date()));
                                    if(dataEncrypt==false){
                                        database.addOrUpdateRaw("loginInfo", initialValues, "account=?", new String[]{historyUser});
                                    }else{
                                        sqllitesecret.addOrUpdateRaw(Sercretdb,"loginInfo", initialValues, "account=?", new String[]{historyUser});
                                    }
                                    showDialog(getString(R.string.remind_title), getString(R.string.confirm_network), 3);
                                }else{
                                    showDialog(getString(R.string.remind_title), getString(R.string.login_failed), 2);
                                }

                            } else {
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String uploadUrl = AppResultReceiver.DEFAULT_LOGIN_PATH;
                                            Map<String, String> params = new HashMap<String, String>();

                                            params.put("uid", account);
                                            params.put("pwd", password);





                                            final StringRequest upload = new StringRequest(uploadUrl)
                                                    .setMethod(HttpMethods.Post)
                                                    .setParamMap(params)
                                                    .setHttpListener(
                                                            new HttpListener<String>(true, false, false) {
                                                                @Override
                                                                public void onSuccess(String s, com.litesuits.http.response.Response<String> response) {
                                                                    super.onSuccess(s, response);

                                                                    response.printInfo();
                                                                    if (AppResultReceiver.cookies==null)
                                                                        AppResultReceiver.cookies = new ArrayList<String>();
                                                                    if (AppResultReceiver.cookies!=null) {
                                                                        AppResultReceiver.cookies.clear();
                                                                        ArrayList<NameValuePair> headerDatas = response.getHeaders();
                                                                        for (NameValuePair nameValuePair : headerDatas) {
                                                                            if (nameValuePair.getName().equals("set-cookies")) {
                                                                                AppResultReceiver.cookies.add(nameValuePair.getValue());
                                                                            }
                                                                        }
                                                                    }
                                                                    JSONObject mJsonObject = null;
                                                                    try {
                                                                        mJsonObject = new JSONObject(response.getResult());
                                                                        String result = mJsonObject.getString("success");
                                                                        Log.v(TAG, response.getResult());

                                                                        if (result.toLowerCase().equals("true")) {
                                                                            try {
                                                                                AppResultReceiver.account=account;
                                                                                AppResultReceiver.password=password;
                                                                                Log.v(TAG, "登入成功");
                                                                                //SQL LITE
                                                                                userId = mJsonObject.getString("userid");
                                                                                roleId = mJsonObject.getString("userrole");
                                                                                username = mJsonObject.getString("username");
                                                                                Log.v(TAG, "接收userId: " + userId);
                                                                                Log.v(TAG, "接收username: " + username);
                                                                                Log.v(TAG, "接收roleid: " + roleId);

                                                                                SharedPreferences setting = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                                                                setting.edit().putString(PREF_USERNAME, account).commit();
                                                                                pass.setText("");

                                                                                ContentValues initialValues = new ContentValues();
                                                                                initialValues.put("userid", userId);
                                                                                initialValues.put("username", username);
                                                                                initialValues.put("account", account);
                                                                                initialValues.put("password", password);
                                                                                initialValues.put("roleid", roleId);
                                                                                initialValues.put("period", periodthis);
                                                                                initialValues.put("evalDate", nowEvalTime.format(new Date()));
                                                                                if(dataEncrypt==false){
                                                                                    database.addOrUpdateRaw("loginInfo", initialValues, "", new String[]{});
                                                                                    database.add("loginInfoHistory", initialValues, "", new String[]{});

                                                                                }else{
                                                                                    sqllitesecret.add(Sercretdb,"loginInfoHistory", initialValues, "", new String[]{});
                                                                                    sqllitesecret.addOrUpdateRaw(Sercretdb,"loginInfo", initialValues, "", new String[]{});
                                                                                }
                                                                                isShowLogoutHint = false;
                                                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                                                intent.putExtra("userName", username);
                                                                                intent.putExtra("userId", userId);
                                                                                intent.putExtra("roleId", roleId);

                                                                                startActivity(intent);

                                                                            } catch (Exception e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        } else {
                                                                            showDialog(getString(R.string.remind_title), getString(R.string.login_failed), 2);
                                                                        }

                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(HttpException exception, Response<String> response) {
                                                                    super.onFailure(exception, response);

                                                                    try {
                                                                        showDialog(getString(R.string.remind_title), getString(R.string.api_failed), 2);
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                    );

                                            mLiteHttp.execute(upload);
                                        }
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            pass.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_LEFT = 0;
                    final int DRAWABLE_TOP = 1;
                    final int DRAWABLE_RIGHT = 2;
                    final int DRAWABLE_BOTTOM = 3;

                    Drawable passIcon = bContext.getDrawable(R.drawable.ic_vpn_key);
                    Drawable visible = bContext.getDrawable(R.drawable.ic_visibility);
                    Drawable invisible = bContext.getDrawable(R.drawable.ic_visibility_off);

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (event.getRawX() >= (pass.getRight() - pass.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            if (showPass) {
                                pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                pass.setCompoundDrawablesWithIntrinsicBounds(passIcon, null, invisible, null);
                                showPass = false;
                            } else {
                                pass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                                pass.setCompoundDrawablesWithIntrinsicBounds(passIcon, null, visible, null);
                                showPass = true;
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            showDialog(getString(R.string.remind_title), getString(R.string.local_data_error), 1);
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart executes ...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart executes ...");
    }

    @Override
    protected void onResume() {
        super.onResume();

        pass.setText("demo");
        pass.setText("123456");

        Log.e(TAG, "onResume executes ...");
        isShowLogoutHint = true;
    }

    //暫停接收串流
    @Override
    protected void onPause() {
        //view2.stopStream();
        super.onPause();
        Log.e(TAG, "onPause executes ...");
    }

    //停止接收串流
    @Override
    protected void onStop() {
        //view2.stopStream();
        super.onStop();
        Log.e(TAG, "onStop executes ...");
        if (isShowLogoutHint) {
            AppResultReceiver.logoutPreExecute(LoginActivity.this);
            isShowLogoutHint = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra("depth_status", "depth_off");
        intent.putExtra("thermal_status", "thermal_off");
        ComponentName componentName = new ComponentName("com.example.mpda_gpio_ctrl", "com.example.mpda_gpio_ctrl.MainActivity");
        intent.setComponent(componentName);
        startActivity(intent);


        Log.e(TAG, "onDestroy executes ...");
    }

    @Override
    public void onBackPressed() {   //離開主畫面之提醒通知
        Log.e(TAG, "onBackPressed executes ...");
        isShowLogoutHint = true;
        AppResultReceiver.vibrating(activity);
        showDialog(getString(R.string.remind_title), getString(R.string.confirm_leave_app), 1);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            isShowLogoutHint = true;
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
//            isShowLogoutHint = true;
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
//            isShowLogoutHint = true;
//            return true;
//        } else
//            return super.onKeyDown(keyCode, event);
//    }

    private void setupAdapter() {
        try {
            List<String> infos = new ArrayList<String>();
            List<Map<String, Object>> listMeasure = null;

            if(dataEncrypt==false){
                listMeasure = database.querySQLDataLoginList();
            }else{
                listMeasure = sqllitesecret.querySQLDataLoginList(Sercretdb);
            }
            if (listMeasure.size() > 0) {
                for (Map<String, Object> map : listMeasure) {
                    String account = (String) map.get("account");
                    infos.add(account);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, infos);
            user.setAdapter(adapter);
            user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user.showDropDown();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void gotoNexActivity() {

        try {  Log.v(TAG,"gotoNexActivity");
            List<Map<String, Object>> listMeasure = null;

            try {
            if(dataEncrypt==false){
                mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                database = DBTableHelper.getInstance(getApplicationContext(), mainDir.getAbsolutePath() + File.separator + AppResultReceiver.PROJECT_NAME + ".db");

                listMeasure = database.querySQLData("loginInfo", null, "", new String[]{}, "");

            }else{
                SQLiteDatabase.loadLibs(this);
                sqllitesecret=new SecretDbHelper(activity);
                Sercretdb = SecretDbHelper.getInstance(this).getWritableDatabase("MIIS");
                listMeasure = sqllitesecret.querySQLData(Sercretdb,"loginInfo", null, "", new String[]{}, "");
            }
            } catch (Exception e){
                e.printStackTrace();
            }
            if (listMeasure!=null && listMeasure.size() > 0) {
                for (Map<String, Object> map : listMeasure) {
                    loginPeriod = (Integer) map.get("period");
                    loginDate = (String) map.get("evalDate");
                    historyUser = (String) map.get("username");
                    userId = String.valueOf((Integer) map.get("userid"));
                    roleId = String.valueOf((Integer) map.get("roleid"));
                    historyAccount =  (String) map.get("account");
                    historyPwd = (String) map.get("password");
                }
                checkLogin = serviceHelper.calcuteLoginTime(loginDate, loginPeriod);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (checkLogin) {

            Log.v(TAG,"historyUser"+historyUser);
            Log.v(TAG,"historyAccount"+historyAccount);
            Log.v(TAG,"historyPwd"+historyPwd);
            if(IS_FOR_MIIS_MPDA==false){
                isShowLogoutHint = false;
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("userName", historyUser);
                intent.putExtra("userId", userId);
                intent.putExtra("roleId", roleId);
                startActivity(intent);
            }else{
                user.setText(historyAccount);
                //pass.setText(historyPwd);
            }

        }else{
            user.setText(historyAccount);
            isShowLogoutHint = false;
        }
    }

    public void showDialog(final String title, final String message, final int type) {
        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setIcon(R.mipmap.color_light_48);
                //按到旁邊的空白處AlertDialog也不會消失
                builder.setCancelable(false);

                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (type == 1) {
                            finish();
                            System.exit(0);
                        } else if (type == 3) {
                            dialog.cancel();
                        }
                    }
                });

                if (type == 1) {
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                }
                if (type == 3) {
                    builder.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.cancel();

                        }
                    });
                    builder.setNegativeButton(getString(R.string.no_internet), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userName", historyUser);
                            intent.putExtra("userId", userId);
                            intent.putExtra("roleId", roleId);
                            startActivity(intent);

                        }
                    });
                }
                builder.show();
            }
        });
    }
//    hsieh0920 123456

    private void checkPermissions() {
        //Checks all given permissions have been granted.


        permissionChecker.verifyPermissions(this, RequiredPermissions, new PermissionChecker.VerifyPermissionsCallback() {

            @Override
            public void onPermissionAllGranted() {
                if (DBTableHelper.OLD_VERSION >= DBTableHelper.NEW_VERSION) {
                    gotoNexActivity();
                }
            }

            @Override
            public void onPermissionDeny(String[] permissions) {
                showToast(getString(R.string.confirm_permission));
                //Log.i("步驟permissions", permissions[0]);
            }
        });
    }

    //修改Toast內容
    public void showToast(String context) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(LoginActivity.this, context, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 250);
                toast.show();
            }
        });
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
////                    .setDefaultMaxRetryTimes(0)
////                    .setDefaultMaxRedirectTimes(1)
//                    //.setWaitingQueueSize(20)
//                    //.setMaxMemCacheBytesSize(5242880L)
//                    .create();
            mLiteHttp = (new XSslLiteHttp(bContext, 5, 5)).getLiteHttp();

        } else {
            clearLiteHttp();
            initLiteHttp();
        }
    }

    public void updatePsd(View v) {
        isShowLogoutHint = false;
        Intent intent = new Intent(LoginActivity.this, UpdatePwdActivity.class);
        startActivity(intent);
        finish();
    }

    public synchronized void clearLiteHttp() {
        if (mLiteHttp != null) {
            this.mLiteHttp.clearMemCache();
            this.mLiteHttp = null;
        }
    }

    void setVersion() {
        version.setText(AppResultReceiver.APP + " " + getString(R.string.version_no) + " " + AppResultReceiver.APPVER);
        version.setVisibility(View.GONE);
    }
    public static String encryptAndDencrypt(String value, char secret){
        byte[] bt = value.getBytes();    //將需要加密的內容轉換成字節數組
        for(int i=0; i<bt.length; i++){
            bt[i] = (byte)(bt[i]^(int)secret);    //利用異或運算進行加密動作
        }
        String newResult = new String(bt,0,bt.length);    //將加密後的字串存取到newresult
        return newResult;
    }

    public static void save_systemtime(){
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();

        File file = new File("/storage/emulated/0/Download/WoundCamRtc/", "time.txt");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(ts.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int read_systemtime(){
        StringBuilder text = new StringBuilder();
        try {
            File file = new File("/storage/emulated/0/Download/WoundCamRtc/", "time.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close() ;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(text.toString());
    }

}
