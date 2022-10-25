package org.itri.woundcamrtc.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class USBShareTool {
    public final static String TAG = "USBShareTool";
    private ConnectivityManager connManager;

    public USBShareTool(ConnectivityManager connManager) {
        this.connManager = connManager;
    }

    public boolean isEnable() {
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info == null) {
            return false;
        } else {
            return true;
        }
    }

    // 取得所有連線的裝置IP
    public ArrayList<String> getConnectedDevices() {
        //Map<String, String> devices = new HashMap<String, String>();
        ArrayList<String> devices = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            br.readLine();
            String line = null;
            while ((line = br.readLine()) != null) {
                Log.i(TAG, "FILE: " + line);
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    String mac = splitted[3];

                    devices.add(ip);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Get ARP List error: ", ex);
            ex.printStackTrace();
        }
        return devices;
    }

}
