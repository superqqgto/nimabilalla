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
import com.pax.eemv.entity.Capk;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.GlManager;

import java.util.LinkedList;
import java.util.List;

@DatabaseTable(tableName = "capk")
public class EmvCapk {
    public final static String ID_FIELD_NAME = "id";
    public final static String RID_FIELD_NAME = "rid";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private int id;

    // 应用注册服务商ID
    @DatabaseField(canBeNull = false, columnName = RID_FIELD_NAME)
    private String RID;
    // 密钥索引
    @DatabaseField(canBeNull = false)
    private int KeyID;
    // HASH算法标志
    @DatabaseField(canBeNull = false)
    private int HashInd;
    // RSA算法标志
    @DatabaseField(canBeNull = false)
    private int arithInd;
    // 模
    @DatabaseField
    private String module;
    // 指数
    @DatabaseField
    private String Exponent;
    // 有效期(YYMMDD)
    @DatabaseField
    private String expDate;
    // 密钥校验和
    @DatabaseField
    private String checkSum;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRID() {

        return RID;
    }

    public void setRID(String rID) {

        RID = rID;
    }

    public int getKeyID() {

        return KeyID;
    }

    public void setKeyID(int keyID) {

        KeyID = keyID;
    }

    public int getHashInd() {

        return HashInd;
    }

    public void setHashInd(int hashInd) {

        HashInd = hashInd;
    }

    public int getArithInd() {

        return arithInd;
    }

    public void setArithInd(int arithInd) {

        this.arithInd = arithInd;
    }

    public String getModule() {

        return module;
    }

    public void setModule(String module) {

        this.module = module;
    }

    public String getExponent() {

        return Exponent;
    }

    public void setExponent(String exponent) {

        Exponent = exponent;
    }

    public String getExpDate() {

        return expDate;
    }

    public void setExpDate(String expDate) {

        this.expDate = expDate;
    }

    public String getCheckSum() {

        return checkSum;
    }

    public void setCheckSum(String checkSum) {

        this.checkSum = checkSum;
    }

    /********************************
     * EmvCapk to Capk
     *******************************/
    public static List<Capk> toCapk() {
        List<Capk> list = new LinkedList<>();

        List<EmvCapk> capkList = DbManager.getEmvDao().findAllCAPK();
        if (capkList == null) {
            return null;
        }
        for (EmvCapk readCapk : capkList) {
            Capk capk = new Capk();
            capk.setRid(GlManager.strToBcdPaddingLeft(readCapk.getRID()));
            capk.setKeyID((byte) readCapk.getKeyID());
            capk.setHashInd((byte) readCapk.getHashInd());
            capk.setArithInd((byte) readCapk.getArithInd());
            if (readCapk.getModule() == null)
                continue;
            capk.setModul(GlManager.strToBcdPaddingLeft(readCapk.getModule()));
            if (readCapk.getExponent() == null)
                continue;
            capk.setExponent(GlManager.strToBcdPaddingLeft(readCapk.getExponent()));
            capk.setExpDate(GlManager.strToBcdPaddingLeft(readCapk.getExpDate()));
            capk.setCheckSum(GlManager.strToBcdPaddingLeft(readCapk.getCheckSum()));
            list.add(capk);
        }
        return list;
    }
}
