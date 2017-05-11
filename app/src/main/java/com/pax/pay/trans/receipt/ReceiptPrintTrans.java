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
import com.pax.manager.sp.SpManager;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.manager.sp.SysParamSp;

/**
 * print receipt
 *
 * @author Steven.W
 */
public class ReceiptPrintTrans extends AReceiptPrint {
    private static ReceiptPrintTrans receiptPrinterTrans;

    private ReceiptPrintTrans() {

    }

    public synchronized static ReceiptPrintTrans getInstance() {
        if (receiptPrinterTrans == null) {
            receiptPrinterTrans = new ReceiptPrintTrans();
        }

        return receiptPrinterTrans;
    }

    public int print(TransData transData, boolean isRePrint, PrintListener listener) {
        if (!transData.getIssuer().isAllowPrint())
            return 0;

        this.listener = listener;
        int ret = 0;
        int receiptNum = getVoucherNum();
        if (listener != null)
            listener.onShowMessage(null, ContextUtils.getString(R.string.wait_print));

        for (int i = 0; i < receiptNum; i++) {
            ReceiptGeneratorTrans receiptGeneratorTrans = new ReceiptGeneratorTrans(transData, i, receiptNum, isRePrint);
            ret = printBitmap(receiptGeneratorTrans.generateBitmap());
            if (ret == -1) {
                break;
            }
        }
        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }

    private int getVoucherNum() {

        int receiptNum = 0;
        String temp = SpManager.getSysParamSp().get(SysParamSp.PRINT_VOUCHER_NUM);
        if (temp != null)
            receiptNum = Integer.parseInt(temp);
        if (receiptNum < 1 || receiptNum > 3) // receipt copy number is 1-3
            receiptNum = 2;

        return receiptNum;
    }

}
