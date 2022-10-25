package org.itri.woundcamrtc.analytics;

import android.content.Context;
import android.util.Log;

import org.itri.woundcamrtc.R;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ContourHelper {


    public static org.opencv.core.Rect BoundingRectangle(MatOfPoint WoundLoc) {
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

    //    private  float[] bitmapToFloatArray(Bitmap bitmap){
//        int height = bitmap.getHeight();
//        int width = bitmap.getWidth();
//        float[] result = new float[height * width];
//
//        int k = 0;
//        for (int j = 0; j < height; j++) {
//            for (int i = 0; i < width; i++) {
//                int argb = bitmap.getPixel(i, j);
//                // 由于是灰度图，所以r,g,b分量是相等的。
//                int r = Color.red(argb);
//                result[k++] = r / 255.0f;
//            }
//        }
//        return result;
//    }
//
//    private void drawRotatedRectangle(Mat  rotatedRectangleImage, RotatedRect rotatedRectangle)
//    {
//        // We take the edges that OpenCV calculated for us
//        Point[] vertices= new Point[4];
//        rotatedRectangle.points(vertices);
//
//        // Convert them so we can use them in a fillConvexPoly
//        MatOfPoint matOfPoint=new MatOfPoint();
//        matOfPoint.fromArray(vertices[0],vertices[1],vertices[2],vertices[3]);
//
//        Mat img = Highgui.imread(fileName);
//
//        Size sz = new Size(img.width() / (GRABCAT_DOWNSAMPLE_RATE), img.height() / (GRABCAT_DOWNSAMPLE_RATE));
//        Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_CUBIC);
//
//        // Now we can fill the rotated rectangle with our specified color
//        //Core.fillConvexPoly(img, matOfPoint, new Scalar(255,100,100), 1, 5);
//        Core.line(img,vertices[0],vertices[1],new Scalar(255,0,0));
//        Core.line(img,vertices[1],vertices[2],new Scalar(255,0,0));
//        Core.line(img,vertices[2],vertices[3],new Scalar(255,0,0));
//        Core.line(img,vertices[3],vertices[0],new Scalar(255,0,0));
//
//        //Highgui.imwrite(outFilePath + "analytics_2.jpg", img);
//
//        matOfPoint.release();
//        matOfPoint =  null;
//    }
//
//

    public static MatOfPoint scaleMatOfPoints(MatOfPoint original, double scale) {
        List<Point> originalPoints = original.toList();
        List<Point> resultPoints = new ArrayList<Point>();

        for (Point point : originalPoints) {
            resultPoints.add(new Point(point.x * scale, point.y * scale));
        }

        MatOfPoint result = new MatOfPoint();
        result.fromList(resultPoints);
        return result;
    }

    public static String pointsToString(MatOfPoint rectangle) {
        StringBuilder builder = new StringBuilder();

        List<Point> points = rectangle.toList();
        Iterator<Point> iterator = points.iterator();

        while (iterator.hasNext()) {
            boolean isNotLast = iterator.hasNext();
            Point point = iterator.next();
            builder.append(point.toString().replace(",", " ").replace("{", "").replace("}", ""));
            if (isNotLast) {
                builder.append(",");
            }
        }

        return builder.toString();
    }
    //    void contourOffset(const std::vector<cv::Point>& src, std::vector<cv::Point>& dst, const cv::Point& offset) {
//        dst.clear();
//        dst.resize(src.size());
//        for (int j = 0; j < src.size(); j++)
//            dst[j] = src[j] + offset;
//
//    }
//    void scaleContour(const std::vector<cv::Point>& src, std::vector<cv::Point>& dst, float scale)
//    {
//        cv::Rect rct = cv::boundingRect(src);
//
//        std::vector<cv::Point> dc_contour;
//        cv::Point rct_offset(-rct.tl().x, -rct.tl().y);
//        contourOffset(src, dc_contour, rct_offset);
//
//        std::vector<cv::Point> dc_contour_scale(dc_contour.size());
//
//        for (int i = 0; i < dc_contour.size(); i++)
//            dc_contour_scale[i] = dc_contour[i] * scale;
//
//        cv::Rect rct_scale = cv::boundingRect(dc_contour_scale);
//
//        cv::Point offset((rct.width - rct_scale.width) / 2, (rct.height - rct_scale.height) / 2);
//        offset -= rct_offset;
//        dst.clear();
//        dst.resize(dc_contour_scale.size());
//        for (int i = 0; i < dc_contour_scale.size(); i++)
//            dst[i] = dc_contour_scale[i] + offset;
//    }
//
//    void scaleContours(const std::vector<std::vector<cv::Point>>& src, std::vector<std::vector<cv::Point>>& dst, float scale)
//    {
//        dst.clear();
//        dst.resize(src.size());
//        for (int i = 0; i < src.size(); i++)
//            scaleContour(src[i], dst[i], scale);
//    }
//
//    public boolean superres_init(Context context){
//        try {
//            // load model file from application resources
//            InputStream is = getResources().openRawResource(R.raw.fsrcnn_x4);
//            File modelDir = context.getDir("models", Context.MODE_PRIVATE);
//            File mModelFile = new File(modelDir, "fsrcnn_x4.pb");
//            java.io.FileOutputStream os = new java.io.FileOutputStream(mModelFile);
//
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//            while ((bytesRead = is.read(buffer)) != -1) {
//                os.write(buffer, 0, bytesRead);
//            }
//            is.close();
//            os.close();
//
//            //mModelFile.delete();
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, "Failed to load model. Exception thrown: " + e);
//            return false;
//        }
//    }
//
//    public boolean superres_processing(Context context, Mat sSrc, Mat sDst){
//        //https://bleedai.com/super-resolution-going-from-3x-to-8x-resolution-in-opencv/
//        try {
//            File modelDir = context.getDir("models", Context.MODE_PRIVATE);
//            File mModelFile = new File(modelDir, "fsrcnn_x4.pb");
//            org.bytedeco.opencv.opencv_dnn_superres.DnnSuperResImpl dsr = new org.bytedeco.opencv.opencv_dnn_superres.DnnSuperResImpl();
//            dsr.readModel(mModelFile.getAbsolutePath());
//            dsr.setModel("fsrcnn", 4);
//            org.bytedeco.opencv.opencv_core.Mat srcMat = new org.bytedeco.opencv.opencv_core.Mat() { { address = sSrc.getNativeObjAddr(); } };
//            org.bytedeco.opencv.opencv_core.Mat dstMat = new org.bytedeco.opencv.opencv_core.Mat() { { address = sDst.getNativeObjAddr(); } };
//            dsr.upsample(srcMat, dstMat);
//            //mModelFile.delete();
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e(TAG, "Failed to process model. Exception thrown: " + e);
//            return false;
//        }
//    }
}
