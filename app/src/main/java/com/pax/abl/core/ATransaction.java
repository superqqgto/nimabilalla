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
package com.pax.abl.core;

import com.pax.pay.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * state machine
 *
 * @author Steven.W
 */
public abstract class ATransaction {
    private static final String TAG = ATransaction.class.getSimpleName();
    /**
     * state and action binding table的
     */
    private Map<String, AAction> actionMap;

    /**
     * transaction end listener
     *
     * @author Steven.W
     */
    public interface TransEndListener {
        void onEnd(ActionResult result);
    }

    /**
     * single state bind action
     *
     * @param state
     * @param action
     */
    protected void bind(String state, AAction action) {
        if (actionMap == null) {
            actionMap = new HashMap<>();
        }
        actionMap.put(state, action);
    }

    /**
     * execute action bound by state
     *
     * @param state
     */
    public void gotoState(String state) {
        AAction action = actionMap.get(state);
        if (action != null) {
            action.execute();
        } else {
            LogUtils.e(TAG, "Invalid State:" + state);
        }
    }

    /**
     * execute transaction
     */
    public void execute() {
        bindStateOnAction();
    }

    /**
     * state绑定action抽象方法，在此实现中调用{@link #bind(String, AAction)}方法， 并在最后调用{@link #gotoState(String)}方法，执行第一个state
     */
    protected abstract void bindStateOnAction();

}
