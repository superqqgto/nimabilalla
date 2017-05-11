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

import com.pax.edc.R;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;

/**
 * generate bitmap image of print preview
 */

public class ReceiptPreviewTrans {

    public Bitmap preview(TransData transData, PrintListener listener) {

        if (listener != null)
            listener.onShowMessage(null, ContextUtils.getString(R.string.wait_receipt_generate));

        ReceiptGeneratorTrans receiptGeneratorTrans = new ReceiptGeneratorTrans(transData, 1, 1, false, true);
        Bitmap bitmap = receiptGeneratorTrans.generateBitmap();

        if (listener != null) {
            listener.onEnd();
        }

        return bitmap;
    }
}
