package com.example.getgo.auth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.example.getgo.api.ApiClient;
import com.example.getgo.model.UserRole;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public class AuthRepository {
    private static AuthRepository instance;
    private final AuthDatabaseHelper dbHelper;
    private final Context appContext;

    // backend register endpoint — change if using physical device to machine IP
    private static final String BACKEND_REGISTER_URL = "http://10.0.2.2:8080/api/auth/register";

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

    // Retrofit interface for register endpoint (no new file)
    private interface AuthApi {
        @Multipart
        @POST("api/auth/register")
        Call<ResponseBody> register(@PartMap Map<String, RequestBody> partMap,
                                    @Part MultipartBody.Part file);
    }

    // register user by forwarding data to backend /api/auth/register (multipart/form-data)
    // Returns null on success, or an error message (body or HTTP code) on failure.
    public String registerUser(String email, String password, String firstName, String lastName,
                                String address, String phone, String roleString, String avatarUriString) {

        try {
            AuthApi api = ApiClient.getClient().create(AuthApi.class);

            // prepare text parts
            Map<String, RequestBody> partMap = new HashMap<>();
            MediaType textType = MediaType.parse("text/plain; charset=utf-8");
            partMap.put("email", RequestBody.create(textType, email != null ? email : ""));
            partMap.put("password", RequestBody.create(textType, password != null ? password : ""));
            partMap.put("name", RequestBody.create(textType, firstName != null ? firstName : ""));
            partMap.put("surname", RequestBody.create(textType, lastName != null ? lastName : ""));
            partMap.put("address", RequestBody.create(textType, address != null ? address : ""));
            partMap.put("phone", RequestBody.create(textType, phone != null ? phone : ""));
            partMap.put("role", RequestBody.create(textType, roleString != null ? roleString : "PASSENGER"));

            MultipartBody.Part filePart = null;

            if (avatarUriString != null && !avatarUriString.isEmpty()) {
                try {
                    Uri avatarUri = Uri.parse(avatarUriString);
                    InputStream is = appContext.getContentResolver().openInputStream(avatarUri);
                    if (is != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, read);
                        }
                        is.close();
                        byte[] fileBytes = baos.toByteArray();

                        String mime = appContext.getContentResolver().getType(avatarUri);
                        if (mime == null) {
                            String ext = MimeTypeMap.getFileExtensionFromUrl(avatarUri.toString());
                            mime = ext != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) : "image/jpeg";
                        }
                        RequestBody reqFile = RequestBody.create(MediaType.parse(mime), fileBytes);
                        String filename = "avatar.jpg";
                        filePart = MultipartBody.Part.createFormData("file", filename, reqFile);
                    }
                } catch (Exception e) {
                    // file attach failed; continue without file
                    e.printStackTrace();
                }
            }

            Call<ResponseBody> call = api.register(partMap, filePart);
            Response<ResponseBody> resp = call.execute();

            int code = resp.code();
            String bodyString = "";
            if (resp.isSuccessful() && resp.body() != null) {
                // read body if present
                try {
                    bodyString = resp.body().string();
                } catch (Exception ignored) {}
            } else {
                // read error body if present
                ResponseBody err = resp.errorBody();
                if (err != null) {
                    try {
                        bodyString = err.string();
                    } catch (Exception ignored) {}
                }
            }

            if (resp.isSuccessful()) {
                return null; // success
            } else {
                if (bodyString != null && !bodyString.isEmpty()) {
                    return "Server error (" + code + "): " + bodyString;
                } else {
                    return "Server error: HTTP " + code;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Network error: " + ex.getMessage();
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
