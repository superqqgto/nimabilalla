package com.pax.view;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by huangmuhua on 2017/4/28.
 */

public class BlinkImageView extends ImageView {

    public final static int OFF = 0;
    public final static int ON = 1;
    public final static int BLINK = 2;

    @IntDef({OFF, ON, BLINK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface STATUS {
    }

    //blink interval, 闪烁的时间间隔
    private static final long interval = 500;
    private Runnable r;
    private boolean isBlink = false;
    @STATUS
    int status = OFF;

    public BlinkImageView(Context context) {
        super(context);
        init();
    }

    public BlinkImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlinkImageView(Context context, int resId) {
        this(context);
        setImageResource(resId);
    }

    private void init() {

        r = new Runnable() {
            @Override
            public void run() {
                if (!isBlink) {
                    return;
                }
                setSelected(!isSelected());
                postDelayed(r, interval);//构成循环
            }
        };
    }

    public void onStatusChanged(@STATUS int status) {

        this.status = status;
        switch (status) {
            case OFF:
                isBlink = false;
                setSelected(false);
                break;
            case ON:
                isBlink = false;
                setSelected(true);
                break;
            case BLINK:
                isBlink = true;
                removeCallbacks(r);
                postDelayed(r, interval);
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        onStatusChanged(BLINK);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(r);
    }
}
