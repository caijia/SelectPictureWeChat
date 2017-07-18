package com.caijia.selectpicture.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by cai.jia on 2017/6/24 0024
 */

public class MediaBean implements Parcelable ,Comparable<MediaBean> {

    private long duration;
    private long size;
    private String fileName;
    private String path;
    private int mediaType;
    private boolean select;
    private long dateModified;

    public MediaBean() {
    }

    public MediaBean(int mediaType) {
        this.mediaType = mediaType;
    }

    public MediaBean(long dateModified,long duration, long size, String fileName, String path,
                     int mediaType) {
        this.dateModified = dateModified;
        this.duration = duration;
        this.size = size;
        this.fileName = fileName;
        this.path = path;
        this.mediaType = mediaType;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
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

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "MediaBean{" +
                "duration=" + duration +
                ", size=" + size +
                ", fileName='" + fileName + '\'' +
                ", path='" + path + '\'' +
                ", mediaType=" + mediaType +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.duration);
        dest.writeLong(this.size);
        dest.writeString(this.fileName);
        dest.writeString(this.path);
        dest.writeInt(this.mediaType);
        dest.writeByte(this.select ? (byte) 1 : (byte) 0);
        dest.writeLong(this.dateModified);
    }

    protected MediaBean(Parcel in) {
        this.duration = in.readLong();
        this.size = in.readLong();
        this.fileName = in.readString();
        this.path = in.readString();
        this.mediaType = in.readInt();
        this.select = in.readByte() != 0;
        this.dateModified = in.readLong();
    }

    public static final Creator<MediaBean> CREATOR = new Creator<MediaBean>() {
        @Override
        public MediaBean createFromParcel(Parcel source) {
            return new MediaBean(source);
        }

        @Override
        public MediaBean[] newArray(int size) {
            return new MediaBean[size];
        }
    };

    @Override
    public int compareTo(@NonNull MediaBean o) {
        return Float.compare(o.getDateModified(),this.getDateModified());
    }

}
