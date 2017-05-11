/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-10
 * Module Author: laiyi
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.clss;

import android.util.Log;

import com.pax.jemv.clcommon.KernType;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.emv.EmvTestAID;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.CountryCode;

public class ClssPassParam {
    private static final byte kernType = KernType.KERNTYPE_MC;


    static void SetMcTermParam(TransData transData, byte[] aidBuff) {
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.AppVerTag, new byte[]{(byte) 0x00, (byte) 0x02}, kernType);

        ClssTransProcess.getInstance().setDetData(ClssTlvTag.CardDataTag, new byte[]{(byte) 0xE0}, kernType);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.CvmReqTag, new byte[]{(byte) 0x60}, kernType);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.CvmNoTag, new byte[]{(byte) 0x08}, kernType);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.SecTag, new byte[]{(byte) 0xC8}, kernType);

        ClssTransProcess.getInstance().setDetData(ClssTlvTag.MagCvmReqTag, new byte[]{(byte) 0x20}, kernType);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.MagCvmNoTag, new byte[]{(byte) 0x00}, kernType);

        long total = Long.parseLong(transData.getAmount()) + Long.parseLong(transData.getTipAmount());
        byte[] tmp = GlManager.strToBcdPaddingLeft(Long.toString(total));
        byte[] amount = new byte[6];
        System.arraycopy(tmp, 0, amount, 6 - tmp.length, tmp.length);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.Amount_Tag, amount, kernType);
        String date = transData.getDateTime().substring(2, 8);
        String time = transData.getDateTime().substring(8, 14);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.TransType_Tag, new byte[]{(byte) 0x00}, kernType);
        tmp = GlManager.strToBcdPaddingLeft(date);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.TransDate_Tag, tmp, kernType);
        tmp = GlManager.strToBcdPaddingLeft(time);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.TransTime_Tag, tmp, kernType);

        ClssTransProcess.getInstance().setDetData(ClssTlvTag.TermDefaultTag, new byte[]{(byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00}, kernType);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.TermDenialTag, new byte[]{(byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00}, kernType);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.TermOnlineTag, new byte[]{(byte) 0xF8, (byte) 0x50, (byte) 0xAC, (byte) 0xF8, (byte) 0x00}, kernType);

        ClssTransProcess.getInstance().setDetData(ClssTlvTag.FloorLimitTag, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x50, (byte) 0x00}, kernType);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.TransLimitTag, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00}, kernType);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.TransCvmLimitTag, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00}, kernType);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.CvmLimitTag, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x30, (byte) 0x00}, kernType);

        ClssTransProcess.getInstance().setDetData(ClssTlvTag.MaxTornTag, new byte[]{(byte) 0x00}, kernType);

        int code = CountryCode.getByCode(transData.getCurrency().getCountry()).getNumeric();
        byte[] countryCode = new byte[]{(byte) (code / 100), (byte) (code % 100)};
        code = CountryCode.getByCode(transData.getCurrency().getCountry()).getCurrencyNumeric();
        byte[] currencyCode = new byte[]{(byte) (code / 100), (byte) (code % 100)};
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.CountryCodeTag, countryCode, kernType);
        ClssTransProcess.getInstance().setDetData(ClssTlvTag.CurrencyCodeTag, currencyCode, kernType);

        byte[] aid = new byte[7];
        System.arraycopy(aidBuff, 1, aid, 0, 7);
        if (EmvTestAID.MASTER_MCHIP.getAid().equals(GlManager.bcdToStr(aid))) {
            ClssTransProcess.getInstance().setDetData(ClssTlvTag.KernCfgTag, new byte[]{(byte) 0x20}, kernType);
            Log.i("MasterTag", "MCHIP");
        } else if (EmvTestAID.MASTER_MAESTRO.getAid().equals(GlManager.bcdToStr(aid))) {
            ClssTransProcess.getInstance().setDetData(ClssTlvTag.KernCfgTag, new byte[]{(byte) 0xA0}, kernType);
            Log.i("MasterTag", "Mestro");
        }
    }
}
