package com.example.mysecondproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "rescue_app.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT, " +
                "middle_name TEXT, " +
                "last_name TEXT, " +
                "email TEXT UNIQUE, " +
                "password TEXT, " +
                "phone TEXT, " +
                "role TEXT, " +
                "address TEXT, " +
                "status TEXT DEFAULT 'FREE', " + // driver status
                "backend_id INTEGER DEFAULT -1" +
                ");";

        String createEmergenciesTable = "CREATE TABLE emergencies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reporter_email TEXT, " +
                "driver_email TEXT, " +
                "address TEXT, " +
                "lat REAL, " +
                "lng REAL, " +
                "status TEXT" +
                ");";

        db.execSQL(createUsersTable);
        db.execSQL(createEmergenciesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables and recreate (simple strategy for now)
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS emergencies");
        onCreate(db);
    }
}
