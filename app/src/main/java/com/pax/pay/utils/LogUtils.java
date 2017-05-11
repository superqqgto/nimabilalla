package com.pax.pay.utils;

import android.text.TextUtils;
import android.util.Log;

import com.pax.edc.BuildConfig;

/**
 * Created by huangmuhua on 2017/4/26.
 */

public class LogUtils {

    private static final boolean enabled = BuildConfig.LOG_ENABLE;
    private static final String defTag = "BASE24_EDC";//默认TAG

    public static void v(String tag, String msg) {
        if (!enabled) {
            return;
        }
        if (TextUtils.isEmpty(tag)) {
            tag = defTag;
        }
        Log.v(tag, msg);
    }

    public static void v(String msg) {
        v(defTag, msg);
    }

    public static void d(String tag, String msg) {
        if (!enabled) {
            return;
        }
        if (TextUtils.isEmpty(tag)) {
            tag = defTag;
        }
        Log.d(tag, msg);
    }

    public static void d(String msg) {
        Log.d(defTag, msg);
    }

    public static void i(String tag, String msg) {
        if (!enabled) {
            return;
        }
        if (TextUtils.isEmpty(tag)) {
            tag = defTag;
        }
        Log.i(tag, msg);
    }

    public static void i(String msg) {
        Log.i(defTag, msg);
    }

    public static void w(String tag, String msg) {
        if (!enabled) {
            return;
        }
        if (TextUtils.isEmpty(tag)) {
            tag = defTag;
        }
        Log.w(tag, msg);
    }

    public static void w(String msg) {
        Log.w(defTag, msg);
    }

    public static void e(String tag, String msg) {
        if (!enabled) {
            return;
        }
        if (TextUtils.isEmpty(tag)) {
            tag = defTag;
        }
        Log.e(tag, msg);
    }

    public static void e(String msg) {
        Log.e(defTag, msg);
    }
}
