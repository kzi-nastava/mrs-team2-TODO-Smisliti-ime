package com.example.getgo.auth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.interfaces.AuthApiService;
import com.example.getgo.dtos.user.CreateLoginDTO;
import com.example.getgo.dtos.user.ForgotPasswordDTO;
import com.example.getgo.dtos.user.ResetPasswordDTO;
import com.example.getgo.model.UserRole;

import org.json.JSONObject;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private static AuthRepository instance;
    private final AuthDatabaseHelper dbHelper;
    private final Context appContext;

    private AuthRepository(Context ctx) {
        this.appContext = ctx.getApplicationContext();
        dbHelper = new AuthDatabaseHelper(appContext);
    }

    public static synchronized AuthRepository getInstance(Context ctx) {
        if (instance == null) instance = new AuthRepository(ctx);
        return instance;
    }

    // returns role if authenticated and active, otherwise null
    public UserRole authenticateUser(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT role, active, password FROM users WHERE email = ?", new String[]{email});
        try {
            if (c.moveToFirst()) {
                String dbPass = c.getString(2);
                int active = c.getInt(1);
                String role = c.getString(0);
                if (dbPass != null && dbPass.equals(password) && active == 1) {
                    try {
                        return UserRole.valueOf(role);
                    } catch (Exception ex) {
                        return UserRole.PASSENGER;
                    }
                }
            }
        } finally {
            c.close();
        }
        return null;
    }

    // helper to activate account (could be called by link handler)
    public boolean activateAccount(String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("active", 1);
        int updated = db.update("users", cv, "email = ?", new String[]{email});
        return updated > 0;
    }

    /**
     * Register user via backend /api/auth/register endpoint using AuthApiService interface.
     * Returns null on success, error message string on failure.
     */
    public String registerUser(String email, String password, String firstName, String lastName,
                                String address, String phone, String roleString, String avatarUri) {
        try {
            AuthApiService service = ApiClient.getAuthApiService();

            // Build text parts
            Map<String, RequestBody> fields = new HashMap<>();
            fields.put("email", RequestBody.create(MediaType.parse("text/plain"), email));
            fields.put("password", RequestBody.create(MediaType.parse("text/plain"), password));
            fields.put("name", RequestBody.create(MediaType.parse("text/plain"), firstName));
            fields.put("surname", RequestBody.create(MediaType.parse("text/plain"), lastName));
            fields.put("address", RequestBody.create(MediaType.parse("text/plain"), address));
            fields.put("phone", RequestBody.create(MediaType.parse("text/plain"), phone));

            // Build file part (optional)
            MultipartBody.Part filePart = null;
            if (avatarUri != null && !avatarUri.isEmpty()) {
                try {
                    Uri uri = Uri.parse(avatarUri);
                    InputStream is = appContext.getContentResolver().openInputStream(uri);
                    if (is != null) {
                        byte[] bytes = new byte[is.available()];
                        is.read(bytes);
                        is.close();
                        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), bytes);
                        filePart = MultipartBody.Part.createFormData("file", "avatar.jpg", fileBody);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to read avatar file: " + ex.getMessage());
                }
            }

            // Call via interface
            Response<ResponseBody> response = service.register(fields, filePart).execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Registration successful for: " + email);
                return null;
            } else {
                String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Registration failed: " + response.code() + " " + errBody);
                return "Server error: " + response.code() + " - " + errBody;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Register exception: " + ex.getMessage(), ex);
            return "Network error: " + ex.getMessage();
        }
    }

    /**
     * Login user via backend /api/auth/login endpoint using AuthApiService interface.
     * Returns LoginResult with role/token on success, or error message on failure.
     */
    public LoginResult loginBackend(String email, String password) {
        try {
            AuthApiService service = ApiClient.getAuthApiService();
            CreateLoginDTO dto = new CreateLoginDTO(email, password);

            Response<ResponseBody> response = service.login(dto).execute();

            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                JSONObject obj = new JSONObject(json);
                String role = obj.optString("role", "PASSENGER");
                String token = obj.optString("jwt", "");
                Long userId = obj.optLong("id", -1L);

                Log.d(TAG, "Login successful: " + email + " role=" + role);
                return new LoginResult(role, token, userId, null);
            } else {
                String errBody = response.errorBody() != null ? response.errorBody().string() : "Login failed";
                Log.e(TAG, "Login failed: " + response.code() + " " + errBody);
                return new LoginResult(null, null, null, "Login failed: " + errBody);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Login exception: " + ex.getMessage(), ex);
            return new LoginResult(null, null, null, "Network error: " + ex.getMessage());
        }
    }

    /**
     * Send forgot-password request via backend /api/auth/forgot-password endpoint.
     * Returns null on success, error string on failure.
     */
    public String forgotPassword(String email) {
        try {
            AuthApiService service = ApiClient.getAuthApiService();
            ForgotPasswordDTO dto = new ForgotPasswordDTO(email);

            Response<ResponseBody> response = service.forgotPassword(dto).execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Forgot password email sent to: " + email);
                return null;
            } else {
                String errBody = response.errorBody() != null ? response.errorBody().string() : "Request failed";
                Log.e(TAG, "Forgot password failed: " + response.code() + " " + errBody);
                return "Server error: " + errBody;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Forgot password exception: " + ex.getMessage(), ex);
            return "Network error: " + ex.getMessage();
        }
    }

    /**
     * Reset password via backend /api/auth/reset-password endpoint.
     * Returns null on success, error string on failure.
     */
    public String resetPassword(String token, String newPassword) {
        try {
            AuthApiService service = ApiClient.getAuthApiService();
            ResetPasswordDTO dto = new ResetPasswordDTO(token, newPassword);

            Response<ResponseBody> response = service.resetPassword(dto).execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Password reset successful for token: " + token);
                return null;
            } else {
                String errBody = response.errorBody() != null ? response.errorBody().string() : "Reset failed";
                Log.e(TAG, "Reset password failed: " + response.code() + " " + errBody);
                return "Server error: " + errBody;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Reset password exception: " + ex.getMessage(), ex);
            return "Network error: " + ex.getMessage();
        }
    }

    /**
     * Activate account via backend /api/auth/activate-mobile endpoint.
     * Returns null on success, error string on failure.
     */
    public String activateByToken(String token) {
        try {
            AuthApiService service = ApiClient.getAuthApiService();

            Response<ResponseBody> response = service.activateMobile(token).execute();

            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                JSONObject obj = new JSONObject(json);
                boolean activated = obj.optBoolean("activated", false);
                if (activated) {
                    Log.d(TAG, "Account activated via token: " + token);
                    return null;
                } else {
                    String msg = obj.optString("message", "Activation failed");
                    Log.e(TAG, "Activation returned false: " + msg);
                    return msg;
                }
            } else {
                String errBody = response.errorBody() != null ? response.errorBody().string() : "Activation failed";
                Log.e(TAG, "Activation failed: " + response.code() + " " + errBody);
                return "Server error: " + errBody;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Activation exception: " + ex.getMessage(), ex);
            return "Network error: " + ex.getMessage();
        }
    }

    // result wrapper returned to UI layer
    public static class LoginResult {
        public final String role;
        public final String token;
        public final Long userId;
        public final String error;

        public LoginResult(String role, String token, Long userId, String error) {
            this.role = role;
            this.token = token;
            this.userId = userId;
            this.error = error;
        }
    }
}
