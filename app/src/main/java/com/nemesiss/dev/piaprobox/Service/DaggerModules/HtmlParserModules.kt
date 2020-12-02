package com.nemesiss.dev.piaprobox.Service.DaggerModules

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier


@Module
class HtmlParserModules(private val context : Context) {
    @Provides
    fun context() = context
}


