/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-30
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.settings;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pax.edc.R;

import java.util.List;

public class SettingMainMenuListAdapter extends BaseAdapter {
    private List<Item> mListItems;
    private LayoutInflater mListContainer;
    private int selectItem = -1;

    public static final class Item {
        int redId;
        Class<?> cls;

        public Item(int redId, Class<?> cls) {
            this.redId = redId;
            this.cls = cls;
        }
    }

    public final class ListItemView {
        public TextView paraName;
    }

    public SettingMainMenuListAdapter(Context context, List<Item> listItems) {
        mListContainer = LayoutInflater.from(context);
        this.mListItems = listItems;
    }

    @Override
    public int getCount() {
        return mListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mListItems.get(position).redId;
    }

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    /**
     * ListView Item
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ListItemView listItemView = null;

        if (convertView == null) {
            listItemView = new ListItemView();
            convertView = mListContainer.inflate(R.layout.setting_main_menu_list_item, parent, false);

            listItemView.paraName = (TextView) convertView.findViewById(R.id.para_name);

            convertView.setTag(listItemView);

        } else {
            listItemView = (ListItemView) convertView.getTag();
        }

        String text = convertView.getResources().getString(mListItems.get(position).redId);
        listItemView.paraName.setText(text);

        if (position != selectItem) {
            convertView.setBackgroundColor(convertView.getResources().getColor(R.color.colorPrimaryDark));
            listItemView.paraName.setTextColor(Color.WHITE);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
            listItemView.paraName.setTextColor(Color.BLACK);
        }

        return convertView;
    }
}
