/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-27
 * Module Auth: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pax.edc.R;
import com.pax.pay.app.FinancialApplication;

public class ToastUtils {

    private static Context context;
    private static Toast toast;
    private static TextView textView;

    private static String lastMsg="";//message of last toast
    private static long lastToastTime = 0;//time millis of last toast

    static {
        context = FinancialApplication.mApp;
        View rootView = View.inflate(context, R.layout.toast_layout, null);
        textView = (TextView) rootView.findViewById(R.id.message);
        toast = new Toast(context);
        toast.setView(rootView);
        toast.setGravity(Gravity.CENTER, 0, 0);// set gravity center
    }


    public static void showMessage(String msg, int duration) {

        if (null == toast || null == textView || TextUtils.isEmpty(msg)) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        //如果内容相同的两次Toast时间间隔太小，则第二个不弹出来
        if (lastMsg.equals(msg) && currentTime - lastToastTime < Toast.LENGTH_SHORT) {
            return;
        }

        //正常弹出
        textView.setText(msg);
        toast.setDuration(duration);
        toast.show();

        //将本次Toast的时间和文本保存起来
        lastToastTime = currentTime;
        lastMsg = msg;
    }

    public static void showShort(String msg) {
        showMessage(msg, Toast.LENGTH_SHORT);
    }

    public static void showShort(int msgResId) {
        showMessage(context.getString(msgResId), Toast.LENGTH_SHORT);
    }

    public static void showLong(String msg) {
        showMessage(msg, Toast.LENGTH_LONG);
    }

    public static void showLong(int msgResId) {
        showMessage(context.getString(msgResId), Toast.LENGTH_LONG);
    }
}
