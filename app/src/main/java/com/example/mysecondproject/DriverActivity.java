package com.example.mysecondproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DriverActivity extends AppCompatActivity {

    private RecyclerView rvJobs;
    private TextView statusText;
    private JobAdapter adapter;
    private List<EmergencyDto> emergencyList = new ArrayList<>();
    private long driverId;
    private String driverEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        rvJobs = findViewById(R.id.rvJobs);
        statusText = findViewById(R.id.statusText);

        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JobAdapter(emergencyList);
        rvJobs.setAdapter(adapter);

        driverId = getIntent().getLongExtra("user_id", -1);
        driverEmail = getIntent().getStringExtra("user_email");

        if (driverId == -1) {
            Toast.makeText(this, "Error: Driver ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadJobs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJobs(); // Refresh when returning to activity
    }

    private void loadJobs() {
        String url = Constants.BASE_URL + "/emergencies/driver/" + driverId + "/assigned";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    emergencyList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject emergencyJson = response.getJSONObject(i);
                            EmergencyDto emergency = new EmergencyDto();
                            emergency.setId(emergencyJson.getLong("id"));
                            emergency.setDescription(emergencyJson.getString("description"));
                            emergency.setStatus(emergencyJson.getString("status"));
                            emergency.setLatitude(emergencyJson.getDouble("latitude"));
                            emergency.setLongitude(emergencyJson.getDouble("longitude"));
                            emergency.setLocationDescription(emergencyJson.getString("locationDescription"));
                            emergency.setReportedAt(emergencyJson.getString("reportedAt"));
                            emergencyList.add(emergency);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DriverActivity.this, "Error parsing emergency data.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("DriverActivity", "Error fetching emergencies: " + error.toString());
                    Toast.makeText(DriverActivity.this, "Failed to fetch assigned emergencies.", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    class JobAdapter extends RecyclerView.Adapter<JobAdapter.VH> {
        List<EmergencyDto> items;

        JobAdapter(List<EmergencyDto> it) {
            items = it;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_job, p, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH h, int i) {
            EmergencyDto e = items.get(i);

            String info = "Emergency ID: " + e.getId() + "\n" +
                    "Description: " + e.getDescription() + "\n" +
                    "Location: " + e.getLocationDescription() + "\n" +
                    "Coordinates: " + e.getLatitude() + ", " + e.getLongitude() + "\n" +
                    "Status: " + e.getStatus();

            h.tvJobInfo.setText(info);

            // You can add logic for the accept and complete buttons here
            // based on the emergency status, similar to your original code.
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvJobInfo;
            Button btnAccept, btnComplete;

            VH(View item) {
                super(item);
                tvJobInfo = item.findViewById(R.id.tvJobInfo);
                btnAccept = item.findViewById(R.id.btnAccept);
                btnComplete = item.findViewById(R.id.btnComplete);
            }
        }
    }
}
