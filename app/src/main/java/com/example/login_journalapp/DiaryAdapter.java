package com.example.login_journalapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    private List<DiaryEntry> diaryEntries;
    private Activity activity;
    private SharedPreferences encryptedPrefs;

    public DiaryAdapter(List<DiaryEntry> diaryEntries, Activity activity) {
        this.diaryEntries = diaryEntries;
        this.activity = activity;

        // Initialize EncryptedSharedPreferences
        try {
            MasterKey masterKey = new MasterKey.Builder(activity)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    activity,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Toast.makeText(activity, "Error loading diary storage", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diary_entry_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryEntry entry = diaryEntries.get(position);
        holder.entryTitle.setText(entry.getTitle());
        holder.entryDate.setText(entry.getDate());
        holder.entryText.setText(entry.getContent());

        // Edit Entry
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(activity, AddEditDiaryActivity.class);
            intent.putExtra("entry_index", position);
            intent.putExtra("title", entry.getTitle());
            intent.putExtra("date", entry.getDate());
            intent.putExtra("content", entry.getContent());
            activity.startActivityForResult(intent, 2);
        });

        // Delete Entry
        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmation(position));
    }

    @Override
    public int getItemCount() {
        return diaryEntries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView entryTitle, entryDate, entryText;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            entryTitle = itemView.findViewById(R.id.diary_entry_title);
            entryDate = itemView.findViewById(R.id.diary_entry_date);
            entryText = itemView.findViewById(R.id.diary_entry_text);
            deleteButton = itemView.findViewById(R.id.delete_diary_entry_btn);
        }
    }

    /**
     * Shows a confirmation dialog before deleting a diary entry.
     */
    private void showDeleteConfirmation(int position) {
        new AlertDialog.Builder(activity)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this diary entry?")
                .setPositiveButton("Delete", (dialog, which) -> deleteDiaryEntry(position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes a diary entry from SharedPreferences and updates the RecyclerView.
     */
    private void deleteDiaryEntry(int position) {
        SharedPreferences.Editor editor = encryptedPrefs.edit();
        int entryCount = encryptedPrefs.getInt("diary_entry_count", 0);

        for (int i = position; i < entryCount - 1; i++) {
            editor.putString("diary_entry_title_" + i, encryptedPrefs.getString("diary_entry_title_" + (i + 1), ""));
            editor.putString("diary_entry_date_" + i, encryptedPrefs.getString("diary_entry_date_" + (i + 1), ""));
            editor.putString("diary_entry_content_" + i, encryptedPrefs.getString("diary_entry_content_" + (i + 1), ""));
        }

        editor.remove("diary_entry_title_" + (entryCount - 1));
        editor.remove("diary_entry_date_" + (entryCount - 1));
        editor.remove("diary_entry_content_" + (entryCount - 1));
        editor.putInt("diary_entry_count", entryCount - 1);
        editor.apply();

        diaryEntries.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, diaryEntries.size());

        Toast.makeText(activity, "Diary entry deleted!", Toast.LENGTH_SHORT).show();
    }
}
