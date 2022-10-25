package org.itri.woundcamrtc.helper;


import android.content.Context;
import android.util.Log;

import org.itri.woundcamrtc.AppResultReceiver;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

import okhttp3.OkHttpClient;

import static android.provider.Telephony.Mms.Part.CHARSET;
import static org.itri.woundcamrtc.AppResultReceiver.ALLOW_XSSL;

public class XSslHttpURLConnection {

    //InputStream is = (InputStream) (new XSslHttpURLConnection()).getImageStream(url,5,10);

    public static String TAG = "AllowSslHttpURLConnection";

    public XSslHttpURLConnection() {

    }

    public InputStream getImageStream(String urlParam, int connectTimeout, int readTimeout) throws Exception {
        Log.i(TAG, "ThreadId=" + Thread.currentThread().getId());
        URL url = new URL(urlParam);
        HttpURLConnection conn = null;

        //**关键代码**
        //ignore https certificate validation |忽略 https 证书验证
        if (url.getProtocol().toUpperCase().equals("HTTPS")) {
            if (ALLOW_XSSL) {
                trustAllHosts();
            }
            HttpsURLConnection https = (HttpsURLConnection) url
                    .openConnection();
            if (AppResultReceiver.ALLOW_XSSL) {
                https.setHostnameVerifier(DO_NOT_VERIFY);
            }
            conn = https;
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }

        conn.setConnectTimeout(connectTimeout * 1000);
        conn.setReadTimeout(readTimeout * 1000);

        conn.setRequestMethod("GET");

        Log.i(TAG, "conn.getResponseCode()------" + conn.getResponseCode());
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream();
        }
        return null;
    }

    private void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}