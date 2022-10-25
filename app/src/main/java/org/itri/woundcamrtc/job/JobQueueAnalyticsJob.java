package org.itri.woundcamrtc.job;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.birbit.android.jobqueue.TagConstraint;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.GrabcutActivity;
import org.itri.woundcamrtc.R;
import org.itri.woundcamrtc.analytics.ColorHelper;
import org.itri.woundcamrtc.analytics.TissueClassification;
import org.itri.woundcamrtc.helper.FileHelper;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.itri.woundcamrtc.AppResultReceiver.mMainActivity;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.resize;

public class JobQueueAnalyticsJob extends Job {
    private final String TAG = getClass().getSimpleName();
    private JobManager jobQueueManager = null;
    private GrabcutActivity activity;
    private String fileName = "";
    private String imgParams = "";
    private String orientation = "";
    private List<MatOfPoint> lastContours = new ArrayList<>();

    public JobQueueAnalyticsJob(JobManager _jobQueueManager, String tag, GrabcutActivity activity, String param, List<MatOfPoint> param2, String imgParams) {
        //super(new Params(PRIORITY).requireNetwork().persist().groupBy(tag).singleInstanceBy(tag));
        super(new Params(Thread.NORM_PRIORITY).groupBy("analyticsJob").singleInstanceBy("analyticsJob").addTags("analyticsJob"));
        jobQueueManager = _jobQueueManager;
        this.activity = activity;
        fileName = param;
        lastContours = param2;
        this.imgParams = imgParams;

        if (jobQueueManager == null) {
            activity.jobManagerLocal = activity.configureJobQueueManager();
            jobQueueManager = activity.jobManagerLocal;
        }
    }

    @Override
    public void onAdded() {
//        Log.e(TAG, TAG+".onAdded " );

    }

    @Override
    public void onRun() throws Throwable {
        newFunc();
    }


    private void newFunc() {

        try {
            try {
                if (!imgParams.equals("")) {
                    JSONObject json = null;
                    json = new JSONObject(imgParams);

                    AppResultReceiver.estimateWidth = 0.0; //預估寬度
                    AppResultReceiver.estimateHeight = 0.0; //預估長度
                    AppResultReceiver.estimateArea = 0.0; //預估面積
                    AppResultReceiver.estimateDepth = 0.0;
                    AppResultReceiver.epithelium_prop = 0; //上皮組織比例
                    AppResultReceiver.granular_prop = 0; //肉芽組織比例
                    AppResultReceiver.slough_prop = 0; //腐皮組織比例
                    AppResultReceiver.eschar_prop = 0; //焦痂組織比例

                    AppResultReceiver.correctionColorDetected = false;
                    AppResultReceiver.correctionColor[0][0] = 0;
                    AppResultReceiver.correctionColor[0][1] = 0;
                    AppResultReceiver.correctionColor[0][2] = 0;
                    if (json.getString("calibrationColor") != null && json.getString("calibrationColor").equals("1")) {
                        AppResultReceiver.correctionColorDetected = true;
                        try {
                            AppResultReceiver.correctionColor[0][0] = Integer.parseInt(json.getString("bValue"));
                            AppResultReceiver.correctionColor[0][1] = Integer.parseInt(json.getString("gValue"));
                            AppResultReceiver.correctionColor[0][2] = Integer.parseInt(json.getString("rValue"));
                        } catch (Exception ex) {
                        }
                    }

                    AppResultReceiver.blue_width_pixel = Double.parseDouble(json.getString("widthPixel"));
                    AppResultReceiver.blue_height_pixel = Double.parseDouble(json.getString("heightPixel"));
                    AppResultReceiver.blue_area = Double.parseDouble(json.getString("blueArea"));
                }
            } catch (Exception ex) {
                //ex.printStackTrace();
                AppResultReceiver.correctionColorDetected = false;
                AppResultReceiver.correctionColor[0][0] = 0;
                AppResultReceiver.correctionColor[0][1] = 0;
                AppResultReceiver.correctionColor[0][2] = 0;
                AppResultReceiver.blue_width_pixel = 0;
                AppResultReceiver.blue_height_pixel = 0;
                AppResultReceiver.blue_area = 0;
                activity.showToast("no blue marker!");
            }

            double blue_pixel_per_cm = 0;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mMainActivity);
            orientation = prefs.getString("screen_orientation", "Null");


            if (lastContours.size() != 0) {
                Mat fileImage = null;
                if (!AppResultReceiver.dataEncrypt) {
                    fileImage = Imgcodecs.imread(fileName);
                } else {
                    fileImage = FileHelper.imreadSecret(fileName);
                }
                Mat saving_mask = new Mat(fileImage.rows(), fileImage.cols(), CvType.CV_8U, new Scalar(0));
                Imgproc.drawContours(saving_mask, lastContours, lastContours.size() - 1, new Scalar(255), -1);
                if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_ORIGN_MASK) {
                    SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                    String fileName = file.getPath() + File.separator + s.format(new Date());
                    Imgcodecs.imwrite(fileName + "_originMask1.png", saving_mask);
                }
                Size sz;
                Size originSize;
                if (AppResultReceiver.blue_width_pixel < AppResultReceiver.blue_height_pixel) {
                    sz = new Size(fileImage.width() * (AppResultReceiver.blue_height_pixel / AppResultReceiver.blue_width_pixel) / GrabcutActivity.TISSUE_DOWNSAMPLE_RATE, fileImage.height() / GrabcutActivity.TISSUE_DOWNSAMPLE_RATE);
                    originSize = new Size(fileImage.width() * (AppResultReceiver.blue_height_pixel / AppResultReceiver.blue_width_pixel), fileImage.height());

                    blue_pixel_per_cm = AppResultReceiver.blue_height_pixel / AppResultReceiver.refMarkerWidth;
                } else if (AppResultReceiver.blue_width_pixel > AppResultReceiver.blue_height_pixel) {
                    sz = new Size(fileImage.width() / GrabcutActivity.TISSUE_DOWNSAMPLE_RATE, fileImage.height() * (AppResultReceiver.blue_width_pixel / AppResultReceiver.blue_height_pixel) / GrabcutActivity.TISSUE_DOWNSAMPLE_RATE);
                    originSize = new Size(fileImage.width(), fileImage.height() * (AppResultReceiver.blue_width_pixel / AppResultReceiver.blue_height_pixel));
                    blue_pixel_per_cm = AppResultReceiver.blue_width_pixel / AppResultReceiver.refMarkerWidth;
                } else {
                    sz = new Size(fileImage.width() / GrabcutActivity.TISSUE_DOWNSAMPLE_RATE, fileImage.height() / GrabcutActivity.TISSUE_DOWNSAMPLE_RATE);
                    originSize = new Size(fileImage.width(), fileImage.height());
                    blue_pixel_per_cm = AppResultReceiver.blue_height_pixel / AppResultReceiver.refMarkerWidth;
                }


                try {
//                    Thread.sleep(300);


//                if (AppResultReceiver.blue_width_pixel<AppResultReceiver.blue_height_pixel)
//                    originSize = new Size(fileImage.width() , fileImage.height()*(AppResultReceiver.blue_width_pixel/AppResultReceiver.blue_height_pixel) );
//                else if (AppResultReceiver.blue_width_pixel>AppResultReceiver.blue_height_pixel)
//                    originSize = new Size(fileImage.width()*(AppResultReceiver.blue_height_pixel/AppResultReceiver.blue_width_pixel) , fileImage.height() );
//                else
//                    originSize = new Size(fileImage.width() , fileImage.height() );

                    Mat originMask = new Mat();
                    Imgproc.resize(saving_mask, originMask, originSize);
                    Imgproc.threshold(originMask, originMask, 1, 255, Imgproc.THRESH_BINARY);
                    if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_ORIGN_MASK) {
                        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                        String fileName = file.getPath() + File.separator + s.format(new Date());
                        Imgcodecs.imwrite(fileName + "_originMask2.png", originMask);
                    }
                    if (AppResultReceiver.blue_area != 0.0) {
                        //Log.v(TAG, "計算:countNonZero = " + Core.countNonZero(originMask) + " blueArea=" + AppResultReceiver.blue_area);
                        //Log.v(TAG, "實際計算:blueArea = " + 3.1415 * (Math.pow((AppResultReceiver.refMarkerWidth / 2), 2)));
                        double realBlueArea = 3.1415 * (Math.pow((AppResultReceiver.refMarkerWidth / 2.0), 2.0));
                        //double realBlueArea = 3.1415 * Math.pow(AppResultReceiver.refMarkerWidth, 2.0);
                        AppResultReceiver.estimateArea = pixelToCentiMeter((realBlueArea * Core.countNonZero(originMask)) / AppResultReceiver.blue_area);
                    } else {
                        AppResultReceiver.estimateArea = 0.0;
                    }

                    lastContours = new ArrayList<>();
                    Mat hierarchy = new Mat();
                    Imgproc.findContours(originMask, lastContours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                    Collections.sort(lastContours, new Comparator<MatOfPoint>() {
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

                    MatOfPoint2f newMtx = new MatOfPoint2f(lastContours.get(lastContours.size() - 1).toArray());
                    org.opencv.core.RotatedRect rotatedRect = Imgproc.minAreaRect(newMtx);
                    if (rotatedRect.size.width < rotatedRect.size.height) {
                        AppResultReceiver.estimateWidth = pixelToCentiMeter(rotatedRect.size.width / blue_pixel_per_cm);
                        AppResultReceiver.estimateHeight = pixelToCentiMeter(rotatedRect.size.height / blue_pixel_per_cm);
                    } else {
                        AppResultReceiver.estimateHeight = pixelToCentiMeter(rotatedRect.size.width / blue_pixel_per_cm);
                        AppResultReceiver.estimateWidth = pixelToCentiMeter(rotatedRect.size.height / blue_pixel_per_cm);
                    }
                    AppResultReceiver.estimateDepth = 0.0;
//                    Log.v(TAG, "計算:blue_pixel_percm = " + blue_pixel_per_cm);
//                    Log.v(TAG, "計算:rotatedRect.size.width = " + rotatedRect.size.width + " rotatedRect.size.height=" + rotatedRect.size.height);
//                    Log.v(TAG, "計算:refWidth = " + AppResultReceiver.blue_width_pixel + " refHeight=" + AppResultReceiver.blue_height_pixel);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if ("Landscape".equals(orientation)) {
                                    activity.sizeInfo1v.setText(String.valueOf(AppResultReceiver.estimateHeight));
                                    activity.sizeInfo2v.setText(String.valueOf(AppResultReceiver.estimateWidth));
                                    activity.sizeInfo3v.setText(String.valueOf(AppResultReceiver.estimateArea));
                                    activity.sizeInfo4v.setText(String.valueOf(AppResultReceiver.estimateDepth));

                                } else {
                                    activity.sizeInfo1v.setText(String.valueOf(AppResultReceiver.estimateHeight));
                                    activity.sizeInfo2v.setText(String.valueOf(AppResultReceiver.estimateWidth));
                                    activity.sizeInfo3v.setText(String.valueOf(AppResultReceiver.estimateArea));
                                    activity.sizeInfo4v.setText(String.valueOf(AppResultReceiver.estimateDepth));
                                }

                                //activity.markerInfo.setText(getApplicationContext().getString(R.string.height) + ": " + pixelToCentiMeter(AppResultReceiver.blue_height_pixel) + ", " + getApplicationContext().getString(R.string.width) + ": " + pixelToCentiMeter(AppResultReceiver.blue_width_pixel) );
                                //activity.sizeInfo.setText("長: " + String.valueOf(AppResultReceiver.estimateHeight) + " , 寬: " + String.valueOf(AppResultReceiver.estimateWidth) + " , 面積: " + String.valueOf(AppResultReceiver.estimateArea));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                } catch (Exception e) {
                    mMainActivity.showToast(e.getLocalizedMessage());

                    e.printStackTrace();
                }

                Mat imageMask = new Mat();
                Imgproc.resize(saving_mask, imageMask, sz);

                Mat imageFile = new Mat();
                Imgproc.resize(fileImage, imageFile, sz);
                Imgproc.threshold(imageMask, imageMask, 100, 255, Imgproc.THRESH_BINARY);

                Mat correctedImage = new Mat();
                if (AppResultReceiver.correctionColorDetected) {
                    //correctedImage = imageFile.clone();
                    correctedImage = ColorHelper.whiteBalanceWithReferencedPoint(imageFile, AppResultReceiver.correctionColor[0][0], AppResultReceiver.correctionColor[0][1], AppResultReceiver.correctionColor[0][2], AppResultReceiver.correctionDefaultGray);
//                    correctedImage = ColorHelper.correctGamma(correctedImage, ColorHelper.auto_gamma_value(correctedImage.clone()));
                    if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_COLOR_CORRECT) {
                        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                        String fileName = file.getPath() + File.separator + s.format(new Date());

                        Imgcodecs.imwrite(fileName + "_orig.png", imageFile);
                        Imgcodecs.imwrite(fileName + "_corrected.png", correctedImage);
                    }
//                    if (AppResultReceiver.correctionColorAlpha != 0.0 ) {
//                        if (true) {
//                            Mat lab = new Mat();
//                            Imgproc.cvtColor(correctedImage, lab, Imgproc.COLOR_BGR2HLS);
//                            List<Mat> listSplited = new ArrayList<Mat>(3);
//                            Core.split(lab, listSplited);
//                            double gamma = (new org.jni.NativeUtils()).get_Gamma_Value(listSplited.get(1));
//                            Mat temp = (new org.jni.NativeUtils()).correctGamma(listSplited.get(1), gamma);
//                            listSplited.set(1,temp);
//                            Core.merge(listSplited, correctedImage);
//                            Imgproc.cvtColor(correctedImage, correctedImage, Imgproc.COLOR_HLS2BGR);
//                        } else {
//                            //(new org.jni.NativeUtils()).autoBrightnessAndContrastWithParams(correctedImage, AppResultReceiver.correctionColorAlpha, AppResultReceiver.correctionColorBeta);
//                            Mat lab = new Mat();
//                            Imgproc.cvtColor(correctedImage, lab, Imgproc.COLOR_BGR2HLS);
//                            List<Mat> listSplited = new ArrayList<Mat>(3);
//                            Core.split(lab, listSplited);
//                            //Mat byteMat = new MatOfByte();
//                            listSplited.get(1).convertTo(listSplited.get(1), CvType.CV_8U, AppResultReceiver.correctionColorAlpha, AppResultReceiver.correctionColorBeta);
//                            Core.merge(listSplited, correctedImage);
//                            Imgproc.cvtColor(correctedImage, correctedImage, Imgproc.COLOR_HLS2BGR);
//                        }
//                    }
//                    Imgcodecs.imwrite(fileName + "_corrected2.png", correctedImage);
                } else {
                    correctedImage = imageFile;
                }

                AnalysisPic_4class(correctedImage, imageMask, getApplicationContext());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if ("Landscape".equals(orientation)) {
                                //Landscape 螢幕保持橫向
                                activity.propInfo.setText(getApplicationContext().getString(R.string.epithelium) + ": " + String.valueOf(AppResultReceiver.epithelium_prop)
                                        + "\n" + getApplicationContext().getString(R.string.granular) + ": " + String.valueOf(AppResultReceiver.granular_prop)
                                        + "\n" + getApplicationContext().getString(R.string.slough) + ": " + String.valueOf(AppResultReceiver.slough_prop)
                                        + "\n" + getApplicationContext().getString(R.string.eschar) + ": " + String.valueOf(AppResultReceiver.eschar_prop));
                            } else {
                                //Portrait 螢幕保持直向
                                activity.propInfo.setText(getApplicationContext().getString(R.string.epithelium) + ": " + String.valueOf(AppResultReceiver.epithelium_prop)
                                        + "," + getApplicationContext().getString(R.string.granular) + ": " + String.valueOf(AppResultReceiver.granular_prop)
                                        + "," + getApplicationContext().getString(R.string.slough) + ": " + String.valueOf(AppResultReceiver.slough_prop)
                                        + "," + getApplicationContext().getString(R.string.eschar) + ": " + String.valueOf(AppResultReceiver.eschar_prop));
                            }
                            AppResultReceiver.redrawMultiSlider = true;
                            try {
                                activity.multiSlider.getThumb(2).setValue(100);
                                activity.multiSlider.getThumb(1).setValue(100);
                                activity.multiSlider.getThumb(0).setValue(AppResultReceiver.epithelium_prop);

                                activity.multiSlider.getThumb(1).setValue(AppResultReceiver.epithelium_prop);
                                activity.multiSlider.getThumb(2).setValue(AppResultReceiver.epithelium_prop + AppResultReceiver.granular_prop + AppResultReceiver.slough_prop);

                                activity.multiSlider.getThumb(1).setValue(AppResultReceiver.epithelium_prop + AppResultReceiver.granular_prop);
                            } catch (Exception e) {

                                e.printStackTrace();
                            }
                            AppResultReceiver.redrawMultiSlider = false;

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }

            SystemClock.sleep(1);
            //Log.v(TAG, "取消");
            jobQueueManager.cancelJobsInBackground(null, TagConstraint.ANY, "analyticsJob");
            //jobQueueManager.clear();
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "onRun() error: " + e.getMessage());
        } finally {
        }
    }

//    private void oldFunc() {
//        //        Log.e(TAG, TAG+".onRun " );
//        try {
//            AppResultReceiver.estimateWidth = 0.0; //預估寬度
//            AppResultReceiver.estimateHeight = 0.0; //預估長度
//            AppResultReceiver.estimateArea = 0.0; //預估面積
//            AppResultReceiver.epithelium_prop = 0.0; //上皮組織比例
//            AppResultReceiver.granular_prop = 0.0; //肉芽組織比例
//            AppResultReceiver.slough_prop = 0.0; //腐皮組織比例
//            AppResultReceiver.eschar_prop = 0.0; //焦痂組織比例
//
//
//            if (lastContours.size() != 0) {
//
//                Mat fileImage = Imgcodecs.imread(fileName);
//                Mat saving_mask = new Mat(fileImage.rows() / GrabcutActivity.GRABCAT_DOWNSAMPLE_RATE, fileImage.cols() / GrabcutActivity.GRABCAT_DOWNSAMPLE_RATE, CvType.CV_8U, new Scalar(0, 0, 0));
//                Imgproc.drawContours(saving_mask, lastContours, lastContours.size() - 1, new Scalar(255, 255, 255), -1);
//
//
//                Size sz = new Size(fileImage.width() / GrabcutActivity.TISSUE_DOWNSAMPLE_RATE, fileImage.height() / GrabcutActivity.TISSUE_DOWNSAMPLE_RATE);
//                Mat imageMask = new Mat();
//                Imgproc.resize(saving_mask, imageMask, sz);
//
//                Mat imageFile = new Mat();
//                Imgproc.resize(fileImage, imageFile, sz);
//
//
//                AnalysisPic_4class(imageFile, imageMask, getApplicationContext());
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        activity.propInfo.setText(
//                                getApplicationContext().getString(R.string.epithelium) + ": " + String.valueOf(AppResultReceiver.epithelium_prop)
//                                        + ", " + getApplicationContext().getString(R.string.granular) + ": " + String.valueOf(AppResultReceiver.granular_prop)
//                                        + ", " + getApplicationContext().getString(R.string.slough) + ": " + String.valueOf(AppResultReceiver.slough_prop)
//                                        + ", " + getApplicationContext().getString(R.string.eschar) + ": " + String.valueOf(AppResultReceiver.eschar_prop));
//
//
//                    }
//                });
//
//
//                Size originSize = new Size(fileImage.width(), fileImage.height());
//                Mat originMask = new Mat();
//                Imgproc.resize(saving_mask, originMask, originSize);
//                Imgproc.threshold(originMask, originMask, 100, 255, Imgproc.THRESH_BINARY);
//                if (AppResultReceiver.blue_area != 0.0) {
//                    //Log.v(TAG, "計算:countNonZero = " + Core.countNonZero(originMask) + " blueArea=" + AppResultReceiver.blue_area);
//                    //Log.v(TAG, "實際計算:blueArea = " + 3.1415 * (Math.pow((AppResultReceiver.refMarkerWidth / 2), 2)));
//                    double realBlueArea = 3.1415 * (Math.pow((AppResultReceiver.refMarkerWidth / 2.0), 2.0));
//                    AppResultReceiver.estimateArea = pixelToCentiMeter((realBlueArea * Core.countNonZero(originMask)) / AppResultReceiver.blue_area);
//                } else {
//                    AppResultReceiver.estimateArea = 0.0;
//                }
//
//                lastContours = new ArrayList<>();
//                Mat hierarchy = new Mat();
//                Imgproc.findContours(originMask, lastContours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//                Collections.sort(lastContours, new Comparator<MatOfPoint>() {
//                    @Override
//                    public int compare(MatOfPoint o1, MatOfPoint o2) {
//                        if (o1.total() > o2.total()) {
//                            return 1;
//                        } else if (o1.total() < o2.total()) {
//                            return -1;
//                        } else {
//                            return 0;
//                        }
//                    }
//                });
//                //Imgproc.drawContours(img, lastContours, lastContours.size() - 1, new Scalar(255, 100, 100), 1);
//
//                MatOfPoint2f newMtx = new MatOfPoint2f(lastContours.get(lastContours.size() - 1).toArray());
//                org.opencv.core.RotatedRect rotatedRect = Imgproc.minAreaRect(newMtx);
//                if (rotatedRect.size.width < rotatedRect.size.height) {
//
//                    AppResultReceiver.estimateWidth = pixelToCentiMeter((rotatedRect.size.width * AppResultReceiver.refMarkerWidth) / AppResultReceiver.blue_width_pixel);
//                    AppResultReceiver.estimateHeight = pixelToCentiMeter((rotatedRect.size.height * AppResultReceiver.refMarkerWidth) / AppResultReceiver.blue_height_pixel);
//                } else {
//                    AppResultReceiver.estimateHeight = pixelToCentiMeter((rotatedRect.size.width * AppResultReceiver.refMarkerWidth) / AppResultReceiver.blue_width_pixel);
//                    AppResultReceiver.estimateWidth = pixelToCentiMeter((rotatedRect.size.height * AppResultReceiver.refMarkerWidth) / AppResultReceiver.blue_height_pixel);
//
//                }
//                //Log.v(TAG, "計算:rotatedRect.size.width = " + rotatedRect.size.width + " rotatedRect.size.height=" + rotatedRect.size.height);
//                //Log.v(TAG, "計算:refWidth = " + AppResultReceiver.blue_width_pixel + " refHeight=" + AppResultReceiver.blue_height_pixel);
//
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //activity.sizeInfo.setText("長: " + String.valueOf(AppResultReceiver.estimateHeight) + " , 寬: " + String.valueOf(AppResultReceiver.estimateWidth) + " , 面積: " + String.valueOf(AppResultReceiver.estimateArea) + " , 距離: " + AppResultReceiver.snapshutDistance + " , 長參: " + AppResultReceiver.blue_height_pixel + " , 寬參: " + AppResultReceiver.blue_width_pixel);
//                        activity.sizeInfo.setText(getApplicationContext().getString(R.string.height) + ": " + String.valueOf(AppResultReceiver.estimateHeight) + ", " + getApplicationContext().getString(R.string.width) + ": " + String.valueOf(AppResultReceiver.estimateWidth) + ", " + getApplicationContext().getString(R.string.area) + ": " + String.valueOf(AppResultReceiver.estimateArea) + ", " + getApplicationContext().getString(R.string.distance) + ": " + AppResultReceiver.snapshutDistance);
//
//                    }
//                });
//            }
//
//            SystemClock.sleep(1);
//            //Log.v(TAG, "取消");
//            jobQueueManager.cancelJobsInBackground(null, TagConstraint.ANY, "analyticsJob");
//            //jobQueueManager.clear();
//        } catch (Exception e) {
//            Log.w(TAG, e.getMessage());
//        } finally {
//        }
//    }


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
//        Log.e(TAG, TAG+".onCancel " );

    }

    private org.opencv.core.Rect BoundingRectangle(MatOfPoint WoundLoc) {
        List<Point> ContourUnits = WoundLoc.toList();

        Collections.sort(ContourUnits, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                return Double.compare(p1.x, p2.x);
            }
        });
        int x = (int) ContourUnits.get(0).x;
        int width = (int) (ContourUnits.get(ContourUnits.size() - 1).x - x);

        Collections.sort(ContourUnits, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                return Double.compare(p1.y, p2.y);
            }
        });
        int y = (int) ContourUnits.get(0).y;
        int height = (int) (ContourUnits.get(ContourUnits.size() - 1).y - y);

        org.opencv.core.Rect Region = new org.opencv.core.Rect();
        Region.x = x;
        Region.y = y;
        Region.width = width;
        Region.height = height;
        return Region;
    }

    private void Analysis_Scale() {
//        sizeInfo = (TextView) findViewById(R.id.size_info);
//        propInfo = (TextView) findViewById(R.id.prop_info);

//        Log.v("GrabcutActivity", distance);
//        sizeInfo.setText("長: " + String.valueOf(AppResultReceiver.estimateHeight) + " , 寬: " + String.valueOf(AppResultReceiver.estimateWidth) + " , 面積: " + String.valueOf(AppResultReceiver.estimateArea) + " , 距離: " + distance);
//        propInfo.setText("上: " + String.valueOf(AppResultReceiver.epithelium_prop) + "% , 芽: " + String.valueOf(AppResultReceiver.granular_prop) + "% , 腐: " + String.valueOf(AppResultReceiver.slough_prop) + "% , 焦: " + String.valueOf(AppResultReceiver.eschar_prop) + "%");

    }

    public void AnalysisPic_4class(Mat inputImage, Mat maskImage, Context context) {
        try {
            int mlType = 0;
            boolean rebuild = false;
            File file = null;
            switch (mlType) {
                case 0:
                    file = new File(AppResultReceiver.ROOT_FOLDER_PATH + "/" + TissueClassification.modelFileName + "_dtrees.xml");
                    if (rebuild || !file.exists() || !file.canRead()) {
                        TissueClassification.classifier_DTrees_4class_build(context);
                    }
                    TissueClassification.classifier_DTrees_4class_fromxml(inputImage, maskImage, context);
                    break;
                case 1:
                    file = new File(AppResultReceiver.ROOT_FOLDER_PATH + "/" + TissueClassification.modelFileName + "_svm.xml");
                    if (rebuild || !file.exists() || !file.canRead()) {
                        TissueClassification.classifier_svm_4class_build(context);
                    }
                    TissueClassification.classifier_svm_4class_fromxml(inputImage, maskImage, context);
                    break;
                case 2:
                    file = new File(AppResultReceiver.ROOT_FOLDER_PATH + "/" + TissueClassification.modelFileName + "_rtrees.xml");
                    if (rebuild || !file.exists() || !file.canRead()) {
                        TissueClassification.classifier_RTrees_4class_build(context);
                    }
                    TissueClassification.classifier_RTrees_4class_fromxml(inputImage, maskImage, context);
                    break;
                case 3:
                    file = new File(AppResultReceiver.ROOT_FOLDER_PATH + "/" + TissueClassification.modelFileName + "_knn.xml");
                    if (rebuild || !file.exists() || !file.canRead()) {
                        TissueClassification.classifier_Knn_4class_build(context);
                    }
                    TissueClassification.classifier_Knn_4class_fromxml(inputImage, maskImage, context);
                    break;
            }
//        Epithelial 嫩皮
//        Granulation 芽
//        Sloughy 腐
//        Necrosis 焦
//        Exudate 滲
//        Maceration 浸
//        Swelling 腫
            int i = 0;
//            outMsg = outMsg + " " + TissueClassification.getTissueAnalysisResult().get(i).get(0) + TissueClassification.getTissueAnalysisResult().get(i).get(1);
            AppResultReceiver.epithelium_prop = Integer.valueOf(TissueClassification.getTissueAnalysisResult().get(i).get(1).replace("%", ""));

            i = 1;
//            outMsg = outMsg + " " + TissueClassification.getTissueAnalysisResult().get(i).get(0) + TissueClassification.getTissueAnalysisResult().get(i).get(1);
            AppResultReceiver.granular_prop = Integer.valueOf(TissueClassification.getTissueAnalysisResult().get(i).get(1).replace("%", ""));

            i = 2;
//            outMsg = outMsg + " " + TissueClassification.getTissueAnalysisResult().get(i).get(0) + TissueClassification.getTissueAnalysisResult().get(i).get(1);
            AppResultReceiver.slough_prop = Integer.valueOf(TissueClassification.getTissueAnalysisResult().get(i).get(1).replace("%", ""));

            i = 3;
//            outMsg = outMsg + " " + TissueClassification.getTissueAnalysisResult().get(i).get(0) + TissueClassification.getTissueAnalysisResult().get(i).get(1);
            AppResultReceiver.eschar_prop = Integer.valueOf(TissueClassification.getTissueAnalysisResult().get(i).get(1).replace("%", ""));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //pixel轉換對應公分(取至小數點後第一位)
    public double pixelToCentiMeter(double result) {
        try {
            DecimalFormat df = new DecimalFormat("#.#");
            double finalResult = Double.valueOf(df.format(result));
            return finalResult;
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        return 0;
    }
}
