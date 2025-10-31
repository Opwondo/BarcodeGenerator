package com.example.barcodegenerator;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText, newPasswordEditText, confirmPasswordEditText;
    private Button resetButton;
    private TextView loginText;
    private AuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        emailEditText = findViewById(R.id.email_editText);
        newPasswordEditText = findViewById(R.id.new_password_editText);
        confirmPasswordEditText = findViewById(R.id.confirm_password_editText);
        resetButton = findViewById(R.id.reset_button);
        loginText = findViewById(R.id.login_textView);

        // Initialize AuthHelper
        authHelper = new AuthHelper(this);

        // Reset button click listener
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPasswordReset();
            }
        });

        // Login text click listener
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void attemptPasswordReset() {
        String email = emailEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("New password is required");
            newPasswordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Please confirm your password");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!authHelper.isValidEmail(email)) {
            emailEditText.setError("Enter a valid email address");
            emailEditText.requestFocus();
            return;
        }

        if (!authHelper.isValidPassword(newPassword)) {
            newPasswordEditText.setError("Password must be at least 6 characters");
            newPasswordEditText.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        // Attempt password reset
        boolean success = authHelper.resetPassword(email, newPassword);

        if (success) {
            Toast.makeText(ForgotPasswordActivity.this, "Password reset successfully!", Toast.LENGTH_LONG).show();

            // Go back to login after a short delay
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        } else {
            Toast.makeText(ForgotPasswordActivity.this, "Password reset failed. Email may not exist.", Toast.LENGTH_SHORT).show();
        }
    }
}