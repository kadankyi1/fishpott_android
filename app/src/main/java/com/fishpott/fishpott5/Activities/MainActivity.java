package com.fishpott.fishpott5.Activities;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Fragments.ChatListFragment;
import com.fishpott.fishpott5.Fragments.InfoFragment;
import com.fishpott.fishpott5.Fragments.NewsFeedFragment;
import com.fishpott.fishpott5.Fragments.NotificationsFragment;
import com.fishpott.fishpott5.Fragments.SettingsFragment;
import com.fishpott.fishpott5.Fragments.SharesToolBox.TransferCenterFragment;
import com.fishpott.fishpott5.Fragments.SuggestionFragment;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.ListDataGenerators.Vertical_NewsType_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Services.NewsFetcherAndPreparerService;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, NewsFeedFragment.OnFragmentInteractionListener {
    private TextView mSearchTextTextView;
    private View mSearchBarView;
    private String backLinkActivityClosedStatus = "0";
    private ImageView mSettingsIconImageView, mProfileImageView, mSearchIconImageView,
            mNewsfeedMenuIconImageView, mMessengerMenuIconImageView, mNotificationMenuIconImageView,
            mTransferCenterMenuIconImageView, mSharesCenterMenuIconImageView;
    private int currentMenuItemSelected = Config.NEWSFEED_ITEM;
    private ConstraintLayout mNewsfeedMenuIconHolderConstraintLayout, mMessengerMenuIconHolderConstraintLayout, mNotificationMenuIconHolderConstraintLayout,
            mTransferCenterMenuIconHolderConstraintLayout, mSharesCenterMenuIconHolderConstraintLayout;
    public static ConstraintLayout mNewsfeedMenuIconUpdateIconConstraintLayout, mMessengerMenuIconUpdateIconConstraintLayout, mNotificationMenuIconUpdateIconConstraintLayout,
            mTransferCenterMenuIconUpdateIconConstraintLayout, mSharesCenterMenuIconUpdateIconConstraintLayout, mProfileIconUpdateIconConstraintLayout;
    private ViewPager mFragmentsHolderViewPager;
    private MyPageAdapter pageAdapter;
    private Thread postNewsThread = null, generalBackgroundThread = null;
    private static final int KEY_PERMISSION_CONTACTS = 0005;
    private String[] permissions = {
            Manifest.permission.READ_CONTACTS
    };
    private Dialog.OnCancelListener cancelListenerActive1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generalBackgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Config.prepAppForUser(MainActivity.this, getApplicationContext());
                NewsFetcherAndPreparerService.updateUserInfo(getApplicationContext(), Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEVICE_TOKEN), LocaleHelper.getLanguage(getApplicationContext()));
            }
        });
        generalBackgroundThread.start();


        // GETTING VALUES FROM OLD-TO-NEW ACTIVITY TRANSITION
        Bundle intentBundle = getIntent().getExtras();
        if(intentBundle !=null) {
            backLinkActivityClosedStatus =(String) intentBundle.get(Config.KEY_ACTIVITY_FINISHED);
            if(backLinkActivityClosedStatus == null){
                backLinkActivityClosedStatus = "0";
            }
        }

        //BINDING VIEWS
        mFragmentsHolderViewPager = findViewById(R.id.activity_mainactivity_fragments_holder_viewpager);
        mSettingsIconImageView = findViewById(R.id.activity_mainactivity_constraintlayout2_menuicon_imageview);
        mProfileImageView = findViewById(R.id.activity_mainactivity_constraintlayout2_profileicon_imageview);
        mSearchIconImageView = findViewById(R.id.activity_mainactivity_constraintlayout2_searchicon_imageview);
        mSearchTextTextView = findViewById(R.id.activity_mainactivity_constraintlayout2_searchtext_textView);
        mSearchBarView = findViewById(R.id.activity_mainactivity_constraintlayout2_searchbar_view);
        mNewsfeedMenuIconImageView = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_newsfeed_imageview);
        mMessengerMenuIconImageView = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_messenger_icon_imageview);
        mNotificationMenuIconImageView = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_notification_imageview);
        mTransferCenterMenuIconImageView = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_transfercenter_imageview);
        mSharesCenterMenuIconImageView = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_sharescenter_imageview);
        mNewsfeedMenuIconHolderConstraintLayout = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_holder_newsfeed_constraintlayout);
        mMessengerMenuIconHolderConstraintLayout = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_holder_messenger_constraintlayout);
        mNotificationMenuIconHolderConstraintLayout = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_holder_notification_constraintlayout);
        mTransferCenterMenuIconHolderConstraintLayout = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_holder_transfercenter_constraintlayout);
        mSharesCenterMenuIconHolderConstraintLayout = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_holder_sharescenter_constraintlayout);
        mNewsfeedMenuIconUpdateIconConstraintLayout = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_newsfeed_icon_update_indicator_constraintlayout);
        mMessengerMenuIconUpdateIconConstraintLayout = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_messenger_icon_update_indicator_constraintlayout);
        mNotificationMenuIconUpdateIconConstraintLayout = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_notification_icon_update_indicator_constraintlayout);
        mTransferCenterMenuIconUpdateIconConstraintLayout = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_transfercenter_icon_update_indicator_constraintlayout);
        mSharesCenterMenuIconUpdateIconConstraintLayout = findViewById(R.id.activity_mainactivity_constraint_layout_menubar_sharescenter_icon_update_indicator_constraintlayout);
        mProfileIconUpdateIconConstraintLayout = findViewById(R.id.activity_mainactivity_constraintlayout2_profile_icon_update_indicator_constraintlayout);



        // ADDING AN ONLICK LISTENER TO THE SETTINS ICON
        mSettingsIconImageView.setOnClickListener(this);
        mProfileImageView.setOnClickListener(this);
        mSearchIconImageView.setOnClickListener(this);
        mSearchTextTextView.setOnClickListener(this);
        mSearchBarView.setOnClickListener(this);
        mNewsfeedMenuIconImageView.setOnClickListener(this);
        mMessengerMenuIconImageView.setOnClickListener(this);
        mNotificationMenuIconImageView.setOnClickListener(this);
        mTransferCenterMenuIconImageView.setOnClickListener(this);
        mSharesCenterMenuIconImageView.setOnClickListener(this);
        mNewsfeedMenuIconHolderConstraintLayout.setOnClickListener(this);
        mMessengerMenuIconHolderConstraintLayout.setOnClickListener(this);
        mNotificationMenuIconHolderConstraintLayout.setOnClickListener(this);
        mTransferCenterMenuIconHolderConstraintLayout.setOnClickListener(this);
        mSharesCenterMenuIconHolderConstraintLayout.setOnClickListener(this);


        // ADDING AN ON-LONG CLICK LISTENER TO THE SETTINS ICON
        mSettingsIconImageView.setOnLongClickListener(this);
        mProfileImageView.setOnLongClickListener(this);
        mSearchIconImageView.setOnLongClickListener(this);
        mSearchTextTextView.setOnLongClickListener(this);
        mSearchBarView.setOnLongClickListener(this);
        mNewsfeedMenuIconImageView.setOnLongClickListener(this);
        mMessengerMenuIconImageView.setOnLongClickListener(this);
        mNotificationMenuIconImageView.setOnLongClickListener(this);
        mTransferCenterMenuIconImageView.setOnLongClickListener(this);
        mSharesCenterMenuIconImageView.setOnLongClickListener(this);
        mNewsfeedMenuIconHolderConstraintLayout.setOnLongClickListener(this);
        mMessengerMenuIconHolderConstraintLayout.setOnLongClickListener(this);
        mNotificationMenuIconHolderConstraintLayout.setOnLongClickListener(this);
        mTransferCenterMenuIconHolderConstraintLayout.setOnLongClickListener(this);
        mSharesCenterMenuIconHolderConstraintLayout.setOnLongClickListener(this);

        //SETTING A PAGE/FRAGMENT CHANGE LISTENER FOR THE VIEWPAGER
        mFragmentsHolderViewPager.addOnPageChangeListener(viewListener);

        List<Fragment> fragmentsList = getFragments();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragmentsList);
        mFragmentsHolderViewPager.setAdapter(pageAdapter);
        mFragmentsHolderViewPager.setCurrentItem(1);

        setNotificationIndicators();

        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Config.getPermission(MainActivity.this, permissions, KEY_PERMISSION_CONTACTS);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Config.permissionsHaveBeenGranted(getApplicationContext(), permissions)) {
                if(!shouldShowRequestPermissionRationale(permissions[0])){
                    cancelListenerActive1 = Config.showDialogType3(MainActivity.this, cancelListenerActive1, true);
                }
            }
        } else {
            if (!Config.permissionsHaveBeenGranted(getApplicationContext(), permissions)) {
                cancelListenerActive1 = Config.showDialogType3(MainActivity.this, cancelListenerActive1, true);
            }
        }

        if(!Config.getSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_HAS_LEARNED)){
            mSearchIconImageView.setVisibility(View.VISIBLE);
            //Config.openToolTip("bottom", false, MainActivity.this, mSearchIconImageView, "Down ToolTip");

            //Config.openToolTip("top", false, MainActivity.this, mHelloWorldText, "Top ToolTip");
            //Config.openToolTip("bottom", true, MainActivity.this, mHelloWorldText, "Down ToolTip");
            //Config.openToolTip("right", false, MainActivity.this, mHelloWorldText, "Right ToolTip");
            //Config.openToolTip("left", true, MainActivity.this, mHelloWorldText, "Left ToolTip");

        }
    }

    //AFTER A PERMISSION IS GRANTED  OR NOT, WHAT WE DO
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == KEY_PERMISSION_CONTACTS) {
            //CHECKING IF A PERMISSION IS GRANTED OR NOT
            if (!Config.permissionsHaveBeenGranted(getApplicationContext(), permissions)) {
                Config.showDialogType1(MainActivity.this, "", getResources().getString(R.string.you_cannot_find_your_contacts_who_have_fishpotts_unless_you_give_the_app_permission_to_your_contacts), "", null, false, getResources().getString(R.string.setprofilepicture_activity_okay), "");
                Log.e("PermissionMy", "Here 5");
            }

        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener(){

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {

            if(i > -1 && i < 6){

                if(i == Config.MESSENGER_ITEM){
                    mMessengerMenuIconUpdateIconConstraintLayout.setVisibility(View.INVISIBLE);
                } else if(i == Config.NOTIFICATION_ITEM){
                    mNotificationMenuIconUpdateIconConstraintLayout.setVisibility(View.INVISIBLE);
                }
                animateMenuBarIcons(currentMenuItemSelected, i);
                currentMenuItemSelected = i;
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    // HANDLING ALL ON-LONG-CLICKS IN THE ACTIVITY
    @Override
    public boolean onLongClick(View v) {
        if(v.getId() == mSettingsIconImageView.getId()){
            Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, InfoFragment.newInstance(R.drawable.main_activity_topbar_menu_active_icon, R.drawable.main_activity_info_fragment_icon_imageview_circle_shadow_background, getString(R.string.settings_title), getString(R.string.settings_body)), "InfoFragment", 3);
        } else if (v.getId() == mProfileImageView.getId()){
            Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, InfoFragment.newInstance(R.drawable.main_activity_profile_icon_active_imageview, R.drawable.main_activity_info_fragment_icon_imageview_circle_shadow_background, getString(R.string.pott_finder_title), getString(R.string.pott_finder_body)), "InfoFragment", 3);
        } else if (v.getId() == mSearchBarView.getId()){
            Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, InfoFragment.newInstance(R.drawable.main_activity_topbar_search_icon, R.drawable.main_activity_info_fragment_icon_imageview_circle_white_background, getString(R.string.info_fragment_main_activity_search), getString(R.string.info_fragment_main_activity_there_are_so_many_things_in_the_fishpott_world_and_you_need_a_way_to_find_them_that_s_what_search_you_to_do_by_typing_the_right_word_for_your_search_you_can_find_yard_sales_events_shares_sales_fundraisers_and_more)), "InfoFragment", 3);
        } else if (v.getId() == mSearchTextTextView.getId()){
            Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, InfoFragment.newInstance(R.drawable.main_activity_topbar_search_icon, R.drawable.main_activity_info_fragment_icon_imageview_circle_white_background, getString(R.string.info_fragment_main_activity_search), getString(R.string.info_fragment_main_activity_there_are_so_many_things_in_the_fishpott_world_and_you_need_a_way_to_find_them_that_s_what_search_you_to_do_by_typing_the_right_word_for_your_search_you_can_find_yard_sales_events_shares_sales_fundraisers_and_more)), "InfoFragment", 3);
        } else if(v.getId() == mSearchIconImageView.getId()){
            Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, InfoFragment.newInstance(R.drawable.main_activity_topbar_search_icon, R.drawable.main_activity_info_fragment_icon_imageview_circle_white_background, getString(R.string.info_fragment_main_activity_search), getString(R.string.info_fragment_main_activity_there_are_so_many_things_in_the_fishpott_world_and_you_need_a_way_to_find_them_that_s_what_search_you_to_do_by_typing_the_right_word_for_your_search_you_can_find_yard_sales_events_shares_sales_fundraisers_and_more)), "InfoFragment", 3);
        } else if (v.getId() == mNewsfeedMenuIconImageView.getId()){
            Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, InfoFragment.newInstance(R.drawable.main_activity_newsfeed_icon_active_imageview, R.drawable.main_activity_info_fragment_icon_imageview_circle_white_background, getString(R.string.info_fragment_main_activity_globefeed), getString(R.string.globefeed_body)), "InfoFragment", 3);
        } else if (v.getId() == mMessengerMenuIconImageView.getId()){
            Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, InfoFragment.newInstance(R.drawable.main_activity_messenger_icon_active_imageview, R.drawable.main_activity_info_fragment_icon_imageview_circle_white_background, getString(R.string.info_fragment_main_activity_messenger), getString(R.string.info_fragment_main_activity_messenger_makes_it_possible_for_you_to_chat_with_the_potts_you_linkup_with)), "InfoFragment", 3);
        } else if (v.getId() == mNotificationMenuIconImageView.getId()){
            Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, InfoFragment.newInstance(R.drawable.main_activity_notification_icon_active_imageview, R.drawable.main_activity_info_fragment_icon_imageview_circle_white_background, getString(R.string.info_fragment_main_activity_notifications), getString(R.string.info_fragment_main_activity_your_pott_gets_notifications_just_to_let_you_know_what_s_going_on_you_get_notified_when_someone_linkups_up_to_your_pott_to_follow_you_likes_or_dislikes_your_posts_shares_you_own_or_might_be_interested_in_changes_in_value_when_someone_buys_shares_tickets_or_any_item_from_your_pott_all_notifications_come_with_an_icon_so_show_you_the_kind_it_is)), "InfoFragment", 3);
        } else if (v.getId() == mTransferCenterMenuIconImageView.getId()){
            Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, InfoFragment.newInstance(R.drawable.main_activity_transfercenter_icon_active_imageview, R.drawable.main_activity_info_fragment_icon_imageview_circle_white_background, getString(R.string.transfer_center_title), getString(R.string.transfer_center_body)), "InfoFragment", 3);
        } else if (v.getId() == mSharesCenterMenuIconImageView.getId()){
            Config.openFragment(getSupportFragmentManager(), R.id.activity_mainactivity_info_fragments_holder_framelayout, InfoFragment.newInstance(R.drawable.main_activity_sharescenter_icon_active_imageview, R.drawable.main_activity_info_fragment_icon_imageview_circle_white_background, getString(R.string.shares_center_title), getString(R.string.shares_center_body)), "InfoFragment", 3);
        } else if (v.getId() == mNewsfeedMenuIconHolderConstraintLayout.getId()){
            //mNewsfeedMenuIconImageView.performClick();
        } else if(v.getId() == mMessengerMenuIconHolderConstraintLayout.getId()){
            //mMessengerMenuIconImageView.performClick();
        } else if(v.getId() == mNotificationMenuIconHolderConstraintLayout.getId()){
            //mNotificationMenuIconImageView.performClick();
        } else if (v.getId() == mTransferCenterMenuIconHolderConstraintLayout.getId()){
            //mTransferCenterMenuIconImageView.performClick();
        } else if(v.getId() == mSharesCenterMenuIconHolderConstraintLayout.getId()){
            //mSharesCenterMenuIconImageView.performClick();
        }

        return true;
    }


    // HANDLING ALL ON-CLICKS IN THE ACTIVITY
    @Override
    public void onClick(View v) {

        if(v.getId() == mSettingsIconImageView.getId()){
            v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            animateMenuBarIcons(currentMenuItemSelected, Config.SETTIINGS_BAR_ITEM);
            currentMenuItemSelected = Config.SETTIINGS_BAR_ITEM;
            mFragmentsHolderViewPager.setCurrentItem(0);
        } else if (v.getId() == mProfileImageView.getId()){
            v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            Config.openActivity(MainActivity.this, ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME));
        }/* else if (v.getId() == mSearchBarView.getId()){
            //mSearchBarView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            //Config.openActivity(MainActivity.this, AboutActivity.class, 1, 0, 0, "", "");
        } else if (v.getId() == mSearchTextTextView.getId()){
            //mSearchBarView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            //mSearchTextTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            mSearchIconImageView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            //Config.showDialogType1(MainActivity.this, "1", getResources().getString(R.string.this_feature_is_not_yet_completed_we_are_sorry_if_this_causes_any_inconvenience_we_will_notify_you_when_it_s_completed), "", null, false, getResources().getString(R.string.setprofilepicture_activity_okay), "");
        }*/ else if(v.getId() == mSearchIconImageView.getId()){
            mSearchIconImageView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            Config.openActivity(MainActivity.this, AboutActivity.class, 1, 0, 0, "", "");
        } else if (v.getId() == mNewsfeedMenuIconImageView.getId()){
            v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            animateMenuBarIcons(currentMenuItemSelected, Config.NEWSFEED_ITEM);
            currentMenuItemSelected = Config.NEWSFEED_ITEM;
            mFragmentsHolderViewPager.setCurrentItem(1);
            if(mNewsfeedMenuIconUpdateIconConstraintLayout.getVisibility() == View.VISIBLE){
                NewsFeedFragment.mNewsRecyclerView.scrollToPosition(0);
                NewsFeedFragment.mMainSwipeRefreshLayout.setRefreshing(true);
                NewsFeedFragment.fetchVerticalNews(getApplicationContext(), true);
                mNewsfeedMenuIconUpdateIconConstraintLayout.setVisibility(View.INVISIBLE);
            }
        } else if (v.getId() == mMessengerMenuIconImageView.getId()){
            v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            animateMenuBarIcons(currentMenuItemSelected, Config.MESSENGER_ITEM);
            currentMenuItemSelected = Config.MESSENGER_ITEM;
            mFragmentsHolderViewPager.setCurrentItem(3);
            mMessengerMenuIconUpdateIconConstraintLayout.setVisibility(View.INVISIBLE);
        } else if (v.getId() == mNotificationMenuIconImageView.getId()){
            v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            animateMenuBarIcons(currentMenuItemSelected, Config.NOTIFICATION_ITEM);
            currentMenuItemSelected = Config.NOTIFICATION_ITEM;
            mFragmentsHolderViewPager.setCurrentItem(5);
            mNotificationMenuIconUpdateIconConstraintLayout.setVisibility(View.INVISIBLE);
        } else if (v.getId() == mTransferCenterMenuIconImageView.getId()){
            v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            animateMenuBarIcons(currentMenuItemSelected, Config.TRANSFER_CENTER_ITEM);
            currentMenuItemSelected = Config.TRANSFER_CENTER_ITEM;
            mFragmentsHolderViewPager.setCurrentItem(4);
        } else if (v.getId() == mSharesCenterMenuIconImageView.getId()){
            v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
            animateMenuBarIcons(currentMenuItemSelected, Config.SHARESCENTER_ITEM);
            currentMenuItemSelected = Config.SHARESCENTER_ITEM;
            mFragmentsHolderViewPager.setCurrentItem(2);
        } else if (v.getId() == mNewsfeedMenuIconHolderConstraintLayout.getId()){
            mNewsfeedMenuIconImageView.performClick();
        } else if(v.getId() == mMessengerMenuIconHolderConstraintLayout.getId()){
            mMessengerMenuIconImageView.performClick();
        } else if(v.getId() == mNotificationMenuIconHolderConstraintLayout.getId()){
            mNotificationMenuIconImageView.performClick();
        } else if (v.getId() == mTransferCenterMenuIconHolderConstraintLayout.getId()){
            mTransferCenterMenuIconImageView.performClick();
        } else if(v.getId() == mSharesCenterMenuIconHolderConstraintLayout.getId()){
            mSharesCenterMenuIconImageView.performClick();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        setNotificationIndicators();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.e("memoryManage", "onResume STARTED MAIN-ACTIVITY");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Config.freeMemory();
    }


    @Override
    public void finish() {
        super.finish();
        Log.e("memoryManage", "finish STARTED MAIN-ACTIVITY");
        if(backLinkActivityClosedStatus.trim().equalsIgnoreCase("1") || backLinkActivityClosedStatus.trim().equalsIgnoreCase("yes")){
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(postNewsThread != null){
            postNewsThread.interrupt();
            postNewsThread = null;
        }
        if(generalBackgroundThread != null){
            generalBackgroundThread.interrupt();
            generalBackgroundThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_main_activity));
        Config.freeMemory();
    }


    // ANIMATING THE TOP MENU ICONS WHEN THEY ARE CLICKED
    public void animateMenuBarIcons(int oldMenuItemSelected, int newMenuItemSelected){
        if(newMenuItemSelected == Config.NEWSFEED_ITEM){
            if(oldMenuItemSelected != Config.NEWSFEED_ITEM){
                mNewsfeedMenuIconImageView.setImageResource(R.drawable.main_activity_newsfeed_menu_icons_transition_on);
                ((TransitionDrawable) mNewsfeedMenuIconImageView.getDrawable()).startTransition(300);
                hideInactiveMenuBarIcons(oldMenuItemSelected);
            }
        } else if (newMenuItemSelected == Config.MESSENGER_ITEM){
            if(oldMenuItemSelected != Config.MESSENGER_ITEM){
                mMessengerMenuIconImageView.setImageResource(R.drawable.main_activity_messenger_menu_icons_transition_on);
                ((TransitionDrawable) mMessengerMenuIconImageView.getDrawable()).startTransition(200);
                hideInactiveMenuBarIcons(oldMenuItemSelected);
            }
        } else if (newMenuItemSelected == Config.NOTIFICATION_ITEM){
            if(oldMenuItemSelected != Config.NOTIFICATION_ITEM){
                mNotificationMenuIconImageView.setImageResource(R.drawable.main_activity_notification_menu_icons_transition_on);
                ((TransitionDrawable) mNotificationMenuIconImageView.getDrawable()).startTransition(200);
                hideInactiveMenuBarIcons(oldMenuItemSelected);
            }
        } else if (newMenuItemSelected == Config.TRANSFER_CENTER_ITEM){
            if(oldMenuItemSelected != Config.TRANSFER_CENTER_ITEM){
                mTransferCenterMenuIconImageView.setImageResource(R.drawable.main_activity_transfercenter_menu_icons_transition_on);
                ((TransitionDrawable) mTransferCenterMenuIconImageView.getDrawable()).startTransition(200);
                hideInactiveMenuBarIcons(oldMenuItemSelected);
            }
        } else if (newMenuItemSelected == Config.SHARESCENTER_ITEM){
            if(oldMenuItemSelected != Config.SHARESCENTER_ITEM){
                mSharesCenterMenuIconImageView.setImageResource(R.drawable.main_activity_sharescenter_menu_icons_transition_on);
                ((TransitionDrawable) mSharesCenterMenuIconImageView.getDrawable()).startTransition(200);
                hideInactiveMenuBarIcons(oldMenuItemSelected);
            }
        } else if (newMenuItemSelected == Config.SETTIINGS_BAR_ITEM){
            if(oldMenuItemSelected != Config.SETTIINGS_BAR_ITEM){
                mSettingsIconImageView.setImageResource(R.drawable.main_activity_menubar_icons_transition_on);
                ((TransitionDrawable) mSettingsIconImageView.getDrawable()).startTransition(200);
                hideInactiveMenuBarIcons(oldMenuItemSelected);
            }
        }
    }

    // HIDING INACTIVTE ICONS
    public void hideInactiveMenuBarIcons(int oldMenuItemSelected){
        if(oldMenuItemSelected == Config.NEWSFEED_ITEM){
            mNewsfeedMenuIconImageView.setImageResource(R.drawable.main_activity_newsfeed_menu_icons_transition_off);
            ((TransitionDrawable) mNewsfeedMenuIconImageView.getDrawable()).startTransition(30);
        } else if(oldMenuItemSelected == Config.MESSENGER_ITEM){
            mMessengerMenuIconImageView.setImageResource(R.drawable.main_activity_messenger_menu_icons_transition_off);
            ((TransitionDrawable) mMessengerMenuIconImageView.getDrawable()).startTransition(30);
        } else if(oldMenuItemSelected == Config.NOTIFICATION_ITEM){
            mNotificationMenuIconImageView.setImageResource(R.drawable.main_activity_notification_menu_icons_transition_off);
            ((TransitionDrawable) mNotificationMenuIconImageView.getDrawable()).startTransition(30);
        } else if(oldMenuItemSelected == Config.TRANSFER_CENTER_ITEM){
            mTransferCenterMenuIconImageView.setImageResource(R.drawable.main_activity_transfercenter_menu_icons_transition_off);
            ((TransitionDrawable) mTransferCenterMenuIconImageView.getDrawable()).startTransition(30);
        } else if (oldMenuItemSelected == Config.SHARESCENTER_ITEM) {
            mSharesCenterMenuIconImageView.setImageResource(R.drawable.main_activity_sharescenter_menu_icons_transition_off);
            ((TransitionDrawable) mSharesCenterMenuIconImageView.getDrawable()).startTransition(30);
        } else if (oldMenuItemSelected == Config.SETTIINGS_BAR_ITEM) {
            mSettingsIconImageView.setImageResource(R.drawable.main_activity_menubar_icons_transition_off);
            ((TransitionDrawable) mSettingsIconImageView.getDrawable()).startTransition(30);
        }
    }


    private List<Fragment> getFragments(){
        List<Fragment> fList = new ArrayList<Fragment>();

        fList.add(SettingsFragment.newInstance());
        //fList.add(NewsFeedFragment.newInstance());
        fList.add(SuggestionFragment.newInstance());
        //fList.add(SharesCenterFragment.newInstance());
        fList.add(ChatListFragment.newInstance());
        fList.add(TransferCenterFragment.newInstance());
        fList.add(NotificationsFragment.newInstance());
        return fList;
    }

    // THE FRAGMENT ADAPTER
    private class MyPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList;

        public MyPageAdapter(FragmentManager fragmentManager, List<Fragment> fragmentsList ) {
            super(fragmentManager);
            this.fragmentList = fragmentsList;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return this.fragmentList.size();
        }
    }

    @Override
    public void onBackPressed() {

        //GETTING THE FRAGMENT MANAGER OF ALL FRAGMENTS OPEN ON THE MAIN ACTIVITY
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if(count == 0){
            if(mFragmentsHolderViewPager.getCurrentItem() == 1){
                Log.e("MainOnBackPressed", "findFirstVisibleItemPosition : " + String.valueOf(NewsFeedFragment.mMainNewsLayoutManager.findFirstVisibleItemPosition()));
                if(
                        NewsFeedFragment.mMainNewsLayoutManager.findFirstVisibleItemPosition() < 3
                ){
                    super.onBackPressed();
                } else {
                    NewsFeedFragment.mNewsRecyclerView.getLayoutManager().scrollToPosition(0);
                    if(mNewsfeedMenuIconUpdateIconConstraintLayout.getVisibility() == View.VISIBLE){
                        NewsFeedFragment.mMainSwipeRefreshLayout.setRefreshing(true);
                        NewsFeedFragment.fetchVerticalNews(getApplicationContext(), true);
                        mNewsfeedMenuIconUpdateIconConstraintLayout.setVisibility(View.INVISIBLE);
                    }
                }
            } else if(mFragmentsHolderViewPager.getCurrentItem() == 0){
                super.onBackPressed();
            } else {
                //mFragmentsHolderViewPager.setCurrentItem(mFragmentsHolderViewPager.getCurrentItem()-1);
                mFragmentsHolderViewPager.setCurrentItem(1);
            }
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Config.CREATE_POST_NEWS_POSTED_REQUEST_CODE && resultCode == RESULT_OK){
            if(data.getIntExtra("result", 0) == 1){
                postNewsThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        uploadNewsPost();
                    }
                });
                postNewsThread.start();
                //Config.showToastType1(MainActivity.this, getString(R.string.post_is_uploading));
                NewsFeedFragment.mNewsRecyclerView.getAdapter().notifyItemInserted(1);
            }
        }
    }


    @Override
    public void onFragmentInteraction(String returnedString) {
        if(returnedString.equalsIgnoreCase("1")){

            postNewsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadNewsPost();
                }
            });
            postNewsThread.start();

            Vertical_NewsType_ListDataGenerator.getAllData().get(1).setNewsHasBeenPosted(1);
            NewsFeedFragment.mNewsRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public void uploadNewsPost(){
        Log.e("MainActLog", "here-n 1");
                final String newsId = Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsId();

                List<File> allImages = new ArrayList<>();
                //File[] allImages = {null, null, null,null, null, null,null, null, null, null};
                for (int i = 0; i < CreatePostActivity.allselectedImagesRealPaths.length; i++){
                    if(CreatePostActivity.allselectedImagesRealPaths[i] != null && !CreatePostActivity.allselectedImagesRealPaths[i].equalsIgnoreCase("")){
                        File thisfile = Config.getFileFromFilePath(CreatePostActivity.allselectedImagesRealPaths[i]);
                        if(thisfile !=  null){
                            allImages.add(thisfile);
                        }
                    }
                }

                // BACKGROUND ACTIONS COME HERE
                if(Connectivity.isConnected(MainActivity.this)){
                    Log.e("MainActLog", "here-n 2");
                    if(!Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsVideosLinksSeparatedBySpaces().trim().equalsIgnoreCase("")) {
                        File videoFile = Config.getFileFromFilePath(Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsVideosLinksSeparatedBySpaces().trim());
                        if(videoFile !=  null){
                            if(allImages.size() == 1){

                                Log.e("MainActLog", "Video Upload 1");
                                AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                        .addMultipartFile("news_video", videoFile)
                                        .addMultipartFile("news_image_1", allImages.get(0))
                                        .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                        .setTag("post_news_1img_1vid_justnewstext_mainactivity")
                                        .setPriority(Priority.HIGH)
                                        .build().getAsString(new StringRequestListener() {
                                    @Override
                                    public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                    @Override
                                    public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                                });
                            } else {
                                Log.e("MainActLog", "Video Upload 2");
                                AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                        .addMultipartFile("news_video", videoFile)
                                        .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                        .setTag("post_news_noimg_1vid_justnewstext_mainactivity")
                                        .setPriority(Priority.HIGH)
                                        .build().getAsString(new StringRequestListener() {
                                    @Override
                                    public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                    @Override
                                    public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                                });
                            }
                        }
                    } else {
                        if(allImages.size() == 0){
                            if(Vertical_NewsType_ListDataGenerator.getAllData().get(1).getRepostedNewsId().equalsIgnoreCase("")){
                                Log.e("MainActLog", "Text Upload 1");
                                Log.e("MainAct", "log_phone: " + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE));
                                Log.e("MainAct", "log_pass_token: " + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN));
                                Log.e("MainAct", "news_reposted_id: " + Vertical_NewsType_ListDataGenerator.getAllData().get(1).getRepostedNewsId().trim());
                                Log.e("MainAct", "news_text: " + Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim());
                                Log.e("MainAct", "news_time: " + Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime());
                                Log.e("MainAct", "added_item_id: " + Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId());
                                Log.e("MainAct", "added_item_price: " + Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice());
                                Log.e("MainAct", "added_item_quantity: " + Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity());
                                Log.e("MainAct", "myrawpass: " + Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon());
                                Log.e("MainAct", "mypottname: " + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME));
                                Log.e("MainAct", "my_currency: " + Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY));
                                Log.e("MainAct", "language: " + LocaleHelper.getLanguage(MainActivity.this));
                                Log.e("MainAct", "app_version_code: " + String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)));


                                AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                        .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                                        .addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                                        .addMultipartParameter("news_reposted_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getRepostedNewsId().trim())
                                        .addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim())
                                        .addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime())
                                        .addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId())
                                        .addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice())
                                        .addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity())
                                        .addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon())
                                        .addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                                        .addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                                        .addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this))
                                        .addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                        .setTag("post_news_text_0img_0vid_justnewstext_mainactivity")
                                        .setPriority(Priority.HIGH)
                                        .build().getAsString(new StringRequestListener() {
                                    @Override
                                    public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                    @Override
                                    public void onError(ANError anError) {Log.e("MainAct", "anError: " + anError);
                                        newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                                });
                            } else {
                                AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                        .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                                        .addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                                        .addMultipartParameter("news_reposted_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getRepostedNewsId().trim())
                                        .addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim())
                                        .addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime())
                                        .addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId())
                                        .addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice())
                                        .addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity())
                                        .addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon())
                                        .addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                                        .addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                                        .addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this))
                                        .addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                        .setTag("post_news_text_0img_0vid_justnewstext_mainactivity")
                                        .setPriority(Priority.HIGH)
                                        .build().getAsString(new StringRequestListener() {
                                    @Override
                                    public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                    @Override
                                    public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                                });
                            }
                        } else if(allImages.size() == 1){
                            AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                    .addMultipartFile("news_image_1", allImages.get(0))
                                    .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                    .setTag("post_news_1img_novid_justnewstext_mainactivity")
                                    .setPriority(Priority.HIGH)
                                    .build().getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                @Override
                                public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                            });
                        } else if(allImages.size() == 2){
                            AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                    .addMultipartFile("news_image_2", allImages.get(1)).addMultipartFile("news_image_1", allImages.get(0))
                                    .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                    .setTag("post_news_2img_novid_justnewstext_mainactivity")
                                    .setPriority(Priority.HIGH)
                                    .build().getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                @Override
                                public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                            });
                        } else if(allImages.size() == 3){
                            AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                    .addMultipartFile("news_image_3", allImages.get(2)).addMultipartFile("news_image_2", allImages.get(1)).addMultipartFile("news_image_1", allImages.get(0))
                                    .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                    .setTag("post_news_3img_novid_justnewstext_mainactivity")
                                    .setPriority(Priority.HIGH)
                                    .build().getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                @Override
                                public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                            });
                        } else if(allImages.size() == 4){
                            AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                    .addMultipartFile("news_image_4", allImages.get(3)).addMultipartFile("news_image_3", allImages.get(2)).addMultipartFile("news_image_2", allImages.get(1)).addMultipartFile("news_image_1", allImages.get(0))
                                    .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                    .setTag("post_news_4img_novid_justnewstext_mainactivity")
                                    .setPriority(Priority.HIGH)
                                    .build().getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                @Override
                                public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                            });
                        } else if(allImages.size() == 5){
                            AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                    .addMultipartFile("news_image_5", allImages.get(4)).addMultipartFile("news_image_4", allImages.get(3)).addMultipartFile("news_image_3", allImages.get(2)).addMultipartFile("news_image_2", allImages.get(1)).addMultipartFile("news_image_1", allImages.get(0))
                                    .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                    .setTag("post_news_5img_novid_justnewstext_mainactivity")
                                    .setPriority(Priority.HIGH)
                                    .build().getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                @Override
                                public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                            });
                        } else if(allImages.size() == 6){
                            AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                    .addMultipartFile("news_image_6", allImages.get(5)).addMultipartFile("news_image_5", allImages.get(4)).addMultipartFile("news_image_4", allImages.get(3)).addMultipartFile("news_image_3", allImages.get(2)).addMultipartFile("news_image_2", allImages.get(1)).addMultipartFile("news_image_1", allImages.get(0))
                                    .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                    .setTag("post_news_6img_novid_justnewstext_mainactivity")
                                    .setPriority(Priority.HIGH)
                                    .build().getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                @Override
                                public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                            });
                        } else if(allImages.size() == 7){
                            AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                    .addMultipartFile("news_image_7", allImages.get(6)).addMultipartFile("news_image_6", allImages.get(5)).addMultipartFile("news_image_5", allImages.get(4)).addMultipartFile("news_image_4", allImages.get(3)).addMultipartFile("news_image_3", allImages.get(2)).addMultipartFile("news_image_2", allImages.get(1)).addMultipartFile("news_image_1", allImages.get(0))
                                    .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                    .setTag("post_news_7img_novid_justnewstext_mainactivity")
                                    .setPriority(Priority.HIGH)
                                    .build().getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                @Override
                                public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                            });
                        } else if(allImages.size() == 8){
                            AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                    .addMultipartFile("news_image_8", allImages.get(7)).addMultipartFile("news_image_7", allImages.get(6)).addMultipartFile("news_image_6", allImages.get(5)).addMultipartFile("news_image_5", allImages.get(4)).addMultipartFile("news_image_4", allImages.get(3)).addMultipartFile("news_image_3", allImages.get(2)).addMultipartFile("news_image_2", allImages.get(1)).addMultipartFile("news_image_1", allImages.get(0))
                                    .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                    .setTag("post_news_8img_novid_justnewstext_mainactivity")
                                    .setPriority(Priority.HIGH)
                                    .build().getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                @Override
                                public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                            });
                        } else if(allImages.size() == 9){
                            AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                    .addMultipartFile("news_image_9", allImages.get(8)).addMultipartFile("news_image_8", allImages.get(7)).addMultipartFile("news_image_7", allImages.get(6)).addMultipartFile("news_image_6", allImages.get(5)).addMultipartFile("news_image_5", allImages.get(4)).addMultipartFile("news_image_4", allImages.get(3)).addMultipartFile("news_image_3", allImages.get(2)).addMultipartFile("news_image_2", allImages.get(1)).addMultipartFile("news_image_1", allImages.get(0))
                                    .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                    .setTag("post_news_9img_novid_justnewstext_mainactivity")
                                    .setPriority(Priority.HIGH)
                                    .build().getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                @Override
                                public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                            });
                        } else if(allImages.size() == 10){
                            AndroidNetworking.upload(Config.LINK_POST_NEWS)
                                    .addMultipartFile("news_image_10", allImages.get(9)).addMultipartFile("news_image_9", allImages.get(8)).addMultipartFile("news_image_8", allImages.get(7)).addMultipartFile("news_image_7", allImages.get(6)).addMultipartFile("news_image_6", allImages.get(5)).addMultipartFile("news_image_5", allImages.get(4)).addMultipartFile("news_image_4", allImages.get(3)).addMultipartFile("news_image_3", allImages.get(2)).addMultipartFile("news_image_2", allImages.get(1)).addMultipartFile("news_image_1", allImages.get(0))
                                    .addMultipartParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE)).addMultipartParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN)).addMultipartParameter("news_text", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsText().trim()).addMultipartParameter("news_time", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsTime()).addMultipartParameter("added_item_id", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemId()).addMultipartParameter("added_item_price", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemPrice()).addMultipartParameter("added_item_quantity", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemQuantity()).addMultipartParameter("myrawpass", Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsAddedItemIcon()).addMultipartParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)).addMultipartParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY)).addMultipartParameter("language", LocaleHelper.getLanguage(MainActivity.this)).addMultipartParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                                    .setTag("post_news_10img_novid_justnewstext_mainactivity")
                                    .setPriority(Priority.HIGH)
                                    .build().getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {newsUploadServerResponseUtilizer(response, newsId);}
                                @Override
                                public void onError(ANError anError) {newsUploadErrorResponse(newsId, true);postNewsThread = Config.closeBackgroundThread2(postNewsThread);}
                            });
                        }
                    }

                } else {newsUploadErrorResponse(newsId, false);}
    }
    public void newsUploadErrorResponse(String newsId, Boolean showToast){

        Log.e("MainActLog", "Upload Error 1");
        if(newsId.equalsIgnoreCase(Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsId())){
            Log.e("MainActLog", "Upload Error 2");
            Vertical_NewsType_ListDataGenerator.getAllData().get(1).setNewsHasBeenPosted(0);
            if(showToast){Config.showToastType1(MainActivity.this, getString(R.string.your_post_failed_to_upload));}
            if(MyLifecycleHandler.isApplicationVisible()){
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        NewsFeedFragment.mNewsRecyclerView.getAdapter().notifyDataSetChanged();
                        NewsFeedFragment.mNewsRecyclerView.scrollToPosition(1);
                    }
                });
            }
        } else {
            Log.e("MainActLog", "Upload Error 3");
            if(showToast){Config.showToastType1(MainActivity.this, getString(R.string.a_post_failed_to_upload));}
        }
    }

    public void newsUploadServerResponseUtilizer(String response, String newsId){
        Log.e("MainActivity", "response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray array = jsonObject.getJSONArray("data_returned");
            JSONObject o = array.getJSONObject(0);
            int myStatus = o.getInt("1");
            String statusMsg = o.getString("2");

            if(newsId.equalsIgnoreCase(Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsId())){
                if(myStatus == 1){
                    NewsFetcherAndPreparerService.fetchVerticalNewsAndPrepareMedia(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()), 0);
                    Log.e("MainActivity", "NEWS FETCHER STARTED");
                    //STORING THE USER DATA
                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));
                    // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                    Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                    //IF ID VERIFICATION IS NEEDED
                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_GOVERNMENT_ID_VERIFICATION_NEEDED_STATUS, o.getBoolean("7"));

                    Vertical_NewsType_ListDataGenerator.getAllData().remove(1);
                    if(MyLifecycleHandler.isApplicationVisible()){
                        NewsFeedFragment.mNewsRecyclerView.getAdapter().notifyItemRemoved(1);
                    }
                    Config.showToastType1(MainActivity.this, getString(R.string.your_news_posted_successfully));
                } else {
                    // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                    if(myStatus == 2){
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                        Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                        return;
                    }
                    // GENERAL ERROR
                    else if(myStatus == 3){Config.showToastType1(MainActivity.this, statusMsg);}
                    //WRONG SHARESFORSALE PASSWORD
                    else if(myStatus == 5){Config.showToastType1(MainActivity.this, getString(R.string.incorrect_password));}
                    // SHARES FOR SALE FAILED TO POST
                    else if(myStatus == 6){Config.showToastType1(MainActivity.this, getString(R.string.shares_failed_to_post));}
                    // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                    else if(myStatus == 4){Config.showToastType1(MainActivity.this, statusMsg);Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);}
                    else {Config.showToastType1(MainActivity.this, getString(R.string.your_post_failed_to_upload));}
                    Vertical_NewsType_ListDataGenerator.getAllData().get(1).setNewsHasBeenPosted(0);
                    if(MyLifecycleHandler.isApplicationVisible()){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                NewsFeedFragment.mNewsRecyclerView.getAdapter().notifyDataSetChanged();
                                NewsFeedFragment.mNewsRecyclerView.scrollToPosition(1);
                            }
                        });
                    }
                }
            } else {
                Config.showToastType1(MainActivity.this, getString(R.string.a_post_failed_to_upload));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            newsUploadErrorResponse(newsId, true);
        }

        // CHECKING IF ACCOUNT IS SUSPENDED
        if(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS) == 1){
            Config.openActivity(MainActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
            return;
        }

        // CHECKING IF ACCOUNT ID VERIFICATION IS NEEDED
        if(Config.getSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_GOVERNMENT_ID_VERIFICATION_NEEDED_STATUS)){
            Config.openActivity(MainActivity.this, GovernmentIDVerificationActivity.class, 1, 2, 0, "", "");
            return;
        }

        // CHECKING IF THE USER HAS TO VERIFY THEIR PHONE NUMBER
        if(Config.getSharedPreferenceBoolean(MainActivity.this, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON) && !Config.getSharedPreferenceString(MainActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE).trim().equalsIgnoreCase("")){
            Config.openActivity(MainActivity.this, ConfirmPhoneNumberActivity.class, 1, 2, 1, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, Config.getSharedPreferenceString(MainActivity.this, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE));
            return;
        }
        postNewsThread = Config.closeBackgroundThread2(postNewsThread);
    }

    public void setNotificationIndicators(){
        if(Config.getSharedPreferenceInt(getApplicationContext(), Config.NOTIFICATION_UNREAD_COUNT) > 0){
            mNotificationMenuIconUpdateIconConstraintLayout.setVisibility(View.VISIBLE);
            mNotificationMenuIconUpdateIconConstraintLayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
        }
        if(Config.getSharedPreferenceInt(getApplicationContext(), Config.CHAT_NOTIFICATION_UNREAD_COUNT) > 0){
            mMessengerMenuIconUpdateIconConstraintLayout.setVisibility(View.VISIBLE);
            mMessengerMenuIconUpdateIconConstraintLayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.main_activity_onclick_icon_anim));
        }
    }

}
