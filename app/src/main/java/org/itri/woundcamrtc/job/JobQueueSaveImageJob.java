package org.itri.woundcamrtc.job;

//import com.flir.flironesdk.Frame;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceResponse;
import android.widget.ImageView;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.drew.tools.FileUtil;
import com.google.android.gms.common.util.IOUtils;
//import com.flir.flironesdk.RenderedImage;


import net.sqlcipher.database.SQLiteDatabase;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.GrabcutActivity;
import org.itri.woundcamrtc.R;
import org.itri.woundcamrtc.analytics.HomographyHelper;
import org.itri.woundcamrtc.analytics.ScaleTransfer;
import org.itri.woundcamrtc.helper.FileHelper;
import org.itri.woundcamrtc.helper.Model3DHelper;
import org.itri.woundcamrtc.helper.SecretDbHelper;
import org.itri.woundcamrtc.helper.ServiceHelper;
import org.itri.woundcamrtc.helper.XSslHttpURLConnection;
import org.itri.woundcamrtc.helper.XSslOkHttpClient;
import org.itri.woundcamrtc.preview.FileUtility;
import org.jni.NativeUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.itri.woundcamrtc.MainActivity;
import org.itri.woundcamrtc.helper.DBTableHelper;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;


public class JobQueueSaveImageJob extends Job {
    private final String TAG = getClass().getSimpleName();
    private JobManager jobQueueManager = null;
    //    private Context context;
    private MainActivity activity;
    private static Mat homography = null;
    private byte[] cameraFrame = null;
    // private Frame flirFrame = null;
    private ImageView view;
    //    private int width = 0;
//    private int height = 0;
    private double blue_area = 0;
    //    private int red_area = 0;
    private int itemId = 0;
    private double rect_width = 0.0;
    private double rect_height = 0.0;
    //    private double rect_width2 = 0.0;
//    private double rect_height2 = 0.0;
    private int type = 0;
    private Uri outputMediaFileUri;
    public SQLiteDatabase Sercretdb;
    public SecretDbHelper sqllitesecret;
    boolean isEmpty = false;
    //    DecimalFormat distanceShutFormat = new DecimalFormat("#");
    SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
    public File mainDir;
    public DBTableHelper database;
//    static {
//        System.loadLibrary("opencv_java3");
//    }

    public JobQueueSaveImageJob(JobManager jobQueueManager, String tag, MainActivity activity, byte[] df, int type, final ImageView view, int itemId) {

        //super(new Params(PRIORITY).requireNetwork().persist().groupBy(tag));

        super(new Params(Thread.NORM_PRIORITY + 3).groupBy("saveJob"));
        this.jobQueueManager = jobQueueManager;
        this.activity = activity;
        this.cameraFrame = df;
        this.type = type;
        this.view = view;
        this.itemId = itemId;
//        this.context = context;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {

//        long start = System.currentTimeMillis();
//        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//        File mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);

//        if (!mainDir.mkdirs()) {
//            //Log.e("", "無法建立目錄");
//        }

//        File backup = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.BackUp_DIR);
        //
//        if (!backup.mkdirs()) {
//            //Log.e("", "無法建立目錄");
//        }

        //
//        if (!file.mkdirs()) {
//            //Log.e("", "無法建立目錄");
//        }

        jobQueueManager.addJobInBackground(new JobQueueBeepJob(jobQueueManager, "", activity.beepManager, 1));
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
        File mediaFile = new File(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + itemId + "_jpg.jpg");
        AppResultReceiver.lastColorJpegPath = mediaFile.getAbsolutePath();
        outputMediaFileUri = Uri.fromFile(mediaFile);
        Log.v("照片位址mediaFile", "outputMediaFileUri=" + mediaFile.getAbsolutePath());
        Log.v("照片位址", "outputMediaFileUri=" + outputMediaFileUri.toString());

        Mat src = new Mat();
        int src_cols = 0;
        int src_rows = 0;
//        if (false) {
////        using write file, and read file, it take 2675 ms
//            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mediaFile));
//            bos.write(cameraFrame);
//            bos.flush();
//            bos.close();
//
//            activity.setPreviewPic(AppResultReceiver.lastColorJpegPath);
//
//            Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//            int rotation = display.getRotation();
//            switch (rotation) {
//                case Surface.ROTATION_0: //0 垂直轉90
//                    AppResultReceiver.lastPicNeedRotate = true;
//                    src = Imgcodecs.imread(mediaFile.getAbsolutePath());
//                    Core.transpose(src, src);
//                    Core.flip(src, src, 1);
//                    Imgcodecs.imwrite(mediaFile.getAbsolutePath(), src);
//                    src_rows = src.cols();
//                    src_cols = src.rows();
//
//                    //如果有使用藍點偵測再計算藍點長寬pixel值
//                    if (AppResultReceiver.IS_USED_MARKER_DETECTION) {
////            if (AppResultReceiver.detectMarkerW != 0 && AppResultReceiver.detectMarkerH != 0) {
////                double scaleW = (double)src_rows  / (double)AppResultReceiver.detectMarkerFrameW;
////                double scaleH = (double)src_cols/ (double)AppResultReceiver.detectMarkerFrameH;
////                rect_width = scaleW * AppResultReceiver.detectMarkerW;
////                rect_height = scaleH * AppResultReceiver.detectMarkerH;
////                blue_area = (int) (Math.max(scaleW,scaleH) * AppResultReceiver.detectMarkerA);
////            } else {
//                        showReferencedMarker(outputMediaFileUri, src);
////            }
//                    }
//                    src.release();
//                    AppResultReceiver.lastPicNeedRotate = false;
//                    break;
//                case Surface.ROTATION_90: //1 左平
//                    AppResultReceiver.lastPicNeedRotate = false;
//                    src = Imgcodecs.imread(mediaFile.getAbsolutePath());
//                    src_cols = src.cols();
//                    src_rows = src.rows();
//                    //如果有使用藍點偵測再計算藍點長寬pixel值
//                    if (AppResultReceiver.IS_USED_MARKER_DETECTION) {
////            if (AppResultReceiver.detectMarkerW != 0 && AppResultReceiver.detectMarkerH != 0) {
////                double scaleW = (double)src_rows  / (double)AppResultReceiver.detectMarkerFrameW;
////                double scaleH = (double)src_cols/ (double)AppResultReceiver.detectMarkerFrameH;
////                rect_width = scaleW * AppResultReceiver.detectMarkerW;
////                rect_height = scaleH * AppResultReceiver.detectMarkerH;
////                blue_area = (int) (Math.max(scaleW,scaleH) * AppResultReceiver.detectMarkerA);
////            } else {
//                        showReferencedMarker(outputMediaFileUri, src);
////            }
//                    }
//                    src.release();
//                    break;
//                case Surface.ROTATION_180: //2 上下倒
//                    AppResultReceiver.lastPicNeedRotate = true;
//                    src = Imgcodecs.imread(mediaFile.getAbsolutePath());
//                    Core.transpose(src, src);
//                    Core.flip(src, src, 0);
//                    Imgcodecs.imwrite(mediaFile.getAbsolutePath(), src);
//                    src_cols = src.cols();
//                    src_rows = src.rows();
//                    //如果有使用藍點偵測再計算藍點長寬pixel值
//                    if (AppResultReceiver.IS_USED_MARKER_DETECTION) {
////            if (AppResultReceiver.detectMarkerW != 0 && AppResultReceiver.detectMarkerH != 0) {
////                double scaleW = (double)src_rows  / (double)AppResultReceiver.detectMarkerFrameW;
////                double scaleH = (double)src_cols/ (double)AppResultReceiver.detectMarkerFrameH;
////                rect_width = scaleW * AppResultReceiver.detectMarkerW;
////                rect_height = scaleH * AppResultReceiver.detectMarkerH;
////                blue_area = (int) (Math.max(scaleW,scaleH) * AppResultReceiver.detectMarkerA);
////            } else {
//                        showReferencedMarker(outputMediaFileUri, src);
////            }
//                    }
//                    src.release();
//                    AppResultReceiver.lastPicNeedRotate = false;
//                    break;
//                case Surface.ROTATION_270: //3 右平轉180
//                    AppResultReceiver.lastPicNeedRotate = true;
//                    src = Imgcodecs.imread(mediaFile.getAbsolutePath());
//                    Core.flip(src, src, -1);
//                    Imgcodecs.imwrite(mediaFile.getAbsolutePath(), src);
//                    src_cols = src.cols();
//                    src_rows = src.rows();
//                    //如果有使用藍點偵測再計算藍點長寬pixel值
//                    if (AppResultReceiver.IS_USED_MARKER_DETECTION) {
////            if (AppResultReceiver.detectMarkerW != 0 && AppResultReceiver.detectMarkerH != 0) {
////                double scaleW = (double)src_rows  / (double)AppResultReceiver.detectMarkerFrameW;
////                double scaleH = (double)src_cols/ (double)AppResultReceiver.detectMarkerFrameH;
////                rect_width = scaleW * AppResultReceiver.detectMarkerW;
////                rect_height = scaleH * AppResultReceiver.detectMarkerH;
////                blue_area = (int) (Math.max(scaleW,scaleH) * AppResultReceiver.detectMarkerA);
////            } else {
//                        showReferencedMarker(outputMediaFileUri, src);
////            }
//                    }
//                    src.release();
//                    AppResultReceiver.lastPicNeedRotate = false;
//                    break;
//            }
//
//        } else {
        //using bitmap memory to mat, need convert BGR2RGB, it take 2308 ms
//            Bitmap bitmap = BitmapFactory.decodeByteArray(cameraFrame, 0, cameraFrame.length);
//            Utils.bitmapToMat(bitmap, src);
//            bitmap.recycle();
//            Imgcodecs.imwrite(mediaFile.getAbsolutePath(), src);


        Bitmap bitmap = BitmapFactory.decodeByteArray(cameraFrame, 0, cameraFrame.length);

        Bitmap bb = null;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int rotation = display.getRotation() + 90;
            if (Build.MODEL.endsWith("MPD100") || Build.MODEL.endsWith("MPD500")) {
                rotation = -90;
            }
            if (rotation != 0)
                bb = rotateBitmap(bitmap, rotation);
            else
                bb = bitmap;
        } else {
            bb = bitmap;
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mediaFile);
            bb.compress(Bitmap.CompressFormat.JPEG, 70, out);
            src_rows = bb.getHeight();
            src_cols = bb.getWidth();
            bitmap.recycle();
            bb.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (AppResultReceiver.IS_USED_MARKER_DETECTION)
            detectReferencedMarker(outputMediaFileUri, null);
        AppResultReceiver.lastPicNeedRotate = false;

        // for data encrypt to read again and embed encrypt key
        //把圖片加密
        if (dataEncrypt == true) {
            Log.v(TAG, "把圖片加密");

            String fileName = outputMediaFileUri.getPath();
            Log.v(TAG, "fileName : " + fileName);
            FileHelper.overwriteFileSecret(fileName);


//            InputStream ins = null;
//            OutputStream outfile = null;
//            try {
//                String fileName = outputMediaFileUri.getPath();
//                ins = new FileInputStream(fileName);
//
//                if (fileName.toLowerCase().endsWith(".png")) {
//                    byte[] targetArray = IOUtils.toByteArray(ins);
//                    String chs = ServiceHelper.getSerialNumber("0");
//                    int chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
//                    targetArray[1] = 0x10;
//                    targetArray[2] = (byte) chn;
//                    ins = new ByteArrayInputStream(targetArray);
//                } else if (fileName.toLowerCase().endsWith(".jpg")) {
//                    byte[] targetArray = IOUtils.toByteArray(ins);
//                    String chs = ServiceHelper.getSerialNumber("0");
//                    int chn = Integer.parseInt(chs.substring(chs.length() - 1), 16);
//                    targetArray[0xb1] = (byte) 0x10;
//                    targetArray[0xb2] = (byte) chn;
//                    ins = new ByteArrayInputStream(targetArray);
//                }
//
//                try {
//                    outfile = new FileOutputStream(fileName);
//                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
//                    int read;
//
//                    while ((read = ins.read(buffer)) != -1) {
//                        outfile.write(buffer, 0, read);
//                    }
//
//                    outfile.flush();
//                } catch (Exception e) {
//                } finally {
//                    try {
//                        if (outfile != null) {
//                            outfile.close();
//                        }
//                    } catch (Exception e) {
//                    }
//                }
//
//            } catch (Exception e) {
//            } finally {
//                try {
//                    if (ins != null) {
//                        ins.close();
//                    }
//                } catch (Exception e) {
//                }
//            }
        }
        activity.setPreviewPic(AppResultReceiver.lastColorJpegPath);
//        }


//    TAG, "save jpg 2 " + (System.currentTimeMillis() - start));


        //objectReferencedMarker(outputMediaFileUri);

        if ("".equals(activity.ownerId) || activity.ownerId.equals("Undefined")) {
            isEmpty = true;
        }

        String rectWidth = String.valueOf(rect_width);
        String rectHeight = String.valueOf(rect_height);
//        String rectWidth2 = String.valueOf(rect_width2);
//        String rectHeight2 = String.valueOf(rect_height2);
        String blueArea = String.valueOf(blue_area);
//        String redArea = String.valueOf(red_area);

        try {
            File targetTxt = null;
            File targetTxtsecret = null;
            if (dataEncrypt == false) {
                targetTxt = new File(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + activity.evlStep + "_data.txt");
            } else {

                try {
                    targetTxtsecret = new File(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + activity.evlStep + "_datax.txt");
                    if (targetTxtsecret.exists()) {
                        FileHelper.txt_decrypt(targetTxtsecret.getAbsolutePath(), 18);

                    }
                    targetTxt = new File(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + activity.evlStep + "_data.txt");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            AppResultReceiver.lastTxtFilePath = targetTxt.getAbsolutePath();
            if (!targetTxt.exists()) {
                AppResultReceiver.writeToFile(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + activity.evlStep + "_data.txt", "evlId=" + activity.evlId + "\r\nownerId=" + activity.ownerId +
                        "\r\n" + "info\r\n", true);
            }


            //AppResultReceiver.blue_width_pixel = 0.0;
            //AppResultReceiver.blue_height_pixel = 0.0;
            //AppResultReceiver.blue_area = 0.0;

            Map map = new HashMap();
            //map.put("itemId", String.valueOf(activity.count));
            map.put("itemId", String.valueOf(itemId));
            map.put("bodyPart", activity.part);

            //if (!activity.iip.equals("")) {
            BigDecimal decimalFormat = new BigDecimal(AppResultReceiver.snapshutDistance);
            map.put("distance", String.valueOf(decimalFormat.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
            Log.v(TAG, "測距: " + AppResultReceiver.snapshutDistance + " 公分");
            //}

            if (AppResultReceiver.IS_USED_MARKER_DETECTION && rect_width != 0.0 && rect_height != 0.0) {
                activity.showToast("Photo taken include marker!");
                map.put("widthPixel", rectWidth);
                map.put("heightPixel", rectHeight);
                map.put("blueArea", blueArea);
                //AppResultReceiver.blue_width_pixel = Double.valueOf(rect_width);
                //AppResultReceiver.blue_height_pixel = Double.valueOf(rect_height);
                //AppResultReceiver.blue_area = blue_area;
                Log.v(TAG, "widthPixel = " + rectWidth + " , heightPixel = " + rectHeight + " , blueArea = " + blueArea);

                if (AppResultReceiver.correctionColorDetected) {
                    map.put("calibrationColor", "1");
                    map.put("bValue", AppResultReceiver.correctionColor[0][0]);
                    map.put("gValue", AppResultReceiver.correctionColor[0][1]);
                    map.put("rValue", AppResultReceiver.correctionColor[0][2]);
                } else {
                    map.put("calibrationColor", "0");
                }

            } else {
                activity.showToast("Photo taken!");
                double[] markerPixels = ScaleTransfer.estimateMarkerPixels(AppResultReceiver.snapshutDistance);
                map.put("widthPixel", markerPixels[0]);
                map.put("heightPixel", markerPixels[1]);

                // the max cycle area will be 86.5% of the rectangle area
                double myblueArea = 0.865 * markerPixels[0] * markerPixels[1];
                map.put("blueArea", myblueArea);
                map.put("calibrationColor", "0");
            }
            //AppResultReceiver.recordList.add(map);
            /*if(AppResultReceiver.recordList.size() != 0){
                for(int i=0; i<AppResultReceiver.recordList.size(); i++){
                    JSONObject jsonObject = new JSONObject(AppResultReceiver.recordList.get(i));
                    Log.v(TAG, "物件" + (i+1) + "為: " + jsonObject.toString());
                }
            }
            Log.v(TAG, "之後recordList的size為: " + AppResultReceiver.recordList.size());*/

                /*if(activity.distance != 0.0){
                    double targetPixel = findCentimeterPixel(activity.distance);
                    AppResultReceiver.estimateWidth = Double.valueOf(df.format(rect_width2 / targetPixel));
                    AppResultReceiver.estimateHeight = Double.valueOf(df.format(rect_height2 / targetPixel));
                    AppResultReceiver.estimateArea = Double.valueOf(df.format(AppResultReceiver.estimateWidth * AppResultReceiver.estimateHeight));
                    Log.v(TAG, "預估長度: " + AppResultReceiver.estimateWidth + " ,預估寬度: " + AppResultReceiver.estimateHeight);
                }*/

            JSONObject jsonObjectJacky = new JSONObject(map);

            Log.v(TAG, "jsonObjectJacky :　" + jsonObjectJacky);
            //  writeToFile(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + activity.evlStep + "_data.txt", jsonObjectJacky +

            AppResultReceiver.writeToFile(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + activity.evlStep + "_data.txt", jsonObjectJacky +

                    "\r\n", true);

            Log.v(TAG, "targetTxt.getAbsolutePath()" + targetTxt.getAbsolutePath());
            if (dataEncrypt == false) {

            } else {

                try {
                    FileHelper.txt_encryption(targetTxt.getAbsolutePath(), 18);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        } catch (Exception e) {
            activity.showToast("write file error, " + e.getMessage());
            e.printStackTrace();
            Log.v(TAG, "寫入txt檔錯誤:" + e.getMessage());
        }

        AppResultReceiver.isTakingPicture = false;
        activity.filesize(file.getAbsolutePath());

        ContentValues initialValues = new ContentValues();
        BigDecimal decimalFormat = new BigDecimal(AppResultReceiver.snapshutDistance);

        initialValues.put("evid", activity.evlId);
        initialValues.put("date", s.format(new Date()));
        initialValues.put("userid", activity.userId);
        initialValues.put("number", itemId);
        initialValues.put("part", activity.part);
        initialValues.put("ownerId", activity.ownerId);
        initialValues.put("type", "jpg.jpg");
        initialValues.put("widthPixel", rectWidth);
        initialValues.put("heightPixel", rectHeight);
        initialValues.put("blueArea", blueArea);

        initialValues.put("calibrationColor", "0");
        initialValues.put("lastanalysis", "False");
        initialValues.put("distance", String.valueOf(decimalFormat.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
        initialValues.put("total", activity.evlId + "_" + s.format(new Date()) + "_" + itemId + "_jpg.jpg");

        //SQLite
        if (dataEncrypt == false) {
            mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
            database = DBTableHelper.getInstance(getApplicationContext(), mainDir.getAbsolutePath() + File.separator + AppResultReceiver.PROJECT_NAME + ".db");
            database.add("table_picNumber", initialValues, "", new String[]{});
        } else {
            SQLiteDatabase.loadLibs(activity);
            sqllitesecret = new SecretDbHelper(activity);
            Sercretdb = SecretDbHelper.getInstance(activity).getWritableDatabase("MIIS");
            sqllitesecret.add(Sercretdb, "table_picNumber", initialValues, "", new String[]{});
        }

        // 彩色疊合熱感照片
        if (AppResultReceiver.IS_FOR_MIIS_MPDA) {
            AsynImageAlignment asyncTask = new AsynImageAlignment();
            asyncTask.execute(file);
        }

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        //如果重試n次仍未成功，那麼就放棄任務，也會進入onCancel
        if (runCount == 1)
            return RetryConstraint.CANCEL;
        return RetryConstraint.RETRY;
    }

    //如果重試超過限定次數，會執行onCancel
    //如果使用者主動放棄此任務，也一樣進入onCancel
    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        if (type == 1) {
            // camera image
            // jpeg data
        }
    }

//    public void writeToFile(String outFilename, String msg, boolean append) {
//        BufferedWriter writer = null;
//        try {
//            File textFile = new File(outFilename);
//            writer = new BufferedWriter(new FileWriter(textFile, append));
//            writer.write(msg);
//            writer.flush();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                // Close the writer regardless of what happens...
//                writer.close();
//            } catch (Exception e) {
//            }
//        }
//    }

    public Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.setRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    //pixel轉換對應公分(取至小數點後第一位)
    public double pixelToCentiMeter(double result) {
        DecimalFormat df = new DecimalFormat("#.#");
        double finalResult = Double.valueOf(df.format(result));
        return finalResult;
    }

    public double findCentimeterPixel(double dist) {
        double targetPixel = 0.0;
        targetPixel = (-0.002 * Math.pow(dist, 3)) + (0.2835 * Math.pow(dist, 2)) - (14.062 * dist) + 286.11;
        return targetPixel;
    }

    public void detectReferencedMarker(Uri uri, Mat src) {
        rect_width = 0.0;
        rect_height = 0.0;
        Mat frame;
        if (src == null || src.empty()) {
            Log.v(TAG, "圖檔: " + uri.toString());
            String targetFileName = uri.toString().replace("file://", "");
            if (!AppResultReceiver.dataEncrypt)
                frame = imread(targetFileName.replace("%20", " "));
            else
                frame = FileHelper.imreadSecret(targetFileName.replace("%20", " "));
        } else {
            frame = src;
        }

//        imwrite(uri.toString().replace("file://","").replace(".jpg","_bixn.jpg"),src);

        // only process submat ROI
//        if (AppResultReceiver.detectMarkerW != 0 && AppResultReceiver.detectMarkerH != 0) {
//            if (homography == null) {
//                homography = HomographyHelper.getImage2ScreenHomography(activity);
//            }
//            Mat dstPoint = new Mat();
//            Mat srcPoint = new Mat();
//            srcPoint.push_back(new MatOfPoint2f(new Point(AppResultReceiver.detectMarkerX - AppResultReceiver.detectMarkerW * 1.5, AppResultReceiver.detectMarkerY - AppResultReceiver.detectMarkerH * 1.5)));
//            srcPoint.push_back(new MatOfPoint2f(new Point(AppResultReceiver.detectMarkerX + AppResultReceiver.detectMarkerW * 2.5, AppResultReceiver.detectMarkerY + AppResultReceiver.detectMarkerH * 2.5)));
//            Core.perspectiveTransform(srcPoint, dstPoint, homography);
//
//            int xx1 = (int) Math.max(0,Math.min(frame.cols(), Math.min(dstPoint.get(0, 0)[0], dstPoint.get(1, 0)[0])));
//            int yy1 = (int) Math.max(0,Math.min(frame.rows(), Math.min(dstPoint.get(0, 0)[1], dstPoint.get(1, 0)[1])));
//            int xx2 = (int) Math.max(0,Math.min(frame.cols(), Math.max(dstPoint.get(0, 0)[0], dstPoint.get(1, 0)[0])));
//            int yy2 = (int) Math.max(0,Math.min(frame.rows(), Math.max(dstPoint.get(0, 0)[1], dstPoint.get(1, 0)[1])));
//            frame = frame.submat(yy1, yy2, xx1, xx2);
//        }
        //Mat frameRf = new Mat(frame.rows(),frame.cols(), CvType.CV_8UC3);

        //activity.setPreviewPic(frame);

        Mat threshImage = new Mat();
        //新建目標輸出影像
        Mat hsv_image = new Mat();
        Mat lower_red_hue_range = new Mat();
        //進行影像彩色空間轉換(讀入影像,輸出影像,顏色轉換)不為空
        //將圖片從RGB空間轉換到HSV空間
        Imgproc.cvtColor(frame, hsv_image, Imgproc.COLOR_BGR2HSV);
        //提取圖片中特定顏色(暗紅色->亮淺藍色)
        //Core.inRange(hsv_image, new Scalar(80, 35, 30), new Scalar(110, 255, 255), lower_red_hue_range);
        //Core.inRange(hsv_image, new Scalar(100, 70, 40), new Scalar(120, 255, 255), lower_red_hue_range);  // 20191024
        //Core.inRange(hsv_image, new Scalar(100, 100, 40), new Scalar(120, 255, 200), lower_red_hue_range); // 20191027
        Core.inRange(hsv_image, new Scalar(95, 80, 20), new Scalar(130, 255, 255), lower_red_hue_range); // 20200528


//        Mat tmp = new Mat();

        //腐蝕(erode)函式表示是影像高亮區域將減少(亦即會侵蝕高亮的區域)
        //src : Mat 輸入影象 對通道數無要求，但是 depth 必須是 CV_8U、CV_16U、CV_16S、CV_32F、CV_64F 之一
        //dst : Mat 輸出影象，與原圖以上的尺寸與型別
        //kernel : Mat 膨脹操作的核 ， null 時表示以當前畫素為中心 3x3 為單位的核
        //erode(lower_red_hue_range, tmp, new Mat());        //使用方式 erode(Mat src, Mat dst, Mat kernel)

        //erode(tmp, lower_red_hue_range, new Mat());

        //膨脹(dilate)函示表示是影像高亮區域會增加(亦即會侵蝕非高亮的區域)
        //使用方式 dilate(Mat src, Mat dst, Mat kernel)
        //src : Mat 輸入影象 對通道數無要求，但是 depth 必須是 CV_8U、CV_16U、CV_16S、CV_32F、CV_64F 之一
        //dst : Mat 輸出影象，與原圖以上的尺寸與型別
        //kernel : Mat 膨脹操作的核 ， null 時表示以當前畫素為中心 3x3 為單位的核
        //dilate(lower_red_hue_range, tmp, new Mat());
        //dilate(tmp, lower_red_hue_range, new Mat());
        //dilate(lower_red_hue_range, tmp, new Mat());
        //dilate(tmp, lower_red_hue_range, new Mat());

        //erode(lower_red_hue_range, tmp, new Mat());
        //erode(tmp, lower_red_hue_range, new Mat());

        //影像可以分為前景(感興趣的部分)、背景(不感興趣的部分)，閾值(threshold)可當作強度標準值，超過則當前景，反之亦然
        //閾值(臨界點)設為250、最大值設為255、選擇 THRESH_BINARY 型態方式( 超過閾值的像素設為最大值(maxval)，小於閾值的設為0 )
//        Imgproc.threshold(lower_red_hue_range, threshImage, 1, 255, Imgproc.THRESH_BINARY);

        //儲存影像至對應目錄( 影像檔名為_bin.jpg )
        //imwrite(uri.toString().replace("file://","").replace(".jpg","_bin.jpg"),threshImage);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Log.v(TAG, String.valueOf(contours.size()));
        //輪廓檢測函式(藉此找到面積最大的輪廓並繪製輪廓)
        //尋找輪廓(輸入圖像, 每個輪廓儲存點, 模式(採只取最外層的輪廓 ), 儲存輪廓方法(採對水平、垂直、對角線留下頭尾點))
        double refMarkerAspectUpper = 1.0 + AppResultReceiver.refMarkerAspectRange;
        double refMarkerAspectLower = 1.0 - AppResultReceiver.refMarkerAspectRange;
        Imgproc.findContours(lower_red_hue_range, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (Iterator<MatOfPoint> iterator = contours.iterator(); iterator.hasNext(); ) {
            MatOfPoint op = iterator.next();
            //獲取輪廓的最小外接矩形
            org.opencv.core.Rect rect2 = Imgproc.boundingRect(op);

            // 147 pixels @ 25cm on MPD100
            // 72 pixels @ 60cm on MPD100
            if (rect2.width < 72 || rect2.height < 72) {
                iterator.remove();
            } else {
                double ratio = rect2.size().width / rect2.size().height;
                if (ratio > refMarkerAspectUpper || ratio < refMarkerAspectLower) {
                    iterator.remove();
                } else {
//                    Log.d(TAG,"");
                }
            }
        }

        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                // first return 1, last return -1
                // bigger first
                if (o1 == null || o2 == null) return 0;
                org.opencv.core.Rect rect1 = Imgproc.boundingRect(o1);
                org.opencv.core.Rect rect2 = Imgproc.boundingRect(o2);
                int rectSize1 = rect1.width * rect1.height;
                int rectSize2 = rect2.width * rect2.height;
                if (rectSize1 > rectSize2) {
                    return 1;
                } else if (rectSize1 == rectSize2) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });


//        double maxArea = 40000;
//        double minArea = 1000;
//        float[] radius = new float[1];
//        Point center = new Point();
        double ratio = 0;
        int area = 0;
        double areaMin = 0;
        double areaMax = 0;
        //找出匹配到的最大輪廓
        for (int i = 0; i < contours.size(); i++) {
            //獲取輪廓的最小外接矩形
            org.opencv.core.Rect rect2 = Imgproc.boundingRect(contours.get(i));

            //計算輪廓面積 (給定幾個座標點即可算出)
            // the max cycle area will be 86.5% of the rectangle area
            MatOfPoint c = contours.get(i);
            area = (int) Imgproc.contourArea(c);
            areaMin = 0.565 * rect2.size().width * rect2.size().height;
            areaMax = 0.965 * rect2.size().width * rect2.size().height;
            if (area > areaMax || area < areaMin)
                continue;

            blue_area = area;
            Log.d(TAG, i + " blue_contourArea:" + area + ", w:" + rect2.size().width + ", h:" + rect2.size().height);

            // 要抓正矩形, 看 x, y 變形多少
            rect_width = rect2.width;
            rect_height = rect2.height;


//                    //將MatOfPoint轉換成MatOfPoint2f
//                    MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
//                    //獲取點集最小外接圓點
//                    Imgproc.minEnclosingCircle(c2f, center, radius);
//
//                    //取得外接矩形
//                    RotatedRect rrect = Imgproc.minAreaRect(c2f);
//                    rect_width = rect.w.size.width;
//                    rect_height = rect.size.height;
//
//                    if(rrect.size.width < rrect.size.height){
//                        rect_width = rrect.size.height;
//                        rect_height = rrect.size.width;
//                    }
//
//                    //建立四個頂點
//                    Point [] rect_points = new Point[4];
//                    rrect.points( rect_points );
//                    for ( int j = 0; j < 4; j++ ) {
//                        //於圖像上畫線(從座標A到座標B畫一條厚度為2公分的紅線
//                        Log.d(TAG, "x點: " + j + "_" + rect_points[j]);
//                        Log.d(TAG, "y點: " + j + "_" + rect_points[(j+1)%4]);
//                        Imgproc.line( frame, rect_points[j], rect_points[(j+1)%4], new Scalar(0, 255, 0), 10 );
//                    }

            // markerMat正中央->上方逆時針開始為白->藍->綠->黑->紅
            //X0 = cols/2;
            //Y0 = rows/2;
            //X1 = cols/2;
            //Y1 = rows/5;
            //X2= cols/10*7;
            //Y2= rows/10*4;
            //X3= cols/10*5;
            //Y3= rows/10*7;
            //X4= cols/10*3;
            //Y4= rows/10*4;
            //if (AppResultReceiver.correctionColorDetected) {
            AppResultReceiver.correctionColor[0][0] = 0;
            AppResultReceiver.correctionColor[0][1] = 0;
            AppResultReceiver.correctionColor[0][2] = 0;
            AppResultReceiver.correctionColorDetected = false;
            //}
            Mat markerMat = new Mat(frame, rect2);
            double[] edgePoint = markerMat.get(markerMat.rows() / 7, markerMat.cols() / 2);
            double[] markerCenter = markerMat.get(markerMat.rows() / 2, markerMat.cols() / 2);
            if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_MARKER) {
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                String fileName = file.getPath() + File.separator + s.format(new Date());
                Imgcodecs.imwrite(fileName + "_markerMat.png", markerMat);
            }
//            // 若中間顏色和邊緣色不同時, 代表用的是校色marker 必須是絕對灰色
            //if (Math.abs(edgePoint[0] - markerCenter[0]) + Math.abs(edgePoint[1] - markerCenter[1]) + Math.abs(edgePoint[2] - markerCenter[2]) > 180) {
            //if (Math.abs(edgePoint[1] - markerCenter[1]) + Math.abs(edgePoint[2] - markerCenter[2]) > 128) {
            if (Math.abs(edgePoint[0] - markerCenter[0]) + Math.abs(edgePoint[1] - markerCenter[1]) + Math.abs(edgePoint[2] - markerCenter[2]) > 20) {
                AppResultReceiver.correctionColor[0][0] = (int) (markerCenter[0]);
                AppResultReceiver.correctionColor[0][1] = (int) (markerCenter[1]);
                AppResultReceiver.correctionColor[0][2] = (int) (markerCenter[2]);
                AppResultReceiver.correctionColorDetected = true;
//                AppResultReceiver.correctionColor[1][0] = (int) (edgePoint[0]);
//                AppResultReceiver.correctionColor[1][1] = (int) (edgePoint[1]);
//                AppResultReceiver.correctionColor[1][2] = (int) (edgePoint[2]);
//                edgePoint = markerMat.get(markerMat.rows() / 10 * 4, markerMat.cols() / 10 * 7);
//                AppResultReceiver.correctionColor[2][0] = (int) (edgePoint[0]);
//                AppResultReceiver.correctionColor[2][1] = (int) (edgePoint[1]);
//                AppResultReceiver.correctionColor[2][2] = (int) (edgePoint[2]);
//                edgePoint = markerMat.get(markerMat.rows() / 10 * 7, markerMat.cols() / 10 * 5);
//                AppResultReceiver.correctionColor[3][0] = (int) (edgePoint[0]);
//                AppResultReceiver.correctionColor[3][1] = (int) (edgePoint[1]);
//                AppResultReceiver.correctionColor[3][2] = (int) (edgePoint[2]);
//                edgePoint = markerMat.get(markerMat.rows() / 10 * 4, markerMat.cols() / 10 * 3);
//                AppResultReceiver.correctionColor[4][0] = (int) (edgePoint[0]);
//                AppResultReceiver.correctionColor[4][1] = (int) (edgePoint[1]);
//                AppResultReceiver.correctionColor[4][2] = (int) (edgePoint[2]);
//
//
//                Mat lab = new Mat();
//                Imgproc.cvtColor(markerMat, lab, Imgproc.COLOR_BGR2HLS);
//                List<Mat> listSplited = new ArrayList<Mat>(3);
//                Core.split(lab, listSplited);
//                Core.MinMaxLocResult mmlr = Core.minMaxLoc(listSplited.get(1));
//                double min = mmlr.minVal; // Math.min(mmlr.minVal, 0);
//                double max = mmlr.maxVal; // Math.max(mmlr.maxVal, 255);
//                AppResultReceiver.correctionColorAlpha = 256.0 / (max - min);
//                AppResultReceiver.correctionColorBeta = -min * AppResultReceiver.correctionColorAlpha;
//                //AppResultReceiver.correctionColorBeta = -min;
//
//                AppResultReceiver.correctionColoring = true;
//                if (AppResultReceiver.DEBUG_LEVEL > 0) {
//                    SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
//                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                    String fileName = file.getPath() + File.separator + s.format(new Date());
//                    Imgcodecs.imwrite(fileName + "_markerMat.png", markerMat);
//                }
//            } else {
//                AppResultReceiver.correctionColoring = false;
//                AppResultReceiver.correctionColor[0][0] = 0;
//                AppResultReceiver.correctionColor[0][1] = 0;
//                AppResultReceiver.correctionColor[0][2] = 0;
//                AppResultReceiver.correctionColor[1][0] = 0;
//                AppResultReceiver.correctionColor[1][1] = 0;
//                AppResultReceiver.correctionColor[1][2] = 0;
//                AppResultReceiver.correctionColor[2][0] = 0;
//                AppResultReceiver.correctionColor[2][1] = 0;
//                AppResultReceiver.correctionColor[2][2] = 0;
//                AppResultReceiver.correctionColor[3][0] = 0;
//                AppResultReceiver.correctionColor[3][1] = 0;
//                AppResultReceiver.correctionColor[3][2] = 0;
//                AppResultReceiver.correctionColor[4][0] = 0;
//                AppResultReceiver.correctionColor[4][1] = 0;
//                AppResultReceiver.correctionColor[4][2] = 0;
//                AppResultReceiver.correctionColorAlpha = 0.0;
//                AppResultReceiver.correctionColorBeta = 0.0;
            } else {
                AppResultReceiver.correctionColor[0][0] = 0;
                AppResultReceiver.correctionColor[0][1] = 0;
                AppResultReceiver.correctionColor[0][2] = 0;
                AppResultReceiver.correctionColorDetected = false;
            }
            break;
        }

        //寫入藍色參考物體框圖檔
        //imwrite(uri.toString().replace("file://","").replace(".jpg","_blue.jpg").replace("%20"," "),frame);

        contours.clear();
        lower_red_hue_range.release();
        hsv_image.release();
        threshImage.release();
    }

    //    public void objectReferencedMarker(Uri uri) {
////        rect_width2 = 0.0;
////        rect_height2 = 0.0;
//        String targetFileName = uri.toString().replace("file://", "");
//        Mat frame = imread(targetFileName.replace("%20", " "));
//        //Mat frameRf = new Mat(frame.rows(),frame.cols(), CvType.CV_8UC3);
//
//        Mat threshImage = new Mat();
//        //新建目標輸出影像
//        Mat hsv_image = new Mat();
//        Mat lower_red_hue_range = new Mat();
//        //進行影像彩色空間轉換(讀入影像,輸出影像,顏色轉換)不為空
//        //將圖片從RGB空間轉換到HSV空間
//        Imgproc.cvtColor(frame, hsv_image, Imgproc.COLOR_BGR2HSV);
//        //提取圖片中特定顏色(暗紅色->亮淺藍色)
//        Core.inRange(hsv_image, new Scalar(0, 100, 120), new Scalar(10, 255, 255), lower_red_hue_range);
//
//        Mat tmp = new Mat();
//
//        //腐蝕(erode)函式表示是影像高亮區域將減少(亦即會侵蝕高亮的區域)
//        //src : Mat 輸入影象 對通道數無要求，但是 depth 必須是 CV_8U、CV_16U、CV_16S、CV_32F、CV_64F 之一
//        //dst : Mat 輸出影象，與原圖以上的尺寸與型別
//        //kernel : Mat 膨脹操作的核 ， null 時表示以當前畫素為中心 3x3 為單位的核
//        erode(lower_red_hue_range, tmp, new Mat());        //使用方式 erode(Mat src, Mat dst, Mat kernel)
//
//        erode(tmp, lower_red_hue_range, new Mat());
//
//        //膨脹(dilate)函示表示是影像高亮區域會增加(亦即會侵蝕非高亮的區域)
//        //使用方式 dilate(Mat src, Mat dst, Mat kernel)
//        //src : Mat 輸入影象 對通道數無要求，但是 depth 必須是 CV_8U、CV_16U、CV_16S、CV_32F、CV_64F 之一
//        //dst : Mat 輸出影象，與原圖以上的尺寸與型別
//        //kernel : Mat 膨脹操作的核 ， null 時表示以當前畫素為中心 3x3 為單位的核
//        dilate(lower_red_hue_range, tmp, new Mat());
//        dilate(tmp, lower_red_hue_range, new Mat());
//        dilate(lower_red_hue_range, tmp, new Mat());
//        dilate(tmp, lower_red_hue_range, new Mat());
//
//        erode(lower_red_hue_range, tmp, new Mat());
//        erode(tmp, lower_red_hue_range, new Mat());
//
//        //影像可以分為前景(感興趣的部分)、背景(不感興趣的部分)，閾值(threshold)可當作強度標準值，超過則當前景，反之亦然
//        //閾值(臨界點)設為250、最大值設為255、選擇 THRESH_BINARY 型態方式( 超過閾值的像素設為最大值(maxval)，小於閾值的設為0 )
//        Imgproc.threshold(lower_red_hue_range, threshImage, 250, 255, Imgproc.THRESH_BINARY);
//
//        //儲存影像至對應目錄( 影像檔名為_bin.jpg )
//        //imwrite(uri.toString().replace("file://","").replace(".jpg","_bin.jpg"),threshImage);
//
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Log.v(TAG, String.valueOf(contours.size()));
//        //輪廓檢測函式(藉此找到面積最大的輪廓並繪製輪廓)
//        //尋找輪廓(輸入圖像, 每個輪廓儲存點, 模式(採只取最外層的輪廓 ), 儲存輪廓方法(採對水平、垂直、對角線留下頭尾點))
//        Imgproc.findContours(threshImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//        double maxArea = 40000;
//        double minArea = 1000;
//        //找出匹配到的最大輪廓
//        for (int i = 0; i < contours.size(); i++) {
//            //獲取輪廓的最小外接矩形
//            org.opencv.core.Rect rect2 = Imgproc.boundingRect(contours.get(i));
//            double ratio = rect2.size().width / rect2.size().height;
//            Log.d(TAG, String.valueOf(ratio));
//            if (ratio < 2.7 && ratio > 0.6) {
//                MatOfPoint c = contours.get(i);
//                //計算輪廓面積 (給定幾個座標點即可算出)
//                int area = (int) Imgproc.contourArea(c);
//                //Log.d(TAG, String.valueOf(0.675 * rect2.size().height * rect2.size().height));
//                //Log.d(TAG, String.valueOf(0.865 * rect2.size().height * rect2.size().height));
//
//                if (rect2.size().width > 100 && rect2.size().height > 100) {
////                    red_area = area;
//                    Log.d(TAG, i + "red_contourArea:" + area + ", w:" + rect2.size().width + ", h:" + rect2.size().height);
//                    //將MatOfPoint轉換成MatOfPoint2f
//                    MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
//
//                    //取得外接矩形
//                    RotatedRect rrect = Imgproc.minAreaRect(c2f);
////                    rect_width2 = rrect.size.width;
////                    rect_height2 = rrect.size.height;
//
//                    if (rrect.size.width < rrect.size.height) {
////                        rect_width2 = rrect.size.height;
////                        rect_height2 = rrect.size.width;
//                    }
//
//                    //Log.v(TAG, "後長度= " + rect_width2);
//
//                    //建立四個頂點
//                    Point[] rect_points = new Point[4];
//                    rrect.points(rect_points);
//                    for (int j = 0; j < 4; j++) {
//                        //於圖像上畫線(從座標A到座標B畫一條厚度為2公分的紅線
//                        Log.d(TAG, "x點: " + j + "_" + rect_points[j]);
//                        Log.d(TAG, "y點: " + j + "_" + rect_points[(j + 1) % 4]);
//                        Imgproc.line(frame, rect_points[j], rect_points[(j + 1) % 4], new Scalar(0, 255, 0), 10);
//                    }
//                } else {
//                    Log.d(TAG, i + " contourArea size error:" + area + ", w:" + rect2.size().width + ", h:" + rect2.size().height);
//                }
//            }
//        }
//
//        //寫入紅色參考物體框圖檔
//        //imwrite(uri.toString().replace("file://","").replace(".jpg","_red.jpg").replace("%20"," "),frame);
//
//        contours.clear();
//        lower_red_hue_range.release();
//        hsv_image.release();
//        threshImage.release();
//
//    }

    private class AsynImageAlignment extends AsyncTask<File, Void, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(File... files) {
            try {
                File file = (File) files[0];
                try {
                    Thread.sleep(1000);
                } catch (Exception ex){}
                File thermalFile = new File(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + (99 + itemId) + "_thm.png");
                if (thermalFile.exists()) {
                    try {
                        if (HomographyHelper.extrinsic3dCalibrationMatrixs == null) {
                            HomographyHelper.initCalibrationMatrixs(AppResultReceiver.mMainActivity);
                        }
                        Mat rgb2thmMatrix = null;
                        if (Model3DHelper.mMatrixWarpType == HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE)
                            rgb2thmMatrix = HomographyHelper.get2dAlignementMatrixs(7, (int) AppResultReceiver.snapshutDistance);
                        else
                            rgb2thmMatrix = HomographyHelper.get2dAlignementMatrixs(11, Model3DHelper.mProximityDist);
                        Mat img = HomographyHelper.getAlignmedImg(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + itemId + "_jpg.jpg", new Size(120, 160), rgb2thmMatrix, 7);
                        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
                        Imgproc.equalizeHist(img, img);
                        Imgproc.cvtColor(img, img, Imgproc.COLOR_GRAY2BGR);
                        Mat org = null;
                        if (!AppResultReceiver.dataEncrypt)
                            org = Imgcodecs.imread(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + (99 + itemId) + "_thm.png");
                        else
                            org = FileHelper.imreadSecret(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + (99 + itemId) + "_thm.png");

                        if (org.rows() == img.rows() && org.cols() == img.cols()) {
                            Core.addWeighted(org, 0.7, img, 0.3, 1, img);
                            Imgcodecs.imwrite(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + (99 + itemId) + "_thm.png", img);
                            img.release();
                            if (AppResultReceiver.dataEncrypt) {
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception ex) {
                                }
                                FileHelper.overwriteFileSecret(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + (99 + itemId) + "_thm.png");
                            }
                        } else {
                            Log.d(TAG, "thermal size not equals raw.rows:"+org.rows()+",raw.cols:"+org.cols() + ", img.rows:"+img.rows()+", img.cols:"+img.cols());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Log.d(TAG,"thermal File not exist");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }

            return "Executed";
        }
    }

}