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
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.manager.DbManager;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.activity.SettleActivity;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.utils.ContextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ActionSettle extends AAction {

    public ActionSettle(ActionStartListener listener) {
        super(listener);
    }

    private String title;
    private LinkedHashMap<String, Object> map;
    private TransTotal total;
    private ArrayList<String> list;
    public static final String TAG = ActionSettle.class.getSimpleName();

    public void setParam(String title, LinkedHashMap<String, Object> map,
                         TransTotal total) {
        this.title = title;
        this.map = map;
        this.total = total;
    }

    public void setParam(String title, ArrayList<String> list) {
        this.title = title;
        this.list = list;
    }

    @Override
    protected void process() {
        Log.e(TAG, "process: " );
        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (DbManager.getTransDao().countOf() == 0) {
                    setResult(new ActionResult(TransResult.ERR_NO_TRANS, null));
                    return;
                }
                Context context = ContextUtils.getActyContext();
                Intent intent = new Intent(context, SettleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), list);

                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

    }
}
