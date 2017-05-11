/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-22
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.settings;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextClock;
import android.widget.TimePicker;

import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.pay.constant.Constants;

import java.util.Calendar;

public class DateTimeFragment extends BaseFragment {

    private TextClock dateTc, timeTc;
    private Button systemCallBtn;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_date_time;
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void initView(View view) {
        dateTc = (TextClock) view.findViewById(R.id.system_date);
        dateTc.setOnClickListener(this);

        timeTc = (TextClock) view.findViewById(R.id.system_time);
        timeTc.setOnClickListener(this);

        systemCallBtn = (Button) view.findViewById(R.id.system_timezone);
        systemCallBtn.setOnClickListener(this);
    }

    @Override
    protected void onClickProtected(View v) {
        final Calendar c = Calendar.getInstance();
        switch (v.getId()) {
            case R.id.system_date:
                DatePickerDialog dialogDate = new DatePickerDialog(DateTimeFragment.this.getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        c.set(year, monthOfYear, dayOfMonth);
                        Device.setSystemTime(DateFormat.format(Constants.TIME_PATTERN_TRANS2, c).toString());
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialogDate.getDatePicker().setMinDate(Device.getMinDate().getTime());
                dialogDate.getDatePicker().setMaxDate(Device.getMaxDate().getTime());
                dialogDate.show();
                break;
            case R.id.system_time:
                TimePickerDialog dialogTime = new TimePickerDialog(DateTimeFragment.this.getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        Device.setSystemTime(DateFormat.format(Constants.TIME_PATTERN_TRANS2, c).toString());
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
                dialogTime.show();
                break;
            case R.id.system_timezone:
                RunSystemDateSetting();
                break;
            default:
                break;
        }

    }

    private void RunSystemDateSetting(){
        context.startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
    }
}
