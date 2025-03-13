package com.example.login_journalapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AddEditDiaryActivity extends AppCompatActivity {

    private EditText titleInput, dateInput, contentInput;
    private Button saveButton, deleteButton;
    private SharedPreferences encryptedPrefs;
    private int entryIndex = -1; // Default for new entry

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_diary_page);

        // Initialize UI components
        titleInput = findViewById(R.id.diary_title_input);
        dateInput = findViewById(R.id.diary_date_input);
        contentInput = findViewById(R.id.diary_content_input);
        saveButton = findViewById(R.id.save_diary_entry_btn);
        deleteButton = findViewById(R.id.delete_diary_entry_btn);

        // ✅ Initialize EncryptedSharedPreferences
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
            Toast.makeText(this, "Error loading diary storage", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if editing an existing entry
        Intent intent = getIntent();
        if (intent.hasExtra("entry_index")) {
            entryIndex = intent.getIntExtra("entry_index", -1);
            titleInput.setText(intent.getStringExtra("title"));
            dateInput.setText(intent.getStringExtra("date"));
            contentInput.setText(intent.getStringExtra("content"));
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.GONE);
        }

        // Save entry
        saveButton.setOnClickListener(v -> saveDiaryEntry());

        // Delete entry
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());
    }

    /**
     * Saves a new or edited diary entry into SharedPreferences.
     */
    private void saveDiaryEntry() {
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();
        String currentDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = encryptedPrefs.edit();
        int entryCount = encryptedPrefs.getInt("diary_entry_count", 0);

        if (entryIndex == -1) { // New entry
            editor.putString("diary_entry_title_" + entryCount, title);
            editor.putString("diary_entry_date_" + entryCount, currentDate); // Store current date as modification date
            editor.putString("diary_entry_content_" + entryCount, content);
            editor.putInt("diary_entry_count", entryCount + 1);
        } else { // Editing existing entry
            editor.putString("diary_entry_title_" + entryIndex, title);
            editor.putString("diary_entry_date_" + entryIndex, currentDate); // Update modification date
            editor.putString("diary_entry_content_" + entryIndex, content);
        }

        editor.apply();
        Toast.makeText(this, "Diary entry saved!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }


    /**
     * Shows a confirmation dialog before deleting an entry.
     */
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this diary entry?")
                .setPositiveButton("Delete", (dialog, which) -> deleteDiaryEntry())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes a diary entry from SharedPreferences and updates the list.
     */
    private void deleteDiaryEntry() {
        if (entryIndex != -1) {
            SharedPreferences.Editor editor = encryptedPrefs.edit();
            int entryCount = encryptedPrefs.getInt("diary_entry_count", 0);

            for (int i = entryIndex; i < entryCount - 1; i++) {
                editor.putString("diary_entry_title_" + i, encryptedPrefs.getString("diary_entry_title_" + (i + 1), ""));
                editor.putString("diary_entry_date_" + i, encryptedPrefs.getString("diary_entry_date_" + (i + 1), ""));
                editor.putString("diary_entry_content_" + i, encryptedPrefs.getString("diary_entry_content_" + (i + 1), ""));
            }

            editor.remove("diary_entry_title_" + (entryCount - 1));
            editor.remove("diary_entry_date_" + (entryCount - 1));
            editor.remove("diary_entry_content_" + (entryCount - 1));
            editor.putInt("diary_entry_count", entryCount - 1);
            editor.apply();

            Toast.makeText(this, "Diary entry deleted!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }
}
