package com.example.login_journalapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView fullNameText, dobText, addressText, phoneText;
    private Button editProfileButton, logoutButton, addEntryButton;
    private RecyclerView diaryRecyclerView;
    private DiaryAdapter diaryAdapter;
    private List<DiaryEntry> diaryEntries;
    private SharedPreferences encryptedPrefs;

    private static final int EDIT_PROFILE_REQUEST = 1;
    private static final int ADD_ENTRY_REQUEST = 2;
    private static final int EDIT_ENTRY_REQUEST = 3;

    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            loadUserProfile(); // ✅ Reload profile after editing
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        profileImageView = findViewById(R.id.profile_image_view);
        fullNameText = findViewById(R.id.full_name_text);
        dobText = findViewById(R.id.dob_text);
        addressText = findViewById(R.id.address_text);
        phoneText = findViewById(R.id.phone_text);
        editProfileButton = findViewById(R.id.edit_profile_btn);
        logoutButton = findViewById(R.id.logout_btn);
        addEntryButton = findViewById(R.id.entryinput_btn);
        diaryRecyclerView = findViewById(R.id.diary_recycler_view);

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
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            return;
        }

        loadUserProfile();
        diaryEntries = loadDiaryEntries();
        diaryAdapter = new DiaryAdapter(diaryEntries, this); // ✅ FIXED: Pass `this` as an activity
        diaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        diaryRecyclerView.setAdapter(diaryAdapter);

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });



        logoutButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        addEntryButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddEditDiaryActivity.class);
            startActivityForResult(intent, ADD_ENTRY_REQUEST);
        });
    }

    /**
     * Loads user profile data from encrypted shared preferences and displays it.
     */
    private void loadUserProfile() {
        if (encryptedPrefs == null) {
            Toast.makeText(this, "Error: Preferences not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = encryptedPrefs.getString("fullname", "N/A");
        String dob = encryptedPrefs.getString("dob", "N/A");
        String address = encryptedPrefs.getString("address", "N/A");
        String phone = encryptedPrefs.getString("phone", "N/A");
        String profileImageUri = encryptedPrefs.getString("profile_image", "");

        fullNameText.setText("Full Name: " + fullName);
        dobText.setText("Date of Birth: " + dob);
        addressText.setText("Address: " + address);
        phoneText.setText("Phone: " + phone);

        if (profileImageUri != null && !profileImageUri.isEmpty()) {
            try {
                profileImageView.setImageURI(Uri.parse(profileImageUri));
            } catch (Exception e) {
                profileImageView.setImageResource(R.drawable.default_profile);
            }
        } else {
            profileImageView.setImageResource(R.drawable.default_profile);
        }
    }

    /**
     * Loads diary entries from shared preferences.
     */
    private List<DiaryEntry> loadDiaryEntries() {
        List<DiaryEntry> entries = new ArrayList<>();
        if (encryptedPrefs == null) {
            Toast.makeText(this, "Error loading diary entries", Toast.LENGTH_SHORT).show();
            return entries;
        }

        int entryCount = encryptedPrefs.getInt("diary_entry_count", 0);

        for (int i = 0; i < entryCount; i++) {
            String title = encryptedPrefs.getString("diary_entry_title_" + i, "Untitled");
            String date = encryptedPrefs.getString("diary_entry_date_" + i, "Unknown Date");
            String content = encryptedPrefs.getString("diary_entry_content_" + i, "No content");

            if (title != null && date != null && content != null) {
                entries.add(new DiaryEntry(title, date, content));
            }
        }

        return entries;
    }

    /**
     * Handles result from Edit Profile and Diary Entry Activities.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_PROFILE_REQUEST) {
                loadUserProfile(); // ✅ Reload profile data when returning from Edit Profile
            } else if (requestCode == ADD_ENTRY_REQUEST || requestCode == EDIT_ENTRY_REQUEST) {
                diaryEntries = loadDiaryEntries();
                diaryAdapter = new DiaryAdapter(diaryEntries, this);
                diaryRecyclerView.setAdapter(diaryAdapter);
            }
        }
    }

    /**
     * Reloads the profile when returning from EditProfileActivity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile(); // ✅ Reload profile data when returning to this screen
    }
}
