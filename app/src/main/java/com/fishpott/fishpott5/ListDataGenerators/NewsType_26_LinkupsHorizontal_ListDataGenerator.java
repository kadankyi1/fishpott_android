package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.NewsType_26_Linkups_Horizontal_Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 5/4/19.
 */

public class NewsType_26_LinkupsHorizontal_ListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<NewsType_26_Linkups_Horizontal_Model> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllDatasAfresh(List<NewsType_26_Linkups_Horizontal_Model> newAllData) {
        NewsType_26_LinkupsHorizontal_ListDataGenerator.allData = newAllData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(NewsType_26_Linkups_Horizontal_Model newsType26Model) {
        return allData.add(newsType26Model);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<NewsType_26_Linkups_Horizontal_Model> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToDesiredPosition(int i, NewsType_26_Linkups_Horizontal_Model newsType26Model){
        allData.add(i, newsType26Model);
    }

}
