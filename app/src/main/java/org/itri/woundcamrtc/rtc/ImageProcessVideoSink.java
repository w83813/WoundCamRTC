package org.itri.woundcamrtc.rtc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ImageProcessVideoSink implements VideoSink {
    private OnFrameListener mOnFrameListener = null;

    public interface OnFrameListener {
        void onFrame(VideoFrame videoFrame);
    }


    public void setOnFrameListener(OnFrameListener l) {
        mOnFrameListener = l;
    }

    @Override
    public void onFrame(VideoFrame videoFrame) {
        //Log.d(TAG, "Got Video Frame. rot=" + videoFrame.getRotation());
        if (mOnFrameListener != null) {
            mOnFrameListener.onFrame(videoFrame);
        }

    }

    public static Bitmap toBitmap(VideoFrame videoFrame) {
        VideoFrame.I420Buffer i420Buffer = videoFrame.getBuffer().toI420();
        final int width = i420Buffer.getWidth();
        final int height = i420Buffer.getHeight();
        //convert to nv21, this is the same as byte[] from onPreviewCallback
        byte[] nv21Data = createNV21Data(i420Buffer);

        //let's test the conversion by converting the NV21 data to jpg and showing it in a bitmap.
        YuvImage yuvImage = new YuvImage(nv21Data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap snapshotBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//            Mat tmp = new Mat (snapshotBitmap.getWidth(), snapshotBitmap.getHeight(), CvType.CV_8UC3);
//            Utils.bitmapToMat(snapshotBitmap, tmp);
//            //Imgproc.cvtColor(tmp, tmp, CvType.CV_8UC3);
//            String Main_DIR = AppResultReceiver.PROJECT_NAME;
//            File file5 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Main_DIR);
//            Imgcodecs.imwrite(file5.getAbsolutePath()+"/GLsnapshut.jpg",tmp);
        return snapshotBitmap;
    }

    public static YuvImage toYuvImage(VideoFrame videoFrame) {
        VideoFrame.I420Buffer i420Buffer = videoFrame.getBuffer().toI420();
        final int width = i420Buffer.getWidth();
        final int height = i420Buffer.getHeight();
        //convert to nv21, this is the same as byte[] from onPreviewCallback
        byte[] nv21Data = createNV21Data(i420Buffer);

        //let's test the conversion by converting the NV21 data to jpg and showing it in a bitmap.
        YuvImage yuvImage = new YuvImage(nv21Data, ImageFormat.NV21, width, height, null);
        return yuvImage;
    }

    public static byte[] createNV21Data(VideoFrame.I420Buffer i420Buffer) {

        //final byte[] dataArrY = dataY.array();

        final int width = i420Buffer.getWidth();
        final int height = i420Buffer.getHeight();
        final int chromaStride = width;
        final int chromaWidth = (width + 1) / 2;
        final int chromaHeight = (height + 1) / 2;
        final int ySize = width * height;
        final ByteBuffer nv21Buffer = ByteBuffer.allocateDirect(ySize + chromaStride * chromaHeight);
        // We don't care what the array offset is since we only want an array that is direct.
        @SuppressWarnings("ByteBufferBackingArray") final byte[] nv21Data = nv21Buffer.array();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final byte yValue = i420Buffer.getDataY().get(y * i420Buffer.getStrideY() + x);
                nv21Data[y * width + x] = yValue;
            }
        }
        for (int y = 0; y < chromaHeight; ++y) {
            for (int x = 0; x < chromaWidth; ++x) {
                final byte uValue = i420Buffer.getDataU().get(y * i420Buffer.getStrideU() + x);
                final byte vValue = i420Buffer.getDataV().get(y * i420Buffer.getStrideV() + x);
                nv21Data[ySize + y * chromaStride + 2 * x + 0] = vValue;
                nv21Data[ySize + y * chromaStride + 2 * x + 1] = uValue;
            }
        }
        return nv21Data;
    }

    /**
     * Convert a byte array to a direct ByteBuffer.
     */
    private ByteBuffer toByteBuffer(int[] array) {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(array.length);
        buffer.put(toByteArray(array));
        buffer.rewind();
        return buffer;
    }


    /**
     * Convert an int array to a byte array and make sure the values are within the range [0, 255].
     */
    private byte[] toByteArray(int[] array) {
        final byte[] res = new byte[array.length];
        for (int i = 0; i < array.length; ++i) {
            final int value = array[i];
            res[i] = (byte) value;
        }
        return res;
    }


    public static Mat ba2Mat(byte[] ba) {
        Mat mat = Imgcodecs.imdecode(new MatOfByte(ba), CvType.CV_8UC3);
        return mat;
    }
}
