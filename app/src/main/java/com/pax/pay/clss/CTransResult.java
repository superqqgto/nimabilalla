/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-2
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.clss;

import android.content.Context;
import android.content.res.Resources;

import com.pax.edc.R;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.CvmType;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clcommon.TransactionPath;
import com.pax.pay.utils.ContextUtils;

public class CTransResult {

    private int transResult;
    private byte cvmResult;
    private int pathResult;
    private int acResult;

    public static final int App_Try_Again = -2000;

    public CTransResult() {
        this.transResult = RetCode.EMV_OK;
        this.cvmResult = CvmType.RD_CVM_NO;
        this.pathResult = TransactionPath.CLSS_PATH_NORMAL;
        this.acResult = ACType.AC_AAC;
    }

    public String getMessage(int ret) {
        String message = "";
        switch (ret) {
            case RetCode.EMV_OK:
                message = ContextUtils.getString(R.string.dialog_trans_succ);
                break;
            case RetCode.EMV_NO_APP:
                message = ContextUtils.getString(R.string.dialog_emv_no_app);
                break;
            case RetCode.EMV_NO_APP_PPSE_ERR:
                message = ContextUtils.getString(R.string.dialog_emv_no_app_ppse_err);
                break;
            case RetCode.CLSS_PARAM_ERR:
                message = ContextUtils.getString(R.string.dialog_clss_param_err);
                break;
            default:
                message = ContextUtils.getString(R.string.err_undefine) + "[" + ret + "]";
                break;
        }
        return message;
    }

    public int getTransResult() {
        return transResult;
    }

    public void setTransResult(int transResult) {
        this.transResult = transResult;
    }

    public byte getCvmResult() {
        return cvmResult;
    }

    public void setCvmResult(byte cvmResult) {
        this.cvmResult = cvmResult;
    }

    public int getPathResult() {
        return pathResult;
    }

    public void setPathResult(int pathResult) {
        this.pathResult = pathResult;
    }

    public int getAcResult() {
        return acResult;
    }

    public void setAcResult(int acResult) {
        this.acResult = acResult;
    }
}
