package org.zhgeaits.risv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangge on 2015/10/12.
 */
public class MultiPointBar extends View {

    private Context mContext;
    private Paint mPaint;

    private Map<Integer, Point> mPoints;
    private int mBarHeight;
    private int mBarWidth;

    public MultiPointBar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public MultiPointBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MultiPointBar(Context context, AttributeSet attrs, int defStyleAttr) {
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

        for (Point point : mPoints.values()) {
            mPaint.setColor(point.color);
            canvas.drawCircle(point.position + mBarHeight / 2, mBarHeight / 2, mBarHeight / 2, mPaint);
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
        mPoints = new HashMap<>();
        mBarHeight = DimenConverter.dip2px(mContext, 4);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void addPoint(int id, int position, int color) {
        Point range = new Point();
        range.position = position;
        range.color = color;
        mPoints.put(id, range);
        invalidate();
    }

    public void removePoint(int id) {
        mPoints.remove(id);
        invalidate();
    }

    /**
     * 设置bar的长度
     * @param barWidth
     */
    public void setBarWidth(int barWidth) {
        this.mBarWidth = barWidth;
    }

    public class Point {
        int position;
        int color;
    }

}
