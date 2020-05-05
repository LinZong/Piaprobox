package com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendTagModel
import com.nemesiss.dev.piaprobox.Adapter.Common.TagItemAdapter
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.Fragment.BaseMainFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.MainRecommendFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.RecommendListType
import com.nemesiss.dev.piaprobox.Fragment.HomePage.SubmissionWorkType
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerHTMParserFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HTMLParserModules
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.handle
import com.nemesiss.dev.piaprobox.View.Common.SingleTagView
import kotlinx.android.synthetic.main.recommend_category_layout.*
import okhttp3.Response
import org.jsoup.Jsoup
import javax.inject.Inject

abstract class BaseRecommendCategoryFragment : BaseMainFragment() {

    @Inject
    protected lateinit var htmlParser: HTMLParser
    protected open var tagListAdapter: TagItemAdapter? = null
    protected open var tagListLayoutManager: LinearLayoutManager? = null
    protected open var tagListData: List<RecommendTagModel>? = null

    protected open var CurrentLoadTagPageURL: String = MainRecommendFragment.DefaultTagUrl

    protected open var CurrentCategoryFragmentType = RecommendListType.TEXT

    abstract override fun Refresh()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerHTMParserFactory
            .builder()
            .hTMLParserModules(HTMLParserModules(context ?: PiaproboxApplication.Self.applicationContext))
            .build()
            .inject(this)
    }

    protected open fun LoadFragmentPage(
        visitUrl: String,
        contentType: RecommendListType,
        resolve: (String) -> Unit,
        rejected: (Int, Response) -> Unit
    ) {
        DaggerFetchFactory.create()
            .fetcher()
            .visit(visitUrl)
            .cookie("top_view", contentType.CookieName)
            .goAsync({ response ->
                response.handle(resolve, rejected)
            }, { e ->
                HideLoadingIndicator()
                activity?.runOnUiThread { LoadFailedTips(-4, e.message ?: "") }
            })
    }

    protected open fun LoadFragmentPage(
        visitUrl: String,
        resolve: (String) -> Unit,
        rejected: (Int, Response) -> Unit
    ) {
        DaggerFetchFactory.create()
            .fetcher()
            .visit(visitUrl)
            .goAsync({ response ->
                response.handle(resolve, rejected)
            }, { e ->
                HideLoadingIndicator()
                activity?.runOnUiThread { LoadFailedTips(-4, e.message ?: "") }
            })
    }

    protected open fun LoadDefaultRecommendPage(contentType: RecommendListType, ShouldUpdateTagList: Boolean = true) {
        ShowLoadingIndicator()
        LoadFragmentPage(CurrentLoadTagPageURL, CurrentCategoryFragmentType, {
            if (ShouldUpdateTagList)
                ParseTagListContent(it) // Load tags.
            ParseRecommendListContent(it, contentType) // Load all recommend item.
        }, { code, _ ->
            HideLoadingIndicator()
            LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
        })
    }

    protected open fun LoadDefaultSubmissionWorkPage(submissionWorkType: SubmissionWorkType) {

    }

    protected open fun LoadRecommendList(tagUrl: String, contentType: RecommendListType) {
        ShowLoadingIndicator()
        LoadFragmentPage(CurrentLoadTagPageURL, CurrentCategoryFragmentType, {
            ParseRecommendListContent(it as String, contentType)
        }, { code, _ ->
            HideLoadingIndicator()
            LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
        })
    }

    protected open fun OnTagItemSelected(index: Int) {
        val childCount = tagListAdapter!!.itemCount
        for (i in 0 until childCount) {
            val vh = Recommend_Frag_Common_Tag_RecyclerView.findViewHolderForAdapterPosition(i)
            if (vh != null) {
                val tagVH = vh as TagItemAdapter.TagItemVH
                val view = tagVH.itemView as SingleTagView
                if (i == index) {
                    view.SetSelected()
                    CurrentLoadTagPageURL = MainRecommendFragment.DefaultTagUrl + tagListData!![i].URL
                    LoadRecommendList(CurrentLoadTagPageURL, CurrentCategoryFragmentType)
                } else {
                    view.SetDeSelected()
                }
            }
        }
    }

    abstract fun OnRecommendItemSelected(index: Int)

    protected open fun ParseTagListContent(HTMLString: String) {
        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("RecommendTag").getJSONArray("Steps")
        try {
            tagListData = (htmlParser.Parser.GoSteps(root, rule) as Array<*>).map { it as RecommendTagModel }
            activity?.runOnUiThread {
                tagListLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                Recommend_Frag_Common_Tag_RecyclerView.layoutManager = tagListLayoutManager
                if (tagListAdapter == null) {
                    tagListAdapter = TagItemAdapter(tagListData!!, this::OnTagItemSelected)
                    Recommend_Frag_Common_Tag_RecyclerView.adapter = tagListAdapter
                } else {
                    Recommend_Frag_Common_Tag_RecyclerView.adapter = tagListAdapter
                    tagListAdapter?.items = tagListData!!
                    tagListAdapter?.notifyDataSetChanged()
                }
            }
        } catch (e: InvalidStepExecutorException) {
            LoadFailedTips(-1, "InvalidStepExecutorException: ${e.message}")
        } catch (e: ClassNotFoundException) {
            LoadFailedTips(-2, "ClassNotFoundException: ${e.message}")
        } catch (e: Exception) {
            LoadFailedTips(-3, "Exception: ${e.message}")
        } finally {

        }
    }

    open fun ParseRecommendListContent(HTMLString: String, contentType: RecommendListType) {

    }

    open fun ParseSubmissionWorkListContent(HTMLString: String, contentType: SubmissionWorkType) {

    }
}