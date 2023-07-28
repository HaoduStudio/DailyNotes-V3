package com.haoduyoudu.DailyAccounts.view.customView.sticker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class StickerScrollView extends ScrollView {
    public Boolean isGetFocus = true;
    private OnTouchListener mOnTouch = null;

    public StickerScrollView(Context context) {
        super(context);
    }
    public StickerScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mOnTouch != null)
            mOnTouch.run();
        if(isGetFocus) return super.onInterceptTouchEvent(ev);
        return false;
    }

    public interface OnTouchListener {
        public void run();
    }

    public void setonTouchListener(OnTouchListener ot) {
        mOnTouch = ot;
    }
}