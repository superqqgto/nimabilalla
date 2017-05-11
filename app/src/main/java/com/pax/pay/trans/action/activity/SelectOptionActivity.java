/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Auth: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.activity;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.OptionModel;
import com.pax.view.OptionsAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectOptionActivity extends BaseActivityWithTickForAction implements OnClickListener, OnItemClickListener {

    private TextView tvTitle;
    private ImageView ivBack;

    private TextView tvPrompt;
    private GridView mGridView;

    private Button confirmBtn;

    private String navTitle;
    private boolean navBack;
    private String prompt1;

    private OptionsAdapter mAdapter;

    private ArrayList<String> mNameList = new ArrayList<>();
    private OptionModel option;

    private List<OptionModel> data = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_mode_layout;
    }

    @Override
    protected void initViews() {
        ivBack = (ImageView) findViewById(R.id.header_back);

        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);

        tvPrompt = (TextView) findViewById(R.id.prompt_select);
        tvPrompt.setText(prompt1);

        mGridView = (GridView) findViewById(R.id.grid_select);
        data = getData(mNameList);

        mAdapter = new OptionsAdapter(this, data);
        mGridView.setAdapter(mAdapter);

        confirmBtn = (Button) findViewById(R.id.info_confirm);
        confirmBtn.setEnabled(false);

    }

    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }

        mGridView.setOnItemClickListener(this);
        confirmBtn.setOnClickListener(this);

    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        mNameList = getIntent().getStringArrayListExtra(EUIParamKeys.CONTENT.toString());

    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
            case R.id.info_confirm:
                finish(new ActionResult(TransResult.SUCC, option));
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.changeState(position);
        confirmBtn.setEnabled(true);
        confirmBtn.setBackgroundResource(R.drawable.button_click_background);
        option = data.get(position);
    }

    private List<OptionModel> getData(ArrayList<String> list) {
        List<OptionModel> models = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            models.add(new OptionModel(Component.getPaddedNumber(i, 2), list.get(i)));
        }
        return models;
    }

}
