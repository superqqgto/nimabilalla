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
package com.pax.pay.emv;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.pax.eemv.entity.AidParam;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.GlManager;

import java.util.LinkedList;
import java.util.List;

@DatabaseTable(tableName = "aid")
public class EmvAid {
    public final static String ID_FIELD_NAME = "id";
    public final static String AID_FIELD_NAME = "aid";


    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private int id;
    /**
     * name
     */
    @DatabaseField(canBeNull = false)
    private String appName;
    /**
     * aid, 应用标志
     */
    @DatabaseField(unique = true, canBeNull = false, columnName = AID_FIELD_NAME)
    private String aid;
    /**
     * 选择标志(PART_MATCH 部分匹配 FULL_MATCH 全匹配)
     */
    @DatabaseField(canBeNull = false)
    private int selFlag;
    /**
     * priority
     */
    @DatabaseField(canBeNull = false)
    private int priority;
    /**
     * 终端联机PIN支持能力
     */
    @DatabaseField(canBeNull = false)
    private int onlinePin;
    /**
     * 读卡器非接触CVM限制(DF21)
     */
    @DatabaseField(canBeNull = false)
    private long rdCVMLmt;
    /**
     * 读卡器非接触交易限额(DF20)
     */
    @DatabaseField(canBeNull = false)
    private long rdClssTxnLmt;
    /**
     * 读卡器非接触脱机最低限额(DF19)
     */
    @DatabaseField(canBeNull = false)
    private long rdClssFLmt;
    /**
     * 是否存在读卡器非接触脱机最低限额
    */
    @DatabaseField(canBeNull = false)
    private int rdClssFLmtFlg;
    /**
     * 是否存在读卡器非接触交易限额
     */
    @DatabaseField(canBeNull = false)
    private int rdClssTxnLmtFlg;
    /**
     * 是否存在读卡器非接触CVM限额
     */
    @DatabaseField(canBeNull = false)
    private int rdCVMLmtFlg;

    /**
     * 目标百分比数
     */
    @DatabaseField(canBeNull = false)
    private int targetPer;
    /**
     * 最大目标百分比数
     */
    @DatabaseField(canBeNull = false)
    private int maxTargetPer;
    /**
     * 是否检查最低限额
     */
    @DatabaseField(canBeNull = false)
    private int floorLimitCheck;
    /**
     * 是否进行随机交易选择
     */
    @DatabaseField(canBeNull = false)
    private int randTransSel;
    /**
     * 是否进行频度检测
     */
    @DatabaseField(canBeNull = false)
    private int velocityCheck;
    /**
     * 最低限额
     */
    @DatabaseField(canBeNull = false)
    private long floorLimit;
    /**
     * 阀值
     */
    @DatabaseField(canBeNull = false)
    private long threshold;
    /**
     * 终端行为代码(拒绝)
     */
    @DatabaseField
    private String tacDenial;
    /**
     * 终端行为代码(联机)
     */
    @DatabaseField
    private String tacOnline;
    /**
     * 终端行为代码(缺省)
     */
    @DatabaseField
    private String tacDefault;
    /**
     * 收单行标志־
     */
    @DatabaseField
    private String acquirerId;
    /**
     * 终端缺省DDOL
     */
    @DatabaseField
    private String dDOL;
    /**
     * 终端缺省TDOL
     */
    @DatabaseField
    private String tDOL;
    /**
     * 应用版本
     */
    @DatabaseField
    private String version;
    /**
     * 风险管理数据
     */
    @DatabaseField
    private String riskManageData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public int getSelFlag() {
        return selFlag;
    }

    public void setSelFlag(int selFlag) {
        this.selFlag = selFlag;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getOnlinePin() {
        return onlinePin;
    }

    public void setOnlinePin(int onlinePin) {
        this.onlinePin = onlinePin;
    }

    public long getRdCVMLmt() {
        return rdCVMLmt;
    }

    public void setRdCVMLmt(long rdCVMLmt) {
        this.rdCVMLmt = rdCVMLmt;
    }

    public long getRdClssTxnLmt() {
        return rdClssTxnLmt;
    }

    public void setRdClssTxnLmt(long rdClssTxnLmt) {
        this.rdClssTxnLmt = rdClssTxnLmt;
    }

    public long getRdClssFLmt() {
        return rdClssFLmt;
    }

    public void setRdClssFLmt(long rdClssFLmt) {
        this.rdClssFLmt = rdClssFLmt;
    }

    public int getRdClssFLmtFlg() {
        return rdClssFLmtFlg;
    }

    public void setRdClssFLmtFlg(int rdClssFLmtFlg) {
        this.rdClssFLmtFlg = rdClssFLmtFlg;
    }

    public int getRdClssTxnLmtFlg() {
        return rdClssTxnLmtFlg;
    }

    public void setRdClssTxnLmtFlg(int rdClssTxnLmtFlg) {
        this.rdClssTxnLmtFlg = rdClssTxnLmtFlg;
    }

    public int getRdCVMLmtFlg() {
        return rdCVMLmtFlg;
    }

    public void setRdCVMLmtFlg(int rdCVMLmtFlg) {
        this.rdCVMLmtFlg = rdCVMLmtFlg;
    }

    public int getTargetPer() {
        return targetPer;
    }

    public void setTargetPer(int targetPer) {
        this.targetPer = targetPer;
    }

    public int getMaxTargetPer() {
        return maxTargetPer;
    }

    public void setMaxTargetPer(int maxTargetPer) {
        this.maxTargetPer = maxTargetPer;
    }

    public int getFloorLimitCheck() {
        return floorLimitCheck;
    }

    public void setFloorLimitCheck(int floorLimitCheck) {
        this.floorLimitCheck = floorLimitCheck;
    }

    public int getRandTransSel() {
        return randTransSel;
    }

    public void setRandTransSel(int randTransSel) {
        this.randTransSel = randTransSel;
    }

    public int getVelocityCheck() {
        return velocityCheck;
    }

    public void setVelocityCheck(int velocityCheck) {
        this.velocityCheck = velocityCheck;
    }

    public long getFloorLimit() {
        return floorLimit;
    }

    public void setFloorLimit(long floorLimit) {
        this.floorLimit = floorLimit;
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

    public String getTacDenial() {
        return tacDenial;
    }

    public void setTacDenial(String tacDenial) {
        this.tacDenial = tacDenial;
    }

    public String getTacOnline() {
        return tacOnline;
    }

    public void setTacOnline(String tacOnline) {
        this.tacOnline = tacOnline;
    }

    public String getTacDefault() {
        return tacDefault;
    }

    public void setTacDefault(String tacDefault) {
        this.tacDefault = tacDefault;
    }

    public String getAcquirerId() {
        return acquirerId;
    }

    public void setAcquirerId(String acquirerId) {
        this.acquirerId = acquirerId;
    }

    public String getDDOL() {
        return dDOL;
    }

    public void setDDOL(String dDOL) {
        this.dDOL = dDOL;
    }

    public String getTDOL() {
        return tDOL;
    }

    public void setTDOL(String tDOL) {
        this.tDOL = tDOL;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRiskManageData() {
        return riskManageData;
    }

    public void setRiskManageData(String riskManageData) {
        this.riskManageData = riskManageData;
    }

    /***************************
     * EmvAidParam to AidParam
     ***********************************/
    public static List<AidParam> toAidParams() {
        List<AidParam> list = new LinkedList<>();

        List<EmvAid> aidList = DbManager.getEmvDao().findAllAID();
        if (aidList == null) {
            return null;
        }
        for (EmvAid emvAidParam : aidList) {
            AidParam aidParam = new AidParam();
            aidParam.setAppName(emvAidParam.getAppName());
            aidParam.setAid(GlManager.strToBcdPaddingLeft(emvAidParam.getAid()));
            aidParam.setSelFlag((byte) emvAidParam.getSelFlag());
            aidParam.setPriority((byte) emvAidParam.getPriority());
            aidParam.setOnlinePin((byte) emvAidParam.getOnlinePin());
            aidParam.setRdCVMLmt(emvAidParam.getRdCVMLmt());
            aidParam.setRdClssTxnLmt(emvAidParam.getRdClssTxnLmt());
            aidParam.setRdClssFLmt(emvAidParam.getRdClssFLmt());
            aidParam.setRdClssFLmtFlag((byte) emvAidParam.getRdClssFLmtFlg());
            aidParam.setRdClssTxnLmtFlag((byte) emvAidParam.getRdClssTxnLmtFlg());
            aidParam.setRdCVMLmtFlag((byte) emvAidParam.getRdCVMLmtFlg());
            aidParam.setFloorLimit(emvAidParam.getFloorLimit());
            aidParam.setFloorLimitCheck((byte) emvAidParam.getFloorLimitCheck());
            aidParam.setThreshold(emvAidParam.getThreshold());
            aidParam.setTargetPer((byte) emvAidParam.getTargetPer());
            aidParam.setMaxTargetPer((byte) emvAidParam.getMaxTargetPer());
            aidParam.setRandTransSel((byte) emvAidParam.getRandTransSel());
            aidParam.setVelocityCheck((byte) emvAidParam.getVelocityCheck());
            aidParam.setTacDenial(GlManager.strToBcdPaddingLeft(emvAidParam.getTacDenial()));
            aidParam.setTacOnline(GlManager.strToBcdPaddingLeft(emvAidParam.getTacOnline()));
            aidParam.setTacDefault(GlManager.strToBcdPaddingLeft(emvAidParam.getTacDefault()));
            if (emvAidParam.getAcquirerId() != null) {
                aidParam.setAcquierId(GlManager.strToBcdPaddingLeft(emvAidParam.getAcquirerId()));
            }
            if (emvAidParam.getDDOL() != null) {
                aidParam.setdDol(GlManager.strToBcdPaddingLeft(emvAidParam.getDDOL()));
            }
            if (emvAidParam.getTDOL() != null) {
                aidParam.settDol(GlManager.strToBcdPaddingLeft(emvAidParam.getTDOL()));
            }
            aidParam.setVersion(GlManager.strToBcdPaddingLeft(emvAidParam.getVersion()));
            if (emvAidParam.getRiskManageData() != null) {
                aidParam.setRiskManData(GlManager.strToBcdPaddingLeft(emvAidParam.getRiskManageData()));
            }
            list.add(aidParam);
        }
        return list;
    }

}
