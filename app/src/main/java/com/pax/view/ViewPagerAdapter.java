/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-1
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.view;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

    private List<View> lists;

    public ViewPagerAdapter(List<View> data) {
        lists = data;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {

        // return false;
        return arg0 == (arg1);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        try {
            // 解决View只能滑动两屏的方法
            if (container != null) {
                container.removeAllViews();
                container.addView(lists.get(position), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lists.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        try {
            container.removeView(lists.get(position));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
