/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-10
 * Module Author: xiawh
 * Description:
 *
 * ============================================================================
 */
package com.pax.settings.multiHostParam;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView.BufferType;

import com.pax.edc.R;
import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.component.Component;
import com.pax.pay.utils.TextValueWatcher;
import com.pax.pay.utils.ToastUtils;
import com.pax.pay.utils.Utils;
import com.pax.settings.BaseFragment;
import com.pax.settings.NewSpinnerAdapter;

import java.util.List;

public class AcquirerFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener {

    private Acquirer acquirer;

    private NewSpinnerAdapter<Acquirer> adapter;

    private ImageView imgCommParam;
    private EditText etTerminalId, etMerchantId, etNii, etBatch, etIp, etPort;
    private CheckBox isDefault, enableTrickFeed;

    //AET-63
    private boolean isFirst = true;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_acquirer_details;
    }

    @Override
    protected void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String acqName = bundle.getString(EUIParamKeys.ACQUIRER_NAME.toString());
            acquirer = DbManager.getAcqDao().findAcquirer(acqName);
        }

        List<Acquirer> listAcquirers = DbManager.getAcqDao().findAllAcquirers();
        if (acquirer == null && listAcquirers.size() > 0)
            acquirer = listAcquirers.get(0);

        adapter = new NewSpinnerAdapter<>(this.context);
        adapter.setListInfo(listAcquirers);
        adapter.setOnTextUpdateListener(new NewSpinnerAdapter.OnTextUpdateListener() {
            @Override
            public String onTextUpdate(final List<?> list, int position) {
                return ((Acquirer) list.get(position)).getName();
            }
        });

    }

    @Override
    protected void initView(View view) {
        Spinner spinner = (Spinner) view.findViewById(R.id.acquirer_list);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                Acquirer newAcquirer = adapter.getListInfo().get(pos);
                if (newAcquirer.getId() != acquirer.getId()) {
                    //AET-36
                    DbManager.getAcqDao().updateAcquirer(acquirer);
                    acquirer = newAcquirer;
                    updateItemsValue();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        if (adapter.getListInfo().size() > 0) {
            updateItems(view);
            updateItemsValue();
        } else {
            view.findViewById(R.id.acquirer_details).setVisibility(View.GONE);
        }
    }

    private void updateItems(View view) {
        isDefault = (CheckBox) view.findViewById(R.id.acquirer_is_default);
        isDefault.setOnCheckedChangeListener(AcquirerFragment.this);

        imgCommParam = (ImageView) view.findViewById(R.id.commParam_image);
        imgCommParam.setOnClickListener(AcquirerFragment.this);

        etTerminalId = (EditText) view.findViewById(R.id.terminal_id);
        etTerminalId.addTextChangedListener(new watcher(R.id.terminal_id));

        etMerchantId = (EditText) view.findViewById(R.id.merchant_id);
        etMerchantId.addTextChangedListener(new watcher(R.id.merchant_id));

        etNii = (EditText) view.findViewById(R.id.nii_acq);
        etNii.addTextChangedListener(new watcher(R.id.nii_acq));

        etBatch = (EditText) view.findViewById(R.id.batch_num);
        etBatch.addTextChangedListener(new watcher(R.id.batch_num));

        etIp = (EditText) view.findViewById(R.id.acq_ip);
        etIp.addTextChangedListener(new watcher(R.id.acq_ip));

        etPort = (EditText) view.findViewById(R.id.acq_ip_port);
        TextValueWatcher<Integer> textValueWatcher = new TextValueWatcher<>(0, 65535);
        textValueWatcher.setOnCompareListener(new TextValueWatcher.OnCompareListener() {
            @Override
            public boolean onCompare(String value, Object min, Object max) {
                int temp = Integer.parseInt(value);
                return temp >= (int) min && temp <= (int) max;
            }
        });
        textValueWatcher.setOnTextChangedListener(new TextValueWatcher.OnTextChangedListener() {
            @Override
            public void afterTextChanged(String value) {
                acquirer.setPort(Integer.parseInt(value));
            }
        });
        etPort.addTextChangedListener(textValueWatcher);

        enableTrickFeed = (CheckBox) view.findViewById(R.id.acquirer_disable_trick_feed);
        enableTrickFeed.setOnCheckedChangeListener(AcquirerFragment.this);
    }

    private void updateItemsValue() {
        isDefault.setChecked(acquirer != null && acquirer.getId() == AcqManager.getInstance().getCurAcq().getId());

        etTerminalId.setText(acquirer.getTerminalId(), BufferType.EDITABLE);

        etMerchantId.setText(acquirer.getMerchantId(), BufferType.EDITABLE);

        etNii.setText(acquirer.getNii(), BufferType.EDITABLE);

        String szBatchNo = Component.getPaddedNumber(acquirer.getCurrBatchNo(), 6);
        etBatch.setText(szBatchNo, BufferType.EDITABLE);

        etIp.setText(acquirer.getIp());

        etPort.setText(String.valueOf(acquirer.getPort()));

        enableTrickFeed.setChecked(acquirer.isDisableTrickFeed());

        //AET-63
        isFirst = false;
    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.commParam_image:
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.acquirer_disable_trick_feed:
                acquirer.setDisableTrickFeed(isChecked);
                break;
            case R.id.acquirer_is_default:
                if (isChecked && acquirer.getId() != AcqManager.getInstance().getCurAcq().getId())
                    AcqManager.getInstance().setCurAcq(acquirer);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        DbManager.getAcqDao().updateAcquirer(acquirer);
        //AET-36
        String curAcqName = AcqManager.getInstance().getCurAcq().getName();
        AcqManager.getInstance().setCurAcq(DbManager.getAcqDao().findAcquirer(curAcqName));
    }

    private class watcher implements TextWatcher {
        //private EditText id=null;
        final int id;

        watcher(int id) {
            this.id = id;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String temp = s.toString();
            switch (id) {
                case R.id.terminal_id:
                    acquirer.setTerminalId(temp);
                    break;
                case R.id.merchant_id:
                    acquirer.setMerchantId(temp);
                    break;
                case R.id.nii_acq:
                    acquirer.setNii(temp);
                    break;
                case R.id.batch_num:
                    //AET-63
                    if (!isFirst && DbManager.getTransDao().findAllTransData(acquirer).size() > 0) {
                        ToastUtils.showLong( R.string.has_trans_for_settle);
                    } else {
                        acquirer.setCurrBatchNo(Integer.parseInt(temp));
                    }
                    break;
                case R.id.acq_ip:
                    if (Utils.checkIp(temp))
                        acquirer.setIp(temp);
                default:
                    break;

            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            switch (id) {
                case R.id.terminal_id:
                    break;
                case R.id.merchant_id:
                    break;
                case R.id.nii_acq:
                    break;
                case R.id.batch_num:
                    break;
                case R.id.acq_ip:
                    break;
                default:
                    break;

            }
        }
    }

}
