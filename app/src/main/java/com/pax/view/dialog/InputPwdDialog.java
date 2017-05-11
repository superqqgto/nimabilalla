/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-1
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.ReplacementTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pax.edc.R;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.Utils;
import com.pax.view.SoftKeyboardPwdStyle;
import com.pax.view.SoftKeyboardPwdStyle.OnItemClickListener;

public class InputPwdDialog extends Dialog {

    private String title; // 标题
    private String prompt; // 提示信息

    private EditText pwdEdt;
    private TextView pwdTv;
    private int maxLength;

    private FrameLayout mFrameLayout;

    public InputPwdDialog(Context context, int length, String title, String prompt) {
        this(context, R.style.popup_dialog);
        this.maxLength = length;
        this.title = title;
        this.prompt = prompt;
    }

    /**
     * 输联机密码时调用次构造方法
     *
     * @param context
     * @param title
     * @param prompt
     */
    public InputPwdDialog(Context context, String title, String prompt) {
        super(context, R.style.popup_dialog);
        this.title = title;
        this.prompt = prompt;
    }

    public InputPwdDialog(Context context, int theme) {
        super(context, theme);

    }

    public interface OnPwdListener {
        void onSucc(String data);

        void onErr();
    }

    private OnPwdListener listener;

    public void setPwdListener(OnPwdListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View convertView = getLayoutInflater().inflate(R.layout.activity_inner_pwd_layout, null);
        setContentView(convertView);
        getWindow().setGravity(Gravity.BOTTOM); // 显示在底部
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = (int) (Utils.getScreenHeight(this.getContext()) * 0.6);  // 屏幕高度（像素）

        getWindow().setAttributes(lp);
        initViews(convertView);
    }

    private void initViews(View view) {

        TextView titleTv = (TextView) view.findViewById(R.id.prompt_title);
        titleTv.setText(title);

        TextView subtitleTv = (TextView) view.findViewById(R.id.prompt_no_pwd);
        if (prompt != null) {
            subtitleTv.setText(prompt);
        } else {
            subtitleTv.setVisibility(View.GONE);
        }

        pwdTv = (TextView) view.findViewById(R.id.pwd_input_text);
        pwdTv.setVisibility(View.GONE);
        pwdEdt = (EditText) view.findViewById(R.id.pwd_input_et);
        pwdEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        Utils.hideSystemKeyboard(getContext(), pwdEdt);
        pwdEdt.setInputType(InputType.TYPE_NULL);
        pwdEdt.setFocusable(true);
        pwdEdt.setTransformationMethod(new WordReplacement());

        mFrameLayout = (FrameLayout) view.findViewById(R.id.fl_trans_softKeyboard);
        SoftKeyboardPwdStyle softKeyboard = (SoftKeyboardPwdStyle) view.findViewById(R.id.pwd_softkeyboard);
        softKeyboard.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(View v, int index) {
                if (index == KeyEvent.KEYCODE_ENTER) {
                    String content = pwdEdt.getText().toString().trim();
                    if (listener != null) {
                        listener.onSucc(content);
                    }
                } else if (index == Constants.KEY_EVENT_CANCEL) {
                    if (listener != null) {
                        listener.onErr();
                    }
                }
            }
        });

    }

    public void setContentText(final String content) {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pwdTv != null) {
                    pwdTv.setText(content);
                    pwdTv.setTextSize(ContextUtils.getResources().getDimension(R.dimen.font_size_key));
                }
            }
        });
    }

    public String getContentText() {
        StringBuffer buffer = new StringBuffer();
        if (pwdTv != null) {
            buffer.append(pwdTv.getText().toString());
        }
        return buffer.toString();
    }

    private class WordReplacement extends ReplacementTransformationMethod {

        private String word;

        @Override
        protected char[] getOriginal() {
            // 循环ASCII值 字符串形式累加到String
            for (char i = 0; i < 256; i++) {
                word += String.valueOf(i);
            }
            return word.toCharArray();
        }

        @Override
        protected char[] getReplacement() {
            char[] charReplacement = new char[255];
            // 输入的字符在ASCII范围内，将其转换为*
            for (int i = 0; i < 255; i++) {
                charReplacement[i] = '*';
            }

            return charReplacement;
        }
    }
}
