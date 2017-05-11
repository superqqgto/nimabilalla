/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-28
 * Module Author: caowb
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.view.fragment.SelectAcqFragment;

import java.util.ArrayList;

public class SelectAcqActivity extends BaseActivityWithTickForAction {

    private FrameLayout mContent;
    private ArrayList<String> checkedAcqs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tickTimer.stop();//do it in SelectAcqFragment
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_selectacq_layout;
    }

    @Override
    protected void initViews() {
        mContent = (FrameLayout) findViewById(R.id.ll_content);
        setContent(new SelectAcqFragment());
    }

    public void setContent(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(Constants.ACQUIRER_NAME, checkedAcqs);
        fragment.setArguments(bundle);
        ft.replace(mContent.getId(), fragment);
        ft.commit();
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            checkedAcqs = bundle.getStringArrayList(Constants.ACQUIRER_NAME);
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
