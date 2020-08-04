package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.SharesModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 9/22/19.
 */

public class MySharesViewingListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<SharesModel> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllDatasAfresh(List<SharesModel> newAllData) {
        MyShares_ListDataGenerator.allData = newAllData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(SharesModel model) {
        return allData.add(model);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<SharesModel> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToDesiredPosition(int i, SharesModel model){
        allData.add(i, model);
    }

}
