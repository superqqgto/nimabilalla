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
import com.pax.pay.trans.BaseTrans;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
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

public class AuthTrans extends BaseTrans {

    private String amount;
    private boolean isNeedInputAmount = true; // is need input amount
    private boolean isFreePin = true;
    boolean isSupportBypass = true;
    private int transNameResId = R.string.trans_preAuth;

    public AuthTrans(boolean isFreePin) {
        super(ETransType.PREAUTH, null);
        this.isFreePin = isFreePin;
        isNeedInputAmount = true;

    }

    public AuthTrans(String amount, TransEndListener transListener) {
        super(ETransType.PREAUTH, transListener);
        this.amount = amount;
        isNeedInputAmount = false;
    }

    @Override
    protected void bindStateOnAction() {

        bindEnterAmount();
        bindSearchCard();
        bindEnterPin();
        bindEmvProcess();
        bindOnline();
        bindSignature();
        bindPrintPreview();
        bindPrintReceipt();

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

        // execute the first action
        if (isNeedInputAmount) {
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            transData.setAmount(amount);
            gotoState(State.CHECK_CARD.toString());
        }
    }

    private void bindEnterAmount() {
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

    private void bindSearchCard() {
        // search card action
        ActionSearchCard.Builder searchBuilder = new ActionSearchCard.Builder()
                .transName(transNameResId)
                .cardReadMode(ETransType.PREAUTH.getReadMode())
                .searchCardPrompt(R.string.prompt_swipe_card)
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ActionSearchCard searchAct = (ActionSearchCard) action;
                        searchAct.setAmount(transData.getAmount());
                        searchAct.setTipAmount("");
                    }
                });

        bind(State.CHECK_CARD.toString(), searchBuilder.create());
    }

    private void bindEnterPin() {
        // if quick pass by pin, set isSupportBypass as false,input password
        if (!isFreePin) {
            isSupportBypass = false;
        }

        // enter card  pin
        ActionEnterPin.Builder pinBuilder = new ActionEnterPin.Builder()
                .transName(transNameResId)
                .isSupportBypass(isSupportBypass)
                .prompt1(R.string.prompt_pin)
                .prompt2(R.string.prompt_no_pin)
                .enterPinType(ActionEnterPin.EEnterPinType.ONLINE_PIN)
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ActionEnterPin actionPin = (ActionEnterPin) action;
                        actionPin.setTotalAmount(transData.getAmount());
                        actionPin.setPan(transData.getPan());
                    }
                });

        bind(State.ENTER_PIN.toString(), pinBuilder.create());
    }

    private void bindEmvProcess() {

        // emv action
        ActionEmvProcess.Builder emvBuilder = new ActionEmvProcess.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionEmvProcess) action).setTransData(transData);
                    }
                });

        bind(State.EMV_PROC.toString(), emvBuilder.create());
    }

    private void bindOnline() {
        // online action
        ActionTransOnline.Builder onlineBuilder = new ActionTransOnline.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionTransOnline) action).setTransData(transData);
                    }
                });

        bind(State.ONLINE.toString(), onlineBuilder.create());
    }

    private void bindSignature() {
        // signature action
        ActionSignature.Builder signBuilder = new ActionSignature.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ActionSignature signAction = (ActionSignature) action;
                        signAction.setAmount(transData.getAmount());
                        signAction.setFeatureCode(Component.genFeatureCode(transData));
                    }
                });

        bind(State.SIGNATURE.toString(), signBuilder.create());
    }

    private void bindPrintPreview() {
        //print preview action
        ActionPrintPreview.Builder previewBuilder = new ActionPrintPreview.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintPreview) action).setTransData(transData);
                    }
                });

        bind(State.PRINT_PREVIEW.toString(), previewBuilder.create());
    }

    private void bindPrintReceipt() {
        // print action
        ActionPrintTransReceipt.Builder printBuilder = new ActionPrintTransReceipt.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintTransReceipt) action).setTransData(transData);
                    }
                });

        bind(State.PRINT_RECEIPT.toString(), printBuilder.create());
    }

    enum State {
        ENTER_AMOUNT,
        CHECK_CARD,
        ENTER_PIN,
        EMV_PROC,
        ONLINE,
        SIGNATURE,
        PRINT_PREVIEW,
        PRINT_RECEIPT,
        //AET-118
        ENTER_PHONE_NUM,
        ENTER_EMAIL,
        SEND_SMS,
        SEND_EMAIL,
    }

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
                } else if (mode == SearchMode.INSERT || mode == SearchMode.WAVE) {
                    // EMV处理
                    gotoState(State.EMV_PROC.toString());
                }
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
                if (transData.getEnterMode() == EnterMode.CLSS) {
                    transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
                }
                //此处需要存取online响应报文中的authcode、responseCode字段
                saveTransToTabBatch();

                // judge whether need signature or print
                toSignOrPrint();

                break;
            case EMV_PROC: // emv后续处理
                onEmvProcessResult(result);
                break;
            case SIGNATURE:
                // save signature data
                byte[] signData = (byte[]) result.getData();
                if (signData != null && signData.length > 0) {
                    transData.setSignData(signData);
                    // update transaction record，save signature
                    DbManager.getTransDao().updateTransData(transData);
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

    public void onEmvProcessResult(ActionResult result) {
        // TODO 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
        ETransResult transResult = (ETransResult) result.getData();
        // EMV完整流程 脱机批准或联机批准都进入签名流程
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {// 联机批准/脱机批准处理
            // judge whether need signature or print
            toSignOrPrint();

        } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程

            if (!isFreePin) {
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
                return;
            }

            if (transResult == ETransResult.ARQC) {
                if (!Component.isQpbocNeedOnlinePin()) {
                    gotoState(State.ONLINE.toString());
                    return;
                }
            }
            if (Component.clssQPSProcess(transData)) { // pin free
                transData.setPinFree(true);
                gotoState(State.ONLINE.toString());
            } else {
                // input password
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
            }
        } else if (transResult == ETransResult.ONLINE_DENIED) { // online denied
            // transaction end
            transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
        } else if (transResult == ETransResult.ONLINE_CARD_DENIED) {// platform approve card denied
            transEnd(new ActionResult(TransResult.ERR_CARD_DENIED, null));
        } else if (transResult == ETransResult.ABORT_TERMINATED) { // emv terminated
            // transaction end
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        } else if (transResult == ETransResult.OFFLINE_DENIED) {
            // transaction end
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    private void saveTransToTabBatch() {
        TabBatchTransData tabBatchTransData = new TabBatchTransData(transData);
        DbManager.getTabBatchTransDao().insertTabBatchTransData(tabBatchTransData);
    }

    // 判断是否需要电子签名或打印
    private void toSignOrPrint() {
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
