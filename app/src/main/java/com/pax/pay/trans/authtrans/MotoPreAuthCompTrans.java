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
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.model.ETransType;

/**
 * Created by zhouhong on 2017/5/6.
 */

public class MotoPreAuthCompTrans extends BaseMotoPreAuthTrans {

    public MotoPreAuthCompTrans(boolean isFreePin) {
        super(ETransType.MOTO_PREAUTH_COMP, null);
        transNameResId = R.string.trans_moto_preauth_comp;
        this.isFreePin = isFreePin;
    }

    @Override
    protected void bindStateOnAction() {

        bindEnterAuthCode();
        bindTransDetail();
        bindEnterAmout();
        bindCheckCard();
        bindEnterCVV2();
        bindEnterPin();
        bindOnline();
        bindSignature();
        bindPrintPreview();
        bindPrintReceipt();

        gotoState(BaseAuthTrans.State.ENTER_AUTH_CODE.toString());//跳到第一个action
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
                gotoState(State.ENTER_AMOUNT.toString());
                break;
            case ENTER_AMOUNT://输入金额之后的处理
                onEnterAmountResult(result);
                break;
            case CHECK_CARD://寻卡之后的处理
                onCheckCardResult(result);
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
                gotoState(State.PRINT_PREVIEW.toString());
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
