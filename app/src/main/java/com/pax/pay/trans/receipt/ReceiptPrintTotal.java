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
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.utils.ContextUtils;

/**
 * print total
 *
 * @author Steven.W
 */
public class ReceiptPrintTotal extends AReceiptPrint {
    private static ReceiptPrintTotal receiptPrintTotal;

    private ReceiptPrintTotal() {

    }

    public synchronized static ReceiptPrintTotal getInstance() {
        if (receiptPrintTotal == null) {
            receiptPrintTotal = new ReceiptPrintTotal();
        }

        return receiptPrintTotal;
    }

    public int print(String title, TransTotal transTotal, PrintListener listener) {
        this.listener = listener;

        if (listener != null) {
            listener.onShowMessage(null, ContextUtils.getString(R.string.wait_print));
        }
        ReceiptGeneratorTotal receiptGeneratorTotal = new ReceiptGeneratorTotal(title, null, transTotal);
        printBitmap(receiptGeneratorTotal.generateBitmap());
        if (listener != null) {
            listener.onEnd();
        }
        return 0;
    }

}
