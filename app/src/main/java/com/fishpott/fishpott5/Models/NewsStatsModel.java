package com.fishpott.fishpott5.Models;

import java.io.Serializable;

/**
 * Created by zatana on 9/26/19.
 */

public class NewsStatsModel  implements Serializable {

    private static final long serialVersionUID = 1L;
    private int statsType; // 0 = Likes, 1 = Dislikes, 2 = Views, 3 = Purchases, 4= Comments
    private String newsId;
    private String reactionOrComment;
    private String date;
    private String senderPottName;
    private String senderPottPic;
    private int sendStatus;
    private Boolean sentLocally;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getStatsType() {
        return statsType;
    }

    public void setStatsType(int statsType) {
        this.statsType = statsType;
    }

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public String getReactionOrComment() {
        return reactionOrComment;
    }

    public void setReactionOrComment(String reactionOrComment) {
        this.reactionOrComment = reactionOrComment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSenderPottName() {
        return senderPottName;
    }

    public void setSenderPottName(String senderPottName) {
        this.senderPottName = senderPottName;
    }

    public String getSenderPottPic() {
        return senderPottPic;
    }

    public void setSenderPottPic(String senderPottPic) {
        this.senderPottPic = senderPottPic;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Boolean getSentLocally() {
        return sentLocally;
    }

    public void setSentLocally(Boolean sentLocally) {
        this.sentLocally = sentLocally;
    }
}
