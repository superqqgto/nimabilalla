/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-21
 * Module Auth: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.view.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.pax.edc.R;

import java.util.List;

public class CustomKeyboardView extends KeyboardView {

    private Drawable mKeyBgDrawable;
    private Drawable mOpKeyBgDrawable;

    private Paint paint = new Paint();

    public CustomKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initResources(context);
    }

    private void initResources(Context context) {
        mKeyBgDrawable = ContextCompat.getDrawable(context, R.drawable.btn_keyboard_key);
        mOpKeyBgDrawable = ContextCompat.getDrawable(context, R.drawable.btn_keyboard_opkey);
    }

    @Override
    public void onDraw(Canvas canvas) {
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            canvas.save();

            int offsetY = 0;
            if (key.y == 0) {
                offsetY = 1;
            }
            int initDrawY = key.y + offsetY;
            Rect rect = new Rect(key.x, initDrawY, key.x + key.width, key.y + key.height);
            canvas.clipRect(rect);

            Drawable drawable = null;
            if (null != key.codes && key.codes.length != 0) {
                int primaryCode = key.codes[0];

                if (primaryCode < 0) {
                    drawable = mOpKeyBgDrawable;
                } else {
                    drawable = mKeyBgDrawable;
                }
            }

            if (null != drawable && null == key.icon) {
                int[] state = key.getCurrentDrawableState();
                drawable.setState(state);
                drawable.setBounds(rect);
                drawable.draw(canvas);
            }

            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(50);
            paint.setColor(Color.BLACK);

            if (key.icon != null) {
                int[] state = key.getCurrentDrawableState();
                key.icon.setState(state);
                key.icon.setBounds(rect);
                key.icon.draw(canvas);
            }

            if (key.label != null) {
                canvas.drawText(
                        key.label.toString(),
                        key.x + (key.width / 2),
                        initDrawY + (key.height + paint.getTextSize() - paint.descent()) / 2,
                        paint
                );
            }
            canvas.restore();
        }
    }
}
