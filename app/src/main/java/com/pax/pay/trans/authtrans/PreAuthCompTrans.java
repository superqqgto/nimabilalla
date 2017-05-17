package com.pax.pay.trans.authtrans;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;

/**
 * Created by huangmuhua on 2017/3/20.
 */

public class PreAuthCompTrans extends BaseAuthTrans {

    private boolean isNeedInputAmount = true; // is need input amount

    public PreAuthCompTrans() {
        super(ETransType.PREAUTH_COMP, R.string.trans_preauth_comp, null);
    }

    @Override
    protected void bindStateOnAction() {

        bindEnterAuthCode();
        bindTransDetail();
        bindEnterAmount();
        bindCheckCard();
        bindEmvProcess();
        bindEnterPin();
        bindOnline();
        bindSignature();
        bindPrintPreview();
        bindPrintReceipt();

        gotoState(State.ENTER_AUTH_CODE.toString());//跳到第一个action
    }

    @Override
    protected void toSignOrPrint() {
        super.toSignOrPrint();
        DbManager.getTransDao().updateTransData(transData);
        deleteTransFromTabBatch(); //从mototabbatch里面删除被void的moto preAuth
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        if (isEndForward(currentState, result)) {
            return;
        }

        switch (state) {
            case ENTER_AUTH_CODE:
                onEnterAuthCodeResult(result);
                gotoState(State.TRANS_DETAIL.toString());
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
            case EMV_PROC: // emv后续处理
                onEmvProcessResult(result);
                break;
            case ENTER_PIN:
                onEnterPinResult(result);
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE:
//                onOnlineResult(result);
                toSignOrPrint();
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
