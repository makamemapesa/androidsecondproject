package com.example.mysecondproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "fireapp.db";
    public static final int DB_VERSION = 2; // Increased version

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table with status column
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT, " +
                "last_name TEXT, " +
                "email TEXT UNIQUE, " +
                "password TEXT, " +
                "role TEXT, " +
                "status TEXT DEFAULT 'FREE', " +  // Add status column
                "assigned_emergency_id INTEGER DEFAULT NULL)");

        // Create emergencies table
        db.execSQL("CREATE TABLE emergencies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reporter_email TEXT, " +
                "latitude REAL, " +
                "longitude REAL, " +
                "address TEXT, " +  // Add address field
                "status TEXT DEFAULT 'NEW', " +
                "assigned_driver_email TEXT DEFAULT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add status column to existing users table
            db.execSQL("ALTER TABLE users ADD COLUMN status TEXT DEFAULT 'FREE'");
            db.execSQL("ALTER TABLE emergencies ADD COLUMN address TEXT");
            db.execSQL("ALTER TABLE emergencies ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP");
        }
    }
}