/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-9
 * Module Auth: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.view.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public abstract class BaseWidget extends AppWidgetProvider {

    public static void pushWidgetUpdate(Context context, RemoteViews rv, Class<?> clz) {
        ComponentName myWidget = new ComponentName(context, clz);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, rv);
    }

    public static void updateWidget(Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        if(context != null)
            context.sendBroadcast(intent);
    }
}
