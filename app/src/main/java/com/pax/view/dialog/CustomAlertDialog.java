/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-1
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.view.dialog;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.edc.R;
import com.pax.pay.utils.TickTimer;
import com.pax.pay.utils.Utils;

import java.util.List;

public class CustomAlertDialog extends Dialog implements android.view.View.OnClickListener {

    private View mDialogView;
    private int mAlertType;

    private TickTimer tickTimer;

    private int timeout;

    /**
     * 标题 title TextView
     */
    private TextView mTitleTextView;
    /**
     * 内容 content TextView
     */
    private TextView mContentTextView;

    private TextView mNormalTextView;
    /**
     * 带有图片
     */
    private ImageView mImageView;
    /**
     * 带有EditText的输入框布局
     */
    private LinearLayout mPwdLayout;
    private EditText mEditText;
    private TextView mErrTipsView;

    /**
     * 圆形进度条Progress布局
     */
    private FrameLayout mProgressLayout;
    private ProgressHelper mProgressHelper;
    /**
     * 显示倒计时的TextView
     */
    private TextView mCountView;

    /**
     * 操作失败的布局
     */
    private FrameLayout mErrorFrame;
    private ImageView mErrorX;
    private Animation mErrorInAnim;
    private AnimationSet mErrorXInAnim;
    /**
     * 操作成功的布局
     */
    private FrameLayout mSuccessFrame;
    private SuccessTickView mSuccessTick;
    /**
     * 取消、确定按钮 布局
     */
    private LinearLayout mButtonLayout;
    private Button mConfirmButton;
    private Button mCancelButton;

    private String mTitleText;
    private String mContentText;
    private Bitmap mBitmap;
    private String mErrTips;
    private String mNormalText;

    private boolean mShowTitle;
    /**
     * 是否显示Cancel Button
     */
    private boolean mShowCancel;
    /**
     * 是否显示Content TextView
     */
    private boolean mShowContent;
    /**
     * 是否显示Confirm Button
     */
    private boolean mShowConfirm;
    private boolean mShowNormal;
    private boolean mShowImage;

    private View mSuccessLeftMask;
    private View mSuccessRightMask;

    private AnimationSet mSuccessLayoutAnimSet;
    private Animation mSuccessBowAnim;

    private String mConfirmText;
    private String mCancelText;

    /**
     * 为Button设置监听事件
     */
    private OnCustomClickListener mCancelClickListener;
    private OnCustomClickListener mConfirmClickListener;

    public static final int NORMAL_TYPE = 0;
    public static final int ERROR_TYPE = 1;
    public static final int SUCCESS_TYPE = 2;
    public static final int CUSTOM_ENTER_TYPE = 3;
    public static final int PROGRESS_TYPE = 4;
    public static final int IMAGE_TYPE = 5;
    public static final int WARN_TYPE = 6;


    public interface OnCustomClickListener {
        void onClick(CustomAlertDialog alertDialog);
    }

    public CustomAlertDialog(Context context) {
        this(context, NORMAL_TYPE);
    }

    public CustomAlertDialog(Context context, int alertType) {
        super(context, R.style.alert_dialog);
        this.mAlertType = alertType;
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        mProgressHelper = new ProgressHelper(context);

        mErrorInAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.error_frame_in);
        mErrorXInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.error_x_in);
        // 2.3.x system don't support alpha-animation on layer-list drawable
        // remove it from animation set
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            List<Animation> childAnims = mErrorXInAnim.getAnimations();
            int idx = 0;
            for (; idx < childAnims.size(); idx++) {
                if (childAnims.get(idx) instanceof AlphaAnimation) {
                    break;
                }
            }
            if (idx < childAnims.size()) {
                childAnims.remove(idx);
            }
        }
        mSuccessBowAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.success_bow_roate);
        mSuccessLayoutAnimSet = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(),
                R.anim.success_mask_layout);

    }

    public CustomAlertDialog(Context context, int alertType, int timeout) {
        this(context, alertType);
        setTimeout(timeout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog_layout);

        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);

        mNormalTextView = (TextView) findViewById(R.id.normal_text);

        mImageView = (ImageView) findViewById(R.id.custom_image);

        mPwdLayout = (LinearLayout) findViewById(R.id.input_edtxt_layout);
        mEditText = (EditText) findViewById(R.id.input_edtxt);
        mErrTipsView = (TextView) findViewById(R.id.input_err_tips);

        mProgressLayout = (FrameLayout) findViewById(R.id.progress_dialog);
        mProgressHelper.setProgressWheel((ProgressWheel) findViewById(R.id.progressWheel));
        mCountView = (TextView) findViewById(R.id.countView);

        mErrorFrame = (FrameLayout) findViewById(R.id.error_frame);
        mErrorX = (ImageView) mErrorFrame.findViewById(R.id.error_x);
        mSuccessFrame = (FrameLayout) findViewById(R.id.success_frame);
        mSuccessTick = (SuccessTickView) mSuccessFrame.findViewById(R.id.success_tick);
        mSuccessLeftMask = mSuccessFrame.findViewById(R.id.mask_left);
        mSuccessRightMask = mSuccessFrame.findViewById(R.id.mask_right);

        mButtonLayout = (LinearLayout) findViewById(R.id.button_layout);
        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);

        mTitleTextView = (TextView) findViewById(R.id.title_text);
        mContentTextView = (TextView) findViewById(R.id.content_text);

        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        setTitleText(mTitleText);
        setContentText(mContentText);

        setCancelText(mCancelText);
        setConfirmText(mConfirmText);

        changeAlertType(mAlertType, true);

    }

    public void setTimeout(int time) {
        this.timeout = time;

        // 当设置超时时间小于1，不显示倒计时
        if (timeout <= 0) {
            if (mCountView != null) {
                mCountView.setVisibility(View.GONE);
            }
            return;
        }
        if(tickTimer != null){
            tickTimer.stop();
        }
        tickTimer = new TickTimer(new TickTimer.OnTickTimerListener() {
            @Override
            public void onTick(long leftTime) {
                String tick = leftTime + getContext().getString(R.string.sec);
                mCountView.setText(tick);
            }

            @Override
            public void onFinish() {
                if (mAlertType == ERROR_TYPE || mAlertType == SUCCESS_TYPE || mAlertType == IMAGE_TYPE) {
                    CustomAlertDialog.super.dismiss();
                }

                String tick = 0 + getContext().getString(R.string.sec);
                mCountView.setText(tick);
                tickTimer.stop();
            }
        });
        tickTimer.start(timeout);
        Utils.wakeupScreen(timeout); // AET-97
    }

    /**
     * 获取标题Title
     *
     * @return
     */
    public String getTitleText() {
        return mTitleText;
    }

    /**
     * 设置标题Title
     *
     * @param text
     * @return
     */
    public CustomAlertDialog setTitleText(String text) {
        mTitleText = text;
        if (mTitleTextView != null && mTitleText != null) {
            showTitleText(true);
            mTitleTextView.setText(mTitleText);
        }
        return this;
    }

    /**
     * 设置图片Image
     *
     * @param bitmap
     * @return
     */
    public CustomAlertDialog setImage(Bitmap bitmap) {

        mBitmap = bitmap;
        if (mImageView != null && mBitmap != null) {
            showImage(true);
            mImageView.setImageBitmap(bitmap);
        }

        return this;
    }

    public String getNormalText() {
        return mNormalText;
    }

    public CustomAlertDialog setNormalText(String text) {
        mNormalText = text;
        if (mNormalTextView != null && mNormalText != null) {
            showNormalText(true);
            mNormalTextView.setText(mNormalText);
        }
        return this;
    }

    public CustomAlertDialog showNormalText(boolean isShow) {
        mShowNormal = isShow;
        if (mNormalTextView != null) {
            mNormalTextView.setVisibility(mShowNormal ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 获取内容Content
     *
     * @return
     */
    public String getContentText() {
        return mContentText;
    }

    /**
     * 设置内容Content
     *
     * @param text
     * @return
     */
    public CustomAlertDialog setContentText(String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText(true);
            mContentTextView.setText(mContentText);
        }
        return this;
    }

    /**
     * 获取输入框EditText中输入的内容
     *
     * @return
     */
    public String getContentEditText() {
        return mEditText.getText().toString().trim();
    }

    public boolean isShowTitleText() {
        return mShowTitle;
    }

    public CustomAlertDialog showTitleText(boolean isShow) {

        mShowTitle = isShow;
        if (mTitleTextView != null) {
            mTitleTextView.setVisibility(mShowTitle ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 获取当前Content TextView的显示状态
     *
     * @return
     */
    public boolean isShowContentText() {
        return mShowContent;
    }

    /**
     * 设置是否显示内容Content TextView
     *
     * @param isShow true :显示ContentTextView<br>
     *               false :不显示ContentTextView
     * @return
     */
    public CustomAlertDialog showContentText(boolean isShow) {
        mShowContent = isShow;
        if (mContentTextView != null) {
            mContentTextView.setVisibility(mShowContent ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 获取Cancel Button 显示状态
     *
     * @return
     */
    public boolean isShowCancelButton() {
        return mShowCancel;
    }

    /**
     * 设置是否显示Cancel Button
     *
     * @param isShow true :显示Cancel Button<br>
     *               false :不显示Cancel Button
     * @return
     */
    public CustomAlertDialog showCancelButton(boolean isShow) {
        mShowCancel = isShow;
        if (mCancelButton != null) {
            mCancelButton.setVisibility(mShowCancel ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public CustomAlertDialog showErrTips(boolean isShow) {
        if (mErrTipsView != null) {
            mErrTipsView.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        }
        return this;
    }

    public boolean isShowImage() {
        return mShowImage;
    }

    public CustomAlertDialog showImage(boolean isShow) {
        mShowImage = isShow;
        if (mImageView != null) {
            mImageView.setVisibility(mShowImage ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 获取Confirm Button 显示状态
     *
     * @return
     */
    public boolean isShowConfirmButton() {
        return mShowConfirm;
    }

    /**
     * 设置是否显示Confirm Button
     *
     * @param isShow true :显示Confirm Button<br>
     *               false :不显示Confirm Button
     * @return
     */
    public CustomAlertDialog showConfirmButton(boolean isShow) {
        mShowConfirm = isShow;
        if (mConfirmButton != null) {
            mConfirmButton.setVisibility(mShowConfirm ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public String getCancelText() {
        return mCancelText;
    }

    /**
     * 设置Cancel Button 文本内容
     *
     * @param text
     * @return
     */
    public CustomAlertDialog setCancelText(String text) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            showCancelButton(true);
            mCancelButton.setText(mCancelText);
        }
        return this;
    }

    public String getConfirmText() {
        return mConfirmText;
    }

    /**
     * 设置Confirm Button 文本内容
     *
     * @param text
     * @return
     */
    public CustomAlertDialog setConfirmText(String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    public CustomAlertDialog setErrTipsText(String text) {
        mErrTips = text;
        if (mErrTipsView != null) {
            showErrTips(true);
            mErrTipsView.setText(mErrTips);
            ObjectAnimator animator = ObjectAnimator.ofFloat(mErrTipsView, "alpha", 0, 1);
            animator.setDuration(1000);
            animator.setRepeatCount(3);
            animator.setRepeatMode(ObjectAnimator.REVERSE);
            animator.start();
        }
        return this;
    }

    public ProgressHelper getProgressHelper() {
        return mProgressHelper;
    }

    /**
     * AlerType
     *
     * @return
     */

    public int getAlerType() {
        return mAlertType;
    }

    public void changeAlertType(int alertType) {
        changeAlertType(alertType, false);
    }

    private void changeAlertType(int alertType, boolean fromCreate) {

        mAlertType = alertType;
        if (mDialogView != null) {

            if (!fromCreate) {
                restore();
            }

            switch (mAlertType) {

                case NORMAL_TYPE:
                    if (isShowConfirmButton())
                        mConfirmButton.setVisibility(View.VISIBLE);
                    break;
                case ERROR_TYPE:
                    mErrorFrame.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS_TYPE:
                    mSuccessFrame.setVisibility(View.VISIBLE);
                    // initial rotate layout of success mask
                    mSuccessLeftMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(0));
                    mSuccessRightMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(1));
                    break;
                case CUSTOM_ENTER_TYPE:
                    mPwdLayout.setVisibility(View.VISIBLE);
                    mCancelButton.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.VISIBLE);
                    break;
                case PROGRESS_TYPE:
                    mProgressLayout.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.GONE);
                    break;
                case IMAGE_TYPE:
                    mImageView.setVisibility(View.VISIBLE);
                    mTitleTextView.setVisibility(View.GONE);
                    mCancelButton.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.VISIBLE);
                    break;
                case WARN_TYPE:
                    mImageView.setVisibility(View.VISIBLE);
                    mTitleTextView.setVisibility(View.GONE);
                    mCancelButton.setVisibility(View.GONE);
                    mConfirmButton.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }

            if (!fromCreate) {
                playAnimation();
            }
        }
    }

    private void playAnimation() {
        if (mAlertType == ERROR_TYPE) {
            mErrorFrame.startAnimation(mErrorInAnim);
            mErrorX.startAnimation(mErrorXInAnim);
        } else if (mAlertType == SUCCESS_TYPE) {
            mSuccessTick.startTickAnim();
            mSuccessRightMask.startAnimation(mSuccessBowAnim);
        }
    }

    private void restore() {
        mPwdLayout.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.GONE);

        mErrorFrame.setVisibility(View.GONE);
        mSuccessFrame.setVisibility(View.GONE);
        mButtonLayout.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.GONE);
        mNormalTextView.setVisibility(View.GONE);
        mErrTipsView.setVisibility(View.INVISIBLE);

        mErrorFrame.clearAnimation();
        mErrorX.clearAnimation();
        mSuccessTick.clearAnimation();
        mSuccessLeftMask.clearAnimation();
        mSuccessRightMask.clearAnimation();

    }

    @Override
    protected void onStart() {
        playAnimation();
    }

    @Override
    public void cancel() {
        dismiss();
    }

    public CustomAlertDialog setCancelClickListener(OnCustomClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    public CustomAlertDialog setConfirmClickListener(OnCustomClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_button) {
            if (mCancelClickListener != null) {
                mCancelClickListener.onClick(CustomAlertDialog.this);
            } else {
                dismiss();
            }
        } else if (v.getId() == R.id.confirm_button) {
            if (mConfirmClickListener != null) {
                mConfirmClickListener.onClick(CustomAlertDialog.this);
            } else {
                dismiss();
            }
        }
    }

}
