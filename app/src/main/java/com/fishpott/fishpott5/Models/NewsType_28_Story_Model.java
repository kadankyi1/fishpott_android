package com.fishpott.fishpott5.Models;


import java.io.Serializable;

/**
 * Created by zatana on 1/15/19.
 */

public class NewsType_28_Story_Model implements Serializable {

    private static final long serialVersionUID = 1L;
    private int storyType;
    private String storyNewsId;
    private String storyMakerPottId;
    private String storyMakerPottName;
    private String storyMakerPottPic;
    private String storyItemPic;
    private String storyItemVideo;
    private String storyItemPrice;
    private String storyItemParentID;
    private String storyItemName;
    private String storyItemQuantity;
    private String storyItemShareID;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getStoryType() {
        return storyType;
    }

    public void setStoryType(int storyType) {
        this.storyType = storyType;
    }

    public String getStoryNewsId() {
        return storyNewsId;
    }

    public void setStoryNewsId(String storyNewsId) {
        this.storyNewsId = storyNewsId;
    }

    public String getStoryMakerPottId() {
        return storyMakerPottId;
    }

    public void setStoryMakerPottId(String storyMakerPottId) {
        this.storyMakerPottId = storyMakerPottId;
    }

    public String getStoryMakerPottName() {
        return storyMakerPottName;
    }

    public void setStoryMakerPottName(String storyMakerPottName) {
        this.storyMakerPottName = storyMakerPottName;
    }

    public String getStoryMakerPottPic() {
        return storyMakerPottPic;
    }

    public void setStoryMakerPottPic(String storyMakerPottPic) {
        this.storyMakerPottPic = storyMakerPottPic;
    }

    public String getStoryItemPic() {
        return storyItemPic;
    }

    public void setStoryItemPic(String storyItemPic) {
        this.storyItemPic = storyItemPic;
    }

    public String getStoryItemVideo() {
        return storyItemVideo;
    }

    public void setStoryItemVideo(String storyItemVideo) {
        this.storyItemVideo = storyItemVideo;
    }

    public String getStoryItemPrice() {
        return storyItemPrice;
    }

    public void setStoryItemPrice(String storyItemPrice) {
        this.storyItemPrice = storyItemPrice;
    }

    public String getStoryItemParentID() {
        return storyItemParentID;
    }

    public void setStoryItemParentID(String storyItemParentID) {
        this.storyItemParentID = storyItemParentID;
    }

    public String getStoryItemName() {
        return storyItemName;
    }

    public void setStoryItemName(String storyItemName) {
        this.storyItemName = storyItemName;
    }

    public String getStoryItemQuantity() {
        return storyItemQuantity;
    }

    public void setStoryItemQuantity(String storyItemQuantity) {
        this.storyItemQuantity = storyItemQuantity;
    }

    public String getStoryItemShareID() {
        return storyItemShareID;
    }

    public void setStoryItemShareID(String storyItemShareID) {
        this.storyItemShareID = storyItemShareID;
    }
}
