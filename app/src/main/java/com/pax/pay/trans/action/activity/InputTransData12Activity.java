/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-13
 * Module Author: qixw
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.EnterAmountTextWatcher;
import com.pax.pay.utils.ToastUtils;
import com.pax.view.keyboard.CustomKeyboardEditText;

@SuppressLint("SimpleDateFormat")
public class InputTransData12Activity extends BaseActivityWithTickForAction {

    private static String TAG = "InfosInputActivity";

    private TextView headerText;
    private ImageView backBtn;

    private TextView mBaseAmount;
    private TextView mOriTips;
    private TextView mTotalAmount;
    private CustomKeyboardEditText mEditNewTips;

    private Button confirmBtn;

    private String prompt1;
    private int maxLen1;
    private int minLen1;
    private String navTitle;

    private String oriTips;
    private String oriTransAmount;

    private long totalAmountLong = 0L;
    private long tipAmountLong = 0L;
    private long baseAmountLong = 0L;
    private float adjustPercent = 0L;


    private EInputType inputType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEditText();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_info12;
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        inputType = (EInputType) getIntent().getSerializableExtra(EUIParamKeys.INPUT_TYPE_1.toString());
        maxLen1 = getIntent().getIntExtra(EUIParamKeys.INPUT_MAX_LEN_1.toString(), 6);
        minLen1 = getIntent().getIntExtra(EUIParamKeys.INPUT_MIN_LEN_1.toString(), 0);

        oriTransAmount = getIntent().getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
        oriTips = getIntent().getStringExtra(EUIParamKeys.ORI_TIPS.toString());
        totalAmountLong = Long.parseLong(oriTransAmount);
        tipAmountLong = Long.parseLong(oriTips);
        baseAmountLong = totalAmountLong - tipAmountLong;
        adjustPercent = getIntent().getFloatExtra(EUIParamKeys.TIP_PERCENT.toString(), 0.0f);

    }

    @Override
    protected void initViews() {
        backBtn = (ImageView) findViewById(R.id.header_back);

        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);
        mBaseAmount = (TextView) findViewById(R.id.value_base_amount);
        mOriTips = (TextView) findViewById(R.id.value_oritips);
        mTotalAmount = (TextView) findViewById(R.id.value_totalamount);

        mBaseAmount.setText(CurrencyConverter.convert(baseAmountLong));
        mOriTips.setText(CurrencyConverter.convert(tipAmountLong));
        mTotalAmount.setText(CurrencyConverter.convert(totalAmountLong));

        mEditNewTips = (CustomKeyboardEditText) findViewById(R.id.prompt_edit_newtips);
        mEditNewTips.setText(CurrencyConverter.convert(tipAmountLong));
        mEditNewTips.setFocusable(true);
        mEditNewTips.requestFocus();

        confirmBtn = (Button) findViewById(R.id.info_confirm);
    }

    private void setEditText() {
        switch (inputType) {
            case AMOUNT:
                setEditText_amount();
                break;
            default:
                break;
        }
    }

    private EnterAmountTextWatcher amountWatcher = null;

    // 金额
    private void setEditText_amount() {
        mEditNewTips.setHint(getString(R.string.amount_default));
        mEditNewTips.requestFocus();

        confirmBtnChange(); //AET-19

        amountWatcher = new EnterAmountTextWatcher();
        amountWatcher.setOnTipListener(new EnterAmountTextWatcher.OnTipListener() {
            @Override
            public void onUpdateTipListener(long baseAmount, long tipAmount) {
                tipAmountLong = tipAmount;
                totalAmountLong = baseAmountLong + tipAmountLong;
                confirmBtnChange();
                mTotalAmount.setText(CurrencyConverter.convert(totalAmountLong));
            }

            @Override
            public boolean onVerifyTipListener(long baseAmount, long tipAmount) {
                return baseAmountLong * adjustPercent / 100 >= tipAmount;
            }
        });
        mEditNewTips.addTextChangedListener(amountWatcher);
    }

    private void confirmBtnChange() {
        boolean enable = !mOriTips.getText().toString().equals(mEditNewTips.getText().toString());
        confirmBtn.setEnabled(enable);
    }

    @Override
    protected void setListeners() {
        backBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
    }

    @Override
    public void onClickProtected(View v) {

        switch (v.getId()) {
            case R.id.header_back:
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
            case R.id.info_confirm:
                String content = process();
                switch (inputType) {
                    case AMOUNT:
                        if (content == null || content.length() == 0) {
                            ToastUtils.showShort(R.string.please_input_again);
                            return;
                        }
                        break;
                    default:
                        break;
                }
                finish(new ActionResult(TransResult.SUCC, totalAmountLong, tipAmountLong));
                break;
            default:
                break;
        }

    }

    /**
     * 输入数值检查
     */
    private String process() {
        String content = mEditNewTips.getText().toString().trim();

        if (content.length() == 0) {
            return null;
        }

        switch (inputType) {
            case AMOUNT:
                content = CurrencyConverter.parse(mEditNewTips.getText().toString().trim()).toString();
                //tip can be 0, so don't need to check here
            default:
                break;
        }
        return content;
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
