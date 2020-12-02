package com.nemesiss.dev.piaprobox.Service.DaggerFactory

import android.content.Context
import com.nemesiss.dev.piaprobox.Activity.TestSkeletonActivity
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HtmlParserModules
import com.nemesiss.dev.piaprobox.Service.DaggerModules.OkHttpModules
import com.nemesiss.dev.piaprobox.Service.DaggerModules.UserLoginServiceModules
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [UserLoginServiceModules::class, OkHttpModules::class, HtmlParserModules::class])
interface UserLoginServiceFactory {
    fun inject(activity: TestSkeletonActivity)
}

/*
    半自动Dagger进行Multi-binding的的使用方式

    1. 声明接口
    2. 声明一个或多个实现类，需要注入东西就@Inject constructor(val xxx: YYY)。注意不能private, 否则Inject不了。
    3. 写Modules以及Binds, Qualifier注解，告知Dagger不同的Qualifier应该如何对应接口与实现的关系。可以参考UserLoginServiceModules。
        注意，在这个地方还不需要写Scope（Singleton之类的，而Hilt就不一样，Hilt要写。

       写Modules的时候，如果当前实例依赖一些别的对象来构造，而这些对象Dagger并不知道怎么产生的时候（例如，依赖一些需要外部给的变量，如context）
       这种情况下就要在Modules里写构造函数然后通过Provider给出去了。参考Context的注入方式。

    4. 声明Factory，就像这个一样，要写明Scope（Singleton），如果这个实力有别的依赖，例如产生实例需要依赖别的Inject过的东西，就需要一同声明
        Component注解，注解中给到的Modules要覆盖到全部的依赖。如果需要inject到Activity，还需要写上面那样的inject函数。
    5. 收工，检查是否注入正常，
*/