/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-14
 * Module Author: laiyi
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.clss;

import android.util.Log;

import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.device.model.ApduRespL2;
import com.pax.jemv.device.model.ApduSendL2;
import com.pax.jemv.paypass.api.ClssPassApi;
import com.pax.jemv.paypass.listener.IClssPassCBFun;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.app.FinancialApplication;

public class ClssPassListen implements IClssPassCBFun {

    public static ByteArray aucOutcomeParamSet = new ByteArray();
    public static ByteArray aucUserInterReqData = new ByteArray();
    public static ByteArray aucErrIndication = new ByteArray();

    @Override
    public int sendDEKData(byte[] bytes, int i) {
        Log.i("sendDEKData", "call back!!!!!!!!");
        return 0;
    }

    @Override
    public int receiveDETData(ByteArray byteArray, byte[] bytes) {
        Log.i("receiveDETData", "call back!!!!!!!!");
        return 0;
    }

    @Override
    public int addAPDUToTransLog(ApduSendL2 apduSendL2, ApduRespL2 apduRespL2) {
        Log.i("addAPDUToTransLog", "call back!!!!!!!!");
        return 0;
    }

    @Override
    public int sendTransDataOutput(byte b) {
        Log.i("log", "call back!!!!!!!!");
        if ((b & 0x01) != 0) {
            Log.i("log", "0x01");
            byte[] tagList = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x29};
            ClssPassApi.clssMcGetTLVDataList(tagList, (byte) 3, (byte) 8, aucOutcomeParamSet);
            Log.i("log", "setDetData :" + GlManager.bcdToStr(aucOutcomeParamSet.data));
        }

        if ((b & 0x04) != 0) {
            Log.i("log", "0x04");
            byte[] tagList = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x16};
            ClssPassApi.clssMcGetTLVDataList(tagList, (byte) 3, (byte) 8, aucUserInterReqData);
            Log.i("log", "setDetData :" + GlManager.bcdToStr(aucUserInterReqData.data));
        }

        if ((b & 0x02) != 0) {
            Log.i("log", "0x02");
            byte[] tagList = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x15};
            ClssPassApi.clssMcGetTLVDataList(tagList, (byte) 3, (byte) 8, aucErrIndication);
            Log.i("log", "setDetData :" + GlManager.bcdToStr(aucErrIndication.data));
        }

        return RetCode.EMV_OK;
    }
}
