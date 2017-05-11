/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-22
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.base;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "card_range")
public class CardRange implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String ID_FIELD_NAME = "card_id";
    public final static String NAME_FIELD_NAME = "card_name";
    public final static String RANGE_LOW_FIELD_NAME = "card_range_low";
    public final static String RANGE_HIGH_FIELD_NAME = "card_range_high";
    public final static String LENGTH_FIELD_NAME = "card_length";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private int id;

    @DatabaseField(columnName = NAME_FIELD_NAME)
    private String name;

    @DatabaseField(unique = true, canBeNull = false, columnName = RANGE_LOW_FIELD_NAME, width = 10)
    private String panRangeLow;

    @DatabaseField(unique = true, canBeNull = false, columnName = RANGE_HIGH_FIELD_NAME, width = 10)
    private String panRangeHigh;

    @DatabaseField(columnName = LENGTH_FIELD_NAME)
    private int panLength;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = Issuer.ID_FIELD_NAME)
    private Issuer issuer;

    public CardRange() {
    }

    public CardRange(String name, String panRangeLow, String panRangeHigh, int panLength, Issuer issuer) {
        this.setName(name);
        this.setPanRangeLow(panRangeLow);
        this.setPanRangeHigh(panRangeHigh);
        this.setPanLength(panLength);
        this.setIssuer(issuer);
    }

    public CardRange(int id, String name, String panRangeLow, String panRangeHigh, int panLength, Issuer issuer) {
        this.setId(id);
        this.setName(name);
        this.setPanRangeLow(panRangeLow);
        this.setPanRangeHigh(panRangeHigh);
        this.setPanLength(panLength);
        this.setIssuer(issuer);
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

    public String getPanRangeLow() {
        return panRangeLow;
    }

    public void setPanRangeLow(String panRangeLow) {
        this.panRangeLow = panRangeLow;
    }

    public String getPanRangeHigh() {
        return panRangeHigh;
    }

    public void setPanRangeHigh(String panRangeHigh) {
        this.panRangeHigh = panRangeHigh;
    }

    public int getPanLength() {
        return panLength;
    }

    public void setPanLength(int panLength) {
        this.panLength = panLength;
    }

    /**
     * foreign issuer
     */
    public Issuer getIssuer() {
        return issuer;
    }

    public void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }
}
