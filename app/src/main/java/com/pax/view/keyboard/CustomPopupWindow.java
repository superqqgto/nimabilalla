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
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;

public class CustomPopupWindow extends PopupWindow {

    public CustomPopupWindow(Context context) {
        this(context, null);
    }

    public CustomPopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomPopupWindow() {
        this(null, 0, 0);
    }

    public CustomPopupWindow(View contentView) {
        this(contentView, 0, 0);
    }

    public CustomPopupWindow(int width, int height) {
        this(null, width, height);
    }

    public CustomPopupWindow(View contentView, int width, int height) {
        this(contentView, width, height, false);
    }

    public CustomPopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
    }

    @Override
    public void dismiss() {
        if(onEnableDismissListener != null && onEnableDismissListener.onEnableDismiss()){
            super.dismiss();
        }
    }

    public void forceDismiss(){
        super.dismiss();
    }

    private OnEnableDismissListener onEnableDismissListener;

    /**
     * Listener that is called when this popup window is dismissed.
     */
    public interface OnEnableDismissListener {
        /**
         * Called when this popup window is dismissed.
         */
        boolean onEnableDismiss();
    }

    public void setOnEnableDismissListener(OnEnableDismissListener onEnableDismissListener){
        this.onEnableDismissListener = onEnableDismissListener;
    }
}
