package com.fishpott.fishpott5.ListDataGenerators;

import com.fishpott.fishpott5.Models.NewsType_28_Story_Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zatana on 1/15/19.
 */

public class NewsType28_Stories_ListDataGenerator {

    // DECLARING THE DATA ARRAY LIST
    static List<NewsType_28_Story_Model> allStoriesData = new ArrayList<>();

    // SETTING/RESETTING ALL SUGGESTED LINKUPS DATA
    public static void setAllStoriesDatasAfresh(List<NewsType_28_Story_Model> newAllStoriesData) {
        NewsType28_Stories_ListDataGenerator.allStoriesData = newAllStoriesData;
    }

    // ADDING ONE DATA TO ARRAY LIST
    public static boolean addOneStoryData(NewsType_28_Story_Model newsType28StoryModel) {
        return allStoriesData.add(newsType28StoryModel);
    }

    // GETTING ALL DATA AS ARRAY LIST
    public static List<NewsType_28_Story_Model> getAllStoriesData() {
        return allStoriesData;
    }

    // ADDING ONE DATA TO A DESIRED POSITION IN ARRAY LIST
    public static void addOneStoryDataToDesiredPosition(int i, NewsType_28_Story_Model newsType28StoryModel){
        allStoriesData.add(i, newsType28StoryModel);
    }
}
