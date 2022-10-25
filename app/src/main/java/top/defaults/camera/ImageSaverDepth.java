package top.defaults.camera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.Image;
import android.media.ImageReader;
import android.os.SystemClock;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.TextureView;

import org.itri.woundcamrtc.AppResultReceiver;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import static android.content.ContentValues.TAG;
import static org.itri.woundcamrtc.AppResultReceiver.isUvcDeviceOK;
import static org.opencv.core.CvType.CV_16UC1;
import static org.opencv.core.CvType.CV_8U;

class ImageSaverDepth implements Runnable {
    private byte[] bytes = null;
    private Camera2Photographer camera2Photographer;
    public static int vmin = 240 * 16; //unit mm, shift 4bits
    public static int vmax = 700 * 16; //unit mm, shift 4bits
    public static Mat invert = new Mat(160, 120, CvType.CV_8UC1, new Scalar(255));
    private int minHist = 20 * 160;
    private int maxHist = 70 * 160;
    private MatOfInt histSize = new MatOfInt(maxHist - minHist);
    private MatOfFloat ranges = new MatOfFloat(minHist, maxHist);

    ImageSaverDepth(byte[] bytes, Camera2Photographer camera2Photographer) {//}, String filePath, TextureView textureView, Lock locker) {
        try {
            this.bytes = bytes;
            this.camera2Photographer = camera2Photographer;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            try {
//                Log.v(TAG," camera2Photographer.doCaptureDepth"+ camera2Photographer.doCaptureDepth);
                if (camera2Photographer != null && camera2Photographer.doCaptureDepth && bytes != null) {
                    //Log.e(TAG, "Time 3d onCapturing: " + AppResultReceiver.lastDepthSnapshotTimems);
                    AppResultReceiver.lastDepthSnapshotBytes = bytes;
                    long timeoffset = AppResultReceiver.lastDepthSnapshotTimems - AppResultReceiver.lastPicSnapshotTimems;
                    if (AppResultReceiver.lastPicSnapshotTimems != 0 && (Math.abs(timeoffset) < 60 || (timeoffset > 100))) {
                        Log.e(TAG, "3D depth image captured: " + SystemClock.uptimeMillis());
                        saveToFile(AppResultReceiver.lastDepthSnapshotBytes);

                        Log.e(TAG, "Time 3d saveToFile: " + AppResultReceiver.lastDepthSnapshotTimems);
                        AppResultReceiver.lastDepthSnapshotBytes = null;
                        AppResultReceiver.lastDepthSnapshotTimems = 0;
                        camera2Photographer.doCaptureDepth = false;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            drawToView(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bytes = null;
            try {
                if (camera2Photographer.semaphore != null) {
                    synchronized (camera2Photographer.semaphore) {
                        camera2Photographer.semaphore.release();
                    }
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            camera2Photographer = null;
        }
    }

    private void drawToView(byte[] bytes) {
        Mat matResized = new Mat();
        Mat matFinal = new Mat();
        Mat matSrc = new Mat(800, 1280, CV_16UC1);

        // try set final modify with data render crash
        final short shorts[] = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        matSrc.put(0, 0, shorts);
        final Bitmap mBitmap = Bitmap.createBitmap(120, 160, Bitmap.Config.ARGB_8888);

        try {
            Imgproc.resize(matSrc, matResized, new org.opencv.core.Size(160, 120), 0, 0, Imgproc.INTER_LINEAR);
            rotateMatCW(matResized, matResized, -90);

//            Mat matHistOfDepth = new Mat();
//            Core.MinMaxLocResult histMinMax;
//
//
//            List<Mat> listMatDepth = new ArrayList<Mat>();
//            listMatDepth.add(matResized);
//            Imgproc.calcHist(listMatDepth, new MatOfInt(0), new Mat(), matHistOfDepth, histSize, ranges, false);
//
//            int firstNonZeroRow = (maxHist - minHist) - 1;
//            for (int row = 0; row < matHistOfDepth.rows(); row++) {
//                double val = matHistOfDepth.get(row, 0)[0];
//                if (val > 0) {
//                    ffirstNonZeroRow = row;
//                    break;
//                }
//            }
//
//            histMinMax = Core.minMaxLoc(matHistOfDepth);
//            int proximityDist = 0;
//            if (histMinMax.maxVal > 10) {
//                proximityDist = ((int) ((histMinMax.maxLoc.y + minHist) / 160) / 5) * 5;
//            }
//
//            int proximityDist = ((int) ((firstNonZeroRow + 1 + minHist) / 160) / 5) * 5;
//            listMatDepth = null;
//            matHistOfDepth.release();


//
//            int proximityDist = 0;
//            for (int row = 0; row < matHistOfDepth.rows(); row++) {
//                double val = matHistOfDepth.get(row, 0)[0];
//                if (val>0) {
//                    proximityDist = ((int) (row+minHist / 160.0) / 5) * 5;
//                    break;
//                }
//            }
//            listMatDepth = null;
//            matHistOfDepth.release();

            double alpha = 255.0 / (vmax - vmin);
            matResized.convertTo(matResized, CV_8U, alpha, -vmin * alpha);
            Core.subtract(invert, matResized, matResized);
            Imgproc.threshold(matResized, matResized, 250, 0, Imgproc.THRESH_TOZERO_INV);
            Imgproc.medianBlur(matResized, matResized, 3);

            org.opencv.android.Utils.matToBitmap(matResized, mBitmap);
            AppResultReceiver.mMainActivity.updateTextureView(camera2Photographer.textureView, mBitmap);
//            Canvas canvas = null;
//            try {
//                if (camera2Photographer.textureView!=null)
//                    canvas = camera2Photographer.textureView.lockCanvas();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            try {
//                if (mBitmap!=null && canvas!=null)
//                    canvas.drawBitmap(mBitmap, 0, 0, null);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            try {
//                if (camera2Photographer.textureView!=null && canvas!=null)
//                    camera2Photographer.textureView.unlockCanvasAndPost(canvas);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }

                if(AppResultReceiver.isUvcDeviceOK){

                    Mat checkImage = new Mat();
                    Imgproc.resize(matSrc, checkImage, new org.opencv.core.Size(16, 12), 0, 0, Imgproc.INTER_LINEAR);
                    int nonzero = Core.countNonZero(checkImage);
                    Log.v(TAG,"nonzero"+nonzero);
                    if(nonzero==0){
                        AppResultReceiver.nonzero=false;
                    }
                    checkImage.release();
                    AppResultReceiver.isUvcDeviceOK=false;
                }





            double center_value = matSrc.get((int) (matSrc.rows() * AppResultReceiver.touchPointXp), (int) (matSrc.cols() * (1.0 - AppResultReceiver.touchPointYp)))[0];

            // 可能存在值為0的黑洞或<25CM突波雜訊, 所以從中央取一個ROI算平均
            Mat depthFocusROI = new Mat();
            Imgproc.resize(matSrc.submat(620, 660, 380, 430), depthFocusROI, new org.opencv.core.Size(16, 12), 0, 0, Imgproc.INTER_LINEAR);
            double avg_distance_value = 0;
            int avg_count = 0;
            int row, col;
            double pixel[] = new double[1];
            for (row = 0; row < depthFocusROI.rows(); row++) {
                for (col = 0; col < depthFocusROI.cols(); col++) {
                    //fetch values in the respective color space
                    pixel = depthFocusROI.get(row, col);
                    if (pixel[0] > 0) {
                        avg_count++;
                        avg_distance_value += pixel[0];
                    }
                }
            }
            depthFocusROI.release();
            avg_distance_value = avg_distance_value / avg_count;

            //center_value do right shift 4bits * focus length params, Unit mm
            center_value = (int) (center_value / 160.0);
            avg_distance_value = (int) (avg_distance_value / 160.0);

            if (avg_distance_value > 20.0)
                AppResultReceiver.touchPointDepthCentiMeterAvg = avg_distance_value;
            else
                AppResultReceiver.touchPointDepthCentiMeterAvg = 0.0;

            if (center_value > 20.0)
                AppResultReceiver.touchPointDepthCentiMeter = center_value;
            else
                AppResultReceiver.touchPointDepthCentiMeter = 0.0;

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                matSrc.release();
            } catch (Exception ex) {
            }
            matSrc = null;
            try {
                matResized.release();
            } catch (Exception ex) {
            }
            matResized = null;
            try {
                matFinal.release();
            } catch (Exception ex) {
            }
            matFinal = null;
//            try {
//                mBitmap.recycle();
//            } catch (Exception ex) {
//            }
            //mBitmap = null;
        }
    }

    private void rotateMatCW(Mat src, Mat dst, double deg) {
        if (deg == 270 || deg == -90) {
            // Rotate clo rotateMatCW(matResized,matResized,-90);ckwise 270 degrees
            Core.transpose(src, dst);
            Core.flip(dst, dst, 0);
        } else if (deg == 180 || deg == -180) {
            // Rotate clockwise 180 degrees
            Core.flip(src, dst, -1);
        } else if (deg == 90 || deg == -270) {
            // Rotate clockwise 90 degrees
            Core.transpose(src, dst);
            Core.flip(dst, dst, 1);
        } else if (deg == 360 || deg == 0 || deg == -360) {
            if (src != dst) {
                src.copyTo(dst);
            }
        } else {
//        cv::Point2f src_center(src.cols / 2.0F, src.rows / 2.0F);
//        cv::Mat rot_mat = getRotationMatrix2D(src_center, 360 - deg, 1.0);
//        warpAffine(src, dst, rot_mat, src.size());
        }
    }

    private void saveToFile(byte[] bytes) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(camera2Photographer.nextImageAbsolutePath);
            output.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (Exception e) {
            }
        }
    }


//    private void drawToView(ByteBuffer buffer) {
//        Mat matResized = new Mat();
//        Mat matFinal = new Mat();
//        Mat matSrc = new Mat(800, 1280, CV_16UC1, buffer);
//        Bitmap mBitmap = Bitmap.createBitmap(120, 160, Bitmap.Config.ARGB_8888);
//
//        try {
//            Imgproc.resize(matSrc, matResized, new org.opencv.core.Size(160, 120), 0, 0, Imgproc.INTER_LINEAR);
//            rotateMatCW(matResized, matResized, -90);
//
////            Imgproc.threshold(matResized, matResized, maxVal, maxVal, Imgproc.THRESH_TOZERO_INV);
////            Core.MinMaxLocResult mmlr = Core.minMaxLoc(matResized);
////            double minVal = mmlr.minVal;
////            double maxVal = mmlr.maxVal;
////            Log.d("kernoli", "min-max " + minVal +","+maxVal);
//            // remove  minimal 4 bit, and limit maximal distance, then invert gradient
////            double minVal = 16;
////            double maxVal = 5120;
//            //Core.convertScaleAbs(matResized,matResized, 255.0/(maxVal - minVal));
//            //matResized.convertTo(matResized, CV_8U, 255.0 / (maxVal - minVal));
//
////            Imgproc.threshold(matResized, matResized, vmax-1, 0, Imgproc.THRESH_TOZERO_INV);
//
//
//            double alpha = 255.0 / (vmax - vmin);
//            matResized.convertTo(matResized, CV_8U, alpha, -vmin * alpha);
//            Core.subtract(invert, matResized, matResized);
//            Imgproc.threshold(matResized, matResized, 230, 0, Imgproc.THRESH_TOZERO_INV);
//            Imgproc.medianBlur(matResized, matResized, 3);
//            //            Mat green_m = new Mat(256,1, CvType.CV_8UC1,new Scalar(255));
////            Core.subtract(  green_m,matResized,matResized);
//            //Imgproc.applyColorMap(matResized,matFinal,Imgproc.COLORMAP_HOT);
//
////                        Core.MinMaxLocResult mmlr = Core.minMaxLoc(matResized);
////                        double minVal = mmlr.minVal; // Math.min(mmlr.minVal, 0);
////                        double maxVal = mmlr.maxVal; // Math.max(mmlr.maxVal, 255);
////                            matResized.convertTo(matResized, CV_8U, 255.0/(maxVal - minVal), -minVal * 255.0/(maxVal - minVal));
//
//
//            org.opencv.android.Utils.matToBitmap(matResized, mBitmap);
//            Canvas canvas = camera2Photographer.textureView.lockCanvas();
//            if (canvas != null)
//                canvas.drawBitmap(mBitmap, 0, 0, null);
//            camera2Photographer.textureView.unlockCanvasAndPost(canvas);
//
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            try {
//                matSrc.release();
//            } catch (Exception ex) {
//            }
//            matSrc = null;
//            try {
//                matResized.release();
//            } catch (Exception ex) {
//            }
//            matResized = null;
//            try {
//                matFinal.release();
//            } catch (Exception ex) {
//            }
//            matFinal = null;
//            try {
//                mBitmap.recycle();
//            } catch (Exception ex) {
//            }
//            mBitmap = null;
//        }
//    }
}
