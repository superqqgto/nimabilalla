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
package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;

public class PackBatchUp extends PackIso8583 {

    public PackBatchUp(PackListener listener) {
        super(listener);
    }

    @Override
    public byte[] pack(TransData transData) {

        if (transData == null) {
            return null;
        }
        int ret = setMandatoryData(transData);
        if (ret != TransResult.SUCC) {
            return null;
        }
//        setBitData_48(transData);
        setBatchUpCommonData(transData);
        setBitData_60(transData);
        return pack(false);
    }

//    @Override
//    protected int setBitData_48(TransData transData) {
//        return setBitData("48", transData.getField48());
//    }
//
//    @Override
//    protected int setBitData_63(TransData transData) {
//        return setBitData("63", transData.getField63());
//    }
}
