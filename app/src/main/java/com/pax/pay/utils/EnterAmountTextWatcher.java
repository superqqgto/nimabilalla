/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-28
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.utils;

import android.text.Editable;
import android.text.TextWatcher;

import com.pax.device.Device;

public class EnterAmountTextWatcher implements TextWatcher {

    private boolean mEditing;
    private long mPre = 0L;
    private String mPreStr;
    private boolean mIsForward = true;

    private long mBaseAmount = 0L;

    private long maxValue = 999999999999L;

    private boolean isFloorLimit = false;

    public EnterAmountTextWatcher() {
        mEditing = false;
    }

    public EnterAmountTextWatcher(boolean isFloorLimit) {
        this.isFloorLimit = isFloorLimit;
    }

    public EnterAmountTextWatcher(long baseAmount, long initTipAmount) {
        mEditing = false;

        setAmount(baseAmount, initTipAmount);
    }

    public void setAmount(long baseAmount, long tipAmount) {
        mBaseAmount = baseAmount;
        mPre = tipAmount;
        if (fListener != null) {
            fListener.onUpdateTipListener(mBaseAmount, mPre);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!mEditing) {
            if (isFloorLimit) {
                mIsForward = true;
                mPreStr = CurrencyConverter.convert(mPre);
                isFloorLimit = false;
            } else {
                mIsForward = (after >= count);
                mPreStr = s.toString();
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!mEditing) {
            mEditing = true;
            String edit = s.toString().trim();
            long curr = mBaseAmount;

            if (mIsForward) {
                long lastDigit = 0L;
                int time = 0;
                if (edit.length() > 0) {
                    int start = edit.indexOf(mPreStr);
                    if (start != -1) {
                        start += mPreStr.length();
                        time = edit.length() - start;
                    } else {
                        start = 0;
                    }
                    if (time > 0) {
                        try {
                            lastDigit = Long.parseLong(edit.substring(start).replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException e) {
                            time = 0;
                        }
                    }
                }

                time = (int) Math.pow(10, time);

                if (curr + mPre * time + lastDigit > maxValue) { //AET-21
                    Device.beepErr();
                    curr += mPre;
                } else if (fListener != null && mBaseAmount >= 0 && !fListener.onVerifyTipListener(mBaseAmount, mPre * time + lastDigit)) { // AET-27
                    Device.beepErr();
                    curr += mPre;
                } else {
                    curr += mPre * time + lastDigit;
                }
            } else {
                if (0 == edit.length()) {
                    mPre = 0L;
                }
                curr += mPre / 10;
            }

            String str = CurrencyConverter.convert(curr);
            try {
                s.replace(0, s.length(), str);
                mPre = curr - mBaseAmount;
                if (fListener != null) {
                    fListener.onUpdateTipListener(mBaseAmount, mPre);
                }
            } catch (NumberFormatException nfe) {
                s.clear();
                mPre = 0L;
            }
            mEditing = false;
        }
    }

    public long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }

    private OnTipListener fListener;

    public void setOnTipListener(OnTipListener listener) {
        this.fListener = listener;
    }

    public interface OnTipListener {
        void onUpdateTipListener(long baseAmount, long tipAmount);

        boolean onVerifyTipListener(long baseAmount, long tipAmount);
    }

    public void setFloorLimit(boolean floorLimit) {
        isFloorLimit = floorLimit;
    }
}
