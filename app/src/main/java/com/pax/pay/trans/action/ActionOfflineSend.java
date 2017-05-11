/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-12
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import android.content.Context;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;
import com.pax.pay.utils.ContextUtils;

public class ActionOfflineSend extends AAction {

    private TransProcessListenerImpl transProcessListenerImpl;

    public ActionOfflineSend(ActionStartListener listener) {
        super(listener);
    }

    @Override
    protected void process() {
        FinancialApplication.mApp.runInBackground(new Runnable() {
            @Override
            public void run() {
                Context context = ContextUtils.getActyContext();
                transProcessListenerImpl = new TransProcessListenerImpl(context);
                int ret = Transmit.getInstance().sendOfflineTrans(transProcessListenerImpl, true, false);
                transProcessListenerImpl.onHideProgress();
                setResult(new ActionResult(ret, null));
            }
        });
    }
}
