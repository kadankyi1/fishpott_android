package com.fishpott.fishpott5.Services;


import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.fishpott.fishpott5.Activities.MainActivity;
import com.fishpott.fishpott5.Activities.MessengerActivity;
import com.fishpott.fishpott5.Adapters.ChatMessages_DatabaseAdapter;
import com.fishpott.fishpott5.Adapters.Notifications_DatabaseAdapter;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.Notifications_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.Notification_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

import static com.fishpott.fishpott5.Activities.MainActivity.mMessengerMenuIconUpdateIconConstraintLayout;
import static com.fishpott.fishpott5.Activities.MainActivity.mNotificationMenuIconUpdateIconConstraintLayout;
import static com.fishpott.fishpott5.Fragments.NotificationsFragment.mNotificationsRecyclerView;

/**
 * Created by zatana on 2/20/2017.
 */

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService  {

    public MyFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e("NotChatFCM", "=======================================================");
        Log.e("NotChatFCM", "HERE 1");
        if(Config.userIsLoggedIn2(getApplicationContext())){
            Log.e("NotChatFCM", "HERE 2");
            if (!remoteMessage.getData().isEmpty()) {

                Map<String, String> data = remoteMessage.getData();
                String not_type = data.get("not_type");
                String not_title = data.get("not_title");
                String not_message = data.get("not_message");
                String not_message_text = data.get("not_message_text");
                String not_message_info1 = data.get("not_message_info1");
                String not_message_info2 = data.get("not_message_info2");
                String not_pott_name = data.get("not_message_info2");
                String not_id_3 = data.get("not_message_info2");
                String not_pott_or_newsid = data.get("not_message_info2");
                String not_message_image = data.get("not_message_image");
                String not_pic = data.get("not_message_image");
                String not_message_video = data.get("not_message_video");
                String not_time = data.get("not_time");
                int notType = Config.getNotificationType(not_type);


                Log.e("NotChatFCM", "not_type : " + not_type
                        +  " -- not_title : " + not_title
                        +  " -- not_message : " + not_message
                        +  " -- not_message_text : " + not_message_text
                        +  " -- not_message_info1 : " + not_message_info1
                        +  " -- not_message_info2 : " + not_message_info2
                        +  " -- not_message_image : " + not_message_image
                        +  " -- not_message_video : " + not_message_video
                        +  " -- not_time : " + not_time
                );

                int countForNotifications = Config.getSharedPreferenceInt(getApplicationContext(), Config.NOTIFICATION_UNREAD_COUNT);
                int countForChats = Config.getSharedPreferenceInt(getApplicationContext(), Config.CHAT_NOTIFICATION_UNREAD_COUNT);


                Log.e("NotChatFCM", "HERE 3");

                if(not_type.trim().equalsIgnoreCase("chat")){

                    Log.e("NotChatFCM", "CHAT NOTIFICATION DETECTED");
                    long onlineSku = 0;
                    ChatMessages_DatabaseAdapter chatMessagesDatabaseAdapter = new ChatMessages_DatabaseAdapter(getApplicationContext());
                    // OPENING THE STORIES DATABASE
                    chatMessagesDatabaseAdapter.openDatabase();

                    Cursor cursor = chatMessagesDatabaseAdapter.getSpecificRowsUsingWhereValueAsString(ChatMessages_DatabaseAdapter.KEY_CHAT_ID + " = '" + not_pott_or_newsid + "'", ChatMessages_DatabaseAdapter.KEY_CHAT_ID + " DESC", true);
                    if(cursor != null && cursor.getCount() > 0){
                        onlineSku = cursor.getLong(chatMessagesDatabaseAdapter.COL_MESSAGE_ONLINE_SKU);
                        Log.e("NotChatFCM", "onlineSku DESC : " + onlineSku);
                    } else {
                        Log.e("NotChatFCM", "CURSOR IS NULL");
                    }

                    cursor.close();
                    chatMessagesDatabaseAdapter.closeDatabase();
                    countForChats  = countForChats + 1;

                    if(MyLifecycleHandler.isApplicationVisible()){
                        Log.e("NotChatFCM", "HERE 1");

                        //update notification bar
                        if(MyLifecycleHandler.getCurrentActivity() != null){
                            Log.e("NotChatFCM", "HERE 2");
                            if(MyLifecycleHandler.getCurrentActivity().getClass().getSimpleName().equalsIgnoreCase(MainActivity.class.getSimpleName())){
                                final int finalCountForChats = countForChats;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {

                                        Config.setSharedPreferenceInt(getApplicationContext(), Config.CHAT_NOTIFICATION_UNREAD_COUNT, finalCountForChats + 1);
                                        mMessengerMenuIconUpdateIconConstraintLayout.setVisibility(View.VISIBLE);
                                        mMessengerMenuIconUpdateIconConstraintLayout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.main_activity_onclick_icon_anim));
                                        if(mNotificationsRecyclerView != null){
                                            mNotificationsRecyclerView.getAdapter().notifyDataSetChanged();
                                            Log.e("NotChatFCM", "HERE 3+");
                                        }
                                    }
                                });

                                Config.getNewMessages(getApplicationContext(), Config.UNREAD, not_pott_or_newsid, not_pic,  not_pott_name, String.valueOf(onlineSku), LocaleHelper.getLanguage(getApplicationContext()));
                                Log.e("NotChatFCM", "HERE 3");
                            } else if(MyLifecycleHandler.getCurrentActivity().getClass().getSimpleName().equalsIgnoreCase(MessengerActivity.class.getSimpleName()) && MessengerActivity.chatID.equalsIgnoreCase(not_pott_or_newsid)){

                                Log.e("NotChatFCM", "HERE 4");
                            } else {
                                final int finalCountForChats = countForChats;

                                Config.setSharedPreferenceInt(getApplicationContext(), Config.CHAT_NOTIFICATION_UNREAD_COUNT, countForChats);
                                Config.setUserNotification(getApplicationContext(), String.valueOf(notType), not_title, not_message, countForChats, R.drawable.notification_icon);
                                Log.e("NotChatFCM", "HERE 5");
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {

                                        Config.setSharedPreferenceInt(getApplicationContext(), Config.CHAT_NOTIFICATION_UNREAD_COUNT, finalCountForChats + 1);
                                        mMessengerMenuIconUpdateIconConstraintLayout.setVisibility(View.VISIBLE);
                                        mMessengerMenuIconUpdateIconConstraintLayout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.main_activity_onclick_icon_anim));
                                        if(mNotificationsRecyclerView != null){
                                            mNotificationsRecyclerView.getAdapter().notifyDataSetChanged();
                                            Log.e("NotChatFCM", "HERE 3+");
                                        }
                                    }
                                });
                                Config.getNewMessages(getApplicationContext(), Config.UNREAD, not_pott_or_newsid, not_pic,  not_pott_name, String.valueOf(onlineSku), LocaleHelper.getLanguage(getApplicationContext()));

                            }
                        }
                    } else {
                        int badgeCountForNotifications = countForNotifications + countForChats;
                        //update app icon badge
                        Config.setSharedPreferenceInt(getApplicationContext(), Config.CHAT_NOTIFICATION_UNREAD_COUNT, countForChats);

                        Config.setUserNotification(getApplicationContext(), String.valueOf(notType), not_title, not_message, countForChats, R.drawable.notification_icon);
                        ShortcutBadger.applyCount(getApplicationContext(), badgeCountForNotifications);
                        Config.getNewMessages(getApplicationContext(), Config.UNREAD, not_pott_or_newsid, not_pic,  not_pott_name, String.valueOf(onlineSku), LocaleHelper.getLanguage(getApplicationContext()));
                        Log.e("NotChatFCM", "HERE 6");
                    }
                    //sendChatNotification(title, message, chat_table, receiver_fullname, receiver_pottname, receiver_verified_tag, receiver_profile_picture, chat_date);

                } else {
                    countForNotifications++;
                    int badgeCountForNotifications = countForNotifications + countForChats;
                    Config.setSharedPreferenceInt(getApplicationContext(), Config.NOTIFICATION_UNREAD_COUNT, countForNotifications);


                    Notifications_DatabaseAdapter notifications_databaseAdapter = new Notifications_DatabaseAdapter(getApplicationContext());
                    // OPENING THE STORIES DATABASE
                    notifications_databaseAdapter.openDatabase();
                    long rowId = notifications_databaseAdapter.insertRow(notType, not_pott_or_newsid, not_pott_name, 0, not_pic, not_message, not_time, not_id_3);
                    notifications_databaseAdapter.closeDatabase();

                    Notification_Model notification_model = new Notification_Model();
                    notification_model.setRowId(rowId);
                    notification_model.setNotificationType(notType);
                    notification_model.setRelevantId_1(not_pott_or_newsid);
                    notification_model.setRelevantId_2(not_pott_name);
                    notification_model.setRelevantId_3(not_id_3);
                    notification_model.setReadStatus(0);
                    notification_model.setPottPic(not_pic);
                    notification_model.setNotificationMessage(not_message);
                    notification_model.setNotificationDate(not_time);

                    //ADDING STORY OBJECT TO LIST
                    Notifications_ListDataGenerator.addOneDataToDesiredPosition(0, notification_model);

                    Log.e("NotChatFCM", "HERE 4");
                    /*
                    if(MyLifecycleHandler.isApplicationVisible()){
                        Log.e("NotChatFCM", "HERE 5");
                        //update notification bar
                        if(MyLifecycleHandler.getCurrentActivity() != null){
                            Log.e("NotChatFCM", "HERE 6");
                            if(MyLifecycleHandler.getCurrentActivity().getClass().getSimpleName().equalsIgnoreCase(MainActivity.class.getSimpleName())){
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mNotificationMenuIconUpdateIconConstraintLayout.setVisibility(View.VISIBLE);
                                        mNotificationMenuIconUpdateIconConstraintLayout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.main_activity_onclick_icon_anim));
                                        if(mNotificationsRecyclerView != null){
                                            mNotificationsRecyclerView.getAdapter().notifyDataSetChanged();
                                            Log.e("NotChatFCM", "HERE 7==1");
                                        }
                                    }
                                });
                                Log.e("NotChatFCM", "HERE 7");
                            } else {
                                Log.e("NotChatFCM", "HERE 8");
                                if(notType != Config.NOTICATION_RELATING_SHARESFORSALE){
                                    notType = Config.NOTICATION_RELATING_JUST_INFO;
                                    Log.e("NotChatFCM", "HERE 9");
                                }
                                Config.setUserNotification(getApplicationContext(), String.valueOf(notType), not_title, not_message, countForNotifications, R.drawable.notification_icon);
                                Log.e("NotChatFCM", "HERE 10");
                            }
                        }
                    } else {
                        //update app icon badge
                        Log.e("NotChatFCM", "ACTIVITY NOT  VISIBLE");
                        if(notType != Config.NOTICATION_RELATING_SHARESFORSALE){
                            notType = Config.NOTICATION_RELATING_JUST_INFO;
                            Log.e("NotChatFCM", "HERE 11");
                        }
                        Config.setUserNotification(getApplicationContext(), String.valueOf(notType), not_title, not_message, countForNotifications, R.drawable.notification_icon);
                        ShortcutBadger.applyCount(getApplicationContext(), badgeCountForNotifications);
                        Log.e("NotChatFCM", "HERE 12");
                    }
                    */

                    //update app icon badge
                    Log.e("NotChatFCM", "ACTIVITY NOT  VISIBLE");
                    if(notType != Config.NOTICATION_RELATING_SHARESFORSALE){
                        notType = Config.NOTICATION_RELATING_JUST_INFO;
                        Log.e("NotChatFCM", "HERE 11");
                    }
                    Config.setUserNotification(getApplicationContext(), String.valueOf(notType), not_title, not_message, countForNotifications, R.drawable.notification_icon);
                    ShortcutBadger.applyCount(getApplicationContext(), badgeCountForNotifications);
                    Log.e("NotChatFCM", "HERE 12");
                }
            }
        }

    }

    @Override
    public void onDeletedMessages() {

    }



}
