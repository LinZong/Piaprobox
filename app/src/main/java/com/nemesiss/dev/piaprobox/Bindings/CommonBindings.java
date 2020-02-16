package com.nemesiss.dev.piaprobox.Bindings;

import android.databinding.BindingAdapter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nemesiss.dev.piaprobox.R;
import com.nemesiss.dev.piaprobox.View.Common.AutoWrapLayout;
import com.nemesiss.dev.piaprobox.View.Common.TextViewUtils;

public class CommonBindings {
    @BindingAdapter({"bind:textWithUrl"})
    public static void textWithUrl(TextView textView, String textMayWithUrl) {
        TextViewUtils.SetTextWithClickableUrl(textView, textMayWithUrl);
    }


    @BindingAdapter({"bind:createDetail"})
    public static void colorfulCreateDetail(AutoWrapLayout wrapper, String createDetailRaw) {
        if (!TextUtils.isEmpty(createDetailRaw)) {
            wrapper.removeAllViews();

            String[] detailParts = createDetailRaw.split(" \\| ");
            for (String detail :
                    detailParts) {
                int DelimiterPos = detail.indexOf('：');
                SpannableString text = new SpannableString(detail);
                ForegroundColorSpan tagColor = new ForegroundColorSpan(wrapper.getContext().getResources().getColor(R.color.TagSelectedBackground));
                if (DelimiterPos > 1) {
                    text.setSpan(tagColor, 0, DelimiterPos, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                TextView tv = new TextView(wrapper.getContext());
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, 6f);
                tv.setText(text);
                ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 12, 8);
                tv.setLayoutParams(lp);
                wrapper.addView(tv);
            }
        }
    }
}
