/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Auth: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.emv;

import android.annotation.SuppressLint;

import com.pax.eemv.*;
import com.pax.eemv.entity.*;
import com.pax.eemv.enums.*;
import com.pax.eemv.exception.EmvException;
import com.pax.manager.AcqManager;
import com.pax.manager.neptune.EmvManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.BaseTransData.*;
import com.pax.pay.utils.CountryCode;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.utils.LogUtils;

import java.util.Currency;
import java.util.List;

public class EmvTransProcess {
    private static EmvTransProcess emvTransProcess;

    private EmvTransProcess() {

    }

    public static synchronized EmvTransProcess getInstance() {
        if (emvTransProcess == null) {
            emvTransProcess = new EmvTransProcess();
        }
        return emvTransProcess;
    }

    public ETransResult transProcess(TransData transData, IEmvListener listener, IEmvDeviceListener deviceListener) throws EmvException {
        EmvManager.setListener(listener);
        EmvManager.setDeviceListener(deviceListener);
        ETransResult result = EmvManager.emvProcess(toInputParam(transData));
        // 将交易类型恢复

        LogUtils.i("TAG", "EMV PROC:" + result.toString());

        //byte[] value95 = FinancialApplication.emv.getTlv(0x95);
        //byte[] value9B = FinancialApplication.emv.getTlv(0x9B);

        //Log.e("TLV", "95:" + FinancialApplication.convert.bcdToStr(value95));
        //Log.e("TLV", "9b:" + FinancialApplication.convert.bcdToStr(value9B));
        return result;
    }

    public long getEcBalance(EChannelType type, IEmvListener listener, IEmvDeviceListener deviceListener) {
        EmvManager.setListener(listener);
        EmvManager.setDeviceListener(deviceListener);
        return EmvManager.readEcBalance(type);
    }

    public List<byte[]> getAllLogRecord(EChannelType type, ELogType logType, IEmvListener listener) {
        EmvManager.setListener(listener);
        return EmvManager.readAllLogRecord(type, logType);
    }

    /**
     * EMV初始化，设置aid，capk和emv配置
     */
    public void init() {

        EmvManager.emvInit();
        setEmvConfig();

        EmvManager.setAidParamList(EmvAid.toAidParams());
        EmvManager.setCapkList(EmvCapk.toCapk());
    }

    private void setEmvConfig() {
        Config cfg = EmvManager.getConfig();

        Currency current = Currency.getInstance(CurrencyConverter.getDefCurrency());
        byte[] currency = GlManager.strToBcdPaddingLeft(String.valueOf(
                CountryCode.getByCode(CurrencyConverter.getDefCurrency().getCountry()).getCurrencyNumeric()));

        cfg.setCapability(new byte[]{(byte) 0xE0, (byte) 0xF0, (byte) 0xC8});
        cfg.setCountryCode(currency);
        cfg.setExCapability(new byte[]{(byte) 0xE0, 0x00, (byte) 0xF0, (byte) 0xA0, 0x01});
        cfg.setForceOnline((byte) 0);
        cfg.setGetDataPIN((byte) 1);
        cfg.setMerchCateCode(new byte[]{0x00, 0x00});
        cfg.setReferCurrCode(currency);
        cfg.setReferCurrCon(1000);
        cfg.setReferCurrExp((byte) current.getDefaultFractionDigits());
        cfg.setSurportPSESel((byte) 1);
        cfg.setTermType((byte) 0x22);
        cfg.setTransCurrCode(currency);
        cfg.setTransCurrExp((byte) current.getDefaultFractionDigits());
        cfg.setTransType((byte) 0x02);
        cfg.setTermId(AcqManager.getInstance().getCurAcq().getTerminalId());
        cfg.setMerchId(AcqManager.getInstance().getCurAcq().getMerchantId());
        cfg.setMerchName(SpManager.getSysParamSp().get(SysParamSp.EDC_MERCHANT_NAME_EN));
        cfg.setTermAIP(new byte[]{0x7C, 0x00});
        cfg.setBypassPin((byte) 1); // 输密码支持bypass
        EmvManager.setConfig(cfg);
    }

    @SuppressLint("DefaultLocale")
    private InputParam toInputParam(TransData transData) {
        InputParam inputParam = new InputParam();
        ETransType transType = transData.getTransType();

        String amount = transData.getAmount();
        if (amount == null || amount.length() == 0) {
            amount = "0";
        }
        inputParam.setAmount(Component.getPaddedNumber(Long.parseLong(amount), 12));
        inputParam.setCashBackAmount("0");
        if (transData.getEnterMode() == EnterMode.INSERT) {
            inputParam.setChannelType(EChannelType.ICC);
        } else {
            inputParam.setChannelType(EChannelType.PICC);
        }
        if ((transData.getTransType().equals(ETransType.SALE))
                || (transData.getTransType().equals(ETransType.PREAUTH))) {
            if (transData.getEnterMode() == EnterMode.INSERT) {
                inputParam.setFlowType(EFlowType.COMPLETE);
            } else {
                inputParam.setFlowType(EFlowType.QPBOC);
            }
            inputParam.setIsCardAuth(true);
            inputParam.setIsSupportCvm(true);

        } else {
            // 联机Q非消费，余额查询，预授权均走简单Q流程
            if (transData.getEnterMode() == EnterMode.CLSS) {
                inputParam.setFlowType(EFlowType.SIMPLE);
            } else {
                inputParam.setFlowType(EFlowType.SIMPLE);
            }
            inputParam.setIsCardAuth(false);
            inputParam.setIsSupportCvm(false);
        }
        byte[] procCode = GlManager.strToBcdPaddingRight(transType.getProcCode());
        inputParam.setTag9CValue(procCode[0]);

        // 根据交易类型判断是否强制联机
        inputParam.setIsForceOnline(true);
        inputParam.setIsSupportEC(false);


        inputParam.setIsSupportSM(true);


        inputParam.setTransDate(transData.getDateTime().substring(0, 8));
        inputParam.setTransTime(transData.getDateTime().substring(8));
        inputParam.setTransTraceNo(Component.getPaddedNumber(transData.getTraceNo(), 6));

        return inputParam;

    }
}
