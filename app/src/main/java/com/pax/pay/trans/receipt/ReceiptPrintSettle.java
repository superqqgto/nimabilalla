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
package com.pax.pay.trans.receipt;

import com.pax.edc.R;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.utils.ContextUtils;

/**
 * print settle
 *
 * @author Steven.W
 */
public class ReceiptPrintSettle extends AReceiptPrint {
    private static ReceiptPrintSettle receiptPrintSettle;

    private ReceiptPrintSettle() {

    }

    public synchronized static ReceiptPrintSettle getInstance() {
        if (receiptPrintSettle == null) {
            receiptPrintSettle = new ReceiptPrintSettle();
        }

        return receiptPrintSettle;
    }

    public int print(String title, String result, TransTotal transTotal, PrintListener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onShowMessage(null, ContextUtils.getString(R.string.wait_print));
        }
        // AET-108
        ReceiptGeneratorTotal receiptGeneratorSettle = new ReceiptGeneratorTotal(title, result, transTotal);
        int ret = printBitmap(receiptGeneratorSettle.generateBitmap());

        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }
}
