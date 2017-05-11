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

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.pax.edc.R;
import com.pax.manager.sp.ControllerSp;
import com.pax.manager.sp.SpManager;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.multiHostParam.AcquirerFragment;
import com.pax.settings.multiHostParam.EDCFragment;
import com.pax.settings.multiHostParam.IssuerParamFragment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SettingMenuFragment extends Fragment {
    public static final String TAG = "SettingsMainMenuFragment";
    //   private Fragment mMerchantParaFragment = null, mPasswordManageFragment = null;
    private int mCurCheckPosition = 0;
    private ListView mListView;
    private SettingMainMenuListAdapter mAdapter;
    private List<SettingMainMenuListAdapter.Item> mData;

    private boolean isFirst = SpManager.getControlSp().getBoolean(ControllerSp.IS_FIRST_RUN);

    private static final List<SettingMainMenuListAdapter.Item> listItems = new LinkedList<SettingMainMenuListAdapter.Item>() {{
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_communication_parameter, CommParamFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_edc_parameter, EDCFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_issuer_parameter, IssuerParamFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_acquirer_parameter, AcquirerFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_keyManage, KeyManageFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_pwd_manage, PwdManageDetailFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_date_time, DateTimeFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_otherManage, OtherManageFragment.class));

    }};

    private static final List<SettingMainMenuListAdapter.Item> initListItems = new LinkedList<SettingMainMenuListAdapter.Item>() {{
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_communication_parameter, CommParamFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_keyManage, KeyManageFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_pwd_manage, PwdManageDetailFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_date_time, DateTimeFragment.class));
        add(new SettingMainMenuListAdapter.Item(R.string.settings_menu_otherManage, OtherManageFragment.class));
    }};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mData = isFirst ? initListItems : listItems;
        View rootView = inflater.inflate(R.layout.setting_options_list, container, false);
        mListView = (ListView) rootView.findViewById(R.id.item_list);
        mAdapter = new SettingMainMenuListAdapter(getActivity(), mData);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(itemClickListener);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }
        // In dual-pane mode, the list view highlights the selected item_gridview.
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        showDetails(mCurCheckPosition, 0);
    }

    private final OnItemClickListener itemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
            showDetails(index, 1);
        }
    };

    private void showDetails(int index, int flag) {
        Fragment fragment = null;

        if ((flag == 1) && (mCurCheckPosition == index))
            return;

        mAdapter.setSelectItem(index);
        mAdapter.notifyDataSetInvalidated();

        try {
            fragment = (Fragment) mData.get(index).cls.newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (fragment != null) {
            mCurCheckPosition = index;
            getFragmentManager().beginTransaction().replace(R.id.settings_main_detail, fragment).commit();
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onSaveInstanceState(android.os.Bundle) // Create fragment and give it an argument
     * specifying the article it should show ArticleFragment newFragment = new ArticleFragment(); Bundle args = new
     * Bundle(); args.putInt(ArticleFragment.ARG_POSITION, position); newFragment.setArguments(args);
     * FragmentTransaction transaction = getSupportFragmentManager().beginTransaction(); // Replace whatever is in the
     * fragment_container view with this fragment, // and add the transaction to the back stack so the user can navigate
     * back transaction.replace(R.id.fragment_container, newFragment); transaction.addToBackStack(null); // Commit the
     * transaction transaction.commit();
     */

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Menu 1a").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add("Menu 1b").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ToastUtils.showShort(" && menu text is " + item.getTitle());
        return super.onOptionsItemSelected(item);
    }

}
