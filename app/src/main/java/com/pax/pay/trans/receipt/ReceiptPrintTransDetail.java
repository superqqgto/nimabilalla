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
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * print detail
 *
 * @author Steven.W
 */
public class ReceiptPrintTransDetail extends AReceiptPrint {
    private static ReceiptPrintTransDetail receiptPrintTransDetail;

    private ReceiptPrintTransDetail() {

    }

    public synchronized static ReceiptPrintTransDetail getInstance() {
        if (receiptPrintTransDetail == null) {
            receiptPrintTransDetail = new ReceiptPrintTransDetail();
        }

        return receiptPrintTransDetail;
    }

    public int print(String title, List<TransData> list, PrintListener listener) {
        this.listener = listener;
        int count = 0;

        if (listener != null)
            listener.onShowMessage(null, ContextUtils.getString(R.string.wait_print));
        // print detail main information
        ReceiptGeneratorTransDetail receiptGeneratorTransDetail = new ReceiptGeneratorTransDetail();
        int ret = printBitmap(receiptGeneratorTransDetail.generateMainInfo(title));
        if (ret != 0) {
            if (listener != null) {
                listener.onEnd();
            }
            return ret;
        }
        List<TransData> details = new ArrayList<>();
        for (TransData data : list) {
            details.add(data);
            count++;
            if (count == list.size() || count % 20 == 0) {
                receiptGeneratorTransDetail = new ReceiptGeneratorTransDetail(details);
                ret = printBitmap(receiptGeneratorTransDetail.generateBitmap());
                if (ret != 0) {
                    if (listener != null) {
                        listener.onEnd();
                    }
                    return ret;
                }
                details = new ArrayList<>();
            }
        }
        printStr("\n\n\n\n\n\n");
        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }
}
