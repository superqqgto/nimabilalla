package com.pax.pay.trans.authtrans;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.trans.model.ETransType;

/**
 * Created by huangmuhua on 2017/4/13.
 */

public class PreAuthCancelTrans extends BaseAuthTrans {

    public PreAuthCancelTrans() {
        super(ETransType.PREAUTH_CANCEL, null);
        transNameResId = R.string.trans_preauth_cancel;
    }

    @Override
    protected void bindStateOnAction() {

        bindEnterAuthCode();
        bindTransDetail();
        bindOnline();
        bindSignature();
        bindPrintPreview();
        bindPrintReceipt();

        gotoState(State.ENTER_AUTH_CODE.toString());//跳到第一个action
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        super.onActionResult(currentState,result);

        switch (state) {
            case ENTER_AUTH_CODE:
                onEnterAuthCodeResult(result);
//                gotoState(State.TRANS_DETAIL.toString());
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
