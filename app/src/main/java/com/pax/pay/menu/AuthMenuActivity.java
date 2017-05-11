/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-26
 * Module Auth: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.menu;

import com.pax.edc.R;
import com.pax.pay.trans.authtrans.AuthTrans;
import com.pax.pay.trans.authtrans.PreAuthCancelTrans;
import com.pax.pay.trans.authtrans.PreAuthCompCancelTrans;
import com.pax.pay.trans.authtrans.PreAuthCompOfflineTrans;
import com.pax.pay.trans.authtrans.PreAuthCompTrans;
import com.pax.view.MenuPage;

public class AuthMenuActivity extends BaseMenuActivity {

    /**
     * 预授权，
     */
    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(AuthMenuActivity.this, 6, 3)
                .addTransItem(R.string.trans_preAuth, R.drawable.app_auth,
                        new AuthTrans(true))
                .addTransItem(R.string.trans_preauth_comp, R.drawable.app_auth,
                        new PreAuthCompTrans())
                .addTransItem(R.string.trans_preauth_cancel, R.drawable.app_void,
                        new PreAuthCancelTrans())
                .addTransItem(R.string.trans_preauth_comp_cancel, R.drawable.app_auth,
                        new PreAuthCompCancelTrans())
                .addTransItem(R.string.trans_preauth_comp_offline, R.drawable.app_void,
                        new PreAuthCompOfflineTrans());
        return builder.create();
    }
}
