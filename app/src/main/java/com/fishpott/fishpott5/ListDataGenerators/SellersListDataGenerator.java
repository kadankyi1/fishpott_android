package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.sellerModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 8/26/19.
 */

public class SellersListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<sellerModel> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllDatasAfresh(List<sellerModel> newAllData) {
        SellersListDataGenerator.allData = newAllData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(sellerModel model) {
        return allData.add(model);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<sellerModel> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToDesiredPosition(int i, sellerModel model){
        allData.add(i, model);
    }

}
