package org.jni;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import org.itri.woundcamrtc.AppResultReceiver;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.StatModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.lang.Math.pow;
import static org.opencv.core.Core.LUT;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

public class NativeUtils {

    static {
        System.loadLibrary("native_utils");
    }

    public static long objPtr = 0;
    public static String EvlId = "";

    // Native methods
    public static native String Version();
    public static native String createObject(NativeUtils mNativeObj);
    public static native String deleteObject(NativeUtils mNativeObj);

    public static native void calibracionParam(NativeUtils mNativeOb, int type, int img_width, int img_height, int cx, int cy, double sx, double sy, double tx, double ty, double tz);

    public static native String genTriMesh2(NativeUtils mNativeObj, short[] depthY16);
    public static native String gen3DColorImage2(NativeUtils mNativeObj, String jFilePath, String jFile3dMainNameJpg, String jPicFileNameJPG, int jpgWidth, int jpgHeight);
    public static native String gen3DThermalImage2(NativeUtils mNativeObj, String jFilePath, String jFile3dMainNameThm, String jPicFileNamePng);
    public static native String calWoundAreaInfo2(NativeUtils mNativeObj, String jFilePath, String jFile3dMainNameJpg, byte[] maskY8, int width, int height, int xx11, int yy11, int xx12, int yy12, int xx21, int yy21, int xx22, int yy22);
    public static native String getThermalOnXY2(NativeUtils mNativeObj, int x, int y);

    public static native String nativeGetWoundInfo(NativeUtils mNativeObj, String jFilePath, short[] depthY16, byte[] jpg888, String jFile3dMainName3ds, String jPicFileNameJPG, float[] thermalY32, byte[] png888, String jFile3dMainNameThm, String jPicFileNamePNG, byte[] maskY8, int width, int height, int xx11, int yy11, int xx12, int yy12, int xx21, int yy21, int xx22, int yy22, boolean save);
    public static native String nativeGen3DColorImage(NativeUtils mNativeObj, String filePath, String file3dMainName, String picFileName, short[] depthY16, byte[] rgb888, int width, int height, long addrDst);
    public static native String nativeGen3DThermalImage(NativeUtils mNativeObj, String filePath, String file3dMainName, String picFileName, short[] depthY16, float[] thermalY32, byte[] rgb888, int width, int height, long addrDst);


    // ref from https://github.com/RayXie29/Linear_Color_Correction_Matrix
    private static int MAX_UCHAR = 255;

    public int saturateCastUchar(int x) {
        return x > MAX_UCHAR ? MAX_UCHAR : (x < 0 ? 0 : x);
    }

    public int saturateCastUchar(double x) {
        return (int) (x > MAX_UCHAR ? MAX_UCHAR : (x < 0 ? 0 : x));
    }


    // Create C++ object
    public void createup() {
        createObject(this);
    }

    // Delete C++ object on cleanup
    public void cleanup() {
        deleteObject(this);
        this.objPtr = 0;
    }

    public Mat LCC5Colors_Java(final Context context, Mat img, int[][] correctionColor) {

        if (false) {
            Mat dst = new Mat();
//            Version();
//            LCC(img.getNativeObjAddr(), dst.getNativeObjAddr(), correctionColor);
            return dst;
        } else {

            Mat ReferenceColor = new Mat(5, 3, CV_32FC1, new Scalar(0));
            ReferenceColor.put(0, 0, Double.valueOf(242));
            ReferenceColor.put(0, 1, Double.valueOf(245));
            ReferenceColor.put(0, 2, Double.valueOf(245));
            ReferenceColor.put(1, 0, Double.valueOf(143));
            ReferenceColor.put(1, 1, Double.valueOf(65));
            ReferenceColor.put(1, 2, Double.valueOf(49));
            ReferenceColor.put(2, 0, Double.valueOf(80));
            ReferenceColor.put(2, 1, Double.valueOf(148));
            ReferenceColor.put(2, 2, Double.valueOf(99));
            ReferenceColor.put(3, 0, Double.valueOf(54));
            ReferenceColor.put(3, 1, Double.valueOf(53));
            ReferenceColor.put(3, 2, Double.valueOf(52));
            ReferenceColor.put(4, 0, Double.valueOf(59));
            ReferenceColor.put(4, 1, Double.valueOf(52));
            ReferenceColor.put(4, 2, Double.valueOf(155));

            Mat OriginalColor = new Mat(5, 3, CV_32FC1, new Scalar(0));
            OriginalColor.put(0, 0, Double.valueOf(correctionColor[0][0]));
            OriginalColor.put(0, 1, Double.valueOf(correctionColor[0][1]));
            OriginalColor.put(0, 2, Double.valueOf(correctionColor[0][2]));
            OriginalColor.put(1, 0, Double.valueOf(correctionColor[1][0]));
            OriginalColor.put(1, 1, Double.valueOf(correctionColor[1][1]));
            OriginalColor.put(1, 2, Double.valueOf(correctionColor[1][2]));
            OriginalColor.put(2, 0, Double.valueOf(correctionColor[2][0]));
            OriginalColor.put(2, 1, Double.valueOf(correctionColor[2][1]));
            OriginalColor.put(2, 2, Double.valueOf(correctionColor[2][2]));
            OriginalColor.put(3, 0, Double.valueOf(correctionColor[3][0]));
            OriginalColor.put(3, 1, Double.valueOf(correctionColor[3][1]));
            OriginalColor.put(3, 2, Double.valueOf(correctionColor[3][2]));
            OriginalColor.put(4, 0, Double.valueOf(correctionColor[4][0]));
            OriginalColor.put(4, 1, Double.valueOf(correctionColor[4][1]));
            OriginalColor.put(4, 2, Double.valueOf(correctionColor[4][2]));

            Mat O_T = OriginalColor.t();
            Mat temp = new Mat();
            Core.gemm(O_T, OriginalColor, 1, new Mat(), 0, temp);

            Mat ColorMatrix = new Mat(3, 3, CV_32FC1, new Scalar(0));
            Mat temp2 = new Mat();
            Core.gemm(temp.inv(), O_T, 1, new Mat(), 0, temp2);
            Core.gemm(temp2, ReferenceColor, 1, new Mat(), 0, ColorMatrix);

            int imgHeight = img.rows(), imgWidth = img.cols();
            Mat imageF = new Mat();
            img.convertTo(imageF, CV_32FC3, 1.0 / 255.0);
            Mat orig_img_linear = imageF.reshape(1, imgHeight * imgWidth);

            Mat color_matrixed_linear = new Mat();
            Core.gemm(orig_img_linear, ColorMatrix, 1, new Mat(), 0, color_matrixed_linear);

            Mat endImage = color_matrixed_linear.reshape(3, imgHeight);
            Mat dst = new Mat();
            endImage.convertTo(dst, CV_8UC3, 255);
            return dst;
        }
    }

    public Mat whiteBalanceWithReferencedPoint(Mat src, double pixelB, double pixelG, double pixelR) {
        //在畫面中選擇一個R/G/B三個顏色值接近相等的白色、黑色或灰色參考點
        List<Mat> imageRGB = new ArrayList<Mat>(3);
        Core.split(src, imageRGB);

        double kb = (pixelR + pixelG + pixelB) / (3 * pixelB);
        double kg = (pixelR + pixelG + pixelB) / (3 * pixelG) + 0.001;
        double kr = (pixelR + pixelG + pixelB) / (3 * pixelR) + 0.001;

        Core.multiply(imageRGB.get(2), new Scalar(kr), imageRGB.get(2));
        Core.multiply(imageRGB.get(1), new Scalar(kg), imageRGB.get(1));
        Core.multiply(imageRGB.get(0), new Scalar(kb), imageRGB.get(0));

        Mat dest = new Mat();
        Core.merge(imageRGB, dest);
        return dest;
    }

    public Mat whiteBalanceWithGrayWorld(Mat img) {
        //灰度世界自動白平衡法
        // 它假設對於一副色彩豐富的圖像，圖像上RGB三個分量的平均值趨於同一個灰度值，
        // 一般取這個灰度值的大小為RGB三分量的平均值。
        List<Mat> imageRGB = new ArrayList<Mat>(3);
        //RGB三通道分離
        Core.split(img, imageRGB);

        MatOfDouble muR = new MatOfDouble();
        MatOfDouble muG = new MatOfDouble();
        MatOfDouble muB = new MatOfDouble();
        MatOfDouble sigma = new MatOfDouble();

        //求原始圖像的RGB分量的均值
        Core.meanStdDev(imageRGB.get(2), muR, sigma);
        Core.meanStdDev(imageRGB.get(1), muG, sigma);
        Core.meanStdDev(imageRGB.get(0), muB, sigma);
        double meanR = muR.get(0, 0)[0];
        double meanG = muG.get(0, 0)[0];
        double meanB = muB.get(0, 0)[0];

        //需要調整的RGB分量的增益
        double kb = (meanR + meanG + meanB) / (3 * meanB);
        double kg = (meanR + meanG + meanB) / (3 * meanG);
        double kr = (meanR + meanG + meanB) / (3 * meanR);

        //調整RGB三個通道各自的值
        Core.multiply(imageRGB.get(2), new Scalar(kr), imageRGB.get(2));
        Core.multiply(imageRGB.get(1), new Scalar(kg), imageRGB.get(1));
        Core.multiply(imageRGB.get(0), new Scalar(kb), imageRGB.get(0));

        Mat dest = new Mat();
        Core.merge(imageRGB, dest);//合併split()方法分離出來的彩色通道數據
        return dest;
    }

    public Mat whiteBalanceWithBrightestPoint(Mat img) {
        //最亮點白平衡法
        // 它假設對於一副色彩豐富的圖像，圖像上有一點為RGB三個分量的平均值趨於同一個最亮值，
        // 一般取這個最亮值的大小為RGB三分量的平均值。
        List<Mat> imageRGB = new ArrayList<Mat>(3);
        //RGB三通道分離
        Core.split(img, imageRGB);

        MatOfDouble muR = new MatOfDouble();
        MatOfDouble muG = new MatOfDouble();
        MatOfDouble muB = new MatOfDouble();
        MatOfDouble sigma = new MatOfDouble();

        //求原始圖像的RGB分量的均值
        Core.meanStdDev(imageRGB.get(2), muR, sigma);
        Core.meanStdDev(imageRGB.get(1), muG, sigma);
        Core.meanStdDev(imageRGB.get(0), muB, sigma);
        double meanR = muR.get(0, 0)[0];
        double meanG = muG.get(0, 0)[0];
        double meanB = muB.get(0, 0)[0];

        //需要調整的RGB分量的增益
        double kb = (meanR + meanG + meanB) / (3 * meanB);
        double kg = (meanR + meanG + meanB) / (3 * meanG);
        double kr = (meanR + meanG + meanB) / (3 * meanR);

        //調整RGB三個通道各自的值
        Core.multiply(imageRGB.get(2), new Scalar(kr), imageRGB.get(2));
        Core.multiply(imageRGB.get(1), new Scalar(kg), imageRGB.get(1));
        Core.multiply(imageRGB.get(0), new Scalar(kb), imageRGB.get(0));

        Mat dest = new Mat();
        Core.merge(imageRGB, dest);//合併split()方法分離出來的彩色通道數據
        return dest;
    }

    public Mat beautyFace(Mat image) {
        //Dest =(Src * (100 - Opacity) + (Src + 2 * GuassBlur(EPFFilter(Src) - Src + 128) - 256) * Opacity) /100 ;
        Mat dst = new Mat();

        // int value1 = 3, value2 = 1; 磨皮程度與細節程度的確定
        int value1 = 3, value2 = 1;
        int dx = value1 * 5; // 雙邊濾波參數之一
        double fc = value1 * 12.5; // 雙邊濾波參數之一
        double p = 0.1f; // 透明度
        Mat temp1 = new Mat(), temp2 = new Mat(), temp3 = new Mat(), temp4 = new Mat();

        // 雙邊濾波
        Imgproc.bilateralFilter(image, temp1, dx, fc, fc);

        // temp2 = (temp1 - image + 128);
        Mat temp22 = new Mat();
        Core.subtract(temp1, image, temp22);
        // Core.subtract(temp22, new Scalar(128), temp2);
        Core.add(temp22, new Scalar(128, 128, 128, 128), temp2);
        // 高斯模糊
        Imgproc.GaussianBlur(temp2, temp3, new org.opencv.core.Size(2 * value2 - 1, 2 * value2 - 1), 0, 0);

        // temp4 = image + 2 * temp3 - 255;
        Mat temp44 = new Mat();
        temp3.convertTo(temp44, temp3.type(), 2, -255);
        Core.add(image, temp44, temp4);
        // dst = (image*(100 - p) + temp4*p) / 100;
        Core.addWeighted(image, p, temp4, 1 - p, 0.0, dst);

        Core.add(dst, new Scalar(10, 10, 10), dst);
        return dst;

    }

    public Mat autoBrightnessAndContrastWithROI(Mat mat, Mat roiMat) {
        // find contrast and brightness to fit into 8 bit
        Core.MinMaxLocResult mmlr = Core.minMaxLoc(roiMat);
        double min = mmlr.minVal; // Math.min(mmlr.minVal, 0);
        double max = mmlr.maxVal; // Math.max(mmlr.maxVal, 255);
        double alpha = 256.0d / (max - min);
        double beta = -min * alpha;

        // conversion to 8 bit Mat
        Mat byteMat = new MatOfByte();
        mat.convertTo(byteMat, CvType.CV_8U, alpha, beta);

        return byteMat;
    }


    public Mat autoBrightnessAndContrastWithParams(Mat mat, double alpha, double beta) {
        // conversion to 8 bit Mat
        Mat byteMat = new MatOfByte();
        mat.convertTo(byteMat, CvType.CV_8U, alpha, beta);

        return byteMat;
    }

    public Mat autoBrightnessAndContrast(Mat mat) {
        // find contrast and brightness to fit into 8 bit
        Core.MinMaxLocResult mmlr = Core.minMaxLoc(mat);
        double min = mmlr.minVal; // Math.min(mmlr.minVal, 0);
        double max = mmlr.maxVal; // Math.max(mmlr.maxVal, 255);
        double alpha = 256.0d / (max - min);
        double beta = -min * alpha;

        // conversion to 8 bit Mat
        Mat byteMat = new MatOfByte();
        mat.convertTo(byteMat, CvType.CV_8U, alpha, beta);

        return byteMat;
    }

    public Mat logOfOnePlusAbs(Mat mat) {

        // make absolute values and log
        Mat tempMat = mat.clone();
        Core.absdiff(tempMat, new Scalar(0.0d), tempMat);
        Core.add(tempMat, new Scalar(1.0d), tempMat);
        Core.log(tempMat, tempMat);

        // find contrast and brightness to fit into 8 bit
        Core.MinMaxLocResult mmlr = Core.minMaxLoc(tempMat);
        double min = Math.min(mmlr.minVal, 0);
        double max = mmlr.maxVal;
        double alpha = 256.0d / (max - min);
        double beta = -min * alpha;

        // conversion to 8 bit Mat applying contrast alpha and brightness beta
        Mat byteMat = new MatOfByte();
        tempMat.convertTo(byteMat, CvType.CV_8U, alpha, beta);

        return byteMat;
    }


    public Mat correctGamma(Mat img, double gamma) {
        double inverse_gamma = 1.0 / gamma;

        Mat lut_matrix = new Mat(1, 256, CV_8UC1);


        final int lutSize = lut_matrix.cols() * lut_matrix.rows() * lut_matrix.channels();
        byte[] lutBufferArray = new byte[lutSize];
        int lutIndex = -1;
        for (int i = 0; i < 256; ++i) {
            lutBufferArray[++lutIndex] = (byte) (pow((double) i / 255.0, inverse_gamma) * 255.0);
        }
        lut_matrix.put(0, 0, lutBufferArray);

        Mat result = new Mat();
        LUT(img, lut_matrix, result);

        return result;
    }

    public double get_Gamma_Value(Mat gray_img) {
        if (gray_img.empty()) {
            return -1.0;
        }

        Scalar meam_value = Core.mean(gray_img);

        double val = meam_value.val[0];
        //float gamma_val = (log10(val / 255.0)) / (log10(0.5));
        double gamma_val = (Math.log10(0.5)) / (Math.log10(val / 255.0));

        return gamma_val;
    }

}
