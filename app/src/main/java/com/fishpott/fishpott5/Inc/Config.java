package com.fishpott.fishpott5.Inc;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fishpott.fishpott5.Activities.AboutActivity;
import com.fishpott.fishpott5.Activities.ConfirmPhoneNumberActivity;
import com.fishpott.fishpott5.Activities.FlaggedAccountActivity;
import com.fishpott.fishpott5.Activities.FullNewsActivity;
import com.fishpott.fishpott5.Activities.GovernmentIDVerificationActivity;
import com.fishpott.fishpott5.Activities.LoginActivity;
import com.fishpott.fishpott5.Activities.MainActivity;
import com.fishpott.fishpott5.Activities.MessengerActivity;
import com.fishpott.fishpott5.Activities.NotificationViewerActivity;
import com.fishpott.fishpott5.Activities.SetProfilePictureActivity;
import com.fishpott.fishpott5.Activities.StartActivity;
import com.fishpott.fishpott5.Activities.UpdateActivity;
import com.fishpott.fishpott5.Activities.WebViewActivity;
import com.fishpott.fishpott5.Adapters.ChatMessages_DatabaseAdapter;
import com.fishpott.fishpott5.Adapters.Vertical_News_Type_DatabaseAdapter;
import com.fishpott.fishpott5.Fragments.Signup.SignupStartFragment;
import com.fishpott.fishpott5.ListDataGenerators.ConversationMessages_ListDataGenerator;
import com.fishpott.fishpott5.Mention.URLSpanNoUnderline;
import com.fishpott.fishpott5.Models.MessageModel;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.NonUnderlinedClickableSpan;
import com.fishpott.fishpott5.Util.RealPathUtil;
import com.fishpott.fishpott5.Util.TooltipWindow;
import com.google.android.material.textfield.TextInputLayout;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

	// ACCEPTED MIME TYPES
	public static final String[] mimeTypes = {"image/jpeg", "image/png"};

	// CURRENT HTTP
	public static final String CURRENT_HTTP_IN_USE = "https://";

	// LIVE OR TEST ENVIRONMENT
	//public static final String CURRENT_ENVIRONMENT_DOMAIN_IN_USE = "test.fishpott.com"; // TEST
	public static final String CURRENT_ENVIRONMENT_DOMAIN_IN_USE = "app.fishpott.com"; // LIVE

	// FP ID
	public static final String FP_ID = "_r_030250308659e9029382af83.46926837";

	// DIRECTORIES-NAMES
	public static final String DIRECTORY_PHOTOS = "/FishPott/Photos";
	public static final String DIRECTORY_HIDDEN_MEDIA = "/hm";

	// Get max available VM memory, exceeding this amount will throw an
	// OutOfMemory exception. Stored in kilobytes as LruCache takes an
	// int in its constructor.
	private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	// Use 1/8th of the available memory for this memory cache.
	private static final int cacheSize = maxMemory / 8;

	private static LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
		@Override
		protected int sizeOf(String key, Bitmap bitmap) {
			// The cache size will be measured in kilobytes rather than
			// number of items.
			return bitmap.getByteCount() / 1024;
		}
	};

	// FRAGMENT CALLING ARGUMENT KEY
	public static final String FRAGMENT_KEY_CALLING_ACTIVITY_OR_FRAGMENT = "CallingActivityName";

	//MESSENGER
	public static final int UNSENT = 0;
	public static final int SENT = 1;

	public static final int UNREAD = 0;
	public static final int READ = 1;

	public static final int LIKED = 1;
	public static final int DISLIKED = 0;
	public static final int NO_REACTION = -1;

	// MAIN ACTIVITY MENU ITEMS KEY
	public static final int SETTIINGS_BAR_ITEM = 0;
	public static final int NEWSFEED_ITEM = 1;
	public static final int SHARESCENTER_ITEM = 2;
	public static final int MESSENGER_ITEM = 3;
	public static final int TRANSFER_CENTER_ITEM = 4;
	public static final int NOTIFICATION_ITEM = 5;

	// FRAGMENT CALLING ARGUMENT KEY
	public static final String WEBVIEW_KEY_URL = "URL";

	// CLIP BOARD INDEX FOR NEWS ID
	public static final String CLIP_BOARD_FISHPOTT_NEWS_ID = "FISHPOTT_NEWS_ID";

	// NEWS KEYS
	public static final int NEWS_TYPE_28_SHARES4SALE_STORY_HORIZONTAL_KEY = 1;
	public static final int NEWS_TYPE_28_UP4SALE_STORY_HORIZONTAL_KEY = 2;
	public static final int NEWS_TYPE_28_EVENT_STORY_HORIZONTAL_KEY = 3;
	public static final int NEWS_TYPE_28_FUNDRAISER_STORY_HORIZONTAL_KEY = 4;
	public static final int NEWS_TYPE_28_JUSTNEWS_STORY_HORIZONTAL_KEY = 5;
	public static final int NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY = 6;
	public static final int NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY = 7;
	public static final int NEWS_TYPE_3_TO_4_JUSTNEWSWITHIMAGEANDMAYBETEXT_VERTICAL_KEY = 8;
	public static final int NEWS_TYPE_5_TO_6_JUSTNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY = 9;
	public static final int NEWS_TYPE_7_AND_9_JUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY = 10;
	public static final int NEWS_TYPE_8_JUSTNEWSWITHURLWITHVIDEOANDMAYBETEXT_VERTICAL_KEY = 11;
	public static final int NEWS_TYPE_10_UPFORSALENEWS_VERTICAL_KEY = 12;
	public static final int NEWS_TYPE_12_EVENTNEWS_VERTICAL_KEY = 13;
	public static final int NEWS_TYPE_14_SHARESFORSALENEWS_VERTICAL_KEY = 14;
	public static final int NEWS_TYPE_16_FUNDRAISERNEWS_VERTICAL_KEY = 15;
	public static final int NEWS_TYPE_17_SHARES4SALEWITHVIDEO_VERTICAL_KEY = 16;
	public static final int NEWS_TYPE_1_SPONSOREDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY = 17;
	public static final int NEWS_TYPE_2_SPONSOREDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY = 18;
	public static final int NEWS_TYPE_3_TO_4_SPONSOREDJUSTNEWSWITHIMAGEANDMAYBETEXT_VERTICAL_KEY = 19;
	public static final int NEWS_TYPE_5_TO_6_SPONSOREDJUSTNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY = 20;
	public static final int NEWS_TYPE_1_REPOSTEDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY = 21;
	public static final int NEWS_TYPE_2_REPOSTEDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY = 22;
	public static final int NEWS_TYPE_3_TO_4_REPOSTEDJUSTNEWSWITHIMAGEANDMAYBETEXT_VERTICAL_KEY = 23;
	public static final int NEWS_TYPE_5_TO_6_REPOSTEDNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY = 24;
	public static final int NEWS_TYPE_7_AND_9_REPOSTEDJUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY = 25;
	public static final int NEWS_TYPE_8_REPOSTEDJUSTNEWSWITHURLWITHVIDEOANDMAYBETEXT_VERTICAL_KEY = 26;
	public static final int NEWS_TYPE_10_REPOSTEDUPFORSALENEWS_VERTICAL_KEY = 27;
	public static final int NEWS_TYPE_14_REPOSTEDSHARESFORSALENEWS_VERTICAL_KEY = 28;
	public static final int NEWS_TYPE_17_REPOSTEDSHARES4SALEWITHVIDEO_VERTICAL_KEY = 29;
	public static final int NEWS_TYPE_25_POSTINGFAILEDRETRYPOST_VERTICAL_KEY = 30;
	public static final int NEWS_TYPE_30_NEWSLOADINGRETRY_VERTICAL_KEY = 31;
	public static final int NEWS_TYPE_32_PROFILE_BIO = 32;
	public static final int NEWS_TYPE_41_SHARESFORSALE_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY = 41;
	public static final int NEWS_TYPE_42_SHARESFORSALE_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY = 42;
	public static final int NEWS_TYPE_43_SHARESFORSALE_REPOSTEDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY = 43;
	public static final int NEWS_TYPE_44_SHARESFORSALE_REPOSTEDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY = 44;

	// NEWS ORIENTATION KEYS
	public static final int VERTICAL_NEWS_KEY = 110;
	public static final int HORIZONTAL_NEWS_TYPE_28_KEY = 111;
	public static final int HORIZONTAL_NEWS_TYPE_15_KEY = 112;
	public static final int HORIZONTAL_NEWS_TYPE_26_KEY = 113;
	public static final int HORIZONTAL_NEWS_TYPE_11_KEY = 114;


	// NOTIFICATION TYPE KEYS = 200
	public static final int NOTICATION_RELATING_COMMENT = 200;
	public static final String NOTICATION_RELATING_COMMENT_STRING = "200";

	public static final int NOTICATION_RELATING_DISLIKE = 201;
	public static final String NOTICATION_RELATING_DISLIKE_STRING = "201";

	public static final int NOTICATION_RELATING_LIKE = 203;
	public static final String NOTICATION_RELATING_LIKE_STRING = "203";

	public static final int NOTICATION_RELATING_VIEW = 213;
	public static final String NOTICATION_RELATING_VIEW_STRING = "213";

	public static final int NOTICATION_RELATING_PURCHASE = 214;
	public static final String NOTICATION_RELATING_PURCHASE_STRING = "214";

	public static final int NOTICATION_RELATING_JUST_INFO = 202;
	public static final int NOTICATION_RELATING_LINKUP = 204;
	public static final int NOTICATION_RELATING_POACH = 205;
	public static final int NOTICATION_RELATING_REPOST = 206;
	public static final int NOTICATION_RELATING_SHARESFORSALE = 207;
	public static final int NOTICATION_RELATING_NEWS_MENTION = 209;
	public static final int CREATE_POST_NEWS_POSTED_REQUEST_CODE = 208;
	public static final int NOTICATION_RELATING_REFERRAL = 210;
	public static final int NOTICATION_RELATING_SHARESFORSALE_TRANSFER = 211;
	public static final int NOTICATION_RELATING_TO_NEW_MESSAGE = 212;
	public static final int NOTICATION_RELATING_SHARES_SUGGESTION = 213;
	public static final int NOTICATION_RELATING_TO_GENERAL_INFO = 214;

	//FULL NEWS ACTIVITY INTENT INDEXES
	public static final String FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID = "newsid";
	public static final String FULL_NEWS_ACTIVITY_INTENT_INDEXES_VIEW_TYPE = "stats_type";

	//CHANNEL ID FOR USER NOTIFICATION
	public static final String USER_NOTIFICATION_KEY_SHARESFORSALE = "USER_NOTIFICATION_KEY_SHARESFORSALE";

	// NOTIFICATIONS READ/UNREAD TOGGLE
	public static final String NOTIFICATION_UNREAD_COUNT = "NOTIFICATION_UNREAD_COUNT";
	public static final String CHAT_NOTIFICATION_UNREAD_COUNT = "CHAT_NOTIFICATION_UNREAD_COUNT";

	// SERVER-SIDE API FOR SIGNUP
	public static final String LINK_SIGNUP_PERSONAL = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/register-personal";
	public static final String LINK_SIGNUP_BUSINESS = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/register-business";

	// SERVER-SIDE API FOR LOGIN
	public static final String LINK_LOGIN = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/login";

	// SERVER-SIDE API FOR POTT-PIC-UPLOAD-AT-SIGNUP-OR-FORCED
	public static final String LINK_UPLOAD_POTT_PICTURE = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/upload-pott-pic";

	// SERVER-SIDE API FOR GETTING RESET CODE
	public static final String LINK_GET_RESET_CODE = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/send-password-reset-code";

	// RESET PASSWORD WITH RESET PASSWORD WITH RESET CODE
	public static final String LINK_RESET_PASSWORD_WITH_CODE = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/change-password-with-reset-code";

	// GET MY SUGGESTION
	public static final String LINK_GET_MY_SUGGESTION = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/get-my-suggestion";

	// GET MY SUGGESTION
	public static final String LINK_SAVE_DRILL_AND_GET_ANSWERS_COUNT = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/save-drill-answer";

	// GET FINAL PRICE SUMMARY
	public static final String LINK_GET_FINAL_PRICE_SUMMARY = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/get-final-price";

	// UPDATE ORDER STATUS
	public static final String LINK_UPDATE_ORDER_STATUS = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/update-order-payment-status";

	// SEND WITHDRAWAL REQUEST
	public static final String LINK_WITHDRAW_FUNDS = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/send-withdrawal-request";

	// SERVER-SIDE API FOR TRANSACTIONS
	public static final String LINK_GET_TRANSACTIONS = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/get-my-transactions";

	// SERVER-SIDE API FOR GETTING MY OWNED SHARES
	public static final String LINK_GET_MY_SHARES = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/get-my-investments";

	// SERVER-SIDE API FOR FINDING A BUSINESS
	public static final String LINK_GET_FIND_BUSINESS = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/find-business";

	// SERVER-SIDE API FOR GETTING HOSTED SHARES
	public static final String LINK_UPDATE_USER_INFO = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/get-user-info";

	// SERVER-SIDE API FOR GETTING MY OWNED SHARES
	public static final String LINK_TRANSFER_STOCKS = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/send-stock-transfer";

	// SERVER-SIDE API FOR SELLING BACK SHARES
	public static final String LINK_SELLBACK_STOCKS = CURRENT_HTTP_IN_USE + CURRENT_ENVIRONMENT_DOMAIN_IN_USE + "/api/v1/user/sellback-stock";

	/********************************************************************************************************************************************

	 OLD LINKS OLD LINKS OLD LINKS OLD LINKS OLD LINKS OLD LINKS OLD LINKS OLD LINKS OLD LINKS OLD LINKS OLD LINKS OLD LINKS OLD LINKS OLD LINKS

	 ********************************************************************************************************************************************/

	// SERVER-SIDE API FOR POTT-PIC-UPLOAD-AT-SIGNUP-OR-FORCED
	public static final String LINK_POST_NEWS = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/post_news.php";

	// SERVER-SIDE API FOR LINKUPS
	public static final String LINK_GET_SUGGESTED_LINKUPS = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_suggested_linkups.php";

	// SERVER-SIDE API FOR STORIES
	public static final String LINK_GET_NEWS_STORIES_SKU_DESCENDING = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_news_stories.php";

	// SERVER-SIDE API FOR NEWS
	public static final String LINK_GET_VERTICAL_NEWS_SKU_DESCENDING = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_news.php";

	// SERVER-SIDE API FOR NEWS
	public static final String LINK_GET_SINGLE_FULLNEWS_WITH_STATS = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_news_single_with_stats.php";

	// SERVER-SIDE API FOR NEWS
	public static final String LINK_GET_PROFILE_WITH_NEWS_SKU_DESCENDING = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_profile_with_news.php";

	// SERVER-SIDE API FOR HORIZONTAL SHARES FOR SALE
	public static final String LINK_GET_HORIZONTAL_SHARES_FOR_SALE_NEWS_SKU_DESCENDING = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_news_horizontal_sharesforsale.php";

	// SERVER-SIDE API FOR HORIZONTAL SHARES FOR SALE
	public static final String LINK_GET_HORIZONTAL_LINKUPS_NEWS = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_news_linkups_suggestions.php";

	// SERVER-SIDE API FOR SENDING MESSAGE
	public static final String LINK_SEND_MESSAGE = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/send_message.php";

	// SERVER-SIDE API FOR SENDING MESSAGE
	public static final String LINK_GET_STATS = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_stats.php";

	// SERVER-SIDE API FOR SENDING MESSAGE
	public static final String LINK_SEND_COMMENT = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/send_comment.php";

	// SERVER-SIDE API FOR GETTING FINAL PRICE
	public static final String LINK_GET_FINAL_PRICE = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_shares_final_price.php";

	// SERVER-SIDE API FOR COMPLETING A SHARES PURCHASE
	public static final String LINK_COMPLETE_PURCHASE = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/complete_shares_purchase.php";

	// SERVER-SIDE API FOR GETTING NEW CHAT MESSAGES MESSAGE
	public static final String LINK_GET_NEW_CHAT_MESSAGES = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_new_chat_messages.php";

	// SERVER-SIDE API FOR GETTING NEW CHAT MESSAGES MESSAGE
	public static final String LINK_LIKE_OR_DISLIKE_NEWS = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/like_or_dislike_news.php";

	public static final String LINK_SET_NEWS_VIEWED = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/set_news_viewed.php";

	// SERVER-SIDE API FOR REDEEMING WALLET CREDIT CODE
	public static final String LINK_REDEEM_WALLET_CREDIT_CODE = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/redeem_wallet_credit_code.php";


	// SERVER-SIDE API FOR MOBILE MONEY DEPOSIT
	public static final String LINK_MOBILE_MONEY_CREDIT = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/mobile_money_credit_request.php";

	// SERVER-SIDE API FOR MOBILE MONEY CREDIT AUTOMATIC REQUEST MATER
	public static final String LINK_MTN_MOBILE_MONEY_CREDIT_AUTOMATIC_REQUEST_MAkER = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/mtn_momo/mtn_mobile_money_collection_request_requesttopay.php";

	// SERVER-SIDE API FOR REDEEMING SHARES CODE
	public static final String LINK_REDEEM_SHARES_CODE = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/redeem_shares_ownership_code.php";

	// SERVER-SIDE API FOR REDEEMING CODE
	public static final String LINK_CHANGE_PASSWORD = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/change_password.php";

	// SERVER-SIDE API FOR TRANSFERING SHARES
	public static final String LINK_TRANSFER_SHARES = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/transfer_shares.php";

	// SERVER-SIDE API FOR GETTING A STOCK PROFILE
	public static final String LINK_GET_SHARE_PROFILE = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_share_profile.php";

	// SERVER-SIDE API FOR GETTING HOSTED SHARES
	public static final String LINK_GET_HOSTED_SHARES = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_hosted_shares.php";

	// SERVER-SIDE API FOR GETTING CONTACTS
	public static final String LINK_GET_MY_CONTACTS = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_my_contacts.php";

	// SERVER-SIDE API FOR LINKUPS
	public static final String LINK_GET_SELLERS_OF_SHARES = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/get_sellers.php";

	// SERVER-SIDE API FOR ADDING-LINKUPS-AT-SIGNUP
	public static final String LINK_ADD_LINKUPS = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/add_linkup.php";

	// SERVER-SIDE API FOR ADDING-LINKUPS-AT-SIGNUP
	public static final String LINK_POACH = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/poach_pott.php";

	// SERVER-SIDE API FOR ADDING-LINKUPS-AT-SIGNUP
	public static final String LINK_SHIELD_POTT = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/shield_pott.php";

	// SERVER-SIDE API FOR ADDING-LINKUPS-AT-SIGNUP
	public static final String LINK_BLOCK_POTT = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/block_pott.php";

	// VERIFY PHONE NUMBER WITH CODE CODE
	public static final String VERIFY_PHONE_NUMBER_WITH_CODE = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/verify_phone_number_with_sms_code.php";

	// RESEND VERIFICATION CODE
	public static final String RESEND_PHONE_VERIFICATION_CODE = CURRENT_HTTP_IN_USE + "www.fishpott.com/inc/android/resend_phone_verification_code.php";

	//PRIVACY POLICY
	public static final String FISHPOTT_PRIVACY_POLICY = CURRENT_HTTP_IN_USE + "www.fishpott.com/pp.html";

	//TERMS OF SERVICE
	public static final String FISHPOTT_TERMS_OF_SERVICE = CURRENT_HTTP_IN_USE + "www.fishpott.com/t_c.html";

	//START ACTIVITY VIEWING CYCLE COUNT KEYS FOR SHARED PREFERENCES
	public static final String SHARED_PREF_KEY_START_ACTIVITY_VIEWING_CYCLE_COUNT = "VIEWING_CYCLE_COUNT";

	//OPEN ACTIVITY
	public static final String KEY_ACTIVITY_FINISHED = "ACTIVITY_FINISHED";

	//UPDATE APP KEYS FOR SHARED PREFERENCES
	public static final String SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE = "UPDATE_VERSION_CODE";
	public static final String SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE = "UPDATE_NOT_NOW_DATE";
	public static final String SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE = "UPDATE_BY_FORCE";

	//USER CREDENTAILS : START ACTIVITY VIEWING CYCLE COUNT KEYS FOR SHARED PREFERENCES
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE = "USER_PHONE";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_EMAIL = "USER_EMAIL";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_ID = "USER_ID";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN = "USER_PASSWORD";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_SERVER_SESSION_ID = "SERVER_SESSION_ID";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEVICE_TOKEN = "DEVICE_TOKEN";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME = "USER_POTT_NAME";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_FULL_NAME = "USER_FULL_NAME";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_PROFILE_PICTURE = "USER_PROFILE_PICTURE";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_COUNTRY = "USER_COUNTRY";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_VERIFIED_STATUS = "USER_VERIFIED_STATUS";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS = "USER_ACCOUNT_SUSPENDED_STATUS";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_GOVERNMENT_ID_VERIFICATION_NEEDED_STATUS = "USER_ACCOUNT_GOVERNMENT_ID_VERIFICATION_NEEDED_STATUS";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_TYPE = "USER_TYPE";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_GENDER = "USER_GENDER";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_DATE_OF_BIRTH = "USER_DATE_OF_BIRTH";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY = "USER_CURRENCY";
	public static final String SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON = "VERIFY_PHONE_NUMBER_IS_ON";
	public static final String SHARED_PREF_KEY_USER_CONTACTS_ACTIVITY_DIALOG_SHOWN = "CONTACTS_ACTIVITY_DIALOG_SHOWN";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_WALLET = "USER_WALLET";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_WITHDRAWAL_WALLET = "USER_WITHDRAWAL_WALLET";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_DEBIT_WALLET = "USER_DEBIT_WALLET";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_PEARLS = "USER_POTT_PEARLS";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_HAS_LEARNED = "USER_HAS_LEARNED";
	public static final String SHARED_PREF_KEY_USER_TRANSFER_FEE = "USER_TRANSFER_FEE";
	public static final String SHARED_PREF_KEY_USER_MEDIA_POSTING_ALLOWED = "USER_MEDIA_POSTING_ALLOWED";
	public static final String SHARED_PREF_KEY_USER_FISHPOTT_EMAIL = "FISHPOTT_EMAIL";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN = "MTN";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE = "VODAFONE";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO = "AIRTELTIGO";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_MTN_NAME = "MTN_NAME";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_VODAFONE_NAME = "VODAFONE_NAME";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_AIRTELTIGO_NAME = "AIRTELTIGO_NAME";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_INTELLIGENCE = "POTT_INTELLIGENCE";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NET_WORTH = "POTT_NET_WORTH";
	public static final String SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_POSITION = "POTT_POSITION";

	//LINKUPS
	public static final String SHARED_PREF_KEY_LINKUPS = "LINKUPS";


	// GET SHARED PREFERENCE STRING-SET
	public static Set<String> getSharedPreferenceStringSet(Context context, String key) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getStringSet(key, null);
	}

	// PUT NEW SHARED PREFERENCE STRING-SET
	public static void setSharedPreferenceSetString(Context context, String key, List<String> value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		Set<String> set = new HashSet<String>();
		set.addAll(value);
		editor.putStringSet(key, set);
		editor.commit();
		editor.apply();
	}

	// PUT NEW SHARED PREFERENCE STRING-SET
	public static void updateSharedPreferenceSetString(Context context, String key, String newValue, Set oldset, Boolean updateIfValueExits) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		Set<String> set = preferences.getStringSet(key, null);
		if(set != null) {
			List<String> itemReferences = new ArrayList<String>(set);
			int itemIndex = itemReferences.indexOf(newValue);
			if (itemIndex < 0) {
				itemReferences.add(newValue);
				setSharedPreferenceSetString(context, key, itemReferences);
			} else {
				if(updateIfValueExits){
					itemReferences.add(itemIndex, newValue);
					setSharedPreferenceSetString(context, key, itemReferences);
				}
			}
		} else {
			List<String> itemReferences = new ArrayList<String>();
			itemReferences.add(newValue);
			setSharedPreferenceSetString(context, key, itemReferences);
		}
	}

	// GET SHARED PREFERENCE STRING
	public static String getSharedPreferenceString(Context context, String key) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		context = null;
		return preferences.getString(key, "");

	}

	// EDIT SHARED PREFERENCE STRING
	public static void setSharedPreferenceString(Context context, String key, String value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.apply();
		context = null;
	}

	// GET SHARED PREFERENCE INT
	public static int getSharedPreferenceInt(Context context, String key) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		context = null;
		return preferences.getInt(key, 0);

	}

	// SET SHARED PREFERENCE INT
	public static void setSharedPreferenceInt(Context context, String key, int value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, value);
		editor.apply();
		context = null;
	}


	// GET SHARED PREFERENCE INT
	public static int getSharedPreferenceFloat(Context context, String key) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		context = null;
		return preferences.getInt(key, 0);

	}

	// SET SHARED PREFERENCE INT
	public static void setSharedPreferenceFloat(Context context, String key, float value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putFloat(key, value);
		editor.apply();
		context = null;
	}


	// GET SHARED PREFERENCE BOOLEAN
	public static boolean getSharedPreferenceBoolean(Context context, String key) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		context = null;
		return preferences.getBoolean(key, false);

	}

	// SET SHARED PREFERENCE BOOLEAN
	public static void setSharedPreferenceBoolean(Context context, String key, boolean value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, value);
		context = null;
		editor.apply();
	}

	// CLEAR ALL SHARED PREFERENCE
	public static void deleteAllDataInSharedPreference(Context context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
		context = null;
	}

	// GET APP VERSION CODE
	public static int getAppVersionCode(Context context){
		PackageInfo pinfo = null;
		int currentVersionNumber = 0;
		try {
			pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			currentVersionNumber = pinfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return currentVersionNumber;
	}


	public static String getCurrentDateTime() {
		// get date time in custom format
		SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");
		return sdf.format(new Date());
	}

	public static String getCurrentDateTime2() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(c.getTime());
	}

	public static String getCurrentDateTime3(String format) {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(c.getTime());
	}

	public static String getCurrentDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}
	/**********************************************************************************************************
	 *
	 * 										MEMORY MANAGEMENT FUNCTIONS START
	 *
	 **********************************************************************************************************/

	// FREE MEMORY WHEN CLOSING AN ACTIVITY AND OPENING A NEW ONE
	public static void freeMemory(){
		System.runFinalization();
		Runtime.getRuntime().gc();
		System.gc();
		Log.e("memoryManage", "freeMemory DONE");
	}

	// FREE MEMORY BY PROPERLY UNLOADING ALL BITMAPS USED IN PREVIOUS ACTIVITY
	public static void unbindDrawables(View view) {

		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}

		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}

			((ViewGroup) view).removeAllViews();
		}
		Log.e("memoryManage", "unbindDrawables DONE");
	}

	/**********************************************************************************************************
	 *
	 * 										MEMORY MANAGEMENT FUNCTIONS END
	 *
	 **********************************************************************************************************/


	public static void openActivity(Activity thisActivity, Class NewActivity, int includeAnimation, int finishActivity, int addData, String dataIndex, String dataValue) {
		Intent intent = new Intent(thisActivity, NewActivity);
		if(addData == 1){
			intent.putExtra(dataIndex, dataValue);
		}

		if(finishActivity == 1){
			thisActivity.startActivity(intent);
			thisActivity.finish();
			if(includeAnimation == 1){
				thisActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			} else if(includeAnimation == 2){
				thisActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
			}

		} else if(finishActivity == 2){
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			thisActivity.startActivity(intent);
			thisActivity.finish();
			if(includeAnimation == 1){
				thisActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			} else if(includeAnimation == 2){
				thisActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
			}

		} else {
			thisActivity.startActivity(intent);
			if(includeAnimation == 1){
				thisActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			} else if(includeAnimation == 2){
				thisActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
			}

		}
		thisActivity = null;
		Config.freeMemory();
	}

	public static void openActivity2(Activity thisActivity, Class NewActivity, int includeAnimation, int finishActivity, int addData, String dataIndex, String dataValue, String dataIndex2, String dataValue2) {
		Intent intent = new Intent(thisActivity, NewActivity);
		if(addData == 1){
			intent.putExtra(dataIndex, dataValue);
			intent.putExtra(dataIndex2, dataValue2);
		}

		if(finishActivity == 1){
			thisActivity.startActivity(intent);
			thisActivity.finish();
			if(includeAnimation == 1){
				thisActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			} else if(includeAnimation == 2){
				thisActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
			}

		} else if(finishActivity == 2){
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			thisActivity.startActivity(intent);
			thisActivity.finish();
			if(includeAnimation == 1){
				thisActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			} else if(includeAnimation == 2){
				thisActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
			}

		} else {
			thisActivity.startActivity(intent);
			if(includeAnimation == 1){
				thisActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			} else if(includeAnimation == 2){
				thisActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
			}

		}
		thisActivity = null;
		Config.freeMemory();
	}

	public static void openActivity3(Context context, Class NewActivity, int addData, String dataIndex, String dataValue) {
		Intent intent = new Intent(context, NewActivity);
		if(addData == 1){
			intent.putExtra(dataIndex, dataValue);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent);
		context = null;
		Config.freeMemory();
	}


	public static void openActivity4(Activity thisActivity, Class NewActivity, int includeAnimation, int finishActivity, int addData, String dataIndex, String[] dataValue) {
		Intent intent = new Intent(thisActivity, NewActivity);
		if(addData == 1){
			intent.putExtra(dataIndex, dataValue);
		}

		if(finishActivity == 1){
			thisActivity.startActivity(intent);
			thisActivity.finish();
			if(includeAnimation == 1){
				thisActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			} else if(includeAnimation == 2){
				thisActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
			}

		} else if(finishActivity == 2){
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			thisActivity.startActivity(intent);
			thisActivity.finish();
			if(includeAnimation == 1){
				thisActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			} else if(includeAnimation == 2){
				thisActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
			}

		} else {
			thisActivity.startActivity(intent);
			if(includeAnimation == 1){
				thisActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			} else if(includeAnimation == 2){
				thisActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
			}

		}
		thisActivity = null;
		Config.freeMemory();
	}

	public static void openActivityWithNonActivityContext(Context context, Class NewActivity, int includeAnimation, int addData, String dataIndex, String dataValue) {
		Intent intent = new Intent(context, NewActivity);
		if(addData == 1){
			intent.putExtra(dataIndex, dataValue);
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent);
		if(includeAnimation == 1){
			//context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		} else if(includeAnimation == 2){
			//context.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
		}

		context = null;
		Config.freeMemory();
	}

	// GET NUMBER OF DAYS DIFFERENCE BETWEEN TWO DATES
	public static long getDateDifferenceBetweenDates(String startDate, String endDate){

		long diff = 0;
		if(!startDate.trim().equalsIgnoreCase("") && !endDate.trim().equalsIgnoreCase("")){

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			try {
				Date realStartDate = sdf.parse(startDate);
				Date realEndDate = sdf.parse(endDate);
				diff = realEndDate.getTime() - realStartDate.getTime();
				diff = (diff/(1000*60*60*24));
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

		return diff;
	}

	// DIALOG TYPE 1 SHOWS AN OKAY BUTTON THAT DOES NOTHING
	public static Dialog.OnCancelListener showDialogType1(final Activity thisActivity, String subTitle, String subBody, String subBody2, Dialog.OnCancelListener cancelListener, Boolean canNotBeClosedFromOutSideClick, String positiveButtonText, String negativeButtonText){

		if(thisActivity != null) {
			final Dialog dialog = new Dialog(thisActivity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			if (canNotBeClosedFromOutSideClick) {
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
			}
			if (subBody2.trim().equalsIgnoreCase("show-positive-image")) {
				dialog.setContentView(R.layout.positive_icon_dialog);
			} else {
				dialog.setContentView(R.layout.login_activity_dialog);
			}

			TextView dialogTextView = dialog.findViewById(R.id.login_activity_dialog_text);
			TextView dialogTextView2 = dialog.findViewById(R.id.login_activity_dialog_text2);
			ImageView dialogTextImageView = dialog.findViewById(R.id.login_activity_dialog_imageview);
			dialogTextView.setText(subBody);

			if (subBody.trim().equalsIgnoreCase("")) {
				dialogTextView.setVisibility(View.INVISIBLE);
			} else {
				dialogTextView.setText(subBody);
			}

			if (subBody2.trim().equalsIgnoreCase("") || subBody2.trim().equalsIgnoreCase("show-positive-image")) {
				dialogTextView2.setVisibility(View.INVISIBLE);
			} else {
				dialogTextView2.setText(subBody2);
			}

			if (subTitle.trim().equalsIgnoreCase("1")) {
				dialogTextImageView.setVisibility(View.GONE);
			}

			dialogTextView2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String url = CURRENT_HTTP_IN_USE + "www.fishpott.com/service_agreements.html";
					openActivity(thisActivity, WebViewActivity.class, 1, 0, 1, WEBVIEW_KEY_URL, url);
				}
			});

			Button positiveDialogButton = dialog.findViewById(R.id.login_activity_dialog_button);
			Button negativeDialogButton = dialog.findViewById(R.id.login_activity_dialog_button_cancel);
			if (!positiveButtonText.trim().equalsIgnoreCase("")) {
				positiveDialogButton.setText(positiveButtonText);
			}
			if (!negativeButtonText.trim().equalsIgnoreCase("")) {
				negativeDialogButton.setText(negativeButtonText);
			} else {
				negativeDialogButton.setVisibility(View.GONE);
			}
			positiveDialogButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
				}
			});
			negativeDialogButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.show();
			dialog.setOnCancelListener(cancelListener);
			return cancelListener;
		} else {
			return null;
		}
	}

	public static Dialog.OnCancelListener showDialogType2(final Activity thisActivity, Dialog.OnCancelListener cancelListener, Boolean canNotBeClosedFromOutSideClick){

		if(thisActivity != null) {
			final Dialog dialog = new Dialog(thisActivity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			if (canNotBeClosedFromOutSideClick) {
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
			}
			dialog.setContentView(R.layout.privacy_policy_dialog);

			//TextView mPrivacyPolicyTextView = dialog.findViewById(R.id.url_pp_dialog);
			TextView mTermsOfServiceTextView = dialog.findViewById(R.id.url_tc_dialog);
			Button positiveDialogButton = dialog.findViewById(R.id.agree_button_pp_dialog);
			Button negativeDialogButton = dialog.findViewById(R.id.deny_button_pp_dialog);
            /*
            mPrivacyPolicyTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openActivity(thisActivity, WebViewActivity.class, 1, 0, 1, WEBVIEW_KEY_URL, FISHPOTT_PRIVACY_POLICY);
                }
            });

             */

			mTermsOfServiceTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openActivity(thisActivity, WebViewActivity.class, 1, 0, 1, WEBVIEW_KEY_URL, FISHPOTT_TERMS_OF_SERVICE);
				}
			});

			positiveDialogButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
				}
			});
			negativeDialogButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.show();
			dialog.setOnCancelListener(cancelListener);
			return cancelListener;
		} else {
			return null;
		}
	}

	public static Dialog.OnCancelListener showDialogType3(final Activity thisActivity, Dialog.OnCancelListener cancelListener, Boolean canNotBeClosedFromOutSideClick){

		if(thisActivity != null) {
			final Dialog dialog = new Dialog(thisActivity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			if (canNotBeClosedFromOutSideClick) {
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
			}

			dialog.setContentView(R.layout.contacts_permission_dialog);

			TextView mPrivacyPolicyTextView = dialog.findViewById(R.id.url_pp_dialog);
			TextView mTermsOfServiceTextView = dialog.findViewById(R.id.url_tc_dialog);
			Button positiveDialogButton = dialog.findViewById(R.id.agree_button_pp_dialog);
			Button negativeDialogButton = dialog.findViewById(R.id.cancel_button_pp_dialog);

			mPrivacyPolicyTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openActivity(thisActivity, WebViewActivity.class, 1, 0, 1, WEBVIEW_KEY_URL, FISHPOTT_PRIVACY_POLICY);
				}
			});

			mTermsOfServiceTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openActivity(thisActivity, WebViewActivity.class, 1, 0, 1, WEBVIEW_KEY_URL, FISHPOTT_TERMS_OF_SERVICE);
				}
			});

			positiveDialogButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
				}
			});
			negativeDialogButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.show();
			dialog.setOnCancelListener(cancelListener);
			return cancelListener;
		} else {
			return null;
		}
	}


	//TOAST TYPE 1 SHOWS JUST A TEXT
	public static void showToastType1(Activity thisActivity, String toastInfo){
		View toastView = thisActivity.getLayoutInflater().inflate(R.layout.login_activity_toast, (ViewGroup) thisActivity.findViewById(R.id.activity_login_toast_root));
		TextView toastTextView = toastView.findViewById(R.id.activity_login_toast_text);
		toastTextView.setText(toastInfo);
		Toast toast = new Toast(thisActivity.getApplicationContext());
		toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
		//toast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
		//toast.setGravity(Gravity.BOTTOM, 0, 0);
		//toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(toastView);
		toast.show();
	}

	// CHECK IF USER IS LOGGED IN
	public static Boolean userIsLoggedIn(Activity thisActivity){
		Boolean userIsLoggedIn = false;
		if(!getSharedPreferenceString(thisActivity.getApplicationContext(), SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE).trim().equalsIgnoreCase("") && !getSharedPreferenceString(thisActivity.getApplicationContext(), SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN).trim().equalsIgnoreCase("")){
			userIsLoggedIn = true;
		} else {
			userIsLoggedIn = false;
		}
		return userIsLoggedIn;
	}

	public static Boolean userIsLoggedIn2(Context context){
		Boolean userIsLoggedIn = false;
		if(!getSharedPreferenceString(context, SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE).trim().equalsIgnoreCase("") && !getSharedPreferenceString(context, SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN).trim().equalsIgnoreCase("")){
			userIsLoggedIn = true;
		} else {
			userIsLoggedIn = false;
		}
		return userIsLoggedIn;
	}

	// CHECKING IS A USER'S PROFILE PICTURE IS SET
	public static Boolean userProfilePictureIsSet(Activity thisActivity){
		if(!getSharedPreferenceString(thisActivity.getApplicationContext(), SHARED_PREF_KEY_USER_CREDENTIALS_USER_PROFILE_PICTURE).trim().equalsIgnoreCase("")){
			return true;
		} else {
			return false;
		}
	}

	// OPENING A FRAGMENT
	public static void openFragment(FragmentManager fragmentManager, int fragmentContainerId, Fragment newFragment, String fragmentName, int includeAnimation){
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if(includeAnimation == 1){
			transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
		} else if (includeAnimation == 2){
			transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_left);
		} else if (includeAnimation == 3){
			transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down,R.anim.slide_in_down, R.anim.slide_out_up);
		}

		transaction.addToBackStack(fragmentName);
		transaction.add(fragmentContainerId, newFragment, fragmentName).commit();
		fragmentManager = null;
	}

	// OPEN A FRAGMENT FROM IT'S STRING NAME
	public static void openFragmentFromStringName(FragmentManager fragmentManager, int fragmentContainerId, String fragmentName, int includeAnimation){
		Fragment newFragment = null;
		if(fragmentName.equalsIgnoreCase("SignupStartFragment")){
			newFragment = SignupStartFragment.newInstance();
			includeAnimation = 0;
		}
		if(newFragment != null){
			openFragment(fragmentManager, fragmentContainerId, newFragment, fragmentName, includeAnimation);
		}
	}

	// OPENING THE DATE PICKER DIALOG BOX
	public static DatePickerDialog.OnDateSetListener  openDatePickerDialog(Activity thisActivity, DatePickerDialog.OnDateSetListener mDateSetListener, Boolean dateIsPreset, int thisDay, int thisMonth, int thisYear){

		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if(dateIsPreset){
			year = thisYear;
			month = thisMonth;
			day = thisDay;
		}

		DatePickerDialog dialog = new DatePickerDialog(thisActivity, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year, month, day);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		dialog.show();

		return  mDateSetListener;
	}

	// GETTING A MONTH NAME(FULL/SHORT) FROM IT'S NUMBER
	public static String getMonthNameFromMonthNumber(Activity thisActivity, int monthNumber, int nameType){
		String[] allMonths;

		if(nameType == 1){
			allMonths = thisActivity.getResources().getStringArray(R.array.fragment_signup_personalstage2_month_names_full);
		} else {
			allMonths = thisActivity.getResources().getStringArray(R.array.fragment_signup_personalstage2_month_names_short);
		}
		return allMonths[monthNumber];
	}

	// FUNCTION FOR SETTING A NUMBER PICKER AND GETTING THE NUMBER VALUE CHANGE LISTENER
	public static NumberPicker.OnValueChangeListener openNumberPickerForCountries(Activity thisActivity, NumberPicker.OnValueChangeListener  mNumberSetListener, int minNumber, int maxNumber, Boolean disNumbersOnUiToUser, String[] displayStringsValues, int defaultCountry) {

		final Dialog d = new Dialog(thisActivity);
		d.setContentView(R.layout.fragment_signup_stage2_country_picker_dialog);
		Button b1 = (Button) d.findViewById(R.id.fragment_signup_stage2_dialog_button);
		final NumberPicker np = (NumberPicker) d.findViewById(R.id.fragment_signup_stage2_numberpicker);
		np.setMaxValue(maxNumber);
		np.setMinValue(minNumber);
		np.setWrapSelectorWheel(false);
		np.setValue(defaultCountry);
		if(disNumbersOnUiToUser){
			np.setDisplayedValues(displayStringsValues);
		}
		np.setOnValueChangedListener(mNumberSetListener);
		b1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
		d.show();

		return mNumberSetListener;
	}

	public static Boolean checkUpdateAndForwardToUpdateActivity(Activity thisActivity, int newVersionCode, Boolean newForceUpdate){
		Log.e("UpdateCheck", "THEnewVersionCode : " + String.valueOf(newVersionCode));
		Log.e("UpdateCheck", "THEAppVersionCode : " + String.valueOf(Config.getAppVersionCode(thisActivity)));
		if(newVersionCode > Config.getAppVersionCode(thisActivity) && newForceUpdate){
			Log.e("UpdateCheck", "HERE 1 : " + String.valueOf(newVersionCode));
			setSharedPreferenceInt(thisActivity.getApplicationContext(),SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, newVersionCode);
			setSharedPreferenceBoolean(thisActivity.getApplicationContext(), SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, newForceUpdate);
			openActivity(thisActivity, UpdateActivity.class, 1, 2, 1, KEY_ACTIVITY_FINISHED, "1");
			return true;
		}
		return false;
	}

	// DISABLE SCREEN ROTATION ON AN ACTIVITY
	public static Boolean disableScreenRotation(Activity thisActivity){
		//thisActivity.setRequestedOrientation(thisActivity.getRequestedOrientation());
		//thisActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		thisActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
		return true;
	}

	// ENABLE SCREEN ROTATION ON AN ACTIVITY
	public static Boolean enableScreenRotation(Activity thisActivity){
		thisActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		//thisActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		return true;
	}

	// OPENING A TOOL TIP
	public static void openToolTip(String toolTipType, Boolean autoDismiss, Context thisActivity, View view, String infoText){

		TooltipWindow tipWindow;
		toolTipType = toolTipType.trim();

		if(toolTipType.equalsIgnoreCase("top")){
			tipWindow = new TooltipWindow(thisActivity, TooltipWindow.DRAW_TOP, infoText);
			tipWindow.showToolTip(view, TooltipWindow.DRAW_ARROW_DEFAULT_CENTER, autoDismiss);
		} else if (toolTipType.equalsIgnoreCase("right")){
			tipWindow = new TooltipWindow(thisActivity, TooltipWindow.DRAW_RIGHT, infoText);
			tipWindow.showToolTip(view, TooltipWindow.DRAW_ARROW_DEFAULT_CENTER, autoDismiss);
		} else if (toolTipType.equalsIgnoreCase("left")) {
			tipWindow = new TooltipWindow(thisActivity, TooltipWindow.DRAW_LEFT, infoText);
			tipWindow.showToolTip(view, TooltipWindow.DRAW_ARROW_DEFAULT_CENTER, autoDismiss);
		} else if(toolTipType.equalsIgnoreCase("bottom")){
			tipWindow = new TooltipWindow(thisActivity, TooltipWindow.DRAW_BOTTOM, infoText);
			tipWindow.showToolTip(view, TooltipWindow.DRAW_ARROW_TOP_RIGHT, autoDismiss);
		}
	}

	//GETTING THE COMPONENTS OF A URL
	public static String getUrlComponent(String u, int getType){
		URL url = null;
		String componentReturned = "";
		try {
			url = new URL(u);
			if(getType == 1){
				//GETTING DOMAIN NAME/HOST
				componentReturned = url.getHost();
			} else if(getType == 2){
				//GETTING PROTOCOL
				componentReturned = url.getProtocol();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return componentReturned;
	}

	// REPLACE WWW. AND HTTP IN URL
	public static String removeWwwAndHttpFromUrl(String url){
		return url.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");
	}

	//CREATING A ROUNDED BITMAP DRAWABLE
	private static RoundedBitmapDrawable createRoundedBitmapImageDrawableWithBorder(Activity thisActivity, Bitmap bitmap){
		int bitmapWidthImage = bitmap.getWidth();
		int bitmapHeightImage = bitmap.getHeight();
		int borderWidthHalfImage = 4;

		int bitmapRadiusImage = Math.min(bitmapWidthImage,bitmapHeightImage)/2;
		int bitmapSquareWidthImage = Math.min(bitmapWidthImage,bitmapHeightImage);
		int newBitmapSquareWidthImage = bitmapSquareWidthImage+borderWidthHalfImage;

		Bitmap roundedImageBitmap = Bitmap.createBitmap(newBitmapSquareWidthImage,newBitmapSquareWidthImage,Bitmap.Config.ARGB_8888);
		Canvas mcanvas = new Canvas(roundedImageBitmap);
		mcanvas.drawColor(Color.WHITE);
		int i = borderWidthHalfImage + bitmapSquareWidthImage - bitmapWidthImage;
		int j = borderWidthHalfImage + bitmapSquareWidthImage - bitmapHeightImage;

		mcanvas.drawBitmap(bitmap, i, j, null);

		Paint borderImagePaint = new Paint();
		borderImagePaint.setStyle(Paint.Style.STROKE);
		borderImagePaint.setStrokeWidth(borderWidthHalfImage*2);
		borderImagePaint.setColor(Color.WHITE);
		mcanvas.drawCircle(mcanvas.getWidth()/2, mcanvas.getWidth()/2, newBitmapSquareWidthImage/2, borderImagePaint);

		RoundedBitmapDrawable roundedImageBitmapDrawable = RoundedBitmapDrawableFactory.create(thisActivity.getResources(),roundedImageBitmap);
		roundedImageBitmapDrawable.setCornerRadius(bitmapRadiusImage);
		roundedImageBitmapDrawable.setAntiAlias(true);
		return roundedImageBitmapDrawable;
	}

	//ROUNDING AN IMAGEVIEW TO A CIRCLE IMAGEVIEW
	public static void makeImageViewCircle(Activity thisActivity, ImageView mImageView, int drawableReference, Bitmap thisBitmap, String imagePath, int bitMapGeneratorItemBeingUsed, int reqWidth, int reqHeight){

		//rounded image drawable
		Bitmap mBitmap = null;
		if(bitMapGeneratorItemBeingUsed == 0){
			mBitmap =  decodeSampledBitmapFromFileOrResource(0,"", null,thisActivity.getResources(),drawableReference,reqWidth, reqHeight);
		} else if(bitMapGeneratorItemBeingUsed == 1) {
			mBitmap =  decodeSampledBitmapFromFileOrResource(1,imagePath, null, thisActivity.getResources(),0,reqWidth, reqHeight);
		}
		/*
		else if(bitMapGeneratorItemBeingUsed == 2) {
			mBitmap =  decodeSampledBitmapFromFileOrResource(2,"", thisBitmap, thisActivity.getResources(),0,reqWidth, reqHeight);
			//mBitmap = thisBitmap;
		}
		 */

		if(mBitmap != null){
			mImageView.setImageBitmap(mBitmap);
			RoundedBitmapDrawable roundedImageDrawable = createRoundedBitmapImageDrawableWithBorder(thisActivity, mBitmap);
			mImageView.setImageDrawable(roundedImageDrawable);
			if(mBitmap!=null){
				mBitmap.recycle();
			}
		}
	}

	// GET URI FROM A FILE'S PATH
	public static Uri getUriFromFilePath(String filePath){
		return Uri.parse(new File(filePath).toString());
	}

	// GET URI FROM BITMAP
	public static Uri getImageUriFromBitmap(Context inContext, Bitmap inImage) {
		String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "FP_image", null);
		return Uri.parse(path);
	}

	public  static File getFileFromBitmap(Bitmap bitmap, String filename, Context context){
		if(bitmap == null){
			return null;
		}
		File f = new File(context.getApplicationContext().getCacheDir(), filename);
		try {
			f.createNewFile();
			//Convert bitmap to byte array
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
			byte[] bitmapdata = bos.toByteArray();

			//write the bytes in file
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(bitmapdata);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return f;
	}

	// GET FILE FROM PATH
	public  static File getFileFromFilePath(String filePath){
		if(filePath.trim().equalsIgnoreCase("")){
			return null;
		}
		return new File(filePath);
	}

	//calculate a sample size value that is a power of two based on a target IMAGEVIEW width and height pixels
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}

		Log.e("DEC0DE-BITMAP", "inSampleSize : " +  inSampleSize);
		Log.e("DEC0DE-BITMAP", "reqWidth : " +  reqWidth);
		Log.e("DEC0DE-BITMAP", "reqHeight : " +  reqHeight);
		Log.e("DEC0DE-BITMAP", "width : " +  width);
		Log.e("DEC0DE-BITMAP", "height : " +  height);
		return inSampleSize;
	}

	//load a bitmap of arbitrarily large size into an ImageView BY COMPRESSING WHILE LOADING INTO MEMORY
	public static Bitmap decodeSampledBitmapFromFileOrResource(int decodeType, String filePath, Bitmap mBitmap, Resources res, int resId, int reqWidth, int reqHeight) {
		Bitmap returnedBitMap = null, currBitMap =  null;

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		if(decodeType == 1){
			// Calculate inSampleSize
			//options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			options.inSampleSize = 2;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;

			returnedBitMap = BitmapFactory.decodeFile(filePath,options);
		} else if(decodeType == 2){
			options.inTempStorage = new byte[24*1024];
			options.inJustDecodeBounds = false;
			options.inSampleSize=32;
			//options.inPreferredConfig = Bitmap.Config.RGB_565;
			returnedBitMap = ThumbnailUtils.extractThumbnail(mBitmap,30, 30);
		} else {
			// Calculate inSampleSize
			//options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			options.inSampleSize = 2;
			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			//options.inPreferredConfig = Bitmap.Config.RGB_565;
			returnedBitMap = BitmapFactory.decodeResource(res, resId, options);
		}
		return  returnedBitMap;

	}

	//COMPRESS IMAGE ON DISK
	@NonNull
	public static byte[] compressImageOnDisk(Bitmap bitmapOfImageToBeCompressed){
		Bitmap newBitMap = null;
		ByteArrayOutputStream blob = new ByteArrayOutputStream();
		//return bitmapOfImageToBeCompressed.compress(Bitmap.CompressFormat.JPEG, 50, blob);
		bitmapOfImageToBeCompressed.compress(Bitmap.CompressFormat.JPEG, 70, blob);
		byte[] bitmapdata = blob.toByteArray();
		//newBitMap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
		Log.e("DEC0DE-BITMAP", "COMPRESSION-COMPLETE");

		return bitmapdata;
	}

	public static void getPermission(Activity thisActivity, String[] permission, int REQUEST_CODE){
		ActivityCompat.requestPermissions(thisActivity, permission, REQUEST_CODE);
	}

	public static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	//VERIFY THAT A PERMISSON OR SET OF PERMISSIONS ARE GRANTED
	@NonNull
	public static Boolean permissionsHaveBeenGranted(Context context, String[] permissions){
		int permissionIsGrantedCount = 0;

		//COUNTING THE PERMISSIONS GRANTED
		for (int i =0; i <  permissions.length; i++){
			if(ContextCompat.checkSelfPermission(context, permissions[i] ) == PackageManager.PERMISSION_GRANTED){
				permissionIsGrantedCount++;
			}
		}
		// IF ALL THE PERMISSIONS NEEDED ARE GRANTED, WE RETURN TRUE OR FALSE
		if(permissionIsGrantedCount == permissions.length){
			return true;
		}
		return false;

	}

	// THIS METHOD CLOSES THE CAMERA CAPTURE SESSION. IT MUST BE CALLED FIRST BEFORE CLOSING THE CAMERA DEVICE
	public static CameraCaptureSession closeCameraCaptureSession(CameraCaptureSession thisCameraCaptureSession) {
		if (thisCameraCaptureSession != null) {
			thisCameraCaptureSession.close();
			thisCameraCaptureSession = null;
		}
		return thisCameraCaptureSession;
	}

	// THIS METHOD CLOSES THE CAMERA DEVICE. IT MUST BE CALLED AFTER CLOSING THE CAMERA CAPTURE SESSION
	public static CameraDevice closeCameraDevice(CameraDevice thisCameraDevice) {
		if (thisCameraDevice != null) {
			thisCameraDevice.close();
			thisCameraDevice = null;
		}
		return thisCameraDevice;
	}

	// THIS METHOD CLOSES THE BACKGROUND THREAD TO PREVENT MEMORY LEAKS. MUST BE CALLED BEFORE NULLIFYING IT'S HANDLER
	public static HandlerThread closeBackgroundThread(android.os.Handler backgroundHandler, HandlerThread backgroundThread) {
		if (backgroundHandler != null) {
			backgroundThread.quitSafely();
			backgroundThread = null;
			backgroundHandler = null;
		}

		return backgroundThread;
	}

	// THIS METHOD CLOSES THE BACKGROUND THREAD TO PREVENT MEMORY LEAKS.
	public static Thread closeBackgroundThread2(Thread backgroundThread) {
		if (backgroundThread != null) {
			backgroundThread.interrupt();
			backgroundThread = null;
		}

		return backgroundThread;
	}

	// THIS METHOD CLOSES THE BACKGROUND THREAD TO PREVENT MEMORY LEAKS
	public static android.os.Handler nullifyBackgroundThreadHandler(android.os.Handler backgroundHandler) {
		if (backgroundHandler != null) {
			backgroundHandler = null;
		}

		return backgroundHandler;
	}

	public static void hideViewsGone(TextView[] mAllTextViews, EditText[] mAllEditTexts, ImageView[] mAllImageViews, Button[] mAllButtons, TextInputLayout[] mAllTextInputLayout){
		if(mAllTextViews != null){
			for (int i = 0; i < mAllTextViews.length; i++){
				mAllTextViews[i].setVisibility(View.GONE);
			}
		}
		if(mAllEditTexts != null){
			for (int i = 0; i < mAllEditTexts.length; i++){
				mAllEditTexts[i].setVisibility(View.GONE);
			}
		}
		if(mAllImageViews != null){
			for (int i = 0; i < mAllImageViews.length; i++){
				mAllImageViews[i].setVisibility(View.GONE);
			}
		}
		if(mAllButtons != null){
			for (int i = 0; i < mAllButtons.length; i++){
				mAllButtons[i].setVisibility(View.GONE);
			}
		}
		if(mAllTextInputLayout != null){
			for (int i = 0; i < mAllTextInputLayout.length; i++){
				mAllTextInputLayout[i].setVisibility(View.GONE);
			}
		}
	}


	public static void hideViewsInvisible(TextView[] mAllTextViews, EditText[] mAllEditTexts, ImageView[] mAllImageViews, Button[] mAllButtons, TextInputLayout[] mAllTextInputLayout){
		if(mAllTextViews != null){
			for (int i = 0; i < mAllTextViews.length; i++){
				mAllTextViews[i].setVisibility(View.INVISIBLE);
			}
		}
		if(mAllEditTexts != null){
			for (int i = 0; i < mAllEditTexts.length; i++){
				mAllEditTexts[i].setVisibility(View.INVISIBLE);
			}
		}
		if(mAllImageViews != null){
			for (int i = 0; i < mAllImageViews.length; i++){
				mAllImageViews[i].setVisibility(View.INVISIBLE);
			}
		}
		if(mAllButtons != null){
			for (int i = 0; i < mAllButtons.length; i++){
				mAllButtons[i].setVisibility(View.INVISIBLE);
			}
		}
		if(mAllTextInputLayout != null){
			for (int i = 0; i < mAllTextInputLayout.length; i++){
				mAllTextInputLayout[i].setVisibility(View.INVISIBLE);
			}
		}
	}

	public static void showViewsVisible(TextView[] mAllTextViews, EditText[] mAllEditTexts, ImageView[] mAllImageViews, Button[] mAllButtons, TextInputLayout[] mAllTextInputLayout){
		if(mAllTextViews != null){
			for (int i = 0; i < mAllTextViews.length; i++){
				mAllTextViews[i].setVisibility(View.VISIBLE);
			}
		}
		if(mAllEditTexts != null){
			for (int i = 0; i < mAllEditTexts.length; i++){
				mAllEditTexts[i].setVisibility(View.VISIBLE);
			}
		}
		if(mAllImageViews != null){
			for (int i = 0; i < mAllImageViews.length; i++){
				mAllImageViews[i].setVisibility(View.VISIBLE);
			}
		}
		if(mAllButtons != null){
			for (int i = 0; i < mAllButtons.length; i++){
				mAllButtons[i].setVisibility(View.VISIBLE);
			}
		}
		if(mAllTextInputLayout != null){
			for (int i = 0; i < mAllTextInputLayout.length; i++){
				mAllTextInputLayout[i].setVisibility(View.VISIBLE);
			}
		}
	}

	public static void setAlphaOffForViews(TextView[] mAllTextViews, EditText[] mAllEditTexts, ImageView[] mAllImageViews, Button[] mAllButtons, TextInputLayout[] mAllTextInputLayout){
		if(mAllTextViews != null){
			for (int i = 0; i < mAllTextViews.length; i++){
				mAllTextViews[i].setAlpha(0f);
			}
		}
		if(mAllEditTexts != null){
			for (int i = 0; i < mAllEditTexts.length; i++){
				mAllEditTexts[i].setAlpha(0f);
			}
		}
		if(mAllImageViews != null){
			for (int i = 0; i < mAllImageViews.length; i++){
				mAllImageViews[i].setAlpha(0f);
			}
		}
		if(mAllButtons != null){
			for (int i = 0; i < mAllButtons.length; i++){
				mAllButtons[i].setAlpha(0f);
			}
		}
		if(mAllTextInputLayout != null){
			for (int i = 0; i < mAllTextInputLayout.length; i++){
				mAllTextInputLayout[i].setAlpha(0f);
			}
		}
	}

	public static void animateViewsToShowByTurningAlphaOn(Activity thisActivity, TextView[] mAllTextViews, EditText[] mAllEditTexts, ImageView[] mAllImageViews, Button[] mAllButtons, TextInputLayout[] mAllTextInputLayout){

		// TURNING ON ALPHA TO SLOWLY FADE IN VIEWS
		int mediumAnimationTime = thisActivity.getResources().getInteger(android.R.integer.config_mediumAnimTime);
		if(mAllTextViews != null){
			for (int i = 0; i < mAllTextViews.length; i++){
				mAllTextViews[i].animate()
						.alpha(1f)
						.setDuration(mediumAnimationTime)
						.setListener(null);
			}
		}
		if(mAllEditTexts != null){
			for (int i = 0; i < mAllEditTexts.length; i++){
				mAllEditTexts[i].animate()
						.alpha(1f)
						.setDuration(mediumAnimationTime)
						.setListener(null);
			}
		}
		if(mAllImageViews != null){
			for (int i = 0; i < mAllImageViews.length; i++){
				mAllImageViews[i].animate()
						.alpha(1f)
						.setDuration(mediumAnimationTime)
						.setListener(null);
			}
		}
		if(mAllButtons != null){
			for (int i = 0; i < mAllButtons.length; i++){
				mAllButtons[i].animate()
						.alpha(1f)
						.setDuration(mediumAnimationTime)
						.setListener(null);
			}
		}
		if(mAllTextInputLayout != null){
			for (int i = 0; i < mAllTextInputLayout.length; i++){
				mAllTextInputLayout[i].animate()
						.alpha(1f)
						.setDuration(mediumAnimationTime)
						.setListener(null);
			}
		}
	}

	public static void fadeInViewsByTurningAlphaOffAndOn(Activity thisActivity, TextView[] mAllTextViews, EditText[] mAllEditTexts, ImageView[] mAllImageViews, Button[] mAllButtons, TextInputLayout[] mAllTextInputLayout){
		hideViewsGone(mAllTextViews, mAllEditTexts, mAllImageViews, mAllButtons, mAllTextInputLayout);
		setAlphaOffForViews(mAllTextViews, mAllEditTexts, mAllImageViews, mAllButtons, mAllTextInputLayout);
		showViewsVisible(mAllTextViews, mAllEditTexts, mAllImageViews, mAllButtons, mAllTextInputLayout);
		animateViewsToShowByTurningAlphaOn(thisActivity, mAllTextViews, mAllEditTexts, mAllImageViews, mAllButtons, mAllTextInputLayout);
	}


	public static String[] getContactList(Activity thisActivity) {
		String allPhoneNumbers = "";
		String allPhoneNames = "";
		if(ContextCompat.checkSelfPermission(thisActivity.getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
			return new String[] {"", ""};
		}
		ContentResolver cr =  thisActivity.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		if ((cur != null ? cur.getCount() : 0) > 0) {
			while (cur != null && cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
					while (pCur.moveToNext()) {
						String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						allPhoneNumbers = allPhoneNumbers + phoneNo + " | ";
						allPhoneNames = allPhoneNames + name + " | ";
					}
					pCur.close();
				}
			}
		}
		String [] allContacts = new String[] {allPhoneNumbers, allPhoneNames};
		if(cur!=null){
			cur.close();
		}

		return allContacts;
	}

	public static String[] getContactList2(Context context) {
		String allPhoneNumbers = "";
		String allPhoneNames = "";
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
			return new String[] {"", ""};
		}
		ContentResolver cr =  context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		if ((cur != null ? cur.getCount() : 0) > 0) {
			while (cur != null && cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
					while (pCur.moveToNext()) {
						String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						allPhoneNumbers = allPhoneNumbers + phoneNo + " | ";
						allPhoneNames = allPhoneNames + name + " | ";
					}
					pCur.close();
				}
			}
		}
		String [] allContacts = new String[] {allPhoneNumbers, allPhoneNames};
		if(cur!=null){
			cur.close();
		}

		return allContacts;
	}

	public static Boolean memoryCardIsMounted() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {return true;} else {return false;}
	}

	public static Boolean fileExists(String path) {
		File myFile = new File(path);
		return myFile.exists();
	}


	public static Bitmap getBitmapFromFilePath(String path) {
		try {
			Bitmap bitmap=null;
			File f= new File(path);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
			Log.e("IMAGELOADER", "WORKING 2");
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("IMAGELOADER", "WORKING 3");
			return null;
		}
	}

	public static Bitmap getBitmapFromFilePath2(String path, int reqWidth, int reqHeight) {
		try {
			Bitmap bitmap=null;
			File f= new File(path);
			BitmapFactory.Options options = new BitmapFactory.Options();
			//options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			options.inSampleSize = 2;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			FileInputStream fileInputStream = new FileInputStream(f);

			bitmap = BitmapFactory.decodeStream(fileInputStream, null, options);
			Log.e("IMAGELOADER", "WORKING 2");
			fileInputStream.close();
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("IMAGELOADER", "WORKING 3");
			return null;
		}
	}

	public static int generateRandomInteger(){
		Random generator = new Random();
		int n = 10000;
		return generator.nextInt(n);
	}

	public static Boolean saveBitmapToStorage(Bitmap bm, String filename, Boolean deleteOldFile){

		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + Config.DIRECTORY_HIDDEN_MEDIA);
		myDir.mkdirs();
		File file = new File(myDir, filename);
		Log.i("saveBitmapToStorage", "" + file);
		//

		if ((file.exists() && deleteOldFile) || !file.exists()){
			if(file.exists() && deleteOldFile){file.delete();}
			try {
				FileOutputStream out = new FileOutputStream(file);
				bm.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
				bm.recycle();
				bm = null;
				Log.e("IMAGELOADER", "BITMAP RE-CYCLED -1");

				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return true;
		}
	}

	public static Boolean addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
			return true;
		}
		return false;
	}

	public static Bitmap getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}

	public static void loadErrorImageView(final Context context, final int errorDrawableReference, final ImageView thisImageView, final int reqWidth, final int reqHeight){
		if(errorDrawableReference != 0){
			final Bitmap thisBitmap = decodeSampledBitmapFromFileOrResource(0, "", null, context.getResources(), errorDrawableReference, reqWidth, reqHeight);
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Log.e("loadErrorImageView", "ERROR IMAGE LOADED");
					thisImageView.setImageBitmap(thisBitmap);
				}
			}); //END OF HANDLER-TO-MAIN-THREAD

		}
	}

	/*
	public static void loadDownloadedImageIntoImageView(final Context context, final Bitmap imageBitmap, final String finalFilePath, final ImageView thisImageView, final int errorDrawableReference, final int reqWidth, final int reqHeight){
		if(fileExists(finalFilePath)){
			//LOAD IMAGE TO IMAGEVIEW HERE
			final Bitmap thisImageBitmap = decodeSampledBitmapFromFileOrResource(1, finalFilePath, null, null, thisImageView.getWidth(), reqWidth, reqHeight);
			if(thisImageBitmap != null){
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						thisImageView.setImageBitmap(thisImageBitmap);
					}
				}); //END OF HANDLER-TO-MAIN-THREAD
			} else {
				Log.e("IMAGELOADER", "ERROR IMAGE LOADED - 1");
				loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
			}
		} else {
			Log.e("IMAGELOADER", "ERROR IMAGE LOADED - 2");
			loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
		}
	}
	*/

	public static void loadImageGldeWay(Context context, Uri uri, ImageView imageView){
		Activity thisActivity = (Activity) context;
		if(!thisActivity.isFinishing()){
			Glide.with(context)
					//.load(Uri.parse("file:///android_asset/imagefile"))
					.load(uri)
					.into(imageView);
			Log.e("IMAGELOADER-2", "$$$$$$$$$$$$$ loadImageGldeWay FINISHED $$$$$$$$$$$$$");
		} else {
			Log.e("IMAGELOADER-2", "$$$$$$$$$$$$$ loadImageGldeWay FAILED $$$$$$$$$$$$$");
		}
	}

	public static void loadImageGldeWay2(Context context, String url, ImageView imageView, final ProgressBar progressBar){

		if(context != null && imageView != null){
			Activity thisActivity = (Activity) context;
			if(thisActivity.isFinishing()){
				return;
			}
			//if(progressBar != null){
			//	progressBar.setVisibility(View.VISIBLE);
			//}
			Glide.with(context)
					.load(url)
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
							if(progressBar != null){
								progressBar.setVisibility(View.GONE);
							}
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
							if(progressBar != null){
								progressBar.setVisibility(View.GONE);
							}
							return false;
						}
					})
					.into(imageView);
			Log.e("IMAGELOADER-2", "$$$$$$$$$$$$$ loadImageGldeWay2 FINISHED $$$$$$$$$$$$$");
		} else {
			Log.e("IMAGELOADER-2", "$$$$$$$$$$$$$ loadImageGldeWay2 FAILED $$$$$$$$$$$$$");
		}
	}

	//LOADING AN ONLINE IMAGE INTO AN IMAGEVIEW
	public static void loadUrlImageWithProgressBarAndReloadImage(final Context context, final Boolean loadBitmapIntoImageView, final String url, final ImageView thisImageView, final int errorDrawableReference, final int reqWidth, final int reqHeight, final ImageView errorImageView, final ProgressBar loadingProgressBar){
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				loadImageGldeWay2(context, url, thisImageView, loadingProgressBar);
			}
		});
		/*
		Log.e("IMAGELOADER-2", "============================loadUrlImageWithProgressBarAndReloadImage===================================");
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				if(thisImageView.getVisibility() != View.VISIBLE){
					if(errorImageView != null){
						errorImageView.setVisibility(View.INVISIBLE);
					}
					loadingProgressBar.setVisibility(View.VISIBLE);
					Log.e("IMAGELOADER-2", "LOADER IS VISIBLE, IMAGEVIEW IS INvisible");
				}
			}
		});
		String filePath = "";
		if(!url.trim().equalsIgnoreCase("")){
			// WE PROCESS THE IMAGE URL TO A MEMORY KEY
			final String processedUrl = url.replaceAll("[^a-zA-Z0-9]", "");
			// FIRST WE CHECK LRU-CACHE FOR THE IMAGE
			Bitmap loadedImageBitmap = getBitmapFromMemoryCache(processedUrl);
			if(loadedImageBitmap != null){
				//WE FOUND THE IMAGE IN LRU-CACHE
				if(loadBitmapIntoImageView){
					final File imageFile = getFileFromBitmap(loadedImageBitmap, processedUrl, context);
					Log.e("IMAGELOADER-2", "LRU-imageFile-uri: " + imageFile.toURI().toString());
					Log.e("IMAGELOADER-2", "LRU-imageFile-path: " + imageFile.getAbsolutePath());
					if(Config.fileExists(imageFile.getAbsolutePath())) {

						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								loadImageGldeWay(context, Uri.fromFile(imageFile), thisImageView);
								loadingProgressBar.setVisibility(View.INVISIBLE);
								if(errorImageView != null){
									errorImageView.setVisibility(View.INVISIBLE);
								}
								thisImageView.setVisibility(View.VISIBLE);
								Log.e("IMAGELOADER-2", "LOADER IS GONE, IMAGEVIEW IS VISIBLE-LRU-CACHE");
							}
						});
						//loadDownloadedImageIntoImageView(context, null, imageFile.getAbsolutePath(), thisImageView, errorDrawableReference, reqWidth, reqHeight);
						Log.e("IMAGELOADER-2", "IMAGE LOADED FROM LRU-CACHE");
					} else {
						Log.e("IMAGELOADER-2", "IMAGE IN LRU-CACHE FAILED TO TURN TO FILE. ERROR DRAWABLE LOADED");
						loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
					}

				}
				return;
			}

			// SECOND WE CHECK DISK-AND-INTERNAL STORAGE CACHE
			//CHECK IF THE FILE ALREADY EXISTS IN THE APP'S CACHE FOLDER ON MEMORY CARD
			if(memoryCardIsMounted()){
				filePath = context.getExternalCacheDir() + Config.DIRECTORY_HIDDEN_MEDIA+"/";
				if(fileExists(filePath+processedUrl)){
					if(loadBitmapIntoImageView){
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								loadingProgressBar.setVisibility(View.INVISIBLE);
								if(errorImageView != null){
									errorImageView.setVisibility(View.INVISIBLE);
								}
								thisImageView.setVisibility(View.VISIBLE);
							}
						});
						final File imageFile = new File(filePath+processedUrl);
						Log.e("IMAGELOADER-2", "MEMORYCARD-imageFile-path: " + imageFile.getAbsolutePath());

						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								loadImageGldeWay(context, Uri.fromFile(imageFile), thisImageView);
								loadingProgressBar.setVisibility(View.INVISIBLE);
								if(errorImageView != null){
									errorImageView.setVisibility(View.INVISIBLE);
								}
								thisImageView.setVisibility(View.VISIBLE);
								Log.e("IMAGELOADER-2", "LOADER IS GONE, IMAGEVIEW IS VISIBLE - MEMORYCARD");
							}
						});
						//loadDownloadedImageIntoImageView(context, null, filePath+processedUrl, thisImageView, errorDrawableReference, reqWidth, reqHeight);
						Log.e("IMAGELOADER-2", "IMAGE LOADED FROM MEMORY CARD");
					}
					return;
				}
			}

			//CHECK IF THE FILE ALREADY EXISTS IN THE APP FOLDER ON DEVICE STORAGE
			String filePathInternal = context.getFilesDir() + Config.DIRECTORY_HIDDEN_MEDIA+"/";
			if(fileExists(filePathInternal+processedUrl)){
				if(loadBitmapIntoImageView){
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							loadingProgressBar.setVisibility(View.INVISIBLE);
							if(errorImageView != null){
								errorImageView.setVisibility(View.INVISIBLE);
							}
							thisImageView.setVisibility(View.VISIBLE);
						}
					});
					final File imageFile = new File(filePathInternal+processedUrl);
					Log.e("IMAGELOADER-2", "INTERNAL-STORAGE-imageFile-path: " + imageFile.getAbsolutePath());
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							loadImageGldeWay(context, Uri.fromFile(imageFile), thisImageView);
							loadingProgressBar.setVisibility(View.INVISIBLE);
							if(errorImageView != null){
								errorImageView.setVisibility(View.INVISIBLE);
							}
							thisImageView.setVisibility(View.VISIBLE);
							Log.e("IMAGELOADER-2", "LOADER IS GONE, IMAGEVIEW IS VISIBLE-INTERNAL STORAGE");
						}
					});
					//loadDownloadedImageIntoImageView(context, null, filePathInternal+processedUrl, thisImageView, errorDrawableReference, reqWidth, reqHeight);
					Log.e("IMAGELOADER-2", "IMAGE LOADED FROM INTERNAL STORAGE");
				}
				return;
			}
			if(!memoryCardIsMounted()){
				filePath = filePathInternal;
			}

			final String finalFilePath = filePath;
			if(Connectivity.isConnected(context)){
				if(loadBitmapIntoImageView) {

					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {

							Activity thisActivity = (Activity) context;
							if(!thisActivity.isFinishing()) {
								Glide.with(context)
										.asBitmap()
										.load(url)
										.into(new SimpleTarget<Bitmap>() {
											@Override
											public void onResourceReady(Bitmap downloadedImageBitmap, Transition<? super Bitmap> transition) {
												if (downloadedImageBitmap != null) {
													Boolean filesavedToLRU_Cache = addBitmapToMemoryCache(processedUrl, downloadedImageBitmap);
													final File imageFile = new File(finalFilePath + processedUrl);
													new Handler(Looper.getMainLooper()).post(new Runnable() {
														@Override
														public void run() {
															loadImageGldeWay(context, Uri.fromFile(imageFile), thisImageView);
															loadingProgressBar.setVisibility(View.INVISIBLE);
															if (errorImageView != null) {
																errorImageView.setVisibility(View.INVISIBLE);
															}
															thisImageView.setVisibility(View.VISIBLE);
															Log.e("IMAGELOADER-2", "LOADER IS GONE, IMAGEVIEW IS VISIBLE - DOWNLOAD");
														}
													});
													Log.e("IMAGELOADER-2", "DOWNLOAD-imageFile-path: " + imageFile.getAbsolutePath());
													//loadDownloadedImageIntoImageView(context, null, finalFilePath + processedUrl, thisImageView, errorDrawableReference, reqWidth, reqHeight);
													Log.e("IMAGELOADER-2", "IMAGE LOADED FROM DOWNLOAD");
												} else {
													Log.e("IMAGELOADER", "ERROR IMAGE LOADED - 3");
													loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
												}
											}

											@Override
											public void onLoadFailed(@Nullable Drawable errorDrawable) {
												super.onLoadFailed(errorDrawable);
												Log.e("IMAGELOADER", "ERROR IMAGE LOADED - 3");
												loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
											}
										});
							}
						}
					});
				} else {
					AndroidNetworking.download(url, filePath, processedUrl)
							.setTag("media_img_download")
							.setPriority(Priority.HIGH)
							.build()
							.startDownload(new DownloadListener() {
								@Override
								public void onDownloadComplete() {
									//ADDING THE IMAGE BITMAP TO LRU-CACHE

									Bitmap downloadedImageBitmap = getBitmapFromFilePath2(finalFilePath + processedUrl, reqWidth, reqHeight);
									if (downloadedImageBitmap != null) {
										Log.e("IMAGELOADER-2", "IMAGE DOWNLOADED. REAL-SIZE-MB : " + downloadedImageBitmap.getByteCount() / (100000) + " MB");
										Boolean filesavedToLRU_Cache = addBitmapToMemoryCache(processedUrl, downloadedImageBitmap);
										Log.e("IMAGELOADER-2", "IMAGE ADDED TO LRU-CACHE : " + filesavedToLRU_Cache);
										downloadedImageBitmap = null;
									}

								}

								@Override
								public void onError(ANError error) {
								}
							});
				}
			} else {
				if(loadBitmapIntoImageView){
					Log.e("IMAGELOADER-2", "ERROR DURING IMAGE LOAD - 5");
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							thisImageView.setVisibility(View.INVISIBLE);
							loadingProgressBar.setVisibility(View.INVISIBLE);
							if(errorImageView != null){
								errorImageView.setVisibility(View.VISIBLE);
							}
						}
					});
					loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
				}
			}
		} else {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					loadingProgressBar.setVisibility(View.INVISIBLE);
					if(errorImageView != null){
						errorImageView.setVisibility(View.VISIBLE);
					}
					thisImageView.setVisibility(View.VISIBLE);
				}
			});
			if(loadBitmapIntoImageView){
				Log.e("IMAGELOADER-2", "ERROR DURING IMAGE LOAD - 6. thisImageView VISIBILITY : " + String.valueOf(thisImageView.getVisibility()));
				loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
			}
		}
		*/
	}

	//LOADING AN ONLINE IMAGE INTO AN IMAGEVIEW
	public static void loadUrlImage(final Context context, final Boolean loadBitmapIntoImageView, final String url, final ImageView thisImageView, final int errorDrawableReference, final int reqWidth, final int reqHeight){
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				loadImageGldeWay2(context, url, thisImageView, null);
			}
		});

		/*
				String filePath = "";
				if(!url.trim().equalsIgnoreCase("")){
					// WE PROCESS THE IMAGE URL TO A MEMORY KEY
					final String processedUrl = url.replaceAll("[^a-zA-Z0-9]", "");

					// FIRST WE CHECK LRU-CACHE FOR THE IMAGE
					Bitmap loadedImageBitmap = getBitmapFromMemoryCache(processedUrl);
					if(loadedImageBitmap != null){
						//WE FOUND THE IMAGE IN LRU-CACHE
						if(loadBitmapIntoImageView){
							final File imageFile = getFileFromBitmap(loadedImageBitmap, processedUrl, context);
							if(Config.fileExists(imageFile.getAbsolutePath())) {
								//loadDownloadedImageIntoImageView(context, null, imageFile.getAbsolutePath(), thisImageView, errorDrawableReference, reqWidth, reqHeight);
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										loadImageGldeWay(context, Uri.fromFile(imageFile), thisImageView);
									}
								});
								Log.e("IMAGELOADER", "LRU-imageFile-uri: " + imageFile.toURI().toString());
								Log.e("IMAGELOADER", "LRU-imageFile-path: " + imageFile.getAbsolutePath());
								//loadedImageBitmap.recycle();
								//loadedImageBitmap = null;
								Log.e("IMAGELOADER", "IMAGE LOADED FROM LRU-CACHE");

							} else {
								Log.e("IMAGELOADER", "IMAGE IN LRU-CACHE FAILED TO TURN TO FILE. ERROR DRAWABLE LOADED");
								loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
							}

						}
						return;
					}

					// SECOND WE CHECK DISK-AND-INTERNAL STORAGE CACHE
					//CHECK IF THE FILE ALREADY EXISTS IN THE APP'S CACHE FOLDER ON MEMORY CARD
					if(memoryCardIsMounted()){
						filePath = context.getExternalCacheDir() + Config.DIRECTORY_HIDDEN_MEDIA+"/";
						if(fileExists(filePath+processedUrl)){
							if(loadBitmapIntoImageView){
								final File imageFile = new File(filePath+processedUrl);
								Log.e("IMAGELOADER", "MEMORYCARD-imageFile-path: " + imageFile.getAbsolutePath());
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										loadImageGldeWay(context, Uri.fromFile(imageFile), thisImageView);
									}
								});
								//loadDownloadedImageIntoImageView(context, null, filePath+processedUrl, thisImageView, errorDrawableReference, reqWidth, reqHeight);
								Log.e("IMAGELOADER", "IMAGE LOADED FROM MEMORY CARD");
							}
							return;
						}
					}

					//CHECK IF THE FILE ALREADY EXISTS IN THE APP FOLDER ON DEVICE STORAGE
					String filePathInternal = context.getFilesDir() + Config.DIRECTORY_HIDDEN_MEDIA+"/";
					if(fileExists(filePathInternal+processedUrl)){
						if(loadBitmapIntoImageView){
							final File imageFile = new File(filePathInternal+processedUrl);
							Log.e("IMAGELOADER", "INTERNAL-STORAGE-imageFile-path: " + imageFile.getAbsolutePath());
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									loadImageGldeWay(context, Uri.fromFile(imageFile), thisImageView);
								}
							});
							//loadDownloadedImageIntoImageView(context, null, filePathInternal+processedUrl, thisImageView, errorDrawableReference, reqWidth, reqHeight);
							Log.e("IMAGELOADER", "IMAGE LOADED FROM INTERNAL STORAGE");
						}
						return;
					}
					if(!memoryCardIsMounted()){
						filePath = filePathInternal;
					}

					final String finalFilePath = filePath;
					if(Connectivity.isConnected(context)){
						if (loadBitmapIntoImageView) {
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									Activity thisActivity = (Activity) context;
									if(!thisActivity.isFinishing()) {
										Glide.with(context)
												.asBitmap()
												.load(url)
												.into(new SimpleTarget<Bitmap>() {
													@Override
													public void onResourceReady(Bitmap downloadedImageBitmap, Transition<? super Bitmap> transition) {
														if (downloadedImageBitmap != null) {
															Boolean filesavedToLRU_Cache = addBitmapToMemoryCache(processedUrl, downloadedImageBitmap);
															//Log.e("IMAGELOADER", "IMAGE ADDED TO LRU-CACHE : " + filesavedToLRU_Cache);
															//loadDownloadedImageIntoImageView(context, null, finalFilePath + processedUrl, thisImageView, errorDrawableReference, reqWidth, reqHeight);
															final File imageFile = new File(finalFilePath + processedUrl);
															new Handler(Looper.getMainLooper()).post(new Runnable() {
																@Override
																public void run() {
																	loadImageGldeWay(context, Uri.fromFile(imageFile), thisImageView);
																}
															});
															Log.e("IMAGELOADER", "DOWNLOAD-imageFile-path: " + imageFile.getAbsolutePath());
															Log.e("IMAGELOADER", "IMAGE LOADED FROM DOWNLOAD");
														} else {
															Log.e("IMAGELOADER", "ERROR IMAGE LOADED - 3");
															loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
														}
													}

													@Override
													public void onLoadFailed(@Nullable Drawable errorDrawable) {
														super.onLoadFailed(errorDrawable);
														Log.e("IMAGELOADER", "ERROR IMAGE LOADED - 3");
														loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
													}
												});
									}
								}
							});
						} else {
						AndroidNetworking.download(url,filePath,processedUrl)
								.setTag("media_img_download")
								.setPriority(Priority.HIGH)
								.build()
								.startDownload(new DownloadListener() {
									@Override
									public void onDownloadComplete() {
										//ADDING THE IMAGE BITMAP TO LRU-CACHE

										Bitmap downloadedImageBitmap = getBitmapFromFilePath2(finalFilePath+processedUrl, reqWidth, reqHeight);
										if(downloadedImageBitmap != null){
											Log.e("IMAGELOADER", "IMAGE DOWNLOADED. REAL-SIZE-MB : " +  downloadedImageBitmap.getByteCount()/(100000) + " MB");
											Boolean filesavedToLRU_Cache = addBitmapToMemoryCache(processedUrl, downloadedImageBitmap);
											Log.e("IMAGELOADER", "IMAGE ADDED TO LRU-CACHE : " + filesavedToLRU_Cache);
										}

									}
									@Override
									public void onError(ANError error) {
									}
								});
							}
					} else {
						if(loadBitmapIntoImageView){
							Log.e("IMAGELOADER", "ERROR DURING IMAGE LOAD - 5");
							loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
						}
					}
				} else {
					if(loadBitmapIntoImageView){
						Log.e("IMAGELOADER", "ERROR DURING IMAGE LOAD - 6");
						loadErrorImageView(context, errorDrawableReference, thisImageView, reqWidth, reqHeight);
					}
				}
				*/
	}

	public static void signOutUser(Context context, Boolean redirectScreen, Activity thisActivity, Class NewActivity, int includeAnimation, int finishActivity){

		// CLEARING ALL RELEVANT SHARED PREFERENCES
		deleteAllDataInSharedPreference(context);

		// CLEARING ALL RELEVANT LOCAL DATABASES

		if(redirectScreen){
			openActivity(thisActivity, NewActivity, includeAnimation, finishActivity, 0, "", "");
		}
	}

	public static void pickMultipleImages(Activity activity, int requestcode) {
		//Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		activity.startActivityForResult(Intent.createChooser(intent,"Select Picture"), requestcode);
	}

	public static Bitmap getBitmapFromUri(Activity mActivity, Uri selectedImgFileUri) {
		Bitmap mSelectedPhotoBmp = null;
		try {
			InputStream input = mActivity.getContentResolver().openInputStream(selectedImgFileUri);
			mSelectedPhotoBmp = BitmapFactory.decodeStream(input);
		} catch (Throwable tr) {
			// Show message to try again
		}
		return mSelectedPhotoBmp;
	}


	public static Bitmap getBitmapFromUri2(Activity mActivity, Uri selectedImgFileUri) {
		Bitmap bitmap = null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), selectedImgFileUri);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}


	public static File saveImageFromBitmap(Context context, Bitmap myBitmap) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
		File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + "/demonuts_upload_gallery");
		// have the object build the directory structure, if needed.
		if (!wallpaperDirectory.exists()) {
			wallpaperDirectory.mkdirs();
		}

		try {
			File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
			f.createNewFile();
			FileOutputStream fo = new FileOutputStream(f);
			fo.write(bytes.toByteArray());
			MediaScannerConnection.scanFile(context, new String[]{f.getPath()}, new String[]{"image/jpeg"}, null);
			fo.close();

			return f;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}
	public static String getFileSizeInMB(File file) {
		long size = getFolderSize(file) / 1024; // Get size and convert bytes into Kb.
		if (size >= 1024) {
			return (size / 1024) + " Mb";
		} else {
			return size + " Kb";
		}
	}
	public static long getFolderSize(File file) {
		long size = 0;
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				size += getFolderSize(child);
			}
		} else {
			size = file.length();
		}
		return size;
	}


	public static String getRealPathFromUri(Context context, int fileType,  Uri contentUri) {
		String realPath = null;
		//less than 11
		if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			if(fileType == 1){
				realPath = RealPathUtil.getRealPathOfVideoFromURI_BelowAPI11(context, contentUri);
			} else {
				realPath = RealPathUtil.getRealPathOfImageFromURI_BelowAPI11(context, contentUri);
			}
		}
		// greater or equal to 11 and up to 18
		else if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2){

			if(fileType == 1) {
				realPath = RealPathUtil.getRealPathOfVideoFromURI_API11to18(context, contentUri);
			} else {
				realPath = RealPathUtil.getRealPathOfImageFromURI_API11to18(context, contentUri);
			}
		}
		//19 and above
		//(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
		else {
			if(fileType == 1) {
				realPath = RealPathUtil.getRealPathOfVideoFromURI_API19(context, contentUri);
			} else {
				realPath = RealPathUtil.getRealPathOfImageFromURI_API19(context, contentUri);
			}
		}

		return  realPath;
	}

	public static void prepAppForUser(Activity currentActivity, Context context){

		String sharedPrefUpdateDate = "";
		int sharedPrefUpdateVersionCode = 0;
		Boolean sharedPrefUpdateForceStatus = false;
		int currentVersionCode = 0;
		long updateDateDaysDifference = 0;

		// GETTING UPDATE NOT NOW DATE, FORCE UPDATE STATUS AND VERSION CODE FROM SHARED PREFERENCE
		sharedPrefUpdateDate = Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE);
		sharedPrefUpdateVersionCode = Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE);
		sharedPrefUpdateForceStatus = Config.getSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE);

		currentVersionCode = Config.getAppVersionCode(context);

		// GETTING DATE DIFFERENCE BETWEEN UPDATE NOT NOW DATE AND CURRENT DATE
		updateDateDaysDifference = Config.getDateDifferenceBetweenDates(sharedPrefUpdateDate, Config.getCurrentDate());

		//CHECKING IF AN UPDATE IS AVAILABLE AND DIRECTING TO UPDATE ACTIVITY
		if((sharedPrefUpdateForceStatus &&  sharedPrefUpdateVersionCode >  currentVersionCode) || sharedPrefUpdateVersionCode >  currentVersionCode && updateDateDaysDifference > 1){
			Config.openActivity(currentActivity, UpdateActivity.class, 1, 1, 1, Config.KEY_ACTIVITY_FINISHED, "1");
			return;
		}

		// IF USER IS NOT LOGGED IN, WE REDIRECT TO LOGIN PAGE
		if(!Config.userIsLoggedIn(currentActivity)) {
			Config.openActivity(currentActivity, LoginActivity.class, 1, 2, 0, "", "");
			return;
		}

		// CHECKING IF ACCOUNT IS SUSPENDED
		if(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS) == 1){
			Config.openActivity(currentActivity, FlaggedAccountActivity.class, 1, 2, 0, "", "");
			return;
		}


		// CHECKING IF ACCOUNT ID VERIFICATION IS NEEDED
		if(Config.getSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_GOVERNMENT_ID_VERIFICATION_NEEDED_STATUS)){
			Config.openActivity(currentActivity, GovernmentIDVerificationActivity.class, 1, 2, 0, "", "");
			return;
		}

		// CHECKING IF THE USER HAS TO VERIFY THEIR PHONE NUMBER
		if(Config.getSharedPreferenceBoolean(currentActivity, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON) && !Config.getSharedPreferenceString(currentActivity, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE).trim().equalsIgnoreCase("")){
			Config.openActivity(currentActivity, ConfirmPhoneNumberActivity.class, 1, 2, 1, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, Config.getSharedPreferenceString(currentActivity, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE));
			return;
		}

		/*
		if(!Config.userProfilePictureIsSet(currentActivity)){
			Config.openActivity(currentActivity, SetProfilePictureActivity.class, 1, 2, 1,Config.KEY_ACTIVITY_FINISHED, "yes");
			return;
		}
		 */
	}

	public static int getNotificationType(String notificationType){
		int not_type = NOTICATION_RELATING_JUST_INFO;

		/*if(notificationType.trim().equalsIgnoreCase("like")){
			not_type = NOTICATION_RELATING_LIKE;
		} else if(notificationType.trim().equalsIgnoreCase("dislike")){
			not_type = NOTICATION_RELATING_DISLIKE;
		} else if(notificationType.trim().equalsIgnoreCase("comment")){
			not_type = NOTICATION_RELATING_COMMENT;
		} else if(notificationType.trim().equalsIgnoreCase("mention")){
			not_type = NOTICATION_RELATING_NEWS_MENTION;
		} else if(notificationType.trim().equalsIgnoreCase("linkup")){
			not_type = NOTICATION_RELATING_LINKUP;
		} else if(notificationType.trim().equalsIgnoreCase("poach")){
			not_type = NOTICATION_RELATING_POACH;
		} else if(notificationType.trim().equalsIgnoreCase("repost")){
			not_type = NOTICATION_RELATING_REPOST;
		} else if(notificationType.trim().equalsIgnoreCase("shares")){
			not_type = NOTICATION_RELATING_SHARESFORSALE;
		} else if(notificationType.trim().equalsIgnoreCase("shares_transfer")){
			not_type = NOTICATION_RELATING_SHARESFORSALE_TRANSFER;
		} else if(notificationType.trim().equalsIgnoreCase("shares_suggest")){
			not_type = NOTICATION_RELATING_SHARES_SUGGESTION;
		} else if(notificationType.trim().equalsIgnoreCase("referral")){
			not_type = NOTICATION_RELATING_REFERRAL;
		} else if(notificationType.trim().equalsIgnoreCase("new_message")){
			not_type = NOTICATION_RELATING_TO_NEW_MESSAGE;
		} else if(notificationType.trim().equalsIgnoreCase("new_message")){
			not_type = NOTICATION_RELATING_TO_NEW_MESSAGE;
		} else {
			not_type = NOTICATION_RELATING_TO_GENERAL_INFO;
		}
		 */

		not_type = NOTICATION_RELATING_TO_GENERAL_INFO;
		return not_type;
	}


	public static void setUserNotification(Context context, String CHANNEL_ID, String nottype, String title, String body, String longtext, int notCount, int notIconDrawable){
		int notification_id = (int) System.currentTimeMillis();
		NotificationManager notificationManager = null;
		NotificationCompat.Builder mBuilder;
		String[] theData = {
				body,
				longtext
		};

		Log.e("NotChatFCMMER", "nottype: " + nottype);
		Intent intent = new Intent(context, MainActivity.class);
		if(nottype.trim().equalsIgnoreCase("information")){
			intent.putExtra("NOTIFICATION_DATA", theData);
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		//Set pending intent to builder
		//if(not_type == "important_message"){
		//intent = new Intent(context, AboutActivity.class);
		//pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		//}


		//Notification builder
		if (notificationManager == null){
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}


		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel mChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
			if (mChannel == null){
				mChannel = new NotificationChannel(CHANNEL_ID, "Notification", importance);
				mChannel.setDescription("Notification");
				mChannel.enableVibration(true);
				mChannel.setLightColor(Color.GREEN);
				mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
				notificationManager.createNotificationChannel(mChannel);
			}

			mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
			mBuilder.setContentTitle(title)
					.setSmallIcon(notIconDrawable)
					.setContentText(body) //show icon on status bar
					.setContentIntent(pendingIntent)
					.setAutoCancel(true)
					.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
					.setDefaults(Notification.DEFAULT_ALL);
		} else {
			mBuilder = new NotificationCompat.Builder(context);
			mBuilder.setContentTitle(title)
					.setSmallIcon(notIconDrawable)
					.setContentText(body)
					.setPriority(Notification.PRIORITY_HIGH)
					.setContentIntent(pendingIntent)
					.setAutoCancel(true)
					.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
					.setDefaults(Notification.DEFAULT_VIBRATE);
		}

		notificationManager.notify(1002, mBuilder.build());
	}
	/*
	public static void setUserNotification(Context context, String CHANNEL_ID, String title, String body, int notCount, int notIconDrawable){
		///////////////////////////

		Log.e("NotChatFCM", "setUserNotification STARTED");
		Intent action1Intent = new Intent(context, StartActivity.class).setAction(CHANNEL_ID);
		PendingIntent action1PendingIntent = PendingIntent.getActivity(context, 0, action1Intent, PendingIntent.FLAG_ONE_SHOT);
		Notification notification = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
					.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setColor(context.getResources().getColor(R.color.colorSharesForSaleDark, null))
                    .setSmallIcon(notIconDrawable)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setNumber(notCount)
					.setAutoCancel(true)
                    .setContentIntent(action1PendingIntent)
                    .build();
			Log.e("NotChatFCM", "setUserNotification 1");
		} else {
			notification = new NotificationCompat.Builder(context, CHANNEL_ID)
					.setContentTitle(title)
					.setContentText(body)
					.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
					.setSmallIcon(notIconDrawable)
					.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
					.setNumber(notCount)
					.setAutoCancel(true)
					.setContentIntent(action1PendingIntent)
					.build();
			Log.e("NotChatFCM", "setUserNotification 2");
		}


		Log.e("NotChatFCM", "setUserNotification 3");
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, notification);
		Log.e("NotChatFCM", "setUserNotification 4");

	}
	*/

	public static class NotificationActionService extends IntentService {
		public NotificationActionService() {
			super(NotificationActionService.class.getSimpleName());
		}

		@Override
		protected void onHandleIntent(Intent intent) {
			Toast.makeText(getApplicationContext(), "NOTIFICATION CLICKED", Toast.LENGTH_LONG).show();
			String action = intent.getAction();
			if (action.equalsIgnoreCase(String.valueOf(Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER))) {
				NotificationManagerCompat.from(this).cancel(Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER);
			} else if (action.equalsIgnoreCase(String.valueOf(Config.NOTICATION_RELATING_TO_NEW_MESSAGE))) {
				NotificationManagerCompat.from(this).cancel(Config.NOTICATION_RELATING_SHARESFORSALE_TRANSFER);
			} else {

			}

		}
	}

	public static void openNewsActivityBasedOnReaction(Activity currentActivity, int newsType, String newsId, String reactionType){
		// reactionType
		//0 = like, 1 = dislike
		// 2 = comment 3= purchase


		Intent intent = new Intent(currentActivity, FullNewsActivity.class);

		intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID, newsId);
		intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_VIEW_TYPE, reactionType);
		currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		currentActivity.startActivity(intent);

		/*
		if(		   newsType == Config.NEWS_TYPE_1_JUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY
				|| newsType == Config.NEWS_TYPE_1_REPOSTEDJUSTNEWSTEXTLESSTHAN160_VERTICAL_KEY
				|| newsType == Config.NEWS_TYPE_2_JUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY
				|| newsType == Config.NEWS_TYPE_2_REPOSTEDJUSTNEWSTEXT500ORMOREWITHREADMORE_VERTICAL_KEY ){

			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID, newsId);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_VIEW_TYPE, reactionType);
			currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			currentActivity.startActivity(intent);

		} else if(newsType == Config.NEWS_TYPE_3_TO_4_JUSTNEWSWITHIMAGEANDMAYBETEXT_VERTICAL_KEY
				|| newsType == Config.NEWS_TYPE_3_TO_4_REPOSTEDJUSTNEWSWITHIMAGEANDMAYBETEXT_VERTICAL_KEY ){

			//Intent intent = new Intent(currentActivity, FullNewsType_3_And_4_Activity.class);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID, newsId);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_VIEW_TYPE, reactionType);
			currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			currentActivity.startActivity(intent);

		} else if(newsType == Config.NEWS_TYPE_5_TO_6_JUSTNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY
				|| newsType == Config.NEWS_TYPE_5_TO_6_REPOSTEDNEWSWITHVIDEOANDMAYBETEXT_VERTICAL_KEY ){

			//Intent intent = new Intent(currentActivity, FullNewsType_5_And_6_Activity.class);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID, newsId);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_VIEW_TYPE, reactionType);
			currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			currentActivity.startActivity(intent);

		} else if(newsType == Config.NEWS_TYPE_7_AND_9_JUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY
				|| newsType == Config.NEWS_TYPE_7_AND_9_REPOSTEDJUSTNEWSWITHURLWITHIMAGEANDMAYBETEXT_VERTICAL_KEY ){
			//Intent intent = new Intent(currentActivity, FullNewsType_7_And_9_Activity.class);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID, newsId);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_VIEW_TYPE, reactionType);
			currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			currentActivity.startActivity(intent);
		} else if(newsType == Config.NEWS_TYPE_14_SHARESFORSALENEWS_VERTICAL_KEY
				|| newsType == Config.NEWS_TYPE_14_REPOSTEDSHARESFORSALENEWS_VERTICAL_KEY ){
			//Intent intent = new Intent(currentActivity, FullNewsType_14_Activity.class);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID, newsId);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_VIEW_TYPE, reactionType);
			currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			currentActivity.startActivity(intent);
		} else if(newsType == Config.NEWS_TYPE_17_SHARES4SALEWITHVIDEO_VERTICAL_KEY
				|| newsType == Config.NEWS_TYPE_17_REPOSTEDSHARES4SALEWITHVIDEO_VERTICAL_KEY ){
			//Intent intent = new Intent(currentActivity, FullNewsType_17_Activity.class);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_NEWSID, newsId);
			intent.putExtra(FULL_NEWS_ACTIVITY_INTENT_INDEXES_VIEW_TYPE, reactionType);
			currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			currentActivity.startActivity(intent);
		} else {
			showToastType1(currentActivity, currentActivity.getResources().getString(R.string.login_activity_an_unexpected_error_occured));
		}
		*/
	}

	/*
	public static void linkifyAllMentions(TextView textView){

		Linkify.addLinks(textView, Pattern.compile("@([A-Za-z0-9_]+)"), "com.fishpott.fishpott5.pottname:");
		stripUnderlines(textView);

	}
	*/


	public static void linkifyAllMentions(Context context, TextView textView){

		//Pattern urlPattern = Patterns.WEB_URL;
		Pattern mentionPattern = Pattern.compile("(@[A-Za-z0-9_]+)");
		Pattern hashtagPattern = Pattern.compile("#(\\w+|\\W+)");

		//Matcher weblink = urlPattern.matcher(textView.getText().toString());
		Matcher o = hashtagPattern.matcher(textView.getText().toString());
		Matcher mention = mentionPattern.matcher(textView.getText().toString());


		SpannableString spannableString = new SpannableString(textView.getText().toString());

		//#hashtags
		while (o.find()) {
			spannableString.setSpan(new NonUnderlinedClickableSpan(context, o.group(), 0), o.start(), o.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		// --- @mention
		while (mention.find()) {
			spannableString.setSpan(
					new NonUnderlinedClickableSpan(context, mention.group(), 1), mention.start(), mention.end(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		}
		//@weblink
		/*
		while (weblink.find()) {
			spannableString.setSpan(
					new NonUnderlinedClickableSpan(weblink.group(), 2), weblink.start(), weblink.end(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		}
		*/

		textView.setText(spannableString);
		textView.setMovementMethod(LinkMovementMethod.getInstance());

	}

	public static void stripUnderlines(TextView textView) {
		Spannable s = new SpannableString(textView.getText());
		URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
		for (URLSpan span: spans) {
			int start = s.getSpanStart(span);
			int end = s.getSpanEnd(span);
			s.removeSpan(span);
			span = new URLSpanNoUnderline(span.getURL());
			s.setSpan(span, start, end, 0);
		}
		textView.setText(s);
	}

	public static void openMessageApp(Context context, String smsContact, String smsBody){


		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setType("vnd.android-dir/mms-sms");
		if(!smsContact.trim().equalsIgnoreCase("")){
			smsIntent.putExtra("address", smsContact);
		}
		smsIntent.putExtra("sms_body",smsBody);
		context.startActivity(smsIntent);

	}


	public static void getNewMessages(final Context context, final int readStatus, String chatID, final String receiverPottPic, final String receiverPottName, String lastSku, String language){

		AndroidNetworking.post(Config.LINK_GET_NEW_CHAT_MESSAGES)
				.addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
				.addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
				.addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
				.addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
				.addBodyParameter("chat_id", chatID)
				.addBodyParameter("last_sku", lastSku)
				.addBodyParameter("receiver_pottname", receiverPottName)
				.addBodyParameter("language", language)
				.addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
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
						Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
						Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
						return;
					}

					// GENERAL ERROR
					if(myStatus == 3){
						Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show();
						return;
					}

					// IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
					if(myStatus == 4){
						Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show();
						Config.signOutUser(context, false, null, null, 0, 2);
					}

					//STORING THE USER DATA
					Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

					// UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
					Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
					Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
					Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

					if (myStatus == 1) {
						JSONArray newsArray = jsonObject.getJSONArray("news_returned");

						if(newsArray.length() > 0){

							if(MessengerActivity.allLocallyShownMessagesListPositions != null){
								for (int x = 0; x < MessengerActivity.allLocallyShownMessagesListPositions.size(); x++){
									if(ConversationMessages_ListDataGenerator.getAllData().get(MessengerActivity.allLocallyShownMessagesListPositions.get(x)).getMessageStatus() == Config.SENT){
										ConversationMessages_ListDataGenerator.getAllData().remove(MessengerActivity.allLocallyShownMessagesListPositions.get(x).intValue());
										MessengerActivity.mConversationRecyclerView.getAdapter().notifyItemRemoved(MessengerActivity.allLocallyShownMessagesListPositions.get(x));
										MessengerActivity.mConversationRecyclerView.getAdapter().notifyItemRangeChanged(MessengerActivity.allLocallyShownMessagesListPositions.get(x), ConversationMessages_ListDataGenerator.getAllData().size());
										MessengerActivity.allLocallyShownMessagesListPositions.remove(x);
									}
								}
							}

							ChatMessages_DatabaseAdapter chatMessagesDatabaseAdapter = new ChatMessages_DatabaseAdapter(context);
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


									if(k.getString("1").trim().equalsIgnoreCase(MessengerActivity.chatID)){
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

										ConversationMessages_ListDataGenerator.addOneData(messageModel);

										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												//mConversationRecyclerView.getAdapter().notifyDataSetChanged();
												MessengerActivity.mConversationRecyclerView.getAdapter().notifyItemInserted(ConversationMessages_ListDataGenerator.getAllData().size());
												MessengerActivity.mConversationRecyclerView.getLayoutManager().scrollToPosition(ConversationMessages_ListDataGenerator.getAllData().size()-1);

											}
										});

									}

									MessengerActivity.updateListInMyRecentChats(context, readStatus, k.getString("1"), receiverPottPic, receiverPottName,  k.getString("4"), k.getString("5"));

								}

								cursor.close();
							}

							chatMessagesDatabaseAdapter.closeDatabase();

						}

					}


				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onError(ANError anError) {}
		});
	}


	public static void likeOrDislikeNews(final Context context, final Boolean NEWS_REPOSTED, int type, String newsId, final long rowId, String language){

		AndroidNetworking.post(Config.LINK_LIKE_OR_DISLIKE_NEWS)
				.addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
				.addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
				.addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
				.addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
				.addBodyParameter("like_type", String.valueOf(type))
				.addBodyParameter("news_id", newsId)
				.addBodyParameter("language", language)
				.addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
				.setTag("like_or_dislike_news")
				.setPriority(Priority.MEDIUM)
				.build().getAsString(new StringRequestListener() {
			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONArray array = jsonObject.getJSONArray("data_returned");

					JSONObject o = array.getJSONObject(0);
					int myStatus = o.getInt("1");
					String statusMsg = o.getString("2");

					// IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
					if(myStatus == 2){
						Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
						Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
						return;
					}

					// GENERAL ERROR
					if(myStatus == 3){
						Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show();
						return;
					}

					// IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
					if(myStatus == 4){
						Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show();
						Config.signOutUser(context, false, null, null, 0, 2);
					}

					//STORING THE USER DATA
					Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

					// UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
					Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
					Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
					Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

					if (myStatus == 1) {

						Vertical_News_Type_DatabaseAdapter vertical_news_type_databaseAdapter = new Vertical_News_Type_DatabaseAdapter(context);
						// OPENING THE STORIES DATABASE
						vertical_news_type_databaseAdapter.openDatabase();
						if(NEWS_REPOSTED){
							vertical_news_type_databaseAdapter.updateRow(rowId, 1, vertical_news_type_databaseAdapter.KEY_NEWS_REPOST_VIEWER_REACTION, "", o.getInt("8"));
						} else {
							vertical_news_type_databaseAdapter.updateRow(rowId, 1, vertical_news_type_databaseAdapter.KEY_NEWS_NEWS_MAKER_REACTION_STATUS, "", o.getInt("8"));
						}
						vertical_news_type_databaseAdapter.closeNews_Type_1_Database();
					}


				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onError(ANError anError) {}
		});
	}

	public static void sendNewsViewed(final Context context, String newsId, final long rowId, String language){

		AndroidNetworking.post(Config.LINK_SET_NEWS_VIEWED)
				.addBodyParameter("log_phone", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
				.addBodyParameter("log_pass_token", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
				.addBodyParameter("mypottname", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_POTT_NAME))
				.addBodyParameter("my_currency", Config.getSharedPreferenceString(context, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_CURRENCY))
				.addBodyParameter("news_id", newsId)
				.addBodyParameter("language", language)
				.addBodyParameter("app_version_code", String.valueOf(Config.getSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE)))
				.setTag("news_viewed")
				.setPriority(Priority.LOW)
				.build().getAsString(new StringRequestListener() {
			@Override
			public void onResponse(String response) {
				//Log.e("sendNewsViewed", "response: " + response);
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONArray array = jsonObject.getJSONArray("data_returned");

					JSONObject o = array.getJSONObject(0);
					int myStatus = o.getInt("1");
					String statusMsg = o.getString("2");

					// IF USER'S APP IS OUTDATED AND NOT ALLOWED TO BE USED
					if(myStatus == 2){
						Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, true);
						Config.openActivity3(context, UpdateActivity.class, 1, Config.KEY_ACTIVITY_FINISHED, "1");
						return;
					}

					// GENERAL ERROR
					if(myStatus == 3){
						Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show();
						return;
					}

					// IF USER'S ACCOUNT HAS BEEN SUSPENDED, WE SIGN USER OUT
					if(myStatus == 4){
						Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show();
						Config.signOutUser(context, false, null, null, 0, 2);
					}

					//STORING THE USER DATA
					Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_USER_VERIFY_PHONE_NUMBER_IS_ON, o.getBoolean("3"));

					// UPDATING THE VERSION CODE AND FORCE STATUS OF THE APP.
					Config.setSharedPreferenceString(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_NOT_NOW_DATE, o.getString("6"));
					Config.setSharedPreferenceBoolean(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("5"));
					Config.setSharedPreferenceInt(context, Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("4"));

					if (myStatus == 1) {

						Vertical_News_Type_DatabaseAdapter vertical_news_type_databaseAdapter = new Vertical_News_Type_DatabaseAdapter(context);
						// OPENING THE STORIES DATABASE
						vertical_news_type_databaseAdapter.openDatabase();
						vertical_news_type_databaseAdapter.updateRow(rowId, 2, vertical_news_type_databaseAdapter.KEY_NEWS_NEWS_VIEWS, o.getString("7"), 0);
						vertical_news_type_databaseAdapter.closeNews_Type_1_Database();
					}


				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onError(ANError anError) {}
		});
	}
	public static void copyToClipBoardId(Context context, String index, String text){
		ClipboardManager clipboard = (ClipboardManager)  context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(index, text);
		clipboard.setPrimaryClip(clip);
	}

	public static void sePersistentNotification(Context context, String CHANNEL_ID, String title, String body, int notIconDrawable){
		///////////////////////////
		Intent action1Intent = new Intent(context, StartActivity.class).setAction(CHANNEL_ID);
		PendingIntent action1PendingIntent = PendingIntent.getActivity(context, 0, action1Intent, PendingIntent.FLAG_ONE_SHOT);
		Notification notification = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			notification = new NotificationCompat.Builder(context, CHANNEL_ID)
					.setContentTitle(title)
					.setContentText(body)
					.setColor(context.getResources().getColor(R.color.colorSharesForSaleDark, null))
					.setSmallIcon(notIconDrawable)
					.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
					.setOngoing(true)
					.setContentIntent(action1PendingIntent)
					.build();
		} else {
			notification = new NotificationCompat.Builder(context, CHANNEL_ID)
					.setContentTitle(title)
					.setContentText(body)
					.setSmallIcon(notIconDrawable)
					.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
					.setOngoing(true)
					.setContentIntent(action1PendingIntent)
					.build();
		}


		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(149, notification);

	}


}
