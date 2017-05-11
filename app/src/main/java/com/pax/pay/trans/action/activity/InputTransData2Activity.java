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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.text.style.AbsoluteSizeSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.dal.IScanner;
import com.pax.edc.R;
import com.pax.manager.neptune.DalManager;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.ToastUtils;
import com.pax.view.keyboard.CustomKeyboardEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

@SuppressLint("SimpleDateFormat")
public class InputTransData2Activity extends BaseActivityWithTickForAction {

    private static String TAG = "InfosInputActivity";

    private TextView headerText;
    private ImageView backBtn;

    private TextView promptNum;
    private CustomKeyboardEditText mEditNum;
    private TextView promptExtraNum;
    private CustomKeyboardEditText mEditExtraNum;

    private String prompt1;
    private String prompt2;
    private EInputType inputType1;
    private EInputType inputType2;
    private int maxLen1;
    private int minLen1;
    private int maxLen2;
    private int minLen2;
    private ImageButton scanner;
    private Button confirm;
    private String navTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEditText();
        setEtraEditText();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_info;
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        inputType1 = (EInputType) getIntent().getSerializableExtra(EUIParamKeys.INPUT_TYPE_1.toString());
        maxLen1 = getIntent().getIntExtra(EUIParamKeys.INPUT_MAX_LEN_1.toString(), 6);
        minLen1 = getIntent().getIntExtra(EUIParamKeys.INPUT_MIN_LEN_1.toString(), 0);
        prompt2 = getIntent().getStringExtra(EUIParamKeys.PROMPT_2.toString());
        inputType2 = (EInputType) getIntent().getSerializableExtra(EUIParamKeys.INPUT_TYPE_2.toString());
        maxLen2 = getIntent().getIntExtra(EUIParamKeys.INPUT_MAX_LEN_2.toString(), 6);
        minLen2 = getIntent().getIntExtra(EUIParamKeys.INPUT_MIN_LEN_2.toString(), 0);
    }

    @Override
    protected void initViews() {
        backBtn = (ImageView) findViewById(R.id.header_back);

        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);

        promptNum = (TextView) findViewById(R.id.prompt_num);
        promptNum.setText(prompt1);
        promptExtraNum = (TextView) findViewById(R.id.prompt_extraNum);
        promptExtraNum.setText(prompt2);

        mEditNum = (CustomKeyboardEditText) findViewById(R.id.prompt_edit_num);
        mEditNum.setFocusable(true);
        mEditNum.requestFocus();

        mEditExtraNum = (CustomKeyboardEditText) findViewById(R.id.prompt_edit_extraNum);
        mEditExtraNum.setFocusable(true);

        scanner = (ImageButton) findViewById(R.id.start_scanner);

        confirm = (Button) findViewById(R.id.infos_confirm);
        confirm.setEnabled(false);
    }

    private void setEditText() {
        switch (inputType1) {
            case DATE:
                setEditText_date(mEditNum);
                break;
            case NUM:
                setEditText_num(mEditNum, maxLen1);
                break;
            case ALPHNUM:
                setEditText_alphnum(mEditNum, maxLen1);
            case TEXT:
                setEditText_text(mEditNum, maxLen1);
            default:
                break;
        }
    }

    private void setEtraEditText() {
        switch (inputType2) {
            case DATE:
                setEditText_date(mEditExtraNum);
                break;
            case NUM:
                setEditText_num(mEditExtraNum, maxLen2);
                break;
            case ALPHNUM:
                setEditText_alphnum(mEditExtraNum, maxLen2);
            case TEXT:
                setEditText_text(mEditExtraNum, maxLen2);
            default:
                break;
        }
    }

    // 数字
    private void setEditText_num(EditText editText, int len) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(len)});
        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    // 日期
    private void setEditText_date(EditText editText) {
        SpannableString ss = new SpannableString(getString(R.string.prompt_date_default2));
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(getResources().getDimensionPixelOffset(R.dimen.font_size_large),
                false);
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setHint(new SpannedString(ss)); // 一定要进行转换,否则属性会消失
        editText.setHintTextColor(getResources().getColor(R.color.textEdit_hint));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
    }

    // 数字加字母
    private void setEditText_alphnum(EditText editText, int len) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(len)});
        editText.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return "qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM1234567890".toCharArray();
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
        });
    }

    // 所有输入形式
    private void setEditText_text(EditText editText, int len) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(len)});
    }

    @Override
    protected void setListeners() {
        backBtn.setOnClickListener(this);
        scanner.setOnClickListener(this);

        // 输入框数值监听
        mEditNum.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                confirmBtnChange();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable e) {

            }
        });

        mEditExtraNum.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                confirmBtnChange();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }
        });
        confirm.setOnClickListener(this);
    }

    @Override
    public void onClickProtected(View v) {

        switch (v.getId()) {
            case R.id.header_back:
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
            case R.id.infos_confirm:

                String content = process(mEditNum, inputType1, maxLen1, minLen1);
                if (content == null) {
                    mEditNum.setText("");
                    mEditNum.requestFocus();
                    return;
                }
                if (content.length() == 0) {
                    ToastUtils.showShort(R.string.please_input_again);
                    mEditNum.requestFocus();
                    return;
                }

                String extraContent = process(mEditExtraNum, inputType2, maxLen2, minLen2);
                if (extraContent == null) {
                    mEditExtraNum.requestFocus();
                    return;
                }
                if (extraContent.length() == 0) {
                    ToastUtils.showShort(R.string.prompt_card_date_err);
                    mEditExtraNum.requestFocus();
                    return;
                }

                if (content.length() != 0 && extraContent.length() != 0) {
                    finish(new ActionResult(TransResult.SUCC, new String[]{content, extraContent}));
                }
                break;
            case R.id.start_scanner:
                FinancialApplication.mApp.runInBackground(new Runnable() {

                    @Override
                    public void run() {
                        final IScanner scanner = DalManager.getScannerRear();
                        scanner.open();
                        scanner.start(new ScannerListener());
                    }
                });
                break;
            default:
                break;
        }

    }

    private void confirmBtnChange() {
        String content = process(mEditNum, inputType1, maxLen1, minLen1);
        String extraContent = process(mEditExtraNum, inputType2, maxLen2, minLen2);
        confirm.setEnabled(content != null || extraContent != null);
    }

    /**
     * 输入数值检查
     */
    private String process(EditText editText, EInputType inputType, int maxLen, int minLen) {
        String content = editText.getText().toString().trim();

        if (content.length() == 0) {
            return null;
        }

        switch (inputType) {
            case DATE:
                if (content.length() != 4) {
                    return "";
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
                try {
                    dateFormat.setLenient(false);
                    dateFormat.parse(content);
                } catch (Exception e) {
                    return "";
                }
                return content;
            case NUM:
                if (content.length() >= minLen && content.length() <= maxLen) {
                    return content;
                } else {
                    return "";
                }
            case ALPHNUM:
                if (content.length() >= minLen && content.length() <= maxLen) {
                    if (content.length() < maxLen) {
                        int flag = maxLen1 - content.length();
                        for (int i = 0; i < flag; i++) {
                            content = "0" + content;
                        }
                    }
                } else {
                    return "";
                }
            default:
                break;
        }
        return content;
    }

    // 扫描
    private class ScannerListener implements IScanner.IScanListener {

        @Override
        public void onCancel() {
            DalManager.getScannerRear().close();
        }

        @Override
        public void onFinish() {
            DalManager.getScannerRear().close();
        }

        @Override
        public void onRead(final String result) {
            FinancialApplication.mApp.runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    try {
                        JSONArray resultArray = new JSONArray(result);
                        JSONObject resultObj = resultArray.optJSONObject(0);

                        String code = "";
                        try {
                            code = resultObj.getString("authCode");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String date = "";
                        try {
                            date = resultObj.getString("date");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // 暂时未用transNo
                        // String transNo = "";
                        // try {
                        // transNo = resultObj.getString("transNo");
                        // } catch (JSONException e) {
                        // e.printStackTrace();
                        // }

                        String refNo = "";
                        try {
                            refNo = resultObj.getString("refNo");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // 设置第一个框
                        switch (inputType1) {
                            case NUM:
                                mEditNum.setText(refNo);
                                break;
                            case DATE:
                                mEditNum.setText(date);
                                break;
                            case ALPHNUM:
                                mEditNum.setText(code);
                            default:
                                break;
                        }

                        // 设置第二个框
                        switch (inputType2) {
                            case NUM:
                                mEditExtraNum.setText(refNo);
                                break;
                            case DATE:
                                mEditExtraNum.setText(date);
                                break;
                            case ALPHNUM:
                                mEditExtraNum.setText(code);
                                break;
                            default:
                                break;
                        }

                    } catch (JSONException e) {
                        LogUtils.i(TAG, e.getMessage());
                    }

                }
            });

        }

    }
}
