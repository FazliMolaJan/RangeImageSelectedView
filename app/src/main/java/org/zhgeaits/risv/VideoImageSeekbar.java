package org.zhgeaits.risv;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangge on 2015/9/24.
 */
public class VideoImageSeekbar extends RelativeLayout {

    private Context mContext;
    private View mRoot;
    private LayoutInflater mInflater;
    private ImageView mSelectbar;
    private LinearLayout mList;
    private RangeImageSelectedView mRangeView;
    private CustomHorizontalScrollView mHorizontalScrollView;
    private RelativeLayout mSeekbarContainer;
    private RelativeLayout.LayoutParams mSelectbarParams;//选择进度条的布局参数
    private MultiRangebar mRangebar;
    private MultiPointBar mPointbar;

    private Map<Integer, RangeImageSelectedView> mCacheRangeView;
    private SparseIntArray mDanmuColor;

    private int mImageBarLen;//图片的长度，进度条真正意义的像素长度
    private int mLeftBlankWidth;//左边空白处的宽度
    private int mRightBlankWidth;//右边空白出的宽度
    private int mSelectbarWidth;//中间竖条的宽度
    private int mCurrentProgress;//光标所在的位置，像素
    private int mMaxProgress = 100;//进度条的长度，不包含前后的空白，默认是100
    private int mMinSelectProgress = 0;//最少的窗口选择宽度，默认是0
    private int mImageHeight;//图片的宽度
    private int mImageWidth;//图片的高度
    private double mLastImageWidthPercent = 1.0d;//最后一张图片的宽度的百分比

    private OnSeekbarChangedListener mProgressChangeListener;//滚动的监听器

    public VideoImageSeekbar(Context context) {
        super(context);
    }

    public VideoImageSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public VideoImageSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mRoot = mInflater.inflate(R.layout.layout_image_seekbar, this, true);
        mCacheRangeView = new HashMap<>();

        mList = (LinearLayout) mRoot.findViewById(R.id.video_image_seekbar_list);
        mSelectbar = (ImageView) mRoot.findViewById(R.id.video_image_seekbar_selectbar);
        mHorizontalScrollView = (CustomHorizontalScrollView) mRoot.findViewById(R.id.video_image_horizontal_scrollview);
        mSeekbarContainer = (RelativeLayout) mRoot.findViewById(R.id.video_image_seekbar_container);

        mSelectbarParams = (LayoutParams) mSelectbar.getLayoutParams();

        mHorizontalScrollView.setOnScrollListener(new CustomHorizontalScrollView.OnScrollListener() {

            @Override
            public void onScrollStop() {
                mCurrentProgress = mHorizontalScrollView.getScrollX() + mSelectbar.getLeft();
                if (mProgressChangeListener != null) {
                    int progress = (mCurrentProgress - mLeftBlankWidth) * mMaxProgress / mImageBarLen;
                    mProgressChangeListener.onProgressChanged(progress, true);
                }
            }

            @Override
            public synchronized void onScrollChanged(int l, int t, int oldl, int oldt, boolean fromUser) {
                mCurrentProgress = l + mSelectbar.getLeft();
                if (mProgressChangeListener != null) {
                    int progress = (mCurrentProgress - mLeftBlankWidth) * mMaxProgress / mImageBarLen;
                    mProgressChangeListener.onProgressChanged(progress, fromUser);
                }
            }
        });
    }

    public void setOnProgressChangeListener(OnSeekbarChangedListener listener) {
        this.mProgressChangeListener = listener;
    }

    /**
     * 设置最大的进度值，一般设置为100
     *
     * @param maxProgress
     */
    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    /**
     * 获取当前的进度值，参考值是MaxProgress, 即返回值是0-100之间
     *
     * @return
     */
    public double getCurrentProgress() {
        double progress = (double) (mCurrentProgress - mLeftBlankWidth) * mMaxProgress / (double) mImageBarLen;
        return progress;
    }

    /**
     * 设置弹幕的颜色缓存
     *
     * @param danmuColor
     */
    public void setDanmuColor(SparseIntArray danmuColor) {
        mDanmuColor = danmuColor;
    }

    /**
     * 设置当前进度
     *
     * @param progress 0 - 100之间的百分比
     */
    public synchronized void setProgress(double progress) {

        //计算光标所在的像素位置
        int newProgress = (int) (progress * mImageBarLen / mMaxProgress + mLeftBlankWidth);

        if (newProgress == mCurrentProgress) {
            return;
        }

        //滚动到当前进度位置
        mHorizontalScrollView.smoothScrollTo(mHorizontalScrollView.getScrollX() + (newProgress - mCurrentProgress), 0);
    }

    /**
     * 设置最小的选择区间
     *
     * @param minWindowLen
     */
    public void setMinWindowLen(int minWindowLen) {
        this.mMinSelectProgress = minWindowLen;
    }

    /**
     * 获取当前的rang id
     *
     * @return
     */
    public int getCurrentRangeId() {
        if (mRangeView != null) {
            return mRangeView.getId();
        }
        return -1;
    }

    /**
     * 判断当前是否有选择框显示，参看invisibleRangeView方法，mRangeView为null的时候没有显示
     * 其他情况添加或者visible以后都不为空
     *
     * @return
     */
    public boolean isRangeViewVisible() {
        if (mRangeView != null) {
            return true;
        }
        return false;
    }

    /**
     * 获取当前选择框的类型，是弹幕的还是标签的。
     *
     * @return
     */
    public int getCurrentRangeType() {
        if (mRangeView != null) {
            return mRangeView.getChangeType();
        }
        return -1;
    }

    /**
     * 设置最后一张图片宽度的百分比
     *
     * @param percent
     */
    public void setLastImageWidthPercent(double percent) {
        mLastImageWidthPercent = percent;
    }

    private void setImageList0(List<Drawable> lists) {
        mList.removeAllViews();

        mImageHeight = DimenConverter.dip2px(mContext, 36);
        mImageWidth = DimenConverter.dip2px(mContext, 27);
        mSelectbarWidth = DimenConverter.dip2px(mContext, 4);
        mLeftBlankWidth = mSelectbar.getLeft();
        mRightBlankWidth = mLeftBlankWidth + mSelectbarWidth;
        mCurrentProgress = mLeftBlankWidth;

        View leftBlank = new View(mContext);
        ViewGroup.LayoutParams leftParam = new RelativeLayout.LayoutParams(mLeftBlankWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        leftBlank.setLayoutParams(leftParam);
        mList.addView(leftBlank);

        mImageBarLen = 0;
        for (int i = 0; i < lists.size(); i++) {

            ImageView imageView = new ImageView(mContext);

            int destWidth = mImageWidth;
            if (i == lists.size() - 1) {
                destWidth = (int) (mImageWidth * mLastImageWidthPercent);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(destWidth, mImageHeight);
            params.gravity = Gravity.CENTER_VERTICAL;
            imageView.setLayoutParams(params);
            imageView.setBackgroundDrawable(lists.get(i));
            mList.addView(imageView);
            mImageBarLen += destWidth;
        }

        View rightBlank = new View(mContext);
        ViewGroup.LayoutParams rightParam = new RelativeLayout.LayoutParams(mRightBlankWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        rightBlank.setLayoutParams(rightParam);
        mList.addView(rightBlank);

        mHorizontalScrollView.setBarLength(getWidth());
        mHorizontalScrollView.setImageBarLen(mImageBarLen);
        mHorizontalScrollView.setSeekWidth(mImageBarLen + mLeftBlankWidth + mRightBlankWidth);
    }

    public void setImageListPath(List<String> paths) {
        List<Drawable> lists = new ArrayList<>();
        for(int i = 0; i < paths.size(); i ++) {
            Drawable drawable = BitmapDrawable.createFromPath(paths.get(i));
            lists.add(drawable);
        }
        setImageList0(lists);
    }

    public void setImageListIds(List<Integer> ids) {
        List<Drawable> lists = new ArrayList<>();
        for(int i = 0; i < ids.size(); i ++) {
            Drawable drawable = getResources().getDrawable(ids.get(i));
            lists.add(drawable);
        }
        setImageList0(lists);
    }

    /**
     * 设置左边选择按钮
     * @param leftChange
     */
    public void setLeftChange(boolean leftChange) {
        if (mRangeView == null) {
            return;
        }

        if(mRangeView.getChangeType() == RangeImageSelectedView.CHANGE_TYPE_MOVE) {
            mRangeView.setLeftChange(leftChange);
        }
    }

    /**
     * 修改当前的rangview宽度
     *
     * @param selectProgress
     */
    public void setCurrentRangeViewLen(int selectProgress) {
        if (mRangeView == null) {
            return;
        }

        int windowLen = selectProgress * mImageBarLen / mMaxProgress;
        mRangeView.setWindowLen(windowLen);
        mRangeView.requestLayout();
    }

    /**
     * 修改当前rangeview的起始进度
     *
     * @param startProgress
     */
    public void setCurrentRangeViewStart(double startProgress) {
        if (mRangeView == null) {
            return;
        }

        int windowStart = (int) (startProgress * mImageBarLen / mMaxProgress + mLeftBlankWidth);
        mRangeView.setWindowStart(windowStart);
        mRangeView.requestLayout();
    }

    /**
     * 获取当前rangeView的起始进度
     * @return 参考值是MaxProgress, 即返回值是0-100之间
     */
    public double getCurrentRangeViewStart() {
        if (mRangeView == null) {
            return -1;
        }

        int windowStart = mRangeView.getWindowStart();
        double startProgress = (windowStart - mLeftBlankWidth) * mMaxProgress / mImageBarLen;
        return startProgress;
    }

    /**
     * 获取当前窗口最大值的百分比，0-100之间
     * @return
     */
    public double getCurrentMaxWindowProgress() {
        if (mRangeView != null) {
            return mRangeView.getMaxWindowLen() * mMaxProgress / mImageBarLen;
        }
        return 0;
    }

    /**
     * 获取当前的最大窗口值
     *
     * @return
     */
    public int getCurrentMaxWindowLen() {
        if (mRangeView != null) {
            return mRangeView.getMaxWindowLen();
        }
        return 0;
    }

    /**
     * 设置当前选择框的最大值
     *
     * @param selectProgress 传值是0-100之间的百分比
     * @return
     */
    public void setCurrentMaxWindowLen(int selectProgress) {
        int windowLen = selectProgress * mImageBarLen / mMaxProgress;
        if (mRangeView != null) {
            mRangeView.setMaxWindowLen(windowLen);
        }
    }

    /**
     * 删除选择窗口
     *
     * @param id
     */
    public void removeRangeView(int id) {
        mSeekbarContainer.removeView(mRangeView);
        mCacheRangeView.remove(id);
        mRangeView = null;
        if (mRangebar != null) {
            mRangebar.removeRange(id);
        }
        if (mPointbar != null) {
            mPointbar.removePoint(id);
        }
    }

    /**
     * 隐藏选择窗口，并显示选择进度条
     *
     * @param id
     */
    public void invisibleRangeView(int id) {
        mSeekbarContainer.removeView(mRangeView);

        if (RangeImageSelectedView.CHANGE_TYPE_MOVE == getCurrentRangeType()) {
            if (mDanmuColor != null) {
                addDanmuPoint(id, mDanmuColor.get(id));
            }
        } else {
            addLabelRange(id);
        }

        mRangeView = null;
    }

    /**
     * 添加标签的选择范围
     *
     * @param id
     */
    public void addLabelRange(int id) {
        if (mRangebar == null) {
            mRangebar = new MultiRangebar(mContext);
            mRangebar.setBarWidth(mList.getWidth());
            RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, R.id.video_image_seekbar_list);
            params.topMargin = DimenConverter.dip2px(mContext, 1);
            mRangebar.setLayoutParams(params);
            mSeekbarContainer.addView(mRangebar);
        }
        mRangebar.addRange(id, mRangeView.getWindowStart(), mRangeView.getWindowStart() + mRangeView.getWindowLen());
    }

    /**
     * 显示弹幕的点
     *
     * @param id
     * @param color
     */
    public void addDanmuPoint(int id, int color) {
        if (mPointbar == null) {
            mPointbar = new MultiPointBar(mContext);
            mPointbar.setBarWidth(mList.getWidth());
            RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ABOVE, R.id.video_image_seekbar_list);
            mPointbar.setLayoutParams(params);
            mSeekbarContainer.addView(mPointbar);
        }
        mPointbar.addPoint(id, mRangeView.getWindowStart(), color);
    }

    /**
     * 显示选择窗口
     *
     * @param id
     */
    public void visibleRangeView(int id) {
        if (mRangeView != null && mRangeView.getId() == id) {
            return;
        }

        if (mRangeView != null) {
            invisibleRangeView(mRangeView.getId());
        }

        RangeImageSelectedView rangeView = mCacheRangeView.get(id);
        if (rangeView != null) {
            mRangeView = rangeView;

            mSeekbarContainer.addView(mRangeView);
            //如果是标签，则需要去掉底部的选中范围条，如果是弹幕，则去掉点
            if (RangeImageSelectedView.CHANGE_TYPE_SCALE == getCurrentRangeType()) {
                mRangebar.removeRange(id);
            } else if (RangeImageSelectedView.CHANGE_TYPE_MOVE == getCurrentRangeType()) {
                mPointbar.removePoint(id);
            }

            //广播进度条
            if (mProgressChangeListener != null) {
                //通知进度条的范围
                double startProgress = (double) (mRangeView.getWindowStart() - mLeftBlankWidth) / (double) mImageBarLen;
                double selectProgress = (double) mRangeView.getWindowLen() / (double) mImageBarLen;
                mProgressChangeListener.onRangeChanged(mRangeView.getId(), startProgress, selectProgress);
            }
        }
    }

    /**
     * 添加标签的选择框
     *
     * @param id
     * @param startProgress
     * @param selectProgress
     */
    public void addLabelRangeView(int id, double startProgress, int selectProgress) {
        addRangeView(id, startProgress, selectProgress, RangeImageSelectedView.CHANGE_TYPE_SCALE, 0);
    }

    /**
     * 添加弹幕的选择框
     *
     * @param id
     * @param startProgress
     * @param selectProgress
     */
    public void addDanmuRangeView(int id, double startProgress, int selectProgress, int maxWindowProgress) {
        addRangeView(id, startProgress, selectProgress, RangeImageSelectedView.CHANGE_TYPE_MOVE, maxWindowProgress);
    }

    /**
     * 添加一个区间选择器，添加的时候往外广播一下区间的范围
     *
     * @param startProgress  这两个进度都是0-100之间的值
     * @param selectProgress
     */
    public void addRangeView(int id, double startProgress, int selectProgress, int changeType, int maxWindowSelect) {
        if (mRangeView != null && mRangeView.getId() == id) {
            return;
        }

        if (selectProgress < 0) {
            return;
        }

        if (mRangeView != null) {
            invisibleRangeView(mRangeView.getId());
        }

        mRangeView = new RangeImageSelectedView(mContext);
        mRangeView.setId(id);
        mRangeView.setChangeType(changeType);

        mRangeView.setHorizontalScrollView(mHorizontalScrollView);
        mRangeView.setSeekWidth(mList.getWidth());
        mRangeView.setSeekbarVisiblewidth(getWidth());
        mRangeView.setLeftBlankWidth(mLeftBlankWidth);
        mRangeView.setRightBlankWidth(mRightBlankWidth);
        mRangeView.setImageListLen(mImageBarLen);

        int windowStart = (int) (startProgress * mImageBarLen / (double) mMaxProgress + mLeftBlankWidth);
        int windowLen = selectProgress * mImageBarLen / mMaxProgress;
        int minWindowLen = mMinSelectProgress * mImageBarLen / mMaxProgress;
        int maxWindowLen = maxWindowSelect * mImageBarLen / mMaxProgress;
        mRangeView.setWindowStart(windowStart);
        mRangeView.setWindowLen(windowLen);
        //图片标签才会有最小宽度，弹幕没有，默认是0
        mRangeView.setMinWindowLen(minWindowLen);

        //如果是弹幕的选择框才会有最大值
        if (changeType == RangeImageSelectedView.CHANGE_TYPE_MOVE) {
            if (maxWindowLen > 0) {
                mRangeView.setMaxWindowLen(maxWindowLen);
                if(maxWindowSelect > selectProgress) {
                    mRangeView.setLeftChange(true);
                }
            } else {
                mRangeView.setMaxWindowLen(windowLen);
            }
        }

        mCacheRangeView.put(id, mRangeView);

        mSeekbarContainer.addView(mRangeView);
        mRangeView.setOnRangeChangedListener(new RangeImageSelectedView.OnRangeChangedListener() {
            @Override
            public void onRangeChanging(int direction, int postion) {

                //移动进度条的光标
                mCurrentProgress = postion;
                mSelectbarParams = new RelativeLayout.LayoutParams(mSelectbarWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                mSelectbarParams.leftMargin = postion - mHorizontalScrollView.getScrollX();
                mSelectbar.setLayoutParams(mSelectbarParams);
                mSelectbar.requestLayout();

                //广播进度条
                if (mProgressChangeListener != null) {
                    double progress = (double) (mCurrentProgress - mLeftBlankWidth) * mMaxProgress / (double) mImageBarLen;
                    double startProgress = (double) (mRangeView.getWindowStart() - mLeftBlankWidth) / (double) mImageBarLen;
                    double selectProgress = (double) mRangeView.getWindowLen() / (double) mImageBarLen;

                    //通知进度条的范围
                    mProgressChangeListener.onRangeChanged(mRangeView.getId(), startProgress, selectProgress);

                    //通知光标百分比
                    mProgressChangeListener.onProgressChanged(progress, true);
                }
            }

            @Override
            public void onRangeChangeFinished() {

                //光标回到中间
                mSelectbarParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                mSelectbar.setLayoutParams(mSelectbarParams);
                mSelectbar.requestLayout();
                mHorizontalScrollView.smoothScrollTo(mCurrentProgress - getWidth() / 2, 0);
            }

        });

        //广播进度条
        if (mProgressChangeListener != null) {
            //通知进度条的范围
            mProgressChangeListener.onRangeChanged(mRangeView.getId(), startProgress / 100.0d, selectProgress / 100.0d);
        }
    }

    public interface OnSeekbarChangedListener {
        void onProgressChanged(double progress, boolean fromUser);
        void onRangeChanged(int id, double startPercent, double selectPercent);
    }
}
