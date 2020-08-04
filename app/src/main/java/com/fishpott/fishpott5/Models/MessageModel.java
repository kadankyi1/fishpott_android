package com.fishpott.fishpott5.Models;

import java.io.Serializable;

/**
 * Created by zatana on 9/10/19.
 */

public class MessageModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private long rowId;
    private long onlineSku;
    private String chatId;
    private String senderPottName;
    private String receiverPottName;
    private String messageText;
    private String messageImage;
    private String messageTime;
    private int messageStatus;
    private String messageId;


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public long getOnlineSku() {
        return onlineSku;
    }

    public void setOnlineSku(long onlineSku) {
        this.onlineSku = onlineSku;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderPottName() {
        return senderPottName;
    }

    public void setSenderPottName(String senderPottName) {
        this.senderPottName = senderPottName;
    }

    public String getReceiverPottName() {
        return receiverPottName;
    }

    public void setReceiverPottName(String receiverPottName) {
        this.receiverPottName = receiverPottName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageImage() {
        return messageImage;
    }

    public void setMessageImage(String messageImage) {
        this.messageImage = messageImage;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
