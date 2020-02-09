package com.nemesiss.dev.piaprobox.Service.DaggerFactory;

import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity;
import com.nemesiss.dev.piaprobox.Fragment.Recommend.Categories.BaseRecommendCategoryFragment;
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HTMLParserModules;
import com.nemesiss.dev.piaprobox.Service.HTMLParser;
import dagger.Component;

@Component(modules = {HTMLParserModules.class})
public interface HTMParserFactory {

    HTMLParser parser();

    void inject(IllustratorViewActivity illustratorViewActivity);
    void inject(BaseRecommendCategoryFragment baseRecommendCategoryFragment);

}
