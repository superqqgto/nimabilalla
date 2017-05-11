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

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.utils.ContextUtils;

public class ReadCardTrans extends BaseTrans {

    public ReadCardTrans(TransEndListener transListener) {
        super(ETransType.READCARDNO, transListener);
    }

    @Override
    protected void bindStateOnAction() {
        // search card
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(ContextUtils.getString(R.string.trans_readCard),
                        ETransType.READCARDNO.getReadMode(), null, null, "");
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // EMV
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEmvProcess) action).setParam(transData);
            }
        });
        bind(State.EMV_PROC.toString(), emvProcessAction);

        gotoState(State.CHECK_CARD.toString());

    }

    enum State {
        CHECK_CARD,
        EMV_PROC,
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            // trans end
            transEnd(result);
            return;
        }

        State state = State.valueOf(currentState);
        switch (state) {
            case CHECK_CARD: // 检测卡的后续处理
                CardInformation cardInfo = (CardInformation) result.getData();
                saveCardInfo(cardInfo, transData);
                byte mode = cardInfo.getSearchMode();
                if (mode == SearchMode.SWIPE) {
                    transEnd(new ActionResult(TransResult.SUCC, cardInfo));
                } else if (mode == SearchMode.INSERT || mode == SearchMode.WAVE) {
                    // EMV处理
                    gotoState(State.EMV_PROC.toString());
                }
                break;
            case EMV_PROC: // emv后续处理
                ETransResult transResult = (ETransResult) result.getData();
                Component.emvTransResultProcess(transResult, transData);

                CardInformation sResult = new CardInformation();
                sResult.setPan(transData.getPan());
                transEnd(new ActionResult(TransResult.SUCC, sResult));
                break;
            default:
                transEnd(result);
                break;
        }

    }

}
