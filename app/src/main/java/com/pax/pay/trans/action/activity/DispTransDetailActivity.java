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
package com.pax.pay.trans.action.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.quickclick.QuickClickUtils;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ViewUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class DispTransDetailActivity extends BaseActivityWithTickForAction {

    @BindView(R.id.header_title)
    TextView tvTitle;
    @BindView(R.id.header_back)
    ImageView ivBack;
    @BindView(R.id.detail_layout)
    LinearLayout llDetailContainer;
    @BindView(R.id.confirm_btn)
    Button btnConfirm;

    private String navTitle;
    private boolean navBack;


    private ArrayList<String> leftColumns = new ArrayList<>();
    private ArrayList<String> rightColumns = new ArrayList<>();

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        leftColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString());
        rightColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trans_detail_layout;
    }

    @Override
    protected void initViews() {

        tvTitle.setText(navTitle);
        ivBack.setVisibility(navBack ? View.VISIBLE : View.GONE);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 15;

        for (int i = 0; i < leftColumns.size(); i++) {
            RelativeLayout layer = ViewUtils.genSingleLineLayout(DispTransDetailActivity.this, leftColumns.get(i),
                    rightColumns.get(i));
            llDetailContainer.addView(layer, params);
        }
    }

    @Override
    protected void setListeners() {
    }

    @OnClick({R.id.header_back, R.id.confirm_btn})
    public void onViewClicked(View view) {

        if (QuickClickUtils.isFastDoubleClick(view)) {
            return;
        }

        switch (view.getId()) {
            case R.id.header_back:
                finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                break;
            case R.id.confirm_btn:
                finish(new ActionResult(TransResult.SUCC, null));
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
