package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pax.abl.core.AAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.SelectDccAmountActivity;
import com.pax.pay.trans.action.activity.SelectAcqActivity;
import com.pax.pay.utils.ContextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by chenzaoyang on 2017/3/31.
 */

public class ActionSelectDccAmount extends AAction {

    public ActionSelectDccAmount(ActionStartListener listener) {
        super(listener);
    }

 //   private Context context;

    private LinkedHashMap<String, String> map;
    private String title;

    /**
     * 参数设置
     *
     * @param title   ：抬头
     * @param map     ：确认信息
     */
    public void setParam(  String title, LinkedHashMap<String, String> map) {
       // this.context = context;
        this.title = title;
        this.map = map;
    }

    @Override
    protected void process() {

        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ArrayList<String> leftColumns = new ArrayList<>();
                ArrayList<String> rightColumns = new ArrayList<>();

                Set<String> keys = map.keySet();
                for (String key : keys) {
                    leftColumns.add(key);
                    Object value = map.get(key);
                    if (value != null) {
                        rightColumns.add((String) value);
                    } else {
                        rightColumns.add("");
                    }

                }

                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString(), leftColumns);
                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), rightColumns);

                Context context= ContextUtils.getActyContext();
                Intent intent = new Intent(context, SelectDccAmountActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

}
