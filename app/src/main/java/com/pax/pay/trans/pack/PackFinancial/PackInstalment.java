package com.pax.pay.trans.pack.PackFinancial;

import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.InstalmentTransData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.LogUtils;

import java.util.Arrays;

/**
 * Created by zhouhong on 2017/5/1.
 */

public class PackInstalment extends BasePackFinancial {

    public PackInstalment(PackListener listener) {
        super(listener);
    }

    @Override
    public byte[] pack(TransData transData) {
        if (transData == null) {
            return null;
        }
        try {
            //Financial common Mandatory field
            setFinancialMandatory(transData);
            //Unique Mandatory field
            setField_62(transData);

            //Condition field
            setField_2(transData);
            setField_14(transData);
            setField_22(transData);
            setField_35(transData);

            //Optional field
            setField_52(transData);
            setField_54(transData);
            setField_63(transData);

            return pack(true);
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected int setField_63(TransData transData) throws Iso8583Exception {
        InstalmentTransData instalTransData = transData.getInstalTransData();
        String tmpStr;

        byte[] dccBuf_63 = new byte[108];
        Arrays.fill(dccBuf_63, 0, 97, (byte) '0');
        Arrays.fill(dccBuf_63, 98, 107, (byte) ' ');

//        //设置tableLen
        byte[] bcdLen = GlManager.strToBcdPaddingLeft("0108");
        System.arraycopy(bcdLen, 0, dccBuf_63, 0, 2);

        //设置tableId
        System.arraycopy("EM".getBytes(), 0, dccBuf_63, 2, 2);

        tmpStr = instalTransData.getProductAmt();
        if (!TextUtils.isEmpty(tmpStr) && (tmpStr.length() == 12)) {
            System.arraycopy(tmpStr.getBytes(), 0, dccBuf_63, 4, 12);
        }

        tmpStr = instalTransData.getDiscountAmt();
        if (!TextUtils.isEmpty(tmpStr) && (tmpStr.length() == 12)) {
            System.arraycopy(tmpStr.getBytes(), 0, dccBuf_63, 16, 12);
        }

        tmpStr = instalTransData.getTenure();
        if (!TextUtils.isEmpty(tmpStr) && (tmpStr.length() == 2)) {
            System.arraycopy(tmpStr.getBytes(), 0, dccBuf_63, 28, 2);
        }

        tmpStr = instalTransData.getProgramCode();
        if (!TextUtils.isEmpty(tmpStr) && (tmpStr.length() == 6)) {
            System.arraycopy(tmpStr.getBytes(), 0, dccBuf_63, 88, 6);
        }

        tmpStr = instalTransData.getProductCode();
        if (!TextUtils.isEmpty(tmpStr) && (tmpStr.length() == 4)) {
            System.arraycopy(tmpStr.getBytes(), 0, dccBuf_63, 94, 4);
        }

        transData.setField63(new String(dccBuf_63));
        LogUtils.d("PackIso8583", "setInstalmentBitData_63 : " + transData.getField63());
        LogUtils.d("PackIso8583", " setInstalmentBitData_63 bcdLen : " + GlManager.bcdToStr(bcdLen) + ", len: " + transData.getField63().length());

        if (transData.getField63() != null) {
            entity.setFieldValue("63", transData.getField63());
        }

        return TransResult.SUCC;
    }

    @Override
    public int unpackField_63(TransData transData) {

        String field63 = transData.getField63();
        InstalmentTransData instalTransData = transData.getInstalTransData();
        String tableId;
        String tmpStr;

        //判断63域长度是否合格
        int tableLen = Integer.valueOf(GlManager.bcdToStr(field63.substring(0, 2).getBytes()));
        LogUtils.d("PackIso8583", " tableLen : " + String.valueOf(tableLen));

        if (tableLen > field63.length()) {
            return TransResult.ERR_UNPACK;
        }

        //判断tableId是否合格
        tableId = field63.substring(2, 4);
        if (!tableId.equals("EM")) {
            return TransResult.ERR_UNPACK;
        }
        LogUtils.d("PackIso8583", "tableId : " + tableId);
        instalTransData.setProductAmt(field63.substring(4, 16));
        LogUtils.d("PackIso8583", " ProductAmt : " + instalTransData.getProductAmt());
        instalTransData.setDiscountAmt(field63.substring(16, 28));
        LogUtils.d("PackIso8583", " DiscountAmt : " + instalTransData.getDiscountAmt());
        instalTransData.setTenure(field63.substring(28, 30));
        LogUtils.d("PackIso8583", " Tenure : " + instalTransData.getTenure());
        instalTransData.setInterestRate(field63.substring(30, 35));
        LogUtils.d("PackIso8583", " InterestRate : " + instalTransData.getInterestRate());
        String roiTenure = field63.substring(35, 40);
        instalTransData.setInterestAmt(field63.substring(40, 52));
        LogUtils.d("PackIso8583", " InterestAmt : " + instalTransData.getInterestAmt());
        instalTransData.setTotalAmt(field63.substring(52, 64));
        LogUtils.d("PackIso8583", " TotalAmt : " + instalTransData.getTotalAmt());
        instalTransData.setEmiPerMonth(field63.substring(64, 76));
        LogUtils.d("PackIso8583", " EmiPerMonth : " + instalTransData.getEmiPerMonth());
        instalTransData.setProcessFee(field63.substring(76, 88));
        LogUtils.d("PackIso8583", " ProcessFee : " + instalTransData.getProcessFee());
        instalTransData.setProgramCode(field63.substring(88, 94));
        LogUtils.d("PackIso8583", " ProgramCode : " + instalTransData.getProgramCode());
        instalTransData.setProductCode(field63.substring(94, 98));
        LogUtils.d("PackIso8583", " ProductCode : " + instalTransData.getProductCode());

        return TransResult.SUCC;
    }
}
