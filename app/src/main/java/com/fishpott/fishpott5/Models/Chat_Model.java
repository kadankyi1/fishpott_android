package com.fishpott.fishpott5.Models;

import java.io.Serializable;

/**
 * Created by zatana on 7/17/19.
 */

public class Chat_Model implements Serializable {

    private static final long serialVersionUID = 1L;
    private long rowId;
    private String chatId;
    private String pottPic;
    private String pottName;
    private String fullName;
    private String lastChatDate;
    private String lastChatMessage;
    private int readStatus;


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getPottPic() {
        return pottPic;
    }

    public void setPottPic(String pottPic) {
        this.pottPic = pottPic;
    }

    public String getPottName() {
        return pottName;
    }

    public void setPottName(String pottName) {
        this.pottName = pottName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLastChatDate() {
        return lastChatDate;
    }

    public void setLastChatDate(String lastChatDate) {
        this.lastChatDate = lastChatDate;
    }

    public String getLastChatMessage() {
        return lastChatMessage;
    }

    public void setLastChatMessage(String lastChatMessage) {
        this.lastChatMessage = lastChatMessage;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }
}
