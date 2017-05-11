/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-30
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.manager.sp;

import android.annotation.SuppressLint;
import android.content.Context;

import com.pax.abl.utils.EncUtils;
import com.pax.appstore.PaxAppStoreTool;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SysParamSp extends BaseSp {

    private static final String IS_PARAMFILEEXIST = "IS_PARAMFILEEXIST";

    private static final String INIT_FILE_NAME = "param.ini";
    // 通讯参数
    /**
     * 超时时间
     */
    public static final String COMM_TIMEOUT = "COMM_TIMEOUT";
    public static final String COMM_REDIAL_TIMES = "COMM_REDIAL_TIMES";
    /**
     * 通讯方式
     */
    public static final String APP_COMM_TYPE = "COMM_TYPE";
    /**
     * SSL通讯控制
     */
    public static final String APP_COMM_TYPE_SSL = "SSL_TYPE";

    /**
     * 移动网络
     */
    public static final String MOBILE_KEEP_ALIVE = "MOBILE_KEEP_ALIVE";
    public static final String MOBILE_WLTELNO = "MOBILE_WLTELNO";
    public static final String MOBILE_APN = "MOBILE_APN";
    public static final String MOBILE_NEED_USER = "MOBILE_NEED_USER";
    public static final String MOBILE_USER = "MOBILE_USER";
    public static final String MOBILE_PWD = "MOBILE_PWD";
    public static final String MOBILE_SIMPIN = "MOBILE_SIMPIN";
    public static final String MOBILE_AUTH = "MOBILE_AUTH";
    public static final String MOBILE_LOGINWAITETIME = "MOBILE_LOGINWAITETIME";
    public static final String MOBILE_HOSTIP = "MOBILE_HOSTIP";
    public static final String MOBILE_HOSTPORT = "MOBILE_HOSTPORT";
    public static final String MOBILE_HOSTIP_BAK = "MOBILE_HOSTIP_BAK";
    public static final String MOBILE_HOSTPORT_BAK = "MOBILE_HOSTPORT_BAK";
    public static final String MOBILE_DOMAINNAME = "MOBILE_DOMAINNAME";

    /**
     * 以太网参数
     */
    public static final String LAN_DHCP = "LAN_DHCP";
    public static final String LAN_LOCALIP = "LAN_LOCALIP";
    public static final String LAN_SUBNETMASK = "LAN_SUBNETMASK";
    public static final String LAN_GATEWAY = "LAN_GATEWAY";
    public static final String LAN_DNS1 = "LAN_DNS1";
    public static final String LAN_DNS2 = "LAN_DNS2";
    public static final String LAN_HOSTIP = "LAN_HOSTIP";
    public static final String LAN_HOSTPORT = "LAN_HOSTPORT";
    public static final String LAN_HOSTIP_BAK = "LAN_HOSTIP_BAK";
    public static final String LAN_HOSTPORT_BAK = "LAN_HOSTPORT_BAK";

    /**
     * WIFI
     */
    public static final String WIFI_ENABLE = "WIFI_ENABLE";

    /**
     * 商户参数
     */
    public static final String EDC_MERCHANT_NAME_EN = "EDC_MERCHANT_NAME_EN";
    public static final String EDC_MERCHANT_ADDRESS = "EDC_MERCHANT_ADDRESS";
    public static final String EDC_CURRENCY_LIST = "EDC_CURRENCY_LIST";
    public static final String EDC_PED_MODE = "EDC_PED_MODE";
    public static final String EDC_CONNECT_TIMEOUT = "EDC_CONNECT_TIMEOUT";
    public static final String EDC_RECEIPT_NUM = "EDC_RECEIPT_NUM";
    public static final String EDC_TRACE_NO = "EDC_TRACE_NO";
    public static final String EDC_SUPPORT_TIP = "EDC_SUPPORT_TIP";
    public static final String EDC_INVOICE_NUM = "EDC_INVOICE_NUM";
    public static final String EDC_SUPPORT_KEYIN = "EDC_SUPPORT_KEYIN";
    public static final String EDC_REVERSAL_RETRY = "EDC_REVERSAL_RETRY";

    public static final String EDC_ENABLE_PAPERLESS = "EDC_ENABLE_PAPERLESS";
    public static final String EDC_SMTP_HOST = "EDC_SMTP_HOST";
    public static final String EDC_SMTP_PORT = "EDC_SMTP_PORT";
    public static final String EDC_SMTP_USERNAME = "EDC_SMTP_USERNAME";
    public static final String EDC_SMTP_PASSWORD = "EDC_SMTP_PASSWORD";
    public static final String EDC_SMTP_ENABLE_SSL = "EDC_SMTP_ENABLE_SSL";
    public static final String EDC_SMTP_SSL_PORT = "EDC_SMTP_SSL_PORT";
    public static final String EDC_SMTP_FROM = "EDC_SMTP_FROM";


    /**
     * 系统参数
     */
    public static final String TRANS_NO = "TRANS_NO";
    public static final String MERCH_AREACODE = "MERCH_AREACODE";
    public static final String PRINT_TITLE = "PRINT_TITLE";
    public static final String PRINT_VOUCHER_NUM = "PRINT_VOUCHER_NUM";
    public static final String REVERSAL_CTRL = "REVERSAL_CTRL";
    public static final String MAX_TRANS_COUNT = "MAX_TRANS_COUNT";
    public static final String SUPPORT_DCC = "SUPPORT_DCC";//Zac
    public static final String AUTH_CODE_MODE = "AUTH_CODE_MODE";//Zac
    public static final String MOTO_FLOOR_LIMIT = "MOTO_FLOOR_LIMIT";//Zac
    public static final String SUPPORT_MOTOSALE = "SUPPORT_MOTOSALE";//Zac
    public static final String SUPPORT_USER_AGREEMENT = "SUPPORT_USER_AGREEMENT";
    public static final String TIP_RATE = "TIP_RATE";
    public static final String FORCE_ONLINE = "FORCE_ONLINE";

    public static final String PRINT_MINUS_SIGN = "PRINT_MINUS_SIGN";
    public static final String EX_PINPAD = "EX_PINPAD";
    public static final String EX_NOTOUCH_SET = "EX_NOTOUCH_SET";
    public static final String EX_NO_TOUCH_CHOOSE = "EX_NO_TOUCH_CHOOSE";
    public static final String EX_NO_TOUCH_SERIAL = "EX_NO_TOUCH_SERIAL";
    public static final String EX_NO_TOUCH_BAUD_RANT = "EX_NO_TOUCH_BAUD_RANT";

    /**
     * 终端密钥管理
     */
    public static final String MK_INDEX = "MK_INDEX";
    public static final String MK_INDEX_MANUAL = "MK_INDEX_MANUAL";
    public static final String MK_VALUE = "MK_VALUE";
    public static final String PK_VALUE = "PK_VALUE";
    public static final String AK_VALUE = "AK_VALUE";
    public static final String KEY_ALGORITHM = "KEY_ALGORITHM";
    public static final String KEYBOARD_TYPE = "KEYBOARD_TYPE";


    public static final String FINAL_TMK = "FINAL_TMK";

    // 交易管理
    // 交易开关
    /**
     * 传统交易开关
     */
    public static final String TTS = "TTS";
    public static final String TTS_SALE = "TTS_SALE";
    public static final String TTS_VOID = "TTS_VOID";
    public static final String TTS_REFUND = "TTS_REFUND";
    public static final String TTS_PREAUTH = "TTS_PREAUTH";
    public static final String TTS_ADJUST = "TTS_ADJUST";

    /**
     * 交易输密控制
     */
    public static final String IPTC = "IPTC";
    /**
     * 交易刷卡控制
     */
    public static final String UCTC = "UCTC";
    /**
     * 结算交易控制
     */
    public static final String SETTLETC = "SETTLETC";
    /**
     * 离线交易控制
     */
    public static final String OFFLINETC_UPLOAD_TYPE = "OFFLINETC_UPLOAD_TYPE";
    public static final String OFFLINETC_UPLOADTIMES = "OFFLINETC_UPLOADTIMES";
    public static final String OFFLINETC_UPLOADNUM = "OFFLINETC_UPLOADNUM";
    /**
     * 其它交易控制
     */
    public static final String OTHTC_VERIFY = "OTHTC_VERIFY";
    public static final String OTHTC_KEYIN = "OTHTC_KEYIN";
    public static final String OTHTC_DEFAULT = "OTHTC_DEFAULT";
    public static final String OTHTC_REFUNDLIMT = "OTHTC_REFUNDLIMT";
    // 电子签名控制
    public static final String OTHTC_SINGATURE = "OTHTC_SINGATURE";
    /**
     * 密码管理
     */
    public static final String SEC_SYSPWD = "SEC_SYSPWD";
    public static final String SEC_MERCHANTPWD = "SEC_MERCHANTPWD";
    public static final String SEC_TERMINALPWD = "SEC_TERMINALPWD";
    public static final String SEC_VOIDPWD = "SEC_VOIDPWD";
    public static final String SEC_REFUNDPWD = "SEC_REFUNDPWD";
    public static final String SEC_ADJUSTPWD = "SEC_ADJUSTPWD";
    public static final String SEC_SETTLEPWD = "SEC_SETTLEPWD";
    public static final String SEC_OFFLINEPWD = "SEC_OFFLINEPWD";
    public static final String SEC_VOIDREFUNDPWD = "SEC_VOIDREFUNDPWD";

    /**
     * 其它管理
     */
    public static final String OTHER_CLEAR_FUNC = "OTHER_CLEAR_FUNC";
    public static final String OTHER_DOWNLOAD_FUNC = "OTHER_DOWNLOAD_FUNC";
    public static final String OTHER_PRINT_PARAM = "OTHER_PRINT_PARAM";
    public static final String SELECT_DCC_PARTNER = "SELECT_DCC_PARTNER";//Zac
    public static final String DCC_PARTNER = "DCC_PARTNER";//Zac
    /**
     * 银行卡闪付 参数
     **/
    public static final String QUICK_PASS_TRANS_PIN_FREE_SWITCH = "QUICK_PASS_TRANS_PIN_FREE_SWITCH";
    public static final String QUICK_PASS_TRANS_FLAG = "非接快速业务标识";
    public static final String QUICK_PASS_TRANS_SWITCH = "QUICK_PASS_TRANS_SWITCH";
    public static final String QUICK_PASS_TRANS_PIN_FREE_AMOUNT = "QUICK_PASS_TRANS_PIN_FREE_AMOUNT";
    public static final String QUICK_PASS_TRANS_CDCVM_FLAG = "CDCVM标识";
    public static final String QUICK_PASS_TRANS_SIGN_FREE_AMOUNT = "QUICK_PASS_TRANS_SIGN_FREE_AMOUNT";
    public static final String QUICK_PASS_TRANS_SIGN_FREE_FLAG = "QUICK_PASS_TRANS_SIGN_FREE_FLAG";

    /**
     * 当前收单行参数
     */
    public static final String ACQ_NAME = "ACQ_NAME";


    private static Set<String> stringKeyMap = new HashSet<String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            // 通讯参数
            // 超时时间
            add(COMM_TIMEOUT);
            // 通讯方式
            add(APP_COMM_TYPE);
            add(APP_COMM_TYPE_SSL);

            // 移动网络参数
            add(MOBILE_WLTELNO);
            add(MOBILE_APN);
            add(MOBILE_USER);
            add(MOBILE_PWD);
            add(MOBILE_SIMPIN);
            add(MOBILE_AUTH);
            add(MOBILE_LOGINWAITETIME);
            add(MOBILE_HOSTIP);
            add(MOBILE_HOSTPORT);
            add(MOBILE_HOSTIP_BAK);
            add(MOBILE_HOSTPORT_BAK);
            add(MOBILE_DOMAINNAME);

            // 以太网参数
            add(LAN_LOCALIP);
            add(LAN_SUBNETMASK);
            add(LAN_GATEWAY);
            add(LAN_HOSTIP_BAK);
            add(LAN_HOSTPORT_BAK);
            add(LAN_HOSTIP);
            add(LAN_HOSTPORT);
            add(LAN_DNS1);
            add(LAN_DNS2);

            add(COMM_REDIAL_TIMES);
            //add(COMM_WAIT_TIME);

            add(EDC_MERCHANT_NAME_EN); // 英文商户名
            add(EDC_MERCHANT_ADDRESS);
            add(EDC_CURRENCY_LIST);
            add(EDC_PED_MODE);
            add(EDC_CONNECT_TIMEOUT);
            add(EDC_RECEIPT_NUM);
            add(EDC_TRACE_NO);
            add(EDC_INVOICE_NUM);

            add(EDC_SMTP_HOST);
            add(EDC_SMTP_PORT);
            add(EDC_SMTP_USERNAME);
            add(EDC_SMTP_PASSWORD);
            add(EDC_SMTP_SSL_PORT);
            add(EDC_SMTP_FROM);

            // 系统参数
            add(TRANS_NO); // 流水号
            add(MERCH_AREACODE); // 地区代码
            add(PRINT_TITLE); // 打印抬头
            add(PRINT_VOUCHER_NUM); // 打印凭单联数
            add(REVERSAL_CTRL); // 冲正控制
            add(MAX_TRANS_COUNT); // 最大交易笔数
            add(TIP_RATE); // 小费比例
            add(MOTO_FLOOR_LIMIT);
            add(EX_NO_TOUCH_CHOOSE);
            add(EX_NO_TOUCH_SERIAL);
            add(EX_NO_TOUCH_BAUD_RANT);


            // 终端密钥管理
            add(MK_INDEX); // 主密码索引
            add(KEY_ALGORITHM); // 密钥算法
            add(KEYBOARD_TYPE); // 密码键盘类型
            add(FINAL_TMK); //最终的TMK

            add(OFFLINETC_UPLOADTIMES); // 离线上送次数
            add(OFFLINETC_UPLOADNUM); // 自动上送累计笔数
            add(OFFLINETC_UPLOAD_TYPE); // 离线上送方式
            // 其它交易控制
            add(OTHTC_REFUNDLIMT); // 最大退货金额
            // 电子签名控制

            // 密码管理
            add(SEC_SYSPWD); // 系统管理员密码
            add(SEC_MERCHANTPWD);
            add(SEC_TERMINALPWD);
            add(SEC_VOIDPWD);
            add(SEC_REFUNDPWD);
            add(SEC_ADJUSTPWD);
            add(SEC_SETTLEPWD);
            add(SEC_OFFLINEPWD); //Zac
            add(AUTH_CODE_MODE); //Zac
            add(SEC_VOIDREFUNDPWD);
            // 闪付
            add(QUICK_PASS_TRANS_PIN_FREE_AMOUNT);
            add(QUICK_PASS_TRANS_SIGN_FREE_AMOUNT);

            //当前收单行名字
            add(ACQ_NAME);

            //DCC PARTNER
            add(DCC_PARTNER);//Zac
        }
    };

    private static Set<String> booleanKeyMap = new HashSet<String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            add(LAN_DHCP);
            // 系统参数
            add(EDC_SUPPORT_TIP); // 支持小费
            add(SUPPORT_DCC); // 支持汇率查询  //Zac
            add(SUPPORT_MOTOSALE); //打开MOTOSALE   //Zac
            add(SUPPORT_USER_AGREEMENT); // 支持用户须知阅读
            add(FORCE_ONLINE);
            add(EX_NOTOUCH_SET);
            add(EX_PINPAD);
            add(PRINT_MINUS_SIGN);

            // 通讯
            add(MOBILE_KEEP_ALIVE);
            add(LAN_DHCP);

            add(EDC_ENABLE_PAPERLESS);
            add(EDC_SMTP_ENABLE_SSL);

            // 其它交易控制
            add(OTHTC_VERIFY); // 撤销退货类交易输入主管密码
            add(OTHTC_KEYIN); // 允许手输卡号
            add(OTHTC_SINGATURE);// 允许电子签名
            add(OTHTC_DEFAULT); // 缺省交易选择
            add(EDC_SUPPORT_KEYIN); // 允许手输卡号

            // 闪付
            add(QUICK_PASS_TRANS_PIN_FREE_SWITCH);
            add(QUICK_PASS_TRANS_CDCVM_FLAG);
            add(QUICK_PASS_TRANS_FLAG);
            add(QUICK_PASS_TRANS_SIGN_FREE_FLAG);
            add(QUICK_PASS_TRANS_SWITCH);

        }
    };

    private static Set<String> setKeyMap = new HashSet<String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {

            // 交易开关
            // 传统交易开关
            add(TTS_SALE); // 消费开关_传统交易
            add(TTS_VOID); // 消费撤销开关_传统交易
            add(TTS_REFUND); // 退货开关_传统交易
            add(TTS_PREAUTH); // 预授权开关_传统交易
            add(TTS_ADJUST); // 调整开关_传统交易
        }
    };

    private static class LazyHolder {
        private static final SysParamSp INSTANCE = new SysParamSp();
    }

    protected static SysParamSp getInstance() {
        return LazyHolder.INSTANCE;
    }

    private SysParamSp() {
        super(null);
        if (!init()) {
            load(); // 加载参数内容到SysParam中
        }
    }

    public interface UpdateListener {
        void onErr(String prompt);
    }

    private UpdateListener updateListener;

    public void setUpdateListener(UpdateListener listener) {
        updateListener = listener;
    }

    /**
     * 下载文件param.ini到files目录的方式初始化参数
     *
     * @return
     */
    @SuppressLint({"NewApi", "ShowToast"})
    public boolean init() {
        File file = new File(ContextUtils.getFilesDir() + File.separator + INIT_FILE_NAME);
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        //如果batch中存有交易，提示settle
        if (DbManager.getTransDao().countOf() > 0) {
            if (updateListener != null) {
                updateListener.onErr(ContextUtils.getString(R.string.param_need_update_please_settle));
            }
            return false;
        }
        try {
            FileInputStream input = new FileInputStream(file);
            byte[] buf = new byte[(int) file.length()];
            int len = input.read(buf);
            input.close();

            if (len <= 0 || buf.length <= 0) {
                return false;
            }

            /*
              From https://github.com/android/platform_external_apache-http/blob/master/src/org/apache/http/util/EncodingUtils.java
              EncodingUtils.getString is just new String(data, offset, length, charset) with argument check
             */
            //String allParams = EncodingUtils.getString(buf, "UTF-8"); // outdated
            String allParams = new String(buf, "UTF-8");
            Set<String> setTtsKey = new HashSet<>();

            String[] paramArr = allParams.split("\r\n");
            if (paramArr.length == 0)
                return false;
            for (String param : paramArr) {
                if (param.contains("####"))
                    continue;
                String[] item = param.split("=", 2);
                if (item.length == 0) { // 文件格式不对
                    return false;

                } else if (item.length == 2) {
                    // if (keys.contains(item_gridview[0]))
                    if (setKeyMap.contains(item[0])) { // 传统类交易
                        if (item[0].equals(TTS_SALE) || item[0].equals(TTS_VOID) || item[0].equals(TTS_REFUND)
                                || item[0].equals(TTS_PREAUTH)
                                || item[0].equals(TTS_ADJUST)) {
                            // setTtsKey.add(item_gridview[0]);
                            if (item[1].equals("Y")) {
                                setTtsKey.add(item[0]);
                            }

                        }
                    } else {
                        switch (item[1]) {
                            case "Y":
                                putBoolean(item[0], true);
                                break;
                            case "N":
                                putBoolean(item[0], false);
                                break;
                            default:
                                putString(item[0], item[1]);
                                break;
                        }
                    }
                } else {
                    putString(item[0], "");
                }

            }

            putStringSet(TTS, setTtsKey);
            setTtsKey.clear();

            file.delete();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // 系统参数加载，如果db中不存在则添加
    @SuppressLint("NewApi")
    private void load() {
        if (isParamFileExist()) {
            return;
        }

        // 设置默认参数值
        Set<String> set = new HashSet<>();
        putBoolean(SysParamSp.IS_PARAMFILEEXIST, true);

        // 通讯参数
        // TPDU

        // 超时时间
        putString(COMM_TIMEOUT, "30");
        putString(COMM_REDIAL_TIMES, "3");
        // 通讯方式
        putString(APP_COMM_TYPE, Constant.COMMTYPE_WIFI);
        putString(APP_COMM_TYPE_SSL, Constant.COMM_NO_SSL);

        // MOBILE参数
        putString(MOBILE_WLTELNO, "8888888");
        putString(MOBILE_APN, "szjrln.gd");
        putString(MOBILE_USER, "");
        putString(MOBILE_PWD, "");
        //  putString(MOBILE_SIMPIN, "");
        //  putString(MOBILE_AUTH, "");
        //  putString(MOBILE_LOGINWAITETIME, "60");
        putString(MOBILE_HOSTIP, "144.144.21.12");
        putString(MOBILE_HOSTPORT, "8998");
        putString(MOBILE_HOSTIP_BAK, "0.0.0.0");
        putString(MOBILE_HOSTPORT_BAK, "0");
        //  putString(MOBILE_DOMAINNAME, "");

        // 以太网参数
        putString(LAN_LOCALIP, "172.16.10.125");
        putString(LAN_SUBNETMASK, "255.255.255.0");
        putString(LAN_GATEWAY, "172.16.10.1");
        putString(LAN_HOSTIP_BAK, "0.0.0.0");
        putString(LAN_HOSTPORT_BAK, "0");
        putBoolean(LAN_DHCP, false);
        putString(LAN_HOSTIP, "116.228.223.216");
        putString(LAN_HOSTPORT, "10021");
        putString(LAN_DNS1, "192.168.0.111");
        putString(LAN_DNS2, "192.168.0.112");

        // 商户参数
        putString(EDC_MERCHANT_NAME_EN, "Merchant Name"); // 英文商户名
        putString(EDC_MERCHANT_ADDRESS, "Merchant Addr");
        putString(EDC_CURRENCY_LIST, CurrencyConverter.getDefCurrency().getCountry());
        putString(EDC_PED_MODE, ContextUtils.getStringArray(R.array.edc_ped_mode_value_entries)[0]);
        putString(EDC_CONNECT_TIMEOUT, ContextUtils.getStringArray(R.array.edc_connect_time_entries)[0]);
        putString(EDC_RECEIPT_NUM, "1");
        putString(EDC_TRACE_NO, "1");
        putString(EDC_INVOICE_NUM, "1");
        putString(AUTH_CODE_MODE, ContextUtils.getStringArray(R.array.auth_code_mode_entries)[0]);//Zac
        putBoolean(EDC_ENABLE_PAPERLESS, true);
        putString(EDC_SMTP_HOST, "");
        putString(EDC_SMTP_PORT, "25");
        putString(EDC_SMTP_USERNAME, "");
        putString(EDC_SMTP_PASSWORD, "");
        putBoolean(EDC_SMTP_ENABLE_SSL, false);
        putString(EDC_SMTP_SSL_PORT, "443");
        putString(EDC_SMTP_FROM, "");

        // 系统参数
        putString(TRANS_NO, "000001"); // 流水号
        // putString(BATCH_NO, "000001"); // 批次号
        putString(PRINT_VOUCHER_NUM, "1"); // 打印凭单联数
        putString(REVERSAL_CTRL, "3"); // 冲正控制
        putString(MAX_TRANS_COUNT, "500");
        putBoolean(EDC_SUPPORT_TIP, true);
        putBoolean(SUPPORT_DCC, true);//Zac
        putBoolean(SUPPORT_MOTOSALE, true);//Zac
        putBoolean(SUPPORT_USER_AGREEMENT, false);
        putString(TIP_RATE, "5"); // 小费比例
        putString(MOTO_FLOOR_LIMIT, "100"); // 小费比例
        putBoolean(FORCE_ONLINE, true);
        putBoolean(PRINT_MINUS_SIGN, true);
        putBoolean(EX_PINPAD, false);
        putBoolean(EX_NOTOUCH_SET, false);
        putString(EX_NO_TOUCH_CHOOSE, "SP20");
        putString(EX_NO_TOUCH_SERIAL, "PINPAD");
        putString(EX_NO_TOUCH_BAUD_RANT, "9600");

        putString(MERCH_AREACODE, "0000"); // 地区代码
        putString(PRINT_TITLE, "银联商务签购单"); // 打印抬头

        putString(DCC_PARTNER, "FINTRAX"); //Zac

        // 终端密钥管理
        putString(MK_INDEX, "1"); // 主密码索引
        putString(KEY_ALGORITHM, Constant.TRIP_DES); // 密钥算法
        putString(KEYBOARD_TYPE, Constant.PED); // 密码键盘类型

        // 交易开关
        // 传统交易开关
        set.clear();
        set.add(TTS_SALE); // 消费开关_传统交易
        set.add(TTS_VOID); // 消费撤销开关_传统交易
        set.add(TTS_REFUND); // 退货开关_传统交易
        set.add(TTS_PREAUTH); // 预授权开关_传统交易
        set.add(TTS_ADJUST); // 调整开关_传统交易
        putStringSet(TTS, set);

        // 离线交易控制
        //  putBoolean(OFFLINETC_MICRO_PAYMENT, true); // 支持小额代付方式
        putString(OFFLINETC_UPLOAD_TYPE, "联机前"); // 离线上送方式
        putString(OFFLINETC_UPLOADTIMES, "3"); // 离线上送次数
        putString(OFFLINETC_UPLOADNUM, "10"); // 自动上送累计笔数
        // 其它交易控制
        putBoolean(OTHTC_VERIFY, true); // 撤销退货类交易输入主管密码
        putBoolean(OTHTC_KEYIN, true); // 允许手输卡号
        putString(OTHTC_REFUNDLIMT, "20000"); // 最大退货金额
        putBoolean(EDC_SUPPORT_KEYIN, true); // 允许手输卡号
        // 电子签名控制
        putBoolean(OTHTC_SINGATURE, true); // 允许电子签名

        // 密码管理
        putString(SEC_SYSPWD, EncUtils.SHA1(Constants.DEF_ADMIN_PWD)); // 系统管理员密码
        putString(SEC_MERCHANTPWD, EncUtils.SHA1(Constants.DEF_TRANS_PWD));
        putString(SEC_TERMINALPWD, EncUtils.SHA1(Constants.DEF_TRANS_PWD));
        putString(SEC_VOIDPWD, EncUtils.SHA1(Constants.DEF_TRANS_PWD));
        putString(SEC_REFUNDPWD, EncUtils.SHA1(Constants.DEF_TRANS_PWD));
        putString(SEC_ADJUSTPWD, EncUtils.SHA1(Constants.DEF_TRANS_PWD));
        putString(SEC_SETTLEPWD, EncUtils.SHA1(Constants.DEF_TRANS_PWD));
        putString(SEC_OFFLINEPWD, EncUtils.SHA1(Constants.DEF_TRANS_PWD));//Zac
        putString(SEC_VOIDREFUNDPWD, EncUtils.SHA1(Constants.DEF_TRANS_PWD));

        putBoolean(QUICK_PASS_TRANS_PIN_FREE_SWITCH, false);
        putBoolean(QUICK_PASS_TRANS_SWITCH, false);
        putBoolean(QUICK_PASS_TRANS_CDCVM_FLAG, false);
        putBoolean(QUICK_PASS_TRANS_FLAG, false);
        putString(QUICK_PASS_TRANS_PIN_FREE_AMOUNT, "30000");
        putBoolean(QUICK_PASS_TRANS_SIGN_FREE_FLAG, false);
        putString(QUICK_PASS_TRANS_SIGN_FREE_AMOUNT, "30000");

        putString(FINAL_TMK, "0000000000000000");//Zac
        //当前收单行参数
        putString(ACQ_NAME, "");
    }

    @SuppressLint("NewApi")
    public synchronized String get(String name) {
        String value = null;

        if (stringKeyMap.contains(name)) {
            value = getString(name, null);
        } else if (booleanKeyMap.contains(name)) {
            value = getBoolean(name, false) ? "Y" : "N";
        } else if (setKeyMap.contains(name)) {
            Set<String> set1 = getStringSet(TTS, null);
            if ((set1 != null) && (set1.contains(name))) {
                // value =  getBoolean(name, false) ? "Y": "N";
                value = "Y";
            } else {
                value = "N";
                // value =  getBoolean(name, false) ? "Y": "N";
            }
        }
        return value;
    }

    @SuppressLint("NewApi")
    public synchronized void set(String name, String value) {

        if (stringKeyMap.contains(name)) {
            putString(name, value);
        } else if (booleanKeyMap.contains(name)) {
            putBoolean(name, value.equals("Y"));
        } else if (setKeyMap.contains(name)) {
            // Set<String> set1 =  getStringSet(TTS, null);
            // Set<String> set2 =  getStringSet(ECTS, null);
            // Set<String> set3 =  getStringSet(EPTS, null);
            // Set<String> set4 =  getStringSet(INSTTS, null);
            // Set<String> set5 =  getStringSet(PTS, null);
            // Set<String> set6 =  getStringSet(MTS, null);
            // Set<String> set7 =  getStringSet(OTS, null);
            // Set<String> set8 =  getStringSet(BTS, null);
            // Set<String> set9 =  getStringSet(OTHS, null);

        } else {
            //保存非SysParam定义的参数， TPK, TAK, TDK
            putString(name, value);
        }
    }

    /**
     * 联机下载参数
     *
     * @return
     */
    public boolean downloadParamOnline() {
        Context context = FinancialApplication.mApp;
        HashMap<String, String> params = (HashMap<String, String>) PaxAppStoreTool.getParam(context);
        if (params == null || params.size() == 0) {
            return false;
        }

        StringBuffer fileContent = new StringBuffer();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            fileContent.append(entry.getKey() + "=" + entry.getValue() + "\r\n");
        }
        File file = new File(ContextUtils.getFilesDir() + File.separator + INIT_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            file.createNewFile();
            bw.write(fileContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public HashMap<String, String> getAllParams() {
        HashMap<String, String> params = new HashMap<>();

        for (String stringKey : stringKeyMap) {
            String stringValue = get(stringKey);
            params.put(stringKey, stringValue);
        }
        for (String booleanKey : booleanKeyMap) {
            String booleanValue = get(booleanKey);
            params.put(booleanKey, booleanValue);
        }
        for (String setKey : setKeyMap) {
            String setValue = get(setKey);
            params.put(setKey, setValue);
        }
        return params;
    }

    /**
     * 联机上送参数
     */
    public void uploadParamOnline(final HashMap<String, String> params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Context context = FinancialApplication.mApp;
                PaxAppStoreTool.setParam(context, params);
            }
        }).start();
    }

    private boolean isParamFileExist() {
        return getBoolean(SysParamSp.IS_PARAMFILEEXIST, false);
    }

    public static class Constant {
        /**
         * 通讯类型
         */
        public static final String COMMTYPE_MOBILE = "MOBILE";
        public static final String COMMTYPE_LAN = "LAN";
        public static final String COMMTYPE_WIFI = "WIFI";
        public static final String COMMTYPE_DEMO = "DEMO";

        // 主密钥接收端口
        public static final String PORT_COM1 = "COM1";
        public static final String PORT_PINPAD = "PINPAD";
        public static final String PORT_USBDEV = "USBDEV";

        /**
         * 不启用SSL
         */
        public static final String COMM_NO_SSL = "NO SSL";

        /**
         * des算法
         */
        public static final String DES = "des";
        public static final String TRIP_DES = "3des";

        /**
         * 密码键盘类型
         */
        public static final String PED = "ped";
        public static final String PINPAD = "pinpad";

        /**
         * 对应于肯定值, 是\支持\等
         */
        public static final String YES = "Y";
        // 对应于否定值, 否\不支持\等
        public static final String NO = "N";

    }

}
