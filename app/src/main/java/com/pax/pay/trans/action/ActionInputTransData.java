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
import com.pax.edc.R;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.InputTransData12Activity;
import com.pax.pay.trans.action.activity.InputTransData1Activity;
import com.pax.pay.trans.action.activity.InputTransData2Activity;
import com.pax.pay.utils.ContextUtils;

import java.util.LinkedHashMap;

public class ActionInputTransData extends AAction {

    private String title;
    private String prompt1;
    private EInputType inputType1;
    private int maxLen1;
    private int minLen1;
    private String prompt2;
    private EInputType inputType2;
    private int maxLen2;
    private int minLen2;
    private int lineNum;
    private boolean isVoidLastTrans;
    private boolean isAuthZero;
    private LinkedHashMap<String, String> map;

    public ActionInputTransData(ActionStartListener listener, int lineNum) {
        super(listener);
        this.lineNum = lineNum;
    }

    public ActionInputTransData(ActionStartListener listener, String title, String prompt1, EInputType inputType1,
                                int maxLen1, int minLen1, String prompt2, EInputType inputType2,
                                int maxLen2, int minLen2, int lineNum, boolean isVoidLastTrans,
                                boolean isAuthZero, LinkedHashMap<String, String> map) {
        super(listener);
        this.title = title;
        this.prompt1 = prompt1;
        this.inputType1 = inputType1;
        this.maxLen1 = maxLen1;
        this.minLen1 = minLen1;
        this.prompt2 = prompt2;
        this.inputType2 = inputType2;
        this.maxLen2 = maxLen2;
        this.minLen2 = minLen2;
        this.lineNum = lineNum;
        this.isVoidLastTrans = isVoidLastTrans;
        this.isAuthZero = isAuthZero;
        this.map = map;
    }

    /**
     * 输入数据类型定义
     *
     * @author Steven.W
     */
    public enum EInputType {
        AMOUNT,
        DATE,
        NUM, // 数字
        ALPHNUM, // 数字加字母
        TEXT, // 所有类型
        PHONE,
        EMAIL,
    }

    public ActionInputTransData setParam(String title) {
        this.title = title;
        return this;
    }

    public ActionInputTransData setParam(String title, LinkedHashMap<String, String> map) {
        this.title = title;
        this.map = map;
        return this;
    }

    public ActionInputTransData setInputLine1(String prompt, EInputType inputType, int maxLen, boolean isVoidLastTrans) {
        return setInputLine1(prompt, inputType, maxLen, 0, isVoidLastTrans);
    }

    public ActionInputTransData setInputLine1(String prompt, EInputType inputType, int maxLen, int minLen,
                                              boolean isVoidLastTrans) {
        this.prompt1 = prompt;
        this.inputType1 = inputType;
        this.maxLen1 = maxLen;
        this.minLen1 = minLen;
        this.isVoidLastTrans = isVoidLastTrans;
        return this;
    }

    public ActionInputTransData setInputLine1(String prompt, EInputType inputType, int maxLen, int minLen,
                                              boolean isVoidLastTrans, boolean isAuthZero) {
        this.prompt1 = prompt;
        this.inputType1 = inputType;
        this.maxLen1 = maxLen;
        this.minLen1 = minLen;
        this.isVoidLastTrans = isVoidLastTrans;
        this.isAuthZero = isAuthZero;
        return this;
    }

    public ActionInputTransData setInputLine2(String prompt, EInputType inputType, int maxLen) {
        return setInputLine2(prompt, inputType, maxLen, 0);
    }

    public ActionInputTransData setInputLine2(String prompt, EInputType inputType, int maxLen, int minLen) {
        this.prompt2 = prompt;
        this.inputType2 = inputType;
        this.maxLen2 = maxLen;
        this.minLen2 = minLen;
        return this;
    }

    @Override
    protected void process() {

        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Context context = ContextUtils.getActyContext();
                if (lineNum == 1) {
                    Intent intent = new Intent(context, InputTransData1Activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                    bundle.putString(EUIParamKeys.PROMPT_1.toString(), prompt1);
                    bundle.putInt(EUIParamKeys.INPUT_MAX_LEN_1.toString(), maxLen1);
                    bundle.putInt(EUIParamKeys.INPUT_MIN_LEN_1.toString(), minLen1);
                    bundle.putSerializable(EUIParamKeys.INPUT_TYPE_1.toString(), inputType1);
                    bundle.putBoolean(EUIParamKeys.VOID_LAST_TRANS_UI.toString(), isVoidLastTrans);
                    bundle.putBoolean(EUIParamKeys.INPUT_AUTH_ZERO.toString(), isAuthZero);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                } else if (lineNum == 2) {
                    Intent intent = new Intent(context, InputTransData2Activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                    bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                    bundle.putString(EUIParamKeys.PROMPT_1.toString(), prompt1);
                    bundle.putInt(EUIParamKeys.INPUT_MAX_LEN_1.toString(), maxLen1);
                    bundle.putInt(EUIParamKeys.INPUT_MIN_LEN_1.toString(), minLen1);
                    bundle.putSerializable(EUIParamKeys.INPUT_TYPE_1.toString(), inputType1);
                    bundle.putString(EUIParamKeys.PROMPT_2.toString(), prompt2);
                    bundle.putInt(EUIParamKeys.INPUT_MAX_LEN_2.toString(), maxLen2);
                    bundle.putInt(EUIParamKeys.INPUT_MIN_LEN_2.toString(), minLen2);
                    bundle.putSerializable(EUIParamKeys.INPUT_TYPE_2.toString(), inputType2);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                } else if (lineNum == 4) {
                    Intent intent = new Intent(context, InputTransData12Activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                    bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                    bundle.putString(EUIParamKeys.PROMPT_1.toString(), prompt1);
                    bundle.putInt(EUIParamKeys.INPUT_MAX_LEN_1.toString(), maxLen1);
                    bundle.putInt(EUIParamKeys.INPUT_MIN_LEN_1.toString(), minLen1);
                    if (map != null) {
                        String totalAmount = map.get(context.getString(R.string.prompt_total_amount));
                        String oriTips = map.get(context.getString(R.string.prompt_ori_tips));
                        String adjustPercent = map.get(context.getString(R.string.prompt_adjust_percent));
                        bundle.putString(EUIParamKeys.TRANS_AMOUNT.toString(), totalAmount);
                        bundle.putString(EUIParamKeys.ORI_TIPS.toString(), oriTips);
                        bundle.putFloat(EUIParamKeys.TIP_PERCENT.toString(), Float.valueOf(adjustPercent));
                    }
                    bundle.putSerializable(EUIParamKeys.INPUT_TYPE_1.toString(), inputType1);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }

            }
        });
    }

    public static class Builder {

        private ActionStartListener startListener;
        private int transNameResId;
        private int prompt1ResId;
        private EInputType inputType1;
        private int maxLen1;
        private int minLen1;
        private int prompt2ResId;
        private EInputType inputType2;
        private int maxLen2;
        private int minLen2;
        private int lineNum;
        private boolean isVoidLastTrans;
        private boolean isAuthZero;
        private LinkedHashMap<String, String> paramMap;

        public Builder startListener(ActionStartListener startListener) {
            this.startListener = startListener;
            return this;
        }

        public Builder transName(int transNameId) {
            this.transNameResId = transNameId;
            return this;
        }

        public Builder inputType1(EInputType inputType1) {
            this.inputType1 = inputType1;
            return this;
        }

        public Builder prompt1(int prompt1ResId) {
            this.prompt1ResId = prompt1ResId;
            return this;
        }

        public Builder maxLen1(int maxLen1) {
            this.maxLen1 = maxLen1;
            return this;
        }

        public Builder minLen1(int minLen1) {
            this.minLen1 = minLen1;
            return this;
        }

        public Builder prompt2(int prompt2ResId) {
            this.prompt2ResId = prompt2ResId;
            return this;
        }

        public Builder inputType2(EInputType inputType2) {
            this.inputType2 = inputType2;
            return this;
        }

        public Builder maxLen2(int maxLen2) {
            this.maxLen2 = maxLen2;
            return this;
        }

        public Builder minLen2(int minLen2) {
            this.minLen2 = minLen2;
            return this;
        }

        public Builder lineNum(int lineNum) {
            this.lineNum = lineNum;
            return this;
        }

        public Builder isVoidLastTrans(boolean isVoidLastTrans) {
            this.isVoidLastTrans = isVoidLastTrans;
            return this;
        }

        public Builder isAuthZero(boolean isAuthZero) {
            this.isAuthZero = isAuthZero;
            return this;
        }

        public Builder paramMap(LinkedHashMap<String, String> paramMap) {
            this.paramMap = paramMap;
            return this;
        }

        public ActionInputTransData create() {

            String transName = ContextUtils.getString(transNameResId);
            String prompt1 = ContextUtils.getString(prompt1ResId);
            String prompt2 = ContextUtils.getString(prompt2ResId);

            return new ActionInputTransData(startListener, transName, prompt1, inputType1,
                    maxLen1, minLen1, prompt2, inputType2,
                    maxLen2, minLen2, lineNum, isVoidLastTrans, isAuthZero, paramMap);
        }
    }
}
