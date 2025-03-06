package com.example.login_journalapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton, signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Initialize UI elements
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_btn);
        signUpButton = findViewById(R.id.signup_btn);

        // Set click listeners for login and sign up buttons
        loginButton.setOnClickListener(this::handleLogin);
        signUpButton.setOnClickListener(v -> startActivity(new Intent(this, CreateAccountActivity.class)));
    }

    /**
     * Handles the login process.
     * Validates the input fields, checks credentials against local storage,
     * and redirects the user upon successful login.
     */
    private void handleLogin(View view) {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        try (FileInputStream fis = openFileInput("credentials.txt")) {
            byte[] data = new byte[fis.available()];
            fis.read(data);
            String credentials = new String(data, StandardCharsets.UTF_8);

            String[] userData = credentials.split("\n");
            String storedUsername = userData[0];
            String storedPassword = userData[1];

            if (username.equals(storedUsername) && password.equals(storedPassword)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ProfileActivity.class));
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Toast.makeText(this, "Error: Account doesn't exist.", Toast.LENGTH_SHORT).show();
        }
    }
}
