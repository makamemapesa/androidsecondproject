package com.example.mysecondproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    EditText fname, lname, email, password;
    Spinner roleSpinner;
    Button registerBtn;
    UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fname = findViewById(R.id.fname);
        lname = findViewById(R.id.lname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        roleSpinner = findViewById(R.id.role_spinner);
        registerBtn = findViewById(R.id.registerBtn);
        userDAO = new UserDAO(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"REPORTER", "DISPATCHER", "DRIVER"});
        roleSpinner.setAdapter(adapter);

        registerBtn.setOnClickListener(v -> {
            boolean inserted = userDAO.registerUser(
                fname.getText().toString(),
                lname.getText().toString(),
                email.getText().toString(),
                password.getText().toString(),
                roleSpinner.getSelectedItem().toString()
            );
            if (inserted) {
                Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                Toast.makeText(this, "Email Already Used!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}