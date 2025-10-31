package com.example.barcodegenerator;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.oned.UPCAWriter;

public class CreateBarcodeActivity extends AppCompatActivity {

    private ImageButton backButton;
    private RadioGroup barcodeTypeGroup;
    private TextView barcodeDataEditText;
    private TextView formatInstructions;
    private Button generateButton;
    private ImageView barcodePreview;
    private TextView barcodeDataText;
    private View previewLayout;
    private Button saveButton;
    private Bitmap currentBarcodeBitmap;
    private String currentBarcodeData;
    private String currentBarcodeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_barcode);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        barcodeTypeGroup = findViewById(R.id.barcode_type_group);
        barcodeDataEditText = findViewById(R.id.barcode_data_editText);
        formatInstructions = findViewById(R.id.format_instructions);
        generateButton = findViewById(R.id.generate_button);
        barcodePreview = findViewById(R.id.barcode_preview);
        barcodeDataText = findViewById(R.id.barcode_data_text);
        previewLayout = findViewById(R.id.preview_layout);
        saveButton = findViewById(R.id.save_button);

        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Barcode type selection listener
        barcodeTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_single) {
                    updateFormatInstructions("UPC-E");
                } else if (checkedId == R.id.radio_branch) {
                    updateFormatInstructions("Code 128");
                }
            }
        });

        // Generate button click listener
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String barcodeData = barcodeDataEditText.getText().toString().trim();

                if (barcodeData.isEmpty()) {
                    Toast.makeText(CreateBarcodeActivity.this,
                            "Please enter barcode data", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get selected barcode type
                String selectedFormat = getSelectedFormat();

                // Generate barcode
                Bitmap barcodeBitmap = generateBarcode(barcodeData, selectedFormat);

                if (barcodeBitmap != null) {
                    // Show preview
                    barcodePreview.setImageBitmap(barcodeBitmap);
                    barcodeDataText.setText(barcodeData);
                    previewLayout.setVisibility(View.VISIBLE);

                    // Store for saving
                    currentBarcodeBitmap = barcodeBitmap;
                    currentBarcodeData = barcodeData;
                    currentBarcodeFormat = selectedFormat;

                    // ✅ ADD THIS: Save to history when generated
                    saveToHistory(barcodeData, selectedFormat);

                    // Show save button
                    saveButton.setVisibility(View.VISIBLE);

                    Toast.makeText(CreateBarcodeActivity.this,
                            "Barcode generated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateBarcodeActivity.this,
                            "Failed to generate barcode. Check your input.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentBarcodeBitmap != null) {
                    String fileName = ImageSaveUtil.generateFileName("Barcode", currentBarcodeFormat);
                    boolean saved = ImageSaveUtil.saveImageToGallery(CreateBarcodeActivity.this, currentBarcodeBitmap, fileName);

                    if (saved) {
                        Toast.makeText(CreateBarcodeActivity.this,
                                "Barcode saved to Gallery!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(CreateBarcodeActivity.this,
                            "No barcode to save. Generate a barcode first.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set initial format instructions
        updateFormatInstructions("UPC-E");
    }

    // ✅ ADD THIS NEW METHOD TO SAVE TO HISTORY
    private void saveToHistory(String barcodeData, String format) {
        try {
            String category = "Generated Barcode";
            HistoryManager.getInstance(this).saveGeneration(barcodeData, format, category);
            android.util.Log.d("CreateBarcode", "Saved to history: " + barcodeData + " (" + format + ")");
        } catch (Exception e) {
            android.util.Log.e("CreateBarcode", "Failed to save to history", e);
        }
    }

    private String getSelectedFormat() {
        int selectedId = barcodeTypeGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_single) {
            return "UPC-A"; // Using UPC-A instead of UPC-E for simplicity
        } else {
            return "Code 128";
        }
    }

    private Bitmap generateBarcode(String data, String format) {
        try {
            BitMatrix bitMatrix;
            int width = 600;
            int height = 300;

            switch (format) {
                case "UPC-A":
                    if (data.length() != 12) {
                        Toast.makeText(this, "UPC-A requires exactly 12 digits", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                    bitMatrix = new UPCAWriter().encode(data, BarcodeFormat.UPC_A, width, height);
                    break;

                case "Code 128":
                    bitMatrix = new Code128Writer().encode(data, BarcodeFormat.CODE_128, width, height);
                    break;

                case "EAN-13":
                    if (data.length() != 13) {
                        Toast.makeText(this, "EAN-13 requires exactly 13 digits", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                    bitMatrix = new EAN13Writer().encode(data, BarcodeFormat.EAN_13, width, height);
                    break;

                default:
                    // Default to Code 128
                    bitMatrix = new Code128Writer().encode(data, BarcodeFormat.CODE_128, width, height);
            }

            return bitMatrixToBitmap(bitMatrix);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap bitMatrixToBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    private void updateFormatInstructions(String format) {
        String instructions = "";

        switch (format) {
            case "UPC-E":
                instructions = "UPC-A (Simplified)\nEnter 12 digits for UPC-A barcode. Used for retail products in North America.";
                break;
            case "Code 128":
                instructions = "Code 128\nSupports alphanumeric characters. Very versatile format used in shipping and packaging.";
                break;
            case "EAN-13":
                instructions = "EAN-13\nEnter 13 digits. Used worldwide for retail products.";
                break;
            default:
                instructions = "Enter barcode data according to the selected format specifications.";
        }

        formatInstructions.setText(instructions);
    }
}