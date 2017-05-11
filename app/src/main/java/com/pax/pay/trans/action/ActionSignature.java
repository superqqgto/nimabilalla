/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.SignatureActivity;
import com.pax.pay.utils.ContextUtils;

public class ActionSignature extends AAction {

    private String amount;
    private String featureCode;

    public ActionSignature(ActionStartListener listener) {
        super(listener);
    }

    private ActionSignature(ActionStartListener listener, String amount) {
        super(listener);
        this.amount = amount;
        this.featureCode = featureCode;
    }

    public void setParam(String amount, String featureCode) {
        this.amount = amount;
        this.featureCode = featureCode;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    @Override
    protected void process() {
        Context context = ContextUtils.getActyContext();
        Intent intent = new Intent(context, SignatureActivity.class);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        //intent.putExtra(EUIParamKeys.SIGN_FEATURE_CODE.toString(), featureCode);

        context.startActivity(intent);
    }

    public static class Builder {

        private ActionStartListener startListener;
        private String transAmount;

        public Builder startListener(ActionStartListener startListener) {
            this.startListener = startListener;
            return this;
        }

        public Builder transAmount(String transAmount) {
            this.transAmount = transAmount;
            return this;
        }

        public ActionSignature create() {
            return new ActionSignature(startListener, transAmount);
        }
    }
}
