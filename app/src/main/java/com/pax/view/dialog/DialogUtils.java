/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-1
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.view.KeyEvent;

import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.SettleTrans;
import com.pax.pay.utils.ContextUtils;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;

public class DialogUtils {

    /**
     * 提示错误信息
     *
     * @param msg
     * @param listener
     * @param timeout
     */
    public static void showErrMessage(final Context context, final String title, final String msg,
                                      final OnDismissListener listener, final int timeout) {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (context == null) {
                    return;
                }
                CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE, timeout);
                dialog.setTitleText(title);
                dialog.setContentText(msg);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });
                dialog.setOnDismissListener(listener);
                Device.beepErr();
            }
        });
    }

    /**
     * 在非Activity中也可以弹框，实际是获取当前Activity的context
     *
     * @param title
     * @param msg
     * @param listener
     * @param timeout
     */
    public static void showErrMsg(String title, String msg, OnDismissListener listener, int timeout) {
        showErrMessage(ContextUtils.getActyContext(), title, msg, listener, timeout);
    }

    /**
     * 单行提示成功信息
     *
     * @param title
     * @param listener
     * @param timeout
     */
    public static void showSuccMessage(final Context context, final String title,
                                       final OnDismissListener listener, final int timeout) {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (context == null) {
                    return;
                }
                CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.SUCCESS_TYPE, timeout);
                dialog.showContentText(false);
                dialog.setTitleText(ContextUtils.getResources()
                        .getString(R.string.dialog_trans_succ_liff, title));
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });
                dialog.setOnDismissListener(listener);
                Device.beepOk();
            }
        });
    }

    /**
     * 在非Activity中也可以弹框，实际是获取当前Activity的context
     *
     * @param title
     * @param listener
     * @param timeout
     */
    public static void showSuccMsg(String title, OnDismissListener listener, int timeout) {
        showSuccMessage(ContextUtils.getActyContext(), title, listener, timeout);
    }

    /**
     * 退出当前应用
     */
    public static void showExitAppDialog(final Context context) {

        final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
        dialog.setCancelClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
            }
        });
        dialog.setConfirmClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                Device.enableStatusBar(true);
                Device.enableHomeRecentKey(true);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        dialog.show();
        dialog.setNormalText(context.getString(R.string.exit_app));
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);
    }

    /**
     * 应用更新或者参数更新提示，点击确定则进行直接结算
     */
    public static void showUpdateDialog(final Context context, final String prompt) {

        final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
        dialog.setCancelClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
            }
        });
        dialog.setConfirmClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
                new SettleTrans(null).execute();
            }
        });
        dialog.show();
        dialog.setNormalText(prompt);
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);
    }
}
