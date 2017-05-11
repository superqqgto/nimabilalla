/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-29
 * Module Auth: xiawh
 * Description:
 *
 * ============================================================================
 */
package com.pax.settings.multiHostParam;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.base.Issuer;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.EnterAmountTextWatcher;
import com.pax.pay.utils.TextValueWatcher;
import com.pax.settings.BaseFragment;
import com.pax.settings.NewSpinnerAdapter;
import com.pax.view.keyboard.CustomKeyboardEditText;

import java.util.List;

public class IssuerParamFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener {

    private Issuer issuer;
    private NewSpinnerAdapter<Issuer> adapter;

    private CustomKeyboardEditText floorLimit;
    private EditText adjustPercent;
    private CheckBox pinRequired, enablePrint, checkPan, checkExpiry, enableManualPan, enableOffline, enableAdjust,
            enableExpiry;

    @Override
    protected int getLayoutId(){
        return R.layout.fragment_issuer_details;
    }

    @Override
    protected void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String issuerName = bundle.getString(EUIParamKeys.ISSUER_NAME.toString());
            issuer = DbManager.getAcqDao().findIssuer(issuerName);
        }

        List<Issuer> listIssuers = DbManager.getAcqDao().findAllIssuers();
        if (issuer == null && listIssuers.size() > 0)
            issuer = listIssuers.get(0);

        adapter = new NewSpinnerAdapter<>(this.context);
        adapter.setListInfo(listIssuers);
        adapter.setOnTextUpdateListener(new NewSpinnerAdapter.OnTextUpdateListener() {
            @Override
            public String onTextUpdate(final List<?> list, int position) {
                return ((Issuer) list.get(position)).getName();
            }
        });
    }

    @Override
    protected void initView(View view) {
        Spinner spinner = (Spinner) view.findViewById(R.id.issuer_list);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                Issuer newIssuer = adapter.getListInfo().get(pos);
                if (newIssuer.getId() != issuer.getId()) {
                    //AET-36
                    DbManager.getAcqDao().updateIssuer(issuer);
                    issuer = newIssuer;
                    watcherFloorLimit.setFloorLimit(true);
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
            view.findViewById(R.id.issuer_details).setVisibility(View.GONE);
        }
    }

   private EnterAmountTextWatcher watcherFloorLimit = new EnterAmountTextWatcher(true);

    private void updateItems(View view) {
        // AET-49
        floorLimit = (CustomKeyboardEditText) view.findViewById(R.id.issuer_floor_limit);
        watcherFloorLimit.setAmount(0L, 0L);
        watcherFloorLimit.setOnTipListener(new EnterAmountTextWatcher.OnTipListener() {
            @Override
            public void onUpdateTipListener(long baseAmount, long tipAmount) {
                issuer.setFloorLimit(tipAmount);
            }

            @Override
            public boolean onVerifyTipListener(long baseAmount, long tipAmount) {
                return true;
            }
        });
        floorLimit.addTextChangedListener(watcherFloorLimit);

        enableAdjust = (CheckBox) view.findViewById(R.id.issuer_enable_adjust);
        enableAdjust.setOnCheckedChangeListener(this);

        adjustPercent = (EditText) view.findViewById(R.id.issuer_adjust_percent);
        TextValueWatcher<Float> textValueWatcher = new TextValueWatcher<>(0.0f, 100.0f);
        textValueWatcher.setOnCompareListener(new TextValueWatcher.OnCompareListener() {
            @Override
            public boolean onCompare(String value, Object min, Object max) {
                float temp = Float.parseFloat(value);
                return temp >= (float) min && temp <= (float) max;
            }
        });
        textValueWatcher.setOnTextChangedListener(new TextValueWatcher.OnTextChangedListener() {
            @Override
            public void afterTextChanged(String value) {
                issuer.setAdjustPercent(Float.parseFloat(value));
            }
        });

        adjustPercent.addTextChangedListener(textValueWatcher);

        enableOffline = (CheckBox) view.findViewById(R.id.issuer_enable_offline);
        enableOffline.setOnCheckedChangeListener(this);

        enableExpiry = (CheckBox) view.findViewById(R.id.issuer_enable_expiry);
        enableExpiry.setOnCheckedChangeListener(this);

        enableManualPan = (CheckBox) view.findViewById(R.id.issuer_enable_manualPan);
        enableManualPan.setOnCheckedChangeListener(this);

        checkExpiry = (CheckBox) view.findViewById(R.id.issuer_check_expiry);
        checkExpiry.setOnCheckedChangeListener(this);

        checkPan = (CheckBox) view.findViewById(R.id.issuer_check_pan);
        checkPan.setOnCheckedChangeListener(this);

        enablePrint = (CheckBox) view.findViewById(R.id.issuer_enable_print);
        enablePrint.setOnCheckedChangeListener(this);

        pinRequired = (CheckBox) view.findViewById(R.id.issuer_pin_required);
        pinRequired.setOnCheckedChangeListener(this);
    }

    private void updateItemsValue() {
        watcherFloorLimit.setAmount(0, issuer.getFloorLimit()); // AET-49
        floorLimit.setText(CurrencyConverter.convert(issuer.getFloorLimit()));

        enableAdjust.setChecked(issuer.isEnableAdjust());

        adjustPercent.setText(String.valueOf(issuer.getAdjustPercent()));
        adjustPercent.setEnabled(enableAdjust.isChecked());

        enableOffline.setChecked(issuer.isEnableOffline());

        enableExpiry.setChecked(issuer.isAllowExpiry());

        enableManualPan.setChecked(issuer.isAllowManualPan());

        checkExpiry.setChecked(issuer.isAllowCheckExpiry());

        checkPan.setChecked(issuer.isAllowCheckPanMod10());

        enablePrint.setChecked(issuer.isAllowPrint());

        pinRequired.setChecked(issuer.isRequirePIN());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.issuer_enable_adjust:
                issuer.setEnableAdjust(isChecked);
                break;
            case R.id.issuer_enable_offline:
                issuer.setEnableOffline(isChecked);
                break;
            case R.id.issuer_enable_expiry:
                issuer.setAllowExpiry(isChecked);
                break;
            case R.id.issuer_enable_manualPan:
                issuer.setAllowManualPan(isChecked);
                break;
            case R.id.issuer_check_expiry:
                issuer.setAllowCheckExpiry(isChecked);
                break;
            case R.id.issuer_check_pan:
                issuer.setAllowCheckPanMod10(isChecked);
                break;
            case R.id.issuer_enable_print:
                issuer.setAllowPrint(isChecked);
                break;
            case R.id.issuer_pin_required:
                issuer.setRequirePIN(isChecked);
                break;

            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        DbManager.getAcqDao().updateIssuer(issuer);
    }

}
