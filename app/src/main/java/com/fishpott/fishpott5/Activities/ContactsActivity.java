package com.fishpott.fishpott5.Activities;

import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fishpott.fishpott5.Adapters.Contacts_DatabaseAdapter;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.Contacts_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.Chat_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Services.NewsFetcherAndPreparerService;
import com.fishpott.fishpott5.Views.CircleImageView;

public class ContactsActivity extends AppCompatActivity implements View.OnClickListener {

    private SwipeRefreshLayout mMainSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.OnScrollListener scrollListener;
    private LinearLayoutManager mLayoutManager;
    private ImageView mBackImageView;
    private Thread requestThread = null, imageLoaderThread = null;
    private String networkResponse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        mBackImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mMainSwipeRefreshLayout = findViewById(R.id.activity_suggestedlinkupsactivity_swiperefreshayout);
        mRecyclerView = findViewById(R.id.activity_suggestedlinkupsactivity_recyclerview);

        //SETTING UP RECYCLERVIEW
        mLayoutManager = new LinearLayoutManager(ContactsActivity.this);

        scrollListener = new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };

        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(scrollListener);
        mRecyclerView.setAdapter(new RecyclerViewAdapter(this));

        mBackImageView.setOnClickListener(this);


        mMainSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        requestThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                NewsFetcherAndPreparerService.fetchMyContacts(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()));
                            }
                        });
                        requestThread.start();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        makeRequest();
                                    }
                                });
                                requestThread.start();
                            }
                        }, 6000);
                    }
                }
        );

        requestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                makeRequest();}
        });
        requestThread.start();

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == mBackImageView.getId()){
            onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mMainSwipeRefreshLayout = findViewById(R.id.activity_suggestedlinkupsactivity_swiperefreshayout);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMainSwipeRefreshLayout = null;
        mBackImageView = null;
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMainSwipeRefreshLayout = null;
        mRecyclerView = null;
        scrollListener = null;
        mLayoutManager = null;
        mBackImageView = null;
        networkResponse  = null;
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        if(requestThread != null){
            requestThread.interrupt();
            requestThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_contacts_activity));
        Config.freeMemory();
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter{

        View.OnClickListener clickListener;

        public RecyclerViewAdapter(View.OnClickListener listener) {
            this.clickListener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0){
                return 0;
            } else {
                return 1;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            if (viewType == 0) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_invite_chat_contacts_activity, parent, false);
                vh = new InviteViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_new_chat_contacts_activity, parent, false);
                vh = new ChatViewHolder(v);
            }

            return vh;
        }

        public class ChatViewHolder extends RecyclerView.ViewHolder {
            private TextView mChatPottNameTextView, mChatFullNameTextView;
            private CircleImageView mChatpProfilePicImageView;
            private ConstraintLayout mRootViewConstraintLayout;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public ChatViewHolder(View v) {
                super(v);
                mRootViewConstraintLayout = itemView.findViewById(R.id.chat_list_root_constraintlayout);
                mChatpProfilePicImageView = itemView.findViewById(R.id.chat_pott_picture_circleimageview);
                mChatPottNameTextView = itemView.findViewById(R.id.chat_pott_name_textview);
                mChatFullNameTextView = itemView.findViewById(R.id.chat_full_name_textview);
                mRootViewConstraintLayout.setOnClickListener(innerClickListener);

            }
        }

        public class InviteViewHolder extends RecyclerView.ViewHolder {
            private ConstraintLayout mRootViewInviteConstraintLayout;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public InviteViewHolder(View v) {
                super(v);
                mRootViewInviteConstraintLayout = v.findViewById(R.id.invite_chat_list_root_constraintlayout);

                mRootViewInviteConstraintLayout.setOnClickListener(innerClickListener);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ChatViewHolder) {
                ((ChatViewHolder) holder).mRootViewConstraintLayout.setBackground(getResources().getDrawable(R.drawable.white_background_pressed_darker, null));
                ((ChatViewHolder) holder).mChatPottNameTextView.setText("@"+Contacts_ListDataGenerator.getAllData().get(position).getPottName());
                ((ChatViewHolder) holder).mChatFullNameTextView.setText(Contacts_ListDataGenerator.getAllData().get(position).getFullName());

                    imageLoaderThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(Contacts_ListDataGenerator.getAllData().get(position).getPottPic().trim().length() > 1){
                                Config.loadUrlImage(ContactsActivity.this, true, Contacts_ListDataGenerator.getAllData().get(position).getPottPic().trim(), ((ChatViewHolder) holder).mChatpProfilePicImageView, 0, 60, 60);
                            } else {
                                Config.loadErrorImageView(ContactsActivity.this, R.drawable.setprofilepicture_activity_imageholder_default_image, ((ChatViewHolder) holder).mChatpProfilePicImageView, 60, 60);
                            }
                        }
                    });
                    imageLoaderThread.start();


            } else {
                ((InviteViewHolder) holder).mRootViewInviteConstraintLayout.setBackground(getResources().getDrawable(R.drawable.white_background_pressed_darker, null));
                ((InviteViewHolder) holder).mRootViewInviteConstraintLayout.setBackground(getResources().getDrawable(R.drawable.white_background_pressed_darker, null));
            }
        }

        @Override
        public int getItemCount() {
            return Contacts_ListDataGenerator.getAllData().size();
        }


    }


    private void allOnClickHandlers(View v, int position) {
        if(v.getId() == R.id.chat_list_root_constraintlayout){
            String[] chatData = {
                    Contacts_ListDataGenerator.getAllData().get(position).getChatId(),
                    Contacts_ListDataGenerator.getAllData().get(position).getPottName(),
                    Contacts_ListDataGenerator.getAllData().get(position).getPottPic()
            };
            Config.openActivity4(ContactsActivity.this, MessengerActivity.class, 1, 0, 1, "CHAT_INFO", chatData);
        } else if(v.getId() == R.id.invite_chat_list_root_constraintlayout){
            Config.openMessageApp(ContactsActivity.this, "", getResources().getString(R.string.lets_connect_through_socio_commerce_download_fishpott_and_we_can_trade_transfer_investments_this_is_the_link_https_play_google_com_store_apps_details_id_com_fishpott_fishpott5_hl_en));
        }
    }

    private void makeRequest(){
        if(Contacts_ListDataGenerator.getAllData().size() > 0){
            Contacts_ListDataGenerator.getAllData().clear();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(!ContactsActivity.this.isFinishing() && ContactsActivity.this != null){
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    } else {
                        return;
                    }
                }
            });
        }
        Chat_Model null_chat_model = new Chat_Model();
        null_chat_model.setRowId(0);
        Contacts_ListDataGenerator.addOneData(null_chat_model);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(!ContactsActivity.this.isFinishing() && ContactsActivity.this != null){
                    mRecyclerView.getAdapter().notifyItemInserted(Contacts_ListDataGenerator.getAllData().size());
                } else {
                    return;
                }
            }
        });

        if (ContactsActivity.this.isFinishing() && ContactsActivity.this != null) {
            //CREATING THE NEWS STORIES DATABASE OBJECT
            Contacts_DatabaseAdapter contacts_databaseAdapter = new Contacts_DatabaseAdapter(getApplicationContext());
            // OPENING THE STORIES DATABASE
            contacts_databaseAdapter.openDatabase();
            //GETTING ALL NEWS IN LOCAL DATABASE AND POINTING ONE-BY-ONE WITH CURSOR
            Cursor cursor = contacts_databaseAdapter.getAllRows();
            if (cursor.moveToLast()) {
                do {
                    // Process the data:
                    Chat_Model chat_model = new Chat_Model();
                    chat_model.setRowId(cursor.getLong(contacts_databaseAdapter.COL_ROWID));
                    chat_model.setChatId(cursor.getString(contacts_databaseAdapter.COL_CHAT_ID));
                    chat_model.setPottPic(cursor.getString(contacts_databaseAdapter.COL_POTT_PIC));
                    chat_model.setPottName(cursor.getString(contacts_databaseAdapter.COL_POTTNAME));
                    chat_model.setFullName(cursor.getString(contacts_databaseAdapter.COL_FULLNAME));

                    Contacts_ListDataGenerator.addOneData(chat_model);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(!ContactsActivity.this.isFinishing() && ContactsActivity.this != null){
                                mRecyclerView.getAdapter().notifyItemInserted(Contacts_ListDataGenerator.getAllData().size());
                            } else {
                                return;
                            }
                        }
                    });

                } while (cursor.moveToPrevious());
            }

            cursor.close();
            contacts_databaseAdapter.closeDatabase();
            cursor = null;
            contacts_databaseAdapter = null;

        }

        if(Contacts_ListDataGenerator.getAllData().size() < 2) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(!ContactsActivity.this.isFinishing() && ContactsActivity.this != null) {
                        if (!Config.getSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CONTACTS_ACTIVITY_DIALOG_SHOWN)) {
                            Config.showDialogType1(ContactsActivity.this, "", getString(R.string.no_contacts_found_after_closing_this_dialog_please_swipe_down_to_refresh_if_no_contacts_are_still_showing_try_again_later_the_system_updates_the_list_periodically), "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CONTACTS_ACTIVITY_DIALOG_SHOWN, true);
                        }
                    }
                }
            });
            NewsFetcherAndPreparerService.fetchMyContacts(getApplicationContext(), LocaleHelper.getLanguage(getApplicationContext()));
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(!ContactsActivity.this.isFinishing() && ContactsActivity.this != null) {
                    if (mMainSwipeRefreshLayout.isRefreshing()) {
                        mMainSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });
    }

}
