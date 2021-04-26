package com.nemesiss.dev.piaprobox.view.common

import android.content.Context
import android.util.AttributeSet

interface XmlConfigurableLayout {

    fun obtainXmlConfig(context: Context, attrs: AttributeSet?)
}