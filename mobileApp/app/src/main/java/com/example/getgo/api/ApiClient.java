package com.example.getgo.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.getgo.api.services.AuthApiService;
import com.example.getgo.api.services.DriverApiService;
import com.example.getgo.api.services.PanicApiService;
import com.example.getgo.api.services.UserApiService;
import com.example.getgo.utils.LocalDateTimeDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;
    private static String currentBaseUrl;
    private static Context appContext;

    // private static final String DEFAULT_BASE_URL = "http://10.0.2.2:8080/";
    public static final String SERVER_URL = "http://10.0.2.2:8080"; // For fetching images
    private static final String PREFS_NAME = "getgo_prefs";
    private static final String PREF_JWT = "jwt_token";

    // public static final String API_BASE_URL = "http://10.0.2.2:8080/";
    public static final String DEFAULT_BASE_URL = "https://nonpossibly-nonderivable-teddy.ngrok-free.dev/";

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        return getClient(DEFAULT_BASE_URL);
    }

    public static synchronized Retrofit getClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = DEFAULT_BASE_URL;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        if (retrofit == null || currentBaseUrl == null || !currentBaseUrl.equals(baseUrl)) {

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                    .registerTypeAdapter(LocalDateTime.class,
                            (JsonDeserializer<LocalDateTime>) (json, type, context) ->
                                    LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .registerTypeAdapter(LocalDateTime.class,
                            (JsonSerializer<LocalDateTime>) (src, type, context) ->
                                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(createLoggingInterceptor())
                    .addInterceptor(createAuthInterceptor())
                    .connectTimeout(25, TimeUnit.SECONDS)
                    .readTimeout(25, TimeUnit.SECONDS)
                    .writeTimeout(25, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            currentBaseUrl = baseUrl;
        }

        return retrofit;
    }

    private static HttpLoggingInterceptor createLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    private static okhttp3.Interceptor createAuthInterceptor() {
        return chain -> {
            Request original = chain.request();

            // Get JWT token from SharedPreferences
            String token = getJwtToken();

            // Add Authorization header if token exists
            Request.Builder requestBuilder = original.newBuilder();
            if (token != null && !token.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            return chain.proceed(requestBuilder.build());
        };
    }

    private static String getJwtToken() {
        if (appContext == null) {
            return null;
        }
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_JWT, null);
    }

    public static synchronized void setDefaultBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) return;
        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";
        // force rebuild of Retrofit on next getClient() call
        currentBaseUrl = baseUrl;
        retrofit = null;
    }

    private static AuthApiService authApiService;
    private static DriverApiService driverApiService;
    private static UserApiService userApiService;
    private static PanicApiService panicApiService;

    public static AuthApiService getAuthApiService() {
        if (authApiService == null) {
            authApiService = getClient().create(AuthApiService.class);
        }
        return authApiService;
    }

    public static DriverApiService getDriverApiService() {
        if (driverApiService == null) {
            driverApiService = getClient().create(DriverApiService.class);
        }
        return driverApiService;
    }

    public static UserApiService getUserApiService() {
        if (userApiService == null) {
            userApiService = getClient().create(UserApiService.class);
        }
        return userApiService;
    }

    public static PanicApiService getPanicApiService() {
        if (panicApiService == null) {
            panicApiService = getClient().create(PanicApiService.class);
        }
        return panicApiService;
    }
}