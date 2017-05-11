/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-26
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.receipt;

import android.content.Context;
import android.os.ConditionVariable;

import com.pax.pay.app.FinancialApplication;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;

public class PrintListenerImpl implements PrintListener {

    private Context context;

    public PrintListenerImpl(Context context) {

        this.context = context;
    }

    private CustomAlertDialog showMsgDialog;
    private CustomAlertDialog confirmDialog;

    @Override
    public void onShowMessage(final String title, final String message) {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (showMsgDialog == null) {
                    showMsgDialog = new CustomAlertDialog(context, CustomAlertDialog.PROGRESS_TYPE);
                    showMsgDialog.show();
                    showMsgDialog.setCancelable(false);
                    showMsgDialog.setTitleText(title);
                    showMsgDialog.setContentText(message);

                } else {
                    showMsgDialog.setTitleText(title);
                    showMsgDialog.setContentText(message);
                }
            }
        });
    }

    private ConditionVariable cv;
    private int result = -1;

    @Override
    public int onConfirm(final String title, final String message) {
        cv = new ConditionVariable();
        result = -1;
        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (confirmDialog != null) {
                    confirmDialog.dismiss();
                }
                confirmDialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE);
                confirmDialog.show();
                confirmDialog.setTitleText(title);
                confirmDialog.setContentText(message);
                confirmDialog.setCancelable(false);
                confirmDialog.setCanceledOnTouchOutside(false);
                confirmDialog.showCancelButton(true);
                confirmDialog.setCancelClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        result = 1;
                        if (cv != null) {
                            cv.open();
                        }
                    }
                });
                confirmDialog.showConfirmButton(true);
                confirmDialog.setConfirmClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        result = 0;
                        if (cv != null) {
                            cv.open();
                        }
                    }
                });
                confirmDialog.show();

            }
        });
        cv.block();
        return result;
    }

    @Override
    public void onEnd() {
        if (showMsgDialog != null) {
            showMsgDialog.dismiss();
        }
        if (confirmDialog != null) {
            confirmDialog.dismiss();
        }
    }

}
