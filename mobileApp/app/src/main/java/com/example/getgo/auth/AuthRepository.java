package com.example.getgo.auth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.example.getgo.api.ApiClient;
import com.example.getgo.model.UserRole;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private static AuthRepository instance;
    private final AuthDatabaseHelper dbHelper;
    private final Context appContext;

    private AuthRepository(Context ctx) {
        dbHelper = new AuthDatabaseHelper(ctx.getApplicationContext());
        this.appContext = ctx.getApplicationContext();
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

    // Retrofit interface (no new file)
    private interface AuthApi {
        @Multipart
        @POST("api/auth/register")
        Call<ResponseBody> register(@PartMap Map<String, RequestBody> partMap,
                                    @Part List<MultipartBody.Part> parts);

        @GET("api/auth/activate-mobile")
        Call<ResponseBody> activateMobile(@Query("token") String token);

        @POST("api/auth/login")
        Call<LoginResponse> login(@Body LoginRequest request);
    }

    // new helper DTOs for login
    private static class LoginRequest {
        public String email;
        public String password;
        LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
    private static class LoginResponse {
        public Long id;
        public String role;
        public String jwt;
    }

    // result wrapper returned to UI layer
    public static class LoginResult {
        public final boolean success;
        public final String error; // null if success
        public final String role;
        public final String token;
        public final Long userId;
        public LoginResult(boolean success, String error, String role, String token, Long userId) {
            this.success = success;
            this.error = error;
            this.role = role;
            this.token = token;
            this.userId = userId;
        }
    }

    // New: perform login against backend. Returns LoginResult (network call must be run off UI thread).
    public LoginResult loginBackend(String email, String password) {
        try {
            AuthApi api = ApiClient.getClient().create(AuthApi.class);
            LoginRequest req = new LoginRequest(email, password);
            Call<LoginResponse> call = api.login(req);
            Log.d(TAG, "Executing login call to: " + call.request().url());
            Response<LoginResponse> resp = call.execute();

            if (resp.isSuccessful() && resp.body() != null) {
                LoginResponse body = resp.body();
                String role = body.role != null ? body.role : "PASSENGER";
                String token = body.jwt;
                Long id = body.id;
                Log.d(TAG, "Login successful: role=" + role + " id=" + id);
                return new LoginResult(true, null, role, token, id);
            } else {
                String errBody = "";
                if (resp.errorBody() != null) {
                    try { errBody = resp.errorBody().string(); } catch (Exception ignored) {}
                }
                int code = resp.code();
                String msg = (errBody != null && !errBody.isEmpty()) ? ("Server: " + errBody) : ("HTTP " + code);
                Log.e(TAG, "Login failed (" + code + "): " + errBody);
                return new LoginResult(false, msg, null, null, null);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Login network error", ex);
            return new LoginResult(false, "Network error: " + ex.getMessage(), null, null, null);
        }
    }

    // register user by forwarding data to backend /api/auth/register (multipart/form-data)
    // Returns null on success, or an error message (body or HTTP code) on failure.
    public String registerUser(String email, String password, String firstName, String lastName,
                               String address, String phone, String roleString, String avatarUriString) {

        try {
            Log.d(TAG, "Starting registration request for: " + email);
            AuthApi api = ApiClient.getClient().create(AuthApi.class);

            Map<String, RequestBody> partMap = new HashMap<>();
            MediaType textType = MediaType.parse("text/plain; charset=utf-8");
            partMap.put("email", RequestBody.create(textType, email != null ? email : ""));
            partMap.put("password", RequestBody.create(textType, password != null ? password : ""));
            partMap.put("name", RequestBody.create(textType, firstName != null ? firstName : ""));
            partMap.put("surname", RequestBody.create(textType, lastName != null ? lastName : ""));
            partMap.put("address", RequestBody.create(textType, address != null ? address : ""));
            partMap.put("phone", RequestBody.create(textType, phone != null ? phone : ""));
            // removed: role (backend CreateUserDTO doesn't define it)

            List<MultipartBody.Part> parts = Collections.emptyList();

            if (avatarUriString != null && !avatarUriString.isEmpty()) {
                try {
                    Uri avatarUri = Uri.parse(avatarUriString);
                    InputStream is = appContext.getContentResolver().openInputStream(avatarUri);
                    if (is != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = is.read(buffer)) != -1) baos.write(buffer, 0, read);
                        is.close();

                        byte[] fileBytes = baos.toByteArray();

                        String mime = appContext.getContentResolver().getType(avatarUri);
                        if (mime == null) {
                            String ext = MimeTypeMap.getFileExtensionFromUrl(avatarUri.toString());
                            mime = ext != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) : "image/jpeg";
                        }

                        RequestBody reqFile = RequestBody.create(MediaType.parse(mime), fileBytes);
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "avatar.jpg", reqFile);
                        parts = Collections.singletonList(filePart);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Avatar attach failed; continuing without file", e);
                }
            }

            Call<ResponseBody> call = api.register(partMap, parts);
            Log.d(TAG, "Executing register call to: " + call.request().url());
            Response<ResponseBody> resp = call.execute();

            int code = resp.code();
            String bodyString = "";
            if (resp.isSuccessful() && resp.body() != null) {
                try { bodyString = resp.body().string(); } catch (Exception ignored) {}
                Log.d(TAG, "Registration OK (" + code + "): " + bodyString);
                return null;
            }

            if (resp.errorBody() != null) {
                try { bodyString = resp.errorBody().string(); } catch (Exception ignored) {}
            }
            Log.e(TAG, "Registration failed (" + code + "): " + bodyString);
            return (bodyString != null && !bodyString.isEmpty())
                    ? "Server error (" + code + "): " + bodyString
                    : "Server error: HTTP " + code;

        } catch (Exception ex) {
            Log.e(TAG, "Registration network error", ex);
            return "Network error: " + ex.getMessage();
        }
    }

    // Activation via backend token (email deep link flow). Returns null on success.
    public String activateByToken(String token) {
        try {
            AuthApi api = ApiClient.getClient().create(AuthApi.class);
            Call<ResponseBody> call = api.activateMobile(token);
            Log.d(TAG, "Executing activate-mobile to: " + call.request().url());

            Response<ResponseBody> resp = call.execute();
            int code = resp.code();

            if (resp.isSuccessful()) return null;

            String body = "";
            if (resp.errorBody() != null) {
                try { body = resp.errorBody().string(); } catch (Exception ignored) {}
            }
            return (body != null && !body.isEmpty())
                    ? "Activation failed (" + code + "): " + body
                    : "Activation failed: HTTP " + code;

        } catch (Exception e) {
            Log.e(TAG, "Activation network error", e);
            return "Network error: " + e.getMessage();
        }
    }

    // helper to activate account (could be called by link handler)
    public boolean activateAccount(String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("active", 1);
        int updated = db.update("users", cv, "email = ?", new String[]{email});
        return updated > 0;
    }
}
