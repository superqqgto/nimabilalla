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
package com.pax.abl.utils;

public class PanUtils {
    public enum EPanMode {
        X9_8_WITH_PAN,
        X9_8_NO_PAN,
    }

    /**
     * 按各个银行要求处理卡号
     *
     * @param pan
     * @param mode
     * @return
     */
    public static String getPanBlock(String pan, EPanMode mode) {
        String panBlock = null;
        if (pan == null || pan.length() < 13 || pan.length() > 19) {
            return null;
        }
        switch (mode) {
            case X9_8_WITH_PAN:
                panBlock = "0000" + pan.substring(pan.length() - 13, pan.length() - 1);
                break;
            case X9_8_NO_PAN:
                panBlock = "0000000000000000";
                break;

            default:
                break;
        }

        return panBlock;
    }

    /**
     * 空格分隔卡号
     *
     * @param cardNo
     * @return
     */
    public static String separateWithSpace(String cardNo) {
        if (cardNo == null)
            return null;

        String temp = "";
        int total = cardNo.length() / 4;
        for (int i = 0; i < total; i++) {
            temp += cardNo.substring(i * 4, i * 4 + 4);
            if (i != (total - 1)) {
                temp += " ";
            }
        }
        if (total * 4 < cardNo.length()) {
            temp += " " + cardNo.substring(total * 4, cardNo.length());
        }

        return temp;
    }

    /**
     * mask card no using specific pattern
     *
     * @param cardNo
     * @param pattern it's a regular expression, def{@link com.pax.pay.constant.Constants#DEF_PAN_MASK_PATTERN}
     * @return
     */
    public static String maskCardNo(String cardNo, String pattern) {
        if (cardNo == null || cardNo.length() == 0 || pattern == null || pattern.length() == 0)
            return cardNo;

        return cardNo.replaceAll(pattern, "*");
    }
}
