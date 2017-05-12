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
package com.pax.pay.trans.model;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.edc.R;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.pack.PackBatchUp;
import com.pax.pay.trans.pack.PackBatchUpNotice;
import com.pax.pay.trans.pack.PackFinancial.PackAdjust;
import com.pax.pay.trans.pack.PackFinancial.PackAuth;
import com.pax.pay.trans.pack.PackFinancial.PackDcc;
import com.pax.pay.trans.pack.PackFinancial.PackInstalment;
import com.pax.pay.trans.pack.PackFinancial.PackMotoPreAuth;
import com.pax.pay.trans.pack.PackFinancial.PackMotoPreAuthCancel;
import com.pax.pay.trans.pack.PackFinancial.PackMotoPreAuthComp;
import com.pax.pay.trans.pack.PackFinancial.PackMotoPreAuthCompCancel;
import com.pax.pay.trans.pack.PackFinancial.PackOfflineTransSend;
import com.pax.pay.trans.pack.PackFinancial.PackPreAuthCancel;
import com.pax.pay.trans.pack.PackFinancial.PackPreAuthComp;
import com.pax.pay.trans.pack.PackFinancial.PackPreAuthCompCancel;
import com.pax.pay.trans.pack.PackFinancial.PackPreAuthCompOffline;
import com.pax.pay.trans.pack.PackFinancial.PackRefund;
import com.pax.pay.trans.pack.PackFinancial.PackReversal;
import com.pax.pay.trans.pack.PackFinancial.PackSale;
import com.pax.pay.trans.pack.PackFinancial.PackSaleVoid;
import com.pax.pay.trans.pack.PackFinancial.PackVoidRefund;
import com.pax.pay.trans.pack.PackIso8583;
import com.pax.pay.trans.pack.PackOfflineBat;
import com.pax.pay.trans.pack.PackSessionMaintain.PackEcho;
import com.pax.pay.trans.pack.PackSessionMaintain.PackLogon;
import com.pax.pay.trans.pack.PackSessionMaintain.PackTMKDownload;
import com.pax.pay.trans.pack.PackSettle;
import com.pax.pay.utils.ContextUtils;

public enum ETransType {

    /************************************************
     * 管理类
     ****************************************************/
    SETTLE() {
        @Override
        Builder initParam() {
             return new Builder()
                    .setMsgType("0500")
                    .setProcCode("920000")
                    .setTransNameResId(R.string.trans_settle)
                    .setReadMode(SearchMode.READ_MODE_NONE)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackSettle(listener);
        }
    },

    BATCH_UP() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0320")
                    .setProcCode("000001")
                    .setTransNameResId(R.string.trans_batch_up)
                    .setReadMode(SearchMode.READ_MODE_NONE)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackBatchUp(listener);
        }
    },


    REFUND_BAT() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0320")
                    .setProcCode("200000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_batch_up)
                    .setReadMode(SearchMode.READ_MODE_NONE)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackBatchUpNotice(listener);
        }
    },

    SETTLE_END() {
        @Override
        Builder initParam() {
             return new Builder()
                    .setMsgType("0500")
                    .setProcCode("960000")
                    .setTransNameResId(R.string.trans_settle_end)
                    .setReadMode(SearchMode.READ_MODE_NONE)
                    ;

        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackSettle(listener);
        }
    },

    /************************************************
     * financial function
     *
     ****************************************************/
    /**********************************************************************************************************/
    SALE() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("000000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_sale)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsAdjustAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackSale(listener);
        }
    },

    /**********************************************************************************************************/

    VOID() {
        @Override
        Builder initParam() {
              return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("200000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_void)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsSymbolNegative(true)
                    .setIsPackReversal(true)
                    ;

        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackSaleVoid(listener);
        }
    },

    /**********************************************************************************************************/
    /**********************************************************************************************************/

    MOTOVOID() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("200000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_moto_void)
                    .setReadMode(SearchMode.KEYIN)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsSymbolNegative(true)
                    .setIsPackReversal(true)
                    ;

        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackSaleVoid(listener);
        }
    },
    REFUND() {
        @Override
        Builder initParam() {
             return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0420")
                    .setProcCode("200000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_refund)
                    .setReadMode((byte)(SearchMode.SWIPE | SearchMode.KEYIN))
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsSymbolNegative(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackRefund(listener);
        }
    },

    /**********************************************************************************************************/
    VOIDREFUND() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("220000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_void_refund)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsSymbolNegative(true)
                    .setIsPackReversal(true)
                    ;

        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackVoidRefund(listener);
        }
    },
    /**********************************************************************************************************/
    MOTOREFUND() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0420")
                    .setProcCode("200000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_moto_refund)
                    .setReadMode(SearchMode.KEYIN)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsAdjustAllowed(true)
                    .setIsSymbolNegative(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackRefund(listener);
        }
    },

    /**********************************************************************************************************/
    ADJUST() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0220")
                    .setDupMsgType("")
                    .setProcCode("000000")
                    .setServiceCode("")
                    .setTransNameResId(R.string.trans_adjust)
                    .setReadMode(SearchMode.READ_MODE_NONE)
                    .setIsOfflineSendAllowed(true)
                    ;
            }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackAdjust(listener);
        }
    },

    /**********************************************************************************************************/
    PREAUTH() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0100")
                    .setDupMsgType("0400")
                    .setProcCode("030000")
                    .setServiceCode("06")
                    .setTransNameResId(R.string.trans_preAuth)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackAuth(listener);
        }

    },

    /**
     * Preauth completion
     */
    PREAUTH_COMP() {
        @Override
        Builder initParam() {
             return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("000000")
                    .setServiceCode("99")
                    .setTransNameResId(R.string.trans_preauth_comp)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackPreAuthComp(listener);
        }
    },

    PREAUTH_CANCEL(){
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0100")
                    .setDupMsgType("0400")
                    .setProcCode("200000")
                    .setServiceCode("99")
                    .setTransNameResId(R.string.trans_preauth_cancel)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackPreAuthCancel(listener);
        }
    },

    PREAUTH_COMP_CANCEL() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("020000")
                    .setTransNameResId(R.string.trans_preauth_comp_cancel)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackPreAuthCompCancel(listener);
        }
    },

    PREAUTH_COMP_OFFLINE() {
        @Override
        Builder initParam() {
             return new Builder()
                    .setMsgType("0220")
                    .setDupMsgType("0400")
                    .setProcCode("000000")
                    .setTransNameResId(R.string.trans_preauth_comp_offline)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackPreAuthCompOffline(listener);
        }
    },

    MOTO_PREAUTH() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0100")
                    .setDupMsgType("0400")
                    .setProcCode("030000")
                    .setServiceCode("99")
                    .setTransNameResId(R.string.trans_moto_preAuth)
                    .setReadMode(SearchMode.KEYIN)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackMotoPreAuth(listener);
        }
    },


    /**
     * Preauth completion
     */
    MOTO_PREAUTH_COMP() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("000000")
                    .setServiceCode("99")
                    .setTransNameResId(R.string.trans_moto_preauth_comp)
                    .setReadMode(SearchMode.KEYIN)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackMotoPreAuthComp(listener);
        }
    },

    MOTO_PREAUTH_CANCEL() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0100")
                    .setDupMsgType("0400")
                    .setProcCode("200000")
                    .setServiceCode("99")
                    .setTransNameResId(R.string.trans_preauth_cancel)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackMotoPreAuthCancel(listener);
        }
    },

    MOTO_PREAUTH_COMP_CANCEL() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("020000")
                    .setTransNameResId(R.string.trans_preauth_comp_cancel)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackMotoPreAuthCompCancel(listener);
        }
    },

    /***************************************************************************************************************/
    READCARDNO() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setProcCode("000000")
                    .setTransNameResId(R.string.trans_readCard)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return null;
        }
    },

    /***************************************************************************************************************/

    OFFLINE_TRANS_SEND() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0220")
                    .setProcCode("000000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_offline_send)
                    .setReadMode((byte)(SearchMode.SWIPE | SearchMode.KEYIN))
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackOfflineTransSend(listener);
        }
    },

    /***************************************************************************************************************/

    OFFLINE_TRANS_SEND_BAT(){
        @Override
        Builder initParam() {
             return new Builder()
                    .setMsgType("0320")
                    .setProcCode("000000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_offline_send_bat)
                    .setReadMode(SearchMode.READ_MODE_NONE)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackOfflineBat(listener);
        }
    },

    /***************************************************************************************************************/

    DCC() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0600")
                    .setDupMsgType("0400")
                    .setProcCode("000000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_dcc)
                    .setReadMode((byte)(SearchMode.INSERT | SearchMode.WAVE))
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackDcc(listener);
        }
    },

    CASH_ADVANCE(){
        @Override
        Builder initParam() {
             return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("010000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_cash_advance)
                    .setReadMode(SearchMode.READ_MODE_ALL)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsPackReversal(true)
                     ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackDcc(listener);
        }
    },


    INSTALMENT() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("000000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_instalment)
                    .setReadMode(SearchMode.INSERT)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackInstalment(listener);
        }
    },


    MOTOSALE() {
        @Override
        Builder initParam() {
             return new Builder()
                    .setMsgType("0200")
                    .setDupMsgType("0400")
                    .setProcCode("000000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_moto_sale)
                    .setReadMode(SearchMode.KEYIN)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    .setIsOfflineSendAllowed(true)
                    .setIsVoidAllowed(true)
                    .setIsAdjustAllowed(true)
                    .setIsPackReversal(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackSale(listener);
        }
    },

    /************************************************
     * session Maintenance
     ****************************************************/
    /***************************************************************************************************************/

    TMK_DOWNLOAD() {
        @Override
        Builder initParam() {
              return new Builder()
                    .setMsgType("8000")
                    .setProcCode("810000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_tmk_download)
                    .setReadMode(SearchMode.READ_MODE_NONE)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackTMKDownload(listener);
        }
    },

    LOGON() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("8000")
                    .setProcCode("920000")
                    .setServiceCode("00")
                    .setTransNameResId(R.string.trans_logon)
                    .setIsDupSendAllowed(true)
                    .setIsScriptSendAllowed(true)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackLogon(listener);
        }
    },

    ECHO() {
        @Override
        Builder initParam() {
            return new Builder()
                    .setMsgType("8000")
                    .setProcCode("990000")
                    .setTransNameResId(R.string.trans_echo)
                    .setReadMode(SearchMode.READ_MODE_NONE)
                    ;
        }

        @Override
        public PackIso8583 getPackager(PackListener listener) {
            return new PackEcho(listener);
        }
    };

    ETransType(){
        setDefaultParams();
        setParamFromBuilder(initParam());
    }

    private void setParamFromBuilder(Builder builder) {
        this.msgType = builder.msgType;
        this.dupMsgType = builder.dupMsgType;
        this.procCode = builder.procCode;
        this.serviceCode = builder.serviceCode;
        this.transNameResId = builder.transNameResId;
        this.readMode = builder.readMode;
        this.isDupSendAllowed = builder.isDupSendAllowed;
        this.isScriptSendAllowed = builder.isScriptSendAllowed;
        this.isOfflineSendAllowed = builder.isOfflineSendAllowed;
        this.isVoidAllowed = builder.isVoidAllowed;
        this.isAdjustAllowed = builder.isAdjustAllowed;
        this.isSymbolNegative = builder.isSymbolNegative;
        this.isPackReversal = builder.isPackReversal;

    }

    private void setDefaultParams() {
        this.msgType = "";
        this.dupMsgType = "";
        this.procCode = "";
        this.serviceCode = "";
        this.transNameResId = R.string.default_transName;
        this.readMode = SearchMode.READ_MODE_NONE;
        this.isDupSendAllowed = false;
        this.isScriptSendAllowed = false;
        this.isOfflineSendAllowed = false;
        this.isVoidAllowed = false;
        this.isAdjustAllowed = false;
        this.isSymbolNegative = false;
        this.isPackReversal = false;
    }

    abstract Builder initParam();

    /**
     * @param msgType              ：消息类型码
     * @param dupMsgType           :冲正消息类型码
     * @param procCode             : 处理码
     * @param serviceCode          ：服务码
     * @param transNameResId       :交易名称  的resId
     * @param isDupSendAllowed     ：是否冲正上送
     * @param isScriptSendAllowed  ：是否脚本结果上送
     * @param isOfflineSendAllowed ：是否脱机交易上送
     * @param isVoidAllowed        : is allowed to void
     * @param readMode             : read mode
     * @param isAdjustAllowed      ：is allowed to adjust
     * @param isSymbolNegative     : is symbol negative 金额符合正负
     * @param isPackReversal
     */

    private String msgType;
    private String dupMsgType;
    private String procCode;
    private String serviceCode;
    private int transNameResId;
    private byte readMode;
    private boolean isDupSendAllowed;
    private boolean isScriptSendAllowed;
    private boolean isOfflineSendAllowed;
    private boolean isVoidAllowed;
    private boolean isAdjustAllowed;
    private boolean isSymbolNegative;
    private boolean isPackReversal;//getDupPackager是返回new PackReversal(listener)还是null

    public String getMsgType() {
        return msgType;
    }

    public String getDupMsgType() {
        return dupMsgType;
    }

    public String getProcCode() {
        return procCode;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public String getTransName() {
        return ContextUtils.getString(transNameResId);
    }

    public int getTransNameResId() {
        return transNameResId;
    }

    public byte getReadMode() {
        byte mode = this.readMode;
        if (mode == SearchMode.READ_MODE_NONE) {
            return mode;
        }
        // does not support manual input
        if (!SpManager.getSysParamSp().get(SysParamSp.EDC_SUPPORT_KEYIN).equals(SysParamSp.Constant.YES)) {
            mode = SearchMode.exclude(mode, SearchMode.KEYIN);
        }
        return mode;
    }

    public boolean isDupSendAllowed() {
        return isDupSendAllowed;
    }

    public boolean isScriptSendAllowed() {
        return isScriptSendAllowed;
    }

    public boolean isOfflineSendAllowed() {
        return isOfflineSendAllowed;
    }

    public boolean isVoidAllowed() {
        return isVoidAllowed;
    }

    public boolean isAdjustAllowed() {
        return isAdjustAllowed;
    }

    public boolean isSymbolNegative() {
        return isSymbolNegative;
    }

    public abstract PackIso8583 getPackager(PackListener listener);

    public PackIso8583 getDupPackager(PackListener listener) {
        if (isPackReversal) {
            return new PackReversal(listener);
        }
        return null;
    }

    //linzhao
    public static class Builder {
        private String msgType;
        private String dupMsgType;
        private String procCode;
        private String serviceCode;
        private int transNameResId;
        private byte readMode;
        private boolean isDupSendAllowed;
        private boolean isScriptSendAllowed;
        private boolean isOfflineSendAllowed;
        private boolean isVoidAllowed;
        private boolean isAdjustAllowed;
        private boolean isSymbolNegative;
        private boolean isPackReversal;

        public Builder setDupMsgType(String dupMsgType) {
            this.dupMsgType = dupMsgType;
            return this;
        }

        public Builder setProcCode(String procCode) {
            this.procCode = procCode;
            return this;
        }

        public Builder setServiceCode(String serviceCode) {
            this.serviceCode = serviceCode;
            return this;
        }

        public Builder setTransNameResId(int transNameResId) {
            this.transNameResId = transNameResId;
            return this;
        }

        public Builder setReadMode(byte readMode) {
            this.readMode = readMode;
            return this;
        }

        public Builder setIsDupSendAllowed(boolean dupSendAllowed) {
            this.isDupSendAllowed = dupSendAllowed;
            return this;
        }

        public Builder setIsScriptSendAllowed(boolean scriptSendAllowed) {
            this.isScriptSendAllowed = scriptSendAllowed;
            return this;
        }

        public Builder setIsOfflineSendAllowed(boolean offlineSendAllowed) {
            this.isOfflineSendAllowed = offlineSendAllowed;
            return this;
        }

        public Builder setIsVoidAllowed(boolean voidAllowed) {
            this.isVoidAllowed = voidAllowed;
            return this;
        }

        public Builder setIsAdjustAllowed(boolean adjustAllowed) {
            this.isAdjustAllowed = adjustAllowed;
            return this;
        }

        public Builder setIsSymbolNegative(boolean symbolNegative) {
            this.isSymbolNegative = symbolNegative;
            return this;
        }

        public Builder setIsPackReversal(boolean packReversal) {
            this.isPackReversal = packReversal;
            return this;
        }

        public Builder setMsgType(String msgType) {
            this.msgType = msgType;
            return this;
        }
    }
}