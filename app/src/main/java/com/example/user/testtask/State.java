package com.example.user.testtask;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.*;
import java.util.Calendar;

/**
 * Created by User on 03.11.2016.
 */

public class State implements Cloneable, Parcelable {
    int pointId;
    String name;
    double latitude;
    double longitude;
    Calendar lastVisited;
    int defaultImage;
    int defaultImageId;
    ArrayList<ImageRow> images;

    int oldPointId;
    String oldName;
    double oldLatitude;
    double oldLongitude;
    Calendar oldLastVisited;
    int oldDefaultImage;
    int oldDefaultImageId;
    ArrayList<ImageRow> deletedImages;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(pointId);
        out.writeString(name);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeSerializable(lastVisited);
        out.writeInt(defaultImage);
        out.writeInt(defaultImageId);
        int count = images.size();
        out.writeInt(count);
        for (ImageRow im : images) {
            out.writeInt(im.id);
            out.writeString(im.url);
        }

        out.writeInt(oldPointId);
        out.writeString(oldName);
        out.writeDouble(oldLatitude);
        out.writeDouble(oldLongitude);
        out.writeSerializable(oldLastVisited);
        out.writeInt(oldDefaultImage);
        out.writeInt(oldDefaultImageId);
        count = deletedImages.size();
        out.writeInt(count);
        for (ImageRow im : deletedImages) {
            out.writeInt(im.id);
            out.writeString(im.url);
        }
    }

    public static final Creator<State> CREATOR = new Creator<State>() {
        @Override
        public State createFromParcel(Parcel in) {
            State rv = new State();
            rv.pointId = in.readInt();
            rv.name = in.readString();
            rv.latitude = in.readDouble();
            rv.longitude = in.readDouble();
            rv.lastVisited = (Calendar)in.readSerializable();
            rv.defaultImage = in.readInt();
            rv.defaultImageId = in.readInt();
            int count = in.readInt();
            rv.images = new ArrayList<>(count);
            for (int i = 0; i < count; ++i) {
                ImageRow im = new ImageRow();
                im.id = in.readInt();
                im.url = in.readString();
                rv.images.add(im);
            }

            rv.oldPointId = in.readInt();
            rv.oldName = in.readString();
            rv.oldLatitude = in.readDouble();
            rv.oldLongitude = in.readDouble();
            rv.oldLastVisited = (Calendar)in.readSerializable();
            rv.oldDefaultImage = in.readInt();
            rv.oldDefaultImageId = in.readInt();
            count = in.readInt();
            rv.deletedImages = new ArrayList<>(count);
            for (int i = 0; i < count; ++i) {
                ImageRow im = new ImageRow();
                im.id = in.readInt();
                im.url = in.readString();
                rv.deletedImages.add(im);
            }
            return rv;
        }

        @Override
        public State[] newArray(int size) {
            return new State[size];
        }
    };
}

class ImageRow {
    int id;
    String url;
}
