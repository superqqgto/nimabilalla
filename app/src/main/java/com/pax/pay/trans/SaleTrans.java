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

import android.content.Context;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ActionResult;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PollingResult;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.enums.ETransResult;
import com.pax.eventbus.ClssLightStatusEvent;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.CvmType;
import com.pax.jemv.clcommon.KernType;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clcommon.TransactionPath;
import com.pax.jemv.paywave.api.ClssWaveApi;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.DalManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.clss.CTransResult;
import com.pax.pay.clss.ClssTransProcess;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.trans.action.ActionAdjustTip;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionOfflineSend;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionSelectDccAmount;
import com.pax.pay.trans.action.ActionSendEmail;
import com.pax.pay.trans.action.ActionSendSMS;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.action.ActionUserAgreement;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.action.activity.SearchCardActivity;
import com.pax.pay.trans.action.activity.UserAgreementActivity;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.BaseTransData.*;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.LogUtils;
import com.pax.view.dialog.CustomAlertDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class SaleTrans extends BaseTrans {
    private byte searchCardMode = -1; // search card mode
    private String amount;
    private String tipAmount;
    private float percent;

    private boolean isFreePin;
    private boolean isSupportBypass = true;
    private boolean hasTip = false;
    private String amountOrg;//Zac
    private String amountInput;
    private int lengthAmount;
    boolean enableDcc = SpManager.getSysParamSp().get(SysParamSp.SUPPORT_DCC).equals(SysParamSp.Constant.YES);

    /**
     * @param amount    :total amount
     * @param isFreePin
     * @param mode      {@link com.pax.pay.trans.action.ActionSearchCard.SearchMode}, 如果等于-1，
     */
    public SaleTrans(String amount, byte mode, boolean isFreePin,
                     TransEndListener transListener) {
        super(ETransType.SALE, transListener);
        setParam(amount, "0", mode, isFreePin, false);
    }

    /**
     * @param amount    :total amount
     * @param tipAmount :tip amount
     * @param isFreePin
     * @param mode      {@link com.pax.pay.trans.action.ActionSearchCard.SearchMode}, 如果等于-1，
     */
    public SaleTrans(String amount, String tipAmount, byte mode, boolean isFreePin,
                     TransEndListener transListener) {
        super(ETransType.SALE, transListener);
        setParam(amount, tipAmount, mode, isFreePin, true);
    }

    private void setParam(String amount, String tipAmount, byte mode, boolean isFreePin, boolean hasTip) {
        this.searchCardMode = mode;
        this.amount = amount;
        this.tipAmount = tipAmount;
        this.isFreePin = isFreePin;
        this.hasTip = hasTip;

        if (searchCardMode == -1) { // 待机银行卡消费入口
            searchCardMode = ETransType.SALE.getReadMode();
            this.transType = ETransType.SALE;
        } else if (searchCardMode == -3) { // entrance of quick pass by pin
            this.searchCardMode = SearchMode.WAVE;
            this.isFreePin = false;
        }
    }

    @Override
    public void bindStateOnAction() {
        if (amount != null && amount.length() > 0) {
            lengthAmount = amount.length();
            amountInput = amount;
            transData.setAmount(amount.replace(".", ""));
        }
        if (tipAmount != null && tipAmount.length() > 0) {
            transData.setTipAmount(tipAmount.replace(".", ""));
        }

        // input trans amount action(This action is mainly used to handle bank card consumption and flash close paid deals)
        ActionInputTransData amountAction = new ActionInputTransData(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.quick_pass_sale_free_pin);
                ((ActionInputTransData) action).setParam(title)
                        .setInputLine1(ContextUtils.getString(R.string.prompt_input_amount),
                                EInputType.AMOUNT, Constants.AMOUNT_DIGIT, false);
            }
        }, 1);
        bind(State.ENTER_AMOUNT.toString(), amountAction);

        // search card action
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_sale);
                ((ActionSearchCard) action).setParam(title, searchCardMode,
                        transData.getAmount(), null, "");
            }
        });

        bind(State.CHECK_CARD.toString(), searchCardAction);

        //adjust tip action
        ActionAdjustTip adjustTipAction = new ActionAdjustTip(new ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String amount = String.valueOf(Long.parseLong(transData.getAmount()) - Long.parseLong(transData.getTipAmount()));
                ((ActionAdjustTip) action).setParam(ContextUtils.getString(R.string.trans_sale), amount, percent);
            }
        });
        bind(State.ADJUST_TIP.toString(), adjustTipAction);

        // enter pin action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {

                // if flash pay by pwd,set isSupportBypass=false,need to enter pin
                if (!isFreePin) {
                    isSupportBypass = false;
                }
                ((ActionEnterPin) action).setParam(ContextUtils.getString(R.string.trans_sale),
                        transData.getPan(), isSupportBypass, ContextUtils.getString(R.string.prompt_pin),
                        ContextUtils.getString(R.string.prompt_no_pin), transData.getAmount(), transData.getTipAmount(),
                        ActionEnterPin.EEnterPinType.ONLINE_PIN);
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        //Zac added ONLINE_DCC ENQUIRE
        // online action
        ActionTransOnline transOnlineAction_dcc = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });

        bind(SaleTrans.State.ONLINE_DCC.toString(), transOnlineAction_dcc);
        // ENQUIRE
        ActionSelectDccAmount EnquireAction = new ActionSelectDccAmount(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String transName = ContextUtils.getString(R.string.trans_dcc);
                int i = 0;
                String mutiAmount;
                amountOrg = amountInput.substring(0, lengthAmount - 2) + "." + amountInput.substring(lengthAmount - 2, lengthAmount) + " HKD";
                LogUtils.i("Zac", amountOrg);
                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                map.put(ContextUtils.getString(R.string.DCC_TotalAmount), amountOrg);
                Iterator iter1 = transData.getDccTransData().getTransAmtList().iterator();
                Iterator iter2 = transData.getDccTransData().getCurrencyList().iterator();
                Iterator iter3 = transData.getDccTransData().getConvRateList().iterator();
                while (iter1.hasNext()) {
                    mutiAmount = iter1.next() + " " + iter2.next() + " " + "Rate:" + iter3.next();
                    map.put(Integer.toString(i), mutiAmount);
                    i++;
                }
                ((ActionSelectDccAmount) action).setParam(transName, map);

            }
        });
        bind(SaleTrans.State.ENQUIRE.toString(), EnquireAction);

        // emv process action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEmvProcess) action).setParam(transData);
            }
        });
        bind(State.EMV_PROC.toString(), emvProcessAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionClssProcess) action).setParam(transData);
            }
        });
        bind(State.CLSS_PROC.toString(), clssProcessAction);

        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionClssPreProc) action).setParam(transData);
            }
        });
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        // online action
        ActionTransOnline transOnlineAction = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });

        bind(State.ONLINE.toString(), transOnlineAction);

        // signature action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setAmount(transData.getAmount());
            }
        });
        bind(State.SIGNATURE.toString(), signatureAction);

        // Agreement action
        ActionUserAgreement userAgreemenAction = new ActionUserAgreement(null);
        bind(State.USER_AGREEMENT.toString(), userAgreemenAction);

        //offline send
        ActionOfflineSend offlineSendAction = new ActionOfflineSend(null);
        bind(State.OFFLINE_SEND.toString(), offlineSendAction);

        //print preview action
        ActionPrintPreview printPreviewAction = new ActionPrintPreview(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintPreview) action).setTransData(transData);
                    }
                });
        bind(State.PRINT_PREVIEW.toString(), printPreviewAction);

        // get Telephone num
        ActionInputTransData phoneAction = new ActionInputTransData(new ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.paperless);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_phone_number), EInputType.PHONE, 20, false);
            }
        }, 1);
        bind(State.ENTER_PHONE_NUM.toString(), phoneAction);

        // get Telephone num
        ActionInputTransData emailAction = new ActionInputTransData(new ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.paperless);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_email_address), EInputType.EMAIL, 100, false);
            }
        }, 1);
        bind(State.ENTER_EMAIL.toString(), emailAction);

        // print action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintTransReceipt) action).setTransData(transData);
                    }
                });
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        ActionSendSMS sendSMSAction = new ActionSendSMS(new ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSendSMS) action).setParam(transData);
            }
        });
        bind(State.SEND_SMS.toString(), sendSMSAction);

        ActionSendEmail sendEmailAction = new ActionSendEmail(new ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSendEmail) action).setParam(transData);
            }
        });
        bind(State.SEND_EMAIL.toString(), sendEmailAction);

        // perform the first action
        if (amount == null || amount.length() == 0) {
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            if (SpManager.getSysParamSp().get(SysParamSp.SUPPORT_USER_AGREEMENT).equals(SysParamSp.Constant.YES)) {
                gotoState(State.USER_AGREEMENT.toString());
            } else {
                if (enableDcc) {
                    transData.setOrigTransType(ETransType.SALE);
                    transData.setTransType(ETransType.DCC);
                    gotoState(SaleTrans.State.ONLINE_DCC.toString());
                } else {
//                    gotoState(State.CHECK_CARD.toString());
                    gotoState(State.CLSS_PREPROC.toString());
                }
            }
        }

    }

    //added ONLINE_DCC , ENQUIRE
    enum State {
        ENTER_AMOUNT,
        CHECK_CARD,
        ADJUST_TIP,
        ENTER_PIN,
        ONLINE_DCC,
        ENQUIRE,
        ONLINE,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        SIGNATURE,
        USER_AGREEMENT,
        OFFLINE_SEND,
        PRINT_PREVIEW,
        PRINT_TICKET,
        ENTER_PHONE_NUM,
        ENTER_EMAIL,
        SEND_SMS,
        SEND_EMAIL,
    }


    private byte mode;

    @Override
    public void onActionResult(String currentState, ActionResult result) {

        State state = State.valueOf(currentState);
        if (state == State.EMV_PROC) {
//             不管emv处理结果成功还是失败，都更新一下冲正
            byte[] f55Dup = EmvTags.getF55(transType, true);
            if (f55Dup != null && f55Dup.length > 0) {
                TransData dupTransData = DbManager.getTransDao().findFirstDupRecord();
                if (dupTransData != null) {
                    dupTransData.setDupIccData(GlManager.bcdToStr(f55Dup));
                    DbManager.getTransDao().updateTransData(dupTransData);
                }
            }

        }

        int ret = result.getRet();
        if (state == State.CLSS_PREPROC) {
            // check action result，if failed，end the trans.
            if (ret != TransResult.SUCC) {
                searchCardMode &= 0x03;
            }
        } else if ((state != State.SIGNATURE) && (state != State.PRINT_PREVIEW) &&
                (state != State.ENTER_PHONE_NUM) && (state != State.ENTER_EMAIL) &&
                (state != State.SEND_SMS) && (state != State.SEND_EMAIL)) {
            // check action result，if failed，end the trans.
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case ENTER_AMOUNT:
                // save trans amount
                String amount = ((String) result.getData()).replace(".", "");
                transData.setAmount(amount);
                gotoState(State.CHECK_CARD.toString());
                break;
            case CHECK_CARD: // subsequent processing of check card
                CardInformation cardInfo = (CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);
                transData.setTransType(ETransType.SALE);
                // enter card number manually
                mode = cardInfo.getSearchMode();
                if (mode == SearchMode.SWIPE | mode == SearchMode.KEYIN) {
                    goTipBranch();
                } else if (mode == SearchMode.INSERT) {
                    // EMV process
                    gotoState(State.EMV_PROC.toString());
                } else if (mode == SearchMode.WAVE) {
                    // AET-15
                    //transEnd(new ActionResult(TransResult.ERR_UNSUPPORTED_FUNC, null));
                    gotoState(State.CLSS_PROC.toString());
                }

                break;
            case ADJUST_TIP:
                //get total amount
                String totalAmountStr = String.valueOf(CurrencyConverter.parse(result.getData().toString()));
                transData.setAmount(totalAmountStr);
                //get tip amount
                String tip = String.valueOf(CurrencyConverter.parse(result.getData1().toString()));
                transData.setTipAmount(tip);
                if (mode == SearchMode.SWIPE || mode == SearchMode.KEYIN) {
                    // enter pin
                    gotoState(State.ENTER_PIN.toString());
                }
                break;
            case ENTER_PIN: // subsequent processing of enter pin
                String pinBlock = (String) result.getData();
                transData.setPin(pinBlock);
                if (pinBlock != null && pinBlock.length() > 0) {
                    transData.setHasPin(true);
                }

                if (transData.getIssuer().getFloorLimit() > Long.parseLong(transData.getAmount())) {
                    if (transData.getEnterMode() == EnterMode.SWIPE) {
                        transData.setTransType(ETransType.OFFLINE_TRANS_SEND);
                        // save trans data
                        transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
                        DbManager.getTransDao().insertTransData(transData);
                        //increase trans no.
                        Component.incTransNo();
                        toSignOrPrint();
                        break;
                    }
                }
                // online process
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE_DCC: {
                gotoState(SaleTrans.State.ENQUIRE.toString());
                break;
            }
            case ENQUIRE: {
                transData.setTransType(ETransType.SALE);
                String dccBackData = (String) result.getData();
                String Amount;
                Iterator iter1 = transData.getDccTransData().getTransAmtList().iterator();
                Iterator iter2 = transData.getDccTransData().getCurrencyList().iterator();
                Iterator iter3 = transData.getDccTransData().getConvRateList().iterator();
                String dccConvRate;
                String dccCurrency;
                String dccTransAmt;
                if (dccBackData.equals(amountOrg)) {
                    transData.getDccTransData().setDccOptIn(false);
                } else {
                    transData.getDccTransData().setDccOptIn(true);
                    while (iter2.hasNext()) {
                        dccTransAmt = (String) iter1.next();
                        dccCurrency = (String) iter2.next();
                        dccConvRate = (String) iter3.next();
                        Amount = dccTransAmt + " " + dccCurrency + " " + "Rate:" + dccConvRate;
                        if (Amount.equals(dccBackData)) {
                            transData.getDccTransData().setDccConvRate(dccConvRate);
                            transData.getDccTransData().setDccCurrency(dccCurrency);
                            transData.getDccTransData().setDccTransAmt(dccTransAmt);
                            break;
                        }
                    }
                }
                gotoState(State.CHECK_CARD.toString());
                break;
            }
            case ONLINE: // subsequent processing of online
                if (transData.getEnterMode() == EnterMode.CLSS) {
                    clssAfterOnline(ContextUtils.getActyContext());
                    //transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
                } else {
                    // determine whether need electronic signature or print
                    toSignOrPrint();
                }
                break;
            case EMV_PROC: // emv后续处理
                // TODO 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
                //get trans result
                ETransResult transResult = (ETransResult) result.getData();
                // EMV完整流程 脱机批准或联机批准都进入签名流程
                afterEMVProcess(transResult);
                break;
            case CLSS_PREPROC:
                gotoState(State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                CTransResult clssResult = (CTransResult) result.getData();
                afterClssProcess(clssResult);
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
                List<TransData.OfflineStatus> filter = new ArrayList<>();
                filter.add(TransData.OfflineStatus.OFFLINE_NOT_SENT);
                List<TransData> offlineTransList = DbManager.getTransDao().findOfflineTransData(filter);
                if (offlineTransList != null) {
                    if (offlineTransList.size() != 0 && offlineTransList.get(0).getId() != transData.getId()) { //AET-92
                        //offline send
                        gotoState(State.OFFLINE_SEND.toString());
                        break;
                    }
                }

                // if terminal does not support signature ,card holder does not sign or time out，print preview directly.
                gotoState(State.PRINT_PREVIEW.toString());
                break;
            case USER_AGREEMENT:
                String agreement = (String) result.getData();
                if (agreement != null && agreement.equals(UserAgreementActivity.ENTER_BUTTON)) {
                    gotoState(State.CLSS_PREPROC.toString());
                } else {
                    transEnd(result);
                }
                break;
            case OFFLINE_SEND:
                gotoState(State.PRINT_PREVIEW.toString());
                break;
            case PRINT_PREVIEW:
                goPrintBranch(result);
                break;
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
            default:
                transEnd(result);
                break;
        }
    }

    private void goTipBranch() {
        boolean enableTip = SpManager.getSysParamSp().get(SysParamSp.EDC_SUPPORT_TIP).equals(SysParamSp.Constant.YES);
        //adjust tip
        long totalAmount = Long.parseLong(transData.getAmount());
        long tipAmount = Long.parseLong(transData.getTipAmount());
        long baseAmount = totalAmount - tipAmount;
        percent = transData.getIssuer().getAdjustPercent();

        if (enableTip) {
            if (!hasTip)
                gotoState(State.ADJUST_TIP.toString());
            else if (baseAmount * percent / 100 < tipAmount)
                showAdjustTipDialog(ContextUtils.getActyContext());
        } else {
            // enter pin
            gotoState(State.ENTER_PIN.toString());
        }
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

    // need electronic signature or send
    private void toSignOrPrint() {
        if (Component.isSignatureFree(transData)) {// signature free
            transData.setSignFree(true);
            // print preview
            gotoState(State.PRINT_PREVIEW.toString());
        } else {
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }
        DbManager.getTransDao().updateTransData(transData);
    }

    private void afterEMVProcess(ETransResult transResult) {
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {// 联机批准/脱机批准处理
            if (transResult == ETransResult.ONLINE_APPROVED) {
                toSignOrPrint();
                return;
            }

            toSignOrPrint();
        } else if (transResult == ETransResult.ARQC) { // request online
/*
                    if (isForcePin) {
                        transData.setPinFree(false);
                        gotoState(State.ENTER_PIN.toString());
                        return;
                    }
*/
            if (!Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            if (isFreePin && Component.clssQPSProcess(transData)) { // pin free
                transData.setPinFree(true);
                gotoState(State.ONLINE.toString());
            } else {
                // enter pwd
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
            }
        } else if (transResult == ETransResult.SIMPLE_FLOW_END) { // simplify the process
            // trans not support simplified process
            // end trans
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        } else if (transResult == ETransResult.ONLINE_DENIED) { // refuse online
            // end trans
            transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
        } else if (transResult == ETransResult.ONLINE_CARD_DENIED) {// 平台批准卡片拒绝
            transEnd(new ActionResult(TransResult.ERR_CARD_DENIED, null));
        } else if (transResult == ETransResult.ABORT_TERMINATED) { // emv interrupt
            // end trans
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        } else if (transResult == ETransResult.OFFLINE_DENIED) {
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    private void afterClssProcess(CTransResult transResult) {
        if (!SpManager.getSysParamSp().get(SysParamSp.APP_COMM_TYPE).equals(SysParamSp.Constant.COMMTYPE_DEMO)
                && transResult.getTransResult() == CTransResult.App_Try_Again) {
            gotoState(State.CHECK_CARD.toString());
            return;
        }

        Component.clssTransResultProcess(transResult, transData);

        if (transResult.getAcResult() == ACType.AC_TC) {
            transData.setOnlineTrans(false);
        } else if (transResult.getAcResult() == ACType.AC_ARQC) {
            transData.setOnlineTrans(true);
            gotoState(State.ONLINE.toString());
            return;
        } else {
            if (transResult.getTransResult() == RetCode.EMV_OK) {
                //FIXME finish virtual PayPass, do mag card flow
                gotoState(State.ONLINE.toString());
                return;
            } else {
                Device.beepErr();
                transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                return;
            }
        }

        if (transResult.getCvmResult() == CvmType.RD_CVM_ONLINE_PIN) {
            transData.setSignFree(true);
            transData.setPinFree(false);
            gotoState(State.ENTER_PIN.toString());
            return;
        } else if (transResult.getCvmResult() == CvmType.RD_CVM_SIG) {
            transData.setSignFree(false);
            transData.setPinFree(true);
            gotoState(State.SIGNATURE.toString());
            return;
        }

        if (transResult.getPathResult() == TransactionPath.CLSS_VISA_MSD) {
            transData.setOnlineTrans(true);
            gotoState(State.ONLINE.toString());
        }
    }

    public void clssAfterOnline(final Context context) {
        EventBus.getDefault().post(new ClssLightStatusEvent(SearchCardActivity.CLSSLIGHTSTATUS_COMPLETE));

        //FIXME Kim don't do 2nd tap for demo
        if (!SpManager.getSysParamSp().get(SysParamSp.APP_COMM_TYPE).equals(SysParamSp.Constant.COMMTYPE_DEMO)
                && (ClssTransProcess.getInstance().getKernelType() == KernType.KERNTYPE_VIS)
                && (ClssTransProcess.getInstance().getTransPath() != TransactionPath.CLSS_VISA_MSD)) {

            final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.PROGRESS_TYPE);
            dialog.show();
            dialog.setTimeout(30);
            dialog.setContentText(ContextUtils.getString(R.string.prompt_wave_card));

            FinancialApplication.mApp.runInBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        //tap card
                        PollingResult result = DalManager.getCardReaderHelper().polling(EReaderType.MAG_ICC_PICC, 30 * 1000);
                        if (result.getReaderType() == EReaderType.PICC) {
                            int ret = ClssWaveApi.clssWaveIssuerAuth(new byte[5], 5);
                            LogUtils.i("clssWaveIssuerAuth", "ret = " + ret);

                            ret = ClssWaveApi.clssWaveIssScriptProc(new byte[6], 6);
                            LogUtils.i("clssWaveIssScriptProc", "ret = " + ret);

                            DalManager.getCardReaderHelper().stopPolling();
                        } else {
                            DalManager.getCardReaderHelper().stopPolling();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        dialog.dismiss();
                        toSignOrPrint();
                    }
                }
            });
        } else {
            toSignOrPrint();
        }
    }

    public void showAdjustTipDialog(final Context context) {

        final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
        dialog.setCancelClickListener(new CustomAlertDialog.OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
                transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            }
        });
        dialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
                gotoState(State.ADJUST_TIP.toString());
            }
        });
        dialog.show();
        dialog.setNormalText(ContextUtils.getString(R.string.prompt_tip_exceed));
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);
    }
}
