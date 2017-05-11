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

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.manager.sp.ControllerSp;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.app.quickclick.QuickClickUtils;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.record.Printer;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;
import com.pax.view.dialog.DialogUtils;

import java.util.ArrayList;

public class SettleActivity extends BaseActivityWithTickForAction {

    private static final String TAG = SettleActivity.class.getSimpleName();
    private TextView headerText;
    private ImageView backBtn;
    private Button settleBtn;

    private ArrayList<String> selectAcqs = new ArrayList<>();
    private TransTotal total;
    private String navTitle;
    private boolean navBack;

    private TextView acquirerName;
    private TextView merchantName;
    private TextView merchantId;
    private TextView terminalId;
    private TextView batchNo;

    private Acquirer acquirer;

    private int currentAcqPosition = 0;

    //AET-41
    private String acquirer_def;

    final ConditionVariable cv = new ConditionVariable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (SpManager.getControlSp().getInt(ControllerSp.BATCH_UP_STATUS) == ControllerSp.Constant.BATCH_UP) {
        settleBtn.setVisibility(View.INVISIBLE);
        Log.e(TAG, "onCreate: before performClick" );
        //fixme linzhao
        settleBtn.performClick();
//        }
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
//        titles = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString());
        selectAcqs = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString());
//        total = (TransTotal) bundle.getSerializable(EUIParamKeys.CONTENT.toString());
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        //AET-41
        acquirer_def = AcqManager.getInstance().getCurAcq().getName();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_settle_layout;
    }

    @Override
    protected void initViews() {
        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);
        backBtn = (ImageView) findViewById(R.id.header_back);

        acquirerName = (TextView) findViewById(R.id.settle_acquirer_name);
        merchantName = (TextView) findViewById(R.id.settle_merchant_name);
        merchantId = (TextView) findViewById(R.id.settle_merchant_id);
        terminalId = (TextView) findViewById(R.id.settle_terminal_id);
        batchNo = (TextView) findViewById(R.id.settle_batch_num);

        setCurrAcquirerContent();
        settleBtn = (Button) findViewById(R.id.settle_confirm);

    }

    public void setCurrAcquirerContent() {
        String currAcquirer = selectAcqs.get(currentAcqPosition);
        acquirer = DbManager.getAcqDao().findAcquirer(currAcquirer);
        //set current acquirer,settle print need it
        AcqManager.getInstance().setCurAcq(acquirer);

        this.acquirerName.setText(currAcquirer);
        //AET-39
        merchantName.setText(SpManager.getSysParamSp().get(SysParamSp.EDC_MERCHANT_NAME_EN));
        merchantId.setText(acquirer.getMerchantId());
        terminalId.setText(acquirer.getTerminalId());
        batchNo.setText(String.valueOf(acquirer.getCurrBatchNo()));

        total = DbManager.getTransTotalDao().CalcTotal(acquirer, total);
        String saleAmt = CurrencyConverter.convert(total.getSaleTotalAmt());
        //AET-18
        String refundAmt = CurrencyConverter.convert(0 - total.getRefundTotalAmt());
        String voidSaleAmt = CurrencyConverter.convert(0 - total.getSaleVoidTotalAmt());
        String voidRefundAmt = CurrencyConverter.convert(0 - total.getRefundVoidTotalAmt());

        ((TextView) findViewById(R.id.settle_sale_total_sum)).setText(String.valueOf(total.getSaleTotalNum()));
        ((TextView) findViewById(R.id.settle_sale_total_amount)).setText(saleAmt);
        ((TextView) findViewById(R.id.settle_refund_total_sum)).setText(String.valueOf(total.getRefundTotalNum()));
        ((TextView) findViewById(R.id.settle_refund_total_amount)).setText(refundAmt);

        ((TextView) findViewById(R.id.settle_void_sale_total_sum)).setText(String.valueOf(total.getSaleVoidTotalNum()));
        ((TextView) findViewById(R.id.settle_void_sale_total_amount)).setText(voidSaleAmt);
        ((TextView) findViewById(R.id.settle_void_refund_total_sum)).setText(String.valueOf(total.getRefundVoidTotalNum()));
        ((TextView) findViewById(R.id.settle_void_refund_total_amount)).setText(voidRefundAmt);

        cv.open();
    }

    @Override
    protected void setListeners() {
        if (!navBack) {
            backBtn.setVisibility(View.GONE);
        } else {
            backBtn.setOnClickListener(this);
        }

        settleBtn.setOnClickListener(this);

    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                break;
            case R.id.settle_confirm:
                Log.e(TAG, "onClickProtected: R.id.settle_confirm " );
                // 进入结算流程时间太长， 停止定时器
                tickTimer.stop();
                if (QuickClickUtils.isFastDoubleClick(v)) {
                    Log.e(TAG, "onClickProtected isFastDoubleClick " );
                    return;
                }

                FinancialApplication.mApp.runInBackground(new Runnable() {

                    @Override
                    public void run() {
                        Log.e(TAG, "run thread " );
                        int ret;
                        for (int i = 0; i < selectAcqs.size(); i++) {
                            TransProcessListenerImpl transProcessListenerImpl = new TransProcessListenerImpl(
                                    SettleActivity.this);
                            //check if zero total AET-75
                            if (total.getSaleTotalNum() == 0 && total.getRefundTotalNum() == 0 &&
                                    total.getSaleVoidTotalNum() == 0 && total.getRefundVoidTotalNum() == 0
                                    && total.getOfflineTotalNum() == 0) {
                                currentAcqPosition++;
                                if (currentAcqPosition < selectAcqs.size()) {
                                    onResetView();
                                    cv.close();
                                    cv.block();
                                }
                                continue;
                            }
                            // 结算
                            ret = TransOnline.settle(total, transProcessListenerImpl);
                            transProcessListenerImpl.onHideProgress();
                            if (ret != TransResult.SUCC && ret != TransResult.SUCC_NOREQ_BATCH) {
                                finish(new ActionResult(ret, null));
                                return;
                            }
                            // 记上批总计，置清除交易记录标志
                            total.setAcquirer(acquirer);
                            total.setMerchantID(acquirer.getMerchantId());
                            total.setTerminalID(acquirer.getTerminalId());
                            total.setBatchNo(acquirer.getCurrBatchNo());
                            total.setDateTime(Device.getTime(Constants.TIME_PATTERN_TRANS));
                            total.setClosed(true);
                            DbManager.getTransTotalDao().insertTransTotal(total);
                            // 打印结算单
                            Printer.printSettle(SettleActivity.this, getString(R.string.history_total), total);

                            // 打印明细
                            printDetail(false);
                            // 打印失败明细
                            printDetail(true);

                            // 批上送结算,将批上送断点赋值为0
                            SpManager.getControlSp().putInt(ControllerSp.BATCH_UP_STATUS, ControllerSp.Constant.WORKED);
                            // 清除交易流水
                            if (DbManager.getTransDao().deleteAllTransData(AcqManager.getInstance().getCurAcq())) {
                                // 删除失败交易文件， fixme
                                // 删除电子签名文件，fixme
                                Component.incBatchNo();
                            }

                            currentAcqPosition++;
                            //AET-31
                            if (currentAcqPosition < selectAcqs.size()) {
                                //AET-37
                                onResetView();
                                cv.close();
                                cv.block();
                            }
                        }

                        //AET-41
                        AcqManager.getInstance().setCurAcq(DbManager.getAcqDao().findAcquirer(acquirer_def));
                        finish(new ActionResult(TransResult.SUCC, null));
                    }
                });

                break;

            default:
                break;
        }
    }

    private void onResetView() {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCurrAcquirerContent();
            }
        });
    }

    private void printDetail(final boolean isFailDetail) {
        final ConditionVariable cv2 = new ConditionVariable();
        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                final CustomAlertDialog dialog = new CustomAlertDialog(SettleActivity.this,
                        CustomAlertDialog.IMAGE_TYPE);
                String info = getString(R.string.settle_print_detail_or_not);
                if (isFailDetail) {
                    info = getString(R.string.settle_print_fail_detail_or_not);
                }
                //AET-76
                dialog.setTimeout(30);
                dialog.setContentText(info);
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() { // AET-77
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                            cv2.open();
                            return true;
                        }
                        return false;
                    }
                });
                dialog.setCancelClickListener(new OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        dialog.dismiss();
                        cv2.open();
                    }
                });
                dialog.setConfirmClickListener(new OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        dialog.dismiss();
                        FinancialApplication.mApp.runInBackground(new Runnable() {

                            @Override
                            public void run() {

                                int result = 0;
                                if (isFailDetail) {
                                    // 打印失败交易明细
                                    result = Printer.printFailDetail(getString(R.string.print_offline_send_failed),
                                            SettleActivity.this);
                                } else {
                                    // 打印交易明细
                                    result = Printer.printTransDetail(getString(R.string.print_settle_detail),
                                            SettleActivity.this, acquirer);
                                }
                                if (result != TransResult.SUCC) {
                                    DialogUtils.showErrMessage(SettleActivity.this,
                                            getString(R.string.transType_print),
                                            getString(R.string.err_no_trans), new OnDismissListener() {

                                                @Override
                                                public void onDismiss(DialogInterface arg0) {
                                                    cv2.open();
                                                }
                                            }, Constants.FAILED_DIALOG_SHOW_TIME);

                                } else {
                                    cv2.open();
                                }

                            }
                        });

                    }
                });

                dialog.show();
                dialog.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic19));
            }
        });

        cv2.block();
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

}
