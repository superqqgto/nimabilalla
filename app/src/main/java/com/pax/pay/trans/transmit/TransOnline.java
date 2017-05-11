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

import android.util.Log;

import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.gl.algo.IAlgo;
import com.pax.gl.packer.TlvException;
import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.ControllerSp;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.CardBinBlack;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.BaseTransData.*;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.RspCodeUtils;
import com.pax.pay.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import com.pax.dal.exceptions.PedDevException;


/**
 * 单独联机处理， 例如签到
 *
 * @author Steven.W
 */
public class TransOnline {
    /**
     * 检查应答码
     *
     * @param transData
     * @param listener
     * @return
     */
    public static final String TAG = TransOnline.class.getSimpleName();

    private static int checkRspCode(TransData transData, TransProcessListener listener) {
        if (!transData.getResponseCode().equals("00")) {
            listener.onHideProgress();
            listener.onShowErrMessageWithConfirm(ContextUtils.getString(R.string.prompt_err_code)
                            + transData.getResponseCode().toString(),
                    Constants.FAILED_DIALOG_SHOW_TIME);
            return TransResult.ERR_HOST_REJECT;
        }
        return TransResult.SUCC;
    }

    /**
     * 保存黑名单
     *
     * @param blackList
     * @throws TlvException
     */
    private static void writeBlack(byte[] blackList) {
        if (blackList == null)
            return;
        int loc = 0;
        while (loc < blackList.length) {
            int len = Integer.parseInt(new String(new byte[]{blackList[loc], blackList[loc + 1]}));
            byte[] cardNo = new byte[len];
            if (len + loc + 2 > blackList.length) {
                return;
            }
            System.arraycopy(blackList, loc + 2, cardNo, 0, len);
            CardBinBlack cardBinBlack = new CardBinBlack();
            cardBinBlack.setBin(new String(cardNo));
            cardBinBlack.setCardNoLen(cardNo.length);
            DbManager.getCardBinDao().insertBlack(cardBinBlack);
            loc += 2 + len;
        }
    }

    /**
     * 结算
     *
     * @param listener
     * @return
     */
    public static int settle(TransTotal total, TransProcessListener listener) {
        int ret;
        Log.e(TAG, "settle ");
        if (SpManager.getControlSp().getInt(ControllerSp.BATCH_UP_STATUS) != ControllerSp.Constant.BATCH_UP) {
            // 处理脱机交易
            ret = Transmit.getInstance().sendOfflineTrans(listener, true, true);
            if (ret != TransResult.SUCC) {
                return ret;
            }
            // 处理冲正
            ret = Transmit.getInstance().sendReversal(listener);
            if (ret == TransResult.ERR_ABORTED) {
                return ret;
            }

            ret = TransOnline.settleRequest(total, listener);
            if (ret != TransResult.SUCC) {
                listener.onHideProgress();
                return ret;
            }
        }

        ret = TransOnline.batchUp(listener);
        if (ret != TransResult.SUCC) {
            listener.onHideProgress();
            return ret;
        }

        return TransResult.SUCC;
    }

    /**
     * 结算请求
     *
     * @param listener
     * @return
     */
    private static int settleRequest(TransTotal total, TransProcessListener listener) {

        TransData transData = Component.transInit();
        transData.setTransType(ETransType.SETTLE);
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.SETTLE.getTransName());
        }

        String saleAmt;
        String saleNum;
        String refundAmt;
        String refundNum;

        String buf;
        saleAmt = Component.getPaddedNumber(total.getSaleTotalAmt(), 12);
        saleNum = Component.getPaddedNumber(total.getSaleTotalNum(), 3);
        refundAmt = Component.getPaddedNumber(total.getRefundTotalAmt(), 12);
        refundNum = Component.getPaddedNumber(total.getRefundTotalNum(), 3);
        buf = saleNum + saleAmt + refundNum + refundAmt;
        buf += "000000000000000000000000000000";
        transData.setField63(buf);

        int ret = Online.getInstance().online(transData, listener);
        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret != TransResult.SUCC) {
            return ret;
        }
        String responseCode = transData.getResponseCode();
        //AET-31
        if (!responseCode.equals("95")) {
            if (responseCode.equals("00")) {
                return TransResult.SUCC_NOREQ_BATCH;
            }
            Device.beepErr();
            if (listener != null) {
                listener.onShowErrMessageWithConfirm(RspCodeUtils.getMsgByCode(responseCode), Constants.FAILED_DIALOG_SHOW_TIME);
            }
            return TransResult.ERR_HOST_REJECT;
        }

        SpManager.getControlSp().putInt(ControllerSp.BATCH_UP_STATUS, ControllerSp.Constant.BATCH_UP);
        SpManager.getControlSp().putInt(ControllerSp.BATCH_NUM, 0);

        return TransResult.SUCC;
    }

    /**
     * 批上送
     *
     * @param listener
     * @return
     */
    private static int batchUp(TransProcessListener listener) {
        int ret;
        listener.onUpdateProgressTitle(ETransType.BATCH_UP.getTransName());
        // 获取交易记录条数
        long cnt = DbManager.getTransDao().countOf();
        if (cnt <= 0) {
            SpManager.getControlSp().putInt(ControllerSp.BATCH_UP_STATUS, ControllerSp.Constant.WORKED);
            return TransResult.ERR_NO_TRANS;
        }
        // 获取交易重复次数
        int resendTimes = Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.COMM_REDIAL_TIMES));
        int sendCnt = 0;
        final boolean[] left = new boolean[]{false};
        while (sendCnt < resendTimes + 1) {
            // 1)(对账平不送)全部磁条卡离线类交易，包括结算调整
            // 2)(对账平不送)基于PBOC标准的借/贷记IC卡脱机消费(含小额支付)成功交易
            // 3)(不存在)基于PBOC标准的电子钱包IC卡脱机消费成功交易 --- 不存在

            // 4)(对账平不送)全部磁条卡的请求类联机成功交易明细
            ret = allMagCardTransBatch(listener, new BatchUpListener() {

                @Override
                public void onLeftResult(boolean l) {
                    left[0] = l;
                }
            });
            if (ret != TransResult.SUCC) {
                return ret;
            }
            // 5)(对账平不送)磁条卡和基于PBOC借/贷记标准IC卡的通知类交易明细，包括退货和预授权完成(通知)交易
            // 6)(对账平也送)为了上送基于PBOC标准的借/贷记IC卡成功交易产生的TC值，所有成功的IC卡借贷记联机交易明细全部重新上送
            // 7)(对账平也送)为了让发卡方了解基于PBOC标准的借/贷记IC卡脱机消费(含小额支付)交易的全部情况，上送所有失败的脱机消费交易明细
            // 8)(对账平也送)为了让发卡方防范基于PBOC标准的借/贷记IC卡风险交易，上送所有ARPC错但卡片仍然承兑的IC卡借贷记联机交易明细
            // 9)(不存在)为了上送基于PBOC标准的电子钱包IC卡成功圈存交易产生的TAC值，上送所有圈存确认的交易明细
            if (left[0]) {
                left[0] = false;
                sendCnt++;
                continue;
            }
            break;
        }
        // 10)(对账平也送)最后需上送批上送结束报文
        ret = batchUpEnd(listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }
        return TransResult.SUCC;
    }

    /**
     * 结算结束
     *
     * @param listener
     * @return
     */
    private static int batchUpEnd(TransProcessListener listener) {
        listener.onUpdateProgressTitle(ETransType.SETTLE_END.getTransName());
        TransData transData = Component.transInit();
        String f60 = "00" + Component.getPaddedNumber(AcqManager.getInstance().getCurAcq().getCurrBatchNo(), 6);
        f60 += "207";

        int batchUpNum = SpManager.getControlSp().getInt(ControllerSp.BATCH_NUM);
        transData.setField48(Component.getPaddedNumber(batchUpNum, 4));
        transData.setField60(f60);
        transData.setTransType(ETransType.SETTLE_END);
        return Online.getInstance().online(transData, listener);
    }

    public interface BatchUpListener {
        void onLeftResult(boolean left);
    }

    /**
     * 全部磁条卡的请求类联机成功交易明细上送
     *
     * @param listener
     * @param batchUpListener
     * @return
     */
    private static int allMagCardTransBatch(TransProcessListener listener,
                                            BatchUpListener batchUpListener) {
        boolean left;
        int ret = TransResult.SUCC;
        int[] sendLoc = new int[8];
        List<TransData> allTrans = DbManager.getTransDao().findAllTransData(AcqManager.getInstance().getCurAcq());
        if (allTrans == null || allTrans.size() == 0) {
            return TransResult.ERR_NO_TRANS;
        }
        int transCnt = allTrans.size();
        int offSendCnt = 0;
        String f48 = "";
        int batchNum = SpManager.getControlSp().getInt(ControllerSp.BATCH_NUM);
        TransData transLog = null;

        for (int cnt = 0; cnt < transCnt; cnt++) {
            transLog = allTrans.get(cnt);
            ETransType transType = transLog.getTransType();
            transLog.setOrigTransType(transType);
            //AET-31、AET-43
            if (transType == ETransType.PREAUTH || transType == ETransType.VOID) {
                continue;
            }


//            if (transType == ETransType.REFUND) {
//                continue;
//            }

//            TransData.EnterMode enterMode = transLog.getEnterMode();
//            if (enterMode == TransData.EnterMode.INSERT || enterMode == TransData.EnterMode.CLSS) {
//                // 如果是IC卡的简化流程交易，如撤销，预授权完成请求，预授权完成请求撤销等，也是当作磁条卡交易进行上送的
//                if (transType != ETransType.VOID) {
//                    continue;
//                }
//            }

            // 已上送的交易不再上送
            if (transLog.isUpload()) {
                continue;
            }

            String field11 = Component.getPaddedNumber(transLog.getTraceNo(), 6);
            String pan = "0000000000" + transLog.getPan();
            pan = pan.substring(pan.length() - 20, pan.length());
            f48 += pan;
            String amt = "000000000000" + transLog.getAmount();
            amt = amt.substring(amt.length() - 12, amt.length());
            f48 += amt;
            offSendCnt++;
//            if (offSendCnt != 8) {
//                continue;
//            }
            TransData transData = Component.transInit();
            f48 = Component.getPaddedNumber(offSendCnt, 2) + f48;
            // field 2
            String field2 = transLog.getPan();
            if (field2 != null) {
                transData.setPan(field2);
            }
            // field 4
            transData.setAmount(transLog.getAmount());
//            transData.setTipAmount(transLog.getTipAmount());
            //field 11
            transData.setTraceNo(Long.parseLong(field11));
            //field 12
            String dateTime = transLog.getDateTime();
            if (dateTime != null) {
                transData.setDateTime(dateTime);
            }
            //field 13
            String date = transLog.getExpDate();
            if (date != null) {
                transData.setExpDate(date);
            }
            //field 22
            transData.setEnterMode(transLog.getEnterMode());
            //field 24
            String nii = transLog.getNii();
            if (nii != null) {
                transData.setNii(nii);
            }
            //field 37
            String refNo = transLog.getRefNo();
            if (refNo != null) {
                transData.setRefNo(refNo);
            }
            transData.setOrigTransType(transType);
            transData.setTransType(ETransType.BATCH_UP);

            ret = Online.getInstance().online(transData, listener);
            if (ret != TransResult.SUCC) {
                if (ret == TransResult.ERR_RECV) {
                    offSendCnt = 0;
                    f48 = "";
                    left = true; // 批上送交易无应答时，终端应在本轮上送完毕后再重发，而非立即重发
                    batchUpListener.onLeftResult(left);
                    f48 = "";
                    continue;
                }
                return ret;
            }
        }
        return ret;
    }

    /**
     * 脱机交易上送
     *
     * @param listener isOnline 是否为下一笔联机交易
     * @return
     */
    public static int offlineTransSend(TransProcessListener listener, boolean isSendAllOfflineTrans, boolean isSettlement) {
        int ret = 0;
        int sendMaxTime = Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.OFFLINETC_UPLOADTIMES));
        int maxOfflineNum = Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.OFFLINETC_UPLOADNUM));

        final List<TransData.OfflineStatus> defFilter = new ArrayList<TransData.OfflineStatus>() {{
            add(TransData.OfflineStatus.OFFLINE_NOT_SENT);
        }};

        //合并了代码，请重新焊接  by huangmuhua
        //Modified by Daisy.zhou 2017-05-02
        List<TransData.ETransStatus> filter = new ArrayList<>();
        filter.add(TransData.ETransStatus.VOIDED);
        filter.add(TransData.ETransStatus.ADJUSTED);

        List<TransData> records = DbManager.getTransDao().findOfflineTransData(defFilter);
        List<TransData> notSendRecords = new ArrayList<>();
        if (records == null || records.size() == 0) {
            return TransResult.SUCC;
        }

        Acquirer acquirer = AcqManager.getInstance().getCurAcq();
        if (acquirer == null || (!isSettlement && acquirer.isDisableTrickFeed())) {
            return TransResult.SUCC;
        }

        if (!isSettlement) {
            notSendRecords.add(records.get(0));
        } else {
            notSendRecords.addAll(records);
        }

        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.OFFLINE_TRANS_SEND.getTransName());
        }

        // 累计达到设置中“满足自动上送的累计笔数”，终端应主动拨号上送当前所有的离线类交易和IC卡脱机交易
        if (!isSendAllOfflineTrans) {
            if (notSendRecords.size() < maxOfflineNum) {
                return TransResult.SUCC;
            }
        }

        // 离线交易上送
        ret = OfflineTransProc(sendMaxTime, notSendRecords, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }
        // IC卡脱机交易上送

        return TransResult.SUCC;
    }

    /****************************************************************************
     * OfflineTransProc 离线交易上送处理
     ****************************************************************************/
    private static int OfflineTransProc(int sendMaxtime, List<TransData> records, TransProcessListener listener) {
        int dup_num = 0;// 重发次数
        int ret = 0;
        boolean isLastTime = false;
        while (dup_num < sendMaxtime + 1) {
            int sendCount = 0;
            if (dup_num == sendMaxtime) {
                isLastTime = true;
            }
            for (int cnt = 0; cnt < records.size(); cnt++) { // 逐笔上送
                TransData record = records.get(cnt);

                // 跳过上送不成功的和应答码非"00"的交易
                if (!record.getOfflineSendState().equals(OfflineStatus.OFFLINE_NOT_SENT)) {
                    continue;
                }
                sendCount++;
                if (listener != null) {
                    listener.onUpdateProgressTitle(ETransType.OFFLINE_TRANS_SEND.getTransName() + "[" + sendCount + "]");
                }
                TransData transData = (TransData) record.clone();
                Component.transInit(transData);
                transData.setTraceNo(record.getTraceNo());
                ret = Online.getInstance().online(transData, listener);
                if (ret != TransResult.SUCC) {
                    if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_SEND || ret == TransResult.ERR_PACK
                            || ret == TransResult.ERR_MAC) {
                        // 如果是发送数据时发生错误(连接错、发送错、数据包错、接收失败、MAC错)，则直接退出，不进行重发
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(
                                    TransResult.getMessage(ret),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        return TransResult.ERR_ABORTED;
                    } else if (ret == TransResult.ERR_RECV) { // BCTC要求离线交易上送时，如果平台无应答要离线交易上送次数上送
                        if (!isLastTime) { // 未达到上送次数，继续送， 如果已达到上送次数，但接收失败按失败处理，不再上送
                            continue;
                        }
                    }
                    record.setOfflineSendState(OfflineStatus.OFFLINE_ERR_SEND);
                    DbManager.getTransDao().updateTransData(record);
                } else {
                    String responseCode = transData.getResponseCode();
                    // 返回码失败处理
                    if (responseCode.equals("A0")) {
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(RspCodeUtils.getMsgByCode(responseCode),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        return TransResult.ERR_ABORTED;
                    }
                    if (!responseCode.equals("00") && !responseCode.equals("94")) { //AET-28
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(RspCodeUtils.getMsgByCode(responseCode),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        record.setOfflineSendState(OfflineStatus.OFFLINE_ERR_RESP);
                        DbManager.getTransDao().updateTransData(record);
                        continue;
                    }

                    record.setSettleDateTime(transData.getSettleDateTime() != null ? transData.getSettleDateTime() : "");
                    record.setAuthCode(transData.getAuthCode() != null ? transData.getAuthCode() : "");
                    record.setRefNo(transData.getRefNo());

                    record.setAcqCode(transData.getAcqCode() != null ? transData.getAcqCode() : "");
                    record.setIssuerCode(transData.getIssuerCode() != null ? transData.getIssuerCode() : "");

                    record.setReserved(transData.getReserved() != null ? transData.getReserved() : "");

                    record.setAuthCode(transData.getAuthCode());
                    record.setOfflineSendState(OfflineStatus.OFFLINE_SENT);
                    DbManager.getTransDao().updateTransData(record);
                }
            }
            dup_num++;
        }
        if (listener != null)
            listener.onHideProgress();
        return TransResult.SUCC;
    }

    /**
     * 回响功能
     *
     * @param listener
     * @return
     */
    public static int echo(TransProcessListener listener) {
        TransData transData = Component.transInit();
        int ret;
        transData.setTransType(ETransType.ECHO);
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.ECHO.getTransName());
        }
        ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }
        ret = checkRspCode(transData, listener);
        return ret;
    }

    /**
     * 下载TMK
     *
     * @param listener
     * @return
     */
    public static IAlgo iAlgo = GlManager.getAlgo();
    public static byte[] masterKey = {0x12,0x13,0x14,0x15,0x00,0x00,0x00,0x00,0x12,0x13,0x14,0x15,0x00,0x00,0x00,0x00};
    public static int downloadTmk(TransProcessListener listener) {

        byte[] decryptTmk1 = null;
        byte[] encryptTmk1 = null;
        byte[] decryptTmk2 = null;
        byte[] encryptTmk2 = null;

        //TMK下载阶段一 从后台获取加密后的TMK1
        TransData transData = Component.transInit();
        int ret;
        transData.setTransType(ETransType.TMK_DOWNLOAD);
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.TMK_DOWNLOAD.getTransName());
        }
        ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }
        // 平台拒绝
        if (!transData.getResponseCode().equals("00")) {
            return  TransResult.ERR_REAPONSE;
        }
        //获得加密的TMK1秘钥
        decryptTmk1 = transData.getSessionMaintData().getTMK().getBytes();
        if (decryptTmk1 == null || decryptTmk1.length == 0) {
            return TransResult.ERR_PARAM;
        }
        encryptTmk1 = iAlgo.des(IAlgo.ECryptOperation.DECRYPT, IAlgo.ECryptOption.ECB, IAlgo.ECryptPaddingOption.NO_PADDING,
                decryptTmk1, masterKey, null);

        //TMK下载阶二 从后台获取加密后的TMK2
        transData = Component.transInit();
        transData.setTransType(ETransType.TMK_DOWNLOAD);
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.TMK_DOWNLOAD.getTransName());
        }
        ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }
        // 平台拒绝
        if (!transData.getResponseCode().equals("00")) {
            return  TransResult.ERR_REAPONSE;
        }
        //获得加密的TMK2秘钥
        decryptTmk2 = transData.getSessionMaintData().getTMK().getBytes();
        if (decryptTmk1 == null || decryptTmk1.length == 0) {
            return TransResult.ERR_PARAM;
        }
        encryptTmk2 = iAlgo.des(IAlgo.ECryptOperation.DECRYPT, IAlgo.ECryptOption.ECB, IAlgo.ECryptPaddingOption.NO_PADDING,
                decryptTmk2, encryptTmk1, null);
        SpManager.getSysParamSp().set(SysParamSp.FINAL_TMK, GlManager.getConvert().bcdToStr(encryptTmk2));
        //将最终TMK注入PED中
        int tmkIndex = Utils.getMainKeyIndex(Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.MK_INDEX)));
        boolean result = Device.writeTMK(tmkIndex,encryptTmk2);
        if(!result) {
            return TransResult.ERR_TMK_TO_PED;
        }

        ret = checkRspCode(transData, listener);
        return ret;
    }


    public static int posLogon(TransProcessListener listener) {
        TransData transData = Component.transInit(); //交易初始化：设置一些交易参数
        int ret;
        transData.setTransType(ETransType.LOGON);
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.LOGON.getTransName());
        }
        ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }
        // 平台拒绝
        if (!transData.getResponseCode().equals("00")) {

            return  TransResult.ERR_REAPONSE;
        }
        String finalTmk = SpManager.getSysParamSp().get(SysParamSp.FINAL_TMK);
        byte[] decryptPinKey = (transData.getSessionMaintData().getDoubleLenTPK1() + transData.getSessionMaintData().getDoubleLenTPK2()).getBytes();
        if (decryptPinKey == null || decryptPinKey.length == 0) {
            return TransResult.ERR_PARAM;
        }
        byte[] decryptTleKey = transData.getSessionMaintData().getTLE().getBytes();
        if (decryptTleKey == null || decryptTleKey.length == 0) {
            return TransResult.ERR_PARAM;
        }
        byte[] encryptPinKey = iAlgo.des(IAlgo.ECryptOperation.ENCRYPT, IAlgo.ECryptOption.ECB, IAlgo.ECryptPaddingOption.NO_PADDING,
                decryptPinKey, GlManager.strToBcdPaddingLeft(finalTmk), null);
        if (encryptPinKey == null || encryptPinKey.length == 0) {
            return TransResult.ERR_PARAM;
        }
        byte[] encryptTleKey = iAlgo.des(IAlgo.ECryptOperation.ENCRYPT, IAlgo.ECryptOption.ECB, IAlgo.ECryptPaddingOption.NO_PADDING,
                decryptTleKey, GlManager.strToBcdPaddingLeft(finalTmk), null);
        if (encryptTleKey == null || encryptTleKey.length == 0) {
            return TransResult.ERR_PARAM;
        }
        //将最终TPK 、 TDK注入PED中
        try {
            Device.writeTPK(encryptPinKey, null);
            Device.writeTDK(encryptTleKey, null);
        } catch (PedDevException e) {
            e.printStackTrace();
            Device.beepErr();
        }

        return ret;

    }
}
