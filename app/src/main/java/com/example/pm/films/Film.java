package com.example.pm.films;

import android.os.Parcel;
import android.os.Parcelable;

/** This class represents one film object
 *  Every film object has an id, title, description, urls to retrieve poster
 *  and video trailer and cast listing of max. 3 names
 * */

class Film implements Parcelable {

    private long mId;
    private String mTitle;
    private String mDescription;
    private String mPosterUrl;
    private String mVideoUrl;
    private String[] mCast;

    Film(long id, String title, String posterUrl, String description) {
        this.mId = id;
        this.mTitle = title;
        this.mPosterUrl = posterUrl;
        this.mDescription = description;
    }

    /** Parcel constructor */
    private Film(Parcel in){

        String[] data = new String[3];
        in.readStringArray(data);

        mId = in.readLong();
        mTitle = data[0];
        mDescription = data[1];
        mPosterUrl = data[2];
    }

    String getTitle() {
        return mTitle;
    }

    void setTitle(String title) {
        this.mTitle = title;
    }

    long getId() {
        return mId;
    }

    void setId(long id) {
        mId = id;
    }

    String getPosterUrl() {
        return mPosterUrl;
    }

    String getDescription() {
        return mDescription;
    }

    String getVideoUrl() {
        return mVideoUrl;
    }

    public String[] getCast() {
        return mCast;
    }

    void setVideoUrl(String videoUrl) {
        mVideoUrl = videoUrl;
    }

    void setCast(String[] cast) {
        mCast = cast;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeStringArray(new String[] {this.mTitle, this.mDescription, this.mPosterUrl});
        parcel.writeLong(this.mId);

    }

    public static final Creator<Film> CREATOR = new Creator<Film>() {

        @Override
        public Film createFromParcel(Parcel source) {
            return new Film(source);
        }

        @Override
        public Film[] newArray(int size) {
            return new Film[size];
        }
    };
}