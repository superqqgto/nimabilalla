package com.pax.pay.trans.authtrans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.authtrans.BaseAuthTrans;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.TimeConverter;
import com.pax.pay.utils.ToastUtils;

import java.util.LinkedHashMap;

/**
 * Created by huangmuhua on 2017/4/13.
 */

public class PreAuthCompCancelTrans extends BaseAuthTrans {
    protected TransData origTransData;

    public PreAuthCompCancelTrans() {
        super(ETransType.PREAUTH_COMP_CANCEL, R.string.trans_preauth_comp_cancel, null);
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
        gotoState(State.ENTER_AUTH_CODE.toString());
    }

    //origTransData的数据类型的区别导致重写函数
    @Override
    protected void bindTransDetail() {

        ActionDispTransDetail.Builder detailBuilder = new ActionDispTransDetail.Builder()
                .transName(transNameResId)
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        String transType = origTransData.getTransType().getTransName();
                        String amount = CurrencyConverter.convert(Long.parseLong(origTransData.getAmount()), transData.getCurrency());

                        transData.setEnterMode(origTransData.getEnterMode());
                        transData.setTrack2(origTransData.getTrack2());
                        transData.setTrack3(origTransData.getTrack3());
                        transData.setOrigDateTime(origTransData.getDateTime());

                        // date and time
                        String formattedDate = TimeConverter.convert(transData.getOrigDateTime(), Constants.TIME_PATTERN_TRANS,
                                Constants.TIME_PATTERN_DISPLAY);

                        LinkedHashMap<String, String> map = new LinkedHashMap<>();
                        map.put(ContextUtils.getString(R.string.history_detail_type), transType);
                        map.put(ContextUtils.getString(R.string.history_detail_amount), amount);
                        String cardNo = PanUtils.maskCardNo(origTransData.getPan(), origTransData.getIssuer().getPanMaskPattern());
                        map.put(ContextUtils.getString(R.string.history_detail_card_no), cardNo);
                        map.put(ContextUtils.getString(R.string.history_detail_auth_code), origTransData.getAuthCode());
                        map.put(ContextUtils.getString(R.string.history_detail_ref_no), origTransData.getRefNo());
                        String traceNo = Component.getPaddedNumber(origTransData.getTraceNo(), 6);
                        map.put(ContextUtils.getString(R.string.history_detail_trace_no), traceNo);
                        map.put(ContextUtils.getString(R.string.dateTime), formattedDate);

                        ((ActionDispTransDetail) action).setMap(map);
                    }
                });

        bind(State.TRANS_DETAIL.toString(), detailBuilder.create());
    }

    @Override
    protected void onEnterAuthCodeResult(ActionResult result) {
        //get auth code
        String authCode = (String) result.getData();
        transData.setOrigAuthCode(authCode);
        if (authCode == null) {
            if (result.getRet() == TransResult.ERR_USER_CANCEL) {
                return;
            } else {
                transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                return;
            }
        }

        //用auth 到terminal Batch中查询之前PreAuth comp的Transaction
        origTransData = DbManager.getTransDao().findTransDataByAuthCode(authCode);
        if (origTransData == null) {
            // trans not exist
            ToastUtils.showShort("记录不存在");
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        ETransType trType = origTransData.getTransType();
        // only preAuth trans can be revoked
        if (!ETransType.PREAUTH_COMP.equals(trType)) {
            transEnd(new ActionResult(TransResult.ERR_PREAUTH_COMP_UNSUPPORTED, null));
            return;
        }

        //set auth code
        transData.setOrigAuthCode(authCode);

        copyOrigTransData();
    }

    // set original trans data
    @Override
    protected void copyOrigTransData() {
        transData.setAmount(origTransData.getAmount());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigRspCode(origTransData.getResponseCode());
        transData.setOrigRefNo(origTransData.getRefNo());
        transData.setOrigTransNo(origTransData.getTraceNo());
        transData.setPan(origTransData.getPan());
        transData.setExpDate(origTransData.getExpDate());
        transData.setAcquirer(origTransData.getAcquirer());
        transData.setIssuer(origTransData.getIssuer());
    }

    @Override
    protected void toSignOrPrint() {
        super.toSignOrPrint();
        DbManager.getTransDao().updateTransData(transData);
        deleteTransFromBatch(); //从terminal batch里面删除被void的moto preAuth comp
    }

    protected void deleteTransFromBatch() {
        DbManager.getTransDao().deleteTransDataByTraceNo(origTransData.getTraceNo());
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
