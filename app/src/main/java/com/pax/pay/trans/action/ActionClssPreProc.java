/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-16
 * Module Author: laiyi
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.jemv.clcommon.ClssPreProcInfo;
import com.pax.jemv.clcommon.ClssTransParam;
import com.pax.jemv.clcommon.KernType;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.device.DeviceImpl;
import com.pax.jemv.device.DeviceManager;
import com.pax.jemv.entrypoint.api.ClssEntryApi;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.clss.ClssWaveParam;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.EmvAid;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;

import java.util.List;

public class ActionClssPreProc extends AAction {

    public ActionClssPreProc(ActionStartListener listener) {
        super(listener);
    }

    private TransData transData;

    public void setParam(TransData transData) {
        this.transData = transData;
    }

    @Override
    protected void process() {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int ret;
                DeviceManager.getInstance().setIDevice(new DeviceImpl());
                ClssWaveParam clssWaveParam = ClssWaveParam.getInstance();

                ret = ClssEntryApi.clssEntryCoreInit();
                if (ret != RetCode.EMV_OK) {
                    setResult(new ActionResult(TransResult.ERR_CLSSPROPROC, null));
                    return;
                }

                List<EmvAid> aidList = DbManager.getEmvDao().findAllAID();
                for (EmvAid aid : aidList) {
                    ClssEntryApi.clssEntryAddAidList(DeviceImpl.str2Bcd(aid.getAid()),
                            (byte) (aid.getAid().length() / 2), (byte) aid.getSelFlag(), (byte) KernType.KERNTYPE_DEF);
                    ClssPreProcInfo clssPreProcInfo = clssWaveParam.getClssPreProcInfo(DeviceImpl.str2Bcd(aid.getAid()), (byte) (aid.getAid().length() / 2));
                    clssWaveParam.setClssPreProcInfo(clssPreProcInfo);
                    ClssEntryApi.clssEntrySetPreProcInfo(clssPreProcInfo);
                }

                long ulAmntAuth = Long.parseLong(transData.getAmount());
                String date = Device.getTime(Constants.TIME_PATTERN_TRANS2).substring(0, 6);
                String time = Device.getTime(Constants.TIME_PATTERN_TRANS2).substring(6);
                byte[] date_byte = GlManager.strToBcdPaddingRight(date);
                byte[] time_byte = GlManager.strToBcdPaddingRight(time);

                ClssTransParam paywaveTransParam = new ClssTransParam(ulAmntAuth, 0, Long.parseLong(SpManager.getSysParamSp().get(SysParamSp.EDC_TRACE_NO)), (byte) 0x00, date_byte, time_byte);
                clssWaveParam.setPaywaveTransParam(paywaveTransParam);
                ret = ClssEntryApi.clssEntryPreTransProc(paywaveTransParam);
                if (ret != RetCode.EMV_OK) {
                    setResult(new ActionResult(TransResult.ERR_CLSSPROPROC, null));
                    return;
                }

                setResult(new ActionResult(TransResult.SUCC, null));
            }
        });

    }
}
