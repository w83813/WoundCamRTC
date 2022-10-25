package org.itri.woundcamrtc.job;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.MainActivity;
import org.itri.woundcamrtc.helper.FileHelper;
import org.itri.woundcamrtc.helper.StringUtils;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;
import static org.itri.woundcamrtc.AppResultReceiver.SAVE_DIR;

public class JobQueueSaveUVCImageJob extends Job {
    private final String TAG = getClass().getSimpleName();
    private JobManager jobQueueManager = null;
    private MainActivity activity;
    private String distance = "0.0";

    public JobQueueSaveUVCImageJob(JobManager jobQueueManager, MainActivity activity, String distance) {

        super(new Params(Thread.NORM_PRIORITY + 1).groupBy("saveJob"));
        //super(new Params(Thread.NORM_PRIORITY + 1).groupBy("saveUVCImageJob"));
        this.jobQueueManager = jobQueueManager;
        this.activity = activity;
        this.distance = distance;
    }

    protected void finalize() {
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        try {
            final SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
//            //
//            if (!file.mkdirs()) {
//                Log.e("", "無法建立目錄");
//            }

            //測試網址用
            if (StringUtils.isBlank(activity.iip))
                return;
            //java.net.URL url = new java.net.URL("https://cw1.tw/CH/images/channel_master/e1b94e86-955d-460d-a9f5-d565318a6696.jpg");
            java.net.URL url = new java.net.URL("http://" + activity.iip + ":9000/ipcam01/image.jpg?action=123");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);
            connection.setDoInput(true);
            connection.connect();
            Log.v(TAG, "通過目前responseCode = " + connection.getResponseCode());
            if (connection.getResponseCode() == 200) {
                InputStream input = connection.getInputStream();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                myBitmap.recycle();
                stream.flush();
                stream.close();

                //新增熱感影像檔案名稱
                int heatImageCount = 99 + activity.count;
                File mediaFile = new File(file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + heatImageCount + "_jpg.jpg");
                Log.v("檔案", Uri.fromFile(mediaFile).toString());

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mediaFile));
                bos.write(byteArray);

                bos.flush();
                bos.close();

                Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                int rotation = display.getRotation();

                switch (rotation) {
                    case Surface.ROTATION_0: //0 垂直轉90
                        Mat src = Imgcodecs.imread(mediaFile.getAbsolutePath());
                        Core.transpose(src, src);
                        Core.flip(src, src, 0);
                        Mat resizeimage = new Mat();
                        Imgproc.resize(src, resizeimage, new Size(src.cols() * 3, src.rows() * 3));

                        if (!AppResultReceiver.dataEncrypt)
                            Imgcodecs.imwrite(mediaFile.getAbsolutePath(), resizeimage);
                        else
                            FileHelper.imwriteSecret(mediaFile.getAbsolutePath(), resizeimage);

                        src.release();
                        resizeimage.release();
                        break;
                    case Surface.ROTATION_90: //1 左平
                        Mat src1 = Imgcodecs.imread(mediaFile.getAbsolutePath());
                        Core.transpose(src1, src1);
                        Core.flip(src1, src1, 1);
                        Core.transpose(src1, src1);
                        Core.flip(src1, src1, 1);
                        Mat resizeimage1 = new Mat();
                        Imgproc.resize(src1, resizeimage1, new Size(src1.cols() * 3, src1.rows() * 3));

                        if (!AppResultReceiver.dataEncrypt)
                            Imgcodecs.imwrite(mediaFile.getAbsolutePath(), resizeimage1);
                        else
                            FileHelper.imwriteSecret(mediaFile.getAbsolutePath(), resizeimage1);

                        src1.release();
                        resizeimage1.release();
                        break;
                    case Surface.ROTATION_180: //2 上下倒
                        Mat src2 = Imgcodecs.imread(mediaFile.getAbsolutePath());
                        Core.transpose(src2, src2);
                        Core.flip(src2, src2, 1);
                        Mat resizeimage2 = new Mat();
                        Imgproc.resize(src2, resizeimage2, new Size(src2.cols() * 3, src2.rows() * 3));

                        if (!AppResultReceiver.dataEncrypt)
                            Imgcodecs.imwrite(mediaFile.getAbsolutePath(), resizeimage2);
                        else
                            FileHelper.imwriteSecret(mediaFile.getAbsolutePath(), resizeimage2);

                        src2.release();
                        resizeimage2.release();
                        break;
                    case Surface.ROTATION_270: //3 右平轉180
                        Mat src3 = Imgcodecs.imread(mediaFile.getAbsolutePath());
                        Mat resizeimage3 = new Mat();
                        Imgproc.resize(src3, resizeimage3, new Size(src3.cols() * 3, src3.rows() * 3));

                        if (!AppResultReceiver.dataEncrypt)
                            Imgcodecs.imwrite(mediaFile.getAbsolutePath(), resizeimage3);
                        else
                            FileHelper.imwriteSecret(mediaFile.getAbsolutePath(), resizeimage3);

                        src3.release();
                        resizeimage3.release();
                        break;
                }

                try {
                    //尋找原來的txt檔並寫入距離


                    if (dataEncrypt == false) {

                    } else {
                        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
                        String filename_sercret = file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + activity.evlStep + "_datax.txt";
                        File target_sercret = new File(mPicDir, filename_sercret);

                        try {
                            FileHelper.txt_decrypt(target_sercret.getAbsolutePath(), 18);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    String path = file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + activity.evlStep + "_data.txt";
                    File targetTxt = new File(path);

                    if (!targetTxt.exists()) {
                        AppResultReceiver.writeToFile(path, "evlId=" + activity.evlId + "\r\nownerId=" + activity.ownerId +
                                "\r\n" + "info\r\n", true);
                    }

                    Map map = new HashMap();
                    map.put("itemId", String.valueOf(activity.count + 99));
                    map.put("bodyPart", activity.part);
                    //AppResultReceiver.recordList.add(map);

                    JSONObject obj = new JSONObject(map);
                    AppResultReceiver.writeToFile(path, obj + "\r\n", true);
                    if (dataEncrypt == false) {

                    } else {
                        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
                        String filename_sercret = file.getPath() + File.separator + activity.evlId + "_" + s.format(new Date()) + "_" + activity.evlStep + "_data.txt";
                        File target_sercret = new File(mPicDir, filename_sercret);

                        try {
                            FileHelper.txt_encryption(target_sercret.getAbsolutePath(), 18);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            activity.filesize(file.getAbsolutePath());

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //      activity.menuImageButton_ShowData.setImageResource(R.mipmap.radar3);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isJsonFormat(String str) {
        boolean result = false;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
//            e.printStackTrace();
        }
        return result;
    }

    public int objNum(String str) {
        int idx = 0;
        try {
            JSONObject jsonObject = new JSONObject(str);
            idx = jsonObject.getInt("itemId");
            Log.v(TAG, "itemId為:" + String.valueOf(idx));
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return idx;
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

    }


//    public void writeToFile(String outFilename, String msg, boolean append) {
//        BufferedWriter writer = null;
//        try {
//            Log.i("writeToFile", "JobQueueSaveUVCImageJob.onRun" + outFilename);
//            Log.i("writeToFile", "JobQueueSaveUVCImageJob.onRun" + msg);
//            File textFile = new File(outFilename);
//
//            writer = new BufferedWriter(new FileWriter(textFile, append));
//            writer.write(msg);
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
}
