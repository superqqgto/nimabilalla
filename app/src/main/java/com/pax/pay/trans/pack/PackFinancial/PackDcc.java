package com.pax.pay.trans.pack.PackFinancial;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.trans.TransResult;
import com.pax.pay.base.DccTransData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.LogUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by zhouhong on 2017/5/1.
 */

public class PackDcc extends BasePackFinancial {
    public PackDcc(PackListener listener) {
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
            setField_14(transData);
            setField_22(transData);
            setField_35(transData);
            setField_49(transData);
            setField_55(transData);

            //Optional field
            setField_52(transData);
            setField_54(transData);
            setField_63(transData);

            return pack(true);
        } catch (Iso8583Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected int setField_63(TransData transData) throws Iso8583Exception {

        DccTransData dccTransData = transData.getDccTransData();
        String dccPartnerTmp;

        //设置tableLen
        byte[] bcdLen = GlManager.strToBcdPaddingLeft("0005");
        String dccBuf_63 = new String(bcdLen);
        LogUtils.d("PackIso8583", " setDccGetBitData_63 bcdLen : " + GlManager.bcdToStr(bcdLen) + ", len: " + dccBuf_63.length());

        //设置tableId
        ETransType origTransType = transData.getOrigTransType();
        switch (origTransType.getMsgType()) {
            case "0100":
                dccBuf_63 += "PS";
                break;
            case "0200":
                dccBuf_63 += "DX";
                break;
            case "0220":
                dccBuf_63 += "EC";
                break;
            default:
                return TransResult.ERR_PACK;
        }

        //设置Indicator
        dccPartnerTmp = dccTransData.getDccPartner();
        LogUtils.d("PackIso8583", "dccPartnerTmp : " + dccPartnerTmp);
        if (dccPartnerTmp != null && dccPartnerTmp.length() > 0) {
            switch (dccPartnerTmp) {
                case "FEXCO":
                    dccBuf_63 += "Y";
                    break;
                case "PC":
                    dccBuf_63 += "2";
                    break;
                case "FINTRAX":
                    dccBuf_63 += "3";
                    break;
                default:
                    return TransResult.ERR_PACK;
            }
        }
        LogUtils.d("PackIso8583", "setDccGetBitData_63 : " + dccBuf_63);
        transData.setField63(dccBuf_63);
        if (transData.getField63() != null) {
            entity.setFieldValue("63", transData.getField63());
        }

        return TransResult.SUCC;
    }

    @Override
    public int unpackField_63(TransData transData) {
        DccTransData dccTransData = transData.getDccTransData();
        String field63 = transData.getField63();

        ArrayList<String> dccTransAmtList = new ArrayList<String>();
        ArrayList<String> dccConvRatetList = new ArrayList<String>();
        ArrayList<String> dccCurrencyList = new ArrayList<String>();
        String tmpStr;
        String tableId;
        String euronetFlag;
        String dccFlag;
        int i;

        //判断63域长度是否合格
        int tableLen = Integer.valueOf(GlManager.bcdToStr(field63.substring(0, 2).getBytes()));
        LogUtils.d("PackIso8583", " tableLen : " + String.valueOf(tableLen));

//        if (tableLen > field63.length()) {
//            return TransResult.ERR_UNPACK;
//        }

        //判断tableId是否合格
        tableId = field63.substring(2, 4);
        LogUtils.d("PackIso8583", "tableId : " + tableId);
        tmpStr = field63.substring(4);
        if (tableId.equals("DC")) {
            dccTransData.setDccRspCode(field63.substring(5, 7));
            LogUtils.d("PackIso8583", "dccRspCode : " + dccTransData.getDccRspCode());

            dccTransAmtList.add(field63.substring(34, 46));
            dccConvRatetList.add(field63.substring(46, 54));
            dccCurrencyList.add(field63.substring(54, 57));

            dccTransData.setDccMargin(field63.substring(57, 64));
            LogUtils.d("PackIso8583", "dccMargin : " + dccTransData.getDccMargin());

            dccTransData.setDccLeg(field63.substring(64, 65));
            LogUtils.d("PackIso8583", "dccLeg : " + dccTransData.getDccLeg());
        }

        if (dccTransData.getDccPartner() != null && dccTransData.getDccPartner().equals("PC") && field63.length() > 64) {
            tmpStr = field63.substring(65);
            dccFlag = tmpStr.substring(0, 1);
            tmpStr = tmpStr.substring(1);
            LogUtils.d("PackIso8583", "dccFlag : " + dccFlag);

            //2 – to indicate “DE” table is appended here.
            if (dccFlag.equals("2")) {
                tableId = tmpStr.substring(0, 2);
                tmpStr = tmpStr.substring(2);
                LogUtils.d("PackIso8583", "tableId : " + tableId);
            } else {
                return TransResult.SUCC;
            }
        }

        if (tableId.equals("DE")) {
            euronetFlag = tmpStr.substring(0, 1);
            LogUtils.d("PackIso8583", "euronetFlag : " + euronetFlag);
            tmpStr = tmpStr.substring(1);

            for (i = 0; i < 6; i++) {
                dccTransAmtList.add(tmpStr.substring(12 * i, 12 * (i + 1)));
            }
            tmpStr = tmpStr.substring(12 * 6);

            for (i = 0; i < 6; i++) {
                dccConvRatetList.add(tmpStr.substring(8 * i, 8 * (i + 1)));
            }
            tmpStr = tmpStr.substring(8 * 6);

            for (i = 0; i < 6; i++) {
                dccCurrencyList.add(tmpStr.substring(3 * i, 3 * (i + 1)));
            }
        }

        dccTransData.setTransAmtList(dccTransAmtList);
        dccTransData.setConvRateList(dccConvRatetList);
        dccTransData.setCurrencyList(dccCurrencyList);

        for (Iterator iter = dccTransData.getTransAmtList().iterator(); iter.hasNext(); ) {
            LogUtils.d("PackIso8583", "dccTransAmtList : " + iter.next());
        }

//        for (Iterator iter = dccTransData.getConvRateList().iterator(); iter.hasNext();) {
//            LogUtils.d("PackIso8583", "dccConvRatetList : " + iter.next());
//        }

        //dcc conversion rate 转换格式
        for (i = 0; i < dccTransData.getConvRateList().size(); i++) {
            String dccConvTmp = dccTransData.getConvRateList().get(i);
            int decimalCnt = Integer.valueOf(dccConvTmp.substring(0, 1));
            if (dccConvTmp.indexOf(".") == -1) {
                dccConvTmp = dccConvTmp.substring(1, 8 - decimalCnt) + "." + dccConvTmp.substring(8 - decimalCnt);
                dccTransData.getConvRateList().set(i, dccConvTmp);
            }
            LogUtils.d("PackIso8583", "dccConvRatetList : " + dccTransData.getConvRateList().get(i));
        }

        for (Iterator iter = dccTransData.getCurrencyList().iterator(); iter.hasNext(); ) {
            LogUtils.d("PackIso8583", "dccCurrencyList : " + iter.next());
        }
        transData.setDccTransData(dccTransData);
        return TransResult.SUCC;
    }
}
