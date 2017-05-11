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
package com.pax.pay.constant;

import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Constants {
    /**
     * the period of showing dialog of successful transaction, unit: second
     */
    public final static int SUCCESS_DIALOG_SHOW_TIME = 2;
    /**
     * the period of showing dialog of failed transaction, unit: second
     */
    public final static int FAILED_DIALOG_SHOW_TIME = 3;

    /**
     * Value of cancel key for custom virtual keyboard
     */
    public final static int KEY_EVENT_CANCEL = 65535;
    /**
     * Value of hiding key for custom virtual keyboard
     */
    public final static int KEY_EVENT_HIDE = 65534;

    /**
     * MAX key index
     */
    public final static byte INDEX_TAK = 0x01;
    /**
     * PIN key index
     */
    public static final byte INDEX_TPK = 0x03;
    /**
     * DES key index
     */
    public static final byte INDEX_TDK = 0x05;

    /**
     * SSL cert
     */
    public static final String CACERT_PATH = ContextUtils.getFilesDir() + File.separator + "cacert.pem";

    /**
     * path of default printer font
     */
    public static final String FONT_PATH = ContextUtils.getFilesDir().getAbsolutePath() + File.separator;
    /**
     * name of printer font
     */
    public static final String FONT_NAME = "Roboto-Regular.ttf"; //FIXME use suitable font

    /**
     * date pattern of storage
     */
    public static final String TIME_PATTERN_TRANS = "yyyyMMddHHmmss";
    public static final String TIME_PATTERN_TRANS2 = "yyMMddHHmmss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm";

    /**
     * date pattern of display
     */
    public static final String TIME_PATTERN_DISPLAY = "yyyy/MM/dd HH:mm:ss";

    public static final String TIME_PATTERN_DISPLAY2 = "MMM d, yyyy HH:mm";

    /**
     * max of amount digit
     */
    public static final int AMOUNT_DIGIT = 12;

    public static final String ACQUIRER_NAME = "acquirer_name";

     /**
     * default pattern of pan mask.
     */
    public static final String DEF_PAN_MASK_PATTERN = "(?<=\\d{6})\\d(?=\\d{4})";

    public static final String DEF_ADMIN_PWD = "88888888";
    public static final String DEF_TRANS_PWD = "000000";


    public static final List<TransData.ETransStatus> defFilter = new ArrayList<TransData.ETransStatus>() {{
        add(TransData.ETransStatus.VOIDED);
        add(TransData.ETransStatus.ADJUSTED);
    }};
}
