package org.itri.woundcamrtc.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;

import com.drew.tools.FileUtil;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.analytics.HomographyHelper;
import org.jni.NativeUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;
import static org.opencv.core.CvType.CV_32FC1;

public class Model3DHelper {

    private static String TAG = "Model3DHelper";

    public final static int mMatrixWarpType = HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE;
    public static String mEvlId = "";
    public static String mEvlIdItemId = "";
    public static Mat mRgb2depthMatrix = null;
    public static Mat mMatDepth = null;
    public static Mat mRgb2thmMatrix = null;
    public static Mat mThm2depthMatrix = null;
    public static Mat mMatThermal = null;

    public static int mProximityDist = 30;
    public static int minHist = 20 * 160;
    public static int maxHist = 70 * 160;  //工作距離外的數值不列入Histagram統計
    public static MatOfInt histSize = new MatOfInt(maxHist - minHist);
    public static MatOfFloat ranges = new MatOfFloat(minHist, maxHist);


    public static String GenWoundInfo(String filePath, String evlId, int itemId, Mat maskY8, int originalx11, int originaly11, int originalx12, int originaly12, int originalx21, int originaly21, int originalx22, int originaly22, int originalcx, int originalcy) {
        try {
            if (NativeUtils.EvlId.equals(evlId))
                return "";

            String dateString = evlId.substring(0, 10);
            mEvlIdItemId = evlId + "_" + dateString + "_" + itemId;

            if (filePath.endsWith(File.separator))
                filePath.substring(0, filePath.length() - 2);

            NativeUtils nativeUtils = new NativeUtils();
            boolean foundCalibrationMatrixs = false;
            try {
                if (HomographyHelper.extrinsic3dCalibrationMatrixs == null || HomographyHelper.extrinsic2dAlignementMatrixs4p == null || HomographyHelper.extrinsic2dAlignementMatrixs3p == null)
                    HomographyHelper.initCalibrationMatrixs(AppResultReceiver.mMainActivity);

                if (HomographyHelper.extrinsic3dCalibrationMatrixs != null) {
                    for (int i = 0; i < HomographyHelper.extrinsic3dCalibrationMatrixs.length; i++) {
                        if (AppResultReceiver.touchPointDepthCentiMeter >= HomographyHelper.extrinsic3dCalibrationMatrixs[i][0] && AppResultReceiver.touchPointDepthCentiMeter < HomographyHelper.extrinsic3dCalibrationMatrixs[i][1]) {
                            nativeUtils.calibracionParam(nativeUtils, (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][2], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][3], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][4], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][5], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][6],
                                    HomographyHelper.extrinsic3dCalibrationMatrixs[i][7], HomographyHelper.extrinsic3dCalibrationMatrixs[i][8], HomographyHelper.extrinsic3dCalibrationMatrixs[i][9], HomographyHelper.extrinsic3dCalibrationMatrixs[i][10], HomographyHelper.extrinsic3dCalibrationMatrixs[i][11]);
                            foundCalibrationMatrixs = true;
                            //break; // for multiple type
                        }
                    }
                }
            } catch (Exception ex) {
            }
            if (!foundCalibrationMatrixs) {
                nativeUtils.calibracionParam(nativeUtils, 1, 1280, 800, 1340, 1453, 8.3, 8.3, 0.023, 0.023, -300.091);
            }

            //smooth depth image
            byte[] bufDepth = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
            short[] depth_in = new short[1280 * 800];
            short[] depth_out = new short[1280 * 800];
            java.nio.ByteBuffer.wrap(bufDepth).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(depth_in);
            try {
                Mat matDepth = new Mat(800, 1280, CvType.CV_16UC1);
                matDepth.put(0, 0, depth_in);

                //*&*& 20201026 HL, add, get proximity distance
                Mat matHistOfDepth = new Mat();
                Core.MinMaxLocResult histMinMax;

                List<Mat> listMatDepth = new ArrayList<Mat>();
                listMatDepth.add(matDepth.submat(200, 600, 400, 800));
                Imgproc.calcHist(listMatDepth, new MatOfInt(0), new Mat(), matHistOfDepth, histSize, ranges, false);
                histMinMax = Core.minMaxLoc(matHistOfDepth);
                mProximityDist = ((int) ((histMinMax.maxLoc.y + minHist) / 160) / 5) * 5 + (int) (((histMinMax.maxLoc.y + minHist) / 160 % 5) / 3) * 5;
                if (mProximityDist < 30)
                    mProximityDist = 30;
                Log.d(TAG, "mProximityDist:" + mProximityDist);
                listMatDepth = null;
                matHistOfDepth.release();

                //&*&* 20201026 HL, add, get proximity distance

//                Mat kernel = new Mat(new Size(11, 11), CvType.CV_8UC1, new Scalar(255));
//                Imgproc.morphologyEx(matDepth, matDepth, Imgproc.MORPH_CLOSE, kernel);

                matDepth.get(0, 0, depth_out);
                if (mMatDepth != null) {
                    mMatDepth.release();
                    mMatDepth = null;
                }
                mMatDepth = matDepth;
                //matDepth.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //*&*& 20201026 HL, move
            Mat Rgb2dAlignementMatrixs = null;
            if (mMatrixWarpType == HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE)
                Rgb2dAlignementMatrixs = HomographyHelper.get2dAlignementMatrixs(5, mProximityDist);
            else
                Rgb2dAlignementMatrixs = HomographyHelper.get2dAlignementMatrixs(9, mProximityDist);
            Point3 newPoint3 = HomographyHelper.rgb2sensingCoord(new Point3(originalx11, originaly11, 0), Rgb2dAlignementMatrixs);
            int finalx11 = (int) newPoint3.x;
            int finaly11 = (int) newPoint3.y;

            newPoint3 = HomographyHelper.rgb2sensingCoord(new Point3(originalx12, originaly12, 0), Rgb2dAlignementMatrixs);
            int finalx12 = (int) newPoint3.x;
            int finaly12 = (int) newPoint3.y;

            newPoint3 = HomographyHelper.rgb2sensingCoord(new Point3(originalx21, originaly21, 0), Rgb2dAlignementMatrixs);
            int finalx21 = (int) newPoint3.x;
            int finaly21 = (int) newPoint3.y;

            newPoint3 = HomographyHelper.rgb2sensingCoord(new Point3(originalx22, originaly22, 0), Rgb2dAlignementMatrixs);
            int finalx22 = (int) newPoint3.x;
            int finaly22 = (int) newPoint3.y;

            if (mRgb2depthMatrix != null) {
                mRgb2depthMatrix.release();
                mRgb2depthMatrix = null;
            }
            mRgb2depthMatrix = Rgb2dAlignementMatrixs;
            // &*&* 20201026 HL, move

            depth_in = null;

            // get color jpg
            //Mat matJPG = Imgcodecs.imread(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg");
            Mat matJPG = HomographyHelper.getAlignmedImg(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg", new Size(1280, 800), Rgb2dAlignementMatrixs, mMatrixWarpType);
            Imgcodecs.imwrite(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg2.jpg", matJPG);
            byte[] arrayJPG = new byte[(int) (matJPG.total() * matJPG.channels())];
            matJPG.get(0, 0, arrayJPG);
            matJPG.release();



            float[] arrayThermal = null;
            byte[] arrayPNG = null;
            try {
                // get thermal raw data with 160x120 sensor size
                byte[] bufThermal = FileUtil.readBytes(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.raw"); // your buffer containing your byte[] data
                arrayThermal = new float[160 * 120];
                java.nio.ByteBuffer.wrap(bufThermal).order(java.nio.ByteOrder.BIG_ENDIAN).asFloatBuffer().get(arrayThermal);

                // get thermal raw data from depth size (160 x 120) with click point of image (120x160)
                Mat matThermal = new Mat(160, 120, CV_32FC1);
                matThermal.put(0, 0, arrayThermal);
                if (mMatThermal != null) {
                    mMatThermal.release();
                    mMatThermal = null;
                }
                mMatThermal = matThermal;

                // get thermal raw data with depth size (1280 x 800) for 3D calculating
                Mat thm2DepthSize = HomographyHelper.getAlignmedImg32FC1(matThermal, new Size(1280, 800), HomographyHelper.get2dAlignementMatrixs(6, mProximityDist), mMatrixWarpType);
                float[] arraythm2DepthSize = new float[1280 * 800];
                thm2DepthSize.get(0, 0, arraythm2DepthSize);
                thm2DepthSize.release();
                arrayThermal = arraythm2DepthSize;

                // get thermal RGB PNG with depth size (1280 x 800) for 3D viewing
                //Mat matPNG = Imgcodecs.imread(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png");
                Mat matPNG = HomographyHelper.getAlignmedImg(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png", new Size(1280, 800), HomographyHelper.get2dAlignementMatrixs(6, mProximityDist), mMatrixWarpType);
                Imgcodecs.imwrite(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_thm2.png", matPNG);
                arrayPNG = new byte[(int) (matPNG.total() * matPNG.channels())];
                matPNG.get(0, 0, arrayPNG);
                matPNG.release();

            } catch (Exception exx) {
                arrayThermal = null;
            }

            // get Mask Y8 with depth size (1280 x 800) for 3D calculating
            maskY8 = HomographyHelper.getAlignmedImg(maskY8, new Size(1280, 800), Rgb2dAlignementMatrixs, mMatrixWarpType);
            Mat kernel = new Mat(new Size(3, 3), CvType.CV_8UC1, new Scalar(255));
            Imgproc.morphologyEx(maskY8, maskY8, Imgproc.MORPH_ERODE, kernel);
            byte[] arrayMaskY8 = new byte[(int) (maskY8.total() * maskY8.channels())];
            maskY8.get(0, 0, arrayMaskY8);
            Imgcodecs.imwrite(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_mak.png", maskY8);


            String result = nativeUtils.nativeGetWoundInfo(nativeUtils, filePath, depth_out, arrayJPG, evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds", evlId + "_" + dateString + "_" + (itemId) + "_jpg2.jpg", arrayThermal, arrayPNG, evlId + "_" + dateString + "_" + (99 + itemId) + "_thm", evlId + "_" + dateString + "_" + (99 + itemId) + "_thm2.png", arrayMaskY8, maskY8.cols(), maskY8.rows(), finalx11, finaly11, finalx12, finaly12, finalx21, finaly21, finalx22, finaly22, false);
            if (dataEncrypt) {
                //FileHelper.overwriteFileSecret(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg2.jpg");
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static boolean get2dAligmentParams(Context context, String filePath, String evlId, String dateString, int itemId, int warpType) throws IOException {
        // check and generate params for get sensing value from click point
        boolean rebuild = false;
        if (!mEvlId.equals(evlId + "_" + dateString + "_" + itemId))
            rebuild = true;
        HomographyHelper.initCalibrationMatrixs(context);
        if (rebuild || Model3DHelper.mMatDepth == null) {
            byte[] bufDepth = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
            short[] depth_in = new short[1280 * 800];
            java.nio.ByteBuffer.wrap(bufDepth).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(depth_in);

            Mat matDepth = new Mat(800, 1280, CvType.CV_16UC1);
            matDepth.put(0, 0, depth_in);

            //*&*& 20201026 HL, add, get proximity distance
            Core.MinMaxLocResult histMinMax;
            Mat matHistOfDepth = new Mat();

            java.util.List<Mat> listMatDepth = new ArrayList<Mat>();
            listMatDepth.add(matDepth.submat(200, 600, 400, 800));
            Imgproc.calcHist(listMatDepth, new MatOfInt(0), new Mat(), matHistOfDepth, histSize, ranges, false);
            histMinMax = Core.minMaxLoc(matHistOfDepth);
            mProximityDist = ((int) ((histMinMax.maxLoc.y + minHist) / 160) / 5) * 5 + (int) (((histMinMax.maxLoc.y + minHist) / 160 % 5) / 3) * 5;
            if (mProximityDist < 30)
                mProximityDist = 30;
            Log.d(TAG, "mProximityDist:" + mProximityDist);
            listMatDepth = null;
            matHistOfDepth.release();

            //&*&* 20201026 HL, add, get proximity distance

            Mat kernel = new Mat(new Size(11, 11), CvType.CV_8UC1, new Scalar(255));
            Imgproc.morphologyEx(matDepth, matDepth, Imgproc.MORPH_CLOSE, kernel);

            if (mMatDepth != null) {
                mMatDepth.release();
                mMatDepth = null;
            }
            mMatDepth = matDepth;
        }
        //if (rebuild || Model3DHelper.mMatThermal == null) {
            byte[] bufThermal = FileUtil.readBytes(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.raw"); // your buffer containing your byte[] data
            float[] arrayThermal = new float[160 * 120];
            java.nio.ByteBuffer.wrap(bufThermal).order(java.nio.ByteOrder.BIG_ENDIAN).asFloatBuffer().get(arrayThermal);

            Mat matThermal = new Mat(160, 120, CV_32FC1);
            matThermal.put(0, 0, arrayThermal);
            if (mMatThermal != null) {
                mMatThermal.release();
                mMatThermal = null;
            }
            mMatThermal = matThermal;
        //}
        if (rebuild || Model3DHelper.mRgb2depthMatrix == null) {
            Mat rgb2depthMatrix = null;
            if (warpType == HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE)
                rgb2depthMatrix = HomographyHelper.get2dAlignementMatrixs(5, mProximityDist);
            else
                rgb2depthMatrix = HomographyHelper.get2dAlignementMatrixs(9, mProximityDist);
            if (mRgb2depthMatrix != null) {
                mRgb2depthMatrix.release();
                mRgb2depthMatrix = null;
            }
            mRgb2depthMatrix = rgb2depthMatrix;
        }
        if (rebuild || Model3DHelper.mRgb2thmMatrix == null) {
            Mat rgb2thmMatrix = null;
            if (warpType == HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE)
                rgb2thmMatrix = HomographyHelper.get2dAlignementMatrixs(7, mProximityDist);
            else
                rgb2thmMatrix = HomographyHelper.get2dAlignementMatrixs(11, mProximityDist);
            if (mRgb2thmMatrix != null) {
                mRgb2thmMatrix.release();
                mRgb2thmMatrix = null;
            }
            mRgb2thmMatrix = rgb2thmMatrix;
        }
        if (rebuild || Model3DHelper.mThm2depthMatrix == null) {
            Mat thm2depthMatrix = null;
            if (warpType == HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE)
                thm2depthMatrix = HomographyHelper.get2dAlignementMatrixs(6, mProximityDist);
            else
                thm2depthMatrix = HomographyHelper.get2dAlignementMatrixs(10, mProximityDist);
            if (mThm2depthMatrix != null) {
                mThm2depthMatrix.release();
                mThm2depthMatrix = null;
            }
            mThm2depthMatrix = thm2depthMatrix;
        }
        mEvlIdItemId = evlId + "_" + dateString + "_" + itemId;
        return true;
    }


    public static String Gen3DColorImage(String filePath, String evlId, int itemId) {

        try {

            if (NativeUtils.EvlId.equals(evlId))
                return "";

            String dateString = evlId.substring(0, 10);
            mEvlIdItemId = evlId + "_" + dateString + "_" + itemId;
            if (filePath.endsWith(File.separator))
                filePath.substring(0, filePath.length() - 2);

            NativeUtils nativeUtils = new NativeUtils();
            boolean foundCalibrationMatrixs = false;
            try {
                if (HomographyHelper.extrinsic3dCalibrationMatrixs == null || HomographyHelper.extrinsic2dAlignementMatrixs4p == null || HomographyHelper.extrinsic2dAlignementMatrixs3p == null)
                    HomographyHelper.initCalibrationMatrixs(AppResultReceiver.mMainActivity);

                if (HomographyHelper.extrinsic3dCalibrationMatrixs != null) {
                    for (int i = 0; i < HomographyHelper.extrinsic3dCalibrationMatrixs.length; i++) {
                        if (AppResultReceiver.touchPointDepthCentiMeter >= HomographyHelper.extrinsic3dCalibrationMatrixs[i][0] && AppResultReceiver.touchPointDepthCentiMeter < HomographyHelper.extrinsic3dCalibrationMatrixs[i][1]) {
                            nativeUtils.calibracionParam(nativeUtils, (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][2], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][3], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][4], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][5], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][6],
                                    HomographyHelper.extrinsic3dCalibrationMatrixs[i][7], HomographyHelper.extrinsic3dCalibrationMatrixs[i][8], HomographyHelper.extrinsic3dCalibrationMatrixs[i][9], HomographyHelper.extrinsic3dCalibrationMatrixs[i][10], HomographyHelper.extrinsic3dCalibrationMatrixs[i][11]);
                            foundCalibrationMatrixs = true;
                            //break; // for multiple type
                        }
                    }
                }
            } catch (Exception ex) {
            }
            if (!foundCalibrationMatrixs) {
                nativeUtils.calibracionParam(nativeUtils, 1, 1280, 800, 1340, 1453, 8.3, 8.3, 0.023, 0.023, -300.091);
            }

            //*&*& 20201026 HL, add, get proximity distance
//            int proximityDist = 30;     // default work distance in cm
            Core.MinMaxLocResult histMinMax;
            //&*&* 20201026 HL, add, get proximity distance

            //smooth depth image
            byte[] bufDepth = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
            short[] depth_in = new short[1280 * 800];
            short[] depth_out = new short[1280 * 800];
            java.nio.ByteBuffer.wrap(bufDepth).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(depth_in);
            try {
                Mat matDepth = new Mat(800, 1280, CvType.CV_16UC1);
                matDepth.put(0, 0, depth_in);

                //*&*& 20201026 HL, add, get proximity distance
                Mat matHistOfDepth = new Mat();
                java.util.List<Mat> listMatDepth = new ArrayList<Mat>();
                listMatDepth.add(matDepth);
                Imgproc.calcHist(listMatDepth, new MatOfInt(0), new Mat(), matHistOfDepth, histSize, ranges, false);
                histMinMax = Core.minMaxLoc(matHistOfDepth);
                mProximityDist = ((int) ((histMinMax.maxLoc.y + minHist) / 160) / 5) * 5;
                Log.d(TAG, "mProximityDist:" + mProximityDist);
                listMatDepth = null;
                matHistOfDepth.release();
                //&*&* 20201026 HL, add, get proximity distance

                Mat kernel = new Mat(new Size(11, 11), CvType.CV_8UC1, new Scalar(255));
                Imgproc.morphologyEx(matDepth, matDepth, Imgproc.MORPH_CLOSE, kernel);

                matDepth.get(0, 0, depth_out);

                if (mMatDepth != null) {
                    mMatDepth.release();
                    mMatDepth = null;
                }
                mMatDepth = matDepth;
//                matDepth.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            depth_in = null;

            Mat Rgb2dAlignementMatrixs = null;
            if (Model3DHelper.mMatrixWarpType == HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE)
                Rgb2dAlignementMatrixs = HomographyHelper.get2dAlignementMatrixs(5, mProximityDist);
            else
                Rgb2dAlignementMatrixs = HomographyHelper.get2dAlignementMatrixs(9, mProximityDist);

            if (mRgb2depthMatrix != null) {
                mRgb2depthMatrix.release();
                mRgb2depthMatrix = null;
            }
            mRgb2depthMatrix = Rgb2dAlignementMatrixs;

            // get color jpg
            //Mat matJPG = Imgcodecs.imread(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg");
            Mat matJPG = HomographyHelper.getAlignmedImg(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg", new Size(1280, 800), Rgb2dAlignementMatrixs, mMatrixWarpType);
            Imgcodecs.imwrite(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg2.jpg", matJPG);
            byte[] arrayJPG = new byte[(int) (matJPG.total() * matJPG.channels())];
            matJPG.get(0, 0, arrayJPG);
            matJPG.release();



            String result = nativeUtils.nativeGen3DColorImage(nativeUtils, filePath, evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds", evlId + "_" + dateString + "_" + (itemId) + "_jpg2.jpg", depth_out, arrayJPG, matJPG.cols(), matJPG.rows(), Rgb2dAlignementMatrixs.getNativeObjAddr());
            if (dataEncrypt) {
                //Thread.sleep(300);
                //FileHelper.overwriteFileSecret(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg2.jpg");
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

//    public static String Gen3DColorImage(String filePath, String evlId, int itemId) {
//        try {
//            String dateString = evlId.substring(0, 10);
//            if (filePath.endsWith(File.separator))
//                filePath.substring(0, filePath.length() - 2);
//
//            //smooth depth image
//            byte[] bufDepth = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
//            short[] depth_in = new short[1280 * 800];
//            short[] depth_out = new short[1280 * 800];
//            java.nio.ByteBuffer.wrap(bufDepth).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(depth_in);
//            try {
//                Mat matDepth = new Mat(800, 1280, CvType.CV_16UC1);
//                matDepth.put(0, 0, depth_in);
//
//                Mat kernel = new Mat(new Size(9, 9), CvType.CV_8UC1, new Scalar(255));
//                Imgproc.morphologyEx(matDepth, matDepth, Imgproc.MORPH_CLOSE, kernel);
//
//                matDepth.get(0, 0, depth_out);
//                matDepth.release();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            depth_in = null;
//
//            // get color jpg
//            Mat matJPG = Imgcodecs.imread(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg2.jpg");
//            byte[] arrayJPG = new byte[(int) (matJPG.total() * matJPG.channels())];
//            matJPG.get(0, 0, arrayJPG);
//
//            Mat temp = new Mat();
//            NativeUtils nativeUtils = new NativeUtils();
//            String result = nativeUtils.nativeGen3DColorImage(nativeUtils, filePath, evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds", evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg", depth_out, arrayJPG, matJPG.cols(), matJPG.rows(), temp.getNativeObjAddr());
//
//            matJPG.release();
//            depth_out = null;
//
//            Log.d("", "result:" + result);
//            return result;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return "";
//    }

    public static String Gen3DThermalImage(String filePath, String evlId, int itemId) {
        try {
            String dateString = evlId.substring(0, 10);
            if (filePath.endsWith(File.separator))
                filePath.substring(0, filePath.length() - 2);

            //smooth depth image
            byte[] bufDepth = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
            short[] depth_in = new short[1280 * 800];
            short[] depth_out = new short[1280 * 800];
            java.nio.ByteBuffer.wrap(bufDepth).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(depth_in);
            try {
                Mat matDepth = new Mat(800, 1280, CvType.CV_16UC1);
                matDepth.put(0, 0, depth_in);

                Mat kernel = new Mat(new Size(9, 9), CvType.CV_8UC1, new Scalar(255));
                Imgproc.morphologyEx(matDepth, matDepth, Imgproc.MORPH_CLOSE, kernel);

                matDepth.get(0, 0, depth_out);
                matDepth.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            depth_in = null;

            // get thermal jpg
            Mat matJPG = null;
            if (!AppResultReceiver.dataEncrypt) {
                matJPG = Imgcodecs.imread(filePath + File.separator + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm2.png");
            } else {
                matJPG = FileHelper.imreadSecret(filePath + File.separator + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm2.png");
            }

            byte[] arrayJPG = new byte[(int) (matJPG.total() * matJPG.channels())];
            matJPG.get(0, 0, arrayJPG);


            byte[] bufThermal = FileUtil.readBytes(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.raw"); // your buffer containing your byte[] data
            float[] arrayThermal = new float[120 * 160];
            java.nio.ByteBuffer.wrap(bufThermal).order(java.nio.ByteOrder.BIG_ENDIAN).asFloatBuffer().get(arrayThermal);

            Mat temp = new Mat();
            NativeUtils nativeUtils = new NativeUtils();
            String result = nativeUtils.nativeGen3DThermalImage(nativeUtils, filePath, evlId + "_" + dateString + "_" + (99 + itemId) + "_thm", evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png", depth_out, arrayThermal, arrayJPG, matJPG.cols(), matJPG.rows(), temp.getNativeObjAddr());

            matJPG.release();
            depth_out = null;
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }


//    public static Point GetNextCandidatedPoint(int xx, int yy, int cx, int cy, Mat depthMat, int limitDepth) {
//        Point ret = new Point(cx, cy);
//        double slope = (yy - cy) / (xx - cx);
//        int maxst = Math.max(Math.abs(yy - cy), Math.abs(xx - cx));
//        int retXX0 = cx;
//        int retYY0 = cy;
//        int retXX1 = cx;
//        int retYY1 = cy;
//        for (int step = 1; step < maxst - 1; step++) {
//            retXX0 = (int) (cx + slope * step - 1);
//            retYY0 = (int) (cy + slope * step - 1);
//            retXX1 = (int) (cx + slope * step);
//            retYY1 = (int) (cy + slope * step);
//            if (Math.abs(depthMat.get(retXX0, retYY0)[0] - depthMat.get(retXX1, retYY1)[0]) > limitDepth) {
//                ret.x = cx + slope * step;
//                ret.y = cy + slope * step;
//            }
//        }
//        return ret;
//    }
//
//    public static String GenWoundInfo2(boolean forceRebuild, String filePath, String evlId, int itemId, Mat maskY8, int xx11, int yy11, int xx12, int yy12, int xx21, int yy21, int xx22, int yy22) {
//        try {
//            String result = "";
//            String dateString = evlId.substring(0, 10);
//            if (filePath.endsWith(File.separator))
//                filePath.substring(0, filePath.length() - 2);
//
//            NativeUtils nativeUtils = new NativeUtils();
//
//            boolean foundCalibrationMatrixs = false;
//            try {
//                if (HomographyHelper.extrinsic3dCalibrationMatrixs == null || HomographyHelper.extrinsic2dAlignementMatrixs == null)
//                    HomographyHelper.initCalibrationMatrixs(AppResultReceiver.mMainActivity);
//
//                if (HomographyHelper.extrinsic3dCalibrationMatrixs != null) {
//                    for (int i = 0; i < HomographyHelper.extrinsic3dCalibrationMatrixs.length; i++) {
//                        if (AppResultReceiver.touchPointDepthCentiMeter >= HomographyHelper.extrinsic3dCalibrationMatrixs[i][0] && AppResultReceiver.touchPointDepthCentiMeter < HomographyHelper.extrinsic3dCalibrationMatrixs[i][1]) {
//                            nativeUtils.calibracionParam(nativeUtils, (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][2], (int)HomographyHelper.extrinsic3dCalibrationMatrixs[i][3], (int)HomographyHelper.extrinsic3dCalibrationMatrixs[i][4], (int)HomographyHelper.extrinsic3dCalibrationMatrixs[i][5], (int)HomographyHelper.extrinsic3dCalibrationMatrixs[i][6],
//                                    HomographyHelper.extrinsic3dCalibrationMatrixs[i][7], HomographyHelper.extrinsic3dCalibrationMatrixs[i][8], HomographyHelper.extrinsic3dCalibrationMatrixs[i][9], HomographyHelper.extrinsic3dCalibrationMatrixs[i][10], HomographyHelper.extrinsic3dCalibrationMatrixs[i][11]);
//                            foundCalibrationMatrixs = true;
//                            break;
//                        }
//                    }
//                }
//            } catch (Exception ex) {
//            }
//            if (!foundCalibrationMatrixs) {
//                nativeUtils.calibracionParam(nativeUtils, 1, 2448, 3264, 1340, 1453, 8.3, 8.3, 0.023, 0.023, -300.0);
//            }
//
//
//            if (!nativeUtils.EvlId.equals(evlId) || forceRebuild) {
//                nativeUtils.deleteObject(nativeUtils);
//                nativeUtils.createObject(nativeUtils);
//
//                //smooth depth image
//                byte[] bufDepth = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
//                short[] depth_in = new short[1280 * 800];
//                short[] depth_out = new short[1280 * 800];
//                java.nio.ByteBuffer.wrap(bufDepth).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(depth_in);
//                try {
//                    Mat matDepth = new Mat(800, 1280, CvType.CV_16UC1);
//                    matDepth.put(0, 0, depth_in);
//
//                    Mat kernel = new Mat(new Size(9, 9), CvType.CV_8UC1, new Scalar(255));
//                    Imgproc.morphologyEx(matDepth, matDepth, Imgproc.MORPH_CLOSE, kernel);
//
//                    matDepth.get(0, 0, depth_out);
//                    matDepth.release();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                result = nativeUtils.genTriMesh2(nativeUtils, depth_out);
//                depth_in = null;
//                depth_out = null;
//
////                // get thermal raw data
////                byte[] bufThermal = FileUtil.readBytes(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.raw"); // your buffer containing your byte[] data
////                float[] arrayThermal = new float[120 * 160];
////                java.nio.ByteBuffer.wrap(bufThermal).order(java.nio.ByteOrder.BIG_ENDIAN).asFloatBuffer().get(arrayThermal);
////                // get thermal PNG
////                Mat matPNG = Imgcodecs.imread(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png");
////                byte[] arrayPNG = new byte[(int) (matPNG.total() * matPNG.channels())];
////                matPNG.get(0, 0, arrayPNG);
////                matPNG.release();
////                result = nativeUtils.gen3DThermalImage2(nativeUtils, filePath, evlId + "_" + dateString + "_" + (99 + itemId) + "_thm", evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png");
//
//                // get color jpg
//                Mat matJPG = Imgcodecs.imread(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg");
//                byte[] arrayJPG = new byte[(int) (matJPG.total() * matJPG.channels())];
//                matJPG.get(0, 0, arrayJPG);
//                result = nativeUtils.gen3DColorImage2(nativeUtils, filePath, evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds", evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg", matJPG.cols(), matJPG.rows());
//                matJPG.release();
//
//                nativeUtils.EvlId = evlId;
//            }
//
//            // get Mask Y8
//            byte[] arrayMaskY8 = new byte[(int) (maskY8.total() * maskY8.channels())];
//            maskY8.get(0, 0, arrayMaskY8);
//
//
//            result = nativeUtils.calWoundAreaInfo2(nativeUtils, filePath, evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg", arrayMaskY8, maskY8.cols(), maskY8.rows(), xx11, yy11, xx12, yy12, xx21, yy21, xx22, yy22);
//            return result;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return "";
//    }
//
//    public static String Gen3DColorImage2(boolean forceRebuild, String filePath, String evlId, int itemId) {
//        try {
//            String result = "";
//            String dateString = evlId.substring(0, 10);
//            if (filePath.endsWith(File.separator))
//                filePath.substring(0, filePath.length() - 2);
//
//            NativeUtils nativeUtils = new NativeUtils();
//            if (!nativeUtils.EvlId.equals(evlId) || forceRebuild) {
//                nativeUtils.deleteObject(nativeUtils);
//                nativeUtils.createObject(nativeUtils);
//
//                //smooth depth image
//                byte[] bufDepth = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
//                short[] depth_in = new short[1280 * 800];
//                short[] depth_out = new short[1280 * 800];
//                java.nio.ByteBuffer.wrap(bufDepth).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(depth_in);
//                try {
//                    Mat matDepth = new Mat(800, 1280, CvType.CV_16UC1);
//                    matDepth.put(0, 0, depth_in);
//
//                    Mat kernel = new Mat(new Size(9, 9), CvType.CV_8UC1, new Scalar(255));
//                    Imgproc.morphologyEx(matDepth, matDepth, Imgproc.MORPH_CLOSE, kernel);
//
//                    matDepth.get(0, 0, depth_out);
//                    matDepth.release();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                result = nativeUtils.genTriMesh2(nativeUtils, depth_out);
//                depth_in = null;
//                depth_out = null;
//
////                // get thermal raw data
////                byte[] bufThermal = FileUtil.readBytes(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.raw"); // your buffer containing your byte[] data
////                float[] arrayThermal = new float[120 * 160];
////                java.nio.ByteBuffer.wrap(bufThermal).order(java.nio.ByteOrder.BIG_ENDIAN).asFloatBuffer().get(arrayThermal);
////                // get thermal PNG
////                Mat matPNG = Imgcodecs.imread(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png");
////                byte[] arrayPNG = new byte[(int) (matPNG.total() * matPNG.channels())];
////                matPNG.get(0, 0, arrayPNG);
////                matPNG.release();
////                result = nativeUtils.gen3DThermalImage2(nativeUtils, filePath, evlId + "_" + dateString + "_" + (99 + itemId) + "_thm", evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png");
//
//                // get color jpg
//                Mat matJPG = Imgcodecs.imread(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg");
//                byte[] arrayJPG = new byte[(int) (matJPG.total() * matJPG.channels())];
//                matJPG.get(0, 0, arrayJPG);
//                result = nativeUtils.gen3DColorImage2(nativeUtils, filePath, evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds", evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg", matJPG.cols(), matJPG.rows());
//                matJPG.release();
//
//                nativeUtils.EvlId = evlId;
//            }
//            return result;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return "";
//    }
//
//    public static String GetTouchPointWoundInfo(String filePath, int x, int y) {
//        try {
//            return "{\"c\":\"32\",\"d\":\"35.3\"}";
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return "{}";
//    }
//
//    public static boolean deleteIfExist(String filePath, String filename, boolean delete) {
//        try {
//            if (!filePath.endsWith("/")) filePath = filePath + File.separator;
//            File file = new File(filePath + filename);
//            if (file.exists()) {
//                if (delete)
//                    file.delete();
//                return true;
//            } else {
//                return false;
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return false;
//    }
//
//    public static boolean existFile(String filePath, String filename) {
//        boolean retVal = false;
//        try {
//            if (!filePath.endsWith("/")) filePath = filePath + File.separator;
//            File file = new File(filePath + filename);
//            if (file.exists()) {
//                retVal = true;
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return retVal;
//    }
//
//    public static boolean deleteFile(String filePath, String filename) {
//        boolean retVal = false;
//        try {
//            if (!filePath.endsWith("/")) filePath = filePath + File.separator;
//            File file = new File(filePath + filename);
//            file.delete();
//            retVal = true;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return retVal;
//    }
//
//
//    public static boolean Gen3DColor2(String filePath, String evlId, int itemId) {
//        try {
//            String dateString = evlId.substring(0, 10);
//            if (filePath.endsWith(File.separator))
//                filePath.substring(0, filePath.length() - 2);
//
//            byte[] bBuffer = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
//            char[] deptha = new char[1280 * 800];
//            java.nio.ByteBuffer.wrap(bBuffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).asCharBuffer().get(deptha);
//
////
////            //smooth mesh points
////            byte[] newbytes = new byte[800 * 1280 * 2];
////            try {
////                Mat matSrc = new Mat(800,1280,CvType.CV_16UC1);
////                matSrc.get(0, 0, bBuffer);
////
////                Mat matResize = new Mat();
////                Imgproc.resize(matSrc, matResize, new org.opencv.core.Size(100, 160));
////
////                Imgproc.medianBlur(matResize, matResize, 11);
////                Imgproc.resize(matResize, matResize, new org.opencv.core.Size(800, 1280));
////
////                matSrc.copyTo(matResize, matSrc);
////                matResize.get(0, 0, newbytes);
////                matResize.release();
////                matSrc.release();
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
//
//            Mat rgb = Imgcodecs.imread(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg");
//            byte[] rgba = new byte[(int) (rgb.total() * rgb.channels())];
//            rgb.get(0, 0, rgba);
//
////            Mat thermal = Imgcodecs.imread(filePath + evlId +"_"+dateString + "_"+(99+itemId)+"_thm.png");
////            float[] thermala = new float[(int) (thermal.total() * thermal.channels())];
////            thermal.get(0, 0, thermala);
////
////            Mat mask = Imgcodecs.imread(filePath + evlId +"_"+dateString + "_"+(299+itemId)+"_mak.png");
////            byte[] maska = new byte[(int) (mask.total() * mask.channels())];
////            mask.get(0, 0, maska);
//            Mat sdst = new Mat();
//            NativeUtils nativeUtils = new NativeUtils();
//            String result = nativeUtils.nativeGet3DColor(nativeUtils, filePath, evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds", evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg", deptha, rgba, rgb.cols(), rgb.rows(), sdst.getNativeObjAddr());
//            Log.d("", "result:" + result);
//            return true;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return false;
//    }

//
//    public static String GenWoundInfo(String filePath, String evlId, int itemId, Mat maskY8) {
//        try {
//            String dateString = evlId.substring(0, 10);
//            if (filePath.endsWith(File.separator))
//                filePath.substring(0, filePath.length() - 2);
//
//            byte[] bBuffer = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
//            char[] deptha = new char[1280 * 800];
//            java.nio.ByteBuffer.wrap(bBuffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).asCharBuffer().get(deptha);
//
//            Mat jpg = Imgcodecs.imread(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg");
//            byte[] jpga = new byte[(int) (jpg.total() * jpg.channels())];
//            jpg.get(0, 0, jpga);
//            jpg.release();
//
//            byte[] tBuffer = FileUtil.readBytes(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.raw"); // your buffer containing your byte[] data
//            float[] thermala = new float[120 * 160];
//            java.nio.ByteBuffer.wrap(tBuffer).order(java.nio.ByteOrder.BIG_ENDIAN).asFloatBuffer().get(thermala);
//
//            Mat png = Imgcodecs.imread(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png");
//            byte[] pnga = new byte[(int) (png.total() * png.channels())];
//            png.get(0, 0, pnga);
//            png.release();
//
////            Mat resizedMaskY8 = new Mat();
////            Size adjustedSize = new Size(rgb.cols() , rgb.rows());
////            resize(maskY8, resizedMaskY8, adjustedSize);
//            byte[] resizedMaskY8a = new byte[(int) (maskY8.total() * maskY8.channels())];
//            maskY8.get(0, 0, resizedMaskY8a);
//
////            adjustedSize=null;
//            Imgcodecs.imwrite(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_mak.png", maskY8);
//
//            Mat sdst = new Mat();
//            NativeUtils nativeUtils = new NativeUtils();
//            String result = nativeUtils.nativeGetWoundInfo(nativeUtils, filePath, deptha, jpga, evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds", evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg", thermala, pnga, evlId + "_" + dateString + "_" + (99 + itemId) + "_thm", evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png", resizedMaskY8a, maskY8.cols(), maskY8.rows(), sdst.getNativeObjAddr());
//            Log.d("", "result:" + result);
//            return result;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return "";
//    }
//
//    public static boolean Gen3DThermal(String filePath, String evlId, int itemId) {
//        try {
//            String dateString = evlId.substring(0, 10);
//            if (filePath.endsWith(File.separator))
//                filePath.substring(0, filePath.length() - 2);
//
//            byte[] bBuffer = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
//            char[] deptha = new char[1280 * 800];
//            java.nio.ByteBuffer.wrap(bBuffer).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().get(deptha);
//
//            //Mat thermal = Imgcodecs.imread(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.raw");
//            byte[] tBuffer = FileUtil.readBytes(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.raw"); // your buffer containing your byte[] data
//            float[] thermala = new float[120 * 160];
//            java.nio.ByteBuffer.wrap(tBuffer).order(java.nio.ByteOrder.BIG_ENDIAN).asFloatBuffer().get(thermala);
////
////            Mat mask = Imgcodecs.imread(filePath + evlId +"_"+dateString + "_"+(299+itemId)+"_mak.png");
////            byte[] maska = new byte[(int) (mask.total() * mask.channels())];
////            mask.get(0, 0, maska);
//            Mat sdst = new Mat();
//            NativeUtils nativeUtils = new NativeUtils();
//            String result = nativeUtils.nativeGet3dThermal(nativeUtils, filePath, evlId + "_" + dateString + "_" + (99 + itemId) + "_thm", evlId + "_" + dateString + "_" + (itemId) + "_thm.png", deptha, thermala, 120, 160, sdst.getNativeObjAddr());
//            Log.d("", "result:" + result);
//            return true;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return false;
//    }
//
//    public static boolean obj2glb(String filePath, String objModelName) {
//        boolean retVal = false;
//        try {
//            //File mOutputDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
//            //obj2glb(mOutputDir.path(),"vase_china_07");
//
//            if (!filePath.endsWith("/")) filePath = filePath + File.separator;
//            if (!existFile(filePath, objModelName + ".glb")) {
//                String inputObjFilePath = filePath + objModelName + ".obj";
//                String inputMtlFileName = objModelName;
//                String outputFilePath = filePath;
//                String outputFileName = objModelName;
//                BufferStrategy bufferStrategy = BufferStrategy.BUFFER_PER_GROUP;
//                IndicesComponentType indicesComponentType = IndicesComponentType.GL_UNSIGNED_BYTE;
//                GltfWriteType gltfWriteType = GltfWriteType.BINARY;
//
////                ConvertObjToGltf convertObjToGltf = new ConvertObjToGltf.Builder().inputObjFilePath(inputObjFilePath)
////                        .inputMtlFileName(inputMtlFileName).outputFilePath(outputFilePath).outputFileName(outputFileName)
////                        .bufferStrategy(bufferStrategy).indicesComponentType(indicesComponentType).gltfWriteType(gltfWriteType).build();
//
//                ConvertObjToGltf convertObjToGltf = new ConvertObjToGltf.Builder().inputObjFilePath(inputObjFilePath)
//                        .inputMtlFileName(inputMtlFileName).outputFilePath(outputFilePath).outputFileName(outputFileName)
//                        .bufferStrategy(bufferStrategy).indicesComponentType(indicesComponentType).build();
//
//                convertObjToGltf.convert();
//                retVal = true;
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return retVal;
//    }


    public static String GetCoordinateDistance(String filePath, String evlId, int itemId, int originalx11, int originaly11, int originalx12, int originaly12) {
        try {
            if (NativeUtils.EvlId.equals(evlId))
                return "";

            String dateString = evlId.substring(0, 10);
            mEvlIdItemId = evlId + "_" + dateString + "_" + itemId;

            if (filePath.endsWith(File.separator))
                filePath.substring(0, filePath.length() - 2);

            NativeUtils nativeUtils = new NativeUtils();
            boolean foundCalibrationMatrixs = false;
            try {
                if (HomographyHelper.extrinsic3dCalibrationMatrixs == null || HomographyHelper.extrinsic2dAlignementMatrixs4p == null || HomographyHelper.extrinsic2dAlignementMatrixs3p == null)
                    HomographyHelper.initCalibrationMatrixs(AppResultReceiver.mMainActivity);

                if (HomographyHelper.extrinsic3dCalibrationMatrixs != null) {
                    for (int i = 0; i < HomographyHelper.extrinsic3dCalibrationMatrixs.length; i++) {
                        if (AppResultReceiver.touchPointDepthCentiMeter >= HomographyHelper.extrinsic3dCalibrationMatrixs[i][0] && AppResultReceiver.touchPointDepthCentiMeter < HomographyHelper.extrinsic3dCalibrationMatrixs[i][1]) {
                            nativeUtils.calibracionParam(nativeUtils, (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][2], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][3], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][4], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][5], (int) HomographyHelper.extrinsic3dCalibrationMatrixs[i][6],
                                    HomographyHelper.extrinsic3dCalibrationMatrixs[i][7], HomographyHelper.extrinsic3dCalibrationMatrixs[i][8], HomographyHelper.extrinsic3dCalibrationMatrixs[i][9], HomographyHelper.extrinsic3dCalibrationMatrixs[i][10], HomographyHelper.extrinsic3dCalibrationMatrixs[i][11]);
                            foundCalibrationMatrixs = true;
                            //break; // for multiple type
                        }
                    }
                }
            } catch (Exception ex) {
            }
            if (!foundCalibrationMatrixs) {
                nativeUtils.calibracionParam(nativeUtils, 1, 1280, 800, 1340, 1453, 8.3, 8.3, 0.023, 0.023, -300.091);
            }

            //smooth depth image
            byte[] bufDepth = FileUtil.readBytes(filePath + File.separator + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw"); // your buffer containing your byte[] data
            short[] depth_in = new short[1280 * 800];
            short[] depth_out = new short[1280 * 800];
            java.nio.ByteBuffer.wrap(bufDepth).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(depth_in);
            try {
                Mat matDepth = new Mat(800, 1280, CvType.CV_16UC1);
                matDepth.put(0, 0, depth_in);

                //*&*& 20201026 HL, add, get proximity distance
                Mat matHistOfDepth = new Mat();
                Core.MinMaxLocResult histMinMax;

                List<Mat> listMatDepth = new ArrayList<Mat>();
                listMatDepth.add(matDepth.submat(200, 600, 400, 800));
                Imgproc.calcHist(listMatDepth, new MatOfInt(0), new Mat(), matHistOfDepth, histSize, ranges, false);
                histMinMax = Core.minMaxLoc(matHistOfDepth);
                mProximityDist = ((int) ((histMinMax.maxLoc.y + minHist) / 160) / 5) * 5 + (int) (((histMinMax.maxLoc.y + minHist) / 160 % 5) / 3) * 5;
                if (mProximityDist < 30)
                    mProximityDist = 30;
                Log.d(TAG, "mProximityDist:" + mProximityDist);
                listMatDepth = null;
                matHistOfDepth.release();

                //&*&* 20201026 HL, add, get proximity distance

//                Mat kernel = new Mat(new Size(11, 11), CvType.CV_8UC1, new Scalar(255));
//                Imgproc.morphologyEx(matDepth, matDepth, Imgproc.MORPH_CLOSE, kernel);

                matDepth.get(0, 0, depth_out);
                if (mMatDepth != null) {
                    mMatDepth.release();
                    mMatDepth = null;
                }
                mMatDepth = matDepth;
                //matDepth.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //*&*& 20201026 HL, move
            Mat Rgb2dAlignementMatrixs = null;
            if (mMatrixWarpType == HomographyHelper.MATRIX_WARP_TYPE_4P_WARPPERSPECTIVE)
                Rgb2dAlignementMatrixs = HomographyHelper.get2dAlignementMatrixs(5, mProximityDist);
            else
                Rgb2dAlignementMatrixs = HomographyHelper.get2dAlignementMatrixs(9, mProximityDist);
            Point3 newPoint3 = HomographyHelper.rgb2sensingCoord(new Point3(originalx11, originaly11, 0), Rgb2dAlignementMatrixs);
            int finalx11 = (int) newPoint3.x;
            int finaly11 = (int) newPoint3.y;

            newPoint3 = HomographyHelper.rgb2sensingCoord(new Point3(originalx12, originaly12, 0), Rgb2dAlignementMatrixs);
            int finalx12 = (int) newPoint3.x;
            int finaly12 = (int) newPoint3.y;

            if (mRgb2depthMatrix != null) {
                mRgb2depthMatrix.release();
                mRgb2depthMatrix = null;
            }
            mRgb2depthMatrix = Rgb2dAlignementMatrixs;
            // &*&* 20201026 HL, move

            depth_in = null;

            // get color jpg
            //Mat matJPG = Imgcodecs.imread(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg");
            Mat matJPG = HomographyHelper.getAlignmedImg(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg.jpg", new Size(1280, 800), Rgb2dAlignementMatrixs, mMatrixWarpType);
            Imgcodecs.imwrite(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg2.jpg", matJPG);
            byte[] arrayJPG = new byte[(int) (matJPG.total() * matJPG.channels())];
            matJPG.get(0, 0, arrayJPG);
            matJPG.release();



            float[] arrayThermal = null;
            byte[] arrayPNG = null;
            try {
                // get thermal raw data with 160x120 sensor size
                byte[] bufThermal = FileUtil.readBytes(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.raw"); // your buffer containing your byte[] data
                arrayThermal = new float[160 * 120];
                java.nio.ByteBuffer.wrap(bufThermal).order(java.nio.ByteOrder.BIG_ENDIAN).asFloatBuffer().get(arrayThermal);

                // get thermal raw data from depth size (160 x 120) with click point of image (120x160)
                Mat matThermal = new Mat(160, 120, CV_32FC1);
                matThermal.put(0, 0, arrayThermal);
                if (mMatThermal != null) {
                    mMatThermal.release();
                    mMatThermal = null;
                }
                mMatThermal = matThermal;

                // get thermal raw data with depth size (1280 x 800) for 3D calculating
                Mat thm2DepthSize = HomographyHelper.getAlignmedImg32FC1(matThermal, new Size(1280, 800), HomographyHelper.get2dAlignementMatrixs(6, mProximityDist), mMatrixWarpType);
                float[] arraythm2DepthSize = new float[1280 * 800];
                thm2DepthSize.get(0, 0, arraythm2DepthSize);
                thm2DepthSize.release();
                arrayThermal = arraythm2DepthSize;

                // get thermal RGB PNG with depth size (1280 x 800) for 3D viewing
                //Mat matPNG = Imgcodecs.imread(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png");
                Mat matPNG = HomographyHelper.getAlignmedImg(filePath + evlId + "_" + dateString + "_" + (99 + itemId) + "_thm.png", new Size(1280, 800), HomographyHelper.get2dAlignementMatrixs(6, mProximityDist), mMatrixWarpType);
                Imgcodecs.imwrite(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_thm2.png", matPNG);
                arrayPNG = new byte[(int) (matPNG.total() * matPNG.channels())];
                matPNG.get(0, 0, arrayPNG);
                matPNG.release();

            } catch (Exception exx) {
                arrayThermal = null;
            }

            // get Mask Y8 with depth size (1280 x 800) for 3D calculating



            byte[] arrayMaskY8 = new byte[1024000];



            int temp_x = (int) (finalx11+finalx12)/2;
            int temp_y = (int) (finaly11+finaly12)/2;
            int temp_x21 = temp_x - 1;
            int temp_x22 = temp_x + 1;
            int temp_y21 = temp_y - 1;
            int temp_y22 = temp_y + 1;

            String result = nativeUtils.nativeGetWoundInfo(nativeUtils, filePath, depth_out, arrayJPG, evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds", evlId + "_" + dateString + "_" + (itemId) + "_jpg2.jpg", arrayThermal, arrayPNG, evlId + "_" + dateString + "_" + (99 + itemId) + "_thm", evlId + "_" + dateString + "_" + (99 + itemId) + "_thm2.png", arrayMaskY8, 1280, 800, finalx11, finaly11, finalx12, finaly12, temp_x21, temp_y21, temp_x22, temp_y22, false);

            if (dataEncrypt) {
                //FileHelper.overwriteFileSecret(filePath + File.separator + evlId + "_" + dateString + "_" + (itemId) + "_jpg2.jpg");
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }


}
