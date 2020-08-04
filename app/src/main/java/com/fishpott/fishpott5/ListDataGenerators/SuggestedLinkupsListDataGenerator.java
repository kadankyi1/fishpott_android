package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.SuggestedLinkUpsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 26/10/17.
 */

// THIS IS THE CLASS THAT IS RESPONSIBLE FOR MANIPULATING ALL SUGGESTED-LINKUPS DATA
public class SuggestedLinkupsListDataGenerator {
    // DECLARING THE DATA ARRAY LIST
    static List<SuggestedLinkUpsModel> allSuggestedLinkUpsData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllSuggestedLinkupsDatasAfresh(List<SuggestedLinkUpsModel> suggestedLinkUpsDataTemp) {
        SuggestedLinkupsListDataGenerator.allSuggestedLinkUpsData = suggestedLinkUpsDataTemp;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneSuggestedLinkupsData(SuggestedLinkUpsModel suggestedLinkUpsModel) {
        return allSuggestedLinkUpsData.add(suggestedLinkUpsModel);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<SuggestedLinkUpsModel> getAllSuggestedLinkUpsData() {
        return allSuggestedLinkUpsData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneSuggestedLinkupsDataToDesiredPosition(int i, SuggestedLinkUpsModel suggestedLinkUpsModel){
        allSuggestedLinkUpsData.add(i, suggestedLinkUpsModel);
    }
}
