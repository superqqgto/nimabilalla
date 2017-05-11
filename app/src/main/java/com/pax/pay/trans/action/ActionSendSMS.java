/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-14
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import android.content.Context;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.receipt.PrintListenerImpl;
import com.pax.pay.trans.receipt.paperless.ReceiptSMSTrans;
import com.pax.pay.utils.ContextUtils;

public class ActionSendSMS extends AAction {

    private TransData transData;

    public ActionSendSMS(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(TransData transData) {
        this.transData = transData;
    }

    @Override
    protected void process() {
        FinancialApplication.mApp.runInBackground(new Runnable() {

            @Override
            public void run() {
                Context context = ContextUtils.getActyContext();
                ReceiptSMSTrans receiptSMSTrans = ReceiptSMSTrans.getInstance();
                PrintListenerImpl listener = new PrintListenerImpl(context);
                int ret = receiptSMSTrans.send(transData, false, listener);
                if (ret == 0) {
                    setResult(new ActionResult(TransResult.SUCC, transData));
                } else {
                    setResult(new ActionResult(TransResult.ERR_SEND, null));
                }
            }
        });
    }
}
