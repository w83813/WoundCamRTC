package org.itri.woundcamrtc.ocr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;


import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.ocr.camera.CameraManager;
import org.itri.woundcamrtc.ocr.decode.CaptureActivityHandler;
import org.itri.woundcamrtc.ocr.decode.InactivityTimer;
import org.itri.woundcamrtc.ocr.core.RecognitionCallback;
import org.itri.woundcamrtc.ocr.core.RecognitionThread;
import org.itri.woundcamrtc.ocr.utils.Tools;
import org.itri.woundcamrtc.ocr.view.ImageDialog;
import org.itri.woundcamrtc.ocr.view.ScannerFinderView;

import org.itri.woundcamrtc.R;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;

public class ScannerActivity extends Activity implements Callback, Camera.PictureCallback, Camera.ShutterCallback {
    private ScannerActivity mActivity ;
    private CaptureActivityHandler mCaptureActivityHandler;
    private boolean mHasSurface;

    private InactivityTimer mInactivityTimer;
    private ScannerFinderView mQrCodeFinderView;
    private SurfaceView mSurfaceView;
    private ViewStub mSurfaceViewStub;
    //    private DecodeManager mDecodeManager = new DecodeManager();
    //private Switch switch1;
    private Button bt;
    public boolean isRequestPreviewFrame = false;
    private ProgressDialog progressDialog;
    private Bitmap bmp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON  //防止螢幕自動關閉
//                        | LayoutParams.FLAG_DISMISS_KEYGUARD    //解鎖螢幕
//                        | LayoutParams.FLAG_SHOW_WHEN_LOCKED    //螢幕鎖定時也可以顯示
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        setContentView(R.layout.ocr_activity_scanner);
        mActivity= this;

//        if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{CAMERA, READ_EXTERNAL_STORAGE}, 100);
//        } else {
        initView();
        initData();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            boolean permissionGranted = true;
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false;
                }
            }
            if (permissionGranted) {
                initView();
                initData();
            } else {
                // 无权限退出
                //finish();
            }
        }
    }

    private void initView() {


        mQrCodeFinderView = (ScannerFinderView) findViewById(R.id.qr_code_view_finder);
        mSurfaceViewStub = (ViewStub) findViewById(R.id.qr_code_view_stub);
        //switch1 = (Switch) findViewById(R.id.switch1);
        mHasSurface = false;

        bt = (Button) findViewById(R.id.bt);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt.setEnabled(false);
                //buildProgressDialog();
                //CameraManager.get().takeShot(ScannerActivity.this, ScannerActivity.this, ScannerActivity.this);
                isRequestPreviewFrame = true;
                CameraManager.get().cancelAutoFocus();
                CameraManager.get().requestAutoFocus(getCaptureActivityHandler(), R.id.auto_focus);
//                CameraManager.get().requestPreviewFrame(new Camera.PreviewCallback() {
//                    @Override
//                    public void onPreviewFrame(byte[] data, Camera camera) {
//                        if (data == null) {
//                            return;
//                        }
//                        mCaptureActivityHandler.onPause();
//                        buildProgressDialog();
//                        if (bmp != null) {
//                            bmp.recycle();
//                            bmp = null;
//                        }
//                        bmp = Tools.getFocusedBitmap2(mActivity, camera, data, getCropRect());
//
//                        Mat img = new Mat();
//                        Utils.bitmapToMat(bmp, img);
//                        RecognitionThread mRecognitionThread = new RecognitionThread(img, new RecognitionCallback() {
//
//                            @Override
//                            public void succeed(String result) {
//                                Message message = Message.obtain();
//                                message.what = 0;
//                                message.obj = result;
//                                mHandler.sendMessage(message);
//                            }
//
//                            @Override
//                            public void fail() {
//                                Message message = Message.obtain();
//                                message.what = 1;
//                                mHandler.sendMessage(message);
//                            }
//                        });
//
//                        Thread thread = new Thread(mRecognitionThread);
//                        thread.start();
//                    }
//                });

            }
        });

        Switch switch2 = (Switch) findViewById(R.id.switch2);
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CameraManager.get().setFlashLight(isChecked);
            }
        });


    }

    public void processPreviewFrame(byte[] data, Camera camera){
        if (data == null) {
            return;
        }
        mCaptureActivityHandler.onPause();
        buildProgressDialog();
        if (bmp != null) {
            bmp.recycle();
            bmp = null;
        }
        bmp = Tools.getFocusedBitmap2(mActivity, camera, data, getCropRect());

        Mat img = new Mat();
        Utils.bitmapToMat(bmp, img);
        RecognitionThread mRecognitionThread = new RecognitionThread(img, new RecognitionCallback() {

            @Override
            public void succeed(String result) {
                Message message = Message.obtain();
                message.what = 0;
                message.obj = result;
                mHandler.sendMessage(message);
            }

            @Override
            public void fail() {
                Message message = Message.obtain();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        });

        Thread thread = new Thread(mRecognitionThread);
        thread.start();
    }

    public Rect getCropRect() {
        return mQrCodeFinderView.getRect();
    }

//    public boolean isQRCode() {
//        return switch1.isChecked();
//    }

    private void initData() {
        mInactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mInactivityTimer != null) {
            CameraManager.init();
            initCamera();
        }
        hideKeyboard(AppResultReceiver.mMainActivity);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
    }

    private void initCamera() {
        if (null == mSurfaceView) {
            mSurfaceViewStub.setLayoutResource(R.layout.ocr_layout_surface_view);
            mSurfaceView = (SurfaceView) mSurfaceViewStub.inflate();
        }
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (mHasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            // 防止sdk8的设备初始化预览异常(可去除，本项目最小16)
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    protected void onPause() {
        if (mCaptureActivityHandler != null) {
            try {
                mCaptureActivityHandler.quitSynchronously();
                mCaptureActivityHandler = null;
                if (null != mSurfaceView && !mHasSurface) {
                    mSurfaceView.getHolder().removeCallback(this);
                }
                CameraManager.get().closeDriver();
            } catch (Exception e) {
                // 关闭摄像头失败的情况下,最好退出该Activity,否则下次初始化的时候会显示摄像头已占用.
                //finish();
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (null != mInactivityTimer) {
            mInactivityTimer.shutdown();
        }
        super.onDestroy();
    }


//    public void handleDecode(Result result) {
//        mInactivityTimer.onActivity();
//        if (null == result) {
//            mDecodeManager.showCouldNotReadQrCodeFromScanner(this, new DecodeManager.OnRefreshCameraListener() {
//                @Override
//                public void refresh() {
//                    restartPreview();
//                }
//            });
//        } else {
//            handleResult(result);
//        }
//    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            if (!CameraManager.get().openDriver(surfaceHolder)) {
                return;
            }
        } catch (IOException e) {
            // 基本不会出现相机不存在的情况
            Toast.makeText(this, getString(R.string.camera_not_found), Toast.LENGTH_SHORT).show();
            //finish();
            return;
        } catch (RuntimeException re) {
            re.printStackTrace();
            return;
        }
        mQrCodeFinderView.setVisibility(View.VISIBLE);
        findViewById(R.id.qr_code_view_background).setVisibility(View.GONE);
        if (mCaptureActivityHandler == null) {
            mCaptureActivityHandler = new CaptureActivityHandler(this);
        }
    }

    public void restartPreview() {
        if (null != mCaptureActivityHandler) {
            try {
                mCaptureActivityHandler.restartPreviewAndDecode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
    }

    public Handler getCaptureActivityHandler() {
        return mCaptureActivityHandler;
    }

//    private void handleResult(Result result) {
//        if (TextUtils.isEmpty(result.getText())) {
//            mDecodeManager.showCouldNotReadQrCodeFromScanner(this, new DecodeManager.OnRefreshCameraListener() {
//                @Override
//                public void refresh() {
//                    restartPreview();
//                }
//            });
//        } else {
//            Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
//            vibrator.vibrate(200L);
//            if (switch1.isChecked()) {
//                qrSucceed(result.getText());
//            } else {
//                phoneSucceed(result.getText(), result.getBitmap());
//            }
//        }
//    }

    {
        System.loadLibrary("tensorflow_inference");
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (data == null) {
            return;
        }
        mCaptureActivityHandler.onPause();
        buildProgressDialog();
        if (bmp != null) {
            bmp.recycle();
            bmp = null;
        }
        bmp = Tools.getFocusedBitmap(this, camera, data, getCropRect());

//        Point ScrRes = ScreenUtils.getScreenResolution(AppResultReceiver.mMainActivity);
//        Point CamRes = CameraConfigurationUtils.findBestPreviewSizeValue(camera.getParameters(), ScrRes);
//
//        Mat img = new Mat();
//        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//        if (CamRes.x > CamRes.y){
//            bitmap = Tools.rotateBitmap(bitmap, 90);
//        }
//        Utils.bitmapToMat(bitmap, img);
//        bitmap.recycle();
//
//
//        org.opencv.core.Rect rect = new org.opencv.core.Rect(CamRes.x * getCropRect().left / ScrRes.x, CamRes.y * getCropRect().top / ScrRes.y, CamRes.x * getCropRect().right / ScrRes.x, CamRes.y * getCropRect().bottom / ScrRes.y);
//        Mat mat = new Mat(img, rect);
//        img.release();
//
//        bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mat, bmp);

        Mat img = new Mat();
        Utils.bitmapToMat(bmp, img);
        RecognitionThread mRecognitionThread = new RecognitionThread(img, new RecognitionCallback() {

            @Override
            public void succeed(String result) {
                Message message = Message.obtain();
                message.what = 0;
                message.obj = result;
                mHandler.sendMessage(message);
            }

            @Override
            public void fail() {
                Message message = Message.obtain();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        });

        Thread thread = new Thread(mRecognitionThread);
        thread.start();
    }



    @Override
    public void onShutter() {
    }

//    private void qrSucceed(String result){
//        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.notification)
//                .setMessage(result)
//                .setPositiveButton(R.string.positive_button_confirm, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        restartPreview();
//                    }
//                })
//                .show();
//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                restartPreview();
//            }
//        });
//    }

    private void phoneSucceed(String result, Bitmap bitmap) {
        ImageDialog dialog = new ImageDialog(this);
        dialog.addBitmap(bitmap);
        dialog.addTitle(TextUtils.isEmpty(result) ? getResources().getString(R.string.no_context) : result);
        dialog.addResult(TextUtils.isEmpty(result) ? "" : result);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restartPreview();
            }
        });
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            bt.setEnabled(true);
            cancelProgressDialog();
            switch (msg.what) {
                case 0:
                    phoneSucceed((String) msg.obj, bmp);
                    break;
                case 1:
                    Toast.makeText(ScannerActivity.this, getResources().getString(R.string.cannt_ocr), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    public void buildProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage(getResources().getString(R.string.recogniting));
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    public void cancelProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}