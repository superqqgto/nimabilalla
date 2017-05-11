/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-26
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeConverter {
    /**
     * @param formattedTime
     * @param oldPattern
     * @param newPattern
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String convert(String formattedTime, final String oldPattern, final String newPattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(oldPattern, Locale.US);
        java.util.Date date;
        try {
            date = sdf.parse(formattedTime);
        } catch (ParseException e) {
            return formattedTime;
        }
        sdf = new SimpleDateFormat(newPattern, Locale.US);
        return sdf.format(date);
    }
}
