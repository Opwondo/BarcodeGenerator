package com.example.barcodegenerator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BarcodeHistory.db";
    private static final int DATABASE_VERSION = 1;

    // History table name and columns
    private static final String TABLE_HISTORY = "history";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_FORMAT = "format";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_FAVORITE = "favorite";

    // NEW: Users table name and columns
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create history table
        String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_FORMAT + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_TIMESTAMP + " INTEGER,"
                + COLUMN_FAVORITE + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_HISTORY_TABLE);

        // NEW: Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_FULL_NAME + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ========== HISTORY METHODS (EXISTING) ==========

    // Add new history item
    public long addHistoryItem(HistoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTENT, item.getContent());
        values.put(COLUMN_TYPE, item.getType());
        values.put(COLUMN_FORMAT, item.getFormat());
        values.put(COLUMN_CATEGORY, item.getCategory());
        values.put(COLUMN_TIMESTAMP, item.getTimestamp().getTime());
        values.put(COLUMN_FAVORITE, item.isFavorite() ? 1 : 0);

        long id = db.insert(TABLE_HISTORY, null, values);
        db.close();
        return id;
    }

    // Get all history items
    public List<HistoryItem> getAllHistory() {
        List<HistoryItem> historyList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HistoryItem item = new HistoryItem();
                item.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                item.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                item.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                item.setFormat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FORMAT)));
                item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                item.setTimestamp(new java.util.Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))));
                item.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1);

                historyList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return historyList;
    }

    // Get favorite items only
    public List<HistoryItem> getFavorites() {
        List<HistoryItem> favoriteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HISTORY +
                " WHERE " + COLUMN_FAVORITE + " = 1" +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HistoryItem item = new HistoryItem();
                item.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                item.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                item.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                item.setFormat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FORMAT)));
                item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                item.setTimestamp(new java.util.Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))));
                item.setFavorite(true);

                favoriteList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return favoriteList;
    }

    // Toggle favorite status
    public void toggleFavorite(String itemId, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORITE, isFavorite ? 1 : 0);

        db.update(TABLE_HISTORY, values, COLUMN_ID + " = ?", new String[]{itemId});
        db.close();
    }

    // Delete history item
    public void deleteItem(String itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, COLUMN_ID + " = ?", new String[]{itemId});
        db.close();
    }

    // Check if item already exists (to avoid duplicates)
    public boolean itemExists(String content, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_HISTORY +
                " WHERE " + COLUMN_CONTENT + " = ? AND " + COLUMN_TYPE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{content, type});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // ========== USER MANAGEMENT METHODS (NEW) ==========

    // Register new user
    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword()); // Store hashed password
        values.put(COLUMN_FULL_NAME, user.getFullName());

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    // Login user
    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        }
        cursor.close();
        db.close();
        return user;
    }

    // Check if user exists by email
    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Update user password
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);

        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        return rowsAffected > 0;
    }

    // Get user by email only (for validation)
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        }
        cursor.close();
        db.close();
        return user;
    }
}