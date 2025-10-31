package com.example.barcodegenerator;

import android.content.Context;
import android.util.Patterns;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthHelper {
    private DatabaseHelper databaseHelper;

    public AuthHelper(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // Validate email format
    public boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Validate password strength
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Simple password hashing (for demo purposes)
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback to plain text (not recommended for production)
        }
    }

    // Register new user
    public boolean registerUser(String email, String password, String fullName) {
        if (!isValidEmail(email) || !isValidPassword(password)) {
            return false;
        }

        if (databaseHelper.checkUserExists(email)) {
            return false; // User already exists
        }

        User user = new User(email, hashPassword(password), fullName);
        long result = databaseHelper.registerUser(user);
        return result != -1;
    }

    // Login user
    public User loginUser(String email, String password) {
        if (!isValidEmail(email) || !isValidPassword(password)) {
            return null;
        }

        String hashedPassword = hashPassword(password);
        return databaseHelper.loginUser(email, hashedPassword);
    }

    // Reset password
    public boolean resetPassword(String email, String newPassword) {
        if (!isValidEmail(email) || !isValidPassword(newPassword)) {
            return false;
        }

        if (!databaseHelper.checkUserExists(email)) {
            return false; // User doesn't exist
        }

        String hashedPassword = hashPassword(newPassword);
        return databaseHelper.updatePassword(email, hashedPassword);
    }
}