/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-29
 * Module Author: caowb
 * Description:
 *
 * ============================================================================
 */
package com.pax.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.activity.SelectAcqActivity;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.TickTimer;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.BaseFragmentV4;
import com.tjerkw.slideexpandable.library.AbstractSlideExpandableListAdapter;
import com.tjerkw.slideexpandable.library.ActionSlideExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectAcqFragment extends BaseFragmentV4 {

    private static final String ACQ_NAME = "acq_name";

    private CheckBox mCheck;
    private ActionSlideExpandableListView mList;
    private Button mSettle;
    private TextView titleTv;
    private ImageView backBt;

    private AcquirerListAdapter acquirerListAdapter;
    private ArrayList<String> list;

    private TickTimer tickTimer = new TickTimer(new TickTimer.OnTickTimerListener() {
        @Override
        public void onTick(long leftTime) {
            Log.i("TAG", "onTick:" + leftTime);
        }

        @Override
        public void onFinish() {
            ((SelectAcqActivity) getActivity()).finish(new ActionResult(TransResult.ERR_TIMEOUT, null));
        }
    });

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_select_acquirer;
    }

    @Override
    protected void initData() {
        //here acquirers should be loaded from database
        Bundle bundle = getArguments();
        //AET-39
        if (bundle != null) {
            list = bundle.getStringArrayList(Constants.ACQUIRER_NAME);
        }
        if (list == null)
            list = new ArrayList<>();

        List<Acquirer> list = DbManager.getAcqDao().findAllAcquirers();
        ArrayList<HashMap<String, String>> myListArray = new ArrayList<>();

        for (int i = 0; i < list.size(); ++i) {
            HashMap<String, String> map = new HashMap<>();
            map.put(ACQ_NAME, list.get(i).getName());
            myListArray.add(map);
        }
        acquirerListAdapter = new AcquirerListAdapter(getActivity(), myListArray);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tickTimer.stop();
    }

    @Override
    protected void initView(View view) {
        titleTv = (TextView) view.findViewById(R.id.header_title);
        backBt = (ImageView) view.findViewById(R.id.header_back);
        mCheck = (CheckBox) view.findViewById(R.id.item_select_acq_check);
        mList = (ActionSlideExpandableListView) view.findViewById(R.id.select_acq_list);
        mSettle = (Button) view.findViewById(R.id.select_acq_settle);
        backBt.setOnClickListener(this);
        confirmBtnChange();
        mSettle.setOnClickListener(this);

        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                tickTimer.start();
                return false;
            }
        });
        mList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                tickTimer.start();
                return false;
            }
        });
        mList.setAdapter(acquirerListAdapter, new AbstractSlideExpandableListAdapter.OnItemExpandCollapseListener() {

            @Override
            public void onExpand(View itemView, int position) {
                updateValueTable(itemView, position);
            }

            @Override
            public void onCollapse(View itemView, int position) {
            }
        });
        mList.setItemActionListener(new ActionSlideExpandableListView.OnActionClickListener() {
            @Override
            public void onActionItemClick(View itemView, View clickedView, final int position) {
                switch (clickedView.getId()) {
                    case R.id.settle_confirm:
                        list.clear();
                        list.add(findAcquirer(position));
                        tickTimer.stop();
                        ((SelectAcqActivity) getActivity()).finish(new ActionResult(TransResult.SUCC, list));
                        break;
                    default:
                        break;
                }
            }
        }, R.id.settle_confirm);

        mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tickTimer.start();
                //AET-39
                if (isChecked) {
                    List<Acquirer> acqList = DbManager.getAcqDao().findAllAcquirers();
                    for (Acquirer acquirer : acqList) {
                        if (!list.contains(acquirer.getName())) {
                            list.add(acquirer.getName());
                        }
                    }
                } else {
                    if (list.size() == DbManager.getAcqDao().findAllAcquirers().size()) {
                        list.clear();
                    }
                }
                confirmBtnChange();
                acquirerListAdapter.notifyDataSetChanged();
            }
        });

        //AET-39
        if (list != null && list.size() == DbManager.getAcqDao().findAllAcquirers().size()) {
            mCheck.setChecked(true);
        }

        titleTv.setText(getActivity().getString(R.string.settle_select_acquirer));
        tickTimer.start();
    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                tickTimer.stop();
                ((SelectAcqActivity) getActivity()).finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                break;
            case R.id.select_acq_settle:
                if (list.size() == 0) {
                    ToastUtils.showShort(R.string.err_settle_select_acq);
                    return;
                }
                tickTimer.stop();
                ((SelectAcqActivity) getActivity()).finish(new ActionResult(TransResult.SUCC, list));
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private String findAcquirer(int position) {
        return ((HashMap<String, String>) acquirerListAdapter.getItem(position)).get(ACQ_NAME);
    }

    private void updateValueTable(View view, int position) {
        String acquirerName = findAcquirer(position);
        Acquirer acquirer = DbManager.getAcqDao().findAcquirer(acquirerName);
        TransTotal total = DbManager.getTransTotalDao().CalcTotal(acquirer, null);

        view.findViewById(R.id.settle_acquirer_name).setVisibility(View.GONE);
        TextView acqName = (TextView) view.findViewById(R.id.settle_acquirer_name);
        TextView merchantName = (TextView) view.findViewById(R.id.settle_merchant_name);
        TextView merchantId = (TextView) view.findViewById(R.id.settle_merchant_id);
        TextView terminalId = (TextView) view.findViewById(R.id.settle_terminal_id);
        TextView batchNo = (TextView) view.findViewById(R.id.settle_batch_num);

        acqName.setText(acquirer.getName());
        merchantName.setText(getString(R.string.settle_merchant_name));
        merchantId.setText(acquirer.getMerchantId());
        terminalId.setText(acquirer.getTerminalId());
        batchNo.setText(String.valueOf(acquirer.getCurrBatchNo()));

        String saleAmt = CurrencyConverter.convert(total.getSaleTotalAmt());
        //AET-18
        String refundAmt = CurrencyConverter.convert(0 - total.getRefundTotalAmt());
        String voidSaleAmt = CurrencyConverter.convert(0 - total.getSaleVoidTotalAmt());
        String voidRefundAmt = CurrencyConverter.convert(0 - total.getRefundVoidTotalAmt());

        ((TextView) view.findViewById(R.id.settle_sale_total_sum)).setText(String.valueOf(total.getSaleTotalNum()));
        ((TextView) view.findViewById(R.id.settle_sale_total_amount)).setText(saleAmt);
        ((TextView) view.findViewById(R.id.settle_refund_total_sum)).setText(String.valueOf(total.getRefundTotalNum()));
        ((TextView) view.findViewById(R.id.settle_refund_total_amount)).setText(refundAmt);

        ((TextView) view.findViewById(R.id.settle_void_sale_total_sum)).setText(String.valueOf(total.getSaleVoidTotalNum()));
        ((TextView) view.findViewById(R.id.settle_void_sale_total_amount)).setText(voidSaleAmt);
        ((TextView) view.findViewById(R.id.settle_void_refund_total_sum)).setText(String.valueOf(total.getRefundVoidTotalNum()));
        ((TextView) view.findViewById(R.id.settle_void_refund_total_amount)).setText(voidRefundAmt);
    }

    private class AcquirerListAdapter extends BaseAdapter {

        private LayoutInflater mContainer;
        private ArrayList<HashMap<String, String>> mList;

        public AcquirerListAdapter(Context context, ArrayList<HashMap<String, String>> list) {
            mContainer = LayoutInflater.from(context);
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            final int pos = position;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mContainer.inflate(R.layout.selectacq_item, parent, false);
                holder.textView = (TextView) convertView.findViewById(R.id.expandable_toggle_button);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.item_select_acq_check);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            holder.textView.setText(mList.get(position).get(ACQ_NAME));
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    tickTimer.start();
                    Log.d("SelectAcq", "onCheckedChanged  " + pos);
                    if (isChecked) {
                        if (!list.contains(mList.get(pos).get(ACQ_NAME)))
                            list.add(mList.get(pos).get(ACQ_NAME));
                    } else {
                        list.remove(mList.get(pos).get(ACQ_NAME));
                    }
                    confirmBtnChange();
                    //AET-39
                    mCheck.setChecked(list.size() == DbManager.getAcqDao().findAllAcquirers().size());
                }
            });

            //AET-39
            holder.checkBox.setChecked(list.contains(mList.get(pos).get(ACQ_NAME)));
            return convertView;
        }

        final class ViewHolder {
            TextView textView;
            CheckBox checkBox;
        }
    }

    // AET-114
    private void confirmBtnChange() {
        mSettle.setEnabled(list.size() > 0);
    }
}
