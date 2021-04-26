package com.nemesiss.dev.piaprobox.view.spans

import android.content.Context
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Util.AppUtil

class UrlClickableSpan(
    private val context: Context,
    private val url: String,
    private val CustomOnClick: ((String, View) -> Unit)? = null
) : ClickableSpan() {

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = context.resources.getColor(R.color.IllustratorArtistNameColor)
        ds.isUnderlineText = false
    }

    override fun onClick(widget: View) {
        if(CustomOnClick != null) {
            CustomOnClick.invoke(url,widget)
        } else {
            AppUtil.OpenBrowser(context, url)
        }
    }
}