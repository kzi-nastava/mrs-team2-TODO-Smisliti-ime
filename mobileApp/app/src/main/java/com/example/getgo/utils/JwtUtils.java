package com.example.getgo.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

public class JwtUtils {
    private static final String TAG = "JwtUtils";

    public static String getEmailFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                Log.e(TAG, "Invalid JWT token format");
                return null;
            }

            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP));

            JSONObject json = new JSONObject(payload);
            return json.getString("sub");

        } catch (Exception e) {
            Log.e(TAG, "Failed to decode JWT token", e);
            return null;
        }
    }
}