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
package com.pax.pay.record;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.BaseActivity;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.app.quickclick.QuickClickUtils;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.TimeConverter;
import com.pax.pay.utils.ToastUtils;
import com.pax.view.PagerSlidingTabStrip;
import com.pax.view.dialog.DialogUtils;
import com.pax.view.dialog.MenuPopupWindow;
import com.pax.view.dialog.MenuPopupWindow.ActionItem;
import com.pax.view.dialog.MenuPopupWindow.OnItemOnClickListener;

import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class TransQueryActivity extends BaseActivity implements OnItemOnClickListener {

    @BindView(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.header_title)
    TextView headerText;
    @BindView(R.id.search_btn)
    ImageView searchBtn;
    @BindView(R.id.print_btn)
    ImageView printBtn;

    private MenuPopupWindow popupWindow;
    private String[] titles;

    private TransDetailFragment detailFragment;
    private TransTotalFragment totalFragment;
    private String navTitle;
    private boolean supportDoTrans;

    private String acquirerName = "";

    @Override
    protected void loadParam() {
        titles = new String[]{getString(R.string.history_detail), getString(R.string.history_total)};
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        supportDoTrans = getIntent().getBooleanExtra(EUIParamKeys.SUPPORT_DO_TRANS.toString(), true);
    }

    private void initMenuPopupWindow() {
        // 实例化标题栏弹窗
        popupWindow = new MenuPopupWindow(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // 给标题栏弹窗添加子类
        popupWindow.addAction(new ActionItem(this, R.string.history_menu_print_trans_last, R.drawable.i1));
        popupWindow.addAction(new ActionItem(this, R.string.history_menu_print_trans_detail, R.drawable.i2));
        popupWindow.addAction(new ActionItem(this, R.string.history_menu_print_trans_total, R.drawable.i3));
        popupWindow.addAction(new ActionItem(this, R.string.history_menu_print_last_total, R.drawable.i4));
    }

    public class MyAdapter extends FragmentPagerAdapter {
        String[] _titles;

        public MyAdapter(FragmentManager fm, String[] titles) {
            super(fm);
            _titles = titles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return _titles[position];
        }

        @Override
        public int getCount() {
            return _titles.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (detailFragment == null) {
                        detailFragment = TransDetailFragment.newInstance(acquirerName, supportDoTrans);
                    }
                    return detailFragment;
                case 1:
                    if (totalFragment == null) {
                        totalFragment = TransTotalFragment.newInstance(acquirerName);
                    }
                    return totalFragment;
                default:
                    return null;
            }
        }
    }

    private void selectAcquirer() {
        List<Acquirer> list = DbManager.getAcqDao().findAllAcquirers();
        final String[] acqNames = new String[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            acqNames[i] = list.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.acq_select_hint));

        builder.setSingleChoiceItems(acqNames, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                acquirerName = acqNames[arg1];
                FinancialApplication.mApp.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pager.setAdapter(new MyAdapter(getSupportFragmentManager(), titles));
                        tabs.setViewPager(pager);
                    }
                });

                arg0.dismiss();
            }
        });

        builder.setPositiveButton(getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        setResult(100);
                        finish();
                        arg0.dismiss();
                    }
                });
        builder.setCancelable(false);
        builder.create().show();
    }

    @OnClick({R.id.header_back, R.id.search_btn, R.id.print_btn})
    public void onViewClicked(View view) {

        if (QuickClickUtils.isFastDoubleClick(view)) {
            return;
        }

        switch (view.getId()) {
            case R.id.header_back:
                setResult(100);
                finish();
                break;
            case R.id.print_btn:
                popupWindow.show(view);
                break;
            case R.id.search_btn:
                queryTransRecordByTransNo();
                break;
            default:
                break;
        }

    }

    /**
     * 根据流水号查询交易记录
     */
    private void queryTransRecordByTransNo() {

        ActionInputTransData inputTransDataAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(getString(R.string.trans_history))
                        .setInputLine1(getString(R.string.prompt_input_transno), EInputType.NUM, 6, false);
            }

        }, 1);

        inputTransDataAction.setEndListener(new AAction.ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {

                if (result.getRet() != TransResult.SUCC) {
                    ActivityStack.getInstance().pop();
                    return;
                }

                String content = (String) result.getData();
                if (content == null || content.length() == 0) {
                    ToastUtils.showShort(R.string.please_input_again);
                    return;
                }
                long transNo = Long.parseLong(content);
                TransData transData = DbManager.getTransDao().findTransDataByTraceNo(transNo);

                if (transData == null) {
                    ToastUtils.showShort(R.string.err_no_orig_trans);
                    return;
                }

                final LinkedHashMap<String, String> map = prepareValuesForDisp(transData);

                ActionDispTransDetail dispTransDetailAction = new ActionDispTransDetail(
                        new AAction.ActionStartListener() {

                            @Override
                            public void onStart(AAction action) {
                                ((ActionDispTransDetail) action).setParam(getString(R.string.trans_history), map);
                            }
                        });
                dispTransDetailAction.setEndListener(new AAction.ActionEndListener() {

                    @Override
                    public void onEnd(AAction action, ActionResult result) {
                        ActivityStack.getInstance().popTo(TransQueryActivity.this);
                    }
                });

                dispTransDetailAction.execute();
            }
        });

        inputTransDataAction.execute();

    }

    @SuppressLint("SimpleDateFormat")
    private LinkedHashMap<String, String> prepareValuesForDisp(TransData transData) {

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        ETransType transType = transData.getTransType();
        String amount;
        if (transType.isSymbolNegative()) {
            amount = CurrencyConverter.convert(0 - Long.parseLong(transData.getAmount()), transData.getCurrency());
        } else {
            amount = CurrencyConverter.convert(Long.parseLong(transData.getAmount()), transData.getCurrency());
        }

        // 日期时间
        String formattedDate = TimeConverter.convert(transData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                Constants.TIME_PATTERN_DISPLAY);

        map.put(getString(R.string.history_detail_type), transType.getTransName());
        map.put(getString(R.string.history_detail_amount), amount);
        map.put(getString(R.string.history_detail_card_no), PanUtils.maskCardNo(transData.getPan(), transData.getIssuer().getPanMaskPattern()));
        map.put(getString(R.string.history_detail_auth_code), transData.getAuthCode());
        map.put(getString(R.string.history_detail_ref_no), transData.getRefNo());
        map.put(getString(R.string.history_detail_trace_no), Component.getPaddedNumber(transData.getTraceNo(), 6));
        map.put(getString(R.string.dateTime), formattedDate);
        return map;
    }

    @Override
    public void onItemClick(ActionItem item, int position) {

        switch (item.getId()) {
            case R.string.history_menu_print_trans_last:
                FinancialApplication.mApp.runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        int result = Printer.printLastTrans(TransQueryActivity.this);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this,
                                    getString(R.string.transType_print), getString(R.string.err_no_trans),
                                    null, Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                });
                break;
            case R.string.history_menu_print_trans_detail:
                FinancialApplication.mApp.runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        //AET-112
                        final Acquirer acquirer = DbManager.getAcqDao().findAcquirer(acquirerName);
                        int result = Printer.printTransDetail(getString(R.string.print_history_detail),
                                TransQueryActivity.this, acquirer);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this,
                                    getString(R.string.transType_print), getString(R.string.err_no_trans),
                                    null, Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                });
                break;
            case R.string.history_menu_print_trans_total:
                FinancialApplication.mApp.runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        final Acquirer acquirer = DbManager.getAcqDao().findAcquirer(acquirerName);
                        Printer.printTransTotal(TransQueryActivity.this, acquirer);
                    }
                });
                break;
            case R.string.history_menu_print_last_total:
                FinancialApplication.mApp.runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        int result = Printer.printLastBatch(TransQueryActivity.this);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this,
                                    getString(R.string.transType_print), getString(R.string.err_no_trans),
                                    null, Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                });
                break;
            default:
                break;
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_trans_query_layout;
    }

    @Override
    protected void initViews() {

        headerText.setText(navTitle);
        searchBtn.setVisibility(View.VISIBLE);
        if (supportDoTrans) {
            printBtn.setVisibility(View.VISIBLE);
        }

        if (supportDoTrans) {
            initMenuPopupWindow();
        }
        selectAcquirer();
    }

    @Override
    protected void setListeners() {
        if (supportDoTrans) {
            popupWindow.setItemOnClickListener(this);
        }
    }

}
