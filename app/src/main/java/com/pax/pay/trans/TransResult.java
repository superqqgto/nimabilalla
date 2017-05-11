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
package com.pax.pay.trans;

import com.pax.edc.R;
import com.pax.pay.utils.ContextUtils;

public class TransResult {
    /**
     * 交易成功
     */
    public static final int SUCC = 0;
    /**
     * 结算对账平,不需要批上送
     */
    public static final int SUCC_NOREQ_BATCH = 1;
    /**
     * 超时
     */
    public static final int ERR_TIMEOUT = -1;
    /**
     * fail to connect
     */
    public static final int ERR_CONNECT = -2;
    /**
     * 发送失败
     */
    public static final int ERR_SEND = -3;
    /**
     * 接收失败
     */
    public static final int ERR_RECV = -4;
    /**
     * 打包失败
     */
    public static final int ERR_PACK = -5;
    /**
     * 解包失败
     */
    public static final int ERR_UNPACK = -6;
    /**
     * 非法包
     */
    public static final int ERR_BAG = -7;
    /**
     * 解包mac错
     */
    public static final int ERR_MAC = -8;
    /**
     * 处理码不一致
     */
    public static final int ERR_PROC_CODE = -9;
    /**
     * 消息类型不一致
     */
    public static final int ERR_MSG = -10;
    /**
     * 交易金额不符
     */
    public static final int ERR_TRANS_AMT = -11;
    /**
     * 流水号不一致
     */
    public static final int ERR_TRACE_NO = -12;
    /**
     * 终端号不一致
     */
    public static final int ERR_TERM_ID = -13;
    /**
     * 商户号不一致
     */
    public static final int ERR_MERCH_ID = -14;
    /**
     * 无交易
     */
    public static final int ERR_NO_TRANS = -15;
    /**
     * 无原始交易
     */
    public static final int ERR_NO_ORIG_TRANS = -16;
    /**
     * 此交易已撤销
     */
    public static final int ERR_HAS_VOIDED = -17;
    /**
     * 此交易不可撤销
     */
    public static final int ERR_VOID_UNSUPPORTED = -18;
    /**
     * 打开通讯口错误
     */
    public static final int ERR_COMM_CHANNEL = -19;
    /**
     * 失败
     */
    public static final int ERR_HOST_REJECT = -20;
    /**
     * 交易终止（终端不需要提示信息）
     */
    public static final int ERR_ABORTED = -21;
    /**
     * 交易终止（User cancel）
     */
    public static final int ERR_USER_CANCEL = -22;
    /**
     * 预处理相关 交易笔数超限，立即结算
     */
    public static final int ERR_NEED_SETTLE_NOW = -23;
    /**
     * 预处理相关 交易笔数超限，稍后结算
     */
    public static final int ERR_NEED_SETTLE_LATER = -24;
    /**
     * 预处理相关 存储空间不足
     */
    public static final int ERR_NO_FREE_SPACE = -25;
    /**
     * 预处理相关 终端不支持该交易
     */
    public static final int ERR_NOT_SUPPORT_TRANS = -26;
    /**
     * 卡号不一致
     */
    public static final int ERR_CARD_NO = -27;
    /**
     * 密码错误
     */
    public static final int ERR_PASSWORD = -28;
    /**
     * 参数错误
     */
    public static final int ERR_PARAM = -29;

    /**
     * 终端批上送未完成
     */
    public static final int ERR_BATCH_UP_NOT_COMPLETED = -31;
    /**
     * 金额超限
     */
    public static final int ERR_AMOUNT = -33;
    /**
     * 平台批准卡片拒绝
     */
    public static final int ERR_CARD_DENIED = -34;
    /**
     * 此交易不可调整
     */
    public static final int ERR_ADJUST_UNSUPPORTED = -36;
    /**
     * 此交易不支持该卡
     */
    public static final int ERR_CARD_UNSUPPORTED = -37;

    /**
     * expired card
     */
    public static final int ERR_CARD_EXPIRED = -38;

    /**
     * invalid card no
     */
    public static final int ERR_CARD_INVALID = -39;

    /**
     * Unsupported function
     */
    public static final int ERR_UNSUPPORTED_FUNC = -40;

    /**
     * 非接设备预处理失败
     */
    public static final int ERR_CLSSPROPROC = -41;


    public static final int ERR_PREAUTH_COMP_UNSUPPORTED = -44;

    /**
     * 后台应答不为 00  //added by Zac
     */
    public static final int ERR_REAPONSE = -42;

    /**
     * 往ped里存密钥失败
     */
    public static final int ERR_TMK_TO_PED = -43;

    public static String getMessage(int ret) {
        String message = "";
        switch (ret) {
            case SUCC:
                message = ContextUtils.getString(R.string.dialog_trans_succ);
                break;
            case ERR_TIMEOUT:
                message = ContextUtils.getString(R.string.err_timeout);
                break;
            case ERR_CONNECT:
                message = ContextUtils.getString(R.string.err_connect);
                break;
            case ERR_SEND:
                message = ContextUtils.getString(R.string.err_send);
                break;
            case ERR_RECV:
                message = ContextUtils.getString(R.string.err_recv);
                break;
            case ERR_PACK:
                message = ContextUtils.getString(R.string.err_pack);
                break;
            case ERR_UNPACK:
                message = ContextUtils.getString(R.string.err_unpack);
                break;
            case ERR_BAG:
                message = ContextUtils.getString(R.string.err_bag);
                break;
            case ERR_MAC:
                message = ContextUtils.getString(R.string.err_mac);
                break;
            case ERR_PROC_CODE:
                message = ContextUtils.getString(R.string.err_proc_code);
                break;
            case ERR_MSG:
                message = ContextUtils.getString(R.string.err_msg);
                break;
            case ERR_TRANS_AMT:
                message = ContextUtils.getString(R.string.err_trans_amt);
                break;
            case ERR_TRACE_NO:
                message = ContextUtils.getString(R.string.err_trace_no);
                break;
            case ERR_TERM_ID:
                message = ContextUtils.getString(R.string.err_term_id);
                break;
            case ERR_MERCH_ID:
                message = ContextUtils.getString(R.string.err_merch_id);
                break;
            case ERR_NO_TRANS:
                message = ContextUtils.getString(R.string.err_no_trans);
                break;
            case ERR_NO_ORIG_TRANS:
                message = ContextUtils.getString(R.string.err_no_orig_trans);
                break;
            case ERR_HAS_VOIDED:
                message = ContextUtils.getString(R.string.err_has_voided);
                break;
            case ERR_VOID_UNSUPPORTED:
                message = ContextUtils.getString(R.string.err_un_voided);
                break;
            case ERR_COMM_CHANNEL:
                message = ContextUtils.getString(R.string.err_comm_channel);
                break;
            case ERR_HOST_REJECT:
                message = ContextUtils.getString(R.string.err_host_reject);
                break;
            case ERR_USER_CANCEL:
                message = ContextUtils.getString(R.string.err_user_cancel);
                break;
            case ERR_NEED_SETTLE_NOW:
                message = ContextUtils.getString(R.string.err_need_settle_now);
                break;
            case ERR_NEED_SETTLE_LATER:
                message = ContextUtils.getString(R.string.err_need_settle_later);
                break;
            case ERR_NO_FREE_SPACE:
                message = ContextUtils.getString(R.string.err_no_free_space);
                break;
            case ERR_NOT_SUPPORT_TRANS:
                message = ContextUtils.getString(R.string.err_unsupported_trans);
                break;
            case ERR_BATCH_UP_NOT_COMPLETED:
                message = ContextUtils.getString(R.string.err_batch_up_break_need_continue);
                break;
            case ERR_CARD_NO:
                message = ContextUtils.getString(R.string.err_original_cardno);
                break;
            case ERR_PASSWORD:
                message = ContextUtils.getString(R.string.err_password);
                break;
            case ERR_PARAM:
                message = ContextUtils.getString(R.string.err_param);
                break;
            case ERR_AMOUNT:
                message = ContextUtils.getString(R.string.err_amount);
                break;
            case ERR_CARD_DENIED:
                message = ContextUtils.getString(R.string.err_card_denied);
                break;
            case ERR_ADJUST_UNSUPPORTED:
                message = ContextUtils.getString(R.string.err_unadjusted);
                break;
            case ERR_CARD_UNSUPPORTED:
                message = ContextUtils.getString(R.string.err_card_unsupported);
                break;
            case ERR_CARD_EXPIRED:
                message = ContextUtils.getString(R.string.err_expired_card);
                break;
            case ERR_CARD_INVALID:
                message = ContextUtils.getString(R.string.err_card_pan);
                break;
            case ERR_UNSUPPORTED_FUNC:
                message = ContextUtils.getString(R.string.err_unsupported_func);
                break;
            case ERR_PREAUTH_COMP_UNSUPPORTED:
                message = ContextUtils.getString(R.string.err_only_preauth_support);
                break;
            case ERR_REAPONSE:
                message = ContextUtils.getString(R.string.err_response);
                break;
            case ERR_TMK_TO_PED:
                message = ContextUtils.getString(R.string.err_write_to_ped);
                break;
            case ERR_CLSSPROPROC:
                message = ContextUtils.getString(R.string.err_clee_preproc_fail);
                break;
            default:
                message = ContextUtils.getString(R.string.err_undefine) + "[" + ret + "]";
                break;
        }
        return message;
    }
}
