package com.example.barcodegenerator;

import java.util.Date;

public class HistoryItem {
    private String id;
    private String content;
    private String type; // "SCANNED" or "GENERATED"
    private String format; // "QR_CODE", "CODE_128", "EAN_13", etc.
    private Date timestamp;
    private boolean isFavorite;
    private String category; // "Text", "URL", "Product", etc.

    public HistoryItem() {
        // Default constructor
    }

    public HistoryItem(String content, String type, String format, String category) {
        this.content = content;
        this.type = type;
        this.format = format;
        this.category = category;
        this.timestamp = new Date();
        this.isFavorite = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}