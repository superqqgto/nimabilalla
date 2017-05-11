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

@DatabaseTable(tableName = "acq_issuer_relation")
public class AcqIssuerRelation {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = Acquirer.ID_FIELD_NAME)
    private Acquirer acquirer;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = Issuer.ID_FIELD_NAME)
    private Issuer issuer;

    public AcqIssuerRelation() {
    }

    public AcqIssuerRelation(Acquirer acquirer, Issuer issuer) {
        this.setAcquirer(acquirer);
        this.setIssuer(issuer);
    }

    public AcqIssuerRelation(int id, Acquirer acquirer, Issuer issuer) {
        this.setId(id);
        this.setAcquirer(acquirer);
        this.setIssuer(issuer);
    }

    /**
     * id
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * foreign acquirer
     */
    public Acquirer getAcquirer() {
        return acquirer;
    }

    public void setAcquirer(Acquirer acquirer) {
        this.acquirer = acquirer;
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
