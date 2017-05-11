package com.pax.pay.trans.mototrans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.abl.utils.PanUtils;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.BaseTrans;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionOfflineSend;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.BaseTransData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.MotoTransData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.TimeConverter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by chenzaoyang on 2017/5/8.
 */

public class MotoSaleVoidTrans extends BaseTrans {
//    private TransData origTransData;
    private MotoTransData origTransData;
    private String origTransNo;
    /**
     * whether need to read the original trans data or not
     */
    private boolean isNeedFindOrigTrans = true;
    /**
     * whether need to input trans no. or not
     */
    private boolean isNeedInputTransNo = true;

    public MotoSaleVoidTrans() {
        super(ETransType.MOTOVOID, null);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }
    @Override
    protected void bindStateOnAction() {
        ActionInputPassword inputPasswordAction = new ActionInputPassword(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(6,
                        ContextUtils.getString(R.string.prompt_void_pwd), null);
            }
        });
        bind(MotoSaleVoidTrans.State.INPUT_PWD.toString(), inputPasswordAction);

        ActionInputTransData enterTransNoAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(ContextUtils.getString(R.string.trans_moto_void))
                        .setInputLine1(ContextUtils.getString(R.string.prompt_input_transno), ActionInputTransData.EInputType.NUM, 6, true);
            }
        }, 1);
        bind(MotoSaleVoidTrans.State.ENTER_TRANSNO.toString(), enterTransNoAction);
        // input cvv2
        ActionInputTransData cvv2Action = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_moto_void);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_input_cvv2), ActionInputTransData.EInputType.NUM, 4, 3, false);
            }
        }, 1);
        bind(MotoSaleVoidTrans.State.ENTER_CVV2.toString(), cvv2Action);
        // confirm information
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {

                String transType = origTransData.getTransType().getTransName();
                String amount;
                amount = CurrencyConverter.convert(Long.parseLong(origTransData.getAmount()), transData.getCurrency());

                transData.setEnterMode(origTransData.getEnterMode());
                transData.setTrack2(origTransData.getTrack2());
                transData.setTrack3(origTransData.getTrack3());

                // date and time
                String formattedDate = TimeConverter.convert(transData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                        Constants.TIME_PATTERN_DISPLAY);

                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                map.put(ContextUtils.getString(R.string.history_detail_type), transType);
                map.put(ContextUtils.getString(R.string.history_detail_amount), amount);
                map.put(ContextUtils.getString(R.string.history_detail_card_no), PanUtils.maskCardNo(origTransData.getPan(), origTransData.getIssuer().getPanMaskPattern()));
                map.put(ContextUtils.getString(R.string.history_detail_auth_code), origTransData.getAuthCode());
                map.put(ContextUtils.getString(R.string.history_detail_ref_no), origTransData.getRefNo());
                map.put(ContextUtils.getString(R.string.history_detail_trace_no), Component.getPaddedNumber(origTransData.getTraceNo(), 6));
                map.put(ContextUtils.getString(R.string.dateTime), formattedDate);
                ((ActionDispTransDetail) action).setParam(ContextUtils.getString(R.string.trans_moto_void), map);
            }
        });
        bind(MotoSaleVoidTrans.State.TRANS_DETAIL.toString(), confirmInfoAction);

        // online action
        ActionTransOnline transOnlineAction = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });

        bind(MotoSaleVoidTrans.State.ONLINE.toString(), transOnlineAction);
        // signature action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setAmount(transData.getAmount());
            }
        });
        bind(MotoSaleVoidTrans.State.SIGNATURE.toString(), signatureAction);
        //offline send
        ActionOfflineSend offlineSendAction = new ActionOfflineSend(null);
        bind(MotoSaleVoidTrans.State.OFFLINE_SEND.toString(), offlineSendAction);
        //print preview action
        ActionPrintPreview printPreviewAction = new ActionPrintPreview(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionPrintPreview) action).setTransData(transData);
            }
        });
        bind(MotoSaleVoidTrans.State.PRINT_PREVIEW.toString(), printPreviewAction);
        // print action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintTransReceipt) action).setTransData(transData);
                    }
                });
        bind(MotoSaleVoidTrans.State.PRINT_TICKET.toString(), printTransReceiptAction);

        // whether void trans need to input password or not
        if (SpManager.getSysParamSp().get(SysParamSp.OTHTC_VERIFY).equals(SysParamSp.Constant.YES)) {
            gotoState(MotoSaleVoidTrans.State.INPUT_PWD.toString());
        } else if (isNeedInputTransNo) {// need to input trans no.
            gotoState(MotoSaleVoidTrans.State.ENTER_TRANSNO.toString());
        } else {// not need to input trans no.
            if (isNeedFindOrigTrans) {
                validateOrigTransData(Long.parseLong(origTransNo));
            } else { // not need to read trans data
                copyOrigTransData();
                checkCardAndPin();
            }
        }

    }

    enum State {
        INPUT_PWD,
        ENTER_CVV2,
        ENTER_TRANSNO,
        TRANS_DETAIL,
        ONLINE,
        SIGNATURE,
        OFFLINE_SEND,
        PRINT_PREVIEW,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        MotoSaleVoidTrans.State state = MotoSaleVoidTrans.State.valueOf(currentState);
        if ((state != MotoSaleVoidTrans.State.SIGNATURE) && (state != MotoSaleVoidTrans.State.PRINT_PREVIEW)) {
            // check action result，if failed，end the trans.
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case INPUT_PWD:
                String data = EncUtils.SHA1((String) result.getData());
                if (!data.equals(SpManager.getSysParamSp().get(SysParamSp.SEC_VOIDPWD))) {
                    transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
                    return;
                }

                if (isNeedInputTransNo) {// need to input trans no.
                    gotoState(MotoSaleVoidTrans.State.ENTER_TRANSNO.toString());
                } else {// not need to input trans no.
                    if (isNeedFindOrigTrans) {
                        validateOrigTransData(Long.parseLong(origTransNo));
                    } else { // not need to read trans data
                        copyOrigTransData();
                        checkCardAndPin();
                    }
                }
                break;
///            case ENTER_TRANSNO:
//                String content = (String) result.getData();
//                long transNo = 0;
//                if (content == null) {
//                    TransData transData = DbManager.getTransDao().findLastTransData();
//                    if (transData == null) {
//                        transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
//                        return;
//                    }
//                    transNo = transData.getTraceNo();
//                } else {
//                    transNo = Long.parseLong(content);
//                }
//                validateOrigTransData(transNo);
//                break;
             case ENTER_TRANSNO:
                String content = (String) result.getData();
                long transNo = 0;
                if (content == null) {
                    MotoTransData motoTransData = DbManager.getMotoTransDao().findLastMotoTransData();
                    if (motoTransData == null) {
                        transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                        return;
                    }
                    transNo = motoTransData.getTraceNo();
                } else {
                    transNo = Long.parseLong(content);
                }
                validateOrigTransData(transNo);
                break;
            case TRANS_DETAIL:
                //checkCardAndPin();
                // online
                gotoState(MotoSaleVoidTrans.State.ONLINE.toString());
                break;
            case ONLINE: //  subsequent processing of online
                // update original trans data
                origTransData.setTransState(BaseTransData.ETransStatus.VOIDED);
                DbManager.getMotoTransDao().updateMotoTransData(origTransData);
                saveTransToMotoTabBatch();
                gotoState(MotoSaleVoidTrans.State.SIGNATURE.toString());

                break;
            case SIGNATURE:
                // save signature data
                byte[] signData = (byte[]) result.getData();
                if (signData != null && signData.length > 0) {
                    transData.setSignData(signData);
                    // update trans data，save signature
//                    DbManager.getTransDao().updateTransData(transData);
                    DbManager.getMotoTransDao().updateMotoTransData(new MotoTransData(transData));
                }

                //get offline trans data list
                List<ETransType> list = new ArrayList<>();
                list.add(ETransType.OFFLINE_TRANS_SEND);

                List<TransData.ETransStatus> filter = new ArrayList<>();
                filter.add(BaseTransData.ETransStatus.VOIDED);
                filter.add(BaseTransData.ETransStatus.ADJUSTED);

                List<TransData> offlineTransList = DbManager.getTransDao().findTransData(list, filter);
                if (offlineTransList != null)
                    if (offlineTransList.size() != 0) {
                        //offline send
                        gotoState(MotoSaleVoidTrans.State.OFFLINE_SEND.toString());
                        break;
                    }

                // if terminal does not support signature ,card holder does not sign or time out，print preview directly.
                gotoState(MotoSaleVoidTrans.State.PRINT_PREVIEW.toString());
                break;
            case OFFLINE_SEND:
                gotoState(MotoSaleVoidTrans.State.PRINT_PREVIEW.toString());
                break;
            case PRINT_PREVIEW:
                String string = (String) result.getData();
                if (string != null && string.equals(PrintPreviewActivity.PRINT_BUTTON)) {
                    //print ticket
                    gotoState(MotoSaleVoidTrans.State.PRINT_TICKET.toString());
                } else {
                    //end trans directly, not print
                    transEnd(result);
                }
                break;
            case PRINT_TICKET:
                // end trans
                transEnd(result);
                break;
            default:
                transEnd(result);
                break;
        }

    }

    private void saveTransToMotoTabBatch() {
    }


    // check original trans data
    private void validateOrigTransData(long origTransNo) {
        origTransData = DbManager.getMotoTransDao().findMotoTransDataByTraceNo(origTransNo);
        if (origTransData == null) {
            // trans not exist
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        ETransType trType = origTransData.getTransType();

        // only sale and refund trans can be revoked
        if (!(trType.equals(ETransType.MOTOSALE) || trType.equals(ETransType.MOTOREFUND))) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORTED, null));
            return;
        } else {
            // offline trans can not be revoked
            if (!origTransData.isOnlineTrans()) {
                transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORTED, null));
                return;
            }
        }

        TransData.ETransStatus trStatus = origTransData.getTransState();
        // void trans can not be revoked again
        if (trStatus.equals(BaseTransData.ETransStatus.VOIDED)) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOIDED, null));
            return;
        }
       copyOrigTransData();
       gotoState(State.TRANS_DETAIL.toString());
    }

    // set original trans data
    private void copyOrigTransData() {
        transData.setAmount(origTransData.getAmount());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigRefNo(origTransData.getRefNo());
        transData.setOrigTransNo(origTransData.getTraceNo());
        transData.setOrigDateTime(origTransData.getDateTime());     //Added by daisy.zhou
        transData.setPan(origTransData.getPan());
        transData.setExpDate(origTransData.getExpDate());
        transData.setAcquirer(origTransData.getAcquirer());
        transData.setIssuer(origTransData.getIssuer());
    }

    // check whether void trans need to swipe card or not
    private void checkCardAndPin() {
        // not need to swipe card
        transData.setEnterMode(BaseTransData.EnterMode.MANUAL);
        checkPin();
    }

    // check whether void trans need to enter pin or not
    private void checkPin() {
        // not need to enter pin
        transData.setPin("");
        transData.setHasPin(false);
        gotoState(MotoSaleVoidTrans.State.ONLINE.toString());
    }

}
