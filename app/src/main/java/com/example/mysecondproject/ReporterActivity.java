package com.example.mysecondproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ReporterActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Button panicButton;
    private TextView panicResult;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporter);

        panicButton = findViewById(R.id.panicButton);
        panicResult = findViewById(R.id.panicResult);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        userEmail = getIntent().getStringExtra("user_email");
        if (userEmail == null) {
            userEmail = "unknown@example.com"; // Fallback
        }

        panicButton.setOnClickListener(v -> {
            if (checkLocationPermissions()) {
                showDescriptionDialog();
            } else {
                requestLocationPermissions();
            }
        });
    }

    private boolean checkLocationPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
               ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showDescriptionDialog();
            } else {
                Toast.makeText(this, "Location permission is required to report an emergency.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDescriptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Describe the Emergency");

        final EditText input = new EditText(this);
        input.setHint("e.g., Fire at the kitchen");
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String description = input.getText().toString();
            getCurrentLocationAndReport(description);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void getCurrentLocationAndReport(String description) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        reportEmergency(location, description);
                    } else {
                        Toast.makeText(this, "Could not get location. Please ensure GPS is enabled.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void reportEmergency(Location location, String description) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = sdf.format(new Date());

        EmergencyDto emergencyDto = new EmergencyDto();
        emergencyDto.setDescription(description);
        emergencyDto.setLocationDescription("Location from GPS");
        emergencyDto.setLatitude(location.getLatitude());
        emergencyDto.setLongitude(location.getLongitude());
        emergencyDto.setStatus("NEW");
        emergencyDto.setReportedAt(formattedDate);

        // Create a UserDto-like structure for the reporter
        JSONObject reporterJson = new JSONObject();
        try {
            reporterJson.put("email", userEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendEmergencyToServer(emergencyDto, reporterJson);
    }

    private void sendEmergencyToServer(EmergencyDto emergencyDto, JSONObject reporterJson) {
        String url = Constants.BASE_URL + "/emergencies/panic";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("description", emergencyDto.getDescription());
            jsonBody.put("status", emergencyDto.getStatus());
            jsonBody.put("latitude", emergencyDto.getLatitude());
            jsonBody.put("longitude", emergencyDto.getLongitude());
            jsonBody.put("locationDescription", emergencyDto.getLocationDescription());
            jsonBody.put("reportedAt", emergencyDto.getReportedAt());
            jsonBody.put("reporter", reporterJson); // Pass the reporter's email

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating emergency report.", Toast.LENGTH_SHORT).show();
            return;
        }

        panicButton.setText("Sending...");
        panicButton.setEnabled(false);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Log.d("EmergencyReport", "Emergency sent successfully: " + response.toString());
                    Toast.makeText(ReporterActivity.this, "Emergency reported successfully!", Toast.LENGTH_LONG).show();
                    panicResult.setText("Emergency reported at " + new Date().toString());
                    panicButton.setText("PANIC");
                    panicButton.setEnabled(true);
                },
                error -> {
                    Log.e("EmergencyReport", "Error sending emergency: " + error.toString());
                    Toast.makeText(ReporterActivity.this, "Failed to report emergency. Please try again.", Toast.LENGTH_LONG).show();
                    panicButton.setText("PANIC");
                    panicButton.setEnabled(true);
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
