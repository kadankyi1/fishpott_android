package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.MessageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 9/10/19.
 */

public class ConversationMessages_ListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<MessageModel> allData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllDatasAfresh(List<MessageModel> newAllData) {
        allData = newAllData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneData(MessageModel model) {
        return allData.add(model);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<MessageModel> getAllData() {
        return allData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneDataToDesiredPosition(int i, MessageModel model){
        allData.add(i, model);
    }
}
