/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-13
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.app;

import android.os.Build;
import android.os.Environment;

import com.pax.device.Device;
import com.pax.pay.constant.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//For enable the StatusBar & HomeRecent key
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    private final UncaughtExceptionHandler mHandler;
    //CrashHandler Singleton
    private static final CrashHandler theInstance = new CrashHandler();

    private final DateFormat formatter = new SimpleDateFormat(Constants.TIME_PATTERN_TRANS, Locale.US);


    private CrashHandler() {
        mHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static CrashHandler getInstance() {
        return theInstance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        Device.enableStatusBar(true);
        Device.enableHomeRecentKey(true);

        saveCrashInfo2File(e);

        mHandler.uncaughtException(thread, e);
    }

    /**
     * get device info
     *
     * @param
     */
    private Map<String, String> getDeviceInfo() {
        Map<String, String> infos = new HashMap<>();
        infos.put("versionName", FinancialApplication.versionName);
        infos.put("versionCode", FinancialApplication.versionCode + "");
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return infos;
    }

    /**
     * save error infos to file
     *
     * @param e
     */
    private void saveCrashInfo2File(Throwable e) {

        StringBuffer sb = new StringBuffer();
        Map<String, String> infos = getDeviceInfo();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "crash/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
