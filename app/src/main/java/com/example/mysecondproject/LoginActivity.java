package com.example.mysecondproject;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    EditText emailLogin, passwordLogin;
    Button loginBtn;
    TextView registerLink;

    String API_URL = Constants.BASE_URL + "/user/login"; // Use the general user login endpoint

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.registerLink);

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        loginBtn.setOnClickListener(v -> {
            String email = emailLogin.getText().toString().trim();
            String password = passwordLogin.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Perform backend login
            JSONObject loginJson = new JSONObject();
            try {
                loginJson.put("email", email);
                loginJson.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL,
                    loginJson,
                    response -> {
                        // On successful login, check the user's role
                        try {
                            String role = response.getString("role");
                            Toast.makeText(this, "Login successful. Role: " + role, Toast.LENGTH_SHORT).show();

                            Intent intent;
                            switch (role.toUpperCase()) {
                                case "REPORTER":
                                    intent = new Intent(this, ReporterActivity.class);
                                    break;
                                case "DRIVER":
                                    intent = new Intent(this, DriverActivity.class);
                                    break;
                                case "DISPATCHER":
                                    intent = new Intent(this, DispatcherActivity.class);
                                    break;
                                default:
                                    Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                                    return;
                            }
                            // Pass user info to the next activity if needed
                            intent.putExtra("user_email", email);
                            intent.putExtra("user_id", response.getLong("id"));
                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Login failed: Invalid response from server.", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        // Enhanced error logging
                        String errorMessage = "Login failed";
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            errorMessage += " (Error " + statusCode + ")";
                            if (statusCode == 401) {
                                errorMessage = "Invalid email or password.";
                            }
                        } else if (error.getCause() != null) {
                            errorMessage += ": " + error.getCause().getMessage();
                        }
                        Log.e("LoginAPI", "Error: " + error.toString());
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
            );

            Volley.newRequestQueue(this).add(request);
        });
    }
}
