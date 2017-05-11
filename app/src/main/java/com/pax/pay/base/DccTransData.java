package com.pax.pay.base;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zhouhong on 2017/4/7.
 */
@DatabaseTable(tableName = "dcc")
public class DccTransData implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String ID_FIELD_NAME = "dcc_id";
    public final static String NAME_FIELD_NAME = "dcc_name";

    /**
     * id
     */
    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private int id;

    /**
     * name
     */
    @DatabaseField(unique = true, columnName = NAME_FIELD_NAME)
    private String name;

    @DatabaseField
    protected Boolean isDccOptIn = false;
    @DatabaseField
    protected String dccTransAmt;
    @DatabaseField
    protected String dccConvRate;
    @DatabaseField
    protected String dccCurrency;
    @DatabaseField
    protected String dccMargin;

    protected String dccPartner;
    protected String dccRspCode;
    protected String dccLeg;

    protected ArrayList<String> dccTransAmtList;
    protected ArrayList<String> dccConvRatetList;
    protected ArrayList<String> dccCurrencyList;

    public DccTransData() {
        isDccOptIn = false;
    }

    public String getDccPartner() {
        dccPartner = SpManager.getSysParamSp().get(SysParamSp.DCC_PARTNER);
        return dccPartner;
    }

    public void setDccPartner(String dccPartner) {
        this.dccPartner = dccPartner;
    }

    public String getDccRspCode() {
        return dccRspCode;
    }

    public void setDccRspCode(String dccRspCode) {
        this.dccRspCode = dccRspCode;
    }


    public String getDccTransAmt() {
        return dccTransAmt;
    }

    public void setDccTransAmt(String dccTransAmt) {
        this.dccTransAmt = dccTransAmt;
    }

    public String getDccConvRate() {
        return dccConvRate;
    }

    public void setDccConvRate(String dccConvRate) {
        this.dccConvRate = dccConvRate;
    }

    public String getDccCurrency() {
        return dccCurrency;
    }

    public void setDccCurrency(String dccCurrency) {
        this.dccCurrency = dccCurrency;
    }


    public ArrayList<String> getTransAmtList() {
        return dccTransAmtList;
    }

    public void setTransAmtList(ArrayList<String> dccTransAmtList) {
        this.dccTransAmtList = dccTransAmtList;
    }

    public ArrayList<String> getConvRateList() {
        return dccConvRatetList;
    }

    public void setConvRateList(ArrayList<String> dccConvRatetList) {
        this.dccConvRatetList = dccConvRatetList;
    }

    public ArrayList<String> getCurrencyList() {
        return dccCurrencyList;
    }

    public void setCurrencyList(ArrayList<String> dccCurrencyList) {
        this.dccCurrencyList = dccCurrencyList;
    }

    public String getDccMargin() {
        return dccMargin;
    }

    public void setDccMargin(String dccMargin) {
        this.dccMargin = dccMargin;
    }

    public String getDccLeg() {
        return dccLeg;
    }

    public void setDccLeg(String dccLeg) {
        this.dccLeg = dccLeg;
    }

    public boolean isDccOptIn() {
        return isDccOptIn;
    }

    public void setDccOptIn(boolean isDccOptIn) {
        this.isDccOptIn = isDccOptIn;
    }

}
