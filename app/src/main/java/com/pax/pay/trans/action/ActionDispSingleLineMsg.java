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
package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pax.abl.core.AAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.DispSingleLineMsgActivity;
import com.pax.pay.utils.ContextUtils;

public class ActionDispSingleLineMsg extends AAction {

    private String title;
    private String prompt;
    private String content;
    private int tiketime;

    public ActionDispSingleLineMsg(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(String title, String prompt, String content, int tiketime) {
        this.title = title;
        this.prompt = prompt;
        this.content = content;
        this.tiketime = tiketime;
    }

    @Override
    protected void process() {

        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Context context = ContextUtils.getActyContext();
                Intent intent = new Intent(context, DispSingleLineMsgActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putString(EUIParamKeys.PROMPT_1.toString(), prompt);
                bundle.putString(EUIParamKeys.CONTENT.toString(), content);
                bundle.putInt(EUIParamKeys.TIKE_TIME.toString(), tiketime);
                intent.putExtras(bundle);
                context.startActivity(intent);

            }
        });

    }

}
