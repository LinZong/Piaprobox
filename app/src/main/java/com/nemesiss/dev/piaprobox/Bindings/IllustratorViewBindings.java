package com.nemesiss.dev.piaprobox.Bindings;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.nemesiss.dev.piaprobox.Service.HTMLParser;

public class IllustratorViewBindings {

    @BindingAdapter({"bind:imageUrlWithPrefix"})
    public static void loadImage(ImageView imageView, String url) {
        if (null != url && !TextUtils.isEmpty(url)) {
            Glide.with(imageView.getContext())
                    .load(url)
                    .priority(Priority.HIGH)
                    .into(imageView);
        }
    }

    @BindingAdapter({"bind:imageUrlNoPrefix"})
    public static void loadImageNoPrefix(ImageView imageView, String url) {
        if (null != url && !TextUtils.isEmpty(url)) {
            Glide.with(imageView.getContext())
                    .load(HTMLParser.GetAlbumThumb(url))
                    .priority(Priority.HIGH)
                    .into(imageView);
        }
    }
}