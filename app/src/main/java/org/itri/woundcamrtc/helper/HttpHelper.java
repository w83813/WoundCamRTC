package org.itri.woundcamrtc.helper;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.litesuits.http.HttpConfig;
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
import com.litesuits.http.impl.huc.HttpUrlClient;
import com.litesuits.http.listener.HttpListener;
import com.litesuits.http.request.AbstractRequest;
import com.litesuits.http.request.StringRequest;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;

import java.net.InetAddress;


public class HttpHelper {

    private final String TAG = "HttpHelper";

    public static final String KEY_RECEIVED_MESSAGE = "http.helper.key.received.message";

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

    protected Context mContext;
    protected LiteHttp mLiteHttp;
    private ResultReceiver mReceiver;

    private int mPingUrlStatus = 0;
    private String mPingUrlMessage = "";

    public HttpHelper(Context context, ResultReceiver receiver) {
        this.mContext = context;
        this.mReceiver = receiver;
    }

    /**
     * 初始化
     */
    protected void initLiteHttp() {
        if (mLiteHttp == null) {
            mLiteHttp = (new XSslLiteHttp(mContext,10,360)).getLiteHttp();
//            mLiteHttp = LiteHttp.build(mContext)
//                    .setHttpClient(new HttpUrlClient())       // http client
////                    .setDebugged(true)                     // log output when debugged
////                    .setDoStatistics(true)                // statistics of time and traffic
////                    .setDetectNetwork(true)              // detect network before connect
////                    .setSocketTimeout(30 * 1000)           // socket timeout: 30s
////                    .setConnectTimeout(30 * 1000)         // connect timeout: 30s
//                    .create();

            HttpConfig newConfig = mLiteHttp.getConfig();
            newConfig.setDefaultCharSet("utf-8");
        } else {
            clearLiteHttp();
            initLiteHttp();
        }
    }

    /**
     * 執行上傳
     */
    public void startUploadAsync(final AbstractRequest request) {
        if (mLiteHttp == null) {
            initLiteHttp();
        }
        try {
            mLiteHttp.executeAsync(request);
        } catch (Exception e) {
            e.printStackTrace();
            mPingUrlStatus = STATUS_TIMEOUT;
        }
    }

    /**
     * 釋放資源
     */
    public void clearLiteHttp() {
        if (mLiteHttp != null) {
            this.mLiteHttp.clearMemCache();
            this.mLiteHttp = null;
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
        } catch (Exception ex2) {
            ex2.printStackTrace();
            return false;
        }
    }


    protected void brocastFinishStatus(ResultReceiver mReceiver, int status_code, String msg) {
        Bundle mBundle = new Bundle();
        mBundle.putString(KEY_RECEIVED_MESSAGE, msg);
        mReceiver.send(status_code, mBundle);
    }


    public void pingUrlAsync(String strUrl) {

        Log.d(TAG, "pingUrlAsync start");

        final StringRequest postRequest = new StringRequest(strUrl);
        postRequest.setMaxRetryTimes(0);
        postRequest.setSocketTimeout(2000);
        postRequest.setMethod(HttpMethods.Get);
        postRequest.setHttpListener(new HttpListener<String>() {
            @Override
            public void onSuccess(String s, Response<String> response) {
                super.onSuccess(s, response);
                brocastFinishStatus(mReceiver, STATUS_FINISHED, "");
            }

            @Override
            public void onFailure(HttpException e, Response<String> response) {
                super.onFailure(e, response);
                brocastFinishStatus(mReceiver, STATUS_TIMEOUT, "");
            }
        });

        startUploadAsync(postRequest);

    }

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
        startUploadAsync(postRequest);

        while (mPingUrlStatus == STATUS_RUNNING) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
                mPingUrlStatus = STATUS_TIMEOUT;
            }
        }
        if (mPingUrlStatus != STATUS_TIMEOUT)
            return true;
        else
            return false;
    }
}
