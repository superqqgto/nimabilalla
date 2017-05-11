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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pax.edc.R;
import com.pax.pay.trans.model.OptionModel;

import java.util.List;
import java.util.Vector;

@SuppressWarnings("rawtypes")
public class OptionsAdapter extends ListAdapter {

    private Activity activity;
    private List<OptionModel> models;
    private LayoutInflater inflater;
    private int pos;
    private int lastPosition = -1; // 记录上一次选中的图片位置，-1表示未选中
    private Vector<Boolean> vector = new Vector<>(); // 定义一个向量作为选中与否容器

    public OptionsAdapter(Activity activity, List<OptionModel> models) {
        super(activity);
        this.activity = activity;
        this.models = models;
        inflater = LayoutInflater.from(activity);

        for (int i = 0; i < models.size(); i++) {
            vector.add(false);
        }
    }

    @Override
    public int getCount() {

        return models.size();
    }

    @Override
    protected ViewHolder createViewHolder(View root) {
        OptionHolder hold = new OptionHolder();
        hold.tvOption = (TextView) root.findViewById(R.id.mode_grid_tv);
        return hold;
    }

    @Override
    protected void fillView(View root, Object item, ViewHolder holder, int position) {
        final OptionHolder hold = (OptionHolder) holder;
        hold.model = models.get(position);
        if (!"".equals(models.get(position).getContent())) {
            hold.tvOption.setText(models.get(position).getContent());
        }
        if (vector.elementAt(position)) {
            hold.tvOption.setBackgroundResource(R.drawable.bg_selected);
        } else {
            hold.tvOption.setBackgroundResource(R.drawable.bg_default);
        }
    }

    @Override
    protected int getItemViewId() {
        return R.layout.mode_grid_item;
    }

    class OptionHolder extends ViewHolder {
        private TextView tvOption;
        private OptionModel model;
    }

    /**
     * 修改选中时的状态
     *
     * @param position
     */
    public void changeState(int position) {
        if (lastPosition != -1)
            vector.setElementAt(false, lastPosition); // 取消上一次的选中状态
        vector.setElementAt(!vector.elementAt(position), position); // 直接取反即可
        lastPosition = position; // 记录本次选中的位置
        notifyDataSetChanged(); // 通知适配器进行更新
    }
}
