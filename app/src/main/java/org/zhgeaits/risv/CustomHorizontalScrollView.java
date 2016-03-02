package org.zhgeaits.risv;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Created by zhangge on 2015/9/25.
 */
public class CustomHorizontalScrollView extends HorizontalScrollView {

    private boolean mRangeMoving = false;
    private boolean mFromUser = false;
    private long mFirstTouch = 0;
    private int mValidPointerId;
    private int mBarLength;//可见长度
    private int mImageBarLen;//图片的长度
    private int mSeekWidth;//整条bar的长度
    private int mTouchEventId = 12345;
    private OnScrollListener mListener;
    private Handler mHandler = new Handler() {

        private int lastX;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            View scroller = (View) msg.obj;
            if (msg.what == mTouchEventId) {
                if (lastX == scroller.getScrollX()) {
                    if (mListener != null && mFromUser == false) {
                        mListener.onScrollStop();
                    }
                } else {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(mTouchEventId, scroller), 5);
                    lastX = scroller.getScrollX();
                }
            }
        }
    };

    public CustomHorizontalScrollView(Context context) {
        super(context);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRangeMoving(boolean rangeMoving) {
        this.mRangeMoving = rangeMoving;
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.mListener = listener;
    }

    public void setBarLength(int len) {
        this.mBarLength = len;
    }

    public void setImageBarLen(int len) {
        this.mImageBarLen = len;
    }

    public void setSeekWidth(int len) {
        this.mSeekWidth = len;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mListener != null) {
            mListener.onScrollChanged(l, t, oldl, oldt, mFromUser);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mFromUser = true;
                mFirstTouch = System.currentTimeMillis();
                mValidPointerId = event.getPointerId(event.getActionIndex());
                break;
            case MotionEvent.ACTION_MOVE:
                mFromUser = true;
                break;
            case MotionEvent.ACTION_UP:
                mFromUser = false;
                mHandler.sendMessageDelayed(mHandler.obtainMessage(mTouchEventId, this), 5);

                //如果300毫秒之间就是一个点击事件
                if (event.getPointerId(event.getActionIndex()) == mValidPointerId && System.currentTimeMillis() - mFirstTouch <= 300) {
                    float posX = event.getX();
                    float blankWidth = (mSeekWidth - mImageBarLen) / 2.0f;
                    if (posX + getScrollX() <= mSeekWidth - blankWidth && posX + getScrollX() >= blankWidth) {
                        int distance = (int) (posX - (mBarLength / 2.0d));
                        if (mListener != null) {
                            mListener.onScrollChanged(getScrollX() + distance, 0, getScrollX(), 0, true);
                        }
                        this.smoothScrollTo(getScrollX() + distance, 0);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mRangeMoving) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    public interface OnScrollListener {
        void onScrollStop();

        void onScrollChanged(int l, int t, int oldl, int oldt, boolean fromUser);
    }
}
