package com.pax.pay.trans.authtrans;

import com.pax.abl.core.ActionResult;
import com.pax.manager.DbManager;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

/**
 * Created by huangmuhua on 2017/4/18.
 */

public class PreAuthCompOfflineTrans extends BaseAuthTrans {

    public PreAuthCompOfflineTrans() {
        super(ETransType.PREAUTH_COMP_OFFLINE, null);
    }

    @Override
    protected void bindStateOnAction() {
        bindEnterAuthCode();
        bindTransDetail();
        bindEnterAmout();
        bindCheckCard();
        bindEnterPin();
        bindSignature();
        bindPrintPreview();
        bindPrintReceipt();

        gotoState(State.ENTER_AUTH_CODE.toString());//跳到第一个action
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        super.onActionResult(currentState, result);

        switch (state) {
            case ENTER_AUTH_CODE:
                onEnterAuthCodeResult(result);
                //linzhao
//                gotoState(State.TRANS_DETAIL.toString());
                break;
            case TRANS_DETAIL:
                gotoState(State.ENTER_AMOUNT.toString());
                break;
            case ENTER_AMOUNT://输入金额之后的处理
                onEnterAmountResult(result);
                break;
            case CHECK_CARD://寻卡之后的处理
                onCheckCardResult(result);
                break;
            case ENTER_PIN:
                onEnterPinResult(result);
                // save trans data
                transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
                DbManager.getTransDao().insertTransData(transData);
                //increase trans no.
                Component.incTransNo();
                gotoState(State.SIGNATURE.toString());
                break;
            case SIGNATURE://输入金额之后的处理
                onSignatureResult(result);
                break;
            case PRINT_PREVIEW://寻卡之后的处理
                onPrintPreviewResult(result);
                break;
            case PRINT_RECEIPT://
                transEnd(result);
                break;
            default:
                transEnd(result);
                break;
        }
    }
}
