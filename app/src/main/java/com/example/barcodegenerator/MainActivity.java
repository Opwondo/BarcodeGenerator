package com.example.barcodegenerator;

import android.content.Intent;
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
        setContentView(R.layout.activity_main);

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
                Toast.makeText(MainActivity.this, "Share App clicked", Toast.LENGTH_SHORT).show();
            }  else if (id == R.id.nav_settings) {
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

        // MODERN BACK PRESS HANDLING - Add this at the end of onCreate
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
        // These IDs need to match your XML - add them to your barcode format TextViews
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
                            Toast.makeText(MainActivity.this, formatName + " selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    // REMOVE the deprecated onBackPressed() method completely
    // Delete everything below this comment
}