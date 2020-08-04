package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.TransactionModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 9/22/19.
 */

public class TransactionsListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<TransactionModel> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllDatasAfresh(List<TransactionModel> newAllData) {
        allData = newAllData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(TransactionModel model) {
        return allData.add(model);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<TransactionModel> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToDesiredPosition(int i, TransactionModel model){
        allData.add(i, model);
    }
}
