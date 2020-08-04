package com.fishpott.fishpott5.Activities;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Adapters.ChatList_DatabaseAdapter;
import com.fishpott.fishpott5.Adapters.ChatMessages_DatabaseAdapter;
import com.fishpott.fishpott5.Fragments.ChatListFragment;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.ListDataGenerators.ConversationMessages_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.MessageModel;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Views.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessengerActivity extends AppCompatActivity implements View.OnClickListener {

    private Timer mTimer = null;
    private CircleImageView mReceiverPottPic;
    private TextView mReceiverPottName;
    private ImageView mBackImageView, mSendIconImageView;
    private EditText mMessageEditText;
    private RecyclerView.OnScrollListener scrollListener;
    private LinearLayoutManager mLayoutManager;
    public static RecyclerView mConversationRecyclerView;
    public static String chatID = "";
    private String receiverPottName = "", receiverPottPic = "", newsId = "";
    private Thread networkRequestThread = null, imageLoaderThread = null;
    private Boolean REQUEST_HAS_STARTED = false;
    public static List<Integer> allLocallyShownMessagesListPositions = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        if(getIntent().getExtras() != null) {
            String[] chatInfo =(String[]) getIntent().getExtras().get("CHAT_INFO");
            chatID = chatInfo[0];
            receiverPottName = chatInfo[1];
            receiverPottPic = chatInfo[2];
            if(chatInfo.length > 3){
                newsId = chatInfo[3];
            }
        } else {
            finish();
        }

        if(chatID.trim().equalsIgnoreCase("") || receiverPottName.trim().equalsIgnoreCase("")){
            finish();
        }

        mReceiverPottPic = findViewById(R.id.chat_pott_picture_circleimageview);
        mReceiverPottName = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_title_textview);
        mBackImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mSendIconImageView = findViewById(R.id.sendicon_contacts_activity_imageview);
        mMessageEditText = findViewById(R.id.messagebox_contacts_activity_edittext);
        mConversationRecyclerView = findViewById(R.id.chat_messages_recyclerview);


        // LOADING A PROFILE PICTURE IF URL EXISTS
        imageLoaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(receiverPottPic.trim().equalsIgnoreCase("fp")){
                    Config.loadErrorImageView(MessengerActivity.this, R.drawable.fishpott_splash_icon, mReceiverPottPic, 60, 60);
                } else if(receiverPottPic.trim().length() > 1){
                    Config.loadUrlImage(MessengerActivity.this, true, receiverPottPic.trim(), mReceiverPottPic, 0, 60, 60);
                }
            }
        });
        imageLoaderThread.start();
        mReceiverPottName.setText(receiverPottName);

        //SETTING UP RECYCLERVIEW
        mLayoutManager = new LinearLayoutManager(MessengerActivity.this);

        mConversationRecyclerView.setItemViewCacheSize(20);
        mConversationRecyclerView.setDrawingCacheEnabled(true);
        mConversationRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mConversationRecyclerView.setLayoutManager(mLayoutManager);
        mConversationRecyclerView.setAdapter(new RecyclerViewAdapter(this));

        mBackImageView.setOnClickListener(this);
        mReceiverPottPic.setOnClickListener(this);
        mReceiverPottName.setOnClickListener(this);
        mSendIconImageView.setOnClickListener(this);

        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }

        networkRequestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                makeRequest();
                mTimer.scheduleAtFixedRate(new MessengerTimeDisplayTimerTask(), 0, 1000);
            }
        });
        networkRequestThread.start();

        if(!newsId.trim().equalsIgnoreCase("")){
            mMessageEditText.setText(getResources().getString(R.string.i_want_to_report_a_news_with_id) + " " + newsId);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == mBackImageView.getId()){
            onBackPressed();
        } else if(view.getId() == mReceiverPottPic.getId() || view.getId() == mReceiverPottName.getId()){
            Config.openActivity(MessengerActivity.this, ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", receiverPottName);
        } else if(view.getId() == mSendIconImageView.getId()){
            if(mMessageEditText.getText().toString().trim().length() > 0){
                String message = mMessageEditText.getText().toString().trim();
                view.startAnimation(AnimationUtils.loadAnimation(MessengerActivity.this, R.anim.main_activity_onclick_icon_anim));
                sendMessage(chatID, receiverPottName, ConversationMessages_ListDataGenerator.getAllData().size(), message, LocaleHelper.getLanguage(getApplicationContext()));
                mSendIconImageView.startAnimation(AnimationUtils.loadAnimation(MessengerActivity.this, R.anim.main_activity_onclick_icon_anim));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiverPottPic = findViewById(R.id.chat_pott_picture_circleimageview);
        mReceiverPottName = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_title_textview);
        mBackImageView = findViewById(R.id.activity_suggestedlinkupsactivity_constraintlayout2_forward_imageview);
        mSendIconImageView = findViewById(R.id.sendicon_contacts_activity_imageview);
        mMessageEditText = findViewById(R.id.messagebox_contacts_activity_edittext);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        mTimer.cancel();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(networkRequestThread != null){
            networkRequestThread.interrupt();
            networkRequestThread = null;
        }
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_messenger_activity));
        Config.freeMemory();
    }

    // THE ADAPTER FOR THE RECYCLER-VIEW TO SPECIFY HOW THE ITEMS ON THE RECYCLERVIEW WILL BEHAVE
    private class RecyclerViewAdapter extends RecyclerView.Adapter{

        //DEFINING A CLICK LISTENER FOR THE RECYCLERVIEW
        View.OnClickListener clickListener;

        public RecyclerViewAdapter(View.OnClickListener listener) {
            this.clickListener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            if(ConversationMessages_ListDataGenerator.getAllData().get(position).getSenderPottName().trim().equalsIgnoreCase(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))){
                if(!ConversationMessages_ListDataGenerator.getAllData().get(position).getMessageImage().trim().equalsIgnoreCase("")){
                    return 0;
                }
                return 1;
            } else {
                if(!ConversationMessages_ListDataGenerator.getAllData().get(position).getMessageImage().trim().equalsIgnoreCase("")){
                    return 2;
                }
                return 3;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v;
            if (viewType == 0) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_conversation_sender_message_just_text, parent, false);
                vh = new MessageWithJustText(v);
            } else if (viewType == 1) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_conversation_sender_message_just_text, parent, false);
                vh = new MessageWithJustText(v);
            } else if (viewType == 2) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_conversation_receiver_message_just_text, parent, false);
                vh = new MessageWithJustText(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_conversation_receiver_message_just_text, parent, false);
                vh = new MessageWithJustText(v);
            }

            return vh;
        }

        public class MessageWithJustText extends RecyclerView.ViewHolder {
            private TextView mMessageTextTextView, mReadMoreTextTextView, mDateOfMessageTextView;
            private ImageView mUnsentMessageImageView;
            private ConstraintLayout mRootViewConstraintLayout;

            public MessageWithJustText(View v) {
                super(v);

                mRootViewConstraintLayout = itemView.findViewById(R.id.root_of_justtext_chat_message);
                mUnsentMessageImageView = itemView.findViewById(R.id.failed_message_icon_imageview);
                mMessageTextTextView = itemView.findViewById(R.id.messagetext_messenger_activity_textview);
                mReadMoreTextTextView = itemView.findViewById(R.id.read_more_messenger_activity_textview);
                mDateOfMessageTextView = itemView.findViewById(R.id.message_date_messenger_activity_textview);


            }
        }

        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof MessageWithJustText) {
                //SETTING ON-CLICK LISTENER
                ((MessageWithJustText) holder).mRootViewConstraintLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Connectivity.isConnected(getApplicationContext()) && ConversationMessages_ListDataGenerator.getAllData().get(position).getMessageStatus() == Config.UNSENT && ConversationMessages_ListDataGenerator.getAllData().get(position).getSenderPottName().equalsIgnoreCase(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))){
                            sendMessage(chatID, receiverPottName, position, ConversationMessages_ListDataGenerator.getAllData().get(position).getMessageText(), LocaleHelper.getLanguage(getApplicationContext()));
                            ConversationMessages_ListDataGenerator.getAllData().remove(position);
                            mConversationRecyclerView.getAdapter().notifyItemRemoved(position);
                            mConversationRecyclerView.getAdapter().notifyItemRangeChanged(position, ConversationMessages_ListDataGenerator.getAllData().size());
                        }
                    }
                });
                ((MessageWithJustText) holder).mDateOfMessageTextView.setText(ConversationMessages_ListDataGenerator.getAllData().get(position).getMessageTime());

                if(ConversationMessages_ListDataGenerator.getAllData().get(position).getMessageText().trim().length() > 400){
                    ((MessageWithJustText) holder).mMessageTextTextView.setText(ConversationMessages_ListDataGenerator.getAllData().get(position).getMessageText().trim().substring(0,300) + "...");
                    ((MessageWithJustText) holder).mReadMoreTextTextView.setVisibility(View.VISIBLE);
                } else {
                    ((MessageWithJustText) holder).mMessageTextTextView.setText(ConversationMessages_ListDataGenerator.getAllData().get(position).getMessageText());
                    ((MessageWithJustText) holder).mReadMoreTextTextView.setVisibility(View.INVISIBLE);
                }

                ((MessageWithJustText) holder).mReadMoreTextTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MessageWithJustText) holder).mMessageTextTextView.setText(ConversationMessages_ListDataGenerator.getAllData().get(position).getMessageText().trim());

                    }
                });

                if(ConversationMessages_ListDataGenerator.getAllData().get(position).getSenderPottName().trim().equalsIgnoreCase(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME)) && ConversationMessages_ListDataGenerator.getAllData().get(position).getMessageStatus() == Config.UNSENT){
                    ((MessageWithJustText) holder).mUnsentMessageImageView.setVisibility(View.VISIBLE);
                } else {
                    ((MessageWithJustText) holder).mUnsentMessageImageView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return ConversationMessages_ListDataGenerator.getAllData().size();
        }


    }


    private void makeRequest(){
        // FOR A FETCH OF CONTACTS IN BACKROUND HERE
        if(ConversationMessages_ListDataGenerator.getAllData().size() > 0){
            ConversationMessages_ListDataGenerator.getAllData().clear();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mConversationRecyclerView.getAdapter().notifyDataSetChanged();
                }
            });
        }

        // populate the message from the cursor
        if (MessengerActivity.this != null) {
            //CREATING THE NEWS STORIES DATABASE OBJECT
            ChatMessages_DatabaseAdapter chatMessages_databaseAdapter = new ChatMessages_DatabaseAdapter(getApplicationContext());
            // OPENING THE STORIES DATABASE
            chatMessages_databaseAdapter.openDatabase();
            //GETTING ALL NEWS IN LOCAL DATABASE AND POINTING ONE-BY-ONE WITH CURSOR
            Cursor cursor = chatMessages_databaseAdapter.getSpecificRowsUsingWhereValueAsString(ChatMessages_DatabaseAdapter.KEY_CHAT_ID + " = '" + chatID + "'", ChatMessages_DatabaseAdapter.KEY_CHAT_ID + " DESC", false);

            if (cursor.moveToLast()) {
                do {
                    // Process the data:
                    MessageModel messageModel = new MessageModel();
                    messageModel.setRowId(cursor.getLong(chatMessages_databaseAdapter.COL_ROWID));
                    messageModel.setChatId(cursor.getString(chatMessages_databaseAdapter.COL_CHAT_ID));
                    messageModel.setSenderPottName(cursor.getString(chatMessages_databaseAdapter.COL_SENDER_POTT_NAME));
                    messageModel.setReceiverPottName(cursor.getString(chatMessages_databaseAdapter.COL_RECEIVER_POTT_NAME));
                    messageModel.setMessageText(cursor.getString(chatMessages_databaseAdapter.COL_MESSAGE_TEXT));
                    messageModel.setMessageImage(cursor.getString(chatMessages_databaseAdapter.COL_MESSAGE_IMAGE));
                    messageModel.setMessageTime(cursor.getString(chatMessages_databaseAdapter.COL_MESSAGE_TIME));
                    messageModel.setMessageStatus(chatMessages_databaseAdapter.COL_MESSAGE_STATUS);
                    messageModel.setOnlineSku(chatMessages_databaseAdapter.COL_MESSAGE_ONLINE_SKU);

                    //ADDING STORY OBJECT TO LIST
                    ConversationMessages_ListDataGenerator.addOneDataToDesiredPosition(0, messageModel);
                    mConversationRecyclerView.getAdapter().notifyItemInserted(ConversationMessages_ListDataGenerator.getAllData().size());
                    mConversationRecyclerView.getLayoutManager().scrollToPosition(ConversationMessages_ListDataGenerator.getAllData().size()-1);

                    if(ConversationMessages_ListDataGenerator.getAllData().size() >= 100){
                        break;
                    }
                } while (cursor.moveToPrevious());
            }

            cursor.close();
            chatMessages_databaseAdapter.closeDatabase();
        }

    }



    class MessengerTimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {

            networkRequestThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    long onlineSku = 0;
                    ChatMessages_DatabaseAdapter chatMessagesDatabaseAdapter = new ChatMessages_DatabaseAdapter(getApplicationContext());
                    // OPENING THE STORIES DATABASE
                    chatMessagesDatabaseAdapter.openDatabase();

                    Cursor cursor = chatMessagesDatabaseAdapter.getSpecificRowsUsingWhereValueAsString(ChatMessages_DatabaseAdapter.KEY_CHAT_ID + " = '" + chatID + "'", ChatMessages_DatabaseAdapter.KEY_CHAT_ID + " DESC", true);
                    if(cursor != null && cursor.getCount() > 0){
                        onlineSku = cursor.getLong(chatMessagesDatabaseAdapter.COL_MESSAGE_ONLINE_SKU);
                        Log.e("checkForNewMessages", "onlineSku DESC : " + onlineSku);
                    } else {
                        Log.e("checkForNewMessages", "CURSOR IS NULL");
                    }
                    cursor.close();
                    chatMessagesDatabaseAdapter.closeDatabase();
                    checkForNewMessages(chatID, receiverPottName, String.valueOf(onlineSku), LocaleHelper.getLanguage(getApplicationContext()));

                }
            });
            networkRequestThread.start();
            }

    }

    public void checkForNewMessages(String chatID, final String receiverPottName, String lastSku, String language){
        if(!REQUEST_HAS_STARTED){
            REQUEST_HAS_STARTED = true;
            AndroidNetworking.post(Config.LINK_GET_NEW_CHAT_MESSAGES)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("chat_id", chatID)
                    .addBodyParameter("last_sku", lastSku)
                    .addBodyParameter("receiver_pottname", receiverPottName)
                    .addBodyParameter("language", language)
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("get_chat_messages")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    Log.e("checkForNewMessages", "response : " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("data_returned");

                        JSONObject o = array.getJSONObject(0);
                        int myStatus = o.getInt("1");
                        String statusMsg = o.getString("2");

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3){
                            Config.showToastType1(MessengerActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(MessengerActivity.this, statusMsg);
                            Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);
                        }

                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                        if (myStatus == 1) {
                            JSONArray newsArray = jsonObject.getJSONArray("news_returned");

                            if(newsArray.length() > 0){

                                for (int x = 0; x < allLocallyShownMessagesListPositions.size(); x++){
                                    if(ConversationMessages_ListDataGenerator.getAllData().get(allLocallyShownMessagesListPositions.get(x)).getMessageStatus() == Config.SENT){
                                        Log.e("checkForNewMessages", "LOCAL MESSAGE TO BE DELETED FOR POSITION : " + String.valueOf(allLocallyShownMessagesListPositions.get(x)) + " WITH MESSAGE : " + ConversationMessages_ListDataGenerator.getAllData().get(allLocallyShownMessagesListPositions.get(x)).getMessageText() + " WITH POSITION : " +  String.valueOf(allLocallyShownMessagesListPositions.get(x)));
                                        Log.e("checkForNewMessages", "OLD LISTS INFO - CHAT-LIST-SIZE = " + String.valueOf(ConversationMessages_ListDataGenerator.getAllData().size()) + "OLD SIZE FOR LOCAL-MESAGES-LIST : " + String.valueOf(allLocallyShownMessagesListPositions.size()));

                                        ConversationMessages_ListDataGenerator.getAllData().remove(allLocallyShownMessagesListPositions.get(x).intValue());
                                        mConversationRecyclerView.getAdapter().notifyItemRemoved(allLocallyShownMessagesListPositions.get(x));
                                        mConversationRecyclerView.getAdapter().notifyItemRangeChanged(allLocallyShownMessagesListPositions.get(x), ConversationMessages_ListDataGenerator.getAllData().size());
                                        allLocallyShownMessagesListPositions.remove(x);
                                        Log.e("checkForNewMessages", "NEW LISTS INFO - CHAT-LIST-SIZE = " + String.valueOf(ConversationMessages_ListDataGenerator.getAllData().size()) + "NEW SIZE FOR LOCAL-MESAGES-LIST : " + String.valueOf(allLocallyShownMessagesListPositions.size()));
                                    }
                                }


                                ChatMessages_DatabaseAdapter chatMessagesDatabaseAdapter = new ChatMessages_DatabaseAdapter(getApplicationContext());
                                // OPENING THE STORIES DATABASE
                                chatMessagesDatabaseAdapter.openDatabase();
                                for (int i = 0; i < newsArray.length(); i++){
                                    final JSONObject k = newsArray.getJSONObject(i);

                                    Cursor cursor = chatMessagesDatabaseAdapter.getSpecificRowsUsingWhereValueAsString(ChatMessages_DatabaseAdapter.KEY_MESSAGE_ID + " = '" + k.getString("0a") + "'", null, false);
                                    if(cursor != null && cursor.getCount() > 0){
                                        String msgID = cursor.getString(chatMessagesDatabaseAdapter.COL_MESSAGE_ID);
                                        Log.e("checkForNewMessages", "msgID : " + msgID);
                                    } else {
                                        Log.e("checkForNewMessages", "msgID NOT FOUND ");

                                        chatMessagesDatabaseAdapter.insertRow(
                                                k.getString("0a"), //message id
                                                k.getString("1"), //chat id
                                                k.getString("2"), //sender_pottname
                                                k.getString("3"), //receiver_pottname
                                                k.getString("4"), // message_text
                                                "", // message_image
                                                k.getString("5"), // message_time
                                                Config.SENT, // pottname name
                                                k.getInt("6") // message_sku
                                        );

                                        MessageModel messageModel = new MessageModel();
                                        messageModel.setRowId(0);
                                        messageModel.setMessageId(k.getString("0a"));
                                        messageModel.setChatId(k.getString("1"));
                                        messageModel.setSenderPottName(k.getString("2"));
                                        messageModel.setReceiverPottName(k.getString("3"));
                                        messageModel.setMessageText(k.getString("4"));
                                        messageModel.setMessageImage("");
                                        messageModel.setMessageTime(k.getString("5"));
                                        messageModel.setMessageStatus(Config.SENT);
                                        messageModel.setOnlineSku(k.getInt("6"));

                                        updateListInMyRecentChats(getApplicationContext(), Config.READ, k.getString("1"), receiverPottPic, receiverPottName, k.getString("4"), k.getString("5"));

                                        //ADDING STORY OBJECT TO LIST
                                        ConversationMessages_ListDataGenerator.addOneData(messageModel);

                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //mConversationRecyclerView.getAdapter().notifyDataSetChanged();
                                                mConversationRecyclerView.getAdapter().notifyItemInserted(ConversationMessages_ListDataGenerator.getAllData().size());
                                                mConversationRecyclerView.getLayoutManager().scrollToPosition(ConversationMessages_ListDataGenerator.getAllData().size()-1);

                                            }
                                        });

                                    }

                                    cursor.close();
                                    cursor = null;
                                }

                                chatMessagesDatabaseAdapter.closeDatabase();
                                chatMessagesDatabaseAdapter = null;

                            }

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    REQUEST_HAS_STARTED = false;
                }

                @Override
                public void onError(ANError anError) {REQUEST_HAS_STARTED = false;}
            });

        }
    }

    private void sendMessage(String chatID, String receiverPottName, final int listPosition, final String message, String language){
        final MessageModel messageModel = new MessageModel();
        messageModel.setRowId(ConversationMessages_ListDataGenerator.getAllData().size()+1);
        messageModel.setChatId(chatID);
        messageModel.setOnlineSku(0);
        messageModel.setSenderPottName(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME));
        messageModel.setReceiverPottName(receiverPottName);
        messageModel.setMessageText(message);
        messageModel.setMessageImage("");
        messageModel.setMessageTime(Config.getCurrentDateTime3("MMM d, yyyy"));
        if(Connectivity.isConnected(getApplicationContext())){
            messageModel.setMessageStatus(Config.SENT);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mMessageEditText.setText("");
                    ConversationMessages_ListDataGenerator.addOneData(messageModel);
                    mConversationRecyclerView.getAdapter().notifyItemInserted(ConversationMessages_ListDataGenerator.getAllData().size());
                    mConversationRecyclerView.getLayoutManager().scrollToPosition(ConversationMessages_ListDataGenerator.getAllData().size()-1);

                }
            });
            allLocallyShownMessagesListPositions.add(listPosition);

            AndroidNetworking.post(Config.LINK_SEND_MESSAGE)
                    .addBodyParameter("log_phone", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                    .addBodyParameter("log_pass_token", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD))
                    .addBodyParameter("mypottname", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
                    .addBodyParameter("my_currency", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
                    .addBodyParameter("chat_id", chatID)
                    .addBodyParameter("message", message)
                    .addBodyParameter("message_time", Config.getCurrentDateTime3("yyyy-M-dd hh:mm:ss"))
                    .addBodyParameter("receiver_pottname", receiverPottName)
                    .addBodyParameter("language", language)
                    .addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
                    .setTag("send_message")
                    .setPriority(Priority.HIGH)
                    .build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("data_returned");

                        JSONObject o = array.getJSONObject(0);
                        int myStatus = o.getInt("1");
                        String statusMsg = o.getString("2");

                        if(myStatus != 1){
                            resetUiForUnsentMessage(listPosition);
                        }

                        // IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
                        if(myStatus == 2){
                            Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
                            Config.openActivity3(getApplicationContext(), UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
                            return;
                        }

                        // GENERAL ERROR
                        if(myStatus == 3){
                            Config.showToastType1(MessengerActivity.this, statusMsg);
                            return;
                        }

                        // IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
                        if(myStatus == 4){
                            Config.showToastType1(MessengerActivity.this, statusMsg);
                            Config.signOutUser(getApplicationContext(), false, null, null, 0, 2);
                        }

                        //STORING THE USER DATA
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

                        // UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
                        Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
                        Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        resetUiForUnsentMessage(listPosition);
                    }
                }

                @Override
                public void onError(ANError anError) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            resetUiForUnsentMessage(listPosition);
                        }
                    });
                }
            });

        } else {
            messageModel.setMessageStatus(Config.UNSENT);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    ConversationMessages_ListDataGenerator.addOneData(messageModel);
                    mConversationRecyclerView.getAdapter().notifyItemInserted(ConversationMessages_ListDataGenerator.getAllData().size());
                    mConversationRecyclerView.getLayoutManager().scrollToPosition(ConversationMessages_ListDataGenerator.getAllData().size()-1);

                }
            });
        }

    }

    public void resetUiForUnsentMessage(final int listPosition){
        MessageModel messageModel = new MessageModel();
        messageModel.setRowId(ConversationMessages_ListDataGenerator.getAllData().get(listPosition).getRowId());
        messageModel.setChatId(ConversationMessages_ListDataGenerator.getAllData().get(listPosition).getChatId());
        messageModel.setSenderPottName(Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME));
        messageModel.setReceiverPottName(receiverPottName);
        messageModel.setMessageText(ConversationMessages_ListDataGenerator.getAllData().get(listPosition).getMessageText());
        messageModel.setMessageImage("");
        messageModel.setMessageTime(ConversationMessages_ListDataGenerator.getAllData().get(listPosition).getMessageTime());
        messageModel.setMessageStatus(Config.UNSENT);
        ConversationMessages_ListDataGenerator.getAllData().remove(listPosition);
        ConversationMessages_ListDataGenerator.addOneDataToDesiredPosition(listPosition, messageModel);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mConversationRecyclerView.getAdapter().notifyItemChanged(listPosition);
                mConversationRecyclerView.getLayoutManager().scrollToPosition(listPosition);
            }
        });
    }

    public static void updateListInMyRecentChats(Context context, int readStatus, String chatID, String receiverPottPic, String receiverPottName, String chatMessage, String chatDate){
        ChatList_DatabaseAdapter chatList_databaseAdapter = new ChatList_DatabaseAdapter(context);
        // OPENING THE  DATABASE
        chatList_databaseAdapter.openDatabase();
        Cursor cursor = chatList_databaseAdapter.getSpecificRowsUsingWhereValueAsString(ChatList_DatabaseAdapter.KEY_CHAT_ID + " = '" + chatID + "'", ChatMessages_DatabaseAdapter.KEY_CHAT_ID + " DESC", false);
        if(cursor != null && cursor.getCount() > 0){
            long rowId = cursor.getLong(chatList_databaseAdapter.COL_ROWID);
            chatList_databaseAdapter.deleteRow(rowId);
        }

        chatList_databaseAdapter.insertRow(chatID, receiverPottPic, receiverPottName, chatDate, chatMessage, readStatus);
        ChatListFragment.getOldChats(context);
        cursor.close();
        chatList_databaseAdapter.closeDatabase();
        chatList_databaseAdapter = null;
        cursor = null;
    }
}
