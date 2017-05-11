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

import com.pax.abl.core.AAction;
import com.pax.abl.utils.PanUtils;
import com.pax.abl.utils.PanUtils.EPanMode;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.EnterPinActivity;
import com.pax.pay.utils.ContextUtils;

public class ActionEnterPin extends AAction {

    /**
     * 脱机pin时返回的结果
     *
     * @author Steven.W
     */
    public static class OfflinePinResult {
        // SW1 SW2
        byte[] respOut;
        int ret;

        public byte[] getRespOut() {
            return respOut;
        }

        public void setRespOut(byte[] respOut) {
            this.respOut = respOut;
        }

        public int getRet() {
            return ret;
        }

        public void setRet(int ret) {
            this.ret = ret;
        }
    }

    public ActionEnterPin(ActionStartListener listener) {
        super(listener);
    }

    private String title;
    private String pan;
    private String header;
    private String subHeader;
    private String totalAmount;
    private String tipAmount;

    private boolean isSupportBypass;
    private EEnterPinType enterPinType;

    private ActionEnterPin(ActionStartListener listener, String title, String pan, String header,
                           String subHeader, String totalAmount, String tipAmount, boolean isSupportBypass,
                           EEnterPinType enterPinType) {
        super(listener);
        this.title = title;
        this.pan = pan;
        this.header = header;
        this.subHeader = subHeader;
        this.totalAmount = totalAmount;
        this.tipAmount = tipAmount; //AET-81
        this.isSupportBypass = isSupportBypass;
        this.enterPinType = enterPinType;
    }

    public void setParam(String title, String pan, boolean supportBypass, String header,
                         String subHeader, String totalAmount, String tipAmount, EEnterPinType enterPinType) {
        this.title = title;
        this.pan = pan;
        this.isSupportBypass = supportBypass;
        this.header = header;
        this.subHeader = subHeader;
        this.totalAmount = totalAmount;
        this.tipAmount = tipAmount; //AET-81
        this.enterPinType = enterPinType;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
    public void setTipAmount(String tipAmount) {
        this.tipAmount = tipAmount;
    }

    public enum EEnterPinType {
        ONLINE_PIN, // 联机pin
        OFFLINE_PLAIN_PIN, // 脱机明文pin
        OFFLINE_CIPHER_PIN, // 脱机密文pin
    }

    @Override
    protected void process() {
        Context context = ContextUtils.getActyContext();
        Intent intent = new Intent(context, EnterPinActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.PROMPT_1.toString(), header);
        intent.putExtra(EUIParamKeys.PROMPT_2.toString(), subHeader);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), totalAmount);
        intent.putExtra(EUIParamKeys.TIP_AMOUNT.toString(), tipAmount); //AET-81
        intent.putExtra(EUIParamKeys.ENTERPINTYPE.toString(), enterPinType);
        intent.putExtra(EUIParamKeys.PANBLOCK.toString(), PanUtils.getPanBlock(pan, EPanMode.X9_8_WITH_PAN));
        intent.putExtra(EUIParamKeys.SUPPORTBYPASS.toString(), isSupportBypass);
        context.startActivity(intent);
    }

    public static class Builder {

        ActionStartListener startListener;
        private int transNameResId;
        private String pan;
        private int prompt1ResId;
        private int prompt2ResId;
        private String totalAmount;
        private String tipAmount;
        private boolean isSupportBypass;
        private EEnterPinType enterPinType;

        public Builder startListener(ActionStartListener startListener) {
            this.startListener = startListener;
            return this;
        }

        public Builder transName(int transNameResId) {
            this.transNameResId = transNameResId;
            return this;
        }

        public Builder pan(String pan) {
            this.pan = pan;
            return this;
        }

        public Builder prompt1(int prompt1ResId) {
            this.prompt1ResId = prompt1ResId;
            return this;
        }

        public Builder prompt2(int prompt2ResId) {
            this.prompt2ResId = prompt2ResId;
            return this;
        }

        public Builder totalAmount(String totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder tipAmount(String tipAmount) {
            this.tipAmount = tipAmount;
            return this;
        }

        public Builder isSupportBypass(boolean isSupportBypass) {
            this.isSupportBypass = isSupportBypass;
            return this;
        }

        public Builder enterPinType(EEnterPinType enterPinType) {
            this.enterPinType = enterPinType;
            return this;
        }

        public ActionEnterPin create() {
            String transName = ContextUtils.getString(transNameResId);
            String prompt1 = ContextUtils.getString(prompt1ResId);
            String prompt2 = ContextUtils.getString(prompt2ResId);
            return new ActionEnterPin(startListener, transName, pan, prompt1,
                    prompt2, totalAmount, tipAmount, isSupportBypass, enterPinType);
        }
    }
}
