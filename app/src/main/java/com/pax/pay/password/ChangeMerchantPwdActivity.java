/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-21
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.password;

import com.pax.abl.utils.EncUtils;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;

/**
 * Change terminal password
 */
public class ChangeMerchantPwdActivity extends BaseChangePwdActivity {

    @Override
    protected void savePwd() {
        SpManager.getSysParamSp().set(SysParamSp.SEC_MERCHANTPWD, EncUtils.SHA1(pwd));
    }
}
