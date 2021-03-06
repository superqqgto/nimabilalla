package com.pax.pay.trans.pack.PackFinancial;

import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;

/**
 * Created by zhouhong on 2017/5/10.
 */

public class PackMotoPreAuthCompCancel extends BasePackFinancial {

    public PackMotoPreAuthCompCancel(PackListener listener) {
        super(listener);
    }

    @Override
    public byte[] pack(TransData transData) {
        if (transData == null) {
            return null;
        }
        try {
            //Auth和Financial类交易的共同Mandatory域
            setFinancialMandatory(transData);
            setField_12(transData);
            setField_13(transData);
            setField_37(transData);
            setField_38(transData);
            setField_62(transData);

            //条件域，C开头的
            setField_2(transData);
            setField_5(transData);
            setField_9(transData);
            setField_14(transData);
            setField_22(transData);
            setField_35(transData);
            setField_49(transData);

            //optional
            setField_63(transData);

        } catch (Iso8583Exception e) {
            e.printStackTrace();
        }
        return pack(true);
    }

    /**
     * field 12, 13，set original transaction time
     *
     * @param transData
     * @return
     */
    @Override
    protected int setField_12(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getOrigDateTime();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("12", temp.substring(8, 14));
        }
        return TransResult.SUCC;
    }

    @Override
    protected int setField_13(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getOrigDateTime();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("13", temp.substring(4, 8));

        }
        return TransResult.SUCC;
    }
}
