/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-16
 * Module Author: laiyi
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.clss;

public class ClssTlvTag {
    public static final byte[] AppVerTag = new byte[]{(byte) 0x9F, (byte) 0x09};

    public static final byte[] CardDataTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x17};
    public static final byte[] CvmReqTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x18};
    public static final byte[] CvmNoTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x19};
    public static final byte[] SecTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x1F};

    public static final byte[] MagCvmReqTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x1E};
    public static final byte[] MagCvmNoTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x2C};

    public static final byte[] Amount_Tag = new byte[]{(byte) 0x9F, (byte) 0x02};

    public static final byte[] TransType_Tag = new byte[]{(byte) 0x9C};
    public static final byte[] TransDate_Tag = new byte[]{(byte) 0x9A};
    public static final byte[] TransTime_Tag = new byte[]{(byte) 0x9F, (byte) 0x21};

    //TAC Online
    public static final byte[] TermDefaultTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x20};
    public static final byte[] TermDenialTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x21};
    public static final byte[] TermOnlineTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x22};

    //limit  set for AID
    public static final byte[] FloorLimitTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x23};
    public static final byte[] TransLimitTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x24};
    public static final byte[] TransCvmLimitTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x25};
    public static final byte[] CvmLimitTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x26};

    public static final byte[] MaxTornTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x1D};

    public static final byte[] CountryCodeTag = new byte[]{(byte) 0x9F, (byte) 0x1A};
    public static final byte[] CurrencyCodeTag = new byte[]{(byte) 0x5F, (byte) 0x2A};

    public static final byte[] KernCfgTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x1B};

    public static final byte[] CapkIdTag = new byte[]{(byte) 0x8F};
    public static final byte[] CapkRidTag = new byte[]{(byte) 0x4F};

    public static final byte[] ProIDTag = new byte[]{(byte) 0x9F, (byte) 0x5A};

    public static final byte[] Track2Tag = new byte[]{(byte) 0x57};

    public static final byte[] PanSeqNoTag = new byte[]{(byte) 0x5F, (byte) 0x34};
    public static final byte[] AppLabelTag = new byte[]{(byte) 0x50};
    public static final byte[] TvrTag = new byte[]{(byte) 0x95};
    public static final byte[] TsiTag = new byte[]{(byte) 0x9B};
    public static final byte[] AtcTag = new byte[]{(byte) 0x9F, (byte) 0x36};
    public static final byte[] AppCryptoTag = new byte[]{(byte) 0x9F, (byte) 0x26};
    public static final byte[] AppNameTag = new byte[]{(byte) 0x9F, (byte) 0x12};

    public static final byte[] pucListTag = new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x29};

}
