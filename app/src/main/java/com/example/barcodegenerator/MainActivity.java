package com.example.barcodegenerator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is logged in - if not, redirect to LoginActivity
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (!isLoggedIn) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return; // Important: prevent further execution
        }

        // Rest of your existing code...
        setContentView(R.layout.activity_main);
        // ... continue with the rest of your onCreate method}

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.menu_button);

        // Menu button click listener
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView);
            }
        });

        // Navigation drawer item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_history) {
                // ✅ UPDATED: Navigate to HistoryActivity
                Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(historyIntent);
            } else if (id == R.id.nav_favorite) {
                // ✅ UPDATED: Navigate to HistoryActivity with favorites filter
                Intent favoritesIntent = new Intent(MainActivity.this, HistoryActivity.class);
                favoritesIntent.putExtra("SHOW_FAVORITES", true);
                startActivity(favoritesIntent);
            } else if (id == R.id.nav_share) {
                // ✅ IMPLEMENTED: Share the App functionality
                shareApp();
            } else if (id == R.id.nav_settings) {
                Toast.makeText(MainActivity.this, "Settings clicked", Toast.LENGTH_SHORT).show();
            }

            // Close drawer after selection
            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        // Main action buttons
        CardView createBarcodeCard = findViewById(R.id.create_barcode_card);
        createBarcodeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateBarcodeActivity.class);
                startActivity(intent);
            }
        });

        // Scan button
        CardView scanCard = findViewById(R.id.scan_card);
        scanCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BarcodeScannerActivity.class);
                startActivity(intent);
            }
        });

        // History button
        CardView historyCard = findViewById(R.id.history_card);
        historyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ✅ UPDATED: Navigate to HistoryActivity
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // Create QR Code button
        CardView createQrCard = findViewById(R.id.create_qr_card);
        createQrCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateQRActivity.class);
                startActivity(intent);
            }
        });

        // More text view
        TextView moreTextView = findViewById(R.id.more_textView);
        moreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "More options", Toast.LENGTH_SHORT).show();
            }
        });

        // Barcode format items click listeners
        setupBarcodeFormatClickListeners();

        // MODERN BACK PRESS HANDLING
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    setEnabled(false);
                    MainActivity.super.onBackPressed();
                }
            }
        });
    }

    private void setupBarcodeFormatClickListeners() {
        int[] barcodeFormatIds = {
                R.id.ean13_text, R.id.code128_text, R.id.code39_text,
                R.id.ean8_text, R.id.upca_text, R.id.codabar_text,
                R.id.code11_text, R.id.code93_text
        };

        for (int id : barcodeFormatIds) {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v instanceof TextView) {
                            TextView textView = (TextView) v;
                            String formatName = textView.getText().toString();

                            openBarcodeCreation(formatName);
                        }
                    }
                });
            }
        }
    }

    /**
     Share the App functionality
     */
    private void shareApp() {
        try {
            // Get app name from resources
            String appName = getString(R.string.app_name);

            // Get package name (for Play Store link)
            String packageName = getPackageName();

            // Create share message
            String shareMessage = "Check out " + appName + " - The Ultimate Barcode Scanner & Generator!\n\n";
            shareMessage += "Scan any barcode or QR code instantly\n";
            shareMessage += "Generate custom barcodes and QR codes\n";
            shareMessage += "Perfect for inventory, shopping, and more!\n\n";
            shareMessage += "Download it from the Play Store:\n";
            shareMessage += "https://play.google.com/store/apps/details?id=" + packageName;

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out " + appName);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

            // Launch share dialog
            startActivity(Intent.createChooser(shareIntent, "Share " + appName));

        } catch (Exception e) {
            Toast.makeText(this, "Error sharing app: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *  Open barcode creation with selected format
     */
    private void openBarcodeCreation(String formatName) {
        // Show toast confirmation
        Toast.makeText(this, "Creating " + formatName + " barcode", Toast.LENGTH_SHORT).show();

        // Navigate to CreateBarcodeActivity with the selected format
        Intent intent = new Intent(MainActivity.this, CreateBarcodeActivity.class);
        intent.putExtra("SELECTED_FORMAT", formatName);
        startActivity(intent);
    }

    /**
     Share content from the app
     */
    public static void shareContent(android.content.Context context, String content, String title) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, content);

            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {
            Toast.makeText(context, "Error sharing content", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *Rate the app functionality
     */
    private void rateApp() {
        try {
            String packageName = getPackageName();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            startActivity(intent);
        } catch (Exception e) {
            // If Play Store not available, open browser
            String packageName = getPackageName();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            startActivity(intent);
        }
    }

    /**
     * Feedback functionality
     */
    private void sendFeedback() {
        try {
            String appName = getString(R.string.app_name);
            String versionName = "";

            try {
                versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                versionName = "Unknown";
            }

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@yourapp.com"}); // Replace with your support email
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, appName + " Feedback - v" + versionName);
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi Team,\n\nI would like to share some feedback about " + appName + ":\n\n");

            startActivity(Intent.createChooser(emailIntent, "Send feedback via"));
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
}