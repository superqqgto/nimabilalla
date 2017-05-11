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
package com.pax.pay.trans.receipt;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.gl.imgprocessing.IImgProcessing.IPage;
import com.pax.gl.imgprocessing.IImgProcessing.IPage.EAlign;
import com.pax.manager.AcqManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;

/**
 * total generator
 *
 * @author Steven.W
 */
@SuppressLint("SimpleDateFormat")
class ReceiptGeneratorTotal implements IReceiptGenerator {
    private String title;
    private String result;
    private TransTotal total;

    /**
     * @param title      ：title
     * @param result     : result
     * @param transTotal ：transTotal
     */
    public ReceiptGeneratorTotal(String title, String result, TransTotal transTotal) {
        this.title = title;
        this.result = result;
        this.total = transTotal;
    }

    @Override
    public Bitmap generateBitmap() {
        IPage page = Device.generatePage();
        Acquirer acquirer = AcqManager.getInstance().getCurAcq();

        // title
        page.addLine().addUnit(title, FONT_BIG, EAlign.CENTER);

        // merchant ID
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_merchant_code),
                FONT_SMALL, (float) 4)
                .addUnit(acquirer.getMerchantId(), FONT_NORMAL, EAlign.RIGHT, (float) 6);

        // terminal ID/operator ID
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_terminal_code_space), FONT_SMALL, (float) 4)
                .addUnit(acquirer.getTerminalId(), FONT_NORMAL, EAlign.RIGHT, (float) 4);

        page.addLine().addUnit(" ", FONT_NORMAL);

        // batch NO
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_batch_num_space), FONT_BIG)
                .addUnit(Component.getPaddedNumber(acquirer.getCurrBatchNo(), 6),
                        FONT_BIG, EAlign.RIGHT);

        // date/time
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_date), FONT_SMALL, (float) 4)
                .addUnit(Device.getTime(Constants.TIME_PATTERN_DISPLAY), FONT_BIG, EAlign.RIGHT, (float) 6);

        // type/count/amount
        //AET-124
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_type), FONT_SMALL, (float) 2.5)
                .addUnit(ContextUtils.getString(R.string.receipt_count), FONT_SMALL, EAlign.CENTER, (float) 1.5)
                .addUnit(ContextUtils.getString(R.string.receipt_amount), FONT_SMALL, EAlign.RIGHT, (float) 3);

        if(result != null)
            page.addLine().addUnit(result, FONT_SMALL);

        // sale
        page.addLine()
                .addUnit(ContextUtils.getString(R.string.trans_sale), FONT_SMALL, (float) 2.5)
                .addUnit(total.getSaleTotalNum() + "", FONT_SMALL, EAlign.CENTER, (float) 1.5)
                .addUnit(CurrencyConverter.convert(total.getSaleTotalAmt()), FONT_SMALL, EAlign.RIGHT, (float) 3);

        // offline

        // refund
        page.addLine()
                .addUnit(ContextUtils.getString(R.string.trans_refund), FONT_SMALL, (float) 2.5)
                .addUnit(total.getRefundTotalNum() + "", FONT_SMALL, EAlign.CENTER, (float) 1.5)
                .addUnit(CurrencyConverter.convert(total.getRefundTotalAmt()), FONT_SMALL, EAlign.RIGHT, (float) 3);

        // AET-66
        page.addLine()
                .addUnit(ContextUtils.getString(R.string.settle_total_void_sale), FONT_SMALL, (float) 2.5)
                .addUnit(total.getSaleVoidTotalNum() + "", FONT_SMALL, EAlign.CENTER, (float) 1.5)
                .addUnit(CurrencyConverter.convert(total.getSaleVoidTotalAmt()), FONT_SMALL, EAlign.RIGHT, (float) 3);

        page.addLine()
                .addUnit(ContextUtils.getString(R.string.settle_total_void_refund), FONT_SMALL, (float) 2.5)
                .addUnit(total.getRefundVoidTotalNum() + "", FONT_SMALL, EAlign.CENTER, (float) 1.5)
                .addUnit(CurrencyConverter.convert(total.getRefundVoidTotalAmt()), FONT_SMALL, EAlign.RIGHT, (float) 3);

        String commType = SpManager.getSysParamSp().get(SysParamSp.APP_COMM_TYPE);
        if (SysParamSp.Constant.COMMTYPE_DEMO.equals(commType)) {
            page.addLine().addUnit(ContextUtils.getString(R.string.demo_mode), FONT_NORMAL, EAlign.CENTER);
        }

        page.addLine().addUnit("\n\n\n\n", FONT_NORMAL);
        IImgProcessing imgProcessing = GlManager.getImgProcessing();
        return imgProcessing.pageToBitmap(page, 384);
    }

    @Override
    public String generateString() {
        return null;
    }
}
