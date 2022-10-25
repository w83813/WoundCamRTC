
package org.itri.woundcamrtc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Configuration;

import com.birbit.android.jobqueue.JobManager;
//import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.github.yoojia.anyversion.AnyVersion;
import com.github.yoojia.anyversion.DownloadingCallback;
import com.github.yoojia.anyversion.NotifyStyle;
import com.google.webviewlocalserver.WebViewLocalServer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.litesuits.http.LiteHttp;

import org.apache.log4j.chainsaw.Main;
import org.appspot.apprtc.AppRTCCallback;
import org.itri.woundcamrtc.analytics.HomographyHelper;
import org.itri.woundcamrtc.helper.BitmapHelper;
import org.itri.woundcamrtc.helper.DocumentsUtils;
import org.itri.woundcamrtc.helper.*;
import org.itri.woundcamrtc.helper.SecretDbHelper;
import org.itri.woundcamrtc.helper.ServiceHelper;
import org.itri.woundcamrtc.helper.ShellHelper;
import org.itri.woundcamrtc.helper.XSslLiteHttp;
import org.itri.woundcamrtc.helper.XSslOkHttpClient;
import org.itri.woundcamrtc.job.JobQueueFindPatientNoJob;
import org.itri.woundcamrtc.job.JobQueueMarkerJob;
import org.itri.woundcamrtc.job.JobQueueSetParamsJob;
import org.itri.woundcamrtc.ocr.ScannerActivity;
import org.itri.woundcamrtc.rtc.ImageProcessVideoSink;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.itri.woundcamrtc.helper.DBTableHelper;
import org.itri.woundcamrtc.helper.PermissionChecker;
import org.itri.woundcamrtc.helper.StringUtils;

import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.listener.HttpListener;
import com.litesuits.http.request.StringRequest;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;
import com.longdo.mjpegviewer.MjpegView;
import com.longdo.mjpegviewer.MtxtDownloader;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.common.ImageSaverFlir;
import com.shlll.libusbcamera.USBCameraHelper;

import net.sqlcipher.database.SQLiteDatabase;

import org.itri.woundcamrtc.job.BeepManager;
import org.itri.woundcamrtc.job.JobQueueFindIPJob;
//import org.itri.woundcamrtc.job.JobQueueMarkerJob;
import org.itri.woundcamrtc.job.JobQueueUploadFileJob;
import org.appspot.apprtc.CallRtcClient;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.webrtc.VideoFrame;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;
import okio.Timeout;
import top.defaults.camera.AutoFitTextureView;
import top.defaults.camera.Error;
import top.defaults.camera.Photographer;
import top.defaults.camera.PhotographerFactory;

import static org.itri.woundcamrtc.AppResultReceiver.IS_FOR_MIIS_MPDA;
import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;
import static org.itri.woundcamrtc.AppResultReceiver.isAccountCorrecct;
import static org.itri.woundcamrtc.AppResultReceiver.REQUEST_CODE_OCR;
import static org.itri.woundcamrtc.AppResultReceiver.REQUEST_CODE_RECORD_OCR;
import static org.itri.woundcamrtc.AppResultReceiver.isUvcDeviceOK;
import static org.itri.woundcamrtc.SettingsFragment.KEY_PREF_FILE_ROOT_PATH;


public class MainActivity extends Activity {
    private final String TAG = getClass().getSimpleName() + ".uvc";

    static {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        //opencv3.4
        System.loadLibrary("opencv_java3");
        //opencv4.1
//        System.loadLibrary("opencv_java4");
    }

    private String callerId;
    public String iip = "";
    public String iipOld = "-";
    public String oldEvlId = "";  //舊的evlId
    public String evlId = "";
    public String evlStep = "13";   //txt檔辨識碼
    public String curStepFilename = "";
    public String ownerId = "";
    public String todaydate = "";
    public String part = "";
    public String account =AppResultReceiver.account;
    public String newdate = "";

    public String password =AppResultReceiver.password;
    private String outputMediaFileType;
    private String loginDate = "";
    public String userId = "";
    public String roleId = "";
    public boolean boolean_uvccamera_view0 = false;
    public boolean boolean_cdpreview = false;
    private boolean isShowLogoutHint = true; //顯示登出提醒

    public int count = 0;
    private int fileCount = 0;
    public int loginPeriod = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Uri outputMediaFileUri;
    public Button button_takePic;
    public Button button_takePicL;
    public Button button_takePicR;
    public Button button_bodyPart;
    public Button button_ownerId;
    private Button detection;
    public Button button_goback;
    //private Button menuImageButton;
    private Button menuImageButton_logout;
    private Button menuImageButton_webView;
    private Button menuImageButton_cam;
    private Button menuImageButton_list;
    public Button menuImageButton_light;
    private Button menuImageButton_help;
    private Button menuImageButton_marker;
    private Button menuImageButton_upload;
    private ImageButton button_record_scan;
    private ImageButton button_record_ocr;
    private ImageButton button_id_scan;
    private ImageButton button_id_ocr;
    public TextView file_time;
    public TextView userName;
    private TextView nonupload_filesize;
    public TextView detect_dist;
    public static CallRtcClient rtcClient;
    private ImageProcessVideoSink rtcVideoSink = null;
    public org.webrtc.SurfaceViewRenderer glview_call;
    private ImageView mediaPreview;
    private EditText record;
    private EditText idcard;
    private TabHost tabs;
    private  TextView tv_pitch,tv_roll;

    private boolean isWebRtcConnected = false;
    private boolean isUploading = false;

    public File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
    File mainDir;
    DBTableHelper database;
    public SQLiteDatabase Sercretdb;
    public SecretDbHelper sqllitesecret;
    private final SimpleDateFormat evalDate = new SimpleDateFormat("yyyy-MM-dd");
    public SimpleDateFormat nowEvalTime = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
    public DecimalFormat df = new DecimalFormat("#.#");

    public JobManager jobManagerUrgent = null;
    public JobManager jobManagerRelax = null;
    public JobManager jobManagerMarker = null;
    public BeepManager beepManager = null;
    private MainActivity mActivity;
    private Context mContext;
    private Handler mHandler = new Handler();
    protected PermissionChecker permissionChecker;
    public TextureView cdpreview = null;
    public Semaphore semaphore = null;
    public JSONArray patientArr = null;
    public static Object canvasSync = new Object();
    private java.util.Timer mTimerCheckCameraTimeout;
    private TimerTask mTaskLoginTimeout;
    private java.util.Timer mTimerLoginTimeout;
    private TimerTask mTaskCheckCameraTimeout;

    private java.util.Timer mTimerTestTimeout;
    private TimerTask mTaskTestTimeout;

    public Runnable mGetThermalRunner;
    public Runnable mGetMsgRunner;
    public Runnable mFindIpRunner;
    public Handler mGetIpHandler = new Handler();
    public Handler mGetMsgHandler = new Handler();
    public Handler mGetThermalHandler = new Handler();
    public MtxtDownloader mtxtDownloader;

    private ProgressDialog indeterminateDialog;
    private AlertDialog alertDialogGridView;
    List<String> myList = new ArrayList<String>();
    AlertDialog optionsMenuDialog = null;

    private io.socket.client.Socket mSocketIO;
    private LiteHttp mLiteHttp = null;
    //    private TrustManager[] trustManagers = null;
    private IntentIntegrator zxingScanIntegrator;

    //    public boolean isUVCPreview0 = false;
    public boolean isUVCRequesting0 = false;
    //public CameraViewInterface mUVCCameraView0;
    public TextureView mUVCCameraView0;
    public int mUVCDeviceFilterXmlId = 0;
    public USBCameraHelper mUSBCameraHelper = null;
    public IFrameCallback mIFrameCallback = null;

    //    private JavaCameraView javaCameraView;
    public Photographer photographer = null;
    //    private Photographer photographer2;
    private MjpegView mIPCameraMjpgView;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetic;
    private TestSensorListener mSensorListener;
    private boolean bl_gpio = true;
    public static String single_upload_id = "";

    private MessageQueue.IdleHandler resumeIdleHandler = new MessageQueue.IdleHandler() {
        @Override
        public boolean queueIdle() {
            Log.d(TAG, "queueIdle");
            onResumeInit();
            return false; //run once
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);  //關閉APP標題橫槓

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateBroadcastReceiver, intentFilter);


        getWindow().addFlags(
                LayoutParams.FLAG_KEEP_SCREEN_ON  //防止螢幕自動關閉
//                        | LayoutParams.FLAG_DISMISS_KEYGUARD    //解鎖螢幕
                        //               | LayoutParams.FLAG_SHOW_WHEN_LOCKED    //螢幕鎖定時也可以顯示
                        | LayoutParams.FLAG_TURN_SCREEN_ON);

        mContext = this;
        mActivity = this;
        mActivity.setContentView(R.layout.main);
        detection = findViewById(R.id.detection);
        tv_pitch = findViewById(R.id.tv_pitch);
        tv_roll = findViewById(R.id.tv_roll);

        mSensorListener = new TestSensorListener();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        todaydate=evalDate.format(new Date());
        if (dataEncrypt == false) {
            mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
            database = DBTableHelper.getInstance(getApplicationContext(), mainDir.getAbsolutePath() + File.separator + AppResultReceiver.PROJECT_NAME + ".db");

        } else {

            SQLiteDatabase.loadLibs(this);
            sqllitesecret = new SecretDbHelper(this);
            Sercretdb = SecretDbHelper.getInstance(this).getWritableDatabase("MIIS");
        }

        AppResultReceiver.mMainActivity = this;

        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                Log.d(TAG, "queueIdle");
                onCreateInit();
                return false; //run once
            }
        });
    }

    protected void onCreateInit() {
        Log.d(TAG, "onCreateInit");
        try {
            AppResultReceiver.initPreferences(this);
            AppResultReceiver.initOrientation(this);
            //AppResultReceiver.setAppLocale(this, AppResultReceiver.getAppLanguage(this));

            mActivity.checkPermissions();
            // globle object
            jobManagerUrgent = configureJobQueueManager();
            jobManagerRelax = configureJobQueueManagerRelax();
            jobManagerMarker = configureJobQueueManagerMarker();
            beepManager = new BeepManager(this);

            mActivity.generateViews();
            AppResultReceiver.GET_USB_DEVICE_DELAY_MS = 100; // more than 1000
            AppResultReceiver.GET_MIPI_DEVICE_DELAY_MS = 100; // more than 1000
        } catch (Exception ex) {
        }
        Log.i(TAG, "DEBUG_LEVEL :" + AppResultReceiver.DEBUG_LEVEL);
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart");
        super.onRestart();
        AppResultReceiver.GET_USB_DEVICE_DELAY_MS = 100; // more than 500
        AppResultReceiver.GET_MIPI_DEVICE_DELAY_MS = 100; // more than 500
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();

        if (IS_FOR_MIIS_MPDA)
            ShellHelper.enableDevice();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");


        generateTasks();
        super.onResume();
        if (bl_gpio){
            Looper.myQueue().addIdleHandler(resumeIdleHandler);
        }

        mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mSensorListener, mMagnetic, SensorManager.SENSOR_DELAY_UI);


        newdate=evalDate.format(new Date());
        if(!newdate.equals(todaydate)){
            updateViewInfo();
            todaydate=newdate;
        }

    }

    protected void onResumeInit() {
        Log.d(TAG, "onResumeInit");
        try {


            //  HomographyHelper.calculateAligmentRgb2DepthHomography(this, HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE);
            //   HomographyHelper.calculateAligmentThm2DepthHomography(this, HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE);
            //   HomographyHelper.calculateAligmentRgb2ThmHomography(this, HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE);




            isShowLogoutHint = true;
            if (rtcClient != null) {
                rtcClient.onResume();
            }

            if (mLiteHttp == null) {

                //版本檢查更新
                //mActivity.appVersionCheck();

                //GUI
                mActivity.generateFileDBs();
                mActivity.generateButtons();

                // network
                mActivity.initLiteHttp();
                mActivity.startWebsocket();
                mtxtDownloader = new MtxtDownloader(mContext, mActivity);
                mtxtDownloader.setMsecWaitAfterReadTxtError(1000);
                AppResultReceiver.mAssetServer = new WebViewLocalServer(mContext);
                AppResultReceiver.mAssetServerDetails = AppResultReceiver.mAssetServer.hostAssets("", "/", true, true);

                // timer & task
                mActivity.generateTasks();

                mActivity.getPatientNoList();

                mediaPreview.setVisibility(View.VISIBLE);
            }

            rtcClient.setFlash(false);
            if (file.exists()) {
                filesize(file.getAbsolutePath());
            } else {
                setNonUploadFileSize(getString(R.string.nonupload_file_count_) + fileCount + getString(R.string._count));
            }

            if (semaphore == null) {
                //locker = new ReentrantLock(false);
                semaphore = new Semaphore(1);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Looper.myQueue().removeIdleHandler(resumeIdleHandler);
            //切到其他畫面 有可能會留在無法拍照的狀態 所以須改為可拍照
            AppResultReceiver.isTakingPicture = false;
        }
    }

    @Override
    public void onPause() {

        Log.i(TAG, "onPause");
        super.onPause();

        mSensorManager.unregisterListener(mSensorListener);

        mGetIpHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        try {
            semaphore.release();
        } catch (Exception e) {

        }
        semaphore = null;

        if (rtcClient != null) {
            rtcClient.onPause();
        }
    }

    @Override
    public void onStop() {

        Log.i(TAG, "onStop");
        super.onStop();
        if(IS_FOR_MIIS_MPDA){
            if(mTimerCheckCameraTimeout!=null){
                mTimerCheckCameraTimeout.cancel();
            }
        }

        if(mTimerLoginTimeout!=null){
            mTimerLoginTimeout.cancel();
        }
        if(mTimerTestTimeout!=null){
            mTimerTestTimeout.cancel();
        }
//        mTimerTestTimeout.cancel();
        mHandler.removeCallbacks(null);
        mGetIpHandler.removeCallbacks(null);

        if (IS_FOR_MIIS_MPDA)
            ShellHelper.disableDevice();

//        if (AppResultReceiver.isUvcDevice) {
//            onStopUVCCamera();
//            onDestroyUVCCamera();
//        }


        if (isShowLogoutHint) {
            AppResultReceiver.logoutPreExecute(MainActivity.this);
            isShowLogoutHint = false;
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();

        unregisterReceiver(mScreenStateBroadcastReceiver);

        if (rtcClient != null) {
            rtcClient.stopCall();
            try{
                rtcClient.onDestroy();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        rtcClient = null;
        photographer = null;

        //System.exit(0);
    }

    @Override
    public void onBackPressed() {   //離開主畫面之提醒通知
        isShowLogoutHint = true;
        if (SettingsFragment.openView) {
            SettingsFragment.openView = false;
            super.onBackPressed();
        } else {
            showDialog(getString(R.string.remind_title), getString(R.string.confirm_leave_app), 1);
        }
    }

    //使用手指觸碰螢幕事件程式
    public boolean onTouchEvent(MotionEvent event) {
        rtcClient.onTouchEvent(event);
        //即便觸擊沒有放開也可持續執行 onTouchEvent  (return true)
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        detectOrientation(newConfig.orientation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "收到的resultCode為:" + resultCode);
        if (data == null)
            return;

        if (requestCode == org.itri.woundcamrtc.AppResultReceiver.REQUEST_CODE_OCR) {
            String ocr_txt = data.getStringExtra(Intent.EXTRA_TEXT);
            idcard.setText(ocr_txt, TextView.BufferType.EDITABLE);
            idcard.setSelection(idcard.getText().length());
            //showOwnerIdDialog(ocr_txt);

        } else if (requestCode == AppResultReceiver.REQUEST_CODE_RECORD_OCR) {
            String ocr_txt = data.getStringExtra(Intent.EXTRA_TEXT);
            record.setText(ocr_txt, TextView.BufferType.EDITABLE);
            record.setSelection(record.getText().length());
            //showOwnerIdDialog(ocr_txt);

        } else if (requestCode == com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scanningResult != null && scanningResult.getContents() != null) {
                String scanContent = scanningResult.getContents();
                int curtab = tabs.getCurrentTab();
                switch (curtab) {
                    case 2:
                        record.setText(scanContent, TextView.BufferType.EDITABLE);
                        record.setSelection(record.getText().length());
                    case 3:
                        idcard.setText(scanContent, TextView.BufferType.EDITABLE);
                        idcard.setSelection(idcard.getText().length());
                }
                //showOwnerIdDialog(scanContent);

            } else {
                super.onActivityResult(requestCode, resultCode, data);
                Toast.makeText(getApplicationContext(), getString(R.string.alert_error), Toast.LENGTH_LONG).show();

                showOwnerIdDialog("");
            }
        } else if (resultCode == AppResultReceiver.CHOOSE_BODY_RESULT_OK) {
            part = data.getStringExtra("bodyChoose");
            button_bodyPart.setText(part);
        } else if (resultCode == AppResultReceiver.SELECT_TAKEPICINFO_OK) {
            ownerId = data.getStringExtra("ownerId");
            part = data.getStringExtra("bodyPart");
            button_ownerId.setText(ownerId);
            button_bodyPart.setText(part);
            List<Map<String, Object>> listMeasure = null;

            if (dataEncrypt == false) {
                listMeasure = database.queryHistoryRecord(ownerId);

            } else {
                listMeasure = sqllitesecret.queryHistoryRecord(Sercretdb, ownerId);
            }


            if (listMeasure.size() > 0) {
                for (Map<String, Object> map : listMeasure) {
                    evlId = (String) map.get("evid");
                    count = (Integer) map.get("number");
                }
            } else {
                evlId = nowEvalTime.format(new Date());
                count = 0;
            }
        } else if (resultCode == AppResultReceiver.SCAN_RESULT_OK) {
            ownerId = data.getStringExtra("scanNo");
            showOwnerIdDialog(ownerId);
        } else if (resultCode == DocumentsUtils.OPEN_DOCUMENT_TREE_CODE) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);

                DocumentsUtils.saveTreeUri(mActivity, KEY_PREF_FILE_ROOT_PATH, uri);
                InputStream is = null;
                try {
                    is = getContentResolver().openInputStream(uri);

                    // Just for quick sample (I know what I will read)
                    byte[] buffer = new byte[1024];
                    int read = is.read(buffer);
                    String text = new String(buffer, 0, read);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showOpenDocumentTree() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            StorageManager sm = mActivity.getSystemService(StorageManager.class);

            StorageVolume volume = sm.getStorageVolume(new File(DocumentsUtils.getAppRootOfSdCardRemovable(mActivity)));

            if (volume != null) {
                intent = volume.createAccessIntent(null);
            }
        }
        isShowLogoutHint = false;
        if (intent == null) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        }
        startActivityForResult(intent, DocumentsUtils.OPEN_DOCUMENT_TREE_CODE);
    }

//
//    public void setRTCButtonToDisconnect() {
//        if (isCamming) {
//            isCamming = false;  //關閉視訊連線
//            //menuImageButton_cam.setImageResource(R.mipmap.webcam_48);
//            //menuImageButton_cam.setBackgroundColor(Color.argb(63, 63, 63, 63));
//            rtcClient.stopCall();
//            // showToast("結束連線");
//            menuImageButton_cam.setEnabled(false);
//            menuImageButton_cam.setAlpha(0.3F);
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    menuImageButton_cam.setEnabled(true);
//                    menuImageButton_cam.setAlpha(1.0F);
//                }
//            }, 5000);
//        }
//    }

    public void showOwnerIdDialog(String _ownerId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.patient_id));

        final EditText input = new EditText(new ContextThemeWrapper(MainActivity.this, R.style.editTextStyle));
        input.setSelectAllOnFocus(true);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.requestFocus();
        input.setText(_ownerId);
        builder.setView(input);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                ownerId = input.getText().toString().trim().toUpperCase();
                if (ownerId.equals("")) {
                    button_ownerId.setText(getString(R.string.patient_id));
                } else {
                    button_ownerId.setText(ownerId);
                }

                SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = shared.edit();
                editor.putString(AppResultReceiver.PREFERENCE_EVALUATION_CASE_ID, evlId);
                editor.putString(AppResultReceiver.PREFERENCE_EVALUATION_OWNER_ID, ownerId);
                editor.apply();
            }
        });
        builder.setNeutralButton(getString(R.string.scan_ownerId), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowLogoutHint = false;
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                startActivityForResult(intent, REQUEST_CODE_OCR);

            }
        });
        builder.setNegativeButton(getString(R.string.scan_patient_barcode), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    zxingScanIntegrator = new IntentIntegrator(MainActivity.this);
                    zxingScanIntegrator.setPrompt(getString(R.string.please_scan_barcode));
                    zxingScanIntegrator.setTimeout(60000);
                    zxingScanIntegrator.initiateScan();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        builder.show();
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    //設定螢幕右下角文字顯示內容
    public void setDetectedDistance(String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    detect_dist.setText(text);
                    detect_dist.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    //設定照片拍攝時間顯示內容
    void setFileTime(String text) {
        file_time.setText(text);
        file_time.setVisibility(View.INVISIBLE);
    }

    //設定目前檔案數量顯示內容
    void setNonUploadFileSize(String text) {
        nonupload_filesize.setText(text);
        nonupload_filesize.setVisibility(View.VISIBLE);
    }

    void resetOwnerId(String text) {
        button_ownerId.setText(text);
    }

    //設定使用者名稱
    void setUserName(String text) {
        userName.setText(getString(R.string.user) + " : " + text);
        userName.setVisibility(View.VISIBLE);
    }

    public void setMarkerBorder(final int leftMargin, final int topMargin) {

        final ImageView imgDetectedMarker = (ImageView) findViewById(R.id.imgDetectedMarker);
        if (imgDetectedMarker != null) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (AppResultReceiver.correctionColorDetected == true) {
                            if (AppResultReceiver.detectedMarkerStep == 0) {
                                if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker_c) {
                                    imgDetectedMarker.setImageResource(R.mipmap.ic_marker_c);
                                    imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker_c));
                                }
                                if (AppResultReceiver.isTakingPicture == false && Math.abs(SystemClock.uptimeMillis() - AppResultReceiver.detectedMarkerUptimeMillis) > 400) {
                                    if (imgDetectedMarker.getVisibility() != View.INVISIBLE)
                                        imgDetectedMarker.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                if (AppResultReceiver.detectedMarkerStep == 1) {
                                    if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker_c3) {
                                        imgDetectedMarker.setImageResource(R.mipmap.ic_marker_c3);
                                        imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker_c3));
                                    }
                                } else if (AppResultReceiver.detectedMarkerStep >= 2) {
                                    if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker_c6) {
                                        imgDetectedMarker.setImageResource(R.mipmap.ic_marker_c6);
                                        imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker_c6));
                                    }
                                }
                                setTopLeftMarginMarker(imgDetectedMarker, leftMargin, topMargin);
                                if (imgDetectedMarker.getVisibility() != View.VISIBLE)
                                    imgDetectedMarker.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (AppResultReceiver.detectedMarkerStep == 0) {
                                if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker) {
                                    imgDetectedMarker.setImageResource(R.mipmap.ic_marker);
                                    imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker));
                                }
                                if (AppResultReceiver.isTakingPicture == false && Math.abs(SystemClock.uptimeMillis() - AppResultReceiver.detectedMarkerUptimeMillis) > 400) {
                                    if (imgDetectedMarker.getVisibility() != View.INVISIBLE)
                                        imgDetectedMarker.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                if (AppResultReceiver.detectedMarkerStep == 1) {
                                    if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker3) {
                                        imgDetectedMarker.setImageResource(R.mipmap.ic_marker3);
                                        imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker3));
                                    }
                                } else if (AppResultReceiver.detectedMarkerStep >= 2) {
                                    if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker6) {
                                        imgDetectedMarker.setImageResource(R.mipmap.ic_marker6);
                                        imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker6));
                                    }
                                }
                                setTopLeftMarginMarker(imgDetectedMarker, leftMargin, topMargin);
                                if (imgDetectedMarker.getVisibility() != View.VISIBLE)
                                    imgDetectedMarker.setVisibility(View.VISIBLE);
                            }

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    public void setMarkerBorder() {
        final ImageView imgDetectedMarker = (ImageView) findViewById(R.id.imgDetectedMarker);
        if (imgDetectedMarker != null) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (AppResultReceiver.correctionColorDetected == true) {
                            if (AppResultReceiver.detectedMarkerStep == 0) {
                                if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker_c) {
                                    imgDetectedMarker.setImageResource(R.mipmap.ic_marker_c);
                                    imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker_c));
                                }
                                if (AppResultReceiver.isTakingPicture == false && Math.abs(SystemClock.uptimeMillis() - AppResultReceiver.detectedMarkerUptimeMillis) > 400)
                                    imgDetectedMarker.setVisibility(View.INVISIBLE);
                            } else {

                                if (AppResultReceiver.detectedMarkerStep == 1) {
                                    if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker_c3) {
                                        imgDetectedMarker.setImageResource(R.mipmap.ic_marker_c3);
                                        imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker_c3));
                                    }
                                } else if (AppResultReceiver.detectedMarkerStep >= 2) {
                                    if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker_c6) {
                                        imgDetectedMarker.setImageResource(R.mipmap.ic_marker_c6);
                                        imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker_c6));
                                    }
                                }
                            }
                        } else {
                            if (AppResultReceiver.detectedMarkerStep == 0) {
                                if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker) {
                                    imgDetectedMarker.setImageResource(R.mipmap.ic_marker);
                                    imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker));
                                }
                                if (AppResultReceiver.isTakingPicture == false && Math.abs(SystemClock.uptimeMillis() - AppResultReceiver.detectedMarkerUptimeMillis) > 400)
                                    imgDetectedMarker.setVisibility(View.INVISIBLE);
                            } else {

                                if (AppResultReceiver.detectedMarkerStep == 1) {
                                    if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker3) {
                                        imgDetectedMarker.setImageResource(R.mipmap.ic_marker3);
                                        imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker3));
                                    }
                                } else if (AppResultReceiver.detectedMarkerStep >= 2) {
                                    if (imgDetectedMarker.getTag() == null || (int) imgDetectedMarker.getTag() != R.mipmap.ic_marker6) {
                                        imgDetectedMarker.setImageResource(R.mipmap.ic_marker6);
                                        imgDetectedMarker.setTag(Integer.valueOf(R.mipmap.ic_marker6));
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    public void setFocusedBorder(final int leftMargin, final int topMargin, boolean show) {
        final ImageView imgFocusArea = (ImageView) findViewById(R.id.imgFocusArea);
        if (imgFocusArea != null) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (leftMargin != -1 && topMargin != -1) {
                            if (imgFocusArea.getTag() == null || (int) imgFocusArea.getTag() != R.mipmap.ic_focused) {
                                imgFocusArea.setImageResource(R.mipmap.ic_focused);
                                imgFocusArea.setTag(Integer.valueOf(R.mipmap.ic_focused));
                            }
                            setTopLeftMarginFoucs(imgFocusArea, leftMargin, topMargin);
                        }
                        if (show) {
                            if (imgFocusArea.getVisibility() != View.VISIBLE)
                                imgFocusArea.setVisibility(View.VISIBLE);
                        } else {
                            if (imgFocusArea.getVisibility() != View.INVISIBLE)
                                imgFocusArea.setVisibility(View.INVISIBLE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    public void setFocusingBorder(final int leftMargin, final int topMargin, boolean show) {
        final ImageView imgFocusArea = (ImageView) findViewById(R.id.imgFocusArea);
        if (imgFocusArea != null) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (leftMargin != -1 && topMargin != -1) {
                            if (imgFocusArea.getTag() == null || (int) imgFocusArea.getTag() != R.mipmap.ic_border) {
                                imgFocusArea.setImageResource(R.mipmap.ic_border);
                                imgFocusArea.setTag(Integer.valueOf(R.mipmap.ic_border));
                            }
                            setTopLeftMarginFoucs(imgFocusArea, leftMargin, topMargin);
                        }
                        if (show) {
                            if (imgFocusArea.getVisibility() != View.VISIBLE)
                                imgFocusArea.setVisibility(View.VISIBLE);
                        } else {
                            if (imgFocusArea.getVisibility() != View.INVISIBLE)
                                imgFocusArea.setVisibility(View.INVISIBLE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    public void setTopLeftMarginFoucs(View view, int leftMargin, int topMargin) {
        if (view != null) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            if (layoutParams != null) {
//                //layoutParams.setMargins(leftMargin - (int) (view.getWidth() * 0.5), topMargin - (int) (view.getHeight() * 1.0), layoutParams.rightMargin, layoutParams.bottomMargin);
//                layoutParams.setMargins(leftMargin - (int) (view.getWidth() * 0.5), topMargin - (int) (view.getHeight() * 0.5), layoutParams.rightMargin, layoutParams.bottomMargin);
//                view.setLayoutParams(layoutParams);
                view.setTranslationX(leftMargin - (int) (view.getWidth() * 0.5));
                view.setTranslationY(topMargin - (int) (view.getHeight() * 0.5));
            }
        }
    }

    public void setTopLeftMarginMarker(View view, int leftMargin, int topMargin) {
        //1920x1080
        if (view != null) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            if (layoutParams != null) {

//                Display display = ((WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//                int rotation = display.getRotation();
//
//                switch (rotation) {
//                    case Surface.ROTATION_0: //0
//                        layoutParams.setMargins(leftMargin - (int) (view.getWidth() * 0.5), topMargin - (int) (view.getHeight() * 0.5),
//                                layoutParams.rightMargin, layoutParams.bottomMargin);
//                        break;
//                    case Surface.ROTATION_90: //1
//                        layoutParams.setMargins(leftMargin - (int) (view.getWidth() * 0.5), topMargin - (int) (view.getHeight() * 0.5),
//                                layoutParams.rightMargin, layoutParams.bottomMargin);
//                        break;
//                    case Surface.ROTATION_180: //2
//                        layoutParams.setMargins(leftMargin - (int) (view.getWidth() * 0.5), topMargin - (int) (view.getHeight() * 0.5),
//                                layoutParams.rightMargin, layoutParams.bottomMargin);
//                        break;
//                    case Surface.ROTATION_270: //3
//                        layoutParams.setMargins(leftMargin - (int) (view.getWidth() * 0.5), topMargin - (int) (view.getHeight() * 0.5),
//                                layoutParams.rightMargin, layoutParams.bottomMargin);
//                        break;
//                }
//                view.setLayoutParams(layoutParams);


//                view.setTranslationX(leftMargin - (int) (view.getWidth() * 0.5));
//                view.setTranslationY(topMargin - (int) (view.getHeight() * 0.5));
//                view.setAlpha(0f);
//                view.setScaleX(0f);
//                view.setScaleY(0f);
                view.setTranslationX(leftMargin - (int) (view.getWidth() * 0.5));
                view.setTranslationY(topMargin - (int) (view.getHeight() * 2.0));
            }
        }
    }

    //開啟傷口部位選擇清單
    public void openPartMenu() {
        final CharSequence[] choiceList =
                {"1535436956667", "1535443167876", "1535446791312", "1536040715405", "1536041576475",
                        "1536045052778", "1536131928859", "1536644606452", "1536647335803", "1537084692595",
                        "1537152292000", "1537159191000", "1537251874752", "1537255015229", "1537329084346",
                        "1537857433813", "1537925291705", "1538530587844", "1539043695467", "1539048391890",
                        "1539064744373", "1539066316585", "1539070021668", "1539671267969", "1539675523610",
                        "1539743616800", "1539746261513", "1540171161278", "1531979923000", "1532410333000",
                        "1532411898000", "1532507828000", "1533003713000", "1533021546000", "1533624186000",
                        "1533869205000", "1534147140000", "1534225792000", "1534318117000", "1534476853000",
                        "1534492157000", "1534838033778", "1534903053519", "1534906230000", "1534920057486"
                };

        ArrayAdapter aas = new ArrayAdapter(this
                , android.R.layout.simple_list_item_1
                , choiceList
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(getResources().getColor(R.color.dialog_text));
                tv.setBackgroundColor(getResources().getColor(R.color.item_background));
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(18.0f);
                //tv.
                return tv;
            }
        };

        // 建立 GridView 物件
        GridView gridView = new GridView(this);
        gridView.setNumColumns(1);
        gridView.setGravity(Gravity.CENTER_HORIZONTAL);
        gridView.setPadding(50, 50, 50, 50);
        gridView.setVerticalSpacing(40);
        gridView.setHorizontalSpacing(40);
        gridView.setAdapter(aas);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                view.setBackgroundColor(getResources().getColor(R.color.item_pressed));
                //view.setBackgroundResource(R.color.item_pressed);
                part = choiceList[position].toString();
                button_bodyPart.setText(part);
                AppResultReceiver.filetag = part;
                //mediaPreview.setVisibility(View.VISIBLE);
                alertDialogGridView.dismiss();
            }
        });

        // 顯示對話框
        alertDialogGridView = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(getString(R.string.please_choose_part))
                .setView(gridView)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        part = "";
                        button_bodyPart.setText(getString(R.string.bodypart));
                    }
                })
                .show();
    }

    //開啟選項Menu
    public void openOptionsMenuT() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("");
        final CharSequence[] choiceList = {getString(R.string.setting_camera), getString(R.string.stop_app)};
        int selected = -1; // does not select anything
        builder.setSingleChoiceItems(choiceList, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
//                    //上傳資料
//                    case 0: {
//                        AppResultReceiver.vibrating(mActivity);
//                        Log.i(TAG, "上傳資料");
//                        if (Integer.parseInt(userId) != 2) {
//                            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(mContext);
//                            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + AppResultReceiver.SAVE_DIR;
//
//                            optionsMenuDialog.hide();
//
//                            //資料上傳後更新時間
//                            evlId = nowEvalTime.format(new Date());
//                            curStepFilename = evlId + "_" + evlStep + "_";
//                            setFileTime(evlId);
//                            SharedPreferences.Editor editor = shared.edit();
//                            editor.putString(AppResultReceiver.PREFERENCE_EVALUATION_CASE_ID, evlId);
//                            editor.apply();
//
//                            count = 0;
//                            filesize(file.getAbsolutePath());
//                            myList = new ArrayList<String>();
//
//                            if (isUploading) {
//                                showToast(getString(R.string.file_uploading));
//                            } else {
//                                if (fileCount <= 0) {
//                                    //showToast(getString(R.string.no_files));
//                                    setUploadStatus(true);
//                                    showToast(getString(R.string.start_file_upload));
//                                    jobManagerRelax.addJobInBackground(new JobQueueUploadFileJob(jobManagerRelax, "", mActivity, mContext, mLiteHttp, path));
//                                } else {
//                                    setUploadStatus(true);
//                                    showToast(getString(R.string.start_file_upload));
//                                    jobManagerRelax.addJobInBackground(new JobQueueUploadFileJob(jobManagerRelax, "", mActivity, mContext, mLiteHttp, path));
//                                }
//                            }
//                        } else {
//                            optionsMenuDialog.hide();
//                            showDialog(getString(R.string.remind_title), getString(R.string.no_authority_to_use), 3);
//                        }
//                        break;
//                    }

                    case 0: {
                        AppResultReceiver.vibrating(mActivity);
                        Log.i(TAG, "設置");
                        optionsMenuDialog.hide();
                        button_bodyPart.setVisibility(View.INVISIBLE);
                        button_ownerId.setVisibility(View.INVISIBLE);

                        SettingsFragment.passCamera(rtcClient.getCamera());
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.activity_main2, new SettingsFragment()).addToBackStack(null).commit();
                        break;
                    }

                    //停止程式
                    case 1: {
                        AppResultReceiver.vibrating(mActivity);
                        optionsMenuDialog.dismiss();
                        Log.i(TAG, "停止程式");
//                        finish();
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                        break;
                    }



                    /*case 1: {
                        Log.i(TAG, "熱感");
                        optionsMenuDialog.hide();

                        if (AppResultReceiver.isUvcDevice) {
                            try {
                            AppResultReceiver.isShowLogoutHint = false;
                                Intent intent = new Intent(MainActivity.this, UVCViewActivity.class);
                                intent.putExtra("iip", iip);
                                startActivityForResult(intent, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                            AppResultReceiver.isShowLogoutHint = false;
                                Intent intent = new Intent(MainActivity.this, MJViewActivity.class);
                                intent.putExtra("iip", iip);
                                Log.v(TAG, "iip is:" + iip);
                                startActivityForResult(intent, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }*/

                }
            }
        });
        optionsMenuDialog = builder.create();
        optionsMenuDialog.show();
    }

    //開啟登出選項Menu
    public void openLogoutMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("");
        final CharSequence[] choiceList = {getString(R.string.go_to_login), getString(R.string.logout_and_close_app), getString(R.string.stay)};
        int selected = -1; // does not select anything
        builder.setSingleChoiceItems(choiceList, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    //登出
                    case 0: {
                        Log.i(TAG, "登出");
                        AppResultReceiver.vibrating(mActivity);
                        optionsMenuDialog.hide();

                        if (dataEncrypt == false) {
                            database.deleteRaw("loginInfo", null, null);

                        } else {
                            sqllitesecret.deleteRaw(Sercretdb, "loginInfo", null, null);
                        }

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        finish();
                        startActivity(intent);
                        break;
                    }
                    //登出且關閉app
                    case 1: {
                        Log.i(TAG, "登出且關閉app");
                        AppResultReceiver.vibrating(mActivity);
                        optionsMenuDialog.hide();
                        if (dataEncrypt == false) {
                            database.deleteRaw("loginInfo", null, null);

                        } else {
                            sqllitesecret.deleteRaw(Sercretdb, "loginInfo", null, null);
                        }
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                        break;
                    }
                    //暫不登出
                    case 2: {
                        Log.i(TAG, "暫不登出");
                        AppResultReceiver.vibrating(mActivity);
                        optionsMenuDialog.hide();
                        break;
                    }
                }
            }
        });
        optionsMenuDialog = builder.create();
        optionsMenuDialog.show();
    }

    //開啟資料重整Menu
    public void openReloadOptionsMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("");
        final CharSequence[] choiceList = {getString(R.string.reset_dataset_time), getString(R.string.clean_patient_id), getString(R.string.reset_clean_time_id)};
        int selected = -1; // does not select anything
        builder.setSingleChoiceItems(choiceList, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    //更新上傳時間
                    case 0: {
                        optionsMenuDialog.hide();

                        evlId = nowEvalTime.format(new Date());
                        curStepFilename = evlId + "_" + evlStep + "_";
                        setFileTime(evlId);

                        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor editor = shared.edit();
                        editor.putString(AppResultReceiver.PREFERENCE_EVALUATION_CASE_ID, evlId);
                        editor.apply();

                        count = 0;
                        filesize(file.getAbsolutePath());
                        myList = new ArrayList<String>();

                        optionsMenuDialog.dismiss();
                        break;
                    }

                    //清除病患編號
                    case 1: {
                        optionsMenuDialog.hide();

                        count = 0;
                        ownerId = "";
                        resetOwnerId(getString(R.string.patient_id));
                        filesize(file.getAbsolutePath());
                        myList = new ArrayList<String>();

                        optionsMenuDialog.dismiss();
                        break;
                    }

                    //更新時間、部位與編號
                    case 2: {

                        optionsMenuDialog.hide();

                        evlId = nowEvalTime.format(new Date());
                        curStepFilename = evlId + "_" + evlStep + "_";
                        setFileTime(evlId);

                        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor editor = shared.edit();
                        editor.putString(AppResultReceiver.PREFERENCE_EVALUATION_CASE_ID, evlId);
                        editor.apply();

                        button_bodyPart.setText(getString(R.string.please_choose_part));

                        count = 0;
                        part = "";
                        ownerId = "";
                        resetOwnerId(getString(R.string.patient_id));
                        filesize(file.getAbsolutePath());
                        myList = new ArrayList<String>();
                        optionsMenuDialog.dismiss();
                        break;
                    }
                }
            }
        });
        optionsMenuDialog = builder.create();
        optionsMenuDialog.show();
    }

    public void setUploadStatus(boolean _isUploading) {
        isUploading = _isUploading;
        if(_isUploading){
            menuImageButton_upload.setEnabled(false);
        }else{
            menuImageButton_upload.setEnabled(true);
        }

    }

    public void filesize(String path) {
        File[] list = new File(path).listFiles();
        fileCount = 0;

        if (list != null && list.length > 0) {
            for (int i = 0; i < list.length; i++) {
                String fileName = list[i].getName();
                if (list[i].isFile() && fileName.endsWith("_jpg.jpg")) {
                    fileCount++;
                }
            }
        }

        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    setNonUploadFileSize(getString(R.string.nonupload_file_count_) + fileCount + getString(R.string._count));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Log.i(TAG, "未上傳檔案數量共 " + fileCount + " 張");
    }

    private File getNewestFilefromDir(String dirPath, String ext) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 0; i < files.length-1; i++) {
            if (files[i].getName().contains(ext) && (lastModifiedFile.lastModified() < files[i].lastModified())) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    public synchronized void updateTextureView(final TextureView textureView, final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            public void run() {
                synchronized (canvasSync) {
                    Canvas canvas = null;
                    try {
                        if (textureView != null)
                            canvas = textureView.lockCanvas();

                        try {
                            //2021/02/27
                            if (bitmap != null && canvas != null)
                                canvas.drawBitmap(bitmap, 0, 0, null);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        try {
                            if (textureView != null && canvas != null)
                                textureView.unlockCanvasAndPost(canvas);
                            textureView.invalidate();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        try {
                            bitmap.recycle();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void updateViewInfo() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    button_bodyPart.setText(getString(R.string.please_choose_part));
                    part = "";
                    count = 0; //id歸0
                    button_ownerId.setText(getString(R.string.patient_id));
                    evlId ="";
                    ownerId="";
                    file_time.setText(evlId);
                    file_time.setVisibility(View.INVISIBLE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void openBodyPartDialog(final String title) {
        runOnUiThread(new Runnable() {
            public void run() {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.tab_host, (ViewGroup) findViewById(R.id.tabhost));

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);

                TabHost tabs = (TabHost) layout.findViewById(R.id.tabhost);
                tabs.setup();

                TabHost.TabSpec tab1 = tabs.newTabSpec("front");
                tab1.setContent(R.id.front_scroll);
                tab1.setIndicator(getString(R.string.front_body));
                //tabpage1.setIndicator("",getResources().getDrawable(R.drawable.one));

                TabHost.TabSpec tab2 = tabs.newTabSpec("back");
                tab2.setContent(R.id.back_scroll);
                tab2.setIndicator(getString(R.string.back_body));
                //tabpage2.setIndicator("",getResources().getDrawable(R.drawable.two));

                tabs.addTab(tab1);
                tabs.addTab(tab2);

                //更新Tab樣式
                updateTabStyle(tabs);

                //按到旁邊的空白處AlertDialog也不會消失
                builder.setCancelable(false);
                builder.setView(layout);

                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //finish();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.show();
            }
        });
    }

    private void openPatientNoDialog() {
        runOnUiThread(new Runnable() {
            public void run() {
                int tabhost_temp = R.layout.tab_host;
                ListView lv;
                ArrayAdapter arrayAdapter;
                final String[] selectedOldRecord = {""};
                String[] periodCode = {"A", "B", "C"};  //看診時段
                String[] tagStrList = {"oldRecord", "clinicNo", "recordNo", "idNo"};
                int[] viewIdList = {R.id.oldRecord, R.id.clinicNo, R.id.recordNo, R.id.idNo};
                int[] indicatorList = {R.string.oldRecord, R.string.clinicNo, R.string.recordNo, R.string.idNo};
                List<String> sqllite = new ArrayList<String>();

                //   database.deleteRaw("table_picNumber", null, null);

                List<Map<String, Object>> listMeasure = null;

                if (dataEncrypt == false) {

                    listMeasure = database.querySQLDataList();

                } else {
                    listMeasure = sqllitesecret.querySQLDataList(Sercretdb);
                }
                if (listMeasure.size() > 0) {
                    for (Map<String, Object> map : listMeasure) {
                        String filename = (String) map.get("ownerId");
                        if(!filename.equals("")){
                            sqllite.add(filename);
                        }

                    }
                }
                String[] strList = (String[]) sqllite.toArray(new String[sqllite.size()]);

                if (Build.VERSION.SDK_INT <= 23) {
                    tabhost_temp = R.layout.tab_host_low;
                }

                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(tabhost_temp, (ViewGroup) findViewById(R.id.tabhost));

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);

                tabs = (TabHost) layout.findViewById(R.id.tabhost);
                tabs.setup();

                for (int i = 0; i < tagStrList.length; i++) {
                    TabHost.TabSpec tab = tabs.newTabSpec(tagStrList[i]);
                    tab.setContent(viewIdList[i]);
                    tab.setIndicator(getString(indicatorList[i]));
                    //tab.setIndicator("",getResources().getDrawable(R.drawable.one));
                    tabs.addTab(tab);
                }

                //更新Tab樣式
                updateTabStyle(tabs);

                lv = (ListView) layout.findViewById(R.id.listView);
                arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_checked, strList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView tv = (TextView) super.getView(position, convertView, parent);
                        // Set the color here
                        tv.setTextColor(Color.parseColor("#000000"));
                        tv.setTextSize(18f);

                        return tv;
                    }
                };
                lv.setAdapter(arrayAdapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int row, long arg3) {
                        selectedOldRecord[0] = lv.getItemAtPosition(row).toString();
                        Log.v(TAG, "你點選了第" + lv.getItemAtPosition(row).toString() + "行");
                    }
                });

                button_record_scan = (ImageButton) layout.findViewById(R.id.record_scan);
                button_record_scan.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            zxingScanIntegrator = new IntentIntegrator(MainActivity.this);
                            zxingScanIntegrator.setPrompt(getString(R.string.please_scan_barcode));
                            zxingScanIntegrator.setTimeout(60000);
                            zxingScanIntegrator.initiateScan();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                button_record_ocr = (ImageButton) layout.findViewById(R.id.record_ocr);
                button_record_ocr.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        isShowLogoutHint = false;
                        Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_RECORD_OCR);
                    }
                });



                button_id_scan = (ImageButton) layout.findViewById(R.id.id_scan);
                button_id_scan.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            zxingScanIntegrator = new IntentIntegrator(MainActivity.this);
                            zxingScanIntegrator.setPrompt(getString(R.string.please_scan_barcode));
                            zxingScanIntegrator.setTimeout(60000);
                            zxingScanIntegrator.initiateScan();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                button_id_ocr = (ImageButton) layout.findViewById(R.id.id_ocr);
                button_id_ocr.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        isShowLogoutHint = false;
                        Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_OCR);
                    }
                });

                RadioGroup rg = (RadioGroup) layout.findViewById(R.id.radioBtnGrp);
                EditText roomNo = (EditText) layout.findViewById(R.id.room);
                EditText patientNo = (EditText) layout.findViewById(R.id.patientNo);
                record = (EditText) layout.findViewById(R.id.record);
                idcard = (EditText) layout.findViewById(R.id.idcard);

                /*tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener(){
                    @Override
                    public void onTabChanged(String tabId) {
                        if (tabId.equals("oldRecord")) {

                        }
                        if (tabId.equals("clinicNo")) {

                        }
                        if (tabId.equals("recordNo")) {

                        }
                        if (tabId.equals("idNo")) {

                        }
                    }
                });*/

                //按到旁邊的空白處AlertDialog也不會消失
                builder.setCancelable(false);
                builder.setView(layout);

                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                        String curTab = tabs.getCurrentTabTag();
                        switch (curTab) {
                            case "oldRecord":
                                ownerId = selectedOldRecord[0];
                                break;
                            case "clinicNo":
                                View chkBtn = rg.findViewById(rg.getCheckedRadioButtonId());
                                int idx = rg.indexOfChild(chkBtn);
                                ownerId = periodCode[idx] + "-" + roomNo.getText().toString() + "-" + patientNo.getText().toString();
                                break;
                            case "recordNo":
                                ownerId = record.getText().toString();
                                break;
                            case "idNo":
                                ownerId = idcard.getText().toString();
                                break;
                        }

                        if (ownerId.equals("") || ownerId.indexOf("--") != -1 || ownerId.lastIndexOf("-") == ownerId.length() - 1) {
                            button_ownerId.setText(getString(R.string.patient_id));
                            count = 0;
                            part = "";
                            evlId = nowEvalTime.format(new Date());
                            button_bodyPart.setText(getString(R.string.please_choose_part));
                        } else {
                            int history_number = 0;
                            String history_number_evid = "";
                            String history_part = "";
                            List<Map<String, Object>> listMeasure = null;


                            if (dataEncrypt == false) {
                                listMeasure = database.queryHistoryRecord(ownerId);

                            } else {
                                listMeasure = sqllitesecret.queryHistoryRecord(Sercretdb, ownerId);
                            }

                            if (listMeasure.size() > 0) {
                                for (Map<String, Object> map : listMeasure) {
                                    history_number_evid = (String) map.get("evid");
                                    history_number = (Integer) map.get("number");
                                    history_part = (String) map.get("part");
                                }
                                count = history_number;
                                part = history_part;
                                evlId = history_number_evid;
                            } else {
                                count = 0;
                                part = "";
                                evlId = nowEvalTime.format(new Date());

                            }

                            if (part.equals("")) {
                                button_bodyPart.setText(getString(R.string.please_choose_part));
                            } else {
                                button_bodyPart.setText(part);
                            }

                            file_time.setText(evlId);
                            button_ownerId.setText(ownerId);
                        }

                        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor editor = shared.edit();
                        editor.putString(AppResultReceiver.PREFERENCE_EVALUATION_CASE_ID, evlId);
                        editor.putString(AppResultReceiver.PREFERENCE_EVALUATION_OWNER_ID, ownerId);
                        editor.apply();
                    }
                });

                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //finish();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.show();
            }
        });
    }

    private void updateTabStyle(TabHost tabs) {
        TabWidget tw = tabs.getTabWidget();
        for (int i = 0; i < tw.getChildCount(); i++) {
            tw.getChildTabViewAt(i).setPadding(5, 5, 5, 5);
            TextView tv = (TextView) tw.getChildAt(i).findViewById(android.R.id.title);
            if (Build.VERSION.SDK_INT <= 23) {
                tv.setTextColor(Color.parseColor("#000000"));
                tv.setTextSize(16f);
            } else {
                tv.setTextSize(18f);
            }
        }
    }

//    private UVCCameraHelper.OnMyDevConnectListener mUVCCameraListener = new UVCCameraHelper.OnMyDevConnectListener() {
//        // 插入USB裝置
//        @Override
//        public void onAttachDev(int index, UsbDevice device) {
//            Log.d(TAG, "onAttachDev()");
//            if (mUVCCameraHelper == null || mUVCCameraHelper.getUsbDeviceCount(mUVCDeviceFilterXmlId) == 0) {
//                //showShortMsg("未檢測到USB攝像頭裝置");
//                return;
//            }
//            // 請求開啟攝像頭
//            if (!isUVCRequesting0 && !mUVCCameraHelper.isCameraOpened(index)) {
//                Log.d(TAG, "onAttachDev() 1");
//                isUVCRequesting0 = true;
//
//                try {
//                    mUVCCameraHelper.requestPermission(device);
//                } catch (Exception ex) {
//                    isUVCRequesting0 = false;
//                }
//            }
//        }
//
//        // 拔出USB裝置
//        @Override
//        public void onDettachDev(int index, UsbDevice device) {
//            Log.d(TAG, "onDettachDev");
////            if (isUVCRequesting0) {
//            // 關閉攝像頭
//            if (mUVCCameraHelper != null && mUVCCameraHelper.isPreviewing(AppResultReceiver.uvcCameraIndex))
//                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, null, null, 3);
//            if (mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex))
//                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, null, null, 4);
//            //if (mUVCCameraHelper != null && mUVCCameraHelper.isPreviewing(AppResultReceiver.uvcCameraIndex)) mUVCCameraHelper.stopPreview(AppResultReceiver.uvcCameraIndex);
//            //if (mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex)) mUVCCameraHelper.closeCamera(AppResultReceiver.uvcCameraIndex);
////                mUVCCameraHelper.release(AppResultReceiver.uvcCameraIndex);
//            //showShortMsg(device.getDeviceName()+"已撥出");
////            }
//            isUVCRequesting0 = false;
////            isUVCPreview0 = false;
//            }
//
//        // 連線USB裝置成功
//        @Override
//        public void onConnectDev(int index, UsbDevice device, boolean isConnected) {
//            Log.d(TAG, "onConnectDev");
////            if (isConnected) {
//            //showShortMsg("連線失敗，請檢查解析度引數是否正確");
//
//            if (mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex) && !mUVCCameraHelper.isPreviewing(AppResultReceiver.uvcCameraIndex) && mUVCCameraView0.isAvailable()) {
//                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, mUVCCameraView0, null, 2);
//
////                mUVCCameraHelper.startPreview(AppResultReceiver.uvcCameraIndex, mUVCCameraView0);
////                isUVCPreview0 = true;
//            }
////            } else {
//////                isUVCPreview0 = true;
////            }
//            }
//
//        // 與USB裝置斷開連線
//        @Override
//        public void onDisConnectDev(int index, UsbDevice device) {
//            Log.d(TAG, "onDisConnectDev");
//            //showShortMsg("連線失敗");
////            if (isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex)) {
////                mUVCCameraHelper.stopPreview(AppResultReceiver.uvcCameraIndex);
////                isUVCPreview0 = false;
////            }
//
//            if (mUVCCameraHelper != null && mUVCCameraHelper.isPreviewing(AppResultReceiver.uvcCameraIndex)) {
//                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, null, null, 3);
//
////                mUVCCameraHelper.stopPreview(AppResultReceiver.uvcCameraIndex);
////                isUVCPreview0 = false;
//            }
//        }
//
//        @Override
//        public void onCancel(int index, UsbDevice device) {
//            Log.d(TAG, "onCancel");
//            mActivity.showToast("連線失敗");
//        }
//    };
//
//    private CameraViewInterface.Callback mUVCCameraCallback = new CameraViewInterface.Callback() {
//        @Override
//        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
//            if (!isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex)) {
//                mUVCCameraHelper.startPreview(AppResultReceiver.uvcCameraIndex, (com.serenegiant.usb.widget.CameraViewInterface) mUVCCameraView0);
//                isUVCPreview0 = true;
//            }
//        }
//
//        @Override
//        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
//        }
//
//        @Override
//        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
//            if (isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex)) {
//                mUVCCameraHelper.stopPreview(AppResultReceiver.uvcCameraIndex);
//                isUVCPreview0 = false;
//            }
//        }
//    };

    public synchronized void initOrResumeOrPauseOrDestroyUVCCamera(int type) {
        try {
            if (type == 0) {
                onInitUVCSurface();
            } else if (type == 1) {
                if (mUSBCameraHelper == null) {
                    TextureView mUVCCameraView = (TextureView) findViewById(R.id.uvccamera_view0);
                    mUVCCameraView.setVisibility(View.VISIBLE);
                    mIFrameCallback = new IFrameCallback() {
                        @Override
                        public void onFrame(final ByteBuffer frame, final double meta) {
                            long snapshotTimems = SystemClock.uptimeMillis();
                            AppResultReceiver.lastThermalOnFrame = true;
                            try {
                                if (AppResultReceiver.initializedFrame < 4) {
                                    AppResultReceiver.initializedFrame++;
                                    return;
                                }
                                //if (mUSBCameraHelper.semaphore != null && (mUSBCameraHelper.doCapture || AppResultReceiver.thermalFrameNo++ % 2 == 0)) {
                                if (mUSBCameraHelper.semaphore != null && (mUSBCameraHelper.doCapture || (snapshotTimems - AppResultReceiver.lastThermalSnapshotTimems) > 100)) {
                                    boolean semaphoreAcquired = false;
                                    synchronized (semaphore) {
                                        semaphoreAcquired = mUSBCameraHelper.semaphore.tryAcquire(50, TimeUnit.MILLISECONDS);
                                    }
                                    if (semaphoreAcquired) {
//                                        if (mUSBCameraHelper.semaphore.tryAcquire(50, TimeUnit.MILLISECONDS)) {
                                        AppResultReceiver.lastThermalSnapshotTimems = snapshotTimems;
                                        mUSBCameraHelper.mWorkerHandler.post(new ImageSaverFlir(frame, meta, mUSBCameraHelper, mUSBCameraHelper.saveFilePathName, mUVCCameraView));
//                                        }
                                    }
                                }
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    mUSBCameraHelper = new USBCameraHelper(mActivity, R.xml.uvcdevice_filter, mUVCCameraView, mIFrameCallback);
                    mUSBCameraHelper.register();
                }
                mUSBCameraHelper.start();
            } else if (type == 3) {
                if (mUSBCameraHelper != null) {
                    mUSBCameraHelper.unregister();
                    mUSBCameraHelper.stop();
                    mUSBCameraHelper.destory();
                    mIFrameCallback = null;
                }
                mUSBCameraHelper = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onInitUVCSurface() {
        Log.d(TAG, "onInitUVCSurface");
        if (AppResultReceiver.isUvcDevice) {
            TextureView uvcCameraView0 = (TextureView) findViewById(R.id.uvccamera_view0);

            uvcCameraView0.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // TODO Auto-generated method stub

                    if (boolean_uvccamera_view0 == true) {
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int width = displayMetrics.widthPixels;
                        int height = displayMetrics.heightPixels;

                        int x = (int) event.getX();
                        int y = (int) event.getY();

                        double touch_coordinate_x = (double) x / width;
                        double touch_coordinate_y = (double) y / height;

                        AppResultReceiver.touchPointXp = touch_coordinate_x;
                        AppResultReceiver.touchPointYp = touch_coordinate_y;


                        detection.setX(x - detection.getWidth() / 2);
                        detection.setY(y - detection.getHeight() / 2);
                        detect_dist.setX(x);
                        detect_dist.setY(y - 80);

                    }
                    return false;
                }
            });


            if (uvcCameraView0 != null) {
                uvcCameraView0.setVisibility(View.VISIBLE);
                //uvcCameraView0.setAlpha(0.5f);
                //        uvcCameraView0.setLayerType(com.serenegiant.usb.widget.UVCCameraTextureView.LAYER_TYPE_SOFTWARE, null);

                uvcCameraView0.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (boolean_uvccamera_view0 == false) {
                            if (true) {
                                try {
                                    Log.v(TAG,"boolean_uvccamera_view0 == false");
                                    AutoFitTextureView uvccamera_view0 = (AutoFitTextureView) findViewById(R.id.uvccamera_view0);
                                    button_goback.setVisibility(View.VISIBLE);
                                    button_goback.bringToFront();
                                    detection.setVisibility(View.VISIBLE);
                                    if (uvccamera_view0.getVisibility() == View.VISIBLE) {
                                        WindowManager wm = (WindowManager) mContext
                                                .getSystemService(Context.WINDOW_SERVICE);
                                        Display display = wm.getDefaultDisplay();
                                        Point size = new Point();
                                        display.getSize(size);
                                        int width = size.x;
                                        int height = size.y;
                                        TextView detect_dist = (TextView) findViewById(R.id.detect_dist);
                                        DisplayMetrics displayMetrics = new DisplayMetrics();
                                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                        detection.setX((displayMetrics.widthPixels / 2) - (detection.getWidth() / 2));
                                        detection.setY((displayMetrics.heightPixels / 2) - (detection.getHeight() / 2));
                                        detect_dist.setX((displayMetrics.widthPixels / 2) - (detection.getWidth() / 2));
                                        detect_dist.setY((displayMetrics.heightPixels / 2) - (detect_dist.getHeight() / 2) - 100);
                                        org.webrtc.SurfaceViewRenderer webrtcSurface = (org.webrtc.SurfaceViewRenderer) findViewById(R.id.glview_call);
                                        TextureView glview_color = (TextureView) findViewById(R.id.glview_color);
                                        TextureView previewDepth = (TextureView) findViewById(R.id.cdpreview);


                                        if (glview_color.getVisibility() == View.INVISIBLE) {
                                            detect_dist.setVisibility(View.VISIBLE);
                                            glview_color.setVisibility(View.VISIBLE);
                                            previewDepth.setVisibility(View.INVISIBLE);
                                            if (uvccamera_view0 != null) {
                                                Log.v(TAG,"uvccamera_view0 != null");
                                                uvccamera_view0.setTag(uvccamera_view0.getTop());
                                                //uvccamera_view0.setZ(0.1f);
                                                uvccamera_view0.configureFillTransform(120, 160, width, height);
                                                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) uvccamera_view0.getLayoutParams();
                                                if (layoutParams != null) {
                                                    Log.v(TAG,"layoutParams != null");
                                                    layoutParams.setMargins(0, 0, 0, 0);
                                                    layoutParams.width = webrtcSurface.getWidth();
                                                    layoutParams.height = webrtcSurface.getHeight();
                                                    uvccamera_view0.setLayoutParams(layoutParams);
                                                }
                                                uvccamera_view0.invalidate();

                                            }

                                            mHandler.postDelayed(new Runnable() {
                                                public void run() {
                                                    try {
                                                        AutoFitTextureView uvccamera_view0 = (AutoFitTextureView) findViewById(R.id.uvccamera_view0);
                                                        org.webrtc.SurfaceViewRenderer webrtcSurface = (org.webrtc.SurfaceViewRenderer) findViewById(R.id.glview_call);
                                                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) uvccamera_view0.getLayoutParams();

                                                        if (layoutParams != null) {
                                                            layoutParams.setMargins(0, 0, 0, 0);
                                                            layoutParams.width = webrtcSurface.getWidth();
                                                            layoutParams.height = webrtcSurface.getHeight();
                                                            uvccamera_view0.setLayoutParams(layoutParams);
                                                            uvccamera_view0.configureFillTransform(120, 160, width, height);
                                                            uvccamera_view0.invalidate();
                                                        }
                                                    } catch (Exception ex) {
                                                    }
                                                }
                                            }, 1);
//                                        webrtcSurface.setLeft(900);
                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            boolean_uvccamera_view0 = true;
                        }
//                        try {
//                        AppResultReceiver.isShowLogoutHint = false;
//                            Intent intent = new Intent(MainActivity.this, UVCViewActivity.class);
//                            intent.putExtra("iip", iip);
//                            startActivityForResult(intent, 0);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    }
                });
                mUVCCameraView0 = uvcCameraView0;
//                mUVCCameraView0 = (CameraViewInterface) findViewById(R.id.uvccamera_view0);
//                mUVCCameraView0.setCallback(mUVCCameraCallback);
            }
        }
    }
//
//    public void onInitUVCCamera() {
//        Log.d(TAG, "onInitUVCCamera");
////        if (AppResultReceiver.isUvcDevice) {
////            // 初始化引擎
////            mUVCDeviceFilterXmlId = R.xml.uvcdevice_filter;
////
////            if (mUVCCameraHelper == null) {
////                UVCCamera.DEFAULT_BANDWIDTH = 0.9f;
////                mUVCCameraHelper = new UVCCameraHelper();
////                mUVCCameraHelper.initUSBMonitor(mActivity, mUVCCameraListener, mUVCDeviceFilterXmlId);
////                //mUVCCameraHelper.setCameraView(AppResultReceiver.uvcCameraIndex, mUVCCameraView0, 160, 120, 9, UVCCamera.PIXEL_FORMAT_YUV420SP);
////                mUVCCameraHelper.setCameraView(AppResultReceiver.uvcCameraIndex, mUVCCameraView0, 160, 120, 9, UVCCamera.PIXEL_FORMAT_Y16);
//////                // 註冊USB事件廣播監聽器
//////                mUVCCameraHelper.registerUSB();
////                mUVCCameraView0.onResume();
////        }
//            }
//
////    public void onStartUVCCamera() {
////        if (AppResultReceiver.isUvcDevice) {
////            onInitUVCCamera();
////            if (mUVCCameraView0 != null) {
//////                mUVCCameraView0.onResume();
////            }
////        }
////    }
//
//    public void onResumeUVCCamera() {
//        Log.d(TAG, "onResumeUVCCamera");
////        if (AppResultReceiver.isUvcDevice) {
////            if (mUVCCameraView0 != null && mUVCCameraHelper != null) {
////
//////                if (isUVCPreview0 != true && mUVCCameraView0.isAvailable()) {
//////                    mUVCCameraHelper.startPreview(AppResultReceiver.uvcCameraIndex, mUVCCameraView0);
//////                    isUVCPreview0 = true;
//////                }
////
////                mUVCCameraView0.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
////                    @Override
////                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
////                        Log.d(TAG, "onSurfaceTextureAvailable");
////                        if (AppResultReceiver.isUvcDevice) {
////                            if (mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex) && !mUVCCameraHelper.isPreviewing(AppResultReceiver.uvcCameraIndex) && mUVCCameraView0.isAvailable()) {
////                                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, mUVCCameraView0, null, 2);
////
//////                                mUVCCameraHelper.startPreview(AppResultReceiver.uvcCameraIndex, mUVCCameraView0);
//////                                isUVCPreview0 = true;
////                            }
////                        }
////                    }
////
////                    @Override
////                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
////                        Log.d(TAG, "onSurfaceTextureSizeChanged");
////                        if (AppResultReceiver.isUvcDevice) {
////                            if (mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex) && !mUVCCameraHelper.isPreviewing(AppResultReceiver.uvcCameraIndex) && mUVCCameraView0.isAvailable()) {
////                                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, mUVCCameraView0, null, 2);
//////                                mUVCCameraHelper.startPreview(AppResultReceiver.uvcCameraIndex, mUVCCameraView0);
//////                                isUVCPreview0 = true;
////                            }
////                        }
////                    }
////
////                    @Override
////                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
////                        Log.d(TAG, "onSurfaceTextureDestroyed");
////                        if (AppResultReceiver.isUvcDevice) {
////                            if (mUVCCameraHelper != null && mUVCCameraHelper.isPreviewing(AppResultReceiver.uvcCameraIndex)) {
////                                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, null, null, 3);
////
//////                                mUVCCameraHelper.stopPreview(AppResultReceiver.uvcCameraIndex);
//////                                isUVCPreview0 = false;
////                            }
////                        }
////                        return false;
////                    }
////
////                    @Override
////                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
////                    }
////                });
////
////
////                if (mUVCCameraHelper != null) mUVCCameraHelper.registerUSB();
////            }
////        }
//        }
//
//    public void onPauseUVCCamera() {
//        Log.d(TAG, "onPauseUVCCamera");
////        if (AppResultReceiver.isUvcDevice) {
////            if (mUVCCameraHelper != null && mUVCCameraHelper.isPreviewing(AppResultReceiver.uvcCameraIndex))  mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, null, null, 3);
////            if (mUVCCameraHelper != null)  mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, null, null, 4);
////
////            if (mUVCCameraHelper != null) {
////                mUVCCameraHelper.unregisterUSB();
////            }
////            if (mUVCCameraView0 != null) mUVCCameraView0.setSurfaceTextureListener(null);
////
////            if (mUVCCameraHelper != null) {
////                mUVCCameraHelper.release(AppResultReceiver.uvcCameraIndex);
////            }
////            //mUVCCameraHelper.stopPreview(AppResultReceiver.uvcCameraIndex);
//////                isUVCPreview0 = false;
////
////        }
//    }
//
////    protected void onStopUVCCamera() {
////        if (AppResultReceiver.isUvcDevice) {
////            if (mUVCCameraView0 != null) {
//////                mUVCCameraView0.onPause();
////            }
////        }
////    }
//
//    public void onDestroyUVCCamera() {
//        Log.d(TAG, "onDestroyUVCCamera");
////        if (AppResultReceiver.isUvcDevice) {
////            if (mUVCCameraHelper != null) {
//////                mUVCCameraHelper.unregisterUSB();
//////                try {
//////                    Thread.sleep(100);
//////                } catch (InterruptedException e) {
//////                    e.printStackTrace();
//////                }
//////                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, null, null, 4);
////
////                //mUVCCameraHelper.closeCamera(AppResultReceiver.uvcCameraIndex);
//////                try {
//////                    Thread.sleep(100);
//////                } catch (InterruptedException e) {
//////                    e.printStackTrace();
//////                }
////
//////                mUVCCameraHelper.release(AppResultReceiver.uvcCameraIndex);
//////                try {
//////                    Thread.sleep(10);
//////                } catch (InterruptedException e) {
//////                    e.printStackTrace();
//////                }
////            }
//////            isUVCPreview0 = false;
////            isUVCRequesting0 = false;
//////            mUVCCameraView0.setCallback(null);
//////        try {
//////            Thread.sleep(100);
//////        } catch (Exception ex) {
//////        }
////            mUVCCameraHelper = null;
////        }
//                }

    //        } catch (Exception ex) {
    public JobManager configureJobQueueManager() {
        //3. JobManager的配置器，利用Builder模式
        com.birbit.android.jobqueue.config.Configuration configuration = new com.birbit.android.jobqueue.config.Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBU";

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
                .maxConsumerCount(5)//up to 10 consumers at a time
                .loadFactor(1)//1 jobs per consumer
                .consumerKeepAlive(600)//wait 10 minute
                .consumerThreadPriority(Thread.NORM_PRIORITY)
                .build();
        jobManagerUrgent = new JobManager(configuration);

        return jobManagerUrgent;
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
                .maxConsumerCount(1)//up to 2 consumers at a time
                .loadFactor(1)//1 jobs per consumer
                .consumerKeepAlive(10)//wait 0.1 minute
                .consumerThreadPriority(Thread.NORM_PRIORITY - 1)
                .build();
        jobManagerRelax = new JobManager(configuration);
        return jobManagerRelax;
    }

    public JobManager configureJobQueueManagerMarker() {
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
                .maxConsumerCount(1)//up to 2 consumers at a time
                .loadFactor(1)//1 jobs per consumer
                .consumerKeepAlive(10)//wait seconds
                .consumerThreadPriority(Thread.NORM_PRIORITY - 1)
                .build();
        jobManagerMarker = new JobManager(configuration);
        return jobManagerMarker;
    }

    @NonNull
    protected synchronized void initLiteHttp() {
        if (mLiteHttp == null) {
//            mLiteHttp = LiteHttp.build(mContext)
//                    .setHttpClient(new HttpUrlClient())       // http client
//                    .setContext(mContext)
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
            mLiteHttp = (new XSslLiteHttp(mContext, 5, 360)).getLiteHttp();

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

    private Emitter.Listener onConnectTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "socket.io " + "onConnectTimeout");
        }
    };
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "socket.io " + "onConnectError");
        }
    };
    private Emitter.Listener onConnecting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "socket.io " + "onConnecting");
        }
    };
    private Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "socket.io " + "onConnected");
        }
    };
    private Emitter.Listener onDisconnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "socket.io " + "onDisconnected");
        }
    };
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "socket.io " + "onNewMessage");
            //主線程調用
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = "";
                    Boolean reply = false;
                    try {
                        org.json.simple.parser.JSONParser jParser = new JSONParser();

                        Object obj = jParser.parse((String) args[0]);
                        if (AppResultReceiver.DEMO_WEBRTC) {
                            message = (String) ((org.json.simple.JSONObject) obj).get("msg");
                            if (StringUtils.isNotBlank(message))
                                addMessage(message);
                        } else {
                            String ownid = (String) ((org.json.simple.JSONObject) obj).get("own");
                            String ownid_spilt[]=ownid.split("_");
                            String socket_userId = (String) ((org.json.simple.JSONObject) obj).get("userId");
                            Log.v(TAG,"userId"+userId);
                            Log.v(TAG,"socket_userId"+socket_userId);
                            if(socket_userId!=null&&socket_userId.equals(userId)){//檢查USERID是否一致
                                if (ownid != null&&ownid_spilt[0].equals(AppResultReceiver.ZONE)) {

                                    message = (String) ((org.json.simple.JSONObject) obj).get("msg");

                                    addMessage(message);
                                    showDialog(getString(R.string.recommend),message,3);

                                } else {
                                    message = (String) ((org.json.simple.JSONObject) obj).get("msg");
                                    if (StringUtils.isNotBlank(message))
                                        addMessage(">>>>>" + message);
                                }

                            }


                        }





                    } catch (Exception e) {

                    }
                }
            });
        }
    };

    private void startWebsocket() {
        try {
//            HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
//                @Override
//                public boolean verify(String hostname, SSLSession session) {
//                    return true;
//                }
//            };
//            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                }
//
//                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                }
//
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[0];
//                }
//            }};
//
//            SSLContext mySSLContext = null;
//            try {
//                mySSLContext = SSLContext.getInstance("TLS");
//                try {
//                    mySSLContext.init(null, trustAllCerts, null);
//                } catch (KeyManagementException e) {
//                    e.printStackTrace();
//                }
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            }
//            OkHttpClient okHttpClient = new OkHttpClient.Builder().hostnameVerifier(myHostnameVerifier).sslSocketFactory(mySSLContext.getSocketFactory()).build();

            OkHttpClient okHttpClient = (new XSslOkHttpClient(10, 120)).getOkHttpClient();

// default settings for all sockets
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            IO.setDefaultOkHttpCallFactory(okHttpClient);

            // set as an option
            IO.Options opts = new IO.Options();
            opts.callFactory = okHttpClient;
            opts.webSocketFactory = okHttpClient;
            opts.reconnectionDelay = 10000;
            Log.v("lag", AppResultReceiver.WEBSOCKET_URL);
            mSocketIO = IO.socket(AppResultReceiver.WEBSOCKET_URL, opts);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            //請確認 web page有登入, 才能收到訊息
            //need login web system to reciving data
            mSocketIO.on("respMsg", onNewMessage);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.on(io.socket.client.Socket.EVENT_MESSAGE, onNewMessage);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.on(io.socket.client.Socket.EVENT_DISCONNECT, onDisconnected);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.on(io.socket.client.Socket.EVENT_CONNECT, onConnected);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.on(io.socket.client.Socket.EVENT_CONNECTING, onConnecting);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.on(io.socket.client.Socket.EVENT_CONNECTING, onConnecting);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.on(io.socket.client.Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeout);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.on(io.socket.client.Socket.EVENT_CONNECT_ERROR, onConnectError);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {

            mSocketIO = mSocketIO.connect();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void stopWebsocket() {
        try {
            mSocketIO.disconnect();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        try {
            mSocketIO.off("respMsg", onNewMessage);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.off(io.socket.client.Socket.EVENT_CONNECT, onConnected);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.off(io.socket.client.Socket.EVENT_DISCONNECT, onDisconnected);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.off(io.socket.client.Socket.EVENT_RECONNECTING, onConnecting);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        try {
            mSocketIO.off(io.socket.client.Socket.EVENT_CONNECTING, onConnecting);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.off(io.socket.client.Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeout);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        try {
            mSocketIO.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void addMessage(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //showDialog(getString(R.string.alert_title), txt, 0);
                    addNotification(mActivity, 0, txt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void addNotification(Context context, int nid, String text) {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);//NotificationManager需使用getSystemService取得實例
        Notification.Builder builder = null;
        Intent notifyIntent = new Intent(context, MainActivity.class);   //Intent所需切換的Activity
        notifyIntent.putExtra("goWhere", "caseMgnt");
        //Intent旗標參數：
        // Intent.FLAG_ACTIVITY_NEW_TASK：在堆疊中開啟一個新的任務
        // Intent.FLAG_ACTIVITY_SINGLE_TOP：將Activity顯示在最上層
        // Intent.FLAG_ACTIVITY_CLEAR_TOP：當前Activity會被新的Intent覆蓋
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT); //PendingIntent之設定

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher); //setLargeIcon時使用

        Notification.BigTextStyle bigStyle = new Notification.BigTextStyle(); //建立一個BigTextStyle的物件，並且設定其文章的內容。
        bigStyle.bigText(text);

        long[] vibratePattern = {400, 500}; //setVibrate範圍時使用

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, "channel01"); //Notification建構器
            NotificationChannel channel = new NotificationChannel("channel01", "myChannel", NotificationManager.IMPORTANCE_HIGH);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            channel.setVibrationPattern(vibratePattern);//振動頻率
            notificationManager.createNotificationChannel(channel);
            builder.setSubText("");
        } else {
            builder = new Notification.Builder(context); //Notification建構器
            builder.setContentInfo("")
                    .setVibrate(vibratePattern)
                    .setPriority(Notification.PRIORITY_HIGH);//可將訊息嵌板直接顯示在TOP，而不需下拉選單
        }

        builder.setContentText(text)
                .setStyle(bigStyle)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker")
                .setAutoCancel(true)//點擊訊息嵌板時，是否自動取消訊息嵌板?
                .setContentTitle(getString(R.string.doctor_note))
                .setLargeIcon(bmp)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        Random random = new Random();
        int uId = random.nextInt(9999 - 1000) + 1000;
        Notification samNotify = builder.build();
        notificationManager.notify(uId, samNotify);
    }

    public Uri getOutputMediaFileUri() {
        return outputMediaFileUri;
    }

    public String getOutputMediaFileType() {
        return outputMediaFileType;
    }

    public File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            outputMediaFileType = "image/*";
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
            outputMediaFileType = "video/*";
        } else {
            return null;
        }
        outputMediaFileUri = Uri.fromFile(mediaFile);
        return mediaFile;
    }

    public void returnFromSetting() {
        rtcClient.onPause();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AppResultReceiver.initOrientation(AppResultReceiver.mMainActivity);
                rtcClient.onResume();
            }
        }, 100);
    }

    public void setThermalBoardParams() {
        jobManagerUrgent.addJobInBackground(new JobQueueSetParamsJob(jobManagerUrgent, mActivity, iip));
    }

    public void setShutBtnParams() {
        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mActivity);
            String ret = prefs.getString(SettingsFragment.KEY_PREF_SHUT_BTN, "c");
            if (ret.equals("c")) {
                button_takePicL.setVisibility(View.INVISIBLE);
                button_takePicR.setVisibility(View.INVISIBLE);
                button_takePic.setVisibility(View.VISIBLE);
            } else if (ret.equals("l")) {
                button_takePic.setVisibility(View.INVISIBLE);
                button_takePicR.setVisibility(View.INVISIBLE);
                button_takePicL.setVisibility(View.VISIBLE);
            } else {
                button_takePic.setVisibility(View.INVISIBLE);
                button_takePicL.setVisibility(View.INVISIBLE);
                button_takePicR.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
        }
    }

    //熱感裝置按鈕直接驅動自動拍照功能
    public void autoTakePic() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.v(TAG, "目前count = " + String.valueOf(count));
                    button_takePic.performClick();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void setPreviewPic(String outputMediaFileUri) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!AppResultReceiver.dataEncrypt) {
                    try {
                        ImageView mediaPreview = (ImageView) findViewById(R.id.media_preview);
                        try {
                            ((BitmapDrawable) mediaPreview.getDrawable()).getBitmap().recycle();
                        } catch (Exception e) {

                        }
                        Bitmap bitmap = BitmapHelper.getFitSampleBitmap(outputMediaFileUri,
                                mediaPreview.getWidth(), mediaPreview.getHeight());
                        mediaPreview.setImageBitmap(bitmap);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        Mat img = FileHelper.imreadSecret(outputMediaFileUri);
//                        FileInputStream ins = new FileInputStream(outputMediaFileUri);
//                        byte[] targetArray = IOUtils.toByteArray(ins);
//                        if (outputMediaFileUri.toLowerCase().endsWith(".jpg")) {
//                            if (targetArray[0xb1] == 0x10) {
//                                String chs = ServiceHelper.getSerialNumber("0");
//                                int chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
//                                if (targetArray[0xb2] == (byte) chn) {
//                                    targetArray[0xb1] = (byte) 0xff;
//                                    targetArray[0xb2] = (byte) 0xc4;
//                                }
//                            }
//                        } else if (outputMediaFileUri.toLowerCase().endsWith(".png")) {
//                            if (targetArray[1] == 0x10) {
//                                String chs = ServiceHelper.getSerialNumber("0");
//                                int chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
//                                if (targetArray[2] == (byte) chn) {
//                                    targetArray[1] = 0x50;
//                                    targetArray[2] = (byte) 0xc4;
//                                }
//                            }
//                        }
                        ImageView mediaPreview = (ImageView) findViewById(R.id.media_preview);
                        try {
                            ((BitmapDrawable) mediaPreview.getDrawable()).getBitmap().recycle();
                        } catch (Exception e) {

                        }
//                        Mat img = Imgcodecs.imdecode(new MatOfByte(targetArray), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
                        Mat mmm = new Mat();
                        Imgproc.resize(img, mmm, new org.opencv.core.Size(mediaPreview.getHeight(), mediaPreview.getWidth()), 0, 0, Imgproc.INTER_LINEAR);

                        Imgproc.cvtColor(mmm, mmm, Imgproc.COLOR_BGRA2RGBA);
                        Bitmap bitmap = Bitmap.createBitmap(mediaPreview.getHeight(), mediaPreview.getWidth(), Bitmap.Config.ARGB_8888);
                        org.opencv.android.Utils.matToBitmap(mmm, bitmap);
                        mmm.release();
                        img.release();
                        mediaPreview.setImageBitmap(bitmap);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void setPreviewPic(final Mat img, boolean convertBGR) {

        final Mat mat = img.clone();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ImageView mediaPreview = (ImageView) findViewById(R.id.media_preview);
                    Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                    try {
                        if (convertBGR)
                            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2RGBA);
                        Utils.matToBitmap(mat, bitmap);
                        mediaPreview.setImageBitmap(bitmap);
                    } catch (Exception e) {

                    }
                    //bitmap.recycle();
                    mat.release();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    //開始與關閉閃光功能之圖片更換
    public void changeLightImg(boolean isOpen) {
        if (isOpen) {
            menuImageButton_light.setCompoundDrawables(null, mContext.getDrawable(R.mipmap.color_light_48), null, null);
        } else {
            menuImageButton_light.setCompoundDrawables(null, mContext.getDrawable(R.mipmap.light_48), null, null);
        }


    }

    private void detectOrientation(int orientation) {
        // 檢查目前手機畫面為橫向或直向
        //上排功能
        //int menu = R.id.menuImageButton;
        int logout = R.id.menuImageButton_logout;
        int webView = R.id.menuImageButton_WebView;
        int cam = R.id.menuImageButton_cam;
        int list = R.id.menuImageButton_list;
        //下排功能
        int light = R.id.menuImageButton_light;
        int marker = R.id.menuImageButton_marker;
        int help = R.id.menuImageButton_help;
        int upload = R.id.menuImageButton_upload;

        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            //land_BtnView(light, renew);
            land_BtnView(help, light);
            land_BtnView(marker, help);
            land_BtnView(upload, marker);
            //land_BtnView(grabcut, cam);
        } else if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            potrait_BtnView(light, 0, logout);
            potrait_BtnView(help, light, webView);
            potrait_BtnView(marker, help, cam);
            potrait_BtnView(upload, marker, list);
            // potrait_BtnView(grabcut, cam, renew);
        }
    }

    private void land_BtnView(int replaceId, int targetId) {
        Button targetButton = findViewById(replaceId);
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(targetButton.getLayoutParams());
        marginParams.setMargins(0, 0, 0, 0);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(marginParams);
        params.addRule(RelativeLayout.START_OF, targetId);
        targetButton.setLayoutParams(params);
    }

    private void potrait_BtnView(int replaceId, int startId, int targetId) {
        Button targetButton = findViewById(replaceId);
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(targetButton.getLayoutParams());
        final float scale = getBaseContext().getResources().getDisplayMetrics().density;
        int topPixel = (int) (5 * scale + 0.5f);
        marginParams.setMargins(0, topPixel, 0, 0);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(marginParams);

        if (startId != 0) {
            params.addRule(RelativeLayout.START_OF, startId);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
        }
        params.addRule(RelativeLayout.BELOW, targetId);
        targetButton.setLayoutParams(params);
    }

    public synchronized int getNextCountValue() {
        if (count < 0) {
            count = 0;
        }
        this.count++;
        return count;
    }

    void displayProgressDialog(String msg) {
        indeterminateDialog = new ProgressDialog(this);
        indeterminateDialog.setTitle(getString(R.string.please_wait));
        indeterminateDialog.setMessage(msg);
        indeterminateDialog.setCancelable(false);
        indeterminateDialog.show();
    }

    public interface DialogCallback {
        public void positiveCallback(Object obj);
        public void negativeCallback(Object obj);
    }

    public class DialogCallbackAdapter implements DialogCallback {
        @Override
        public void positiveCallback(Object obj) {

        }

        @Override
        public void negativeCallback(Object obj) {

        }
    }

    public void showDialogWithCallback(final String title, final String message, final int type, DialogCallback dialogCallback) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(title);
                    builder.setMessage(message);
                    builder.setIcon(R.mipmap.color_light_48);
                    //按到旁邊的空白處AlertDialog也不會消失
                    builder.setCancelable(false);

                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (type == 1) {
                                isShowLogoutHint = false;
                                finish();
                            } else if (type == 2) {
                                isShowLogoutHint = false;
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (type == 3) {
                                dialog.cancel();
                            } else {
                                dialog.cancel();
                                (new Thread() {
                                    @Override
                                    public void run() {
                                        dialogCallback.positiveCallback(null);
                                    }
                                }).start();
                            }
                        }
                    });

                    if (type == 1) {
                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                (new Thread() {
                                    @Override
                                    public void run() {
                                        dialogCallback.negativeCallback(null);
                                    }
                                }).start();
                            }
                        });
                    }

                    builder.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void showDialog(final String title, final String message, final int type) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(title);
                    builder.setMessage(message);
                    builder.setIcon(R.mipmap.color_light_48);
                    //按到旁邊的空白處AlertDialog也不會消失
                    builder.setCancelable(false);

                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (type == 1) {
                                isShowLogoutHint = false;
                                finish();
                            } else if (type == 2) {
                                isShowLogoutHint = false;
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (type == 3) {
                                dialog.cancel();
                            } else{
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

                    builder.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    //修改Toast內容
    public void showToast(String context) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Toast toast = Toast.makeText(MainActivity.this, context, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 250);
                    toast.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

//    //寫入文檔
//    public void writeToFile(String outFilename, String msg, boolean append) {
//        BufferedWriter writer = null;
//        try {
//            //建立檔名
//            File textFile = new File(outFilename);
//            writer = new BufferedWriter(new FileWriter(textFile, append));
//            writer.write(msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                // 關閉BufferedWriter
//                writer.close();
//            } catch (Exception e) {
//            }
//        }
//    }

    //讀取對應的txt檔內容
    public void readtxt(String pathname) {
        try {
            String line = null;
            int n = 3;//从第三行开始读取
            BufferedReader br = new BufferedReader(new FileReader(pathname));

            while (n-- > 1) {
                br.readLine();
            }
            while ((line = br.readLine()) != null) {
                Log.i(TAG, "每行內容為: " + line);
                myList.add(line);
            }
            br.close();


        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void checkPermissions() {
        Date date = new Date();
        if (Build.MODEL != "" || Build.VERSION.SDK_INT != Build.VERSION_CODES.KITKAT) {
//            showToast("Incorrect device");
//            finish();
        }

        //Checks all given permissions have been granted.
        String[] RequiredPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}; //權限允許(照相功能與錄音功能)
        permissionChecker = new PermissionChecker();
        permissionChecker.verifyPermissions(this, RequiredPermissions, new PermissionChecker.VerifyPermissionsCallback() {

            @Override
            public void onPermissionAllGranted() {

            }

            @Override
            public void onPermissionDeny(String[] permissions) {
                showToast(getString(R.string.confirm_permission));
                //Log.i("步驟permissions", permissions[0]);
            }
        });
    }

    public void appVersionCheck() {
        //版本檢查更新
        AnyVersion version = AnyVersion.getInstance();
        version.setURL(AppResultReceiver.VERSION_CHECK_URL);
        version.setDownloadingCallback(new DownloadingCallback() {
            @Override
            public void onDownloading(final long downloadId) {
                final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                final ProgressDialog indeterminateDialog = new ProgressDialog(mContext);
                indeterminateDialog.setTitle("Downloading");
                indeterminateDialog.setMessage("0%");
                indeterminateDialog.setCancelable(false);
                indeterminateDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean downloading = true;
                        while (downloading) {
                            DownloadManager.Query q = new DownloadManager.Query();
                            q.setFilterById(downloadId);

                            Cursor cursor = manager.query(q);
                            cursor.moveToFirst();
                            int bytes_downloaded = cursor.getInt(cursor
                                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                downloading = false;
                            }
                            String str_progress = "";
                            int dl_progress = 0;
                            if (bytes_total > 0) {
                                dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                                str_progress = dl_progress + "%";
                            } else {
                                try {
                                    dl_progress = (int) bytes_downloaded / 1048576;
                                    str_progress = Math.round((double) dl_progress / (double) Math.max(40, dl_progress) * 100) + "% ( " + dl_progress + " MB )";
                                } catch (Exception ee) {
                                    try {
                                        dl_progress = (int) bytes_downloaded / 1048576;
                                        str_progress = dl_progress + " MB";
                                    } catch (Exception eee) {

                                    }
                                }
                            }

                            final String finalStr_progress = str_progress;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        indeterminateDialog.setMessage(finalStr_progress);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                }
                            });
//                            Log.d(Constants.MAIN_VIEW_ACTIVITY, statusMessage(cursor));
                            cursor.close();
                        }
                        indeterminateDialog.dismiss();
                    }
                }).start();
            }
        });
        version.check(NotifyStyle.Dialog);
    }

    public void generateFileDBs() {
//        if (DocumentsUtils.checkWritableRootPath(mActivity,AppResultReceiver.getFileRootPath(mActivity) )) {
//            showOpenDocumentTree();
//        }

        File mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
        if (!mainDir.mkdirs()) {
            //Log.e("", "無法建立目錄");
        }

        File backup = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.BackUp_DIR);
        if (!backup.mkdirs()) {
            //Log.e("", "無法建立目錄");
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
        if (!file.mkdirs()) {
            Log.e("", "無法建立目錄");
        }


        //顯示測距
        detect_dist = (TextView) findViewById(R.id.detect_dist);
        file_time = (TextView) findViewById(R.id.file_time);
        nonupload_filesize = (TextView) findViewById(R.id.nonupload_filesize);
        if (file.exists()) {
            filesize(file.getAbsolutePath());
        } else {
            setNonUploadFileSize(getString(R.string.nonupload_file_count_) + fileCount + getString(R.string._count));
        }

        try {
            final Intent intent = getIntent();  //代表使用者與應用程式的互動
            final String action = intent.getAction();   //啟動狀態(決定應用程式最先啟動的Activity)
            if (Intent.ACTION_VIEW.equals(action)) {
                final List<String> segments = intent.getData().getPathSegments();
                callerId = segments.get(0);
                Log.i(TAG, "呼叫CallId為: " + callerId);
            }

            userName = (TextView) findViewById(R.id.userName);
            userId = intent.getStringExtra("userId");
            roleId = intent.getStringExtra("roleId");
            single_upload_id = userId;

            setUserName(intent.getStringExtra("userName"));

        } catch (Exception ex) {
        }

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<Map<String, Object>> listMeasure = null;

                    if (dataEncrypt == false) {
                        listMeasure = database.querySQLData("loginInfo", null, "", new String[]{}, "");

                    } else {
                        listMeasure = sqllitesecret.querySQLData(Sercretdb, "loginInfo", null, "", new String[]{}, "");
                    }

                    if (listMeasure.size() > 0) {
                        for (Map<String, Object> map : listMeasure) {
                            loginPeriod = (Integer) map.get("period");
                            loginDate = (String) map.get("evalDate");

                        }
                    }

                }
            });
        } catch (Exception ex) {
        }
        Log.v(TAG,"loginDate"+loginDate);
        Log.v(TAG,"loginPeriod"+loginPeriod);
    }

    public void generateButtons() {
        //在右下角preview顯示最新的照片圖像
        File newestFile = getNewestFilefromDir(file.getAbsolutePath(), "jpg.jpg");
        if (newestFile != null) {
            AppResultReceiver.lastColorJpegPath = newestFile.getAbsolutePath();
            setPreviewPic(AppResultReceiver.lastColorJpegPath);
        }
        //拍攝照片預覽畫面
        mediaPreview = (ImageView) findViewById(R.id.media_preview);
        mediaPreview.setVisibility(View.VISIBLE);
        mediaPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                Long lastClickTime = (Long) AppResultReceiver.lastUIClickTime.get("mediaPreview");
                if (lastClickTime != null && SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                }
                AppResultReceiver.lastUIClickTime.put("mediaPreview", SystemClock.elapsedRealtime());

                AppResultReceiver.vibrating(mActivity);
                isShowLogoutHint = false;
                Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        //輸入病患編號按鈕
        button_ownerId = (Button) findViewById(R.id.button_ownerId);
//        button_ownerId.setDrawingCacheEnabled(true);
        button_ownerId.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                AppResultReceiver.vibrating(mActivity);
                openPatientNoDialog();

//                InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        button_takePic = (Button) findViewById(R.id.takePic);
        button_takePic.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {


                DialogProgress.showProgressDialog(MainActivity.this);                if (AppResultReceiver.isTakingPicture)
                    return;

                AppResultReceiver.vibrating(mActivity);
                AppResultReceiver.isTakingPicture = true;

                AppResultReceiver.lastColorOnFrame = false;
                AppResultReceiver.lastDepthOnFrame = false;
                AppResultReceiver.lastThermalOnFrame = false;

                if (evlId.isEmpty()||ownerId=="") {
                    evlId = nowEvalTime.format(new Date());
                    setFileTime(evlId);
                }

                if (AppResultReceiver.FOCUS_AREA_TYPE.equals("point")) {
                    AppResultReceiver.lastPicFocusXScale = AppResultReceiver.touchFocusXScale;
                    AppResultReceiver.lastPicFocusYScale = AppResultReceiver.touchFocusYScale;
                } else {
//                Display display = ((WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//                int viewWidth = display.getWidth();//getWidth();
//                int viewHeight = display.getHeight(); //getHeight();
                    AppResultReceiver.lastPicFocusXScale = 0.5;
                    AppResultReceiver.lastPicFocusYScale = 0.5;
                }

                mediaPreview = (ImageView) findViewById(R.id.media_preview);
//                mediaPreview.setDrawingCacheEnabled(true);
                mediaPreview.setVisibility(View.VISIBLE);

//                if (AppResultReceiver.isUvcDevice && mUVCCameraHelper.isCameraOpened(0))
//                    rtcClient.takePicture(mediaPreview, mUVCCameraHelper, 0, photographer);
//                else
                rtcClient.takePicture(mediaPreview, mUSBCameraHelper, AppResultReceiver.uvcCameraIndex, photographer);
                //Log.d(TAG, "執行拍照動作");

                new CountDownTimer(3100, 3100) {

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        DialogProgress.dismiss();
                    }

                }.start();


            }
        });

        //選擇傷口部位按鈕
        button_bodyPart = (Button) findViewById(R.id.button_bodyPart);
//        button_bodyPart.setDrawingCacheEnabled(true);
        button_bodyPart.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                AppResultReceiver.vibrating(mActivity);
                if (evlId.isEmpty()) {
                    evlId = nowEvalTime.format(new Date());
                }

                setFileTime(evlId);
                isShowLogoutHint = false;
                Intent intent = new Intent(MainActivity.this, BodyPartActivity.class);
                intent.putExtra("bodyPart", part);
                intent.putExtra("where", "main");
                startActivityForResult(intent, 0);
                //openBodyPartDialog(getString(R.string.please_choose_part));
                //openPartMenu();
            }
        });

        //前往功能設置選單
        /*menuImageButton = (Button) findViewById(R.id.menuImageButton);
        menuImageButton.setBackgroundColor(Color.argb(63, 63, 63, 63));
        menuImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AppResultReceiver.vibrating(mActivity);
                openOptionsMenuT();
            }
        });*/

        //使用者登出
        menuImageButton_logout = (Button) findViewById(R.id.menuImageButton_logout);
        menuImageButton_logout.setBackgroundColor(Color.argb(63, 63, 63, 63));
        menuImageButton_logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AppResultReceiver.vibrating(mActivity);
                openLogoutMenu();
            }
        });

        menuImageButton_logout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (userId.equals("2")) { //if demo/demo login
                    AppResultReceiver.vibrating(mActivity);
                    openOptionsMenuT();
                }
                return false;
            }
        });

        //前往個案管理畫面
        menuImageButton_webView = (Button) findViewById(R.id.menuImageButton_WebView);
        menuImageButton_webView.setBackgroundColor(Color.argb(63, 63, 63, 63));
        menuImageButton_webView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

                if (mNetworkInfo == null) {

                    showToast(getString(R.string.open_internet_or_wifi));
                    return;

                } else {
                    if (Integer.parseInt(userId) != 2) {

                        checkAccountCorrecct();
                        Log.v(TAG,"isAccountCorrecct"+isAccountCorrecct);
                        if(isAccountCorrecct==true){
                            AppResultReceiver.vibrating(mActivity);

                            Intent intent = new Intent(MainActivity.this, CaseMgntActivity.class);
                            intent.putExtra("patientNo", ownerId);
                            intent.putExtra("roleId", roleId);
                            intent.putExtra("patientNoList", patientArr.toString());
                            isShowLogoutHint = false;
                            startActivityForResult(intent, 0);
                        } else {
                            showDialog(getString(R.string.remind_title), getString(R.string.no_account_authority_to_use), 3);
                        }
                    }else {
                        showDialog(getString(R.string.remind_title), getString(R.string.no_authority_to_use), 3);
                    }

                }
            }
        });

//開啟或關閉視訊連線功能
        menuImageButton_cam = (Button) findViewById(R.id.menuImageButton_cam);
        menuImageButton_cam.setBackgroundColor(Color.argb(63, 63, 63, 63));
        menuImageButton_cam.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AppResultReceiver.vibrating(mActivity);

                ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                if (mNetworkInfo == null) {
                    showToast(getString(R.string.open_internet_or_wifi));
                    return;
                }

                if (AppResultReceiver.DEBUG_LEVEL < AppResultReceiver.DEBUG_APPRTC && StringUtils.isBlank(ownerId)) {
                    showToast(getString(R.string.set_owner_first));
                    return;
                }

                if (!isWebRtcConnected) {


                    if (AppResultReceiver.DEMO_WEBRTC)
                        rtcClient.EXTRA_ROOMID = AppResultReceiver.WEBRTC_ROOMID;
                    else if (AppResultReceiver.DEBUG_LEVEL >= AppResultReceiver.DEBUG_APPRTC) {
                        rtcClient.EXTRA_ROOMURL = "https://appr.tc";
                        rtcClient.EXTRA_ROOMID = AppResultReceiver.DEBUG_TAG;
                    } else {
                        if (ownerId.equals("")) {
                            rtcClient.EXTRA_ROOMID = AppResultReceiver.ZONE + "_" + AppResultReceiver.WEBRTC_ROOMID;
                        } else {
                            rtcClient.EXTRA_ROOMID = AppResultReceiver.ZONE + "_" + ownerId;
                        }
                    }

                    SimpleDateFormat nowTime = new SimpleDateFormat("HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());
                    String str = nowTime.format(curDate);

                    rtcClient.startCall(str);

                    if (mSocketIO.connected()) {
                        if (ownerId.equals("")) {
                            mSocketIO.emit("measureValue", "{\"msg\":\"" + AppResultReceiver.WEBRTC_ROOMID + "\",\"zone\":\"" + AppResultReceiver.ZONE + "\",\"own\":\"" + AppResultReceiver.ZONE + "_" + AppResultReceiver.WEBRTC_ROOMID + "\",\"type\":\"v\"}");
                        } else {
                            mSocketIO.emit("measureValue", "{\"msg\":\"" + ownerId + "\",\"zone\":\"" + AppResultReceiver.ZONE + "\",\"own\":\"" + AppResultReceiver.ZONE + "_" + ownerId + "\",\"type\":\"v\"}");
                        }
                        Log.d(TAG, "mSocketIOSIDconnected" + "{\"msg\":\"" + ownerId + "\"}");
                        showToast(getString(R.string.connected_to_server));
                    } else {
                        Log.d(TAG, "mSocketIOSIDconnected");
                        showDialog(getString(R.string.alert_title), getString(R.string.not_connect_backside), 0);
                    }
//                    while(!isWebRtcConnected){
//                        try {
//                            Thread.sleep(1000);
//
//                        } catch (InterruptedException e) {
//
//                        }
//                        Log.d(TAG, "   Thread.sleep(1000)");
//                    }
                    menuImageButton_cam.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.webcam_40, 0, 0);
                    menuImageButton_cam.setEnabled(false);
                    menuImageButton_cam.setAlpha(0.3F);
                    isWebRtcConnected=true;
                } else {


                    rtcClient.stopCall();
//                    while(isWebRtcConnected){
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//
//                        }
//
//                    }
                    menuImageButton_cam.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.webcam_40, 0, 0);
                    menuImageButton_cam.setEnabled(true);
                    menuImageButton_cam.setAlpha(0.3F);
                    isWebRtcConnected=false;
                }
            }
        });

        //前往病患過往傷口紀錄
        menuImageButton_list = (Button) findViewById(R.id.menuImageButton_list);
        menuImageButton_list.setBackgroundColor(Color.argb(63, 63, 63, 63));
        menuImageButton_list.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                if (mNetworkInfo == null) {
                    showToast(getString(R.string.open_internet_or_wifi));
                    return;
                } else {

                    if (Integer.parseInt(userId) != 2) {
                        checkAccountCorrecct();
                        if (isAccountCorrecct == true) {
                            AppResultReceiver.vibrating(mActivity);

                            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                            intent.putExtra("roleid", roleId);
                            intent.putExtra("ownerId", ownerId);
                            isShowLogoutHint = false;
                            startActivityForResult(intent, 0);
                        } else {
                            showDialog(getString(R.string.remind_title), getString(R.string.no_account_authority_to_use), 3);
                        }
                    }else {
                        showDialog(getString(R.string.remind_title), getString(R.string.no_authority_to_use), 3);
                    }

                }
            }
        });

        //開啟或關閉打光功能
        menuImageButton_light = (Button) findViewById(R.id.menuImageButton_light);
        menuImageButton_light.setBackgroundColor(Color.argb(63, 63, 63, 63));
        menuImageButton_light.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AppResultReceiver.vibrating(mActivity);
                rtcClient.toggleFlash();
            }
        });

        //呼叫醫師功能
        menuImageButton_help = (Button) findViewById(R.id.menuImageButton_help);
        menuImageButton_help.setBackgroundColor(Color.argb(63, 63, 63, 63));
        menuImageButton_help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    AppResultReceiver.vibrating(mActivity);
                    ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                    if (mNetworkInfo == null) {
                        showToast(getString(R.string.open_internet_or_wifi));
                        return;
                    }
                    checkAccountCorrecct();
                    if (isAccountCorrecct == true) {
                        if (StringUtils.isBlank(ownerId)) {
                            showToast(getString(R.string.set_owner_first));
                            return;
                        }
                        if (mSocketIO.connected()) {
                            if (ownerId.equals("")) {
                                // mSocketIO.emit("measureValue", "{\"msg\":\"" + ownerId + "\",\"own\":\"" + AppResultReceiver.APP + "_" + AppResultReceiver.WEBRTC_ROOMID  + "\"}");
                            } else {
                                mSocketIO.emit("measureValue", "{\"msg\":\"" + ownerId + "\",\"zone\":\"" + AppResultReceiver.ZONE + "\",\"own\":\"" + AppResultReceiver.ZONE + "_" + ownerId + "\"}");
                            }
                            Log.d(TAG, "mSocketIOSIDconnected" + "{\"msg\":\"" + ownerId + "\"}");
                            showToast(getString(R.string.connected_to_server));
                        } else {
                            Log.d(TAG, "mSocketIOSIDconnected");
                            showDialog(getString(R.string.alert_title), getString(R.string.not_connect_backside), 0);
                        }
                    }else {
                        showDialog(getString(R.string.remind_title), getString(R.string.no_account_authority_to_use), 3);
                    }

                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });

        //開啟或關閉藍點標記功能
        menuImageButton_marker = (Button) findViewById(R.id.menuImageButton_marker);
        menuImageButton_marker.setBackgroundColor(Color.argb(63, 63, 63, 63));
        menuImageButton_marker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AppResultReceiver.vibrating(mActivity);
                if (AppResultReceiver.IS_USED_MARKER_DETECTION) {
                    AppResultReceiver.IS_USED_MARKER_DETECTION = false;  //關閉參考點標記
                    menuImageButton_marker.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.marker_40, 0, 0);
                    menuImageButton_marker.setBackgroundColor(Color.argb(63, 63, 63, 63));

                    final ImageView imgDetectedMarker = (ImageView) findViewById(R.id.imgDetectedMarker);
                    if (imgDetectedMarker != null)
                        imgDetectedMarker.setVisibility(View.INVISIBLE);
                } else {
                    AppResultReceiver.IS_USED_MARKER_DETECTION = true;  //開啟參考點標記
                    menuImageButton_marker.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.blue_marker_40, 0, 0);
                    menuImageButton_marker.setBackgroundColor(Color.argb(80, 255, 255, 77));
                }
            }
        });

        //上傳檔案
        menuImageButton_upload = (Button) findViewById(R.id.menuImageButton_upload);
        menuImageButton_upload.setBackgroundColor(Color.argb(63, 63, 63, 63));
        menuImageButton_upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                if ( mNetworkInfo == null) {

                    showToast(getString(R.string.open_internet_or_wifi));
                    return;
                }
                if (Integer.parseInt(userId) != 2) {
                    // preventing double, using threshold of 1000 ms
                    Long lastClickTime = (Long) AppResultReceiver.lastUIClickTime.get("menuImageButton_upload");
                    if (lastClickTime != null && SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                        return;
                    }
                    AppResultReceiver.lastUIClickTime.put("menuImageButton_upload", SystemClock.elapsedRealtime());

                    Log.v(TAG,"fileCount"+fileCount);
                    if(fileCount != 0){
                        Log.v(TAG,"isUploading"+isUploading);
                        if(isUploading==false){
                            isUploading=true;
                            menuImageButton_upload.setEnabled(false);
                            checkAccountCorrecct();
                            if(isAccountCorrecct==true){
                                AppResultReceiver.vibrating(mActivity);

//                if (Integer.parseInt(userId) != 2) {
                                SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(mContext);
                                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + AppResultReceiver.SAVE_DIR;

                                //資料上傳後更新時間
                                evlId = nowEvalTime.format(new Date());
                                curStepFilename = evlId + "_" + evlStep + "_";
                                setFileTime(evlId);
                                SharedPreferences.Editor editor = shared.edit();
                                editor.putString(AppResultReceiver.PREFERENCE_EVALUATION_CASE_ID, evlId);
                                editor.apply();

                                count = 0;
                                filesize(file.getAbsolutePath());
                                myList = new ArrayList<String>();


//                    if (fileCount <= 0) {
                                //showToast(getString(R.string.no_files));
//                            setUploadStatus(true);
//                            showToast(getString(R.string.start_file_upload));
//                            jobManagerRelax.addJobInBackground(new JobQueueUploadFileJob(jobManagerRelax, "", mActivity, mContext, mLiteHttp, path));
//                        } else {

//                        showToast(getString(R.string.start_file_upload));
                                jobManagerRelax.addJobInBackground(new JobQueueUploadFileJob(jobManagerRelax, "", mActivity, mContext, mLiteHttp, path));
                                menuImageButton_upload.setEnabled(true);

                            }else{
                                showDialog(getString(R.string.remind_title), getString(R.string.no_account_authority_to_use), 3);
                            }
                        }else{
                            showDialog(getString(R.string.remind_title),getString(R.string.file_uploading), 3);
                            //
                        }
                    }else{
                        showDialog(getString(R.string.remind_title),getString(R.string.no_files), 3);
                    }


                } else {
                    showDialog(getString(R.string.remind_title), getString(R.string.no_authority_to_use), 3);
                }
            }
        });

        button_goback = (Button) findViewById(R.id.button_goback);
        button_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppResultReceiver.vibrating(mActivity);
                detection.setVisibility(View.INVISIBLE);
                detect_dist.setVisibility(View.INVISIBLE);
                if (boolean_uvccamera_view0 == true) {
                    TextureView glview_color = (TextureView) findViewById(R.id.glview_color);
                    TextureView previewDepth = (TextureView) findViewById(R.id.cdpreview);
                    AutoFitTextureView uvccamera_view0 = (AutoFitTextureView) findViewById(R.id.uvccamera_view0);
                    glview_color.setVisibility(View.INVISIBLE);
                    previewDepth.setVisibility(View.VISIBLE);
                    //uvccamera_view0.setTop((Integer) uvccamera_view0.getTag());
                    if (uvccamera_view0 != null) {
                        uvccamera_view0.configureClearTransform();
                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) uvccamera_view0.getLayoutParams();
                        if (layoutParams != null) {
                            layoutParams.setMargins(8, (Integer) uvccamera_view0.getTag(), 0, 0);
                            layoutParams.width = getPixelsFromDp(60);
                            layoutParams.height = getPixelsFromDp(80);
                            uvccamera_view0.setLayoutParams(layoutParams);
                        }
                    }
                    glview_color.setVisibility(View.INVISIBLE);
                    boolean_uvccamera_view0 = false;
                } else if (boolean_cdpreview == true) {
                    AutoFitTextureView previewView = (AutoFitTextureView) findViewById(R.id.cdpreview);
                    detect_dist.setVisibility(View.INVISIBLE);
                    previewView.configureClearTransform();
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) previewView.getLayoutParams();
                    if (layoutParams != null) {
                        layoutParams.setMargins(8, (Integer) previewView.getTag(), 0, 0);
                        layoutParams.width = getPixelsFromDp(60);
                        layoutParams.height = getPixelsFromDp(80);
                        previewView.setLayoutParams(layoutParams);
                    }
                    button_goback.setVisibility(View.INVISIBLE);
                    boolean_cdpreview = false;
                }
                button_goback.setVisibility(View.INVISIBLE);
            }
        });

        if (!IS_FOR_MIIS_MPDA) {
            menuImageButton_marker.performClick();
        }
    }

    public void generateTasks() {
        if(mTimerLoginTimeout!=null){
            mTimerLoginTimeout.cancel();
        }
        mTimerLoginTimeout = new java.util.Timer(true);
        mTaskLoginTimeout = new TimerTask() {
            public void run() {
                try {
                    Boolean isExist = true;
                    ServiceHelper s = new ServiceHelper();
                    isExist = s.calcuteLoginTime(loginDate, loginPeriod);
                    Log.v(TAG,"isExist"+isExist);
                    if (!isExist) {
                        showDialog(getString(R.string.remind_title), getString(R.string.login_timeout), 2);
                        mTimerLoginTimeout.cancel();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        mTimerLoginTimeout.schedule(mTaskLoginTimeout, 60000, 60000);

        if(mTimerCheckCameraTimeout!=null){
            mTimerCheckCameraTimeout.cancel();
        }
//        if(IS_FOR_MIIS_MPDA){
//            mTimerCheckCameraTimeout = new java.util.Timer(true);
//            mTaskCheckCameraTimeout= new TimerTask() {
//                public void run() {
//                    try {
//                        isUvcDeviceOK=true;
//                        AppResultReceiver.nonzero = true;
//
//
//                        Boolean cameraOK = true;
//                        cameraOK=checkCamera();
//                        Log.v("TEST check camera","cameraOK"+cameraOK+" : "+nowEvalTime.format(new Date()));
//
//                        Log.v("TEST check camera","AppResultReceiver.lastColorOnFrame"+AppResultReceiver.lastColorOnFrame);
//                        Log.v("TEST check camera","AppResultReceiver.lastThermalOnFrame"+AppResultReceiver.lastThermalOnFrame);
//                        Log.v("TEST check camera","AppResultReceiver.lastDepthOnFrame"+AppResultReceiver.lastDepthOnFrame);
//                        Log.v("TEST check camera","AppResultReceiver.nonzero"+AppResultReceiver.nonzero);
//                        if (!cameraOK) {
//                            //  if (!cameraOK) {
//                            showDialog(getString(R.string.remind_title), getString(R.string.camera_failed), 4);
//                            //mTimerCheckCameraTimeout.cancel();
//                            mTimerLoginTimeout.cancel();
//                            mTaskTestTimeout.cancel();
//                            mTaskCheckCameraTimeout.cancel();
//                        }
//
//
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//
//            mTimerCheckCameraTimeout.schedule(mTaskCheckCameraTimeout, 60000, 60000);
//        }



/*
        if(mTaskTestTimeout!=null){
            mTimerTestTimeout.cancel();
        }

        mTimerTestTimeout = new java.util.Timer(true);
        mTaskTestTimeout= new TimerTask() {
            public void run() {
                try {
                    Log.v(TAG,"rtcClient.onPause() : "+nowEvalTime.format(new Date()));

                    rtcClient.onPause();
                    //    Thread.sleep(1000); //android.os.SystemClock.uptimeMillis()
                    // GPIOOFF();
                    //Thread.sleep(1000); //android.os.SystemClock.uptimeMillis()
                    //GPIOON();
                    Thread.sleep(500); //android.os.SystemClock.uptimeMillis()
                    rtcClient.onResume();
                    AppResultReceiver.isTakingPicture=false;
                    Log.v(TAG," rtcClient.onResume() : "+nowEvalTime.format(new Date()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mTimerTestTimeout.schedule(mTaskTestTimeout, 10000, 8000);
*/
        if (AppResultReceiver.isPiModule) {
            mFindIpRunner = new Runnable() {
                public void run() {
                    mGetIpHandler.removeCallbacksAndMessages(null);
                    jobManagerUrgent.addJobInBackground(new JobQueueFindIPJob(jobManagerUrgent, mActivity));

                    if (AppResultReceiver.GET_IP_DELAY_MS < 3000)
                        AppResultReceiver.GET_IP_DELAY_MS = 3000;
                    mGetIpHandler.postDelayed(this, AppResultReceiver.GET_IP_DELAY_MS);
                }
            };

            mGetMsgRunner = new Runnable() {
                public void run() {
                    //從server 取得txt 檔案內容
                    if (!iipOld.equals(iip) && !iip.equals("")) {
                        mtxtDownloader.stop();
                        mtxtDownloader.setUrl("http://" + iip + ":9000/ipcam01/msg.txt?action=123");
                        mtxtDownloader.start();
                        iipOld = iip;
                    }

                    mGetMsgHandler.postDelayed(this, 10000);
                }
            };

            mGetThermalRunner = new Runnable() {
                public void run() {
                    if (mIPCameraMjpgView != null)
                        mIPCameraMjpgView.setUrl("http://" + iip + ":9000/ipcam01/video.mjpeg?action=123");
                }
            };
        }
    }

    private void getPatientNoList(){
        try{
            if(!userId.equals("2")) {
                jobManagerUrgent.addJobInBackground(new JobQueueFindPatientNoJob(jobManagerUrgent, mActivity, roleId));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private int getPixelsFromDp(int size){

        DisplayMetrics metrics =new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return(size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;

    }

    public void setPatientNoList(JSONArray objList){
        try{
            patientArr = objList;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void adjustAspectRatio(TextureView mTextureView, int videoWidth, int videoHeight) {
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
        mTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        mTextureView.setTransform(txform);
    }

    private void updateTextureViewScaling(TextureView textureView, int viewWidth, int viewHeight) {
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int VIDEO_HEIGHT = displayMetrics.heightPixels;
        int VIDEO_WIDTH = displayMetrics.widthPixels;
        if (VIDEO_WIDTH > viewWidth && VIDEO_WIDTH > viewHeight) {
            scaleX = (float) VIDEO_WIDTH / viewWidth;
            scaleY = (float) VIDEO_HEIGHT / viewHeight;
        } else if (VIDEO_WIDTH < viewWidth && VIDEO_HEIGHT < viewHeight) {
            scaleY = (float) viewWidth / VIDEO_WIDTH;
            scaleX = (float) viewHeight / VIDEO_HEIGHT;
        } else if (viewWidth > VIDEO_WIDTH) {
            scaleY = ((float) viewWidth / VIDEO_WIDTH) / ((float) viewHeight / VIDEO_HEIGHT);
        } else if (viewHeight > VIDEO_HEIGHT) {
            scaleX = ((float) viewHeight / VIDEO_WIDTH) / ((float) viewWidth / VIDEO_WIDTH);
        }

        // Calculate pivot points, in our case crop from center
        int pivotPointX = viewWidth / 2;
        int pivotPointY = viewHeight / 2;

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);

        textureView.setTransform(matrix);
    }

    public Bitmap getBitmapFromSurface(SurfaceView surfaceView) {
        Bitmap bitmap = Bitmap.createBitmap(surfaceView.getWidth() + 900, surfaceView.getHeight(), Bitmap.Config.ARGB_8888);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PixelCopy.request(surfaceView, bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
                @Override
                public void onPixelCopyFinished(int copyResult) {
                    if (PixelCopy.SUCCESS == copyResult) {
                        // onSuccessCallback(bitmap)
                    } else {
                        // onErrorCallback()
                    }
                }
            }, surfaceView.getHandler());
        }
        return bitmap;
    }

    private Bitmap convolve(Bitmap original, float[] coefficients) {
        Bitmap bitmap = Bitmap.createBitmap(
                original.getWidth(), original.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(this);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicConvolve3x3 convolution
                = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
        convolution.setInput(allocIn);
        convolution.setCoefficients(coefficients);
        convolution.forEach(allocOut);

        allocOut.copyTo(bitmap);         // { -1, -1, -1,
        rs.destroy();                    //   -1 , 8, -1,
        return bitmap;                   //   -1, -1, -1  }
    }

    private Bitmap createInvertedBitmap(Bitmap src) {
        ColorMatrix colorMatrix_Inverted =
                new ColorMatrix(new float[]{
                        -1, 0, 0, 0, 255,
                        0, -1, 0, 0, 255,
                        0, 0, -1, 0, 255,
                        0, 0, 0, 1, 0});

        ColorFilter ColorFilter_Sepia = new ColorMatrixColorFilter(
                colorMatrix_Inverted);

        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        paint.setColorFilter(ColorFilter_Sepia);
        canvas.drawBitmap(src, 0, 0, paint);

        return bitmap;
    }

    public void generateViews() {
        try {
            WindowManager wm = (WindowManager) mContext
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            top.defaults.camera.AutoFitTextureView glview_color = (top.defaults.camera.AutoFitTextureView) findViewById(R.id.glview_color);
            glview_color.setAlpha(0.3f);
            glview_color.setZ(0.2f);
            //glview_color.configureFillTransform(width,height,width, height);
            org.webrtc.SurfaceViewRenderer webrtcSurface = (org.webrtc.SurfaceViewRenderer) findViewById(R.id.glview_call);
            Rect dest = new Rect(0, 0, width, height);

            rtcVideoSink = new ImageProcessVideoSink();
            rtcVideoSink.setOnFrameListener(new ImageProcessVideoSink.OnFrameListener() {
                @Override
                public void onFrame(VideoFrame videoFrame) {
                    try {
                        AppResultReceiver.lastColorOnFrame = true;
                        if (AppResultReceiver.webrtcFrameNo++ < 30) {
                            if (AppResultReceiver.webrtcFrameNo < 0)
                                AppResultReceiver.webrtcFrameNo = 0;
                            return;
                        }

                        if (semaphore != null && AppResultReceiver.webrtcFrameNo % 3 == 0) {
                            synchronized (semaphore) {
                                try {
                                    if (semaphore.tryAcquire(50, TimeUnit.MILLISECONDS)) {
                                        jobManagerMarker.addJobInBackground(new JobQueueMarkerJob(jobManagerMarker, "", mActivity, videoFrame, videoFrame.getRotatedWidth(), videoFrame.getRotatedHeight()));
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        if (AppResultReceiver.webrtcFrameNo % 10 == 0) {
//                        //Log.d(TAG,"touchThermalPointCelsius:"+ AppResultReceiver.touchThermalPointCelsius);
                            TextView detect_dist = (TextView) findViewById(R.id.detect_dist);
                            if (detect_dist != null && detect_dist.getVisibility() == View.VISIBLE) {
                                try {
                                    detect_dist.setZ(0.3f);
                                    if (boolean_uvccamera_view0) {
                                        double resultThermal = Double.valueOf(df.format(AppResultReceiver.touchPointThermalCelsius));
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                detect_dist.setText(resultThermal + "C");
                                            }
                                        });
                                    } else if (boolean_cdpreview) {
                                        double resultDistance = Double.valueOf(df.format(AppResultReceiver.touchPointDepthCentiMeter));
                                        Log.v("sdasdasdasdasd",String.valueOf(AppResultReceiver.touchPointDepthCentiMeter));
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                detect_dist.setText(resultDistance + "cm");
                                            }
                                        });
                                    }
                                } catch (Exception ex) {
                                    // ex.printStackTrace();
                                }
                            }
                        }

                        if (AppResultReceiver.webrtcFrameNo % 12 == 0) {
                            double resultDistance = Double.valueOf(df.format(AppResultReceiver.touchPointDepthCentiMeterAvg));
                            String temp = "";

                            if (AppResultReceiver.DEBUG_LEVEL > 0)
                                temp = temp + "\n" + AppResultReceiver.lastPicRValue + "," + AppResultReceiver.lastPicGValue + "," + AppResultReceiver.lastPicBValue;

                            if (resultDistance < 24)
                                temp = temp + "\n" + getString(R.string.bad_distance);
                            else if (resultDistance > 45)
                                temp = temp + "\n" + getString(R.string.far);
                            else if (resultDistance < 27)
                                temp = temp + "\n" + getString(R.string.near);

                            if (AppResultReceiver.detectBrightness > 0) {
                                temp = temp + "\n" + getString(R.string.bright);

                            } else if (AppResultReceiver.detectBrightness < 0) {
                                temp = temp + "\n" + getString(R.string.dark);
                            } else {
                            }

                            final String distString = temp;
                            TextView detect_brightness = (TextView) findViewById(R.id.detect_brightness);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    detect_brightness.setText(distString);
                                }
                            });
                        }
//
//                        //top.defaults.camera.AutoFitTextureView glview_color = (top.defaults.camera.AutoFitTextureView) findViewById(R.id.glview_color);
//                        if (glview_color.getVisibility() == View.VISIBLE) {
//                            final Bitmap bmp = getBitmapFromSurface(webrtcSurface);
//                            Canvas canvas = glview_color.lockCanvas();
//                            PaintFlagsDrawFilter mSetfil = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
//                            canvas.setDrawFilter(mSetfil);
//                            canvas.drawBitmap(bmp, null, dest, new Paint());
//                            glview_color.unlockCanvasAndPost(canvas);
//                            bmp.recycle();
//                        }
//                        }
                        Thread.sleep(1);
                    } catch (Exception ex) {
                        //ex.printStackTrace();
                    }
                }
            });

            rtcClient = new CallRtcClient(this, jobManagerUrgent, jobManagerRelax, R.id.glview_call, rtcVideoSink);
            rtcClient.EXTRA_ROOMURL = AppResultReceiver.WEBRTC_URL;
            rtcClient.EXTRA_ROOMID = AppResultReceiver.WEBRTC_ROOMID;
            rtcClient.addCallback(new AppRTCCallback() {
                @Override
                public void onConnected() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isWebRtcConnected = true;  //開啟視訊連線
                    menuImageButton_cam.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.blue_webcam_40, 0, 0);
                    menuImageButton_cam.setEnabled(true);
                    menuImageButton_cam.setAlpha(1.0F);
                }

                @Override
                public void onDisconnected(final String description) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.v(TAG,"onDisconnected : "+description);
                    menuImageButton_cam.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.webcam_40, 0, 0);
                    menuImageButton_cam.setEnabled(true);
                    menuImageButton_cam.setAlpha(1.0F);
                    isWebRtcConnected = false;  //開啟視訊連線
                }

                @Override
                public void onError(final String description) {
                    Log.v(TAG,"onError");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    menuImageButton_cam.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.webcam_40, 0, 0);
                    menuImageButton_cam.setEnabled(true);
                    menuImageButton_cam.setAlpha(1.0F);
                    isWebRtcConnected = false;  //開啟視訊連線
                }
            });
        } catch (Exception ex) {
            showToast("open camera fail");
        }

//        if (AppResultReceiver.isUvcDevice) {
//            onInitUVCSurface();
//        }
//
//        if (AppResultReceiver.isGifDemo) {
//            GifImageView gifImageView1 = (GifImageView) findViewById(R.id.uvc_gif_view1);
//            if (gifImageView1 != null) {
//                gifImageView1.setVisibility(View.VISIBLE);
//                GifDrawable gifDrawable1 = (GifDrawable) gifImageView1.getDrawable();
//                gifDrawable1.setSpeed((float) 0.5);
//                gifDrawable1.pause();
//                View view = (View) findViewById(R.id.uvc_gif_view1);
//                view.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        try {
//                            Intent intent = new Intent(MainActivity.this, MJViewActivity.class);
//                            intent.putExtra("iip", iip);
//                            startActivityForResult(intent, 0);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//
//            GifImageView gifImageView2 = (GifImageView) findViewById(R.id.uvc_gif_view2);
//            if (gifImageView2 != null) {
//                gifImageView2.setVisibility(View.VISIBLE);
//                GifDrawable gifDrawable2 = (GifDrawable) gifImageView2.getDrawable();
//                gifDrawable2.setSpeed((float) 0.5);
//            }
//        } else {
//            GifImageView gifImageView1 = (GifImageView) findViewById(R.id.uvc_gif_view1);
//            if (gifImageView1 != null) {
//                GifDrawable gifDrawable1 = (GifDrawable) gifImageView1.getDrawable();
//                gifDrawable1.stop();
//            }
//
//            GifImageView gifImageView2 = (GifImageView) findViewById(R.id.uvc_gif_view2);
//            if (gifImageView2 != null) {
//                GifDrawable gifDrawable2 = (GifDrawable) gifImageView2.getDrawable();
//                gifDrawable2.stop();
//            }
//        }

//        if (AppResultReceiver.isPiModule) {
//                View uvcCameraView = (View) findViewById(R.id.thermalMjpegview);
//                if (uvcCameraView != null) {
//                    uvcCameraView.setVisibility(View.VISIBLE);
//                }
//                mIPCameraMjpgView = findViewById(R.id.thermalMjpegview);
//            if (mIPCameraMjpgView != null) {
//                mIPCameraMjpgView.setMode(MjpegView.MODE_FIT_HEIGHT);
//                mIPCameraMjpgView.setRotation(-90.0f);
//                mIPCameraMjpgView.setConnectTimeout(1000);
//                mIPCameraMjpgView.setReadTimeout(3000);
//                mIPCameraMjpgView.setMsecWaitAfterReadImageError(1000);
//                mIPCameraMjpgView.setRecycleBitmap(true);
//
//                mIPCameraMjpgView.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        try {
//                            Intent intent = new Intent(MainActivity.this, MJViewActivity.class);
//                            intent.putExtra("iip", iip);
//                            startActivityForResult(intent, 0);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//        } else {
//            if (mIPCameraMjpgView != null) {
//                mIPCameraMjpgView.setUrl("");
//                mIPCameraMjpgView.stopStream();
//            }
//        }

        if ((Build.MODEL.endsWith("MPD100") || Build.MODEL.endsWith("MPD500")) && AppResultReceiver.isMultiCam) {
            // set android studio locat Regex
            // ^(?!.*(ConfigStore|QCamera|mm-camera|improveTouch|healthd|BatteryService|ServiceManagement|KeyguardUpdateMonitor)).*$
            // ^(?!.*(<IMGLIB><ERROR>|<IFACE ><ERROR>|<CPP   >< INFO>|BatteryService|healthd|improveTouch|ServiceManagement)).*$
            // ^(?!.*(<IMGLIB><ERROR> 1682|<IFACE ><ERROR> 2291|<IMGLIB><ERROR> 2561|<CPP   >< INFO> 371|improveTouch|<ISP   >< INFO> 245|<STATS_AF >< INFO> 439|<IFACE >< INFO> 12367|<MCT   >< INFO> 1209|<HAL><ERROR> streamCbRoutine: 951|healthd: update|<MCT   >< INFO>|<IFACE >< INFO>|ConfigStore: android::hardware|system_process|com.android.systemui|<CPP   >< INFO>|wmt_configuration_service|com.qualcomm.qti.qdma|chatty: uid=1000|healthd: sw555|ServiceManagement|<HAL><INFO> handleMetadataWithLock|Background concurrent copying|CANNOT LINK EXECUTABLE|socket.io onConnectError)).*$
            // ^(?!.*(improveTouch|ServiceManagement|healthd|mm-camera|ConfigStore|BatteryService|chatty|KeyguardUpdateMonitor|DPMJ|wmt_sn_service|BatteryMonitor|linker|UsbDevice vid:|socket.io onConnectError|QDMA:PeriodicCI|wm.app.white.list|storaged|KernelUidCpuTimeReader|WebViewAssetServer|WebViewJSInterface|NuPlayerDriver|KeyguardSecurityView|KeyguardViewMediator|KeyguardStatusView|CompatibilityInfo|ActivityTrigger|StatusBar|WificondControl|WmtPowerSaving|charlie|PowerManagerService|CameraProviderManager|FileUtility|CameraLatencyHistogram|Camera3-OutputStream|Camera2Client|qdlights|NotificationService|audio_hw|DreamController|Keyguard|UsageStatsService|DreamManagerService|InputReader|soundtrigger|wificond|ThermalEngine|WindowManager|system_server|ShellHelper|QCamera|qomx_image_core|EventHub|Camera3|AMessage|PowerHAL|WIFI|webview|FileHelper|chromium|cr_media|Camera2|BpMediaSource|MediaPlayer|sound|ANDR-PERF|BoostFramework|OpenGLRenderer|CameraService|CamComm|_uvc_frame_format_matches_guid|MetaDataBase|mm-still|quadracfa_dummy|FileSource|GenericSource)).*$
            // set this APP to be system's APP, then can catch camera 3
            // adb shell su 0 setenforce 0
            // adb shell su 0 setprop persist.camera.isp.dualisp 1
            // adb shell su 0 getprop vendor.camera.aux.packagelist
            // adb shell su 0 setprop vendor.camera.aux.packagelist "org.codeaurora.snapcam,wm.andro.doctor,org.itri.woundcamrtc"
            cdpreview = findViewById(R.id.cdpreview);

            if (cdpreview != null) {
                View view = (View) cdpreview;
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                    cdpreview.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            cdpreview.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {

                                    if (boolean_cdpreview == true) {
                                        DisplayMetrics displayMetrics = new DisplayMetrics();
                                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                        int width = displayMetrics.widthPixels;
                                        int height = displayMetrics.heightPixels;

                                        int x = (int) event.getX();
                                        int y = (int) event.getY();

                                        double touch_coordinate_x = (double) x / width;
                                        double touch_coordinate_y = (double) y / height;

                                        AppResultReceiver.touchPointXp = touch_coordinate_x;
                                        AppResultReceiver.touchPointYp = touch_coordinate_y;


                                        detection.setX(x - detection.getWidth() / 2);
                                        detection.setY(y - detection.getHeight() / 2);
                                        detect_dist.setX(x);
                                        detect_dist.setY(y - 80);

                                    }
                                    return false;
                                }
                            });


                            if (boolean_cdpreview == false) {
                                try {
                                    AutoFitTextureView previewView = (AutoFitTextureView) findViewById(R.id.cdpreview);
                                    org.webrtc.SurfaceViewRenderer webrtcSurface = (org.webrtc.SurfaceViewRenderer) findViewById(R.id.glview_call);
                                    button_goback.setVisibility(View.VISIBLE);

                                    if (previewView.getVisibility() == View.VISIBLE) {
                                        WindowManager wm = (WindowManager) mContext
                                                .getSystemService(Context.WINDOW_SERVICE);
                                        Display display = wm.getDefaultDisplay();
                                        Point size = new Point();
                                        display.getSize(size);
                                        int width = size.x;
                                        int height = size.y;
                                        TextView detect_dist = (TextView) findViewById(R.id.detect_dist);
                                        DisplayMetrics displayMetrics = new DisplayMetrics();
                                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                        detection.setX((displayMetrics.widthPixels / 2) - (detection.getWidth() / 2));
                                        detection.setY((displayMetrics.heightPixels / 2) - (detection.getHeight() / 2));
                                        detect_dist.setX((displayMetrics.widthPixels / 2) - (detection.getWidth() / 2));
                                        detect_dist.setY((displayMetrics.heightPixels / 2) - (detect_dist.getHeight() / 2) - 100);

                                        if (detect_dist.getVisibility() == View.INVISIBLE) {
                                            detect_dist.setVisibility(View.VISIBLE);
                                            detection.setVisibility(View.VISIBLE);

                                            previewView.setTag(previewView.getTop());
//                                         previewView.setZ(0.1f);
                                            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) previewView.getLayoutParams();
                                            if (layoutParams != null) {
                                                layoutParams.setMargins(0, 0, 0, 0);
                                                layoutParams.width = webrtcSurface.getWidth();
                                                layoutParams.height = webrtcSurface.getHeight();
                                                previewView.setLayoutParams(layoutParams);
                                                previewView.configureFillTransform(120, 160, width, height);
                                                previewView.invalidate();
                                            }

                                            //need do again
                                            mHandler.postDelayed(new Runnable() {
                                                public void run() {
                                                    try {
                                                        AutoFitTextureView previewView = (AutoFitTextureView) findViewById(R.id.cdpreview);
                                                        org.webrtc.SurfaceViewRenderer webrtcSurface = (org.webrtc.SurfaceViewRenderer) findViewById(R.id.glview_call);
                                                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) previewView.getLayoutParams();

                                                        if (layoutParams != null) {
                                                            layoutParams.setMargins(0, 0, 0, 0);
                                                            layoutParams.width = webrtcSurface.getWidth();
                                                            layoutParams.height = webrtcSurface.getHeight();
                                                            previewView.setLayoutParams(layoutParams);
                                                            previewView.configureFillTransform(120, 160, width, height);
                                                            previewView.invalidate();
                                                        }
                                                    } catch (Exception ex) {
                                                    }
                                                }
                                            }, 1);
                                        } else if (detect_dist.getVisibility() == View.VISIBLE) {

                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                boolean_cdpreview = true;
                            }
                        }
                    });

                }

//                cdpreview.setAlpha(0.5f);
//                cdpreview.setZ(0.3f);
                photographer = PhotographerFactory.createPhotographerWithCamera2(this, cdpreview, 1);
//                photographer.setFacing(1);
                photographer.setOnEventListener(new Photographer.OnEventListener() {

                    @Override
                    public void onDeviceConfigured() {

                    }

                    @Override
                    public void onPreviewStarted() {

                    }

                    @Override
                    public void onZoomChanged(float zoom) {

                    }

                    @Override
                    public void onPreviewStopped() {
                        photographer.restartPreview();
                    }

                    @Override
                    public void onStartRecording() {

                    }

                    @Override
                    public void onFinishRecording(String filePath) {

                    }

                    @Override
                    public void onShotFinished(String filePath) {

                    }

                    @Override
                    public void onError(Error error) {
                        error.printStackTrace();
                        //   rtcClient.onPause();
                        try {
                            //     Thread.sleep(500); // more than 500
                        } catch (Exception ex) {
                        }
                        //  rtcClient.onResume();
                    }
                });
            }
        } else {
            cdpreview = findViewById(R.id.cdpreview);
            if (cdpreview != null) {
                View view = (View) findViewById(R.id.cdpreview);
                if (view != null) {
                    view.setVisibility(View.INVISIBLE);
                }
            }
        }

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (CallRtcClient.EXTRA_CAMERA2) {
//                    Camera2Enumerator camera2Enumerator = new Camera2Enumerator(mActivity);
//                    final String[] deviceNames = camera2Enumerator.getDeviceNames();
//                    Toast logToast = Toast.makeText(mActivity, "Total camera count =" + deviceNames.length, Toast.LENGTH_SHORT);
//                    logToast.show();
//                } else {
//                    Camera1Enumerator camera1Enumerator = new Camera1Enumerator(false);
//                    final String[] deviceNames = camera1Enumerator.getDeviceNames();
//                    Toast logToast = Toast.makeText(mActivity, "Total camera count =" + deviceNames.length, Toast.LENGTH_SHORT);
//                    logToast.show();
//                }
//            }
//        }, 3000);
    }

    public  void checkAccountCorrecct() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

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
                                    new HttpListener<String>(false, false, false) {
                                        @Override
                                        public void onSuccess(String s, com.litesuits.http.response.Response<String> response) {
                                            super.onSuccess(s, response);
                                            JSONObject mJsonObject = null;
                                            try {
                                                mJsonObject = new JSONObject(response.getResult());
                                                String result = mJsonObject.getString("success");
                                                Log.v(TAG,"response.getResult() : "+ response.getResult());
                                                Log.v(TAG,"result.toLowerCase() : "+result.toLowerCase());
                                                if (result.toLowerCase().equals("true")) {
                                                    try {

                                                        isAccountCorrecct=true;
                                                        Log.v(TAG,"isAccountCorrecct123 : "+isAccountCorrecct);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    isAccountCorrecct=false;
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(HttpException exception, Response<String> response) {
                                            super.onFailure(exception, response);

                                            try {
                                                Log.v(TAG,"response.onFailure : ");
                                                isAccountCorrecct=false;
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

        return ;
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
    public void GPIOOFF() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.putExtra("depth_status", "depth_off");
            intent.putExtra("thermal_status", "thermal_off");
            ComponentName componentName = new ComponentName("com.example.mpda_gpio_ctrl", "com.example.mpda_gpio_ctrl.MainActivity");
            intent.setComponent(componentName);
            startActivity(intent);
            Thread.sleep(800);
        } catch (Exception e) {

        }
    }
    public boolean checkCamera() {
        try {AppResultReceiver.lastColorOnFrame=false;
            AppResultReceiver.lastThermalOnFrame=false;
            AppResultReceiver.lastDepthOnFrame=false;

            Thread.sleep(2000);
            if (!AppResultReceiver.lastColorOnFrame || !AppResultReceiver.lastThermalOnFrame || !AppResultReceiver.lastDepthOnFrame) {
                //   if (!AppResultReceiver.lastColorOnFrame ||!AppResultReceiver.lastThermalOnFrame) {
                //       if (!AppResultReceiver.lastColorOnFrame || !AppResultReceiver.lastDepthOnFrame) {
                return false;
            }
        } catch (Exception e) {

        }
        return true;
    }


    class TestSensorListener implements SensorEventListener {

        float[] aValues = new float[3];
        float[] mValues = new float[3];

        @Override
        public void onSensorChanged(SensorEvent event) {
            // 读取加速度传感器数值，values数组0,1,2分别对应x,y,z轴的加速度

            switch (event.sensor.getType ()){
                case Sensor.TYPE_ACCELEROMETER:
                    aValues = event.values.clone ();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mValues = event.values.clone ();
                    break;
            }

            float[] R = new float[16];
            float[] orientationValues = new float[3];
            SensorManager.getRotationMatrix(R, null, aValues, mValues);
            SensorManager.getOrientation(R, orientationValues);

            float azimuth = (float)Math.toDegrees (orientationValues [0]);
            float pitch = (float)Math.toDegrees (orientationValues [1]);
            float roll = (float)Math.toDegrees (orientationValues [2]);

            tv_pitch.setText(String.valueOf(Math.round(pitch) + "°"));
            tv_roll.setText(String.valueOf(Math.round(roll) + "°"));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i(TAG, "onAccuracyChanged");
        }

    }

    private BroadcastReceiver mScreenStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                GPIOON();
                bl_gpio = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                GPIOOFF();
                bl_gpio = false;
            }
        }
    };
}
