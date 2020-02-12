package com.nemesiss.dev.piaprobox.Service.DaggerFactory;

import com.nemesiss.dev.piaprobox.Activity.Common.SplashActivity;
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity;
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity2;
import com.nemesiss.dev.piaprobox.Fragment.Recommend.Categories.RecommendImageCategoryFragment;
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HTMLParserModules;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {HTMLParserModules.class})
public interface AsyncExecutorFactory {

    void inject(SplashActivity splashActivity);
    void inject(IllustratorViewActivity illustratorViewActivity);
    void inject(IllustratorViewActivity2 illustratorViewActivity);
    void inject(RecommendImageCategoryFragment recommendImageCategoryFragment);
}

