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

@DatabaseTable(tableName = "card_bin")
public class CardBin {
    public final static String ID_FIELD_NAME = "id";
    public final static String BIN_FIELD_NAME = "card_bin";


    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    protected int id;
    // 卡号
    @DatabaseField(unique = true, columnName = BIN_FIELD_NAME)
    protected String bin;
    @DatabaseField
    protected int cardNoLen;

    public CardBin() {
    }

    public CardBin(String bin, int cardNoLen) {
        this.bin = bin;
        this.cardNoLen = cardNoLen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public int getCardNoLen() {
        return cardNoLen;
    }

    public void setCardNoLen(int cardNoLen) {
        this.cardNoLen = cardNoLen;
    }

}
