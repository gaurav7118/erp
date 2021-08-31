/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.Approvalhistory;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accAccountHandler;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;

public class AccFinancialReportsServiceImpl implements AccFinancialReportsService {

    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accProductDAO accProductObj;
    private accAccountDAO accAccountDAOobj;
    private String successView;
    private MessageSource messageSource;
    private AccReportsService accReportsService;
    private AccProductService AccProductService;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accInvoiceCMN accInvoiceCommon;
    private accJournalEntryDAO accJournalEntryobj;
    private fieldDataManager fieldDataManagercntrl;
    private accTaxDAO accTaxObj;
    private accPaymentDAO accPaymentDAOobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAO;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private fieldManagerDAO fieldManagerDAOobj;
    
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
  
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
        
    public void setAccProductService(AccProductService AccProductService) {
        this.AccProductService = AccProductService;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }   
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }
    public void setaccGoodsReceiptServiceDAO(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAO) {
        this.accGoodsReceiptServiceDAO = accGoodsReceiptServiceDAO;
    }
    @Override
    @Deprecated
    public JSONObject getMonthlyTradingProfitLossJasperExport(HttpServletRequest request, boolean monthYearFormat) throws ServiceException, SessionExpiredException {
        JSONObject jobj1 = new JSONObject();
        try {
            CompanyAccountPreferences pref = null;
            KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            }
            if (!pref.isShowchild()) {// Anup Check to hide child customer / vendors from balance sheet
                request.setAttribute("hidechildCV", true);
            }

            JSONObject jArrR = new JSONObject();
            JSONObject jArrL = new JSONObject();
            JSONObject paramJobj = new JSONObject();
            JSONArray temprightObjArr1 = new JSONArray();
            JSONArray templeftObjArr = new JSONArray();
            LocalDate localStartDate = null;
            LocalDate localEndDate = null;
            Date endDate = null;
            Date startDate = null;
            if (monthYearFormat) {
                final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
                localStartDate = dtf.parseLocalDate(request.getParameter("stdate"));
                localEndDate = dtf.parseLocalDate(request.getParameter("enddate"));
                startDate = localStartDate.toDate();
                endDate = localEndDate.toDate();
            } else {
                endDate = authHandler.getDateOnlyFormat().parse(request.getParameter("enddate"));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                startDate = authHandler.getDateOnlyFormat().parse(request.getParameter("stdate"));
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(startDate);
                localStartDate = new LocalDate(startDate);
                localEndDate = new LocalDate(endDate);
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int startMonth = cal.get(Calendar.MONTH);
            int startYear = cal.get(Calendar.YEAR);
            cal.setTime(endDate);
            int endMonth = cal.get(Calendar.MONTH);
            int endYear = cal.get(Calendar.YEAR);
            boolean isOneMonth = false;
            int monthCount = 0;
            // if user selects same month & year for both start & end fields, we get the last date of the month to populate into the endDate
            if (startMonth == endMonth && startYear == endYear) {
                DateTime date = localEndDate.toDateTime(LocalTime.MIDNIGHT);
                date = date.plusSeconds(86399);
                endDate = date.dayOfMonth().withMaximumValue().toDate();
                isOneMonth = true;
                localEndDate = new LocalDate(endDate);
                monthCount = 1;
            }

            if (!isOneMonth) { // just a trick to include the last month as well
                localEndDate = localEndDate.plus(Period.months(1));
            }

            while (localStartDate.isBefore(localEndDate)) {
                localStartDate = localStartDate.plus(Period.months(1));
                monthCount++;
            }

            // GOING TO CALCULATE THE MONTHLY OPEN & CLOSING BALANCE - START
            if (monthYearFormat) {
                final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
                localStartDate = dtf.parseLocalDate(request.getParameter("stdate"));
                localEndDate = dtf.parseLocalDate(request.getParameter("enddate"));
            } else {
                localStartDate = new LocalDate(startDate);
                localEndDate = new LocalDate(endDate);
            }
            int monthIndex = 0;
            if (!isOneMonth) {// just a trick to include the last month as well
                localEndDate = localEndDate.plus(Period.months(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }

            /**
             * When from date and to date are equal i.e. single month is selected then substract 2 from monthcount
             */
            int monthsToSubstract = isOneMonth ? 2 : 1;

            while (localStartDate.isBefore(localEndDate) && (monthIndex <= (monthCount - 2) && !monthYearFormat) || (monthIndex <= (monthCount - monthsToSubstract) && monthYearFormat)) { //we give PDF generation option in two places  since from one place months are coming exactly 12 and in other case months are 13 to handle this addition conditions are required
                DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                Date startDate1;
                if (monthIndex == 0) {
                    if (!monthYearFormat) {
                        startDate1 = authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
                    } else {
                        Date tempDate = localStartDate.toDate();
                        Calendar cal1 = Calendar.getInstance();
                        Date d = new Date();
                        SimpleDateFormat df = new SimpleDateFormat();
                        df.applyPattern(Constants.DATEFORMATINGPATTERN);
                        cal1.setTime(df.parse(df.format(tempDate)));//cal1.setTime(new Date(df.format(tempDate)));
                        String sstart = authHandler.getDateOnlyFormat().format(cal1.getTime());
                        startDate1 = authHandler.getDateOnlyFormat().parse(sstart);
                        // startDate1 = cal1.getTime();
                    }
                } else {
                    DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();
                    Date tempDate = firstDateOfMonth.toDate();
                    Calendar cal1 = Calendar.getInstance();
                    Date d = new Date();
                    SimpleDateFormat df = new SimpleDateFormat();
                    df.applyPattern(Constants.DATEFORMATINGPATTERN);
                    cal1.setTime(df.parse(df.format(tempDate)));//cal1.setTime(new Date(df.format(tempDate)));
                    String sstart = authHandler.getDateOnlyFormat().format(cal1.getTime());
                    startDate1 = authHandler.getDateOnlyFormat().parse(sstart);
                    //startDate1 = cal1.getTime();
                }
                DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                //  including whole last day for calculation             
                request.setAttribute("jasperreport", "JasperReport");
                request.setAttribute("monthlyreport", "MonthlyReport");
                request.setAttribute("jaspersdate", startDate1);
                Calendar endcal = Calendar.getInstance();
                if (endDate != null) {
                    if ((monthIndex == (monthCount - 2) && !monthYearFormat)) {
                        endcal.setTime(authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate")));
                    } else {
                        lastDateOfMonth = lastDateOfMonth.plusDays(0);  //For Monthly Revenue Report Total Income
                        Date tempDate = lastDateOfMonth.toDate();
                        Date d = new Date();
                        SimpleDateFormat df = new SimpleDateFormat();
                        df.applyPattern(Constants.DATEFORMATINGPATTERN);
                        endcal.setTime(df.parse(df.format(tempDate)));//endcal.setTime(new Date(df.format(tempDate)));
                    }
                }
                String sstart = authHandler.getDateOnlyFormat().format(endcal.getTime());
                request.setAttribute("jasperenddate", authHandler.getDateOnlyFormat().parse(sstart));
                paramJobj = StringUtil.convertRequestToJsonObject(request);
                JSONObject jobj = getTradingAndProfitLoss(paramJobj);
                JSONObject jobj2 = jobj.getJSONObject(Constants.RES_data);
                JSONArray rightObjArr1 = jobj2.getJSONArray("right");
                JSONArray leftObjArr = jobj2.getJSONArray("left");
                if (monthIndex == 0) {
                    temprightObjArr1 = jobj2.getJSONArray("right");
                    templeftObjArr = jobj2.getJSONArray("left");
                }

                //put info into left json
                getJSONArrayForTNPL(leftObjArr, jArrL, monthIndex, templeftObjArr);
                //put info into right json
                getJSONArrayForTNPL(rightObjArr1, jArrR, monthIndex, temprightObjArr1);
                
                    localStartDate = localStartDate.plus(Period.months(1));
                    monthIndex++;
            }// end looping thru the months  

            //puting calculated total at the end
            int totalpos = monthYearFormat && !isOneMonth ? monthCount : monthCount - 1;
            paramJobj.put("monthCount",totalpos);
            paramJobj.put("monthYearFormat",monthYearFormat);
            paramJobj.put("showZeroAmountAsBlank", extrapref.isShowZeroAmountAsBlank());
//            for (int i = 0; i < temprightObjArr1.length(); i++) {
//                JSONObject getObj1 = temprightObjArr1.getJSONObject(i);
//                if (getObj1.has("accountid")) {
//                    String accId = getObj1.getString("accountid");
//                    if (jArrR.has(accId)) {
//                        JSONObject getObj = jArrR.getJSONObject(accId);
//                        if (getObj.has("totalamount")) {
//                            if (!extrapref.isShowAllAccountsInPnl()) {
//                                //To remove 0 amount account and which should not be at level 0.
//                                double totalAmt = Double.parseDouble(getObj.get("totalamount").toString());
//                                double level = Double.parseDouble(getObj.get("level").toString());
//                                if (totalAmt == 0.0 && level != 0.0) {
//                                    jArrR.remove(accId);
//                                } else {
//                                getObj.put("amount_" + totalpos, getObj.get("totalamount"));
//                                }
//                            } else {
//                                getObj.put("amount_" + totalpos, getObj.get("totalamount"));
//                            }
                                
//                            if (monthYearFormat && getObj.has("accountid") && (getObj.getString("accountid").equals(Constants.Finish_Products_account) || getObj.getString("accountid").equals(Constants.Raw_Materials_account) || getObj.getString("accountid").equals(Constants.Total_Closing_Stock_account) )) {
//                                //monthYearFormat flag is used to bypass this block when this function called from other report except monthly revenue ,monthly trading and profit loss.
//                                //Total column for Closing Stock,Finish Products,Raw Materials entity will show amount in last month's column value and not the sum of all column's amount like other normal accounts.
//                                if (!StringUtil.isNullOrEmpty(getObj.getString("amount_" + String.valueOf(totalpos - 1)))) {
//                                    getObj.put("amount_" + totalpos, getObj.getDouble("amount_" + String.valueOf(totalpos - 1)));
//                                } else {
//                                    getObj.put("amount_" + totalpos, "");
//                        }
//                    }
//                }
//            }
//                }
//            }

//            for (int i = 0; i < templeftObjArr.length(); i++) {
//                JSONObject getObj1 = templeftObjArr.getJSONObject(i);
//                if (getObj1.has("accountid")) {
//                    String accId = getObj1.getString("accountid");
//                    if (jArrL.has(accId)) {
//                        JSONObject getObj = jArrL.getJSONObject(accId);
//                        if (getObj.has("totalamount")) {
//                            if (!extrapref.isShowAllAccountsInPnl()) {
//                                //To remove 0 amount account and which should not be at level 0.
//                                double totalAmt = Double.parseDouble(getObj.get("totalamount").toString());
//                                double level = Double.parseDouble(getObj.get("level").toString());
//                                if (totalAmt == 0.0 && level != 0.0) {
//                                    jArrL.remove(accId);
//                                } else {
//                                getObj.put("amount_" + totalpos, getObj.get("totalamount"));
//                                }
//                            } else {
//                                getObj.put("amount_" + totalpos, getObj.get("totalamount"));
//                            }
                           
//                            if (monthYearFormat && getObj.has("accountid") && getObj.getString("accountid").equals(Constants.Opening_Stock_account)) {
//                                //Total column for Opening Stock entity will show amount in first month's column value and not the sum of all column's amount like other normal accounts.
//                                if (!StringUtil.isNullOrEmpty(getObj.getString("amount_0"))) {
//                                    getObj.put("amount_" + totalpos, getObj.getDouble("amount_0"));
//                                }else{
//                                    getObj.put("amount_" + totalpos, "");
//                        }
//
//                    }
//                }
//            }
//                }
//            }

            List<String> monthList = new ArrayList();
            localStartDate = new LocalDate(startDate);
            localEndDate = new LocalDate(endDate);
            // just a trick to include the last month as well
            if (!isOneMonth) {
                localEndDate = localEndDate.plus(Period.months(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }
            while (localStartDate.isBefore(localEndDate)) {
                String monthName = localStartDate.toString("MMM yyyy");
                localStartDate = localStartDate.plus(Period.months(1));
                monthList.add(monthName);
            }
            // the first object would be the months array
            int monthlist = monthYearFormat ? monthList.size() : (isOneMonth ? monthList.size() : (monthList.size()-1));
            JSONArray monthArray = new JSONArray();
            JSONObject monthObj;
            for (int i = 0; i < monthlist; i++) {
                monthObj = new JSONObject();
                monthObj.put("monthname", monthList.get(i));
                monthArray.put(monthObj);
            }
            monthObj = new JSONObject();
            monthObj.put("monthname", "Total");
            monthArray.put(monthObj);
            // Add title and header
            if(monthYearFormat){
                Map<String, String> headerTitleMap = getColumnHeaderAndTitlesFromMonthList(monthArray,extrapref.isShowAccountCodeInFinancialReport());
                String titles = headerTitleMap.get("titles");
                String headers = headerTitleMap.get("headers");
                String aligns = headerTitleMap.get("aligns");
                if(request.getAttribute("header")!=null){
                    request.removeAttribute("header");
                }
                if(request.getAttribute("title")!=null){
                    request.removeAttribute("title");
                }
                if(request.getAttribute("align")!=null){
                    request.removeAttribute("align");
                }
                request.setAttribute("header", headers);
                request.setAttribute("title", titles);
                request.setAttribute("align", aligns);
            }
            /**
             * Putting monthYearFormat true to use in getAccountsConvertedJSONArray.
             */
            if (paramJobj.has("isForBS") && paramJobj.optBoolean("isForBS", true)) {
                paramJobj.put("monthYearFormat", true);
            }
            jobj1.put("months", monthArray);
            jobj1.put("left", jArrL);
            jobj1.put("refleft", AccReportsHandler.getAccountsConvertedJSONArray(paramJobj,jArrL, templeftObjArr,extrapref.isShowAllAccountsInPnl(),"totalamount"));
            jobj1.put("right", jArrR);
            jobj1.put("refright", AccReportsHandler.getAccountsConvertedJSONArray(paramJobj,jArrR, temprightObjArr1,extrapref.isShowAllAccountsInPnl(),"totalamount"));
            /**
             * Reseting monthYearFormat flag
             */
            paramJobj.put("monthYearFormat", monthYearFormat);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getMonthlyTradingProfitLossExport : " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println(e);
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("getMonthlyTradingProfitLossExport : " + e.getMessage(), e);
        }
        return jobj1;
    }

    
    private JSONObject getMonthlyPeriodAmount(JSONObject paramJobj, boolean monthYearFormat, Map<String, Double> accAmtMap) throws ServiceException, SessionExpiredException {
        JSONObject jobj1 = new JSONObject();
        try {
            CompanyAccountPreferences pref = null;
            String companyid = paramJobj.getString(Constants.companyKey);
            KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            }
            if (!pref.isShowchild()) {// Anup Check to hide child customer / vendors from balance sheet
                paramJobj.put("hidechildCV", true);
            }
            boolean isBalanceSheet = paramJobj.optBoolean("isBalanceSheet",false);
            JSONObject jArrR = new JSONObject();
            JSONObject jArrL = new JSONObject();
            JSONArray temprightObjArr1 = new JSONArray();
            JSONArray templeftObjArr = new JSONArray();
            LocalDate localStartDate = null;
            LocalDate localEndDate = null;
            Date endDate = null;
            Date startDate = null;
            if (monthYearFormat) {
                final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
                localStartDate = dtf.parseLocalDate(paramJobj.getString("stdate"));
                localEndDate = dtf.parseLocalDate(paramJobj.getString("enddate"));
                startDate = localStartDate.toDate();
                endDate = localEndDate.toDate();
            } else {
                endDate = authHandler.getDateOnlyFormat().parse(paramJobj.getString("enddate"));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                startDate = authHandler.getDateOnlyFormat().parse(paramJobj.getString("stdate"));
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(startDate);
                localStartDate = new LocalDate(startDate);
                localEndDate = new LocalDate(endDate);
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int startMonth = cal.get(Calendar.MONTH);
            int startYear = cal.get(Calendar.YEAR);
            cal.setTime(endDate);
            int endMonth = cal.get(Calendar.MONTH);
            int endYear = cal.get(Calendar.YEAR);
            boolean isOneMonth = false;
            int monthCount = 0;
            // if user selects same month & year for both start & end fields, we get the last date of the month to populate into the endDate
            if (startMonth == endMonth && startYear == endYear) {
                DateTime date = localEndDate.toDateTime(LocalTime.MIDNIGHT);
                date = date.plusSeconds(86399);
                endDate = date.dayOfMonth().withMaximumValue().toDate();
                isOneMonth = true;
                localEndDate = new LocalDate(endDate);
                monthCount = 1;
            }

            if (!isOneMonth) { // just a trick to include the last month as well
                localEndDate = localEndDate.plus(Period.months(1));
            }

            while (localStartDate.isBefore(localEndDate)) {
                localStartDate = localStartDate.plus(Period.months(1));
                monthCount++;
            }

            // GOING TO CALCULATE THE MONTHLY OPEN & CLOSING BALANCE - START
            if (monthYearFormat) {
                final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
                localStartDate = dtf.parseLocalDate(paramJobj.getString("stdate"));
                localEndDate = dtf.parseLocalDate(paramJobj.getString("enddate"));
            } else {
                localStartDate = new LocalDate(startDate);
                localEndDate = new LocalDate(endDate);
            }
            int monthIndex = 0;
            if (!isOneMonth) {// just a trick to include the last month as well
                localEndDate = localEndDate.plus(Period.months(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }
            

            while (localStartDate.isBefore(localEndDate) && ((monthIndex <= (monthCount - 2) && !monthYearFormat) || (monthIndex <= (monthCount - 1) && monthYearFormat))) { //we give PDF generation option in two places  since from one place months are coming exactly 12 and in other case months are 13 to handle this addition conditions are required
                DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                Date startDate1;
                paramJobj.put("isForTradingAndProfitLoss",true);
                if (monthIndex == 0) {
                    if (!monthYearFormat) {
                        startDate1 = authHandler.getDateOnlyFormat().parse(paramJobj.getString("stdate"));
                    } else {
                        Date tempDate = localStartDate.toDate();
                        Calendar cal1 = Calendar.getInstance();
                        Date d = new Date();
                        SimpleDateFormat df = new SimpleDateFormat();
                        df.applyPattern(Constants.DATEFORMATINGPATTERN);
                        cal1.setTime(df.parse(df.format(tempDate)));//cal1.setTime(new Date(df.format(tempDate)));
                        String sstart = authHandler.getDateOnlyFormat().format(cal1.getTime());
                        startDate1 = authHandler.getDateOnlyFormat().parse(sstart);
                        // startDate1 = cal1.getTime();
                    }
                } else {
                    DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();
                    Date tempDate = firstDateOfMonth.toDate();
                    Calendar cal1 = Calendar.getInstance();
                    Date d = new Date();
                    SimpleDateFormat df = new SimpleDateFormat();
                    df.applyPattern(Constants.DATEFORMATINGPATTERN);
                    cal1.setTime(df.parse(df.format(tempDate)));//cal1.setTime(new Date(df.format(tempDate)));
                    String sstart = authHandler.getDateOnlyFormat().format(cal1.getTime());
                    startDate1 = authHandler.getDateOnlyFormat().parse(sstart);
                    //startDate1 = cal1.getTime();
                }
                DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                //  including whole last day for calculation             
                paramJobj.put("jasperreport", "JasperReport");
                paramJobj.put("monthlyreport", "MonthlyReport");
                paramJobj.put("jaspersdate", startDate1);
                Calendar endcal = Calendar.getInstance();
                if (endDate != null) {
                    if ((monthIndex == (monthCount - 2) && !monthYearFormat)) {
                        endcal.setTime(authHandler.getDateOnlyFormat().parse(paramJobj.getString("enddate")));
                    } else {
                        lastDateOfMonth = lastDateOfMonth.plusDays(0);  //For Monthly Revenue Report Total Income
                        Date tempDate = lastDateOfMonth.toDate();
                        Date d = new Date();
                        SimpleDateFormat df = new SimpleDateFormat();
                        df.applyPattern(Constants.DATEFORMATINGPATTERN);
                        endcal.setTime(df.parse(df.format(tempDate)));//endcal.setTime(new Date(df.format(tempDate)));
                    }
                }
                String sstart = authHandler.getDateOnlyFormat().format(endcal.getTime());
                paramJobj.put("jasperenddate", authHandler.getDateOnlyFormat().parse(sstart));                
                paramJobj.put("yearpassed", String.valueOf(endcal.get(Calendar.YEAR)));
                paramJobj.put("monthpassed", String.valueOf(endcal.get(Calendar.MONTH)+1));
                
                if(isBalanceSheet){
                    JSONObject jobj = accReportsService.getBalanceSheetAllAccounts(paramJobj, accAmtMap);
                    JSONObject jobj2 = jobj.getJSONObject(Constants.RES_data);
                    JSONArray rightObjArr1 = jobj2.getJSONArray("right");
                    JSONArray leftObjArr = jobj2.getJSONArray("left");
                    if (monthIndex == 0) {
                        temprightObjArr1 = jobj2.getJSONArray("right");
                        templeftObjArr = jobj2.getJSONArray("left");
                    }

                        //put info into left json
                        getJSONArrayForBalanceSheet(leftObjArr, jArrL, monthIndex, templeftObjArr);
                        //put info into right json
                        getJSONArrayForBalanceSheet(rightObjArr1, jArrR, monthIndex, temprightObjArr1);
                }
                else{
                    JSONObject jobj = getTradingAndProfitLossAllAccounts(paramJobj, accAmtMap);
                    JSONObject jobj2 = jobj.getJSONObject(Constants.RES_data);
                    JSONArray rightObjArr1 = jobj2.getJSONArray("right");
                    JSONArray leftObjArr = jobj2.getJSONArray("left");
                    if (monthIndex == 0) {
                        temprightObjArr1 = jobj2.getJSONArray("right");
                        templeftObjArr = jobj2.getJSONArray("left");
                    }

                        //put info into left json
                        getJSONArrayForTNPL(leftObjArr, jArrL, monthIndex, templeftObjArr);
                        //put info into right json
                        getJSONArrayForTNPL(rightObjArr1, jArrR, monthIndex, temprightObjArr1);
                }
                localStartDate = localStartDate.plus(Period.months(1));
                monthIndex++;
            }// end looping thru the months  

            
            List<String> monthList = new ArrayList();
            localStartDate = new LocalDate(startDate);
            localEndDate = new LocalDate(endDate);
          
            paramJobj.put("monthCount",monthCount);
            // just a trick to include the last month as well
            if (!isOneMonth) {
                localEndDate = localEndDate.plus(Period.months(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }
            while (localStartDate.isBefore(localEndDate)) {
                String monthName = localStartDate.toString("MMM yyyy");
                localStartDate = localStartDate.plus(Period.months(1));
                monthList.add(monthName);
            }
            // the first object would be the months array
            int monthlist = monthYearFormat ? monthList.size() : monthList.size() - 1;
            JSONArray monthArray = new JSONArray();
            JSONObject monthObj;
            for (int i = 0; i < monthlist; i++) {
                monthObj = new JSONObject();
                monthObj.put("monthname", monthList.get(i));
                monthArray.put(monthObj);
            }
            monthObj = new JSONObject();
            monthObj.put("monthname", "Total");
            monthArray.put(monthObj);
            // Add title and header
            if(monthYearFormat){
                Map<String, String> headerTitleMap = getColumnHeaderAndTitlesFromMonthList(monthArray,extrapref.isShowAccountCodeInFinancialReport());
                String titles = headerTitleMap.get("titles");
                String headers = headerTitleMap.get("headers");
                String aligns = headerTitleMap.get("aligns");
                
                jobj1.put("header", headers);
                jobj1.put("title", titles);
                jobj1.put("align", aligns);
                }
            jobj1.put("months", monthArray);
            jobj1.put("left", jArrL);
            
            jobj1.put("right", jArrR);
            boolean showAllAccountsFlag = false;
            if(isBalanceSheet){
                showAllAccountsFlag = extrapref.isShowallaccountsinbs();
            }
            else{
                showAllAccountsFlag = extrapref.isShowAllAccountsInPnl();
            }
            paramJobj.put("monthYearFormat", monthYearFormat);
            jobj1.put("refleft",AccReportsHandler.getAccountsConvertedJSONArray(paramJobj,jArrL, templeftObjArr,showAllAccountsFlag,"totalamount")); 
            jobj1.put("refright",AccReportsHandler.getAccountsConvertedJSONArray(paramJobj,jArrR, temprightObjArr1,showAllAccountsFlag,"totalamount")); 
            
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getMonthlyTradingProfitLossExport : " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println(e);
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("getMonthlyTradingProfitLossExport : " + e.getMessage(), e);
        }
        return jobj1;
    }
    
    
    @Override
    public Map<String, String> getColumnHeaderAndTitlesFromMonthList(JSONArray monthArr,boolean isShowAccountCode) throws ServiceException, SessionExpiredException {
        Map<String, String> reportColumnMap=new HashMap<>();
        try{
            String titles= "Particulars";
            String headers="accountname";
            String aligns="none";
            if(isShowAccountCode){
                titles += ",Account Code";
                headers += ",accountcode";
                aligns += ",none";
            }
            for (int i = 0; i < monthArr.length(); i++) {
                headers += (",amount_" + i);
                titles += (", " + (monthArr.getJSONObject(i) != null ? monthArr.getJSONObject(i).getString("monthname") : ""));
                aligns += ",currency";
            }
            reportColumnMap.put("titles", titles);
            reportColumnMap.put("headers", headers);
            reportColumnMap.put("aligns", aligns);
        } catch (Exception e) {
            System.out.println(e);
        }
        return  reportColumnMap;
    }
    
    @Override
    public JSONObject getYearlyTradingProfitLossJasperExport(HttpServletRequest request, boolean monthYearFormat) throws ServiceException, SessionExpiredException {
        JSONObject jobj1 = new JSONObject();
        try {
            CompanyAccountPreferences pref = null;
            JSONObject paramJobj = new JSONObject();
            KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            }
            if (!pref.isShowchild()) {
                request.setAttribute("hidechildCV", true);
            }

            JSONObject jArrR = new JSONObject();
            JSONObject jArrL = new JSONObject();
            JSONArray temprightObjArr1 = new JSONArray();
            JSONArray templeftObjArr = new JSONArray();
            LocalDate localStartDate = null;
            LocalDate localEndDate = null;
            Date endDate = null;
            Date startDate = null;
            if (monthYearFormat) {
                final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
                localStartDate = dtf.parseLocalDate(request.getParameter("stdate"));
                localEndDate = dtf.parseLocalDate(request.getParameter("enddate"));
                startDate = localStartDate.toDate();
                endDate = localEndDate.toDate();
            } else {
                endDate = authHandler.getDateOnlyFormat().parse(request.getParameter("enddate"));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                startDate = authHandler.getDateOnlyFormat().parse(request.getParameter("stdate"));
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(startDate);
                localStartDate = new LocalDate(startDate);
                localEndDate = new LocalDate(endDate);
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);

            /*
             * we have to define which month is taken as starting month of Year
             * i.e. Jan or April
             */
            int startMonth = cal.get(Calendar.MONTH);
            int startYear = cal.get(Calendar.YEAR);
            cal.setTime(endDate);
            /*
             * we have to define which month is taken as ending month of Year
             * i.e. Dec or March
             */
            int endMonth = cal.get(Calendar.MONTH);
            int endYear = cal.get(Calendar.YEAR);
            boolean isOneYear = false;
            int yearCount = 0;
            // if user selects same month & year for both start & end fields, we get the last date of the month to populate into the endDate
            if (startMonth == endMonth && startYear == endYear) {
                DateTime date = localEndDate.toDateTime(LocalTime.MIDNIGHT);
                date = date.plusSeconds(86399);
                endDate = date.dayOfMonth().withMaximumValue().toDate();
                isOneYear = true;
                localEndDate = new LocalDate(endDate);
                yearCount = 1;
            }

            while (localStartDate.isBefore(localEndDate)) {
                localStartDate = localStartDate.plus(Period.years(1));
                yearCount++;
            }

            // GOING TO CALCULATE THE YEARLY OPEN & CLOSING BALANCE - START
            if (monthYearFormat) {
                final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
                localStartDate = dtf.parseLocalDate(request.getParameter("stdate"));
            } else {
                localStartDate = new LocalDate(startDate);
            }
            int yearIndex = 0;
            localEndDate = new LocalDate(endDate);

            while (localStartDate.isBefore(localEndDate) && (yearIndex <= (yearCount - 2) && !monthYearFormat) || (yearIndex <= (yearCount - 1) && monthYearFormat)) { //we give PDF generation option in two places  since from one place months are coming exactly 12 and in other case months are 13 to handle this addition conditions are required
                DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                Date startDate1;
                if (yearIndex == 0) {
                    if (!monthYearFormat) {
                        startDate1 = authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
                    } else {
                        Date tempDate = localStartDate.toDate();
                        Calendar cal1 = Calendar.getInstance();
                        Date d = new Date();
                        SimpleDateFormat df = new SimpleDateFormat();
                        df.applyPattern(Constants.DATEFORMATINGPATTERN);
                        cal1.setTime(df.parse(df.format(tempDate)));//cal1.setTime(new Date(df.format(tempDate)));
                        String sstart = authHandler.getDateOnlyFormat().format(cal1.getTime());
                        startDate1 = authHandler.getDateOnlyFormat().parse(sstart);
                        // startDate1 = cal1.getTime();
                    }
                } else {
                    DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();
                    Date tempDate = firstDateOfMonth.toDate();
                    Calendar cal1 = Calendar.getInstance();
                    Date d = new Date();
                    SimpleDateFormat df = new SimpleDateFormat();
                    df.applyPattern(Constants.DATEFORMATINGPATTERN);
                    cal1.setTime(df.parse(df.format(tempDate)));//cal1.setTime(new Date(df.format(tempDate)));
                    String sstart = authHandler.getDateOnlyFormat().format(cal1.getTime());
                    startDate1 = authHandler.getDateOnlyFormat().parse(sstart);
                    //startDate1 = cal1.getTime();
                }
                DateTime lastDateOfYear = date.dayOfYear().withMaximumValue();
                //  including whole last day for calculation             
                request.setAttribute("jasperreport", "JasperReport");
                request.setAttribute("monthlyreport", "MonthlyReport");
                request.setAttribute("jaspersdate", startDate1);
                Calendar endcal = Calendar.getInstance();
                if (endDate != null) {
                    if ((yearIndex == (yearCount - 2) && !monthYearFormat)) {
                        endcal.setTime(authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate")));
                    } else {
                        lastDateOfYear = lastDateOfYear.plusDays(0);  //For Monthly Revenue Report Total Income
                        Date tempDate = lastDateOfYear.toDate();
                        Date d = new Date();
                        SimpleDateFormat df = new SimpleDateFormat();
                        df.applyPattern(Constants.DATEFORMATINGPATTERN);
                        endcal.setTime(df.parse(df.format(tempDate)));//endcal.setTime(new Date(df.format(tempDate)));
                    }
                }
                String sstart = authHandler.getDateOnlyFormat().format(endcal.getTime());
                request.setAttribute("jasperenddate", authHandler.getDateOnlyFormat().parse(sstart));
                paramJobj = StringUtil.convertRequestToJsonObject(request);
                JSONObject jobj = getTradingAndProfitLoss(paramJobj);
                
                JSONObject jobj2 = jobj.getJSONObject(Constants.RES_data);
                JSONArray rightObjArr1 = jobj2.getJSONArray("right");
                JSONArray leftObjArr = jobj2.getJSONArray("left");
                if (yearIndex == 0) {
                    temprightObjArr1 = jobj2.getJSONArray("right");
                    templeftObjArr = jobj2.getJSONArray("left");
                }
                /*
                 * put info into left json
                 */
                getJSONArrayForTNPL(leftObjArr, jArrL, yearIndex, templeftObjArr);
                /*
                 * put info into right json
                 */
                getJSONArrayForTNPL(rightObjArr1, jArrR, yearIndex, temprightObjArr1);
                
                /*
                 * increamenting the year.
                 */
                localStartDate = localStartDate.plus(Period.years(1));
                yearIndex++;
            }// end looping thru the months  

            /*
             * puting calculated total at the end
             */
            int totalpos = monthYearFormat ? yearCount : yearCount - 1;
            paramJobj.put("monthCount",totalpos);
            paramJobj.put("monthYearFormat",monthYearFormat);

            /*
             * add totals into right json
             */
//            addTotalsInJSONArray(jArrR, extrapref.isShowAllAccountsInPnl(), totalpos, temprightObjArr1);

            /*
             * add totals into left json
             */
//            addTotalsInJSONArray(jArrL, extrapref.isShowAllAccountsInPnl(), totalpos, templeftObjArr);

            List<String> yearList = new ArrayList();
            localStartDate = new LocalDate(startDate);
            localEndDate = new LocalDate(endDate);
            // just a trick to include the last month as well
            if (!isOneYear) {
                localEndDate = localEndDate.plus(Period.months(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }
            while (localStartDate.isBefore(localEndDate)) {
                String monthName = localStartDate.toString("yyyy");
                localStartDate = localStartDate.plus(Period.years(1));
                yearList.add(monthName);
            }
            // the first object would be the months array
            int yearlist = monthYearFormat ? yearList.size() : yearList.size() - 1;
            JSONArray yearArray = new JSONArray();
            JSONObject yearObj;
            for (int i = 0; i < yearlist; i++) {
                yearObj = new JSONObject();
                yearObj.put("monthname", yearList.get(i));
                yearArray.put(yearObj);
            }
            yearObj = new JSONObject();
            yearObj.put("monthname", "Total");
            yearArray.put(yearObj);
            jobj1.put("months", yearArray);
            jobj1.put("left", jArrL);
            jobj1.put("refleft", AccReportsHandler.getAccountsConvertedJSONArray(paramJobj,jArrL, templeftObjArr,extrapref.isShowAllAccountsInPnl(),"totalamount"));
            jobj1.put("right", jArrR);
            jobj1.put("refright", AccReportsHandler.getAccountsConvertedJSONArray(paramJobj,jArrR, temprightObjArr1,extrapref.isShowAllAccountsInPnl(),"totalamount"));
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getYearlyTradingProfitLossExport : " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println(e);
        }
        return jobj1;
    }

    @Override
    @Deprecated
    public JSONObject getTradingAndProfitLoss(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        
        int type = StringUtil.isNullOrEmpty(paramJobj.optString("Nature", null)) ? 0 : Integer.parseInt(paramJobj.getString("Nature"));
        JSONObject jobj = new JSONObject();
        double invOpeBal = 0, invCloseBal = 0, assemblyValuation = 0;
        double preinvOpeBal = 0, preinvCloseBal = 0, preassemblyValuation = 0;
        try {
            Date toDate = null;
            Date startDate = null;
            Date endDate = null;
            if (paramJobj.optString("jasperreport", null) != null && paramJobj.optString("jasperreport").equals("JasperReport")) {
                startDate = (Date) paramJobj.get("jaspersdate");
                endDate = (Date) paramJobj.get("jasperenddate");
            } else {
                startDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("stdate"));
                endDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("enddate"));
            }
             boolean isMonthlyOrYearlyPNL=false;
             if (paramJobj.optString("isMonthlyOrYearlyPNL",null)!=null) {
                isMonthlyOrYearlyPNL= Boolean.parseBoolean(paramJobj.optString("isMonthlyOrYearlyPNL"));
            }
            if(isMonthlyOrYearlyPNL && paramJobj.optBoolean("isJasper", false)) {
                toDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("enddate"));
            }
            double rate = 0.0;
            boolean stockValuationFlag = true;
            boolean isForTradingAndProfitLoss = false;
            if ((paramJobj.optString("isForTradingAndProfitLoss", null) != null)) {
                isForTradingAndProfitLoss = Boolean.parseBoolean(paramJobj.optString("isForTradingAndProfitLoss"));
            }
            String companyid = paramJobj.getString(Constants.companyKey);
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                stockValuationFlag = extrapref.isStockValuationFlag();
            }
            if (extrapref != null && extrapref.isShowAllAccountsInPnl()) {//Check to show all accounts.
                paramJobj.put("monthlyreport", "MonthlyReport");
            }
            boolean isShowZeroAmountAsBlank = extrapref.isShowZeroAmountAsBlank();
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String filterCurrency = paramJobj.optString("filterCurrency", null) != null ? paramJobj.optString("filterCurrency") : "";
            Map<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate", null) != null ? paramJobj.getString("externalcurrencyrate") : "1.0");
            KwlReturnObject result = null;
            if(isMonthlyOrYearlyPNL && paramJobj.optBoolean("isJasper", false)) {
                result = accCurrencyDAOobj.getExcDetailID(requestParams, filterCurrency, toDate, null);
            } else {
                result = accCurrencyDAOobj.getExcDetailID(requestParams, filterCurrency, endDate, null);
            }
            List li = result.getEntityList();
            if (!li.isEmpty()) {
                Iterator itr = li.iterator();
                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                rate = erd != null ? erd.getExchangeRate() : 1.0;
            }
            externalCurrencyRate = rate;
            
            paramJobj.put("externalCurrencyRate", externalCurrencyRate);
            paramJobj.put("externalcurrencyrate", externalCurrencyRate);

            String costCenterId = paramJobj.optString("costcenter", null); //Filter for costcenter
            String reportView = paramJobj.optString("reportView", null); //"TradingAndProfitLoss","CostCenter"
            double dtotal = 0, ctotal = 0;
            double predtotal = 0, prectotal = 0;
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            JSONObject objlast = new JSONObject();
            Date startPreDate = null;
            Date endPreDate = null;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("stpredate", null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("endpredate", null))) {
                startPreDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("stpredate"));
                endPreDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("endpredate"));
            }
            String Searchjson = paramJobj.optString("searchJson", null) != null ? paramJobj.optString("searchJson") : "";
            if (paramJobj.optString("DimensionBasedComparisionReport") != null && paramJobj.optString("DimensionBasedComparisionReport").equals("DimensionBasedComparisionReport")) {
                Searchjson = paramJobj.optString("DimensionBasedSearchJson");
            }
            if (!"CostCenter".equalsIgnoreCase(reportView) && StringUtil.isNullOrEmpty(costCenterId) && extrapref != null && !(extrapref.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) { //Don't show Opening/Closing Stock for any Cost-Center
                if (stockValuationFlag) {
                    JSONArray jarr = new JSONArray(); // fetching stock valuation in one function call
                    HashMap<String, Object> requestParam = new HashMap<String, Object>();
                    requestParam.put(Constants.df, authHandler.getDateOnlyFormat());
                    requestParam.put(Constants.REQ_startdate, authHandler.getDateOnlyFormat().format(startDate));
                    requestParam.put(Constants.REQ_enddate, authHandler.getDateOnlyFormat().format(endDate));
                    requestParam.put(Constants.companyKey, companyid);
                    requestParam.put("searchJson", Searchjson);
                    requestParam.put(Constants.Filter_Criteria, paramJobj.optString(Constants.Filter_Criteria));
                    double[] valuation = AccProductService.getInventoryValuationDataForFinancialReports(new HashMap<String, Object>(requestParam));
                    if(isMonthlyOrYearlyPNL || isForTradingAndProfitLoss){
                        invOpeBal = valuation[2];// Opening Stock-> Closing
                        invCloseBal = valuation[5];// Closing Stock-> Ending
                    }else{
                        invOpeBal = valuation[1];
                        invCloseBal = valuation[4];
                    }
                     
                    assemblyValuation = valuation[7];
                    if (startPreDate != null && endPreDate != null) {//these date came when we click on compare button in P&L Report
                        requestParam.put(Constants.REQ_startdate, paramJobj.optString("stpredate"));
                        requestParam.put(Constants.REQ_enddate, paramJobj.optString("endpredate"));
                        valuation = AccProductService.getInventoryValuationDataForFinancialReports(new HashMap<String, Object>(requestParam));
                        preinvCloseBal = valuation[5];
                        preassemblyValuation = valuation[8];
                        preinvOpeBal = authHandler.round(preinvOpeBal, companyid);
                    }

                    objlast = new JSONObject();
                    //Opening Stock
                    objlast.put("accountname", messageSource.getMessage("acc.report.13", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    objlast.put("accountid", messageSource.getMessage("acc.report.13", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    if (!isForTradingAndProfitLoss) {//include stock related groups in CoGS grou
                        objlast.put("acctype", "expense");
                    } else {
                        objlast.put("acctype", "costofgoodssold");
                    }
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("amount", (isShowZeroAmountAsBlank && invOpeBal == 0.0 ? "" : invOpeBal));
                    objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && invOpeBal == 0.0 ? "" : externalCurrencyRate * invOpeBal));
                    objlast.put("preamount", preinvOpeBal);
                    objlast.put("fmt", "H");
                    jArrL.put(objlast);
                }
            }
            if (type == Group.NATURE_EXPENSES || type == 0) {
//                double tempdTotal[] = accReportsService.getTrading(request, Group.NATURE_EXPENSES, jArrL, false);
                double tempdTotal[] = accReportsService.getTrading(paramJobj, Group.NATURE_EXPENSES, jArrL, false,null);
                dtotal = tempdTotal[0];
                predtotal = tempdTotal[1];
            }
            if (type == Group.NATURE_INCOME || type == 0) {
//                double tempcTotal[] = accReportsService.getTrading(request, Group.NATURE_INCOME, jArrR, false);
                double tempcTotal[] = accReportsService.getTrading(paramJobj, Group.NATURE_INCOME, jArrR, false,null);
                ctotal = tempcTotal[0];
                prectotal = tempcTotal[1];
            }
            double costofgoodsSoldTotal = 0, preCostOfGoodsSold = 0;
            if (isForTradingAndProfitLoss) { // If "Profit and Loss Report" then calculate CoGS accounts seperately
                paramJobj.put("isCostOfGoodsSold", true);
                double goodsSoldAmount[] = accReportsService.getTrading(paramJobj, Group.NATURE_EXPENSES, jArrL, false,null);
                costofgoodsSoldTotal = goodsSoldAmount[0];
                preCostOfGoodsSold = goodsSoldAmount[1];

                double profitLossAmountCoGS[] = accReportsService.getProfitLoss(paramJobj, Group.NATURE_EXPENSES, jArrL, false,null);

                costofgoodsSoldTotal += profitLossAmountCoGS[0];
                preCostOfGoodsSold += profitLossAmountCoGS[1];
                costofgoodsSoldTotal = authHandler.round((costofgoodsSoldTotal + invOpeBal - invCloseBal), companyid);
                preCostOfGoodsSold = authHandler.round((preCostOfGoodsSold + preinvOpeBal - preinvCloseBal), companyid);
                paramJobj.remove("isCostOfGoodsSold");

                objlast = new JSONObject();
                objlast.put("accountid", "Total Cost of Goods Sold");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", "Total Cost of Goods Sold");
                objlast.put("amount", (isShowZeroAmountAsBlank && costofgoodsSoldTotal == 0.0 ? "" : costofgoodsSoldTotal));
                objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && costofgoodsSoldTotal == 0.0 ? "" : authHandler.round((costofgoodsSoldTotal * externalCurrencyRate), companyid)));
                objlast.put("preamount", preCostOfGoodsSold);
                objlast.put("fmt", "B");
                objlast.put("acctype", "totalcogs");
                jArrL.put(objlast);
            } else {
                dtotal += invOpeBal;
                ctotal -= invCloseBal;
                predtotal += preinvOpeBal;
                prectotal -= preinvCloseBal;
            }
            if (!"CostCenter".equalsIgnoreCase(reportView) && StringUtil.isNullOrEmpty(costCenterId) && extrapref != null && !(extrapref.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) { //Don't show Opening/Closing Stock for any Cost-Center
                JSONObject obj = new JSONObject();
                if (stockValuationFlag) {
                    obj.put("accountname", messageSource.getMessage("acc.report.17", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Closing Stock");
                    obj.put("accountid", messageSource.getMessage("acc.report.17", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    if (!isForTradingAndProfitLoss) {//include stock related groups in CoGS grou
                        obj.put("acctype", "income");
                    } else {
                        obj.put("acctype", "costofgoodssold");
                    }
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", false);
//                    if (extrapref.isShowZeroAmountAsBlank()) {
                        obj.put("amount", "");
                        obj.put("amountInSelectedCurrency", "");
//                    } else {
//                        obj.put("amount", 0.0);
//                        obj.put("amountInSelectedCurrency", 0.0);
//                    }
                    obj.put("isaccountgroup", true);
                    obj.put("preamount", "");
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.14", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  // "Finish Products (Total Value of \"Inventory Assembly\" products)");
                    obj.put("accountid", messageSource.getMessage("acc.report.14", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    if (!isForTradingAndProfitLoss) {//include stock related groups in CoGS grou
                        obj.put("acctype", "income");
                    } else {
                        obj.put("acctype", "costofgoodssold");
                    }
                    obj.put("level", 1);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", (isShowZeroAmountAsBlank && assemblyValuation == 0.0 ? "" : assemblyValuation));
                    obj.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && assemblyValuation == 0.0 ? "" : externalCurrencyRate * assemblyValuation));
                    obj.put("preamount", preassemblyValuation);
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.15", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Raw Materials (Total Value of \"Inventory Item\" products)");
                    obj.put("accountid", messageSource.getMessage("acc.report.15", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    if (!isForTradingAndProfitLoss) {//include stock related groups in CoGS grou
                        obj.put("acctype", "income");
                    } else {
                        obj.put("acctype", "costofgoodssold");
                    }
                    obj.put("level", 1);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", (isShowZeroAmountAsBlank && (invCloseBal - assemblyValuation) == 0.0 ? "" : invCloseBal - assemblyValuation));
                    obj.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && (invCloseBal - assemblyValuation) == 0.0 ? "" : externalCurrencyRate * (invCloseBal - assemblyValuation)));
                    obj.put("preamount", preinvCloseBal - preassemblyValuation);
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.16", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Total Closing Stock");
                    obj.put("accountid", messageSource.getMessage("acc.report.16", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    if (!isForTradingAndProfitLoss) {//include stock related groups in CoGS grou
                        obj.put("acctype", "income");
                    } else {
                        obj.put("acctype", "costofgoodssold");
                    }
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", (isShowZeroAmountAsBlank && invCloseBal == 0.0 ? "" : invCloseBal));
                    obj.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && invCloseBal == 0.0 ? "" : externalCurrencyRate * invCloseBal));
                    obj.put("preamount", preinvCloseBal);
                    objlast.put("fmt", "H");
                    jArrR.put(obj);
                }
            }
            double expensePnLTotal[] = {0, 0, 0, 0, 0, 0};
            double incomePnLTotal[] = {0, 0, 0, 0, 0, 0};
            double balance = 0, preBalance = 0, expenseTotal = dtotal, incometotal = ctotal, preExpenseTotal = predtotal, preIncomeTotal = prectotal, grossprofit = 0, pregrossprofit = 0;
            if (type == Group.NATURE_EXPENSES || type == 0) {
                expensePnLTotal = accReportsService.getProfitLoss(paramJobj, Group.NATURE_EXPENSES, jArrL, false,null);
                expenseTotal += expensePnLTotal[0];
                preExpenseTotal += expensePnLTotal[1];
            }
            if (type == Group.NATURE_INCOME || type == 0) {
                incomePnLTotal = accReportsService.getProfitLoss(paramJobj, Group.NATURE_INCOME, jArrR, false,null);
                incometotal += incomePnLTotal[0];
                preIncomeTotal += incomePnLTotal[1];
            }
            if (!isForTradingAndProfitLoss) {
                balance = dtotal + ctotal;
                preBalance = predtotal + prectotal;
            } else { // calculate gross profit as "total income" - "cost of goods sold"
                balance = grossprofit = ((-1 * incometotal) - costofgoodsSoldTotal) * (-1);
                preBalance = pregrossprofit = ((-1 * preIncomeTotal) - preCostOfGoodsSold) * (-1);
            }
            if (!"CostCenter".equalsIgnoreCase(reportView)) {//Don't show GrossLoss,GrossProfit for cost center report
                boolean lossflag = false;
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "Gross Loss");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.5", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Loss");
                    objlast.put("amount", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : balance));
                    objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : externalCurrencyRate * balance));
                    objlast.put("preamount", "");
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "grossloss");
                    ctotal -= balance;
                    lossflag = true;
                }

                if (preBalance > 0) {
                    if (!lossflag) {
                        objlast = new JSONObject();
                        objlast.put("accountid", "Gross Loss");
                        objlast.put("accountname", messageSource.getMessage("acc.report.5", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Loss");
                        objlast.put("accountid", "");
                        objlast.put("level", 0);
                        objlast.put("leaf", true);
                        if (extrapref.isShowZeroAmountAsBlank()) {
                            objlast.put("amount", "");
                            objlast.put("amountInSelectedCurrency", "");
                        } else {
                            objlast.put("amount", 0.0);
                            objlast.put("amountInSelectedCurrency", 0.0);
                        }
                        objlast.put("isdebit", false);
                        objlast.put("fmt", "B");
                        objlast.put("acctype", "grossloss");
                    }
                    objlast.put("preamount", preBalance);
                    prectotal -= preBalance;
                    jArrR.put(objlast);
                    jArrL.put(new JSONObject());
                } else if (balance > 0) {
                    jArrR.put(objlast);
                    jArrL.put(new JSONObject());
                }
                boolean profitflag = true;

                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "Gross Profit");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.6", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Profit");
                    objlast.put("amount", balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance) : -balance);//Remove '-' sign if 0
                    objlast.put("amountInSelectedCurrency", (balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance * externalCurrencyRate) : -balance * externalCurrencyRate));//Remove '-' sign if 0
                    objlast.put("preamount", "");
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "grossprofit");
                    dtotal -= balance;
                    profitflag = true;
                }
                if (preBalance < 0) {
                    if (!profitflag) {
                        objlast = new JSONObject();
                        objlast.put("accountname", messageSource.getMessage("acc.report.6", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Profit");
                        objlast.put("accountid", "Gross Profit");
                        objlast.put("level", 0);
                        objlast.put("leaf", true);
                        if (extrapref.isShowZeroAmountAsBlank()) {
                            objlast.put("amount", "");
                            objlast.put("amountInSelectedCurrency", "");
                        } else {
                            objlast.put("amount", 0.0);
                            objlast.put("amountInSelectedCurrency", 0.0);
                        }
                        objlast.put("isdebit", true);
                        objlast.put("fmt", "B");
                        objlast.put("acctype", "grossprofit");
                    }
                    objlast.put("preamount", preBalance == 0 ? preBalance : -preBalance);//Remove '-' sign if 0
                    predtotal -= preBalance;
                    jArrL.put(objlast);
                    jArrR.put(new JSONObject());
                } else if (balance < 0) {
                    jArrL.put(objlast);
                    jArrR.put(new JSONObject());
                }

                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.7", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Total Debit");
                objlast.put("accountid", "Total Debit");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", (isShowZeroAmountAsBlank && dtotal == 0.0 ? "" : dtotal));
                objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && dtotal == 0.0 ? "" : dtotal * externalCurrencyRate));
                objlast.put("preamount", predtotal);
                objlast.put("fmt", "T");
                jArrL.put(objlast);
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.8", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Total Credit");
                objlast.put("accountid", "Total Credit");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", ctotal == 0 ? (isShowZeroAmountAsBlank ? "" : ctotal) : -ctotal);//Remove '-' sign if 0
                objlast.put("amountInSelectedCurrency", ctotal == 0 ? (isShowZeroAmountAsBlank ? "" : ctotal * externalCurrencyRate) : -ctotal * externalCurrencyRate);//Remove '-' sign if 0
                objlast.put("preamount", prectotal == 0 ? prectotal : -prectotal);//Remove '-' sign if 0
                objlast.put("fmt", "T");
                jArrR.put(objlast);

                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Particulars");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", "<div align=right>" + messageSource.getMessage("acc.report.3", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");   //Amount (Debit)
                objlast.put("amountInSelectedCurrency", "<div align=right>" + messageSource.getMessage("acc.report.3", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");   //Amount (Debit)
                objlast.put("preamount", "<div align=right>" + messageSource.getMessage("acc.report.3", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");   //Amount (Debit)
                objlast.put("fmt", "H");
                jArrL.put(objlast);
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Particulars");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", "<div align=right>" + messageSource.getMessage("acc.report.4", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");       //Amount (Credit)
                objlast.put("amountInSelectedCurrency", "<div align=right>" + messageSource.getMessage("acc.report.4", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");       //Amount (Credit)
                objlast.put("preamount", "<div align=right>" + messageSource.getMessage("acc.report.4", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");       //Amount (Credit)
                objlast.put("fmt", "H");
                jArrR.put(objlast);
                dtotal = 0;
                ctotal = 0;

                boolean consolidationPandL = paramJobj.optBoolean("consolidationPandL", false);
                if (!consolidationPandL) {// below code for Gross profit/loss does not required for Consolidation P&L as it is aready putted in jArrR
                    lossflag = false;
                    if (balance > 0) {
                        objlast = new JSONObject();
                        objlast.put("accountid", "Gross Loss");
                        objlast.put("level", 0);
                        objlast.put("isdebit", false);
                        objlast.put("leaf", true);
                        objlast.put("accountname", messageSource.getMessage("acc.report.5", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Loss");
                        objlast.put("amount", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : balance));
                        objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : externalCurrencyRate * balance));
                        objlast.put("preamount", "");
                        objlast.put("fmt", "B");
                        dtotal = balance;
                        lossflag = true;
                    }

                    if (preBalance > 0) {
                        if (!lossflag) {
                            objlast = new JSONObject();
                            objlast.put("accountname", messageSource.getMessage("acc.report.5.1", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Loss");
                            objlast.put("accountid", "Gross Loss");
                            objlast.put("level", 0);
                            objlast.put("leaf", true);
                            if (extrapref.isShowZeroAmountAsBlank()) {
                                objlast.put("amount", "");
                                objlast.put("amountInSelectedCurrency", "");
                            } else {
                                objlast.put("amount", 0.0);
                                objlast.put("amountInSelectedCurrency", 0.0);
                            }
                            objlast.put("isdebit", false);
                            objlast.put("fmt", "B");
                        }
                        objlast.put("preamount", preBalance);
                        predtotal = preBalance;
                        jArrL.put(objlast);
                        if (!isMonthlyOrYearlyPNL) {//in Montly P&L Below code not required, if it is given it is creating issue like SDP-5177
                            jArrR.put(new JSONObject());//empty json putted for empty row in report.
                        }
                    } else if (balance > 0) {
                        jArrL.put(objlast);
                        if (!isMonthlyOrYearlyPNL) {//in Montly P&L Below code not required, if it is given it is creating issue like SDP-5177
                            jArrR.put(new JSONObject());//empty json putted for empty row in report.
                        }
                    }

                    profitflag = true;

                    if (balance < 0) {
                        objlast = new JSONObject();
                        objlast.put("accountid", "Gross Profit");
                        objlast.put("level", 0);
                        objlast.put("isdebit", false);
                        objlast.put("leaf", true);
                        objlast.put("accountname", messageSource.getMessage("acc.report.6", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Profit");
                        objlast.put("amount", balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance) : -balance);//Remove '-' sign if 0
                        objlast.put("amountInSelectedCurrency", balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance * externalCurrencyRate) : (-balance * externalCurrencyRate));//Remove '-' sign if 0
                        objlast.put("preamount", "");//Remove '-' sign if 0
                        objlast.put("fmt", "B");
                        ctotal = balance;
                        profitflag = true;
                    }
                    if (preBalance < 0) {
                        if (!profitflag) {
                            objlast = new JSONObject();
                            objlast.put("accountname", messageSource.getMessage("acc.report.6.1", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Profit");
                            objlast.put("accountid", "Gross Profit");
                            objlast.put("level", 0);
                            objlast.put("leaf", true);
                            if (extrapref.isShowZeroAmountAsBlank()) {
                                objlast.put("amount", "");
                                objlast.put("amountInSelectedCurrency", "");
                            } else {
                                objlast.put("amount", 0.0);
                                objlast.put("amountInSelectedCurrency", 0.0);
                            }
                            objlast.put("isdebit", true);
                            objlast.put("fmt", "B");
                        }
                        objlast.put("preamount", preBalance == 0 ? preBalance : -preBalance);//Remove '-' sign if 0
                        prectotal = preBalance;
                        jArrR.put(objlast);
                        if (!isMonthlyOrYearlyPNL) {//in Montly P&L Below code not required, if it is given it is creating issue like SDP-5177
                            jArrL.put(new JSONObject());//empty json putted for empty row in report.
                        }

                    } else if (balance < 0) {
                        jArrR.put(objlast);
                        if (!isMonthlyOrYearlyPNL) {
                            jArrL.put(new JSONObject());
                        }
                    }

                }
            }
            if (type == Group.NATURE_EXPENSES || type == 0) {
                double tempdTotal1[] = expensePnLTotal;
                dtotal += tempdTotal1[0];
                predtotal += tempdTotal1[1];
            }
            if (type == Group.NATURE_INCOME || type == 0) {
                double tempcTotal1[] = incomePnLTotal;
                ctotal += tempcTotal1[0];
                prectotal += tempcTotal1[1];
            }

            if (!"CostCenter".equalsIgnoreCase(reportView)) { //Don't show NetLoss,NetProfit for cost center report
                if (!isForTradingAndProfitLoss) {
                    balance = dtotal + ctotal;
                } else {// calculate "Net Profit" as ("Gross Profit" -"Total Expense")
                    balance = ((grossprofit * (-1)) - expenseTotal) * (-1);
                    preBalance = ((pregrossprofit * (-1)) - preExpenseTotal) * (-1);
                }
//                if (balance > 0) {
//                    objlast = new JSONObject();
//                    objlast.put("accountid", "Net Loss");
//                    objlast.put("level", 0);
//                    objlast.put("isdebit", false);
//                    objlast.put("leaf", true);
////                    objlast.put("accountname", messageSource.getMessage("acc.report.9", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Net Loss");
//                    objlast.put("accountname", messageSource.getMessage("acc.report.netprofit.netloss.currentyrearnings", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //SDP-13756 - "Current Year Earnings"
//                    objlast.put("amount", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : -balance));
//                    objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : externalCurrencyRate * -balance));
//                    objlast.put("preamount", preBalance);
//                    objlast.put("fmt", "B");
//                    objlast.put("acctype", "netloss");
//                    jArrR.put(objlast);
//                    ctotal -= balance;
//                    prectotal -= preBalance;
//                }
                boolean isNetDebit = false;
                if ((balance > 0) || (isForTradingAndProfitLoss ? (balance <= 0) : (balance < 0))) {// If "Profit and loss report" then add "0" amount as "Net Profit"
                    if (isForTradingAndProfitLoss ? (balance <= 0) : (balance < 0)) {
                        isNetDebit = true;
                    }
                    objlast = new JSONObject();
                    objlast.put("accountid", "Net Profit");
                    objlast.put("level", 0);
                    objlast.put("isdebit", isNetDebit);
                    objlast.put("leaf", true);
//                    objlast.put("accountname", messageSource.getMessage("acc.report.10", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Net Profit");
                    objlast.put("accountname", messageSource.getMessage("acc.report.32", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //SDP-13756 - "Current Year Earnings"
                    objlast.put("amount", balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance) : -balance);//Remove '-' sign if 0
                    objlast.put("amountInSelectedCurrency", balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance * externalCurrencyRate) : (-balance * externalCurrencyRate));//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "netprofit");
                    jArrL.put(objlast);
                    dtotal -= balance;
                    predtotal -= preBalance;
                }
            }

            if ("CostCenter".equalsIgnoreCase(reportView)) { //Add LIABILITY for cost center report (Tax Amount)
                //Logic to get new OTHER_CURRENT_LIABILITIES group from old OTHER_CURRENT_LIABILITIES
                //To do - Need to test wheteher is working or not
                Group liab_group = accAccountDAOobj.getNewGroupFromOldId(Group.OTHER_CURRENT_LIABILITIES, companyid);
                if (liab_group != null) {
                    double tempFormat[] = accReportsService.formatGroupDetails(paramJobj, companyid, liab_group, startDate, endDate, 0, true, jArrR, startPreDate, endPreDate,null); //Bug Fixed #16746
                    ctotal += tempFormat[0]; //Bug Fixed #16746
                    liab_group.getName();
                }
            }

            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("[" + dtotal + "," + (ctotal == 0 ? ctotal : -ctotal) + "]"));
            fobj.put("pretotal", new JSONArray("[" + predtotal + "," + (prectotal == 0 ? prectotal : -prectotal) + "]"));
            jobj.put(Constants.RES_data, fobj);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : " + ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : " + e.getMessage(), e);
        }
        return jobj;
    }
    public JSONObject getTradingAndProfitLossAllAccounts(JSONObject paramJobj, Map<String, Double> accAmtMap) throws ServiceException, SessionExpiredException, JSONException {
        
        int type = StringUtil.isNullOrEmpty(paramJobj.optString("Nature", null)) ? 0 : Integer.parseInt(paramJobj.getString("Nature"));
        JSONObject jobj = new JSONObject();
        double invOpeBal = 0, invCloseBal = 0, assemblyValuation = 0;
        double preinvOpeBal = 0, preinvCloseBal = 0, preassemblyValuation = 0;
        try {
            boolean isBalanceSheet = paramJobj.optBoolean("isBalanceSheet",false);
            Date startDate = null;
            Date endDate = null;
            if (paramJobj.optString("jasperreport", null) != null && paramJobj.optString("jasperreport").equals("JasperReport")) {
                startDate = (Date) paramJobj.get("jaspersdate");
                endDate = (Date) paramJobj.get("jasperenddate");
            } else {
                startDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("stdate"));
                endDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("enddate"));
            }
             boolean isMonthlyOrYearlyPNL=false;
             if (paramJobj.optString("isMonthlyOrYearlyPNL",null)!=null) {
                isMonthlyOrYearlyPNL= Boolean.parseBoolean(paramJobj.optString("isMonthlyOrYearlyPNL"));
            }
            double rate = 0.0;
            boolean stockValuationFlag = true;
            boolean isForTradingAndProfitLoss = false;
            if ((paramJobj.optString("isForTradingAndProfitLoss", null) != null)) {
                isForTradingAndProfitLoss = Boolean.parseBoolean(paramJobj.optString("isForTradingAndProfitLoss"));
            }
            String companyid = paramJobj.getString(Constants.companyKey);
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                stockValuationFlag = extrapref.isStockValuationFlag();
            }
            if (extrapref != null && extrapref.isShowAllAccountsInPnl()) {//Check to show all accounts.
                paramJobj.put("monthlyreport", "MonthlyReport");
            }
            boolean isShowZeroAmountAsBlank = extrapref.isShowZeroAmountAsBlank();
            paramJobj.put("isShowZeroAmountAsBlank", isShowZeroAmountAsBlank);
            paramJobj.put("isForTradingAndProfitLoss",isForTradingAndProfitLoss);
            
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String filterCurrency = paramJobj.optString("filterCurrency", null) != null ? paramJobj.optString("filterCurrency") : "";
            Map<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate", null) != null ? paramJobj.getString("externalcurrencyrate") : "1.0");
            KwlReturnObject result = accCurrencyDAOobj.getExcDetailID(requestParams, filterCurrency, endDate, null);
            List li = result.getEntityList();
            if (!li.isEmpty()) {
                Iterator itr = li.iterator();
                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                rate = erd != null ? erd.getExchangeRate() : 1.0;
            }
            externalCurrencyRate = rate;

            paramJobj.put("externalCurrencyRate", externalCurrencyRate);

            String costCenterId = paramJobj.optString("costcenter", null); //Filter for costcenter
            String reportView = paramJobj.optString("reportView", null); //"TradingAndProfitLoss","CostCenter"
            double dtotal = 0, ctotal = 0;
            double predtotal = 0, prectotal = 0;
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            JSONObject objlast = new JSONObject();
            Date startPreDate = null;
            Date endPreDate = null;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("stpredate", null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("endpredate", null))) {
                startPreDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("stpredate"));
                endPreDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("endpredate"));
            }
            String Searchjson = paramJobj.optString("searchJson", null) != null ? paramJobj.optString("searchJson") : "";
            if (paramJobj.optString("DimensionBasedComparisionReport") != null && paramJobj.optString("DimensionBasedComparisionReport").equals("DimensionBasedComparisionReport")) {
                Searchjson = paramJobj.optString("DimensionBasedSearchJson");
            }
            if (!"CostCenter".equalsIgnoreCase(reportView) && StringUtil.isNullOrEmpty(costCenterId) && extrapref != null && !(extrapref.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) { //Don't show Opening/Closing Stock for any Cost-Center
                if (stockValuationFlag) {
                    JSONArray jarr = new JSONArray(); // fetching stock valuation in one function call
                    HashMap<String, Object> requestParam = new HashMap<String, Object>();
                    requestParam.put(Constants.df, authHandler.getDateOnlyFormat());
                    requestParam.put(Constants.REQ_startdate, authHandler.getDateOnlyFormat().format(startDate));
                    requestParam.put(Constants.REQ_enddate, authHandler.getDateOnlyFormat().format(endDate));
                    requestParam.put(Constants.companyKey, companyid);
                    requestParam.put("searchJson", Searchjson);
                    double[] valuation = AccProductService.getInventoryValuationDataForFinancialReports(new HashMap<String, Object>(requestParam));
                    if(isMonthlyOrYearlyPNL || isForTradingAndProfitLoss){
                        invOpeBal = valuation[2];// Opening Stock-> Closing
                        invCloseBal = valuation[5];// Closing Stock-> Ending
                    }else{
                        invOpeBal = valuation[1];
                        invCloseBal = valuation[4];
                    }
                     
                    assemblyValuation = valuation[7];
                    if (startPreDate != null && endPreDate != null) {//these date came when we click on compare button in P&L Report
                        requestParam.put(Constants.REQ_startdate, paramJobj.optString("stpredate"));
                        requestParam.put(Constants.REQ_enddate, paramJobj.optString("endpredate"));
                        valuation = AccProductService.getInventoryValuationDataForFinancialReports(new HashMap<String, Object>(requestParam));
                        preinvCloseBal = valuation[5];
                        preassemblyValuation = valuation[8];
                        preinvOpeBal = authHandler.round(preinvOpeBal, companyid);
                    }

                    objlast = new JSONObject();
                    objlast.put("accountname", messageSource.getMessage("acc.report.13", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    objlast.put("accountid", messageSource.getMessage("acc.report.13", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    if (!isForTradingAndProfitLoss) {//include stock related groups in CoGS grou
                        objlast.put("acctype", "expense");
                    } else {
                        objlast.put("acctype", "costofgoodssold");
                    }
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("amount", (isShowZeroAmountAsBlank && invOpeBal == 0.0 ? "" : invOpeBal));
                    objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && invOpeBal == 0.0 ? "" : externalCurrencyRate * invOpeBal));
                    objlast.put("preamount", preinvOpeBal);
                    objlast.put("fmt", "H");
                    jArrL.put(objlast);
                }
            }
            Map<String, Object> extraObjects = new HashMap<String, Object>();
            extraObjects.put("extraCompanyPreferences", extrapref);
            if (type == Group.NATURE_EXPENSES || type == 0) {
//                double tempdTotal[] = accReportsService.getTrading(request, Group.NATURE_EXPENSES, jArrL, false);
//                double tempdTotal[] = accReportsService.getTrading(paramJobj, Group.NATURE_EXPENSES, jArrL, false);
                dtotal = accReportsService.getTradingAllAccount(paramJobj, Group.NATURE_EXPENSES, jArrL, accAmtMap, extraObjects)[0];
                
            }
            if (type == Group.NATURE_INCOME || type == 0) {
//                double tempcTotal[] = accReportsService.getTrading(request, Group.NATURE_INCOME, jArrR, false);
                ctotal = accReportsService.getTradingAllAccount(paramJobj, Group.NATURE_INCOME, jArrL,  accAmtMap, extraObjects)[0];
                
            }
            double costofgoodsSoldTotal = 0, preCostOfGoodsSold = 0;
            if (isForTradingAndProfitLoss) { // If "Profit and Loss Report" then calculate CoGS accounts seperately
                paramJobj.put("isCostOfGoodsSold", true);
                    costofgoodsSoldTotal= accReportsService.getTradingAllAccount(paramJobj, Group.NATURE_EXPENSES, jArrL, accAmtMap, extraObjects)[0];                
                    costofgoodsSoldTotal += accReportsService.getProfitLossAllAccounts(paramJobj, Group.NATURE_EXPENSES, jArrL, false, accAmtMap, extraObjects)[0];

                costofgoodsSoldTotal = authHandler.round((costofgoodsSoldTotal + invOpeBal - invCloseBal), companyid);
                paramJobj.remove("isCostOfGoodsSold");

                objlast = new JSONObject();
                objlast.put("accountid", "Total Cost of Goods Sold");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("accountname", "Total Cost of Goods Sold");
                objlast.put("amount", (isShowZeroAmountAsBlank && costofgoodsSoldTotal == 0.0 ? "" : costofgoodsSoldTotal));
                objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && costofgoodsSoldTotal == 0.0 ? "" : authHandler.round((costofgoodsSoldTotal * externalCurrencyRate), companyid)));
                objlast.put("preamount", preCostOfGoodsSold);
                objlast.put("fmt", "B");
                objlast.put("acctype", "totalcogs");
                jArrL.put(objlast);
            } else {
                dtotal += invOpeBal;
                ctotal -= invCloseBal;
                predtotal += preinvOpeBal;
                prectotal -= preinvCloseBal;
            }
            if (!"CostCenter".equalsIgnoreCase(reportView) && StringUtil.isNullOrEmpty(costCenterId) && extrapref != null && !(extrapref.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) { //Don't show Opening/Closing Stock for any Cost-Center
                JSONObject obj = new JSONObject();
                if (stockValuationFlag) {
                    obj.put("accountname", messageSource.getMessage("acc.report.17", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Closing Stock");
                    obj.put("accountid", messageSource.getMessage("acc.report.17", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    if (!isForTradingAndProfitLoss) {//include stock related groups in CoGS grou
                        obj.put("acctype", "income");
                    } else {
                        obj.put("acctype", "costofgoodssold");
                    }
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", false);
                    if (extrapref.isShowZeroAmountAsBlank()) {
                        obj.put("amount", "");
                        obj.put("amountInSelectedCurrency", "");
                    } else {
                        obj.put("amount", 0.0);
                        obj.put("amountInSelectedCurrency", 0.0);
                    }
                    obj.put("preamount", "");
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.14", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  // "Finish Products (Total Value of \"Inventory Assembly\" products)");
                    obj.put("accountid", messageSource.getMessage("acc.report.14", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    if (!isForTradingAndProfitLoss) {//include stock related groups in CoGS grou
                        obj.put("acctype", "income");
                    } else {
                        obj.put("acctype", "costofgoodssold");
                    }
                    obj.put("level", 1);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", (isShowZeroAmountAsBlank && assemblyValuation == 0.0 ? "" : assemblyValuation));
                    obj.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && assemblyValuation == 0.0 ? "" : externalCurrencyRate * assemblyValuation));
                    obj.put("preamount", preassemblyValuation);
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.15", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Raw Materials (Total Value of \"Inventory Item\" products)");
                    obj.put("accountid", messageSource.getMessage("acc.report.15", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    if (!isForTradingAndProfitLoss) {//include stock related groups in CoGS grou
                        obj.put("acctype", "income");
                    } else {
                        obj.put("acctype", "costofgoodssold");
                    }
                    obj.put("level", 1);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", (isShowZeroAmountAsBlank && (invCloseBal - assemblyValuation) == 0.0 ? "" : invCloseBal - assemblyValuation));
                    obj.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && (invCloseBal - assemblyValuation) == 0.0 ? "" : externalCurrencyRate * (invCloseBal - assemblyValuation)));
                    obj.put("preamount", preinvCloseBal - preassemblyValuation);
                    jArrR.put(obj);

                    obj = new JSONObject();
                    obj.put("accountname", messageSource.getMessage("acc.report.16", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Total Closing Stock");
                    obj.put("accountid", messageSource.getMessage("acc.report.16", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    if (!isForTradingAndProfitLoss) {//include stock related groups in CoGS grou
                        obj.put("acctype", "income");
                    } else {
                        obj.put("acctype", "costofgoodssold");
                    }
                    obj.put("level", 0);
                    obj.put("isdebit", false);
                    obj.put("leaf", true);
                    obj.put("amount", (isShowZeroAmountAsBlank && invCloseBal == 0.0 ? "" : invCloseBal));
                    obj.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && invCloseBal == 0.0 ? "" : externalCurrencyRate * invCloseBal));
                    obj.put("preamount", preinvCloseBal);
                    objlast.put("fmt", "H");
                    jArrR.put(obj);
                }
            }
            double expensePnLTotal[] = {0, 0, 0, 0, 0, 0};
            double incomePnLTotal[] = {0, 0, 0, 0, 0, 0};
            double balance = 0, preBalance = 0, expenseTotal = dtotal, incometotal = ctotal, preExpenseTotal = predtotal, preIncomeTotal = prectotal, grossprofit = 0, pregrossprofit = 0;
            if(isBalanceSheet){
                if ( type == Group.NATURE_ASSET || type == 0) {
                expenseTotal += accReportsService.getProfitLossAllAccounts(paramJobj, Group.NATURE_ASSET, jArrL, false, accAmtMap, extraObjects)[0];
                }
                if (type == Group.NATURE_LIABILITY || type == 0) {
                    incometotal += accReportsService.getProfitLossAllAccounts(paramJobj, Group.NATURE_LIABILITY, jArrR, false, accAmtMap, extraObjects)[0];
                }
            }
                
            else{    if ( type == Group.NATURE_EXPENSES || type == 0) {
                expenseTotal += accReportsService.getProfitLossAllAccounts(paramJobj, Group.NATURE_EXPENSES, jArrL, false, accAmtMap, extraObjects)[0];
                }
                if (type == Group.NATURE_INCOME || type == 0) {
                    incometotal += accReportsService.getProfitLossAllAccounts(paramJobj, Group.NATURE_INCOME, jArrR, false, accAmtMap, extraObjects)[0];
                }
            }
            if (!isForTradingAndProfitLoss) {
                balance = dtotal + ctotal;
                preBalance = predtotal + prectotal;
            } else { // calculate gross profit as "total income" - "cost of goods sold"
                balance = grossprofit = ((-1 * incometotal) - costofgoodsSoldTotal) * (-1);
                preBalance = pregrossprofit = ((-1 * preIncomeTotal) - preCostOfGoodsSold) * (-1);
            }
            if (!"CostCenter".equalsIgnoreCase(reportView)) {//Don't show GrossLoss,GrossProfit for cost center report
                boolean lossflag = false;
                if (balance > 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "Gross Loss");
                    objlast.put("level", 0);
                    objlast.put("isdebit", false);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.5", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Loss");
                    objlast.put("amount", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : balance));
                    objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : externalCurrencyRate * balance));
                    objlast.put("preamount", "");
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "grossloss");
                    ctotal -= balance;
                    lossflag = true;
                }

                if (preBalance > 0) {
                    if (!lossflag) {
                        objlast = new JSONObject();
                        objlast.put("accountid", "Gross Loss");
                        objlast.put("accountname", messageSource.getMessage("acc.report.5", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Loss");
                        objlast.put("accountid", "");
                        objlast.put("level", 0);
                        objlast.put("leaf", true);
                        if (extrapref.isShowZeroAmountAsBlank()) {
                            objlast.put("amount", "");
                            objlast.put("amountInSelectedCurrency", "");
                        } else {
                            objlast.put("amount", 0.0);
                            objlast.put("amountInSelectedCurrency", 0.0);
                        }
                        objlast.put("isdebit", false);
                        objlast.put("fmt", "B");
                        objlast.put("acctype", "grossloss");
                    }
                    objlast.put("preamount", preBalance);
                    prectotal -= preBalance;
                    jArrR.put(objlast);
                    jArrL.put(new JSONObject());
                } else if (balance > 0) {
                    jArrR.put(objlast);
                    jArrL.put(new JSONObject());
                }
                boolean profitflag = true;

                if (balance < 0) {
                    objlast = new JSONObject();
                    objlast.put("accountid", "Gross Profit");
                    objlast.put("level", 0);
                    objlast.put("isdebit", true);
                    objlast.put("leaf", true);
                    objlast.put("accountname", messageSource.getMessage("acc.report.6", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Profit");
                    objlast.put("amount", balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance) : -balance);//Remove '-' sign if 0
                    objlast.put("amountInSelectedCurrency", (balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance * externalCurrencyRate) : -balance * externalCurrencyRate));//Remove '-' sign if 0
                    objlast.put("preamount", "");
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "grossprofit");
                    dtotal -= balance;
                    profitflag = true;
                }
                if (preBalance < 0) {
                    if (!profitflag) {
                        objlast = new JSONObject();
                        objlast.put("accountname", messageSource.getMessage("acc.report.6", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Profit");
                        objlast.put("accountid", "Gross Profit");
                        objlast.put("level", 0);
                        objlast.put("leaf", true);
                        if (extrapref.isShowZeroAmountAsBlank()) {
                            objlast.put("amount", "");
                            objlast.put("amountInSelectedCurrency", "");
                        } else {
                            objlast.put("amount", 0.0);
                            objlast.put("amountInSelectedCurrency", 0.0);
                        }
                        objlast.put("isdebit", true);
                        objlast.put("fmt", "B");
                        objlast.put("acctype", "grossprofit");
                    }
                    objlast.put("preamount", preBalance == 0 ? preBalance : -preBalance);//Remove '-' sign if 0
                    predtotal -= preBalance;
                    jArrL.put(objlast);
                    jArrR.put(new JSONObject());
                } else if (balance < 0) {
                    jArrL.put(objlast);
                    jArrR.put(new JSONObject());
                }

                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.7", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Total Debit");
                objlast.put("accountid", "Total Debit");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", (isShowZeroAmountAsBlank && dtotal == 0.0 ? "" : dtotal));
                objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && dtotal == 0.0 ? "" : dtotal * externalCurrencyRate));
                objlast.put("preamount", predtotal);
                objlast.put("fmt", "T");
                jArrL.put(objlast);
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.8", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Total Credit");
                objlast.put("accountid", "Total Credit");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", ctotal == 0 ? (isShowZeroAmountAsBlank ? "" : ctotal) : -ctotal);//Remove '-' sign if 0
                objlast.put("amountInSelectedCurrency", ctotal == 0 ? (isShowZeroAmountAsBlank ? "" : ctotal * externalCurrencyRate) : -ctotal * externalCurrencyRate);//Remove '-' sign if 0
                objlast.put("preamount", prectotal == 0 ? prectotal : -prectotal);//Remove '-' sign if 0
                objlast.put("fmt", "T");
                jArrR.put(objlast);

                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Particulars");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", true);
                objlast.put("leaf", true);
                objlast.put("amount", "<div align=right>" + messageSource.getMessage("acc.report.3", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");   //Amount (Debit)
                objlast.put("amountInSelectedCurrency", "<div align=right>" + messageSource.getMessage("acc.report.3", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");   //Amount (Debit)
                objlast.put("preamount", "<div align=right>" + messageSource.getMessage("acc.report.3", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");   //Amount (Debit)
                objlast.put("fmt", "H");
                jArrL.put(objlast);
                objlast = new JSONObject();
                objlast.put("accountname", messageSource.getMessage("acc.report.2", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Particulars");
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", "<div align=right>" + messageSource.getMessage("acc.report.4", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");       //Amount (Credit)
                objlast.put("amountInSelectedCurrency", "<div align=right>" + messageSource.getMessage("acc.report.4", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");       //Amount (Credit)
                objlast.put("preamount", "<div align=right>" + messageSource.getMessage("acc.report.4", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "</div>");       //Amount (Credit)
                objlast.put("fmt", "H");
                jArrR.put(objlast);
                dtotal = 0;
                ctotal = 0;

                boolean consolidationPandL = paramJobj.optBoolean("consolidationPandL", false);
                if (!consolidationPandL) {// below code for Gross profit/loss does not required for Consolidation P&L as it is aready putted in jArrR
                    lossflag = false;
                    if (balance > 0) {
                        objlast = new JSONObject();
                        objlast.put("accountid", "Gross Loss");
                        objlast.put("level", 0);
                        objlast.put("isdebit", false);
                        objlast.put("leaf", true);
                        objlast.put("accountname", messageSource.getMessage("acc.report.5", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Loss");
                        objlast.put("amount", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : balance));
                        objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : externalCurrencyRate * balance));
                        objlast.put("preamount", "");
                        objlast.put("fmt", "B");
                        dtotal = balance;
                        lossflag = true;
                    }

                    if (preBalance > 0) {
                        if (!lossflag) {
                            objlast = new JSONObject();
                            objlast.put("accountname", messageSource.getMessage("acc.report.5.1", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Loss");
                            objlast.put("accountid", "Gross Loss");
                            objlast.put("level", 0);
                            objlast.put("leaf", true);
                            if (extrapref.isShowZeroAmountAsBlank()) {
                                objlast.put("amount", "");
                                objlast.put("amountInSelectedCurrency", "");
                            } else {
                                objlast.put("amount", 0.0);
                                objlast.put("amountInSelectedCurrency", 0.0);
                            }
                            objlast.put("isdebit", false);
                            objlast.put("fmt", "B");
                        }
                        objlast.put("preamount", preBalance);
                        predtotal = preBalance;
                        jArrL.put(objlast);
                        if (!isMonthlyOrYearlyPNL) {//in Montly P&L Below code not required, if it is given it is creating issue like SDP-5177
                            jArrR.put(new JSONObject());//empty json putted for empty row in report.
                        }
                    } else if (balance > 0) {
                        jArrL.put(objlast);
                        if (!isMonthlyOrYearlyPNL) {//in Montly P&L Below code not required, if it is given it is creating issue like SDP-5177
                            jArrR.put(new JSONObject());//empty json putted for empty row in report.
                        }
                    }

                    profitflag = true;

                    if (balance < 0) {
                        objlast = new JSONObject();
                        objlast.put("accountid", "Gross Profit");
                        objlast.put("level", 0);
                        objlast.put("isdebit", false);
                        objlast.put("leaf", true);
                        objlast.put("accountname", messageSource.getMessage("acc.report.6", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Profit");
                        objlast.put("amount", balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance) : -balance);//Remove '-' sign if 0
                        objlast.put("amountInSelectedCurrency", balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance * externalCurrencyRate) : (-balance * externalCurrencyRate));//Remove '-' sign if 0
                        objlast.put("preamount", "");//Remove '-' sign if 0
                        objlast.put("fmt", "B");
                        ctotal = balance;
                        profitflag = true;
                    }
                    if (preBalance < 0) {
                        if (!profitflag) {
                            objlast = new JSONObject();
                            objlast.put("accountname", messageSource.getMessage("acc.report.6.1", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Gross Profit");
                            objlast.put("accountid", "Gross Profit");
                            objlast.put("level", 0);
                            objlast.put("leaf", true);
                            if (extrapref.isShowZeroAmountAsBlank()) {
                                objlast.put("amount", "");
                                objlast.put("amountInSelectedCurrency", "");
                            } else {
                                objlast.put("amount", 0.0);
                                objlast.put("amountInSelectedCurrency", 0.0);
                            }
                            objlast.put("isdebit", true);
                            objlast.put("fmt", "B");
                        }
                        objlast.put("preamount", preBalance == 0 ? preBalance : -preBalance);//Remove '-' sign if 0
                        prectotal = preBalance;
                        jArrR.put(objlast);
                        if (!isMonthlyOrYearlyPNL) {//in Montly P&L Below code not required, if it is given it is creating issue like SDP-5177
                            jArrL.put(new JSONObject());//empty json putted for empty row in report.
                        }

                    } else if (balance < 0) {
                        jArrR.put(objlast);
                        if (!isMonthlyOrYearlyPNL) {
                            jArrL.put(new JSONObject());
                        }
                    }

                }
            }
            if (isBalanceSheet) {
                if (type == Group.NATURE_ASSET || type == 0) {
                    double tempdTotal1[] = expensePnLTotal;
                    dtotal += tempdTotal1[0];
                    predtotal += tempdTotal1[1];
                }
                if (type == Group.NATURE_LIABILITY || type == 0) {
                    double tempcTotal1[] = incomePnLTotal;
                    ctotal += tempcTotal1[0];
                    prectotal += tempcTotal1[1];
                }
            } else {
                if (type == Group.NATURE_EXPENSES || type == 0) {
                    double tempdTotal1[] = expensePnLTotal;
                    dtotal += tempdTotal1[0];
                    predtotal += tempdTotal1[1];
                }
                if (type == Group.NATURE_INCOME || type == 0) {
                    double tempcTotal1[] = incomePnLTotal;
                    ctotal += tempcTotal1[0];
                    prectotal += tempcTotal1[1];
                }
            }
            boolean isNetDebit = false;
            if (!"CostCenter".equalsIgnoreCase(reportView)) { //Don't show NetLoss,NetProfit for cost center report
                if (!isForTradingAndProfitLoss) {
                    balance = dtotal + ctotal;
                } else {// calculate "Net Profit" as ("Gross Profit" -"Total Expense")
                    balance = ((grossprofit * (-1)) - expenseTotal) * (-1);
                    preBalance = ((pregrossprofit * (-1)) - preExpenseTotal) * (-1);
                }
                
                if ((isForTradingAndProfitLoss ? (balance <= 0) : (balance < 0))) {
                    isNetDebit = true;
                }
//                if (balance > 0) {
//                    objlast = new JSONObject();
//                    objlast.put("accountid", "Net Loss");
//                    objlast.put("level", 0);
//                    objlast.put("isdebit", false);
//                    objlast.put("leaf", true);
////                    objlast.put("accountname", messageSource.getMessage("acc.report.9", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Net Loss");
//                    objlast.put("accountname", messageSource.getMessage("acc.report.netprofit.netloss.currentyrearnings", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //SDP-14331 - "Current Year Earnings"
//                    objlast.put("amount", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : -balance));
//                    objlast.put("amountInSelectedCurrency", (isShowZeroAmountAsBlank && balance == 0.0 ? "" : externalCurrencyRate * -balance));
//                    objlast.put("preamount", preBalance);
//                    objlast.put("fmt", "B");
//                    objlast.put("acctype", "netloss");
//                    jArrR.put(objlast);
//                    ctotal -= balance;
//                    prectotal -= preBalance;
//                }
                if ((balance > 0) || (isForTradingAndProfitLoss ? (balance <= 0) : (balance < 0))) {// If "Profit and loss report" then add "0" amount as "Net Profit"
                    objlast = new JSONObject();
                    objlast.put("accountid", "Net Profit");
                    objlast.put("level", 0);
                    objlast.put("isdebit", isNetDebit);
                    objlast.put("leaf", true);
//                    objlast.put("accountname", messageSource.getMessage("acc.report.10", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //"Net Profit");
                    objlast.put("accountname", messageSource.getMessage("acc.report.32", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));  //SDP-14331 - "Current Year Earnings"
                    objlast.put("amount", balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance) : -balance);//Remove '-' sign if 0
                    objlast.put("amountInSelectedCurrency", balance == 0 ? (isShowZeroAmountAsBlank ? "" : balance * externalCurrencyRate) : (-balance * externalCurrencyRate));//Remove '-' sign if 0
                    objlast.put("fmt", "B");
                    objlast.put("acctype", "netprofit");
                    jArrL.put(objlast);
                    dtotal -= balance;
                    predtotal -= preBalance;
                }
            }

            if ("CostCenter".equalsIgnoreCase(reportView)) { //Add LIABILITY for cost center report (Tax Amount)
                //Logic to get new OTHER_CURRENT_LIABILITIES group from old OTHER_CURRENT_LIABILITIES
                //To do - Need to test wheteher is working or not
                Group liab_group = accAccountDAOobj.getNewGroupFromOldId(Group.OTHER_CURRENT_LIABILITIES, companyid);
                if (liab_group != null) {
                    double tempFormat[] = accReportsService.formatGroupDetails(paramJobj, companyid, liab_group, startDate, endDate, 0, true, jArrR, startPreDate, endPreDate,null); //Bug Fixed #16746
                    ctotal += tempFormat[0]; //Bug Fixed #16746
                    liab_group.getName();
                }
            }

            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);
            fobj.put("total", new JSONArray("[" + dtotal + "," + (ctotal == 0 ? ctotal : -ctotal) + "]"));
            fobj.put("pretotal", new JSONArray("[" + predtotal + "," + (prectotal == 0 ? prectotal : -prectotal) + "]"));
            jobj.put(Constants.RES_data, fobj);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : " + ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : " + e.getMessage(), e);
        }
        return jobj;
    }
    
 @Override
    public JSONObject getBSorPL_CustomLayout(JSONObject paramJobj, ExtraCompanyPreferences extrapref, String companyid) throws ServiceException, SessionExpiredException,JSONException {
        JSONObject jobj = new JSONObject();
        double predtotal = 0, prectotal = 0;
        try {
            String reportView = paramJobj.optString("reportView",null); //"TradingAndProfitLoss","CostCenter"
            double dtotal = 0, ctotal = 0;
            JSONArray jArrL = new JSONArray();
            JSONArray jArrR = new JSONArray();
            
            if (reportView.equals("TradingAndProfitLoss")) {
                if (extrapref != null && extrapref.isShowAllAccountsInPnl()) {//Check to show all accounts.
                    paramJobj.put("monthlyreport", "MonthlyReport");
                }
            }else if (reportView.equals("BalanceSheet")) {
                if (extrapref != null && extrapref.isShowallaccountsinbs()) {//Check to show all accounts.
                    paramJobj.put("monthlyreport", "MonthlyReport");
                }
            }
            if (extrapref != null ) {
                paramJobj.put("stockValuationFlag", extrapref.isStockValuationFlag());
            }
            double tradingAmount[] = getBSorPL_CustomLayout(paramJobj, jArrL, companyid,extrapref);
            JSONObject fobj = new JSONObject();

            double totalDebitOpeningAmnt = tradingAmount[2];
            double totalDebitPeriodDAmnt = tradingAmount[4];
            double totalDebitEndingAmnt = tradingAmount[6];
            double totalCreditOpeningAmnt = tradingAmount[3];
            double totalCreditPeriodDAmnt = tradingAmount[5];
            double totalCreditEndingAmnt = tradingAmount[7];

            if (reportView.equals("TrialBalance")) {
                for (int cnt = 0; cnt < jArrL.length(); cnt++) {
                    JSONObject tempObj = jArrL.getJSONObject(cnt);
                    double openingAmt = tempObj.optDouble("openingamount", 0.0);
                    if (openingAmt > 0) {
                        tempObj.put("openingamount", "");
                        tempObj.put("openingamountd", openingAmt);
                    }
                    double periodamount = tempObj.optDouble("periodamount", 0.0);
                    if (periodamount > 0) {
                        tempObj.put("periodamount", "");
                        tempObj.put("periodamountd", periodamount);
                    }
                    double amount = tempObj.optDouble("amount", 0.0);
                    if (amount > 0) {
                        tempObj.put("amount", "");
                        tempObj.put("amountd", amount);
                    }
                }
            }
            fobj.put("left", jArrL);
            fobj.put("right", jArrR);

            if (reportView.equals("TrialBalance")) {
                fobj.put("total", new JSONArray("[" + totalDebitOpeningAmnt + "," + totalCreditOpeningAmnt + "," + totalDebitPeriodDAmnt + "," + totalCreditPeriodDAmnt
                        + "," + totalDebitEndingAmnt + "," + totalCreditEndingAmnt + "]"));
            } else {
                fobj.put("total", new JSONArray("[" + dtotal + "," + (ctotal == 0 ? ctotal : -ctotal) + "]"));
            }
            fobj.put("pretotal", new JSONArray("[" + predtotal + "," + (prectotal == 0 ? prectotal : -prectotal) + "]"));
            jobj.put(Constants.RES_data, fobj);
        } /*catch (ParseException ex) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : " + ex.getMessage(), ex);
        } */catch (JSONException e) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : " + e.getMessage(), e);
        }
        return jobj;
    }

 @Override
    public double[] getBSorPL_CustomLayout(JSONObject paramJobj, JSONArray jArr, String companyid, ExtraCompanyPreferences extrapref) throws ServiceException, SessionExpiredException, JSONException {
        double[] total = {0, 0, 0, 0, 0, 0, 0, 0};
        try {
            boolean isBalanceSheet = paramJobj.optString("isBalanceSheet", null) != null ? Boolean.parseBoolean(paramJobj.optString("isBalanceSheet")) : false;

            Map<String, double[]> groupTotalMap = new HashMap<String, double[]>();
            String templateid = paramJobj.optString("templateid", null);
            HashMap<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(Constants.companyKey, companyid);
            filterParams.put("templateid", templateid);
            filterParams.put("levelZeroFlag", true);
            KwlReturnObject plresult = accAccountDAOobj.getCustomLayoutGroups(filterParams);
            List<LayoutGroup> list = plresult.getEntityList();
            if (extrapref != null) {
                paramJobj.put("stockValuationFlag", extrapref.isStockValuationFlag());
            }
            Date startDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("stdate"));
            Date endDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("enddate"));
            Date startPreDate = null;
            Date endPreDate = null;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("stpredate", null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("endpredate", null))) {
                startPreDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("stpredate"));
                endPreDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("endpredate"));
            }
            String searchJson = null;
            if (paramJobj.optString("DimensionBasedComparisionReport", null) != null && paramJobj.optString("DimensionBasedComparisionReport").equals("DimensionBasedComparisionReport")) {
                searchJson = paramJobj.optString("DimensionBasedSearchJson", null);
            } else {
                searchJson = paramJobj.optString(Constants.Acc_Search_Json, null);
            }
            Map<String, Map> stockDateMap = null;
            if (paramJobj.has(Constants.preferences) && paramJobj.optString("DimensionBasedComparisionReport", null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("stdate")) && !StringUtil.isNullOrEmpty(paramJobj.optString("enddate")) && !StringUtil.isNullOrEmpty(searchJson)) {
                stockDateMap = new HashMap<>();
                DateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat df = authHandler.getDateOnlyFormat();
                HashMap temp = new HashMap();
                temp.put(Constants.df, df);
                temp.put(Constants.companyKey, companyid);
                temp.put(Constants.Searchjson, searchJson);
                temp.put(Constants.Acc_Search_Json, searchJson);
                temp.put("startdate", df.parse(paramJobj.optString("stdate")));
                temp.put("enddate", df.parse(paramJobj.optString("enddate")));
                if (paramJobj.has(Constants.preferences)) {
                    temp.put(Constants.preferences, (CompanyAccountPreferences) paramJobj.get(Constants.preferences));
                }
                if (paramJobj.has(Constants.basecurrencyid)) {
                    temp.put(Constants.basecurrencyid, paramJobj.optString(Constants.basecurrencyid));
                }
                JSONObject jobj = (!StringUtil.isNullOrEmpty(searchJson)) ? new JSONObject(searchJson) : new JSONObject();
                JSONArray rootArr = new JSONArray();
                if (jobj.has("root")) {
                    rootArr = jobj.optJSONArray("root");
                }
                JSONObject searchjobj = null;
                for (int i = 0; i < rootArr.length(); i++) {
                    searchjobj = rootArr.getJSONObject(i);
                    break;
                }
                stockDateMap.put(sqlDF.format(df.parse(paramJobj.optString("stdate"))) + "-" + sqlDF.format(df.parse(paramJobj.optString("enddate"))) + "-" + searchjobj.optString("searchText"), temp);
            }
            
            Map<String, Object> advSearchAttributes = null;
            if (!StringUtil.isNullOrEmpty(searchJson)) {
                CompanyAccountPreferences preferences = null;
                String filterConjuctionCriteria = paramJobj.optString(InvoiceConstants.Filter_Criteria, com.krawler.common.util.Constants.and);
                if (!StringUtil.isNullOrEmpty(companyid)) {
                    KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
                }
                advSearchAttributes = accJournalEntryobj.getAdvanceSearchAttributes(searchJson, preferences, companyid, null, filterConjuctionCriteria);
            }
            
            for (LayoutGroup group : list) {
                double[] tempTotal = formatLayoutGroupDetails(paramJobj, companyid, group, startDate, endDate, 0, isBalanceSheet, jArr,
                        startPreDate, endPreDate, groupTotalMap, advSearchAttributes,stockDateMap);
                total[0] += tempTotal[0];
                total[1] += tempTotal[1];
                total[2] += tempTotal[6];
                total[3] += tempTotal[7];
                total[4] += tempTotal[8];
                total[5] += tempTotal[9];
                total[6] += tempTotal[10];
                total[7] += tempTotal[11];
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTrading : " + ex.getMessage(), ex);
        }
        return total;
    }
 
 @Override
    public double[] formatLayoutGroupDetails(JSONObject paramJobj, String companyid, LayoutGroup group, Date startDate, Date endDate, int level, 
            boolean isBalanceSheet, JSONArray jArr,Date startPreDate,Date endPreDate, Map<String, double[]> groupTotalMap, Map<String, Object> advSearchAttributes, Map<String, Map> stockDateMap) throws ServiceException, SessionExpiredException, ParseException {
       
     double totalAmount[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        boolean isDebit = false;
        try {
            String reportView = paramJobj.optString("reportView",null);
            boolean isPeriodView = StringUtil.isNullOrEmpty(paramJobj.optString("periodView",null)) ? false : Boolean.parseBoolean(paramJobj.optString("periodView"));
            boolean isConsolidationReport = paramJobj.optBoolean("isConsolidationReport",false);
            boolean isCashFlowStatement = !StringUtil.isNullOrEmpty(reportView) ? reportView.equals("CashFlowStatement") : false;
            boolean isTrialBalance = !StringUtil.isNullOrEmpty(reportView) ? reportView.equals("TrialBalance") : false;
            double invCloseBal = 0,periodValuation=0,openingValuation=0;// assemblyValuation = 0 ,invOpeBal = 0;
            double preinvCloseBal = 0,prePeriodValuation=0,preOpeningValuation=0;// preassemblyValuation = 0, preinvOpeBal = 0;
            String Searchjson = paramJobj.optString("searchJson",null) != null ? paramJobj.optString("searchJson") : "";
            if (paramJobj.optString("DimensionBasedComparisionReport",null) != null && paramJobj.optString("DimensionBasedComparisionReport").equals("DimensionBasedComparisionReport")) {
                Searchjson = paramJobj.optString("DimensionBasedSearchJson");
            }
            boolean isShowZeroAmountAsBlank = false;
            ExtraCompanyPreferences extrapref = null;
            if (paramJobj.has(Constants.extraCompanyPreferences)) {
                extrapref = (ExtraCompanyPreferences) paramJobj.get(Constants.extraCompanyPreferences);
            } else {
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                    extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                }
            }
            isShowZeroAmountAsBlank = extrapref.isShowZeroAmountAsBlank();
            if(group.getNature() == Constants.CUSTOM_LAYOUT_CLOSING_STOCK) {//Closing Stock
                double[] groupTotal = {0,0,0,0,0,0};
                paramJobj.put("assemblyValuation", true);
                double[] valuation  = null;
                HashMap<String, Object> requestParam = new HashMap<String, Object>();
                requestParam.put(Constants.REQ_startdate, paramJobj.optString("stdate"));
                requestParam.put(Constants.REQ_enddate, paramJobj.optString("enddate"));
                requestParam.put(Constants.df, authHandler.getDateOnlyFormat());
                requestParam.put(Constants.companyKey, companyid);
                requestParam.put(Constants.Searchjson, Searchjson);
                requestParam.put(Constants.Acc_Search_Json, Searchjson);
                if (paramJobj.has(Constants.preferences)) {
                    requestParam.put(Constants.preferences, (CompanyAccountPreferences) paramJobj.get(Constants.preferences));
                }
                if (paramJobj.has(Constants.basecurrencyid)) {
                    requestParam.put(Constants.basecurrencyid, paramJobj.optString(Constants.basecurrencyid));
                }
                if (paramJobj.optString("DimensionBasedComparisionReport", null) != null && stockDateMap != null) {
                    DateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");
                    JSONObject jobj = (!StringUtil.isNullOrEmpty(Searchjson)) ? new JSONObject(Searchjson) : new JSONObject();
                    JSONArray rootArr = new JSONArray();
                    if (jobj.has("root")) {
                        rootArr = jobj.optJSONArray("root");
                    }
                    JSONObject searchjobj = null;
                    for (int i = 0; i < rootArr.length(); i++) {
                        searchjobj = rootArr.getJSONObject(i);
                        break;
                    }
                    DateFormat df = authHandler.getDateOnlyFormat();
                    String key = sqlDF.format(df.parse(paramJobj.optString("stdate"))) + "-" + sqlDF.format(df.parse(paramJobj.optString("enddate"))) + "-" + searchjobj.optString("searchText");
                    if (stockDateMap != null && stockDateMap.containsKey(key) && stockDateMap.get(key) != null && stockDateMap.get(key).containsKey("valuation")) {
                        //  if valuation for selected period and dimension is already present
                        valuation = (double[]) stockDateMap.get(key).get("valuation");
                    } else if (stockDateMap != null) {
                        //  if valuation is not calculated for selected period and dimension
                        stockDateMap = AccProductService.getInventoryValuationDataForFinancialReports(requestParam, stockDateMap);
                        valuation = (double[]) stockDateMap.get(key).get("valuation");
                    } else {
                        valuation = AccProductService.getInventoryValuationDataForFinancialReports(new HashMap<String, Object>(requestParam));
                    }
                } else {
                    valuation = AccProductService.getInventoryValuationDataForFinancialReports(new HashMap<String, Object>(requestParam));
                }
                
                openingValuation = valuation[3];
                periodValuation = valuation[4];
                if (isPeriodView) {
                    periodValuation = authHandler.round(openingValuation,companyid) + authHandler.round(periodValuation,companyid);
                    invCloseBal = periodValuation;
                } else {
                    invCloseBal = authHandler.round(openingValuation,companyid) + authHandler.round(periodValuation,companyid);
                }
                if (startPreDate != null && endPreDate != null) {
                    requestParam.put(Constants.REQ_startdate, paramJobj.optString("stpredate"));
                    requestParam.put(Constants.REQ_enddate, paramJobj.optString("endpredate"));
                    valuation = AccProductService.getInventoryValuationDataForFinancialReports(new HashMap<String, Object>(requestParam));
                    preOpeningValuation = valuation[3];
                    prePeriodValuation = valuation[4];
                    if (isPeriodView) {
                        prePeriodValuation = valuation[5];
                    } else {
                        prePeriodValuation = valuation[4];
                    }
                    preinvCloseBal = valuation[5];
                }

                groupTotal[0] = openingValuation;
                groupTotal[1] = periodValuation;
                groupTotal[2] = invCloseBal;
                groupTotal[3] = preOpeningValuation;
                groupTotal[4] = prePeriodValuation;
                groupTotal[5] = preinvCloseBal;
                if (!groupTotalMap.containsKey(group.getID())) {
                    groupTotalMap.put(group.getID(), groupTotal);
                }
                if (group.getParent() != null) {
                    totalAmount[0] += openingValuation;
                    totalAmount[1] += periodValuation;
                    totalAmount[2] += invCloseBal;
                    totalAmount[3] += preOpeningValuation;
                    totalAmount[4] += prePeriodValuation;
                    totalAmount[5] += preinvCloseBal;
                }

                JSONObject obj = new JSONObject();
                obj.put("accountname", group.getName());
                obj.put("accountid", group.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                obj.put("openingamount", isShowZeroAmountAsBlank && openingValuation == 0 ? "" : openingValuation);
                obj.put("periodamount", isShowZeroAmountAsBlank && periodValuation == 0 ? "" : periodValuation);
                obj.put("amount", isShowZeroAmountAsBlank && invCloseBal == 0 ? "" : invCloseBal);
                obj.put("preopeningamount", isShowZeroAmountAsBlank && preOpeningValuation == 0 ? "" : preOpeningValuation);
                obj.put("preperiodamount", isShowZeroAmountAsBlank && prePeriodValuation == 0 ? "" : prePeriodValuation);
                obj.put("preamount", isShowZeroAmountAsBlank && preinvCloseBal == 0 ? "" : preinvCloseBal);
                obj.put("isdebit", false);
                obj.put("nature", group.getNature());
                obj.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                obj.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
                if (group.getNumberofrows() > 0) {
                    for (int i = 0; i < group.getNumberofrows(); i++) {
                        JSONObject objtemp = new JSONObject();
                        objtemp.put("accountname", "");
                        objtemp.put("accountid", "");
                        objtemp.put("level", level);
                        objtemp.put("leaf", true);
                        objtemp.put("openingamount","");
                        objtemp.put("periodamount", "");
                        objtemp.put("amount", "");
                        objtemp.put("preopeningamount", "");
                        objtemp.put("preperiodamount", "");
                        objtemp.put("preamount","");
                        objtemp.put("isdebit", "");
                        objtemp.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                        objtemp.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
                      
                        jArr.put(objtemp);
                    }
                }
                jArr.put(obj);
            } else if (group.getNature() == Constants.CUSTOM_LAYOUT_OPENING_STOCK) {
                double[] groupTotal = {0,0,0,0,0,0};
                HashMap<String, Object> requestParam = new HashMap<String, Object>();
                requestParam.put(Constants.REQ_startdate, paramJobj.optString("stdate"));
                requestParam.put(Constants.REQ_enddate, paramJobj.optString("enddate"));
                requestParam.put(Constants.df, authHandler.getDateOnlyFormat());
                requestParam.put(Constants.companyKey, companyid);
                requestParam.put(Constants.Searchjson, Searchjson);
                requestParam.put(Constants.Acc_Search_Json, Searchjson);
                if (paramJobj.has(Constants.preferences)) {
                    requestParam.put(Constants.preferences, (CompanyAccountPreferences) paramJobj.get(Constants.preferences));
                }
                if (paramJobj.has(Constants.basecurrencyid)) {
                    requestParam.put(Constants.basecurrencyid, paramJobj.optString(Constants.basecurrencyid));
                }
                double[] valuation = null;
                if (paramJobj.optString("DimensionBasedComparisionReport", null) != null && stockDateMap != null) {
                    DateFormat sqlDF = new SimpleDateFormat("yyyy-MM-dd");
                    JSONObject jobj = (!StringUtil.isNullOrEmpty(Searchjson)) ? new JSONObject(Searchjson) : new JSONObject();
                    JSONArray rootArr = new JSONArray();
                    if (jobj.has("root")) {
                        rootArr = jobj.optJSONArray("root");
                    }
                    JSONObject searchjobj = null;
                    for (int i = 0; i < rootArr.length(); i++) {
                        searchjobj = rootArr.getJSONObject(i);
                        break;
                    }
                    DateFormat df = authHandler.getDateOnlyFormat();
                    String key = sqlDF.format(df.parse(paramJobj.optString("stdate"))) + "-" + sqlDF.format(df.parse(paramJobj.optString("enddate"))) + "-" + searchjobj.optString("searchText");
                    if (stockDateMap != null && stockDateMap.containsKey(key) && stockDateMap.get(key) != null && stockDateMap.get(key).containsKey("valuation")) {
                        //  if valuation for selected period and dimension is already present`
                        valuation = (double[]) stockDateMap.get(key).get("valuation");
                    } else if (stockDateMap != null) {
                        //  if valuation is not calculated for selected period and dimension
                        stockDateMap = AccProductService.getInventoryValuationDataForFinancialReports(requestParam, stockDateMap);
                        valuation = (double[]) stockDateMap.get(key).get("valuation");
                    } else {
                        valuation = AccProductService.getInventoryValuationDataForFinancialReports(new HashMap<String, Object>(requestParam));
                    }
                } else {
                    valuation = AccProductService.getInventoryValuationDataForFinancialReports(new HashMap<String, Object>(requestParam));
                }
                double openingStockOpening = valuation[0];
                double openingStockPeriod = 0.0,preOpeningStock=0.0,prePeriodStock=0.0,preClosingStock=0.0;
                if (isPeriodView) {
                    openingStockPeriod = valuation[2];
                } else {
                    openingStockPeriod = valuation[1];
                }
                double openingStockClosing = valuation[2];
                 if (startPreDate != null && endPreDate != null) {
                    requestParam.put(Constants.REQ_startdate, paramJobj.optString("stpredate"));
                    requestParam.put(Constants.REQ_enddate, paramJobj.optString("endpredate"));
                    valuation = AccProductService.getInventoryValuationDataForFinancialReports(new HashMap<String, Object>(requestParam));
                    preOpeningStock = valuation[0];
                    if (isPeriodView) {
                        prePeriodStock = valuation[2];
                    } else {
                        prePeriodStock = valuation[1];
                    }
                    preClosingStock = valuation[2];
                }
                groupTotal[0] = openingStockOpening;
                groupTotal[1] = openingStockPeriod;
                groupTotal[2] = openingStockClosing;
                groupTotal[3] = preOpeningStock;
                groupTotal[4] = prePeriodStock;
                groupTotal[5] = preClosingStock;
                if (!groupTotalMap.containsKey(group.getID())) {
                    groupTotalMap.put(group.getID(), groupTotal);
                }
                if (group.getParent() != null) {
                    totalAmount[0] += openingStockOpening;
                    totalAmount[1] += openingStockPeriod;
                    totalAmount[2] += openingStockClosing;
                    totalAmount[3] += preOpeningStock;
                    totalAmount[4] += prePeriodStock;
                    totalAmount[5] += preClosingStock;
                }
           
                
                
                JSONObject obj = new JSONObject();
                obj.put("accountname", group.getName());
                obj.put("accountid", group.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                obj.put("openingamount", isShowZeroAmountAsBlank && openingStockOpening==0 ? "":openingStockOpening);
                obj.put("periodamount", isShowZeroAmountAsBlank && openingStockPeriod==0 ? "":openingStockPeriod);
                obj.put("amount", isShowZeroAmountAsBlank && openingStockClosing==0 ? "":openingStockClosing);
                obj.put("preopeningamount", isShowZeroAmountAsBlank && preOpeningStock == 0 ? "" : preOpeningStock);
                obj.put("preperiodamount", isShowZeroAmountAsBlank && prePeriodStock==0 ? "":prePeriodStock);
                obj.put("preamount", isShowZeroAmountAsBlank && preClosingStock==0 ? "":preClosingStock);
                obj.put("isdebit", true);
                obj.put("nature", group.getNature());
                obj.put("fmt", "H");
                obj.put("acctype", "costofgoodssold");
                obj.put("group", "");
                if (group.getNumberofrows() > 0) {
                    for (int i = 0; i < group.getNumberofrows(); i++) {
                        JSONObject objtemp = new JSONObject();
                        objtemp.put("accountname", "");
                        objtemp.put("accountid", "");
                        objtemp.put("level", level);
                        objtemp.put("leaf", true);
                        objtemp.put("openingamount", "");
                        objtemp.put("periodamount", "");
                        objtemp.put("amount", "");
                        objtemp.put("preopeningamount", "");
                        objtemp.put("preperiodamount", "");
                        objtemp.put("preamount", "");
                        objtemp.put("isdebit", "");
                        objtemp.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                        objtemp.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));

                        jArr.put(objtemp);
                    }
                }
                jArr.put(obj);
            } else if (group.getNature() == Constants.CUSTOM_LAYOUT_DIFF_OPENING_BALANCE) {//Diff In Opening Balance
                double[] groupTotal = {0, 0, 0, 0, 0, 0};
//                double bals[] = accReportsService.getOpeningBalancesWithDate(request, sessionHandlerImpl.getCompanyid(request), startDate, endDate);
                double bals[] = accReportsService.getOpeningBalancesWithDate(paramJobj, paramJobj.getString(Constants.companyKey), !isPeriodView ? new Date(1970) : startDate, endDate);
                double preBalance = 0;
                if (startPreDate != null && endPreDate != null) {
//                    double prebalances[] = accReportsService.getOpeningBalancesWithDate(request, sessionHandlerImpl.getCompanyid(request), startPreDate, endPreDate);
                    double prebalances[] = accReportsService.getOpeningBalancesWithDate(paramJobj, paramJobj.getString(Constants.companyKey),  !isPeriodView ? new Date(1970) : startPreDate, endPreDate);
                    preBalance = prebalances[0] + prebalances[1];////+preinvCloseBal;
                }

                double balance = bals[0] + bals[1];////+invCloseBal;
                balance = authHandler.round(balance, companyid);
                preBalance = authHandler.round(preBalance, companyid);
                
                boolean isparenHaveLiabilityNature = false;
                if (group.getParent() != null && group.getParent().getNature() == Group.NATURE_LIABILITY) {
                    isparenHaveLiabilityNature = true;
                }
                if (isBalanceSheet && isparenHaveLiabilityNature) {
                    groupTotal[0] = balance != 0 ? -balance : balance;
                    groupTotal[1] = 0;
                    groupTotal[2] = balance != 0 ? -balance : balance;
                    groupTotal[3] = preBalance != 0 ? -preBalance : preBalance;
                    groupTotal[4] = 0;
                    groupTotal[5] = preBalance != 0 ? -preBalance : preBalance;
                } else{
                    groupTotal[0] = balance;
                    groupTotal[1] = 0;
                    groupTotal[2] = balance;
                    groupTotal[3] = preBalance;
                    groupTotal[4] = 0;
                    groupTotal[5] = preBalance;
                }

                
                if (!groupTotalMap.containsKey(group.getID())) {
                    groupTotalMap.put(group.getID(), groupTotal);
                }
                if (group.getParent() != null) {
                    totalAmount[0] += groupTotal[0];
                    totalAmount[1] += groupTotal[1];
                    totalAmount[2] += groupTotal[2];
                    totalAmount[3] += groupTotal[3];
                    totalAmount[4] += groupTotal[4];
                    totalAmount[5] += groupTotal[5];
                }

                JSONObject obj = new JSONObject();
                obj.put("accountname", group.getName());
                obj.put("accountid", group.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                obj.put("openingamount", isShowZeroAmountAsBlank && balance == 0 ? "" : balance);
                obj.put("periodamount", isShowZeroAmountAsBlank ? "":0);
                obj.put("amount", isShowZeroAmountAsBlank && balance == 0 ? "":balance);
                obj.put("preopeningamount", isShowZeroAmountAsBlank && preBalance == 0 ? "":preBalance);
                obj.put("preperiodamount", isShowZeroAmountAsBlank ? "":0);
                obj.put("preamount", isShowZeroAmountAsBlank && preBalance == 0 ? "":preBalance);
                obj.put("isdebit", false);
                obj.put("nature", group.getNature());
                obj.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                obj.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
                jArr.put(obj);
            } else if (group.getNature() == Constants.CUSTOM_LAYOUT_DEFINE_TOTAL) {//Defined Total
                double[] groupTotal = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                KwlReturnObject plresult1 = accAccountDAOobj.getCustomsGroupsForTotal(group.getID());
                List list1 = plresult1.getEntityList();
                Iterator itr1 = list1.iterator();
                while (itr1.hasNext()) {
                    Groupmapfortotal grouprule = (Groupmapfortotal) itr1.next();
                    String grId = grouprule.getGroupid().getID();
                    String ruletype = grouprule.getAction() != null ? grouprule.getAction() : "plus";
                    if (groupTotalMap.containsKey(grId)) {
                        double[] groupTotalTemp = groupTotalMap.get(grId);
                        if (ruletype.equalsIgnoreCase("minus")) {
                            groupTotal[0] -= groupTotalTemp[0];//Opeing amount
                            groupTotal[1] -= groupTotalTemp[1];//period amount
                            groupTotal[2] -= groupTotalTemp[2];//Ending amount
                            groupTotal[3] -= groupTotalTemp[3];
                            groupTotal[4] -= groupTotalTemp[4];
                            groupTotal[5] -= groupTotalTemp[5];
                            if (isTrialBalance) {
                                groupTotal[6] -= groupTotalTemp[6]; // d_opening
                                groupTotal[7] -= groupTotalTemp[7]; // c_opening
                                groupTotal[8] -= groupTotalTemp[8]; // d_period
                                groupTotal[9] -= groupTotalTemp[9]; // c_period
                                groupTotal[10] -= groupTotalTemp[10]; // d_ending
                                groupTotal[11] -= groupTotalTemp[11]; // c_ending
                            }
                        } else {
                            groupTotal[0] += groupTotalTemp[0];
                            groupTotal[1] += groupTotalTemp[1];
                            groupTotal[2] += groupTotalTemp[2];
                            groupTotal[3] += groupTotalTemp[3];
                            groupTotal[4] += groupTotalTemp[4];
                            groupTotal[5] += groupTotalTemp[5];
                            if (isTrialBalance) {
                                groupTotal[6] += groupTotalTemp[6];
                                groupTotal[7] += groupTotalTemp[7];
                                groupTotal[8] += groupTotalTemp[8];
                                groupTotal[9] += groupTotalTemp[9];
                                groupTotal[10] += groupTotalTemp[10];
                                groupTotal[11] += groupTotalTemp[11];
                            }
                        }
                    }
                }
                if (!groupTotalMap.containsKey(group.getID())) {
                    groupTotalMap.put(group.getID(), groupTotal);
                }
//                if (groupTotal[2] != 0 || groupTotal[5] != 0) {
                    JSONObject obj = new JSONObject();
                    obj.put("accountname", group.getName());
                    obj.put("accountid", group.getID());
                    obj.put("level", level);
                    obj.put("leaf", true);
                    obj.put("openingamount", isShowZeroAmountAsBlank && groupTotal[0]==0 ? "":groupTotal[0]);
                    obj.put("periodamount", isShowZeroAmountAsBlank && groupTotal[1]==0? "":groupTotal[1]);
                    obj.put("amount", isShowZeroAmountAsBlank && groupTotal[2]==0 ? "":groupTotal[2]);
                    obj.put("preopeningamount", isShowZeroAmountAsBlank && groupTotal[3]==0 ? "":groupTotal[3]);
                    obj.put("preperiodamount", isShowZeroAmountAsBlank && groupTotal[4]==0 ? "":groupTotal[4]);
                    obj.put("preamount", isShowZeroAmountAsBlank && groupTotal[5]==0 ? "":groupTotal[5]);
                    obj.put("isdebit", false);
                    obj.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                    obj.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
                    if (isTrialBalance) {
                        obj.put("d_openingamount", groupTotal[6]);
                        obj.put("c_openingamount", groupTotal[7]);
                        obj.put("d_periodamount", groupTotal[8]);
                        obj.put("c_periodamount", groupTotal[9]);
                        obj.put("d_endingamount", groupTotal[10]);
                        obj.put("c_endingamount", groupTotal[11]);
                    }
                    if (group.getNumberofrows() > 0) {
                        for (int i = 0; i < group.getNumberofrows(); i++) {
                            JSONObject objtemp = new JSONObject();
                            objtemp.put("accountname", "");
                            objtemp.put("accountid", "");
                            objtemp.put("level", level);
                            objtemp.put("leaf", true);
                            objtemp.put("openingamount", "");
                            objtemp.put("periodamount", "");
                            objtemp.put("amount", "");
                            objtemp.put("preopeningamount", "");
                            objtemp.put("preperiodamount", "");
                            objtemp.put("preamount", "");
                            objtemp.put("isdebit", "");
                            objtemp.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                            objtemp.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));

                            jArr.put(objtemp);
                        }
                    }
                    jArr.put(obj);
//                }
            } else if (group.getNature() == Constants.CUSTOM_LAYOUT_NET_PROFIT_LOSS) {
                double[] groupTotal = {0, 0, 0, 0, 0, 0};
                boolean stockValuationFlag = paramJobj.optBoolean("stockValuationFlag", false);
                Date openBalEndDate = new DateTime(startDate).minusDays(1).toDate();
                double[] profitAndLossList = accReportsService.calculateProfitLossForTrialBalance(paramJobj, startDate, endDate, openBalEndDate, true, !isPeriodView, false, stockValuationFlag, null,null);
                double openingprofitloss = profitAndLossList[1];
                profitAndLossList = accReportsService.calculateProfitLossForTrialBalance(paramJobj, startDate, endDate, startDate, false, !isPeriodView, true, stockValuationFlag, null,null);
                double periodprofitloss = profitAndLossList[1];
                JSONObject obj = new JSONObject();
                obj.put("accountname", group.getName());
                obj.put("accountid", group.getID());
                obj.put("level", level);
                obj.put("leaf", true);
                if (periodprofitloss < 0 || (openingprofitloss < 0 && periodprofitloss == 0)) {
                    obj.put("accountname", group.getName() + " [Net Profit]");
                } else {
                    obj.put("accountname", group.getName() + "[Net Loss]");
                }
                /* 
                 SDP-5509 [Profit to be shown as positive and loss is to be shown in negative] 
                 If balance < 0 then it is profit 
                 */
                obj.put("openingamount", openingprofitloss != 0 ? -openingprofitloss : openingprofitloss);
                obj.put("periodamount", periodprofitloss != 0 ? -periodprofitloss : periodprofitloss);
                obj.put("amount", -(openingprofitloss + periodprofitloss));
                double preopeningprofitloss = 0.0, preperiodprofitloss = 0.0;
                if (startPreDate != null && endPreDate != null) {
                    Date preopenBalEndDate = new DateTime(startPreDate).minusDays(1).toDate();
                    double[] preprofitAndLossList = accReportsService.calculateProfitLossForTrialBalance(paramJobj, startPreDate, endPreDate, preopenBalEndDate, true, !isPeriodView, false, stockValuationFlag, null, null);
                    preopeningprofitloss = preprofitAndLossList[1];
                    preprofitAndLossList = accReportsService.calculateProfitLossForTrialBalance(paramJobj, startPreDate, endPreDate, startPreDate, false, !isPeriodView, true, stockValuationFlag, null, null);
                    preperiodprofitloss = preprofitAndLossList[1];
                }
                boolean isparenHaveLiabilityNature = false;
                if (group.getParent() != null && group.getParent().getNature() == Group.NATURE_LIABILITY) {
                    isparenHaveLiabilityNature = true;
                }
                if (isBalanceSheet && isparenHaveLiabilityNature) {//isBalanceSheet will be true when call came for custom balance sheet 
                    groupTotal[0] = openingprofitloss ;
                    groupTotal[1] = periodprofitloss ;
                    groupTotal[2] = openingprofitloss + periodprofitloss;
                } else {
                    groupTotal[0] = openingprofitloss != 0 ? -openingprofitloss : openingprofitloss;
                    groupTotal[1] = periodprofitloss != 0 ? -periodprofitloss : periodprofitloss;
                    groupTotal[2] = -(openingprofitloss + periodprofitloss);
                }                
                if (isBalanceSheet && isparenHaveLiabilityNature) {//isBalanceSheet will be true when call came for custom balance sheet 
                    groupTotal[3] = preopeningprofitloss ;
                    groupTotal[4] = preperiodprofitloss ;
                    groupTotal[5] = preopeningprofitloss + preperiodprofitloss;
                } else {
                    groupTotal[3] = preopeningprofitloss != 0 ? -preopeningprofitloss : preopeningprofitloss;
                    groupTotal[4] = preperiodprofitloss != 0 ? -preperiodprofitloss : preperiodprofitloss;
                    groupTotal[5] = -(preopeningprofitloss + preperiodprofitloss);
                }                
                if (group.getParent() != null) {
                    totalAmount[0] += groupTotal[0];
                    totalAmount[1] += groupTotal[1];
                    totalAmount[2] += groupTotal[2];
                    totalAmount[3] += groupTotal[3];
                    totalAmount[4] += groupTotal[4];
                    totalAmount[5] += groupTotal[5];
                }
                if (!groupTotalMap.containsKey(group.getID())) {
                    groupTotalMap.put(group.getID(), groupTotal);
                }
                obj.put("preopeningamount", preopeningprofitloss != 0 ? -preopeningprofitloss : preopeningprofitloss);
                obj.put("preperiodamount", preperiodprofitloss != 0 ? -preperiodprofitloss : preperiodprofitloss);
                obj.put("preamount", -(preopeningprofitloss + preperiodprofitloss));
                obj.put("isdebit", false);
                obj.put("nature", group.getNature());
                obj.put("acctype", "");
                obj.put("group", "");
                if (group.getNumberofrows() > 0) {
                    for (int i = 0; i < group.getNumberofrows(); i++) {
                        JSONObject objtemp = new JSONObject();
                        objtemp.put("accountname", "");
                        objtemp.put("accountid", "");
                        objtemp.put("level", level);
                        objtemp.put("leaf", true);
                        objtemp.put("openingamount", "");
                        objtemp.put("periodamount", "");
                        objtemp.put("amount", "");
                        objtemp.put("preopeningamount", "");
                        objtemp.put("preperiodamount", "");
                        objtemp.put("preamount", "");
                        objtemp.put("isdebit", "");
                        objtemp.put("acctype", "");
                        objtemp.put("group", "");

                        jArr.put(objtemp);
                    }
                }
                paramJobj.put("periodView",isPeriodView);
                jArr.put(obj);
            } else {
                if (isBalanceSheet) {
                    if (group.getNature() == Group.NATURE_LIABILITY) {
                        isDebit = true;
                    }
                } else if(isCashFlowStatement){
                    if (group.getNature() == Group.NATURE_LIABILITY || group.getNature() == Group.NATURE_EXPENSES) {
                        isDebit = true;
                    }
                } else if (group.getNature() == Group.NATURE_EXPENSES) {
                    isDebit = true;
                }

                JSONArray chArr = new JSONArray();
                Map<String, Object> filterParams = new HashMap<String, Object>();
                if (isConsolidationReport && !group.getCompany().getCompanyID().equals(companyid)) {//For Custom Consolidation Report we need Child Company's Account by name. So following code written in if condition
                    filterParams.put(Constants.companyKey, group.getCompany().getCompanyID());
                } else {
                    filterParams.put(Constants.companyKey, companyid);
                }

                filterParams.put("groupid", group.getID());
                filterParams.put("parent", null);
                KwlReturnObject accresult = accAccountDAOobj.getAccountsForLayoutGroup(filterParams);
                List<GroupAccMap> list2 = accresult.getEntityList();
                boolean directionDesc = false;
                boolean sortOnType = false;
                LayoutAccountComp accComp = new LayoutAccountComp(sortOnType, directionDesc);
                Collections.sort(list2, accComp);
                DateFormat sdf = authHandler.getDateOnlyFormat();

                //For Custom Consolidation Report we need Child Company's Account by name. So following code written in if condition
                if (isConsolidationReport && !group.getCompany().getCompanyID().equals(companyid)) {
                    Set<String> accName = new HashSet<>();
                    for (GroupAccMap accgroup : list2) {
                        accName.add(accgroup.getAccount().getAccountName());
                    }
                    if(!accName.isEmpty()){
                        KwlReturnObject accResultByName = accAccountDAOobj.getAccountsFromName(companyid, accName);
                        List<Account> accList = accResultByName.getEntityList();

                        for (Account groupAccount : accList) {
                            double tempTotalAmount[] = formatLayoutAccountDetails(paramJobj, group, groupAccount, startDate, endDate, level + 1,
                                    isDebit, isBalanceSheet, chArr, sdf, startPreDate, endPreDate, companyid, advSearchAttributes);

                            totalAmount[0] += tempTotalAmount[0];
                            totalAmount[1] += tempTotalAmount[1];
                            totalAmount[2] += tempTotalAmount[2];
                            totalAmount[3] += tempTotalAmount[3];
                            totalAmount[4] += tempTotalAmount[4];
                            totalAmount[5] += tempTotalAmount[5];
                            totalAmount[6] += tempTotalAmount[6];
                            totalAmount[7] += tempTotalAmount[7];
                            totalAmount[8] += tempTotalAmount[8];
                            totalAmount[9] += tempTotalAmount[9];
                            totalAmount[10] += tempTotalAmount[10];
                            totalAmount[11] += tempTotalAmount[11];
                        }
                    }
                } else {
                    for (GroupAccMap accgroup : list2) {
//                    double tempTotalAmount[] = formatLayoutAccountDetails(request, group, accgroup.getAccount(), startDate, endDate, level + 1, 
//                            isDebit, isBalanceSheet, chArr, sdf,startPreDate,endPreDate, companyMaxDateProductPriceList, inventoryOpeningBalanceDate, companyid);
                        double tempTotalAmount[] = formatLayoutAccountDetails(paramJobj, group, accgroup.getAccount(), startDate, endDate, level + 1,
                                isDebit, isBalanceSheet, chArr, sdf, startPreDate, endPreDate, companyid, advSearchAttributes);

                        totalAmount[0] += tempTotalAmount[0];
                        totalAmount[1] += tempTotalAmount[1];
                        totalAmount[2] += tempTotalAmount[2];
                        totalAmount[3] += tempTotalAmount[3];
                        totalAmount[4] += tempTotalAmount[4];
                        totalAmount[5] += tempTotalAmount[5];
                        totalAmount[6] += tempTotalAmount[6];
                        totalAmount[7] += tempTotalAmount[7];
                        totalAmount[8] += tempTotalAmount[8];
                        totalAmount[9] += tempTotalAmount[9];
                        totalAmount[10] += tempTotalAmount[10];
                        totalAmount[11] += tempTotalAmount[11];
                    }
                }

                HashMap<String, Object> filterParams1 = new HashMap<String, Object>();
                filterParams1.put("parentid", group.getID());
                KwlReturnObject plresult = accAccountDAOobj.getCustomLayoutGroups(filterParams1);
                List<LayoutGroup> list1 = plresult.getEntityList();
                for(LayoutGroup child : list1) {
//                         double tempTotalAmount[] = formatLayoutGroupDetails(request, companyid, child, startDate, endDate, level + 1, 
//                                 isBalanceSheet, chArr,startPreDate,endPreDate, groupTotalMap, companyMaxDateProductPriceList, inventoryOpeningBalanceDate); 
                         double tempTotalAmount[] = formatLayoutGroupDetails(paramJobj, companyid, child, startDate, endDate, level + 1, 
                                 isBalanceSheet, chArr,startPreDate,endPreDate, groupTotalMap, advSearchAttributes,stockDateMap); 
                         
                         
                    totalAmount[0] += tempTotalAmount[0];
                    totalAmount[1] += tempTotalAmount[1];
                    totalAmount[2] += tempTotalAmount[2];
                    totalAmount[3] += tempTotalAmount[3];
                    totalAmount[4] += tempTotalAmount[4];
                    totalAmount[5] += tempTotalAmount[5];
                    totalAmount[6] += tempTotalAmount[6];
                    totalAmount[7] += tempTotalAmount[7];
                    totalAmount[8] += tempTotalAmount[8];
                    totalAmount[9] += tempTotalAmount[9];
                    totalAmount[10] += tempTotalAmount[10];
                    totalAmount[11] += tempTotalAmount[11];
                }
                double openingamount = totalAmount[0], periodamount = totalAmount[1], endingamount = totalAmount[2];
                double preopeningamount = totalAmount[3], preperiodamount = totalAmount[4], preendingamount = totalAmount[5];
                double newTotalAmount[] = {0, 0, 0, 0, 0, 0};
                double d_openingamount = totalAmount[6], c_openingamount = totalAmount[7], d_periodamount = totalAmount[8], c_periodamount = totalAmount[9], d_endingamount = totalAmount[10], c_endingamount = totalAmount[11];

                newTotalAmount = Arrays.copyOf(totalAmount, totalAmount.length);

                if (!isDebit) {
                    if (totalAmount[0] != 0) {
                        openingamount = -totalAmount[0];
                        newTotalAmount[0] = -totalAmount[0];
                    }
                    if (totalAmount[3] != 0) {
                        preopeningamount = -totalAmount[3];
                        newTotalAmount[3] = -totalAmount[3];
                    }

                    if (totalAmount[1] != 0) {
                        periodamount = -totalAmount[1];
                        newTotalAmount[1] = -totalAmount[1];
                    }
                    if (totalAmount[4] != 0) {
                        preperiodamount = -totalAmount[4];
                        newTotalAmount[4] = -totalAmount[4];
                    }

                    if (totalAmount[2] != 0) {
                        endingamount = -totalAmount[2];
                        newTotalAmount[2] = -totalAmount[2];
                    }
                    if (totalAmount[5] != 0) {
                        preendingamount = -totalAmount[5];
                        newTotalAmount[5] = -totalAmount[5];
                    }
                }
                if (isBalanceSheet || (isCashFlowStatement && (group.getNature() == Group.NATURE_LIABILITY || group.getNature() == Group.NATURE_ASSET))) {
                    if (totalAmount[0] != 0) {
                        openingamount = -openingamount;
                        newTotalAmount[0] = openingamount;
                    }
                    if (totalAmount[3] != 0) {
                        preopeningamount = -preopeningamount;
                        newTotalAmount[3] = preopeningamount;
                    }

                    if (totalAmount[1] != 0) {
                        periodamount = -periodamount;
                        newTotalAmount[1] = periodamount;
                    }
                    if (totalAmount[4] != 0) {
                        preperiodamount = -preperiodamount;
                        newTotalAmount[4] = preperiodamount;
                    }

                    if (totalAmount[2] != 0) {
                        endingamount = -endingamount;
                        newTotalAmount[2] = endingamount;
                    }
                    if (totalAmount[5] != 0) {
                        preendingamount = -preendingamount;
                        newTotalAmount[5] = preendingamount;
                    }
                }
                if (!groupTotalMap.containsKey(group.getID())) {
                    groupTotalMap.put(group.getID(), newTotalAmount);
                }

                if (chArr.length() > 0) {
                    JSONObject obj = new JSONObject();
                    obj.put("accountname", group.getName());
                    obj.put("accountid", group.getID());
                    obj.put("level", level);
                    obj.put("leaf", false);
                    obj.put("openingamount", "");
                    obj.put("periodamount", "");
                    obj.put("amount", "");
                    obj.put("preopeningamount", "");
                    obj.put("preperiodamount", "");
                    obj.put("preamount", "");
                     if (isTrialBalance) {
                        obj.put("d_openingamount", "");
                        obj.put("c_openingamount", "");
                        obj.put("d_periodamount", "");
                        obj.put("c_periodamount", "");
                        obj.put("d_endingamount", "");
                        obj.put("c_endingamount", "");
                    }
                    obj.put("isdebit", isDebit);
                    obj.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                    obj.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
                    if(isConsolidationReport){//This flag used in Custom Consolidation Report
                        obj.put("isgroupflag", true);
                    }
                    
                  if (group.getNumberofrows() > 0) {
                    for (int i = 0; i < group.getNumberofrows(); i++) {
                        JSONObject objtemp = new JSONObject();
                        objtemp.put("accountname", "");
                        objtemp.put("accountid", "");
                        objtemp.put("level", level);
                        objtemp.put("leaf", true);
                        objtemp.put("openingamount","");
                        objtemp.put("periodamount", "");
                        objtemp.put("amount", "");
                        objtemp.put("preopeningamount", "");
                        objtemp.put("preperiodamount", "");
                        objtemp.put("preamount","");
                        objtemp.put("isdebit", "");
                        objtemp.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                        objtemp.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
                      
                        jArr.put(objtemp);
                    }
                }
                    jArr.put(obj);
                    if (group.getShowchild() == 1) {
                        for (int i = 0; i < chArr.length(); i++) {
                            /*
                             * If Net Profit/Loss , Diff in opening balance,
                             * opening stock or closing stock are added as sub
                             * group in custom layout then add amounts of above
                             * in total of Parent group.
                             */
//                            JSONObject childObj = chArr.getJSONObject(i);
//                            int childNature = childObj.optInt("nature", -1);
//                            if (childNature == Constants.CUSTOM_LAYOUT_NET_PROFIT_LOSS || childNature == Constants.CUSTOM_LAYOUT_DIFF_OPENING_BALANCE || childNature == Constants.CUSTOM_LAYOUT_OPENING_STOCK || childNature == Constants.CUSTOM_LAYOUT_CLOSING_STOCK) {
//                                openingamount += childObj.optDouble("openingamount",0);
//                                periodamount += childObj.optDouble("periodamount",0);
//                                endingamount += childObj.optDouble("amount",0);
//                            }
                            
                            jArr.put(chArr.getJSONObject(i));
                        }
                    }

                    if (group.getShowtotal() == 1) {
                        obj = new JSONObject();
                        obj.put("accountname", "Total " + group.getName());
                        obj.put("accountid", "Total" + group.getID());
                        obj.put("level", level);
                        obj.put("leaf", true);
                        obj.put("openingamount", openingamount);
                        obj.put("periodamount", periodamount);
                        obj.put("amount", endingamount);
                        obj.put("preopeningamount", preopeningamount);
                        obj.put("preperiodamount", preperiodamount);
                        obj.put("preamount", preendingamount);
                        obj.put("isdebit", false);
                        obj.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                        obj.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
                        if (isTrialBalance) {
                            obj.put("d_openingamount", d_openingamount);
                            obj.put("c_openingamount", c_openingamount);
                            obj.put("d_periodamount", d_periodamount);
                            obj.put("c_periodamount", c_periodamount);
                            obj.put("d_endingamount", d_endingamount);
                            obj.put("c_endingamount", c_endingamount);
                        }
                        jArr.put(obj);
                    }
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("formatGroupDetails : " + ex.getMessage(), ex);
        }
        return totalAmount;
    }

 
    private class accountComp implements Comparator<Account> {

        private boolean sortOnType;
        private boolean directionDesc;

        private accountComp(boolean sortOnType1, boolean direction1) {
            sortOnType = sortOnType1;
            directionDesc = direction1;
        }

        @Override
        public int compare(Account o1, Account o2) {
            String o1_Code = (o1.getAcccode() == null) ? "" : o1.getAcccode();
            String o2_Code = (o2.getAcccode() == null) ? "" : o2.getAcccode();

            if (sortOnType) {
                if (StringUtil.equal(o1.getGroup().getName(), o2.getGroup().getName())) {
                    if (StringUtil.equal(o1_Code, o2_Code)) {
                        return directionDesc ? o2.getName().compareTo(o1.getName()) : o1.getName().compareTo(o2.getName());
                    } else {
                        return directionDesc ? o2_Code.compareTo(o1_Code) : o1_Code.compareTo(o2_Code);
                    }
                } else {
                    return directionDesc ? o2.getGroup().getName().compareTo(o1.getGroup().getName()) : o1.getGroup().getName().compareTo(o2.getGroup().getName());
                }
            } else {
                if (StringUtil.equal(o1_Code, o2_Code)) {
                    return directionDesc ? o2.getName().compareTo(o1.getName()) : o1.getName().compareTo(o2.getName());
                } else {
                    return directionDesc ? o2_Code.compareTo(o1_Code) : o1_Code.compareTo(o2_Code);
                }
            }
        }
    }
 
      private class LayoutAccountComp implements Comparator<GroupAccMap> {

        private boolean sortOnType;
        private boolean directionDesc;

        private LayoutAccountComp(boolean sortOnType1, boolean direction1) {
            sortOnType = sortOnType1;
            directionDesc = direction1;
        }

        @Override
        public int compare(GroupAccMap o1, GroupAccMap o2) {
            String o1_Code = (o1.getAccount().getAcccode() == null) ? "" : o1.getAccount().getAcccode();
            String o2_Code = (o2.getAccount().getAcccode() == null) ? "" : o2.getAccount().getAcccode();

            if (sortOnType) {
                if (StringUtil.equal(o1.getAccount().getGroup().getName(), o2.getAccount().getGroup().getName())) {
                    if (StringUtil.equal(o1_Code, o2_Code)) {
                        return directionDesc ? o2.getAccount().getName().compareTo(o1.getAccount().getName()) : o1.getAccount().getName().compareTo(o2.getAccount().getName());
                    } else {
                        return directionDesc ? o2_Code.compareTo(o1_Code) : o1_Code.compareTo(o2_Code);
                    }
                } else {
                    return directionDesc ? o2.getAccount().getGroup().getName().compareTo(o1.getAccount().getGroup().getName()) : o1.getAccount().getGroup().getName().compareTo(o2.getAccount().getGroup().getName());
                }
            } else {
                if (StringUtil.equal(o1_Code, o2_Code)) {
                    return directionDesc ? o2.getAccount().getName().compareTo(o1.getAccount().getName()) : o1.getAccount().getName().compareTo(o2.getAccount().getName());
                } else {
                    return directionDesc ? o2_Code.compareTo(o1_Code) : o1_Code.compareTo(o2_Code);
                }
            }
        }
    }  
    
 @Override
    public double[] formatLayoutAccountDetails(JSONObject paramJobj, LayoutGroup group, Account account, Date startDate, Date endDate, int level, 
            boolean isDebit, boolean isBalanceSheet, JSONArray jArr, DateFormat sdf,Date startPreDate,Date endPreDate, 
            String companyid, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException, ParseException, JSONException {
        
        boolean isDeleted = false;
        if (account.isDeleted()) { //BUG #16733: Deleted account check for sub Assets/Account
            isDeleted = true;
        }
        String reportView = paramJobj.optString("reportView",null);
        boolean isCashFlowStatement = !StringUtil.isNullOrEmpty(reportView) ? reportView.equals("CashFlowStatement") : false;
        boolean isTrialBalance = !StringUtil.isNullOrEmpty(reportView) ? reportView.equals("TrialBalance") : false;
        boolean isMonthlyReport=(paramJobj.optString("monthlyreport",null)!=null && paramJobj.optString("monthlyreport").equals("MonthlyReport")); //this flag is used to take all accounts in monthly reports
        boolean isConsolidationReport = paramJobj.optBoolean("isConsolidationReport",false);
        CompanyAccountPreferences pref = null;
        ExtraCompanyPreferences extrapref = null;
        boolean isShowZeroAmountAsBlank=false;
     if (paramJobj.has(Constants.extraCompanyPreferences)) {
         extrapref = (ExtraCompanyPreferences) paramJobj.get(Constants.extraCompanyPreferences);
     } else {
        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
            extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
        }
     }
     isShowZeroAmountAsBlank = extrapref.isShowZeroAmountAsBlank();
     if (paramJobj.has(Constants.preferences)) {
         pref = (CompanyAccountPreferences) paramJobj.get(Constants.preferences);
     } else {
         KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
            pref = (CompanyAccountPreferences) extraprefresult.getEntityList().get(0);
        }
     }

        //Logic to get new fixed asset group from old fixed asset id
        //To do - Need to test wheteher is working or not
        String fixedAssetgrpName = "";
        Group fixedAssetgrp = accAccountDAOobj.getNewGroupFromOldId(Group.FIXED_ASSETS, account.getCompany().getCompanyID());
        if (fixedAssetgrp != null) {
            fixedAssetgrpName = fixedAssetgrp.getID();
        }

        if (account.getGroup() != null && account.getGroup().getID().equalsIgnoreCase(fixedAssetgrpName)) { //BUG Fixed #16739 : Creation date check for Fixed Assets
            Date createdOn = AccountingManager.resetTimeField(account.getCreationDate());
            Date toDate = AccountingManager.resetTimeField(endDate);
            if (toDate.compareTo(createdOn) <= 0) {
                isDeleted = true;
            }
        }
        double amount[] = {0, 0, 0};
        double previousAmount[] = {0, 0, 0};
        double arrayAmount[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        double openingamount = 0, periodamount = 0, endingamount = 0;
        double d_openingamount=0,c_openingamount=0,d_periodamount=0,c_periodamount=0,d_endingamount=0,c_endingamount=0;
        double preopeningamount = 0, preperiodamount = 0, preendingamount = 0;
        JSONArray chArr = new JSONArray();
        if (!isDeleted) {
//            amount = getLayoutAccountBalance(request, account.getID(), startDate, endDate);
            amount = getLayoutAccountBalance(paramJobj, account.getID(), startDate, endDate,companyid, advSearchAttributes);
            openingamount = amount[0];
            periodamount = amount[1];
            endingamount = amount[2];
            if (isTrialBalance) {
                if (openingamount > 0) {
                    d_openingamount = openingamount;
                } else {
                    c_openingamount = openingamount;
                }
                if (periodamount > 0) {
                    d_periodamount = periodamount;
                } else {
                    c_periodamount = periodamount;
                }
                if (endingamount > 0) {
                    d_endingamount = endingamount;
                } else {
                    c_endingamount = endingamount;
                }
            }
            if (startPreDate != null && endPreDate != null) {
//                previousAmount = getLayoutAccountBalance(request, account.getID(), startPreDate, endPreDate);
                previousAmount = getLayoutAccountBalance(paramJobj, account.getID(), startPreDate, endPreDate,companyid, advSearchAttributes);
                preopeningamount = previousAmount[0];
                preperiodamount = previousAmount[1];
                preendingamount = previousAmount[2];
            }
            openingamount = authHandler.round(openingamount, companyid);
            preopeningamount = authHandler.round(preopeningamount, companyid);
            periodamount = authHandler.round(periodamount, companyid);
            preperiodamount = authHandler.round(preperiodamount, companyid);
            endingamount = authHandler.round(endingamount, companyid);
            preendingamount = authHandler.round(preendingamount, companyid);
            if (isTrialBalance) {
                d_openingamount = authHandler.round(d_openingamount, companyid);
                c_openingamount = authHandler.round(c_openingamount, companyid);
                d_periodamount = authHandler.round(d_periodamount, companyid);
                c_periodamount = authHandler.round(c_periodamount, companyid);
                d_endingamount = authHandler.round(d_endingamount, companyid);
                c_endingamount = authHandler.round(c_endingamount, companyid);
            }

            boolean directionDesc = false;
            boolean sortOnType = false;
            accountComp accComp = new accountComp(sortOnType, directionDesc);
            List<Account> list = new ArrayList(account.getChildren());//Added code for sorting on account code, account name
            Collections.sort(list, accComp);
            if (list != null && !list.isEmpty()) {
                for(Account child : list) {
//                    double tempAmount[] = formatLayoutAccountDetails(request, group, child, startDate, endDate, level + 1, isDebit, isBalanceSheet, chArr,
//                            sdf, startPreDate, endPreDate, companyMaxDateProductPriceList, inventoryOpeningBalanceDate, companyid);
                    double tempAmount[] = formatLayoutAccountDetails(paramJobj, group, child, startDate, endDate, level + 1, isDebit, isBalanceSheet, chArr,
                            sdf, startPreDate, endPreDate, companyid, advSearchAttributes);
                    
                    if (!group.isExcludeChildAccountBalances()) { // do not include child account balances
                        openingamount += tempAmount[0];
                        periodamount += tempAmount[1];
                        endingamount += tempAmount[2];
                        if (isTrialBalance) {
                            if (tempAmount[0] > 0) {
                                d_openingamount += tempAmount[0];
                            } else {
                                c_openingamount += tempAmount[0];
                            }
                            if (tempAmount[1] > 0) {
                                d_periodamount += tempAmount[1];
                            } else {
                                c_periodamount += tempAmount[1];
                            }
                            if (tempAmount[2] > 0) {
                                d_endingamount += tempAmount[2];
                            } else {
                                c_endingamount += tempAmount[2];
                            }
                        }
                        preopeningamount += tempAmount[3];
                        preperiodamount += tempAmount[4];
                        preendingamount += tempAmount[5];
                    }
                }
            }
        }

        boolean excludePreviousYear = false;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("excludePreviousYear",null))) {
            excludePreviousYear = Boolean.parseBoolean(paramJobj.optString("excludePreviousYear"));
        }
        Date start = null;
        if (excludePreviousYear) {
//            start = accReportsService.getDateForExcludePreviousYearBalanceFilter(request, startDate);
            start = accReportsService.getDateForExcludePreviousYearBalanceFilter(paramJobj, startDate);
        }

        String accname = StringUtil.isNullOrEmpty(account.getName()) ? "" : account.getName();
        String acccode = StringUtil.isNullOrEmpty(account.getAcccode()) ? "" : account.getAcccode();
        String accID = account.getID();
        if (!StringUtil.isNullOrEmpty(accID) && !StringUtil.isNullOrEmpty(extrapref.getProfitLossAccountId()) && accID.equals(extrapref.getProfitLossAccountId())) {
            double periodprofitloss = periodamount;
            Date excludedPreviousYearDate = accReportsService.getDateForExcludePreviousYearBalanceFilter(paramJobj, startDate);
            Date previousFYEndDate = new DateTime(excludedPreviousYearDate).minusDays(1).toDate();
            double retainedEarningsOpening = accReportsService.getClosedYearNetProfitAndLoss(previousFYEndDate, pref, extrapref, companyid);
            openingamount += retainedEarningsOpening;
            arrayAmount[0] = openingamount;
            arrayAmount[1] = periodprofitloss;
            arrayAmount[2] = openingamount + periodprofitloss;
            arrayAmount[3] = preopeningamount;
            arrayAmount[4] = preperiodamount;
            arrayAmount[5] = preendingamount;
            JSONObject obj = new JSONObject();
            obj.put("accountname", accname);
            obj.put("accountcode", acccode);
            obj.put("accountid", account.getID());
            obj.put("level", level);
            obj.put("leaf", true);
            obj.put("openingamount", -openingamount);
            obj.put("periodamount", -periodprofitloss);
            obj.put("amount", -(openingamount + periodprofitloss));
            obj.put("preopeningamount", -preopeningamount);
            obj.put("preperiodamount", -preperiodamount);
            obj.put("preamount", -(preopeningamount + preperiodamount));
            obj.put("isdebit", isDebit);
            obj.put("accountflag", true);
            obj.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
            if (isTrialBalance) {
                if (retainedEarningsOpening > 0) {
                    d_openingamount += retainedEarningsOpening;
                } else {
                    c_openingamount += (retainedEarningsOpening == 0 ? retainedEarningsOpening : -retainedEarningsOpening);
                }
                if (periodprofitloss > 0) {
                    d_periodamount += periodprofitloss;
                } else {
                    c_periodamount += periodprofitloss;
                    c_periodamount +=  (periodprofitloss == 0 ? periodprofitloss : -periodprofitloss);
                }
                if ((openingamount + periodprofitloss) > 0) {
                    d_endingamount += (retainedEarningsOpening + periodprofitloss);
                } else {
                    c_endingamount += ((retainedEarningsOpening + periodprofitloss) == 0 ? (retainedEarningsOpening + periodprofitloss) : -(retainedEarningsOpening + periodprofitloss));
                }
                obj.put("d_openingamount", d_openingamount);
                obj.put("c_openingamount", c_openingamount);
                obj.put("d_periodamount", d_periodamount);
                obj.put("c_periodamount", c_periodamount);
                obj.put("d_endingamount", d_endingamount);
                obj.put("c_endingamount", c_endingamount);
            }
            arrayAmount[0] = openingamount;
            /**
             * openingamount + periodprofitloss --> For Total Net Earning in custom layout  (SDP-9594).
             */
            arrayAmount[1] = periodprofitloss;
            arrayAmount[2] = openingamount + periodprofitloss;
            arrayAmount[3] = preopeningamount;
            arrayAmount[4] = preperiodamount;
            arrayAmount[5] = preendingamount;
            arrayAmount[6] = d_openingamount;
            arrayAmount[7] = c_openingamount;
            arrayAmount[8] = d_periodamount;
            arrayAmount[9] = c_periodamount;
            arrayAmount[10] = d_endingamount;
            arrayAmount[11] = c_endingamount;
            obj.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
            boolean showChildAcc = group.getShowchildacc() == 1 ? true : false;
            if (showChildAcc && chArr.length() > 0) {
                obj.put("leaf", false);
            }
            
            jArr.put(obj);
            if (showChildAcc) {
                for (int i = 0; i < chArr.length(); i++) {
                    jArr.put(chArr.getJSONObject(i));
                }
            }
        } else if (accID.equals(extrapref.getOpeningStockAccountId())) {
//            double stock[] = accReportsService.calculateOpeningAndClosingStock(request, pref, extrapref, inventoryOpeningBalanceDate, companyid, startDate, endDate);
//            double prestock[] = accReportsService.calculateOpeningAndClosingStock(request, pref, extrapref, inventoryOpeningBalanceDate, companyid, startPreDate, endPreDate);
            double stock[] = accReportsService.calculateOpeningAndClosingStock(paramJobj, pref, extrapref,  companyid, startDate, endDate, null,null,null);
            double prestock[] = accReportsService.calculateOpeningAndClosingStock(paramJobj, pref, extrapref, companyid, startPreDate, endPreDate, null,null,null);

            arrayAmount[0] = stock[0];
            arrayAmount[1] = stock[1];
            arrayAmount[2] = stock[2];
            arrayAmount[3] = prestock[0];
            arrayAmount[4] = prestock[1];
            arrayAmount[5] = prestock[2];

            JSONObject obj = new JSONObject();
            obj.put("accountname", accname);
            obj.put("accountcode", acccode);
            obj.put("accountid", account.getID());
            obj.put("level", level);
            obj.put("leaf", true);
            obj.put("openingamount", stock[0]);
            obj.put("periodamount", stock[1]);
            obj.put("amount", stock[2]);
            obj.put("preopeningamount", prestock[0]);
            obj.put("preperiodamount", prestock[1]);
            obj.put("preamount", prestock[2]);
            obj.put("isdebit", isDebit);
            obj.put("accountflag", true);
            obj.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
            obj.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
            boolean showChildAcc = group.getShowchildacc() == 1 ? true : false;
            if (showChildAcc && chArr.length() > 0) {
                obj.put("leaf", false);
            }
            jArr.put(obj);
            if (showChildAcc) {
                for (int i = 0; i < chArr.length(); i++) {
                    jArr.put(chArr.getJSONObject(i));
                }
            }
        } else {
            try {
                if ((endingamount != 0 || preendingamount != 0 || openingamount != 0 || periodamount != 0 || preopeningamount != 0 || preperiodamount != 0) || isMonthlyReport || isConsolidationReport) {
                    if (accname.equals("Sales")) {
                        accname = accname + "";
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("accountname", accname);
                    obj.put("accountcode", acccode);
                    obj.put("accountid", account.getID());
                    obj.put("level", level);
                    obj.put("leaf", true);
                    arrayAmount[0] = openingamount;
                    arrayAmount[1] = periodamount;
                    arrayAmount[2] = endingamount;
                    arrayAmount[3] = preopeningamount;
                    arrayAmount[4] = preperiodamount;
                    arrayAmount[5] = preendingamount;
                    arrayAmount[6] = d_openingamount;
                    arrayAmount[7] = c_openingamount;
                    arrayAmount[8] = d_periodamount;
                    arrayAmount[9] = c_periodamount;
                    arrayAmount[10] = d_endingamount;
                    arrayAmount[11] = c_endingamount;
                    if (!isDebit) {
                        if (openingamount != 0) {
                            openingamount = -openingamount;
                        }
                        if (preopeningamount != 0) {
                            preopeningamount = -preopeningamount;
                        }

                        if (periodamount != 0) {
                            periodamount = -periodamount;
                        }
                        if (preperiodamount != 0) {
                            preperiodamount = -preperiodamount;
                        }

                        if (endingamount != 0) {
                            endingamount = -endingamount;
                        }
                        if (preendingamount != 0) {
                            preendingamount = -preendingamount;
                        }
                    }
                    if (isBalanceSheet || (isCashFlowStatement && (group.getNature() == Group.NATURE_LIABILITY || group.getNature() == Group.NATURE_ASSET))) {
                        if (openingamount != 0) {
                            openingamount = -openingamount;
                        }
                        if (preopeningamount != 0) {
                            preopeningamount = -preopeningamount;
                        }

                        if (periodamount != 0) {
                            periodamount = -periodamount;
                        }
                        if (preperiodamount != 0) {
                            preperiodamount = -preperiodamount;
                        }

                        if (endingamount != 0) {
                            endingamount = -endingamount;
                        }
                        if (preendingamount != 0) {
                            preendingamount = -preendingamount;
                        }
                    }

                    obj.put("openingamount", (isShowZeroAmountAsBlank && (openingamount == 0.0) ?  "":openingamount ));
                    obj.put("periodamount", (isShowZeroAmountAsBlank && periodamount == 0.0 ? "":periodamount ));
                    obj.put("amount", (isShowZeroAmountAsBlank && endingamount == 0.0 ? "":endingamount));
                    obj.put("preopeningamount", (isShowZeroAmountAsBlank && preopeningamount == 0.0 ? "":preopeningamount));
                    obj.put("preperiodamount", (isShowZeroAmountAsBlank && preperiodamount == 0.0 ? "":preperiodamount ));
                    obj.put("preamount", (isShowZeroAmountAsBlank && preendingamount == 0.0 ? "":preendingamount ));
                    obj.put("isdebit", isDebit);
                    obj.put("accountflag", true);
                    obj.put("acctype", "");//(group.getNature() == Group.NATURE_EXPENSES)?"expense":"income");
                    obj.put("group", "");//(group.getID().toString().equals("15"))?"income":((group.getID().toString().equals("8"))?"expense":""));
                    if (isTrialBalance) {
                        obj.put("d_openingamount", d_openingamount);
                        obj.put("c_openingamount", c_openingamount);
                        obj.put("d_periodamount", d_periodamount);
                        obj.put("c_periodamount", c_periodamount);
                        obj.put("d_endingamount", d_endingamount);
                        obj.put("c_endingamount", c_endingamount);
                    }
                    boolean showChildAcc = group.getShowchildacc() == 1 ? true : false;
                    if (showChildAcc && chArr.length() > 0) {
                        obj.put("leaf", false);
                    }
                    jArr.put(obj);
                    if (showChildAcc) {
                        for (int i = 0; i < chArr.length(); i++) {
                            jArr.put(chArr.getJSONObject(i));
                        }
                    }
                } else {
                    double localArrayAmount[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                    return localArrayAmount;
                }
            } catch (JSONException e) {
                throw ServiceException.FAILURE("formatAccountDetails : " + e.getMessage(), e);
            }
        }
        return arrayAmount;
    }
    
    @Override
    public double[] getLayoutAccountBalance(JSONObject paramJobj, String accountid, Date startDate, Date endDate, String companyid, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException, JSONException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        requestParams.put("costcenter", paramJobj.optString("costcenter", null));
        String searchJson = "";
        if (paramJobj.optString("DimensionBasedComparisionReport",null) != null && paramJobj.optString("DimensionBasedComparisionReport").equals("DimensionBasedComparisionReport")) {
            searchJson =  paramJobj.optString("DimensionBasedSearchJson", null);
        } else {
            searchJson = paramJobj.optString(Constants.Acc_Search_Json, null);
        }
        if (!StringUtil.isNullOrEmpty(searchJson)) {//This is used for to Get different dimension entry from dimension name for diffrent modules
            searchJson = accJournalEntryobj.getJsornStringForSearch(searchJson, accountid, null);
        }
        requestParams.put(Constants.Acc_Search_Json, searchJson);
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(InvoiceConstants.Filter_Criteria, null));
        requestParams.put("templatecode", (StringUtil.isNullOrEmpty(paramJobj.optString("templatecode", null))) ? -1 : Integer.parseInt(paramJobj.getString("templatecode")));
//        return getLayoutAccountBalance(request, requestParams, accountid, startDate, endDate);
        return getLayoutAccountBalance(paramJobj, requestParams, accountid, startDate, endDate, companyid, advSearchAttributes);
    }
    
 @Override 
    public double[] getLayoutAccountBalance(JSONObject paramJobj, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, String companyid, Map<String, Object> advSearchAttributes) throws ServiceException,JSONException {
        double amount[] = {0, 0, 0};
        double openingamount = 0;
        double periodamount = 0;
        double endingamount = 0;
        double prevendingamount = 0;
        try {
            KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
            Account account = (Account) accresult.getEntityList().get(0);
            int templatecode = (Integer) requestParams.get("templatecode");
            String costCenterId = (String) requestParams.get("costcenter");
            boolean excludePreviousYear = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("excludePreviousYear",null))) {
                excludePreviousYear = Boolean.parseBoolean(paramJobj.optString("excludePreviousYear"));
            }
            Date openBalEndDate = new DateTime(startDate).minusDays(1).toDate();
            if (excludePreviousYear && account.getAccounttype() == 0) {
//                Date start = accReportsService.getDateForExcludePreviousYearBalanceFilter(request, startDate);
                Date start = accReportsService.getDateForExcludePreviousYearBalanceFilter(paramJobj, startDate);
                prevendingamount = getLayoutAccountBalanceTrans(requestParams, accountid, start, openBalEndDate,companyid, advSearchAttributes);
            } else {
                if ((templatecode == -1) || (account.getTemplatepermcode() != null && account.getTemplatepermcode() != 0 && ((templatecode & account.getTemplatepermcode()) == templatecode))) {
                    if (StringUtil.isNullOrEmpty(costCenterId)) { //Don't consider opening balance for CostCenter
//                        openingamount = accInvoiceCommon.getOpeningBalanceOfAccount(request, account, false, null);
                        openingamount = accInvoiceCommon.getOpeningBalanceOfAccountJson(paramJobj, account, false, null);
                    }
                }
                prevendingamount = getLayoutAccountBalanceTrans(requestParams, accountid, null, openBalEndDate,companyid, advSearchAttributes);
            }
            openingamount += prevendingamount;
            amount[0] = authHandler.round(openingamount, companyid);
            periodamount = getLayoutAccountBalanceTrans(requestParams, accountid, startDate, endDate,companyid, advSearchAttributes);
            amount[1] = authHandler.round(periodamount, companyid);
            endingamount = openingamount + periodamount;
            amount[2] = authHandler.round(endingamount, companyid);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAccountBalance : " + ex.getMessage(), ex);
        }
        return amount;
    }   
 
 @Override
    public double getLayoutAccountBalanceTrans(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, String companyid, Map<String, Object> advSearchAttributes) throws ServiceException {
        double periodamount = 0;
        try {
//            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
            Account account = (Account) accresult.getEntityList().get(0);

            int templatecode = (Integer) requestParams.get("templatecode");

            String costCenterId = (String) requestParams.get("costcenter");
            HashMap params = new HashMap();
            if (requestParams.containsKey(Constants.companyKey) && requestParams.get(Constants.companyKey) != null) {
                params.put(Constants.companyKey, requestParams.get(Constants.companyKey));
            }

            String Searchjson = "";

            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();
            }
            if (StringUtil.isNullOrEmpty(Searchjson) && account.getCompany().isOptimizedflag() && (templatecode == -1)) {
                KwlReturnObject abresult = accJournalEntryobj.getAccountBalance_optimized(accountid, startDate, endDate, costCenterId);
                List list = abresult.getEntityList();
                if (list.size() > 0 && list.get(0) != null) {
                    periodamount += authHandler.round((Double) list.get(0), companyid);
                }
            } else {
                KwlReturnObject abresult = accJournalEntryobj.getAccountBalanceAmount(params, accountid, startDate, endDate, costCenterId, filterConjuctionCriteria, Searchjson,advSearchAttributes);
                List list = abresult.getEntityList();
                if (list.get(0) != null) {
                    periodamount += authHandler.round((Double) list.get(0), companyid);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAccountBalance : " + ex.getMessage(), ex);
        }
        return authHandler.round(periodamount, companyid);
    }
 
 @Override
     public JSONObject getAccountJson(JSONObject paramJobj, List list, accCurrencyDAO accCurrencyDAOobj, Map<String, Object> paramMap) throws SessionExpiredException, ServiceException {
        boolean noactivity = paramMap.get("noactivity") != null ? (Boolean) paramMap.get("noactivity") : false;
        boolean isCustomColumnExport = paramMap.get("isCustomColumnExport") != null ? (Boolean) paramMap.get("isCustomColumnExport") : false;
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        KwlReturnObject result = null;
        try {
            boolean isSplitOpeningBalanceAmount = paramJobj.optString("isSplitOpeningBalanceAmount",null) != null ? (Boolean) paramJobj.get("isSplitOpeningBalanceAmount") : false;
            boolean isSplitOpeningBalanceSearch = paramJobj.optString("isSplitOpeningBalanceSearch",null) != null ? (Boolean) paramJobj.get("isSplitOpeningBalanceSearch") : false;
            boolean isCOA = (paramMap.containsKey("isCOA") && paramMap.get("isCOA") != null) ? (Boolean) paramMap.get("isCOA") : false;
            int accountTransactionType = paramJobj.optInt("accountTransactionType",Constants.All_Transaction_TypeID);// It will be zero for all transactions  otherwise it value will be transaction type value given in constant
            KwlReturnObject bAmt = null, presentBaseAmount = null;
            String currencyid = "";
            String companyid = paramJobj.getString(Constants.companyKey);
            int countryid = accCompanyPreferencesObj.getCountryID(companyid);
            boolean accountHasJedTransaction;
            boolean accountTypeTransaction;
            boolean isGeneralLedger = false;
            boolean includeExcludeChildBalances = paramJobj.optString("includeExcludeChildBalances",null) != null ? Boolean.parseBoolean(paramJobj.optString("includeExcludeChildBalances")) : true;
            double openbalanceInbase = 0, presentbalanceInBase = 0, openbalanceSummary = 0, presentbalanceSummary = 0;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
//            Map<String, Object> variableMap = new HashMap<>();
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap<>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Account_Statement_ModuleId));
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            for (Object object : list) {
                accountHasJedTransaction = false;
                accountTypeTransaction = false;
                openbalanceInbase = 0;
                presentbalanceInBase = 0;
                Object[] row = (Object[]) object;
                Account account = (Account) row[0];
                Group group = account.getGroup();
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("groupid", group.getID());
                obj.put("groupname", group.getName());   //To show group name in COA Report.
                obj.put("nature", group.getNature());
                obj.put("ifsccode",!StringUtil.isNullOrEmpty(account.getIfsccode())?account.getIfsccode():"");
                KWLCurrency currency = (KWLCurrency) row[5];
                currencyid = account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID();
                if (paramMap.containsKey("isGeneralLedger") && (Boolean) paramMap.get("isGeneralLedger")) {
                    isGeneralLedger = true;
                }
                if (!isGeneralLedger) {
                    int count = 0;
                    if (!StringUtil.isNullOrEmpty(account.getID())) {
                        result = accJournalEntryobj.getJEDfromAccount(account.getID(), companyid);
                        count += result.getRecordTotalCount();
                        if (count > 0) {
                            accountHasJedTransaction = true;
                        }
                    }
                    obj.put("accountHasJedTransaction", accountHasJedTransaction);

                    if (!StringUtil.isNullOrEmpty(account.getID())) {
                        result = accAccountDAOobj.getIBGDetailsForAccount(account.getID(), companyid);
                        count += result.getRecordTotalCount();
                        if (count > 0) {
                            accountTypeTransaction = true;
                        } else {
                            result = accTaxObj.getTaxFromAccount(account.getID(), companyid);
                            count += result.getRecordTotalCount();
                        }
                        if (count > 0) {
                            accountTypeTransaction = true;
                        } else {
                            result = accPaymentDAOobj.getPaymentMethodFromAccount(account.getID(), companyid);
                            count += result.getRecordTotalCount();
                        }
                        if (count > 0) {
                            accountTypeTransaction = true;
                        } else {
                            result = accProductObj.getProductfromAccount(account.getID(), companyid);
                            count += result.getRecordTotalCount();
                        }
                        if (count > 0) {
                            accountTypeTransaction = true;
                        }
                    }
                    obj.put("accountTypeTransaction", accountTypeTransaction);
                }
                String Searchjson = "", searchJson = "";
                if (isGeneralLedger) {
                    // Removed search json. Because while calculating opening balance isSplitOpeningBalanceAmount is handled on search json.
                    if (paramJobj.has("Searchjson")) {
                        Searchjson = paramJobj.optString("Searchjson", "");
                        paramJobj.remove("Searchjson");
                    }
                    if (paramJobj.has("searchJson")) {
                        searchJson = paramJobj.optString("searchJson", "");
                        paramJobj.remove("searchJson");
                    }
                }
                // calculation of opening balance 
                double openbalance = accInvoiceCommon.getOpeningBalanceOfAccountJson(paramJobj, account, false, null);
                if (isGeneralLedger) {
                    if(!StringUtil.isNullOrEmpty(Searchjson)){
                        paramJobj.put("Searchjson", Searchjson);
                    }
                    if(!StringUtil.isNullOrEmpty(searchJson)){
                        paramJobj.put("searchJson", searchJson);
                    }
                }
                
                boolean isCustomer = false;
                boolean isVendor = false;
                boolean isDepreciationAccount = false;               
                boolean isAssetPurchaseAccount = false;               


                boolean accountHasOpeningTransactions = false;
                if (account.getUsedIn() != null) {
                    if (account.getUsedIn().contains(Constants.Customer_Default_Account)) {
                        isCustomer = true;
                    } else if (account.getUsedIn().contains(Constants.Vendor_Default_Account)) {
                        isVendor = true;
                    } else if (account.getUsedIn().equals(Constants.Depreciation_Provision_GL_Account)) {
                        isDepreciationAccount = true;
                    }else if (account.getUsedIn().contains(Constants.Product_Sales_Return_Account) && account.getUsedIn().contains(Constants.Product_Purchase_Return_Account)) {
                        isAssetPurchaseAccount=true;
                    }

                    if (isCustomer || isVendor ||isDepreciationAccount ||isAssetPurchaseAccount) {
//                        accountHasOpeningTransactions = accInvoiceCommon.accountHasOpeningTransactions(request, account, false, null);
                        accountHasOpeningTransactions = accInvoiceCommon.accountHasOpeningTransactionsJson(paramJobj, account, false, null);
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, openbalance, currencyid, account.getCreationDate(), 0);
                    }
                }
                obj.put("accountHasOpeningTransactions", accountHasOpeningTransactions);
                obj.put("accountopenbalance", openbalance);
                obj.put("acctaxcode", (!StringUtil.isNullOrEmpty(account.getTaxid())) ? account.getTaxid() : "");//"c340667e2896c0d80128a569f065017a");
                
                boolean accountUsedAsInventoryAccountInProduct = false;
                if(preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD){
                    KwlReturnObject result2 = accProductObj.getProductCountfromInventoryAccount(account.getID(), companyid);
                    BigInteger valueCount = !result2.getEntityList().isEmpty() ? (BigInteger) result2.getEntityList().get(0) : BigInteger.valueOf(0);
                    if(valueCount.intValue() > 0){
                        accountUsedAsInventoryAccountInProduct = true;
                    }
                }
                obj.put("accountUsedAsInventoryAccountInProduct", accountUsedAsInventoryAccountInProduct);

                double openingBalanceInAccountCurrency = 0;
                if(isGeneralLedger && accountTransactionType!=Constants.All_Transaction_TypeID && account != null && !accountHasOpeningTransactions){//GL Report Case : when report get filtered on transaction type and there is no transaction in that case we have to put zero value instead of getting from database
                    openingBalanceInAccountCurrency =0;
                }else if (account != null && !accountHasOpeningTransactions) {
                    openingBalanceInAccountCurrency = account.getOpeningBalance(); //when we change the account balance by some .1 or .01 then due to calaculation it shows wrong figures
                } else {
                    openingBalanceInAccountCurrency = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                }
                obj.put("openbalance", openingBalanceInAccountCurrency);
                if (isCOA) {// opening balance excluding child opening balance 
                    obj.put("orignalopenbalance", openingBalanceInAccountCurrency);
                }
                if (!noactivity && !account.isDeleted()) {
                    List childlist = new ArrayList(account.getChildren());
                    if (childlist.isEmpty()) {
                        openbalanceInbase = openbalance;
                        openbalanceInbase = authHandler.round(openbalanceInbase, companyid);
                    } else {
                        openbalanceInbase = openbalance;
                        openbalanceInbase = authHandler.round(openbalanceInbase, companyid);
                        if (includeExcludeChildBalances) {
                            openbalanceInbase = getTotalOpeningBalance(account, openbalanceInbase, currency.getCurrencyID(), accCurrencyDAOobj, paramJobj,companyid);
                            if (isCOA) {
                                bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, openbalanceInbase, currencyid, account.getCreationDate(), 0);
                                obj.put("openbalance", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                            }
                        }
                    }
                    if (isCOA) {
                        if ((account.getParent() == null && includeExcludeChildBalances) || !includeExcludeChildBalances) {
                            openbalanceSummary += openbalanceInbase;
                        }
                    } else if (account.getParent() == null) {
                        openbalanceSummary += openbalanceInbase;
                    }
                    obj.put("openbalanceinbase", openbalanceInbase);
                } else {
                    obj.put("openbalanceinbase", openbalanceInbase);
                }
                
                if (!isGeneralLedger) {
                    presentBaseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, account.getPresentValue(), currencyid, account.getCreationDate(), 0);
                    presentbalanceInBase = authHandler.round((Double) presentBaseAmount.getEntityList().get(0), companyid);
                    obj.put("presentbalanceInBase", presentbalanceInBase);
                    Account parentAccount = (Account) row[6];
                    if (parentAccount != null) {
                        obj.put("parentid", parentAccount.getID());
                        obj.put("parentname", parentAccount.getName());
                    }
                    obj.put("taxid", account.getTaxid());
                    if (!StringUtil.isNullOrEmpty(account.getTaxid())) {
                        KwlReturnObject taxResult = accountingHandlerDAOobj.getObject(Tax.class.getName(), account.getTaxid());
                        Tax tax = (Tax) taxResult.getEntityList().get(0);
                        obj.put("taxName", tax!=null?tax.getName():"");
                    }
                }
                               
                obj.put(Constants.currencyKey, currencyid);
                obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                obj.put("currencyCode", (account.getCurrency() == null ? currency.getCurrencyCode() : account.getCurrency().getCurrencyCode()));
                obj.put("level", row[3]);
                obj.put("leaf", row[4]);
                obj.put("presentbalance", account.getPresentValue());
                obj.put("custminbudget", account.getCustMinBudget());

                if (!StringUtil.isNullOrEmpty(account.getAcccode())) {
                    obj.put("acccode", account.getAcccode());
                } else {
                    obj.put("acccode", "");
                }
//                boolean accountCodeNotAdded = Boolean.parseBoolean((String) paramJobj.get("accountCodeNotAdded"));
                boolean accountCodeNotAdded = Boolean.parseBoolean(paramJobj.optString("accountCodeNotAdded"));
                obj.put("accnamecode", (accountCodeNotAdded) ? account.getName() : ((!StringUtil.isNullOrEmpty(account.getAcccode())) ? ("[" + account.getAcccode() + "] " + account.getName()) : account.getName()));
                obj.put("deleted", account.isDeleted());
                obj.put("creationDate", authHandler.getGlobalDateFormat().format(account.getCreationDate()));
                obj.put("userid", account.getUser() == null ? "" : account.getUser().getUserID());
                obj.put("costcenterid", account.getCostcenter() == null ? "" : account.getCostcenter().getID());
                obj.put("costcenterName", account.getCostcenter() == null ? "" : account.getCostcenter().getName());
                obj.put("aliascode", account.getAliascode() == null ? "" : account.getAliascode());
                obj.put("accounttype", account.getAccounttype());
                obj.put("accdesc", StringUtil.isNullOrEmpty(account.getDescription()) ? "" : account.getDescription());
                obj.put("controlAccounts", account.isControlAccounts());
                
                /*
                In export file status passes Active/Dormant ERP-38772
                */
                if (paramJobj.optBoolean("isExport")) {
                    obj.put("isactivate", account.isActivate() ? "Active" : "Dormant");
                } else {
                    obj.put("isactivate", account.isActivate());
                }
                obj.put("hasAccess", account.isActivate());
                /* ------------------------ Indian Company TDS Flow (ERP-20907)----------------------------------- */
                obj.put("bankbranchname", account.getBankbranchname());
                obj.put("accountno", account.getAccountno());
                obj.put("bankbranchaddress", account.getBankbranchaddress());
                obj.put("branchstate", account.getBranchstate()!=null?(account.getBranchstate().getID()!=null?account.getBranchstate().getID():""):"");
                obj.put("bsrcode", account.getBsrcode());
                obj.put("mvatcode", account.getMVATCode());
                obj.put("pincode", account.getPincode());
                /* -----------------------------------------------------------------------------------------------*/
                switch (account.getAccounttype()) {
                    case Group.ACC_TYPE_BALANCESHEET:
                        obj.put("accounttypestring", Group.ACC_TYPE_BALANCESHEETSTR);
                        break;
                    case Group.ACC_TYPE_PROFITLOSS:
                        obj.put("accounttypestring", Group.ACC_TYPE_PROFITLOSSSTR);
                        break;
                }
                obj.put("mastertypevalue", account.getMastertypevalue());
                switch (account.getMastertypevalue()) {
                    case Group.ACCOUNTTYPE_GL:
                        obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_GLSTR);
                        break;
                    case Group.ACCOUNTTYPE_CASH:
                        obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_CASHSTR);
                        break;
                    case Group.ACCOUNTTYPE_BANK:
                        obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_BANKSTR);
                        break;
                    case Group.ACCOUNTTYPE_GST:
                        if(countryid == Constants.indian_country_id){//For India Country
                            obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_GSTSTRForIndia);
                        }else if (countryid == Constants.PHILIPPINES_COUNTRY_ID) { // For PHILIPPINES country MasterTypeValueString="Tax"
                            obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_GSTSTRForPhilippines);
                        }else{
                            obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_GSTSTR);
                        }
                        break;
                }

                if (!account.isDeleted()) {
                    presentbalanceSummary += presentbalanceInBase;
                }
                if (account.isHeaderaccountflag()) {
                    obj.put("isHeaderAccount", true);
                } else {
                    obj.put("isHeaderAccount", false);
                }
                obj.put("eliminateflag", account.isEliminateflag());

                obj.put(Constants.IS_IBG_BANK, account.isIBGBank());
                if (account.isIBGBank()) {
                    if (account.getIbgBankType() == Constants.DBS_BANK_Type) {       // FOR DBS bank
                        KwlReturnObject ibgDetailResult = accAccountDAOobj.getIBGDetailsForAccount(account.getID(), account.getCompany().getCompanyID());
                        if (!ibgDetailResult.getEntityList().isEmpty()) {
                            IBGBankDetails IBGBankDetails = (IBGBankDetails) ibgDetailResult.getEntityList().get(0);
                            obj.put(Constants.IBG_BANK_DETAIL_ID, IBGBankDetails.getID());
                            obj.put(Constants.IBG_BANK, "Development Bank Of Singapore");
//                            obj.put(Constants.IBG_BANK, IBGBankDetails.getIbgbank());
                            obj.put(Constants.BANK_CODE, IBGBankDetails.getBankCode());
                            obj.put(Constants.BRANCH_CODE, IBGBankDetails.getBranchCode());
                            obj.put(Constants.ACCOUNT_NUMBER, IBGBankDetails.getAccountNumber());
                            obj.put(Constants.ACCOUNT_NAME, IBGBankDetails.getAccountName());
                            obj.put(Constants.SENDERS_COMPANYID, IBGBankDetails.getSendersCompanyID());
                            obj.put(Constants.BANK_DAILY_LIMIT, IBGBankDetails.getBankDailyLimit());
                        }
                    } else if(account.getIbgBankType() == Constants.CIMB_BANK_Type){    // FOR CIMB bank
                        KwlReturnObject ibgDetailResult = accAccountDAOobj.getCIMBDetailsForAccount(account.getID(), account.getCompany().getCompanyID());
                        if (!ibgDetailResult.getEntityList().isEmpty()) {
                            CIMBBankDetails IBGBankDetails = (CIMBBankDetails) ibgDetailResult.getEntityList().get(0);
                            obj.put(Constants.CIMB_BANK_DETAIL_ID, IBGBankDetails.getID());
                            obj.put(Constants.IBG_BANK, "Commerce International Merchant Bankers");
                            obj.put("bankAccountNumber", IBGBankDetails.getBankAccountNumber());
                            obj.put("serviceCode", IBGBankDetails.getServiceCode());
                            obj.put("ordererName", IBGBankDetails.getOrdererName());
                            obj.put("currencyCode", IBGBankDetails.getCurrencyCode());
                            obj.put("settlementMode", IBGBankDetails.getSettelementMode());
                            obj.put("postingIndicator", IBGBankDetails.getPostingIndicator());
                        }
                    } else if(account.getIbgBankType() == Constants.UOB_Bank){
                        KwlReturnObject ibgDetailResult = accAccountDAOobj.getUOBDetailsForAccount(account.getID(), account.getCompany().getCompanyID());
                        if (!ibgDetailResult.getEntityList().isEmpty()) {
                            UOBBankDetails IBGBankDetails = (UOBBankDetails) ibgDetailResult.getEntityList().get(0);
                            obj.put(Constants.UOB_BANK_DETAIL_ID, IBGBankDetails.getID());
                            obj.put(Constants.IBG_BANK, Constants.UOB_FullForm);
                            obj.put("uobOriginatingBICCode", IBGBankDetails.getOriginatingBICCode());
                            obj.put("uobCurrencyCode", IBGBankDetails.getCurrencyCode());
                            obj.put("uobOriginatingAccountNumber", IBGBankDetails.getOriginatingAccountNumber());
                            obj.put("uobOriginatingAccountName", IBGBankDetails.getOriginatingAccountName());
                            obj.put("uobUltimateOriginatingCustomer", IBGBankDetails.getUltimateOriginatingCustomer());
                            obj.put(Constants.UOB_CompanyID, IBGBankDetails.getUOBCompanyID() != null ? IBGBankDetails.getUOBCompanyID() : "");
                        }
                    } else if (account.getIbgBankType() == Constants.OCBC_BankType) {
                        KwlReturnObject ibgDetailResult = accAccountDAOobj.getOCBCBankDetailsForAccount(account.getID(), companyid);
                        if (!ibgDetailResult.getEntityList().isEmpty()) {
                            OCBCBankDetails IBGBankDetails = (OCBCBankDetails) ibgDetailResult.getEntityList().get(0);
                            obj.put(Constants.OCBC_BANK_DETAIL_ID, IBGBankDetails.getId());
                            obj.put(Constants.IBG_BANK, Constants.OCBC_FullForm);
                            obj.put(Constants.OCBC_OriginatingBankCode, IBGBankDetails.getOriginatingBankCode());
                            obj.put(Constants.OCBC_AccountNumber, IBGBankDetails.getAccountNumber());
                            obj.put(Constants.OCBC_ReferenceNumber, IBGBankDetails.getReferenceNumber());
                        }
                    }
                    obj.put("ibgbanktype",account.getIbgBankType());
                }
                
                obj.put("purchasetype", account.getPurchaseType());
                /* Indian Company - Tagging of Transaction Description at the Account Creation Level for DVAT Form 31*/
                obj.put("salestype", !StringUtil.isNullOrEmpty(account.getSalesType())?account.getSalesType():"");
                // for exporting custom fields
                if (isCustomColumnExport) {

                    KwlReturnObject idcustresult = accountingHandlerDAOobj.getObject(AccountCustomData.class.getName(), account.getID());
                    if (idcustresult.getEntityList().size() > 0) {
                        Map<String, Object> variableMap = new HashMap<>();
                        AccountCustomData jeCustom = (AccountCustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                        boolean isExport = paramJobj.optString("isExport",null)!=null? Boolean.parseBoolean(paramJobj.optString("isExport")) : false;
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyid);
                        params.put("isExport", isExport);
                        params.put("accountid", account.getID());
                        params.put("isSplitOpeningBalanceAmount", isSplitOpeningBalanceAmount);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }

                }
                if (isSplitOpeningBalanceAmount && isSplitOpeningBalanceSearch) {
                    if (openbalanceInbase != 0) {
                        jArr.put(obj);
                    }
                } else {
                    jArr.put(obj);
                }
//            }//if
            }//while"data"
            jobj.put(Constants.RES_data, jArr);
            jobj.put("openbalanceSummary", openbalanceSummary);
            jobj.put("presentbalanceSummary", presentbalanceSummary);
            requestParams = null;
//            variableMap = null;
            replaceFieldMap = null;
            customFieldMap = null;
            customDateFieldMap = null;
            FieldMap = null;
            fieldrequestParams = null;
        } catch (JSONException ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
 

//     public double getTotalOpeningBalance(Account account, double totalOpeningBalance, String defaultCurrencyid, accCurrencyDAO accCurrencyDAOobj, HttpServletRequest request) throws ServiceException {
//        try {
//            List<Account> list = new ArrayList(account.getChildren());
//            for ( Account subAccount : list) {
//                double balance = 0;
//                if (!subAccount.isDeleted()) {
//                    double openingBalance = accInvoiceCommon.getOpeningBalanceOfAccount(request, subAccount, false, null);
//                    balance = authHandler.round(openingBalance, 2);
//                }
//                totalOpeningBalance = totalOpeningBalance + balance;
//                if (subAccount.getChildren().isEmpty()) {
//                    continue;
//                }
//                //Recursive function to get child accounts
//                totalOpeningBalance = getTotalOpeningBalance(subAccount, totalOpeningBalance, defaultCurrencyid, accCurrencyDAOobj, request);
//            }
//        } catch (Exception ex) {
//             throw ServiceException.FAILURE("getTotalOpeningBalance : " + ex.getMessage(), ex);
//        }
//        return totalOpeningBalance;
//    }
 
  @Override
    public double getTotalOpeningBalance(Account account, double totalOpeningBalance, String defaultCurrencyid, accCurrencyDAO accCurrencyDAOobj, JSONObject paramJobj,String companyid) throws ServiceException {
        try {
            List<Account> list = new ArrayList(account.getChildren());
            for (Account subAccount : list) {
                double balance = 0;
                if (!subAccount.isDeleted()) {
                    double openingBalance = accInvoiceCommon.getOpeningBalanceOfAccountJson(paramJobj, subAccount, false, null);
                    balance = authHandler.round(openingBalance, companyid);
                }
                totalOpeningBalance = totalOpeningBalance + balance;
                if (subAccount.getChildren().isEmpty()) {
                    continue;
                }
                //Recursive function to get child accounts
                totalOpeningBalance = getTotalOpeningBalance(subAccount, totalOpeningBalance, defaultCurrencyid, accCurrencyDAOobj, paramJobj,companyid);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getTotalOpeningBalance : " + ex.getMessage(), ex);
        }
        return totalOpeningBalance;
    }
 
 
    @Override
    public JSONObject getNewMonthlyMYOBtradingreport(JSONObject paramJobj, JSONObject tradingjobj, boolean isPrint) throws ServiceException, SessionExpiredException {
        try {
            boolean isForTradingAndProfitLoss = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isForTradingAndProfitLoss", null))) {
                isForTradingAndProfitLoss = Boolean.parseBoolean(paramJobj.optString("isForTradingAndProfitLoss"));
            }
            double cogsGroupTotal = 0, openingStock = 0, closingStock = 0;
            double totalForIncome = 0, totalForExpense = 0;
            double totalGrossProfit = 0;
            boolean dimensionBasedMonthlyPL=(paramJobj.has("dimensionBasedMonthlyPL") && paramJobj.get("dimensionBasedMonthlyPL")!=null)? Boolean.parseBoolean(paramJobj.get("dimensionBasedMonthlyPL").toString()) :false;
            /**
             monthCount is coming only from  monthly trading profit loss , monthly revenue report, Yearly PNL report.  so added check on "monthCount".
             In case of monthly report monthCount is referrred as number of month 
             In case of Yearly report monthCount is referrred as number of Years 
             */
            int monthCount = tradingjobj.has("monthCount") ? tradingjobj.getInt("monthCount") : -1;
            
            JSONObject jobj = tradingjobj.getJSONObject(Constants.RES_data);
            JSONArray rightObjArr = jobj.getJSONArray("right");
            JSONArray leftObjArr = jobj.getJSONArray("left");
            JSONArray tradingArray = new JSONArray();

            JSONObject objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("accountname", "Income");
            objlast.put("amount", "");
            objlast.put("fmt", "B");

            for (int i = 0; i < rightObjArr.length(); i++) {
                leftObjArr.put(rightObjArr.getJSONObject(i));
            }

            int j = 0;
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("income")) {
                    if (!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("income"))) {
                        if (j == 0) {
                            tradingArray.put(objlast);
                            j++;
                        }
                        tradingArray.put(leftobj);
                    }
                    if (leftobj.has("GroupTotal") && monthCount != -1) {
                        /**
                         calculating total income = income + other income
                         */
                        if (!StringUtil.isNullOrEmpty(leftobj.getString("totalamount")) && (leftobj.optInt("level",-1) == 0)) {
                            totalForIncome += leftobj.getDouble("totalamount");
                        }
                    }
                }
            }
            boolean grossprofit = true;
            boolean grossloss = true;

            //Hrdcoded accounts to exclude from COGS group total amount calculation.
            Set<String> cogsGroupTotalAccounts = new HashSet();
            cogsGroupTotalAccounts.add(Constants.Opening_Stock_account);
            cogsGroupTotalAccounts.add(Constants.Cost_of_Goods_Sold_account);
            cogsGroupTotalAccounts.add(Constants.Total_for_Cost_of_Goods_Sold_account);
            cogsGroupTotalAccounts.add(Constants.Closing_Stock_account);
            cogsGroupTotalAccounts.add(Constants.Finish_Products_account);
            cogsGroupTotalAccounts.add(Constants.Raw_Materials_account);
            cogsGroupTotalAccounts.add(Constants.Total_Closing_Stock_account);
            
            if (isForTradingAndProfitLoss) { // show CoGS group seperately
                j = 0;
                objlast = new JSONObject();
                objlast.put("accountid", "");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amount", "");
                objlast.put("amountInSelectedCurrency", "");
                objlast.put("preamount", "");
                objlast.put("fmt", "B");
                objlast.put("accountname", "Cost of Goods Sold");
                for (int i = 0; i < leftObjArr.length(); i++) {
                    JSONObject leftobj = leftObjArr.getJSONObject(i);
                    if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("costofgoodssold")) {
                        if (!leftobj.has("group") || (leftobj.has("group") && leftobj.get("group").toString().equals("costofgoodssold"))) {
                            if (j == 0) {
                                tradingArray.put(objlast);
                                j++;
                            }
                            tradingArray.put(leftobj);
                        }
                        if(leftobj.getString("accountname").equals("Opening Stock")  && monthCount!= -1){
                            /*
                              Opening stock which is first months opening stock , but it tis already updated in last toatl column so taking it from last column
                            */
                            if (!StringUtil.isNullOrEmpty(leftobj.getString("amount_" + (monthCount - 1)))) {
                                openingStock = leftobj.getDouble("amount_" + (monthCount - 1));
                            }
                        }
                        if(leftobj.getString("accountname").equals("Total Closing Stock")  && monthCount!= -1){
                            /*
                            Closing stock  which last months closing stock  but it tis already updated in last toatl column so taking it from last column
                            */
                            if (!StringUtil.isNullOrEmpty(leftobj.getString("amount_" + (monthCount - 1)))) {
                                closingStock = leftobj.getDouble("amount_" + (monthCount - 1));
                            }
                        }
                        if ((!(cogsGroupTotalAccounts.contains(leftobj.getString("accountname"))) || (cogsGroupTotalAccounts.contains(leftobj.getString("accountname")) && leftobj.optBoolean("accountflag",false))) && monthCount != -1) {
                             // Exclude Group Total while calculating Total COGS
                            boolean isGroupTotal = StringUtil.isNullOrEmpty(leftobj.optString("GroupTotal"))? false :  Boolean.parseBoolean(leftobj.getString("GroupTotal"));
                            boolean isAccountTotal = leftobj.optBoolean("totalFlagAccountsWithchild", false);
                            if (!(isGroupTotal || isAccountTotal)) {
                                /*
                                 This is total of all accounts which comes under cogs group excling above mentioned hardcoded accounts.
                                 */
                                if (!StringUtil.isNullOrEmpty(leftobj.getString("totalamount"))) {
                                    cogsGroupTotal += leftobj.getDouble("totalamount");
                                }
                            }
                        }
                    }
                }
                j = 0;
                /*
                total COGS calculation for Total(last) column
                */
                double totalCOGS = openingStock + cogsGroupTotal - closingStock;
                
                objlast = new JSONObject();
                objlast.put("accountid", "Total Cost of Goods Sold");
                objlast.put("level", 0);
                objlast.put("isdebit", false);
                objlast.put("leaf", true);
                objlast.put("amountInSelectedCurrency", "");
                objlast.put("preamount", "");
                objlast.put("fmt", "B");
                objlast.put("accountname", "Total Cost of Goods Sold");
                for (int i = 0; i < leftObjArr.length(); i++) {
                    JSONObject leftobj = leftObjArr.getJSONObject(i);
                    if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("totalcogs")) {
                        /*
                        Updating cogs amount in total(last) column
                        */
                        if (monthCount != -1) {
                            leftobj.put("amount_" + (monthCount - 1), totalCOGS);
                        }
                        tradingArray.put(leftobj);
                    }
                }
                
                totalGrossProfit=totalForIncome - totalCOGS;
                for (int i = 0; i < leftObjArr.length(); i++) {
                    JSONObject leftobj = leftObjArr.getJSONObject(i);
                    if (leftobj.has("accountname") && leftobj.get("accountname").toString().equals("Gross Profit") && leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossprofit")) {
                        /*
                        Updating Gross Profit amount in total(last) column
                        */
                        if (monthCount != -1) {
                            leftobj.put("amount_" + (monthCount - 1), totalGrossProfit);
                        }
                        if (grossprofit) {
                            tradingArray.put(leftobj);
                            grossprofit = false;
                        }
                    } else if (leftobj.has("accountname") && leftobj.get("accountname").toString().equals("Gross Loss") && leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossloss")) {
                         /*
                        Updating Gross Loss amount in total(last) column
                        */
                        if (monthCount != -1) {
                            leftobj.put("amount_" + (monthCount - 1), totalGrossProfit);
                        }
                        if (grossloss) {
                            tradingArray.put(leftobj);
                            grossloss = false;
                        }
                    }
                }
            }
            j = 0;
            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "");
            objlast.put("fmt", "B");
            objlast.put("accountname", "Expense");
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("expense")) {
                    if (!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("expense"))) {
                        if (j == 0) {
                            tradingArray.put(objlast);
                            j++;
                        }
                        tradingArray.put(leftobj);
                        if (leftobj.has("GroupTotal") && monthCount != -1) {
                             /* 
                               calculating total expense = expense + other expense
                            */
                            if (!StringUtil.isNullOrEmpty(leftobj.getString("totalamount")) && (leftobj.optInt("level",-1) == 0)) {
                                totalForExpense += leftobj.getDouble("totalamount");
                            }
                        }
                    }
                }
            }

            if (!isForTradingAndProfitLoss) {
                for (int i = 0; i < leftObjArr.length(); i++) {
                    JSONObject leftobj = leftObjArr.getJSONObject(i);
                    if (leftobj.has("accountname") && leftobj.get("accountname").toString().equals("Gross Profit")) {
                        if (grossprofit) {
                            tradingArray.put(leftobj);
                            grossprofit = false;
                        }
                    } else if (leftobj.has("accountname") && leftobj.get("accountname").toString().equals("Gross Loss")) {
                        if (grossloss) {
                            tradingArray.put(leftobj);
                            grossloss = false;
                        }
                    }
                }
            }
            j = 0;
            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "");
            objlast.put("fmt", "B");
            objlast.put("accountname", "Other Income");
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("group") && leftobj.get("group").toString().equals("income")) {
                    if (j == 0) {
                        tradingArray.put(objlast);
                        j++;
                    }
                    tradingArray.put(leftobj);
                }
            }

            j = 0;
            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "");
            objlast.put("fmt", "B");
            objlast.put("accountname", "Other Expense");
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("group") && leftobj.get("group").toString().equals("expense")) {
                    if (j == 0) {
                        tradingArray.put(objlast);
                        j++;
                    }
                    tradingArray.put(leftobj);
                }
            }

            boolean netprofit = true;
            boolean netloss = true;
            double totoalNetProfit=totalGrossProfit - totalForExpense;
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("accountid") && leftobj.get("accountid").toString().equals("Net Profit") && leftobj.has("acctype") && leftobj.get("acctype").toString().equals("netprofit")) {
                    if (monthCount != -1) {
                        leftobj.put("amount_" + (monthCount - 1), totoalNetProfit);
                    }
                    if (netprofit) {
                        tradingArray.put(leftobj);
                        netprofit = false;
                    }
                } else if (leftobj.has("accountid") && leftobj.get("accountid").toString().equals("Net Loss") && leftobj.has("acctype") && leftobj.get("acctype").toString().equals("netloss")) {
                    if (monthCount != -1) {
                        leftobj.put("amount_" + (monthCount - 1), totoalNetProfit);
                    }
                    if (netloss) {
                        tradingArray.put(leftobj);
                        netloss = false;
                    }
                }
            }

            jobj.put("left", tradingArray);
            tradingjobj.getJSONObject(Constants.RES_data).remove("left");
            tradingjobj.getJSONObject(Constants.RES_data).put("left", tradingArray);

            if (isPrint) {
                tradingjobj.remove(Constants.RES_data);
                tradingjobj.put(Constants.RES_data, tradingArray);
            }
            if(dimensionBasedMonthlyPL){
                String customDimensionName = (paramJobj.has("customDimensionName") && paramJobj.get("customDimensionName")!=null)? paramJobj.get("customDimensionName").toString() : "";
                tradingjobj.put("customDimensionName", customDimensionName);
                tradingjobj.put("dimBasedSearchJson", (paramJobj.has("dimBasedSearchJson") && paramJobj.get("dimBasedSearchJson")!=null)? paramJobj.get("dimBasedSearchJson").toString() : "");
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return tradingjobj;
    }
    
    @Override
    public JSONObject getMonthlyYearlyTradingProfitAndLossChartJSON(JSONObject paramJobj, JSONObject jobj) throws ServiceException, SessionExpiredException {
        try {
            JSONArray jArr = new JSONArray();
            String companyid = paramJobj.optString(Constants.companyKey);
            JSONArray jleft = jobj.optJSONObject("data").optJSONArray("left");
            JSONArray jright = jobj.optJSONObject("data").optJSONArray("right");
            int monthCount = jright.optJSONObject(jright.length() - 1).optJSONArray("months").length() - 1;

            for (int i = 0; i < monthCount; i++) {
                JSONObject temp = new JSONObject();

                double total_income = 0.0;
//                double opening_stock = 0.0;
//                double closing_stock = 0.0;
                double total_cost_of_good_sold = 0.0;
                double gross_profit_loss = 0.0;
                double total_expense = 0.0;
                double net_profit_loss = 0.0;

                double gross_profit = 0.0;
                double gross_loss = 0.0;
                double net_profit = 0.0;
                double net_loss = 0.0;

                for (int j = 0; j < jleft.length(); j++) {
                    JSONObject jdataleft = jleft.optJSONObject(j);

                    String acctype = jdataleft.optString("acctype");
                    int level = jdataleft.optInt("level", 1);

                    if (level == 0) {

                        if (!StringUtil.isNullOrEmpty(acctype)) {
                            switch (acctype) {
                                case "totalcogs":
                                    total_cost_of_good_sold = jdataleft.optDouble("amount_" + i, 0.0);
                                    break;
                                case "grossprofit":
                                    gross_profit = jdataleft.optDouble("amount_" + i, 0.0);
                                    break;
                                case "expense":
                                    total_expense += jdataleft.optDouble("amount_" + i, 0.0);
                                    break;
                                case "netprofit":
                                    net_profit = jdataleft.optDouble("amount_" + i, 0.0);
                                    break;
                            }
                        }
                    }
                }
                
                for (int j = 0; j < jright.length(); j++) {
                    JSONObject jdataright = jright.optJSONObject(j);

                    String acctype = jdataright.optString("acctype");
                    int level = jdataright.optInt("level", 1);

                    if (level == 0) {

                        if (!StringUtil.isNullOrEmpty(acctype)) {
                            switch (acctype) {
                                case "income":
                                    total_income += jdataright.optDouble("amount_" + i, 0.0);
                                    break;
                                case "grossloss":
                                    gross_loss = jdataright.optDouble("amount_" + i, 0.0);
                                    break;
                                case "netloss":
                                    net_loss = jdataright.optDouble("amount_" + i, 0.0);
                                    break;
                            }
                        }
                    }
                }

                if (gross_profit != 0.0) {
                    gross_profit_loss = +gross_profit;
                } else if (gross_loss != 0.0) {
                    gross_profit_loss = -gross_loss;
                }

                if (net_profit != 0.0) {
                    net_profit_loss = +net_profit;
                } else if (net_loss != 0.0) {
                    net_profit_loss = -net_loss;
                }

                temp.put("monthname", jright.optJSONObject(jright.length() - 1).optJSONArray("months").optJSONObject(i).optString("monthname"));
                temp.put("Total Income", authHandler.round(total_income, companyid));
//                temp.put("Opening Stock", authHandler.round(opening_stock, companyid));
//                temp.put("Closing Stock", authHandler.round(closing_stock, companyid));
                temp.put("Total Cost of Good Sold", authHandler.round(total_cost_of_good_sold, companyid));
                temp.put("Gross Profit/Loss", authHandler.round(gross_profit_loss, companyid));
                temp.put("Total Expense", authHandler.round(total_expense, companyid));
                temp.put("Net Profit/Loss", authHandler.round(net_profit_loss, companyid));
                jArr.put(temp);
            }
            jobj.put(Constants.data, jArr);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return jobj;
    }
    
    private void getJSONArrayForBalanceSheet(JSONArray inputObjArr, JSONObject outputJsonObj, int monthIndex, JSONArray tempObjArr) throws JSONException {
        for (int i = 0; i < inputObjArr.length(); i++) {
            JSONObject getObj = inputObjArr.getJSONObject(i);
            String accId = "";
            if (getObj.has("accountid")) {
                accId = getObj.getString("accountid");
            }
            if (!accId.equals("")) {
                if (monthIndex == 0) {
                    JSONObject putObj = new JSONObject();
                    if (getObj.has("accountname")) {
                        putObj.put("accountname", getObj.get("accountname"));
                    }
                    if (getObj.has("accountcode")) {
                        putObj.put("accountcode", getObj.optString("accountcode"));
                    }
                    if (getObj.has("haschild")) {
                        putObj.put("haschild", getObj.optString("haschild"));
                    }
                    if (getObj.has("acctype")) {
                        putObj.put("acctype", getObj.getString("acctype"));
                    }
                    if (getObj.has("level")) {
                        putObj.put("level", getObj.get("level"));
                    }
                    if (getObj.has("fmt")) {
                        putObj.put("fmt", getObj.get("fmt"));
                    }
                    if (getObj.has("accountflag")) {
                        putObj.put("accountflag", getObj.get("accountflag"));
                    }
                    if (getObj.has("leaf")) {
                        putObj.put("leaf", getObj.getBoolean("leaf"));
                    }
                    if (getObj.has("amount")) {
                        putObj.put("amount_0", getObj.get("amount"));
                        putObj.put("totalamount", getObj.get("amount"));
                    }
                    putObj.put("accountid", accId);
                    outputJsonObj.put(accId, putObj);
                } else {
                    JSONObject putObj1 = new JSONObject();
                    if (outputJsonObj.has(accId)) {
                        putObj1 = outputJsonObj.getJSONObject(accId);
                    } else {
                        if (getObj.has("accountname")) {
                            putObj1.put("accountname", getObj.get("accountname"));
                        }
                        if (getObj.has("accountcode")) {
                            putObj1.put("accountcode", getObj.optString("accountcode"));
                        }
                        if (getObj.has("haschild")) {
                            putObj1.put("haschild", getObj.optString("haschild"));
                        }
                        if (getObj.has("acctype")) {
                            putObj1.put("acctype", getObj.getString("acctype"));
                        }
                        if (getObj.has("level")) {
                            putObj1.put("level", getObj.get("level"));
                        }
                        if (getObj.has("fmt")) {
                            putObj1.put("fmt", getObj.get("fmt"));
                        }
                        if (getObj.has("accountflag")) {
                            putObj1.put("accountflag", getObj.get("accountflag"));
                        }
                        if (getObj.has("leaf")) {
                            putObj1.put("leaf", getObj.getBoolean("leaf"));
                        }
                        putObj1.put("accountid", accId);
                        if (getObj.get("accountname").toString().equals("Net Loss")) {
                            JSONObject check = tempObjArr.getJSONObject(tempObjArr.length() - 1);
                            JSONObject check1 = tempObjArr.getJSONObject(tempObjArr.length() - 2);
                            if (check.has("accountname") && check1.has("accountname")) {
                                if (!check.get("accountname").toString().equals("Net Loss") && !check1.get("accountname").toString().equals("Net Loss")) {
                                    tempObjArr.put(getObj);
                                }
                            }
                        }
                        if (getObj.get("accountname").toString().equals("Net Profit")) {
                            JSONObject check = tempObjArr.getJSONObject(tempObjArr.length() - 1);
                            JSONObject check1 = tempObjArr.getJSONObject(tempObjArr.length() - 2);
                            if (check.has("accountname") && check1.has("accountname")) {
                                if (!check.get("accountname").toString().equals("Net Profit") && !check1.get("accountname").toString().equals("Net Profit")) {
                                    tempObjArr.put(getObj);
                                }
                            }
                        }
                        if (getObj.get("accountname").toString().equals("Gross Loss")) {
                            JSONObject check = tempObjArr.getJSONObject(tempObjArr.length() - 1);
                            JSONObject check1 = tempObjArr.getJSONObject(tempObjArr.length() - 2);
                            if (check.has("accountname") && check1.has("accountname")) {
                                if (!check.get("accountname").toString().equals("Gross Loss") && !check1.get("accountname").toString().equals("Gross Loss")) {
                                    tempObjArr.put(getObj);
                                }
                            }
                        }
                        if (getObj.get("accountname").toString().equals("Gross Profit")) {
                            JSONObject check = tempObjArr.getJSONObject(tempObjArr.length() - 1);
                            JSONObject check1 = tempObjArr.getJSONObject(tempObjArr.length() - 2);
                            if (check.has("accountname") && check1.has("accountname")) {
                                if (!check.get("accountname").toString().equals("Gross Profit") && !check1.get("accountname").toString().equals("Gross Profit")) {
                                    tempObjArr.put(getObj);
                                }
                            }
                        }
                    }
                    if (getObj.has("amount")) {
                        putObj1.put("amount_" + monthIndex, getObj.get("amount"));
                        if (putObj1.has("totalamount")) {
                            double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                            temptotal = temptotal + Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                            putObj1.put("totalamount", temptotal);
                        } else {
                            putObj1.put("totalamount", getObj.get("amount"));
                        }
                    }
                    outputJsonObj.put(accId, putObj1);
                }
            }
        }
    }
    
    public void getJSONArrayForTNPL(JSONArray inputObjArr, JSONObject outputJsonObj, int index, JSONArray tempObjArr) throws JSONException {
        for (int i = 0; i < inputObjArr.length(); i++) {
            JSONObject getObj = inputObjArr.getJSONObject(i);
            String accId = "";
            if (getObj.has("accountid")) {
                accId = getObj.getString("accountid");
            }
            if (!accId.equals("")) {
                if (index == 0) {
                    JSONObject putObj = new JSONObject();
                    if (getObj.has("accountname")) {
                        putObj.put("accountname", getObj.get("accountname"));
                    }
                    if (getObj.has("accountcode")) {
                        putObj.put("accountcode", getObj.optString("accountcode"));
                    }
                    if (getObj.has("haschild")) {
                        putObj.put("haschild", getObj.optString("haschild"));
                    }
                    if (getObj.has("acctype")) {
                        putObj.put("acctype", getObj.getString("acctype"));
                    }
                    if (getObj.has("level")) {
                        putObj.put("level", getObj.get("level"));
                    }
                    if (getObj.has("fmt")) {
                        putObj.put("fmt", getObj.get("fmt"));
                    }
                    if (getObj.has("accountflag")) {
                        putObj.put("accountflag", getObj.get("accountflag"));
                    }
                    if (getObj.has("leaf")) {
                        putObj.put("leaf", getObj.getBoolean("leaf"));
                    }
                    if (getObj.has("isdebit")) {
                        putObj.put("isdebit", getObj.get("isdebit"));
                    }
                    if (getObj.has("isparent")) {
                        putObj.put("isparent", getObj.get("isparent"));
                    }
                    
                    if (getObj.has("amountInSelectedCurrency")) {
                        putObj.put("amount_0", getObj.get("amountInSelectedCurrency"));
                        putObj.put("totalamount", getObj.get("amountInSelectedCurrency"));
                    } else if (getObj.has("amount")) {
                        putObj.put("amount_0", getObj.get("amount"));
                        putObj.put("totalamount", getObj.get("amount"));
                    }
                    
                    if (getObj.has("GroupTotal")) {
                        putObj.put("GroupTotal", getObj.get("GroupTotal"));
                    }
                    if (getObj.has("totalFlagAccountsWithchild")) {
                        putObj.put("totalFlagAccountsWithchild", getObj.get("totalFlagAccountsWithchild"));
                    }
                    putObj.put("accountid", accId);
                    outputJsonObj.put(accId, putObj);
                } else {
                    JSONObject putObj1 = new JSONObject();
                    if (outputJsonObj.has(accId)) {
                        putObj1 = outputJsonObj.getJSONObject(accId);
                    } else {
                        if (getObj.has("accountname")) {
                            putObj1.put("accountname", getObj.get("accountname"));
                        }
                        if (getObj.has("accountcode")) {
                            putObj1.put("accountcode", getObj.optString("accountcode"));
                        }
                        if (getObj.has("haschild")) {
                            putObj1.put("haschild", getObj.optString("haschild"));
                        }
                        if (getObj.has("acctype")) {
                            putObj1.put("acctype", getObj.getString("acctype"));
                        }
                        if (getObj.has("level")) {
                            putObj1.put("level", getObj.get("level"));
                        }
                        if (getObj.has("fmt")) {
                            putObj1.put("fmt", getObj.get("fmt"));
                        }
                        if (getObj.has("accountflag")) {
                            putObj1.put("accountflag", getObj.get("accountflag"));
                        }
                        if (getObj.has("leaf")) {
                            putObj1.put("leaf", getObj.getBoolean("leaf"));
                        }
                        if (getObj.has("isdebit")) {
                            putObj1.put("isdebit", getObj.get("isdebit"));
                        }
                        if (getObj.has("isparent")) {
                            putObj1.put("isparent", getObj.get("isparent"));
                        }
                        
                        if (getObj.has("totalFlagAccountsWithchild")){
                            putObj1.put("totalFlagAccountsWithchild", getObj.get("totalFlagAccountsWithchild"));
                        }
                        putObj1.put("accountid", accId);
                        if (getObj.get("accountid").toString().equals("Net Loss")) {
                            JSONObject check = tempObjArr.getJSONObject(tempObjArr.length() - 1);
                            JSONObject check1 = tempObjArr.getJSONObject(tempObjArr.length() - 2);
                            if (check.has("accountid") && check1.has("accountid")) {
                                if (!check.get("accountid").toString().equals("Net Loss") && !check1.get("accountid").toString().equals("Net Loss")) {
                                    tempObjArr.put(getObj);
                                }
                            }
                        }
                        if (getObj.get("accountid").toString().equals("Net Profit")) {
                            JSONObject check = tempObjArr.getJSONObject(tempObjArr.length() - 1);
                            JSONObject check1 = tempObjArr.getJSONObject(tempObjArr.length() - 2);
                            if (check.has("accountid") && check1.has("accountid")) {
                                if (!check.get("accountid").toString().equals("Net Profit") && !check1.get("accountid").toString().equals("Net Profit")) {
                                    tempObjArr.put(getObj);
                                }
                            }
                        }
                        if (getObj.get("accountid").toString().equals("Gross Loss")) {
                            JSONObject check = tempObjArr.getJSONObject(tempObjArr.length() - 1);
                            JSONObject check1 = tempObjArr.getJSONObject(tempObjArr.length() - 2);
                            if (check.has("accountid") && check1.has("accountid")) {
                                if (!check.get("accountid").toString().equals("Gross Loss") && !check1.get("accountid").toString().equals("Gross Loss")) {
                                    tempObjArr.put(getObj);
                                }
                            }
                        }
                        if (getObj.get("accountid").toString().equals("Gross Profit")) {
                            JSONObject check = tempObjArr.getJSONObject(tempObjArr.length() - 1);
                            JSONObject check1 = tempObjArr.getJSONObject(tempObjArr.length() - 2);
                            if (check.has("accountid") && check1.has("accountid")) {
                                if (!check.get("accountid").toString().equals("Gross Profit") && !check1.get("accountid").toString().equals("Gross Profit")) {
                                    tempObjArr.put(getObj);
                                }
                            }
                        }
                    }
                    
                    if(getObj.has("amountInSelectedCurrency")) {
                        putObj1.put("amount_" + index, getObj.get("amountInSelectedCurrency"));
                        if (putObj1.has("totalamount")) {
                            double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                            temptotal = temptotal + Double.parseDouble((getObj.get("amountInSelectedCurrency").toString().equals("") || getObj.get("amountInSelectedCurrency").toString().equals("-") || getObj.get("amountInSelectedCurrency").toString().contains("<")) ? "0.0" : (getObj.get("amountInSelectedCurrency").toString()));
                            putObj1.put("totalamount", temptotal);
                        } else {
                            putObj1.put("totalamount", getObj.get("amountInSelectedCurrency"));
                        }
                    } else if (getObj.has("amount")) {
                        putObj1.put("amount_" + index, getObj.get("amount"));
                        if (putObj1.has("totalamount")) {
                            double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                            temptotal = temptotal + Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                            putObj1.put("totalamount", temptotal);
                        } else {
                            putObj1.put("totalamount", getObj.get("amount"));
                        }
                    }
                        outputJsonObj.put(accId, putObj1);
                    }
                }
            }
        }

    public void addTotalsInJSONArray(JSONObject inputObjArr, boolean showAllAccountsInPnL, int totalpos, JSONArray tempObjArr) throws JSONException {
        for (int i = 0; i < tempObjArr.length(); i++) {
            JSONObject getObj1 = tempObjArr.getJSONObject(i);
            if (getObj1.has("accountid")) {
                String accId = getObj1.getString("accountid");
                if (inputObjArr.has(accId)) {
                    JSONObject getObj = inputObjArr.getJSONObject(accId);
                    if (getObj.has("totalamount")) {
                        if (!showAllAccountsInPnL) {
                            //To remove 0 amount account and which should not be at level 0.
                            double level = Double.parseDouble(getObj.get("level").toString());
                            if (getObj.get("totalamount").toString().equals("")) {
                                if (level != 0.0) {
                                    inputObjArr.remove(accId);
                                } else {
                                    continue;
                                }
                            } else {
                                double totalAmt = Double.parseDouble(getObj.get("totalamount").toString());
                                if (totalAmt == 0.0 && level != 0.0) {
                                    inputObjArr.remove(accId);
                                } else {
                                    getObj.put("amount_" + totalpos, getObj.get("totalamount"));
                                }
                            }
                        } else {
                            getObj.put("amount_" + totalpos, getObj.get("totalamount"));
                        }
                        if (getObj.has("accountid") && (getObj.getString("accountid").equals(Constants.Finish_Products_account) || getObj.getString("accountid").equals(Constants.Raw_Materials_account) || getObj.getString("accountid").equals(Constants.Total_Closing_Stock_account))) {
                            //Total column for Closing Stock,Finish Products,Raw Materials entity will show amount in last month's column value and not the sum of all column's amount like other normal accounts.
                            if (!StringUtil.isNullOrEmpty(getObj.getString("amount_" + String.valueOf(totalpos - 1)))) {
                                getObj.put("amount_" + totalpos, getObj.getDouble("amount_" + String.valueOf(totalpos - 1)));
                            } else {
                                getObj.put("amount_" + totalpos, "");
                            }
                        }

                        if (getObj.has("accountid") && getObj.getString("accountid").equals(Constants.Opening_Stock_account)) {
                            //Total column for Opening Stock entity will show amount in first month's column value and not the sum of all column's amount like other normal accounts.
                            if (!StringUtil.isNullOrEmpty(getObj.getString("amount_0"))) {
                                getObj.put("amount_" + totalpos, getObj.getDouble("amount_0"));
                            } else {
                                getObj.put("amount_" + totalpos, "");
                            }

                        }
                    }
                }
            }
        }
    }
    
    @Override
    public JSONArray getConsolidationReport(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONArray array = new JSONArray();
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            Date startDate = authHandler.getDateOnlyFormat().parse(paramJobj.getString("startdate"));
            Date endDate = authHandler.getDateOnlyFormat().parse(paramJobj.getString("enddate"));
            
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            
            boolean ytdFlag = false;
            boolean eliminateflag = false;
            
            Map<String,JSONObject> conslidationCompanyMap = new HashMap();// This Map is used to hold consolidation companies data like companyid,current exchange rate, stake in percentage etc 
            Map<String, List<Account>> accountMapByAccountCode = new LinkedHashMap();//this map will contains all acount list with same Account code in orddered manner
            String childCompanyIDs = "";
            
            Map requestMap = new HashMap();
            requestMap.put(Constants.companyKey, companyid);
            KwlReturnObject result = accCurrencyDAOobj.getConsolidation(requestMap);
            
            if (result != null && !result.getEntityList().isEmpty()) {
                List<ConsolidationData> consolidationDatas = result.getEntityList();
                for (ConsolidationData data : consolidationDatas) {
                    JSONObject object = new JSONObject();
                    if (data.getChildCompany() != null) {
                        Map filterMap = new HashMap();
                        filterMap.put("consolidationid", data.getID());
                        KwlReturnObject result1 = accCurrencyDAOobj.getConsolidationExchangeRate(filterMap);

                        if (result1 == null || result1.getEntityList().isEmpty()) {
                            // If echange rate is not set for any consolidation company then we will not consider that company for consolidation report
                            continue;
                        } else {
                            ConsolidationExchangeRateDetails cerd = (ConsolidationExchangeRateDetails) result1.getEntityList().get(0);
                            object.put("exchangerate", cerd.getExchangeRate());
                        }
                        object.put("id", data.getID());
                        object.put("stakeinpercentage", data.getStakeInPercentage());
                        object.put("subdomainid", data.getChildCompany().getCompanyID());
                        object.put("subdomainname", data.getChildCompany().getSubDomain());
                        childCompanyIDs += StringUtil.isNullOrEmpty(childCompanyIDs) ? data.getChildCompany().getCompanyID() : "," + data.getChildCompany().getCompanyID();
                        conslidationCompanyMap.put(data.getChildCompany().getCompanyID(), object);
                    }
                }
            }
            
            if (!StringUtil.isNullOrEmpty(childCompanyIDs)) {
                HashMap<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put("order_by", " acccode, name asc");
                filterParams.put("parent", null);
                filterParams.put("companyGroupIDs", childCompanyIDs);
                filterParams.put("ss", paramJobj.optString("ss",""));//Putting search string
                
                KwlReturnObject accresult = accAccountDAOobj.getAccountEntry(filterParams);
                List<Account> accList = accresult.getEntityList();

                //below loop use for putting  account with same code in map 
                for (Account account : accList) {
                    String key = "";
                    if (StringUtil.isNullOrEmpty(account.getAcccode())) {
                        key = account.getAccountName();
                    } else {
                        key = account.getAcccode();
                    }
                    if (accountMapByAccountCode.containsKey(key)) {
                        accountMapByAccountCode.get(key).add(account);
                    } else {
                        List<Account> accountList = new ArrayList<>();
                        accountList.add(account);
                        accountMapByAccountCode.put(key, accountList);
                    }
                }

                //Below for loop for getting opening,period and ending balance of each Account 
                for (Map.Entry<String, List<Account>> entry : accountMapByAccountCode.entrySet()) {
                    double totalOpening = 0;
                    double totalPeriod = 0;
                    double totalEnding = 0;
                    JSONObject obj = new JSONObject();
                    String accountCode = entry.getKey();
                    boolean doesAccountHaveNonZeroValueForAllCompany=false;
                    List<Account> sameAcccodeAccountList = entry.getValue();
                    for (Account account : sameAcccodeAccountList) {
                        HashMap<String, Double> openPeriodAmounts = new HashMap<String, Double>();
                        paramJobj.put(Constants.companyKey, account.getCompany().getCompanyID());
                        paramJobj.put(Constants.globalCurrencyKey, account.getCompany().getCurrency().getCurrencyID());
                        
                        accReportsService.getAccountClosingBalanceDateWiseMerged(paramJobj, account, startDate, endDate, ytdFlag, eliminateflag, openPeriodAmounts);
                        
                        double openingAmount = 0;
                        double periodAmount = 0;
                        double endingAmount = 0;
                        
                        if (openPeriodAmounts.containsKey("openingBalance")) {
                            openingAmount = openPeriodAmounts.get("openingBalance");
                        }
                        if (openPeriodAmounts.containsKey("periodBalance")) {
                            periodAmount += openPeriodAmounts.get("periodBalance");
                        }
                        
                        if (StringUtil.roundDoubleTo(openingAmount, 2) != 0 || StringUtil.roundDoubleTo(periodAmount, 2) != 0 ) {
                            doesAccountHaveNonZeroValueForAllCompany=true;
                        }
                        
                        JSONObject consolidationCompanyObject=conslidationCompanyMap.get(account.getCompany().getCompanyID());
                        double stakeInPercentage=consolidationCompanyObject.getDouble("stakeinpercentage");
                        double exchangeRate=consolidationCompanyObject.getDouble("exchangerate");
                        if(openingAmount!=0){
                            openingAmount=openingAmount*(stakeInPercentage/100)*exchangeRate;
                        }
                        if(periodAmount!=0){
                            periodAmount=periodAmount*(stakeInPercentage/100)*exchangeRate;
                        }
                        
                        endingAmount = openingAmount + periodAmount;
                        
                        obj.put("accountid", account.getID());
                        obj.put("acccode", account.getAcccode());
                        obj.put("accountname", account.getAccountName());
                        obj.put(account.getCompany().getSubDomain() + "_openingamount", openingAmount);
                        obj.put(account.getCompany().getSubDomain() + "_periodamount", periodAmount);
                        obj.put(account.getCompany().getSubDomain() + "_endingamount", endingAmount);

                        totalOpening += openingAmount;
                        totalPeriod += periodAmount;
                        totalEnding += endingAmount;
                    }
                    if (doesAccountHaveNonZeroValueForAllCompany || extraCompanyPreferences.isShowAllAccount()) { 
                        obj.put("totalopeningamount", totalOpening);
                        obj.put("totalperiodamount", totalPeriod);
                        obj.put("totalendingamount", totalEnding);
                        array.put(obj);
                    }
                }
            }
        } catch (JSONException | SessionExpiredException | ParseException | ServiceException ex) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : " + ex.getMessage(), ex);
        }
        return array;
    }
    
    /**
     *
     * @param JSONObjectParams
     * @return JSONArray of P&L data
     * @throws ServiceException
     * @throws SessionExpiredException
     */
    @Override
    public JSONObject getConsolidationProfitAndLossReport(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject returnObj = new JSONObject();
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            boolean isExport = paramJobj.optBoolean("isExport", false);
            Map<String, JSONObject> conslidationCompanyMap = new HashMap();// This Map is used to hold consolidation companies data like companyid,current exchange rate, stake in percentage etc 
            String consolidationCompanyIDs = "";

            Map requestMap = new HashMap();
            requestMap.put(Constants.companyKey, companyid);
            KwlReturnObject consolidationResult = accCurrencyDAOobj.getConsolidation(requestMap);

            if (consolidationResult != null && !consolidationResult.getEntityList().isEmpty()) {
                List<ConsolidationData> consolidationDatas = consolidationResult.getEntityList();
                for (ConsolidationData data : consolidationDatas) {
                    JSONObject object = new JSONObject();
                    if (data.getChildCompany() != null) {
                        Map filterMap = new HashMap();
                        filterMap.put("consolidationid", data.getID());
                        KwlReturnObject result1 = accCurrencyDAOobj.getConsolidationExchangeRate(filterMap);

                        if (result1 == null || result1.getEntityList().isEmpty()) {
                            // If echange rate is not set for any consolidation company then we will not consider that company for consolidation report
                            continue;
                        } else {
                            ConsolidationExchangeRateDetails cerd = (ConsolidationExchangeRateDetails) result1.getEntityList().get(0);
                            object.put("exchangerate", cerd.getExchangeRate());
                        }
                        object.put("id", data.getID());
                        object.put("stakeinpercentage", data.getStakeInPercentage());
                        object.put("subdomainid", data.getChildCompany().getCompanyID());
                        object.put("subdomainname", data.getChildCompany().getSubDomain());
                        consolidationCompanyIDs += StringUtil.isNullOrEmpty(consolidationCompanyIDs) ? data.getChildCompany().getCompanyID() : "," + data.getChildCompany().getCompanyID();
                        conslidationCompanyMap.put(data.getChildCompany().getCompanyID(), object);
                    }
                }
            }


            JSONArray totalArray = new JSONArray();
            for (Map.Entry<String, JSONObject> entry : conslidationCompanyMap.entrySet()) {
                JSONObject consolidationCompanyDetail = entry.getValue();
                String subdomain = consolidationCompanyDetail.getString("subdomainname");
                double exchangeRate = consolidationCompanyDetail.optDouble("exchangerate", 0);
                double stakeInPercentage = consolidationCompanyDetail.optDouble("stakeinpercentage", 0);

                paramJobj.put(Constants.companyKey, consolidationCompanyDetail.get("subdomainid"));
                paramJobj.put("consolidationPandL", true);
                JSONObject resOBject = getTradingAndProfitLoss(paramJobj).getJSONObject(Constants.RES_data);

                JSONArray jArrL = resOBject.getJSONArray("left");
                JSONArray jArrR = resOBject.getJSONArray("right");

                for (int i = 0; i < jArrR.length(); i++) {
                    jArrL.put(jArrR.getJSONObject(i));
                }

                for (int i = 0; i < jArrL.length(); i++) {
                    JSONObject obj = jArrL.getJSONObject(i);
                    if (obj.length() > 0) {
                        double amount = obj.optDouble("amount", 0);
                        if (amount != 0) {
                            amount = amount * (stakeInPercentage / 100) * exchangeRate;
                        }
                        boolean recordFound = false;
                        String accountName = obj.getString("accountname");
                        int x = 0;
                        for (int j = 0; j < totalArray.length(); j++) {
                            JSONObject obj1 = totalArray.getJSONObject(j);
                            if (accountName.equals(obj1.getString("accountname"))) {// once record found no need to find any more so breaking inner loop
                                recordFound = true;
                                if (amount != 0) {
                                    totalArray.getJSONObject(j).put(subdomain + "_amount", amount);
                                    totalArray.getJSONObject(j).put("totalamount", (obj1.optDouble("totalamount", 0) + amount));
                                }

                                if (obj1.optBoolean("GroupTotal", false)) {//If group total then we need to add it in the last hence removing existing object and putting at last
                                    JSONObject temp = totalArray.getJSONObject(j);
                                    totalArray.remove(j);//removing from jth location and putting at last 
                                    totalArray.put(temp);
                                }

                                break;
                            }
                        }
                        if (!recordFound) { // when record not found that means we need to add it in tatalArray
                            if (amount != 0) {
                                obj.put(subdomain + "_amount", amount);
                                obj.put("totalamount", amount);
                            }
                            totalArray.put(obj);
                        }
                    }
                }
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("left", totalArray);
            jsonObject.put("right", new JSONArray());
            returnObj.put(Constants.RES_data, jsonObject);

            JSONObject pandlOnject = getOrderedConsolidationTradingReport(returnObj, isExport);
            returnObj.put(Constants.RES_data, pandlOnject.getJSONArray(Constants.RES_data));
            returnObj.put(Constants.RES_count, pandlOnject.getJSONArray(Constants.RES_data));
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : " + ex.getMessage(), ex);
        }
        return returnObj;
    }

    private JSONObject getOrderedConsolidationTradingReport(JSONObject tradingjobj, boolean isExport) throws ServiceException, SessionExpiredException {
        try {
            JSONObject jobj = tradingjobj.getJSONObject(Constants.RES_data);
            JSONArray rightObjArr = jobj.getJSONArray("right");
            JSONArray leftObjArr = jobj.getJSONArray("left");

            JSONArray tradingArray = new JSONArray();

            JSONObject objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("accountname", "Income");
            objlast.put("amount", "");
            objlast.put("amountInSelectedCurrency", "");
            objlast.put("preamount", "");
            objlast.put("fmt", "B");

            for (int i = 0; i < rightObjArr.length(); i++) {
                leftObjArr.put(rightObjArr.getJSONObject(i));
            }

            int j = 0;
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("income")) {
                    if (!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("income"))) {
                        if (j == 0 && !isExport) {
                            tradingArray.put(objlast);
                            j++;
                        }
                        tradingArray.put(leftobj);
                    }
                }
            }
            j = 0;
            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "");
            objlast.put("amountInSelectedCurrency", "");
            objlast.put("preamount", "");
            objlast.put("fmt", "B");
            objlast.put("accountname", "Cost of Goods Sold");
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("costofgoodssold")) {
                    if (!leftobj.has("group") || (leftobj.has("group") && leftobj.get("group").toString().equals("costofgoodssold"))) {
                        if (j == 0) {
                            tradingArray.put(objlast);
                            j++;
                        }
                        tradingArray.put(leftobj);
                    }
                }
            }
            j = 0;
            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amountInSelectedCurrency", "");
            objlast.put("preamount", "");
            objlast.put("fmt", "B");
            objlast.put("accountname", "Total Cost of Goods Sold");
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("totalcogs")) {
                    tradingArray.put(leftobj);
                }
            }
            for (int i = 0; i < leftObjArr.length(); i++) {

                JSONObject leftobj = leftObjArr.getJSONObject(i);

                if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossprofit")) {
                    tradingArray.put(leftobj);
                } else if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossloss")) {
                    tradingArray.put(leftobj);
                }
            }
            j = 0;
            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "");
            objlast.put("amountInSelectedCurrency", "");
            objlast.put("preamount", "");
            objlast.put("fmt", "B");
            objlast.put("accountname", "Expense");
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("expense")) {
                    if (!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("expense"))) {
                        if (j == 0 && !isExport) {
                            tradingArray.put(objlast);
                            j++;
                        }
                        tradingArray.put(leftobj);
                    }
                }
            }

            j = 0;
            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "");
            objlast.put("amountInSelectedCurrency", "");
            objlast.put("preamount", "");
            objlast.put("fmt", "B");
            objlast.put("accountname", "Other Income");
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("group") && leftobj.get("group").toString().equals("income")) {
                    if (j == 0) {
                        tradingArray.put(objlast);
                        j++;
                    }
                    tradingArray.put(leftobj);
                }
            }

            j = 0;
            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("amount", "");
            objlast.put("amountInSelectedCurrency", "");
            objlast.put("preamount", "");
            objlast.put("fmt", "B");
            objlast.put("accountname", "Other Expense");
            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);
                if (leftobj.has("group") && leftobj.get("group").toString().equals("expense")) {
                    if (j == 0) {
                        tradingArray.put(objlast);
                        j++;
                    }
                    tradingArray.put(leftobj);
                }
            }

            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject leftobj = leftObjArr.getJSONObject(i);

                if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("netprofit")) {
                    tradingArray.put(leftobj);
                } else if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("netloss")) {
                    tradingArray.put(leftobj);
                }
            }
            tradingjobj.remove(Constants.RES_data);
            tradingjobj.put(Constants.RES_data, tradingArray);

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return tradingjobj;
    }
    
    public Map<String, JSONObject> getConsolidationCompanyDetailMap(JSONObject paramJobj) throws JSONException, ServiceException {
        Map<String, JSONObject> conslidationCompanyMap = new LinkedHashMap();// This Map is used to hold consolidation companies data like companyid,current exchange rate, stake in percentage etc 
        String companyid = paramJobj.getString(Constants.companyKey);
        String consolidationCompanyIDs = "";

        Map requestMap = new HashMap();
        requestMap.put(Constants.companyKey, companyid);
        KwlReturnObject consolidationResult = accCurrencyDAOobj.getConsolidation(requestMap);

        if (consolidationResult != null && !consolidationResult.getEntityList().isEmpty()) {
            List<ConsolidationData> consolidationDatas = consolidationResult.getEntityList();
            for (ConsolidationData data : consolidationDatas) {
                JSONObject object = new JSONObject();
                if (data.getChildCompany() != null) {
                    Map filterMap = new HashMap();
                    filterMap.put("consolidationid", data.getID());
                    KwlReturnObject result1 = accCurrencyDAOobj.getConsolidationExchangeRate(filterMap);

                    if (result1 == null || result1.getEntityList().isEmpty()) {
                        // If echange rate is not set for any consolidation company then we will not consider that company for consolidation report
                        continue;
                    } else {
                        ConsolidationExchangeRateDetails cerd = (ConsolidationExchangeRateDetails) result1.getEntityList().get(0);
                        object.put("exchangerate", cerd.getExchangeRate());
                    }
                    object.put("id", data.getID());
                    object.put("stakeinpercentage", data.getStakeInPercentage());
                    object.put("subdomainid", data.getChildCompany().getCompanyID());
                    object.put("subdomainname", data.getChildCompany().getSubDomain());
                    consolidationCompanyIDs += StringUtil.isNullOrEmpty(consolidationCompanyIDs) ? data.getChildCompany().getCompanyID() : "," + data.getChildCompany().getCompanyID();
                    conslidationCompanyMap.put(data.getChildCompany().getCompanyID(), object);
                }
            }
        }
        return conslidationCompanyMap;
    }

    /**
     *
     * @param JSONObjectParams
     * @return JSONArray of P&L data
     * @throws ServiceException
     * @throws SessionExpiredException
     */
    @Override
    public JSONObject getConsolidationBalanceSheetReport(HttpServletRequest request, JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject returnObj = new JSONObject();
        try {
            Map<String, JSONObject> conslidationCompanyMap = getConsolidationCompanyDetailMap(paramJobj);
            Set<String> subDomains = new HashSet();// The set is used to calculate company wise total show in grid below to asset and liability
            request.setAttribute("consolidationBS", true);
            request.setAttribute("isForTradingAndProfitLoss", true);
            JSONArray totalLeftArray = new JSONArray();
            JSONArray totalRightArray = new JSONArray();
            for (Map.Entry<String, JSONObject> entry : conslidationCompanyMap.entrySet()) {
                JSONObject consolidationCompanyDetail = entry.getValue();
                subDomains.add(consolidationCompanyDetail.getString("subdomainname"));
                request.setAttribute("consolidationCompanyID", consolidationCompanyDetail.get("subdomainid"));
//                JSONObject resOBject = accReportsService.getBalanceSheet(request).getJSONObject(Constants.RES_data);
                JSONObject pramObj = StringUtil.convertRequestToJsonObject(request);
                JSONObject resOBject = accReportsService.getBalanceSheetAllAccounts(pramObj,null).getJSONObject(Constants.RES_data);

                JSONArray jArrL = resOBject.getJSONArray("left");
                JSONArray jArrR = resOBject.getJSONArray("right");
                
                formatJSONArrayForConsolidationBS(jArrL, totalLeftArray, consolidationCompanyDetail);
                formatJSONArrayForConsolidationBS(jArrR, totalRightArray, consolidationCompanyDetail);
            }

            totalLeftArray = orderdedConsolidationBSDataArray(totalLeftArray, subDomains);
            totalRightArray = orderdedConsolidationBSDataArray(totalRightArray, subDomains);

            JSONArray totalArray = new JSONArray();
            JSONObject objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("accountname", "Assets");
            objlast.put("amount", "");
            objlast.put("fmt", "B");
            totalArray.put(objlast);

            for (int i = 0; i < totalRightArray.length(); i++) {
                totalArray.put(totalRightArray.getJSONObject(i));
            }

            objlast = new JSONObject();
            totalArray.put(objlast);
            objlast = new JSONObject();
            totalArray.put(objlast);// empty object added for blank lines between Asset and Liability

            objlast = new JSONObject();
            objlast.put("accountid", "");
            objlast.put("level", 0);
            objlast.put("isdebit", false);
            objlast.put("leaf", true);
            objlast.put("accountname", "Liabilities");
            objlast.put("amount", "");
            objlast.put("fmt", "B");
            totalArray.put(objlast);

            for (int i = 0; i < totalLeftArray.length(); i++) {
                totalArray.put(totalLeftArray.getJSONObject(i));
            }
            returnObj.put(Constants.RES_data, totalArray);
            returnObj.put(Constants.RES_count, totalArray.length());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getTradingAndProfitLoss : " + ex.getMessage(), ex);
        }
        return returnObj;
    }
    
    @Override
    public JSONObject getCustomConsolidationPNLReport(JSONObject paramJobj) throws ServiceException, SessionExpiredException {

        JSONObject returnObj = new JSONObject();
        Map<String, Object> requestParams = new HashMap();
        String companyid = paramJobj.optString(Constants.companyKey);
        String allCompanyids = "";

        try {
            Date startDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("stdate"));
            Date endDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("enddate"));
            Boolean isPNL = paramJobj.optBoolean("isConsolidatedPNL", false);
            requestParams.put("isConsolidatedPNL", isPNL);
            requestParams.put("startdate", startDate);
            requestParams.put("enddate", endDate);
            Map<String, JSONObject> conslidationCompanyMap = getConsolidationCompanyDetailMap(paramJobj);

            //Here we need a map which contain parent company data first, Because custom consolidation report will work on parent company
            Map<String, JSONObject> parentCompanyDataFirstMap = new LinkedHashMap<>();
            if (!conslidationCompanyMap.isEmpty()) {
                parentCompanyDataFirstMap.put(companyid, conslidationCompanyMap.get(companyid));//Parent Company Data putted first 
                allCompanyids += "'" + conslidationCompanyMap.get(companyid).optString("subdomainid") + "'";
                for (String childCompanyid : conslidationCompanyMap.keySet()) {
                    if (!childCompanyid.equals(companyid)) {//child company data only
                        parentCompanyDataFirstMap.put(childCompanyid, conslidationCompanyMap.get(childCompanyid));
                        allCompanyids += "," + "'" + conslidationCompanyMap.get(childCompanyid).optString("subdomainid") + "'";
                    }
                }
            }

            requestParams.put("companyid", companyid);
            requestParams.put("multiCompanyid", allCompanyids);

            List<Object[]> accList = accJournalEntryobj.getSumAmountForAccount(requestParams);
            int level = 0;

            HashMap<String, Object> filterParams = new HashMap<>();
            String templateid = paramJobj.optString("templateid", null);
            filterParams.put(Constants.companyKey, companyid);
            filterParams.put("templateid", templateid);
            filterParams.put("levelZeroFlag", true);

            JSONArray dataArray = new JSONArray();

            KwlReturnObject plresult = accAccountDAOobj.getCustomLayoutGroups(filterParams);
            List<LayoutGroup> list = plresult.getEntityList();
            for (LayoutGroup group : list) {
                if (group.getNature() == Constants.CUSTOM_LAYOUT_CLOSING_STOCK || group.getNature() == Constants.CUSTOM_LAYOUT_OPENING_STOCK) {
                    getOpeningClosingStockJsonForConsolidatedPNLReport(dataArray, group, allCompanyids.replaceAll("'", "").split(","), level, paramJobj, parentCompanyDataFirstMap);
                } else {
                    filterParams.put("groupid", group.getID());
                    filterParams.put("companyid", companyid);
                    getConsolidatedPNLJSON(dataArray, level, group, parentCompanyDataFirstMap, accList, filterParams);
                }
            }
            returnObj.put("data", dataArray);
        } catch (JSONException | SessionExpiredException | ParseException | ServiceException ex) {
            throw ServiceException.FAILURE("getCustomConsolidationPNLReport : " + ex.getMessage(), ex);
        }
        return returnObj;
    }
    
    
    public Map getConsolidatedPNLJSON(JSONArray dataArray, int level, LayoutGroup group,  Map <String, JSONObject> parentCompanyDataFirstMap , List<Object[]> accList, HashMap<String, Object> filterParams){
        JSONArray perGroup = new JSONArray();
        Map SubdomainwiseTotal = new HashMap();
        try {
            Set<LayoutGroup> childrenSet = group.getChildren();

            JSONObject groupObject = new JSONObject();
            groupObject.put("accountname", group.getName());
            groupObject.put("accountid", group.getID());
            groupObject.put("leaf", false);
            groupObject.put("level", level);
            groupObject.put("isgroupingflag", false);
            dataArray.put(groupObject);
            groupObject = null;
            filterParams.put("groupid", group.getID());
            double groupTotalAmount = 0;

            KwlReturnObject accresult = accAccountDAOobj.getAccountsForLayoutGroup(filterParams);
            List<GroupAccMap> list2 = accresult.getEntityList();
            for (GroupAccMap accMap : list2) {
                Account account = accMap.getAccount();
                JSONObject accountJSONObject = new JSONObject();
                Boolean putAccount = false;
                double accountTotal = 0;
                for (Object[] row : accList) {
                    String accountName = (String) row[2];

                    if (accountName.equalsIgnoreCase(account.getName())) {
                        putAccount = true;
                        String rowCompany = (String) row[1];
                        Double periodAmount = (Double) row[3];
                        JSONObject companyJson = parentCompanyDataFirstMap.get(rowCompany);
                        double stake = (companyJson.optDouble("stakeinpercentage")) / 100;
                        periodAmount *= stake;
                        accountJSONObject.put(companyJson.optString("subdomainname") + "_openingamount", 0);
                        accountJSONObject.put(companyJson.optString("subdomainname") + "_periodamount", periodAmount);
                        accountJSONObject.put(companyJson.optString("subdomainname") + "_endingamount", periodAmount);
                        accountTotal += periodAmount;
                        boolean isShowZeroAmountAsBlank = false;
                        ExtraCompanyPreferences extrapref = null;
                        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), (String) filterParams.get("companyid"));
                        if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                            extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                            isShowZeroAmountAsBlank = extrapref.isShowZeroAmountAsBlank();
                        }
                        if (isShowZeroAmountAsBlank) {
                            for (JSONObject companies : parentCompanyDataFirstMap.values()) {
                                for (int arraySize = 0; arraySize < dataArray.length(); arraySize++) {
                                    if (!accountJSONObject.has(companies.optString("subdomainname") + "_periodamount")) {
                                        accountJSONObject.put(companies.optString("subdomainname") + "_openingamount", 0);
                                        accountJSONObject.put(companies.optString("subdomainname") + "_periodamount", 0);
                                        accountJSONObject.put(companies.optString("subdomainname") + "_endingamount", 0);
                                    }
                                }
                            }
                        }
                    }
                }
                if (putAccount) {
                    accountJSONObject.put("accountname", account.getName());
                    accountJSONObject.put("accountid", account.getID());
                    accountJSONObject.put("leaf", true);
                    accountJSONObject.put("level", level + 1);
                    accountJSONObject.put("group", "");
                    accountJSONObject.put("totalopeningamount", 0);
                    accountJSONObject.put("totalperiodamount", accountTotal);
                    accountJSONObject.put("totalendingamount", accountTotal);
                    dataArray.put(accountJSONObject);
                    perGroup.put(accountJSONObject);
                }
                accountJSONObject = null;

            }

            JSONObject groupTotal = new JSONObject();

            for (JSONObject companies : parentCompanyDataFirstMap.values()) {
                double companyTotal = 0;
                for (int arraySize = 0; arraySize < perGroup.length(); arraySize++) {
                    companyTotal += perGroup.getJSONObject(arraySize).optDouble(companies.optString("subdomainname") + "_periodamount", 0);
                }
                groupTotalAmount += companyTotal;
                double[] totals = new double[3];
                groupTotal.put(companies.optString("subdomainname") + "_openingamount", 0);
                totals[0] = 0;
                groupTotal.put(companies.optString("subdomainname") + "_periodamount", companyTotal);
                totals[1] = companyTotal;
                groupTotal.put(companies.optString("subdomainname") + "_endingamount", companyTotal);
                totals[2] = companyTotal;
                SubdomainwiseTotal.put(companies.optString("subdomainid"), totals);
            }
            double[] totals = new double[3];
            groupTotal.put("totalopeningamount", 0);
            totals[0] = 0;
            groupTotal.put("totalperiodamount", groupTotalAmount);
            totals[1] = groupTotalAmount;
            groupTotal.put("totalendingamount", groupTotalAmount);
            totals[2] = groupTotalAmount;
            SubdomainwiseTotal.put("groupTotal", totals);
            groupTotal.put("accountname", "Total " + group.getName());
            groupTotal.put("leaf", true);
            groupTotal.put("level", level);
            groupTotal.put("accountid", "Toatal" + group.getID());
            groupTotal.put("isdebit", false);
            groupTotal.put("group", "");
            for (LayoutGroup subgroup : childrenSet) {
                // Recursive function used to calculate to handle sub groups and to do the sum of subgroups as well as groups containing them. 
                // This code can handle N number of sub groups
                Map previousTotal = getConsolidatedPNLJSON(dataArray, level + 1, subgroup, parentCompanyDataFirstMap, accList, filterParams);
                for (JSONObject companies : parentCompanyDataFirstMap.values()) {
                    double[] previousAdditions = (double[]) previousTotal.get(companies.optString("subdomainid"));
                    double[] totals1 = new double[3];
                    totals1[0] = groupTotal.optDouble(companies.optString("subdomainname") + "_openingamount", 0) + previousAdditions[0];
                    totals1[1] = groupTotal.optDouble(companies.optString("subdomainname") + "_periodamount", 0) + previousAdditions[1];
                    totals1[2] = groupTotal.optDouble(companies.optString("subdomainname") + "_endingamount", 0) + previousAdditions[2];
                    groupTotal.put(companies.optString("subdomainname") + "_openingamount", totals1[0]);
                    groupTotal.put(companies.optString("subdomainname") + "_periodamount", totals1[1]);
                    groupTotal.put(companies.optString("subdomainname") + "_endingamount", totals1[2]);
                    SubdomainwiseTotal.put(companies.optString("subdomainid"), totals1);
                }
                double[] previousAdditions = (double[]) previousTotal.get("groupTotal");
                double[] totals1 = new double[3];
                totals1[0] = groupTotal.getDouble("totalopeningamount") + previousAdditions[0];
                totals1[1] = groupTotal.getDouble("totalperiodamount") + previousAdditions[1];
                totals1[2] = groupTotal.getDouble("totalendingamount") + previousAdditions[2];
                groupTotal.put("totalopeningamount", (groupTotal.getDouble("totalopeningamount") + previousAdditions[0]));
                groupTotal.put("totalperiodamount", (groupTotal.getDouble("totalperiodamount") + previousAdditions[1]));
                groupTotal.put("totalendingamount", (groupTotal.getDouble("totalendingamount") + previousAdditions[2]));
                SubdomainwiseTotal.put("groupTotal", totals1);
            }

            dataArray.put(groupTotal);
            groupTotal = null;

        } catch (JSONException ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return SubdomainwiseTotal;
    }
    
    public void getOpeningClosingStockJsonForConsolidatedPNLReport(JSONArray dataArray, LayoutGroup group, String[] companyids, int level, JSONObject paramJobj, Map<String, JSONObject> parentCompanyDataFirstMap) {
        try {
            JSONObject groupObject = new JSONObject();
            groupObject.put("accountname", group.getName());
            groupObject.put("accountid", group.getID());
            groupObject.put("leaf", true);
            groupObject.put("level", level);
            groupObject.put("isgroupingflag", false);
            groupObject.put("group", "");
            double openingStockOpening = 0;
            double openingStockPeriod = 0;
            double openingStockClosing = 0;
            double closingStockOpening = 0;
            double closingStockPeriod = 0;
            double closingStockClosing = 0;
            double totalOpening = 0;
            double totalPeriod = 0;
            double totalending = 0;
            boolean isShowZeroAmountAsBlank = false;
            String companyid = paramJobj.optString("companyid", "");
            ExtraCompanyPreferences extrapref = null;
            if (!StringUtil.isNullOrEmptyWithTrim(companyid)) {
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                    extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                    isShowZeroAmountAsBlank = extrapref.isShowZeroAmountAsBlank();
                }
            }
            for (String companyiditr : companyids) {
                JSONObject companyJson = parentCompanyDataFirstMap.get(companyiditr);
                double stake = (companyJson.optDouble("stakeinpercentage")) / 100;
                HashMap<String, Object> requestParam = new HashMap<String, Object>();
                requestParam.put(Constants.REQ_startdate, paramJobj.optString("stdate"));
                requestParam.put(Constants.REQ_enddate, paramJobj.optString("enddate"));
                requestParam.put(Constants.df, authHandler.getDateOnlyFormat());
                requestParam.put(Constants.companyKey, companyiditr);
                double[] valuation = AccProductService.getInventoryValuationDataForFinancialReports(requestParam);
                if (group.getNature() == Constants.CUSTOM_LAYOUT_OPENING_STOCK) {
                    openingStockOpening = authHandler.round((valuation[0] * stake), companyiditr);
                    openingStockPeriod = authHandler.round((valuation[1] * stake), companyiditr);
                    openingStockClosing = authHandler.round((valuation[2] * stake), companyiditr);
                    groupObject.put(companyJson.optString("subdomainname") + "_openingamount", (openingStockOpening == 0 && isShowZeroAmountAsBlank) ? "" : openingStockOpening);
                    groupObject.put(companyJson.optString("subdomainname") + "_periodamount", (openingStockPeriod == 0 && isShowZeroAmountAsBlank) ? "" : openingStockPeriod);
                    groupObject.put(companyJson.optString("subdomainname") + "_endingamount", (openingStockClosing == 0 && isShowZeroAmountAsBlank) ? "" : openingStockClosing);
                    totalOpening += openingStockOpening;
                    totalPeriod += openingStockPeriod;
                    totalending += openingStockClosing;
                } else if (group.getNature() == Constants.CUSTOM_LAYOUT_CLOSING_STOCK) {
                    closingStockOpening = authHandler.round((valuation[3] * stake), companyiditr);
                    closingStockPeriod = authHandler.round((valuation[4] * stake), companyiditr);
                    closingStockClosing = authHandler.round(closingStockOpening, companyiditr) + authHandler.round(closingStockPeriod, companyiditr);
                    groupObject.put(companyJson.optString("subdomainname") + "_openingamount", (closingStockOpening == 0 && isShowZeroAmountAsBlank) ? "" : closingStockOpening);
                    groupObject.put(companyJson.optString("subdomainname") + "_periodamount", (closingStockPeriod == 0 && isShowZeroAmountAsBlank) ? "" : closingStockPeriod);
                    groupObject.put(companyJson.optString("subdomainname") + "_endingamount", (closingStockClosing == 0 && isShowZeroAmountAsBlank) ? "" : closingStockClosing);
                    totalOpening += closingStockOpening;
                    totalPeriod += closingStockPeriod;
                    totalending += closingStockClosing;
                }
            }
            groupObject.put("totalopeningamount", (totalOpening == 0 && isShowZeroAmountAsBlank) ? "" : totalOpening);
            groupObject.put("totalperiodamount", (totalPeriod == 0 && isShowZeroAmountAsBlank) ? "" : totalPeriod);
            groupObject.put("totalendingamount", (totalending == 0 && isShowZeroAmountAsBlank) ? "" : totalending);
            dataArray.put(groupObject);
        } catch (JSONException | SessionExpiredException | ServiceException ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public JSONObject getCustomConsolidationBalanceSheetReport(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject returnObj = new JSONObject();
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            String templateid = paramJobj.optString("templateid", null);
            Set<String> subDomains = new HashSet();
            Date startDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("stdate"));
            Date endDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("enddate"));
            boolean isBalanceSheet=paramJobj.optBoolean("isBalanceSheet",false);

            Map<String, JSONObject> conslidationCompanyMap = getConsolidationCompanyDetailMap(paramJobj);

            //Here we need a map which contain parent company data first, Because custom consolidation report will work on parent company
            LinkedHashMap<String, JSONObject> parentCompanyDataFirstMap = new LinkedHashMap<>();
            if (!conslidationCompanyMap.isEmpty()) {
                parentCompanyDataFirstMap.put(companyid, conslidationCompanyMap.get(companyid));//Parent Company Data putted first 
                for (String childCompanyid : conslidationCompanyMap.keySet()) {
                    if (!childCompanyid.equals(companyid)) {//child company data only
                        parentCompanyDataFirstMap.put(childCompanyid, conslidationCompanyMap.get(childCompanyid));
                    }
                }
            }


            HashMap<String, Object> filterParams = new HashMap<>();
            filterParams.put(Constants.companyKey, companyid);
            filterParams.put("templateid", templateid);
            filterParams.put("levelZeroFlag", true);
            KwlReturnObject plresult = accAccountDAOobj.getCustomLayoutGroups(filterParams);
            List<LayoutGroup> list = plresult.getEntityList();

            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            }
            if (extrapref != null) {
                paramJobj.put("stockValuationFlag", extrapref.isStockValuationFlag());
            }

            paramJobj.put("isConsolidationReport", true);
            JSONArray totalJArr = new JSONArray();
            Map<String, Object> advSearchAttributes = null;
            //Loop For geeting Every Company Data
            for (Map.Entry<String, JSONObject> entry : parentCompanyDataFirstMap.entrySet()) {
                JSONObject consolidationCompanyDetail = entry.getValue();

                String subdomain = consolidationCompanyDetail.getString("subdomainname");
                String subdomainid = consolidationCompanyDetail.getString("subdomainid");
                double[] total = {0, 0, 0, 0, 0, 0, 0, 0};
                Map<String, double[]> groupTotalMap = new HashMap<>();
                JSONArray jArr = new JSONArray();

                subDomains.add(subdomain);
                paramJobj.put(Constants.companyKey, subdomainid);

                for (LayoutGroup group : list) {
                    double[] tempTotal = formatLayoutGroupDetails(paramJobj, subdomainid, group, startDate, endDate, 0, isBalanceSheet, jArr, null, null, groupTotalMap, advSearchAttributes, null);
                    total[0] += tempTotal[0];
                    total[1] += tempTotal[1];
                    total[2] += tempTotal[6];
                    total[3] += tempTotal[7];
                    total[4] += tempTotal[8];
                    total[5] += tempTotal[9];
                    total[6] += tempTotal[10];
                    total[7] += tempTotal[11];
                }
                boolean isParentCompany=false;
                if(subdomainid.equalsIgnoreCase(companyid)){
                    isParentCompany = true;
                }
                formatJSONArrayForCustomConsolidationBS(jArr, totalJArr, consolidationCompanyDetail,isParentCompany);
            }
            returnObj.put(Constants.data, totalJArr);
        } catch (JSONException | SessionExpiredException | ParseException | ServiceException ex) {
            throw ServiceException.FAILURE("getCustomConsolidationBalanceSheetReport : " + ex.getMessage(), ex);
        }
        return returnObj;
    }

    private void formatJSONArrayForCustomConsolidationBS(JSONArray eachComapnyArray, JSONArray totalArray, JSONObject consolidationCompanyDetail, boolean isParentCompany) throws JSONException {
        String subdomain = consolidationCompanyDetail.getString("subdomainname");
        double exchangeRate = consolidationCompanyDetail.optDouble("exchangerate", 0);
        double stakeInPercentage = consolidationCompanyDetail.optDouble("stakeinpercentage", 0);
        for (int i = 0; i < eachComapnyArray.length(); i++) {// for each company data loop
            JSONObject eachCompanyBSObj = eachComapnyArray.getJSONObject(i);
            if (eachCompanyBSObj.has("accountname")) {
                boolean accountFound = false;
                String accName = eachCompanyBSObj.getString("accountname");
                double openingAmt = eachCompanyBSObj.optDouble("openingamount", 0);
                double periodAmt = eachCompanyBSObj.optDouble("periodamount", 0);
                boolean isgroupflag = eachCompanyBSObj.optBoolean("isgroupflag",false);

                if (openingAmt != 0) {
                    openingAmt = openingAmt * (stakeInPercentage / 100) * exchangeRate;
                }
                if (periodAmt != 0) {
                    periodAmt = periodAmt * (stakeInPercentage / 100) * exchangeRate;
                }
                double endingAmt = openingAmt + periodAmt;

                for (int j = 0; j < totalArray.length(); j++) {
                    JSONObject allCompanyBSObj = totalArray.getJSONObject(j);
                    if (allCompanyBSObj.has("accountname") && allCompanyBSObj.getString("accountname").equalsIgnoreCase(accName)) {// If account Already present then need to udate record otherwise need to add
                        accountFound = true;
                        if (!isgroupflag) {
                            //Putting opening,Period and ending amount gainst each company
                            totalArray.getJSONObject(j).put(subdomain + "_openingamount", openingAmt);
                            totalArray.getJSONObject(j).put(subdomain + "_periodamount", periodAmt);
                            totalArray.getJSONObject(j).put(subdomain + "_endingamount", endingAmt);

                            //Putting Total opening,Period and ending amount for all company
                            totalArray.getJSONObject(j).put("totalopeningamount", allCompanyBSObj.optDouble("totalopeningamount", 0) + openingAmt);
                            totalArray.getJSONObject(j).put("totalperiodamount", allCompanyBSObj.optDouble("totalperiodamount", 0) + periodAmt);
                            totalArray.getJSONObject(j).put("totalendingamount", allCompanyBSObj.optDouble("totalendingamount", 0) + endingAmt);
                        }
                        break;
                    }
                }
                if (!accountFound && isParentCompany) {// If account not found in totalArray and company is parent then then adding eachCompanyBSObj to  totalArray
                    if (!isgroupflag) {
                        //Putting opening,Period and ending amount against each company
                        eachCompanyBSObj.put(subdomain + "_openingamount", openingAmt);
                        eachCompanyBSObj.put(subdomain + "_periodamount", periodAmt);
                        eachCompanyBSObj.put(subdomain + "_endingamount", endingAmt);

                        //Putting Total opening,Period and ending amount for all company
                        eachCompanyBSObj.put("totalopeningamount", openingAmt);
                        eachCompanyBSObj.put("totalperiodamount", periodAmt);
                        eachCompanyBSObj.put("totalendingamount", endingAmt);
                    }
                    totalArray.put(eachCompanyBSObj);
                }
            }
        }
    }
    
    private JSONArray orderdedConsolidationBSDataArray(JSONArray consolidationBSDataArray, Set<String> subDomains) throws JSONException {
        JSONArray orderdedArray = new JSONArray();

        JSONArray groupArrays = new JSONArray();
        JSONArray accountsArrays = new JSONArray();
        JSONArray groupTotalArrays = new JSONArray();
        JSONArray labelArrays = new JSONArray();

        for (int i = 0; i < consolidationBSDataArray.length(); i++) {
            JSONObject object = consolidationBSDataArray.getJSONObject(i);
            if (object.has("accountflag") && object.optBoolean("accountflag", false)) {//
                if (object.getString("accountname").equals(Constants.Difference_in_Opening_balances)) {// Diffreence in opening balance is a label but for it accountflag is coming as true hence need to handle this code
                    labelArrays.put(object);
                } else {
                    accountsArrays.put(object);
                }
            } else if (object.has("isgroupflag") && object.optBoolean("isgroupflag", false)) {
                groupArrays.put(object);
            } else if (object.has("GroupTotal") && object.optBoolean("GroupTotal", false)) {
                groupTotalArrays.put(object);
            } else {
                labelArrays.put(object);
            }
        }

        accountsArrays = sortJsonArrayOnAccountName(accountsArrays);
        groupArrays = sortJsonArrayOnAccountName(groupArrays);

        for (int i = 0; i < groupArrays.length(); i++) {
            JSONObject object = groupArrays.getJSONObject(i);
            orderdedArray.put(object);
            String groupname = object.getString("accountname");
            for (int j = 0; j < accountsArrays.length(); j++) {
                JSONObject accObj = accountsArrays.getJSONObject(j);
                if (accObj.has("nameofaccountgroup") && accObj.optString("nameofaccountgroup", "").equals(groupname)) {
                    orderdedArray.put(accObj);
                }
            }
            for (int j = 0; j < groupTotalArrays.length(); j++) {
                JSONObject grTotalObj = groupTotalArrays.getJSONObject(j);
                if (grTotalObj.has("accountname") && grTotalObj.optString("accountname", "").equals("Total for " + groupname)) {
                    orderdedArray.put(grTotalObj);
                }
            }
        }
        for (int i = 0; i < labelArrays.length(); i++) {
            orderdedArray.put(labelArrays.getJSONObject(i));
        }

        JSONObject objlast = new JSONObject();
        objlast.put("accountid", "");
        objlast.put("level", 0);
        objlast.put("isdebit", false);
        objlast.put("leaf", true);
        objlast.put("accountname", "Total");
        objlast.put("amount", "");
        objlast.put("fmt", "T");

        // Calculating  company wise total show in grid below to asset and liability 
        for (String subdomain : subDomains) {
            double companyOpening = 0;
            double companyPeriod = 0;
            double companyEnding = 0;
            for (int j = 0; j < groupTotalArrays.length(); j++) {
                JSONObject grTotalObj = groupTotalArrays.getJSONObject(j);
                companyOpening += grTotalObj.optDouble(subdomain + "_openingamount", 0);
                companyPeriod += grTotalObj.optDouble(subdomain + "_periodamount", 0);
                companyEnding += grTotalObj.optDouble(subdomain + "_endingamount", 0);
            }
            for (int j = 0; j < labelArrays.length(); j++) {
                JSONObject labelObj = labelArrays.getJSONObject(j);
                companyOpening += labelObj.optDouble(subdomain + "_openingamount", 0);
                companyPeriod += labelObj.optDouble(subdomain + "_periodamount", 0);
                companyEnding += labelObj.optDouble(subdomain + "_endingamount", 0);
            }
            if (companyOpening != 0 || companyPeriod != 0 || companyEnding != 0) {
                objlast.put(subdomain + "_openingamount", companyOpening);
                objlast.put(subdomain + "_periodamount", companyPeriod);
                objlast.put(subdomain + "_endingamount", companyEnding);
            } else {
                objlast.put(subdomain + "_openingamount", "");
                objlast.put(subdomain + "_periodamount", "");
                objlast.put(subdomain + "_endingamount", "");
            }
        }
        orderdedArray.put(objlast);
        return orderdedArray;
    }

    private static JSONArray sortJsonArrayOnAccountName(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    String lid = "", rid = "";
                    try {
                        lid = lhs.getString("accountname");
                        rid = rhs.getString("accountname");
                    } catch (JSONException ex) {
                        Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return lid.compareTo(rid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new JSONArray(jsons);
    }

    private void formatJSONArrayForConsolidationBS(JSONArray eachComapnyArray, JSONArray totalArray, JSONObject consolidationCompanyDetail) throws JSONException {
        String subdomain = consolidationCompanyDetail.getString("subdomainname");
        double exchangeRate = consolidationCompanyDetail.optDouble("exchangerate", 0);
        double stakeInPercentage = consolidationCompanyDetail.optDouble("stakeinpercentage", 0);
        for (int i = 0; i < eachComapnyArray.length(); i++) {// for each company data loop
            JSONObject eachCompanyBSObj = eachComapnyArray.getJSONObject(i);
            if (eachCompanyBSObj.has("accountname")) {
                boolean accountFound = false;
                String accName = eachCompanyBSObj.getString("accountname");
                double openingAmt = eachCompanyBSObj.optDouble("openingamount", 0);
                double periodAmt = eachCompanyBSObj.optDouble("periodamount", 0);

                if (openingAmt != 0) {
                    openingAmt = openingAmt * (stakeInPercentage / 100) * exchangeRate;
                }
                if (periodAmt != 0) {
                    periodAmt = periodAmt * (stakeInPercentage / 100) * exchangeRate;
                }
                double endingAmt = openingAmt + periodAmt;

                for (int j = 0; j < totalArray.length(); j++) {
                    JSONObject allCompanyBSObj = totalArray.getJSONObject(j);
                    if (allCompanyBSObj.has("accountname") && allCompanyBSObj.getString("accountname").equals(accName)) {// If account Already present then need to udate record otherwise need to add
                        accountFound = true;
                        if (openingAmt != 0 || periodAmt != 0 || endingAmt != 0) {
                            //Putting opening,Period and ending amount gainst each company
                            totalArray.getJSONObject(j).put(subdomain + "_openingamount", openingAmt);
                            totalArray.getJSONObject(j).put(subdomain + "_periodamount", periodAmt);
                            totalArray.getJSONObject(j).put(subdomain + "_endingamount", endingAmt);

                            //Putting Total opening,Period and ending amount for all company
                            totalArray.getJSONObject(j).put("totalopeningamount", allCompanyBSObj.optDouble("totalopeningamount", 0) + openingAmt);
                            totalArray.getJSONObject(j).put("totalperiodamount", allCompanyBSObj.optDouble("totalperiodamount", 0) + periodAmt);
                            totalArray.getJSONObject(j).put("totalendingamount", allCompanyBSObj.optDouble("totalendingamount", 0) + endingAmt);
                        }
                        break;
                    }
                }
                if (!accountFound) {// If account not found in all company array then adding it
                    if (openingAmt != 0 || periodAmt != 0 || endingAmt != 0) {

                        //Putting opening,Period and ending amount gainst each company
                        eachCompanyBSObj.put(subdomain + "_openingamount", openingAmt);
                        eachCompanyBSObj.put(subdomain + "_periodamount", periodAmt);
                        eachCompanyBSObj.put(subdomain + "_endingamount", endingAmt);

                        //Putting Total opening,Period and ending amount for all company
                        eachCompanyBSObj.put("totalopeningamount", openingAmt);
                        eachCompanyBSObj.put("totalperiodamount", periodAmt);
                        eachCompanyBSObj.put("totalendingamount", endingAmt);
                    }
                    totalArray.put(eachCompanyBSObj);
                }
            }
        }

    }

    /**
     *
     * @param requestParams
     * @return SearchJson for advanced search based on Multi Entity for SI,PI,JE,MP,RP,DN,CN,DO
     * @throws ServiceException
     * @throws SessionExpiredException
     */
    @Override
    public HashMap<String, Object> getAdvanceSearchModuleFieldParams(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        try {
            String Searchjson = "";
            String invoiceSearchJson = "";
            String purchaseInvoiceSearchJson = "";
            String debitNoteSearchJson = "";
            String creditNoteSearchJson = "";
            String makePaymentSearchJson = "";
            String receivePaymentSearchJson = "";
            String journalEntrySearchJson = "";
            String deliveryOrderSearchJson = "";
            String fixedAssetsPurchaseInvoiceSearchJson = "";
            String fixedAssetsDisposalInvoiceSearchJson = "";
            String filterConjuctionCriteria = Constants.and;
            boolean isAdvanceSearch = false;

            if (requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty(requestParams.get(Constants.Acc_Search_Json).toString())) {
                Searchjson = (String) requestParams.get(Constants.Acc_Search_Json);
            }
            if (requestParams.containsKey(Constants.Filter_Criteria) && !StringUtil.isNullOrEmpty(requestParams.get(Constants.Filter_Criteria).toString())) {
                filterConjuctionCriteria = (String) requestParams.get(Constants.Filter_Criteria);
            }
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                isAdvanceSearch = true;
                HashMap<String, Object> reqPar1 = new HashMap<>();
                reqPar1.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);

                reqPar1.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                invoiceSearchJson = accReportsService.getSearchJsonByModule(reqPar1);//return SearchJson for customer invoice
                requestParams.put(Constants.invoiceSearchJson, invoiceSearchJson);

                reqPar1.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                purchaseInvoiceSearchJson = accReportsService.getSearchJsonByModule(reqPar1);//return SearchJson for vendor invoice
                requestParams.put(Constants.purchaseInvoiceSearchJson, purchaseInvoiceSearchJson);

                reqPar1.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                debitNoteSearchJson = accReportsService.getSearchJsonByModule(reqPar1);//return SearchJson for Debit Note
                requestParams.put(Constants.debitNoteSearchJson, debitNoteSearchJson);

                reqPar1.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                creditNoteSearchJson = accReportsService.getSearchJsonByModule(reqPar1);//return SearchJson for Credit Note
                requestParams.put(Constants.creditNoteSearchJson, creditNoteSearchJson);
                
                reqPar1.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                makePaymentSearchJson = accReportsService.getSearchJsonByModule(reqPar1);//return SearchJson for Make Payment
                requestParams.put(Constants.makePaymentSearchJson, makePaymentSearchJson);
                
                reqPar1.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                receivePaymentSearchJson = accReportsService.getSearchJsonByModule(reqPar1);//return SearchJson for Receive Payment
                requestParams.put(Constants.receivePaymentSearchJson, receivePaymentSearchJson);

                reqPar1.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                journalEntrySearchJson = accReportsService.getSearchJsonByModule(reqPar1);//return SearchJson for GL
                requestParams.put(Constants.journalEntrySearchJson, journalEntrySearchJson);
                
                reqPar1.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                deliveryOrderSearchJson = accReportsService.getSearchJsonByModule(reqPar1);//return SearchJson for Delivery Order
                requestParams.put(Constants.deliveryOrderSearchJson, deliveryOrderSearchJson);
                
                reqPar1.put(Constants.moduleid, Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId);
                fixedAssetsPurchaseInvoiceSearchJson = accReportsService.getSearchJsonByModule(reqPar1);//return SearchJson for Acc_FixedAssets_PurchaseInvoice_ModuleId
                requestParams.put(Constants.fixedAssetsPurchaseInvoiceSearchJson, fixedAssetsPurchaseInvoiceSearchJson);

                reqPar1.put(Constants.moduleid, Constants.Acc_FixedAssets_DisposalInvoice_ModuleId);
                fixedAssetsDisposalInvoiceSearchJson = accReportsService.getSearchJsonByModule(reqPar1);//return SearchJson for Acc_FixedAssets_DisposalInvoice_ModuleId
                requestParams.put(Constants.fixedAssetsDisposalInvoiceSearchJson, fixedAssetsDisposalInvoiceSearchJson);
                
                requestParams.put(Constants.isAdvanceSearch, isAdvanceSearch);//Added advanceSearch true 
            }
        } catch (SessionExpiredException | UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("getAdvanceSearchModuleFieldParams : " + ex.getMessage(), ex);
        }
        return requestParams;
    }
    
    @Override
    public JSONObject getExportBalanceSheetJSON(HttpServletRequest request, JSONObject jobj, int flag, int toggle, boolean periodView) throws ServiceException{
        JSONObject retObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        JSONArray rightObjArr = new JSONArray();
        JSONArray leftObjArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            jobj = jobj.getJSONObject(Constants.RES_data);
            if (toggle == 0) {
                rightObjArr = jobj.getJSONArray("right");
                leftObjArr = jobj.getJSONArray("left");
            } else {
                rightObjArr = jobj.getJSONArray("left");
                leftObjArr = jobj.getJSONArray("right");
            }
            int length = leftObjArr.length() > rightObjArr.length() ? leftObjArr.length() : rightObjArr.length();
            for (int i = 0; i < length; i++) {
                JSONObject tempObj = new JSONObject();
                if (i < leftObjArr.length() && !leftObjArr.getJSONObject(i).toString().equalsIgnoreCase("{}")) {
                    JSONObject leftObj = leftObjArr.getJSONObject(i);
                    if (periodView) {
                        tempObj.put("laccountname", leftObj.get("accountname"));
                        tempObj.put("laccountcode", leftObj.optString("accountcode"));
                        tempObj.put("laccountid", leftObj.get("accountid"));
                        tempObj.put("llevel", leftObj.get("level"));
                        tempObj.put("lisdebit", leftObj.get("isdebit"));
                        tempObj.put("lleaf", leftObj.get("leaf"));
                        tempObj.put("lpreamount", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.has("preamount") ? leftObj.get("preamount").toString() : ""));
                        tempObj.put("lopeningamount", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.has("openingamount") ? leftObj.get("openingamount").toString() : ""));
                        tempObj.put("lperiodamount", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.has("periodamount") ? leftObj.get("periodamount").toString() : ""));
                        tempObj.put("lendingamount", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.has("endingamount") ? leftObj.get("endingamount").toString() : ""));
                    } else {
                        tempObj.put("laccountname", leftObj.get("accountname"));
                        tempObj.put("laccountcode", leftObj.optString("accountcode"));
                        tempObj.put("laccountid", leftObj.get("accountid"));
                        tempObj.put("llevel", leftObj.get("level"));
                        tempObj.put("lisdebit", leftObj.get("isdebit"));
                        tempObj.put("lleaf", leftObj.get("leaf"));
                        String lamount = !StringUtil.isNullOrEmpty(leftObj.has("amount") ? leftObj.get("amount").toString() : "") ? authHandler.formattedAmount((Double)leftObj.get("amount"), companyid) : "";
                        tempObj.put("lamount", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.has("amount") ? lamount : ""));
                        tempObj.put("lamountInSelectedCurrency", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.has("amountInSelectedCurrency") ? leftObj.get("amountInSelectedCurrency").toString() : ""));
                        tempObj.put("lpreamount", com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.has("preamount") ? leftObj.get("preamount").toString() : ""));/*
                         * tempObj.put("lfmt",leftObj.get("fmt"));
                         */
                    }
                } else {
                    if (periodView) {
                        tempObj.put("laccountname", "");
                        tempObj.put("laccountcode","");
                        tempObj.put("laccountid", "");
                        tempObj.put("llevel", "");
                        tempObj.put("lisdebit", "");
                        tempObj.put("lleaf", "");
                        tempObj.put("lpreamount", "");
                        tempObj.put("lopeningamount", "");
                        tempObj.put("lperiodamount", "");
                        tempObj.put("lendingamount", "");
                    } else {
                        tempObj.put("laccountname", "");
                        tempObj.put("laccountcode","");
                        tempObj.put("laccountid", "");
                        tempObj.put("llevel", "");
                        tempObj.put("lisdebit", "");
                        tempObj.put("lleaf", "");
                        tempObj.put("lamount", "");
                        tempObj.put("lamountInSelectedCurrency", "");
                        tempObj.put("lpreamount", "");
                    }
                }
                if (i < rightObjArr.length() && !rightObjArr.getJSONObject(i).toString().equalsIgnoreCase("{}")) {
                    JSONObject rightObj = rightObjArr.getJSONObject(i);
                    if (periodView) {
                        tempObj.put("raccountname", rightObj.get("accountname"));
                        tempObj.put("raccountcode", rightObj.optString("accountcode"));
                        tempObj.put("raccountid", rightObj.get("accountid"));
                        tempObj.put("rlevel", rightObj.get("level"));
                        tempObj.put("risdebit", rightObj.get("isdebit"));
                        tempObj.put("rleaf", rightObj.get("leaf"));
                        tempObj.put("ropeningamount", com.krawler.common.util.StringUtil.serverHTMLStripper((rightObj.has("openingamount") ? rightObj.get("openingamount").toString() : "")));
                        tempObj.put("rperiodamount", com.krawler.common.util.StringUtil.serverHTMLStripper((rightObj.has("periodamount") ? rightObj.get("periodamount").toString() : "")));
                        tempObj.put("rendingamount", com.krawler.common.util.StringUtil.serverHTMLStripper((rightObj.has("endingamount") ? rightObj.get("endingamount").toString() : "")));
                    } else {
                        tempObj.put("raccountname", rightObj.get("accountname"));
                        tempObj.put("raccountcode", rightObj.optString("accountcode"));
                        tempObj.put("raccountid", rightObj.get("accountid"));
                        tempObj.put("rlevel", rightObj.get("level"));
                        tempObj.put("risdebit", rightObj.get("isdebit"));
                        tempObj.put("rleaf", rightObj.get("leaf"));
                        String ramount = !StringUtil.isNullOrEmpty(rightObj.has("amount") ? rightObj.get("amount").toString() : "") ? authHandler.formattedAmount((Double)rightObj.get("amount"), companyid) : "";
                        tempObj.put("ramount", com.krawler.common.util.StringUtil.serverHTMLStripper(rightObj.has("amount") ? ramount : ""));
                        tempObj.put("ramountInSelectedCurrency", com.krawler.common.util.StringUtil.serverHTMLStripper(rightObj.has("amountInSelectedCurrency") ? rightObj.get("amountInSelectedCurrency").toString() : ""));
                        tempObj.put("rpreamount", com.krawler.common.util.StringUtil.serverHTMLStripper((rightObj.has("preamount") ? rightObj.get("preamount").toString() : "")));/*
                         * tempObj.put("rfmt",rightObj.get("fmt"));
                         */
                    }
                } else {
                    if (periodView) {
                        tempObj.put("raccountname", "");
                        tempObj.put("raccountcode","");
                        tempObj.put("raccountid", "");
                        tempObj.put("rlevel", "");
                        tempObj.put("risdebit", "");
                        tempObj.put("rleaf", "");
                        tempObj.put("ropeningamount", "");
                        tempObj.put("rperiodamount", "");
                        tempObj.put("rendingamount", "");
                    } else {
                        tempObj.put("raccountname", "");
                        tempObj.put("raccountcode","");
                        tempObj.put("raccountid", "");
                        tempObj.put("rlevel", "");
                        tempObj.put("risdebit", "");
                        tempObj.put("rleaf", "");
                        tempObj.put("ramount", "");
                        tempObj.put("ramountInSelectedCurrency", "");
                        tempObj.put("rpreamount", "");
                    }
                }
                jArr.put(tempObj);
            }
            if (flag != -1) {
                double externalCurrencyRate = request.getAttribute("externalCurrencyRate") != null ? (Double) request.getAttribute("externalCurrencyRate") : 1.0;
                double totalAsset = 0, totalLibility = 0, totalOpenAsset = 0, totalOpenLiability = 0, totalEndAsset = 0, totalEndLiability = 0;
                double pretotalAsset = 0, pretotalLibility = 0;
                JSONArray finalValArr = periodView ? jobj.getJSONArray("periodtotal") : jobj.getJSONArray("total");
                JSONArray prefinalValArr = jobj.getJSONArray("pretotal");
                if (flag != 2) {
                    /*
                     * 0 index in openValArray,endValArray is for Liability 
                     * 1 index in openValArray,endValArray is for Asset
                     */
                    JSONArray openValArray = jobj.getJSONArray("opentotal");
                    JSONArray endValArray = jobj.getJSONArray("endtotal");
                    totalOpenAsset = Double.parseDouble(openValArray.getString(1));
                    totalOpenLiability = Double.parseDouble(openValArray.getString(0));
                    totalEndAsset = Double.parseDouble(endValArray.getString(1));
                    totalEndLiability = Double.parseDouble(endValArray.getString(0));
                    pretotalAsset = Double.parseDouble(prefinalValArr.getString(1));
                    pretotalLibility = Double.parseDouble(prefinalValArr.getString(0));
                    totalAsset = Double.parseDouble(finalValArr.getString(1));
                    totalLibility = Double.parseDouble(finalValArr.getString(0));
                } else {
                    pretotalAsset = Double.parseDouble(prefinalValArr.getString(0));
                    pretotalLibility = Double.parseDouble(prefinalValArr.getString(1));
                    totalAsset = Double.parseDouble(finalValArr.getString(0));
                    totalLibility = Double.parseDouble(finalValArr.getString(1));
                }

                String leftSummaryHeader = "", rightSummaryHeader = "";
                double leftOpen = totalOpenAsset, leftPeriod = totalAsset, leftEnd = totalEndAsset,leftPreTotal = pretotalAsset;
                double rightOpen = totalOpenLiability, rightPeriod = totalLibility, rightEnd = totalEndLiability,rightPreTotal = pretotalLibility;
                if (flag == 1) {
                    if (toggle == 0) {
                        //assigning values as per toggle condition.
                        leftSummaryHeader = "Total Liability";
                        leftOpen = totalOpenLiability;
                        leftPeriod = totalLibility;
                        leftEnd = totalEndLiability;
                        leftPreTotal = pretotalLibility;
                        
                        rightSummaryHeader = "Total Asset";
                        rightOpen = totalOpenAsset;
                        rightPeriod = totalAsset;
                        rightEnd = totalEndAsset;
                        rightPreTotal = pretotalAsset;
                    } else {
                        leftSummaryHeader = "Total Asset";
                        rightSummaryHeader = "Total Liability";
                    }
                } else if (flag == 2) {
                    leftSummaryHeader = "Total Debit";
                    rightSummaryHeader = "Total Credit";
                }

                if (periodView) {
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("laccountname", leftSummaryHeader);
                    tempObj.put("laccountid", "");
                    tempObj.put("llevel", "");
                    tempObj.put("lisdebit", "");
                    tempObj.put("lleaf", "");
                    tempObj.put("lopeningamount", leftOpen);
                    tempObj.put("lperiodamount", leftPeriod);
                    tempObj.put("lendingamount", leftEnd);

                    tempObj.put("raccountname", rightSummaryHeader);
                    tempObj.put("raccountid", "");
                    tempObj.put("rlevel", "");
                    tempObj.put("risdebit", "");
                    tempObj.put("rleaf", "");
                    tempObj.put("ropeningamount", rightOpen);
                    tempObj.put("rperiodamount", rightPeriod);
                    tempObj.put("rendingamount", rightEnd);
                    jArr.put(tempObj);
                } else {
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("laccountname", leftSummaryHeader);
                    tempObj.put("laccountid", "");
                    tempObj.put("llevel", "");
                    tempObj.put("lisdebit", "");
                    tempObj.put("lleaf", "");
                    tempObj.put("lamount", leftPeriod);
                    tempObj.put("lamountInSelectedCurrency", leftPeriod * externalCurrencyRate);
                    tempObj.put("lpreamount", leftPreTotal);

                    tempObj.put("raccountname", rightSummaryHeader);
                    tempObj.put("raccountid", "");
                    tempObj.put("rlevel", "");
                    tempObj.put("risdebit", "");
                    tempObj.put("rleaf", "");
                    tempObj.put("ramount", rightPeriod);
                    tempObj.put("ramountInSelectedCurrency", rightPeriod * externalCurrencyRate);
                    tempObj.put("rpreamount", rightPreTotal);
                    jArr.put(tempObj);
                }
            }
            retObj.put(Constants.RES_data, jArr);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("getMonthlyBalanceSheetforExport : " + ex.getMessage(), ex);
        }
        return retObj;
    }
    
 @Override   
      public JSONObject getMonthlyBalanceSheetforExport(HttpServletRequest request, boolean monthYearDate) throws ServiceException, SessionExpiredException {
        JSONObject jobj1 = new JSONObject();
        try {
            CompanyAccountPreferences pref = null;
            KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            if (!pref.isShowchild()) {// Anup Check to hide child customer / vendors from balance sheet
                request.setAttribute("hidechildCV", true);
            }
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            }
            JSONObject jArrL = new JSONObject();
            JSONObject jArrR = new JSONObject();
            JSONArray temprightObjArr1 = new JSONArray();
            JSONArray templeftObjArr = new JSONArray();
            LocalDate localStartDate = null;
            LocalDate localEndDate = null;
            Date endDate = null;
            Date startDate = null;
            if (monthYearDate) {
                final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
                localStartDate = dtf.parseLocalDate(request.getParameter("stdate"));
                localEndDate = dtf.parseLocalDate(request.getParameter("enddate"));
                startDate = localStartDate.toDate();
                endDate = localEndDate.toDate();
            } else {
                endDate = authHandler.getDateOnlyFormat().parse(request.getParameter("enddate"));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                startDate = authHandler.getDateOnlyFormat().parse(request.getParameter("stdate"));
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(startDate);
                localStartDate = new LocalDate(startDate);
                localEndDate = new LocalDate(endDate);
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int startMonth = cal.get(Calendar.MONTH);
            int startYear = cal.get(Calendar.YEAR);
            cal.setTime(endDate);
            int endMonth = cal.get(Calendar.MONTH);
            int endYear = cal.get(Calendar.YEAR);
            boolean isOneMonth = false;
            int monthCount = 0;
            // if user selects same month & year for both start & end fields, we get the last date of the month to populate into the endDate
            if (startMonth == endMonth && startYear == endYear) {
                DateTime date = localEndDate.toDateTime(LocalTime.MIDNIGHT);
                date = date.plusSeconds(86399);
                endDate = date.dayOfMonth().withMaximumValue().toDate();
                isOneMonth = true;
                localEndDate = new LocalDate(endDate);
                monthCount = 1;
            }

            if (!isOneMonth) { // just a trick to include the last month as well
                localEndDate = localEndDate.plus(Period.months(1));
            }

            while (localStartDate.isBefore(localEndDate)) {
                localStartDate = localStartDate.plus(Period.months(1));
                monthCount++;
            }

            // GOING TO CALCULATE THE MONTHLY OPEN & CLOSING BALANCE - START
            if (monthYearDate) {
                final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
                localStartDate = dtf.parseLocalDate(request.getParameter("stdate"));
                localEndDate = dtf.parseLocalDate(request.getParameter("enddate"));
            } else {
                localStartDate = new LocalDate(startDate);
                localEndDate = new LocalDate(endDate);
            }
            int monthIndex = 0;
            if (!isOneMonth) { // just a trick to include the last month as well
                localEndDate = localEndDate.plus(Period.months(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }
            
            /**
             * When from date and to date are equal i.e. single month is selected then substract 2 from monthcount
             */
            int monthsToSubstract = isOneMonth ? 2 : 1;

            while (localStartDate.isBefore(localEndDate) && (monthIndex <= (monthCount - 2) && !monthYearDate) || (monthIndex <= (monthCount - monthsToSubstract) && monthYearDate)) {
                DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                Date startDate1;
                if (monthIndex == 0) {
                    if (!monthYearDate) {
                        startDate1 = authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
                    } else {
                        Date tempDate = localStartDate.toDate();
                        Calendar cal1 = Calendar.getInstance();
                        Date d = new Date();
                        SimpleDateFormat df = new SimpleDateFormat();
                        df.applyPattern("MMM dd yyyy");
                        cal1.setTime(new Date(df.format(tempDate)));
                        String sstart = authHandler.getDateOnlyFormat().format(cal1.getTime());
                        startDate1 = authHandler.getDateOnlyFormat().parse(sstart);
                        // startDate1 = cal1.getTime();
                    }
                } else {
                    DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();
                    Date tempDate = firstDateOfMonth.toDate();
                    Calendar cal1 = Calendar.getInstance();
                    Date d = new Date();
                    SimpleDateFormat df = new SimpleDateFormat();
                    df.applyPattern("MMM dd yyyy");
                    cal1.setTime(new Date(df.format(tempDate)));
                    String sstart = authHandler.getDateOnlyFormat().format(cal1.getTime());
                    startDate1 = authHandler.getDateOnlyFormat().parse(sstart);
                    //startDate1 = cal1.getTime();
                }
                DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                //  including whole last day for calculation             
                request.setAttribute("jasperreport", "JasperReport");
                request.setAttribute("monthlyreport", "MonthlyReport");
                request.setAttribute("jaspersdate", startDate1);
                Calendar endcal = Calendar.getInstance();
                if (endDate != null) {
                    if ((monthIndex == (monthCount - 2) && !monthYearDate)) {
                        endcal.setTime(authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate")));
                    } else {
                        lastDateOfMonth = lastDateOfMonth.plusDays(0);
                        Date tempDate = lastDateOfMonth.toDate();
                        Date d = new Date();
                        SimpleDateFormat df = new SimpleDateFormat();
                        df.applyPattern("MMM dd yyyy");
                        endcal.setTime(new Date(df.format(tempDate)));
                    }
                }
                //Calendar cl = Calendar.getInstance();
                String sstart = authHandler.getDateOnlyFormat().format(endcal.getTime());
                request.setAttribute("jasperenddate", (authHandler.getDateOnlyFormat().parse(sstart)));
                JSONObject jobj = accReportsService.getBalanceSheet(request);
                JSONObject jobj2 = jobj.getJSONObject(Constants.RES_data);
                JSONArray rightObjArr1 = jobj2.getJSONArray("right");
                JSONArray leftObjArr = jobj2.getJSONArray("left");
                if (monthIndex == 0) {
                    temprightObjArr1 = jobj2.getJSONArray("right");
                    templeftObjArr = jobj2.getJSONArray("left");
                }
                for (int i = 0; i < leftObjArr.length(); i++) {
                    JSONObject getObj = leftObjArr.getJSONObject(i);
                    String accId = "";
                    if (getObj.has("accountid")) {
                        accId = getObj.getString("accountid");
                    }
                    if (!accId.equals("")) {
                        if (monthIndex == 0) {
                            JSONObject putObj = new JSONObject();
                            if (getObj.has("accountname")) {
                                putObj.put("accountname", getObj.get("accountname"));
                            }
                            if (getObj.has("accountcode")) {
                                putObj.put("accountcode", getObj.optString("accountcode"));
                            }
                            if (getObj.has("acctype")) {
                                putObj.put("acctype", getObj.getString("acctype"));
                            }
                            if (getObj.has("level")) {
                                putObj.put("level", getObj.get("level"));
                            }
                            if (getObj.has("amount")) {
                                putObj.put("amount_0", getObj.get("amount"));
                                putObj.put("totalamount", getObj.get("amount"));
                            }
                            putObj.put("accountid", accId);
                            jArrL.put(accId, putObj);
                        } else {
                            JSONObject putObj1 = new JSONObject();
                            if (jArrL.has(accId)) {
                                putObj1 = jArrL.getJSONObject(accId);
                            } else {
                                if (getObj.has("accountname")) {
                                    putObj1.put("accountname", getObj.get("accountname"));
                                }
                                if (getObj.has("accountcode")) {
                                    putObj1.put("accountcode", getObj.optString("accountcode"));
                                }
                                if (getObj.has("acctype")) {
                                    putObj1.put("acctype", getObj.getString("acctype"));
                                }
                                if (getObj.has("level")) {
                                    putObj1.put("level", getObj.get("level"));
                                }
                                putObj1.put("accountid", accId);
                                if (getObj.get("accountname").toString().equals("Net Loss")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (check.has("accountname") && check1.has("accountname")) {
                                        if (!check.get("accountname").toString().equals("Net Loss") && !check1.get("accountname").toString().equals("Net Loss")) {
                                            templeftObjArr.put(getObj);
                                        }
                                    }
                                }
                                if (getObj.get("accountname").toString().equals("Net Profit")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (check.has("accountname") && check1.has("accountname")) {
                                        if (!check.get("accountname").toString().equals("Net Profit") && !check1.get("accountname").toString().equals("Net Profit")) {
                                            templeftObjArr.put(getObj);
                                        }
                                    }
                                }
                                if (getObj.get("accountname").toString().equals("Gross Loss")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (check.has("accountname") && check1.has("accountname")) {
                                        if (!check.get("accountname").toString().equals("Gross Loss") && !check1.get("accountname").toString().equals("Gross Loss")) {
                                            templeftObjArr.put(getObj);
                                        }
                                    }
                                }
                                if (getObj.get("accountname").toString().equals("Gross Profit")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (check.has("accountname") && check1.has("accountname")) {
                                        if (!check.get("accountname").toString().equals("Gross Profit") && !check1.get("accountname").toString().equals("Gross Profit")) {
                                            templeftObjArr.put(getObj);
                                        }
                                    }
                                }
                            }
                            if (getObj.has("amount")) {
                                putObj1.put("amount_" + monthIndex, getObj.get("amount"));
                                if (putObj1.has("totalamount")) {
                                    double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                                    temptotal = temptotal + Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                                    putObj1.put("totalamount", temptotal);
                                } else {
                                    putObj1.put("totalamount", getObj.get("amount"));
                                }
                            }
                            jArrL.put(accId, putObj1);
                        }
                    }
                }

                for (int i = 0; i < rightObjArr1.length(); i++) {
                    JSONObject getObj = rightObjArr1.getJSONObject(i);
                    String accId = "";
                    if (getObj.has("accountid")) {
                        accId = getObj.getString("accountid");
                    }
                    if (!accId.equals("")) {
                        if (monthIndex == 0) {
                            JSONObject putObj = new JSONObject();
                            if (getObj.has("accountname")) {
                                putObj.put("accountname", getObj.get("accountname"));
                            }
                            if (getObj.has("accountcode")) {
                                putObj.put("accountcode", getObj.optString("accountcode"));
                            }
                            if (getObj.has("acctype")) {
                                putObj.put("acctype", getObj.getString("acctype"));
                            }
                            if (getObj.has("level")) {
                                putObj.put("level", getObj.get("level"));
                            }
                            if (getObj.has("amount")) {
                                putObj.put("amount_0", getObj.get("amount"));
                                putObj.put("totalamount", getObj.get("amount"));
                            }
                            putObj.put("accountid", accId);
                            jArrR.put(accId, putObj);
                        } else {
                            JSONObject putObj1 = new JSONObject();
                            if (jArrR.has(accId)) {
                                putObj1 = jArrR.getJSONObject(accId);
                            } else {
                                if (getObj.has("accountname")) {
                                    putObj1.put("accountname", getObj.get("accountname"));
                                }
                                if (getObj.has("accountcode")) {
                                    putObj1.put("accountcode", getObj.optString("accountcode"));
                                }
                                if (getObj.has("acctype")) {
                                    putObj1.put("acctype", getObj.getString("acctype"));
                                }
                                if (getObj.has("level")) {
                                    putObj1.put("level", getObj.get("level"));
                                }
                                putObj1.put("accountid", accId);
                                if (getObj.get("accountname").equals("Net Loss")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (check.has("accountname") && check1.has("accountname")) {
                                        if (!check.get("accountname").toString().equals("Net Loss") && !check1.get("accountname").toString().equals("Net Loss")) {
                                            temprightObjArr1.put(getObj);
                                        }
                                    }
                                }
                                if (getObj.get("accountname").equals("Net Profit")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (check.has("accountname") && check1.has("accountname")) {
                                        if (!check.get("accountname").toString().equals("Net Profit") && !check1.get("accountname").toString().equals("Net Profit")) {
                                            temprightObjArr1.put(getObj);
                                        }
                                    }
                                }
                                if (getObj.get("accountname").equals("Gross Loss")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (check.has("accountname") && check1.has("accountname")) {
                                        if (!check.get("accountname").toString().equals("Gross Loss") && !check1.get("accountname").toString().equals("Gross Loss")) {
                                            temprightObjArr1.put(getObj);
                                        }
                                    }
                                }
                                if (getObj.get("accountname").equals("Gross Profit")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (check.has("accountname") && check1.has("accountname")) {
                                        if (!check.get("accountname").toString().equals("Gross Profit") && !check1.get("accountname").toString().equals("Gross Profit")) {
                                            temprightObjArr1.put(getObj);
                                        }
                                    }
                                }
                            }
                            if (getObj.has("amount")) {
                                putObj1.put("amount_" + monthIndex, getObj.get("amount"));
                                if (putObj1.has("totalamount")) {
                                    double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                                    temptotal = temptotal + Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                                    putObj1.put("totalamount", temptotal);
                                } else {
                                    putObj1.put("totalamount", getObj.get("amount"));
                                }
                            }
                            jArrR.put(accId, putObj1);
                        }
                    }
                }
                localStartDate = localStartDate.plus(Period.months(1));
                monthIndex++;
            }// end looping thru the months

            //put the total values into the respective objects as the last amount
            int totalpos = monthYearDate && !isOneMonth ? monthCount : monthCount - 1;
            for (int i = 0; i < temprightObjArr1.length(); i++) {
                JSONObject getObj1 = temprightObjArr1.getJSONObject(i);
                if (getObj1.has("accountid")) {
                    String accId = getObj1.getString("accountid");
                    if (jArrR.has(accId)) {
                        JSONObject getObj = jArrR.getJSONObject(accId);
                        if (getObj.has("totalamount")) {
                            if (!extrapref.isShowallaccountsinbs()) {
                                //To remove 0 amount account and which should not be at level 0.
                                double totalAmt = !StringUtil.isNullOrEmpty(getObj.get("totalamount").toString()) ? Double.parseDouble(getObj.get("totalamount").toString()) : 0;
                                double level = !StringUtil.isNullOrEmpty(getObj.get("level").toString()) ? Double.parseDouble(getObj.get("level").toString()) : 0;	//SDP-11724
                                if (totalAmt == 0.0 && level != 0.0) {
                                    jArrR.remove(accId);
                                } else {
                                    getObj.put("amount_" + (totalpos), getObj.get("totalamount"));
                                }
                            } else {
                                getObj.put("amount_" + (totalpos), getObj.get("totalamount"));
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < templeftObjArr.length(); i++) {
                JSONObject getObj1 = templeftObjArr.getJSONObject(i);
                if (getObj1.has("accountid")) {
                    String accId = getObj1.getString("accountid");
                    if (jArrL.has(accId)) {
                        JSONObject getObj = jArrL.getJSONObject(accId);
                        if (getObj.has("totalamount")) {
                            if (!extrapref.isShowallaccountsinbs()) {
                                //To remove 0 amount account and which should not be at level 0.
                                double totalAmt = !StringUtil.isNullOrEmpty(getObj.get("totalamount").toString()) ? Double.parseDouble(getObj.get("totalamount").toString()) : 0;
                                double level = !StringUtil.isNullOrEmpty(getObj.get("level").toString()) ? Double.parseDouble(getObj.get("level").toString()) : 0;	//SDP-11724
                                if (totalAmt == 0.0 && level != 0.0) {
                                    jArrL.remove(accId);
                                } else {
                                    getObj.put("amount_" + (totalpos), getObj.get("totalamount"));
                                }
                            } else {
                                getObj.put("amount_" + (totalpos), getObj.get("totalamount"));
                            }
                        }
                    }
                }
            }

            List<String> monthList = new ArrayList();
            localStartDate = new LocalDate(startDate);
            localEndDate = new LocalDate(endDate);
            // just a trick to include the last month as well
            if (!isOneMonth) {
                localEndDate = localEndDate.plus(Period.months(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }
            while (localStartDate.isBefore(localEndDate)) {
                String monthName = localStartDate.toString("MMM yyyy");
                localStartDate = localStartDate.plus(Period.months(1));
                monthList.add(monthName);
            }
            // the first object would be the months array
            int monthlist = monthYearDate ? monthList.size() : monthList.size() - 1;
            JSONArray monthArray = new JSONArray();
            JSONObject monthObj;
            for (int i = 0; i < monthlist; i++) {
                monthObj = new JSONObject();
                monthObj.put("monthname", monthList.get(i));
                monthArray.put(monthObj);
            }
            monthObj = new JSONObject();
            monthObj.put("monthname", "Total");
            monthArray.put(monthObj);
            jobj1.put("months", monthArray);
            jobj1.put("left", jArrL);
            jobj1.put("refleft", templeftObjArr);
            jobj1.put("right", jArrR);
            jobj1.put("refright", temprightObjArr1);
        } catch (JSONException e) {
            e.printStackTrace();
            throw ServiceException.FAILURE("getMonthlyBalanceSheetforExport : " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return jobj1;
    } 
 

@Override
    public JSONObject getTradingAndProfitLossWithBudget(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject endjobj = new JSONObject();
        try {
            CompanyAccountPreferences pref = null;
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
            pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            if (!pref.isShowchild()) {// Anup Check to hide child customer / vendors from balance sheet
                request.setAttribute("hidechildCV", true);
                paramJobj.put("hidechildCV", true);
            }
            JSONObject jArrR = new JSONObject();
            JSONObject jArrL = new JSONObject();
            JSONArray temprightObjArr1 = new JSONArray();
            JSONArray templeftObjArr = new JSONArray();
       
            Date startDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("stdate"));
            Date endDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("enddate"));

            //to calculate monthly budget:ERP-37238
            paramJobj.put("isMonthlyBudget", true);
            paramJobj.put("isYTDBudget", false);
            paramJobj.put("isAnnualBudget", false);

            JSONObject jobj = getTradingAndProfitLoss(paramJobj);

            JSONObject jobj2 = jobj.getJSONObject(Constants.RES_data);
            JSONArray rightObjArr1 = jobj2.getJSONArray("right");
            JSONArray leftObjArr = jobj2.getJSONArray("left");

            temprightObjArr1 = jobj2.getJSONArray("right");
            templeftObjArr = jobj2.getJSONArray("left");

            for (int i = 0; i < leftObjArr.length(); i++) {
                JSONObject getObj = leftObjArr.getJSONObject(i);
                String accId = "";
                boolean leaf = false;
                int level = 0;
                if (getObj.has("accountid")) {
                    accId = getObj.getString("accountid");
                }
                if (!accId.equals("")) {
                    JSONObject putObj = new JSONObject();
                    double budget = 0, varianceInPercent = 0, actualamount = 0, variance = 0;
                    String varianceInPercentStr = "";
                    if (getObj.has("accountname")) {
                        putObj.put("accountname", getObj.get("accountname"));
                    }
                    if (getObj.has("acctype")) {
                        putObj.put("acctype", getObj.getString("acctype"));
                    }
                    if (getObj.has("level")) {
                        putObj.put("level", getObj.get("level"));
                        level = getObj.getInt("level");
                    }
                    if (getObj.has("fmt")) {
                        putObj.put("fmt", getObj.get("fmt"));
                    }
                    if (getObj.has("accountflag")) {
                        putObj.put("accountflag", getObj.get("accountflag"));
                    }
                    if (getObj.has("leaf")) {
                        putObj.put("leaf", getObj.getBoolean("leaf"));
                        leaf = getObj.getBoolean("leaf");
                    }
                    if (getObj.has("isdebit")) {
                        putObj.put("isdebit", getObj.get("isdebit"));
                    }
                    if (getObj.has("amount")) {
                        putObj.put("actualamount", getObj.get("amount"));
                        actualamount += Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                    }
                                if (getObj.has("budget")) {
                                    putObj.put("actualbudget", getObj.get("budget"));
                                    budget += Double.parseDouble((getObj.get("budget").toString().equals("") || getObj.get("budget").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("budget").toString()));
                                }
                    variance = budget - actualamount;
                    //if budget is zero then don't calculate variance percentage
                    if(Double.compare(budget, 0) > 0){
                        varianceInPercent = variance / budget * 100;
                        //do rounding of percentage
                        varianceInPercentStr = authHandler.formattingdecimal(varianceInPercent, 2);
                    }
                    if (!leaf && level == 0) {
                        if (variance != 0) {
                            putObj.put("variance", variance);
                            //if budget present not present or blank then show it as NA
                            if(!(getObj.has("budget")) || (getObj.has("budget") && getObj.get("budget").toString().equals(""))){
                                putObj.put("varianceinpercent", "NA");
                            } else{
                                putObj.put("varianceinpercent", varianceInPercentStr);
                            }
                        } else {
                            putObj.put("variance", "");
                            putObj.put("varianceinpercent", "");
                        }
                    } else {
                        putObj.put("variance", variance);
                        //if budget present not present or blank then show it as NA
                        if(!(getObj.has("budget")) || (getObj.has("budget") && getObj.get("budget").toString().equals(""))){
                            putObj.put("varianceinpercent", "NA");
                        } else{
                            putObj.put("varianceinpercent", varianceInPercentStr);
                        }
                    }
                    putObj.put("accountid", accId);
                    jArrL.put(accId, putObj);
                }
            }

            for (int i = 0; i < rightObjArr1.length(); i++) {
                JSONObject getObj = rightObjArr1.getJSONObject(i);
                String accId = "";
                boolean leaf = false;
                int level = 0;
                if (getObj.has("accountid")) {
                    accId = getObj.getString("accountid");
                }
                if (!accId.equals("")) {
                    JSONObject putObj = new JSONObject();
                    double variance = 0, varianceInPercent = 0, budget = 0, actualamount = 0;
                    String varianceInPercentStr = "";
                    if (getObj.has("accountname")) {
                        putObj.put("accountname", getObj.get("accountname"));
                    }
                    if (getObj.has("acctype")) {
                        putObj.put("acctype", getObj.getString("acctype"));
                    }
                    if (getObj.has("level")) {
                        putObj.put("level", getObj.get("level"));
                        level = getObj.getInt("level");
                    }
                    if (getObj.has("fmt")) {
                        putObj.put("fmt", getObj.get("fmt"));
                    }
                    if (getObj.has("accountflag")) {
                        putObj.put("accountflag", getObj.get("accountflag"));
                    }
                    if (getObj.has("leaf")) {
                        putObj.put("leaf", getObj.getBoolean("leaf"));
                        leaf = getObj.getBoolean("leaf");
                    }
                    if (getObj.has("isdebit")) {
                        putObj.put("isdebit", getObj.get("isdebit"));
                    }
                    if (getObj.has("amount")) {
                        putObj.put("actualamount", getObj.get("amount"));
                        actualamount += Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                    }
                    if (getObj.has("budget")) {
                        putObj.put("actualbudget", getObj.get("budget"));
                        budget += Double.parseDouble((getObj.get("budget").toString().equals("") || getObj.get("budget").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("budget").toString()));
                    }
                    variance = budget - actualamount;
                    //if budget is zero then don't calculate variance percentage
                    if(Double.compare(budget, 0) > 0){
                        varianceInPercent = variance / budget * 100;
                        //do rounding of percentage
                        varianceInPercentStr = authHandler.formattingdecimal(varianceInPercent, 2);
                    }
                    if (!leaf && level == 0) {
                        if (variance != 0) {
                            putObj.put("variance", variance);
                            //if budget present not present or blank then show it as NA
                            if(!(getObj.has("budget")) || (getObj.has("budget") && getObj.get("budget").toString().equals(""))){
                                putObj.put("varianceinpercent", "NA");
                            } else{
                                putObj.put("varianceinpercent", varianceInPercentStr);
                            }
                        } else {
                            putObj.put("variance", "");
                            putObj.put("varianceinpercent", "");
                        }
                    } else {
                        putObj.put("variance", variance);
                        //if budget present not present or blank then show it as NA
                        if(!(getObj.has("budget")) || (getObj.has("budget") && getObj.get("budget").toString().equals(""))){
                            putObj.put("varianceinpercent", "NA");
                        } else{
                            putObj.put("varianceinpercent", varianceInPercentStr);
                        }
                    }
                    putObj.put("accountid", accId);
                    jArrR.put(accId, putObj);
                }
            }

            // to calculate YTD amount and YTD budget  ERP-37238              
            paramJobj.put("jasperreport", "JasperReport");
            paramJobj.put("monthlyreport", "MonthlyReport");
            paramJobj.put("jaspersdate", new Date(1970));
            paramJobj.put("jasperenddate", endDate);
            paramJobj.put("isMonthlyBudget", false);
            paramJobj.put("isYTDBudget", true); //monthly ytd
            paramJobj.put("isAnnualBudget", false);

//            JSONObject jobj4 = getTradingAndProfitLoss(request);
            JSONObject jobj4 = getTradingAndProfitLoss(paramJobj);

            JSONObject jobj3 = jobj4.getJSONObject(Constants.RES_data);
            JSONArray rightObjArr2 = jobj3.getJSONArray("right");
            JSONArray leftObjArr1 = jobj3.getJSONArray("left");
               // to calculate YTD amount and YTD budget             
            for (int i = 0; i < leftObjArr1.length(); i++) {
                JSONObject getObj = leftObjArr1.getJSONObject(i);
                String accId = "";
                if (getObj.has("accountid")) {
                    accId = getObj.getString("accountid");
                }
                if (!accId.equals("")) {
                    JSONObject putObj1 = new JSONObject();
                    if (jArrL.has(accId)) {
                        putObj1 = jArrL.getJSONObject(accId);
                    }
                    if (getObj.has("accountname")) {
                        putObj1.put("accountname", getObj.get("accountname"));
                    }
                    if (getObj.has("acctype")) {
                        putObj1.put("acctype", getObj.getString("acctype"));
                    }
                    if (getObj.has("level")) {
                        putObj1.put("level", getObj.get("level"));
                    }
                    if (getObj.has("fmt")) {
                        putObj1.put("fmt", getObj.get("fmt"));
                    }
                    if (getObj.has("accountflag")) {
                        putObj1.put("accountflag", getObj.get("accountflag"));
                    }
                    if (getObj.has("leaf")) {
                        putObj1.put("leaf", getObj.getBoolean("leaf"));
                    }
                    if (getObj.has("isdebit")) {
                        putObj1.put("isdebit", getObj.get("isdebit"));
                    }
                    if (getObj.has("amount")) {
                        putObj1.put("ytdamount", getObj.get("amount"));
                    }
                            if (getObj.has("budget")) {
                            putObj1.put("ytdbudget", getObj.get("budget"));
                        }
                    putObj1.put("accountid", accId);
                    if (getObj.get("accountid").equals("Net Loss")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Net Loss") && !check1.get("accountid").toString().equals("Net Loss")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Net Profit")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Net Profit") && !check1.get("accountid").toString().equals("Net Profit")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Gross Loss")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);

                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Gross Loss") && !check1.get("accountid").toString().equals("Gross Loss")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Gross Profit")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Gross Profit") && !check1.get("accountid").toString().equals("Gross Profit")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    jArrL.put(accId, putObj1);
                }
            }

            for (int i = 0; i < rightObjArr2.length(); i++) {
                JSONObject getObj = rightObjArr2.getJSONObject(i);
                String accId = "";
                if (getObj.has("accountid")) {
                    accId = getObj.getString("accountid");
                }
                if (!accId.equals("")) {
                    JSONObject putObj1 = new JSONObject();
                    if (jArrR.has(accId)) {
                        putObj1 = jArrR.getJSONObject(accId);
                    }
                    if (getObj.has("accountname")) {
                        putObj1.put("accountname", getObj.get("accountname"));
                    }
                    if (getObj.has("acctype")) {
                        putObj1.put("acctype", getObj.getString("acctype"));
                    }
                    if (getObj.has("level")) {
                        putObj1.put("level", getObj.get("level"));
                    }
                    if (getObj.has("fmt")) {
                        putObj1.put("fmt", getObj.get("fmt"));
                    }
                    if (getObj.has("accountflag")) {
                        putObj1.put("accountflag", getObj.get("accountflag"));
                    }
                    if (getObj.has("leaf")) {
                        putObj1.put("leaf", getObj.getBoolean("leaf"));
                    }
                    if (getObj.has("isdebit")) {
                        putObj1.put("isdebit", getObj.get("isdebit"));
                    }
                    if (getObj.has("amount")) {
                        putObj1.put("ytdamount", getObj.get("amount"));
                    }
                    if (getObj.has("budget")) {
                        putObj1.put("ytdbudget", getObj.get("budget"));
                    }
                    putObj1.put("accountid", accId);
                    if (getObj.get("accountid").equals("Net Loss")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Net Loss") && !check1.get("accountid").toString().equals("Net Loss")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Net Profit")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Net Profit") && !check1.get("accountid").toString().equals("Net Profit")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Gross Loss")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);

                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Gross Loss") && !check1.get("accountid").toString().equals("Gross Loss")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Gross Profit")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Gross Profit") && !check1.get("accountid").toString().equals("Gross Profit")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    jArrR.put(accId, putObj1);
                }
            }

            // To Calculate Annual Budget
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int startYear = cal.get(Calendar.YEAR);
            Calendar annualcal = Calendar.getInstance();
            annualcal.set(Calendar.YEAR, startYear);
            annualcal.set(Calendar.DAY_OF_YEAR, 1);
            String sstart = authHandler.getDateOnlyFormat().format(annualcal.getTime());
            Date start = authHandler.getDateOnlyFormat().parse(sstart);

            //set date to last day of startyear
            annualcal.set(Calendar.YEAR, startYear);
            annualcal.set(Calendar.MONTH, 11); // 11 = december
            annualcal.set(Calendar.DAY_OF_MONTH, 31); // new years eve
            sstart = authHandler.getDateOnlyFormat().format(annualcal.getTime());
            Date end = authHandler.getDateOnlyFormat().parse(sstart);
            
            //to calculate annual budget.isAnnualBudget:true-ERP-37238
            paramJobj.put("jasperreport", "JasperReport");
            paramJobj.put("monthlyreport", "MonthlyReport");
            paramJobj.put("jaspersdate", start);
            paramJobj.put("jasperenddate", end);
            paramJobj.put("isMonthlyBudget", false);
            paramJobj.put("isYTDBudget", false);
            paramJobj.put("isAnnualBudget", true);

            JSONObject jobj6 =getTradingAndProfitLoss(paramJobj);

            JSONObject jobj5 = jobj6.getJSONObject(Constants.RES_data);
            JSONArray rightObjArr3 = jobj5.getJSONArray("right");
            JSONArray leftObjArr3 = jobj5.getJSONArray("left");

            for (int i = 0; i < leftObjArr3.length(); i++) {
                JSONObject getObj = leftObjArr3.getJSONObject(i);
                String accId = "";
                if (getObj.has("accountid")) {
                    accId = getObj.getString("accountid");
                }
                if (!accId.equals("")) {
                    JSONObject putObj1 = new JSONObject();
                    if (jArrL.has(accId)) {
                        putObj1 = jArrL.getJSONObject(accId);
                    }
                    if (getObj.has("accountname")) {
                        putObj1.put("accountname", getObj.get("accountname"));
                    }
                    if (getObj.has("acctype")) {
                        putObj1.put("acctype", getObj.getString("acctype"));
                    }
                    if (getObj.has("level")) {
                        putObj1.put("level", getObj.get("level"));
                    }
                    if (getObj.has("fmt")) {
                        putObj1.put("fmt", getObj.get("fmt"));
                    }
                    if (getObj.has("accountflag")) {
                        putObj1.put("accountflag", getObj.get("accountflag"));
                    }
                    if (getObj.has("leaf")) {
                        putObj1.put("leaf", getObj.getBoolean("leaf"));
                    }
                    if (getObj.has("isdebit")) {
                        putObj1.put("isdebit", getObj.get("isdebit"));
                    }
                            if (getObj.has("budget")) {
                            putObj1.put("annualbudget", getObj.get("budget"));
                        }
                    putObj1.put("accountid", accId);
                    if (getObj.get("accountid").equals("Net Loss")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Net Loss") && !check1.get("accountid").toString().equals("Net Loss")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Net Profit")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Net Profit") && !check1.get("accountid").toString().equals("Net Profit")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Gross Loss")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);

                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Gross Loss") && !check1.get("accountid").toString().equals("Gross Loss")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Gross Profit")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Gross Profit") && !check1.get("accountid").toString().equals("Gross Profit")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    jArrL.put(accId, putObj1);
                }
            }

            for (int i = 0; i < rightObjArr3.length(); i++) {
                JSONObject getObj = rightObjArr3.getJSONObject(i);
                String accId = "";
                if (getObj.has("accountid")) {
                    accId = getObj.getString("accountid");
                }
                if (!accId.equals("")) {
                    JSONObject putObj1 = new JSONObject();
                    if (jArrR.has(accId)) {
                        putObj1 = jArrR.getJSONObject(accId);
                    }
                    if (getObj.has("accountname")) {
                        putObj1.put("accountname", getObj.get("accountname"));
                    }
                    if (getObj.has("acctype")) {
                        putObj1.put("acctype", getObj.getString("acctype"));
                    }
                    if (getObj.has("level")) {
                        putObj1.put("level", getObj.get("level"));
                    }
                    if (getObj.has("fmt")) {
                        putObj1.put("fmt", getObj.get("fmt"));
                    }
                    if (getObj.has("accountflag")) {
                        putObj1.put("accountflag", getObj.get("accountflag"));
                    }
                    if (getObj.has("leaf")) {
                        putObj1.put("leaf", getObj.getBoolean("leaf"));
                    }
                    if (getObj.has("isdebit")) {
                        putObj1.put("isdebit", getObj.get("isdebit"));
                    }
                    if (getObj.has("budget")) {
                        putObj1.put("annualbudget", getObj.get("budget"));
                    }
                    putObj1.put("accountid", accId);
                    if (getObj.get("accountid").equals("Net Loss")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Net Loss") && !check1.get("accountid").toString().equals("Net Loss")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Net Profit")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Net Profit") && !check1.get("accountid").toString().equals("Net Profit")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Gross Loss")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);

                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Gross Loss") && !check1.get("accountid").toString().equals("Gross Loss")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    if (getObj.get("accountid").equals("Gross Profit")) {
                        JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                        JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                        if (check.has("accountid") && check1.has("accountid")) {
                            if (!check.get("accountid").toString().equals("Gross Profit") && !check1.get("accountid").toString().equals("Gross Profit")) {
                                temprightObjArr1.put(getObj);
                            }
                        }
                    }
                    jArrR.put(accId, putObj1);
                }
            }
            endjobj.put("left", jArrL);
            endjobj.put("refleft", AccReportsHandler.getConvertedJSONArray(jArrL, templeftObjArr));
            endjobj.put("right", jArrR);
            endjobj.put("refright", AccReportsHandler.getConvertedJSONArray(jArrR, temprightObjArr1));
        } catch (JSONException e) {
            e.printStackTrace();
            throw ServiceException.FAILURE("getTradingAndProfitLossWithBudget : " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return endjobj;
    }

@Override
 public JSONObject getDimesionBasedProfitLoss(JSONObject paramJobj, boolean monthYearFormat) throws ServiceException, SessionExpiredException {
        JSONObject jobj1 = new JSONObject();
        JSONObject jobjTemp = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            String currencyid =paramJobj.getString(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyname = (!StringUtil.isNullOrEmpty(currency.getSymbol())) ? currency.getSymbol() : currency.getName();

            CompanyAccountPreferences pref = null;
            KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            if (!pref.isShowchild()) {// Anup Check to hide child customer / vendors from balance sheet
                paramJobj.put("hidechildCV", true);
            }
            
            ExtraCompanyPreferences extrapref = null;
            boolean isShowAllAccountsInPnl = false;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                isShowAllAccountsInPnl = extrapref.isShowAllAccountsInPnl();
            }
            
            boolean isCustomLayout = !StringUtil.isNullOrEmpty(paramJobj.optString("isCustomLayout",null)) ? Boolean.parseBoolean(paramJobj.optString("isCustomLayout")) : false;
            JSONObject jArrR = new JSONObject();
            JSONObject jArrL = new JSONObject();
            JSONArray temprightObjArr1 = new JSONArray();
            JSONArray templeftObjArr = new JSONArray();
            int j = 0;
            String Searchjson = null;
            if (paramJobj.optString(Constants.Acc_Search_Json,null) != null) {
                Searchjson = paramJobj.optString(Constants.Acc_Search_Json, "{}");
            }

            JSONObject SearchJsonObj = new JSONObject(Searchjson);
            JSONArray SearchJsonArray = SearchJsonObj.getJSONArray("root");
            JSONObject compareObj = SearchJsonArray.optJSONObject(0);

            String column = compareObj.optString("column");
            String refdbname = compareObj.optString("refdbname");
            String xfield = compareObj.optString("xfield");
            String iscustomcolumn = compareObj.optString("iscustomcolumn");
            String iscustomcolumndata = compareObj.optString("iscustomcolumndata");
            String isfrmpmproduct = compareObj.optString("isfrmpmproduct");
            String fieldtype = compareObj.optString("fieldtype");
            String searchText = compareObj.optString("searchText");
            String columnheader = compareObj.optString("columnheader");
            String search = compareObj.optString("search");
            String xtype = compareObj.optString("xtype");
            String combosearch = "";
            try{
                combosearch = StringUtil.DecodeText(compareObj.optString("combosearch"));
            } catch(Exception e){
                combosearch = compareObj.optString("combosearch");
            }
            String isinterval = compareObj.optString("isinterval");
            String interval = compareObj.optString("interval");
            String isbefore = compareObj.optString("isbefore");

            List<String> searchTextItems = Arrays.asList(searchText.split("\\s*,\\s*"));
            List<String> searchItems = Arrays.asList(search.split("\\s*,\\s*"));
            List<String> combosearchItems = Arrays.asList(combosearch.split("\\s*,\\s*"));

            JSONArray comparedata = new JSONArray();
            for (int i = 0; i < searchTextItems.size(); i++) {
                JSONObject cntObj = new JSONObject();
                cntObj.put("searchText", searchTextItems.get(i));
                cntObj.put("search", searchItems.get(i));
                cntObj.put("combosearch", combosearchItems.get(i));
                cntObj.put("column", column);
                cntObj.put("refdbname", refdbname);
                cntObj.put("xfield", xfield);
                cntObj.put("iscustomcolumn", iscustomcolumn);
                cntObj.put("iscustomcolumndata", iscustomcolumndata);
                cntObj.put("isfrmpmproduct", isfrmpmproduct);
                cntObj.put("fieldtype", fieldtype);
                cntObj.put("columnheader", columnheader);
                cntObj.put("xtype", xtype);
                cntObj.put("isinterval", isinterval);
                cntObj.put("interval", interval);
                cntObj.put("isbefore", isbefore);
                comparedata.put(cntObj);
            }

            for (j = 0; j < comparedata.length(); j++) {
                JSONArray dimSearchJsonArr = new JSONArray();
                JSONObject dimJson = comparedata.getJSONObject(j);
                dimSearchJsonArr.put(dimJson);
                JSONObject putSearchJson = new JSONObject();
                putSearchJson.put("root", dimSearchJsonArr);
                //  search JSON Array for Dimension Based report
                paramJobj.put("DimensionBasedComparisionReport", "DimensionBasedComparisionReport");
                paramJobj.put("DimensionBasedSearchJson", putSearchJson);
                paramJobj.put("monthlyreport", "MonthlyReport");
                paramJobj.put("isPeriod", true);
                JSONObject jobj = new JSONObject();
                if (!isCustomLayout) {// call for dimension based PnL
                    jobj = getTradingAndProfitLoss(paramJobj);
                } else {// call for dimension based Custom PnL
//                    jobj = getBSorPL_CustomLayout(request, extrapref, companyid);
                    jobj = getBSorPL_CustomLayout(paramJobj, extrapref, companyid);
                }
                JSONObject jobj2 = jobj.getJSONObject(Constants.RES_data);
                JSONArray rightObjArr1 = jobj2.getJSONArray("right");
                JSONArray leftObjArr = jobj2.getJSONArray("left");
                if (j == 0) {
                    temprightObjArr1 = jobj2.getJSONArray("right");
                    templeftObjArr = jobj2.getJSONArray("left");
                }

                for (int i = 0; i < leftObjArr.length(); i++) {
                    JSONObject getObj = leftObjArr.getJSONObject(i);
                    String accId = "";
                    if (getObj.has("accountid")) {
                        accId = getObj.getString("accountid");
                    }
                    if (!accId.equals("")) {
                        if (j == 0) {
                            JSONObject putObj = new JSONObject();
                            if (getObj.has("accountname")) {
                                putObj.put("accountname", getObj.get("accountname"));
                            }
                            if (getObj.has("accountcode")) {
                                putObj.put("accountcode", getObj.get("accountcode"));
                            }
                            if (getObj.has("haschild")) {
                                putObj.put("haschild", getObj.get("haschild"));
                            }
                            if (getObj.has("acctype")) {
                                putObj.put("acctype", getObj.getString("acctype"));
                            }
                            if (getObj.has("level")) {
                                putObj.put("level", getObj.get("level"));
                            }
                            if (getObj.has("fmt")) {
                                putObj.put("fmt", getObj.get("fmt"));
                            }
                            if (getObj.has("accountflag")) {
                                putObj.put("accountflag", getObj.get("accountflag"));
                            }
                            if (getObj.has("leaf")) {
                                putObj.put("leaf", getObj.getBoolean("leaf"));
                            }
                            if (getObj.has("isdebit")) {
                                putObj.put("isdebit", getObj.get("isdebit"));
                            }
                            if (getObj.has("amount") && !isCustomLayout) {
                                try {
                                    double temptotal = Double.parseDouble((getObj.get("amount").toString()));
                                    putObj.put("amount_0", authHandler.formattedAmount(temptotal, companyid));
                                    putObj.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                } catch (Exception e) {
                                    putObj.put("amount_0", getObj.get("amount"));
                                    putObj.put("totalamount", getObj.get("amount"));
                                }
                            } else if (getObj.has("periodamount") && isCustomLayout) {
                                try {
                                    double temptotal = Double.parseDouble((getObj.get("periodamount").toString()));
                                    if (temptotal != 0.0) {
                                        putObj.put("amount_0", authHandler.formattedAmount(temptotal, companyid));
                                        putObj.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    } else {
                                        putObj.put("amount_0", getObj.get("periodamount"));
                                        putObj.put("totalamount", getObj.get("periodamount"));
                                    }
                                } catch (Exception e) {
                                    putObj.put("amount_0", getObj.get("periodamount"));
                                    putObj.put("totalamount", getObj.get("periodamount"));
                                }
                            }
                            putObj.put("accountid", accId);
                            jArrL.put(accId, putObj);
                        } else {
                            JSONObject putObj1 = new JSONObject();
                            if (jArrL.has(accId)) {
                                putObj1 = jArrL.getJSONObject(accId);
                            } else {
                                if (getObj.has("accountname")) {
                                    putObj1.put("accountname", getObj.get("accountname"));
                                }
                                if (getObj.has("accountcode")) {
                                    putObj1.put("accountcode", getObj.get("accountcode"));
                                }
                                if (getObj.has("haschild")) {
                                    putObj1.put("haschild", getObj.get("haschild"));
                                }
                                if (getObj.has("acctype")) {
                                    putObj1.put("acctype", getObj.getString("acctype"));
                                }
                                if (getObj.has("level")) {
                                    putObj1.put("level", getObj.get("level"));
                                }
                                if (getObj.has("fmt")) {
                                    putObj1.put("fmt", getObj.get("fmt"));
                                }
                                if (getObj.has("accountflag")) {
                                    putObj1.put("accountflag", getObj.get("accountflag"));
                                }
                                if (getObj.has("leaf")) {
                                    putObj1.put("leaf", getObj.getBoolean("leaf"));
                                }
                                if (getObj.has("isdebit")) {
                                    putObj1.put("isdebit", getObj.get("isdebit"));
                                }
                                putObj1.put("accountid", accId);
                                if (getObj.get("accountname").toString().equals("Net Loss")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (!check.optString("accountname","").toString().equals("Net Loss") && !check1.optString("accountname","").toString().equals("Net Loss")) {
                                        templeftObjArr.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").toString().equals("Net Profit")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (!check.optString("accountname","").toString().equals("Net Profit") && !check1.optString("accountname","").toString().equals("Net Profit")) {
                                        templeftObjArr.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").toString().equals("Gross Loss")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (!check.optString("accountname","").toString().equals("Gross Loss") && !check1.optString("accountname","").toString().equals("Gross Loss")) {
                                        templeftObjArr.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").toString().equals("Gross Profit")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (!check.optString("accountname","").toString().equals("Gross Profit") && !check1.optString("accountname","").toString().equals("Gross Profit")) {
                                        templeftObjArr.put(getObj);
                                    }
                                }
                                if(isCustomLayout){
                                    templeftObjArr.put(getObj);
                                }
                            }
                            if (getObj.has("amount") && !isCustomLayout) {
                                try {
                                    double temptotal = Double.parseDouble((getObj.get("amount").toString()));
                                    putObj1.put("amount_" + j, authHandler.formattedAmount(temptotal, companyid));
                                } catch (Exception e) {
                                    putObj1.put("amount_" + j, getObj.get("amount"));
                                }

                                if (putObj1.has("totalamount")) {
                                    if (!isCustomLayout) {
                                        double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                                        temptotal = temptotal + Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                                        putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    } else if (!putObj1.get("totalamount").toString().equals("")) {
                                        double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                                        temptotal = temptotal + Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                                        putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    }
                                } else {
                                    try {
                                        double temptotal = Double.parseDouble((getObj.get("amount").toString()));
                                        putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    } catch (Exception e) {
                                        putObj1.put("totalamount", getObj.get("amount"));
                                    }
                                }
                            } else if (getObj.has("periodamount") && isCustomLayout) {
                                try {
                                    double temptotal = Double.parseDouble((getObj.get("periodamount").toString()));
                                    putObj1.put("amount_" + j, authHandler.formattedAmount(temptotal, companyid));
                                } catch (Exception e) {
                                    putObj1.put("amount_" + j, getObj.get("periodamount"));
                                }
                                if (putObj1.has("totalamount")) {
                                    if (!StringUtil.isNullOrEmpty(getObj.optString("periodamount"))) {
                                        double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                                        temptotal = temptotal + Double.parseDouble((getObj.get("periodamount").toString().equals("") || getObj.get("periodamount").toString().equals("-") || getObj.get("periodamount").toString().contains("<")) ? "0.0" : (getObj.get("periodamount").toString()));
                                        putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    }
                                } else {
                                    try {
                                        if (!StringUtil.isNullOrEmpty(getObj.optString("periodamount"))) {
                                            double temptotal = Double.parseDouble((getObj.get("periodamount").toString()));
                                            putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                        }
                                    } catch (Exception e) {
                                        putObj1.put("totalamount", getObj.get("periodamount"));
                                    }
                                }
                            }
                            jArrL.put(accId, putObj1);
                        }
                    }
                }

                for (int i = 0; i < rightObjArr1.length(); i++) {
                    JSONObject getObj = rightObjArr1.getJSONObject(i);
                    String accId = "";
                    if (getObj.has("accountid")) {
                        accId = getObj.getString("accountid");
                    }
                    if (!accId.equals("")) {
                        if (j == 0) {
                            JSONObject putObj = new JSONObject();
                            if (getObj.has("accountname")) {
                                putObj.put("accountname", getObj.get("accountname"));
                            }
                            if (getObj.has("accountcode")) {
                                putObj.put("accountcode", getObj.get("accountcode"));
                            }
                            if (getObj.has("haschild")) {
                                putObj.put("haschild", getObj.get("haschild"));
                            }
                            if (getObj.has("acctype")) {
                                putObj.put("acctype", getObj.getString("acctype"));
                            }
                            if (getObj.has("level")) {
                                putObj.put("level", getObj.get("level"));
                            }
                            if (getObj.has("fmt")) {
                                putObj.put("fmt", getObj.get("fmt"));
                            }
                            if (getObj.has("accountflag")) {
                                putObj.put("accountflag", getObj.get("accountflag"));
                            }
                            if (getObj.has("leaf")) {
                                putObj.put("leaf", getObj.getBoolean("leaf"));
                            }
                            if (getObj.has("isdebit")) {
                                putObj.put("isdebit", getObj.get("isdebit"));
                            }
                            if (getObj.has("amount")) {
                                try {
                                    double temptotal = Double.parseDouble((getObj.get("amount").toString()));
                                    putObj.put("amount_0", authHandler.formattedAmount(temptotal, companyid));
                                    putObj.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                } catch (Exception e) {
                                    putObj.put("amount_0", getObj.get("amount"));
                                    putObj.put("totalamount", getObj.get("amount"));
                                }
                            }
                            putObj.put("accountid", accId);
                            jArrR.put(accId, putObj);
                        } else {
                            JSONObject putObj1 = new JSONObject();
                            if (jArrR.has(accId)) {
                                putObj1 = jArrR.getJSONObject(accId);
                            } else {
                                if (getObj.has("accountname")) {
                                    putObj1.put("accountname", getObj.get("accountname"));
                                }
                                if (getObj.has("accountcode")) {
                                    putObj1.put("accountcode", getObj.get("accountcode"));
                                }
                                if (getObj.has("haschild")) {
                                    putObj1.put("haschild", getObj.get("haschild"));
                                }
                                if (getObj.has("acctype")) {
                                    putObj1.put("acctype", getObj.getString("acctype"));
                                }
                                if (getObj.has("level")) {
                                    putObj1.put("level", getObj.get("level"));
                                }
                                if (getObj.has("fmt")) {
                                    putObj1.put("fmt", getObj.get("fmt"));
                                }
                                if (getObj.has("accountflag")) {
                                    putObj1.put("accountflag", getObj.get("accountflag"));
                                }
                                if (getObj.has("leaf")) {
                                    putObj1.put("leaf", getObj.getBoolean("leaf"));
                                }
                                if (getObj.has("isdebit")) {
                                    putObj1.put("isdebit", getObj.get("isdebit"));
                                }
                                putObj1.put("accountid", accId);
                                if (getObj.get("accountname").equals("Net Loss")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (!check.optString("accountname","").toString().equals("Net Loss") && !check1.optString("accountname","").toString().equals("Net Loss")) {
                                        temprightObjArr1.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").equals("Net Profit")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (!check.optString("accountname","").toString().equals("Net Profit") && !check1.optString("accountname","").toString().equals("Net Profit")) {
                                        temprightObjArr1.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").equals("Gross Loss")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (!check.optString("accountname","").toString().equals("Gross Loss") && !check1.optString("accountname","").toString().equals("Gross Loss")) {
                                        temprightObjArr1.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").equals("Gross Profit")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (!check.optString("accountname","").toString().equals("Gross Profit") && !check1.optString("accountname","").toString().equals("Gross Profit")) {
                                        temprightObjArr1.put(getObj);
                                    }
                                }
                                if (isCustomLayout) {
                                    temprightObjArr1.put(getObj);
                                }
                            }
                            if (getObj.has("amount")) {
                                putObj1.put("amount_" + j, getObj.get("amount"));
                                if (putObj1.has("totalamount")) {
                                    double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                                    temptotal = temptotal + Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                                    putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                } else {
                                    try {
                                        double temptotal = Double.parseDouble(getObj.get("amount").toString());
                                        putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    } catch (Exception e) {
                                        putObj1.put("totalamount", getObj.get("amount"));
                                    }
                                }
                            }
                            jArrR.put(accId, putObj1);
                        }
                    }
                }
            }// end looping Dimensions  
            
            // Put Column Model
            
            JSONObject dimObj = new JSONObject();
            dimObj = new JSONObject();
            dimObj.put("name", "accountcode");
            jarrRecords.put(dimObj);

            dimObj = new JSONObject();
            dimObj.put("name", "accountname");
            jarrRecords.put(dimObj);
            
            if (!paramJobj.has("isExport")) {
            dimObj = new JSONObject();
            dimObj.put("name", "accountflag");
            jarrRecords.put(dimObj);

            dimObj = new JSONObject();
            dimObj.put("name", "isdebit");
            dimObj.put("type", "boolean");
            jarrRecords.put(dimObj);

            dimObj = new JSONObject();
            dimObj.put("name", "level");
            jarrRecords.put(dimObj);

            dimObj = new JSONObject();
            dimObj.put("name", "fmt");
            jarrRecords.put(dimObj);

            dimObj = new JSONObject();
            dimObj.put("name", "leaf");
            jarrRecords.put(dimObj);

            dimObj = new JSONObject();
            dimObj.put("name", "acctype");
            jarrRecords.put(dimObj);

                dimObj = new JSONObject();
                dimObj.put("name", "accountid");
                jarrRecords.put(dimObj);
            }
            if (extrapref.isShowAccountCodeInFinancialReport()) {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<div align=center><b>" + "Account Code" + "</b></div>");
                jobjTemp.put("tip", "Account Code");
                jobjTemp.put("dataIndex", "accountcode");
                jobjTemp.put("width", 200);
                jobjTemp.put("pdfwidth", 200);
                jarrColumns.put(jobjTemp);
            }
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<div align=center><b>" + "Particulars" + "</b></div>");
            jobjTemp.put("tip", "Particulars");
            jobjTemp.put("dataIndex", "accountname");
            jobjTemp.put("width", 200);
            jobjTemp.put("pdfwidth", 200);
            jarrColumns.put(jobjTemp);
            

            for (int k = 0; k < combosearchItems.size(); k++) {
                //put the Record
                dimObj = new JSONObject();
                dimObj.put("name", "amount_" + k);
                jarrRecords.put(dimObj);

                //put Column
                jobjTemp = new JSONObject();
                jobjTemp.put("header","<div align=center><b>"+ combosearchItems.get(k) + " Amount " + currencyname+"</b></div>");
                jobjTemp.put("tip", combosearchItems.get(k) + " Amount " + currencyname);
                jobjTemp.put("dataIndex", "amount_" + k);
                jobjTemp.put("width", 200);
                jobjTemp.put("pdfwidth", 200);
                jobjTemp.put("align", "right");
                jarrColumns.put(jobjTemp);
            }

            //put the Record
            dimObj = new JSONObject();
            dimObj.put("name", "totalamount");
            jarrRecords.put(dimObj);

            //put Column
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<div align=center><b>Total Opening Amount " + currencyname+"</b></div>");
            jobjTemp.put("tip", "Total Amount " + currencyname);
            jobjTemp.put("dataIndex", "totalamount");
            jobjTemp.put("width", 200);
            jobjTemp.put("pdfwidth", 200);
            jarrColumns.put(jobjTemp);

            jobj1.put("jarrColumns", jarrColumns);
            jobj1.put("jarrRecords", jarrRecords);
            jobj1.put("left", jArrL);
            jobj1.put("refleft", AccReportsHandler.getAccountsConvertedJSONArray(paramJobj,jArrL, templeftObjArr,isShowAllAccountsInPnl,"totalamount"));
            jobj1.put("right", jArrR);
            jobj1.put("refright", AccReportsHandler.getAccountsConvertedJSONArray(paramJobj,jArrR, temprightObjArr1,isShowAllAccountsInPnl,"totalamount"));
        } catch (JSONException e) {
            e.printStackTrace();
            throw ServiceException.FAILURE("getMonthlyTradingProfitLossExport : " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return jobj1;
    }
    
 
    //This method is copied from getDimesionBasedProfitLoss(JSONObject paramJobj, boolean monthYearFormat). The difference is this method uses one call to database for fetching the amount for all accounts
    public JSONObject getDimesionBasedProfitLossAllAccounts(JSONObject paramJobj, boolean monthYearFormat) throws ServiceException, SessionExpiredException {
        JSONObject jobj1 = new JSONObject();
        JSONObject jobjTemp = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            String currencyid = paramJobj.getString(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyname = (!StringUtil.isNullOrEmpty(currency.getSymbol())) ? currency.getSymbol() : currency.getName();

            CompanyAccountPreferences pref = null;
            if (paramJobj.has(Constants.preferences)) {
                pref = (CompanyAccountPreferences) paramJobj.get(Constants.preferences);
            } else {
                KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            }
            if (!pref.isShowchild()) {// Anup Check to hide child customer / vendors from balance sheet
                paramJobj.put("hidechildCV", true);
            }

            ExtraCompanyPreferences extrapref = null;
            if (paramJobj.has(Constants.extraCompanyPreferences)) {
                extrapref = (ExtraCompanyPreferences) paramJobj.get(Constants.extraCompanyPreferences);
            } else {
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                    extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                }
            }
            
            boolean isShowAllAccountsInPnl = extrapref.isShowAllAccountsInPnl();
            boolean isCustomLayout = !StringUtil.isNullOrEmpty(paramJobj.optString("isCustomLayout", null)) ? Boolean.parseBoolean(paramJobj.optString("isCustomLayout")) : false;
            JSONObject jArrR = new JSONObject();
            JSONObject jArrL = new JSONObject();
            JSONArray temprightObjArr1 = new JSONArray();
            JSONArray templeftObjArr = new JSONArray();
            int j = 0;
            String Searchjson = null;
            if (paramJobj.optString(Constants.Acc_Search_Json, null) != null) {
                Searchjson = paramJobj.optString(Constants.Acc_Search_Json);
            }

            JSONObject SearchJsonObj = new JSONObject(Searchjson);
            JSONArray SearchJsonArray = SearchJsonObj.getJSONArray("root");
            JSONObject compareObj = SearchJsonArray.optJSONObject(0);

            String iscustomcolumn = compareObj.optString("iscustomcolumn");
            String iscustomcolumndata = compareObj.optString("iscustomcolumndata");
            String fieldtype = compareObj.optString("fieldtype");
            String xtype = compareObj.optString("xtype");
//            String searchText = compareObj.optString("searchText");
            String columnheader = compareObj.optString("columnheader");
            String isfrmpmproduct = compareObj.optString("isfrmpmproduct");
            String searchText = compareObj.optString("searchText");
            String isinterval = compareObj.optString("isinterval");
            String interval = compareObj.optString("interval");
            String isbefore = compareObj.optString("isbefore");
            String isForProductMasterSearch = compareObj.optString("isForProductMasterSearch");
            
            String combosearch = "";
            try {
                combosearch = StringUtil.DecodeText(compareObj.optString("combosearch"));
            } catch (Exception e) {
                combosearch = compareObj.optString("combosearch");
            }
//            List<String> searchTextItems = Arrays.asList(searchText.split("\\s*,\\s*"));
            List<String> combosearchItems = Arrays.asList(combosearch.split("\\s*,\\s*"));
            List<String> searchTextItems = new ArrayList();
            if (!StringUtil.isNullOrEmpty(searchText)) {
                searchTextItems = Arrays.asList(searchText.split("\\s*,\\s*"));
            }

            try {
                combosearch = URLDecoder.decode(compareObj.optString("combosearch"), StaticValues.ENCODING);
            } catch (Exception e) {
                combosearch = compareObj.optString("combosearch");
            }
            try {
                columnheader = URLDecoder.decode(compareObj.optString("columnheader"), StaticValues.ENCODING);
            } catch (Exception e) {
                columnheader = compareObj.optString("columnheader");
            }
//             List<String> searchTextItems = Arrays.asList(searchText.split("\\s*,\\s*"));
//             List<String> combosearchItems = Arrays.asList(combosearch.split("\\s*,\\s*"));
            List<String> columns = new ArrayList<String>();
            int customcolumn = 0;
            if(!StringUtil.isNullOrEmpty(iscustomcolumndata) && Boolean.parseBoolean(iscustomcolumndata)){
                customcolumn = 1;
            }
            HashMap<String, Object> fieldParamRequestMap = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList();
            ArrayList filter_values = new ArrayList();
            filter_names.add("companyid");
//            filter_names.add("customcolumn");
            filter_names.add("fieldlabel");
            filter_values.add(companyid);
            
//            filter_values.add( customcolumn);
            filter_values.add(columnheader);
            fieldParamRequestMap.put("filter_names", filter_names);
            fieldParamRequestMap.put("filter_values", filter_values);
            KwlReturnObject returnObj = accAccountDAOobj.getFieldParams(fieldParamRequestMap);
            boolean isProductCustomData = false;
            boolean isForKnockOff = false;
            // Create map for moduleid and its respective colnum.
            Map<Integer,String> colnumMap = new HashMap<Integer,String>();
            if (returnObj != null && returnObj.getEntityList() != null && !returnObj.getEntityList().isEmpty()) {
                Set<String> colSet = new HashSet<String>();
                for (int i = 0; i < returnObj.getEntityList().size(); i++) {
                    FieldParams fp = (FieldParams) returnObj.getEntityList().get(i);
                    colSet.add("Col" + fp.getColnum());
                    if(fp.getModuleid() == Constants.Acc_Product_Master_ModuleId){
                        isProductCustomData = true;
                    }
                    if (fp.isIsForKnockOff()) {
                        isForKnockOff = true;
                    }
                    //maintan map with moduleid and colnum
                    colnumMap.put(fp.getModuleid(),"Col" + fp.getColnum());
                }
                columns.addAll(colSet);
            }
            JSONArray comparedata = new JSONArray();
            for (int i = 0; i < combosearchItems.size(); i++) {
                JSONObject cntObj = new JSONObject();
                cntObj.put("combosearch", combosearchItems.get(i));
                cntObj.put("columnheader", columnheader);
                cntObj.put("iscustomcolumn", iscustomcolumn);
                cntObj.put("iscustomcolumndata", iscustomcolumndata);
                cntObj.put("fieldtype", fieldtype);
                cntObj.put("xtype", xtype);
                if (searchTextItems.size() > i) {
                    cntObj.put("searchText", searchTextItems.get(i));
                }
                cntObj.put("isfrmpmproduct", isfrmpmproduct);
                comparedata.put(cntObj);
            }

            Map<String, Object> requestParams = new HashMap<String, Object>();
            Date startDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("stdate"));
            Date endDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("enddate"));
            requestParams.put("startdate", startDate);
            requestParams.put("enddate", endDate);
            requestParams.put("companyid", companyid);
            requestParams.put("customdatavalues", combosearchItems);
            requestParams.put("columns", columns);
            requestParams.put("colnumMap", colnumMap);
            requestParams.put("xtype", xtype);
            requestParams.put("fieldtype", fieldtype);
            requestParams.put("iscustomcolumndata", customcolumn);
            requestParams.put("columnheader", columnheader);
            requestParams.put("isForKnockOff", isForKnockOff);
            requestParams.put("isProductCustomData", isProductCustomData);
            if (paramJobj.has("costcenter") && !StringUtil.isNullOrEmpty(paramJobj.getString("costcenter"))) {
                requestParams.put("costcenter", paramJobj.getString("costcenter"));
            }
            Map<String, Double> accAmtMap = accReportsService.getPeriodAccountAmountMap(requestParams);
            Map<String, List<Account>> accGroupMap = accReportsService.getGroupAccountMap(paramJobj);
            for (j = 0; j < comparedata.length(); j++) {
//                System.out.println("idx: " + j);
//                long start = System.currentTimeMillis();
                JSONArray dimSearchJsonArr = new JSONArray();
                JSONObject dimJson = comparedata.getJSONObject(j);
//                dimJson.put("searchText", searchText);
                dimJson.put("isinterval", isinterval);
                dimJson.put("interval", interval);
                dimJson.put("isbefore", isbefore);
                dimJson.put("isForProductMasterSearch", isForProductMasterSearch);
                dimSearchJsonArr.put(dimJson);
                JSONObject putSearchJson = new JSONObject();
                putSearchJson.put("root", dimSearchJsonArr);
                //  search JSON Array for Dimension Based report
                paramJobj.put("DimensionBasedComparisionReport", "DimensionBasedComparisionReport");
                paramJobj.put("DimensionBasedSearchJson", putSearchJson);
                paramJobj.put("monthlyreport", "MonthlyReport");
                paramJobj.put("isPeriod", true);
                JSONObject jobj = new JSONObject();
                if (!isCustomLayout) {// call for dimension based PnL
                    paramJobj.put("customvaluepassed", dimJson.getString("combosearch"));
                    jobj = getTradingAndProfitLossAllAccounts(paramJobj, accAmtMap);
                } else {// call for dimension based Custom PnL
                    //                    jobj = getBSorPL_CustomLayout(request, extrapref, companyid);
                    jobj = getBSorPL_CustomLayout(paramJobj, extrapref, companyid);
                }
                JSONObject jobj2 = jobj.getJSONObject(Constants.RES_data);
                JSONArray rightObjArr1 = jobj2.getJSONArray("right");
                JSONArray leftObjArr = jobj2.getJSONArray("left");
                if (j == 0) {
                    temprightObjArr1 = jobj2.getJSONArray("right");
                    templeftObjArr = jobj2.getJSONArray("left");
                }

                for (int i = 0; i < leftObjArr.length(); i++) {
                    JSONObject getObj = leftObjArr.getJSONObject(i);
                    String accId = "";
                    if (getObj.has("accountid")) {
                        accId = getObj.getString("accountid");
                    }
                    if (!accId.equals("")) {
                        if (j == 0) {
                            JSONObject putObj = new JSONObject();
                            if (getObj.has("accountname")) {
                                putObj.put("accountname", getObj.get("accountname"));
                            }
                            if (getObj.has("accountcode")) {
                                putObj.put("accountcode", getObj.get("accountcode"));
                            }
                            if (getObj.has("haschild")) {
                                putObj.put("haschild", getObj.get("haschild"));
                            }
                            if (getObj.has("acctype")) {
                                putObj.put("acctype", getObj.getString("acctype"));
                            }
                            if (getObj.has("level")) {
                                putObj.put("level", getObj.get("level"));
                            }
                            if (getObj.has("fmt")) {
                                putObj.put("fmt", getObj.get("fmt"));
                            }
                            if (getObj.has("accountflag")) {
                                putObj.put("accountflag", getObj.get("accountflag"));
                            }
                            if (getObj.has("leaf")) {
                                putObj.put("leaf", getObj.getBoolean("leaf"));
                            }
                            if (getObj.has("isdebit")) {
                                putObj.put("isdebit", getObj.get("isdebit"));
                            }
                            if (getObj.has("amount") && !isCustomLayout) {
                                try {
                                    double temptotal = Double.parseDouble((getObj.get("amount").toString()));
                                    putObj.put("amount_0", authHandler.formattedAmount(temptotal, companyid));
                                    putObj.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                } catch (Exception e) {
                                    putObj.put("amount_0", getObj.get("amount"));
                                    putObj.put("totalamount", getObj.get("amount"));
                                }
                            } else if (getObj.has("periodamount") && isCustomLayout) {
                                try {
                                    double temptotal = Double.parseDouble((getObj.get("periodamount").toString()));
                                    if (temptotal != 0.0) {
                                        putObj.put("amount_0", authHandler.formattedAmount(temptotal, companyid));
                                        putObj.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    } else {
                                        putObj.put("amount_0", getObj.get("periodamount"));
                                        putObj.put("totalamount", getObj.get("periodamount"));
                                    }
                                } catch (Exception e) {
                                    putObj.put("amount_0", getObj.get("periodamount"));
                                    putObj.put("totalamount", getObj.get("periodamount"));
                                }
                            }
                            putObj.put("accountid", accId);
                            jArrL.put(accId, putObj);
                        } else {
                            JSONObject putObj1 = new JSONObject();
                            if (jArrL.has(accId)) {
                                putObj1 = jArrL.getJSONObject(accId);
                            } else {
                                if (getObj.has("accountname")) {
                                    putObj1.put("accountname", getObj.get("accountname"));
                                }
                                if (getObj.has("accountcode")) {
                                    putObj1.put("accountcode", getObj.get("accountcode"));
                                }
                                if (getObj.has("haschild")) {
                                    putObj1.put("haschild", getObj.get("haschild"));
                                }
                                if (getObj.has("acctype")) {
                                    putObj1.put("acctype", getObj.getString("acctype"));
                                }
                                if (getObj.has("level")) {
                                    putObj1.put("level", getObj.get("level"));
                                }
                                if (getObj.has("fmt")) {
                                    putObj1.put("fmt", getObj.get("fmt"));
                                }
                                if (getObj.has("accountflag")) {
                                    putObj1.put("accountflag", getObj.get("accountflag"));
                                }
                                if (getObj.has("leaf")) {
                                    putObj1.put("leaf", getObj.getBoolean("leaf"));
                                }
                                if (getObj.has("isdebit")) {
                                    putObj1.put("isdebit", getObj.get("isdebit"));
                                }
                                putObj1.put("accountid", accId);
                                if (getObj.get("accountname").toString().equals("Net Loss")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (!check.optString("accountname", "").toString().equals("Net Loss") && !check1.optString("accountname", "").toString().equals("Net Loss")) {
                                        templeftObjArr.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").toString().equals("Net Profit")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (!check.optString("accountname", "").toString().equals("Net Profit") && !check1.optString("accountname", "").toString().equals("Net Profit")) {
                                        templeftObjArr.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").toString().equals("Gross Loss")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (!check.optString("accountname", "").toString().equals("Gross Loss") && !check1.optString("accountname", "").toString().equals("Gross Loss")) {
                                        templeftObjArr.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").toString().equals("Gross Profit")) {
                                    JSONObject check = templeftObjArr.getJSONObject(templeftObjArr.length() - 1);
                                    JSONObject check1 = templeftObjArr.getJSONObject(templeftObjArr.length() - 2);
                                    if (!check.optString("accountname", "").toString().equals("Gross Profit") && !check1.optString("accountname", "").toString().equals("Gross Profit")) {
                                        templeftObjArr.put(getObj);
                                    }
                                }
                                if (isCustomLayout) {
                                    templeftObjArr.put(getObj);
                                }
                            }
                            if (getObj.has("amount") && !isCustomLayout) {
                                try {
                                    double temptotal = Double.parseDouble((getObj.get("amount").toString()));
                                    putObj1.put("amount_" + j, authHandler.formattedAmount(temptotal, companyid));
                                } catch (Exception e) {
                                    putObj1.put("amount_" + j, getObj.get("amount"));
                                }

                                if (putObj1.has("totalamount")) {
                                    if (!isCustomLayout) {
                                        double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                                        temptotal = temptotal + Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                                        putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    } else if (!putObj1.get("totalamount").toString().equals("")) {
                                        double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                                        temptotal = temptotal + Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                                        putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    }
                                } else {
                                    try {
                                        double temptotal = Double.parseDouble((getObj.get("amount").toString()));
                                        putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    } catch (Exception e) {
                                        putObj1.put("totalamount", getObj.get("amount"));
                                    }
                                }
                            } else if (getObj.has("periodamount") && isCustomLayout) {
                                try {
                                    double temptotal = Double.parseDouble((getObj.get("periodamount").toString()));
                                    putObj1.put("amount_" + j, authHandler.formattedAmount(temptotal, companyid));
                                } catch (Exception e) {
                                    putObj1.put("amount_" + j, getObj.get("periodamount"));
                                }
                                if (putObj1.has("totalamount")) {
                                    if (!StringUtil.isNullOrEmpty(getObj.optString("periodamount"))) {
                                        double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                                        temptotal = temptotal + Double.parseDouble((getObj.get("periodamount").toString().equals("") || getObj.get("periodamount").toString().equals("-") || getObj.get("periodamount").toString().contains("<")) ? "0.0" : (getObj.get("periodamount").toString()));
                                        putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    }
                                } else {
                                    try {
                                        if (!StringUtil.isNullOrEmpty(getObj.optString("periodamount"))) {
                                            double temptotal = Double.parseDouble((getObj.get("periodamount").toString()));
                                            putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                        }
                                    } catch (Exception e) {
                                        putObj1.put("totalamount", getObj.get("periodamount"));
                                    }
                                }
                            }
                            jArrL.put(accId, putObj1);
                        }
                    }
                }

                for (int i = 0; i < rightObjArr1.length(); i++) {
                    JSONObject getObj = rightObjArr1.getJSONObject(i);
                    String accId = "";
                    if (getObj.has("accountid")) {
                        accId = getObj.getString("accountid");
                    }
                    if (!accId.equals("")) {
                        if (j == 0) {
                            JSONObject putObj = new JSONObject();
                            if (getObj.has("accountname")) {
                                putObj.put("accountname", getObj.get("accountname"));
                            }
                            if (getObj.has("accountcode")) {
                                putObj.put("accountcode", getObj.get("accountcode"));
                            }
                            if (getObj.has("haschild")) {
                                putObj.put("haschild", getObj.get("haschild"));
                            }
                            if (getObj.has("acctype")) {
                                putObj.put("acctype", getObj.getString("acctype"));
                            }
                            if (getObj.has("level")) {
                                putObj.put("level", getObj.get("level"));
                            }
                            if (getObj.has("fmt")) {
                                putObj.put("fmt", getObj.get("fmt"));
                            }
                            if (getObj.has("accountflag")) {
                                putObj.put("accountflag", getObj.get("accountflag"));
                            }
                            if (getObj.has("leaf")) {
                                putObj.put("leaf", getObj.getBoolean("leaf"));
                            }
                            if (getObj.has("isdebit")) {
                                putObj.put("isdebit", getObj.get("isdebit"));
                            }
                            if (getObj.has("amount")) {
                                try {
                                    double temptotal = Double.parseDouble((getObj.get("amount").toString()));
                                    putObj.put("amount_0", authHandler.formattedAmount(temptotal, companyid));
                                    putObj.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                } catch (Exception e) {
                                    putObj.put("amount_0", getObj.get("amount"));
                                    putObj.put("totalamount", getObj.get("amount"));
                                }
                            }
                            putObj.put("accountid", accId);
                            jArrR.put(accId, putObj);
                        } else {
                            JSONObject putObj1 = new JSONObject();
                            if (jArrR.has(accId)) {
                                putObj1 = jArrR.getJSONObject(accId);
                            } else {
                                if (getObj.has("accountname")) {
                                    putObj1.put("accountname", getObj.get("accountname"));
                                }
                                if (getObj.has("accountcode")) {
                                    putObj1.put("accountcode", getObj.get("accountcode"));
                                }
                                if (getObj.has("haschild")) {
                                    putObj1.put("haschild", getObj.get("haschild"));
                                }
                                if (getObj.has("acctype")) {
                                    putObj1.put("acctype", getObj.getString("acctype"));
                                }
                                if (getObj.has("level")) {
                                    putObj1.put("level", getObj.get("level"));
                                }
                                if (getObj.has("fmt")) {
                                    putObj1.put("fmt", getObj.get("fmt"));
                                }
                                if (getObj.has("accountflag")) {
                                    putObj1.put("accountflag", getObj.get("accountflag"));
                                }
                                if (getObj.has("leaf")) {
                                    putObj1.put("leaf", getObj.getBoolean("leaf"));
                                }
                                if (getObj.has("isdebit")) {
                                    putObj1.put("isdebit", getObj.get("isdebit"));
                                }
                                putObj1.put("accountid", accId);
                                if (getObj.get("accountname").equals("Net Loss")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (!check.optString("accountname", "").toString().equals("Net Loss") && !check1.optString("accountname", "").toString().equals("Net Loss")) {
                                        temprightObjArr1.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").equals("Net Profit")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (!check.optString("accountname", "").toString().equals("Net Profit") && !check1.optString("accountname", "").toString().equals("Net Profit")) {
                                        temprightObjArr1.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").equals("Gross Loss")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (!check.optString("accountname", "").toString().equals("Gross Loss") && !check1.optString("accountname", "").toString().equals("Gross Loss")) {
                                        temprightObjArr1.put(getObj);
                                    }
                                }
                                if (getObj.get("accountname").equals("Gross Profit")) {
                                    JSONObject check = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 1);
                                    JSONObject check1 = temprightObjArr1.getJSONObject(temprightObjArr1.length() - 2);
                                    if (!check.optString("accountname", "").toString().equals("Gross Profit") && !check1.optString("accountname", "").toString().equals("Gross Profit")) {
                                        temprightObjArr1.put(getObj);
                                    }
                                }
                                if (isCustomLayout) {
                                    temprightObjArr1.put(getObj);
                                }
                            }
                            if (getObj.has("amount")) {
                                putObj1.put("amount_" + j, getObj.get("amount"));
                                if (putObj1.has("totalamount")) {
                                    double temptotal = Double.parseDouble((putObj1.get("totalamount").toString().equals("") || putObj1.get("totalamount").toString().equals("-") || putObj1.get("totalamount").toString().contains("<")) ? "0.0" : (putObj1.get("totalamount").toString()));
                                    temptotal = temptotal + Double.parseDouble((getObj.get("amount").toString().equals("") || getObj.get("amount").toString().equals("-") || getObj.get("amount").toString().contains("<")) ? "0.0" : (getObj.get("amount").toString()));
                                    putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                } else {
                                    try {
                                        double temptotal = Double.parseDouble(getObj.get("amount").toString());
                                        putObj1.put("totalamount", authHandler.formattedAmount(temptotal, companyid));
                                    } catch (Exception e) {
                                        putObj1.put("totalamount", getObj.get("amount"));
                                    }
                                }
                            }
                            jArrR.put(accId, putObj1);
                        }
                    }
                }
//                long end = System.currentTimeMillis();
//                System.out.println(" Time : " + ((end - start) / 1000));
            }// end looping Dimensions  
            
            // Put Column Model
            JSONObject dimObj = new JSONObject();
            if (extrapref.isShowAccountCodeInFinancialReport()) {
                dimObj = new JSONObject();
                dimObj.put("name", "accountcode");
                jarrRecords.put(dimObj);
            }

            dimObj = new JSONObject();
            dimObj.put("name", "accountname");
            jarrRecords.put(dimObj);

            if (!paramJobj.has("isExport")) {
                dimObj = new JSONObject();
                dimObj.put("name", "accountflag");
                jarrRecords.put(dimObj);

                dimObj = new JSONObject();
                dimObj.put("name", "isdebit");
                dimObj.put("type", "boolean");
                jarrRecords.put(dimObj);

                dimObj = new JSONObject();
                dimObj.put("name", "level");
                jarrRecords.put(dimObj);

                dimObj = new JSONObject();
                dimObj.put("name", "fmt");
                jarrRecords.put(dimObj);

                dimObj = new JSONObject();
                dimObj.put("name", "leaf");
                jarrRecords.put(dimObj);

                dimObj = new JSONObject();
                dimObj.put("name", "acctype");
                jarrRecords.put(dimObj);

                dimObj = new JSONObject();
                dimObj.put("name", "accountid");
                jarrRecords.put(dimObj);
            }
            if (extrapref.isShowAccountCodeInFinancialReport()) {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<div align=center><b>" + "Account Code" + "</b></div>");
                jobjTemp.put("tip", "Account Code");
                jobjTemp.put("dataIndex", "accountcode");
                jobjTemp.put("width", 200);
                jobjTemp.put("pdfwidth", 200);
                jarrColumns.put(jobjTemp);
            }

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<div align=center><b>" + "Particulars" + "</b></div>");
            jobjTemp.put("tip", "Particulars");
            jobjTemp.put("dataIndex", "accountname");
            jobjTemp.put("width", 200);
            jobjTemp.put("pdfwidth", 200);
            jarrColumns.put(jobjTemp);

            for (int k = 0; k < combosearchItems.size(); k++) {
                //put the Record
                dimObj = new JSONObject();
                dimObj.put("name", "amount_" + k);
                jarrRecords.put(dimObj);

                //put Column
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<div align=center><b>" + combosearchItems.get(k) + " Amount " + currencyname + "</b></div>");
                jobjTemp.put("tip", combosearchItems.get(k) + " Amount " + currencyname);
                jobjTemp.put("dataIndex", "amount_" + k);
                jobjTemp.put("width", 200);
                jobjTemp.put("pdfwidth", 200);
                jobjTemp.put("align", "right");
                jarrColumns.put(jobjTemp);
            }

            //put the Record
            dimObj = new JSONObject();
            dimObj.put("name", "totalamount");
            jarrRecords.put(dimObj);

            //put Column
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<div align=center><b>"+ messageSource.getMessage("acc.conslodation.totalperiodamount", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " " + currencyname + "</b></div>");
            jobjTemp.put("tip", "Total Amount " + currencyname);
            jobjTemp.put("dataIndex", "totalamount");
            jobjTemp.put("width", 200);
            jobjTemp.put("pdfwidth", 200);
            jarrColumns.put(jobjTemp);

            jobj1.put("jarrColumns", jarrColumns);
            jobj1.put("jarrRecords", jarrRecords);
            jobj1.put("left", jArrL);
            jobj1.put("refleft", AccReportsHandler.getAccountsConvertedJSONArray(paramJobj,jArrL, templeftObjArr, isShowAllAccountsInPnl, "totalamount"));
            jobj1.put("right", jArrR);
            jobj1.put("refright", AccReportsHandler.getAccountsConvertedJSONArray(paramJobj,jArrR, temprightObjArr1, isShowAllAccountsInPnl, "totalamount"));
        } catch (JSONException e) {
            e.printStackTrace();
            throw ServiceException.FAILURE("getDimesionBasedProfitLossAllAccounts : " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobj1;
    }
 
@Override
    public JSONObject getMonthwiseGeneralLedgerReport(JSONObject paramJobj, boolean isExport) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
//            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            HashMap<String, Object> requestParams = accAccountHandler.getJsonMap(paramJobj);
            String selectedAccountIds = paramJobj.optString("accountIds", null);
            if (!StringUtil.isNullOrEmpty(selectedAccountIds)) {
                requestParams.put("selectedAccountIds", selectedAccountIds);
            }
            LocalDate localStartDate = null;
            LocalDate localEndDate = null;
            Date startDate = new Date(0);
            Date endDate = new Date();
            String sDate = paramJobj.optString("startDate", null);
            String eDate = paramJobj.optString("endDate", null);
            if (!StringUtil.isNullOrEmpty(sDate)) {
                startDate = authHandler.getDateOnlyFormat().parse(sDate);
            }
            if (!StringUtil.isNullOrEmpty(eDate)) {
                endDate = authHandler.getDateOnlyFormat().parse(eDate);
            }

            HashMap<String, Object> hashMap = new HashMap();
            hashMap.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
            List list = result.getEntityList();
            Map<String, Object> accountJsonparamMap = new HashMap<String, Object>();
            JSONObject jobj1 = getAccountJson(paramJobj, list, accCurrencyDAOobj, accountJsonparamMap);
            JSONArray jSONArray = jobj1.getJSONArray(Constants.RES_data);
            JSONArray newJSONArray = new JSONArray();
            for (int count = 0; count < jSONArray.length(); count++) {
                if (jSONArray.getJSONObject(count).has("accid")) {
                    String accId = jSONArray.getJSONObject(count).getString("accid");
                    KwlReturnObject accountResult = accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Account", accId);
                    if (accountResult.getEntityList().get(0) != null) {
                        Account account = (Account) accountResult.getEntityList().get(0);
                        if (!account.isDeleted()) {
                            localStartDate = new LocalDate(startDate);
                            localEndDate = new LocalDate(endDate);
                            while (localStartDate.isBefore(localEndDate) || localStartDate.isEqual(localEndDate)) {
                                String monthName = localStartDate.toString("MMM yyyy");
                                DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                                DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                                Date monthEndDate = lastDateOfMonth.toDate();
                                JSONObject objNew = new JSONObject();
                                objNew.put("accname", jSONArray.getJSONObject(count).opt("accname"));
                                objNew.put("acccode", jSONArray.getJSONObject(count).opt("acccode"));
                                objNew.put("glcode", monthName);
                                paramJobj.put("isPeriod", true);
                                double mtd = accReportsService.getAccountBalanceWithOutClosing(paramJobj, account.getID(), localStartDate.toDate(), monthEndDate,null);
                                objNew.put("mtd", mtd);
                                double ytd = accReportsService.getAccountBalanceWithOutClosing(paramJobj, account.getID(), startDate, monthEndDate,null);
                                objNew.put("ytd", ytd);
                                paramJobj.remove("isPeriod");
                                newJSONArray.put(objNew);
                                localStartDate = localStartDate.plus(Period.months(1));
                            }
                        }
                    }
                }
            }

            // Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            HashMap<String, Object> params = new HashMap<>();
            params.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            params.put("locale", requestParams.get("locale"));
            createColumnModelForMonthwiseGeneralLedgerReport(jarrColumns, jarrRecords, params);

            // Column Model
            commData.put(Constants.RES_success, true);
            commData.put("coldata", newJSONArray);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", newJSONArray.length());
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);

            jobj.put("valid", true);
            if (isExport) {
                jobj.put(Constants.RES_data, newJSONArray);
            } else {
                jobj.put(Constants.RES_data, commData);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("getMonthwiseGeneralLedgerReport : " + e.getMessage(), e);
        }
        return jobj;
    }

    public void createColumnModelForMonthwiseGeneralLedgerReport(JSONArray jarrColumns, JSONArray jarrRecords, HashMap<String, Object> params) throws JSONException, ServiceException {
        JSONObject jobjTemp = new JSONObject();
        jobjTemp.put("name", "glcode");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "desc");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "acccode");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "accountid");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "accname");
        jarrRecords.put(jobjTemp);

        //put the Record
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "mtd");
        jarrRecords.put(jobjTemp);

        //put the Record
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "ytd");
        jarrRecords.put(jobjTemp);
    }
    
    @Override
    public double getAccountBalanceInOriginalCurrency(JSONObject paramJobj, String accountid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException, JSONException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        requestParams.put("costcenter", paramJobj.optString("costcenter", null));
        requestParams.put("tocurrencyid", paramJobj.optString("tocurrencyid", null));
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json, null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(InvoiceConstants.Filter_Criteria, null));
        requestParams.put("templatecode", (StringUtil.isNullOrEmpty(paramJobj.optString("templatecode", null))) ? -1 : Integer.parseInt(paramJobj.getString("templatecode")));
        String selectedCurrencyIds = paramJobj.optString("currencyIds", null);
        if (!StringUtil.isNullOrEmpty(selectedCurrencyIds)) {
            requestParams.put("currencyFlag", true);
            requestParams.put("selectedCurrencyIds", selectedCurrencyIds);
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isGeneralLedger", null))) {
            boolean isGeneralLedger = Boolean.parseBoolean(paramJobj.optString("isGeneralLedger"));
            requestParams.put("generalLedgerFlag", isGeneralLedger);
        }
        if(paramJobj.optInt("accountTransactionType", Constants.All_Transaction_TypeID)!=Constants.All_Transaction_TypeID){
             requestParams.put("accountTransactionType", paramJobj.optInt("accountTransactionType", Constants.All_Transaction_TypeID));
        }
        return getAccountBalanceInOriginalCurrency(paramJobj, requestParams, accountid, startDate, endDate);
    }

    
    public double getAccountBalanceInOriginalCurrency(JSONObject paramJobj, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate) throws ServiceException {
        double amount = 0;
        try {
            KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
            Account account = (Account) accresult.getEntityList().get(0);

            int templatecode = (Integer) requestParams.get("templatecode");
            String tocurrencyid = requestParams.containsKey("tocurrencyid") && requestParams.get("tocurrencyid") != null ? (String) requestParams.get("tocurrencyid") : account.getCurrency().getCurrencyID();
            boolean convertOBFlag = requestParams.containsKey("tocurrencyid") && requestParams.get("tocurrencyid") != null ? true : false;//No need to convert opening balance in case of tocurrencyid = acc currency as opening balance value enetered is in account currnecy only
            int accountTransactionType = requestParams.containsKey("accountTransactionType") && requestParams.get("accountTransactionType")!=null?Integer.parseInt(requestParams.get("accountTransactionType").toString()):Constants.All_Transaction_TypeID;//It will be zero for all otherwise it value will be transaction type value given in constant
            boolean isPeriod = paramJobj.optString("isPeriod", null) != null ? Boolean.parseBoolean(paramJobj.optString("isPeriod")) : false;
            String costCenterId = requestParams.containsKey("costcenter") && requestParams.get("costcenter") != null ? (String) requestParams.get("costcenter") : "";
            if ((templatecode == -1) || (account.getTemplatepermcode() != null && account.getTemplatepermcode() != 0 && ((templatecode & account.getTemplatepermcode()) == templatecode))) {
                if (StringUtil.isNullOrEmpty(costCenterId)) { //Don't consider opening balance for CostCenter
                    if (startDate != null && ((startDate.before(account.getCreationDate()) || startDate.equals(account.getCreationDate())) && endDate.after(account.getCreationDate()) || endDate.equals(account.getCreationDate()))) {
                        double accountOpeningBalanceInBase = accInvoiceCommon.getOpeningBalanceOfAccountJson(paramJobj, account, false, null);
                        amount = accountOpeningBalanceInBase;//account.getOpeningBalance();
                        boolean accountHasOpeningTransactions = false; //accInvoiceCommon.accountHasOpeningTransactions(request, account, false, null);
                        boolean isCustomer = false;
                        boolean isVendor = false;
                        boolean isDepreciationAccount = false;
                        boolean isAssetPurchaseAccount = false;
                        if (account != null && account.getUsedIn() != null) {
                            if (account.getUsedIn().contains(Constants.Customer_Default_Account)) {
                                isCustomer = true;
                            } else if (account.getUsedIn().contains(Constants.Vendor_Default_Account)) {
                                isVendor = true;
                            } else if (account.getUsedIn().equals(Constants.Depreciation_Provision_GL_Account)) {
                                isDepreciationAccount = true;
                            }else if (account.getUsedIn().contains(Constants.Product_Sales_Return_Account) && account.getUsedIn().contains(Constants.Product_Purchase_Return_Account)) { 
                                isAssetPurchaseAccount = true;
                            }
                        }
                        if (isCustomer || isVendor || isDepreciationAccount || isAssetPurchaseAccount) {
//                            accountHasOpeningTransactions = accInvoiceCommon.accountHasOpeningTransactions(request, account, false, null);
                            accountHasOpeningTransactions = accInvoiceCommon.accountHasOpeningTransactionsJson(paramJobj, account, false, null);
                        }
                        
                        if(!accountHasOpeningTransactions && accountTransactionType!=Constants.All_Transaction_TypeID){// GL Report case: Report filtered based on transaction type in that case if there is no opening transaction in that case opening amount will be zero.
                            amount=0;
                        } else if (!accountHasOpeningTransactions) {
                            amount = account.getOpeningBalance();
                            boolean isSplitOpeningBalanceAmount = accInvoiceCommon.isSplitOpeningBalanceAmount(paramJobj.getString(Constants.companyKey));
                            boolean isSplitOpeningBalanceSearch = accInvoiceCommon.isSplitOpeningBalanceSearch(paramJobj.optString(Constants.Acc_Search_Json),paramJobj.getString(Constants.companyKey));
                            if (isSplitOpeningBalanceAmount && isSplitOpeningBalanceSearch) {
                                if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.Acc_Search_Json,null))) {
                                    List l = accInvoiceCommon.getSplitOpeningBalance(paramJobj.optString(Constants.Acc_Search_Json), account.getID(),paramJobj.getString(Constants.companyKey));
                                    if (l.size() > 0) {
                                        boolean issearch = Boolean.parseBoolean(l.get(0).toString());
                                        if (issearch) {
                                            amount = Double.parseDouble(l.get(1).toString());
                                        }
                                    }
                                }
                            }
                        } else {
                            KwlReturnObject cresult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, account.getCurrency().getCurrencyID(), account.getCreationDate(), 0);
                            amount = (Double) cresult.getEntityList().get(0);
                        }
                        if (convertOBFlag) {
                            String fromcurrencyid = account.getCurrency().getCurrencyID();
                            KwlReturnObject crresult = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amount, fromcurrencyid, tocurrencyid, account.getCreationDate(), 0);
                            amount = (Double) crresult.getEntityList().get(0);
                        }
                    }

                }
            }
            String Searchjson = "";

            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey(InvoiceConstants.Filter_Criteria) && requestParams.get(InvoiceConstants.Filter_Criteria) != null) {
                if (requestParams.get(InvoiceConstants.Filter_Criteria).toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            if (requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null) {
                Searchjson = requestParams.get(Constants.Acc_Search_Json).toString();
            }
            if (!isPeriod && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.companyKey))) {
                KwlReturnObject prefresult = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), paramJobj.optString(Constants.companyKey));
                CompanyAccountPreferences pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), paramJobj.optString(Constants.companyKey));
                ExtraCompanyPreferences extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                if (account != null && !StringUtil.isNullOrEmpty(extrapref.getProfitLossAccountId()) && extrapref.getProfitLossAccountId().equals(account.getID())) {
                    Date excludedPreviousYearDate = accReportsService.getDateForExcludePreviousYearBalanceFilter(paramJobj, endDate);
                    Date previousFYEndDate = new DateTime(excludedPreviousYearDate).minusDays(1).toDate();
                    double closedYrNetProfitAndLoss = accReportsService.getClosedYearNetProfitAndLoss(previousFYEndDate, pref, extrapref, paramJobj.optString(Constants.companyKey));
                    KwlReturnObject cresult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, closedYrNetProfitAndLoss, account.getCurrency().getCurrencyID(), account.getCreationDate(), 0);
                    amount += (Double) cresult.getEntityList().get(0);
                }
            }
            if(StringUtil.isNullOrEmpty(Searchjson) && account.getCompany().isOptimizedflag() && (templatecode == -1)) {
                KwlReturnObject abresult = accJournalEntryobj.getAccountBalance_optimized(accountid, startDate, endDate, costCenterId);
                List list = abresult.getEntityList();
                if(list.size() > 0 && list.get(0) != null) {
                    amount += (Double) list.get(0);
                }
            } else {
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject abresult = accJournalEntryobj.getAccountBalance(requestParams, accountid, startDate, endDate, costCenterId, filterConjuctionCriteria, Searchjson);
            //KwlReturnObject abresult = accJournalEntryobj.getAccountBalanceAmount(requestParams, accountid, startDate, endDate, costCenterId, filterConjuctionCriteria, Searchjson);
            List list = abresult.getEntityList();
            Iterator itr = list.iterator();
            if (requestParams.containsKey("generalLedgerFlag") && requestParams.get("generalLedgerFlag") != null) {
                boolean generalLedgerFlag = Boolean.parseBoolean(requestParams.get("generalLedgerFlag").toString());
                if (generalLedgerFlag && !StringUtil.isNullOrEmpty(Searchjson)) {
                    /*
                     * code in this if is done for:- 1)do not include opening
                     * balance of account 2) do not include opening trasactions
                     * amount of customer/vendor mapped with this current
                     * account when advanced search is performed on dimension
                     * 3)when advanced search is performed on dimension then
                     * documents are considered for calculation are - saved with
                     * that dimension.
                     */
                    amount = 0;
                }
            }
           while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JournalEntryDetail jed = (JournalEntryDetail) row[1];
                if ((templatecode == -1) || (jed.getJournalEntry().getTemplatepermcode() != null && jed.getJournalEntry().getTemplatepermcode() != 0 && ((templatecode & jed.getJournalEntry().getTemplatepermcode()) == templatecode))) {
                    if (jed.getJournalEntry().getIsReval() == 0) {
                        String fromcurrencyid = (jed.getJournalEntry().getCurrency() == null ? gcurrencyid : jed.getJournalEntry().getCurrency().getCurrencyID());
                        //            amount += CompanyHandler.getCurrencyToBaseAmount(session, request, ((Double) row[0]).doubleValue(), fromcurrencyid, jed.getJournalEntry().getEntryDate());
                        KwlReturnObject crresult = null;//if Same currency then use - getOneCurrencyToOther()
                        if (fromcurrencyid.equalsIgnoreCase(tocurrencyid)) {
                            crresult = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, ((Double) row[0]).doubleValue(), fromcurrencyid, tocurrencyid, jed.getJournalEntry().getEntryDate(), jed.getJournalEntry().getExternalCurrencyRate());
                        } else {
                            crresult = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, ((Double) row[0]).doubleValue(), fromcurrencyid, tocurrencyid, jed.getJournalEntry().getEntryDate(), jed.getJournalEntry().getExternalCurrencyRate());
                        }

                        amount += (Double) crresult.getEntityList().get(0);
                    }
                }
            }
//            if (list.get(0) != null) {
//              amount += (Double) list.get(0);
//        }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAccountBalanceInOriginalCurrency : " + ex.getMessage(), ex);
        }
        return amount;
    } 
    
    @Override
    public JSONObject getAmountsForCashFlowStatementAsPerCOA(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray amtArray = new JSONArray();
            double pretaxprofit = 0;
            double provision = 0;
            double depreciation = 0;
            double lossondisp = 0;
            double gainondisp = 0;
            double divincome = 0;
            double consume = 0;
            double tradedebtors = 0;
            double otherdebtors = 0;
            double tradecreditors = 0;
            double othercreditors = 0;
            double perchasefixass = 0;
            double acqofinvest = 0;
            double proinsssha = 0;
            double hpfinance = 0;
            double termloan = 0;
            double cashbalbegin = 0;
            double cashbalnet = 0;

            boolean noactivity = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);

            HashMap<String, Object> companyPriceListParams = new HashMap<String, Object>();
            companyPriceListParams.put("isPurchase", true);
            KwlReturnObject kwlCompanyMaxDateProductPriceList = accProductObj.getAllProductsMaxAppliedDatePriceDetails(companyid, companyPriceListParams);
            Map<String, Object[]> companyMaxDateProductPriceList = AccReportsHandler.getcompanyMaxDateProductPriceListMap(kwlCompanyMaxDateProductPriceList.getEntityList());

            Date inventoryOpeningBalanceDate = null;
            KwlReturnObject rtObj = accProductObj.getInventoryOpeningBalanceDate(companyid);
            List<Date> lst = rtObj.getEntityList();
            for (Date dateObj : lst) {
                inventoryOpeningBalanceDate = dateObj;
            }
            Date stDate = new Date(request.getParameter("stdate"));
            Date endDate = new Date(request.getParameter("enddate"));
            CompanyAccountPreferences pref = null;
            KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            Calendar calendar = Calendar.getInstance();
            if (stDate != null) {
                calendar.setTime(stDate);
            } else {
                calendar.setTime(endDate);
            }
            KwlReturnObject closingAccountBalanceResultList = accCompanyPreferencesObj.getClosingBalanceList(null, (calendar.get(Calendar.YEAR) - 1), companyid);
            List<ClosingAccountBalance> closingAccountBalancesList = closingAccountBalanceResultList.getEntityList();
            HashMap<String, Date> startEndDateHashMap = accReportsService.getStartAndEndFinancialDate(pref, calendar.get(Calendar.YEAR) - 1);
            Date closingStartDate = startEndDateHashMap.get("startDate");
            Date closingEndDate = startEndDateHashMap.get("endDate");
            boolean stockValuationFlag = true;
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                stockValuationFlag = extrapref.isStockValuationFlag();
            }
            double invCloseBal = 0, invOpeBal = 0;
            if (stockValuationFlag) {
                com.krawler.utils.json.base.JSONObject jObjX = accReportsService.getInventoryOpeningBalance(request, companyid, stDate, companyMaxDateProductPriceList, inventoryOpeningBalanceDate);
                JSONArray jarr = jObjX.getJSONArray(Constants.RES_data);
                if (jarr.length() > 0) {
                    com.krawler.utils.json.base.JSONObject jobj1 = jarr.getJSONObject(0);
                    invOpeBal = jobj1.has("valuation") ? jobj1.getDouble("valuation") : 0;
                }
                invOpeBal = authHandler.round(invOpeBal, companyid);
                //As we have to hide opening balance in balance setting invOpeBal & preinvOpeBal to Zero 
                invOpeBal = 0;
                jObjX = new com.krawler.utils.json.base.JSONObject();
                jObjX = accReportsService.getInventoryOpeningBalance(request, companyid, endDate, companyMaxDateProductPriceList, inventoryOpeningBalanceDate);
                jarr = jObjX.getJSONArray(Constants.RES_data);
                if (jarr.length() > 0) {
                    com.krawler.utils.json.base.JSONObject jobj1 = jarr.getJSONObject(0);
                    invCloseBal = jobj1.has("valuation") ? jobj1.getDouble("valuation") : 0;
                }
                invCloseBal = authHandler.round(invCloseBal, companyid);
            }

            HashMap<String, List<Account>> accountGroupMap = new HashMap<String, List<Account>>();
            HashMap<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(Constants.companyKey, companyid);
            filterParams.put("parent", null);
            KwlReturnObject accgroupresult = accAccountDAOobj.getAccountGroupInfo(filterParams);
            List<Object[]> list1 = accgroupresult.getEntityList();
            for (Object[] row : list1) {
                String grID = row[1].toString();
                List<Account> tempList = new ArrayList();
                if (accountGroupMap.containsKey(grID)) {
                    tempList = accountGroupMap.get(grID);
                }
                tempList.add((Account) row[0]);
                accountGroupMap.put(grID, tempList);
            }
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            double tradingAmount1[] = accReportsService.getTrading(paramJobj, Group.NATURE_EXPENSES, new JSONArray(), false,null);
            double tradingAmount2[] = accReportsService.getTrading(paramJobj, Group.NATURE_INCOME, new JSONArray(), false,null);
            double profitLossAmount1[] = accReportsService.getProfitLoss(paramJobj, Group.NATURE_EXPENSES, new JSONArray(), false,null);
            double profitLossAmount2[] = accReportsService.getProfitLoss(paramJobj, Group.NATURE_INCOME, new JSONArray(), false,null);

            double profitloss = tradingAmount1[0] - invCloseBal + tradingAmount2[0] + profitLossAmount1[0] + profitLossAmount2[0] + invOpeBal;
            double netProfitAndLossAmount = 0.0;
            if (closingAccountBalancesList.size() > 0 && stDate.after(closingEndDate)) {
                paramJobj.put("closingFilterFlag", true);
                paramJobj.put("closingStartDate", closingEndDate);
                paramJobj.put("closingEndDate", stDate);

                double tradingAmount3[] = accReportsService.getTrading(paramJobj, Group.NATURE_EXPENSES, new JSONArray(), false,null);
                double tradingAmount4[] = accReportsService.getTrading(paramJobj, Group.NATURE_INCOME, new JSONArray(), false,null);
                double profitLossAmount3[] = accReportsService.getProfitLoss(paramJobj, Group.NATURE_EXPENSES, new JSONArray(), false,null);
                double profitLossAmount4[] = accReportsService.getProfitLoss(paramJobj, Group.NATURE_INCOME, new JSONArray(), false,null);

                profitloss += tradingAmount3[0] + tradingAmount4[0] + profitLossAmount3[0] + profitLossAmount4[0];
            }
            Account accountProfitAndLoss = null;
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            if (!StringUtil.isNullOrEmpty(extrapref.getProfitLossAccountId())) {
                filter_names.add("ID");
                filter_params.add(extrapref.getProfitLossAccountId());
            }
            requestParams1.put("filter_names", filter_names);
            requestParams1.put("filter_params", filter_params);
            KwlReturnObject venresult = accAccountDAOobj.getAccount(requestParams1);
            if (!venresult.getEntityList().isEmpty()) {
                accountProfitAndLoss = (Account) venresult.getEntityList().get(0);
                KwlReturnObject closingAccountBalanceResult = accJournalEntryobj.getClosingAccountBalance(accountProfitAndLoss.getID(), companyid, (calendar.get(Calendar.YEAR) - 1));
                List closingAccountBalanceList = closingAccountBalanceResult.getEntityList();
                if (!closingAccountBalanceList.isEmpty()) {
                    ClosingAccountBalance closingAccountBalance = (ClosingAccountBalance) closingAccountBalanceList.get(0);
                    netProfitAndLossAmount += closingAccountBalance.getAmount();
                }
                netProfitAndLossAmount = netProfitAndLossAmount * (-1);//to match the conventions of balance (-bal---profit +bal Loss) 
//                netProfitAndLossAmount += accReportsService.getAccountBalanceWithOutClosing(request, accountProfitAndLoss.getID(), stDate, endDate);
                netProfitAndLossAmount += accReportsService.getAccountBalanceWithOutClosing(paramJobj, accountProfitAndLoss.getID(), stDate, endDate,null);
                profitloss = profitloss + netProfitAndLossAmount;
            }
            profitloss = authHandler.round(profitloss, companyid);
            pretaxprofit = -profitloss;
            boolean accMapFlag = false;
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
//            HashMap<String, Object> requestParams = accAccountHandler.getJsonMap(paramJobj);
            if (requestParams.containsKey("acctypes") && requestParams.get("acctypes") != null && StringUtil.equal(requestParams.get("acctypes").toString(), "3")) {
                noactivity = true;
            }
            boolean isCustomColumnExport = false;
            accMapFlag = paramJobj.optString("accMapFlag", null) != null ? Boolean.parseBoolean(paramJobj.optString("accMapFlag")) : false;
            if (accMapFlag) {
                requestParams.put(Constants.companyKey, paramJobj.optString("childcompanyid", null));
            }
            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
            List list = result.getEntityList();
            Map<String, Object> accountJsonparamMap = new HashMap<String, Object>();
            accountJsonparamMap.put("noactivity", noactivity);
            accountJsonparamMap.put("isCustomColumnExport", isCustomColumnExport);
//            jobj = accFinancialReportsService.getAccountJson(request, list, accCurrencyDAOobj, accountJsonparamMap);
            jobj = getAccountJson(paramJobj, list, accCurrencyDAOobj, accountJsonparamMap);
            JSONArray jSONArray = jobj.getJSONArray(Constants.RES_data);
            for (int count = 0; count < jSONArray.length(); count++) {
                double balance = 0;
                boolean flag = false;
                if (!noactivity) {
                    if (jSONArray.getJSONObject(count).has("accid")) {
                        String accId = jSONArray.getJSONObject(count).getString("accid");
                        String accCode = jSONArray.getJSONObject(count).getString("acccode");
                        JSONObject tempObj = new JSONObject();
//                        if(!accCode.equals("")){
                        if (accCode.equals("8-8103")) {
                            tempObj.put("accCode", "8-8103");
                            flag = true;
                        } else if (accCode.equals("8-7105")) {
                            tempObj.put("accCode", "8-7105");
                            flag = true;
                        } else if (accCode.equals("1-1200")) {
                            tempObj.put("accCode", "1-1200");
                            flag = true;
                        } else if (accCode.equals("8-8102")) {
                            tempObj.put("accCode", "8-8102");
                            flag = true;
                        } else if (accCode.equals("8-7104")) {
                            tempObj.put("accCode", "8-7104");
                            flag = true;
                        } else if (accCode.equals("2-3100")) {
                            tempObj.put("accCode", "2-3100");
                            flag = true;
                        } else if (accCode.equals("2-2000")) {
                            tempObj.put("accCode", "2-2000");
                            flag = true;
                        } else if (accCode.equals("2-4000")) {
                            tempObj.put("accCode", "2-4000");
                            flag = true;
                        } else if (accCode.equals("2-5000")) {
                            tempObj.put("accCode", "2-5000");
                            flag = true;
                        } else if (accCode.equals("2-6000")) {
                            tempObj.put("accCode", "2-6000");
                            flag = true;
                        } else if (accCode.equals("2-7000")) {
                            tempObj.put("accCode", "2-7000");
                            flag = true;
                        } else if (accCode.equals("2-8000")) {
                            tempObj.put("accCode", "2-8000");
                            flag = true;
                        } else if (accCode.equals("2-2900")) {
                            tempObj.put("accCode", "2-2900");
                            flag = true;
                        } else if (accCode.equals("4-1100")) {
                            tempObj.put("accCode", "4-1100");
                            flag = true;
                        } else if (accCode.equals("4-2000")) {
                            tempObj.put("accCode", "4-2000");
                            flag = true;
                        } else if (accCode.equals("4-3000")) {
                            tempObj.put("accCode", "4-3000");
                            flag = true;
                        } else if (accCode.equals("4-4000")) {
                            tempObj.put("accCode", "4-4000");
                            flag = true;
                        } else if (accCode.equals("4-5000")) {
                            tempObj.put("accCode", "4-5000");
                            flag = true;
                        } else if (accCode.equals("4-6000")) {
                            tempObj.put("accCode", "4-6000");
                            flag = true;
                        } else if (accCode.equals("4-7000")) {
                            tempObj.put("accCode", "4-7000");
                            flag = true;
                        } else if (accCode.equals("4-8000")) {
                            tempObj.put("accCode", "4-8000");
                            flag = true;
                        } else if (accCode.equals("4-9000")) {
                            tempObj.put("accCode", "4-9000");
                            flag = true;
                        } else if (accCode.equals("1-1100")) {
                            tempObj.put("accCode", "1-1100");
                            flag = true;
                        } else if (accCode.equals("1-2000")) {
                            tempObj.put("accCode", "1-2000");
                            flag = true;
                        } else if (accCode.equals("3-1000")) {
                            tempObj.put("accCode", "3-1000");
                            flag = true;
                        } else if (accCode.equals("5-1000")) {
                            tempObj.put("accCode", "5-1000");
                            flag = true;
                        } else if (accCode.equals("5-3000")) {
                            tempObj.put("accCode", "5-3000");
                            flag = true;
                        } else if (accCode.equals("5-2000")) {
                            tempObj.put("accCode", "5-2000");
                            flag = true;
                        }
//                        }   
                        if (flag) {
                            KwlReturnObject accountResult = accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Account", accId);
                            if (accountResult.getEntityList().get(0) != null) {
                                Account account = (Account) accountResult.getEntityList().get(0);
                                if (!account.isDeleted()) {
                                    if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {
                                        paramJobj.put("stDate", paramJobj.optString("stdate", null));
                                        paramJobj.put("endDate", paramJobj.optString("enddate", null));
                                    }
//                                    balance = accReportsService.getAccountBalanceForCashFlowStatement(request, account, stDate, endDate);
                                    balance = accReportsService.getAccountBalanceForCashFlowStatement(paramJobj, account, stDate, endDate);
                                    boolean isDebit = false;
                                    if (account.getAccounttype() == 1) {
                                        if (account.getGroup().getNature() == Group.NATURE_LIABILITY) {
                                            isDebit = true;
                                        }
                                    } else if (account.getGroup().getNature() == Group.NATURE_EXPENSES) {
                                        isDebit = true;
                                    }
                                    if (!isDebit) {
                                        if (balance != 0) {
                                            balance = -balance;
                                        }
                                    }
                                    if (account.getAccounttype() == 1) {
                                        if (balance != 0) {
                                            balance = -balance;
                                        }
                                    }
                                }
                            }
                            tempObj.put("openbalanceinbase", jSONArray.getJSONObject(count).getString("openbalanceinbase"));
                            tempObj.put("endingBalance", balance);
                            amtArray.put(tempObj);
                        }
                    }

                }
            }

            for (int count = 0; count < amtArray.length(); count++) {
                String accCode = amtArray.getJSONObject(count).optString("accCode");
                if (accCode.equals("8-8103")) {
//                    provision = provision + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("8-7105")) {
//                    provision = provision + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("1-1200")) { //as per aamir suggestion puting balancetyppe account insted of 8-0030
                    depreciation = depreciation + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("8-8102")) {
//                    lossondisp = lossondisp + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("8-7104")) {
//                    gainondisp = gainondisp + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("2-3100")) {
                    consume = consume + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("2-2000")) {
                    tradedebtors = tradedebtors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("2-4000")) {
                    otherdebtors = otherdebtors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("2-5000")) {
                    otherdebtors = otherdebtors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("2-6000")) {
                    otherdebtors = otherdebtors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("2-7000")) {
                    otherdebtors = otherdebtors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("2-8000")) {
                    otherdebtors = otherdebtors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("2-2900")) {
//                    otherdebtors = otherdebtors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("4-1100")) {
                    tradecreditors = tradecreditors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("4-2000")) {
                    othercreditors = othercreditors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("4-3000")) {
                    othercreditors = othercreditors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("4-4000")) {
                    othercreditors = othercreditors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("4-5000")) {
                    othercreditors = othercreditors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("4-6000")) {
                    othercreditors = othercreditors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("4-7000")) {
                    othercreditors = othercreditors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("4-8000")) {
                    othercreditors = othercreditors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("4-9000")) {
                    othercreditors = othercreditors + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("1-1100")) {
                    perchasefixass = perchasefixass + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("1-2000")) {
                    acqofinvest = acqofinvest + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("3-1000")) {
                    proinsssha = proinsssha + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("5-1000")) {
                    hpfinance = hpfinance + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("5-3000")) {
                    hpfinance = hpfinance + amtArray.getJSONObject(count).getDouble("endingBalance");
                } else if (accCode.equals("5-2000")) {
                    termloan = termloan + amtArray.getJSONObject(count).getDouble("endingBalance");
                }
            }
            double totalAmount[] = {0, 0, 0, 0, 0};
            for (int count = 0; count < jSONArray.length(); count++) {
                double balance = 0;
                boolean flag = false;
                if (jSONArray.getJSONObject(count).has("accid")) {
                    String accId = jSONArray.getJSONObject(count).getString("accid");
                    String mastertypevalue = jSONArray.getJSONObject(count).getString("mastertypevalue");
                    if (mastertypevalue.equals("2") || mastertypevalue.equals("3")) {   //
                        KwlReturnObject accountResult = accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Account", accId);
                        if (accountResult.getEntityList().get(0) != null) {
                            DateFormat sdf = authHandler.getDateOnlyFormat();
                            Account account = (Account) accountResult.getEntityList().get(0);
                            boolean isDebit = false;
                            JSONArray chArr = new JSONArray();
                            if (account.getAccounttype() == 1) {
                                if (account.getGroup().getNature() == Group.NATURE_LIABILITY) {
                                    isDebit = true;
                                }
                            } else if (account.getGroup().getNature() == Group.NATURE_EXPENSES) {
                                isDebit = true;
                            }
//                            double tempTotalAmount[] = accReportsService.formatAccountDetails(request, account, stDate, endDate, 1, isDebit, account.getAccounttype() == 1 ? true : false, chArr, sdf, null, null);
                            double tempTotalAmount[] = accReportsService.formatAccountDetails(paramJobj, account, stDate, endDate, 1, isDebit, account.getAccounttype() == 1 ? true : false, chArr, sdf, null, null,null,null);
                            totalAmount[0] = tempTotalAmount[0];
                            totalAmount[1] = tempTotalAmount[1];
                            totalAmount[2] = tempTotalAmount[2];
                            totalAmount[3] = tempTotalAmount[3];
                            totalAmount[4] = tempTotalAmount[4];
                            if (chArr.length() > 0) {
                                double ta = totalAmount[0];
                                double tempta = totalAmount[1];
                                double topen = totalAmount[2];
                                double tperiod = totalAmount[3];
                                double tend = totalAmount[4];
                                if (!isDebit) {
                                    if (ta != 0) {
                                        ta = -ta;
                                    } else {
                                        ta = ta;
                                    }
                                    if (tempta != 0) {
                                        tempta = -tempta;
                                    } else {
                                        tempta = -tempta;
                                    }
                                    if (topen != 0) {
                                        topen = -topen;
                                    } else {
                                        topen = topen;
                                    }
                                    if (tperiod != 0) {
                                        tperiod = -tperiod;
                                    } else {
                                        tperiod = tperiod;
                                    }
                                    if (tend != 0) {
                                        tend = -tend;
                                    } else {
                                        tend = tend;
                                    }
                                }
                                if (account.getAccounttype() == 1) {
                                    if (ta != 0) {
                                        ta = -ta;
                                    } else {
                                        ta = ta;
                                    }
                                    if (tempta != 0) {
                                        tempta = -tempta;
                                    } else {
                                        tempta = -tempta;
                                    }
                                    if (topen != 0) {
                                        topen = -topen;
                                    } else {
                                        topen = topen;
                                    }
                                    if (tperiod != 0) {
                                        tperiod = -tperiod;
                                    } else {
                                        tperiod = tperiod;
                                    }
                                    if (tend != 0) {
                                        tend = -tend;
                                    } else {
                                        tend = tend;
                                    }
                                }
                                cashbalbegin += topen;
                                cashbalnet = cashbalnet + tend;
                            }
                        }
                    }
                }
            }
            jobj.put("pretaxprofit", pretaxprofit);
            jobj.put("provision", provision);
            jobj.put("depreciation", -depreciation);
            jobj.put("lossondisp", lossondisp);
            jobj.put("gainondisp", gainondisp);
            jobj.put("divincome", divincome);
            jobj.put("consume", -consume);
            jobj.put("tradedebtors", -tradedebtors);
            jobj.put("otherdebtors", -otherdebtors);
            jobj.put("tradecreditors", tradecreditors);
            jobj.put("othercreditors", othercreditors);
            jobj.put("perchasefixass", -perchasefixass);
            jobj.put("acqofinvest", acqofinvest);
            jobj.put("proinsssha", proinsssha);
            jobj.put("hpfinance", hpfinance);
            jobj.put("termloan", termloan);
            jobj.put("cashbalbegin", cashbalbegin);
            jobj.put("cashbalnet", cashbalnet);
        } catch (Exception je) {
            System.out.println(je);
        }
        return jobj;
    }  
   
  @Override  
    public double getTotalAccountBalanceInSelectedCurrency(Account account, double totalAccountBalance, JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException {
        try {
            List<Account> list = new ArrayList(account.getChildren());
            Date stDate = new Date(0);
            Date endDate = new Date();
            for (Account subAccount : list) {
                double balance = 0;
                if (!subAccount.isDeleted()) {
//                    balance = getAccountBalanceInOriginalCurrency(request, subAccount.getID(), stDate, endDate);
                    balance = getAccountBalanceInOriginalCurrency(paramJobj, subAccount.getID(), stDate, endDate);
                }
                totalAccountBalance = totalAccountBalance + balance;
                if (subAccount.getChildren().isEmpty()) {
                    continue;
                }
                //Recursive function to get child accounts
                totalAccountBalance = getTotalAccountBalanceInSelectedCurrency(subAccount, totalAccountBalance, paramJobj);
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getMonthlyTradingProfitLossExport : " + e.getMessage(), e);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("getAccountBalance : " + ex.getMessage(), ex);
        }
        return totalAccountBalance;
    }  
    
    /**
     *
     * @param requestParams
     * @return totalPricipleAmountForEClaimJE
     * @throws ServiceException
     */
    @Override
    public double getTotalPricipleAmountForEClaimJE(Map<String, Object> requestParams) throws ServiceException {
        double totalPrincipleAmount = 0.0;
        try {
            /*
            *If JE is from EClaim,then Principal amount will be the
            * sum of amounts entered for all debit accounts except GST account
            */
            KwlReturnObject jeDetailsList = accJournalEntryobj.getJEDetailListToCalculatePrincipleAmount(requestParams);
            List<JournalEntryDetail> journalEntryDetails = (List<JournalEntryDetail>) jeDetailsList.getEntityList();
            for (JournalEntryDetail jed : journalEntryDetails) {
                JournalEntry JE = jed.getJournalEntry();
                double amount = jed.getAmount();
                KwlReturnObject bAmt = null;

                if (requestParams.containsKey(Constants.gstFlag)) {
                    if (!JE.getCurrency().getCurrencyID().equals(Constants.SGDID)) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amount, JE.getCurrency().getCurrencyID(), Constants.SGDID, JE.getEntryDate(), JE.getExternalCurrencyRate());
                    }
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, JE.getCurrency().getCurrencyID(), JE.getEntryDate(), JE.getExternalCurrencyRate());
                }
                if (bAmt != null) {
                    amount = (Double) bAmt.getEntityList().get(0);
                }
                totalPrincipleAmount += amount;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccFinancialReportsServiceImpl.getTotalPricipleAmountForEClaimJE:" + ex.getMessage(), ex);
        }
        return totalPrincipleAmount;
    }
    /**
     * 
     * @param requestParams
     * @param list :  List of JE records fetch from DataBase
     * @param jArr: Put data in Array
     * @param templateflag
     * @return
     * @throws ServiceException 
     */
    public JSONObject getJournalEntryJsonForExportMerged(HashMap<String, Object> requestParams, List list, JSONArray jArr, int templateflag) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String jeapprover = "NA", filetype="";
        int counter = 0;
        try {

            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = null;//authHandler.getUserDateFormatterWithoutTimeZone(request);
            DateFormat userdf = null;//authHandler.getUserDateFormatterWithoutTimeZone(request);
            if(requestParams.containsKey("type") && requestParams.get("type") != null){
                filetype = (String) requestParams.get("type");   
            }            
            if (requestParams.containsKey("dateformat")) {
                df = (DateFormat) requestParams.get("dateformat");
            }
            if (requestParams.containsKey("userdateformat")) {
                userdf = (DateFormat) requestParams.get("userdateformat");
            }
            KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) requestParams.get(Constants.companyKey));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            String cashAccount = pref.getCashAccount().getID();
            boolean isDetailedXls = false;
            if (requestParams.containsKey("type") && requestParams.get("type").toString().equals("detailedXls")) {
                isDetailedXls = true;
            }
            boolean isDetailedCSV = false;
            if (requestParams.containsKey("type") && requestParams.get("type").toString().equals("detailedCSV")) {
                isDetailedCSV = true;
            }
            List<String> jeIdList = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                Object[] row = (Object[]) list.get(i);
                JournalEntry entry = (JournalEntry) row[0];
                jeIdList.add(entry.getID());
            }

            /*
             Create custom field map for all modules
             */
            String companyid = (String) requestParams.get(Constants.companyKey);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> JEcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> JEcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, 0));
            HashMap<String, String> JEreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> JEFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, JEreplaceFieldMap, JEcustomFieldMap, JEcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> VIcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> VIcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Vendor_Invoice_ModuleId, 0));
            HashMap<String, String> VIreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> VIFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, VIreplaceFieldMap, VIcustomFieldMap, VIcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> MPcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> MPcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId, 0));
            HashMap<String, String> MPreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> MPFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, MPreplaceFieldMap, MPcustomFieldMap, MPcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> DNcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> DNcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Debit_Note_ModuleId, 0));
            HashMap<String, String> DNreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> DNFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, DNreplaceFieldMap, DNcustomFieldMap, DNcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> CIcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> CIcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Invoice_ModuleId, 0));
            HashMap<String, String> CIreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> CIFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, CIreplaceFieldMap, CIcustomFieldMap, CIcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> RPcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> RPcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId, 0));
            HashMap<String, String> RPreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> RPFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, RPreplaceFieldMap, RPcustomFieldMap, RPcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> CNcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> CNcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Credit_Note_ModuleId, 0));
            HashMap<String, String> CNreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> CNFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, CNreplaceFieldMap, CNcustomFieldMap, CNcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> FAVIcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> FAVIcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId, 0));
            HashMap<String, String> FAVIreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FAVIFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, FAVIreplaceFieldMap, FAVIcustomFieldMap, FAVIcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> FACIcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> FACIcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_FixedAssets_DisposalInvoice_ModuleId, 0));
            HashMap<String, String> FACIreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FACIFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, FACIreplaceFieldMap, FACIcustomFieldMap, FACIcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> LCIcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> LCIcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.LEASE_INVOICE_MODULEID, 0));
            HashMap<String, String> LCIreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> LCIFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, LCIreplaceFieldMap, LCIcustomFieldMap, LCIcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> ConsCIcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> ConsCIcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_ConsignmentInvoice_ModuleId, 0));
            HashMap<String, String> ConsCIreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> ConsCIFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, ConsCIreplaceFieldMap, ConsCIcustomFieldMap, ConsCIcustomDateFieldMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> ConsVIcustomFieldMap = new HashMap<String, String>();
            HashMap<String, String> ConsVIcustomDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Consignment_GoodsReceipt_ModuleId, 0));
            HashMap<String, String> ConsVIreplaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> ConsVIFieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, ConsVIreplaceFieldMap, ConsVIcustomFieldMap, ConsVIcustomDateFieldMap);
            
            String inParam = accReportsService.getInParamFromList(jeIdList);
            HashMap<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put(Constants.companyKey, (String) requestParams.get(Constants.companyKey));
            reqParams.put("jeIds", inParam);
            reqParams.put("CashAndInvoice", Boolean.FALSE.parseBoolean(String.valueOf(requestParams.get("CashAndInvoice"))));
            Map<String, BillingGoodsReceipt> billingGrMap = Collections.emptyMap();
            Map<String, Object[]> billingCreditNoteMap = Collections.emptyMap();
            Map<String, Object[]> billingDebitNoteMap = Collections.emptyMap();
            Map<String, Object[]> billingPaymentReceivedMap = Collections.emptyMap();
            Map<String, Object[]> billingPaymentMadeMap = Collections.emptyMap();
            /**
             * To Add DisHonouredPaymentReceived (for Receive Payment),
             * DisHonouredPayment Transaction (for Receive Payment) Details Map,
             * while exporting JE.
             */
            Map<String, Object[]> disHonouredpaymentReceivedMap = new HashMap();
            Map<String, Object[]> disHonouredPaymentMadeMap = new HashMap();

            Map<String, Object[]> creditNoteMap = new HashMap();
            Map<String, Object[]> debitNoteMap = new HashMap();
            Map<String, Object[]> paymentReceivedMap = new HashMap();
            Map<String, Object[]> paymentMadeMap = new HashMap();

            Map<String, Object[]> creditNoteMapVendor = new HashMap();
            Map<String, Object[]> debitNoteMapCustomer = new HashMap();
            Map<String, DeliveryOrder> doMap = new HashMap();
            Map<String, AssetDepreciationDetail> assetDepreciationDetailMap = new HashMap();
            
                
            /**
             * line level custom field map 
             */
            fieldrequestParams = new HashMap();
            HashMap<String, String> JEcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> JEcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, 1));
            HashMap<String, String> JEreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> JEFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, JEreplaceFieldLineMap, JEcustomFieldLineMap, JEcustomDateFieldLineMap);


            fieldrequestParams = new HashMap();
            HashMap<String, String> VIcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> VIcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Vendor_Invoice_ModuleId, 1));
            HashMap<String, String> VIreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> VIFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, VIreplaceFieldLineMap, VIcustomFieldLineMap, VIcustomDateFieldLineMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> MPcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> MPcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId, 1));
            HashMap<String, String> MPreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> MPFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, MPreplaceFieldLineMap, MPcustomFieldLineMap, MPcustomDateFieldLineMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> DNcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> DNcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Debit_Note_ModuleId, 1));
            HashMap<String, String> DNreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> DNFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, DNreplaceFieldLineMap, DNcustomFieldLineMap, DNcustomDateFieldLineMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> CIcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> CIcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Invoice_ModuleId, 1));
            HashMap<String, String> CIreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> CIFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, CIreplaceFieldLineMap, CIcustomFieldLineMap, CIcustomDateFieldLineMap);

            
            fieldrequestParams = new HashMap();
            HashMap<String, String> RPcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> RPcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId, 1));
            HashMap<String, String> RPreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> RPFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, RPreplaceFieldLineMap, RPcustomFieldLineMap, RPcustomDateFieldLineMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> CNcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> CNcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Credit_Note_ModuleId, 1));
            HashMap<String, String> CNreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> CNFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, CNreplaceFieldLineMap, CNcustomFieldLineMap, CNcustomDateFieldLineMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> FAVIcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> FAVIcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId, 1));
            HashMap<String, String> FAVIreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> FAVIFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, FAVIreplaceFieldLineMap, FAVIcustomFieldLineMap, FAVIcustomDateFieldLineMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> FACIcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> FACIcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_FixedAssets_DisposalInvoice_ModuleId, 1));
            HashMap<String, String> FACIreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> FACIFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, FACIreplaceFieldLineMap, FACIcustomFieldLineMap, FACIcustomDateFieldLineMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> LCIcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> LCIcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.LEASE_INVOICE_MODULEID, 1));
            HashMap<String, String> LCIreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> LCIFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, LCIreplaceFieldLineMap, LCIcustomFieldLineMap, LCIcustomDateFieldLineMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> ConsCIcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> ConsCIcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_ConsignmentInvoice_ModuleId, 1));
            HashMap<String, String> ConsCIreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> ConsCIFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, ConsCIreplaceFieldLineMap, ConsCIcustomFieldLineMap, ConsCIcustomDateFieldLineMap);

            fieldrequestParams = new HashMap();
            HashMap<String, String> ConsVIcustomFieldLineMap = new HashMap<String, String>();
            HashMap<String, String> ConsVIcustomDateFieldLineMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Consignment_GoodsReceipt_ModuleId, 1));
            HashMap<String, String> ConsVIreplaceFieldLineMap = new HashMap<String, String>();
            HashMap<String, Integer> ConsVIFieldLineMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, ConsVIreplaceFieldLineMap, ConsVIcustomFieldLineMap, ConsVIcustomDateFieldLineMap);

            
            KwlReturnObject jedcdresults =null;
            Map<String, Object> jedcdresultmap=null;
            KwlReturnObject jecdresults = null;
            Map<String, Object> jecdresultmap=null;
            JSONArray tempArray = new JSONArray();
            /*
            Execute Function for Every Modules batch wise
            */
            reqParams.put("includeFixedAssetInvoicesFlag", true);
            if (reqParams.containsKey("jeIds")) {
                /*
                Split JE Ids data into Array
                */
                String jeIds = (String) reqParams.get("jeIds");
                List<String> items = Arrays.asList(jeIds.split("\\s*,\\s*"));
                int start = 0;
                int end = Constants.MaxThreasholdValue;
                int endfinal = Constants.MaxThreasholdValue;
                while (start < items.size()) {
                    if (end > items.size()) {
                        end = items.size() - 1;
                    }
                    /*
                    Create Sublist from Main list
                    */
                    List<String> secsublist = list.subList(start,end+1);
                    List<String> sublist = items.subList(start, end+1);
                    
                    String jeidsarr = sublist.toString();
                    String jeids = jeidsarr.substring(1, jeidsarr.length() - 1);
                    reqParams.put("jeIds", jeids);
                    start = end + 1;
                    end = end + endfinal;
                    Map<String, DeliveryOrder> doMaptemp = accReportsService.getDOMap(reqParams);
                    reqParams.put("isConsignment", true);
                    reqParams.put("isConsignment", false);
                    reqParams.put("isPartyEntry", true); // fetch only CN/DN whichi is created by party journal
                    Map<String, Object[]> creditNoteMaptemp = accReportsService.getCreditNoteMap(reqParams);
                    Map<String, Object[]> debitNoteMaptemp = accReportsService.getDebitNoteMap(reqParams);
                    reqParams.put("paymentWindowType", 3); // fetch payment/receipt only against GL
                    Map<String, Object[]> paymentReceivedMaptemp = accReportsService.getPaymentReceivedMap(reqParams);
                    Map<String, Object[]> paymentMadeMaptemp = accReportsService.getPaymentMadeMap(reqParams);
                    reqParams.remove("paymentWindowType");
                    /**
                     * To Get Dis-Honoured Received Payment, Make Payment
                     * Transaction.
                     */
                    requestParams.put("jeIds", jeids);
                    jedcdresults = accJournalEntryobj.getJournalEntryDetailsCustomDatabyJEIds(requestParams);
                    jedcdresultmap = new HashMap();
                    

                    if (jedcdresults != null && jedcdresults.getEntityList() != null && !jedcdresults.getEntityList().isEmpty()) {
                        List jedetcustomdataList = jedcdresults.getEntityList();

                        for (Object jedetcustomdataListObj : jedetcustomdataList) {
                            Object[] row = (Object[]) jedetcustomdataListObj;
                            String accJEDetailCustomDataId = (String) row[0];    // AccJEDetailCustomData -> jedetailId
                            Object accJEDetailCustomDataObj = row[1];            //AccJEDetailCustomData Object 
                            jedcdresultmap.put(accJEDetailCustomDataId, accJEDetailCustomDataObj);
                        }
                    }
                    jecdresults = accJournalEntryobj.getJournalEntryCustomDataByJEIds(requestParams);
                    jecdresultmap =new HashMap();
                    
                    if(jecdresults!=null && jecdresults.getEntityList()!=null && !jecdresults.getEntityList().isEmpty()){
                        List jecustomdataList = jecdresults.getEntityList();

                        for (Object jecustomdataListObj : jecustomdataList) {
                            Object[] row = (Object[]) jecustomdataListObj;
                            String accJECustomDataId = (String) row[0];    // AccJECustomData -> journalentryId
                            Object accJECustomDataObj = row[1];            //AccJECustomData Object 
                            jecdresultmap.put(accJECustomDataId, accJECustomDataObj);
                        }
                    }
                    reqParams.put("disHonouredJeIds", jeids);
                    reqParams.remove("jeIds");
                    Map<String, Object[]> paymentMadeMaptempdisHonouredPaymentMadeMapTemp = accReportsService.getPaymentMadeMap(reqParams);
                    Map<String, Object[]> disHonouredpaymentReceivedMapTemp = accReportsService.getPaymentReceivedMap(reqParams);
                    reqParams.put("jeIds", jeids);
                    reqParams.remove("disHonouredJeIds");
                    Map<String, Object[]> creditNoteMapVendortemp = accReportsService.creditNoteMapVendor(reqParams);
                    Map<String, Object[]> debitNoteMapCustomertemp = accReportsService.debitNoteMapCustomer(reqParams);
                    Map<String, AssetDepreciationDetail> assetDepreciationDetailMaptemp = accReportsService.getAssetDepreciationMap(reqParams);
                    /*
                    Put Sub Map into Main parent map
                    */
                    doMap.putAll(doMaptemp);
                    creditNoteMap.putAll(creditNoteMaptemp);
                    debitNoteMap.putAll(debitNoteMaptemp);
                    paymentReceivedMap.putAll(paymentReceivedMaptemp);
                    paymentMadeMap.putAll(paymentMadeMaptemp);
                    creditNoteMapVendor.putAll(creditNoteMapVendortemp);
                    debitNoteMapCustomer.putAll(debitNoteMapCustomertemp);
                    assetDepreciationDetailMap.putAll(assetDepreciationDetailMaptemp);
                    disHonouredPaymentMadeMap.putAll(paymentMadeMaptempdisHonouredPaymentMadeMapTemp);
                    disHonouredpaymentReceivedMap.putAll(disHonouredpaymentReceivedMapTemp);
             
                    String currentJE = "";
                    Iterator itr = secsublist.iterator();

            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JournalEntry entry = (JournalEntry) row[0];
                int moduleID = entry.getTransactionModuleid();
                if (!currentJE.equals(entry.getID())) {
                    currentJE = entry.getID();
                    if (tempArray.length() > 0) {
                        if (tempArray.getJSONObject(0).has("ismanualje")) {
                            tempArray = AccReportsHandler.sortJsonArrayOnJEDetailSerialNo(tempArray);
                        }
                        for (int addCnt = 0; addCnt < tempArray.length(); addCnt++) {
                            jArr.put(tempArray.getJSONObject(addCnt));
                        }
                        tempArray = new JSONArray();
                    }
                }
                System.out.println(entry.getEntryNumber());
                JournalEntryDetail jed = (JournalEntryDetail) row[1];
                String currId = entry.getCurrency() == null ? currency.getCurrencyID() : entry.getCurrency().getCurrencyID();

                // To get the name of final Approver of JE
                //Below code removed as journal entry approver name is not displayed in Export File 
//                KwlReturnObject approvalresult = accountingHandlerDAOobj.getApprovalHistoryForExport(entry.getID(), entry.getCompany().getCompanyID());
//                if (approvalresult != null && (!approvalresult.getEntityList().isEmpty())) {
//                    Approvalhistory ah = (Approvalhistory) approvalresult.getEntityList().get(approvalresult.getEntityList().size() - 1);   //If multiple approvers are there, then get the name of final approver only.
//                    jeapprover = ah.getApprover().getFullName();
//                }
                
                JSONObject obj = new JSONObject();
                obj.put("journalentrytype", entry.getTypeValue());
                obj.put("journalentryid", entry.getID());
                obj.put("entryno", entry.getEntryNumber());
                obj.put("srno", jed.getSrno());
                obj.put(Constants.companyKey, entry.getCompany().getCompanyID());
                obj.put("companyname", entry.getCompany().getCompanyName());
                obj.put("eliminateflag", entry.isEliminateflag() ? 1 : 0);
                obj.put("currencysymbol", entry.getCurrency() == null ? currency.getSymbol() : entry.getCurrency().getSymbol());
                obj.put("currencName", entry.getCurrency() == null ? currency.getName() : entry.getCurrency().getName());
                obj.put("currencycode", entry.getCurrency() == null ? currency.getCurrencyCode() : entry.getCurrency().getCurrencyCode());
                obj.put("memo", entry.getMemo());
                try {
                    if (filetype.equals("pdf")) {
                        obj.put("description", jed.getDescription() != null ? StringUtil.DecodeText(jed.getDescription()) : "");                        
                    }else{
                        obj.put("description", jed.getDescription() != null ? StringUtil.replaceFullHTML(StringUtil.DecodeText(jed.getDescription()).replace("<br>", "\n")) : "");
                    }
                } catch (Exception ex) {
                    obj.put("description", jed.getDescription() != null ? jed.getDescription() : "");
                }
                obj.put("journalEntryDetailsDescription", jed.getDescription() != null ? StringUtil.replaceFullHTML(StringUtil.DecodeText(jed.getDescription()).replace("<br>", "\n")) : "");
                obj.put("deleted", entry.isDeleted());
                obj.put("entrydate", userdf.format(entry.getEntryDate()));
                obj.put("entrydateinuserformat", userdf.format(entry.getEntryDate()));
//                obj.put("jeapprover", jeapprover);
                if (entry.getCheque() != null) {
                    obj.put("bank", entry.getCheque().getBankName());
                    obj.put("cheque", entry.getCheque().getChequeNo());
                }
                /**
                 * call getCurrencyToBaseAmount() only when base amount and currency amount are different
                 */
                double cdAmount = 0;
                if (!currency.getCurrencyID().equals(entry.getCurrency().getCurrencyID())) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), currId, entry.getEntryDate(), entry.getExternalCurrencyRate());
                    cdAmount = Double.parseDouble(bAmt.getEntityList().get(0).toString());
                    if (Double.parseDouble(bAmt.getEntityList().get(1).toString()) != 0.0) {
                        obj.put("exchangerate", 1 / Double.parseDouble(bAmt.getEntityList().get(1).toString()));
                    } else {
                        obj.put("exchangerate", 1);
                    }
                } else {
                    obj.put("exchangerate", 1);
                    cdAmount = jed.getAmount();
                }
                if (jed.isDebit()) {
                    obj.put("debitAmount", authHandler.round(cdAmount, companyid));
                    obj.put("debitamountintransactioncurrency", authHandler.formattedAmount(jed.getAmount(), companyid));
                    obj.put("creditamountintransactioncurrency", 0.0);
                    obj.put("creditAmount", 0.0);
                } else {
                    obj.put("debitAmount", 0.0);
                    obj.put("debitamountintransactioncurrency", 0.0);
                    obj.put("creditamountintransactioncurrency", authHandler.formattedAmount(jed.getAmount(), companyid));
                    obj.put("creditAmount", authHandler.round(cdAmount, companyid));
                }
                obj.put("accountName", jed.getAccount().getName());
                obj.put("accountCode", jed.getAccount().getAcccode());

                String project = "";
                String reference = "";
                String optionalField = "";
//                   if (templateflag == Constants.BIT_templateflag || templateflag == Constants.BestSafety_templateflag || templateflag == Constants.Monzone_templateflag || templateflag == 0 || isDetailedXls == true || isDetailedCSV == true || templateflag == Constants.PrimePartners_templateflag) {
                    // ## Get Custom Field Data 
//                    fieldrequestParams = new HashMap();
//                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
//                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
//                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
//                    fieldrequestParams.put(Constants.filter_values, Arrays.asList((String) requestParams.get(Constants.companyKey), Constants.Acc_GENERAL_LEDGER_ModuleId));
//                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
//                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
//                    
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    // Below code commented for the Je detail Custom Data as we have included Line custom fields seprately with global Map's
//                    Detailfilter_names.add("jedetailId");
//                    if (jed.getJournalEntry().getParentJE() != null) {
//                        if (jed.getAccJEDetailCustomData() != null) {
//                            Detailfilter_params.add(jed.getAccJEDetailCustomData().getJedetail().getID());
//                        } else {
//                            Detailfilter_params.add(jed.getID());
//                        }
//                    } else {
//                        Detailfilter_params.add(jed.getID());
//                    }//entry.getTypeValue()==0?entry.getTransactionModuleid():Constants.Acc_GENERAL_LEDGER_ModuleId
//                    Detailfilter_names.add("moduleId");
//                    Detailfilter_params.add( Constants.Acc_GENERAL_LEDGER_ModuleId + "");
//                    invDetailRequestParams.put("filter_names", Detailfilter_names);
//                    invDetailRequestParams.put("filter_params", Detailfilter_params);
//                    KwlReturnObject idcustresult = accInvoiceDAOobj.getInvoiceDetailsCustomData(invDetailRequestParams);
//                    if (idcustresult.getEntityList().size() > 0) {
//                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
//                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
//                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
//                            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
//                            if (customFieldMap.containsKey(varEntry.getKey())) {
//                                String value = "";
//                                String Ids[] = coldata.split(",");
//                                for (int i = 0; i < Ids.length; i++) {//Deop Down or Checklist
//                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
//                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
//                                    if (fieldComboData != null) {
//                                        value += fieldComboData.getValue() != null ? fieldComboData.getValue() + ";" : ";";
//                                        if ((varEntry.getKey().split("_")[1]).equals("Project")) {
//                                            project = fieldComboData.getValue() != null ? fieldComboData.getValue() : "";
//                                        }
//                                    }
//                                }
//                                if (!StringUtil.isNullOrEmpty(value)) {
//                                    value = value.substring(0, value.length() - 1);
//                                }
//                                obj.put(varEntry.getKey(), value);
//                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
//                                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
//                                long milliSeconds = Long.parseLong(coldata);
//                                if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null && !StringUtil.isNullOrEmpty(requestParams.get("browsertz").toString())) {
//                                    df2.setTimeZone(TimeZone.getTimeZone("GMT" + (String)requestParams.get("browsertz")));
//                                }
//                                coldata = df2.format(new java.util.Date(milliSeconds));
//                                obj.put(varEntry.getKey(), coldata);
//                            } else {
//                                if (!StringUtil.isNullOrEmpty(coldata)) {
//                                    obj.put(varEntry.getKey(), coldata);
//                                    if ((varEntry.getKey().split("_")[1]).equals("Reference") || (varEntry.getKey().split("_")[1]).equals("Ref")) {
//                                        reference = coldata;
//                                    }
//                                    if ((varEntry.getKey().split("_")[1]).equals("Optional Field")) {
//                                        optionalField = coldata;
//                                    }
//                                }
//                            }
//                        }
//                    }

                    /*
                    Put All module respective custom data 
                    */
                    variableMap = new HashMap<String, Object>();
//                    invDetailRequestParams = new HashMap<String, Object>();
//                    Detailfilter_names = new ArrayList();
//                    Detailfilter_params = new ArrayList();
//                    Detailfilter_names.add(Constants.companyKey);
//                    Detailfilter_params.add(entry.getCompany().getCompanyID());
//                    Detailfilter_names.add("journalentryId");
//                    Detailfilter_params.add(entry.getID());
//                    invDetailRequestParams.put("filter_names", Detailfilter_names);
//                    invDetailRequestParams.put("filter_params", Detailfilter_params);
//                    KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomData(invDetailRequestParams);
//                    if (idcustresult != null && idcustresult.getEntityList().size() > 0) {
//                        AccJECustomData jeDetailCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                    if (jecdresultmap != null && jecdresultmap.containsKey(entry.getID())) {
                        AccJECustomData jeDetailCustom = (AccJECustomData) jecdresultmap.get(entry.getID());
                        if (jeDetailCustom != null) {
                            JSONObject params = new JSONObject();
                            params.put("companyid", companyid);
                            params.put("customcolumn", 0);
                            params.put("isExport", true);
                            params.put(Constants.userdf, df);
                            if (moduleID == Constants.Acc_Vendor_Invoice_ModuleId || moduleID == Constants.Acc_Cash_Purchase_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, VIFieldMap, VIreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, VIcustomFieldMap, VIcustomDateFieldMap, obj, params);
                            } else if (moduleID == Constants.Acc_Make_Payment_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, MPFieldMap, MPreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, MPcustomFieldMap, MPcustomDateFieldMap, obj, params);
                            } else if (moduleID == Constants.Acc_Debit_Note_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, DNFieldMap, DNreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, DNcustomFieldMap, DNcustomDateFieldMap, obj, params);
                            } else if (moduleID == Constants.Acc_Invoice_ModuleId || moduleID == Constants.Acc_Cash_Sales_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, CIFieldMap, CIreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, CIcustomFieldMap, CIcustomDateFieldMap, obj, params);
                            } else if (moduleID == Constants.Acc_Receive_Payment_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, RPFieldMap, RPreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, RPcustomFieldMap, RPcustomDateFieldMap, obj, params);
                            } else if (moduleID == Constants.Acc_Credit_Note_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, CNFieldMap, CNreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, CNcustomFieldMap, CNcustomDateFieldMap, obj, params);
                            } else if (moduleID == Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FAVIFieldMap, FAVIreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, FAVIcustomFieldMap, FAVIcustomDateFieldMap, obj, params);
                            } else if (moduleID == Constants.Acc_FixedAssets_DisposalInvoice_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FACIFieldMap, FACIreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, FACIcustomFieldMap, FACIcustomDateFieldMap, obj, params);
                            } else if (moduleID == Constants.LEASE_INVOICE_MODULEID) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, LCIFieldMap, LCIreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, LCIcustomFieldMap, LCIcustomDateFieldMap, obj, params);
                            } else if (moduleID == Constants.Acc_Consignment_GoodsReceipt_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, ConsVIFieldMap, ConsVIreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, ConsVIcustomFieldMap, ConsVIcustomDateFieldMap, obj, params);
                            } else if (moduleID == Constants.Acc_ConsignmentInvoice_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, ConsCIFieldMap, ConsCIreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, ConsCIcustomFieldMap, ConsCIcustomDateFieldMap, obj, params);
                            } else {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, JEFieldMap, JEreplaceFieldMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, JEcustomFieldMap, JEcustomDateFieldMap, obj, params);
                            }
                        }
                    }
                //ERM-656
                if (pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                    accReportsService.addPerpetualInventoryJEGlobalLevelCustomData(requestParams, entry, obj);
                }
                    /*Below Code for Line Lvel custom Fields
                     *For All the tarancstions 
                     *Requirement for SDP-6001 
                     */
                    variableMap = new HashMap();
//                    invDetailRequestParams = new HashMap();
//                    Detailfilter_names = new ArrayList();
//                    Detailfilter_params = new ArrayList();
//                    Detailfilter_names.add("jedetailId");
//                    Detailfilter_params.add(jed.getID());
//                    invDetailRequestParams.put("filter_names", Detailfilter_names);
//                    invDetailRequestParams.put("filter_params", Detailfilter_params);
//                    idcustresult = accInvoiceDAOobj.getInvoiceDetailsCustomData(invDetailRequestParams);
//                    if (idcustresult.getEntityList().size() > 0) {
//                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    if (jedcdresultmap != null && jedcdresultmap.containsKey(jed.getID())) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) jedcdresultmap.get(jed.getID());    
                        if (jeDetailCustom != null) {
                            JSONObject params = new JSONObject();
                            params.put("companyid", companyid);
                            params.put("customcolumn", 1);
                            params.put("isExport", true);
                            params.put(Constants.userdf, df);
                            if (moduleID == Constants.Acc_Vendor_Invoice_ModuleId || moduleID == Constants.Acc_Cash_Purchase_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, VIFieldLineMap, VIreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, VIcustomFieldLineMap, VIcustomDateFieldLineMap, obj, params);
                            } else if (moduleID == Constants.Acc_Make_Payment_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, MPFieldLineMap, MPreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, MPcustomFieldLineMap, MPcustomDateFieldLineMap, obj, params);
                            } else if (moduleID == Constants.Acc_Debit_Note_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, DNFieldLineMap, DNreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, DNcustomFieldLineMap, DNcustomDateFieldLineMap, obj, params);
                            } else if (moduleID == Constants.Acc_Invoice_ModuleId || moduleID == Constants.Acc_Cash_Sales_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, CIFieldLineMap, CIreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, CIcustomFieldLineMap, CIcustomDateFieldLineMap, obj, params);
                            } else if (moduleID == Constants.Acc_Receive_Payment_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, RPFieldLineMap, RPreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, RPcustomFieldLineMap, RPcustomDateFieldLineMap, obj, params);
                            } else if (moduleID == Constants.Acc_Credit_Note_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, CNFieldLineMap, CNreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, CNcustomFieldLineMap, CNcustomDateFieldLineMap, obj, params);
                            } else if (moduleID == Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FAVIFieldLineMap, FAVIreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, FAVIcustomFieldLineMap, FAVIcustomDateFieldLineMap, obj, params);
                            } else if (moduleID == Constants.Acc_FixedAssets_DisposalInvoice_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FACIFieldLineMap, FACIreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, FACIcustomFieldLineMap, FACIcustomDateFieldLineMap, obj, params);
                            } else if (moduleID == Constants.LEASE_INVOICE_MODULEID) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, LCIFieldLineMap, LCIreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, LCIcustomFieldLineMap, LCIcustomDateFieldLineMap, obj, params);
                            } else if (moduleID == Constants.Acc_Consignment_GoodsReceipt_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, ConsVIFieldLineMap, ConsVIreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, ConsVIcustomFieldLineMap, ConsVIcustomDateFieldLineMap, obj, params);
                            } else if (moduleID == Constants.Acc_ConsignmentInvoice_ModuleId) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, ConsCIFieldLineMap, ConsCIreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, ConsCIcustomFieldLineMap, ConsCIcustomDateFieldLineMap, obj, params);
                            } else {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, JEFieldLineMap, JEreplaceFieldLineMap, variableMap);
                                fieldDataManagercntrl.addCustomData(variableMap, JEcustomFieldLineMap, JEcustomDateFieldLineMap, obj, params);
                            }
                        }
                    }
                //ERM-656
                if (pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                    accReportsService.addPerpetualInventoryJELineLevelCustomData(requestParams, jed, obj);
                }
//                }
                obj.put("project", project);         // for Project combo of BIT 
                obj.put("reference", reference);     // for Reference Custom Field of BIT
                obj.put("optionalfield", optionalField);     // for Reference Custom Field of BIT
                try {
                    if (entry.getCreatedby() != null) {
                        obj.put("createdby", StringUtil.getFullName(entry.getCreatedby()));
                    } else {
                        obj.put("createdby", "NA");
                    }
                } catch (Exception e) {
                    obj.put("createdby", "NA");
                }
                boolean isPartyJournalEntry = entry.getTypeValue() == 2;
                boolean isNormalJournalEntry = entry.getTypeValue() == 1;
                boolean isFundTransferJournalEntry = entry.getTypeValue() == 3;
                
                if (billingGrMap.containsKey(entry.getID())) {
                    if (billingGrMap.get(entry.getID()).getVendorEntry() != null && billingGrMap.get(entry.getID()).getVendorEntry().getAccount().getID().equals(cashAccount)) {
                        obj.put("transactionID", billingGrMap.get(entry.getID()).getBillingGoodsReceiptNumber());
                        obj.put("transactionDetails", Constants.CASH_PURCHASE + ", " + billingGrMap.get(entry.getID()).getVendor().getName());
                    } else {
                        obj.put("transactionID", billingGrMap.get(entry.getID()).getBillingGoodsReceiptNumber());
                        obj.put("transactionDetails", Constants.VENDOR_INVOICE + ", " + billingGrMap.get(entry.getID()).getVendor().getName());
                    }

                } else if (billingCreditNoteMap.containsKey(entry.getID())) {
                    obj.put("transactionID", ((BillingCreditNote) billingCreditNoteMap.get(entry.getID())[0]).getCreditNoteNumber());
                    obj.put("transactionDetails", Constants.CREDIT_NOTE + ", " + ((Customer) billingCreditNoteMap.get(entry.getID())[1]).getName());
                } else if (billingDebitNoteMap.containsKey(entry.getID())) {
                    obj.put("transactionID", ((BillingDebitNote) billingDebitNoteMap.get(entry.getID())[0]).getDebitNoteNumber());
                    obj.put("transactionDetails", Constants.DEBIT_NOTE + ", " + ((Vendor) billingDebitNoteMap.get(entry.getID())[1]).getName());
                } else if (billingPaymentReceivedMap.containsKey(entry.getID())) {
                    obj.put("transactionID", ((BillingReceipt) billingPaymentReceivedMap.get(entry.getID())[0]).getBillingReceiptNumber());
                    obj.put("transactionDetails", Constants.PAYMENT_RECEIVED + ", " + ((Account) billingPaymentReceivedMap.get(entry.getID())[1]).getName());
                } else if (billingPaymentMadeMap.containsKey(entry.getID())) {
                    obj.put("transactionID", ((BillingPayment) billingPaymentMadeMap.get(entry.getID())[0]).getBillingPaymentNumber());
                    obj.put("transactionDetails", Constants.PAYMENT_MADE + ", " + ((Account) billingPaymentMadeMap.get(entry.getID())[1]).getName());
                } else if (moduleID == Constants.Acc_Cash_Sales_ModuleId || moduleID == Constants.Acc_Invoice_ModuleId || moduleID == Constants.Acc_FixedAssets_DisposalInvoice_ModuleId || moduleID == Constants.LEASE_INVOICE_MODULEID) {
                    KwlReturnObject KwlInvoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), entry.getTransactionId());
                    Invoice invoice = (Invoice) KwlInvoice.getEntityList().get(0);
                    if (invoice != null) {
                        if (invoice.getCustomerEntry() != null && invoice.getCustomerEntry().getAccount().getID().equals(cashAccount)) {
                            obj.put("transactionID", invoice.getInvoiceNumber());
                            obj.put("transactionDetails", Constants.CASH_SALE + ", " + invoice.getCustomer().getName());
                        } else {
                            obj.put("transactionID", invoice.getInvoiceNumber());
                            obj.put("transactionDetails", Constants.CUSTOMER_INVOICE + ", " + invoice.getCustomer().getName());
                        }
                    } else {
                        obj.put("transactionID", "");
                        obj.put("transactionDetails", "");
                        obj.put("ismanualje", true);
                    }
                } else if (moduleID == Constants.Acc_ConsignmentInvoice_ModuleId) {
                    KwlReturnObject KwlInvoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), entry.getTransactionId());
                    Invoice invoice = (Invoice) KwlInvoice.getEntityList().get(0);
                    if (invoice != null) {
                        obj.put("transactionID", invoice.getInvoiceNumber());
                        obj.put("transactionDetails", (Constants.CUSTOMERCON_INVOICE) + ", " + invoice.getCustomer().getName());
                    } else {
                        obj.put("transactionID", "");
                        obj.put("transactionDetails", "");
                        obj.put("ismanualje", true);
                    }
                } else if (assetDepreciationDetailMap.containsKey(entry.getID())) {
                    obj.put("transactionID", "Fixed Asset Depreciation");
                    obj.put("transactionDetails", "Fixed Asset Depreciation");
                } else if (doMap.containsKey(entry.getID())) {
                    obj.put("transactionID", doMap.get(entry.getID()).getDeliveryOrderNumber());
                    obj.put("transactionDetails", Constants.Delivery_Order);
                } else if (moduleID == Constants.Acc_Cash_Purchase_ModuleId || moduleID == Constants.Acc_Vendor_Invoice_ModuleId || moduleID == Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId) {
                    KwlReturnObject KwlgoodsReceipt = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), entry.getTransactionId());
                    GoodsReceipt goodsReceipt = (GoodsReceipt) KwlgoodsReceipt.getEntityList().get(0);
                    if (goodsReceipt != null) {
                        if (goodsReceipt.getVendorEntry() != null && goodsReceipt.getVendorEntry().getAccount().getID().equals(cashAccount)) {
                            obj.put("transactionID", goodsReceipt.getGoodsReceiptNumber());
                            obj.put("transactionDetails", Constants.CASH_PURCHASE + ", " + goodsReceipt.getVendor().getName());
                        } else {
                            obj.put("transactionID", goodsReceipt.getGoodsReceiptNumber());
                            boolean fixedassetinvoiceflag = goodsReceipt.isFixedAssetInvoice();
                            obj.put("transactionDetails", ((fixedassetinvoiceflag == true) ? Constants.ACQUIRED_INVOICE : Constants.VENDOR_INVOICE) + ", " + goodsReceipt.getVendor().getName());
                        }
                    } else {
                        obj.put("transactionID", "");
                        obj.put("transactionDetails", "");
                        obj.put("ismanualje", true);
                    }

                } else if (moduleID == Constants.Acc_Consignment_GoodsReceipt_ModuleId) {
                    KwlReturnObject KwlgoodsReceipt = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), entry.getTransactionId());
                    GoodsReceipt goodsReceipt = (GoodsReceipt) KwlgoodsReceipt.getEntityList().get(0);
                    if (goodsReceipt != null) {
                        obj.put("transactionID", goodsReceipt.getGoodsReceiptNumber());
                        obj.put("transactionDetails", (Constants.VENDORCON_INVOICE) + ", " + goodsReceipt.getVendor().getName());
                    } else {
                        obj.put("transactionID", "");
                        obj.put("transactionDetails", "");
                        obj.put("ismanualje", true);
                    }
                } 
                /**
                 * Transaction Details and Transaction ID for JE from 
                 * Goods Receipt, Delivery Order, Sales Return and Purchase Return
                 * should be viewable in document.
                 */
                else if (moduleID == Constants.Acc_Goods_Receipt_ModuleId && !StringUtil.isNullOrEmpty(entry.getTransactionId())) {
                    KwlReturnObject KwlgoodsReceipt = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), entry.getTransactionId());
                    GoodsReceiptOrder grOrder = (GoodsReceiptOrder) KwlgoodsReceipt.getEntityList().get(0);
                    if (grOrder != null) {
                        obj.put("transactionID", grOrder.getGoodsReceiptOrderNumber());
                        obj.put("transactionDetails", (Constants.Goods_Receipt) + ", " + grOrder.getVendor().getName());
                        obj.put(Constants.billid, grOrder.getID());
                        obj.put("type", Constants.GOODS_RECEIPT_ORDER);
                    }
                } else if (moduleID == Constants.Acc_Purchase_Return_ModuleId && !StringUtil.isNullOrEmpty(entry.getTransactionId())) {
                    KwlReturnObject kwlPurchaseReturn = accountingHandlerDAOobj.getObject((PurchaseReturn.class.getName()), entry.getTransactionId());
                    PurchaseReturn purchaseReturn = (PurchaseReturn) kwlPurchaseReturn.getEntityList().get(0);
                    if (purchaseReturn != null) {
                        obj.put("transactionID", purchaseReturn.getPurchaseReturnNumber());
                        obj.put("transactionDetails", (Constants.PURCHASE_RETURN) + ", " + purchaseReturn.getVendor().getName());
                        obj.put(Constants.billid, purchaseReturn.getID());
                        obj.put("type", Constants.PURCHASE_RETURN);
                    }
                } else if (moduleID == Constants.Acc_Delivery_Order_ModuleId && !StringUtil.isNullOrEmpty(entry.getTransactionId())) {
                    KwlReturnObject kwlDeliveryOrder = accountingHandlerDAOobj.getObject((DeliveryOrder.class.getName()), entry.getTransactionId());
                    DeliveryOrder deliveryOrder = (DeliveryOrder) kwlDeliveryOrder.getEntityList().get(0);
                    if (deliveryOrder != null) {
                        obj.put("transactionID", deliveryOrder.getDeliveryOrderNumber());
                        obj.put("transactionDetails", (Constants.Delivery_Order) + ", " + deliveryOrder.getCustomer().getName());
                        obj.put(Constants.billid, deliveryOrder.getID());
                        obj.put("type", Constants.Delivery_Order);
                    }
                } else if(moduleID == Constants.Acc_Sales_Return_ModuleId && !StringUtil.isNullOrEmpty(entry.getTransactionId())){
                    KwlReturnObject kwlSalesReturn = accountingHandlerDAOobj.getObject((SalesReturn.class.getName()), entry.getTransactionId());
                    SalesReturn salesReturn = (SalesReturn) kwlSalesReturn.getEntityList().get(0);
                    if(salesReturn!=null){
                        obj.put("transactionID", salesReturn.getSalesReturnNumber());
                        obj.put("transactionDetails", (Constants.SALES_RETURN)+", "+ salesReturn.getCustomer().getName());
                        obj.put(Constants.billid, salesReturn.getID());
                        obj.put("type", Constants.SALES_RETURN);
                    }
                } else if (isPartyJournalEntry && (creditNoteMap.containsKey(entry.getID()) || creditNoteMapVendor.containsKey(entry.getID()))) {
                    obj.put("transactionID", "");
                    obj.put("transactionDetails", "Party Journal Entry");
                } else if (moduleID == Constants.Acc_Credit_Note_ModuleId && !isPartyJournalEntry) {
                    KwlReturnObject KwlCreditNote = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), entry.getTransactionId());
                    CreditNote creditNote = (CreditNote) KwlCreditNote.getEntityList().get(0);
                    if (creditNote != null) {
                        if (creditNote.getCustomer() != null) {
                            obj.put("transactionID", creditNote.getCreditNoteNumber());
                            obj.put("transactionDetails", Constants.CREDIT_NOTE + ", " + (creditNote.getCustomer() != null ? creditNote.getCustomer().getName() : creditNote.getVendor().getName()));
                        } else if (creditNote.getVendor() != null) {
                            obj.put("type", Constants.CREDIT_NOTE);
                            obj.put("transactionID", creditNote.getCreditNoteNumber());
                            obj.put("transactionDetails", Constants.CREDIT_NOTE + ", " + (creditNote.getVendor() != null ? creditNote.getVendor().getName() : creditNote.getCustomer().getName()));
                        }
                    } else {
                        obj.put("transactionID", "");
                        obj.put("transactionDetails", "");
                        obj.put("ismanualje", true);
                    }
                } else if (isPartyJournalEntry && (debitNoteMap.containsKey(entry.getID()) || debitNoteMapCustomer.containsKey(entry.getID()))) {
                    obj.put("transactionID", "");
                    obj.put("transactionDetails", "Party Journal Entry");
                } else if (moduleID == Constants.Acc_Debit_Note_ModuleId && !isPartyJournalEntry) {
                    KwlReturnObject KwlDebitNote = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), entry.getTransactionId());
                    DebitNote debitNote = (DebitNote) KwlDebitNote.getEntityList().get(0);
                    if (debitNote != null) {
                        if (debitNote.getVendor() != null) {
                            obj.put("transactionID", debitNote.getDebitNoteNumber());
                            obj.put("transactionDetails", Constants.DEBIT_NOTE + ", " + (debitNote.getVendor() != null ? debitNote.getVendor().getName() : debitNote.getCustomer().getName()));
                        } else if (debitNote.getCustomer() != null) {
                            obj.put("type", Constants.DEBIT_NOTE);
                            obj.put("transactionID", debitNote.getDebitNoteNumber());
                            obj.put("transactionDetails", Constants.DEBIT_NOTE + ", " + (debitNote.getCustomer() != null ? debitNote.getCustomer().getName() : debitNote.getVendor().getName()));
                        }
                    } else {
                        obj.put("transactionID", "");
                        obj.put("transactionDetails", "");
                        obj.put("ismanualje", true);
                    }
                } else if (moduleID == Constants.Acc_Receive_Payment_ModuleId) {// Issue ERP-2244
                    KwlReturnObject resultReceipt = accountingHandlerDAOobj.getObject(Receipt.class.getName(), entry.getTransactionId());
                    Receipt receipt = (Receipt) resultReceipt.getEntityList().get(0);
                    if (receipt != null) {
                        String name = "";
                        boolean addDetails = true;
                        if (receipt.getCustomer() != null) {
                            name = receipt.getCustomer().getName();
                        } else if (receipt.getVendor() != null && !receipt.getVendor().equals("")) {
                            KwlReturnObject result = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                            Vendor vendor = (Vendor) result.getEntityList().get(0);
                            name = vendor.getName();
                        } else {
                            if (paymentReceivedMap.containsKey(entry.getID())) {
                                Account account = (Account) paymentReceivedMap.get(entry.getID())[1];
                                name = account.getName();
                            } else {
                                addDetails = false;
                                obj.put("transactionID", "");
                                obj.put("transactionDetails", "");
                                obj.put("ismanualje", true);
                            }
                        }
                        if (addDetails) {
                            obj.put("transactionID", receipt.getReceiptNumber());
                            obj.put("transactionDetails", Constants.PAYMENT_RECEIVED + ", " + name);
                        }
                        if (receipt.getPayDetail() != null) {    /// GET BANK AND CHEQUE DETAILS FOR THE PAYMENT
                            Cheque cheque = receipt.getPayDetail().getCheque();
                            if (cheque != null) {
                                obj.put("bank", cheque.getBankName());
                                obj.put("cheque", cheque.getChequeNo());
                            }
                        }
                    } else {
                        obj.put("transactionID", "");
                        obj.put("transactionDetails", "");
                        obj.put("ismanualje", true);
                    }
                } else if (moduleID == Constants.Acc_Make_Payment_ModuleId) {
                    KwlReturnObject resultPayment = accountingHandlerDAOobj.getObject(Payment.class.getName(), entry.getTransactionId());
                    Payment payment = (Payment) resultPayment.getEntityList().get(0);
                    if (payment != null) {
                        String name = "";
                        boolean addDetails = true;
                        if (payment.getVendor() != null) {
                            name = payment.getVendor().getName();
                        } else if (payment.getCustomer() != null && !payment.getCustomer().equals("")) {
                            KwlReturnObject result = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                            Customer customer = (Customer) result.getEntityList().get(0);
                            name = customer.getName();
                        } else {
                            if (paymentMadeMap.containsKey(entry.getID())) {
                                Account account = (Account) paymentMadeMap.get(entry.getID())[1];
                                name = account.getName();
                            } else {
                                addDetails = false;
                                obj.put("transactionID", "");
                                obj.put("transactionDetails", "");
                                obj.put("ismanualje", true);
                            }
                        }
                        if (addDetails) {
                            obj.put("transactionID", payment.getPaymentNumber());
                            obj.put("transactionDetails", Constants.PAYMENT_MADE + ", " + name);
                        }
                        if (payment.getPayDetail() != null) { /// GET BANK AND CHEQUE DETAILS FOR THE PAYMENT
                            Cheque cheque = payment.getPayDetail().getCheque();
                            if (cheque != null) {
                                obj.put("bank", cheque.getBankName());
                                obj.put("cheque", cheque.getChequeNo());
                            }
                        }
                    } else {
                        obj.put("transactionID", "");
                        obj.put("transactionDetails", "");
                        obj.put("ismanualje", true);
                    }
                } else if (isPartyJournalEntry) {
                    obj.put("transactionID", "");
                    obj.put("transactionDetails", "Party Journal Entry");
                    obj.put("ismanualje", true);
                } else if (isNormalJournalEntry) {
                    obj.put("transactionID", "");
                    obj.put("transactionDetails", "Normal Journal Entry");
                    obj.put("ismanualje", true);
                } else if (isFundTransferJournalEntry) {
                    obj.put("transactionID", "");
                    obj.put("transactionDetails", "Funds Transfer");
                } else if (entry.getIsReval() == 1) {
                    obj.put("transactionID", "");
                    obj.put("transactionDetails", "Unrealised Gain/Loss");
                } else if (entry.getIsReval() == 2) {
                    obj.put("transactionID", "");
                    obj.put("transactionDetails", "Realised Gain/Loss");
                } else if (entry.isBadDebtJE()) {
                    if (StringUtil.isNullOrEmpty(entry.getBadDebtSeqNumber())) {
                        obj.put("transactionID", "Bad Debt Adjustment");
                    } else {
                        obj.put("transactionID", entry.getBadDebtSeqNumber());
                    }
                    obj.put("transactionDetails", "Bad Debt Adjustment");
                } else if (entry.isTaxAdjustmentJE()) {
                    obj.put("transactionID", "Tax Adjustment");
                    obj.put("transactionDetails", "Tax Adjustment");
                } else if (entry.isIsexchangegainslossje()) {
                    obj.put("transactionID", "");
                    obj.put("transactionDetails", "Exchange Gains/Loss JE");
                } else if (disHonouredpaymentReceivedMap.containsKey(entry.getID())) {
                    /**
                     * to check Dis-honoured is done against 'Receive Payment'
                     * and get Customer/Vendor/Account Name to put in
                     * Description.
                     */
                    Receipt receipt = (Receipt) disHonouredpaymentReceivedMap.get(entry.getID())[0];
                    String name = "";
                    if (receipt.getCustomer() != null) {
                        name = receipt.getCustomer().getName();
                    } else if (receipt.getVendor() != null && !receipt.getVendor().equals("")) {
                        KwlReturnObject result = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                        Vendor vendor = (Vendor) result.getEntityList().get(0);
                        name = vendor.getName();
                    } else {
                        Account account = (Account) disHonouredpaymentReceivedMap.get(entry.getID())[1];
                        name = account.getName();
                    }
                    obj.put("transactionID", receipt.getReceiptNumber());
                    if (receipt.getDisHonouredChequeJe() != null && receipt.getDisHonouredChequeJe().getID() == entry.getID()) {
                        obj.put("transactionDetails", "JE for Cancelled/Dishonored Cheque for Receipt " + receipt.getReceiptNumber() + ", " + name);
                        obj.put("memo", "");
                    } else {
                        obj.put("transactionDetails", Constants.PAYMENT_RECEIVED + ", " + name);
                    }
                    obj.put(Constants.billid, receipt.getID());
                    obj.put("type", Constants.PAYMENT_RECEIVED);
                } else if (moduleID == Constants.Acc_Dishonoured_Make_Payment_ModuleId && !disHonouredPaymentMadeMap.isEmpty() && disHonouredPaymentMadeMap.containsKey(entry.getID()) && disHonouredPaymentMadeMap.get(entry.getID()) != null) {
                    /**
                     * to check Dis-honoured is done against 'Make Payment' and
                     * get Customer/Vendor/Account Name to put in Description.
                     */
                    Payment payment = (Payment) disHonouredPaymentMadeMap.get(entry.getID())[0];
                    String name = "";
                    if (payment.getVendor() != null) {
                        name = payment.getVendor().getName();
                    } else if (payment.getCustomer() != null && !payment.getCustomer().equals("")) {
                        KwlReturnObject result = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                        Customer customer = (Customer) result.getEntityList().get(0);
                        name = customer.getName();
                    } else {
                        Account account = (Account) disHonouredPaymentMadeMap.get(entry.getID())[1];
                        name = account.getName();
                    }
                    obj.put("transactionID", payment.getPaymentNumber());
                    if (payment.getDisHonouredChequeJe() != null && payment.getDisHonouredChequeJe().getID() == entry.getID()) {
                        obj.put("transactionDetails", "JE for Cancelled/Dishonored Cheque for Payment " + payment.getPaymentNumber() + ", " + name);
                        obj.put("memo", "");
                    } else {
                        obj.put("transactionDetails", Constants.PAYMENT_MADE + ", " + name);
                    }
                    obj.put(Constants.billid, payment.getID());
                    obj.put("type", Constants.PAYMENT_MADE);
                } else {
                    obj.put("transactionID", "");
                    obj.put("transactionDetails", "");
                    obj.put("ismanualje", true);
                }
                //     }
                if (requestParams.containsKey("isCustomerReport")) {        // show report for LMS Weekly JE
                    if (!StringUtil.isNullOrEmpty(entry.getCustomer())) {
                        KwlReturnObject jeres = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) entry.getCustomer());
                        Customer customer = (Customer) jeres.getEntityList().get(0);
                        obj.put("customerid", customer.getID());
                        obj.put("customername", customer.getName());
                        obj.put("amountinbase", jed.getAmount());
                        if (requestParams.get("customer").toString().contains("All") || requestParams.get("customer").toString().contains(customer.getID())) {
                            boolean duplicate = false;
                            for (int i = 0; i < jArr.length(); i++) {
                                JSONObject jSONObject = jArr.getJSONObject(i);
                                if (jSONObject.getString("entryno").equals(entry.getEntryNumber())) {
                                    duplicate = true;
                                    break;
                                }
                            }
                            if (!duplicate) {
                                jArr.put(obj);
                            }
                        }
                    }

                } else {
                    tempArray.put(obj);
                }
                    
               }
                jedcdresultmap.clear();
                jecdresultmap.clear();
            }
            }
            if (tempArray.length() > 0) {
                if (tempArray.getJSONObject(0).has("ismanualje")) {
                    tempArray = AccReportsHandler.sortJsonArrayOnJEDetailSerialNo(tempArray);
                }
                for (int addCnt = 0; addCnt < tempArray.length(); addCnt++) {
                    jArr.put(tempArray.getJSONObject(addCnt));
                }
            }
        } catch (Exception ex) {
            System.out.println("Problem Occurs at" + counter);
            throw ServiceException.FAILURE("getJournalEntryJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    
    public JSONObject getDimensionBasedMonthlyPeriodAmount(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException{
        JSONObject returnObj = new JSONObject();
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            JSONArray jArrObj = new JSONArray();
            DateFormat df = new SimpleDateFormat("MMMM, yyyy");
            Date startDate = df.parse(paramJobj.getString("stdate"));
            Date endDate = df.parse(paramJobj.getString("enddate"));
            Calendar cend = Calendar.getInstance();
            cend.setTime(endDate);
            cend.set(Calendar.DAY_OF_MONTH, cend.getActualMaximum(Calendar.DAY_OF_MONTH));
            endDate = cend.getTime();

            String searchjson = paramJobj.optString("dimensionBasedSearchJson");
            JSONObject searchJsonObj = new JSONObject(searchjson);
            JSONArray searchJsonArray = searchJsonObj.getJSONArray("root");
            boolean isBalanceSheet = paramJobj.optBoolean("isBalanceSheet",false);
            for (int k = 0; k < searchJsonArray.length(); k++) {

                JSONObject compareObj = searchJsonArray.optJSONObject(k);
                String column = compareObj.optString("column");
                String iscustomcolumn = compareObj.optString("iscustomcolumn");
                String iscustomcolumndata = compareObj.optString("iscustomcolumndata");
                String isfrmpmproduct = compareObj.optString("isfrmpmproduct");
                String fieldtype = compareObj.optString("fieldtype");
                String searchText = compareObj.optString("searchText");                
                String columnheader =null;
                String isinterval = compareObj.optString("isinterval");
                String isbefore = compareObj.optString("isbefore");
                String interval = compareObj.optString("interval");
                String combosearch = "";
                try {
                    combosearch = StringUtil.DecodeText(compareObj.optString("combosearch"));
                    columnheader = StringUtil.DecodeText(compareObj.optString("columnheader"));
                } catch (Exception e) {
                    columnheader = compareObj.optString("columnheader");
                    combosearch = compareObj.optString("combosearch");
                }
                List<String> combosearchItems = Arrays.asList(combosearch.split("\\s*,\\s*"));
                List<String> searchTextItems = Arrays.asList(searchText.split("\\s*,\\s*"));                
                int customcolumn = 0;
                if(!StringUtil.isNullOrEmpty(iscustomcolumndata) && Boolean.parseBoolean(iscustomcolumndata)){
                    customcolumn = 1;
                }
                List<String> columns = new ArrayList<String>();
                HashMap<String, Object> fieldParamRequestMap = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList();
                ArrayList filter_values = new ArrayList();
                filter_names.add("companyid");
//                filter_names.add("customcolumn");                
                filter_names.add("fieldlabel");
                filter_values.add(companyid);
//                filter_values.add( customcolumn);                   
                filter_values.add(columnheader);
                fieldParamRequestMap.put("filter_names", filter_names);
                fieldParamRequestMap.put("filter_values", filter_values);
                KwlReturnObject kwlReturnObj = accAccountDAOobj.getFieldParams(fieldParamRequestMap);
                boolean isProductCustomData = false;
                boolean isForKnockOff = false;
                // Create map for moduleid and its respective colnum.
                Map<Integer,String> colnumMap = new HashMap<Integer,String>();
                if (kwlReturnObj != null && kwlReturnObj.getEntityList() != null && !kwlReturnObj.getEntityList().isEmpty()) {
                    Set<String> colSet = new HashSet<String>();
                    for (int j = 0; j < kwlReturnObj.getEntityList().size(); j++) {
                        FieldParams fp = (FieldParams) kwlReturnObj.getEntityList().get(j);
                        colSet.add("Col" + fp.getColnum());
                        if(fp.getModuleid() == Constants.Acc_Product_Master_ModuleId){
                            isProductCustomData = true ;
                        }
                        if (fp.isIsForKnockOff()) {
                            isForKnockOff = true;
                        }
                        //maintan map with moduleid and colnum
                        colnumMap.put(fp.getModuleid(),"Col" + fp.getColnum());
                    }
                    columns.addAll(colSet);
                }

                Map<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("isMonthly", paramJobj.has("isMonthly") ? paramJobj.getBoolean("isMonthly") : false);
                requestParams.put("startdate", startDate);
                requestParams.put("enddate", endDate);
                requestParams.put("companyid", companyid);
                requestParams.put("fieldtype", fieldtype);
                requestParams.put("customdatavalues", combosearchItems);
                requestParams.put("columns", columns);
                requestParams.put("colnumMap", colnumMap);
                requestParams.put("iscustomcolumndata", customcolumn);                
                requestParams.put("columnheader", columnheader);
                requestParams.put("isProductCustomData", isProductCustomData);
                requestParams.put("isForKnockOff", isForKnockOff);
                if (paramJobj.has("costcenter") && !StringUtil.isNullOrEmpty(paramJobj.getString("costcenter"))) {
                    requestParams.put("costcenter", paramJobj.getString("costcenter"));
                }
                Map<String, Double> accAmtMap = null;
                accAmtMap = accReportsService.getPeriodAccountAmountMap(requestParams);
                Map<String, List<Account>> accGroupMap = null;
                accGroupMap = accReportsService.getGroupAccountMap(paramJobj);

                JSONArray comparedata = new JSONArray();
                for (int i = 0; i < combosearchItems.size(); i++) {
                    JSONObject cntObj = new JSONObject();
                    cntObj.put("combosearch", combosearchItems.get(i));
                    cntObj.put("searchText", searchTextItems.get(i));                    
                    cntObj.put("column", column);
                    cntObj.put("iscustomcolumn", iscustomcolumn);
                    cntObj.put("iscustomcolumndata", iscustomcolumndata);
                    cntObj.put("isfrmpmproduct", isfrmpmproduct);
                    cntObj.put("fieldtype", fieldtype);
                    cntObj.put("columnheader", columnheader);
                    cntObj.put("isinterval", isinterval);
                    cntObj.put("isbefore", isbefore);
                    cntObj.put("interval", interval);
                    comparedata.put(cntObj);
                }

                for (int cnt = 0; cnt < comparedata.length(); cnt++) {
                    JSONArray dimSearchJsonArr = new JSONArray();
                    JSONObject dimJson = comparedata.getJSONObject(cnt);
                    dimSearchJsonArr.put(dimJson);
                    JSONObject putSearchJson = new JSONObject();
                    putSearchJson.put("root", dimSearchJsonArr);
                    paramJobj.put(Constants.Acc_Search_Json, putSearchJson.toString());
                    paramJobj.put("customvaluepassed", dimJson.getString("combosearch"));
                    JSONObject fobj1 = getMonthlyPeriodAmount(paramJobj, true, accAmtMap);
                    returnObj.put("title", fobj1.getString("title"));
                    returnObj.put("header", fobj1.getString("header"));
                    returnObj.put("align", fobj1.getString("align"));
                    
                    JSONObject fobj = new JSONObject();
                    JSONArray jArrL = fobj1.getJSONArray("refleft");
                    JSONArray jArrR = fobj1.getJSONArray("refright");
                    JSONArray array = fobj1.getJSONArray("months");
                    JSONObject monthArrayObject = new JSONObject();
                    monthArrayObject.put("months", array);
                    jArrL.put(monthArrayObject);
                    jArrR.put(monthArrayObject);
                    fobj.put("left", jArrL);
                    fobj.put("right", jArrR);
                    JSONObject jobj = new JSONObject();
                    jobj.put(Constants.RES_data, fobj);

                    
                    if(isBalanceSheet){
                        jobj = populateBalanceSheetJson(jobj);
                        jobj.put("customDimensionName", dimJson.get("combosearch"));
                        jobj.put("dimBasedSearchJson", putSearchJson.toString());
                    }
                    else{
                        paramJobj.put("customDimensionName", dimJson.get("combosearch"));
                        paramJobj.put("dimBasedSearchJson", putSearchJson.toString());
                        jobj = getNewMonthlyMYOBtradingreport(paramJobj, jobj, true);
                    }
                    jArrObj.put(jobj);
                }
            }
            returnObj.put("dimMonthlyData", jArrObj);
        } catch (ParseException ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnObj;
    }
    
     private JSONObject populateBalanceSheetJson(JSONObject jobj) throws JSONException{
         JSONObject jobj1 = jobj.getJSONObject(Constants.RES_data);
         JSONArray rightObjArr = new JSONArray();
         JSONArray rightObjArr1 = jobj1.getJSONArray("right");
         JSONArray leftObjArr = jobj1.getJSONArray("left");
         JSONObject objlast = new JSONObject();
         objlast.put("accountid", "");
         objlast.put("level", 0);
         objlast.put("isdebit", false);
         objlast.put("leaf", true);
         objlast.put("accountname", "Assets");
         objlast.put("amount", "");
         objlast.put("fmt", "B");

         rightObjArr.put(0, objlast);
         for (int i = 0; i < rightObjArr1.length(); i++) {
             rightObjArr.put(rightObjArr1.getJSONObject(i));
         }

         objlast = new JSONObject();
         objlast.put("accountid", "");
         objlast.put("level", 0);
         objlast.put("isdebit", false);
         objlast.put("leaf", true);
         objlast.put("accountname", "Liabilities");
         objlast.put("amount", "");
         objlast.put("fmt", "B");

         rightObjArr.put(objlast);

         objlast = new JSONObject();
         objlast.put("accountid", "");
         objlast.put("level", 0);
         objlast.put("isdebit", false);
         objlast.put("leaf", true);
         objlast.put("accountname", "Equity");
         objlast.put("amount", "");
         objlast.put("fmt", "B");

         for (int i = 0; i < leftObjArr.length(); i++) {
             rightObjArr.put(leftObjArr.getJSONObject(i));
         }
         jobj.getJSONObject(Constants.RES_data).remove("left");
         jobj.put(Constants.RES_data, rightObjArr);
         return jobj;
     }
    
    /*
     This method fetched data for Monthly Profit And Loss Custom Layout, Monthly Balance Sheet Custom Layout, and Dimension Based Monthly Profit and Loss Custom Layout
     "Monthly Profit And Loss Report" and "Dimension Based Monthly Profit and Loss Report" can be accessed from inside a Profit-Loss type Custom layout
     "Monthly Balance Sheet Report" can be accessed from inside a Balance Sheet type Custom layout
     */
    public JSONObject getMonthlyCustomLayout(JSONObject paramJobj, ExtraCompanyPreferences extrapref, String companyid) throws ServiceException, SessionExpiredException, JSONException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArrL = new JSONArray();
            boolean isShowZeroAmountsAsBlank = false, isShowAccountCode = true;
            
            //reportid is the variable which distinguishes between three reports with its value
            int reportid = paramJobj.optInt("reportid");//Monthly PnL -> 68, Monthly BS -> 69, Dimension Based MOnthly PnL -> 70

            //In case if Dimension Based Monthly PL report, dimensions data is fetched from searchJson and put into a JSONarray
            JSONArray selectedDimensionsJArr = new JSONArray();
            String combosearch = "", searchText = "";
            String searchJsonString = paramJobj.optString(Constants.Acc_Search_Json);
            if (reportid == Constants.dimensionBasedMonthlyPLCustomLayout && !StringUtil.isNullOrEmpty(searchJsonString)) {
                JSONObject searchJson = new JSONObject(searchJsonString);
                JSONArray rootJArr = searchJson.optJSONArray(Constants.root);
                JSONObject searchJobj = rootJArr.optJSONObject(0);
                try {
                    combosearch = StringUtil.DecodeText(searchJobj.optString("combosearch"));
                } catch (Exception e) {
                    combosearch = searchJobj.optString("combosearch");
                }
                searchText = searchJobj.optString(Constants.searchText);
                String[] combosearchItems = combosearch.split("\\s*,\\s*");
                String[] searchTextItems = searchText.split(",");
                for (int i = 0; i < combosearchItems.length; i++) {
                    JSONObject tempSearchJson = new JSONObject();
                    JSONArray tempRootJArr = new JSONArray();
                    JSONObject tempSearchJobj = new JSONObject(searchJobj.toString());
                    tempSearchJobj.put("combosearch", combosearchItems[i]);
                    tempSearchJobj.put(Constants.searchText, searchTextItems[i]);
                    tempRootJArr.put(0, tempSearchJobj);
                    tempSearchJson.put(Constants.root, tempRootJArr);
                    selectedDimensionsJArr.put(tempSearchJson);
                }
            }
            
            //List of Dimensions selected by user in case of Dimension Based Monthyl PL, this is used while creating headers for columns to be shown in the report
            List<String> dimensionTitlesList = new ArrayList<String>();
            if (!StringUtil.isNullOrEmpty(combosearch)) {
                dimensionTitlesList = Arrays.asList(combosearch.split("\\s*,\\s*"));
            }

            if (extrapref != null) {//Flags based on check-boxes in System Control under Financial Reports Settings
                paramJobj.put("stockValuationFlag", extrapref.isStockValuationFlag());
                isShowZeroAmountsAsBlank = extrapref.isShowZeroAmountAsBlank();
                isShowAccountCode = extrapref.isShowAccountCodeInFinancialReport();
            }

            //Flags to indicate which columns are to be shown in the report so that only those columns' data will be fetched
            boolean isShowPreiodAmounts = false;
            boolean isShowTotalPreiodAmounts = false;
            boolean isShowBudgetAmounts = false;
            boolean isShowTotalBudgetAmounts = false;
            boolean isShowYTDPeriodAmount = false;
            boolean isShowYTDBudgetAmount = false;
            boolean isShowYTDVariance = false;
            boolean isShowMonthlyTotalInDimensionPnL = false;
            String reportViewType = paramJobj.optString("reportViewType");
            if (StringUtil.isNullOrEmpty(reportViewType)) {
                isShowPreiodAmounts = true; //Default Columns to Show in the monthly P&L report in case user does not select any Columns to Show from Combo-box
                isShowTotalPreiodAmounts = true;
            } else {//Columns to Show in the monthly P&L report when user selects which columns are to be shown from combo-box
                String[] reportViewTypeArr = reportViewType.split(",");
                for (int i = 0; i < reportViewTypeArr.length; i++) {
                    if (StringUtil.equal(reportViewTypeArr[i], "0")) {
                        isShowPreiodAmounts = true;
                    } else if (StringUtil.equal(reportViewTypeArr[i], "1")) {
                        isShowTotalPreiodAmounts = true;
                    } else if (StringUtil.equal(reportViewTypeArr[i], "2")) {
                        isShowBudgetAmounts = true;
                    } else if (StringUtil.equal(reportViewTypeArr[i], "3")) {
                        isShowTotalBudgetAmounts = true;
                    } else if (StringUtil.equal(reportViewTypeArr[i], "4")) {
                        isShowYTDPeriodAmount = true;
                    } else if (StringUtil.equal(reportViewTypeArr[i], "5")) {
                        isShowYTDBudgetAmount = true;
                    } else if (StringUtil.equal(reportViewTypeArr[i], "6")) {
                        isShowYTDVariance = true;
                    } else if (StringUtil.equal(reportViewTypeArr[i], "7")) {
                        isShowMonthlyTotalInDimensionPnL = true;
                    }
                }
            }

            
            //Creating start and end dates from selected start and end months as well as start and end years
            DateFormat df1 = authHandler.getDateOnlyFormat();
            Date startDate = df1.parse(paramJobj.optString("stdate"));
            Date endDate = df1.parse(paramJobj.optString("enddate"));
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int startMonth = cal.get(Calendar.MONTH);//get start month and year from start-date
            int startYear = cal.get(Calendar.YEAR);
            long startDateLong = cal.getTimeInMillis();
            cal.setTime(endDate);
            int endMonth = cal.get(Calendar.MONTH);//get end month and year from end-date
            int endYear = cal.get(Calendar.YEAR);

            int monthCount = (endMonth - startMonth) + 12 * (endYear - startYear) + 1;//number of months from start to end date

            DateTime date1 = new DateTime(startDateLong);//This variable is used to update start and end dates while iterating over months to get monthly data
            Date start = date1.toDate();
            date1 = date1.plusSeconds(86399);//Add 86399 seconds to set time to 00H:00M:00S to 23H:59M:59S
            date1 = date1.dayOfMonth().withMaximumValue();
            Date end = date1.toDate();
            DateFormat df = authHandler.getGlobalDateFormatInRequestFormat();


            /*  
            Check to show all accounts. In this method, All accounts are fetched first and then accounts with all amounts zero are removed at the end.
            This has been done because for a month amount for an account may be zero but for some other month it may not be zero.
            */
            paramJobj.put("monthlyreport", "MonthlyReport");
            
            /*
             Fetching Accounts' data along with total period amounts over selected date range.
             This code has been put outside a check on flag because this needs to be executed in all cases, reason being it
             also fetches accounts to be displayed in the layout in which we need to display budget data.
             */
            paramJobj.put("stdate", df.format(startDate));
            paramJobj.put("enddate", df.format(endDate));
            JSONArray jArr = new JSONArray();
            double tradingAmount[] = {0, 0, 0, 0, 0, 0, 0, 0};
            tradingAmount = getBSorPL_CustomLayout(paramJobj, jArr, companyid, extrapref);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject tempJobj1 = jArr.optJSONObject(i);
                tempJobj1.put("totalperiodamount", tempJobj1.opt("periodamount"));
                tempJobj1.put("isallbalanceszero", (tempJobj1.optDouble("periodamount", 0.0D) == 0.0 && tempJobj1.optBoolean("accountflag")) ? true : false); //Flag to check if all balances for the account are zero
                if (tempJobj1.has("periodamount")) {
                    tempJobj1.remove("periodamount");
                }
                if (tempJobj1.has("preperiodamount")) {
                    tempJobj1.remove("preperiodamount");
                }
                if (tempJobj1.has("openingamount")) {
                    tempJobj1.remove("openingamount");
                }
                if (tempJobj1.has("preopeningamount")) {
                    tempJobj1.remove("preopeningamount");
                }
                if (tempJobj1.has("amount")) {
                    tempJobj1.remove("amount");
                }
                if (tempJobj1.has("preamount")) {
                    tempJobj1.remove("preamount");
                }
                jArrL.put(i, tempJobj1);
            }

            //To fetch total amounts over selected period for Dimension Based Monthly PnL Layout
            if (reportid == Constants.dimensionBasedMonthlyPLCustomLayout && isShowTotalPreiodAmounts) {
                paramJobj.put(Constants.Acc_Search_Json, searchJsonString);
                jArrL = getCustomLayoutDataJson(jArrL, paramJobj, companyid, extrapref, "totalperiodamount", "isallbalanceszero");
                for (int j = 0; j < selectedDimensionsJArr.length(); j++) {
                    paramJobj.put(Constants.Acc_Search_Json, selectedDimensionsJArr.optJSONObject(j).toString());
                    jArrL = getCustomLayoutDataJson(jArrL, paramJobj, companyid, extrapref, ("totalperiodamount_" + j), "isallbalanceszero");
                }
            }

            //For Monthly period Amounts
            if (isShowPreiodAmounts || reportid == Constants.dimensionBasedMonthlyPLCustomLayout && isShowMonthlyTotalInDimensionPnL) {
                for (int i = 0; i < monthCount; i++) {
                    paramJobj.put("stdate", df.format(start));
                    paramJobj.put("enddate", df.format(end));
                    if (reportid == Constants.dimensionBasedMonthlyPLCustomLayout) {
                        paramJobj.put(Constants.Acc_Search_Json, searchJsonString);
                        if (isShowMonthlyTotalInDimensionPnL) {//Add monthly total over selected dimensions column on check
                            jArrL = getCustomLayoutDataJson(jArrL, paramJobj, companyid, extrapref, "periodamount_" + i, "isallbalanceszero");
                        }
                        if (isShowPreiodAmounts) {//Add monthly amounts for dimensions on check
                            for (int j = 0; j < selectedDimensionsJArr.length(); j++) {
                                paramJobj.put(Constants.Acc_Search_Json, selectedDimensionsJArr.optJSONObject(j).toString());
                                jArrL = getCustomLayoutDataJson(jArrL, paramJobj, companyid, extrapref, ("periodamount_" + i + "_" + j), "isallbalanceszero");
                            }
                        }
                    } else {//To fetch monthly period amounts in case of Monthly PnL or Monthly BS layouts
                        jArr = new JSONArray();
                        tradingAmount = getBSorPL_CustomLayout(paramJobj, jArr, companyid, extrapref);
                        for (int j = 0; j < jArr.length(); j++) {
                            JSONObject tempJobj1 = jArr.optJSONObject(j);
                            JSONObject tempJobj2 = jArrL.optJSONObject(j);
                            tempJobj2.put("periodamount_" + i, tempJobj1.opt("periodamount"));
                            if (tempJobj1.optDouble("periodamount", 0.0D) != 0.0) {
                                tempJobj2.put("isallbalanceszero", false);
                            }
                            jArrL.put(j, tempJobj2);
                        }
                    }
                    date1 = date1.plusSeconds(1);//Add one second to date1 to go to first day of next month and set this date as start for next iteration
                    start = date1.toDate();
                    date1 = date1.plusSeconds(86399);//Add 86399 seconds to date1 to go to last second of first day of month. This updates time from 00H:00M:00S to 23H:59M:59S
                    date1 = date1.dayOfMonth().withMaximumValue();
                    end = date1.toDate();
                }
            }

            //For monthly budget data. Used only in Monthly PnL Layout. Only when user selects to show Monthly Budget amounts or Total Budget Amounts for accounts.
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put(Constants.companyKey, companyid);
            if (isShowBudgetAmounts || isShowTotalBudgetAmounts) {
                for (int i = 0; i < jArrL.length(); i++) {
                    JSONObject tempJobj = jArrL.optJSONObject(i);
                    if (tempJobj.optBoolean("accountflag", false)) {
                        String accountid = tempJobj.optString("accountid");
                        paramsMap.put("accountid", accountid);
                        paramsMap.put("year", startYear);
                        double totalBudgetAmount = 0.0D;
                        double monthBudget = 0.0D;
                        int monthNumber = startMonth;
                        int index = 0;//Index corresponding to the month in the columns' dataIndex
                        KwlReturnObject monthlyBudgetObj = accAccountDAOobj.getMonthlyBudget(paramsMap);
                        List monthlyBudgetList = monthlyBudgetObj.getEntityList();
                        if (monthlyBudgetList != null && !monthlyBudgetList.isEmpty()) {
                            AccountBudget accountBudget = (AccountBudget) monthlyBudgetList.get(0);
                            while (index < monthCount) {
                                monthBudget = getBudgetAmountForMonthByMonthNumber(accountBudget, monthNumber);
                                monthBudget = authHandler.round(monthBudget, companyid);
                                tempJobj.put("budgetamount_" + index, monthBudget > 0 ? monthBudget : (isShowZeroAmountsAsBlank ? "" : authHandler.round(0.0D, companyid)));
                                if (monthBudget != 0.0D) {
                                    tempJobj.put("isallbalanceszero", false);
                                }
                                totalBudgetAmount += monthBudget > 0 ? monthBudget : 0.0D;
                                monthNumber = (monthNumber + 1) % 12;
                                index++;
                                if (monthNumber == 0) {
                                    break;
                                }
                            }
                        } else {
                            while (index < monthCount) {    //If AccountBudget is not set for the year, then set budget amounts as zero
                                tempJobj.put("budgetamount_" + index, isShowZeroAmountsAsBlank ? "" : authHandler.round(0.0D, companyid));
                                monthNumber = (monthNumber + 1) % 12;
                                index++;
                                if (monthNumber == 0) {
                                    break;
                                }
                            }
                        }
                        if (endYear != startYear) { //if start year is not same as end year, get budget for end year separately
                            monthNumber = 0;//Set Month Number to 0 (index of January) for next year's budget calculation
                            paramsMap.put("year", endYear);
                            monthlyBudgetObj = accAccountDAOobj.getMonthlyBudget(paramsMap);
                            monthlyBudgetList = monthlyBudgetObj.getEntityList();
                            if (monthlyBudgetList != null && !monthlyBudgetList.isEmpty()) {
                                AccountBudget accountBudget = (AccountBudget) monthlyBudgetList.get(0);
                                while ((monthNumber <= endMonth)) {
                                    monthBudget = getBudgetAmountForMonthByMonthNumber(accountBudget, monthNumber);
                                    monthBudget = authHandler.round(monthBudget, companyid);
                                    tempJobj.put("budgetamount_" + index, monthBudget > 0 ? monthBudget : (isShowZeroAmountsAsBlank ? "" : authHandler.round(0.0D, companyid)));
                                    if (monthBudget != 0.0D) {
                                        tempJobj.put("isallbalanceszero", false);
                                    }
                                    totalBudgetAmount += monthBudget > 0 ? monthBudget : 0.0D;
                                    monthNumber = (monthNumber + 1) % 12;
                                    index++;
                                }
                            } else {
                                while ((monthNumber <= endMonth)) { //If AccountBudget is not set for the year, then set budget amounts as zero
                                    tempJobj.put("budgetamount_" + index, isShowZeroAmountsAsBlank ? "" : authHandler.round(0.0D, companyid));
                                    monthNumber = (monthNumber + 1) % 12;
                                    index++;
                                }
                            }
                        }
                        totalBudgetAmount = authHandler.round(totalBudgetAmount, companyid);
                        tempJobj.put("totalbudgetamount", (isShowZeroAmountsAsBlank && totalBudgetAmount == 0.0D) ? "" : totalBudgetAmount);
                        jArrL.put(i, tempJobj);
                    }
                }
            }

            //Updating start date to start date of Financial year (fetched from CompanyAccountPreferences)
            KwlReturnObject cap = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            Date financialYearStartDate = preferences.getFinancialYearFrom();   //start date of current financial year, to be used as start-date for YTD data
            cal.setTime(financialYearStartDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            int financialYearStartMonth = cal.get(Calendar.MONTH);
            int financialYearStartYear = cal.get(Calendar.YEAR);
            long financialYearStartDateLong = cal.getTimeInMillis();
            date1 = new DateTime(financialYearStartDateLong);
            paramJobj.put("stdate", df.format(date1.toDate()));

            //Updating end date to today's date which is end-date for Year To Date amounts
            Date todaysDate = new Date();   //today's date, to be used as end-date for YTD data
            cal.setTime(todaysDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            int todaysMonth = cal.get(Calendar.MONTH);
            int todaysYear = cal.get(Calendar.YEAR);
            long todaysDateLong = cal.getTimeInMillis();
            date1 = new DateTime(todaysDateLong);
            paramJobj.put("enddate", df.format(date1.toDate()));
            int monthCountForYTD = (todaysMonth - financialYearStartMonth) + 12 * (todaysYear - financialYearStartYear) + 1;//number of months for Year-To-Date period

            //For Year to Date Period Amount(s). YTD period amounts are also required for calculation of YTD variance, therefore also fetched if isShowYTDVariance flag is true
            if (isShowYTDPeriodAmount || isShowYTDVariance) {
                if (reportid == Constants.dimensionBasedMonthlyPLCustomLayout) {
                    paramJobj.put(Constants.Acc_Search_Json, searchJsonString);
                    jArrL = getCustomLayoutDataJson(jArrL, paramJobj, companyid, extrapref, "yeartodateperiodamount", "isallbalanceszero");
                    for (int j = 0; j < selectedDimensionsJArr.length(); j++) {
                        paramJobj.put(Constants.Acc_Search_Json, selectedDimensionsJArr.optJSONObject(j).toString());
                        jArrL = getCustomLayoutDataJson(jArrL, paramJobj, companyid, extrapref, ("yeartodateperiodamount_" + j), "isallbalanceszero");
                    }
                } else {
                    jArr = new JSONArray();
                    tradingAmount = getBSorPL_CustomLayout(paramJobj, jArr, companyid, extrapref);  //to fetch Year To Date Period amounts for accounts
                    for (int j = 0; j < jArrL.length(); j++) {   //to add Year To Date Period amounts for accounts into JSONObject
                        JSONObject tempJobj2 = jArrL.optJSONObject(j);
                        JSONObject tempJobj1 = jArr.optJSONObject(j);
                        tempJobj2.put("yeartodateperiodamount", tempJobj1.opt("periodamount"));
                        if (tempJobj1.optDouble("periodamount", 0.0D) != 0.0D) {
                            tempJobj2.put("isallbalanceszero", false);
                        }
                        jArrL.put(j, tempJobj2);
                    }
                }
            }

            //Year to Date Budget Amount and Variance. YTD budget amounts are also required for calculation of YTD variance, therefore also fetched if isShowYTDVariance flag is true
            //YTD Variance = YTD Period Amount - YTD Budget Amount
            if (isShowYTDBudgetAmount || isShowYTDVariance) {
                for (int i = 0; i < jArrL.length(); i++) {
                    JSONObject tempJobj = jArrL.optJSONObject(i);
                    if (tempJobj.optBoolean("accountflag", false)) {
                        String accountid = tempJobj.optString("accountid");
                        paramsMap.put("accountid", accountid);
                        paramsMap.put("year", financialYearStartYear);
                        double monthBudget = 0.0D;
                        int monthNumber = financialYearStartMonth;
                        double yearToDateBudget = 0.0D;
                        int index = 0;//Index corresponding to the month in the columns' dataIndex
                        KwlReturnObject monthlyBudgetObj = accAccountDAOobj.getMonthlyBudget(paramsMap);
                        List monthlyBudgetList = monthlyBudgetObj.getEntityList();
                        if (monthlyBudgetList != null && !monthlyBudgetList.isEmpty()) {
                            AccountBudget accountBudget = (AccountBudget) monthlyBudgetList.get(0);
                            while (index < monthCountForYTD) {
                                monthBudget = getBudgetAmountForMonthByMonthNumber(accountBudget, monthNumber);
                                yearToDateBudget += monthBudget > 0 ? monthBudget : 0.0D;
                                monthNumber = (monthNumber + 1) % 12;
                                index++;
                                if (monthNumber == 0) {
                                    break;
                                }
                            }
                        }
                        if (todaysYear != financialYearStartYear) {//if start year is not same as end year, get budget for end year separately
                            monthNumber = 0;//Set Month Number to 0 (index of January) for next year's budget calculation
                            paramsMap.put("year", todaysYear);
                            monthlyBudgetObj = accAccountDAOobj.getMonthlyBudget(paramsMap);
                            monthlyBudgetList = monthlyBudgetObj.getEntityList();
                            if (monthlyBudgetList != null && !monthlyBudgetList.isEmpty()) {
                                AccountBudget accountBudget = (AccountBudget) monthlyBudgetList.get(0);
                                while ((monthNumber <= todaysMonth)) {
                                    monthBudget = getBudgetAmountForMonthByMonthNumber(accountBudget, monthNumber);
                                    yearToDateBudget += monthBudget > 0 ? monthBudget : 0.0D;
                                    monthNumber = (monthNumber + 1) % 12;
                                }
                            }
                        }
                        double yearToDateVariance = tempJobj.optDouble("yeartodateperiodamount", 0.0) - yearToDateBudget;//YTD Variance = YTD Period Amount - YTD Budget Amount
                        yearToDateBudget = authHandler.round(yearToDateBudget, companyid);
                        yearToDateVariance = authHandler.round(yearToDateVariance, companyid);
                        tempJobj.put("yeartodatebudgetamount", (isShowZeroAmountsAsBlank && yearToDateBudget == 0.0D) ? "" : yearToDateBudget);
                        tempJobj.put("yeartodatevariance", (isShowZeroAmountsAsBlank && yearToDateVariance == 0.0D) ? "" : yearToDateVariance);
                        if (yearToDateBudget != 0.0D || yearToDateVariance != 0.0D) {
                            tempJobj.put("isallbalanceszero", false);
                        }
                        jArrL.put(i, tempJobj);
                    }
                }
            }

            /*
             Depending upon flag in System Control, remove accounts which have all amounts zero.
             We fetch data for all accounts in this method. Therefore if flag to show zero balance accounts is false, we need to remove accounts with zero balances for all columns.
            */
            if (extrapref != null) {
                boolean isProfitAndLoss = (reportid == Constants.profitAndLossMonthlyCustomLayout || reportid == Constants.dimensionBasedMonthlyPLCustomLayout);
                boolean isBalanceSheet = (reportid == Constants.balanceSheetMonthlyCustomLayout);
                if (isProfitAndLoss && !extrapref.isShowAllAccountsInPnl() || isBalanceSheet && !extrapref.isShowallaccountsinbs()) {
                    jArrL = removeAccountsWithZeroBalances(jArrL, "isallbalanceszero");
                }
            }

            /*
            Updating Totals for Account Groups as Group Totals have not been set in above code for Budget Amounts and YTD Amounts
            Our code above only updates budget amounts and variance amount for accounts. Therefore we have to calculate and update group totals separately
            */
            if (isShowBudgetAmounts || isShowTotalBudgetAmounts || isShowYTDBudgetAmount || isShowYTDVariance) {
                for (int i = jArrL.length() - 1; i > 0; i--) {
                    JSONObject tempJobj = jArrL.optJSONObject(i);
                    if (!tempJobj.optBoolean("accountflag", false) && tempJobj.optBoolean(Constants.Acc_leaf, false) && !StringUtil.isNullOrEmpty(tempJobj.optString("accountid", ""))) {//if accountid is empty, then record is skipped as its a blank row
                        jArrL = updateGroupTotalsRecursively(jArrL, tempJobj.optInt(Constants.Acc_level), i, monthCount, isShowBudgetAmounts, isShowTotalBudgetAmounts, isShowYTDPeriodAmount, isShowYTDBudgetAmount, isShowYTDVariance, isShowZeroAmountsAsBlank, companyid);
                    }
                }
                
                /**
                 * Updating Custom Group Total column budget amounts
                 */
                String templateid = paramJobj.optString("templateid", null);
                HashMap<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put(Constants.companyKey, companyid);
                filterParams.put("templateid", templateid);
                filterParams.put("levelZeroFlag", true);
                filterParams.put("nature", Constants.CUSTOM_LAYOUT_DEFINE_TOTAL);
                KwlReturnObject plresult = accAccountDAOobj.getCustomLayoutGroups(filterParams);
                List<LayoutGroup> list = plresult.getEntityList();
                
                for (int i = 0; i < jArrL.length(); i++) {
                    JSONObject tempJobj = jArrL.optJSONObject(i);
                    for (LayoutGroup group : list) {
                        if (group.getID() != null && group.getID().equalsIgnoreCase(tempJobj.optString("accountid"))) {
                            KwlReturnObject plresult1 = accAccountDAOobj.getCustomsGroupsForTotal(group.getID());
                            List<Groupmapfortotal> list1 = plresult1.getEntityList();
                            double yeartodategroupbudgetamount = 0.0D;
                            double totalgroupbudgetamount = 0.0D;
                            double groupbudgetamount[] = new double[monthCount];
                            if (list1 != null && list1.size() > 0) {
                                for (Groupmapfortotal grouprule : list1) {
                                    String groupAccountid = grouprule.getGroupid() != null ? grouprule.getGroupid().getID() : "";
                                    String ruletype = grouprule.getAction();
                                    for (int j = 0; j < jArrL.length(); j++) {
                                        JSONObject tempJobj1 = jArrL.optJSONObject(j);
                                        if (tempJobj1.optString("accountid").equalsIgnoreCase("Total" + groupAccountid) || tempJobj1.optString("accountid").equalsIgnoreCase(groupAccountid)) {
                                            if (!StringUtil.isNullOrEmpty(ruletype)) {
                                                if (ruletype.equalsIgnoreCase("NULL") || ruletype.equalsIgnoreCase("PLUS")) {
                                                    yeartodategroupbudgetamount += tempJobj1.optDouble("yeartodatebudgetamount", 0.0D);
                                                    totalgroupbudgetamount += tempJobj1.optDouble("totalbudgetamount", 0.0D);
                                                    for (int m = 0; m < monthCount; m++) {
                                                        groupbudgetamount[m] += tempJobj1.optDouble("budgetamount_" + m, 0.0D);
                                                    }
                                                } else if (ruletype.equalsIgnoreCase("MINUS")) {
                                                    yeartodategroupbudgetamount -= tempJobj1.optDouble("yeartodatebudgetamount", 0.0D);
                                                    totalgroupbudgetamount -= tempJobj1.optDouble("totalbudgetamount", 0.0D);
                                                    for (int m = 0; m < monthCount; m++) {
                                                        groupbudgetamount[m] -= tempJobj1.optDouble("budgetamount_" + m, 0.0D);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (isShowYTDBudgetAmount) {
                                tempJobj.put("yeartodatebudgetamount", authHandler.round(yeartodategroupbudgetamount, companyid));
                            }
                            if (isShowYTDVariance) {
                                double yeartodategroupvarianceamount = tempJobj.optDouble("yeartodateperiodamount", 0.0) - yeartodategroupbudgetamount;
                                tempJobj.put("yeartodatevariance", authHandler.round(yeartodategroupvarianceamount, companyid));
                            }
                            if (isShowTotalBudgetAmounts) {
                                tempJobj.put("totalbudgetamount", authHandler.round(totalgroupbudgetamount, companyid));
                            }
                            if (isShowBudgetAmounts) {
                                for (int n = 0; n < monthCount; n++) {
                                    tempJobj.put("budgetamount_" + n, authHandler.round(groupbudgetamount[n], companyid));
                                }
                            }
                        }
                    }
                }
            }

            //Creating Columns for Report Grid
            JSONObject columnsJobj = createColumnsForMonthlyCustomLayout(monthCount, startMonth, startYear, paramJobj, reportid, isShowBudgetAmounts, isShowTotalBudgetAmounts, isShowPreiodAmounts, isShowTotalPreiodAmounts, isShowYTDPeriodAmount, isShowYTDBudgetAmount, isShowYTDVariance, isShowAccountCode, isShowMonthlyTotalInDimensionPnL, dimensionTitlesList);

            JSONObject fobj = new JSONObject();
            fobj.put("left", jArrL);
            fobj.put("columns", columnsJobj.optJSONArray("columns"));
            fobj.put("metaData", columnsJobj.optJSONObject("metaData"));
            jobj.put(Constants.RES_data, fobj);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("AccFinancialReportsServiceImpl.getMonthlyCustomLayout : " + e.getMessage(), e);
        }
        return jobj;
    }

    /*
    This method calls method getBSorPL_CustomLayout with parameters passed to it.
    Then it gets the value of "periodamount" key from the resultset and puts it in our JSONArray against desired key.
    Also puts/updates the value of allBalancesZeroKey which is a flag to indicate whether all amounts in a record are zero of not.
    */
    public JSONArray getCustomLayoutDataJson(JSONArray jArrL, JSONObject paramJobj, String companyid, ExtraCompanyPreferences extrapref, String putKey, String allBalancesZeroKey) throws JSONException, ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        double tradingAmount[] = getBSorPL_CustomLayout(paramJobj, jArr, companyid, extrapref);
        for (int j = 0; j < jArr.length(); j++) {
            JSONObject tempJobj1 = jArr.optJSONObject(j);
            JSONObject tempJobj2 = jArrL.optJSONObject(j);
            tempJobj2.put(putKey, tempJobj1.opt("periodamount"));
            if (tempJobj1.optDouble("periodamount", 0.0D) != 0.0) {
                tempJobj2.put(allBalancesZeroKey, false);
            }
            jArrL.put(j, tempJobj2);
        }
        return jArrL;
    }

    //allBalancesZeroKey is the key which indicates whether all balances for an account are zero.
    //If the value against key allBalancesZeroKey is true, it means all balances are zero for respective account
    public JSONArray removeAccountsWithZeroBalances(JSONArray jArr, String allBalancesZeroKey) {
        JSONArray newJArr = new JSONArray();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject tempJobj = jArr.optJSONObject(i);
            if (!tempJobj.optBoolean(allBalancesZeroKey, false)) {
                if (tempJobj.has(allBalancesZeroKey)) {
                    tempJobj.remove(allBalancesZeroKey);
                }
                newJArr.put(tempJobj);
            }
        }
        return newJArr;
    }

    //This method returns budget for a month when month's index (between 0 & 11) is passed
    public double getBudgetAmountForMonthByMonthNumber(AccountBudget accountBudget, int monthNumber) {
        double monthBudget = 0.0D;
        switch (monthNumber) {
            case 0:
                monthBudget = accountBudget.getJan();
                break;
            case 1:
                monthBudget = accountBudget.getFeb();
                break;
            case 2:
                monthBudget = accountBudget.getMarch();
                break;
            case 3:
                monthBudget = accountBudget.getApril();
                break;
            case 4:
                monthBudget = accountBudget.getMay();
                break;
            case 5:
                monthBudget = accountBudget.getJune();
                break;
            case 6:
                monthBudget = accountBudget.getJuly();
                break;
            case 7:
                monthBudget = accountBudget.getAug();
                break;
            case 8:
                monthBudget = accountBudget.getSept();
                break;
            case 9:
                monthBudget = accountBudget.getOct();
                break;
            case 10:
                monthBudget = accountBudget.getNov();
                break;
            case 11:
                monthBudget = accountBudget.getDecember();
                break;
            default:
                break;
        }
        return monthBudget;
    }

    /*
    This method calculates and updates totals for groups recursively in a JSONArray containing data for a custom layout.
    It starts from the botton of a group and iterates over JSONArray containing the data. Whenever we encounter another group inside our group, we call the same method with appropriate parameters.
    Paramters for the method are: jArr -> contains data, level -> level of group in the hierarchy, index -> index of Total record of group, And some flags to indicate which amounts are present in the data
    */
    public JSONArray updateGroupTotalsRecursively(JSONArray jArr, int level, int index, int monthCount, boolean isShowBudgetAmounts, boolean isShowTotalBudgetAmounts, boolean isShowYTDPeriodAmount, boolean isShowYTDBudgetAmount, boolean isShowYTDVariance, boolean isShowZeroAmountsAsBlank, String companyid) throws JSONException {
        int tempIndex = index - 1;
        JSONObject tempJobj = jArr.optJSONObject(tempIndex);
        int tempLevel = tempJobj.optInt(Constants.Acc_level);
        double yeartodategroupbudgetamount = 0.0D;
        double totalgroupbudgetamount = 0.0D;
        double groupbudgetamount[] = new double[monthCount];
        while (tempLevel > level) {
            if (!tempJobj.optBoolean("accountflag", false)) {//If account flag is false, it means we are at end of a subgroup, so call same methdo for the subgroup
                jArr = updateGroupTotalsRecursively(jArr, tempLevel, tempIndex, monthCount, isShowBudgetAmounts, isShowTotalBudgetAmounts, isShowYTDPeriodAmount, isShowYTDBudgetAmount, isShowYTDVariance, isShowZeroAmountsAsBlank, companyid);
                tempJobj = jArr.optJSONObject(tempIndex);
            } else {//If the record contains an account's data, we update our amounts accordingly
                yeartodategroupbudgetamount += tempJobj.optDouble("yeartodatebudgetamount", 0.0D);
                totalgroupbudgetamount += tempJobj.optDouble("totalbudgetamount", 0.0D);
                for (int i = 0; i < monthCount; i++) {
                    groupbudgetamount[i] += tempJobj.optDouble("budgetamount_" + i, 0.0D);
                }
            }
            tempIndex--;
            tempJobj = jArr.optJSONObject(tempIndex);
            tempLevel = tempJobj.optInt(Constants.Acc_level);
        }
        tempJobj = jArr.optJSONObject(index);
        
        //Check whether record is an empty row. If the record is not an empty row, update group totals
        if (tempJobj.optBoolean(Constants.Acc_leaf, false) && !StringUtil.isNullOrEmpty(tempJobj.optString("accountid", ""))) {
            if (isShowYTDBudgetAmount) {
                tempJobj.put("yeartodatebudgetamount", authHandler.round(yeartodategroupbudgetamount, companyid));
            }
            if (isShowYTDVariance) {
                double yeartodategroupvarianceamount = tempJobj.optDouble("yeartodateperiodamount", 0.0) - yeartodategroupbudgetamount;
                tempJobj.put("yeartodatevariance", authHandler.round(yeartodategroupvarianceamount, companyid));
            }
            if (isShowTotalBudgetAmounts) {
                tempJobj.put("totalbudgetamount", authHandler.round(totalgroupbudgetamount, companyid));
            }
            if (isShowBudgetAmounts) {
                for (int i = 0; i < monthCount; i++) {
                    tempJobj.put("budgetamount_" + i, authHandler.round(groupbudgetamount[i], companyid));
                }
            }
        }
        jArr.put(index, tempJobj);
        return jArr;
    }

    /*
    This method creates columns and metaData for following three reports based on the parameters passed to it
    1. Monthly PnL Custom layout
    2. Monthly Balance Sheet Custom layout
    3. Dimension Based Monhtly PnL Custom Layout
    */
    public JSONObject createColumnsForMonthlyCustomLayout(int monthCount, int startMonth, int startYear, JSONObject paramJobj, int reportid, boolean isShowBudgetAmounts, boolean isShowTotalBudgetAmounts, boolean isShowPreiodAmounts, boolean isShowTotalPreiodAmounts, boolean isShowYTDPeriodAmount, boolean isShowYTDBudgetAmount, boolean isShowYTDVariance, boolean isShowAccountCode, boolean isShowMonthlyTotalInDimensionPnL, List<String> dimensionTitlesList) throws JSONException {
        JSONObject columnsJobj = new JSONObject();
        JSONArray fieldsJarr = new JSONArray();
        JSONArray columnsJarr = new JSONArray();
        Locale locale = Locale.forLanguageTag(paramJobj.getString(Constants.language));

        //Add Particulars (Account/Group Name) column
        JSONObject jobjTemp = new JSONObject();
        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.balanceSheet.particulars", null, locale) + "</b>");
        jobjTemp.put("dataIndex", "accountname");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("sortable", true);
        jobjTemp.put("renderer", "this.formatAccountName");
        columnsJarr.put(jobjTemp);

        //Add Particular dataIndex to store fields' array
        JSONObject jobjTemp1 = new JSONObject();
        jobjTemp1.put(Constants.Acc_name, "accountname");
        fieldsJarr.put(jobjTemp1);

        if (isShowAccountCode) {//Add account code column only if its check is enabled in Syatem Controls
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.coa.accCode", null, locale) + "</b>");
            jobjTemp.put("align", "center");
            jobjTemp.put("dataIndex", "accountcode");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("renderer", "this.formatAccountName");
            columnsJarr.put(jobjTemp);

            jobjTemp1 = new JSONObject();
            jobjTemp1.put(Constants.Acc_name, "accountcode");
            fieldsJarr.put(jobjTemp1);
        }

        int monthNumber = startMonth;//index of month
        int year = startYear;
        DateFormatSymbols dfs = new DateFormatSymbols();

        for (int i = 0; i < monthCount; i++) {
            if (monthNumber == 0 && i > 0) {//If monthNumber goes to January (index 0), then increase the year by one to go to next year
                year += 1;
            }
            String monthName = dfs.getShortMonths()[monthNumber];//Get Name of month (like 'Jan', 'Feb') from month index (index between 0-11)
            String columnHeader = monthName + " " + year;

            if (isShowPreiodAmounts || (reportid == Constants.dimensionBasedMonthlyPLCustomLayout) && isShowMonthlyTotalInDimensionPnL) {
                if (reportid == Constants.dimensionBasedMonthlyPLCustomLayout) {
                    if (isShowPreiodAmounts) {
                        for (int j = 0; j < dimensionTitlesList.size(); j++) {//Loop to add columns for monthly dimensionwise amounts
                            String dimensionTitle = dimensionTitlesList.get(j);
                            jobjTemp = new JSONObject();
                            jobjTemp.put("header", "<b>" + dimensionTitle + " - " + columnHeader + "</b>");
                            jobjTemp.put("dataIndex", "periodamount_" + i + "_" + j);
                            jobjTemp.put("align", "right");
                            jobjTemp.put("width", 135);
                            jobjTemp.put("pdfwidth", 135);
                            jobjTemp.put("renderer", "this.formatMoney");
                            jobjTemp.put("summaryRenderer", "this.formatMoney");
                            columnsJarr.put(jobjTemp);

                            jobjTemp1 = new JSONObject();
                            jobjTemp1.put(Constants.Acc_name, "periodamount_" + i + "_" + j);
                            fieldsJarr.put(jobjTemp1);
                        }
                    }
                    if (isShowMonthlyTotalInDimensionPnL) {//Add month total column
                        jobjTemp = new JSONObject();
                        jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.common.total", null, locale) + " - " + columnHeader + "</b>");
                        jobjTemp.put("dataIndex", "periodamount_" + i);
                        jobjTemp.put("align", "right");
                        jobjTemp.put("width", 135);
                        jobjTemp.put("pdfwidth", 135);
                        jobjTemp.put("renderer", "this.formatMoney");
                        jobjTemp.put("summaryRenderer", "this.formatMoney");
                        columnsJarr.put(jobjTemp);

                        jobjTemp1 = new JSONObject();
                        jobjTemp1.put(Constants.Acc_name, "periodamount_" + i);
                        fieldsJarr.put(jobjTemp1);
                    }
                } else {//Add Monthly Actual Amount column in case of Monthly PnL or Monthly BS report
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.forecast.actual", null, locale) + " - " + columnHeader + "</b>");
                    jobjTemp.put("dataIndex", "periodamount_" + i);
                    jobjTemp.put("align", "right");
                    jobjTemp.put("width", 135);
                    jobjTemp.put("pdfwidth", 135);
                    jobjTemp.put("renderer", "this.formatMoney");
                    jobjTemp.put("summaryRenderer", "this.formatMoney");
                    columnsJarr.put(jobjTemp);

                    jobjTemp1 = new JSONObject();
                    jobjTemp1.put(Constants.Acc_name, "periodamount_" + i);
                    fieldsJarr.put(jobjTemp1);
                }
            }

            if (isShowBudgetAmounts) {//Add monthly budget amount column. Executed in case of Monthly PnL report only
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.forecast.budget", null, locale) + " - " + columnHeader + "</b>");
                jobjTemp.put("dataIndex", "budgetamount_" + i);
                jobjTemp.put("align", "right");
                jobjTemp.put("width", 135);
                jobjTemp.put("pdfwidth", 135);
                jobjTemp.put("renderer", "this.formatMoney");
                jobjTemp.put("summaryRenderer", "this.formatMoney");
                columnsJarr.put(jobjTemp);

                jobjTemp1 = new JSONObject();
                jobjTemp1.put(Constants.Acc_name, "budgetamount_" + i);
                fieldsJarr.put(jobjTemp1);
            }

            monthNumber = (monthNumber + 1) % 12;
        }

        if (isShowTotalPreiodAmounts) {//Code to add columns for total amounts over selected date range
            if (reportid == Constants.dimensionBasedMonthlyPLCustomLayout) {
                for (int j = 0; j < dimensionTitlesList.size(); j++) {//Add dimensionwise total column
                    String dimensionTitle = dimensionTitlesList.get(j);
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.common.total", null, locale) + " - " + dimensionTitle + "</b>");
                    jobjTemp.put("dataIndex", "totalperiodamount_" + j);
                    jobjTemp.put("align", "right");
                    jobjTemp.put("width", 135);
                    jobjTemp.put("pdfwidth", 135);
                    jobjTemp.put("renderer", "this.formatMoney");
                    jobjTemp.put("summaryRenderer", "this.formatMoney");
                    columnsJarr.put(jobjTemp);

                    jobjTemp1 = new JSONObject();
                    jobjTemp1.put(Constants.Acc_name, "totalperiodamount_" + j);
                    fieldsJarr.put(jobjTemp1);
                }
                jobjTemp = new JSONObject();    //Add overall total over selected range column
                jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.1099.gridTotalAmt", null, locale) + "</b>");
                jobjTemp.put("dataIndex", "totalperiodamount");
                jobjTemp.put("align", "right");
                jobjTemp.put("width", 135);
                jobjTemp.put("pdfwidth", 135);
                jobjTemp.put("renderer", "this.formatMoney");
                jobjTemp.put("summaryRenderer", "this.formatMoney");
                columnsJarr.put(jobjTemp);

                jobjTemp1 = new JSONObject();
                jobjTemp1.put(Constants.Acc_name, "totalperiodamount");
                fieldsJarr.put(jobjTemp1);
            } else {
                jobjTemp = new JSONObject();//Add overall total over selected range column, for Monthly PnL or Monthly BS
                jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.forecast.totalactual", null, locale) + "</b>");
                jobjTemp.put("dataIndex", "totalperiodamount");
                jobjTemp.put("align", "right");
                jobjTemp.put("width", 135);
                jobjTemp.put("pdfwidth", 135);
                jobjTemp.put("renderer", "this.formatMoney");
                jobjTemp.put("summaryRenderer", "this.formatMoney");
                columnsJarr.put(jobjTemp);

                jobjTemp1 = new JSONObject();
                jobjTemp1.put(Constants.Acc_name, "totalperiodamount");
                fieldsJarr.put(jobjTemp1);
            }
        }

        if (isShowTotalBudgetAmounts) {//YTD budget amount column, only in Monthly PL report
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.forecast.totalbudget", null, locale) + "</b>");
            jobjTemp.put("dataIndex", "totalbudgetamount");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 135);
            jobjTemp.put("pdfwidth", 135);
            jobjTemp.put("renderer", "this.formatMoney");
            jobjTemp.put("summaryRenderer", "this.formatMoney");
            columnsJarr.put(jobjTemp);

            jobjTemp1 = new JSONObject();
            jobjTemp1.put(Constants.Acc_name, "totalbudgetamount");
            fieldsJarr.put(jobjTemp1);
        }

        if (isShowYTDPeriodAmount) {//Columns to display Year To Date period amounts
            if (reportid == Constants.dimensionBasedMonthlyPLCustomLayout) {
                for (int j = 0; j < dimensionTitlesList.size(); j++) {
                    String dimensionTitle = dimensionTitlesList.get(j);
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.header.YTD", null, locale) + " - " + dimensionTitle + "</b>");
                    jobjTemp.put("dataIndex", "yeartodateperiodamount_" + j);
                    jobjTemp.put("align", "right");
                    jobjTemp.put("width", 135);
                    jobjTemp.put("pdfwidth", 135);
                    jobjTemp.put("renderer", "this.formatMoney");
                    jobjTemp.put("summaryRenderer", "this.formatMoney");
                    columnsJarr.put(jobjTemp);

                    jobjTemp1 = new JSONObject();
                    jobjTemp1.put(Constants.Acc_name, "yeartodateperiodamount_" + j);
                    fieldsJarr.put(jobjTemp1);
                }
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.header.YTD", null, locale) + " - " + messageSource.getMessage("acc.common.total", null, locale) + "</b>");
                jobjTemp.put("dataIndex", "yeartodateperiodamount");
                jobjTemp.put("align", "right");
                jobjTemp.put("width", 135);
                jobjTemp.put("pdfwidth", 135);
                jobjTemp.put("renderer", "this.formatMoney");
                jobjTemp.put("summaryRenderer", "this.formatMoney");
                columnsJarr.put(jobjTemp);

                jobjTemp1 = new JSONObject();
                jobjTemp1.put(Constants.Acc_name, "yeartodateperiodamount");
                fieldsJarr.put(jobjTemp1);
            } else {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.header.YTD", null, locale) + " " + messageSource.getMessage("acc.forecast.actual", null, locale) + "</b>");
                jobjTemp.put("dataIndex", "yeartodateperiodamount");
                jobjTemp.put("align", "right");
                jobjTemp.put("width", 135);
                jobjTemp.put("pdfwidth", 135);
                jobjTemp.put("renderer", "this.formatMoney");
                jobjTemp.put("summaryRenderer", "this.formatMoney");
                columnsJarr.put(jobjTemp);

                jobjTemp1 = new JSONObject();
                jobjTemp1.put(Constants.Acc_name, "yeartodateperiodamount");
                fieldsJarr.put(jobjTemp1);
            }
        }

        if (isShowYTDBudgetAmount) {//YTD Budget column, only in Monthly PnL report
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.header.YTD", null, locale) + " " + messageSource.getMessage("acc.forecast.budget", null, locale) + "</b>");
            jobjTemp.put("dataIndex", "yeartodatebudgetamount");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 135);
            jobjTemp.put("pdfwidth", 135);
            jobjTemp.put("renderer", "this.formatMoney");
            jobjTemp.put("summaryRenderer", "this.formatMoney");
            columnsJarr.put(jobjTemp);

            jobjTemp1 = new JSONObject();
            jobjTemp1.put(Constants.Acc_name, "yeartodatebudgetamount");
            fieldsJarr.put(jobjTemp1);
        }

        if (isShowYTDVariance) {//YTD variance column, only in Monthly PnL layout
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.header.YTD", null, locale) + " " + messageSource.getMessage("acc.field.Variance", null, locale) + "</b>");
            jobjTemp.put("dataIndex", "yeartodatevariance");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 135);
            jobjTemp.put("pdfwidth", 135);
            jobjTemp.put("renderer", "this.formatMoney");
            jobjTemp.put("summaryRenderer", "this.formatMoney");
            columnsJarr.put(jobjTemp);

            jobjTemp1 = new JSONObject();
            jobjTemp1.put(Constants.Acc_name, "yeartodatevariance");
            fieldsJarr.put(jobjTemp1);
        }

        //Some more field added into metaData which are used to display data in hierarchical order in the grid
        jobjTemp1 = new JSONObject();
        jobjTemp1.put(Constants.Acc_name, "accountflag");//Indicates whether a record is an account or not
        fieldsJarr.put(jobjTemp1);
        
        jobjTemp1 = new JSONObject();
        jobjTemp1.put(Constants.Acc_name, "accountid");
        fieldsJarr.put(jobjTemp1);
        
        jobjTemp1 = new JSONObject();
        jobjTemp1.put(Constants.Acc_name, "acctype");
        fieldsJarr.put(jobjTemp1);
        
        jobjTemp1 = new JSONObject();
        jobjTemp1.put(Constants.Acc_name, "group");
        fieldsJarr.put(jobjTemp1);

        jobjTemp1 = new JSONObject();
        jobjTemp1.put(Constants.Acc_name, "isdebit");
        jobjTemp1.put(Constants.type, "boolean");
        fieldsJarr.put(jobjTemp1);

        jobjTemp1 = new JSONObject();
        jobjTemp1.put(Constants.Acc_name, "level");
        fieldsJarr.put(jobjTemp1);

        jobjTemp1 = new JSONObject();
        jobjTemp1.put(Constants.Acc_name, "fmt");
        fieldsJarr.put(jobjTemp1);

        jobjTemp1 = new JSONObject();
        jobjTemp1.put(Constants.Acc_name, "leaf");
        fieldsJarr.put(jobjTemp1);

        JSONObject metaDataJobj = new JSONObject();
        metaDataJobj.put("fields", fieldsJarr);
        metaDataJobj.put(Constants.root, "left");//"left" is the key under which reports' data will be sitting in response

        columnsJobj.put("columns", columnsJarr);
        columnsJobj.put("metaData", metaDataJobj);

        return columnsJobj;
    }
    
    @Override
    public JSONArray compareAgedAndbalanceSheetReport(JSONObject jsonParamObj, HashMap<String, Object> requestParams) throws JSONException, ParseException, ServiceException, SessionExpiredException {
        JSONArray dataArray = new JSONArray();
        //common code before executing loop

        String companyID = jsonParamObj.getString(Constants.companyKey);
        int agedReportType = jsonParamObj.optInt("agedreporttype", 0);
        int interval = jsonParamObj.optInt("interval", 30);

        KwlReturnObject accountresult = accAccountDAOobj.getAccountsMappedToCustomerVendor(companyID, (agedReportType == 0 ? true : false));
        DateFormat df = authHandler.getDateOnlyFormat();
        String strStartDate = jsonParamObj.optString(Constants.REQ_startdate, null);
        String strEndDate = jsonParamObj.optString(Constants.REQ_enddate, null);
        Date startDate = null;
        Date endDate = null;
        if (strStartDate != null) {
            startDate = df.parse(strStartDate);
        }
        if (strEndDate != null) {
            endDate = df.parse(strEndDate);
        }

        Calendar tempEndDate = Calendar.getInstance();
        tempEndDate.setTime(startDate);
        Calendar finalEndDate = Calendar.getInstance();
        finalEndDate.setTime(endDate);
        while (tempEndDate.getTimeInMillis() <= finalEndDate.getTimeInMillis()) {//loop to get difference on different date
            //Code to get Aged Total Amount in base
            double agedGrandTotalInBase = 0;
            jsonParamObj.put("curdate", df.format(tempEndDate.getTime()));
            jsonParamObj.put("asofdate", df.format(tempEndDate.getTime()));
            jsonParamObj.put(Constants.REQ_enddate, df.format(tempEndDate.getTime()));

            requestParams.put("curdate", df.format(tempEndDate.getTime()));
            requestParams.put("asofdate", df.format(tempEndDate.getTime()));
            requestParams.put(Constants.REQ_enddate, df.format(tempEndDate.getTime()));

            if (agedReportType == 0) {
                JSONObject agedJSONObj = accInvoiceServiceDAO.getCustomerAgedReceivableMerged(jsonParamObj, false, true);
                JSONArray agedJSOnArray = agedJSONObj.optJSONArray(Constants.data);
                if (agedJSOnArray.length() > 0) {
                    JSONObject totalJSONObj = agedJSOnArray.getJSONObject(agedJSOnArray.length() - 1);//get last JSON
                    agedGrandTotalInBase = authHandler.round(totalJSONObj.optDouble("grandTotalInBase", 0), companyID);
                }
            } else {
                JSONArray agedJSONArray = accGoodsReceiptServiceDAO.getVendorAgedPayableMerged(jsonParamObj, requestParams);
                JSONArray agedGrandTotalArray = StringUtil.getPagedJSONForAgedWIthTotal(agedJSONArray, 0, 30);
                if (agedGrandTotalArray.length() > 0) {
                    JSONObject totalJSONObj = agedGrandTotalArray.getJSONObject(agedGrandTotalArray.length() - 1);//get last JSON
                    agedGrandTotalInBase = authHandler.round(totalJSONObj.optDouble("grandTotalInBase", 0), companyID);
                }
            }
            //Code to get Balance Sheet Total Amount (customer or vendor account)in base
            double totalAccountEndingBalance = 0;

            if (accountresult != null && !accountresult.getEntityList().isEmpty()) {
                List<Account> accountList = accountresult.getEntityList();
                for (Account account : accountList) {
                    double accountBalance = accReportsService.getAccountBalance(jsonParamObj, account.getID(), new Date(1970), tempEndDate.getTime(),null);
                    if (account.getGroup().getNature() == 0) { // amount will be (-)ve if nature of account is Liability. because of to show (+)ve amount to Creditor account in balance sheet, Ledger Report, Trail Balance etc.
                        accountBalance = (-1) * accountBalance;
                    }
                    totalAccountEndingBalance += accountBalance;
                }
            }
            totalAccountEndingBalance = authHandler.round(totalAccountEndingBalance, companyID);

            double difference = authHandler.round(agedGrandTotalInBase - totalAccountEndingBalance, companyID);

            JSONObject obj = new JSONObject();
            obj.put("asondate", df.format(tempEndDate.getTime()));
            obj.put("agingamount", agedGrandTotalInBase);
            obj.put("balancesheetamount", totalAccountEndingBalance);
            obj.put("amountdifference", difference);
            dataArray.put(obj);
            if (tempEndDate.getTimeInMillis() == finalEndDate.getTimeInMillis()) {
                break;
            }
            tempEndDate.add(Calendar.DATE, interval);
            if (tempEndDate.getTimeInMillis() > finalEndDate.getTimeInMillis()) {
                tempEndDate.setTime(endDate);
            }
        }
        return dataArray;
    }
    @Override
    public JSONObject getBudgetVsCostReport(JSONObject jsonParamObj) throws ServiceException {
        JSONObject result = new JSONObject();
        try {
            JSONArray dataArray = new JSONArray();
            String companyID = jsonParamObj.getString(Constants.companyKey);
            String startDate = null;
            String endDate = null;
            FieldParams lldObj = null;
            String fieldlabelline = "";
            boolean isExport = jsonParamObj.has("export")?jsonParamObj.getBoolean("export"):false;
            HashMap<String, Object> requestMap = new HashMap<String, Object>();
            if (!jsonParamObj.has("fieldlabelline") || StringUtil.isNullOrEmpty(jsonParamObj.getString("fieldlabelline"))) {
                result.put(Constants.RES_data, dataArray);
                result.put(Constants.RES_count, 0);
                return result;
            }
            requestMap.put("companyid", companyID);
            //ERP-41557
            int amountDigit = Constants.AMOUNT_DIGIT_AFTER_DECIMAL;
            if (Constants.CompanyPreferencePrecisionMap.containsKey(companyID)) {
                amountDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyID).get(Constants.amountdecimalforcompany);
            }
            
            if (jsonParamObj.has("startDate") && jsonParamObj.getString("startDate") != null && !StringUtil.isNullOrEmpty(jsonParamObj.getString("startDate"))) {
                startDate = jsonParamObj.getString("startDate");
                requestMap.put("startDate", startDate);
            }
            if (jsonParamObj.has("endDate") && jsonParamObj.getString("endDate") != null && !StringUtil.isNullOrEmpty(jsonParamObj.getString("endDate"))) {
                endDate = jsonParamObj.getString("endDate");
                requestMap.put("endDate", endDate);
            }
            if (jsonParamObj.has("fieldlabelline") && jsonParamObj.getString("fieldlabelline") != null && !StringUtil.isNullOrEmpty(jsonParamObj.getString("fieldlabelline"))) {
                fieldlabelline = jsonParamObj.getString("fieldlabelline");
                KwlReturnObject lldkwlObj = accAccountDAOobj.getFieldParams(fieldlabelline, Constants.Acc_Sales_Order_ModuleId, companyID);
                if (lldkwlObj != null && lldkwlObj.getRecordTotalCount() > 0) {
                    lldObj = (FieldParams) lldkwlObj.getEntityList().get(0);
                }

            }
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            filter_names.add("field.id");
            
            String currencySymbol="USD";
            ExtraCompanyPreferences extraPref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            extraPref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            if (extraPref != null) {
                currencySymbol = extraPref.getCompany() != null && extraPref.getCompany().getCurrency() != null ? extraPref.getCompany().getCurrency().getCurrencyCode() : "";
            }
            
//            String start = jsonParamObj.getString("start");
//            String limit = jsonParamObj.getString("limit");

            /*      Fetch Global Dimension 
             *   Master items ids and col number
             */
            if (jsonParamObj.has("globalDimensioncombodataname") && jsonParamObj.getString("globalDimensioncombodataname") != "") {
                requestMap.put("dimension", jsonParamObj.getString("globalDimensioncombodataname"));
                requestMap.put("customfield", "0");// For Dimension field
                List listCombodata = accAccountDAOobj.getFieldComboData(requestMap);
                Iterator itrcombo = listCombodata.iterator();
                JSONObject jobjCombodata = new JSONObject();
                while (itrcombo.hasNext()) {
                    Object[] row = (Object[]) itrcombo.next();
                    if ((short) row[2] == Constants.Acc_Sales_Order_ModuleId) {
                        jobjCombodata.put("Acc_Sales_Order_ModuleId", row[0]);
                        jobjCombodata.put("Acc_Sales_Order_ModuleCol", row[1]);
                    }
                    if ((short) row[2] == Constants.Acc_Purchase_Order_ModuleId) {
                        jobjCombodata.put("Acc_Purchase_Order_ModuleId", row[0]);
                        jobjCombodata.put("Acc_Purchase_Order_ModuleCol", row[1]);
                    }
                    if ((short) row[2] == Constants.Acc_Vendor_Invoice_ModuleId) {
                        jobjCombodata.put("Acc_Vendor_Invoice_ModuleId", row[0]);
                        jobjCombodata.put("Acc_Vendor_Invoice_ModuleCol", row[1]);
                    }
                    if ((short) row[2] == Constants.Acc_Invoice_ModuleId) {
                        jobjCombodata.put("Acc_Invoice_ModuleId", row[0]);
                        jobjCombodata.put("Acc_Invoice_ModuleCol", row[1]);
                    }
                }
                requestMap.put("globalCombodata", jobjCombodata);
            }
            filter_params.add(lldObj.getId());
            filterRequestParams.put("filter_params", filter_params);
//            filterRequestParams.put("filter_start", start);
//            filterRequestParams.put("filter_limit", limit);
            // Line Dimension Master Items
            JSONObject jobjkwl = accAccountDAOobj.getMasterItemsDimension(filterRequestParams);
            KwlReturnObject resultkwl = (KwlReturnObject) jobjkwl.get("kwlReturnObject");
            List list = resultkwl.getEntityList();
            Iterator itr = list.iterator();
            double totalprojectcostsales = 0;
            double totalprojectedfee = 0;
            double totalbudget = 0;
            double totalprojectcostpurchase = 0;
            double totalactualcost = 0;
            double totalbilling = 0;
            double totalearnedfee = 0;
            double spentCommitted = 0;
            while (itr.hasNext()) {

                JSONObject jobj = new JSONObject();
                Object[] row = (Object[]) itr.next();
                FieldComboData fieldComboData = (FieldComboData) row[0];
                jobj.put("dimension", fieldComboData.getValue());
                requestMap.put("dimension", fieldComboData.getValue());
                requestMap.put("linedimensionid", fieldComboData.getId());
                if (fieldComboData.getField() != null) {
                    requestMap.put("fieldid", fieldComboData.getField().getId());
                    requestMap.put("column", fieldComboData.getField().getColnum());
                }

                requestMap.put("mastrComboDataList", (List) row[2]);// List of combo value of dimension which is required for amount calculation in function "getActualVsBudgetReportDetails"

                //Default Values
                double projectcostsales = 0;
                double projectedfee = 0;
                double budget = 0;
                double projectcostpurchase = 0; 
                double actualcost = 0;
                double billing = 0;
                double earnedfee = 0;
                //ERP-41557
                requestMap.put(Constants.amountdecimalforcompany,amountDigit);
                JSONObject jobjBudgetData = accAccountDAOobj.getBudgetVsCostReportDetails(requestMap);
                // Sales Order
                if (jobjBudgetData.has("salesOrderDetails") && jobjBudgetData.get("salesOrderDetails") != null) {
                    JSONArray salesOrderDetailsList = jobjBudgetData.getJSONArray("salesOrderDetails");
                    if (salesOrderDetailsList != null && salesOrderDetailsList.length() > 0) {
                        budget = !salesOrderDetailsList.getJSONArray(0).get(0).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(0) : 0;
                        projectcostsales = !salesOrderDetailsList.getJSONArray(0).get(1).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(1) : 0;
                    }
                }
                // Purchase Order
                if (jobjBudgetData.has("purchaseOrderDetails") && jobjBudgetData.get("purchaseOrderDetails") != null) {
                    JSONArray purchaseOrderDetailsList = jobjBudgetData.getJSONArray("purchaseOrderDetails");
                    if (purchaseOrderDetailsList != null && purchaseOrderDetailsList.length() > 0) {
                        projectcostpurchase = !purchaseOrderDetailsList.get(0).equals(null) ? purchaseOrderDetailsList.getDouble(0) : 0;
                    }
                }
                // Purchase Order - Expense
                if (jobjBudgetData.has("expensePODetails") && jobjBudgetData.get("expensePODetails") != null) {
                    JSONArray expensePODetailsList = jobjBudgetData.getJSONArray("expensePODetails");
                    if (expensePODetailsList != null && expensePODetailsList.length() > 0) {
                        projectcostpurchase += !expensePODetailsList.get(0).equals(null) ? expensePODetailsList.getDouble(0) : 0;
                    }
                }
                // Goods Receipt 
                if (jobjBudgetData.has("goodsReceiptDetails") && jobjBudgetData.get("goodsReceiptDetails") != null) {
                    JSONArray goodsReceiptDetailsList = jobjBudgetData.getJSONArray("goodsReceiptDetails");
                    if (goodsReceiptDetailsList != null && goodsReceiptDetailsList.length() > 0) {
                        actualcost = !goodsReceiptDetailsList.get(0).equals(null) ? goodsReceiptDetailsList.getDouble(0) : 0;
                    }
                }
                if (jobjBudgetData.has("goodsReceiptExpenseDetails") && jobjBudgetData.get("goodsReceiptExpenseDetails") != null) {
                    JSONArray goodsReceiptExpenseDetailsList = jobjBudgetData.getJSONArray("goodsReceiptExpenseDetails");
                    if (goodsReceiptExpenseDetailsList != null && goodsReceiptExpenseDetailsList.length() > 0) {
                        actualcost += !goodsReceiptExpenseDetailsList.get(0).equals(null) ? goodsReceiptExpenseDetailsList.getDouble(0) : 0;
                    }
                }
                if (jobjBudgetData.has("goodsReceiptDetailsWithoutDate") && jobjBudgetData.get("goodsReceiptDetailsWithoutDate") != null) {
                    JSONArray goodsReceiptDetailsList = jobjBudgetData.getJSONArray("goodsReceiptDetailsWithoutDate");
                    if (goodsReceiptDetailsList != null && goodsReceiptDetailsList.length() > 0) {
                        spentCommitted = (!goodsReceiptDetailsList.get(0).equals(null) ? goodsReceiptDetailsList.getDouble(0) : 0);
                    }
                }
                // Goods Receipt Expense Grid without date filter
                if (jobjBudgetData.has("goodsReceiptExpenseDetailsWithOutdateFilter") && jobjBudgetData.get("goodsReceiptExpenseDetailsWithOutdateFilter") != null) {
                    JSONArray goodsReceiptExpenseDetailsList = jobjBudgetData.getJSONArray("goodsReceiptExpenseDetailsWithOutdateFilter");
                    if (goodsReceiptExpenseDetailsList != null && goodsReceiptExpenseDetailsList.length() > 0) {
                        spentCommitted += (!goodsReceiptExpenseDetailsList.get(0).equals(null) ? goodsReceiptExpenseDetailsList.getDouble(0) : 0);
                    }
                }
                if (jobjBudgetData.has("purchaseOrderDetailsSpentCommitted") && jobjBudgetData.get("purchaseOrderDetailsSpentCommitted") != null) {
                    JSONArray purchaseOrderDetailsSpentCommitted = jobjBudgetData.getJSONArray("purchaseOrderDetailsSpentCommitted");
                    if (purchaseOrderDetailsSpentCommitted != null && purchaseOrderDetailsSpentCommitted.length() > 0) {
                        spentCommitted += (!purchaseOrderDetailsSpentCommitted.get(0).equals(null) ? purchaseOrderDetailsSpentCommitted.getDouble(0) : 0);
                    }
                }
                // Goods Receipt Expense Grid without date filter
                if (jobjBudgetData.has("expensePODetailsSpentCommitted") && jobjBudgetData.get("expensePODetailsSpentCommitted") != null) {
                    JSONArray expensePODetailsSpentCommitted = jobjBudgetData.getJSONArray("expensePODetailsSpentCommitted");
                    if (expensePODetailsSpentCommitted != null && expensePODetailsSpentCommitted.length() > 0) {
                        spentCommitted += (!expensePODetailsSpentCommitted.get(0).equals(null) ? expensePODetailsSpentCommitted.getDouble(0) : 0);
                    }
                }
                // Invoice Receipt
                if (jobjBudgetData.has("invoiceDetails") && jobjBudgetData.get("invoiceDetails") != null) {
                    JSONArray invoiceDetailsList = jobjBudgetData.getJSONArray("invoiceDetails");
                    if (invoiceDetailsList != null && invoiceDetailsList.length() > 0) {
                        billing = !invoiceDetailsList.get(0).equals(null) ? invoiceDetailsList.getDouble(0) : 0;
                    }
                }
                if (projectcostsales >= spentCommitted) {
                    projectedfee = budget - (projectcostsales);
                } else {
                    projectedfee = budget - (spentCommitted);
                }
//                earnedfee = billing - actualcost;// Formula changed please check ERP-41067
                if (budget != 0) {
                    earnedfee = billing / budget * projectedfee;
                }
                if (budget == 0 && projectcostsales == 0 && projectcostpurchase == 0 && actualcost == 0 && billing == 0 && projectedfee == 0 && earnedfee == 0) {
                        continue;
                }
                // Put data 
                if (!isExport) {
                    jobj.put("budget", budget);
                    jobj.put("projectcostsales", projectcostsales);
                    jobj.put("projectcostpurchase", projectcostpurchase);
                    jobj.put("actualcost", actualcost);
                    jobj.put("billing", billing);
                    jobj.put("projectedfee", projectedfee);
                    jobj.put("earnedfee", earnedfee);
                    jobj.put("currencySymbol", currencySymbol);
                } else {
                    jobj.put("budget", authHandler.round(budget, companyID));
                    jobj.put("projectcostsales", authHandler.round(projectcostsales, companyID));
                    jobj.put("projectcostpurchase", authHandler.round(projectcostpurchase, companyID));
                    jobj.put("actualcost", authHandler.round(actualcost, companyID));
                    jobj.put("billing", authHandler.round(billing, companyID));
                    jobj.put("projectedfee", authHandler.round(projectedfee, companyID));
                    jobj.put("earnedfee", authHandler.round(earnedfee, companyID));
                    jobj.put("currencySymbol", currencySymbol);
                }
                
                totalbudget+= budget; 
                totalprojectcostsales +=projectcostsales;
                totalprojectcostpurchase +=projectcostpurchase;
                totalactualcost +=actualcost;
                totalbilling +=billing;
                totalprojectedfee += projectedfee;
                totalearnedfee +=earnedfee;
                
                dataArray.put(jobj);
            }
            if (!isExport) {
                JSONObject jobj = new JSONObject();
                jobj.put("dimension", "Total");
                jobj.put("budget", totalbudget);
                jobj.put("projectcostsales", totalprojectcostsales);
                jobj.put("projectcostpurchase", totalprojectcostpurchase);
                jobj.put("actualcost", totalactualcost);
                jobj.put("billing", totalbilling);
                jobj.put("projectedfee", totalprojectedfee);
                jobj.put("earnedfee", totalearnedfee);
                jobj.put("currencySymbol", totalearnedfee);
                dataArray.put(jobj);
            }
            
            result.put(Constants.RES_data, dataArray);
            if (jobjkwl.has("count") && jobjkwl.get("count") != null) {
                result.put(Constants.RES_count, jobjkwl.get("count"));
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    @Override
    public JSONObject getActualVsBudgetReport(JSONObject jsonParamObj) throws ServiceException {
        JSONObject result = new JSONObject();
        try {
            JSONArray dataArray = new JSONArray();
            String companyID = jsonParamObj.getString(Constants.companyKey);
            String startDate = null;
            String endDate = null;
            String cogaAccount = "";
            FieldParams lldObj = null;
            String fieldlabelline = "";
            //ERP-41557
            int amountDigit = Constants.AMOUNT_DIGIT_AFTER_DECIMAL;
            if (Constants.CompanyPreferencePrecisionMap.containsKey(companyID)) {
                amountDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyID).get(Constants.amountdecimalforcompany);
            }
            if (!jsonParamObj.has("fieldlabelline") || StringUtil.isNullOrEmpty(jsonParamObj.getString("fieldlabelline"))) {
                result.put(Constants.RES_data, dataArray);
                result.put(Constants.RES_count, 0);
                return result;
            }
            HashMap<String, Object> requestGlobalMap = new HashMap<String, Object>();
            HashMap<String, Object> requestLineMap = new HashMap<String, Object>();
            requestLineMap.put("companyid", companyID);
            if (jsonParamObj.has("startDate") && jsonParamObj.getString("startDate") != null && !StringUtil.isNullOrEmpty(jsonParamObj.getString("startDate"))) {
                startDate = jsonParamObj.getString("startDate");
                requestLineMap.put("startDate", startDate);
            }
            if (jsonParamObj.has("endDate") && jsonParamObj.getString("endDate") != null && !StringUtil.isNullOrEmpty(jsonParamObj.getString("endDate"))) {
                endDate = jsonParamObj.getString("endDate");
                requestLineMap.put("endDate", endDate);
            }
            if (jsonParamObj.has("cogaAccount") && jsonParamObj.getString("cogaAccount") != null && !StringUtil.isNullOrEmpty(jsonParamObj.getString("cogaAccount"))) {
                cogaAccount = jsonParamObj.getString("cogaAccount");
            }
            if (jsonParamObj.has("fieldlabelline") && jsonParamObj.getString("fieldlabelline") != null && !StringUtil.isNullOrEmpty(jsonParamObj.getString("fieldlabelline"))) {
                fieldlabelline = jsonParamObj.getString("fieldlabelline");
                KwlReturnObject lldkwlObj = accAccountDAOobj.getFieldParams(fieldlabelline, Constants.Acc_Sales_Order_ModuleId, companyID);
                if (lldkwlObj != null && lldkwlObj.getRecordTotalCount() > 0) {
                    lldObj = (FieldParams) lldkwlObj.getEntityList().get(0);
                }

            }
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            filter_names.add("field.id");

//            String start = jsonParamObj.getString("start");
//            String limit = jsonParamObj.getString("limit");

            /*      Fetch Global Dimension 
             *   Master items ids and col number
             */
            if (jsonParamObj.has("globalDimensioncombodataname") && jsonParamObj.getString("globalDimensioncombodataname") != "") {
                requestGlobalMap.put("dimension", jsonParamObj.getString("globalDimensioncombodataname"));
                requestGlobalMap.put("customfield", "0");// For Dimension field
                List listCombodata = accAccountDAOobj.getFieldComboData(requestGlobalMap);
                requestGlobalMap.clear();
                Iterator itrcombo = listCombodata.iterator();
                JSONObject jobjCombodata = new JSONObject();
                while (itrcombo.hasNext()) {
                    Object[] row = (Object[]) itrcombo.next();
                    if ((short) row[2] == Constants.Acc_Sales_Order_ModuleId) {
                        jobjCombodata.put("Acc_Sales_Order_ModuleId", row[0]);
                        jobjCombodata.put("Acc_Sales_Order_ModuleCol", row[1]);
                    }
                    if ((short) row[2] == Constants.Acc_Purchase_Order_ModuleId) {
                        jobjCombodata.put("Acc_Purchase_Order_ModuleId", row[0]);
                        jobjCombodata.put("Acc_Purchase_Order_ModuleCol", row[1]);
                    }
                    if ((short) row[2] == Constants.Acc_Vendor_Invoice_ModuleId) {
                        jobjCombodata.put("Acc_Vendor_Invoice_ModuleId", row[0]);
                        jobjCombodata.put("Acc_Vendor_Invoice_ModuleCol", row[1]);
                    }
                    if ((short) row[2] == Constants.Acc_Invoice_ModuleId) {
                        jobjCombodata.put("Acc_Invoice_ModuleId", row[0]);
                        jobjCombodata.put("Acc_Invoice_ModuleCol", row[1]);
                    }
                    if ((short) row[2] == Constants.Acc_Customer_Quotation_ModuleId) {
                        jobjCombodata.put("Acc_Customer_Quotation_ModuleId", row[0]);
                        jobjCombodata.put("Acc_Customer_Quotation_ModuleCol", row[1]);
                    }
                }
                requestLineMap.put("globalCombodata", jobjCombodata);
            }

            /*      Fetch Global Custom Field - "Change Order"(Hardcoded value as per sample report)
             *   Master items ids and col number
             */
            requestGlobalMap.put("dimension", "Yes");// Custom field Value
            requestGlobalMap.put("customfieldName", "Change Order"); //Custom field Lebel
            requestGlobalMap.put("customfield", "1");// For Custom field
            List listCombodata = accAccountDAOobj.getFieldComboData(requestGlobalMap);
            requestGlobalMap.clear();
            Iterator itrcombo = listCombodata.iterator();
            JSONObject jobjCombodata = new JSONObject();
            while (itrcombo.hasNext()) {
                Object[] row = (Object[]) itrcombo.next();
                if ((short) row[2] == Constants.Acc_Sales_Order_ModuleId) {
                    jobjCombodata.put("Sales_Order_customfieldId", row[0]);
                    jobjCombodata.put("Sales_Order_customfieldCol", row[1]);
                }
                if ((short) row[2] == Constants.Acc_Purchase_Order_ModuleId) {
                    jobjCombodata.put("Purchase_Order_customfieldId", row[0]);
                    jobjCombodata.put("Purchase_Order_customfieldCol", row[1]);
                }
                if ((short) row[2] == Constants.Acc_Vendor_Invoice_ModuleId) {
                    jobjCombodata.put("Vendor_Invoice_customfieldId", row[0]);
                    jobjCombodata.put("Vendor_Invoice_customfieldCol", row[1]);
                }
                if ((short) row[2] == Constants.Acc_Customer_Quotation_ModuleId) {
                    jobjCombodata.put("Customer_Quotation_customfieldId", row[0]);
                    jobjCombodata.put("Customer_Quotation_customfieldCol", row[1]);
                }
            }
            requestLineMap.put("globalCustomField", jobjCombodata);
            filter_params.add(lldObj.getId());
            filterRequestParams.put("filter_params", filter_params);
            //Accounts For grouping
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
//            ArrayList<String> jobjcount = new ArrayList<String>();

            requestParams.put("mode", 2);
            requestParams.put("ignorecustomers", "true");
            requestParams.put("ignorevendors", "true");
            requestParams.put("nondeleted", "true");
            requestParams.put("controlAccounts", true);
            requestParams.put("companyid", companyID);
            requestParams.put("templateid", null);
            requestParams.put("ignorePaging", false);
            String currencySymbol = "USD";
            ExtraCompanyPreferences extraPref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            extraPref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            if (extraPref != null) {
                requestParams.put("accountsortingflag", extraPref.isAccountSortingFlag());
                currencySymbol = extraPref.getCompany()!=null && extraPref.getCompany().getCurrency()!=null?extraPref.getCompany().getCurrency().getCurrencyCode():"";
            }
            if (!StringUtil.isNullOrEmpty(cogaAccount) && !cogaAccount.equals("1")) {
                requestParams.put("cogaid", cogaAccount);
                requestParams.put("isFromReport", true);
            }
            KwlReturnObject accounts = accAccountDAOobj.getAccountsForCombo(requestParams);
            List ls = accounts.getEntityList();
            JSONObject jobjkwl = accAccountDAOobj.getMasterItemsDimension(filterRequestParams);
            KwlReturnObject resultkwl = (KwlReturnObject) jobjkwl.get("kwlReturnObject");
            List list = accAccountDAOobj.isAccountsUsed(accounts.getEntityList(), companyID); // For optimization - Only those account which are in use of po,so,pi q
            Iterator<Object[]> itracc = list.iterator();
            List listMaster = resultkwl.getEntityList();
            int count = 0;
            while (itracc.hasNext()) {// Account Loop - Requirement to show grouping of Account
                Object[] rowacc = (Object[]) itracc.next();
                requestLineMap.put("cogaAccount", rowacc[0].toString());
                
                double currentcostbudgetGroupTotal = 0;
                double pendingquotescostGroupTotal = 0;
                double pendingquotessalespriceGroupTotal = 0;
                double budgetGroupTotal = 0;
                double projectedrevenuebudgetGroupTotal = 0;
                double purchaseorderamountGroupTotal = 0;
                double spentCommittedGroupTotal = 0;
                double projectedcostbudgetGroupTotal = 0;
                double spentperiodGroupTotal = 0;
                double marginGroupTotal = 0;
                
                // Line Dimension Master Items
                Iterator itr = listMaster.iterator();
                while (itr.hasNext()) {

                    JSONObject jobj = new JSONObject();
                    Object[] row = (Object[]) itr.next();
                    FieldComboData fieldComboData = (FieldComboData) row[0];
                    jobj.put("dimension", fieldComboData.getValue());
                    requestLineMap.put("dimension", fieldComboData.getValue());
                    requestLineMap.put("linedimensionid", fieldComboData.getId());
                    if (fieldComboData.getField() != null) {
                        requestLineMap.put("fieldid", fieldComboData.getField().getId());
                        requestLineMap.put("column", fieldComboData.getField().getColnum());
                    }

                    requestLineMap.put("mastrComboDataList", (List) row[2]);// List of combo value of dimension which is required for amount calculation in function "getActualVsBudgetReportDetails"

                    //Default Values
                    double currentcostbudget = 0;
                    double pendingquotescost = 0;
                    double pendingquotessalesprice = 0;
                    double budget = 0;
                    double projectedrevenuebudget = 0;
                    double purchaseorderamount = 0;
//                    double purchaseorderamountDateFilter = 0;
                    double spentCommitted = 0;
                    double projectedcostbudget = 0;
                    double spentperiod = 0;
                    double margin = 0;
                    //ERP-41557
                    requestLineMap.put(Constants.amountdecimalforcompany,amountDigit);
                    JSONObject jobjBudgetData = accAccountDAOobj.getActualVsBudgetReportDetails(requestLineMap);

                    // Sales Order total cost
                    if (jobjBudgetData.has("salesOrderDetails") && jobjBudgetData.get("salesOrderDetails") != null) {
                        JSONArray salesOrderDetailsList = jobjBudgetData.getJSONArray("salesOrderDetails");
                        if (salesOrderDetailsList != null && salesOrderDetailsList.length() > 0) {
                            budget = !salesOrderDetailsList.getJSONArray(0).get(0).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(0) : 0;
                            currentcostbudget = !salesOrderDetailsList.getJSONArray(0).get(1).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(1) : 0;
                        }
                    }
                    // Sales Order Approved billing amount
                    if (jobjBudgetData.has("salesOrderDetailsApproved") && jobjBudgetData.get("salesOrderDetailsApproved") != null) {
                        JSONArray salesOrderDetailsList = jobjBudgetData.getJSONArray("salesOrderDetailsApproved");
                        if (salesOrderDetailsList != null && salesOrderDetailsList.length() > 0) {
                            projectedrevenuebudget = !salesOrderDetailsList.getJSONArray(0).get(0).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(0) : 0;
                        }
                    }
                    // Purchase Order Product grid without date filter
                    if (jobjBudgetData.has("purchaseOrderDetails") && jobjBudgetData.get("purchaseOrderDetails") != null) {
                        JSONArray purchaseOrderDetailsList = jobjBudgetData.getJSONArray("purchaseOrderDetails");
                        if (purchaseOrderDetailsList != null && purchaseOrderDetailsList.length() > 0) {
                            purchaseorderamount = !purchaseOrderDetailsList.get(0).equals(null) ? purchaseOrderDetailsList.getDouble(0) : 0;
                        }
                    }
                    // Purchase Order Expense grid without date filter
                    if (jobjBudgetData.has("expensePODetails") && jobjBudgetData.get("expensePODetails") != null) {
                        JSONArray expensePODetails = jobjBudgetData.getJSONArray("expensePODetails");
                        if (expensePODetails != null && expensePODetails.length() > 0) {
                            purchaseorderamount += !expensePODetails.get(0).equals(null) ? expensePODetails.getDouble(0) : 0;
                        }
                    }
                    // Purchase Order Product grid with date filter
//                    if (jobjBudgetData.has("purchaseOrderDetails_DateFilter") && jobjBudgetData.get("purchaseOrderDetails_DateFilter") != null) {
//                        JSONArray purchaseOrderDetails_DateFilter = jobjBudgetData.getJSONArray("purchaseOrderDetails_DateFilter");
//                        if (purchaseOrderDetails_DateFilter != null && purchaseOrderDetails_DateFilter.length() > 0) {
//                            purchaseorderamountDateFilter = !purchaseOrderDetails_DateFilter.get(0).equals(null) ? purchaseOrderDetails_DateFilter.getDouble(0) : 0;
//                        }
//                    }
//                    // Purchase Order Expense grid with date filter
//                    if (jobjBudgetData.has("expensePODetails_DateFilter") && jobjBudgetData.get("expensePODetails_DateFilter") != null) {
//                        JSONArray expensePODetails_DateFilter = jobjBudgetData.getJSONArray("expensePODetails_DateFilter");
//                        if (expensePODetails_DateFilter != null && expensePODetails_DateFilter.length() > 0) {
//                            purchaseorderamountDateFilter += !expensePODetails_DateFilter.get(0).equals(null) ? expensePODetails_DateFilter.getDouble(0) : 0;
//                        }
//                    }
                    
                    // Goods Receipt Product Grid with date filter
                    if (jobjBudgetData.has("goodsReceiptDetailsDateFilter") && jobjBudgetData.get("goodsReceiptDetailsDateFilter") != null) {
                        JSONArray goodsReceiptDetailsList = jobjBudgetData.getJSONArray("goodsReceiptDetailsDateFilter");
                        if (goodsReceiptDetailsList != null && goodsReceiptDetailsList.length() > 0) {
                            spentperiod = !goodsReceiptDetailsList.get(0).equals(null) ? goodsReceiptDetailsList.getDouble(0) : 0;
                        }
                    }
                    // Goods Receipt Expense Grid with date filter
                    if (jobjBudgetData.has("goodsReceiptExpenseDetailsDateFilter") && jobjBudgetData.get("goodsReceiptExpenseDetailsDateFilter") != null) {
                        JSONArray goodsReceiptExpenseDetailsDateFilterList = jobjBudgetData.getJSONArray("goodsReceiptExpenseDetailsDateFilter");
                        if (goodsReceiptExpenseDetailsDateFilterList != null && goodsReceiptExpenseDetailsDateFilterList.length() > 0) {
                            spentperiod += !goodsReceiptExpenseDetailsDateFilterList.get(0).equals(null) ? goodsReceiptExpenseDetailsDateFilterList.getDouble(0) : 0;
                        }
                    }

                    // Goods Receipt Product Grid without date filter
                    if (jobjBudgetData.has("goodsReceiptDetails") && jobjBudgetData.get("goodsReceiptDetails") != null) {
                        JSONArray goodsReceiptDetailsList = jobjBudgetData.getJSONArray("goodsReceiptDetails");
                        if (goodsReceiptDetailsList != null && goodsReceiptDetailsList.length() > 0) {
                            spentCommitted = (!goodsReceiptDetailsList.get(0).equals(null) ? goodsReceiptDetailsList.getDouble(0) : 0);
                        }
                    }
                    // Goods Receipt Expense Grid without date filter
                    if (jobjBudgetData.has("goodsReceiptExpenseDetails") && jobjBudgetData.get("goodsReceiptExpenseDetails") != null) {
                        JSONArray goodsReceiptExpenseDetailsList = jobjBudgetData.getJSONArray("goodsReceiptExpenseDetails");
                        if (goodsReceiptExpenseDetailsList != null && goodsReceiptExpenseDetailsList.length() > 0) {
                            spentCommitted += (!goodsReceiptExpenseDetailsList.get(0).equals(null) ? goodsReceiptExpenseDetailsList.getDouble(0) : 0);
                        }
                    }
                    spentCommitted = spentCommitted + purchaseorderamount;
                    // Customer Quotation
                    if (jobjBudgetData.has("quotationDetails") && jobjBudgetData.get("quotationDetails") != null) {
                        JSONArray quotationDetailsList = jobjBudgetData.getJSONArray("quotationDetails");
                        if (quotationDetailsList != null && quotationDetailsList.length() > 0) {
                            pendingquotessalesprice = !quotationDetailsList.getJSONArray(0).get(0).equals(null) ? quotationDetailsList.getJSONArray(0).getDouble(0) : 0;
                            pendingquotescost = !quotationDetailsList.getJSONArray(0).get(1).equals(null) ? quotationDetailsList.getJSONArray(0).getDouble(1) : 0;
                        }
                    }

                    //Other calculation as per report
                    projectedcostbudget = currentcostbudget + pendingquotescost;

                    if (currentcostbudget >= spentCommitted) {
                        margin = budget - (currentcostbudget);
                    } else {
                        margin = budget - (spentCommitted);
                    }

                    projectedrevenuebudget += pendingquotessalesprice;

                    /*
                     If all amount is zero, need not push to the return object - its a part of requirement
                     */
                    if (currentcostbudget == 0 && spentperiod == 0 && spentCommitted == 0 && pendingquotescost == 0 && projectedcostbudget == 0 && pendingquotessalesprice == 0 && projectedrevenuebudget == 0) {
                        continue;
                    }
                    
                    // Put data 
                    jobj.put("currentcostbudget", currentcostbudget);
                    jobj.put("spentperiod", spentperiod);
                    jobj.put("spentcommitted", spentCommitted);
                    jobj.put("pendingquotescost", pendingquotescost);
                    jobj.put("projectedcostbudget", projectedcostbudget);
                    jobj.put("pendingquotessalesprice", pendingquotessalesprice);
                    jobj.put("projectedrevenuebudget", projectedrevenuebudget);
                    jobj.put("margin", margin);
                    jobj.put("account", rowacc[1].toString());
                    jobj.put("currencySymbol", currencySymbol);
                    dataArray.put(jobj);
                    
                    // For Group Total
                    if (jsonParamObj.has("isfromExport") && jsonParamObj.getBoolean("isfromExport")) {
                        currentcostbudgetGroupTotal += currentcostbudget;
                        pendingquotescostGroupTotal += pendingquotescost;
                        pendingquotessalespriceGroupTotal += pendingquotessalesprice;
                        projectedrevenuebudgetGroupTotal += projectedrevenuebudget;
                        purchaseorderamountGroupTotal += purchaseorderamount;
                        spentCommittedGroupTotal += spentCommitted;
                        projectedcostbudgetGroupTotal += projectedcostbudget;
                        spentperiodGroupTotal += spentperiod;
                        marginGroupTotal += margin;
                    }
//                    if (!jobjcount.contains(rowacc[1].toString())) {
//                        jobjcount.add(rowacc[1].toString());
//                        count++;
//                    }
                }
                if (jsonParamObj.has("isfromExport") && jsonParamObj.getBoolean("isfromExport")) {
                    if (currentcostbudgetGroupTotal != 0 || spentCommittedGroupTotal!= 0 || spentperiodGroupTotal != 0 || pendingquotescostGroupTotal != 0 || projectedcostbudgetGroupTotal != 0 || pendingquotessalespriceGroupTotal != 0 || projectedrevenuebudgetGroupTotal != 0 || marginGroupTotal != 0) {
                        JSONObject jobjHead = new JSONObject();
                        jobjHead.put("dimension", "Total :");
                        jobjHead.put("currentcostbudget", currentcostbudgetGroupTotal);
                        jobjHead.put("spentperiod", spentperiodGroupTotal);
                        jobjHead.put("spentcommitted", spentCommittedGroupTotal);
                        jobjHead.put("pendingquotescost", pendingquotescostGroupTotal);
                        jobjHead.put("projectedcostbudget", projectedcostbudgetGroupTotal);
                        jobjHead.put("pendingquotessalesprice", pendingquotessalespriceGroupTotal);
                        jobjHead.put("projectedrevenuebudget", projectedrevenuebudgetGroupTotal);
                        jobjHead.put("margin", marginGroupTotal);
                        jobjHead.put("currencySymbol", currencySymbol);
                        dataArray.put(jobjHead);
                        jobjHead = new JSONObject();
                        jobjHead.put("dimension", " ");
                        jobjHead.put("currentcostbudget", " ");
                        jobjHead.put("spentperiod", " ");
                        jobjHead.put("spentcommitted", " ");
                        jobjHead.put("pendingquotescost", " ");
                        jobjHead.put("projectedcostbudget", " ");
                        jobjHead.put("pendingquotessalesprice", " ");
                        jobjHead.put("projectedrevenuebudget", " ");
                        jobjHead.put("margin", " ");
                        jobjHead.put("currencySymbol", " ");
                        dataArray.put(jobjHead);
                    }
                }                
//                if (count == Integer.parseInt(limit)) {// page limit count of account group per page
////                if (count == 1) {// page limit count of account group per page
//                    break;
//                }
            }
            result.put(Constants.RES_data, dataArray);
            result.put(Constants.RES_count, count);
        } catch (JSONException ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    @Override
    public JSONObject getForecastingReport(JSONObject jsonParamObj) throws ServiceException {
        JSONObject response = new JSONObject();
        JSONObject result = new JSONObject();
        try {
            JSONArray dataArray = new JSONArray();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String companyID = jsonParamObj.getString(Constants.companyKey);
            String startDate = null;
            String endDate = null;
            //**Filter data not found - return empty JSON**/
            if (!jsonParamObj.has("globalDimensioncombodataname") || StringUtil.isNullOrEmpty(jsonParamObj.getString("globalDimensioncombodataname"))) {
                result.put(Constants.RES_data, dataArray);
                result.put(Constants.RES_count, 0);
                return result;
            }
            //ERP-41557
            int amountDigit = Constants.AMOUNT_DIGIT_AFTER_DECIMAL;
            if (Constants.CompanyPreferencePrecisionMap.containsKey(companyID)) {
                amountDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyID).get(Constants.amountdecimalforcompany);
            }
            HashMap<String, Object> requestGlobalMap = new HashMap<String, Object>();
            HashMap<String, Object> requestLineMap = new HashMap<String, Object>();
            requestLineMap.put("companyid", companyID);
            if (jsonParamObj.has("startDate") && jsonParamObj.getString("startDate") != null && !StringUtil.isNullOrEmpty(jsonParamObj.getString("startDate"))) {
                startDate = jsonParamObj.getString("startDate");
                requestLineMap.put("startDate", startDate);
            }
            if (jsonParamObj.has("endDate") && jsonParamObj.getString("endDate") != null && !StringUtil.isNullOrEmpty(jsonParamObj.getString("endDate"))) {
                endDate = jsonParamObj.getString("endDate");
                requestLineMap.put("endDate", endDate);
            }
            Date currentMonthStartDate = null;
            Date currentMonthEndDate = null;
            Date lastMonthStartDate = null;
            Date lastMonthEndDate = null;

            Calendar aCalendar = Calendar.getInstance();
            aCalendar.set(Calendar.DATE, 1);
            currentMonthStartDate = aCalendar.getTime();
            int enddate = aCalendar.getActualMaximum(Calendar.DATE);
            aCalendar.set(Calendar.DATE, enddate);
            currentMonthEndDate = aCalendar.getTime();// Current month End Date

            aCalendar.setTime(currentMonthStartDate);// Current month Start Date
            aCalendar.add(Calendar.DATE, -1);
            lastMonthEndDate = aCalendar.getTime();// Previous month End Date
            aCalendar.set(Calendar.DATE, 1);
            lastMonthStartDate = aCalendar.getTime();
            requestLineMap.put("currentMonthStartDate", sdf.format(currentMonthStartDate));
            requestLineMap.put("currentMonthEndDate", sdf.format(currentMonthEndDate));
            requestLineMap.put("lastMonthStartDate", sdf.format(lastMonthStartDate));
            requestLineMap.put("lastMonthEndDate", sdf.format(lastMonthEndDate));
            
            aCalendar = Calendar.getInstance();
            requestLineMap.put("currentdate", sdf.format(aCalendar.getTime()));// Today date
            aCalendar.add(Calendar.DATE, -29);
            requestLineMap.put("30days", sdf.format(aCalendar.getTime()));// Date of Today date - 30 days
            aCalendar.add(Calendar.DATE, -1);
            requestLineMap.put("31days", sdf.format(aCalendar.getTime()));// Date of Today date - 31 days
            aCalendar.add(Calendar.DATE, -29);
            requestLineMap.put("60days", sdf.format(aCalendar.getTime()));// Date of Today date - 60 days
            aCalendar.add(Calendar.DATE, -1);
            requestLineMap.put("61days", sdf.format(aCalendar.getTime()));// Date of Today date - 61 days
            aCalendar.add(Calendar.DATE, -29);
            requestLineMap.put("90days", sdf.format(aCalendar.getTime()));// Date of Today date - 90 days
            
            aCalendar = Calendar.getInstance();
            aCalendar.set(Calendar.DATE, 1);
            aCalendar.set(Calendar.MONTH, 0);
            requestLineMap.put("firstdateofyear", sdf.format(aCalendar.getTime())); // First Date of current year
            /*      Fetch Global Dimension 
             *   Master items ids and col number
             */
            requestGlobalMap.put("dimension", jsonParamObj.getString("globalDimensioncombodataname"));
            requestGlobalMap.put("customfield", "0");// For Dimension field
            List listCombodata = accAccountDAOobj.getFieldComboData(requestGlobalMap);
            requestGlobalMap.clear();
            Iterator itrcombo = listCombodata.iterator();
            JSONObject jobjCombodata = new JSONObject();
            while (itrcombo.hasNext()) {
                Object[] row = (Object[]) itrcombo.next();
                if ((short) row[2] == Constants.Acc_Sales_Order_ModuleId) {
                    jobjCombodata.put("Acc_Sales_Order_ModuleId", row[0]);
                    jobjCombodata.put("Acc_Sales_Order_ModuleCol", row[1]);
                }
                if ((short) row[2] == Constants.Acc_Purchase_Order_ModuleId) {
                    jobjCombodata.put("Acc_Purchase_Order_ModuleId", row[0]);
                    jobjCombodata.put("Acc_Purchase_Order_ModuleCol", row[1]);
                }
                if ((short) row[2] == Constants.Acc_Vendor_Invoice_ModuleId) {
                    jobjCombodata.put("Acc_Vendor_Invoice_ModuleId", row[0]);
                    jobjCombodata.put("Acc_Vendor_Invoice_ModuleCol", row[1]);
                }
                if ((short) row[2] == Constants.Acc_Invoice_ModuleId) {
                    jobjCombodata.put("Acc_Invoice_ModuleId", row[0]);
                    jobjCombodata.put("Acc_Invoice_ModuleCol", row[1]);
                }
                if ((short) row[2] == Constants.Acc_Customer_Quotation_ModuleId) {
                    jobjCombodata.put("Acc_Customer_Quotation_ModuleId", row[0]);
                    jobjCombodata.put("Acc_Customer_Quotation_ModuleCol", row[1]);
                }
            }
            requestLineMap.put("globalCombodata", jobjCombodata);

            //Change order custom field data 
            requestGlobalMap.put("dimension", "Yes");// Custom field Value
            requestGlobalMap.put("customfieldName", "Change Order"); //Custom field Lebel
            requestGlobalMap.put("customfield", "1");// For Custom field
            List changeorderCombodata = accAccountDAOobj.getFieldComboData(requestGlobalMap);
            requestGlobalMap.clear();
            Iterator itrchangeordercombo = changeorderCombodata.iterator();
            JSONObject jobjchangeorderCombodata = new JSONObject();
            while (itrchangeordercombo.hasNext()) {
                Object[] row = (Object[]) itrchangeordercombo.next();
                if ((short) row[2] == Constants.Acc_Sales_Order_ModuleId) {
                    jobjchangeorderCombodata.put("Sales_Order_customfieldId", row[0]);
                    jobjchangeorderCombodata.put("Sales_Order_customfieldCol", row[1]);
                }
                if ((short) row[2] == Constants.Acc_Purchase_Order_ModuleId) {
                    jobjchangeorderCombodata.put("Purchase_Order_customfieldId", row[0]);
                    jobjchangeorderCombodata.put("Purchase_Order_customfieldCol", row[1]);
                }
                if ((short) row[2] == Constants.Acc_Vendor_Invoice_ModuleId) {
                    jobjchangeorderCombodata.put("Vendor_Invoice_customfieldId", row[0]);
                    jobjchangeorderCombodata.put("Vendor_Invoice_customfieldCol", row[1]);
                }
                if ((short) row[2] == Constants.Acc_Customer_Quotation_ModuleId) {
                    jobjchangeorderCombodata.put("Customer_Quotation_customfieldId", row[0]);
                    jobjchangeorderCombodata.put("Customer_Quotation_customfieldCol", row[1]);
                }
            }
            requestLineMap.put("globalCustomField", jobjchangeorderCombodata);
            
            //Accounts For grouping
            HashMap<String, Object> requestParams = new HashMap<String, Object>();

            requestParams.put("mode", 2);
            requestParams.put("ignorecustomers", "true");
            requestParams.put("ignorevendors", "true");
            requestParams.put("nondeleted", "true");
            requestParams.put("controlAccounts", true);
            requestParams.put("companyid", companyID);
            requestParams.put("templateid", null);
            requestParams.put("ignorePaging", false);
            KwlReturnObject accounts = accAccountDAOobj.getAccountsForCombo(requestParams);
            List list = accAccountDAOobj.isAccountsUsed(accounts.getEntityList(), companyID); // For optimization - Only those account Which are used in Transaction 
            Iterator<Object[]> itracc = list.iterator();
            int count = 0;
            double spentperiod = 0.0;
            double spentCommitted = 0.0;
            double purchaseorderamount = 0.0;
//            double purchaseorderForCostForCast = 0.0;
            double poAmountCurrentMonth = 0.0;
            double poAmountLastMonth = 0.0;
            double soAmountCurrentMonth = 0.0;
            double soAmountLastMonth = 0.0;
            double soCostCurrentMonth = 0.0;
            double soCostLastMonth = 0.0;
            double sqAmountCurrentMonth = 0.0;
            double sqAmountLastMonth = 0.0;
            double totalpurchaseinvoiceamount = 0.0;//For Box 3
            double totalpurchaseinvoiceamountYTD = 0.0;//For Box 6
            double pmForcastedFee = 0.0;//For Box 5
            double oneMonthVariance = 0.0;
            double spenttodate = 0.0;
            double budget = 0.0;
            double outputCountyTax = 0.0;
            double outputStateTax = 0.0;
            double outputCityTax = 0.0;
            double costforecast = 0.0;
            double socost = 0.0;
            double costcomplet = 0.0;
            double anticipatedchangeorder = 0.0;
            double revenueforcast = 0.0;
            double totalrevenueforcast = 0.0;
            double projectgainloat = 0.0;
            double costcomplete = 0.0;
            double totalspentperiod=0.0;
            double totalspenttodate=0.0;
            double totalspentCommitted=0.0;
            double totalcostcomplet=0.0;
            double totalcostforecast=0.0;
            double totalsocost=0.0;
            double totalanticipatedchangeorder=0.0;
            double totalprojectgainloat=0.0;
            double totaloneMonthVariance=0.0;
            double totalcostcomplete=0.0;
            while (itracc.hasNext()) {// Account Loop - Requirement to show grouping of Account
                JSONObject jobj = new JSONObject();
                spentperiod=0.0;spentCommitted=0.0;purchaseorderamount=0.0;poAmountCurrentMonth=0.0;poAmountLastMonth=0.0;
                soAmountCurrentMonth=0.0;soAmountLastMonth=0.0;soCostCurrentMonth=0.0;soCostLastMonth=0.0;spenttodate=0.0;
                budget=0.0;costforecast=0.0;socost=0.0;costcomplet=0.0;anticipatedchangeorder=0.0;revenueforcast=0.0;
                projectgainloat=0.0;costcomplete=0.0;sqAmountCurrentMonth=0.0; sqAmountLastMonth=0.0;
                oneMonthVariance=0.0;   
                Object[] rowacc = (Object[]) itracc.next();
                requestLineMap.put("account", rowacc[0].toString());
                jobj.put("accounts", rowacc[1].toString());
                //ERP-41557
                requestLineMap.put(Constants.amountdecimalforcompany,amountDigit);
                JSONObject jobjBudgetData = accAccountDAOobj.getForecastingReportDetails(requestLineMap);
                // Goods Receipt Product Grid with date filter
                if (jobjBudgetData.has("goodsReceiptDetailsDateFilter") && jobjBudgetData.get("goodsReceiptDetailsDateFilter") != null) {
                    JSONArray goodsReceiptDetailsList = jobjBudgetData.getJSONArray("goodsReceiptDetailsDateFilter");
                    if (goodsReceiptDetailsList != null && goodsReceiptDetailsList.length() > 0) {
                        spentperiod = !goodsReceiptDetailsList.get(0).equals(null) ? goodsReceiptDetailsList.getDouble(0) : 0;
                    }
                }
                // Goods Receipt Expense Grid with date filter
                if (jobjBudgetData.has("goodsReceiptExpenseDetailsDateFilter") && jobjBudgetData.get("goodsReceiptExpenseDetailsDateFilter") != null) {
                    JSONArray goodsReceiptExpenseDetailsDateFilterList = jobjBudgetData.getJSONArray("goodsReceiptExpenseDetailsDateFilter");
                    if (goodsReceiptExpenseDetailsDateFilterList != null && goodsReceiptExpenseDetailsDateFilterList.length() > 0) {
                        spentperiod += !goodsReceiptExpenseDetailsDateFilterList.get(0).equals(null) ? goodsReceiptExpenseDetailsDateFilterList.getDouble(0) : 0;
                    }
                }
                //Goods Receipt Year to Current Date  Data
                if (jobjBudgetData.has("goodsReceiptDetailsYTD") && jobjBudgetData.get("goodsReceiptDetailsYTD") != null) {
                    JSONArray goodsReceiptDetailsList = jobjBudgetData.getJSONArray("goodsReceiptDetailsYTD");
                    if (goodsReceiptDetailsList != null && goodsReceiptDetailsList.length() > 0) {
                        totalpurchaseinvoiceamountYTD += !goodsReceiptDetailsList.get(0).equals(null) ? goodsReceiptDetailsList.getDouble(0) : 0;
                    }
                }
                //Goods Receipt Expense Year to Current Date  Data
                if (jobjBudgetData.has("goodsReceiptExpenseDetailsYTD") && jobjBudgetData.get("goodsReceiptExpenseDetailsYTD") != null) {
                    JSONArray goodsReceiptExpenseDetailsDateFilterList = jobjBudgetData.getJSONArray("goodsReceiptExpenseDetailsYTD");
                    if (goodsReceiptExpenseDetailsDateFilterList != null && goodsReceiptExpenseDetailsDateFilterList.length() > 0) {
                        totalpurchaseinvoiceamountYTD += !goodsReceiptExpenseDetailsDateFilterList.get(0).equals(null) ? goodsReceiptExpenseDetailsDateFilterList.getDouble(0) : 0;
                    }
                }

                // Goods Receipt Product Grid without date filter
                if (jobjBudgetData.has("goodsReceiptDetails") && jobjBudgetData.get("goodsReceiptDetails") != null) {
                    JSONArray goodsReceiptDetailsList = jobjBudgetData.getJSONArray("goodsReceiptDetails");
                    if (goodsReceiptDetailsList != null && goodsReceiptDetailsList.length() > 0) {
                        spenttodate = (!goodsReceiptDetailsList.get(0).equals(null) ? goodsReceiptDetailsList.getDouble(0) : 0);
                    }
                }
                // Goods Receipt Expense Grid without date filter
                if (jobjBudgetData.has("goodsReceiptExpenseDetails") && jobjBudgetData.get("goodsReceiptExpenseDetails") != null) {
                    JSONArray goodsReceiptExpenseDetailsList = jobjBudgetData.getJSONArray("goodsReceiptExpenseDetails");
                    if (goodsReceiptExpenseDetailsList != null && goodsReceiptExpenseDetailsList.length() > 0) {
                        spenttodate += (!goodsReceiptExpenseDetailsList.get(0).equals(null) ? goodsReceiptExpenseDetailsList.getDouble(0) : 0);
                    }
                }
                // Purchase Order Product grid without date filter
                if (jobjBudgetData.has("purchaseOrderDetails") && jobjBudgetData.get("purchaseOrderDetails") != null) {
                    JSONArray purchaseOrderDetailsList = jobjBudgetData.getJSONArray("purchaseOrderDetails");
                    if (purchaseOrderDetailsList != null && purchaseOrderDetailsList.length() > 0) {
                        purchaseorderamount = !purchaseOrderDetailsList.get(0).equals(null) ? purchaseOrderDetailsList.getDouble(0) : 0;
                    }
                }
                // Purchase Order Product grid with date filter
//                if (jobjBudgetData.has("purchaseOrderDetails_DateFilter") && jobjBudgetData.get("purchaseOrderDetails_DateFilter") != null) {
//                    JSONArray purchaseOrderDetailsList = jobjBudgetData.getJSONArray("purchaseOrderDetails_DateFilter");
//                    if (purchaseOrderDetailsList != null && purchaseOrderDetailsList.length() > 0) {
//                        purchaseorderForCostForCast = !purchaseOrderDetailsList.get(0).equals(null) ? purchaseOrderDetailsList.getDouble(0) : 0;
//                    }
//                }
                // Purchase Order Amount of current Month
                if (jobjBudgetData.has("purchaseOrderDetails_CurrentMonth") && jobjBudgetData.get("purchaseOrderDetails_CurrentMonth") != null) {
                    JSONArray purchaseOrderDetailsList = jobjBudgetData.getJSONArray("purchaseOrderDetails_CurrentMonth");
                    if (purchaseOrderDetailsList != null && purchaseOrderDetailsList.length() > 0) {
                        poAmountCurrentMonth = !purchaseOrderDetailsList.get(0).equals(null) ? purchaseOrderDetailsList.getDouble(0) : 0;
                    }
                }
                // Purchase Invoice Amount of Last Month
                if (jobjBudgetData.has("purchaseOrderDetails_LastMonth") && jobjBudgetData.get("purchaseOrderDetails_LastMonth") != null) {
                    JSONArray purchaseOrderDetailsList = jobjBudgetData.getJSONArray("purchaseOrderDetails_LastMonth");
                    if (purchaseOrderDetailsList != null && purchaseOrderDetailsList.length() > 0) {
                        poAmountLastMonth = !purchaseOrderDetailsList.get(0).equals(null) ? purchaseOrderDetailsList.getDouble(0) : 0;
                    }
                }
                // Purchase Order Expense grid without date filter
                if (jobjBudgetData.has("expensePODetails") && jobjBudgetData.get("expensePODetails") != null) {
                    JSONArray expensePODetails = jobjBudgetData.getJSONArray("expensePODetails");
                    if (expensePODetails != null && expensePODetails.length() > 0) {
                        purchaseorderamount += !expensePODetails.get(0).equals(null) ? expensePODetails.getDouble(0) : 0;
                    }
                }
//                if (jobjBudgetData.has("expensePODetails_DateFilter") && jobjBudgetData.get("expensePODetails_DateFilter") != null) {
//                    JSONArray expensePODetails = jobjBudgetData.getJSONArray("expensePODetails_DateFilter");
//                    if (expensePODetails != null && expensePODetails.length() > 0) {
//                        purchaseorderForCostForCast += !expensePODetails.get(0).equals(null) ? expensePODetails.getDouble(0) : 0;
//                    }
//                }
                // Purchase Order Expense Amount Of Current Month 
                if (jobjBudgetData.has("expensePODetails_CurrentMonth") && jobjBudgetData.get("expensePODetails_CurrentMonth") != null) {
                    JSONArray expensePODetails = jobjBudgetData.getJSONArray("expensePODetails_CurrentMonth");
                    if (expensePODetails != null && expensePODetails.length() > 0) {
                        poAmountCurrentMonth += !expensePODetails.get(0).equals(null) ? expensePODetails.getDouble(0) : 0;
                    }
                }
                // Purchase Order Expense Amount Of Last Month 
                if (jobjBudgetData.has("expensePODetails_LastMonth") && jobjBudgetData.get("expensePODetails_LastMonth") != null) {
                    JSONArray expensePODetails = jobjBudgetData.getJSONArray("expensePODetails_LastMonth");
                    if (expensePODetails != null && expensePODetails.length() > 0) {
                        poAmountLastMonth += !expensePODetails.get(0).equals(null) ? expensePODetails.getDouble(0) : 0;
                    }
                }
                // Sales Order total cost and its amount
                if (jobjBudgetData.has("salesOrderDetails") && jobjBudgetData.get("salesOrderDetails") != null) {
                    JSONArray salesOrderDetailsList = jobjBudgetData.getJSONArray("salesOrderDetails");
                    if (salesOrderDetailsList != null && salesOrderDetailsList.length() > 0) {
                        budget = !salesOrderDetailsList.getJSONArray(0).get(0).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(0) : 0;
                        socost = !salesOrderDetailsList.getJSONArray(0).get(1).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(1) : 0;
                    }
                }
                // Sales Order Line Level Tax Amount - GST
                if (jobjBudgetData.has("salesOrderDetailsTax") && jobjBudgetData.get("salesOrderDetailsTax") != null) {
                    JSONArray salesOrderDetailsList = jobjBudgetData.getJSONArray("salesOrderDetailsTax");
                    if (salesOrderDetailsList != null && salesOrderDetailsList.length() > 0) {
                        for(int i=0;i<salesOrderDetailsList.length();i++){
                            if (!salesOrderDetailsList.getJSONArray(i).get(1).equals(null)) {
                                if (salesOrderDetailsList.getJSONArray(i).get(1).equals("Output County Tax")) {
                                    outputCountyTax += salesOrderDetailsList.getJSONArray(i).getDouble(0) ;
                                }
                                if (salesOrderDetailsList.getJSONArray(i).get(1).equals("Output State Tax")) {
                                    outputStateTax += salesOrderDetailsList.getJSONArray(i).getDouble(0) ;
                                }
                                if (salesOrderDetailsList.getJSONArray(i).get(1).equals("Output City Tax")) {
                                    outputCityTax += salesOrderDetailsList.getJSONArray(i).getDouble(0) ;
                                }
                            }
                        }
                    }
                }
                // Sales Order Amount of Current Month
                if (jobjBudgetData.has("salesOrderDetails_CurrentMonth") && jobjBudgetData.get("salesOrderDetails_CurrentMonth") != null) {
                    JSONArray salesOrderDetailsList = jobjBudgetData.getJSONArray("salesOrderDetails_CurrentMonth");
                    if (salesOrderDetailsList != null && salesOrderDetailsList.length() > 0) {
                        soAmountCurrentMonth = !salesOrderDetailsList.getJSONArray(0).get(0).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(0) : 0;
                        soCostCurrentMonth = !salesOrderDetailsList.getJSONArray(0).get(1).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(1) : 0;
                    }
                }
                // Sales Order Amount of Last Month
                if (jobjBudgetData.has("salesOrderDetails_LastMonth") && jobjBudgetData.get("salesOrderDetails_LastMonth") != null) {
                    JSONArray salesOrderDetailsList = jobjBudgetData.getJSONArray("salesOrderDetails_LastMonth");
                    if (salesOrderDetailsList != null && salesOrderDetailsList.length() > 0) {
                        soAmountLastMonth = !salesOrderDetailsList.getJSONArray(0).get(0).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(0) : 0;
                        soCostLastMonth = !salesOrderDetailsList.getJSONArray(0).get(1).equals(null) ? salesOrderDetailsList.getJSONArray(0).getDouble(1) : 0;
                    }
                }

                // Customer Quotation
                if (jobjBudgetData.has("quotationDetails") && jobjBudgetData.get("quotationDetails") != null) {
                    JSONArray quotationDetailsList = jobjBudgetData.getJSONArray("quotationDetails");
                    if (quotationDetailsList != null && quotationDetailsList.length() > 0) {
                        anticipatedchangeorder = !quotationDetailsList.getJSONArray(0).get(0).equals(null) ? quotationDetailsList.getJSONArray(0).getDouble(0) : 0;
                    }
                }
                // Customer Quotation Line Level Tax - GST
                if (jobjBudgetData.has("quotationDetailsTax") && jobjBudgetData.get("quotationDetailsTax") != null) {
                    JSONArray quotationDetailsList = jobjBudgetData.getJSONArray("quotationDetailsTax");
                    if (quotationDetailsList != null && quotationDetailsList.length() > 0) {
                        for(int i=0;i<quotationDetailsList.length();i++){
                            if (!quotationDetailsList.getJSONArray(i).get(1).equals(null)) {
                                if (quotationDetailsList.getJSONArray(i).get(1).equals("Output County Tax")) {
                                    outputCountyTax += quotationDetailsList.getJSONArray(i).getDouble(0) ;
                                }
                                if (quotationDetailsList.getJSONArray(i).get(1).equals("Output State Tax")) {
                                    outputStateTax += quotationDetailsList.getJSONArray(i).getDouble(0) ;
                                }
                                if (quotationDetailsList.getJSONArray(i).get(1).equals("Output City Tax")) {
                                    outputCityTax += quotationDetailsList.getJSONArray(i).getDouble(0) ;
                                }
                            }
                        }
                    }
                }
                // Customer Quotation Detail of Current Month
                if (jobjBudgetData.has("quotationDetails_CurrentMonth") && jobjBudgetData.get("quotationDetails_CurrentMonth") != null) {
                    JSONArray quotationDetailsList = jobjBudgetData.getJSONArray("quotationDetails_CurrentMonth");
                    if (quotationDetailsList != null && quotationDetailsList.length() > 0) {
                        sqAmountCurrentMonth = !quotationDetailsList.getJSONArray(0).get(0).equals(null) ? quotationDetailsList.getJSONArray(0).getDouble(0) : 0;
                    }
                }
                // Customer Quotation Detail of Last Month
                if (jobjBudgetData.has("quotationDetails_LastMonth") && jobjBudgetData.get("quotationDetails_LastMonth") != null) {
                    JSONArray quotationDetailsList = jobjBudgetData.getJSONArray("quotationDetails_LastMonth");
                    if (quotationDetailsList != null && quotationDetailsList.length() > 0) {
                        sqAmountLastMonth = !quotationDetailsList.getJSONArray(0).get(0).equals(null) ? quotationDetailsList.getJSONArray(0).getDouble(0) : 0;
                    }
                }

                spentCommitted = spenttodate + purchaseorderamount;

                if (socost >= spentCommitted) {
                    costforecast = budget - (socost);
                } else {
                    costforecast = budget - (spentCommitted);
                }

                costcomplet = costforecast - spentCommitted;
                revenueforcast = socost + anticipatedchangeorder;
                projectgainloat = revenueforcast - costforecast;

                if (costforecast != 0) {
                    costcomplete = spenttodate / costforecast;
                }
                double greaterSOPOCurrent = soCostCurrentMonth > poAmountCurrentMonth ? soCostCurrentMonth : poAmountCurrentMonth;
                
                // Formula Current month - Last Month - More Details in ERM-758 Attached Forcasting Report details
                double greaterSOPOLast = soCostLastMonth > poAmountLastMonth ? soCostLastMonth : poAmountLastMonth;
                oneMonthVariance = ((soCostCurrentMonth + sqAmountCurrentMonth) - (soAmountCurrentMonth - (greaterSOPOCurrent)))// Current Month
                        - ((soCostLastMonth + sqAmountLastMonth) - (soAmountLastMonth - (greaterSOPOLast)));// Last Moth       
                
                totalpurchaseinvoiceamount += spentperiod;
                pmForcastedFee += projectgainloat;
                if (spenttodate == 0 && spentperiod == 0 && spentCommitted == 0 && costcomplet == 0 && costforecast == 0 && socost == 0 && anticipatedchangeorder == 0 && revenueforcast==0 && projectgainloat==0 && oneMonthVariance==0 && costcomplete==0) {
                    continue;
                }

                jobj.put("spentthisperiod", spentperiod);
                jobj.put("spenttodate", spenttodate);
                jobj.put("spentcommitted", spentCommitted);
                jobj.put("cost", costcomplet);
                jobj.put("costforcast", costforecast);
                jobj.put("currentRevenuebudget", socost);
                jobj.put("anticipatedchangeorder", anticipatedchangeorder);
                jobj.put("revenueforecast", revenueforcast);
                jobj.put("projectedgainloss", projectgainloat);
                jobj.put("onemonthvariance", oneMonthVariance);
                jobj.put("costcomplete", costcomplete);
                
                // For Total Amount Last Row
                totalspentperiod+=spentperiod;
                totalspenttodate+=spenttodate;
                totalspentCommitted+=spentCommitted;
                totalcostcomplet+=costcomplet;
                totalcostforecast+=costforecast;
                totalsocost+=socost;
                totalanticipatedchangeorder+=anticipatedchangeorder;
                totalrevenueforcast += revenueforcast;
                totalprojectgainloat+=projectgainloat;
                totaloneMonthVariance+=oneMonthVariance;
                totalcostcomplete+=costcomplete;
                dataArray.put(jobj);
            }
                JSONObject jobjTotal = new JSONObject();
                jobjTotal.put("accounts", "Total");
                jobjTotal.put("spentthisperiod", totalspentperiod);
                jobjTotal.put("spenttodate", totalspenttodate);
                jobjTotal.put("spentcommitted", totalspentCommitted);
                jobjTotal.put("cost", totalcostcomplet);
                jobjTotal.put("costforcast", totalcostforecast);
                jobjTotal.put("currentRevenuebudget", totalsocost);
                jobjTotal.put("anticipatedchangeorder", totalanticipatedchangeorder);
                jobjTotal.put("revenueforecast", totalrevenueforcast);
                jobjTotal.put("projectedgainloss", totalprojectgainloat);
                jobjTotal.put("onemonthvariance", totaloneMonthVariance);
                jobjTotal.put("costcomplete", totalcostcomplete);
                dataArray.put(jobjTotal);
                
                // For Extra 4 lines for tax details is its total.
                JSONObject jobjTax = new JSONObject();
                jobjTax.put("anticipatedchangeorder", "Output City Tax :");
                jobjTax.put("revenueforecast", outputCityTax);
                dataArray.put(jobjTax);
                jobjTax = new JSONObject();
                jobjTax.put("anticipatedchangeorder", "Output State Tax :");
                jobjTax.put("revenueforecast", outputStateTax);
                dataArray.put(jobjTax);
                jobjTax = new JSONObject();
                jobjTax.put("anticipatedchangeorder", "Output County Tax :");
                jobjTax.put("revenueforecast", outputCountyTax);
                dataArray.put(jobjTax);
                jobjTax = new JSONObject();
                jobjTax.put("anticipatedchangeorder", "Total Tax :");
                jobjTax.put("revenueforecast", outputCityTax + outputStateTax +outputCountyTax);
                dataArray.put(jobjTax);
            
            
            result.put(Constants.RES_data, dataArray);
            result.put(Constants.RES_count, count);
            response.put("jobforcastsummaryreport", result);
            
            // Report Widget 2 data
            double sofirstrecord = 0.0;
            double totalSOAmount = 0.0;
            int SOcount = 0;
            double currentContactAmount = 0.0;
            double totalexternalAmount = 0.0;
            int totalexternalcount = 0;
            double opensubmittedAmount = 0.0;
            int opensubmittedcount = 0;
            double openrevisedresubAmount = 0.0;
            int openrevisedresubcount = 0;
            double openinprogressAmount = 0.0;
            int openinprogresscount = 0;
            double closeddeniedAmount = 0.0;
            int closeddeniedcount = 0;
            double projectfinalamount = 0;
            int projectfinalcount = 0;
            
            //Quote Status custom field data 
            requestGlobalMap.put("customfieldName", "Quote Status"); //Custom field Lebel
            requestGlobalMap.put("customfield", "1");// For Custom field
            List quotesStatusCombodata = accAccountDAOobj.getFieldComboData(requestGlobalMap);
            requestGlobalMap.clear();
            Iterator itrquoteStatuscombo = quotesStatusCombodata.iterator();
            JSONObject jobjquotesStatusCombodata = new JSONObject();
            while (itrquoteStatuscombo.hasNext()) {
                Object[] row = (Object[]) itrquoteStatuscombo.next();
                if ((short) row[2] == Constants.Acc_Customer_Quotation_ModuleId) {
                    jobjquotesStatusCombodata.put(row[3].toString()+"Id", row[0]);
                    jobjquotesStatusCombodata.put("QuotationStatusCol", row[1]);
                }
            }
            requestLineMap.put("quotesStatusField", jobjquotesStatusCombodata);
            
            //PCI Type custom field data 
            requestGlobalMap.put("dimension", "External");// Custom field Value
            requestGlobalMap.put("customfieldName", "PCI Type"); //Custom field Lebel
            requestGlobalMap.put("customfield", "1");// For Custom field
            List PCITypeCombodata = accAccountDAOobj.getFieldComboData(requestGlobalMap);
            requestGlobalMap.clear();
            Iterator itrPCITypecombo = PCITypeCombodata.iterator();
            JSONObject jobjPCITypeCombodata = new JSONObject();
            while (itrPCITypecombo.hasNext()) {
                Object[] row = (Object[]) itrPCITypecombo.next();
                if ((short) row[2] == Constants.Acc_Customer_Quotation_ModuleId) {
                    jobjPCITypeCombodata.put("Customer_Quotation_PCI_customfieldId", row[0]);
                    jobjPCITypeCombodata.put("Customer_Quotation_PCI_customfieldCol", row[1]);
                }
            }
            requestLineMap.put("globalCustomFieldPCIType", jobjPCITypeCombodata);


            // First Sales Order Details related to Project Code
            //ERP-41557
            requestLineMap.put(Constants.amountdecimalforcompany,amountDigit);
            JSONObject jobjBudgetData = accAccountDAOobj.getChangeOrderStatusReportDetails(requestLineMap);
            if (jobjBudgetData.has("salesOrderDetails_FST") && jobjBudgetData.get("salesOrderDetails_FST") != null) {
                JSONArray SODetailsList = jobjBudgetData.getJSONArray("salesOrderDetails_FST");
                if (SODetailsList != null && SODetailsList.length() > 0) {
                    sofirstrecord = !SODetailsList.getJSONArray(0).get(0).equals(null) ? SODetailsList.getJSONArray(0).getDouble(0) : 0;
                }
            }
            // Total number of Sales order Document
            if (jobjBudgetData.has("salesOrderDetails_SUMCOUNT") && jobjBudgetData.get("salesOrderDetails_SUMCOUNT") != null) {
                JSONArray SODetailsList = jobjBudgetData.getJSONArray("salesOrderDetails_SUMCOUNT");
                if (SODetailsList != null && SODetailsList.length() > 0) {
                    totalSOAmount = !SODetailsList.getJSONArray(0).get(0).equals(null) ? SODetailsList.getJSONArray(0).getDouble(0) : 0;
                    SOcount = !SODetailsList.getJSONArray(0).get(1).equals(null) ? SODetailsList.getJSONArray(0).getInt(1) : 0;
                }
            }
            
            // Quotation Details Based of Custom Column - Quots Status.
            if (jobjBudgetData.has("quotationstatusDetails") && jobjBudgetData.get("quotationstatusDetails") != null) {
                JSONArray SODetailsList = jobjBudgetData.getJSONArray("quotationstatusDetails");
                if (SODetailsList != null && SODetailsList.length() > 0) {
                    for (int j = 0; j < SODetailsList.length(); j++) {
                        if (SODetailsList.getJSONArray(j) != null) {
                            if (jobjquotesStatusCombodata.getString("Open-SubmittedId").equals(SODetailsList.getJSONArray(j).get(3))) {
                                opensubmittedAmount = SODetailsList.getJSONArray(j).get(0)!=null?SODetailsList.getJSONArray(j).getDouble(0):0;
                                opensubmittedcount = SODetailsList.getJSONArray(j).get(2)!=null?SODetailsList.getJSONArray(j).getInt(2):0;
                            }
                            if (jobjquotesStatusCombodata.getString("Open-Revise and ResubmitId").equals(SODetailsList.getJSONArray(j).get(3))) {
                                openrevisedresubAmount = SODetailsList.getJSONArray(j).get(0)!=null?SODetailsList.getJSONArray(j).getDouble(0):0;
                                openrevisedresubcount = SODetailsList.getJSONArray(j).get(2)!=null?SODetailsList.getJSONArray(j).getInt(2):0;
                            }
                            if (jobjquotesStatusCombodata.getString("Open-In ProgressId").equals(SODetailsList.getJSONArray(j).get(3))) {
                                openinprogressAmount = SODetailsList.getJSONArray(j).get(0)!=null?SODetailsList.getJSONArray(j).getDouble(0):0;
                                openinprogresscount = SODetailsList.getJSONArray(j).get(2)!=null?SODetailsList.getJSONArray(j).getInt(2):0;
                            }
                            if (jobjquotesStatusCombodata.getString("Closed-DeniedId").equals(SODetailsList.getJSONArray(j).get(3))) {
                                closeddeniedAmount = SODetailsList.getJSONArray(j).get(0)!=null?SODetailsList.getJSONArray(j).getDouble(0):0;
                                closeddeniedcount = SODetailsList.getJSONArray(j).get(2)!=null?SODetailsList.getJSONArray(j).getInt(2):0;
                            }
                        }
                    } 
                }
            }
            
            totalexternalAmount = opensubmittedAmount + openrevisedresubAmount  + openinprogressAmount + closeddeniedAmount;
            totalexternalcount = opensubmittedcount + openrevisedresubcount  + openinprogresscount + closeddeniedcount;
            currentContactAmount = sofirstrecord + totalSOAmount;
            projectfinalcount = SOcount + totalexternalcount;
            projectfinalamount = totalSOAmount + totalexternalAmount;
            JSONObject resultchangeorder = new JSONObject();
            JSONArray dataChangeorder = new JSONArray();
            JSONObject jobj = new JSONObject();
            jobj.put("parametertitles", "Original Contract");
            jobj.put("no", " - ");
            jobj.put("currentamount", sofirstrecord);
            dataChangeorder.put(jobj);
            jobj = new JSONObject();
            jobj.put("parametertitles", "Approved Customer Change Order");
            jobj.put("no",SOcount);
            jobj.put("currentamount", totalSOAmount);
            dataChangeorder.put(jobj);
            jobj = new JSONObject();
            jobj.put("parametertitles", "Current Contract");
            jobj.put("no", " - ");
            jobj.put("currentamount", currentContactAmount);
            dataChangeorder.put(jobj);
            jobj = new JSONObject();
            jobj.put("parametertitles", "Submitted (External Change Order Customer Quotation)");
            jobj.put("no", opensubmittedcount);
            jobj.put("currentamount", opensubmittedAmount);
            dataChangeorder.put(jobj);
            jobj = new JSONObject();
            jobj.put("parametertitles", "Revise and Resubmit  (External Change Order Customer Quotation)");
            jobj.put("no", openrevisedresubcount);
            jobj.put("currentamount", openrevisedresubAmount);
            dataChangeorder.put(jobj);
            jobj = new JSONObject();
            jobj.put("parametertitles", "Pending   (External Change Order Customer Quotation)");
            jobj.put("no", openinprogresscount);
            jobj.put("currentamount", openinprogressAmount);
            dataChangeorder.put(jobj);
            jobj = new JSONObject();
            jobj.put("parametertitles", "Denied  (External Change Order Customer Quotation)");
            jobj.put("no", closeddeniedcount);
            jobj.put("currentamount", closeddeniedAmount);
            dataChangeorder.put(jobj);
            jobj = new JSONObject();
            jobj.put("parametertitles", "Total External PCI's ");
            jobj.put("no", totalexternalcount);
            jobj.put("currentamount", totalexternalAmount);
            dataChangeorder.put(jobj);
            jobj = new JSONObject();
            jobj.put("parametertitles", "Projected Final");
            jobj.put("no", projectfinalcount);
            jobj.put("currentamount", projectfinalamount);
            dataChangeorder.put(jobj);
            resultchangeorder.put(Constants.RES_data, dataChangeorder);
            response.put("changeorderstatus", resultchangeorder);
            
            // Report Widget 3 data
            double salesbillingamount=0.0;
            double salestaxamount=0.0;
            if (jobjBudgetData.has("invoiceBillingDetails") && jobjBudgetData.get("invoiceBillingDetails") != null) {
                JSONArray SODetailsList = jobjBudgetData.getJSONArray("invoiceBillingDetails");
                if (SODetailsList != null && SODetailsList.length() > 0) {
                    salesbillingamount = !SODetailsList.getJSONArray(0).get(0).equals(null) ? SODetailsList.getJSONArray(0).getDouble(0) : 0;
                    salestaxamount = !SODetailsList.getJSONArray(0).get(1).equals(null) ? SODetailsList.getJSONArray(0).getDouble(1) : 0;
                }
            }
            double grossmargin = salesbillingamount - totalpurchaseinvoiceamount;
            double grossbilling = salesbillingamount + salestaxamount;
            
            double gm = 0.0;
            double estfnlcony = 0.0;
            if(salesbillingamount!=0.0){
                gm = (grossmargin/ salesbillingamount)*100;
            }
            if(totalrevenueforcast!=0.0){
                estfnlcony = (salesbillingamount/ totalrevenueforcast);
            }
            JSONObject resultbilling = new JSONObject();
            JSONArray databilling = new JSONArray();
            JSONObject jobjbilling = new JSONObject();
            jobjbilling.put("parametertitles", "Gross Billing");
            jobjbilling.put("current", grossbilling);
            databilling.put(jobjbilling);
            jobjbilling = new JSONObject();
            jobjbilling.put("parametertitles", "Sales Tax");
            jobjbilling.put("current", salestaxamount);
            databilling.put(jobjbilling);
            jobjbilling = new JSONObject();
            jobjbilling.put("parametertitles", "Billing Net of Tax");
            jobjbilling.put("current", salesbillingamount);
            databilling.put(jobjbilling);
            jobjbilling = new JSONObject();
            jobjbilling.put("parametertitles", "Gross Cost");
            jobjbilling.put("current", totalpurchaseinvoiceamount);
            databilling.put(jobjbilling);
            jobjbilling = new JSONObject();
            jobjbilling.put("parametertitles", "Gross Margin");
            jobjbilling.put("current", grossmargin);
            databilling.put(jobjbilling);
            jobjbilling = new JSONObject();
            jobjbilling.put("parametertitles", "GM %");
            jobjbilling.put("current", gm);
            databilling.put(jobjbilling);
            jobjbilling = new JSONObject();
            jobjbilling.put("parametertitles", "% Est Fnl Cont Billed");
            jobjbilling.put("current", estfnlcony);
            databilling.put(jobjbilling);
            resultbilling.put(Constants.RES_data, databilling);
            response.put("billing", resultbilling);
            
            // Report Widget 4 data
            double totalsalesoutstanding = 0.0;
            double currentsalesoutstanding = 0.0;
            double over30daysoutstanding = 0.0;
            double over60daysoutstanding = 0.0;
            double over90daysoutstanding = 0.0;
            
            // Total Sales Order Due Amount - Pending Payment 
            if (jobjBudgetData.has("totalOutstandingAmount") && jobjBudgetData.get("totalOutstandingAmount") != null) {
                JSONArray SIDetailsList = jobjBudgetData.getJSONArray("totalOutstandingAmount");
                if (SIDetailsList != null && SIDetailsList.length() > 0) {
                    totalsalesoutstanding = !SIDetailsList.get(0).equals(null) ? SIDetailsList.getDouble(0) : 0;
                }
            }
            
            // Current Month Sales Order Due Amount - Pending Payment 
            if (jobjBudgetData.has("currentoutstanging") && jobjBudgetData.get("currentoutstanging") != null) {
                JSONArray SIDetailsList = jobjBudgetData.getJSONArray("currentoutstanging");
                if (SIDetailsList != null && SIDetailsList.length() > 0) {
                    currentsalesoutstanding = !SIDetailsList.get(0).equals(null) ? SIDetailsList.getDouble(0) : 0;
                }
            }
            
            // 30 to 60 days Sales Order Due Amount - Pending Payment 
            if (jobjBudgetData.has("over30Outstanding") && jobjBudgetData.get("over30Outstanding") != null) {
                JSONArray SIDetailsList = jobjBudgetData.getJSONArray("over30Outstanding");
                if (SIDetailsList != null && SIDetailsList.length() > 0) {
                    over30daysoutstanding = !SIDetailsList.get(0).equals(null) ? SIDetailsList.getDouble(0) : 0;
                }
            }
            
            // 60 to 90 days Sales Order Due Amount - Pending Payment 
            if (jobjBudgetData.has("over60Outstanding") && jobjBudgetData.get("over60Outstanding") != null) {
                JSONArray SIDetailsList = jobjBudgetData.getJSONArray("over60Outstanding");
                if (SIDetailsList != null && SIDetailsList.length() > 0) {
                    over60daysoutstanding = !SIDetailsList.get(0).equals(null) ? SIDetailsList.getDouble(0) : 0;
                }
            }
            
            // All Due Amount over 90 days- Pending Payment 
            if (jobjBudgetData.has("over90Outstanding") && jobjBudgetData.get("over90Outstanding") != null) {
                JSONArray SIDetailsList = jobjBudgetData.getJSONArray("over90Outstanding");
                if (SIDetailsList != null && SIDetailsList.length() > 0) {
                    over90daysoutstanding = !SIDetailsList.get(0).equals(null) ? SIDetailsList.getDouble(0) : 0;
                }
            }
            
            // Below Code is to fetch Inventory Amount of Financials Statement Report ---- Start ----
            JSONObject searchjson = new JSONObject();
            JSONArray root= new JSONArray();
            searchjson.put("column", jsonParamObj.getString("globaldimension"));
            searchjson.put("iscustomcolumn", true);
            searchjson.put("iscustomcolumndata", false);
            searchjson.put("isfrmpmproduct", "");
            searchjson.put("fieldtype", 4);
            searchjson.put("searchText", jsonParamObj.getString("globaldimensionCombodate"));
            searchjson.put("columnheader", jsonParamObj.getString("globaldimensionName"));
            searchjson.put("search", jsonParamObj.getString("globaldimensionCombodate"));
            searchjson.put("xtype", "select");
            searchjson.put("combosearch", jsonParamObj.getString("globalDimensioncombodataname"));
            searchjson.put("isinterval", false);
            searchjson.put("interval", "");
            searchjson.put("isbefore", "");
            searchjson.put("isdefaultfield", false);
            searchjson.put("moduleid", Constants.Acc_Invoice_ModuleId);
            searchjson.put("isForProductMasterOnly", "");
            searchjson.put("transactionSearch", "1");
            searchjson.put("includingTax", false);
            searchjson.put("includingDiscount", false);
            searchjson.put("isForProductMasterOnly", "");
            searchjson.put("iscustomfield", false);
            searchjson.put("isMultiEntity", false);
            searchjson.put("modulename", "");
            searchjson.put("isForProductMasterSearch", "");
            root.put(searchjson);
            searchjson = new JSONObject();
            searchjson.put("root", root);
            Map<String, Double> accAmtMap = null;
            Map<String, Object> requestParamsAccount = null;
            JSONArray jArrR = new JSONArray();
            requestParamsAccount = new HashMap<String, Object>();
            requestParamsAccount.put("isMonthly", false);
            requestParamsAccount.put("startdate", sdf.parse(startDate));
            requestParamsAccount.put("enddate", sdf.parse(endDate));
            requestParamsAccount.put("startpredate", null);
            requestParamsAccount.put("endpredate", null);
            requestParamsAccount.put("companyid", companyID);
            requestParamsAccount.put("fieldtype", null);
            requestParamsAccount.put("customdatavalues", null);
            requestParamsAccount.put("columns", null);
            requestParamsAccount.put("iscustomcolumndata", 0);
            requestParamsAccount.put("columnheader", null);
            requestParamsAccount.put("isProductCustomData", false);
            requestParamsAccount.put("isForKnockOff", false);
            requestParamsAccount.put("Searchjson", searchjson.toString());
            requestParamsAccount.put("filterConjuctionCriteria", " and ");
            accAmtMap = accReportsService.getPeriodAccountAmountMap(requestParamsAccount); 
            try {
                jsonParamObj.put("excludeP_LFilters", true);
                double tempcTotal[] = accReportsService.getTradingAllAccount(jsonParamObj, Group.NATURE_ASSET, jArrR, accAmtMap, requestParamsAccount);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            double totalInventory = 0.0;
            for(int i = 0;i<jArrR.length();i++){
                JSONObject jobjInventory = (JSONObject)jArrR.get(i);
                if(jobjInventory.has("accountname") && jobjInventory.getString("accountname").equals("Inventory")){
                    totalInventory = jobjInventory.getDouble("periodamount");
                    break;
                }
            }
            // Above Code is to fetch Inventory Amount of Financials Statement Report ---- End ----
            
            JSONObject resultReceivablesInventory = new JSONObject();
            JSONArray dataReceivablesInventory = new JSONArray();
            JSONObject jobjReceivablesInventory = new JSONObject();
            jobjReceivablesInventory.put("parametertitles", "Total Inventory");
            jobjReceivablesInventory.put("current", totalInventory);
            dataReceivablesInventory.put(jobjReceivablesInventory);
            jobjReceivablesInventory = new JSONObject();
            jobjReceivablesInventory.put("parametertitles", "Total Receivables");
            jobjReceivablesInventory.put("current", totalsalesoutstanding);
            dataReceivablesInventory.put(jobjReceivablesInventory);
            jobjReceivablesInventory = new JSONObject();
            jobjReceivablesInventory.put("parametertitles", "Current");
            jobjReceivablesInventory.put("current", currentsalesoutstanding);
            dataReceivablesInventory.put(jobjReceivablesInventory);
            jobjReceivablesInventory = new JSONObject();
            jobjReceivablesInventory.put("parametertitles", "Over 30");
            jobjReceivablesInventory.put("current", over30daysoutstanding);
            dataReceivablesInventory.put(jobjReceivablesInventory);
            jobjReceivablesInventory = new JSONObject();
            jobjReceivablesInventory.put("parametertitles", "Over 60");
            jobjReceivablesInventory.put("current", over60daysoutstanding);
            dataReceivablesInventory.put(jobjReceivablesInventory);
            jobjReceivablesInventory = new JSONObject();
            jobjReceivablesInventory.put("parametertitles", "Over 90");
            jobjReceivablesInventory.put("current", over90daysoutstanding);
            dataReceivablesInventory.put(jobjReceivablesInventory);
            resultReceivablesInventory.put(Constants.RES_data, dataReceivablesInventory);
            response.put("ReceivablesInventory", resultReceivablesInventory);
            
            // Report Widget 5 data
            double profitmargin = 0.0;
            double variance = 0.0;
            // Total Profit Margin
            if (jobjBudgetData.has("salesOrderDetails_PROFIT") && jobjBudgetData.get("salesOrderDetails_PROFIT") != null) {
                JSONArray SIDetailsList = jobjBudgetData.getJSONArray("salesOrderDetails_PROFIT");
                if (SIDetailsList != null && SIDetailsList.length() > 0) {
                    profitmargin = !SIDetailsList.get(0).equals(null) ? SIDetailsList.getDouble(0) : 0;
                }
            }
            variance = pmForcastedFee - profitmargin;
            JSONObject resultFeeSummary = new JSONObject();
            JSONArray dataFeeSummary = new JSONArray();
            JSONObject jobjFeeSummary = new JSONObject();
            jobjFeeSummary.put("parametertitles", "PM Forecasted Fee");
            jobjFeeSummary.put("current", pmForcastedFee);
            dataFeeSummary.put(jobjFeeSummary);
            jobjFeeSummary = new JSONObject();
            jobjFeeSummary.put("parametertitles", "Sales Order Fee");
            jobjFeeSummary.put("current", profitmargin);
            dataFeeSummary.put(jobjFeeSummary);
            jobjFeeSummary = new JSONObject();
            jobjFeeSummary.put("parametertitles", "Variance");
            jobjFeeSummary.put("current", variance);
            dataFeeSummary.put(jobjFeeSummary);
            jobjFeeSummary = new JSONObject();
            jobjFeeSummary.put("parametertitles", "Original Contract Fee");
            jobjFeeSummary.put("current", sofirstrecord);
            dataFeeSummary.put(jobjFeeSummary);
            resultFeeSummary.put(Constants.RES_data, dataFeeSummary);
            response.put("FeeSummary", resultFeeSummary);
            // Report 6
            double salesinvoiceYTD = 0.0;
            double grossMarginYTD = 0.0;
            double gmpercentYTD = 0.0;
             // Sales Invoice Amount Year to current Date
            if (jobjBudgetData.has("invoiceAmountYearToDate") && jobjBudgetData.get("invoiceAmountYearToDate") != null) {
                JSONArray SODetailsList = jobjBudgetData.getJSONArray("invoiceAmountYearToDate");
                if (SODetailsList != null && SODetailsList.length() > 0) {
                    salesinvoiceYTD = !SODetailsList.getJSONArray(0).get(0).equals(null) ? SODetailsList.getJSONArray(0).getDouble(0) : 0;
                }
            }
            grossMarginYTD = salesinvoiceYTD - totalpurchaseinvoiceamountYTD;
            if(salesinvoiceYTD!=0.0){
                gmpercentYTD = (grossMarginYTD/salesinvoiceYTD) * 100;
            }
            
            JSONObject resultProfitLossBacklog = new JSONObject();
            JSONArray dataProfitLossBacklog = new JSONArray();
            JSONObject jobjProfitLossBacklog = new JSONObject();
            jobjProfitLossBacklog.put("parametertitles", "Billing Net of Tax");
            jobjProfitLossBacklog.put("yeartodate", salesinvoiceYTD);
            jobjProfitLossBacklog.put("inceptiontodate", salesbillingamount);
            dataProfitLossBacklog.put(jobjProfitLossBacklog);
            jobjProfitLossBacklog = new JSONObject();
            jobjProfitLossBacklog.put("parametertitles", "Gross Cost");
            jobjProfitLossBacklog.put("yeartodate", totalpurchaseinvoiceamountYTD);
            jobjProfitLossBacklog.put("inceptiontodate", totalpurchaseinvoiceamount);
            dataProfitLossBacklog.put(jobjProfitLossBacklog);
            jobjProfitLossBacklog = new JSONObject();
            jobjProfitLossBacklog.put("parametertitles", "Gross Margin");
            jobjProfitLossBacklog.put("yeartodate", grossMarginYTD);
            jobjProfitLossBacklog.put("inceptiontodate", grossmargin);
            dataProfitLossBacklog.put(jobjProfitLossBacklog);
            jobjProfitLossBacklog = new JSONObject();
            jobjProfitLossBacklog.put("parametertitles", "GM %");
            jobjProfitLossBacklog.put("yeartodate", gmpercentYTD);
            jobjProfitLossBacklog.put("inceptiontodate", gm);
            dataProfitLossBacklog.put(jobjProfitLossBacklog);
            resultProfitLossBacklog.put(Constants.RES_data, dataProfitLossBacklog);
            response.put("ProfitLossBacklog", resultProfitLossBacklog);

        } catch (JSONException ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    /**Below Function is used only for Forcasting report 
     * Its Merge Multiple Report Data into a single Report data
    **/
    @Override
    public JSONObject getForecastingReportExportObject(JSONObject jsonParamObj) throws ServiceException {
        JSONObject result = new JSONObject();
        try {
            JSONObject response = new JSONObject();
            JSONArray jsonArrayEx = new JSONArray();
            response = jsonParamObj.getJSONObject("jobforcastsummaryreport");
            JSONArray jsonArray = response.getJSONArray(Constants.RES_data);// Report 1 Data

            /**
             * Empty Space*
             */
            JSONObject jobj = new JSONObject();
            jobj.put("accounts", " ");
            jobj.put("spentthisperiod", " ");
            jobj.put("spenttodate", " ");
            jsonArray.put(jobj);

            /**
             * Report 2 Name*
             */
            jobj = new JSONObject();
            jobj.put("accounts", "Change Order Status - External PCIs : ");
            jobj.put("spentthisperiod", "No.");
            jobj.put("spenttodate", "Current Amount");
            jsonArray.put(jobj);

            /**
             * Report 2 Data*
             */
            response = jsonParamObj.getJSONObject("changeorderstatus");
            jsonArrayEx = response.getJSONArray(Constants.RES_data);
            for (int i = 0; i < jsonArrayEx.length(); i++) {
                jobj = jsonArrayEx.getJSONObject(i);
                jobj.put("accounts", jobj.get("parametertitles"));
                jobj.put("spentthisperiod", jobj.get("no"));
                jobj.put("spenttodate", jobj.get("currentamount"));
                jsonArray.put(jobj);
            }

            /**
             * Empty Space*
             */
            jobj = new JSONObject();
            jobj.put("accounts", " ");
            jobj.put("spentthisperiod", " ");
            jsonArray.put(jobj);

            /**
             * Report 3 Name*
             */
            jobj = new JSONObject();
            jobj.put("accounts", "Billings : ");
            jobj.put("spentthisperiod", "Current");
            jsonArray.put(jobj);

            /**
             * Report 3 Data*
             */
            response = jsonParamObj.getJSONObject("billing");
            jsonArrayEx = response.getJSONArray(Constants.RES_data);
            for (int i = 0; i < jsonArrayEx.length(); i++) {
                jobj = jsonArrayEx.getJSONObject(i);
                jobj.put("accounts", jobj.get("parametertitles"));
                jobj.put("spentthisperiod", jobj.get("current"));
                jsonArray.put(jobj);
            }

            /**
             * Empty Space*
             */
            jobj = new JSONObject();
            jobj.put("accounts", " ");
            jobj.put("spentthisperiod", " ");
            jsonArray.put(jobj);

            /**
             * Report 4 Name*
             */
            jobj = new JSONObject();
            jobj.put("accounts", "Receivables and Inventory : ");
            jobj.put("spentthisperiod", "Current");
            jsonArray.put(jobj);

            /**
             * Report 4 Data*
             */
            response = jsonParamObj.getJSONObject("ReceivablesInventory");
            jsonArrayEx = response.getJSONArray(Constants.RES_data);
            for (int i = 0; i < jsonArrayEx.length(); i++) {
                jobj = jsonArrayEx.getJSONObject(i);
                jobj.put("accounts", jobj.get("parametertitles"));
                jobj.put("spentthisperiod", jobj.get("current"));
                jsonArray.put(jobj);
            }

            /**
             * Empty Space*
             */
            jobj = new JSONObject();
            jobj.put("accounts", " ");
            jobj.put("spentthisperiod", " ");
            jsonArray.put(jobj);

            /**
             * Report 5 Name*
             */
            jobj = new JSONObject();
            jobj.put("accounts", "Fee Summary : ");
            jobj.put("spentthisperiod", "Amount");
            jsonArray.put(jobj);

            /**
             * Report 5 Data*
             */
            response = jsonParamObj.getJSONObject("FeeSummary");
            jsonArrayEx = response.getJSONArray(Constants.RES_data);
            for (int i = 0; i < jsonArrayEx.length(); i++) {
                jobj = jsonArrayEx.getJSONObject(i);
                jobj.put("accounts", jobj.get("parametertitles"));
                jobj.put("spentthisperiod", jobj.get("current"));
                jsonArray.put(jobj);
            }

            /**
             * Empty Space*
             */
            jobj = new JSONObject();
            jobj.put("accounts", " ");
            jobj.put("spentthisperiod", " ");
            jsonArray.put(jobj);

            /**
             * Report 6 Name*
             */
            jobj = new JSONObject();
            jobj.put("accounts", "Profit & Loss and Backlog : ");
            jobj.put("spentthisperiod", "YTD");
            jobj.put("spenttodate", "ITD");
            jsonArray.put(jobj);

            /**
             * Report 6 Data*
             */
            response = jsonParamObj.getJSONObject("ProfitLossBacklog");
            jsonArrayEx = response.getJSONArray(Constants.RES_data);
            for (int i = 0; i < jsonArrayEx.length(); i++) {
                jobj = jsonArrayEx.getJSONObject(i);
                jobj.put("accounts", jobj.get("parametertitles"));
                jobj.put("spentthisperiod", jobj.get("yeartodate"));
                jobj.put("spenttodate", jobj.get("inceptiontodate"));
                jsonArray.put(jobj);
            }
            result.put(Constants.RES_data, jsonArray);

        } catch (Exception ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    public JSONObject getCommonParametersForCustomLayout(JSONObject paramJobj, HttpServletRequest request) {
        try {
            ExtraCompanyPreferences extraCompanyPreferences = null;
            if (paramJobj.has(Constants.extraCompanyPreferences)) {
                extraCompanyPreferences = (ExtraCompanyPreferences) paramJobj.get(Constants.extraCompanyPreferences);
            } else {
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramJobj.optString(Constants.companyKey));
                if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                    extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                }
            }
            CompanyAccountPreferences preferences = null;
            if (paramJobj.has(Constants.preferences)) {
                preferences = (CompanyAccountPreferences) paramJobj.get(Constants.preferences);
            } else {
                KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.optString(Constants.companyKey));
                preferences = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            }
            paramJobj.put(Constants.extraCompanyPreferences, extraCompanyPreferences);
            paramJobj.put(Constants.preferences, preferences);
            paramJobj.put(Constants.basecurrencyid, sessionHandlerImpl.getCurrencyID(request));
        } catch (Exception ex) {
            Logger.getLogger(AccFinancialReportsServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return paramJobj;
    }
}
