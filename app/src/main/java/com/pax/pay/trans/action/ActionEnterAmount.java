/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-11
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
import com.pax.pay.trans.action.activity.EnterAmountActivity;
import com.pax.pay.utils.ContextUtils;

public class ActionEnterAmount extends AAction {

    private String title;
    private float percent;

    public ActionEnterAmount(ActionStartListener listener) {
        super(listener);
    }

    private ActionEnterAmount(ActionStartListener listener, String title, float percent) {
        super(listener);
        this.title = title;
        this.percent = percent;
    }

    public void setParam(String title, float percent) {
        this.title = title;
        this.percent = percent;
    }

    public void setAdjustPercent(float adjustPercent) {
        this.percent = adjustPercent;
    }

    @Override
    protected void process() {
        Context context = ContextUtils.getActyContext();
        Intent intent = new Intent(context, EnterAmountActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.TIP_PERCENT.toString(), percent);
        context.startActivity(intent);
    }

    public static class Builder {
        private ActionStartListener startListener;
        private int transNameResId;
        private float adjustPercent;

        public Builder startListener(ActionStartListener startListener) {
            this.startListener = startListener;
            return this;
        }

        public Builder transName(int transNameResId) {
            this.transNameResId = transNameResId;
            return this;
        }

        public Builder adjustPercent(float adjustPercent) {
            this.adjustPercent = adjustPercent;
            return this;
        }

        public ActionEnterAmount create() {
            String transName = ContextUtils.getString(transNameResId);
            return new ActionEnterAmount(startListener, transName, adjustPercent);
        }
    }
}
