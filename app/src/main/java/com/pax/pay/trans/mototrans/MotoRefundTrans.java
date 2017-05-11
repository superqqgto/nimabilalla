package com.pax.pay.trans.mototrans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.BaseTrans;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionOfflineSend;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.MotoTransData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzaoyang on 2017/5/5.
 */

public class MotoRefundTrans extends BaseTrans {
    public MotoRefundTrans() {
        super(ETransType.MOTOREFUND, null);
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
        bind(MotoRefundTrans.State.INPUT_PWD.toString(), inputPasswordAction);
        // input amount
        ActionInputTransData amountAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(ContextUtils.getString(R.string.trans_moto_refund))
                        .setInputLine1(ContextUtils.getString(R.string.prompt_input_moto_refund_amount), ActionInputTransData.EInputType.AMOUNT, Constants.AMOUNT_DIGIT,
                                false);
            }
        }, 1);
        bind(MotoRefundTrans.State.ENTER_AMOUNT.toString(), amountAction);
        // input cvv2
        ActionInputTransData cvv2Action = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_moto_refund);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_input_cvv2), ActionInputTransData.EInputType.NUM, 4, 3, false);
            }
        }, 1);
        bind(MotoRefundTrans.State.ENTER_CVV2.toString(), cvv2Action);
        // search card
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(ContextUtils.getString(R.string.trans_moto_refund),
                        ETransType.MOTOREFUND.getReadMode(), transData.getAmount(), transData.getTipAmount(), null, null,
                        ActionSearchCard.ESearchCardUIType.DEFAULT, "");
            }
        });
        bind(MotoRefundTrans.State.CHECK_CARD.toString(), searchCardAction);
        // input password action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {


            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(
                        ContextUtils.getString(R.string.trans_refund), transData.getPan(), true,
                        ContextUtils.getString(R.string.prompt_pin),
                        ContextUtils.getString(R.string.prompt_no_pin),
                        "-" + transData.getAmount(),
                        transData.getTipAmount(),
                        ActionEnterPin.EEnterPinType.ONLINE_PIN);
            }
        });
        bind(MotoRefundTrans.State.ENTER_PIN.toString(), enterPinAction);
        // online action
        ActionTransOnline transOnlineAction = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });

        bind(MotoRefundTrans.State.ONLINE.toString(), transOnlineAction);
        // signature action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setAmount(transData.getAmount());
            }
        });
        bind(MotoRefundTrans.State.SIGNATURE.toString(), signatureAction);
        //offline send
        ActionOfflineSend offlineSendAction = new ActionOfflineSend(null);
        bind(MotoRefundTrans.State.OFFLINE_SEND.toString(), offlineSendAction);
        //print preview
        ActionPrintPreview printPreviewAction = new ActionPrintPreview(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionPrintPreview) action).setTransData(transData);
            }
        });
        bind(MotoRefundTrans.State.PRINT_PREVIEW.toString(), printPreviewAction);
        // print action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintTransReceipt) action).setTransData(transData);
                    }
                });
        bind(MotoRefundTrans.State.PRINT_TICKET.toString(), printTransReceiptAction);
        // whether need input management password for void and refund
        if (SpManager.getSysParamSp().get(SysParamSp.OTHTC_VERIFY).equals(SysParamSp.Constant.YES)) {
            gotoState(MotoRefundTrans.State.INPUT_PWD.toString());
        } else {
            gotoState(MotoRefundTrans.State.ENTER_AMOUNT.toString());
        }
    }
    enum State {
        INPUT_PWD,
        ENTER_AMOUNT,
        CHECK_CARD,
        ENTER_PIN,
        ENTER_CVV2,
        ONLINE,
        SIGNATURE,
        OFFLINE_SEND,
        PRINT_PREVIEW,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        MotoRefundTrans.State state = MotoRefundTrans.State.valueOf(currentState);
        if ((state != MotoRefundTrans.State.SIGNATURE) && (state != MotoRefundTrans.State.PRINT_PREVIEW)) {
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
                gotoState(MotoRefundTrans.State.CHECK_CARD.toString());
                break;
            case ENTER_AMOUNT:
                String amount = ((String) result.getData()).replace(".", "");
                if (!checkAmount(amount)) {
                    transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
                    return;
                }
                transData.setAmount(amount);

                gotoState(MotoRefundTrans.State.ENTER_PIN.toString());

                break;
            case CHECK_CARD: // check card
                ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);
                gotoState(MotoRefundTrans.State.ENTER_AMOUNT.toString());
                break;
            case ENTER_PIN: // enter pin
                String pinBlock = (String) result.getData();
                transData.setPin(pinBlock);
                if (pinBlock != null && pinBlock.length() > 0) {
                    transData.setHasPin(true);
                }
                // ENTER CVV2
                gotoState(State.ENTER_CVV2.toString());
                break;
            case ENTER_CVV2:
                //online
                String cvv2 = (String) result.getData();
                transData.setCVV2(cvv2);
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE: // after online
                transData.setOnlineTrans(true);
                DbManager.getTransDao().updateTransData(transData);
                saveTransToMotoBatch();
                gotoState(MotoRefundTrans.State.SIGNATURE.toString());
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
                if (offlineTransList != null)
                    if (offlineTransList.size() != 0) {
                        //offline send
                        gotoState(MotoRefundTrans.State.OFFLINE_SEND.toString());
                        break;
                    }
                // if terminal not support electronic signature, user do not make signature or signature time out, print preview
                gotoState(MotoRefundTrans.State.PRINT_PREVIEW.toString());
                break;
            case OFFLINE_SEND:
                gotoState(MotoRefundTrans.State.PRINT_PREVIEW.toString());
                break;
            case PRINT_PREVIEW:
                String string = (String) result.getData();
                if (string != null && string.equals(PrintPreviewActivity.PRINT_BUTTON)) {
                    //print ticket
                    gotoState(MotoRefundTrans.State.PRINT_TICKET.toString());
                } else {
                    //end trans directly, not print
                    transEnd(result);
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

    private void saveTransToMotoBatch() {

        MotoTransData motoTransData = new MotoTransData(transData);
        DbManager.getMotoTransDao().insertMotoTransData(motoTransData);
    }

    private boolean checkAmount(String amountStr) {
        long amount = Long.parseLong(amountStr);
        long amountMax = Long.parseLong(SpManager.getSysParamSp().get(SysParamSp.OTHTC_REFUNDLIMT)) * 100;

        return (amount <= amountMax);
    }
}
