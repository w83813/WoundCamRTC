package org.itri.woundcamrtc.analytics;import android.graphics.Color;import android.graphics.Paint;import android.util.Log;public class GrabcutPen {    private Paint mPen;    private String paint_color = "#0000FF";    private String stroke_width = "10";    public GrabcutPen() {        mPen = new Paint();        mPen.setColor(Color.parseColor(paint_color));        mPen.setStrokeWidth(Float.parseFloat(stroke_width));        mPen.setAntiAlias(false);        mPen.setStrokeJoin(Paint.Join.MITER);        mPen.setStyle(Paint.Style.STROKE);        //mPen.setStyle(Paint.Style.FILL);    }    public GrabcutPen(GrabcutPen pen) {        mPen = new Paint(pen.getPen());        mPen.setColor(pen.getPen().getColor());        mPen.setStrokeWidth(pen.getPen().getStrokeWidth());        mPen.setAntiAlias(false);        mPen.setStrokeJoin(Paint.Join.MITER);        mPen.setStyle(Paint.Style.STROKE);//        mPen.setStyle(Paint.Style.FILL);    }    /* Getters Start*/    public Paint getPen() {        return mPen;    }    public String getPaint_color() {        return paint_color;    }    public Float getStroke_width() {        return Float.parseFloat(stroke_width);    }    /* Getters End*/    /* Setters Start*/    public void setPaint_color(int PaintColor) {        try {            mPen.setColor(PaintColor);            paint_color = String.format("#%06X", (0xFFFFFF & PaintColor));        } catch (Exception e) {            Log.d("Pen", e.toString());        }    }    public void setStrokeWidth(float StrokeWidth) {        try {            mPen.setStrokeWidth(StrokeWidth);            stroke_width = String.valueOf(StrokeWidth);        } catch (Exception e) {            Log.d("Paint", e.toString());        }    }    /*Setters End*/}