package com.nemesiss.dev.piaprobox.Service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.nemesiss.dev.HTMLContentParser.ContentParserFactory
import com.nemesiss.dev.HTMLContentParser.Steps.ContentParserImpl
import com.nemesiss.dev.piaprobox.Model.HTMLParser.RuleVersion
import com.nemesiss.dev.piaprobox.Util.AppUtil
import com.nemesiss.dev.piaprobox.Util.AsPath
import java.io.*
import java.util.*


class HTMLParser(context: Context) {
    companion object {

        @JvmStatic
        val LOGTAG = "HTMLParser"

        @JvmStatic
        fun ProvideRuleJsonFileHandle() : File {
            val cachePath = AppUtil.GetAppCachePath()
            return File(cachePath).resolve("ContentParserRule.json")
        }
    }

    private var Rules : JSONObject

    val version : RuleVersion

    val Parser : ContentParserImpl

    init {
        val configText : String
        val cachedRuleFileHandle = ProvideRuleJsonFileHandle()
        configText = if(cachedRuleFileHandle.exists()) {
            cachedRuleFileHandle.readText()
        } else {
            BufferedReader(InputStreamReader(context.assets.open("ContentParserRule.json"))).readText()
        }
        Rules = JSONObject.parseObject(configText)
        version = RuleVersion(Rules.getString("version"))
        Parser = ContentParserFactory.Provide()

        Log.d(LOGTAG, "HTML Parser Loaded! Description here: ↓")
        Parser.Description()
    }

}