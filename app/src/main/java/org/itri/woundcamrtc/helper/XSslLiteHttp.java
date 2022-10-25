package org.itri.woundcamrtc.helper;


import android.content.Context;

import com.litesuits.http.LiteHttp;
import com.litesuits.http.impl.huc.HttpUrlClient;

import org.itri.woundcamrtc.AppResultReceiver;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



public class XSslLiteHttp {

    private MyTrustManager mMyTrustManager;
    private LiteHttp mLiteHttp;

    public XSslLiteHttp(Context mContext, int connectTimeout, int readTimeout) {

        if (AppResultReceiver.ALLOW_XSSL) {
            HttpUrlClient httpUrlClient = new HttpUrlClient();
            httpUrlClient.setHostnameVerifier(DO_NOT_VERIFY);
            httpUrlClient.setSslSocketFactory(createSSLSocketFactory());

            mLiteHttp = LiteHttp.build(mContext)
                    .setContext(mContext)
                    .setHttpClient(httpUrlClient) // default: HttpUrlClient
                    .setDebugged(true)                     // log output when debugged
                    .setDoStatistics(false)                // default:false
                    .setDetectNetwork(false)              // default:false
                    .setSocketTimeout(readTimeout * 1000)           // default: 20000, our: 120000
                    .setConnectTimeout(connectTimeout * 1000)         // default: 20000, our: 5000
                    //.setRetrySleepMillis(1000) // default: 3000, our: 1000
                    .setConcurrentSize(1)  // default:cpu core number
                    .setDefaultMaxRetryTimes(0) // default:3
                    .setDefaultMaxRedirectTimes(10) // default:5
                    .setWaitingQueueSize(100) // default:ConcurrentSize*20
                    //.setMaxMemCacheBytesSize(5242880L) // default:524288L
                    .create();
        } else {
            mLiteHttp = LiteHttp.build(mContext)
                    .setContext(mContext)
                    .setHttpClient(new HttpUrlClient())
                    .setDebugged(true)                     // log output when debugged
                    .setDoStatistics(false)                // default:false
                    .setDetectNetwork(false)              // default:false
                    .setSocketTimeout(readTimeout)           // default: 20000, our: 120000
                    .setConnectTimeout(connectTimeout)         // default: 20000, our: 5000
                    //.setRetrySleepMillis(1000) // default: 3000, our: 1000
                    .setConcurrentSize(1)  // default:cpu core number
                    .setDefaultMaxRetryTimes(0) // default:3
                    .setDefaultMaxRedirectTimes(10) // default:5
                    .setWaitingQueueSize(100) // default:ConcurrentSize*20
                    //.setMaxMemCacheBytesSize(5242880L) // default:524288L
                    .create();
        }

    }

    public LiteHttp getLiteHttp() {
        return mLiteHttp;
    }

    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            mMyTrustManager = new MyTrustManager();
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{mMyTrustManager}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return ssfFactory;
    }

    //实现X509TrustManager接口
    protected class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}