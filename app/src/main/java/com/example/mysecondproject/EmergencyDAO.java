package com.example.mysecondproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EmergencyDAO {
    private DBHelper dbHelper;

    public EmergencyDAO(Context ctx) {
        dbHelper = new DBHelper(ctx);
    }

    // Save emergency to SQLite only
    public long create(String reporterEmail, double lat, double lng, String address) {
        ContentValues cv = new ContentValues();
        cv.put("reporter_email", reporterEmail);
        cv.put("latitude", lat);
        cv.put("longitude", lng);
        cv.put("address", address);
        cv.put("status", "NEW");
        cv.putNull("assigned_driver_email");

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = db.insert("emergencies", null, cv);
        db.close();
        return result;
    }

    // Save emergency to SQLite and send to server
    public long createAndSend(Context context, String reporterEmail, double lat, double lng, String address) {
        ContentValues cv = new ContentValues();
        cv.put("reporter_email", reporterEmail);
        cv.put("latitude", lat);
        cv.put("longitude", lng);
        cv.put("address", address);
        cv.put("status", "NEW");
        cv.putNull("assigned_driver_email");

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = db.insert("emergencies", null, cv);
        db.close();

        Emergency emergency = new Emergency(
                (int) result,
                reporterEmail,
                lat,
                lng,
                "NEW",
                null,
                address
        );

        sendEmergencyToServer(context, emergency);

        return result;
    }

    // Fetch all emergencies
    public List<Emergency> fetchAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM emergencies ORDER BY created_at DESC", null);
        List<Emergency> list = new ArrayList<>();

        while (c.moveToNext()) {
            list.add(new Emergency(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("reporter_email")),
                    c.getDouble(c.getColumnIndexOrThrow("latitude")),
                    c.getDouble(c.getColumnIndexOrThrow("longitude")),
                    c.getString(c.getColumnIndexOrThrow("status")),
                    c.getString(c.getColumnIndexOrThrow("assigned_driver_email")),
                    c.getString(c.getColumnIndexOrThrow("address"))
            ));
        }
        c.close();
        db.close();
        return list;
    }

    // Fetch emergencies assigned to a specific driver
    public List<Emergency> fetchByDriverEmail(String driverEmail) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM emergencies WHERE assigned_driver_email = ? ORDER BY created_at DESC",
                new String[]{driverEmail});
        List<Emergency> list = new ArrayList<>();

        while (c.moveToNext()) {
            list.add(new Emergency(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("reporter_email")),
                    c.getDouble(c.getColumnIndexOrThrow("latitude")),
                    c.getDouble(c.getColumnIndexOrThrow("longitude")),
                    c.getString(c.getColumnIndexOrThrow("status")),
                    c.getString(c.getColumnIndexOrThrow("assigned_driver_email")),
                    c.getString(c.getColumnIndexOrThrow("address"))
            ));
        }
        c.close();
        db.close();
        return list;
    }

    // Assign driver to an emergency
    public void assignDriver(int emergencyId, String driverEmail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("assigned_driver_email", driverEmail);
        cv.put("status", "ASSIGNED");
        db.update("emergencies", cv, "id = ?", new String[]{String.valueOf(emergencyId)});
        db.close();
    }

    // Update emergency status
    public void updateStatus(int emergencyId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        db.update("emergencies", cv, "id = ?", new String[]{String.valueOf(emergencyId)});
        db.close();
    }

    // Get single emergency by ID
    public Emergency getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM emergencies WHERE id = ?", new String[]{String.valueOf(id)});

        Emergency emergency = null;
        if (c.moveToFirst()) {
            emergency = new Emergency(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("reporter_email")),
                    c.getDouble(c.getColumnIndexOrThrow("latitude")),
                    c.getDouble(c.getColumnIndexOrThrow("longitude")),
                    c.getString(c.getColumnIndexOrThrow("status")),
                    c.getString(c.getColumnIndexOrThrow("assigned_driver_email")),
                    c.getString(c.getColumnIndexOrThrow("address"))
            );
        }
        c.close();
        db.close();
        return emergency;
    }

    // Send emergency to backend API
    public void sendEmergencyToServer(Context context, Emergency emergency) {
        String url = "http://192.168.0.150:8080/emergencies/ema";
        // <-- this should match your @PostMapping

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("latitude", emergency.lat);
            jsonBody.put("longitude", emergency.lng);
            jsonBody.put("locationDescription", emergency.address); // match field
            jsonBody.put("status", emergency.status);
//            jsonBody.put("driverId", JSONObject.NULL); // or provide real ID if known
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> Log.d("EmergencyDAO", "Emergency sent successfully: " + response),
                error -> Log.e("EmergencyDAO", "Error sending emergency: " + error.toString())
        );

        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }

}
