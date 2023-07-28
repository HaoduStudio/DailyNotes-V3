package com.haoduyoudu.DailyAccounts.view.customView.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.haoduyoudu.DailyAccounts.BaseApplication;
import com.haoduyoudu.DailyAccounts.R;


/**
 * 贴纸的基类（存放贴纸的基本的属性）
 * Create by: chenWei.li
 * Date: 2019/2/4
 * Time: 12:55 AM
 * Email: lichenwei.me@foxmail.com
 */
public abstract class BaseSticker implements ISupportOperation {

    private Bitmap mStickerBitmap;//贴纸图像
    private Bitmap mDelBitmap;//贴纸图像
    private Matrix mMatrix;//维护图像变化的矩阵
    private boolean isFocus;//当前是否聚焦
    protected int mMode;//当前模式

    private float[] mSrcPoints;//矩阵变换前的点坐标
    private float[] mDstPoints;//矩阵变换后的点坐标
    private RectF mStickerBound;//贴纸范围
    private RectF mDelBound;//删除按钮范围
    private PointF mMidPointF;//贴纸中心的点坐标

    public static final int MODE_NONE = 0;//初始状态
    public static final int MODE_SINGLE = 1;//标志是否可移动
    public static final int MODE_MULTIPLE = 2;//标志是否可缩放，旋转

    private static final int PADDING = 30;//避免图像与边框太近，这里设置一个边距

    public BaseSticker(Bitmap bitmap) {
        //将贴纸默认移动到屏幕中间
        initSticker(bitmap);
        WindowManager windowManager = (WindowManager) BaseApplication.context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        float dx = displayMetrics.widthPixels / 2 - mStickerBitmap.getWidth() / 2;
        float dy = displayMetrics.heightPixels / 2 - mStickerBitmap.getHeight() / 2;
        translate(dx, dy);
        //将贴纸默认缩小1/2
        scale(0.5f, 0.5f);
    }

    public BaseSticker(Bitmap bitmap, float dx, float dy) {
        initSticker(bitmap);
        translate(dx, dy);
        //将贴纸默认缩小1/2
        scale(0.5f, 0.5f);
    }

    private void initSticker(Bitmap bitmap){
        this.mStickerBitmap = bitmap;
        mMatrix = new Matrix();
        mMidPointF = new PointF();

        mSrcPoints = new float[]{
                0, 0,//左上
                bitmap.getWidth(), 0,//右上
                bitmap.getWidth(), bitmap.getHeight(),//右下
                0, bitmap.getHeight(),//左下
                bitmap.getWidth() / 2, bitmap.getHeight() / 2//中间点
        };
        mDstPoints = mSrcPoints.clone();
        mStickerBound = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());

        mDelBitmap = BitmapFactory.decodeResource(BaseApplication.context.getResources(), R.drawable.ic_sticker_view_delete);
        mDelBound = new RectF(0 - mDelBitmap.getWidth() / 2 - PADDING, 0 - mDelBitmap.getHeight() / 2 - PADDING, mDelBitmap.getWidth() / 2 + PADDING, mDelBitmap.getHeight() / 2 + PADDING);

    }

    public Bitmap getBitmap() {
        return mStickerBitmap;
    }

    public RectF getStickerBitmapBound() {
        return mStickerBound;
    }

    public RectF getDelBitmapBound() {
        return mDelBound;
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public boolean isFocus() {
        return isFocus;
    }

    public void setFocus(boolean focus) {
        isFocus = focus;
    }

    public PointF getMidXy(){return mMidPointF;}

    public void setMidXy(PointF mMidXy){mMidPointF = mMidXy;}

    /**
     * 平移操作
     *
     * @param dx
     * @param dy
     */
    @Override
    public void translate(float dx, float dy) {
        mMatrix.postTranslate(dx, dy);
        updatePoints();
    }

    /**
     * 缩放操作
     *
     * @param sx
     * @param sy
     */
    @Override
    public void scale(float sx, float sy) {
        mMatrix.postScale(sx, sy, mMidPointF.x, mMidPointF.y);
        updatePoints();
    }

    /**
     * 旋转操作
     *
     * @param degrees
     */
    @Override
    public void rotate(float degrees) {
        mMatrix.postRotate(degrees, mMidPointF.x, mMidPointF.y);
        updatePoints();
    }


    /**
     * 当矩阵发生变化的时候，更新坐标点（src坐标点经过matrix映射变成了dst坐标点）
     */
    public void updatePoints() {
        //更新贴纸点坐标
        mMatrix.mapPoints(mDstPoints, mSrcPoints);
        //更新贴纸中心点坐标
        mMidPointF.set(mDstPoints[8], mDstPoints[9]);
    }

    /**
     * 绘制贴纸自身
     *
     * @param canvas
     * @param paint
     */
    @Override
    public void onDraw(Canvas canvas, Paint paint) {
        //绘制贴纸
        canvas.drawBitmap(mStickerBitmap, mMatrix, paint);
        if (isFocus) {
            //绘制贴纸边框(艹 画的不准)
            //canvas.drawLine(mDstPoints[0] - 0, mDstPoints[1] - 0, mDstPoints[2] + 0, mDstPoints[3] - 0, paint);
            //canvas.drawLine(mDstPoints[2] + 0, mDstPoints[3] - 0, mDstPoints[4] + 0, mDstPoints[5] + 0, paint);
            //canvas.drawLine(mDstPoints[4] + 0, mDstPoints[5] + 0, mDstPoints[6] - 0, mDstPoints[7] + 0, paint);
            //canvas.drawLine(mDstPoints[6] - 0, mDstPoints[7] + 0, mDstPoints[0] - 0, mDstPoints[1] - 0, paint);
            //上面几行代码里面的0改PADDING
            //绘制移除按钮
            //canvas.drawBitmap(mDelBitmap, mDstPoints[0] - mDelBitmap.getWidth() / 2 - PADDING, mDstPoints[1] - mDelBitmap.getHeight() / 2 - PADDING, paint);
            double radian = Math.atan((mDstPoints[5] - mDstPoints[1])/(mDstPoints[4] - mDstPoints[0]));
            double angle = radian * 180 / Math.PI;
            double distance = Math.sqrt(Math.pow(mDstPoints[8]-mDstPoints[0],2)+Math.pow(mDstPoints[9]-mDstPoints[1],2))*1.2;
            PointF Startxy = new PointF();
            Startxy.set(mDstPoints[8],mDstPoints[9]);
            for (int i = 1;i<=4;i++){
                float startX = getEndPointByTrigonometric(angle,Startxy,distance).x;
                float startY = getEndPointByTrigonometric(angle,Startxy,distance).y;
                angle += 90;
                float endX = getEndPointByTrigonometric(angle,Startxy,distance).x;
                float endY = getEndPointByTrigonometric(angle,Startxy,distance).y;
                canvas.drawLine(startX, startY, endX, endY, paint);
            }
        }
    }

    public static PointF getEndPointByTrigonometric(double angle, PointF StartPoint, double distance)
    {

        //角度转弧度
        double radian = (angle * Math.PI) / 180;
        PointF newpos = new PointF();
        //计算新坐标 r 就是两者的距离
        float x = (float) (StartPoint.x + distance * Math.cos(radian));
        float y = (float) (StartPoint.y + distance * Math.sin(radian));
        newpos.set(x,y);

        return newpos;
    }

}
