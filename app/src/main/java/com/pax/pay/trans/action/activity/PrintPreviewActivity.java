/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-6
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.manager.sp.SpManager;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.ToastUtils;
import com.pax.pay.utils.Utils;
import com.pax.manager.sp.SysParamSp;
import com.pax.view.dialog.MenuPopupWindow;

import butterknife.BindView;
public class PrintPreviewActivity extends BaseActivityWithTickForAction {

    private TextView headerText;
    private ImageView backBtn;
    private ImageView imageView;
    private Button btnCancel;
    private Button btnPrint;
    private ImageButton receiptModeBtn;

    public static final String HAS_SIGNATURE = "HAS_SIGNATURE";
    public final static String CANCEL_BUTTON = "CANCEL";
    public final static String BACK = "BACK";
    public final static String PRINT_BUTTON = "PRINT";
    public final static String SMS_BUTTON = "SMS";
    public final static String EMAIL_BUTTON = "EMAIL";

    private Bitmap bitmap;
    private byte[] bis;//bitmap byte data
    private Animation receiptOutAnim;
    private MenuPopupWindow popupWindow = null;
    private boolean hasSignature = false;

    @Override
    protected void loadParam() {
        //get data
        Intent intent = getIntent();
        if (intent != null) {
            //byte data to
            bis = intent.getByteArrayExtra(EUIParamKeys.BITMAP.toString());
            bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
            hasSignature = intent.getBooleanExtra(HAS_SIGNATURE, false);
        }
    }

    private void initMenuPopupWindow() {
        popupWindow = new MenuPopupWindow(this, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Utils.isSimReady(PrintPreviewActivity.this))
            popupWindow.addAction(new MenuPopupWindow.ActionItem(this, R.string.dialog_sms, R.drawable.ic29));
        if (Utils.isNetworkAvailable(PrintPreviewActivity.this))
            popupWindow.addAction(new MenuPopupWindow.ActionItem(this, R.string.dialog_email, R.drawable.ic29));
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_print_preview_layout;
    }

    @Override
    protected void initViews() {
        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(R.string.receipt_preview);
        backBtn = (ImageView) findViewById(R.id.header_back);
        backBtn.setVisibility(View.GONE);

        imageView = (ImageView) findViewById(R.id.printPreview);
        imageView.setImageBitmap(bitmap);
        btnCancel = (Button) findViewById(R.id.cancel_button);
        btnPrint = (Button) findViewById(R.id.print_button);
        receiptModeBtn = (ImageButton) findViewById(R.id.receipt_mode_btn);

        if (hasSignature && SpManager.getSysParamSp().get(SysParamSp.EDC_ENABLE_PAPERLESS).equals(SysParamSp.Constant.YES)) {
            receiptModeBtn.setVisibility(View.VISIBLE);
            initMenuPopupWindow();
        } else {
            receiptModeBtn.setVisibility(View.GONE);
        }

        receiptOutAnim = AnimationUtils.loadAnimation(this, R.anim.receipt_out);
    }

    @Override
    protected void setListeners() {
        btnCancel.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
        receiptModeBtn.setOnClickListener(this);
        if (popupWindow != null)
            popupWindow.setItemOnClickListener(new MenuPopupWindow.OnItemOnClickListener() {
                @Override
                public void onItemClick(MenuPopupWindow.ActionItem item, int position) {
                    switch (item.getId()) {
                        case R.string.dialog_sms:
                            finish(new ActionResult(TransResult.SUCC, SMS_BUTTON));
                            break;
                        case R.string.dialog_email:
                            finish(new ActionResult(TransResult.SUCC, EMAIL_BUTTON));
                            break;
                        default:
                            break;
                    }
                }
            });
    }

    @Override
    public void onClickProtected(View v) {
        LogUtils.i("On Click", v.toString());
        switch (v.getId()) {
            case R.id.cancel_button:
                //end trans
                finish(new ActionResult(TransResult.SUCC, CANCEL_BUTTON));
                break;
            case R.id.print_button:
                //print
                FinancialApplication.mApp.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.startAnimation(receiptOutAnim);
                    }
                });
                finish(new ActionResult(TransResult.SUCC, PRINT_BUTTON));
                break;
            case R.id.receipt_mode_btn:
                //sms or email
                if (popupWindow != null) {
                    popupWindow.show(v);
                } else {
                    ToastUtils.showLong(R.string.err_unsupported_func);
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected boolean onKeyBackDown() {
        // AET-91
        if (hasSignature) {
            ToastUtils.showShort(R.string.err_not_allowed);
        } else {
            finish(new ActionResult(TransResult.SUCC, BACK));
        }
        return true;
    }

    // AET-102
    @Override
    protected void onTimerFinish() {
        onClick(btnPrint);
    }
}
