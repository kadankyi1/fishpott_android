package com.fishpott.fishpott5.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by zatana on 9/11/19.
 */

public class ChatMessages_DatabaseAdapter {

    private static final String TAG = "Contacts_DbAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final String KEY_MESSAGE_ID = "MESSAGE_ID";
    public static final String KEY_CHAT_ID = "CHAT_ID";
    public static final String KEY_SENDER_POTT_NAME = "SENDER_POTT_NAME";
    public static final String KEY_RECEIVER_POTT_NAME = "RECEIVER_POTT_NAME";
    public static final String KEY_MESSAGE_TEXT = "MESSAGE_TEXT";
    public static final String KEY_MESSAGE_IMAGE = "MESSAGE_IMAGE";
    public static final String KEY_MESSAGE_TIME = "MESSAGE_TIME";
    public static final String KEY_MESSAGE_STATUS = "MESSAGE_STATUS";
    public static final String KEY_MESSAGE_ONLINE_SKU = "MESSAGE_ONLINE_SKU";

    public static final int COL_ROWID = 0;
    public static final int COL_MESSAGE_ID = 1;
    public static final int COL_CHAT_ID = 2;
    public static final int COL_SENDER_POTT_NAME = 3;
    public static final int COL_RECEIVER_POTT_NAME = 4;
    public static final int COL_MESSAGE_TEXT = 5;
    public static final int COL_MESSAGE_IMAGE = 6;
    public static final int COL_MESSAGE_TIME = 7;
    public static final int COL_MESSAGE_STATUS = 8;
    public static final int COL_MESSAGE_ONLINE_SKU = 9;

    public static final String[] ALL_KEYS = new String[] {
            KEY_ROWID,
            KEY_MESSAGE_ID,
            KEY_CHAT_ID,
            KEY_SENDER_POTT_NAME,
            KEY_RECEIVER_POTT_NAME,
            KEY_MESSAGE_TEXT,
            KEY_MESSAGE_IMAGE,
            KEY_MESSAGE_TIME,
            KEY_MESSAGE_STATUS,
            KEY_MESSAGE_ONLINE_SKU
    };

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "OLD_CHAT_MESSAGES";
    public static final String DATABASE_TABLE = "ALL_OLD_CHAT_MESSAGES";

    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, "
                    + KEY_MESSAGE_ID + " text not null unique, "
                    + KEY_CHAT_ID + " text not null, "
                    + KEY_SENDER_POTT_NAME + " text not null, "
                    + KEY_RECEIVER_POTT_NAME + " text not null, "
                    + KEY_MESSAGE_TEXT + " text not null, "
                    + KEY_MESSAGE_IMAGE + " text not null, "
                    + KEY_MESSAGE_TIME + " text not null, "
                    + KEY_MESSAGE_STATUS + " integer , "
                    + KEY_MESSAGE_ONLINE_SKU + " integer not null "
                    + ");";

    // Context of application who uses us.
    private final Context context;

    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public ChatMessages_DatabaseAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public ChatMessages_DatabaseAdapter openDatabase() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void closeDatabase() {
        myDBHelper.close();
    }


    // Add a new set of values to the database.
    public long insertRow(
              String message_id
            , String chat_id
            , String sender_pottname
            , String receiver_pottname
            , String message_text
            , String message_image
            , String message_time
            , int message_status
            , int online_sku
    ) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_MESSAGE_ID, message_id);
        initialValues.put(KEY_CHAT_ID, chat_id);
        initialValues.put(KEY_SENDER_POTT_NAME, sender_pottname);
        initialValues.put(KEY_RECEIVER_POTT_NAME, receiver_pottname);
        initialValues.put(KEY_MESSAGE_TEXT, message_text);
        initialValues.put(KEY_MESSAGE_IMAGE, message_image);
        initialValues.put(KEY_MESSAGE_TIME, message_time);
        initialValues.put(KEY_MESSAGE_STATUS, message_status);
        initialValues.put(KEY_MESSAGE_ONLINE_SKU, online_sku);

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

    public Cursor getSpecificRowByRowId(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getSpecificRowsUsingWhereValueAsString(String whereClause, String order_by, Boolean reverseOrder) {
        //String where = whereKey + " = " + searchParameter;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS, whereClause, null, null, null, order_by, null);
        if (c != null) {
            if(reverseOrder){
                c.moveToLast();
            } else {
                c.moveToFirst();
            }
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
