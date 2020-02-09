package com.nemesiss.dev.piaprobox.Service.DaggerModules

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier


@Module
class HTMLParserModules(val context : Context) {

    @Provides
    fun getCtx() = context
}


