/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-8
 * Module Auth: huangwp
 * Description:
 *
 * ============================================================================
 */
package com.pax.view.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.pax.edc.R;
import com.pax.pay.WebViewActivity;
import com.pax.pay.constant.AdConstants;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

public class AdWidget extends BaseWidget {

    public final String ACTION_UPDATE_ALL = "com.pax.pay.AdWidget.UPDATE_ALL";
    private static final String TAG = "AdWidget";

    private AppWidgetTarget appWidgetTarget;

    private final long UPDATE_PERIOD = 5*1000;     //5s
    private Intent timerIntent;
    private static PendingIntent pi;
    private static AlarmManager am;

    private static List<String> mapKeyList = new ArrayList<>(AdConstants.ad.keySet());
    private static List<String> mapValuesList  = new ArrayList<>(AdConstants.ad.values());

    private static int currentPage = 0;
    private static int totalPage = mapKeyList.size();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if(intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)){
            timerIntent = new Intent(ACTION_UPDATE_ALL);
            pi = PendingIntent.getBroadcast(context,0, timerIntent,0);
            am = (AlarmManager)context.getSystemService(ALARM_SERVICE);
            am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), UPDATE_PERIOD,pi);
        }

        if(intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)){
            if((am != null) && (pi != null)){
                am.cancel(pi);
            }
        }

        if(intent.getAction().equals(ACTION_UPDATE_ALL)){
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_ad);
            appWidgetTarget = new AppWidgetTarget( context, rv, R.id.iv, new ComponentName(context, AdWidget.class) );
               Glide.with(context.getApplicationContext()) // safer!
                    .load(mapKeyList.get(currentPage))
                    .asBitmap()
                    .into(appWidgetTarget);
            pushWidgetUpdate(context, rv, AdWidget.class);

            Intent webIntent =  new Intent(context, WebViewActivity.class);
            webIntent.putExtra(WebViewActivity.KEY, mapValuesList.get(currentPage));
            webIntent.putExtra(WebViewActivity.IS_FROM_WIDGET, true);
            PendingIntent webPendingIntent = PendingIntent.getActivity(context, 0, webIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id. iv, webPendingIntent);
            pushWidgetUpdate(context,rv, AdWidget.class);
            currentPage++;
            if(currentPage >= totalPage){
                currentPage = 0;
            }
        }
    }
}
