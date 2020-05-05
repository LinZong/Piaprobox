package com.nemesiss.dev.piaprobox.Fragment.HomePage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories.BaseRecommendCategoryFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.RecommendListType
import org.apache.http.client.utils.URIBuilder
import java.net.URI


enum class SubmissionWorkType(val StepRulePostfix: String, val UrlSegmentName: String) {
    MUSIC("MUSIC", "music"),
    ILLUSTRATION("IMAGE", "illust"),
    TEXT("TEXT", "text");
}

class SubmissionWorkUrlBuilder {
    private val builder = URIBuilder("https://piapro.jp/")
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


    protected abstract fun LoadMore()
}