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
package com.pax.pay.trans.receipt;

import android.graphics.Bitmap;

import com.pax.pay.constant.Constants;

/**
 * receipt generator
 *
 * @author Steven.W
 */
public interface IReceiptGenerator {
    int FONT_BIG = 30;
    int FONT_NORMAL = 24;
    int FONT_SMALL = 20;
    String TYPE_FACE = Constants.FONT_PATH + Constants.FONT_NAME;

    /**
     * generate receipt
     *
     * @return
     */
    Bitmap generateBitmap();

    /**
     * generate simplified receipt string
     */
    String generateString();
}
