/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-30
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.pax.dal.exceptions.PedDevException;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.DalManager;
import com.pax.manager.sp.ControllerSp;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.ToastUtils;

import java.util.Set;

//import static com.pax.manager.sp.SysParamSp.DCC_PARTNER;

public class OtherManageFragment extends PreferenceFragment {
    public static final String TAG = "OtherManageFragment";

    private static Context context = null;
    private static String title = "";
    private static String prompt = "";
    private static String buttonName = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate");
        context = getActivity();
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.other_manage_pref);

        bindPreferenceSummaryToValue(findPreference(SysParamSp.OTHER_CLEAR_FUNC));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.OTHER_DOWNLOAD_FUNC));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.OTHER_PRINT_PARAM));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.SELECT_DCC_PARTNER));//Zac
    }

    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            if (preference.getKey().equals(SysParamSp.OTHER_CLEAR_FUNC)) { // 清除功能
                clearFunc(value);
            } else if (preference.getKey().equals(SysParamSp.OTHER_DOWNLOAD_FUNC)) { // 下载功能
                downloadFunc(value);
            } else if (preference.getKey().equals(SysParamSp.OTHER_PRINT_PARAM)) { // 参数打印
                paraPrint(value);
            } else if (preference.getKey().equals(SysParamSp.SELECT_DCC_PARTNER)) { // 选择DCC的partner
                choosepartner(value);
            }

            return true;
        }
    };

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
    }

    private static void clearFunc(Object value) {
        //noinspection unchecked
        Set<String> clearSet = (Set) value;
        boolean canBeep = false;
        if (clearSet.contains(context.getResources().getString(R.string.om_clearTrade_menu_reversal))) {
            canBeep = true;
            DbManager.getTransDao().deleteDupRecord();
        }
        if (clearSet.contains(context.getResources().getString(R.string.om_clearTrade_menu_trade_voucher))) {
            canBeep = true;
            SpManager.getControlSp().putInt(ControllerSp.BATCH_UP_STATUS, ControllerSp.Constant.WORKED);
            DbManager.getTransDao().deleteAllTransData();
            SpManager.getControlSp().putInt(ControllerSp.CLEAR_LOG, ControllerSp.Constant.NO);
        }
        if (clearSet.contains(context.getResources().getString(R.string.om_clearTrade_menu_black_list))) {
            canBeep = true;
            DbManager.getCardBinDao().deleteAllBlack();
        }
        if (clearSet.contains(context.getResources().getString(R.string.om_clearTrade_menu_key))) {
            canBeep = true;
            try {
                DalManager.getPedInternal().erase();
            } catch (PedDevException e) {
                e.printStackTrace();
            }
        }

        if (canBeep) {
            Device.beepOk();
        }

    }

    private void downloadFunc(final Object value) {
        FinancialApplication.mApp.runInBackground(new Runnable() {

            @Override
            public void run() {
                int ret = -1;
                String stringValue = value.toString();
                TransProcessListenerImpl listenerImpl = new TransProcessListenerImpl(context);

                if (stringValue.equals(ContextUtils.getString(R.string.om_download_menu_echo_test))) {
                    ret = TransOnline.echo(listenerImpl);
                } else if (stringValue.equals(ContextUtils.getString(R.string.om_download_tmk))) {
                    ret = TransOnline.downloadTmk(listenerImpl);
                }

                listenerImpl.onHideProgress();

                if (ret == TransResult.SUCC) {
                    Device.beepOk();
                } else if (ret == TransResult.ERR_ABORTED || ret == TransResult.ERR_HOST_REJECT) {
                    // ERR_ABORTED AND ERR_HOST_REJECT 之前已提示错误信息， 此处不需要再提示
                } else {
                    listenerImpl.onShowErrMessageWithConfirm(TransResult.getMessage(ret),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                }

            }
        });

    }

    /**
     * 显示dialog, state只能succ为0, error为1;
     *
     * @param value
     * @param state
     */
    public void showDialog(String value, int state) {

    }

    private void paraPrint(final Object value) {

    }
//choose dcc partner
    private static void choosepartner(final Object value) {
        String stringValue = value.toString();
        if (stringValue.equals(context.getResources().getString(R.string.om_dcc_partner_FEXCO))) {
            LogUtils.i("Zac", "FEXCO");
            SpManager.getSysParamSp().set(SysParamSp.DCC_PARTNER, ContextUtils.getString(R.string.om_dcc_partner_FEXCO));
            ToastUtils.showShort(ContextUtils.getString(R.string.om_dcc_partner_FEXCO));
            LogUtils.i("Zac", SpManager.getSysParamSp().get(SysParamSp.DCC_PARTNER));
        }
        else if(stringValue.equals(context.getResources().getString(R.string.om_dcc_partner_PC))){
            LogUtils.i("Zac", "PC");
            SpManager.getSysParamSp().set(SysParamSp.DCC_PARTNER,"PC");
            ToastUtils.showShort(ContextUtils.getString(R.string.om_dcc_partner_PC));
            LogUtils.i("Zac",SpManager.getSysParamSp().get(SysParamSp.DCC_PARTNER));
        }else{
            LogUtils.i("Zac", "FINTRAX");
            SpManager.getSysParamSp().set(SysParamSp.DCC_PARTNER,"FINTRAX");
            ToastUtils.showShort(ContextUtils.getString(R.string.om_dcc_partner_FINTRAX));
            LogUtils.i("Zac", SpManager.getSysParamSp().get(SysParamSp.DCC_PARTNER));
        }

    }
}
