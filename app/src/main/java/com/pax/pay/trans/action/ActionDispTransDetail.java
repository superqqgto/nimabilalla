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
import com.pax.pay.trans.action.activity.DispTransDetailActivity;
import com.pax.pay.utils.ContextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class ActionDispTransDetail extends AAction {

    private String title;
    private LinkedHashMap<String, String> map;

    public ActionDispTransDetail(ActionStartListener listener) {
        super(listener);
    }

    private ActionDispTransDetail(ActionStartListener listener, String title, LinkedHashMap<String, String> map) {
        super(listener);
        this.title = title;
        this.map = map;
    }

    /**
     * 参数设置
     *
     * @param title   ：抬头
     * @param map     ：确认信息
     */
    public void setParam(String title, LinkedHashMap<String, String> map) {
        this.title = title;
        this.map = map;
    }

    public void setMap(LinkedHashMap<String, String> map) {
        this.map=map;
    }

    @Override
    protected void process() {

        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ArrayList<String> leftColumns = new ArrayList<>();
                ArrayList<String> rightColumns = new ArrayList<>();

                Set<String> keys = map.keySet();
                for (String key : keys) {
                    leftColumns.add(key);
                    Object value = map.get(key);
                    if (value != null) {
                        rightColumns.add((String) value);
                    } else {
                        rightColumns.add("");
                    }

                }

                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString(), leftColumns);
                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), rightColumns);

                Context context=ContextUtils.getActyContext();
                Intent intent = new Intent(context, DispTransDetailActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    public static class Builder {
        private ActionStartListener startListener;
        private int transNameResId;
        private LinkedHashMap<String, String> paramMap;

        public Builder() {
            paramMap = new LinkedHashMap<String, String>();
        }

        public Builder startListener(ActionStartListener startListener) {
            this.startListener = startListener;
            return this;
        }

        public Builder transName(int transNameResId) {
            this.transNameResId = transNameResId;
            return this;
        }

        public Builder param(int key, String value) {
            String keyStr = ContextUtils.getString(key);
            paramMap.put(keyStr, value);
            return this;
        }

        public ActionDispTransDetail create() {
            String transName = ContextUtils.getString(transNameResId);
            return new ActionDispTransDetail(startListener, transName, paramMap);
        }
    }
}
