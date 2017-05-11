/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-15
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.clss;

import com.pax.jemv.clcommon.ClssPreProcInfo;
import com.pax.jemv.clcommon.ClssReaderParam;
import com.pax.jemv.clcommon.ClssTransParam;
import com.pax.jemv.clcommon.ClssVisaAidParam;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.CountryCode;

public class ClssWaveParam {
    private ClssPreProcInfo clssPreProcInfo;
    private ClssReaderParam clssReaderParam;
    private ClssVisaAidParam clssVisaAidParam;
    private ClssTransParam paywaveTransParam;

    private final static byte RD_CVM_REQ_SIG = 0x01;
    private final static byte RD_CVM_REQ_ONLINE_PIN = 0x02;

    private static ClssWaveParam clssWaveParam;

    private ClssWaveParam() {

    }

    public static synchronized ClssWaveParam getInstance() {
        if (clssWaveParam == null) {
            clssWaveParam = new ClssWaveParam();
        }
        return clssWaveParam;
    }

    public void setClssPreProcInfo(ClssPreProcInfo clssPreProcInfo) {
        this.clssPreProcInfo = clssPreProcInfo;
    }

    public ClssPreProcInfo getClssPreProcInfo() {
        return clssPreProcInfo;
    }

    public ClssPreProcInfo getClssPreProcInfo(byte[] aucAID, byte ucAidLen) {
        byte[] TTQ = new byte[]{(byte) 0x36, (byte) 0x00, (byte) 0x80, (byte) 0x00};
        clssPreProcInfo = new ClssPreProcInfo(5000, 100000, 3000, 5000, aucAID, ucAidLen, (byte) 0,
                (byte) 1, (byte) 0, (byte) 0, TTQ, (byte) 1, (byte) 1, (byte) 1, (byte) 1, new byte[2]);
        return clssPreProcInfo;
    }

    public void setClssReaderParam(ClssReaderParam clssReaderParam) {
        this.clssReaderParam = clssReaderParam;
    }

    public ClssReaderParam getClssReaderParam(TransData transData) {
        clssReaderParam.aucTmCap = new byte[]{(byte) 0xE0, (byte) 0xE1, (byte) 0xC8};
        clssReaderParam.aucTmCapAd = new byte[]{(byte) 0xE0, (byte) 0x00, (byte) 0xF0, (byte) 0xA0, (byte) 0x01};
        clssReaderParam.ucTmType = 0x22;
        int code = CountryCode.getByCode(transData.getCurrency().getCountry()).getNumeric();
        clssReaderParam.aucTmCntrCode = new byte[]{(byte) (code / 100), (byte) (code % 100)};
        code = CountryCode.getByCode(transData.getCurrency().getCountry()).getCurrencyNumeric();
        clssReaderParam.aucTmRefCurCode = new byte[]{(byte) (code / 100), (byte) (code % 100)};
        clssReaderParam.aucTmTransCur = new byte[]{(byte) (code / 100), (byte) (code % 100)};
        return clssReaderParam;
    }

    public void setClssVisaAidParam(ClssVisaAidParam clssVisaAidParam) {
        this.clssVisaAidParam = clssVisaAidParam;
    }

    public ClssVisaAidParam getClssVisaAidParam() {
        byte[] aucCvmReq = new byte[5];
        aucCvmReq[0] = RD_CVM_REQ_SIG;
        aucCvmReq[1] = RD_CVM_REQ_ONLINE_PIN;
        clssVisaAidParam = new ClssVisaAidParam(5000, (byte) 0x00, (byte) 2, aucCvmReq, (byte) 0);
        return clssVisaAidParam;
    }

    public void setPaywaveTransParam(ClssTransParam paywaveTransParam) {
        this.paywaveTransParam = paywaveTransParam;
    }

    public ClssTransParam getPaywaveTransParam() {
        return paywaveTransParam;
    }
}
