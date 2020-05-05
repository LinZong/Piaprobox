package com.nemesiss.dev.piaprobox.Fragment.HomePage

import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories.BaseRecommendCategoryFragment
import com.nemesiss.dev.piaprobox.R
import org.apache.http.client.utils.URIBuilder
import java.net.URI


enum class SubmissionWorkType(val StepRulePostfix: String, val UrlPathName: String) {
    MUSIC("MUSIC", "music"),
    ILLUSTRATION("IMAGE", "illust"),
    TEXT("TEXT", "text");
}

class SubmissionWorkUrlBuilder(fromUrl: String = "https://piapro.jp/") {
    private val builder = URIBuilder(fromUrl)
    fun type(type: SubmissionWorkType): SubmissionWorkUrlBuilder {
        builder.path = type.UrlPathName
        return this
    }

    fun tag(tagString: String): SubmissionWorkUrlBuilder {
        builder.addParameter("tag", tagString)
        return this
    }

    fun category(id: Int): SubmissionWorkUrlBuilder {
        builder.addParameter("categoryId", id.toString(10))
        return this
    }

    fun page(page: Int): SubmissionWorkUrlBuilder {
        builder.addParameter("page", page.toString(10))
        return this
    }

    fun buildString() = builder.build().toString()
    fun build(): URI = builder.build()
}

abstract class BaseSubmissionWorkCategoryFragment : BaseRecommendCategoryFragment() {

    companion object {
        @JvmStatic
        private val PageLimit = 30
    }

    protected fun LoadMore(CurrentVisitUrl: String, NextPage: Int, submissionWorkType: SubmissionWorkType) {
        if (!(1..PageLimit).contains(NextPage)) {
            AppendSubmissionWorkListContent("", submissionWorkType, true)
            return
        }
        ShowLoadingIndicator()
        LoadFragmentPage(SubmissionWorkUrlBuilder(CurrentVisitUrl).page(NextPage).buildString(),
            {
                AppendSubmissionWorkListContent(it, submissionWorkType) // Load all recommend item.
            }, { code, _ ->
                HideLoadingIndicator()
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
}