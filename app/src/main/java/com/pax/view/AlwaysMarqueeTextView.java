/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2015 - ? Pax Corporation. All rights reserved.
 * Module Date: 2015-08-26
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.view;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

public class AlwaysMarqueeTextView extends TextView {

    public AlwaysMarqueeTextView(Context context) {
        super(context);
        SetAlwaysMarquee();
    }

    public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SetAlwaysMarquee();
    }

    public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        SetAlwaysMarquee();
    }

    private void SetAlwaysMarquee() {
        setEllipsize(TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setSingleLine();
    }

    @Override
    public boolean isFocused() {
        return true;
    }

}

