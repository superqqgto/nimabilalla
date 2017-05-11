package com.pax.pay.trans.model;

/**
 * Created by huangmuhua on 2017/3/28.
 */

public class BaseTransData {

    public final static String BATCHNO_FIELD_NAME = "batch_no";
    public final static String OFFLINE_STATE_FIELD_NAME = "offline_state";

    public final static String ID_FIELD_NAME = "id";
    public final static String TRACENO_FIELD_NAME = "trace_no";
    public final static String AUTHCODE_FIELD_NAME = "auth_code";
    public final static String TYPE_FIELD_NAME = "type";
    public final static String STATE_FIELD_NAME = "state";
    public final static String AMOUNT_FIELD_NAME = "amount";
    public final static String REVERSAL_FIELD_NAME = "REVERSAL";

    public static final String DUP_REASON_NO_RECV = "98";
    public static final String DUP_REASON_MACWRONG = "A0";
    public static final String DUP_REASON_OTHERS = "06";

    /**
     * 交易状态
     *
     * @author Steven.W
     */
    public enum ETransStatus {
        /**
         * 正常
         */
        NORMAL,
        /**
         * 已撤销
         */
        VOIDED,
        /**
         * 已调整
         */
        ADJUSTED
    }

    /* 脱机上送失败原因 */

    public enum OfflineStatus {
        /**
         * offline not sent
         */
        OFFLINE_NOT_SENT(0),
        /**
         * offline sent
         */
        OFFLINE_SENT(1),
        /**
         * 脱机上送失败
         */
        OFFLINE_ERR_SEND(2),
        /**
         * 脱机上送平台拒绝(返回码非00)
         */
        OFFLINE_ERR_RESP(3),
        /**
         * 脱机上送未知失败原因
         */
        OFFLINE_ERR_UNKNOWN(0xff),;

        private final int value;

        OfflineStatus(int value) {
            this.value = value;
        }
    }

    /**
     * 电子签名上送状态
     */
    public enum SignSendStatus {
        /**
         * 未上送
         */
        SEND_SIG_NO,
        /**
         * 上送成功
         */
        SEND_SIG_SUCC,
        /**
         * 上送失败
         */
        SEND_SIG_ERR,
    }

    public enum EnterMode {
        /**
         * 手工输入
         */
        MANUAL,
        /**
         * 刷卡
         */
        SWIPE,
        /**
         * 插卡
         */
        INSERT,
        /**
         * IC卡回退
         */
        FALLBACK,
        /**
         * 非接支付
         */
        CLSS,
    }

    public enum ReversalStatus {
        /**
         *
         */
        NORMAL,
        /**
         *
         */
        PENDING,
        /**
         *
         */
        REVERSAL,
    }
}
