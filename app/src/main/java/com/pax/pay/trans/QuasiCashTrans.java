package com.pax.pay.trans;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.utils.ContextUtils;

/**
 * Created by zhouhong on 2017/5/12.
 */

public class QuasiCashTrans extends SaleTrans {

    /**
     * @param amount    :total amount
     * @param isFreePin
     * @param mode      {@link com.pax.pay.trans.action.ActionSearchCard.SearchMode}, 如果等于-1，
     */
    public QuasiCashTrans(String amount, byte mode, boolean isFreePin,
                     TransEndListener transListener) {
        super(amount, mode, isFreePin, transListener);
    }

    public QuasiCashTrans() {
        super(ETransType.QUASI_CASH, null, "0", (byte) -1, true, true, null);
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        super.onActionResult(currentState,  result);
    }
}
