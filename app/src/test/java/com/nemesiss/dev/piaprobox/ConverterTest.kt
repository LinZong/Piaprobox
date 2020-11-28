package com.nemesiss.dev.piaprobox

import com.alibaba.fastjson.JSON
import com.nemesiss.dev.piaprobox.Model.User.LoginCredentials
import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ConverterTest {
    @Test
    fun LoginCredentialsToJsonCorrect() {
        val sampleCredentials = LoginCredentials("TestUser", "123456")
        val serializeJson = JSON.toJSONString(sampleCredentials)
        println(serializeJson)
        val deserializeCredentials = JSON.parseObject(serializeJson)
        Assert.assertTrue(deserializeCredentials.containsKey("_username"))
        Assert.assertTrue(deserializeCredentials.containsKey("_password"))
    }

    @Test
    fun JsonToLoginCredentialsCorrect() {
        val json = "{\"_username\":\"测试用户\",\"_password\":\"测试用户密码\"}"
        var deserializeCredentials = JSON.parseObject(json, LoginCredentials::class.java)
        deserializeCredentials.apply {
            Assert.assertEquals("测试用户", UserName)
            Assert.assertEquals("测试用户密码", Password)
        }
    }
}
