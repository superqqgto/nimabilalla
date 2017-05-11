/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-13
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.dal.entity.EReaderType;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.EnterAmountTextWatcher;
import com.pax.view.keyboard.CustomKeyboardEditText;

import butterknife.BindView;

public class AdjustTipActivity extends BaseActivityWithTickForAction {

    @BindView(R.id.header_back)
    ImageView imageView;
    @BindView(R.id.header_title)
    TextView titleTv;
    @BindView(R.id.prompt_tip)
    TextView promptTip;
    @BindView(R.id.tip_amount_ll)
    LinearLayout tipAmountLL;
    @BindView(R.id.base_amount_input_text)
    TextView textBaseAmount;//base amount text
    @BindView(R.id.tip_amount_input_text)
    TextView textTipAmount;//tip amount text
    @BindView(R.id.amount_edit)
    CustomKeyboardEditText editAmount;//total amount edit text

    private String title;
    private String amount;
    private float percent;

    private String cardMode;
    private boolean isFirstStart = true;// whether the first time or not

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (isFirstStart) {
                editAmount.requestFocus();
                isFirstStart = false;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        amount = getIntent().getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
        percent = getIntent().getFloatExtra(EUIParamKeys.TIP_PERCENT.toString(), 0.0f);
        cardMode = getIntent().getStringExtra(EUIParamKeys.CARD_MODE.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_enter_amount;
    }

    @Override
    protected void initViews() {
        imageView.setVisibility(View.INVISIBLE);

        titleTv.setText(title);

        textBaseAmount.setVisibility(View.VISIBLE);
        textBaseAmount.setText(CurrencyConverter.convert(Long.parseLong(amount)));

        promptTip.setText(getString(R.string.prompt_tip) + "(max:" + percent + ")");

        tipAmountLL.setVisibility(View.VISIBLE);
        textTipAmount.setText(CurrencyConverter.convert(Long.parseLong("0")));

        editAmount.setText(CurrencyConverter.convert(Long.parseLong(amount)));
        editAmount.requestFocus();
    }

    @Override
    protected void setListeners() {

        EnterAmountTextWatcher amountWatcher = new EnterAmountTextWatcher();
        amountWatcher.setAmount(Long.parseLong(amount), 0L);

        amountWatcher.setOnTipListener(new EnterAmountTextWatcher.OnTipListener() {
            @Override
            public void onUpdateTipListener(long baseAmount, long tipAmount) {
                textTipAmount.setText(CurrencyConverter.convert(tipAmount));
            }

            @Override
            public boolean onVerifyTipListener(long baseAmount, long tipAmount) {
                return (baseAmount * percent / 100 >= tipAmount); //AET-33
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
        textTipAmount.setText("");
        editAmount.setText("");
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
    }

    private void onKeyOk() {
        if ((cardMode != null) && (cardMode.equals(EReaderType.ICC.toString()))) {
            Intent intent = getIntent();
            intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), editAmount.getText().toString().trim());
            intent.putExtra(EUIParamKeys.TIP_AMOUNT.toString(), textTipAmount.getText().toString().trim());
            setResult(SearchCardActivity.REQ_ADJUST_TIP, intent);
            finish();
        } else {
            finish(new ActionResult(TransResult.SUCC, editAmount.getText().toString().trim(), textTipAmount.getText().toString().trim()));
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        onKeyCancel();
        return true;
    }
}
