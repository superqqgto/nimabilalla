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
package com.pax.pay.trans.transmit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.ConditionVariable;
import android.os.SystemClock;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionEndListener;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ActionResult;
import com.pax.dal.exceptions.PedDevException;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.view.dialog.CustomAlertDialog;

public class TransProcessListenerImpl implements TransProcessListener {

    private Context context;
    private CustomAlertDialog dialog;

    private ConditionVariable cv;
    private boolean isShowMessage;

    public TransProcessListenerImpl(Context context) {
        this.context = context;
        this.isShowMessage = true;
    }

    public TransProcessListenerImpl(Context context, boolean isShowMessage) {
        this.context = context;
        this.isShowMessage = isShowMessage;
    }

    private void ShowDialog(final String message, final int timeout, final int alertType) {
        if (!isShowMessage) {
            return;
        }
        if (dialog == null) {
            FinancialApplication.mApp.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    dialog = new CustomAlertDialog(context, alertType);
                    dialog.show();
                    dialog.setCancelable(false);
                    dialog.setTimeout(timeout);
                    dialog.setTitleText(title);
                    dialog.setContentText(message);
                }
            });
        } else {
            FinancialApplication.mApp.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    dialog.setTimeout(timeout);
                    dialog.setTitleText(title);
                    dialog.setContentText(message);
                }
            });
        }

    }

    @Override
    public void onShowProgress(final String message, final int timeout) {
        ShowDialog(message, timeout, CustomAlertDialog.PROGRESS_TYPE);
    }

    private int onShowMessageWithConfirm(final String message, final int timeout, final int alertType) {
        if (!isShowMessage) {
            return 0;
        }
        onHideProgress();
        cv = new ConditionVariable();
        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                CustomAlertDialog cfmDialog = new CustomAlertDialog(context, alertType, timeout);
                cfmDialog.setContentText(message);
                cfmDialog.show();
                cfmDialog.showConfirmButton(true);
                cfmDialog.setOnDismissListener(new OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface arg0) {
                        cv.open();
                    }
                });
            }
        });

        cv.block();
        return 0;
    }

    @Override
    public int onShowNormalMessageWithConfirm(final String message, final int timeout) {
        return onShowMessageWithConfirm(message, timeout, CustomAlertDialog.NORMAL_TYPE);
    }

    @Override
    public int onShowErrMessageWithConfirm(final String message, final int timeout) {
        return onShowMessageWithConfirm(message, timeout, CustomAlertDialog.ERROR_TYPE);
    }

    @Override
    public byte[] onCalcMac(byte[] data) {
        return Device.getMac(data);
    }

    @Override
    public byte[] onEncTrack(byte[] track) {
        int len = track.length;
        String trackStr;
        if (len % 2 > 0) {
            trackStr = new String(track) + "0";
        } else {
            trackStr = new String(track);
        }

        byte[] trackData = new byte[8];

        byte[] bTrack = GlManager.strToBcdPaddingLeft(trackStr);
        System.arraycopy(bTrack, bTrack.length - trackData.length - 1, trackData, 0, trackData.length);
        try {
            byte[] block = Device.calcDes(bTrack);
            System.arraycopy(block, 0, bTrack, bTrack.length - block.length - 1, block.length);
        } catch (PedDevException e) {
            e.printStackTrace();
        }

        return GlManager.bcdToStr(bTrack).substring(0, len).getBytes();
    }

    @Override
    public void onHideProgress() {

        if (dialog != null) {
            SystemClock.sleep(200);
            dialog.dismiss();
            dialog = null;
        }
    }

    private String title;

    @Override
    public void onUpdateProgressTitle(String title) {
        if (!isShowMessage) {
            return;
        }
        this.title = title;
    }

    private int result;

    @Override
    public int onInputOnlinePin(final TransData transData) {
        cv = new ConditionVariable();
        result = 0;

        final String totalAmount = transData.getTransType().isSymbolNegative() ? "-" + transData.getAmount() : transData.getAmount();
        final String tipAmount = transData.getTransType().isSymbolNegative() ? null : transData.getTipAmount();

        ActionEnterPin actionEnterPin = new ActionEnterPin(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(
                        transData.getTransType().getTransName(), transData.getPan(), true,
                        context.getString(R.string.prompt_pin),
                        context.getString(R.string.prompt_no_pin),
                        totalAmount, tipAmount,
                        ActionEnterPin.EEnterPinType.ONLINE_PIN);

            }
        });

        actionEnterPin.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult actionResult) {
                int ret = actionResult.getRet();
                if (ret == TransResult.SUCC) {
                    String data = (String) actionResult.getData();
                    transData.setPin(data);
                    if (data != null && data.length() > 0) {
                        transData.setHasPin(true);
                    } else {
                        transData.setHasPin(false);
                    }
                    result = 0;
                    cv.open();
                } else {
                    result = -1;
                    cv.open();
                }
                ActivityStack.getInstance().pop();
            }
        });
        actionEnterPin.execute();

        cv.block();

        return result;
    }
}
