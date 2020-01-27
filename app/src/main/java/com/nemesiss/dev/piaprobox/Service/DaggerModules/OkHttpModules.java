package com.nemesiss.dev.piaprobox.Service.DaggerModules;


import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

@Module
public interface OkHttpModules {
    @Provides
    static OkHttpClient http() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5,  TimeUnit.SECONDS)
                .build();
    }
}
