package com.nemesiss.dev.piaprobox.Service.DaggerFactory

import com.nemesiss.dev.piaprobox.Activity.Common.SplashActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component
interface AsyncExecutorFactory {
    fun inject(splashActivity: SplashActivity)
}