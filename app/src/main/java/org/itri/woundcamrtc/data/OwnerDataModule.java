package org.itri.woundcamrtc.data;

import android.os.Parcel;
import android.os.Parcelable;


public class OwnerDataModule implements Parcelable {


    private long _id;
    private long ownerId;
    private String roomName;
    private String ownerName;
    private String roleName;

    public OwnerDataModule() {
    }

    public OwnerDataModule(String roomName) {
        this.roomName = roomName;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeLong(this.ownerId);
        dest.writeString(this.roomName);
        dest.writeString(this.ownerName);
        dest.writeString(this.roleName);
    }

    protected OwnerDataModule(Parcel in) {
        this._id = in.readLong();
        this.ownerId = in.readLong();
        this.roomName = in.readString();
        this.ownerName = in.readString();
        this.roleName = in.readString();
    }

    public static final Creator<OwnerDataModule> CREATOR = new Creator<OwnerDataModule>() {
        @Override
        public OwnerDataModule createFromParcel(Parcel source) {
            return new OwnerDataModule(source);
        }

        @Override
        public OwnerDataModule[] newArray(int size) {
            return new OwnerDataModule[size];
        }
    };
}
