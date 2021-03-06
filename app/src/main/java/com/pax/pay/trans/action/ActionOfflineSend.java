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
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;
import com.pax.pay.utils.ContextUtils;

public class ActionOfflineSend extends AAction {

    private TransProcessListenerImpl transProcessListenerImpl;

    private TransData transData;

    public ActionOfflineSend(ActionStartListener listener) {
        super(listener);
    }

    private ActionOfflineSend(ActionStartListener listener, TransData transData) {
        super(listener);
        this.transData = transData;
    }

    public void setTransData(TransData transData) {
        this.transData = transData;
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

    public static class Builder {

        ActionStartListener startListener;
        private TransData transData;

        public Builder startListener(ActionStartListener startListener) {
            this.startListener = startListener;
            return this;
        }

        public Builder transData(TransData transData) {
            this.transData = transData;
            return this;
        }

        public ActionOfflineSend create() {
            return new ActionOfflineSend(startListener, transData);
        }
    }
}
