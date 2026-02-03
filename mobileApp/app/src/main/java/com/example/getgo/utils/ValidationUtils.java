package com.example.getgo.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Email pattern matching backend: ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Serbian phone pattern matching backend: ^(\+3816[0-9]|06[0-9])[0-9]{6,7}$
    // Examples: +381612345678 or 0612345678
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+3816[0-9]|06[0-9])[0-9]{6,7}$"
    );

    /**
     * Validates email format.
     * Backend constraint: @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates password length.
     * Backend constraint: @Size(min = 8, message = "Password must be at least 8 characters long")
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    /**
     * Validates Serbian phone number format.
     * Backend constraint: @Pattern(regexp = "^(\\+3816[0-9]|06[0-9])[0-9]{6,7}$")
     * Accepts formats like +381612345678 or 0612345678
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && !phone.trim().isEmpty() && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates name/surname length.
     * Backend constraint: @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
     */
    public static boolean isValidName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 50;
    }

    /**
     * Validates that a field is not blank (not null, not empty, not whitespace-only).
     * Backend constraint: @NotBlank
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

