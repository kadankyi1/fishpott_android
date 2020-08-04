package com.fishpott.fishpott5.Models;

import java.io.Serializable;

/**
 * Created by zatana on 7/17/19.
 */

public class Notification_Model implements Serializable {

    private static final long serialVersionUID = 1L;
    private long rowId;
    private int notificationType;
    private String relevantId_1;
    private String relevantId_2;
    private String relevantId_3;
    private int readStatus;
    private String pottPic;
    private String notificationMessage;
    private String notificationDate;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    public String getRelevantId_1() {
        return relevantId_1;
    }

    public void setRelevantId_1(String relevantId_1) {
        this.relevantId_1 = relevantId_1;
    }

    public String getRelevantId_2() {
        return relevantId_2;
    }

    public void setRelevantId_2(String relevantId_2) {
        this.relevantId_2 = relevantId_2;
    }

    public String getRelevantId_3() {
        return relevantId_3;
    }

    public void setRelevantId_3(String relevantId_3) {
        this.relevantId_3 = relevantId_3;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public String getPottPic() {
        return pottPic;
    }

    public void setPottPic(String pottPic) {
        this.pottPic = pottPic;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public String getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(String notificationDate) {
        this.notificationDate = notificationDate;
    }
}
