package org.itri.woundcamrtc.job;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;
import android.widget.Toast;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.drew.lang.StringUtil;
import com.google.android.gms.common.util.IOUtils;
import com.google.webviewlocalserver.WebViewLocalServer;
import com.litesuits.http.LiteHttp;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.exception.ClientException;
import com.litesuits.http.exception.HttpClientException;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.exception.HttpNetException;
import com.litesuits.http.exception.HttpServerException;
import com.litesuits.http.exception.NetException;
import com.litesuits.http.exception.ServerException;
import com.litesuits.http.exception.handler.HttpExceptionHandler;
import com.litesuits.http.listener.HttpListener;
import com.litesuits.http.request.AbstractRequest;
import com.litesuits.http.request.StringRequest;
import com.litesuits.http.request.content.ByteArrayBody;
import com.litesuits.http.request.content.multi.FilePart;
import com.litesuits.http.request.content.multi.InputStreamPart;
import com.litesuits.http.request.content.multi.MultipartBody;
import com.litesuits.http.request.content.multi.StringPart;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;
import com.litesuits.http.utils.HttpUtil;


import net.sqlcipher.database.SQLiteDatabase;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.R;
import org.itri.woundcamrtc.helper.DBTableHelper;
import org.itri.woundcamrtc.helper.*;

import org.itri.woundcamrtc.helper.SecretDbHelper;
import org.itri.woundcamrtc.helper.ServiceHelper;
import org.itri.woundcamrtc.helper.StringUtils;
import org.itri.woundcamrtc.preview.FileUtility;
import org.itri.woundcamrtc.preview.TxtData;
import org.itri.woundcamrtc.preview.WebViewJavaScriptInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.itri.woundcamrtc.MainActivity;
import org.itri.woundcamrtc.helper.DNSResolver;
import org.itri.woundcamrtc.helper.TimeHelper;

import static org.itri.woundcamrtc.AppResultReceiver.CHECK_INTERNET;
import static org.itri.woundcamrtc.AppResultReceiver.DEFAULT_UPLOAD_PATH;
import static org.itri.woundcamrtc.AppResultReceiver.IS_FOR_IMAS_BOX;
import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;
import static org.itri.woundcamrtc.AppResultReceiver.PING_INTERNET_URL;
import static org.itri.woundcamrtc.AppResultReceiver.SAVE_DIR;
import static org.itri.woundcamrtc.AppResultReceiver.ZONE;
import static org.itri.woundcamrtc.AppResultReceiver.isGifDemo;
import static org.itri.woundcamrtc.AppResultReceiver.mMainActivity;
import static org.itri.woundcamrtc.AppResultReceiver.touchRotate;


public class JobQueueUploadFileJob extends Job {
    private static String TAG = JobQueueUploadFileJob.class.getSimpleName();
    private JobManager jobQueueManager = null;

    private static MainActivity activity;
    private Context mContext;
    public static LiteHttp mLiteHttp = null;
    private String rootPath = "";
    private static String userId = "";
    private File mainDir;
    public static SQLiteDatabase Sercretdb;
    public static SecretDbHelper sqllitesecret;
    public static DBTableHelper database;

    private File expectedFilesRoot = null;
    private int expectedFiles = 0; // total want to upload jpg count
    private static int enqueueFiles = 0; // on start upload jpg count
    private static int dequeueFiles = 0; // on end upload jpg count

    public JobQueueUploadFileJob(JobManager _jobQueueManager, String tag, MainActivity activity, Context context, LiteHttp param1, String param2) {
        //super(new Params(PRIORITY).requireNetwork().persist().groupBy(tag).singleInstanceBy(tag));
        super(new Params(Thread.NORM_PRIORITY - 3).groupBy("uploadJob"));
        jobQueueManager = _jobQueueManager;
        this.activity = activity;
        this.mContext = context;
        mLiteHttp = param1;
        rootPath = param2;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        activity.updateViewInfo();
        if(dataEncrypt==false){
            mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
            database = DBTableHelper.getInstance(getApplicationContext(), mainDir.getAbsolutePath() + File.separator + AppResultReceiver.PROJECT_NAME + ".db");
        }else{
            SQLiteDatabase.loadLibs(activity);
            sqllitesecret=new SecretDbHelper(activity);
            Sercretdb = SecretDbHelper.getInstance(activity).getWritableDatabase("MIIS");








        }
     //   activity.setUploadStatus(true);

        try {
            if (CHECK_INTERNET && !checkNetworkConnected(PING_INTERNET_URL)) {
                activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.not_connect_backside), 0);
            } else {
                dequeueFiles = 0;
                enqueueFiles = 0;
                expectedFiles = 0;
                expectedFilesRoot = new File(rootPath);
                //walkFiles(expectedFilesRoot);
                Log.v("ssssssdasdasd",String.valueOf(expectedFilesRoot.getAbsolutePath()));
                walkFilesEx(expectedFilesRoot);
            }
            SystemClock.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
       // activity.setUploadStatus(false);

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        //如果重試n次仍未成功，那麼就放棄任務，也會進入onCancel
        if (runCount > 0)
            return RetryConstraint.CANCEL;
        return RetryConstraint.RETRY;
    }

    //如果重試超過限定次數，會執行onCancel
    //如果使用者主動放棄此任務，也一樣進入onCancel
    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
    }

    public static final int STATUS_INIT = -1;
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_UPDAE = 3;
    public static final int STATUS_TIMEOUT = 4;
    public static final int STATUS_4XX_ERROR = 5;
    public static final int STATUS_5XX_ERROR = 6;
    public static final int STATUS_LOGIN_ERROR = 7;
    public static final int STATUS_CALL_PHONE = 8;

    private String mPingUrlMessage = "";
    private int mPingUrlStatus = 0;

    public boolean pingUrl(String strUrl, int timeout) {

        Log.d(TAG, "pingUrl start");

        // HttpUrlConnection will not timeout during domain name resolution, please use IP address
        final StringRequest postRequest = new StringRequest(strUrl);
        postRequest.setMaxRetryTimes(0);
        postRequest.setSocketTimeout(timeout);
        postRequest.setConnectTimeout(timeout);
        postRequest.setMethod(HttpMethods.Get);
        postRequest.setHttpListener(new HttpListener<String>(false, false, false) {
            @Override
            public void onSuccess(String s, Response<String> response) {
                mPingUrlStatus = STATUS_FINISHED;
                mPingUrlMessage = "";
                super.onSuccess(s, response);
            }

            @Override
            public void onFailure(HttpException exception, Response<String> response) {
                super.onFailure(exception, response);
                try {
                    new HttpExceptionHandler() {
                        @Override
                        protected void onClientException(HttpClientException e, ClientException type) {
                            mPingUrlMessage = ((HttpClientException) e).getExceptionType().reason;
                            switch (e.getExceptionType()) {
                                case UrlIsNull:
                                    mPingUrlStatus = STATUS_ERROR;
                                    break;
                                case IllegalScheme:
                                    mPingUrlStatus = STATUS_ERROR;
                                    break;
                                case ContextNeeded:
                                    mPingUrlStatus = STATUS_ERROR;
                                    break;
                                case PermissionDenied:
                                    mPingUrlStatus = STATUS_LOGIN_ERROR;
                                    break;
                                case SomeOtherException:
                                    mPingUrlStatus = STATUS_ERROR;
                                    break;
                                default:
                                    mPingUrlStatus = STATUS_ERROR;
                                    break;
                            }
                        }

                        @Override
                        protected void onNetException(HttpNetException e, NetException type) {
                            mPingUrlMessage = ((HttpNetException) e).getExceptionType().reason;
                            switch (e.getExceptionType()) {
                                case NetworkNotAvilable:
                                    mPingUrlStatus = STATUS_TIMEOUT;
                                    break;
                                case NetworkUnstable:
                                    mPingUrlStatus = STATUS_TIMEOUT;
                                    break;
                                case NetworkDisabled:
                                    mPingUrlStatus = STATUS_TIMEOUT;
                                    break;
                                case NetworkUnreachable:
                                    mPingUrlStatus = STATUS_TIMEOUT;
                                    break;
                                default:
                                    mPingUrlStatus = STATUS_TIMEOUT;
                                    break;
                            }
                        }

                        @Override
                        protected void onServerException(HttpServerException e, ServerException type, HttpStatus status) {
                            mPingUrlMessage = ((HttpServerException) e).getExceptionType().reason;
                            switch (e.getExceptionType()) {
                                case ServerInnerError:
                                    // status code 5XX error
                                    mPingUrlStatus = STATUS_5XX_ERROR;
                                    break;
                                case ServerRejectClient:
                                    // status code 4XX error
                                    mPingUrlStatus = STATUS_4XX_ERROR;
                                    break;
                                case RedirectTooMuch:
                                    mPingUrlStatus = STATUS_5XX_ERROR;
                                    break;
                                default:
                                    mPingUrlStatus = STATUS_5XX_ERROR;
                                    break;
                            }
                        }
                    }.handleException(exception);
                    Log.d(TAG, Integer.toString(response.getHttpStatus().getCode()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onStart(AbstractRequest<String> request) {
                super.onStart(request);
            }

            @Override
            public void onLoading(AbstractRequest<String> request, long total, long len) {
                super.onLoading(request, total, len);
            }

            @Override
            public void onRedirect(AbstractRequest<String> request, int max, int times) {
                super.onRedirect(request, max, times);
            }

            @Override
            public void onEnd(Response<String> response) {
                super.onEnd(response);
            }

            @Override
            public void onCancel(String s, Response<String> response) {
                super.onCancel(s, response);
            }
        });

        mPingUrlStatus = STATUS_RUNNING;
        mLiteHttp.executeAsync(postRequest);

        while (mPingUrlStatus == STATUS_RUNNING) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                mPingUrlStatus = STATUS_TIMEOUT;
            }
        }

        if (mPingUrlStatus != STATUS_TIMEOUT) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkDNS(String url, long timeout) {
        boolean result = false;
        try {
            DNSResolver dnsRes = new DNSResolver(url);

            try {
                int tmp;
                String[] splitData = dnsRes.getDomainName().split("\\.");
                for (String tmpString : splitData) {
                    tmp = Integer.parseInt(tmpString);
                }
                result = true;
            } catch (Exception ex) {
                Thread t = new Thread(dnsRes);
                t.setDaemon(true);
                t.setPriority(8);
                t.start();
                t.join(timeout);

                InetAddress inetAddr = dnsRes.get();
                dnsRes = null;
                result = inetAddr != null;
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean checkNetworkConnected(String ping_url) {
        boolean result = false;
        ConnectivityManager CM = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (CM == null) {
            result = false;
        } else {
            NetworkInfo info = CM.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (!info.isAvailable()) {
                    result = false;
                } else {
                    if (pingUrl(ping_url, 1500)) {
                        result = true;
                    } else {
                        result = false;
                    }
                    mLiteHttp.clearMemCache();
                }
            }
        }
        return result;
    }

//    private void writeToFile(String outFilename, String msg, boolean append) {
//        BufferedWriter writer = null;
//        try {
//            File textFile = new File(outFilename);
//
//            writer = new BufferedWriter(new FileWriter(textFile, append));
//            writer.write(msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                // Close the writer regardless of what happens...
//                writer.close();
//            } catch (Exception e) {
//            }
//        }
//    }

    private boolean walkFilesEx(File root) {
        //
        // 檢查目錄有沒有存在
        // --------------------------------------------------------------------------------
        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
        if (!mPicDir.exists()) {
            Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + "不存在");
            return false;
        }

        //
        // 1. 整理出要上傳的 groupIds (List of EvalID)
        // --------------------------------------------------------------------------------
        List<String> groupIds = new ArrayList<String>();
        Map<String, Map<String, String>> filenamesByGroupId = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        try {
            String[] filenames;
            //
            // 1.1 取得要上傳的檔案列表,並依檔名排序 jpg, png, txt
            // --------------------------------------------------------------------------------

             filenames = mPicDir.list(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String filename) {
                        if (filename.endsWith("_jpg.jpg") || filename.endsWith("_thm.png") || filename.endsWith(".txt")) {
                            Log.v(TAG,"TEst1 : "+ filename);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

             Log.v(TAG + "_" + "filenames_0",filenames[0]);
             Log.v(TAG + "_" + "filenames_1",filenames[1]);
             Log.v(TAG + "_" + "filenames_2",filenames[2]);

            if (filenames == null) {
                Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + " 沒有可用的檔案");
                return false;
            }

            //
            // 1.2 留下唯一 groupId 產生 String<groupIds> 及 Map<itemId, filename>
            // --------------------------------------------------------------------------------
            Log.v(TAG + "_" + "mPicDir_2",mPicDir.toString());
            for (String filename : filenames) {
                // filename 是相對路徑格式
                try {
                    File file = new File(mPicDir, filename);
                    Log.v(TAG + "_" + "file_3",file.toString());
                    if (file.exists()) {
                        String fileName = file.getName();
                        String filePath = file.getAbsolutePath();
                        String groupId = fileName.substring(0, "yyyy-MM-dd HH-mm-ss-SSS".length());
                        String[] nameSplit = file.getAbsoluteFile().getName().split("_");
                        String itemId= nameSplit[2];
                        Log.v(TAG + "_" + "groupId_1",groupId.toString());
                        Log.v(TAG + "_" + "nameSplit_2",nameSplit.toString());
                        Log.v(TAG + "_" + "itemId_3",itemId.toString());
                        if(itemId.equals("13")&&(fileName.endsWith("_data.txt")||fileName.endsWith("_datax.txt"))){
                            itemId="0";
                        }
                        Map<String, String> filenameMap = filenamesByGroupId.get(groupId);
                        if (filenameMap==null){
                            filenameMap = new TreeMap<String, String>();
                            filenameMap.put(itemId, fileName);
                            filenamesByGroupId.put(groupId, filenameMap);
                            Log.v(TAG + "_" + "filenameMap_4",filenameMap.toString());
                            Log.v(TAG + "_" + "itemId_5",itemId.toString());
                            Log.v(TAG + "_" + "fileName_6",fileName.toString());
                            Log.v(TAG + "_" + "groupId_7",groupId.toString());
                            Log.v(TAG + "_" + "filenamesByGroupId_8",filenamesByGroupId.toString());

                        } else {
                            filenameMap.put(itemId, fileName);
                            Log.v(TAG + "_" + "filenameMap_9",filenameMap.toString());
                            Log.v(TAG + "_" + "itemId_10",itemId.toString());
                            Log.v(TAG + "_" + "fileName_11",fileName.toString());
                        }
                        if (!groupIds.contains(groupId) && StringUtils.isValidDate(groupId,"")) {
                            groupIds.add(groupId);
                            Log.v(TAG + "_" + "groupIds_12",groupIds.toString());
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Get Photo File Error", ex);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //
        // 2. 檢查 groupIds 每一個group id對應的檔案bodyPart/ownerID設正確，則產生 waitForUploadGroupIds
        // --------------------------------------------------------------------------------
        int sendCount = 0;
        int noBodyPart = 0;
        int noOwnerId = 0;
        int noTxt = 0;
        List<String> waitForUploadGroupIds = new ArrayList<String>();
        for (String groupId : groupIds) {
            try {
                Map<String, String> filenameMap = filenamesByGroupId.get(groupId);
                Log.v(TAG + "_" + "filenameMap_13",filenameMap.toString());
                if (filenameMap!=null) {
                    TxtData txtObject = FileUtility.getTxtDataFromDownload(mContext, groupId);
                    Log.v(TAG + "_" + "txtObject",txtObject.toString());
                    Log.v(TAG + "_" + "groupId",groupId.toString());
                    if (txtObject == null) {
                        // 2.1 沒有 txt, 就直接排入 waitForUploadGroupIds
                        // --------------------------------------------------------------------------------
                        waitForUploadGroupIds.add(groupId);
                        Log.v(TAG + "_" + "waitForUploadGroupIds",waitForUploadGroupIds.toString());
                        for (String itemId : filenameMap.keySet()) {
                            try {
                                if (Integer.parseInt(itemId)!=13&&Integer.parseInt(itemId)<100) {
                                    noTxt++;
                                    sendCount++;
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Log.e(TAG, "Upload File Error", ex);
                            }
                        }
                    } else {
                        // 2.2 存在 txt, 就檢查每一個 jpg有沒有設定好 bodyPart, ownerID
                        // --------------------------------------------------------------------------------
                        int thisGroupSendFile = 0;
                        boolean notAssign = false;
                        if (IS_FOR_IMAS_BOX == true) {

                        } else {
                            if (StringUtils.isBlank(txtObject.getOwnerId())) {
                                Log.v(TAG + "_" + "StringUtils",String.valueOf(StringUtils.isBlank(txtObject.getOwnerId())));
                                Log.v(TAG + "_" + "txtObject.getOwnerId",txtObject.getOwnerId());
                                notAssign = true;
                                for (String itemId : filenameMap.keySet()) {
                                    try {
                                        if (Integer.parseInt(itemId)!=0&&Integer.parseInt(itemId)<100)
                                            noOwnerId++;
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        Log.e(TAG, "Upload File Error", ex);
                                    }
                                }
                            } else {
                                Map<String, JSONObject> info = txtObject.getInfo();

                                for (String itemId : filenameMap.keySet()) {
                                    try {
                                        if (Integer.parseInt(itemId)!=0&&Integer.parseInt(itemId)<100) {
                                            JSONObject _info = info.get(itemId);
                                            if (_info == null || _info.isNull("bodyPart") || _info.getString("bodyPart") == null || _info.getString("bodyPart").isEmpty()) {
                                                _info = info.get(itemId + 99);
                                                if (_info == null || _info.isNull("bodyPart") || _info.getString("bodyPart") == null || _info.getString("bodyPart").isEmpty()) {
                                                    _info = info.get(itemId + 199);
                                                    if (_info == null || _info.isNull("bodyPart") || _info.getString("bodyPart") == null || _info.getString("bodyPart").isEmpty()) {
                                                        _info = info.get(itemId + 299);
                                                        if (_info == null || _info.isNull("bodyPart") || _info.getString("bodyPart") == null || _info.getString("bodyPart").isEmpty()) {
                                                            notAssign = true;
                                                            if (Integer.parseInt(itemId)!=0&&Integer.parseInt(itemId)<100)
                                                                noBodyPart++;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (!notAssign && Integer.parseInt(itemId)!=0&&Integer.parseInt(itemId)<100)
                                            thisGroupSendFile++;
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        Log.e(TAG, "Upload File Error", ex);
                                    }
                                } // for
                            } // if
                        } //

                        //
                        // 2.3 若都有設定, 就排入 waitForUploadGroupIds
                        // --------------------------------------------------------------------------------
                        if (!notAssign) {
                            waitForUploadGroupIds.add(groupId);
                            sendCount = sendCount + thisGroupSendFile;
                            Log.v(TAG + "_" + "waitForUploadGroupIds",waitForUploadGroupIds.toString());
                            Log.v(TAG + "_" + "sendCount",String.valueOf(sendCount));
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(TAG, "Upload File Error", ex);
            }
        }

        String summary = "Total files send:" + sendCount + ", No send:" + (noOwnerId + noBodyPart);
        if (noOwnerId > 0) {
            summary = summary + "\n" + noOwnerId + " No OwnerId";
        }
        if (noBodyPart > 0)
        {
            summary = summary + "\n" + noBodyPart + " No BodyPart";
        }
        if (noTxt > 0)
        {
            summary = summary + "\n" + noTxt + " No Description";
        }
        if (summary.endsWith(","))
        {
            summary = summary.substring(0, summary.length() - 1);
        }
        String finalSummary = summary;

//        mMainActivity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
////                    HttpUtil.showTips(mMainActivity, "", summary);
//                mMainActivity.showDialog(mMainActivity.getString(R.string.start_file_upload), finalSummary, 0);
//            }
//        });

        int finalSendCount = sendCount;
        mMainActivity.showDialogWithCallback(mMainActivity.getString(R.string.start_file_upload), finalSummary, 0, new MainActivity.DialogCallback() {
            @Override
            public void positiveCallback(Object obj) {
//
                // 3. 根據取出的 waitForUploadGroupIds
                // --------------------------------------------------------------------------------
                expectedFiles= finalSendCount;
                Log.v(TAG,"expectedFiles : "+expectedFiles);

                    int uploadCount = 1;
                    for (String groupId : waitForUploadGroupIds) {

                        Map<String, String> filenameMap = filenamesByGroupId.get(groupId);
                        if (filenameMap!=null) {
                            //
                            // 3.1 先找對應的jpg上傳
                            // --------------------------------------------------------------------------------
                            for (String itemId : filenameMap.keySet()) {
                                try {
                                    Integer intItemId = Integer.parseInt(itemId);

                                    Log.v(TAG + "___1",filenameMap.toString());
                                    Log.v(TAG + "___2",itemId.toString());
                                    Log.v(TAG + "___3",filenameMap.get(itemId));
                                    Log.v(TAG + "___4",mPicDir.getAbsolutePath());

                                    if (filenameMap.get(itemId).endsWith("_jpg.jpg")) {
                                        Log.d(TAG, "----------------------上傳中----------------------");
                                        Log.d(TAG, "正在上傳的檔名為: " + filenameMap.get(itemId));
                                        String[] nameSplit = filenameMap.get(itemId).split("_");
                                        if (IS_FOR_IMAS_BOX == true) {
                                            uploadSingleRecordForIMASBox(uploadCount, nameSplit[0], nameSplit[1], itemId, new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)), false);
                                        } else {
                                            Log.v("upload_jpg_1",String.valueOf(uploadCount));
                                            Log.v("upload_jpg_2",String.valueOf(nameSplit[0]));
                                            Log.v("upload_jpg_3",String.valueOf(nameSplit[1]));
                                            Log.v("upload_jpg_4",String.valueOf(itemId));
                                            Log.v("upload_jpg_5",String.valueOf(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)));
                                            uploadSingleRecord(uploadCount, nameSplit[0], nameSplit[1], itemId, new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)), false);
                                        }
                                    }

                                    Thread.sleep(10);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                uploadCount++;
                                expectedFiles--;
                            }

                            //
                            // 3.2 再找png上傳
                            // --------------------------------------------------------------------------------
                            for (String itemId : filenameMap.keySet()) {
                                try {
                                    Integer intItemId = Integer.parseInt(itemId);
                                    if (filenameMap.get(itemId).endsWith("_thm.png")) {
                                        Log.d(TAG, "----------------------上傳中----------------------");
                                        Log.d(TAG, "正在上傳的檔名為: " + filenameMap.get(itemId));
                                        String[] nameSplit = filenameMap.get(itemId).split("_");
                                        if (IS_FOR_IMAS_BOX == true) {
                                            //   uploadSingleRecordForIMASBox(uploadCount, nameSplit[0], nameSplit[1], itemId, new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)), false);
                                        } else {
                                            Log.v("upload_png_1",String.valueOf(uploadCount));
                                            Log.v("upload_png_2",String.valueOf(nameSplit[0]));
                                            Log.v("upload_png_3",String.valueOf(nameSplit[1]));
                                            Log.v("upload_jpg_4",String.valueOf(itemId));
                                            Log.v("upload_png_5",String.valueOf(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)));
                                            uploadSingleRecord(uploadCount, nameSplit[0], nameSplit[1], itemId, new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)), false);
                                        }
                                    }

                                    Thread.sleep(10);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                uploadCount++;
                                expectedFiles--;
                            }

                            //
                            // 3.3 後找txt上傳
                            // --------------------------------------------------------------------------------
                            for (String itemId : filenameMap.keySet()) {
                                try {
                                    Integer intItemId = Integer.parseInt(itemId);
                                    if (filenameMap.get(itemId).endsWith("_data.txt") || filenameMap.get(itemId).endsWith("_datax.txt")) {

                                        boolean foundFiles = false;
                                        // 3.3.1 檢查對應的 jpg, png是否都上傳完畢, 不在目錄中
                                        for (String checkItemId : filenameMap.keySet()) {
                                            if (intItemId != 0 && FileHelper.isExist(mPicDir.getAbsolutePath() , filenameMap.get(itemId), false)) {
                                                foundFiles = true;
                                                break;
                                            }
                                        }

                                        // 3.3.2 都上傳完畢, 就上傳txt
                                        if (!foundFiles) {
                                            Log.d(TAG, "----------------------上傳中----------------------");
                                            Log.d(TAG, "正在上傳的檔名為: " + filenameMap.get(itemId));
                                            String[] nameSplit = filenameMap.get(itemId).split("_");
                                            String filename=filenameMap.get(itemId);
                                            Log.v(TAG, "sercrettxt :　"+filename);
                                            if(dataEncrypt==true){

                                                if(filenameMap.get(itemId).endsWith("_datax.txt")){

                                                    File sercrettxt=new File(mPicDir,filename);

                                                    try {
                                                        FileHelper.txt_decrypt(sercrettxt.getAbsolutePath(),18);
                                                        filename=sercrettxt.getName().replace("_datax.txt","_data.txt");
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                            }
                                            Log.v(TAG, "sercrettxt :　"+filename);
                                            if (IS_FOR_IMAS_BOX == true) {
                                                //   uploadSingleRecordForIMASBox(uploadCount, nameSplit[0], nameSplit[1], "13", new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)), false);
                                            } else {
                                                Log.v("upload_txt_1",String.valueOf(uploadCount));
                                                Log.v("upload_txt_2",String.valueOf(nameSplit[0]));
                                                Log.v("upload_txt_3",String.valueOf(nameSplit[1]));
                                                Log.v("upload_jpg_4",String.valueOf(itemId));
                                                Log.v("upload_txt_5",String.valueOf(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)));
                                                uploadSingleRecord(uploadCount, nameSplit[0], nameSplit[1], "13", new File(mPicDir.getAbsolutePath() + "/" + filename), false);
                                            }

                                        }
                                    }

                                    Thread.sleep(10);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                uploadCount++;
                                expectedFiles--;
                            }
                        }
                    }
                    Log.v(TAG,"expectedFiles :"+expectedFiles);
                    if (expectedFiles> 0) {
                        activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.please_retry_upload), 0);
                    } else {
                        //AppResultReceiver.recordList = new ArrayList<>();
                        activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.success_upload), 0);
                        activity.updateViewInfo();
                        activity.setUploadStatus(false);

                    }





            }

            @Override
            public void negativeCallback(Object obj) {

            }
        });
        
        return true;
    }


    private boolean walkFiles(File root) {
        List<File> getCorrectFiles = getUploadableFiles(mContext);
        expectedFiles = expectedFiles + getCorrectFiles.size();

        Log.i(TAG, "執行walkFiles程式");

        //File[] list = root.listFiles();

        for (File f : getCorrectFiles) {
            Log.v("收到", f.getName());
        }

        int fileTotalCount = getCorrectFiles.size();
        int count = 0;
        List<File> txtListFile = new ArrayList<File>();
        List<String> txtListEvlid = new ArrayList<String>();
        List<String> txtListTime = new ArrayList<String>();
        List<String> txtListItemid = new ArrayList<String>();

        List<File> jpgListFile = new ArrayList<File>();
        List<String> jpgListEvlid = new ArrayList<String>();
        List<String> jpgListTime = new ArrayList<String>();
        List<String> jpgListItemid = new ArrayList<String>();
        int fileCount = 0;
        for (File f : getCorrectFiles) {
            try {
                if (f.isDirectory()) {
                    Log.d("", "目錄為: " + f.getAbsoluteFile());
                    walkFiles(f);
                } else {
                    fileCount++;
                    Log.d("", "讀取到的尚未上傳檔案有:  " + fileCount + " " + f.getAbsoluteFile());

                    String[] nameSplit = f.getAbsoluteFile().getName().split("_");
                    String itemId = nameSplit[2];
                    Log.v(TAG, nameSplit[3]);
                    String format = nameSplit[3];

                    if (format.equals("data.txt")) {
                        txtListEvlid.add(nameSplit[0]);
                        txtListTime.add(nameSplit[1]);
                        txtListItemid.add(itemId);
                        txtListFile.add(f.getAbsoluteFile());
                    } else {
                        jpgListEvlid.add(nameSplit[0]);
                        jpgListTime.add(nameSplit[1]);
                        jpgListItemid.add(itemId);
                        jpgListFile.add(f.getAbsoluteFile());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < jpgListEvlid.size(); i++) {
            Log.d(TAG, "----------------------上傳中----------------------");
            Log.d(TAG, "jpg檔案數量: " + jpgListEvlid.size());
            Log.d(TAG, "正在上傳的jpg檔名為: " + jpgListFile.get(i));
//            long start = System.currentTimeMillis();
            if(IS_FOR_IMAS_BOX==true){
                uploadSingleRecordForIMASBox(i, jpgListEvlid.get(i), jpgListTime.get(i), jpgListItemid.get(i), jpgListFile.get(i), false);

            }else{
                uploadSingleRecord(i, jpgListEvlid.get(i), jpgListTime.get(i), jpgListItemid.get(i), jpgListFile.get(i), false);
            }
            fileCount--;
//            long elapsedTimeMillis = System.currentTimeMillis() - start;
//            float elapsedTimeSec = elapsedTimeMillis / 1000F;
//            Log.v(TAG, "檔案" + i + "之上傳秒數為" + elapsedTimeSec);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        for (int i = 0; i < txtListEvlid.size(); i++) {
            Log.d(TAG, "----------------------上傳中----------------------");
            Log.d(TAG, "txt檔案數量: " + txtListEvlid.size());
            Log.d(TAG, "正在上傳的jtxt檔名為: " + txtListFile.get(i));
            if(IS_FOR_IMAS_BOX==true){

            }else{
                uploadSingleRecord(i, txtListEvlid.get(i), txtListTime.get(i), txtListItemid.get(i), txtListFile.get(i), false);
            }
            try {
                fileCount--;
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        activity.setUploadStatus(false);
        Log.v(TAG,"fileCount : "+fileCount);
        if (fileCount> 0) {
            activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.please_retry_upload), 0);
        } else {
            //AppResultReceiver.recordList = new ArrayList<>();
            activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.success_upload), 0);
            activity.updateViewInfo();
        }
        if (fileCount==0) activity.setUploadStatus(false);
//
//        long start = System.currentTimeMillis();
//        while (System.currentTimeMillis() - start < root.listFiles().length * 1000) {
//            try {
//                Thread.sleep(50);
//                if (root.listFiles().length == 0)
//                    break;
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (root.listFiles().length > 0) {
//            activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.please_retry_upload), 0);
//        } else {
//            //AppResultReceiver.recordList = new ArrayList<>();
//            activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.success_upload), 0);
//            activity.updateViewInfo();
//        }

        return true;
    }

    public static synchronized boolean uploadSingleRecord(int i, String evlId, String time, String itemid, final File file, boolean lastfile) {

        try {
            String uploadUrl = DEFAULT_UPLOAD_PATH;
            String[] splitDateTime = evlId.split(" ");
            MultipartBody body = new MultipartBody();
            long timestamp = TimeHelper.getTimeFormat(splitDateTime[0] + " " + splitDateTime[1].replace("-", ":").replace(".", ":"));
            String part = "";

            List<Map<String, Object>> listMeasure = null;
            Log.v(TAG,"file"+file.getName());
            if(dataEncrypt==false){
                listMeasure = database.queryRecordByfilepath(file.getName());

            }else{
                listMeasure = sqllitesecret.queryRecordByfilepath(Sercretdb,file.getName());
            }

            Log.v("asdjiasodjasiod",String.valueOf(listMeasure.size()));

            if (listMeasure.size() > 0) {

                Log.v(TAG,"listMeasure.size()>0");
                userId = String.valueOf(listMeasure.get(0).get("userid"));
                part = String.valueOf(listMeasure.get(0).get("part"));

                Log.v("sdasdamskdasmdkamsd",String.valueOf(part));
                Log.v("sdasdamskdasmdkamsd_1",String.valueOf(userId));

            } else {

                userId = activity.single_upload_id;
                Log.v("uuuuuuuuuuu",String.valueOf(userId));

            }

            if (StringUtils.isBlank(userId))
                userId = activity.userId;
        Log.v(TAG,"file.getName() :　"+file.getName());
            Log.v(TAG, "itemid"+itemid);
            JSONArray infoArr = new JSONArray();
            String fileSpilt[] = file.getName().split("_");
        if(Integer.valueOf(itemid)>99){

            int thmpng_item=Integer.valueOf(itemid)-99;
            Log.v(TAG,"thmpng_item"+thmpng_item);
            if(dataEncrypt==false){
                listMeasure = database.queryRecordByfilepaththm(evlId,String.valueOf(thmpng_item));

            }else{
                listMeasure = sqllitesecret.queryRecordByfilepaththm(Sercretdb,evlId,String.valueOf(thmpng_item));
            }
            if (listMeasure.size() > 0) {
                part = String.valueOf(listMeasure.get(0).get("part"));
                Log.v(TAG, "part"+part);
            }
        }

            if(fileSpilt[3].equals("data.txt")){

                Log.v(TAG, "進入TXT上傳地方");
                List<Map<String, Object>> mapList = null;

                if(dataEncrypt==false){
                    mapList = database.querySQLData("table_picNumber", null, "evid=?", new String[]{fileSpilt[0]}, "");

                }else{
                    mapList = sqllitesecret.querySQLData(Sercretdb,"table_picNumber", null, "evid=?", new String[]{fileSpilt[0]}, "");
                }
                for (Map<String, Object> map : mapList) {
                    if(map.get("type").equals("jpg.jpg")){
                        JSONObject infoObj = new JSONObject();
                        ArrayList<String> itemIds = new ArrayList<String>();
                        String fever = (String) map.get("fever");
                        String smell = (String) map.get("smell");
                        String level = (String) map.get("level");
                        String character = (String) map.get("character");
                        String overtime = (String) map.get("overtime");
                        String occur = (String) map.get("firstOcurred");
                        String bodypart = (String) map.get("part");

                        String occurDate_data=(String) map.get("createdate");

                        if(occurDate_data==null){
                            occurDate_data="";
                        }

                        if(StringUtils.isNotBlank(fever)){
                            itemIds.add(fever);
                        }
                        if(StringUtils.isNotBlank(smell)){
                            itemIds.add(smell);
                        }
                        if(StringUtils.isNotBlank(level)){
                            itemIds.add(level);
                        }
                        if(StringUtils.isNotBlank(character)){
                            itemIds.add(character);
                        }
                        if(StringUtils.isNotBlank(overtime)){
                            itemIds.add(overtime);
                        }
                        if(StringUtils.isNotBlank(occur)){
                            itemIds.add(occur);
                        }

                        infoObj.put("itemIds", itemIds);
                        infoObj.put("occurDate_data", occurDate_data);
                        infoObj.put("bodypart", bodypart);
                        infoObj.put("itemNo", (Integer) map.get("number"));
                        infoObj.put("analysisTime", (String) map.get("analysisTime"));
                        infoArr.put(infoObj);
                        Log.v(TAG,"statusInfo:"+ infoArr.toString());
                    }
                }
                part="";
            }
            if(!fileSpilt[3].equals("data.txt")){

                String urlPath = file.getAbsolutePath();
                Log.v(TAG,"file.getAbsolutePath() : "+file.getAbsolutePath());
                if (AppResultReceiver.dataEncrypt) {
                    try {
                        InputStream ins = null;
                        Log.v(TAG,"urlPath : "+urlPath);
                        String fileName =urlPath.replace("%20", " ");
                        Log.v(TAG,"fileName : "+fileName);
                        try {
                        ins= FileHelper.inputStreamSecret(fileName);
                        } catch (Exception e) {

                        }
                        new WebResourceResponse(
                                MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(urlPath)),
                                "UTF-8", ins);

                        byte[] buffer = new byte[ins.available()];
                        ins.read(buffer);

                        File targetFile = new File( file.getAbsolutePath());
                        OutputStream outStream = new FileOutputStream(targetFile);
                        outStream.write(buffer);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


            Log.v(TAG,"userId"+userId);
            if (AppResultReceiver.ZONE.equals("TZUCHI") || AppResultReceiver.ZONE.equals("TC")) {
                Log.v(TAG,"infoArr.toString()"+infoArr.toString());


                    body.addPart(new StringPart("itemNo", itemid))
                            .addPart(new StringPart("statusInfo", infoArr.toString()))
                            .addPart(new StringPart("timestamp", Long.toString(timestamp)))

                            .addPart(new FilePart("myImg", file))
                            .addPart(new StringPart("bodypart", part))
                            .addPart(new StringPart("location", AppResultReceiver.ZONE))
                            .addPart(new StringPart("userId", userId));


            } else {

                    body.addPart(new StringPart("itemNo", itemid))
                            .addPart(new StringPart("statusInfo", infoArr.toString()))
                            .addPart(new StringPart("timestamp", Long.toString(timestamp)))
                            .addPart(new FilePart("myImg", file))
                            .addPart(new StringPart("bodypart", part))
                            .addPart(new StringPart("userId", userId));



            }


            final StringRequest upload = new StringRequest(uploadUrl);
            Log.v("sdmkasldmalskdmlaskd",String.valueOf(upload));

            if (AppResultReceiver.cookies!=null) {
                for (String cookie : AppResultReceiver.cookies) {
                    upload.addHeader("set-cookies", cookie);
                }
            }
                     upload.setMethod(HttpMethods.Post)
                    .setHttpListener(new HttpListener<String>(false, false, false) {

                        public void onStart(AbstractRequest<String> request) {
                            Log.w(TAG, "onStart");
                            enqueueFiles++;
                        }

                        public void onCancel(String data, Response<String> response) {
                            Log.w(TAG, "onCancel");
//                            try {
//                                final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//                                File targetTxt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                                if (!targetTxt.exists()) {
//                                    writeToFile(targetTxt.getPath() + File.separator + "WoundcamRtc.log",
//                                            "Cancel Now! " + response.getRawString()+"\r\n", true);
//                                }
//                            } catch (Exception e) {
//                                Log.v(TAG, "寫入txt檔錯誤：" + e.getMessage());
//                            }
                            String msg = response.getException().toString();
                            try {
                                msg = msg.substring(msg.indexOf(":"));
                            } catch (Exception eee) {
                            }
                            HttpUtil.showTips(mMainActivity, "", "Upload Cancel " + i + msg);
                        }

                        public void onLoading(AbstractRequest<String> request, long total, long len) {
                            //Log.w(TAG, "onLoading " + len + "/" + total);
                        }

                        public void onUploading(AbstractRequest<String> request, long total, long len) {
                            Log.w(TAG, "onUploading " + len + "/" + total);
                            Date ddd = new Date();
                            if (ddd.getSeconds() % 10 == 0)
                                Toast.makeText(mMainActivity, "Uploading file " + i + ":" + len + "/" + total, Toast.LENGTH_SHORT);
                        }

                        @Override
                        public void onRedirect(AbstractRequest<String> request, int max, int times) {
                            Log.w(TAG, "onRedirect");
//
//                            try {
//                                final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//                                File targetTxt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                                if (!targetTxt.exists()) {
//                                    writeToFile(targetTxt.getPath() + File.separator + "WoundcamRtc.log",
//                                            "Redirect max num: " + max + " , times: " + times  +" GO-TO: " + timestamp+"\r\n", true);
//                                }
//                            } catch (Exception e) {
//                                Log.v(TAG, "寫入txt檔錯誤：" + e.getMessage());
//                            }
                        }

                        @Override
                        public void onRetry(AbstractRequest<String> request, int max, int times) {
                            Log.w(TAG, "onRetry");
//                            try {
//                                final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//                                File targetTxt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                                if (!targetTxt.exists()) {
//                                    writeToFile(targetTxt.getPath() + File.separator + "WoundcamRtc.log",
//                                            "Retry Now! max num: " + max + " , times: " + times  +" GO-TO: " + timestamp+"\r\n", true);
//                                }
//                            } catch (Exception e) {
//                                Log.v(TAG, "寫入txt檔錯誤：" + e.getMessage());
//                            }
                        }

                        @Override
                        public void onEnd(Response<String> response) {
                            Log.w(TAG, "onEnd");
                            dequeueFiles++;
/*
                            if (enqueueFiles >= expectedFiles && dequeueFiles >= enqueueFiles) {
                                expectedFiles = -1;
                                if (expectedFilesRoot.listFiles().length > 0) {
                                    activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.please_retry_upload), 0);
                                } else {
                                    //AppResultReceiver.recordList = new ArrayList<>();
                                    activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.success_upload), 0);
                                    activity.updateViewInfo();
                                }
                                activity.setUploadStatus(false);
                            }

 */
//                            try {
//                                final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//                                File targetTxt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                                if (!targetTxt.exists()) {
//                                    writeToFile(targetTxt.getPath() + File.separator + "WoundcamRtc.log",
//                                            "End Now! " + response.getRawString()+"\r\n", true);
//                                }
//                            } catch (Exception e) {
//                                Log.v(TAG, "寫入txt檔錯誤：" + e.getMessage());
//                            }
                        }

                        @Override
                        public void onSuccess(String s, Response<String> response) {
                            super.onSuccess(s, response);

                            response.printInfo();

                            JSONObject mJsonObject = null;
                            try {
                                mJsonObject = new JSONObject(response.getResult());
                                String result = mJsonObject.getString("success");

                                if (result.toLowerCase().equals("true")) {
                                    try {
                                        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
                                        File backup = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.BackUp_DIR);
                                        File mediaFile = new File(backup.getPath() + "/" + file.getName());
                                        //copyFile(file.getAbsolutePath(), mediaFile.getAbsolutePath());
                                        Log.v("dasdmasldkamskldm",String.valueOf(file.getAbsolutePath()));

                                        if (WebViewJavaScriptInterface.bl_singleupload&&file.getAbsolutePath().contains(".txt")){
                                            WebViewJavaScriptInterface.bl_singleupload = false;
                                        } else {
                                            file.delete();
                                        }
                                        //刪除用不到的檔案
                                        String filePart = file.getName().substring(0, file.getName().lastIndexOf("_")); //切割刪除檔案之檔名內容
                                        Log.v(TAG, "檔名 : " + file.getPath() + " " + filePart);
                                        String delFileParam[]=filePart.split("_");
                                        int threed=199;
                                        threed+=Integer.parseInt(delFileParam[2]);
                                        String dsraw=delFileParam[0]+"_"+delFileParam[1]+"_"+String.valueOf(threed);
                                        Log.v(TAG,"dsraw :"+dsraw);
                                        FileHelper.isExist(mPicDir.getPath(), filePart + "_thm.raw", true);
                                        FileHelper.isExist(mPicDir.getPath(), dsraw + "_3ds.raw", true);
                                        FileHelper.isExist(mPicDir.getPath(), filePart + "_jpg2.jpg", true);
                                        FileHelper.isExist(mPicDir.getPath(), filePart + "_mak.png", true);
                                        FileHelper.isExist(mPicDir.getPath(), filePart + "_thm2.png", true);
                                        FileHelper.isExist(mPicDir.getPath(), filePart + "_gai.jpg", true);
                                        FileHelper.isExist(mPicDir.getPath(), filePart + "_roi.jpg", true);
                                        FileHelper.isExist(mPicDir.getPath(), dsraw + "_3ds.obj", true);
                                        FileHelper.isExist(mPicDir.getPath(), dsraw + "_3ds.mtl", true);



//
//
//
//                                        String delFileParam[] = file.getName().split("_");   //切割刪除檔案之檔名內容
//                                        String[] fileNames = mPicDir.list();    //尋找 Download>carameImg 檔案夾所有檔案
//                                        for (String fileName : fileNames) {
//                                            Log.v(TAG,"檔名 : "+fileName);
//                                            File compareFile = new File(mPicDir, fileName);
//                                            String compareFileParam[] = fileName.split("_");
//                                            if(compareFileParam[0].equals(delFileParam[0]) && compareFileParam[1].equals(delFileParam[1])&&(compareFileParam[3].equals("thm.raw")||compareFileParam[3].equals("3ds.raw")))
//                                            {
//                                                Log.v(TAG,"刪除檔名 : "+compareFile.getName());
//                                                compareFile.delete();
//                                            }
//
//                                        }

                                        activity.filesize(activity.file.getAbsolutePath());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(HttpException exception, Response<String> response) {
//                            super.onFailure(exception, response);
//                            Log.w(TAG, "onFailure:" + response.getResult());
//                            try {
//                                final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//                                File targetTxt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                                if (!targetTxt.exists()) {
//                                    writeToFile(targetTxt.getPath() + File.separator + "WoundcamRtc.log",
//                                            "Failure Now! :" + response.getRawString()+"\r\n", true);
//                                }
//                            } catch (Exception e) {
//                                Log.v(TAG, "寫入txt檔錯誤：" + e.getMessage());
//                            }



                            String msg = response.getException().toString();
                            try {
                                if (exception instanceof HttpNetException) {
                                    HttpNetException httpException = (HttpNetException) exception;
                                    msg = httpException.getExceptionType().reason + " (" + httpException.getExceptionType().chiReason + ") :" + httpException.getMessage();
                                } else  if (exception instanceof HttpClientException) {
                                    HttpClientException httpException = (HttpClientException) exception;
                                    msg = httpException.getExceptionType().reason + " (" + httpException.getExceptionType().chiReason + ") :" + httpException.getMessage();
                                } else  if (exception instanceof HttpServerException) {
                                    HttpServerException httpException = (HttpServerException) exception;
                                    msg = httpException.getExceptionType().reason + " (" + httpException.getExceptionType().chiReason + ") :" + httpException.getMessage();
                                } else {
                                    msg = msg.substring(msg.indexOf(":"));
                                 }
                            } catch (Exception eee) {
                            }
//                            HttpUtil.showTips(mMainActivity, "", "File "+(i+1) +" upload fail. " + msg);
                            mMainActivity.showDialog(mMainActivity.getString(R.string.alert_title), "File "+(i+1) +" upload fail. " + msg, 0);
                            // if  java.net.SocketException: sendto failed: EPIPE (Broken pipe), pls increment upload file size
                            super.onFailure(exception, response);
                            Log.w(TAG, "onFailure:" + response.getResult());
                            Log.w(TAG, "onFailure:exception" + exception);
                        }
                    })
                    .setHttpBody(body);

            if (mLiteHttp == null){
            }
            Response res = mLiteHttp.execute(upload);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private synchronized boolean uploadSingleRecordForIMASBox(int i, String evlId, String time, String itemid, final File file, boolean lastfile) {
        try {
            // String uploadUrl = "";

            String uploadUrl = DEFAULT_UPLOAD_PATH;

            Log.v(TAG,"uploadUrl"+uploadUrl);
            String[] splitDateTime = evlId.split(" ");
            MultipartBody body = new MultipartBody();
            long timestamp = TimeHelper.getTimeFormat(splitDateTime[0] + " " + splitDateTime[1].replace("-", ":").replace(".", ":"));

            JSONObject test = new JSONObject();
            JSONObject test1 = new JSONObject();
            List<Map<String, Object>> listMeasure = null;

            if(dataEncrypt==false){
                listMeasure = database.queryRecordByfilepath(file.getName());

            }else{
                listMeasure = sqllitesecret.queryRecordByfilepath(Sercretdb,file.getName());
            }
            if (listMeasure.size() > 0) {

                JSONObject infoObj = new JSONObject();

                userId = String.valueOf(listMeasure.get(0).get("userid"));
                if (AppResultReceiver.ZONE.equals("TZUCHI") || AppResultReceiver.ZONE.equals("TC")) {
                    test.put("location", AppResultReceiver.ZONE);
                }
                ArrayList<String> itemIds = new ArrayList<String>();
                String fever = String.valueOf(listMeasure.get(0).get("fever"));
                String smell =String.valueOf(listMeasure.get(0).get("smell"));
                String level = String.valueOf(listMeasure.get(0).get("level"));
                String character = String.valueOf(listMeasure.get(0).get("character"));
                String overtime =String.valueOf(listMeasure.get(0).get("overtime"));
                String occur = String.valueOf(listMeasure.get(0).get("occur"));

                if(StringUtils.isNotBlank(fever)){
                    itemIds.add(fever);
                }
                if(StringUtils.isNotBlank(smell)){
                    itemIds.add(smell);
                }
                if(StringUtils.isNotBlank(level)){
                    itemIds.add(level);
                }
                if(StringUtils.isNotBlank(character)){
                    itemIds.add(character);
                }
                if(StringUtils.isNotBlank(overtime)){
                    itemIds.add(overtime);
                }
                if(StringUtils.isNotBlank(occur)){
                    itemIds.add(occur);
                }

                infoObj.put("itemIds", itemIds);
                infoObj.put("itemNo", itemid);
                infoObj.put("analysisTime", String.valueOf(listMeasure.get(0).get("analysisTime")));
                test.put("statusInfo",infoObj);
                test.put("evlId",Long.toString(timestamp));
                test.put("devId","HTC-A9");
                test.put("divNo","2");//科別
                test.put("ownerId",String.valueOf(listMeasure.get(0).get("ownerId")));
                test.put("userId",String.valueOf(listMeasure.get(0).get("userid")));
                test1.put("itemNo",itemid);
                test1.put("bodyPart",String.valueOf(listMeasure.get(0).get("part")));
                test1.put("widthPixel",String.valueOf(listMeasure.get(0).get("widthPixel")));
                test1.put("heightPixel",String.valueOf(listMeasure.get(0).get("heightPixel")));
                test1.put("distance",String.valueOf(listMeasure.get(0).get("distance")));
                test1.put("height",String.valueOf(listMeasure.get(0).get("height")));
                test1.put("width",String.valueOf(listMeasure.get(0).get("width")));
                test1.put("area",String.valueOf(listMeasure.get(0).get("area")));
                test1.put("epithelium",String.valueOf(listMeasure.get(0).get("epithelium")));
                test1.put("granular",String.valueOf(listMeasure.get(0).get("granular")));
                test1.put("slough",String.valueOf(listMeasure.get(0).get("slough")));
                test1.put("eschar",String.valueOf(listMeasure.get(0).get("eschar")));
                test1.put("analysisTime",String.valueOf(listMeasure.get(0).get("analysisTime")));
                test1.put("newInfo",String.valueOf(listMeasure.get(0).get("lastanalysis")));
                test.put("contents",test1);


            }
            else{

                String[] fileSplit = file.getName().split("_");

                if(dataEncrypt==false){
                    listMeasure = database.queryRecordByevid(fileSplit[0]);

                }else{
                    listMeasure = sqllitesecret.queryRecordByevid(Sercretdb,fileSplit[0]);
                }
                if (listMeasure.size() > 0) {
                    for (Map<String, Object> map : listMeasure) {
                        userId = String.valueOf((Integer) map.get("userid"));
                    }
                }
                else{
                    userId = activity.userId;
                }
            }

            Log.v(TAG, "file:" + file.getAbsolutePath());
            Log.v(TAG, "上傳JSON格式:" + test.toString());

            body.addPart(new StringPart("parameter", test.toString()))
                   // .addPart(new InputStreamPart("parameter", ins))
                    .addPart(new FilePart("myImg", file));

            //  body.addPart(new FilePart("myImg", file));
            final StringRequest upload = new StringRequest(uploadUrl)
                    .setMethod(HttpMethods.Post)
                    .setHttpListener(new HttpListener<String>(false, false, false) {

                        public void onStart(AbstractRequest<String> request) {
                            Log.w(TAG, "onStart");
                            enqueueFiles++;
                        }

                        public void onCancel(String data, Response<String> response) {
                            Log.w(TAG, "onCancel");
//                            try {
//                                final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//                                File targetTxt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                                if (!targetTxt.exists()) {
//                                    writeToFile(targetTxt.getPath() + File.separator + "WoundcamRtc.log",
//                                            "Cancel Now! " + response.getRawString()+"\r\n", true);
//                                }
//                            } catch (Exception e) {
//                                Log.v(TAG, "寫入txt檔錯誤：" + e.getMessage());
//                            }
                            String msg = response.getException().toString();
                            try {
                                msg = msg.substring(msg.indexOf(":"));
                            } catch (Exception eee) {
                            }
                            HttpUtil.showTips(mMainActivity, "", "Upload Cancel " + i + msg);
                        }

                        public void onLoading(AbstractRequest<String> request, long total, long len) {
                            //Log.w(TAG, "onLoading " + len + "/" + total);
                        }

                        public void onUploading(AbstractRequest<String> request, long total, long len) {
                            Log.w(TAG, "onUploading " + len + "/" + total);
                            Date ddd = new Date();
                            if (ddd.getSeconds() % 10 == 0)
                                Toast.makeText(mMainActivity, "Uploading file " + i + ":" + len + "/" + total, Toast.LENGTH_SHORT);
                        }

                        @Override
                        public void onRedirect(AbstractRequest<String> request, int max, int times) {
                            Log.w(TAG, "onRedirect");
//
//                            try {
//                                final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//                                File targetTxt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                                if (!targetTxt.exists()) {
//                                    writeToFile(targetTxt.getPath() + File.separator + "WoundcamRtc.log",
//                                            "Redirect max num: " + max + " , times: " + times  +" GO-TO: " + timestamp+"\r\n", true);
//                                }
//                            } catch (Exception e) {
//                                Log.v(TAG, "寫入txt檔錯誤：" + e.getMessage());
//                            }
                        }

                        @Override
                        public void onRetry(AbstractRequest<String> request, int max, int times) {
                            Log.w(TAG, "onRetry");
//                            try {
//                                final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//                                File targetTxt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                                if (!targetTxt.exists()) {
//                                    writeToFile(targetTxt.getPath() + File.separator + "WoundcamRtc.log",
//                                            "Retry Now! max num: " + max + " , times: " + times  +" GO-TO: " + timestamp+"\r\n", true);
//                                }
//                            } catch (Exception e) {
//                                Log.v(TAG, "寫入txt檔錯誤：" + e.getMessage());
//                            }
                        }

                        @Override
                        public void onEnd(Response<String> response) {
                            Log.w(TAG, "onEnd");
                            dequeueFiles++;
                            if (enqueueFiles >= expectedFiles && dequeueFiles >= enqueueFiles) {
                                expectedFiles = -1;
                                if (expectedFilesRoot.listFiles().length > 0) {

                                    activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.please_retry_upload), 0);
                                } else {
                                    //AppResultReceiver.recordList = new ArrayList<>();
                                    activity.showDialog(mMainActivity.getString(R.string.alert_title), mMainActivity.getString(R.string.success_upload), 0);
                                    activity.updateViewInfo();
                                }
                                activity.setUploadStatus(false);
                            }
//                            try {
//                                final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//                                File targetTxt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                                if (!targetTxt.exists()) {
//                                    writeToFile(targetTxt.getPath() + File.separator + "WoundcamRtc.log",
//                                            "End Now! " + response.getRawString()+"\r\n", true);
//                                }
//                            } catch (Exception e) {
//                                Log.v(TAG, "寫入txt檔錯誤：" + e.getMessage());
//                            }
                        }

                        @Override
                        public void onSuccess(String s, Response<String> response) {
                            super.onSuccess(s, response);

                            response.printInfo();

                            JSONObject mJsonObject = null;
                            try {
                                mJsonObject = new JSONObject(response.getResult());
                                String result = mJsonObject.getString("success");

                                if (result.toLowerCase().equals("true")) {
                                    try {
                                        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
                                        File backup = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.BackUp_DIR);
                                        File mediaFile = new File(backup.getPath() + "/" + file.getName());
                                        copyFile(file.getAbsolutePath(), mediaFile.getAbsolutePath());
                                        String test[]=file.getName().split("_");
                                        file.delete();
                                        File [] findAllMatchFiles1 = mPicDir.listFiles(new FilenameFilter() {
                                            @Override
                                            public boolean accept(File dir, String name) {


                                                return (name.startsWith(test[0])
                                                        //  ||(name.startsWith(delFileParam[0]) && name.endsWith("_mak.png")
                                                        //     ||(name.startsWith(delFileParam[0]) && name.endsWith("_mak.png")

                                                );   //挑出所有符合檔案名稱字串條件的檔案
                                            }
                                        });
                                        Log.d(TAG, "剩餘檔案數量: " + findAllMatchFiles1.length);
                                        if(findAllMatchFiles1.length - 1 == 0){  //檢查同檔名之檔案數於圖檔刪除後的數量是否為0，為0則刪除同檔名之txt檔
                                            String delTxtName = test[0] + "_" + test[1] + "_13_data.txt";

                                            File delTxt = new File(mPicDir.getPath() + File.separator + delTxtName );
                                            delTxt.delete();
                                            Log.d(TAG, "執行刪除TXT完畢: " + delTxt.getName());
                                        }
                                        String delTxtName = test[0] + "_" + test[1] + "_13_data.txt";
                                        File delTxt = new File(mPicDir.getPath() + File.separator + delTxtName );
                                        delTxt.delete();
                                        activity.filesize(activity.file.getAbsolutePath());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(HttpException exception, Response<String> response) {
                            //                            try {
//                                final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
//                                File targetTxt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                                if (!targetTxt.exists()) {
//                                    writeToFile(targetTxt.getPath() + File.separator + "WoundcamRtc.log",
//                                            "Failure Now! :" + response.getRawString()+"\r\n", true);
//                                }
//                            } catch (Exception e) {
//                                Log.v(TAG, "寫入txt檔錯誤：" + e.getMessage());
//                            }
                            String msg = response.getException().toString();
                            try {
                                if (exception instanceof HttpNetException) {
                                    HttpNetException httpException = (HttpNetException) exception;
                                    msg = httpException.getExceptionType().reason + " (" + httpException.getExceptionType().chiReason + ") :" + httpException.getMessage();
                                } else  if (exception instanceof HttpClientException) {
                                    HttpClientException httpException = (HttpClientException) exception;
                                    msg = httpException.getExceptionType().reason + " (" + httpException.getExceptionType().chiReason + ") :" + httpException.getMessage();
                                } else  if (exception instanceof HttpServerException) {
                                    HttpServerException httpException = (HttpServerException) exception;
                                    msg = httpException.getExceptionType().reason + " (" + httpException.getExceptionType().chiReason + ") :" + httpException.getMessage();
                                } else {
                                    msg = msg.substring(msg.indexOf(":"));
                                }
                            } catch (Exception eee) {
                            }
//                            HttpUtil.showTips(mMainActivity, "", "File "+(i+1) +" upload fail. " + msg);
                            mMainActivity.showDialog(mMainActivity.getString(R.string.alert_title), "File "+(i+1) +" upload fail. " + msg, 0);
                            // if  java.net.SocketException: sendto failed: EPIPE (Broken pipe), pls increment upload file size
                            super.onFailure(exception, response);
                            Log.w(TAG, "onFailure:" + response.getResult());
                            Log.w(TAG, "onFailure:exception" + exception);
                        }
                    })
                    .setHttpBody(body);

            Response res = mLiteHttp.execute(upload);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //檔存在時
                InputStream inStream = new FileInputStream(oldPath); //讀入原檔
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //位元組數 檔案大小
                    //System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("複製單個檔操作出錯");
            e.printStackTrace();
        }
    }

    public static List<File> getUploadableFiles(final Context ctx) {

        File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
        if (!mPicDir.exists()) {
            Log.d(TAG, "目錄 " + mPicDir.getAbsolutePath() + "不存在");
            return null;
        }

        List<File> uploadableFiles = new ArrayList<File>();
        try {
            // 取得.txt及jpg.jpg/thm.png檔案列表, K: imageId, V: imagePath
            //Map<String, String> map = FileUtility.getImageListFromDownload(ctx);
            Map<String, String> txtAndJpgFileNames = FileUtility.getAllListFromDownload(ctx);
            Log.v(TAG,"txtAndJpgFiles.size"+txtAndJpgFileNames.size());
            if (txtAndJpgFileNames == null) { // 沒有圖片
                return null;
            }
            // --------------------------------

            // 從圖片中去取出groupId = EvalId
            // K: groupId = EvalId (單次看診唯一碼), V: itemId vs. imagePath Map
            Map<String, Map<String, String>> collectedGroupIds = new TreeMap<String, Map<String, String>>() {
                @Nullable
                @Override
                public Comparator<? super String> comparator() {
                    return new Comparator<String>() {
                        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH-mm-ss-SSS");

                        @Override
                        public int compare(String string1, String string2) {
                            try {
                                Date date1 = format.parse(string1);
                                Date date2 = format.parse(string2);
                                return date1.after(date2) ? 1 : -1;
                            } catch (Exception ex) {
                                Log.e(TAG, "EvlId格式有錯誤", ex);
                                throw new RuntimeException(ex);
                            }
                        }
                    };
                }
            };
            //
            for (String fileName : txtAndJpgFileNames.keySet()) {
                Log.v(TAG,"fileName:"+fileName);
                // imageId 是檔案名稱，格式為：
                // yyyy-MM-dd HH-mm-ss-SSS_yyyy-MM-dd_sn_jpg.jpg
                int pos = Math.max(Math.max(Math.max(fileName.lastIndexOf("_data.txt"), fileName.lastIndexOf("_jpg.jpg")), fileName.lastIndexOf("_thm.png")), fileName.lastIndexOf("_3ds.jpg"));
                if (pos > 0) {
                    String temp = fileName.substring(0, pos);
                    //
                    String[] field = temp.split("_");
                    if (field.length != 3) {
                        // 格式有錯誤？
                        Log.d(TAG, "格式有錯誤？ " + temp);
                        continue; // SKIP
                    }
                    // evlId = groupId, date, itemId
                    String groupId = field[0];
                    String itemId = field[2];
                    // K: itemId, V: imagePath(imageId)
                    Map<String, String> fileMap = null;
                    if (collectedGroupIds.containsKey(groupId)) {
                        fileMap = collectedGroupIds.get(groupId);
                    } else {
                        fileMap = new TreeMap<String, String>();
                        collectedGroupIds.put(groupId, fileMap);
                    }
                    fileMap.put(itemId, fileName);
                }
            }
            // --------------------------------

            int sendCount = 0;
            int noBodyPart = 0;
            int noOwnerId = 0;
            int noTxt = 0;
            // 對每一個群組進行照片與Txt資料的合併
            StringBuilder msg = new StringBuilder();
            for (String groupId : collectedGroupIds.keySet()) {
                TxtData txtObject = FileUtility.getTxtDataFromDownload(ctx, groupId);
                Map<String, String> filenameMap = collectedGroupIds.get(groupId);
                if (txtObject == null) {
                    // 只有jpg沒有txt的集合, 可能之前已經上傳過txt及jpg, 現在補拍jpg, 則直接上傳jpg
                    Log.d(TAG, "群組 " + groupId + " 不存在Txt資料");
                    final String evlId = groupId; // 格式 yyyy-MM-dd HH-mm-ss-SSS
                    for (String itemId : filenameMap.keySet()) {
                        String imageId = filenameMap.get(itemId);
                        try {
                            if (!imageId.endsWith("_data.txt")) {
                                if (imageId.endsWith("jpg.jpg"))
                                    sendCount++;
                                File imageFile = new File(mPicDir, imageId);
                                uploadableFiles.add(imageFile);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Log.e(TAG, "Upload File Error", ex);
                        }
                    }
                } else {
                    // 同時存在txt及jpg的集合, 或只有txt的集合, 從txt的Info中, 檢查每個圖片是否已設定 BodyPart
                    Log.v(TAG,"同時存在.txt及jpg.jpg/thm.png的集合, 或只有txt的集合, 從txt的Info中, 檢查每個圖片是否已設定 BodyPart");
                    Map<String, JSONObject> info = txtObject.getInfo();
                    int available_count = 0;
                    int total_count = 0;
                    for (String itemId : filenameMap.keySet()) {
                        String imageFilename = filenameMap.get(itemId); // 格式 yyyy-MM-dd HH-mm-ss-SSS_yyyy-MM-dd_sn_jpg.jpg
                        //don't check txt, only check jpg's data with txt
                        if (!imageFilename.endsWith("_data.txt")) {
                            total_count++;
                            String imagePath = txtAndJpgFileNames.get(imageFilename);
                            JSONObject _info = info.get(itemId);
                            if(IS_FOR_IMAS_BOX==true){

                            }else {
                                if (_info==null || _info.isNull("bodyPart") || _info.getString("bodyPart") == null || _info.getString("bodyPart").isEmpty()) {
                                    _info = info.get(itemId+99);
                                    if (_info==null || _info.isNull("bodyPart") || _info.getString("bodyPart") == null || _info.getString("bodyPart").isEmpty()) {
                                        _info = info.get(itemId+199);
                                        if (_info==null || _info.isNull("bodyPart") || _info.getString("bodyPart") == null || _info.getString("bodyPart").isEmpty()) {
                                            _info = info.get(itemId+299);
                                            if (_info==null || _info.isNull("bodyPart") || _info.getString("bodyPart") == null || _info.getString("bodyPart").isEmpty()) {
                                                //msg.append("not picture assign bodyPart")
                                                if (imageFilename.endsWith("jpg.jpg"))
                                                    noBodyPart++;
                                                msg.append("圖片 ").append(imagePath).append(" 未設定 BodyPart\n");
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            available_count++;

                        }
                    }
                    // 若都已填完OwnerId及bodyPart, 則依序上傳檔案
                    // 若都已填完OwnerId及bodyPart, 則依序上傳檔案
                    if(IS_FOR_IMAS_BOX==true){
                        if (available_count == total_count) {
                            // 先上傳全部jpg
                            final String evlId = groupId; // 格式 yyyy-MM-dd HH-mm-ss-SSS
                            for (String itemId : filenameMap.keySet()) {
                                String imageFilename = filenameMap.get(itemId);
                                try {
                                    if (!imageFilename.endsWith("_data.txt")) {
                                        if (imageFilename.endsWith("jpg.jpg"))
                                            sendCount++;
                                        File imageFile = new File(mPicDir, imageFilename);
                                        Log.v(TAG, "上傳圖檔名：" + imageFile.getName());
                                        uploadableFiles.add(imageFile);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Log.e(TAG, "Upload File Error", ex);
                                }
                            }
                            // 再上傳txt
                            for (String itemId : filenameMap.keySet()) {
                                String imageFilename = filenameMap.get(itemId);
                                try {
                                    if (imageFilename.endsWith("_data.txt")) {
                                        File imageFile = new File(mPicDir, imageFilename);
                                        Log.v(TAG, "上傳TXT檔名：" + imageFile.getName());
                                        uploadableFiles.add(imageFile);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Log.e(TAG, "Upload File Error", ex);
                                }
                            }
                        }
                    }else{
                        // 全部jpg都有填bodyPart
                        if (available_count == total_count) {
                            // 有填ownerId
                            if (StringUtils.isNotBlank(txtObject.getOwnerId())) {
                                // 巡所有檔, 先上傳全部jpg
                                final String evlId = groupId; // 格式 yyyy-MM-dd HH-mm-ss-SSS
                                for (String itemId : filenameMap.keySet()) {
                                    String imageFilename = filenameMap.get(itemId);
                                    Log.v(TAG, "imageFilename:" + imageFilename);
                                    try {
                                        if (!imageFilename.endsWith("_data.txt")) {
                                            if (imageFilename.endsWith("jpg.jpg"))
                                                sendCount++;
                                            File imageFile = new File(mPicDir, imageFilename);
                                            Log.v(TAG, "上傳圖檔名：" + imageFile.getName());
                                            uploadableFiles.add(imageFile);
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        Log.e(TAG, "Upload File Error", ex);
                                    }
                                }
                                // 巡所有檔, 再上傳txt
                                for (String itemId : filenameMap.keySet()) {
                                    String imageFilename = filenameMap.get(itemId);
                                    try {
                                        if (imageFilename.endsWith("_data.txt")) {
                                            File imageFile = new File(mPicDir, imageFilename);
                                            Log.v(TAG, "上傳TXT檔名：" + imageFile.getName());
                                            uploadableFiles.add(imageFile);
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        Log.e(TAG, "Upload File Error", ex);
                                    }
                                }
                            } else {
                                noOwnerId++;
                                /// msg.append("no owner id")
                            }
                        } else {
                            /// msg.append("not all picture assign bodyPart")
                        }
                    }
                }
            }
            Log.d(TAG, "LOG " + msg.toString());
            String summary = "Total files send:" + sendCount + ", No send:" + (noOwnerId + noBodyPart);
            if (noOwnerId > 0) summary = summary + "\n" + noOwnerId + " No OwnerId";
            if (noBodyPart > 0) summary = summary + "\n" + noBodyPart + " No BodyPart";
            if (noTxt > 0) summary = summary + "\n" + noTxt + " No Description";
            if (summary.endsWith(",")) summary = summary.substring(0, summary.length() - 1);
            String finalSummary = summary;
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    HttpUtil.showTips(mMainActivity, "", summary);
                    mMainActivity.showDialog(mMainActivity.getString(R.string.start_file_upload), finalSummary, 0);
                }
            });
            return uploadableFiles;
        } catch (Exception ex) {
            Log.e(TAG, "getTargetFiles 錯誤", ex);
            //
            return null;
        }
    }


//    private void single_walkFilesEx(File root) {
//
//
//        int uploadCount = 1;
//
//
//        //upload jpg
//        try {
//            if (filenameMap.get(itemId).endsWith("_jpg.jpg")) {
//                Log.d(TAG, "----------------------上傳中----------------------");
//                Log.d(TAG, "正在上傳的檔名為: " + filenameMap.get(itemId));
//                String[] nameSplit = filenameMap.get(itemId).split("_");
//                if (IS_FOR_IMAS_BOX == true) {
//                    uploadSingleRecordForIMASBox(uploadCount, nameSplit[0], nameSplit[1], itemId, new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)), false);
//                } else {
//                    uploadSingleRecord(uploadCount, nameSplit[0], nameSplit[1], itemId, new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)), false);
//                }
//            }
//
//            Thread.sleep(10);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        //upload png
//        try {
//            Integer intItemId = Integer.parseInt(itemId);
//            if (filenameMap.get(itemId).endsWith("_thm.png")) {
//                Log.d(TAG, "----------------------上傳中----------------------");
//                Log.d(TAG, "正在上傳的檔名為: " + filenameMap.get(itemId));
//                String[] nameSplit = filenameMap.get(itemId).split("_");
//                if (IS_FOR_IMAS_BOX == true) {
//                    //   uploadSingleRecordForIMASBox(uploadCount, nameSplit[0], nameSplit[1], itemId, new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)), false);
//                } else {
//                    uploadSingleRecord(uploadCount, nameSplit[0], nameSplit[1], itemId, new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)), false);
//                }
//            }
//
//            Thread.sleep(10);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        //upload txt
//        try {
//            Integer intItemId = Integer.parseInt(itemId);
//            if (filenameMap.get(itemId).endsWith("_data.txt") || filenameMap.get(itemId).endsWith("_datax.txt")) {
//
//                boolean foundFiles = false;
//                // 3.3.1 檢查對應的 jpg, png是否都上傳完畢, 不在目錄中
//                for (String checkItemId : filenameMap.keySet()) {
//                    if (intItemId != 0 && FileHelper.isExist(mPicDir.getAbsolutePath() , filenameMap.get(itemId), false)) {
//                        foundFiles = true;
//                        break;
//                    }
//                }
//
//                // 3.3.2 都上傳完畢, 就上傳txt
//                if (!foundFiles) {
//                    Log.d(TAG, "----------------------上傳中----------------------");
//                    Log.d(TAG, "正在上傳的檔名為: " + filenameMap.get(itemId));
//                    String[] nameSplit = filenameMap.get(itemId).split("_");
//                    String filename=filenameMap.get(itemId);
//                    Log.v(TAG, "sercrettxt :　"+filename);
//                    if(dataEncrypt==true){
//
//                        if(filenameMap.get(itemId).endsWith("_datax.txt")){
//
//                            File sercrettxt=new File(mPicDir,filename);
//
//                            try {
//                                FileHelper.txt_decrypt(sercrettxt.getAbsolutePath(),18);
//                                filename=sercrettxt.getName().replace("_datax.txt","_data.txt");
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                    }
//                    Log.v(TAG, "sercrettxt :　"+filename);
//                    if (IS_FOR_IMAS_BOX == true) {
//                        //   uploadSingleRecordForIMASBox(uploadCount, nameSplit[0], nameSplit[1], "13", new File(mPicDir.getAbsolutePath() + "/" + filenameMap.get(itemId)), false);
//                    } else {
//                        uploadSingleRecord(uploadCount, nameSplit[0], nameSplit[1], "13", new File(mPicDir.getAbsolutePath() + "/" + filename), false);
//                    }
//
//                }
//            }
//
//            Thread.sleep(10);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }



}