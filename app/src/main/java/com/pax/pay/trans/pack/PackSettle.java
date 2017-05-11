/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Auth: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackSettle extends PackIso8583 {

    public PackSettle(PackListener listener) {
        super(listener);
    }

    @Override
    public byte[] pack(TransData transData) {

        if (transData == null) {
            return null;
        }
        setMandatoryData(transData);

        try {
            // field 11
            String temp = String.valueOf(transData.getTraceNo());
            if (temp.length() > 0) {
                entity.setFieldValue("11", temp);
            }

            // field 63
            setBitData_63(transData);

            setBitData_60(transData);

        } catch (Iso8583Exception e) {
            e.printStackTrace();
        }

        return pack(false);
    }
}
