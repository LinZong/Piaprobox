package com.nemesiss.dev.piaprobox.Service.DaggerModules

import com.nemesiss.dev.piaprobox.Service.User.CookieUserLoginServiceImpl
import com.nemesiss.dev.piaprobox.Service.User.UserLoginService
import dagger.Binds
import dagger.Module
import javax.inject.Qualifier

@Module
abstract class UserLoginServiceModules {

    @Binds
    @CookieLoginService
    abstract fun getCookieUserLoginService(cookieUserLoginServiceImpl: CookieUserLoginServiceImpl): UserLoginService

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CookieLoginService
