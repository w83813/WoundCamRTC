package org.itri.woundcamrtc.job;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.itri.woundcamrtc.CaseMgntActivity;
import org.itri.woundcamrtc.MainActivity;
import org.itri.woundcamrtc.helper.XSslOkHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.EventListener;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
//import okhttp3.logging.HttpLoggingInterceptor;
import org.itri.woundcamrtc.AppResultReceiver;

public class JobQueueFindPatientNoJob extends Job {
    private final String TAG = getClass().getSimpleName();
    private JobManager jobQueueManager = null;
    private MainActivity activity;
    String roleId = "";
    private static OkHttpClient client = null;


    public JobQueueFindPatientNoJob(JobManager jobQueueManager, MainActivity activity, String roleId) {
        super(new Params(Thread.NORM_PRIORITY - 5).groupBy("FindPatientNoJob"));
        this.jobQueueManager = jobQueueManager;
        this.activity = activity;
        this.roleId = roleId;
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    protected void finalize() {
        try {
            super.finalize();
        } catch (Throwable ee) {

            ee.printStackTrace();
        }
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {

        //doTestMask();
        long startTime = System.currentTimeMillis();
        postRequest(AppResultReceiver.DEFAULT_QRY_PATIENTNOLIST_PATH, roleId);
        long endTime = System.currentTimeMillis();
        Log.v(TAG, "task finish time : " + (endTime - startTime) / 1000 + "sec");
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
    

    public void postRequest(String postURL, String roleId) {
        try {
            if (client == null) {
                client = (new XSslOkHttpClient(10,120)).getOkHttpClient();
            }


            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("roleId", roleId)
                    .build();
            Request request = new Request.Builder()
                    .url(postURL)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            JSONObject result = new JSONObject(response.body().string());
            String success = result.getString("success");

            if(success.toLowerCase().equals("true")){
                JSONArray objList = result.getJSONArray("data");
                activity.setPatientNoList(objList);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
