package org.itri.woundcamrtc.data;

import android.os.Parcel;
import android.os.Parcelable;


public class WoundDataModule implements Parcelable {

    private long _id;
    private long nurseId;
    private String nurseName;
    private int demo;
    private int https;
    private String url;
    private String babyNurse;
    private String momNurse;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public long getNurseId() {
        return nurseId;
    }

    public void setNurseId(long nurseId) {
        this.nurseId = nurseId;
    }

    public String getNurseName() {
        return nurseName;
    }

    public void setNurseName(String nurseName) {
        this.nurseName = nurseName;
    }

    public int getDemoFlag() {
        return demo;
    }

    public void setDemoFlag(int demo) {
        this.demo = demo;
    }

    public int getNeedHTTPS() {
        return https;
    }

    public void setNeedHTTPS(int https) {
        this.https = https;
    }

    public String getWebviewUrl() {
        return url;
    }

    public void setWebviewUrl(String url) {
        this.url = url;
    }

    public String getBabyNurse() {
        return babyNurse;
    }

    public void setBabyNurse(String babyNurse) {
        this.babyNurse = babyNurse;
    }

    public String getMomNurse() {
        return momNurse;
    }

    public void setMomNurse(String momNurse) {
        this.momNurse = momNurse;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeLong(this.nurseId);
        dest.writeString(this.nurseName);
        dest.writeInt(this.demo);
        dest.writeInt(this.https);
        dest.writeString(this.url);
        dest.writeString(this.babyNurse);
        dest.writeString(this.momNurse);
    }

    public WoundDataModule() {
    }

    protected WoundDataModule(Parcel in) {
        this._id = in.readInt();
        this.nurseId = in.readLong();
        this.nurseName = in.readString();
        this.demo = in.readInt();
        this.https = in.readInt();
        this.url = in.readString();
        this.babyNurse = in.readString();
        this.momNurse = in.readString();
    }

    public static final Creator<WoundDataModule> CREATOR = new Creator<WoundDataModule>() {
        @Override
        public WoundDataModule createFromParcel(Parcel source) {
            return new WoundDataModule(source);
        }

        @Override
        public WoundDataModule[] newArray(int size) {
            return new WoundDataModule[size];
        }
    };
}

