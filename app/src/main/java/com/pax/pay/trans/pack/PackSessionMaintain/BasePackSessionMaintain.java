package com.pax.pay.trans.pack.PackSessionMaintain;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.SessionMaintData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.pack.PackIso8583;
import com.pax.pay.utils.LogUtils;

/**
 * Created by huangmuhua on 2017/4/7.
 * 参考文档Terminal Specification for VMJC V1 6.pdf
 * 第5.5节 Session Maintanance Messages Formates
 */

public abstract class BasePackSessionMaintain extends PackIso8583 {
    private String temp = "";
    public BasePackSessionMaintain(PackListener listener) {
        super(listener);
    }


    /**
     * 设置Session Maintenance Message的共同M条件的域
     * <p> h,m
     * <p> field 3
     * field 4, field 41
     *
     * @param transData
     * @return
     */
    protected int setSessionMaintMandatory(TransData transData) throws Iso8583Exception {
        // h
        String pHeader = transData.getTpdu() + transData.getHeader();
        entity.setFieldValue("h", pHeader);
        // m
        ETransType transType = transData.getTransType();
        if (transData.getReversalStatus() == TransData.ReversalStatus.REVERSAL) {
            entity.setFieldValue("m", transType.getDupMsgType());
        } else {
            entity.setFieldValue("m", transType.getMsgType());
        }

        setField_3(transData);
        setField_24(transData);
        setField_41(transData);

        return TransResult.SUCC;
    }

    @Override
    public int unpackField_62(TransData transData) {

        ETransType transType = transData.getTransType();
        if(transType != ETransType.TMK_DOWNLOAD && transType != ETransType.LOGON) {
            return TransResult.SUCC;
        }

        SessionMaintData sessionMaintData = transData.getSessionMaintData();
        String field_62 = transData.getField62();
        int length = Integer.valueOf(GlManager.bcdToStr(field_62.substring(0, 1).getBytes()));
        LogUtils.d("PackLogon", " [unpackField_62] field_62 : " + field_62);
        LogUtils.d("PackLogon", " [unpackField_62] length : " + length);

        length = 16;    //for test
        if (length > 15) {
            sessionMaintData.setTPK(field_62.substring(1, 9));
            sessionMaintData.setMACKEY(field_62.substring(9, 17));
            LogUtils.d("PackLogon", " [unpackField_62] TPK : " + sessionMaintData.getTPK());
            LogUtils.d("PackLogon", " [unpackField_62] MACKEY : " + sessionMaintData.getMACKEY());
        }

        return TransResult.SUCC;
    }

}
