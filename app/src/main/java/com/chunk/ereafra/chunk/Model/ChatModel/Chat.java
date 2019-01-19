package com.chunk.ereafra.chunk.Model.ChatModel;

import android.os.Parcel;
import android.os.Parcelable;

public class Chat implements Parcelable {

    private String id;
    private String lastMessage;
    private long timestamp;
    private String titleChat;
    private String urlImage;

    public Chat(String id, String lastMessage, long timestamp, String titleChat, String urlImage) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.titleChat = titleChat;
        this.urlImage = urlImage;
    }

    protected Chat(Parcel in) {
        id = in.readString();
        lastMessage = in.readString();
        timestamp = in.readLong();
        titleChat = in.readString();
        urlImage = in.readString();
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public String getTitleChat() {
        return titleChat;
    }

    public void setTitleChat(String titleChat) {
        this.titleChat = titleChat;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public Chat() {
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(lastMessage);
        dest.writeLong(timestamp);
        dest.writeString(titleChat);
        dest.writeString(urlImage);
    }
}
