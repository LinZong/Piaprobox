package com.nemesiss.dev.piaprobox.Service.DaggerModules;

import android.content.Context;
import dagger.Module;
import dagger.Provides;

@Module
public class DownloadServiceModules {

    private final Context context;

    public DownloadServiceModules(Context context) {
        this.context = context;
    }

    @Provides
    public Context context() {
        return this.context;
    }
}
