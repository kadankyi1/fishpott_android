package com.fishpott.fishpott5.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by zatana on 7/17/19.
 */

public class Notifications_DatabaseAdapter {


    private static final String TAG = "Notifications_DBAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    public static final String KEY_RELEVANT_ID_1 = "RELEVANT_ID_1";
    public static final String KEY_RELEVANT_ID_2 = "RELEVANT_ID_2";
    public static final String KEY_READ_STATUS = "READ_STATUS";
    public static final String KEY_POTT_PIC = "POTT_PIC";
    public static final String KEY_NOTIFICATION_MESSAGE = "NOTIFICATION_MESSAGE";
    public static final String KEY_NOTIFICATION_DATE = "NOTIFICATION_DATE";
    public static final String KEY_RELEVANT_ID_3 = "RELEVANT_ID_3";

    public static final int COL_ROWID = 0;
    public static final int COL_NOTIFICATION_TYPE = 1;
    public static final int COL_RELEVANT_ID_1 = 2;
    public static final int COL_RELEVANT_ID_2 = 3;
    public static final int COL_READ_STATUS = 4;
    public static final int COL_POTT_PIC = 5;
    public static final int COL_NOTIFICATION_MESSAGE = 6;
    public static final int COL_NOTIFICATION_DATE = 7;
    public static final int COL_RELEVANT_ID_3 = 8;

    public static final String[] ALL_KEYS = new String[] {
            KEY_ROWID,
            KEY_NOTIFICATION_TYPE,
            KEY_RELEVANT_ID_1,
            KEY_RELEVANT_ID_2,
            KEY_READ_STATUS,
            KEY_POTT_PIC,
            KEY_NOTIFICATION_MESSAGE,
            KEY_NOTIFICATION_DATE,
            KEY_RELEVANT_ID_3
    };

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "OLD_NOTIFICATIONS";
    public static final String DATABASE_TABLE = "ALL_OLD_NOTIFICATIONS";

    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, "
                    + KEY_NOTIFICATION_TYPE + " integer not null, "
                    + KEY_RELEVANT_ID_1 + " text, "
                    + KEY_RELEVANT_ID_2 + " text, "
                    + KEY_READ_STATUS + " integer not null, "
                    + KEY_POTT_PIC + " text, "
                    + KEY_NOTIFICATION_MESSAGE + " text, "
                    + KEY_NOTIFICATION_DATE + " text, "
                    + KEY_RELEVANT_ID_3 + " text "
                    + ");";

    // Context of application who uses us.
    private final Context context;

    private Notifications_DatabaseAdapter.DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public Notifications_DatabaseAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new Notifications_DatabaseAdapter.DatabaseHelper(context);
    }

    // Open the database connection.
    public Notifications_DatabaseAdapter openDatabase() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void closeDatabase() {
        myDBHelper.close();
    }


    // Add a new set of values to the database.
    public long insertRow(
            int noti_type
            , String id_1
            , String id_2
            , int read_status
            , String pott_pic
            , String noti_message
            , String noti_date
            , String id_3
    ) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NOTIFICATION_TYPE, noti_type);
        initialValues.put(KEY_RELEVANT_ID_1, id_1);
        initialValues.put(KEY_RELEVANT_ID_2, id_2);
        initialValues.put(KEY_READ_STATUS, read_status);
        initialValues.put(KEY_POTT_PIC, pott_pic);
        initialValues.put(KEY_NOTIFICATION_MESSAGE, noti_message);
        initialValues.put(KEY_NOTIFICATION_DATE, noti_date);
        initialValues.put(KEY_RELEVANT_ID_3, id_3);

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
