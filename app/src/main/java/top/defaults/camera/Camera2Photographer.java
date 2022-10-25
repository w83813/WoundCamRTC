package top.defaults.camera;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Range;
import android.view.TextureView;

import org.apache.log4j.chainsaw.Main;
import org.appspot.apprtc.CallRtcClient;
import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.MainActivity;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.content.Context.CAMERA_SERVICE;
import static org.opencv.core.CvType.CV_16UC1;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC1;

//public class Camera2Photographer extends Service implements InternalPhotographer

public class Camera2Photographer implements InternalPhotographer {
    protected static final int CAMERA_CALIBRATION_DELAY = 500;
    protected static final String TAG = "Camera2Photographer";
    protected static final int CAMERACHOICE = CameraCharacteristics.LENS_FACING_BACK;
    protected static long cameraCaptureStartTime;

    protected CameraDevice cameraDevice;
    protected CameraCaptureSession captureSession;
    protected ImageReader imageReader;

    public int facing = Values.FACING_OTHER;
    private String cameraId = "2";
    public int initializedFrame = 0;
    private Camera2Photographer camera2Photographer;
    private Activity activityContext;

    private CameraManager cameraManager;
    private CallbackHandler callbackHandler;
    public TextureView textureView;

    public String nextImageAbsolutePath;
    public volatile Semaphore semaphore = null;
    public boolean doCaptureDepth = false;
    public boolean doCaptureNIR = false;
    private HandlerThread backgroundThread = null;
    private Handler backgroundHandler = null;

//    @Override
//    public void onCreate() {
//        Log.d(TAG, "onCreate service");
//        super.onCreate();
//    }
//
//    @Override
//    public void onDestroy() {
//        stopCamera();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(TAG, "onStartCommand flags " + flags + " startId " + startId);
//
//        readyCamera();
//
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }

    protected CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "CameraDevice.StateCallback onOpened() start");
            cameraDevice = camera;
            actOnReadyCameraDevice();
            Log.d(TAG, "CameraDevice.StateCallback,onOpened() end");
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "CameraDevice.StateCallback,onDisconnected() start");
            callbackHandler.onPreviewStopped();
            if (semaphore != null)
                semaphore.release();
            cameraDevice.close();
            Log.d(TAG, "CameraDevice.StateCallback,onDisconnected() end");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "CameraDevice.StateCallback,onError() start " + error);
            callbackHandler.onError(new Error(Error.ERROR_CAMERA));
            if(cameraDevice!=null){
                cameraDevice.close();
            }

            cameraDevice = null;
            Log.d(TAG, "CameraDevice.StateCallback,onError() end");
        }
    };

    protected CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onReady(CameraCaptureSession session) {
            Log.d(TAG, "CameraCaptureSession.onReady() start");
            Camera2Photographer.this.captureSession = session;
            try {

                // 正常程序 readyCamera() -> CameraDevice.StateCallback onOpened() 取得cameraDevice -> CameraCaptureSession.onReady() -> createCaptureRequest() 打開capturing
                // 在關畢螢幕或解鎖USB時，偶發會出錯 readyCamera() ->  CameraCaptureSession.onReady() -> createCaptureRequest() 打開capturing,失敗因為cameraDevice或getSurface()不存在 -> CameraDevice.StateCallback onOpened() 取得cameraDevice
                Thread.sleep(2000);
                session.setRepeatingRequest(createCaptureRequest(), null, null);
                cameraCaptureStartTime = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
                callbackHandler.onError(new Error(Error.ERROR_CAMERA));
                MainActivity.rtcClient.onPause();
                MainActivity.rtcClient.onResume();
            }
            Log.d(TAG, "CameraCaptureSession.onReady() end");
        }

        @Override
        public void onConfigured(CameraCaptureSession session) {
            Log.d(TAG, "CameraCaptureSession.onConfigured() start");
            callbackHandler.onDeviceConfigured();
            Log.d(TAG, "CameraCaptureSession.onConfigured() end");
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "CameraCaptureSession.onConfigureFailed() start");
            callbackHandler.onError(new Error(Error.ERROR_CAMERA));
            if(captureSession!=null){
                captureSession.close();
            }

            Log.d(TAG, "CameraCaptureSession.onConfigureFailed() end");
        }
    };

    protected ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            //Log.d(TAG, "onImageAvailable");
            if (true) {
                processImageReader(reader);
            } else {
                Image img = reader.acquireLatestImage();
                if (img != null) {
                    if (System.currentTimeMillis() > cameraCaptureStartTime + CAMERA_CALIBRATION_DELAY) {
                        //processImage(img);
                    }
                    img.close();
                }
            }
        }
    };

    public void stopCamera() {
        Log.d(TAG, "stopCamera() start");
        try {
            if (null != imageReader) {
                imageReader.close();

            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        imageReader = null;
        try {
            captureSession.stopRepeating();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        try {
            captureSession.abortCaptures();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        try {
            captureSession.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        captureSession = null;

        try {
            cameraDevice.close();
        } catch (Exception e) {
        }
        cameraDevice = null;

        unInitParams();
        Log.d(TAG, "stopCamera() end");
    }

    public void readyCamera() {
        Log.d(TAG, "readyCamera() start");
        initParams();

        CameraManager manager = (CameraManager) activityContext.getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager);

            cameraId = String.valueOf(facing);
            pickedCamera = cameraId;
            if (ActivityCompat.checkSelfPermission(activityContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(2560, 800, ImageFormat.RAW_PRIVATE, 1);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            Log.d(TAG, "readyCamera() finish");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCamera(CameraManager manager) {
        Log.d(TAG, "getCamera() start");
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CAMERACHOICE) {
                    Log.d(TAG, "getCamera() finish");
                    return cameraId;
                }
            }
            Log.d(TAG, "getCamera() finish");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void actOnReadyCameraDevice() {
        Log.d(TAG, "actOnReadyCameraDevice() start");
        try {
            if (imageReader == null || imageReader.getSurface() == null){
                return;
            }
            cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), sessionStateCallback, null);
            Log.d(TAG, "actOnReadyCameraDevice() finish");
        } catch (Exception e) {
            MainActivity.rtcClient.onPause();
            MainActivity.rtcClient.onResume();
            e.printStackTrace();
            callbackHandler.onError(new Error(Error.ERROR_CAMERA));
        }
    }

    protected CaptureRequest createCaptureRequest() {
        Log.d(TAG, "createCaptureRequest() start");
        try {

            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            CameraCharacteristics mCameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            Range<Integer>[] fpsRanges = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

            int mina = 20, maxa = 20;
            for (Range<Integer> range : fpsRanges) {
                if (mina >= range.getLower()) {
                    mina = range.getLower();
                }
            }

            for (Range<Integer> range : fpsRanges) {
                if (mina == range.getLower() && maxa >= range.getUpper()) {
                    maxa = range.getUpper();
                }
            }

            int i = 0;
            int fpsRangeIndex = 0;
            for (Range<Integer> range : fpsRanges) {
                if (mina == range.getLower() && maxa == range.getUpper()) {
                    fpsRangeIndex = i;
                }
                i++;
            }
            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRanges[fpsRangeIndex]);

            if (imageReader == null || imageReader.getSurface() == null){
                return null;
            }

            builder.addTarget(imageReader.getSurface());

            Log.d(TAG, "createCaptureRequest() finish");
            return builder.build();
        } catch (Exception e) {
            MainActivity.rtcClient.onPause();
            MainActivity.rtcClient.onResume();
            e.printStackTrace();
            return null;
        }
    }

    public void initWithViewfinder(Activity activity, TextureView preview) {
        this.camera2Photographer = this;
        this.activityContext = activity;
        this.textureView = preview;
        cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
        callbackHandler = new CallbackHandler(activityContext);
    }


    @Override
    public boolean isPreviewStarted() {
        return false;
    }

    @Override
    public void setAspectRatio(AspectRatio ratio) {

    }

    @Override
    public void setAutoFocus(boolean autoFocus) {

    }

    @Override
    public void setFacing(int facing) {

    }

    @Override
    public int getFacing() {
        return 0;
    }

    @Override
    public void setFlash(int flash) {

    }

    @Override
    public void setMode(int mode) {

    }

    @Override
    public int getMode() {
        return 0;
    }

    @Override
    public void setOnEventListener(OnEventListener listener) {
        callbackHandler.setOnEventListener(listener);
    }

    @Override
    public void restartPreview() {
        if (backgroundHandler != null) {
            startOrStopPreview(true);
        }
    }

    private synchronized void startOrStopPreview(boolean start) {
        if (!start) {
            stopCamera();
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readyCamera();
        }
    }

    public void startPreview() {
        startOrStopPreview(true);
    }


    public void stopPreview() {
        startOrStopPreview(false);
    }

    public void initParams() {
        if (semaphore == null) {
            semaphore = new Semaphore(1);
        }
        if (backgroundThread == null) {
            backgroundThread = new HandlerThread("DepthCamera BackgroundThread");
            backgroundThread.setPriority(Thread.MIN_PRIORITY);
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());

            Log.d(TAG, "startBackgroundThread step 1");
        }
        Log.d(TAG, "startBackgroundThread step 2");
        initializedFrame = 0;
    }

    public void unInitParams() {
        if (backgroundHandler != null) {
            try {
                backgroundHandler.removeCallbacksAndMessages(null);
                backgroundHandler.getLooper().quitSafely();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        backgroundHandler = null;

        if (backgroundThread != null) {
            try {
                backgroundThread.quitSafely();
                backgroundThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        backgroundThread = null;

        try {
            semaphore.release();
        } catch (Exception e) {

        }
        semaphore = null;
    }

    public void setTextureView(TextureView preview) {
        this.textureView = preview;
    }

    public void takePicture(String imageAbsolutePath) {
        try {
            nextImageAbsolutePath = imageAbsolutePath;
        } catch (Exception e) {
            callbackHandler.onError(Utils.errorFromThrowable(e));
            return;
        }
        doCaptureDepth = true;
        if (AppResultReceiver.DEBUG_LEVEL > 0) {
            doCaptureNIR = true;
        }
    }

    private void processImageReader(ImageReader reader) {
        android.media.Image image = null;

        long snapshotTimems = SystemClock.uptimeMillis();
        AppResultReceiver.lastDepthOnFrame = true;
        boolean foundDepth = false;
        boolean foundNIR = false;
        try {
//              此處有坑, 不停頓的話會當機, 網路上說要停1秒, 原因不明
            //Thread.sleep(1);
//            for (int i = 0; i < reader.getMaxImages(); i++) {
            image = reader.acquireNextImage();

//                if (image == null)
//                    break;

            //張迫清掉可能在 buffer裡的 30 個frames
            if (initializedFrame < 30) {
                initializedFrame++;
                try {
                    image.close();
                } catch (Exception ex) {
                }
                image = null;
                return;
            }
//                Log.d(TAG, "onImageAvailable");

            //onImageAvailable()只做 image.getPlanes()及遮depth cam時,
            // HAL會看到IOMMU的錯, 有機會在一段時間(2~10s, 常常是5s)產生camera onError,
            // 從onError 暫停1秒後會重啟動3個cam
            if (semaphore != null || camera2Photographer.doCaptureDepth) {
                boolean semaphoreAcquired = false;
                synchronized (semaphore) {
                    semaphoreAcquired = semaphore.tryAcquire(1, TimeUnit.MILLISECONDS);
                }
                //if (semaphore.tryAcquire(10, TimeUnit.MILLISECONDS)) {
                if (semaphoreAcquired) {
                    if (image != null) {
                        final Image.Plane[] planes = image.getPlanes();
                        if (planes != null && planes[0] != null) {
                            try {
                                final ByteBuffer buffer = planes[0].getBuffer();
                                int isNIROrDepth = (int) (buffer.get(1280 * 800 * 2 - 1));
                                if (isNIROrDepth == 2 && (snapshotTimems - AppResultReceiver.lastDepthSnapshotTimems) > 100) {
                                    try {
                                        foundDepth = true;
                                        AppResultReceiver.lastDepthSnapshotTimems = snapshotTimems;
                                        //Log.e(TAG, "Time 3d onImageAvailable: " + AppResultReceiver.lastDepthSnapshotTimems);
                                        //final byte[] bytes = new byte[buffer.remaining()];
                                        byte[] bytes = new byte[1280 * 800 * 2];
                                        buffer.get(bytes);
                                        backgroundHandler.post(new ImageSaverDepth(bytes, camera2Photographer)); //, nextImageAbsolutePath, textureView, locker));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        semaphore.release();
                                    }
                                } else if (isNIROrDepth == 1) {
                                    foundNIR = true;
                                    if (camera2Photographer != null && camera2Photographer.doCaptureNIR) {
                                        camera2Photographer.doCaptureNIR = false;
//                                        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
//                                        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(AppResultReceiver.mMainActivity);
//                                        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + AppResultReceiver.SAVE_DIR;
//                                        File mediaFile = new File(path + File.separator + AppResultReceiver.txtData.getFilename2Part() + "_" + (AppResultReceiver.txtData.getCurItemId() + 1) + "_nir.jpg");
//                                        if (!mediaFile.exists()) {
//                                            //final byte[] bytes = new byte[buffer.remaining()];
//                                            byte[] bytes = new byte[1280 * 800 * 2];
//                                            buffer.get(bytes);
//
//                                            Mat matSrc = new Mat(800, 1280, CV_16UC1);
//                                            short shorts[] = new short[bytes.length / 2];
//                                            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
//                                            matSrc.put(0, 0, shorts);
//
//                                            Mat matFinal = new Mat(800, 1280, CV_8UC1);
//                                            matSrc.convertTo(matFinal, CV_8U, 1.0 / 32.0, 0);
//                                            Imgcodecs.imwrite(path + File.separator + AppResultReceiver.txtData.getFilename2Part() + "_" + (AppResultReceiver.txtData.getCurItemId() + 1) + "_nir.jpg", matFinal);
//                                            Thread.sleep(30);
//                                            matFinal.release();
//                                            matSrc.release();
//                                            AppResultReceiver.showToast(AppResultReceiver.mMainActivity, "NIR Image Captured");
//     }
                                    }
                                    semaphore.release();
                                } else {
                                    semaphore.release();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                semaphore.release();
                                callbackHandler.onError(new Error(Error.ERROR_CAMERA, e));
                            }
                        } else {
                            semaphore.release();
                        }
                    } else {
                        semaphore.release();
                    }
                }
//                    }
            }

//                    if ((!foundDepth) && (!foundNIR && camera2Photographer.doCaptureNIR)){
//
//                    } else {
//                        break;
//                    }
//
//            try {
//                image.close();
//            } catch (Exception ex) {
//            }
//            image = null;
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
            callbackHandler.onError(new Error(Error.ERROR_CAMERA, ex));
        } finally {
            try {
                if (image != null)
                    image.close();
//                    image = null;
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }
}