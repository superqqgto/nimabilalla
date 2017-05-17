package com.pax.pay.trans.pack.PackFinancial;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.pack.PackIso8583;

/**
 * Created by xub on 2017/5/9.
 */

public class PackBalance extends BasePackFinancial {

    public PackBalance(PackListener listener) {
        super(listener);
    }

    @Override
    public byte[] pack(TransData transData) {
        try {
            if (transData == null) {
                return null;
            }
            //setFinancialData(transData);
            //setAuthAndFinancialMandatory(transData);
            setFinancialMandatory(transData);
            setField_12(transData);
            setField_13(transData);
            setField_22(transData);
            setField_35(transData);
            setField_52(transData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pack(false);
    }
}
