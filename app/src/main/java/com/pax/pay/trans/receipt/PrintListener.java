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

public interface PrintListener {
    int CONTINUE = 0;
    int CANCEL = 1;

    /**
     * print prompt
     *
     * @param title
     * @param message
     */
    void onShowMessage(String title, String message);

    /**
     * printer abnormal
     *
     * @param title
     * @param message
     * @return {@link PrintListener#CONTINUE}/{@link PrintListener#CANCEL}
     */
    int onConfirm(String title, String message);

    void onEnd();
}
