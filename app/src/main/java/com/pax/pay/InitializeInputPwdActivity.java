/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-27
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.pax.abl.utils.EncUtils;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.sp.ControllerSp;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.base.Acquirer;
import com.pax.pay.base.CardRange;
import com.pax.pay.base.Issuer;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.EmvTestAID;
import com.pax.pay.emv.EmvTestCAPK;
import com.pax.pay.trans.TransResult;
import com.pax.manager.AcqManager;
import com.pax.pay.utils.KeyBoardUtils;
import com.pax.pay.utils.ToastUtils;
import com.pax.view.SoftKeyboardPwdStyle;
import com.pax.view.SoftKeyboardPwdStyle.OnItemClickListener;
import com.pax.view.dialog.DialogUtils;

public class InitializeInputPwdActivity extends BaseActivity implements OnItemClickListener {

    private EditText edtPwd;

    private FrameLayout flKeyboardContainer;
    private SoftKeyboardPwdStyle softKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyBoardUtils.show(InitializeInputPwdActivity.this, flKeyboardContainer);
    }

    @Override
    protected void loadParam() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_initialize_pwd_layout;
    }

    @Override
    protected void initViews() {
        edtPwd = (EditText) findViewById(R.id.operator_pwd_edt);

        flKeyboardContainer = (FrameLayout) findViewById(R.id.fl_trans_softKeyboard);
        softKeyboard = (SoftKeyboardPwdStyle) findViewById(R.id.soft_keyboard_view);
    }

    @Override
    protected void setListeners() {
        edtPwd.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyBoardUtils.show(InitializeInputPwdActivity.this, flKeyboardContainer);
                return true;
            }
        });
        softKeyboard.setOnItemClickListener(this);
    }

    @Override
    protected boolean onKeyBackDown() {
        // exit app
        DialogUtils.showExitAppDialog(InitializeInputPwdActivity.this);
        return true;
    }

    /**
     * check password
     */
    private void process() {
        String password = edtPwd.getText().toString().trim();
        if (password.length() == 0) {
            edtPwd.setFocusable(true);
            edtPwd.requestFocus();
            return;
        }
        if (!EncUtils.SHA1(password).equals(SpManager.getSysParamSp().get(SysParamSp.SEC_TERMINALPWD))) {
            ToastUtils.showShort(R.string.error_password);
            edtPwd.setText("");
            edtPwd.setFocusable(true);
            edtPwd.requestFocus();
            return;
        }

        //start wizard activity
        Intent intent = new Intent(this, WizardActivity.class);
        startActivityForResult(intent, REQ_WIZARD);
        //Intent intent = getIntent();
        //setResult(Activity.RESULT_OK, intent);
        //finish();
    }

    public static final int REQ_WIZARD = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_WIZARD:
                //remain for process result
                Intent intent = getIntent();
                setResult(MainActivity.REQ_INITIALIZE, intent);
                insertAcquirer();
                initEMVParam();
                SpManager.getControlSp().putBoolean(ControllerSp.IS_FIRST_RUN, false);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(View v, int index) {
        if (index == KeyEvent.KEYCODE_ENTER) {
            FinancialApplication.mApp.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    process();
                }
            });
        } else if (index == Constants.KEY_EVENT_CANCEL) {
            FinancialApplication.mApp.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtils.showExitAppDialog(InitializeInputPwdActivity.this);
                }
            });
        }
    }

    private int initEMVParam() {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // emv公钥下载
                ControllerSp ctrlSp = SpManager.getControlSp();
                if (ctrlSp.getInt(ControllerSp.NEED_DOWN_CAPK) == ControllerSp.Constant.YES) {
                    EmvTestCAPK.load();
                    ctrlSp.putInt(ControllerSp.NEED_DOWN_CAPK, ControllerSp.Constant.NO);
                }
                // emv 参数下载
                if (ctrlSp.getInt(ControllerSp.NEED_DOWN_AID) == ControllerSp.Constant.YES) {
                    EmvTestAID.load();
                    ctrlSp.putInt(ControllerSp.NEED_DOWN_AID, ControllerSp.Constant.NO);
                }

                ToastUtils.showShort(R.string.emv_init_succ);
            }
        });
        return TransResult.SUCC;
    }

    private void insertAcquirer() {
        AcqManager acqManager = AcqManager.getInstance();
        Acquirer acquirer = new Acquirer("acquirer0");
        acquirer.setNii("019");
        acquirer.setMerchantId("123456789012345");
        acquirer.setTerminalId("12345678");
        acquirer.setCurrBatchNo(1);
//        acquirer.setIp("127.0.0.1");
//        acquirer.setPort((short) 10001);
        acquirer.setIp("192.168.253.1");
        acquirer.setPort((short) 8000);
        SpManager.getSysParamSp().set(SysParamSp.ACQ_NAME, "acquirer0");
        DbManager.getAcqDao().insertAcquirer(acquirer);
        acqManager.setCurAcq(acquirer);

        Acquirer acquirer2 = new Acquirer("acquirer1");
        acquirer2.setNii("020");
        acquirer2.setMerchantId("222456789012345");
        acquirer2.setTerminalId("22245678");
        acquirer2.setCurrBatchNo(1);
        acquirer2.setIp("127.0.0.1");
        acquirer2.setPort((short) 10001);
        DbManager.getAcqDao().insertAcquirer(acquirer2);

        Issuer issuer = new Issuer("VISA");
        issuer.setPanMaskPattern(Constants.DEF_PAN_MASK_PATTERN);
        issuer.setFloorLimit(0);
        issuer.setAdjustPercent(10);
        DbManager.getAcqDao().insertIssuer(issuer);
        DbManager.getAcqDao().bind(acquirer, issuer);

        CardRange cardRang = new CardRange("VISA", "4000000000", "4999999999", 0, issuer);
        DbManager.getAcqDao().insertCardRange(cardRang);

        issuer = new Issuer("MASTER");
        issuer.setPanMaskPattern(Constants.DEF_PAN_MASK_PATTERN);
        issuer.setFloorLimit(0);
        issuer.setAdjustPercent(10);
        DbManager.getAcqDao().insertIssuer(issuer);
        DbManager.getAcqDao().bind(acquirer, issuer);

        cardRang = new CardRange("MASTER", "5000000000", "5999999999", 0, issuer);
        DbManager.getAcqDao().insertCardRange(cardRang);
        cardRang = new CardRange("MASTER", "2000000000", "2999999999", 0, issuer);
        DbManager.getAcqDao().insertCardRange(cardRang);

        issuer = new Issuer("UnionPay");
        issuer.setPanMaskPattern(Constants.DEF_PAN_MASK_PATTERN);
        issuer.setFloorLimit(0);
        issuer.setAdjustPercent(10);
        DbManager.getAcqDao().insertIssuer(issuer);
        DbManager.getAcqDao().bind(acquirer, issuer);

        cardRang = new CardRange("UnionPay", "6000000000", "6999999999", 0, issuer);
        DbManager.getAcqDao().insertCardRange(cardRang);
    }


}
