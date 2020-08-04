package com.fishpott.fishpott5.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class News_Type_28_DatabaseAdapter {

	private static final String TAG = "News_Type_1_DBAdapter";

	// DB Fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NEWS_STORY_TYPE = "NEWS_STORY_TYPE";
	public static final String KEY_NEWS_STORY_ID = "NEWS_STORY_ID";
	public static final String KEY_NEWS_MAKER_ID = "NEWS_MAKER_ID";
	public static final String KEY_NEWS_MAKER_POTTNAME = "NEWS_MAKER_POTTNAME";
	public static final String KEY_NEWS_MAKER_PROFILE_PICTURE = "NEWS_MAKER_PROFILE_PICTURE";
	public static final String KEY_NEWS_STORY_IMAGE = "NEWS_STORY_IMAGE";
	public static final String KEY_NEWS_STORY_VIDEO = "NEWS_STORY_VIDEO";
	public static final String KEY_NEWS_STORY_ITEM_PRICE = "NEWS_STORY_ITEM_PRICE";
	public static final String KEY_NEWS_STORY_ITEM_PARENT_ID = "NEWS_STORY_ITEM_PARENT_ID";
	public static final String KEY_NEWS_STORY_ITEM_NAME = "NEWS_STORY_ITEM_NAME";
	public static final String KEY_NEWS_STORY_ITEM_QUANTITY = "NEWS_STORY_ITEM_QUANTITY";
	public static final String KEY_NEWS_STORY_ITEM_SHARES_ID = "NEWS_STORY_ITEM_SHARES_ID";

	public static final int COL_ROWID = 0;
	public static final int COL_NEWS_STORY_TYPE = 1;
	public static final int COL_NEWS_STORY_ID = 2;
	public static final int COL_NEWS_MAKER_ID = 3;
	public static final int COL_NEWS_MAKER_POTTNAME = 4;
	public static final int COL_NEWS_MAKER_PROFILE_PICTURE = 5;
	public static final int COL_NEWS_STORY_IMAGE = 6;
	public static final int COL_NEWS_STORY_VIDEO = 7;
	public static final int COL_NEWS_STORY_ITEM_PRICE = 8;
	public static final int COL_NEWS_STORY_ITEM_PARENT_ID = 9;
	public static final int COL_NEWS_STORY_ITEM_NAME = 10;
	public static final int COL_NEWS_STORY_ITEM_QUANTITY = 11;
	public static final int COL_NEWS_STORY_ITEM_SHARES_ID = 12;


	public static final String[] ALL_KEYS = new String[] {
			KEY_ROWID,
			KEY_NEWS_STORY_TYPE,
			KEY_NEWS_STORY_ID,
			KEY_NEWS_MAKER_ID,
			KEY_NEWS_MAKER_POTTNAME,
			KEY_NEWS_MAKER_PROFILE_PICTURE,
			KEY_NEWS_STORY_IMAGE,
			KEY_NEWS_STORY_VIDEO,
			KEY_NEWS_STORY_ITEM_PRICE,
			KEY_NEWS_STORY_ITEM_PARENT_ID,
			KEY_NEWS_STORY_ITEM_NAME,
			KEY_NEWS_STORY_ITEM_QUANTITY,
			KEY_NEWS_STORY_ITEM_SHARES_ID
	};


	// DB info: it's name, and the table we are using (just one).
	public static final String DATABASE_NAME = "NEWS_STORIES";
	public static final String DATABASE_TABLE = "ALL_STORIES";

	// Track DB version if a new version of your app changes the format.
	public static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE_SQL =
					"create table " + DATABASE_TABLE
					+ " (" + KEY_ROWID + " integer primary key autoincrement, "
					+ KEY_NEWS_STORY_TYPE + " integer not null, "
					+ KEY_NEWS_STORY_ID + " text not null unique, "
					+ KEY_NEWS_MAKER_ID + " text not null, "
					+ KEY_NEWS_MAKER_POTTNAME + " text not null, "
					+ KEY_NEWS_MAKER_PROFILE_PICTURE + " text, "
					+ KEY_NEWS_STORY_IMAGE + " text, "
					+ KEY_NEWS_STORY_VIDEO + " text, "
					+ KEY_NEWS_STORY_ITEM_PRICE + " string, "
							+ KEY_NEWS_STORY_ITEM_PARENT_ID + " text not null, "
							+ KEY_NEWS_STORY_ITEM_NAME + " text not null, "
							+ KEY_NEWS_STORY_ITEM_QUANTITY + " text not null, "
							+ KEY_NEWS_STORY_ITEM_SHARES_ID + " text "
					+ ");";


	// Context of application who uses us.
	private final Context context;
	
	private DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	/////////////////////////////////////////////////////////////////////
	//	Public methods:
	/////////////////////////////////////////////////////////////////////
	
	public News_Type_28_DatabaseAdapter(Context ctx) {
		this.context = ctx;
			myDBHelper = new DatabaseHelper(context);
	}
	
	// Open the database connection.
	public News_Type_28_DatabaseAdapter openDatabase() {
		db = myDBHelper.getWritableDatabase();
		return this;
	}
	
	// Close the database connection.
	public void closeNewsStoriesDatabase() {
		myDBHelper.close();
	}


	// Add a new set of values to the database.
	public long insertRow(int news_story_type
			, String news_story_id
			, String news_maker_id
			, String news_maker_pottname
			, String news_maker_profile_picture
			, String news_story_image
			, String news_story_video
			, String news_story_price
			, String item_parent_shares_id
			, String news_item_name
			, String number_available
			, String shares4sale_id) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NEWS_STORY_TYPE, news_story_type);
		initialValues.put(KEY_NEWS_STORY_ID, news_story_id);
		initialValues.put(KEY_NEWS_MAKER_ID, news_maker_id);
		initialValues.put(KEY_NEWS_MAKER_POTTNAME, news_maker_pottname);
		initialValues.put(KEY_NEWS_MAKER_PROFILE_PICTURE, news_maker_profile_picture);
		initialValues.put(KEY_NEWS_STORY_IMAGE, news_story_image);
		initialValues.put(KEY_NEWS_STORY_VIDEO, news_story_video);
		initialValues.put(KEY_NEWS_STORY_ITEM_PRICE, news_story_price);
		initialValues.put(KEY_NEWS_STORY_ITEM_PARENT_ID, item_parent_shares_id);
		initialValues.put(KEY_NEWS_STORY_ITEM_NAME, news_item_name);
		initialValues.put(KEY_NEWS_STORY_ITEM_QUANTITY, number_available);
		initialValues.put(KEY_NEWS_STORY_ITEM_SHARES_ID, shares4sale_id);

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
