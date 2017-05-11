/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-30
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.settings;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.pax.edc.R;
import com.pax.manager.sp.SpManager;
import com.pax.pay.BaseActivity;
import com.pax.pay.constant.EUIParamKeys;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity{

    @BindView(R.id.header_title)
    TextView headerView;

    String title;
    HashMap<String, String> beforeSet = new HashMap<>();
    HashMap<String, String> afterSet = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loadParam();
        initViews();
        setListeners();
        beforeSet = SpManager.getSysParamSp().getAllParams();
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected void onDestroy() {
        afterSet = SpManager.getSysParamSp().getAllParams();
        if (afterSet != null && !afterSet.toString().equals(beforeSet.toString())) {
            SpManager.getSysParamSp().uploadParamOnline(afterSet);
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void initViews() {
        headerView.setText(title);
    }

    @Override
    protected void setListeners() {
    }

    @OnClick(R.id.header_back)
    public void onBackClicked() {
        setResult(100);// 第三方调用需要
        finish();
    }
}
