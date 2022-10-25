package com.shlll.libusbcamera;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.utils.HandlerThreadHandler;
import com.serenegiant.widget.UVCCameraTextureView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class USBCameraHelper {
    private String TAG = getClass().getSimpleName() + ".uvc";
    private static Semaphore mSemaphoreRunning = new Semaphore(1);
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    public TextureView mUVCCameraView;
    //    private FrameLayout mFrameLayout;
    private Surface mPreviewSurface;
    private Context mContext;
    private OnCameraButtonListener mButtonListener = null;
    private IFrameCallback mIFrameCallback = null;
    private Vibrator mVibraotor = null;

    public Semaphore semaphore = null;
    public boolean doCapture = false;
    public String saveFilePathName = "";

    private static int mUVCDeviceFilterXmlId = 0;

    /**
     * Event Handler
     */
    public Handler mWorkerHandler = null;
    private long mWorkerThreadID = -1;

    private USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = null;

    public void capturePicture(int index, String toString, Object o) {
        doCapture = true;
        saveFilePathName = toString;
    }

    public boolean isCameraOpened(int uvcCameraIndex) {
        return checkCameraOpened();
    }

    public interface OnCameraButtonListener {
        public void onCameraButton();
    }

    public void setOnCameraButtonListener(OnCameraButtonListener listener) {
        mButtonListener = listener;
    }

    public USBCameraHelper(final Context context, final int UVCDeviceFilterXmlId, final TextureView cameraView, final IFrameCallback iFrameCallback) {
        Log.d(TAG, "USBCameraHelper.Constructor");
        if (mWorkerHandler == null) {
//            if (mSync == null)
//                mSync = new Object();
            semaphore = new Semaphore(1);
            mWorkerHandler = HandlerThreadHandler.createHandler(TAG);
            mWorkerThreadID = mWorkerHandler.getLooper().getThread().getId();
            mIFrameCallback = iFrameCallback;
            mUVCCameraView = cameraView;

            mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
                @Override
                public void onAttach(final UsbDevice device) {
                    try {
//                        if (mSemaphoreRunning.tryAcquire(10, TimeUnit.SECONDS)) {
                        if(!checkCameraOpened()){
                            showShortMsg("mContext.getResources().getString(R.string.msg_usb_device_attached)");
                            requestPermission(mUVCDeviceFilterXmlId);
                        }
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
                    try {
                        if (device.getProductId() == 38208 || device.getVendorId() == 1423)
                            return;



//                        releaseCamera();
//                                                    queueEvent(new Runnable() {
//                                                        @Override
//                                                        public void run() {


                        final UVCCamera camera = new UVCCamera();
                        try {
                            camera.open(ctrlBlock);
                            camera.setButtonCallback(new IButtonCallback() {
                                @Override
                                public void onButton(final int button, final int state) {
                                    ((Activity) mContext).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mButtonListener != null)
                                                mButtonListener.onCameraButton();
                                        }
                                    });
                                }
                            });
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            //camera.destroy();
                            return;
                        }

                        if (mPreviewSurface != null) {
                            mPreviewSurface.release();
                            mPreviewSurface = null;
                        }
                        //                                try {
                        //                                    camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
                        //                                } catch (final IllegalArgumentException e) {
                        //                                    // fallback to YUV mode
                        try {
                            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            //camera.destroy();
                            return;
                        }
                        //                                }

                        if (mIFrameCallback != null) {
                            camera.setFrameCallback(mIFrameCallback, 11);
                        } else {
                            final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                            if (st != null) {
                                mPreviewSurface = new Surface(st);
                                camera.setPreviewDisplay(mPreviewSurface);
                            }
                        }

                        //                        try {
                        //                            Thread.sleep(500);
                        //                        } catch (Exception e) {
                        //                            e.printStackTrace();
                        //                        }
                        //                        synchronized (mSync) {
                        mUVCCamera = camera;
                        camera.startPreview();
                        mSemaphoreRunning.release();
                        //                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                        }, 0);
//                    }
                }

                @Override
                public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
                    try {
//                        if (mSemaphoreRunning.tryAcquire(10, TimeUnit.SECONDS)) {
                        releaseCamera();
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDettach(final UsbDevice device) {
                    showShortMsg("mContext.getResources().getString(R.string.msg_usb_device_detached)");
                }

                @Override
                public void onCancel(final UsbDevice device) {
                }
            };


        }

        mContext = context;
        mUVCDeviceFilterXmlId = UVCDeviceFilterXmlId;
//        mFrameLayout = frameLayout;
        mUVCCameraView = cameraView;
        mUSBMonitor = new USBMonitor(mContext, mOnDeviceConnectListener);
        mVibraotor = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

//        final ViewTreeObserver vto = mFrameLayout.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                float aspect_ratio = UVCCamera.DEFAULT_PREVIEW_WIDTH / (float)UVCCamera.DEFAULT_PREVIEW_HEIGHT;
//                int layout_width  = mFrameLayout.getMeasuredWidth();
//                int layout_height = mFrameLayout.getMeasuredHeight();
//                mUVCCameraView.setAspectRatio(aspect_ratio, layout_width, layout_height);
//
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
//                    mFrameLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                else
//                    mFrameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//        });
    }

    public void rightRotate() {
        if (!checkCameraOpened())
            return;
//        mUVCCameraView.rightRotate();
    }

    public void leftRotate() {
        if (!checkCameraOpened())
            return;
//        mUVCCameraView.leftRotate();
    }

    public void toggleMirror() {
        if (!checkCameraOpened())
            return;
//        mUVCCameraView.toggleMirror();
    }

    public void setPosition(UVCCameraTextureView.TransState pos) {
        if (!checkCameraOpened())
            return;
//        mUVCCameraView.setPosition(pos);
    }

    /**
     * Get current USB device list.
     *
     * @return List of UsbDeice object.
     */
    public List<UsbDevice> getDeviceList(int deviceFilterXmlId) {
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(mContext, deviceFilterXmlId);
        final List<UsbDevice> deviceList = mUSBMonitor.getDeviceList(filter);
        return deviceList;
    }

    public synchronized void register() {
        try {
            if (mSemaphoreRunning.tryAcquire(1, TimeUnit.SECONDS)) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mUSBMonitor != null)
                    mUSBMonitor.register();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void start() {
//        try {
//            if (mUVCCamera != null) {
//                mUVCCamera.startPreview();
//            }
//            mSemaphoreRunning.release();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    public synchronized void stop() {
        try {
            if (mSemaphoreRunning.tryAcquire(1, TimeUnit.SECONDS)) {
                try {
                    if (mUVCCamera != null) {
                        mUVCCamera.stopPreview();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void unregister() {
        try {
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void destory() {
        if (mUSBMonitor != null)
            mUSBMonitor.destroy();
        mUSBMonitor = null;
        releaseCamera();
        mUVCCameraView = null;
        mOnDeviceConnectListener = null;
        mIFrameCallback = null;

        mWorkerHandler.removeCallbacksAndMessages(null);
        mWorkerHandler.getLooper().quitSafely();
        mWorkerHandler.getLooper().getThread().interrupt();
        mWorkerHandler = null;
        //semaphore = null;
        mSemaphoreRunning.release();
        //mSync = null;
        Log.d(TAG, "USBCameraHelper.destory");
    }

    /**
     * Save current picture to system and add picture to gallery.
     */
    public void saveCapturePicture() {
        if (!checkCameraOpened())
            return;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        timeStamp = null;
        OutputStream fos = null;
        Uri imageUri = null;
        final ContentResolver resolver = mContext.getContentResolver();

        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                String storagePath = Environment.DIRECTORY_PICTURES + File.separator + "USBCamera";
//                ContentValues contentValues = new ContentValues();
//                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName);
//                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
//                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, storagePath);
//
//                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//                fos = resolver.openOutputStream(imageUri);
//                Bitmap bitmap = mUVCCameraView.getBitmap();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                fos.close();
//            } else {
            final String storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "USBCamera";
            File storageDir = new File(storagePath);
            if (!storageDir.exists()) {
                storageDir.mkdir();
            }
            storageDir = null;

            File image = new File(storagePath, imageFileName);
            fos = new FileOutputStream(image);
            Bitmap bitmap = mUVCCameraView.getBitmap();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            fos = null;
            galleryAddPic(image.toString());
            image = null;
//            }

            if (mVibraotor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    mVibraotor.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                else
                    mVibraotor.vibrate(100);
            }
            showShortMsg("mContext.getResources().getString(R.string.msg_capturesaved)");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (imageUri != null) {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(imageUri, null, null);
            }
            e.printStackTrace();
        }
    }

    private boolean checkCameraOpened() {
        if (mUVCCamera == null) {
            showShortMsg("mContext.getResources().getString(R.string.msg_camera_open_fail)");
            return false;
        }

        return true;
    }

    /**
     * Request USB device permission.
     */
    private void requestPermission(int deviceFilterXmlId) {
        try {
            if (mUSBMonitor == null) return;
            final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(mContext, deviceFilterXmlId);
            final List<UsbDevice> deviceList = mUSBMonitor.getDeviceList(filter);

            if (deviceList == null || deviceList.size() == 0) {
                return;
            }

            if (mUSBMonitor != null) {
                mUSBMonitor.requestPermission(deviceList.get(deviceList.size()-1));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Release the USB camera device.
     */
    private synchronized void releaseCamera() {
        Log.d(TAG, "USBCameraHelper.releaseCamera");
//        synchronized (mSync) {
        try {
            if (mUVCCamera != null)
                mUVCCamera.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            if (mUVCCamera != null)
                mUVCCamera.destroy();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            if (mUVCCamera != null)
                mUVCCamera.setButtonCallback(null);
            if (mUVCCamera != null)
                mUVCCamera.setStatusCallback(null);
            if (mUVCCamera != null)
                mUVCCamera.setFrameCallback(null, 0);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        mUVCCamera = null;

        if (mPreviewSurface != null)
            mPreviewSurface.release();
        mPreviewSurface = null;

        if (mWorkerHandler != null)
            mWorkerHandler.removeCallbacksAndMessages(null);
//        }
    }

    private void galleryAddPic(String photoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        f = null;
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
        mediaScanIntent = null;
    }

    /**
     * Run runnable specified on worker thread
     * the same runnable that is unexecuted is cancelled (executed only later)
     *
     * @param task
     * @param delayMillis
     */
    protected final synchronized void queueEvent(final Runnable task, final long delayMillis) {
        if ((task == null) || (mWorkerHandler == null)) return;
        try {
            mWorkerHandler.removeCallbacks(task);
            if (delayMillis > 0) {
                mWorkerHandler.postDelayed(task, delayMillis);
            } else if (mWorkerThreadID == Thread.currentThread().getId()) {
                task.run();
            } else {
                mWorkerHandler.post(task);
            }
        } catch (final Exception e) {
        }
    }

    private void showShortMsg(String msg) {
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
