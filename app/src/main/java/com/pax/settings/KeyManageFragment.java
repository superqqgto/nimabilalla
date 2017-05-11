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

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.pax.dal.exceptions.PedDevException;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.manager.sp.SpManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;


public class KeyManageFragment extends PreferenceFragment {
    public static final String TAG = "KeyManageFragment";
    private static List<String> options = new ArrayList<String>() {{
        add(SysParamSp.MK_INDEX);
        add(SysParamSp.KEY_ALGORITHM);
    }};
    private static int MAX_CNT = options.size();
    private static int cnt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate");
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.key_manage_pref);

        options.add(SysParamSp.MK_INDEX_MANUAL);
        options.add(SysParamSp.MK_VALUE);
        options.add(SysParamSp.PK_VALUE);
        options.add(SysParamSp.AK_VALUE);
        MAX_CNT = options.size();

        for (String i : options) {
            bindPreferenceSummaryToValue(findPreference(i));
        }

    }

    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            String key = preference.getKey();

            LogUtils.i("TAG", "KEY:" + key);
            if (preference instanceof ListPreference) {
                LogUtils.i("KeyManageFragment", value.toString());
                stringValue = value.toString();
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            }
            if (SysParamSp.MK_VALUE.equals(key)) {
                String index = SpManager.getSysParamSp().getString(SysParamSp.MK_INDEX_MANUAL, null);
                if (cnt >= MAX_CNT) {
                    if (index == null || index.length() == 0) {
                        ToastUtils.showShort(R.string.keyManage_menu_index_err);
                    } else if ((Integer.parseInt(index) < 0) || (Integer.parseInt(index) >= 100)) {
                        ToastUtils.showShort(R.string.keyManage_menu_tmk_index_no);
                    } else {
                        if (stringValue.length() != 16) {
                            ToastUtils.showShort(R.string.input_len_err);
                        } else {
                            // 写主密钥
                            if (Device.writeTMK((byte) Integer.parseInt(index),
                                    GlManager.strToBcdPaddingLeft(stringValue))) {
                                Device.beepOk();
                                ToastUtils.showShort(R.string.set_key_success);
                            } else {
                                Device.beepErr();
                                ToastUtils.showShort(R.string.set_key_fail);
                            }
                        }
                    }
                }

//                SpManager.getSysParamSp().remove(ContextUtils.getString(R.string.keyManage_menu_key_value));
                return false;
            } else if (SysParamSp.PK_VALUE.equals(key)) {
                if (cnt >= MAX_CNT) {
                    if (stringValue.length() != 16) {
                        ToastUtils.showShort(R.string.input_len_err);
                    } else {
                        if (Device.writeTPK(GlManager.strToBcdPaddingLeft(stringValue), null)) {
                            Device.beepOk();
                            ToastUtils.showShort(R.string.set_key_success);
                        } else {
                            Device.beepErr();
                            ToastUtils.showShort(R.string.set_key_fail);
                        }
                    }
                }

//                SpManager.getSysParamSp().remove(ContextUtils.getString(R.string.keyManage_menu_key_value));
                return false;
            } else if (SysParamSp.AK_VALUE.equals(key)) {
                if (cnt >= MAX_CNT) {
                    if (stringValue.length() != 16) {
                        ToastUtils.showShort(R.string.input_len_err);
                    } else {
                        if (Device.writeTAK(GlManager.strToBcdPaddingLeft(stringValue), null)) {
                            Device.beepOk();
                            ToastUtils.showShort(R.string.set_key_success);
                        } else {
                            Device.beepErr();
                            ToastUtils.showShort(R.string.set_key_fail);
                        }
                    }
                }

//                SpManager.getSysParamSp().remove(ContextUtils.getString(R.string.keyManage_menu_key_value));
                return false;
            } else if (key.equals(SysParamSp.MK_INDEX)
                    || key.equals(SysParamSp.MK_INDEX_MANUAL)) {
                boolean flag = false;

                if (Integer.valueOf(stringValue) < 0 || Integer.valueOf(stringValue) >= 100) {
                    flag = true;
                } else {
                    preference.setSummary(value.toString());
                }

                if (flag) {
                    ToastUtils.showShort(R.string.input_len_err);
                    return false;
                }

            } else {
                ++cnt;
                preference.setSummary(value.toString());
            }

            return true;
        }
    };

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                SpManager.getSysParamSp().getString(preference.getKey(), "0"));
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume");
        cnt = MAX_CNT;// 在调用前,监听器已经被调用了5次,故cnt不用赋值
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtils.d(TAG, "onPause");
        cnt = 0;
    }
}
