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
package com.pax.pay.trans.receipt.paperless;

import com.pax.edc.R;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.receipt.PrintListener;
import com.pax.pay.trans.receipt.ReceiptGeneratorTrans;
import com.pax.pay.utils.ContextUtils;

public class ReceiptSMSTrans extends AReceiptSMS {
    private static ReceiptSMSTrans receiptSMSTrans;

    private ReceiptSMSTrans() {

    }

    public synchronized static ReceiptSMSTrans getInstance() {
        if (receiptSMSTrans == null) {
            receiptSMSTrans = new ReceiptSMSTrans();
        }

        return receiptSMSTrans;
    }

    public int send(TransData transData, boolean isRePrint, PrintListener listener) {
        if (!transData.getIssuer().isAllowPrint())
            return 0;

        this.listener = listener;
        int ret = 0;
        if (listener != null)
            listener.onShowMessage(null, ContextUtils.getString(R.string.wait_send));

        ReceiptGeneratorTrans receiptGeneratorTrans = new ReceiptGeneratorTrans(transData, 2, 1, isRePrint);
        ret = sendTextMessage(transData.getPhoneNum(), receiptGeneratorTrans.generateString());

        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }
}
