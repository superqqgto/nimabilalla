/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-10
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.view.keyboard.CustomKeyboardEditText;

import butterknife.BindView;

public class EnterAuthCodeActivity extends BaseActivityWithTickForAction {

    @BindView(R.id.header_back)
    ImageView imageView;
    @BindView(R.id.header_title)
    TextView titleTv;
    @BindView(R.id.amount_txt)
    TextView amountTv;
    @BindView(R.id.trans_amount_layout)
    LinearLayout amountLayout;
    @BindView(R.id.prompt_title)
    TextView promptTv1;
    @BindView(R.id.auth_code_input_text)
    CustomKeyboardEditText authCodeTv;

    private String title;
    private String prompt1;
    private String amount;
    private boolean isFirstStart = true;// whether the first time or not

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tickTimer.stop();
        authCodeTv.requestFocus();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (isFirstStart) {
                isFirstStart = false;
            }
        }
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        amount = getIntent().getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_enter_auth_code;
    }

    @Override
    protected void initViews() {

        imageView.setVisibility(View.INVISIBLE);
        titleTv.setText(title);

        if (amount != null && amount.length() != 0) {
            amount = CurrencyConverter.convert(Long.parseLong(amount));
            amountTv.setText(amount);
        } else {
            amountLayout.setVisibility(View.INVISIBLE);
        }

        promptTv1.setText(prompt1);
    }

    @Override
    protected void setListeners() {
        authCodeTv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onKeyboardOk();
                } else if (actionId == EditorInfo.IME_ACTION_NONE) {
                    onKeyboardCancel();
                }
                return false;
            }
        });
    }

    private void onKeyboardCancel() {
        authCodeTv.setText("");
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
    }

    private void onKeyboardOk() {
        String authText = authCodeTv.getText().toString();
        if (!TextUtils.isEmpty(authText)) {
            finish(new ActionResult(TransResult.SUCC, authText));
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
