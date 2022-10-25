//package org.itri.woundcamrtc;
//
//import android.content.Intent;
//import android.graphics.Matrix;
//import android.graphics.SurfaceTexture;
//import android.hardware.usb.UsbDevice;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.Surface;
//import android.view.TextureView;
//import android.view.View;
//import android.widget.Toast;
//
//import com.jiangdg.usbcamera.UVCCameraHelper;
//import com.serenegiant.common.BaseActivity;
//import com.serenegiant.usb.UVCCamera;
//import com.serenegiant.usb.widget.CameraViewInterface;
//
//import java.util.Deque;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Queue;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//import top.defaults.camera.CameraView;
//import top.defaults.camera.Photographer;
//import top.defaults.camera.PhotographerFactory;
//
//
//public class UVCViewActivity extends BaseActivity {
//
//    private final String TAG = getClass().getSimpleName();
//
//    private UVCViewActivity mActivity;
//
//    public boolean isUVCPreview0 = false;
//    public boolean isUVCRequest0 = false;
//    public TextureView mUVCCameraView0;
//
////    public boolean isUVCPreview1 = false;
////    public boolean isUVCRequest1 = false;
////    public CameraViewInterface mUVCCameraView1;
//
//    public UVCCameraHelper mUVCCameraHelper = null;
//    public int mUVCDeviceFilterXmlId = 0;
//
//    private Photographer photographer0;
////    private Photographer photographer1;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_uvcview);
//        mActivity = this;
//        AppResultReceiver.initOrientation(mActivity);
//
//
//        if (AppResultReceiver.isMultiCam) {
//            TextureView preview0 = findViewById(R.id.cdpreview_sub);
//            photographer0 = PhotographerFactory.createPhotographerWithCamera2(this, preview0, 1);
////            photographer0.setFacing(1);
//
////        TextureView preview1 = findViewById(R.id.camera_view1);
////        photographer1 = PhotographerFactory.createPhotographerWithCamera2(this, preview1);
////        photographer1.setFacing(1);
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (AppResultReceiver.isUvcDevice) {
//            onInitUVCSurface();
//            onInitUVCCamera();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (AppResultReceiver.isUvcDevice) {
//            onResumeUVCCamera();
//        }
//
//        if (AppResultReceiver.isMultiCam) {
//            photographer0.startPreview();
////        photographer1.startPreview();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (AppResultReceiver.isUvcDevice) {
//            onPauseUVCCamera();
//        }
//
//        if (AppResultReceiver.isMultiCam) {
//            photographer0.stopPreview();
////        photographer1.stopPreview();
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (AppResultReceiver.isUvcDevice) {
//            onDestroyUVCCamera();
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    public void showToast(String context) {
//        runOnUiThread(new Runnable() {
//            public void run() {
//                try {
//                    Toast toast = Toast.makeText(UVCViewActivity.this, context, Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 250);
//                    toast.show();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//    }
////
////    private void initUVCSurface() {
//////        try {
//////            Thread.sleep(300);
//////        } catch (Exception ex){}
////        mUVCDeviceFilterXmlId= R.xml.uvcdevice_filter;
////        com.serenegiant.usb.widget.UVCCameraTextureView uvcCameraView0 = (com.serenegiant.usb.widget.UVCCameraTextureView)findViewById(R.id.uvc_camera_0);
////        uvcCameraView0.setLayerType(com.serenegiant.usb.widget.UVCCameraTextureView.LAYER_TYPE_SOFTWARE, null);
////        uvcCameraView0.setOnClickListener(new View.OnClickListener() {
////            public void onClick(View v) {
////            }
////        });
////        mUVCCameraView0 = (CameraViewInterface) findViewById(R.id.uvc_camera_0);
////        mUVCCameraView0.setCallback(mUVCCameraCallback0);
////
////        com.serenegiant.usb.widget.UVCCameraTextureView uvcCameraView1 = (com.serenegiant.usb.widget.UVCCameraTextureView)findViewById(R.id.uvc_camera_1);
////        uvcCameraView1.setLayerType(com.serenegiant.usb.widget.UVCCameraTextureView.LAYER_TYPE_SOFTWARE, null);
////        uvcCameraView1.setOnClickListener(new View.OnClickListener() {
////            public void onClick(View v) {
////            }
////        });
////        mUVCCameraView1 = (CameraViewInterface) findViewById(R.id.uvc_camera_1);
////        mUVCCameraView1.setCallback(mUVCCameraCallback1);
////    }
////
////    private void initUVCCamera() {
////        // 初始化引擎
////        mUVCCameraHelper = new UVCCameraHelper();
////        mUVCCameraHelper.initUSBMonitor(mActivity, mUVCCameraListener, mUVCDeviceFilterXmlId);
////        mUVCCameraHelper.setCameraView(0, mUVCCameraView0, 160, 120, 9, UVCCamera.PIXEL_FORMAT_YUV420SP);
////        mUVCCameraHelper.setCameraView(1, mUVCCameraView1, 160, 120, 10, UVCCamera.FRAME_FORMAT_MJPEG);
////    }
////
////    protected void resumeUVCCamera() {
////        //恢復Camera預覽
////        if (mUVCCameraView0 != null) {
////            mUVCCameraView0.onResume();
////            if (mUVCCameraHelper==null){
////                initUVCCamera();
////            }
////            mUVCCameraHelper.startPreview(0, mUVCCameraView0);
////        }
////        if (mUVCCameraView1 != null) {
////            mUVCCameraView1.onResume();
////            if (mUVCCameraHelper==null){
////                initUVCCamera();
////            }
////            mUVCCameraHelper.startPreview(1, mUVCCameraView1);
////        }
////
////        // 註冊USB事件廣播監聽器
////        if (mUVCCameraHelper != null) {
////            mUVCCameraHelper.registerUSB();
////        }
////    }
////
////
////    private CameraViewInterface.Callback mUVCCameraCallback0 = new CameraViewInterface.Callback() {
////        @Override
////        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
////            if (!isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(0)) {
////                mUVCCameraHelper.startPreview(0, (com.serenegiant.usb.widget.CameraViewInterface) mUVCCameraView0);
////                isUVCPreview0 = true;
////            }
////        }
////
////        @Override
////        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
////        }
////
////        @Override
////        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
////            if (isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(0)) {
////                mUVCCameraHelper.stopPreview(0);
////                isUVCPreview0 = false;
////            }
////        }
////    };
////
////    private CameraViewInterface.Callback mUVCCameraCallback1 = new CameraViewInterface.Callback() {
////        @Override
////        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
////            if (!isUVCPreview1 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(1)) {
////                mUVCCameraHelper.startPreview(1, (com.serenegiant.usb.widget.CameraViewInterface) mUVCCameraView1);
////                isUVCPreview1 = true;
////            }
////        }
////
////        @Override
////        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
////        }
////
////        @Override
////        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
////            if (isUVCPreview1 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(1)) {
////                mUVCCameraHelper.stopPreview(1);
////                isUVCPreview1 = false;
////            }
////        }
////    };
////
////
////
////    private UVCCameraHelper.OnMyDevConnectListener mUVCCameraListener = new UVCCameraHelper.OnMyDevConnectListener() {
////        // 插入USB裝置
////        @Override
////        public void onAttachDev(int index, UsbDevice device) {
////            if (mUVCCameraHelper == null || mUVCCameraHelper.getUsbDeviceCount(mUVCDeviceFilterXmlId) == 0) {
////                //showShortMsg("未檢測到USB攝像頭裝置");
////                return;
////            }
////            // 請求開啟攝像頭
////            if (index == 0) {
////                if (!isUVCRequest0) {
////                    isUVCRequest0 = true;
////                    if (mUVCCameraHelper != null) {
////                        mUVCCameraHelper.requestPermission(device);
////                    }
////                }
////            } else {
////                if (!isUVCRequest1) {
////                    isUVCRequest1 = true;
////                    if (mUVCCameraHelper != null) {
////                        mUVCCameraHelper.requestPermission(device);
////                    }
////                }
////            }
////        }
////
////        // 拔出USB裝置
////        @Override
////        public void onDettachDev(int index, UsbDevice device) {
////            if (index == 0) {
////                if (isUVCRequest0) {
////                    // 關閉攝像頭
////                    isUVCRequest0 = false;
////                    mUVCCameraHelper.closeCamera(0);
////                    //showShortMsg(device.getDeviceName()+"已撥出");
////                }
////            } else {
////                if (isUVCRequest1) {
////                    // 關閉攝像頭
////                    isUVCRequest1 = false;
////                    mUVCCameraHelper.closeCamera(1);
////                    //showShortMsg(device.getDeviceName()+"已撥出");
////                }
////            }
////        }
////
////        // 連線USB裝置成功
////        @Override
////        public void onConnectDev(int index, UsbDevice device, boolean isConnected) {
////            if (!isConnected) {
////                //showShortMsg("連線失敗，請檢查解析度引數是否正確");
////                if (index == 0) {
////                    isUVCPreview0 = false;
////                    if (!isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(0)) {
////                        mUVCCameraHelper.startPreview(0, (com.serenegiant.usb.widget.CameraViewInterface) mUVCCameraView0);
////                        isUVCPreview0 = true;
////                    }
////                } else {
////                    isUVCPreview1 = false;
////                    if (!isUVCPreview1 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(1)) {
////                        mUVCCameraHelper.startPreview(1, (com.serenegiant.usb.widget.CameraViewInterface) mUVCCameraView1);
////                        isUVCPreview1 = true;
////                    }
////                }
////            } else {
////                if (index == 0)
////                    isUVCPreview0 = true;
////                else
////                    isUVCPreview1 = true;
////            }
////        }
////
////        // 與USB裝置斷開連線
////        @Override
////        public void onDisConnectDev(int index, UsbDevice device) {
////            //showShortMsg("連線失敗");
////            if (index == 0) {
////                if (isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(0)) {
////                    mUVCCameraHelper.stopPreview(0);
////                    isUVCPreview0 = false;
////                }
////            } else {
////                if (isUVCPreview1 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(1)) {
////                    mUVCCameraHelper.stopPreview(1);
////                    isUVCPreview1 = false;
////                }
////            }
////        }
////
////        @Override
////        public void onCancel(int index, UsbDevice device) {
////            Toast toast = Toast.makeText(mActivity, "連線失敗", Toast.LENGTH_SHORT);
////            toast.show();
////        }
////    };
////
////
////
////    private UVCCameraHelper.OnMyDevConnectListener mUVCCameraListener = new UVCCameraHelper.OnMyDevConnectListener() {
////        // 插入USB裝置
////        @Override
////        public void onAttachDev(int index, UsbDevice device) {
////            if (mUVCCameraHelper == null || mUVCCameraHelper.getUsbDeviceCount(mUVCDeviceFilterXmlId) == 0) {
////                //showShortMsg("未檢測到USB攝像頭裝置");
////                return;
////            }
////            // 請求開啟攝像頭
////            if (index==0) {
////                if (!isUVCRequest0) {
////                    isUVCRequest0 = true;
////                    if (mUVCCameraHelper != null) {
////                        mUVCCameraHelper.requestPermission(device);
////                    }
////                }
////            } else {
//////                if (!isUVCRequest1) {
//////                    isUVCRequest1 = true;
//////                    if (mUVCCameraHelper != null) {
//////                        mUVCCameraHelper.requestPermission(device);
//////                    }
//////                }
////            }
////        }
////
////        // 拔出USB裝置
////        @Override
////        public void onDettachDev(int index, UsbDevice device) {
////            if (index==0) {
////                if (isUVCRequest0) {
////                    // 關閉攝像頭
////                    isUVCRequest0 = false;
////                    mUVCCameraHelper.closeCamera(0);
////                    //showShortMsg(device.getDeviceName()+"已撥出");
////                }
////            } else {
//////                if (isUVCRequest1) {
//////                    // 關閉攝像頭
//////                    isUVCRequest1 = false;
//////                    mUVCCameraHelper.closeCamera(1);
//////                    //showShortMsg(device.getDeviceName()+"已撥出");
//////                }
////            }
////        }
////
////        // 連線USB裝置成功
////        @Override
////        public void onConnectDev(int index, UsbDevice device, boolean isConnected) {
////            if (!isConnected) {
////                //showShortMsg("連線失敗，請檢查解析度引數是否正確");
////                if (index==0) {
////                    isUVCPreview0 = false;
////                    if (!isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(0)) {
////                        mUVCCameraHelper.startPreview(0, mUVCCameraView0);
////                        isUVCPreview0 = true;
////                    }
////                }else {
//////                    isUVCPreview1 = false;
//////                    if (!isUVCPreview1 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(1)) {
//////                        mUVCCameraHelper.startPreview(1, (com.serenegiant.usb.widget.CameraViewInterface) mUVCCameraView1);
//////                        isUVCPreview1 = true;
//////                    }
////                }
////            } else {
////                if (index==0)
////                    isUVCPreview0 = true;
//////                else
//////                    isUVCPreview1 = true;
////            }
////        }
////
////        // 與USB裝置斷開連線
////        @Override
////        public void onDisConnectDev(int index, UsbDevice device) {
////            //showShortMsg("連線失敗");
////            if (index ==0) {
////                if (isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(0)) {
////                    mUVCCameraHelper.stopPreview(0);
////                    isUVCPreview0 = false;
////                }
////            } else {
//////                if (isUVCPreview1 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(1)) {
//////                    mUVCCameraHelper.stopPreview(1);
//////                    isUVCPreview1 = false;
//////                }
////            }
////        }
////
////        @Override
////        public void onCancel(int index, UsbDevice device) {
////            mActivity.showToast("連線失敗");
////        }
////    };
////
////    private CameraViewInterface.Callback mUVCCameraCallback0 = new CameraViewInterface.Callback() {
////        @Override
////        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
////            if (!isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(0)) {
////                mUVCCameraHelper.startPreview(0,   mUVCCameraView0);
////                isUVCPreview0 = true;
////            }
////        }
////
////        @Override
////        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
////        }
////
////        @Override
////        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
////            if (isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(0)) {
////                mUVCCameraHelper.stopPreview(0);
////                isUVCPreview0 = false;
////            }
////        }
////    };
////
//////    private CameraViewInterface.Callback mUVCCameraCallback1 = new CameraViewInterface.Callback() {
//////        @Override
//////        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
//////            if (!isUVCPreview1 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(1)) {
//////                mUVCCameraHelper.startPreview(1, (com.serenegiant.usb.widget.CameraViewInterface) mUVCCameraView1);
//////                isUVCPreview1 = true;
//////            }
//////        }
//////
//////        @Override
//////        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
//////        }
//////
//////        @Override
//////        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
//////            if (isUVCPreview1 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(1)) {
//////                mUVCCameraHelper.stopPreview(1);
//////                isUVCPreview1 = false;
//////            }
//////        }
//////    };
////
////    private void onInitUVCSurface() {
//////        try {
//////            Thread.sleep(300);
//////        } catch (Exception ex){}
////        //mUVCDeviceFilterXmlId= R.xml.uvcdevice_filter;
////        com.serenegiant.usb.widget.UVCCameraTextureView uvcCameraView0 = (com.serenegiant.usb.widget.UVCCameraTextureView) findViewById(R.id.uvccamera_view0);
//////        uvcCameraView0.setLayerType(com.serenegiant.usb.widget.UVCCameraTextureView.LAYER_TYPE_SOFTWARE, null);
////        uvcCameraView0.setOnClickListener(new View.OnClickListener() {
////            public void onClick(View v) {
////            }
////        });
//////        mUVCCameraView0 = (CameraViewInterface) findViewById(R.id.uvccamera_view0);
//////        mUVCCameraView0.setCallback(mUVCCameraCallback0);
////
//////        com.serenegiant.usb.widget.UVCCameraTextureView uvcCameraView1 = (com.serenegiant.usb.widget.UVCCameraTextureView) findViewById(R.id.uvccamera_view1);
////////        uvcCameraView1.setLayerType(com.serenegiant.usb.widget.UVCCameraTextureView.LAYER_TYPE_SOFTWARE, null);
//////        uvcCameraView1.setOnClickListener(new View.OnClickListener() {
//////            public void onClick(View v) {
//////            }
//////        });
//////        mUVCCameraView1 = (CameraViewInterface) findViewById(R.id.uvccamera_view1);
//////        mUVCCameraView1.setCallback(mUVCCameraCallback1);
////    }
////
////    private void onInitUVCCamera() {
////        // 初始化引擎
////        mUVCDeviceFilterXmlId= R.xml.uvcdevice_filter;
////        mUVCCameraHelper = new UVCCameraHelper();
////        mUVCCameraHelper.initUSBMonitor(mActivity, mUVCCameraListener, mUVCDeviceFilterXmlId);
////        mUVCCameraHelper.setCameraView(0, mUVCCameraView0, 160, 120, 9, UVCCamera.PIXEL_FORMAT_YUV420SP);
//////        mUVCCameraHelper.setCameraView(1, mUVCCameraView1, 160, 120, 10, UVCCamera.FRAME_FORMAT_MJPEG);
////
////        // 註冊USB事件廣播監聽器
////        if (mUVCCameraHelper != null) {
////            mUVCCameraHelper.registerUSB();
////        }
////    }
////
////    protected void onStartUVCCamera() {
////        if (mUVCCameraView0 != null) {
////            if (mUVCCameraHelper == null) {
////                onInitUVCCamera();
////            }
//////            mUVCCameraView0.onResume();
////        }
//////        if (mUVCCameraView1 != null) {
//////            if (mUVCCameraHelper == null) {
//////                onInitUVCCamera();
//////            }
//////            mUVCCameraView1.onResume();
//////        }
////    }
////
////    protected void onResumeUVCCamera() {
////        if (mUVCCameraView0 != null) {
////            mUVCCameraHelper.startPreview(0, mUVCCameraView0);
////            isUVCPreview0 = true;
////        }
////
//////        if (mUVCCameraView1 != null) {
//////            mUVCCameraHelper.startPreview(1, mUVCCameraView1);
//////            isUVCPreview1 = true;
//////        }
////    }
////
////    protected void onPauseUVCCamera() {
////        if (mUVCCameraView0 != null) {
////            mUVCCameraHelper.stopPreview(0);
////            isUVCPreview0 = false;
////        }
////
//////        if (mUVCCameraView1 != null) {
//////            mUVCCameraHelper.stopPreview(1);
//////            isUVCPreview1 = false;
//////        }
////    }
////
////    protected void onStopUVCCamera() {
////        if (mUVCCameraView0 != null) {
//////            mUVCCameraView0.onPause();
////        }
////
//////        if (mUVCCameraView1 != null) {
//////            mUVCCameraView1.onPause();
//////        }
////    }
////
////    public void onDestroyUVCCamera() {
////        if (mUVCCameraHelper != null) {
////            mUVCCameraHelper.unregisterUSB();
////        }
////
////        isUVCPreview0 = false;
////        isUVCRequest0 = false;
////        mUVCCameraHelper.release(0);
//////        mUVCCameraView0.setCallback(null);
////
//////        isUVCPreview1 = false;
//////        isUVCRequest1 = false;
//////        mUVCCameraHelper.release(1);
////////        try {
////////            Thread.sleep(100);
////////        } catch (Exception ex) {
////////        }
////        mUVCCameraHelper = null;
////    }
//
//    private UVCCameraHelper.OnMyDevConnectListener mUVCCameraListener = new UVCCameraHelper.OnMyDevConnectListener() {
//        // 插入USB裝置
//        @Override
//        public void onAttachDev(int index, UsbDevice device) {
//            if (mUVCCameraHelper == null || mUVCCameraHelper.getUsbDeviceCount(mUVCDeviceFilterXmlId) == 0) {
//                //showShortMsg("未檢測到USB攝像頭裝置");
//                return;
//            }
//            // 請求開啟攝像頭
//            if (!isUVCRequest0) {
//                isUVCRequest0 = true;
//                if (mUVCCameraHelper != null) {
//                    mUVCCameraHelper.requestPermission(device);
//                }
//            }
//        }
//
//        // 拔出USB裝置
//        @Override
//        public void onDettachDev(int index, UsbDevice device) {
//            if (isUVCRequest0) {
//                // 關閉攝像頭
//                isUVCRequest0 = false;
//                mUVCCameraHelper.closeCamera(AppResultReceiver.uvcCameraIndex);
//                //showShortMsg(device.getDeviceName()+"已撥出");
//            }
//        }
//
//        // 連線USB裝置成功
//        @Override
//        public void onConnectDev(int index, UsbDevice device, boolean isConnected) {
//            if (!isConnected) {
//                //showShortMsg("連線失敗，請檢查解析度引數是否正確");
//                isUVCPreview0 = false;
//                if (!isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex)) {
//                    mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, mUVCCameraView0, null, 2);
//                    isUVCPreview0 = true;
//                }
//            } else {
//                isUVCPreview0 = true;
//            }
//        }
//
//        // 與USB裝置斷開連線
//        @Override
//        public void onDisConnectDev(int index, UsbDevice device) {
//            //showShortMsg("連線失敗");
//            if (isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex)) {
//                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, null, null, 3);
//                isUVCPreview0 = false;
//            }
//        }
//
//        @Override
//        public void onCancel(int index, UsbDevice device) {
//            mActivity.showToast("連線失敗");
//        }
//    };
//
////    private CameraViewInterface.Callback mUVCCameraCallback = new CameraViewInterface.Callback() {
////        @Override
////        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
////            if (!isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex)) {
////                mUVCCameraHelper.startPreview(AppResultReceiver.uvcCameraIndex, (com.serenegiant.usb.widget.CameraViewInterface) mUVCCameraView0);
////                isUVCPreview0 = true;
////            }
////        }
////
////        @Override
////        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
////        }
////
////        @Override
////        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
////            if (isUVCPreview0 && mUVCCameraHelper != null && mUVCCameraHelper.isCameraOpened(AppResultReceiver.uvcCameraIndex)) {
////                mUVCCameraHelper.stopPreview(AppResultReceiver.uvcCameraIndex);
////                isUVCPreview0 = false;
////            }
////        }
////    };
//
//    public void onInitUVCSurface() {
////        try {
////            Thread.sleep(300);
////        } catch (Exception ex){}
//        //mUVCDeviceFilterXmlId= R.xml.uvcdevice_filter;
//
//        if (AppResultReceiver.isUvcDevice) {
//            TextureView uvcCameraView0 = (TextureView) findViewById(R.id.uvccamera_view_sub);
//            if (uvcCameraView0 != null) {
//                //https://www.codota.com/code/java/methods/android.view.TextureView/setTransform
////                Matrix matrix = new Matrix();
////                matrix.postRotate(-90, 60/2, 80/2);
////                uvcCameraView0.setTransform(matrix);
//                uvcCameraView0.setVisibility(View.VISIBLE);
//                //        uvcCameraView0.setLayerType(com.serenegiant.usb.widget.UVCCameraTextureView.LAYER_TYPE_SOFTWARE, null);
////                uvcCameraView0.setOnClickListener(new View.OnClickListener() {
////                    public void onClick(View v) {
////
////                    }
////                });
//                mUVCCameraView0 = uvcCameraView0;
////                mUVCCameraView0 = (CameraViewInterface) findViewById(R.id.uvccamera_view0);
////                mUVCCameraView0.setCallback(mUVCCameraCallback);
//            }
//        }
//    }
//
//    public void onInitUVCCamera() {
//        if (AppResultReceiver.isUvcDevice) {
//            // 初始化引擎
//            mUVCDeviceFilterXmlId = R.xml.uvcdevice_filter;
//
//            if (mUVCCameraHelper == null) {
//                UVCCamera.DEFAULT_BANDWIDTH = 1.0f;
//                mUVCCameraHelper = new UVCCameraHelper();
//                mUVCCameraHelper.initUSBMonitor(mActivity, mUVCCameraListener, mUVCDeviceFilterXmlId);
//                //mUVCCameraHelper.setCameraView(AppResultReceiver.uvcCameraIndex, mUVCCameraView0, 160, 120, 9, UVCCamera.PIXEL_FORMAT_YUV420SP);
//                mUVCCameraHelper.setCameraView(AppResultReceiver.uvcCameraIndex, mUVCCameraView0, 160, 120, 9, UVCCamera.PIXEL_FORMAT_Y16);
//                // 註冊USB事件廣播監聽器
//                mUVCCameraHelper.registerUSB();
//            }
//        }
//    }
//
////    public void onStartUVCCamera() {
////        if (AppResultReceiver.isUvcDevice) {
////            onInitUVCCamera();
////            if (mUVCCameraView0 != null) {
//////                mUVCCameraView0.onResume();
////            }
////        }
////    }
//
//    public void onResumeUVCCamera() {
//        if (AppResultReceiver.isUvcDevice) {
//            if (mUVCCameraView0 != null && mUVCCameraHelper != null) {
//
////                if (isUVCPreview0 != true && mUVCCameraView0.isAvailable()) {
////                    mUVCCameraHelper.startPreview(AppResultReceiver.uvcCameraIndex, mUVCCameraView0);
////                    isUVCPreview0 = true;
////                }
//
//                mUVCCameraView0.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//                    @Override
//                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//                        if (AppResultReceiver.isUvcDevice) {
//                            if (mUVCCameraView0 != null && mUVCCameraHelper != null) {
//                                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, mUVCCameraView0, null, 2);
//                                isUVCPreview0 = true;
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//
//                    }
//
//                    @Override
//                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                        if (AppResultReceiver.isUvcDevice) {
//                            if (mUVCCameraView0 != null && mUVCCameraHelper != null) {
//                                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, null, null, 3);
//                                isUVCPreview0 = false;
//                            }
//                        }
//                        return false;
//                    }
//
//                    @Override
//                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//                    }
//                });
//            }
//        }
//    }
//
//    public void onPauseUVCCamera() {
//        if (AppResultReceiver.isUvcDevice) {
//            if (mUVCCameraView0 != null && mUVCCameraHelper != null) {
//                mUVCCameraView0.setSurfaceTextureListener(null);
//                mUVCCameraHelper.openOrStartOrStopOrClosePreview(AppResultReceiver.uvcCameraIndex, null, null, 3);
//                isUVCPreview0 = false;
//            }
//        }
//    }
//
////    protected void onStopUVCCamera() {
////        if (AppResultReceiver.isUvcDevice) {
////            if (mUVCCameraView0 != null) {
//////                mUVCCameraView0.onPause();
////            }
////        }
////    }
//
//    public void onDestroyUVCCamera() {
//        if (AppResultReceiver.isUvcDevice) {
//            if (mUVCCameraHelper != null) {
//                mUVCCameraHelper.unregisterUSB();
//                mUVCCameraHelper.release(AppResultReceiver.uvcCameraIndex);
//            }
//            isUVCPreview0 = false;
//            isUVCRequest0 = false;
////            mUVCCameraView0.setCallback(null);
////        try {
////            Thread.sleep(100);
////        } catch (Exception ex) {
////        }
//            mUVCCameraHelper = null;
//        }
//    }
//
//}