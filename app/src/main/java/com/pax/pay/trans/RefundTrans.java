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

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.enums.ETransResult;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionOfflineSend;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSelectDccAmount;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.action.ActionSendEmail;
import com.pax.pay.trans.action.ActionSendSMS;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class RefundTrans extends BaseTrans {
    private String amountOrg;//Zac
    private String amountInput;
    private int lengthAmount;

    public RefundTrans(TransEndListener transListener) {
        super(ETransType.REFUND, transListener);
    }

    @Override
    protected void bindStateOnAction() {

        ActionInputPassword inputPasswordAction = new ActionInputPassword(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(6,
                        ContextUtils.getString(R.string.prompt_refund_pwd), null);
            }
        });
        bind(State.INPUT_PWD.toString(), inputPasswordAction);

        // input amount
        ActionInputTransData amountAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(ContextUtils.getString(R.string.trans_refund))
                        .setInputLine1(ContextUtils.getString(R.string.prompt_input_refund_amount), EInputType.AMOUNT, Constants.AMOUNT_DIGIT,
                                false);
            }
        }, 1);
        bind(State.ENTER_AMOUNT.toString(), amountAction);

        // search card
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(ContextUtils.getString(R.string.trans_refund),
                        ETransType.REFUND.getReadMode(), transData.getAmount(),
                        null, "");
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // input password action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {


            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(
                        ContextUtils.getString(R.string.trans_refund), transData.getPan(), true,
                        ContextUtils.getString(R.string.prompt_pin),
                        ContextUtils.getString(R.string.prompt_no_pin),
                        "-" + transData.getAmount(),
                        null,
                        ActionEnterPin.EEnterPinType.ONLINE_PIN);
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        // emv deal action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEmvProcess) action).setParam(transData);
            }
        });
        bind(State.EMV_PROC.toString(), emvProcessAction);
        //Zac added ONLINE_DCC ENQUIRE
        // online action
        ActionTransOnline transOnlineAction_dcc = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });

        bind(RefundTrans.State.ONLINE_DCC.toString(), transOnlineAction_dcc);
        // ENQUIRE
        ActionSelectDccAmount EnquireAction = new ActionSelectDccAmount(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String transName = ContextUtils.getString(R.string.trans_refund);
//                String amount = transData.getAmount();
                int i = 0;
                String MutiAmount;
                amountOrg = amountInput.substring(0, lengthAmount - 2) + "." + amountInput.substring(lengthAmount - 2, lengthAmount) + " HKD";
                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                map.put(ContextUtils.getString(R.string.DCC_TotalAmount), amountOrg);
                Iterator iter1 = transData.getDccTransData().getTransAmtList().iterator();
                Iterator iter2 = transData.getDccTransData().getCurrencyList().iterator();
                Iterator iter3 = transData.getDccTransData().getConvRateList().iterator();
                while (iter1.hasNext()) {
                    MutiAmount = iter1.next() + " " + iter2.next() + " " + "Rate:" + iter3.next();
                    map.put(Integer.toString(i), MutiAmount);
                    i++;
                }
                ((ActionSelectDccAmount) action).setParam(transName, map);

            }
        });
        bind(RefundTrans.State.ENQUIRE.toString(), EnquireAction);
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
        //offline send
        ActionOfflineSend offlineSendAction = new ActionOfflineSend(null);
        bind(State.OFFLINE_SEND.toString(), offlineSendAction);
        //print preview
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

        // whether need input management password for void and refund
        if (SpManager.getSysParamSp().get(SysParamSp.OTHTC_VERIFY).equals(SysParamSp.Constant.YES)) {
            gotoState(State.INPUT_PWD.toString());
        } else {
            gotoState(State.CHECK_CARD.toString());
        }
    }

    enum State {
        INPUT_PWD,
        ENTER_AMOUNT,
        CHECK_CARD,
        ENTER_PIN,
        ONLINE_DCC,
        ENQUIRE,
        ONLINE,
        EMV_PROC,
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
            // check action result, if fail, transaction end
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }
        switch (state) {
            case INPUT_PWD:
                String data = EncUtils.SHA1((String) result.getData());
                if (!data.equals(SpManager.getSysParamSp().get(SysParamSp.SEC_REFUNDPWD))) {
                    transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
                    return;
                }
                gotoState(State.CHECK_CARD.toString());
                break;
            case ENTER_AMOUNT:
                lengthAmount = ((String) result.getData()).length();
                amountInput = (String) result.getData();
                String amount = ((String) result.getData()).replace(".", "");
                if (!checkAmount(amount)) {
                    transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
                    return;
                }
                transData.setAmount(amount);
                if (SpManager.getSysParamSp().get(SysParamSp.SUPPORT_DCC).equals(SysParamSp.Constant.YES)) {
                    transData.setOrigTransType(ETransType.REFUND);
                    transData.setTransType(ETransType.DCC);
                    gotoState(State.ONLINE_DCC.toString());
                } else {
                    gotoState(State.ENTER_PIN.toString());
                }
                break;
            case CHECK_CARD: // check card
                CardInformation cardInfo = (CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);
                // manual enter card NO

                byte mode = cardInfo.getSearchMode();
                if (mode == ActionSearchCard.SearchMode.WAVE) {
                    // AET-15
                    transEnd(new ActionResult(TransResult.ERR_UNSUPPORTED_FUNC, null));
                }

                gotoState(State.ENTER_AMOUNT.toString());
                break;
            case ENTER_PIN: // enter pin
                String pinBlock = (String) result.getData();
                transData.setPin(pinBlock);
                if (pinBlock != null && pinBlock.length() > 0) {
                    transData.setHasPin(true);
                }
                // online
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE_DCC:
                gotoState(RefundTrans.State.ENQUIRE.toString());
                break;
            case ENQUIRE:
                transData.setTransType(ETransType.VOID);
                String dccbackdata = (String) result.getData();
                Iterator iter1 = transData.getDccTransData().getTransAmtList().iterator();
                Iterator iter2 = transData.getDccTransData().getCurrencyList().iterator();
                Iterator iter3 = transData.getDccTransData().getConvRateList().iterator();
                String dccConvRate;
                String dccCurrency;
                String dccTransAmt;
                String amountSelect;
                if (dccbackdata.equals(amountOrg)) {
                    transData.getDccTransData().setDccOptIn(false);
                } else {
                    transData.getDccTransData().setDccOptIn(true);
                }
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
                transData.setTransType(ETransType.REFUND);
                gotoState(State.ENTER_PIN.toString());
                break;
            case EMV_PROC: // emv
                // TODO 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
                ETransResult transResult = (ETransResult) result.getData();
                // EMV完整流程 脱机批准或联机批准都进入签名流程
                Component.emvTransResultProcess(transResult, transData);
                if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {// 联机批准/脱机批准处理
                    // electronic signature
                    gotoState(State.SIGNATURE.toString());
                } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // request online/simplify process
                    // enter pin
                    gotoState(State.ENTER_PIN.toString());
                } else if (transResult == ETransResult.ONLINE_DENIED) { // online denied
                    // transaction end
                    transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
                } else if (transResult == ETransResult.ONLINE_CARD_DENIED) {// platform approve card denied
                    transEnd(new ActionResult(TransResult.ERR_CARD_DENIED, null));
                } else if (transResult == ETransResult.ABORT_TERMINATED) { // emv terminated
                    // 交易结束
                    transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                } else if (transResult == ETransResult.OFFLINE_DENIED) {
                    // 走到这里EMV有问题, 退货不支持脱机， 怎么改看着办吧
                    // FIXME Kim CDOL
                    Device.beepErr();
                    transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                }
                break;
            case ONLINE: // after online
                gotoState(State.SIGNATURE.toString());
                break;
            case SIGNATURE:
                // save signature
                byte[] signData = (byte[]) result.getData();

                if (signData != null && signData.length > 0) {
                    transData.setSignData(signData);
                    // update transaction record, save signature
                    DbManager.getTransDao().updateTransData(transData);
                }
                //get offline trans data list
                List<ETransType> list = new ArrayList<>();
                list.add(ETransType.OFFLINE_TRANS_SEND);

                List<TransData.ETransStatus> filter = new ArrayList<>();
                filter.add(TransData.ETransStatus.VOIDED);
                filter.add(TransData.ETransStatus.ADJUSTED);
                List<TransData> offlineTransList = DbManager.getTransDao().findTransData(list, filter);
                if (offlineTransList != null) {
                    if (offlineTransList.size() != 0 && offlineTransList.get(0).getId() != transData.getId()) { //AET-92
                        //offline send
                        gotoState(State.OFFLINE_SEND.toString());
                        break;
                    }
                }
                // if terminal not support electronic signature, user do not make signature or signature time out, print preview
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
                // transaction end
                transEnd(result);
                break;
            default:
                transEnd(result);
                break;
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

    private boolean checkAmount(String amountStr) {
        long amount = Long.parseLong(amountStr);
        long amountMax = Long.parseLong(SpManager.getSysParamSp().get(SysParamSp.OTHTC_REFUNDLIMT)) * 100;

        return (amount <= amountMax);
    }

}
