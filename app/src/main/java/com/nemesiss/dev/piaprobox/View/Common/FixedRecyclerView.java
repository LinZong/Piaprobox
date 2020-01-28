package com.nemesiss.dev.piaprobox.View.Common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class FixedRecyclerView extends RecyclerView {
    public FixedRecyclerView(@NonNull Context context) {
        super(context);
    }

    public FixedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean requestCancelDisallowInterceptTouchEvent = getScrollState() == SCROLL_STATE_SETTLING;
        boolean consumed = super.onInterceptTouchEvent(event);
        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if( requestCancelDisallowInterceptTouchEvent ){
                    getParent().requestDisallowInterceptTouchEvent(false);

                    // only if it touched the top or the bottom. Thanks to @Sergey's answer.
                    if (!canScrollVertically(-1) || !canScrollVertically(1)) {
                        // stop scroll to enable child view to get the touch event
                        stopScroll();
                        // do not consume the event
                        return false;
                    }
                }
                break;
        }

        return consumed;
    }
}
