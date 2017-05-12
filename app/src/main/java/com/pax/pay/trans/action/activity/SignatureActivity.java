/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.edc.R;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.quickclick.QuickClickUtils;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.ToastUtils;
import com.pax.view.ElectronicSignatureView;

import butterknife.BindView;
import butterknife.OnClick;

public class SignatureActivity extends BaseActivityWithTickForAction {

    @BindView(R.id.header_title)
    TextView headerText;
    @BindView(R.id.header_back)
    ImageView backBtn;
    @BindView(R.id.trans_amount_tv)
    TextView amountText;
    @BindView(R.id.trans_amount_layout)
    LinearLayout transAmountLayout;
    @BindView(R.id.writeUserNameSpace)
    RelativeLayout writeUserName;
    @BindView(R.id.clear_btn)
    Button clearBtn;
    @BindView(R.id.confirm_btn)
    Button confirmBtn;

    public ElectronicSignatureView mSignatureView;
    private String amount;
    private boolean processing = false;

    // 保存签名图片
    private byte[] data;

    public static final String SIGNATURE_FILE_NAME = "customSignature.png";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_AMOUNT = "amount";


    @Override
    protected int getLayoutId() {
        return R.layout.activity_authgraph_layout;
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        amount = bundle.getString(EUIParamKeys.TRANS_AMOUNT.toString());
    }

    @Override
    protected void initViews() {

        headerText.setText(R.string.trans_signature);
        backBtn.setVisibility(View.GONE);

        amount = CurrencyConverter.convert(Long.parseLong(amount));
        amountText.setText(amount);

        mSignatureView = new ElectronicSignatureView(SignatureActivity.this);
        mSignatureView.setBitmap(new Rect(0, 0, 474, 158), 10, Color.WHITE);
        writeUserName.addView(mSignatureView);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
    }

    OnKeyListener onkeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        clearBtn.performClick();
                        return true;
                    case KeyEvent.KEYCODE_ENTER:
                        confirmBtn.performClick();
                        break;
                    case KeyEvent.KEYCODE_DEL:
                        clearBtn.performClick();
                        break;
                    default:
                        break;
                }
            }
            return false;
        }

    };

    @Override
    protected void setListeners() {

        writeUserName.setOnKeyListener(onkeyListener);
        mSignatureView.setOnKeyListener(onkeyListener);
        clearBtn.setOnKeyListener(onkeyListener);
        confirmBtn.setOnKeyListener(onkeyListener);
    }

    @Override
    protected boolean onKeyBackDown() {
        ToastUtils.showShort(R.string.err_not_allowed);
        return true;
    }

    @OnClick({R.id.clear_btn, R.id.confirm_btn})
    public void onViewClicked(View view) {

        if (QuickClickUtils.isFastDoubleClick(view)) {
            return;
        }

        switch (view.getId()) {
            case R.id.clear_btn:

                if (isProcessing()) {
                    return;
                }
                setProcessFlag();
                mSignatureView.clear();
                clearProcessFlag();
                break;
            case R.id.confirm_btn:
                LogUtils.i("TAG", "sign confirm_btn");
                if (isProcessing()) {
                    return;
                }
                setProcessFlag();
                if (!mSignatureView.getTouched()) {
                    LogUtils.i("touch", "no touch");
                    finish(new ActionResult(TransResult.SUCC, null));
                    return;
                }

                Bitmap bitmap = mSignatureView.save(true, 0);
                /*
                 * FileOutputStream fos = null; try { fos = openFileOutput(SIGNATURE_FILE_NAME, Context.MODE_PRIVATE);
                 * if (fos != null) { bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos); fos.close(); } } catch
                 * (Exception e) { e.printStackTrace(); }
                 */
                data = GlManager.getImgProcessing().bitmapToJbig(bitmap,
                        new IImgProcessing.IRgbToMonoAlgorithm() {

                            @Override
                            public int evaluate(int r, int g, int b) {
                                int v = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                                // set new pixel color to output bitmap
                                if (v < 200) {
                                    return 0;
                                } else {
                                    return 1;
                                }
                            }
                        });

                LogUtils.i("TAG", "电子签名数据长度为:" + data.length);

                if (data.length > 999) {
                    ToastUtils.showShort(R.string.signature_redo);
                    setProcessFlag();
                    mSignatureView.clear();
                    clearProcessFlag();
                    return;
                }
                clearProcessFlag();
                finish(new ActionResult(TransResult.SUCC, data));

                break;
            default:
                break;
        }
    }

    protected void setProcessFlag() {
        processing = true;
    }

    protected void clearProcessFlag() {
        processing = false;
    }

    protected boolean isProcessing() {
        return processing;
    }
}