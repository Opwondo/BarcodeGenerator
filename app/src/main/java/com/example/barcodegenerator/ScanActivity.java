package com.example.barcodegenerator;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ScanActivity extends AppCompatActivity {

    private ImageButton backButton;
    private View scanImageLayout;
    private View flashlightLayout;
    private View batchScanLayout;
    private ImageView flashlightIcon;
    private View scanLine;

    private boolean isFlashlightOn = false;
    private ObjectAnimator scanAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        scanImageLayout = findViewById(R.id.scan_image_layout);
        flashlightLayout = findViewById(R.id.flashlight_layout);
        batchScanLayout = findViewById(R.id.batch_scan_layout);
        flashlightIcon = findViewById(R.id.flashlight_icon);
        scanLine = findViewById(R.id.scan_line);

        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });

        // Scan Image button click listener
        scanImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ScanActivity.this,
                        "Scan Image from gallery", Toast.LENGTH_SHORT).show();
                // TODO: Implement image picker and barcode detection
            }
        });

        // Flashlight button click listener
        flashlightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFlashlight();
            }
        });

        // Batch Scan button click listener
        batchScanLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ScanActivity.this,
                        "Batch Scan mode activated", Toast.LENGTH_SHORT).show();
                // TODO: Implement continuous scanning mode
            }
        });

        // Start scanning animation
        startScanAnimation();

        // Simulate barcode detection after 3 seconds (for demo)
        simulateBarcodeDetection();
    }

    private void startScanAnimation() {
        scanAnimator = ObjectAnimator.ofFloat(scanLine, "translationY", 0f, 180f);
        scanAnimator.setDuration(2000);
        scanAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        scanAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        scanAnimator.setInterpolator(new LinearInterpolator());
        scanAnimator.start();
    }

    private void toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn;

        if (isFlashlightOn) {
            flashlightIcon.setImageResource(android.R.drawable.ic_media_play);
            Toast.makeText(this, "Flashlight ON", Toast.LENGTH_SHORT).show();
        } else {
            flashlightIcon.setImageResource(android.R.drawable.ic_menu_help);
            Toast.makeText(this, "Flashlight OFF", Toast.LENGTH_SHORT).show();
        }
    }

    private void simulateBarcodeDetection() {
        // Simulate barcode detection after 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // In real app, this would be triggered by actual barcode detection
                Toast.makeText(ScanActivity.this,
                        "Barcode detected! (Demo mode)", Toast.LENGTH_SHORT).show();
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop animation when activity is destroyed
        if (scanAnimator != null) {
            scanAnimator.cancel();
        }
    }
}