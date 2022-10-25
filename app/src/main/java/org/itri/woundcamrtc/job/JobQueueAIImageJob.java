package org.itri.woundcamrtc.job;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.GrabcutActivity;
import org.itri.woundcamrtc.analytics.GrabcutTouchView;
import org.itri.woundcamrtc.helper.FileHelper;
import org.itri.woundcamrtc.helper.XSslHttpURLConnection;
import org.itri.woundcamrtc.helper.XSslOkHttpClient;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.EventListener;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
//import okhttp3.logging.HttpLoggingInterceptor;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.itri.woundcamrtc.GrabcutActivity.GRABCAT_DOWNSAMPLE_RATE;


public class JobQueueAIImageJob extends Job {
    private final String TAG = getClass().getSimpleName();
    private JobManager jobQueueManager = null;
    private GrabcutActivity activity;
    String targetFileName = "";
    private static OkHttpClient client = null;
//    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

    public JobQueueAIImageJob(JobManager jobQueueManager, GrabcutActivity activity, String fileName) {
        super(new Params(Thread.NORM_PRIORITY - 1).groupBy("AIImageJob"));
        this.jobQueueManager = jobQueueManager;
        this.activity = activity;
        this.targetFileName = fileName;
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    protected void finalize() {
        try {
            super.finalize();
        } catch (Throwable ee) {

            ee.printStackTrace();
        }
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {

        //doTestMask();
        long startTime = System.currentTimeMillis();
        postAIRequest(AppResultReceiver.DEFAULT_POST_AI_COLOR_IMAGE_PATH, targetFileName, AppResultReceiver.AI_SERVER_IP_PORT);
        long endTime = System.currentTimeMillis();
        Log.v(TAG, "task finish time : " + (endTime - startTime) / 1000 + "sec");
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

    public interface OnPreviewListener {
        void OnCancelListener();

        void OnAcceptedListener();
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public void doTestMask() {
        try {
            String[] maskGrp = {"mask_1531380301000"};
            for (int i = 0; i < maskGrp.length; i++) {
                File mydir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.TEST_DIR);
                File target = new File(mydir, maskGrp[i] + ".jpg");
                Bitmap bmp = BitmapFactory.decodeFile(target.getPath());

                activity.greyImg = new Mat();
                Utils.bitmapToMat(bmp, activity.greyImg);
                bmp.recycle();

                Imgproc.cvtColor(activity.greyImg, activity.greyImg, Imgproc.COLOR_RGB2GRAY);

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                String fileName = file.getPath() + File.separator + maskGrp[i];

                Size sz = new Size(activity.greyImg.width() / GRABCAT_DOWNSAMPLE_RATE, activity.greyImg.height() / GRABCAT_DOWNSAMPLE_RATE);
                //Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_CUBIC);
                Imgproc.resize(activity.greyImg.clone(), activity.greyImg, sz, 0, 0, Imgproc.INTER_LANCZOS4);
                Imgproc.erode(activity.greyImg, activity.greyImg, new Mat());

                convertToGrabCutClasses(activity.greyImg);

                activity.drawView.redoGrabCut(activity.greyImg, activity, fileName);
                Log.v(TAG, "執行完畢");

                Log.v(TAG, "取得bmp尺寸為: 寬-" + bmp.getWidth() + "/長-" + bmp.getHeight());
            }
        } catch (Exception e) {
            Log.v(TAG, "取得AI圖片錯誤: " + e.toString());

            e.printStackTrace();
        }
    }

    public void postAIRequest(String postURL, String targetFileName, String imgURL) {
        try {
            Bitmap newBmp = null;
            activity.greyHeatImgUrl = "";

            if (client == null) {
//                OkHttpClient.Builder builder = new OkHttpClient.Builder();
//                builder.connectTimeout(10, TimeUnit.SECONDS);
//                builder.readTimeout(120, TimeUnit.SECONDS);
//                //builder.addInterceptor(logging);
//                client = builder.build();
                client = (new XSslOkHttpClient(10, 160)).getOkHttpClient();
            }
            activity.showToast("Wait for call AI Server");

            if (AppResultReceiver.dataEncrypt) {
                try {
                    FileHelper.decodeFileSecret(targetFileName);
                    Thread.sleep(100);
                } catch (Exception ex) {
                }
            }

            File targetFile = new File(targetFileName);
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("image", targetFileName, RequestBody.create(MediaType.parse("image/jpeg"), targetFile))
                    .build();
            Request request = new Request.Builder()
                    .url(postURL)
                    .post(body)
                    .build();
            Log.v(TAG, "執行拋出AI需求");
            Response response = client.newCall(request).execute();
            Log.v(TAG, "執行得到heatmap");
            Log.v(TAG, "回覆訊息為：" + response.toString());

            if (AppResultReceiver.dataEncrypt) {
                try {
                    //FileHelper.overwriteFileSecret(targetFileName);
                } catch (Exception ex) {
                }
            }

            JSONObject result = new JSONObject(response.body().string());
            String heatmapURL = imgURL + "/" + result.getString("heatmap_url");
            activity.greyHeatImgUrl = heatmapURL;
//            URL url = new URL(heatmapURL.replace(" ", "%20"));
            InputStream is = (InputStream) (new XSslHttpURLConnection()).getImageStream(heatmapURL.replace(" ", "%20"), 5, 10);
            Log.v(TAG, "執行decodeStream前");
            Bitmap bmp = BitmapFactory.decodeStream(is);
            Log.v(TAG, "執行decodeStream後");

            if (activity.greyImg == null) {
                activity.greyImg = new Mat();
            }
            Utils.bitmapToMat(bmp, activity.greyImg);

            Log.v(TAG, "取得bmp尺寸為: 寬-" + bmp.getWidth() + "/長-" + bmp.getHeight());
            bmp.recycle();

            Imgproc.cvtColor(activity.greyImg, activity.greyImg, Imgproc.COLOR_RGB2GRAY);


            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
            String fileName = file.getPath() + File.separator + s.format(new Date());
            //File mediaFile = new File(fileName + "_ai.png");
            if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_AI_RESPONSE)
                Imgcodecs.imwrite(fileName + "_ai_response.png", activity.greyImg);


            Size sz = new Size(activity.greyImg.width() / GRABCAT_DOWNSAMPLE_RATE, activity.greyImg.height() / GRABCAT_DOWNSAMPLE_RATE);
            //Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_CUBIC);
            Imgproc.resize(activity.greyImg.clone(), activity.greyImg, sz, 0, 0, Imgproc.INTER_LANCZOS4);
            //Imgproc.erode(activity.greyImg,activity.greyImg, new Mat());
            //Imgproc.erode(activity.greyImg,activity.greyImg, new Mat());

            Log.v(TAG, "bmp resize to " + sz.toString());


//
//
//            final Size kernelSize = new Size(5, 5);
//            final Point anchor = new Point(-1, -1);
//            final int iterations = 2;
//
//            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize);
//            Imgproc.erode(activity.greyImg, activity.greyImg, kernel, anchor, iterations);
//
//            Mat fgdImage = new Mat();
//            Mat prfgdImage = new Mat();
//            Imgproc.threshold(activity.greyImg, fgdImage, 250, Imgproc.GC_FGD, Imgproc.THRESH_TOZERO_INV);
//            Imgproc.threshold(activity.greyImg, prfgdImage, 128, Imgproc.GC_PR_FGD, Imgproc.THRESH_TOZERO_INV);
//
            if (AppResultReceiver.DEBUG_LEVEL > 0) {
                Imgcodecs.imwrite(fileName + "_erode.png", activity.greyImg);
            }


            // kernoli todo
            Log.v(TAG, "a.執行convertToGrabCutClasses前");
            //convertToGrabCutClasses(activity.greyImg);
            Imgproc.threshold(activity.greyImg, activity.greyImg, 1, Imgproc.GC_FGD, Imgproc.THRESH_BINARY);
            //Imgcodecs.imwrite(fileName+ "_thresh.png", activity.greyImg);
            Log.v(TAG, "a.執行convertToGrabCutClasses後");
            Log.v(TAG, "a.執行GrabCut前");
            activity.setRedograbcut(true);
            AppResultReceiver.grabcutWithDnnAI = true;


            Log.v(TAG, "取得mask尺寸為: 寬-" + activity.greyImg.cols() + "/長-" + activity.greyImg.rows());
            activity.drawView.redoGrabCut(activity.greyImg, activity, fileName);
            Log.v(TAG, "完成時間:" + System.nanoTime());
            Log.v(TAG, "a.執行GrabCut完畢");
            /*if (bmp.getWidth() >= bmp.getHeight()) {
                newBmp = getResizedBitmap(bmp, 640, 480);
            } else {
                newBmp = getResizedBitmap(bmp, 480, 640);
            }*/
            //Log.v(TAG, "取得newBmp尺寸為: 寬-" + newBmp.getWidth() + "/長-" + newBmp.getHeight());
            Log.v(TAG, "取得結果為: " + heatmapURL);
            activity.showToast("Response from AI Server");
        } catch (Exception e) {
            Log.v(TAG, "取得AI圖片錯誤: " + e.toString());
            activity.showToast("Error on call AI Server: " + e.getMessage());

            e.printStackTrace();
        }
    }

    private void convertToGrabCutClasses(Mat input) {
        int rows = input.rows();
        int cols = input.cols();

        int val = 0;
        int y = 0;
        int x = 0;
        for (y = 0; y < rows; y++) {
            for (x = 0; x < cols; x++) {
                val = (int) input.get(y, x)[0];
                if (val < 32)
                    input.put(y, x, Imgproc.GC_BGD);
                else if (val > 32)
                    input.put(y, x, Imgproc.GC_FGD);
            }
        }
    }

}
