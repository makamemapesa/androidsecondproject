//package com.example.mysecondproject;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.*;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.android.volley.Request;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//public class RegisterActivity extends AppCompatActivity {
//    EditText fname, lname, email, password;
//    Spinner roleSpinner;
//    Button registerBtn;
//    UserDAO userDAO;
//
//    String API_URL = Constants.BASE_URL + "/user";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
//
//        fname = findViewById(R.id.fname);
//        lname = findViewById(R.id.lname);
//        email = findViewById(R.id.email);
//        password = findViewById(R.id.password);
//        roleSpinner = findViewById(R.id.role_spinner);
//        registerBtn = findViewById(R.id.registerBtn);
//
//        userDAO = new UserDAO(this);
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this, android.R.layout.simple_spinner_dropdown_item,
//                new String[]{"REPORTER", "DISPATCHER", "DRIVER"}
//        );
//        roleSpinner.setAdapter(adapter);
//
//        registerBtn.setOnClickListener(v -> {
//            String role = roleSpinner.getSelectedItem().toString();
//
//            if (!role.equalsIgnoreCase("REPORTER")) {
//                Toast.makeText(this, "Only REPORTERs can register themselves", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            boolean inserted = userDAO.registerUser(
//                    fname.getText().toString(),
//                    lname.getText().toString(),
//                    email.getText().toString(),
//                    password.getText().toString(),
//                    role
//            );
//
//            if (inserted) {
//                // ✅ Sync with backend
//                JSONObject json = new JSONObject();
//                try {
//                    json.put("firstName", fname.getText().toString());
//                    json.put("middleName", "N/A");
//                    json.put("lastName", lname.getText().toString());
//                    json.put("email", email.getText().toString());
//                    json.put("password", password.getText().toString());
//                    json.put("role", "REPORTER");
//                    json.put("phoneNumber", "000000000");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    return;
//                }
//
//                JsonObjectRequest request = new JsonObjectRequest(
//                        Request.Method.POST,
//                        API_URL,
//                        json,
//                        response -> {
//                            try {
//                                long backendUserId = response.getLong("id"); // make sure backend returns this
//                                userDAO.saveBackendId(email.getText().toString(), backendUserId);
//
//                                Toast.makeText(this, "Registered and synced successfully!", Toast.LENGTH_SHORT).show();
//                                startActivity(new Intent(this, LoginActivity.class));
//                                finish();
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                Toast.makeText(this, "Sync failed: Invalid server response.", Toast.LENGTH_SHORT).show();
//                            }
//                        },
//                        error -> {
//                            Toast.makeText(this, "Local saved. Sync to backend failed!", Toast.LENGTH_LONG).show();
//                            Log.e("RegisterSync", "Error: " + error.toString());
//                        }
//                );
//
//                Volley.newRequestQueue(this).add(request);
//
//            } else {
//                Toast.makeText(this, "User already exists!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}
package com.example.mysecondproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    EditText fname, mname, lname, email, password, phone, address;
    Spinner roleSpinner;
    Button registerBtn;
    UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fname = findViewById(R.id.fname);
        mname = findViewById(R.id.mname);
        lname = findViewById(R.id.lname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phone);
        address = findViewById(R.id.address);
        roleSpinner = findViewById(R.id.role_spinner);
        registerBtn = findViewById(R.id.registerBtn);

        userDAO = new UserDAO(this);

        // Populate spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"REPORTER", "DISPATCHER", "DRIVER"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        registerBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String firstName = fname.getText().toString().trim();
        String middleName = mname.getText().toString().trim();
        String lastName = lname.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String phoneText = phone.getText().toString().trim();
        String addressText = address.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || emailText.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!role.equals("REPORTER")) {
            Toast.makeText(this, "Only REPORTERs can register from the app", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = userDAO.registerUser(firstName, middleName, lastName, emailText, pass, phoneText, role, addressText);
        if (inserted) {
            sendToBackend(firstName, middleName, lastName, emailText, pass, phoneText, role, addressText);
        } else {
            Toast.makeText(this, "Local registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToBackend(String firstName, String middleName, String lastName, String email, String password, String phoneNumber, String role, String address) {
        String url = Constants.BASE_URL + "/user/register";

        JSONObject userJson = new JSONObject();
        try {
            userJson.put("firstName", firstName);
            userJson.put("middleName", middleName);
            userJson.put("lastName", lastName);
            userJson.put("email", email);
            userJson.put("password", password);
            userJson.put("phoneNumber", phoneNumber);
            userJson.put("role", role);
            userJson.put("address", address);

            // ✅ Add this log to see what you're sending
            Log.d("RegisterRequest", "Sending JSON: " + userJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("RegisterRequest", "JSON creation failed: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, userJson,
                response -> {
                    Log.d("RegisterRequest", "Success response: " + response.toString());
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                },
                error -> {
                    Log.e("RegisterRequest", "Error response: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("RegisterRequest", "Status Code: " + error.networkResponse.statusCode);
                        Log.e("RegisterRequest", "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(RegisterActivity.this, "Backend registration failed", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

}

