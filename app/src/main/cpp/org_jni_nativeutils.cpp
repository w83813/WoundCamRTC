
#include <jni.h>
#include <string>
#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <opencv2/core.hpp>
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include "org_jni_nativeutils.h"
#include "Mesh3DAPI.h"
#include "ITRITriMesh.h"


#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR , "ProjectName", __VA_ARGS__)

//#include <opencv2/dnn_superres.hpp>
using namespace std;
using namespace cv;
//using namespace dnn;
//using namespace dnn_superres;


/*
 *  This is a 3D and thermal C++ Library.
 *
 *  拍照後產生彩色RGB888 jpg, 熱感RGB888 png, 景深Y16 raw, 熱感Y16 raw,
 *  在按下「傷口互動圈選」右下「估算」, 或「照片分析」的左方3D, 會產生obj, mtl,
 *  計算 WoundAreaInfo 需要mask資訊
 *
 *  攝像機內參 ：確定攝像機從三維空間mesh到二維圖像depth的投影關係。f焦距,κ徑向畸變量級,Sx,Sy(縮放比例因子),Cx,Cy(徑向畸變的中心)
 *  攝像機外參 ：決定攝像機坐標與世界坐標系之間相對位置關係。(Tx,Ty,Tz)，是平移向量，(α,β,γ)是旋轉矩陣
 *  ----- structure -----
 *  DepthCameraParam  記錄depth camera主要的內參外參
 *  BoundingBox 預設尺寸面積的計算範圍
 *  ThermalAttMeshParam 熱感Y16與三維景深Y16的鏡頭成像疊合參數, c中央, s成像面偏移, t空間軸偏移
 *  RGBAttMeshParam 彩色RGB888、熱感RGB888與三維景深Y16的鏡頭成像疊合參數, c中央, s成像面偏移, t空間軸偏移
 *  WoundAreaInfo 計算完的回傳結構 高低溫、面積、最深深度
 *
 *  -----  Main object  ------
 *  ITRITriMesh 根據相機內參外參計算完的3D景深，轉換成為的點雲資料
 *
 *  -----  Main function  ------
 *  DepthToTriMesh 根據相機內參外參轉換3D景深成為的點雲資料
 *  TriMeshAttRGBIMG 彩色RGB888與景深Y16疊圖
 *  TriMeshAttThermalIMG 熱感熱感Y16與景深Y16疊圖
 *  SaveToObjFile 儲存3D檔, .obj 與 .mtl
 *
 *  ----- Utility function
 *  CalWoundAreaInfo 配合mask計算範圍內的高低溫度、深度、面積, 需同時疊完彩色RGB888、熱感Y16、景深Y16才能算
 *  GetTwoPointsDistOnTexture 計算2組xy間的距離, 需景深Y16才能算
 *  GetPointThermalOnTexture 計算1點xy對應的溫度, 需熱感Y16才能算
 *  GetPointDepthOnTexture 計算1點xy對應的深度值, 需景深Y16才能算
 *  BoundClipDepth 消除雜點
 *  FillDepthHV 線性補洞
 *
 * //https://codertw.com/android-%E9%96%8B%E7%99%BC/27928/
*/
bool inited = false;
int sample_step = 2;
int img_width = 1280;
int img_height = 800;
DepthCameraParam m_dcp_param;
BoundingBox bbox;
RGBAttMeshParam m_png_param;
RGBAttMeshParam m_jpg_param;
ThermalAttMeshParam m_thermal_param;

void initParam() {
    if (inited == false) {
        m_dcp_param.FOCAL_LENGTH_X = 835.2802734375;
        m_dcp_param.FOCAL_LENGTH_Y = 835.9163208008;
        m_dcp_param.PRINCIPAL_X = 409.8193359375;
        m_dcp_param.PRINCIPAL_Y = 636.9195556641;

        bbox.min_x = -200;
        bbox.max_x = 200;
        bbox.min_y = -300;
        bbox.max_y = 300;
        bbox.min_z = 100;
        bbox.max_z = 600;

        m_thermal_param.img_width = 1280;
        m_thermal_param.img_height = 800;
        m_thermal_param.cx = 64;
        m_thermal_param.cy = 82;
        m_thermal_param.sx = 0.49;
        m_thermal_param.sy = 0.49;
        m_thermal_param.tx = 0.0022;
        m_thermal_param.ty = 0.0022;
        m_thermal_param.tz = -300.0;

        m_png_param.img_width = 120;
        m_png_param.img_height = 160;
        m_png_param.cx = 64;
        m_png_param.cy = 82;
        m_png_param.sx = 0.49;
        m_png_param.sy = 0.49;
        m_png_param.tx = 0.0022;
        m_png_param.ty = 0.0022;
        m_png_param.tz = -300.0;

        m_jpg_param.img_width = 1280;
        m_jpg_param.img_height = 800;
        //m_jpg_param.img_width = 2448;
        //m_jpg_param.img_height = 3264;
        m_jpg_param.cx = 1340;
        m_jpg_param.cy = 1453;
        m_jpg_param.sx = 8.3;
        m_jpg_param.sy = 8.3;
        m_jpg_param.tx = 0.023;
        m_jpg_param.ty = 0.023;
        m_jpg_param.tz = -300.01;

        inited = true;
    }
}

void focalLensParam(double focal_length_x, double focal_length_y, double principal_x,
                    double principal_y) {
    initParam();
    m_dcp_param.FOCAL_LENGTH_X = focal_length_x;
    m_dcp_param.FOCAL_LENGTH_Y = focal_length_y;
    m_dcp_param.PRINCIPAL_X = principal_x;
    m_dcp_param.PRINCIPAL_Y = principal_y;
}

void boundingBoxParam(double min_x, double max_x, double min_y, double max_y, double min_z,
                      double max_z) {
    initParam();
    bbox.min_x = min_x;
    bbox.max_x = max_x;
    bbox.min_y = min_y;
    bbox.max_y = max_y;
    bbox.min_z = min_z;
    bbox.max_z = max_z;
}

void printCalibracionParam() {
    LOGE("calibracionParam: %d, %d, %d, %d, %.3f, %.3f, %.3f, %.3f, %.3f ", m_jpg_param.img_width,
         m_jpg_param.img_height, m_jpg_param.cx,
         m_jpg_param.cy, m_jpg_param.sx, m_jpg_param.sy, m_jpg_param.tx,
         m_jpg_param.ty, m_jpg_param.tz);
}

extern "C" JNIEXPORT void JNICALL
Java_org_jni_NativeUtils_calibracionParam(JNIEnv *env, jclass cls, jobject obj, jint type,
                                          jint img_width,
                                          jint img_height, jint cx, jint cy, jdouble sx,
                                          jdouble sy, jdouble tx, jdouble ty, jdouble tz) {
    initParam();
    if (type == 1) {
        m_jpg_param.img_width = img_width;
        m_jpg_param.img_height = img_height;
        m_jpg_param.cx = cx;
        m_jpg_param.cy = cy;
        m_jpg_param.sx = sx;
        m_jpg_param.sy = sy;
        m_jpg_param.tx = tx;
        m_jpg_param.ty = ty;
        m_jpg_param.tz = tz;
    } else if (type == 2) {
        m_png_param.img_width = img_width;
        m_png_param.img_height = img_height;
        m_png_param.cx = cx;
        m_png_param.cy = cy;
        m_png_param.sx = sx;
        m_png_param.sy = sy;
        m_png_param.tx = tx;
        m_png_param.ty = ty;
        m_png_param.tz = tz;
    } else if (type == 3) {
        m_thermal_param.img_width = img_width;
        m_thermal_param.img_height = img_height;
        m_thermal_param.cx = cx;
        m_thermal_param.cy = cy;
        m_thermal_param.sx = sx;
        m_thermal_param.sy = sy;
        m_thermal_param.tx = tx;
        m_thermal_param.ty = ty;
        m_thermal_param.tz = tz;
    }
    printCalibracionParam();
}

// Get pointer field straight from `JavaClass`
jfieldID getPtrFieldId(JNIEnv *env, jobject obj) {
    static jfieldID ptrFieldId = 0;

    if (!ptrFieldId) {
        jclass c = env->GetObjectClass(obj);
        ptrFieldId = env->GetFieldID(c, "objPtr", "J");
        env->DeleteLocalRef(c);
    }

    return ptrFieldId;
}

extern "C" JNIEXPORT jstring JNICALL Java_org_jni_NativeUtils_Version(JNIEnv *env, jclass obj) {
    std::string version = "NativeUtils 110";
    return env->NewStringUTF(version.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_createObject(JNIEnv *env, jclass cls, jobject obj) {
    ITRITriMesh *pMesh = (ITRITriMesh *) env->GetLongField(obj, getPtrFieldId(env, obj));
    if (pMesh == 0) {
        pMesh = new ITRITriMesh();
        env->SetLongField(obj, getPtrFieldId(env, obj), (jlong) pMesh);
    }

    initParam();

    std::string result = "success=true\n";
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_deleteObject(JNIEnv *env, jclass cls, jobject obj) {
    ITRITriMesh *pMesh = (ITRITriMesh *) env->GetLongField(obj, getPtrFieldId(env, obj));
    if (pMesh == 0) {
        delete pMesh;
        env->SetLongField(obj, getPtrFieldId(env, obj), (jlong) 0);
    }
    std::string result = "success=true\n";
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_getThermalOnXY2(JNIEnv *env, jclass cls, jobject obj, int x, int y) {
    std::string result = "success=false\n";
    ITRITriMesh *pMesh = (ITRITriMesh *) env->GetLongField(obj, getPtrFieldId(env, obj));
    if (pMesh != 0) {
        float ret = GetPointThermalOnTexture(pMesh, x, y, 120, 160);
        std::ostringstream stringStream;
        stringStream << "success=true\n" << "temp=" << ret << "\n";
        result = stringStream.str();
    }
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_genTriMesh2(JNIEnv *env, jclass cls, jobject obj, jshortArray jDepthY16) {
    ITRITriMesh *pMesh = (ITRITriMesh *) env->GetLongField(obj, getPtrFieldId(env, obj));
    if (pMesh == 0) {
        pMesh = new ITRITriMesh();
        env->SetLongField(obj, getPtrFieldId(env, obj), (jlong) pMesh);
    }

    // Write your code to work on CppObject here
    //取得 Y16 depth array, ushort pDepth[w][h]
    jshort *cDepthY16 = env->GetShortArrayElements(jDepthY16, NULL);
    int len_arr = env->GetArrayLength(jDepthY16);
    unsigned short *pDepthY16 = reinterpret_cast<unsigned short *>(cDepthY16); //pDepth[0] ~ pDepth[n]
    LOGE("step 1: %d(len of pDepthY16), %hu(value of pDepthY16[700,400])", len_arr,
         pDepthY16[1280 * 400 + 700]);

    initParam();
    LOGE("step 2: initParam()");

    std::string result = "success=true\n";

    // Remove Noise Data
    BoundClipDepth(pDepthY16, img_width, img_height, 200, 500);

    // fill hole (liner fill)
    FillDepthHV(pDepthY16, img_width, img_height, 100, 100);

    //depth轉為mesh
    int rslMesh = DepthToTriMesh(pDepthY16, img_width, img_height, m_dcp_param, bbox, sample_step,
                                 pMesh);
    LOGE("step 3 DepthToTriMesh: %d ", rslMesh);

    if (rslMesh != 1)
        result = "success=false\n";

    env->ReleaseShortArrayElements(jDepthY16, cDepthY16, 0);
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_gen3DThermalImage2(JNIEnv *env, jclass cls, jobject obj,
                                            jstring jFilePath, jstring jFile3dMainNameThm,
                                            jstring jPicFileNamePng) {
    ITRITriMesh *pMesh = (ITRITriMesh *) env->GetLongField(obj, getPtrFieldId(env, obj));
    if (pMesh == 0) {
        pMesh = new ITRITriMesh();
        env->SetLongField(obj, getPtrFieldId(env, obj), (jlong) pMesh);
    }

    // Write your code to work on CppObject here
    std::string result = "success=true\n";

    //取得 File path
    const char *cFilePath = env->GetStringUTFChars(jFilePath, NULL);
    string sFilePath(cFilePath);
    LOGE("step 1: %d", strlen(cFilePath));

    const char *cFile3dMainNameThm = env->GetStringUTFChars(jFile3dMainNameThm, NULL);
    string sFile3dMainNameThm(cFile3dMainNameThm);
    LOGE("step 1 cFile3dMainNameThm: %d", strlen(cFile3dMainNameThm));

    const char *cPicFileNamePng = env->GetStringUTFChars(jPicFileNamePng, NULL);
    string sPicFileNamePng(cPicFileNamePng);
    LOGE("step 1 sPngFileName: %d", strlen(cPicFileNamePng));

    // attached thermal image
    int rgb_rsl = TriMeshAttRGBIMG(pMesh, m_png_param, const_cast<char *>(cPicFileNamePng), 120,
                                   160);
    LOGE("step 4 TriMeshAttRGBIMG: %d ", rgb_rsl);
    int rslSaveThmObj = pMesh->SaveToObjFile(const_cast<char *>(cFile3dMainNameThm),
                                             const_cast<char *>(cFilePath));
    LOGE("step 4 SaveToObjFile: %d", rslSaveThmObj);

    if (rslSaveThmObj != 1)
        result = "success=false\n";

    env->ReleaseStringUTFChars(jPicFileNamePng, cPicFileNamePng);
    env->ReleaseStringUTFChars(jFile3dMainNameThm, cFile3dMainNameThm);
    env->ReleaseStringUTFChars(jFilePath, cFilePath);

    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_gen3DColorImage2(JNIEnv *env, jclass cls, jobject obj,
                                          jstring jFilePath, jstring jFile3dMainNameJpg,
                                          jstring jPicFileNameJpg,
                                          jint jpgWidth, jint jpgHeight) {
    ITRITriMesh *pMesh = (ITRITriMesh *) env->GetLongField(obj, getPtrFieldId(env, obj));
    if (pMesh == 0) {
        pMesh = new ITRITriMesh();
        env->SetLongField(obj, getPtrFieldId(env, obj), (jlong) pMesh);
    }

    // Write your code to work on CppObject here
    std::string result = "success=true\n";

    //取得 File path
    const char *cFilePath = env->GetStringUTFChars(jFilePath, NULL);
    string sFilePath(cFilePath);
    LOGE("step 1: %d", strlen(cFilePath));

    const char *cFile3dMainNameJpg = env->GetStringUTFChars(jFile3dMainNameJpg, NULL);
    string sFile3dMainName3ds(cFile3dMainNameJpg);
    LOGE("step 1 cFile3dMainNameJpg: %d", strlen(cFile3dMainNameJpg));

    const char *cPicFileNameJpg = env->GetStringUTFChars(jPicFileNameJpg, NULL);
    string sPicFileNameJpg(cPicFileNameJpg);
    LOGE("step 1 cPicFileNameJpg: %d", strlen(cPicFileNameJpg));

    //mesh疊上rgb
    int rslRGB = TriMeshAttRGBIMG(pMesh, m_jpg_param, const_cast<char *>(cPicFileNameJpg), jpgWidth,
                                  jpgHeight);
    LOGE("step 3 TriMeshAttRGBIMG: %d ", rslRGB);
    int rslSave3dsObj = pMesh->SaveToObjFile(const_cast<char *>(cFile3dMainNameJpg),
                                             const_cast<char *>(cFilePath));
    LOGE("step 3 SaveToObjFile: %d", rslSave3dsObj);

    if (rslRGB != 1)
        result = "success=false\n";

    env->ReleaseStringUTFChars(jPicFileNameJpg, cPicFileNameJpg);
    env->ReleaseStringUTFChars(jFile3dMainNameJpg, cFile3dMainNameJpg);
    env->ReleaseStringUTFChars(jFilePath, cFilePath);

    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_calWoundAreaInfo2(JNIEnv *env, jclass cls, jobject obj,
                                           jstring jFilePath,
                                           jstring jPicFileNameJpg, jbyteArray jMaskY8,
                                           jint jpgWidth, jint jpgHeight, jint xx11, jint yy11,
                                           jint xx12, jint yy12, jint xx21, jint yy21, jint xx22,
                                           jint yy22) {

    printCalibracionParam();

    ITRITriMesh *pMesh = (ITRITriMesh *) env->GetLongField(obj, getPtrFieldId(env, obj));
    if (pMesh == 0) {
        pMesh = new ITRITriMesh();
        env->SetLongField(obj, getPtrFieldId(env, obj), (jlong) pMesh);
    }

    //取得 File path
    const char *cFilePath = env->GetStringUTFChars(jFilePath, NULL);
    string sFilePath(cFilePath);
    LOGE("step 1: %d", strlen(cFilePath));

    const char *cPicFileNameJpg = env->GetStringUTFChars(jPicFileNameJpg, NULL);
    string sPicFileNameJpg(cPicFileNameJpg);
    LOGE("step 1 cPicFileNameJpg: %d", strlen(cPicFileNameJpg));

    //取得 Y8 mask array, ubool pMask[w][h]
    jbyte *cMaskY8 = env->GetByteArrayElements(jMaskY8, 0);
    int len_arr = env->GetArrayLength(jMaskY8);
    unsigned char *pMaskY8 = reinterpret_cast<unsigned char *>(cMaskY8); //pMask[0] ~ pMask[n]
    LOGE("step 2: %d(len of pMaskY8), %d(value of pMaskY8[700,400])", len_arr,
         pMaskY8[1280 * 400 + 700]);

    //mesh疊上rgb
    int rslRGB = TriMeshAttRGBIMG(pMesh, m_jpg_param, const_cast<char *>(cPicFileNameJpg), jpgWidth,
                                  jpgHeight);
    LOGE("step 3 TriMeshAttRGBIMG: %d ", rslRGB);

    //透過wound area mask, 計算尺寸及最深
    WoundAreaInfo pInfo;
    int rslInfo = CalWoundAreaInfo(pMaskY8, jpgWidth, jpgHeight, pMesh, &pInfo);
    LOGE("step 5 CalWoundAreaInfo: %d ", rslInfo);
    LOGE("step 5 20200906");

    std::ostringstream stringStream;
    stringStream << "success=true\n" << "wound area=" << pInfo.wound_area << "\n";
    stringStream << "max depth=" << pInfo.wound_max_depth / 10.0 << "\n";
    stringStream << "hi temp=" << pInfo.wound_high_temp.temp << "\n";
    stringStream << "lo temp=" << pInfo.wound_low_temp.temp << "\n";

    double dist1 = GetTwoPointsDistOnTexture(pMesh, xx11, yy11, xx12, yy12, m_jpg_param.img_width,
                                             m_jpg_param.img_height);
    double dist2 = GetTwoPointsDistOnTexture(pMesh, xx21, yy21, xx22, yy22, m_jpg_param.img_width,
                                             m_jpg_param.img_height);
    LOGE("step 5 GetTwoPointsDistOnTexture1 %dx%d at (%d,%d)(%d,%d): %f ", m_jpg_param.img_width,
         m_jpg_param.img_height, xx11, yy11, xx12, yy12, dist1);
    LOGE("step 5 GetTwoPointsDistOnTexture2 %dx%d at (%d,%d)(%d,%d): %f ", m_jpg_param.img_width,
         m_jpg_param.img_height, xx21, yy21, xx22, yy22, dist2);

    if (dist1 > dist2) {
        stringStream << "long=" << dist1 / 10.0 << "\n";
        stringStream << "short=" << dist2 / 10.0 << "\n";
    } else {
        stringStream << "long=" << dist2 / 10.0 << "\n";
        stringStream << "short=" << dist1 / 10.0 << "\n";
    }

    std::string result = stringStream.str();
    LOGE("step 6 info: %s ", result.c_str());

    env->ReleaseByteArrayElements(jMaskY8, cMaskY8, 0);
    env->ReleaseStringUTFChars(jPicFileNameJpg, cPicFileNameJpg);
    env->ReleaseStringUTFChars(jFilePath, cFilePath);

    LOGE("step 7 release");
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_nativeGetWoundInfo(JNIEnv *env, jclass cls, jobject obj,
                                            jstring jFilePath, jshortArray jDepthY16,
                                            jbyteArray jJpg888, jstring jFile3dMainName3ds,
                                            jstring jPicFileNameJpg, jfloatArray jThermalY32,
                                            jbyteArray jPng888, jstring jFile3dMainNameThm,
                                            jstring jPicFileNamePng, jbyteArray jMaskY8,
                                            jint width, jint height, jint xx11, jint yy11,
                                            jint xx12, jint yy12, jint xx21, jint yy21, jint xx22,
                                            jint yy22, jboolean save) {

    bool success = true;
    LOGE("step 1: nativeGetWoundInfo");

    //取得 file path
    const char *cFilePath = env->GetStringUTFChars(jFilePath, NULL);
    string sFilePath(cFilePath);
    LOGE("step 1: cFilePath: %d %s", strlen(cFilePath), cFilePath);

    //取得 file main name of 3DS obj
    const char *cFile3dMainName3ds = env->GetStringUTFChars(jFile3dMainName3ds, NULL);
    string sFile3dMainName3ds(cFile3dMainName3ds);
    LOGE("step 1: cFile3dMainName3ds: %d %s", strlen(cFile3dMainName3ds), cFile3dMainName3ds);

    //取得 file name of rgb jpg
    const char *cPicFileNameJpg = env->GetStringUTFChars(jPicFileNameJpg, NULL);
    string sPicFileNameJpg(cPicFileNameJpg);
    LOGE("step 1: cPicFileNameJpg: %d %s", strlen(cPicFileNameJpg), cPicFileNameJpg);

    char *cFile3dMainNameThm = 0;
    char *cPicFileNamePng = 0;
    if (jThermalY32!=NULL) {
        //取得 file main name of thermal png
        cFile3dMainNameThm = const_cast<char *>(env->GetStringUTFChars(jFile3dMainNameThm, NULL));
        string sFile3dMainNameThm(cFile3dMainNameThm);
        LOGE("step 1: cFile3dMainNameThm: %d %s", strlen(cFile3dMainNameThm), cFile3dMainNameThm);

        //取得 file name of thermal png
        cPicFileNamePng = const_cast<char *>(env->GetStringUTFChars(jPicFileNamePng, NULL));
        string sPicFileNamePng(cPicFileNamePng);
        LOGE("step 1: sPngFileName: %d %s", strlen(cPicFileNamePng), cPicFileNamePng);
    }

    //取得 Y16 depth array, ushort pDepth[w][h], 1280x800
    jshort *cDepthY16 = env->GetShortArrayElements(jDepthY16, NULL);
    int len_arr = env->GetArrayLength(jDepthY16);
    unsigned short *pDepthY16 = reinterpret_cast<unsigned short *>(cDepthY16); //pDepth[0] ~ pDepth[n]
    LOGE("step 2: %d(len of pDepthY16 1280x800), %hu(value of pDepthY16[640,400])", len_arr,
         pDepthY16[1280 * 400 + 640]);

    //取得 JPG color array, uchar pColor[w][h][ch], 2448x3264 ->1280x800
    jbyte *cJpg888 = env->GetByteArrayElements(jJpg888, 0);
    len_arr = env->GetArrayLength(jJpg888);
    unsigned char *pJpg888 = reinterpret_cast<unsigned char *>(cJpg888); //pColor[0] ~ pColor[n]
    LOGE("step 2: %d(len of pJpg888 1280x800), %d(value of pJpg888[640,400])", len_arr,
         pJpg888[1280 * 400 + 640]);

    jfloat *cThermalY32 = 0;
    jbyte *cPng888 = 0;
    float *pThermalY32 = 0;
    unsigned char *pPng888 = 0;
    if (jThermalY32!=NULL) {
        //取得 Y32 thermal array, float pThermal[w][h], 160x120
        cThermalY32 = env->GetFloatArrayElements(jThermalY32, 0);
        len_arr = env->GetArrayLength(jThermalY32);
        pThermalY32 = reinterpret_cast<float *>(cThermalY32); //pThermal[0] ~ pThermal[n]
        LOGE("step 2: %d(len of pThermalY32 160x120), %.2f(value of pThermalY32[80,60])", len_arr,
             pThermalY32[160 * 60 + 80]);

        //取得 PNG color array, uchar pColor[w][h][ch], 120x160
        cPng888 = env->GetByteArrayElements(jPng888, 0);
        len_arr = env->GetArrayLength(jPng888);
        pPng888 = reinterpret_cast<unsigned char *>(cPng888); //pColor[0] ~ pColor[n]
        LOGE("step 2: %d(len of pPng888 120x160), %d(value of pPng888[60,80])", len_arr,
             pPng888[120 * 80 + 60]);
    }

    //取得 Y8 mask array, ubool pMask[w][h], 2448x3264 ->1280x800
    jbyte *cMaskY8 = env->GetByteArrayElements(jMaskY8, 0);
    len_arr = env->GetArrayLength(jMaskY8);
    unsigned char *pMaskY8 = reinterpret_cast<unsigned char *>(cMaskY8); //pMask[0] ~ pMask[n]
    LOGE("step 2: %d(len of pMaskY8 1280x800), %d(value of pMaskY8[640,400])", len_arr,
         pMaskY8[1280 * 400 + 640]);

    initParam();
    LOGE("step 3: initParam()");

    // Remove Noise Data
    BoundClipDepth(pDepthY16, img_width, img_height, 200, 500);

    // Remove Edge Peak
    RemoveEdgePeakDepthHV(pDepthY16, img_width, img_height, 2);

    // fill hole (liner fill)
    FillDepthHV(pDepthY16, img_width, img_height, 100, 100);

    // smooth (liner smooth)
    SmoothDepthHV(pDepthY16, img_width, img_height, 3, 3);

    //depth轉為mesh
    ITRITriMesh *pMesh = new ITRITriMesh();
//   int rslMesh = DepthToTriMesh(pDepthY16, img_width, img_height, m_dcp_param, bbox, sample_step, pMesh);
//    LOGE("step 3: DepthTransRGBToTriMesh: %d ", rslMesh);

    //depth轉為mesh並疊上rgb
    int rslMesh = DepthNIRToTriMesh(pDepthY16, img_width, img_height,
                                    const_cast<char *>(cPicFileNameJpg), m_dcp_param, bbox,
                                    sample_step, pMesh);
    LOGE("step 3: DepthNIRToTriMesh: %d ", rslMesh);
    if (rslMesh<1) success = false;

    //對mesh smooth
//    int rslSmooth = SmoothTriMesh(pMesh, 2);
//    LOGE("step 3: SmoothTriMesh: %d", rslSmooth);

    if (save && rslMesh>0) {
        int rslSave3dsObj = pMesh->SaveToObjFile(const_cast<char *>(cFile3dMainName3ds),
                                                 const_cast<char *>(cFilePath));
        LOGE("step 4: SaveToObjFile rgb 3DS: %d", rslSave3dsObj);
    }

    if (jThermalY32!=NULL) {
        // attached thermal data
//        int thermal_rsl = TriMeshAttThermalIMG(pMesh, m_thermal_param, pThermalY32, 160, 120);
        int thermal_rsl = TriMeshAttThermalIMGEx(pMesh, pThermalY32, 1280, 800);
        LOGE("step 4: TriMeshAttThermalIMG: %d ", thermal_rsl);

//    int rgb_rsl = TriMeshAttRGBIMGEx(pMesh, m_png_param, const_cast<char *>(cPicFileNamePng), 120,
        int rgb_rsl = TriMeshAttThermalRGBIMGEx(pMesh, const_cast<char *>(cPicFileNamePng), 1280,
                                                800);

//    LOGE("step 4: TriMeshAttRGBIMGEx: %d ", rgb_rsl);
        LOGE("step 4: TriMeshAttThermalRGBIMGEx: %d ", rgb_rsl);

//    if (thermal_rsl!=1 || rgb_rsl!=1) success = false;
        if (save && thermal_rsl == 1 && rgb_rsl == 1) {
            int rslSaveThmObj = pMesh->SaveToObjFileEx(const_cast<char *>(cFile3dMainNameThm),
                                                       const_cast<char *>(cFilePath));
            LOGE("step 4: SaveToObjFile: %d", rslSaveThmObj);
        }
    }

    //透過wound area mask, 計算尺寸及最深
    WoundAreaInfo pInfo;
    int rslInfo = CalWoundAreaInfo(pMaskY8, img_width, img_height, pMesh, &pInfo);
    LOGE("step 5: CalWoundAreaInfo: %d ", rslInfo);
    if (rslInfo < 0 ) success = false;

    std::ostringstream stringStream;
    if (success==true) {
        stringStream << "success=true\n" << "wound area=" << pInfo.wound_area / 100.0<< "\n";
    } else {
        stringStream << "success=false\n" << "wound area=" << pInfo.wound_area / 100.0<< "\n";
    }
    stringStream << "max depth=" << pInfo.wound_max_depth / 10.0 << "\n";
    stringStream << "hi temp=" << pInfo.wound_high_temp.temp << "\n";
    stringStream << "lo temp=" << pInfo.wound_low_temp.temp << "\n";

    // new regression size calculation function
    LOGE("step 5: SetFour2DPoints %dx%d at (%d,%d)(%d,%d) - (%d,%d)(%d,%d)", m_jpg_param.img_width, m_jpg_param.img_height, xx11, yy11, xx12, yy12, xx21, yy21, xx22, yy22);
    if (xx11>0 && xx12>0 && yy11>0 && yy12>0) {

        int ImgX[4], ImgY[4];
        float p3DX[4], p3DY[4], p3DZ[4];
        char mess[100];
        ImgX[0] = xx11;
        ImgY[0] = yy11;
        ImgX[1] = xx12;
        ImgY[1] = yy12;
        ImgX[2] = xx21;
        ImgY[2] = yy21;
        ImgX[3] = xx22;
        ImgY[3] = yy22;


        int size_rsl = GetFour3DPointsOnDepthWithCPTRegression(pDepthY16, m_dcp_param, img_width,
                                                               img_height, 4, ImgX, ImgY, p3DX,
                                                               p3DY, p3DZ);

        LOGE("step 5: GetFour3DPointsOnDepthWithCPTRegression: %d", size_rsl);
        if (size_rsl > 0) {
            float dist1 = float(sqrt((p3DX[0] - p3DX[1]) * (p3DX[0] - p3DX[1]) +
                                     (p3DY[0] - p3DY[1]) * (p3DY[0] - p3DY[1]) +
                                     (p3DZ[0] - p3DZ[1]) * (p3DZ[0] - p3DZ[1])));
            float dist2 = 0.0;
            if (xx21 != 0 && xx22 != 0){
                dist2 = float(sqrt((p3DX[2] - p3DX[3]) * (p3DX[2] - p3DX[3]) +
                                   (p3DY[2] - p3DY[3]) * (p3DY[2] - p3DY[3]) +
                                   (p3DZ[2] - p3DZ[3]) * (p3DZ[2] - p3DZ[3])));
            }

            //sprintf(mess, "Width= %.2f mm; Height= %.2f mm", dist1, dist2);

            LOGE("ppp_1_x: %f", p3DX[0]);
            LOGE("ppp_1_y: %f", p3DY[0]);

            LOGE("ppp_2_x: %f", p3DX[1]);
            LOGE("ppp_2_y: %f", p3DY[1]);

            LOGE("ppp_3_x: %f", p3DX[2]);
            LOGE("ppp_3_y: %f", p3DY[2]);

            LOGE("ppp_4_x: %f", p3DX[3]);
            LOGE("ppp_4_y: %f", p3DY[3]);

            LOGE("xx21: %d", xx21);
            LOGE("xx22: %d", xx22);
            LOGE("yy21: %d", yy21);
            LOGE("yy22: %d", yy22);

            int y_dist = yy22 - yy21;
            if( xx21 != xx22 && y_dist != 2){
                if (dist1 > dist2) {
                    stringStream << "long=" << dist1 / 10.0 << "\n";
                    stringStream << "short=" << dist2 / 10.0 << "\n";
                } else {
                    stringStream << "long=" << dist2 / 10.0 << "\n";
                    stringStream << "short=" << dist1 / 10.0 << "\n";
                }
            } else {
                stringStream << "long=" << dist1 / 10.0 << "\n";
                stringStream << "short=" << dist2 / 10.0 << "\n";
            }

        }

        // old size calculation function
//    double dist1 = GetTwoPointsDistOnTexture(pMesh, xx11, yy11, xx12, yy12, m_jpg_param.img_width,
//                                             m_jpg_param.img_height);
//    double dist2 = GetTwoPointsDistOnTexture(pMesh, xx21, yy21, xx22, yy22, m_jpg_param.img_width,
//                                             m_jpg_param.img_height);
//    LOGE("step 5: GetTwoPointsDistOnTexture1 %dx%d at (%d,%d)(%d,%d): %f ", m_jpg_param.img_width,
//         m_jpg_param.img_height, xx11, yy11, xx12, yy12, dist1);
//    LOGE("step 5: GetTwoPointsDistOnTexture2 %dx%d at (%d,%d)(%d,%d): %f ", m_jpg_param.img_width,
//         m_jpg_param.img_height, xx21, yy21, xx22, yy22, dist2);
//
//    if (dist1 > dist2) {
//        stringStream << "long=" << dist1 / 10.0 << "\n";
//        stringStream << "short=" << dist2 / 10.0 << "\n";
//    } else {
//        stringStream << "long=" << dist2 / 10.0 << "\n";
//        stringStream << "short=" << dist1 / 10.0 << "\n";
//    }

    }

    std::string result = stringStream.str();
    LOGE("step 6: info: %s ", result.c_str());

    //釋放記憶體
    delete pMesh;

    env->ReleaseShortArrayElements(jDepthY16, cDepthY16, 0);
    env->ReleaseByteArrayElements(jJpg888, cJpg888, 0);
    if (jThermalY32!=NULL) {
        env->ReleaseFloatArrayElements(jThermalY32, cThermalY32, 0);
        env->ReleaseByteArrayElements(jPng888, cPng888, 0);
    }
    env->ReleaseByteArrayElements(jMaskY8, cMaskY8, 0);

    env->ReleaseStringUTFChars(jPicFileNameJpg, cPicFileNameJpg);
    env->ReleaseStringUTFChars(jFile3dMainName3ds, cFile3dMainName3ds);
    if (jThermalY32!=NULL) {
        env->ReleaseStringUTFChars(jPicFileNamePng, cPicFileNamePng);
        env->ReleaseStringUTFChars(jFile3dMainNameThm, cFile3dMainNameThm);
    }
    env->ReleaseStringUTFChars(jFilePath, cFilePath);

    LOGE("step 7: release");
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_nativeGetWoundInfo_20200909(JNIEnv *env, jclass cls, jobject obj,
                                                     jstring jFilePath, jshortArray jDepthY16,
                                                     jbyteArray jJpg888, jstring jFile3dMainName3ds,
                                                     jstring jPicFileNameJpg,
                                                     jfloatArray jThermalY32,
                                                     jbyteArray jPng888, jstring jFile3dMainNameThm,
                                                     jstring jPicFileNamePng, jbyteArray jMaskY8,
                                                     jint width, jint height, jint xx11, jint yy11,
                                                     jint xx12, jint yy12, jint xx21, jint yy21,
                                                     jint xx22,
                                                     jint yy22) {
    //取得 File path
    const char *cFilePath = env->GetStringUTFChars(jFilePath, NULL);
    string sFilePath(cFilePath);
    LOGE("step 1 nativeGetWoundInfo: %d", strlen(cFilePath));

    //取得 obj File main name
    const char *cFile3dMainName3ds = env->GetStringUTFChars(jFile3dMainName3ds, NULL);
    string sFile3dMainName3ds(cFile3dMainName3ds);
    LOGE("step 1 cFile3dMainName3ds: %d", strlen(cFile3dMainName3ds));

    const char *cPicFileNameJpg = env->GetStringUTFChars(jPicFileNameJpg, NULL);
    string sPicFileNameJpg(cPicFileNameJpg);
    LOGE("step 1 cPicFileNameJpg: %d", strlen(cPicFileNameJpg));

    //取得 obj File main name
    const char *cFile3dMainNameThm = env->GetStringUTFChars(jFile3dMainNameThm, NULL);
    string sFile3dMainNameThm(cFile3dMainNameThm);
    LOGE("step 1 cFile3dMainNameThm: %d", strlen(cFile3dMainNameThm));

    const char *cPicFileNamePng = env->GetStringUTFChars(jPicFileNamePng, NULL);
    string sPicFileNamePng(cPicFileNamePng);
    LOGE("step 1 sPngFileName: %d", strlen(cPicFileNamePng));

    //取得 Y16 depth array, ushort pDepth[w][h]
    jshort *cDepthY16 = env->GetShortArrayElements(jDepthY16, NULL);
    int len_arr = env->GetArrayLength(jDepthY16);
    unsigned short *pDepthY16 = reinterpret_cast<unsigned short *>(cDepthY16); //pDepth[0] ~ pDepth[n]
    LOGE("step 2: %d(len of pDepthY16), %hu(value of pDepthY16[700,400])", len_arr,
         pDepthY16[1280 * 400 + 700]);

    //取得 JPG color array, uchar pColor[w][h][ch]
    jbyte *cJpg888 = env->GetByteArrayElements(jJpg888, 0);
    len_arr = env->GetArrayLength(jJpg888);
    unsigned char *pJpg888 = reinterpret_cast<unsigned char *>(cJpg888); //pColor[0] ~ pColor[n]
    LOGE("step 2: %d(len of pJpg888), %d(value of pJpg888[700,400])", len_arr,
         pJpg888[1280 * 400 + 700]);

    //取得 Y32 thermal array, float pThermal[w][h]
    jfloat *cThermalY32 = env->GetFloatArrayElements(jThermalY32, 0);
    len_arr = env->GetArrayLength(jThermalY32);
    float *pThermalY32 = reinterpret_cast<float *>(cThermalY32); //pThermal[0] ~ pThermal[n]
    LOGE("step 2: %d(len of pThermalY32), %.2f(value of pThermalY32[60,80])", len_arr,
         pThermalY32[120 * 80 + 60]);

    //取得 PNG color array, uchar pColor[w][h][ch]
    jbyte *cPng888 = env->GetByteArrayElements(jPng888, 0);
    len_arr = env->GetArrayLength(jPng888);
    unsigned char *pPng888 = reinterpret_cast<unsigned char *>(cPng888); //pColor[0] ~ pColor[n]
    LOGE("step 2: %d(len of pPng888), %d(value of pPng888[60,80])", len_arr,
         pPng888[80 * 120 + 60]);


    //取得 Y8 mask array, ubool pMask[w][h]
    jbyte *cMaskY8 = env->GetByteArrayElements(jMaskY8, 0);
    len_arr = env->GetArrayLength(jMaskY8);
    unsigned char *pMaskY8 = reinterpret_cast<unsigned char *>(cMaskY8); //pMask[0] ~ pMask[n]
    LOGE("step 2: %d(len of pMaskY8), %d(value of pMaskY8[700,400])", len_arr,
         pMaskY8[1280 * 400 + 700]);

    initParam();
    LOGE("step 3: initParam()");
    //depth轉為mesh
//    int sample_step = 2;
//    DepthCameraParam m_dcp_param;
//    m_dcp_param.FOCAL_LENGTH_X = 835.2802734375;
//    m_dcp_param.FOCAL_LENGTH_Y = 835.9163208008;
//    m_dcp_param.PRINCIPAL_X = 409.8193359375;
//    m_dcp_param.PRINCIPAL_Y = 636.9195556641;
//
//    BoundingBox bbox;
//    bbox.min_x = -200;
//    bbox.max_x = 200;
//    bbox.min_y = -300;
//    bbox.max_y = 300;
//    bbox.min_z = 100;
//    bbox.max_z = 600;
//
//    int img_width = 1280;
//    int img_height = 800;
    // Remove Noise Data
    BoundClipDepth(pDepthY16, img_width, img_height, 200, 500);

    // fill hole (liner fill)
    FillDepthHV(pDepthY16, img_width, img_height, 100, 100);

    // process the data
    ITRITriMesh *pMesh = new ITRITriMesh();
    int rslMesh = DepthToTriMesh(pDepthY16, img_width, img_height, m_dcp_param, bbox, sample_step,
                                 pMesh);
    LOGE("step 3 DepthToTriMesh: %d ", rslMesh);

//    int rslSmooth = SmoothTriMesh(pMesh, 2);
//    LOGE("step 3 SmoothTriMesh: %d", rslSmooth);

    // attached thermal data
//    ThermalAttMeshParam m_thermal_param;
////    m_thermal_param.img_width = 120;
////    m_thermal_param.img_height = 160;
////    m_thermal_param.cx = 60;
////    m_thermal_param.cy = 100;
////    m_thermal_param.sx = 0.1;
////    m_thermal_param.sy = 0.1;
////    m_thermal_param.tx = 0.001;
////    m_thermal_param.ty = 0.001;
////    m_thermal_param.tz = -400.0;
////
////    m_thermal_param.img_width = 120;
////    m_thermal_param.img_height = 160;
////    m_thermal_param.cx = 60;
////    m_thermal_param.cy = 100;
////    m_thermal_param.sx = 0.1;
////    m_thermal_param.sy = 0.1;
////    m_thermal_param.tx = 0.001;
////    m_thermal_param.ty = 0.001;
////    m_thermal_param.tz = -400.0;
//
//    m_thermal_param.img_width = 120;
//    m_thermal_param.img_height = 160;
//    m_thermal_param.cx = 68;
//    m_thermal_param.cy = 86;
//    m_thermal_param.sx = 0.49;
//    m_thermal_param.sy = 0.49;
//    m_thermal_param.tx = -0.1;
//    m_thermal_param.ty = -0.1;
//    m_thermal_param.tz = -300.0;


//    FILE *ifp = fopen("/storage/emulated/0/Download/WoundCamRtc/CameraImg/SampleThermal.raw", "wb");
//    if (ifp != NULL) {
//        fwrite(pThermalY32, sizeof(float), 120 * 160, ifp);
//        fclose(ifp);
//    }

    int thermal_rsl = TriMeshAttThermalIMG(pMesh, m_thermal_param, pThermalY32, 120, 160);
    LOGE("step 4 TriMeshAttThermalIMG: %d ", thermal_rsl);

//    RGBAttMeshParam m_png_param;
////    m_rgb_param.img_width = 120;
////    m_rgb_param.img_height = 160;
////    m_rgb_param.cx = 60;
////    m_rgb_param.cy = 80;
////    m_rgb_param.sx = 0.1;
////    m_rgb_param.sy = 0.1;
////    m_rgb_param.tx = 0.001;
////    m_rgb_param.ty = 0.001;
////    m_rgb_param.tz = -400.0;
//
//    m_png_param.img_width = 120;
//    m_png_param.img_height = 160;
//    m_png_param.cx = 68;
//    m_png_param.cy = 86;
//    m_png_param.sx = 0.49;
//    m_png_param.sy = 0.49;
//    m_png_param.tx = -0.1;
//    m_png_param.ty = -0.1;
//    m_png_param.tz = -300.0;

    int rgb_rsl = TriMeshAttRGBIMG(pMesh, m_png_param, const_cast<char *>(cPicFileNamePng), 120,
                                   160);
    LOGE("step 4 TriMeshAttRGBIMG: %d ", rgb_rsl);
    int rslSaveThmObj = pMesh->SaveToObjFile(const_cast<char *>(cFile3dMainNameThm),
                                             const_cast<char *>(cFilePath));
    LOGE("step 4 SaveToObjFile: %d", rslSaveThmObj);

    //mesh疊上rgb
//    m_rgb_param.img_width = 2448;
//    m_rgb_param.img_height = 3264;
//    m_rgb_param.cx = 1291;
//    m_rgb_param.cy = 1615;
//    m_rgb_param.sx = 6.0;
//    m_rgb_param.sy = 6.0;
//    m_rgb_param.tx = 0.01;
//    m_rgb_param.ty = 0.01;
//    m_rgb_param.tz = -400.0;


//    m_rgb_param.img_width = 2448;
//    m_rgb_param.img_height = 3264;
//    m_rgb_param.cx = 1360;
//    m_rgb_param.cy = 1453;
//    m_rgb_param.sx = 8.3;
//    m_rgb_param.sy = 8.3;
//    m_rgb_param.tx = -0.5;
//    m_rgb_param.ty = -0.5;
//    m_rgb_param.tz = -300.0;

//    RGBAttMeshParam m_jpg_param;
//    m_jpg_param.img_width = 2448;
//    m_jpg_param.img_height = 3264;
//    m_jpg_param.cx = 1370;
//    m_jpg_param.cy = 1463;
//    m_jpg_param.sx = 9.78;
//    m_jpg_param.sy = 9.78;
//    m_jpg_param.tx = -0.2;
//    m_jpg_param.ty = -0.2;
//    m_jpg_param.tz = -300.0;

    int rslRGB = TriMeshAttRGBIMG(pMesh, m_jpg_param, const_cast<char *>(cPicFileNameJpg), width,
                                  height);
    LOGE("step 4 TriMeshAttRGBIMG: %d ", rslRGB);
    int rslSave3dsObj = pMesh->SaveToObjFile(const_cast<char *>(cFile3dMainName3ds),
                                             const_cast<char *>(cFilePath));
    LOGE("step 4 SaveToObjFile: %d", rslSave3dsObj);



    //透過wound area mask, 計算尺寸及最深
    WoundAreaInfo pInfo;
    int rslInfo = CalWoundAreaInfo(pMaskY8, width, height, pMesh, &pInfo);
    LOGE("step 5 CalWoundAreaInfo: %d ", rslInfo);

    std::ostringstream stringStream;
    stringStream << "success=true\n" << "wound area=" << pInfo.wound_area << "\n";
    stringStream << "max depth=" << pInfo.wound_max_depth / 10.0 << "\n";
    stringStream << "hi temp=" << pInfo.wound_high_temp.temp << "\n";
    stringStream << "lo temp=" << pInfo.wound_low_temp.temp << "\n";

    double dist1 = GetTwoPointsDistOnTexture(pMesh, xx11, yy11, xx12, yy12, m_jpg_param.img_width,
                                             m_jpg_param.img_height);
    double dist2 = GetTwoPointsDistOnTexture(pMesh, xx21, yy21, xx22, yy22, m_jpg_param.img_width,
                                             m_jpg_param.img_height);
    LOGE("step 5 GetTwoPointsDistOnTexture1 %dx%d at (%d,%d)(%d,%d): %f ", m_jpg_param.img_width,
         m_jpg_param.img_height, xx11, yy11, xx12, yy12, dist1);
    LOGE("step 5 GetTwoPointsDistOnTexture2 %dx%d at (%d,%d)(%d,%d): %f ", m_jpg_param.img_width,
         m_jpg_param.img_height, xx21, yy21, xx22, yy22, dist2);



        if (dist1 > dist2) {
            stringStream << "long=" << dist1 / 10.0 << "\n";
            stringStream << "short=" << dist2 / 10.0 << "\n";
        } else {
            stringStream << "long=" << dist2 / 10.0 << "\n";
            stringStream << "short=" << dist1 / 10.0 << "\n";
        }




    std::string result = stringStream.str();
    LOGE("step 6 info: %s ", result.c_str());

    //釋放記憶體
    delete pMesh;

    env->ReleaseShortArrayElements(jDepthY16, cDepthY16, 0);
    env->ReleaseByteArrayElements(jJpg888, cJpg888, 0);
    env->ReleaseFloatArrayElements(jThermalY32, cThermalY32, 0);
    env->ReleaseByteArrayElements(jPng888, cPng888, 0);
    env->ReleaseByteArrayElements(jMaskY8, cMaskY8, 0);

    env->ReleaseStringUTFChars(jPicFileNameJpg, cPicFileNameJpg);
    env->ReleaseStringUTFChars(jFile3dMainName3ds, cFile3dMainName3ds);

    env->ReleaseStringUTFChars(jPicFileNamePng, cPicFileNamePng);
    env->ReleaseStringUTFChars(jFile3dMainNameThm, cFile3dMainNameThm);
    env->ReleaseStringUTFChars(jFilePath, cFilePath);

    LOGE("step 7 release");
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_nativeGen3DColorImage(JNIEnv *env, jclass cls, jobject obj,
                                               jstring jFilePath,
                                               jstring jFile3dMainName3ds, jstring jPicFileNameJpg,
                                               jshortArray jDepthY16, jbyteArray jJpg888,
                                               jint width,
                                               jint height, jlong addrDstImage) {

    bool success = true;
    bool save = true;
    LOGE("step 1: nativeGetWoundInfo");

    //取得 file path
    const char *cFilePath = env->GetStringUTFChars(jFilePath, NULL);
    string sFilePath(cFilePath);
    LOGE("step 1: cFilePath: %d %s", strlen(cFilePath), cFilePath);

    //取得 file main name of 3DS obj
    const char *cFile3dMainName3ds = env->GetStringUTFChars(jFile3dMainName3ds, NULL);
    string sFile3dMainName3ds(cFile3dMainName3ds);
    LOGE("step 1: cFile3dMainName3ds: %d %s", strlen(cFile3dMainName3ds), cFile3dMainName3ds);

    //取得 file name of rgb jpg
    const char *cPicFileNameJpg = env->GetStringUTFChars(jPicFileNameJpg, NULL);
    string sPicFileNameJpg(cPicFileNameJpg);
    LOGE("step 1: cPicFileNameJpg: %d %s", strlen(cPicFileNameJpg), cPicFileNameJpg);

    //取得 Y16 depth array, ushort pDepth[w][h], 1280x800
    jshort *cDepthY16 = env->GetShortArrayElements(jDepthY16, NULL);
    int len_arr = env->GetArrayLength(jDepthY16);
    unsigned short *pDepthY16 = reinterpret_cast<unsigned short *>(cDepthY16); //pDepth[0] ~ pDepth[n]
    LOGE("step 2: %d(len of pDepthY16 1280x800), %hu(value of pDepthY16[640,400])", len_arr,
         pDepthY16[1280 * 400 + 640]);

    //取得 JPG color array, uchar pColor[w][h][ch]
    jbyte *cJpg888 = env->GetByteArrayElements(jJpg888, 0);
    len_arr = env->GetArrayLength(jJpg888);
    unsigned char *pJpg888 = reinterpret_cast<unsigned char *>(cJpg888); //pColor[0] ~ pColor[n]
    LOGE("step 2: %d(len of pJpg888), %d(value of pJpg888[700,400])", len_arr,
         pJpg888[1280 * 400 + 700]);

    initParam();
    LOGE("step 3: initParam()");

    // Remove Noise Data
    BoundClipDepth(pDepthY16, img_width, img_height, 200, 500);

    // fill hole (liner fill)
    FillDepthHV(pDepthY16, img_width, img_height, 100, 100);

    //depth轉為mesh
    ITRITriMesh *pMesh = new ITRITriMesh();
//   int rslMesh = DepthToTriMesh(pDepthY16, img_width, img_height, m_dcp_param, bbox, sample_step, pMesh);
//    LOGE("step 3: DepthTransRGBToTriMesh: %d ", rslMesh);

    //depth轉為mesh並疊上rgb
    int rslMesh = DepthNIRToTriMesh(pDepthY16, img_width, img_height,
                                    const_cast<char *>(cPicFileNameJpg), m_dcp_param, bbox,
                                    sample_step, pMesh);
    LOGE("step 3: DepthNIRToTriMesh: %d ", rslMesh);
    if (rslMesh!=1) success = false;

    //對mesh smooth
//    int rslSmooth = SmoothTriMesh(pMesh, 2);
//    LOGE("step 3: SmoothTriMesh: %d", rslSmooth);

    if (save && rslMesh>0) {
        int rslSave3dsObj = pMesh->SaveToObjFile(const_cast<char *>(cFile3dMainName3ds),
                                                 const_cast<char *>(cFilePath));
        LOGE("step 4: SaveToObjFile rgb 3DS: %d", rslSave3dsObj);
    }

    //釋放記憶體
    delete pMesh;

    env->ReleaseShortArrayElements(jDepthY16, cDepthY16, 0);
    env->ReleaseByteArrayElements(jJpg888, cJpg888, 0);

    env->ReleaseStringUTFChars(jPicFileNameJpg, cPicFileNameJpg);
    env->ReleaseStringUTFChars(jFile3dMainName3ds, cFile3dMainName3ds);
    env->ReleaseStringUTFChars(jFilePath, cFilePath);

    LOGE("step 7: release");

    std::string result = "success=true\n";
    return env->NewStringUTF(result.c_str());
}

//extern "C" JNIEXPORT jstring JNICALL
//Java_org_jni_NativeUtils_nativeGen3DColorImage(JNIEnv *env, jclass cls, jobject obj,
//                                               jstring jFilePath,
//                                               jstring jFile3dMainName, jstring jPicFileName,
//                                               jshortArray jDepthY16, jbyteArray jRGB888,
//                                               jint width,
//                                               jint height, jlong addrDstImage) {
//    Mat &dst = *(Mat *) addrDstImage;
//    LOGE("step 1: nativeGen3DColorImage", 0);
//
//    //取得 File path
//    const char *cFilePath = env->GetStringUTFChars(jFilePath, NULL);
//    string sFilePath(cFilePath);
//    LOGE("step 1: %d", strlen(cFilePath));
//
//    //取得 obj File main name
//    const char *cFile3dMainName = env->GetStringUTFChars(jFile3dMainName, NULL);
//    string sFile3dMainName(cFile3dMainName);
//    LOGE("step 1: %d", strlen(cFile3dMainName));
//
//    const char *cPicFileName = env->GetStringUTFChars(jPicFileName, NULL);
//    string sPicFileName(cPicFileName);
//    LOGE("step 1: %d", strlen(cPicFileName));
//
//    //取得 Y16 depth array, ushort pDepth[w][h]
//    jshort *cDepthY16 = env->GetShortArrayElements(jDepthY16, NULL);
//    int len_arr = env->GetArrayLength(jDepthY16);
//    unsigned short *pDepthY16 = reinterpret_cast<unsigned short *>(cDepthY16); //pDepth[0] ~ pDepth[n]
//    LOGE("step 2: %d(len of pDepthY16), %hu(value of pDepthY16[700,400])", len_arr,
//         pDepthY16[1280 * 400 + 700]);
//
//    //取得 RGB color array, uchar pColor[w][h][ch]
//    jbyte *cRGB888 = env->GetByteArrayElements(jRGB888, 0);
//    len_arr = env->GetArrayLength(jRGB888);
//    unsigned char *pRGB888 = reinterpret_cast<unsigned char *>(cRGB888); //pColor[0] ~ pColor[n]
//    LOGE("step 2: %d(len of pRGB888), %d(value of pRGB888[700,400])", len_arr,
//         pRGB888[1280 * 400 + 700]);
//
//
//    initParam();
//    LOGE("step 3: initParam()");
//    //depth轉為mesh
////    int sample_step = 2;
////    DepthCameraParam m_dcp_param;
////    m_dcp_param.FOCAL_LENGTH_X = 835.2802734375;
////    m_dcp_param.FOCAL_LENGTH_Y = 835.9163208008;
////    m_dcp_param.PRINCIPAL_X = 409.8193359375;
////    m_dcp_param.PRINCIPAL_Y = 636.9195556641;
////
////    BoundingBox bbox;
////    bbox.min_x = -200;
////    bbox.max_x = 200;
////    bbox.min_y = -300;
////    bbox.max_y = 300;
////    bbox.min_z = 100;
////    bbox.max_z = 600;
////
////    int img_width = 1280;
////    int img_height = 800;
//    // Remove Noise Data
//    BoundClipDepth(pDepthY16, img_width, img_height, 200, 500);
//
//    // fill hole (liner fill)
//    FillDepthHV(pDepthY16, img_width, img_height, 100, 100);
//
//    // process the data
//    ITRITriMesh *pMesh = new ITRITriMesh();
//    int rslMesh = DepthToTriMesh(pDepthY16, img_width, img_height, m_dcp_param, bbox, sample_step,
//                                 pMesh);
//    LOGE("step 3 DepthToTriMesh: %d ", rslMesh);
//
////    int rslSmooth = SmoothTriMesh(pMesh, 2);
////    LOGE("step 3 SmoothTriMesh: %d", rslSmooth);
//
//    //mesh疊上rgb
////    RGBAttMeshParam m_jpg_param;
//    //20200811
////    m_rgb_param.img_width = 2448;
////    m_rgb_param.img_height = 3264;
////    m_rgb_param.cx = 1157;
////    m_rgb_param.cy = 1649;
////    m_rgb_param.sx = 6.0;
////    m_rgb_param.sy = 6.0;
////    m_rgb_param.tx = 0.01;
////    m_rgb_param.ty = 0.01;
////    m_rgb_param.tz = -400.0;
//
//    //20200810
////    m_rgb_param.img_width= 2448;
////    m_rgb_param.img_height= 3264;
////    m_rgb_param.cx= 1224;
////    m_rgb_param.cy= 1600;
////    m_rgb_param.sx= 0.25;
////    m_rgb_param.sy= 0.25;
////    m_rgb_param.tx= 0.01;
////    m_rgb_param.ty= 0.01;
////    m_rgb_param.tz= 0.01;
//
////    m_jpg_param.img_width = 2448;
////    m_jpg_param.img_height = 3264;
////    m_jpg_param.cx = 1360;
////    m_jpg_param.cy = 1453;
////    m_jpg_param.sx = 8.3;
////    m_jpg_param.sy = 8.3;
////    m_jpg_param.tx = -0.5;
////    m_jpg_param.ty = -0.5;
////    m_jpg_param.tz = -300.0;
//
//
//    int rslRGB = TriMeshAttRGBIMG(pMesh, m_jpg_param, const_cast<char *>(cPicFileName), width,
//                                  height);
//    LOGE("step 5 TriMeshAttRGBIMG: %d ", rslRGB);
//    LOGE("step 5 cFile3dMainName: %s ", cFile3dMainName);
//    LOGE("step 5 cFilePath: %s ", cFilePath);
//    int rslSave = pMesh->SaveToObjFile(const_cast<char *>(cFile3dMainName),
//                                       const_cast<char *>(cFilePath));
//    LOGE("step 6 SaveToObjFile: %d", rslSave);
//    //釋放記憶體
//    delete pMesh;
//
//    env->ReleaseShortArrayElements(jDepthY16, cDepthY16, 0);
//    env->ReleaseByteArrayElements(jRGB888, cRGB888, 0);
//    env->ReleaseStringUTFChars(jPicFileName, cPicFileName);
//    env->ReleaseStringUTFChars(jFile3dMainName, cFile3dMainName);
//    env->ReleaseStringUTFChars(jFilePath, cFilePath);
//    dst.release();
//
//    LOGE("step 7 released");
//    std::string result = "success=true\n";
//    return env->NewStringUTF(result.c_str());
//}

extern "C" JNIEXPORT jstring JNICALL
Java_org_jni_NativeUtils_nativeGen3DThermalImage(JNIEnv *env, jclass cls, jobject obj,
                                                 jstring jFilePath, jstring jFile3dMainName,
                                                 jstring jPicFileName, jshortArray jDepthY16,
                                                 jfloatArray jThermalY32, jbyteArray jRGB888,
                                                 jint width, jint height, jlong addrDstImage) {

    Mat &dst = *(Mat *) addrDstImage;
    LOGE("step 1: ", 0);

    //取得 File path
    const char *cFilePath = env->GetStringUTFChars(jFilePath, NULL);
    string sFilePath(cFilePath);
    LOGE("step 2: %d", strlen(cFilePath));

    //取得 obj File main name
    const char *cFile3dMainName = env->GetStringUTFChars(jFile3dMainName, NULL);
    string sFile3dMainName(cFile3dMainName);
    LOGE("step 2: %d", strlen(cFile3dMainName));

    const char *cPicFileName = env->GetStringUTFChars(jPicFileName, NULL);
    string sPicFileName(cPicFileName);
    LOGE("step 2: %d", strlen(cPicFileName));

    //取得 Y16 depth array, ushort pDepth[w][h]
    jshort *cDepthY16 = env->GetShortArrayElements(jDepthY16, NULL);
    int len_arr = env->GetArrayLength(jDepthY16);
    unsigned short *pDepthY16 = reinterpret_cast<unsigned short *>(cDepthY16); //pDepth[0] ~ pDepth[n]
    LOGE("step 3: %d(len of pDepthY16), %hu(value of pDepthY16[700,400])", len_arr,
         pDepthY16[1280 * 400 + 700]);

    //取得 Y32 thermal array, float pThermal[w][h]
    jfloat *cThermalY32 = env->GetFloatArrayElements(jThermalY32, 0);
    len_arr = env->GetArrayLength(jThermalY32);
    float *pThermalY32 = reinterpret_cast<float *>(cThermalY32); //pThermal[0] ~ pThermal[n]
    LOGE("step 3: %d(len of pThermalY32), %.2f(value of pThermalY32[60,80])", len_arr,
         pThermalY32[120 * 80 + 60]);

    //取得 RGB color array, uchar pColor[w][h][ch]
    jbyte *cRGB888 = env->GetByteArrayElements(jRGB888, 0);
    len_arr = env->GetArrayLength(jRGB888);
    unsigned char *pRGB888 = reinterpret_cast<unsigned char *>(cRGB888); //pColor[0] ~ pColor[n]
    LOGE("step 3: %d(len of pRGB888), %d(value of pRGB888[60,80])", len_arr,
         pRGB888[120 * 80 + 60]);

    //depth轉為mesh
    int sample_step = 2;
    DepthCameraParam m_dcp_param;
    m_dcp_param.FOCAL_LENGTH_X = 835.2802734375;
    m_dcp_param.FOCAL_LENGTH_Y = 835.9163208008;
    m_dcp_param.PRINCIPAL_X = 409.8193359375;
    m_dcp_param.PRINCIPAL_Y = 636.9195556641;

    BoundingBox bbox;
    bbox.min_x = -200;
    bbox.max_x = 200;
    bbox.min_y = -300;
    bbox.max_y = 300;
    bbox.min_z = 100;
    bbox.max_z = 600;

    ITRITriMesh *pMesh = new ITRITriMesh();
    int rslMesh = DepthToTriMesh(pDepthY16, 1280, 800, m_dcp_param, bbox, sample_step, pMesh);
    LOGE("step 4 DepthToTriMesh: %d ", rslMesh);

    ThermalAttMeshParam m_thermal_param;
//    m_thermal_param.img_width = 120;
//    m_thermal_param.img_height = 160;
//    m_thermal_param.cx = 60;
//    m_thermal_param.cy = 100;
//    m_thermal_param.sx = 0.1;
//    m_thermal_param.sy = 0.1;
//    m_thermal_param.tx = 0.001;
//    m_thermal_param.ty = 0.001;
//    m_thermal_param.tz = -400.0;

    m_thermal_param.img_width = 120;
    m_thermal_param.img_height = 160;
    m_thermal_param.cx = 68;
    m_thermal_param.cy = 86;
    m_thermal_param.sx = 0.49;
    m_thermal_param.sy = 0.49;
    m_thermal_param.tx = -0.1;
    m_thermal_param.ty = -0.1;
    m_thermal_param.tz = -300.0;
    int thermal_rsl = TriMeshAttThermalIMG(pMesh, m_thermal_param, pThermalY32, width, height);
    LOGE("step 5 TriMeshAttThermalIMG: %d ", thermal_rsl);

    //mesh疊上png rgb
    RGBAttMeshParam m_rgb_param;
//    m_rgb_param.img_width = 120;
//    m_rgb_param.img_height = 160;
//    m_rgb_param.cx = 60;
//    m_rgb_param.cy = 80;
//    m_rgb_param.sx = 0.1;
//    m_rgb_param.sy = 0.1;
//    m_rgb_param.tx = 0.001;
//    m_rgb_param.ty = 0.001;
//    m_rgb_param.tz = -400.0;


    m_png_param.img_width = 120;
    m_png_param.img_height = 160;
    m_png_param.cx = 68;
    m_png_param.cy = 86;
    m_png_param.sx = 0.49;
    m_png_param.sy = 0.49;
    m_png_param.tx = -0.1;
    m_png_param.ty = -0.1;
    m_png_param.tz = -300.0;

    int rslRGB = TriMeshAttRGBIMG(pMesh, m_png_param, const_cast<char *>(cPicFileName), width,
                                  height);
    LOGE("step 6 TriMeshAttRGBIMG: %d ", rslRGB);
    LOGE("step 7 cFile3dMainName: %s ", cFile3dMainName);
    int rslSave = pMesh->SaveToObjFile(const_cast<char *>(cFile3dMainName),
                                       const_cast<char *>(cFilePath));
    LOGE("step 8 SaveToObjFile: %d", rslSave);
    //釋放記憶體
    delete pMesh;

    env->ReleaseShortArrayElements(jDepthY16, cDepthY16, 0);
    env->ReleaseFloatArrayElements(jThermalY32, cThermalY32, 0);
    env->ReleaseByteArrayElements(jRGB888, cRGB888, 0);
    env->ReleaseStringUTFChars(jPicFileName, cPicFileName);
    env->ReleaseStringUTFChars(jFile3dMainName, cFile3dMainName);
    env->ReleaseStringUTFChars(jFilePath, cFilePath);
    dst.release();

    std::string result = "success=true\n";
    return env->NewStringUTF(result.c_str());
}


//
//extern "C" JNIEXPORT void JNICALL Java_org_jni_NativeUtils_SuperRes(JNIEnv *env, jclass obj, jstring jFileName, jlong addrImg, jlong addrDst) {
//    Mat &img = *(Mat *) addrImg;
//    Mat &dst = *(Mat *) addrDst;
//    const char* jnamestr = env->GetStringUTFChars(jFileName, NULL);
//    string stdFileName(jnamestr);
//
////    DnnSuperResImpl sr;
////    //sr.readModel("models/FSRCNN_x2.pb");
////    sr.readModel(stdFileName);
////    sr.setModel("fsrcnn", 16);
////    sr.upsample(img, dst);
//    img.release();
//    dst.release();
//}

//extern "C" JNIEXPORT void JNICALL
//Java_org_jni_NativeUtils_LCC(JNIEnv *env, jobject obj, jlong addrImg, jlong addrDst,
//                             jobjectArray array) {
//    Mat &img = *(Mat *) addrImg;
//    Mat &dst = *(Mat *) addrDst;
//
//    float OriginalData[5][3] = {{242.0, 245.0, 245.0},
//                                {143.0, 65.0,  49.0},
//                                {80.0,  148.0, 99.0},
//                                {54.0,  53.0,  52.0},
//                                {59.0,  52.0,  155.0}};
//    //獲取引數int陣列的元素個數;
//    jsize size = env->GetArrayLength(array);
//    //獲取int陣列的所有元素
//    for(jsize i = 0; i < size; ++i) {
//        jintArray inner = static_cast<jintArray>(env->GetObjectArrayElement(array, i));
//        jint *intArray = env->GetIntArrayElements(inner, JNI_FALSE);
//        OriginalData[i][0]=intArray[0];
//        OriginalData[i][1]=intArray[1];
//        OriginalData[i][2]=intArray[2];
//        env->ReleaseIntArrayElements(inner, intArray, 0);
//    }
//    Mat OriginalColor = Mat(5, 3, CV_32FC1, OriginalData);
//
//    float ReferenceData[5][3] = {{242.0, 245.0, 245.0},
//                                 {143.0, 65.0,  49.0},
//                                 {80.0,  148.0, 99.0},
//                                 {54.0,  53.0,  52.0},
//                                 {59.0,  52.0,  155.0}};
//    Mat ReferenceColor = Mat(5, 3, CV_32FC1, ReferenceData);
//
//    cv::Mat O_T = OriginalColor.t();
//    cv::Mat temp = O_T * OriginalColor;
//    cv::Mat ColorMatrix(3, 3, CV_32F, cv::Scalar(0));
//    ColorMatrix = temp.inv() * O_T * ReferenceColor;
//
//    int ImgHeight = img.rows, ImgWidth = img.cols;
//    //Mat img32f;
//    Mat img32f = Mat(ImgHeight, ImgWidth, CV_32FC1);
//    //img.convertTo(img32f, CV_32F);
//
//    Mat orig_img_linear = img32f.reshape(1, ImgHeight * ImgWidth);
//    Mat color_matrixed_linear = orig_img_linear.inv() * ColorMatrix;
//    Mat out=  color_matrixed_linear.reshape(3, ImgHeight);
//    //out.convertTo(dst,CV_8UC3,255.0);
//
//
//    //resources release
//    img.release();
//    dst.release();
//    return;
//}
//
//
//
//extern "C" JNIEXPORT void JNICALL
//Java_org_jni_NativeUtils_AdaptiveGamma(JNIEnv *env, jobject obj, jlong addrImg, jlong addrDst) {
//    //https://blog.csdn.net/just_sort/article/details/88569129
////    Mat &src = *(Mat *) addrImg;
////    Mat &dst = *(Mat *) addrDst;
////    int row = src.rows;
////    int col = src.cols;
////    Mat now;
////    cvtColor(src,now,COLOR_BGR2HSV);
////    //Mat now = RGB2HSV(src);
////    Mat H(row, col, CV_32FC1);
////    Mat S(row, col, CV_32FC1);
////    Mat V(row, col, CV_32FC1);
////    for (int i = 0; i < row; i++) {
////        for (int j = 0; j < col; j++) {
////            H.at<float>(i, j) = now.at<Vec3f>(i, j)[0];
////            S.at<float>(i, j) = now.at<Vec3f>(i, j)[1];
////            V.at<float>(i, j) = now.at<Vec3f>(i, j)[2];
////        }
////    }
////    int kernel_size = min(row, col);
////    if (kernel_size % 2 == 0) {
////        kernel_size -= 1;
////    }
////    float SIGMA1 = 15;
////    float SIGMA2 = 80;
////    float SIGMA3 = 250;
////    float q = sqrt(2.0);
////    Mat F(row, col, CV_32FC1);
////    Mat F1, F2, F3;
////    GaussianBlur(V, F1, Size(kernel_size, kernel_size), SIGMA1 / q);
////    GaussianBlur(V, F2, Size(kernel_size, kernel_size), SIGMA2 / q);
////    GaussianBlur(V, F3, Size(kernel_size, kernel_size), SIGMA3 / q);
////    for (int i = 0; i < row; i++) {
////        for (int j = 0; j < col; j++) {
////            F.at <float>(i, j) = (F1.at<float>(i, j) + F2.at<float>(i, j) + F3.at<float>(i, j)) / 3.0;
////        }
////    }
////    float average = mean(F)[0];
////    Mat out(row, col, CV_32FC1);
////    for (int i = 0; i < row; i++) {
////        for (int j = 0; j < col; j++) {
////            float gamma = powf(0.5, (average - F.at<float>(i, j)) / average);
////            out.at<float>(i, j) = powf(V.at<float>(i, j), gamma);
////        }
////    }
////    vector <Mat> v;
////    v.push_back(H);
////    v.push_back(S);
////    v.push_back(out);
////    Mat merge_;
////    merge(v, merge_);
//////    Mat dst = HSV2RGB(merge_);
////
////    cvtColor(merge_,dst,COLOR_HSV2BGR);
//    return;
//}
//
//
//extern "C" JNIEXPORT jboolean JNICALL
//Java_org_jni_NativeUtils_PartialcolorJudge(JNIEnv *env, jobject obj, jlong addrImg) {
//    //https://blog.csdn.net/just_sort/article/details/84897976
//    Mat &imgLab = *(Mat *) addrImg;
//
//    Mat_<Vec3b>::iterator begin = imgLab.begin<Vec3b>();
//    Mat_<Vec3b>::iterator end = imgLab.end<Vec3b>();
//    float suma = 0, sumb = 0;
//    for (; begin != end; begin++) {
//        suma += (*begin)[1];//a
//        sumb += (*begin)[2];//b
//    }
//    int MN = imgLab.rows * imgLab.cols;
//    double Da = suma / MN - 128; //归一化到[-128,127]
//    double Db = sumb / MN - 128; //同上
//    //求平均色度
//    double D = sqrt(Da * Da + Db * Db);
//    begin = imgLab.begin<Vec3b>();
//    double Ma = 0, Mb = 0;
//    //求色度中心距
//    for (; begin != end; begin++) {
//        Ma += abs((*begin)[1] - 128 - Da);
//        Mb += abs((*begin)[2] - 128 - Db);
//    }
//    Ma = Ma / MN;
//    Mb = Mb / MN;
//    double M = sqrt(Ma * Ma + Mb * Mb);
//    float K = float(D / M);
//    if (K >= 1.5) {
//        return true;
//    }
//    else {
//        return false;
//    }
//}
//
////Mat Adaptive2DGammaCorrection(Mat src) {
////
////    //https://blog.csdn.net/just_sort/article/details/88569129
////    int row = src.rows;
////    int col = src.cols;
////    Mat now = RGB2HSV(src);
////    Mat H(row, col, CV_32FC1);
////    Mat S(row, col, CV_32FC1);
////    Mat V(row, col, CV_32FC1);
////    for (int i = 0; i < row; i++) {
////        for (int j = 0; j < col; j++) {
////            H.at<float>(i, j) = now.at<Vec3f>(i, j)[0];
////            S.at<float>(i, j) = now.at<Vec3f>(i, j)[1];
////            V.at<float>(i, j) = now.at<Vec3f>(i, j)[2];
////        }
////    }
////    int kernel_size = min(row, col);
////    if (kernel_size % 2 == 0) {
////        kernel_size -= 1;
////    }
////    float SIGMA1 = 15;
////    float SIGMA2 = 80;
////    float SIGMA3 = 250;
////    float q = sqrt(2.0);
////    Mat F(row, col, CV_32FC1);
////    Mat F1, F2, F3;
////    GaussianBlur(V, F1, Size(kernel_size, kernel_size), SIGMA1 / q);
////    GaussianBlur(V, F2, Size(kernel_size, kernel_size), SIGMA2 / q);
////    GaussianBlur(V, F3, Size(kernel_size, kernel_size), SIGMA3 / q);
////    for (int i = 0; i < row; i++) {
////        for (int j = 0; j < col; j++) {
////            F.at <float>(i, j) = (F1.at<float>(i, j) + F2.at<float>(i, j) + F3.at<float>(i, j)) / 3.0;
////        }
////    }
////    float average = mean(F)[0];
////    Mat out(row, col, CV_32FC1);
////    for (int i = 0; i < row; i++) {
////        for (int j = 0; j < col; j++) {
////            float gamma = powf(0.5, (average - F.at<float>(i, j)) / average);
////            out.at<float>(i, j) = powf(V.at<float>(i, j), gamma);
////        }
////    }
////    vector <Mat> v;
////    v.push_back(H);
////    v.push_back(S);
////    v.push_back(out);
////    Mat merge_;
////    merge(v, merge_);
////    Mat dst = HSV2RGB(merge_);
////    return dst;
////}