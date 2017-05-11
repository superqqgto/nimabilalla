/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-27
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.pax.pay.UnlockTerminalActivity;

// not used
public class LockService extends Service {
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        IntentFilter mScreenOnFilter = new IntentFilter();
        mScreenOnFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenOnFilter.addAction(Intent.ACTION_SCREEN_ON);
        LockService.this.registerReceiver(mScreenActionReceiver, mScreenOnFilter);
    }

    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mScreenActionReceiver);
    }

    private BroadcastReceiver mScreenActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                Intent lockIntent = new Intent(LockService.this, UnlockTerminalActivity.class);
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(lockIntent);
            }
        }
    };
}
