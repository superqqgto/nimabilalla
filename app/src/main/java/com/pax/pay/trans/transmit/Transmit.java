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
package com.pax.pay.trans.transmit;

import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.RspCodeUtils;

public class Transmit {
    private static Transmit transmit;

    private Transmit() {

    }

    public static Transmit getInstance() {
        if (transmit == null) {
            transmit = new Transmit();
        }

        return transmit;
    }

    public int transmit(TransData transData, TransProcessListener listener) {
        int ret = 0;
        ETransType transType = transData.getTransType();

        // 脱机交易在联机之后上送
        // sendOfflineTrans(listener, true);

        // 处理冲正
        if (transType.isDupSendAllowed()) {
            ret = sendReversal(listener);
            //冲正交易失败直接退出，不进行后续交易
            if(ret != TransResult.SUCC){
                return ret;
            }
        }

        if (listener != null) {
            listener.onUpdateProgressTitle(transType.getTransName());
        }

        int i = 0;
        // 只有平台返回密码错时， 才会下次循环
        for (i = 0; i < 3; i++) {
            if (i != 0) {
                // 输入密码
                if (listener != null) {
                    ret = listener.onInputOnlinePin(transData);
                    if (ret != 0) {
                        return TransResult.ERR_ABORTED;
                    }
                } else {
                    return TransResult.ERR_HOST_REJECT;
                }
                transData.setTraceNo(Long.parseLong(SpManager.getSysParamSp().get(SysParamSp.TRANS_NO)));
            }
            if (listener != null) {
                listener.onUpdateProgressTitle(transType.getTransName());
            }

            ret = Online.getInstance().online(transData, listener);
            if (ret == TransResult.SUCC) {
                String retCode = transData.getResponseCode();

                if (retCode.equals("00")) {
                    // write transaction record
                    transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
                    transData.setDupReason("");
                    DbManager.getTransDao().updateTransData(transData);
                    return TransResult.SUCC;
                } else {
                    DbManager.getTransDao().deleteDupRecord();
                    if (retCode.equals("55")) {
                        if (listener != null) {
                            Device.beepErr();
                            listener.onShowErrMessageWithConfirm(
                                    ContextUtils.getString(R.string.err_password_reenter),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        continue;
                    }
                    if (listener != null) {
                        Device.beepErr();

                        listener.onShowErrMessageWithConfirm(
                                ContextUtils.getString(R.string.prompt_err_code)
                                        + retCode + "\n" + RspCodeUtils.getMsgByCode(retCode),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                    }
                    return TransResult.ERR_HOST_REJECT;
                }
            }

            break;
        }
        if (i == 3) {
            return TransResult.ERR_ABORTED;
        }

        return ret;
    }

    /**
     * 冲正处理
     *
     * @return
     */
    public int sendReversal(TransProcessListener listener) {
        TransData dupTransData = DbManager.getTransDao().findFirstDupRecord();
        if (dupTransData == null) {
            return TransResult.SUCC;
        }
        int ret = 0;
        long transNo = dupTransData.getTraceNo();
        String dupReason = dupTransData.getDupReason();

        ETransType transType = dupTransData.getTransType();
        if (transType == ETransType.VOID) {
            dupTransData.setOrigAuthCode(dupTransData.getOrigAuthCode());
        } else {
            dupTransData.setOrigAuthCode(dupTransData.getAuthCode());
        }
        //dupTransData.setReversalStatus(TransData.ReversalStatus.REVERSAL);
        Component.transInit(dupTransData);

        dupTransData.setTraceNo(transNo);
        dupTransData.setDupReason(dupReason);

        int retry = Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.REVERSAL_CTRL));
        if (listener != null) {
            listener.onUpdateProgressTitle(ContextUtils.getString(R.string.prompt_reverse));
        }

        for (int i = 0; i < retry; i++) {
            //AET-126
            dupTransData.setReversalStatus(TransData.ReversalStatus.REVERSAL);
            ret = Online.getInstance().online(dupTransData, listener);
            if (ret == TransResult.SUCC) {
                String retCode = dupTransData.getResponseCode();
                // 冲正收到响应码12或者25的响应码，应默认为冲正成功
                if (retCode.equals("00") || retCode.equals("12") || retCode.equals("25")) {
                    DbManager.getTransDao().deleteDupRecord();
                    return TransResult.SUCC;
                }
                dupTransData.setReversalStatus(TransData.ReversalStatus.PENDING);
                dupTransData.setDupReason(TransData.DUP_REASON_OTHERS);
                DbManager.getTransDao().updateTransData(dupTransData);
                continue;
            }
            if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_PACK || ret == TransResult.ERR_SEND) {
                if (listener != null) {
                    listener.onShowErrMessageWithConfirm(
                            TransResult.getMessage(ret),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                }
                return TransResult.ERR_ABORTED;
            }
            if (ret == TransResult.ERR_RECV) {
                dupTransData.setReversalStatus(TransData.ReversalStatus.PENDING);
                dupTransData.setDupReason(TransData.DUP_REASON_NO_RECV);
                DbManager.getTransDao().updateTransData(dupTransData);

            }
        }
        if (listener != null) {
            listener.onShowErrMessageWithConfirm(ContextUtils.getString(R.string.err_reverse),
                    Constants.FAILED_DIALOG_SHOW_TIME);
        }
        //冲正失败不删除冲正交易
       //DbManager.getTransDao().deleteDupRecord();
        return ret;
    }

    /**
     * 脱机交易上送
     *
     * @param isOnline 是否为在下笔联机交易到来之前上送
     * @return
     */
    public int sendOfflineTrans(TransProcessListener listener, boolean isOnline, boolean isSettlement) {
        int ret = TransOnline.offlineTransSend(listener, isOnline, isSettlement);
        if (ret == TransResult.ERR_ABORTED) {
            return ret;
        }
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                Device.beepErr();
                listener.onShowErrMessageWithConfirm(
                        TransResult.getMessage(ret),
                        Constants.FAILED_DIALOG_SHOW_TIME);
            }
        }
        return ret;
    }

}
