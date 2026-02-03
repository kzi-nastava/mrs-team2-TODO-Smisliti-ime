package com.example.getgo.api;

import com.example.getgo.interfaces.AuthApiService;
import com.example.getgo.helpers.LocalDateTimeDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static Retrofit retrofit;
    private static String currentBaseUrl;

    /*private static final String DEFAULT_BASE_URL = "http://10.0.2.2:8000/";*/
    private static final String DEFAULT_BASE_URL = "https://nonpossibly-nonderivable-teddy.ngrok-free.dev/";

    public static Retrofit getClient() {
        return getClient(DEFAULT_BASE_URL);
    }

    // New: allow creating/returning a client for a custom backend URL (useful for physical device)
    public static synchronized Retrofit getClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) baseUrl = DEFAULT_BASE_URL;
        // ensure baseUrl ends with '/'
        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";

        if (retrofit == null || currentBaseUrl == null || !currentBaseUrl.equals(baseUrl)) {

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(25, TimeUnit.SECONDS)
                    .readTimeout(25, TimeUnit.SECONDS)
                    .writeTimeout(25, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl) // use provided baseUrl
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            currentBaseUrl = baseUrl;
        }

        return retrofit;
    }

    // New: allow overriding base URL at runtime (call before making network calls)
    public static synchronized void setDefaultBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) return;
        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";
        // force rebuild of Retrofit on next getClient() call
        currentBaseUrl = baseUrl;
        retrofit = null;
    }

    // New: provide the AuthApiService instance from the interfaces folder
    private static AuthApiService authApiService;

    /**
     * Returns the AuthApiService instance for making auth-related API calls.
     * All auth endpoints are defined in the AuthApiService interface.
     */
    public static AuthApiService getAuthApiService() {
        if (authApiService == null) {
            authApiService = getClient().create(AuthApiService.class);
        }
        return authApiService;
    }
}
