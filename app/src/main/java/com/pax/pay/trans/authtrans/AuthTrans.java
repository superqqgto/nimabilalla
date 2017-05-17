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
package com.pax.pay.trans.authtrans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.enums.ETransResult;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.action.ActionSendEmail;
import com.pax.pay.trans.action.ActionSendSMS;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TabBatchTransData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.BaseTransData.*;
import com.pax.pay.utils.ContextUtils;

public class AuthTrans extends BaseAuthTrans {

    private String amount;
    private boolean isNeedInputAmount = true; // is need input amount
    private boolean isFreePin = true;
    boolean isSupportBypass = true;

    public AuthTrans(boolean isFreePin) {
        super(ETransType.PREAUTH, R.string.trans_preAuth, null);
        this.isFreePin = isFreePin;
        isNeedInputAmount = true;
    }

    public AuthTrans(String amount, TransEndListener transListener) {
        super(ETransType.PREAUTH, R.string.trans_preAuth, transListener);
        this.amount = amount;
        isNeedInputAmount = false;
    }

    @Override
    protected void bindStateOnAction() {

        bindEnterAmount();
        bindCheckCard();
        bindEnterPin();
        bindEmvProcess();
        bindOnline();
        bindSignature();
        bindPrintPreview();
        bindPrintReceipt();
        bindPhoneNum();
        bindEnterEmail();
        bindSendSMS();
        bindSendEmail();

        // execute the first action
        if (isNeedInputAmount) {
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            transData.setAmount(amount);
            gotoState(State.CHECK_CARD.toString());
        }
    }

    protected void bindEnterAmount() {
        // input amount
        ActionInputTransData.Builder inputBuilder = new ActionInputTransData.Builder()
                .transName(transNameResId)
                .lineNum(1)
                .prompt1(R.string.prompt_input_amount)
                .inputType1(EInputType.AMOUNT)
                .maxLen1(9)
                .isVoidLastTrans(false);

        bind(State.ENTER_AMOUNT.toString(), inputBuilder.create());
    }

//    protected void bindCheckCard() {
//        // search card action
//        ActionSearchCard.Builder searchBuilder = new ActionSearchCard.Builder()
//                .transName(transNameResId)
//                .cardReadMode(ETransType.PREAUTH.getReadMode())
//                .startListener(new AAction.ActionStartListener() {
//                    @Override
//                    public void onStart(AAction action) {
//                        ActionSearchCard searchAct = (ActionSearchCard) action;
//                        searchAct.setAmount(transData.getAmount());
//                    }
//                });
//
//        bind(State.CHECK_CARD.toString(), searchBuilder.create());
//    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        if (state == State.EMV_PROC) {
            // 不管emv处理结果成功还是失败，都更新一下冲正
            byte[] f55Dup = EmvTags.getF55(transType, true);
            if (f55Dup != null && f55Dup.length > 0) {
                TransData dupTransData = DbManager.getTransDao().findFirstDupRecord();
                if (dupTransData != null) {
                    dupTransData.setDupIccData(GlManager.bcdToStr(f55Dup));
                    DbManager.getTransDao().updateTransData(dupTransData);
                }
            }
        }
        if (state != State.SIGNATURE) {
            // check action result，if fail，finish transaction
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case ENTER_AMOUNT:// 输入交易金额后续处理
                // save amount
                String amount = ((String) result.getData()).replace(".", "");
                transData.setAmount(amount);
                gotoState(State.CHECK_CARD.toString());
                break;
            case CHECK_CARD: // 检测卡的后续处理
                CardInformation cardInfo = (CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);

                // 手输卡号处理
                byte mode = cardInfo.getSearchMode();
                if (mode == SearchMode.KEYIN || mode == SearchMode.SWIPE) {
                    // input password
                    gotoState(State.ENTER_PIN.toString());
                } else if (mode == SearchMode.INSERT) {
                    // EMV处理
                    gotoState(State.EMV_PROC.toString());
                }
                break;
            case EMV_PROC: // emv后续处理
                onEmvProcessResult(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                String pinBlock = (String) result.getData();
                transData.setPin(pinBlock);
                if (pinBlock != null && pinBlock.length() > 0) {
                    transData.setHasPin(true);
                }
                // online
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE: // after online
                // judge whether need signature or print
                toSignOrPrint();

                break;
            case SIGNATURE:
                // save signature data
                byte[] signData = (byte[]) result.getData();
                if (signData != null && signData.length > 0) {
                    transData.setSignData(signData);
                    // update transaction record，save signature
                    DbManager.getTransDao().updateTransData(transData);
                    updateTransToTabBatch();
                }
                // if terminal not support electronic signature or user do not make signature or signature time out, print preview
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
            case PRINT_RECEIPT:
                // transaction end
                transEnd(result);
                break;
            default:
                transEnd(result);
                break;
        }
    }

    private void saveTransToTabBatch() {
        TabBatchTransData tabBatchTransData = new TabBatchTransData(transData);
        DbManager.getTabBatchTransDao().insertTabBatchTransData(tabBatchTransData);
    }

    private void updateTransToTabBatch() {
        TabBatchTransData tabBatchTransData = new TabBatchTransData(transData);
        DbManager.getTabBatchTransDao().updateTabBatchTransData(tabBatchTransData);
    }

    // 判断是否需要电子签名或打印
    @Override
    protected void toSignOrPrint() {
        if (Component.isSignatureFree(transData)) {// 免签
            transData.setSignFree(true);
            // 打印
            gotoState(State.PRINT_RECEIPT.toString());
        } else {
            // 电子签名
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }
        DbManager.getTransDao().updateTransData(transData);
        saveTransToTabBatch();
    }

    private void goPrintBranch(ActionResult result) {
        String string = (String) result.getData();
        if (string != null && string.equals(PrintPreviewActivity.PRINT_BUTTON)) {
            //print ticket
            gotoState(State.PRINT_RECEIPT.toString());
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
