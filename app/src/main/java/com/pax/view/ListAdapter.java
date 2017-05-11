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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.pax.pay.app.FinancialApplication;

import java.util.ArrayList;
import java.util.List;

public abstract class ListAdapter<T> extends BaseAdapter {

    protected List<T> list;
    protected Context ctx;

    protected boolean loadFlag = true;
    /**
     * 加载过的图片
     */
    protected List<String> loadList = new ArrayList<>(0);

    public ListAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void stopLoadImg() {
        loadFlag = false;
        notifyDataSetChanged();
    }

    public void resumeLoadImg() {
        loadFlag = true;
        notifyDataSetChanged();
    }

    /**
     * 异步提交 通知数据变更
     */
    public synchronized void postNotifyDataSetChanged() {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    /**
     * 自动添加数据至第一条
     *
     * @param t
     */
    public void addFromLocal(T t) {
        // if (tempList == null) {
        // tempList = new ArrayList<>();
        // }
        if (null == list) {
            list = new ArrayList<>(0);
        }
        if (!list.contains(t))
            list.add(0, t);
        // tempList.add(t);
    }

    public void add(T t) {
        if (null == list) {
            list = new ArrayList<>(0);
        }
        if (!list.contains(t))
            list.add(t);
    }

    public void add(T t, int index) {
        if (null == list) {
            list = new ArrayList<>(0);
        }
        if (!list.contains(t))
            list.add(index, t);
    }

    public void add(List<T> t) {
        if (null == list) {
            list = new ArrayList<>(0);
        }
        // if (tempList != null && tempList.s ze() > 0) {
        // list.removeAll(tempList);
        // }
        for (T item : t) {
            if (!list.contains(item))
                list.add(item);
        }
    }

    public void addBefore(List<T> t) {
        if (null == list) {
            list = new ArrayList<>(0);
        }
        if (null == t || t.size() < 1) {
            return;
        }

        // if (tempList != null && tempList.size() > 0) {
        // list.removeAll(tempList);
        // }
        List<T> tl = new ArrayList<>();
        for (T item : t) {
            if (list.contains(item)) {
                tl.add(item);
            }
        }
        list.removeAll(tl);
        list.addAll(0, t);

    }

    public void remove(int position) {
        if (list == null)
            return;
        if (position > -1 && position < this.getCount()) {
            list.remove(position);
        }
    }

    public void remove(T t) {
        if (list == null)
            return;
        if (list.contains(t)) {
            list.remove(t);
        }
    }

    public void setList(List<T> t) {
        this.list = t;
    }

    public List<T> getList() {
        return list;
    }

    public void clear() {
        if (null != list) {
            this.list.clear();
        }
        clearLoadList();
    }

    public void clearLoadList() {
        if (null != this.loadList) {
            this.loadList.clear();
        }
    }

    @Override
    public int getCount() {
        return null == list ? 0 : list.size();
    }

    public T getListItem(int position) {
        if (list == null)
            return null;
        if (position > -1 && position < this.getCount()) {
            return list.get(position);
        }
        return null;
    }

    @Override
    public Object getItem(int position) {
        if (list == null)
            return null;
        if (position > -1 && position < this.getCount()) {
            return list.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        T t = this.getListItem(position);
        if (null == convertView || convertView.getTag() == null) {
            convertView = View.inflate(ctx, getItemViewId(), null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            //noinspection unchecked
            holder = (ViewHolder) convertView.getTag();
        }

        fillView(convertView, t, holder, position);
        // getItemPosition(position, holder);

        return convertView;
    }

    /**
     * 创建ViewHolder
     *
     * @param root
     * @return
     */
    protected abstract ViewHolder createViewHolder(View root);

    /**
     * 填充viewholder 数据
     *
     * @param root
     * @param item
     * @param holder
     */
    protected abstract void fillView(View root, T item, ViewHolder holder, int position);

    /**
     * 子布局id
     *
     * @return
     */
    protected abstract int getItemViewId();

    protected class ViewHolder {

    }

}
