package org.itri.woundcamrtc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.litesuits.http.LiteHttp;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.impl.huc.HttpUrlClient;
import com.litesuits.http.listener.HttpListener;
import com.litesuits.http.request.StringRequest;
import com.litesuits.http.request.content.multi.MultipartBody;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.MainActivity;
import org.itri.woundcamrtc.R;
import org.itri.woundcamrtc.helper.DBTableHelper;
import org.itri.woundcamrtc.helper.PermissionChecker;
import org.itri.woundcamrtc.helper.ServiceHelper;
import org.itri.woundcamrtc.helper.XSslLiteHttp;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdatePwdActivity extends Activity {
    private final String TAG = getClass().getSimpleName();

    private boolean isShowLogoutHint = true; //顯示登出提醒

    private UpdatePwdActivity activity;
    private Context bContext;

    public String params = "";

    private EditText user;
    private EditText oldPsd;
    private EditText checkPsd;
    private EditText newPsd;

    private TextView version;

    private Button Button_update;
    private Button Button_cancel;

    private LiteHttp mLiteHttp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updatepwd);

        activity = this;
        bContext = this;
        initLiteHttp();

        user = (EditText) findViewById(R.id.username1);
        oldPsd = (EditText) findViewById(R.id.oldPsd);
        newPsd = (EditText) findViewById(R.id.newPsd);
        checkPsd = (EditText) findViewById(R.id.checkPsd);
        Button_update = (Button) findViewById(R.id.updatePsd);
        version = (TextView) findViewById(R.id.version);
        Button_cancel = (Button) findViewById(R.id.cancel);

        setVersion();

        Button_update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                StrictMode.setThreadPolicy(policy);
                ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

                try {
                    char secret = '8';

                    String account = encryptAndDencrypt(user.getText().toString(), secret);
                    String oldPass = encryptAndDencrypt(oldPsd.getText().toString(), secret);
                    String newPass = encryptAndDencrypt(newPsd.getText().toString(), secret);
                    String checkPass = encryptAndDencrypt(checkPsd.getText().toString(), secret);


                    if (account.isEmpty() || oldPass.isEmpty() || newPass.isEmpty() || checkPass.isEmpty()) {
                        showDialog(getString(R.string.remind_title), getString(R.string.info_update_not_filled), 1);
                    } else {
                        if (!newPass.equals(checkPass)) {
                            showDialog(getString(R.string.remind_title), getString(R.string.newpwd_not_match_checkpwd), 1);
                        } else {
                            if (mNetworkInfo == null) {
                                showDialog(getString(R.string.remind_title), getString(R.string.confirm_network), 1);
                            } else {
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String uploadUrl = AppResultReceiver.DEFAULT_UPDATE_PASS_PATH;

                                            MultipartBody body = new MultipartBody();

                                            Map<String, String> params = new HashMap<String, String>();
                                            params.put("email", account);
                                            params.put("oldPassword", oldPass);
                                            params.put("newPassword", newPass);
                                            Log.v(TAG, "加密後email " + account);
                                            Log.v(TAG, "加密後newPassword " + newPass);
                                            Log.v(TAG, "加密後oldPassword " + oldPass);
                                            final StringRequest upload = new StringRequest(uploadUrl)
                                                    .setMethod(HttpMethods.Post)
                                                    .setParamMap(params)
                                                    .setHttpListener(
                                                            new HttpListener<String>(true, false, false) {
                                                                @Override
                                                                public void onSuccess(String s, Response<String> response) {
                                                                    super.onSuccess(s, response);
                                                                    response.printInfo();
                                                                    JSONObject mJsonObject = null;
                                                                    try {
                                                                        mJsonObject = new JSONObject(response.getResult());
                                                                        String result = mJsonObject.getString("success");
                                                                        String msg = mJsonObject.getString("msg");

                                                                        if (result.toLowerCase().equals("true")) {
                                                                            try {
                                                                                Log.v(TAG, "修改成功");
                                                                                showDialog(getString(R.string.remind_title), getString(R.string.update_success), 2);

                                                                            } catch (Exception e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        } else {
                                                                            showDialog(getString(R.string.remind_title), getString(R.string.login_failed), 1);
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(HttpException exception, Response<String> response) {
                                                                    super.onFailure(exception, response);

                                                                    try {
                                                                        showDialog(getString(R.string.remind_title), getString(R.string.api_failed), 1);
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        setListener(oldPsd);
        setListener(newPsd);
        setListener(checkPsd);

        Button_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(UpdatePwdActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
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
            AppResultReceiver.logoutPreExecute(UpdatePwdActivity.this);
            isShowLogoutHint = false;
        }
    }

    @Override
    public void onBackPressed() {   //返回登入頁面
        Intent intent = new Intent(UpdatePwdActivity.this, LoginActivity.class);
        startActivity(intent);
        isShowLogoutHint = true;
        finish();
    }

    private void setListener(EditText editText) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            Boolean isShow = false;

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
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (isShow) {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(passIcon, null, invisible, null);
                            isShow = false;
                        } else {
                            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(passIcon, null, visible, null);
                            isShow = true;
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void showDialog(final String title, final String message, final int type) {
        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdatePwdActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setIcon(R.mipmap.color_light_48);
                //按到旁邊的空白處AlertDialog也不會消失
                builder.setCancelable(false);

                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (type == 1) {
                            dialog.cancel();
                        } else if (type == 2) {
                            Intent intent = new Intent(UpdatePwdActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                });

                builder.show();
            }
        });
    }

    //修改Toast內容
    public void showToast(String context) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(UpdatePwdActivity.this, context, Toast.LENGTH_SHORT);
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
//                    .setConnectTimeout(30000)         // connect timeout: 10s
//                    //.setConcurrentSize(1)
//                    //.setDefaultMaxRetryTimes(0)
//                    //.setDefaultMaxRedirectTimes(1)
//                    //.setWaitingQueueSize(20)
//                    //.setMaxMemCacheBytesSize(5242880L)
//                    .create();
            mLiteHttp = (new XSslLiteHttp(bContext, 30, 360)).getLiteHttp();

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

    void setVersion() {
        version.setText(AppResultReceiver.APP + " " + getString(R.string.version_no) + " " + AppResultReceiver.APPVER);
        version.setVisibility(View.VISIBLE);
    }

    public static String encryptAndDencrypt(String value, char secret) {
        byte[] bt = value.getBytes();    //將需要加密的內容轉換成字節數組
        for (int i = 0; i < bt.length; i++) {
            bt[i] = (byte) (bt[i] ^ (int) secret);    //利用異或運算進行加密動作
        }
        String newResult = new String(bt, 0, bt.length);    //將加密後的字串存取到newresult
        return newResult;
    }
}
