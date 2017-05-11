/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-14
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.receipt.paperless;

import android.telephony.SmsManager;

import com.pax.edc.R;
import com.pax.pay.trans.receipt.PrintListener;
import com.pax.pay.utils.ContextUtils;

abstract class AReceiptSMS {

    protected PrintListener listener;

    //send SMS
    public int sendTextMessage(String phoneNo, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
        } catch (Exception e) {
            if (listener != null)
                listener.onShowMessage(null, ContextUtils.getString(R.string.err_sms_sent_fail));
            return -1;
        }
        return 0;
    }
}
