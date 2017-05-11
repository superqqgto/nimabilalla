/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans;

import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.edc.R;
import com.pax.manager.sp.SpManager;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.pay.trans.action.ActionSettle;
import com.pax.pay.trans.action.ActionSelectAcquirer;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.utils.ContextUtils;
import com.pax.manager.sp.SysParamSp;

import java.util.ArrayList;

public class SettleTrans extends BaseTrans {

    private static final String TAG = SettleTrans.class.getSimpleName() ;
    private ArrayList<String> selectAcqs;

    public SettleTrans(TransEndListener listener) {
        super(ETransType.SETTLE, listener);
    }

    @Override
    protected void bindStateOnAction() {

        ActionInputPassword inputPasswordAction = new ActionInputPassword(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(6,
                        ContextUtils.getString(R.string.prompt_settle_pwd), null);
            }
        });
        bind(State.INPUT_PWD.toString(), inputPasswordAction);

        ActionSelectAcquirer actionSelectAcquirer = new ActionSelectAcquirer(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSelectAcquirer) action).setParam(
                        ContextUtils.getString(R.string.settle_select_acquirer));
            }
        });
        bind(State.SELECT_ACQ.toString(), actionSelectAcquirer);

        ActionSettle settleAction = new ActionSettle(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSettle) action).setParam(ContextUtils.getString(R.string.trans_settle), selectAcqs);
//                ((ActionSettle) action).setParam("linzhao", selectAcqs);
            }

        });

        bind(State.SETTLE.toString(), settleAction);

        //结算是否需要输入密码
        if (SpManager.getSysParamSp().get(SysParamSp.OTHTC_VERIFY).equals(SysParamSp.Constant.YES)) {
            gotoState(State.INPUT_PWD.toString());
        } else {
            gotoState(State.SETTLE.toString());
        }
    }

    enum State {
        INPUT_PWD,
        SELECT_ACQ,
        SETTLE
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        if (state != State.SETTLE) {
            // check action result，if failed，end the trans.
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                if (selectAcqs != null) {
                    selectAcqs.clear();
                }
                transEnd(result);
                return;
            }
        }
        switch (state) {
            case INPUT_PWD:
                String data = EncUtils.SHA1((String) result.getData());
                if (!data.equals(SpManager.getSysParamSp().get(SysParamSp.SEC_SETTLEPWD))) {
                    if (selectAcqs != null) {
                        selectAcqs.clear();
                    }
                    transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
                    return;
                }
                gotoState(State.SELECT_ACQ.toString());
                break;
            case SELECT_ACQ:
                //noinspection unchecked
                selectAcqs = (ArrayList<String>) result.getData();
                Log.e(TAG, "onActionResult:  SELECT_ACQ " );
                gotoState(State.SETTLE.toString());
                break;
            case SETTLE:
                Log.e(TAG, "onActionResult: SETTLE" );
                if (result.getRet() == TransResult.ERR_USER_CANCEL) {
                    gotoState(State.SELECT_ACQ.toString());
                } else {
                    if (selectAcqs != null) {
                        selectAcqs.clear();
                    }
                    transEnd(result);
                }
                break;
        }
    }

}
