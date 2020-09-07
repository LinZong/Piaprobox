package com.nemesiss.dev.piaprobox.Fragment.ImageViewer

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.nemesiss.dev.HTMLContentParser.Model.RelatedImageInfo
import com.nemesiss.dev.piaprobox.Activity.Common.PreviewImageActivity
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorImageProviderActivity
import com.nemesiss.dev.piaprobox.Adapter.IllustratorViewer.RelatedImageListAdapter
import com.nemesiss.dev.piaprobox.Model.Image.IllustratorViewFragmentViewModel
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Util.AppUtil
import com.nemesiss.dev.piaprobox.View.Common.AutoWrapLayout
import com.nemesiss.dev.piaprobox.databinding.IllustratorViewFragmentBinding
import kotlinx.android.synthetic.main.illustrator_view_fragment.*
import kotlinx.android.synthetic.main.illustrator_view_fragment.view.*

class IllustratorViewFragment : BaseIllustratorViewFragment() {

    companion object {
        @JvmStatic
        val CLICKED_ITEM_INDEX = "MyIndex"

        @JvmStatic
        val SHOULD_FETCH_DRAWABLE = "FetchDrawable"
    }

    // 加载状态管理变量：

    private var VIEW_CREATED = false
        set(value) {
            field = value
            TryLoadViewModelWhileFragmentStateChanged()
        }
    private var USER_CAN_VISITED = false

    @Volatile
    private var DATA_LOADED = false

    private val CURRENT_CAN_APPLY_VIEWMODEL
        get() = VIEW_CREATED && USER_CAN_VISITED && !DATA_LOADED

    private lateinit var binding: IllustratorViewFragmentBinding

    // 状态相关变量
    private var CurrentViewModel: IllustratorViewFragmentViewModel? = null
    private var IS_CURRENT_BIG_SIZE_IMAGE_LOADED = false
    private var FRAG_INDEX: Int = 0
    private var FETCH_DRAWABLE = false

    // Custom image load handler.
    private val LoadOriginalWorkDrawableToImageView = object : SimpleTarget<GlideDrawable>() {
        override fun onResourceReady(
            resource: GlideDrawable?,
            glideAnimation: GlideAnimation<in GlideDrawable>?
        ) {
            IS_CURRENT_BIG_SIZE_IMAGE_LOADED = true
            Illustrator2_View_ItemImageView?.setImageDrawable(resource)
        }
    }

    private var relatedImageListAdapter: RelatedImageListAdapter? = null
    private var relatedImagGridLayoutManager: GridLayoutManager = GridLayoutManager(context, 3)

    override fun onStop() {
        Glide.with(context).onStop()
        super.onStop()
    }
    override fun onDestroy() {
        Glide.with(context).onDestroy()
        VIEW_CREATED = false
        DATA_LOADED = false
        CurrentViewModel = null
        super.onDestroy()
    }

    override fun onDestroyView() {
        Illustrator2_View_ItemImageView.transitionName = null
        super.onDestroyView()
    }

    private fun UpdateTransitionNameBasedOnUserVisible() {
        if (USER_CAN_VISITED && Illustrator2_View_ItemImageView != null) {
            Illustrator2_View_ItemImageView.transitionName = resources.getString(R.string.ImageViewTransitionName)
        } else if (!USER_CAN_VISITED && Illustrator2_View_ItemImageView != null) {
            // 不可见的第一时间取消transitionName
            Illustrator2_View_ItemImageView.transitionName = null
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        USER_CAN_VISITED = isVisibleToUser

        // 对当前可见的ImageView设置transitionName
        UpdateTransitionNameBasedOnUserVisible()
        TryLoadViewModelWhileFragmentStateChanged()
    }

    private fun TryLoadViewModelWhileFragmentStateChanged() {
        if (VIEW_CREATED && USER_CAN_VISITED && !DATA_LOADED) {
            (activity as? IllustratorImageProviderActivity)?.AskForViewModel(FRAG_INDEX, this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FRAG_INDEX = arguments?.getInt(CLICKED_ITEM_INDEX, 0)!!
        FETCH_DRAWABLE = arguments?.getBoolean(SHOULD_FETCH_DRAWABLE, false)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.illustrator_view_fragment, container, false)
        // 对于需要从Provider处获取Drawable的Fragment，指示它获取Drawable，并且监听视图树。
        val root = binding.root
        if (FETCH_DRAWABLE) {
            root.Illustrator2_View_ItemImageView.transitionName = resources.getString(R.string.ImageViewTransitionName)
            root.Illustrator2_View_ItemImageView.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    root.Illustrator2_View_ItemImageView.viewTreeObserver.removeOnPreDrawListener(this)
                    activity?.supportStartPostponedEnterTransition() // 需要进行共享元素过渡的下一个ImageView已准备好，恢复过渡
                    return true
                }
            })
        }
        return root
    }

    override fun OnRelatedItemSelected(index: Int, relatedImageInfo: RelatedImageInfo) {
        DATA_LOADED = false
        (activity as? IllustratorImageProviderActivity)?.AskForViewModel(FRAG_INDEX, relatedImageInfo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        VIEW_CREATED = true
        if (FETCH_DRAWABLE) {
            Illustrator2_View_ItemImageView.setImageDrawable(IllustratorImageProviderActivity.PRE_SHOWN_IMAGE)
        }
        BindButtons()
    }

    private fun TellImageIsStillPreparing() {
        Toast.makeText(context, R.string.ImageContentInfoIsPreparing, Toast.LENGTH_SHORT).show()
    }

    private fun HandleEnterPinchImagePreview() {
        if (IS_CURRENT_BIG_SIZE_IMAGE_LOADED) {
            val intent = Intent(context!!, PreviewImageActivity::class.java)
            PreviewImageActivity.SetPreShownDrawable(Illustrator2_View_ItemImageView.drawable)
            startActivity(intent)
        } else {
            TellImageIsStillPreparing()
        }
    }

    private fun HandleDownloadImage() {
        // Call activity method.
        (activity as? IllustratorImageProviderActivity)?.HandleDownloadImage(
            CurrentViewModel!!.ItemImageUrl,
            CurrentViewModel!!.Title
        )
    }

    private fun BindButtons() {
        Illustrator2_View_BackButton.setOnClickListener { (activity as? IllustratorImageProviderActivity)?.HandleClose() }
        Illustrator2_View_DownloadImage.setOnClickListener { HandleDownloadImage() }
        Illustrator2_View_ItemImageView.setOnClickListener { HandleEnterPinchImagePreview() }
        Illustrator2_View_OpenBrowser.setOnClickListener {
            if (CurrentViewModel != null) {
                AppUtil.OpenBrowser(context!!, CurrentViewModel!!.BrowserPageUrl)
            }
        }
    }

    // Activity调用，喂数据给Fragment
    fun ApplyViewModel(model: IllustratorViewFragmentViewModel) {
        if (CURRENT_CAN_APPLY_VIEWMODEL) {

            binding.root.Illustrator2_ScrollView.scrollTo(0, 0)

            CurrentViewModel = model
            binding.model = CurrentViewModel

            Glide.with(context!!)
                .load(model.ItemImageUrl)
                .priority(Priority.IMMEDIATE)
                .into(LoadOriginalWorkDrawableToImageView)

            DATA_LOADED = true

            // Load Related Items.
            if (relatedImageListAdapter == null) {
                relatedImageListAdapter = RelatedImageListAdapter(model.RelatedItems, this::OnRelatedItemSelected)
                binding.root.Illustrator2_View_RelatedItems.adapter = relatedImageListAdapter
                binding.root.Illustrator2_View_RelatedItems.layoutManager = relatedImagGridLayoutManager
            } else {
                relatedImageListAdapter?.items = model.RelatedItems
                relatedImageListAdapter?.notifyDataSetChanged()
            }
        }
    }
}