package com.example.getgo.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {
    // Shows a short concise message
    public static void showShort(Context ctx, String message) {
        if (ctx == null) return;
        Toast.makeText(ctx, message != null ? message : "", Toast.LENGTH_SHORT).show();
    }

    // Shows a concise error message. If reason is provided, append short reason (max 80 chars).
    public static void showError(Context ctx, String baseMessage, String reason) {
        if (ctx == null) return;
        String msg = baseMessage != null ? baseMessage : "Failed";
        if (reason != null && !reason.isEmpty()) {
            String r = reason.replaceAll("\n", " ");
            if (r.length() > 80) r = r.substring(0, 77) + "...";
            msg = msg + ": " + r;
        }
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }
}

