package com.example.login_journalapp;

public class DiaryEntry {
    private String title;
    private String date;
    private String content;

    // Constructor
    public DiaryEntry(String title, String date, String content) {
        this.title = title;
        this.date = date;
        this.content = content;
    }

    // Getter for title
    public String getTitle() {
        return title;
    }

    // Setter for title
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter for date
    public String getDate() {
        return date;
    }

    // Setter for date
    public void setDate(String date) {
        this.date = date;
    }

    // Getter for content
    public String getContent() {
        return content;
    }

    // Setter for content
    public void setContent(String content) {
        this.content = content;
    }

    // Method to get a preview of the entry (first 50 characters)
    public String getPreviewText() {
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }
}
