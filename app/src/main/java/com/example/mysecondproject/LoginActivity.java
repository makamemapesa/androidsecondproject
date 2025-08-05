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
    UserDAO userDAO;

    String API_URL = Constants.BASE_URL + "/driver/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.registerLink);

        userDAO = new UserDAO(this);

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

            Cursor cursor = userDAO.loginUser(email, password);
            if (cursor.moveToFirst()) {
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));

                Toast.makeText(this, "Logged in as " + role, Toast.LENGTH_SHORT).show();

                Intent i;
                switch (role.toUpperCase()) {
                    case "REPORTER":
                        i = new Intent(this, ReporterActivity.class);
                        i.putExtra("email", userEmail);
                        break;
                    case "DISPATCHER":
                        i = new Intent(this, DispatcherActivity.class);
                        i.putExtra("email", userEmail);
                        break;
                    case "DRIVER":
                        i = new Intent(this, DriverActivity.class);
                        i.putExtra("email", userEmail);
                        break;
                    default:
                        Toast.makeText(this, "Unknown role!", Toast.LENGTH_SHORT).show();
                        cursor.close();
                        return;
                }
                cursor.close();
                startActivity(i);
                finish();
            } else {
                // Fallback: try backend login (for driver only)
                JSONObject loginJson = new JSONObject();
                try {
                    loginJson.put("email", email);
                    loginJson.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        API_URL,
                        loginJson,
                        response -> {
                            Toast.makeText(this, "Logged in via API as DRIVER", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, DriverActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        },
                        error -> {
                            Toast.makeText(this, "Login failed!", Toast.LENGTH_SHORT).show();
                            Log.e("LoginAPI", "Error: " + error.toString());
                        }
                );

                Volley.newRequestQueue(this).add(request);
            }
        });
    }
}
