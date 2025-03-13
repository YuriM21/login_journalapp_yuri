package com.example.login_journalapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private EditText fullnameInput, dobInput, addressInput, phoneInput, usernameInput, passwordInput;
    private Button saveChangesButton, changeProfilePicButton, deleteProfileButton;
    private SharedPreferences encryptedPrefs;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile_page);

        // Initialize UI Elements
        profileImageView = findViewById(R.id.profile_image_view);
        fullnameInput = findViewById(R.id.fullname_input);
        dobInput = findViewById(R.id.dateofbirth_input);
        addressInput = findViewById(R.id.address_input);
        phoneInput = findViewById(R.id.phonenumber_input);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        saveChangesButton = findViewById(R.id.save_changes_btn);
        changeProfilePicButton = findViewById(R.id.change_profile_pic_btn);
        deleteProfileButton = findViewById(R.id.delete_profile_btn);

        /**
         * initialize sharedpreference
         */
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
            Toast.makeText(this, "Error: Secure storage failed to load. Try restarting the app.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
            return;
        }

        if (encryptedPrefs == null) {
            Toast.makeText(this, "Error: Unable to access preferences.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load user details from SharedPreferences
        loadUserDetails();

        // Handle profile picture change
        changeProfilePicButton.setOnClickListener(v -> openGallery());

        // Save changes button
        saveChangesButton.setOnClickListener(v -> saveUserChanges());

        // Delete account button
        deleteProfileButton.setOnClickListener(v -> showDeleteConfirmation());
    }

    /**
     * Opens the gallery for selecting a new profile picture.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(intent);
    }

    /**
     * Handles the result of the image picker.
     */
    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                                selectedImageUri = result.getData().getData();
                                profileImageView.setImageURI(selectedImageUri);
                            }
                        }
                    });

    /**
     * Loads user details from SharedPreferences and populates the UI.
     */
    private void loadUserDetails() {
        if (encryptedPrefs == null) {
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            return;
        }

        fullnameInput.setText(encryptedPrefs.getString("fullname", ""));
        dobInput.setText(encryptedPrefs.getString("dob", ""));
        addressInput.setText(encryptedPrefs.getString("address", ""));
        phoneInput.setText(encryptedPrefs.getString("phone", ""));
        usernameInput.setText(encryptedPrefs.getString("username", ""));
        passwordInput.setText(encryptedPrefs.getString("password", ""));

        // ✅ Prevent App Crash from Invalid Image URI
        String profileImageUri = encryptedPrefs.getString("profile_image", null);
        if (profileImageUri != null && !profileImageUri.isEmpty()) {
            try {
                profileImageView.setImageURI(Uri.parse(profileImageUri));
            } catch (Exception e) {
                profileImageView.setImageResource(R.drawable.default_profile); // Set default image if error
            }
        } else {
            profileImageView.setImageResource(R.drawable.default_profile);
        }
    }

    /**
     * Saves user changes and updates SharedPreferences.
     */
    private void saveUserChanges() {
        if (encryptedPrefs == null) {
            Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show();
            return;
        }

        String newFullname = fullnameInput.getText().toString().trim();
        String newDob = dobInput.getText().toString().trim();
        String newAddress = addressInput.getText().toString().trim();
        String newPhone = phoneInput.getText().toString().trim();
        String newUsername = usernameInput.getText().toString().trim();
        String newPassword = passwordInput.getText().toString().trim();

        SharedPreferences.Editor editor = encryptedPrefs.edit();
        editor.putString("fullname", newFullname);
        editor.putString("dob", newDob);
        editor.putString("address", newAddress);
        editor.putString("phone", newPhone);
        editor.putString("username", newUsername);
        editor.putString("password", newPassword);

        // Ensure Profile Image is Updated
        if (selectedImageUri != null) {
            editor.putString("profile_image", selectedImageUri.toString());
        }

        editor.apply();
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

        // Send success result to ProfileActivity
        Intent intent = new Intent();
        intent.putExtra("profile_updated", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Shows a confirmation dialog before deleting the account.
     */
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes all user data from EncryptedSharedPreferences and logs out.
     */
    private void deleteAccount() {
        if (encryptedPrefs == null) {
            Toast.makeText(this, "Error: Unable to delete account", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = encryptedPrefs.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Account deleted successfully!", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
        startActivity(intent);
    }
}
