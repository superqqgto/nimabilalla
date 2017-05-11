/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-26
 * Module Auth: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.utils;

import com.pax.edc.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 平台应答码工具类
 *
 * @author huangmuhua
 */
public class RspCodeUtils {

    /**
     * 将平台应答码映射成相应的提示语
     *
     * @param code
     * @return
     */
    public static String getMsgByCode(final String code) {
        Integer id = codeMsgMap.get(code);
        if (id == null) {
            id = R.string.response_unknown;
        }
        return ContextUtils.getString(id);
    }

    private static Map<String, Integer> codeMsgMap = new HashMap<String, Integer>() {{
        put("00", R.string.response_00);
        put("01", R.string.response_01);
        put("02", R.string.response_02);
        put("03", R.string.response_03);
        put("04", R.string.response_04);
        put("05", R.string.response_05);
        put("06", R.string.response_06);
        put("07", R.string.response_06);
        put("08", R.string.response_08);
        put("09", R.string.response_09);
        put("09", R.string.response_10);
        put("10", R.string.response_11);
        put("12", R.string.response_12);
        put("13", R.string.response_13);
        put("14", R.string.response_14);
        put("15", R.string.response_15);

        put("19", R.string.response_19);
        put("20", R.string.response_20);
        put("21", R.string.response_21);
        put("22", R.string.response_22);
        put("25", R.string.response_25);

        put("30", R.string.response_30);
        put("31", R.string.response_31);
        put("32", R.string.response_32);
        put("33", R.string.response_33);
        put("34", R.string.response_34);
        put("36", R.string.response_36);
        put("38", R.string.response_38);
        put("39", R.string.response_39);


        put("40", R.string.response_40);
        put("41", R.string.response_41);
        put("42", R.string.response_42);
        put("43", R.string.response_43);
        put("45", R.string.response_45);

        put("51", R.string.response_51);
        put("52", R.string.response_52);
        put("53", R.string.response_53);
        put("54", R.string.response_54);
        put("55", R.string.response_55);
        put("56", R.string.response_56);
        put("57", R.string.response_57);
        put("58", R.string.response_58);
        put("59", R.string.response_59);

        put("61", R.string.response_61);
        put("62", R.string.response_62);
        put("63", R.string.response_63);
        put("64", R.string.response_64);
        put("65", R.string.response_65);
        put("68", R.string.response_68);
//        put("60", R.string.response_60);
//        put("76", R.string.response_76);
        put("75", R.string.response_75);
        put("77", R.string.response_77);
        put("78", R.string.response_78);
//
//        put("80", R.string.response_80);
//        put("85", R.string.response_85);
//        put("88", R.string.response_88);
//        put("89", R.string.response_89);

        put("90", R.string.response_90);
        put("91", R.string.response_91);
        put("92", R.string.response_92);
        put("94", R.string.response_94);
        put("95", R.string.response_95);
        put("96", R.string.response_96);
//        put("97", R.string.response_97);
        put("99", R.string.response_99);

        put("D1", R.string.response_D1);
        put("D2", R.string.response_D2);
        put("D8", R.string.response_D8);

        put("N1", R.string.response_N1);
        put("Q1", R.string.response_Q1);
        put("Y1", R.string.response_Y1);
        put("Z1", R.string.response_Z1);

        put("Y2", R.string.response_Y2);
        put("Z2", R.string.response_Z2);

        put("Y3", R.string.response_Y3);
        put("Z3", R.string.response_Z3);

        put("NA", R.string.response_NA);
        put("P0", R.string.response_P0);
        put("XY", R.string.response_XY);
        put("XX", R.string.response_XX);
    }};
}
