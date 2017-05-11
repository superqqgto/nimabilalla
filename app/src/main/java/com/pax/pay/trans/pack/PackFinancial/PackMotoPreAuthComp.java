package com.pax.pay.trans.pack.PackFinancial;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

/**
 * Created by zhouhong on 2017/5/9.
 */

public class PackMotoPreAuthComp extends BasePackFinancial {
    public PackMotoPreAuthComp(PackListener listener) {
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

            //Optional field
//            setField_63(transData);
            return pack(true);
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    @Override
//    protected int setField_63(TransData transData) throws Iso8583Exception {
//        return setMotoField_63(transData);
//    }

//    @Override
//    public int unpackField_63(TransData transData) {
//        return unpackMotoField_63(transData);
//    }

}
