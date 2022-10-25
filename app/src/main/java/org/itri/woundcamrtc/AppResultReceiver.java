package org.itri.woundcamrtc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;
import com.google.webviewlocalserver.WebViewLocalServer;
//import com.serenegiant.utils.FileUtils;

import org.itri.woundcamrtc.helper.DocumentsUtils;
import org.itri.woundcamrtc.helper.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.itri.woundcamrtc.SettingsFragment.KEY_PREF_DEBUG_LEVEL;
import static org.itri.woundcamrtc.SettingsFragment.KEY_PREF_FILE_ROOT_PATH;
import static org.itri.woundcamrtc.SettingsFragment.KEY_PREF_THERMAL_TYPE;

//NO Name      APP    ServerIP       ServerMAC             WIFI AP
//1. 台北台大   NTUH    please import   org.itri.woundcamrtc.constant.NTUH.*;
//2. 新竹台大   CNTUH   please import   org.itri.woundcamrtc.constant.CNTUH.*;
//3. 壢新醫院   LSH     please import   org.itri.woundcamrtc.constant.LSH.*;
//4. 奇美醫院   CMH     please import   org.itri.woundcamrtc.constant.CMH.*;
//5. 三總(佑康) TSGH     please import   org.itri.woundcamrtc.constant.TSGH.*;
//6. 北巿聯醫   TPECH    please import   org.itri.woundcamrtc.constant.TPECH.*;
//7. 雲林台大   YLH      please import   org.itri.woundcamrtc.constant.YLH.*;
//8. 花蓮慈濟   TZUCHI   please import   org.itri.woundcamrtc.constant.TZUCHI.*;
//9. 晉弘       MIIS    please import   org.itri.woundcamrtc.constant.MIIS.*;

import static org.itri.woundcamrtc.constant.MIISv3.*;

public class AppResultReceiver extends ResultReceiver implements Serializable {
    private static final long serialVersionUID = 1382331359868556980L;
    public static final String PROJECT_NAME = "WoundCamRtc"; // 專案名稱
    public static final String PACKAGE_NAME = "org.itri.wound"; // APP 封包名稱
    public static final String APPVER = "1.3.svn98.20210322"; // APP版本號 major.minor[.build] 記得要改 versionCode, versionName @ /app/build.gradle

    //  public static final String PROJECT_PATH = CONSTAT_PROJECT_PATH; //WEB URL的第一個path
    public static final String APP = CONSTANT_APP; //使用APP的機構;(主責醫院單位)
    public static final String ZONE = CONSTANT_ZONE; //使用APP的機構分區;(照護機構單位)
    public static final String SERVER_IP = CONSTANT_SERVER_IP; //Web Server IP/domain name
    public static final String SERVER_IP_PORT = CONSTANT_SERVER_IP_PORT; //Web Server IP/domain name, 及port
    public static final String AI_SERVER_IP_PORT = CONSTANT_AI_SERVER_IP_PORT;  //AI Server IP/domain name, 及port
    public static String VERSION_CHECK_URL = CONSTANT_VERSION_CHECK_URL; // APP 最新版本訊息, 提供版本檢查及更新 //SERVER_IP_PORT + "/" + PROJECT_PATH + "/baby/ovoRefreshJson?app=wound." + APP.toLowerCase() + "&location=" + ZONE;
    public static String WEBSOCKET_URL = CONSTANT_WEBSOCKET_URL; // 溝通訊息 websocket IP:port
    public static String WEBRTC_URL = CONSTANT_WEBRTC_URL; // WebRTC視訊溝通 IP:port, https://140.96.170.75:8300
    public static String STUN_URL = CONSTANT_STUN_URL; // WebRTC視訊溝通STUN IP:port,
    public static String WEBRTC_ROOMID = CONSTANT_WEBRTC_ROOMID;
    public static String DEFAULT_UPLOAD_PATH = CONSTANT_UPLOAD_PATH;
    public static String DEFAULT_LOGIN_PATH = CONSTANT_LOGIN_PATH;
    public static String DEFAULT_UPDATE_PASS_PATH = CONSTANT_UPDATE_PASS_PATH;
    public static String DEFAULT_QRY_PATIENTINFO_PATH = CONSTANT_QRY_PATIENTINFO_PATH;
    public static String DEFAULT_QRY_DATE_PATH = CONSTANT_QRY_DATE_PATH;
    public static String DEFAULT_QRY_RECORD_PATH = CONSTANT_QRY_RECORD_PATH;
    public static String DEFAULT_QRY_PATIENTNOLIST_PATH = CONSTANT_QRY_PATIENTNOLIST_PATH;
    public static String DEFAULT_LOADIMG_PATH = CONSTANT_LOADIMG_PATH;
    public static String DEFAULT_WEBVIEW_PATH = CONSTANT_WEBVIEW_URL;
    public static String DEFAULT_POST_AI_COLOR_IMAGE_PATH = CONSTANT_POST_AI_COLOR_IMAGE_PATH;
    public static  ArrayList<String> cookies = null; // APP HTTP login的session, 避免呼叫http request時, 每次都要登入
    public static boolean DEMO_WEBRTC = CONSTANT_DEMO_WEBRTC;
    public static boolean CHECK_INTERNET = CONSTANT_CHECK_INTERNET;
    public static String PING_SERVER_URL = CONSTANT_PING_SERVER_URL;
    public static String PING_INTERNET_URL = CONSTANT_PING_INTERNET_URL;
    public static String PING_DEVICE_IP1 = "192.168.1.101"; // no longer in use for PI module
    public static String PING_DEVICE_IP2 = "192.168.1.102"; // no longer in use for PI module
    public static String PING_DEVICE_IP3 = "192.168.1.103"; // no longer in use for PI module

    public static String URL_DEFAULT = "file:///android_asset/html/batch.html"; // no longer in use

    //本地端資料位置及資料庫
    public static final String ROOT_FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + PROJECT_NAME;
    public static final String DB_FILE_NAME = PROJECT_NAME + ".db";
    public static final String DB_PATH_FILE_NAME = PROJECT_NAME + File.separator + PROJECT_NAME;
    public static final String Main_DIR = PROJECT_NAME;
    public static final String SAVE_DIR = Main_DIR + File.separator + "CameraImg";
    public static final String BackUp_DIR = Main_DIR + File.separator + "CameraImgBackup";
    public static final String TEST_DIR = Main_DIR + File.separator + "TestMaskImg";
    public static final String KEY_PATIENT_DATA = PACKAGE_NAME + ".data.patient";
    public static final String KEY_NURSE_DATA = PACKAGE_NAME + ".data.nurse";
    public static String filetag = ""; // 目前選擇的傷口部位

    //全域變數
    public Context mContext;
    private Receiver mReceiver;
    public static MainActivity mMainActivity; // 跨activity要用的main ui activity
    public static WebViewLocalServer mAssetServer; // 因應 安全性, 需要local web server
    public static WebViewLocalServer.AssetHostingDetails mAssetServerDetails; // 因應 安全性, 需要local web server, 設定/ASSET資源路徑
    public static String app_url_base = ""; // 照片瀏覽畫面的webview進入點url
    public static String account = ""; // 定期檢查登入帳號是否有效, 帳號資料
    public static String password = "2"; // 定期檢查登入帳號是否有效, 密碼資料
    public static boolean isAccountCorrecct = true; // 定期檢查登入帳號是否有效
    public static String SSID = ""; // no longer in use
    public static String OWNER_ID = "2"; // no longer in use
    public static String NURSE_ID = "2"; // no longer in use
    public static String UI_TYPE = "wound"; // no longer in use
    public static String UI_LANGUAGE = "eng"; // no longer in use
    public static int DEBUG_LEVEL = AppResultReceiver.DEBUG_IDLE; // APP debug level
    public static String DEBUG_TAG = "12345"; // the parameter for APP debug level
    public static boolean ALLOW_XSSL = true; //APP是否不檢查URL憑證的有效性 ignore https certificate validation
    public static boolean dataEncrypt = CONSTANT_DATA_ENCRYPT; // image, txt, sqlite DB  加密

    public static double refMarkerAspectRange = 0.2; // 藍色參考物在影像上可容忍的pixel長寬比
    public static double refMarkerWidth = 1.5; //藍色參考物實際直徑大小cm, 20200512 閔易測試可取消, 20200511偵測出的小0.05cm, 20191026偵測到的小0.15cm, 20191029好像又沒這問題
    public static double estimateWidth = 0.0; //預估寬度
    public static double estimateHeight = 0.0; //預估長度
    public static double getEstimateDepth = 0.0; //預估長度
    public static double estimateArea = 0.0; //預估面積
    public static double estimateDepth = 0.0; //預估深度
    public static int epithelium_prop = 0; //上皮組織比例
    public static int granular_prop = 0; //肉芽組織比例
    public static int slough_prop = 0; //腐皮組織比例
    public static int eschar_prop = 0; //焦痂組織比例
    public static double snapshutDistance = 0.0; // 按下拍照時的拍攝距離CM
    public static boolean redrawMultiSlider = false; // 控制左右拉bar在繪製時, 是否動作
    public static int grabcutDownsampleType = 5; // 下修grabcut處理影像大小倍數(原image大小在舊機器會Out of memory)
    public static boolean grabcutWithColorAI = false; // 指示目前處理來源是彩色小AI
    public static boolean grabcutWithDnnAI = false; // 指示目前處理來源是DNN 大AI
    public static boolean grabcutWithInteraction = false; // 指示目前處理來源是人工手動
    public static int uploadRawfile = CONSTANT_UPLOAD_RAW; // 是否上傳 depth(2MB)及 thermal(76KB)

    public static double blue_width_pixel = 0.0; // 估算目前藍標籤在image橫向占的pixels
    public static double blue_height_pixel = 0.0; // 估算目前藍標籤在image直向占的pixels
    public static double blue_area = 0.0; // 估算目前藍標籤在image面積占的pixels

    public static boolean correctionColorDetected = false; // 是否偵測到藍圈內灰的校色卡
    public static int[][] correctionColor = new int[5][3]; // 是否偵測到藍圈內灰的校色卡, 灰卡的RGB色偏值
    public static int correctionDefaultGray = -1; // 是否偵測到藍圈內灰的校色卡, 預設灰卡的R=G=B色值,default gray value of b=g=r, if -1 means no assign
    public static boolean correctionColoring = false; // no longer in use
    public static double correctionColorAlpha = 0.0; // no longer in use
    public static double correctionColorBeta = 0.0; // no longer in use

    public static List<Map> recordList = new ArrayList<>(); // no longer in use
    public static Mat lookupTableThermalColorMap = null; // 熱感Y16數值轉成 256彩色的 lookup table
    public static Mat lookupTableDepthColorMap = null; // 深度Y12數值轉成 256彩色的 lookup table
    public static String lastColorJpegPath = ""; // 最後彩色照片檔名及儲存路徑
    public static String lastTxtFilePath = "";  // 最後描述檔名及儲存路徑
    public static HashMap<String,Long> lastUIClickTime = new HashMap<String,Long>(); // 記錄UI last click時間, 用來避免快速連續點擊

    public static boolean IS_FOR_MIIS_MPDA = CONSTANT_IS_FOR_MIIS_MPDA; // 是否為MPDA, 某些動作有關 (自動登出)
    public static boolean IS_FOR_IMAS_BOX = CONSTANT_IS_FOR_IMAS_BOX; // 是否為醫咖Go, 可不輸入患者ID及部位, 到後台再統一歸戶

    public static boolean IS_USED_MARKER_DETECTION = false; // 是否要檢查照片中有無藍標籤
    public static long detectedMarkerUptimeMillis = 0; // 藍標籤最後偵測到的時間
    public static long detectedMarkerStep = 0; // 藍標籤最後偵測到的穩定次數
    public static int detectMarkerFrameW = 0; // 藍標籤最後偵測到的寬
    public static int detectMarkerFrameH = 0; // 藍標籤最後偵測到的高
    public static int detectBrightness = 0; // 偵測到的環境亮度

    public static int detectMarkerX = 0; // 藍標籤最後偵測到的位置X
    public static int detectMarkerY = 0; // 藍標籤最後偵測到的位置Y
    public static int detectMarkerW = 0; // 藍標籤最後偵測到的寬
    public static int detectMarkerH = 0; // 藍標籤最後偵測到的高
    public static int detectMarkerA = 0; // 藍標籤最後偵測到的面積
    public static int detectMarkerHSVRangeH0 = 100;  // 藍標籤色偏 min hue 0 ~ 180
    public static int detectMarkerHSVRangeH1 = 120;  // 藍標籤色偏 max hue 0 ~ 180
    public static int detectMarkerHSVRangeS0 = 90;   // 藍標籤色飽和 min saturation 0 ~ 255
    public static int detectMarkerHSVRangeS1 = 255;  // 藍標籤色飽和 max saturation 0 ~ 255
    public static int detectMarkerHSVRangeV0 = 25;   // 藍標籤亮度 min value 0 ~ 255
    public static int detectMarkerHSVRangeV1 = 230;  // 藍標籤亮度 max value 0 ~ 255
    public static double touchFocusXScale = 0.5; // 觸碰畫面的對焦位置X相對畫面比例(jpg and focus)
    public static double touchFocusYScale = 0.5; // 觸碰畫面的對焦位置Y相對畫面比例(jpg and focus)

    public static double touchPointThermalCelsius = 0.0; // 觸碰畫面位置換算出來的溫度值
    public static double touchPointThermalOffsetCelsius = 0.0; // 觸碰畫面位置換算出來的溫度值加減值
    public static double touchPointXp = 0.5; // 觸碰畫面的位置X相對畫面比例(thermal,depth)
    public static double touchPointYp = 0.5; // 觸碰畫面的位置Y相對畫面比例(thermal,depth)
    public static int touchPointThermalFormula = CONSTANT_THERMAL_FORMULA; // FLIR 熱感模組是 3.0, 2.0, 或 3.5, 2.5 指令
    public static int touchPointThermalBaseCelsius = 33; // 熱感溫度顯示彩色的模式  maxmin base(center)
    public static int touchPointThermalBaseCelsiusRange = 7; // 熱感溫度顯示彩色的模式 maxmin base(center) +- range
    public static int touchPointThermalDisplay = 0; // 熱感溫度顯示彩色的模式 0: auto gan, 1: 27~38℃, 2: center+-7, 3: center+-1, 4: center+-0.5
    public static double touchPointDepthCentiMeter = 30.0; // 對焦中央點拍攝距離 default 30.0cm
    public static double touchPointDepthCentiMeterAvg = 30.0; // 對焦中央小區域多點平均拍攝距離 default 30.0cm

    public static int lastPicRValue = 0; // 最後一次jpg preview中央點的 RGB R值
    public static int lastPicGValue = 0; // 最後一次jpg preview中央點的 RGB G值
    public static int lastPicBValue = 0; // 最後一次jpg preview中央點的 RGB B值

    public static long lastPicSnapshotTimems = 0;  // 最後一次取得JPG的時間
    public static long lastDepthSnapshotTimems = 0; // 最後一次取得depth的時間
    public static byte[] lastDepthSnapshotBytes = null; // 最後一次取得depth的byte[]資料

    public static long lastThermalSnapshotTimems = 0; // 最後一次熱感拍照的時間
    public static Mat lastThermalSnapshotMat = null; // 最後一次熱感拍照的Y16 matrix
    public static boolean lastColorOnFrame = false; // 按下拍照鈕後, color frame是否已取得
    public static boolean lastDepthOnFrame = false; // 按下拍照鈕後, depth frame是否已取得
    public static boolean lastThermalOnFrame = false; // 按下拍照鈕後, depth frame是否已取得
    public static int initializedFrame = 0;
    public static double lastPicFocusXScale = 0.5; // 觸碰畫面的位置X相對畫面比例(jpg and focus)
    public static double lastPicFocusYScale = 0.5; // 觸碰畫面的位置Y相對畫面比例(jpg and focus)
    public static boolean lastPicNeedRotate = false; // 最後color照片是否需要旋轉且未完成
    public static boolean isTakingPicture = false; // 是否正在拍照程序
    public static boolean isGifDemo = false; // no longer in use, 是否要啟動 動態 GIF
    public static boolean isPiModule = false; // no longer in use, 是否要啟動 PI 模組
    public static boolean isUvcDevice = true; // 是否要啟動 thermal USB 相機
    public static boolean isMultiCam = true; // 是否要啟動 depth MIPI /w camera api2 相機

    public static boolean isUvcDeviceOK = false; //檢查3D timer
    public static boolean nonzero = false; //是否會傳黑畫面

    public static int thermalImageRotateAngle = CONSTANT_THERMAL_IMAGE_ROTATE_ANGLE; // 取得的 熱感影像是否轉角度
    public static int webrtcMinPreviewFps = 7000; // color preview的最小 fps
    public static int webrtcFrameNo = 0; // color preview 的第幾次frame,  用來控制幾張frame做一次處理, 避免每張frame都處理, 花太多cpu
    public static int thermalFrameNo = 0; // thermal preview 的第幾次frame,  用來控制幾張frame做一次處理, 避免每張frame都處理, 花太多cpu
    public static int depthFrameNo = 0; // depth preview 的第幾次frame,  用來控制幾張frame做一次處理, 避免每張frame都處理, 花太多cpu
    public static int uvcCameraIndex = 0; // 多個uvc cam時要使用哪一個
    public static int touchRotate = 1; // 觸控畫面是否有旋轉
    public static double focusTargetSize = 1; //目標傷口長或寬大小pixels *GRABCAT_DOWNSAMPLE_RATE

    public static int GET_USB_DEVICE_DELAY_MS = 4000; //等待 USB device ready
    public static int GET_MIPI_DEVICE_DELAY_MS = 300; //等待 MIPI device ready
    public static int GET_IP_DELAY_MS = 3000; // no longer in use, PI 模組延遲取IP
    public static String FOCUS_AREA_TYPE = "shut"; //對焦模式: shut:拍照時對焦, touch:觸碰時對焦
    public static int FOUCS_AREA_OFFSET = 60; // no longer in use
    //    public static int GRABCAT_DOWNSAMPLE_RATE = 10; // no longer in use
    public static int START_DELAY = 3000; // no longer in use
    public static boolean WEBVIEW_PAGEREADY = false; // no longer in use
    public static int BLE_SCAN_TIMEOUT = 15000; // no longer in use
    public static int RFID_LOOP_SLEEP = 30; // no longer in use
    public static int BARCODE_LOOP_SLEEP = 30; // no longer in use
    public static String whitebalanceMode = "auto"; // no longer in use


    // 以下是回傳 key 值的 constant
    public static final String KEY_PLAY_BEEP = "preferences_play_beep";
    public static final String PREFERENCE_EVALUATION_CASE_ID = "preferences_evaluation_case_id";
    public static final String PREFERENCE_EVALUATION_OWNER_ID = "preferences_evaluation_owner_id";

    public static final String KEY_LAST_LEAVE_URL = "keynlastleaveurl";
    public static final String KEY_RECEIVER_DATA_TYPE = "keynickreceiverdatatype";
    public static final String KEY_RECEIVER_MESSAGE = "keynickreceivermessage";
    public static final String KEY_RECEIVER_FLAG = "keynickreceiverflag";
    public static final String KEY_RECEIVER_PATIENT_ID = "keynickreceiverpatientId";
    public static final String KEY_RECEIVER_NURSE_ID = "keynickreceivernurseId";
    public static final String KEY_RECEIVER_MEASURE_DATA = "keynickreceivermessage.measure.data";


    public static final String MSG_NURSE_NAME = "message.nurse.name";
    public static final String MSG_OWNER_NAME = "message.owner.name";
    public static final String MSG_ROOM_NAME = "message.room.name";

    public static int CHOOSE_BODY_RESULT_OK = 10;
    public static int SCAN_RESULT_OK = 20;
    public static int SELECT_TAKEPICINFO_OK = 30;
    public static int SUCCESS_LOGOUT = 40;
    public final static int VIDEO_CALL_SENT = 666;

    public static final int DEBUG_IDLE = 0;
    public static final int DEBUG_APPRTC = 1000;
    public static final int DEBUG_MARKER = DEBUG_APPRTC + 1;
    public static final int DEBUG_GRABCUT = DEBUG_MARKER + 1;
    public static final int DEBUG_COLOR_CORRECT = DEBUG_GRABCUT + 1;
    public static final int DEBUG_ORIGN_MASK = DEBUG_COLOR_CORRECT + 1;
    public static final int DEBUG_AI_RESPONSE = DEBUG_ORIGN_MASK + 1;
    public static final int DEBUG_WOUND_INRANGE = DEBUG_AI_RESPONSE + 1;

    public static final int STATUS_START = 1000;
    public static final int STATUS_STOP = STATUS_START + 1;
    public static final int STATUS_RELOAD_BTMAC = STATUS_STOP + 1;
    public static final int STATUS_UPLOAD_RUNNING = STATUS_RELOAD_BTMAC + 1;
    public static final int STATUS_UPLOAD_FINISHED = STATUS_UPLOAD_RUNNING + 1;
    public static final int STATUS_UPLOAD_ERROR = STATUS_UPLOAD_FINISHED + 1;
    public static final int STATUS_REFRESH_UI = STATUS_UPLOAD_ERROR + 1;

    public static final int STATUS_CONNECTION_FORA_DEVICE = 2000 + 1;
    public static final int STATUS_DISCONNECTION_FORA_DEVICE = STATUS_CONNECTION_FORA_DEVICE + 1;
    public static final int STATUS_CONNECTION_WITH_INIT_DEVICE = STATUS_DISCONNECTION_FORA_DEVICE + 1;
    public static final int STATUS_UPDAE = STATUS_CONNECTION_WITH_INIT_DEVICE + 1;
    public static final int STATUS_BLE_DEVICE_DOWNLOAD_FINISHED = STATUS_UPDAE + 1;
    public static final int STATUS_LOGIN_SUCCESS = STATUS_BLE_DEVICE_DOWNLOAD_FINISHED + 1;
    public static final int STATUS_LOGIN_FAIL = STATUS_LOGIN_SUCCESS + 1;

    public static final int STATUS_DL_NURSE_SUCCESS = STATUS_LOGIN_FAIL + 1;
    public static final int STATUS_DL_NURSE_FAIL = STATUS_DL_NURSE_SUCCESS + 1;

    public static final int STATUS_DL_OWNER_SUCCESS = STATUS_DL_NURSE_FAIL + 1;
    public static final int STATUS_DL_OWNER_FAIL = STATUS_DL_OWNER_SUCCESS + 1;
    public static final int STATUS_BARCODE_SUCCESS = STATUS_DL_OWNER_FAIL + 1;
    public static final int REQUEST_CODE_OCR = STATUS_BARCODE_SUCCESS + 1;
    public static final int STATUS_BARCODE_RECORD_SUCCESS = REQUEST_CODE_OCR + 1;
    public static final int REQUEST_CODE_RECORD_OCR = STATUS_BARCODE_RECORD_SUCCESS + 1;

    public static final int JAVASCRIPT_WEBVIEW_PAGEREADY = 4000;
    public static final int JAVASCRIPT_WEBVIEW_PAGEEVENT = JAVASCRIPT_WEBVIEW_PAGEREADY + 1;
    public static final int JAVASCRIPT_WEBVIEW_UPLOADDATA = JAVASCRIPT_WEBVIEW_PAGEREADY + 2;
    public static final int JAVASCRIPT_WEBVIEW_STOREDATA = JAVASCRIPT_WEBVIEW_PAGEREADY + 3;
    public static final int JAVASCRIPT_LAST_KEYBOARD_ID = JAVASCRIPT_WEBVIEW_PAGEREADY + 4;
    public static final int JAVASCRIPT_OPEN_URL = JAVASCRIPT_WEBVIEW_PAGEREADY + 5;
    public static final int JAVASCRIPT_TESTING = JAVASCRIPT_WEBVIEW_PAGEREADY + 6;
    public static final int JAVASCRIPT_GET_ALL_USERDATA = JAVASCRIPT_WEBVIEW_PAGEREADY + 7;
    public static final int JAVASCRIPT_CLOSE_APP = JAVASCRIPT_WEBVIEW_PAGEREADY + 8;
    public static String IMAGE_PATH = "";

    // 以下是 函式
    public AppResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver, Context context) {
        mReceiver = receiver;
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

    public static void setAppLocale(Activity activity, String localeCode) {
        Resources resources = activity.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(new Locale(localeCode.toLowerCase()));
        } else {
            config.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void initOrientation(Activity activity) {
        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mMainActivity);
            String orientation = prefs.getString("screen_orientation", "Null");
            if ("Landscape".equals(orientation)) {
                //Landscape 螢幕保持橫向
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//                mParameters.setRotation(rotation);
            } else {
                //Portrait 螢幕保持直向
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (Exception e) {

        }

        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mMainActivity);
            DEBUG_LEVEL = Integer.parseInt(prefs.getString(KEY_PREF_DEBUG_LEVEL, "0"));
        } catch (Exception e) {
        }

        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mMainActivity);
            touchPointThermalDisplay = Integer.parseInt(prefs.getString(KEY_PREF_THERMAL_TYPE, "0"));
        } catch (Exception e) {

        }

    }

//    public void onOrientationChanged(Parameters mParameters, int orientation) {
//        if (orientation == ORIENTATION_UNKNOWN) {
//            return;
//        }
//        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
//        android.hardware.Camera.getCameraInfo(cameraId, info);
//
//        orientation = (orientation + 45) / 90 * 90;
//        int rotation = 0;
//
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            rotation = (info.orientation - orientation + 360) % 360;
//        } else {  // back-facing camera
//            rotation = (info.orientation + orientation) % 360;
//        }
//        mParameters.setRotation(rotation);
//    }

    public static String getAppLanguage(Activity activity) {
        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mMainActivity);
            String ret = prefs.getString(SettingsFragment.KEY_PREF_APP_LANGUAGE, "zh-rTW");
            return ret;
        } catch (Exception e) {
            return "en";
        }
    }

    public static String getThermalBoardIP(Activity activity) {
        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mMainActivity);
            String ret = prefs.getString("thermalBoardIP", "140.96.170.103");
            return ret;
        } catch (Exception e) {

        }
        return "";
    }

    public static String getThermalType(Activity activity) {
        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mMainActivity);
            String ret = prefs.getString("thermal_type", "0");
            return ret;
        } catch (Exception e) {

        }
        return "";
    }

    public static String getFileRootPath(Activity activity) {
        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mMainActivity);
            String ret = prefs.getString(KEY_PREF_FILE_ROOT_PATH, DocumentsUtils.getAppRootOfSdCardRemovable(activity));
            return ret;
        } catch (Exception e) {

        }
        return "";
    }

    public static String initPreferences(Activity activity) {
        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mMainActivity);
            SettingsFragment.setDefault(prefs);
        } catch (Exception e) {

        }
        return "";
    }

    public static Mat getLutThermalColorMap(Context context) {
        if (lookupTableThermalColorMap != null) {
            return lookupTableThermalColorMap;
        } else {
            lookupTableThermalColorMap = new Mat();
            lookupTableThermalColorMap.create(256, 1, CvType.CV_8UC3);
            BufferedReader br = null;
            String line;

            try {
                AssetManager assetManager = context.getAssets();
                InputStream inputStream = assetManager.open("mldata/linear.lut");
                br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("I") || line.startsWith("/") || line.startsWith("\r") || line.startsWith("\n") || line.equals("") || line.startsWith("#")) {
                    } else {
                        String[] sss = line.replace("\t", ",").split(",");
                        lookupTableThermalColorMap.put(Integer.valueOf(sss[0]), 0, Integer.valueOf(sss[1]), Integer.valueOf(sss[2]), Integer.valueOf(sss[3]));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return lookupTableThermalColorMap;
        }
    }


    public static Mat getLutDepthColorMap(Context context) {
        if (lookupTableDepthColorMap != null) {
            return lookupTableDepthColorMap;
        } else {
            lookupTableDepthColorMap = new Mat();
            lookupTableDepthColorMap.create(256, 1, CvType.CV_8UC3);
            BufferedReader br = null;
            String line;

            try {
                AssetManager assetManager = context.getAssets();
                InputStream inputStream = assetManager.open("mldata/linear.lut");
                br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("I") || line.startsWith("/") || line.startsWith("\r") || line.startsWith("\n") || line.equals("") || line.startsWith("#")) {
                    } else {
                        String[] sss = line.replace("\t", ",").split(",");
                        lookupTableDepthColorMap.put(Integer.valueOf(sss[0]), 0, Integer.valueOf(sss[1]), Integer.valueOf(sss[2]), Integer.valueOf(sss[3]));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return lookupTableDepthColorMap;
        }
    }

    public static void vibrating(Context context) {
        try {
            //啟動震動，並持續指定的時間
            Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(30);
        } catch (Exception e) {
        }
    }

    public static void logoutPreExecute(Context context) {
        Toast.makeText(context, context.getString(R.string.alert_remember_logout), Toast.LENGTH_LONG).show();
    }

    public static void showToast(Activity context, String message) {
        context.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 250);
                    toast.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    //寫入文檔
    public static synchronized void writeToFile(String outFilename, String msg, boolean append) {
        BufferedWriter writer = null;
        try {
//            if (dataEncrypt)
//                msg = StringUtils.encryptByDES(msg);
            //建立檔名
            File textFile = new File(outFilename);
            writer = new BufferedWriter(new FileWriter(textFile, append));
            writer.write(msg);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.flush();
                // 關閉BufferedWriter
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public static synchronized String readFromFile(String outFilename) {
        FileInputStream fis = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            fis = new FileInputStream(outFilename);
            br = new BufferedReader(new InputStreamReader(fis, "UTF8"));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br!=null) {
                    br.close();
                }
            } catch (Exception e) {
            }
            try {
                if (fis!=null) {
                    fis.close();
                }
            } catch (Exception e) {
            }
        }

        String result = sb.toString();
//        if (!result.startsWith("{") && !result.startsWith("evl")) {
//            try {
//                result = StringUtils.decryptByDES(result);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        return result;
    }

//    public static void getFileDesctptionJsonText(JSONObject jsonObject, JSONArray infoArray){
////        {
////            "id":"1", "name":"歐文", "title":"json 解析", "tag":"android json",
////            "info":[
////            {"id":"1", "title":"json object 1", "tag":"android json", "info":"1234567890" },
////            {"id":"2",  "title":"json object 2", "tag":"android json", "info":"abcdefg" },
////            {"id":"3",  "title":"json object 3", "tag":"android json", "info":"qwerty" }
////            ]
////        }
//
//        try{
//            //建立一個JSONObject並帶入JSON格式文字，getString(String key)取出欄位的數值
//            JSONObject detailObject = new JSONObject();
//            detailObject.put("name","");
//            detailObject.put("title","");
//            detailObject.put("tag","");
//
//            infoArray.put( detailObject);
//
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void parseFileDesctptionJsonText(String jsonText){
////        {
////            "id":"1", "name":"歐文", "title":"json 解析", "tag":"android json",
////            "info":[
////            {"id":"1", "title":"json object 1", "tag":"android json", "info":"1234567890" },
////            {"id":"2",  "title":"json object 2", "tag":"android json", "info":"abcdefg" },
////            {"id":"3",  "title":"json object 3", "tag":"android json", "info":"qwerty" }
////            ]
////        }
//
//        try{
//            //建立一個JSONObject並帶入JSON格式文字，getString(String key)取出欄位的數值
//            JSONObject jsonObject = new JSONObject(jsonText);
//            String name = jsonObject.getString("name");
//            String title = jsonObject.getString("title");
//            String tag = jsonObject.getString("tag");
//
//            JSONArray array = jsonObject.getJSONArray("info");
//            for (int i = 0; i < array.length(); i++) {
//                jsonObject = array.getJSONObject(i);
//                  title = jsonObject.getString("title");
//                  tag = jsonObject.getString("tag");
//                Log.d("TAG", "title:" + title + ", tag:" + tag );
//            }
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//        }
//    }


}
