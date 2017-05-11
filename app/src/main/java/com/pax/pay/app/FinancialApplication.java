/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.app;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.pax.appstore.PaxAppStoreCallback;
import com.pax.appstore.PaxAppStoreTool;
import com.pax.edc.R;
import com.pax.manager.AcqManager;
import com.pax.manager.CurActivityManager;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.constant.Constants;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class FinancialApplication extends Application {

    public static final String TAG = FinancialApplication.class.getSimpleName();
    public static FinancialApplication mApp;

    // 应用版本号
    public static String versionName;
    public static int versionCode;

    private Handler handler;
    private ExecutorService backgroundExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
//        FreelineCore.init(this);
        FinancialApplication.mApp = this;
        versionName = ContextUtils.getVersionName();
        versionCode = ContextUtils.getVersionCode();
        CrashHandler.getInstance();

        initManagers();

        handler = new Handler();

        backgroundExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable runnable) {
                Thread thread = new Thread(runnable, "Background executor service");
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setDaemon(true);
                return thread;
            }
        });

        initData();

        registerActivityLifecycleCallbacks(new CurActivityManager.ActyLifeCallbacks());

//        LeakUtils.init(this);
    }

    public void initManagers() {

        //下面这句会自动初始化ControllerSp, GeneralParamSp, SysParamSp
        SpManager.getSysParamSp();
        AcqManager.getInstance();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String countryCode = SpManager.getSysParamSp().get(SysParamSp.EDC_CURRENCY_LIST);
        Utils.changeAppLanguage(mApp, CurrencyConverter.setDefCurrency(countryCode));
    }

    private void initData() {
        runInBackground(new Runnable() {

            @Override
            public void run() {
                initAppstoreTool();
                // 拷贝打印字体
                Utils.install(mApp, Constants.FONT_NAME, Constants.FONT_PATH);
            }
        });
    }

    /**
     * AppstoreTool初始化
     */
    private void initAppstoreTool() {
        PaxAppStoreTool.init(mApp, new PaxAppStoreCallback() {

            @Override
            public void enableUpdateAndUninstall() {
                if (DbManager.getTransDao().countOf() > 0) {
                    PaxAppStoreTool.enableUpdateAndUninstall(mApp, false, mApp
                            .getString(R.string.app_need_update_please_settle));
                    return;
                }
                PaxAppStoreTool.enableUpdateAndUninstall(mApp, true, null);
            }

            @Override
            public void onParamChange() {
                SpManager.getSysParamSp().downloadParamOnline();
            }
        });
    }

    public void runInBackground(final Runnable runnable) {
        backgroundExecutor.submit(runnable);
    }

    public void runOnUiThread(final Runnable runnable) {
        handler.post(runnable);
    }

    public void runOnUiThreadDelay(final Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }
}
