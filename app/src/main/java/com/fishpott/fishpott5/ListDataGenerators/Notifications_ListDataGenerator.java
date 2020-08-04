package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.Notification_Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 7/17/19.
 */

public class Notifications_ListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<Notification_Model> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllDatasAfresh(List<Notification_Model> newAllData) {
        Notifications_ListDataGenerator.allData = newAllData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(Notification_Model model) {
        return allData.add(model);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<Notification_Model> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToDesiredPosition(int i, Notification_Model model){
        allData.add(i, model);
    }

}
