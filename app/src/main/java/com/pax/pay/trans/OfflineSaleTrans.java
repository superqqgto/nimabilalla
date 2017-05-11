/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-10
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.base.Issuer;
import com.pax.pay.trans.action.ActionEnterAmount;
import com.pax.pay.trans.action.ActionEnterAuthCode;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionSendEmail;
import com.pax.pay.trans.action.ActionSendSMS;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;

public class OfflineSaleTrans extends BaseTrans {

    public OfflineSaleTrans(TransEndListener transListener) {
        super(ETransType.OFFLINE_TRANS_SEND, transListener);
    }

    @Override
    protected void bindStateOnAction() {

        // search card action
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_offline);

                ((ActionSearchCard) action).setParam(title, ETransType.OFFLINE_TRANS_SEND.getReadMode(), null,
                        null, ContextUtils.getString(R.string.prompt_swipe_card));
            }
        });

        bind(State.CHECK_CARD.toString(), searchCardAction);

        // input amount and tip amount action
        ActionEnterAmount amountAction = new ActionEnterAmount(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                float percent = transData.getIssuer().getAdjustPercent();
                ((ActionEnterAmount) action).setParam(ContextUtils.getString(R.string.trans_offline), percent);
            }
        });
        bind(State.ENTER_AMOUNT.toString(), amountAction);

        //enter auth code action
        ActionEnterAuthCode enterAuthCodeAction = new ActionEnterAuthCode(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionEnterAuthCode) action).setParam(ContextUtils.getString(R.string.trans_offline),
                        ContextUtils.getString(R.string.prompt_auth_code), transData.getAmount());
            }
        });
        bind(State.ENTER_AUTH_CODE.toString(), enterAuthCodeAction);

        // enter pin action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(
                        ContextUtils.getString(R.string.trans_offline), transData.getPan(), true,
                        ContextUtils.getString(R.string.prompt_pin),
                        ContextUtils.getString(R.string.prompt_no_pin),
                        transData.getAmount(),transData.getTipAmount(),
                        ActionEnterPin.EEnterPinType.ONLINE_PIN);
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        // signature action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setAmount(transData.getAmount());
            }
        });
        bind(State.SIGNATURE.toString(), signatureAction);

        //print preview action
        ActionPrintPreview printPreviewAction = new ActionPrintPreview(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintPreview) action).setParam(transData);
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

        // perform the first action
        gotoState(State.CHECK_CARD.toString());
    }

    enum State {
        CHECK_CARD,
        ENTER_AMOUNT,
        ENTER_AUTH_CODE,
        ENTER_PIN,
        SIGNATURE,
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
            case CHECK_CARD:
                ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);

                Issuer issuer = transData.getIssuer();
                if (issuer != null) {
                    if (!issuer.isEnableOffline()) {
                        transEnd(new ActionResult(TransResult.ERR_NOT_SUPPORT_TRANS, null));
                    }
                } else {
                    transEnd(new ActionResult(TransResult.ERR_CARD_UNSUPPORTED, null));
                }

                byte mode = cardInfo.getSearchMode();
                transData.setTransType(ETransType.OFFLINE_TRANS_SEND);

                // enter amount
                gotoState(State.ENTER_AMOUNT.toString());

                break;
            case ENTER_AMOUNT:
                //set total amount
                transData.setAmount(result.getData().toString());
                //set tip amount
                transData.setTipAmount(result.getData1().toString());
                //enter auth code
                gotoState(State.ENTER_AUTH_CODE.toString());
                break;
            case ENTER_AUTH_CODE:
                //get auth code
                String authCode = (String) result.getData();
                //set auth code
                transData.setAuthCode(authCode);
                //enter pin
                gotoState(State.ENTER_PIN.toString());
                break;
            case ENTER_PIN:
                String pinBlock = (String) result.getData();
                transData.setPin(pinBlock);
                if (pinBlock != null && pinBlock.length() > 0) {
                    transData.setHasPin(true);
                }
                // save trans data
                transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
                DbManager.getTransDao().insertTransData(transData);
                //increase trans no.
                Component.incTransNo();
                // signature
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
                // if terminal does not support signature ,card holder does not sign or time out，print preview directly.
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
