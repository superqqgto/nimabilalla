/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-13
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.AdjustTipActivity;
import com.pax.pay.utils.ContextUtils;

public class ActionAdjustTip extends AAction {

    public ActionAdjustTip(ActionStartListener listener) {
        super(listener);
    }

    private String title;
    private String amount;
    private float percent;

    public void setParam(String title, String amount, float percent) {
        this.title = title;
        this.amount = amount;
        this.percent = percent;
    }

    @Override
    protected void process() {
        Context context = ContextUtils.getActyContext();
        Intent intent = new Intent(context, AdjustTipActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        intent.putExtra(EUIParamKeys.TIP_PERCENT.toString(), percent);
        context.startActivity(intent);
    }
}
