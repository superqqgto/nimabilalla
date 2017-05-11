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

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.enums.ETransResult;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.eemv.exception.EmvException;
import com.pax.manager.sp.SpManager;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.EmvDeviceListenerImpl;
import com.pax.pay.emv.EmvListenerImpl;
import com.pax.pay.emv.EmvTransProcess;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.BaseTransData.*;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.ContextUtils;
import com.pax.manager.sp.SysParamSp;

public class ActionEmvProcess extends AAction {

    private TransData transData;

    public ActionEmvProcess(ActionStartListener listener) {
        super(listener);
    }

    private ActionEmvProcess(ActionStartListener listener, TransData transData) {
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
                TransProcessListener transProcessListener = new TransProcessListenerImpl(context);
                EmvListenerImpl emvListener = new EmvListenerImpl(context, transData, transProcessListener);
                EmvDeviceListenerImpl emvDeviceListenerImpl = new EmvDeviceListenerImpl(context, transData);
                if (transData.getEnterMode() == EnterMode.INSERT) {
                    transProcessListener.onShowProgress(context.getString(R.string.wait_process), 0);
                }
                try {
                    EmvTransProcess emvTransProcess = EmvTransProcess.getInstance();
                    ETransResult result = emvTransProcess.transProcess(transData, emvListener, emvDeviceListenerImpl);
                    transProcessListener.onHideProgress();
                    setResult(new ActionResult(TransResult.SUCC, result));
                } catch (EmvException e) {
                    e.printStackTrace();

                    // FIXME workaround for DEMO mode
                    String commType = SpManager.getSysParamSp().get(SysParamSp.APP_COMM_TYPE);
                    if (SysParamSp.Constant.COMMTYPE_DEMO.equals(commType) &&
                            e.getErrCode() == EEmvExceptions.EMV_ERR_UNKNOWN.getErrCodeFromBasement()) {
                        transProcessListener.onHideProgress();
                        // end the EMV process, and continue a mag process
                        setResult(new ActionResult(TransResult.SUCC, ETransResult.ARQC));
                        return;
                    }

                    Device.beepErr();
                    if (e.getErrCode() != EEmvExceptions.EMV_ERR_UNKNOWN.ordinal()) {
                        if (e.getErrCode() == EEmvExceptions.EMV_ERR_FALL_BACK.ordinal()) {
                            transProcessListener.onShowErrMessageWithConfirm(
                                    context.getString(R.string.err_card_unsupported_demotion),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        } else if (e.getErrCode() == EEmvExceptions.EMV_ERR_USER_CANCEL.ordinal()) {
                            // 用户取消， 不提示
                            transProcessListener.onShowErrMessageWithConfirm(
                                    context.getString(R.string.err_user_cancel),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        } else {
                            transProcessListener.onShowErrMessageWithConfirm(e.getErrMsg(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                    transProcessListener.onHideProgress();
                    setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                }
            }
        });
    }

    public static class Builder {

        ActionStartListener startListener;
        private TransData transData;

        public Builder startListener(ActionStartListener startListener) {
            this.startListener = startListener;
            return this;
        }

        public Builder transData(TransData transData) {
            this.transData = transData;
            return this;
        }

        public ActionEmvProcess create() {
            return new ActionEmvProcess(startListener, transData);
        }
    }
}
