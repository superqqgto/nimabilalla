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

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputType;

import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.MainActivity;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.ToastUtils;
import com.pax.pay.utils.Utils;
import com.pax.view.widget.BaseWidget;

import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class EDCFragment extends PreferenceFragment {

    private static CharSequence[] entries;
    private static CharSequence[] entryValues;
    private static boolean isFirst = true;

    // AET-116
    private static void updateEntries() {
        Map<String, String> allEntries = new TreeMap<>();
        List<Locale> locales = CurrencyConverter.getSupportedLocale();
        for (Locale i : locales) {
            try {
                Currency currency = Currency.getInstance(i);
                LogUtils.i("TAG", i.getISO3Country() + "  " + i.getDisplayName(Locale.US) + " " + currency.getDisplayName(Locale.US));
                allEntries.put(i.getDisplayName(Locale.US) + " " + currency.getDisplayName(), i.getDisplayName(Locale.US));
            } catch (IllegalArgumentException e){
                //e.printStackTrace();
            }
        }
        entries = allEntries.keySet().toArray(new CharSequence[allEntries.size()]);
        entryValues = allEntries.values().toArray(new CharSequence[allEntries.size()]);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFirst = true;
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        updateEntries();
        addPreferencesFromResource(R.xml.edc_para_pref);

        ListPreference listPreference = (ListPreference) getPreferenceManager().findPreference(SysParamSp.EDC_CURRENCY_LIST);
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entryValues);

        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_MERCHANT_NAME_EN));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_MERCHANT_ADDRESS));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_CURRENCY_LIST));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_PED_MODE));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_RECEIPT_NUM));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_TRACE_NO));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_SUPPORT_TIP));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_SUPPORT_KEYIN));
//        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_INVOICE_NUM));
//        bindPreferenceSummaryToValue(findPreference(SysParamSp.AUTH_CODE_MODE));//Zac
//        bindPreferenceSummaryToValue(findPreference(SysParamSp.MOTO_FLOOR_LIMIT));//Zac
        bindPreferenceSummaryToValue(findPreference(SysParamSp.SUPPORT_DCC));//Zac
        bindPreferenceSummaryToValue(findPreference(SysParamSp.SUPPORT_MOTOSALE));//Zac
//        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_CONNECT_TIMEOUT));

        bindPreferenceSummaryToValue(findPreference(SysParamSp.SUPPORT_USER_AGREEMENT));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_ENABLE_PAPERLESS));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_SMTP_HOST));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_SMTP_PORT));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_SMTP_USERNAME));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_SMTP_PASSWORD));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_SMTP_ENABLE_SSL));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_SMTP_SSL_PORT));
        bindPreferenceSummaryToValue(findPreference(SysParamSp.EDC_SMTP_FROM));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                if (SysParamSp.EDC_CURRENCY_LIST.equals(preference.getKey())) {
                    // Set the summary to reflect the new value.
                    if (index >= 0) {
                        if (!isFirst && DbManager.getTransDao().countOf() > 0) {
                            ToastUtils.showShort(R.string.has_trans_for_settle);
                            return false;
                        } else {
                            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                            if (!isFirst && !CurrencyConverter.getDefCurrency().getCountry().equals(listPreference.getEntryValues()[index].toString())) {
                                BaseWidget.updateWidget(FinancialApplication.mApp);
                                Utils.changeAppLanguage(FinancialApplication.mApp, CurrencyConverter.setDefCurrency(listPreference.getEntryValues()[index].toString()));

                                ActivityStack.getInstance().popAll();
                                Intent intent = new Intent();
                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                intent.setClass(FinancialApplication.mApp, MainActivity.class);
                                FinancialApplication.mApp.startActivity(intent);
                            }

                            isFirst = false;
                        }
                    }
                } else {
                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                }

            } else if (preference instanceof CheckBoxPreference) {

            } else if (preference instanceof EditTextPreference) {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                /*String pref_key = preference.getKey();
                boolean flag = false; //FIXME dont check value
                if (flag) {
                    Toast.makeText(preference.getContext(), R.string.input_err, Toast.LENGTH_SHORT).show();
                    return false;
                }*/
                if ((((EditTextPreference) preference).getEditText().getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                    if (stringValue.length() > 0) {
                        stringValue = "******";
                    }
                    preference.setSummary(stringValue);
                } else {
                    preference.setSummary(stringValue);
                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                /*String pref_key = preference.getKey();
                boolean flag = false; //FIXME dont check value
                if (flag) {
                    Toast.makeText(preference.getContext(), R.string.input_err, Toast.LENGTH_SHORT).show();
                    return false;
                }*/

                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        if (preference instanceof CheckBoxPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    SpManager.getSysParamSp().getBoolean(preference.getKey(), false));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    SpManager.getSysParamSp().getString(preference.getKey(), ""));
        }
    }

}
