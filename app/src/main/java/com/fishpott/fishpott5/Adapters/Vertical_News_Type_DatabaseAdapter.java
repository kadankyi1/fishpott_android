package com.fishpott.fishpott5.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by zatana on 4/14/19.
 */

public class Vertical_News_Type_DatabaseAdapter {

    private static final String TAG = "News_Type_1_DBAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NEWS_NEWS_ID = "NEWS_ID";
    public static final String KEY_NEWS_NEWS_TEXT = "NEWS_TEXT";
    public static final String KEY_NEWS_NEWS_TIME = "NEWS_TIME";
    public static final String KEY_NEWS_NEWS_LIKES = "NEWS_LIKES";
    public static final String KEY_NEWS_NEWS_DISLIKES = "NEWS_DISLIKES";
    public static final String KEY_NEWS_NEWS_COMMENTS = "NEWS_COMMENTS";
    public static final String KEY_NEWS_NEWS_VIEWS = "NEWS_VIEWS";
    public static final String KEY_NEWS_NEWS_TRANSACTIONS = "NEWS_TRANSACTIONS";
    public static final String KEY_NEWS_NEWS_SHARES = "NEWS_SHARES";
    public static final String KEY_NEWS_NEWS_MAKER_ID = "NEWS_MAKER_ID";
    public static final String KEY_NEWS_NEWS_MAKER_POTTNAME = "NEWS_MAKER_POTTNAME";
    public static final String KEY_NEWS_NEWS_MAKER_FULLNAME = "NEWS_MAKER_FULLNAME";
    public static final String KEY_NEWS_NEWS_MAKER_POTTPIC = "NEWS_MAKER_POTTPIC";
    public static final String KEY_NEWS_NEWS_MAKER_REACTION_STATUS = "NEWS_MAKER_REACTIONS_STATUS";
    public static final String KEY_NEWS_NEWS_ADDED_ITEM_ID = "NEWS_ADDED_ITEM_ID";
    public static final String KEY_NEWS_NEWS_ADDED_ITEM_PRICE = "NEWS_ADDED_ITEM_PRICE";
    public static final String KEY_NEWS_NEWS_ADDED_ITEM_ICON = "NEWS_ADDED_ITEM_ICON";
    public static final String KEY_NEWS_NEWS_ADDED_ITEM_QUANTITY = "NEWS_ADDED_ITEM_QUANTITY";
    public static final String KEY_NEWS_NEWS_TYPE = "NEWS_TYPE";
    public static final String KEY_NEWS_NEWS_MAKER_ACCOUNT_TYPE = "NEWS_MAKER_ACCOUNT_TYPE";
    public static final String KEY_NEWS_NEWS_MAKER_VERIFIED_STATUS = "NEWS_MAKER_VERIFIED_STATUS";
    public static final String KEY_NEWS_NEWS_IMAGES_LINKS_SEPARATED_BY_SPACES = "NEWS_IMAGES_LINKS_SEPARATED_BY_SPACES";
    public static final String KEY_NEWS_NEWS_IMAGES_COUNT = "NEWS_IMAGES_COUNT";
    public static final String KEY_NEWS_NEWS_TEXT_READ_MORE_STATUS = "NEWS_TEXT_READ_MORE";
    public static final String KEY_NEWS_NEWS_ADDED_ITEM_TYPE = "NEWS_ADDED_ITEM_TYPE";
    public static final String KEY_NEWS_NEWS_ADDED_ITEM_STATUS = "NEWS_ADDED_ITEM_STATUS";
    public static final String KEY_NEWS_NEWS_VIDEOS_LINKS_SEPARATED_BY_SPACES = "NEWS_VIDEOS_LINKS_SEPARATED_BY_SPACES";
    public static final String KEY_NEWS_NEWS_VIDEOS_COVERARTS_LINKS_SEPARATED_BY_SPACES = "NEWS_VIDEOS_COVERARTS_LINKS_SEPARATED_BY_SPACES";
    public static final String KEY_NEWS_NEWS_VIDEOS_COUNT = "NEWS_VIDEOS_COUNT";
    public static final String KEY_NEWS_NEWS_URL = "NEWS_URL";
    public static final String KEY_NEWS_NEWS_URL_TITLE = "NEWS_URL_TITLE";
    public static final String KEY_NEWS_NEWS_ITEM_NAME = "NEWS_ITEM_NAME";
    public static final String KEY_NEWS_NEWS_ITEM_LOCATION = "NEWS_ITEM_LOCATION";
    public static final String KEY_NEWS_NEWS_ITEM_VERIFIED_STATUS = "NEWS_ITEM_VERIFIED_STATUS";
    public static final String KEY_NEWS_ADVERT_ICON = "ADVERT_ICON";
    public static final String KEY_NEWS_ADVERT_TEXT_TITLE = "ADVERT_TEXT_TITLE";
    public static final String KEY_NEWS_ADVERT_TEXT_TITLE2 = "ADVERT_TEXT_TITLE2";
    public static final String KEY_NEWS_ADVERT_BUTTON_TEXT = "ADVERT_BUTTON_TEXT";
    public static final String KEY_NEWS_ADVERT_LINK = "ADVERT_LINK";
    public static final String KEY_NEWS_REPOST_MAKER_POTTNAME = "REPOST_MAKER_POTTNAME";
    public static final String KEY_NEWS_REPOST_TEXT = "REPOST_TEXT";
    public static final String KEY_NEWS_REPOST_ICON = "REPOST_ICON";
    public static final String KEY_NEWS_REPOST_VIEWER_REACTION = "REPOST_VIEWER_REACTION";
    public static final String KEY_NEWS_REPOST_ADDITEM_PRICE = "REPOST_ADDITEM_PRICE";
    public static final String KEY_NEWS_BACKGROUND_COLOR = "BACKGROUND_COLOR";
    public static final String KEY_NEWS_VIEWS_REPORT_OR_PUCHASES_SHOWN_STATUS = "VIEWS_REPORT_OR_PUCHASES_SHOWN_STATUS";
    public static final String KEY_NEWS_NEWS_SKU = "NEWS_SKU";
    public static final String KEY_NEWS_REPOSTED_ADDED_ITEM_ID = "NEWS_REPOSTED_ADDED_ITEM_ID";
    public static final String KEY_NEWS_REPOSTED_ID = "NEWS_REPOSTED_ID";
    public static final String KEY_NEWS_NEWS_ITEM_PARENT_ID = "NEWS_ITEM_PARENT_ID";
    public static final String KEY_NEWS_REPOST_ITEM_PARENT_ID = "REPOST_ITEM_PARENT_ID";
    public static final String KEY_NEWS_REPOST_ITEM_NAME = "REPOST_ITEM_NAME";
    public static final String KEY_NEWS_REAL_ITEM_NAME = "REAL_ITEM_NAME";
    public static final String KEY_NEWS_REPOST_ITEM_QUANTITY = "REPOST_ITEM_QUANTITY";

    public static final int COL_ROWID = 0;
    public static final int COL_NEWS_ID = 1;
    public static final int COL_NEWS_TEXT = 2;
    public static final int COL_NEWS_TIME = 3;
    public static final int COL_NEWS_LIKES = 4;
    public static final int COL_NEWS_DISLIKES = 5;
    public static final int COL_NEWS_COMMENTS = 6;
    public static final int COL_NEWS_VIEWS = 7;
    public static final int COL_NEWS_TRANSACTIONS = 8;
    public static final int COL_NEWS_REPOSTS = 9;
    public static final int COL_NEWS_MAKER_ID = 10;
    public static final int COL_NEWS_MAKER_POTTNAME = 11;
    public static final int COL_NEWS_MAKER_FULLNAME = 12;
    public static final int COL_NEWS_MAKER_POTTPIC = 13;
    public static final int COL_NEWS_MAKER_REACTIONS_STATUS = 14;
    public static final int COL_NEWS_ADDED_ITEM_ID = 15;
    public static final int COL_NEWS_ADDED_ITEM_PRICE = 16;
    public static final int COL_NEWS_ADDED_ITEM_ICON = 17;
    public static final int COL_NEWS_ADDED_ITEM_QUANTITY = 18;
    public static final int COL_NEWS_TYPE = 19;
    public static final int COL_NEWS_MAKER_ACCOUNT_TYPE = 20;
    public static final int COL_NEWS_MAKER_VERIFIED_STATUS = 21;
    public static final int COL_NEWS_IMAGES_LINKS_SEPARATED_BY_SPACES = 22;
    public static final int COL_NEWS_IMAGES_COUNT = 23;
    public static final int COL_NEWS_TEXT_READ_MORE = 24;
    public static final int COL_NEWS_ADDED_ITEM_TYPE = 25;
    public static final int COL_NEWS_ADDED_ITEM_STATUS = 26;
    public static final int COL_NEWS_VIDEOS_LINKS_SEPARATED_BY_SPACES = 27;
    public static final int COL_NEWS_VIDEOS_COVERARTS_LINKS_SEPARATED_BY_SPACES = 28;
    public static final int COL_NEWS_VIDEOS_COUNT = 29;
    public static final int COL_NEWS_URL = 30;
    public static final int COL_NEWS_URL_TITLE = 31;
    public static final int COL_NEWS_ITEM_NAME = 32;
    public static final int COL_NEWS_ITEM_LOCATION = 33;
    public static final int COL_NEWS_ITEM_VERIFIED_STATUS = 34;
    public static final int COL_NEWS_ADVERT_ICON = 35;
    public static final int COL_NEWS_ADVERT_TEXT_TITLE = 36;
    public static final int COL_NEWS_ADVERT_TEXT_TITLE2 = 37;
    public static final int COL_NEWS_ADVERT_BUTTON_TEXT = 38;
    public static final int COL_NEWS_ADVERT_LINK = 39;
    public static final int COL_NEWS_REPOST_MAKER_POTTNAME = 40;
    public static final int COL_NEWS_REPOST_TEXT = 41;
    public static final int COL_NEWS_REPOST_ICON = 42;
    public static final int COL_NEWS_REPOST_VIEWER_REACTION = 43;
    public static final int COL_NEWS_REPOST_ADDITEM_PRICE = 44;
    public static final int COL_NEWS_BACKGROUND_COLOR = 45;
    public static final int COL_NEWS_VIEWS_REPORT_OR_PUCHASES_SHOWN_STATUS = 46;
    public static final int COL_NEWS_NEWS_SKU = 47;
    public static final int COL_NEWS_REPOSTED_ADDED_ITEM_ID = 48;
    public static final int COL_NEWS_REPOSTED_ID = 49;
    public static final int COL_NEWS_ITEM_PARENT_ID = 50;
    public static final int COL_REPOST_ITEM_PARENT_ID = 51;
    public static final int COL_REPOST_ITEM_NAME = 52;
    public static final int COL_NEWS_REAL_ITEM_NAME = 53;
    public static final int COL_REPOST_ITEM_QUANTITY = 54;


    public static final String[] ALL_KEYS = new String[] {
            KEY_ROWID,
            KEY_NEWS_NEWS_ID,
            KEY_NEWS_NEWS_TEXT,
            KEY_NEWS_NEWS_TIME,
            KEY_NEWS_NEWS_LIKES,
            KEY_NEWS_NEWS_DISLIKES,
            KEY_NEWS_NEWS_COMMENTS,
            KEY_NEWS_NEWS_VIEWS,
            KEY_NEWS_NEWS_TRANSACTIONS,
            KEY_NEWS_NEWS_SHARES,
            KEY_NEWS_NEWS_MAKER_ID,
            KEY_NEWS_NEWS_MAKER_POTTNAME,
            KEY_NEWS_NEWS_MAKER_FULLNAME,
            KEY_NEWS_NEWS_MAKER_POTTPIC,
            KEY_NEWS_NEWS_MAKER_REACTION_STATUS,
            KEY_NEWS_NEWS_ADDED_ITEM_ID,
            KEY_NEWS_NEWS_ADDED_ITEM_PRICE,
            KEY_NEWS_NEWS_ADDED_ITEM_ICON,
            KEY_NEWS_NEWS_ADDED_ITEM_QUANTITY,
            KEY_NEWS_NEWS_TYPE,
            KEY_NEWS_NEWS_MAKER_ACCOUNT_TYPE,
            KEY_NEWS_NEWS_MAKER_VERIFIED_STATUS,
            KEY_NEWS_NEWS_IMAGES_LINKS_SEPARATED_BY_SPACES,
            KEY_NEWS_NEWS_IMAGES_COUNT,
            KEY_NEWS_NEWS_TEXT_READ_MORE_STATUS,
            KEY_NEWS_NEWS_ADDED_ITEM_TYPE,
            KEY_NEWS_NEWS_ADDED_ITEM_STATUS,
            KEY_NEWS_NEWS_VIDEOS_LINKS_SEPARATED_BY_SPACES,
            KEY_NEWS_NEWS_VIDEOS_COVERARTS_LINKS_SEPARATED_BY_SPACES,
            KEY_NEWS_NEWS_VIDEOS_COUNT,
            KEY_NEWS_NEWS_URL,
            KEY_NEWS_NEWS_URL_TITLE,
            KEY_NEWS_NEWS_ITEM_NAME,
            KEY_NEWS_NEWS_ITEM_LOCATION,
            KEY_NEWS_NEWS_ITEM_VERIFIED_STATUS,
            KEY_NEWS_ADVERT_ICON,
            KEY_NEWS_ADVERT_TEXT_TITLE,
            KEY_NEWS_ADVERT_TEXT_TITLE2,
            KEY_NEWS_ADVERT_BUTTON_TEXT,
            KEY_NEWS_ADVERT_LINK,
            KEY_NEWS_REPOST_MAKER_POTTNAME,
            KEY_NEWS_REPOST_TEXT,
            KEY_NEWS_REPOST_ICON,
            KEY_NEWS_REPOST_VIEWER_REACTION,
            KEY_NEWS_REPOST_ADDITEM_PRICE,
            KEY_NEWS_BACKGROUND_COLOR,
            KEY_NEWS_VIEWS_REPORT_OR_PUCHASES_SHOWN_STATUS,
            KEY_NEWS_NEWS_SKU,
            KEY_NEWS_REPOSTED_ADDED_ITEM_ID,
            KEY_NEWS_REPOSTED_ID,
            KEY_NEWS_NEWS_ITEM_PARENT_ID,
            KEY_NEWS_REPOST_ITEM_PARENT_ID,
            KEY_NEWS_REPOST_ITEM_NAME,
            KEY_NEWS_REAL_ITEM_NAME,
            KEY_NEWS_REPOST_ITEM_QUANTITY
    };

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "NEWS_TYPE_1";
    public static final String DATABASE_TABLE = "NEWS_TYPE_1_TABLE";

    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, "
                    + KEY_NEWS_NEWS_ID  + " text not null unique, "
                    + KEY_NEWS_NEWS_TEXT + " text, "
                    + KEY_NEWS_NEWS_TIME + " string, "
                    + KEY_NEWS_NEWS_LIKES + " string, "
                    + KEY_NEWS_NEWS_DISLIKES + " string, "
                    + KEY_NEWS_NEWS_COMMENTS + " string, "
                    + KEY_NEWS_NEWS_VIEWS + " string, "
                    + KEY_NEWS_NEWS_TRANSACTIONS + " string, "
                    + KEY_NEWS_NEWS_SHARES + " string, "
                    + KEY_NEWS_NEWS_MAKER_ID + " text not null, "
                    + KEY_NEWS_NEWS_MAKER_POTTNAME  + " text not null, "
                    + KEY_NEWS_NEWS_MAKER_FULLNAME + " text, "
                    + KEY_NEWS_NEWS_MAKER_POTTPIC + " text, "
                    + KEY_NEWS_NEWS_MAKER_REACTION_STATUS + " int, "
                    + KEY_NEWS_NEWS_ADDED_ITEM_ID  + " text, "
                    + KEY_NEWS_NEWS_ADDED_ITEM_PRICE + " text, "
                    + KEY_NEWS_NEWS_ADDED_ITEM_ICON + " text, "
                    + KEY_NEWS_NEWS_ADDED_ITEM_QUANTITY + " string, "
                    + KEY_NEWS_NEWS_TYPE + " int not null, "
                    + KEY_NEWS_NEWS_MAKER_ACCOUNT_TYPE + " int not null, "
                    + KEY_NEWS_NEWS_MAKER_VERIFIED_STATUS + " int not null, "
                    + KEY_NEWS_NEWS_IMAGES_LINKS_SEPARATED_BY_SPACES + " text, "
                    + KEY_NEWS_NEWS_IMAGES_COUNT + " string, "
                    + KEY_NEWS_NEWS_TEXT_READ_MORE_STATUS + " int not null, "
                    + KEY_NEWS_NEWS_ADDED_ITEM_TYPE + " int, "
                    + KEY_NEWS_NEWS_ADDED_ITEM_STATUS + " int not null, "
                    + KEY_NEWS_NEWS_VIDEOS_LINKS_SEPARATED_BY_SPACES + " text, "
                    + KEY_NEWS_NEWS_VIDEOS_COVERARTS_LINKS_SEPARATED_BY_SPACES + " text, "
                    + KEY_NEWS_NEWS_VIDEOS_COUNT + " string, "
                    + KEY_NEWS_NEWS_URL + " text, "
                    + KEY_NEWS_NEWS_URL_TITLE + " text, "
                    + KEY_NEWS_NEWS_ITEM_NAME + " text, "
                    + KEY_NEWS_NEWS_ITEM_LOCATION + " text, "
                    + KEY_NEWS_NEWS_ITEM_VERIFIED_STATUS + " int, "
                    + KEY_NEWS_ADVERT_ICON + " text, "
                    + KEY_NEWS_ADVERT_TEXT_TITLE + " text, "
                    + KEY_NEWS_ADVERT_TEXT_TITLE2 + " text, "
                    + KEY_NEWS_ADVERT_BUTTON_TEXT + " text, "
                    + KEY_NEWS_ADVERT_LINK + " text, "
                    + KEY_NEWS_REPOST_MAKER_POTTNAME + " text, "
                    + KEY_NEWS_REPOST_TEXT + " text, "
                    + KEY_NEWS_REPOST_ICON + " text, "
                    + KEY_NEWS_REPOST_VIEWER_REACTION + " int, "
                    + KEY_NEWS_REPOST_ADDITEM_PRICE + " text, "
                    + KEY_NEWS_BACKGROUND_COLOR + " int, "
                    + KEY_NEWS_VIEWS_REPORT_OR_PUCHASES_SHOWN_STATUS + " int, "
                    + KEY_NEWS_NEWS_SKU + " int not null unique, "
                    + KEY_NEWS_REPOSTED_ADDED_ITEM_ID + " text, "
                    + KEY_NEWS_REPOSTED_ID + " text , "
                    + KEY_NEWS_NEWS_ITEM_PARENT_ID + " text , "
                    + KEY_NEWS_REPOST_ITEM_PARENT_ID + " text , "
                    + KEY_NEWS_REPOST_ITEM_NAME + " text , "
                    + KEY_NEWS_REAL_ITEM_NAME + " text, "
                    + KEY_NEWS_REPOST_ITEM_QUANTITY + " text "
                    + ");";



    // Context of application who uses us.
    private final Context context;

    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public Vertical_News_Type_DatabaseAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public Vertical_News_Type_DatabaseAdapter openDatabase() {
        if(db != null){
            if(db.isOpen()){
                db.close();
            }
        }
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    public Boolean isVertNewsDbOpen(){
        return db.isOpen();
    }

    // Close the database connection.
    public void closeNews_Type_1_Database() {
        myDBHelper.close();
    }

    // Add a new set of values to the database.
    public long insertRow(
            String newsId
            , String newsText
            , String newsTime
            , String newsLikes
            , String newsDislikes
            , String newsComments
            , String newsViews
            , String newsTransactions
            , String newsShares
            , String newsMakerId
            , String newsMakerPottName
            , String newsMakerFullName
            , String newsMakerPottPic
            , String newMakerReactionStatus
            , String newsAddedItemId
            , String newsAddedItemPrice
            , String newsAddedItemIcon
            , String newsAddedItemQuantity
            , int newsType
            , int newsMakerAccountType
            , int newsVerifiedStatus
            , String newsImagesLinksSeparatedBySpaces
            , String newsImagesCount
            , int newsTextReadMoreStatus
            , int newsAddedItemType
            , int newsAddedItemStatus
            , String newsVideosLinksSeparatedBySpaces
            , String newsVideosCoverArtsLinksSeparatedBySpaces
            , String newsVideosCount
            , String newsUrl
            , String newsUrlTitle
            , String newsItemName
            , String newsItemLocation
            , int newsAddedItemVerifiedStatus
            , String advertIcon
            , String advertTitle
            , String advertTitle2
            , String advertButtonText
            , String advertLink
            , String newsRepostMakerPottName
            , String newsRepostText
            , String newsRepostAddedItemIcon
            , int newsReportViewReactionStatus
            , String newsRepostAddedItemPrice
            , int newsBackground
            , int newsViewsRepostOrPurchasesShownStatus
            , int newsSku
            , String newsRepostAddedItemId
            , String newsRepostNewsID
            , String newsAddedItemParentID
            , String newsRepostAddedItemParentID
            , String newsRepostItemName
            , String newsRealItemName
            , String newsRepostItemQuantity
    ) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NEWS_NEWS_ID, newsId);
        initialValues.put(KEY_NEWS_NEWS_TEXT, newsText);
        initialValues.put(KEY_NEWS_NEWS_TIME, newsTime);
        initialValues.put(KEY_NEWS_NEWS_LIKES, newsLikes);
        initialValues.put(KEY_NEWS_NEWS_DISLIKES, newsDislikes);
        initialValues.put(KEY_NEWS_NEWS_COMMENTS, newsComments);
        initialValues.put(KEY_NEWS_NEWS_VIEWS, newsViews);
        initialValues.put(KEY_NEWS_NEWS_TRANSACTIONS, newsTransactions);
        initialValues.put(KEY_NEWS_NEWS_SHARES, newsShares);
        initialValues.put(KEY_NEWS_NEWS_MAKER_ID, newsMakerId);
        initialValues.put(KEY_NEWS_NEWS_MAKER_POTTNAME, newsMakerPottName);
        initialValues.put(KEY_NEWS_NEWS_MAKER_FULLNAME, newsMakerFullName);
        initialValues.put(KEY_NEWS_NEWS_MAKER_POTTPIC, newsMakerPottPic);
        initialValues.put(KEY_NEWS_NEWS_MAKER_REACTION_STATUS, newMakerReactionStatus);
        initialValues.put(KEY_NEWS_NEWS_ADDED_ITEM_ID, newsAddedItemId);
        initialValues.put(KEY_NEWS_NEWS_ADDED_ITEM_PRICE, newsAddedItemPrice);
        initialValues.put(KEY_NEWS_NEWS_ADDED_ITEM_ICON, newsAddedItemIcon);
        initialValues.put(KEY_NEWS_NEWS_ADDED_ITEM_QUANTITY, newsAddedItemQuantity);
        initialValues.put(KEY_NEWS_NEWS_TYPE, newsType);
        initialValues.put(KEY_NEWS_NEWS_MAKER_ACCOUNT_TYPE, newsMakerAccountType);
        initialValues.put(KEY_NEWS_NEWS_MAKER_VERIFIED_STATUS, newsVerifiedStatus);
        initialValues.put(KEY_NEWS_NEWS_IMAGES_LINKS_SEPARATED_BY_SPACES, newsImagesLinksSeparatedBySpaces);
        initialValues.put(KEY_NEWS_NEWS_IMAGES_COUNT, newsImagesCount);
        initialValues.put(KEY_NEWS_NEWS_TEXT_READ_MORE_STATUS, newsTextReadMoreStatus);
        initialValues.put(KEY_NEWS_NEWS_ADDED_ITEM_TYPE, newsAddedItemType);
        initialValues.put(KEY_NEWS_NEWS_ADDED_ITEM_STATUS, newsAddedItemStatus);
        initialValues.put(KEY_NEWS_NEWS_VIDEOS_LINKS_SEPARATED_BY_SPACES, newsVideosLinksSeparatedBySpaces);
        initialValues.put(KEY_NEWS_NEWS_VIDEOS_COVERARTS_LINKS_SEPARATED_BY_SPACES, newsVideosCoverArtsLinksSeparatedBySpaces);
        initialValues.put(KEY_NEWS_NEWS_VIDEOS_COUNT, newsVideosCount);
        initialValues.put(KEY_NEWS_NEWS_URL, newsUrl);
        initialValues.put(KEY_NEWS_NEWS_URL_TITLE, newsUrlTitle);
        initialValues.put(KEY_NEWS_NEWS_ITEM_NAME, newsItemName);
        initialValues.put(KEY_NEWS_NEWS_ITEM_LOCATION, newsItemLocation);
        initialValues.put(KEY_NEWS_NEWS_ITEM_VERIFIED_STATUS, newsAddedItemVerifiedStatus);
        initialValues.put(KEY_NEWS_ADVERT_ICON, advertIcon);
        initialValues.put(KEY_NEWS_ADVERT_TEXT_TITLE, advertTitle);
        initialValues.put(KEY_NEWS_ADVERT_TEXT_TITLE2, advertTitle2);
        initialValues.put(KEY_NEWS_ADVERT_BUTTON_TEXT, advertButtonText);
        initialValues.put(KEY_NEWS_ADVERT_LINK, advertLink);
        initialValues.put(KEY_NEWS_REPOST_MAKER_POTTNAME, newsRepostMakerPottName);
        initialValues.put(KEY_NEWS_REPOST_TEXT, newsRepostText);
        initialValues.put(KEY_NEWS_REPOST_ICON, newsRepostAddedItemIcon);
        initialValues.put(KEY_NEWS_REPOST_VIEWER_REACTION, newsReportViewReactionStatus);
        initialValues.put(KEY_NEWS_REPOST_ADDITEM_PRICE, newsRepostAddedItemPrice);
        initialValues.put(KEY_NEWS_BACKGROUND_COLOR, newsBackground);
        initialValues.put(KEY_NEWS_VIEWS_REPORT_OR_PUCHASES_SHOWN_STATUS, newsViewsRepostOrPurchasesShownStatus);
        initialValues.put(KEY_NEWS_NEWS_SKU, newsSku);
        initialValues.put(KEY_NEWS_REPOSTED_ADDED_ITEM_ID, newsRepostAddedItemId);
        initialValues.put(KEY_NEWS_REPOSTED_ID, newsRepostNewsID);
        initialValues.put(KEY_NEWS_NEWS_ITEM_PARENT_ID, newsAddedItemParentID);
        initialValues.put(KEY_NEWS_REPOST_ITEM_PARENT_ID, newsRepostAddedItemParentID);
        initialValues.put(KEY_NEWS_REPOST_ITEM_NAME, newsRepostItemName);
        initialValues.put(KEY_NEWS_REAL_ITEM_NAME, newsRealItemName);
        initialValues.put(KEY_NEWS_REPOST_ITEM_QUANTITY, newsRepostItemQuantity);

        // Insert it into the database.
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getSpecificRows(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getLastRow() {
        String order_by = KEY_ROWID + " DESC";
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS, null, null, null, null, order_by, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getFirstRow() {
        String order_by = KEY_ROWID + " ASC";
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS, null, null, null, null, order_by, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, int columnValueType, String columnName, String columnNewValueString, int columnNewValueInt ) {
        String where = KEY_ROWID + "=" + rowId;

        // Create row's data:
        ContentValues newValues = new ContentValues();

        //IF COLUMN TAKES INTEGERS OR STRINGS
        if(columnValueType == 1){
            newValues.put(columnName, columnNewValueInt);
        } else {
            newValues.put(columnName, columnNewValueString);
        }

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }


    /////////////////////////////////////////////////////////////////////
    //	Private Helper Classes:
    /////////////////////////////////////////////////////////////////////

    /**
     * Private class which handles database creation and upgrading.
     * Used to handle low-level database access.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

            // Recreate new database:
            onCreate(_db);
        }
    }

}
