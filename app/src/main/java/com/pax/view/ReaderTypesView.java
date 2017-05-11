package com.pax.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pax.edc.R;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.*;

/**
 * Created by huangmuhua on 2017/4/28.
 */

public class ReaderTypesView extends LinearLayout {

    private ImageView[] imgViews;

    public ReaderTypesView(Context context) {
        super(context);
        init();
    }

    public ReaderTypesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        imgViews = new ImageView[3];
        Context context = getContext();
        imgViews[0] = new ImageView(context);
        imgViews[1] = new ImageView(context);
        imgViews[2] = new ImageView(context);

        imgViews[0].setImageResource(R.drawable.selector_swipe_card);
        imgViews[1].setImageResource(R.drawable.selector_insert_card);
        imgViews[2].setImageResource(R.drawable.selector_tap_card);

        LayoutParams parentParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        parentParams.setLayoutDirection(HORIZONTAL);

        LayoutParams childParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        childParams.weight = 1;

        for (ImageView img : imgViews) {
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setLayoutParams(parentParams);
            addView(img, childParams);
        }

    }

    /**
     * show images according to supported card read modes
     * 根据支持的读卡模式，显示相应的图片
     */
    public void show(byte mode) {

        boolean supportSwipe = SearchMode.contain(mode, ActionSearchCard.SearchMode.SWIPE);
        boolean supportInsert = SearchMode.contain(mode, ActionSearchCard.SearchMode.INSERT);
        boolean supportWave = SearchMode.contain(mode, ActionSearchCard.SearchMode.WAVE);
        show(supportSwipe, supportInsert, supportWave);
    }

    /**
     *  show images according to flags
     * @param supportSwipe
     * @param supportInsert
     * @param supportWave
     */
    private void show(boolean supportSwipe, boolean supportInsert, boolean supportWave) {
        imgViews[0].setSelected(supportSwipe);
        imgViews[1].setSelected(supportInsert);
        imgViews[2].setSelected(supportWave);
    }
}
