package com.pax.pay.trans.authtrans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.dal.exceptions.PiccDevException;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.enums.ETransResult;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.DalManager;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.BaseTrans;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterAmount;
import com.pax.pay.trans.action.ActionEnterAuthCode;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionOfflineSend;
import com.pax.pay.trans.action.ActionPrintPreview;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSendEmail;
import com.pax.pay.trans.action.ActionSendSMS;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.action.activity.PrintPreviewActivity;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TabBatchTransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.TimeConverter;
import com.pax.pay.utils.ToastUtils;

import java.util.LinkedHashMap;

/**
 * Created by huangmuhua on 2017/4/13.
 */

public abstract class BaseAuthTrans extends BaseTrans {

    protected enum State {
        //交易状态枚举
        ENTER_AUTH_CODE,//通过授权码查询一笔preAuth的交易
        TRANS_DETAIL,//显示查出来的那笔preAuth交易详情
        ENTER_AMOUNT,//输入金额
        CHECK_CARD,//刷卡，寻卡界面，显示刷卡、插卡、挥卡、手输卡号（如果支持）
        ENTER_CVV2,//输入CVV2
        ENTER_PIN,//输入卡密码
        EMV_PROC,
        ONLINE,//联机处理
        SIGNATURE,//签名
        OFFLINE_SEND, //上传offline交易，piggyback
        PRINT_PREVIEW,//打印预览
        PRINT_RECEIPT,//打印收据
        //AET-118
        ENTER_PHONE_NUM,
        ENTER_EMAIL,
        SEND_SMS,
        SEND_EMAIL
    }

    protected int transNameResId;
    protected TabBatchTransData origTransData;
    protected State state;
    protected boolean isFreePin = false;
    protected boolean isSupportBypass = true;
    protected float variance = 0.15f;//可超过的百分比 0<variance<15%

    public BaseAuthTrans(ETransType transType, int transNameResId, TransEndListener transListener) {
        super(transType, transListener);
        this.transNameResId = transNameResId;
    }


    protected void bindEnterAuthCode() {
        //enter auth code action
        ActionEnterAuthCode.Builder authCodeBuilder = new ActionEnterAuthCode.Builder()
                .transName(transNameResId)
                .prompt(R.string.prompt_auth_code)
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
//                        ((ActionEnterAuthCode) action).setTransAmount(transData.getAmount());
                    }
                });

        bind(State.ENTER_AUTH_CODE.toString(), authCodeBuilder.create());
    }

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

    protected void bindEnterAmount() {
        // input amount and tip amount action
        ActionEnterAmount.Builder enterBuilder = new ActionEnterAmount.Builder()
                .transName(transNameResId)//定义时就能确定的参数
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {//执行到这里时才能确定的参数
                        float percent = transData.getIssuer().getAdjustPercent();
                        ((ActionEnterAmount) action).setAdjustPercent(percent);
                    }
                });

        //一旦create，transName等定义时确定的参数不能再修改
        bind(State.ENTER_AMOUNT.toString(), enterBuilder.create());
    }

    protected void bindCheckCard() {
        // search card action
        ActionSearchCard.Builder searchBuilder = new ActionSearchCard.Builder()
                .transName(transNameResId)
                .cardReadMode(transData.getTransType().getReadMode())
//                .searchCardPrompt(R.string.prompt_swipe_card)
//                .searchCardUIType(ActionSearchCard.ESearchCardUIType.DEFAULT)
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ActionSearchCard searchAct = (ActionSearchCard) action;
                        searchAct.setAmount(transData.getAmount());
                    }
                });

        bind(State.CHECK_CARD.toString(), searchBuilder.create());
    }

    protected void bindEnterPin() {
        // if quick pass by pin, set isSupportBypass as false,input password
        if (!isFreePin) {
            isSupportBypass = false;
        }

        // enter card  pin
        ActionEnterPin.Builder pinBuilder = new ActionEnterPin.Builder()
                .transName(transNameResId)
                .isSupportBypass(isSupportBypass)
                .prompt1(R.string.prompt_pin)
                .prompt2(R.string.prompt_no_pin)
                .enterPinType(ActionEnterPin.EEnterPinType.ONLINE_PIN)
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ActionEnterPin actionEnterPin = (ActionEnterPin) action;
                        actionEnterPin.setPan(transData.getPan());
                        actionEnterPin.setTotalAmount(transData.getAmount());
                        actionEnterPin.setTipAmount(transData.getTipAmount());
                    }
                });

        bind(State.ENTER_PIN.toString(), pinBuilder.create());
    }

    protected void bindEmvProcess() {

        // emv action
        ActionEmvProcess.Builder emvBuilder = new ActionEmvProcess.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionEmvProcess) action).setTransData(transData);
                    }
                });

        bind(State.EMV_PROC.toString(), emvBuilder.create());
    }

    protected void bindOnline() {

        // online action
        ActionTransOnline.Builder onlineBuilder = new ActionTransOnline.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionTransOnline) action).setTransData(transData);
                    }
                });

        bind(State.ONLINE.toString(), onlineBuilder.create());
    }

    protected void bindSignature() {

        // signature action
        ActionSignature.Builder signBuilder = new ActionSignature.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ActionSignature signAction = (ActionSignature) action;
                        signAction.setAmount(transData.getAmount());
                        signAction.setFeatureCode(Component.genFeatureCode(transData));
                    }
                });

        bind(State.SIGNATURE.toString(), signBuilder.create());
    }

    protected void bindOfflienSend() {
        ActionOfflineSend.Builder offlineSendBuilder = new ActionOfflineSend.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionOfflineSend) action).setTransData(transData);
                    }
                });

        bind(MotoPreAuthTrans.State.OFFLINE_SEND.toString(), offlineSendBuilder.create());
    }

    protected void bindPrintPreview() {

        //print preview action
        ActionPrintPreview.Builder previewBuilder = new ActionPrintPreview.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintPreview) action).setTransData(transData);
                    }
                });

        bind(State.PRINT_PREVIEW.toString(), previewBuilder.create());
    }

    protected void bindPrintReceipt() {
        // print action
        ActionPrintTransReceipt.Builder printBuilder = new ActionPrintTransReceipt.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintTransReceipt) action).setTransData(transData);
                    }
                });

        bind(State.PRINT_RECEIPT.toString(), printBuilder.create());
    }

    protected void bindPhoneNum() {
        //AET-118
        // get Telephone num
        ActionInputTransData.Builder phoneBuilder = new ActionInputTransData.Builder()
                .transName(R.string.paperless)
                .lineNum(1)
                .prompt1(R.string.prompt_phone_number)
                .inputType1(ActionInputTransData.EInputType.PHONE)
                .maxLen1(20)
                .isVoidLastTrans(false);

        bind(State.ENTER_PHONE_NUM.toString(), phoneBuilder.create());
    }

    protected void bindEnterEmail() {
        // get Telephone num
        ActionInputTransData.Builder emailBuilder = new ActionInputTransData.Builder()
                .transName(R.string.paperless)
                .lineNum(1)
                .prompt1(R.string.prompt_email_address)
                .inputType1(ActionInputTransData.EInputType.EMAIL)
                .maxLen1(100)
                .isVoidLastTrans(false);

        bind(State.ENTER_EMAIL.toString(), emailBuilder.create());
    }

    protected void bindSendSMS() {

        ActionSendSMS.Builder smsBuilder = new ActionSendSMS.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionSendSMS) action).setParam(transData);
                    }
                });
        bind(State.SEND_SMS.toString(), smsBuilder.create());
    }

    protected void bindSendEmail() {
        ActionSendEmail.Builder sendEmailBuilder = new ActionSendEmail.Builder()
                .startListener(new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionSendEmail) action).setParam(transData);
                    }
                });
        bind(State.SEND_EMAIL.toString(), sendEmailBuilder.create());
    }

    //    @Override
    public boolean isEndForward(String currentState, ActionResult result) {
        state = State.valueOf(currentState);
        if ((state != State.SIGNATURE) && (state != State.PRINT_PREVIEW)) {
            // check action result，if failed，end the trans.
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return true;
            }
        }
        return false;
    }

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

        //用auth code到Tab Batch中查询之前PreAuth的Transaction
        origTransData = DbManager.getTabBatchTransDao().findTransDataByAuthCode(authCode);
        if (origTransData == null) {
            // trans not exist
            ToastUtils.showShort("记录不存在");
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        ETransType trType = origTransData.getTransType();
        // only preAuth trans can be revoked
        if (!ETransType.PREAUTH.equals(trType)) {
            transEnd(new ActionResult(TransResult.ERR_PREAUTH_COMP_UNSUPPORTED, null));
            return;
        }

        //set auth code
        transData.setOrigAuthCode(authCode);

        copyOrigTransData();
    }

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

//    protected void onCheckCardResult(ActionResult result) {
//        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
//
//        //PreAuthorization Sale Completion只支持银联
////                if(!"CUP".equals(cardInfo.getIssuer().getName())){
////                    return;
////                }
//        saveCardInfo(cardInfo, transData);
//        gotoState(State.ENTER_PIN.toString());
//    }

    protected void onCheckCardResult(ActionResult result) {
        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
        saveCardInfo(cardInfo, transData);

        byte mode = cardInfo.getSearchMode();
        if (mode == ActionSearchCard.SearchMode.KEYIN || mode == ActionSearchCard.SearchMode.SWIPE) {
            // input password
            gotoState(State.ENTER_PIN.toString());
        } else if (mode == ActionSearchCard.SearchMode.INSERT) {
            // EMV处理
            gotoState(State.EMV_PROC.toString());
        }
    }

    public void onEmvProcessResult(ActionResult result) {
        // TODO 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
        ETransResult transResult = (ETransResult) result.getData();
        // EMV完整流程 脱机批准或联机批准都进入签名流程
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {// 联机批准/脱机批准处理
            // judge whether need signature or print
            toSignOrPrint();

        } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程

            if (!isFreePin) {
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
                return;
            }

            if (transResult == ETransResult.ARQC) {
                if (!Component.isQpbocNeedOnlinePin()) {
                    gotoState(State.ONLINE.toString());
                    return;
                }
            }
            if (Component.clssQPSProcess(transData)) { // pin free
                transData.setPinFree(true);
                gotoState(State.ONLINE.toString());
            } else {
                // input password
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
            }
        } else if (transResult == ETransResult.ONLINE_DENIED) { // online denied
            // transaction end
            transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
        } else if (transResult == ETransResult.ONLINE_CARD_DENIED) {// platform approve card denied
            transEnd(new ActionResult(TransResult.ERR_CARD_DENIED, null));
        } else if (transResult == ETransResult.ABORT_TERMINATED) { // emv terminated
            // transaction end
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        } else if (transResult == ETransResult.OFFLINE_DENIED) {
            // transaction end
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    protected void onEnterPinResult(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
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
    }

    protected void onSignatureResult(ActionResult result) {
        // save signature data
        byte[] signData = (byte[]) result.getData();

        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // update trans data，save signature
            DbManager.getTransDao().updateTransData(transData);
        }
        gotoState(State.PRINT_PREVIEW.toString());
    }

    protected void onPrintPreviewResult(ActionResult result) {
        String string = (String) result.getData();
        if (string != null && string.equals(PrintPreviewActivity.PRINT_BUTTON)) {
            //print ticket
            gotoState(State.PRINT_RECEIPT.toString());
        } else {
            //end trans directly, not print
            transEnd(result);
        }
    }

    // set original trans data
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


    // 判断是否需要电子签名或打印
    protected void toSignOrPrint() {
        if (Component.isSignatureFree(transData)) {// 免签
            transData.setSignFree(true);
            // 打印
            gotoState(State.PRINT_RECEIPT.toString());
        } else {
            // 电子签名
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }
//        DbManager.getTransDao().updateTransData(transData);
//        saveTransToTabBatch();
    }

    protected void deleteTransFromTabBatch() {
        DbManager.getTabBatchTransDao().deleteTabBatchTransDataByTraceNo(origTransData.getTraceNo());
    }
}
