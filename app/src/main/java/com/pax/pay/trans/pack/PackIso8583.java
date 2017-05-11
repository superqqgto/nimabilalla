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
package com.pax.pay.trans.pack;

import android.text.TextUtils;

import com.pax.abl.core.ipacker.IPacker;
import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.convert.IConvert;
import com.pax.gl.packer.IIso8583;
import com.pax.gl.packer.IIso8583.IIso8583Entity;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.manager.AcqManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.BaseTransData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.LogUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

public abstract class PackIso8583 implements IPacker<TransData, byte[]> {
    private IIso8583 iso8583;
    protected IIso8583Entity entity;
    protected PackListener listener;

    public PackIso8583(PackListener listener) {
        this.listener = listener;
        initEntity();
    }

    /**
     * 获取打包entity
     *
     * @return
     */
    private void initEntity() {
        iso8583 = GlManager.getPacker().getIso8583();
        try {
            entity = iso8583.getEntity();
            entity.loadTemplate(ContextUtils.getAssets().open("edc8583.xml"));
        } catch (Iso8583Exception | IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    protected byte[] pack(boolean isNeedMac) {
        try {

            if (isNeedMac) {
                entity.setFieldValue("64", new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
            }
            // for debug
            // entity.dump();
            byte[] packData = iso8583.pack();

            if (isNeedMac) {
                if (packData == null || packData.length == 0) {
                    return null;
                }

                int len = packData.length;

                byte[] calMacBuf = new byte[len - 11 - 8];//去掉header和mac
                System.arraycopy(packData, 11, calMacBuf, 0, len - 11 - 8);
                byte[] mac = listener.onCalcMac(calMacBuf);
                if (mac == null) {
                    return null;
                }
                System.arraycopy(mac, 0, packData, len - 8, 8);
            }

            return packData;
        } catch (Iso8583Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置公共数据
     * <p>
     * 设置域： h,m, field 3, field 25, field 41,field 42
     *
     * @param transData
     * @return
     */
    protected int setMandatoryData(TransData transData) {
        try {
            String temp = "";
            // h
            String pHeader = transData.getTpdu() + transData.getHeader();
            entity.setFieldValue("h", pHeader);
            // m
            ETransType transType = transData.getTransType();
            if (transData.getReversalStatus() == TransData.ReversalStatus.REVERSAL) {
                entity.setFieldValue("m", transType.getDupMsgType());
            } else {
                entity.setFieldValue("m", transType.getMsgType());
            }

            // field 3/25 交易处理码/服务码
            // 脚本上送同执行该脚本通知交易的原始信息
            /*temp = transData.getOrigTransType();
            if (temp == null || temp.length() == 0)
                return TransResult.ERR_PACK;*/

            temp = transType.getProcCode();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("3", temp);
            }
            temp = transType.getServiceCode();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("25", temp);
            }

            // field 24 NII
            transData.setNii(AcqManager.getInstance().getCurAcq().getNii());
            temp = transData.getNii();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("24", temp);
            }

            // field 41 终端号
            temp = transData.getAcquirer().getTerminalId();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("41", temp);
            }

            // field 42 商户号
            temp = transData.getAcquirer().getMerchantId();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("42", temp);
            }

            temp = SpManager.getSysParamSp().get(SysParamSp.EDC_INVOICE_NUM);
            if (temp != null && temp.length() > 0) {
                temp = Component.getPaddedNumber(Long.valueOf(temp), 6);
                entity.setFieldValue("62", temp);
            }

            return TransResult.SUCC;

        } catch (Exception e) {
            e.printStackTrace();
            return TransResult.ERR_PACK;
        }
    }

    /**
     * 设置批上送公共数据
     * <p>
     * 设置域： h,m, field 3, field 25, field 41,field 42
     * field 4、11、12、14、22
     *
     * @param transData
     * @return
     */
    public int setBatchUpCommonData(TransData transData) {
        String temp = "";

        try {
            //field 2
            temp = transData.getPan();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("2", temp);
            }
            // field 4 交易金額
            temp = transData.getAmount();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("4", temp);
            }
            // field 11 流水号
            temp = String.valueOf(transData.getTraceNo());
            if (temp.length() > 0) {
                entity.setFieldValue("11", temp);
            }
            //field 12
            temp = transData.getDateTime();
            if (temp.length() > 0) {
                String date = temp.substring(4, 8);
                String time = temp.substring(8, temp.length());
                entity.setFieldValue("12", time);
                entity.setFieldValue("13", date);
            }
            //field 14
            temp = transData.getExpDate();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("14", temp);
            }
            // field 22
            if (transData.getEnterMode() != null) { //AET-40
                temp = getInputMethod(transData.getEnterMode(), transData.isHasPin());
                if (temp != null && temp.length() > 0) {
                    entity.setFieldValue("22", temp);
                }
            }
            // field 25
//            temp = transData.gets;
//            if (temp != null && temp.length() > 0) {
//                entity.setFieldValue("25", temp);
//            }
            //field 37
            temp = transData.getRefNo();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("37", temp);
            }
            return TransResult.SUCC;
        } catch (Iso8583Exception e) {
            e.printStackTrace();
        }

        return TransResult.ERR_PACK;
    }

    /**
     * 设置field 2, 4, 11, 14, 22, 23, 26, 35,36,49,52, 53
     *
     * @param transData
     * @return
     */
    private int setCommonData(TransData transData) {
        String temp = "";
        try {

            TransData.EnterMode enterMode = transData.getEnterMode();

            if (enterMode == TransData.EnterMode.MANUAL) {
                // 手工输入
                // [2]主账号,[14]有效期
                temp = transData.getPan();
                if (temp != null && temp.length() > 0) {
                    entity.setFieldValue("2", temp);
                }

                temp = transData.getExpDate();
                if (temp != null && temp.length() > 0) {
                    entity.setFieldValue("14", temp);
                }

            } else if (enterMode == TransData.EnterMode.SWIPE) {
                // 刷卡

                // [35]二磁道,[36]三磁道
                temp = transData.getTrack2();
                if (temp != null && temp.length() > 0) {
                    entity.setFieldValue("35", temp);
                }

//                temp = transData.getTrack3();
//                if (temp != null && temp.length() > 0) {
//                    entity.setFieldValue("36", temp);
//                }

                //[54]tip amount  by lixc
                temp = transData.getTipAmount();
                if (temp != null && temp.length() > 0) {
                    entity.setFieldValue("54", temp);
                }

            } else if (enterMode == TransData.EnterMode.INSERT || enterMode == TransData.EnterMode.CLSS) {
                // [2]主账号
                temp = transData.getPan();
                if (temp != null && temp.length() > 0) {
                    entity.setFieldValue("2", temp);
                }
                // [14]有效期
                temp = transData.getExpDate();
                if (temp != null && temp.length() > 0) {
                    entity.setFieldValue("14", temp);
                }
//                // [23]卡序列号
//                temp = transData.getCardSerialNo();
//                if (temp != null && temp.length() > 0) {
//                    entity.setFieldValue("23", temp);
//                }
                // [35]二磁道
                temp = transData.getTrack2();
                if (temp != null && temp.length() > 0) {
                    entity.setFieldValue("35", temp);
                }
            }

            // field 4
            temp = transData.getAmount();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("4", temp);

                ETransType transType = transData.getTransType();
                if ((transType == ETransType.SALE
                        || transType == ETransType.PREAUTH
                        || transType == ETransType.PREAUTH_COMP
                        || transType == ETransType.VOID)
                        && transData.getDccTransData() != null && transData.isDccTrans() && transType != null
                        ) {
                    if (transData.getDccTransData().isDccOptIn()) {
                        temp = transData.getDccTransData().getDccTransAmt();
                        if (temp != null && temp.length() > 0) {
                            entity.setFieldValue("5", temp);
                        }

                        // field 9
                        temp = transData.getDccTransData().getDccConvRate();
                        if (temp != null && temp.length() > 0) {
                            entity.setFieldValue("9", temp);
                        }

                        // field 49
                        temp = transData.getDccTransData().getDccCurrency();
                        if (temp != null && temp.length() > 0) {
                            entity.setFieldValue("49", temp);
                        }

                    } else {
                        entity.setFieldValue("5", temp);
                    }
                }
            }

            // field 11 流水号
            temp = String.valueOf(transData.getTraceNo());
            if (temp.length() > 0) {
                entity.setFieldValue("11", temp);
            }

            // field 22 服务点输入 方式码
            entity.setFieldValue("22", getInputMethod(enterMode, transData.isHasPin()));

            // [26]服务点PIN获取码,[52]PIN,[53]安全控制信息
            if (transData.isHasPin() && !TextUtils.isEmpty(transData.getPin())) {
                entity.setFieldValue("52",
                        GlManager.strToBcdPaddingLeft(transData.getPin()));
            }

            return TransResult.SUCC;
        } catch (Iso8583Exception e) {
            e.printStackTrace();
        }

        return TransResult.ERR_PACK;
    }

    /**
     * 设置金融类数据
     * <p>
     * 设置域
     * <p>
     * field 2, field 4,field 14, field 22,field 23,field 26, field 35,field 36,field 49, field 52,field 53, field 55
     *
     * @param transData
     */
    protected int setFinancialData(TransData transData) {

        try {
            int ret = setMandatoryData(transData);
            if (ret != TransResult.SUCC) {
                return ret;
            }
            ret = setCommonData(transData);
            if (ret != TransResult.SUCC) {
                return ret;
            }

            // field 55
            String temp = transData.getSendIccData();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("55", GlManager.strToBcdPaddingLeft(temp));
            }

            return TransResult.SUCC;
        } catch (Exception e) {
            e.printStackTrace();
            return TransResult.ERR_PACK;
        }
    }

    /**
     * 设置撤销类交易 设置域
     * <p>
     * field 2, field 4, field 14,field 22, field 23,field 26,field 35,field 36,
     * <p>
     * field 37,field 38, field 49,field 53,field 61
     *
     * @param transData
     * @return
     */
    protected int setVoidCommonData(TransData transData) {
        try {
            String temp = "";
            int ret = 0;

            ret = setMandatoryData(transData);
            if (ret != TransResult.SUCC) {
                return ret;
            }
            ret = setCommonData(transData);
            if (ret != TransResult.SUCC) {
                return ret;
            }

            temp = transData.getOrigDateTime();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("12", temp.substring(8, 14));
                entity.setFieldValue("13", temp.substring(4, 8));
            }

            // [37]原参考号
            temp = transData.getOrigRefNo();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("37", temp);
            }
            // [38]原授权码
            temp = transData.getOrigAuthCode();
            if (temp != null && temp.length() > 0) {
                entity.setFieldValue("38", temp);
            }
            // field 61
            String f61 = "";
            temp = Component.getPaddedNumber(transData.getOrigBatchNo(), 6);
            if (temp != null && temp.length() > 0) {
                f61 += temp;
            } else {
                f61 += "000000";
            }
            temp = Component.getPaddedNumber(transData.getOrigTransNo(), 6);
            if (temp != null && temp.length() > 0) {
                f61 += temp;
            } else {
                f61 += "000000";
            }
            entity.setFieldValue("61", f61);

            return TransResult.SUCC;
        } catch (Exception e) {
            e.printStackTrace();
            return TransResult.ERR_PACK;
        }
    }


    /**
     * 检查请求和返回的关键域field4, field11, field41, field42
     *
     * @param map        解包后的map
     * @param transData  请求
     * @param isCheckAmt 是否检查field4
     * @return
     */
    protected int checkRecvData(HashMap<String, byte[]> map, TransData transData, boolean isCheckAmt) {
        String temp;
        byte[] data;
        // 交易金额
        if (isCheckAmt) {
            data = map.get("4");
            if (data != null && data.length > 0) {
                temp = new String(data);
                if (Long.parseLong(temp) != Long.parseLong(transData.getAmount())) {
                    return TransResult.ERR_TRANS_AMT;
                }
            }
        }
        // 校验11域
        data = map.get("11");
        if (data != null && data.length > 0) {
            temp = new String(data);
            if (!temp.equals(Component.getPaddedNumber(transData.getTraceNo(), 6))) {
                return TransResult.ERR_TRACE_NO;
            }
        }
        // 校验终端号
        data = map.get("41");
        if (data != null && data.length > 0) {
            temp = new String(data);
            if (!temp.equals(transData.getAcquirer().getTerminalId())) {
                return TransResult.ERR_TERM_ID;
            }
        }
        // 校验商户号
        data = map.get("42");
        if (data != null && data.length > 0) {
            temp = new String(data);
            if (!temp.equals(transData.getAcquirer().getMerchantId())) {
                return TransResult.ERR_MERCH_ID;
            }
        }
        return TransResult.SUCC;
    }

    // 设置 field 48
    protected int setBitData_48(TransData transData) {
        try {
            ETransType transType = transData.getTransType();
            switch (transType) {
                case SETTLE:
                case BATCH_UP:
                case SETTLE_END:
                    entity.setFieldValue("48", transData.getField48());
                    break;

                default:
                    break;
            }
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return TransResult.ERR_PACK;
        }
        return TransResult.SUCC;
    }

    // 设置field 60
    protected int setBitData_60(TransData transData) {
        try {
            String temp = "";
            ETransType transType = transData.getTransType();

            String f60 = Component.getPaddedNumber(transData.getBatchNo(), 6); // f60.2

//            if (transType == ETransType.SALE || transType == ETransType.VOID
//                    || transType == ETransType.REFUND
//                    || transType == ETransType.PREAUTH) {
//                temp = "60";
//                temp += "0";
//                f60 += temp;
//            } else if (transType == ETransType.OFFLINE_TRANS_SEND) {
//                temp = "60";
//                f60 += temp;
//            }

            if (transType == ETransType.SETTLE_END) {
                f60 = transData.getField60();
            }

            entity.setFieldValue("60", f60);

        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return TransResult.ERR_PACK;
        }

        return TransResult.SUCC;

    }

    // set field 63
    protected int setBitData_63(TransData transData) {
        try {
            ETransType transType = transData.getTransType();
            switch (transType) {
                case SETTLE:
                case BATCH_UP:
                case SETTLE_END:
                    break;
//                case DCC:
//                    setDccGetBitData_63(transData);
//                    break;
//                case SALE:
//                case VOID:
//                case PREAUTH:
//                case PREAUTH_COMP:
//                    setDccAuthBitData_63(transData);
//                    break;
//                case INSTALMENT:
//                    setInstalmentBitData_63(transData);
//                    break;
//                case OFFLINE_TRANS_SEND:
//                    setOfflineSaleBitData_63(transData);
                default:
                    break;
            }

            if (transData.getField63() != null) {
                entity.setFieldValue("63", transData.getField63());
            }
        } catch (Iso8583Exception e) {
            LogUtils.d("PackIso8583", "PackIso8583 63 field -- Exception ");
            e.printStackTrace();
            return TransResult.ERR_PACK;
        }
        return TransResult.SUCC;
    }

    /**
     * @param enterMode
     * @param hasPin
     * @return
     */
    protected String getInputMethod(TransData.EnterMode enterMode, boolean hasPin) {
        String inputMethod = "";
        if (enterMode != null) {
            switch (enterMode) {
                case MANUAL:
                    inputMethod = "01";
                    break;
                case SWIPE:
                    inputMethod = "02";
                    break;
                case INSERT:
                    inputMethod = "05";
                    break;
                case CLSS:
                    inputMethod = "07";
                    break;
                case FALLBACK:
                    inputMethod = "80";
                    break;
                default:
                    break;
            }
        }

        if (hasPin) {
            inputMethod += "1";
        } else {
            inputMethod += "2";
        }

        return inputMethod;
    }

    /*********************************************
     unpack
     ********************************************/
    public int unpackField_62(TransData transData) {
        return TransResult.SUCC;
    }

    public int unpackField_63(TransData transData) {
        return TransResult.SUCC;
    }

    @Override
    public int unpack(TransData transData, byte[] rsp) {

        HashMap<String, byte[]> map = null;
        try {
            map = iso8583.unpack(rsp, true);
            // 调试信息， 日志输入解包后数据
            entity.dump();
        } catch (Exception e) {
            e.printStackTrace();
            return TransResult.ERR_UNPACK;
        }

        // 报文头
        byte[] header = map.get("h");
        // TPDU检查
        String rspTpdu = new String(header).substring(0, 10);
        String reqTpdu = transData.getTpdu();
        if (!rspTpdu.substring(2, 6).equals(reqTpdu.substring(6, 10))
                || !rspTpdu.substring(6, 10).equals(reqTpdu.substring(2, 6))) {
            return TransResult.ERR_UNPACK;
        }
        transData.setHeader(new String(header).substring(10));

        ETransType transType = transData.getTransType();

        byte[] buff;
        // 检查39域应答码
        buff = map.get("39");
        if (buff == null) {
            return TransResult.ERR_BAG;
        }
        transData.setResponseCode(new String(buff));

        // 检查返回包的关键域， 包含field4
        boolean isCheckAmt = true;
        if (transType == ETransType.SETTLE) {
            isCheckAmt = false;
        }
        int ret = checkRecvData(map, transData, isCheckAmt);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        // field 2 主账号

        // field 3 交易处理码
        buff = map.get("3");
        if (buff != null && buff.length > 0) {
            String origField3 = transData.getField3();
            if (origField3 != null && origField3.length() > 0) {
                if (!origField3.equals(new String(buff))) {
                    return TransResult.ERR_PROC_CODE;
                }
            }
        }
        // field 4 交易金额
        buff = map.get("4");
        if (buff != null && buff.length > 0) {
            transData.setAmount(new String(buff));
        }

        // field 11 流水号
        buff = map.get("11");
        if (buff != null && buff.length > 0) {
            transData.setTraceNo(Long.parseLong(new String(buff)));
        }

        // field 13 受卡方所在地日期
        String dateTime = "";
        buff = map.get("13");
        if (buff != null) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            dateTime = Integer.toString(year) + new String(buff);
        }

        // field 12 受卡方所在地时间
        buff = map.get("12");
        if (buff != null && buff.length > 0) {
            transData.setDateTime(dateTime + new String(buff));
        }

        // field 14 卡有效期
        buff = map.get("14");
        if (buff != null && buff.length > 0) {
            String expDate = new String(buff);
            if (!expDate.equals("0000")) {
                transData.setExpDate(expDate);
            }
        }

        // field 22

        // field 23 卡片序列号
        buff = map.get("23");
        if (buff != null && buff.length > 0) {
            transData.setCardSerialNo(new String(buff));
        }
        // field 25
        // field 26

        // field 35
        // field 36

        // field 37 检索参考号
        buff = map.get("37");
        if (buff != null && buff.length > 0) {
            transData.setRefNo(new String(buff));
        }

        // field 38 授权码
        buff = map.get("38");
        if (buff != null && buff.length > 0) {
            transData.setAuthCode(new String(buff));
        }

        /*
        // field 41 校验终端号
        buff = map.get("41");
        if (buff != null && buff.length > 0) {
            transData.setTermID(new String(buff));
        }

        // field 42 校验商户号
        buff = map.get("42");
        if (buff != null && buff.length > 0) {
            transData.setMerchID(new String(buff));
        }
        */

        // field 44
        buff = map.get("44");
        if (buff != null && buff.length > 11) {
            String temp = new String(buff).substring(0, 11).trim();
            transData.setIssuerCode(temp);
            if (buff.length > 11) {
                temp = new String(buff).substring(11).trim();
                transData.setAcqCode(temp);
            }
        }
        // field 48
        buff = map.get("48");
        if (buff != null && buff.length > 0) {
            transData.setField48(new String(buff));
        }

        // field 52

        // field 53

        // field 54

        // field 55
        buff = map.get("55");
        if (buff != null && buff.length > 0) {
            transData.setRecvIccData(GlManager.bcdToStr(buff));
        }

        // field 58

        // field 60
        buff = map.get("60");
        if (buff != null && buff.length > 0) {
            transData.setBatchNo(Long.parseLong(new String(buff).substring(2, 8)));
        }

        // field 61
        // field 62
        buff = map.get("62");
        if (buff != null && buff.length > 0) {
//            transData.setField62(GlManager.bcdToStr(buff));
            transData.setField62(new String(buff));
            unpackField_62(transData);
        }

        //field 63
        buff = map.get("63");       //Added
        if (buff != null && buff.length > 0) {
            transData.setField63(new String(buff));
            unpackField_63(transData);
        }

        // field 64
        // 解包校验mac
//        byte[] data = new byte[rsp.length - 11 - 8];
//        System.arraycopy(rsp, 11, data, 0, data.length);
//        buff = map.get("64");
//        if (buff != null && buff.length > 0 && listener != null) {
//            byte[] mac = listener.onCalcMac(data);
//            if (!FinancialApplication.gl.getUtils().isByteArrayValueSame(buff, 0, mac, 0, 8)) {
//                return TransResult.ERR_MAC;
//            }
//        }

        return TransResult.SUCC;
    }


////////////////////代码重构部分///////////////////////
    /**
     * field 2，Primary Account Number，PAN
     *
     * @param transData
     * @return
     */
    protected int setField_2(TransData transData) throws Iso8583Exception {

        String temp = "";
        if (TransData.EnterMode.MANUAL == transData.getEnterMode()) {
            temp = transData.getPan();
            if (!TextUtils.isEmpty(temp)) {
                entity.setFieldValue("2", temp);
            }
        }

        return TransResult.SUCC;
    }

     /* field 3，Processing Code
     * processing code
     *
     * @param transData
     * @return
     */
    protected int setField_3(TransData transData) throws Iso8583Exception {
        String temp = "";
        ETransType transType = transData.getTransType();
        if (null == transType) {
            return TransResult.ERR_PACK;
        }
        temp = transType.getProcCode();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("3", temp);
        }
        return TransResult.SUCC;
    }

    /**
     * field 4，Amount
     *
     * @param transData
     * @return
     */
    protected int setField_4(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getAmount();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("4", temp);
        }
        return TransResult.SUCC;
    }

    /**
     * field 5，Amount for dcc，Settlement,C12
     *
     * @param transData
     * @return
     */
    protected int setField_5(TransData transData) throws Iso8583Exception {
        String temp = "";
        if (transData.isDccTrans()) {
            if (transData.getDccTransData().isDccOptIn()) {
                temp = transData.getDccTransData().getDccTransAmt();
                if (!TextUtils.isEmpty(temp)) {
                    entity.setFieldValue("5", temp);
                }
            } else {
                entity.setFieldValue("5", transData.getAmount());
            }
        }

        return TransResult.SUCC;
    }

    /**
     * field 9, Conversion rate for dcc, settlement, C12
     *
     * @param transData
     * @return
     */
    protected int setField_9(TransData transData) throws Iso8583Exception {
        String temp = "";
        if (transData.isDccTrans() && transData.getDccTransData().isDccOptIn()) {
            temp = transData.getDccTransData().getDccConvRate();
            if (!TextUtils.isEmpty(temp)) {
                entity.setFieldValue("9", temp);
            }
        }
        return TransResult.SUCC;
    }

    /**
     * field 11，ystem Trace Number
     *
     * @param transData
     * @return
     */
    protected int setField_11(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = String.valueOf(transData.getTraceNo());
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("11", temp);
        }

        return TransResult.SUCC;
    }

    /**
     * field 12, 13，set time
     *
     * @param transData
     * @return
     */
    protected int setField_12(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getDateTime();
        if (!TextUtils.isEmpty(temp) && temp.length() > 8) {
            String time = temp.substring(8, temp.length());
            entity.setFieldValue("12", time);
        }
        return TransResult.SUCC;
    }

    protected int setField_13(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getDateTime();
        if (!TextUtils.isEmpty(temp) && temp.length() > 7) {
            String date = temp.substring(4, 8);
            entity.setFieldValue("13", date);
        }
        return TransResult.SUCC;
    }

    /**
     * field 14，Expiration Date,C02
     *
     * @param transData
     * @return
     */
    protected int setField_14(TransData transData) throws Iso8583Exception {
        String temp = "";
        boolean acceptExpiryDate = true;//the card processing options are set to accept expiry date
        if (TransData.EnterMode.MANUAL == transData.getEnterMode() && acceptExpiryDate) {
            temp = transData.getExpDate();
            if (!TextUtils.isEmpty(temp)) {
                entity.setFieldValue("14", temp);
            }
        }
        return TransResult.SUCC;
    }

    /**
     * field 22，，POS Entry Mode, C03
     *
     * @param transData
     * @return
     */
    protected int setField_22(TransData transData) throws Iso8583Exception {
        String temp = "";
        BaseTransData.EnterMode enterMode = transData.getEnterMode();
        if (null != enterMode) {
            temp = getInputMethod(enterMode, transData.isHasPin());
            if (!TextUtils.isEmpty(temp)) {
                entity.setFieldValue("22", temp);
            }
        }
        return TransResult.SUCC;
    }

    /**
     * field 24，Network International ID
     *
     * @param transData
     * @return
     */
    protected int setField_24(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = AcqManager.getInstance().getCurAcq().getNii();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("24", temp);
        }

        return TransResult.SUCC;
    }

    /**
     * field 25，Point of Service Condition Code
     *
     * @param transData
     * @return
     */
    protected int setField_25(TransData transData) throws Iso8583Exception {
        String temp = "";
        ETransType transType = transData.getTransType();
        temp = transType.getServiceCode();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("25", temp);
        }
        return TransResult.SUCC;
    }

    /**
     * field 35，Track 2 Data, C04, if is IC card or magnetic stripe card, need track2 data
     *
     * @param transData
     * @return
     */
    protected int setField_35(TransData transData) throws Iso8583Exception {
        String temp = "";
        BaseTransData.EnterMode enterMode = transData.getEnterMode();
        if (null != enterMode) {
            if (TransData.EnterMode.SWIPE == enterMode || TransData.EnterMode.INSERT == enterMode
                    || TransData.EnterMode.CLSS == enterMode) {
                temp = transData.getTrack2();
                if (!TextUtils.isEmpty(temp)) {
                    entity.setFieldValue("35", temp);
                }
            }
        }

        return TransResult.SUCC;
    }

    /**
     * field 37，Retrieval Reference Number
     *
     * @param transData
     * @return
     */
    protected int setField_37(TransData transData) throws Iso8583Exception {
        return TransResult.SUCC;
    }

    /**
     * field 38, auth code
     *
     * @param transData
     * @return
     */
    protected int setField_38(TransData transData) throws Iso8583Exception {
        return TransResult.SUCC;
    }

    /**
     * 39 filed，response code from the original transaction
     *
     * @param transData
     * @return
     * @throws Iso8583Exception
     */
    protected int setField_39(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getOrigRspCode();
        LogUtils.d("PackIso8583", "PackIso8583 39 " + temp);
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("39", temp);
        }
//        else {
//            entity.setFieldValue("39", "00");
//        }
        return TransResult.SUCC;
    }

    /**
     * field 41，Terminal ID
     *
     * @param transData
     * @return
     */
    protected int setField_41(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getAcquirer().getTerminalId();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("41", temp);
        }
        return TransResult.SUCC;
    }

    /**
     * field 42，Card Acceptor Indentification Code,merchant Id
     *
     * @param transData
     * @return
     */
    protected int setField_42(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getAcquirer().getMerchantId();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("42", temp);
        }

        return TransResult.SUCC;
    }

//    protected int setField_38(TransData transData) throws Iso8583Exception {
//        // field 38, 授权码
//        return TransResult.SUCC;
//    }

    /**
     * field 49，Currency, for dcc transaction,C13
     *
     * @param transData
     * @return
     */
    protected int setField_49(TransData transData) throws Iso8583Exception {
        String temp = "";
        if (transData.isDccTrans()) {
            temp = transData.getDccTransData().getDccCurrency();
            if (!TextUtils.isEmpty(temp)) {
                entity.setFieldValue("49", temp);
            }
        }
        return TransResult.SUCC;
    }

    /**
     * field 52，[26]服务点PIN获取码,[52]PIN,[53]安全控制信息
     *
     * @param transData
     * @return
     */
    protected int setField_52(TransData transData) throws Iso8583Exception {
        if (transData.isHasPin() && !TextUtils.isEmpty(transData.getPin())) {
            entity.setFieldValue("52",
                    GlManager.strToBcdPaddingLeft(transData.getPin()));
        }

        return TransResult.SUCC;
    }

    /**
     * field 54，Additional Amount (tip amount)
     *
     * @param transData
     * @return
     */
    protected int setField_54(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getTipAmount();
        if (!TextUtils.isEmpty(temp)) {
            temp = GlManager.getConvert().stringPadding(temp, '0', 12, IConvert.EPaddingPosition.PADDING_LEFT);
            entity.setFieldValue("54", temp);
        }
        return TransResult.SUCC;
    }

    /**
     * field 55，EMV Data, C07
     *
     * @param transData
     * @return
     */
    protected int setField_55(TransData transData) throws Iso8583Exception {
        String temp = "";
        BaseTransData.EnterMode enterMode = transData.getEnterMode();
        if (null != enterMode) {
            if (TransData.EnterMode.INSERT == enterMode
                    || TransData.EnterMode.CLSS == enterMode) {
                temp = transData.getSendIccData();
                if (!TextUtils.isEmpty(temp)) {
                    entity.setFieldValue("55", GlManager.strToBcdPaddingLeft(temp));
                }
            }
        }

        return TransResult.SUCC;
    }

    //Data Element 060 - Reserved Private Fields
    protected int setField_60(TransData transData) throws Iso8583Exception {
        return TransResult.SUCC;
    }

//    protected int setField_61(TransData transData) throws Iso8583Exception {
//        return TransResult.SUCC;
//    }

    /**
     * field 62，Invoice No / Working Keys
     * @param transData
     * @return
     */
    protected int setField_62(TransData transData) throws Iso8583Exception {
        return TransResult.SUCC;
    }

    protected int setField_63(TransData transData) throws Iso8583Exception {
        return TransResult.SUCC;
    }
}
