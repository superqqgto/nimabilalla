/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-21
 * Module Author: Kim.L
 * Description: workaround for EditTextPreference, cuz the keyboard cannot be hidden after dialog is gone.
 * AET-79
 *
 * ============================================================================
 */
package com.pax.view;

import android.app.Activity;
import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;

import com.pax.pay.utils.Utils;

public class EditTextPreferenceFix extends EditTextPreference {

    public EditTextPreferenceFix(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditTextPreferenceFix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreferenceFix(Context context) {
        this(context, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        getEditText().clearFocus();
        hideSysInput();
    }

    private void hideSysInput() {
        Window window = ((Activity) getContext()).getWindow();
        View contentView = window.findViewById(Window.ID_ANDROID_CONTENT);

        if (contentView.getWindowToken() != null) {
            Utils.hideSystemKeyboard(getContext(), contentView);
        }
    }
}
