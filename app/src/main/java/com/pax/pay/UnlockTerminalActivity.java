/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-27
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.edc.R;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.constant.AdConstants;
import com.pax.pay.constant.Constants;
import com.pax.pay.service.LockService;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.view.dialog.DialogUtils;

import java.util.ArrayList;

import cn.bingoogolapple.bgabanner.BGABanner;

public class UnlockTerminalActivity extends BaseActivity {

    private GestureDetector mGestureDetector;
    private final ActionInputPassword actionInputPassword = createInputPwdAction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //must set before setContentView
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_unlock_terminal;
    }

    @Override
    protected void initViews() {
        mGestureDetector = new GestureDetector(this, new LearnGestureListener());

        BGABanner banner = (BGABanner) findViewById(R.id.banner_guide_content);
        banner.setAdapter(new BGABanner.Adapter<ImageView, String>() {
            @Override
            public void fillBannerItem(BGABanner banner, ImageView itemView, String model, int position) {
                Glide.with(UnlockTerminalActivity.this)
                        .load(model)
                        .centerCrop()
                        .dontAnimate()
                        .into(itemView);
            }
        });
        banner.setData(new ArrayList<>(AdConstants.ad.keySet()), null);
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void loadParam() {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected boolean onKeyBackDown() {
        actionInputPassword.execute();
        return true;
    }

    private ActionInputPassword createInputPwdAction() {
        ActionInputPassword inputPasswordAction = new ActionInputPassword(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(6, getString(R.string.prompt_terminal_pwd), null);
            }
        });

        inputPasswordAction.setEndListener(new AAction.ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                if (result.getRet() != TransResult.SUCC) {
                    return;
                }

                String data = EncUtils.SHA1((String) result.getData());
                if (!data.equals(SpManager.getSysParamSp().get(SysParamSp.SEC_TERMINALPWD))) {
                    DialogUtils.showErrMessage(UnlockTerminalActivity.this, getString(R.string.trans_password),
                            getString(R.string.err_password), null, Constants.FAILED_DIALOG_SHOW_TIME);
                    return;
                }

                finish();
                Intent i = new Intent();
                i.setClass(UnlockTerminalActivity.this, LockService.class);
                stopService(i);
            }
        });

        return inputPasswordAction;
    }

    private class LearnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityY < 0) // AET-67
                actionInputPassword.execute();
            return true;
        }
    }

}
