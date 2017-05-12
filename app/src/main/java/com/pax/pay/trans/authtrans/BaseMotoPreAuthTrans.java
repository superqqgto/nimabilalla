package com.pax.pay.trans.authtrans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.BaseTrans;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionOfflineSend;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.BaseTransData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.MotoTabBatchTransData;
import com.pax.pay.trans.model.TabBatchTransData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.TimeConverter;
import com.pax.pay.utils.ToastUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by zhouhong on 2017/5/6.
 */

public abstract class BaseMotoPreAuthTrans extends BaseAuthTrans {

    //    protected int transNameResId;
    protected MotoTabBatchTransData origTransData;

    public BaseMotoPreAuthTrans(ETransType transType, TransEndListener transListener) {
        super(transType, transListener);
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

    //origTransData的数据类型的区别导致重写函数
    @Override
    protected void onEnterAmountResult(ActionResult result) {
        //set total amount
        transData.setAmount(result.getData().toString());
        //set tip amount
        transData.setTipAmount(result.getData1().toString());

        long amount = Long.parseLong(transData.getAmount());
        long origAmount = Long.parseLong(origTransData.getAmount());
        //如果超出比例
        if (amount > (1 + variance) * origAmount) {
            ToastUtils.showShort("超出可浮动范围了");
            //重新输入金额
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            //enter pin
            gotoState(State.CHECK_CARD.toString());
        }
    }

    protected void bindEnterCVV2() {
        ActionInputTransData.Builder cvv2Builder = new ActionInputTransData.Builder()
                .transName(transNameResId)
                .lineNum(1)
                .prompt1(R.string.prompt_input_cvv2)
                .inputType1(ActionInputTransData.EInputType.NUM)
                .minLen1(3)
                .maxLen1(4)
                .isVoidLastTrans(false);

        bind(State.ENTER_CVV2.toString(), cvv2Builder.create());
    }

    protected void onEnterAuthCodeResult(ActionResult result) {
        //get auth code
        String authCode = (String) result.getData();
        //linzhao
        if (authCode == null) {
            transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
            return;
        }
        transData.setOrigAuthCode(authCode);


        //用auth code到Tab Batch中查询之前PreAuth的Transaction
        origTransData = DbManager.getMotoTabBatchTransDao().findTransDataByAuthCode(authCode);
        if (origTransData == null) {
            // trans not exist
            ToastUtils.showShort("记录不存在");
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        ETransType trType = origTransData.getTransType();
        // only preAuth trans can be revoked

        if (!ETransType.MOTO_PREAUTH.equals(trType)) {
            transEnd(new ActionResult(TransResult.ERR_PREAUTH_COMP_UNSUPPORTED, null));
            return;
        }

        //set auth code
        transData.setOrigAuthCode(authCode);

        copyOrigTransData();
        //linzhao
        gotoState(State.TRANS_DETAIL.toString());

    }

    protected void onCheckCardResult(ActionResult result) {
        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
        saveCardInfo(cardInfo, transData);
    }

    protected void onEnterCVV2Result(ActionResult result) {
        String cvv2 = (String) result.getData();
        transData.setCVV2(cvv2);
    }

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
        gotoState(State.SIGNATURE.toString());
    }

    protected void onSignatureResult(ActionResult result) {
        // save signature data
        byte[] signData = (byte[]) result.getData();

        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // update trans data，save signature
            DbManager.getTransDao().updateTransData(transData);
        }

        //get offline trans data list， piggyback
        List<TransData.OfflineStatus> filter = new ArrayList<>();
        filter.add(TransData.OfflineStatus.OFFLINE_NOT_SENT);
        List<TransData> offlineTransList = DbManager.getTransDao().findOfflineTransData(filter);
        if (offlineTransList != null) {
            if (offlineTransList.size() != 0 && offlineTransList.get(0).getId() != transData.getId()) { //AET-92
                //offline send
                gotoState(State.OFFLINE_SEND.toString());
            }
        }
        else {
            gotoState(State.PRINT_PREVIEW.toString());
        }
    }

    protected void deleteTransFromMotoTabBatch() {
        DbManager.getMotoTabBatchTransDao().deleteTransDataByTraceNo(origTransData.getTraceNo());
    }

    // set original trans data
    //origTransData的数据类型的区别导致重写函数
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

}
