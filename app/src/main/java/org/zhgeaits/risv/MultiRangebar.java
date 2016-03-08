/*
 * Copyright (C) 2016 Zhang Ge <zhgeaits@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zhgeaits.risv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangge on 15/9/27.
 *
 */
public class MultiRangebar extends View {

    private Context mContext;
    private Drawable mSelectedBar;

    private Map<Integer, Range> mRanges;
    private int mBarHeight;
    private int mBarWidth;

    public MultiRangebar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public MultiRangebar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MultiRangebar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mBarWidth == 0) {
            mBarWidth = MeasureSpec.getSize(widthMeasureSpec);
        }

        int width = measureDimension(mBarWidth, widthMeasureSpec);
        int height = measureDimension(mBarHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Range range : mRanges.values()) {
            mSelectedBar.setBounds(range.left, 0, range.right, mBarHeight);
            mSelectedBar.draw(canvas);
        }
    }

    protected int measureDimension(int defaultSize, int measureSpec) {

        int result;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        } else {
            result = defaultSize;
        }

        return result;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
        mRanges = new HashMap<>();
        mBarHeight = DimenConverter.dip2px(mContext, 2);
        mSelectedBar = getResources().getDrawable(R.drawable.multi_range_bg);
    }

    public void addRange(int id, int left, int right) {
        Range range = new Range();
        range.left = left;
        range.right = right;
        mRanges.put(id, range);
        invalidate();
    }

    public void removeRange(int id) {
        mRanges.remove(id);
        invalidate();
    }

    public void setBarWidth(int barWidth) {
        this.mBarWidth = barWidth;
    }

    public class Range {
        int left;
        int right;
    }
}
