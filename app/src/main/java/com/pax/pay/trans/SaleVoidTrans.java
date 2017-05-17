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
package com.pax.pay.trans;

import android.annotation.SuppressLint;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.abl.utils.PanUtils;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionOfflineSend;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSelectDccAmount;
import com.pax.pay.trans.action.ActionSendEmail;
import com.pax.pay.trans.action.ActionSendSMS;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.BaseTransData.*;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.TimeConverter;
import com.pax.manager.sp.SysParamSp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class SaleVoidTrans extends BaseTrans {

    private TransData origTransData;
    private String origTransNo;
    private String amountOrg;
    private String amount;
    /**
     * whether need to read the original trans data or not
     */
    private boolean isNeedFindOrigTrans = true;
    /**
     * whether need to input trans no. or not
     */
    private boolean isNeedInputTransNo = true;


    public SaleVoidTrans(TransEndListener transListener) {
        super(ETransType.VOID, transListener);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }

    public SaleVoidTrans(TransData origTransData, TransEndListener transListener) {
        super(ETransType.VOID, transListener);
        this.origTransData = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;
    }

    public SaleVoidTrans(String origTransNo, TransEndListener transListener) {
        super(ETransType.VOID, transListener);
        this.origTransNo = origTransNo;
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = false;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void bindStateOnAction() {

        ActionInputPassword inputPasswordAction = new ActionInputPassword(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(6,
                        ContextUtils.getString(R.string.prompt_void_pwd), null);
            }
        });
        bind(SaleVoidTrans.State.INPUT_PWD.toString(), inputPasswordAction);

        ActionInputTransData enterTransNoAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(ContextUtils.getString(R.string.trans_void))
                        .setInputLine1(ContextUtils.getString(R.string.prompt_input_transno), EInputType.NUM, 6, true);
            }
        }, 1);
        bind(SaleVoidTrans.State.ENTER_TRANSNO.toString(), enterTransNoAction);

        //Zac added ONLINE_DCC ENQUIRE
        // online action
        ActionTransOnline transOnlineAction_dcc = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });

        bind(SaleVoidTrans.State.ONLINE_DCC.toString(), transOnlineAction_dcc);
        // ENQUIRE
        ActionSelectDccAmount EnquireAction = new ActionSelectDccAmount(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
//                DccTransData dccTransData = transData.getDccTransData();
                String transName = ContextUtils.getString(R.string.trans_void);
//                String amount = CurrencyConverter.convert(Long.parseLong(origTransData.getAmount()), transData.getCurrency());
                amount = CurrencyConverter.convert(Long.parseLong(origTransData.getAmount()), transData.getCurrency()) + "HKD";
                int i = 0;
                String mutiAmount;
                LinkedHashMap<String, String> map = new LinkedHashMap<>();
//                map.put(ContextUtils.getString(R.string.DCC_TransType), ContextUtils.getString(R.string.trans_dcc));
                map.put(ContextUtils.getString(R.string.DCC_TotalAmount), amount);
                Iterator iter1 = transData.getDccTransData().getTransAmtList().iterator();
                Iterator iter2 = transData.getDccTransData().getCurrencyList().iterator();
                Iterator iter3 = transData.getDccTransData().getConvRateList().iterator();
                while (iter1.hasNext()) {
//                    mutiAmount = iter1.next() + " " + iter2.next() + " " + "Rate:" + iter3.next();
                    mutiAmount = iter1.next() + " " + iter2.next();
                    map.put(Integer.toString(i), mutiAmount);
                    i++;
                }
                ((ActionSelectDccAmount) action).setParam(transName, map);

            }
        });
        bind(SaleVoidTrans.State.ENQUIRE.toString(), EnquireAction);
        // confirm information
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {

                String transType = origTransData.getTransType().getTransName();
                String amount;
                if (SpManager.getSysParamSp().get(SysParamSp.SUPPORT_DCC).equals(SysParamSp.Constant.YES)) {
                    amount = amountOrg;
                } else {
                    amount = CurrencyConverter.convert(Long.parseLong(origTransData.getAmount()), transData.getCurrency());
                }


                transData.setEnterMode(origTransData.getEnterMode());
                transData.setTrack2(origTransData.getTrack2());
                transData.setTrack3(origTransData.getTrack3());

                // date and time
                //AET-95
                String formattedDate = TimeConverter.convert(origTransData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                        Constants.TIME_PATTERN_DISPLAY);

                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                map.put(ContextUtils.getString(R.string.history_detail_type), transType);
                map.put(ContextUtils.getString(R.string.history_detail_amount), amount);
                map.put(ContextUtils.getString(R.string.history_detail_card_no), PanUtils.maskCardNo(origTransData.getPan(), origTransData.getIssuer().getPanMaskPattern()));
                map.put(ContextUtils.getString(R.string.history_detail_auth_code), origTransData.getAuthCode());
                map.put(ContextUtils.getString(R.string.history_detail_ref_no), origTransData.getRefNo());
                map.put(ContextUtils.getString(R.string.history_detail_trace_no), Component.getPaddedNumber(origTransData.getTraceNo(), 6));
                map.put(ContextUtils.getString(R.string.dateTime), formattedDate);
                ((ActionDispTransDetail) action).setParam(ContextUtils.getString(R.string.trans_void), map);
            }
        });
        bind(SaleVoidTrans.State.TRANS_DETAIL.toString(), confirmInfoAction);

        // online action
        ActionTransOnline transOnlineAction = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });

        bind(SaleVoidTrans.State.ONLINE.toString(), transOnlineAction);
        // signature action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setAmount(transData.getAmount());
            }
        });
        bind(SaleVoidTrans.State.SIGNATURE.toString(), signatureAction);
        //offline send
        ActionOfflineSend offlineSendAction = new ActionOfflineSend(null);
        bind(SaleVoidTrans.State.OFFLINE_SEND.toString(), offlineSendAction);
        //print preview action
        ActionPrintPreview printPreviewAction = new ActionPrintPreview(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionPrintPreview) action).setTransData(transData);
            }
        });
        bind(State.PRINT_PREVIEW.toString(), printPreviewAction);
        // print action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintTransReceipt) action).setTransData(transData);
                    }
                });
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        //AET-118
        // get Telephone num
        ActionInputTransData phoneAction = new ActionInputTransData(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.paperless);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_phone_number), EInputType.PHONE, 20, false);
            }
        }, 1);
        bind(State.ENTER_PHONE_NUM.toString(), phoneAction);

        // get Telephone num
        ActionInputTransData emailAction = new ActionInputTransData(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.paperless);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_email_address), EInputType.EMAIL, 100, false);
            }
        }, 1);
        bind(State.ENTER_EMAIL.toString(), emailAction);

        ActionSendSMS sendSMSAction = new ActionSendSMS(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSendSMS) action).setParam(transData);
            }
        });
        bind(State.SEND_SMS.toString(), sendSMSAction);

        ActionSendEmail sendEmailAction = new ActionSendEmail(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSendEmail) action).setParam(transData);
            }
        });
        bind(State.SEND_EMAIL.toString(), sendEmailAction);

        // whether void trans need to input password or not
        if (SpManager.getSysParamSp().get(SysParamSp.OTHTC_VERIFY).equals(SysParamSp.Constant.YES)) {
            gotoState(State.INPUT_PWD.toString());
        } else if (isNeedInputTransNo) {// need to input trans no.
            gotoState(State.ENTER_TRANSNO.toString());
        } else {// not need to input trans no.
            if (isNeedFindOrigTrans) {
                validateOrigTransData(Long.parseLong(origTransNo));
            } else { // not need to read trans data
                copyOrigTransData();
                checkCardAndPin();
            }
        }
    }

    enum State {
        INPUT_PWD,
        ONLINE_DCC,
        ENQUIRE,
        ENTER_TRANSNO,
        TRANS_DETAIL,
        ONLINE,
        SIGNATURE,
        OFFLINE_SEND,
        PRINT_PREVIEW,
        PRINT_TICKET,
        //AET-118
        ENTER_PHONE_NUM,
        ENTER_EMAIL,
        SEND_SMS,
        SEND_EMAIL,
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        if ((state != State.SIGNATURE) && (state != State.PRINT_PREVIEW)) {
            // check action result，if failed，end the trans.
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case INPUT_PWD:
                String data = EncUtils.SHA1((String) result.getData());
                if (!data.equals(SpManager.getSysParamSp().get(SysParamSp.SEC_VOIDPWD))) {
                    transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
                    return;
                }

                if (isNeedInputTransNo) {// need to input trans no.
                    gotoState(State.ENTER_TRANSNO.toString());
                } else {// not need to input trans no.
                    if (isNeedFindOrigTrans) {
                        validateOrigTransData(Long.parseLong(origTransNo));
                    } else { // not need to read trans data
                        copyOrigTransData();
                        checkCardAndPin();
                    }
                }
                break;
            case ENTER_TRANSNO:
                String content = (String) result.getData();
                long transNo = 0;
                if (content == null) {
                    TransData transData = DbManager.getTransDao().findLastTransData();
                    if (transData == null) {
                        transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                        return;
                    }
                    transNo = transData.getTraceNo();
                } else {
                    transNo = Long.parseLong(content);
                }
                validateOrigTransData(transNo);
                break;
            case ONLINE_DCC:
                gotoState(SaleVoidTrans.State.ENQUIRE.toString());
                break;
            case ENQUIRE:
                String dccbackdata = (String) result.getData();
                Iterator iter1 = transData.getDccTransData().getTransAmtList().iterator();
                Iterator iter2 = transData.getDccTransData().getCurrencyList().iterator();
                Iterator iter3 = transData.getDccTransData().getConvRateList().iterator();
                String dccConvRate;
                String dccCurrency;
                String dccTransAmt;
                String amountSelect;
                if (dccbackdata.equals(amount)) {
                    amountOrg = dccbackdata;
                    transData.getDccTransData().setDccOptIn(false);
                } else {
                    transData.getDccTransData().setDccOptIn(true);
                    while (iter2.hasNext()) {
                        dccTransAmt = (String) iter1.next();
                        dccCurrency = (String) iter2.next();
                        dccConvRate = (String) iter3.next();
                        amountSelect = dccTransAmt + " " + dccCurrency + " " + "Rate:" + dccConvRate;
                        if (amountSelect.equals(dccbackdata)) {
                            transData.getDccTransData().setDccConvRate(dccConvRate);
                            transData.getDccTransData().setDccCurrency(dccCurrency);
                            transData.getDccTransData().setDccTransAmt(dccTransAmt);
                            break;
                        }
                    }
                    amountOrg = transData.getDccTransData().getDccTransAmt() + transData.getDccTransData().getDccCurrency();
                }
                transData.setTransType(ETransType.VOID);
                gotoState(State.TRANS_DETAIL.toString());
                break;
            case TRANS_DETAIL:
                //checkCardAndPin();
                // online
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE: //  subsequent processing of online
                // update original trans data
                origTransData.setTransState(ETransStatus.VOIDED);
                DbManager.getTransDao().updateTransData(origTransData);
                gotoState(State.SIGNATURE.toString());

                break;
            case SIGNATURE:
                // save signature data
                byte[] signData = (byte[]) result.getData();
                if (signData != null && signData.length > 0) {
                    transData.setSignData(signData);
                    // update trans data，save signature
                    DbManager.getTransDao().updateTransData(transData);
                }

                //get offline trans data list
                List<ETransType> list = new ArrayList<>();
                list.add(ETransType.OFFLINE_TRANS_SEND);

                List<TransData.ETransStatus> filter = new ArrayList<>();
                filter.add(ETransStatus.VOIDED);
                filter.add(ETransStatus.ADJUSTED);

                List<TransData> offlineTransList = DbManager.getTransDao().findTransData(list, filter);
                if (offlineTransList != null) {
                    if (offlineTransList.size() != 0) {
                        //offline send
                        gotoState(State.OFFLINE_SEND.toString());
                        break;
                    }
                }
                // if terminal does not support signature ,card holder does not sign or time out，print preview directly.
                gotoState(State.PRINT_PREVIEW.toString());
                break;
            case OFFLINE_SEND:
                gotoState(State.PRINT_PREVIEW.toString());
                break;
            case PRINT_PREVIEW:
                //AET-118
                goPrintBranch(result);
                break;
            //AET-118
            case ENTER_PHONE_NUM:
                if (result.getRet() == TransResult.SUCC) {
                    transData.setPhoneNum((String) result.getData());
                    gotoState(State.SEND_SMS.toString());
                } else {
                    gotoState(State.PRINT_PREVIEW.toString());
                }
                break;
            case ENTER_EMAIL:
                if (result.getRet() == TransResult.SUCC) {
                    transData.setEmail((String) result.getData());
                    gotoState(State.SEND_EMAIL.toString());
                } else {
                    gotoState(State.PRINT_PREVIEW.toString());
                }
                break;
            case SEND_SMS:
            case SEND_EMAIL:
                if (result.getRet() == TransResult.SUCC) {
                    // end trans
                    transEnd(result);
                } else {
                    dispResult(transType.getTransName(), result, null);
                    gotoState(State.PRINT_PREVIEW.toString());
                }
                break;
            case PRINT_TICKET:
                // end trans
                transEnd(result);
                break;
            default:
                transEnd(result);
                break;
        }

    }

    // check original trans data
    private void validateOrigTransData(long origTransNo) {
        origTransData = DbManager.getTransDao().findTransDataByTraceNo(origTransNo);
        if (origTransData == null) {
            // trans not exist
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        ETransType trType = origTransData.getTransType();

        // only sale and refund trans can be revoked
        // AET-101
        if (!trType.isVoidAllowed()
                && (trType == ETransType.OFFLINE_TRANS_SEND && origTransData.getOfflineSendState() != TransData.OfflineStatus.OFFLINE_SENT)) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORTED, null));
            return;
        }

        TransData.ETransStatus trStatus = origTransData.getTransState();
        // void trans can not be revoked again
        if (trStatus.equals(ETransStatus.VOIDED)) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOIDED, null));
            return;
        }

        copyOrigTransData();
        if (SpManager.getSysParamSp().get(SysParamSp.SUPPORT_DCC).equals(SysParamSp.Constant.YES)) {
            transData.setOrigTransType(ETransType.VOID);
            transData.setTransType(ETransType.DCC);
            gotoState(SaleVoidTrans.State.ONLINE_DCC.toString());
        } else {
            gotoState(State.TRANS_DETAIL.toString());
        }
//        gotoState(State.TRANS_DETAIL.toString());
    }

    // set original trans data
    private void copyOrigTransData() {
        transData.setAmount(origTransData.getAmount());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigRefNo(origTransData.getRefNo());
        transData.setOrigTransNo(origTransData.getTraceNo());
        transData.setOrigDateTime(origTransData.getDateTime());     //Added by daisy.zhou
        transData.setPan(origTransData.getPan());
        transData.setExpDate(origTransData.getExpDate());
        transData.setAcquirer(origTransData.getAcquirer());
        transData.setIssuer(origTransData.getIssuer());
    }

    // check whether void trans need to swipe card or not
    private void checkCardAndPin() {
        // not need to swipe card
        transData.setEnterMode(EnterMode.MANUAL);
        checkPin();
    }

    // check whether void trans need to enter pin or not
    private void checkPin() {
        // not need to enter pin
        transData.setPin("");
        transData.setHasPin(false);
        gotoState(State.ONLINE.toString());
    }

    private void goPrintBranch(ActionResult result) {
        String string = (String) result.getData();
        if (string != null && string.equals(PrintPreviewActivity.PRINT_BUTTON)) {
            //print ticket
            gotoState(State.PRINT_TICKET.toString());
        } else if (string != null && string.equals(PrintPreviewActivity.SMS_BUTTON)) {
            gotoState(State.ENTER_PHONE_NUM.toString());
        } else if (string != null && string.equals(PrintPreviewActivity.EMAIL_BUTTON)) {
            gotoState(State.ENTER_EMAIL.toString());
        } else if (string != null && string.equals(PrintPreviewActivity.BACK)) {
            transData.setSignData(null);
            gotoState(State.SIGNATURE.toString());
        } else {
            //end trans directly, not print
            transEnd(result);
        }
    }
}
