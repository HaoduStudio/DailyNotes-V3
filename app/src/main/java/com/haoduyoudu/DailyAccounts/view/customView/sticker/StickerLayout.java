package com.haoduyoudu.DailyAccounts.view.customView.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import java.util.List;

public class StickerLayout extends View implements View.OnTouchListener {
    private Paint mPaint;

    private Sticker mStick;

    private Boolean canEdit = true;

    private StickerScrollView scrollParent;

    private OnEditPopChangeListener mEPCListener;

    private boolean isPop = false;

    public StickerLayout(Context context) {
        super(context);
        init(context);
    }

    public StickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化操作
     */
    private void init(Context context) {
        //设置触摸监听
        setOnTouchListener(this);
        mPaint = getPaint();
    }

    public Paint getPaint() {
        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(2);
        }
        return mPaint;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    public void addSticker(Sticker sticker) {
        StickerManager.getInstance().addSticker(sticker);
        if (canEdit)
            StickerManager.getInstance().setFocusSticker(sticker);
        invalidate();
    }

    public void removeSticker(Sticker sticker) {
        if (sticker.isFocus()) {
            StickerManager.getInstance().removeSticker(sticker);
            invalidate();
        }
    }

    /**
     * 清空贴纸
     */
    public void removeAllSticker() {
        StickerManager.getInstance().removeAllSticker();
        invalidate();
    }

    /**
     * 清空所有焦点
     */
    public void cleanAllFocus() {
        StickerManager.getInstance().clearAllFocus();
        invalidate();

    }

    /**
     * 获取有焦点的贴纸
     */
    public Sticker getFocusSticker() {
        return StickerManager.getInstance().getFocusSticker();
    }

    public void setFocusSticker(Sticker focus){
        StickerManager.getInstance().setFocusSticker(focus);
    }

    /**
     * 缩放操作
     */
    public void scaleSticker(Sticker mSticker,float sx,float sy) {
        StickerManager.getInstance().scaleSticker(mSticker,sx,sy);
        invalidate();
    }

    /**
     * 旋转操作
     */
    public void rotateSticker(Sticker mSticker,float degrees) {
        StickerManager.getInstance().rotateSticker(mSticker,degrees);
        invalidate();
    }

    /**
     * 旋转操作
     */
    public List<Sticker> returnAllSticker() {
        return StickerManager.getInstance().getStickerList();
    }

    public void update() {
        StickerManager.getInstance().upalldata();
    }

    private StickerScrollView findStickerScrollView(View view) {
        if (view instanceof StickerScrollView) {
            Log.d("StickerLayout", "Find Sticker Scroll View");
            return (StickerScrollView) view;
        }
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            return findStickerScrollView((View) parent);
        }else {
            return null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        List<Sticker> stickerList = StickerManager.getInstance().getStickerList();
        for (Sticker st : stickerList) {
            st.onDraw(canvas, mPaint);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (canEdit) {
                    v.requestFocus();
                    Log.d("StickerLayout", "OnDown");
                    Sticker before = mStick;
                    mStick = StickerManager.getInstance().getSticker(event.getX(), event.getY());

                    if (mStick != null) {
                        StickerManager.getInstance().setFocusSticker(mStick);
                        List<Sticker> stList = StickerManager.getInstance().getStickerList();
                        stList.remove(mStick);
                        stList.add(mStick);
                    }else {
                        StickerManager.getInstance().clearAllFocus();
                    }

                    if (mEPCListener != null) {
                        if (before != null && mStick != null && before == mStick && !isPop) {
                            mEPCListener.onChange(true, null);
                            isPop = true;
                        }else if (before != null && mStick != null && before != mStick) {
                            mEPCListener.onChange(true, mStick);
                            isPop = true;
                        }else if (before == null && mStick != null) {
                            mEPCListener.onChange(true, mStick);
                            isPop = true;
                        }else if (before != null && mStick == null) {
                            mEPCListener.onChange(false, before);
                            isPop = false;
                        }
                    }

                    invalidate();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (scrollParent != null) {
                    if (mStick != null) {
                        scrollParent.isGetFocus = false;
                        if (mEPCListener != null && isPop) {
                            mEPCListener.onChange(false, null);
                            isPop = false;
                        }
                        scrollParent.post(() -> {
                            try {
                                Log.d("StickerLayout", "Event y = " + event.getY());
                                if (event.getY() < 50) {
                                    scrollParent.smoothScrollTo(0, scrollParent.getScrollY() - 10);
                                }else if (event.getY() > 360 - 50) {
                                    scrollParent.smoothScrollTo(0, scrollParent.getScrollY() + 10);
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }else {
                        scrollParent.isGetFocus = true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (scrollParent != null) scrollParent.isGetFocus = true;
                break;
        }

        if (mStick != null) {
            mStick.onTouch(event);
        }
        invalidate();
        return canEdit;
    }

    public interface OnEditPopChangeListener {
        void onChange(Boolean onFocus, Sticker sticker);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        scrollParent = findStickerScrollView(this);
    }

    public void setOnEditPopChangeListener(OnEditPopChangeListener listener) {
        mEPCListener = listener;
    }
}
