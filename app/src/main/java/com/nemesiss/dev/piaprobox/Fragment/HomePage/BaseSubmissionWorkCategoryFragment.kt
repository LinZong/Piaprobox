package com.nemesiss.dev.piaprobox.Fragment.HomePage

import android.net.Uri
import android.util.Log
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories.BaseRecommendCategoryFragment
import com.nemesiss.dev.piaprobox.R


enum class SubmissionWorkType(val StepRulePostfix: String, val UrlPathName: String) {
    MUSIC("MUSIC", "music/"),
    ILLUSTRATION("IMAGE", "illust/"),
    TEXT("TEXT", "text/");
}

class SubmissionWorkUrlBuilder(fromUrl: String = "https://piapro.jp") {
    private val builder = Uri.parse(fromUrl).buildUpon()
    fun type(type: SubmissionWorkType): SubmissionWorkUrlBuilder {
        builder.appendPath(type.UrlPathName)
        return this
    }

    fun tag(tagString: String): SubmissionWorkUrlBuilder {
        builder.appendQueryParameter("tag", tagString)
        return this
    }

    fun category(id: Int): SubmissionWorkUrlBuilder {
        builder.appendQueryParameter("categoryId", id.toString(10))
        return this
    }

    fun page(page: Int): SubmissionWorkUrlBuilder {
        builder.appendQueryParameter("page", page.toString(10))
        return this
    }

    fun buildString() = builder.build().toString().replace("%2F", "/")
    fun build() = builder.build()
}

abstract class BaseSubmissionWorkCategoryFragment : BaseRecommendCategoryFragment() {

    companion object {
        @JvmStatic
        private val PageLimit = 30
    }

    protected fun LoadMoreItem(CurrentVisitUrl: String, NextPage: Int, submissionWorkType: SubmissionWorkType) {
        if (!(1..PageLimit).contains(NextPage)) {
            AppendSubmissionWorkListContent("", submissionWorkType, true)
            return
        }
        // Show a small loading indicator at the 'last' position of RecyclerView.
        // Determined by the subclass.
        Log.d("CategoryFragment", "即将加载第: $NextPage 页")
        ShowLoadMoreIndicatorOnRecyclerView()
        LoadFragmentPage(SubmissionWorkUrlBuilder(CurrentVisitUrl).page(NextPage).buildString(),
            {
                AppendSubmissionWorkListContent(it, submissionWorkType) // Load all recommend item.
            }, { code, _ ->
                // Should not pending refresh adapter status, but clear the loading indicator immediately
                // due to a failed load more request.
                activity?.runOnUiThread { HideLoadMoreIndicatorOnRecyclerView(false) }
                LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
            })
    }

    override fun LoadDefaultSubmissionWorkPage(submissionWorkType: SubmissionWorkType) {
        ShowLoadingIndicator()
        LoadFragmentPage(SubmissionWorkUrlBuilder().type(submissionWorkType).buildString(),
            {
                ParseSubmissionWorkListContent(it, submissionWorkType) // Load all recommend item.
            }, { code, _ ->
                HideLoadingIndicator()
                LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
            })
    }

    abstract fun AppendSubmissionWorkListContent(
        HTMLString: String,
        submissionWorkType: SubmissionWorkType,
        ReachPageLimit: Boolean = false
    )

    protected abstract fun ShowLoadMoreIndicatorOnRecyclerView()

    protected abstract fun HideLoadMoreIndicatorOnRecyclerView(PendingRefreshAdapterStatus: Boolean = true)

    protected abstract fun ShowNothingMoreIndicatorOnRecyclerView()
}