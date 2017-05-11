/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-26
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.menu;

import com.pax.edc.R;
import com.pax.pay.trans.SaleTrans;
import com.pax.view.MenuPage;

public class BypassMenuActivity extends BaseMenuActivity {

    @Override
    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(BypassMenuActivity.this, 3, 3)
                .addTransItem(R.string.quick_pass_sale_free_pin, R.drawable.sale_bypin,
                        new SaleTrans(null, (byte) -3, false, null));
        return builder.create();
    }

}
