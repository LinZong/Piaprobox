package com.nemesiss.dev.piaprobox.Service.DaggerModules;

import android.content.Context;
import dagger.Module;
import dagger.Provides;

@Module
public class DownloadServiceModules {

    private Context context;

    public DownloadServiceModules(Context context) {
        this.context = context;
    }

    @Provides
    public Context getCtx() {
        return this.context;
    }
}
