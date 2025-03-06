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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText fullnameInput, dobInput, addressInput, phoneNumberInput;
    private EditText usernameInput, passwordInput, confirmPasswordInput;
    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createaccount_page);

        // Initialize UI elements
        fullnameInput = findViewById(R.id.fullname_input);
        dobInput = findViewById(R.id.dateofbirth_input);
        addressInput = findViewById(R.id.address_input);
        phoneNumberInput = findViewById(R.id.phonenumber_input);
        usernameInput = findViewById(R.id.createusername_input);
        passwordInput = findViewById(R.id.createpassword_input);
        confirmPasswordInput = findViewById(R.id.confirmpassword_input);
        createAccountButton = findViewById(R.id.createaccount_btn);

        // Set click listener for account creation
        createAccountButton.setOnClickListener(this::handleCreateAccount);
    }

    /**
     * Handles the account creation process.
     * Validates inputs, checks password requirements, verifies unique username,
     * and saves the user profile data to local storage.
     */
    private void handleCreateAccount(View view) {
        // Collect input data from UI
        String fullName = fullnameInput.getText().toString().trim();
        String dob = dobInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneNumberInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Check if any fields are empty
        if (fullName.isEmpty() || dob.isEmpty() || address.isEmpty() || phone.isEmpty()
                || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify that passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password strength
        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 8 characters, include letters, numbers, one capital letter, and one special character.", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if the username is already taken
        try (FileInputStream fis = openFileInput("credentials.txt")) {
            byte[] data = new byte[fis.available()];
            fis.read(data);
            String credentials = new String(data, StandardCharsets.UTF_8);

            String[] userData = credentials.split("\n");
            String storedUsername = userData[0];

            if (username.equals(storedUsername)) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (IOException ignored) {
            // File might not exist yet, which is fine for first-time registration
        }

        // Save profile data to local storage
        String profileData = username + "\n" + password + "\n" + fullName + "\n" + dob + "\n" + address + "\n" + phone;
        try (FileOutputStream fos = openFileOutput("credentials.txt", Context.MODE_PRIVATE)) {
            fos.write(profileData.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            // Redirect to login screen
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } catch (IOException e) {
            Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates the password against complexity requirements.
     * @param password The password to validate
     * @return true if password meets the criteria, false otherwise
     */
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        return pattern.matcher(password).matches();
    }
}
