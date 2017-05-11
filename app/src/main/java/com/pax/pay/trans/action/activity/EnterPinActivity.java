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
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.dal.IPed.*;
import com.pax.dal.entity.EKeyCode;
import com.pax.dal.entity.RSAPinKey;
import com.pax.dal.exceptions.EPedDevException;
import com.pax.dal.exceptions.PedDevException;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.manager.neptune.DalManager;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionEnterPin.OfflinePinResult;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.LogUtils;
import com.pax.view.dialog.CustomAlertDialog;

import butterknife.BindView;

@SuppressLint("SimpleDateFormat")
public class EnterPinActivity extends BaseActivityWithTickForAction {

    @BindView(R.id.header_back)
    ImageView imageView;
    @BindView(R.id.header_title)
    TextView titleTv;
    @BindView(R.id.prompt_title)
    TextView promptTv1;
//    @BindView(R.id.pwd_input_text)
    @BindView(R.id.pin_input_text)
    TextView pwdTv;
//    @BindView(R.id.prompt_no_pwd)
    @BindView(R.id.prompt_no_pin)
    TextView promptTv2;
    @BindView(R.id.trans_total_amount_layout)
    LinearLayout totalAmountLayout;
    @BindView(R.id.trans_tip_amount_layout)
    LinearLayout tipAmountLayout;
    @BindView(R.id.tip_amount_txt)
    TextView tipAmountTv;
    @BindView(R.id.total_amount_txt)
    TextView totalAmountTv;

    private String title;
    private String panBlock;
    private String prompt2;
    private String prompt1;
    private String totalAmount;
    private String tipAmount;


    private CustomAlertDialog promptDialog;
    private boolean supportBypass;

    private boolean isFirstStart = true;// 判断界面是否第一次加载
    private EEnterPinType enterPinType;

    private RSAPinKey rsaPinKey;
    private static final byte ICC_SLOT = 0x00;
    public static final String OFFLINE_EXP_PIN_LEN = "0,4,5,6,7,8,9,10,11,12";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 界面不需要超时， 超时有输密码接口控制
        tickTimer.stop();
    }

    // 当页面加载完成之后再执行弹出键盘的动作
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (isFirstStart) {
                if (enterPinType == EEnterPinType.ONLINE_PIN) {
                    enterOnlinePin(panBlock, supportBypass);
                } else if (enterPinType == EEnterPinType.OFFLINE_CIPHER_PIN) {
                    enterOfflineCipherPin();
                } else if (enterPinType == EEnterPinType.OFFLINE_PLAIN_PIN) {
                    enterOfflinePlainPin();
                }
                isFirstStart = false;
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_pin;
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        prompt2 = getIntent().getStringExtra(EUIParamKeys.PROMPT_2.toString());
        totalAmount = getIntent().getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
        tipAmount = getIntent().getStringExtra(EUIParamKeys.TIP_AMOUNT.toString());

        enterPinType = (EEnterPinType) getIntent().getSerializableExtra(EUIParamKeys.ENTERPINTYPE.toString());
        if (enterPinType == EEnterPinType.ONLINE_PIN) {
            panBlock = getIntent().getStringExtra(EUIParamKeys.PANBLOCK.toString());
            supportBypass = getIntent().getBooleanExtra(EUIParamKeys.SUPPORTBYPASS.toString(), false);
        } else {
            rsaPinKey = getIntent().getParcelableExtra(EUIParamKeys.RSA_PIN_KEY.toString());
        }
    }

    @Override
    protected void initViews() {

        imageView.setVisibility(View.INVISIBLE);
        titleTv.setText(title);

        if (totalAmount != null && totalAmount.length() != 0) {
            totalAmount = CurrencyConverter.convert(Long.parseLong(totalAmount));
            totalAmountTv.setText(totalAmount);
        } else {
            totalAmountLayout.setVisibility(View.INVISIBLE);
        }

        if (tipAmount != null && tipAmount.length() != 0) {
            tipAmount = CurrencyConverter.convert(Long.parseLong(tipAmount));
            tipAmountTv.setText(tipAmount);
        } else {
            tipAmountLayout.setVisibility(View.INVISIBLE);
        }

        promptTv1.setText(prompt1);

        if (prompt2 != null) {
            promptTv2.setText(prompt2);
        } else {
            promptTv2.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void setListeners() {
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

    private void enterOnlinePin(final String panBlock, final boolean supportBypass) {
        FinancialApplication.mApp.runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    DalManager.getPedInternal().setIntervalTime(1, 1);
                    DalManager.getPedInternal().setInputPinListener(new IPedInputPinListener() {

                        @Override
                        public void onKeyEvent(final EKeyCode arg0) {
                            String temp = "";
                            if (arg0 == EKeyCode.KEY_CLEAR) {
                                temp = "";
                            } else if (arg0 == EKeyCode.KEY_ENTER || arg0 == EKeyCode.KEY_CANCEL) {
                                // do nothing
                                return;
                            } else {
                                temp = pwdTv.getText().toString();
                                temp += "*";
                            }
                            setContentText(temp);
                        }
                    });

                    byte[] pinData = Device.getPinBlock(panBlock, supportBypass);
                    if (pinData == null || pinData.length == 0)
                        finish(new ActionResult(TransResult.SUCC, null));
                    else {
                        finish(new ActionResult(TransResult.SUCC, GlManager.bcdToStr(pinData)));
                    }
                } catch (final PedDevException e) {
                    e.printStackTrace();

                    if (e.getErrCode() == EPedDevException.PED_ERR_INPUT_CANCEL.getErrCodeFromBasement()) {
                        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                    } else {

                        FinancialApplication.mApp.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Device.beepErr();
                                promptDialog = new CustomAlertDialog(EnterPinActivity.this, CustomAlertDialog.ERROR_TYPE);
                                promptDialog.setTimeout(3);
                                promptDialog.setContentText(e.getErrMsg());
                                promptDialog.show();
                                promptDialog.showConfirmButton(true);
                                promptDialog.setOnDismissListener(new OnDismissListener() {

                                    @Override
                                    public void onDismiss(DialogInterface arg0) {
                                        finish(new ActionResult(TransResult.ERR_ABORTED, null));
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
    }

    public void enterOfflineCipherPin() {
        FinancialApplication.mApp.runInBackground(new Runnable() {

            @Override
            public void run() {

                try {
                    DalManager.getPedInternal().setInputPinListener(new IPedInputPinListener() {

                        @Override
                        public void onKeyEvent(final EKeyCode arg0) {
                            String temp = "";
                            if (arg0 == EKeyCode.KEY_CLEAR) {
                                temp = "";
                            } else if (arg0 == EKeyCode.KEY_ENTER || arg0 == EKeyCode.KEY_CANCEL) {
                                // do nothing
                                return;
                            } else {
                                temp = pwdTv.getText().toString();
                                temp += "*";
                            }
                            setContentText(temp);
                        }
                    });
                    DalManager.getPedInternal().setIntervalTime(1, 1);
                    byte[] resp = DalManager.getPedInternal().verifyCipherPin(ICC_SLOT, OFFLINE_EXP_PIN_LEN, rsaPinKey, (byte) 0x00, 60 * 1000);
                    OfflinePinResult offlinePinResult = new OfflinePinResult();
                    offlinePinResult.setRet(EEmvExceptions.EMV_OK.getErrCodeFromBasement());
                    offlinePinResult.setRespOut(resp);
                    finish(new ActionResult(TransResult.SUCC, offlinePinResult));
                } catch (PedDevException e) {
                    e.printStackTrace();
                    if (e.getErrCode() == EPedDevException.PED_ERR_INPUT_CANCEL.getErrCodeFromBasement()) {
                        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                    } else {
                        OfflinePinResult offlinePinResult = new OfflinePinResult();
                        offlinePinResult.setRet(e.getErrCode());
                        finish(new ActionResult(TransResult.ERR_ABORTED, offlinePinResult));
                    }
                }
            }
        });
    }

    public void enterOfflinePlainPin() {
        FinancialApplication.mApp.runInBackground(new Runnable() {

            @Override
            public void run() {

                try {
                    DalManager.getPedInternal().setInputPinListener(new IPedInputPinListener() {

                        @Override
                        public void onKeyEvent(final EKeyCode arg0) {
                            String temp = "";
                            if (arg0 == EKeyCode.KEY_CLEAR) {
                                temp = "";
                            } else if (arg0 == EKeyCode.KEY_ENTER || arg0 == EKeyCode.KEY_CANCEL) {
                                // do nothing
                                return;
                            } else {
                                temp = pwdTv.getText().toString();
                                temp += "*";
                            }
                            setContentText(temp);
                        }
                    });
                    DalManager.getPedInternal().setIntervalTime(1, 1);
                    byte[] resp = DalManager.getPedInternal().verifyPlainPin(ICC_SLOT, OFFLINE_EXP_PIN_LEN, (byte) 0x00, 60 * 1000);
                    if (resp == null || resp.length == 0) {
                        LogUtils.i("TAG", "verifyPlainPin resp = null or len = 0");
                    } else {
                        LogUtils.i("TAG", "verifyPlainPin resp = " + GlManager.bcdToStr(resp));
                    }
                    OfflinePinResult offlinePinResult = new OfflinePinResult();
                    offlinePinResult.setRet(EEmvExceptions.EMV_OK.getErrCodeFromBasement());
                    offlinePinResult.setRespOut(resp);
                    finish(new ActionResult(TransResult.SUCC, offlinePinResult));
                } catch (PedDevException e) {
                    e.printStackTrace();
                    if (e.getErrCode() == EPedDevException.PED_ERR_INPUT_CANCEL.getErrCodeFromBasement()) {
                        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                    } else {
                        OfflinePinResult offlinePinResult = new OfflinePinResult();
                        offlinePinResult.setRet(e.getErrCode());
                        finish(new ActionResult(TransResult.ERR_ABORTED, offlinePinResult));
                    }
                }

            }
        });
    }

}