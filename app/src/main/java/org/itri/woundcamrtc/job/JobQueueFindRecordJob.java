package org.itri.woundcamrtc.job;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.CaseMgntActivity;
import org.itri.woundcamrtc.MainActivity;
import org.itri.woundcamrtc.R;
import org.itri.woundcamrtc.helper.XSslOkHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
import android.net.ConnectivityManager;
import static org.itri.woundcamrtc.AppResultReceiver.ZONE;
import static org.itri.woundcamrtc.AppResultReceiver.showToast;
//import okhttp3.logging.HttpLoggingInterceptor;

public class JobQueueFindRecordJob extends Job {
    private final String TAG = getClass().getSimpleName();
    private JobManager jobQueueManager = null;
    private CaseMgntActivity activity;
    private Activity Activity;
    String keyNo = "";
    int position = -1;
    private static OkHttpClient client = null;
    private Context mContext;
    public JobQueueFindRecordJob(JobManager jobQueueManager, CaseMgntActivity activity, String keyNo, int position) {
        super(new Params(Thread.NORM_PRIORITY - 8).groupBy("FindRecordJob"));
        this.jobQueueManager = jobQueueManager;
        this.activity = activity;
        this.keyNo = keyNo;
        this.position = position;
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
        ConnectivityManager mConnectivityManager = (ConnectivityManager) Activity.getSystemService(mContext.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if(mNetworkInfo!=null){
            postRequest(AppResultReceiver.DEFAULT_QRY_RECORD_PATH, keyNo, position);
        }else{
            showToast(mContext.getString(R.string.no_internet));
        }


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

    public void postRequest(String postURL, String keyNo, int position) {
        try {
            if (client == null) {
//                OkHttpClient.Builder builder = new OkHttpClient.Builder();
//                builder.connectTimeout(10, TimeUnit.SECONDS);
//                builder.readTimeout(120, TimeUnit.SECONDS);
//                //builder.addInterceptor(logging);
//                client = builder.build();
                client = (new XSslOkHttpClient(10,120)).getOkHttpClient();
            }

            JSONObject postData = new JSONObject();
            postData.put("userFormKeyNo", keyNo);
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("data", postData.toString())
                    .build();
            Request request = new Request.Builder()
                    .url(postURL)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            JSONObject result = new JSONObject(response.body().string());
            String success = result.getString("success");

            if(success.toLowerCase().equals("true")){
                JSONObject record = result.getJSONObject("record");
                JSONArray imgArr = result.getJSONArray("images");

                if(record != null){
                    activity.setRecordInfo(record, position);
                }

                if(imgArr.length() > 0){
                    for(int i=0; i<imgArr.length(); i++){
                        JSONObject obj = imgArr.getJSONObject(i);
                        activity.setWoundImage(obj, position, i, imgArr.length());
                        //activity.setWoundImage(obj.getString("itemId"), AppResultReceiver.DEFAULT_LOADIMG_PATH + obj.getString("url") + "&location=" + ZONE);
                    }
                }
            }else{

                showToast(mContext.getString(R.string.no_internet));
            }

        }
        catch (Exception e){

            e.printStackTrace();
        }
    }
    public void showToast(String context) {


            try {
                Toast toast = Toast.makeText(this.activity, context, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 250);
                toast.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

    }
}
