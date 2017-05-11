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
package com.pax.pay;

import android.os.Bundle;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.TickTimer;

public abstract class BaseActivityWithTickForAction extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tickTimer = new TickTimer(new TickTimer.OnTickTimerListener() {
            @Override
            public void onTick(long leftTime) {
                LogUtils.i("TAG", "onTick:" + leftTime);
            }

            @Override
            public void onFinish() {
                onTimerFinish();
            }
        });
        tickTimer.start();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tickTimer.stop();
    }

    protected TickTimer tickTimer;


    public void finish(ActionResult result) {
        tickTimer.stop();
        AAction action = TransContext.getInstance().getCurrentAction();
        if (action != null) {
            action.setResult(result);
        } else {
            finish();
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_ABORTED, null));
        return true;
    }

    protected void onTimerFinish() {
        finish(new ActionResult(TransResult.ERR_TIMEOUT, null));
    }
}
