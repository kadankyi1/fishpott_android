package com.fishpott.fishpott5.Models;

import java.io.Serializable;

/**
 * Created by zatana on 5/5/19.
 */

public class NewsType_11_Upforsale_Horizontal_Model implements Serializable {

    private static final long serialVersionUID = 1L;
    private String sellerPottPic;
    private String itemPic;
    private String itemSellingPrice;
    private String itemInfoOrLocation;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getSellerPottPic() {
        return sellerPottPic;
    }

    public void setSellerPottPic(String sellerPottPic) {
        this.sellerPottPic = sellerPottPic;
    }

    public String getItemPic() {
        return itemPic;
    }

    public void setItemPic(String itemPic) {
        this.itemPic = itemPic;
    }

    public String getItemSellingPrice() {
        return itemSellingPrice;
    }

    public void setItemSellingPrice(String itemSellingPrice) {
        this.itemSellingPrice = itemSellingPrice;
    }

    public String getItemInfoOrLocation() {
        return itemInfoOrLocation;
    }

    public void setItemInfoOrLocation(String itemInfoOrLocation) {
        this.itemInfoOrLocation = itemInfoOrLocation;
    }
}
