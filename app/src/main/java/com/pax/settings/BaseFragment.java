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

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pax.pay.app.quickclick.QuickClickUtils;
import com.pax.pay.utils.LeakUtils;

abstract public class BaseFragment extends Fragment implements View.OnClickListener {
    protected Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        context = view.getContext();
        initData();
        initView(view);

        return view;
    }

    /**
     * get layout ID
     *
     * @return
     */
    abstract protected int getLayoutId();

    abstract protected void initData();

    abstract protected void initView(View view);

    // AET-93
    @Override
    public final void onClick(View v) {
        if (QuickClickUtils.isFastDoubleClick(800)) {
            return;
        }
        onClickProtected(v);
    }

    protected void onClickProtected(View v) {
        //do nothing
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LeakUtils.watch(this);
    }
}
