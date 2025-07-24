package com.example.mysecondproject;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import java.util.List;

public class DriverActivity extends AppCompatActivity {
    private EmergencyDAO eDao;
    private UserDAO uDao;
    private String myEmail;
    private RecyclerView rv;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_driver);

        eDao = new EmergencyDAO(this);
        uDao = new UserDAO(this);
        myEmail = getIntent().getStringExtra("email");

        rv = findViewById(R.id.rvJobs);
        statusText = findViewById(R.id.statusText); // Add this to your layout

        rv.setLayoutManager(new LinearLayoutManager(this));

        if (myEmail == null) {
            Toast.makeText(this, "Error: No email provided", Toast.LENGTH_SHORT).show();
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
        // Get my assignments
        List<Emergency> myJobs = eDao.fetchByDriverEmail(myEmail);

        // Update status display
        String currentStatus = uDao.getDriverStatus(myEmail);
        statusText.setText("Your Status: " + currentStatus);

        rv.setAdapter(new JobAdapter(myJobs));
    }

    class JobAdapter extends RecyclerView.Adapter<JobAdapter.VH> {
        List<Emergency> items;
        JobAdapter(List<Emergency> it) { items = it; }

        @Override
        public VH onCreateViewHolder(ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_job, p, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH h, int i) {
            Emergency e = items.get(i);

            String info = "Emergency ID: " + e.id + "\n" +
                         "Reporter: " + e.reporterEmail + "\n" +
                         "Location: " + e.address + "\n" +
                         "Coordinates: " + e.lat + ", " + e.lng + "\n" +
                         "Status: " + e.status;

            h.tvJobInfo.setText(info);

            // Enable/disable buttons based on status
            h.btnAccept.setEnabled("ASSIGNED".equals(e.status));
            h.btnComplete.setEnabled("IN_PROGRESS".equals(e.status));

            h.btnAccept.setOnClickListener(v -> {
                eDao.updateStatus(e.id, "IN_PROGRESS");
                uDao.updateDriverStatus(myEmail, "ON_DUTY");

                Toast.makeText(DriverActivity.this,
                        "Emergency accepted! Proceeding to location...",
                        Toast.LENGTH_SHORT).show();

                loadJobs();
                // TODO: Send notification to reporter and dispatcher
            });

            h.btnComplete.setOnClickListener(v -> {
                eDao.updateStatus(e.id, "RESOLVED");
                uDao.updateDriverStatus(myEmail, "FREE");

                Toast.makeText(DriverActivity.this,
                        "Emergency resolved! You are now available for new assignments.",
                        Toast.LENGTH_SHORT).show();

                loadJobs();
                // TODO: Send notification to reporter and dispatcher
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

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