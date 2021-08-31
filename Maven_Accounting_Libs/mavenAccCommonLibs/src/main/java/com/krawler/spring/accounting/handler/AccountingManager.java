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
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.customDesign.InventoryCustomDesignerConstants;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.ObjectNotFoundException;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccountingManager {

    private static companyDetailsDAO companyDetailsDAOObj;
    public static String generateNextAutoNumber(String pattern, String strCurrent) {
        StringBuffer strNext = new StringBuffer(pattern);
        int x = 0;
        if (strCurrent != null && pattern.length() == strCurrent.length()) {
            for (x = 0; x < pattern.length(); x++) {
                if (pattern.charAt(x) == '0' && (strCurrent.charAt(x) < '0' || strCurrent.charAt(x) > '9')) {
                    break;
                }
            }
        }
        if (x == pattern.length()) {
            strNext = new StringBuffer(strCurrent);
        }
        int carry = 1;
        for (int i = strNext.length() - 1; i >= 0; i--) {
            if (pattern.charAt(i) < '0' || pattern.charAt(i) > '9') {
                continue;
            }
            int sum = (strNext.charAt(i) - '0') + carry;
            strNext.setCharAt(i, (char) (sum % 10 + '0'));
            carry = sum / 10;
        }
        return strNext.toString();
    }

    /**
     *
     * @param pattern
     * @param strCurrent
     * @param ignoreLeadingZero - if true means without leading zero.
     * @param startfrom
     * @return
     */
    public static String generateNextAutoNumber(String pattern, String strCurrent, boolean ignoreLeadingZero, int startfrom) {
        StringBuffer strNext = new StringBuffer(pattern);
        if (!(ignoreLeadingZero && pattern.matches("[0-9]\\d*"))) {// controll will go inside - if pattern like VI000000 or etc OR pattern like 000000 and ignoreleadingzero false.
            int x = 0;
            if (strCurrent != null && pattern.length() == strCurrent.length()) {
                for (x = 0; x < pattern.length(); x++) {
                    if (pattern.charAt(x) == '0' && (strCurrent.charAt(x) < '0' || strCurrent.charAt(x) > '9')) {
                        break;
                    }
                }
            }
            if (x == pattern.length()) {
                strNext = new StringBuffer(strCurrent);
            }
            int carry = 1;
            for (int i = strNext.length() - 1; i >= 0; i--) {
                if (pattern.charAt(i) < '0' || pattern.charAt(i) > '9') {
                    continue;
                }
                int sum = (strNext.charAt(i) - '0') + carry;
                strNext.setCharAt(i, (char) (sum % 10 + '0'));
                carry = sum / 10;
            }
            //**** following code is to handle generated code regards to handle start from value.
            String nextAutoNumber = strNext.toString();
            if (!ignoreLeadingZero && nextAutoNumber.matches("[0-9]\\d*")) {//This will come from what is set against format...Whether leading zero need 0 remove or not.

                if (Integer.valueOf(nextAutoNumber) < startfrom) {
                    Double num = Double.parseDouble(startfrom + "");
                    int count = 0;
                    while (num > 0) {
                        num = num / 10;
                        num = Math.floor(num);
                        count++;
                    }
                    if (count < nextAutoNumber.length()) {
                        nextAutoNumber = nextAutoNumber.substring(0, nextAutoNumber.length() - count);
                        nextAutoNumber += startfrom;
                    } else {
                        nextAutoNumber = startfrom + "";
                    }
                }
            }

            strNext = new StringBuffer(nextAutoNumber);

            //****
        } else {
            if (!StringUtil.isNullOrEmpty(strCurrent)) {
                strNext = new StringBuffer(String.valueOf(Integer.parseInt(strCurrent) + 1));
            } else {
                strNext = new StringBuffer(String.valueOf(startfrom));
            }
        }
        return strNext.toString();
    }
    /**
    * This Method Uses HttpServletRequest and this method
    * was called from service layer forcing to send request
    * object to Service Layer
    * @deprecated 
    */
    public static HashMap<String, Object> getGlobalParams(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put(Constants.companyKey, getCompanyidFromRequest(request));
        requestMap.put(Constants.globalCurrencyKey, getGlobalCurrencyidFromRequest(request));
//        requestMap.put(Constants.df, authHandler.getDateFormatter(request));  //This format belongs to our global date format[i.e.new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa")]
//        requestMap.put(Constants.userdf, authHandler.getUserDateFormatter(request)); //This format holds users date format.
        requestMap.put(Constants.df, authHandler.getDateOnlyFormat(request));
        requestMap.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(request));
        return requestMap;
    }
        
    public static HashMap<String, Object> getGlobalParamsJson(JSONObject jobj) throws JSONException, ServiceException, SessionExpiredException {
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        String companyId = getCompanyid(jobj);
        requestMap.put(Constants.companyKey, companyId);
        jobj.put(Constants.companyKey, companyId);
        requestMap.put(Constants.globalCurrencyKey, getGlobalCurrencyid(jobj));
        requestMap.put(Constants.df, authHandler.getDateOnlyFormat());
        requestMap.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(jobj));
        return requestMap;
    }
    
    public static HashMap<String, Object> getGlobalParams(JSONObject requestJobj) throws SessionExpiredException {
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey,""));
        requestMap.put(Constants.globalCurrencyKey, requestJobj.optString(Constants.globalCurrencyKey,""));
        requestMap.put(Constants.df, authHandler.getDateOnlyFormat());
        requestMap.put(Constants.userdf, requestJobj.optString(Constants.userdateformat,""));
        return requestMap;
    }

    public static String getCompanyidFromRequest(HttpServletRequest request) throws SessionExpiredException {
        String companyid = request.getAttribute(Constants.companyKey) != null ? request.getAttribute(Constants.companyKey).toString() : sessionHandlerImpl.getCompanyid(request);
        return companyid;
    }

    public static String getGlobalCurrencyidFromRequest(HttpServletRequest request) throws SessionExpiredException {
        String gcurrencyid = request.getParameter(Constants.globalCurrencyKey) != null ? request.getParameter(Constants.globalCurrencyKey) : sessionHandlerImpl.getCurrencyID(request);
        return gcurrencyid;
    }


    public static String getCompanyid(JSONObject jobj) throws SessionExpiredException, JSONException, ServiceException {
        String companyid = (jobj.has(Constants.companyKey) && jobj.getString(Constants.companyKey)!= null) ? jobj.getString(Constants.companyKey) : (jobj.has("cdomain") ? companyDetailsDAOObj.getCompanyid(jobj.getString("cdomain")) : null);
        return companyid;
    }

    public static String getGlobalCurrencyid(JSONObject jobj) throws SessionExpiredException, JSONException, ServiceException {
        if(jobj.has(Constants.globalCurrencyKey) && jobj.getString(Constants.globalCurrencyKey)!= null){
            return jobj.getString(Constants.globalCurrencyKey);
        }
        else{
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", jobj.getString(Constants.companyKey));
            ArrayList filter_names = new ArrayList();
            filter_names.add("companyID");
            KwlReturnObject response = companyDetailsDAOObj.getCompanyInformation(requestParams, filter_names, null);
            if(response != null && response.getEntityList() != null && !response.getEntityList().isEmpty()){
                Company company = (Company) response.getEntityList().get(0);
                return company.getCurrency()!= null ? company.getCurrency().getCurrencyID() : null;
            }
            return null;
        }
    }

    public static String getFilterInString(String id) {
        String[] ids = (String[]) id.split(",");
        String companyFilter = " ( ";
        for (int i = 0; i < ids.length; i++) {
            companyFilter += "'" + ids[i] + "',";
        }
        companyFilter = companyFilter.substring(0, companyFilter.length() - 1) + ") ";
        return companyFilter;
    }
    public static String getFilterInNumber(String id) {
        String[] ids = (String[]) id.split(",");
        String companyFilter = " ( ";
        for (int i = 0; i < ids.length; i++) {
            companyFilter +=  ids[i] + ",";
        }
        companyFilter = companyFilter.substring(0, companyFilter.length() - 1) + ") ";
        return companyFilter;
    }

    public static boolean isCompanyAdmin(User user) {
        if (user != null) {
            return user.getRoleID().equals(Role.COMPANY_ADMIN);
        }
        return false;
    }

    public static Date resetTimeField(Date date) {
        Date returnDate = date;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 12);//Reset Time field
            cal.set(Calendar.MINUTE, 00);
            cal.set(Calendar.SECOND, 00);
            returnDate = cal.getTime();
        } catch (Exception ex) {
        }
        return returnDate;
    }


    /*
     * Function used reseting time field in dates used filtering the report
     * having StartDate & EndDate as filters e.g. For StartDate ==>
     * setFilterTime("20101-12-01 02:34:56", true) ===> "20101-12-01 00:00:00"
     * For EndDate ====> setFilterTime("20101-12-01 02:34:56", false) ==>
     * "20101-12-01 23:59:59"
     *
     */
    public static Date setFilterTime(Date date, boolean isStartDate) {
        Date returnDate = date;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, isStartDate ? 00 : 23);
            cal.set(Calendar.MINUTE, isStartDate ? 00 : 59);
            cal.set(Calendar.SECOND, isStartDate ? 00 : 59);
            returnDate = cal.getTime();
        } catch (Exception ex) {
        }
        return returnDate;
    }

       public static void setCustomColumnValues(AccCustomData customData, HashMap<String, Integer> fieldMap, Map<String, String> replaceFieldMap,
            Map<String, Object> variableMap) {
        if (customData != null) {
            for (Map.Entry<String, Integer> field : fieldMap.entrySet()) {
                Integer colnumber = field.getValue();
                if (colnumber > 0) { // colnumber will be 0 if key is part of reference map
                    Integer isref = fieldMap.get(field.getKey() + "#" + colnumber);// added '#' while creating map collection for custom fields.
                    // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
                    String coldata = null;
                    if (isref != null) {
                        try {
                            if (!StringUtil.isNullOrEmptyWithTrim(customData.getCol(colnumber))) {
                                coldata = customData.getCol(colnumber);
                            }
                            String coldataVal = null;
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                if (coldata.length() > 1) {
                                    if (isref == 1) {
//                                        coldataVal = fieldDataManagercntrl.getMultiSelectColData(coldata);
//                                        if(isExport) {
//                                            coldata = coldataVal;
//                                        }
//                                        variablesMapForCustomData.put(field.getKey(), coldataVal);
                                    } else if (isref == 0) {
//                                        coldataVal = customData.getRefCol(colnumber);
//                                        if(isExport) {
//                                            coldata = coldataVal;
//                                        }
//                                        variablesMapForCustomData.put(field.getKey(), coldataVal);
                                    } else if (isref == 3) {
//                                        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
//                                        long milliSeconds= Long.parseLong(coldata);
//                                        coldata = df2.format(milliSeconds);
                                    }
                                }
                                variableMap.put(field.getKey(), coldata);
//                                try {
//                                    variableMapForFormula.put(replaceFieldMap.get(field.getKey()), Double.parseDouble(coldata));
//                                } catch (Exception ex) {
//                                    variableMapForFormula.put(replaceFieldMap.get(field.getKey()), 0);
//                                }
                            }
                        } catch (IllegalArgumentException ex) {
                            ex.printStackTrace();
                        } catch (ObjectNotFoundException ex) {
                            ex.printStackTrace();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    replaceFieldMap.put("col" + colnumber, coldata);//document designer
                }
            }
        }
    }
    
    public static void setCustColValuesForExport(JSONObject paramJobj,AccCustomData customData, HashMap<String, Integer> fieldMap, Map<String, String> replaceFieldMap,Map<String, Object> variableMap) {
        for (Map.Entry<String, Integer> field : fieldMap.entrySet()) {
            Integer colnumber = field.getValue();
            if (colnumber > 0) { // colnumber will be 0 if key is part of reference map
                Integer isref = fieldMap.get(field.getKey() + "#" + colnumber);// added '#' while creating map collection for custom fields.
                // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
                String coldata = "";
                if (isref != null) {
                    try {
                        if(customData!=null){
                        coldata = customData.getCol(colnumber);
                        String coldataVal = null;
                         DateFormat defaultDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                         Date dateFromDB = null;
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            if (coldata.length() > 1) {
                                if (isref == 1) {
//                                        coldataVal = fieldDataManagercntrl.getMultiSelectColData(coldata);
//                                        if(isExport) {
//                                            coldata = coldataVal;
//                                        }
//                                        variablesMapForCustomData.put(field.getKey(), coldataVal);
                                } else if (isref == 0) {
//                                        coldataVal = customData.getRefCol(colnumber);
//                                        if(isExport) {
//                                            coldata = coldataVal;
//                                        }
//                                        variablesMapForCustomData.put(field.getKey(), coldataVal);
                                } else if (isref == 3) {
                                    DateFormat df = authHandler.getUserDateFormatterJson(paramJobj);//User Date Formatter
                                    if (df != null) {
                                        try {
                                            dateFromDB = defaultDateFormat.parse(coldata);
                                            coldata = df.format(dateFromDB);
                                        } catch (ParseException p) {
                                            
                                        }
                                        //If User date format is provided, date is formated in that format
                                    }
                                }
                            }
                            variableMap.put(field.getKey(), coldata);
                        }
                      }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (ObjectNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                replaceFieldMap.put("col" + colnumber, coldata);
            }
        }
    }

    public static String[] getMergedMailIds(String[] arr1, String[] arr2) {
        String[] emails = {};
        try {
            List emailList1 = Arrays.asList(arr1);
            List emailList2 = Arrays.asList(arr2);
            HashSet<String> hashSet = new HashSet<String>(emailList1);
            hashSet.addAll(emailList2);
            emails = hashSet.toArray(emails);

        } catch (Exception ex) {
            Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return emails;
    }

    public static HashMap<String, Object> getIBGBankDetailParams(HttpServletRequest request) {
        HashMap<String, Object> ibgBankDetailMap = new HashMap<String, Object>();
        try {
            ibgBankDetailMap.put(Constants.IBG_BANK, Integer.parseInt(request.getParameter(Constants.IBG_BANK)));
            ibgBankDetailMap.put(Constants.BANK_CODE, request.getParameter(Constants.BANK_CODE));
            ibgBankDetailMap.put(Constants.BRANCH_CODE, request.getParameter(Constants.BRANCH_CODE));
            ibgBankDetailMap.put(Constants.ACCOUNT_NUMBER, request.getParameter(Constants.ACCOUNT_NUMBER));
            ibgBankDetailMap.put(Constants.ACCOUNT_NAME, request.getParameter(Constants.ACCOUNT_NAME));
            ibgBankDetailMap.put(Constants.SENDERS_COMPANYID, request.getParameter(Constants.SENDERS_COMPANYID));
            ibgBankDetailMap.put(Constants.BANK_DAILY_LIMIT, Double.parseDouble(request.getParameter(Constants.BANK_DAILY_LIMIT)));
        } catch (Exception ex) {
            Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ibgBankDetailMap;
    }
    
    public static HashMap<String, Object> getCIMBBankDetailParams(HttpServletRequest request) {
        HashMap<String, Object> cimbBankDetailMap = new HashMap<String, Object>();
        try {
            cimbBankDetailMap.put(Constants.SERVICE_CODE, request.getParameter(Constants.SERVICE_CODE));
            cimbBankDetailMap.put(Constants.BANK_Account_Number, request.getParameter(Constants.BANK_Account_Number));
            cimbBankDetailMap.put(Constants.ORDERER_NAME, request.getParameter(Constants.ORDERER_NAME));
            cimbBankDetailMap.put(Constants.SETTELEMENT_MODE, Integer.parseInt(request.getParameter(Constants.SETTELEMENT_MODE)));
            cimbBankDetailMap.put(Constants.POSTING_INDICATOR, Integer.parseInt(request.getParameter(Constants.POSTING_INDICATOR)));
            cimbBankDetailMap.put(Constants.CIMB_BANK_DETAIL_ID, request.getParameter(Constants.CIMB_BANK_DETAIL_ID));
        } catch (Exception ex) {
            Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cimbBankDetailMap;
    }
    
    /*
     * Method for getting common parameters for UOB bank
     */
    public static HashMap<String, Object> getUOBBankDetailParams(HttpServletRequest request) {
        HashMap<String, Object> uobBankDetailMap = new HashMap<String, Object>();
        try {
            uobBankDetailMap.put(Constants.UOB_Originating_BIC_Code, request.getParameter(Constants.UOB_Originating_BIC_Code));
            uobBankDetailMap.put(Constants.UOB_Currency_Code, request.getParameter(Constants.UOB_Currency_Code));
            uobBankDetailMap.put(Constants.UOB_Originating_Account_Number, request.getParameter(Constants.UOB_Originating_Account_Number));
            uobBankDetailMap.put(Constants.UOB_Originating_Account_Name, request.getParameter(Constants.UOB_Originating_Account_Name));
            uobBankDetailMap.put(Constants.UOB_Ultimate_Originating_Customer, request.getParameter(Constants.UOB_Ultimate_Originating_Customer));
            uobBankDetailMap.put(Constants.UOB_CompanyID, request.getParameter(Constants.UOB_CompanyID));
        } catch (Exception ex) {
            Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return uobBankDetailMap;
    }
    public static HashMap<String, Object> getOCBCBankDetailsParams(HttpServletRequest request) throws ServiceException {
        HashMap<String, Object> ocbcBankdetailsMap = new HashMap<>();
        try {
            ocbcBankdetailsMap.put(Constants.OCBC_BANK_DETAIL_ID, request.getParameter(Constants.OCBC_BANK_DETAIL_ID));
            ocbcBankdetailsMap.put(Constants.OCBC_OriginatingBankCode, request.getParameter(Constants.OCBC_OriginatingBankCode));
            ocbcBankdetailsMap.put(Constants.OCBC_AccountNumber, request.getParameter(Constants.OCBC_AccountNumber));
            ocbcBankdetailsMap.put(Constants.OCBC_ReferenceNumber, request.getParameter(Constants.OCBC_ReferenceNumber));
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccountingManager.getOCBCBankDetailsParams:" + ex.getMessage(), ex);
        }
        return ocbcBankdetailsMap;
    }

    public static JSONObject getAddressJsonObject(AddressDetails addressDetails) {
        JSONObject object = new JSONObject();
        if (addressDetails != null) {
            try {
                object.put("aliasName", addressDetails.getAliasName());
                object.put("address", addressDetails.getAddress());
                object.put("county", addressDetails.getCounty());
                object.put("city", addressDetails.getCity());
                object.put("state", addressDetails.getState());
                object.put("country", addressDetails.getCountry());
                object.put("postalCode", addressDetails.getPostalCode());
                object.put("phone", addressDetails.getPhone());
                object.put("mobileNumber", addressDetails.getMobileNumber());
                object.put("fax", addressDetails.getFax());
                object.put("emailID", addressDetails.getEmailID());
                object.put("contactPerson", addressDetails.getContactPerson());
                object.put("recipientName", addressDetails.getRecipientName());
                object.put("contactPersonNumber", addressDetails.getContactPersonNumber()); 
                object.put("contactPersonDesignation", addressDetails.getContactPersonDesignation()); 
                object.put("website", addressDetails.getWebsite()); 
                object.put("isDefaultAddress", addressDetails.isIsDefaultAddress()); 
            } catch (Exception ex) {
                Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return object;
    }
    
    /*
     * Building Message for Audit Trial
     */

    public static String BuildAuditTrialMessage(Map<String, Object> newrecord, Map<String, Object> oldrecord, int moduleid, Map<String, Object> newAuditTrailKey) {
        String smsAudit = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fields = "";
        String oldvalues = "";
        String newvalues = "";
        try {
         
            for (String key : oldrecord.keySet()) {
                //if value are not same 
                if (newrecord.containsKey(key))
                {
                    if (!oldrecord.get(key).equals(newrecord.get(key))) {
                        fields = (String) newAuditTrailKey.get(key);
                        if (!fields.equalsIgnoreCase("Due Date") || (moduleid==Constants.Acc_Consignment_GoodsReceipt_ModuleId ||moduleid==Constants.Acc_ConsignmentInvoice_ModuleId ||moduleid==Constants.Acc_Invoice_ModuleId || moduleid==Constants.Acc_Vendor_Invoice_ModuleId) ||moduleid==Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId || moduleid==Constants.Acc_FixedAssets_DisposalInvoice_ModuleId ||moduleid==Constants.LEASE_INVOICE_MODULEID ||moduleid==Constants.POS_MODULEID) {
                            if (oldrecord.get(key) == "" || oldrecord.get(key) == null) {
                                oldvalues = "' '";
                            } else {
                                oldvalues = oldrecord.get(key).toString();
                            }
                            if (newrecord.get(key) == null || newrecord.get(key) == "") {
                                newvalues = "' '";
                            } else {
                                if (key.equals("advanceDate")) {
                                    Date newAdvanceDate = (Date) newrecord.get(key);
                                    newvalues = sdf.format(newAdvanceDate);
                                } else {
                                    newvalues = newrecord.get(key).toString();
                                }
                            }
                            smsAudit += "<br> [ Field : " + fields + " : Old Value = " + oldvalues + " , New Value = " + newvalues.toString() + " ] <br>";
                        }
                    }

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return smsAudit;
    } 
    public static HashMap<String, Object> getSyncAllRequestParams(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("modName", request.getParameter("modName"));
        requestParams.put("moduleName", request.getParameter("moduleName"));
        requestParams.put("delimiterType", request.getParameter("delimiterType"));
        requestParams.put("filename", request.getParameter("filename"));
        requestParams.put("resjson", request.getParameter("resjson"));
        requestParams.put("dateFormat", request.getParameter("dateFormat"));
        requestParams.put("masterPreference", request.getParameter("masterPreference"));
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("userid", sessionHandlerImpl.getUserid(request));
        requestParams.put("userfullName", sessionHandlerImpl.getUserFullName(request));
        requestParams.put("createdby", sessionHandlerImpl.getUserid(request));
        requestParams.put("modifiedby", sessionHandlerImpl.getUserid(request));
        requestParams.put("reqHeader", request.getHeader("x-real-ip"));
        requestParams.put("remoteAddress", request.getRemoteAddr());
        
        requestParams.put(Constants.currencyKey, sessionHandlerImpl.getCurrencyID(request));
        requestParams.put("requestUtil", RequestContextUtils.getLocale(request));
        requestParams.put(Constants.companyKey, getCompanyidFromRequest(request));
        requestParams.put(Constants.globalCurrencyKey, getGlobalCurrencyidFromRequest(request));
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));  //This format belongs to our global date format[i.e.new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa")]
        requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(request)); //This format holds users date format.
        requestParams.put("fromLinkCombo", request.getParameter("fromLinkCombo")); 
        requestParams.put("linkNumber", request.getParameter("linkNumber")); 
        requestParams.put("addressParamsMap", AccountingAddressManager.getAddressParams(request,false)); 
        

        return requestParams;
    }
    public static HashMap<String, Object> getEmailNotificationParams(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("userid", sessionHandlerImpl.getUserid(request));
        requestParams.put("userfullName", sessionHandlerImpl.getUserFullName(request));
        requestParams.put("usermailid", sessionHandlerImpl.getUserMailId(request));
        return requestParams;
    }
    
    public static HashMap<String, Object> getEmailNotificationParamsJson(JSONObject jobj) throws JSONException, ServiceException, SessionExpiredException {
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        String companyId = getCompanyid(jobj);
        requestMap.put(Constants.companyKey, companyId);
        jobj.put(Constants.companyKey, companyId);
        requestMap.put(Constants.useridKey, jobj.getString(Constants.useridKey));
        requestMap.put("userfullName", jobj.optString(Constants.userfullname,""));
        requestMap.put("usermailid",jobj.optString(Constants.usermailId,""));
        return requestMap;
    }
    
    public static boolean checkForProductAndProductDiscountRule(JSONArray productDiscountMapList, int appliedUpon, String rule, String discountRule){
        boolean sendForApproval=false;
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        if (productDiscountMapList != null) {
            try{
                for (int cnt = 0; cnt < productDiscountMapList.length(); cnt++) {
                    String productId = "";
                    String discountAmount = "";
                    JSONObject jObj = (JSONObject) productDiscountMapList.get(cnt);
                    productId = jObj.get("productId").toString();
                    if (rule.contains(productId)) {
                        if (appliedUpon == Constants.Specific_Products_Discount) {
                            discountAmount = jObj.get("discountAmount").toString();
                            String discountRuleForEval = discountRule.replaceAll("[$$]+", discountAmount);
                            if (Boolean.parseBoolean(engine.eval(discountRuleForEval).toString())) {
                                sendForApproval = true;
                                break;
                            }
                        } else {
                            sendForApproval = true;
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sendForApproval;
    }

    /*
     * get Extra Fields
     */
    public static HashMap<String, String> getExtraFieldsForModule(int moduleid) {
        HashMap<String, String> map = null;

        switch (moduleid) {
            case Constants.Acc_Invoice_ModuleId: // Invoice
                map = CustomDesignerConstants.CustomDesignInvoiceExtraFieldsMap;
                break;
            case Constants.Acc_Vendor_Invoice_ModuleId: // Vendor Invoice 
                map = CustomDesignerConstants.CustomDesignVendorInvoiceExtraFieldsMap;
                break;
            case Constants.Acc_Debit_Note_ModuleId://DebitNote
                map = CustomDesignerConstants.CustomDesignDebitNoteExtraFieldsMap;
                break;
            case Constants.Acc_Credit_Note_ModuleId://CreditNOte
                map = CustomDesignerConstants.CustomDesignCreditNoteExtraFieldsMap;
                break;
            case Constants.Acc_Make_Payment_ModuleId://14 - Make Payment;
                map = CustomDesignerConstants.CustomDesignMakePaymentNewExtraFieldsMap;
                break;
            case Constants.Acc_Receive_Payment_ModuleId://16 - Receive Payment
                map = CustomDesignerConstants.CustomDesignReceivePaymentNewExtraFieldsMap;
                break;
            case Constants.Acc_Purchase_Order_ModuleId: // Purchase Order
                map = CustomDesignerConstants.CustomDesignPurchaseOrderExtraFieldsMap;
                break;
            case Constants.Acc_Sales_Order_ModuleId: // Sales Order
                map = CustomDesignerConstants.CustomDesignSalesOrderExtraFieldsMap;
                break;
            case Constants.Acc_Customer_Quotation_ModuleId: // Custom Quotation
                map = CustomDesignerConstants.CustomDesignCustomerQuotationExtraFieldsMap;
                break;
            case Constants.Acc_Vendor_Quotation_ModuleId: // Vendor Quotation
                map = CustomDesignerConstants.CustomDesignVendorQuotationExtraFieldsMap;
                break;
            case Constants.Acc_Delivery_Order_ModuleId: //Delivery Order
                map = CustomDesignerConstants.CustomDesignDOExtraFieldsMap;
                break;
            case Constants.Acc_Goods_Receipt_ModuleId: //Good Receipt Order
                map = CustomDesignerConstants.CustomDesignGROExtraFieldsMap;
                break;
            case Constants.Acc_Sales_Return_ModuleId://Sales Return
                map = CustomDesignerConstants.CustomDesignSalesReturnExtraFieldsMap;
                break;
            case Constants.Acc_ConsignmentRequest_ModuleId: // Sales Order
                map = CustomDesignerConstants.CustomDesignSalesOrderExtraFieldsMap;
                break;
            case Constants.Acc_ConsignmentDeliveryOrder_ModuleId: // Delivery Order
                map = CustomDesignerConstants.CustomDesignDOExtraFieldsMap;
                break;
            case Constants.Acc_ConsignmentInvoice_ModuleId: // Consignment Invoice
                map = CustomDesignerConstants.CustomDesignInvoiceExtraFieldsMap;
                break;
            case Constants.Acc_ConsignmentSalesReturn_ModuleId: // Consignment Sales Return
                map = CustomDesignerConstants.CustomDesignSalesReturnExtraFieldsMap;
                break;
            case Constants.Acc_Purchase_Return_ModuleId: // Purchase Return
                map = CustomDesignerConstants.CustomDesignPurchaseReturnExtraFieldsMap;
                break;
            case Constants.Acc_Stock_Request_ModuleId: // Consignment Stock Request
                map = CustomDesignerConstants.CustomDesignStockRequestExtraFieldsMap;
                break;
            case Constants.Inventory_ModuleId: // Consignment Stock Issue
                map = CustomDesignerConstants.CustomDesignStockRequestExtraFieldsMap;
                break;
            case Constants.Acc_Stock_Adjustment_ModuleId: // Consignment Stock Adjustment
                map = CustomDesignerConstants.CustomDesignStockAdjustmentExtraFieldsMap;
                break;
            case Constants.Acc_InterStore_ModuleId: // Consignment Stock Adjustment
                map = CustomDesignerConstants.CustomDesignInterStoreTransferExtraFieldsMap;
                break;
            case Constants.Acc_InterLocation_ModuleId: // Consignment Stock Adjustment
                map = CustomDesignerConstants.CustomDesignInterLocationstockTransferExtraFieldsMap;
                break;
            case Constants.Acc_RFQ_ModuleId: // Request For Quotation
                map = CustomDesignerConstants.CustomDesignRequestForQuotationExtraFieldsMap;
                break;
            case Constants.Acc_Purchase_Requisition_ModuleId: // Purchase Requisition //ERP-19851
                map = CustomDesignerConstants.CustomDesignPurchaseRequisitionExtraFieldsMap;
                break;
            case Constants.Acc_Stock_Repair_Report_ModuleId: // Inventory Stock Repair Module
                map = InventoryCustomDesignerConstants.CustomDesignStockRepairExtraFieldsMap;
                break;
        }
        return map;
    }

    /*
     * Default Headers---fieldid
     */
    public static HashMap getDefaultHeaderName_XtypeNew(CustomDesignDAO customDesignDAOObj, JSONArray jArr) throws JSONException, ServiceException {

        String fieldIds = "";
        KwlReturnObject result = null;
        List list = null;
        Object[] rows = null;
        for (int cnt = 0; cnt < jArr.length(); cnt++) { // Iterate over rows
            JSONObject gf_jObj = jArr.getJSONObject(cnt);
            if (!StringUtil.isNullOrEmpty(gf_jObj.optString("fieldid", ""))) {
                fieldIds += "'" + gf_jObj.getString("fieldid") + "',";
            }
        }

        HashMap<String, Integer> default_headers = new HashMap();
        if (!StringUtil.isNullOrEmpty(fieldIds)) {
            fieldIds = fieldIds.substring(0, fieldIds.length() - 1);
            /*
             * Below function called to fetch defaultHeader name which is used
             * for placeholder - select defaultHeader, dummyvalue, xtype
             */
            result = customDesignDAOObj.getDummyValue(fieldIds);
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                rows = (Object[]) list.get(cnt);
                default_headers.put(rows[0].toString(), Integer.parseInt(rows[2].toString()));
            }
        }
        return default_headers;
    }
     
    /*
     * fetching details of other current user details
     */
    public static JSONObject fetchCurrentUserandCompanyDetails(CustomDesignDAO customDesignDAOObj,JSONObject jresult, int moduleid,boolean userorcompanyflag,String companyid) {
        try {
            JSONObject userorcompanyDetailsjSONObject = new JSONObject();
            if (userorcompanyflag) {
                HashMap<String, String> fieldMap = CustomDesignerConstants.CurrentUserDetailsMap;
                if (fieldMap.size() > 0) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("fieldid", "NA");
                    jSONObject.put("id", "NA");
                    jSONObject.put("label", "---------------------------[User Details]------------------");
                    jSONObject.put("xtype", "");
                    userorcompanyDetailsjSONObject.append("data", jSONObject);
                }

                for (Map.Entry<String, String> fieldentry : fieldMap.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(fieldentry.getValue());
                    staticamountInfo.put("fieldid", fieldentry.getKey());
                    staticamountInfo.put("id", fieldentry.getKey());
                    staticamountInfo.put("label", staticamountInfo.get("label"));
                    staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                    userorcompanyDetailsjSONObject.append("data", staticamountInfo);
                }
                    jresult.append("data", userorcompanyDetailsjSONObject);
                    
            } else {
                if (moduleid == StringUtil.getInteger(CustomDesignerConstants.CompanyModuleid)) {
                    KwlReturnObject result = customDesignDAOObj.getDefaultHeaders(String.valueOf(moduleid), companyid);
                    List list = result.getEntityList();
                    JSONObject companyjson = new JSONObject();
                    List companyFields = getFieldlabelsList(true);
                    
                    JSONObject jSONObjectcomp = new JSONObject();
                    jSONObjectcomp.put("fieldid", "NA");
                    jSONObjectcomp.put("id", "NA");
                    jSONObjectcomp.put("label", "----------------[Company Level Fields]------------");
                    jSONObjectcomp.put("xtype", "");
                    companyjson.append("data", jSONObjectcomp);
                    
                    for (int cnt = 0; cnt < list.size(); cnt++) {
                        Object[] row = (Object[]) list.get(cnt);
                        if (row[8].equals('1')) {
                            JSONObject tempObj = new JSONObject();
                            tempObj.put("id", row[0]);
                            tempObj.put("label", row[1]);
                            tempObj.put("dbcolumnname", row[2]);
                            tempObj.put("reftablename", row[3]);
                            tempObj.put("reftablefk", row[4]);
                            tempObj.put("reftabledatacolumn", row[5]);
                            tempObj.put("dummyvalue", row[6]);
                            tempObj.put("xtype", row[7]);
                            tempObj.put("customfield", false);
                            if (companyFields.contains((String) row[1])) {
                                companyjson.append("data", tempObj);
                            }
                        }
                    }
                    jresult.append("data", companyjson);
                }
            }
           
        } catch (Exception ex) {
            Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return jresult;
        }
    }
    
    //fetching customfields for particular moduleid
    public static JSONObject fetchCustomFieldsWithModule(CustomDesignDAO customDesignDAOObj,JSONObject jresult, int moduleid, String companyid) {
        try {
            JSONObject customfieldjSONObject = new JSONObject();
            KwlReturnObject result = customDesignDAOObj.getGlobalCustomFields(companyid, moduleid);
            List list = result.getEntityList();
            if (list.size() > 0) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("fieldid", "NA");
                jSONObject.put("id", "NA");
                jSONObject.put("xtype", "");
                jSONObject.put("label", !StringUtil.isNullOrEmpty(getModuleName(moduleid)) ? "------["+getModuleName(moduleid)+" Custom Fields/Dimensions]-----" : "------[Custom Fields/Dimensions]------");
                customfieldjSONObject.append("data", jSONObject);
            }
            for (int cnt = 0; cnt < list.size(); cnt++) {
                // fetch columns - id, defaultHeader, dbcolumnname,reftablename, reftablefk,reftabledatacolumn,dummyvalue
                Object[] row = (Object[]) list.get(cnt);
                JSONObject tempObj = new JSONObject();
                tempObj.put("id", row[0]);
                tempObj.put("label", row[1]);
                tempObj.put("xtype", row[2]);
                tempObj.put("customfield", true);
                customfieldjSONObject.append("data", tempObj);
            }
              jresult.append("data", customfieldjSONObject);
        } catch (Exception ex) {
            Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return  jresult;
        }
    }

    public static JSONObject fetchDefaultHeaderFieldsWithCategories(CustomDesignDAO customDesignDAOObj, JSONObject jresult, int moduleid, String companyid) {
        try {
            KwlReturnObject result = customDesignDAOObj.getDefaultHeaders(String.valueOf(moduleid), companyid);
            List list = result.getEntityList();
            List addressFields = getFieldlabelsList(false);
            JSONObject addressjson = new JSONObject();
            JSONObject otherfieldsjson = new JSONObject();
            
            JSONObject jSONObjectaddress= new JSONObject();
            jSONObjectaddress.put("fieldid", "NA");
            jSONObjectaddress.put("id", "NA");
            jSONObjectaddress.put("label", "--------------------[Address Fields]------------");
            jSONObjectaddress.put("xtype", "");
            addressjson.append("data", jSONObjectaddress);
            
            JSONObject jSONObjectotherfields = new JSONObject();
            jSONObjectotherfields.put("fieldid", "NA");
            jSONObjectotherfields.put("id", "NA");
            jSONObjectotherfields.put("label","-----------["+getModuleName(moduleid)+" Fields]-----------");
            jSONObjectotherfields.put("xtype", "");
            otherfieldsjson.append("data", jSONObjectotherfields);

            for (int cnt = 0; cnt < list.size(); cnt++) {
                // fetch columns - id, defaultHeader, dbcolumnname,reftablename, reftablefk,reftabledatacolumn,dummyvalue,xtype,allowincustomtemplate
                Object[] row = (Object[]) list.get(cnt);
                if (row[8].equals('1')) { // value at index - 8 - allowincustomtemplate
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("id", row[0]);
                    tempObj.put("label", row[1]);
                    tempObj.put("dbcolumnname", row[2]);
                    tempObj.put("reftablename", row[3]);
                    tempObj.put("reftablefk", row[4]);
                    tempObj.put("reftabledatacolumn", row[5]);
                    tempObj.put("dummyvalue", row[6]);
                    tempObj.put("xtype", row[7]);
//                    tempObj.put("allowincustomtemplate", row[8]);
                    tempObj.put("customfield", false);
                    if (addressFields.contains((String) row[1])) {
                        addressjson.append("data", tempObj);
                    } else {
                        otherfieldsjson.append("data", tempObj);
                    }
                }
            }
           /*Hardocoded fields from customdesigner constants*/
            HashMap<String, String> extraCols = null;
            extraCols = getExtraFieldsForModule(moduleid);
            if (extraCols != null) {
                for (Map.Entry<String, String> extraColsEntry : extraCols.entrySet()) {
                    if (!extraColsEntry.getKey().contains("AllDimensions") && !extraColsEntry.getKey().contains("AllLinelevelCustomFields")) {//No need to show line level dimension and custom fields
                        JSONObject staticamountInfo = new JSONObject(extraColsEntry.getValue());
                        staticamountInfo.put("fieldid", extraColsEntry.getKey());
                        staticamountInfo.put("label", staticamountInfo.get("label"));
                        staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                        staticamountInfo.put("id", extraColsEntry.getKey());
                        if (addressFields.contains(staticamountInfo.get("label"))) {
                            addressjson.append("data", staticamountInfo);
                        } else {
                            otherfieldsjson.append("data", staticamountInfo);
                        }
                    }
                }
            }
            jresult.append("data", addressjson);
            jresult.append("data", otherfieldsjson);
            
        } catch (Exception ex) {
            Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return jresult;
        }
    }
    
    public static List getFieldlabelsList(boolean addresscompanyflag) {
        List returnlist = new ArrayList();

        List companyFields = new ArrayList();
        companyFields.add("Company City");
        companyFields.add("Company Name");
        companyFields.add("Company PhoneNo");
        companyFields.add("Company State");
        companyFields.add("Company ZipCode");
        companyFields.add("Company FaxNumber");
        companyFields.add("Company Email");
        companyFields.add("Company Website");
        companyFields.add("Company Post Text");
        companyFields.add("Company Shipping Address");
        companyFields.add("Company Billing Address");

        List addressFields = new ArrayList();
        addressFields.add("Vendor Billing Address");
        addressFields.add("Vendor Billing County");
        addressFields.add("Vendor Billing City");
        addressFields.add("Vendor Billing State");
        addressFields.add("Vendor Billing Country");
        addressFields.add("Vendor Billing Postal Code");
        addressFields.add("Vendor Billing Phone No");
        addressFields.add("Vendor Billing Fax No");
        addressFields.add("Vendor Billing Contact Person");
        addressFields.add("Vendor Billing Email");
        addressFields.add("Vendor Billing Mobile No");
        addressFields.add("Vendor Shipping Address");
        addressFields.add("Vendor Shipping County");
        addressFields.add("Vendor Shipping City");
        addressFields.add("Vendor Shipping State");
        addressFields.add("Vendor Shipping Country");
        addressFields.add("Vendor Shipping Postal code");
        addressFields.add("Vendor Shipping Phone No");
        addressFields.add("Vendor Shipping Fax No");
        addressFields.add("Vendor Shipping Contact Person");
        
        addressFields.add("Customer Billing Address");
        addressFields.add("Customer Billing County");
        addressFields.add("Customer Billing City");
        addressFields.add("Customer Billing State");
        addressFields.add("Customer Billing Country");
        addressFields.add("Customer Billing Postal Code");
        addressFields.add("Customer Billing Phone No");
        addressFields.add("Customer Billing Fax No");
        addressFields.add("Customer Billing Contact Person");
        addressFields.add("Customer Billing Email");
        addressFields.add("Customer Billing Mobile No");
        addressFields.add("Customer Shipping Address");
        addressFields.add("Customer Shipping County");
        addressFields.add("Customer Shipping City");
        addressFields.add("Customer Shipping State");
        addressFields.add("Customer Shipping Country");
        addressFields.add("Customer Shipping Postal code");
        addressFields.add("Customer Shipping Phone No");
        addressFields.add("Customer Shipping Fax No");
        addressFields.add("Customer Shipping Contact Person");

        addressFields.add("Bill To");
        addressFields.add("Ship To");
        addressFields.add("Shipping Address");
        addressFields.add("Shipping Address County");
        addressFields.add("Shipping Address City");
        addressFields.add("Shipping Address State");
        addressFields.add("Shipping Address Country");
        addressFields.add("Shipping Address Postal Code");
        addressFields.add("Billing Address Email");
        addressFields.add("Shipping Address Fax No");
        addressFields.add("Shipping Address Phone No");
        addressFields.add("Shipping Address Contact Person");
        addressFields.add("Shipping Address Mobile No");
        addressFields.add("Shipping Address Email");
        addressFields.add("Shipping Address ContactPersonNo");

        addressFields.add("Billing Address");
        addressFields.add("Billing Address County");
        addressFields.add("Billing Address City");
        addressFields.add("Billing Address State");
        addressFields.add("Billing Address Country");
        addressFields.add("Billing Address Postal Code");
        addressFields.add("Billing Address Email");
        addressFields.add("Billing Address Phone No");
        addressFields.add("Billing Address Fax No");
        addressFields.add("Billing Address Contact Person");
        addressFields.add("Billing Address MobileNo");
        addressFields.add("Billing Address ContactPersonNo");

        addressFields.add("Vendor Transactional Shipping Address");
        addressFields.add("Vendor Transactional Shipping Address County");
        addressFields.add("Vendor Transactional Shipping Address City");
        addressFields.add("Vendor Transactional Shipping Address State");
        addressFields.add("Vendor Transactional Shipping Address Country");
        addressFields.add("Vendor Transactional Shipping Address Postal Code");
        addressFields.add("Vendor Transactional Shipping Address Mobile No");
        addressFields.add("Vendor Transactional Shipping Address Phone");
        addressFields.add("Vendor Transactional ShippingAddressContactPerson");
        addressFields.add("Vendor Transactional ShippingAddressContactPersonNo");
        addressFields.add("Vendor Transactional ShippingAddress Fax");
        addressFields.add("Vendor Transactional ShippingAddress Email");
        addressFields.add("Vendor Transactional ShipTo");

        if (addresscompanyflag) {
            returnlist = companyFields;
        } else {
            returnlist = addressFields;
        }
        return returnlist;
    }

    public static String getModuleName(int moduleid) {
        String moduleName = "";
        switch (moduleid) {
            case (Constants.Acc_Invoice_ModuleId):
                moduleName = "Sales Invoice/Cash Sales";
                break;
            case (Constants.Acc_Vendor_Invoice_ModuleId):
                moduleName = "Purchase Invoice/Cash Purchase";
                break;
            case (Constants.Acc_Sales_Order_ModuleId):
                moduleName = "Sales Order";
                break;
            case (Constants.Acc_Purchase_Order_ModuleId):
                moduleName = "Purchase Order";
                break;
            case (Constants.Acc_Customer_Quotation_ModuleId):
                moduleName = "Customer Quotation";
                break;
            case (Constants.Acc_Vendor_Quotation_ModuleId):
                moduleName = "Vendor Quotation";
                break;
            case (Constants.Acc_Customer_ModuleId):
                moduleName = "Customer Master";
                break;
            case (Constants.Acc_Vendor_ModuleId):
                moduleName = "Vendor Master";
                break;
            case (Constants.Acc_Delivery_Order_ModuleId):
                moduleName = "Delivery Order";
                break;
            case (Constants.Acc_Goods_Receipt_ModuleId):
                moduleName = "Goods Receipt Order";
                break;
            case (Constants.Acc_Purchase_Return_ModuleId):
                moduleName = "Purchase Return";
                break;
            case (Constants.Acc_Sales_Return_ModuleId):
                moduleName = "Sales Return";
                break;
            case (Constants.Acc_Make_Payment_ModuleId):
                moduleName = "Make Payment";
                break;
            case (Constants.Acc_Receive_Payment_ModuleId):
                moduleName = "Receive Payment";
                break;

        }
        return moduleName;
    }
    /**
     * Description: This method is used to sort JSONArray based on sort key on String Value
     * @param array: JSONArray which has to be sort
     * @param sortkey: It is key of JSONObject, Array will sort on its value
     * @param sortedby: If it will true then array will sort in ASC order
     * otherwise it will sort in DESC order
     * @return JSONArray
     * @throws JSONException
     */
    public static JSONArray sortJsonArrayOnStringValues(JSONArray array, String sortkey, boolean sortedby) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        final String sortKey = sortkey;
        final boolean sortedBy = sortedby;
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    String lid = "", rid = "";
                    try {
                        lid = lhs.getString(sortKey);
                        rid = rhs.getString(sortKey);  
                    } catch (JSONException ex) {
                         Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return sortedBy ? lid.compareTo(rid) : rid.compareTo(lid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }
        return new JSONArray(jsons);
    }

    /**
     * Method to sort JSONArray based on JSONObjec -> Key by ignoring case.
     * mETHOD
     * @param array
     * @param sortkey
     * @param sortedby
     * @return
     * @throws JSONException
     */
    public static JSONArray sortJsonArrayOnStringValuesByIgnoringCase(JSONArray array, String sortkey, boolean sortedby) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        final String sortKey = sortkey;
        final boolean sortedBy = sortedby;
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    String lid = "", rid = "";
                    try {
                        lid = lhs.getString(sortKey);
                        rid = rhs.getString(sortKey);  
                    } catch (JSONException ex) {
                         Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return sortedBy ? lid.compareToIgnoreCase(rid) : rid.compareToIgnoreCase(lid);
//                    return sortedBy ? lid.compareTo(rid) : rid.compareTo(lid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }
        return new JSONArray(jsons);
    }
    
    public static Map<String, JSONArray> getSortedArrayMapBasedOnJSONAttribute(JSONArray array, String key) throws JSONException{
        Map<String, JSONArray> jArrMap = new TreeMap<String, JSONArray>();
        for (int i = 0; i < array.length(); i++) {
            String keyValue = array.getJSONObject(i).getString(key);
            if(jArrMap.containsKey(keyValue)){
                jArrMap.get(keyValue).put(array.getJSONObject(i));
            }
            else{
                jArrMap.put(keyValue, (new JSONArray().put(array.getJSONObject(i))));
            }
        }
        return jArrMap;
    }
    public static Map<String, JSONArray> getSortedArrayMapBasedOnJSONAttribute(JSONArray array, String key, String dir) throws JSONException {
        Map<String, JSONArray> jArrMap = new TreeMap<String, JSONArray>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < array.length(); i++) {
            String keyValue = array.getJSONObject(i).optString(key);
            if (jArrMap.containsKey(keyValue)) {
                jArrMap.get(keyValue).put(array.getJSONObject(i));
            } else {
                jArrMap.put(keyValue, (new JSONArray().put(array.getJSONObject(i))));
            }
        }
        if (dir.equals("DESC")) {
            Map<String, JSONArray> reverseSortedMap = new TreeMap<String, JSONArray>(Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER));
            reverseSortedMap.putAll(jArrMap);
            return reverseSortedMap;
        } else {

            return jArrMap;
        }
    }
    /**
     * Description: This method is used to sort JSONArray based on sort key on
     * String Value
     *
     * @param array: JSONArray which has to be sort
     * @param sortkey: It is key of JSONObject, Array will sort on its value
     * @param sortedby: If it will true then array will sort in ASC order
     * otherwise it will sort in DESC order
     * @return JSONArray
     * @throws JSONException
     */
     public static JSONArray sortJsonArrayOnIntegerValues(JSONArray array, final String sortkey, final String order) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    Double lid = 0.00, rid = 0.00;
                    lid = lhs.optDouble(sortkey, 0);
                    rid = rhs.optDouble(sortkey, 0);
                    if ("ASC".equalsIgnoreCase(order)) {
                        return lid.compareTo(rid);
                    } else if ("DESC".equalsIgnoreCase(order)) {
                        return rid.compareTo(lid);
                    } else {
                        return lid.compareTo(rid);
                    }

                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new JSONArray(jsons);
    }


    /**
     * Description: This method is used to sort JSONArray based on sort key having Date Value
     * @param array: JSONArray which has to be sort
     * @param sortkey: It is key of JSONObject, Array will sort on its value
     * @param sortedby: If it will true then array will sort in ASC order
     * otherwise it will sort in DESC order
     * @return JSONArray
     * @throws JSONException
     */
    public static JSONArray sortJsonArrayOnDateValues(JSONArray array, DateFormat df, String sortkey, boolean sortedby) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        final DateFormat df1 = df;
        final String sortKey = sortkey;
        final boolean sortedBy = sortedby;
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    Date ldate = new Date(), rdate = new Date();
                    try {
                        ldate = df1.parse(lhs.getString(sortKey));
                        rdate = df1.parse(rhs.getString(sortKey));
                    } catch (JSONException | ParseException ex) {
                        Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return sortedBy ? ldate.compareTo(rdate) : rdate.compareTo(ldate);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }
        return new JSONArray(jsons);
    }
    
    public static List<Map<String, Object>> sortListOfMapsOnStringValues(List<Map<String, Object>> list, String sortkey, boolean sortedby) {
        final String sortKey = sortkey;
        final boolean sortedBy = sortedby;
        try {
            Collections.sort(list, new Comparator<Map<String, Object>>() {

                @Override
                public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                    String lid = "", rid = "";
                    lid = (String) lhs.get(sortKey);
                    rid = (String) rhs.get(sortKey);
                    return sortedBy ? lid.compareTo(rid) : rid.compareTo(lid);
}
            });
        } catch (Exception ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
    
    public static List<Map<String, Object>> sortListOfMapsOnDateValues(List<Map<String, Object>> list, String sortkey, boolean sortedby) {
        final String sortKey = sortkey;
        final boolean sortedBy = sortedby;
        try {
            Collections.sort(list, new Comparator<Map<String, Object>>() {

                @Override
                public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                    Date lid = new Date(), rid = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        lid = formatter.parse((String) lhs.get(sortKey));
                        rid = formatter.parse((String) rhs.get(sortKey));
                    } catch (ParseException ex) {
                        Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return sortedBy ? lid.compareTo(rid) : rid.compareTo(lid);
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(authHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

}
