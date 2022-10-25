package org.itri.woundcamrtc.helper;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class ShellHelper {

    private final static String TAG = ShellHelper.class.getSimpleName();


    public static void enableDevice() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            DataOutputStream dos = new DataOutputStream(process.getOutputStream());
            dos.writeBytes("echo \"1\" > /dev/usb1_enable\n");
            dos.writeBytes("echo \"1\" > /dev/wm_3d_5v\n");
            dos.writeBytes("exit\n");
            dos.flush();
            Log.d(TAG, "enableDevice with su exit");
        } catch (Exception ex) {
            //若沒有su時該做的事
            Log.d(TAG, "enableDevice with su error");
            //ex.printStackTrace();
            enableDevice_usb1();
            enableDevice_wm_3d_5v();
        }
    }

    public static void disableDevice() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            DataOutputStream dos = new DataOutputStream(process.getOutputStream());
            dos.writeBytes("echo \"0\" > /dev/usb1_enable\n");
            dos.writeBytes("echo \"0\" > /dev/wm_3d_5v\n");
            dos.writeBytes("exit\n");
            dos.flush();
            Log.d(TAG, "disableDevice with su exit");
        } catch (Exception ex) {
            //若沒有su時該做的事
            Log.d(TAG, "disableDevice with su error");
            //ex.printStackTrace();
            disableDevice_usb1();
            disableDevice_wm_3d_5v();
        }
    }

    public static void enableDevice_usb1() {
        try {
            Process process = Runtime.getRuntime().exec("echo \"1\" > /dev/usb1_enable\n");
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            Log.d(TAG, "enableDevice2 without su exit");
        } catch (Exception e) {
            //若沒有su時該做的事
            Log.d(TAG, "enableDevice2 without su error");
            e.printStackTrace();
        }
    }

    public static void disableDevice_usb1() {
        try {
            Process process = Runtime.getRuntime().exec("echo \"0\" > /dev/usb1_enable\n");
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            Log.d(TAG, "disableDevice_usb1 without su exit");
        } catch (Exception e) {
            //若沒有su時該做的事
            Log.d(TAG, "disableDevice_usb1 without su error");
            e.printStackTrace();
        }
    }

    public static void enableDevice_wm_3d_5v() {
        try {
            Process process = Runtime.getRuntime().exec("echo \"1\" > /dev/wm_3d_5v\n");
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            Log.d(TAG, "enableDevice_wm_3d_5v without su exit");
        } catch (Exception e) {
            //若沒有su時該做的事
            Log.d(TAG, "enableDevice_wm_3d_5v without su error");
            e.printStackTrace();
        }
    }

    public static void disableDevice_wm_3d_5v() {
        try {
            Process process = Runtime.getRuntime().exec("echo \"0\" > /dev/wm_3d_5v\n");
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            Log.d(TAG, "disableDevice_wm_3d_5v without su exit");
        } catch (Exception e) {
            //若沒有su時該做的事
            Log.d(TAG, "disableDevice_wm_3d_5v without su error");
            e.printStackTrace();
        }
    }

    public static void setpropDevice(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            Log.d(TAG, "setpropDevice_mpd100 without su exit");
        } catch (Exception e) {
            //若沒有su時該做的事
            Log.d(TAG, "setpropDevice_mpd100 without su error");
            e.printStackTrace();
        }
    }

    public static void runShell(){
        try {
            //在系統裡，一般應用程式受到UID的限制，僅能查看屬於自己的Log
            //若想蒐集完整的Log必須先使用su指令切換至root身份
            Process process = Runtime.getRuntime().exec("su");

            //透過Outputstream將指令輸出至Shell執行
            DataOutputStream dos = new DataOutputStream(process.getOutputStream());

            //透過getInputstream接收Shell回傳的資料，並放入BufferedReader
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));



            //將欲執行的指令寫入OutPutStream
            //write方法會先將資料暫存於Buffer(緩衝區)裡面
            //"\n"是換行，相當於在Shell按下Enter讓指令執行
            dos.writeBytes("logcat -v time -d |grep 'android.intent.action.'\n");

            //離開root身份
            dos.writeBytes("exit\n");

            //flush這個方法會強制將Stream的Buffer(緩衝區)裡暫存的資料發送出去
            //在這裡的意思就是開始執行上述寫好的指令
            dos.flush();

            StringBuilder mylog = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null){
                mylog.append(line);
                mylog.append("\n");
            }
        } catch (Exception e) {
            //若沒有su時該做的事
        }
    }
}
