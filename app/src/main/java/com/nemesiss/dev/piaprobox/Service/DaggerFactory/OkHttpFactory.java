package com.nemesiss.dev.piaprobox.Service.DaggerFactory;

import com.nemesiss.dev.piaprobox.Service.DaggerModules.OkHttpModules;
import dagger.Component;
import okhttp3.OkHttpClient;


@Component(modules = {OkHttpModules.class})
public interface OkHttpFactory {

    OkHttpClient http();
}
