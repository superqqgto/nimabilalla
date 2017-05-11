/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-28
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import android.content.Context;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eventbus.ClssLightStatusEvent;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.RetCode;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.clss.CTransResult;
import com.pax.pay.clss.ClssTransProcess;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.activity.SearchCardActivity;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.ContextUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lixc on 2017/2/28.
 */

public class ActionClssProcess extends AAction {

    private TransData transData;

    public ActionClssProcess(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(TransData transData) {
        this.transData = transData;
    }

    @Override
    protected void process() {
        FinancialApplication.mApp.runInBackground(new Runnable() {
            @Override
            public void run() {
                Context context = ContextUtils.getActyContext();
                TransProcessListener transProcessListener = new TransProcessListenerImpl(context);
                if (transData.getEnterMode() == TransData.EnterMode.CLSS) {
                    transProcessListener.onShowProgress(context.getString(R.string.wait_process), 0);
                }
                try {
                    ClssTransProcess clssTransProcess = ClssTransProcess.getInstance();
                    CTransResult result = clssTransProcess.transProcess(context, transData, transProcessListener);
                    if (result.getTransResult() != RetCode.EMV_OK) {
                        if (result.getTransResult() == CTransResult.App_Try_Again) {
                            transProcessListener.onHideProgress();
                            setResult(new ActionResult(TransResult.SUCC, result));
                        } else {
                            Device.beepErr();

                        EventBus.getDefault().post(new ClssLightStatusEvent(SearchCardActivity.CLSSLIGHTSTATUS_ERROR));

                            if (result.getAcResult() == ACType.AC_AAC) {
                                transProcessListener.onShowErrMessageWithConfirm(context.getString(R.string.dialog_clss_aac),
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            } else {
                                transProcessListener.onShowErrMessageWithConfirm(result.getMessage(result.getTransResult()),
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            }
                            setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                        }
                    } else {
                        Device.beepPrompt();
                        transProcessListener.onHideProgress();
                        setResult(new ActionResult(TransResult.SUCC, result));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
