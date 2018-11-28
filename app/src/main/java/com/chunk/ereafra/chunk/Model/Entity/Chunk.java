package com.chunk.ereafra.chunk.Model.Entity;

import android.net.Uri;

public class Chunk {

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
}
