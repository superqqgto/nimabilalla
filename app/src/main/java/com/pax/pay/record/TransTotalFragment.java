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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.utils.CurrencyConverter;

public class TransTotalFragment extends Fragment {

    private TextView saleNumberTv;
    private TextView saleAmountTv;
    private TextView refundNumberTv;
    private TextView refundAmountTv;
    private TextView voidedSaleNumberTv;
    private TextView voidedSaleAmountTv;
    private TextView voidedRefundNumberTv;
    private TextView voidedRefundAmountTv;

    private String acquirerName = "";

    public TransTotalFragment() {
    }

    public static TransTotalFragment newInstance(String acquirerName) {
        TransTotalFragment f = new TransTotalFragment();
        Bundle b = new Bundle();
        b.putString(EUIParamKeys.ACQUIRER_NAME.toString(), acquirerName);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trans_total_layout, container, false);

        acquirerName = getArguments().getString(EUIParamKeys.ACQUIRER_NAME.toString(), null);

        saleNumberTv = (TextView) view.findViewById(R.id.sale_number);
        saleAmountTv = (TextView) view.findViewById(R.id.sale_amount);

        refundNumberTv = (TextView) view.findViewById(R.id.refund_number);
        refundAmountTv = (TextView) view.findViewById(R.id.refund_amount);

        voidedSaleNumberTv = (TextView) view.findViewById(R.id.voided_sale_number);
        voidedSaleAmountTv = (TextView) view.findViewById(R.id.voided_sale_amount);

        voidedRefundNumberTv = (TextView) view.findViewById(R.id.voided_refund_number);
        voidedRefundAmountTv = (TextView) view.findViewById(R.id.voided_refund_amount);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        initTables();
    }

    // 初始化表格
    private void initTables() {
        Acquirer acquirer = DbManager.getAcqDao().findAcquirer(acquirerName);
        if (acquirer == null) {
            return;
        }
        TransTotal total = DbManager.getTransTotalDao().CalcTotal(acquirer, null);
        String saleAmt = CurrencyConverter.convert(total.getSaleTotalAmt());
        //AET-18
        String refundAmt = CurrencyConverter.convert(0 - total.getRefundTotalAmt());
        String voidSaleAmt = CurrencyConverter.convert(0 - total.getSaleVoidTotalAmt());
        String voidRefundAmt = CurrencyConverter.convert(0 - total.getRefundVoidTotalAmt());

        saleNumberTv.setText(String.valueOf(total.getSaleTotalNum()));
        saleAmountTv.setText(saleAmt);

        refundNumberTv.setText(String.valueOf(total.getRefundTotalNum()));
        refundAmountTv.setText(refundAmt);

        voidedSaleNumberTv.setText(String.valueOf(total.getSaleVoidTotalNum()));
        voidedSaleAmountTv.setText(voidSaleAmt);

        voidedRefundNumberTv.setText(String.valueOf(total.getRefundVoidTotalNum()));
        voidedRefundAmountTv.setText(voidRefundAmt);

    }
}
