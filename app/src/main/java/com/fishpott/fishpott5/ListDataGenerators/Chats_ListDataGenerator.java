package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.Chat_Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 7/17/19.
 */

public class Chats_ListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<Chat_Model> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllDatasAfresh(List<Chat_Model> newAllData) {
        Chats_ListDataGenerator.allData = newAllData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(Chat_Model model) {
        return allData.add(model);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<Chat_Model> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToDesiredPosition(int i, Chat_Model model){
        allData.add(i, model);
    }

}
