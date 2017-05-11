package com.pax.pay.trans.model;

/**
 * Created by zhouhong on 2017/4/26.
 */

public class SessionMaintData {
    protected String TMK;
    protected String TLE;
    protected String TPK;
    protected String MACKEY;
    protected String doubleLenTPK1;
    protected String doubleLenTPK2;

    public String getTMK() {
        return TMK;
    }

    public void setTMK(String TMK) {
        this.TMK = TMK;
    }

    public String getTLE() {
        return TLE;
    }

    public void setTLE(String TLE) {
        this.TLE = TLE;
    }

    public String getTPK() {
        return TPK;
    }

    public void setTPK(String TPK) {
        this.TPK = TPK;
    }

    public String getMACKEY() {
        return MACKEY;
    }

    public void setMACKEY(String MACKEY) {
        this.MACKEY = MACKEY;
    }

    public String getDoubleLenTPK1() {
        return doubleLenTPK1;
    }

    public void setDoubleLenTPK1(String doubleLenTPK1) {
        this.doubleLenTPK1 = doubleLenTPK1;
    }

    public String getDoubleLenTPK2() {
        return doubleLenTPK2;
    }

    public void setDoubleLenTPK2(String doubleLenTPK2) {
        this.doubleLenTPK2 = doubleLenTPK2;
    }

}
