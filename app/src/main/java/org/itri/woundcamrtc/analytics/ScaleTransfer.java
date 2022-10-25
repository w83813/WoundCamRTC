package org.itri.woundcamrtc.analytics;

import android.os.Build;
import android.util.Log;

import java.math.BigDecimal;

import org.itri.woundcamrtc.AppResultReceiver;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

/**
 * @author YueMin
 * @version DM1.0
 * @Effective_Range 1~75CM
 * @since 2018/02/26
 */
public class ScaleTransfer {

    private static final String TAG = "ScaleTransfer";
//    final private static double defaultScaleWidth = 106.9782;//59.60181;
//    final private static double defaultScaleHeight = 104.3222;
//    final private static double WidthScaleCoef = 1.43236;//0.46549;
//    final private static double HeightScaleCoef = 1.40758;//1.21027;
//    private static boolean DistanceSetting = false;
//    private static double Distance = 30;
//    private static double PixelsPerCentimeterWidth = 0;
//    private static double PixelsPerCentimeterHeight = 0;
//
//    private static double powPixelsPerCentimeter = 0;
//
//    public static double getPowPixelsPerCentimeter() {
//        return powPixelsPerCentimeter;
//    }
//
//    public static double getMultiPixelsPerCentimeter() {
//        return multiPixelsPerCentimeter;
//    }
//
//    private static double multiPixelsPerCentimeter = 0;

    public static double[] estimateMarkerPixels(double distance) {
        // 參考標籤對應的長寬pixels
        Log.d(TAG,"Build.MODEL:" + Build.MODEL);
        if (Build.MODEL.endsWith("MPD100") || Build.MODEL.endsWith("MPD500"))
            return estimateMarkerPixelsMPD100(distance);
        else if (Build.MODEL.endsWith("SM-N9700"))
            return estimateMarkerPixelsSamsungNote10(distance);
        else if (Build.MODEL.endsWith("Redmi Note 8"))
            return estimateMarkerPixelsRedmi8T(distance);
        else
            return estimateMarkerPixelsMPD100(distance);
    }

    public static double[] estimateMarkerPixelsMPD100(double distance) {
        if (distance < 25)
            distance = 25;
        // the formula generated from excel xy scatter charts for MPD100
        double[] realScale = new double[2];
        realScale[0] = ((0.0308 * distance * distance) - (4.2579 * distance) + 183.22) * AppResultReceiver.refMarkerWidth;
        realScale[1] = ((0.0308 * distance * distance) - (4.2579 * distance) + 183.22) * AppResultReceiver.refMarkerWidth;
        return realScale;
    }

    public static double[] estimateMarkerPixelsSamsungNote10(double distance) {
        if (distance < 5)
            distance = 5;
        distance = distance - 1.5;  // 扣掉背夾厚度
        // 1cm at 10cm w382 h380
        // 1cm at 20cm w169 h197
        // 1cm at 30cm w109 h107
        // 1cm at 40cm w80  h79
        // 1cm at 50cm w63  h61
        // 1cm at 60cm w53  h53
        // the formula generated from excel xy scatter charts
        double[] realScale = new double[2];
        //realScale[0] = ((0.0002 * distance * distance * distance * distance) - (0.0414 * distance * distance * distance) + (2.6033 * distance * distance) - (73.854 * distance) + 898.83) * AppResultReceiver.refMarkerWidth;
        //realScale[1] = ((0.0002 * distance * distance * distance * distance) - (0.0414 * distance * distance * distance) + (2.6033 * distance * distance) - (73.854 * distance) + 898.83) * AppResultReceiver.refMarkerWidth;
        realScale[0] = ((0.00006 * distance * distance * distance * distance) - (0.0143 * distance * distance * distance) + (1.1937 * distance * distance) - (45.223 * distance) + 726.83) * AppResultReceiver.refMarkerWidth;
        realScale[1] = ((0.00006 * distance * distance * distance * distance) - (0.0143 * distance * distance * distance) + (1.1937 * distance * distance) - (45.223 * distance) + 726.83) * AppResultReceiver.refMarkerWidth;
        return realScale;
    }

    public static double[] estimateMarkerPixelsRedmi8T(double distance) {
        if (distance < 5)
            distance = 5;
        distance = distance - 2.2; // 扣掉背夾厚度
        // 1cm at 10cm w271 h271
        // 1cm at 20cm w132 h132
        // 1cm at 30cm w83  h83
        // 1cm at 40cm w63  h62
        // 1cm at 50cm w49  h48
        // 1cm at 60cm w40  h40
        // the formula generated from excel xy scatter charts
        double[] realScale = new double[2];
        realScale[0] = ((0.0001 * distance * distance * distance * distance) - (0.0221 * distance * distance * distance) + (1.4553 * distance * distance) - (43.948 * distance) + 585.67) * AppResultReceiver.refMarkerWidth;
        realScale[1] = ((0.0001 * distance * distance * distance * distance) - (0.0221 * distance * distance * distance) + (1.4553 * distance * distance) - (43.948 * distance) + 585.67) * AppResultReceiver.refMarkerWidth;
        return realScale;
    }

//
//    /**
//     * @author YueMin
//     * @version DM1.0
//     * @Effective_Range 1~75CM
//     * @notice: must set distance first or getting zero
//     */
//    private static double getPixelsPerCentimeterWidth() {
//        if (DistanceSetting) return PixelsPerCentimeterWidth;
//        else {
//            PixelsPerCentimeterWidth = 0;
//            return PixelsPerCentimeterWidth;
//        }
//    }
//
//    /**
//     * @author YueMin
//     * @version DM1.0
//     * @Effective_Range 1~100CM
//     * @notice: must set distance first or getting zero
//     * @since 2018/02/26
//     */
//    private static double getPixelsPerCentimeterHeight() {
//        if (DistanceSetting) return PixelsPerCentimeterHeight;
//        else {
//            PixelsPerCentimeterHeight = 0;
//            return PixelsPerCentimeterHeight;
//        }
//    }
//
//    /**
//     * @author YueMin
//     * @version DM1.0
//     * @Effective_Range 1~75CM
//     * @notice: must set distance first or getting zero
//     * @since 2018/02/26
//     */
//    public static void setDistance(double distance) {
//        Distance = distance;
//        DistanceSetting = true;
//        if (Distance < 75.1 && Distance > 0.9) {
//            PixelsPerCentimeterWidth = defaultScaleWidth - ((Distance - 1) * WidthScaleCoef);
//            PixelsPerCentimeterHeight = defaultScaleHeight - ((Distance - 1) * HeightScaleCoef);
//
//            //y = -0.0017x3 + 0.2536x2 - 12.934x + 261.95
//            //		y=2086.4*POWER(I30,-1.056)
//
//            //multiPixelsPerCentimeter= -0.0017*Distance*Distance*Distance + 0.2536*Distance*Distance - 12.934*Distance + 261.95;
//
//            // HTC D610
//            //multiPixelsPerCentimeter = -0.0037 * Distance * Distance * Distance + 0.4586 * Distance * Distance - 19.473 * Distance + 326.24;
//
//            //MI3
//            multiPixelsPerCentimeter = -0.0033 * Distance * Distance * Distance + 0.4601 * Distance * Distance - 23.02 * Distance + 477.66;
//
//
//            //powPixelsPerCentimeter =2086.4* Math.pow(Distance, -1.056);
//            powPixelsPerCentimeter = 2063.6 * Math.pow(Distance, -1.052);
//
//
//        } else {
//            System.out.println("UnKnow Distance");
//        }
//
//
//    }
//
//    /**
//     * @author YueMin
//     * @version DM1.0
//     * @Effective_Range 1~75CM
//     * @notice: must set distance first or getting zero
//     * @since 2018/04/11
//     */
//    public static double[] ScaleMeasurement(Rect Region) {
//        double[] RealScale = new double[2]; //width and height
//        double s1 = getPixelsPerCentimeterWidth();
//        double s2 = getPixelsPerCentimeterHeight();
//
//        if (DistanceSetting) {
//            RealScale[0] = new BigDecimal(s1 / Region.width).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//            RealScale[1] = new BigDecimal(s2 / Region.height).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//        } else {
//            RealScale[0] = 0;
//            RealScale[1] = 0;
//        }
//        return RealScale;
//    }
//
//    public static double[] ScaleMeasurement(Rect Region, double Magnificationx, double Magnificationy) {
//        double[] RealScale = new double[2]; //width and height
//        double s1 = getPixelsPerCentimeterWidth();
//        double s2 = getPixelsPerCentimeterHeight();
//
//        if (DistanceSetting) {
//            double vr1 = Magnificationx * Region.width / s1;
//            double vr2 = Magnificationy * Region.height / s1;
//            RealScale[0] = new BigDecimal(vr1).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//            RealScale[1] = new BigDecimal(vr2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//        } else {
//            RealScale[0] = 0;
//            RealScale[1] = 0;
//        }
//        return RealScale;
//    }
//
//    public static double[] MeasurementRealDistancePOW(Rect Region, double Magnificationx, double Magnificationy) {
//        double[] RealScale = new double[2]; //width and height
//        double s = getPowPixelsPerCentimeter();
//
//
//        if (DistanceSetting) {
//            double vr1 = Magnificationx * Region.width / s;
//            double vr2 = Magnificationy * Region.height / s;
//            RealScale[0] = new BigDecimal(vr1).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//            RealScale[1] = new BigDecimal(vr2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//        } else {
//            RealScale[0] = 0;
//            RealScale[1] = 0;
//        }
//        return RealScale;
//    }
//
//    public static double[] MeasurementRealDistanceMULTI(Rect Region, double Magnificationx, double Magnificationy) {
//        double[] RealScale = new double[2]; //width and height
//        double s = getMultiPixelsPerCentimeter();
//
//
//        if (DistanceSetting) {
//            double vr1 = Magnificationx * Region.width / s;
//            double vr2 = Magnificationy * Region.height / s;
//            RealScale[0] = new BigDecimal(vr1).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//            RealScale[1] = new BigDecimal(vr2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//        } else {
//            RealScale[0] = 0;
//            RealScale[1] = 0;
//        }
//        return RealScale;
//    }
//
//    public static double[] MeasurementRealDistanceMULTI(RotatedRect rotatedRect, double Magnificationx, double Magnificationy) {
//        double[] RealScale = new double[2]; //width and height
//        double s = getMultiPixelsPerCentimeter();
//
//
//        if (DistanceSetting) {
//            double vr1 = Magnificationx * rotatedRect.size.width / s;
//            double vr2 = Magnificationy * rotatedRect.size.height / s;
//            RealScale[0] = new BigDecimal(vr1).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//            RealScale[1] = new BigDecimal(vr2).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//        } else {
//            RealScale[0] = 0;
//            RealScale[1] = 0;
//        }
//        return RealScale;
//    }

}