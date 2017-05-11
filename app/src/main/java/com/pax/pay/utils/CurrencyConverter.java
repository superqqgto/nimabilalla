/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-30
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.utils;


import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

public class CurrencyConverter {

    private static final List<Locale> locales = new ArrayList<>();

    private static Locale defLocale = Locale.US;

    static {
        Locale[] tempLocales = Locale.getAvailableLocales();
        for (Locale i : tempLocales) {
            try {
                CountryCode country = CountryCode.getByCode(i.getISO3Country());
                Currency.getInstance(i); // just for filtering
                if (country != null) {
                    locales.add(i);
                }
            } catch (IllegalArgumentException | MissingResourceException e) {
                //e.printStackTrace();
            }
        }
    }

    public static List<Locale> getSupportedLocale(){
        return locales;
    }

    /**
     * @param countryName : {@see Locale#getDisplayName(Locale)}
     */
    public static Locale setDefCurrency(String countryName) {
        for(Locale i : locales){
            if(i.getDisplayName(Locale.US).equals(countryName)){
                if (!i.equals(defLocale)) {
                    defLocale = i;
                    Locale.setDefault(defLocale);
                }
                return defLocale;
            }
        }
        return defLocale;
    }

    public static Locale getDefCurrency() {
        return defLocale;
    }

    /**
     * @param amount
     * @return
     */
    public static String convert(Long amount) {
        return convert(amount, defLocale);
    }

    /**
     * @param amount
     * @param locale
     * @return
     */
    public static String convert(Long amount, Locale locale) {
        Currency currency = Currency.getInstance(locale);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setMinimumFractionDigits(currency.getDefaultFractionDigits());
        formatter.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        Long newAmount = amount < 0 ? -amount : amount; // AET-58
        String prefix = amount < 0 ? "-" : "";
        try {
            double amt = Double.valueOf(newAmount) / (Math.pow(10, currency.getDefaultFractionDigits()));
            return prefix + formatter.format(amt);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Long parse(String formatterAmount) {
        return parse(formatterAmount, defLocale);
    }

    public static Long parse(String formatterAmount, Locale locale) {
        Currency currency = Currency.getInstance(locale);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setMinimumFractionDigits(currency.getDefaultFractionDigits());
        formatter.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        try {
            Number num = formatter.parse(formatterAmount);

            return Math.round(num.doubleValue() * Math.pow(10, currency.getDefaultFractionDigits()));
        } catch (ParseException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 0L;
    }
}
