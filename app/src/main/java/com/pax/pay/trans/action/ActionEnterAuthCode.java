/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-10
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
import com.pax.pay.trans.action.activity.EnterAuthCodeActivity;
import com.pax.pay.utils.ContextUtils;

public class ActionEnterAuthCode extends AAction {

    private String title;
    private String header;
    private String amount;

    public ActionEnterAuthCode(ActionStartListener listener) {
        super(listener);
    }

    private ActionEnterAuthCode(ActionStartListener listener, String title, String header, String amount) {
        super(listener);
        this.title = title;
        this.header = header;
        this.amount = amount;
    }

    public void setTransAmount(String amount) {
        this.amount = amount;
    }

    public void setParam(String title, String header, String amount) {
        this.title = title;
        this.header = header;
        this.amount = amount;
    }

    @Override
    protected void process() {
        Context context = ContextUtils.getActyContext();
        Intent intent = new Intent(context, EnterAuthCodeActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.PROMPT_1.toString(), header);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        context.startActivity(intent);
    }

    public static class Builder {
        private ActionStartListener startListener;
        private int transNameResId;
        private int promptResId;
        private String transAmount;

        public Builder startListener(ActionStartListener startListener) {
            this.startListener = startListener;
            return this;
        }

        public Builder transName(int transNameResId) {
            this.transNameResId = transNameResId;
            return this;
        }

        public Builder prompt(int promptResId) {
            this.promptResId = promptResId;
            return this;
        }

        public Builder transAmount(String transAmount) {
            this.transAmount = transAmount;
            return this;
        }

        public ActionEnterAuthCode create() {
            String transName = ContextUtils.getString(transNameResId);
            String prompt = ContextUtils.getString(promptResId);
            return new ActionEnterAuthCode(startListener, transName, prompt, transAmount);
        }
    }
}
