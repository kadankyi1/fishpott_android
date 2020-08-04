package com.fishpott.fishpott5.Models;

import java.io.Serializable;

/**
 * Created by zatana on 5/4/19.
 */

public class NewsType_26_Linkups_Horizontal_Model implements Serializable {

    private static final long serialVersionUID = 1L;
    private String linkupPottPic;
    private String linkupPottName;
    private String linkupFullName;
    private int verifiedStatus;
    private String linkupInfo;
    private String linkID;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getLinkupPottPic() {
        return linkupPottPic;
    }

    public void setLinkupPottPic(String linkupPottPic) {
        this.linkupPottPic = linkupPottPic;
    }

    public String getLinkupPottName() {
        return linkupPottName;
    }

    public void setLinkupPottName(String linkupPottName) {
        this.linkupPottName = linkupPottName;
    }

    public String getLinkupFullName() {
        return linkupFullName;
    }

    public void setLinkupFullName(String linkupFullName) {
        this.linkupFullName = linkupFullName;
    }

    public int getVerifiedStatus() {
        return verifiedStatus;
    }

    public void setVerifiedStatus(int verifiedStatus) {
        this.verifiedStatus = verifiedStatus;
    }

    public String getLinkupInfo() {
        return linkupInfo;
    }

    public void setLinkupInfo(String linkupInfo) {
        this.linkupInfo = linkupInfo;
    }

    public String getLinkID() {
        return linkID;
    }

    public void setLinkID(String linkID) {
        this.linkID = linkID;
    }
}
