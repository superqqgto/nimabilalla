/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-21
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.view.keyboard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.text.Editable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.pax.edc.R;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.TickTimer;
import com.pax.pay.utils.Utils;

import java.lang.reflect.Field;
import java.util.List;

import static android.content.Context.AUDIO_SERVICE;

// FIXME Kim scrollView cannot work with this
public class CustomKeyboardEditText extends EditText implements KeyboardView.OnKeyboardActionListener {

    private Keyboard mKeyboard;
    private CustomKeyboardView mKeyboardView;

    private boolean autoSize = false;
    private boolean keepKeyBoardOn = false;
    private int timeout = -1;
    private TickTimer tickTimer;

    private Window mWindow;
    private View mDecorView;
    private View mContentView;

    private CustomPopupWindow mKeyboardWindow;

    private boolean isNeedCustomKeyboard = true; // 是否启用自定义键盘
    /**
     * adjusted distance
     */
    private int mScrollDistance = 0;

    /**
     * the real height : screen height - guide height - status height
     */
    public static int screenContentHeight = -1;

    private Context mContext;

    public CustomKeyboardEditText(Context context) {
        this(context, null);
    }

    public CustomKeyboardEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomKeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttributes(context);
        initKeyboard(context, attrs);
    }

    private void initKeyboard(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Keyboard);
        if (array.hasValue(R.styleable.Keyboard_xml)) {
            isNeedCustomKeyboard = true;
            int xmlId = array.getResourceId(R.styleable.Keyboard_xml, 0);
            mKeyboard = new Keyboard(context, xmlId);
            setAutoSize(array.getBoolean(R.styleable.Keyboard_autoSize, false));
            setKeepKeyBoardOn(array.getBoolean(R.styleable.Keyboard_keepKeyboardOn, false));
            setTimeout(array.getInt(R.styleable.Keyboard_timeout_sec, -1));

            mKeyboardView = (CustomKeyboardView) LayoutInflater.from(context).inflate(R.layout.custom_keyboard_view, null);
            mKeyboardView.setKeyboard(mKeyboard);
            mKeyboardView.setEnabled(true);
            mKeyboardView.setPreviewEnabled(false);
            mKeyboardView.setOnKeyboardActionListener(this);

            mKeyboardWindow = new CustomPopupWindow(mKeyboardView,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mKeyboardWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            mKeyboardWindow.setOutsideTouchable(true);
            mKeyboardWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (mScrollDistance > 0) {
                        int temp = mScrollDistance;
                        mScrollDistance = 0;
                        if (null != mContentView) {
                            mContentView.scrollBy(0, -temp);
                        }
                    }
                }
            });

            mKeyboardWindow.setOnEnableDismissListener(new CustomPopupWindow.OnEnableDismissListener() {
                @Override
                public boolean onEnableDismiss() {
                    return false;
                }
            });
        } else {
            isNeedCustomKeyboard = false;
        }

        tickTimer = new TickTimer(new TickTimer.OnTickTimerListener() {
            @Override
            public void onTick(long leftTime) {
                LogUtils.i("TAG", "onTick:" + leftTime);
            }

            @Override
            public void onFinish() {
                onEditorAction(EditorInfo.IME_ACTION_NONE);
            }
        });


        // AET-65
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setText(getText());
            }
        });

        array.recycle();
    }

    private void initAttributes(Context context) {
        initScreenParams(context);
        setLongClickable(false);
        setCursorVisible(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        removeCopyAbility();

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(false);
                } else {
                    hideSysInput();
                    showKeyboard();
                }
            }
        });
    }

    @TargetApi(11)
    private void removeCopyAbility() {
        setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    private void initScreenParams(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(metrics);

        screenContentHeight = metrics.heightPixels - getStatusBarHeight(context);
    }

    /**
     *
     */
    private int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            Field field = clazz.getField("status_bar_height");
            int x = Integer.parseInt(field.get(object).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }


    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Editable editable = getText();
        int start = getText().length();
        playClick(primaryCode);

        if (timeout > 0) {
            tickTimer.start(timeout); // AET-85
        }

        if (primaryCode == Keyboard.KEYCODE_CANCEL) {// cancel
            hideKeyboard(false);
            onEditorAction(EditorInfo.IME_ACTION_NONE);
        } else if (primaryCode == Keyboard.KEYCODE_DONE) {// done
            hideKeyboard(false);
            onEditorAction(EditorInfo.IME_ACTION_DONE);
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {// delete
            if (editable != null && editable.length() > 0) {
                if (start > 0) {
                    editable.delete(start - 1, start);
                }
            }
        } else if (0x0 <= primaryCode && primaryCode <= 0x7f) {
            editable.insert(start, Character.toString((char) primaryCode));
        } else if (primaryCode > 0x7f) {
            Key key = getKeyByKeyCode(primaryCode);
            if (key != null)
                editable.insert(start, key.label);
        } else {

        }
    }

    private void playClick(int keyCode) {
        AudioManager am = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    private Key getKeyByKeyCode(int primaryCode) {
        if (null != mKeyboard) {
            List<Key> keyList = mKeyboard.getKeys();
            for (int i = 0, size = keyList.size(); i < size; i++) {
                Key key = keyList.get(i);

                if (key.codes[0] == primaryCode) {
                    return key;
                }
            }
        }

        return null;
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != mKeyboardWindow && mKeyboardWindow.isShowing())
            return true;
        if (!isFocusable())
            return true;

        super.onTouchEvent(event);

        if (isNeedCustomKeyboard && event.getAction() == MotionEvent.ACTION_UP) {
            hideSysInput();
            showKeyboard();
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (null != mKeyboardWindow && mKeyboardWindow.isShowing()) {
                if (!isKeepKeyBoardOn())
                    mKeyboardWindow.forceDismiss();
                tickTimer.stop();
                onEditorAction(EditorInfo.IME_ACTION_NONE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mWindow = ((Activity) getContext()).getWindow();
        mDecorView = mWindow.getDecorView();
        mContentView = mWindow.findViewById(Window.ID_ANDROID_CONTENT);
        mContentView.setFocusableInTouchMode(true);

        if (isFocused()) {
            hideSysInput();
            showKeyboard();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        hideSysInput();
        hideKeyboard(true);

        mKeyboardWindow = null;
        mKeyboardView = null;
        mKeyboard = null;

        mDecorView = null;
        mContentView = null;
        mWindow = null;
    }

    private int maxFontSize = 0;

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (isAutoSize()) {
            TextPaint textPaint = getPaint();
            int width = getMeasuredWidth() - getPaddingEnd() - getPaddingStart(); //AET-20
            if (width > 0) {
                int size = maxFontSize > 0 ? maxFontSize : (int) (textPaint.getTextSize());
                if (size > maxFontSize)
                    maxFontSize = size;

                while (true) {
                    textPaint.setTextSize(size);
                    float w = textPaint.measureText(text.toString());
                    if (w > width) {
                        size -= 2;
                    } else {
                        break;
                    }
                }
                setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            }
        }
    }

    private void showKeyboard() {
        if (null != mKeyboardWindow && !mKeyboardWindow.isShowing()) {
            mKeyboardView.setKeyboard(mKeyboard);
            if (null != mDecorView) {
                mKeyboardWindow.forceDismiss();
                mKeyboardWindow.showAtLocation(mDecorView, Gravity.BOTTOM, 0, 0);
                //mKeyboardWindow.update(); //bug on Android 7.0, it hardcode Gravity!!!!!
                if (timeout > 0)
                    tickTimer.start(timeout);
                setSelection(getText().length());

                if (null != mContentView) {
                    int[] pos = new int[2];
                    getLocationOnScreen(pos);
                    float height = dpToPx(getContext(), 240);

                    Rect outRect = new Rect();
                    mDecorView.getWindowVisibleDisplayFrame(outRect);

                    int screen = screenContentHeight;
                    mScrollDistance = (int) ((pos[1] + getMeasuredHeight() - outRect.top) - (screen - height));

                    if (mScrollDistance > 0) {
                        mContentView.scrollBy(0, mScrollDistance);
                    }
                }
            }
        }
    }

    private void hideKeyboard(boolean force) {
        if (null != mKeyboardWindow && mKeyboardWindow.isShowing()) {
            if (force || !isKeepKeyBoardOn())
                mKeyboardWindow.forceDismiss();
            this.clearFocus();
            tickTimer.stop();
        }
    }

    /**
     * 密度转换为像素值
     */
    private float dpToPx(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void hideSysInput() {
        if (getWindowToken() != null) {
            Utils.hideSystemKeyboard(getContext(), CustomKeyboardEditText.this);
        }
    }

    public boolean isAutoSize() {
        return autoSize;
    }

    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }

    public boolean isKeepKeyBoardOn() {
        return keepKeyBoardOn;
    }

    public void setKeepKeyBoardOn(boolean keepKeyBoardOn) {
        this.keepKeyBoardOn = keepKeyBoardOn;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
