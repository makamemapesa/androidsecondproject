package com.example.mysecondproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private DBHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public boolean registerUser(String fname, String lname, String email, String pass, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("first_name", fname);
        cv.put("last_name", lname);
        cv.put("email", email);
        cv.put("password", pass);
        cv.put("role", role);
        cv.put("status", "FREE"); // Default status
        
        long result = db.insert("users", null, cv);
        db.close();
        return result != -1;
    }

    public Cursor loginUser(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", 
                new String[]{email, password});
    }

    public List<String> fetchByRoleAndStatus(String role, String status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT email FROM users WHERE role = ? AND status = ?", 
                new String[]{role, status});
        
        List<String> out = new ArrayList<>();
        while (c.moveToNext()) {
            out.add(c.getString(0));
        }
        c.close();
        db.close();
        return out;
    }

    public void updateDriverStatus(String email, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        db.update("users", cv, "email = ?", new String[]{email});
        db.close();
    }

    public String getDriverStatus(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT status FROM users WHERE email = ?", new String[]{email});
        
        String status = "FREE"; // default
        if (c.moveToFirst()) {
            status = c.getString(0);
            if (status == null) status = "FREE";
        }
        c.close();
        db.close();
        return status;
    }

    public String getUserFullName(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT first_name, last_name FROM users WHERE email = ?", 
                new String[]{email});
        
        String fullName = email; // fallback
        if (c.moveToFirst()) {
            String firstName = c.getString(0);
            String lastName = c.getString(1);
            fullName = firstName + " " + lastName;
        }
        c.close();
        db.close();
        return fullName;
    }

    public List<String> getAllFreeDrivers() {
        return fetchByRoleAndStatus("DRIVER", "FREE");
    }
}