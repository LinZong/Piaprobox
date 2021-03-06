package com.nemesiss.dev.piaprobox.Service.DaggerFactory;

import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity;
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity2;
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity;
import com.nemesiss.dev.piaprobox.Fragment.ImageViewer.IllustratorViewFragment;
import com.nemesiss.dev.piaprobox.Service.DaggerModules.DownloadServiceModules;
import com.nemesiss.dev.piaprobox.Service.Download.DownloadService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {DownloadServiceModules.class})
public interface DownloadServiceFactory {

    DownloadService instance();

    void inject(MusicPlayerActivity mpa);

    void inject(IllustratorViewActivity2 illustratorViewActivity);

    void inject(IllustratorViewActivity illustratorViewActivity);

    void inject(IllustratorViewFragment illustratorViewFragment);
}
