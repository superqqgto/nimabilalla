/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-20
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.base;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "acquirer")
public class Acquirer implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String ID_FIELD_NAME = "acquirer_id";
    public final static String NAME_FIELD_NAME = "acquirer_name";

    /**
     * id
     */
    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private int id;

    /**
     * name
     */
    @DatabaseField(unique = true, columnName = NAME_FIELD_NAME)
    private String name;

    @DatabaseField(canBeNull = false)
    private String nii;

    @DatabaseField(canBeNull = false)
    private String terminalId;

    @DatabaseField(canBeNull = false)
    private String merchantId;

    @DatabaseField
    private int currBatchNo;

    @DatabaseField
    private String ip;

    @DatabaseField
    private int port;

    @DatabaseField
    private String ipBak1;

    @DatabaseField
    private short portBak1;

    @DatabaseField
    private String ipBak2;

    @DatabaseField
    private short portBak2;

    @DatabaseField
    private int tcpTimeOut;

    @DatabaseField
    private int wirelessTimeOut;

    @DatabaseField
    private boolean isDisableTrickFeed = false;

    public Acquirer() {
    }

    public Acquirer(String name) {
        this.setName(name);
    }

    public Acquirer(int id, String acquirerName) {
        this.setId(id);
        this.setName(acquirerName);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public int getCurrBatchNo() {
        return currBatchNo;
    }

    public void setCurrBatchNo(int currBatchNo) {
        this.currBatchNo = currBatchNo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIpBak1() {
        return ipBak1;
    }

    public void setIpBak1(String ipBak1) {
        this.ipBak1 = ipBak1;
    }

    public short getPortBak1() {
        return portBak1;
    }

    public void setPortBak1(short portBak1) {
        this.portBak1 = portBak1;
    }

    public String getIpBak2() {
        return ipBak2;
    }

    public void setIpBak2(String ipBak2) {
        this.ipBak2 = ipBak2;
    }

    public short getPortBak2() {
        return portBak2;
    }

    public void setPortBak2(short portBak2) {
        this.portBak2 = portBak2;
    }

    public int getTcpTimeOut() {
        return tcpTimeOut;
    }

    public void setTcpTimeOut(int tcpTimeOut) {
        this.tcpTimeOut = tcpTimeOut;
    }

    public int getWirelessTimeOut() {
        return wirelessTimeOut;
    }

    public void setWirelessTimeOut(int wirelessTimeOut) {
        this.wirelessTimeOut = wirelessTimeOut;
    }

    public boolean isDisableTrickFeed() {
        return isDisableTrickFeed;
    }

    public void setDisableTrickFeed(boolean disableTrickFeed) {
        isDisableTrickFeed = disableTrickFeed;
    }
}
