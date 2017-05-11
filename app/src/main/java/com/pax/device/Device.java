/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.device;

import android.annotation.SuppressLint;

import com.pax.dal.entity.*;
import com.pax.dal.exceptions.PedDevException;
import com.pax.gl.imgprocessing.IImgProcessing.IPage;
import com.pax.manager.neptune.DalManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.GeneralParamSp;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.receipt.IReceiptGenerator;
import com.pax.pay.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class Device {
    /**
     * beep 成功
     */
    public static void beepOk() {
        DalManager.getSys().beep(EBeepMode.FREQUENCE_LEVEL_3, 100);
        DalManager.getSys().beep(EBeepMode.FREQUENCE_LEVEL_4, 100);
        DalManager.getSys().beep(EBeepMode.FREQUENCE_LEVEL_5, 100);
    }

    /**
     * beep 失败
     */
    public static void beepErr() {
        DalManager.getSys().beep(EBeepMode.FREQUENCE_LEVEL_6, 200);
    }

    /**
     * beep 提示音
     */

    public static void beepPrompt() {
        DalManager.getSys().beep(EBeepMode.FREQUENCE_LEVEL_6, 50);
    }

    /**
     * 设置系统时间
     *
     * @param time
     */
    public static boolean setSystemTime(String time) {
        if (isValidDate(time)) {
            DalManager.getSys().setDate(time);
            return true;
        }
        return false;
    }

    @SuppressLint("SimpleDateFormat")
    private static boolean isValidDate(String str) {
        boolean convertSuccess = true;
        SimpleDateFormat format = new SimpleDateFormat(Constants.TIME_PATTERN_TRANS2);
        try {
            format.setLenient(false);
            format.parse(str);
        } catch (ParseException e) {
            convertSuccess = false;
        }
        return convertSuccess;
    }

    /* according to {@param pattern} to get date/time
    *
    * @param pattern
    * @return
    */
    @SuppressLint("SimpleDateFormat")
    public static String getTime(String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(new Date());
    }

    public static Date getMinDate() {
        return new GregorianCalendar(1970, 0, 1).getTime();
    }

    // AET-68 FIXME workaround now, bug from sp
    public static Date getMaxDate() {
        return new GregorianCalendar(2031, 11, 31).getTime();
    }


    public static void enableStatusBar(boolean enable) {
        DalManager.getSys().enableStatusBar(enable);
    }

    public static void enableHomeRecentKey(boolean enable) {
        DalManager.getSys().enableNavigationKey(ENavigationKey.HOME, enable);
        DalManager.getSys().enableNavigationKey(ENavigationKey.RECENT, enable);
    }

    /**
     * write TMK
     *
     * @param tmkIndex
     * @param tmkValue
     * @return
     */
    public static boolean writeTMK(int tmkIndex, byte[] tmkValue) {
        // write TMK
        try {
            DalManager.getPedInternal().writeKey(EPedKeyType.TLK, (byte) 0,
                    EPedKeyType.TMK, (byte) Utils.getMainKeyIndex(tmkIndex),
                    tmkValue, ECheckMode.KCV_NONE, null);
            return true;
        } catch (PedDevException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * write TPK
     *
     * @param tpkValue
     * @param tpkKcv
     */
    public static boolean writeTPK(byte[] tpkValue, byte[] tpkKcv) {
        try {
            int mKeyIndex = Utils.getMainKeyIndex(Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.MK_INDEX)));
            ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
            if (tpkKcv == null || tpkKcv.length == 0) {
                checkMode = ECheckMode.KCV_NONE;
            }
            DalManager.getPedInternal().writeKey(EPedKeyType.TMK, (byte) mKeyIndex,
                    EPedKeyType.TPK, Constants.INDEX_TPK, tpkValue, checkMode, tpkKcv);
            return true;
        } catch (PedDevException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * write tak
     *
     * @param takValue
     * @param takKcv
     */
    public static boolean writeTAK(byte[] takValue, byte[] takKcv) {
        try {
            int mKeyIndex = Utils.getMainKeyIndex(Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.MK_INDEX)));
            ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
            if (takKcv == null || takKcv.length == 0) {
                checkMode = ECheckMode.KCV_NONE;
            }
            DalManager.getPedInternal().writeKey(EPedKeyType.TMK, (byte) mKeyIndex,
                    EPedKeyType.TAK, Constants.INDEX_TAK, takValue, checkMode, takKcv);
            return true;
        } catch (PedDevException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * write tdk
     *
     * @param tdkValue
     * @param tdkKcv
     * @throws PedDevException
     */
    public static void writeTDK(byte[] tdkValue, byte[] tdkKcv) throws PedDevException {
        int mKeyIndex = Utils.getMainKeyIndex(Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.MK_INDEX)));
        ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
        if (tdkKcv == null || tdkKcv.length == 0) {
            checkMode = ECheckMode.KCV_NONE;
        }
        DalManager.getPedInternal().writeKey(EPedKeyType.TMK, (byte) mKeyIndex,
                EPedKeyType.TDK, Constants.INDEX_TDK, tdkValue, checkMode, tdkKcv);
    }

    /**
     * calculate pinblock
     *
     * @param panBlock
     * @return
     * @throws PedDevException
     */
    public static byte[] getPinBlock(String panBlock, boolean supportBypass) throws PedDevException {
        // write TPK
        String tpk = SpManager.getGeneralParamSp().getString(GeneralParamSp.TPK);
        if (tpk != null && tpk.length() > 0) {
            writeTPK(GlManager.strToBcdPaddingRight(tpk), null);
        }
        String pinLen = "4,5,6,7,8,9,10,11,12";
        if (supportBypass) {
            pinLen = "0," + pinLen;
        }
        return DalManager.getPedInternal().getPinBlock(Constants.INDEX_TPK, pinLen, panBlock.getBytes(), EPinBlockMode.ISO9564_0, 60 * 1000);
    }

    /**
     * calculate mac
     *
     * @param data
     * @return
     */
    public static byte[] calcMac(String data) {
        try {
            // write TPK
            String tak = SpManager.getGeneralParamSp().getString(GeneralParamSp.TAK);
            if (tak != null && tak.length() > 0) {
                writeTAK(GlManager.strToBcdPaddingRight(tak), null);
            }
            return DalManager.getPedInternal().getMac(Constants.INDEX_TAK, data.getBytes(), EPedMacMode.MODE_00);
        } catch (PedDevException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * calculate DES
     *
     * @param data
     * @return
     * @throws PedDevException
     */
    public static byte[] calcDes(byte[] data) throws PedDevException {
        // write TPK
        String tdk = SpManager.getGeneralParamSp().getString(GeneralParamSp.TDK);
        if (tdk != null && tdk.length() > 0) {
            writeTDK(GlManager.strToBcdPaddingRight(tdk), null);
        }
        return DalManager.getPedInternal().calcDes(Constants.INDEX_TDK, data, EPedDesMode.ENCRYPT);
    }

    public static IPage generatePage() {
        IPage page = GlManager.getImgProcessing().createPage();
        page.adjustLineSpace(-9);
        page.setTypeFace(IReceiptGenerator.TYPE_FACE);
        return page;
    }

    public static byte[] getMac(byte[] data) {

        String beforeCalcMacData = GlManager.bcdToStr(data);
        byte[] mac = calcMac(beforeCalcMacData);
        return GlManager.bcdToStr(mac).substring(0, 8).getBytes();
    }
}
