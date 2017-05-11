/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-26
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.menu;

import android.content.Intent;
import android.os.Bundle;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionEndListener;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.edc.R;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.UnlockTerminalActivity;
import com.pax.pay.WizardActivity;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.logon.PosLogon;
import com.pax.pay.record.TransQueryActivity;
import com.pax.pay.service.LockService;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionConfirmLock;
import com.pax.pay.trans.action.ActionDispSingleLineMsg;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.view.MenuPage;
import com.pax.view.dialog.DialogUtils;

public class ManageMenuActivity extends BaseMenuActivity {

    @Override
    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(ManageMenuActivity.this, 9, 3)
                // 密码管理
                .addActionItem(R.string.trans_password, R.drawable.app_opermag,
                        createInputPwdActionForManagePassword())
                // Lock Terminal
                .addActionItem(R.string.trans_lock, R.drawable.pwd_small,
                        createConfirmLockAction())
                // 终端设置
                .addActionItem(R.string.settings_title, R.drawable.app_setting,
                        createInputPwdActionForSettings())
                // 交易查询
                .addMenuItem(R.string.trans_history, R.drawable.app_query, TransQueryActivity.class)
                // 查看版本
                .addActionItem(R.string.version, R.drawable.app_version, createDispActionForVersion())

                .addTransItem(R.string.pos_logon, R.drawable.app_poslogon, new PosLogon())
                // 签退
                ;
        return builder.create();
    }

    private AAction createDispActionForVersion() {
        ActionDispSingleLineMsg displayInfoAction = new ActionDispSingleLineMsg(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionDispSingleLineMsg) action).setParam(getString(R.string.version),
                        getString(R.string.app_version), FinancialApplication.versionName, 60);
            }
        });

        displayInfoAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                ActivityStack.getInstance().pop();
            }
        });

        return displayInfoAction;
    }

    private AAction createInputPwdActionForSettings() {
        ActionInputPassword inputPasswordAction = new ActionInputPassword(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(8, getString(R.string.prompt_sys_pwd), null);
            }
        });

        inputPasswordAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {

                if (result.getRet() != TransResult.SUCC) {
                    return;
                }

                String data = EncUtils.SHA1((String) result.getData());
                if (!data.equals(SpManager.getSysParamSp().get(SysParamSp.SEC_SYSPWD))) {
                    DialogUtils.showErrMessage(ManageMenuActivity.this, getString(R.string.settings_title),
                            getString(R.string.err_password), null, Constants.FAILED_DIALOG_SHOW_TIME);
                    return;
                }
                Intent intent = new Intent(ManageMenuActivity.this, WizardActivity.class);
                startActivity(intent);

            }
        });

        return inputPasswordAction;

    }

    private AAction createInputPwdActionForManagePassword() {
        ActionInputPassword inputPasswordAction = new ActionInputPassword(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(6,
                        getString(R.string.prompt_terminal_pwd), null);
            }
        });

        inputPasswordAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                if (result.getRet() != TransResult.SUCC) {
                    return;
                }

                String data = EncUtils.SHA1((String) result.getData());
                if (!data.equals(SpManager.getSysParamSp().get(SysParamSp.SEC_TERMINALPWD))) {
                    DialogUtils.showErrMessage(ManageMenuActivity.this, getString(R.string.trans_password),
                            getString(R.string.err_password), null, Constants.FAILED_DIALOG_SHOW_TIME);
                    return;
                }
                Intent intent = new Intent(ManageMenuActivity.this, PasswordMenuActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.trans_password));
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        return inputPasswordAction;
    }

    private AAction createConfirmLockAction() {
        ActionConfirmLock confirmLock = new ActionConfirmLock(null);

        confirmLock.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {

                if (result.getRet() != TransResult.SUCC) {
                    return;
                }
                Intent intent = new Intent(ManageMenuActivity.this, UnlockTerminalActivity.class);
                startActivity(intent);
            }
        });

        return confirmLock;

    }
}
