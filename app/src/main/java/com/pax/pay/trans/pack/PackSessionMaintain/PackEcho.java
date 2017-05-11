package com.pax.pay.trans.pack.PackSessionMaintain;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

/**
 * Created by zhouhong on 2017/5/2.
 */

public class PackEcho extends BasePackSessionMaintain {
    public PackEcho(PackListener listener) {
        super(listener);
    }

    @Override
    public byte[] pack(TransData transData) {
        if (transData == null) {
            return null;
        }
        try {
            //Echo commmon Mandatory Fields
            setSessionMaintMandatory(transData);

            //Echo unique Mandatory Fields
            setField_42(transData);

            return pack(true);
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
