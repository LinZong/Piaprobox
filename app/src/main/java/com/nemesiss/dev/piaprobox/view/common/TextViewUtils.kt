package com.nemesiss.dev.piaprobox.view.common

import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Util.AppUtil
import com.nemesiss.dev.piaprobox.view.spans.UrlClickableSpan

private fun <T> ConfirmListNotNull(list: List<T>, vararg indexes: Int): Boolean {
    for (i in indexes) {
        if (list[i] == null) {
            return false
        }
    }
    return true
}

class TextViewUtils {
    companion object {
        @JvmStatic
        fun SetTextWithClickableUrl(textView: TextView, text: String?) {
            textView.SetTextWithClickableUrl(text ?: "")
        }
    }
}

fun TextView.SetTextWithClickableUrl(text: String) {
    val regex = "<a (?<=(.))href=\"(.*)\" .*>(.*)</a>".toRegex()
    val result = regex.find(text)
    if (result != null && ConfirmListNotNull(result.groups.toList())) {
        val total = result.groups[0]
        val href = result.groups[2]
        val innerText = result.groups[3]
        val hrefBegin = total!!.range.first
        val replaceHtmlTagText = text.replace(regex, innerText!!.value)
        val clickUrlText = SpannableString(replaceHtmlTagText)
        clickUrlText.setSpan(
            UrlClickableSpan(this.context, HTMLParser.wrapDomain(href!!.value)) { url, _ ->
                AppUtil.OpenBrowser(this.context, url, listOf(Pair("Referer", "https://piapro.jp/t/8tQS")))
            },
            hrefBegin,
            hrefBegin + innerText.value.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        this.text = clickUrlText
        this.movementMethod = LinkMovementMethod.getInstance()
    } else {
        this.text = text
    }
}