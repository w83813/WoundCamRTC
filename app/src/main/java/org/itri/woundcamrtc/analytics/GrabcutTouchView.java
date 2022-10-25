package org.itri.woundcamrtc.analytics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.birbit.android.jobqueue.TagConstraint;

import org.itri.woundcamrtc.AppResultReceiver;
import org.itri.woundcamrtc.GrabcutActivity;
import org.itri.woundcamrtc.R;
import org.itri.woundcamrtc.job.JobQueueAnalyticsJob;
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
//import org.opencv.highgui.Highgui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// pls check 11/4 15:24:45  111版的 grabcutTouchView.java fit to screen

public class GrabcutTouchView extends android.support.v7.widget.AppCompatImageView {
    private String TAG = GrabcutTouchView.class.getSimpleName();

    private Context mContext;
    private GrabcutActivity mGrabcutActivity;
    public static Semaphore redoGrabCutSemaphore = new Semaphore(2);  //避免所有 onTouch 完都做grabcut

    private GrabcutPen myPen;
    private Path path;
    private String bg_color_string = "BLACK";
    private ArrayList<Pair<Path, GrabcutPen>> paths;//keeps record of every different path and paint properties associated with it
    private Stack<Pair<Path, GrabcutPen>> backup;//keeps a backup for redoing the changes

    private Path path_border;
    private ArrayList<Pair<Path, GrabcutPen>> paths_border;

    private Bitmap mBitmap = null;
    private Bitmap initdBitmap;
    private Paint mPaint, mBitmapPaint;
    private int color;


    public Mat initdImage;

    public Mat initPreprocessed;
    private Rect initdRoi;
    public Mat initdMask;

    private Mat finalMask;
    private String inputFileName;
    private String outputFilePath;
    private String imgParams = "";
    private int downsampleRate;
    public List<MatOfPoint> lastContours;
    Mat lastResultImage;

    int screenWidth = 112;
    int screenHeight = 54;
    float scale = (float) 1.0;
    int strokeWidth = 8;

    private boolean isCanTouch = false;
    public static final float SCALE_MAX = 5.0f; //最大的缩放比例
    private static final float SCALE_MIN = 1.0f;
    private double oldDist = 0;
    private double moveDist = 0;
    private float downX1 = 0;
    private float downX2 = 0;
    private float downY1 = 0;
    private float downY2 = 0;
    private String str_draw = "draw";
    private String str_scale = "scale";
    private String str_drawborder = "drawborder";
    public static String str_touchstatus;
    private float start_x,start_y;
    private boolean draw_status = false;
    public static boolean drawbroder_done = false;
    private boolean touch_scale = false;
    public static boolean remove_status = false;

    int lineType = 8;
    int shift = 0;

    public List<Point> points_border = new ArrayList<>();
    public MatOfPoint matPt_border = new MatOfPoint();
    public List<MatOfPoint> ppt_border = new ArrayList<MatOfPoint>();

    public GrabcutTouchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        path = new Path();
        paths = new ArrayList<>();
        path_border = new Path();
        paths_border = new ArrayList<>();
        backup = new Stack<>();
        initPaint(attrs);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.MITER);
        mPaint.setStrokeCap(Paint.Cap.BUTT);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setTextSize(30);

//        superres_init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        onSizeChangedv110(w, h, oldw, oldh);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onSizeChangedv110(int w, int h, int oldw, int oldh) {
        try {
            if (initdBitmap == null)
                return;
            float xscale = (float) w / (float) initdBitmap.getWidth();
            float yscale = (float) h / (float) initdBitmap.getHeight();

            scale = xscale;
            if (xscale > yscale) // make sure both dimensions fit (use the
                // smaller scale)
                scale = yscale;
            screenWidth = (int) (initdBitmap.getWidth() * scale);
            screenHeight = (int) (initdBitmap.getHeight() * scale); // use the same scale for both
            // dimensions
            // if you want it centered on the display (black borders)
            mBitmap = Bitmap.createScaledBitmap(initdBitmap, screenWidth,
                    screenHeight, true);
        } catch (Exception ee) {
        }
    }


    //called when view is refreshed
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if (mBitmap == null)
                return;
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            Paint paint = myPen.getPen();


                for (Pair<Path, GrabcutPen> p : paths_border) {
                    canvas.drawPath(p.first, p.second.getPen());
                }
                canvas.drawPath(path_border, paint);

                for (Pair<Path, GrabcutPen> p : paths) {
                    canvas.drawPath(p.first, p.second.getPen());
                }
                canvas.drawPath(path, paint);

                if (drawbroder_done == true){
                    GrabcutActivity.tv_drawstatus.setText(R.string.please_circle_the_wound);
                    if (!remove_status){
                        myPen.setPaint_color(0xFFFF0000);
                        str_touchstatus = str_draw;
                    } else {
                        myPen.setPaint_color(0xFF0000FF);
                        str_touchstatus = str_draw;
                    }

                }


        } catch (Exception ee) {
            //ee.printStackTrace();
        }
    }

    //called when screen is touched or untouched
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float X = (int) event.getX();
        float Y = (int) event.getY();
        int eventaction = event.getPointerCount();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                draw_status = true;
                touch_scale = false;
                start_x = X;
                start_y = Y;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                draw_status = false;
                touch_scale = true;
                downX1 = event.getX(0);
                downX2 = event.getX(1);
                downY1 = event.getY(0);
                downY2 = event.getY(1);
                oldDist = spacing(event); //两点按下时的距离
                break;

            case MotionEvent.ACTION_MOVE:
                if (touch_scale == false){
                    if ( str_touchstatus == str_drawborder){
                        if (draw_status) {
                            path_border.moveTo(start_x, start_y);
                        }
                        draw_status = false;
                        Point temp_point = new Point(X * 3.4,Y*3.4);
                        points_border.add(temp_point);
                        matPt_border.fromList(points_border);
                        path_border.lineTo(X, Y);
                    }
                    if ( str_touchstatus == str_draw){
                        if (draw_status) {
                            path.moveTo(start_x, start_y);
                        }
                        draw_status = false;
                        path.lineTo(X, Y);
                    }
                }
                else if (eventaction == 2) {
                    float x1 = event.getX(0);
                    float x2 = event.getX(1);
                    float y1 = event.getY(0);
                    float y2 = event.getY(1);

                    double changeX1 = x1 - downX1;
                    double changeX2 = x2 - downX2;
                    double changeY1 = y1 - downY1;
                    double changeY2 = y2 - downY2;

                    if (getScaleX() > 1) { //滑动
                        float lessX = (float) ((changeX1) / 2 + (changeX2) / 2);
                        float lessY = (float) ((changeY1) / 2 + (changeY2) / 2);
                        setSelfPivot(-lessX, -lessY);
                        Log.d(TAG, "此时为滑动");
                    }
                    //缩放处理
                    moveDist = spacing(event);
                    double space = moveDist - oldDist;
                    float scale = (float) (getScaleX() + space / mBitmap.getWidth());
                    if (scale < SCALE_MIN) {
                        setScale(SCALE_MIN);
                    } else if (scale > SCALE_MAX) {
                        setScale(SCALE_MAX);
                    } else {
                        setScale(scale);
                    }
                }


                break;

            case MotionEvent.ACTION_UP:
                //===========================================================================

                if (touch_scale == false){
                    if ( str_touchstatus == str_drawborder){
                        points_border.add(new Point(start_x * 3.4,start_y * 3.4));
                        path_border.lineTo(start_x, start_y);
                        Pair<Path, GrabcutPen> pair = new Pair<>(path_border, myPen);
                        paths_border.add(pair);
                        path_border = new Path();
                        String c = myPen.getPaint_color();
                        float w = myPen.getStroke_width();
                        myPen = new GrabcutPen();
                        myPen.setStrokeWidth(w);

                        myPen.setPaint_color(Color.parseColor(c));

                        if (GrabcutTouchView.redoGrabCutSemaphore != null) {
                            synchronized (GrabcutTouchView.redoGrabCutSemaphore) {
                                try {
                                    if (GrabcutTouchView.redoGrabCutSemaphore.tryAcquire(50, TimeUnit.MILLISECONDS)) {
                                        System.gc();
                                        // perform grabcut with Cancelable
                                        mGrabcutActivity.setRedograbcut(true);
                                        AppResultReceiver.grabcutWithInteraction = true;

                                        AsynRedoGrabCut asynRedoGrabCut = new AsynRedoGrabCut();
                                        asynRedoGrabCut.execute();

                                        try {
                                            redoGrabCutSemaphore.release();
                                        } catch (Exception ee) {
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }


                        Mat mat = Imgcodecs.imread(inputFileName);
                        Mat temp_mat =  new Mat(mat.height(), mat.width(), CvType.CV_8UC3, new Scalar(255,0,0));
                        Mat black_mat = new Mat(temp_mat.height(), temp_mat.width(), CvType.CV_8U, new Scalar(0));
                        ppt_border.add(matPt_border);

                        Imgproc.fillPoly(black_mat,
                                ppt_border,
                                new Scalar(255),
                                lineType,
                                shift,
                                new Point(0,0) );

                        mat.copyTo(temp_mat,black_mat);

                        //Core.bitwise_and(temp_mat,black_mat,temp_mat);


                        //Saving and displaying the image
                        String newString = inputFileName.replace("_jpg.jpg", "_roi.jpg");
                        Imgcodecs.imwrite(newString, temp_mat);

                        ppt_border.clear();
                        points_border.clear();

                        GrabcutActivity.iv_drawcolor.setBackgroundColor(Color.rgb(255,0,0));
                        GrabcutActivity.tv_draw.setText(R.string.keep);
                        drawbroder_done = true;

                    }

                    if ( str_touchstatus == str_draw){
                        Pair<Path, GrabcutPen> pair = new Pair<>(path, myPen);
                        paths.add(pair);
                        path = new Path();
                        String c = myPen.getPaint_color();
                        float w = myPen.getStroke_width();
                        myPen = new GrabcutPen();
                        myPen.setStrokeWidth(w);

                        myPen.setPaint_color(Color.parseColor(c));

                        if (GrabcutTouchView.redoGrabCutSemaphore != null) {
                            synchronized (GrabcutTouchView.redoGrabCutSemaphore) {
                                try {
                                    if (GrabcutTouchView.redoGrabCutSemaphore.tryAcquire(50, TimeUnit.MILLISECONDS)) {
                                        System.gc();
                                        // perform grabcut with Cancelable
                                        mGrabcutActivity.setRedograbcut(true);
                                        AppResultReceiver.grabcutWithInteraction = true;

                                        AsynRedoGrabCut asynRedoGrabCut = new AsynRedoGrabCut();
                                        asynRedoGrabCut.execute();

                                        try {
                                            redoGrabCutSemaphore.release();
                                        } catch (Exception ee) {
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }

                    }


                }

                else {

                    downX1 = 0;
                    downY1 = 0;
                    downX2 = 0;
                    downY2 = 0;
                }

                break;
        }
        invalidate();
        return true;
    }

    /**
     * 触摸使用的移动事件
     *
     * @param lessX
     * @param lessY
     */
    private void setSelfPivot(float lessX, float lessY) {
        float setPivotX = 0;
        float setPivotY = 0;
        setPivotX = getPivotX() + lessX;
        setPivotY = getPivotY() + lessY;
        if (setPivotX < 0 && setPivotY < 0) {
            setPivotX = 0;
            setPivotY = 0;
        } else if (setPivotX > 0 && setPivotY < 0) {
            setPivotY = 0;
            if (setPivotX > getWidth()) {
                setPivotX = getWidth();
            }
        } else if (setPivotX < 0 && setPivotY > 0) {
            setPivotX = 0;
            if (setPivotY > getHeight()) {
                setPivotY = getHeight();
            }
        } else {
            if (setPivotX > getWidth()) {
                setPivotX = getWidth();
            }
            if (setPivotY > getHeight()) {
                setPivotY = getHeight();
            }
        }
        setPivot(setPivotX, setPivotY);
    }

    /**
     * 计算两个点的距离
     *
     * @param event
     * @return
     */
    private double spacing(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    /**
     * 平移画面，当画面的宽或高大于屏幕宽高时，调用此方法进行平移
     *
     * @param x
     * @param y
     */
    public void setPivot(float x, float y) {
        setPivotX(x);
        setPivotY(y);
    }


    /**
     * 设置放大缩小
     *
     * @param scale
     */
    public void setScale(float scale) {
        setScaleX(scale);
        setScaleY(scale);
    }

    /**
     * 初始化比例，也就是原始比例
     */
    public void setInitScale() {
        setScaleX(1.0f);
        setScaleY(1.0f);
        setPivot(getWidth() / 2, getHeight() / 2);
    }


    private final class AsynRedoGrabCut extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            mGrabcutActivity.showProgressBarInfo(mGrabcutActivity.getString(R.string.progressing_ranging), true);
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(Void... params) {
            try {
                redoGrabCut(getLastMask(), mGrabcutActivity, inputFileName);
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
                    progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.DnnAI) + "& " + mGrabcutActivity.getString(R.string.interactied);
                else
                    progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.DnnAI);

            } else if (AppResultReceiver.grabcutWithColorAI) {
                if (AppResultReceiver.grabcutWithInteraction)
                    progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.ColorAI) + "& " + mGrabcutActivity.getString(R.string.interactied);
                else
                    progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.ColorAI);
            } else {
                if (AppResultReceiver.grabcutWithInteraction)
                    progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.interactied);
                else
                    progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.default_boundary);
            }
            mGrabcutActivity.showProgressBarInfo(progressBarInfo, false);
        }
    }

    //Initializes and set all the values of paint and background color by taking values from xml
    @SuppressLint("ResourceType")
    private void initPaint(AttributeSet atr) {
        myPen = new GrabcutPen();
        TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(atr, R.styleable.Canvas, 0, 0);
        String t_color = typedArray.getString(R.styleable.Canvas_paint_color);
        String t_size = typedArray.getString(R.styleable.Canvas_paint_width);
        String background_color = typedArray.getString(R.styleable.Canvas_bg_color);

        t_size = "10";

        if (t_color != null) {
            myPen.setPaint_color(Color.parseColor(t_color));
        }

        if (t_size != null) {
            myPen.setStrokeWidth(Float.valueOf(t_size));
        }

        if (background_color != null) {
            try {
                this.setBackgroundColor(Color.parseColor(background_color));
                bg_color_string = String.format("#%06X", (0xFFFFFF & Color.parseColor(background_color)));
            } catch (Exception e) {
                Log.d("TouchDrawView", e.toString());
            }

        }
    }


    @SuppressLint("WrongThread")
    public void saveFile(GrabcutTouchView view, String folderName, String fileName) {
        Bitmap bitmap = view.mBitmap;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();

        File f = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!f.exists()) {
            f.mkdirs();
        }
        File file = new File(folderName + "/" + fileName);
        FileOutputStream ostream;
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.flush();
            ostream.close();
            Toast.makeText(mContext, mContext.getString(R.string.image_saved), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, mContext.getString(R.string.alert_error), Toast.LENGTH_LONG).show();
        }

    }

    //returns the  bitmap file of the screen
    public Bitmap getFile() {
        Bitmap file = this.getDrawingCache();
        return file;
    }

    //UnDo the last change done
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public void undo() {
        if (paths.size() >= 1) {
            backup.push(paths.get(paths.size() - 1));
            paths.remove(paths.size() - 1);
            // perform grabcut with Cancelable
            //redoGrabCut(regenMask());
            if (GrabcutTouchView.redoGrabCutSemaphore != null) {
                synchronized (GrabcutTouchView.redoGrabCutSemaphore) {
                    try {
                        if (GrabcutTouchView.redoGrabCutSemaphore.tryAcquire(50, TimeUnit.MILLISECONDS)) {
                            redoGrabCut(getLastMask(), mGrabcutActivity, "");
                            try {
                                redoGrabCutSemaphore.release();
                            } catch (Exception ee) {
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
            invalidate();
        }
    }

    //ReDo the last change done
    public void redo() {
        if (!backup.empty()) {
            paths.add(backup.peek());
            backup.pop();
            invalidate();
        }
    }

    //Clears the screen
    public void clear() {
        try {
            backup.clear();
            for (Pair<Path, GrabcutPen> p : paths) {
                backup.push(p);
            }
            paths.clear();
        } catch (Exception ee) {
            //ee.printStackTrace();
        }
        invalidate();
    }

    public void clearAndRedoGrabCut() {
        try {
            backup.clear();
            for (Pair<Path, GrabcutPen> p : paths) {
                backup.push(p);
            }
            paths.clear();
            mGrabcutActivity.setRedograbcut(true);
            //redoGrabCut(initdMask.clone(), mGrabcutActivity, "");
        } catch (Exception ee) {
            //ee.printStackTrace();
        }
        invalidate();
    }



    //setters start
    public void setPaintColor(int paintColor) {
        myPen.setPaint_color(paintColor);
    }


    public void initGrabCut(GrabcutActivity mGrabcutActivity, Mat img, Mat resized, Mat preprocessed, Rect inROI, Mat mask, String fileName, String outFilePath, int downsampleRate, String params) {
        this.initdImage = img.clone();
        this.initPreprocessed = preprocessed.clone();
        this.initdRoi = inROI;
        this.initdMask = mask.clone();
        this.inputFileName = fileName;
        this.outputFilePath = outFilePath;
        this.downsampleRate = downsampleRate;
        if (this.screenWidth == 112) {
            this.screenWidth = img.cols();
            this.screenHeight = img.rows();
//            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGRA2RGBA);
//            mBitmap = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(img, mBitmap);
        }

        this.imgParams = params;
        this.mGrabcutActivity = mGrabcutActivity;
        mGrabcutActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    invalidate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void redoGrabCut(Mat mask, GrabcutActivity grabcutActivity, String writeFile) {
        if (grabcutActivity != null)
            mGrabcutActivity = grabcutActivity;
        if (mask == null || initdImage == null || initPreprocessed == null)
            return;
        try {

            String roi_image = writeFile.replace("_jpg.jpg", "_roi.jpg");
            File roi_file = new File(roi_image);
            if (roi_file.exists()) {
                writeFile = roi_image;
            }

            Mat img = new Mat();
            if (writeFile.contains("roi")== true){
                img = Imgcodecs.imread(writeFile);
                Mat preprocessed = new Mat();
                Mat resized = new Mat();
                Size sz = new Size(img.width() / 5, img.height() / 5);
                Imgproc.resize(img.clone(), resized, sz, 0, 0, Imgproc.INTER_LINEAR);
                Imgproc.medianBlur(resized.clone(), preprocessed, 11);
                img = preprocessed;
            } else {
                img = initPreprocessed.clone();
            }

            //this.invalidate();


            if (mGrabcutActivity.getRedograbcut()) {
                Mat bgModel = new Mat();
                Mat fgModel = new Mat();

                if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_GRABCUT) {
                    Mat tmp = mask.clone();
                    ColorHelper.changeColor(tmp, Imgproc.GC_BGD, 0);
                    ColorHelper.changeColor(tmp, Imgproc.GC_PR_BGD, 64);
                    ColorHelper.changeColor(tmp, Imgproc.GC_PR_FGD, 128);
                    ColorHelper.changeColor(tmp, Imgproc.GC_FGD, 180);
                    ColorHelper.changeColor(tmp, 4, 235);
                    ColorHelper.changeColor(tmp, 5, 245);
                    ColorHelper.changeColor(tmp, 6, 255);
                    String Main_DIR = AppResultReceiver.PROJECT_NAME;
                    File file5 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Main_DIR);
                    Imgcodecs.imwrite(file5.getAbsolutePath() + "/mask_before.png", tmp);
                    tmp.release();
                }

                try {

                    Log.d(TAG,"img.w:"+img.cols()+",img.h:"+img.rows()+",mask.w:"+mask.cols()+",mask.h:"+mask.rows());
                    Imgproc.grabCut(img, mask, initdRoi, bgModel, fgModel, 1, Imgproc.GC_INIT_WITH_MASK);
                } catch (Exception ex) {
                    //error occurred if all pixel in the mask are not set fg or bg
                    ex.printStackTrace();
                }


                if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_GRABCUT) {
                    Mat tmp = mask.clone();
                    ColorHelper.changeColor(tmp, Imgproc.GC_BGD, 0);
                    ColorHelper.changeColor(tmp, Imgproc.GC_PR_BGD, 64);
                    ColorHelper.changeColor(tmp, Imgproc.GC_PR_FGD, 128);
                    ColorHelper.changeColor(tmp, Imgproc.GC_FGD, 180);
                    ColorHelper.changeColor(tmp, 4, 235);
                    ColorHelper.changeColor(tmp, 5, 245);
                    ColorHelper.changeColor(tmp, 6, 255);
                    String Main_DIR = AppResultReceiver.PROJECT_NAME;
                    File file5 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Main_DIR);
                    Imgcodecs.imwrite(file5.getAbsolutePath() + "/mask_after.png", tmp);
                    tmp.release();
                }

                initdMask = mask.clone();

                Mat source_PR_FGD = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
                Mat mask_PR_FGD = new Mat();
                Core.compare(mask, source_PR_FGD, mask_PR_FGD, Core.CMP_EQ);

                Mat source_FGD = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_FGD));
                Mat mask_FGD = new Mat();
                Core.compare(mask, source_FGD, mask_FGD, Core.CMP_EQ);

                mask.setTo(new Scalar(Imgproc.GC_PR_BGD));
                Core.add(mask_PR_FGD, mask_FGD, mask);
                mask_PR_FGD.release();
                mask_FGD.release();

                if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_GRABCUT) {
                    Mat tmp = mask.clone();
                    //ColorHelper.changeColor(tmp,Imgproc.GC_BGD, 0 );
                    ColorHelper.changeColor(tmp, Imgproc.GC_PR_BGD, 32);
                    ColorHelper.changeColor(tmp, Imgproc.GC_PR_FGD, 168);
                    ColorHelper.changeColor(tmp, Imgproc.GC_FGD, 200);
                    ColorHelper.changeColor(tmp, 4, 255);
                    String Main_DIR = AppResultReceiver.PROJECT_NAME;
                    File file5 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Main_DIR);
                    Imgcodecs.imwrite(file5.getAbsolutePath() + "/mask_end.png", tmp);
                    tmp.release();
                }

                Mat mask_orig = new Mat();
                Size sz = new Size(initdImage.width(), initdImage.height());
                Imgproc.resize(mask.clone(), mask_orig, sz, 0, 0, Imgproc.INTER_CUBIC);

                if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_GRABCUT) {
                    Mat tmp = mask_orig.clone();
                    //ColorHelper.changeColor(tmp,Imgproc.GC_BGD, 0 );
                    ColorHelper.changeColor(tmp, Imgproc.GC_PR_BGD, 32);
                    ColorHelper.changeColor(tmp, Imgproc.GC_PR_FGD, 168);
                    ColorHelper.changeColor(tmp, Imgproc.GC_FGD, 200);
                    ColorHelper.changeColor(tmp, 4, 255);
                    String Main_DIR = AppResultReceiver.PROJECT_NAME;
                    File file5 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Main_DIR);
                    Imgcodecs.imwrite(file5.getAbsolutePath() + "/mask_upsampling.png", tmp);
                    tmp.release();
                }

                lastContours = new ArrayList<>();
                Mat hierarchy = new Mat();
                //Imgproc.findContours(mask_orig, lastContours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.findContours(mask_orig, lastContours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                Collections.sort(lastContours, new Comparator<MatOfPoint>() {
                    @Override
                    public int compare(MatOfPoint o1, MatOfPoint o2) {
                        // first return 1, last return -1
                        // biggest first
                        if (o1 == null || o2 == null) return 0;
                        org.opencv.core.Rect rect1 = Imgproc.boundingRect(o1);
                        org.opencv.core.Rect rect2 = Imgproc.boundingRect(o2);
                        int rectSize1 = rect1.width * rect1.height;
                        int rectSize2 = rect2.width * rect2.height;
                        if (rectSize1 > rectSize2) {
                            return 1;
                        } else if (rectSize1 == rectSize2) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                });
                hierarchy.release();
                mask_FGD.release();
                source_FGD.release();
                mask_PR_FGD.release();
                source_PR_FGD.release();
                fgModel.release();
                bgModel.release();
                img.release();
                img = initdImage.clone();


                if (lastContours.size() > 0) {
                    MatOfPoint2f newMtx = new MatOfPoint2f(lastContours.get(lastContours.size() - 1).toArray());
                    org.opencv.core.RotatedRect rotatedRect = Imgproc.minAreaRect(newMtx);
                    org.opencv.core.Point[] vtx = new Point[4];
                    rotatedRect.points(vtx);
                    Scalar colorScalar1 = new Scalar(0, 255, 0);
                    for (int jj = 0; jj < 4; jj++)
                        Imgproc.line(img, vtx[jj], vtx[(jj + 1) % 4], colorScalar1, 12);

                    Imgproc.drawContours(img, lastContours, lastContours.size() - 1, new Scalar(255, 100, 100), 15);

                    lastResultImage = img;

                    if (AppResultReceiver.DEBUG_LEVEL == AppResultReceiver.DEBUG_GRABCUT) {
                        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), AppResultReceiver.Main_DIR);
                        String fileName = file.getPath() + File.separator + s.format(new Date());
                        Imgcodecs.imwrite(fileName + "_finalContours.png", lastResultImage);
                    }

                }
                grabcutActivity.setRedograbcut(false);
                Imgproc.cvtColor(img, img, Imgproc.COLOR_BGRA2RGBA);

                Size sz1 = new Size(screenWidth, screenHeight);
                Imgproc.resize(img, img, sz1);
                if (mBitmap == null) {
                    mBitmap = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
                    initdBitmap = mBitmap;
                } else {
                    mBitmap = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
                }
                Utils.matToBitmap(img, mBitmap);

                String progressBarInfo = "";
                if (AppResultReceiver.grabcutWithDnnAI) {
                    if (AppResultReceiver.grabcutWithInteraction)
                        progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.DnnAI) + "& " + mGrabcutActivity.getString(R.string.interactied);
                    else
                        progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.DnnAI);

                } else if (AppResultReceiver.grabcutWithColorAI) {
                    if (AppResultReceiver.grabcutWithInteraction)
                        progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.ColorAI) + "& " + mGrabcutActivity.getString(R.string.interactied);
                    else
                        progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.ColorAI);
                } else {
                    if (AppResultReceiver.grabcutWithInteraction)
                        progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.interactied);
                    else
                        progressBarInfo = mGrabcutActivity.getString(R.string.with) + mGrabcutActivity.getString(R.string.default_boundary);
                }
                mGrabcutActivity.showProgressBarInfo(progressBarInfo, false);

                try {
                    //Thread.sleep(100);
                    mGrabcutActivity.jobManagerLocal.cancelJobsInBackground(null, TagConstraint.ALL, "analyticsJob");
                } catch (Exception e) {
                }
                mGrabcutActivity.jobManagerLocal.addJobInBackground(new JobQueueAnalyticsJob(mGrabcutActivity.jobManagerLocal, "", mGrabcutActivity, mGrabcutActivity.fileName, lastContours, imgParams));

                mGrabcutActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            invalidate();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public Mat getLastMask() {
        if (initdMask == null)
            return null;
        float xp = initdMask.width() / ((float) initdBitmap.getWidth() * scale);
        float yp = initdMask.height() / ((float) initdBitmap.getHeight() * scale);
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(xp, yp, 1, 1);
        Bitmap _image = Bitmap.createBitmap(initdMask.width(), initdMask.height(), Bitmap.Config.ARGB_8888);


        Utils.matToBitmap(initdMask, _image);
        Canvas _canvas = new Canvas(_image);
        int redColor = Color.parseColor("#FF0000");
        int blueColor = Color.parseColor("#0000FF");
        for (Pair<Path, GrabcutPen> p : paths) {
            Path path = new Path(p.first);
            GrabcutPen pen = new GrabcutPen(p.second);
            path.transform(scaleMatrix);
            pen.getPen().setStrokeWidth(strokeWidth * xp);


            if (pen.getPen().getColor() == blueColor)
                pen.getPen().setColor(0xFF000000);
            else if (pen.getPen().getColor() == redColor)
                // kernoli todo
                pen.getPen().setColor(0xFF010101);  // maybe Imgproc.grabCut 2.4.13 bug? need use 0 to be obvious background, and 2 to be obvious foreground

            _canvas.drawPath(path, pen.getPen());
        }

        Mat mat = new Mat();
        Utils.bitmapToMat(_image, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        _image.recycle();
        return mat;
    }


    public List<MatOfPoint> getLastResultContours() {
        return this.lastContours;
    }


    public static Mat grabcut(Mat src) {
        Mat thr = new Mat();
        Imgproc.cvtColor(src, thr, Imgproc.COLOR_BGR2GRAY);
        // Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU
        Imgproc.GaussianBlur(thr, thr, new Size(3, 3), 0);
        // http://docs.opencv.org/master/d7/d4d/tutorial_py_thresholding.html#gsc.tab=0
        Imgproc.threshold(thr, thr, 0, 255, Imgproc.THRESH_BINARY
                + Imgproc.THRESH_OTSU); // Threshold the gray
        // Imgproc.adaptiveThreshold(imgGray, imgThreshold, 255,
        // Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>(); // Vector for
        // adaptiveThreshold(gray, result, 255, ADAPTIVE_THRESH_MEAN_C,
        // THRESH_BINARY, 15, 40);
        // double mean = Core.mean(image).val[0];
        // Imgproc.GaussianBlur(imageMat, imageMat, new Size(3, 3), 0);
        // Imgproc.threshold(imageMat, imageMat, 0, 255, Imgproc.THRESH_OTSU);
        // cvSmooth(imageMat, imageMat, CV_MEDIAN, new Size(3, 3), 0);
        // Imgproc.adaptiveThreshold(imageMat, imageMat, 255,
        // Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);
        Mat h = new Mat();
        double largest_area = 0;
        int largest_contour_index = 0;
        Rect bounding_rect = new Rect();
        Imgproc.findContours(thr, contours, h, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE); // Find the contours in the image
        for (int i = 0; i < contours.size(); i++) // iterate through each
        {
            double a = Imgproc.contourArea(contours.get(i), false); // Find the
            if (a > largest_area) {
                largest_area = a;
                largest_contour_index = i; // Store the index of largest contour
                bounding_rect = Imgproc.boundingRect(contours.get(i)); // Find
            }

        }
        int width = bounding_rect.width;
        int height = bounding_rect.height;
        if (largest_area == 0 || width < 160 || height < 160) {
            //没有轮廓不做处理
            return src;
        } else {

            int x1 = bounding_rect.x;
            int y1 = bounding_rect.y;
            int x2 = x1 + width;
            int y2 = y1 + height;
            Point tl = new Point(x1, y1);
            Point br = new Point(x2, y2);
            return grabcut(src, tl, br);
        }

    }

    public static Mat grabcut(Mat img, Point tl, Point br) {
        Mat background = new Mat(img.size(), CvType.CV_8UC3,
                new Scalar(255, 255, 255));
        Mat firstMask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat mask;
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        Mat dst = new Mat();
        Rect rect = new Rect(tl, br);
        Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel,
                5, Imgproc.GC_INIT_WITH_RECT);
        Core.compare(firstMask, source, firstMask, Core.CMP_EQ);
        Mat foreground = new Mat(img.size(), CvType.CV_8UC3,
                new Scalar(255, 255, 255));
        img.copyTo(foreground, firstMask);
        Scalar color = new Scalar(255, 0, 0, 255);
        Imgproc.rectangle(img, tl, br, color);
        Mat tmp = new Mat();
        Imgproc.resize(background, tmp, img.size());
        background = tmp;
        mask = new Mat(foreground.size(), CvType.CV_8UC1,
                new Scalar(255, 255, 255));
        Imgproc.cvtColor(foreground, mask, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mask, mask, 254, 255, Imgproc.THRESH_BINARY_INV);
        Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
        background.copyTo(dst);
        background.setTo(vals, mask);
        Core.add(background, foreground, dst, mask);
        firstMask.release();
        source.release();
        bgModel.release();
        fgModel.release();
        vals.release();
        //Highgui.imwrite("/root/grabcut.jpg", dst);
        return dst;
    }

    public void clearAndRedoGrabCut_border() {
        try {
            backup.clear();
            for (Pair<Path, GrabcutPen> p : paths_border) {
                backup.push(p);
            }
            paths_border.clear();
            mGrabcutActivity.setRedograbcut(true);
            //redoGrabCut(initdMask.clone(), mGrabcutActivity, "");
        } catch (Exception ee) {
            //ee.printStackTrace();
        }
        invalidate();
    }

    public void reset_drawstatus(){
        str_draw = "draw";
        str_scale = "scale";
        str_drawborder = "drawborder";
        String str_touchstatus;
        draw_status = false;
        drawbroder_done = false;
        touch_scale = false;
        remove_status = false;
    }

}
