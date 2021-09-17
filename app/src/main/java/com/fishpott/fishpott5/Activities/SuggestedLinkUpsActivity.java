package com.fishpott.fishpott5.Activities;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.ListDataGenerators.SuggestedLinkupsListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.SuggestedLinkUpsModel;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Views.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SuggestedLinkUpsActivity extends AppCompatActivity implements View.OnClickListener {

    private SwipeRefreshLayout mMainSwipeRefreshLayout;
    private RecyclerView mSuggestedLinkupsRecyclerView;
    private RecyclerView.OnScrollListener scrollListener;
    private ImageView mForwardImageView,mReloadFreshImageView;
    private TextView mContinueTextView;
    private ProgressBar mLinkupsLoadingProgressBar;
    private LinearLayoutManager mLayoutManager;
    private Dialog.OnCancelListener cancelListenerActive1;
    private static final int KEY_PERMISSION_CONTACTS = 0005;
    private String[] permissions = {
            Manifest.permission.READ_CONTACTS
    };
    private Boolean REQUEST_HAS_STARTED = false;
    private Thread suggestedLinkupsFetchThread2 = null, imageLoaderThread = null, addLinkupThread = null;
    private String sessionId = "", allPhonesNumbers = "", allPhonesNames = "",  networkResponse = "";
    private Boolean someLinkupshaveBeenFetched = false, contactsSavedToServer = false, contactsPicked = false;
    private int requestMakerStyle = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_link_ups);

        mMainSwipeRefreshLayout = findViewById(R.id.activity_suggestedlinkupsactivity_swiperefreshayout);
        mSuggestedLinkupsRecyclerView = findViewById(R.id.activity_suggestedlinkupsactivity_recyclerview);
        mForwardImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mReloadFreshImageView = findViewById(R.id.activity_suggestedlinkupsactivity_reload_imageview);
        mLinkupsLoadingProgressBar = findViewById(R.id.activity_suggestedlinkupsactivity_loader);
        mContinueTextView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_title_textview);

        mLayoutManager = new LinearLayoutManager(SuggestedLinkUpsActivity.this);

        scrollListener = new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!REQUEST_HAS_STARTED && mLayoutManager.getItemCount() <= (mLayoutManager.findLastVisibleItemPosition() + 5)) {
                    REQUEST_HAS_STARTED =  true;
                    if(contactsPicked){
                        requestMakerStyle = 1;
                    } else {
                        requestMakerStyle = 0;
                    }
                    makeRequest();
                }
            }
        };

        mSuggestedLinkupsRecyclerView.setItemViewCacheSize(20);
        mSuggestedLinkupsRecyclerView.setDrawingCacheEnabled(true);
        mSuggestedLinkupsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mSuggestedLinkupsRecyclerView.setLayoutManager(mLayoutManager);
        mSuggestedLinkupsRecyclerView.addOnScrollListener(scrollListener);
        mSuggestedLinkupsRecyclerView.setAdapter(new RecyclerViewAdapter(this));

        mContinueTextView.setOnClickListener(this);
        mForwardImageView.setOnClickListener(this);
        mReloadFreshImageView.setOnClickListener(this);

        mMainSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if(!REQUEST_HAS_STARTED && Connectivity.isConnected(SuggestedLinkUpsActivity.this)){
                            allPhonesNames = "";
                            allPhonesNumbers = "";
                            sessionId = "";
                            contactsSavedToServer = false;
                            requestMakerStyle = 2;
                            makeRequest();
                        } else {
                            mMainSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
        );

        suggestedLinkupsFetchThread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                if(Connectivity.isConnected(SuggestedLinkUpsActivity.this)){
                    REQUEST_HAS_STARTED =  true;
                    getSuggestedLinkUps(LocaleHelper.getLanguage(SuggestedLinkUpsActivity.this), allPhonesNumbers, allPhonesNames);
                } else {
                    if(MyLifecycleHandler.isApplicationInForeground()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                tuningViewsOnError(0, "");
                            }
                        });
                    } else {
                        networkResponse = "00";
                    }
                    REQUEST_HAS_STARTED =  false;
                }
            }
        });

        suggestedLinkupsFetchThread2.start();

        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Config.getPermission(SuggestedLinkUpsActivity.this, permissions, KEY_PERMISSION_CONTACTS);
            }
        };

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == KEY_PERMISSION_CONTACTS) {
                if (Config.permissionsHaveBeenGranted(getApplicationContext(), permissions)) {
                    suggestedLinkupsFetchThread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            REQUEST_HAS_STARTED =  true;
                            if(!contactsSavedToServer){
                                if(allPhonesNumbers.equalsIgnoreCase("") && allPhonesNames.equalsIgnoreCase("")){
                                    String[] allContacts = Config.getContactList(SuggestedLinkUpsActivity.this);
                                    allPhonesNumbers = allContacts[0];
                                    allPhonesNames = allContacts[1];
                                    contactsPicked = true;
                                }
                            } else {
                                allPhonesNumbers = "";
                                allPhonesNames = "";
                            }
                            if(Connectivity.isConnected(SuggestedLinkUpsActivity.this)){
                                getSuggestedLinkUps(LocaleHelper.getLanguage(SuggestedLinkUpsActivity.this), allPhonesNumbers, allPhonesNames);
                            } else {
                                REQUEST_HAS_STARTED =  false;
                                if(MyLifecycleHandler.isApplicationInForeground()) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            tuningViewsOnError(0, "");
                                        }
                                    });
                                } else {
                                    networkResponse = "00";
                                }
                            }
                        }
                    });
                    suggestedLinkupsFetchThread2.start();
                } else {
                    suggestedLinkupsFetchThread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(Connectivity.isConnected(SuggestedLinkUpsActivity.this)){
                                REQUEST_HAS_STARTED =  true;
                                getSuggestedLinkUps(LocaleHelper.getLanguage(SuggestedLinkUpsActivity.this), "", "");
                            } else {
                                REQUEST_HAS_STARTED =  false;
                                if(MyLifecycleHandler.isApplicationInForeground()) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            tuningViewsOnError(0, "");
                                        }
                                    });
                                } else {
                                    networkResponse = "00";
                                }
                            }
                        }
                    });
                    suggestedLinkupsFetchThread2.start();
                }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED SUGGESTEDLINKUPS-ACTIVITY");
        mMainSwipeRefreshLayout = findViewById(R.id.activity_suggestedlinkupsactivity_swiperefreshayout);
        mForwardImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mReloadFreshImageView = findViewById(R.id.activity_suggestedlinkupsactivity_reload_imageview);
        mLinkupsLoadingProgressBar = findViewById(R.id.activity_suggestedlinkupsactivity_loader);
        mContinueTextView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_title_textview);

        if(!networkResponse.trim().equalsIgnoreCase("")){
            if(networkResponse.trim().equalsIgnoreCase("00")){
                tuningViewsOnError(0, "");
            } else if(networkResponse.trim().equalsIgnoreCase("1")){
                if(mMainSwipeRefreshLayout.isRefreshing()){
                    mMainSwipeRefreshLayout.setRefreshing(false);
                }
                if(requestMakerStyle == 0){
                    mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                    mReloadFreshImageView.setVisibility(View.INVISIBLE);
                }

                mSuggestedLinkupsRecyclerView.setVisibility(View.VISIBLE);
            }  else if(networkResponse.trim().equalsIgnoreCase("2")){
                mReloadFreshImageView.setVisibility(View.INVISIBLE);
                mLinkupsLoadingProgressBar.setVisibility(View.VISIBLE);
            } else {
                tuningViewsOnError(1, networkResponse);
            }
            networkResponse = "";
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mMainSwipeRefreshLayout = null;
        mForwardImageView = null;
        mReloadFreshImageView = null;
        mLinkupsLoadingProgressBar = null;
        mContinueTextView = null;
        if(suggestedLinkupsFetchThread2 != null){
            suggestedLinkupsFetchThread2.interrupt();
            suggestedLinkupsFetchThread2 = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        Log.e("memoryManage", "finish STARTED SUGGESTEDLINKUPS-ACTIVITY");
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED SUGGESTEDLINKUPS-ACTIVITY");
        mMainSwipeRefreshLayout = null;
        mSuggestedLinkupsRecyclerView = null;
        scrollListener = null;
        mForwardImageView = null;
        mReloadFreshImageView = null;
        mContinueTextView = null;
        mLinkupsLoadingProgressBar = null;
        mLayoutManager = null;
        cancelListenerActive1 = null;
        permissions = null;
        if(suggestedLinkupsFetchThread2 != null){
            suggestedLinkupsFetchThread2.interrupt();
            suggestedLinkupsFetchThread2 = null;
        }
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        addLinkupThread = null;
        someLinkupshaveBeenFetched = null;
        contactsSavedToServer = null;
        contactsPicked = null;
        sessionId = null;
        allPhonesNumbers = null;
        allPhonesNames = null;
        networkResponse = null;
        Config.unbindDrawables(findViewById(R.id.root_suggestedlinkUps_activity));
        Config.freeMemory();
    }

    @Override
    public void onClick(final View v) {
        if(v.getId() == R.id.list_item_suggestedlinkupsactivity_activity_linkup_button){
                final int itemPosition = mSuggestedLinkupsRecyclerView.getChildLayoutPosition((View) v.getParent());
                addLinkupThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String thisPottName = SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(itemPosition).getPottName();
                        String thisPottId = SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(itemPosition).getInvestorId();
                        Config.updateSharedPreferenceSetString(getApplicationContext(), Config.SHARED_PREF_KEY_LINKUPS, thisPottName, Config.getSharedPreferenceStringSet(getApplicationContext(), Config.SHARED_PREF_KEY_LINKUPS), false);

                        if(Connectivity.isConnected(SuggestedLinkUpsActivity.this)){
                            if(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData() != null){
                                if(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().size() > 0){

                                    SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().remove(itemPosition);
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSuggestedLinkupsRecyclerView.getAdapter().notifyItemRemoved(itemPosition);
                                        }
                                    });
                                    addLinkUp(LocaleHelper.getLanguage(SuggestedLinkUpsActivity.this), thisPottId);
                                }
                            }
                        } else {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showToastType1(SuggestedLinkUpsActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));                                }
                            });

                        }
                    }
                });
                addLinkupThread.start();
        } else if (v.getId() == R.id.activity_suggestedlinkupsactivity_bottomreload_imageview){
            if(!REQUEST_HAS_STARTED && Connectivity.isConnected(SuggestedLinkUpsActivity.this)) {
                mSuggestedLinkupsRecyclerView.getChildViewHolder((View) v.getParent()).itemView.findViewById(R.id.activity_suggestedlinkupsactivity_bottomreload_imageview).setVisibility(View.INVISIBLE);
                mSuggestedLinkupsRecyclerView.getChildViewHolder((View) v.getParent()).itemView.findViewById(R.id.activity_suggestedlinkupsactivity_bottomloader).setVisibility(View.VISIBLE);
                requestMakerStyle = 1;
                makeRequest();
            }
        } else if(v.getId() == R.id.activity_suggestedlinkupsactivity_constraintlayout2_title_textview){
            Config.openActivity(SuggestedLinkUpsActivity.this, MainActivity.class, 1, 2, 1, "", "");
        } else if(v.getId() == R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview){
            Config.openActivity(SuggestedLinkUpsActivity.this, MainActivity.class, 1, 2, 1, "", "");
        } else if(v.getId() == R.id.activity_suggestedlinkupsactivity_reload_imageview){
            if(!REQUEST_HAS_STARTED && Connectivity.isConnected(SuggestedLinkUpsActivity.this)) {
                mReloadFreshImageView.setVisibility(View.INVISIBLE);
                mLinkupsLoadingProgressBar.setVisibility(View.VISIBLE);
                requestMakerStyle = 0;
                makeRequest();
            }
        } else if(v.getId() == R.id.activity_suggestedlinkupsactivity_swiperefreshayout){

        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter{

        View.OnClickListener clickListener;

        public RecyclerViewAdapter(View.OnClickListener listener) {
            this.clickListener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            return SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(position) != null ? VIEW_ITEM : VIEW_PROG;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            if (viewType == VIEW_ITEM) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_suggestedlinkups_activity, parent, false);
                vh = new SuggestedLinkupViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loadmore_suggestedlinkups_activity, parent, false);
                vh = new ProgressViewHolder(v);
            }

            return vh;
        }

        public class SuggestedLinkupViewHolder extends RecyclerView.ViewHolder {
            private TextView mLinkupPottNameTextView, mLinkupFullNameTextView, mLinkupReasonTextView;
            private ImageView mLinkupVerifiedBlueStatusIconImageView, mLinkupVerifiedGreenStatusIconImageView, mLinkupBusinessAccountIconImageView;
            private CircleImageView mLinkupProfilePicImageView;
            private Button mLinkupButton;

            public SuggestedLinkupViewHolder(View v) {
                super(v);
                mLinkupProfilePicImageView = itemView.findViewById(R.id.chat_pott_picture_circleimageview);
                mLinkupVerifiedBlueStatusIconImageView = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_verfiedblue_imageview);
                mLinkupVerifiedGreenStatusIconImageView = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_verfiedgreen_imageview);
                mLinkupBusinessAccountIconImageView = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_businessicon_imageview);
                mLinkupPottNameTextView = itemView.findViewById(R.id.chat_full_name_textview);
                mLinkupFullNameTextView = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_fullname_textView);
                mLinkupReasonTextView = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_reason_textView);
                mLinkupButton = itemView.findViewById(R.id.list_item_suggestedlinkupsactivity_activity_linkup_button);
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {
            private ProgressBar mLinkupsLoadingBottomProgressBar;
            public ImageView mReloadBottomImageView;

            public ProgressViewHolder(View v) {
                super(v);
                mLinkupsLoadingBottomProgressBar = v.findViewById(R.id.activity_suggestedlinkupsactivity_bottomloader);
                mReloadBottomImageView = v.findViewById(R.id.activity_suggestedlinkupsactivity_bottomreload_imageview);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof SuggestedLinkupViewHolder) {
                ((SuggestedLinkupViewHolder) holder).mLinkupButton.setOnClickListener(clickListener);

                ((SuggestedLinkupViewHolder) holder).mLinkupPottNameTextView.setText("@"+SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(position).getPottName());
                ((SuggestedLinkupViewHolder) holder).mLinkupFullNameTextView.setText(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(position).getPottFullName());

                if(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(position).getSuggestionReason().trim().length() < 1){
                    ((SuggestedLinkupViewHolder) holder).mLinkupReasonTextView.setVisibility(View.GONE);
                } else {
                    ((SuggestedLinkupViewHolder) holder).mLinkupReasonTextView.setVisibility(View.VISIBLE);
                    ((SuggestedLinkupViewHolder) holder).mLinkupReasonTextView.setText(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(position).getSuggestionReason());
                }

                if(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(position).getVerifiedStatus() == 1){
                    ((SuggestedLinkupViewHolder) holder).mLinkupVerifiedBlueStatusIconImageView.setVisibility(View.INVISIBLE);
                    ((SuggestedLinkupViewHolder) holder).mLinkupVerifiedGreenStatusIconImageView.setVisibility(View.VISIBLE);
                } else if(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(position).getVerifiedStatus() == 2){
                    ((SuggestedLinkupViewHolder) holder).mLinkupVerifiedGreenStatusIconImageView.setVisibility(View.INVISIBLE);
                    ((SuggestedLinkupViewHolder) holder).mLinkupVerifiedBlueStatusIconImageView.setVisibility(View.VISIBLE);
                } else {
                    ((SuggestedLinkupViewHolder) holder).mLinkupVerifiedGreenStatusIconImageView.setVisibility(View.INVISIBLE);
                    ((SuggestedLinkupViewHolder) holder).mLinkupVerifiedBlueStatusIconImageView.setVisibility(View.INVISIBLE);
                }

                if(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(position).getPottType() == 2){
                    ((SuggestedLinkupViewHolder) holder).mLinkupBusinessAccountIconImageView.setVisibility(View.VISIBLE);
                } else {
                    ((SuggestedLinkupViewHolder) holder).mLinkupBusinessAccountIconImageView.setVisibility(View.INVISIBLE);
                }

                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(position).getPottPictureLink().trim().length() > 1){
                            Config.loadUrlImage(SuggestedLinkUpsActivity.this, true, SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().get(position).getPottPictureLink().trim(), ((SuggestedLinkupViewHolder) holder).mLinkupProfilePicImageView, 0, 60, 60);
                        } else {
                            Config.loadErrorImageView(SuggestedLinkUpsActivity.this, R.drawable.setprofilepicture_activity_imageholder_default_image, ((SuggestedLinkupViewHolder) holder).mLinkupProfilePicImageView, 60, 60);
                        }
                    }
                });
                imageLoaderThread.start();

            } else {
                if(REQUEST_HAS_STARTED){
                    ((ProgressViewHolder) holder).mReloadBottomImageView.setVisibility(View.INVISIBLE);
                    ((ProgressViewHolder) holder).mLinkupsLoadingBottomProgressBar.setVisibility(View.VISIBLE);
                } else {
                    ((ProgressViewHolder) holder).mLinkupsLoadingBottomProgressBar.setVisibility(View.INVISIBLE);
                    ((ProgressViewHolder) holder).mReloadBottomImageView.setVisibility(View.VISIBLE);
                }

                ((ProgressViewHolder) holder).mReloadBottomImageView.setOnClickListener(clickListener);

            }
        }

        @Override
        public void onViewRecycled(@NonNull final RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
            if (holder instanceof SuggestedLinkupViewHolder) {
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.loadErrorImageView(SuggestedLinkUpsActivity.this, R.drawable.setprofilepicture_activity_imageholder_default_image, ((SuggestedLinkupViewHolder) holder).mLinkupProfilePicImageView, 60, 60);
                    }
                });
                imageLoaderThread.start();

            }
        }

        @Override
        public int getItemCount() {
            return SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().size();
        }


    }

    public void getSuggestedLinkUps(final String language, final String allContactsPhoneNumbers, final String allContactsNames){

        Config.sePersistentNotification(getApplicationContext(), "149", "FishPott", getString(R.string.finding_new_people),  R.drawable.notification_icon);
        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    REQUEST_HAS_STARTED = true;
                    if (requestMakerStyle == 0) {
                        if(MyLifecycleHandler.isApplicationInForeground() && mReloadFreshImageView != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mReloadFreshImageView.setVisibility(View.INVISIBLE);
                                    mLinkupsLoadingProgressBar.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            networkResponse = "2";
                        }
                    } else if (requestMakerStyle == 2) {
                        sessionId = "";
                    }


                AndroidNetworking.post(Config.LINK_GET_SUGGESTED_LINKUPS)
                        .addBodyParameter("session_id", sessionId)
                        .addBodyParameter("log_id_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                        .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                        .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                        .addBodyParameter("all_contacts_names", allContactsNames)
                        .addBodyParameter("all_contacts_numbers", allContactsPhoneNumbers)
                        .addBodyParameter("language", language)
                        .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                        .setTag("suggestedlinkups_activity_fetchlinkups")
                        .setPriority(Priority.MEDIUM)
                        .build().getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        mNotificationManager.cancel(149);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray array = jsonObject.getJSONArray("data_returned");

                                JSONObject o = array.getJSONObject(0);
                                int myStatus = o.getInt("1");
                                final String myStatusMessage = o.getString("2");

                                if (myStatus == 1) {
                                    contactsSavedToServer = o.getBoolean("3");
                                    sessionId = o.getString("4");

                                    Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("5"));

                                    if(o.getBoolean("5")){
                                        Config.openActivity(SuggestedLinkUpsActivity.this, ConfirmPhoneNumberActivity.class, 1, 2, 1, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE));
                                        return;
                                    }

                                    if(o.getBoolean("9")){
                                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_GOVERNMENT_ID_VERIFICATION_NEEDED_STATUS, true);
                                        Config.openActivity(SuggestedLinkUpsActivity.this, GovernmentIDVerificationActivity.class, 1, 2, 0, "", "");
                                        return;
                                    }

                                    if (Config.checkUpdateAndForwardToUpdateActivity(SuggestedLinkUpsActivity.this, o.getInt("6"), o.getBoolean("7"), o.getString("8"), false)) {
                                        Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("8"));
                                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("7"));
                                        Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("6"));
                                        return;
                                    }
                                    JSONArray linkupsSuggestionsArray = jsonObject.getJSONArray("linkups_suggestions_returned");
                                    if(linkupsSuggestionsArray.length() > 0){

                                        if(requestMakerStyle == 2){
                                            SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().clear();
                                            if(MyLifecycleHandler.isApplicationVisible() && mSuggestedLinkupsRecyclerView != null) {
                                                mSuggestedLinkupsRecyclerView.getAdapter().notifyDataSetChanged();
                                            }
                                        } else {
                                            if(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData() != null){
                                                if(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().size() > 0){
                                                    SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().remove(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().size()-1);
                                                    if(MyLifecycleHandler.isApplicationVisible() && mSuggestedLinkupsRecyclerView != null) {
                                                        mSuggestedLinkupsRecyclerView.getAdapter().notifyItemRemoved(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().size()-1);
                                                    }
                                                }
                                            }
                                        }
                                        for (int i = 0; i<=linkupsSuggestionsArray.length(); i++){
                                            SuggestedLinkUpsModel mine1 = new SuggestedLinkUpsModel();
                                            if(i<linkupsSuggestionsArray.length()){
                                                final JSONObject k = linkupsSuggestionsArray.getJSONObject(i);
                                                mine1.setPottName(k.getString("1"));
                                                mine1.setPottFullName(k.getString("2"));
                                                mine1.setPottPictureLink(k.getString("3"));
                                                mine1.setVerifiedStatus(k.getInt("4"));
                                                mine1.setInvestorId(k.getString("5"));
                                                mine1.setPottType(k.getInt("6"));
                                                mine1.setSuggestionReason(k.getString("7"));

                                                if(!k.getString("3").trim().equalsIgnoreCase("")){
                                                    final String thisImageurl = k.getString("3").trim();
                                                    imageLoaderThread = new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Config.loadUrlImage(SuggestedLinkUpsActivity.this, false, thisImageurl, null, 0, 60, 60);
                                                        }
                                                    });
                                                    imageLoaderThread.start();


                                                }
                                            } else {
                                                mine1 = null;
                                            }
                                            SuggestedLinkupsListDataGenerator.addOneSuggestedLinkupsData(mine1);
                                            if(MyLifecycleHandler.isApplicationVisible() && mSuggestedLinkupsRecyclerView != null) {
                                                mSuggestedLinkupsRecyclerView.getAdapter().notifyItemInserted(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().size());
                                            }
                                        }
                                        someLinkupshaveBeenFetched = true;
                                        if(MyLifecycleHandler.isApplicationInForeground() && mReloadFreshImageView != null) {
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(mMainSwipeRefreshLayout.isRefreshing()){
                                                        mMainSwipeRefreshLayout.setRefreshing(false);
                                                    }
                                                    if(requestMakerStyle == 0){
                                                        mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                                                        mReloadFreshImageView.setVisibility(View.INVISIBLE);
                                                    }

                                                    mSuggestedLinkupsRecyclerView.setVisibility(View.VISIBLE);
                                                }
                                            });
                                        } else {
                                            networkResponse = "1";
                                        }
                                    } else {
                                        if(MyLifecycleHandler.isApplicationInForeground()) {
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    tuningViewsOnError(1, myStatusMessage);
                                                }
                                            });
                                        } else {
                                            networkResponse = myStatusMessage;
                                        }
                                    }
                                } else if(myStatus == 0){
                                    Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS, 1);
                                    Config.openActivity(SuggestedLinkUpsActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                                    return;
                                } else {
                                    if(MyLifecycleHandler.isApplicationInForeground()) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                tuningViewsOnError(1, myStatusMessage);
                                            }
                                        });
                                    } else {
                                        networkResponse = myStatusMessage;
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                if(MyLifecycleHandler.isApplicationInForeground()) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            tuningViewsOnError(0, "");
                                        }
                                    });
                                } else {
                                    networkResponse = "00";
                                }
                            }
                            REQUEST_HAS_STARTED =  false;
                    }

                    @Override
                    public void onError(ANError anError) {
                        mNotificationManager.cancel(149);
                        if(MyLifecycleHandler.isApplicationInForeground()){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    tuningViewsOnError(0, "");
                                }
                            });
                            REQUEST_HAS_STARTED =  false;
                        } else {
                            networkResponse = "00";
                        }
                    }
                });
    }


    public void addLinkUp(final String language, final String linkupId){
        AndroidNetworking.post(Config.LINK_ADD_LINKUPS)
                .addBodyParameter("log_id_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID))
                .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                .addBodyParameter("linkup_id", linkupId)
                .addBodyParameter("language", language)
                .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                .setTag("suggestedlinkups_activity_addlinkup")
                .setPriority(Priority.MEDIUM)
                .build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {}
            @Override
            public void onError(ANError anError) {}
        });
    }


    private void makeRequest(){
        REQUEST_HAS_STARTED =  true;

        if (!Config.permissionsHaveBeenGranted(getApplicationContext(), permissions)) {
            cancelListenerActive1 = Config.showDialogType3(SuggestedLinkUpsActivity.this, cancelListenerActive1, true);
        } else {
            suggestedLinkupsFetchThread2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(!contactsSavedToServer){
                        if(allPhonesNumbers.equalsIgnoreCase("") && allPhonesNames.equalsIgnoreCase("")){
                            String[] allContacts = Config.getContactList(SuggestedLinkUpsActivity.this);
                            allPhonesNumbers = allContacts[0];
                            allPhonesNames = allContacts[1];
                        }
                    } else {
                        allPhonesNumbers = "";
                        allPhonesNames = "";
                    }
                    if(Connectivity.isConnected(SuggestedLinkUpsActivity.this)){
                        getSuggestedLinkUps(LocaleHelper.getLanguage(SuggestedLinkUpsActivity.this), allPhonesNumbers, allPhonesNames);
                    } else {
                        tuningViewsOnError(0, "");
                        REQUEST_HAS_STARTED =  false;
                    }
                }
            });
            suggestedLinkupsFetchThread2.start();
        }
    }

    private void tuningViewsOnError(final int showToast, final String statusMsg){
        if (MyLifecycleHandler.isApplicationInForeground() && mReloadFreshImageView != null) {
            REQUEST_HAS_STARTED =  false;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(mMainSwipeRefreshLayout.isRefreshing()){
                        mMainSwipeRefreshLayout.setRefreshing(false);
                    }
                    if(requestMakerStyle == 0){
                        if(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().isEmpty()){
                            mLinkupsLoadingProgressBar.setVisibility(View.INVISIBLE);
                            mSuggestedLinkupsRecyclerView.setVisibility(View.INVISIBLE);
                            mReloadFreshImageView.setVisibility(View.VISIBLE);
                        }
                    } else if(requestMakerStyle == 1){
                        if(!SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().isEmpty()){
                            mSuggestedLinkupsRecyclerView.getAdapter().notifyItemChanged(SuggestedLinkupsListDataGenerator.getAllSuggestedLinkUpsData().size()-1);
                        }
                    } else if(requestMakerStyle == 2){
                        if(!someLinkupshaveBeenFetched){
                            if(showToast == 1){
                                Config.showToastType1(SuggestedLinkUpsActivity.this, statusMsg);
                            }
                        }
                    }
                }
            });
        }
    }

}
