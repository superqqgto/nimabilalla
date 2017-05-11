/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-30
 * Module Auth: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.settings.wifi;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;

import com.pax.edc.R;

public class Summary {
    static String get(Context context, String ssid, DetailedState state) {
        String[] formats = context.getResources().getStringArray(R.array.wifi_status);
        int index = state.ordinal();
        //Log.v("", "index=" + index);
        if (index >= formats.length || formats[index].length() == 0) {
            return null;
        }
        //Log.v("", "index1=" + index);
        //Log.v("aaa", "String.format(formats[index-1], ssid)=" + String.format(formats[index - 1], ssid));
        return String.format(formats[index - 1], ssid);
    }

    static String get(Context context, DetailedState state) {
        return get(context, null, state);
    }
}
