package com.fishpott.fishpott5.Models;

import java.io.Serializable;

/**
 * Created by zatana on 8/25/19.
 */

public class ShareHostedModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private long rowId;
    private String shareId;
    private String shareName;
    private String shareLogo;
    private String valuePerShare;
    private String dividendPerShare;
    private String companyName;
    private String companyPottName;
    private String shareInfo;


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getShareName() {
        return shareName;
    }

    public void setShareName(String shareName) {
        this.shareName = shareName;
    }

    public String getShareLogo() {
        return shareLogo;
    }

    public void setShareLogo(String shareLogo) {
        this.shareLogo = shareLogo;
    }

    public String getValuePerShare() {
        return valuePerShare;
    }

    public void setValuePerShare(String valuePerShare) {
        this.valuePerShare = valuePerShare;
    }

    public String getDividendPerShare() {
        return dividendPerShare;
    }

    public void setDividendPerShare(String dividendPerShare) {
        this.dividendPerShare = dividendPerShare;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyPottName() {
        return companyPottName;
    }

    public void setCompanyPottName(String companyPottName) {
        this.companyPottName = companyPottName;
    }

    public String getShareInfo() {
        return shareInfo;
    }

    public void setShareInfo(String shareInfo) {
        this.shareInfo = shareInfo;
    }
}
