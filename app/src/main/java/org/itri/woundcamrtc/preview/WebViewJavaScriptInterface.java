package org.itri.woundcamrtc.preview;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.BodyPartActivity;
import org.itri.woundcamrtc.GrabcutActivity;
import org.itri.woundcamrtc.HistoryActivity;
import org.itri.woundcamrtc.PreviewActivity;
import org.itri.woundcamrtc.R;
import org.itri.woundcamrtc.WebviewActivity;
import org.itri.woundcamrtc.analytics.HomographyHelper;
import org.itri.woundcamrtc.helper.DBTableHelper;
import org.itri.woundcamrtc.helper.FileHelper;
import org.itri.woundcamrtc.helper.Model3DHelper;
import org.itri.woundcamrtc.helper.SecretDbHelper;
import org.itri.woundcamrtc.helper.StringUtils;
import org.itri.woundcamrtc.helper.XSslLiteHttp;
import org.itri.woundcamrtc.job.JobQueueUploadFileJob;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.support.v7.app.AppCompatActivity;

import com.google.webviewlocalserver.WebViewLocalServer;

import net.sqlcipher.database.SQLiteDatabase;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.itri.woundcamrtc.AppResultReceiver.SAVE_DIR;
import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;
import static org.itri.woundcamrtc.AppResultReceiver.mMainActivity;
import static org.itri.woundcamrtc.GrabcutActivity.fileName;
import static org.webrtc.ContextUtils.getApplicationContext;

/**
 * Created by schung on 2017/9/23.
 */

public class WebViewJavaScriptInterface {
    private PreviewActivity previewActivity = null;
    private HistoryActivity historyActivity = null;

    private WebviewActivity webviewActivity = null;
    private static final String TAG = "WebViewJSInterface";
    private AppCompatActivity context;
    private WebView webView;
    // 在應用程式中放一份快取
    private Properties profile;
    private long createTime;
    private Context bContext;
    public SQLiteDatabase Sercretdb;
    public SecretDbHelper sqllitesecret;
    public File file;
    public File mainDir;
    DBTableHelper database;
    public static boolean bl_singleupload = false;

    public WebViewJavaScriptInterface(AppCompatActivity context, WebView webView, PreviewActivity previewActivity, HistoryActivity historyActivity, WebviewActivity webviewActivity) {
        this.context = context;
        this.webView = webView;
        this.previewActivity = previewActivity;
        this.historyActivity = historyActivity;
        this.webviewActivity = webviewActivity;

        if (dataEncrypt == false) {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
            database = DBTableHelper.getInstance(getApplicationContext(), file.getAbsolutePath() + File.separator + AppResultReceiver.PROJECT_NAME + ".db");

        } else {
            if (historyActivity != null) {
                SQLiteDatabase.loadLibs(historyActivity);
                sqllitesecret = new SecretDbHelper(historyActivity);
                Sercretdb = SecretDbHelper.getInstance(historyActivity).getWritableDatabase("MIIS");
            } else if (previewActivity != null) {
                SQLiteDatabase.loadLibs(previewActivity);
                sqllitesecret = new SecretDbHelper(previewActivity);
                Sercretdb = SecretDbHelper.getInstance(previewActivity).getWritableDatabase("MIIS");
            } else {
                SQLiteDatabase.loadLibs(webviewActivity);
                sqllitesecret = new SecretDbHelper(webviewActivity);
                Sercretdb = SecretDbHelper.getInstance(webviewActivity).getWritableDatabase("MIIS");
            }

        }

    }

    /*
     * This method can be called from Android. @JavascriptInterface
     * required after SDK version 17.
     */

    @JavascriptInterface
    public void postMessage(String jsonString) {
        JSONObject cmd = null;
        try {
            cmd = new JSONObject(jsonString);
            String method = cmd.getString("method");
            Log.i(TAG, "呼叫APP端的Function " + method);
            if (method.equals("vibrating")) {
                AppResultReceiver.vibrating(context);
            }
            // 取得照片列表
            if (method.equals("getPhotoList")) {
                Log.i(TAG,"getPhotoList");
                this.getPhotoList();
            }
            // 刪除照片
            if (method.equals("delPhotoList")) {
                AppResultReceiver.vibrating(context);
                String photos = cmd.getString("photos");
                Log.v(TAG, "被刪除的照片如下：" + photos);
                JSONArray list = new JSONArray(photos);
                this.delPhotoList(list);
            }
            // 儲存TxtData
            if (method.equals("saveTxtData")) {

                AppResultReceiver.vibrating(context);
                String params = cmd.getString("params");
                this.saveTxtData(params);
            }
            // 返回上一層的ActivityView
            if (method.equals("goback")) {
                AppResultReceiver.vibrating(context);

                if (previewActivity != null) previewActivity.isShowLogoutHint = false;
                if (historyActivity != null) historyActivity.isShowLogoutHint = false;
                this.goback();
            }

            if (method.equals("checkpart")) {

                AppResultReceiver.vibrating(context);
                String params = cmd.getString("params");
                JSONObject jsonObject = new JSONObject(params);
                String ownerId = jsonObject.getString("charNo");
                String bodyPart = jsonObject.getString("bodyPart");
                if (historyActivity != null) historyActivity.isShowLogoutHint = false;
                this.checkpart(ownerId, bodyPart);
            }
            // 跳轉至分析ActivityView
            if (method.equals("gotoAnalysis")) {
                Log.i(TAG,"gotoAnalysis");
                AppResultReceiver.vibrating(context);
                String params = cmd.getString("params");
                //String imagePath = cmd.getString("imagePath");
                if (previewActivity != null) previewActivity.isShowLogoutHint = false;
                this.gotoAnalysis(params);
            }
            // 跳轉至部位選擇ActivityView
            if (method.equals("gotoBodyPartPicker")) {
                AppResultReceiver.vibrating(context);
                String params = cmd.getString("params");
                if (previewActivity != null) previewActivity.isShowLogoutHint = false;
                this.gotoBodyPartPicker(params);
            }

            if (method.equals("singleUpload")) {
                this.singleUpload();
            }

            //回傳深度d及溫度k，從彩色影像疊合座標x,y
            if (method.equals("getSensingValueWithRGBCoord")) {


                String params = cmd.getString("params");
                JSONObject jsonObject = new JSONObject(params);
                String fileName = jsonObject.getString("f");
                String xxx = jsonObject.getString("x");
                String yyy = jsonObject.getString("y");
                String cnt = jsonObject.getString("cnt");
                String type = jsonObject.getString("type");


//                // for test
//                if (type.equals("j") || type.equals("g")) {
//                    xxx = Integer.toString((new java.util.Random()).nextInt(2447) + 1);
//                    yyy = Integer.toString((new java.util.Random()).nextInt(3263) + 1);
//                } else {
//                    xxx = Integer.toString((new java.util.Random()).nextInt(119) + 1);
//                    yyy = Integer.toString((new java.util.Random()).nextInt(159) + 1);
//                }

                if (type.equals("g")){
                    xxx = String.valueOf(Double.parseDouble(xxx) * 3.4);
                    yyy = String.valueOf(Double.parseDouble(yyy) * 3.4);
                }

                Log.i(TAG, "xxx : " + xxx + ", yyy : " + yyy);

                DecimalFormat df = new DecimalFormat(".#");
                double sensingValueD = -999;
                double sensingValueC = -999;


                try {
                    String[] emp = fileName.split("_");
                    String filePath = emp[0].substring(emp[0].indexOf("/storage/"), emp[0].indexOf(emp[1]));
                    String evlId = emp[0].substring(emp[0].indexOf(emp[1]));
                    if (type.equals("j") || type.equals("g")) // color jpeg
                        Model3DHelper.get2dAligmentParams(AppResultReceiver.mMainActivity, filePath, evlId, emp[1], Integer.parseInt(emp[2]), Model3DHelper.mMatrixWarpType);
                    else
                        Model3DHelper.get2dAligmentParams(AppResultReceiver.mMainActivity, filePath, evlId, emp[1], Integer.parseInt(emp[2]) - 99, Model3DHelper.mMatrixWarpType);
                    //HomographyHelper.printMat("Model3DHelper.mRgb2depthMatrix", Model3DHelper.mRgb2depthMatrix);
                    //HomographyHelper.printMat("Model3DHelper.mRgb2thmMatrix", Model3DHelper.mRgb2thmMatrix);
                    //HomographyHelper.printMat("Model3DHelper.mThm2depthMatrix", Model3DHelper.mThm2depthMatrix);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    if (type.equals("j") || type.equals("g")) // color jpeg
                        sensingValueD = HomographyHelper.getSensingValueWithRgbCoord(new Point3(Double.parseDouble(xxx), Double.parseDouble(yyy), 0), Model3DHelper.mRgb2depthMatrix, Model3DHelper.mMatDepth) / 160.0;
                    else //thermal png
                        sensingValueD = HomographyHelper.getSensingValueWithRgbCoord(new Point3(Double.parseDouble(xxx), Double.parseDouble(yyy), 0), Model3DHelper.mThm2depthMatrix, Model3DHelper.mMatDepth) / 160.0;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    if (type.equals("j") || type.equals("g")) // color jpeg
                        sensingValueC = HomographyHelper.getSensingValueWithRgbCoord(new Point3(Double.parseDouble(xxx), Double.parseDouble(yyy), 0), Model3DHelper.mRgb2thmMatrix, Model3DHelper.mMatThermal);
                    else {//thermal png
                        //90 degrees clockwise
                        double xx = Double.parseDouble(xxx);
                        double yy = Double.parseDouble(yyy);
//                        double cosAngle = Math.cos(Math.PI / 2);
//                        double sinAngle = Math.sin(Math.PI / 2);
//                        int XX = (int) ((xx - 60) * cosAngle - (yy - 80) * sinAngle) + 80;
//                        int YY = (int) ((xx - 60) * sinAngle + (yy - 80) * cosAngle) + 60;
//                        Log.i(TAG, "XX : " + XX + ", YY : " + YY);
                        sensingValueC = HomographyHelper.getSensingValue(new Point3(xx, yy, 0), Model3DHelper.mMatThermal);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                Log.i(TAG, "sensingValueD : " + sensingValueD + ",sensingValueC : " + sensingValueC);

                Point3 spacePoint = getPixel2cm(new Point3(Double.parseDouble(xxx), Double.parseDouble(yyy), sensingValueD));

                org.opencv.core.Point3 thermalPoint = HomographyHelper.rgb2sensingCoord(new Point3(Double.parseDouble(xxx), Double.parseDouble(yyy), 0), Model3DHelper.mRgb2thmMatrix);

                Log.i(TAG, "thm_point.x = "+thermalPoint.x + ", thm_point.y = "+thermalPoint.y);
                try {
                    final String execCallbackJs = String.format("javascript:onGetSensingValueWithRGBCoordCallback('%s', %s, %s, %s, %s, %s, %s, %s);", "", cnt, df.format(sensingValueC), df.format(sensingValueD), df.format(spacePoint.x), df.format(spacePoint.y), df.format(thermalPoint.x), df.format(thermalPoint.y));
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(execCallbackJs);
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            //要求3D加彩色影像疊合資訊之取得
            if (method.equals("getGen3DColorImage")) {
                AppResultReceiver.vibrating(context);
                String params = cmd.getString("params");
                String filePathAndName = this.getGen3DColorImage(params);
                Log.v(TAG, "filePathAndName");
                if (StringUtils.isNotBlank(filePathAndName)) {
                    //回傳3D加彩色影像疊合資訊之取得
                    try {
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
                        String filePath = file.getAbsolutePath();
                        if (!filePath.endsWith(File.separator))
                            filePath = filePath + File.separator;
                        file = null;
                        filePathAndName = filePathAndName.substring(0, filePathAndName.length() - 4);
                        filePathAndName = filePathAndName.replace(filePath, "");
                        try {
                            Intent intent = new Intent(context, WebviewActivity.class);
                            intent.putExtra(WebviewActivity.REQUESTED_3D_PATH, filePath);
                            intent.putExtra(WebviewActivity.REQUESTED_3D_FILE, filePathAndName);
                            Log.v(TAG, "REQUESTED_3D_PATH" + filePath);
                            Log.v(TAG, "REQUESTED_3D_FILE" + filePathAndName.replace(filePath, ""));

                            if (FileHelper.isExist(filePath, filePathAndName + ".obj", false) && FileHelper.isExist(filePath, filePathAndName + ".mtl", false)) {
                                if (previewActivity != null)
                                    previewActivity.isShowLogoutHint = false;
                                context.startActivityForResult(intent, 0);
                            } else {
                                jsAlert(mMainActivity.getString(R.string.alert_error), mMainActivity.getString(R.string.file_not_exist));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.i(TAG, "呼叫JS端的Function getGen3DColorImage");
                    } catch (Exception ex) {
                        Log.e(TAG, "呼叫JS端的Function getGen3DColorImage Error", ex);
                    }
                } else {
                    jsAlert(mMainActivity.getString(R.string.alert_error), mMainActivity.getString(R.string.file_not_exist));
                }
            }

            if (method.equals("getGen3DThermalImage")) {
                AppResultReceiver.vibrating(context);
                String params = cmd.getString("params");
                String filePathAndName = this.getGen3DThermalImage(params);
                if (StringUtils.isNotBlank(filePathAndName)) {
                    //回傳3D加熱感影像疊合資訊之取得
                    try {
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
                        String filePath = file.getAbsolutePath();
                        if (!filePath.endsWith(File.separator))
                            filePath = filePath + File.separator;
                        file = null;
                        filePathAndName = filePathAndName.substring(0, filePathAndName.length() - 4);
                        filePathAndName = filePathAndName.replace(filePath, "");
                        try {
                            Intent intent = new Intent(context, WebviewActivity.class);
                            intent.putExtra(WebviewActivity.REQUESTED_3D_PATH, filePath);
                            intent.putExtra(WebviewActivity.REQUESTED_3D_FILE, filePathAndName);
                            if (FileHelper.isExist(filePath, filePathAndName + ".obj", false) && FileHelper.isExist(filePath, filePathAndName + ".mtl", false)) {
                                if (previewActivity != null)
                                    previewActivity.isShowLogoutHint = false;
                                context.startActivityForResult(intent, 0);
                            } else {
                                jsAlert(mMainActivity.getString(R.string.alert_error), mMainActivity.getString(R.string.file_not_exist));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.i(TAG, "呼叫JS端的Function getGen3DThermalImage");
                    } catch (Exception ex) {
                        Log.e(TAG, "呼叫JS端的Function getGen3DThermalImage Error", ex);
                    }
                } else {
                    jsAlert(mMainActivity.getString(R.string.alert_error), mMainActivity.getString(R.string.file_not_exist));
                }
            }

        } catch (Exception ex) {
            Log.e(TAG, "Parse JSON String Error", ex);
            return;
        }
    }

    private void getPhotoListEx() {
        try {
            //
            // 檢查目錄有沒有存在
            // --------------------------------------------------------------------------------
            File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
            if (!mPicDir.exists()) {
                Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + "不存在");
            }

            //
            // 1. 整理出要上傳的 groupIds (List of EvalID)
            // --------------------------------------------------------------------------------
            List<String> groupIds = new ArrayList<String>();
            Map<String, Map<String, String>> filenamesByGroupId = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            try {
                //
                // 1.1 取得要上傳的檔案列表,並依檔名排序 jpg, png, txt
                // --------------------------------------------------------------------------------
                String[] filenames = mPicDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if (filename.endsWith("_jpg.jpg") || filename.endsWith("_thm.png") || filename.endsWith(".txt")) {
                            Log.v(TAG, "TEst1 : " + filename);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

                //
                // 1.2 留下唯一 groupId 產生 String<groupIds> 及 Map<itemId, filename>
                // --------------------------------------------------------------------------------
                for (String filename : filenames) {
                    // filename 是相對路徑格式
                    try {
                        File file = new File(mPicDir, filename);
                        if (file.exists()) {
                            String fileName = file.getName();
                            String filePath = file.getAbsolutePath();
                            String groupId = fileName.substring(0, "yyyy-MM-dd HH-mm-ss-SSS".length());
                            String[] nameSplit = file.getAbsoluteFile().getName().split("_");
                            String itemId = nameSplit[2];

                            Map<String, String> filenameMap = filenamesByGroupId.get(groupId);
                            if (filenameMap == null) {
                                filenameMap = new TreeMap<String, String>();
                                filenameMap.put(itemId, fileName);
                                filenamesByGroupId.put(groupId, filenameMap);
                            } else {
                                filenameMap.put(itemId, fileName);
                            }
                            if (!groupIds.contains(groupId) && StringUtils.isValidDate(groupId, "")) {
                                groupIds.add(groupId);
                            }
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Get Photo File Error", ex);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            JSONArray item_list = new JSONArray();
            for (String groupId : groupIds) {
                TxtData txt = FileUtility.getTxtDataFromDownload(this.context, groupId);
                Map<String, String> filenameMap = filenamesByGroupId.get(groupId);
                if (txt == null) {
                    Log.d(TAG, "群組 " + groupId + " 不存在Txt資料");
                    continue;
                }
                //
                JSONObject group = new JSONObject();
                // 已分析過的部位
                Set<String> analyzedBodyParts = new HashSet<String>();
                group.put("id", groupId);
                group.put("title", groupId);
                if (txt.getOwnerId() != null) {
                    group.put("ownerId", txt.getOwnerId());
                }
                group.put("evlId", txt.getEvlId());
                // 從圖片列表與Txt資料Info中取資料
                Map<String, JSONObject> info = txt.getInfo();
                JSONArray info_list = new JSONArray();
                for (String itemId : filenameMap.keySet()) {
                    String imageId = filenameMap.get(itemId);
//                    String imagePath = map.get(imageId);
                    File img = new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId));

                    JSONObject _info = info.get(itemId);
                    if (_info == null) {
                        Log.d(TAG, "Item " + itemId + " 沒有對應的資料");
                        continue;
                    }
                    /* 格式 Example
                    {
                    "itemId":"2",
                    "bodyPart":"右手",
                    "distance":"14.3",
                    "width":1.8,
                    "height":2.0,
                    "area":33.3,
                    "slough":25,
                    "eschar":75,
                    "epithelium":0,
                    "granular":0,
                    "heightPixel":"291.0",
                    "widthPixel":"296.0",
                    "analysisTime":"1479249799770"
                    }
                    */
                    //
                    String bodyPart = _info.getString("bodyPart");
                    // 判斷這張照片是否已分析過
                    if (!_info.isNull("analysisTime") && !analyzedBodyParts.contains(bodyPart)) {
                        analyzedBodyParts.add(bodyPart);
                    }
                    //
                    if (img.exists()) {
                        Date dt = new Date(img.lastModified());
                        _info.put("createTime", timeFormat.format(dt));
                    }
                    //
                    _info.put("imagePath", mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId));
                    info_list.put(_info);
                }

                for (int i = 0; i < info_list.length(); i++) {

                }

                group.put("info", info_list);
                JSONArray bodyparts = new JSONArray(analyzedBodyParts);
                group.put("bodyparts", bodyparts);
                //
                item_list.put(group);
            }
            //
//            long stop = System.currentTimeMillis();
//            Log.d(TAG, "取得照片列表時間: " + (stop - start) + " ms");
            //
            JSONObject data = new JSONObject();
            data.put("list", item_list);
            final String result = data.toString();
            try {
                // 呼叫JS端的Function
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            webView.loadUrl("javascript:onGetPhotoList('" + result + "')");
                            Log.i(TAG, "呼叫JS端的Function onGetPhotoList");
                        } catch (Exception ex) {
                            Log.e(TAG, "呼叫JS端的Function onGetPhotoList Error", ex);
                        }
                    }
                });
            } catch (Exception ex) {
                Log.e(TAG, "Call JS Error", ex);
                return;
            }
        } catch (Exception ex) {
            Log.e(TAG, "getPhotoList Error", ex);
        }
    }

    public boolean jsAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(webView.getContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok,
                new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.setCancelable(true);
        builder.create();
        builder.show();
        return true;
    }
    private void getPhotoList() {
        try {
            long start = System.currentTimeMillis();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat fileTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            // 取得檔案列表, K: imageId, V: imagePath
            Map<String, String> map = FileUtility.getImageListFromDownload(context);
            if (map == null) { // 沒有圖片
                return;
            }
            // 從圖片中去取出群組Id
            // K: 群組Id(單次看診唯一碼), V: itemId vs. imagePath Map
            Map<String, Map<String, String>> collections = new TreeMap<String, Map<String, String>>();
            for (String imageId : map.keySet()) {

                // imageId 是檔案名稱，格式為：
                // yyyy-MM-dd HH-mm-ss-SSS_yyyy-MM-dd_sn_jpg.jpg
                //int pos = Math.max(Math.max(imageId.lastIndexOf("_jpg.jpg"), imageId.lastIndexOf("_thm.png")), imageId.lastIndexOf("_3ds.jpg"));
                int pos = imageId.lastIndexOf("_jpg.jpg");
                if (pos > 0) {
                    String temp = imageId.substring(0, pos);
                    //
                    String[] field = temp.split("_");
                    if (field.length != 3) {
                        // 格式有錯誤？
                        Log.d(TAG, "格式有錯誤？ " + temp);
                        continue; // SKIP
                    }
                    // evlId, date, sn
                    String groupId = field[0];
                    String sn = field[2];

                    // K: itemId, V: imagePath(imageId)
                    Map<String, String> imageMap = null;
                    if (collections.containsKey(groupId)) {
                        imageMap = collections.get(groupId);
                    } else {
                        imageMap = new TreeMap<String, String>();
                        collections.put(groupId, imageMap);
                    }
                    //
                    imageMap.put(sn, imageId);
                }
            }

            JSONArray item_list = new JSONArray();
            // 對每一個群組進行照片與Txt資料的合併

            Object[] arr = collections.keySet().toArray();
            //針對groupId先進行倒敘排序
            Arrays.sort(arr, Collections.reverseOrder());

            for (Object arrItem : arr) {
                String groupId = (String) arrItem;
                TxtData txt = FileUtility.getTxtDataFromDownload(this.context, groupId);
                Map<String, String> treeMap = new TreeMap<String, String>(collections.get(groupId));
                //20210323更新treeSortedByValues(先比較檔案建立時間先後順序，採正序排列)
                TreeMap<String,String> treeSortedByValues = new TreeMap<String,String>(new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        File img1 = new File(map.get(treeMap.get(o1)));
                        File img2 = new File(map.get(treeMap.get(o2)));
                        String imgModifiedTime1 = fileTimeFormat.format(new Date(img1.lastModified()));
                        String imgModifiedTime2 = fileTimeFormat.format(new Date(img2.lastModified()));
                        return imgModifiedTime1.compareTo(imgModifiedTime2);
                    }
                });
                treeSortedByValues.putAll(treeMap);

                if (txt == null) {
                    Log.d(TAG, "群組 " + groupId + " 不存在Txt資料");
                    continue;
                }
                //
                JSONObject group = new JSONObject();
                // 已分析過的部位
                Set<String> analyzedBodyParts = new HashSet<String>();
                group.put("id", groupId);
                group.put("title", groupId);
                if (txt.getOwnerId() != null) {
                    group.put("ownerId", txt.getOwnerId());
                }
                group.put("evlId", txt.getEvlId());
                // 從圖片列表與Txt資料Info中取資料
                Map<String, JSONObject> info = txt.getInfo();
                JSONArray analyzedinfo_list = new JSONArray();
                JSONArray info_list = new JSONArray();
                //20210323更新for loop
                for ( Map.Entry<String, String> e : treeSortedByValues.entrySet() ) {
                    String imagePath = map.get(e.getValue());
                    File img = new File(imagePath);

                    JSONObject _info = info.get(e.getKey());
                    if (_info == null) {
                        Log.d(TAG, "Item " + e.getKey() + " 沒有對應的資料");
                        continue;
                    }
                    /* 格式 Example
                    {
                    "itemId":"2",
                    "bodyPart":"右手",
                    "distance":"14.3",
                    "width":1.8,
                    "height":2.0,
                    "area":33.3,
                    "slough":25,
                    "eschar":75,
                    "epithelium":0,
                    "granular":0,
                    "heightPixel":"291.0",
                    "widthPixel":"296.0",
                    "analysisTime":"1479249799770"
                    }
                    */
                    //
                    String bodyPart = _info.getString("bodyPart");
                    // 判斷這張照片是否已分析過
                    if (!_info.isNull("analysisTime") && !analyzedBodyParts.contains(bodyPart)) {
                        analyzedBodyParts.add(bodyPart);
                    }
                    //
                    if (img.exists()) {
                        Date dt = new Date(img.lastModified());
                        _info.put("createTime", timeFormat.format(dt));
                    }
                    //
                    _info.put("imagePath", imagePath);

                    //20210323更新-確認該照片是否有分析過，如有分析則加入analyzedinfo_list，未分析則加入info_list
                    if(_info.has("epithelium")){
                        analyzedinfo_list.put(_info);
                    }
                    else {
                        info_list.put(_info);
                    }
                }

                for(int i=0; i <analyzedinfo_list.length(); i++) {
                    info_list.put(analyzedinfo_list.get(i));
                }

                group.put("info", info_list);
                JSONArray bodyparts = new JSONArray(analyzedBodyParts);
                group.put("bodyparts", bodyparts);
                //
                item_list.put(group);
            }
            //
            long stop = System.currentTimeMillis();
            Log.d(TAG, "取得照片列表時間: " + (stop - start) + " ms");
            //
            JSONObject data = new JSONObject();
            data.put("list", item_list);
            Log.d(TAG, "item_list: " + item_list);
            final String result = data.toString();
            try {
                // 呼叫JS端的Function
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            webView.loadUrl("javascript:onGetPhotoList('" + result + "')");
                            Log.i(TAG, "呼叫JS端的Function onGetPhotoList");
                        } catch (Exception ex) {
                            Log.e(TAG, "呼叫JS端的Function onGetPhotoList Error", ex);
                        }
                    }
                });
            } catch (Exception ex) {
                Log.e(TAG, "Call JS Error", ex);
                return;
            }
        } catch (Exception ex) {
            Log.e(TAG, "getPhotoList Error", ex);
        }
    }


    private void delPhotoList(JSONArray list) {
        String message = "";
        try {
            String[] imagePaths = new String[list.length()];
            for (int i = 0; i < imagePaths.length; i++) {
                String urlPath = list.get(i).toString();
                if (urlPath.startsWith("http")) {
                    int pos = urlPath.indexOf("/storage/");
                    imagePaths[i] = urlPath.substring(pos);
                } else {
                    imagePaths[i] = urlPath;
                }
            }
            FileUtility.delImageFromDownload(this.context, imagePaths);
            //
            message = "刪除 " + imagePaths.length + " 張照片";
        } catch (Exception ex) {
            Log.e(TAG, "刪除照片錯誤", ex);
            message = "刪除照片錯誤: " + ex.getMessage();
        }
        //
        final String error_msg = new String(message);
        try {
            // 呼叫JS端的Function
            webView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        webView.loadUrl("javascript:onDeleteFinish('" + error_msg + "')");
                        Log.i(TAG, "呼叫JS端的Function onDeleteFinish");
                    } catch (Exception ex) {
                        Log.e(TAG, "呼叫JS端的Function onDeleteFinish Error", ex);
                    }
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Call JS Error", ex);
            return;
        }
    }

    private void saveTxtData(String params) {
        try {
            Log.d(TAG, "Data: " + params);
            JSONObject data = new JSONObject(params);
            String evlId = data.getString("evlId");
            ContentValues ownerIdValues = new ContentValues();
            ownerIdValues.put("ownerId", data.getString("ownerId"));


            ContentValues bodyPartValues = new ContentValues();
            bodyPartValues.put("part", data.getString("bodyPart"));

            if (dataEncrypt == false) {
                database.addOrUpdateRaw("table_picNumber", ownerIdValues, "evid=?", new String[]{evlId});
                database.addOrUpdateRaw("table_picNumber", bodyPartValues, "evid=? AND number=?", new String[]{evlId, data.getString("itemId")});
            } else {
                sqllitesecret.addOrUpdateRaw(Sercretdb, "table_picNumber", ownerIdValues, "evid=?", new String[]{evlId});
                sqllitesecret.addOrUpdateRaw(Sercretdb, "table_picNumber", bodyPartValues, "evid=? AND number=?", new String[]{evlId, data.getString("itemId")});
            }
            TxtData txtData = FileUtility.getTxtDataFromDownload(this.context, evlId);
            //
            if (txtData != null) {
                if (!data.isNull("ownerId")) {
                    txtData.setOwnerId(data.getString("ownerId"));
                    Log.d(TAG, "Update TxtData " + txtData.getOwnerId());
                }
                // 2020-04-29 修改
                //
                String[] keys = {"bodyPart", "heightPixel", "widthPixel", "width", "height", "area", "depth", "epithelium", "granular", "slough", "eschar", "analysisTime"};
                // 先更新當下的Item
                String itemId = data.getString("itemId");
                JSONObject item = txtData.getInfo().get(itemId);
                if (item != null) {
                    for (String key : keys) {
                        if (!data.isNull(key)) {
                            item.put(key, data.getString(key));
                        }
                    }
                    Log.d(TAG, "Update TxtData Item: " + item.toString());
                }

                FileUtility.saveTxtDataToDownload(this.context, txtData);
                String[] keys1 = {"bodyPart"};
                int a = Integer.valueOf(data.getString("itemId")) + 99;
                String itemId1 = String.valueOf(a);
                JSONObject item1 = txtData.getInfo().get(itemId1);
                if (itemId1 != null) {
                    for (String key : keys1) {
                        if (!data.isNull(key)) {
                            item1.put(key, data.getString(key));
                        }
                    }
                    Log.d(TAG, "Update TxtData Item: " + item1.toString());
                }
                FileUtility.saveTxtDataToDownload(this.context, txtData);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Save TxtData Error", ex);
            return;
        }
    }

    // 由 PreviewActivity 返回 MainActivity
    private void goback() {
        Log.d(TAG, "呼叫 goback");
        if (this.context instanceof PreviewActivity) {
            ((PreviewActivity) this.context).finish();
        }
        if (this.context instanceof HistoryActivity) {

            ((HistoryActivity) this.context).finish();

        }
    }

    // 由 HistoryActivity 返回 MainActivity
    private void checkpart(String ownerId, String bodyPart) {
        try {
            ((HistoryActivity) this.context).showTakePicInfo(ownerId, bodyPart);
            ((HistoryActivity) this.context).finish();

        } catch (Exception ex) {
            Log.e(TAG, "Call JS Error", ex);
            return;
        }
    }

    private void gotoAnalysis(String params) {
        // 跳轉到 AnalysisActivity
        Log.d(TAG, "跳轉到 GrabcutActivity");
        Intent intent = new Intent(this.context, GrabcutActivity.class);
        intent.putExtra("params", params);
        JSONObject json = null;
        String filename = "";
        try {


            json = new JSONObject(params);
            String filePath = json.getString("img").replace("file:///", "/");

            Log.v(TAG, "filePath" + filePath);

            if (dataEncrypt == false) {
                intent.putExtra("txtFile", filePath.split("_")[0].concat("_" + filePath.split("_")[1] + "_13_data.txt"));
            } else {
                try {
                    InputStream ins = null;

                    String b[] = filePath.split("/storage");
                    filename = "/storage" + b[1];

                    ins = FileHelper.inputStreamSecret(filename);
                    new WebResourceResponse(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(filename)),
                            "UTF-8", ins);

                    byte[] buffer = new byte[ins.available()];
                    ins.read(buffer);

                    File targetFile = new File(filename);
                    OutputStream outStream = new FileOutputStream(targetFile);
                    outStream.write(buffer);
                    Log.v(TAG, "filePath txt : " + filePath);
                    Log.v(TAG, "txtFile : " + filePath.split("_")[0].concat("_" + filePath.split("_")[1] + "_13_datax.txt"));
                    intent.putExtra("txtFile", filePath.split("_")[0].concat("_" + filePath.split("_")[1] + "_13_datax.txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }


            intent.putExtra("fileName", filePath);

            intent.putExtra("evlId", json.getString("evlId"));
            intent.putExtra("ownerId", json.getString("ownerId"));
            intent.putExtra("assignId", json.getString("itemId"));

            AppResultReceiver.snapshutDistance = json.getDouble("distance");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        //
        context.startActivityForResult(intent, PreviewActivity.REQUEST_ANALYSIS);
        if (AppResultReceiver.dataEncrypt) {
            Log.v(TAG, "   FileHelper.rewriteFileSecret(filename)" + filename);
            FileHelper.overwriteFileSecret(filename);
        }
    }

    private void gotoBodyPartPicker(String params) {
        // 跳轉到 BodyPartActivity
        Log.d(TAG, "跳轉到 BodyPartActivity");
        Intent intent = new Intent(context, BodyPartActivity.class);
        intent.putExtra("params", params);
        intent.putExtra("where", "preview");
        context.startActivityForResult(intent, PreviewActivity.REQUEST_BODY_PART_PICKER);
    }

    private void singleUpload() {



        String pattern = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{3}_[0-9]{4}-[0-9]{2}-[0-9]{2}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(fileName);
        String str_upload_file = "";
        if (m.find( )) {
            str_upload_file = m.group(0);
        }else {
            System.out.println("NO MATCH");
        }

        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
        File[] files=f.listFiles();
        String str_upload_jpg_path = "";
        String str_upload_png_path = "";
        String str_upload_txt_path = "";
        String str_upload_jpg_itemid = "";
        String str_upload_png_itemid = "";
        String str_upload_txt_itemid = "";

        Pattern r_itemid = Pattern.compile("_[0-9]{1,3}_");

        for(int i=0; i<files.length; i++)
        {
            File file = files[i];

            if (file.getAbsolutePath().contains("jpg.jpg") && file.getAbsolutePath().contains(str_upload_file)){
                str_upload_jpg_path = file.getName();
                Matcher m_itemid = r_itemid.matcher(str_upload_jpg_path);
                if (m_itemid.find( )) {
                    str_upload_jpg_itemid = m_itemid.group(0).substring(1,m_itemid.group(0).length()-1);
                }else {
                    System.out.println("NO MATCH11111");
                }


            }


            if (file.getAbsolutePath().contains(".png") &&file.getAbsolutePath().contains(str_upload_file)){
                str_upload_png_path = file.getName();
                Matcher m_itemid = r_itemid.matcher(str_upload_png_path);
                if (m_itemid.find( )) {
                    str_upload_png_itemid = m_itemid.group(0).substring(1,m_itemid.group(0).length()-1);
                }else {
                    System.out.println("NO MATCH2222");
                }

            }


            if (file.getAbsolutePath().contains(".txt") && file.getAbsolutePath().contains(str_upload_file)){
                str_upload_txt_path = file.getName();
                Matcher m_itemid = r_itemid.matcher(str_upload_txt_path);
                if (m_itemid.find( )) {
                    str_upload_txt_itemid = m_itemid.group(0).substring(1,m_itemid.group(0).length()-1);
                }else {
                    System.out.println("NO MATCH3333");
                }
            }
        }


        Pattern evlId_r = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{3}");
        Matcher evlId_m = evlId_r.matcher(str_upload_jpg_path);
        String str_evlId = "";
        if (evlId_m.find( )) {
            str_evlId = evlId_m.group(0);
        }else {
            System.out.println("NO MATCH");
        }

        Pattern time_r = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}");
        Matcher time_m = time_r.matcher(str_upload_jpg_path);
        String str_time = "";
        if (time_m.find( )) {
            str_time = time_m.group(0);
        }else {
            System.out.println("NO MATCH");
        }

        Log.v("asdsddddddddddd_1",str_time);
        Log.v("asdsddddddddddd_2",str_evlId);
        Log.v("asdsddddddddddd_3",str_upload_jpg_path);
        Log.v("asdsddddddddddd_4",str_upload_png_path);
        Log.v("asdsddddddddddd_5",str_upload_txt_path);
        Log.v("asdsddddddddddd_6",str_upload_jpg_itemid);
        Log.v("asdsddddddddddd_7",str_upload_png_itemid);
        Log.v("asdsddddddddddd_8",str_upload_txt_itemid);


        mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
        JobQueueUploadFileJob.database = DBTableHelper.getInstance(getApplicationContext(), mainDir.getAbsolutePath() + File.separator + AppResultReceiver.PROJECT_NAME + ".db");
        JobQueueUploadFileJob.mLiteHttp = (new XSslLiteHttp(this.context, 5, 360)).getLiteHttp();

        File file_jpg = new File("/storage/emulated/0/Download/WoundCamRtc/CameraImg/"+str_upload_jpg_path);
        File file_png = new File("/storage/emulated/0/Download/WoundCamRtc/CameraImg/"+str_upload_png_path);
        File file_txt = new File("/storage/emulated/0/Download/WoundCamRtc/CameraImg/"+str_upload_txt_path);

        int uploadCount = 1;

        try{
            bl_singleupload = true;
            JobQueueUploadFileJob.uploadSingleRecord(uploadCount,str_evlId,str_time,str_upload_jpg_itemid,file_jpg,false);
            uploadCount++;
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            bl_singleupload = true;
            JobQueueUploadFileJob.uploadSingleRecord(uploadCount,str_evlId,str_time,str_upload_png_itemid,file_png,false);
            uploadCount++;
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            bl_singleupload = true;
            JobQueueUploadFileJob.uploadSingleRecord(uploadCount,str_evlId,str_time,"13",file_txt,false);
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //要求3D加彩色影像疊合資訊之取得是否成功
    private String getGen3DColorImage(String params) {
        Log.v(TAG, "getGen3DColorImage");
        String evlId = AppResultReceiver.mMainActivity.evlId;
        int itemId = AppResultReceiver.mMainActivity.count;
        Intent intent = new Intent(this.context, GrabcutActivity.class);
        intent.putExtra("params", params);
        JSONObject json = null;
        try {
            json = new JSONObject(params);
            evlId = json.getString("evlId");
            itemId = Integer.parseInt(json.getString("itemId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        evlId = "2020-08-04 14-50-47-922";
//        itemId = 1;

        String retVal = "";
        String dateString = evlId.substring(0, 10);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
        String filePath = file.getAbsolutePath();
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        file = null;

        //filePath = "./assets/html/js3d/assets/";
        String path = filePath + evlId + "_" + dateString + "_" + AppResultReceiver.mMainActivity.evlStep + "_data.txt";
        Log.v(TAG, "getGen3DColorImage+path: " + path);
        if (dataEncrypt == false) {

        } else {
            File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
            String filename_sercret = evlId + "_" + dateString + "_" + AppResultReceiver.mMainActivity.evlStep + "_datax.txt";
            File target_sercret = new File(mPicDir, filename_sercret);
            Log.v(TAG, "getGen3DColorImage+target_sercret: " + target_sercret.getAbsolutePath());
            try {

                if (target_sercret.exists()) {
                    FileHelper.txt_decrypt(target_sercret.getAbsolutePath(), 18);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        File txtFile = new File(path);
        Log.v(TAG, "txtFile exists: " + txtFile.exists());
        //File glbFile = new File(filePath + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.glb");
        File rawFile = new File(filePath + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw");
        File objFile = new File(filePath + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.obj");
        if (rawFile.exists() && rawFile.length() > 0) {
            // gen 3ds.obj & 3ds.mtl
            if (!objFile.exists()) {
                Model3DHelper.Gen3DColorImage(filePath, evlId, itemId);


                //File objFile = new File(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_3ds.obj");
//            if (objFile.exists() && objFile.length()>0) {
                try {
                    // delete rawFile
//                    rawFile.delete();
                    Log.v(TAG, "path" + txtFile.getAbsolutePath());
                    if (!txtFile.exists()) {
                        Log.v(TAG, "!txtFile.exists()");
                        AppResultReceiver.writeToFile(path, "evlId=" + evlId + "\r\nownerId=" + AppResultReceiver.mMainActivity.ownerId +
                                "\r\n" + "info\r\n", true);
                    }

                    Map map = new HashMap();
                    map.put("itemId", String.valueOf(itemId + 199));
                    map.put("bodyPart", AppResultReceiver.mMainActivity.part);

                    JSONObject obj = new JSONObject(map);
                    AppResultReceiver.writeToFile(path, obj + "\r\n", true);
                    if (dataEncrypt == false) {

                    } else {

                        try {
                            Log.v(TAG, "getGen3DColorImage+txt_encryption 加密: ");
                            FileHelper.txt_encryption(txtFile.getAbsolutePath(), 18);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            retVal = filePath + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.obj";
        }
        // } else if (objFile.exists()) {
//         } else {
//            try {
//                    if (!txtFile.exists()) {
//                        writeToFile(path, "evlId=" + evlId + "\r\nownerId=" + AppResultReceiver.mMainActivity.ownerId +
//                                "\r\n" + "info\r\n", true);
//                    }
//
//                    Map map = new HashMap();
//                    map.put("itemId", String.valueOf(itemId + 199));
//                    map.put("bodyPart", AppResultReceiver.mMainActivity.part);
//
//                    JSONObject obj = new JSONObject(map);
//                    writeToFile(path, obj + "\r\n", true);
//
//                    retVal = filePath + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.obj";
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        txtFile = null;
        objFile = null;
        rawFile = null;
        file = null;

        return retVal;
    }

    //要求3D加熱感影像疊合資訊之取得是否成功
    private String getGen3DThermalImage(String params) {
        Log.v(TAG, "getGen3DThermalImage  ");
        String evlId = AppResultReceiver.mMainActivity.evlId;
        int itemId = AppResultReceiver.mMainActivity.count;
        Intent intent = new Intent(this.context, GrabcutActivity.class);
        intent.putExtra("params", params);
        JSONObject json = null;
        try {
            json = new JSONObject(params);
            evlId = json.getString("evlId");
            itemId = Integer.parseInt(json.getString("itemId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //evlId = "2020-08-10 11-40-35-337";
        //itemId = 4;

        String retVal = "";
        String dateString = evlId.substring(0, 10);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
        String filePath = file.getAbsolutePath();
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        file = null;


        //filePath = "./assets/html/js3d/assets/";
        String path = filePath + evlId + "_" + dateString + "_" + AppResultReceiver.mMainActivity.evlStep + "_data.txt";
        if (dataEncrypt == false) {

        } else {

            File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
            String filename_sercret = evlId + "_" + dateString + "_" + AppResultReceiver.mMainActivity.evlStep + "_datax.txt";
            File target_sercret = new File(mPicDir, filename_sercret);

            try {
                Log.v(TAG, "getGen3DThermalImage  target_sercret : " + target_sercret);
                if (target_sercret.exists()) {
                    FileHelper.txt_decrypt(target_sercret.getAbsolutePath(), 18);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        File txtFile = new File(path);
        //File glbFile = new File(filePath + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.glb");
        File rawFile = new File(filePath + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw");
        File objFile = new File(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.obj");
        if (rawFile.exists()) {
            // gen 3ds.obj & 3ds.mtl
            Model3DHelper.Gen3DThermalImage(filePath, evlId, itemId);

            //File objFile = new File(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_3ds.obj");
            if (objFile.exists() && objFile.length() > 0) {
                try {
                    // delete rawFile
//                    rawFile.delete();

                    if (!txtFile.exists()) {
                        AppResultReceiver.writeToFile(path, "evlId=" + evlId + "\r\nownerId=" + AppResultReceiver.mMainActivity.ownerId +
                                "\r\n" + "info\r\n", true);
                    }

                    Map map = new HashMap();
                    map.put("itemId", String.valueOf(itemId + 99));
                    map.put("bodyPart", AppResultReceiver.mMainActivity.part);

                    JSONObject obj = new JSONObject(map);
                    AppResultReceiver.writeToFile(path, obj + "\r\n", true);

                    retVal = filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.obj";

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // } else if (objFile.exists()) {
        } else {
            try {
                if (!txtFile.exists()) {
                    AppResultReceiver.writeToFile(path, "evlId=" + evlId + "\r\nownerId=" + AppResultReceiver.mMainActivity.ownerId +
                            "\r\n" + "info\r\n", true);
                }

                Map map = new HashMap();
                map.put("itemId", String.valueOf(itemId + 99));
                map.put("bodyPart", AppResultReceiver.mMainActivity.part);

                JSONObject obj = new JSONObject(map);
                AppResultReceiver.writeToFile(path, obj + "\r\n", true);

                retVal = filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.obj";
                if (dataEncrypt == false) {

                } else {

                    try {
                        Log.v(TAG, "getGen3DThermalImage  txt_encryption : ");
                        FileHelper.txt_encryption(txtFile.getAbsolutePath(), 18);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        txtFile = null;
        objFile = null;
        rawFile = null;
        file = null;

        return retVal;
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

    public static double toDecimalFormat(String val) {
        try {
            double result = Double.parseDouble(val);
            DecimalFormat df = new DecimalFormat("#.#");
            double finalResult = Double.valueOf(df.format(result));
            return finalResult;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }
    public static Point3 getPixel2cm(Point3 touchPoints){
        double imgX = touchPoints.x - 1224;
        double imgY = touchPoints.y - 1632;
        double imgD = touchPoints.z;
        double effective_pixelX = 2464.0, effective_pixelY = 3568.0;
        double radianceH = Math.toRadians(27), radianceV = Math.toRadians(36.45), radianceD = Math.toRadians(40) ;       // FOV : Diagonal = 80, Vertical = 68, Horizontal = 54
        Point3 spacePoints = new Point3();
        double spaceX = imgX * (imgD*Math.tan(radianceH)*2 / effective_pixelX );
        double spaceY = imgY * (imgD*Math.tan(radianceV)*2 / effective_pixelY );

        spacePoints.set( new double[]{spaceX, spaceY, imgD} );
        Log.i(TAG, "space Point : "+spacePoints.toString());
        return spacePoints;
    }
    public static double getdistance(int org_x, int org_y,int sec_x,int sec_y){
        String path = "/storage/emulated/0/Download/WoundCamRtc/CameraImg/";
        String[] temp_emp = AppResultReceiver.IMAGE_PATH.split("_");
        String temp_evlId = temp_emp[0].substring(temp_emp[0].indexOf(temp_emp[1]));
        int temp_itemId = Integer.parseInt(temp_emp[2]);


        String dist = Model3DHelper.GetCoordinateDistance(path, temp_evlId, temp_itemId, org_x, org_y, sec_x, sec_y);
        Log.v(TAG, "dist is " + String.valueOf(dist));
        try {
            String[] lines = dist.split(System.getProperty("line.separator"));
            for (String str : lines) {
                if (str.startsWith("long=")) {
                    try {
                        double val = toDecimalFormat(str.replace("long=", ""));

                        double org_z = HomographyHelper.getSensingValueWithRgbCoord(new Point3(Double.parseDouble(String.valueOf(org_x)), Double.parseDouble(String.valueOf(org_y)), 0), Model3DHelper.mRgb2depthMatrix, Model3DHelper.mMatDepth) / 160.0;
                        double sec_z = HomographyHelper.getSensingValueWithRgbCoord(new Point3(Double.parseDouble(String.valueOf(sec_x)), Double.parseDouble(String.valueOf(sec_y)), 0), Model3DHelper.mRgb2depthMatrix, Model3DHelper.mMatDepth) / 160.0;
                        double result_x = Math.pow((org_x - sec_x), 2);
                        double result_y = Math.pow((org_y - sec_y), 2);
                        double result_z = Math.pow((org_z - sec_z), 2);
                        double result_pow = Math.pow( (result_x + result_y + result_z) , 0.5 );
                        double pixel = val/result_pow;
                        Log.v(TAG, "pixel is " + String.valueOf(pixel));
                        return pixel;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.013243371688829969;
    }

}
