package com.example.mysecondproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class ReporterActivity extends AppCompatActivity {
    private Button panicButton;
    private TextView panicResult;
    private EmergencyDAO emergencyDAO;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporter);

        panicButton = findViewById(R.id.panicButton);
        panicResult = findViewById(R.id.panicResult);
        emergencyDAO = new EmergencyDAO(this);

        // Get email from intent
        userEmail = getIntent().getStringExtra("email");
        if (userEmail == null) {
            userEmail = "unknown@example.com"; // fallback
        }

        // Set click listener
        panicButton.setOnClickListener(v -> reportEmergency());
    }

    private void reportEmergency() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = sdf.format(new Date());

        double baseLat = -6.1659;
        double baseLng = 39.2026;
        Random rand = new Random();
        double lat = baseLat + (rand.nextDouble() - 0.5) * 0.1;
        double lng = baseLng + (rand.nextDouble() - 0.5) * 0.1;

        String fakeAddress = String.format(Locale.getDefault(),
                "Fake Location #%d, Zanzibar", rand.nextInt(1000));

        EmergencyDto emergencyDto = new EmergencyDto();
        emergencyDto.setDescription("Panic reported");
        emergencyDto.setLocationDescription(fakeAddress);
        emergencyDto.setLatitude(lat);
        emergencyDto.setLongitude(lng);
        emergencyDto.setStatus("NEW");
        emergencyDto.setReportedAt(formattedDate);

        // Save to local database
        emergencyDAO.create(userEmail, lat, lng, fakeAddress);

        // Send to server
        sendEmergencyToServer(emergencyDto);
    }

    private void sendEmergencyToServer(EmergencyDto emergencyDto) {
        String url = "http://192.168.1.108:8080/emergencies";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("description", emergencyDto.getDescription());
            jsonBody.put("status", emergencyDto.getStatus());
            jsonBody.put("latitude", emergencyDto.getLatitude());
            jsonBody.put("longitude", emergencyDto.getLongitude());
            jsonBody.put("locationDescription", emergencyDto.getLocationDescription());
            jsonBody.put("reportedAt", emergencyDto.getReportedAt());
            jsonBody.put("respondedAt", JSONObject.NULL);
            jsonBody.put("completedAt", JSONObject.NULL);
            jsonBody.put("reporterId", 2); // Replace with actual reporter ID
            jsonBody.put("driverId", 2);   // Replace with actual driver ID

            Log.d("EmergencyJSON", jsonBody.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Show "Sending..." while request is in progress
        panicButton.setText("Sending...");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Log.d("EmergencyDAO", "Emergency sent successfully: " + response);
                    Toast.makeText(ReporterActivity.this, "🚨 Emergency sent successfully!", Toast.LENGTH_SHORT).show();
                    panicButton.setText("Panic!");
                },
                error -> {
                    Log.e("EmergencyDAO", "Error sending emergency: " + error.toString());
                    Toast.makeText(ReporterActivity.this, "❌ Failed to send emergency", Toast.LENGTH_SHORT).show();
                    panicButton.setText("Panic!");
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
