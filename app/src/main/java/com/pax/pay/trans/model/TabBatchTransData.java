package com.pax.pay.trans.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.pax.pay.base.Acquirer;
import com.pax.pay.base.Issuer;

import java.io.Serializable;
import java.util.Locale;

/**
 * created by Muhua Huang 2775
 */
@DatabaseTable(tableName = "tab_batch_trans_data")
public class TabBatchTransData extends BaseTransData implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    public TabBatchTransData() {//无惨构造函数
    }

    public TabBatchTransData(TransData transData) {
        this.setTraceNo(transData.getTraceNo());
        this.setTransType(transData.getTransType());
        this.setTransState(transData.getTransState());
        this.setUpload(transData.isUpload());
        this.setSendTimes(transData.getSendTimes());
        this.setBatchNo(transData.getBatchNo());
        this.setHasPin(transData.isHasPin());
        setAmount(transData.getAmount());
        setAuthCode(transData.getAuthCode());
        setResponseCode(transData.getResponseCode());
        setRefNo(transData.getRefNo());
        setPan(transData.getPan());
        setExpDate(transData.getExpDate());
        setAcquirer(transData.getAcquirer());
        setIssuer(transData.getIssuer());
//        FinancialApplication.acqManager.setCurIssuer(transData.getIssuer());
    }

    // ============= 需要存储 ==========================
    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    protected int id;
    @DatabaseField(unique = true, canBeNull = false, columnName = TRACENO_FIELD_NAME)
    protected long traceNo; // pos流水号
    @DatabaseField
    protected long origTransNo; // 原pos流水号
    @DatabaseField(canBeNull = false, columnName = TYPE_FIELD_NAME)
    protected ETransType transType; // 交易类型
    @DatabaseField
    protected ETransType origTransType; // 原交易类型
    @DatabaseField(canBeNull = false, columnName = STATE_FIELD_NAME)
    protected ETransStatus transState = ETransStatus.NORMAL; // 交易状态
    @DatabaseField(canBeNull = false)
    protected boolean isUpload = false; // 是否已批上送
    @DatabaseField
    protected OfflineStatus offlineSendState = OfflineStatus.OFFLINE_NOT_SENT; // 脱机上送失败类型 ：上送失败/平台拒绝
    @DatabaseField(canBeNull = false)
    protected int sendTimes; // 已批上送次数
    @DatabaseField
    protected String procCode; // 处理码，39域
    @DatabaseField(columnName = AMOUNT_FIELD_NAME)
    protected String amount; // 交易金额
    @DatabaseField
    protected String tipAmount; // 小费金额
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    protected Locale currency; // currency
    @DatabaseField(canBeNull = false)
    protected long batchNo; // 批次号
    @DatabaseField
    protected long origBatchNo; // 原批次号
    @DatabaseField
    protected String pan; // 主账号-刷两张卡时为转出卡卡号    // TODO Encrypt it E2EE
    @DatabaseField
    protected String dateTime; // 交易日期时间
    @DatabaseField
    protected String origDateTime; // 原交易日期时间
    @DatabaseField
    protected String settleDateTime; // 清算日期时间
    @DatabaseField
    protected String expDate; // 卡有效期
    @DatabaseField
    protected EnterMode enterMode; // 输入模式
    @DatabaseField
    protected String nii;     //Network International Identifier
    @DatabaseField
    protected String refNo; // 系统参考号
    @DatabaseField
    protected String origRefNo; // 原系统参考号
    @DatabaseField(columnName = AUTHCODE_FIELD_NAME)
    protected String authCode; // 授权码
    @DatabaseField
    protected String origAuthCode; // 原授权码
    @DatabaseField
    protected String issuerCode; // 发卡行标识码
    @DatabaseField
    protected String acqCode; // 收单机构标识码
    @DatabaseField(canBeNull = false)
    protected boolean hasPin; // 是否有输密码
    @DatabaseField
    protected String track1; // 磁道一信息
    @DatabaseField
    protected String track2; // 磁道二数据
    @DatabaseField
    protected String track3; // 磁道三数据
    @DatabaseField
    protected String dupReason; // 冲正原因
    @DatabaseField
    protected String reserved; // 保留域[field63]

    // =================EMV数据=============================
    @DatabaseField(canBeNull = false)
    protected boolean pinFree; // 免密
    @DatabaseField(canBeNull = false)
    protected boolean signFree; // 免签
    @DatabaseField(canBeNull = false)
    protected boolean isCDCVM; // CDCVM标识
    @DatabaseField(canBeNull = false)
    protected boolean isOnlineTrans; // 是否为联机交易
    // 电子签名专用
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    protected byte[] signData; // signData

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = Issuer.ID_FIELD_NAME)
    protected Issuer issuer;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = Acquirer.ID_FIELD_NAME)
    protected Acquirer acquirer;

    // =================EMV数据=============================
    @DatabaseField(dataType = DataType.BYTE)
    protected byte emvResult; // EMV交易的执行状态
    @DatabaseField
    protected String cardSerialNo; // 23 域，卡片序列号
    @DatabaseField
    protected String sendIccData; // IC卡信息,55域
    @DatabaseField
    protected String dupIccData; // IC卡冲正信息,55域
    @DatabaseField
    protected String tc; // IC卡交易证书(TC值)tag9f26,(BIN)
    @DatabaseField
    protected String arqc; // 授权请求密文(ARQC)
    @DatabaseField
    protected String arpc; // 授权响应密文(ARPC)
    @DatabaseField
    protected String tvr; // 终端验证结果(TVR)值tag95
    @DatabaseField
    protected String aid; // 应用标识符AID
    @DatabaseField
    protected String emvAppLabel; // 应用标签
    @DatabaseField
    protected String emvAppName; // 应用首选名称
    @DatabaseField
    protected String tsi; // 交易状态信息(TSI)tag9B
    @DatabaseField
    protected String atc; // 应用交易计数器(ATC)值tag9f36

    @DatabaseField(canBeNull = false, columnName = REVERSAL_FIELD_NAME)
    protected ReversalStatus reversalStatus = ReversalStatus.NORMAL;

    @DatabaseField
    private String phoneNum;

    @DatabaseField
    private String email;

    // ================不需要存储=============================
    private String pin;

    protected String responseCode;
    protected String header;
    protected String tpdu;


    protected String field48;
    protected String field60;
    protected String field62;
    protected String field63;
    protected String recvIccData;
    protected String field3;


    /**
     * id
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTraceNo() {
        return traceNo;
    }

    public void setTraceNo(long traceNo) {
        this.traceNo = traceNo;
    }

    public long getOrigTransNo() {
        return origTransNo;
    }

    public void setOrigTransNo(long origTransNo) {
        this.origTransNo = origTransNo;
    }

    public ETransType getTransType() {
        return transType;
    }

    public void setTransType(ETransType transType) {
        this.transType = transType;
    }

    public ETransType getOrigTransType() {
        return origTransType;
    }

    public void setOrigTransType(ETransType origTransType) {
        this.origTransType = origTransType;
    }

    public ETransStatus getTransState() {
        return transState;
    }

    public void setTransState(ETransStatus transState) {
        this.transState = transState;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public OfflineStatus getOfflineSendState() {
        return offlineSendState;
    }

    public void setOfflineSendState(OfflineStatus offlineSendState) {
        this.offlineSendState = offlineSendState;
    }

    public int getSendTimes() {
        return sendTimes;
    }

    public void setSendTimes(int sendTimes) {
        this.sendTimes = sendTimes;
    }

    public String getProcCode() {
        return procCode;
    }

    public void setProcCode(String procCode) {
        this.procCode = procCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(String tipAmount) {
        this.tipAmount = tipAmount;
    }

    public Locale getCurrency() {
        return currency;
    }

    public void setCurrency(Locale currency) {
        this.currency = currency;
    }

    public long getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(long batchNo) {
        this.batchNo = batchNo;
    }

    public long getOrigBatchNo() {
        return origBatchNo;
    }

    public void setOrigBatchNo(long origBatchNo) {
        this.origBatchNo = origBatchNo;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getOrigDateTime() {
        return origDateTime;
    }

    public void setOrigDateTime(String origDateTime) {
        this.origDateTime = origDateTime;
    }

    public String getSettleDateTime() {
        return settleDateTime;
    }

    public void setSettleDateTime(String settleDateTime) {
        this.settleDateTime = settleDateTime;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public EnterMode getEnterMode() {
        return enterMode;
    }

    public void setEnterMode(EnterMode enterMode) {
        this.enterMode = enterMode;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getOrigRefNo() {
        return origRefNo;
    }

    public void setOrigRefNo(String origRefNo) {
        this.origRefNo = origRefNo;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getOrigAuthCode() {
        return origAuthCode;
    }

    public void setOrigAuthCode(String origAuthCode) {
        this.origAuthCode = origAuthCode;
    }

    public String getIssuerCode() {
        return issuerCode;
    }

    public void setIssuerCode(String issuerCode) {
        this.issuerCode = issuerCode;
    }

    public String getAcqCode() {
        return acqCode;
    }

    public void setAcqCode(String acqCode) {
        this.acqCode = acqCode;
    }

    public boolean isHasPin() {
        return hasPin;
    }

    public void setHasPin(boolean hasPin) {
        this.hasPin = hasPin;
    }

    public String getTrack1() {
        return track1;
    }

    public void setTrack1(String track1) {
        this.track1 = track1;
    }

    public String getTrack2() {
        return track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getTrack3() {
        return track3;
    }

    public void setTrack3(String track3) {
        this.track3 = track3;
    }

    public String getDupReason() {
        return dupReason;
    }

    public void setDupReason(String dupReason) {
        this.dupReason = dupReason;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public boolean isPinFree() {
        return pinFree;
    }

    public void setPinFree(boolean pinFree) {
        this.pinFree = pinFree;
    }

    public boolean isSignFree() {
        return signFree;
    }

    public void setSignFree(boolean signFree) {
        this.signFree = signFree;
    }

    public boolean isCDCVM() {
        return isCDCVM;
    }

    public void setCDCVM(boolean CDCVM) {
        isCDCVM = CDCVM;
    }

    public boolean isOnlineTrans() {
        return isOnlineTrans;
    }

    public void setOnlineTrans(boolean onlineTrans) {
        isOnlineTrans = onlineTrans;
    }

    public byte[] getSignData() {
        return signData;
    }

    public void setSignData(byte[] signData) {
        this.signData = signData;
    }

    public Issuer getIssuer() {
        return issuer;
    }

    public void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }

    public Acquirer getAcquirer() {
        return acquirer;
    }

    public void setAcquirer(Acquirer acquirer) {
        this.acquirer = acquirer;
    }

    /**
     * EMV交易的执行状态
     */
    public byte getEmvResult() {
        return emvResult;
    }

    public void setEmvResult(byte emvResult) {
        this.emvResult = emvResult;
    }

    public String getCardSerialNo() {
        return cardSerialNo;
    }

    public void setCardSerialNo(String cardSerialNo) {
        this.cardSerialNo = cardSerialNo;
    }

    public String getSendIccData() {
        return sendIccData;
    }

    public void setSendIccData(String sendIccData) {
        this.sendIccData = sendIccData;
    }

    public String getDupIccData() {
        return dupIccData;
    }

    public void setDupIccData(String dupIccData) {
        this.dupIccData = dupIccData;
    }

    public String getTc() {
        return tc;
    }

    public void setTc(String tc) {
        this.tc = tc;
    }

    public String getArqc() {
        return arqc;
    }

    public void setArqc(String arqc) {
        this.arqc = arqc;
    }

    public String getArpc() {
        return arpc;
    }

    public void setArpc(String arpc) {
        this.arpc = arpc;
    }

    public String getTvr() {
        return tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getEmvAppLabel() {
        return emvAppLabel;
    }

    public void setEmvAppLabel(String emvAppLabel) {
        this.emvAppLabel = emvAppLabel;
    }

    public String getEmvAppName() {
        return emvAppName;
    }

    public void setEmvAppName(String emvAppName) {
        this.emvAppName = emvAppName;
    }

    public String getTsi() {
        return tsi;
    }

    public void setTsi(String tsi) {
        this.tsi = tsi;
    }

    public String getAtc() {
        return atc;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }

    /**
     * 个人密码(密文)
     */
    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    /**
     * 响应码
     */
    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTpdu() {
        return tpdu;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public ReversalStatus getReversalStatus() {
        return reversalStatus;
    }

    public void setReversalStatus(ReversalStatus reversalStatus) {
        this.reversalStatus = reversalStatus;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getField48() {
        return field48;
    }

    public void setField48(String field48) {
        this.field48 = field48;
    }

    public String getField60() {
        return field60;
    }

    public void setField60(String field60) {
        this.field60 = field60;
    }

    public String getField62() {
        return field62;
    }

    public void setField62(String field62) {
        this.field62 = field62;
    }

    public String getField63() {
        return field63;
    }

    public void setField63(String field63) {
        this.field63 = field63;
    }

    public String getRecvIccData() {
        return recvIccData;
    }

    public void setRecvIccData(String recvIccData) {
        this.recvIccData = recvIccData;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
