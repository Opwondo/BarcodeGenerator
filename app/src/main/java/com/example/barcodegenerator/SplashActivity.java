package com.example.barcodegenerator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.barcodegenerator.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // We'll add the navigation logic here later
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                // For now, this will just stay on the splash screen

                // TODO: Later we'll add decision logic here:
                // - Check if user is logged in
                // - Navigate to LoginActivity OR MainActivity
            }
        }, 2000);
    }
}