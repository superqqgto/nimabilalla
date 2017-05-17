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
import com.pax.pay.trans.receipt.paperless.ReceiptEmailTrans;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.EmailInfo;

public class ActionSendEmail extends AAction {

    private TransData transData;

    public ActionSendEmail(ActionStartListener listener) {
        super(listener);
    }

    public ActionSendEmail(ActionStartListener startListener, TransData transData) {
        super(startListener);
        this.transData = transData;
    }

    public ActionSendEmail setParam(TransData transData) {
        this.transData = transData;
        return this;
    }

    @Override
    protected void process() {
        FinancialApplication.mApp.runInBackground(new Runnable() {

            @Override
            public void run() {
                Context context = ContextUtils.getActyContext();
                EmailInfo emailInfo = EmailInfo.generateSmtpInfo();
                ReceiptEmailTrans receiptEmailTrans = ReceiptEmailTrans.getInstance();
                PrintListenerImpl listener = new PrintListenerImpl(context);
                int ret = receiptEmailTrans.send(transData, emailInfo, false, listener);
                if (ret == 0) {
                    setResult(new ActionResult(TransResult.SUCC, transData));
                } else {
                    setResult(new ActionResult(TransResult.ERR_SEND, null));
                }
            }
        });
    }

    public static class Builder {

        private ActionStartListener startListener;
        private TransData transData;

        public Builder startListener(ActionStartListener startListener) {
            this.startListener = startListener;
            return this;
        }

        public Builder transData(TransData transData) {
            this.transData = transData;
            return this;
        }

        public ActionSendEmail create() {
            return new ActionSendEmail(startListener, transData);
        }
    }
}
