package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.Vertical_NewsType_Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 11/1/19.
 */

public class FullNewsListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<Vertical_NewsType_Model> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllData(List<Vertical_NewsType_Model> theFullData) {
        Vertical_NewsType_ListDataGenerator.allData = theFullData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(Vertical_NewsType_Model theSingleData) {
        return allData.add(theSingleData);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<Vertical_NewsType_Model> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToSpecificPosition(int i, Vertical_NewsType_Model theSingleData){
        allData.add(i, theSingleData);
    }
}
