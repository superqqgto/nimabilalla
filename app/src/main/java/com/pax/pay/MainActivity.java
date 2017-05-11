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
package com.pax.pay;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.sp.ControllerSp;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.AdConstants;
import com.pax.pay.menu.AuthMenuActivity;
import com.pax.pay.menu.MotoMenuActivity;
import com.pax.pay.menu.ManageMenuActivity;
import com.pax.pay.menu.OtherFunctionMenuActivity;
import com.pax.pay.trans.AdjustTrans;
import com.pax.pay.trans.OfflineSaleTrans;
import com.pax.pay.trans.RefundTrans;
import com.pax.pay.trans.SaleTrans;
import com.pax.pay.trans.SaleVoidTrans;
import com.pax.pay.trans.SettleTrans;
import com.pax.pay.trans.component.Component;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.EnterAmountTextWatcher;
import com.pax.pay.utils.LogUtils;
import com.pax.view.widget.AmountWidget;
import com.pax.view.MenuPage;
import com.pax.view.dialog.DialogUtils;
import com.pax.view.keyboard.CustomKeyboardEditText;

import java.util.ArrayList;

import cn.bingoogolapple.bgabanner.BGABanner;
import io.github.skyhacker2.sqliteonweb.SQLiteOnWeb;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static String NEED_SELF_TEST = "true";

    private boolean isFromWidget;

    // AET-87 remove payType

    private CustomKeyboardEditText edtAmount; // input amount
    private MenuPage menuPage;

    private boolean isInstalledNeptune = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SQLiteOnWeb.init(this).start();

        super.onCreate(savedInstanceState);

        isInstalledNeptune = Component.neptuneInstalled(this, new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        if (!isInstalledNeptune) {
            return;
        }
        SpManager.getControlSp().putInt(ControllerSp.NEED_SET_WIZARD, ControllerSp.Constant.YES);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInstalledNeptune) {
            return;
        }
        ActivityStack.getInstance().popAllButBottom();

        //modify by xiawh
        //activity wizard instead of checklog

        //not start initialize activity
        //SysParam.IS_INITED = "true";
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onCheckInit();
            }
        });

        resetUI();

        SpManager.getSysParamSp().setUpdateListener(new SysParamSp.UpdateListener() {

            @Override
            public void onErr(String prompt) {
                DialogUtils.showUpdateDialog(MainActivity.this, prompt);
            }
        });
        SpManager.getSysParamSp().init();
    }

    @Override
    protected void loadParam() {
        //If widget call MainActivity, need to show keyboard immediately.
        Intent intent = getIntent();
        isFromWidget = intent.getBooleanExtra(AmountWidget.KEY, false);
        LogUtils.d(TAG, "loadParam: data:" + isFromWidget);

        CurrencyConverter.setDefCurrency(SpManager.getSysParamSp().get(SysParamSp.EDC_CURRENCY_LIST));
    }

    /**
     * reset MainActivity
     */
    private void resetUI() {
        menuPage.setCurrentPager(0);
        UpdateAmount();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        // set amount input box
        edtAmount = (CustomKeyboardEditText) findViewById(R.id.amount_editText);
        edtAmount.setText("");

        LinearLayout mLayout = (LinearLayout) findViewById(R.id.ll_gallery);
        android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        menuPage = createMenu();
        mLayout.addView(menuPage, params);

        BGABanner banner = (BGABanner) findViewById(R.id.banner_guide_content);
        banner.setAdapter(new BGABanner.Adapter<ImageView, String>() {
            @Override
            public void fillBannerItem(BGABanner banner, ImageView itemView, String model, int position) {
                Glide.with(MainActivity.this)
                        .load(model)
                        .centerCrop()
                        .dontAnimate()
                        .into(itemView);
            }
        });

        banner.setData(new ArrayList<>(AdConstants.ad.keySet()), null);
        banner.setDelegate(new BGABanner.Delegate<ImageView, String>() {
            @Override
            public void onBannerItemClick(BGABanner banner, ImageView itemView, String model, int position) {
                edtAmount.clearFocus(); //AET-78
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.KEY, AdConstants.ad.get(model));
                intent.putExtra(WebViewActivity.IS_FROM_WIDGET, false);
                startActivity(intent);
            }
        });

        if (isFromWidget) {
            edtAmount.requestFocus();
        } else {
            edtAmount.clearFocus();
        }
    }

    public ATransaction.TransEndListener listener = new ATransaction.TransEndListener() {

        @Override
        public void onEnd(ActionResult result) {
            FinancialApplication.mApp.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    resetUI();
                }
            });

        }
    };

    /*
     * create menu
     */
    private MenuPage createMenu() {
        MenuPage.Builder builder = new MenuPage.Builder(MainActivity.this, 9, 3)
                // void
                .addTransItem(R.string.trans_void, R.drawable.app_void,
                        new SaleVoidTrans(listener))
                // refund
                .addTransItem(R.string.trans_refund, R.drawable.app_refund,
                        new RefundTrans(listener))
                // pre-authorization
                .addMenuItem(R.string.trans_preAuth, R.drawable.app_auth, AuthMenuActivity.class)
                // moto pre-authorization
                .addMenuItem(R.string.trans_moto_preAuth, R.drawable.app_auth, MotoMenuActivity.class)
                //offline
                .addTransItem(R.string.trans_offline, R.drawable.app_sale,
                        new OfflineSaleTrans(listener))
                //adjust
                .addTransItem(R.string.trans_adjust, R.drawable.app_adjust,
                        new AdjustTrans(listener))
                // 结算 AET-14
                .addTransItem(R.string.trans_settle, R.drawable.app_settle,
                        new SettleTrans(null))
                // management
                .addMenuItem(R.string.trans_manage, R.drawable.app_manage, ManageMenuActivity.class)
                // other function
                .addMenuItem(R.string.trans_other_function, R.drawable.app_other, OtherFunctionMenuActivity.class);


        return builder.create();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void setListeners() {

        EnterAmountTextWatcher amountWatcher = new EnterAmountTextWatcher();
        edtAmount.addTextChangedListener(amountWatcher);
        edtAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

    /**
     * @param amount
     * @param searchCardMode {@link com.pax.pay.trans.action.ActionSearchCard.SearchMode}
     */
    private void doSale(String amount, byte searchCardMode) {
        new SaleTrans(amount, searchCardMode, true, listener).execute();
    }

    private void onKeyOk() {
        String amount = CurrencyConverter.parse(edtAmount.getText().toString().trim()).toString();
        if (!"0".equals(amount)) {
            doSale(amount, (byte) -1);
        } else {
            UpdateAmount();
        }
    }

    private void onKeyCancel() {
        UpdateAmount();
    }

    private void onCheckLog() {
        if (SpManager.getControlSp().getInt(ControllerSp.CLEAR_LOG) == ControllerSp.Constant.YES) {
            if (DbManager.getTransDao().deleteAllTransData() && DbManager.getTransTotalDao().deleteAllTransTotal()) {
                SpManager.getControlSp().putInt(ControllerSp.CLEAR_LOG, ControllerSp.Constant.NO);
                SpManager.getControlSp().putInt(ControllerSp.BATCH_UP_STATUS, ControllerSp.Constant.WORKED);
            }
        }
    }

    private void onCheckInit() {
        if (SpManager.getControlSp().getBoolean(ControllerSp.IS_FIRST_RUN)) {
            Intent intent = new Intent(this, InitializeInputPwdActivity.class);
            startActivityForResult(intent, REQ_INITIALIZE);
        }
    }

    private void onSelfTest() {
        if (NEED_SELF_TEST.equals("true")) {
            Intent intent = new Intent(this, SelfTestActivity.class);
            startActivityForResult(intent, REQ_SELFTEST);
        }
    }

    public static final int REQ_INITIALIZE = 1;
    private static final int REQ_SELFTEST = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_INITIALIZE:
                break;
            case REQ_SELFTEST:
                NEED_SELF_TEST = "false";
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        // exit current app
        DialogUtils.showExitAppDialog(MainActivity.this);
        return true;
    }

    // AET-48
    private synchronized void UpdateAmount() {
        edtAmount.setText("");
    }

}
