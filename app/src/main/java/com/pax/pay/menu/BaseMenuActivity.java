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
package com.pax.pay.menu;

import android.app.ActionBar.LayoutParams;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.edc.R;
import com.pax.pay.BaseActivity;
import com.pax.pay.app.quickclick.QuickClickUtils;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.view.MenuPage;

import butterknife.BindView;
import butterknife.OnClick;

public abstract class BaseMenuActivity extends BaseActivity {

    /**
     * 9宫格菜单容器
     */
    @BindView(R.id.ll_container)
    LinearLayout llContainer;
    /**
     * 抬头
     */
    @BindView(R.id.header_title)
    TextView tvTitle;
    /**
     * 返回按钮
     */
    @BindView(R.id.header_back)
    ImageView IvBack;

    /**
     * 显示的抬头
     */
    private String navTitle;
    /**
     * 是否显示返回按钮
     */
    private boolean navBack;

    @Override
    protected int getLayoutId() {
        return R.layout.menu_layout;
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
    }

    @Override
    protected void initViews() {

        IvBack.setVisibility(navBack ? View.VISIBLE : View.GONE);
        tvTitle.setText(navTitle);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        llContainer.addView(createMenuPage(), params);
    }

    public abstract MenuPage createMenuPage();

    @Override
    protected void setListeners() {
    }

    @Override
    public void onClickProtected(View v) {
    }

    @OnClick(R.id.header_back)
    public void onViewClicked() {
        if (QuickClickUtils.isFastDoubleClick(null)) {
            return;
        }
        finish();
    }
}
