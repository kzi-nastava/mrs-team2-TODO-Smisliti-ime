package com.example.getgo;

import android.app.Application;
import com.example.getgo.api.ApiClient;

public class GetGoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);
    }
}