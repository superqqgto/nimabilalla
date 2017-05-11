/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-03-10
 * Module Author: huangwp
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.activity;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.trans.TransResult;

public class UserAgreementActivity extends BaseActivityWithTickForAction {
    public final static String CANCEL_BUTTON = "CANCEL";
    public final static String ENTER_BUTTON = "ENTER";
    private String agreementFilePath = "file:///android_asset/agreement_content.html";

    private TextView headerText;
    private WebView agreementText;
    private ImageView backBtn;

    private Button cancelBtn;
    private Button confirmBtn;
    private CheckBox checkBox;

    private final static String USEE_AGREEMENT = "";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_agreement_layout;
    }

    @Override
    protected void loadParam() {
    }

    @Override
    protected void initViews() {

        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(R.string.trans_user_agreement);
        agreementText = (WebView) findViewById(R.id.AgreementContent);
        agreementText.getSettings().setJavaScriptEnabled(false);
        agreementText.loadUrl(agreementFilePath);

        backBtn = (ImageView) findViewById(R.id.header_back);
        backBtn.setVisibility(View.GONE);
        checkBox = (CheckBox) findViewById(R.id.AgreementCheck);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        confirmBtn = (Button) findViewById(R.id.enter_btn);
    }

    @Override
    protected void setListeners() {
        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        checkBox.setOnClickListener(this);
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    @Override
    public void onClickProtected(View v) {

        switch (v.getId()) {
            case R.id.cancel_btn:
                finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                break;
            case R.id.enter_btn:
                finish(new ActionResult(TransResult.SUCC, ENTER_BUTTON));
                break;
            case R.id.AgreementCheck:
                confirmBtn.setEnabled(checkBox.isChecked());
                break;
            default:
                break;
        }

    }
}
