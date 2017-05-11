/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Auth: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.receipt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.pax.abl.utils.PanUtils;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.enums.ETransResult;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.gl.imgprocessing.IImgProcessing.IPage;
import com.pax.gl.imgprocessing.IImgProcessing.IPage.EAlign;
import com.pax.manager.AcqManager;
import com.pax.manager.neptune.DalManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.BaseTransData.*;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.TimeConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * receipt generator
 *
 * @author Steven.W
 */
@SuppressLint("SimpleDateFormat")
public class ReceiptGeneratorTrans implements IReceiptGenerator {

    int receiptNo = 0;
    private TransData transData;
    private boolean isRePrint = false;
    private int receiptMax = 0;

    /**
     * @param transData        ：transData
     * @param currentReceiptNo : currentReceiptNo
     * @param receiptMax       ：generate which one, start from 0
     * @param isReprint        ：is reprint?
     */
    public ReceiptGeneratorTrans(TransData transData, int currentReceiptNo, int receiptMax, boolean isReprint) {
        this.transData = transData;
        this.receiptNo = currentReceiptNo;
        this.isRePrint = isReprint;
        this.receiptMax = receiptMax;
    }

    private boolean isPrintPreview = false;

    public ReceiptGeneratorTrans(TransData transData, int currentReceiptNo, int receiptMax, boolean isReprint, boolean isPrintPreview) {
        this(transData, currentReceiptNo, receiptMax, isReprint);
        this.isPrintPreview = isPrintPreview;
    }

    @Override
    public Bitmap generateBitmap() {
        boolean isPrintSign = false;
        // 生成第几张凭单不合法时， 默认0
        if (receiptNo > receiptMax) {
            receiptNo = 0;
        }
        // the first copy print signature, if three copies, the second copy should print signature too
        if (receiptNo == 0 || ((receiptMax == 3) && receiptNo == 1)) {
            isPrintSign = true;
        }

        IPage page = Device.generatePage();
        Context context = FinancialApplication.mApp;
        //page.setTypeFace(TYPE_FACE);
        // transaction type
        ETransType transType = transData.getTransType();

        SysParamSp sysParam = SpManager.getSysParamSp();
        Acquirer acquirer = AcqManager.getInstance().getCurAcq();
        String temp = "";
        String temp1 = "";
        String temp2 = "";

        // title
        //page.addLine().addUnit(getImageFromAssetsFile("pax_logo_normal.png"), EAlign.CENTER);
        page.addLine().addUnit(getImageFromAssetsFile("Base24_logo.png"), EAlign.CENTER);
        page.addLine().addUnit("-----------------------------------------------------", FONT_NORMAL, EAlign.CENTER);

        // merchant name
        //page.addLine().addUnit(context.getString(R.string.receipt_merchant_name) + sysParam.get(SysParamSp.EDC_MERCHANT_NAME_EN),FONT_NORMAL, EAlign.LEFT);

        // merchant ID
        page.addLine().addUnit(context.getString(R.string.receipt_merchant_code), FONT_SMALL, (float) 4)
                .addUnit(acquirer.getMerchantId(), FONT_NORMAL, EAlign.RIGHT, (float) 6);

        // terminal ID/operator ID
        page.addLine()
                .addUnit(context.getString(R.string.receipt_terminal_code_space), FONT_SMALL, (float) 4)
                .addUnit(acquirer.getTerminalId(), FONT_NORMAL, EAlign.RIGHT, (float) 4);
        page.addLine().addUnit(" ", FONT_NORMAL);

        // card NO
        if (transType == ETransType.PREAUTH) {
            temp = transData.getPan();
        } else {
            temp = PanUtils.maskCardNo(transData.getPan(), transData.getIssuer().getPanMaskPattern());
            if (!transData.isOnlineTrans()) {
                temp = transData.getPan();
            }
        }

        TransData.EnterMode enterMode = transData.getEnterMode();
        if (enterMode == EnterMode.MANUAL) {
            temp += " /M";
        } else if (enterMode == EnterMode.SWIPE) {
            temp += " /S";
        } else if (enterMode == EnterMode.INSERT) {
            temp += " /I";
        } else if (enterMode == EnterMode.CLSS) {
            temp += " /C";
        }

        temp1 = transData.getExpDate();
        if (temp1 != null && temp1.length() != 0) {
            if (transData.getIssuer().isRequireMaskExpiry()) {
                temp1 = "**/**";
            } else {
                temp1 = temp1.substring(2) + "/" + temp1.substring(0, 2);// 将yyMM转换成MMyy
            }
        } else {
            temp1 = "";
        }

        //card NO/expiry date
        page.addLine().addUnit(context.getString(R.string.receipt_card_no) + "/" + context.getString(R.string.receipt_card_date),
                FONT_SMALL);
        page.addLine().addUnit(temp, FONT_BIG, EAlign.LEFT, (float) 4.6)
                .addUnit(temp1, FONT_BIG, EAlign.RIGHT, (float) 1.0);

        // transaction type
        temp = getEnTransType(transType);
        if (temp != null && context.getResources().getConfiguration().locale.getCountry().equals("CN")) {// 根据字数长度设置不同的打印样式
            if (transType == ETransType.SALE || transType == ETransType.VOID || transType == ETransType.REFUND
                    || transType == ETransType.PREAUTH || transType == ETransType.VOIDREFUND) {
                page.addLine().addUnit(context.getString(R.string.receipt_trans_type), FONT_SMALL);
                page.addLine().addUnit(transType.getTransName() + "(" + temp + ")", FONT_BIG);
            }
        } else {
            page.addLine().addUnit(context.getString(R.string.receipt_trans_type), FONT_SMALL);
            page.addLine().addUnit(transType.getTransName(), FONT_BIG);
        }

        /***
         // transaction NO/expiry date
         temp = transData.getExpDate();
         if (temp != null && temp.length() != 0) {
         temp = temp.substring(2) + "/" + temp.substring(0, 2);// transfer yyMM into MMyy
         } else {
         temp = "";
         }

         temp2 = Component.getPaddedNumber(transData.getTraceNo(), 6);

         page.addLine().addUnit(context.getString(R.string.receipt_trans_no), FONT_NORMAL, (float) 4)
         //.addUnit(temp2, FONT_BIG, (float) 1.12)
         .addUnit(context.getString(R.string.receipt_card_date) , FONT_NORMAL, (float) 4);
         page.addLine().addUnit(temp2, FONT_BIG, (float) 2)
         .addUnit(temp, FONT_NORMAL, (float) 2);

         ***/

        // batch NO/transaction N0
        temp2 = Component.getPaddedNumber(transData.getTraceNo(), 6);

        page.addLine()
                .addUnit(context.getString(R.string.receipt_batch_num_colon), FONT_SMALL)
                .addUnit(context.getString(R.string.receipt_trans_no), FONT_SMALL, EAlign.RIGHT);
        page.addLine()
                .addUnit(Component.getPaddedNumber(transData.getBatchNo(), 6), FONT_BIG)
                .addUnit(temp2, FONT_BIG, EAlign.RIGHT);


        /*
         // auth code/issuer
         String authCode = transData.getAuthCode();
         page.addLine()
         .addUnit(context.getString(R.string.receipt_auth_code) , FONT_NORMAL)
         .addUnit(context.getString(R.string.receipt_card_acquire) , FONT_NORMAL);
         page.addLine()
         .addUnit((authCode == null ? "" : authCode), FONT_NORMAL)
         .addUnit(transData.getAcqCode(), FONT_NORMAL);
         */

        // date/time
        String formattedDate = TimeConverter.convert(transData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                Constants.TIME_PATTERN_DISPLAY);
        page.addLine().addUnit(context.getString(R.string.receipt_date), FONT_SMALL);
        page.addLine().addUnit(formattedDate, FONT_BIG);

        // reference NO
        temp = transData.getRefNo();
        if (temp == null) {
            temp = "";
        }
        temp1 = transData.getAuthCode();

        page.addLine().addUnit(context.getString(R.string.receipt_ref_no), FONT_SMALL).addUnit(context.getString(R.string.receipt_app_code), FONT_SMALL, EAlign.RIGHT);
        page.addLine().addUnit(temp, FONT_BIG, (float) 4).addUnit(temp1, FONT_BIG, EAlign.RIGHT, (float) 3);
        page.addLine().addUnit(" ", FONT_NORMAL);

        //base amount
        if (transType == ETransType.SALE) {
            long base = Long.parseLong(transData.getAmount()) - Long.parseLong(transData.getTipAmount());
            temp = CurrencyConverter.convert(base, transData.getCurrency());
            page.addLine().addUnit(context.getString(R.string.receipt_amount_base), FONT_BIG, (float) 4)
                    .addUnit(temp, FONT_BIG, EAlign.RIGHT, (float) 9);
        }

        //tip
        if ((transType == ETransType.SALE) || (transType == ETransType.OFFLINE_TRANS_SEND)) {
            long tips = Long.parseLong(transData.getTipAmount());
            temp = CurrencyConverter.convert(tips, transData.getCurrency());
            page.addLine().addUnit(context.getString(R.string.receipt_amount_tip), FONT_BIG, (float) 4)
                    .addUnit(temp, FONT_BIG, EAlign.RIGHT, (float) 6);
            page.addLine().addUnit(" ", FONT_NORMAL);
        }

        // amount
        long amount = Long.parseLong(transData.getAmount());
        temp = CurrencyConverter.convert(amount, transData.getCurrency());
        page.addLine().addUnit(context.getString(R.string.receipt_amount), FONT_BIG, (float) 5)
                .addUnit(temp, FONT_BIG, EAlign.RIGHT, (float) 9);
        page.addLine().addUnit(" ", FONT_NORMAL);

        // comment
        //page.addLine().addUnit(context.getString(R.string.receipt_comment), FONT_NORMAL);

        if (transType == ETransType.VOID || transType == ETransType.VOIDREFUND) {
            page.addLine().addUnit(
                    context.getString(R.string.receipt_orig_trans_no)
                            + Component.getPaddedNumber(transData.getOrigTransNo(), 6), FONT_NORMAL);
        }

        if (enterMode == EnterMode.INSERT || enterMode == EnterMode.CLSS) {
            if (transType == ETransType.SALE || transType == ETransType.PREAUTH) {
                if (transData.getEmvResult() == ETransResult.OFFLINE_APPROVED.ordinal()) {
                    page.addLine().addUnit("TC:", FONT_NORMAL)
                            .addUnit(transData.getTc(), FONT_NORMAL, EAlign.RIGHT);
                } else {
                    page.addLine().addUnit("ARQC:", FONT_NORMAL, (float) 4)
                            .addUnit(transData.getArqc(), FONT_NORMAL, EAlign.RIGHT, (float) 7);
                }

                page.addLine().addUnit("TSI:  " + transData.getTsi(), FONT_NORMAL).addUnit("ATC:  " + transData.getAtc(), FONT_NORMAL, EAlign.RIGHT);
                page.addLine().addUnit("TVR:", FONT_NORMAL).addUnit(transData.getTvr(), FONT_NORMAL, EAlign.RIGHT);
                page.addLine().addUnit("APP LABEL:", FONT_NORMAL).addUnit(transData.getEmvAppLabel(), FONT_NORMAL, EAlign.RIGHT);
                page.addLine().addUnit("AID:", FONT_NORMAL).addUnit(transData.getAid(), FONT_NORMAL, EAlign.RIGHT);
            }
        }

        String pinFreeAmt = sysParam.get(SysParamSp.QUICK_PASS_TRANS_PIN_FREE_AMOUNT);
        Long pinFreeAmtLong = Long.parseLong(pinFreeAmt);
        String signFreeAmt = sysParam.get(SysParamSp.QUICK_PASS_TRANS_SIGN_FREE_AMOUNT);
        Long signFreeAmtLong = Long.parseLong(signFreeAmt);
        boolean isPinFree = transData.isPinFree();
        boolean isSignFree = transData.isSignFree();

        if (isPinFree && isSignFree) {// sign free and pin free
            if (pinFreeAmtLong > signFreeAmtLong) {
                page.addLine().addUnit(
                        context.getString(R.string.receipt_amount_prompt_start)
                                + CurrencyConverter.convert(signFreeAmtLong, transData.getCurrency())
                                + context.getString(R.string.receipt_amount_prompt_end), FONT_NORMAL, EAlign.LEFT);
            } else {
                page.addLine().addUnit(
                        context.getString(R.string.receipt_amount_prompt_start)
                                + CurrencyConverter.convert(pinFreeAmtLong, transData.getCurrency())
                                + context.getString(R.string.receipt_amount_prompt_end), FONT_NORMAL, EAlign.LEFT);
            }
        } else if (isSignFree) {// only sign free
            page.addLine().addUnit(
                    context.getString(R.string.receipt_amount_prompt_start)
                            + CurrencyConverter.convert(signFreeAmtLong, transData.getCurrency())
                            + context.getString(R.string.receipt_amount_prompt_end_sign), FONT_NORMAL, EAlign.LEFT);

        } else if (isPinFree) {// pin free
            if (!transData.isCDCVM()) {
                page.addLine().addUnit(
                        context.getString(R.string.receipt_amount_prompt_start)
                                + CurrencyConverter.convert(pinFreeAmtLong, transData.getCurrency())
                                + context.getString(R.string.receipt_amount_prompt_end_pin), FONT_NORMAL,
                        EAlign.LEFT);
            }
        }
        if (isRePrint) {
            page.addLine().addUnit(context.getString(R.string.receipt_print_again), FONT_BIG);

            if (transData.getTransState().equals(TransData.ETransStatus.VOIDED)) {
                page.addLine().addUnit(context.getString(R.string.receipt_had_void), FONT_BIG);
            }
        }
        if (!isSignFree && isPrintSign) {
            page.addLine().addUnit(context.getString(R.string.receipt_sign), FONT_SMALL, EAlign.LEFT);
            Bitmap bitmap = loadSignature(transData);
            if (bitmap != null) {
                page.addLine().addUnit(loadSignature(transData), EAlign.CENTER);
            } else {
                page.addLine().addUnit("\n", FONT_NORMAL);
            }

        }
        page.addLine().addUnit("--------------------------------------------------------", FONT_NORMAL, EAlign.CENTER);
        page.addLine().addUnit(context.getString(R.string.receipt_verify), FONT_SMALL, EAlign.CENTER);

        if (receiptMax == 3) {
            if (receiptNo == 0) {
                page.addLine()
                        .addUnit(context.getString(R.string.receipt_stub_acquire), FONT_NORMAL, EAlign.CENTER, (float) 1);
                page.addLine()
                        .addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2);
                // .addUnit(context.getString(R.string.receipt_stub_acquire), FONT_NORMAL, EAlign.RIGHT, (float) 1);
            } else if (receiptNo == 1) {
                page.addLine()
                        .addUnit(context.getString(R.string.receipt_stub_merchant), FONT_NORMAL, EAlign.CENTER, (float) 1);
                page.addLine()
                        .addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2);
                // .addUnit(context.getString(R.string.receipt_stub_merchant), FONT_NORMAL, EAlign.RIGHT,(float) 1);
            } else {
                page.addLine().addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2)
                        .addUnit(context.getString(R.string.receipt_stub_user), FONT_NORMAL, EAlign.RIGHT, (float) 1);
            }
        } else {
            if (receiptNo == 0) {
                page.addLine()
                        .addUnit(context.getString(R.string.receipt_stub_merchant), FONT_NORMAL, EAlign.CENTER, (float) 1);
                page.addLine()
                        .addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2);
                //.addUnit(context.getString(R.string.receipt_stub_merchant), FONT_NORMAL, EAlign.RIGHT, (float) 1);
            } else {
                page.addLine()
                        .addUnit(context.getString(R.string.receipt_stub_user), FONT_NORMAL, EAlign.RIGHT, (float) 1);
                page.addLine().addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2);
                // .addUnit(context.getString(R.string.receipt_stub_user), FONT_NORMAL, EAlign.RIGHT, (float) 1);
            }
        }

        String commType = SpManager.getSysParamSp().get(SysParamSp.APP_COMM_TYPE);
        if (SysParamSp.Constant.COMMTYPE_DEMO.equals(commType)) {
            page.addLine().addUnit(context.getString(R.string.demo_mode), FONT_NORMAL, EAlign.CENTER);
        }

        /***
         // merchant copy print bar code
         if (((receiptMax != 3) && receiptNo == 0) || ((receiptMax == 3) && receiptNo == 1)) {
         generateBarCode(transData, page);
         }
         ***/

        if (!isPrintPreview) {
            page.addLine().addUnit("\n\n\n\n", FONT_NORMAL);
        }

        IImgProcessing imgProcessing = GlManager.getImgProcessing();
        return imgProcessing.pageToBitmap(page, 384);
    }

    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = ContextUtils.getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;

    }

    private Bitmap loadSignature(TransData transData) {
        byte[] signData = transData.getSignData();
        if (signData == null) {
            return null;
        }
        return GlManager.getImgProcessing().jbigToBitmap(signData);
    }

    /***
     * // generate bar code
     * private void generateBarCode(TransData transData, IPage page) {
     * <p>
     * ETransType transType = ETransType.valueOf(transData.getTransType());
     * if (transType == ETransType.PREAUTH || transType == ETransType.SALE) {
     * if (transData.getTransState().equals(ETransStatus.VOIDED)) {
     * return;
     * }
     * try {
     * JSONObject json = new JSONObject();
     * <p>
     * json.put("authCode", transData.getAuthCode());
     * json.put("date", transData.getDateTime().substring(4, 8));
     * json.put("transNo", transData.getTraceNo());
     * json.put("refNo", transData.getRefNo());
     * <p>
     * JSONArray array = new JSONArray();
     * array.put(json);
     * <p>
     * page.addLine().addUnit(
     * FinancialApplication.gl.getImgProcessing().generateBarCode(array.toString(), 230, 230,
     * BarcodeFormat.QR_CODE), EAlign.CENTER);
     * } catch (JSONException e) {
     * e.printStackTrace();
     * }
     * <p>
     * }
     * }
     ***/

    private String getTerminalandAppVersion() {

        Map<ETermInfoKey, String> map = DalManager.getSys().getTermInfo();
        return map.get(ETermInfoKey.MODEL) + " " + FinancialApplication.versionName;
    }

    private String getEnTransType(ETransType transType) {
        if (transType == ETransType.SALE) {
            return "SALE";
        } else if (transType == ETransType.VOID) {
            return "VOIDED";
        } else if (transType == ETransType.REFUND) {
            return "REFUND";
        } else if (transType == ETransType.PREAUTH) {
            return "AUTH";
        } else if (transType == ETransType.VOIDREFUND) {
            return "VOID REFUND";
        }
        return null;
    }

    @Override
    public String generateString() {
        return "Card No:" + transData.getPan() + "\nTrans Type:" + transData.getTransType().toString()
                + "\nAmount:" + CurrencyConverter.convert(Long.parseLong(transData.getAmount()), transData.getCurrency())
                + "\nTip:" + CurrencyConverter.convert(Long.parseLong(transData.getTipAmount()), transData.getCurrency())
                + "\nTransData:" + transData.getDateTime();
    }
}
