package com.pax.pay.trans.mototrans;

import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.BaseTrans;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.MotoTransData;

/**
 * Created by chenzaoyang on 2017/5/5.
 */

public class BaseMotoTrans extends BaseTrans {
    protected MotoTransData motoTransData;

    public BaseMotoTrans(ETransType transType, TransEndListener transListener) {
        super(transType, transListener);
    }
    @Override
    public void onActionResult(String currentState, ActionResult result) {

    }
    @Override
    protected void bindStateOnAction() {

    }
}
