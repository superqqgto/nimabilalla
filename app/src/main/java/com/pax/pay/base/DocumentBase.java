/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-16
 * Module Author: laiyi
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.base;

public class DocumentBase {

    private String acqFilePath;
    private String issuerFilePath;
    private String cardRangeFilePath;
    private String relationFilePath;

    public DocumentBase(String acqPath, String issuerPath, String cardRangePath, String relationPath) {
        this.acqFilePath = acqPath;
        this.issuerFilePath = issuerPath;
        this.cardRangeFilePath = cardRangePath;
        this.relationFilePath = relationPath;
    }

    public String getAcqFilePath() {
        return acqFilePath;
    }

    public void setAcqFilePath(String acqFilePath) {
        this.acqFilePath = acqFilePath;
    }

    public String getIssuerFilePath() {
        return issuerFilePath;
    }

    public void setIssuerFilePath(String issuerFilePath) {
        this.issuerFilePath = issuerFilePath;
    }

    public String getRelationFilePath() {
        return relationFilePath;
    }

    public void setRelationFilePath(String relationFilePath) {
        this.relationFilePath = relationFilePath;
    }

    public String getCardRangeFilePath() {
        return cardRangeFilePath;
    }

    public void setCardRangeFilePath(String cardRangeFilePath) {
        this.cardRangeFilePath = cardRangeFilePath;
    }
}
