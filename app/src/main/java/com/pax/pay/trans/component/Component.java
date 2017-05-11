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
package com.pax.pay.trans.component;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;

import com.pax.abl.utils.TrackUtils;
import com.pax.dal.entity.EPedDesMode;
import com.pax.dal.exceptions.PedDevException;
import com.pax.dal.exceptions.PiccDevException;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.enums.ETransResult;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.DalManager;
import com.pax.manager.neptune.EmvManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.ControllerSp;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.base.Acquirer;
import com.pax.pay.clss.CTransResult;
import com.pax.pay.clss.ClssTlvTag;
import com.pax.pay.clss.ClssTransProcess;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.emv.EmvTransProcess;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.BaseTransData.ETransStatus;
import com.pax.pay.trans.model.BaseTransData.EnterMode;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.view.dialog.CustomAlertDialog;

import java.io.File;

public class Component {

    /**
     * 交易预处理，检查是否签到， 是否需要结束， 是否继续批上送， 是否支持该交易， 是否需要参数下载
     *
     * @param context
     * @param transType
     * @return
     */
    public static int transPreDeal(final Context context, ETransType transType) {
        if (!IsNeedPreDeal(transType)) {
            return TransResult.SUCC;
        }
        // TODO 检测电量状态，暂不处理，后续再确定需不需要


        // 判断是否需要结算
        int ret = checkSettle();
        if (ret != TransResult.SUCC) {
            return ret;
        }
        // 批上送断点
        if (isNeedBatchUp()) {
            return TransResult.ERR_BATCH_UP_NOT_COMPLETED;
        }
        // 根据交易类型判断是否支持此交易
        if (!isSupportTran(transType)) {
            return TransResult.ERR_NOT_SUPPORT_TRANS;
        }
        // 判断是否有参数下载

        // EMV内核初始化
        // 配置终端相关参数

        // EMV初始化， 交易开始前设置emv配置，capk和aid，减少后面emv处理时间
        EmvTransProcess.getInstance().init();

        return ret;
    }

    /**
     * 根据交易类型、冲正标识确认当前交易是否预处理
     *
     * @param transType
     * @return true:需要预处理 false:不需要预处理 备注：签到，签退，结算，参数下发，公钥下载，冲正类不需要预处理,新增交易类型时，需修改添加交易类型判断
     */
    private static boolean IsNeedPreDeal(ETransType transType) {
        return transType != ETransType.SETTLE;
    }

    /**
     * 检查是否达结算要求
     *
     * @return 0：不用结算 1：结算提醒,立即 2：结算提醒，稍后 3：结算提醒,空间不足
     */
    private static int checkSettle() {
        // 获取交易笔数
        long cnt = DbManager.getTransDao().countOf();
        // 获取允许的最大交易笔数
        long maxCnt = Long.MAX_VALUE;
        String temp = SpManager.getSysParamSp().get(SysParamSp.MAX_TRANS_COUNT);
        if (temp != null) {
            maxCnt = Long.parseLong(temp);
        }
        // 判断交易笔数是否超限
        if (cnt >= maxCnt) {
            if (cnt >= maxCnt + 10) {
                return TransResult.ERR_NEED_SETTLE_NOW; // 结算提醒,立即
            } else {
                return TransResult.ERR_NEED_SETTLE_LATER; // 结算提醒,稍后
            }
        }
        // 判断存储空间大小
        if (!hasFreeSpace()) {
            return TransResult.ERR_NO_FREE_SPACE; // 存储空间不足,需要结算
        }
        return TransResult.SUCC; // 不用结算
    }

    /**
     * 判断是否有剩余空间
     *
     * @return true: 有空间 false：无空间
     */
    @SuppressWarnings("deprecation")
    private static boolean hasFreeSpace() {
        File dataPath = Environment.getDataDirectory();
        StatFs dataFs = new StatFs(dataPath.getPath());
        long sizes = (long) dataFs.getFreeBlocks() * (long) dataFs.getBlockSize();
        long available = sizes / ((1024 * 1024));
        return available > 1;
    }

    private static boolean isNeedBatchUp() {
        return SpManager.getControlSp().getInt(ControllerSp.BATCH_UP_STATUS) == ControllerSp.Constant.BATCH_UP;
    }

    /**
     * 判断是否支持该交易
     *
     * @param transType
     * @return
     */
    private static boolean isSupportTran(ETransType transType) {
        switch (transType) {
            case SALE:
                return SpManager.getSysParamSp().get(SysParamSp.TTS_SALE).equals(SysParamSp.Constant.YES);
            case VOID:
                return SpManager.getSysParamSp().get(SysParamSp.TTS_VOID).equals(SysParamSp.Constant.YES);
            case REFUND:
                return SpManager.getSysParamSp().get(SysParamSp.TTS_REFUND).equals(SysParamSp.Constant.YES);
            case PREAUTH:
                return SpManager.getSysParamSp().get(SysParamSp.TTS_PREAUTH).equals(SysParamSp.Constant.YES);
            default:
                break;
        }

        return true;
    }

    /**
     * CLSS结果处理
     *
     * @param result
     * @param transData
     */
    public static void clssTransResultProcess(CTransResult result, TransData transData) {
        //FIXME
        //PanSeqNo
        ByteArray list = new ByteArray();
        ClssTransProcess.getInstance().getClssTlv(ClssTlvTag.PanSeqNoTag, list, ClssTransProcess.getInstance().getKernelType());
        String cardSerialNo = GlManager.bcdToStr(list.data);
        transData.setCardSerialNo(cardSerialNo.substring(0, list.length * 2));

        //AppLabel
        ClssTransProcess.getInstance().getClssTlv(ClssTlvTag.AppLabelTag, list, ClssTransProcess.getInstance().getKernelType());
        String appLabel = GlManager.bcdToStr(list.data);
        transData.setEmvAppLabel(appLabel.substring(0, list.length * 2));

        //TVR
        ClssTransProcess.getInstance().getClssTlv(ClssTlvTag.TvrTag, list, ClssTransProcess.getInstance().getKernelType());
        String tvr = GlManager.bcdToStr(list.data);
        transData.setTvr(tvr.substring(0, list.length * 2));

        //TSI
        ClssTransProcess.getInstance().getClssTlv(ClssTlvTag.TsiTag, list, ClssTransProcess.getInstance().getKernelType());
        String tsi = GlManager.bcdToStr(list.data);
        transData.setTsi(tsi.substring(0, list.length * 2));

        //ATC
        ClssTransProcess.getInstance().getClssTlv(ClssTlvTag.AtcTag, list, ClssTransProcess.getInstance().getKernelType());
        String atc = GlManager.bcdToStr(list.data);
        transData.setAtc(atc.substring(0, list.length * 2));

        //AppCrypto
        ClssTransProcess.getInstance().getClssTlv(ClssTlvTag.AppCryptoTag, list, ClssTransProcess.getInstance().getKernelType());
        String arqc = GlManager.bcdToStr(list.data);
        transData.setArqc(arqc.substring(0, list.length * 2));

        //AppName
        ClssTransProcess.getInstance().getClssTlv(ClssTlvTag.AppNameTag, list, ClssTransProcess.getInstance().getKernelType());
        String appName = GlManager.bcdToStr(list.data);
        transData.setEmvAppName(appName.substring(0, list.length * 2));

        // AID
        ClssTransProcess.getInstance().getClssTlv(ClssTlvTag.CapkRidTag, list, ClssTransProcess.getInstance().getKernelType());
        String aid = GlManager.bcdToStr(list.data);
        transData.setAid(aid.substring(0, list.length * 2));
    }

    /**
     * EMV结果处理
     *
     * @param result
     * @param transData
     */
    public static void emvTransResultProcess(ETransResult result, TransData transData) {
        ETransType transType = transData.getTransType();

        // 保存emv TSI值
        saveTvrTsi(transData);
        // 脚本结果处理
        checkScriptResult(transData);
        if (result == ETransResult.OFFLINE_APPROVED) {
            // 脱机处理
            // FIXME 显示余额
            EmvManager.setTlv(0x8a, "Y1".getBytes());
            // 设置交易结果
            transData.setEmvResult((byte) result.ordinal());
            // 取55域数据到交易结构
            byte[] f55 = EmvTags.getF55(transData.getTransType(), false);
            transData.setSendIccData(GlManager.bcdToStr(f55));
            // 流水号+1
            incTransNo();

        } else if (result == ETransResult.ONLINE_APPROVED) {
            // 联机批准
            // 读55域数据
            byte[] f55 = EmvTags.getF55(transType, false);
            transData.setSendIccData(GlManager.bcdToStr(f55));
            // 设置交易结果
            transData.setEmvResult((byte) result.ordinal());
        } else if (result == ETransResult.ARQC || result == ETransResult.SIMPLE_FLOW_END) {
            try {
                saveCardInfoAndCardSeq(transData);
                transData.setEmvResult((byte) result.ordinal());

                if (result == ETransResult.ARQC) {
                    generateF55AfterARQC(transData);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    DalManager.getPiccInternal().close();
                } catch (PiccDevException e) {
                    e.printStackTrace();
                }
            }
        } else if (result == ETransResult.ONLINE_DENIED) {
            // 联机拒绝

        } else if (result == ETransResult.OFFLINE_DENIED) {
            EmvManager.setTlv(0x8a, "Z1".getBytes());
            // fixme 准备55域数据
            // 流水号增加
            incTransNo();
            // 设置交易结果
            transData.setEmvResult((byte) result.ordinal());
        } else if (result == ETransResult.ONLINE_CARD_DENIED) {
            // 平台批准卡片拒绝
            byte[] f55 = EmvTags.getF55forPosAccpDup();
            if (f55 != null && f55.length > 0) {
                TransData dupTransData = DbManager.getTransDao().findFirstDupRecord();
                if (dupTransData != null) {
                    dupTransData.setDupIccData(GlManager.bcdToStr(f55));
                    DbManager.getTransDao().updateTransData(dupTransData);
                }

            }
        }
    }

    /**
     * ARQC时， 读取55域（交易/冲正）
     *
     * @param transData
     */
    public static void generateF55AfterARQC(TransData transData) {
        ETransType transType = transData.getTransType();

        byte[] f55 = EmvTags.getF55(transType, false);
        transData.setSendIccData(GlManager.bcdToStr(f55));

        byte[] arqc = EmvManager.getTlv(0x9f26);
        if (arqc != null && arqc.length > 0) {
            transData.setArqc(GlManager.bcdToStr(arqc));
        }

        byte[] f55Dup = EmvTags.getF55(transType, true);
        if (f55Dup != null && f55Dup.length > 0) {
            transData.setDupIccData(GlManager.bcdToStr(f55Dup));
        }
    }

    /**
     * 保存磁道信息， 卡号， 有效期， 卡片序列号
     *
     * @param transData
     */
    public static void saveCardInfoAndCardSeq(TransData transData) {

        byte[] track2 = EmvManager.getTlv(0x57);
        String strTrack2 = GlManager.bcdToStr(track2);
        strTrack2 = strTrack2.split("F")[0];
        transData.setTrack2(strTrack2);
        // 卡号
        String pan = TrackUtils.getPan(strTrack2);
        transData.setPan(pan);
        // 有效期
        byte[] expDate = EmvManager.getTlv(0x5f24);
        if (expDate != null && expDate.length > 0) {
            String temp = GlManager.bcdToStr(expDate);
            transData.setExpDate(temp.substring(0, 4));
        }
        // 获取卡片序列号
        byte[] cardSeq = EmvManager.getTlv(0x5f34);
        if (cardSeq != null && cardSeq.length > 0) {
            String temp = GlManager.bcdToStr(cardSeq);
            transData.setCardSerialNo(temp.substring(0, 2));
        }

    }

    /**
     * 纯电子现金卡做联机交易拒绝
     */
    public static int pureEcOnlineReject(TransData transData) {

        byte[] aid = EmvManager.getTlv(0x4f);

        if (aid != null && aid.length > 0) {
            boolean ret = GlManager.getUtils().isByteArrayValueSame(aid, 0,
                    new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x33, 0x01, 0x01, 0x06}, 0, 8);
            if (ret) {
                return -1;
            }
        }

        return 0;
    }

    private static void saveTvrTsi(TransData transData) {

        // TVR
        byte[] tvr = EmvManager.getTlv(0x95);
        if (tvr != null && tvr.length > 0) {
            transData.setTvr(GlManager.bcdToStr(tvr));
        }
        // ATC
        byte[] atc = EmvManager.getTlv(0x9f36);
        if (atc != null && atc.length > 0) {
            transData.setAtc(GlManager.bcdToStr(atc));
        }
        //
        // TSI
        byte[] tsi = EmvManager.getTlv(0x9b);
        if (tsi != null && tsi.length > 0) {
            transData.setTsi(GlManager.bcdToStr(tsi));
        }
        // TC
        byte[] tc = EmvManager.getTlv(0x9f26);
        if (tc != null && tc.length > 0) {
            transData.setTc(GlManager.bcdToStr(tc));
        }

        // AppLabel
        byte[] AppLabel = EmvManager.getTlv(0x50);
        if (AppLabel != null && AppLabel.length > 0) {
            transData.setEmvAppLabel(new String(AppLabel));
        }
        // AppName
        byte[] AppName = EmvManager.getTlv(0x9f12);
        if (AppName != null && AppName.length > 0) {
            transData.setEmvAppName(new String(AppName));
        }
        // AID
        byte[] aid = EmvManager.getTlv(0x4f);
        if (aid != null && aid.length > 0) {
            transData.setAid(GlManager.bcdToStr(aid));
        }
    }

    /**
     * 检查脚本结果，并保存
     *
     * @param transData
     */
    private static void checkScriptResult(TransData transData) {

        byte[] issuScript71 = EmvManager.getTlv(0x71);
        byte[] issuScript72 = EmvManager.getTlv(0x72);
        if ((issuScript71 != null && issuScript71.length > 0) || (issuScript72 != null && issuScript72.length > 0)) {
            // 保存脚本
        }

    }

    private static final long MAX_TRANS_NO = 999999;
    private static final long MAX_BATCH_NO = 999999;

    /**
     * 流水号+1
     */
    public static void incTransNo() {
        long transNo = Long.parseLong(SpManager.getSysParamSp().get(SysParamSp.TRANS_NO));
        if (transNo >= MAX_TRANS_NO) {
            transNo = 0;
        }
        transNo++;
        SpManager.getSysParamSp().set(SysParamSp.TRANS_NO, String.valueOf(transNo));
    }

    /**
     * 批次号+1
     */
    public static void incBatchNo() {
        int batchNo = AcqManager.getInstance().getCurAcq().getCurrBatchNo();
        if (batchNo >= MAX_BATCH_NO) {
            batchNo = 0;
        }
        batchNo++;

        AcqManager.getInstance().getCurAcq().setCurrBatchNo(batchNo);
        DbManager.getAcqDao().updateAcquirer(AcqManager.getInstance().getCurAcq());
    }

    public static String getPaddedNumber(long num, int digit) {
        return String.format("%0" + digit + "d", num);
    }

    /**
     * 生成凭单水印
     */
    public static String genFeatureCode(TransData transData) {
        ETransType transType = transData.getTransType();
        String data1;
        String data2;
        if (transType == ETransType.SALE && transData.getEmvResult() == ETransResult.OFFLINE_APPROVED.ordinal()) {
            data1 = getPaddedNumber(transData.getBatchNo(), 6);
            String temp = getPaddedNumber(transData.getTraceNo(), 6);
            data1 += temp.substring(0, 2);
            data2 = temp.substring(2) + "0000";
        } else {
            data1 = transData.getSettleDateTime();
            if (data1 == null || data1.length() == 0) {
                data1 = "0000";
            } else {
                data1 = data1.substring(4, 8);
            }
            data2 = transData.getRefNo();
            if (data2 == null || data2.length() == 0) {
                data2 = "000000000000";
            }

            data1 += data2.substring(0, 4);
            data2 = data2.substring(4);
        }

        byte[] xorData = new byte[4];
        byte[] bData1 = GlManager.strToBcdPaddingLeft(data1);
        byte[] bData2 = GlManager.strToBcdPaddingLeft(data2);
        for (int i = 0; i < 4; i++) {
            xorData[i] = (byte) (bData1[i] ^ bData2[i]);
        }

        return GlManager.bcdToStr(xorData);
    }

    /**
     * 交易初始化
     *
     * @return
     */
    public static TransData transInit() {
        TransData transData = new TransData();
        transInit(transData);
        return transData;
    }

    /**
     * 交易初始化
     *
     * @param transData
     */
    public static void transInit(TransData transData) {
        Acquirer acquirer = AcqManager.getInstance().getCurAcq();

        transData.setTraceNo(getTransNo());
        transData.setBatchNo(acquirer.getCurrBatchNo());
        transData.setDateTime(Device.getTime(Constants.TIME_PATTERN_TRANS));
        transData.setHeader("");
        transData.setTpdu("600" + acquirer.getNii() + "0000");
        // 冲正原因
        transData.setDupReason("06");
        transData.setTransState(ETransStatus.NORMAL);
        transData.setAcquirer(AcqManager.getInstance().getCurAcq());
        transData.setCurrency(CurrencyConverter.getDefCurrency());
    }

    // 获取流水号
    private static long getTransNo() {
        long transNo = Long.parseLong(SpManager.getSysParamSp().get(SysParamSp.TRANS_NO));
        if (transNo == 0) {
            transNo += 1;
            SpManager.getSysParamSp().set(SysParamSp.TRANS_NO, String.valueOf(transNo));
        }
        return transNo;
    }

    private static final byte ONLINEPIN_CVM = (byte) 0x80;
    private static final byte SIGNATURE_CMV = 0x40;
    private static final byte CD_CVM = (byte) 0x80;
    private static final byte NO_CVM = 0x00;

    /**
     * @return:true-免密 false-未知
     */
    private static boolean clssCDCVMProcss() {
        if (SpManager.getSysParamSp().get(SysParamSp.QUICK_PASS_TRANS_CDCVM_FLAG).equals(SysParamSp.Constant.YES)) {

            byte[] value = EmvManager.getTlv(0x9f6c);
            if (null == value) {
                return false;
            }
            if ((value[1] & CD_CVM) == CD_CVM && (value[0] & ONLINEPIN_CVM) != ONLINEPIN_CVM) {
                return true;
            }

        }

        return false;
    }

    /**
     * Qpboc判定卡片是否需要输入联机pin, 只有外卡才需要通过9f6c来判断, 内卡默认都要输pin
     *
     * @return true：需要， false：不需要
     */
    public static boolean isQpbocNeedOnlinePin() {
        if (!isCupOutSide()) {
            return true;
        }

        byte[] value = EmvManager.getTlv(0x9f6c);
        return (value[0] & ONLINEPIN_CVM) == ONLINEPIN_CVM;

    }

    /**
     * 判断是否是银联外卡
     */
    private static boolean isCupOutSide() {
        int[] tags = new int[]{0x9F51, 0xDF71}; // tag9F51：第一货币 tagDF71：第二货币
        int flag = 0;
        byte[] val = null;
        for (int tag : tags) {
            val = EmvManager.getTlv(tag);
            if (val == null) {
                continue;
            }
            flag = 1; // 能获取到货币代码值
            if ("0156".equals(GlManager.bcdToStr(val))) {
                return false;
            }
        }

        return !(val == null && flag == 0);
    }

    /**
     * 根据AID判断是否是贷记卡或准贷记卡
     *
     * @param aid
     * @return true: 贷记卡或准贷记卡 false: 其他
     */
    private static boolean isCredit(String aid) {
        final String UNIONPAY_DEBITAID = "A000000333010101";
        final String UNIONPAY_CREDITAID = "A000000333010102";
        final String UNIONPAY_QUASICREDITAID = "A000000333010103";

        if (UNIONPAY_DEBITAID.equals(aid)) { // 借记卡
            return false;
        } else if (UNIONPAY_CREDITAID.equals(aid)) { // 贷记卡
            return true;
        } else // 准贷记卡
            return UNIONPAY_QUASICREDITAID.equals(aid);
    }

    /**
     * @param transData
     * @return true-免密 false-未知
     */
    public static boolean clssQPSProcess(TransData transData) {

        if (SpManager.getSysParamSp().get(SysParamSp.QUICK_PASS_TRANS_PIN_FREE_SWITCH).equals(SysParamSp.Constant.NO)) {
            return false;
        }
        EnterMode enterMode = transData.getEnterMode();
        if (enterMode != EnterMode.CLSS) {
            return false;
        }
        int limitAmount = Integer.valueOf(SpManager.getSysParamSp().get(SysParamSp.QUICK_PASS_TRANS_PIN_FREE_AMOUNT));
        String amount = transData.getAmount().replace(".", "");
        String cardNo = transData.getPan();
        // 判断卡类型
        byte[] aid = null;
        aid = EmvManager.getTlv(0x4F);
        if (aid == null) {
            return false;
        }
        boolean isCredit = isCredit(GlManager.bcdToStr(aid));
        boolean pinFree;
        ETransType transType = transData.getTransType();
        if (ETransType.SALE.equals(transType)
                || ETransType.PREAUTH.equals(transType)) {
            pinFree = clssCDCVMProcss();
            transData.setCDCVM(pinFree);
            if (!pinFree) {
                if (SpManager.getSysParamSp().get(SysParamSp.QUICK_PASS_TRANS_FLAG).equals(SysParamSp.Constant.NO)) {
                    return false;
                }

                if (isCupOutSide()) { // 外卡
                    // 贷记或准贷记卡: 小于免密限额则免输密码 借记卡：依据卡片与终端协商结果

                    if (!isCredit) { // 借记卡
                        return false;
                    }
                    // 贷记卡或准贷记卡处理
                    return (Integer.parseInt(amount) <= limitAmount);
                } else { // 内卡
                    return (Integer.parseInt(amount) <= limitAmount);
                }
            } else {
                return true;
            }
        }

        return false;

    }

    /**
     * 是否免签
     *
     * @param transData
     * @return true: 免签 false: 需要签名
     */
    public static boolean isSignatureFree(TransData transData) {
        if (SysParamSp.Constant.NO.equals(SpManager.getSysParamSp().get(SysParamSp.QUICK_PASS_TRANS_SIGN_FREE_FLAG))) {
            return false;
        }

        int limitAmount = Integer.valueOf(SpManager.getSysParamSp().get(SysParamSp.QUICK_PASS_TRANS_SIGN_FREE_AMOUNT));
        String amount = transData.getAmount().replace(".", "");
        ETransType transType = transData.getTransType();
        if (!(ETransType.SALE.equals(transType) || ETransType.PREAUTH.equals(transType))) {
            return false;
        }
        return Integer.parseInt(amount) <= limitAmount;
    }

    /**
     * 磁道加密
     *
     * @param trackData
     * @return
     */
    public static String encryptTrack(String trackData) {
        if (trackData == null || trackData.length() == 0) {
            return null;
        }
        int len = trackData.length();
        if (trackData.length() % 2 > 0) {
            trackData += "0";
        }
        byte[] tb = new byte[8];
        byte[] bTrack = GlManager.strToBcdPaddingLeft(trackData);
        System.arraycopy(bTrack, bTrack.length - 9, tb, 0, 8);
        byte[] block = new byte[8];
        try {
            block = DalManager.getPedInternal().calcDes(Constants.INDEX_TDK, tb,
                    EPedDesMode.ENCRYPT);
        } catch (PedDevException e) {
            e.printStackTrace();
        }
        System.arraycopy(block, 0, bTrack, bTrack.length - 9, 8);
        return GlManager.bcdToStr(bTrack).substring(0, len);
    }

    /**
     * check whether the Neptune is installed, if not, display prompt
     *
     * @return
     */
    public static boolean neptuneInstalled(Context context, DialogInterface.OnDismissListener onDismissListener) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo("com.pax.ipp.neptune", 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo == null) {
            CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE, 5);
            dialog.setContentText(context.getString(R.string.please_install_neptune));
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            dialog.setOnDismissListener(onDismissListener);
            return false;
        }
        return true;
    }
}
