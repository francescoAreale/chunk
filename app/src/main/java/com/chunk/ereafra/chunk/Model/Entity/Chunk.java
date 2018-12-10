package com.chunk.ereafra.chunk.Model.Entity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Chunk implements Parcelable {

    private String id;
    private String chunkName;
    private long timestamp;
    private double latitude;
    private double longitude;
    private String chatOfChunkID;
    private String imagePath;

    public Chunk(String id, String chunkName, long timestamp, double latitude, double longitude, String chatOfChunkID, String image) {
        this.id = id;
        this.chunkName = chunkName;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.chatOfChunkID = chatOfChunkID;
        this.imagePath = image;
    }

    public Chunk() {
    }

    public static final Creator<Chunk> CREATOR = new Creator<Chunk>() {
        @Override
        public Chunk createFromParcel(Parcel in) {
            return new Chunk(in);
        }

        @Override
        public Chunk[] newArray(int size) {
            return new Chunk[size];
        }
    };

    protected Chunk(Parcel in) {
        id = in.readString();
        chunkName = in.readString();
        timestamp = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
        chatOfChunkID = in.readString();
        imagePath = in.readString();
    }

    public String getImage() {
        return imagePath;
    }

    public void setImage(String image) {
        this.imagePath = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChunkName() {
        return chunkName;
    }

    public void setChunkName(String chunkName) {
        this.chunkName = chunkName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getChatOfChunkID() {
        return chatOfChunkID;
    }

    public void setChatOfChunkID(String chatOfChunkID) {
        this.chatOfChunkID = chatOfChunkID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(id);
        dest.writeString(chunkName);
        dest.writeLong(timestamp);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(chatOfChunkID);
        dest.writeString(imagePath);
    }
}
