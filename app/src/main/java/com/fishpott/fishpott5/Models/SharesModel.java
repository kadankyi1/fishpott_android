package com.fishpott.fishpott5.Models;

/**
 * Created by zatana on 8/2/19.
 */

public class SharesModel {

    private static final long serialVersionUID = 1L;
    private long rowId;
    private String sharesName;
    private String sharesId;
    private String sharesParentId;
    private String sharesAvailableQuantity;
    private String sharesCostPricePerShare;
    private String sharesMaxPricePerShare;
    private String sharesDividendPerShare;
    private String profitOrLoss;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public String getSharesName() {
        return sharesName;
    }

    public void setSharesName(String sharesName) {
        this.sharesName = sharesName;
    }

    public String getSharesId() {
        return sharesId;
    }

    public void setSharesId(String sharesId) {
        this.sharesId = sharesId;
    }

    public String getSharesParentId() {
        return sharesParentId;
    }

    public void setSharesParentId(String sharesParentId) {
        this.sharesParentId = sharesParentId;
    }

    public String getSharesAvailableQuantity() {
        return sharesAvailableQuantity;
    }

    public void setSharesAvailableQuantity(String sharesAvailableQuantity) {
        this.sharesAvailableQuantity = sharesAvailableQuantity;
    }

    public String getSharesCostPricePerShare() {
        return sharesCostPricePerShare;
    }

    public void setSharesCostPricePerShare(String sharesCostPricePerShare) {
        this.sharesCostPricePerShare = sharesCostPricePerShare;
    }

    public String getSharesMaxPricePerShare() {
        return sharesMaxPricePerShare;
    }

    public void setSharesMaxPricePerShare(String sharesMaxPricePerShare) {
        this.sharesMaxPricePerShare = sharesMaxPricePerShare;
    }

    public String getSharesDividendPerShare() {
        return sharesDividendPerShare;
    }

    public void setSharesDividendPerShare(String sharesDividendPerShare) {
        this.sharesDividendPerShare = sharesDividendPerShare;
    }

    public String getProfitOrLoss() {
        return profitOrLoss;
    }

    public void setProfitOrLoss(String profitOrLoss) {
        this.profitOrLoss = profitOrLoss;
    }
}
