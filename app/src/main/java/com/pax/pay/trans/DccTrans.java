package com.pax.pay.trans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.trans.action.ActionSelectDccAmount;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.base.DccTransData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.LogUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;

//import static com.pax.manager.sp.SysParam.DCC_PARTNER;

/**
 * Created by chenzaoyang on 2017/3/24.
 */

public class DccTrans extends BaseTrans {
//    private boolean isNeedInputAmount = true; // is need input amount
//    private boolean isFreePin = true;
//    boolean isSupportBypass = true;
//    private static String AmountStr;
    private String transName;
    private String Amount_HK;
    private int length_HK;
    enum State {
        ENTER_AMOUNT,
        CHECK_CARD,
        ONLINE_DCC,
        ENQUIRE
    }
    public DccTrans() {
        super(ETransType.DCC, null);
        transName = ContextUtils.getString(R.string.trans_dcc);

    }
//    public DccTrans(TransEndListener transListener) {
//        super(ETransType.DCC, transListener);
//        transName = ContextUtils.getString(R.string.trans_dcc);
//
//    }

    @Override
    protected void bindStateOnAction() {
        // input amount
        ActionInputTransData amountAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_dcc);
                ((ActionInputTransData) action).setParam(title).setInputLine1(
                        ContextUtils.getString(R.string.prompt_input_amount), ActionInputTransData.EInputType.AMOUNT, 9, false);
            }
        }, 1);
        bind(DccTrans.State.ENTER_AMOUNT.toString(), amountAction);
        // read card
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(ContextUtils.getString(R.string.trans_dcc),
                        ETransType.DCC.getReadMode(), transData.getAmount(), transData.getTipAmount(), null, null,
                        ActionSearchCard.ESearchCardUIType.DEFAULT, "");
            }
        });
        bind(DccTrans.State.CHECK_CARD.toString(), searchCardAction);

        // online action
        ActionTransOnline transOnlineAction = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setTransData(transData);
            }
        });

        bind(DccTrans.State.ONLINE_DCC.toString(), transOnlineAction);

         // ENQUIRE
        ActionSelectDccAmount EnquireAction = new ActionSelectDccAmount(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                DccTransData dccTransData = transData.getDccTransData();
                String amount = transData.getAmount();
                int i =0 ;
                String MutiAmount;
                String amount2=new StringBuffer(amount).reverse().toString();
                Amount_HK = amount2.substring(0,length_HK-2) + "." + amount2.substring(length_HK-2,length_HK) +" HKD";
                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                map.put(ContextUtils.getString(R.string.DCC_TransType), ContextUtils.getString(R.string.trans_dcc));
                map.put(ContextUtils.getString(R.string.DCC_TotalAmount), Amount_HK);
                Iterator iter1 = dccTransData.getTransAmtList().iterator();
                Iterator iter2 = dccTransData.getCurrencyList().iterator();
                Iterator iter3 = dccTransData.getConvRateList().iterator();
                while(iter1.hasNext()){
                     MutiAmount =  iter1.next() + " " + iter2.next() + " " + "Rate:" + iter3.next();
                     map.put(Integer.toString(i),MutiAmount);
                     i++;
                }
                LogUtils.i("Zac", Integer.toString(i));
                ((ActionSelectDccAmount) action).setParam(transName, map);

            }
        });
        bind(DccTrans.State.ENQUIRE.toString(), EnquireAction);

        // execute the first action
        gotoState(DccTrans.State.ENTER_AMOUNT.toString());
    }
    @Override
    public void onActionResult(String currentState, ActionResult result) {
        DccTrans.State state = DccTrans.State.valueOf(currentState);
        if (state != State.ENQUIRE) {
            // check action result，if failed，end the trans.
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }
        switch (state) {
            case ENTER_AMOUNT:// 输入交易金额后续处理
                // save amount
                String amount = ((String) result.getData()).replace(".", "");
                transData.setAmount(amount);
                length_HK = amount.length();
                LogUtils.i("Zac", Integer.toString(amount.length()));
                gotoState(State.CHECK_CARD.toString());
                break;
            case CHECK_CARD: // 检测卡的后续处理
                ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);
                gotoState(DccTrans.State.ONLINE_DCC.toString());
//                // 手输卡号处理
//                byte mode = cardInfo.getSearchMode();
//                if (mode == ActionSearchCard.SearchMode.KEYIN || mode == ActionSearchCard.SearchMode.SWIPE) {
//                    // input password
//                    gotoState(DccTrans.State.ENTER_PIN.toString());
//                } else if (mode == ActionSearchCard.SearchMode.INSERT || mode == ActionSearchCard.SearchMode.WAVE) {
//                    // EMV处理
//                    gotoState(DccTrans.State.EMV_PROC.toString());
//                }
                break;

            case ONLINE_DCC: // after online
                //  AmountStr = String.valueOf(CurrencyConverter.parse(result.getData().toString()));
                //  transData.setAmount(AmountStr);
                gotoState(State.ENQUIRE.toString());
                break;
            case ENQUIRE:
                DccTransData dccTransData = transData.getDccTransData();
                String Dccbackdata =(String) result.getData();
                LogUtils.i("Zac", Dccbackdata +"output");
                String Amount;
                Iterator iter1 = dccTransData.getTransAmtList().iterator();
                Iterator iter2 = dccTransData.getCurrencyList().iterator();
                Iterator iter3 = dccTransData.getConvRateList().iterator();
                String DccConvRate;
                String DccCurrency;
                String DccTransAmt;
                if(Dccbackdata.equals(Amount_HK)){
                    dccTransData.setDccOptIn(false);
                } else{
                    dccTransData.setDccOptIn(true);
                }

                while(iter2.hasNext()) {
                    DccTransAmt = (String) iter1.next();
                    DccCurrency = (String) iter2.next();
                    DccConvRate = (String) iter3.next();
                    Amount =  DccTransAmt + " " + DccCurrency + " " + "Rate:" + DccConvRate;
                    LogUtils.i("Zac", Amount);
                    if (Amount.equals(Dccbackdata)) {
                        dccTransData.setDccConvRate(DccConvRate);
                        dccTransData.setDccCurrency(DccCurrency);
                        dccTransData.setDccTransAmt(DccTransAmt);
                        LogUtils.i("Zac", Amount);
                        break;
                    }
                }
                // transaction end
                transEnd(result);
                break;
            default:
                transEnd(result);
                break;
        }

    }
}
