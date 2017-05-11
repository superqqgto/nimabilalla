/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-28
 * Module Auth: Robert.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.view.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.pax.edc.R;
import com.pax.pay.MainActivity;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.LogUtils;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class AmountWidget extends BaseWidget {

    private static final String TAG = AmountWidget.class.getSimpleName();
    public static final String KEY = "isFromWidget";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        LogUtils.i(TAG, "onReceiver: context : action = " + context +" " + intent.getAction());

        if(intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)){
            Intent newIntent = new Intent(context, MainActivity.class);
            newIntent.putExtra(KEY, true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, newIntent, FLAG_UPDATE_CURRENT);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_amount_layout);
            remoteViews.setTextViewText(R.id.button_amount, CurrencyConverter.convert(0L));
            remoteViews.setOnClickPendingIntent(R.id.button_amount, pendingIntent);
            pushWidgetUpdate(context, remoteViews, AmountWidget.class);
        }
    }

}
