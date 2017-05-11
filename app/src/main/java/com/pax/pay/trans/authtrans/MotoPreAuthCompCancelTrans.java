package com.pax.pay.trans.authtrans;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.trans.model.ETransType;

/**
 * Created by zhouhong on 2017/5/9.
 */

public class MotoPreAuthCompCancelTrans extends BaseMotoPreAuthTrans {
    public MotoPreAuthCompCancelTrans(boolean isFreePin) {
        super(ETransType.MOTO_PREAUTH_COMP_CANCEL, null);
        transNameResId = R.string.trans_moto_preauth_comp_cancel;
        this.isFreePin = isFreePin;
    }

    @Override
    protected void bindStateOnAction() {
        bindEnterAuthCode();
        bindOnline();
        bindSignature();
        bindPrintPreview();
        bindPrintReceipt();
        gotoState(State.ENTER_AUTH_CODE.toString());
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        super.onActionResult(currentState, result);

        switch (state) {
            case ENTER_AUTH_CODE:
                onEnterAuthCodeResult(result);
                gotoState(State.TRANS_DETAIL.toString());
                break;
            case TRANS_DETAIL:
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE:
                onOnlineResult(result);
                break;
            case SIGNATURE://输入金额之后的处理
                onSignatureResult(result);
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
