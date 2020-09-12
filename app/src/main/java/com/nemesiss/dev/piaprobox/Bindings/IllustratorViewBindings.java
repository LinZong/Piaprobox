package com.nemesiss.dev.piaprobox.Bindings;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.nemesiss.dev.piaprobox.Misc.StaticResourcesMap;
import com.nemesiss.dev.piaprobox.R;
import com.nemesiss.dev.piaprobox.Service.GlideApp;
import com.nemesiss.dev.piaprobox.Service.HTMLParser;

public class IllustratorViewBindings {
    private static boolean fixThumbReference(ImageView imageView, String url) {
        if (url.matches("^th-.*")) {
            Integer resId;
            if ((resId = StaticResourcesMap.getDefaultThumbMaps().get(url)) != null) {
                imageView.setImageResource(resId);
                Log.d("IllustratorViewBindings", "已修正" + url + "到资源ID: " + resId);
            } else {
                imageView.setImageResource(R.drawable.thumb_empty);
                Log.d("IllustratorViewBindings", "修正失败: " + url + ", 重置到th-empty.");
            }
            return true;
        }
        return false;
    }

    @BindingAdapter({"bind:imageUrlWithPrefix"})
    public static void loadImage(ImageView imageView, String url) {
        if (null != url && !TextUtils.isEmpty(url) && !fixThumbReference(imageView, url)) {
            GlideApp.with(imageView.getContext())
                    .load(url)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .priority(Priority.HIGH)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(imageView);
        }
    }

    @BindingAdapter({"bind:imageUrlNoPrefix"})
    public static void loadImageNoPrefix(ImageView imageView, String url) {
        if (null != url && !TextUtils.isEmpty(url) && !fixThumbReference(imageView, url)) {
            GlideApp.with(imageView.getContext())
                    .load(HTMLParser.GetAlbumThumb(url))
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .priority(Priority.HIGH)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(imageView);
        }
    }

    @BindingAdapter({"bind:imageArtistAvatar"})
    public static void loadImageArtistAvatar(ImageView imageView, String url) {
        if (null != url && !TextUtils.isEmpty(url) && !fixThumbReference(imageView, url)) {
            imageView.post(() -> GlideApp.with(imageView.getContext())
                    .load(HTMLParser.GetAlbumThumb(url))
                    .transform(new RoundedCorners(imageView.getWidth() / 2))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .priority(Priority.HIGH)
                    .into(imageView));
        }
    }
}