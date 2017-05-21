package com.invisible.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ives.yeung on 2016/11/10.
 */

public class ImageInfo implements Parcelable, Serializable {

    /**
     * @Fields serialVersionUID : TODO
     */

    private static final long serialVersionUID = 1L;

    /**
     * 大图的url
     */
    private String bigImage;

    /**
     * 小图的url
     */
    private String smallImage;

    /**
     * 小图宽度
     */
    private int smallWidth;

    /**
     * 小图高度
     */
    private int smallHeight;

    public ImageInfo() {
        super();
    }

    public ImageInfo(String bigImage, String smallImage, int smallWidth, int smallHeight) {
        super();
        this.bigImage = bigImage;
        this.smallImage = smallImage;
        this.smallWidth = smallWidth;
        this.smallHeight = smallHeight;
    }

    public String getBigImage() {
        return bigImage;
    }

    public void setBigImage(String bigImage) {
        this.bigImage = bigImage;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }

    public int getSmallWidth() {
        return smallWidth;
    }

    public void setSmallWidth(int smallWidth) {
        this.smallWidth = smallWidth;
    }

    public int getSmallHeight() {
        return smallHeight;
    }

    public void setSmallHeight(int smallHeight) {
        this.smallHeight = smallHeight;
    }

    public static final Parcelable.Creator<ImageInfo> CREATOR = new Creator<ImageInfo>() {
        public ImageInfo createFromParcel(Parcel source) {
            ImageInfo image = new ImageInfo();
            image.bigImage = source.readString();
            image.smallImage = source.readString();
            image.smallWidth = source.readInt();
            image.smallHeight = source.readInt();
            return image;
        }

        public ImageInfo[] newArray(int size) {
            return new ImageInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bigImage);
        dest.writeString(smallImage);
        dest.writeInt(smallWidth);
        dest.writeInt(smallHeight);
    }

}

