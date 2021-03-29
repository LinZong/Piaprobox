package com.nemesiss.dev.piaprobox.Service.DaggerModules

import android.content.Context
import dagger.Module
import dagger.Provides


@Module
class HtmlParserModules(private val context : Context) {
    @Provides
    fun context() = context
}


