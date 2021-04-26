package com.nemesiss.dev.piaprobox.Service

import android.content.Context
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.nemesiss.dev.contentparser.ContentParserFactory
import com.nemesiss.dev.contentparser.steps.ContentParserImpl
import com.nemesiss.dev.piaprobox.model.HTMLParser.RuleVersion
import com.nemesiss.dev.piaprobox.model.resources.Constants
import com.nemesiss.dev.piaprobox.Util.AppUtil
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject


class HTMLParser @Inject constructor(val context: Context) {
    companion object {

        const val LOG_TAG = "HTMLParser"

        const val MAIN_DOMAIN = Constants.Url.MAIN_DOMAIN

        @JvmStatic
        fun ProvideRuleJsonFileHandle(): File {
            val cachePath = AppUtil.GetAppCachePath()
            return File(cachePath).resolve("ContentParserRule.json")
        }

        /**
         * 图片Url都需要用这个方法处理下，带上前缀。
         */
        @JvmStatic
        fun GetAlbumThumb(ValueFromParser: String): String {
            return if (ValueFromParser.startsWith("http") || ValueFromParser.startsWith("https")) {
                ValueFromParser
            } else if (ValueFromParser.contains("cdn")) {
                "http:${ValueFromParser}"
            } else {
                wrapDomain(ValueFromParser)
            }
        }

        @JvmStatic
        fun wrapDomain(urlForWrap: String): String {
            return if (urlForWrap.startsWith("http")) {
                urlForWrap
            } else {
                MAIN_DOMAIN + urlForWrap
            }
        }
    }

    var Rules: JSONObject private set

    val version: RuleVersion

    val Parser: ContentParserImpl

    init {
        val configText: String
        val cachedRuleFileHandle = ProvideRuleJsonFileHandle()
        configText = if (cachedRuleFileHandle.exists()) {
            cachedRuleFileHandle.readText()
        } else {
            BufferedReader(InputStreamReader(context.assets.open("ContentParserRule.json"))).readText()
        }
        Rules = JSONObject.parseObject(configText)
        version = RuleVersion(Rules.getString("version"))
        Parser = ContentParserFactory.Provide()

        Log.d(LOG_TAG, "HTML Parser Loaded! Description here: ↓")
        Parser.Description()
    }
}