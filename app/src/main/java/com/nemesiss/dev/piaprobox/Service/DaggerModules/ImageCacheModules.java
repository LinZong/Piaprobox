package com.nemesiss.dev.piaprobox.Service.DaggerModules;

import android.content.Context;
import dagger.Module;
import dagger.Provides;

@Module
public class ImageCacheModules {

    private Context context;

    public ImageCacheModules(Context context) {
        this.context = context;
    }

    @Provides
    public Context getContext() {
        return context;
    }
}
