package com.fishpott.fishpott5.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by zatana on 8/1/19.
 */

public class MyShares_DatabaseAdapter {

    private static final String TAG = "My_Shares_DBAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final String KEY_SHARE_NAME = "SHARE_NAME ";
    public static final String KEY_SHARE_ID = "SHARE_ID";
    public static final String KEY_SHARE_AVAILABLE_QUANTITY = "SHARE_AVAILABLE_QUANTITY";
    public static final String KEY_SHARE_COST_PRICE_PER_SHARE = "SHARE_COST_PRICE_PER_SHARE";
    public static final String KEY_SHARE_MAX_PRICE_PER_SHARE = "SHARE_MAX_PRICE_PER_SHARE";
    public static final String KEY_SHARE_DIVIDEND_PER_SHARE = "SHARE_DIVIDEND_PER_SHARE";
    public static final String KEY_SHARE_PARENT_ID = "SHARE_PARENT_ID";

    public static final int COL_ROWID = 0;
    public static final int COL_SHARE_NAME= 1;
    public static final int COL_SHARE_ID = 2;
    public static final int COL_SHARE_AVAILABLE_QUANTITY = 3;
    public static final int COL_SHARE_COST_PRICE_PER_SHARE = 4;
    public static final int COL_SHARE_MAX_PRICE_PER_SHARE = 5;
    public static final int COL_SHARE_DIVIDEND_PER_SHARE = 6;
    public static final int COL_SHARE_PARENT_ID = 7;

    public static final String[] ALL_KEYS = new String[] {
            KEY_ROWID,
            KEY_SHARE_NAME,
            KEY_SHARE_ID,
            KEY_SHARE_AVAILABLE_QUANTITY,
            KEY_SHARE_COST_PRICE_PER_SHARE,
            KEY_SHARE_MAX_PRICE_PER_SHARE,
            KEY_SHARE_DIVIDEND_PER_SHARE,
            KEY_SHARE_PARENT_ID
    };

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "MY_SHARES";
    public static final String DATABASE_TABLE = "ALL_MY_SHARES";

    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, "
                    + KEY_SHARE_NAME + " text not null, "
                    + KEY_SHARE_ID + " text not null unique, "
                    + KEY_SHARE_AVAILABLE_QUANTITY + " text not null, "
                    + KEY_SHARE_COST_PRICE_PER_SHARE + " text not null, "
                    + KEY_SHARE_MAX_PRICE_PER_SHARE + " text not null, "
                    + KEY_SHARE_DIVIDEND_PER_SHARE + " text not null, "
                    + KEY_SHARE_PARENT_ID + " text not null "
                    + ");";

    // Context of application who uses us.
    private final Context context;

    private MyShares_DatabaseAdapter.DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public MyShares_DatabaseAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new MyShares_DatabaseAdapter.DatabaseHelper(context);
    }

    // Open the database connection.
    public MyShares_DatabaseAdapter openDatabase() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void closeNewsStoriesDatabase() {
        myDBHelper.close();
    }


    // Add a new set of values to the database.
    public long insertRow(
              String share_name
            , String share_id
            , String share_available_quantity
            , String share_cost_price_per_share
            , String share_max_price_per_share
            , String share_dividend_price_per_share
            , String share_parent_id) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_SHARE_NAME, share_name);
        initialValues.put(KEY_SHARE_ID, share_id);
        initialValues.put(KEY_SHARE_AVAILABLE_QUANTITY, share_available_quantity);
        initialValues.put(KEY_SHARE_COST_PRICE_PER_SHARE, share_cost_price_per_share);
        initialValues.put(KEY_SHARE_MAX_PRICE_PER_SHARE, share_max_price_per_share);
        initialValues.put(KEY_SHARE_DIVIDEND_PER_SHARE, share_dividend_price_per_share);
        initialValues.put(KEY_SHARE_PARENT_ID, share_parent_id);

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
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                null, null, null, null, order_by, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getFirstRow() {
        String order_by = KEY_ROWID + " ASC";
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                null, null, null, null, order_by, null);
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

	/*
	// Change an existing row to be equal to new data.
	public boolean updateRow(long rowId,
							 int news_story_type
							, String news_story_id
							, String news_maker_id
							, String news_maker_pottname
							, String news_maker_profile_picture
							, String news_story_image
							, String news_story_video
							, String news_story_price) {
		String where = KEY_ROWID + "=" + rowId;

		// Create row's data:
		ContentValues newValues = new ContentValues();

		newValues.put(KEY_NEWS_STORY_TYPE, news_story_type);
		newValues.put(KEY_NEWS_STORY_ID, news_story_id);
		newValues.put(KEY_NEWS_MAKER_ID, news_maker_id);
		newValues.put(KEY_NEWS_MAKER_POTTNAME, news_maker_pottname);
		newValues.put(KEY_NEWS_MAKER_PROFILE_PICTURE, news_maker_profile_picture);
		newValues.put(KEY_NEWS_STORY_IMAGE, news_story_image);
		newValues.put(KEY_NEWS_STORY_VIDEO, news_story_video);
		newValues.put(KEY_NEWS_STORY_ITEM_PRICE, news_story_price);

		// Insert it into the database.
		return db.update(DATABASE_TABLE, newValues, where, null) != 0;
	}
	*/

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
