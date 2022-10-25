package org.itri.woundcamrtc.helper;

import android.os.Build;
import android.util.Log;

import org.itri.woundcamrtc.AppResultReceiver;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServiceHelper {
    public SimpleDateFormat nowEvalTime = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");

    //把之前登入日期與設定時間 計算有無超過時間
    public boolean calcuteLoginTime(String date1, int period) throws ParseException {
        Boolean overTime = false;
        java.util.Date today = null;
        java.util.Date Loginday = null;
        Date date2 = nowEvalTime.parse(date1);
        today = nowEvalTime.parse(nowEvalTime.format(new Date()));
        Loginday = nowEvalTime.parse(nowEvalTime.format(date2));
        long calcutetoday = today.getTime();
        long calcuteLoginday = Loginday.getTime();

        if (Math.abs(calcutetoday - calcuteLoginday) >= period * 1000 * 60 * 60) {  //要試燈入時間超過 需要登入
            overTime = false;
        } else {
            overTime = true;

        }


        return overTime;
    }

    public static String getSerialNumber(String defaultSN) {
        String serialNumber = "";

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            if (!AppResultReceiver.IS_FOR_MIIS_MPDA) {
                // MPD100 get gsm.sn1 will crash
                serialNumber = (String) get.invoke(c, "gsm.sn1");
            }

            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ro.serialno");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = Build.SERIAL;

            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = defaultSN;
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = defaultSN;
        }

        return serialNumber;
    }

    public static String getBuildSerialNumber(String defaultSN) {
        String serialNumber;
        try {
            serialNumber = Build.SERIAL;
            if (serialNumber.equals(""))
                serialNumber = defaultSN;
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = defaultSN;
        }

        return serialNumber;
    }

    public static String getIpAddress(String defaultValue){
        InetAddress ip = null;
        try {
            ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static String getMacAddress(String defaultValue) {
        InetAddress ip = null;
        try {
            ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%s%02X", (i > 0 ? "-" : ""), mac[i]));
            }
            // System.out.println("Current MAC address : " + sb.toString());
            // Current MAC address : 38-2C-4A-B4-C3-24
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }
}
