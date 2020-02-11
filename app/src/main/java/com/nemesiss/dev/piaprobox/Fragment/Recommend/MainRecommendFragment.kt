package com.nemesiss.dev.piaprobox.Fragment.Recommend

import RecommendMusicCategoryFragment
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.RecommendCategoryFragmentPageAdapter
import com.nemesiss.dev.piaprobox.Fragment.Main.BaseMainFragment
import com.nemesiss.dev.piaprobox.Fragment.Recommend.Categories.BaseRecommendCategoryFragment
import com.nemesiss.dev.piaprobox.Fragment.Recommend.Categories.RecommendImageCategoryFragment
import com.nemesiss.dev.piaprobox.Fragment.Recommend.Categories.RecommendTextCategoryFragment
import com.nemesiss.dev.piaprobox.R
import kotlinx.android.synthetic.main.fragment_header.*
import kotlinx.android.synthetic.main.recommand_fragment.*

enum class RecommendListType(var Name: String, var Index: Int, var CookieName: String) {
    MUSIC("MUSIC", 0, "music"),
    IMAGE("IMAGE", 1, "image"),
    TEXT("TEXT", 2, "text");
}

class MainRecommendFragment : BaseMainFragment() {

    private var CurrentContentType =
        RecommendListType.MUSIC

    private lateinit var fragments: List<BaseRecommendCategoryFragment>

    private var CurrentDisplayFragmentIndex = 0

    companion object {
        @JvmStatic
        val DefaultTagUrl = "https://piapro.jp"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        Log.d("MainRecommendFragment","创建View")
        return inflater.inflate(R.layout.recommand_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        Log.d("MainRecommendFragment","View 被创建")
        BindCategoryClickHandler()
        Recommend_Category_Tag_Music.isSelected = true
        Recommend_Category_Tag_Music.setTextColor(Color.WHITE)
        fragments = arrayListOf(
            RecommendMusicCategoryFragment(),
            RecommendImageCategoryFragment(),
            RecommendTextCategoryFragment()
        )
        Recommend_Category_Frag_Pager.adapter =
            RecommendCategoryFragmentPageAdapter(childFragmentManager, fragments) // 嵌套在Fragment里面的子Fragment需要使用childFragmentManager.
        Recommend_Category_Frag_Pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(index: Int) {
                // 左右滑动和上面的点击区域联动
                FadeInAndOutCategoryTagBackground(CurrentDisplayFragmentIndex, index)
                CurrentDisplayFragmentIndex = index
            }

        })
        Recommend_Category_Frag_Pager.offscreenPageLimit = fragments.size
    }

    override fun Refresh() {
        // 暂时不联网测试
        fragments[CurrentDisplayFragmentIndex].Refresh()
    }

    fun CurrentDisplayFragment() : BaseRecommendCategoryFragment {
        return fragments[CurrentDisplayFragmentIndex]
    }

    private fun BindCategoryClickHandler() {
        arrayOf(Recommend_Category_Tag_Music, Recommend_Category_Tag_Image, Recommend_Category_Tag_Text)
            .zip(
                arrayOf(
                    RecommendListType.MUSIC,
                    RecommendListType.IMAGE,
                    RecommendListType.TEXT
                )
            )
            .forEachIndexed { index, pair ->
                pair.first.setOnClickListener {
                    if (pair.second != CurrentContentType) {
                        FadeInAndOutCategoryTagBackground(CurrentContentType.Index, index)
                        CurrentContentType = pair.second
                        OnCategoryTagSelected(CurrentContentType)
                    }
                }
            }
    }

    private fun FadeInAndOutCategoryTagBackground(DimIndex: Int, BrightIndex: Int) {
        val dimView = Recommend_Category_Tag_Container.getChildAt(DimIndex) as TextView
        val brightView = Recommend_Category_Tag_Container.getChildAt(BrightIndex) as TextView

        val dimViewBg = dimView.background
        val brightBg = brightView.background
        val dimAnim = ObjectAnimator.ofInt(dimViewBg, "alpha", 255, 0)
        val brightAnim = ObjectAnimator.ofInt(brightBg, "alpha", 0, 255)

        val DimBrightSet = AnimatorSet()
        DimBrightSet.playTogether(dimAnim, brightAnim)
        DimBrightSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }
            override fun onAnimationEnd(p0: Animator?) {
                dimView.isSelected = false
            }
            override fun onAnimationCancel(p0: Animator?) {
            }
            override fun onAnimationStart(p0: Animator?) {
                brightView.isSelected = true
                brightBg.alpha = 0
            }
        })
        DimBrightSet.duration = 300
        val textColorDimBrightAnim = ValueAnimator.ofInt(128, 255)
        textColorDimBrightAnim.addUpdateListener {
            val value = it.animatedValue as Int
            val dimColor = Color.rgb(128 + 255 - value, 128 + 255 - value, 128 + 255 - value)
            val brightColor = Color.rgb(value, value, value)
            dimView.setTextColor(dimColor)
            brightView.setTextColor(brightColor)
        }
        textColorDimBrightAnim.duration = 300
        DimBrightSet.start()
        textColorDimBrightAnim.start()
    }

    private fun OnCategoryTagSelected(contentType: RecommendListType) {
        Recommend_Category_Frag_Pager.setCurrentItem(contentType.Index, true)
    }

    override fun LoadBannerImage() {
        BaseMainFragment_Banner_ImageView.setImageResource(R.drawable.recommand_banner)
    }
}