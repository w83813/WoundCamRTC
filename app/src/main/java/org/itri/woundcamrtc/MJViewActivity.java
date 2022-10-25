package org.itri.woundcamrtc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.longdo.mjpegviewer.MjpegView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MJViewActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    private String iip = "";
    private String iport = "9000";
    private String pwd = "123";

    private MjpegView mjpegview1;
    private TextView item_dist_value;
    private LinearLayout mjview_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mjview);
        Intent intent = this.getIntent();
        iip = intent.getStringExtra("iip");
        Log.v(TAG, iip);

        mjpegview1 = findViewById(R.id.mjpegview1);
        mjpegview1.setMode(MjpegView.MODE_FIT_HEIGHT);
        mjpegview1.setRotation(-90.0f);
        mjpegview1.setConnectTimeout(1000);
        mjpegview1.setReadTimeout(3000);
        mjpegview1.setMsecWaitAfterReadImageError(1000);
        mjpegview1.setRecycleBitmap(true);
        mjpegview1.setUrl("http://" + iip + ":" + iport + "/ipcam01/video.mjpeg?action=" + pwd);
        //mjpegview1.setUrl("http://10.0.1.45:9000/ipcam01/video.mjpeg?action=123");
        mjpegview1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                String fileName = file.getPath() + File.separator + s.format(new Date());

                mjpegview1.saveBitmap(fileName+ "_thermal.png");
            }
        });

        item_dist_value = findViewById(R.id.item_dist_value);
        item_dist_value.setText(iip);

        mjview_title = findViewById(R.id.mjview_title);
        detectOrientation(this.getResources().getConfiguration().orientation);





    }

    @Override
    protected void onResume() {
        mjpegview1.startStream();
        super.onResume();
        Log.v(TAG, "到達MJViewActivity畫面");
    }

    //暫停接收串流
    @Override
    protected void onPause() {
        mjpegview1.stopStream();
        super.onPause();
    }

    //停止接收串流
    @Override
    protected void onStop() {
        mjpegview1.stopStream();
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        detectOrientation(newConfig.orientation);
    }

    private void detectOrientation(int orientation) {

        // 檢查目前手機畫面為橫向或直向
        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            mjview_title.setVisibility(View.INVISIBLE);
        } else if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            mjview_title.setVisibility(View.VISIBLE);
        }

    }
}