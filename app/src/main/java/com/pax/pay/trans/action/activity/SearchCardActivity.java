/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Sim.G
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.abl.utils.PanUtils;
import com.pax.abl.utils.TrackUtils;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PollingResult;
import com.pax.dal.entity.PollingResult.EOperationType;
import com.pax.dal.exceptions.IccDevException;
import com.pax.dal.exceptions.MagDevException;
import com.pax.dal.exceptions.PiccDevException;
import com.pax.device.Device;
import com.pax.edc.BuildConfig;
import com.pax.edc.R;
import com.pax.eventbus.ClssLightStatusEvent;
import com.pax.manager.AcqManager;
import com.pax.manager.neptune.DalManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.base.Issuer;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.emv.EmvListenerImpl;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.ToastUtils;
import com.pax.view.BlinkImageView;
import com.pax.view.ClssLightView;
import com.pax.view.ReaderTypesView;
import com.pax.view.dialog.DialogUtils;
import com.pax.view.keyboard.CustomKeyboardEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * SearchCardAction中跳转至SearchCardActivity,Intent传递两个参数;
 * <p>
 * {@link EUIParamKeys#TRANS_AMOUNT},
 * {@link EUIParamKeys #CARD_SEARCH_MODE}
 *
 * @author Sim.G
 */
@SuppressLint("SimpleDateFormat")
public class SearchCardActivity extends BaseActivityWithTickForAction {

    @BindView(R.id.clssLight)
    ClssLightView clssLight;
    @BindView(R.id.bank_card_number)
    CustomKeyboardEditText edtCardNo;// 输入框
    @BindView(R.id.bank_card_expdate)
    CustomKeyboardEditText expDate; //卡有效期
    @BindView(R.id.supported_reader_types)
    ReaderTypesView readerTypesView;
    @BindView(R.id.ok_btn)
    Button btnConfirm;// 确认按钮
    @BindView(R.id.tv_prompt_readcard)
    TextView tvPrompt; // 输入方式提示
    @BindView(R.id.bank_card_holder_name)
    TextView holderName; //显示持卡人姓名
    @BindView(R.id.amount_layout)
    LinearLayout llAmount; // 交易金额布局
    @BindView(R.id.amount_txt)
    TextView tvAmount;
    @BindView(R.id.header_title)
    TextView tvTitle;
    @BindView(R.id.header_back)
    ImageView ivBack;

    private String navTitle;
    private String amount; // 交易金额
    private ConditionVariable cv; //在EMV卡号确认的时候需要block一下
    private String cardNo; // 卡号
    private String transDate; // 日期


    private final static int READ_CARD_OK = 1; // 读卡成功
    //    private final static int READ_CARD_CANCEL = 2; // 取消读卡
//    private final static int READ_CARD_ERR = 3; // 读卡失败
//    private final static int READ_CARD_PAUSE = 4; // 读卡暂停
    public final static int CARD_NUM_CONFIRM = 10; //

//    private final static int KEYBOARD_GONE = 5; // 隐藏键盘
//    private final static int EDITTEXT_CARDNO = 6; // 卡号输入框
//    private final static int EDITTEXT_DATE = 7; // 日期输入框
//    private final static int EDITTEXT_CARDNO_ERR = 8; // 卡号输入错误
//    private final static int EDITTEXT_DATE_ERR = 9; // 日期输入错误

    public final static int CLSSLIGHTSTATUS_NOTREADY = 11; //not ready
    public final static int CLSSLIGHTSTATUS_IDLE = 12; //idle
    public final static int CLSSLIGHTSTATUS_READYFORTXN = 13; //ready for transaction
    public final static int CLSSLIGHTSTATUS_PROCESSING = 14; //processing
    public final static int CLSSLIGHTSTATUS_REMOVECARD = 15; //remove card
    public final static int CLSSLIGHTSTATUS_COMPLETE = 16; //complete
    public final static int CLSSLIGHTSTATUS_ERROR = 17; //error
    //    private final static int LIGHT_BLINK = 18;

    private boolean supportManual = false; // 是否支持手输

    private EReaderType readerType = null; // 读卡类型

    private Issuer matchedIssuer = null;

    /*
     * 外部调用参数
     */

    /**
     * 支持的寻卡类型{@link SearchMode}
     */
    private byte mode; // 寻卡模式
    private String prompt;//寻卡提示

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunSearchCardThread();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClssLightStatusEvent(ClssLightStatusEvent event) {
        switch (event.getStatus()) {
            case CARD_NUM_CONFIRM:
                onCardNumConform();
                break;
            case CLSSLIGHTSTATUS_NOTREADY:
                clssLight.setLightStatus(-1, BlinkImageView.OFF);
                break;
            case CLSSLIGHTSTATUS_IDLE:
                clssLight.setLightStatus(0, BlinkImageView.BLINK);
                break;
            case CLSSLIGHTSTATUS_READYFORTXN:
                clssLight.setLightStatus(0, BlinkImageView.BLINK);
                break;
            case CLSSLIGHTSTATUS_PROCESSING:
                clssLight.setLightStatus(1, BlinkImageView.ON);
                break;
            case CLSSLIGHTSTATUS_REMOVECARD:
                clssLight.setLightStatus(2, BlinkImageView.ON);
                break;
            case CLSSLIGHTSTATUS_COMPLETE:
                clssLight.setLightStatus(2, BlinkImageView.BLINK);
                break;
            case CLSSLIGHTSTATUS_ERROR:
                clssLight.setLightStatus(3, BlinkImageView.BLINK);
        }
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();

        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());

        // 显示金额
        try {
            amount = bundle.getString(EUIParamKeys.TRANS_AMOUNT.toString());
            if (!TextUtils.isEmpty(amount)) {
                amount = CurrencyConverter.convert(Long.parseLong(amount));
            }
        } catch (Exception e) {
            e.printStackTrace();
            amount = null;
        }

        // 寻卡方式
        try {
            mode = bundle.getByte(EUIParamKeys.CARD_SEARCH_MODE.toString(), SearchMode.SWIPE);
            // 是否支持手输卡号
            supportManual = SearchMode.contain(mode, SearchMode.KEYIN);

            if (Build.MODEL.contains("px7")) {
                mode = SearchMode.exclude(mode, SearchMode.WAVE);
            }
            readerType = SearchMode.toReaderType(mode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取日期
        try {
            transDate = bundle.getString(EUIParamKeys.TRANS_DATE.toString());
            if (transDate != null && transDate.length() > 0) {
                transDate = transDate.substring(0, 2) + "/" + transDate.substring(2, 4);
            }
        } catch (Exception e) {
            e.printStackTrace();
            transDate = null;
        }

        // 获取寻卡提醒
        prompt = bundle.getString(EUIParamKeys.SEARCH_CARD_PROMPT.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bankcard_pay;
    }

    @Override
    protected void initViews() {
        tvTitle.setText(navTitle);
        initDefaultViews();
    }

    // 默认寻卡界面初始化
    private void initDefaultViews() {
        if (SearchMode.contain(mode, SearchMode.WAVE)) {
            clssLight.setVisibility(View.VISIBLE);
            clssLight.setLightStatus(0, BlinkImageView.BLINK);
        }

        if (TextUtils.isEmpty(amount)) { // 余额查询不显示金额
            llAmount.setVisibility(View.INVISIBLE);
        } else {
            tvAmount.setText(amount);
        }

        if (supportManual) {
            edtCardNo.setText(BuildConfig.CARDNUMBER);
            TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (R.id.bank_card_number == v.getId()) {
                            processManualCardNo();
                        } else if (R.id.bank_card_expdate == v.getId()) {
                            processManualExpDate();
                        }
                    } else if (actionId == EditorInfo.IME_ACTION_NONE) {
                        onViewClicked(ivBack);
                    }
                    return false;
                }
            };

            edtCardNo.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            edtCardNo.addTextChangedListener(new CardNoWatcher());
            edtCardNo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(19 + 4)});// 4为卡号分隔符个数

            expDate.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            expDate.addTextChangedListener(new ExpDateWatcher());
            expDate.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4 + 1)});// 4为卡号分隔符个数

            edtCardNo.setOnEditorActionListener(onEditorActionListener);
            expDate.setOnEditorActionListener(onEditorActionListener);
        } else {
            edtCardNo.setFocusable(false);// 不支持手输入卡号
            expDate.setFocusable(false);// 不支持手输入卡号
        }

        readerTypesView.show(mode);
    }

    @Override
    protected void setListeners() {
    }

    @OnClick({R.id.header_back, R.id.ok_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.header_back:
                if (isSuccLeave) {
                    return;
                }
                onReadCardCancel();
            case R.id.ok_btn:
                // manual input case: get click event from IME_ACTION_DONE, the button is always hidden.
                if (pollingResult != null && pollingResult.getReaderType() == EReaderType.ICC) {
                    boolean enableTip = SysParamSp.Constant.YES.equals(SpManager.getSysParamSp().get(SysParamSp.EDC_SUPPORT_TIP));
                    if (enableTip && !navTitle.equals("Inquiry Balance")) {
                        long baseAmountLong = CurrencyConverter.parse(amount);
                        float percent = EmvListenerImpl.percent;
                        Intent intent = new Intent(SearchCardActivity.this, AdjustTipActivity.class);
                        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), navTitle);
                        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), String.valueOf(baseAmountLong));
                        intent.putExtra(EUIParamKeys.TIP_PERCENT.toString(), percent);
                        intent.putExtra(EUIParamKeys.CARD_MODE.toString(), EReaderType.ICC.toString());
                        startActivityForResult(intent, REQ_ADJUST_TIP);
                    } else {
                        EmvListenerImpl.cardNumConfigSucc();
                    }
                } else if (pollingResult != null && pollingResult.getReaderType() == EReaderType.MAG) {
                    processMag();
                }
                break;
            default:
                break;
        }
    }

    public final static int REQ_ADJUST_TIP = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_ADJUST_TIP:
                if (data != null) { //AET-82
                    String amount = data.getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
                    String tipAmount = data.getStringExtra(EUIParamKeys.TIP_AMOUNT.toString());
                    tvAmount.setText(amount);
                    EmvListenerImpl.cardNumConfigSucc(amount, tipAmount);
                    isSuccLeave = true; //AET-106
                }
                break;
            default:
                break;
        }
    }

    private PollingResult pollingResult;

    private SearchCardThread searchCardThread = null;

    private void RunSearchCardThread() {
        if (searchCardThread != null && searchCardThread.getState() == Thread.State.TERMINATED) {
            DalManager.getCardReaderHelper().stopPolling();
            searchCardThread.interrupt();
        }
        searchCardThread = new SearchCardThread();
        searchCardThread.start();
    }

    // 寻卡线程
    private class SearchCardThread extends Thread {

        @Override
        public void run() {
            try {
                if (readerType == null) {
                    return;
                }
                pollingResult = DalManager.getCardReaderHelper().polling(readerType, 60 * 1000);//寻卡
                DalManager.getCardReaderHelper().stopPolling();//停止轮询
                EOperationType optType = pollingResult.getOperationType();
                if (optType == EOperationType.CANCEL || optType == EOperationType.TIMEOUT) {
                    FinancialApplication.mApp.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onReadCardCancel();
                        }
                    });
                } else if (optType == EOperationType.PAUSE) {
                    FinancialApplication.mApp.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onReadCardPause();
                        }
                    });
                } else {
                    FinancialApplication.mApp.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onReadCardOk();
                        }
                    });
                }

            } catch (MagDevException | IccDevException | PiccDevException e) {
                e.printStackTrace();
                // 读卡失败处理
                FinancialApplication.mApp.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onReadCardError();
                    }
                });
            }

        }

    }

    // 卡号分割及输入长度检查
    private class CardNoWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0)
                return;

            String card = s.toString().replace(" ", "");
            card = card.replaceAll("(\\d{4}(?!$))", "$1 ");
            if (!card.equals(s.toString())) {
                edtCardNo.setText(card);
                edtCardNo.setSelection(card.length());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    }

    private class ExpDateWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0)
                return;
            String exp = s.toString().replace("/", "");
            exp = exp.replaceAll("(\\d{2}(?!$))", "$1/");
            if (!exp.equals(s.toString())) {
                expDate.setText(exp);
                expDate.setSelection(exp.length());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private void onReadCardOk() {
        //TODO case of allowing Fallback
        if (pollingResult.getReaderType() == EReaderType.MAG) {
            /*FIXME un-comment it if EMV is ready
            if (SearchMode.contain(mode,SearchMode.INSERT) && TrackUtils.isIcCard(pollingResult.getTrack2())) {
                Device.beepErr();
                ToastUtils.showShort(R.string.prompt_ic_card_input);
                mode=SearchMode.exclude(mode, SearchMode.SWIPE);
                readerType = SearchMode.toReaderType(mode);
                readerTypesView.show(mode);
                tvPrompt.setText(searchCardPrompt);
                RunNewSearchCardThread();
                return;
            }
            */
            Device.beepPrompt();
            // 有时刷卡成功，单没有磁道II，做一下防护
            String track2 = pollingResult.getTrack2();
            String track1 = pollingResult.getTrack1();
            String pan = TrackUtils.getPan(track2);
            String exp = TrackUtils.getExpDate(track2);
            String cardholder = TrackUtils.getHolderName(track1);

            //日期显示格式为（MMYY）
            if (exp == null || exp.length() != 4) {
                Device.beepErr();
                ToastUtils.showShort(R.string.prompt_ic_card_input);
                readerTypesView.show(mode);
                if (!TextUtils.isEmpty(prompt)) {
                    tvPrompt.setText(prompt);
                }
                RunSearchCardThread();
                return;
            }

            edtCardNo.setFocusable(false);
            edtCardNo.setText(PanUtils.separateWithSpace(pan));
            expDate.setVisibility(View.VISIBLE);
            expDate.setFocusable(false);

            //MM/YY
            exp = exp.substring(2, 4) + "/" + exp.substring(0, 2);
            expDate.setText(exp);

            //持卡人姓名为非空时才可见
            if (cardholder != null) {
                holderName.setVisibility(View.VISIBLE);
                holderName.setText(cardholder.trim());
            }

            readerTypesView.setVisibility(View.INVISIBLE);
            confirmBtnChange();
        } else if (pollingResult.getReaderType() == EReaderType.ICC) {
            //需要通过EMV才能获取到卡号等信息,所以先在EMV里面获取到信息，再到case CARD_NUM_CONFIRM中显示
            readerTypesView.setVisibility(View.INVISIBLE);
            edtCardNo.setFocusable(false);
            finish(new ActionResult(TransResult.SUCC, new CardInformation(SearchMode.INSERT)));
        } else if (pollingResult.getReaderType() == EReaderType.PICC) {
            readerTypesView.setVisibility(View.INVISIBLE);
            edtCardNo.setFocusable(false);
            clssLight.setVisibility(View.VISIBLE);
            finish(new ActionResult(TransResult.SUCC, new CardInformation(SearchMode.WAVE)));
        }
    }

    private void onReadCardPause() {

    }

    private void onReadCardCancel() {
        LogUtils.i("TAG", "SEARCH CARD CANCEL");
        DalManager.getCardReaderHelper().stopPolling();
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
    }

    private void onReadCardError() {
        ToastUtils.showShort(R.string.prompt_swipe_failed_please_retry);
        RunSearchCardThread();
    }

    private void onEditCardNo() {
        DalManager.getCardReaderHelper().setIsPause(true);
        DalManager.getCardReaderHelper().stopPolling();

        cardNo = edtCardNo.getText().toString().replace(" ", "");
        expDate.setVisibility(View.VISIBLE);
        expDate.setText("");
        expDate.requestFocus();
    }

    private void onEditCardNoError() {
        ToastUtils.showShort(R.string.prompt_card_num_err);
        edtCardNo.setText("");
        edtCardNo.requestFocus();
    }

    private void onEditDate() {
        runInputMerchantPwdAction();
    }

    private void onEditDateError() {
        ToastUtils.showShort(R.string.prompt_card_date_err);
        expDate.setText("");
        expDate.requestFocus();
    }

    private void onVerifyManualPan() {
        String date = expDate.getText().toString().replace("/", "");
        if (date.length() != 0) {
            date = date.substring(2) + date.substring(0, 2);// 将MMyy转换成yyMM
        }

        matchedIssuer = AcqManager.getInstance().findIssuerByPan(cardNo);
        if (matchedIssuer == null || !AcqManager.getInstance().isIssuerSupported(matchedIssuer)) {
            finish(new ActionResult(TransResult.ERR_CARD_UNSUPPORTED, null));
            return;
        }

        if (!matchedIssuer.isAllowManualPan()) {
            finish(new ActionResult(TransResult.ERR_UNSUPPORTED_FUNC, null));
            return;
        }

        if (!Issuer.validPan(matchedIssuer, cardNo)) {
            finish(new ActionResult(TransResult.ERR_CARD_INVALID, null));
            return;
        }

        if (!Issuer.validCardExpiry(matchedIssuer, date)) {
            finish(new ActionResult(TransResult.ERR_CARD_EXPIRED, null));
            return;
        }

        CardInformation cardInfo = new CardInformation(SearchMode.KEYIN, cardNo, date, matchedIssuer);
        finish(new ActionResult(TransResult.SUCC, cardInfo));
    }

    private void onCardNumConform() {
        isSuccLeave = false;
        edtCardNo.setFocusable(false);
        edtCardNo.setText(PanUtils.separateWithSpace(EmvListenerImpl.cardnum));

        //日期显示格式为（MM/YY）
        //AET-40
        if (EmvListenerImpl.expdate != null) {
            String exp = EmvListenerImpl.expdate.substring(2, 4) + "/" + EmvListenerImpl.expdate.substring(0, 2);
            expDate.setText(exp);
            expDate.setVisibility(View.VISIBLE);
        }

        //持卡人姓名为非空时才可见
        if (EmvListenerImpl.holdername != null) {
            holderName.setVisibility(View.VISIBLE);
            holderName.setText(EmvListenerImpl.holdername.trim());
        }
        //AET-120
        tickTimer.start();
        confirmBtnChange();
    }

    @Override
    protected boolean onKeyBackDown() {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onReadCardCancel();
            }
        });
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isSuccLeave) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 寻卡成功时，此界面还保留， 在后续界面切换时，还有机会跑到前台，此时按返回键，此activity finish，同时会有两个分支同时进行
    // 如果寻卡成功时， 此标志为true
    private boolean isSuccLeave = false;

    @Override
    public void finish(ActionResult result) {
        DalManager.getCardReaderHelper().setIsPause(true);
        DalManager.getCardReaderHelper().stopPolling();

        if (result.getRet() == TransResult.SUCC) {
            isSuccLeave = true;
        }
        super.finish(result);
    }

    // 填写信息校验
    private void processMag() {
        String content = edtCardNo.getText().toString().replace(" ", "");
        if (content == null || content.length() == 0) {
            FinancialApplication.mApp.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onReadCardError();
                }
            });
            return;
        }

        CardInformation cardInfo = null;
        if (pollingResult.getReaderType() == EReaderType.MAG) {
            matchedIssuer = AcqManager.getInstance().findIssuerByPan(TrackUtils.getPan(pollingResult.getTrack2()));
            if (matchedIssuer == null || !AcqManager.getInstance().isIssuerSupported(matchedIssuer)) {
                finish(new ActionResult(TransResult.ERR_CARD_UNSUPPORTED, null));
                return;
            }

            cardInfo = new CardInformation(SearchMode.SWIPE, pollingResult.getTrack1(), pollingResult.getTrack2(),
                    pollingResult.getTrack3(), TrackUtils.getPan(pollingResult.getTrack2()), matchedIssuer);

            if (!Issuer.validPan(matchedIssuer, cardInfo.getPan())) {
                finish(new ActionResult(TransResult.ERR_CARD_INVALID, null));
                return;
            }

            if (!Issuer.validCardExpiry(matchedIssuer, TrackUtils.getExpDate(cardInfo.getTrack2()))) {
                finish(new ActionResult(TransResult.ERR_CARD_EXPIRED, null));
                return;
            }
        }
        finish(new ActionResult(TransResult.SUCC, cardInfo));

    }

    private void processManualCardNo() {
        final String content = edtCardNo.getText().toString().replace(" ", "");
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (content.length() < 13) {
                    onEditCardNoError();
                } else {
                    onEditCardNo();
                }
            }
        });
    }

    private void processManualExpDate() {
        final String content = expDate.getText().toString().replace(" ", "");
        FinancialApplication.mApp.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (content == null || content.length() == 0) {
                    onEditDate();
                } else {
                    if (dateProcess(content)) {
                        onEditDate();
                    } else {
                        onEditDateError();
                    }
                }
            }
        });
    }

    private boolean dateProcess(String content) {
        final String mmYY = "MM/yy";
        if (content.length() != mmYY.length()) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(mmYY);
        try {
            dateFormat.setLenient(false);
            dateFormat.parse(content);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private void confirmBtnChange() {
        String content = edtCardNo.getText().toString();
        btnConfirm.setVisibility(TextUtils.isEmpty(content) ? View.INVISIBLE : View.VISIBLE);
    }

    private int retryTime = 3;

    private void runInputMerchantPwdAction() {
        final AAction aAction = TransContext.getInstance().getCurrentAction();
        final ActionInputPassword inputPasswordAction = new ActionInputPassword(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(6,
                        getString(R.string.prompt_merchant_pwd), null, false);
            }
        });

        inputPasswordAction.setEndListener(new AAction.ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                TransContext.getInstance().setCurrentAction(aAction);

                if (result.getRet() != TransResult.SUCC) {
                    finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                    return;
                }

                String data = EncUtils.SHA1((String) result.getData());
                if (!data.equals(SpManager.getSysParamSp().get(SysParamSp.SEC_MERCHANTPWD))) {
                    // AET-110
                    DialogUtils.showErrMessage(SearchCardActivity.this, getString(R.string.trans_password),
                            getString(R.string.err_password), null, Constants.FAILED_DIALOG_SHOW_TIME);
                    if (retryTime > 0) {
                        retryTime--;
                        FinancialApplication.mApp.runOnUiThreadDelay(new Runnable() {
                            @Override
                            public void run() {
                                onEditDate();
                            }
                        }, Constants.FAILED_DIALOG_SHOW_TIME * 1000);
                    } else {
                        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                    }
                    return;
                }

                FinancialApplication.mApp.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onVerifyManualPan();
                    }
                });
            }
        });

        inputPasswordAction.execute();
    }
}
