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
package com.pax.pay.password;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pax.edc.R;
import com.pax.pay.BaseActivity;
import com.pax.pay.app.quickclick.QuickClickUtils;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.utils.ToastUtils;
import com.pax.pay.utils.Utils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Chang password
 *
 * @author Steven.W
 */
public abstract class BaseChangePwdActivity extends BaseActivity {

    @BindView(R.id.header_title)
    TextView tvTitle;
    @BindView(R.id.setting_new_pwd)
    EditText edtNewPwd;
    @BindView(R.id.setting_confirm_new_pwd)
    EditText edtNewPwdConfirm;

    protected String navTitle;
    protected String pwd;

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chg_pwd_layout;
    }

    @Override
    protected void initViews() {

        tvTitle.setText(navTitle);
        edtNewPwd.requestFocus();
    }

    @Override
    protected void setListeners() {
    }

    @OnClick({R.id.header_back, R.id.setting_confirm_pwd})
    public void onViewClicked(View view) {

        if (QuickClickUtils.isFastDoubleClick(view)) {
            return;
        }

        switch (view.getId()) {
            case R.id.header_back:
                // AET-121
                Utils.hideSystemKeyboard(getApplicationContext(), getCurrentFocus());
                finish();
                break;
            case R.id.setting_confirm_pwd:
                if (updatePwd()) {
                    ToastUtils.showShort(R.string.pwd_succ);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    //将密码保存到系统参数中
    protected abstract void savePwd();

    protected boolean updatePwd() {

        // 检查newPwd合法性
        String newPWD = edtNewPwd.getText().toString();
        if (TextUtils.isEmpty(newPWD)) {
            edtNewPwd.requestFocus();
            return false;
        }

        if (newPWD.length() != 6) {
            ToastUtils.showShort(R.string.pwd_incorrect_length);
            return false;
        }

        // 检查newPwdConfirm合法性
        String newAgainPWD = edtNewPwdConfirm.getText().toString();

        if (TextUtils.isEmpty(newAgainPWD)) {
            edtNewPwdConfirm.requestFocus();
            return false;
        }

        // 比较两次输入是否相同
        if (!newAgainPWD.equals(newPWD)) {
            ToastUtils.showShort(R.string.pwd_not_equal);
            return false;
        }
        pwd = edtNewPwd.getText().toString().trim();
        savePwd();
        return true;
    }
}
