/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-27
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.pax.pay.app.FinancialApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static android.content.Context.TELEPHONY_SERVICE;

public class Utils {

    /**
     * 得到设备屏幕的宽度
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 得到设备屏幕的高度
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 得到设备的密度
     */
    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 把密度转换为像素
     */
    public static int dip2px(Context context, float px) {
        final float scale = getScreenDensity(context);
        return (int) (px * scale + 0.5);
    }

    public static void install(Context context, String name, String path) {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = context.getAssets().open(name);
            File file = new File(path + name);
            out = new FileOutputStream(file);
            int count = 0;
            byte[] tmp = new byte[1024];
            while ((count = in.read(tmp)) != -1) {
                out.write(tmp, 0, count);
            }
            Runtime.getRuntime().exec("chmod 777 " + path + name);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取主秘钥索引
     *
     * @param index 0~99的主秘钥索引值
     * @return 1~100的主秘钥索引值
     */
    public static int getMainKeyIndex(int index) {
        return index + 1;
    }

    public static boolean checkIp(String ip) {
        return ip.matches("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)");
    }

    public static void changeAppLanguage(Context context, Locale locale) {
        if (context == null) {
            return;
        }
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }

    public static boolean isSimReady(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return manager.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void wakeupScreen(int timeout){
        PowerManager pm=(PowerManager) FinancialApplication.mApp.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
        wl.acquire();
        FinancialApplication.mApp.runOnUiThreadDelay(new Runnable() {
            @Override
            public void run() {
                wl.release();
            }
        }, timeout * 1000);
    }

    public static void hideSystemKeyboard(Context context, View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
