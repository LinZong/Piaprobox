package com.nemesiss.dev.piaprobox.Service.DaggerModules

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier


@Module
class HtmlParserModules(val context : Context) {

    @Provides
    fun getCtx() = context
}


