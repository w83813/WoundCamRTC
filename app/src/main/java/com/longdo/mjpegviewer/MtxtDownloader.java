package com.longdo.mjpegviewer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.nfc.Tag;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.MainActivity;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;

public class MtxtDownloader {

    private static final int WAIT_AFTER_READ_TXT_ERROR_MSEC = 5000;
    //硬碟設定(設定為4096 bytes)
    private static final int CHUNK_SIZE = 4096;
    //得到類的簡寫名稱
    private final String tag = getClass().getSimpleName();

    private Context context;
    private String url;
    private MainActivity activity;
    private txtDownloader downloader;
    private final Object lockBitmap = new Object();
    public String content = "";

    //讀寫圖片錯誤等待時間(毫秒)
    private int msecWaitAfterReadTxtError = WAIT_AFTER_READ_TXT_ERROR_MSEC;


    public MtxtDownloader(Context context, MainActivity activity) {
        this.context = context;
        this.activity = activity;
    }

    //設定URL
    public void setUrl(String url) {
        this.url = url;
    }

    //開始接收熱感影像串流
    public void start() {
        //如果確認啟動
        if (downloader != null && downloader.isRunning()) {
            Log.w(tag, "Already started, stop by calling stopStream() first.");
            return;
        }

        downloader = new txtDownloader();
        //同步開始啟動
        downloader.start();
        Log.v(tag, "啟動");
    }

    public void stop() {
        try {
            downloader.cancel();
        } catch (Exception e) {
            Log.v(tag, "cancelError: " + e);
        }
        downloader = null;
    }

    public int getMsecWaitAfterReadTxtError() {
        return msecWaitAfterReadTxtError;
    }

    public void setMsecWaitAfterReadTxtError(int msecWaitAfterReadTxtError) {
        this.msecWaitAfterReadTxtError = msecWaitAfterReadTxtError;
    }

    public String getContent() {
        return content;
    }

    //新增一個名為txtDownloader的 Thread Class
    class txtDownloader extends Thread {

        private boolean run = true;

        public void cancel() {
            run = false;
        }

        public boolean isRunning() {
            return run;
        }

        @Override
        public void run() {
            while (run) {

                HttpURLConnection connection = null;
                BufferedInputStream bis = null;
                URL serverUrl = null;

                // start get messages
                try {
                    //取得serverURL
                    serverUrl = new URL(url);
                    //建立url物件能與HttpURLConnection對話
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestMethod("GET");
                    //開啟輸入流，方便傳入資料
                    connection.setDoInput(true);
                    connection.setConnectTimeout(1000);
                    Log.v(tag, "connect: " + url);
                    connection.connect();
                    Log.v(tag, "httpCode: " + connection.getResponseCode());

                    if (connection.getResponseCode() == 200) {

                        //正規表達式
                        String headerBoundary = "[_a-zA-Z0-9]*boundary"; // Default boundary pattern

                        try {
                            // Try to extract a boundary from HTTP header first.
                            // If the information is not presented, throw an exception and use default value instead.
                            //獲取伺服器傳送時所給的Content-Type
                            String contentType = connection.getHeaderField("Content-Type");
                            if (contentType == null) {
                                throw new Exception("Unable to get content type");
                            }

                            String[] types = contentType.split(";");
                            if (types.length == 0) {
                                throw new Exception("Content type was empty");
                            }

                            String extractedBoundary = null;
                            for (String ct : types) {
                                String trimmedCt = ct.trim();
                                if (trimmedCt.startsWith("boundary=")) {
                                    //擷取 boundary= 此字串後9碼
                                    extractedBoundary = trimmedCt.substring(9); // Content after 'boundary='
                                }
                            }

                            if (extractedBoundary == null) {
                                throw new Exception("Unable to find mjpeg boundary");
                            }

                            headerBoundary = extractedBoundary;
                        } catch (Exception e) {
                            Log.w(tag, "Cannot extract a boundary string from HTTP header with message: " + e.getMessage() + ". Use a default value instead.");
                        }

                        //determine boundary pattern
                        //use the whole header as separator in case boundary locate in difference chunks
                        //匹配所有字符包括换行符號
                        Pattern pattern = Pattern.compile("--" + headerBoundary + "\\s+(.*)\\r\\n\\r\\n", Pattern.DOTALL);

                        //Pattern pattern = Pattern.compile("\\r\\n",Pattern.DOTALL);
                        Matcher matcher;

                        bis = new BufferedInputStream(connection.getInputStream());
                        byte[] txt = new byte[0], read = new byte[CHUNK_SIZE], tmpCheckBoundry;
                        int readByte, boundaryIndex;
                        String checkHeaderStr, boundary;

                        //always keep reading txt from server (持續從server讀取txt檔)
                        while (run) {
                            try {
                                readByte = bis.read(read);

                                //no more data
                                if (readByte == -1) {
                                    break;
                                }

                                tmpCheckBoundry = addByte(txt, read, 0, readByte);
                                //將ASCII Byte[] 轉成 String
                                checkHeaderStr = new String(tmpCheckBoundry, "ASCII");
                                Log.v(tag, "checkHeaderStr=" + checkHeaderStr);

                                String[] content = checkHeaderStr.split("\r\n\r\n");
                                if (content != null && content.length >= 2) {
                                    String[] splitted = content[1].split("=");
                                    //String[] splitted = target.replace("\r\n", "").split("=");
                                    String behavior = "", relValue = "";
                                    if (splitted != null && splitted.length == 2) {
                                        behavior = splitted[0];
                                        relValue = splitted[1];
                                        //Log.v(tag, "行為 = " + behavior);
                                        //Log.v(tag, "數值 = " + String.valueOf(relValue));
                                        try {
                                            switch (behavior) {
                                                case "btn":
                                                    if (relValue.startsWith("1")) {
                                                        Log.v(tag, "按下btn按鈕");
                                                        activity.autoTakePic();
                                                    }
                                                    break;
                                                case "dist":
                                                    String[] relSplitted = relValue.replace("\0", "").split(" ");
                                                    AppResultReceiver.snapshutDistance = Double.parseDouble(relSplitted[0]);
                                                    if (AppResultReceiver.snapshutDistance != 0.0) {
                                                        activity.setDetectedDistance("測距: " + AppResultReceiver.snapshutDistance + " 公分");
                                                        //activity.setDetectedDistance(activity.getString(R.string.detect_distance) + ": " + AppResultReceiver.snapshutDistance + activity.getString(R.string.centimeter));
                                                    }
                                                    Log.v(tag, "距離為:" + relSplitted[0]);
                                                    break;
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                    }

                                    int headerIndex = checkHeaderStr.length();

                                    txt = addByte(new byte[0], read, headerIndex, readByte - headerIndex);
                                    //String showTxt = new String(txt, "ASCII");
                                    //showTxt=null;


//                                //使用 matcher 方法指定要比對的字串
//                                matcher = pattern.matcher(checkHeaderStr);
//                                if (matcher.find()) {
//                                    //boundary is found
//                                    //使用 group 方法傳回符合的字串
//                                    boundary = matcher.group(0);
//                                    Log.v(tag, "boundary= " + boundary);
//                                    //
//                                    boundaryIndex = checkHeaderStr.indexOf(boundary);
//                                    Log.v(tag, "boundaryIndex = " + String.valueOf(boundaryIndex));
//                                    //boundaryIndex -= txt.length;
//                                    String target = "";
//                                    /*if (boundaryIndex > 0) {
//                                        //txt = addByte(txt, read, 0, boundaryIndex);
//                                        target = checkHeaderStr.substring(0, boundaryIndex);
//                                    } else {
//                                        //txt = delByte(txt, -boundaryIndex);
//                                        target = checkHeaderStr.substring(boundary.length(), checkHeaderStr.length());
//                                    }*/
//                                    int startIndex = checkHeaderStr.indexOf("btn=1");
//                                    target = checkHeaderStr.substring(startIndex, startIndex + 5);
//
//                                    Log.v(tag, "找到的字串= " + target);
//
//                                    //String target = new String(txt, "ASCII");
//                                    //content = "";
//                                    String[] splitted = target.split("=");
//                                    //String[] splitted = target.replace("\r\n", "").split("=");
//                                    String behavior = "", relValue = "";
//                                    if (splitted != null && splitted.length >= 2) {
//                                        behavior = splitted[0];
//                                        relValue = splitted[1];
//                                        Log.v(tag, "行為 = " + behavior);
//                                        Log.v(tag, "數值 = " + String.valueOf(relValue));
//                                    }
//
//                                    switch (behavior) {
//                                        case "btn":
//                                            if (Integer.parseInt(relValue) == 1) {
//                                                Log.v(tag, "執行按鈕按下");
//                                                //content = "takePic";
//                                                activity.autoTakePic();
//                                            }
//                                            break;
//                                        case "dist":
//                                            Log.v(tag, "距離為:" + relValue);
//                                            //content = relValue;
//                                            activity.distance = Double.parseDouble(relValue);
//                                            break;
//                                    }
//
//                                    int headerIndex = boundaryIndex + boundary.length();
//
//                                    txt = addByte(new byte[0], read, headerIndex, readByte - headerIndex);
                                } else {
                                    txt = addByte(txt, read, 0, readByte);
                                    //String showTxt = new String(txt, "ASCII");
                                    //showTxt=null;
                                }
                            } catch (Exception e) {
                                if (e != null && e.getMessage() != null) {
                                    Log.e(tag, e.getMessage());
                                }
                                break;
                            }
                        }
                    }

                } catch (Exception e) {
                    if (e != null && e.getMessage() != null) {
                        Log.e(tag, e.getMessage());
                    }
                }


                try {
                    bis.close();
                } catch (Exception e) {
                    if (e != null && e.getMessage() != null) {
                        Log.e(tag, e.getMessage());
                    }
                } finally {
                    bis = null;
                }

                try {
                    connection.disconnect();
                    //Log.i(tag,"disconnected with " + url);
                } catch (Exception e) {
                    if (e != null && e.getMessage() != null) {
                        Log.e(tag, e.getMessage());
                    }
                } finally {
                    connection = null;
                    serverUrl = null;
                }

                if (msecWaitAfterReadTxtError > 0) {
                    try {
                        //可使目前的執行緒暫停執行一段時間(毫秒)
                        Thread.sleep(msecWaitAfterReadTxtError);
                    } catch (InterruptedException e) {
                        if (e != null && e.getMessage() != null) {
                            Log.e(tag, e.getMessage());
                        }
                    }
                }
            }
        }

        //增加檔案的Byte數
        private byte[] addByte(byte[] base, byte[] add, int addIndex, int length) {
            byte[] tmp = new byte[base.length + length];
            System.arraycopy(base, 0, tmp, 0, base.length);
            System.arraycopy(add, addIndex, tmp, base.length, length);
            return tmp;
        }

        //刪除檔案的Byte數
        private byte[] delByte(byte[] base, int del) {
            byte[] tmp = new byte[base.length - del];
            System.arraycopy(base, 0, tmp, 0, tmp.length);
            return tmp;
        }

    }
}

