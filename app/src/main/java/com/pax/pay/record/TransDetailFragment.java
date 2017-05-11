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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.AdjustTrans;
import com.pax.pay.trans.SaleVoidTrans;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.TimeConverter;
import com.tjerkw.slideexpandable.library.AbstractSlideExpandableListAdapter;
import com.tjerkw.slideexpandable.library.ActionSlideExpandableListView;

import java.util.Collections;
import java.util.List;

//TODO Kim need to be optimized cuz the list can have hundreds of records
public class TransDetailFragment extends Fragment implements ActionSlideExpandableListView.OnActionClickListener {
    private ActionSlideExpandableListView mListView;
    private RecordListAdapter mAdapter;

    private TextView noTransRecord;

    private RecordAsyncTask mRecordAsyncTask;

    private boolean supportDoTrans;
    private String acquirerName = "";

    public TransDetailFragment() {
    }

    public static TransDetailFragment newInstance(String acquirerName, boolean isSupportDoTrans) {
        TransDetailFragment f = new TransDetailFragment();
        Bundle b = new Bundle();
        b.putString(EUIParamKeys.ACQUIRER_NAME.toString(), acquirerName);
        b.putBoolean(EUIParamKeys.SUPPORT_DO_TRANS.toString(), isSupportDoTrans);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trans_detail_layout, container, false);
        mListView = (ActionSlideExpandableListView) view.findViewById(R.id.trans_list);
        mListView.setItemActionListener(this);
        noTransRecord = (TextView) view.findViewById(R.id.no_trans_record);
        acquirerName = getArguments().getString(EUIParamKeys.ACQUIRER_NAME.toString(), "");
        supportDoTrans = getArguments().getBoolean(EUIParamKeys.SUPPORT_DO_TRANS.toString(), true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRecordAsyncTask != null) {
            mRecordAsyncTask.cancel(true);
            //ActivityStack.getInstance().pop(); // why need this
        }
        mRecordAsyncTask = new RecordAsyncTask();
        mRecordAsyncTask.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRecordAsyncTask != null) {
            mRecordAsyncTask.cancel(true);
        }
        mRecordAsyncTask = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    // 使用Task异步加载数据填充ListView
    private class RecordAsyncTask extends AsyncTask<Void, Void, List<TransData>> {
        private List<TransData> mListItems;

        @Override
        protected List<TransData> doInBackground(Void... params) {
            Acquirer acquirer = DbManager.getAcqDao().findAcquirer(acquirerName);
            if (acquirer == null) {
                mListItems = null;
                return null;
            }
            mListItems = DbManager.getTransDao().findAllTransData(acquirer);
            return mListItems;
        }

        @Override
        protected void onPostExecute(List<TransData> result) {
            super.onPostExecute(result);
            if (mListItems == null) {
                mListView.setVisibility(View.GONE);
                noTransRecord.setVisibility(View.VISIBLE);
                return;
            }
            Collections.reverse(mListItems);// 把list倒序，使最新一条记录在最上
            if (mAdapter == null) {
                mAdapter = new RecordListAdapter(getActivity(), mListItems);
                mListView.setAdapter(mAdapter, new AbstractSlideExpandableListAdapter.OnItemExpandCollapseListener() {

                    @Override
                    public void onExpand(View itemView, int position) {
                        final TransData transData = (TransData) mAdapter.getItem(position);
                        updateValueTable(itemView, transData);
                    }

                    @Override
                    public void onCollapse(View itemView, int position) {

                    }
                });
                mListView.setItemActionListener(new ActionSlideExpandableListView.OnActionClickListener() {
                    @Override
                    public void onActionItemClick(View itemView, View clickedView, final int position) {
                        final TransData transData = (TransData) mAdapter.getItem(position);
                        switch (clickedView.getId()) {
                            case R.id.history_trans_action_reprint:
                                FinancialApplication.mApp.runInBackground(new Runnable() {
                                    @Override
                                    public void run() {
                                        Printer.printTransAgain(getActivity(), transData);
                                    }
                                });
                                break;
                            case R.id.history_trans_action_void:
                                // 消费撤销
                                new SaleVoidTrans(transData, new ATransaction.TransEndListener() {

                                    @Override
                                    public void onEnd(ActionResult result) {
                                        getActivity().finish();
                                    }
                                }).execute();
                                break;
                            case R.id.history_trans_action_adjust:
                                new AdjustTrans(transData, new ATransaction.TransEndListener() {

                                    @Override
                                    public void onEnd(ActionResult result) {
                                        getActivity().finish();
                                    }
                                }).execute();
                                break;
                            default:
                                break;
                        }
                    }
                }, R.id.history_trans_action_void, R.id.history_trans_action_adjust, R.id.history_trans_action_reprint);

            } else {
                mAdapter.notifyDataSetChanged();
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    @Override
    public void onActionItemClick(View itemView, View clickedView, int position) {

    }


    private void updateValueTable(View view, TransData transData) {

        TransData.ETransStatus temp = transData.getTransState();
        String state = "";
        // 联机交易
        if (transData.isOnlineTrans()) {

            if (temp.equals(TransData.ETransStatus.NORMAL)) {
                state = getString(R.string.state_normal);
            } else if (temp.equals(TransData.ETransStatus.VOIDED)) {
                state = getString(R.string.state_voided);
            } else if (temp.equals(TransData.ETransStatus.ADJUSTED)) {
                state = getString(R.string.state_adjusted);
            }

        } else {
            // 对于脱机交易，显示 已上送、未上送
            if (transData.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_SENT) {
                // true:脱机上送成功
                state = getString(R.string.state_uploaded);
            } else {
                state = getString(R.string.state_not_sent);
            }

            if (temp.equals(TransData.ETransStatus.ADJUSTED)) {
                state = getString(R.string.state_adjusted);
            }

        }

        String cardNo;
        // 卡号
        if (transData.getTransType() == ETransType.PREAUTH) {
            cardNo = transData.getPan();
        } else {
            cardNo = PanUtils.maskCardNo(transData.getPan(), transData.getIssuer().getPanMaskPattern());
            if (!transData.isOnlineTrans()) {
                cardNo = transData.getPan();
            }
        }

        String authCode = transData.getAuthCode();
        String refNo = transData.getRefNo();

        ((TextView) view.findViewById(R.id.history_detail_state)).setText(state);
        ((TextView) view.findViewById(R.id.history_detail_card_no)).setText(cardNo);
        ((TextView) view.findViewById(R.id.history_detail_auth_code)).setText(authCode != null ? authCode : "");
        ((TextView) view.findViewById(R.id.history_detail_ref_no)).setText(refNo != null ? refNo : "");

        view.findViewById(R.id.history_trans_action).setEnabled(supportDoTrans);

        if (transData.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_SENT) {
            view.findViewById(R.id.history_trans_action_void).setEnabled(true);
            view.findViewById(R.id.history_trans_action_adjust).setEnabled(transData.getTransType().isAdjustAllowed());
        } else if (transData.getTransState().equals(TransData.ETransStatus.NORMAL)
                || transData.getTransState().equals(TransData.ETransStatus.ADJUSTED)) {
            view.findViewById(R.id.history_trans_action_void).setEnabled(transData.getTransType().isVoidAllowed());
            view.findViewById(R.id.history_trans_action_adjust).setEnabled(transData.getTransType().isAdjustAllowed());
        } else {
            view.findViewById(R.id.history_trans_action_void).setEnabled(false);
            view.findViewById(R.id.history_trans_action_adjust).setEnabled(false);
        }
        view.findViewById(R.id.history_trans_action_reprint).setEnabled(supportDoTrans);
    }
}

class RecordListAdapter extends BaseAdapter {

    private Context context;
    private List<TransData> data;

    RecordListAdapter(Context context, List<TransData> list) {
        super();
        this.context = context;
        this.data = list;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.trans_item, parent, false);
        }
        TextView transTypeTv = BaseViewHolder.get(convertView, R.id.trans_type_tv);
        TextView transAmountTv = BaseViewHolder.get(convertView, R.id.trans_amount_tv);
        TextView transIssuerTv = BaseViewHolder.get(convertView, R.id.issuer_type_tv);
        TextView transNoTv = BaseViewHolder.get(convertView, R.id.trans_no_tv);
        TextView transDateTv = BaseViewHolder.get(convertView, R.id.trans_date_tv);

        TransData transData = data.get(position);
        ETransType transType = transData.getTransType();
        transTypeTv.setText(transType.getTransName());

        //AET-18
        String amount;

        if (!transData.getTransType().isSymbolNegative()) {
            amount = CurrencyConverter.convert(Long.parseLong(transData.getAmount()), transData.getCurrency());
            transAmountTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));
        } else {
            amount = CurrencyConverter.convert(0 - Long.parseLong(transData.getAmount()), transData.getCurrency()); //AET-18
            transAmountTv.setTextColor(context.getResources().getColor(R.color.success_stroke_color));
        }
        transAmountTv.setText(amount);

        transIssuerTv.setText(transData.getIssuer().getName());
        transNoTv.setText(Component.getPaddedNumber(transData.getTraceNo(), 6));

        String formattedDate = TimeConverter.convert(transData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                Constants.TIME_PATTERN_DISPLAY2);
        transDateTv.setText(formattedDate);
        return convertView;
    }

    private static class BaseViewHolder {
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {

            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();

            if (viewHolder == null) {
                viewHolder = new SparseArray<>();
                view.setTag(viewHolder);
            }

            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }

            return (T) childView;
        }
    }
}
