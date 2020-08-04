package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.NewsStatsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 9/26/19.
 */

public class NewsStatsListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<NewsStatsModel> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllDatasAfresh(List<NewsStatsModel> newAllData) {
        allData = newAllData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(NewsStatsModel model) {
        return allData.add(model);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<NewsStatsModel> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToDesiredPosition(int i, NewsStatsModel model){
        allData.add(i, model);
    }
}
