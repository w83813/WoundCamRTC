package org.itri.woundcamrtc.constant;

public class TZUCHIv3MPD100 {
    //花蓮慈濟   TZUCHI
    public static final String CONSTANT_APP = "TZUCHI";
    public static final String CONSTANT_ZONE = "TC";
    public static final String CONSTANT_PROJECT_PATH = "woundcare";
    public static final String CONSTANT_SERVER_IP = "icare.itri.org.tw"; //記得 preferences.xml也要改
    public static final String CONSTANT_SERVER_IP_PORT = "https://icare.itri.org.tw";
    public static final String CONSTANT_AI_SERVER_IP_PORT = "https://140.96.170.74:8007";
    public static final String CONSTANT_POST_AI_COLOR_IMAGE_PATH =  CONSTANT_AI_SERVER_IP_PORT + "/WoundService/api/v1.0.0/inference?location=" + CONSTANT_ZONE;

    public static final String CONSTANT_WEBSOCKET_URL = "https://icare.itri.org.tw:8300";
    public static final String CONSTANT_WEBRTC_URL = "https://icare.itri.org.tw:8180";  //https://140.96.170.75:8300
    public static final String CONSTANT_STUN_URL = "stun:61.61.246.67:8478";
    public static final String CONSTANT_VERSION_CHECK_URL = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/appUpdateJson?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_WEBVIEW_URL = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/wound/tsgh/v3/api/webviewLogin?target=casepage&access=true&from=app";


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
    public static final boolean CONSTANT_INTEGRAT_IMAS = false;
    public static final String CONSTANT_WEBRTC_ROOMID = "12345";
    public static final boolean CONSTANT_CHECK_INTERNET = true;

    public static final int CONSTANT_THERMAL_FORMULA = 35;
    public static final int CONSTANT_THERMAL_IMAGE_ROTATE_ANGLE = -90;
    public static final boolean CONSTANT_IS_FOR_IMAS_BOX = false;
    public static final boolean CONSTANT_IS_FOR_MIIS_MPDA = false;
    public static final boolean CONSTANT_DATA_ENCRYPT = true;
    public static final int CONSTANT_UPLOAD_RAW = 0;
}
