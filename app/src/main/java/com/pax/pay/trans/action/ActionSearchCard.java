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
import com.pax.dal.entity.EReaderType;
import com.pax.pay.base.Issuer;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.SearchCardActivity;
import com.pax.pay.utils.ContextUtils;

public class ActionSearchCard extends AAction {

    private byte mode;
    private String title;
    private String amount;
    private boolean hasTip;
    private String tipAmount;
    private String date;
    private String code;
    private String searchCardPrompt;
    private ESearchCardUIType searchCardUIType;

    public ActionSearchCard(ActionStartListener listener) {
        super(listener);
    }

    private ActionSearchCard(ActionStartListener listener, byte mode, String title,
                             String amount, String date, String code, String searchCardPrompt,
                             ESearchCardUIType searchCardUIType, boolean hasTip, String tipAmount) {
        super(listener);
        this.mode = mode;
        this.title = title;
        this.amount = amount;
        this.hasTip = hasTip;
        this.tipAmount = tipAmount;
        this.date = date;
        this.code = code;
        this.searchCardPrompt = searchCardPrompt;
        this.searchCardUIType = searchCardUIType;
    }

    /**
     * 寻卡类型定义
     *
     * @author Steven.W
     */
    public static class SearchMode {
        /**
         * 刷卡
         */
        public static final byte SWIPE = 0x01;
        /**
         * 插卡
         */
        public static final byte INSERT = 0x02;
        /**
         * 挥卡
         */
        public static final byte WAVE = 0x04;
        /**
         * 支持手输
         */
        public static final byte KEYIN = 0x08;

        public static final byte READ_MODE_ALL = (byte) (SWIPE | INSERT | WAVE | KEYIN);
        public static final byte READ_MODE_NONE = (byte) 0x00;

        public static byte include(byte mode, byte mode2) {
            return (byte) (mode | mode2);
        }

        public static byte exclude(byte mode, byte mode2) {
            return (byte) (mode & ~mode2);
        }

        public static boolean contain(byte mode, byte mode2) {
            return (mode & mode2) == mode2;
        }

        /**
         * SearchMode to EReaderType
         *
         * @param mode
         * @return
         */
        public static EReaderType toReaderType(byte mode) {
            mode = SearchMode.exclude(mode, SearchMode.KEYIN);
            EReaderType[] types = EReaderType.values();
            for (EReaderType type : types) {
                if (type.getEReaderType() == mode)
                    return type;
            }
            return null;
        }
    }

    public static class CardInformation {
        private byte searchMode;
        private String track1;
        private String track2;
        private String track3;
        private String pan;
        private String expDate;
        private Issuer issuer;

        public CardInformation(byte mode, String pan, String expDate, Issuer issuer) {
            this.searchMode = mode;
            this.pan = pan;
            this.expDate = expDate;
            this.issuer = issuer;
        }

        public CardInformation(byte mode, String track1, String track2, String track3, String pan, Issuer issuer) {
            this.searchMode = mode;
            this.track1 = track1;
            this.track2 = track2;
            this.track3 = track3;
            this.pan = pan;
            this.issuer = issuer;
        }

        public CardInformation(byte mode) {
            this.searchMode = mode;
        }

        public CardInformation(byte mode, String pan) {
            this.searchMode = mode;
            this.pan = pan;
        }

        public CardInformation() {
        }

        public byte getSearchMode() {
            return searchMode;
        }

        public void setSearchMode(byte searchMode) {
            this.searchMode = searchMode;
        }

        public String getTrack1() {
            return track1;
        }

        public void setTrack1(String track1) {
            this.track1 = track1;
        }

        public String getTrack2() {
            return track2;
        }

        public void setTrack2(String track2) {
            this.track2 = track2;
        }

        public String getTrack3() {
            return track3;
        }

        public void setTrack3(String track3) {
            this.track3 = track3;
        }

        public String getPan() {
            return pan;
        }

        public void setPan(String pan) {
            this.pan = pan;
        }

        public String getExpDate() {
            return expDate;
        }

        public void setExpDate(String expDate) {
            this.expDate = expDate;
        }

        public Issuer getIssuer() {
            return issuer;
        }

        public void setIssuer(Issuer issuer) {
            this.issuer = issuer;
        }
    }

    public enum ESearchCardUIType {
        DEFAULT,//默认
        QUICKPASS,//闪付
        EC,//电子现金
    }

    /**
     * 设置参数
     *
     * @param mode   ：读卡模式
     * @param amount ：交易模式
     */
    public void setParam(String title, byte mode, String amount, String date, String searchCardPrompt) {
        this.title = title;
        this.mode = mode;
        this.amount = amount;
        this.date = date;
        this.searchCardPrompt = searchCardPrompt;
    }

    /**
     * 设置参数
     *
     * @param mode   ：读卡模式
     * @param amount ：交易模式
     */
    public void setParam(String title, byte mode, String amount, String code, String date,
                         ESearchCardUIType searchCardUIType, String searchCardPrompt) {
        setParam(title, mode, amount, code, date, searchCardUIType, searchCardPrompt, false, "0");
    }

    public void setParam(String title, byte mode, String amount, String tipAmount, String code, String date,
                         ESearchCardUIType searchCardUIType, String searchCardPrompt) {
        setParam(title, mode, amount, code, date, searchCardUIType, searchCardPrompt, true, tipAmount);
    }

    public void setParam(String title, byte mode, String amount, String code, String date,
                         ESearchCardUIType searchCardUIType, String searchCardPrompt, boolean hasTip, String tipAmount) {
        this.title = title;
        this.mode = mode;
        this.amount = amount;
        this.code = code;
        this.date = date;
        this.searchCardUIType = searchCardUIType;
        this.searchCardPrompt = searchCardPrompt;

        this.hasTip = hasTip;
        this.tipAmount = tipAmount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setTipAmount(String tipAmount) {
        this.tipAmount = tipAmount;
    }

    @Override
    protected void process() {
        Context context = ContextUtils.getActyContext();
        Intent intent = new Intent(context, SearchCardActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.NAV_BACK.toString(), true);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        intent.putExtra(EUIParamKeys.HAS_TIP.toString(), hasTip);
        intent.putExtra(EUIParamKeys.TIP_AMOUNT.toString(), tipAmount);
        intent.putExtra(EUIParamKeys.CARD_SEARCH_MODE.toString(), mode);
        intent.putExtra(EUIParamKeys.AUTH_CODE.toString(), code);
        intent.putExtra(EUIParamKeys.TRANS_DATE.toString(), date);
        intent.putExtra(EUIParamKeys.SEARCH_CARD_UI_TYPE.toString(), searchCardUIType);
        intent.putExtra(EUIParamKeys.SEARCH_CARD_PROMPT.toString(), searchCardPrompt);
        context.startActivity(intent);
    }

    public static class Builder {

        private ActionStartListener startListener;
        private byte cardReadMode;
        private int transNameResId;
        private String transAmount;
        private boolean hasTip;
        private String tipAmount;
        private String transDate;
        private String authCode;
        private int promptResId;
        private ESearchCardUIType searchCardUIType;

        public Builder startListener(ActionStartListener startListener) {
            this.startListener = startListener;
            return this;
        }

        public Builder cardReadMode(byte cardReadMode) {
            this.cardReadMode = cardReadMode;
            return this;
        }

        public Builder transName(int transNameResId) {
            this.transNameResId = transNameResId;
            return this;
        }

        public Builder transAmount(String transAmount) {
            this.transAmount = transAmount;
            return this;
        }

        public Builder hasTip(boolean hasTip) {
            this.hasTip = hasTip;
            return this;
        }

        public Builder tipAmount(String tipAmount) {
            this.tipAmount = tipAmount;
            return this;
        }

        public Builder transDate(String transDate) {
            this.transDate = transDate;
            return this;
        }

        public Builder authCode(String authCode) {
            this.authCode = authCode;
            return this;
        }

        public Builder searchCardPrompt(int promptResId) {
            this.promptResId = promptResId;
            return this;
        }

        public Builder searchCardUIType(ESearchCardUIType searchCardUIType) {
            this.searchCardUIType = searchCardUIType;
            return this;
        }

        public ActionSearchCard create() {

            String transName = ContextUtils.getString(transNameResId);
            String searchCardPrompt = ContextUtils.getString(promptResId);

            return new ActionSearchCard(startListener, cardReadMode, transName,
                    transAmount, transDate, authCode, searchCardPrompt,
                    searchCardUIType, hasTip, tipAmount);
        }
    }
}
