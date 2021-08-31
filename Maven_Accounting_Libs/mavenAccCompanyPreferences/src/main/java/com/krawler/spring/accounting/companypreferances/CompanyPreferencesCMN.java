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
package com.krawler.spring.accounting.companypreferances;

import static com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants.*;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.UserActiveDaysDetails;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.YearLock;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author krawler
 */
public class CompanyPreferencesCMN {

    public static String getChequeNumberFormat(ChequeSequenceFormat chequeSequenceFormat) {
        String format = "";
        int numberofdigit = chequeSequenceFormat != null ? chequeSequenceFormat.getNumberOfDigits() : 0;
        /**
         * if block is use for Existing records because all exiting records are saved with null 
         * and system will work as before is was working for existing records .
         */
        if (chequeSequenceFormat != null && chequeSequenceFormat.getName() != null && !StringUtil.isNullOrEmpty(chequeSequenceFormat.getName())) {
            format = chequeSequenceFormat.getName();
        } else {
            for (int i = 0; i < numberofdigit; i++) {
                format += "0";
            }
        }
        return format;
    }

//Neeraj Dwivedi--Checking Lock period
    public static void checkLockPeriod(accCompanyPreferencesDAO accCompanyPreferencesObj, Map<String, Object> requestParams, Date date, boolean isimport) throws ServiceException, SessionExpiredException, ParseException, JSONException, AccountingException {
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            List<YearLock> list = new ArrayList<YearLock>();
            JSONArray jArr = new JSONArray();
            YearLock yearlockobj = null;
            Date finanDate = null, bookdate = null;
            boolean isOpeningBalanceOrder = (requestParams.containsKey("isOpeningBalanceOrder") && requestParams.get("isOpeningBalanceOrder") != null) ? Boolean.parseBoolean(requestParams.get("isOpeningBalanceOrder").toString()) : false;
            boolean isEdit=(requestParams.containsKey("isEdit") && requestParams.get("isEdit") != null) ? Boolean.parseBoolean(requestParams.get("isEdit").toString()) : false;
            boolean isLinkedTransaction=(requestParams.containsKey("isLinkedTransaction") && requestParams.get("isLinkedTransaction") != null) ? Boolean.parseBoolean(requestParams.get("isLinkedTransaction").toString()) : false;
            boolean isCopy=(requestParams.containsKey("isCopy") && requestParams.get("isCopy") != null) ? Boolean.parseBoolean(requestParams.get("isCopy").toString()) : false;
            boolean isBlockQuantity=(requestParams.containsKey("islockQuantity") && requestParams.get("islockQuantity") != null) ? Boolean.parseBoolean(requestParams.get("islockQuantity").toString()) : false;
            boolean isFromSO=(requestParams.containsKey(Constants.isFromSO) && requestParams.get(Constants.isFromSO) != null) ? Boolean.parseBoolean(requestParams.get(Constants.isFromSO).toString()) : false;
            boolean isFromPO=(requestParams.containsKey(Constants.isFromPO) && requestParams.get(Constants.isFromPO) != null) ? Boolean.parseBoolean(requestParams.get(Constants.isFromPO).toString()) : false;
            
            Date transactiondate = null;
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
            if (date != null) {
                transactiondate = date;
            }
            //            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, companyid);
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(ID, (String) requestParams.get("companyid"));
            KwlReturnObject kresult = accCompanyPreferencesObj.getCompanyPreferences(filterParams);
            CompanyAccountPreferences preferences = null;
            if (kresult.getEntityList().size() > 0) {
                preferences = (CompanyAccountPreferences) kresult.getEntityList().get(0);
            }
            if (preferences != null && preferences.getFinancialYearFrom() != null) {
                finanDate = preferences.getFirstFinancialYearFrom() != null ? preferences.getFirstFinancialYearFrom() : preferences.getFinancialYearFrom();
                bookdate = preferences.getBookBeginningFrom();
            }
            Calendar FinYearCal = Calendar.getInstance();
            FinYearCal.setTime(finanDate);
            int financialyear = FinYearCal.get(Calendar.YEAR);
            requestParams.put("yearid", financialyear);
            KwlReturnObject result = accCompanyPreferencesObj.getYearLock(requestParams);
            if (result.getEntityList().size() > 0) {
                yearlockobj = (YearLock) result.getEntityList().get(0);
            }

            if (preferences != null) {
                boolean checkimport = isimport;

                if (!checkimport) {//this if condition is used to check the transaction date
                    finanDate = removeTimefromDate(finanDate);
                    transactiondate = removeTimefromDate(transactiondate);
                    bookdate = removeTimefromDate(bookdate);
                    if (finanDate.after(transactiondate) && !isOpeningBalanceOrder) {//if date is less than first financial year date
                        throw new AccountingException("Transaction before First Financial Year Date are not allowed.You can update the First Financial Year Date and then proceed.");
                    } else if (bookdate.after(transactiondate) && !isOpeningBalanceOrder) {//if date lies between first financial year date & book beginning date
                        throw new AccountingException("Transaction before First Book Beginning Date are not allowed. You can update the First Book Beginning Date and then proceed.");
                    } else if (bookdate.before(transactiondate) || bookdate.equals(transactiondate)) {//checking the lock period if the date lies after book beginning date
                        Calendar caldate = Calendar.getInstance();
                        Date currentyear = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(caldate.getTime()));
                        int year = caldate.get(Calendar.YEAR);
                        requestParams.put("companyid", (String) requestParams.get("companyid"));
                        requestParams.put("CurrentFinancialYear", financialyear);
                        requestParams.put("CurrentFinanYear", finanDate);
                        requestParams.put("CurrentBookingYear", bookdate);
                        requestParams.put("CurrentYear", currentyear);
                        requestParams.put("CurrentServerYear", year);
                        KwlReturnObject yearresult = accCompanyPreferencesObj.getYearLockforPreferences(requestParams);
                        if (yearresult != null && yearresult.getEntityList().size() != 0) {
                            list = yearresult.getEntityList();
                        }

                        jArr = getYearLockJson(accCompanyPreferencesObj, requestParams, list);

                        if (jArr.length() > 0) {
                            for (int i = 0; i < jArr.length(); i++) {
                                JSONObject jsonobj = jArr.getJSONObject(i);
                                Date startdate = formatter.parse(jsonobj.getString("startdate"));
                                Date enddate = formatter.parse(jsonobj.getString("enddate"));
                                enddate = removeTimefromDate(enddate);
                                startdate = removeTimefromDate(startdate);
                                transactiondate = removeTimefromDate(transactiondate);

                                String islockboolean = jsonobj.getString("islock");
                                if (((transactiondate.after(startdate) && transactiondate.before(enddate)) || (transactiondate.equals(startdate)) || (transactiondate.equals(enddate)))&&(isFromSO||isFromPO)) {
                                    if (islockboolean.equals("true")) {
                                        /*
                                         Allows user to edit SO/PO of closed period if SO Block Quantity option is not checked and SO/PO are not linked to any transaction.
                                         */
                                        if (!isEdit || (isEdit && isCopy)) { //create and copy case 
                                            throw new AccountingException("Transaction belongs to locked period. You can reopen the books and then proceed.");
                                        } else if (isEdit && isBlockQuantity && !isCopy) { //edit case and having block quantity
                                            throw new AccountingException("You can not edit Transaction as Transaction belongs to locked period  and has blocked quantity . You can reopen the books and then proceed.");
                                        } else if (isEdit && isLinkedTransaction && !isCopy) { //edit case and is linked to another transaction 
                                            throw new AccountingException("You can not edit Transaction as Transaction belongs to locked period  and it is linked to other transaction. You can reopen the books and then proceed.");
                                        }
                                    } else {
                                        continue;
                                    }
                                }else if ((transactiondate.after(startdate) && transactiondate.before(enddate)) || (transactiondate.equals(startdate)) || (transactiondate.equals(enddate))) {
                                    if (islockboolean.equals("true")) {
                                        throw new AccountingException("Transaction belongs to locked period. You can reopen the books and then proceed.");
                                    } else {
                                        continue;
                                    }
                                 }
                            }
                        }
//                    else {//condition when the new company is created.
//                        throw new AccountingException("Please save the Account Preferences Settings first.");
//                    }
                    }

                } else {//checking for import section in customer and vendor management opening balance
                    Calendar caldate = Calendar.getInstance();
                    Date currentyear = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(caldate.getTime()));
                    int year = caldate.get(Calendar.YEAR);
                    requestParams.put("companyid", (String) requestParams.get("companyid"));
                    requestParams.put("CurrentFinancialYear", financialyear);
                    requestParams.put("CurrentFinanYear", finanDate);
                    requestParams.put("CurrentBookingYear", bookdate);
                    requestParams.put("CurrentYear", currentyear);
                    requestParams.put("CurrentServerYear", year);
                    KwlReturnObject yearresult = accCompanyPreferencesObj.getYearLockforPreferences(requestParams);
                    if (yearresult != null && yearresult.getEntityList().size() != 0) {
                        list = yearresult.getEntityList();
                    }

                    jArr = getYearLockJson(accCompanyPreferencesObj, requestParams, list);

                    if (jArr.length() > 0) {
                        for (int i = 0; i < jArr.length(); i++) {
                            JSONObject jsonobj = jArr.getJSONObject(i);
                            Date startdate = formatter.parse(jsonobj.getString("startdate"));
                            Date enddate = formatter.parse(jsonobj.getString("enddate"));
                            String islockboolean = jsonobj.getString("islock");
                            if (transactiondate.after(startdate) && transactiondate.before(enddate)) {
                                if (islockboolean.equals("true")) {
                                    throw new AccountingException("Transaction belongs to locked period. You can reopen the books and then proceed.");
                                } else {
                                    continue;
                                }
                            }
                        }
                    }
//                    else {//condition when the new company is created.
//                        throw new AccountingException("Please save the Account Preferences Settings first.");
//                    }
//                }
                }//end of checkimport
            }
        }
    }

    public static JSONArray getYearLockJson(accCompanyPreferencesDAO accCompanyPreferencesObj, Map<String, Object> requestParams, List<YearLock> list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(ID, (String) requestParams.get("companyid"));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(filterParams);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);
            Date CurrentFinanYear = (Date) requestParams.get("CurrentFinanYear");
            Date CurrentYear = (Date) requestParams.get("CurrentYear");
            int CurrentServerYear = 0, CurrentFinancialYear = 0, CurrentBookingYear = 0, CurrentFinancialMnth = 0, CurrentFinancialDate = 0;
            if (requestParams.get("CurrentFinanYear") != null) {
                Calendar FinYearCal = Calendar.getInstance();
                FinYearCal.setTime((Date) requestParams.get("CurrentFinanYear"));
                CurrentFinancialMnth = FinYearCal.get(Calendar.MONTH);
                CurrentFinancialDate = FinYearCal.get(Calendar.DAY_OF_MONTH);
            }
            if (requestParams.get("CurrentFinancialYear") != null) {
                CurrentFinancialYear = (Integer) requestParams.get("CurrentFinancialYear");
            }
            if (requestParams.get("CurrentServerYear") != null) {
                CurrentServerYear = (Integer) requestParams.get("CurrentServerYear");
            }
            if (requestParams.get("Backfiveyears") != null) {
                CurrentBookingYear = (Integer) requestParams.get("Backfiveyears");
            } else {
                if (requestParams.get("CurrentBookingYear") != null) {
                    Calendar BookYearCal = Calendar.getInstance();
                    BookYearCal.setTime((Date) requestParams.get("CurrentBookingYear"));
                    CurrentBookingYear = BookYearCal.get(Calendar.YEAR);
                }
            }
            //this function is used for grid data loading from database
            for (YearLock yl : list) {
//                    if (yl.getYearid() >= CurrentBookingYear) {
                JSONObject obj = new JSONObject();
                obj.put(ID, yl.getID());
                obj.put(NAME, yl.getYearid());
                obj.put(ISLOCK, yl.isIsLock());
                if (pref != null) {
                    Calendar startFinYearCal = Calendar.getInstance();
                    Calendar endFinYearCal = Calendar.getInstance();
                    startFinYearCal.setTime(pref.getFirstFinancialYearFrom() != null ? pref.getFirstFinancialYearFrom() : pref.getFinancialYearFrom());
                    startFinYearCal.set(Calendar.YEAR, yl.getYearid() - 1);
                    // TODO check FY date (ticket assigned by @Neeraj for edno)
                    
                    
                    endFinYearCal.setTime(CurrentFinanYear);
                    endFinYearCal.set(Calendar.YEAR, yl.getYearid() + 1);
                    endFinYearCal.set(Calendar.DATE, CurrentFinancialDate - 1);
                    endFinYearCal.set(Calendar.MONTH, CurrentFinancialMnth);
                    if ((authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(endFinYearCal.getTime()))).after(CurrentYear)) {
                        continue;
                    } else {
                        startFinYearCal.set(Calendar.YEAR, yl.getYearid());
                        obj.put(STARTDATE, formatter.format(startFinYearCal.getTime()));
                        obj.put(ENDDATE, formatter.format(endFinYearCal.getTime()));
                    }
                    obj.put("endYearId", endFinYearCal.get(Calendar.YEAR));/* Financial Year - End Date (Year) ERP-29582*/
                }
                YearEndCheckList yearEndCheckList = accCompanyPreferencesObj.getYearEndCheckList(yl.getID());
                if (yearEndCheckList != null) {
                    obj.put(Constants.CHECKLIST_ADJUSTMENT_FOR_TRANSACTIONS_COMPLETED, yearEndCheckList.isAdjustmentForTransactionCompleted());
                    obj.put(Constants.CHECKLIST_DOCUMENT_REVALUATION_COMPLETED, yearEndCheckList.isDocumentRevaluationCompleted());
                    obj.put(Constants.CHECKLIST_INVENTORY_ADJUSTMENT_COMPLETED, yearEndCheckList.isInventoryAdjustmentCompleted());
                    obj.put(Constants.CHECKLIST_ASSET_DEPRECIATION_COMPLETED, yearEndCheckList.isAssetDepreciationPosted());
                }
                jArr.put(obj);
//                    }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getYearLockJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public static void checkActiveDateRange(accCompanyPreferencesDAO accCompanyPreferencesObj, HttpServletRequest request, Date date) throws ServiceException, AccountingException, SessionExpiredException, ParseException,JSONException {
        JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
        checkActiveDateRange(accCompanyPreferencesObj,requestJobj,date);        
    }
    
    public static void checkActiveDateRange(accCompanyPreferencesDAO accCompanyPreferencesObj, JSONObject requestJobj, Date date) throws ServiceException, AccountingException, SessionExpiredException, ParseException {
        Date fromDate = null;
        Date toDate = null;
        boolean isFromSO=requestJobj.optBoolean(Constants.isFromSO,false);
        boolean isFromPO=requestJobj.optBoolean(Constants.isFromPO,false);;
        boolean isBlockQuantity = requestJobj.optBoolean("islockQuantity",false);
        boolean isLinkedTransaction= requestJobj.optBoolean("isLinkedTransaction",false);
        boolean isEdit= requestJobj.optBoolean("isEdit",false);
        boolean isCopy= requestJobj.optBoolean("copyInv",false);
        ExtraCompanyPreferences pref = null;
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("id", requestJobj.optString(Constants.companyKey,""));
        KwlReturnObject result = accCompanyPreferencesObj.getExtraCompanyPreferences(requestParams);
        if (result.getEntityList().size() > 0) {
            pref = (ExtraCompanyPreferences) result.getEntityList().get(0);
        }

        if (pref != null) {
            if (pref.getActiveFromDate() != null) {
                fromDate = pref.getActiveFromDate();
            }
            
            if (pref.getActiveToDate() != null) {
                toDate = pref.getActiveToDate();
            }
            
            if (fromDate != null && toDate != null) {
                if ((!(date.getTime() >= fromDate.getTime() && date.getTime() <= toDate.getTime()))&&(isFromSO||isFromPO)) {
                    /*
                     Allows user to edit SO/PO of closed period if SO Block Quantity option is not checked and SO/PO are not linked to any transaction.
                     */
                    if (!isEdit || (isEdit && isCopy)) { //create and copy case 
                        throw new AccountingException("Transaction cannot be completed. Date must belong to Active Date Range Period.");
                    } else if (isEdit && isBlockQuantity && !isCopy) {  //edit case and having block quantity
                        throw new AccountingException("You can not edit Transaction as it does not belong to active date range and has blocked quantity.");
                    } else if (isEdit && isLinkedTransaction && !isCopy) { //edit case and is linked to another transaction 
                        throw new AccountingException("You can not edit Transaction as it does not belong to active date range and it is linked to other transaction.");
                    }

                }else if (!(date.getTime() >= fromDate.getTime() && date.getTime() <= toDate.getTime())) {
                    
                    throw new AccountingException("Transaction cannot be completed. Date must belong to Active Date Range Period.");
                }
            }
        }
    }

    public static void checkUserActivePeriodRange(accCompanyPreferencesDAO accCompanyPreferencesObj, HashMap<String, Object> requestParams, Date activeStartDate) throws ServiceException, AccountingException, SessionExpiredException, ParseException {
        Calendar cldToday = Calendar.getInstance();
        cldToday.set(Calendar.HOUR_OF_DAY, 00);
        cldToday.set(Calendar.MINUTE, 00);
        cldToday.set(Calendar.SECOND, 00);
        cldToday.set(Calendar.MILLISECOND, 00);

        Calendar cldActiveStartDate = Calendar.getInstance();
        cldActiveStartDate.setTime(activeStartDate);
        cldActiveStartDate.set(Calendar.HOUR_OF_DAY, 00);
        cldActiveStartDate.set(Calendar.MINUTE, 00);
        cldActiveStartDate.set(Calendar.SECOND, 00);
        cldActiveStartDate.set(Calendar.MILLISECOND, 00);

        KwlReturnObject result = accCompanyPreferencesObj.getUserActiveDaysDetails(requestParams);
        if (result.getEntityList() != null && !result.getEntityList().isEmpty()) { // chek for User Specific rule
            UserActiveDaysDetails uadDetail = (UserActiveDaysDetails) result.getEntityList().get(0);
            int activeDays = uadDetail.getActiveDays();

            Date activeEndDate = activeStartDate;
            if (activeDays >= 1) {
                activeEndDate = DateUtils.addDays(activeStartDate, activeDays-1);
            }

            Calendar cldActiveEndDate = Calendar.getInstance();
            cldActiveEndDate.setTime(activeEndDate);
            cldActiveEndDate.set(Calendar.HOUR_OF_DAY, 00);
            cldActiveEndDate.set(Calendar.MINUTE, 00);
            cldActiveEndDate.set(Calendar.SECOND, 00);
            cldActiveEndDate.set(Calendar.MILLISECOND, 00);

            if (cldActiveStartDate != null && cldActiveEndDate != null) {
                if (!((cldToday.equals(cldActiveStartDate) || cldToday.after(cldActiveStartDate)) && (cldToday.equals(cldActiveEndDate) || cldToday.before(cldActiveEndDate))) || activeDays == 0) {
                    throw new AccountingException("The Operation cannot be completed due to 'Active Days Period' User restrictions.");
                }
            }
        } else { // if User Specific rule not exist then check for All User rule
            requestParams.put("isAllUser", true);
            result = accCompanyPreferencesObj.getUserActiveDaysDetails(requestParams);

            if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                UserActiveDaysDetails uadDetail = (UserActiveDaysDetails) result.getEntityList().get(0);
                int activeDays = uadDetail.getActiveDays();

                Date activeEndDate = activeStartDate;
                if (activeDays >= 1) {
                    activeEndDate = DateUtils.addDays(activeStartDate, activeDays - 1);
                }

                Calendar cldActiveEndDate = Calendar.getInstance();
                cldActiveEndDate.setTime(activeEndDate);
                cldActiveEndDate.set(Calendar.HOUR_OF_DAY, 00);
                cldActiveEndDate.set(Calendar.MINUTE, 00);
                cldActiveEndDate.set(Calendar.SECOND, 00);
                cldActiveEndDate.set(Calendar.MILLISECOND, 00);

                if (activeStartDate != null && activeEndDate != null) {
                    if (!((cldToday.equals(cldActiveStartDate) || cldToday.after(cldActiveStartDate)) && (cldToday.equals(cldActiveEndDate) || cldToday.before(cldActiveEndDate))) || activeDays == 0) {
                        throw new AccountingException("The Operation cannot be completed due to 'Active Days Period' User restrictions.");
                    }
                }
            }
        }
    }

    public static Date removeTimefromDate(Date sampledate) { //removing time from date-Neeraj D
        Calendar c = Calendar.getInstance();
        c.setTime(sampledate);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        sampledate = c.getTime();
        return sampledate;
    }
}
