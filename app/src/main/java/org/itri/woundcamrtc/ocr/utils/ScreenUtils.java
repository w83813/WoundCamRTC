package org.itri.woundcamrtc.ocr.utils;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import org.itri.woundcamrtc.AppResultReceiver;


/**
 * ScreenUtils
 */
public class ScreenUtils {

    private ScreenUtils() {
        throw new AssertionError();
    }

    public static int getScreenWidth() {
        Context context = AppResultReceiver.mMainActivity;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getScreenHeight() {
        Context context = AppResultReceiver.mMainActivity;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static Point getScreenResolution(Context context) {

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        int width = display.getWidth();
        int height = display.getHeight();
        return new Point(width, height);
    }

    public static int getUIBarHeight(){
        Rect rectangle = new Rect();
        Window window = AppResultReceiver.mMainActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight= contentViewTop - statusBarHeight;
        return titleBarHeight;
    }
}
