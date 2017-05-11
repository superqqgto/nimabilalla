/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-30
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.pax.edc.R;
import com.pax.pay.app.FinancialApplication;

public class SelfTestActivity extends Activity {

    private int count = 3;
    private boolean isEnd = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selftest_layout);

        FinancialApplication.mApp.runInBackground(new CounterThread());
    }

    private class CounterThread implements Runnable {
        @Override
        public void run() {
            while (!isEnd) {
                try {
                    Thread.sleep(1000);
                    count--;
                    if (count < 1) {
                        isEnd = true;
                        FinancialApplication.mApp.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = (TextView) findViewById(R.id.selfTest);
                                textView.setText(getString(R.string.selfTest_succ));
                                Intent intent = getIntent();
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
