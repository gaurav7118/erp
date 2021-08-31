/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.authHandler;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONObject;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.TimeZone;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserLogin;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Karthik
 */
public class authHandler {

    public static JSONObject getVerifyLoginJson(List ll, HttpServletRequest request) {
        JSONObject jobj = new JSONObject();
        try {
            Iterator ite = ll.iterator();
            if (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                User user = (User) row[0];
                UserLogin userLogin = (UserLogin) row[1];
                Company company = (Company) row[2];
                jobj.put("success", true);
                jobj.put("lid", userLogin.getUserID());
                jobj.put("username", userLogin.getUserName());
                jobj.put("companyid", company.getCompanyID());
                jobj.put("company", company.getCompanyName());
                jobj.put("companyTzDiff", company.getTimeZone().getDifference());   //Added company timezone in JSON
                jobj.put("roleid", user.getRoleID());
                jobj.put("callwith", user.getCallwith());
                jobj.put("timeformat", user.getTimeformat());
                KWLTimeZone timeZone = user.getTimeZone();
                if (timeZone == null) {
                    timeZone = company.getTimeZone();
                }
                if (timeZone == null) {
                    timeZone = (KWLTimeZone) ll.get(1);
                }
                jobj.put("timezoneid", timeZone.getTimeZoneID());
                jobj.put("tzdiff", timeZone.getDifference());
                KWLDateFormat dateFormat = user.getDateFormat();
                if (dateFormat == null) {
                    dateFormat = (KWLDateFormat) ll.get(2);
                }
                jobj.put("dateformatid", dateFormat.getFormatID());
                KWLCurrency currency = company.getCurrency();
                if (currency == null) {
                    currency = (KWLCurrency) ll.get(3);
                }
                jobj.put("currencyid", currency.getCurrencyID());
                jobj.put("success", true);
            } else {
                jobj.put("failure", true);
                jobj.put("success", false);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public static DateFormat getGlobalDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
        return sdf;
    }
    
    /**
     * This DateFormat object has used previously to convert the date object as
     * per User's Timezone.       *
     * @deprecated use {@link #getDateOnlyFormat() OR getDateOnlyFormat(HttpServletRequest request)}
     * instead.
     */
    @Deprecated
    public static DateFormat getDateFormatter(HttpServletRequest request) throws SessionExpiredException {
        SimpleDateFormat sdf = (SimpleDateFormat) getGlobalDateFormat();
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));
        return sdf;
    }
    
    public static DateFormat getDateFormatterWithouTime(JSONObject jobj) throws SessionExpiredException, JSONException {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
        if(jobj.has("timezonedifference") && jobj.getString("timezonedifference")!=null){
            sdf.setTimeZone(TimeZone.getTimeZone("GMT" + jobj.getString("timezonedifference")));
        }
        return sdf;
    }
    
    /**
     * This DateFormat object has used previously to convert the date object as
     * per User's Timezone.       *
     * @deprecated use {@link #getDateOnlyFormat() OR getDateOnlyFormat(HttpServletRequest request)}
     * instead.
     */
    @Deprecated
    public static DateFormat getDateFormatter(JSONObject jobj) throws SessionExpiredException, JSONException {
        SimpleDateFormat sdf = (SimpleDateFormat) getGlobalDateFormat();
        if(jobj.has("timezonedifference") && jobj.getString("timezonedifference")!=null){
            //sdf.setTimeZone(TimeZone.getTimeZone("GMT" + jobj.getString("timezonedifference")));  //Do not add Timezone difference in Date Object
        }
        return sdf;
    }
        
    public static DateFormat getDateFormatterWithoutSeconds(HttpServletRequest request) throws SessionExpiredException {
        SimpleDateFormat sdf = (SimpleDateFormat) new SimpleDateFormat("MMMM d, yyyy hh:mm");
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));
        return sdf;
    }
    
    //This function designed to show date as equal to UI with client specified date format. Second param indicate Client specify date format.
    /**
     * This DateFormat object has used previously to convert the date object as
     * per User's Timezone.       *
     * @deprecated use {@link #getUserDateFormatterWithoutTimeZone() OR getUserDateFormatterWithoutTimeZone(HttpServletRequest request)}
     * instead.
     */
    @Deprecated
    public static DateFormat getDateFormatterForPDF(HttpServletRequest request, DateFormat df) throws SessionExpiredException {
        //df.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));
        return df;
    }

    public static DateFormat getGlobalDateFormatInRequestFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
        return sdf;
    }

    /*Erp-13304*/
    public static DateFormat getUserDateFormatterWithoutTimeZone(HttpServletRequest request) throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat(sessionHandlerImpl.getUserDateFormat(request));
        return sdf;
    }
    
    public static DateFormat getUserDateFormatterWithoutTimeZone(String userDateFormat) throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat(userDateFormat);
        return sdf;
    }
    
    public static DateFormat getUserDateFormatterWithoutTimeZone(JSONObject jobj) throws JSONException {        
        String format = "MMM d, yyyy";
        if(jobj.has("userdateformat") && jobj.getString("userdateformat")!=null){
            format = jobj.getString("userdateformat");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf;
    }
    
    /**
     * This DateFormat object has used previously to convert the date object as
     * per User's Timezone.       *
     * @deprecated use {@link #getUserDateFormatterWithoutTimeZone() OR getUserDateFormatterWithoutTimeZone(HttpServletRequest request)}
     * instead.
     */
    @Deprecated
    public static DateFormat getUserDateFormatter(HttpServletRequest request) throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat(sessionHandlerImpl.getUserDateFormat(request));;
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));
        return sdf;
    }
    
    public static DateFormat getUserDateFormatterJson(JSONObject jobj) throws SessionExpiredException, JSONException {
        String format = "MMM d, yyyy";
        String timeZone = "";
        if(jobj.has("userdateformat") && jobj.getString("userdateformat")!=null){
            format = jobj.getString("userdateformat");
            if(jobj.has("timezonedifference") && jobj.getString("timezonedifference")!=null){
                timeZone = jobj.getString("timezonedifference");
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));  //ERP-20961
        return sdf;
    }
    
    public static DateFormat getConstantDateFormatter(HttpServletRequest request) throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf;
    }
    
     public static String removeStyleFromString(String str) throws SessionExpiredException {
         String newString="";
         String expression = "style=\".+?\"";
         Pattern p = Pattern.compile(expression);
         String[] items = p.split(str);
         for (String s : items) {
             newString += s;
         }
         return newString;
    }

    @Deprecated
    public static double round(double Rval, int Rpl) {
//        if(Constants.AMOUNT_DIGIT_AFTER_DECIMAL==4){
//            Rpl=4;
//        }else if(Constants.AMOUNT_DIGIT_AFTER_DECIMAL==3){
//            Rpl=3;
//        } 
        if (Constants.AMOUNT_DIGIT_AFTER_DECIMAL != 2) {
            Rpl = Constants.AMOUNT_DIGIT_AFTER_DECIMAL;
         }
//        val = val + 1/Math.pow(10,Wtf.AMOUNT_DIGIT_AFTER_DECIMAL+2);
        double p = (double) Math.pow(10, Rpl);
        double sign = 1;
            if (Rval < 0) {
            sign = -1;
        }

        Rval = Math.abs(Rval) + 1 / (double) Math.pow(10, Rpl + 10);           // Changed for ERP-14410
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return (double) (tmp / p) * (sign);
    }
    
    public static double round(double Rval, String companyid) {
//        if(Constants.AMOUNT_DIGIT_AFTER_DECIMAL==4){
//            Rpl=4;
//        }else if(Constants.AMOUNT_DIGIT_AFTER_DECIMAL==3){
//            Rpl=3;
//        } 
        
        int Rpl=Constants.AMOUNT_DIGIT_AFTER_DECIMAL; 
         if(Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
             Rpl=(Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.amountdecimalforcompany);   
         }
//        val = val + 1/Math.pow(10,Wtf.AMOUNT_DIGIT_AFTER_DECIMAL+2);
        double p = (double) Math.pow(10, Rpl);
        double sign = 1;
        if (Rval < 0) {
            sign = -1;
        }

        Rval = Math.abs(Rval) + 1 / (double) Math.pow(10, Rpl + 10);           // Changed for ERP-14410
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return (double) (tmp / p) * (sign);
    }
    
    public static double roundGSTValue(double Rval, String companyid) {
        int Rpl = Constants.GSTValue_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
            Rpl = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.gstAmountDigitAfterDecimal);
        }
        double p = (double) Math.pow(10, Rpl);
        double sign = 1;
        if (Rval < 0) {
            sign = -1;
        }

        Rval = Math.abs(Rval) + 1 / (double) Math.pow(10, Rpl + 10);
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return (double) (tmp / p) * (sign);
    }
    
        @Deprecated
        public static double roundUnitPrice(double Rval) {
        int Rpl=Constants.UNITPRICE_DIGIT_AFTER_DECIMAL;          
        double p = (double) Math.pow(10, Rpl);
        double sign = 1;
        if (Rval < 0) {
            sign = -1;
        }

        Rval = Math.abs(Rval) + 1 / (double) Math.pow(10, Rpl + 10);            // Changed for ERP-14410
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return (double) (tmp / p) * (sign);
    }
     // roundUnitPrice function is Overriden to get UNITPRICE_DIGIT_AFTER_DECIMAL from Map
     public static double roundUnitPrice(double Rval, String companyid) {
         int Rpl=Constants.UNITPRICE_DIGIT_AFTER_DECIMAL;  
         
         if(Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
             Rpl=(Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.unitpricedecimalforcompany);   
         }
         
        double p = (double) Math.pow(10, Rpl);
        double sign = 1;
        if (Rval < 0) {
            sign = -1;
        }
        Rval = Math.abs(Rval) + 1 / (double) Math.pow(10, Rpl + 10);            // Changed for ERP-14410
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return (double) (tmp / p) * (sign);
    }
    
    @Deprecated
    public static double roundQuantity(double Rval, int Rpl) {
        if (Constants.QUANTITY_DIGIT_AFTER_DECIMAL != 2) {
            Rpl = Constants.QUANTITY_DIGIT_AFTER_DECIMAL;
        }
        double p = (double) Math.pow(10, Rpl);
        Rval = Rval + 1 / (double) Math.pow(10, Rpl + 10);                      // Changed for ERP-14410
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return (double) tmp / p;
    }
    
    // roundQuantity function is Overriden to get QUANTITY_DIGIT_AFTER_DECIMAL from Map
    public static double roundQuantity(double Rval, String companyid) {
        int Rpl = Constants.QUANTITY_DIGIT_AFTER_DECIMAL;
        if(Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
           Rpl = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.quantitydecimalforcompany); 
        }
        double p = (double) Math.pow(10, Rpl);
        Rval = Rval + 1 / (double) Math.pow(10, Rpl + 10);                      // Changed for ERP-14410
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return (double) tmp / p;
    }
    
    @Deprecated
     public static double calculateBaseUOMQuatity(double quantity, double baseuomRate) {
         double baseUOMQuatity=roundQuantity(quantity * baseuomRate, Constants.QUANTITY_DIGIT_AFTER_DECIMAL);
         return baseUOMQuatity;
     }
    
     public static double calculateBaseUOMQuatity(double quantity, double baseuomRate, String companyid) {
         double baseUOMQuatity=roundQuantity(quantity * baseuomRate, companyid);
         return baseUOMQuatity;
     }

    @Deprecated
    public static String formattedAmount(double Rval) {
        Rval=round(Rval,Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
        DecimalFormat df = null;
        String str = getCompleteDFStringForAmount("###0.");
        df = new DecimalFormat(str);
        return df.format(Rval);
    }
    
    public static String formattedAmount(double Rval, String companyid) {
        Rval=round(Rval, companyid);
        DecimalFormat df = null;
        String str = getCompleteDFStringForAmount("###0.", companyid);
        df = new DecimalFormat(str);
        return df.format(Rval);
    }
    
    /*
     * @return -gives same value as WtfGlobal.getDates() method;
     */
    public static String getDates(Date financialYrStartDate, boolean start) throws ParseException, SessionExpiredException {
        DateFormat df = getGlobalDateFormatInRequestFormat();
        
        String returnDate = "";
        
        if (start) {
            returnDate = df.format(financialYrStartDate);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(financialYrStartDate);
            cal.add(Calendar.YEAR, 1);
            
            Date endDate = cal.getTime();
            cal.setTime(endDate);
            cal.add(Calendar.DATE, -1);
            
            endDate = cal.getTime();
            
            returnDate = df.format(endDate);
        }
        
        return returnDate;
    }
    public static HashMap<String, Date> getFinancialsDates(Date financialYrStartDate, Date BillDate) throws ParseException, SessionExpiredException {
        DateFormat df = getGlobalDateFormatInRequestFormat();
        HashMap<String, Date> hm = new HashMap<String, Date>();
        int billYear = 1900 + BillDate.getYear();

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(financialYrStartDate);
        cal1.set(Calendar.YEAR, billYear);
        Date startDate1 = cal1.getTime();
        cal1.setTime(startDate1);
        cal1.set(Calendar.YEAR, billYear + 1);
        cal1.add(Calendar.DATE, -1);
        Date endDate1 = cal1.getTime();
        if ((startDate1.before(BillDate) || startDate1.equals(BillDate)) && (endDate1.after(BillDate) || startDate1.equals(BillDate))) {
            hm.put("financialstartdate", startDate1);
            hm.put("financialenddate", endDate1);
            return hm;
        } else {
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(financialYrStartDate);
            cal2.set(Calendar.YEAR, billYear - 1);
            Date startDate2 = cal2.getTime();
            cal2.setTime(startDate2);
            cal2.add(Calendar.DATE, -1);
            cal2.set(Calendar.YEAR, billYear);
            Date endDate2 = cal2.getTime();
            hm.put("financialstartdate", startDate2);
            hm.put("financialenddate", endDate2);
            return hm;
        }
    }

    @Deprecated
    public static String formattedCommaSeparatedAmount(double Rval) {
        Rval=round(Rval,Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
        DecimalFormat df = null;
        String str = getCompleteDFStringForAmount("#,###,###,##0.");
        df = new DecimalFormat(str);
        return df.format(Rval);
    }
    
    public static String formattedCommaSeparatedAmount(double Rval, String companyid) {
        Rval=round(Rval, companyid);
        DecimalFormat df = null;
        String str = getCompleteDFStringForAmount("#,###,###,##0.", companyid);
        df = new DecimalFormat(str);
        return df.format(Rval);
    }
    
    @Deprecated
    public static String formattedQuantity(double Rval) {
        DecimalFormat df = null;
        String str;
        if (Constants.QUANTITY_DIGIT_AFTER_DECIMAL != 0) {
            str = getCompleteDFStringForQuantity("###0.");
        } else {
         //ERP-8911, Quantity formatted so that we can set decimal digit as per requirement.
            str = getCompleteDFStringForQuantity("######");
        }
        df = new DecimalFormat(str);
        return df.format(Rval);
    }
    
    public static String formattedQuantity(double Rval, String companyid) {
        DecimalFormat df = null;
        String str;
        int quantityDigit = Constants.QUANTITY_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
            quantityDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.quantitydecimalforcompany);
        }
        
        if (quantityDigit != 0) {
            str = getCompleteDFStringForQuantity("###0.", companyid);
        } else {
         //ERP-8911, Quantity formatted so that we can set decimal digit as per requirement.
            str = getCompleteDFStringForQuantity("######", companyid);
        }
        df = new DecimalFormat(str);
        return df.format(Rval);
    }
    

    @Deprecated
    public static String getFormattedUnitPrice(double value) {
        
        String str=getCompleteDFStringwithDigitNumber("###0.",Constants.UNITPRICE_DIGIT_AFTER_DECIMAL);
        DecimalFormat df = new DecimalFormat(str);
        return df.format(value);
    }
    
    public static String getFormattedUnitPrice(double value, String companyid) {
        
        String str=getCompleteDFStringwithDigitNumber("###0.", companyid);
        DecimalFormat df = new DecimalFormat(str);
        return df.format(value);
    }
    
    @Deprecated
    public static String formattedCommaSeparatedUnitPrice(double value) {
        
        String str=getCompleteDFStringwithDigitNumber("#,###,###,##0.",Constants.UNITPRICE_DIGIT_AFTER_DECIMAL);
        DecimalFormat df = new DecimalFormat(str);
        return df.format(value);
    }
    
    public static String formattedCommaSeparatedUnitPrice(double value, String companyid) {
        
        String str=getCompleteDFStringwithDigitNumber("#,###,###,##0.", companyid);
        DecimalFormat df = new DecimalFormat(str);
        return df.format(value);
    }
    
    public static String formattedCommaSeparatedQyantityJasper(double value,String companyid) {
        int Rpl = Constants.QUANTITY_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
            Rpl = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.quantitydecimalforcompany);
        }
        String str = getCompleteDFStringForQuantity("#,###,###,##0.", companyid);
        DecimalFormat df = new DecimalFormat(str);
        return df.format(value);
    }

    @Deprecated
    public static String getCompleteDFStringForAmount(String str) {
        String result = str;
        for (int i = 0; i < Constants.AMOUNT_DIGIT_AFTER_DECIMAL; i++) {
            result = result + "0";
        }
        return result;
    }
    
    public static String getCompleteDFStringForAmount(String str, String companyid) {
        String result = str;
        int amountDigit = Constants.AMOUNT_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
            amountDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.amountdecimalforcompany);
        }
        
        for (int i = 0; i < amountDigit; i++) {
            result = result + "0";
        }
        return result;
    }
    
    @Deprecated
    public static String getCompleteDFStringwithDigitNumber(String str,int no) {
        String result = str;
        for (int i = 0; i < no; i++) {
            result = result + "0";
        }
        return result;
    }
    
    public static String getCompleteDFStringwithDigitNumber(String str, String companyid) {
        int unitPriceDigit=Constants.UNITPRICE_DIGIT_AFTER_DECIMAL;  
         
         if(Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
             unitPriceDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.unitpricedecimalforcompany);   
         }
        String result = str;
        for (int i = 0; i < unitPriceDigit; i++) {
            result = result + "0";
        }
        return result;
    }

    @Deprecated
    public static String getCompleteDFStringForQuantity(String str) {
        String result = str;
        for (int i = 0; i < Constants.QUANTITY_DIGIT_AFTER_DECIMAL; i++) {
            result = result + "0";
        }
        return result;
    }
    
    public static String getCompleteDFStringForQuantity(String str, String companyid) {
        String result = str;
        int quantityDigit = Constants.QUANTITY_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
            quantityDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.quantitydecimalforcompany);
        }
        for (int i = 0; i < quantityDigit; i++) {
            result = result + "0";
        }
        return result;
    }
    
    public static String formattingdecimal(double Rval, int Rpl) {// to return no of zeros after the decimals (company specific)
        DecimalFormat df = null;
        String result = ("###0.");
        for (int i = 0; i < Rpl ; i++) {
            result = result + "0";
        }

        df = new DecimalFormat(result);
        return df.format(Rval);
    }
    
    public static String formattingDecimalForUnitPrice(double Rval, String companyid) {// to return no of zeros after the decimals (company specific)
        int unitPriceDigit = Constants.UNITPRICE_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
            unitPriceDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.unitpricedecimalforcompany);
        }
        String value = formattingdecimal(Rval, unitPriceDigit);
        return value;
    }

    public static String formattingDecimalForAmount(double Rval, String companyid) {// to return no of zeros after the decimals (company specific)
        int amountDigit = Constants.AMOUNT_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
            amountDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.amountdecimalforcompany);
        }
        String value = formattingdecimal(Rval, amountDigit);
        return value;
    }

    public static String formattingDecimalForQuantity(double Rval, String companyid) {// to return no of zeros after the decimals (company specific)
        int quantityDigit = Constants.QUANTITY_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
            quantityDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.quantitydecimalforcompany);
        }
        String value = formattingdecimal(Rval, quantityDigit);
        return value;
    }
    /**
     * get Date with start minimum time stamp as default 00.00.00 
     * @param date
     * @return Date with time stamp
     * Ex : return date 2016-01-01 00.00.00
     */
    public static Date minDate(Date date) {
        return setDateTime(date, 0, 0, 0);
    }

    /**
     * get Date with start maximum time stamp as default 23.59.59 
     * @param date
     * @return Date with time stamp
     */
    public static Date maxDate(Date date) {
        return setDateTime(date, 23, 59, 59);
    }
    /**
     * get Date with time stamp
     * @param date
     * @param hour
     * @param minute
     * @param second
     * @return 
     */
    private static Date setDateTime(Date date, int hour, int minute, int second) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        return c.getTime();
    }
    
    public static DateFormat getDateFormatter(String userTimeFormatId, String timeZoneDiff) throws ServiceException {
        SimpleDateFormat sdf = null;
        try {
            String dateformat = "";
            if (userTimeFormatId.equals("1")) {
                dateformat = "MMMM d, yyyy hh:mm:ss aa";
            } else {
                dateformat = "MMMM d, yyyy HH:mm:ss";
            }
            sdf = new SimpleDateFormat(dateformat);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT" + timeZoneDiff));
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.getDateFormatter", e);
        }
        return sdf;
    }
    
    /**
     * This DateFormat object has used previously to convert the date object as
     * per User's Timezone.       *
     * @deprecated use {@link #getOnlyDateFormat() OR getOnlyDateFormat(HttpServletRequest request)}
     * instead.
     */
    @Deprecated
    public static DateFormat getDateOnlyFormatter(HttpServletRequest request)
            throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));
        return sdf;
    }
    
    public static DateFormat getDateOnlyFormatter(JSONObject paramJobj)
            throws SessionExpiredException, JSONException {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + paramJobj.getString(Constants.timezonedifference)));
        return sdf;
    }
   
    public static DateFormat getOnlyDateFormat(HttpServletRequest request)
            throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));
        return sdf;
    }
   
    public static DateFormat getOnlyDateFormat() throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf;
    }
    
    public static DateFormat getPrefDateFormatter(String userTimeFormatId, String timeZoneDiff, String pref) throws ServiceException {
        SimpleDateFormat sdf = null;
        try {
            String dateformat = "";
            if (userTimeFormatId.equals("1")) {
                dateformat = pref.replace('H', 'h');
                if (!dateformat.equals(pref)) {
                    dateformat += " a";
                }
            } else {
                dateformat = pref;
            }
            sdf = new SimpleDateFormat(dateformat);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT" + timeZoneDiff));
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.getPrefDateFormatter", e);
        }
        return sdf;
    }

    public static DateFormat getTimeFormatter(String userTimeFormatId) throws ServiceException {
        SimpleDateFormat sdf = null;
        try {
            String dateformat = "";
            if (userTimeFormatId.equals("1")) {
                dateformat = " hh:mm:ss aa ";
            } else {
                dateformat = "HH:mm:ss";
            }
            sdf = new SimpleDateFormat(dateformat);
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.getTimeFormatter", e);
        }
        return sdf;
    }

    public static String generateNewPassword() throws ServiceException {
        String randomStr = "";
        try {
            randomStr = RandomStringUtils.random(8, true, true);
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.generateNewPassword", e);
        }
        return randomStr;
    }

    public static String getSHA1(String inStr) throws ServiceException {
        String outStr = inStr;
        try {
            byte[] theTextToDigestAsBytes = inStr.getBytes("utf-8");

            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha.digest(theTextToDigestAsBytes);

            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                String h = Integer.toHexString(b & 0xff);
                if (h.length() == 1) {
                    sb.append("0" + h);
                } else {
                    sb.append(h);
                }
            }
            outStr = sb.toString();
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.getSHA1", e);
        }
        return outStr;
    }

    //Calculate no of days between two dates
    public static final double diffDays(Date from, Date to) {
        return ((to.getTime() - from.getTime()) / Constants.DAY_MILLIS)+1;
    }
    public static JSONArray sortJson(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject ja, JSONObject jb) {
                    double sr1 = 0, sr2 = 0;
                    try {
                        sr1 = Integer.parseInt(ja.optString("srNoForRow", "0"));
                        sr2 = Integer.parseInt(jb.optString("srNoForRow", "0"));
                    } catch (Exception ex) {
                        Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (sr1 > sr2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });

        } catch (JSONException ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONArray(jsons);
    }

    //Use this formatter to convert UTC date to User Timezone date  //Ajit. A
    public static String getUTCToUserLocalDateFormatter_NEW(HttpServletRequest request, Date date) throws SessionExpiredException {
        String FinalDate = "";
        DateTime jodatime = UTCToUserLocalDateFormatter(request, date);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern(sessionHandlerImpl.getUserDateFormat(request));
        FinalDate = dtfOut.print(jodatime);
        return FinalDate;
    }
    
    private static DateTime UTCToUserLocalDateFormatter(HttpServletRequest request, Date date) throws SessionExpiredException{
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String TempDate = sdf1.format(date);
        TimeZone TargetTZ = TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request));
        DateTimeZone TargetTimezone = DateTimeZone.forTimeZone(TargetTZ);
        DateTimeZone SourceTimezone = DateTimeZone.forID("GMT");
        DateTime SourceDate = new DateTime(TempDate, SourceTimezone);
        DateTime TargetDate = SourceDate.withZone(TargetTimezone);
        //Format it as per User's Date Format.
        String FormatTargetDate = TargetDate.toString();
        if (!StringUtil.isNullOrEmpty(FormatTargetDate)) {
            FormatTargetDate = FormatTargetDate.replaceAll("T", " ");
            FormatTargetDate = FormatTargetDate.substring(0, FormatTargetDate.lastIndexOf("."));
        }
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime jodatime = dtf.parseDateTime(FormatTargetDate);
        return jodatime;
    }
    
    public static String getUTCToUserLocalDateFormatter_NEWJson(JSONObject paramJObj, Date date) throws SessionExpiredException {
        String FinalDate = "";
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");;
        String TempDate = sdf1.format(date);
        TimeZone TargetTZ = TimeZone.getTimeZone("GMT" + paramJObj.optString(Constants.timezonedifference));
        DateTimeZone TargetTimezone = DateTimeZone.forTimeZone(TargetTZ);
        DateTimeZone SourceTimezone = DateTimeZone.forID("GMT");
        DateTime SourceDate = new DateTime(TempDate, SourceTimezone);
        DateTime TargetDate = SourceDate.withZone(TargetTimezone);
        //Format it as per User's Date Format.
        String FormatTargetDate = TargetDate.toString();
        if (!StringUtil.isNullOrEmpty(FormatTargetDate)) {
            FormatTargetDate = FormatTargetDate.replaceAll("T", " ");
            FormatTargetDate = FormatTargetDate.substring(0, FormatTargetDate.lastIndexOf("."));
        }
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime jodatime = dtf.parseDateTime(FormatTargetDate);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern(paramJObj.optString(Constants.userdateformat,"yyyy-MM-dd'T'HH:mm:ss"));
        FinalDate = dtfOut.print(jodatime);
        return FinalDate;
    }

    //Ajit. A
    public static DateFormat getSimpleDateAndTimeFormat(HttpServletRequest request) throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return sdf;
    }
    //To get new Date() in User's Time zone, parameters(ether User' timezone difference, or company creator's timezone difference)
    public static Date getUserNewDate(String userTZDiff ,String companyTZDiff ) throws SessionExpiredException {
        DateTime jodatime = null;
        TimeZone TargetTZ = null ;
        try {
            String FinalDate = "";
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date newUserDate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            String TempDate = sdf1.format(newUserDate);
            if(!StringUtil.isNullOrEmpty(userTZDiff)){
                TargetTZ = TimeZone.getTimeZone("GMT" + userTZDiff );
            }else{
                TargetTZ = TimeZone.getTimeZone("GMT" + companyTZDiff );
            }
            DateTimeZone TargetTimezone = DateTimeZone.forTimeZone(TargetTZ);
            DateTimeZone SourceTimezone = DateTimeZone.forID("GMT");
            DateTime SourceDate = new DateTime(TempDate, SourceTimezone);
            DateTime TargetDate = SourceDate.withZone(TargetTimezone);
            //Format it as per User's Date Format.
            String FormatTargetDate = TargetDate.toString();
            if (!StringUtil.isNullOrEmpty(FormatTargetDate)) {
                FormatTargetDate = FormatTargetDate.replaceAll("T", " ");
                FormatTargetDate = FormatTargetDate.substring(0, FormatTargetDate.lastIndexOf("."));
            }
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            jodatime = dtf.parseDateTime(FormatTargetDate);
            DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            FinalDate = dtfOut.print(jodatime);
    //        return FinalDate;
        } catch (ParseException ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jodatime.toDate();
    }

    //Ajit. A
    public static DateFormat getSimpleDateAndTimeFormat() throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf;
    }

    //**To convert UTC/GMT date to User local time(i.e. date + TimeZoneDiff)
    public static DateFormat getUTCToUserLocalDateFormatter(HttpServletRequest request) throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdf.setTimeZone(TimeZone.getTimeZone(sessionHandlerImpl.getTimeZoneDifference(request)));
        return sdf;
    }

    //Convert to UTC/GMT
    public static DateFormat getConstantDateFormatter() throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf;
    }

    //For Integration Purpose  //Deepak S.
    public static DateFormat getDateWithTimeFormat() throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf;
    }

    //Use this to convert date into User timezone date by passing Date Pattern. Use this for Jasper Report   // Vaibhav P.
    /**
     * This DateFormat object has used previously to convert the date object as
     * per User's Timezone.       *
     * @deprecated use {@link #getUserDateFormatterWithoutTimeZone() OR getUserDateFormatterWithoutTimeZone(HttpServletRequest request)}
     * instead.
     */
    @Deprecated
    public static DateFormat getClientPDFDateFormatter(HttpServletRequest request, String datePattern) //ERP-15947(1)
            throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));
        return sdf;
    }

    /**
     * @deprecated use {@link #getDateOnlyFormat()}
     */
    @Deprecated
    public static DateFormat getDateOnlyFormat(HttpServletRequest request) //ERP-13711
            throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat(getDateOnlyFormatPattern());
        return sdf;
    }

    // Added below function for ERP-36334
    public static String getFormatedDate(Date date, String format) throws SessionExpiredException {
        String result = "";
        if (date != null && !StringUtil.isNullOrEmpty(format)) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            result = sdf.format(date);
        }
        return result;
    }

    //Use this formatter instead of getDateFormatter(). Use this very commonly.
    public static DateFormat getDateOnlyFormat() //ERP-13711
            throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat(getDateOnlyFormatPattern());
        return sdf;
    }

    public static String getDateOnlyFormatPattern(){
        return Constants.MMMMdyyyy;        
    }
    
    //Used this formmater for Recurring Purpose //Vaibhav P.
    public static DateFormat getCompanyTimezoneDiffFormat(String companyTZDiff)
            throws SessionExpiredException {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
        //String companyid = sessionHandlerImpl.getCompanyTZDiff(request);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + companyTZDiff));
        return sdf;
    } 
    
    //Used this method to send SMTP config - Static Method
    public static Map<String, Object> getSMTPConfigMap(Company company){
        Map<String, Object> SMTPConfig = new HashMap<String, Object>();
        try {
            SMTPConfig.put("SMTPFlow", company.getSmtpflow());
            SMTPConfig.put("SMTPPath", company.getMailserveraddress());
            SMTPConfig.put("SMTPPort", company.getMailserverport());
            SMTPConfig.put("SMTPUsername", company.getEmailID());
            SMTPConfig.put("SMTPPassword", company.getSmtppassword());
            SMTPConfig.put("replytoemail", company.getReplytoemail());
        } catch (Exception ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, "AuthHandler.getSMTPConfig", ex);

        }
        return SMTPConfig;
    }
    
    public static boolean isAdminSubDomain(HttpServletRequest request) {
        boolean isAdminSubDomain = false;
        try {
            String subdomain = sessionHandlerImpl.getCompanySessionObj(request).getCdomain();
            if (!StringUtil.isNullOrEmpty(subdomain) && subdomain.equalsIgnoreCase("admin")) {
                isAdminSubDomain = true;
}
        } catch (Exception ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return isAdminSubDomain;
    }
     /**
     * Description: This method is used to formatting check data as per Font
     * style,size etc.
     * @param requestMap
     * @return String
     * @throws ServiceException
     */
 
    public static String applyFontStyleForCheque(Map<String, Object> requestMap) throws ServiceException {
        String fontString = "";
        String fontSize = "";
        boolean isFontStylePresent = false;
        String fontStyle = "";
        if (requestMap.containsKey("fontStyle") && !StringUtil.isNullOrEmpty((String) requestMap.get("fontStyle"))) {
            isFontStylePresent = true;
            fontStyle = (String) requestMap.get("fontStyle");
        }
        if (requestMap.containsKey("fontString") && !StringUtil.isNullOrEmpty((String) requestMap.get("fontString"))) {
            fontString = (String) requestMap.get("fontString");
        }
        if (requestMap.containsKey("fontSize") && !StringUtil.isNullOrEmpty((String) requestMap.get("fontSize"))) {
            fontSize = (String) requestMap.get("fontSize");
        }
        char fontStyleChar;
        if (fontStyle.equals("1")) {
            fontStyleChar = 'b';
        } else if (fontStyle.equals("2")) {
            fontStyleChar = 'i';
        } else {
            fontStyleChar = 'p';
        }

        if (isFontStylePresent && !StringUtil.isNullOrEmpty(fontSize)) {
            fontString = "<font size=" + fontSize + "><" + fontStyleChar + ">" + fontString + "</" + fontStyleChar + "></font> ";
        } else if (!StringUtil.isNullOrEmpty(fontSize)) {
            fontString = "<font size=" + fontSize + ">" + fontString + "</font> ";
        } else {
            fontString = "<" + fontStyleChar + ">" + fontString + "</" + fontStyleChar + ">";
        }
        return fontString;
    }
    /**
     * method to get number of digits after decimal points
     * 
     */
    public static String getNumberOfdigitsAfterDecimal(String amount, String companyid) throws ServiceException {

        int digitsAfterDecimalForcompany = 2;
        /**
         * getting number of digits after decimal points from company
         * preferences
         */
        if (Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
            digitsAfterDecimalForcompany = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.amountdecimalforcompany);
        }
        String digitsAfterDecimal = "0";
        if (amount.contains(".")) {
            digitsAfterDecimal = Integer.toString(amount.substring(amount.indexOf('.'), amount.length()).length() - 1);
        } else {
            /**
             * if decimal point is not present then append the decimal point and
             * number of zeroes equal to company preferences digits
             */
            digitsAfterDecimal += ".";
            for (int d = 0; d < digitsAfterDecimalForcompany; d++) {
                digitsAfterDecimal += "0";
            }
            digitsAfterDecimal = Integer.toString(digitsAfterDecimal.substring(digitsAfterDecimal.indexOf('.'), digitsAfterDecimal.length()).length() - 1);
        }
        return digitsAfterDecimal;
    }
    /**
     * Function to return GST status for the sub-domain
     * @param paramMap
     * @return
     * @throws ServiceException 
     */
    public static byte getGSTStatus(Map<String, Object> paramMap) throws ServiceException {

        boolean isNewGSTOnly = false;
        int countryId = 0;
        if (paramMap.containsKey("countryId") && !StringUtil.isNullOrEmpty((String) paramMap.get("countryId"))) {
            countryId = (Integer) paramMap.get("countryId");
        }
        if (paramMap.containsKey("isNewGSTOnly") && !StringUtil.isNullOrEmpty((String) paramMap.get("isNewGSTOnly"))) {
            isNewGSTOnly = (Boolean) paramMap.get("isNewGSTOnly");
        } 
        
        if(isNewGSTOnly){
            return Constants.NEW_GST_ONLY;
        }else if(!isNewGSTOnly && countryId == Constants.indian_country_id){
            return Constants.OLD_NEW_GST;
        }else{
            return Constants.NONEGST;
        }
    }
}
