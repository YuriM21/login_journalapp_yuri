package com.example.login_journalapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.regex.Pattern;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText fullnameInput, dobInput, addressInput, phoneNumberInput;
    private EditText usernameInput, passwordInput, confirmPasswordInput;
    private Button createAccountButton, uploadImageButton;
    private ImageView profileImage;
    private SharedPreferences encryptedPrefs;
    private Uri selectedImageUri;

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
        uploadImageButton = findViewById(R.id.uploadpic_btn);
        profileImage = findViewById(R.id.profileimage_upload);

        // Initialize EncryptedSharedPreferences
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    this,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Toast.makeText(this, "Encryption error!", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageButton.setOnClickListener(v -> openGallery());
        createAccountButton.setOnClickListener(this::handleCreateAccount);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(intent);
    }

    private final androidx.activity.result.ActivityResultLauncher<Intent> galleryActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                selectedImageUri = result.getData().getData();
                                profileImage.setImageURI(selectedImageUri);
                            }
                        }
                    });

    private void handleCreateAccount(View view) {
        String fullName = fullnameInput.getText().toString().trim();
        String dob = dobInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneNumberInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (fullName.isEmpty() || dob.isEmpty() || address.isEmpty() || phone.isEmpty()
                || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 8 characters, include letters, numbers, one capital letter, and one special character.", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if username exists
        String existingUsername = encryptedPrefs.getString("username", null);
        if (existingUsername != null && existingUsername.equals(username)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Store user details securely
        SharedPreferences.Editor editor = encryptedPrefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("fullname", fullName);
        editor.putString("dob", dob);
        editor.putString("address", address);
        editor.putString("phone", phone);

        // Save profile image URI if selected
        if (selectedImageUri != null) {
            editor.putString("profile_image", selectedImageUri.toString());
        }

        editor.apply();

        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        return pattern.matcher(password).matches();
    }
}
