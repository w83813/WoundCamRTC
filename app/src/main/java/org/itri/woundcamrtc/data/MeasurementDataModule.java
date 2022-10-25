package org.itri.woundcamrtc.data;

import android.os.Parcel;
import android.os.Parcelable;


public class MeasurementDataModule implements Parcelable {

    public static final String TYPE_TEMPERATURE = "TP";
    public static final String TYPE_WEIGHT = "WG";

    //脈搏
    public static final String TYPE_BLOOD_PRESSURE_PULSE = "BG_PULSE";
    //收縮壓
    public static final String TYPE_BLOOD_PRESSURE_SYSTOLIC = "BG_SYSTOLIC";
    //舒張壓
    public static final String TYPE_BLOOD_PRESSURE_DISTOLIC = "BG_DISTOLIC";
    public static final String TYPE_BLOOD_SUGURE_AC = "AC";
    public static final String TYPE_BLOOD_SUGURE_PC = "PC";

    //拍攝距離
    public static final String TYPE_SHUT_DISTANCE = "WD_SHUT_DIST";

    //傷口長
    public static final String TYPE_WOUND_LENGTH = "WD_LENGTH";
    //傷口寬
    public static final String TYPE_WOUND_WIDTH = "WD_WIDTH";
    //傷口面積
    public static final String TYPE_WOUND_AREA = "WD_AREA";
    //傷口深度
    public static final String TYPE_WOUND_DEPTH = "WD_DEPTH";
    //傷口高溫
    public static final String TYPE_WOUND_TEMP_HIGH = "WD_TEMP_HIGH";
    //傷口均溫
    public static final String TYPE_WOUND_TEMP_AVG = "WD_TEMP_AVG";

    public static final String TYPE_WOUND_TEMP_CENTER = "WD_TEMP_CENTER";
    //傷口邊溫
    public static final String TYPE_WOUND_TEMP_EDGE = "WD_TEMP_EDGE";
    //傷口棕黄色比例
    public static final String TYPE_WOUND_COLOR_YELLOWD = "WD_COLOR_YELLOWD";
    //傷口黑褐色比例
    public static final String TYPE_WOUND_COLOR_BROWN = "WD_COLOR_BROWN";
    //傷口紅粉色比例
    public static final String TYPE_WOUND_COLOR_PINK = "WD_COLOR_PINK";
    //T
    public static final String TYPE_WOUND_TIME_T = "WD_TIME_T";
    //I
    public static final String TYPE_WOUND_TIME_I = "WD_TIME_I";
    //M
    public static final String TYPE_WOUND_TIME_M = "WD_TIME_M";
    //E
    public static final String TYPE_WOUND_TIME_E = "WD_TIME_E";


    public static final String[] VALUE_TYPE_ARRAY = new String[]{
            TYPE_TEMPERATURE,
            TYPE_WEIGHT,
            TYPE_BLOOD_PRESSURE_PULSE,
            TYPE_BLOOD_PRESSURE_SYSTOLIC,
            TYPE_BLOOD_SUGURE_PC,
            TYPE_BLOOD_SUGURE_AC,
            TYPE_BLOOD_PRESSURE_DISTOLIC,

            TYPE_WOUND_LENGTH,
            TYPE_WOUND_WIDTH,
            TYPE_WOUND_AREA,
            TYPE_WOUND_TEMP_HIGH,
            TYPE_WOUND_TEMP_AVG,
            TYPE_WOUND_TEMP_CENTER,
            TYPE_WOUND_TEMP_EDGE,
            TYPE_WOUND_COLOR_YELLOWD,
            TYPE_WOUND_COLOR_BROWN,
            TYPE_WOUND_COLOR_PINK,
            TYPE_WOUND_TIME_T,
            TYPE_WOUND_TIME_I,
            TYPE_WOUND_TIME_M,
            TYPE_WOUND_TIME_E


    };

    private long _id;
    private String barcodeType;
    private String caseId;
    private long nurseId;
    private long ownerId;
    private long evalDate;
    private String valueType;
    private String value = "";
    private long uploadId = -1;


    public String getBarcodeType() {
        return barcodeType;
    }

    public void setBarcodeType(String barcodeType) {
        this.barcodeType = barcodeType;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public long getNurseId() {
        return nurseId;
    }

    public void setNurseId(long nurseId) {
        this.nurseId = nurseId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getEvalDate() {
        return evalDate;
    }

    public void setEvalDate(long evalDate) {
        this.evalDate = evalDate;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public long getUploadId() {
        return uploadId;
    }

    public void setUploadId(long uploadId) {
        this.uploadId = uploadId;
    }

    public MeasurementDataModule(long evalDate) {
        this.evalDate = evalDate;
    }

    public MeasurementDataModule() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeString(this.caseId);
        dest.writeString(this.barcodeType);
        dest.writeLong(this.nurseId);
        dest.writeLong(this.ownerId);
        dest.writeLong(this.evalDate);
        dest.writeString(this.valueType);
        dest.writeString(this.value);
        dest.writeLong(this.uploadId);
    }

    protected MeasurementDataModule(Parcel in) {
        this._id = in.readLong();
        this.caseId = in.readString();
        this.barcodeType = in.readString();
        this.nurseId = in.readLong();
        this.ownerId = in.readLong();
        this.evalDate = in.readLong();
        this.valueType = in.readString();
        this.value = in.readString();
        this.uploadId = in.readLong();
    }

    public static final Creator<MeasurementDataModule> CREATOR = new Creator<MeasurementDataModule>() {
        @Override
        public MeasurementDataModule createFromParcel(Parcel source) {
            return new MeasurementDataModule(source);
        }

        @Override
        public MeasurementDataModule[] newArray(int size) {
            return new MeasurementDataModule[size];
        }
    };
}
