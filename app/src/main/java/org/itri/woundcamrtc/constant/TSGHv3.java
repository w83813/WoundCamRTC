package org.itri.woundcamrtc.constant;

public class TSGHv3 {
    //服科中心       ITRI
    public static final String CONSTANT_APP = "TSGH";
    public static final String CONSTANT_ZONE = "NT";
    public static final String CONSTANT_PROJECT_PATH = "woundcare";
    public static final String CONSTANT_SERVER_IP = "60.250.3.252"; //記得 preferences.xml也要改192.168.50.12
    public static final String CONSTANT_SERVER_IP_PORT = "http://" + CONSTANT_SERVER_IP + ":8080";
    public static final String CONSTANT_AI_SERVER_IP_PORT = "http://60.250.3.251:8007";
    public static final String CONSTANT_POST_AI_COLOR_IMAGE_PATH =  CONSTANT_AI_SERVER_IP_PORT + "/WoundService/api/v1.0.0/inference?location=" + CONSTANT_ZONE;

    public static final String CONSTANT_WEBSOCKET_URL = "http://" + CONSTANT_SERVER_IP + ":8300";
    public static final String CONSTANT_WEBRTC_URL = "http://" + CONSTANT_SERVER_IP + ":8180";
    public static final String CONSTANT_STUN_URL = "stun:" + CONSTANT_SERVER_IP + ":3478";
    public static final String CONSTANT_VERSION_CHECK_URL = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/appUpdateJson?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_WEBVIEW_URL = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/wound/tsgh/v3/api/webviewLogin?target=casepage&access=true&from=app&location=" + CONSTANT_ZONE;

    public static final String CONSTANT_LOGIN_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/wound/tsgh/v3/api/userLogin?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_UPDATE_PASS_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/wound/tsgh/v3/api/resetPassword?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_QRY_PATIENTINFO_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/wound/tsgh/v3/api/qryPatientInfoByApp?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_UPLOAD_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/wound/tsgh/v3/api/repository/uploadPhotoV2?location=" + CONSTANT_ZONE;;
    public static final String CONSTANT_QRY_DATE_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/wound/tsgh/v3/api/qryApp?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_QRY_RECORD_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/wound/tsgh/v3/api/qryWoundRecordByDate?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_QRY_PATIENTNOLIST_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/wound/tsgh/v3/api/qrySimilarPatientNo?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_LOADIMG_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH;
    public static final String CONSTANT_PING_SERVER_URL = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/widget/menus?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_PING_INTERNET_URL = "https://www.google.com/";

    public static final boolean CONSTANT_DEMO_WEBRTC = false;
    public static final String CONSTANT_WEBRTC_ROOMID = "12345";
    public static final boolean CONSTANT_CHECK_INTERNET = false;

    public static final int CONSTANT_THERMAL_FORMULA = 30; // FLIR 熱感模組是 3.0, 2.0, 或 3.5, 2.5 指令
    public static final int CONSTANT_THERMAL_IMAGE_ROTATE_ANGLE = 0; // 取得的 熱感影像是否轉角度
    public static final boolean CONSTANT_IS_FOR_IMAS_BOX = false; // 是否為醫咖Go, 可不輸入患者ID及部位, 到後台再統一歸戶
    public static final boolean CONSTANT_IS_FOR_MIIS_MPDA = false; // 是否為MPDA, 某些動作有關 (自動登出)
    public static final boolean CONSTANT_DATA_ENCRYPT = false; // 是否加密 image及 txt, sqlite db
    public static final int CONSTANT_UPLOAD_RAW = 0; // 是否上傳 thermal.raw, depth.raw
}
