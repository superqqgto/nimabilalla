package com.pax.pay.trans.pack.PackFinancial;

import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.LogUtils;

import java.util.Arrays;

/**
 * Created by zhouhong on 2017/5/1.
 */

public class PackOfflineTransSend extends BasePackFinancial {
    public PackOfflineTransSend(PackListener listener) {
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
            setField_12(transData);
            setField_13(transData);
            setField_38(transData);
            setField_39(transData);
            setField_62(transData);

            //Condition field
            setField_2(transData);
            setField_14(transData);
            setField_22(transData);
            setField_35(transData);
            setField_55(transData);

            //Optional field
            setField_54(transData);
            setField_63(transData);
            return pack(true);
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected int setField_38(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getAuthCode();
        LogUtils.d("PackIso8583", "PackIso8583 38 " + temp);
        if (temp != null && temp.length() > 0) {
            entity.setFieldValue("38", temp);
        }

        return TransResult.SUCC;
    }

    @Override
    protected int setField_63(TransData transData) throws Iso8583Exception {
        byte[] dccBuf_63 = new byte[24];
        Arrays.fill(dccBuf_63, 0, 23, (byte) 0);

        //set tableLen
        byte[] bcdLen = GlManager.strToBcdPaddingLeft("0022");
        System.arraycopy(bcdLen, 0, dccBuf_63, 0, 2);

        //set tableId
        System.arraycopy("OS".getBytes(), 0, dccBuf_63, 2, 2);

        //"OFFLINE-KEYEDIN" or "OFFLINE-TMS"
        System.arraycopy("OFFLINE-KEYEDIN".getBytes(), 0, dccBuf_63, 4, "OFFLINE-KEYEDIN".length());

        transData.setField63(new String(dccBuf_63));
        LogUtils.d("PackIso8583", "setOfflineSaleBitData_63 : " + transData.getField63());
        LogUtils.d("PackIso8583", " setOfflineSaleBitData_63 bcdLen : " + GlManager.bcdToStr(bcdLen) + ", len: " + transData.getField63().length());

        if (transData.getField63() != null) {
            entity.setFieldValue("63", transData.getField63());
        }

        return TransResult.SUCC;
    }
}
