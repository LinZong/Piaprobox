package com.nemesiss.dev.piaprobox.Fragment.HomePage

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.nemesiss.dev.piaprobox.Adapter.Common.InfinityLoadAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories.BaseRecommendCategoryFragment
import com.nemesiss.dev.piaprobox.Misc.RecyclerViewInnerIndicator
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.View.Common.canAddIndicator
import com.nemesiss.dev.piaprobox.View.Common.removeIndicator


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
                HideLoadMoreIndicatorOnRecyclerView(false)
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
}