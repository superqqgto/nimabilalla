package com.pax.pay.trans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.enums.ETransResult;
import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionOfflineSend;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSendEmail;
import com.pax.pay.trans.action.ActionSendSMS;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.action.ActionUserAgreement;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.action.activity.UserAgreementActivity;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.BaseTransData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.InstalmentTransData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chenzaoyang on 2017/4/7.
 */

public class Instalment extends BaseTrans {
    private boolean isSupportBypass = false;
    private boolean isFreePin = true;

//    public Instalment(TransEndListener transListener) {
//        super(ETransType.INSTALMENT, transListener);
//
//    }
    public Instalment() {
        super(ETransType.INSTALMENT, null);

    }
    @Override
    protected void bindStateOnAction() {
        // input amount
        ActionInputTransData amountAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_instalment);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_input_amount), ActionInputTransData.EInputType.AMOUNT, 9, false);
            }
        }, 1);
        bind(Instalment.State.ENTER_AMOUNT.toString(), amountAction);
        // input discount amount
        ActionInputTransData discountAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_instalment);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_discount_amount), ActionInputTransData.EInputType.AMOUNT, 9, false);
            }
        }, 1);
        bind(Instalment.State.ENTER_DISCOUNT_AMOUNT.toString(), discountAction);
        // input tenure
        ActionInputTransData tenureAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_instalment);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_input_tenure), ActionInputTransData.EInputType.NUM,2,2, false);
            }
        }, 1);
        bind(Instalment.State.ENTER_TENURE.toString(), tenureAction);
        // input program code
        ActionInputTransData ProgramIdAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_instalment);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_input_program_id), ActionInputTransData.EInputType.NUM,6, 6, false);
            }
        }, 1);
        bind(State.ENTER_PROGRAM_ID.toString(), ProgramIdAction);
        // input product code
        ActionInputTransData ProductIdAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_instalment);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_input_product_id), ActionInputTransData.EInputType.NUM,4, 4, false);
            }
        }, 1);
        bind(State.ENTER_PRODUCT_ID.toString(), ProductIdAction);
        // read card
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(ContextUtils.getString(R.string.trans_instalment),
                        ETransType.INSTALMENT.getReadMode(), transData.getAmount(), transData.getTipAmount(), null, null,
                        ActionSearchCard.ESearchCardUIType.DEFAULT, "");
            }
        });
        bind(Instalment.State.CHECK_CARD.toString(), searchCardAction);
        // emv process action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEmvProcess) action).setParam(transData);
            }
        });
        bind(Instalment.State.EMV_PROC.toString(), emvProcessAction);
        // enter pin action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {

                // if flash pay by pwd,set isSupportBypass=false,need to enter pin
//                if (!isFreePin) {
//                    isSupportBypass = false;
//                }
                ((ActionEnterPin) action).setParam(ContextUtils.getString(R.string.trans_instalment),
                        transData.getPan(), isSupportBypass, ContextUtils.getString(R.string.prompt_pin),
                        ContextUtils.getString(R.string.prompt_no_pin), transData.getAmount(),transData.getTipAmount(), ActionEnterPin.EEnterPinType.ONLINE_PIN);
            }
        });
        bind(Instalment.State.ENTER_PIN.toString(), enterPinAction);
        // online action
        ActionTransOnline transOnlineAction = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });
        bind(Instalment.State.ONLINE.toString(), transOnlineAction);

        // signature action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setAmount(transData.getAmount());
            }
        });
        bind(Instalment.State.SIGNATURE.toString(), signatureAction);

        // Agreement action
        ActionUserAgreement userAgreemenAction = new ActionUserAgreement(null);
        bind(Instalment.State.USER_AGREEMENT.toString(), userAgreemenAction);

        //offline send
        ActionOfflineSend offlineSendAction = new ActionOfflineSend(null);
        bind(Instalment.State.OFFLINE_SEND.toString(), offlineSendAction);

        //print preview action
        ActionPrintPreview printPreviewAction = new ActionPrintPreview(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintPreview) action).setTransData(transData);
                    }
                });
        bind(Instalment.State.PRINT_PREVIEW.toString(), printPreviewAction);

        // get Telephone num
        ActionInputTransData phoneAction = new ActionInputTransData(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.paperless);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_phone_number), ActionInputTransData.EInputType.PHONE, 20, false);
            }
        }, 1);
        bind(Instalment.State.ENTER_PHONE_NUM.toString(), phoneAction);

        // get Telephone num
        ActionInputTransData emailAction = new ActionInputTransData(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.paperless);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_email_address), ActionInputTransData.EInputType.EMAIL, 100, false);
            }
        }, 1);
        bind(Instalment.State.ENTER_EMAIL.toString(), emailAction);

        // print action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintTransReceipt) action).setTransData(transData);
                    }
                });
        bind(Instalment.State.PRINT_TICKET.toString(), printTransReceiptAction);

        ActionSendSMS sendSMSAction = new ActionSendSMS(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSendSMS) action).setParam(transData);
            }
        });
        bind(Instalment.State.SEND_SMS.toString(), sendSMSAction);

        ActionSendEmail sendEmailAction = new ActionSendEmail(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSendEmail) action).setParam(transData);
            }
        });
        bind(Instalment.State.SEND_EMAIL.toString(), sendEmailAction);


        gotoState(Instalment.State.ENTER_AMOUNT.toString());
    }
    enum State {
        ENTER_AMOUNT,
        ENTER_DISCOUNT_AMOUNT,
        ENTER_TENURE,
        ENTER_PROGRAM_ID,
        ENTER_PRODUCT_ID,
        CHECK_CARD,
        ONLINE,
        EMV_PROC,
        ENTER_PIN,
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
        Instalment.State state = Instalment.State.valueOf(currentState);
        if (state == Instalment.State.EMV_PROC) {
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
        if ((state != Instalment.State.SIGNATURE) && (state != Instalment.State.PRINT_PREVIEW) &&
                (state != Instalment.State.ENTER_PHONE_NUM) && (state != Instalment.State.ENTER_EMAIL)) {
            // check action result，if failed，end the trans.
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }
        InstalmentTransData instalTransData = transData.getInstalTransData();
        switch (state) {
            case ENTER_AMOUNT:// 输入交易金额后续处理
                // save amount
                String amount = ((String) result.getData());
                transData.setAmount(amount);
                String strProductAmt = String.format("%12s",transData.getAmount());
                transData.getInstalTransData().setProductAmt(strProductAmt.replace(" ", "0"));
                LogUtils.i("Zac", transData.getInstalTransData().getProductAmt());
                gotoState(Instalment.State.ENTER_DISCOUNT_AMOUNT.toString());
                break;
            case ENTER_DISCOUNT_AMOUNT:// 输入交易金额后续处理
                // save amount
                long totalAmount = Long.parseLong(transData.getAmount());
                long discountAmount = Long.parseLong(((String) result.getData()));
                long leftAmount = totalAmount - discountAmount;
                if(leftAmount <= 0){
                    ToastUtils.showShort("折扣金额太大，请重输");
                    gotoState(State.ENTER_DISCOUNT_AMOUNT.toString());
                }else{
                    String setAmount = String.valueOf(leftAmount);
                    transData.setAmount(setAmount);
                    String discount = ((String) result.getData()).replace(".", "");
                    String strDiscountAmt= String.format("%12s",discount);
                    transData.getInstalTransData().setDiscountAmt(strDiscountAmt.replace(" ", "0"));
                    LogUtils.i("Zac", transData.getInstalTransData().getDiscountAmt());
                    gotoState(State.ENTER_TENURE.toString());
                }
                break;
            case ENTER_TENURE:
                // save amount
                String tenure = (String) result.getData();
                transData.getInstalTransData().setTenure(tenure);
                LogUtils.i("Zac", tenure);
                gotoState(State.ENTER_PROGRAM_ID.toString());
                break;
            case ENTER_PROGRAM_ID:
                // save amount
                String ProgramId= (String) result.getData();
                transData.getInstalTransData().setProgramCode(ProgramId);
                LogUtils.i("Zac", ProgramId);
                gotoState(State.ENTER_PRODUCT_ID.toString());
                break;
            case ENTER_PRODUCT_ID:
                // save amount
                String ProductId= (String) result.getData();
                transData.getInstalTransData().setProductCode(ProductId);
                LogUtils.i("Zac", ProductId);
                gotoState(State.CHECK_CARD.toString());
                break;
            case CHECK_CARD:
                ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);
                transData.setTransType(ETransType.INSTALMENT);
                mode = cardInfo.getSearchMode();
                if (mode == ActionSearchCard.SearchMode.SWIPE) {
                    // enter pin
                    transData.setTransType(ETransType.INSTALMENT);
                    LogUtils.i("Zac", Boolean.toString(transData.getTransType().equals(ETransType.INSTALMENT)));
                    gotoState(State.ENTER_PIN.toString());
                }else if (mode == ActionSearchCard.SearchMode.INSERT) {
                    gotoState(Instalment.State.EMV_PROC.toString());
                }
            break;
            case EMV_PROC: // emv后续处理
                onEmvProcessResult(result);
                break;

            case ENTER_PIN: // subsequent processing of enter pin
                String pinBlock = (String) result.getData();
                transData.setPin(pinBlock);
                if (pinBlock != null && pinBlock.length() > 0) {
                    transData.setHasPin(true);
                }

                if (transData.getIssuer().getFloorLimit() > Long.parseLong(transData.getAmount())) {
                    if (transData.getEnterMode() == BaseTransData.EnterMode.SWIPE) {
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
                gotoState(Instalment.State.ONLINE.toString());
                break;
            case ONLINE: // subsequent processing of online
                toSignOrPrint();
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
                if (transData.getTransType().equals(ETransType.INSTALMENT)) {
                    List<ETransType> list = new ArrayList<>();
                    list.add(ETransType.OFFLINE_TRANS_SEND);
                    List<TransData.ETransStatus> filter = new ArrayList<>();
                    filter.add(TransData.ETransStatus.VOIDED);
                    filter.add(TransData.ETransStatus.ADJUSTED);
                    List<TransData> offlineTransList = DbManager.getTransDao().findTransData(list, filter);
                    if (offlineTransList != null) {
                        if (offlineTransList.size() != 0) {
                            //offline send
                            gotoState(Instalment.State.OFFLINE_SEND.toString());
                            break;
                        }
                    }
                }
                // if terminal does not support signature ,card holder does not sign or time out，print preview directly.
                gotoState(Instalment.State.PRINT_PREVIEW.toString());
                break;
            case USER_AGREEMENT:
                String agreement = (String) result.getData();
                if (agreement != null && agreement.equals(UserAgreementActivity.ENTER_BUTTON)) {
                    gotoState(Instalment.State.CHECK_CARD.toString());
                } else {
                    //gotoState(State.ENTER_AMOUNT.toString());
                    transEnd(result);
                }
                break;
            case OFFLINE_SEND:
                gotoState(Instalment.State.PRINT_PREVIEW.toString());
                break;
            case PRINT_PREVIEW:
                goPrintBranch(result);
                break;
            case ENTER_PHONE_NUM:
                if (result.getRet() == TransResult.SUCC) {
                    transData.setPhoneNum((String) result.getData());
                    gotoState(Instalment.State.SEND_SMS.toString());
                } else {
                    gotoState(Instalment.State.PRINT_PREVIEW.toString());
                }
                break;
            case ENTER_EMAIL:
                if (result.getRet() == TransResult.SUCC) {
                    transData.setPhoneNum((String) result.getData());
                    gotoState(Instalment.State.SEND_EMAIL.toString());
                } else {
                    gotoState(Instalment.State.PRINT_PREVIEW.toString());
                }
                break;
            case PRINT_TICKET:
            case SEND_SMS:
            case SEND_EMAIL:
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
            gotoState(Instalment.State.PRINT_TICKET.toString());
        } else if (string != null && string.equals(PrintPreviewActivity.SMS_BUTTON)) {
            gotoState(Instalment.State.ENTER_PHONE_NUM.toString());
        } else if (string != null && string.equals(PrintPreviewActivity.EMAIL_BUTTON)) {
            gotoState(Instalment.State.ENTER_EMAIL.toString());
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
            gotoState(Instalment.State.PRINT_PREVIEW.toString());
        } else {
            transData.setSignFree(false);
            gotoState(Instalment.State.SIGNATURE.toString());
        }
        DbManager.getTransDao().updateTransData(transData);
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
                gotoState(Instalment.State.ENTER_PIN.toString());
                return;
            }

            if (transResult == ETransResult.ARQC) {
                if (!Component.isQpbocNeedOnlinePin()) {
                    gotoState(Instalment.State.ONLINE.toString());
                    return;
                }
            }
            if (Component.clssQPSProcess(transData)) { // pin free
                transData.setPinFree(true);
                gotoState(Instalment.State.ONLINE.toString());
            } else {
                // input password
                transData.setPinFree(false);
                gotoState(Instalment.State.ENTER_PIN.toString());
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
}
