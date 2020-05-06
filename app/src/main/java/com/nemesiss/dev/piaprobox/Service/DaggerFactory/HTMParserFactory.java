package com.nemesiss.dev.piaprobox.Service.DaggerFactory;

import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity;
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity2;
import com.nemesiss.dev.piaprobox.Activity.Text.TextDetailActivity;
import com.nemesiss.dev.piaprobox.Fragment.ImageViewer.IllustratorViewFragment;
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories.BaseRecommendCategoryFragment;
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HTMLParserModules;
import com.nemesiss.dev.piaprobox.Service.HTMLParser;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {HTMLParserModules.class})
public interface HTMParserFactory {

    HTMLParser parser();

    void inject(IllustratorViewActivity2 illustratorViewActivity);

    void inject(IllustratorViewFragment illustratorViewFragment);

    void inject(IllustratorViewActivity illustratorViewActivity);

    void inject(BaseRecommendCategoryFragment baseRecommendCategoryFragment);

    void inject(TextDetailActivity textDetailActivity);
}
