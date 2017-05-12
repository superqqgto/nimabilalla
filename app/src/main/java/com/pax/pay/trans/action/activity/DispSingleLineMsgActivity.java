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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.quickclick.QuickClickUtils;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.TickTimer;

import butterknife.BindView;
import butterknife.OnClick;

public class DispSingleLineMsgActivity extends BaseActivityWithTickForAction {

    @BindView(R.id.version_prompt)
    TextView tvPrompt;
    @BindView(R.id.header_title)
    TextView tvTitle;
    @BindView(R.id.version_tv)
    TextView tvContent;

    private String navTitle;
    private String prompt;
    private String content;
    private int tickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tickTimer.start(tickTime);
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt = bundle.getString(EUIParamKeys.PROMPT_1.toString());
        content = bundle.getString(EUIParamKeys.CONTENT.toString());
        tickTime = bundle.getInt(EUIParamKeys.TIKE_TIME.toString(), TickTimer.DEFAULT_TIMEOUT);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_disp_single_line_msg_layout;
    }

    @Override
    protected void initViews() {

        tvTitle.setText(navTitle);
        tvPrompt.setText(prompt);

        tvContent.setText(content);
        if (content.contains("￥")) {
            SpannableString msp = new SpannableString(content);
            msp.setSpan(new RelativeSizeSpan(0.7f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(R.dimen.font_size_larger));
            tvContent.setText(msp);
        }
    }

    @Override
    protected void setListeners() {
    }

    @Override
    public void onClickProtected(View v) {
    }

    @OnClick({R.id.header_back, R.id.confirm_btn})
    public void onViewClicked(View view) {

        if (QuickClickUtils.isFastDoubleClick()) {
            return;
        }

        switch (view.getId()) {
            case R.id.header_back:
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
            case R.id.confirm_btn:
                finish(new ActionResult(TransResult.SUCC, null));
                break;
            default:
                break;
        }
    }
}
