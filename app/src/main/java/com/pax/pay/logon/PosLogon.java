package com.pax.pay.logon;

import android.content.Context;

import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.BaseTrans;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.ContextUtils;

/**
 * Created by chenzaoyang on 2017/4/27.
 */

public class PosLogon extends BaseTrans {
    private String transName;
    public PosLogon() {
        super(ETransType.LOGON, null);
        transName = ContextUtils.getString(R.string.trans_logon);

    }
    @Override
    protected void bindStateOnAction() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                Context context = ContextUtils.getActyContext();
                TransProcessListenerImpl transProcessListenerImpl = new TransProcessListenerImpl(context);
                int ret = TransOnline.posLogon(transProcessListenerImpl);
                if (ret != TransResult.SUCC) {
                    transProcessListenerImpl.onHideProgress();
                    transEnd(new ActionResult(ret, null));
                    return;
                }
                transProcessListenerImpl.onHideProgress();
                transEnd(new ActionResult(ret, null));
                return;
            }
        }).start();
    }
    @Override
    public void onActionResult(String state, ActionResult result) {

    }
}
