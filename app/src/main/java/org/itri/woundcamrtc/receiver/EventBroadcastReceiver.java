package org.itri.woundcamrtc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import org.itri.woundcamrtc.AppResultReceiver;

public class EventBroadcastReceiver extends BroadcastReceiver {
    protected Context context;

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String action = intent.getAction();

        if (Intent.ACTION_USER_PRESENT.equals(action)
                || Intent.ACTION_POWER_CONNECTED.equals(action)
                || Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
//            startPushService();
        }

        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            AppResultReceiver.mMainActivity.finish();
        }
    }

}
