package org.itri.woundcamrtc.analytics;

//import java.awt.image.BufferedImage;
//import java.awt.image.DataBufferByte;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.helper.FileHelper;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;

/**
 * Created by wanghong on 10/8/16.
 */

public class ColorHelper {


    public boolean ruleRGB(int R, int G, int B) {
        boolean value1 = (R > 95) && (G > 40) && (B > 20) && ((Math.max(R, Math.max(G, B)) - Math.min(R, Math.min(G, B))) > 15) && (Math.abs(R - G) > 15) && (R > G) && (R > B);
        boolean value2 = (R > 220) && (G > 210) && (B > 170) && (Math.abs(R - G) <= 15) && (R > B) && (G > B);
        return (value1 || value2);
    }

    public boolean ruleHSV(int H, int S, int V) {
        return (H < 25) || (H > 230);
    }

    public boolean ruleYCrCb(int Y, int Cr, int Cb) {
        boolean value1 = Cr <= 1.5862 * Cb + 20;
        boolean value2 = Cr >= 0.3448 * Cb + 76.2069;
        boolean value3 = Cr >= -4.5652 * Cb + 234.5652;
        boolean value4 = Cr <= -1.15 * Cb + 301.75;
        boolean value5 = Cr <= -2.2857 * Cb + 432.85;
        return value1 && value2 && value3 && value4 && value5;
    }

    public Mat getSkin(Mat inputImage) {
        //http://bytefish.de/blog/opencv/skin_color_thresholding/

        Mat outputImage = inputImage.clone();
        Mat srcYCrCb = inputImage.clone(), srcHSV = inputImage.clone();

        //Convert the image into YCrCb and HSV ranges.
        Imgproc.cvtColor(inputImage, srcYCrCb, Imgproc.COLOR_BGR2YCrCb);
        inputImage.convertTo(srcHSV, inputImage.type());
        Imgproc.cvtColor(srcHSV, srcHSV, Imgproc.COLOR_BGR2HSV);
        //Normalize the values for HSV
        Core.normalize(srcHSV, srcHSV, 0.0, 255.0, Core.NORM_MINMAX, inputImage.type());
        double zero[] = new double[]{0, 0, 0};

        for (int row = 0; row < inputImage.rows(); row++) {
            for (int col = 0; col < inputImage.cols(); col++) {
                //fetch values in the respective color space
                double pixel[] = inputImage.get(row, col);
                double pixelYCrCb[] = srcYCrCb.get(row, col);
                double pixelHSV[] = srcHSV.get(row, col);


                int B = (int) pixel[0];
                int G = (int) pixel[1];
                int R = (int) pixel[2];
                boolean resultsRGB = ruleRGB(R, G, B);

                int Y = (int) pixelYCrCb[0];
                int Cr = (int) pixelYCrCb[1];
                int Cb = (int) pixelYCrCb[2];
                boolean resultsYCrCb = ruleYCrCb(Y, Cr, Cb);

                int H = (int) pixelHSV[0];
                int S = (int) pixelHSV[1];
                int V = (int) pixelHSV[2];
                boolean resultsHSV = ruleHSV(H, S, V);

                if (!(resultsRGB && resultsYCrCb && resultsHSV)) {
                    outputImage.put(row, col, zero);
                }

            }
        }

        return outputImage;
    }


//    private void detectWound(String procpath) {
//        Core.inRange(rgbMat, new Scalar(0, 0, 120), new Scalar(100, 100, 255), redMat);
//
////find contour
//        Mat ghierarchy = new Mat();
//        List<MatOfPoint> gcontours = new ArrayList<>();
//        Mat rhierarchy = new Mat();
//        List<MatOfPoint> rcontours = new ArrayList<>();
//        Imgproc.findContours(redMat, rcontours, rhierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        List<MatOfPoint> rhullList = new ArrayList<>();
//        for (MatOfPoint contour : rcontours) {
//            MatOfInt hull = new MatOfInt();
//            Imgproc.convexHull(contour, hull);
//            Point[] contourArray = contour.toArray();
//            Point[] hullPoints = new Point[hull.rows()];
//            List<Integer> hullContourIdxList = hull.toList();
//            for (int i = 0; i < hullContourIdxList.size(); i++) {
//                hullPoints[i] = contourArray[hullContourIdxList.get(i)];
//            }
//            rhullList.add(new MatOfPoint(hullPoints));
//        }
//
//        double rlargest_area =0;
//        int rlargest_contour_index = 0;
//        for (int contourIdx = 0; contourIdx < rcontours.size(); contourIdx++) {
//            double contourArea = Imgproc.contourArea(rcontours.get(contourIdx));
//            if (contourArea > rlargest_area) {
//                rlargest_area = contourArea;
//                rlargest_contour_index = contourIdx;
//            }
//        }
//
//        double rcurrentMax = 0;
//        for (MatOfPoint c: rhullList){
//            double area= Imgproc.contourArea(c);
//            if(area>rcurrentMax){
//                rcurrentMax = area;
//            }
//        }
//
//        Imgproc.drawContours(mat, rhullList, rlargest_contour_index, new Scalar(0, 255, 0, 255), 3);
//    }

    public static int[][][] ArrayRGB2HSV(Mat image) {
        final int width = image.width();
        final int height = image.height();

        int[][][] result = new int[height][width][3];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double[] rgb = image.get(0, 0);
                //int rgb = image.getRGB(x, y);

                double r = rgb[0] / 256;//(rgb >> 16) & 0xFF;
                double g = rgb[1] / 256;//(rgb >> 8) & 0xFF;
                double b = rgb[2] / 256;//(rgb & 0xFF);
                //System.out.println(r+","+g+","+b+",");
                int[] hsv = new int[3];
                Hsv hsv2 = rgb2hsv(new Rgb(r, g, b));
                hsv[0] = (Double.valueOf(hsv2.h)).intValue();
                hsv[1] = (Double.valueOf(hsv2.s * 255)).intValue();
                hsv[2] = (Double.valueOf(hsv2.v * 255)).intValue();

            }
        }
        return result;
    }
//	   public static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) 
//	   {
//
//		      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//		      final int width = image.getWidth();
//		      final int height = image.getHeight();
//		      final boolean hasAlphaChannel = image.getAlphaRaster() != null;
//
//		      int[][] result = new int[height][width];
//		      if (hasAlphaChannel) {
//		         final int pixelLength = 4;
//		         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
//		            int argb = 0;
//		            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
//		            argb += ((int) pixels[pixel + 1] & 0xff); // blue
//		            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
//		            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
//		            result[row][col] = argb;
//		            col++;
//		            if (col == width) {
//		               col = 0;
//		               row++;
//		            }
//		         }
//		      } else {
//		         final int pixelLength = 3;
//		         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
//		            int argb = 0;
//		            argb += -16777216; // 255 alpha
//		            argb += ((int) pixels[pixel] & 0xff); // blue
//		            argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
//		            argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
//		            result[row][col] = argb;
//		            col++;
//		            if (col == width) {
//		               col = 0;
//		               row++;
//		            }
//		         }
//		      }
//
//		      return result;
//		   }

    public static Rgb hsv2rgb(Hsv hsv) {
        Rgb rgb = new Rgb();

        double hh = hsv.h / 60;
        int i = ((int) hh) % 6;

        double f = hh - i;
        double p = hsv.v * (1 - hsv.s);
        double q = hsv.v * (1 - f * hsv.s);
        double t = hsv.v * (1 - (1 - f) * hsv.s);

        switch (i) {
            case 0:
                rgb.r = hsv.v;
                rgb.g = t;
                rgb.b = q;
                break;
            case 1:
                rgb.r = q;
                rgb.g = hsv.v;
                rgb.b = p;
                break;
            case 2:
                rgb.r = p;
                rgb.g = hsv.v;
                rgb.b = t;
                break;
            case 3:
                rgb.r = p;
                rgb.g = q;
                rgb.b = hsv.v;
                break;
            case 4:
                rgb.r = t;
                rgb.g = p;
                rgb.b = hsv.v;
                break;
            case 5:
                rgb.r = hsv.v;
                rgb.g = p;
                rgb.b = q;
                break;
            default:
        }

        return rgb;
    }

    public static Hsv rgb2hsv(Rgb rgb) {
        Hsv hsv = new Hsv();

        double max = Math.max(Math.max(rgb.r, rgb.g), rgb.b);
        double min = Math.min(Math.min(rgb.r, rgb.g), rgb.b);
        double delta = max - min;

        if (delta == 0) {
            hsv.h = 360;
            hsv.s = 0;
            hsv.v = max;
            return hsv;
        }

        if (max == rgb.r) {
            hsv.h = (rgb.g - rgb.b) / delta % 6;
        } else if (max == rgb.g) {
            hsv.h = (rgb.b - rgb.r) / delta + 2;
        } else {
            hsv.h = (rgb.r - rgb.g) / delta + 4;
        }
        hsv.h *= 60;

        if (max == 0) {
            hsv.s = 0;
        } else {
            hsv.s = delta / max;
        }

        hsv.v = max;

        return hsv;
    }

    private static class Hsv {
        double h; /* 0 ~ 360 degree */
        double s; /* 0.0 ~ 1.0 */
        double v; /* 0.0 ~ 1.0 */

        public Hsv() {
        }

        @SuppressWarnings("unused")
        public Hsv(double h, double s, double v) {
            this.h = h;
            this.s = s;
            this.v = v;
        }

        @Override
        public String toString() {
            return "[h = " + h + ", s = " + s + ", v = " + v + "]";
        }
    }

    private static class Rgb {
        double r; /* 0.0 ~ 1.0 */
        double g; /* 0.0 ~ 1.0 */
        double b; /* 0.0 ~ 1.0 */

        public Rgb() {
        }

        public Rgb(double r, double g, double b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public String toString() {
            return "[r = " + r + ", g = " + g + ", b = " + b + "]";
        }
    }

    public static void changeColor(Mat input, int from, int to) {
        int rows = input.rows();
        int cols = input.cols();

        int val = 0;
        int y = 0;
        int x = 0;
        for (y = 0; y < rows; y++) {
            for (x = 0; x < cols; x++) {
                val = (int) input.get(y, x)[0];
                if (val == from)
                    input.put(y, x, to);
            }
        }
    }

    public static Mat adjustBrightnessContrast(Mat srcImage) {
        Mat filterImage = srcImage.clone();
        Imgproc.cvtColor(srcImage, filterImage, Imgproc.COLOR_BGR2YCrCb);
        java.util.List<Mat> filterImageList = new ArrayList<Mat>(3);
        Core.split(filterImage, filterImageList);
        Mat luminance = filterImageList.get(0);
        Imgproc.equalizeHist(luminance, luminance);
        filterImageList.set(0, luminance);
        Core.merge(filterImageList, filterImage);
        Imgproc.cvtColor(filterImage, srcImage, Imgproc.COLOR_YCrCb2BGR);
        return srcImage;
    }

    public static Mat whiteBalanceWithReferencedPoint(Mat img, double refR, double refG, double refB, int defaultGray) {
        //    ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//    R/G/B?????????????????????????????????????????????????????????????????????????????????
//    ???????????????????????????????????????????????????????????????????????????
//    Manually select a reference point in the picture, the color of the point is white, black or gray, try to choose the non-reflective part.
//    The values of the three colors of R / G / B should be close to equal, otherwise it is judged that the color at that point has been cast.
//    According to the gray-scale world method, the color of the reference point is analyzed, and the white balance is corrected.

        //???????????????RGB???????????????
        double refMean = 128;
        if (defaultGray < 0)
            refMean = (refR + refG + refB) / 3;
        else
            refMean = defaultGray;

        double KB = refMean / refB;
        double KG = refMean / refG;
        double KR = refMean / refR;

        List<Mat> lRgb = new ArrayList<Mat>(3);
        //RGB???????????????
        Core.split(img, lRgb);
        Mat mR = lRgb.get(2);
        Mat mG = lRgb.get(1);
        Mat mB = lRgb.get(0);

        Core.multiply(mR, new Scalar(KR), mR);
        Core.multiply(mG, new Scalar(KG), mG);
        Core.multiply(mB, new Scalar(KB), mB);

        Mat dest = new Mat();
        List<Mat> listMat = Arrays.asList(mB, mG, mR);
        Core.merge(listMat, dest);
        return dest;
    }

    public static Mat chrominanceAutoBalance(Mat img) {
        //??????????????????????????????????????????
        // ??????????????????????????????????????????????????????RGB???????????????????????????????????????????????????
        // ????????????????????????????????????RGB????????????????????????
        List<Mat> lRgb = new ArrayList<Mat>(3);
        //RGB???????????????

        Core.split(img, lRgb);
        Mat mR = lRgb.get(2);
        Mat mG = lRgb.get(1);
        Mat mB = lRgb.get(0);
        MatOfDouble muR = new MatOfDouble();
        MatOfDouble muG = new MatOfDouble();
        MatOfDouble muB = new MatOfDouble();
        MatOfDouble sigma = new MatOfDouble();

        //??????????????????RGB???????????????
        Core.meanStdDev(mR, muR, sigma);
        Core.meanStdDev(mG, muG, sigma);
        Core.meanStdDev(mB, muB, sigma);
        double meanR = muR.get(0, 0)[0];
        double meanG = muG.get(0, 0)[0];
        double meanB = muB.get(0, 0)[0];

        //???????????????RGB???????????????
        double KB = (meanR + meanG + meanB) / (3 * meanB);
        double KG = (meanR + meanG + meanB) / (3 * meanG);
        double KR = (meanR + meanG + meanB) / (3 * meanR);

        //??????RGB????????????????????????
        Core.multiply(mR, new Scalar(KR), mR);
        Core.multiply(mG, new Scalar(KG), mG);
        Core.multiply(mB, new Scalar(KB), mB);


        Mat dest = new Mat();
        List<Mat> listMat = Arrays.asList(mB, mG, mR);
        Core.merge(listMat, dest);//??????split()???????????????????????????????????????

//        Imgproc.cvtColor(dest, dest, Imgproc.COLOR_BGR2HSV);
//        Core.split(dest, lRgb);
//        mR = lRgb.get(2);
//        mG = lRgb.get(1);
//        mB = lRgb.get(0);
////        Core.multiply(mG, new Scalar(10), mG);
//        Imgproc.equalizeHist(mG, mG);
//
////        Imgproc.equalizeHist(mR, mR);
//////        listMat = Arrays.asList(mB, mG, mR);
//////        Core.merge(listMat, dest);//??????split()???????????????????????????????????????
//////        Imgproc.cvtColor(dest, dest, Imgproc.COLOR_HSV2BGR);
////
////        //RGB?????????????????????
////        //Mat dest = new Mat();
//        List<Mat> listMat2 = Arrays.asList(mB, mG, mR);
//        Core.merge(listMat2, dest);//??????split()???????????????????????????????????????
//        Imgproc.cvtColor(dest, dest, Imgproc.COLOR_HSV2BGR);
        return dest;
    }

    public static void colorCorrectionFast(Mat image, int rOffset, int gOffset, int bOffset) {
        if (bOffset == 0 && gOffset == 0 && rOffset == 0)
            return;
        // kernoli todo
        int nr = image.rows(); // number of rows
        int nc = image.cols(); // number of columns
        int r = 0;
        int g = 0;
        int b = 0;
        for (int y = 0; y < nr; y++) {
            for (int x = 0; x < nc; x++) {
                double[] data = image.get(y, x);
                b = (int) data[0] + bOffset;
                g = (int) data[1] + gOffset;
                r = (int) data[2] + rOffset;
                image.put(y, x, b, g, r);
            } // end of row
        }
    }

    public static Mat reduceColorFast(Mat input, int div) {
        // kernoli todo
        Mat image = input.clone();
        int nr = image.rows(); // number of rows
        int nc = image.cols(); // number of columns
        int r = 0;
        int g = 0;
        int b = 0;
        for (int y = 0; y < nr; y++) {
            for (int x = 0; x < nc; x++) {
                double[] data = image.get(y, x);
                b = (int) data[0] / div * div + div / 2;
                g = (int) data[1] / div * div + div / 2;
                r = (int) data[2] / div * div + div / 2;
                image.put(y, x, b, g, r);
            } // end of row
        }
        return image;
    }

    public static Mat reduceColorKmean(Mat input, int colorCount) {
        Mat samples = input.reshape(1, input.cols() * input.rows());
        Mat samples32f = new Mat();
        samples.convertTo(samples32f, CvType.CV_32F, 1.0 / 255.0);

        Mat labels = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 1, 1);
        Mat centers = new Mat();
        Core.kmeans(samples32f, colorCount, labels, criteria, 1, Core.KMEANS_PP_CENTERS, centers);

        centers.convertTo(centers, CvType.CV_8UC1, 255.0);
        centers.reshape(3);

        Mat dst = input.clone();
        int rows = 0;
        int label = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        int y = 0;
        int x = 0;
        for (y = 0; y < input.rows(); y++) {
            for (x = 0; x < input.cols(); x++) {
                label = (int) labels.get(rows, 0)[0];
                r = (int) centers.get(label, 2)[0];
                g = (int) centers.get(label, 1)[0];
                b = (int) centers.get(label, 0)[0];
                dst.put(y, x, b, g, r);
                rows++;
            }
        }
        return dst;
    }

    public static Mat applyCLAHE(Mat src) {
        //https://www.itread01.com/content/1546985164.html
        Mat img = new Mat();
        Mat L2 = new Mat();
        Mat dest = new Mat();
        Imgproc.cvtColor(src, img, Imgproc.COLOR_BGR2Lab);
        List<Mat> channels = new ArrayList<Mat>(3);
        Core.split(img, channels);
        CLAHE clahe = Imgproc.createCLAHE();
//        clahe.setClipLimit(2.0);
        clahe.apply(channels.get(0), L2);
        Core.merge(new ArrayList<Mat>(Arrays.asList(L2, channels.get(1), channels.get(2))), dest);
        Imgproc.cvtColor(dest, dest, Imgproc.COLOR_Lab2BGR);
        return dest;
    }


    public static double mean_pixel(Mat img) {
        MatOfDouble mu = new MatOfDouble();
        MatOfDouble sigma = new MatOfDouble();
        if (img.channels() > 2) {
            Imgproc.cvtColor(img.clone(), img, Imgproc.COLOR_BGR2GRAY);
            Core.meanStdDev(img, mu, sigma);
            return mu.get(0, 0)[0];
        } else {
            Core.meanStdDev(img, mu, sigma);
            return mu.get(0, 0)[0];
        }
    }

    public static double auto_gamma_value(Mat img) {
        double max_pixel = 255;
        double middle_pixel = 128;
        double pixel_range = 256;
        double mean_l = mean_pixel(img);

        double gamma = Math.log(middle_pixel / pixel_range) / Math.log(mean_l / pixel_range); // Formula from ImageJ

        return gamma;
    }

    public static byte saturate(double val) {
        int iVal = (int) Math.round(val);
        iVal = iVal > 255 ? 255 : (iVal < 0 ? 0 : iVal);
        return (byte) iVal;
    }

    public static Mat correctGamma(Mat matImgSrc, double gammaValue) {
        Mat lookUpTable = new Mat(1, 256, CvType.CV_8U);
        byte[] lookUpTableData = new byte[(int) (lookUpTable.total() * lookUpTable.channels())];
        for (int i = 0; i < lookUpTable.cols(); i++) {
            lookUpTableData[i] = saturate(Math.pow(i / 255.0, gammaValue) * 255.0);
        }
        lookUpTable.put(0, 0, lookUpTableData);

        Mat img = new Mat();
        Mat dest = new Mat();
        Imgproc.cvtColor(matImgSrc, img, Imgproc.COLOR_BGR2Lab);
        List<Mat> channels = new ArrayList<Mat>(3);
        Core.split(img, channels);

        Core.LUT(channels.get(0), lookUpTable, img);

        Core.merge(new ArrayList<Mat>(Arrays.asList(img, channels.get(1), channels.get(2))), dest);
        Imgproc.cvtColor(dest, dest, Imgproc.COLOR_Lab2BGR);
        return dest;
    }

    //https://zh-tw.programqa.com/question/56905592


    /**
     * opencv ??????????????????
     * brightnessException ????????????????????????????????????????????????????????????
     * cast ??????????????????????????????1???????????????????????????1??????????????????????????????cast????????????da??????0???????????????da??????0????????????
     * ???????????????cast???da???????????????????????????????????????
     * https://janche.github.io/2019/04/26/OpenCV/
     */

    public static Integer brightnessException(File jpegFile) {
        Mat srcImage = null;
        if (!AppResultReceiver.dataEncrypt) {
            srcImage = Imgcodecs.imread(jpegFile.getAbsolutePath());
        } else {
            srcImage = FileHelper.imreadSecret(jpegFile.getAbsolutePath());
        }
        Mat dstImage = new Mat();
        // ???RGB??????????????????
        Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_BGR2GRAY);
        float a = 0;
        int Hist[] = new int[256];
        for (int i = 0; i < 256; i++) {
            Hist[i] = 0;
        }
        for (int i = 0; i < dstImage.rows(); i++) {
            for (int j = 0; j < dstImage.cols(); j++) {
                //???????????????????????????128??????????????????
                a += (float) (dstImage.get(i, j)[0] - 128);
                int x = (int) dstImage.get(i, j)[0];
                Hist[x]++;
            }
        }
        float da = a / (float) (dstImage.rows() * dstImage.cols());
        System.out.println(da);
        float D = Math.abs(da);
        float Ma = 0;
        for (int i = 0; i < 256; i++) {
            Ma += Math.abs(i - 128 - da) * Hist[i];
        }
        Ma /= (float) ((dstImage.rows() * dstImage.cols()));
        float M = Math.abs(Ma);
        float K = D / M;
        float cast = K;
        System.out.printf("??????????????? %f\n", cast);
        if (cast >= 1) {
            System.out.printf("?????????" + da);
            if (da > 0) {
                System.out.printf("??????\n");
                return 2;
            } else {
                System.out.printf("??????\n");
                return 1;
            }
        } else {
            System.out.printf("???????????????\n");
            return 0;
        }
    }

    public static void imageColor(File jpegFile) {
        Mat srcImage = null;
        if (!AppResultReceiver.dataEncrypt) {
            srcImage = Imgcodecs.imread(jpegFile.getAbsolutePath());
        } else {
            srcImage = FileHelper.imreadSecret(jpegFile.getAbsolutePath());
        }
        Mat dstImage = new Mat();
        Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_BGR2HSV);
        int i = 0, j = 0;
        loop:
        for (i = 0; i < dstImage.rows(); i++) {
            for (j = 0; j < dstImage.cols(); j++) {
                //???????????????????????????128??????????????????
                double[] colorVec = dstImage.get(i, j);
                int x = (int) dstImage.get(i, j)[0];
                if ((colorVec[0] >= 0 && colorVec[0] <= 180)
                        && (colorVec[1] >= 0 && colorVec[1] <= 255)
                        && (colorVec[2] >= 0 && colorVec[2] <= 46)) {
                    continue;
                } else if ((colorVec[0] >= 0 && colorVec[0] <= 180)
                        && (colorVec[1] >= 0 && colorVec[1] <= 43)
                        && (colorVec[2] >= 46 && colorVec[2] <= 220)) {
                    continue;
                } else if ((colorVec[0] >= 0 && colorVec[0] <= 180)
                        && (colorVec[1] >= 0 && colorVec[1] <= 30)
                        && (colorVec[2] >= 221 && colorVec[2] <= 255)) {
                    continue;
                } else {
                    System.out.println("????????????");
                    break loop;
                }
            }
        }
        if (i == dstImage.rows() && j == dstImage.cols()) {
            System.out.println("????????????");
        }
    }

    /**
     * javacv ?????????????????????
     * ???????????????????????????????????????
     */
    public static void clarityException(File jpegFile) {
        String path = "E:\\test\\";
        Mat srcImage = null;
        if (!AppResultReceiver.dataEncrypt) {
            srcImage = Imgcodecs.imread(jpegFile.getAbsolutePath());
        } else {
            srcImage = FileHelper.imreadSecret(jpegFile.getAbsolutePath());
        }

        Mat dstImage = new Mat();
        //??????????????????
        Imgproc.cvtColor(srcImage, dstImage, Imgproc.COLOR_BGR2GRAY);
        //???gray???????????????????????????
        Imgcodecs.imwrite(path + "gray-" + jpegFile.getName(), dstImage);

        Mat laplacianDstImage = new Mat();
        //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Imgproc.Laplacian(dstImage, laplacianDstImage, CvType.CV_64F);
        //???laplacian???????????????????????????????????????????????????????????????
        Imgcodecs.imwrite(path + "laplacian-" + jpegFile.getName(), laplacianDstImage);

        //???????????????
        MatOfDouble meansrc = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();

        //??????????????????????????????
        Core.meanStdDev(laplacianDstImage, meansrc, stddev);
        // ((?????????????????????)??????)????????????
        // double norm = Core.norm(laplacianDstImage);
        // System.out.println("\n??????????????????\n" + mean.dump());
        double lMeanSrc = meansrc.get(0, 0)[0];
        double lStdSrc = stddev.get(0, 0)[0];
        System.out.println(jpegFile.getName() + "?????????????????????\n" + lStdSrc);
        // System.out.println(jpegFile.getName()+"????????????\n" + norm);
    }
}