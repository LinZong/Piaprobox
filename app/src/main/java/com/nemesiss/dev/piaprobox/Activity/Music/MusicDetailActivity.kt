package com.nemesiss.dev.piaprobox.Activity.Music

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.nemesiss.dev.HTMLContentParser.Model.MusicContentInfo
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Bindings.IllustratorViewBindings
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser.Companion.GetAlbumThumb
import com.nemesiss.dev.piaprobox.Util.AppUtil
import com.nemesiss.dev.piaprobox.View.Common.AutoWrapLayout
import com.nemesiss.dev.piaprobox.View.Common.SetTextWithClickableUrl
import kotlinx.android.synthetic.main.activity_music_detail.*

class MusicDetailActivity : PiaproboxBaseActivity() {

    companion object {
        @JvmStatic
        val MUSIC_CONTENT_INFO_INTENT_KEY = "MUSIC_CONTENT_INFO_INTENT_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_detail)
        ShowToolbarBackIcon(MusicDetail_Toolbar)

        val contentInfo = intent.getSerializableExtra(MUSIC_CONTENT_INFO_INTENT_KEY) as? MusicContentInfo
        if (contentInfo == null) {
            Toast.makeText(this, resources.getString(R.string.MusicDetailActivityParamWrong), Toast.LENGTH_SHORT).show()
            return
        }
        val AvatarURL = GetAlbumThumb(contentInfo.ArtistAvatar)
        ShowArtistInfo(AvatarURL, contentInfo.Artist)
        ShowWorkItemInfo(contentInfo.CreateDetail, MusicDetail_AutoWrap_Tag_Container)
        ShowWorkItemDetail(contentInfo.CreateDescription)
    }

    private fun ShowArtistInfo(AvatarURL: String, Artist: String) {

//        Glide.with(this)
//            .load(AvatarURL)
//            .into(MusicDetail_ArtistAvatar)

        IllustratorViewBindings.loadImageArtistAvatar(MusicDetail_ArtistAvatar,AvatarURL)

        MusicDetail_Artist.text = Artist
    }

    private fun ShowWorkItemInfo(OriginalWorkInfoText: String, AutoWrapContainer : AutoWrapLayout) {
        OriginalWorkInfoText.split(" | ").forEach {
            val DelimiterPos = it.indexOf('：')
            // Key: 0-DelimiterPos   Value: DelimiterPos+1-End
            val text = SpannableString(it)
            val tagColor = ForegroundColorSpan(resources.getColor(R.color.TagSelectedBackground))
            if (DelimiterPos > 1) {
                text.setSpan(tagColor, 0, DelimiterPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            val tv = TextView(this)
            tv.text = text
            val lp =
                ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 0, 12, 8)
            tv.layoutParams = lp
//            MusicDetail_AutoWrap_Tag_Container.addView(tv)
            AutoWrapContainer.addView(tv)

        }
    }

    private fun ShowWorkItemDetail(OriginalWorkItemDetailText: String) {

        OriginalWorkItemDetailText.split("<br> ").forEach {

            val tv = TextView(this)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tv.SetTextWithClickableUrl(it)
            tv.layoutParams = lp
            lp.setMargins(0, 0, 0, AppUtil.Dp2Px(resources, 8))
            tv.setTextColor(resources.getColor(R.color.WorkDetailText))
            MusicDetail_DetailText_Container.addView(tv)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }
}