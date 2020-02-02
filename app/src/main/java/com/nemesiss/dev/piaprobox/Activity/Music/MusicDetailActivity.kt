package com.nemesiss.dev.piaprobox.Activity.Music

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Util.AppUtil
import kotlinx.android.synthetic.main.activity_music_detail.*

class MusicDetailActivity : PiaproboxBaseActivity()
{

    companion object {
        @JvmStatic
        val WORK_ITEM_INFO_INTENT_KEY = "WORK_ITEM_INFO_INTENT_KEY"

        @JvmStatic
        val WORK_ITEM_DETAIL_INTENT_KEY = "WORK_ITEM_DETAIL_INTENT_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_detail)
        ShowToolbarBackIcon(MusicDetail_Toolbar)

//        val WorkItemInfo = intent.getStringExtra(WORK_ITEM_INFO_INTENT_KEY)
//        val WorkItemDetail = intent.getStringExtra(WORK_ITEM_DETAIL_INTENT_KEY)

        ShowArtistInfo()
        ShowWorkItemInfo("投稿日：2009/05/02 13:34:18 | タイム/サイズ：04:04/(5,710KB） | 閲覧数：111,559 | カテゴリ：カラオケ/インスト | 全2バージョン")
        val DetailText = "またもや急遽off vocalアップしました…！ なんの調整もしてないのでちょっと大丈夫なのか心配ですが…。。 あと今回から元音源をノーマライズするだけにしたので 音圧はボーカルと混ぜてから上げて頂けるようお願いします(`д´ )ゞ 【追記】 と昨日の時点では書いたのですが、やっぱ音圧上げたものも上げておきますorz 優柔不断でスミマセンorz ミックスする時にボーカルの入る隙間がある方が良い！という方は 「前のバージョン」をクリックしてノーマライズのみの音源を 持って行って下さいまし！ ----------------------------- 「magnet」 (ミク)か細い火が　心の端に灯る いつの間にか燃え広がる熱情 私の蝶　不規則に飛び回り あなたの手に鱗粉を付けた (ルカ)絡み合う指ほどいて　唇から舌へと 許されない事ならば　尚更燃え上がるの (ミク)抱き寄せて欲しい　確かめて欲しい 間違いなど無いんだと　思わせて キスをして　塗り替えて欲しい 魅惑の時に酔いしれ溺れていたいの (ルカ)束縛して　もっと必要として 愛しいなら執着を見せつけて 「おかしい」のが　たまらなく好きになる 行けるトコまで行けばいいよ (ミク)迷い込んだ心なら　簡単に融けてゆく 優しさなんて感じる暇など　無い位に (ルカ)繰り返したのは　あの夢じゃなくて 紛れも無い現実の私達 触れてから　戻れないと知る　それでいいの… 誰よりも大切なあなた (ミク)夜明けが来ると不安で　泣いてしまう私に 「大丈夫」と囁いたあなたも　泣いていたの？ (ミク)抱き寄せて欲しい　確かめて欲しい 間違いなど無いんだと　思わせて キスをして　塗り替えて欲しい　魅惑の時に 酔いしれ溺れたい (ルカ)引き寄せて　マグネットのように 例えいつか離れても巡り会う 触れていて　戻れなくていい　それでいいの 誰よりも大切なあなた -----------------------------"
        ShowWorkItemDetail(DetailText)

    }

    private fun ShowArtistInfo() {

        Glide.with(this)
            .load(R.drawable.thumb_rin)
            .into(MusicDetail_ArtistAvatar)

        MusicDetail_Artist.text = "Hahahaha"

    }

    private fun ShowWorkItemInfo(OriginalWorkInfoText : String) {
        OriginalWorkInfoText.split(" | ").forEach {
            val DelimiterPos = it.indexOf('：')
            // Key: 0-DelimiterPos   Value: DelimiterPos+1-End
            val text = SpannableString(it)
            val tagColor = ForegroundColorSpan(resources.getColor(R.color.TagSelectedBackground))
            if(DelimiterPos > 1) {
                text.setSpan(tagColor, 0, DelimiterPos,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            val tv = TextView(this)
            tv.text = text

            val lp = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0,0,12,8)
            tv.layoutParams = lp
            MusicDetail_AutoWrap_Tag_Container.addView(tv)
        }
    }

    private fun ShowWorkItemDetail(OriginalWorkItemDetailText : String) {

        OriginalWorkItemDetailText.split(" ").forEach {

            val tv = TextView(this)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            tv.text = it
            tv.layoutParams = lp
            lp.setMargins(0,0,0,AppUtil.Dp2Px(resources, 8))
            tv.setTextColor(resources.getColor(R.color.WorkDetailText))
            MusicDetail_DetailText_Container.addView(tv)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }
}