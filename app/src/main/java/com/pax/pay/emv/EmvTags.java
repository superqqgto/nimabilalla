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

import com.pax.gl.packer.ITlv;
import com.pax.gl.packer.ITlv.ITlvDataObj;
import com.pax.gl.packer.ITlv.ITlvDataObjList;
import com.pax.gl.packer.TlvException;
import com.pax.manager.neptune.EmvManager;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.trans.model.ETransType;

public class EmvTags {
    /**
     * 消费55域EMV标签
     */
    public static final int[] TAGS_SALE = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02, 0x5F2A,
            0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F34, 0x9F35, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F63};
    /**
     * 脱机消费（PBOC）55域EMV标签
     */
    public static final int[] TAGS_PBOC_OFFLINE = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02,
            0x5F2A, 0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F34, 0x9F35, 0x9F63, 0x8A};
    /**
     * 预授权55域EMV标签
     */
    public static final int[] TAGS_AUTH = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02, 0x5F2A,
            0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F34, 0x9F35, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F63};

    /**
     * 冲正
     */
    public static final int[] TAGS_DUP = {0x95, 0x9F10, 0x9F1E, 0xDF31};

    /**
     * 交易承兑但卡片拒绝时发起的冲正
     */
    public static final int[] TAGS_POSACCPDUP = {0x95, 0x9F10, 0x9F1E, 0x9F36, 0xDF31};

    private EmvTags() {

    }

    /**
     * 根据交易类型获取55域TLV数据
     *
     * @param transType
     * @param isDup
     * @return
     */
    public static byte[] getF55(ETransType transType, boolean isDup) {
        switch (transType) {
            case SALE:
                if (isDup) {
                    return getValueList(TAGS_DUP);
                }

                return getValueList(TAGS_SALE);
            case PREAUTH:
                if (isDup) {
                    return getValueList(TAGS_DUP);
                }
                return getValueList(TAGS_AUTH);
            default:
                break;
        }
        return null;
    }

    public static byte[] getF55forPosAccpDup() {
        return getValueList(TAGS_POSACCPDUP);
    }

    private static byte[] getValueList(int[] tags) {
        if (tags == null || tags.length == 0) {
            return null;
        }

        ITlv tlv = GlManager.getPacker().getTlv();
        ITlvDataObjList tlvList = tlv.createTlvDataObjectList();
        for (int tag : tags) {
            try {
                byte[] value = EmvManager.getTlv(tag);
                if (value == null || value.length == 0) {
                    if (tag == 0x9f03) {
                        value = new byte[6];
                    } else {
                        continue;
                    }
                }
                ITlvDataObj obj = tlv.createTlvDataObject();
                obj.setTag(tag);
                obj.setValue(value);
                tlvList.addDataObj(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            return tlv.pack(tlvList);
        } catch (TlvException e) {
            e.printStackTrace();
        }

        return null;

    }
}
