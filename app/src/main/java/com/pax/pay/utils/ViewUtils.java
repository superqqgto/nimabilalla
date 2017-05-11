/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-27
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.edc.R;

public class ViewUtils {

    /**
     * 生成每一行记录
     *
     * @param title
     * @param value
     * @return
     */
    public static RelativeLayout genSingleLineLayout(Context context, String title, Object value) {
        RelativeLayout layout = new RelativeLayout(context);

        TextView titleTv = new TextView(context);
        titleTv.setText(title);
        titleTv.setGravity(Gravity.START);
        titleTv.setTextSize(context.getResources().getDimension(R.dimen.font_size_stander));
        titleTv.setTextColor(context.getResources().getColor(R.color.prompt_text_color));

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);// 与父容器的左侧对齐
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        titleTv.setLayoutParams(lp);// 设置布局参数
        layout.addView(titleTv);

        /**************************************************/

        TextView valueTv = new TextView(context);
        valueTv.setText(String.valueOf(value));
        valueTv.setGravity(Gravity.END);
        valueTv.setTextSize(context.getResources().getDimension(R.dimen.font_size_stander));
        if (value.equals(context.getString(R.string.state_voided))) {
            valueTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));

        } else if (String.valueOf(value).equals(context.getString(R.string.state_normal))
                || String.valueOf(value).equals(context.getString(R.string.trans_adjust))) {
            valueTv.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }

        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        rp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);// 与父容器的右侧对齐
        rp.addRule(RelativeLayout.CENTER_VERTICAL);
        valueTv.setLayoutParams(rp);// 设置布局参数
        layout.addView(valueTv);

        return layout;
    }

}
