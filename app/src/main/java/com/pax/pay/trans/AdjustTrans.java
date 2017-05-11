/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-27
 * Module Author: caowb
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans;

import android.annotation.SuppressLint;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.edc.R;
import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;

import java.util.LinkedHashMap;

/*
 * according to the EDC Prolin, Tip of Sale,
 * Offline sale can be adjusted times before settlement.
 */

public class AdjustTrans extends BaseTrans {
    private String origTransNo;

    // the adjustment is just a state for a transaction,
    // so it cannot use the logic of base transaction which uses transData from the BaseTrans,
    // and in the db, each record has its id,
    // which means we cannot call transData.save() cuz it will create an excess record,
    // and we cannot call the transData.updateTrans() either, cuz the record with new id is not existed.
    // So we have to use the origTransData instead of transData.
    private TransData origTransData;
    /**
     * is need read transaction record
     */
    private boolean isNeedFindOrigTrans = true;
    /**
     * is need input transaction NO
     */
    private boolean isNeedInputTransNo = true;

    public AdjustTrans(TransEndListener transListener) {
        super(ETransType.ADJUST, transListener);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }

    public AdjustTrans(TransData origTransData, TransEndListener transListener) {
        super(ETransType.ADJUST, transListener); // ignore the type, cuz we are using the origTransData
        this.origTransData = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;
    }

    public AdjustTrans(String origTransNo, TransEndListener transListener) {
        super(ETransType.ADJUST, transListener);
        this.origTransNo = origTransNo;
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = false;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void bindStateOnAction() {
        //input manager password
        ActionInputPassword inputPasswordAction = new ActionInputPassword(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(6,
                        ContextUtils.getString(R.string.prompt_adjust_pwd), null);
            }
        });
        bind(AdjustTrans.State.INPUT_PWD.toString(), inputPasswordAction);

        //input original trance no
        ActionInputTransData enterTransNoAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(ContextUtils.getString(R.string.trans_adjust))
                        .setInputLine1(ContextUtils.getString(R.string.prompt_input_transno),
                                ActionInputTransData.EInputType.NUM, 6, false);
            }
        }, 1);
        bind(AdjustTrans.State.ENTER_TRANSNO.toString(), enterTransNoAction);

        // input new tips
        ActionInputTransData newTipsAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String title = ContextUtils.getString(R.string.trans_adjust);
                String transAmount = origTransData.getAmount();
                String transTips = origTransData.getTipAmount();
                float adjustPercent = origTransData.getIssuer().getAdjustPercent();

                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                map.put(ContextUtils.getString(R.string.prompt_total_amount), transAmount);
                map.put(ContextUtils.getString(R.string.prompt_ori_tips), transTips);
                map.put(ContextUtils.getString(R.string.prompt_adjust_percent), Float.toString(adjustPercent));

                ((ActionInputTransData) action).setParam(title, map).setInputLine1(
                        ContextUtils.getString(R.string.prompt_new_tips), ActionInputTransData.EInputType.AMOUNT, Constants.AMOUNT_DIGIT, false);
            }
        }, 4);
        bind(AdjustTrans.State.ENTER_AMOUNT.toString(), newTipsAction);

        // if need pwd for adjust
        if (SpManager.getSysParamSp().get(SysParamSp.OTHTC_VERIFY).equals(SysParamSp.Constant.YES)) {
            gotoState(AdjustTrans.State.INPUT_PWD.toString());
        } else if (isNeedInputTransNo) {// need input trans NO
            gotoState(AdjustTrans.State.ENTER_TRANSNO.toString());
        } else {// not need input trans NO
            if (isNeedFindOrigTrans) {
                validateOrigTransData(Long.parseLong(origTransNo));
            } else {
                updateAcqIssuer();
            }
        }

    }

    enum State {
        INPUT_PWD,
        ENTER_TRANSNO,
        ENTER_AMOUNT
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        // check action result，if fail,transaction end
        int ret = result.getRet();
        if (ret != TransResult.SUCC) {
            transEnd(result);
            return;
        }
        switch (state) {
            case INPUT_PWD:
                String data = EncUtils.SHA1((String) result.getData());
                if (!data.equals(SpManager.getSysParamSp().get(SysParamSp.SEC_ADJUSTPWD))) {
                    transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
                    return;
                }
                if (isNeedInputTransNo) {// need input trans NO
                    gotoState(AdjustTrans.State.ENTER_TRANSNO.toString());
                } else {// not need input trans NO
                    if (isNeedFindOrigTrans) {
                        validateOrigTransData(Long.parseLong(origTransNo));
                    } else {
                        updateAcqIssuer();
                        gotoState(State.ENTER_AMOUNT.toString());
                    }
                }
                break;
            case ENTER_TRANSNO:
                String content = (String) result.getData();
                long transNo;
                if (content == null) {
                    TransData tempTransData = DbManager.getTransDao().findLastTransData();
                    if (tempTransData == null) {
                        transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                        return;
                    }
                    transNo = tempTransData.getTraceNo();
                } else {
                    transNo = Long.parseLong(content);
                }
                validateOrigTransData(transNo);
                break;
            case ENTER_AMOUNT:
                long newTotalAmount = (long) result.getData();
                long newTipAmount = (long) result.getData1();

                //base amount and tip
                origTransData.setAmount(newTotalAmount + "");
                //set tip
                origTransData.setTipAmount(newTipAmount + "");
                // update original transaction record
                //set status as adjusted
                origTransData.setTransState(TransData.ETransStatus.ADJUSTED);
                origTransData.setOfflineSendState(TransData.OfflineStatus.OFFLINE_NOT_SENT);
                DbManager.getTransDao().updateTransData(origTransData);
                transEnd(result);
            default:
                break;
        }
    }

    // check original transaction information
    private void validateOrigTransData(long origTransNo) {
        origTransData = DbManager.getTransDao().findTransDataByTraceNo(origTransNo);
        if (origTransData == null) {
            // no original transaction
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }

        // unsale or offline sale can't adjust
        ETransType trType = origTransData.getTransType();
        if (!trType.isAdjustAllowed()) {
            transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORTED, null));
            return;
        }

        // tip not open
        if (SpManager.getSysParamSp().get(SysParamSp.EDC_SUPPORT_TIP).equals(SysParamSp.Constant.NO)) {
            transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORTED, null));
            return;
        }

        //  has voided/adjust transaction can not adjust
        TransData.ETransStatus trStatus = origTransData.getTransState();
        if (trStatus.equals(TransData.ETransStatus.VOIDED)) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOIDED, null));
            return;
        }

        gotoState(State.ENTER_AMOUNT.toString());
    }

    // set original trans data
    private void updateAcqIssuer() {
        transData.setIssuer(origTransData.getIssuer());
    }

}
