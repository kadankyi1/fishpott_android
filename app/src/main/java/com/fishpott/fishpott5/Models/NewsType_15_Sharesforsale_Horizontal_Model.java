package com.fishpott.fishpott5.Models;

import java.io.Serializable;

/**
 * Created by zatana on 5/2/19.
 */

public class NewsType_15_Sharesforsale_Horizontal_Model  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String sellerPottPic;
    private String sellerPottName;
    private String stockPic;
    private String stockSellingPrice;
    private String stockQuantity;
    private String stockInfo;
    private String sharesNewsId;
    private String sharesParentId;
    private String shareItemName;
    private String shareID;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getSellerPottPic() {
        return sellerPottPic;
    }

    public void setSellerPottPic(String sellerPottPic) {
        this.sellerPottPic = sellerPottPic;
    }

    public String getSellerPottName() {
        return sellerPottName;
    }

    public void setSellerPottName(String sellerPottName) {
        this.sellerPottName = sellerPottName;
    }

    public String getStockPic() {
        return stockPic;
    }

    public void setStockPic(String stockPic) {
        this.stockPic = stockPic;
    }

    public String getStockSellingPrice() {
        return stockSellingPrice;
    }

    public void setStockSellingPrice(String stockSellingPrice) {
        this.stockSellingPrice = stockSellingPrice;
    }

    public String getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(String stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getStockInfo() {
        return stockInfo;
    }

    public void setStockInfo(String stockInfo) {
        this.stockInfo = stockInfo;
    }

    public String getSharesNewsId() {
        return sharesNewsId;
    }

    public void setSharesNewsId(String sharesNewsId) {
        this.sharesNewsId = sharesNewsId;
    }

    public String getSharesParentId() {
        return sharesParentId;
    }

    public void setSharesParentId(String sharesParentId) {
        this.sharesParentId = sharesParentId;
    }

    public String getShareItemName() {
        return shareItemName;
    }

    public void setShareItemName(String shareItemName) {
        this.shareItemName = shareItemName;
    }

    public String getShareID() {
        return shareID;
    }

    public void setShareID(String shareID) {
        this.shareID = shareID;
    }
}
