package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.ShareHostedModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 8/25/19.
 */

public class ShareHostedListDataGenerators {

    // DECLARING THE DATA ARRAY LIST
    static List<ShareHostedModel> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllDatasAfresh(List<ShareHostedModel> newAllData) {
        ShareHostedListDataGenerators.allData = newAllData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(ShareHostedModel model) {
        return allData.add(model);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<ShareHostedModel> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToDesiredPosition(int i, ShareHostedModel model){
        allData.add(i, model);
    }

}
