package org.itri.woundcamrtc.job;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.MainActivity;
import org.itri.woundcamrtc.helper.USBShareTool;

import java.net.HttpURLConnection;
import java.net.URL;

public class JobQueueFindIPJob extends Job {
    private final String TAG = getClass().getSimpleName();
    private JobManager jobQueueManager = null;
    private MainActivity activity;

    public JobQueueFindIPJob(JobManager jobQueueManager, MainActivity activity) {
        super(new Params(Thread.NORM_PRIORITY - 4).groupBy("findIPJob"));
        this.jobQueueManager = jobQueueManager;
        this.activity = activity;
    }

    protected void finalize() {
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        AppResultReceiver.PING_DEVICE_IP1 = AppResultReceiver.getThermalBoardIP(activity);
        checkConnectStatus(AppResultReceiver.PING_DEVICE_IP1, 1000);
        checkConnectStatus(AppResultReceiver.PING_DEVICE_IP2, 1000);
        checkConnectStatus(AppResultReceiver.PING_DEVICE_IP3, 1000);
//        checkConnectStatus(AppResultReceiver.PING_DEVICE_IP4,1000);

        USBShareTool tool = new USBShareTool((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        //Log.v(TAG, "檢查到的設備數量=" + tool.getConnectedDevices().size());
        //Log.v(TAG, "目前delayMills= " + String.valueOf(activity.delayMills) );
        /*List<String> ipList = new ArrayList<String>();
        ipList.add("192.168.0.108");
        ipList.add("13.114.175.239");*/

        for (String deviceIP : tool.getConnectedDevices()) {
            Log.v(TAG, "ipAddress check " + deviceIP);
            if (deviceIP.startsWith(AppResultReceiver.PING_DEVICE_IP1)){
//            if ((deviceIP.startsWith(AppResultReceiver.CHECK_DEVICE_IP1) || deviceIP.startsWith(AppResultReceiver.CHECK_DEVICE_IP2)) && checkConnectStatus(deviceIP, 1000)) {

                Log.v(TAG, "ipAddress is our device " + deviceIP);
                AppResultReceiver.GET_IP_DELAY_MS = 60000;
                if ( !activity.iip.equals(deviceIP)) {
                    activity.iip = deviceIP;
                    activity.mtxtDownloader.start();
                    activity.mGetThermalHandler.removeCallbacks(activity.mGetThermalRunner);
                    activity.mGetThermalHandler.postDelayed(activity.mGetThermalRunner, 1);
                    //Log.v(TAG,"網頁連線成功");
                }
                break;
            }
        }
        //測試網址用
        /*if(checkConnectStatus("127.0.0.1")){
            activity.delayMills = 10000;
            Log.v(TAG,"網頁連線成功");
        }
        else{
            activity.delayMills = 3000;
            Log.v(TAG,"網頁連線失敗");
        }*/

        //jobQueueManager.addJobInBackground(new JobQueueBeepJob(jobQueueManager, "", activity.beepManager, 0));

//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                //      activity.menuImageButton_ShowData.setImageResource(R.mipmap.radar3);
//            }
//        });
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        //如果重試n次仍未成功，那麼就放棄任務，也會進入onCancel
        if (runCount == 1)
            return RetryConstraint.CANCEL;
        return RetryConstraint.RETRY;
    }

    //如果重試超過限定次數，會執行onCancel
    //如果使用者主動放棄此任務，也一樣進入onCancel
    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    public interface OnPreviewListener {
        void OnCancelListener();

        void OnAcceptedListener();
    }

    public boolean checkConnectStatus(String ipAddress, int timeout) {
        boolean status = false;
        try {
            //URL url = new URL("https://www.findjobstw.com/");
            URL url = new URL("http://" + ipAddress + ":9000/ipcam01/version?action=123");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            con.connect();
            Log.v(TAG, "目前responseCode = " + con.getResponseCode());
            if (con.getResponseCode() == 200) {
                Log.v(TAG, "Success code of the object is " + con.getResponseCode());
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }
}
