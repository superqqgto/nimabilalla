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
package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pax.abl.core.AAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.SelectOptionActivity;
import com.pax.pay.utils.ContextUtils;

import java.util.ArrayList;

public class ActionSelectOption extends AAction {

    public ActionSelectOption(ActionStartListener listener) {
        super(listener);
    }

    private String title;
    private String subTitle;

    private ArrayList<String> nameList;

    public void setParam(String title, String subTitle, ArrayList<String> list) {
        this.title = title;
        this.subTitle = subTitle;
        this.nameList = list;
    }

    @Override
    protected void process() {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Context context = ContextUtils.getActyContext();
                Intent intent = new Intent(context, SelectOptionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putString(EUIParamKeys.PROMPT_1.toString(), subTitle);
                bundle.putStringArrayList(EUIParamKeys.CONTENT.toString(), nameList);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }
}
