package com.chunk.ereafra.chunk;

public class Chat {

    private String id;
    private String lastMessage;
    private long timestamp;


    public Chat() {
    }

    public Chat(String lastMessage, long timestamp) {
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public Chat(String id, String lastMessage, long timestamp) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
