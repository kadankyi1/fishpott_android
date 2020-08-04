package com.fishpott.fishpott5.Models;

/**
 * Created by zatana on 8/26/19.
 */

public class sellerModel {

    private static final long serialVersionUID = 1L;
    private long rowId;
    private String sellerPottName;
    private int sellerVerifiedStatus;
    private int sellerAccountType;
    private String sellerProfilePicture;
    private String offerPricePerShare;
    private String offerQuantity;
    private String shareId;
    private String newsId;


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public String getSellerPottName() {
        return sellerPottName;
    }

    public void setSellerPottName(String sellerPottName) {
        this.sellerPottName = sellerPottName;
    }

    public int getSellerVerifiedStatus() {
        return sellerVerifiedStatus;
    }

    public void setSellerVerifiedStatus(int sellerVerifiedStatus) {
        this.sellerVerifiedStatus = sellerVerifiedStatus;
    }

    public int getSellerAccountType() {
        return sellerAccountType;
    }

    public void setSellerAccountType(int sellerAccountType) {
        this.sellerAccountType = sellerAccountType;
    }

    public String getSellerProfilePicture() {
        return sellerProfilePicture;
    }

    public void setSellerProfilePicture(String sellerProfilePicture) {
        this.sellerProfilePicture = sellerProfilePicture;
    }

    public String getOfferPricePerShare() {
        return offerPricePerShare;
    }

    public void setOfferPricePerShare(String offerPricePerShare) {
        this.offerPricePerShare = offerPricePerShare;
    }

    public String getOfferQuantity() {
        return offerQuantity;
    }

    public void setOfferQuantity(String offerQuantity) {
        this.offerQuantity = offerQuantity;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }
}
