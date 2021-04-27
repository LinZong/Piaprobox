package com.nemesiss.dev.piaprobox.Service.DaggerFactory;

import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity;
import com.nemesiss.dev.piaprobox.Service.DaggerModules.ImageCacheModules;
import dagger.Component;
import dagger.Provides;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ImageCacheModules.class})
public interface ImageCacheFactory {

    void inject(MusicPlayerActivity activity);

}
