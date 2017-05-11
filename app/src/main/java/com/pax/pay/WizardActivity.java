/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-27
 * Module Author: xiawh
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.edc.R;
import com.pax.manager.sp.ControllerSp;
import com.pax.manager.sp.SpManager;

public class WizardActivity extends BaseActivity{
    //  Button //confirmBtn;
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loadParam();
        initViews();
        setListeners();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_wizard;
    }

    @Override
    protected void initViews() {
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(R.string.settings_title);

        backBtn = (ImageView) findViewById(R.id.header_back);
    }

    @Override
    protected void setListeners() {
        backBtn.setOnClickListener(this);
    }

    @Override
    protected void loadParam() {

    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                SpManager.getControlSp().putInt(ControllerSp.NEED_SET_WIZARD, ControllerSp.Constant.NO);
                Intent intent = getIntent();
                setResult(InitializeInputPwdActivity.REQ_WIZARD, intent);
                finish();
                break;
            default:
                break;
        }
    }

}
