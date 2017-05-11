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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.pax.edc.R;
import com.pax.pay.utils.ContextUtils;

/**
 * print bitmap
 */
public class ReceiptPrintBitmap extends AReceiptPrint {
    private static ReceiptPrintBitmap receiptPrintBitmap;

    private ReceiptPrintBitmap() {

    }

    public synchronized static ReceiptPrintBitmap getInstance() {
        if (receiptPrintBitmap == null) {
            receiptPrintBitmap = new ReceiptPrintBitmap();
        }

        return receiptPrintBitmap;
    }

    public int print(String bitmapStr, PrintListener listener) {
        this.listener = listener;

        if (listener != null) {
            listener.onShowMessage(null, ContextUtils.getString(R.string.wait_process));
        }

        // 将json传入的String转换成Bitmap
        byte[] bitmapArray;
        bitmapArray = Base64.decode(bitmapStr, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);

        if (listener != null) {
            listener.onShowMessage(null, ContextUtils.getString(R.string.wait_print));
        }

        printBitmap(bitmap);
        if (listener != null) {
            listener.onEnd();
        }
        return 0;
    }

}
