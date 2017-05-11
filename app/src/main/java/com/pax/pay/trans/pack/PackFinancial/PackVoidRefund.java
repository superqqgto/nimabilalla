package com.pax.pay.trans.pack.PackFinancial;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

/**
 * Created by wangyq on 2017/5/9.
 */

public class PackVoidRefund extends BasePackFinancial {
    public PackVoidRefund(PackListener listener) {
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
            setField_5(transData);
            setField_9(transData);
            setField_12(transData);
            setField_13(transData);
            setField_14(transData);
            setField_22(transData);
            setField_35(transData);
            setField_37(transData);
            setField_38(transData);
            setField_49(transData);

            //Optional field
            setField_63(transData);

            return pack(true);
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
