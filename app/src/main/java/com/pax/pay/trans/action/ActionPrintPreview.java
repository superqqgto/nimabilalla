/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-6
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.pax.abl.core.AAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.receipt.PrintListenerImpl;
import com.pax.pay.trans.receipt.ReceiptPreviewTrans;
import com.pax.pay.utils.ContextUtils;

import java.io.ByteArrayOutputStream;

public class ActionPrintPreview extends AAction {

    private TransData transData;

    private Bitmap loadedBitmap = null;
    private boolean hasSignature = false;

    public ActionPrintPreview(ActionStartListener listener) {
        super(listener);
    }

    private ActionPrintPreview(ActionStartListener listener, TransData transData) {
        super(listener);
        this.transData = transData;
    }

    public void setParam(TransData transData) {
        this.transData = transData;
    }

    public void setTransData(TransData transData) {
        this.transData = transData;
    }

    @Override
    protected void process() {
        FinancialApplication.mApp.runInBackground(new Runnable() {

            @Override
            public void run() {
                Context context = ContextUtils.getActyContext();
                boolean signed = transData.getSignData() != null && transData.getSignData().length > 0;
                if (loadedBitmap == null || hasSignature != signed) {
                    //generate bitmap image of send preview
                    ReceiptPreviewTrans receiptPreviewTrans = new ReceiptPreviewTrans();
                    PrintListenerImpl listener = new PrintListenerImpl(context);
                    loadedBitmap = receiptPreviewTrans.preview(transData, listener);
                    hasSignature = signed;
                }
                //bitmap to byte data, then transfer data to PrintPreviewActivity
                Intent intent = new Intent(context, PrintPreviewActivity.class);
                ByteArrayOutputStream bitmapData = new ByteArrayOutputStream();
                loadedBitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapData);
                byte[] bitmapByte = bitmapData.toByteArray();
                intent.putExtra(EUIParamKeys.BITMAP.toString(), bitmapByte);
                intent.putExtra(PrintPreviewActivity.HAS_SIGNATURE, hasSignature);
                context.startActivity(intent);

                if (null != loadedBitmap) {
                    loadedBitmap.recycle();
                    loadedBitmap = null;
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

        public ActionPrintPreview create() {
            return new ActionPrintPreview(startListener, transData);
        }
    }
}
