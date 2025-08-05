package com.example.mysecondproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserDAO {
    private final DBHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    // Register user locally
    public boolean registerUser(String firstName, String middleName, String lastName,
                                String email, String password, String phone,
                                String role, String address) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("first_name", firstName);
        values.put("middle_name", middleName);
        values.put("last_name", lastName);
        values.put("email", email);
        values.put("password", password);
        values.put("phone", phone);
        values.put("role", role);
        values.put("address", address);
        values.put("status", "FREE");      // Driver status
        values.put("backend_id", -1);      // Default backend ID

        try {
            long id = db.insertOrThrow("users", null, values);
            Log.d("UserDAO", "User registered locally with ID: " + id);
            return true;
        } catch (Exception e) {
            Log.e("UserDAO", "Insert failed for email: " + email + " Error: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // Check if user exists by email
    public boolean userExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM users WHERE email = ?", new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    // Login and get full user record
    public Cursor loginUser(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                new String[]{email, password}
        );
    }

    // Validate login only (true/false)
    public boolean validateLogin(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                new String[]{email, password}
        );
        boolean isValid = cursor.moveToFirst();
        cursor.close();
        db.close();
        return isValid;
    }

    // Update backend ID
    public boolean saveBackendId(String email, long backendId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("backend_id", backendId);
        int rows = db.update("users", values, "email = ?", new String[]{email});
        db.close();
        return rows > 0;
    }

    // Get driver status
    public String getDriverStatus(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT status FROM users WHERE email = ?", new String[]{email});
        String status = "UNKNOWN";
        if (cursor.moveToFirst()) {
            status = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return status;
    }

    // Update driver status
    public boolean updateDriverStatus(String email, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", newStatus);
        int rows = db.update("users", values, "email = ?", new String[]{email});
        db.close();
        return rows > 0;
    }
}
