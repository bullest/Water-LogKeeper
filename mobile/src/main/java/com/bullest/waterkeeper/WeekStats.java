package com.bullest.waterkeeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yunfezhang on 7/30/17.
 */

public class WeekStats extends View {
    int mWeeks;

    public WeekStats(Context context) {
        super(context);
    }

    public WeekStats(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.WeekStats, 0, 0);

        try {
            mWeeks = a.getInt(R.styleable.WeekStats_weeks, 3);
        } finally {
            a.recycle();
        }
    }

    public void update(){
        invalidate();
        requestLayout();
    }
}
