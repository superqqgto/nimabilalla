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
package com.pax.pay.trans.action;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ContextUtils;
import com.pax.view.dialog.InputPwdDialog;
import com.pax.view.dialog.InputPwdDialog.OnPwdListener;

public class ActionInputPassword extends AAction {

    public ActionInputPassword(ActionStartListener listener) {
        super(listener);
    }

    private int maxLen;
    private String title;
    private String subTitle;
    private boolean allowCanceledOnTouchOutside = true;

    public void setParam(int maxLen, String title, String subTitle) {
        this.maxLen = maxLen;
        this.title = title;
        this.subTitle = subTitle;
        this.allowCanceledOnTouchOutside = true;
    }

    public void setParam(int maxLen, String title, String subTitle, boolean allowCanceledOnTouchOutside) {
        this.maxLen = maxLen;
        this.title = title;
        this.subTitle = subTitle;
        this.allowCanceledOnTouchOutside = allowCanceledOnTouchOutside;
    }

    private InputPwdDialog dialog = null;

    @Override
    protected void process() {
        FinancialApplication.mApp.runOnUiThreadDelay(new Runnable() {

            @Override
            public void run() {
                Context context = ContextUtils.getActyContext();
                dialog = new InputPwdDialog(context, maxLen, title, subTitle);
                dialog.setOnKeyListener(new OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
                dialog.setCancelable(false);
                dialog.setPwdListener(new OnPwdListener() {
                    @Override
                    public void onSucc(String data) {

                        setResult(new ActionResult(TransResult.SUCC, data));
                        dialog.dismiss();
                    }

                    @Override
                    public void onErr() {

                        setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                        dialog.dismiss();
                    }
                });

                //AET-50
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                        dialog.dismiss();
                    }
                });

                dialog.setCanceledOnTouchOutside(allowCanceledOnTouchOutside); // AET-17
                dialog.show();
            }
        }, 100);

    }

}
