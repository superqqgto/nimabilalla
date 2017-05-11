package com.pax.pay.trans.pack.PackFinancial;

import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.pack.PackFinancial.BasePackFinancial;
import com.pax.pay.utils.LogUtils;

/**
 * Created by zhouhong on 2017/4/25.
 */

public class PackAdjust extends BasePackFinancial {

    public PackAdjust(PackListener listener) {
        super(listener);
    }

    @Override
    public byte[] pack(TransData transData) {

        LogUtils.d("PackAdjust", "------- PackAdjust pack -----------");
        if (transData == null) {
            return null;
        }
        try {
            //Financial类交易的共同Mandatory域
            setFinancialMandatory(transData);
            //特定的Mandatory域
            setField_12(transData);
            setField_13(transData);
            setField_37(transData);
            setField_62(transData);

            //条件域，C开头的
            setField_2(transData);
//            setField_5(transData);
//            setField_9(transData);
            setField_14(transData);
            setField_22(transData);
            setField_35(transData);
//            setField_49(transData);

            //可选域，O开头
            setField_38(transData);
            setField_54(transData);
            setField_60(transData);

            return pack(true);
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected int setField_37(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getOrigRefNo();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("37", temp);
        }
        return TransResult.SUCC;
    }

    protected int setField_60(TransData transData) throws Iso8583Exception {
        String temp = "";
        temp = transData.getOrigAmount();
        if (!TextUtils.isEmpty(temp)) {
            entity.setFieldValue("60", temp);
        }
        return TransResult.SUCC;
    }
}
