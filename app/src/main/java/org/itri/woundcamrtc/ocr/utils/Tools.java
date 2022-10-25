package org.itri.woundcamrtc.ocr.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;


import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.ocr.camera.CameraConfigurationUtils;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap preRotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.preRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
    }

    public enum ScalingLogic {
        CROP, FIT
    }

    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                          ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return srcWidth / dstWidth;
            } else {
                return srcHeight / dstHeight;
            }
        } else {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return srcHeight / dstHeight;
            } else {
                return srcWidth / dstWidth;
            }
        }
    }

    public static Bitmap decodeByteArray(byte[] bytes, int dstWidth, int dstHeight,
                                         ScalingLogic scalingLogic) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth,
                dstHeight, scalingLogic);
        Bitmap unscaledBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        return unscaledBitmap;
    }

    public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.CROP) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                final int srcRectWidth = (int) (srcHeight * dstAspect);
                final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
                return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
            } else {
                final int srcRectHeight = (int) (srcWidth / dstAspect);
                final int scrRectTop = (int) (srcHeight - srcRectHeight) / 2;
                return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
            }
        } else {
            return new Rect(0, 0, srcWidth, srcHeight);
        }
    }

    public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return new Rect(0, 0, dstWidth, (int) (dstWidth / srcAspect));
            } else {
                return new Rect(0, 0, (int) (dstHeight * srcAspect), dstHeight);
            }
        } else {
            return new Rect(0, 0, dstWidth, dstHeight);
        }
    }

    public static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight,
                                            ScalingLogic scalingLogic) {
        Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                dstWidth, dstHeight, scalingLogic);
        Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                dstWidth, dstHeight, scalingLogic);
        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    public static Bitmap getFocusedBitmap(Context context, Camera camera, byte[] data, Rect box) {
        Point ScrRes = ScreenUtils.getScreenResolution(context);
        Point CamRes = CameraConfigurationUtils.findBestPreviewSizeValue(camera.getParameters(), ScrRes);

//        int SW = ScrRes.x;
//        int SH = ScrRes.y;
//
//        int RW = (int)(box.width()*0.7);
//        int RH = box.height();
//        int RL = (int)(box.left*1.5);
//        int RT = (int)(box.top*1.06);
//
//
//        float RSW = (float) (RW * Math.pow(SW, -1));
//        float RSH = (float) (RH * Math.pow(SH, -1));
//
//        float RSL = (float) (RL * Math.pow(SW, -1));
//        float RST = (float) (RT * Math.pow(SH, -1));

        //float k = 0.5f;
//        float k = 1.0f;
//
//
//        int CW = CamRes.x;
//        int CH = CamRes.y;
//
//        int X = (int) (k * CW);
//        int Y = (int) (k * CH);


        int X = CamRes.x;
        int Y = CamRes.y;

        Bitmap unscaledBitmap = Tools.decodeByteArray(data, X, Y, ScalingLogic.CROP);
        Bitmap bmp = Tools.createScaledBitmap(unscaledBitmap, X, Y, ScalingLogic.CROP);
        unscaledBitmap.recycle();


        if (AppResultReceiver.IS_FOR_MIIS_MPDA)
            bmp = Tools.rotateBitmap(bmp, 270);
        else {
            if (X > Y) {
                bmp = Tools.rotateBitmap(bmp, 90);
            }
        }

//        int BW = bmp.getWidth();
//        int BH = bmp.getHeight();
//
//        int RBL = (int) (RSL * BW);
//        int RBT = (int) (RST * BH);
//
//        int RBW = (int) (RSW * BW);
//        int RBH = (int) (RSH * BH);

//        Bitmap res = Bitmap.createBitmap(bmp, RBL, RBT, RBW, RBH);
        int RW = (int) (bmp.getWidth() * box.width() / ScrRes.x);
        int RH = (int) (bmp.getHeight() * box.height() / ScrRes.y);
        int RL = (int) (bmp.getWidth() * box.left / ScrRes.x);
        int RT = (int) (bmp.getHeight() * box.top / ScrRes.y);


        Bitmap res = Bitmap.createBitmap(bmp, RL, RT, RW, RH);
        bmp.recycle();

        return res;
    }

    public static Bitmap getFocusedBitmap2(Context context, Camera camera, byte[] data, Rect box) {

        Point ScrRes = ScreenUtils.getScreenResolution(context);
        Point CamRes = CameraConfigurationUtils.findBestPreviewSizeValue(camera.getParameters(), ScrRes);
        Camera.Parameters parameters = camera.getParameters();
        int format = parameters.getPreviewFormat();
        //YUV formats require more conversion
        if (format == ImageFormat.NV21 || format == ImageFormat.YUY2 || format == ImageFormat.NV16) {
            int w = parameters.getPreviewSize().width;
            int h = parameters.getPreviewSize().height;
            Log.v("ocr preview寬", String.valueOf(w));
            Log.v("ocr preview高", String.valueOf(h));
            Log.v("ocr roi top", String.valueOf(box.top));
            Log.v("ocr roi bottom", String.valueOf(box.bottom));
            // Get the YuV image
            YuvImage yuv_image = new YuvImage(data, format, w, h, null);
            // Convert YuV to Jpeg
            Rect rect = new Rect(0, 0, w, h);
            ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
            yuv_image.compressToJpeg(rect, 100, output_stream);
            byte[] byt = output_stream.toByteArray();

            Bitmap bmp = BitmapFactory.decodeByteArray(byt, 0, byt.length);

            if (AppResultReceiver.IS_FOR_MIIS_MPDA)
                bmp = Tools.rotateBitmap(bmp, 270);
            else {
                if (bmp.getWidth() > bmp.getHeight()) {
                    bmp = Tools.rotateBitmap(bmp, 90);
                }
            }

            int RW = (int) (bmp.getWidth() * box.width() / ScrRes.x);
            int RH = (int) (bmp.getHeight() * box.height() / ScrRes.y);
            int RL = (int) (bmp.getWidth() * box.left / ScrRes.x);
            int RT = (int) (bmp.getHeight() * (box.top * 1.0314) / ScrRes.y);  //1.0314 for MPD100

            Bitmap res = Bitmap.createBitmap(bmp, RL, RT, RW, RH);
            bmp.recycle();

            return res;
        }
        return null;
    }

//    private static Pattern pattern = Pattern.compile("(1|861)\\d{10}$*");
//
//    private static StringBuilder bf = new StringBuilder();
//
//    public static String getTelNum(String sParam) {
//        if (TextUtils.isEmpty(sParam)) {
//            return "";
//        }
//
//        Matcher matcher = pattern.matcher(sParam.trim());
//        bf.delete(0, bf.length());
//
//        while (matcher.find()) {
//            bf.append(matcher.group()).append("\n");
//        }
//        int len = bf.length();
//        if (len > 0) {
//            bf.deleteCharAt(len - 1);
//        }
//        return bf.toString();
//    }
}
