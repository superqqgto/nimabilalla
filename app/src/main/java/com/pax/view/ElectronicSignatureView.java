/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-1
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.pax.edc.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ElectronicSignatureView extends View {

    private Context mContext;

    /**
     * 笔画X坐标起点
     */
    private float mX;
    /**
     * 笔画Y坐标起点
     */
    private float mY;
    /**
     * 手写画笔
     */
    private final Paint mGesturePaint = new Paint();
    /**
     * 路径
     */
    private final Path mPath = new Path();
    /**
     * 背景画布
     */
    private Canvas cacheCanvas;
    /**
     * 背景Bitmap缓存
     */
    private Bitmap cachebBitmap;
    /**
     * 是否已经签名
     */
    private boolean isTouched = false;

    /**
     * 画笔宽度 px；
     */
    private int mPaintWidth;

    /**
     * 前景色
     */
    private int mPenColor = Color.BLACK;

    private int mBackColor = Color.TRANSPARENT;

    int textColor;
    float textSize;
    String text;
    Rect rect;
    int padding;
    int background;
    boolean isFirst = true;

    public ElectronicSignatureView(Context context) {
        super(context);
        init(context);
    }

    public ElectronicSignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ElectronicSignatureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        this.mContext = context;

        mPaintWidth = (int) context.getResources().getDimension(R.dimen.paint_width);
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setStyle(Style.STROKE);
        mGesturePaint.setStrokeWidth(mPaintWidth);
        mGesturePaint.setColor(mPenColor);
        mGesturePaint.setStrokeJoin(Paint.Join.ROUND);
        mGesturePaint.setStrokeCap(Paint.Cap.ROUND);

        this.text = "";
        this.textColor = Color.BLACK;
        this.textSize = context.getResources().getDimension(R.dimen.font_size_largest);

        this.rect = new Rect(0, 0, 500, 200);
        this.padding = 0;
        this.background = Color.WHITE;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cachebBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        cacheCanvas = new Canvas(cachebBitmap);
        cacheCanvas.drawColor(mBackColor);
        isTouched = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                isTouched = true;
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                cacheCanvas.drawPath(mPath, mGesturePaint);
                mPath.reset();
                break;
        }
        // 更新绘制
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(cachebBitmap, 0, 0, mGesturePaint);
        // 通过画布绘制多点形成的图形
        canvas.drawPath(mPath, mGesturePaint);

        if (isFirst) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTextSize(textSize);
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            cacheCanvas.drawText(text, getMeasuredWidth() / 2 - bounds.width() / 2,
                    getMeasuredHeight() / 2 + bounds.height() / 2, paint);
            isFirst = false;
        }
    }

    public void setText(int textSize, int textColor, String text) {
        this.textSize = textSize;
        this.textColor = textColor;
        this.text = text;
    }

    public void setBitmap(Rect rect, int padding, int background) {
        this.background = background;
        this.rect = rect;
        this.padding = padding;
    }

    // 手指点下屏幕时调用
    private void touchDown(MotionEvent event) {

        // mPath.rewind();
        // 重置绘制路线，即隐藏之前绘制的轨迹
        mPath.reset();
        float x = event.getX();
        float y = event.getY();

        mX = x;
        mY = y;
        // mPath绘制的绘制起点
        mPath.moveTo(x, y);
    }

    // 手指在屏幕上滑动时调用
    private void touchMove(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        // 两点之间的距离大于等于3时，生成贝塞尔绘制曲线
        if (dx >= 3 || dy >= 3) {
            // 设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;

            // 二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
            mPath.quadTo(previousX, previousY, cX, cY);

            // 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            mX = x;
            mY = y;
        }
    }

    /**
     * 清除画板
     */
    public void clear() {
        isFirst = true;
        if (cacheCanvas != null) {
            isTouched = false;
            mGesturePaint.setColor(mPenColor);
            cacheCanvas.drawColor(mBackColor, PorterDuff.Mode.CLEAR);
            mGesturePaint.setColor(mPenColor);
            invalidate();
        }
    }

    /**
     * 保存画板
     *
     * @param path 保存到路径
     */

    public void save(String path) throws IOException {
        save(path, false, 0);
    }

    /**
     * 保存bitmap
     *
     * @param clearBlank
     * @param blank
     * @return
     */
    public Bitmap save(boolean clearBlank, int blank) {
        Bitmap bitmap = cachebBitmap;
        if (clearBlank) {
            bitmap = clearBlank(bitmap, blank);
        }
        bitmap = placeBitmapIntoRect(bitmap, rect, padding);

        return bitmap;
    }

    /**
     * 保存画板
     *
     * @param path       保存到路径
     * @param clearBlank 是否清楚空白区域
     * @param blank      边缘空白区域
     */
    public void save(String path, boolean clearBlank, int blank) throws IOException {

        Bitmap bitmap = cachebBitmap;
        // BitmapUtil.createScaledBitmapByHeight(srcBitmap, 300);// 压缩图片
        if (clearBlank) {
            bitmap = clearBlank(bitmap, blank);
        }
        bitmap = placeBitmapIntoRect(bitmap, rect, padding);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] buffer = bos.toByteArray();
        if (buffer != null) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(buffer);
            outputStream.close();
        }
    }

    /**
     * 获取画板的bitmap
     *
     * @return
     */
    public Bitmap getBitMap() {
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        Bitmap bitmap = getDrawingCache();
        setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 逐行扫描 清楚边界空白。
     *
     * @param bp
     * @param blank 边距留多少个像素
     * @return
     */
    private Bitmap clearBlank(Bitmap bp, int blank) {
        int HEIGHT = bp.getHeight();
        int WIDTH = bp.getWidth();
        int top = 0, left = 0, right = 0, bottom = 0;
        int[] pixs = new int[WIDTH];
        boolean isStop;
        for (int y = 0; y < HEIGHT; y++) {
            bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    top = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        for (int y = HEIGHT - 1; y >= 0; y--) {
            bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    bottom = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        pixs = new int[HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    left = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        for (int x = WIDTH - 1; x > 0; x--) {
            bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    right = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        if (blank < 0) {
            blank = 0;
        }
        left = left - blank > 0 ? left - blank : 0;
        top = top - blank > 0 ? top - blank : 0;
        right = right + blank > WIDTH - 1 ? WIDTH - 1 : right + blank;
        bottom = bottom + blank > HEIGHT - 1 ? HEIGHT - 1 : bottom + blank;
        return Bitmap.createBitmap(bp, left, top, right - left, bottom - top);
    }

    /**
     * 设置画笔宽度 默认宽度为10px
     *
     * @param mPaintWidth
     */
    public void setPaintWidth(int mPaintWidth) {
        mPaintWidth = mPaintWidth > 0 ? mPaintWidth : 5;
        this.mPaintWidth = mPaintWidth;
        mGesturePaint.setStrokeWidth(mPaintWidth);

    }

    public void setBackColor(int backColor) {
        mBackColor = backColor;
    }

    /**
     * 设置画笔颜色
     *
     * @param mPenColor
     */
    public void setPenColor(int mPenColor) {
        this.mPenColor = mPenColor;
        mGesturePaint.setColor(mPenColor);
    }

    /**
     * 是否有签名
     *
     * @return
     */
    public boolean getTouched() {
        return isTouched;
    }

    public Bitmap placeBitmapIntoRect(final Bitmap bitmap, final Rect rect, int padding) {
        if (padding * 2 >= rect.height() || padding * 2 >= rect.width() || padding < 0) {
            padding = 0;
        }
        int height = rect.height() - padding * 2;
        int width = rect.width() - padding * 2;
        Matrix matrix = new Matrix();
        float h = (float) height / bitmap.getHeight();
        float w = (float) width / bitmap.getWidth();
        float size = h > w ? w : h;
        if (size > 0.5f) {
            size = 0.5f;
        }
        matrix.postScale(size, size);// 获取缩放比例
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // 根据缩放比例获取新的位图

        Bitmap newBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        // 设置边框颜色
        paint.setColor(background);
        canvas.drawRect(new Rect(0, 0, rect.width(), rect.height()), paint);
        if ((h > w ? w : h) > 0.5f) {
            canvas.drawBitmap(bitmap1, rect.width() / 2 - bitmap1.getWidth() / 2,
                    rect.height() / 2 - bitmap1.getHeight() / 2, null);
        } else {
            canvas.drawBitmap(bitmap1, rect.width() / 2 - bitmap1.getWidth() / 2, padding, null);
        }

        bitmap1.recycle();

        return newBitmap;
    }
}
