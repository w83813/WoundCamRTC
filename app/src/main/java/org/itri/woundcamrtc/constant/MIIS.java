package org.itri.woundcamrtc.constant;

public class MIIS {
    //晉弘       MiiS
    public static final String CONSTANT_APP = "MIIS";
    public static final String CONSTANT_ZONE = "MIIS";
    public static final String CONSTANT_PROJECT_PATH = "woundcare";
    public static final String CONSTANT_SERVER_IP = "192.168.1.10"; //記得 preferences.xml也要改
    public static final String CONSTANT_SERVER_IP_PORT = "http://192.168.1.10:8080";
    public static final String CONSTANT_AI_SERVER_IP_PORT = "http://192.168.1.10:8007";
    public static final String CONSTANT_POST_AI_COLOR_IMAGE_PATH =  CONSTANT_AI_SERVER_IP_PORT + "/WoundService/api/v1.0.0/inference?location=" + CONSTANT_ZONE;

    public static final String CONSTANT_WEBSOCKET_URL = "http://" + CONSTANT_SERVER_IP + ":8300";
    public static final String CONSTANT_WEBRTC_URL = "http://" + CONSTANT_SERVER_IP + ":8180"; //http://140.96.170.75:8180(for better tablet) http://140.96.170.76:8080
    public static final String CONSTANT_STUN_URL = "stun:" + CONSTANT_SERVER_IP + ":3478";
    public static final String CONSTANT_VERSION_CHECK_URL = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/appUpdateJson?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_WEBVIEW_URL = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/device/authloginget?uid=admin&pwd=123456&target=mobilePage&param=";


    public static final String CONSTANT_LOGIN_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/connect/v3/api/userLogin?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_UPDATE_PASS_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/connect/v3/api/resetPassword?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_QRY_PATIENTINFO_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/connect/v3/api/qryPatientInfoByApp?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_UPLOAD_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/connect/v3/api/repository/uploadPhoto?location=" + CONSTANT_ZONE;;
    public static final String CONSTANT_QRY_DATE_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/connect/v3/api/qryApp?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_QRY_RECORD_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/connect/v3/api/qryWoundRecordByDate?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_QRY_PATIENTNOLIST_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/connect/v3/api/qrySimilarPatientNo?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_LOADIMG_PATH = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH;
    public static final String CONSTANT_PING_SERVER_URL = CONSTANT_SERVER_IP_PORT + "/" + CONSTANT_PROJECT_PATH + "/widget/menus?location=" + CONSTANT_ZONE;
    public static final String CONSTANT_PING_INTERNET_URL = "https://www.google.com/";

    public static final boolean CONSTANT_DEMO_WEBRTC = false;
    public static final String CONSTANT_WEBRTC_ROOMID = "12345";
    public static final boolean CONSTANT_CHECK_INTERNET = false;

    public static final int CONSTANT_THERMAL_FORMULA = 35;
    public static final int CONSTANT_THERMAL_IMAGE_ROTATE_ANGLE = -90;
    public static final boolean CONSTANT_IS_FOR_IMAS_BOX = false;

    public static final boolean CONSTANT_IS_FOR_MIIS_MPDA = true;


    public static final boolean CONSTANT_DATA_ENCRYPT = false;
    public static final int CONSTANT_UPLOAD_RAW = 1;
}
