package top.defaults.camera;

import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

class ImageSaverRaw implements Runnable {

    private String TAG = ImageSaverRaw.class.getSimpleName();
    private Image image;
    private final String filePath;

    ImageSaverRaw(Image image, String filePath) {
        this.image = image;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            int isNIROrDepth = (int) (buffer.get(1280 * 800 * 2 - 1));
            switch (isNIROrDepth) {
                case 2:
                    Log.d(TAG, "=== is Depth");
                    break;
                case 1:
                    Log.d(TAG, "=== is NIR");
                default:
                    Log.d(TAG, "=== is unknown");
                    bytes = null;
                    buffer.clear();
                    buffer = null;
                    return;
            }

            FileOutputStream output = null;
            try {
                output = new FileOutputStream(filePath);
                output.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                image = null;
            } catch (Exception ee) {
            }
            image.close();
        }
    }
}
