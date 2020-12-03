package com.nemesiss.dev.piaprobox

import com.nemesiss.dev.piaprobox.Util.Cookie
import com.nemesiss.dev.piaprobox.Util.CookieUtil.parseCookies
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assert.assertEquals as asEquals

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CookieParserTest {
    @Test
    fun testParseMultipartCookie() {
        val complexCookies = "piapro_r=UGlhcH3D; expires=Tue, 02-Mar-2021 14:34:11 GMT; path=/; httponly"
        val cookies = parseCookies(listOf(complexCookies))
        asEquals(1, cookies.size)
        val cookie = cookies[0]
        asEquals("UGlhcH3D", cookie.value)
        asEquals("piapro_r", cookie.name)
        asEquals("/", cookie.path)
        assertTrue(cookie.isHttpOnly)
        assertFalse(cookie.isSecure)
    }

    @Test
    fun testParseSimpleCookie() {
        val simpleCookie = "piapro_s=v056lasov59o2h2kn4s7hdsdm1; path=/"
        val cookie = parseCookies(listOf(simpleCookie))[0]
        asEquals("piapro_s", cookie.name)
        asEquals("v056lasov59o2h2kn4s7hdsdm1", cookie.value)
        assertFalse(cookie.isHttpOnly)
        assertFalse(cookie.isSecure)
        asEquals("/", cookie.path)
    }

}
