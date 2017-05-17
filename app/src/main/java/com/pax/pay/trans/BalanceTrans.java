package com.pax.pay.trans;

import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.enums.ETransResult;
import com.pax.gl.convert.IConvert;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionDispSingleLineMsg;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.authtrans.BaseAuthTrans;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.transmit.Online;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.LogUtils;

import static com.pax.manager.sp.SysParamSp.EDC_SUPPORT_TIP;

/**
 * Created by xub on 2017/5/9.
 */

public class BalanceTrans extends BaseTrans {

    private boolean isFreePin;
    private boolean isSupportBypass = true;
    private int transNameResId = R.string.trans_balance;
    public BalanceTrans() {
        super(ETransType.BALANCE, null);

    }

    @Override
    protected void bindStateOnAction() {
        LogUtils.i("Frank", "Read card");
        // read card
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(ContextUtils.getString(R.string.trans_balance),
                        ETransType.BALANCE.getReadMode(), transData.getAmount(), transData.getTipAmount(), null, null,
                        ActionSearchCard.ESearchCardUIType.DEFAULT, "");
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);



        // enter pin action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {

                // if flash pay by pwd,set isSupportBypass=false,need to enter pin
                if (!isFreePin) {
                    isSupportBypass = false;
                }
                ((ActionEnterPin) action).setParam(ContextUtils.getString(R.string.trans_balance),
                        transData.getPan(), isSupportBypass, ContextUtils.getString(R.string.prompt_pin),
                        ContextUtils.getString(R.string.prompt_no_pin), transData.getAmount(), transData.getTipAmount(), ActionEnterPin.EEnterPinType.ONLINE_PIN);
            }
        });
        bind(BalanceTrans.State.ENTER_PIN.toString(), enterPinAction);
        // emv process action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEmvProcess) action).setParam(transData);
            }
        });
        bind(BalanceTrans.State.EMV_PROC.toString(), emvProcessAction);
        // online action
        ActionTransOnline transOnlineAction = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });
        bind(BalanceTrans.State.ONLINE.toString(), transOnlineAction);
        // 余额显示
        ActionDispSingleLineMsg balanceDispAction = new ActionDispSingleLineMsg(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String content = GlManager.getConvert().amountMinUnitToMajor(transData.getAmount(),
                        IConvert.ECurrencyExponent.CURRENCY_EXPONENT_2, true);
                if (content == null || content.length() == 0) {
                    content = "0";
                }
                // String amount = ContextUtils.getString(R.string.trans_amount_default);
                String amount = transData.getBalanceAmount();
                //amount=amount.substring(0, amount.length() - 2) + "." + amount.substring(amount.length() - 2, amount.length()) + " HKD";
                ((ActionDispSingleLineMsg) action).setParam(ContextUtils.getString(R.string.trans_balance), ContextUtils.getString(R.string.trans_balance), amount,
                        5);
            }
        });
        bind(State.BALANCE_DISP.toString(), balanceDispAction);

        gotoState(State.CHECK_CARD.toString());
    }

    enum State {
        CHECK_CARD,
        ENTER_PIN,
        EMV_PROC,
        ONLINE,
        BALANCE_DISP
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        if (!currentState.equals(State.BALANCE_DISP.toString())) {
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        State state = State.valueOf(currentState);
        switch (state) {
            case CHECK_CARD: // 检测卡的后续处理
                LogUtils.i("Frank", "Read card");
                ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);
                // 手输卡号处理
                byte mode = cardInfo.getSearchMode();
                if (mode == ActionSearchCard.SearchMode.KEYIN) {
                    // 跑到这里， 请检查程序逻辑， 有问题
                    gotoState(State.ENTER_PIN.toString());
                } else if (mode == ActionSearchCard.SearchMode.SWIPE) {
                    // 输密码
                    Log.d("Frank", "Go to Enter Pin");
                    gotoState(State.ENTER_PIN.toString());
                } else if (mode == ActionSearchCard.SearchMode.INSERT || mode == ActionSearchCard.SearchMode.WAVE) {
                    // EMV处理
                    Log.d("Frank", "EMV Process");
//                    SpManager.getSysParamSp().putBoolean(EDC_SUPPORT_TIP, false);//使插卡查询余额不走Enter tip
                    gotoState(State.EMV_PROC.toString());
                }
                break;
            case ENTER_PIN: // 输入密码的后续处理
                Log.d("Frank", "Enter Pin");
                String pinBlock = (String) result.getData();
                transData.setPin(pinBlock);
                if (pinBlock != null && pinBlock.length() > 0) {
                    transData.setHasPin(true);
                }
                // 联机处理
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE: // 联机的后续处理
                Log.d("Frank", "Online");
                gotoState(State.BALANCE_DISP.toString());
                break;
            case EMV_PROC: // emv后续处理
                Log.w("Frank", "EMV");
//                SpManager.getSysParamSp().putBoolean(EDC_SUPPORT_TIP, true);
                ETransResult transResult = (ETransResult) result.getData();
                Component.emvTransResultProcess(transResult, transData);
                if (transResult == ETransResult.ONLINE_APPROVED) {// 联机批准/脱机批准处理
                    gotoState(State.BALANCE_DISP.toString());
                } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程

                    if (transResult == ETransResult.ARQC) {
                        if (!Component.isQpbocNeedOnlinePin()) {
                            gotoState(State.ONLINE.toString());
                            return;
                        }
                    }
                    // 输密码
                    gotoState(State.ENTER_PIN.toString());
                } else if (transResult == ETransResult.ONLINE_DENIED) { // 联机拒绝
                    // 交易结束
                    transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
                } else if (transResult == ETransResult.ONLINE_CARD_DENIED) {// 平台批准卡片拒绝
                    transEnd(new ActionResult(TransResult.ERR_CARD_DENIED, null));
                } else if (transResult == ETransResult.ABORT_TERMINATED) { // emv中断
                    // 交易结束
                    transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                } else if (transResult == ETransResult.OFFLINE_APPROVED) {
                    transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                } else if (transResult == ETransResult.OFFLINE_DENIED) {
                    Device.beepErr();
                    transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                }
                break;
            case BALANCE_DISP:
                Log.e("Frank", "Display");
                transEnd(new ActionResult(TransResult.SUCC, null));
                break;
            default:
                transEnd(result);
                break;
        }
    }
}
