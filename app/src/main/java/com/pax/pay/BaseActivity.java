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
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

import com.pax.device.Device;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.quickclick.QuickClickUtils;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
public abstract class BaseActivity extends FragmentActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Device.enableStatusBar(false);
        Device.enableHomeRecentKey(false);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        loadParam();
        initViews();
        setListeners();
        WeakReference<BaseActivity> weakReference = new WeakReference<>(this);
        ActivityStack.getInstance().push(weakReference.get());
    }

    /**
     * get layout ID
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * views initial
     */
    protected abstract void initViews();

    /**
     * set listeners
     */
    protected abstract void setListeners();

    /**
     * load parameter
     */
    protected abstract void loadParam();

    // AET-93
    @Override
    public final void onClick(View v) {
        if (QuickClickUtils.isFastDoubleClick(v, 800)) {
            return;
        }
        onClickProtected(v);
    }

    protected void onClickProtected(View v) {
        //do nothing
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (QuickClickUtils.isFastDoubleClick(800)) { //AET-123
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return onKeyBackDown();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected boolean onKeyBackDown(){
        finish();
        return true;
    }
}
