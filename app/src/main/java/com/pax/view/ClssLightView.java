package com.pax.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pax.edc.R;

/**
 * Created by huangmuhua on 2017/4/28.
 */

public class ClssLightView extends LinearLayout {

    private BlinkImageView[] blinkImgs;

    public ClssLightView(Context context) {
        super(context);
        init();
    }

    public ClssLightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        blinkImgs = new BlinkImageView[4];
        Context context = getContext();
        blinkImgs[0] = new BlinkImageView(context, R.drawable.selector_blink_blue);
        blinkImgs[1] = new BlinkImageView(context, R.drawable.selector_blink_yellow);
        blinkImgs[2] = new BlinkImageView(context, R.drawable.selector_blink_green);
        blinkImgs[3] = new BlinkImageView(context, R.drawable.selector_blink_red);

        LayoutParams parentParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        parentParams.setLayoutDirection(HORIZONTAL);

        LayoutParams childParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        childParams.weight = 1;

        for (BlinkImageView img : blinkImgs) {
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setLayoutParams(parentParams);
            addView(img, childParams);
        }
        //first light blink, the rest lights off
        setLightStatus(0, BlinkImageView.BLINK);
    }

    public void setLightStatus(int index, @BlinkImageView.STATUS int status) {
        for (int i = 0; i < blinkImgs.length; i++) {
            if (i == index) {
                blinkImgs[i].onStatusChanged(status);
            } else {
                blinkImgs[i].onStatusChanged(BlinkImageView.OFF);
            }
        }
    }
}
