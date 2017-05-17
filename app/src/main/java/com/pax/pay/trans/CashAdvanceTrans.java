package com.pax.pay.trans;

import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.model.ETransType;

/**
 * Created by zhouhong on 2017/5/12.
 */
public class CashAdvanceTrans extends SaleTrans {
    /**
     * @param amount    :total amount
     * @param isFreePin
     * @param mode      {@link com.pax.pay.trans.action.ActionSearchCard.SearchMode}, 如果等于-1，
     */
    public CashAdvanceTrans(String amount, byte mode, boolean isFreePin,
                          TransEndListener transListener) {
        super(amount, mode, isFreePin, transListener);
    }

    public CashAdvanceTrans() {
        super(ETransType.CASH_ADVANCE, null, "0", (byte) -1, true, true, null);
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        super.onActionResult(currentState,  result);
    }
}
