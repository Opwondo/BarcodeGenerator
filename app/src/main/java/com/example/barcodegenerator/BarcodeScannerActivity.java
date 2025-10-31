package com.example.barcodegenerator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.BarcodeFormat;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScannerActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageButton backButton;
    private View scanImageLayout;
    private View flashlightLayout;
    private View batchScanLayout;
    private ImageView flashlightIcon;
    private View scanLine;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Camera camera;
    private boolean isFlashOn = false;
    private ExecutorService cameraExecutor;
    private ImageAnalysis imageAnalysis;
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        initViews();
        setupClickListeners();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void initViews() {
        previewView = findViewById(R.id.camera_preview);
        backButton = findViewById(R.id.back_button);
        scanImageLayout = findViewById(R.id.scan_image_layout);
        flashlightLayout = findViewById(R.id.flashlight_layout);
        batchScanLayout = findViewById(R.id.batch_scan_layout);
        flashlightIcon = findViewById(R.id.flashlight_icon);
        scanLine = findViewById(R.id.scan_line);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        scanImageLayout.setOnClickListener(v -> {
            Toast.makeText(this, "Scan from image - Coming soon", Toast.LENGTH_SHORT).show();
        });

        flashlightLayout.setOnClickListener(v -> toggleFlashlight());

        batchScanLayout.setOnClickListener(v -> {
            Toast.makeText(this, "Batch scan mode", Toast.LENGTH_SHORT).show();
        });
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("BarcodeScanner", "Error starting camera", e);
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(1920, 1080)) // Higher resolution for better detection
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Optimize ImageAnalysis
        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720)) // Balanced resolution
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888) // Easier processing
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new BarcodeAnalyzer());

        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void toggleFlashlight() {
        if (camera != null) {
            if (camera.getCameraInfo().hasFlashUnit()) {
                if (isFlashOn) {
                    camera.getCameraControl().enableTorch(false);
                    flashlightIcon.setImageResource(android.R.drawable.ic_menu_help);
                    isFlashOn = false;
                } else {
                    camera.getCameraControl().enableTorch(true);
                    flashlightIcon.setImageResource(android.R.drawable.ic_media_play);
                    isFlashOn = true;
                }
            } else {
                Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ADD THIS NEW METHOD TO SAVE TO HISTORY
    private void saveToHistory(String barcodeValue, String format) {
        try {
            HistoryManager.getInstance(this).saveScan(barcodeValue, format);
            Log.d("BarcodeScanner", "Saved to history: " + barcodeValue);
        } catch (Exception e) {
            Log.e("BarcodeScanner", "Failed to save to history", e);
        }
    }

    private class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
        private final MultiFormatReader reader;
        private long lastScanTime = 0;
        private static final long SCAN_COOLDOWN = 1000; // 1 second between scans
        private int frameCount = 0;
        private long startTime = System.currentTimeMillis();

        public BarcodeAnalyzer() {
            reader = new MultiFormatReader();

            // Include all common barcode formats
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS,
                    EnumSet.of(
                            BarcodeFormat.UPC_A,
                            BarcodeFormat.UPC_E,
                            BarcodeFormat.EAN_13,
                            BarcodeFormat.EAN_8,
                            BarcodeFormat.CODE_128,
                            BarcodeFormat.CODE_39,
                            BarcodeFormat.CODE_93,
                            BarcodeFormat.CODABAR,
                            BarcodeFormat.ITF,
                            BarcodeFormat.QR_CODE,
                            BarcodeFormat.DATA_MATRIX,
                            BarcodeFormat.PDF_417
                    ));
            reader.setHints(hints);
        }

        @Override
        public void analyze(@NonNull ImageProxy image) {
            if (!isScanning) {
                image.close();
                return;
            }

            // Performance monitoring
            frameCount++;
            if (frameCount % 30 == 0) {
                long currentTime = System.currentTimeMillis();
                float fps = 30000f / (currentTime - startTime);
                Log.d("BarcodeScanner", "FPS: " + fps);
                startTime = currentTime;
            }

            // Add cooldown to prevent excessive scanning
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastScanTime < SCAN_COOLDOWN) {
                image.close();
                return;
            }

            try {
                // Process image directly using efficient conversion
                Bitmap bitmap = imageToBitmap(image);
                if (bitmap != null) {
                    Result result = detectBarcodeWithRotation(bitmap);
                    if (result != null) {
                        lastScanTime = currentTime;
                        String barcodeValue = result.getText();
                        String format = result.getBarcodeFormat().toString();
                        runOnUiThread(() -> handleBarcodeResult(barcodeValue, format));
                    }
                }
            } catch (Exception e) {
                Log.e("BarcodeScanner", "Analysis error", e);
            } finally {
                image.close();
            }
        }

        private Bitmap imageToBitmap(ImageProxy image) {
            // Efficient conversion using ImageProxy's built-in method
            try {
                return image.toBitmap();
            } catch (Exception e) {
                Log.e("BarcodeScanner", "Bitmap conversion failed", e);
                return null;
            }
        }

        private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
            if (degrees != 0 && bitmap != null) {
                Matrix matrix = new Matrix();
                matrix.postRotate(degrees);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
            return bitmap;
        }

        private Result detectBarcodeWithRotation(Bitmap bitmap) {
            // Try original orientation first (fastest)
            Result result = scanSingleOrientation(bitmap, 0);
            if (result != null) return result;

            // If no barcode found, try other orientations (slower but more thorough)
            // Barcodes often need to be horizontal to be detected
            int[] rotations = {90, 180, 270};
            for (int rotation : rotations) {
                result = scanSingleOrientation(bitmap, rotation);
                if (result != null) return result;
            }

            return null;
        }

        private Result scanSingleOrientation(Bitmap bitmap, int rotation) {
            try {
                Bitmap rotatedBitmap = rotateBitmap(bitmap, rotation);
                if (rotatedBitmap == null) return null;

                int width = rotatedBitmap.getWidth();
                int height = rotatedBitmap.getHeight();
                int[] pixels = new int[width * height];
                rotatedBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

                LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

                return reader.decodeWithState(binaryBitmap);

            } catch (Exception e) {
                return null; // No barcode found in this orientation
            }
        }
    }

    private void handleBarcodeResult(String barcodeValue, String format) {
        if (!isScanning) return;
        isScanning = false;

        // Validate barcode content
        if (barcodeValue == null || barcodeValue.trim().isEmpty()) {
            Log.e("BarcodeScanner", "Empty barcode detected");
            isScanning = true; // Resume scanning
            return;
        }

        // Clean the barcode value
        String cleanBarcode = barcodeValue.trim();

        Log.d("BarcodeScanner", "Barcode detected: " + cleanBarcode + " Format: " + format);

        // ✅ ADD THIS LINE: Save to history before navigating
        saveToHistory(cleanBarcode, format);

        // Vibrate on detection
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(100);
        }

        // Navigate to Results Activity
        Intent intent = new Intent(this, ScanResultActivity.class);
        intent.putExtra("SCANNED_BARCODE", cleanBarcode);
        intent.putExtra("BARCODE_FORMAT", format);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isScanning = false;
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}