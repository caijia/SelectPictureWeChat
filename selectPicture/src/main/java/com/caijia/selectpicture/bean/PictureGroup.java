package com.caijia.selectpicture.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class PictureGroup implements Parcelable {

    /**
     * 包含图片的文件夹名称
     */
    private String groupName;

    /**
     * 文件夹下面的所有文件的路径
     */
    private List<String> picturePaths;

    public String getFirstPicture() {
        if (picturePaths != null && !picturePaths.isEmpty()) {
            return picturePaths.get(0);

        }else{
            return "";
        }
    }

    public int getImageCount() {
        return picturePaths != null ? picturePaths.size() : 0;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getPicturePaths() {
        return picturePaths;
    }

    public void setPicturePaths(List<String> picturePaths) {
        this.picturePaths = picturePaths;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.groupName);
        dest.writeStringList(this.picturePaths);
    }

    public PictureGroup() {
    }

    protected PictureGroup(Parcel in) {
        this.groupName = in.readString();
        this.picturePaths = in.createStringArrayList();
    }

    public static final Creator<PictureGroup> CREATOR = new Creator<PictureGroup>() {
        public PictureGroup createFromParcel(Parcel source) {
            return new PictureGroup(source);
        }

        public PictureGroup[] newArray(int size) {
            return new PictureGroup[size];
        }
    };
}
