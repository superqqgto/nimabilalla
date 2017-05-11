/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-14
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.utils;

import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;

public class EmailInfo {
    private String hostName;
    private int port;
    private String userName;
    private String password;
    private boolean isSsl;
    private int sslPort;
    private String from;

    private EmailInfo() {

    }

    private EmailInfo(String hostName, int port, String userName, String password, boolean isSsl, int sslPort, String from) {
        this.setHostName(hostName);
        this.setPort(port);
        this.setUserName(userName);
        this.setPassword(password);
        this.setSsl(isSsl);
        this.setSslPort(sslPort);
        this.setFrom(from);
    }

    public static EmailInfo generateSmtpInfo() {
        SysParamSp sysParam = SpManager.getSysParamSp();

        EmailInfo info = new EmailInfo();
        info.setHostName(sysParam.get(SysParamSp.EDC_SMTP_HOST));
        info.setPort(Integer.parseInt(sysParam.get(SysParamSp.EDC_SMTP_PORT)));
        info.setUserName(sysParam.get(SysParamSp.EDC_SMTP_HOST));
        info.setPassword(sysParam.get(SysParamSp.EDC_SMTP_HOST));
        info.setSsl(sysParam.get(SysParamSp.EDC_SMTP_ENABLE_SSL).equals(SysParamSp.Constant.YES));
        info.setSslPort(Integer.parseInt(sysParam.get(SysParamSp.EDC_SMTP_SSL_PORT)));
        info.setFrom(sysParam.get(SysParamSp.EDC_SMTP_FROM));

        return info;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsl() {
        return isSsl;
    }

    public void setSsl(boolean ssl) {
        isSsl = ssl;
    }

    public int getSslPort() {
        return sslPort;
    }

    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
