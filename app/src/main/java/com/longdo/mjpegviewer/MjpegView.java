package com.longdo.mjpegviewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.itri.woundcamrtc.helper.StringUtils;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class MjpegView extends View {
    public static final int MODE_ORIGINAL = 0;
    public static final int MODE_FIT_WIDTH = 1;
    public static final int MODE_FIT_HEIGHT = 2;
    public static final int MODE_BEST_FIT = 3;
    public static final int MODE_STRETCH = 4;

    private static final int WAIT_AFTER_READ_IMAGE_ERROR_MSEC = 10000;

    //硬碟設定(設定為4096 bytes)
    private static final int CHUNK_SIZE = 1024;
    //得到類的簡寫名稱
    private final String tag = getClass().getSimpleName();

    private Context context;
    private String url;
    private Bitmap lastBitmap;
    private MjpegDownloader downloader;
    private final Object lockBitmap = new Object();
    private PaintFlagsDrawFilter mSetfil = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG |Paint.FILTER_BITMAP_FLAG);
    private String outputFilename="";
    //繪圖物件
    private Paint paint;
    private Rect dst;

    private int mode = MODE_ORIGINAL;
    private int drawX, drawY, vWidth = -1, vHeight = -1;
    private int lastImgWidth, lastImgHeight;

    private boolean adjustWidth, adjustHeight;

    //讀寫圖片錯誤等待時間(毫秒)
    private int msecWaitAfterReadImageError = WAIT_AFTER_READ_IMAGE_ERROR_MSEC;
    private int http_connect_timeout = 3000;
    private int http_read_timeout = 5000;

    //bitmap是否有recycle
    private boolean isRecycleBitmap;
    //是否由使用者自行設定recycle
    private boolean isUserForceConfigRecycle;

    public MjpegView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        //建立繪圖物件
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dst = new Rect(0, 0, 0, 0);
    }

    //設定URL
    public void setUrl(String url) {
        this.url = url;
    }

    //開始接收熱感影像串流
    public void startStream() {
        //如果確認啟動
        if (downloader != null && downloader.isRunning()) {
            Log.w(tag, "Already started, stop by calling stopStream() first.");
            return;
        }

        downloader = new MjpegDownloader();
        //同步開始啟動
        downloader.start();
    }

    public void stopStream() {
        downloader.cancel();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void saveBitmap(String filename){
        outputFilename = filename;
    }

    @SuppressLint("WrongThread")
    public void setBitmap(Bitmap bm) {
        //Log.v(tag,"New frame");

        //lockBitmap本身處於被Lock狀態，但此時其他的Thread是可以去更改lockBitmap裡面的值，Lock只是同步化的狀態，不表示不能更改資料
        synchronized (lockBitmap) {
            if (lastBitmap != null && ((isUserForceConfigRecycle && isRecycleBitmap) || (!isUserForceConfigRecycle && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB))) {
                //Log.v(tag, "Manually recycle bitmap");

                if (StringUtils.isNotBlank(outputFilename)) {
                    try (FileOutputStream out = new FileOutputStream(outputFilename)) {
                        lastBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    outputFilename="";
                }

                lastBitmap.recycle();
            }

            lastBitmap = bm;
        }

        //判斷context是否為Activity的實例
        if (context instanceof Activity) {
            //引數所帶入的 action，將會保證在 UI thread 被執行
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //呼叫invalidate 重新安排layout位置
                        invalidate();
                        requestLayout();
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(tag, "Can not request Canvas's redraw. Context is not an instance of Activity");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean shouldRecalculateSize;
        synchronized (lockBitmap) {
            shouldRecalculateSize = lastBitmap != null && (lastImgWidth != lastBitmap.getWidth() || lastImgHeight != lastBitmap.getHeight());
            if (shouldRecalculateSize) {
                lastImgWidth = lastBitmap.getWidth();
                lastImgHeight = lastBitmap.getHeight();
            }
        }

        if (shouldRecalculateSize) {
            Log.d(tag, "Recalculate view/image size");

            vWidth = MeasureSpec.getSize(widthMeasureSpec);
            vHeight = MeasureSpec.getSize(heightMeasureSpec);

            if (mode == MODE_ORIGINAL) {
                drawX = (vWidth - lastImgWidth) / 2;
                drawY = (vHeight - lastImgHeight) / 2;

                if (adjustWidth) {
                    vWidth = lastImgWidth;
                    drawX = 0;
                }

                if (adjustHeight) {
                    vHeight = lastImgHeight;
                    drawY = 0;
                }
            } else if (mode == MODE_FIT_WIDTH) {
                int newHeight = (int) (((float) lastImgHeight / (float) lastImgWidth) * vWidth);

                drawX = 0;

                if (adjustHeight) {
                    vHeight = newHeight;
                    drawY = 0;
                } else {
                    drawY = (vHeight - newHeight) / 2;
                }

                //no need to check adjustWidth because in this mode image's width is always equals view's width.

                dst.set(drawX, drawY, vWidth, drawY + newHeight);
            } else if (mode == MODE_FIT_HEIGHT) {
                int newWidth = (int) (((float) lastImgWidth / (float) lastImgHeight) * vHeight);

                drawY = 0;

                if (adjustWidth) {
                    vWidth = newWidth;
                    drawX = 0;
                } else {
                    drawX = (vWidth - newWidth) / 2;
                }

                //no need to check adjustHeight because in this mode image's height is always equals view's height.

                dst.set(drawX, drawY, drawX + newWidth, vHeight);
            } else if (mode == MODE_BEST_FIT) {
                if ((float) lastImgWidth / (float) vWidth > (float) lastImgHeight / (float) vHeight) {
                    //duplicated code
                    //fit width
                    int newHeight = (int) (((float) lastImgHeight / (float) lastImgWidth) * vWidth);

                    drawX = 0;

                    if (adjustHeight) {
                        vHeight = newHeight;
                        drawY = 0;
                    } else {
                        drawY = (vHeight - newHeight) / 2;
                    }

                    //no need to check adjustWidth because in this mode image's width is always equals view's width.

                    dst.set(drawX, drawY, vWidth, drawY + newHeight);
                } else {
                    //duplicated code
                    //fit height
                    int newWidth = (int) (((float) lastImgWidth / (float) lastImgHeight) * vHeight);

                    drawY = 0;

                    if (adjustWidth) {
                        vWidth = newWidth;
                        drawX = 0;
                    } else {
                        drawX = (vWidth - newWidth) / 2;
                    }

                    //no need to check adjustHeight because in this mode image's height is always equals view's height.

                    dst.set(drawX, drawY, drawX + newWidth, vHeight);
                }
            } else if (mode == MODE_STRETCH) {
                dst.set(0, 0, vWidth, vHeight);
                //no need to check neither adjustHeight nor adjustHeight because in this mode image's size is always equals view's size.
            }

            setMeasuredDimension(vWidth, vHeight);
        } else {
            if (vWidth == -1 || vHeight == -1) {
                vWidth = MeasureSpec.getSize(widthMeasureSpec);
                vHeight = MeasureSpec.getSize(heightMeasureSpec);
            }

            setMeasuredDimension(vWidth, vHeight);
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        synchronized (lockBitmap) {
            if (c != null && lastBitmap != null && !lastBitmap.isRecycled()) {
                c.setDrawFilter(mSetfil);
                if (isInEditMode()) {
                    // TODO: preview while edit xml
                } else if (mode != MODE_ORIGINAL) {
                    c.drawBitmap(lastBitmap, null, dst, paint);
                } else {
                    c.drawBitmap(lastBitmap, drawX, drawY, paint);
                }
            } else {
                Log.d(tag, "Skip drawing, canvas is null or bitmap is not ready yet");
            }
        }
    }

    public boolean isAdjustWidth() {
        return adjustWidth;
    }

    public void setAdjustWidth(boolean adjustWidth) {
        this.adjustWidth = adjustWidth;
    }

    public boolean isAdjustHeight() {
        return adjustHeight;
    }

    public void setAdjustHeight(boolean adjustHeight) {
        this.adjustHeight = adjustHeight;
    }

    public int getMsecWaitAfterReadImageError() {
        return msecWaitAfterReadImageError;
    }

    public void setMsecWaitAfterReadImageError(int msecWaitAfterReadImageError) {
        this.msecWaitAfterReadImageError = msecWaitAfterReadImageError;
    }

    public int getConnectTimeout() {
        return http_connect_timeout;
    }

    public void setConnectTimeout(int _http_connect_timeout) {
        this.http_connect_timeout = _http_connect_timeout;
    }

    public int getReadTimeout() {
        return http_connect_timeout;
    }

    public void setReadTimeout(int _http_read_timeout) {
        this.http_read_timeout = _http_read_timeout;
    }

    public boolean isRecycleBitmap() {
        return isRecycleBitmap;
    }

    public void setRecycleBitmap(boolean recycleBitmap) {
        isUserForceConfigRecycle = true;
        isRecycleBitmap = recycleBitmap;
    }

    //新增一個名為MjpegDownloader 的 Thread Class
    class MjpegDownloader extends Thread {

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

                try {
                    //取得serverURL
                    serverUrl = new URL(url);
                    //建立url物件能與HttpURLConnection對話
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setConnectTimeout(http_connect_timeout);
                    connection.setReadTimeout(http_read_timeout);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
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
                        Matcher matcher;

                        bis = new BufferedInputStream(connection.getInputStream());
                        byte[] image = new byte[0], read = new byte[CHUNK_SIZE], tmpCheckBoundry;
                        int readByte, boundaryIndex;
                        String checkHeaderStr, boundary;

                        //always keep reading images from server (持續從server讀取圖片)
                        while (run) {
                            try {
                                readByte = bis.read(read);
                                //Log.v(tag, "讀取的byte數=" + String.valueOf(readByte));

                                //no more data
                                if (readByte == -1) {
                                    break;
                                }

                                tmpCheckBoundry = addByte(image, read, 0, readByte);
                                //將ASCII Byte[] 轉成 String
                                checkHeaderStr = new String(tmpCheckBoundry, "ASCII");

                                //使用 matcher 方法指定要比對的字串
                                matcher = pattern.matcher(checkHeaderStr);
                                if (matcher.find()) {
                                    //boundary is found
                                    //使用 group 方法傳回符合的字串
                                    boundary = matcher.group(0);

                                    //
                                    boundaryIndex = checkHeaderStr.indexOf(boundary);
                                    boundaryIndex -= image.length;

                                    if (boundaryIndex > 0) {
                                        image = addByte(image, read, 0, boundaryIndex);
                                    } else {
                                        image = delByte(image, -boundaryIndex);
                                    }

                                    //Log.v(tag,"image的byte[]=" + String.valueOf(image));
                                    Bitmap outputImg = BitmapFactory.decodeByteArray(image, 0, image.length);
                                    if (outputImg != null) {
                                        if (run) {
                                            newFrame(outputImg);
                                        }
                                    } else {
                                        Log.e(tag, "Read image error");
                                    }

                                    int headerIndex = boundaryIndex + boundary.length();

                                    image = addByte(new byte[0], read, headerIndex, readByte - headerIndex);
                                } else {
                                    image = addByte(image, read, 0, readByte);
                                }
                            } catch (Exception e) {
                                if (e != null && e.getMessage() != null) {
                                    Log.e(tag, e.getMessage());
                                }
                                break;
                            }
                        }
                    } else {

                    }
                } catch (Exception e) {
                    if (e != null && e.getMessage() != null) {
                        Log.e(tag, e.getMessage());
                    }
                }

                try {
                    if (bis != null && connection != null) {
                        bis.close();
                        connection.disconnect();
                        Log.i(tag, "disconnected with " + url);
                    }
                } catch (Exception e) {
                    if (e != null && e.getMessage() != null) {
                        Log.e(tag, e.getMessage());
                    }
                }

                if (msecWaitAfterReadImageError > 0) {
                    try {
                        //可使目前的執行緒暫停執行一段時間(毫秒)
                        Thread.sleep(msecWaitAfterReadImageError);
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

        //新增Bitmap 圖檔
        private void newFrame(Bitmap bitmap) {
            setBitmap(bitmap);
        }
    }
}
