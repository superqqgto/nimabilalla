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
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

public class PackOfflineBat extends PackIso8583 {

    public PackOfflineBat(PackListener listener) {
        super(listener);
    }

    @Override
    public byte[] pack(TransData transData) {

        if (transData == null) {
            return null;
        }
        // 当前交易类型，通知类批上送
        ETransType transType = transData.getTransType();
        byte[] buf = null;
        byte[] temp = ETransType.OFFLINE_TRANS_SEND.getPackager(listener).pack(transData);
        if (temp != null) {
            buf = new byte[temp.length - 8];
            System.arraycopy(temp, 0, buf, 0, temp.length - 8);
            // 把0220转成0320
            buf[11] = 0x03;
            // 去掉bit64
            buf[11 + 2 + 8 - 1] &= 0xfe;
        }
        // 恢复交易类型
        transData.setTransType(transType);

        return buf;
    }

}
