/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-1
 * Module Auth: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ATransaction;
import com.pax.edc.R;
import com.pax.pay.app.quickclick.QuickClickUtils;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.utils.ContextUtils;
import com.pax.view.GridViewAdapter.GridItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单选项
 *
 * @author Steven.W
 */
public class MenuPage extends LinearLayout {
    private Context context;

    /*
     * 菜单项列表
     */
    private List<GridItem> itemList;

    private ViewPager mViewPager;
    /*
     * 页面指示器（。。。）容器
     */
    private LinearLayout pageIndicatorLayout;
    /*
     * 页面指示器（。。。）
     */
    private ImageView[] pageIndicator;
    /*
     * 总页面数
     */
    private int numPages;
    /*
     * 当前页面索引
     */
    private int currentPageIndex;
    /*
     * 每页最大显示item数目
     */
    private int maxItemNumPerPage = 9;
    /*
     * 列数
     */
    private int columns = 3;
    private List<View> gridViewList;

    public MenuPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.itemList = null;
        initView();
    }

    @SuppressWarnings("unchecked")
    public MenuPage(Context context, int maxItemNumPerPage, int columns, List<?> list) {
        super(context);
        this.context = context;
        this.columns = columns;
        this.maxItemNumPerPage = maxItemNumPerPage;
        this.itemList = (List<GridItem>) list;
        initView();
        initPageIndicator();
        initOptionsMenu();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_menu, null);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        pageIndicatorLayout = (LinearLayout) view.findViewById(R.id.ll_dots);
        addView(view);
    }

    /**
     * 设置当前页面指示器
     *
     * @param position
     */
    private void setCurrentIndicator(int position) {
        if (position < 0 || position > numPages - 1 || currentPageIndex == position) {
            return;
        }
        for (ImageView i : pageIndicator) {
            i.setImageResource(R.drawable.guide_dot_normal);
        }
        pageIndicator[position].setImageResource(R.drawable.guide_dot_select);
        currentPageIndex = position;
    }

    /**
     * 获取每个页面girdView
     *
     * @param pageIndex
     * @return
     */
    private View getViewPagerItem(int pageIndex) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.viewpage_gridview, null);
        CustomGridView gridView = (CustomGridView) layout.findViewById(R.id.vp_gv);
        gridView.setNumColumns(columns);

        GridViewAdapter adapter = new GridViewAdapter(context, itemList, pageIndex, maxItemNumPerPage);
        adapter.setColumns(columns);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 连续多次点击未处理
                if (QuickClickUtils.isFastDoubleClick(view)) {
                    return;
                }

                if ((position + currentPageIndex * maxItemNumPerPage) < itemList.size()) {
                    GridItem item = itemList.get(position + currentPageIndex * maxItemNumPerPage);
                    process(item);
                }
            }
        });
        return gridView;
    }

    /**
     * 点击菜单项目处理
     *
     * @param item
     */
    private void process(GridItem item) {

        Class<?> clazz = item.getActivity();
        if (clazz != null) {
            Intent intent = new Intent(context, clazz);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), item.getName());
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            intent.putExtras(bundle);
            context.startActivity(intent);

            return;
        }

        ATransaction trans = item.getTrans();
        if (trans != null) {
            trans.execute();
            return;
        }

        AAction action = item.getAction();
        if (action != null) {
            action.execute();
        }
    }

    // 初始化选项菜单
    public void initOptionsMenu() {
        gridViewList = new ArrayList<>();
        for (int i = 0; i < numPages; i++) {
            gridViewList.add(getViewPagerItem(i));
        }
        mViewPager.setAdapter(new ViewPagerAdapter(gridViewList));
    }

    /**
     * 设置ViewPager显示position指定的页面
     *
     * @param position
     */
    public void setCurrentPager(int position) {
        mViewPager.setCurrentItem(position);
    }

    // 初始化指示点
    public void initPageIndicator() {

        if (itemList.size() % maxItemNumPerPage == 0) {
            numPages = itemList.size() / maxItemNumPerPage;
        } else {
            numPages = itemList.size() / maxItemNumPerPage + 1;
        }
        if (0 < numPages) {
            pageIndicatorLayout.removeAllViews();
            if (1 == numPages) {
                pageIndicatorLayout.setVisibility(View.GONE);
            } else if (1 < numPages) {
                pageIndicatorLayout.setVisibility(View.VISIBLE);
                for (int j = 0; j < numPages; j++) {
                    ImageView image = new ImageView(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20); // dot的宽高
                    params.setMargins(10, 0, 10, 0);
                    image.setImageResource(R.drawable.guide_dot_normal);
                    pageIndicatorLayout.addView(image, params);
                }
            }
        }
        if (numPages != 1) {
            pageIndicator = new ImageView[numPages];
            for (int i = 0; i < numPages; i++) {
                pageIndicator[i] = (ImageView) pageIndicatorLayout.getChildAt(i);
            }
            currentPageIndex = 0;
            pageIndicator[currentPageIndex].setImageResource(R.drawable.guide_dot_select);
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageSelected(int index) {
                    setCurrentIndicator(index);
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {

                }

                @Override
                public void onPageScrollStateChanged(int arg0) {

                }
            });
        }

    }

    public static class Builder {

        private MenuPage menuPage;
        private Context context;
        private int maxItemNumPerPage;
        private int columns;
        private List<GridItem> itemList;

        public Builder(Context context, int maxItemNumPerPage, int columns) {
            this.context = context;
            this.maxItemNumPerPage = maxItemNumPerPage;
            this.columns = columns;
        }

        /**
         * 设置与交易相关的菜单项
         *
         * @param title 菜单项的名称
         * @param icon  菜单项的图片ID
         * @param trans 相关联的交易
         * @return
         */

        public Builder addTransItem(String title, int icon, ATransaction trans) {
            if (itemList == null) {
                itemList = new ArrayList<>();
            }
            itemList.add(new GridItem(title, icon, trans));
            return this;
        }

        public Builder addTransItem(int transNameResId, int iconResId, ATransaction trans) {
            return addTransItem(ContextUtils.getString(transNameResId), iconResId, trans);
        }

        /**
         * 设置与交易无关的菜单项,只负责Activity的跳转
         *
         * @param title 菜单项的名称
         * @param icon  菜单项的图片ID
         * @param act   相关联的Activity
         * @return
         */
        public Builder addMenuItem(String title, int icon, Class<?> act) {
            if (itemList == null) {
                itemList = new ArrayList<>();
            }
            itemList.add(new GridItem(title, icon, act));
            return this;
        }

        public Builder addMenuItem(int titleResId, int iconResId, Class<?> activity) {
            return addMenuItem(ContextUtils.getString(titleResId), iconResId, activity);
        }

        /**
         * 设置非交易类,使用Action跳转的菜单项
         *
         * @param title  菜单项的名称
         * @param icon   菜单项的图片ID
         * @param action 相关联的AAction
         * @return
         */
        public Builder addActionItem(String title, int icon, AAction action) {
            if (itemList == null) {
                itemList = new ArrayList<>();
            }
            itemList.add(new GridItem(title, icon, action));
            return this;

        }

        public Builder addActionItem(int titleResId, int iconResId, AAction action) {
            return addActionItem(ContextUtils.getString(titleResId), iconResId, action);
        }

        /**
         * 创建并返回MenuPage视图
         *
         * @return
         */
        public MenuPage create() {
            menuPage = new MenuPage(context, maxItemNumPerPage, columns, itemList);
            return menuPage;
        }

    }

}
