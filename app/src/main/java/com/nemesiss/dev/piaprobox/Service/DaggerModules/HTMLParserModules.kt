package com.nemesiss.dev.piaprobox.Service.DaggerModules

import dagger.Module
import javax.inject.Qualifier


@Module
abstract class HTMLParserModules {


    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class HTMLParserConfig
}


