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
import android.os.SystemClock;

import com.pax.dal.exceptions.PrinterDevException;
import com.pax.edc.R;
import com.pax.manager.neptune.DalManager;
import com.pax.pay.utils.ContextUtils;

abstract class AReceiptPrint {

    protected PrintListener listener;

    /**
     * return -1 stop print
     *
     * @param bitmap
     * @return
     */
    protected int printBitmap(Bitmap bitmap) {
        try {
            DalManager.getPrinter().init();
            DalManager.getPrinter().printBitmap(bitmap);

            return start();

        } catch (PrinterDevException e) {
            e.printStackTrace();
        }

        return -1;
    }

    protected int printStr(String str) {
        try {
            DalManager.getPrinter().init();
            DalManager.getPrinter().printStr(str, null);
            return start();

        } catch (PrinterDevException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private int start() {
        int result;
        try {
            while (true) {
                if (listener != null)
                    listener.onShowMessage(null, ContextUtils.getString(R.string.wait_print));
                int ret = DalManager.getPrinter().start();
                // printer busy please wait
                if (ret == 1) {
                    SystemClock.sleep(100);
                    continue;
                } else if (ret == 2) {
                    if (listener != null) {
                        result = listener.onConfirm(null, ContextUtils.getString(R.string.err_print_paper));
                        if (result == PrintListener.CONTINUE) {
                            continue;
                        }
                    }
                    return -1;
                } else if (ret == 8) {
                    if (listener != null) {
                        result = listener.onConfirm(null, ContextUtils.getString(R.string.err_print_hot));
                        if (result == PrintListener.CONTINUE) {
                            continue;
                        }
                    }
                    return -1;
                } else if (ret == 9) {
                    if (listener != null) {
                        result = listener.onConfirm(null, ContextUtils.getString(R.string.err_print_voltage));
                        if (result == PrintListener.CONTINUE) {
                            continue;
                        }
                    }
                    return -1;
                } else if (ret != 0) {
                    return -1;
                }

                return 0;
            }
        } catch (PrinterDevException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
