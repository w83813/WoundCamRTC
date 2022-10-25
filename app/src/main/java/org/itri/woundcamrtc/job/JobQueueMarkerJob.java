package org.itri.woundcamrtc.job;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ImageView;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.birbit.android.jobqueue.TagConstraint;

import org.itri.woundcamrtc.AppResultReceiver;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.itri.woundcamrtc.MainActivity;
import org.webrtc.VideoFrame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.resize;

public class JobQueueMarkerJob extends Job {
    private final String TAG = getClass().getSimpleName();
    private JobManager jobManagerMarker = null;
    private MainActivity activity;
    private VideoFrame videoFrame = null;
    private VideoFrame.I420Buffer i420Buffer;
    private VideoFrame.Buffer videoFrameBuffer;

    public JobQueueMarkerJob(JobManager _jobManagerMarker, String tag, MainActivity activity, VideoFrame param, int w, int h) {
        //super(new Params(PRIORITY).requireNetwork().persist().groupBy(tag).singleInstanceBy(tag));
        super(new Params(Thread.NORM_PRIORITY - 1).groupBy("markerJob").singleInstanceBy("markerJob").addTags("markerJob"));

        jobManagerMarker = _jobManagerMarker;
        this.activity = activity;
        videoFrame = param;
        videoFrame.retain();
    }

    @Override
    public void onAdded() {
        //Log.e(TAG, TAG+".onAdded " + videoFrame.hashCode());
    }

    @Override
    public void onRun() throws Throwable {
        byte[] nv21;
        Mat mRGB = null;
        try {
            videoFrameBuffer = videoFrame.getBuffer();
            i420Buffer = videoFrameBuffer.toI420();
            if (i420Buffer != null) {

                ByteBuffer yBuffer = i420Buffer.getDataY();
                ByteBuffer uBuffer = i420Buffer.getDataU();
                ByteBuffer vBuffer = i420Buffer.getDataV();

                int ySize = yBuffer.remaining();
                int uSize = uBuffer.remaining();
                int vSize = vBuffer.remaining();

                nv21 = new byte[ySize + uSize + vSize];

                //U and V are swapped
                yBuffer.get(nv21, 0, ySize);
                vBuffer.get(nv21, ySize, vSize);
                uBuffer.get(nv21, ySize + vSize, uSize);

                mRGB = getYUV2Mat(nv21);


                //String Main_DIR = AppResultReceiver.PROJECT_NAME;
                //File file5 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Main_DIR);
                //Imgcodecs.imwrite(file5.getAbsolutePath() + "/GLsnapshut.jpg", mRGB);

                double scale = 0.5;
                Size adjustedSize = new Size(mRGB.cols() * scale, mRGB.rows() * scale);
                resize(mRGB, mRGB, adjustedSize);

                AppResultReceiver.detectBrightness = checkBrightnessByHist(mRGB);

                if (AppResultReceiver.DEBUG_LEVEL > 0) {
                    AppResultReceiver.lastPicBValue = (int) ((AppResultReceiver.lastPicBValue * 0.8) + (0.2 * (int) mRGB.get((int) (mRGB.rows() * 0.5), (int) (mRGB.cols() * 0.5))[0]));
                    AppResultReceiver.lastPicGValue = (int) ((AppResultReceiver.lastPicBValue * 0.8) + (0.2 * (int) mRGB.get((int) (mRGB.rows() * 0.5), (int) (mRGB.cols() * 0.5))[1]));
                    AppResultReceiver.lastPicRValue = (int) ((AppResultReceiver.lastPicBValue * 0.8) + (0.2 * (int) mRGB.get((int) (mRGB.rows() * 0.5), (int) (mRGB.cols() * 0.5))[2]));
                }

                if (AppResultReceiver.IS_USED_MARKER_DETECTION && AppResultReceiver.isTakingPicture == false) {
                    detectReferencedMarker(mRGB);
                }
            }
            try {
                //Thread.sleep(100);
                jobManagerMarker.cancelJobsInBackground(null, TagConstraint.ANY, "markerJob");
                jobManagerMarker.clear();
                if (activity.semaphore!=null)
                    activity.semaphore.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (mRGB != null)
                    mRGB.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRGB = null;
            nv21 = null;
            try {
                if (i420Buffer != null)
                    i420Buffer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            i420Buffer = null;
            videoFrameBuffer = null;

            try {
                if (videoFrame != null)
                    videoFrame.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            videoFrame = null;
            System.gc();

        }
    }

    public Mat getYUV2Mat(byte[] data) {
        Mat mYuv = new Mat(i420Buffer.getHeight() + i420Buffer.getHeight() / 2, i420Buffer.getWidth(), CV_8UC1);
        mYuv.put(0, 0, data);
        Mat mRGB = new Mat();
        cvtColor(mYuv, mRGB, Imgproc.COLOR_YUV2RGB_I420, 3);
        mYuv.release();
        mYuv = null;
        return mRGB;
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        //如果重試n次仍未成功，那麼就放棄任務，也會進入onCancel
        //if (runCount >= 0)
        return RetryConstraint.CANCEL;
        //return RetryConstraint.RETRY;
    }

    //如果重試超過限定次數，會執行onCancel
    //如果使用者主動放棄此任務，也一樣進入onCancel
    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        //Log.e(TAG, TAG+".onCancel " + videoFrame.hashCode());
        try {
            if (i420Buffer != null)
                i420Buffer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        i420Buffer = null;
        videoFrameBuffer = null;

        try {
            if (videoFrame != null)
                videoFrame.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        videoFrame = null;

        try {
            //Thread.sleep(100);
            jobManagerMarker.cancelJobsInBackground(null, TagConstraint.ANY, "markerJob");
            jobManagerMarker.clear();
            if (activity.semaphore!=null)
                activity.semaphore.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.gc();
    }


    public void detectReferencedMarker(Mat frame) {
        AppResultReceiver.detectMarkerFrameW = frame.cols();
        AppResultReceiver.detectMarkerFrameH = frame.rows();
        //新建目標輸出影像
        Mat hsv_image = new Mat();
        //進行影像彩色空間轉換(讀入影像,輸出影像,顏色轉換)不為空
        //將圖片從RGB空間轉換到HSV空間
        cvtColor(frame, hsv_image, Imgproc.COLOR_BGR2HSV);
        //提取圖片中特定顏色(暗紅色->亮淺藍色)至0或255
        Mat lower_red_hue_range = new Mat();
        //Core.inRange(hsv_image, new Scalar(80, 35, 30), new Scalar(110, 255, 255), lower_red_hue_range);
        //Core.inRange(hsv_image, new Scalar(80, 35, 30), new Scalar(110, 255, 255), lower_red_hue_range);
        //Core.inRange(hsv_image, new Scalar(100, 70, 40), new Scalar(120, 255, 255), lower_red_hue_range);  // 20191024
        //Core.inRange(hsv_image, new Scalar(100, 100, 40), new Scalar(120, 255, 200), lower_red_hue_range); // 20191027
        //Core.inRange(hsv_image, new Scalar(100, 70, 70), new Scalar(120, 255, 255), lower_red_hue_range); // 20191211
        //Core.inRange(hsv_image, new Scalar(100, 90, 70), new Scalar(120, 230, 255), lower_red_hue_range); // 20200525
        Core.inRange(hsv_image, new Scalar(AppResultReceiver.detectMarkerHSVRangeH0, AppResultReceiver.detectMarkerHSVRangeS0 + 10, AppResultReceiver.detectMarkerHSVRangeV0 + 10), new Scalar(AppResultReceiver.detectMarkerHSVRangeH1, AppResultReceiver.detectMarkerHSVRangeS1, AppResultReceiver.detectMarkerHSVRangeV1), lower_red_hue_range);
        //H:194 ~ 202~220 =>97 ~ 101~110


        //儲存影像至對應目錄( 影像檔名為_bin.jpg )
        //File file5 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
        //imwrite(file5.toString().replace("file://","").replace(".jpg","")+"/marker.jpg",frame);
        //activity.setPreviewPic(lower_red_hue_range, false);

//        Mat tmp = new Mat();

        //腐蝕(erode)函式表示是影像高亮區域將減少(亦即會侵蝕高亮的區域)
        //src : Mat 輸入影象 對通道數無要求，但是 depth 必須是 CV_8U、CV_16U、CV_16S、CV_32F、CV_64F 之一
        //dst : Mat 輸出影象，與原圖以上的尺寸與型別
        //kernel : Mat 膨脹操作的核 ， null 時表示以當前畫素為中心 3x3 為單位的核

        //dilate(lower_red_hue_range, tmp, new Mat());
        //dilate(tmp, lower_red_hue_range, new Mat());
        //erode(lower_red_hue_range, tmp, new Mat());        //使用方式 erode(Mat src, Mat dst, Mat kernel)
        //erode(tmp, lower_red_hue_range, new Mat());

        //膨脹(dilate)函示表示是影像高亮區域會增加(亦即會侵蝕非高亮的區域)
        //使用方式 dilate(Mat src, Mat dst, Mat kernel)
        //src : Mat 輸入影象 對通道數無要求，但是 depth 必須是 CV_8U、CV_16U、CV_16S、CV_32F、CV_64F 之一
        //dst : Mat 輸出影象，與原圖以上的尺寸與型別
        //kernel : Mat 膨脹操作的核 ， null 時表示以當前畫素為中心 3x3 為單位的核

        //dilate(lower_red_hue_range, tmp, new Mat());
        //dilate(tmp, lower_red_hue_range, new Mat());

        //erode(lower_red_hue_range, tmp, new Mat());
        //erode(tmp, lower_red_hue_range, new Mat());

        //影像可以分為前景(感興趣的部分)、背景(不感興趣的部分)，閾值(threshold)可當作強度標準值，超過則當前景，反之亦然
        //閾值(臨界點)設為250、最大值設為255、選擇 THRESH_BINARY 型態方式( 超過閾值的像素設為最大值(maxval)，小於閾值的設為0 )
//        Mat threshImage = new Mat();
//        Imgproc.threshold(lower_red_hue_range, threshImage, 1, 255, Imgproc.THRESH_BINARY);


        //輪廓檢測函式(藉此找到面積最大的輪廓並繪製輪廓)
        //尋找輪廓(輸入圖像, 每個輪廓儲存點, 模式(採只取最外層的輪廓 ), 儲存輪廓方法(採對水平、垂直、對角線留下頭尾點))
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(lower_red_hue_range, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//        Log.v(TAG, String.valueOf(contours.size()));
        org.opencv.core.Rect rect;
        double ratio;
        double refMarkerAspectUpper = 1.0 + AppResultReceiver.refMarkerAspectRange - 0.1;
        double refMarkerAspectLower = 1.0 - AppResultReceiver.refMarkerAspectRange + 0.1;
        for (Iterator<MatOfPoint> iterator = contours.iterator(); iterator.hasNext(); ) {
            MatOfPoint op = iterator.next();
            //獲取輪廓的最小外接矩形
            rect = Imgproc.boundingRect(op);

            // 28 pixels @ 25cm on MPD100
            // 11 pixels @ 60cm on MPD100
            if (rect.width < 11 || rect.height < 11) {
                iterator.remove();
            } else {
                ratio = rect.size().width / rect.size().height;
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

//        threshImage.release();
//        threshImage = null;
//        tmp.release();
//        tmp = null;
        lower_red_hue_range.release();
        lower_red_hue_range = null;
        hsv_image.release();
        hsv_image = null;

//        double maxArea = 40000;
//        double minArea = 1000;
        float[] radius = new float[1];
        Point center = new Point();
        int area = 0;
        //double ratio = 0;
        double areaMin = 0;
        double areaMax = 0;
        boolean found = false;
//        AppResultReceiver.detectMarkerW = 0;
//        AppResultReceiver.detectMarkerH = 0;
//        AppResultReceiver.detectMarkerA = 0;
        //找出匹配到的最大輪廓
        for (int i = 0; i < contours.size(); i++) {
            org.opencv.core.Rect rect2 = Imgproc.boundingRect(contours.get(i));
            //Log.d(TAG,"contour "+i+", size="+rect2.width*rect2.height);
            //計算輪廓面積 (給定幾個座標點即可算出)
            // the max cycle area will be 86% of the rectangle area
            MatOfPoint c = contours.get(i);
            area = (int) Imgproc.contourArea(c);
            areaMin = 0.665 * rect2.size().width * rect2.size().height;
            areaMax = 0.865 * rect2.size().width * rect2.size().height;
            if (area > areaMax || area < areaMin)
                continue;

            //Log.d(TAG, i + " accepted size:" + area + ", w:" + rect2.size().width + ", h:" + rect2.size().height);
            AppResultReceiver.detectMarkerX = rect2.x;
            AppResultReceiver.detectMarkerY = rect2.y;
            AppResultReceiver.detectMarkerW = (int) rect2.size().width;
            AppResultReceiver.detectMarkerH = (int) rect2.size().height;
            AppResultReceiver.detectMarkerA = area;

            Mat markerMat = new Mat(frame, rect2);
            double[] edgePoint = markerMat.get(markerMat.rows() / 7, markerMat.cols() / 2);
            double[] markerCenter = markerMat.get(markerMat.rows() / 2, markerMat.cols() / 2);
//            // 若中間顏色和邊緣色不同時, 代表用的是校色marker 必須是絕對灰色
            AppResultReceiver.correctionColorDetected = false;
            //if (Math.abs(edgePoint[0] - markerCenter[0]) + Math.abs(edgePoint[1] - markerCenter[1]) + Math.abs(edgePoint[2] - markerCenter[2]) > 180) {
            //if (Math.abs(edgePoint[1] - markerCenter[1]) + Math.abs(edgePoint[2] - markerCenter[2]) > 128) {
            if (Math.abs(edgePoint[0] - markerCenter[0]) + Math.abs(edgePoint[1] - markerCenter[1]) + Math.abs(edgePoint[2] - markerCenter[2])>20) {
                AppResultReceiver.correctionColorDetected = true;
                //AppResultReceiver.correctionColor[0][0] = (int) (markerCenter[0]);
                //AppResultReceiver.correctionColor[0][1] = (int) (markerCenter[1]);
                //AppResultReceiver.correctionColor[0][2] = (int) (markerCenter[2]);
            } else {
                AppResultReceiver.correctionColorDetected = false;
            }
            markerMat.release();

            //將MatOfPoint轉換成MatOfPoint2f
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            //獲取點集最小外接圓點
            Imgproc.minEnclosingCircle(c2f, center, radius);
            Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int rotation = display.getRotation();
            float x_prop = 0;
            float y_prop = 0;
//                    double originalScale = 2.0;
//                    Log.v(TAG, "centerX= " + center.x);
//                    Log.v(TAG, "centerY= " + center.y);
//                    Log.v(TAG, "cols= " + String.valueOf(frame.cols()));
//                    Log.v(TAG, "rows= " + String.valueOf(frame.rows()));
//                    //Log.v(TAG, "displayWidth= " + String.valueOf(display.getWidth()));
//                    //Log.v(TAG, "displayHeight= " + String.valueOf(display.getHeight()));
//                    Log.v(TAG, "frameWidth= " + String.valueOf(frame.width()));
//                    Log.v(TAG, "frameHeight= " + String.valueOf(frame.height()));

//                    switch (rotation) {
//                        case Surface.ROTATION_0: //0
//                            x_prop = (float) (center.x / frame.rows()) * 1.25f;
//                            y_prop = (float) ((frame.height() - center.y) / frame.cols());
//                            activity.setMarkerBorder((int) (y_prop * display.getHeight()) - 120, (int) (x_prop * display.getWidth()) - 30);
//                            break;
//                        case Surface.ROTATION_90: //1
//                            x_prop = (float) (center.x / frame.cols());
//                            y_prop = (float) (center.y / frame.rows()) * 1.25f;
//                            activity.setMarkerBorder((int) (x_prop * display.getWidth()), (int) (y_prop * display.getHeight()) - 120);
//                            break;
//                        case Surface.ROTATION_180: //2
//                            x_prop = (float) ((frame.width() - center.x) / frame.rows()) * 1.25f;
//                            y_prop = (float) (center.y / frame.cols());
//                            activity.setMarkerBorder((int) (y_prop * display.getHeight()) - 30, (int) (x_prop * display.getWidth()) - 120);
//                            break;
//                        case Surface.ROTATION_270: //3
//                            x_prop = (float) ((frame.width() - center.x) / frame.cols());
//                            y_prop = (float) ((frame.height() - center.y) / frame.rows()) * 1.25f;
//                            activity.setMarkerBorder((int) (x_prop * display.getWidth()), (int) (y_prop * display.getHeight()) - 120);
//                            break;
//                    }

            if ((Build.MODEL.endsWith("MPD100") || Build.MODEL.endsWith("MPD500"))) {
                //rotation = Surface.ROTATION_180;
                x_prop = (float) (center.y) / (frame.rows());
                y_prop = (float) (frame.cols() - center.x - 6) / (frame.cols());
            } else {
                switch (rotation) {
                    case Surface.ROTATION_0: //0
                        x_prop = (float) (frame.rows() - center.y - 48) / (frame.rows() - 96);
                        y_prop = (float) (center.x) / (frame.cols() + 30);
                        break;
                    case Surface.ROTATION_90: //1
                        x_prop = (float) (center.x / frame.cols());
                        y_prop = (float) ((center.y - 64) / (frame.rows() - 110));
                        break;
                    case Surface.ROTATION_180: //2
                        x_prop = (float) (center.y - 48) / (frame.rows() - 96);
                        y_prop = (float) (frame.cols() - center.x) / (frame.cols() + 30);
                        break;
                    case Surface.ROTATION_270: //3
                        x_prop = (float) (frame.cols() - center.x) / frame.cols();
                        y_prop = (float) (frame.rows() - center.y - 64) / (frame.rows() - 110);
                        break;
                }
            }

            activity.setMarkerBorder((int) (x_prop * display.getWidth()), (int) (y_prop * display.getHeight()));
            found = true;
            AppResultReceiver.detectedMarkerUptimeMillis = SystemClock.uptimeMillis();
            if (AppResultReceiver.detectedMarkerStep < 2) {
                AppResultReceiver.detectedMarkerStep++;
            }
            break;
        }

        if (!found) {
            if (AppResultReceiver.detectedMarkerStep > 0) {
                AppResultReceiver.detectedMarkerStep--;
            } else {
                AppResultReceiver.detectMarkerW = 0;
                AppResultReceiver.detectMarkerH = 0;
            }
            activity.setMarkerBorder();
        }

        contours.clear();
    }

    private int covMat2bm(Mat mat, Bitmap bm) {
        //將圖片從BGR空間轉換到RGB空間
        cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        //轉換Mat圖片回Bitmap圖片
        Utils.matToBitmap(mat, bm);
        return 1;
    }

    private Mat ba2Mat(byte[] ba) {
        //opencv3.4
//        Mat mat = Imgcodecs.imdecode(new MatOfByte(ba), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        //opencv4.1
        Mat mat = Imgcodecs.imdecode(new MatOfByte(ba), Imgcodecs.IMREAD_UNCHANGED);
        return mat;
    }

    public int checkBrightnessByHist(Mat frame) {
        try {
            Mat srcImage = new Mat();
            Size adjustedSize = new Size(40, 30);
            resize(frame, srcImage, adjustedSize);

            //File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
            //imwrite(file.toString().replace("file://","").replace(".jpg","")+"/checkBrightnessByHist.jpg",srcImage);

            Mat grayImage = new Mat();
            Imgproc.cvtColor(srcImage, grayImage, Imgproc.COLOR_RGB2GRAY);

            ArrayList<Mat> histsSource = new ArrayList<Mat>();
            histsSource.add(grayImage);

            Mat hist = new Mat();
            Imgproc.calcHist(histsSource, new MatOfInt(0), new Mat(), hist, new MatOfInt(256), new MatOfFloat(4f, 252f));

            Core.MinMaxLocResult mmr = Core.minMaxLoc(hist);
//            MatOfDouble mMean = new MatOfDouble();
//            MatOfDouble mStdDev = new MatOfDouble();
//            Core.meanStdDev(hist, mMean, mStdDev);
//            Log.d(TAG, "max idx:" + mmr.maxLoc.y + ", mean:" + mMean.get(0, 0)[0] + ",stddev:" + mStdDev.get(0, 0)[0]);
//            Log.d(TAG, "max idx:" + mmr.maxLoc.y);
            if (mmr.maxLoc.y < 80)
                return -1;
            else if (mmr.maxLoc.y > 220)
                return 1;
            else
                return 0;
        } catch (Exception ex) {

        }
        return 0;
    }
}
