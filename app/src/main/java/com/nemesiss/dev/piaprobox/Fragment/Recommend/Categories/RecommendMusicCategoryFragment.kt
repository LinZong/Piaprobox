import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.HTMLContentParser.Model.RecommendTagModel
import com.nemesiss.dev.piaprobox.Activity.Music.MusicControlActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity
import com.nemesiss.dev.piaprobox.Adapter.Common.TagItemAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.MusicRecommendItemAdapter
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.Fragment.Recommend.MainRecommendFragment.Companion.DefaultTagUrl
import com.nemesiss.dev.piaprobox.Fragment.Recommend.RecommendListType
import com.nemesiss.dev.piaprobox.Fragment.Recommend.Categories.BaseRecommendCategoryFragment
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import com.nemesiss.dev.piaprobox.View.Common.SingleTagView
import kotlinx.android.synthetic.main.recommend_category_layout.*
import org.jsoup.Jsoup

class RecommendMusicCategoryFragment : BaseRecommendCategoryFragment() {


    private lateinit var htmlParser: HTMLParser

    private var recommendListAdapter: MusicRecommendItemAdapter? = null
    private var recommendItemLayoutManager: LinearLayoutManager? = null
    private var recommendListData: List<RecommendItemModel>? = null

    private var tagListAdapter: TagItemAdapter? = null
    private var tagListLayoutManager: LinearLayoutManager? = null
    private var tagListData: List<RecommendTagModel>? = null

    private var CurrentLoadTagPageURL = DefaultTagUrl


    override fun Refresh() {
        LoadDefaultPage(RecommendListType.MUSIC,false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("MainRecommendFragment","创建View  RecommendMusicCategoryFragment")
        return inflater.inflate(R.layout.recommend_category_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        htmlParser = HTMLParser(context ?: PiaproboxApplication.Self.applicationContext)

        LoadDefaultPage(RecommendListType.MUSIC)
    }

    override fun LoadDefaultPage(contentType: RecommendListType, ShouldUpdateTagList : Boolean) {
        ShowLoadingIndicator()
        DaggerFetchFactory.create()
            .fetcher()
            .visit(CurrentLoadTagPageURL)
            .cookie("top_view","music")
            .goAsync({ response ->
                SimpleResponseHandler(response, String::class)
                    .Handle({
                        if(ShouldUpdateTagList)
                            ParseTagListContent(it as String) // Load tags.
                        ParseRecommendListContent(it as String, contentType) // Load all recommend item.
                    }, { code, _ ->
                        HideLoadingIndicator()
                        LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
                    })
            }, { e ->
                HideLoadingIndicator()
                activity?.runOnUiThread { LoadFailedTips(-4, e.message ?: "") }
            })
    }

    override fun LoadRecommendList(tagUrl: String, contentType: RecommendListType) {
        DaggerFetchFactory.create()
            .fetcher()
            .visit(tagUrl)
            .goAsync({ response ->
                SimpleResponseHandler(response, String::class)
                    .Handle({
                        ParseRecommendListContent(it as String, contentType)
                    }, { code, _ ->
                        HideLoadingIndicator()
                        LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
                    })
            }, { e ->
                HideLoadingIndicator()
                activity?.runOnUiThread { LoadFailedTips(-4, e.message ?: "") }
            })
    }

    override fun OnTagItemSelected(index: Int) {
        val childCount = tagListAdapter!!.itemCount
        for (i in 0 until childCount) {
            val vh = Recommend_Frag_Common_Tag_RecyclerView.findViewHolderForAdapterPosition(i)
            if (vh != null) {
                val tagVH = vh as TagItemAdapter.TagItemVH
                val view = tagVH.itemView as SingleTagView
                if (i == index) {
                    view.SetSelected()
                    ShowLoadingIndicator()
                    CurrentLoadTagPageURL = DefaultTagUrl + tagListData!![i].URL
                    LoadRecommendList(CurrentLoadTagPageURL, RecommendListType.MUSIC)
                } else {
                    view.SetDeSelected()
                }
            }
        }
    }

    override fun OnRecommendItemSelected(index: Int) {
        val item = recommendListData!!.get(index)
        val URL = DefaultTagUrl + item.URL
        val intent = Intent(PiaproboxApplication.Self.applicationContext, MusicControlActivity::class.java)
        intent.putExtra(MusicPlayerActivity.MUSIC_CONTENT_URL, URL)
        startActivity(intent)
    }

    override fun ParseTagListContent(HTMLString: String) {
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

    override fun ParseRecommendListContent(HTMLString: String, contentType: RecommendListType) {
        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("RecommendList-" + contentType.Name).getJSONArray("Steps")
        try {
            recommendListData = (htmlParser.Parser.GoSteps(root, rule) as Array<*>).map { it as RecommendItemModel }

            activity?.runOnUiThread {
                recommendItemLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                Recommend_Frag_Common_RecyclerView.layoutManager = recommendItemLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter =
                        MusicRecommendItemAdapter(recommendListData!!, context!!, this::OnRecommendItemSelected)
                    Recommend_Frag_Common_RecyclerView.adapter = recommendListAdapter
                } else {
                    Recommend_Frag_Common_RecyclerView.adapter = recommendListAdapter
                    recommendListAdapter?.items = recommendListData!!
                    recommendListAdapter?.notifyDataSetChanged()
                }
            }
        } catch (e: InvalidStepExecutorException) {
            LoadFailedTips(-1, "InvalidStepExecutorException: ${e.message}")
        } catch (e: ClassNotFoundException) {
            LoadFailedTips(-2, "ClassNotFoundException: ${e.message}")
        } catch (e: Exception) {
            LoadFailedTips(-3, "Exception: ${e.message}")
        } finally {
            HideLoadingIndicator()
        }
    }
}