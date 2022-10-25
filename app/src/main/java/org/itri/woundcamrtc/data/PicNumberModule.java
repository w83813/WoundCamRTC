package org.itri.woundcamrtc.data;

import android.os.Parcel;
import android.os.Parcelable;


public class PicNumberModule implements Parcelable {

    private long _id;
    private String evid;
    private String part;
    private String date;
    private int number;
    private String type;
    private String total;
    private String owner;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeString(this.evid);
        dest.writeString(this.part);
        dest.writeString(this.date);
        dest.writeInt(this.number);
        dest.writeString(this.type);
        dest.writeString(this.total);

    }

    public PicNumberModule() {
    }

    protected PicNumberModule(Parcel in) {
        this._id = in.readInt();
        this.evid = in.readString();
        this.part = in.readString();
        this.date = in.readString();
        this.number = in.readInt();
        this.type = in.readString();
        this.total = in.readString();

    }

    public static final Creator<PicNumberModule> CREATOR = new Creator<PicNumberModule>() {
        @Override
        public PicNumberModule createFromParcel(Parcel source) {
            return new PicNumberModule(source);
        }

        @Override
        public PicNumberModule[] newArray(int size) {
            return new PicNumberModule[size];
        }
    };

    public String getEvid() {
        return evid;
    }

    public void setEvid(String evid) {
        this.evid = evid;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}

