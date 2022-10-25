package org.itri.woundcamrtc.ocr.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;


import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.MainActivity;
//import org.itri.woundcamrtc.ocr.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public final class CameraConfigurationManager {

    private static final String TAG = "CameraConfiguration";
    // 屏幕分辨率
    private Point screenResolution;
    // 相机分辨率
    private Point cameraResolution;

    public void initFromCameraParameters(Camera camera) {
        // 需要判断摄像头是否支持缩放
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.isZoomSupported()) {
            // 设置成最大倍数的1/10，基本符合远近需求
            parameters.setZoom(Math.min(parameters.getMaxZoom() / 10, 4));
            camera.setParameters(parameters);
        }
        if (parameters.getMaxNumFocusAreas() > 0) {
            List focusAreas = new ArrayList();
            Rect focusRect = new Rect(-800, -800, 800, 200);
            focusAreas.add(new Camera.Area(focusRect, 1000));
            parameters.setFocusAreas(focusAreas);
        }

        WindowManager manager = (WindowManager) AppResultReceiver.mMainActivity.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
//		Point theScreenResolution = getDisplaySize(display);
//		theScreenResolution = getDisplaySize(display);

        screenResolution = getDisplaySize(display);
        Log.i(TAG, "Screen resolution: " + screenResolution);

        /** 因为换成了竖屏显示，所以不替换屏幕宽高得出的预览图是变形的 */
        Point screenResolutionForCamera = new Point();
        screenResolutionForCamera.x = screenResolution.x;
        screenResolutionForCamera.y = screenResolution.y;

        if (screenResolution.x < screenResolution.y) {
            screenResolutionForCamera.x = screenResolution.y;
            screenResolutionForCamera.y = screenResolution.x;
        }

        CameraConfigurationUtils.MAX_PREVIEW_PIXELS = screenResolution.x * screenResolution.y + 1000;
        CameraConfigurationUtils.MIN_PREVIEW_PIXELS = screenResolution.x * screenResolution.y - 1000;
        cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolutionForCamera);
        Log.i(TAG, "Camera resolution x: " + cameraResolution.x);
        Log.i(TAG, "Camera resolution y: " + cameraResolution.y);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private Point getDisplaySize(final Display display) {
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (NoSuchMethodError ignore) {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        return point;
    }

    public void setDesiredCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

//		if (safeMode) {
//			Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
//		}

        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        camera.setParameters(parameters);

        Camera.Parameters afterParameters = camera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();
        if (afterSize != null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
            Log.w(TAG, "Camera said it supported preview size " + cameraResolution.x + 'x' + cameraResolution.y + ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
            cameraResolution.x = afterSize.width;
            cameraResolution.y = afterSize.height;
        }

        /** 设置相机预览为竖屏 */

        if (AppResultReceiver.IS_FOR_MIIS_MPDA)
            camera.setDisplayOrientation(270);
        else
            camera.setDisplayOrientation(90);
    }

    public Point getCameraResolution() {
        return cameraResolution;
    }

    public Point getScreenResolution() {
        return screenResolution;
    }

}
