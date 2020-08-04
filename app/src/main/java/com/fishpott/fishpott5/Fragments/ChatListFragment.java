package com.fishpott.fishpott5.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fishpott.fishpott5.Activities.ContactsActivity;
import com.fishpott.fishpott5.Activities.MessengerActivity;
import com.fishpott.fishpott5.Adapters.ChatList_DatabaseAdapter;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.Chats_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.Models.Chat_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Views.CircleImageView;

import me.leolin.shortcutbadger.ShortcutBadger;

public class ChatListFragment extends Fragment  implements View.OnClickListener {

    public static RecyclerView mChatsRecyclerView;
    private ConstraintLayout mNewChatIconHolderConstraintlayout;
    private Thread dbThread = null, imageLoaderThread = null;
    private LinearLayoutManager mLayoutManager;
    private View view;

    public ChatListFragment() {}

    public static ChatListFragment newInstance() {
        ChatListFragment fragment = new ChatListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mChatsRecyclerView = view.findViewById(R.id.activity_main_old_chats_fragment_recyclerview);
        mNewChatIconHolderConstraintlayout = view.findViewById(R.id.new_chat_bubble_contraintlayout);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mChatsRecyclerView.setItemViewCacheSize(20);
        mChatsRecyclerView.setDrawingCacheEnabled(true);
        mChatsRecyclerView.setHasFixedSize(true);
        mChatsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mChatsRecyclerView.setLayoutManager(mLayoutManager);
        mChatsRecyclerView.setAdapter(new RecyclerViewAdapter());

        mNewChatIconHolderConstraintlayout.setOnClickListener(this);

        dbThread = new Thread(new Runnable() {
            @Override
            public void run() {
                getOldChats(getActivity().getApplicationContext());
            }
        });
        dbThread.start();

        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.CHAT_NOTIFICATION_UNREAD_COUNT, 0);
        Config.setSharedPreferenceInt(getActivity().getApplicationContext(), Config.NOTIFICATION_UNREAD_COUNT, 0);
        ShortcutBadger.removeCount(getActivity().getApplicationContext());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        Config.freeMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(dbThread != null){
            dbThread.interrupt();
            dbThread = null;
        }
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_chatlist_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }

    public static void getOldChats(Context context) {
        if(Chats_ListDataGenerator.getAllData().size() > 0){
            Chats_ListDataGenerator.getAllData().clear();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(MyLifecycleHandler.isApplicationInForeground() && mChatsRecyclerView != null) {
                        mChatsRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                }
            });
        }

            ChatList_DatabaseAdapter chatList_databaseAdapter = new ChatList_DatabaseAdapter(context);
            chatList_databaseAdapter.openDatabase();
            Cursor cursor = chatList_databaseAdapter.getAllRows();
            if (cursor.moveToLast()) {
                do {
                    // Process the data:
                    Chat_Model chat_model = new Chat_Model();
                    chat_model.setRowId(cursor.getLong(chatList_databaseAdapter.COL_ROWID));
                    chat_model.setChatId(cursor.getString(chatList_databaseAdapter.COL_CHAT_ID));
                    chat_model.setPottPic(cursor.getString(chatList_databaseAdapter.COL_POTT_PIC));
                    chat_model.setPottName(cursor.getString(chatList_databaseAdapter.COL_POTTNAME));
                    chat_model.setLastChatDate(cursor.getString(chatList_databaseAdapter.COL_LAST_CHAT_DATE));
                    chat_model.setLastChatMessage(cursor.getString(chatList_databaseAdapter.COL_LAST_CHAT_MESSAGE));
                    chat_model.setReadStatus(cursor.getInt(chatList_databaseAdapter.COL_READ_STATUS));

                    //ADDING STORY OBJECT TO LIST
                    Chats_ListDataGenerator.addOneData(chat_model);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(MyLifecycleHandler.isApplicationInForeground() && mChatsRecyclerView != null) {
                                mChatsRecyclerView.getAdapter().notifyItemInserted(Chats_ListDataGenerator.getAllData().size());
                            }
                        }
                    });

                } while (cursor.moveToPrevious());
            }

            cursor.close();
            chatList_databaseAdapter.closeDatabase();

        if(Chats_ListDataGenerator.getAllData().size() < 1){
            Chat_Model null_chat_model = new Chat_Model();
            null_chat_model.setRowId(0);
            Chats_ListDataGenerator.addOneData(null_chat_model);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(MyLifecycleHandler.isApplicationInForeground() && mChatsRecyclerView != null) {
                        mChatsRecyclerView.getAdapter().notifyItemInserted(Chats_ListDataGenerator.getAllData().size());
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.new_chat_bubble_contraintlayout || v.getId() == R.id.new_chat_bubble_imageview){
            Config.openActivity(getActivity(), ContactsActivity.class, 1, 0, 0, "", "");
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemViewType(int position) {
            if(Chats_ListDataGenerator.getAllData().get(position).getRowId() == 0){
                return 0;
            }
            return 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            if (viewType == 0) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_invite_chat_contacts_activity, parent, false);
                vh = new InviteViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_old_chat_list_fragment, parent, false);
                vh = new ViewHolder(v);
            }

            return vh;
        }


        public class InviteViewHolder extends RecyclerView.ViewHolder {
            private ConstraintLayout mRootViewInviteConstraintLayout;
            private TextView mStartChartTextView;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public InviteViewHolder(View v) {
                super(v);
                mStartChartTextView = v.findViewById(R.id.invite_textview);
                mRootViewInviteConstraintLayout = v.findViewById(R.id.invite_chat_list_root_constraintlayout);

                mRootViewInviteConstraintLayout.setOnClickListener(innerClickListener);
            }
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            private ConstraintLayout mChatListRootConstraintlayout;
            private CircleImageView mChatPottPicCicleImageView;
            private TextView mPottNameTextView, mLastChatDateTextView, mLastChatMessageTextView;
            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public ViewHolder(View v) {
                super(v);
                mChatListRootConstraintlayout = itemView.findViewById(R.id.chat_list_root_constraintlayout);
                mChatPottPicCicleImageView = itemView.findViewById(R.id.chat_pott_picture_circleimageview);
                mPottNameTextView = itemView.findViewById(R.id.chat_pottname_textview);
                mLastChatDateTextView = itemView.findViewById(R.id.chat_date_textview);
                mLastChatMessageTextView = itemView.findViewById(R.id.last_message_text_textview);

                mChatListRootConstraintlayout.setOnClickListener(innerClickListener);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

            if (holder instanceof ViewHolder) {
                if(Chats_ListDataGenerator.getAllData().get(position).getReadStatus() == Config.UNREAD){
                    ((ViewHolder) holder).mChatListRootConstraintlayout.setBackground(getResources().getDrawable(R.drawable.grey_background_pressed_darker, null));
                } else {
                    ((ViewHolder) holder).mChatListRootConstraintlayout.setBackground(getResources().getDrawable(R.drawable.white_background_pressed_darker, null));
                }

                ((ViewHolder) holder).mPottNameTextView.setText(Chats_ListDataGenerator.getAllData().get(position).getPottName());
                ((ViewHolder) holder).mLastChatDateTextView.setText(Chats_ListDataGenerator.getAllData().get(position).getLastChatDate());

                if(Chats_ListDataGenerator.getAllData().get(position).getLastChatMessage().trim().length() > 25){
                    ((ViewHolder) holder).mLastChatMessageTextView.setText(Chats_ListDataGenerator.getAllData().get(position).getLastChatMessage().trim().substring(0,25) + "...");
                } else {
                    ((ViewHolder) holder).mLastChatMessageTextView.setText(Chats_ListDataGenerator.getAllData().get(position).getLastChatMessage());
                }

                // LOADING A PROFILE PICTURE IF URL EXISTS
                imageLoaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(Chats_ListDataGenerator.getAllData().get(position).getPottPic().trim().equalsIgnoreCase("fp")){
                            Config.loadErrorImageView(getActivity(), R.drawable.fishpott_splash_icon, ((ViewHolder) holder).mChatPottPicCicleImageView, 60, 60);
                        } else if (Chats_ListDataGenerator.getAllData().get(position).getPottPic().trim().length() > 1) {
                            Config.loadUrlImage(getActivity(), true, Chats_ListDataGenerator.getAllData().get(position).getPottPic().trim(), ((ViewHolder) holder).mChatPottPicCicleImageView, 0, 60, 60);
                        } else {
                            Config.loadErrorImageView(getActivity(), R.drawable.setprofilepicture_activity_imageholder_default_image, ((ViewHolder) holder).mChatPottPicCicleImageView, 60, 60);
                        }
                    }
                });
                imageLoaderThread.start();
            } else {
                ((InviteViewHolder) holder).mRootViewInviteConstraintLayout.setBackground(getResources().getDrawable(R.drawable.white_background_pressed_darker, null));
                ((InviteViewHolder) holder).mStartChartTextView.setText(getActivity().getResources().getString(R.string.start_a_conversation));
            }


        }

        @Override
        public int getItemCount() {
            return Chats_ListDataGenerator.getAllData().size();
        }

    }


    private void allOnClickHandlers(View v, int position) {
        if (v.getId() == R.id.chat_list_root_constraintlayout) {

            String[] chatData = {
                    Chats_ListDataGenerator.getAllData().get(position).getChatId(),
                    Chats_ListDataGenerator.getAllData().get(position).getPottName(),
                    Chats_ListDataGenerator.getAllData().get(position).getPottPic()
            };
            ChatList_DatabaseAdapter chatListDatabaseAdapter = new ChatList_DatabaseAdapter(getActivity().getApplicationContext());
            // OPENING THE STORIES DATABASE
            chatListDatabaseAdapter.openDatabase();
            chatListDatabaseAdapter.updateRow(Chats_ListDataGenerator.getAllData().get(position).getRowId(),  1, ChatList_DatabaseAdapter.KEY_READ_STATUS, "", Config.READ);
            chatListDatabaseAdapter.closeDatabase();
            Chats_ListDataGenerator.getAllData().get(position).setReadStatus(1);
            mChatsRecyclerView.getAdapter().notifyItemChanged(position);
            mChatsRecyclerView.getAdapter().notifyItemRangeChanged(position, Chats_ListDataGenerator.getAllData().size());
            Config.openActivity4(getActivity(), MessengerActivity.class, 1, 0, 1, "CHAT_INFO", chatData);

        } else if (v.getId() == R.id.invite_chat_list_root_constraintlayout) {
            Config.openActivity(getActivity(), ContactsActivity.class, 1, 0, 0, "", "");
        }
    }

}
