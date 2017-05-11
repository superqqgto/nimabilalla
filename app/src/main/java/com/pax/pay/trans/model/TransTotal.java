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
package com.pax.pay.trans.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.pax.pay.base.Acquirer;

import java.io.Serializable;

/**
 * 交易总计
 *
 * @author Steven.W
 */

@DatabaseTable(tableName = "trans_total")
public class TransTotal implements Serializable {
    private static final long serialVersionUID = 1L;

    public final static String ID_FIELD_NAME = "id";
    public final static String IS_CLOSED_FIELD_NAME = "closed";
    public final static String MID_FIELD_NAME = "mid";
    public final static String TID_FIELD_NAME = "tid";
    public final static String BATCHNO_FIELD_NAME = "batch_no";
    public final static String TIME_FIELD_NAME = "batch_time";

    public final static String SALE_AMOUNT = "SALE_AMOUNT";
    public final static String SALE_NUM = "SALE_NUM";
    public final static String VOID_AMOUNT = "VOID_AMOUNT";
    public final static String VOID_NUM = "VOID_NUM";
    public final static String REFUND_AMOUNT = "REFUND_AMOUNT";
    public final static String REFUND_NUM = "REFUND_NUM";
    public final static String REFUND_VOID_AMOUNT = "REFUND_VOID_AMOUNT";
    public final static String REFUND_VOID_NUM = "REFUND_VOID_NUM";
    public final static String SALE_VOID_AMOUNT = "SALE_VOID_AMOUNT";
    public final static String SALE_VOID_NUM = "SALE_VOID_NUM";
    public final static String AUTH_AMOUNT = "AUTH_AMOUNT";
    public final static String AUTH_NUM = "AUTH_NUM";
    public final static String OFFLINE_AMOUNT = "OFFLINE_AMOUNT";
    public final static String OFFLINE_NUM = "OFFLINE_NUM";


    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    protected int id;

    /**
     * 商户号
     */
    @DatabaseField(columnName = MID_FIELD_NAME)
    private String merchantID;
    /**
     * 终端号
     */
    @DatabaseField(columnName = TID_FIELD_NAME)
    private String terminalID;
    /**
     * 批次号
     */
    @DatabaseField(columnName = BATCHNO_FIELD_NAME)
    private int batchNo;

    /**
     * 日期时间
     */
    @DatabaseField(columnName = TIME_FIELD_NAME)
    private String dateTime;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = Acquirer.ID_FIELD_NAME)
    private Acquirer acquirer;

    @DatabaseField(columnName = IS_CLOSED_FIELD_NAME)
    private boolean isClosed;

    /**
     * 消费总金额
     */
    @DatabaseField(columnName = SALE_AMOUNT)
    private long saleTotalAmt;
    /**
     * 消费总笔数
     */
    @DatabaseField(columnName = SALE_NUM)
    private long saleTotalNum;

    /**
     * 撤销总金额
     */
    @DatabaseField(columnName = VOID_AMOUNT)
    private long voidTotalAmt;
    /**
     * 撤销总笔数
     */
    @DatabaseField(columnName = VOID_NUM)
    private long voidTotalNum;
    /**
     * 退货总金额
     */
    @DatabaseField(columnName = REFUND_AMOUNT)
    private long refundTotalAmt;
    /**
     * 退货总笔数
     */
    @DatabaseField(columnName = REFUND_NUM)
    private long refundTotalNum;
    /**
     * refund void total amount
     */
    @DatabaseField(columnName = REFUND_VOID_AMOUNT)
    private long refundVoidTotalAmt;
    /**
     * refund void total num
     */
    @DatabaseField(columnName = REFUND_VOID_NUM)
    private long refundVoidTotalNum;
    /**
     * sale void total amount
     */
    @DatabaseField(columnName = SALE_VOID_AMOUNT)
    private long saleVoidTotalAmt;
    /**
     * sale void total num
     */
    @DatabaseField(columnName = SALE_VOID_NUM)
    private long saleVoidTotalNum;
    /**
     * 预授权总金额
     */
    @DatabaseField(columnName = AUTH_AMOUNT)
    private long authTotalAmt;
    /**
     * 预授权总笔数
     */
    @DatabaseField(columnName = AUTH_NUM)
    private long authTotalNum;
    //AET-75
    /**
     * 脱机交易总金额
     */
    @DatabaseField(columnName = OFFLINE_AMOUNT)
    private long offlineTotalAmt;
    /**
     * 脱机交易总笔数
     */
    @DatabaseField(columnName = OFFLINE_NUM)
    private long offlineTotalNum;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSaleTotalAmt() {
        return saleTotalAmt;
    }

    public void setSaleTotalAmt(long saleTotalAmt) {
        this.saleTotalAmt = saleTotalAmt;
    }

    public long getSaleTotalNum() {
        return saleTotalNum;
    }

    public void setSaleTotalNum(long saleTotalNum) {
        this.saleTotalNum = saleTotalNum;
    }

    public long getVoidTotalAmt() {
        return voidTotalAmt;
    }

    public void setVoidTotalAmt(long voidTotalAmt) {
        this.voidTotalAmt = voidTotalAmt;
    }

    public long getVoidTotalNum() {
        return voidTotalNum;
    }

    public void setVoidTotalNum(long voidTotalNum) {
        this.voidTotalNum = voidTotalNum;
    }

    public long getRefundVoidTotalAmt() {
        return refundVoidTotalAmt;
    }

    public void setRefundVoidTotalAmt(long refundVoidTotalAmt) {
        this.refundVoidTotalAmt = refundVoidTotalAmt;
    }

    public long getRefundVoidTotalNum() {
        return refundVoidTotalNum;
    }

    public void setRefundVoidTotalNum(long refundVoidTotalNum) {
        this.refundVoidTotalNum = refundVoidTotalNum;
    }

    public long getSaleVoidTotalAmt() {
        return saleVoidTotalAmt;
    }

    public void setSaleVoidTotalAmt(long saleVoidTotalAmt) {
        this.saleVoidTotalAmt = saleVoidTotalAmt;
    }

    public long getSaleVoidTotalNum() {
        return saleVoidTotalNum;
    }

    public void setSaleVoidTotalNum(long saleVoidTotalNum) {
        this.saleVoidTotalNum = saleVoidTotalNum;
    }

    public long getRefundTotalAmt() {
        return refundTotalAmt;
    }

    public void setRefundTotalAmt(long refundTotalAmt) {
        this.refundTotalAmt = refundTotalAmt;
    }

    public long getRefundTotalNum() {
        return refundTotalNum;
    }

    public void setRefundTotalNum(long refundTotalNum) {
        this.refundTotalNum = refundTotalNum;
    }

    public long getAuthTotalAmt() {
        return authTotalAmt;
    }

    public void setAuthTotalAmt(long authTotalAmt) {
        this.authTotalAmt = authTotalAmt;
    }

    public long getAuthTotalNum() {
        return authTotalNum;
    }

    public void setAuthTotalNum(long authTotalNum) {
        this.authTotalNum = authTotalNum;
    }

    public String getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public int getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(int batchNo) {
        this.batchNo = batchNo;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public long getOfflineTotalAmt() {
        return offlineTotalAmt;
    }

    public void setOfflineTotalAmt(long offlineTotalAmt) {
        this.offlineTotalAmt = offlineTotalAmt;
    }

    public long getOfflineTotalNum() {
        return offlineTotalNum;
    }

    public void setOfflineTotalNum(long offlineTotalNum) {
        this.offlineTotalNum = offlineTotalNum;
    }

    public Acquirer getAcquirer() {
        return acquirer;
    }

    public void setAcquirer(Acquirer acquirer) {
        this.acquirer = acquirer;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }
}
