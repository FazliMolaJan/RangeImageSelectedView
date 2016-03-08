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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhangge on 2015/9/24.
 */
public class RangeImageSelectedView extends View {

    public static final int HINT_NONE = 1;//无效点击状态
    public static final int HINT_DIRECTION_LEFT = 2;//点击左边拖动
    public static final int HINT_DIRECTION_RIGHT = 3;//点击右边拖动状态
    public static final int CHANGE_TYPE_SCALE = 1;//区间修改的类型，修改区间的大小
    public static final int CHANGE_TYPE_MOVE = 2;//区间修改的类型，移动区间

    private Drawable mLeftSelectBgDrawable;
    private Drawable mLeftSelectArrowDrawable;
    private Drawable mRightSelectBgDrawable;
    private Drawable mRightSelectArrowDrawable;
    private Drawable mPointArrowDrawable;

    private Context mContext;
    private CustomHorizontalScrollView mHorizontalScrollView;
    private Paint mPaint;
    private Paint mOutsidePaint;

    private int id;
    private int mSeekWidth;//整条bar的宽度，包括前后空白
    private int mSelectWidth;//点击按钮的宽度
    private int mSelectHeight;//点击按钮的高度，就是整条bar的高度了
    private int mArrowWidth;//箭头的宽度
    private int mArrowHeight;//箭头的高度
    private int mPointArrowWidth;//3个点的宽度
    private int mPointArrowHeight;//3个点的高度
    private int mStartLeft;//画左边按钮的的起始位置
    private int mWindowLen;//窗口的宽度
    private int mBorder;//窗口的边框宽度
    private int mHitState;//点击的状态
    private int mValidPointerId;//有效的多点触控点Id
    private int mLeftBlankWidth;//左边空白的宽度
    private int mRightBlankWidth;//右边空白的宽度
    private int mImageListLen;//图片的长度，就是bar的宽度，不包括前后空白
    private int mSeekbarVisibleWidth;//bar的可见宽度
    private int mMinWindowLen = 0;//窗口的最小宽度，默认是0
    private int mMaxWindowLen = 0;//窗口的最大宽度，弹幕初始值最大的宽度
    private float mLastX;//上次点击的X坐标
    private float mLastY;//上次点击的Y坐标
    private boolean mCanChange = true;//是否可以修改
    private int mChangeType = 1;//区间修改的类型
    private boolean mLeftChange = false;//弹幕框移动到最右边以后开始变小

    private OnRangeChangedListener mRangeChangedListener;

    public RangeImageSelectedView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public RangeImageSelectedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public RangeImageSelectedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
        mSelectWidth = DimenConverter.dip2px(context, 16);
        mSelectHeight = DimenConverter.dip2px(context, 44);
        mArrowWidth = DimenConverter.dip2px(context, 7);
        mArrowHeight = DimenConverter.dip2px(context, 15);
        mPointArrowWidth = DimenConverter.dip2px(context, 4);
        mPointArrowHeight = DimenConverter.dip2px(context, 19);
        mBorder = DimenConverter.dip2px(context, 4);
        mStartLeft = 0;
        mWindowLen = 10;

        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#ff8900"));
        mPaint.setAntiAlias(true);

        mOutsidePaint = new Paint();
        mOutsidePaint.setColor(Color.parseColor("#8A000000"));
        mOutsidePaint.setAntiAlias(true);

        mLeftSelectBgDrawable = getContext().getResources().getDrawable(R.drawable.img_bar_left);
        mLeftSelectArrowDrawable = getResources().getDrawable(R.drawable.left_arrow_selector);

        mRightSelectBgDrawable = getResources().getDrawable(R.drawable.img_bar_right);
        mRightSelectArrowDrawable = getResources().getDrawable(R.drawable.right_arrow_selector);

        mPointArrowDrawable = getResources().getDrawable(R.mipmap.point_arrow);
    }

    public void setOnRangeChangedListener(OnRangeChangedListener listener) {
        this.mRangeChangedListener = listener;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * 设置最小的窗口宽度
     * @param minWindowLen
     */
    public void setMinWindowLen(int minWindowLen) {
        this.mMinWindowLen = minWindowLen;
    }

    /**
     * 设置最大的窗口宽度
     * @param maxWindowlen
     */
    public void setMaxWindowLen(int maxWindowlen) {
        this.mMaxWindowLen = maxWindowlen;
    }

    /**
     * 获取当前的最大窗口值
     * @return
     */
    public int getMaxWindowLen() {
        return this.mMaxWindowLen;
    }

    /**
     * 设置左边绘制的起点位置
     * @param startLeft
     */
    public void setStartLeft(int startLeft) {
        this.mStartLeft = startLeft;
    }

    /**
     * 设置左边选择的按钮样式
     * @param leftChange
     */
    public void setLeftChange(boolean leftChange) {
        mLeftChange = leftChange;
        invalidate();
    }

    /**
     * 设置窗口的起始位置
     * @param windowStart
     */
    public void setWindowStart(int windowStart) {
        this.mStartLeft = windowStart - mSelectWidth;
    }

    /**
     * 设置选择窗口的长度
     * @param windowLen
     */
    public void setWindowLen(int windowLen) {
        this.mWindowLen = windowLen;
    }

    /**
     * 获取窗口起始位置
     * @return
     */
    public int getWindowStart() {
        return mStartLeft + mSelectWidth;
    }

    /**
     * 获取窗口长度
     * @return
     */
    public int getWindowLen() {
        return mWindowLen;
    }

    /**
     * 设置左边的空白长度
     * @param blankWidth
     */
    public void setLeftBlankWidth(int blankWidth) {
        this.mLeftBlankWidth = blankWidth;
    }

    /**
     * 设置右边的空白宽度
     * @param blankWidth
     */
    public void setRightBlankWidth(int blankWidth) {
        this.mRightBlankWidth = blankWidth;
    }

    /**
     * 设置可用bar的长度
     * @param imageLen
     */
    public void setImageListLen(int imageLen) {
        this.mImageListLen = imageLen;
    }

    public void setHorizontalScrollView(CustomHorizontalScrollView scrollView) {
        this.mHorizontalScrollView = scrollView;
    }

    /**
     * 设置整个bar的宽度
     * @param width
     */
    public void setSeekWidth(int width) {
        this.mSeekWidth = width;
    }

    /**
     * 设置bar的可见宽度
     * @param width
     */
    public void setSeekbarVisiblewidth(int width) {
        this.mSeekbarVisibleWidth = width;
    }

    /**
     * 画外面的蒙层
     *
     * @param canvas
     */
    private void drawOutsideLayer(Canvas canvas) {
        int radius = DimenConverter.dip2px(mContext, 3);
        RectF rectfl = new RectF(0, 0, mStartLeft + mSelectWidth / 2, mSelectHeight);
        RectF rectfr = new RectF(mStartLeft + mWindowLen + mSelectWidth + mSelectWidth / 2, 0, canvas.getWidth(), mSelectHeight);
        canvas.drawRoundRect(rectfl, radius, radius, mOutsidePaint);
        canvas.drawRoundRect(rectfr, radius, radius, mOutsidePaint);
    }

    /**
     * 画左边的3个点选择框
     * @param canvas
     */
    private void drawLeftPointSelect(Canvas canvas) {
        mLeftSelectBgDrawable.setBounds(mStartLeft, 0, mStartLeft + mSelectWidth, mSelectHeight);
        mLeftSelectBgDrawable.draw(canvas);
        mPointArrowDrawable.setBounds((mSelectWidth - mPointArrowWidth) / 2 + mStartLeft, (mSelectHeight - mPointArrowHeight) / 2,
                (mSelectWidth - mPointArrowWidth) / 2 + mPointArrowWidth + mStartLeft, (mSelectHeight - mPointArrowHeight) / 2 + mPointArrowHeight);
        mPointArrowDrawable.draw(canvas);
    }

    /**
     * 画右边的3个点选择框
     * @param canvas
     */
    private void drawRightPointSelect(Canvas canvas) {
        mRightSelectBgDrawable.setBounds(mStartLeft + mSelectWidth + mWindowLen, 0, mStartLeft + mSelectWidth + mWindowLen + mSelectWidth, mSelectHeight);
        mRightSelectBgDrawable.draw(canvas);
        mPointArrowDrawable.setBounds((mSelectWidth - mPointArrowWidth) / 2 + mStartLeft + mSelectWidth + mWindowLen, (mSelectHeight - mPointArrowHeight) / 2,
                (mSelectWidth - mPointArrowWidth) / 2 + mStartLeft + mSelectWidth + mWindowLen + mPointArrowWidth, (mSelectHeight - mPointArrowHeight) / 2 + mPointArrowHeight);
        mPointArrowDrawable.draw(canvas);
    }

    /**
     * 画左边拖动按钮
     *
     * @param canvas
     */
    private void drawLeftSelect(Canvas canvas) {
        mLeftSelectBgDrawable.setBounds(mStartLeft, 0, mStartLeft + mSelectWidth, mSelectHeight);
        mLeftSelectBgDrawable.draw(canvas);
        mLeftSelectArrowDrawable.setBounds((mSelectWidth - mArrowWidth) / 2 + mStartLeft, (mSelectHeight - mArrowHeight) / 2,
                (mSelectWidth - mArrowWidth) / 2 + mArrowWidth + mStartLeft, (mSelectHeight - mArrowHeight) / 2 + mArrowHeight);
        mLeftSelectArrowDrawable.draw(canvas);
    }

    /**
     * 画中间的两条边框
     *
     * @param canvas
     */
    private void drawCenterRect(Canvas canvas) {
        canvas.drawRect(mStartLeft + mSelectWidth / 2, 0, mStartLeft + mSelectWidth + mWindowLen + mSelectWidth / 2, mBorder, mPaint);
        canvas.drawRect(mStartLeft + mSelectWidth / 2, mSelectHeight - mBorder, mStartLeft + mSelectWidth + mWindowLen + mSelectWidth / 2, mSelectHeight, mPaint);
    }

    /**
     * 画右边的拖动按钮
     *
     * @param canvas
     */
    private void drawRightSelect(Canvas canvas) {
        mRightSelectBgDrawable.setBounds(mStartLeft + mSelectWidth + mWindowLen, 0, mStartLeft + mSelectWidth + mWindowLen + mSelectWidth, mSelectHeight);
        mRightSelectBgDrawable.draw(canvas);
        mRightSelectArrowDrawable.setBounds((mSelectWidth - mArrowWidth) / 2 + mStartLeft + mSelectWidth + mWindowLen, (mSelectHeight - mArrowHeight) / 2,
                (mSelectWidth - mArrowWidth) / 2 + mStartLeft + mSelectWidth + mWindowLen + mArrowWidth, (mSelectHeight - mArrowHeight) / 2 + mArrowHeight);
        mRightSelectArrowDrawable.draw(canvas);
    }

    /**
     * 设置区间是否可以修改
     * @param canChange
     */
    public void setCanChange(boolean canChange) {
        this.mCanChange = canChange;
    }

    /**
     * 设置区间的修改类型
     * @param type
     */
    public void setChangeType(int type) {
       this.mChangeType = type;
    }

    /**
     * 获取区间的类型
     * @return
     */
    public int getChangeType() {
        return this.mChangeType;
    }

    /**
     * 判断点击的位置
     *
     * @param x
     * @param y
     * @return
     */
    private int getHit(float x, float y) {

        if (x >= mStartLeft && x <= mStartLeft + mSelectWidth) {
            return HINT_DIRECTION_LEFT;
        }
        if (x >= mStartLeft + mSelectWidth + mWindowLen && x <= mStartLeft + mSelectWidth + mWindowLen + mSelectWidth) {
            return HINT_DIRECTION_RIGHT;
        }

        return HINT_NONE;
    }

    /**
     * 移动窗口的大小
     * @param hintState
     * @param distance
     */
    private void handleMotion(int hintState, int distance) {

        if (hintState == HINT_DIRECTION_LEFT) {
            int startLeft = mStartLeft + distance;
            int windowLen = mWindowLen - distance;
            if(windowLen >= mMinWindowLen && startLeft + mSelectWidth >= mLeftBlankWidth && startLeft >= mHorizontalScrollView.getScrollX()) {
                mWindowLen = windowLen;
                mStartLeft = startLeft;
                invalidate();
                if(mRangeChangedListener != null) {
                    int leftSelectPostion = mStartLeft + mSelectWidth;
                    mRangeChangedListener.onRangeChanging(HINT_DIRECTION_LEFT, leftSelectPostion);
                }
            }
        } else if (hintState == HINT_DIRECTION_RIGHT) {
            int windowLen = mWindowLen + distance;
            int rightStart = mStartLeft + mSelectWidth + windowLen;
            //如果窗口宽度大于mMinWindowLen，或者没有超出右边范围就移动
            if (windowLen >= mMinWindowLen && rightStart <= mSeekWidth - mRightBlankWidth && (rightStart + mSelectWidth) <= (mHorizontalScrollView.getScrollX() + mSeekbarVisibleWidth)) {
                mWindowLen = windowLen;
                invalidate();
                if(mRangeChangedListener != null) {
                    mRangeChangedListener.onRangeChanging(HINT_DIRECTION_RIGHT, rightStart);
                }
            }
        }
    }

    /**
     * 移动区间
     * @param hintState
     * @param distance
     */
    private void handleMoveMotion(int hintState, int distance) {
        int startLeft = mStartLeft + distance;
        int rightStart = startLeft + mSelectWidth + mWindowLen;

        if (hintState == HINT_DIRECTION_LEFT) {

            //如果触控点超出可视范围或者进度条最左端就不让移动了
            if(startLeft + mSelectWidth < mLeftBlankWidth || startLeft < mHorizontalScrollView.getScrollX()) {
                mLeftChange = false;
                invalidate();
                return;
            }

            //向左拖动
            if(distance < 0) {
                //如果窗口没缩小的情况，则整体拖动
                if(mWindowLen == mMaxWindowLen) {
                    mLeftChange = false;
                    mStartLeft = startLeft;
                } else {//窗口被缩小的情况，则放大窗口
                    mLeftChange = true;
                    int windowLen = mWindowLen - distance;
                    mWindowLen = windowLen;
                    if(mWindowLen > mMaxWindowLen) {
                        mWindowLen = mMaxWindowLen;
                    }
                    mStartLeft = startLeft;
                }
            } else if(distance > 0) {//向右拖动
                //如果已经顶到最右端了，并且窗口为0了，则不让拖动了
                if(mWindowLen <= 0 && rightStart >= mSeekWidth - mRightBlankWidth) {
                    return;
                }

                //如果已经顶到最右端了则缩小窗口，否则整体移动
                //只能右边移动，不能缩小窗口
                if(rightStart >= mSeekWidth - mRightBlankWidth) {
                    return;
                    /*int windowLen = mWindowLen - distance;
                    mWindowLen = windowLen;
                    if(mWindowLen > mMaxWindowLen) {
                        mWindowLen = mMaxWindowLen;
                    } else if(mWindowLen < 0) {
                        mWindowLen = 0;
                    }
                    mLeftChange = true;
                    mStartLeft = startLeft;*/
                } else {
                    mLeftChange = false;
                    mStartLeft = startLeft;
                }
            }

            invalidate();

            if(mRangeChangedListener != null) {
                int leftSelectPostion = mStartLeft + mSelectWidth;
                mRangeChangedListener.onRangeChanging(HINT_DIRECTION_LEFT, leftSelectPostion);
            }

        } else if (hintState == HINT_DIRECTION_RIGHT) {

            //现在右边只有完整显示的情况下可以拖动，其他时候无法点击
            if(mWindowLen < mMaxWindowLen) {
                return;
            }

            //如果触控点超出可视范围或者进度条最左端就不让移动了
            if(rightStart > mSeekWidth - mRightBlankWidth || (rightStart + mSelectWidth) > (mHorizontalScrollView.getScrollX() + mSeekbarVisibleWidth)) {
                return;
            }

            //向右拖动
            if(distance > 0) {
                mLeftChange = false;
                //如果窗口没缩小的情况，则整体拖动
                if(mWindowLen == mMaxWindowLen) {
                    mStartLeft = startLeft;
                } else {//窗口被缩小的情况，则放大窗口
//                    mLeftChange = true;
//                    int windowLen = mWindowLen + distance;
//                    mWindowLen = windowLen;
//                    if(mWindowLen > mMaxWindowLen) {
//                        mWindowLen = mMaxWindowLen;
//                    }
                    return;
                }
            } else if(distance < 0) {//向左拖动
                mLeftChange = false;
                //如果已经顶到最左端了，并且窗口为0了，则不让拖动了
                if(mWindowLen <= 0 && startLeft + mSelectWidth <= mLeftBlankWidth) {
                    return;
                }

                //如果已经顶到最左端了则缩小窗口，否则整体移动
                //如果顶到最左端以后不允许缩小了，无法拖动。
                if(startLeft + mSelectWidth <= mLeftBlankWidth) {
                    /*int windowLen = mWindowLen + distance;
                    mWindowLen = windowLen;
                    if(mWindowLen > mMaxWindowLen) {
                        mWindowLen = mMaxWindowLen;
                    } else if(mWindowLen < 0) {
                        mWindowLen = 0;
                    }*/
                    return;
                } else {
                    mStartLeft = startLeft;
                }
            }

            invalidate();

            if(mRangeChangedListener != null) {
                mRangeChangedListener.onRangeChanging(HINT_DIRECTION_RIGHT, rightStart);
            }
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mSeekWidth == 0) {
            mSeekWidth = MeasureSpec.getSize(widthMeasureSpec);
        }

        int width = measureDimension(mSeekWidth, widthMeasureSpec);
        int height = measureDimension(mSelectHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawOutsideLayer(canvas);
        if(mChangeType == CHANGE_TYPE_SCALE || (mChangeType == CHANGE_TYPE_MOVE && mLeftChange)) {
            drawLeftSelect(canvas);
        } else {
            drawLeftPointSelect(canvas);
        }
        drawCenterRect(canvas);
        if(mChangeType == CHANGE_TYPE_SCALE) {
            drawRightSelect(canvas);
        }  else if(mChangeType == CHANGE_TYPE_MOVE) {
            drawRightPointSelect(canvas);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mHorizontalScrollView != null) {
            mHorizontalScrollView.setRangeMoving(false);
        }
        if(!mCanChange) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHitState = getHit(event.getX(), event.getY());
                if (mHitState != HINT_NONE) {
                    mLastX = event.getX();
                    mLastY = event.getY();
                    mValidPointerId = event.getPointerId(event.getActionIndex());
                    if (mHorizontalScrollView != null) {
                        mHorizontalScrollView.setRangeMoving(true);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mHitState = HINT_NONE;
                if (mHorizontalScrollView != null) {
                    mHorizontalScrollView.setRangeMoving(false);
                }
                if(mRangeChangedListener != null) {
                    mRangeChangedListener.onRangeChangeFinished();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerId(event.getActionIndex()) == mValidPointerId && mHitState != HINT_NONE) {
                    if(mChangeType == CHANGE_TYPE_SCALE) {
                        handleMotion(mHitState, (int) (event.getX() - mLastX));
                    } else if(mChangeType == CHANGE_TYPE_MOVE) {
                        handleMoveMotion(mHitState, (int) (event.getX() - mLastX));
                    }
                    mLastX = event.getX();
                    mLastY = event.getY();
                    if (mHorizontalScrollView != null) {
                        mHorizontalScrollView.setRangeMoving(true);
                    }
                }
                break;
        }

        return true;
    }

    public interface OnRangeChangedListener {
        void onRangeChanging(int direction, int postion);
        void onRangeChangeFinished();
    }
}
