package org.itri.woundcamrtc.ocr.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;


import org.itri.woundcamrtc.AppResultReceiver;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RecognitionEngine {

    static final String TAG = "DBG_" + RecognitionEngine.class.getName();

    private static ArrayList<String> labellist = null;
    private static TensorFlowInferenceInterface tfInterface = null;
    private int IMG_HEIGHT = 32;
    private int IMG_CHANNEL = 1;
    private int WIDTH_STRIDE = 8;
    private int IMAGE_STD = 128;
    private int IMAGE_MEAN = 128;
    private String INPUT_IMAGE = "inputs";
    private String INPUT_IS_TRAINING = "is_training";
    //       private String OUTPUT_NODE = "SparseToDense";
    private String OUTPUT_NODE = "output";

    private RecognitionEngine() {
    }

    public static RecognitionEngine Generate() {
        return new RecognitionEngine();
    }

    public String detectText(Mat mat) {

        getLabelList(AppResultReceiver.mMainActivity, "mldata/ocr_label.txt");
        if (tfInterface == null) {
            tfInterface = new TensorFlowInferenceInterface(AppResultReceiver.mMainActivity.getAssets(), "mldata/ocr_crnn.pb");
        }

//        Mat tmp = new Mat();
//        Utils.bitmapToMat(bitmap, tmp);
        // bitmap.recycle();
        String result = recoginze(AppResultReceiver.mMainActivity, mat, null);
//        tmp.release();
//        TessDataManager.initTessTrainedData(MyApplication.sAppContext);
//        TessBaseAPI tessBaseAPI = new TessBaseAPI();
//        String path = TessDataManager.getTesseractFolder();
//        Log.d(TAG, "Tess folder: " + path);
//        tessBaseAPI.setDebug(true);
//        tessBaseAPI.init(path, "eng");
//        // 白名单
//        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
//        // 黑名单
//        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:'\"\\|~`,./<>?");
//        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
//        Log.d(TAG, "Ended initialization of TessEngine");
//        Log.d(TAG, "Running inspection on bitmap");
//        tessBaseAPI.setImage(bitmap);
//        String inspection = tessBaseAPI.getHOCRText(0);
//
//        Log.d(TAG, "Confidence values: " + tessBaseAPI.meanConfidence());
//        tessBaseAPI.end();
        System.gc();
//        return Tools.getTelNum(inspection);
        return result;
    }


    public String recoginze(Context context, Mat img, org.opencv.core.Rect roi) {
        try {

            if (roi != null)
                img = getSubMat(img, roi);
            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);

            int scaledWidth = calScaledWidth(img.width(), img.height());
            Mat resizedGrayImg = new Mat();
            Imgproc.resize(img, resizedGrayImg, new Size((double) scaledWidth, (double) IMG_HEIGHT), 0.0D, 0.0D, 3);
            //String Main_DIR = AppResultReceiver.PROJECT_NAME;
            //File file5 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Main_DIR);
            //Imgcodecs.imwrite(file5.getAbsolutePath() + "/ocr.jpg", resizedGrayImg);

            float[] pixels = getPixels(resizedGrayImg, scaledWidth);
            //TensorFlowInferenceInterface tfInterface = new TensorFlowInferenceInterface(context.getAssets(), "ocr_crnn.pb");

            //tfInterface.feed(INPUT_IMAGE, pixels, new long[]{1L, (long)IMG_HEIGHT, (long)scaledWidth, 1L});
            tfInterface.feed(INPUT_IMAGE, pixels, 1L, (long) IMG_HEIGHT, (long) scaledWidth, 1L);
            tfInterface.feed(INPUT_IS_TRAINING, new boolean[]{false}, new long[0]);
            tfInterface.run(new String[]{OUTPUT_NODE});

            int steps = (int) Math.ceil((double) scaledWidth / (double) WIDTH_STRIDE);
            int[] result = new int[steps];
            for (int i = 0; i < steps; ++i) {
                result[i] = -1;
            }

            tfInterface.fetch(OUTPUT_NODE, result);
            Log.d(TAG, "ocr " + result.toString());

            return convertToString(result);
        } catch (Exception e) {
            Log.e(TAG, "load model file error");
            e.printStackTrace();
        }
        return "";
    }


    private final float[] getPixels(Mat img, int width) {
        Bitmap bmp = Bitmap.createBitmap(width, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bmp);
        int[] intValues = new int[width * IMG_HEIGHT];
        bmp.getPixels(intValues, 0, width, 0, 0, width, IMG_HEIGHT);
        float[] floatValues = new float[width * IMG_HEIGHT * IMG_CHANNEL];
        int i = 0;
        for (int var7 = intValues.length; i < var7; ++i) {
            int p = intValues[i];
            floatValues[i] = ((float) (p >> 16 & 255) - (float) IMAGE_MEAN) / (float) IMAGE_STD;
        }

        return floatValues;
    }

    public final int calScaledWidth(int width, int height) {
        float scale = (float) IMG_HEIGHT / (float) height;
        return (int) ((float) width * scale);
    }

    public final Mat getSubMat(Mat parentMat, org.opencv.core.Rect rect) {
        Mat mat;
        try {
            mat = new Mat(parentMat, rect);
        } catch (Exception e) {
            mat = parentMat;
        }

        return mat;
    }

    public String convertToString(int[] result) {
        String res = "";
        for (int i = 0; i < result.length; ++i) {
            if (result[i] == -1)
                break;
            else if (result[i] > 25 && result[i] < 62)
                res = res + labellist.get(result[i]);
        }
        ;
        return res;
    }

    public void getLabelList(Context context, String fileName) {
        if (labellist == null) {
            labellist = new ArrayList<String>(50);
            InputStream inStream = null;
            try {
                inStream = context.getAssets().open(fileName);
                InputStreamReader reader = null;
                try {
                    reader = new InputStreamReader(inStream);
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(reader);
                        String line;
                        while ((line = br.readLine()) != null) {
                            labellist.add(line);
                        }
                    } catch (IOException e) {
                    } finally {
                        try {
                            if (br != null) {
                                br.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
            } finally {
                try {
                    if (inStream != null) {
                        inStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
