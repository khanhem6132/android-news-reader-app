package com.example.newsapp;

public class Comment {
    private String username;
    private String content;
    private long timestamp;

    public Comment() {} // Firestore cần constructor rỗng

    public Comment(String username, String content, long timestamp) {
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
}
