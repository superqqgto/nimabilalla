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
package com.pax.pay.trans.transmit;

public class TcpResponse {
    int retCode;
    byte[] data;

    public int getRetCode() {
        return retCode;
    }

    public TcpResponse(int retCode, byte[] data) {
        this.retCode = retCode;
        this.data = data;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
