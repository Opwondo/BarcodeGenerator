package com.example.barcodegenerator;

import android.content.Context;

public class HistoryManager {
    private static HistoryManager instance;
    private DatabaseHelper databaseHelper;

    private HistoryManager(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public static synchronized HistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryManager(context);
        }
        return instance;
    }

    public void saveScan(String content, String format) {
        // Avoid duplicates
        if (!databaseHelper.itemExists(content, "SCANNED")) {
            HistoryItem item = new HistoryItem(content, "SCANNED", format, "Text");
            databaseHelper.addHistoryItem(item);
        }
    }

    public void saveGeneration(String content, String format, String category) {
        // Avoid duplicates
        if (!databaseHelper.itemExists(content, "GENERATED")) {
            HistoryItem item = new HistoryItem(content, "GENERATED", format, category);
            databaseHelper.addHistoryItem(item);
        }
    }
}