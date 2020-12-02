package com.nemesiss.dev.piaprobox.Util

import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Cookie {
    var name: String? = null
    var value: String? = null
    var path: String? = null
    var expires: Date? = null
    var domain: String? = null
    var isHttpOnly: Boolean = false
    var maxAge: Int? = null
    var isSecure: Boolean = false
    var sameSite: String? = null
}

private val cookieExpiresDateFormatter = SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz", Locale.ROOT)


fun parseCookies(setCookieDirectives: List<String>): ArrayList<Cookie> {
    val result = ArrayList<Cookie>()
    setCookieDirectives.forEach { cookieSetString ->
        val cookie = Cookie()
        // 首先，按照分号隔开，然后去掉头尾的空格
        val entries = cookieSetString.split(";").map { textNeedTrim -> textNeedTrim.trim() }
        // 对于每个entry，再按等号分隔开，等号左边是key, 右边是value
        entries.forEach {
                entry ->
            val kv = entry.split("=")
            if (kv.size >= 2) {
                val key = kv[0].toLowerCase(Locale.getDefault())
                val value = kv[1]
                when (key) {
                    "path" -> cookie.path = value
                    "expires" -> cookie.expires = cookieExpiresDateFormatter.parse(value)
                    "domain" -> cookie.domain = value
                    "max-age" -> cookie.maxAge = value.toIntOrNull()
                    "same-site" -> cookie.sameSite = value
                    else -> {
                        if (cookie.name == null) {
                            // use kv[0] to keep char case.
                            cookie.name = kv[0]
                            cookie.value = value
                        }
                    }
                }
            } else if (kv.size == 1) {
                val key = kv[0].toLowerCase(Locale.getDefault())
                when (key) {
                    "httponly" -> cookie.isHttpOnly = true
                    "secure" -> cookie.isSecure = true
                }
            }
        }
        result.add(cookie)
    }
    return result
}

fun Response.serverSetCookies(): ArrayList<Cookie> {
    var cookiesShouldSet = headers("Set-Cookie")
    if (cookiesShouldSet.isEmpty()) {
        // try 'set-cookie'
        cookiesShouldSet = headers("Set-Cookie".toLowerCase(Locale.getDefault()))
    }
    return parseCookies(cookiesShouldSet)
}

