package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.StockProfileModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 8/31/19.
 */

public class StockProfileListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<StockProfileModel> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllDatasAfresh(List<StockProfileModel> newAllData) {
        allData = newAllData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(StockProfileModel model) {
        return allData.add(model);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<StockProfileModel> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToDesiredPosition(int i, StockProfileModel model){
        allData.add(i, model);
    }
}
