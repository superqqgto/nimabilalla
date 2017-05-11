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
package com.pax.pay.trans.transmit;

import com.pax.pay.trans.model.TransData;

public interface TransProcessListener {
    void onShowProgress(String message, int timeout);

    void onUpdateProgressTitle(String title);

    void onHideProgress();

    int onShowNormalMessageWithConfirm(String message, int timeout);

    int onShowErrMessageWithConfirm(String message, int timeout);

    int onInputOnlinePin(TransData transData);

    byte[] onCalcMac(byte[] data);

    byte[] onEncTrack(byte[] track);
}