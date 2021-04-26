package com.nemesiss.dev.piaprobox.view.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class AutoWrapLayout extends ViewGroup {

    private ArrayList<int[]> childPosition;

    private void Init() {
        childPosition = new ArrayList<>();
    }

    public AutoWrapLayout(Context context) {
        super(context);
        Init();
    }

    public AutoWrapLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();

    }

    public AutoWrapLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init();
    }

    public AutoWrapLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        final int childCount = getChildCount();
        int left = 0, top = 0, totalWidth = 0, totalHeight = 0;

        for (int i = 0; i < childCount; ++i) {
            View child = getChildAt(i);

            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            if (i == 0) {
                // 第一行，总体行高是这一行的子View两边Margin + 自己的高度就够了
                totalHeight = lp.topMargin + child.getMeasuredHeight() + lp.bottomMargin;
            }
            if (left + lp.leftMargin + child.getMeasuredWidth() + lp.rightMargin > getMeasuredWidth()) {
                left = 0;
                top += lp.topMargin + child.getMeasuredHeight() + lp.bottomMargin;
                totalHeight += lp.topMargin + child.getMeasuredHeight() + lp.bottomMargin;
            }
            int cleft = left + lp.leftMargin, ctop = top + lp.topMargin, cright = cleft + child.getMeasuredWidth(), cbottom = ctop + child.getMeasuredHeight();
            childPosition.add(new int[]{cleft, ctop, cright, cbottom});
            left += lp.leftMargin + child.getMeasuredWidth() + lp.rightMargin;

            if (left > totalWidth) {
                totalWidth = left;
            }
        }
        int height = 0;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = totalHeight;
        }

        int width = 0;
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            width = totalWidth;
        }

        setMeasuredDimension(width, height);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int childCount = getChildCount();
        for (int j = 0; j < childCount; j++) {
            View child = getChildAt(j);
            int[] position = childPosition.get(j);
            child.layout(position[0], position[1], position[2], position[3]);
        }
    }
}