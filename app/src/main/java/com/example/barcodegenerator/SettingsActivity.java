package com.example.barcodegenerator;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView tvUserName, tvUserEmail;
    private SwitchCompat switchAutoSave, switchVibration, switchSound, switchAutoCopy;
    private Button btnLogout;
    private View btnClearHistory, btnExportData, btnPrivacyPolicy;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);

        initViews();
        loadUserProfile();
        loadSettings();
        setupClickListeners();
    }

    private void initViews() {
        backButton = findViewById(R.id.back_button);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        switchAutoSave = findViewById(R.id.switch_auto_save);
        switchVibration = findViewById(R.id.switch_vibration);
        switchSound = findViewById(R.id.switch_sound);
        switchAutoCopy = findViewById(R.id.switch_auto_copy);
        btnLogout = findViewById(R.id.btn_logout);
        btnClearHistory = findViewById(R.id.btn_clear_history);
        btnExportData = findViewById(R.id.btn_export_data);
        btnPrivacyPolicy = findViewById(R.id.btn_privacy_policy);
    }

    private void loadUserProfile() {
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = userPrefs.getString("user_name", "Guest User");
        String userEmail = userPrefs.getString("user_email", "guest@example.com");

        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);
    }

    private void loadSettings() {
        // Load saved settings or use defaults
        boolean autoSave = sharedPreferences.getBoolean("auto_save", true);
        boolean vibration = sharedPreferences.getBoolean("vibration", true);
        boolean sound = sharedPreferences.getBoolean("sound", false);
        boolean autoCopy = sharedPreferences.getBoolean("auto_copy", false);

        switchAutoSave.setChecked(autoSave);
        switchVibration.setChecked(vibration);
        switchSound.setChecked(sound);
        switchAutoCopy.setChecked(autoCopy);

        // Add listeners to save changes
        switchAutoSave.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSetting("auto_save", isChecked));

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSetting("vibration", isChecked));

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSetting("sound", isChecked));

        switchAutoCopy.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSetting("auto_copy", isChecked));
    }

    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();

        Toast.makeText(this, "Setting saved", Toast.LENGTH_SHORT).show();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        btnClearHistory.setOnClickListener(v -> showClearHistoryConfirmation());

        btnExportData.setOnClickListener(v -> exportScanHistory());

        btnPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performLogout() {
        try {
            // Clear user session
            SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = userPrefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Navigate to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show();
        }
    }

    private void showClearHistoryConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("This will delete all your scan history. This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> clearScanHistory())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void clearScanHistory() {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Clearing history...");
        progress.setCancelable(false);
        progress.show();

        new Thread(() -> {
            try {
                // Clear database using SQLite
                DatabaseHelper dbHelper = new DatabaseHelper(SettingsActivity.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete("scan_history", null, null); // Replace "scan_history" with your table name
                db.close();

                runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(SettingsActivity.this, "History cleared successfully", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(SettingsActivity.this, "Error clearing history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();

    }

    private void exportScanHistory() {
        // For now, show a message. You can implement CSV/PDF export later.
        Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void openPrivacyPolicy() {
        try {
            // Open privacy policy URL in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://yourwebsite.com/privacy-policy")); // Replace with your URL
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open browser", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to get settings from other activities
    public static boolean getSetting(android.content.Context context, String key, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences("app_settings", MODE_PRIVATE);
        return prefs.getBoolean(key, defaultValue);
    }

    // Helper method to update user profile
    public static void updateUserProfile(android.content.Context context, String name, String email) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_name", name);
        editor.putString("user_email", email);
        editor.apply();
    }
}