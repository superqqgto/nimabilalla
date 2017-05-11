package com.pax.pay.trans.pack.PackFinancial;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.pack.PackFinancial.BasePackFinancial;

/**
 * Created by huangmuhua on 2017/3/21.
 */

public class PackPreAuthComp extends BasePackFinancial {
    public PackPreAuthComp(PackListener listener) {
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
            //38、39也是PreAuth Comp的Mandatory
            setField_38(transData);
            setField_39(transData);
            setField_62(transData);

            //条件域，C开头的
            setField_2(transData);
            setField_14(transData);
            setField_22(transData);
            setField_35(transData);
            setField_49(transData);
            setField_55(transData);

            return pack(true);
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
