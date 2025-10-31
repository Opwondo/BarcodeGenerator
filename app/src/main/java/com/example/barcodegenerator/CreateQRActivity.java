package com.example.barcodegenerator;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class CreateQRActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Spinner typeSpinner;
    private TextView qrTextEditText;
    private TextView charCountText;
    private TextView warningText;
    private Button createButton;
    private LinearLayout previewLayout;
    private ImageView qrPreview;
    private TextView qrDataText;
    private Button saveButton;
    private Bitmap currentQRBitmap;
    private String currentQRData;
    private String currentQRType;

    private int maxChars = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qractivity);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        typeSpinner = findViewById(R.id.type_spinner);
        qrTextEditText = findViewById(R.id.qr_text_editText);
        charCountText = findViewById(R.id.char_count_text);
        warningText = findViewById(R.id.warning_text);
        createButton = findViewById(R.id.create_button);
        previewLayout = findViewById(R.id.preview_layout);
        qrPreview = findViewById(R.id.qr_preview);
        qrDataText = findViewById(R.id.qr_data_text);
        saveButton = findViewById(R.id.save_button);

        // Setup spinner
        setupSpinner();

        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Text change listener for character counting
        qrTextEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int currentLength = s.length();
                charCountText.setText(currentLength + "/" + maxChars);

                // Show/hide warning based on character count
                if (currentLength > maxChars) {
                    warningText.setVisibility(View.VISIBLE);
                } else {
                    warningText.setVisibility(View.GONE);
                }
            }
        });

        // Create button click listener
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrText = qrTextEditText.getText().toString().trim();
                String selectedType = typeSpinner.getSelectedItem().toString();

                if (qrText.isEmpty()) {
                    Toast.makeText(CreateQRActivity.this,
                            "Please enter text for the QR code", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Format the text based on selected type
                String formattedText = formatQRText(qrText, selectedType);

                if (formattedText.length() > maxChars) {
                    Toast.makeText(CreateQRActivity.this,
                            "Text exceeds recommended length. QR code may be difficult to scan.",
                            Toast.LENGTH_LONG).show();
                }

                // Generate QR code
                Bitmap qrBitmap = generateQRCode(formattedText);

                if (qrBitmap != null) {
                    // Show preview
                    qrPreview.setImageBitmap(qrBitmap);
                    qrDataText.setText(getDisplayText(qrText, selectedType));
                    previewLayout.setVisibility(View.VISIBLE);

                    // Store for saving
                    currentQRBitmap = qrBitmap;
                    currentQRData = qrText;
                    currentQRType = selectedType;

                    // ✅ ADD THIS: Save to history when generated
                    saveToHistory(qrText, selectedType);

                    // Show save button
                    saveButton.setVisibility(View.VISIBLE);

                    Toast.makeText(CreateQRActivity.this,
                            "QR Code created successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateQRActivity.this,
                            "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQRBitmap != null) {
                    String fileName = ImageSaveUtil.generateFileName("QRCode", currentQRType);
                    boolean saved = ImageSaveUtil.saveImageToGallery(CreateQRActivity.this, currentQRBitmap, fileName);

                    if (saved) {
                        Toast.makeText(CreateQRActivity.this,
                                "QR Code saved to Gallery!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(CreateQRActivity.this,
                            "No QR code to save. Generate a QR code first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ✅ ADD THIS NEW METHOD TO SAVE TO HISTORY
    private void saveToHistory(String qrData, String type) {
        try {
            String category = type; // Use the QR type as category (Text, URL, Contact, etc.)
            String format = "QR_CODE";
            HistoryManager.getInstance(this).saveGeneration(qrData, format, category);
            android.util.Log.d("CreateQR", "Saved to history: " + qrData + " (" + type + ")");
        } catch (Exception e) {
            android.util.Log.e("CreateQR", "Failed to save to history", e);
        }
    }

    private void setupSpinner() {
        // Create array of QR code types
        String[] qrTypes = {"Text", "URL", "Contact", "Wi-Fi", "SMS", "Email", "Location"};

        // Create adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                qrTypes
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        // Set default selection
        typeSpinner.setSelection(0); // "Text" is default

        // Spinner item selection listener
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                updateInputHint(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private String formatQRText(String text, String type) {
        switch (type) {
            case "URL":
                if (!text.startsWith("http://") && !text.startsWith("https://")) {
                    return "https://" + text;
                }
                return text;

            case "SMS":
                // Format: sms:phone_number?body=message
                String[] smsParts = text.split(",", 2);
                if (smsParts.length == 2) {
                    return "sms:" + smsParts[0].trim() + "?body=" + smsParts[1].trim();
                }
                return "sms:" + text;

            case "Email":
                // Format: mailto:email?subject=subject&body=body
                String[] emailParts = text.split(",", 3);
                if (emailParts.length == 3) {
                    return "mailto:" + emailParts[0].trim() +
                            "?subject=" + emailParts[1].trim() +
                            "&body=" + emailParts[2].trim();
                }
                return "mailto:" + text;

            case "Wi-Fi":
                // Simple format for demo: WIFI:S:SSID;T:WPA;P:Password;;
                return "WIFI:S:" + text + ";T:WPA;P:password;;";

            case "Contact":
                // Simple vCard format
                return "BEGIN:VCARD\nVERSION:3.0\nFN:" + text + "\nEND:VCARD";

            case "Location":
                // geo:latitude,longitude
                if (text.contains(",")) {
                    return "geo:" + text;
                }
                return text;

            default: // Text
                return text;
        }
    }

    private String getDisplayText(String text, String type) {
        // Return a shortened version for display
        if (text.length() > 20) {
            return text.substring(0, 17) + "...";
        }
        return text;
    }

    private Bitmap generateQRCode(String text) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateInputHint(String selectedType) {
        String hint = "";

        switch (selectedType) {
            case "Text":
                hint = "Please fill in the text here.";
                break;
            case "URL":
                hint = "Enter website URL (e.g., example.com or https://example.com)";
                break;
            case "Contact":
                hint = "Enter contact name";
                break;
            case "Wi-Fi":
                hint = "Enter Wi-Fi network name (SSID)";
                break;
            case "SMS":
                hint = "Enter phone number, message (e.g., 1234567890, Hello)";
                break;
            case "Email":
                hint = "Enter email, subject, body (e.g., test@email.com, Subject, Message)";
                break;
            case "Location":
                hint = "Enter latitude, longitude (e.g., 40.7128, -74.0060)";
                break;
        }

        qrTextEditText.setHint(hint);
    }
}