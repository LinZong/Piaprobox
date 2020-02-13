package com.nemesiss.dev.piaprobox.Activity.Image.Bindings;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.nemesiss.dev.piaprobox.Service.HTMLParser;

public class IllustratorViewBindings {

    @BindingAdapter({"bind:imageUrlWithPrefix"})
    public static void loadImage(ImageView imageView, String url) {
        Log.d("IllustratorViewBindings", "被执行，URL is " + url);
        if(null != url && !TextUtils.isEmpty(url)) {
            Glide.with(imageView.getContext())
                    .load(url)
                    .priority(Priority.HIGH)
                    .into(imageView);
        }
    }
}
