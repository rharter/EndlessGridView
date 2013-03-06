package com.ryanharter.endlessgrid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

public class EndlessGridView extends ScrollView {
	private static final String TAG = "EndlessGridView";

    private static final int UPDATE_GRID = 1;
    
    private int mNumColumns;
	
    protected ListAdapter mAdapter;

    private int mItemWidth;
    private int mItemHeight;

	protected FrameLayout mContainer;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case UPDATE_GRID:
                    fillGrid();
                    break;
            }
        }
    };

	public EndlessGridView(Context context, AttributeSet attrs) {
		super(context, attrs);

        readAttributes(attrs);

		init();
	}

	public EndlessGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

        readAttributes(attrs);
        
		init();
	}

    private void readAttributes(AttributeSet attrs) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, 
            R.styleable.com_ryanharter_endlessgrid_EndlessGridView);

        mNumColumns = a.getInt(R.styleable.com_ryanharter_endlessgrid_EndlessGridView_numColumns, 3);
    }

	private void init() {
		mContainer = new FrameLayout(getContext());

		final LayoutParams lp = new LayoutParams(Integer.MAX_VALUE, Integer.MAX_VALUE);
		
        mContainer.setMinimumHeight(Integer.MAX_VALUE);
        mContainer.setMinimumWidth(Integer.MAX_VALUE);
        mContainer.setLayoutParams(lp);

		addView(mContainer, lp);

        //scrollTo(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
	}

	public void setAdapter(ListAdapter adapter) {
		mAdapter = adapter;

        // Get the initial view dimensions
        View dummyView = mAdapter.getView(0, null, this);
        mItemHeight = 400;//dummyView.getMeasuredHeight();
        mItemWidth = 400;//dummyView.getMeasuredWidth();
		fillGrid();
	}

	public ListAdapter getAdapter() {
		return mAdapter;
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);fillGrid();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        
        Message msg = Message.obtain();
        msg.what = UPDATE_GRID;

        if (mHandler.hasMessages(UPDATE_GRID)) {
            mHandler.removeMessages(UPDATE_GRID);
        }

        mHandler.sendMessage(msg);
    }

	private void fillGrid() {
		Rect visible = new Rect();
		mContainer.getDrawingRect(visible);

		mContainer.removeAllViews();

		final int left = visible.left + getScrollX();
		final int top = visible.top + getScrollY();

		final int width = (int) (getMeasuredWidth()) + getScrollX() + mItemWidth;
		final int height = (int) (getMeasuredHeight()) + getScrollY() + mItemHeight;

		Log.d(TAG, "measuredWidth=" + getMeasuredWidth() + ", measuredHeight=" + getMeasuredHeight());
		Log.d(TAG, "left=" + left + ", top=" + top + ", width=" + width + ", height=" + height);

		// Get the tiles
		for (int y = top; y < height;) {
			final int yPos = new Double(Math.ceil(y / mItemHeight)).intValue();
			Log.d(TAG, "yPos(" + yPos + ")");
			for (int x = left; x < width;) {
				final int xPos = new Double(Math.ceil(x / mItemWidth)).intValue();
				Log.d(TAG, "xPos(" + xPos + ")");

				final int viewPos = mAdapter.getCount() / mNumColumns * xPos + yPos;
                View v = mAdapter.getView(viewPos, null, this);

				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

				lp.leftMargin = xPos * mItemWidth;
				lp.topMargin = yPos * mItemHeight;
				lp.gravity = Gravity.TOP | Gravity.LEFT;
				v.setLayoutParams(lp);

				mContainer.addView(v, lp);

				x += mItemWidth;
			}
			y += mItemHeight;
		}
	}
}