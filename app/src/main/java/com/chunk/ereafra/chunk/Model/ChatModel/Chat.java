package com.chunk.ereafra.chunk.Model.ChatModel;

public class Chat {

    private String id;
    private String lastMessage;
    private long timestamp;
    private String TitleChat;
    private String urlImage;

    public Chat(String id, String lastMessage, long timestamp, String titleChat, String urlImage) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        TitleChat = titleChat;
        this.urlImage = urlImage;
    }

    public String getTitleChat() {
        return TitleChat;
    }

    public void setTitleChat(String titleChat) {
        TitleChat = titleChat;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public Chat() {
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
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
