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
package com.pax.view.dialog;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.pax.edc.R;
import com.pax.pay.utils.Utils;
import com.pax.view.AlwaysMarqueeTextView;

import java.util.ArrayList;

public class MenuPopupWindow extends PopupWindow {

    public static class ActionItem {
        // 定义图片对象
        private Drawable mDrawable;
        // 定义文本对象
        private CharSequence mTitle;

        private int mTitleId;

        public CharSequence getTitle() {
            return mTitle;
        }

        public int getId() {
            return mTitleId;
        }

        public ActionItem(Drawable drawable, CharSequence title) {
            this.mDrawable = drawable;
            this.mTitle = title;
        }

        public ActionItem(Context context, int titleId, int drawableId) {
            mTitleId = titleId;
            this.mTitle = context.getResources().getText(mTitleId);
            this.mDrawable = context.getResources().getDrawable(drawableId);
        }

        public ActionItem(Context context, int titleId, CharSequence title, int drawableId) {
            mTitleId = titleId;
            this.mTitle = title;
            this.mDrawable = context.getResources().getDrawable(drawableId);
        }
    }

    private Context mContext;
    // 列表弹窗的间隔
    private final int LIST_PADDING = 20;

    // 屏幕的宽度和高度
    private static int mScreenWidth, mScreenHeight;

    // 判断是否需要添加或更新列表子类项
    private boolean mIsDirty;

    // 位置不在中心
    private int popupGravity = Gravity.TOP | Gravity.START;

    // 弹窗子类项选中时的监听
    private OnItemOnClickListener mItemOnClickListener;

    // 定义列表对象
    private ListView mListView;

    // 定义弹窗子类项列表
    private ArrayList<ActionItem> mActionItems = new ArrayList<>();

    public MenuPopupWindow(Context context) {
        // 设置布局的参数
        this(context, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    public MenuPopupWindow(Context context, int width, int height) {
        this.mContext = context;

        // 设置可以获得焦点
        setFocusable(true);
        // 设置弹窗内可点击
        setTouchable(true);
        // 设置弹窗外可点击
        setOutsideTouchable(true);

        // 获得屏幕的宽度和高度
        mScreenWidth = Utils.getScreenWidth(mContext);
        mScreenHeight = Utils.getScreenHeight(mContext);

        // 设置弹窗的宽度和高度
        setWidth(width);
        setHeight(height);

        setBackgroundDrawable(new ColorDrawable());

        // 设置弹窗的布局界面
        setContentView(LayoutInflater.from(mContext).inflate(R.layout.menu_popup, null));

        initUI();
    }

    /**
     * 初始化弹窗列表
     */
    private void initUI() {
        mListView = (ListView) getContentView().findViewById(R.id.title_list);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 点击子类项后，弹窗消失
                dismiss();

                if (mItemOnClickListener != null)
                    mItemOnClickListener.onItemClick(mActionItems.get(position), position);
            }
        });
    }

    /**
     * 显示弹窗列表界面
     */
    public void show(View view) {
        // 判断是否需要添加或更新列表子类项
        if (mIsDirty) {
            populateActions();
        }

        int windowPos[] = calculatePopWindowPos(view, mListView);
        windowPos[0] -= LIST_PADDING;
        showAtLocation(view, popupGravity, windowPos[0], windowPos[1]);
    }

    private static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
        // 获取锚点View在屏幕上的左上角坐标位置
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 计算contentView的高宽
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        // 判断需要向上弹出还是向下弹出显示
        windowPos[0] = mScreenWidth - windowWidth;
        final boolean isNeedShowUp = (mScreenHeight - anchorLoc[1] - anchorHeight < windowHeight);
        if (isNeedShowUp) {
            windowPos[1] = anchorLoc[1] - windowHeight;
        } else {
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }

    /**
     * 设置弹窗列表子项
     */
    private void populateActions() {
        mIsDirty = false;

        // 设置列表的适配器
        mListView.setAdapter(new BaseAdapter() {

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public Object getItem(int position) {
                return mActionItems.get(position);
            }

            @Override
            public int getCount() {
                return mActionItems.size();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                AlwaysMarqueeTextView textView = null;

                if (convertView == null) {
                    textView = new AlwaysMarqueeTextView(mContext);
                    textView.setTextColor(mContext.getResources().getColor(android.R.color.white));
                    textView.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_stander_small));
                    // 设置文本居中
                    textView.setGravity(Gravity.CENTER);
                    // 设置文本域的范围
                    textView.setPadding(20, 15, 10, 15);
                    // 设置文本在一行内显示（不换行）
                    textView.setSingleLine(true);
                } else {
                    textView = (AlwaysMarqueeTextView) convertView;
                }

                ActionItem item = mActionItems.get(position);

                // 设置文本文字
                textView.setText(item.mTitle);
                // 设置文字与图标的间隔
                textView.setCompoundDrawablePadding(1);
                // 设置在文字的左边放一个图标
                textView.setCompoundDrawablesWithIntrinsicBounds(item.mDrawable, null, null, null);

                return textView;
            }
        });
    }

    /**
     * 添加子类项
     */
    public void addAction(ActionItem action) {
        if (action != null) {
            mActionItems.add(action);
            mIsDirty = true;
        }
    }

    /**
     * 清除子类项
     */
    public void cleanAction() {
        if (mActionItems.isEmpty()) {
            mActionItems.clear();
            mIsDirty = true;
        }
    }

    /**
     * 根据位置得到子类项
     */
    public ActionItem getAction(int position) {
        if (position < 0 || position > mActionItems.size())
            return null;
        return mActionItems.get(position);
    }

    /**
     * 设置监听事件
     */
    public void setItemOnClickListener(OnItemOnClickListener onItemOnClickListener) {
        this.mItemOnClickListener = onItemOnClickListener;
    }

    /**
     * 功能描述：弹窗子类项按钮监听事件
     */
    public interface OnItemOnClickListener {
        void onItemClick(ActionItem item, int position);
    }
}
