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
package com.pax.pay.emv;

import android.content.Context;
import android.os.ConditionVariable;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionEndListener;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.eemv.IEmvDeviceListener;
import com.pax.eemv.entity.RSAPinKey;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionEnterPin.OfflinePinResult;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.LogUtils;

public class EmvDeviceListenerImpl implements IEmvDeviceListener {

    private Context context;
    private TransData transData;
    private ConditionVariable cv;
    private int intResult;

    private String totalAmount;
    private String tipAmount;

    public EmvDeviceListenerImpl(Context context, TransData transData) {
        this.context = context;
        this.transData = transData;

        totalAmount = transData.getTransType().isSymbolNegative() ? "-" + transData.getAmount() : transData.getAmount();
        tipAmount = transData.getTransType().isSymbolNegative() ? null : transData.getTipAmount();
    }

    private String header;
    private String subHeader;

    @Override
    public int onVerifyCipherPin(RSAPinKey arg0, int leftPinTimes, final byte[] arg1) {
        header = context.getString(R.string.prompt_offline_pin);
        subHeader = context.getString(R.string.prompt_no_pin);

        ActionEnterPin actionEnterPin = new ActionEnterPin(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(
                        transData.getTransType().getTransName(), "", true, header, subHeader,
                        totalAmount, tipAmount, EEnterPinType.OFFLINE_PLAIN_PIN);
            }
        });

        actionEnterPin.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                int ret = result.getRet();
                OfflinePinResult offlinePinResult = (OfflinePinResult) result.getData();
                if (ret == TransResult.SUCC) {
                    intResult = offlinePinResult.getRet();
                    arg1[0] = offlinePinResult.getRespOut()[0];
                    arg1[1] = offlinePinResult.getRespOut()[1];
                    if (cv != null)
                        cv.open();
                } else {
                    intResult = offlinePinResult.getRet();
                    if (cv != null)
                        cv.open();
                }
                ActivityStack.getInstance().pop();
            }
        });
        actionEnterPin.execute();
        cv = new ConditionVariable();
        cv.block();
        return intResult;
    }

    @Override
    public int onVerifyPlainPin(int leftPinTimes, final byte[] arg0) {
        header = context.getString(R.string.prompt_offline_pin);
        if (leftPinTimes == 1) {
            header += context.getString(R.string.emv_input_last);
        }
        subHeader = context.getString(R.string.prompt_no_pin);

        ActionEnterPin actionEnterPin = new ActionEnterPin(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(
                        transData.getTransType().getTransName(), "", true, header, subHeader,
                        totalAmount, tipAmount, EEnterPinType.OFFLINE_PLAIN_PIN);
            }
        });

        actionEnterPin.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                int ret = result.getRet();
                OfflinePinResult offlinePinResult = (OfflinePinResult) result.getData();
                if (ret == TransResult.SUCC) {
                    intResult = offlinePinResult.getRet();
                    if (arg0 == null) {
                        LogUtils.i("TAG", "onVerifyPlainPin: arg0 = NULL");
                    } else {
                        LogUtils.i("TAG", "onVerifyPlainPin: arg0 len = " + arg0.length);
                    }
                    byte[] pinResult = offlinePinResult.getRespOut();
                    if (pinResult == null) {
                        LogUtils.i("TAG", "onVerifyPlainPin: pinResult = NULL");
                    } else if (pinResult.length == 0) {
                        LogUtils.i("TAG", "onVerifyPlainPin: pinResult len = 0");
                    } else {
                        LogUtils.i("TAG", "onVerifyPlainPin pinResult:" + GlManager.bcdToStr(pinResult));
                    }
                    if (arg0 != null && arg0.length > 1) {
                        arg0[0] = offlinePinResult.getRespOut()[0];
                        arg0[1] = offlinePinResult.getRespOut()[1];
                    }
                    if (cv != null)
                        cv.open();
                } else {
                    intResult = offlinePinResult.getRet();
                    if (cv != null)
                        cv.open();
                }
                ActivityStack.getInstance().pop();
            }
        });
        actionEnterPin.execute();
        cv = new ConditionVariable();
        cv.block();
        return intResult;
    }

}
