package com.pax.pay.trans.model;

import android.content.Context;

/**
 * Created by zhouhong on 2017/4/6.
 */

public class InstalmentTransData {

    protected String instalDsp;  //instalment description
    protected String tenure;
    protected String minAmt;
    protected String maxAmt;
    protected String waiverMon;
    protected String validFromDate;
    protected String validToDate;
    protected String programCode;
    protected String productCode;
    protected String hostId;

    protected String productAmt;
    protected String discountAmt;
    protected String interestRate;
//    protected String roiTenure;
    protected String interestAmt;
    protected String totalAmt;
    protected String emiPerMonth;
    protected String processFee;

    public InstalmentTransData() {
        //初始化参数列表
        //............
    }

    public String getTenure() {
        return tenure;
    }
    public void setTenure(String tenure) {
        this.tenure = tenure;
    }

    public String getMinAmt() {
        return minAmt;
    }
    public void setMinAmt(String minAmt) {
        this.minAmt = minAmt;
    }

    public String getMaxAmt() {
        return maxAmt;
    }
    public void setMaxAmt(String maxAmt) {
        this.maxAmt = maxAmt;
    }

    public String getWaiverMon() {
        return waiverMon;
    }
    public void setWaiverMon(String waiverMon) {
        this.waiverMon = waiverMon;
    }

    public String getValidFromDate() {
        return validFromDate;
    }
    public void setValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
    }

    public String getValidToDate() {
        return validToDate;
    }
    public void setValidToDate(String validToDate) {
        this.validToDate = validToDate;
    }

    public String getProgramCode() {
        return programCode;
    }
    public void setProgramCode(String programCode) {
        this.programCode = programCode;
    }

    public String getProductCode() {
        return productCode;
    }
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getHostId() {
        return hostId;
    }
    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getProductAmt() {
        return productAmt;
    }
    public void setProductAmt(String productAmt) {
        this.productAmt = productAmt;
    }

    public String getDiscountAmt() {
        return discountAmt;
    }
    public void setDiscountAmt(String discountAmt) {
        this.discountAmt = discountAmt;
    }

    public String getInterestRate() {
        return interestRate;
    }
    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public String getInterestAmt() {
        return interestAmt;
    }
    public void setInterestAmt(String interestAmt) {
        this.interestAmt = interestAmt;
    }

    public String getTotalAmt() {
        return totalAmt;
    }
    public void setTotalAmt(String totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getEmiPerMonth() {
        return emiPerMonth;
    }
    public void setEmiPerMonth(String emiPerMonth) {
        this.emiPerMonth = emiPerMonth;
    }

    public String getProcessFee() {
        return processFee;
    }
    public void setProcessFee(String processFee) {
        this.processFee = processFee;
    }

}
