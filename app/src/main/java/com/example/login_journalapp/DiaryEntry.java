package com.example.login_journalapp;

public class DiaryEntry {
    private String title;
    private String date; // This should now represent the last modified date
    private String content;

    // Constructor
    public DiaryEntry(String title, String date, String content) {
        this.title = title;
        this.date = date; // Last modification date
        this.content = content;
    }

    // Getter and Setter for title
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and Setter for date (modification date)
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date; // Update modification date
    }

    // Getter and Setter for content
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    // Method to get a preview of the entry (first 50 characters)
    public String getPreviewText() {
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }
}
