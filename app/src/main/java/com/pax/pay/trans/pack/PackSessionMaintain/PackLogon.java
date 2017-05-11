package com.pax.pay.trans.pack.PackSessionMaintain;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.SessionMaintData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.LogUtils;

/**
 * Created by zhouhong on 2017/4/28.
 */

public class PackLogon extends BasePackSessionMaintain {
    public PackLogon(PackListener listener) {
        super(listener);
    }

    @Override
    public byte[] pack(TransData transData) {
        if (transData == null) {
            return null;
        }
        try {
            //LOGON commmon Mandatory Fields
            setSessionMaintMandatory(transData);

            //LOGON unique Mandatory Fields
            setField_11(transData);
            setField_42(transData);

            return pack(true);
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int unpackField_63(TransData transData) {
        SessionMaintData sessionMaintData = transData.getSessionMaintData();
        String field_63 = transData.getField63();
        String tableId = "";

        int length = Integer.valueOf(GlManager.bcdToStr(field_63.substring(0, 1).getBytes()));
        LogUtils.d("PackLogon", " [unpackField_63] field_63 : " + field_63);
        LogUtils.d("PackLogon", " [unpackField_63] length : " + length);

        length = 34;    //for test
        tableId = field_63.substring(1, 3);
        if (!tableId.equals("KP")) {
            return TransResult.ERR_UNPACK;
        }

        if (length > 17) {
            sessionMaintData.setDoubleLenTPK1(field_63.substring(3, 11));
            sessionMaintData.setDoubleLenTPK2(field_63.substring(11, 19));
            LogUtils.d("PackLogon", "[unpackField_63] doubleLenTPK1 : " + sessionMaintData.getDoubleLenTPK1());
            LogUtils.d("PackLogon", "[unpackField_63] doubleLenTPK2 : " + sessionMaintData.getDoubleLenTPK2());
        }

        if (length > 33) {
            sessionMaintData.setTLE(field_63.substring(19, 35));
            LogUtils.d("PackLogon", "[unpackField_63] TLE : " + sessionMaintData.getTLE());
        }

        return TransResult.SUCC;
    }
}
