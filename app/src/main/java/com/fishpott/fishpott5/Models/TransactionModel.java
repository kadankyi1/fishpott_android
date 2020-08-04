package com.fishpott.fishpott5.Models;

import java.io.Serializable;

/**
 * Created by zatana on 9/22/19.
 */

public class TransactionModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private long rowId;
    private String type;
    private String date;
    private String quantityOrAmount;
    private String itemNameOrReceiveNumberOrCreditType;
    private String statusOrBuyerName;
    private String totalCharge;
    private String addedPottName;
    private String addedInfo1;
    private int statusNumber;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getQuantityOrAmount() {
        return quantityOrAmount;
    }

    public void setQuantityOrAmount(String quantityOrAmount) {
        this.quantityOrAmount = quantityOrAmount;
    }

    public String getItemNameOrReceiveNumberOrCreditType() {
        return itemNameOrReceiveNumberOrCreditType;
    }

    public void setItemNameOrReceiveNumberOrCreditType(String itemNameOrReceiveNumberOrCreditType) {
        this.itemNameOrReceiveNumberOrCreditType = itemNameOrReceiveNumberOrCreditType;
    }

    public String getStatusOrBuyerName() {
        return statusOrBuyerName;
    }

    public void setStatusOrBuyerName(String statusOrBuyerName) {
        this.statusOrBuyerName = statusOrBuyerName;
    }

    public String getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(String totalCharge) {
        this.totalCharge = totalCharge;
    }

    public String getAddedPottName() {
        return addedPottName;
    }

    public void setAddedPottName(String addedPottName) {
        this.addedPottName = addedPottName;
    }

    public String getAddedInfo1() {
        return addedInfo1;
    }

    public void setAddedInfo1(String addedInfo1) {
        this.addedInfo1 = addedInfo1;
    }

    public int getStatusNumber() {
        return statusNumber;
    }

    public void setStatusNumber(int statusNumber) {
        this.statusNumber = statusNumber;
    }
}
