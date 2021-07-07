package com.fishpott.fishpott5.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fishpott.fishpott5.Activities.ProfileOfDifferentPottActivity;
import com.fishpott.fishpott5.Activities.StockProfileActivity;
import com.fishpott.fishpott5.Adapters.Notifications_DatabaseAdapter;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.Notifications_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.Models.Notification_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Views.CircleImageView;

import me.leolin.shortcutbadger.ShortcutBadger;

public class NotificationsFragment extends Fragment {

    public static RecyclerView mNotificationsRecyclerView;
    private Thread imageLoaderThread = null, newsFetchThread = null;
    private LinearLayoutManager mLayoutManager;
    private View view;

    public NotificationsFragment() {}


    public static NotificationsFragment newInstance() {
        NotificationsFragment fragment = new NotificationsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.fragment_notifications, container, false);

        //PREPARING NOTIFICATIONS

        newsFetchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                getOldNotifications();
            }
        });
        newsFetchThread.start();
        // CASTING OBJECTS TO VIEWS
        mNotificationsRecyclerView = (RecyclerView) view.findViewById(R.id.activity_main_notifications_fragment_recyclerview);
        mLayoutManager = new LinearLayoutManager(getActivity());


        // SETTING RECYCLER VIEW PROPERTIES
        mNotificationsRecyclerView.setItemViewCacheSize(20);
        mNotificationsRecyclerView.setDrawingCacheEnabled(true);
        mNotificationsRecyclerView.setHasFixedSize(true);
        mNotificationsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mNotificationsRecyclerView.setLayoutManager(mLayoutManager);
        mNotificationsRecyclerView.setAdapter(new RecyclerViewAdapter());

        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.CHAT_NOTIFICATION_UNREAD_COUNT, 0);
        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.NOTIFICATION_UNREAD_COUNT, 0);
        ShortcutBadger.removeCount(getActivity().getApplicationContext());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        mNotificationsRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        if(newsFetchThread != null){
            newsFetchThread.interrupt();
            newsFetchThread = null;
        }
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_notification_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }


    public void getOldNotifications() {
        // populate the message from the cursor
        if (getActivity() != null) {

            //CREATING THE NEWS STORIES DATABASE OBJECT
            Notifications_DatabaseAdapter notifications_databaseAdapter = new Notifications_DatabaseAdapter(getActivity().getApplication());
            // OPENING THE STORIES DATABASE
            notifications_databaseAdapter.openDatabase();
            //GETTING ALL NEWS IN LOCAL DATABASE AND POINTING ONE-BY-ONE WITH CURSOR
            Cursor cursor = notifications_databaseAdapter.getAllRows();
            if (cursor.moveToLast()) {
                if(Notifications_ListDataGenerator.getAllData().size() > 0){
                    Notifications_ListDataGenerator.getAllData().clear();
                }
                do {
                    // Process the data:
                    Notification_Model notification_model = new Notification_Model();
                    notification_model.setRowId(cursor.getLong(notifications_databaseAdapter.COL_ROWID));
                    notification_model.setNotificationType(cursor.getInt(notifications_databaseAdapter.COL_NOTIFICATION_TYPE));
                    notification_model.setRelevantId_1(cursor.getString(notifications_databaseAdapter.COL_RELEVANT_ID_1));
                    notification_model.setRelevantId_2(cursor.getString(notifications_databaseAdapter.COL_RELEVANT_ID_2));
                    notification_model.setReadStatus(cursor.getInt(notifications_databaseAdapter.COL_READ_STATUS));
                    notification_model.setPottPic(cursor.getString(notifications_databaseAdapter.COL_POTT_PIC));
                    notification_model.setNotificationMessage(cursor.getString(notifications_databaseAdapter.COL_NOTIFICATION_MESSAGE));
                    notification_model.setNotificationDate(cursor.getString(notifications_databaseAdapter.COL_NOTIFICATION_DATE));
                    notification_model.setRelevantId_3(cursor.getString(notifications_databaseAdapter.COL_RELEVANT_ID_3));
                    //ADDING STORY OBJECT TO LIST
                    Notifications_ListDataGenerator.addOneData(notification_model);

                } while (cursor.moveToPrevious());
            }

            cursor.close();
            notifications_databaseAdapter.closeDatabase();

        }
    }


    // THE ADAPTER FOR THE RECYCLER-VIEW TO SPECIFY HOW THE ITEMS ON THE RECYCLERVIEW WILL BEHAVE

    private class RecyclerViewAdapter extends RecyclerView.Adapter{

        //DEFINING A CLICK LISTENER FOR THE RECYCLERVIEW

        @Override
        public int getItemViewType(int position) {
            return Notifications_ListDataGenerator.getAllData().get(position).getNotificationType();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v;
            if(viewType == Config.NOTICATION_RELATING_COMMENT){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notify_comment_info_notification_fragment, parent, false);
            } else if(viewType == Config.NOTICATION_RELATING_DISLIKE){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notify_dislike_info_notification_fragment, parent, false);
            } else if(viewType == Config.NOTICATION_RELATING_LIKE){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notify_like_info_notification_fragment, parent, false);
            } else if(viewType == Config.NOTICATION_RELATING_LINKUP){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notify_linkup_info_notification_fragment, parent, false);
            } else if(viewType == Config.NOTICATION_RELATING_POACH){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notify_poach_info_notification_fragment, parent, false);
            } else if(viewType == Config.NOTICATION_RELATING_REPOST){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notify_repost_info_notification_fragment, parent, false);
            } else if(viewType == Config.NOTICATION_RELATING_SHARESFORSALE || viewType == Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER || viewType == Config.NOTICATION_RELATING_SHARES_SUGGESTION){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notify_sharesforsale_info_notification_fragment, parent, false);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notify_justinfo_info_notification_fragment, parent, false);
            }
            vh = new NotificationViewHolder(v);

            return vh;
        }


        public class NotificationViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout mNotificationListRootView;
            private CircleImageView mNotificationPic;
            private TextView mNotificationMsgTextView, mNotificationDateTextView;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public NotificationViewHolder(View v) {
                super(v);
                mNotificationListRootView = (ConstraintLayout) itemView.findViewById(R.id.notification_list_root_view_constraintlayout);
                mNotificationPic = (CircleImageView) itemView.findViewById(R.id.notification_pott_picture_circleimageview);
                mNotificationMsgTextView = (TextView) itemView.findViewById(R.id.notification_infotext_textview);
                mNotificationDateTextView = (TextView) itemView.findViewById(R.id.notification_date_textview);

                // ALL ON-CLICK LISTENERS
                mNotificationListRootView.setOnClickListener(innerClickListener);
                mNotificationMsgTextView.setOnClickListener(innerClickListener);
                mNotificationPic.setOnClickListener(innerClickListener);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            ((NotificationViewHolder) holder).mNotificationMsgTextView.setText(Notifications_ListDataGenerator.getAllData().get(position).getNotificationMessage());
            Config.linkifyAllMentions(getActivity().getApplicationContext(), ((NotificationViewHolder) holder).mNotificationMsgTextView);
            //Linkify.addLinks(((NotificationViewHolder) holder).mNotificationMsgTextView, Pattern.compile("@([A-Za-z0-9_]+)"), "com.my.package.tel:");
            if(Notifications_ListDataGenerator.getAllData().get(position).getReadStatus() == 0){
                ((NotificationViewHolder) holder).mNotificationListRootView.setBackground(getResources().getDrawable(R.drawable.grey_background_pressed_darker, null));
            } else {
                ((NotificationViewHolder) holder).mNotificationListRootView.setBackground(getResources().getDrawable(R.drawable.white_background_pressed_darker, null));
            }
            ((NotificationViewHolder) holder).mNotificationDateTextView.setText(Notifications_ListDataGenerator.getAllData().get(position).getNotificationDate());

            // LOADING A PROFILE PICTURE IF URL EXISTS
            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(Notifications_ListDataGenerator.getAllData().get(position).getPottPic().trim().equalsIgnoreCase("fp")){
                        Config.loadErrorImageView(getActivity(), R.drawable.fishpott_splash_icon, ((NotificationViewHolder) holder).mNotificationPic, 60, 60);
                    } else if (Notifications_ListDataGenerator.getAllData().get(position).getPottPic().trim().length() > 1) {
                        Config.loadUrlImage(getActivity(), true, Notifications_ListDataGenerator.getAllData().get(position).getPottPic().trim(), ((NotificationViewHolder) holder).mNotificationPic, 0, 60, 60);
                    }   else {
                        Config.loadErrorImageView(getActivity(), R.drawable.setprofilepicture_activity_imageholder_default_image, ((NotificationViewHolder) holder).mNotificationPic, 60, 60);
                    }
                }
            });

            imageLoaderThread.start();

        }

        @Override
        public int getItemCount() {
            return Notifications_ListDataGenerator.getAllData().size();
        }

    }

    private void allOnClickHandlers(View v, int position) {
        Log.e("NotChatFCM", "=|=|=|=|=|=|=|=|=|=| CLICK STARTED HERE 1 =|=|=|=|=|=|=|=|=|=|");
        Log.e("NotChatFCM", " HERE 2 RelevantId_1: " + Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_1());
        Log.e("NotChatFCM", " HERE 3 RelevantId_2: " + Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_2());
        Log.e("NotChatFCM", " HERE 4 RelevantId_3: " + Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_3());

        int newsType = 0;
        Notifications_DatabaseAdapter notifications_databaseAdapter = new Notifications_DatabaseAdapter(getActivity().getApplication());
        // OPENING THE STORIES DATABASE
        notifications_databaseAdapter.openDatabase();
        notifications_databaseAdapter.updateRow(Notifications_ListDataGenerator.getAllData().get(position).getRowId(),  1, Notifications_DatabaseAdapter.KEY_READ_STATUS, "", 1);
        Notifications_ListDataGenerator.getAllData().get(position).setReadStatus(1);
        notifications_databaseAdapter.closeDatabase();

        if (v.getId() == R.id.notification_list_root_view_constraintlayout || v.getId() == R.id.notification_infotext_textview || v.getId() == R.id.notification_date_textview) {
            if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_LIKE){
                Log.e("NotChatFCM", "HERE 5 ");
                Config.openNewsActivityBasedOnReaction(getActivity(), newsType, Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_3(), Config.NOTICATION_RELATING_LIKE_STRING);
            } else if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_DISLIKE){
                Log.e("NotChatFCM", "HERE 6 ");
                Config.openNewsActivityBasedOnReaction(getActivity(), newsType, Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_3(), Config.NOTICATION_RELATING_DISLIKE_STRING);
            } else if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_COMMENT){
                Log.e("NotChatFCM", "HERE 7 ");
                Config.openNewsActivityBasedOnReaction(getActivity(), newsType, Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_1(), Config.NOTICATION_RELATING_COMMENT_STRING);
            } else if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_NEWS_MENTION){
                Log.e("NotChatFCM", "HERE 8 ");
                Config.openNewsActivityBasedOnReaction(getActivity(), newsType, Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_1(), Config.NOTICATION_RELATING_COMMENT_STRING);
            } else if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_LINKUP){
                Log.e("NotChatFCM", "HERE 9 ");
                Config.openActivity(getActivity(), ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_2());
            } else if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_POACH){
                Log.e("NotChatFCM", "HERE 10 ");
                Config.showDialogType1(getActivity(), "1", Notifications_ListDataGenerator.getAllData().get(position).getNotificationMessage(), "", null, false, getString(R.string.setprofilepicture_activity_okay), "");
                //Config.openActivity(getActivity(), ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_2());
            } else if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_REFERRAL){
                Log.e("NotChatFCM", "HERE 11 ");
                Config.openActivity(getActivity(), ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_2());
            } else if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_REPOST){
                Log.e("NotChatFCM", "HERE 12 ");
                Config.openNewsActivityBasedOnReaction(getActivity(), newsType, Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_1(), Config.NOTICATION_RELATING_COMMENT_STRING);
            } else if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_SHARESFORSALE){
                Log.e("NotChatFCM", "HERE 13 ");
                Config.openNewsActivityBasedOnReaction(getActivity(), newsType, Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_1(), Config.NOTICATION_RELATING_PURCHASE_STRING);
            } else if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER){
                Log.e("NotChatFCM", "HERE 14 ");
                //Config.openNewsActivityBasedOnReaction(getActivity(), newsType, Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_1(), Config.NOTICATION_RELATING_LIKE_STRING);
                Config.showDialogType1(getActivity(), "1", Notifications_ListDataGenerator.getAllData().get(position).getNotificationMessage(), "", null, false, getString(R.string.setprofilepicture_activity_okay), "");
            } else if(Notifications_ListDataGenerator.getAllData().get(position).getNotificationType() == Config.NOTICATION_RELATING_SHARES_SUGGESTION){
                Log.e("NotChatFCM", "HERE 16 ");
                Config.openActivity(getActivity(), StockProfileActivity.class, 1, 0, 1, "shareparentid",  Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_1());
            } else {
                Log.e("NotChatFCM", "HERE 15 ");
                Config.showDialogType1(getActivity(), "1", Notifications_ListDataGenerator.getAllData().get(position).getNotificationMessage(), "", null, false, getString(R.string.setprofilepicture_activity_okay), "");
                //Config.showToastType1(getActivity(), "UNSPECIFIED NOTIIFICATION TYPE");
            }
        } else if(v.getId() == R.id.notification_pott_picture_circleimageview){
            if(!Notifications_ListDataGenerator.getAllData().get(position).getPottPic().trim().equalsIgnoreCase("fp")){
                Config.openActivity(getActivity(), ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", Notifications_ListDataGenerator.getAllData().get(position).getRelevantId_2());
            } else {
                Config.openActivity(getActivity(), ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", "fishpot_inc");
            }

        }

        mNotificationsRecyclerView.getAdapter().notifyDataSetChanged();
    }

}
