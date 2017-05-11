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
package com.pax.pay.service;

import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.manager.AcqManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.base.Acquirer;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.TransData;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseResp {
    private static ParseResp parseResp;

    private ParseResp() {

    }

    public synchronized static ParseResp getInstance() {
        if (parseResp == null) {
            parseResp = new ParseResp();
        }

        return parseResp;
    }

    public String parse(ActionResult result) {
        try {
            String temp;
            JSONObject json = new JSONObject();

            if (result == null) {
                json.put(ServiceConstant.RSP_CODE, TransResult.ERR_HOST_REJECT);
                return json.toString();
            }

            ParseReq.RequestData requestData = ParseReq.getInstance().getRequestData();
            if (requestData != null) {
                temp = requestData.getAppId();
                if (temp != null && temp.length() != 0) {
                    json.put(ServiceConstant.APP_ID, temp);
                }
            }

            // 应答码
            json.put(ServiceConstant.RSP_CODE, result.getRet());

            if (result.getRet() != TransResult.SUCC) {
                temp = (String) result.getData();

                // 错误应答信息
                if (temp != null && temp.length() != 0) {
                    json.put(ServiceConstant.RSP_MSG, result.getData());
                }
                return json.toString();
            }

            if (result.getData() == null) {
                return json.toString();
            }

            if (result.getData() instanceof CardInformation) {
                CardInformation cardInfo = (CardInformation) result.getData();

                if (cardInfo != null) {
                    json.put(ServiceConstant.CARD_NO, cardInfo.getPan());
                }

                return json.toString();
            }

            // 商户名称
            json.put(ServiceConstant.MERCH_NAME, SpManager.getSysParamSp().get(SysParamSp.EDC_MERCHANT_NAME_EN));

            Acquirer curAcq = AcqManager.getInstance().getCurAcq();

            // 商户编号
            json.put(ServiceConstant.MERCH_ID, curAcq.getMerchantId());
            // 终端编号
            json.put(ServiceConstant.TERM_ID, curAcq.getTerminalId());

            TransData transData = (TransData) result.getData();

            // 卡号
            temp = transData.getPan();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.CARD_NO, PanUtils.maskCardNo(transData.getPan(), transData.getIssuer().getPanMaskPattern()));
            }

            // 凭证号
            temp = String.valueOf(transData.getTraceNo());
            if (temp.length() != 0) {
                json.put(ServiceConstant.VOUCHER_NO, Component.getPaddedNumber(transData.getTraceNo(), 6));
            }

            // 批次号
            temp = String.valueOf(transData.getBatchNo());
            if (temp.length() != 0) {
                json.put(ServiceConstant.BATCH_NO, Component.getPaddedNumber(transData.getBatchNo(), 6));
            }

            // 发卡行号
            temp = transData.getIssuerCode();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.ISSER_CODE, temp);
            }

            // 收单行号
            temp = transData.getAcqCode();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.ACQ_CODE, temp);
            }

            // 授权码
            temp = transData.getAuthCode();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.AUTH_NO, temp);
            }

            // 参考号
            temp = transData.getRefNo();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.REF_NO, temp);
            }

            // 交易时间/日期
            temp = transData.getDateTime();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.TRANS_TIME, temp.substring(8));
                json.put(ServiceConstant.TRANS_DATE, temp.substring(4, 8));
            }


            // 交易金额
            temp = transData.getAmount();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.TRANS_AMOUNT, temp);
            }

            // 原授权码
            temp = transData.getOrigAuthCode();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.ORIG_AUTH_NO, temp);
            }

            // 原凭证号
            temp = String.valueOf(transData.getOrigTransNo());
            if (temp.length() != 0 && !temp.equals("0")) {
                json.put(ServiceConstant.ORIG_VOUCHER_NO, Component.getPaddedNumber(transData.getOrigTransNo(), 6));
            }

            // 原参考号
            temp = transData.getOrigRefNo();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.ORIG_REF_NO, temp);
            }

            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();

        }
        return null;
    }

}
