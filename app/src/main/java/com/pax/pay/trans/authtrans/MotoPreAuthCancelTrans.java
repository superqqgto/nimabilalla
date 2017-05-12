package com.pax.pay.trans.authtrans;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;

/**
 * Created by zhouhong on 2017/5/9.
 */

public class MotoPreAuthCancelTrans extends BaseMotoPreAuthTrans {

    public MotoPreAuthCancelTrans(boolean isFreePin) {
        super(ETransType.MOTO_PREAUTH_CANCEL, null);
        transNameResId = R.string.trans_moto_preauth_cancel;
        this.isFreePin = isFreePin;
    }

    @Override
    protected void bindStateOnAction() {
        bindEnterAuthCode();
        bindTransDetail();
        bindEnterCVV2();
        bindEnterPin();
        bindOnline();
        bindSignature();
        bindOfflienSend();
        bindPrintPreview();
        bindPrintReceipt();
        gotoState(State.ENTER_AUTH_CODE.toString());
    }

    @Override
    protected void onOnlineResult(ActionResult result) {

        if (Component.isSignatureFree(transData)) {// 免签
            transData.setSignFree(true);
            // 打印
            gotoState(State.PRINT_PREVIEW.toString());
        } else {
            // 电子签名
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }
        DbManager.getTransDao().updateTransData(transData);
        deleteTransFromMotoTabBatch(); //从mototabbatch里面删除被void的moto preAuth

        gotoState(State.SIGNATURE.toString());
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        super.onActionResult(currentState, result);

        switch (state) {
            case ENTER_AUTH_CODE:
                onEnterAuthCodeResult(result);
//                gotoState(State.TRANS_DETAIL.toString());
                break;
            case TRANS_DETAIL:
                gotoState(State.ENTER_CVV2.toString());
                break;
            case ENTER_CVV2:
                onEnterCVV2Result(result);
                gotoState(State.ENTER_PIN.toString());
                break;
            case ENTER_PIN:
                onEnterPinResult(result);
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE:
                onOnlineResult(result);
                break;
            case SIGNATURE://输入金额之后的处理
                onSignatureResult(result);
                break;
            case OFFLINE_SEND:
                gotoState(State.PRINT_PREVIEW.toString());
                break;
            case PRINT_PREVIEW:
                onPrintPreviewResult(result);
                break;
            case PRINT_RECEIPT:
                transEnd(result);
                break;
            default:
                transEnd(result);
                break;
        }
    }

}

