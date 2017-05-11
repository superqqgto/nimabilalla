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
import com.pax.pay.utils.EmailInfo;

public class ReceiptEmailTrans extends AReceiptEmail {
    private static ReceiptEmailTrans receiptEmailTrans;

    private ReceiptEmailTrans() {

    }

    public synchronized static ReceiptEmailTrans getInstance() {
        if (receiptEmailTrans == null) {
            receiptEmailTrans = new ReceiptEmailTrans();
        }

        return receiptEmailTrans;
    }

    public int send(TransData transData, EmailInfo emailInfo, boolean isRePrint, PrintListener listener) {
        if (!transData.getIssuer().isAllowPrint())
            return 0;

        this.listener = listener;
        int ret = 0;
        if (listener != null)
            listener.onShowMessage(null, ContextUtils.getString(R.string.wait_send));

        ReceiptGeneratorTrans receiptGeneratorTrans = new ReceiptGeneratorTrans(transData, 2, 1, isRePrint);
        ret = sendHtmlEmail(emailInfo, transData.getEmail(), generateReceiptSubject(), "Receipt", receiptGeneratorTrans.generateBitmap());

        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }

    private String generateReceiptSubject() {
        return "";
    }
}
