package com.pax.pay.trans.mototrans;

import android.content.Context;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.trans.BaseTrans;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionAdjustTip;
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
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.MotoTransData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.view.dialog.CustomAlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzaoyang on 2017/4/14.
 */

public class MotoSaleTrans extends BaseTrans {
    private float percent;
    private boolean isFreePin = true;
    private boolean isSupportBypass = true;
    private boolean hasTip = false;
    private long motoFloorLimit = Long.parseLong(SpManager.getSysParamSp().get(SysParamSp.MOTO_FLOOR_LIMIT));
    boolean enableMotoSale = SpManager.getSysParamSp().get(SysParamSp.SUPPORT_MOTOSALE).equals(SysParamSp.Constant.YES);

    public MotoSaleTrans() {
        super(ETransType.MOTOSALE, null);
    }

    @Override
    protected void bindStateOnAction() {
        // input amount
        ActionInputTransData amountAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_moto_sale);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_input_amount), ActionInputTransData.EInputType.AMOUNT, 9, false);
            }
        }, 1);
        bind(MotoSaleTrans.State.ENTER_AMOUNT.toString(), amountAction);
        // read card
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(ContextUtils.getString(R.string.trans_moto_sale),
                        ETransType.MOTOSALE.getReadMode(), transData.getAmount(), transData.getTipAmount(), null, null,
                        ActionSearchCard.ESearchCardUIType.DEFAULT, "");
            }
        });
        bind(MotoSaleTrans.State.CHECK_CARD.toString(), searchCardAction);

        //adjust tip action
        ActionAdjustTip adjustTipAction = new ActionAdjustTip(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String amount = String.valueOf(Long.parseLong(transData.getAmount()) - Long.parseLong(transData.getTipAmount()));
                ((ActionAdjustTip) action).setParam(ContextUtils.getString(R.string.trans_moto_sale), amount, percent);
            }
        });
        bind(MotoSaleTrans.State.ADJUST_TIP.toString(), adjustTipAction);
        // enter pin action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {

                // if flash pay by pwd,set isSupportBypass=false,need to enter pin
//                if (!isFreePin) {
//                    isSupportBypass = false;
//                }
                ((ActionEnterPin) action).setParam(ContextUtils.getString(R.string.trans_moto_sale),
                        transData.getPan(), isSupportBypass, ContextUtils.getString(R.string.prompt_pin),
                        ContextUtils.getString(R.string.prompt_no_pin), transData.getAmount(),transData.getTipAmount(), ActionEnterPin.EEnterPinType.ONLINE_PIN);
            }
        });
        bind(MotoSaleTrans.State.ENTER_PIN.toString(), enterPinAction);
        // input cvv2
        ActionInputTransData cvv2Action = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_moto_sale);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_input_cvv2), ActionInputTransData.EInputType.NUM, 4, 3, false);
            }
        }, 1);
        bind(State.ENTER_CVV2.toString(), cvv2Action);
        // online action
        ActionTransOnline transOnlineAction = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });
        bind(MotoSaleTrans.State.ONLINE.toString(), transOnlineAction);

        // signature action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setAmount(transData.getAmount());
            }
        });
        bind(MotoSaleTrans.State.SIGNATURE.toString(), signatureAction);

        // Agreement action
        ActionUserAgreement userAgreemenAction = new ActionUserAgreement(null);
        bind(MotoSaleTrans.State.USER_AGREEMENT.toString(), userAgreemenAction);

        //offline send
        ActionOfflineSend offlineSendAction = new ActionOfflineSend(null);
        bind(MotoSaleTrans.State.OFFLINE_SEND.toString(), offlineSendAction);

        //print preview action
        ActionPrintPreview printPreviewAction = new ActionPrintPreview(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintPreview) action).setTransData(transData);
                    }
                });
        bind(MotoSaleTrans.State.PRINT_PREVIEW.toString(), printPreviewAction);

        // get Telephone num
        ActionInputTransData phoneAction = new ActionInputTransData(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.paperless);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_phone_number), ActionInputTransData.EInputType.PHONE, 20, false);
            }
        }, 1);
        bind(MotoSaleTrans.State.ENTER_PHONE_NUM.toString(), phoneAction);

        // get Telephone num
        ActionInputTransData emailAction = new ActionInputTransData(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.paperless);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_email_address), ActionInputTransData.EInputType.EMAIL, 100, false);
            }
        }, 1);
        bind(MotoSaleTrans.State.ENTER_EMAIL.toString(), emailAction);

        // print action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintTransReceipt) action).setTransData(transData);
                    }
                });
        bind(MotoSaleTrans.State.PRINT_TICKET.toString(), printTransReceiptAction);

        ActionSendSMS sendSMSAction = new ActionSendSMS(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSendSMS) action).setParam(transData);
            }
        });
        bind(MotoSaleTrans.State.SEND_SMS.toString(), sendSMSAction);

        ActionSendEmail sendEmailAction = new ActionSendEmail(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSendEmail) action).setParam(transData);
            }
        });
        bind(MotoSaleTrans.State.SEND_EMAIL.toString(), sendEmailAction);

        // perform the first action
        gotoState(MotoSaleTrans.State.ENTER_AMOUNT.toString());

    }

    enum State {
        ENTER_AMOUNT,
        CHECK_CARD,
        ADJUST_TIP,
        ENTER_PIN,
        ENTER_CVV2,
        ONLINE,
        SIGNATURE,
        USER_AGREEMENT,
        OFFLINE_SEND,
        PRINT_PREVIEW,
        PRINT_TICKET,
        ENTER_PHONE_NUM,
        ENTER_EMAIL,
        SEND_SMS,
        SEND_EMAIL
    }

    private byte mode;
    @Override
    public void onActionResult(String currentState, ActionResult result) {
        MotoSaleTrans.State state = MotoSaleTrans.State.valueOf(currentState);
        if ((state != MotoSaleTrans.State.SIGNATURE) && (state != MotoSaleTrans.State.PRINT_PREVIEW) &&
                (state != MotoSaleTrans.State.ENTER_PHONE_NUM) && (state != MotoSaleTrans.State.ENTER_EMAIL)) {
            // check action result，if failed，end the trans.
            int ret = result.getRet();
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
                transData.setTipAmount("0");//Zac
                gotoState(MotoSaleTrans.State.CHECK_CARD.toString());
                break;
            case CHECK_CARD: // subsequent processing of check card
                ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);
                transData.setTransType(ETransType.MOTOSALE);
                // enter card number manually
                mode = cardInfo.getSearchMode();
                if (mode == ActionSearchCard.SearchMode.KEYIN) {
                    boolean enableTip = SpManager.getSysParamSp().get(SysParamSp.EDC_SUPPORT_TIP).equals(SysParamSp.Constant.YES);
                    long totalAmount = Long.parseLong(transData.getAmount());
                    long tipAmount = Long.parseLong(transData.getTipAmount());
                    long baseAmount = totalAmount - tipAmount;
                    percent = transData.getIssuer().getAdjustPercent();
                    if (enableTip) {
                        if (!hasTip)
                            gotoState(MotoSaleTrans.State.ADJUST_TIP.toString());
                        else if (baseAmount * percent / 100 < tipAmount)
                            showAdjustTipDialog();
                    } else {
                        // enter pin
                        gotoState(MotoSaleTrans.State.ENTER_PIN.toString());
                    }
                }
                break;
            case ADJUST_TIP:
                //get total amount
                String totalAmountStr = String.valueOf(CurrencyConverter.parse(result.getData().toString()));
                transData.setAmount(totalAmountStr);
                //get tip amount
                String tip = String.valueOf(CurrencyConverter.parse(result.getData1().toString()));
                transData.setTipAmount(tip);
                // enter pin
                gotoState(MotoSaleTrans.State.ENTER_PIN.toString());
                break;
            case ENTER_PIN: // subsequent processing of enter pin
                String pinBlock = (String) result.getData();
                transData.setPin(pinBlock);
                if (pinBlock != null && pinBlock.length() > 0) {
                    transData.setHasPin(true);
                }
//FinancialApplication.acqManager.getCurIssuer().getFloorLimit()
                if (motoFloorLimit > Long.parseLong(transData.getAmount())) {
                    transData.setTransType(ETransType.OFFLINE_TRANS_SEND);
                    // save trans data
                    transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
                    DbManager.getTransDao().insertTransData(transData);

                    saveTransToMotoBatch();
                    //increase trans no.
                    Component.incTransNo();
                    toSignOrPrint();
                    break;
                } else {
                    gotoState(State.ENTER_CVV2.toString());
                    break;
                }
            case ENTER_CVV2:
                String cvv2 = (String) result.getData();
                transData.setCVV2(cvv2);
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE: // subsequent processing of online
                transData.setOnlineTrans(true);
                DbManager.getTransDao().updateTransData(transData);
                saveTransToMotoBatch();
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
                if (transData.getTransType().equals(ETransType.MOTOSALE)) {
                    List<ETransType> list = new ArrayList<>();
                    list.add(ETransType.OFFLINE_TRANS_SEND);
                    List<TransData.ETransStatus> filter = new ArrayList<>();
                    filter.add(TransData.ETransStatus.VOIDED);
                    filter.add(TransData.ETransStatus.ADJUSTED);
                    List<TransData> offlineTransList = DbManager.getTransDao().findTransData(list, filter);
                    if (offlineTransList != null) {
                        if (offlineTransList.size() != 0) {
                            //offline send
                            gotoState(MotoSaleTrans.State.OFFLINE_SEND.toString());
                            break;
                        }
                    }
                }
                // if terminal does not support signature ,card holder does not sign or time out，print preview directly.
                gotoState(MotoSaleTrans.State.PRINT_PREVIEW.toString());
                break;
            case USER_AGREEMENT:
                String agreement = (String) result.getData();
                if (agreement != null && agreement.equals(UserAgreementActivity.ENTER_BUTTON)) {
                    gotoState(MotoSaleTrans.State.CHECK_CARD.toString());
                } else {
                    //gotoState(State.ENTER_AMOUNT.toString());
                    transEnd(result);
                }
                break;
            case OFFLINE_SEND:
                gotoState(MotoSaleTrans.State.PRINT_PREVIEW.toString());
                break;
            case PRINT_PREVIEW:
                goPrintBranch(result);
                break;
            case ENTER_PHONE_NUM:
                if (result.getRet() == TransResult.SUCC) {
                    transData.setPhoneNum((String) result.getData());
                    gotoState(MotoSaleTrans.State.SEND_SMS.toString());
                } else {
                    gotoState(MotoSaleTrans.State.PRINT_PREVIEW.toString());
                }
                break;
            case ENTER_EMAIL:
                if (result.getRet() == TransResult.SUCC) {
                    transData.setPhoneNum((String) result.getData());
                    gotoState(MotoSaleTrans.State.SEND_EMAIL.toString());
                } else {
                    gotoState(MotoSaleTrans.State.PRINT_PREVIEW.toString());
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

    private void saveTransToMotoBatch() {
        MotoTransData motoTransData = new MotoTransData(transData);
        DbManager.getMotoTransDao().insertMotoTransData(motoTransData);

    }

    private void goPrintBranch(ActionResult result) {
        String string = (String) result.getData();
        if (string != null && string.equals(PrintPreviewActivity.PRINT_BUTTON)) {
            //print ticket
            gotoState(MotoSaleTrans.State.PRINT_TICKET.toString());
        } else if (string != null && string.equals(PrintPreviewActivity.SMS_BUTTON)) {
            gotoState(MotoSaleTrans.State.ENTER_PHONE_NUM.toString());
        } else if (string != null && string.equals(PrintPreviewActivity.EMAIL_BUTTON)) {
            gotoState(MotoSaleTrans.State.ENTER_EMAIL.toString());
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
            gotoState(MotoSaleTrans.State.PRINT_PREVIEW.toString());
        } else {
            transData.setSignFree(false);
            gotoState(MotoSaleTrans.State.SIGNATURE.toString());
        }
        DbManager.getTransDao().updateTransData(transData);
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
                gotoState(MotoSaleTrans.State.ADJUST_TIP.toString());
            }
        });
        dialog.show();
        dialog.setNormalText(ContextUtils.getString(R.string.prompt_tip_exceed));
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);
    }

    public void showAdjustTipDialog() {
        showAdjustTipDialog(ContextUtils.getActyContext());
    }
}

