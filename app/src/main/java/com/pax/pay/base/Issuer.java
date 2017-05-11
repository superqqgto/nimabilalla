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
import java.util.Calendar;

@DatabaseTable(tableName = "issuer")
public class Issuer implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String ID_FIELD_NAME = "issuer_id";
    public final static String NAME_FIELD_NAME = "issuer_name";

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

    @DatabaseField
    private long floorLimit;

    @DatabaseField
    private float adjustPercent;

    /**
     * using regular expression, example {@link com.pax.pay.constant.Constants#DEF_PAN_MASK_PATTERN}
     */
    @DatabaseField
    private String panMaskPattern;

    @DatabaseField
    private boolean isEnableAdjust = true;

    @DatabaseField
    private boolean isEnableOffline = true;

    @DatabaseField
    private boolean isAllowExpiry = true;

    @DatabaseField
    private boolean isAllowManualPan = true;

    @DatabaseField
    private boolean isAllowCheckExpiry = false;

    @DatabaseField
    private boolean isAllowPrint = true;

    @DatabaseField
    private boolean isAllowCheckPanMod10 = true;

    @DatabaseField
    private boolean isRequirePIN = true;

    @DatabaseField
    private boolean isRequireMaskExpiry = true;

    public Issuer() {
    }

    public Issuer(String name) {
        this.setName(name);
    }

    public Issuer(int id, String name) {
        this.setId(id);
        this.setName(name);
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

    public long getFloorLimit() {
        return floorLimit;
    }

    public void setFloorLimit(long floorLimit) {
        this.floorLimit = floorLimit;
    }

    public float getAdjustPercent() {
        return adjustPercent;
    }

    public void setAdjustPercent(float adjustPercent) {
        this.adjustPercent = adjustPercent;
    }

    public String getPanMaskPattern() {
        return panMaskPattern;
    }

    public void setPanMaskPattern(String panMaskPattern) {
        this.panMaskPattern = panMaskPattern;
    }

    public boolean isEnableAdjust() {
        return isEnableAdjust;
    }

    public void setEnableAdjust(boolean enableAdjust) {
        isEnableAdjust = enableAdjust;
    }

    public boolean isEnableOffline() {
        return isEnableOffline;
    }

    public void setEnableOffline(boolean enableOffline) {
        isEnableOffline = enableOffline;
    }

    public boolean isAllowExpiry() {
        return isAllowExpiry;
    }

    public void setAllowExpiry(boolean allowExpiry) {
        isAllowExpiry = allowExpiry;
    }

    public boolean isAllowManualPan() {
        return isAllowManualPan;
    }

    public void setAllowManualPan(boolean allowManualPan) {
        isAllowManualPan = allowManualPan;
    }

    public boolean isAllowCheckExpiry() {
        return isAllowCheckExpiry;
    }

    public void setAllowCheckExpiry(boolean allowCheckExpiry) {
        isAllowCheckExpiry = allowCheckExpiry;
    }

    public boolean isAllowPrint() {
        return isAllowPrint;
    }

    public void setAllowPrint(boolean allowPrint) {
        isAllowPrint = allowPrint;
    }

    public boolean isAllowCheckPanMod10() {
        return isAllowCheckPanMod10;
    }

    public void setAllowCheckPanMod10(boolean allowCheckPanMod10) {
        isAllowCheckPanMod10 = allowCheckPanMod10;
    }

    public boolean isRequirePIN() {
        return isRequirePIN;
    }

    public void setRequirePIN(boolean requirePIN) {
        isRequirePIN = requirePIN;
    }

    public boolean isRequireMaskExpiry() {
        return isRequireMaskExpiry;
    }

    public void setRequireMaskExpiry(boolean requireMaskExpiry) {
        isRequireMaskExpiry = requireMaskExpiry;
    }

    public static boolean validPan(final Issuer issuer, String pan) {
        if (!issuer.isAllowCheckPanMod10()) {
            return true;
        }

        boolean flag = false;
        int result = 0;
        for (int i = (pan.length() - 1); i >= 0; --i) {
            int tmp = pan.charAt(i) & 15;
            if (flag) {
                tmp *= 2;
            }
            if (tmp > 9) {
                tmp -= 9;
            }
            result = (tmp + result) % 10;
            flag = !flag;
        }

        return result == 0;
    }

    public static boolean validCardExpiry(final Issuer issuer, String date) {
        if (!issuer.isAllowExpiry() || !issuer.isAllowCheckExpiry()) {
            return true;
        }
        Calendar now = Calendar.getInstance();

        int year = Integer.valueOf(date.substring(0, 2));
        year += year > 80 ? 1900 : 2000;
        int month = Integer.valueOf(date.substring(2, 4));

        return !(year < now.get(Calendar.YEAR) ||
                (year <= now.get(Calendar.YEAR) && month < now.get(Calendar.MONTH)));
    }
}
