/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-03-10
 * Module Author: huangwp
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.activity.UserAgreementActivity;
import com.pax.pay.utils.ContextUtils;

public class ActionUserAgreement extends AAction {

    public ActionUserAgreement(ActionStartListener listener) {
        super(listener);
    }

    @Override
    protected void process() {
        if (SpManager.getSysParamSp().get(SysParamSp.SUPPORT_USER_AGREEMENT).equals(SysParamSp.Constant.NO)) {
            setResult(new ActionResult(TransResult.SUCC, null));
            return;
        }

        Context context = ContextUtils.getActyContext();
        Intent intent = new Intent(context, UserAgreementActivity.class);
        context.startActivity(intent);
    }
}
