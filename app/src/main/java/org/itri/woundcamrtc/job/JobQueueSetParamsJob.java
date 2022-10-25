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

public class JobQueueSetParamsJob extends Job {
    private final String TAG = getClass().getSimpleName();
    private JobManager jobQueueManager = null;
    private MainActivity activity;
    private String ipAddress = "";

    public JobQueueSetParamsJob(JobManager jobQueueManager, MainActivity activity, String iip) {
        super(new Params(Thread.NORM_PRIORITY - 3).groupBy("setParamsJob"));
        this.jobQueueManager = jobQueueManager;
        this.activity = activity;
        this.ipAddress = iip;
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
        checkConnectStatus(ipAddress, 1000);
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
            URL url = new URL("http://" + ipAddress + ":9000/ipcam01/version?action=123&thermal_type="+AppResultReceiver.getThermalType(activity));
            //URL url = new URL("http://" + ipAddress + ":9000/ipcam01/version?action=123");
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
