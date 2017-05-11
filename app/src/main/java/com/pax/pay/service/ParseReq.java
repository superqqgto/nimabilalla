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

import android.annotation.SuppressLint;

import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class ParseReq {
    private static ParseReq parseReq;

    private ParseReq() {

    }

    public synchronized static ParseReq getInstance() {
        if (parseReq == null) {
            parseReq = new ParseReq();
        }

        return parseReq;
    }

    private JSONObject json;
    private RequestData requestData;

    public int check(JSONObject json) {
        this.json = json;
        String transType = "";
        int ret = -1;
        requestData = new RequestData();

        ret = checkTransType();
        if (ret != TransResult.SUCC) {
            return ret;
        }

        ret = checkAppID();
        if (ret != TransResult.SUCC) {
            return ret;
        }

        transType = requestData.getTransType();

        switch (transType) {
            case ServiceConstant.TRANS_SALE:
            case ServiceConstant.TRANS_AUTH:
                ret = checkTransAmount1();
                if (ret != TransResult.SUCC) {
                    return ret;
                }

                ret = checkTipAmount();
                if (ret != TransResult.SUCC) {
                    return ret;
                }
                break;
            case ServiceConstant.TRANS_VOID:
                ret = checkVoucherNo();
                if (ret != TransResult.SUCC) {
                    return ret;
                }
                break;
            case ServiceConstant.TRANS_REFUND:
            case ServiceConstant.TRANS_SETTLE:
            case ServiceConstant.TRANS_PRN_LAST:
            case ServiceConstant.TRANS_PRN_ANY:
            case ServiceConstant.TRANS_PRN_DETAIL:
            case ServiceConstant.TRANS_PRN_TOTAL:
            case ServiceConstant.TRANS_PRN_LAST_BATCH:
            case ServiceConstant.TRANS_GET_CARD_NO:
            case ServiceConstant.TRANS_SETTING:
                return TransResult.SUCC;
            case ServiceConstant.PRN_BITMAP:
                ret = checkBitmap();
                if (ret != TransResult.SUCC) {
                    return ret;
                }
                break;
            default:
                return TransResult.ERR_PARAM;
        }

        return TransResult.SUCC;

    }

    public RequestData getRequestData() {
        return requestData;
    }

    /**
     * 检查交易类型
     *
     * @return
     */
    private int checkTransType() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.TRANS_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
            return TransResult.ERR_PARAM;
        }

        if (temp == null || temp.length() < 1) {
            return TransResult.ERR_PARAM;
        }

        requestData.setTransType(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查交易金额
     *
     * @return
     */

    /**
     * 检查交易金额(不存在或者格式错都返回ERR_PARAM)
     *
     * @return
     */
    private int checkTransAmount1() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.TRANS_AMOUNT);
        } catch (JSONException e) {
            e.printStackTrace();
            return TransResult.ERR_PARAM;
        }

        // 交易金额
        if (temp == null || temp.length() < 1) {
            return TransResult.ERR_PARAM;
        }

        long amount = 0;
        try {
            amount = Long.parseLong(temp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (amount == 0) {
            return TransResult.ERR_PARAM;
        }
        requestData.setTransAmount(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查交易金额(不存在返回SUCC,格式错返回ERR_PARAM)
     *
     * @return
     */
    private int checkTransAmount2() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.TRANS_AMOUNT);
        } catch (JSONException e) {
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        long amount = 0;
        try {
            amount = Long.parseLong(temp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (amount == 0) {
            return TransResult.ERR_PARAM;
        }
        requestData.setTransAmount(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查小费金额(不存在返回SUCC,格式错返回ERR_PARAM)
     *
     * @return
     */
    private int checkTipAmount() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.TIP_AMOUNT);
        } catch (JSONException e) {
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        long amount = 0;
        try {
            amount = Long.parseLong(temp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        requestData.setTipAmount(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查应用ID
     *
     * @return
     */
    private int checkAppID() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.APP_ID);
        } catch (JSONException e) {
            e.printStackTrace();
            return TransResult.ERR_PARAM;
        }
        if (temp == null || temp.length() == 0) {
            return TransResult.ERR_PARAM;
        }

        requestData.setAppId(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查原交易日期
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    private int checkOrigDate() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.ORIG_DATE);
        } catch (JSONException e) {
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        if (temp.length() != 4) {
            return TransResult.ERR_PARAM;
        }

        // 检查合法性
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
        try {
            dateFormat.setLenient(false);
            dateFormat.parse(temp);
        } catch (Exception e) {
            e.printStackTrace();
            return TransResult.ERR_PARAM;
        }
        requestData.setOrigDate(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查原授权码
     *
     * @return
     */
    private int checkOrigAuthNo() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.ORIG_AUTH_NO);
        } catch (JSONException e) {
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        if (temp.length() > 6) {
            return TransResult.ERR_PARAM;
        }

        if (temp.length() < 6) {
            int flag = 6 - temp.length();
            for (int i = 0; i < flag; i++) {
                temp = "0" + temp;
            }
        }
        requestData.setOrigAuthNo(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查图片
     *
     * @return
     */
    private int checkBitmap() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.PRN_BMP);
        } catch (JSONException e) {
            e.printStackTrace();
            return TransResult.ERR_PARAM;
        }

        if (temp == null || temp.length() < 1) {
            return TransResult.ERR_PARAM;
        }

        requestData.setBitmap(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查凭证号
     *
     * @return
     */
    private int checkVoucherNo() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.VOUCHER_NO);
        } catch (JSONException e) {
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        if (temp.length() > 6) {
            return TransResult.ERR_PARAM;
        }

        long voucherNo = -1;
        try {
            voucherNo = Long.parseLong(temp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (voucherNo < 0) {
            return TransResult.ERR_PARAM;
        }

        requestData.setVoucherNo(Component.getPaddedNumber(voucherNo, 6));
        return TransResult.SUCC;
    }

    /**
     * 检查原参考号
     *
     * @return
     */
    private int checkOriRefNo() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.ORIG_REF_NO);
        } catch (JSONException e) {
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        if (temp.length() > 12) {
            return TransResult.ERR_PARAM;
        }

        long refNo = -1;
        try {
            refNo = Long.parseLong(temp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (refNo < 0) {
            return TransResult.ERR_PARAM;
        }

        requestData.setOriRefNo(Component.getPaddedNumber(refNo, 12));
        return TransResult.SUCC;
    }

    public static class RequestData {
        String appId;
        String appName;
        String transType;
        String transAmount;
        String tipAmount;
        String origDate;
        String origAuthNo;
        String bitmap;
        String voucherNo;
        String oriRefNo;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getTransType() {
            return transType;
        }

        public void setTransType(String transType) {
            this.transType = transType;
        }

        public String getTransAmount() {
            return transAmount;
        }

        public void setTransAmount(String transAmount) {
            this.transAmount = transAmount;
        }

        public String getTipAmount() {
            return tipAmount;
        }

        public void setTipAmount(String tipAmount) {
            this.tipAmount = tipAmount;
        }

        public String getOrigDate() {
            return origDate;
        }

        public void setOrigDate(String origDate) {
            this.origDate = origDate;
        }

        public String getOrigAuthNo() {
            return origAuthNo;
        }

        public void setOrigAuthNo(String origAuthNo) {
            this.origAuthNo = origAuthNo;
        }

        public String getBitmap() {
            return bitmap;
        }

        public void setBitmap(String bitmap) {
            this.bitmap = bitmap;
        }

        public String getVoucherNo() {
            return voucherNo;
        }

        public void setVoucherNo(String voucherNo) {
            this.voucherNo = voucherNo;
        }

        public String getOriRefNo() {
            return oriRefNo;
        }

        public void setOriRefNo(String oriRefNo) {
            this.oriRefNo = oriRefNo;
        }
    }
}
