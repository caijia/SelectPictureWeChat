package com.caijia.selectpicture.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.List;

public class MediaGroup implements Parcelable {

    private String groupName;
    private List<MediaBean> mediaList;
    private int mediaType;
    private boolean select;

    public MediaGroup(boolean isSelect,int mediaType,String groupName, List<MediaBean> mediaList) {
        this.select = isSelect;
        this.mediaType = mediaType;
        this.groupName = groupName;
        this.mediaList = mediaList;
    }

    public MediaGroup() {
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public
    @Nullable
    MediaBean getFirst() {
        return mediaList != null && !mediaList.isEmpty() ? mediaList.get(0) : null;
    }

    public int getSize() {
        return mediaList != null ? mediaList.size() : 0;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<MediaBean> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<MediaBean> mediaList) {
        this.mediaList = mediaList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.groupName);
        dest.writeTypedList(this.mediaList);
        dest.writeInt(this.mediaType);
        dest.writeByte(this.select ? (byte) 1 : (byte) 0);
    }

    protected MediaGroup(Parcel in) {
        this.groupName = in.readString();
        this.mediaList = in.createTypedArrayList(MediaBean.CREATOR);
        this.mediaType = in.readInt();
        this.select = in.readByte() != 0;
    }

    public static final Creator<MediaGroup> CREATOR = new Creator<MediaGroup>() {
        @Override
        public MediaGroup createFromParcel(Parcel source) {
            return new MediaGroup(source);
        }

        @Override
        public MediaGroup[] newArray(int size) {
            return new MediaGroup[size];
        }
    };
}
