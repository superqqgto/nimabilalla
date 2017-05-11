/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-30
 * Module Auth: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.settings.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import java.util.List;

public class WifiAdmin {
    private static WifiAdmin mWifiAdmin = null;
    // WifiManager对象
    private WifiManager mWifiManager;
    // WifiInfo对象
    private WifiInfo mWifiInfo;
    // 网络列表
    private List<ScanResult> mWifiList;
    // 网络配置列表
    private List<WifiConfiguration> mWifiConfiguration;
    // wifi锁
    private WifiLock mWifiLock;

    // wifi加密类型定义
    public enum WifiCipherType {
        WIFICIPHER_WEP,
        WIFICIPHER_WPA,
        WIFICIPHER_NOPASS,
        WIFICIPHER_INVALID
    }

    // 实例化
    public static WifiAdmin getInstance(Context context) {
        if (mWifiAdmin == null) {
            mWifiAdmin = new WifiAdmin(context);
        }

        return mWifiAdmin;
    }

    // 构造方法
    private WifiAdmin(Context context) {
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    // 打开WIFI
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);

        }
    }

    // 关闭WIFI
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
        if (mWifiList != null && mWifiList.size() > 0)
            mWifiList.clear();
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    // 检测wif状态״
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    // 获取WifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 释放WifiLock
    public void releaseWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    // 创建WifiLock
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    // 获取网络配置列表
    public List<WifiConfiguration> getConfiguration() {
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
        return mWifiConfiguration;
    }

    // 指定配置好的网络进行连接
    public boolean connectConfiguration(int index) {
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            return false;
        }
        // 选择配置好的指定ID网络
        return mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
    }

    public boolean connectConfiguration(WifiConfiguration config) {
        return mWifiManager.enableNetwork(config.networkId, true);
    }

    public boolean startScan() {
        boolean ret = false;
        ret = mWifiManager.startScan();
        // 得到扫描结果
        mWifiList = mWifiManager.getScanResults();
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
        return ret;
    }

    // 获取扫描到的网络列表
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }

    // 查看扫描结果
    @SuppressLint("UseValueOf")
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder.append("Index_" + (i + 1) + ":");
            // 将ScanResult信息转换成一个字符串包
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("\n");
        }
        return stringBuilder;
    }

    // 获取当前已连接的wifi信息
    public WifiInfo getConnectionInfo() {
        return mWifiManager.getConnectionInfo();
    }

    // 获取MAC地址ַ
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    // 获取接入点BSSID
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    // 获取IP地址ַ
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    // 获取连接的网络ID
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    // 获取WifiInfo信息包
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    // 计算信号强度
    public int calSignalLevel(int rssi, int numLevels) {
        return WifiManager.calculateSignalLevel(rssi, numLevels);
    }

    // 创建网络配置
    public WifiConfiguration createWifiConfiguration(String ssid, String pwd, WifiCipherType type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = "\"" + ssid + "\"";

        WifiConfiguration tempConfig = this.GetConfig(ssid);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WifiCipherType.WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + pwd + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + pwd + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    private WifiConfiguration GetConfig(String ssid) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + ssid + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    // 添加网络
    public boolean addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        mWifiManager.saveConfiguration();
        return mWifiManager.enableNetwork(wcgID, true);
    }

    // 删除网络
    public boolean removeNetwork(int index) {
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            return false;
        }
        mWifiManager.removeNetwork(mWifiConfiguration.get(index).networkId);
        mWifiManager.saveConfiguration();
        return true;
    }

    // 断开指定ID的网络
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

}
