/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-11
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.EnterAmountTextWatcher;
import com.pax.view.keyboard.CustomKeyboardEditText;

import butterknife.BindView;

public class EnterAmountActivity extends BaseActivityWithTickForAction {

    @BindView(R.id.header_back)
    ImageView imageView;
    @BindView(R.id.header_title)
    TextView titleTv;
    @BindView(R.id.base_amount_input_text)
    TextView textBaseAmount;//base amount text
    @BindView(R.id.tip_amount_input_text)
    TextView textTipAmount;//tip amount text
    @BindView(R.id.tip_amount_ll)
    LinearLayout tipAmountLL;
    @BindView(R.id.amount_edit)
    CustomKeyboardEditText editAmount;//total amount edit text
    @BindView(R.id.prompt_tip)
    TextView promptTip;

    private String title;
    private float percent;
    private boolean isTipMode = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        editAmount.requestFocus();
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        percent = getIntent().getFloatExtra(EUIParamKeys.TIP_PERCENT.toString(), 0.0f);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_enter_amount;
    }

    @Override
    protected void initViews() {

        imageView.setVisibility(View.INVISIBLE);
        titleTv.setText(title);

        editAmount.setText(CurrencyConverter.convert(0L)); //AET-64
        editAmount.requestFocus();
    }

    private EnterAmountTextWatcher amountWatcher = null;

    @Override
    protected void setListeners() {

        amountWatcher = new EnterAmountTextWatcher();
        amountWatcher.setOnTipListener(new EnterAmountTextWatcher.OnTipListener() {
            @Override
            public void onUpdateTipListener(long baseAmount, long tipAmount) {
                textTipAmount.setText(CurrencyConverter.convert(tipAmount));
            }

            @Override
            public boolean onVerifyTipListener(long baseAmount, long tipAmount) {
                return !isTipMode || (baseAmount * percent / 100 >= tipAmount); //AET-33
            }
        });
        editAmount.addTextChangedListener(amountWatcher);

        editAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    FinancialApplication.mApp.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onKeyOk();
                        }
                    });
                } else if (actionId == EditorInfo.IME_ACTION_NONE) {
                    FinancialApplication.mApp.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onKeyCancel();
                        }
                    });
                }
                return false;
            }
        });
    }

    private void onKeyCancel() {
        UpdateAmount(false);
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null)); // AET-64
    }

    private void onKeyOk() {
        if (!isTipMode) {
            if (CurrencyConverter.parse(editAmount.getText().toString().trim()).toString().equals("0")) {
                return;
            }
            UpdateAmount(true);
            return;
        }
        finish(new ActionResult(TransResult.SUCC,
                CurrencyConverter.parse(editAmount.getText().toString().trim()),
                CurrencyConverter.parse(textTipAmount.getText().toString().trim()))
        );
    }

    //update total amount
    private synchronized void UpdateAmount(boolean isTipMode) {
        this.isTipMode = isTipMode;
        editAmount.requestFocus();
        if (isTipMode) {
            textBaseAmount.setVisibility(View.VISIBLE);
            tipAmountLL.setVisibility(View.VISIBLE);
            textBaseAmount.setText(editAmount.getText());
            promptTip.setText(getString(R.string.prompt_tip) + "(max:" + percent + ")");
            textTipAmount.setText("");
            if (amountWatcher != null)
                amountWatcher.setAmount(CurrencyConverter.parse(editAmount.getText().toString().trim()), 0L);
        } else {
            textBaseAmount.setVisibility(View.INVISIBLE);
            tipAmountLL.setVisibility(View.INVISIBLE);
            textBaseAmount.setText("");
            textTipAmount.setText("");
            if (amountWatcher != null)
                amountWatcher.setAmount(0L, 0L);
            editAmount.setText("");
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
