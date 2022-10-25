package org.itri.woundcamrtc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.MessageQueue;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.litesuits.http.LiteHttp;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.listener.HttpListener;
import com.litesuits.http.request.StringRequest;
import com.litesuits.http.request.param.HttpMethods;

import net.sqlcipher.database.SQLiteDatabase;

import org.itri.woundcamrtc.analytics.GrabcutTouchView;
import org.itri.woundcamrtc.analytics.TissueClassification;
import org.itri.woundcamrtc.helper.DBTableHelper;
import org.itri.woundcamrtc.helper.FileHelper;
import org.itri.woundcamrtc.helper.Model3DHelper;
import org.itri.woundcamrtc.helper.SecretDbHelper;
import org.itri.woundcamrtc.helper.StringUtils;
//import org.itri.woundcamrtc.job.JobQueueAnalyticsJob;
import org.itri.woundcamrtc.helper.XSslHttpURLConnection;
import org.itri.woundcamrtc.helper.XSslLiteHttp;
import org.itri.woundcamrtc.helper.XSslOkHttpClient;
import org.itri.woundcamrtc.job.JobQueueAIImageJob;
import org.itri.woundcamrtc.job.JobQueueUploadFileJob;
import org.itri.woundcamrtc.view.FlowRadioGroup;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.apptik.widget.MultiSlider;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.itri.woundcamrtc.AppResultReceiver.SAVE_DIR;
import static org.itri.woundcamrtc.AppResultReceiver.ZONE;
import static org.itri.woundcamrtc.AppResultReceiver.dataEncrypt;
import static org.itri.woundcamrtc.AppResultReceiver.mMainActivity;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.itri.woundcamrtc.AppResultReceiver.isAccountCorrecct;
public class GrabcutActivity extends FragmentActivity
        implements NumberPickerDialogFragment.NumberPickerDialogHandlerV2 {

    private final String TAG = getClass().getSimpleName();
    private LiteHttp mLiteHttp = null;
    public String greyHeatImgUrl = "";
    public String params = "";
    private String fever = "";
    private String smell = "";
    private String level = "";
    private String character = "";
    private String overtime = "";
    private String occur = "";
    private String feverNum = "";
    private String smellNum = "";
    private String levelNum = "";
    private String characterNum = "";
    private String overtimeNum = "";
    private String occurNum = "";

    public static String fileName = "";
    public static String outFilePath = "";
    public static String txtFilePath = "";
    public static String evlId = "";
    public static String ownerId = "";

    public static int assignId;
    public static int TISSUE_DOWNSAMPLE_RATE = 20;
    public static int GRABCAT_DOWNSAMPLE_RATE = 5;
    public int feverId = -1;
    public int smellId = -1;
    public int levelId = -1;
    public int characterId = -1;
    public int overtimeId = -1;
    public int occurId = -1;
    public boolean redograbcut = true;
    public String account =AppResultReceiver.account;

    public String password =AppResultReceiver.password;
    public static GrabcutActivity activity;
    private Context mContext;
    private boolean isShowLogoutHint = true; //顯示登出提醒
    public JobManager jobManagerRelax = null;
    public static JobManager jobManagerLocal = null;
    public ProgressDialog mProgressDialog = null;

    private static OkHttpClient client = null;
    private static String selectedDate="";
    public TextView progressBarText;
//    public ProgressBar progressBarImage;

    ImageButton btnBack, btnDone;
    Button btnAdd, btnRemove, btnTogo, btnClear, btnReset, btnSetref, btnSymptom, btnHistory;
    public TextView propInfo, markerInfo, requiredNote;
    public TextView sizeInfo1v, sizeInfo2v, sizeInfo3v, sizeInfo4v;
    FlowRadioGroup fever_rg, smell_rg, level_rg, character_rg, overtime_rg, occur_rg;
    public static EditText datePicker;
    private LinearLayout grabcut_top_layout;
    private LinearLayout grabcut_btm_layout;
    public MultiSlider multiSlider;
    public GrabcutTouchView drawView;
    public static TextView tv_drawstatus;
    public static TextView tv_draw;
    public static ImageView iv_drawcolor;
    public static boolean upload_txt = false;

    public static Mat img = null;
    public Mat greyImg = null;
    Mat mask;
    //    double distanceOffset = 3.3;
    //    double magnification = 3;

    //public
    Rect inROI = null;
    List<MatOfPoint> contours = new ArrayList<>();
    int lastColor = 0xFFFF0000;
    public static final int SUCCESS = 200;
    private JSONObject txtData;
    public File mainDir;
    public DBTableHelper database;
    public SQLiteDatabase Sercretdb;
    public SecretDbHelper sqllitesecret;
    private MessageQueue.IdleHandler resumeIdleHandler = new MessageQueue.IdleHandler() {
        @Override
        public boolean queueIdle() {
            Log.d(TAG, "queueIdle");
            onResumeInit();
            return false; //run once
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);  //關閉APP標題橫槓
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON  //防止螢幕自動關閉
//                        | LayoutParams.FLAG_DISMISS_KEYGUARD    //解鎖螢幕
//                        | LayoutParams.FLAG_SHOW_WHEN_LOCKED    //螢幕鎖定時也可以顯示
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
            setContentView(R.layout.grabcut);
            activity = this;
            mContext= this;
            if (dataEncrypt == false) {
                mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                database = DBTableHelper.getInstance(getApplicationContext(), mainDir.getAbsolutePath() + File.separator + AppResultReceiver.PROJECT_NAME + ".db");
            } else {
                SQLiteDatabase.loadLibs(this);
                sqllitesecret = new SecretDbHelper(this);
                Sercretdb = SecretDbHelper.getInstance(this).getWritableDatabase("MIIS");
            }
            Intent intent = this.getIntent();
            params = intent.getStringExtra("params");
            //fileName = "/storage/emulated/0/Download/WoundCamRtc/TestMaskImg/" + AppResultReceiver.filetag + "_src.jpg";
            fileName = intent.getStringExtra("fileName");
            if (fileName.startsWith("http"))
                fileName = fileName.substring(fileName.substring(10).indexOf("/") + 10);

            txtFilePath = intent.getStringExtra("txtFile");
            Log.v(TAG, "txtFilePath" + txtFilePath);
            if (txtFilePath.startsWith("http"))
                txtFilePath = txtFilePath.substring(txtFilePath.substring(10).indexOf("/") + 10);

            evlId = intent.getStringExtra("evlId");
            ownerId = intent.getStringExtra("ownerId");

            try {
                assignId = Integer.parseInt(intent.getStringExtra("assignId"));
            } catch (Exception ex) {
                assignId = 0;
            }

            onInit();
//            Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
//                @Override
//                public boolean queueIdle() {
//                    Log.d(TAG, "queueIdle");
//                    onInit();
//                    return false; //run once
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        drawView.setPaintColor(0xFFFFFF00);
        GrabcutTouchView.str_touchstatus = "drawborder";

    }

    protected void onInit() {
        Log.d(TAG, "onInit");
        try {
            long startWait = System.currentTimeMillis();
            while (AppResultReceiver.isTakingPicture == true && (System.currentTimeMillis() - startWait < 3000)) {
                Thread.sleep(10);
            }

            if (jobManagerLocal == null)
                configureJobQueueManager();


            AppResultReceiver.grabcutWithColorAI = false;
            AppResultReceiver.grabcutWithDnnAI = false;
            AppResultReceiver.grabcutWithInteraction = false;
            AppResultReceiver.initOrientation(activity);
            generateViews();

            redograbcut = true;
            //AppResultReceiver.mMainActivity.jobManagerRelax.addJobInBackground(new JobQueueAnalyticsJob(AppResultReceiver.mMainActivity.jobManagerRelax, "", activity, fileName, drawView.getLastResultContours()));

        } catch (Exception ex) {
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        //Looper.myQueue().addIdleHandler(resumeIdleHandler);
        onResumeInit();
    }

    protected void onResumeInit() {
        Log.d(TAG, "onResumeInit");
        try {
            isShowLogoutHint = true;

//            AlertDialog dialog = setProgressDialog(activity);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
            if (img == null) {

                startGrabCut_0120(); //RM8 focus box only
            }
//            }
//        },100);
//

//            dialog.dismiss();
//        img = imread(fileName);
//        if (img.cols() == 0 || img.rows() == 0)
//            return;
//        Size sz = new Size(img.width() / GRABCAT_DOWNSAMPLE_RATE, img.height() / GRABCAT_DOWNSAMPLE_RATE);
//        Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_LINEAR);
//        drawView.initImage(activity, img.clone() );
//        new asynDo().execute();
        } catch (Exception ex) {
        } finally {
            Looper.myQueue().removeIdleHandler(resumeIdleHandler);
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        if (img != null) {
            img.release();
            img = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isShowLogoutHint) {
            AppResultReceiver.logoutPreExecute(GrabcutActivity.this);
            isShowLogoutHint = false;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            jobManagerLocal.stop();
            //jobManagerLocal.destroy();
            //jobManagerLocal.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jobManagerLocal = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {   //離開主畫面之提醒通知
        isShowLogoutHint = false;
        btnBack.performClick();
    }

    public JobManager configureJobQueueManager() {
        //3. JobManager的配置器，利用Builder模式
        Configuration configuration = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBR";

                    @Override
                    public boolean isDebugEnabled() {
                        return false;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        //Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }

                    @Override
                    public void v(String text, Object... args) {
                        //Log.v(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(0)//always keep at least one consumer alive
                .maxConsumerCount(1)//up to 1 consumers at a time
                .loadFactor(1)//1 jobs per consumer
                .consumerKeepAlive(10)//wait 0.1 minute
                .build();
        jobManagerLocal = new JobManager(configuration);
        return jobManagerLocal;
    }

    public double secondMax(double a, double b, double max) {
        if (a != a)
            return a;   // a is NaN
        if (max == a)
            return b;
        if (max == b)
            return a;
        if ((a == 0.0d) &&
                (b == 0.0d) &&
                (Double.doubleToRawLongBits(a) == Double.doubleToRawLongBits(-0.0d))) {
            // Raw conversion ok since NaN can't map to -0.0.
            return b;
        }
        return (a >= b) ? a : b;
    }

    public double secondMin(double a, double b, double min) {
        if (a != a)
            return a;   // a is NaN
        if (min == a)
            return b;
        if (min == b)
            return a;
        if ((a == 0.0d) &&
                (b == 0.0d) &&
                (Double.doubleToRawLongBits(b) == Double.doubleToRawLongBits(-0.0d))) {
            // Raw conversion ok since NaN can't map to -0.0.
            return b;
        }
        return (a <= b) ? a : b;
    }

    private void generateViews() {

        jobManagerRelax = configureJobQueueManagerRelax();
        //jobManagerRelax.addJobInBackground(new JobQueueAIImageJob(jobManagerRelax,  activity, fileName));
        grabcut_top_layout = (LinearLayout) findViewById(R.id.grabcut_top_layout);
        grabcut_btm_layout = (LinearLayout) findViewById(R.id.grabcut_btm_layout);
        tv_drawstatus = findViewById(R.id.tv_drawstatus);

        tv_draw = findViewById(R.id.tv_draw);
        iv_drawcolor = findViewById(R.id.iv_drawcolor);

        GrabcutTouchView.drawbroder_done = false;
        GrabcutTouchView.remove_status = false;

        drawView = (GrabcutTouchView) findViewById(R.id.touchDraw);
        drawView.setPaintColor(lastColor);
        drawView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        drawView.setAdjustViewBounds(true);

        btnBack = (ImageButton) findViewById(R.id.back);
        btnDone = (ImageButton) findViewById(R.id.done);

        btnAdd = (Button) findViewById(R.id.add);
        btnRemove = (Button) findViewById(R.id.remove);
        btnClear = (Button) findViewById(R.id.clear);
        btnReset = (Button) findViewById(R.id.reset);
        btnTogo = (Button) findViewById(R.id.togo);
        btnSetref = (Button) findViewById(R.id.setref);
        btnSymptom = (Button) findViewById(R.id.symptom);
        btnHistory = (Button) findViewById(R.id.history);

        //btnBack.setOnTouchListener(touch);
        //btnDone.setOnTouchListener(touch);

        btnAdd.setOnTouchListener(touch);
        btnRemove.setOnTouchListener(touch);
        btnClear.setOnTouchListener(touch);

        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isShowLogoutHint = false;
                AppResultReceiver.vibrating(activity);
                finish();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!GrabcutTouchView.drawbroder_done){
                    DialogROI dialogROI = new DialogROI();
                    dialogROI.show(getSupportFragmentManager(), "example dialog");
                    return;
                }


                iv_drawcolor.setBackgroundColor(Color.rgb(255,0,0));
                tv_draw.setText(R.string.keep);

                AppResultReceiver.vibrating(activity);
//				resetButtons();
//				btnAdd.setSelected(true);
                drawView.setPaintColor(0xFFFF0000);
                GrabcutTouchView.remove_status = false;
//				isTextModeOn = false;
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!GrabcutTouchView.drawbroder_done){
                    DialogROI dialogROI = new DialogROI();
                    dialogROI.show(getSupportFragmentManager(), "example dialog");
                    return;
                }

                iv_drawcolor.setBackgroundColor(Color.rgb(0,0,255));
                tv_draw.setText(R.string.exclude);

                AppResultReceiver.vibrating(activity);
//				resetButtons();
//				btnAdd.setSelected(true);
                drawView.setPaintColor(0xFF0000FF);
                GrabcutTouchView.remove_status = true;
//				isTextModeOn = false;
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppResultReceiver.vibrating(activity);
                drawView.clearAndRedoGrabCut();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GrabcutActivity.this);
                builder.setMessage(R.string.remake_confirmation);
                builder.setPositiveButton(R.string.confirm_title, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        drawView.clearAndRedoGrabCut();
                        drawView.clearAndRedoGrabCut_border();
                        startGrabCut_0120();
                        drawView.setPaintColor(0xFFFFFF00);
                        drawView.reset_drawstatus();
                        GrabcutTouchView.str_touchstatus = "drawborder";
                        tv_draw.setText("ROI");
                        iv_drawcolor.setBackgroundColor(Color.rgb(255,255,0));
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AppResultReceiver.vibrating(activity);

                if (ZONE.equals("TC")) {
                    if (feverId != -1 && smellId != -1 && levelId != -1 && characterId != -1 && occurId != -1 && overtimeId != -1) {
                        showStatDialog(getString(R.string.anaylsis_title), R.layout.analysis_info);
                    } else {
                        showAlertDialog(getString(R.string.remind_title), getString(R.string.not_finished));
                    }
                } else {
                    showStatDialog(getString(R.string.anaylsis_title), R.layout.analysis_info);
                }
            }
        });

        if (btnTogo != null) {
            btnTogo.setOnTouchListener(touch);
            btnTogo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (!GrabcutTouchView.drawbroder_done){
                        DialogROI dialogROI = new DialogROI();
                        dialogROI.show(getSupportFragmentManager(), "example dialog");
                        return;
                    }

                    AppResultReceiver.vibrating(activity);

                    ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                    if (mNetworkInfo == null) {
                        showToast(getString(R.string.open_internet_or_wifi));
                        return;
                    } else {
                        checkAccountCorrecct();
                        if (isAccountCorrecct == true) {
                            drawView.clear();
                            AsynAiDetection asyncTask = new AsynAiDetection();
                            asyncTask.execute();
                        }else{
                            showDialog(getString(R.string.remind_title), getString(R.string.no_account_authority_to_use), 3);
                        }


//                        jobManagerRelax = configureJobQueueManagerRelax();
//                        jobManagerRelax.addJobInBackground(new JobQueueAIImageJob(jobManagerRelax, activity, fileName));
                    }
                }
            });
        }


        if (btnSetref != null) {
            btnSetref.setOnTouchListener(touch);
            btnSetref.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showProgressBarInfo(getString(R.string.progressing_measuring), true);
                    AppResultReceiver.vibrating(activity);
                    AsynMeasurement asyncTask = new AsynMeasurement();
                    asyncTask.execute();

//                jobManagerLocal.addJobInBackground(new JobQueueAnalyticsJob(jobManagerLocal, "", activity, fileName, drawView.getLastResultContours()));
//
//
//
//
////                AppResultReceiver.estimateWidth = 0.0; //預估寬度
////                AppResultReceiver.estimateHeight = 0.0; //預估長度
////                AppResultReceiver.estimateArea = 0.0; //預估面積
////                AppResultReceiver.refMarkerWidth = 1.5;
////                AppResultReceiver.epithelium_prop = 0.0; //上皮組織比例
////                AppResultReceiver.granular_prop = 0.0; //肉芽組織比例
////                AppResultReceiver.slough_prop = 0.0; //腐皮組織比例
////                AppResultReceiver.eschar_prop = 0.0; //焦痂組織比例
////
////
////                if (drawView.getLastResultContours().size() == 0)
////                    return;
////
////
////                Mat resultImage = drawView.getLastResultImage();
////                if (resultImage != null) {
//////                    Highgui.imwrite(fileName + "mask3.png", resultImage);
////
//////                    Imgproc.cvtColor(resultImage, resultImage, Imgproc.COLOR_RGB2BGR);
////
////                    //Highgui.imwrite(outFilePath + "analytics.jpg", resultImage);
////                    Intent intent = new Intent();
//////                    intent.putExtra(ChooseActivity.BITMAP, editedImage);
//////                    // AddReportItemActivity.mPhoto =
//////                    // drawView.getDrawingCache();
////
////
////                    setResult(SUCCESS, intent);
////                    org.opencv.core.Rect regionRect = BoundingRectangle(drawView.getLastResultContours().get(drawView.getLastResultContours().size() - 1));
////
////                    MatOfPoint2f newMtx = new MatOfPoint2f(drawView.getLastResultContours().get(drawView.getLastResultContours().size() - 1).toArray());
////                    org.opencv.core.RotatedRect rotatedRect = Imgproc.minAreaRect(newMtx);
////
////                    //drawRotatedRectangle(resultImage, rotatedRect);
/////////////////./.....>???????????????
//////                    rect = cv2.minAreaRect(cnt)
//////                    box = cv2.cv.BoxPoints(rect)
////
////                    Mat fileImage = Imgcodecs.imread(fileName);
////                    Mat saving_mask = new Mat(fileImage.rows() / GRABCAT_DOWNSAMPLE_RATE, fileImage.cols() / GRABCAT_DOWNSAMPLE_RATE, CvType.CV_8U, new Scalar(0, 0, 0));
////                    Imgproc.drawContours(saving_mask, drawView.getLastResultContours(), drawView.getLastResultContours().size() - 1, new Scalar(255, 255, 255), -1);
////
////
//////                     Mat fileImage = Imgcodecs.imread(fileName);
////                    Size sz = new Size(fileImage.width()/TISSUE_DOWNSAMPLE_RATE, fileImage.height()/TISSUE_DOWNSAMPLE_RATE);
////                    Mat imageMask = new Mat();
////                    Imgproc.resize(saving_mask, imageMask, sz);
////
////                    Mat imageFile = new Mat();
////                    Imgproc.resize(fileImage, imageFile, sz);
////                    fileImage.release();
////
////////                    //Highgui.imwrite(fileName + "_grabcut_mask.png", saving_mask);
//////
//////                    String resultSize = drawView.getLastImageSize();
//////                    String resultCountour = drawView.getLastContour();
//////
//////                    Rect resizedROI =  new Rect(region.x/(TISSUE_DOWNSAMPLE_RATE/GRABCAT_DOWNSAMPLE_RATE),region.y/(TISSUE_DOWNSAMPLE_RATE/GRABCAT_DOWNSAMPLE_RATE),region.width/(TISSUE_DOWNSAMPLE_RATE/GRABCAT_DOWNSAMPLE_RATE),region.height/(TISSUE_DOWNSAMPLE_RATE/GRABCAT_DOWNSAMPLE_RATE));
////
////                    AnalysisPic_4class(imageFile, imageMask, getApplicationContext());
////                    //success(region.boundingRect() , "grabCutImageSize=" + resultSize + "\r\ngrabCutContour=" + resultCountour);
////                    //success(regionRect, rotatedRect, "grabCutImageSize=" + resultSize + "\r\ngrabCutContour=" + resultCountour);
////
////
////                }
////
////                generateUI();
//
                }
            });
        }

        btnSymptom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppResultReceiver.vibrating(activity);
                showDialog(getString(R.string.symptom_title), R.layout.question_symptom, 1);
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppResultReceiver.vibrating(activity);
                showDialog(getString(R.string.history_title), R.layout.question_history, 2);
            }
        });

        // sizeInfo = (TextView) findViewById(R.id.size_info);
        sizeInfo1v = (TextView) findViewById(R.id.size_info1v);
        sizeInfo2v = (TextView) findViewById(R.id.size_info2v);
        sizeInfo3v = (TextView) findViewById(R.id.size_info3v);
        sizeInfo4v = (TextView) findViewById(R.id.size_info4v);

        sizeInfo1v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerBuilder npb = new NumberPickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment)
                        .setPlusMinusVisibility(View.INVISIBLE)
                        .setCurrentNumber((int) Double.parseDouble(sizeInfo1v.getText().toString()))
                        .setLabelText(getApplicationContext().getString(R.string.height))
                        .setReference(1);
                npb.show();
            }
        });

        sizeInfo2v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerBuilder npb = new NumberPickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment)
                        .setPlusMinusVisibility(View.INVISIBLE)
                        .setCurrentNumber((int) Double.parseDouble(sizeInfo2v.getText().toString()))
                        .setLabelText(getApplicationContext().getString(R.string.width))
                        .setReference(2);
                npb.show();
            }
        });

        sizeInfo3v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerBuilder npb = new NumberPickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment)
                        .setPlusMinusVisibility(View.INVISIBLE)
                        .setCurrentNumber((int) Double.parseDouble(sizeInfo3v.getText().toString()))
                        .setLabelText(getApplicationContext().getString(R.string.area))
                        .setReference(3);
                npb.show();
            }
        });

        sizeInfo4v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerBuilder npb = new NumberPickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment)
                        .setPlusMinusVisibility(View.INVISIBLE)
                        .setCurrentNumber((int) Double.parseDouble(sizeInfo4v.getText().toString()))
                        .setLabelText(getApplicationContext().getString(R.string.distance))
                        .setReference(4);
                npb.show();
            }
        });

        progressBarText = (TextView) findViewById(R.id.progressBarText);
//        progressBarImage = (ProgressBar) findViewById(R.id.progressBarImage);

        propInfo = (TextView) findViewById(R.id.prop_info);
        //markerInfo = (TextView) findViewById(R.id.marker_info);

        multiSlider = (MultiSlider) findViewById(R.id.multiSlider);
        AppResultReceiver.redrawMultiSlider = true;
        multiSlider.setTrackDrawable(new ColorDrawable(0xFF552A2A));
        multiSlider.setBackground(new ColorDrawable(0x0));
        multiSlider.getThumb(0).setRange(new ColorDrawable(0xFFFF9090));
        multiSlider.getThumb(1).setRange(new ColorDrawable(0xFFFF0000));
        multiSlider.getThumb(2).setRange(new ColorDrawable(0xFF00FF00));

        multiSlider.getThumb(2).setValue(100);
        multiSlider.getThumb(1).setValue(100);
        multiSlider.getThumb(0).setValue(AppResultReceiver.epithelium_prop);

        multiSlider.getThumb(1).setValue(AppResultReceiver.epithelium_prop);
        multiSlider.getThumb(2).setValue(AppResultReceiver.epithelium_prop + AppResultReceiver.granular_prop + AppResultReceiver.slough_prop);

        multiSlider.getThumb(1).setValue(AppResultReceiver.epithelium_prop + AppResultReceiver.granular_prop);
        AppResultReceiver.redrawMultiSlider = false;

//        multiSlider.refreshDrawableState();
        multiSlider.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider,
                                       MultiSlider.Thumb thumb,
                                       int thumbIndex,
                                       int value) {

                if (AppResultReceiver.redrawMultiSlider == false) {
                    AppResultReceiver.epithelium_prop = multiSlider.getThumb(0).getValue();
                    AppResultReceiver.granular_prop = multiSlider.getThumb(1).getValue() - AppResultReceiver.epithelium_prop;
                    AppResultReceiver.slough_prop = multiSlider.getThumb(2).getValue() - AppResultReceiver.epithelium_prop - AppResultReceiver.granular_prop;
                    AppResultReceiver.eschar_prop = 100 - multiSlider.getThumb(2).getValue();
                }
                try {
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(mMainActivity);
                    String orientation = prefs.getString("screen_orientation", "Null");
                    if ("Landscape".equals(orientation)) {
                        //Landscape 螢幕保持橫向
                        activity.propInfo.setText(getApplicationContext().getString(R.string.epithelium) + ": " + String.valueOf(AppResultReceiver.epithelium_prop)
                                + "\n" + getApplicationContext().getString(R.string.granular) + ": " + String.valueOf(AppResultReceiver.granular_prop)
                                + "\n" + getApplicationContext().getString(R.string.slough) + ": " + String.valueOf(AppResultReceiver.slough_prop)
                                + "\n" + getApplicationContext().getString(R.string.eschar) + ": " + String.valueOf(AppResultReceiver.eschar_prop));
                    } else {
                        //Portrait 螢幕保持直向
                        activity.propInfo.setText(getApplicationContext().getString(R.string.epithelium) + ": " + String.valueOf(AppResultReceiver.epithelium_prop)
                                + "," + getApplicationContext().getString(R.string.granular) + ": " + String.valueOf(AppResultReceiver.granular_prop)
                                + "," + getApplicationContext().getString(R.string.slough) + ": " + String.valueOf(AppResultReceiver.slough_prop)
                                + "," + getApplicationContext().getString(R.string.eschar) + ": " + String.valueOf(AppResultReceiver.eschar_prop));
                    }
                } catch (Exception e) {

                }
            }
        });
    }


    private class asynDo extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                //Thread.sleep(1000);
                startGrabCut_0120(); //RM8 focus box only
                //startGrabCut_0119(); //RM8 range too over
                //startGrabCut_0108(); //S60 better
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    @Override
    public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
        //mResultTextView.setText(getString(R.string.number_picker_result_value, number, decimal, isNegative, fullNumber));
        if (reference == 1) {
            sizeInfo1v = (TextView) findViewById(R.id.size_info1v);
            sizeInfo1v.setText(fullNumber.toString());
            AppResultReceiver.estimateHeight = fullNumber.doubleValue();
        } else if (reference == 2) {
            sizeInfo2v = (TextView) findViewById(R.id.size_info2v);
            sizeInfo2v.setText(fullNumber.toString());
            AppResultReceiver.estimateWidth = fullNumber.doubleValue();
        } else if (reference == 3) {
            sizeInfo3v = (TextView) findViewById(R.id.size_info3v);
            sizeInfo3v.setText(fullNumber.toString());
            AppResultReceiver.estimateArea = fullNumber.doubleValue();
        } else if (reference == 4) {
            sizeInfo4v = (TextView) findViewById(R.id.size_info4v);
            sizeInfo4v.setText(String.valueOf(fullNumber.doubleValue()));
            AppResultReceiver.snapshutDistance = fullNumber.doubleValue();
        }
    }

    public void startGrabCut_0120() {
        try {
            // load image with data encrypt
            if (!AppResultReceiver.dataEncrypt) {
                img = Imgcodecs.imread(fileName);
            } else {
                img = FileHelper.imreadSecret(fileName);
            }
            if (img == null || img.cols() == 0 || img.rows() == 0)
                return;

            Mat preprocessed = new Mat();
            Mat resized = new Mat();
            Size sz = new Size(img.width() / GRABCAT_DOWNSAMPLE_RATE, img.height() / GRABCAT_DOWNSAMPLE_RATE);
            //Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_CUBIC);
            Imgproc.resize(img.clone(), resized, sz, 0, 0, Imgproc.INTER_LINEAR);

            // kernoli todo
            Imgproc.medianBlur(resized.clone(), preprocessed, 11);

            inROI = new Rect(0, 0, resized.cols(), resized.rows());

            // kernoli todo

            int offsetX = 0;
            int offsetY = 0;
            switch (AppResultReceiver.touchRotate) {
                case Surface.ROTATION_0: //0
                    offsetX = 0;
                    offsetY = 0;
                    break;
                case Surface.ROTATION_90: //1
                    offsetY = 0;
                    break;
                case Surface.ROTATION_180: //2
                    break;
                case Surface.ROTATION_270: //3
                    break;
            }

            int wound_tissue_count = 0;

            if (wound_tissue_count == 0) {
                AppResultReceiver.grabcutWithColorAI = false;
                mask = new Mat(resized.rows(), resized.cols(), CvType.CV_8U, new Scalar(Imgproc.GC_PR_BGD));
            }


            drawView.initGrabCut(activity, img, resized, preprocessed, inROI, mask, fileName, outFilePath, GRABCAT_DOWNSAMPLE_RATE, params);
//            drawView.initImage(this,resized);


            drawView.redoGrabCut(mask, activity, fileName);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private final class AsynRedoGrabCut extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            activity.showProgressBarInfo(activity.getString(R.string.progressing_ranging), true);
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(Void... params) {
            try {
                drawView.redoGrabCut(mask, activity, "");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            String progressBarInfo = "";
            if (AppResultReceiver.grabcutWithDnnAI) {
                if (AppResultReceiver.grabcutWithInteraction)
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.DnnAI) + "& " + activity.getString(R.string.interactied);
                else
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.DnnAI);

            } else if (AppResultReceiver.grabcutWithColorAI) {
                if (AppResultReceiver.grabcutWithInteraction)
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.ColorAI) + "& " + activity.getString(R.string.interactied);
                else
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.ColorAI);
            } else {
                if (AppResultReceiver.grabcutWithInteraction)
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.interactied);
                else
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.default_boundary);
            }
            activity.showProgressBarInfo(progressBarInfo, false);
        }
    }

    private final class AsynMeasurement extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            activity.showProgressBarInfo(activity.getString(R.string.progressing_measuring), true);
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(Void... params) {

            try {

                String[] emp = fileName.split("_");
                String evlId = emp[0].substring(emp[0].indexOf(emp[1]));
                int itemId = Integer.parseInt(emp[2]);
//                    String evlId = "2020-08-10 11-40-35-336"; //AppResultReceiver.mMainActivity.evlId;
//                    int itemId = 4; //AppResultReceiver.mMainActivity.count;
//
//                    String evlId = AppResultReceiver.mMainActivity.evlId;
//                    int itemId = AppResultReceiver.mMainActivity.count;

                String dateString = evlId.substring(0, 10);

                File filea = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.SAVE_DIR);
                String filePath = filea.getAbsolutePath();
                if (!filePath.endsWith(File.separator))
                    filePath = filePath + File.separator;
                filea = null;

                File rawFile = new File(filePath + evlId + "_" + dateString + "_" + (199 + itemId) + "_3ds.raw");
                if (rawFile.exists() && rawFile.length() > 0) {
                    Mat rMask = new Mat(drawView.initdImage.size(), CvType.CV_8UC1, new Scalar(0));
                    Imgproc.drawContours(rMask, drawView.lastContours, drawView.lastContours.size() - 1, new Scalar(1), Core.FILLED);

                    MatOfPoint2f newMtx = new MatOfPoint2f(drawView.getLastResultContours().get(drawView.getLastResultContours().size() - 1).toArray());
                    org.opencv.core.RotatedRect rotatedRect = Imgproc.minAreaRect(newMtx);
                    Point pts[] = new Point[4];
                    rotatedRect.points(pts);
                    //point 0: left bottom, point 1: left top, point 2: right top, point 3: right bottom
                    int LX = (int) ((pts[0].x + pts[1].x) / 2);
                    int LY = (int) ((pts[0].y + pts[1].y) / 2);
                    int RX = (int) ((pts[2].x + pts[3].x) / 2);
                    int RY = (int) ((pts[2].y + pts[3].y) / 2);

                    int TX = (int) ((pts[1].x + pts[2].x) / 2);
                    int TY = (int) ((pts[1].y + pts[2].y) / 2);
                    int BX = (int) ((pts[0].x + pts[3].x) / 2);
                    int BY = (int) ((pts[0].y + pts[3].y) / 2);


                    String result = Model3DHelper.GenWoundInfo(filePath, evlId, itemId, rMask, LX, LY, RX, RY, TX, TY, BX, BY, (int) rotatedRect.center.x, (int) rotatedRect.center.y);
                    rMask.release();

                    if (AppResultReceiver.DEBUG_LEVEL > 0)
                        showProgressBarInfo(result, false);
                    else {
                        String progressBarInfo = "";
                        if (AppResultReceiver.grabcutWithDnnAI) {
                            if (AppResultReceiver.grabcutWithInteraction)
                                progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.DnnAI) + "& " + activity.getString(R.string.interactied);
                            else
                                progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.DnnAI);

                        } else if (AppResultReceiver.grabcutWithColorAI) {
                            if (AppResultReceiver.grabcutWithInteraction)
                                progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.ColorAI) + "& " + activity.getString(R.string.interactied);
                            else
                                progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.ColorAI);
                        } else {
                            if (AppResultReceiver.grabcutWithInteraction)
                                progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.interactied);
                            else
                                progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.default_boundary);
                        }
                        showProgressBarInfo(progressBarInfo, false);
                    }

                    if (StringUtils.isNotBlank(result) && result.startsWith("success=true")) {
                        try {
                            String[] lines = result.split(System.getProperty("line.separator"));
                            for (String str : lines) {
                                if (str.startsWith("wound area=")) {
                                    // 1/100 cm
                                    try {
                                        double val = toDecimalFormat(toDecimalFormat(str.replace("wound area=", "")));
                                        if (val != 0) {
                                            AppResultReceiver.estimateArea = val;
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (str.startsWith("max depth=")) {
                                    // 1/10 cm
                                    try {
                                        double val = toDecimalFormat(str.replace("max depth=", ""));
                                        if (val != 0) {
                                            AppResultReceiver.estimateDepth = val;
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (str.startsWith("long=")) {
                                    try {
                                        double val = toDecimalFormat(str.replace("long=", ""));
                                        if (val != 0) {
                                            AppResultReceiver.estimateHeight = val;
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (str.startsWith("short=")) {
                                    try {
                                        double val = toDecimalFormat(str.replace("short=", ""));
                                        if (val != 0) {
                                            AppResultReceiver.estimateWidth = val;
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (str.startsWith("hi temp=")) {

                                } else if (str.startsWith("lo temp=")) {

                                }
                            }
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        activity.sizeInfo1v.setText(String.valueOf(AppResultReceiver.estimateHeight));
                                        activity.sizeInfo2v.setText(String.valueOf(AppResultReceiver.estimateWidth));
                                        activity.sizeInfo3v.setText(String.valueOf(AppResultReceiver.estimateArea));
                                        activity.sizeInfo4v.setText(String.valueOf(AppResultReceiver.estimateDepth));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {

                        }
                    }

                }


            } catch (Exception ex) {

            } finally {
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            String progressBarInfo = "";
            if (AppResultReceiver.grabcutWithDnnAI) {
                if (AppResultReceiver.grabcutWithInteraction)
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.DnnAI) + "& " + activity.getString(R.string.interactied);
                else
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.DnnAI);

            } else if (AppResultReceiver.grabcutWithColorAI) {
                if (AppResultReceiver.grabcutWithInteraction)
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.ColorAI) + "& " + activity.getString(R.string.interactied);
                else
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.ColorAI);
            } else {
                if (AppResultReceiver.grabcutWithInteraction)
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.interactied);
                else
                    progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.default_boundary);
            }
            activity.showProgressBarInfo(progressBarInfo, false);
        }
    }

    private final class AsynAiDetection extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            try{
                activity.showProgressBarInfo(activity.getString(R.string.progressing_detecting), true);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(Void... params) {
            try {
                String targetFileName = fileName;
                String postURL = AppResultReceiver.DEFAULT_POST_AI_COLOR_IMAGE_PATH;
                String imgURL = AppResultReceiver.AI_SERVER_IP_PORT;

                Bitmap newBmp = null;
                activity.greyHeatImgUrl = "";

                if (client == null) {
//                OkHttpClient.Builder builder = new OkHttpClient.Builder();
//                builder.connectTimeout(10, TimeUnit.SECONDS);
//                builder.readTimeout(120, TimeUnit.SECONDS);
//                //builder.addInterceptor(logging);
//                client = builder.build();
                    client = (new XSslOkHttpClient(10, 160)).getOkHttpClient();
                }
                //activity.showToast("Wait for call AI Server");

//                if (AppResultReceiver.dataEncrypt) {
//                    try {
//                        FileHelper.decodeFileSecret(targetFileName);
//                        Thread.sleep(100);
//                    } catch (Exception ex) {
//                    }
//                }

                String roi_image = targetFileName.replace("_jpg.jpg", "_roi.jpg");
                File roi_file = new File(roi_image);
                if (roi_file.exists()) {
                    targetFileName = roi_image;
                }


                Mat orgl = FileHelper.imreadSecret(targetFileName);
//                Size originalSz = new Size(orgl.width() , orgl.height() );
//                Size sz = new Size(orgl.width() * 0.4, orgl.height() * 0.4);
                Size sz = new Size(orgl.width() / GRABCAT_DOWNSAMPLE_RATE, orgl.height() / GRABCAT_DOWNSAMPLE_RATE);
                Imgproc.resize(orgl.clone(), orgl, sz, 0, 0, Imgproc.INTER_LANCZOS4);
                InputStream isJPG = FileHelper.mat2InputStream(orgl);

                RequestBody dataBody = XSslOkHttpClient.createRequestBody(MediaType.parse("image/jpeg"),isJPG);

                RequestBody multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        //.addFormDataPart("image", targetFileName, RequestBody.create(MediaType.parse("image/jpeg"), targetFile))
                        .addFormDataPart("image", targetFileName, dataBody)
                        .build();

                Request request = new Request.Builder()
                        .url(postURL)
                        .post(multipartBody)
                        .build();
//                Log.v(TAG, "執行拋出AI需求");
                Response response = client.newCall(request).execute();
//                Log.v(TAG, "執行得到heatmap");
                Log.v(TAG, "回覆訊息為：" + response.toString());
                isJPG.close();
//                if (AppResultReceiver.dataEncrypt) {
//                    try {
//                        FileHelper.overwriteFileSecret(targetFileName);
//                    } catch (Exception ex) {
//                    }
//                }

                JSONObject result = new JSONObject(response.body().string());
                String heatmapURL = imgURL + "/" + result.getString("heatmap_url");
                activity.greyHeatImgUrl = heatmapURL;
//                URL url = new URL(heatmapURL.replace(" ", "%20"));
                InputStream is = (InputStream) (new XSslHttpURLConnection()).getImageStream(heatmapURL.replace(" ", "%20"), 5, 10);

//                Log.v(TAG, "執行decodeStream前");
                Bitmap bmp = BitmapFactory.decodeStream(is);
//                Log.v(TAG, "執行decodeStream後");

                if (activity.greyImg == null) {
                    activity.greyImg = new Mat();
                }
                Utils.bitmapToMat(bmp, activity.greyImg);
                bmp.recycle();

                Imgproc.cvtColor(activity.greyImg, activity.greyImg, Imgproc.COLOR_RGB2GRAY);


//                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
//                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
//                String fileName = file.getPath() + File.separator + s.format(new Date());
//                Log.v("sss_4",String.valueOf(fileName));
//                //File mediaFile = new File(fileName + "_ai.png");
//                if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_AI_RESPONSE) {
//                    Imgcodecs.imwrite(fileName + "_ai_response.png", activity.greyImg);
//                }



                Imgproc.threshold(activity.greyImg, activity.greyImg, 1, Imgproc.GC_PR_FGD, Imgproc.THRESH_BINARY);

                Mat tmp = new Mat(activity.greyImg.rows(), activity.greyImg.cols(), CvType.CV_8UC1, Scalar.all(Imgproc.GC_PR_BGD));
                activity.greyImg.copyTo(tmp, activity.greyImg);
                activity.greyImg.release();
                activity.greyImg = tmp;

                Imgproc.rectangle(
                        activity.greyImg,                    //Matrix obj of the image
                        new Point(activity.greyImg.width() * 0.5-2, activity.greyImg.height() * 0.5-2),        //p1
                        new Point(activity.greyImg.width() * 0.5+2, activity.greyImg.height() * 0.5+2),       //p2
                        new Scalar(Imgproc.GC_FGD),     //Scalar object for color
                        Core.FILLED                         //Thickness of the line
                );
                Imgproc.rectangle(
                        activity.greyImg,                    //Matrix obj of the image
                        new Point(2, 2),        //p1
                        new Point(activity.greyImg.width() - 2, activity.greyImg.height() - 2),       //p2
                        new Scalar(Imgproc.GC_BGD),  //Scalar object for color
                        8 //Core.FILLED                          //Thickness of the line
                );

//                Log.v(TAG, "a.執行convertToGrabCutClasses後");
//                Log.v(TAG, "a.執行GrabCut前");
                activity.setRedograbcut(true);
                AppResultReceiver.grabcutWithDnnAI = true;
                activity.drawView.redoGrabCut(activity.greyImg, activity, targetFileName);

//                Log.v(TAG, "完成時間:" + System.nanoTime());
//                Log.v(TAG, "a.執行GrabCut完畢");
            /*if (bmp.getWidth() >= bmp.getHeight()) {
                newBmp = getResizedBitmap(bmp, 640, 480);
            } else {
                newBmp = getResizedBitmap(bmp, 480, 640);
            }*/
                Log.v(TAG, "取得bmp尺寸為: 寬-" + bmp.getWidth() + "/長-" + bmp.getHeight());
                //Log.v(TAG, "取得newBmp尺寸為: 寬-" + newBmp.getWidth() + "/長-" + newBmp.getHeight());
                Log.v(TAG, "取得結果為: " + heatmapURL);
//                activity.showToast("Response from AI Server");
            } catch (Exception e) {
                Log.v(TAG, "取得AI圖片錯誤: " + e.toString());
                activity.showToast("Error on call AI Server: " + e.getMessage());
                e.printStackTrace();
            } finally {
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            try{
                String progressBarInfo = "";
                if (AppResultReceiver.grabcutWithDnnAI) {
                    if (AppResultReceiver.grabcutWithInteraction)
                        progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.DnnAI) + "& " + activity.getString(R.string.interactied);
                    else
                        progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.DnnAI);

                } else if (AppResultReceiver.grabcutWithColorAI) {
                    if (AppResultReceiver.grabcutWithInteraction)
                        progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.ColorAI) + "& " + activity.getString(R.string.interactied);
                    else
                        progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.ColorAI);
                } else {
                    if (AppResultReceiver.grabcutWithInteraction)
                        progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.interactied);
                    else
                        progressBarInfo = activity.getString(R.string.with) + activity.getString(R.string.default_boundary);
                }
                activity.showProgressBarInfo(progressBarInfo, false);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public void setProgressDialog(String msg, boolean show) {
        if (!show) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        } else {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(activity, getString(R.string.please_wait), "Please wait, Loading Page...", true);
            }
//            mProgressDialog.setTitle(getString(R.string.please_wait));
//            mProgressDialog.setMessage(msg);
//            mProgressDialog.setCancelable(false);
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mProgressDialog.show();
//                }
//            });
        }
    }

//    public AlertDialog setProgressDialog(Context context) {
//        int llPadding = 30;
//        LinearLayout ll = new LinearLayout(context);
//        ll.setOrientation(LinearLayout.HORIZONTAL);
//        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
//        ll.setGravity(Gravity.CENTER);
//        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//        llParam.gravity = Gravity.CENTER;
//        ll.setLayoutParams(llParam);
//
//        ProgressBar progressBar = new ProgressBar(context);
//        progressBar.setIndeterminate(true);
//        progressBar.setPadding(0, 0, llPadding, 0);
//        progressBar.setLayoutParams(llParam);
//
//        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        llParam.gravity = Gravity.CENTER;
//        TextView tvText = new TextView(context);
//        tvText.setText("Loading ...");
//        tvText.setTextColor(Color.parseColor("#000000"));
//        tvText.setTextSize(20);
//        tvText.setLayoutParams(llParam);
//
//        ll.addView(progressBar);
//        ll.addView(tvText);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setCancelable(true);
//        builder.setView(ll);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//        Window window = dialog.getWindow();
//        if (window != null) {
//            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//            layoutParams.copyFrom(dialog.getWindow().getAttributes());
//            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
//            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
//            dialog.getWindow().setAttributes(layoutParams);
//        }
//        return dialog;
//    }
//
//    public void startGrabCut_0119() {
//        //if (img==null) {
//        img = imread(fileName);
//
//        if (img.cols() == 0 || img.rows() == 0)
//            return;
//
////        Size sz2 = new Size(img.width() / 50, img.height() / 50);
////        //Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_CUBIC);
//        Mat preprocessed = new Mat();
////        Imgproc.resize(img, preprocessed, sz2, 0, 0, Imgproc.INTER_LINEAR);
//
//        Size sz = new Size(img.width() / GRABCAT_DOWNSAMPLE_RATE, img.height() / GRABCAT_DOWNSAMPLE_RATE);
//        //Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_CUBIC);
//        Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_LINEAR);
//
//        //drawView.initImage(activity,img,preprocessed);
//        //colorCorrectionFast(img,AppResultReceiver.rOffset,AppResultReceiver.gOffset,AppResultReceiver.bOffset);
//
//
//        // kernoli todo
//        Imgproc.blur(img.clone(), preprocessed, new Size(11, 11), new Point(-1, -1));
//
//        preprocessed = reduceColorKmean(img, 12);
//        //
//        //preprocessed = chrominanceBalance(img);
//        //preprocessed = img.clone();
//
//
//        //Imgproc.resize(preprocessed.clone(), preprocessed, sz, 0, 0, Imgproc.INTER_LINEAR);
//
//
//        inROI = new Rect(0, 0, img.cols(), img.rows());
//
//        // kernoli todo
//        mask = new Mat(img.rows(), img.cols(), CvType.CV_8U, new Scalar(Imgproc.GC_PR_BGD));
//        //mask = new Mat(img.rows(), img.cols(), CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
//        Imgproc.rectangle(
//                mask,                    //Matrix obj of the image
//                new Point(2, 2),        //p1
//                new Point(img.width() - 2, img.height() - 2),       //p2
//                new Scalar(Imgproc.GC_BGD), //new Scalar(Imgproc.GC_PR_BGD),     //Scalar object for color
//                8 //Core.FILLED                          //Thickness of the line
//        );
//
////        Imgproc.rectangle(
////                mask,                    //Matrix obj of the image
////                new Point( mask.width() * AppResultReceiver.touchFocusX-50,  mask.height() * AppResultReceiver.touchFocusY-50),        //p1
////                new Point( mask.width() * AppResultReceiver.touchFocusX+50,  mask.height() * AppResultReceiver.touchFocusY+50),       //p2
////                new Scalar(Imgproc.GC_PR_FGD),     //Scalar object for color , Imgproc.GC_FGD
////                Core.FILLED                         //Thickness of the line
////        );
//
//        int offsetX = 0;
//        int offsetY = 0;
//        switch (AppResultReceiver.touchRotate) {
//            case Surface.ROTATION_0: //0
//                offsetX = 0;
//                offsetY = 0;
//                break;
//            case Surface.ROTATION_90: //1
//                offsetY = 0;
//                break;
//            case Surface.ROTATION_180: //2
//                break;
//            case Surface.ROTATION_270: //3
//                break;
//        }
//
//        Imgproc.rectangle(
//                mask,                    //Matrix obj of the image
//                new Point(mask.width() * AppResultReceiver.lastPicFocusXScale + offsetX - AppResultReceiver.focusTargetSize * 2, mask.height() * AppResultReceiver.lastPicFocusYScale + offsetY - AppResultReceiver.focusTargetSize),        //p1
//                new Point(mask.width() * AppResultReceiver.lastPicFocusXScale + offsetX + AppResultReceiver.focusTargetSize * 2, mask.height() * AppResultReceiver.lastPicFocusYScale + offsetY + AppResultReceiver.focusTargetSize),       //p2
//                new Scalar(Imgproc.GC_FGD),     //Scalar object for color , Imgproc.GC_FGD
//                Core.FILLED                         //Thickness of the line
//        );
//
////        Mat dest = chrominanceBalance(img);
//        Mat dest = img;
//
//
//        drawView.initGrabCut(activity, dest, preprocessed, inROI, mask, fileName, outFilePath, GRABCAT_DOWNSAMPLE_RATE, params);
//        drawView.redoGrabCut(mask, activity,"");
//
//    }
//
//
//    public void startGrabCut_0108() {
//        //if (img==null) {
//        img = imread(fileName);
//
//        if (img.cols() == 0 || img.rows() == 0)
//            return;
//
//        Size sz2 = new Size(img.width() / 50, img.height() / 50);
//        //Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_CUBIC);
//        Mat preprocessed = new Mat();
//        Imgproc.resize(img, preprocessed, sz2, 0, 0, Imgproc.INTER_LANCZOS4);
//
//        Size sz = new Size(img.width() / GRABCAT_DOWNSAMPLE_RATE, img.height() / GRABCAT_DOWNSAMPLE_RATE);
//        //Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_CUBIC);
//        Imgproc.resize(img.clone(), img, sz, 0, 0, Imgproc.INTER_LANCZOS4);
//
//        //Imgproc.blur(img.clone(), preprocessed, new Size(11, 11), new Point(-1, -1));
//        preprocessed = chrominanceBalance(preprocessed);
//
//        //preprocessed = reduceColorKmean(preprocessed, 12);
//        //Imgproc.blur(preprocessed.clone(), preprocessed, new Size(3, 3));
//
//        Imgproc.resize(preprocessed.clone(), preprocessed, sz, 0, 0, Imgproc.INTER_LANCZOS4);
//
//
//        inROI = new Rect(0, 0, img.cols(), img.rows());
//
//        mask = new Mat(img.rows(), img.cols(), CvType.CV_8U, new Scalar(Imgproc.GC_PR_BGD));
//        Imgproc.rectangle(
//                mask,                    //Matrix obj of the image
//                new Point(2, 2),        //p1
//                new Point(img.width() - 2, img.height() - 2),       //p2
//                new Scalar(Imgproc.GC_BGD), //new Scalar(Imgproc.GC_PR_BGD),     //Scalar object for color
//                8 //Core.FILLED                          //Thickness of the line
//        );
//
////        Imgproc.rectangle(
////                mask,                    //Matrix obj of the image
////                new Point( mask.width() * AppResultReceiver.touchFocusX-50,  mask.height() * AppResultReceiver.touchFocusY-50),        //p1
////                new Point( mask.width() * AppResultReceiver.touchFocusX+50,  mask.height() * AppResultReceiver.touchFocusY+50),       //p2
////                new Scalar(Imgproc.GC_PR_FGD),     //Scalar object for color , Imgproc.GC_FGD
////                Core.FILLED                         //Thickness of the line
////        );
//
//        int offsetX = 0;
//        int offsetY = 0;
//        switch (AppResultReceiver.touchRotate) {
//            case Surface.ROTATION_0: //0
//                offsetX = 0;
//                offsetY = 0;
//                break;
//            case Surface.ROTATION_90: //1
//                offsetY = 0;
//                break;
//            case Surface.ROTATION_180: //2
//                break;
//            case Surface.ROTATION_270: //3
//                break;
//        }
//
//        Imgproc.rectangle(
//                mask,                    //Matrix obj of the image
//                new Point(mask.width() * AppResultReceiver.lastPicFocusXScale + offsetX - AppResultReceiver.focusTargetSize * 2, mask.height() * AppResultReceiver.lastPicFocusYScale + offsetY - AppResultReceiver.focusTargetSize),        //p1
//                new Point(mask.width() * AppResultReceiver.lastPicFocusXScale + offsetX + AppResultReceiver.focusTargetSize * 2, mask.height() * AppResultReceiver.lastPicFocusYScale + offsetY + AppResultReceiver.focusTargetSize),       //p2
//                new Scalar(Imgproc.GC_FGD),     //Scalar object for color , Imgproc.GC_FGD
//                Core.FILLED                         //Thickness of the line
//        );
//
////        Mat dest = chrominanceBalance(img);
//        Mat dest = img;
//
//
//        drawView.initGrabCut(activity, dest, preprocessed, inROI, mask, fileName, outFilePath, GRABCAT_DOWNSAMPLE_RATE, params);
//        drawView.redoGrabCut(mask, activity,"");
//        //}
//    }


    public void showToast(String context) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Toast toast = Toast.makeText(GrabcutActivity.this, context, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 250);
                    toast.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

//    public void showLongToast(String msg) {
//        GrabcutActivity.this.runOnUiThread(new Runnable() {
//            public void run() {
//                try {
//
//                    progressBarText = (TextView) findViewById(R.id.progressBarText);
//                    progressBarImage = (ProgressBar) findViewById(R.id.progressBarImage);
//
//                    Toast toast = Toast.makeText(GrabcutActivity.this, msg, Toast.LENGTH_LONG);
//                    toast.setGravity(Gravity.CENTER, 0, 250);
//                    toast.show();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//    }

    public void showProgressBarInfo(String msg, final boolean showImage) {
//        boolean showImage2 = true;
//        activity.runOnUiThread(new Runnable() {
//        new Handler().postDelayed(new Runnable() {
//            public void run() {
        try {
            LinearLayout progressBarLayout = (LinearLayout) activity.findViewById(R.id.progressBarLayout);

            pl.droidsonroids.gif.GifImageView progressBarImage = (pl.droidsonroids.gif.GifImageView) activity.findViewById(R.id.progressBarImage);
            if (showImage) {
                progressBarImage.setVisibility(View.VISIBLE);
                progressBarImage.invalidate();
            } else {
                progressBarImage.setVisibility(View.INVISIBLE);

            }
            progressBarImage.invalidate();
            progressBarText.setText(msg);

            progressBarLayout.invalidate();
            progressBarLayout.requestLayout();
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
//            }
//        });
    }

//    public void AnalysisPic_4class(Mat inputImage, Mat maskImage, Context context) {
//        try {
//            //TissueClassification.classifier_knn_4class(inputImage, maskImage,context);
//            //TissueClassification.classifier_RTrees_4class2018(inputImage, maskImage,context);
//            //TissueClassification.classifier_svm_4class2018_build(context);
//            TissueClassification.classifier_svm_4class_fromxml(inputImage, maskImage, context);
//            //TissueClassification.classifier_svm_4class2019_fromxml(inputImage, maskImage,context);
//
////            Epithelial 嫩皮
////        Sloughy 腐
////        Exudate 滲
////        Granulation 芽
////        Necrosis 焦
////        Maceration 浸
////        Swelling 腫
//            int i = 0;
////            outMsg = outMsg + " " + TissueClassification.getTissueAnalysisResult().get(i).get(0) + TissueClassification.getTissueAnalysisResult().get(i).get(1);
//            AppResultReceiver.epithelium_prop = Integer.valueOf(TissueClassification.getTissueAnalysisResult().get(i).get(1).replace("%", ""));
//
//            i = 1;
////            outMsg = outMsg + " " + TissueClassification.getTissueAnalysisResult().get(i).get(0) + TissueClassification.getTissueAnalysisResult().get(i).get(1);
//            AppResultReceiver.granular_prop = Integer.valueOf(TissueClassification.getTissueAnalysisResult().get(i).get(1).replace("%", ""));
//
//            i = 2;
////            outMsg = outMsg + " " + TissueClassification.getTissueAnalysisResult().get(i).get(0) + TissueClassification.getTissueAnalysisResult().get(i).get(1);
//            AppResultReceiver.slough_prop = Integer.valueOf(TissueClassification.getTissueAnalysisResult().get(i).get(1).replace("%", ""));
//
//            i = 3;
////            outMsg = outMsg + " " + TissueClassification.getTissueAnalysisResult().get(i).get(0) + TissueClassification.getTissueAnalysisResult().get(i).get(1);
//            AppResultReceiver.eschar_prop = Integer.valueOf(TissueClassification.getTissueAnalysisResult().get(i).get(1).replace("%", ""));
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//
//
//    public void writeToFile(String outFilename, String msg, boolean append) {
//        BufferedWriter writer = null;
//        try {
//            //create a temporary file
////            String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//            File textFile = new File(outFilename);
//
//            // This will output the full path where the file will be written to...
////            System.out.println(logFile.getCanonicalPath());
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
//
//
//    public void success(org.opencv.core.Rect region, org.opencv.core.RotatedRect rotatedRect, String grabCutResultMsg) {
////
////        // 用 position 取得第幾個 Grid 位置
//////                Toast.makeText(context, "" + position, Toast.LENGTH_SHORT).show();
////        writeToFile(outFilePath + "woundSize.txt", "", false);
////        writeToFile(outFilePath + "woundTissue.txt", "", false);
////        writeToFile(outFilePath + "woundAss.txt", "", false);
////
//////        org.opencv.core.Rect region = BoundingRectangle(contours.get(contours.size() - 1));
////
//////        Mat roi_image = Highgui.imread(outFilePath + "analytics.jpg");
//////        Core.rectangle(
//////                roi_image,                    //Matrix obj of the image
//////                new Point(region.x, region.y),        //p1
//////                new Point(region.x + region.width, region.y + region.height),       //p2
//////                new Scalar(128, 255, 255),     //Scalar object for color
//////                5                          //Thickness of the line
//////        );
////        double magnificationx = 20.0;
////        double magnificationy = 20.0;
////        if (distance == 0)
////            distance=30;
////
////        ScaleTransfer.setDistance(distance + distanceOffset);
//////                    ScaleTransfer.setDistance(distance);
////        Log.d(TAG,"rotatedRect.width:"+rotatedRect.size.width+ ", rotatedRect.height:"+rotatedRect.size.height);
////        double[] scale3 = ScaleTransfer.MeasurementRealDistanceMULTI(rotatedRect, magnificationx, magnificationy);
////        System.out.println("Width3 = " + scale3[0] + "cm(" + rotatedRect.size.width * magnification + " pixels) , Height = " + scale3[1] + "cm(" + rotatedRect.size.height * magnification + " pixels)");
////        DecimalFormat decimalFormat = new DecimalFormat("#.0");
////        String outMsg = "長:" + decimalFormat.format(scale3[0]) + ",寬:" + decimalFormat.format(scale3[1]) + ",面積:" + decimalFormat.format(scale3[0] * scale3[1])+ ",距離:" +decimalFormat.format(distance + distanceOffset);
////        writeToFile(outFilePath + "woundSize.txt", outMsg, false);
////        outMsg = grabCutResultMsg + "\r\nwidth=" + scale3[0] + "\r\nheight=" + scale3[1] + "\r\nsize=" + decimalFormat.format(scale3[0] * scale3[1]) + "\r\ndist="+distance+"\r\n";
////        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity);
////
////        String data_filename= shared.getString(KEY_STOREAGE_PATH_PREFERENCE, "") + "wound_img/" + activity.evlId + "_0_06_";
////
////        writeToFile(data_filename + "data.txt", outMsg, true);
////
////
////        String evlId = activity.evlId;
////        String ownerId = activity.ownerId;
////        DBTableHelper database = DBTableHelper.getInstance(getApplicationContext(), NickResultReceiver.DB_PATH_FILE_NAME);
////        Random ran = new Random();
////        long nowtime = TimeHelper.stringToDate(TimeHelper.getDateFormat(TimeHelper.getSystemTime()), new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN)).getTime();
////        ContentValues initialValues = new ContentValues();
////        initialValues.put("caseId", evlId);
////        initialValues.put("ownerId", ownerId);
////        initialValues.put("evalDate", nowtime);
////        initialValues.put("valueType", TYPE_SHUT_DISTANCE);
////        initialValues.put("value", distance);
////        database.addOrUpdateRaw("table_measure", initialValues, "evalDate = ? and caseId = ? and valueType = ?", new String[]{Long.toString(nowtime), evlId, TYPE_SHUT_DISTANCE});
////
////
////        initialValues.put("caseId", evlId);
////        initialValues.put("ownerId", ownerId);
////        initialValues.put("evalDate", nowtime);
////        initialValues.put("valueType", TYPE_WOUND_LENGTH);
////        initialValues.put("value", scale3[1]);//ran.nextInt(15) + 1);
////        database.addOrUpdateRaw("table_measure", initialValues, "evalDate = ? and caseId = ? and valueType = ?", new String[]{Long.toString(nowtime), evlId, TYPE_WOUND_LENGTH});
////
////        initialValues = new ContentValues();
////        initialValues.put("caseId", evlId);
////        initialValues.put("ownerId", ownerId);
////        initialValues.put("evalDate", nowtime);
////        initialValues.put("valueType", TYPE_WOUND_WIDTH);
////        initialValues.put("value", scale3[0]);//ran.nextInt(12) + 1);
////        database.addOrUpdateRaw("table_measure", initialValues, "evalDate = ? and caseId = ? and valueType = ?", new String[]{Long.toString(nowtime), evlId, TYPE_WOUND_WIDTH});
////
////        initialValues = new ContentValues();
////        initialValues.put("caseId", evlId);
////        initialValues.put("ownerId", ownerId);
////        initialValues.put("evalDate", nowtime);
////        initialValues.put("valueType", TYPE_WOUND_AREA);
////        initialValues.put("value", scale3[0] * scale3[1]);//ran.nextInt(12) + 10);
////        database.addOrUpdateRaw("table_measure", initialValues, "evalDate = ? and caseId = ? and valueType = ?", new String[]{Long.toString(nowtime), evlId, TYPE_WOUND_AREA});
////
//
//        Rect resizedROI =  new Rect(region.x/(TISSUE_DOWNSAMPLE_RATE/GRABCAT_DOWNSAMPLE_RATE),region.y/(TISSUE_DOWNSAMPLE_RATE/GRABCAT_DOWNSAMPLE_RATE),region.width/(TISSUE_DOWNSAMPLE_RATE/GRABCAT_DOWNSAMPLE_RATE),region.height/(TISSUE_DOWNSAMPLE_RATE/GRABCAT_DOWNSAMPLE_RATE));
//
//        AnalysisPic_4class(fileImage, resizedROI, ROOT_FOLDER_PATH);
//    }
//
//    public void colorChanged(int color) {
//        // TODO Auto-generated method stub
//        lastColor = color;
//        drawView.setPaintColor(lastColor);
//    }

    public void showAlertDialog(final String title, final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GrabcutActivity.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(title);
                    builder.setMessage(message);
                    builder.setIcon(R.mipmap.color_light_48);
                    //按到旁邊的空白處AlertDialog也不會消失
                    builder.setCancelable(false);

                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void showStatDialog(final String title, final int resourceId) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.analysis_info, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(GrabcutActivity.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(title);
                    builder.setView(layout);
                    //builder.setIcon(R.mipmap.color_light_48);
                    //按到旁邊的空白處AlertDialog也不會消失
                    builder.setCancelable(false);
                    //傷口尺寸
                    TextView height = (TextView) layout.findViewById(R.id.height_data);
                    TextView width = (TextView) layout.findViewById(R.id.width_data);
                    TextView depth = (TextView) layout.findViewById(R.id.depth_data);
                    TextView area = (TextView) layout.findViewById(R.id.area_data);
                    //傷口組織比例
                    TextView epithelium = (TextView) layout.findViewById(R.id.epithelium_data);
                    TextView granular = (TextView) layout.findViewById(R.id.granular_data);
                    TextView slough = (TextView) layout.findViewById(R.id.slough_data);
                    TextView eschar = (TextView) layout.findViewById(R.id.eschar_data);
                    //傷口症狀
                    TextView feverAns = (TextView) layout.findViewById(R.id.fever_data);
                    TextView smellAns = (TextView) layout.findViewById(R.id.smell_data);
                    TextView levelAns = (TextView) layout.findViewById(R.id.level_data);
                    TextView characterAns = (TextView) layout.findViewById(R.id.character_data);
                    //傷口史
                    TextView occurAns = (TextView) layout.findViewById(R.id.occur_data);
                    TextView overtimeAns = (TextView) layout.findViewById(R.id.overtime_data);
                    TextView occurDate_data = (TextView) layout.findViewById(R.id.occurDate_data);
                    //
                    BigDecimal decimalFormat = new BigDecimal(AppResultReceiver.estimateDepth);

                    height.setText(String.valueOf(AppResultReceiver.estimateHeight) + getString(R.string.cm));
                    width.setText(String.valueOf(AppResultReceiver.estimateWidth) + getString(R.string.cm));
                    depth.setText(String.valueOf(decimalFormat.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()) + getString(R.string.cm));
                    area.setText(String.valueOf(AppResultReceiver.estimateArea) + getString(R.string.cm2));

                    epithelium.setText(String.valueOf(AppResultReceiver.epithelium_prop) + getString(R.string.prop));
                    granular.setText(String.valueOf(AppResultReceiver.granular_prop) + getString(R.string.prop));
                    slough.setText(String.valueOf(AppResultReceiver.slough_prop) + getString(R.string.prop));
                    eschar.setText(String.valueOf(AppResultReceiver.eschar_prop) + getString(R.string.prop));

                    feverAns.setText(fever);
                    smellAns.setText(smell);
                    levelAns.setText(level);
                    characterAns.setText(character);

                    occurAns.setText(occur);
                    overtimeAns.setText(overtime);
                    occurDate_data.setText(selectedDate);
                    builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Timestamp nowtimestamp = new Timestamp(System.currentTimeMillis());
                            File file = new File(txtFilePath);
                            if (file.exists()) {
                                try {
                                    txtData = new JSONObject(params);
                                    txtData.put("width", String.valueOf(AppResultReceiver.estimateWidth));
                                    txtData.put("height", String.valueOf(AppResultReceiver.estimateHeight));
                                    txtData.put("depth", String.valueOf(AppResultReceiver.getEstimateDepth));
                                    txtData.put("area", String.valueOf(AppResultReceiver.estimateArea));
                                    txtData.put("depth", String.valueOf(AppResultReceiver.estimateDepth));
                                    txtData.put("epithelium", String.valueOf(AppResultReceiver.epithelium_prop));
                                    txtData.put("granular", String.valueOf(AppResultReceiver.granular_prop));
                                    txtData.put("slough", String.valueOf(AppResultReceiver.slough_prop));
                                    txtData.put("eschar", String.valueOf(AppResultReceiver.eschar_prop));
                                    txtData.put("analysisTime", nowtimestamp.getTime());

                                    //SQL LITE
                                    String sqlFileName[] = fileName.split("CameraImg/");
                                    List<Map<String, Object>> listMeasure = null;


                                    ContentValues initialValues = new ContentValues();
                                    initialValues.put("width", String.valueOf(AppResultReceiver.estimateWidth));
                                    initialValues.put("height", String.valueOf(AppResultReceiver.estimateHeight));
                                    initialValues.put("area", String.valueOf(AppResultReceiver.estimateArea));
                                    initialValues.put("depth", String.valueOf(AppResultReceiver.estimateDepth));
                                    initialValues.put("epithelium", String.valueOf(AppResultReceiver.epithelium_prop));
                                    initialValues.put("granular", String.valueOf(AppResultReceiver.granular_prop));
                                    initialValues.put("slough", String.valueOf(AppResultReceiver.slough_prop));
                                    initialValues.put("eschar", String.valueOf(AppResultReceiver.eschar_prop));
                                    initialValues.put("analysisTime", nowtimestamp.getTime());

                                    initialValues.put("fever", feverNum);
                                    initialValues.put("smell", smellNum);
                                    initialValues.put("level", levelNum);
                                    initialValues.put("character", characterNum);
                                    initialValues.put("overtime", overtimeNum);
                                    initialValues.put("firstOcurred", occurNum);
                                    Log.v(TAG,"selectedDate"+selectedDate);
                                    initialValues.put("createdate", selectedDate);
                                    initialValues.put("total", sqlFileName[1]);


                                    if (dataEncrypt == false) {

                                        database.addOrUpdateRaw("table_picNumber", initialValues, "total=?", new String[]{sqlFileName[1]});
                                    } else {

                                        sqllitesecret.addOrUpdateRaw(Sercretdb, "table_picNumber", initialValues, "total=?", new String[]{sqlFileName[1]});
                                        sqllitesecret.add(Sercretdb, "table_picNumberHistory", initialValues, "", new String[]{});

                                    }
                                    // listMeasure = sqllitesecret.querySQLData(Sercretdb,"table_picNumber", null, "", new String[]{}, "");


                                    /*if (AppResultReceiver.recordList.size() != 0) {
                                        //Log.v(TAG,"targetId = assignId = " + String.valueOf(assignId));
                                        addNewParamToMap(assignId);
                                        RandomAccessFile raf = new RandomAccessFile(file, "rw");
                                        raf.setLength(0);
                                        writeToFile(txtFilePath, "evlId=" + evlId + "\r\nownerId=" + ownerId + "\r\n" + "info\r\n", true);
                                        for (int i = 0; i < AppResultReceiver.recordList.size(); i++) {
                                            JSONObject jsonObject = new JSONObject(AppResultReceiver.recordList.get(i));
                                            writeToFile(txtFilePath, jsonObject + "\r\n", true);
                                        }
                                    }*/

                                    String filePath = fileName.substring(0, fileName.lastIndexOf("/") + 1);
                                    Log.v(TAG, "filePath" + filePath);
                                    // String gaiFileName =

                                    String gaiFileName = fileName.substring(fileName.lastIndexOf("/") + 1).replace("_jpg.jpg", "_gai.jpg");
                                    try {
                                        Log.v(TAG, "gaiFileName : " + gaiFileName);
                                        drawView.saveFile(drawView, filePath, gaiFileName);
                                        txtData.put("gaiFileName", gaiFileName);

                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        Mat mat = Imgcodecs.imread("/storage/emulated/0/Download/WoundCamRtc/CameraImg/" + gaiFileName);
                                        Imgproc.rectangle (
                                                mat,                    //Matrix obj of the image
                                                new Point(440, 770),        //p1
                                                new Point(700, 890),       //p2
                                                new Scalar(204, 204, 153),
                                                Core.FILLED,
                                                Core.LINE_8
                                        );

                                        Imgproc.putText(mat, "Height : " + AppResultReceiver.estimateHeight + " cm", new Point(450,810), Core.FONT_ITALIC, 0.7 ,new  Scalar(77),2);
                                        Imgproc.putText(mat, "Width  : " + AppResultReceiver.estimateWidth + " cm", new Point(450,840), Core.FONT_ITALIC, 0.7 ,new  Scalar(77),2);
                                        Imgproc.putText(mat, "Area   : " + AppResultReceiver.estimateArea + " " + Html.fromHtml("cm<sup>2</sup>"), new Point(450,870), Core.FONT_ITALIC, 0.7 ,new  Scalar(77),2);
                                        Imgcodecs.imwrite("/storage/emulated/0/Download/WoundCamRtc/CameraImg/" + gaiFileName, mat);


                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    gaiFileName = filePath + "/" + fileName.substring(fileName.lastIndexOf("/") + 1).replace("_jpg.jpg", "_gai.jpg");
                                    // for data encrypt to read again and embed encrypt key
                                    if (dataEncrypt == true) {
                                        //加密GAI
                                        try {
                                            Thread.sleep(100);
                                        }catch (Exception ex){}
                                        FileHelper.overwriteFileSecret(gaiFileName);
                                    }


                                } catch (Exception e) {
                                    Log.v(TAG, "覆寫檔案錯誤: " + e.getMessage());
                                }
                            }
                            //Log.v(TAG, "位址txt檔案是否存在: " + String.valueOf(file.exists()));
                            isShowLogoutHint = false;
                            selectedDate="";

                            finish();
                        }
                    });

                    //grab upload
                    builder.setNeutralButton(getString(R.string.leave), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
//                            isShowLogoutHint = false;
//                            finish();

                            upload_txt = true;
                            String pattern = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{3}_[0-9]{4}-[0-9]{2}-[0-9]{2}";
                            Pattern r = Pattern.compile(pattern);
                            Matcher m = r.matcher(fileName);
                            String str_upload_file = "";
                            if (m.find( )) {
                                str_upload_file = m.group(0);
                            }else {
                                System.out.println("NO MATCH");
                            }

                            File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_DIR);
                            File[] files=f.listFiles();
                            String str_upload_jpg_path = "";
                            String str_upload_png_path = "";
                            String str_upload_txt_path = "";
                            String str_upload_jpg_itemid = "";
                            String str_upload_png_itemid = "";
                            String str_upload_txt_itemid = "";

                            Pattern r_itemid = Pattern.compile("_[0-9]{1,3}_");

                            for(int i=0; i<files.length; i++)
                            {
                                File file = files[i];

                                if (file.getAbsolutePath().contains("jpg.jpg") && file.getAbsolutePath().contains(str_upload_file)){
                                    str_upload_jpg_path = file.getName();
                                    Matcher m_itemid = r_itemid.matcher(str_upload_jpg_path);
                                    if (m_itemid.find( )) {
                                        str_upload_jpg_itemid = m_itemid.group(0).substring(1,m_itemid.group(0).length()-1);
                                    }else {
                                        System.out.println("NO MATCH11111");
                                    }


                                }


                                if (file.getAbsolutePath().contains(".png") &&file.getAbsolutePath().contains(str_upload_file)){
                                    str_upload_png_path = file.getName();
                                    Matcher m_itemid = r_itemid.matcher(str_upload_png_path);
                                    if (m_itemid.find( )) {
                                        str_upload_png_itemid = m_itemid.group(0).substring(1,m_itemid.group(0).length()-1);
                                    }else {
                                        System.out.println("NO MATCH2222");
                                    }

                                }


                                if (file.getAbsolutePath().contains(".txt") && file.getAbsolutePath().contains(str_upload_file)){
                                    str_upload_txt_path = file.getName();
                                    Matcher m_itemid = r_itemid.matcher(str_upload_txt_path);
                                    if (m_itemid.find( )) {
                                        str_upload_txt_itemid = m_itemid.group(0).substring(1,m_itemid.group(0).length()-1);
                                    }else {
                                        System.out.println("NO MATCH3333");
                                    }
                                }
                            }


                            Pattern evlId_r = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{3}");
                            Matcher evlId_m = evlId_r.matcher(str_upload_jpg_path);
                            String str_evlId = "";
                            if (evlId_m.find( )) {
                                str_evlId = evlId_m.group(0);
                            }else {
                                System.out.println("NO MATCH");
                            }

                            Pattern time_r = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}");
                            Matcher time_m = time_r.matcher(str_upload_jpg_path);
                            String str_time = "";
                            if (time_m.find( )) {
                                str_time = time_m.group(0);
                            }else {
                                System.out.println("NO MATCH");
                            }

                            Log.v("asdsddddddddddd_1",str_time);
                            Log.v("asdsddddddddddd_2",str_evlId);
                            Log.v("asdsddddddddddd_3",str_upload_jpg_path);
                            Log.v("asdsddddddddddd_4",str_upload_png_path);
                            Log.v("asdsddddddddddd_5",str_upload_txt_path);
                            Log.v("asdsddddddddddd_6",str_upload_jpg_itemid);
                            Log.v("asdsddddddddddd_7",str_upload_png_itemid);
                            Log.v("asdsddddddddddd_8",str_upload_txt_itemid);


                            mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                            JobQueueUploadFileJob.database = DBTableHelper.getInstance(getApplicationContext(), mainDir.getAbsolutePath() + File.separator + AppResultReceiver.PROJECT_NAME + ".db");
                            JobQueueUploadFileJob.mLiteHttp = (new XSslLiteHttp(mContext, 5, 360)).getLiteHttp();

                            File file_jpg = new File("/storage/emulated/0/Download/WoundCamRtc/CameraImg/"+str_upload_jpg_path);
                            File file_png = new File("/storage/emulated/0/Download/WoundCamRtc/CameraImg/"+str_upload_png_path);
                            File file_txt = new File("/storage/emulated/0/Download/WoundCamRtc/CameraImg/"+str_upload_txt_path);

                            int uploadCount = 1;

                            try{
                                JobQueueUploadFileJob.uploadSingleRecord(uploadCount,str_evlId,str_time,str_upload_jpg_itemid,file_jpg,false);
                                uploadCount++;
                                Thread.sleep(10);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try{
                                JobQueueUploadFileJob.uploadSingleRecord(uploadCount,str_evlId,str_time,str_upload_png_itemid,file_png,false);
                                uploadCount++;
                                Thread.sleep(10);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try{
                                JobQueueUploadFileJob.uploadSingleRecord(uploadCount,str_evlId,str_time,"13",file_txt,false);
                                Thread.sleep(10);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }




                        }
                    });
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    public void showDialog(final String title, final String message, final int type) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GrabcutActivity.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(title);
                    builder.setMessage(message);
                    builder.setIcon(R.mipmap.color_light_48);
                    //按到旁邊的空白處AlertDialog也不會消失
                    builder.setCancelable(false);

                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        if (type == 3) {
                                dialog.cancel();
                            }
                        }
                    });
                    builder.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    public void showDialog(final String title, final int resourceId, final int type) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(resourceId, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(GrabcutActivity.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(title);
                    builder.setView(layout);

                    requiredNote = layout.findViewById(R.id.required_note);

                    if (type == 1) {
                        setCheckedItem(layout, feverId);
                        setCheckedItem(layout, smellId);
                        setCheckedItem(layout, levelId);
                        setCheckedItem(layout, characterId);
                    } else {
                        datePicker = layout.findViewById(R.id.datePicker);
                        setCheckedItem(layout, overtimeId);
                        setCheckedItem(layout, occurId);
                        //日期點擊事件設定
                        datePicker.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP) {
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    DialogFragment dialogfragment = new DatePickerTheme();
                                    dialogfragment.show(getFragmentManager(), "datePicker");
                                    return true;
                                }
                                return false;
                            }
                        });
                    }
                    //builder.setMessage(message);
                    //builder.setIcon(R.mipmap.color_light_48);
                    //按到旁邊的空白處AlertDialog也不會消失
                    builder.setCancelable(false);
                    builder.setPositiveButton(getString(R.string.ok), null);
                    builder.setNegativeButton(getString(R.string.reset), null);

                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            Button positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            Button negativeBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                            positiveBtn.setOnClickListener(new Button.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Boolean required = false;

                                    if (type == 1) {
                                        fever_rg = layout.findViewById(R.id.fever_radioBtnGrp);
                                        smell_rg = layout.findViewById(R.id.smell_radioBtnGrp);
                                        level_rg = layout.findViewById(R.id.level_radioBtnGrp);
                                        character_rg = layout.findViewById(R.id.character_radioBtnGrp);

                                        feverId = fever_rg.getCheckedRadioButtonId();
                                        Log.v(TAG, "get feverId is " + feverId);
                                        if (feverId != -1) {
                                            feverNum = getResources().getResourceEntryName(feverId).split("_")[1];
                                            RadioButton feverBtn = layout.findViewById(feverId);
                                            fever = feverBtn.getText().toString();
                                        }

                                        smellId = smell_rg.getCheckedRadioButtonId();
                                        if (smellId != -1) {
                                            smellNum = getResources().getResourceEntryName(smellId).split("_")[1];
                                            RadioButton smellBtn = layout.findViewById(smellId);
                                            smell = smellBtn.getText().toString();
                                        }

                                        levelId = level_rg.getCheckedRadioButtonId();
                                        if (levelId != -1) {
                                            levelNum = getResources().getResourceEntryName(levelId).split("_")[1];
                                            RadioButton levelBtn = layout.findViewById(levelId);
                                            level = levelBtn.getText().toString();
                                        }

                                        characterId = character_rg.getCheckedRadioButtonId();
                                        if (characterId != -1) {
                                            characterNum = getResources().getResourceEntryName(characterId).split("_")[1];
                                            RadioButton characterBtn = layout.findViewById(characterId);
                                            character = characterBtn.getText().toString();
                                        }
                                        Log.v(TAG, "feverId:" + feverId + ",smellId:" + smellId + ",levelId" + levelId + ",characterId:" + characterId);
                                        if (feverId != -1 && smellId != -1 && levelId != -1 && characterId != -1) {
                                            required = true;
                                        }

                                    } else {
                                        occur_rg = layout.findViewById(R.id.occur_radioBtnGrp);
                                        overtime_rg = layout.findViewById(R.id.overtime_radioBtnGrp);

                                        occurId = occur_rg.getCheckedRadioButtonId();
                                        if (occurId != -1) {
                                            occurNum = getResources().getResourceEntryName(occurId).split("_")[1];
                                            RadioButton occurBtn = layout.findViewById(occurId);
                                            occur = occurBtn.getText().toString();
                                        }

                                        overtimeId = overtime_rg.getCheckedRadioButtonId();
                                        if (overtimeId != -1) {
                                            overtimeNum = getResources().getResourceEntryName(overtimeId).split("_")[1];
                                            RadioButton overtimeBtn = layout.findViewById(overtimeId);
                                            overtime = overtimeBtn.getText().toString();
                                        }

                                        if (occurId != -1 && overtimeId != -1) {
                                            required = true;
                                        }

                                    }

                                    if (required) {
                                        requiredNote.setVisibility(View.GONE);
                                        dialogInterface.cancel();
                                    } else {
                                        requiredNote.setVisibility(View.VISIBLE);
                                    }
                                }
                            });

                            negativeBtn.setOnClickListener(new Button.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (type == 1) {
                                        feverId = -1;
                                        smellId = -1;
                                        levelId = -1;
                                        characterId = -1;
                                    } else {
                                        occurId = -1;
                                        overtimeId = -1;
                                    }
                                    dialogInterface.cancel();
                                }
                            });
                        }
                    });

                    alertDialog.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

//    public void writeToFile(String outFilename, String msg, boolean append) {
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

    public void addNewParamToMap(int targetId) {
        for (int i = 0; i < AppResultReceiver.recordList.size(); i++) {
            String itemId = (String) AppResultReceiver.recordList.get(i).get("itemId");
            if (Integer.parseInt(itemId) == targetId) {
                AppResultReceiver.recordList.get(i).put("width", String.valueOf(AppResultReceiver.estimateWidth));
                AppResultReceiver.recordList.get(i).put("height", String.valueOf(AppResultReceiver.estimateHeight));
                AppResultReceiver.recordList.get(i).put("area", String.valueOf(AppResultReceiver.estimateArea));
                AppResultReceiver.recordList.get(i).put("epithelium", String.valueOf(AppResultReceiver.epithelium_prop));
                AppResultReceiver.recordList.get(i).put("granular", String.valueOf(AppResultReceiver.granular_prop));
                AppResultReceiver.recordList.get(i).put("slough", String.valueOf(AppResultReceiver.slough_prop));
                AppResultReceiver.recordList.get(i).put("eschar", String.valueOf(AppResultReceiver.eschar_prop));
                break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        Log.v("Message", "inside onConfigurationChanged()");
        /*setContentView(R.layout.grabcut);
        generateViews();*/
        super.onConfigurationChanged(newConfig);
    }

    //設定Grabcut繪圖區域
    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            final float scale = getBaseContext().getResources().getDisplayMetrics().density;
            int l = (int) (left * scale + 0.5f);
            int r = (int) (right * scale + 0.5f);
            int t = (int) (top * scale + 0.5f);
            int b = (int) (bottom * scale + 0.5f);
            p.setMargins(l, t, r, b);
            view.requestLayout();
        }
    }

    //按鈕觸碰事件設定
    private Button.OnTouchListener touch = new Button.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            /*if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                view.setBackgroundResource(R.drawable.button_border);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.setBackgroundColor(getResources().getColor(R.color.button_pressed));
            }*/
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.setBackgroundColor(getResources().getColor(R.color.button_pressed));
            } else {
                view.setBackgroundResource(R.drawable.button_border);
            }
            return false;
        }
    };

    public JobManager configureJobQueueManagerRelax() {
        //3. JobManager的配置器，利用Builder模式
        com.birbit.android.jobqueue.config.Configuration configuration = new com.birbit.android.jobqueue.config.Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBR";

                    @Override
                    public boolean isDebugEnabled() {
                        return false;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        //Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }

                    @Override
                    public void v(String text, Object... args) {
                        //Log.v(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(0)//always keep at least one consumer alive
                .maxConsumerCount(2)//up to 2 consumers at a time
                .loadFactor(1)//1 jobs per consumer
                .consumerKeepAlive(10)//wait 0.1 minute
                .build();
        jobManagerRelax = new JobManager(configuration);
        return jobManagerRelax;
    }

    @Override
    public void finish() {
        Log.i("AnalysisActivity", "關閉 AnalysisActivity 並回傳值");
        //
        Intent intent = new Intent();
        intent.putExtra("url", "demo");
        if (txtData != null && StringUtils.isNotBlank(txtData.toString())) {
            intent.putExtra("params", txtData.toString());
            setResult(Activity.RESULT_OK, intent);
        } else {
            intent.putExtra("params", "");
            setResult(Activity.RESULT_CANCELED, intent);
        }
        super.finish(); // 關閉 AnalysisActivity
    }

    public double toDecimalFormat(double result) {
        try {
            DecimalFormat df = new DecimalFormat("#.#");
            double finalResult = Double.valueOf(df.format(result));
            return finalResult;
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        return 0;
    }

    public double toDecimalFormat(String val) {
        try {
            double result = Double.parseDouble(val);
            DecimalFormat df = new DecimalFormat("#.#");
            double finalResult = Double.valueOf(df.format(result));
            return finalResult;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    //設定暫存的radioButton選項
    public void setCheckedItem(View layout, int radioId) {
        if (radioId != -1) {
            RadioButton radioBtn = layout.findViewById(radioId);
            radioBtn.setChecked(true);
        }
    }

    public void setRedograbcut(boolean param) {
        redograbcut = param;
    }

    public boolean getRedograbcut() {
        return redograbcut;
    }

    public static class DatePickerTheme extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int year = 0, month = 0, day = 0;
            if (datePicker.getText().toString().equals("")) {
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
            } else {
                String dateStr = datePicker.getText().toString();
                String[] dateArr = dateStr.split("-");
                year = Integer.parseInt(dateArr[0]);
                month = Integer.parseInt(dateArr[1]);
                day = Integer.parseInt(dateArr[2]);
            }

            DatePickerDialog picker = new DatePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, this, year, month, day);

            picker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    datePicker.setText("");
                }
            });

            return picker;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            String month_string="";
            String day_string="";
            if(month+1<10){
                month_string="0"+String.valueOf(month+1);
            }else{
                month_string=String.valueOf(month+1);
            }
            if(day<10){
                day_string="0"+day;
            }else{
                day_string=String.valueOf(day);
            }
            selectedDate = year + "-" + month_string + "-" + day_string;
            datePicker.setText(selectedDate);
            datePicker.setSelection(selectedDate.length());
        }
    }

    public boolean checkAccountCorrecct() {
        try {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String uploadUrl = AppResultReceiver.DEFAULT_LOGIN_PATH;
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("uid", AppResultReceiver.account);
                    params.put("pwd", AppResultReceiver.password);
                    final StringRequest upload = new StringRequest(uploadUrl)
                            .setMethod(HttpMethods.Post)
                            .setParamMap(params)
                            .setHttpListener(
                                    new HttpListener<String>(false, false, false) {
                                        @Override
                                        public void onSuccess(String s, com.litesuits.http.response.Response<String> response) {
                                            super.onSuccess(s, response);
                                            JSONObject mJsonObject = null;
                                            try {
                                                mJsonObject = new JSONObject(response.getResult());
                                                String result = mJsonObject.getString("success");
                                                Log.v(TAG, response.getResult());

                                                if (result.toLowerCase().equals("true")) {
                                                    try {
                                                        isAccountCorrecct=true;

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    isAccountCorrecct=false;
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(HttpException exception, com.litesuits.http.response.Response<String> response) {
                                            super.onFailure(exception, response);

                                            try {
                                                isAccountCorrecct=false;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                            );

                    mLiteHttp.execute(upload);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return isAccountCorrecct;
    }
    @NonNull
    protected synchronized void initLiteHttp() {
        if (mLiteHttp == null) {
//            mLiteHttp = LiteHttp.build(mContext)
//                    .setHttpClient(new HttpUrlClient())       // http client
//                    .setContext(mContext)
//                    .setDebugged(true)                     // log output when debugged
//                    .setDoStatistics(false)                // statistics of time and traffic
//                    .setDetectNetwork(false)              // detect network before connect
//                    //.setDefaultMaxRetryTimes(0)
//                    .setSocketTimeout(360000)           // socket timeout: 60s
//                    .setConnectTimeout(30000)         // connect timeout: 10s
//                    //.setConcurrentSize(1)
//                    //.setDefaultMaxRetryTimes(0)
//                    //.setDefaultMaxRedirectTimes(1)
//                    //.setWaitingQueueSize(20)
//                    //.setMaxMemCacheBytesSize(5242880L)
//                    .create();
            mLiteHttp = (new XSslLiteHttp(mContext, 5, 360)).getLiteHttp();

        } else {
            clearLiteHttp();
            initLiteHttp();
        }
    }
    public synchronized void clearLiteHttp() {
        if (mLiteHttp != null) {
            this.mLiteHttp.clearMemCache();
            this.mLiteHttp = null;
        }
    }

}
