package org.itri.woundcamrtc;


import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.github.yoojia.anyversion.AnyVersion;
import com.github.yoojia.anyversion.Version;
import com.github.yoojia.anyversion.VersionParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.webrtc.Logging;

public class WoundCamRTC extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            public void uncaughtException(Thread t, Throwable e) {
//                e.printStackTrace();
//                uncaughtExceptionHandler.uncaughtException(t,e);
//            }
//        });
//
//        Log.d("App", "WoundCamRTC init ...");
//        AnyVersion.init(this, new VersionParser() {
//            @Override
//            public Version onParse(String response) {
//                final JSONTokener tokener = new JSONTokener(response);
//                try {
//                    JSONObject json = (JSONObject) tokener.nextValue();
//                    return new Version(
//                            "發現新版程式",
//                            "更新前請先將資料上傳完畢。要進行下載更新嗎?",
//                            json.getString("updateURL"),
//                            json.getInt("versionCode")
//                    );
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        });
    }
}

