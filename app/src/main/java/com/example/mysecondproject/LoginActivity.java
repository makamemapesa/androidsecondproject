package com.example.mysecondproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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

    String DRIVER_LOGIN_URL = Constants.BASE_URL + "/driver/login";
    String USER_LOGIN_URL = Constants.BASE_URL + "/user/login";

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

            attemptDriverLogin(email, password);
        });
    }

    private void attemptDriverLogin(String email, String password) {
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
                DRIVER_LOGIN_URL,
                loginJson,
                response -> {
                    try {
                        long driverId = response.getLong("id");
                        Intent intent = new Intent(this, DriverActivity.class);
                        intent.putExtra("user_id", driverId);
                        intent.putExtra("user_email", email);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Login failed: Invalid response from server.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        // Driver not found, try logging in as a regular user
                        attemptUserLogin(email, password);
                    } else {
                        Toast.makeText(this, "Driver login failed.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void attemptUserLogin(String email, String password) {
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
                USER_LOGIN_URL,
                loginJson,
                response -> {
                    try {
                        String role = response.getString("role");
                        Intent intent;
                        switch (role.toUpperCase()) {
                            case "REPORTER":
                                intent = new Intent(this, ReporterActivity.class);
                                break;
                            case "DISPATCHER":
                                intent = new Intent(this, DispatcherActivity.class);
                                break;
                            default:
                                Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                                return;
                        }
                        intent.putExtra("user_id", response.getLong("id"));
                        intent.putExtra("user_email", email);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Login failed: Invalid response from server.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}
