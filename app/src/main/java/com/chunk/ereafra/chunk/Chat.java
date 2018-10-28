package com.chunk.ereafra.chunk;

public class Chat {

    private String id;
    private String title;
    private MessageChat lastMessage;
    private String timestamp;
    private String latitude;
    private String longitude;


    public Chat() {
    }

    public Chat(String title, MessageChat lastMessage, String timestamp, String latitude, String longitude) {
        this.title = title;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Chat(String id, String title, MessageChat lastMessage, String timestamp, String latitude, String longitude) {
        this.id = id;
        this.title = title;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MessageChat getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageChat lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
