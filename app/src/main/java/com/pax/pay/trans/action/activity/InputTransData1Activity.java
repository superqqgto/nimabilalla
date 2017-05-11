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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.EnterAmountTextWatcher;
import com.pax.pay.utils.ToastUtils;
import com.pax.view.keyboard.CustomKeyboardEditText;

import java.text.SimpleDateFormat;

@SuppressLint("SimpleDateFormat")
public class InputTransData1Activity extends BaseActivityWithTickForAction {

    private TextView headerText;
    private TextView promptText;

    private ImageView backBtn;
    private Button confirmBtn;

    private String prompt;
    private String navTitle;

    private EInputType inputType;

    private boolean isVoidLastTrans;
    private boolean isAuthZero;

    private int maxLen;
    private int minLen;

    private CustomKeyboardEditText mEditText = null;

    private TextView mPromptDoLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEditText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEditText.setText("");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_trans_data1;
    }

    @Override
    protected void loadParam() {
        prompt = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        inputType = (EInputType) getIntent().getSerializableExtra(EUIParamKeys.INPUT_TYPE_1.toString());
        maxLen = getIntent().getIntExtra(EUIParamKeys.INPUT_MAX_LEN_1.toString(), 6);
        minLen = getIntent().getIntExtra(EUIParamKeys.INPUT_MIN_LEN_1.toString(), 0);
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        isVoidLastTrans = getIntent().getBooleanExtra(EUIParamKeys.VOID_LAST_TRANS_UI.toString(), false);
        isAuthZero = getIntent().getBooleanExtra(EUIParamKeys.INPUT_AUTH_ZERO.toString(), true);
    }

    @Override
    protected void initViews() {
        backBtn = (ImageView) findViewById(R.id.header_back);

        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);

        promptText = (TextView) findViewById(R.id.prompt_amount);
        promptText.setText(prompt);

        confirmBtn = (Button) findViewById(R.id.info_confirm);
        mPromptDoLast = (TextView) findViewById(R.id.prompt_do_last);
        if (!isVoidLastTrans) {
            mPromptDoLast.setVisibility(View.INVISIBLE);
        }
    }

    private void setEditText() {
        switch (inputType) {
            case AMOUNT:
                setEditTextAmount();
                break;
            case DATE:
                setEditTextDate();
                break;
            case NUM:
                setEditTextNum();
                break;
            case ALPHNUM:
                setEditTextAlphaNum();
                break;
            case PHONE:
                setEditTextPhone();
                break;
            case EMAIL:
                setEditTextEmail();
                break;
            default:
                break;
        }
        if (mEditText != null) {
            mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onClick(confirmBtn);
                    } else if (actionId == EditorInfo.IME_ACTION_NONE) {
                        onClick(backBtn);
                    }
                    return false;
                }
            });
        }
    }

    // 金额
    private void setEditTextAmount() {
        mPromptDoLast.setVisibility(View.INVISIBLE);
        mEditText = (CustomKeyboardEditText) findViewById(R.id.input_data_1);
        mEditText.requestFocus();
        mEditText.setHint(getString(R.string.amount_default));
        mEditText.addTextChangedListener(new EnterAmountTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                confirmBtnChange();

            }

        });
    }

    // 数字
    private void setEditTextNum() {
        mEditText = (CustomKeyboardEditText) findViewById(R.id.input_data_1);
        mEditText.requestFocus();
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
        if (minLen == 0) {
            confirmBtn.setEnabled(true);
            confirmBtn.setBackgroundResource(R.drawable.button_background);
        } else {
            mEditText.addTextChangedListener(new EnterAmountTextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    confirmBtnChange();
                }
            });
        }

    }

    // 日期
    private void setEditTextDate() {
        mEditText = (CustomKeyboardEditText) findViewById(R.id.input_data_1);
        mEditText.requestFocus();
        mPromptDoLast.setVisibility(View.INVISIBLE);
        mEditText.setHint(getString(R.string.prompt_date_default2));
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEditText.addTextChangedListener(new EnterAmountTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                confirmBtnChange();
            }
        });
    }

    // 数字加字母
    private void setEditTextAlphaNum() {
        mEditText = (CustomKeyboardEditText) findViewById(R.id.input_data_1);
        mEditText.requestFocus();
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
        mEditText.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return "qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM1234567890".toCharArray();
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
        });
        mEditText.addTextChangedListener(new EnterAmountTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                confirmBtnChange();
            }
        });
    }

    private void setEditTextPhone() {
        findViewById(R.id.input_data_1).setVisibility(View.GONE);
        mEditText = (CustomKeyboardEditText) findViewById(R.id.input_data_sp);
        mEditText.setVisibility(View.VISIBLE);
        mEditText.requestFocus();
        //if(Utils.isSimReady(InputTransData1Activity.this)) {
        mEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        mEditText.addTextChangedListener(new EnterAmountTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                confirmBtnChange();
            }
        });
        /*} else {
            mEditText.setEnabled(false);
            mEditText.setFocusable(false);
            mEditText.setFocusableInTouchMode(false);
            mEditText.clearFocus();
            mHintLayout.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.prompt_do_last)).setText(R.string.prompt_no_sms);
        }*/
    }

    private void setEditTextEmail() {
        findViewById(R.id.input_data_1).setVisibility(View.GONE);
        mEditText = (CustomKeyboardEditText) findViewById(R.id.input_data_sp);
        mEditText.setVisibility(View.VISIBLE);
        mEditText.requestFocus();
        mEditText.addTextChangedListener(new EnterAmountTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                confirmBtnChange();
            }
        });
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
                finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                break;
            case R.id.info_confirm:
                String content = process();
                switch (inputType) {
                    case NUM:
                    case ALPHNUM:
                        if (minLen == 0) {
                            break;
                        }

                        if (content == null || content.length() == 0) {
                            ToastUtils.showShort(R.string.please_input_again);
                            return;
                        }
                        break;
                    case AMOUNT:
                    case PHONE:
                    case EMAIL:
                        if (content == null || content.length() == 0) {
                            ToastUtils.showShort(R.string.please_input_again);
                            return;
                        }

                        break;
                    default:
                        break;
                }

                finish(new ActionResult(TransResult.SUCC, content));

                break;
            default:
                break;
        }

    }

    /**
     * 输入数值检查
     */
    private String process() {
        String content = mEditText.getText().toString().trim();

        if (content.length() == 0) {
            return null;
        }

        switch (inputType) {
            case DATE:
                if (content.length() != 4) {
                    return null;
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
                try {
                    dateFormat.setLenient(false);
                    dateFormat.parse(content);
                } catch (Exception e) {
                    return null;
                }
                break;
            case NUM:
                if (content.length() >= minLen && content.length() <= maxLen) {
                    if (isAuthZero) {
                        int flag = maxLen - content.length();
                        for (int i = 0; i < flag; i++) {
                            content = "0" + content;
                        }
                    }
                } else {
                    return null;
                }
                break;
            case ALPHNUM:
                if (content.length() >= minLen && content.length() <= maxLen) {
                    if (isAuthZero) {
                        if (content.length() < maxLen) {
                            int flag = maxLen - content.length();
                            for (int i = 0; i < flag; i++) {
                                content = "0" + content;
                            }
                        }
                    }
                } else {
                    return null;
                }
                break;

            case AMOUNT:
                content = CurrencyConverter.parse(mEditText.getText().toString().trim()).toString();
                if ("0".equals(content))
                    return null;
                break;
            case PHONE:
                break;
            case EMAIL:
                if (!isEmailValid(content))
                    return null;
                break;
            default:
                break;
        }
        return content;
    }

    //Check the email number
    private boolean isEmailValid(String email) {
        String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
        return email.matches(regex);
    }

    private void confirmBtnChange() {
        boolean enable = false;
        String content = mEditText.getText().toString();
        if (content.length() > 0) {
            if (ActionInputTransData.EInputType.AMOUNT == inputType) {
                content = CurrencyConverter.parse(content.trim()).toString();
                if (!"0".equals(content)) {
                    enable = true;
                }
            } else {
                enable = true;
            }
        } else {
            enable = false;
        }

        confirmBtn.setEnabled(enable);
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}