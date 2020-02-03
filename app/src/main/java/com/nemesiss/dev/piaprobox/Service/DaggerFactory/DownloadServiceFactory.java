package com.nemesiss.dev.piaprobox.Service.DaggerFactory;

import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity;
import com.nemesiss.dev.piaprobox.Activity.TestSkeletonActivity;
import com.nemesiss.dev.piaprobox.Service.DaggerModules.DownloadServiceModules;
import com.nemesiss.dev.piaprobox.Service.Download.DownloadService;
import dagger.Component;

@Component(modules = {DownloadServiceModules.class})
public interface DownloadServiceFactory {

    DownloadService ds();

    void inject(TestSkeletonActivity ba);
    void inject(MusicPlayerActivity mpa);
}
