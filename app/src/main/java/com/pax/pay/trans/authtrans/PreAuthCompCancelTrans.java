package com.pax.pay.trans.authtrans;

import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.authtrans.BaseAuthTrans;
import com.pax.pay.trans.model.ETransType;

/**
 * Created by huangmuhua on 2017/4/13.
 */

public class PreAuthCompCancelTrans extends BaseAuthTrans {

    public PreAuthCompCancelTrans() {
        super(ETransType.PREAUTH_COMP_CANCEL, null);
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
        super.onActionResult(currentState,result);

        switch (state) {
            case ENTER_AUTH_CODE:
                onEnterAuthCodeResult(result);
//                gotoState(State.ONLINE.toString());
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
