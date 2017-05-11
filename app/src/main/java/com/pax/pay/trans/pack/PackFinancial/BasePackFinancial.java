package com.pax.pay.trans.pack.PackFinancial;

import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.base.DccTransData;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.pack.PackIso8583;
import com.pax.pay.utils.LogUtils;

/**
 * Created by huangmuhua on 2017/4/7.
 * 参考文档Terminal Specification for VMJC V1 6.pdf
 * 第5.3节Authorization and Financial Message Formats，包含10多种交易
 */

public abstract class BasePackFinancial extends PackIso8583 {

    private String temp = "";

    public BasePackFinancial(PackListener listener) {
        super(listener);
    }

    /**
     * 设置Authorization and Financial Message的共同M条件的域
     * <p> h,m
     * <p> field 3
     * field 4, field 11,field 24,field 25, field 41,field 42
     *
     * @param transData
     * @return
     */
    protected int setFinancialMandatory(TransData transData) throws Iso8583Exception {

        // h
        String pHeader = transData.getTpdu() + transData.getHeader();
        entity.setFieldValue("h", pHeader);
        // m
        ETransType transType = transData.getTransType();
        if (transData.getReversalStatus() == TransData.ReversalStatus.REVERSAL ||
                transData.getReversalStatus() == TransData.ReversalStatus.PENDING) {
            entity.setFieldValue("m", transType.getDupMsgType());
        } else {
            entity.setFieldValue("m", transType.getMsgType());
        }

        setField_4(transData);
        setField_11(transData);
        setField_24(transData);
        setField_25(transData);
        setField_41(transData);
        setField_42(transData);

        //虽然不属于Mandatory，但也是必要的。并且查表可得每个transaction的
        setField_3(transData);

        return TransResult.SUCC;
    }

    /**
     * field 37，original Retrieval Reference Number
     *
     * @param transData
     * @return
     */
    protected int setField_37(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getOrigRefNo();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("37", temp);
        }
        return TransResult.SUCC;
    }

    /**
     * field 38, auth code
     *
     * @param transData
     * @return
     */
    @Override
    protected int setField_38(TransData transData) throws Iso8583Exception {
        temp = transData.getOrigAuthCode();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("38", temp);
        }
        return TransResult.SUCC;
    }

    /**
     * field 62，Invoice No / Working Keys
     * @param transData
     * @return
     */
    @Override
    protected int setField_62(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = SpManager.getSysParamSp().get(SysParamSp.EDC_INVOICE_NUM);
        if (!TextUtils.isEmpty(temp)) {
            temp = Component.getPaddedNumber(Long.valueOf(temp), 6);
            entity.setFieldValue("62", temp);
        }

        return TransResult.SUCC;
    }

    /**
     * field 63, set auth convert amount for dcc related transaction
     *
     * @param transData
     * @return
     */
    protected int setField_63(TransData transData) throws Iso8583Exception {
        if (!transData.isDccTrans()) {
            return TransResult.SUCC;
        }

        DccTransData dccTransData = transData.getDccTransData();
        String temp;

        //设置tableLen
        byte[] bcdLen = GlManager.strToBcdPaddingLeft("0019");
        String dccBuf_63 = new String(bcdLen);
        LogUtils.d("PackIso8583", " setDccAuthBitData_63 bcdLen : " + GlManager.bcdToStr(bcdLen) + ", len: " + dccBuf_63.length());

        //设置tableId
        dccBuf_63 += "DC";

        //设置 Conversion Rate
        temp = dccTransData.getDccConvRate();
        if (!TextUtils.isEmpty(temp)) {
            dccBuf_63 += temp;
        }

        //设置Dcc Margin
        temp = dccTransData.getDccMargin();
        if (!TextUtils.isEmpty(temp)) {
            dccBuf_63 += temp;
        }

        LogUtils.d("PackIso8583", "setDccAuthBitData_63 : " + dccBuf_63);
        transData.setField63(String.valueOf(dccBuf_63));

        if (transData.getField63() != null) {
            entity.setFieldValue("63", transData.getField63());
        }

        return TransResult.SUCC;
    }


    protected int setMotoField_63(TransData transData) throws Iso8583Exception {
        //设置tableLen
        byte[] bcdLen = GlManager.strToBcdPaddingLeft("0009");
        String dccBuf_63 = new String(bcdLen);
        LogUtils.d("PackIso8583", " setMotoField_63 bcdLen : " + GlManager.bcdToStr(bcdLen) + ", len: " + dccBuf_63.length());

        dccBuf_63 += "16";

        temp = transData.getCVV2();
        if (!TextUtils.isEmpty(temp) && temp.length() < 4) {
            LogUtils.d("PackIso8583", " getCVV2: " + temp);
            dccBuf_63 += String.format("% 6s",  temp);
        }

        dccBuf_63 += ' ';

        LogUtils.d("PackIso8583", "setMotoField_63 : " + dccBuf_63);
        transData.setField63(String.valueOf(dccBuf_63));

        if (transData.getField63() != null) {
            entity.setFieldValue("63", transData.getField63());
        }

        return TransResult.SUCC;
    }

    public int unpackMotoField_63(TransData transData) {
        String field63 = transData.getField63();
        String tableId;
        String cvv2ResultCode;

        //判断63域长度是否合格
        int tableLen = Integer.valueOf(GlManager.bcdToStr(field63.substring(0, 2).getBytes()));
        LogUtils.d("PackIso8583", " tableLen : " + String.valueOf(tableLen));

//        if (tableLen > field63.length()) {
//            return TransResult.ERR_UNPACK;
//        }

        //判断tableId是否合格
        tableId = field63.substring(2, 4);
        LogUtils.d("PackIso8583", "tableId : " + tableId);

        if (field63.length() > 10) {
            cvv2ResultCode = field63.substring(10, 11);
        }

        return TransResult.SUCC;
    }
}
