package com.nemesiss.dev.piaprobox.View.Common

import android.content.Context
import android.util.AttributeSet

interface XmlConfigurableLayout {

    fun obtainXmlConfig(context: Context, attrs: AttributeSet?)
}