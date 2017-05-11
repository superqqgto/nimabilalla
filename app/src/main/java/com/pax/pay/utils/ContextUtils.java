package com.pax.pay.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;

import com.pax.manager.CurActivityManager;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;

import java.io.File;

/**
 * Created by huangmuhua on 2017/3/30.
 */

public class ContextUtils {

    private static Context appContext = FinancialApplication.mApp;
    private static final String defaultStr = "";

    public static String getString(int resId) {
        try {
            return appContext.getString(resId);
        } catch (Resources.NotFoundException nfe) {
            return defaultStr;
        }
    }

    public static Resources getResources() {
        return appContext.getResources();
    }

    public static AssetManager getAssets() {
        return appContext.getAssets();
    }

    public static String[] getStringArray(int resId) {
        return appContext.getResources().getStringArray(resId);
    }

    public static File getFilesDir() {
        return appContext.getFilesDir();
    }

    public static String getVersionName() {

        String packageName = appContext.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }

        try {
            PackageManager pm = appContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getVersionCode() {

        String packageName = appContext.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            return -1;
        }
        try {
            PackageManager pm = appContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? -1 : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static Context getActyContext() {
        Context actyContext = CurActivityManager.getInstance().getCurActivity();
        if (null == actyContext) {
            actyContext = ActivityStack.getInstance().top();
        }
        return actyContext;
    }
}
