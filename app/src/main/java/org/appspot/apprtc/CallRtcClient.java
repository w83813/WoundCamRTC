/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.appspot.apprtc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.view.Display;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
//import com.jiangdg.usbcamera.UVCCameraHelper;
import com.shlll.libusbcamera.USBCameraHelper;
//import com.serenegiant.usbcameracommon.UVCCameraHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.appspot.apprtc.AppRTCAudioManager.AudioDevice;
import org.appspot.apprtc.AppRTCAudioManager.AudioManagerEvents;
import org.appspot.apprtc.AppRTCClient.RoomConnectionParameters;
import org.appspot.apprtc.AppRTCClient.SignalingParameters;
import org.appspot.apprtc.PeerConnectionClient.PeerConnectionParameters;
import org.appspot.apprtc.util.AsyncHttpURLConnection;
import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.MainActivity;
import org.itri.woundcamrtc.R;
import org.itri.woundcamrtc.SettingsFragment;
import org.itri.woundcamrtc.helper.FileHelper;
import org.itri.woundcamrtc.helper.Model3DHelper;
import org.itri.woundcamrtc.helper.StringUtils;
import org.itri.woundcamrtc.job.JobQueueSaveMJpegImageJob;
import org.itri.woundcamrtc.job.JobQueueSaveImageJob;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Capturer;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Capturer;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import top.defaults.camera.Photographer;

import static android.hardware.Camera.Parameters.FLASH_MODE_AUTO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
import static android.hardware.Camera.Parameters.WHITE_BALANCE_AUTO;
import static org.itri.woundcamrtc.AppResultReceiver.IS_FOR_MIIS_MPDA;
import static org.itri.woundcamrtc.AppResultReceiver.SAVE_DIR;
import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;

//import static android.content.ContentValues.TAG;
//import static org.itri.woundcamrtc.SettingsFragment.KEY_PREF_EXPOS_COMP;
//import static org.itri.woundcamrtc.SettingsFragment.KEY_PREF_FLASH_MODE;
//import static org.itri.woundcamrtc.SettingsFragment.KEY_PREF_FOCUS_MODE;
//import static org.itri.woundcamrtc.SettingsFragment.KEY_PREF_WHITE_BALANCE;


public class CallRtcClient extends Object implements AppRTCClient.SignalingEvents,
        PeerConnectionClient.PeerConnectionEvents {
    private static final String TAG = "CallRtcClient" + ".uvc";

    public static String EXTRA_ROOMURL = "https://appr.tc";
    public static String EXTRA_ROOMID = "12345";
    public static String EXTRA_URLPARAMETERS = "";
    public static boolean EXTRA_LOOPBACK = false;
    public static boolean EXTRA_VIDEO_CALL = true;
    public static boolean EXTRA_SCREENCAPTURE = false;
    public static boolean EXTRA_CAMERA2 = false;
    public static int EXTRA_VIDEO_WIDTH = 1280;
    public static int EXTRA_VIDEO_HEIGHT = 720;
    public static int EXTRA_VIDEO_FPS = 5;
    public static int EXTRA_VIDEO_MAX_BITRATE = 3600;
    public static int EXTRA_VIDEO_MIN_BITRATE = 90;
    public static String EXTRA_VIDEOCODEC = "H264";
    public static boolean EXTRA_HWCODEC_ENABLED = true;
    public static boolean EXTRA_FLEXFEC_ENABLED = false;
    public static int EXTRA_AUDIO_BITRATE = 16;
    public static String EXTRA_AUDIOCODEC = "iSAC"; //iLBC, Opus, iSAC
    public static boolean EXTRA_NOAUDIOPROCESSING_ENABLED = false;
    public static boolean EXTRA_AECDUMP_ENABLED = false;
    public static boolean EXTRA_OPENSLES_ENABLED = false;
    public static boolean EXTRA_DISABLE_BUILT_IN_AEC = true;
    public static boolean EXTRA_DISABLE_BUILT_IN_AGC = true;
    public static boolean EXTRA_DISABLE_BUILT_IN_NS = false;
    public static boolean EXTRA_DISABLE_WEBRTC_AGC_AND_HPF = true;
    public static boolean EXTRA_TRACING = false;
//    public static boolean EXTRA_CMDLINE = false;
//    public static int EXTRA_RUNTIME = 0;
//
//    public static boolean EXTRA_DATA_CHANNEL_ENABLED = false;
//    public static boolean EXTRA_ORDERED = false;
//    public static int EXTRA_MAX_RETRANSMITS_MS = -1;
//    public static int EXTRA_MAX_RETRANSMITS = -1;
//    public static String EXTRA_PROTOCOL = "";
//    public static boolean EXTRA_NEGOTIATED = false;
//    public static int EXTRA_ID = -1;

    public static boolean EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED = false;
    public static boolean EXTRA_ENABLE_RTCEVENTLOG = false;


    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;

    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }

    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    @Nullable
    private PeerConnectionClient peerConnectionClient;
    @Nullable
    private AppRTCClient appRtcClient;
    private AppRTCCallback mCallback = null;
    @Nullable
    private SignalingParameters signalingParameters;
    @Nullable
    private AppRTCAudioManager audioManager;
    @Nullable
    private SurfaceViewRenderer fullscreenRenderer;
    private final List<VideoSink> remoteSinks = new ArrayList<>();
    private Toast logToast;
    private boolean commandLineRun;
    private boolean activityRunning;
    private RoomConnectionParameters roomConnectionParameters;
    @Nullable
    private PeerConnectionParameters peerConnectionParameters;
    private boolean connected;
    private boolean isError;
    private long callStartedTimeMs;
    private boolean micEnabled = true;

    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;
    private boolean isSwappedFeeds;


    android.hardware.Camera mCamera = null; // for camera api
    android.hardware.camera2.CameraManager mCameraManager = null; // for camera2 api
    android.hardware.camera2.CameraDevice mCameraDevice = null;
    Camera1Capturer cameraCapturer;
    Camera2Capturer camera2Capturer;
    private VideoCapturer videoCapturer;
    MainActivity mainActivity;
    JobManager jobManagerUrgent;
    JobManager jobManagerRelax;

    android.os.Handler handler = new android.os.Handler();
    boolean ishandleFocusMetering = false;

    private Matrix camera_to_preview_matrix = new Matrix();
    private Matrix preview_to_camera_matrix = new Matrix();
    private float oldDist = 1f;

    // Controls
//  private CallFragment callFragment;
//  private HudFragment hudFragment;
//  private CpuMonitor cpuMonitor;


    public CallRtcClient(MainActivity _mainActivity, JobManager _jobManagerUrgent, JobManager _jobManagerRelax, int _localViewId, VideoSink ipVideoSink) {
        try {
            mainActivity = _mainActivity;
            jobManagerUrgent = _jobManagerUrgent;
            jobManagerRelax = _jobManagerRelax;

            connected = false;
            signalingParameters = null;

            // init OPENGL
            final EglBase eglBase = EglBase.create();

            // create PeerConnectionParameters
            peerConnectionParameters =
                    new PeerConnectionClient.PeerConnectionParameters(EXTRA_VIDEO_CALL, EXTRA_LOOPBACK,
                            EXTRA_TRACING, EXTRA_VIDEO_WIDTH, EXTRA_VIDEO_HEIGHT, EXTRA_VIDEO_FPS, EXTRA_VIDEO_MAX_BITRATE, EXTRA_VIDEOCODEC,
                            EXTRA_HWCODEC_ENABLED,
                            EXTRA_FLEXFEC_ENABLED,
                            EXTRA_AUDIO_BITRATE, EXTRA_AUDIOCODEC,
                            EXTRA_NOAUDIOPROCESSING_ENABLED,
                            EXTRA_AECDUMP_ENABLED,
                            EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED,
                            EXTRA_OPENSLES_ENABLED,
                            EXTRA_DISABLE_BUILT_IN_AEC,
                            EXTRA_DISABLE_BUILT_IN_AGC,
                            EXTRA_DISABLE_BUILT_IN_NS,
                            EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                            EXTRA_ENABLE_RTCEVENTLOG, null);

            // create PeerConnectionFactory
            peerConnectionClient = new PeerConnectionClient(
                    mainActivity.getApplicationContext(), eglBase, peerConnectionParameters, CallRtcClient.this, videoCapturer);
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            if (EXTRA_LOOPBACK) {
                options.networkIgnoreMask = 0;
            }
            peerConnectionClient.createPeerConnectionFactory(options);

            // create VideoCapturer
            if (peerConnectionParameters.videoCallEnabled) {
                videoCapturer = createVideoCapturer();
            }
            peerConnectionClient.videoCapturer = videoCapturer;

            // display in localView
            fullscreenRenderer = (SurfaceViewRenderer) mainActivity.findViewById(_localViewId);
            fullscreenRenderer.init(eglBase.getEglBaseContext(), null);
            fullscreenRenderer.setScalingType(ScalingType.SCALE_ASPECT_FILL);
            fullscreenRenderer.setEnableHardwareScaler(true);
            peerConnectionClient.localRender = fullscreenRenderer;
            peerConnectionClient.localProcess = ipVideoSink;
            peerConnectionClient.remoteSinks = remoteSinks;
            setSwappedFeeds(false);

            // create Media Constraints
            peerConnectionClient.createMediaConstraintsInternal();

            // create VideoTrack & create AudioTrack
            peerConnectionClient.createVideoTrack(videoCapturer);
            peerConnectionClient.createAudioTrack();
            Logging.enableLogToDebugOutput(Logging.Severity.LS_NONE);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        Log.v(TAG, "finalize");
    }

    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) mainActivity.getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    private boolean captureToTexture() {
        return false; //getIntent().getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, false);
    }

    @TargetApi(21)
    private @Nullable
    VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            reportError("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(
                mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                reportError("User revoked permission to capture the screen.");
            }
        });
    }


    private @Nullable
    VideoCapturer createCameraCapturer(CameraEnumerator enumerator, boolean isFront) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras. total camera count =" + deviceNames.length);
        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                //if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    cameraCapturer = (Camera1Capturer) videoCapturer;
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
//            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    cameraCapturer = (Camera1Capturer) videoCapturer;
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private @Nullable
    VideoCapturer createCamera2Capturer(CameraEnumerator enumerator, boolean isFront) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras. total camera count =" + deviceNames.length);
        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                //if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    camera2Capturer = (Camera2Capturer) videoCapturer;
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
//            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    camera2Capturer = (Camera2Capturer) videoCapturer;
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    public android.hardware.Camera getCamera() {
        android.hardware.Camera mCamera = null;
        Field pricamera = null;
        try {
            pricamera = Camera1Capturer.class.getSuperclass().getDeclaredField("currentSession");

            pricamera.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object cameraSession = null;
        try {

            cameraSession = (Object) pricamera.get(cameraCapturer);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        Field pricamera2 = null;
        try {
            pricamera2 = cameraSession.getClass().getDeclaredField("camera");

            pricamera2.setAccessible(true);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        try {
            mCamera = (android.hardware.Camera) pricamera2.get(cameraSession);
        } catch (Exception e) {
            //e.printStackTrace();
        }


        //Log.i("步驟mCamera1", "mCamera" + mCamera);
        return mCamera;
    }

    public android.hardware.camera2.CameraManager getCameraManager(Camera2Capturer camera2Capturer) {
        Field ff1 = null;
        try {
            ff1 = Camera2Capturer.class.getDeclaredField("cameraManager");
            ff1.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object oo1 = null;
        try {
            oo1 = (Object) ff1.get(camera2Capturer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (android.hardware.camera2.CameraManager) oo1;
    }

    // Activity interfaces
    public void onPause() {
        resumeOrPause(false);
    }

    public void onResume() {
        resumeOrPause(true);
    }


    public synchronized void resumeOrPause(boolean resume) {
        try {
            Log.v(TAG,"resumeOrPause");
            if (!resume) {
                activityRunning = false;

                try {
                    if (peerConnectionClient != null) {
                        peerConnectionClient.stopVideoSource();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (AppResultReceiver.isPiModule) {
                    mainActivity.mGetIpHandler.removeCallbacksAndMessages(null);mainActivity.mGetMsgHandler.removeCallbacksAndMessages(null);mainActivity.mGetThermalHandler.removeCallbacksAndMessages(null);
                    mainActivity.mGetIpHandler.removeCallbacks(mainActivity.mFindIpRunner);
                    mainActivity.mGetMsgHandler.removeCallbacks(mainActivity.mGetMsgRunner);
                    mainActivity.mGetThermalHandler.removeCallbacks(mainActivity.mGetThermalRunner);
                    mainActivity.mtxtDownloader.stop();
                }

                if (AppResultReceiver.isUvcDevice) {
                    mainActivity.initOrResumeOrPauseOrDestroyUVCCamera(3);
                    mainActivity.initOrResumeOrPauseOrDestroyUVCCamera(4);
//                mainActivity.onPauseUVCCamera();
//                mainActivity.onDestroyUVCCamera();
                }

                if (AppResultReceiver.isMultiCam && mainActivity.photographer != null) {
                    mainActivity.photographer.stopPreview();
                }
                try {
                    jobManagerRelax.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.removeCallbacksAndMessages(null);

//    if (cpuMonitor != null) {
//      cpuMonitor.pause();
//    }
            } else {
                activityRunning = true;
                jobManagerRelax.start();

                if (peerConnectionClient != null) {
                    peerConnectionClient.startVideoSource();
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                            EXTRA_VIDEO_MAX_BITRATE = Integer.parseInt(sharedPreferences.getString(SettingsFragment.KEY_PREF_VIDEO_BITRATE, "1024"));
                            peerConnectionClient.setVideoEnabled(true);
                            peerConnectionClient.setVideoBitrate(EXTRA_VIDEO_MIN_BITRATE, EXTRA_VIDEO_MAX_BITRATE);
                        } catch (Exception e) {
                            Log.v(TAG, "setVideoMaxBitrate error:" + e.getMessage());
                        }

                        //try to get camera object until camera is ready
                        if (!EXTRA_CAMERA2) {
                            long startTime = System.currentTimeMillis(); //fetch starting time
                            while (System.currentTimeMillis() - startTime < 5000) {
                                try {
                                    android.hardware.Camera camera = getCamera();
                                    if (camera != null) {
                                        mCamera = camera;
                                        break;
                                    }
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    Log.v(TAG, "camera.setParameters error 1:" + e.getMessage());
                                }
                            }

                            try {
                                Camera.Parameters mParameters = mCamera.getParameters();
                                loadBiggestSupportedPictureSize(mParameters);
                                mCamera.setParameters(mParameters);
                            } catch (Exception e) {
                                Log.v(TAG, "camera.setParameters error 2:" + e.getMessage());
                            }

                            try {
                                Camera.Parameters mParameters = mCamera.getParameters();
                                String rotation = mParameters.get("rotation");
                                if (Build.MODEL.endsWith("MPD100") || Build.MODEL.endsWith("MPD500")) {
                                    mParameters.setRotation(-90);
                                } else {
                                    mParameters.setRotation(90);
                                }
                                mCamera.setParameters(mParameters);
                            } catch (Exception e) {
                                Log.v(TAG, "camera.setParameters error 3:" + e.getMessage());
                            }

                            try {
                                Camera.Parameters mParameters = mCamera.getParameters();
                                loadSmallestSupportedFps(mParameters);
                                mCamera.setParameters(mParameters);
                            } catch (Exception e) {
                                Log.v(TAG, "camera.setParameters error 4:" + e.getMessage());
                            }

                            try {
                                Camera.Parameters mParameters = mCamera.getParameters();
                                mParameters.set("denoise", "off");
                                mCamera.setParameters(mParameters);
                            } catch (Exception e) {
                                Log.v(TAG, "camera.setParameters error 5:" + e.getMessage());
                            }

                            try {
                                Camera.Parameters mParameters = mCamera.getParameters();
                                mParameters.setPreviewSize(1280,720);
                                mParameters.set("preview-frame-rate", "7");
                                mCamera.setParameters(mParameters);
                            } catch (Exception e) {
                                Log.v(TAG, "camera.setParameters error 6:" + e.getMessage());
                            }

                            try {
                                Camera.Parameters mParameters = mCamera.getParameters();
                                mParameters.set("gps_data", "false");
                                mParameters.removeGpsData();
                                mCamera.setParameters(mParameters);
                            } catch (Exception e) {
                                Log.v(TAG, "camera.setParameters error 7:" + e.getMessage());
                            }

                            try {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                                Camera.Parameters mParameters = mCamera.getParameters();
                                mParameters.setExposureCompensation(Integer.parseInt(sharedPreferences.getString(SettingsFragment.KEY_PREF_EXPOS_COMP, "0")));
                                mParameters.setFlashMode(sharedPreferences.getString(SettingsFragment.KEY_PREF_FLASH_MODE, FLASH_MODE_AUTO));
                                mParameters.setFocusMode(sharedPreferences.getString(SettingsFragment.KEY_PREF_FOCUS_MODE, FOCUS_MODE_CONTINUOUS_PICTURE));
                                mParameters.setWhiteBalance(sharedPreferences.getString(SettingsFragment.KEY_PREF_WHITE_BALANCE, WHITE_BALANCE_AUTO));
                                mParameters.setJpegQuality(Integer.parseInt(sharedPreferences.getString(SettingsFragment.KEY_PREF_JPEG_QUALITY, "70")));
                                mParameters.setVideoStabilization(false);
                                mCamera.setParameters(mParameters);
                            } catch (Exception e) {
                                Log.v(TAG, "camera.setParameters error 8:" + e.getMessage());
                            }

                            try {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                                Camera.Parameters mParameters = mCamera.getParameters();
                                String supportedIsoValues = mParameters.get("iso-values");
                                String isoValue = mParameters.get("iso");
                                mParameters.set("iso", "1600");
                                mCamera.setParameters(mParameters);
                            } catch (Exception e) {
                                Log.v(TAG, "camera.setParameters error 9:" + e.getMessage());
                            }
                        } else if (EXTRA_CAMERA2) {
                            android.hardware.camera2.CameraManager cameraCaptureSession = getCameraManager(camera2Capturer);
                            try {
                                int mCameraId = 0;
                                CameraCharacteristics mCameraCharacteristics = mCameraManager.getCameraCharacteristics(Integer.toString(mCameraId));


//                        if (exposureMode == Mode.MANUAL) {
//                            int isoValue = (int) manualState.ISO;
//                            request.set(CaptureRequest.SENSOR_SENSITIVITY, isoValue);
//                        } else {
//                            request.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureCompensation);
//                        }
//
//                        if (colorCorrectionMode == Mode.MANUAL) {
//                            request.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);
//                            request.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
//                            request.set(CaptureRequest.COLOR_CORRECTION_GAINS, manualState.colorCorrection);
//                        }


                                Range<Integer>[] fpsRanges = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                                if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                cameraCaptureSession.openCamera(String.valueOf(0), new CameraDevice.StateCallback() {
                                    @Override
                                    public void onOpened(@NonNull CameraDevice cameraDevice) {

                                    }

                                    @Override
                                    public void onDisconnected(@NonNull CameraDevice cameraDevice) {

                                    }

                                    @Override
                                    public void onError(@NonNull CameraDevice cameraDevice, int i) {

                                    }
                                }, null);

                                //                        CameraCaptureSession cameraCaptureSession;
//                        cameraCaptureSession.
//                      mCameraDevice =  mCameraManager.registerAvailabilityCallback().openCamera(0,null,null);
//                        mCameraDevice.
//                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRanges[0]);
//                        StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "AppResultReceiver.GET_MIPI_DEVICE_DELAY_MS:" + AppResultReceiver.GET_MIPI_DEVICE_DELAY_MS);
                                try {
                                    if (AppResultReceiver.isMultiCam && mainActivity.photographer != null) {
                                        mainActivity.photographer.startPreview();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "AppResultReceiver.GET_USB_DEVICE_DELAY_MS:" + AppResultReceiver.GET_USB_DEVICE_DELAY_MS);
                                        try {
                                            if (AppResultReceiver.isUvcDevice) {
                                                mainActivity.initOrResumeOrPauseOrDestroyUVCCamera(0);
                                                mainActivity.initOrResumeOrPauseOrDestroyUVCCamera(1);
                                                mainActivity.initOrResumeOrPauseOrDestroyUVCCamera(2);
//                                            mainActivity.onInitUVCSurface();
//                                            mainActivity.onInitUVCCamera();
//                                            mainActivity.onResumeUVCCamera();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    // 設定定時要執行的函數
                                                    if (AppResultReceiver.isPiModule) {
                                                        mainActivity.mGetIpHandler.removeCallbacks(mainActivity.mFindIpRunner);
                                                        mainActivity.mGetMsgHandler.removeCallbacks(mainActivity.mGetMsgRunner);
                                                        mainActivity.mGetThermalHandler.removeCallbacks(mainActivity.mGetThermalRunner);
                                                        // 設定間隔的時間
                                                        mainActivity.mGetIpHandler.postDelayed(mainActivity.mFindIpRunner, 1);
                                                        mainActivity.mGetMsgHandler.postDelayed(mainActivity.mGetMsgRunner, 1);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, 600);
                                        Log.d(TAG, "isPiModule.postDelay");
                                    }
                                }, 100 + AppResultReceiver.GET_USB_DEVICE_DELAY_MS);
                                Log.d(TAG, "isUvcDevice.postDelay");
                            }
                        }, 100 + AppResultReceiver.GET_MIPI_DEVICE_DELAY_MS);
                        Log.d(TAG, "isMultiCam.postDelay");
                    }
                }, 100);
                Log.d(TAG, "onResume.postDelay");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect("Destory");
        if (fullscreenRenderer != null) {
            fullscreenRenderer.release();
            fullscreenRenderer = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.closeAll();
            peerConnectionClient = null;
        }

        remoteProxyRenderer.setTarget(null);
        localProxyVideoSink.setTarget(null);

        if (logToast != null) {
            logToast.cancel();
        }
        activityRunning = false;
    }

    // CallFragment.OnCallEvents interface implementation.

    public void onCallHangUp() {
        disconnect("Call hang up");
    }

    public void onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
        }
    }

    public void onVideoScalingSwitch(ScalingType scalingType) {
        fullscreenRenderer.setScalingType(scalingType);
    }

    public void onCaptureFormatChange(int width, int height, int framerate) {
        if (peerConnectionClient != null) {
            peerConnectionClient.changeCaptureFormat(width, height, framerate);
        }
    }

    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }


    public void startCall(String tid) {

        // 1. 通知WebRTC Server, 移除前次APP不正常終止, 遺留在room裡舊的session id
        if (appRtcClient != null) {
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                String url = sharedPreferences.getString(AppResultReceiver.KEY_LAST_LEAVE_URL, "");
                if (StringUtils.isNotBlank(url))
                    sendPostMessage(WebSocketRTCClient.MessageType.LEAVE, url, null);
            } catch (Exception e) {
                Log.v(TAG, "RTC last leave url is error:" + e.getMessage());
            }

            //stopCall();
        }

        // 2. 建立新的WebRTC client Create WebSocketRTCClient.
        appRtcClient = new WebSocketRTCClient(this);

        // 3. 建立連線參數 Create WebSocketRTC connection parameters.
        roomConnectionParameters = new RoomConnectionParameters(EXTRA_ROOMURL, EXTRA_ROOMID, EXTRA_LOOPBACK, EXTRA_URLPARAMETERS);

        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.
        logAndToast("connecting to " + roomConnectionParameters.roomUrl);

        appRtcClient.connectToRoom(roomConnectionParameters);

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(mainActivity.getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    AudioDevice audioDevice, Set<AudioDevice> availableAudioDevices) {
            }
        });
    }

    private void sendPostMessage(
            final WebSocketRTCClient.MessageType messageType, final String url,
            @Nullable final String message) {
        String logInfo = url;
        if (message != null) {
            logInfo += ". Message: " + message;
        }
        Log.d(TAG, "C->GAE: " + logInfo);
        AsyncHttpURLConnection httpConnection =
                new AsyncHttpURLConnection("POST", url, message, new AsyncHttpURLConnection.AsyncHttpEvents() {
                    @Override
                    public void onHttpError(String errorMessage) {
                        reportError("GAE POST error: " + errorMessage);
                    }

                    @Override
                    public void onHttpComplete(String response) {
                        if (messageType == WebSocketRTCClient.MessageType.MESSAGE) {
                            try {
                                JSONObject roomJson = new JSONObject(response);
                                String result = roomJson.getString("result");
                                if (!result.equals("SUCCESS")) {
                                    reportError("GAE POST error: " + result);
                                }
                            } catch (JSONException e) {
                                reportError("GAE POST JSON error: " + e.toString());
                            }
                        }
                    }
                });
        httpConnection.send();
    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
        setSwappedFeeds(false /* isSwappedFeeds */);
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
            final AudioDevice device, final Set<AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    public void stopCall() {
        // 1. 先停掉聲音
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }

        // 2. 通知登出
        if (appRtcClient != null) {
            logAndToast("disconnecting");
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }

        // 3. 清掉peer
        if (peerConnectionClient != null)
            peerConnectionClient.localSdp = null;

        // 4. 清掉參數
        roomConnectionParameters = null;
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect(final String description) {
        Log.v(TAG,"disconnect : "+description);
        activityRunning = false;

        stopCall();
//        remoteProxyRenderer.setTarget(null);
//        localProxyVideoSink.setTarget(null);
//        if (appRtcClient != null) {
//            appRtcClient.disconnectFromRoom();
//            appRtcClient = null;
//        }
//    if (pipRenderer != null) {
//      pipRenderer.release();
//      pipRenderer = null;
//    }
//        if (fullscreenRenderer != null) {
//            fullscreenRenderer.release();
//            fullscreenRenderer = null;
//        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
//            peerConnectionClient = null;
        }
//        if (audioManager != null) {
//            audioManager.stop();
//            audioManager = null;
//        }
//    if (connected && !isError) {
//      setResult(RESULT_OK);
//    } else {
//      setResult(RESULT_CANCELED);
//    }
//    finish();

        if (mCallback != null) {
            if (StringUtils.isBlank(description))
                mCallback.onDisconnected("");
            else
                mCallback.onError(description);
        }
    }

    private void disconnectWithErrorMessage(final String errorMessage) {

        disconnect(errorMessage);

        if (commandLineRun || !activityRunning) {
            Log.e(TAG, "Critical error: " + errorMessage);
        } else {
            new AlertDialog.Builder(mainActivity)
                    .setTitle("channel_error_title")
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton("ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .create()
                    .show();
        }
    }


    public void setVideoMaxBitrate(int videoMaxBitrate) {
        try {
            peerConnectionClient.setVideoMaxBitrate(videoMaxBitrate);
        } catch (Exception e) {

        }
    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(mainActivity, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    private void reportError(final String description) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isError = true;
                disconnectWithErrorMessage(description);
//                    mainActivity.setRTCButtonToDisconnect();
//                    if (mCallback!=null)
//                        mCallback.onError(description);
            }
        });
    }

    private @Nullable
    VideoCapturer createVideoCapturer() {
        final VideoCapturer videoCapturer;
//    String videoFileAsCamera = getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
//    if (videoFileAsCamera != null) {
//      try {
//        videoCapturer = new FileVideoCapturer(videoFileAsCamera);
//      } catch (IOException e) {
//        reportError("Failed to open video file for emulated camera");
//        return null;
//      }
        if (EXTRA_SCREENCAPTURE) {
            return createScreenCapturer();
        } else if (EXTRA_CAMERA2) {
//            if (!captureToTexture()) {
//                reportError("camera2_texture_only_error");
//                return null;
//            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCamera2Capturer(new Camera2Enumerator(mainActivity), false);
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()), false);
        }
        if (videoCapturer == null) {
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    public void toggleFlash() {
        try {
            Camera.Parameters mParameters = mCamera.getParameters(); //取得Camera的參數
            Log.v(TAG, "模式:" + mParameters.getFlashMode());
            if (mParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_AUTO) || mParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF) || mParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_RED_EYE)) {
                setFlash(true);
            } else {
                setFlash(false);
            }
        } catch (Exception ex){

        }
    }

    public void setFlash(boolean on) {
        try {
            android.hardware.Camera camera = getCamera();
            if (camera != null)
                mCamera = camera;
        } catch (Exception e) {

        }
        try {
            if (on) {
//            mainActivity.changeLightImg(true);
                mainActivity.menuImageButton_light.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.blue_light_40, 0, 0);
                mainActivity.menuImageButton_light.setBackgroundColor(Color.argb(80, 255, 255, 77));

                Camera.Parameters mParameters = mCamera.getParameters(); //取得Camera的參數
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH); //設定持續亮燈模式
                mCamera.setParameters(mParameters); //將參數回存回Camera
            } else {
//            mainActivity.changeLightImg(false);
                mainActivity.menuImageButton_light.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.light_40, 0, 0);
                mainActivity.menuImageButton_light.setBackgroundColor(Color.argb(63, 63, 63, 63));

                Camera.Parameters mParameters = mCamera.getParameters(); //取得Camera的參數
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mParameters);
            }
        } catch (Exception e) {

        }
    }

    @SuppressLint("WrongConstant")
    public void takePicture(final ImageView view, USBCameraHelper usbCameraHelper,
                            int index, Photographer multiCamPhotographer) {
        try {
            android.hardware.Camera camera = getCamera();

            if (camera != null)
                mCamera = camera;

        } catch (Exception e) {
            Log.d(TAG, "Error mCamera error: " + e.getMessage());
        }

        try {
            Display display = ((WindowManager) mainActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            AppResultReceiver.touchRotate = display.getRotation();

            if (AppResultReceiver.FOCUS_AREA_TYPE.equals("shut")) {
//                int viewWidth = display.getWidth();//getWidth();
//                int viewHeight = display.getHeight(); //getHeight();
                AppResultReceiver.touchFocusXScale = 0.5;
                AppResultReceiver.touchFocusYScale = 0.5;

                mCamera.cancelAutoFocus();
                try {
                    Camera.Parameters params = mCamera.getParameters();
                    params.setFocusMode(FOCUS_MODE_AUTO);
                    params.setFocusAreas(null);
                    mCamera.setParameters(params);
                } catch (Exception e) {
                    Log.i(TAG, "camera.setParameters error 10:" + e.getMessage());
                }
//                Log.e(TAG, "Time autoFocus: " + SystemClock.uptimeMillis());
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        try {
                            doTakePicture(view, usbCameraHelper, index, multiCamPhotographer);
                        } catch (Exception e) {
                            Log.i(TAG, "camera.setParameters error 11:" + e.getMessage());
                        }
                    }
                });
            } else {

                doTakePicture(view, usbCameraHelper, index, multiCamPhotographer);
            }

        } catch (Exception e) {
            // mainActivity.showAlertAndExit("can't capture:"+e.getMessage());
        }
    }

    //執行拍照
    public void doTakePicture(ImageView view, USBCameraHelper usbCameraHelper,
                              int index, Photographer multiCamPhotographer) {
        if (IS_FOR_MIIS_MPDA == true ) {
            if (!AppResultReceiver.lastColorOnFrame || !AppResultReceiver.lastThermalOnFrame || !AppResultReceiver.lastDepthOnFrame){
                mainActivity.showToast(mainActivity.getString(R.string.takePic_error));
                AppResultReceiver.isTakingPicture = false;
                return ;
            }
        }

        Log.e(TAG, "Time doTakePicture: " + SystemClock.uptimeMillis());
        AppResultReceiver.lastPicSnapshotTimems = 0;

        try {
            //儲存3D景深
            if (AppResultReceiver.isMultiCam && multiCamPhotographer != null) {
                final SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
                int depthImageCount = 200 + mainActivity.count;
                File mediaFile = new File(file.getPath() + File.separator + mainActivity.evlId + "_" + s.format(new Date()) + "_" + depthImageCount + "_3ds.raw");
                multiCamPhotographer.takePicture(mediaFile.toString());
//                        try {
//                            String path = file.getPath() + File.separator + mainActivity.evlId + "_" + s.format(new Date()) + "_" + mainActivity.evlStep + "_data.txt";
//                            File targetTxt = new File(path);
//                            if (!targetTxt.exists()) {
//                                writeToFile(path, "evlId=" + mainActivity.evlId + "\r\nownerId=" + mainActivity.ownerId +
//                                        "\r\n" + "info\r\n", true);
//                            }
//
//                            Map map = new HashMap();
//                            map.put("itemId", String.valueOf(mainActivity.count + 199));
//                            map.put("bodyPart", mainActivity.part);
//
//                            JSONObject obj = new JSONObject(map);
//                            writeToFile(path, obj + "\r\n", true);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
            }
        } catch (Exception e) {
            Log.d(TAG, "Error JobQueueSaveMultiCamImageJob error: " + e.getMessage());
        }

        try {
            //儲存熱感圖像
            if (usbCameraHelper != null && usbCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex)) {
                final SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
                int heatImageCount = 100 + mainActivity.count;
                File mediaFile = new File(file.getPath() + File.separator + mainActivity.evlId + "_" + s.format(new Date()) + "_" + heatImageCount + "_thm.png");
                usbCameraHelper.capturePicture(index, mediaFile.toString(), null);
                try {


                    if (dataEncrypt == false) {

                    } else {
                        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
                        String filename_sercret = file.getPath() + File.separator + mainActivity.evlId + "_" + s.format(new Date()) + "_" + mainActivity.evlStep + "_datax.txt";
                        File target_sercret = new File(filename_sercret);
                        if (target_sercret.exists()) {
                            try {
                                FileHelper.txt_decrypt(target_sercret.getAbsolutePath(), 18);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    String path = file.getPath() + File.separator + mainActivity.evlId + "_" + s.format(new Date()) + "_" + mainActivity.evlStep + "_data.txt";
                    File targetTxt = new File(path);
                    if (!targetTxt.exists()) {
                        AppResultReceiver.writeToFile(path, "evlId=" + mainActivity.evlId + "\r\nownerId=" + mainActivity.ownerId +
                                "\r\n" + "info\r\n", true);
                    }

                    Map map = new HashMap();
                    map.put("itemId", String.valueOf(mainActivity.count + 100));
                    map.put("bodyPart", mainActivity.part);

                    JSONObject obj = new JSONObject(map);
                    AppResultReceiver.writeToFile(path, obj + "\r\n", true);
                    if (dataEncrypt == false) {

                    } else {
                        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
                        String filename_sercret = file.getPath() + File.separator + mainActivity.evlId + "_" + s.format(new Date()) + "_" + mainActivity.evlStep + "_data.txt";
                        File target_sercret = new File(filename_sercret);
                        if (target_sercret.exists()) {
                            try {

                                FileHelper.txt_encryption(target_sercret.getAbsolutePath(), 18);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{

                Log.v(TAG,"3260Lagi");
            }

//                    if (mUVCCameraHandler != null && mUVCCameraHandler.isOpened()) {
//                        try {
//                            final SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
//                            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
//                            int heatImageCount = 99 + mainActivity.count;
//                            File mediaFile = new File(file.getPath() + File.separator + mainActivity.evlId + "_" + s.format(new Date()) + "_" + heatImageCount + "_jpg.png");
//                            mUVCCameraHandler.captureStill(mediaFile.toString());
////                    jobManagerUrgent.addJobInBackground(new JobQueueSaveUVCImageJob(jobManagerRelax, mainActivity, "0.0"));
//
//                            try {
//                                //尋找原來的txt檔並寫入距離
//                                String path = file.getPath() + File.separator + mainActivity.evlId + "_" + s.format(new Date()) + "_" + mainActivity.evlStep + "_data.txt";
//                                File targetTxt = new File(path);
//
//                                if (!targetTxt.exists()) {
//                                    writeToFile(path, "evlId=" + mainActivity.evlId + "\r\nownerId=" + mainActivity.ownerId +
//                                            "\r\n" + "info\r\n", true);
//                                }
//
//                                Map map = new HashMap();
//                                map.put("itemId", String.valueOf(mainActivity.count + 99));
//                                map.put("bodyPart", mainActivity.part);
//                                //AppResultReceiver.recordList.add(map);
//
//                                JSONObject obj = new JSONObject(map);
//                                writeToFile(path, obj + "\r\n", true);
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//
//                        } catch (Exception ex) {
//
//                        }
//                    }
        } catch (Exception e) {
            Log.d(TAG, "Error JobQueueSaveUVCImageJob error: " + e.getMessage());
        }

        try {
            if (AppResultReceiver.isPiModule) {
                jobManagerRelax.addJobInBackground(new JobQueueSaveMJpegImageJob(jobManagerRelax, mainActivity, "0.0"));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error JobQueueSavePiHeatImageJob error: " + e.getMessage());
        }

        Log.e(TAG, "Time takePicture: " + SystemClock.uptimeMillis());
        AppResultReceiver.lastPicSnapshotTimems = SystemClock.uptimeMillis() + 150;
//        AppResultReceiver.snapshutDistance =  AppResultReceiver.touchPointDepthCentiMeter;
        AppResultReceiver.snapshutDistance = AppResultReceiver.touchPointDepthCentiMeterAvg;
        try {
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, final Camera camera) {
                    Log.e(TAG, "Color image captured onPictureTaken: " + SystemClock.uptimeMillis());
                    int colorImageCount = mainActivity.getNextCountValue();
                    try {
                        jobManagerUrgent.addJobInBackground(new JobQueueSaveImageJob(jobManagerUrgent, "", mainActivity, data, 1, view, colorImageCount));
                    } catch (Exception e) {
                        Log.d(TAG, "Error JobQueueSaveImageJob error: " + e.getMessage());
                    }

                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                camera.startPreview();
                            } catch (Exception e) {
                                Log.d(TAG, "Error takePicture error: " + e.getMessage());
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "Error takePicture error: " + e.getMessage());
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        try {
            android.hardware.Camera camera = getCamera();
            if (camera != null)
                mCamera = camera;
        } catch (Exception e) {

        }

//        WindowManager manager = (WindowManager) mainActivity.getSystemService(Context.WINDOW_SERVICE);
//        Display display = manager.getDefaultDisplay();
//        int width = display.getWidth();
//        int height = display.getHeight();

//        mCamera = client.getCamera();
//        initCamera();
//        if (mCamera!=null) {
        //如果點擊觸碰數目為一次時
        if (event.getPointerCount() == 1) {
            //處理相機focus事件
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                handleFocusMetering(event, mCamera);
        } else {
            //ACTION_MOVE為持續觸擊且移動、ACTION_POINTER_DOWN為多個手指同時觸碰螢幕
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }

        // }
        //即便觸擊沒有放開也可持續執行 onTouchEvent  (return true)
        return true;
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        remoteProxyRenderer.setTarget(fullscreenRenderer);
        fullscreenRenderer.setMirror(isSwappedFeeds);
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        signalingParameters = params;
        peerConnectionClient.signalingParameters = params;

        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            sharedPreferences.edit().putString(AppResultReceiver.KEY_LAST_LEAVE_URL, params.leaveUrl).commit();
        } catch (Exception e) {
            Log.v(TAG, "RTC last leave url is error:" + e.getMessage());
        }


        logAndToast("Creating peer connection, delay=" + delta + "ms");
//        VideoCapturer videoCapturer = null;
//        if (peerConnectionParameters.videoCallEnabled) {
//            videoCapturer = createVideoCapturer();
//        }
//        peerConnectionClient.createPeerConnection(
//                localProxyVideoSink, remoteSinks, videoCapturer, signalingParameters);

        peerConnectionClient.createPeerConnectionInternal();
        if (signalingParameters.initiator) {
            logAndToast("Creating OFFER...");
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();

            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
        if (mCallback != null)
            mCallback.onConnected();
    }

    @Override
    public void onConnectedToRoom(final SignalingParameters params) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onConnectedToRoomInternal(params);
            }
        });
    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
                peerConnectionClient.setRemoteDescription(sdp);
                if (!signalingParameters.initiator) {
                    logAndToast("Creating ANSWER...");
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createAnswer();
                }
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.removeRemoteIceCandidates(candidates);
            }
        });
    }

    @Override
    public void onChannelClose() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("Remote end hung up; dropping PeerConnection");
                if (mCallback != null)
                    mCallback.onDisconnected("Hung up");
            }
        });
    }

    @Override
    public void onChannelError(final String description) {
        reportError(description);
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms");
                    if (signalingParameters.initiator) {
                        appRtcClient.sendOfferSdp(sdp);
                    } else {
                        appRtcClient.sendAnswerSdp(sdp);
                    }
                }
                if (peerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidate(candidate);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
//        mainActivity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (appRtcClient != null) {
//                    appRtcClient.sendLocalIceCandidateRemovals(candidates);
//                }
//            }
//        });
    }

    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("ICE connected, delay=" + delta + "ms");
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("ICE disconnected");
            }
        });
    }

    @Override
    public void onConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("DTLS connected, delay=" + delta + "ms");
                connected = true;
                callConnected();
            }
        });
    }

    @Override
    public void onDisconnected() {

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                logAndToast("DTLS disconnected");
                connected = false;
                disconnect("DTLS disconnected");
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError && connected) {
//          hudFragment.updateEncoderStatistics(reports);
                }
            }
        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
    }

    public void loadBiggestSupportedPictureSize(Camera.Parameters mParameters) {
        Camera.Size size = getOptimalSize(mParameters.getSupportedPictureSizes(), 8000000, 7000000);
        mParameters.setPictureSize(size.width, size.height);
    }

    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int maxPixels, int minPixels) {
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                // first return 1, last return -1
                // smallest first
                if (o1 == null || o2 == null) return 0;
                if (o1.width < o2.width) {
                    return 1;
                } else if (o1.width == o2.width) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        Camera.Size f_size = null;
        if (sizes.size() > 0) f_size = sizes.get(0);
        //預設最佳拍照解析度為800萬畫素(或是該相機的最大值)
        double aspectRatio = 4.0 / 3.0;
        Camera.Size o_size = null;

        for (Iterator<Camera.Size> iterator = sizes.iterator(); iterator.hasNext(); ) {
            Camera.Size size = iterator.next();
            if (size.height * size.width < minPixels) {
                iterator.remove();
            } else if (size.width * size.height > maxPixels) {
                iterator.remove();
            } else if ((double) size.width / (double) size.height != aspectRatio && (double) size.height / (double) size.width != aspectRatio) {
                iterator.remove();
            }
        }

        for (Camera.Size size : sizes) {
            o_size = size;
            break;
        }

        if (o_size != null)
            return o_size;
        else
            return f_size;
    }

    public void loadSmallestSupportedFps(Camera.Parameters mParameters) {
        int mina = 15000, maxa = 30000;
        int tmp[];
        for (Object size : mParameters.getSupportedPreviewFpsRange().toArray()) {
            tmp = (int[]) size;
            if (mina >= tmp[0]) {
                mina = tmp[0];
            }
        }

        for (Object size : mParameters.getSupportedPreviewFpsRange().toArray()) {
            tmp = (int[]) size;
            if (mina == tmp[0] && maxa >= tmp[1]) {
                maxa = tmp[1];
                //mina = tmp[0];
            }
        }

        mina = maxa = 15000;
        mParameters.setPreviewFpsRange(mina, maxa);
        AppResultReceiver.webrtcMinPreviewFps = mina;
    }

    public void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    public void handleFocusMetering(final MotionEvent event, Camera camera) {
        if (ishandleFocusMetering) return;
        ishandleFocusMetering = true;
        try {
            mCamera.cancelAutoFocus();
        } catch (Exception e) {
            Log.i(TAG, "camera.cancelAutoFocus error:" + e.getMessage());
        }

//        Log.i(TAG, "handleFocusMetering");
        Display display = ((WindowManager) mainActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();

        int offsetX = 0;
        int offsetY = -48;

//        switch (rotation) {
//            case Surface.ROTATION_0: //0
//                offsetY = -90;
//                break;
//            case Surface.ROTATION_90: //1
//                offsetY = -54;
//                break;
//            case Surface.ROTATION_180: //2
//                offsetY = -64;
//                break;
//            case Surface.ROTATION_270: //3
//                offsetY = 0;
//                break;
//        }

        final float eventX = event.getX() + offsetX;
        final float eventY = event.getY() + offsetY;

//        int viewWidth = display.getWidth();//getWidth();
//        int viewHeight = display.getHeight(); //getHeight();
//        Log.v(TAG, "viewWidth:" + viewWidth);
//        Log.v(TAG, "viewHeight:" + viewHeight);
//        Rect focusRect = calculateTapArea(eventX, eventY, 1.5f, viewWidth, viewHeight);
//        Rect meteringRect = calculateTapArea(eventX, eventY, 2.5f, viewWidth, viewHeight);

        if (AppResultReceiver.FOCUS_AREA_TYPE.equals("point")) {
            AppResultReceiver.touchFocusXScale = eventX / display.getWidth();
            AppResultReceiver.touchFocusYScale = eventY / display.getHeight();
        } else {
            AppResultReceiver.touchFocusXScale = 0.5;
            AppResultReceiver.touchFocusYScale = 0.5;
        }

        String _currentFocusMode = "";
        try {
            Camera.Parameters params = mCamera.getParameters();
            _currentFocusMode = params.getFocusMode();

            // need use Auto Focus
            params.setFocusMode(FOCUS_MODE_AUTO);
            //params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            mCamera.setParameters(params);
        } catch (Exception e) {
            Log.i(TAG, "camera.setParameters error 12:" + e.getMessage());
        }

        //Camera.Parameters params = mCamera.getParameters();
        try {
            Camera.Parameters params = mCamera.getParameters();
            if (params.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(getRect(eventX, eventY), 1000));
                params.setFocusAreas(focusAreas);
            } else {
                Log.i(TAG, "focus areas not supported");
            }

            mCamera.setParameters(params);
        } catch (Exception e) {
            Log.i(TAG, "camera.setParameters error 13:" + e.getMessage());
        }

        final String currentFocusMode = _currentFocusMode;
        try {

            if (AppResultReceiver.FOCUS_AREA_TYPE.equals("point"))
                mainActivity.setFocusingBorder((int) eventX, (int) eventY, true);

            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (AppResultReceiver.FOCUS_AREA_TYPE.equals("point")) {
                        if (success)
                            mainActivity.setFocusedBorder((int) eventX, (int) eventY, true);
                        else
                            mainActivity.setFocusingBorder((int) eventX, (int) eventY, true);
                    }
                    try {
                        Camera.Parameters params = camera.getParameters();
                        params.setFocusMode(currentFocusMode);
                        camera.setParameters(params);
                    } catch (Exception e) {
                        Log.i(TAG, "camera.setParameters error 14:" + e.getMessage());
                    }
                }
            });

            // need set AutoFocusMoveCallback
            camera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
                @Override
                public void onAutoFocusMoving(boolean start, Camera camera) {

                }
            });
        } catch (Exception e) {
            Log.i(TAG, "camera.autoFocus error:" + e.getMessage());
        }
        ishandleFocusMetering = false;
    }

    private void calculateCameraToPreviewMatrix() {
        if (mCamera == null)
            return;
        camera_to_preview_matrix.reset();
//        if( !using_android_l ) {
//            // Need mirror for front camera
//            boolean mirror = camera_controller.isFrontFacing();
//            camera_to_preview_matrix.setScale(mirror ? -1 : 1, 1);
//            // This is the value for android.hardware.Camera.setDisplayOrientation.
//            camera_to_preview_matrix.postRotate(camera_controller.getDisplayOrientation());
//        }
//        else {
        // unfortunately the transformation for Android L API isn't documented, but this seems to work for Nexus 6
        boolean mirror = false; // mCamera.isFrontFacing();
        camera_to_preview_matrix.setScale(1, mirror ? -1 : 1);
//        }
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        camera_to_preview_matrix.postScale(fullscreenRenderer.getWidth() / 2000f, fullscreenRenderer.getHeight() / 2000f);
        camera_to_preview_matrix.postTranslate(fullscreenRenderer.getWidth() / 2f, fullscreenRenderer.getHeight() / 2f);
    }

    private void calculatePreviewToCameraMatrix() {
        if (mCamera == null)
            return;
        calculateCameraToPreviewMatrix();
        if (!camera_to_preview_matrix.invert(preview_to_camera_matrix)) {
            Log.d(TAG, "calculatePreviewToCameraMatrix failed to invert matrix!?");
        }
    }

    private Rect getRect(float x, float y) {
        float[] coords = {x, y};

        calculatePreviewToCameraMatrix();
        preview_to_camera_matrix.mapPoints(coords);
        double focus_x = coords[0];
        double focus_y = coords[1];

        int focus_size = 50;
        Rect rect = new Rect();
        rect.left = (int) focus_x - focus_size;
        rect.right = (int) focus_x + focus_size;
        rect.top = (int) focus_y - focus_size;
        rect.bottom = (int) focus_y + focus_size;
        if (rect.left < -1000) {
            rect.left = -1000;
            rect.right = rect.left + 2 * focus_size;
        } else if (rect.right > 1000) {
            rect.right = 1000;
            rect.left = rect.right - 2 * focus_size;
        }
        if (rect.top < -1000) {
            rect.top = -1000;
            rect.bottom = rect.top + 2 * focus_size;
        } else if (rect.bottom > 1000) {
            rect.bottom = 1000;
            rect.top = rect.bottom - 2 * focus_size;
        }
        return rect;
    }

    public static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
//
//    public static Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
//        float focusAreaSize = 100;
//        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
//        int centerX = (int) (x / width * 2000 - 1000);
//        int centerY = (int) (y / height * 2000 - 1000);
//
//        int halfAreaSize = areaSize / 2;
//        RectF rectF = new RectF(clamp(centerX - halfAreaSize, -1000, 1000)
//                , clamp(centerY - halfAreaSize, -1000, 1000)
//                , clamp(centerX + halfAreaSize, -1000, 1000)
//                , clamp(centerY + halfAreaSize, -1000, 1000));
//        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
//    }
//
//    public static int clamp(int x, int min, int max) {
//        if (x > max) {
//            return max;
//        }
//        if (x < min) {
//            return min;
//        }
//        return x;
//    }

//    public void writeToFile(String outFilename, String msg, boolean append) {
//        BufferedWriter writer = null;
//        try {
//            Log.i("writeToFile", "JobQueueSaveHeatImageJob.onRun" + outFilename);
//            Log.i("writeToFile", "JobQueueSaveHeatImageJob.onRun" + msg);
//            File textFile = new File(outFilename);
//
//            writer = new BufferedWriter(new FileWriter(textFile, append));
//            writer.write(msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                // Close the writer regardless of what happens...
//                writer.close();
//            } catch (Exception e) {
//            }
//        }
//    }

    public void addCallback(final AppRTCCallback callback) {
        if (callback != null) {
            mCallback = callback;
        }
    }

    public void removeCallback() {
        mCallback = null;
    }
}
