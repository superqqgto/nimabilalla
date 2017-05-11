package com.pax.pay.trans.action.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;


/**
 * Created by chenzaoyang on 2017/3/31.
 */

public class SelectDccAmountActivity extends BaseActivityWithTickForAction{

    private TextView tvTitle;
    private ImageView ivBack;
//    private Button btnConfirm;


    private ListView listView;
    //数据源
    private ArrayList<HashMap<String, Object>> listItems;    //存放文字、图片信息
    //数据源适配器
    private SimpleAdapter listItemAdapter;           //适配器

    private ArrayList<String> list = new ArrayList<String>();
    private String navTitle;
    private boolean navBack;
    private LinearLayout llDetailContainer;

    private ArrayList<String> leftColumns = new ArrayList<>();
    private ArrayList<String> rightColumns = new ArrayList<>();


    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        leftColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString());
        rightColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dcc_select_amount;
    }

    @Override
    protected void initViews() {

        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);

        listView = (ListView) findViewById(R.id.select_dcc_list);
        initListView();
     //   listView.setAdapter(listItemAdapter);

        ArrayAdapter<String> myArrayAdapter = new ArrayAdapter<String>
                (this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(myArrayAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
        @Override
         public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
              long arg3) {
            for (int i = 0; i < leftColumns.size(); i++) {
                if(list.get(arg2).equals(rightColumns.get(i))) {
                    Toast.makeText(SelectDccAmountActivity.this, list.get(arg2),Toast.LENGTH_SHORT).show();
                    finish(new ActionResult(TransResult.SUCC, list.get(arg2)));
                }
            }
               }
        });
//        listView.setOnItemClickListener(new OnItemClickListener(){
//            @override
//            public void onItemClick(AdapterView<?> parent , View view , int position, long id){
//                listItems.get;
//            }
//
//        });
//        llDetailContainer = (LinearLayout) findViewById(R.id.detail_layout);

//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        params.bottomMargin = 15;
//
//        for (int i = 0; i < leftColumns.size(); i++) {
//            RelativeLayout layer = ViewUtils.genSingleLineLayout(SelectDccAmountActivity.this, leftColumns.get(i),
//                    rightColumns.get(i));
//            llDetailContainer.addView(layer, params);
//        }

//        btnConfirm = (Button) findViewById(R.id.confirm_btn);
    }

    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }

 //       btnConfirm.setOnClickListener(this);
    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                break;
//            case R.id.confirm_btn:
//                finish(new ActionResult(TransResult.SUCC, null));
//                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置适配器内容
     */
    private void initListView() {
        for (int i = 0; i < leftColumns.size(); i++) {
            list.add(rightColumns.get(i));
        }

//        listItems = new ArrayList<HashMap<String, Object>>();//HashMap<String, Object>  //String, Object
//        for (int i = 0; i < leftColumns.size(); i++) {
//            HashMap<String, Object> map = new HashMap<String,Object>();
//            map.put("ItemTitle", leftColumns.get(i));    //Title
//            map.put("ItemValue", rightColumns.get(i));   //Value
//            Log.i("Zac","Title" + leftColumns.get(i));
//            Log.i("Zac", "Value" + rightColumns.get(i));
//            listItems.add(map);
//        }
//        listItemAdapter = new SimpleAdapter(this,listItems,   // listItems数据源
//                R.layout.list_item,  //ListItem的XML布局实现
//                new String[] {"ItemTitle", "ItemValue"},     //动态数组与ImageItem对应的子项
//                new int[ ] {R.id.ItemTitle, R.id.ItemValue}      //list_item.xml布局文件
//        );
    }
}
