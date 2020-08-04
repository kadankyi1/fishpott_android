package com.fishpott.fishpott5.Models;

import java.io.Serializable;

/**
 * Created by zatana on 12/11/18.
 */

// THIS IS THE OBJECT THAT WILL USED FOR EACH LIST ITEM OF THE LIST OF SUGGESTED LINKUPS
public class SuggestedLinkUpsModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private String pottPictureLink;
    private String pottName;
    private String pottFullName;
    private int pottType;
    private int verifiedStatus;
    private String suggestionReason;
    private String investorId;

    public String getPottPictureLink() {
        return pottPictureLink;
    }

    public void setPottPictureLink(String pottPictureLink) {
        this.pottPictureLink = pottPictureLink;
    }

    public String getPottName() {
        return pottName;
    }

    public void setPottName(String pottName) {
        this.pottName = pottName;
    }

    public String getPottFullName() {
        return pottFullName;
    }

    public void setPottFullName(String pottFullName) {
        this.pottFullName = pottFullName;
    }

    public int getPottType() {
        return pottType;
    }

    public void setPottType(int pottType) {
        this.pottType = pottType;
    }

    public int getVerifiedStatus() {
        return verifiedStatus;
    }

    public void setVerifiedStatus(int verifiedStatus) {
        this.verifiedStatus = verifiedStatus;
    }

    public String getSuggestionReason() {
        return suggestionReason;
    }

    public void setSuggestionReason(String suggestionReason) {
        this.suggestionReason = suggestionReason;
    }

    public String getInvestorId() {
        return investorId;
    }

    public void setInvestorId(String investorId) {
        this.investorId = investorId;
    }
}
