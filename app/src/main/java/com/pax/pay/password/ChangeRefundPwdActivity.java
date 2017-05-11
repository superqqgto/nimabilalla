/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-21
 * Module Author: Rim.Z
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.password;

import com.pax.abl.utils.EncUtils;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;

/**
 * Change refund password
 */
public class ChangeRefundPwdActivity extends BaseChangePwdActivity {

    @Override
    protected void savePwd() {
        SpManager.getSysParamSp().set(SysParamSp.SEC_REFUNDPWD, EncUtils.SHA1(pwd));
    }
}
