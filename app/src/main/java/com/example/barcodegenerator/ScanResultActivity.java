package com.example.barcodegenerator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanResultActivity<Preseed> extends AppCompatActivity {

    private TextView barcodeText;
    private TextView formatText;
    private View shareButton;
    private View saveButton;
    private View copyButton;
    private View newScanButton;
    private ImageButton backButton;
    private CardView resultCard;

    private String barcodeValue;
    private String barcodeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        initViews();
        setupClickListeners();

        // Get data from scanner - Support multiple parameter names for compatibility
        Intent intent = getIntent();
        barcodeValue = intent.getStringExtra("SCANNED_BARCODE");
        if (barcodeValue == null) {
            barcodeValue = intent.getStringExtra("SCAN_RESULT"); // Fallback for other scanners
        }

        barcodeFormat = intent.getStringExtra("BARCODE_FORMAT");
        if (barcodeFormat == null) {
            barcodeFormat = intent.getStringExtra("SCAN_FORMAT"); // Fallback for other scanners
        }

        displayResult();

        // Add entrance animation
        animateEntrance();
    }

    private void initViews() {
        barcodeText = findViewById(R.id.barcode_text);
        formatText = findViewById(R.id.format_text);
        shareButton = findViewById(R.id.share_button);
        saveButton = findViewById(R.id.save_button);
        copyButton = findViewById(R.id.copy_button);
        newScanButton = findViewById(R.id.new_scan_button);
        backButton = findViewById(R.id.back_button);
        resultCard = findViewById(R.id.result_card);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            // Add exit animation
            animateExit();
            finish();
        });

        shareButton.setOnClickListener(v -> shareBarcode());

        saveButton.setOnClickListener(v -> saveBarcodeImage());

        copyButton.setOnClickListener(v -> copyToClipboard());

        newScanButton.setOnClickListener(v -> {
            animateExit();
            Intent intent = new Intent(this, BarcodeScannerActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void displayResult() {
        if (barcodeValue != null) {
            barcodeText.setText(barcodeValue);

            // Make format more user-friendly
            String displayFormat = barcodeFormat;
            if (barcodeFormat != null) {
                // Convert format to more readable text
                displayFormat = barcodeFormat.replace("_", " ").toLowerCase();
                displayFormat = Character.toUpperCase(displayFormat.charAt(0)) + displayFormat.substring(1);
            } else {
                displayFormat = "Unknown format";
            }

            formatText.setText("Format: " + displayFormat);
        } else {
            barcodeText.setText("No barcode data received");
            formatText.setText("Format: Unknown");
        }
    }

    private void animateEntrance() {
        // Start with card invisible and slightly scaled down
        resultCard.setAlpha(0f);
        resultCard.setScaleX(0.9f);
        resultCard.setScaleY(0.9f);
        resultCard.setTranslationY(50f);

        // Animate to full visibility
        resultCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(500)
                .start();
    }

    private void animateExit() {
        resultCard.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .translationY(50f)
                .setDuration(300)
                .start();
    }

    private void shareBarcode() {
        if (barcodeValue == null) {
            Toast.makeText(this, "No barcode data to share", Toast.LENGTH_SHORT).show();
            return;
        }

        // Button animation
        animateButtonClick(shareButton);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Scanned Barcode:\n" + barcodeValue + "\nFormat: " + barcodeFormat);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Scanned Barcode");

        startActivity(Intent.createChooser(shareIntent, "Share Barcode"));
    }

    private void copyToClipboard() {
        if (barcodeValue == null) {
            Toast.makeText(this, "No barcode data to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Button animation
        animateButtonClick(copyButton);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Barcode", barcodeValue);
        clipboard.setPrimaryClip(clip);

        // Vibrate on copy
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(100);
        }

        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void saveBarcodeImage() {
        if (barcodeValue == null) {
            Toast.makeText(this, "No barcode data to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Button animation
        animateButtonClick(saveButton);

        try {
            // Generate barcode image
            Bitmap barcodeBitmap = generateBarcodeBitmap(barcodeValue, barcodeFormat);
            if (barcodeBitmap != null) {
                boolean saved = saveImageToGallery(barcodeBitmap);

                // Vibrate on save
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(100);
                }

                if (saved) {
                    Toast.makeText(this, "Barcode saved to gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save barcode", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Could not generate barcode image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving barcode", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void animateButtonClick(View button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    private Bitmap generateBarcodeBitmap(String data, String format) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE; // Default to QR code

            // Convert string format to BarcodeFormat
            if (format != null) {
                try {
                    barcodeFormat = BarcodeFormat.valueOf(format);
                } catch (Exception e) {
                    // Use appropriate fallback based on format name
                    if (format.toLowerCase().contains("qr")) {
                        barcodeFormat = BarcodeFormat.QR_CODE;
                    } else if (format.toLowerCase().contains("code128")) {
                        barcodeFormat = BarcodeFormat.CODE_128;
                    } else if (format.toLowerCase().contains("code39")) {
                        barcodeFormat = BarcodeFormat.CODE_39;
                    } else if (format.toLowerCase().contains("ean13")) {
                        barcodeFormat = BarcodeFormat.EAN_13;
                    } else if (format.toLowerCase().contains("upc")) {
                        barcodeFormat = BarcodeFormat.UPC_A;
                    }
                    // Otherwise keep QR code as default
                }
            }

            BitMatrix bitMatrix = writer.encode(data, barcodeFormat, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.createBitmap(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean saveImageToGallery(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Barcode_" + timeStamp + ".png";

        // Use our existing ImageSaveUtil for better compatibility
        return ImageSaveUtil.saveImageToGallery(this, bitmap, fileName);
    }

    public void enBack(Preseed pressed) {
        animateExit();
        super.onBackPressed();
    }
}