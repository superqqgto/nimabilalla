package com.pax.pay.trans.authtrans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.trans.BaseTrans;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionOfflineSend;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.BaseTransData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.MotoTabBatchTransData;
import com.pax.pay.trans.model.TransData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouhong on 2017/5/5.
 */

public class MotoPreAuthTrans extends BaseTrans {

    private String amount;
//    private long motoFloorLimit = Long.parseLong(SpManager.getSysParamSp().get(SysParamSp.MOTO_FLOOR_LIMIT));
    private boolean isNeedInputAmount = true; // is need input amount
    private boolean isFreePin = true;
    boolean isSupportBypass = true;
    private int transNameResId = R.string.trans_moto_preAuth;

    public MotoPreAuthTrans(boolean isFreePin) {
        super(ETransType.MOTO_PREAUTH, null);
        this.isFreePin = isFreePin;
        isNeedInputAmount = true;
    }

    public MotoPreAuthTrans(String amount, ATransaction.TransEndListener transListener) {
        super(ETransType.MOTO_PREAUTH, transListener);
        this.amount = amount;
        isNeedInputAmount = false;
    }

    @Override
    protected void bindStateOnAction() {

        bindEnterAmount();
        bindSearchCard();
        bindEnterPin();
        bindEnterCVV2();
        bindOnline();
        bindSignature();
        bindOfflienSend();
        bindPrintPreview();
        bindPrintReceipt();

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
                .inputType1(ActionInputTransData.EInputType.AMOUNT)
                .maxLen1(9)
                .isVoidLastTrans(false);

        bind(State.ENTER_AMOUNT.toString(), inputBuilder.create());
    }

    private void bindSearchCard() {
        // search card action
        ActionSearchCard.Builder searchBuilder = new ActionSearchCard.Builder()
                .transName(transNameResId)
                .cardReadMode(ETransType.MOTO_PREAUTH.getReadMode())
                .searchCardPrompt(R.string.prompt_swipe_card)
                .searchCardUIType(ActionSearchCard.ESearchCardUIType.DEFAULT)
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ActionSearchCard searchAct = (ActionSearchCard) action;
                        searchAct.setAmount(transData.getAmount());
                        searchAct.setTipAmount(transData.getTipAmount());
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

    private void bindEnterCVV2() {
        ActionInputTransData.Builder inputTransDataBuilder = new ActionInputTransData.Builder()
                .transName(transNameResId)
                .lineNum(1)
                .prompt1(R.string.prompt_input_cvv2)
                .inputType1(ActionInputTransData.EInputType.NUM)
                .minLen1(3)
                .maxLen1(4)
                .isVoidLastTrans(false);

        bind(State.ENTER_CVV2.toString(), inputTransDataBuilder.create());
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

    private void bindOfflienSend() {
        ActionOfflineSend.Builder offlineSendBuilder = new ActionOfflineSend.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionOfflineSend) action).setTransData(transData);
                    }
                });

        bind(State.OFFLINE_SEND.toString(), offlineSendBuilder.create());
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
        ENTER_CVV2,
        ONLINE,
        SIGNATURE,
        OFFLINE_SEND, //上传offline交易，piggyback
        PRINT_PREVIEW,
        PRINT_RECEIPT
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
            case ENTER_AMOUNT:// 输入交易金额后续处理
                // save amount
                String amount = ((String) result.getData()).replace(".", "");
                transData.setAmount(amount);
                gotoState(State.CHECK_CARD.toString());
                break;
            case CHECK_CARD: // 检测卡的后续处理
                ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);

                // 手输卡号处理
                byte mode = cardInfo.getSearchMode();
                if (mode == ActionSearchCard.SearchMode.KEYIN) {
                    // input password
                    gotoState(State.ENTER_CVV2.toString());
                }
                break;
            case ENTER_PIN: // 输入密码的后续处理
                String pinBlock = (String) result.getData();
                transData.setPin(pinBlock);
                if (pinBlock != null && pinBlock.length() > 0) {
                    transData.setHasPin(true);
                }

                gotoState(State.ONLINE.toString());
                break;
            case ENTER_CVV2:
                String cvv2 = (String) result.getData();
                transData.setCVV2(cvv2);
                gotoState(State.ENTER_PIN.toString());
                break;

            case ONLINE: // after online
                //此处需要存取online响应报文中的authcode、responseCode字段
                saveTransToTabBatch();

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

                //get offline trans data list， piggyback
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
                // if terminal not support electronic signature or user do not make signature or signature time out, print preview
                gotoState(State.PRINT_PREVIEW.toString());

                break;
            case OFFLINE_SEND:
                gotoState(State.PRINT_PREVIEW.toString());
            case PRINT_PREVIEW:
                String string = (String) result.getData();
                if (string != null && string.equals(PrintPreviewActivity.PRINT_BUTTON)) {
                    //print ticket
                    gotoState(State.PRINT_RECEIPT.toString());
                } else {
                    //end trans directly, not print
                    transEnd(result);
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
        MotoTabBatchTransData motoTabBatchTransData = new MotoTabBatchTransData(transData);
        DbManager.getMotoTabBatchTransDao().insertTransData(motoTabBatchTransData);
    }

    private void updateTransToTabBatch() {
        MotoTabBatchTransData motoTabBatchTransData = new MotoTabBatchTransData(transData);
        DbManager.getMotoTabBatchTransDao().updateTransData(motoTabBatchTransData);
    }

    // need electronic signature or send
    private void toSignOrPrint() {
        if (Component.isSignatureFree(transData)) {// signature free
            transData.setSignFree(true);
            // print preview
            gotoState(MotoPreAuthTrans.State.PRINT_PREVIEW.toString());
        } else {
            transData.setSignFree(false);
            gotoState(MotoPreAuthTrans.State.SIGNATURE.toString());
        }
        DbManager.getTransDao().updateTransData(transData);
        updateTransToTabBatch();
    }
}
