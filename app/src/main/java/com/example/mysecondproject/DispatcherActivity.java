package com.example.mysecondproject;

//import android.os.Bundle;
//import android.widget.TextView;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class DispatcherActivity extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        TextView tv = new TextView(this);
//        tv.setText("Dispatcher Dashboard - You will receive panic alerts here.");
//        setContentView(tv);
//    }
//}


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import java.util.List;

public class DispatcherActivity extends AppCompatActivity {
    private EmergencyDAO eDao;
    private UserDAO uDao;
    private RecyclerView rv;
    private EmergAdapter adapter;

    @Override
    protected void onCreate(Bundle bd) {
        super.onCreate(bd);
        setContentView(R.layout.activity_dispatcher);

        eDao = new EmergencyDAO(this);
        uDao = new UserDAO(this);
        rv = findViewById(R.id.rvEmergencies);
        rv.setLayoutManager(new LinearLayoutManager(this));

        loadList();
    }

    private void loadList() {
        List<Emergency> list = eDao.fetchAll();
        adapter = new EmergAdapter(list);
        rv.setAdapter(adapter);
    }

    class EmergAdapter extends RecyclerView.Adapter<EmergAdapter.VH> {
        List<Emergency> items;
        EmergAdapter(List<Emergency> it) { items = it; }

        @Override public VH onCreateViewHolder(ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_emergency, p, false);
            return new VH(view);
        }
        @Override public void onBindViewHolder(VH h, int i) {
            Emergency em = items.get(i);
            h.tvReporter.setText("Reporter: " + em.reporterEmail);
            h.tvLocation.setText("Loc: " + em.lat + ", " + em.lng);
            h.tvStatus.setText("Status: " + em.status);

            h.btnAssign.setEnabled("NEW".equals(em.status));
//            h.btnAssign.setOnClickListener(v -> showDriverPicker(em.id));
        }
        @Override public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvReporter, tvLocation, tvStatus;
            Button btnAssign;
            VH(View item) {
                super(item);
                tvReporter = item.findViewById(R.id.tvReporter);
                tvLocation = item.findViewById(R.id.tvLocation);
                tvStatus   = item.findViewById(R.id.tvStatus);
                btnAssign  = item.findViewById(R.id.btnAssign);
            }
        }
    }

//    private void showDriverPicker(int emergencyId) {
//        // fetch all FREE drivers
//        List<String> freeDrivers = uDao.fetchByRoleAndStatus("DRIVER", "FREE");
//        String[] arr = freeDrivers.toArray(new String[0]);
//
//        new AlertDialog.Builder(this)
//                .setTitle("Assign Driver")
//                .setItems(arr, (dlg, which) -> {
//                    String email = arr[which];
//                    eDao.assignDriver(emergencyId, email);
//                    uDao.updateDriverStatus(email, "ON_PROGRESS");
//                    loadList();
//
//                })
//                .show();
//    }
}
