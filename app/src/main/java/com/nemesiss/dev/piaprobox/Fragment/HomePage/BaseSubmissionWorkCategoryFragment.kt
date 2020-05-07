package com.nemesiss.dev.piaprobox.Fragment.HomePage

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jaredrummler.materialspinner.MaterialSpinner
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendTagModel
import com.nemesiss.dev.HTMLContentParser.Model.SubmissionWorkFilterModel
import com.nemesiss.dev.piaprobox.Adapter.Common.InfinityLoadAdapter
import com.nemesiss.dev.piaprobox.Adapter.Common.TagItemAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories.BaseRecommendCategoryFragment
import com.nemesiss.dev.piaprobox.Misc.RecyclerViewInnerIndicator
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.View.Common.canAddIndicator
import com.nemesiss.dev.piaprobox.View.Common.removeIndicator
import kotlinx.android.synthetic.main.category_filter.*
import kotlinx.android.synthetic.main.recommend_category_layout.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


enum class SubmissionWorkType(val StepRulePostfix: String, val UrlPathName: String) {
    MUSIC("MUSIC", "music/"),
    ILLUSTRATION("IMAGE", "illust/"),
    TEXT("TEXT", "text/"),
    __FILTERMENU("FilterMenu", ""),
    __FILTERCATEGORY("FilterCategory", "")
}

enum class FilterType {
    FilterMenu,
    FilterCategory
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

    private var filterMenu: List<SubmissionWorkFilterModel>? = null
    private var filterCategory: List<SubmissionWorkFilterModel>? = null
    protected open val MySubmissionType = SubmissionWorkType.MUSIC


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.category_fragment, container, false)
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
                HideLoadMoreIndicatorOnRecyclerView(false)
                LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
            })
    }

    override fun LoadDefaultSubmissionWorkPage(submissionWorkType: SubmissionWorkType) {
        ShowLoadingIndicator()
        LoadFragmentPage(SubmissionWorkUrlBuilder().type(submissionWorkType).buildString(),
            {
                ParseSubmissionWorkListContent(it, submissionWorkType) // Load all recommend item.
                ParseFilterOptions(it)
            }, { code, _ ->
                HideLoadingIndicator()
                LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
            })
    }

    private fun ParseFilterOptions(HTMLString: String) {
        val rootDocument = Jsoup.parse(HTMLString)
        val filterCategoryRules =
            htmlParser.Rules.getJSONObject("Submission-" + SubmissionWorkType.__FILTERCATEGORY.StepRulePostfix)
                .getJSONArray("Steps")

        val filterMenuRules =
            htmlParser.Rules.getJSONObject("Submission-" + SubmissionWorkType.__FILTERMENU.StepRulePostfix)
                .getJSONArray("Steps")

        try {
            filterMenu = (htmlParser.Parser.GoSteps(
                rootDocument,
                filterMenuRules
            ) as Array<*>).map { it as SubmissionWorkFilterModel }

            filterCategory = (htmlParser.Parser.GoSteps(
                rootDocument,
                filterCategoryRules
            ) as Array<*>).map { it as SubmissionWorkFilterModel }
            val categoryStrItems = filterCategory!!.map { menu -> menu.Name }
            val menuStrItems = filterMenu!!.map { menu -> menu.Name }
            activity?.runOnUiThread {
                // 填充filter选项
                BaseCategory_FilterCategory.setItems(categoryStrItems)
                BaseCategory_FilterMenu.setItems(menuStrItems)
                BaseCategory_FilterCategory.setOnItemSelectedListener(OnFilterItemClickedHandler(FilterType.FilterCategory))
                BaseCategory_FilterMenu.setOnItemSelectedListener(OnFilterItemClickedHandler(FilterType.FilterMenu))
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

    private fun <T> OnFilterItemClickedHandler(whichOne: FilterType): (MaterialSpinner, Int, Long, T) -> Unit {
        return { view, position, id, item ->
            val selectedItem = (when (whichOne) {
                FilterType.FilterMenu -> filterMenu
                FilterType.FilterCategory -> filterCategory
            })!![position]
            ShowLoadingIndicator()
            DisableFilter()
            LoadFragmentPage(HTMLParser.WrapDomain(selectedItem.URL),
                {
                    EnableFilter()
                    ParseSubmissionWorkListContent(it, MySubmissionType) // Load all recommend item.
                }, { code, _ ->
                    EnableFilter()
                    HideLoadingIndicator()
                    LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
                })
        }
    }

    private fun DisableFilter() {
        activity?.runOnUiThread {
            BaseCategory_FilterCategory.isEnabled = false
            BaseCategory_FilterMenu.isEnabled = false
        }
    }

    private fun EnableFilter() {
        activity?.runOnUiThread {
            BaseCategory_FilterCategory.isEnabled = true
            BaseCategory_FilterMenu.isEnabled = true
        }
    }

    abstract fun AppendSubmissionWorkListContent(
        HTMLString: String,
        submissionWorkType: SubmissionWorkType,
        ReachPageLimit: Boolean = false
    )

    protected abstract fun ShowLoadMoreIndicatorOnRecyclerView()

    protected inline fun <reified T> ShowLoadMoreIndicatorOnRecyclerView(
        adapterDataSource: MutableList<T>,
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        PendingRefreshAdapterStatus: Boolean = true
    ): Boolean {
        // assume all the elements are the same.
        if (adapterDataSource.isNotEmpty() && canAddIndicator<T>()) {
            return try {
                val elem = adapterDataSource[0] as Any
                val clazz = elem::class.java
                val URL_Field = clazz.getDeclaredField("URL")
                val indicator = clazz.newInstance() as T
                URL_Field.set(indicator, RecyclerViewInnerIndicator.RECYCLER_VIEW_LOAD_MORE_INDICATOR.TAG);
                adapterDataSource.add(indicator)
                if (!PendingRefreshAdapterStatus) {
                    activity?.runOnUiThread { adapter.notifyItemInserted(adapterDataSource.size - 1) }
                }
                true
            } catch (noField: NoSuchFieldException) {
                false
            } catch (illegalAccess: IllegalAccessException) {
                false
            } catch (instantiationFailed: InstantiationException) {
                false
            }
        }
        return false
    }

    protected inline fun <reified T> HideLoadMoreIndicatorOnRecyclerView(
        adapterDataSource: MutableList<T>,
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        PendingRefreshAdapterStatus: Boolean = true
    ): Boolean {
        if (canAddIndicator<T>()) {
            val removedIndex = adapterDataSource.removeIndicator()
            if (!PendingRefreshAdapterStatus && removedIndex != -1) {
                activity?.runOnUiThread { adapter.notifyItemRemoved(removedIndex) }
                return true
            }
        }
        // 告知它已经完成加载了
        (adapter as? InfinityLoadAdapter)?.loaded()
        return false
    }


    protected inline fun <reified T> ShowNothingMoreIndicatorOnRecyclerView(
        adapterDataSource: MutableList<T>,
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    ): Boolean {
        if (adapterDataSource.isNotEmpty() && canAddIndicator<T>()) {
            return try {
                val elem = adapterDataSource[0] as Any
                val clazz = elem::class.java
                val URL_Field = clazz.getDeclaredField("URL")
                val indicator = clazz.newInstance() as T
                URL_Field.set(indicator, RecyclerViewInnerIndicator.RECYCLER_VIEW_NOTHING_MORE_INDICATOR.TAG);
                adapterDataSource.add(indicator)
                adapter.notifyItemInserted(adapterDataSource.size - 1)
                if (adapter is InfinityLoadAdapter) {
                    adapter.loaded()
                    adapter.disable()
                }
                true
            } catch (noField: NoSuchFieldException) {
                false
            } catch (illegalAccess: IllegalAccessException) {
                false
            } catch (instantiationFailed: InstantiationException) {
                false
            }
        }
        return false
    }

    protected abstract fun HideLoadMoreIndicatorOnRecyclerView(PendingRefreshAdapterStatus: Boolean = true)

    protected abstract fun ShowNothingMoreIndicatorOnRecyclerView()

    override fun Refresh() {
        LoadDefaultSubmissionWorkPage(MySubmissionType)
    }
}