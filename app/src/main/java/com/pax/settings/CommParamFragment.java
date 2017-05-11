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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.wifi.AccessPoint;
import com.pax.settings.wifi.WifiAdmin;
import com.pax.settings.wifi.WifiAdmin.WifiCipherType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommParamFragment extends PreferenceFragment implements DialogInterface.OnClickListener {
    public static final String TAG = "CommParaFragment";

    // 设置无密码的wifi热点是否显示
    private static final boolean dispNoSecurityWifi = true;
    // wifi
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
    private static WifiAdmin mWifiAdmin = null;
    private CheckBoxPreference mWifiCheckBox;
    private IntentFilter mFilter = null;
    private BroadcastReceiver mReceiver = null;
    private WifiManager mWifiManager;
    private WifiInfo mLastInfo;
    private DetailedState mLastState;
    private Scanner mScanner = null;
    private AccessPoint mSelectedAccessPoint;
    private AtomicBoolean mConnected = new AtomicBoolean(false);
    private boolean wifiConfigIsSave = false;
    private int wifiConfigIndex = -1;

    private static boolean isFirst = true;
    // private final Scanner mScanner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate");

        isFirst = true;
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
        mScanner = new Scanner();
        mWifiAdmin = WifiAdmin.getInstance(getActivity());
        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        setupSimplePreferencesScreen();
        // mEmptyView = (TextView) getView().findViewById(android.R.id.empty);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mFilter);
        updateAccessPoints();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
        mScanner.pause();
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void setupSimplePreferencesScreen() {

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        addPreferencesFromResource(R.xml.comm_para_pref);

        mWifiCheckBox = (CheckBoxPreference) findPreference(SysParamSp.WIFI_ENABLE);
        removeAccessPoint();
        if (mWifiCheckBox.isChecked()) {
            wifiEnable();
        } else {
            wifiDisable();
        }

        bindPreferenceSummaryToValue(findPreference(SysParamSp.APP_COMM_TYPE));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.COMM_TIMEOUT));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.MOBILE_WLTELNO));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.MOBILE_APN));

        bindPreferenceSummaryToValue(findPreference(SysParamSp.MOBILE_USER));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.MOBILE_PWD));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue;
            if (preference instanceof CheckBoxPreference) {

            } else if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                LogUtils.i("CommParamFragment", value.toString());
                stringValue = value.toString();
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                if (!isFirst && DbManager.getTransDao().countOf() > 0) {
                    if (stringValue.equals("DEMO") || stringValue.equals("MOBILE") || stringValue.equals("WIFI")) {
                        ToastUtils.showLong(R.string.has_trans_for_settle);
                        return false;
                    }
                } else {
                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                }
                isFirst = false;
            } else if (preference instanceof RingtonePreference) {

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                stringValue = value.toString();
                String pref_key = preference.getKey();
                boolean flag = false;

                if ((pref_key.equals(SysParamSp.COMM_TIMEOUT))) {
                    int cmpValue = -1;
                    try {
                        cmpValue = Integer.parseInt(stringValue);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        LogUtils.d(TAG, "exception");
                    }
                    if (cmpValue < 1) {
                        flag = true;
                    }
                }

                if (flag) {
                    LogUtils.d(TAG, "pref_key=" + pref_key);
                    LogUtils.d(TAG, stringValue);
                    ToastUtils.showShort(R.string.input_err);
                    return false;
                }
                preference.setSummary(stringValue);
            }

            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        if (preference instanceof CheckBoxPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    SpManager.getSysParamSp().getBoolean(preference.getKey(), false));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    SpManager.getSysParamSp().getString(preference.getKey(), ""));
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mWifiCheckBox) {
            if (mWifiCheckBox.isChecked()) {
                wifiEnable();
            } else {
                wifiDisable();
            }
        } else if (preference instanceof AccessPoint) {
            mSelectedAccessPoint = (AccessPoint) preference;
            if (mSelectedAccessPoint.getSecurity() == AccessPoint.SECURITY_NONE
                    && mSelectedAccessPoint.getNetworkId() == AccessPoint.INVALID_NETWORK_ID) {
                mSelectedAccessPoint.generateOpenNetworkConfig();
                mWifiAdmin.connectConfiguration(mSelectedAccessPoint.getConfig());
            } else {
                showConfigUi(mSelectedAccessPoint, false);
            }
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    private void showConfigUi(AccessPoint accessPoint, boolean edit) {
        showDialog(accessPoint, edit);
    }

    private void showDialog(final AccessPoint accessPoint, boolean edit) {
        Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_wifi_setting, null);
        TextView textTitle = (TextView) view.findViewById(R.id.set_para1_title);
        textTitle.setText(accessPoint.getSsid());
        TextView signalLevel = (TextView) view.findViewById(R.id.signal_level);
        int level = accessPoint.getLevel();
        if (level > 3) {
            signalLevel.setText(getActivity().getResources().getString(R.string.strong));
        } else if (level > 2) {
            signalLevel.setText(getActivity().getResources().getString(R.string.general));
        } else {
            signalLevel.setText(getActivity().getResources().getString(R.string.weak));
        }
        final WifiCipherType mWifiCipherType;
        TextView securityType = (TextView) view.findViewById(R.id.security_type);
        int type = accessPoint.getSecurity();
        if (type == AccessPoint.SECURITY_PSK) {
            securityType.setText(getString(R.string.wifi_security_wpa_wpa2));
            mWifiCipherType = WifiCipherType.WIFICIPHER_WPA;
        } else if (type == AccessPoint.SECURITY_WEP) {
            securityType.setText(getString(R.string.wifi_security_wep));
            mWifiCipherType = WifiCipherType.WIFICIPHER_WEP;
        } else {
            securityType.setText(getString(R.string.wifi_security_none));
            mWifiCipherType = WifiCipherType.WIFICIPHER_NOPASS;
        }

        final EditText password = (EditText) view.findViewById(R.id.password);
        wifiConfigIsSave = false;
        wifiConfigIndex = -1;
        final List<WifiConfiguration> configs = mWifiAdmin.getConfiguration();
        if (configs != null) {
            for (int i = 0; i < configs.size(); i++) {
                String configSsid = configs.get(i).SSID;
                if (configSsid == null)
                    continue;
                configSsid = configSsid.replace("\"", "");
                if (!configSsid.equals(accessPoint.getSsid()))
                    continue;

                wifiConfigIsSave = true;
                wifiConfigIndex = i;
                LinearLayout pwdView = (LinearLayout) view.findViewById(R.id.pwd_view);
                pwdView.setVisibility(View.GONE);

            }
        }
        builder.setView(view);
        if (wifiConfigIsSave) {
            builder.setNeutralButton(getString(R.string.set_cancel_save), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mWifiAdmin.removeNetwork(wifiConfigIndex);
                }
            });
        }
        builder.setPositiveButton(getActivity().getResources().getString(R.string.connect),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        boolean isSuccess = false;

                        if (wifiConfigIsSave) {
                            isSuccess = mWifiAdmin.connectConfiguration(wifiConfigIndex);
                        } else {
                            String ssid = accessPoint.getSsid();
                            if(ssid != null){
                                WifiConfiguration config = mWifiAdmin.createWifiConfiguration(ssid,
                                        password.getText().toString(), mWifiCipherType);
                                isSuccess = mWifiAdmin.addNetwork(config);
                            }
                        }
                        if (!isSuccess) {
                            ToastUtils.showShort(R.string.connect_fail);
                        }
                        //else {
                        // update(getActivity(), position);
                        //}
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            if (!isSuccess) { // 失败时不关闭对话框
                                field.set(dialog, false);
                            } else { // 成功时关闭对话框
                                field.set(dialog, true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        builder.setNegativeButton(getActivity().getResources().getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
    }

    private void wifiEnable() {
        mWifiAdmin.openWifi();
        // updateAccessPoints();
    }

    private void wifiDisable() {
        mWifiAdmin.closeWifi();
    }

    private void updateAccessPoints() {
        final int wifiState = mWifiManager.getWifiState();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                addMessagePreference(R.string.wifi_started);
                mLastInfo = mWifiManager.getConnectionInfo();
                final Collection<AccessPoint> accessPoints = constructAccessPoints();
                for (AccessPoint accessPoint : accessPoints) {
                    getPreferenceScreen().addPreference(accessPoint);
                }
                break;

            case WifiManager.WIFI_STATE_ENABLING:
                addMessagePreference(R.string.wifi_starting);
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                addMessagePreference(R.string.wifi_stopping);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_UNKNOWN:
                addMessagePreference(R.string.wifi_empty_list_wifi_off);
            default:
                break;
        }
    }

    // sort the list
    private List<AccessPoint> constructAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<>();
        /**
         * Lookup table to more quickly update AccessPoints by only considering objects with the correct SSID. Maps SSID
         * -> List of AccessPoints with the given SSID.
         */
        Multimap<String, AccessPoint> apMap = new Multimap<>();

        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                AccessPoint accessPoint = new AccessPoint(getActivity(), config);
                if ((accessPoint.getSecurity() == AccessPoint.SECURITY_NONE) && (!dispNoSecurityWifi)) { // 无密码的过滤掉, 不显示
                    continue;
                }
                accessPoint.update(mLastInfo, mLastState);
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.getSsid(), accessPoint);
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                boolean found = false;
                for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result))
                        found = true;
                }
                if (!found) {
                    AccessPoint accessPoint = new AccessPoint(getActivity(), result);
                    if ((accessPoint.getSecurity() == AccessPoint.SECURITY_NONE) && (!dispNoSecurityWifi)) { // 无密码的过滤掉,
                        // 不显示
                        continue;
                    }
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.getSsid(), accessPoint);
                }
            }
        }

        // Pre-sort accessPoints to speed preference insertion
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        if (accessPoints.size() != 0)
            Collections.sort(accessPoints);
        return accessPoints;
    }

    private class Multimap<K, V> {
        private HashMap<K, List<V>> store = new HashMap<>();

        /**
         * retrieve a non-null list of values with key K
         */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V>emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = store.get(key);
            if (curVals == null) {
                curVals = new ArrayList<>(3);
                store.put(key, curVals);
            }
            curVals.add(val);
        }
    }

    private void addMessagePreference(int messageId) {
        mWifiCheckBox.setSummary(messageId);
        removeAccessPoint();
    }

    private void removeAccessPoint() {
        for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= 0; --i) {
            // Maybe there's a WifiConfigPreference
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof AccessPoint) {
                getPreferenceScreen().removePreference(preference);
            }
        }
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            // Ignore supplicant state changes when network is connected
            // TODO: we should deprecate SUPPLICANT_STATE_CHANGED_ACTION and
            // introduce a broadcast that combines the supplicant and network
            // network state change events so the apps dont have to worry about
            // ignoring supplicant state change when network is connected
            // to get more fine grained information.
            if (!mConnected.get()) {
                updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState) intent
                        .getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
            updateAccessPoints();
            updateConnectionState(info.getDetailedState());
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        }
    }

    private void updateConnectionState(DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }

        if (state == DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }

        for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= 0; --i) {
            // Maybe there's a WifiConfigPreference
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof AccessPoint) {
                final AccessPoint accessPoint = (AccessPoint) preference;
                accessPoint.update(mLastInfo, mLastState);
            }
        }
    }

    private void updateWifiState(int state) {
        getActivity().invalidateOptionsMenu();

        switch (state) {
            case WifiManager.WIFI_STATE_ENABLED:
                mScanner.resume();
                return; // not break, to avoid the call to pause() below

            case WifiManager.WIFI_STATE_ENABLING:
                addMessagePreference(R.string.wifi_starting);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                addMessagePreference(R.string.wifi_empty_list_wifi_off);
                break;
        }

        mLastInfo = null;
        mLastState = null;
        mScanner.pause();
    }

    @SuppressLint("HandlerLeak")
    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        @SuppressWarnings("unused")
        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (mWifiAdmin.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                ToastUtils.showLong(R.string.wifi_fail_to_scan);
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }
}
