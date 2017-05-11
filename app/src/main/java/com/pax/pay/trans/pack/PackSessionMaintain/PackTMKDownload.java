package com.pax.pay.trans.pack.PackSessionMaintain;

import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.SessionMaintData;
import com.pax.pay.utils.LogUtils;

/**
 * Created by zhouhong on 2017/4/26.
 */

public class PackTMKDownload extends BasePackSessionMaintain {

    public PackTMKDownload(PackListener listener) {
        super(listener);
    }

    @Override
    public byte[] pack(TransData transData) {
        if (transData == null) {
            return null;
        }
        try {
            //TMK Download类交易的共同Mandatory域
            setSessionMaintMandatory(transData);
            //TMK Download类交易的特有的Mandatory域
            setField_11(transData);
            setField_63(transData);

            return pack(true);
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected int setField_63(TransData transData) throws Iso8583Exception {
        String field_63 = "";
        String noOfTerm = "01";
        String terminalId = transData.getAcquirer().getTerminalId();

        byte[] bcdLen = GlManager.strToBcdPaddingLeft("10");
        field_63 = new String(bcdLen);

        if(!TextUtils.isEmpty(terminalId)) {
            field_63 += noOfTerm + terminalId;
            LogUtils.d("PackTMKDownload", " [setField_63] pack field_63 : " + field_63);
            entity.setFieldValue("63", field_63);
        }

        return TransResult.SUCC;
    }

    @Override
    public int unpackField_63(TransData transData) {
        SessionMaintData sessionMaintData = transData.getSessionMaintData();
        String field_63 = transData.getField63();

        int length = Integer.valueOf(GlManager.bcdToStr(field_63.substring(0, 1).getBytes()));
        LogUtils.d("PackTMKDownload", " [unpackField_63] field_63 : " + field_63);
        LogUtils.d("PackTMKDownload", " [unpackField_63] length : " + length);

        length = 32;    //for test
        if (length > 15) {
            sessionMaintData.setTMK(field_63.substring(1, 17));
            LogUtils.d("PackTMKDownload", "[unpackField_63] TMK : " + sessionMaintData.getTMK());
        }

        if (length > 31) {
            sessionMaintData.setTLE(field_63.substring(17, 33));
            LogUtils.d("PackTMKDownload", "[unpackField_63] TLE : " + sessionMaintData.getTLE());
        }

        return TransResult.SUCC;
    }
}
