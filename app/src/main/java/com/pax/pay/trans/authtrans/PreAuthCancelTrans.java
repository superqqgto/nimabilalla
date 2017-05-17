package com.pax.pay.trans.authtrans;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.trans.model.ETransType;

/**
 * Created by huangmuhua on 2017/4/13.
 */

public class PreAuthCancelTrans extends BaseAuthTrans {

    public PreAuthCancelTrans() {
        super(ETransType.PREAUTH_CANCEL, R.string.trans_preauth_cancel, null);
    }

    @Override
    protected void bindStateOnAction() {

        bindEnterAuthCode();
        bindTransDetail();
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

    protected void deleteTransFromTabBatch() {
        DbManager.getTabBatchTransDao().deleteTabBatchTransDataByTraceNo(origTransData.getTraceNo());
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
                gotoState(State.CHECK_CARD.toString());
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
