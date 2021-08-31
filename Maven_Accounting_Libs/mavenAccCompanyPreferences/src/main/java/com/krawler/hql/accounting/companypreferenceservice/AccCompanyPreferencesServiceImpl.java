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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  0in 2110-1301, USA.
 */
package com.krawler.hql.accounting.companypreferenceservice;

import com.krawler.accounting.integration.common.IntegrationCommonService;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.ChequeSequenceFormat;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import static com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants.*;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesController;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.CurrencyContants;
import com.krawler.spring.accounting.currency.accCurrencyController;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.context.MessageSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * @author krawler
 */
public class AccCompanyPreferencesServiceImpl implements AccCompanyPreferencesService, CurrencyContants {

    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private APICallHandlerService apiCallHandlerService;
    private profileHandlerDAO profileHandlerDAOObj;
    private IntegrationCommonService integrationCommonService;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setIntegrationCommonService(IntegrationCommonService integrationCommonService) {
        this.integrationCommonService = integrationCommonService;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj) {
        this.profileHandlerDAOObj = profileHandlerDAOObj;
    }

    @Override
    public JSONObject getSequenceFormatStore(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String mode = request.getParameter("mode");
            String isAllowNAStr = request.getParameter("isAllowNA");
            CompanyAccountPreferences preferences;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);

            JSONArray jArr = new JSONArray();

            JSONObject jNA = new JSONObject();

            jNA.put("id", "NA");
            jNA.put("value", "NA");
            jNA.put("oldflag", true);
            boolean isAllNA = true;
            if (!StringUtil.isNullOrEmpty(isAllowNAStr)) {
                isAllNA = Boolean.parseBoolean(isAllowNAStr);
            }

            String formatArr[] = null;
            if (!StringUtil.isNullOrEmpty(mode)) {
                if (mode.equalsIgnoreCase("autojournalentry")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getJournalEntryNumberFormat())) {
                        formatArr = preferences.getJournalEntryNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autoinvoice")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getInvoiceNumberFormat())) {
                        formatArr = preferences.getInvoiceNumberFormat().split(",");

                    }
                }

                if (mode.equalsIgnoreCase("autocreditmemo")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getCreditNoteNumberFormat())) {
                        formatArr = preferences.getCreditNoteNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autoreceipt")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getReceiptNumberFormat())) {
                        formatArr = preferences.getReceiptNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autogoodsreceipt")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getGoodsReceiptNumberFormat())) {
                        formatArr = preferences.getGoodsReceiptNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autodebitnote")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getDebitNoteNumberFormat())) {
                        formatArr = preferences.getDebitNoteNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autopayment")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getPaymentNumberFormat())) {
                        formatArr = preferences.getPaymentNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autoso")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getSalesOrderNumberFormat())) {
                        formatArr = preferences.getSalesOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autopo")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getPurchaseOrderNumberFormat())) {
                        formatArr = preferences.getPurchaseOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autocashsales")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getCashSaleNumberFormat())) {
                        formatArr = preferences.getCashSaleNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autocashpurchase")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getCashPurchaseNumberFormat())) {
                        formatArr = preferences.getCashPurchaseNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillinginvoice")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingInvoiceNumberFormat())) {
                        formatArr = preferences.getBillingInvoiceNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingreceipt")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingReceiptNumberFormat())) {
                        formatArr = preferences.getBillingReceiptNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingcashsales")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingCashSaleNumberFormat())) {
                        formatArr = preferences.getBillingCashSaleNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillinggoodsreceipt")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingGoodsReceiptNumberFormat())) {
                        formatArr = preferences.getBillingGoodsReceiptNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingdebitnote")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingDebitNoteNumberFormat())) {
                        formatArr = preferences.getBillingDebitNoteNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingcreditmemo")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingCreditNoteNumberFormat())) {
                        formatArr = preferences.getBillingCreditNoteNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingpayment")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingPaymentNumberFormat())) {
                        formatArr = preferences.getBillingPaymentNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingso")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingSalesOrderNumberFormat())) {
                        formatArr = preferences.getBillingSalesOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingpo")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingPurchaseOrderNumberFormat())) {
                        formatArr = preferences.getBillingPurchaseOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingcashpurchase")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingCashPurchaseNumberFormat())) {
                        formatArr = preferences.getBillingCashPurchaseNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autorequisition")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getRequisitionNumberFormat())) {
                        formatArr = preferences.getRequisitionNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autorequestforquotation")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getRfqNumberFormat())) {
                        formatArr = preferences.getRfqNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autovenquotation")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getVenQuotationNumberFormat())) {
                        formatArr = preferences.getVenQuotationNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autoquotation")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getQuotationNumberFormat())) {
                        formatArr = preferences.getQuotationNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autodo")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getDeliveryOrderNumberFormat())) {
                        formatArr = preferences.getDeliveryOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autogro")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getGoodsReceiptOrderNumberFormat())) {
                        formatArr = preferences.getGoodsReceiptOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autosr")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getSalesReturnNumberFormat())) {
                        formatArr = preferences.getSalesReturnNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autopr")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getPurchaseReturnNumberFormat())) {
                        formatArr = preferences.getPurchaseReturnNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autoproductid")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getProductidNumberFormat())) {
                        formatArr = preferences.getProductidNumberFormat().split(",");
                    }
                }

//                if(mode.equalsIgnoreCase("autocustomercode")){
//                    if(!StringUtil.isNullOrEmpty(preferences.getCustomerCodeFormat())){
//                        formatArr = preferences.getCustomerCodeFormat().split(",");
//                    }
//                }
//                
//                if(mode.equalsIgnoreCase("autovendorcode")){
//                    if(!StringUtil.isNullOrEmpty(preferences.getVendorCodeFormat())){
//                        formatArr = preferences.getVendorCodeFormat().split(",");
//                    }
//                }


                if (formatArr != null) {

                    for (String format : formatArr) {
                        JSONObject j = new JSONObject();
                        if (!StringUtil.isNullOrEmpty(format)) {
                            j.put("id", format);
                            j.put("value", format);
                            j.put("oldflag", true);
                            jArr.put(j);
                        }

                    }

                }

                Map<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                filterParams.put("modulename", mode);
                KwlReturnObject result1 = accCompanyPreferencesObj.getSequenceFormat(filterParams);
                Iterator itr = result1.getEntityList().iterator();
                while (itr.hasNext()) {
                    SequenceFormat seqFormat = (SequenceFormat) itr.next();
                    JSONObject j = new JSONObject();
                    j.put("id", seqFormat.getID());
                    j.put("value", seqFormat.getName());
                    j.put("prefix", seqFormat.getPrefix());
                    j.put("suffix", seqFormat.getSuffix());
                    j.put("numberofdigit", seqFormat.getNumberofdigit());
                    j.put("startfrom", seqFormat.getStartfrom());
                    j.put("showdateinprefix", seqFormat.isDateBeforePrefix()? "Yes" : "No");
                    j.put("showleadingzero", seqFormat.isShowleadingzero() ? "Yes" : "No");
                    j.put("oldflag", false);
                    jArr.put(j);
                }
            }
            if (isAllNA) {
                jArr.put(jNA);
            }
            jobj.put(Constants.RES_data, jArr);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    @Override
    public JSONObject getNextAutoNumber(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int from = Integer.parseInt(request.getParameter("from"));
            String sequenceformat = request.getParameter("sequenceformat");
            boolean ignoreLeadingZero = false;
            boolean oldflag = request.getParameter("oldflag") != null ? StringUtil.getBoolean(request.getParameter("oldflag")) : false;
            CompanyAccountPreferences preferences;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", companyid);
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);
            ignoreLeadingZero = !preferences.isShowLeadingZero();
            int autoGenNumberStartFrom = getAutoGenNumberStartFrom(from, companyid);
            String nextAutoNumber = "";
            if (!StringUtil.isNullOrEmpty(sequenceformat)) {
                if (oldflag) {
                    nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, from, sequenceformat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, from, sequenceformat, oldflag, null);
                    nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                }
            } else {
                nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, from);
            }

            jobj.put(Constants.RES_data, nextAutoNumber);
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public int getAutoGenNumberStartFrom(int from, String companyId) {
        int startfrom = 1;
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", companyId);
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);
            switch (from) {
                case StaticValues.AUTONUM_JOURNALENTRY:
                    startfrom = pref.getJournalEntryNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_SALESORDER:
                    startfrom = pref.getSalesOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_INVOICE:
                    startfrom = pref.getInvoiceNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_CASHSALE:
                    startfrom = pref.getCashSaleNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_CREDITNOTE:
                    startfrom = pref.getCreditNoteNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_RECEIPT:
                    startfrom = pref.getReceiptNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_PURCHASEORDER:
                    startfrom = pref.getPurchaseOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_GOODSRECEIPT:
                    startfrom = pref.getGoodsReceiptNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_CASHPURCHASE:
                    startfrom = pref.getCashPurchaseNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_DEBITNOTE:
                    startfrom = pref.getDebitNoteNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_PAYMENT:
                    startfrom = pref.getPaymentNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGINVOICE:
                    startfrom = pref.getBillingInvoiceNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGRECEIPT:
                    startfrom = pref.getBillingReceiptNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGCASHSALE:
                    startfrom = pref.getBillingCashSaleNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGGOODSRECEIPT:
                    startfrom = pref.getBillingGoodsReceiptNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGPAYMENT:
                    startfrom = pref.getBillingPaymentNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGCASHPURCHASE:
                    startfrom = pref.getBillingCashPurchaseNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGPURCHASEORDER:
                    startfrom = pref.getBillingPurchaseOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGSALESORDER:
                    startfrom = pref.getBillingSalesOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGDEBITNOTE:
                    startfrom = pref.getBillingDebitNoteNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGCREDITNOTE:
                    startfrom = pref.getBillingCreditNoteNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_QUOTATION:
                    startfrom = pref.getQuotationNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_VENQUOTATION:
                    startfrom = pref.getVenQuotationNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_REQUISITION:
                    startfrom = pref.getRequisitionNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_RFQ:
                    startfrom = pref.getRfqNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_PRODUCTID:
                    startfrom = pref.getProductidNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_DELIVERYORDER:
                    startfrom = pref.getDeliveryOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_GOODSRECEIPTORDER:
                    startfrom = pref.getGoodsReceiptOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_SALESRETURN:
                    startfrom = pref.getSalesReturnNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_PURCHASERETURN:
                    startfrom = pref.getPurchaseReturnNumberFormatStartFrom();
                    break;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return startfrom;
    }

    @Override
    public JSONObject getCompanyAccountPreferences(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);

            KwlReturnObject resultExtraCmpPre = accCompanyPreferencesObj.getExtraCompanyPreferences(requestParams);
            ExtraCompanyPreferences extraCompanyPreferences = null;
            if (!resultExtraCmpPre.getEntityList().isEmpty()) {
                extraCompanyPreferences = (ExtraCompanyPreferences) resultExtraCmpPre.getEntityList().get(0);
            }
            // Compliance Extra company preferences
            KwlReturnObject resultComplianceExtraCmpPre = accCompanyPreferencesObj.getIndiaComplianceExtraCompanyPreferences(requestParams);
            IndiaComplianceCompanyPreferences indiaComplianceExtraCompanyPreferences = null;
            if (!resultComplianceExtraCmpPre.getEntityList().isEmpty()) {
                indiaComplianceExtraCompanyPreferences = (IndiaComplianceCompanyPreferences) resultComplianceExtraCmpPre.getEntityList().get(0);
            }
            DateFormat caldf = authHandler.getDateOnlyFormat();
            Calendar systemDate = Calendar.getInstance();
            Calendar financialYearFromTemp = Calendar.getInstance();
            financialYearFromTemp.setTime(pref.getFinancialYearFrom());
            Calendar financialYearFrom = Calendar.getInstance();
            financialYearFrom.setTime(pref.getFinancialYearFrom());
            financialYearFrom.set(Calendar.YEAR, financialYearFrom.get(Calendar.YEAR) + 1);
            if (systemDate.after(financialYearFrom)) {  //Check this condition
                Date finYearFrom  = caldf.parse(caldf.format(financialYearFrom.getTime()));
                Date finYearFromTemp = caldf.parse(caldf.format(financialYearFromTemp.getTime()));
                pref.setFinancialYearFrom(finYearFrom);
                accCompanyPreferencesObj.setNewYear(finYearFrom, finYearFromTemp, sessionHandlerImpl.getCompanyid(request));
//            	accCompanyPreferencesObj.setCurrentYear(financialYearFrom.get(Calendar.YEAR),(financialYearFrom.get(Calendar.YEAR) - 1),sessionHandlerImpl.getCompanyid(request));
            } else if (systemDate.before(financialYearFromTemp)) { //Check this condition
                Date finYearFrom  = caldf.parse(caldf.format(financialYearFrom.getTime()));
                Date finYearFromTemp = caldf.parse(caldf.format(financialYearFromTemp.getTime()));
                financialYearFromTemp.set(Calendar.YEAR, financialYearFromTemp.get(Calendar.YEAR) - 1);
                pref.setFinancialYearFrom(finYearFromTemp);
                accCompanyPreferencesObj.setNewYear(finYearFrom, finYearFromTemp, sessionHandlerImpl.getCompanyid(request));
//            	accCompanyPreferencesObj.setCurrentYear((financialYearFrom.get(Calendar.YEAR) - 1),(financialYearFrom.get(Calendar.YEAR) - 2),sessionHandlerImpl.getCompanyid(request));
            }
            //Fetch newly created sequence formats
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put("companyid", pref.getID());
            KwlReturnObject result1 = accCompanyPreferencesObj.getSequenceFormat(filterParams);
            List ll = result1.getEntityList();


            KwlReturnObject chequeFormatResult = accCompanyPreferencesObj.getChequeSequenceFormatList(filterParams);
            List chequeFormatList = chequeFormatResult.getEntityList();
            if (extraCompanyPreferences != null) {
                request.setAttribute("extraCompanyPreferences", extraCompanyPreferences);
            }
            if (indiaComplianceExtraCompanyPreferences != null) {
                request.setAttribute("complianceExtraCompanyPreferences", indiaComplianceExtraCompanyPreferences);
            }

            boolean freezDepreciation=accCompanyPreferencesObj.getDepreciationCount(sessionHandlerImpl.getCompanyid(request));           
            JSONObject prefJobj = getCompanyAccountPreferences(request, pref, ll, chequeFormatList);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            prefJobj.put("freezDepreciation", freezDepreciation);
            boolean openingDepreciationPosted=accCompanyPreferencesObj.getopeningDepreciationPostedCount(companyid);                                 
            prefJobj.put("openingDepreciationPosted",openingDepreciationPosted);
            /**
             * **** If Integration with Accounting and Inventory System then
             * set QA Approval Flow Status from Inventory system *****
             */
            if (pref.isInventoryAccountingIntegration()) {
                HashMap<String, Object> prefMap = new HashMap<String, Object>();
                String userid = sessionHandlerImpl.getUserid(request);
                boolean isQAApprovalFlow = getQAApprovalStatus(companyid, userid);
                prefMap.put("id", companyid);
                prefMap.put("isQaApprovalFlow", isQAApprovalFlow);
                result = accCompanyPreferencesObj.updatePreferences(prefMap);
                if (!result.getEntityList().isEmpty()) {
                    CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);
                    prefJobj.put(CompanyPreferencesConstants.QAAPPROVALFLOW, companyAccountPreferences.isQaApprovalFlow());
                } else {
                    prefJobj.put(CompanyPreferencesConstants.QAAPPROVALFLOW, isQAApprovalFlow);
                }
            }

            getTransactionFormFieldHideShowStatus(companyid, prefJobj);

            List list = accCompanyPreferencesObj.getMappedCompanies(companyid);
            jobj.put("consolidateFlag", list.size() > 0 ? true : false);
            jobj.put(Constants.RES_data, prefJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    private boolean getQAApprovalStatus(String companyid, String userid) {
        boolean isQAApprovalFlow = false;
        Session session=null;
        try {
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", userid);
            userData.put("companyid", companyid);

            //session = HibernateUtil.getCurrentSession();

            String url = storageHandlerImpl.GetinventoryURL();
            JSONObject resObj = apiCallHandlerService.callApp(url, userData, companyid, "20");
            if (!resObj.isNull(Constants.RES_success) && resObj.getBoolean(Constants.RES_success)) {
                if (!resObj.isNull("isIncludeQAapprovalFlow") && resObj.has("isIncludeQAapprovalFlow")) {
                    isQAApprovalFlow = resObj.getBoolean("isIncludeQAapprovalFlow");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, "CompanyPreferencesCMN.getQAApprovalStatus", ex);
        }
//        finally{
//            HibernateUtil.closeSession(session);
//        }
        return isQAApprovalFlow;
    }

    private void getTransactionFormFieldHideShowStatus(String companyid, JSONObject prefJobj) {
        // Putting data for hide show form fields of Customer invoice form.

        try {

            // fetching data for customer invoice module

            JSONArray crmCIArray = new JSONArray();

            int moduleId = Constants.Acc_Invoice_ModuleId;

            KwlReturnObject crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            List<CustomizeReportMapping> crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmCIArray.put(crmObj);
            }

            prefJobj.put("customerInvoice", crmCIArray);


            // fetching data for vendor invoice module

            JSONArray crmVIArray = new JSONArray();

            moduleId = Constants.Acc_Vendor_Invoice_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmVIArray.put(crmObj);
            }

            prefJobj.put("vendorInvoice", crmVIArray);

            // fetching data for Cash Purchase module

            JSONArray crmCPArray = new JSONArray();

            moduleId = Constants.Acc_Cash_Purchase_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmCPArray.put(crmObj);
            }

            prefJobj.put("CP", crmCPArray);

            // fetching data for Cash Sales module

            JSONArray crmCSArray = new JSONArray();

            moduleId = Constants.Acc_Cash_Sales_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmCSArray.put(crmObj);
            }

            prefJobj.put("CS", crmCSArray);


            // fetching data for Purchase Order module

            JSONArray crmPOArray = new JSONArray();

            moduleId = Constants.Acc_Purchase_Order_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmPOArray.put(crmObj);
            }

            prefJobj.put("purchaseOrder", crmPOArray);


            // fetching data for Purchase Order module

            JSONArray crmSOArray = new JSONArray();

            moduleId = Constants.Acc_Sales_Order_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmSOArray.put(crmObj);
            }

            prefJobj.put("salesOrder", crmSOArray);


            // fetching data for Purchase Order module

            JSONArray crmVQArray = new JSONArray();

            moduleId = Constants.Acc_Vendor_Quotation_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmVQArray.put(crmObj);
            }

            prefJobj.put("vendorQuotation", crmVQArray);

            // fetching data for Purchase Order module

            JSONArray crmCQArray = new JSONArray();

            moduleId = Constants.Acc_Customer_Quotation_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmCQArray.put(crmObj);
            }

            prefJobj.put("customerQuotation", crmCQArray);

            // fetching data for Purchase Return module

            JSONArray crmPRArray = new JSONArray();

            moduleId = Constants.Acc_Purchase_Return_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmPRArray.put(crmObj);
            }

            prefJobj.put("purchaseReturn", crmPRArray);

            // fetching data for Sales Return module

            JSONArray crmSRArray = new JSONArray();

            moduleId = Constants.Acc_Sales_Return_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmSRArray.put(crmObj);
            }

            prefJobj.put("salesReturn", crmSRArray);

            // fetching data for Goods Receipt module

            JSONArray crmGRArray = new JSONArray();

            moduleId = Constants.Acc_Goods_Receipt_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmGRArray.put(crmObj);
            }

            prefJobj.put("goodsReceipt", crmGRArray);

            // fetching data for Delivery Order module

            JSONArray crmDOArray = new JSONArray();

            moduleId = Constants.Acc_Delivery_Order_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmDOArray.put(crmObj);
            }

            prefJobj.put("deliveryOrder", crmDOArray);


        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public JSONObject getCompanyAddressDetails(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONArray dataArray = new JSONArray();
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess=false;
        try{
            KwlReturnObject addressResult = accountingHandlerDAOobj.getCompanyAddressDetails(requestParams);               
            if (!addressResult.getEntityList().isEmpty()) {
                List<CompanyAddressDetails> addressDetails = addressResult.getEntityList();
                for (CompanyAddressDetails details : addressDetails) {
                    JSONObject addrObject = new JSONObject();
                    addrObject.put("aliasName", details.getAliasName() != null ? details.getAliasName() : "");
                    addrObject.put("address", details.getAddress() != null ? details.getAddress() : "");
                    addrObject.put("county", details.getCounty()!= null ? details.getCounty() : "");
                    addrObject.put("city", details.getCity() != null ? details.getCity() : "");
                    addrObject.put("state", details.getState() != null ? details.getState() : "");
                    addrObject.put("country", details.getCountry() != null ? details.getCountry() : "");
                    addrObject.put("postalCode", details.getPostalCode() != null ? details.getPostalCode() : "");
                    addrObject.put("phone", details.getPhone() != null ? details.getPhone() : "");
                    addrObject.put("mobileNumber", details.getMobileNumber() != null ? details.getMobileNumber() : "");
                    addrObject.put("fax", details.getFax() != null ? details.getFax() : "");
                    addrObject.put("emailID", details.getEmailID() != null ? details.getEmailID() : "");
                    addrObject.put("recipientName", details.getRecipientName() != null ? details.getRecipientName() : "");
                    addrObject.put("contactPerson", details.getContactPerson() != null ? details.getContactPerson() : "");
                    addrObject.put("contactPersonNumber", details.getContactPersonNumber() != null ? details.getContactPersonNumber() : "");
                    addrObject.put("contactPersonDesignation", details.getContactPersonDesignation() != null ? details.getContactPersonDesignation() : "");
                    addrObject.put("website", details.getWebsite() != null ? details.getWebsite() : "");
                    addrObject.put("isDefaultAddress", details.isIsDefaultAddress());
                    addrObject.put("isBillingAddress", details.isIsBillingAddress());
                    dataArray.put(addrObject);
                }
            }
            issuccess=true;           
        } catch(Exception ex){
             issuccess = false;
             msg = "accCompanyPreferencesController.getCompanyAddressDetails : " + ex;
             Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally{
            try {
                jobj.put(Constants.RES_data, dataArray);
                jobj.put("count", dataArray.length());
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    @Override
    public JSONObject saveCompanyAddressDetails(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject obj=new JSONObject();
        String msg="";
        boolean issuccess=false;
        try{
            String companyid =(String) requestParams.get(Constants.companyKey);
            KwlReturnObject deleteResult = accCompanyPreferencesObj.deleteCompanyAddressDetails(companyid);//Deleting existing address (if present) before update 

            String addressDetails =(String) requestParams.get("addressDetail");
            JSONArray jArr = new JSONArray(addressDetails);
            HashMap<String, Object> addrMap = new HashMap<String, Object>();
            addrMap.put("companyid", companyid);            
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);               
                addrMap.put("aliasName", jobj.optString("aliasName", ""));
                addrMap.put("address", jobj.optString("address", ""));
                addrMap.put("county", jobj.optString("county", ""));
                addrMap.put("city", jobj.optString("city", ""));
                addrMap.put("state", jobj.optString("state", ""));
                addrMap.put("country", jobj.optString("country", ""));
                addrMap.put("postalCode", jobj.optString("postalCode", ""));
                addrMap.put("phone", jobj.optString("phone", ""));
                addrMap.put("mobileNumber", jobj.optString("mobileNumber", ""));
                addrMap.put("fax", jobj.optString("fax", ""));
                addrMap.put("emailID", jobj.optString("emailID", ""));
                addrMap.put("recipientName", jobj.optString("recipientName", ""));
                addrMap.put("contactPerson", jobj.optString("contactPerson", ""));
                addrMap.put("contactPersonNumber", jobj.optString("contactPersonNumber", ""));
                addrMap.put("contactPersonDesignation", jobj.optString("contactPersonDesignation", ""));
                addrMap.put("website", jobj.optString("website", ""));
                addrMap.put("shippingRoute", jobj.optString("shippingRoute", ""));
                addrMap.put("isBillingAddress", jobj.getBoolean("isBillingAddress"));
                addrMap.put("isDefaultAddress", jobj.getBoolean("isDefaultAddress"));
                KwlReturnObject result = accCompanyPreferencesObj.saveCompanyAddressDetails(addrMap);
            }
            issuccess=true;
            msg="Company address has been saved successfully.";
        }catch(Exception ex){
             issuccess = false;
             msg = "accCompanyPreferencesController.saveCompanyAddressDetails : " + ex;
             Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally{
            try {
                obj.put(Constants.RES_success, issuccess);
                obj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return obj;
    }
  
    @Override
    public JSONObject getCompanyAccountPreferences(HttpServletRequest request, CompanyAccountPreferences pref, List ll, List chequeFormatList) throws ServiceException, SessionExpiredException {
        JSONObject obj = new JSONObject();
        HashMap<String, Integer> precisionMap = new HashMap<>();
        try {
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat(request);
            if (pref == null) {
                return obj;
            }
            Company company = null;
            if (pref != null) {
                company = pref.getCompany();
                boolean isMalasianCompany = company.getCountry().getID().equalsIgnoreCase("137");
                obj.put(PREFID, pref.getID());
                obj.put(FYFROM, formatter.format(pref.getFinancialYearFrom()));
                obj.put(FIRSTFYFROM, formatter.format(pref.getFirstFinancialYearFrom() != null ? pref.getFirstFinancialYearFrom() : pref.getFinancialYearFrom()));//saving in actual first financial year
                obj.put(BBFROM, formatter.format(pref.getBookBeginningFrom()));
                obj.put(DISCOUNTGIVEN, pref.getDiscountGiven().getID());
                obj.put(GSTAPPIEDDATE, pref.getGSTApplicableDate() != null ? formatter.format(pref.getGSTApplicableDate()) : null);
                obj.put("cashoutaccountforpos", pref.getPaymentMethod());
                obj.put(DISCOUNTRECEIVED, pref.getDiscountReceived().getID());
                if (pref.getForeignexchange() != null) {
                    obj.put(FOREIGNEXCHANGE, pref.getForeignexchange().getID());
                }
                if (pref.getUnrealisedgainloss() != null) {
                    obj.put(UNREALISEDGAINLOSS, pref.getUnrealisedgainloss().getID());
                }
                obj.put(SHIPPINGCHARGES, "");//pref.getShippingCharges().getID());
//                obj.put(OTHERCHARGES, pref.getOtherCharges().getID());
                obj.put(CASHACCOUNT, pref.getCashAccount().getID());
                if (pref.getDepereciationAccount() != null) {
                    obj.put(DEPRECIATIONACCOUNT, pref.getDepereciationAccount().getID());
                }

                String AUTOINVOICE_F = "";
                String AUTOBILLINGINVOICE_F = "";
                String AUTOGOODSRECEIPT_F = "";
                String AUTOBILLINGGOODSRECEIPT_F = "";
                String AUTOCASHPURCHASE_F = "";
                String AUTOBILLINGCASHPURCHASE_F = "";
                String AUTOCREDITMEMO_F = "";
                String AUTORECEIPT_F = "";
                String AUTOJOURNALENTRY_F = "";
                String AUTODEBITNOTE_F = "";
                String AUTOPAYMENT_F = "";
                String AUTOSO_F = "";
                String AUTOCONTRACT_F = "";
                String AUTODO_F = "";
                String AUTOSR_F = "";
                String AUTOGRO_F = "";
                String AUTOPO_F = "";
                String AUTOCASHSALES_F = "";
                String AUTOBILLINGRECEIPT_F = "";
                String AUTOBILLINGCASHSALES_F = "";
                String AUTOBILLINGCREDITMEMO_F = "";
                String AUTOBILLINGDEBITNOTE_F = "";
                String AUTOBILLINGPAYMENT_F = "";
                String AUTOBILLINGSO_F = "";
                String AUTOBILLINGPO_F = "";
                String AUTOQUOTATION_F = "";
                String AUTOVENQUOTATION_F = "";
                String AUTOREQUISITION_F = "";
                String AUTORFQ_F = "";
                String AUTOPACKINGDO_F = "";
                String AUTOSHIPPINGDO_F = "";
                String AUTOPRODUCTID_F = "";
                String AUTOPR_F = "";
                String AUTOCUSTOMERID_F = "";
                String AUTOVENDORID_F = "";
                String AUTOSALESDEBTCLAIMID_F = "";
                String AUTOSALESDEBTRECOVERID_F = "";
                String AUTOPURCHASEDEBTCLAIMID_F = "";
                String AUTOPURCHASEDEBTRECOVERID_F = "";
                String AUTOBUILDASSEMBLY_F = "";
                String AUTOUNBUILDASSEMBLY_F = "";
                String AUTORECONCILENUMBER_F = "";
                String AUTOUNRECONCILENUMBER_F = "";
                String AUTOSECURITYNO_F = "";
                String AUTOASSETGROUP_F = "";
                String AUTOLOANREFNUMBER_F = "";
                String AUTOLABOURREFNUMBER_F = "";
                String AUTOMRPCONTRACTREFNUMBER_F = "";
                String AUTOMACHINE_F = "";
                String AUTOJOBWORKNUMBER_F = "";
                String AUTOWORKCENTRENUMBER_F = "";
                String AUTOWORKORDERNUMBER_F = "";
                String AUTOROUTECODENUMBER_F = "";
                String AUTORG23ENTRYNUMBER_F = "";
                String AUTOJWO_F = "";
                Iterator itr = ll.iterator();
                while (itr.hasNext()) {
                    SequenceFormat seqFormat = (SequenceFormat) itr.next();
                    boolean isDateBeforePrefix = seqFormat.isDateBeforePrefix();
                    boolean isDateAfterPrefix = seqFormat.isDateAfterPrefix();
                    boolean isDateAfterSuffix = seqFormat.isShowDateFormatAfterSuffix();
                    String dateFormatBeforePrefix = StringUtil.isNullOrEmpty(seqFormat.getDateformatinprefix()) ? "" : seqFormat.getDateformatinprefix();
                    String dateFormatAfterPrefix = StringUtil.isNullOrEmpty(seqFormat.getDateformatafterprefix()) ? "" : seqFormat.getDateformatafterprefix();
                    String dateFormatAftrerSufix = StringUtil.isNullOrEmpty(seqFormat.getDateFormatAfterSuffix()) ? "" : seqFormat.getDateFormatAfterSuffix();
                    String name = seqFormat.getName();
                    if (isDateBeforePrefix && isDateAfterSuffix) {
                        name = dateFormatBeforePrefix + name + dateFormatAftrerSufix;
                    } else if (isDateAfterSuffix) {
                        name = name + dateFormatAftrerSufix;
                    } else if (isDateBeforePrefix) {
                        name = dateFormatBeforePrefix + name;
                    }
                    String modulename = seqFormat.getModulename();
                    if (modulename.equals(AUTOGOODSRECEIPT)) {
                        AUTOGOODSRECEIPT_F += name + ",";
                    }
                    if (modulename.equals(AUTOBILLINGGOODSRECEIPT)) {
                        AUTOBILLINGGOODSRECEIPT_F += name + ",";
                    }
                    if (modulename.equals(AUTOINVOICE)) {
                        AUTOINVOICE_F += name + ",";
                    }
                    if (modulename.equals(AUTOBILLINGINVOICE)) {
                        AUTOBILLINGINVOICE_F += name + ",";
                    }
                    if (modulename.equals(AUTOCASHPURCHASE)) {
                        AUTOCASHPURCHASE_F += name + ",";
                    }
                    if (modulename.equals(AUTOBILLINGCASHPURCHASE)) {
                        AUTOBILLINGCASHPURCHASE_F += name + ",";
                    }
                    if (modulename.equals(AUTOCREDITMEMO)) {
                        AUTOCREDITMEMO_F += name + ",";
                    }
                    if (modulename.equals(AUTORECEIPT)) {
                        AUTORECEIPT_F += name + ",";
                    }
                    if (modulename.equals(AUTOJOURNALENTRY)) {
                        AUTOJOURNALENTRY_F += name + ",";
                    }
                    if (modulename.equals(AUTODEBITNOTE)) {
                        AUTODEBITNOTE_F += name + ",";
                    }
                    if (modulename.equals(AUTOPAYMENT)) {
                        AUTOPAYMENT_F += name + ",";
                    }
                    if (modulename.equals(AUTOSO)) {
                        AUTOSO_F += name + ",";
                    }
                    if (modulename.equals(AUTOCONTRACT)) {
                        AUTOCONTRACT_F += name + ",";
                    }
                    if (modulename.equals(AUTODO)) {
                        AUTODO_F += name + ",";
                    }
                    if (modulename.equals(AUTOSR)) {
                        AUTOSR_F += name + ",";
                    }
                    if (modulename.equals(AUTOGRO)) {
                        AUTOGRO_F += name + ",";
                    }
                    if (modulename.equals(AUTOPO)) {
                        AUTOPO_F += name + ",";
                    }
                    if (modulename.equals(AUTOCASHSALES)) {
                        AUTOCASHSALES_F += name + ",";
                    }
                    if (modulename.equals(AUTOBILLINGRECEIPT)) {
                        AUTOBILLINGRECEIPT_F += name + ",";
                    }
                    if (modulename.equals(AUTOBILLINGCASHSALES)) {
                        AUTOBILLINGCASHSALES_F += name + ",";
                    }
                    if (modulename.equals(AUTOBILLINGCREDITMEMO)) {
                        AUTOBILLINGCREDITMEMO_F += name + ",";
                    }
                    if (modulename.equals(AUTOBILLINGDEBITNOTE)) {
                        AUTOBILLINGDEBITNOTE_F += name + ",";
                    }
                    if (modulename.equals(AUTOBILLINGPAYMENT)) {
                        AUTOBILLINGPAYMENT_F += name + ",";
                    }
                    if (modulename.equals(AUTOBILLINGSO)) {
                        AUTOBILLINGSO_F += name + ",";
                    }
                    if (modulename.equals(AUTOBILLINGPO)) {
                        AUTOBILLINGPO_F += name + ",";
                    }
                    if (modulename.equals(AUTOQUOTATION)) {
                        AUTOQUOTATION_F += name + ",";
                    }
                    if (modulename.equals(AUTOVENQUOTATION)) {
                        AUTOVENQUOTATION_F += name + ",";
                    }
                    if (modulename.equals(AUTOREQUISITION)) {
                        AUTOREQUISITION_F += name + ",";
                    }
                    if (modulename.equals(AUTORFQ)) {
                        AUTORFQ_F += name + ",";
                    }
                    if (modulename.equals(AUTOPACKINGDONUMBER)) {
                        AUTOPACKINGDO_F += name + ",";
                    }
                    if (modulename.equals(AUTOSHIPPINGDONUMBER)) {
                        AUTOSHIPPINGDO_F += name + ",";
                    }
                    if (modulename.equals(AUTOPRODUCTID)) {
                        AUTOPRODUCTID_F += name + ",";
                    }
                    if (modulename.equals(AUTOPR)) {
                        AUTOPR_F += name + ",";
                    }
                    if (modulename.equals(AUTOCUSTOMERID)) {
                        AUTOCUSTOMERID_F += name + ",";
                    }
                    if (modulename.equals(AUTOVENDORID)) {
                        AUTOVENDORID_F += name + ",";
                    }
                    if (modulename.equals(AUTOSALESDEBTCLAIMID)) {
                        AUTOSALESDEBTCLAIMID_F += name + ",";
                    }
                    if (modulename.equals(AUTOSALESDEBTRECOVERID)) {
                        AUTOSALESDEBTRECOVERID_F += name + ",";
                    }
                    if (modulename.equals(AUTOPURCHASEDEBTCLAIMID)) {
                        AUTOPURCHASEDEBTCLAIMID_F += name + ",";
                    }
                    if (modulename.equals(AUTOPURCHASEDEBTRECOVERID)) {
                        AUTOPURCHASEDEBTRECOVERID_F += name + ",";
                    }
                    if (modulename.equals(AUTOBUILDASSEMBLY)) {
                        AUTOBUILDASSEMBLY_F += name + ",";
                    }
                    if (modulename.equals(AUTOUNBUILDASSEMBLY)) {
                        AUTOUNBUILDASSEMBLY_F += name + ",";
                    }
                    if (modulename.equals(AUTORECONCILENUMBER)) {   //Reconcile No.
                        AUTORECONCILENUMBER_F += name + ",";
                    }
                    if (modulename.equals(AUTOUNRECONCILENUMBER)) {  //Unreconcile No.
                        AUTOUNRECONCILENUMBER_F += name + ",";
                    }
                    if (modulename.equals(AUTOASSETGROUP)) {
                        AUTOASSETGROUP_F += name + ",";
                    }
                    if (modulename.equals(AUTOLOANREFNUMBER)) {
                        AUTOLOANREFNUMBER_F += name + ",";
                    }
                    if (modulename.equals(AUTOLABOURREFNUMBER)) {
                        AUTOLABOURREFNUMBER_F += name + ",";
                    }
                    if (modulename.equals(AUTOMRPCONTRACTNUMBER)) {
                        AUTOMRPCONTRACTREFNUMBER_F += name + ",";
                    }
                    if (modulename.equals(AUTOROUTECODENUMBER)) {
                        AUTOROUTECODENUMBER_F += name + ",";
                    }
                    if (modulename.equals(AUTOMACHINEID)) {
                        AUTOMACHINE_F += name + ",";
                    }
                    if (modulename.equals(AUTOJOBWORKNUMBER)) {
                        AUTOJOBWORKNUMBER_F += name + ",";
                    }
                    if (modulename.equals(AUTOWORKCENTRENUMBER)) {
                        AUTOWORKCENTRENUMBER_F += name + ",";
                    }
                    if (modulename.equals(AUTOWORKORDERNUMBER)) {
                        AUTOWORKORDERNUMBER_F += name + ",";
                    }
                    if (modulename.equals(AUTOSECURITYNO)) {
                        AUTOSECURITYNO_F += name + ",";
                    }
                    if (modulename.equals(AUTORG23ENTRYNUMBER)) {
                        AUTORG23ENTRYNUMBER_F += name + ",";
                    }
                    if (modulename.equals(AUTOJWO)) {
                        AUTOJWO_F += name + ",";
                    }

                }
                if (!StringUtil.isNullOrEmpty(pref.getGoodsReceiptNumberFormat())) {
                    AUTOGOODSRECEIPT_F += pref.getGoodsReceiptNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getBillingGoodsReceiptNumberFormat())) {
                    AUTOBILLINGGOODSRECEIPT_F += pref.getBillingGoodsReceiptNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getInvoiceNumberFormat())) {
                    AUTOINVOICE_F += pref.getInvoiceNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getBillingInvoiceNumberFormat())) {
                    AUTOBILLINGINVOICE_F += pref.getBillingInvoiceNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getCashPurchaseNumberFormat())) {
                    AUTOCASHPURCHASE_F += pref.getCashPurchaseNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getBillingCashPurchaseNumberFormat())) {
                    AUTOBILLINGCASHPURCHASE_F += pref.getBillingCashPurchaseNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getCreditNoteNumberFormat())) {
                    AUTOCREDITMEMO_F += pref.getCreditNoteNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getReceiptNumberFormat())) {
                    AUTORECEIPT_F += pref.getReceiptNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getJournalEntryNumberFormat())) {
                    AUTOJOURNALENTRY_F += pref.getJournalEntryNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getDebitNoteNumberFormat())) {
                    AUTODEBITNOTE_F += pref.getDebitNoteNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getPaymentNumberFormat())) {
                    AUTOPAYMENT_F += pref.getPaymentNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getSalesOrderNumberFormat())) {
                    AUTOSO_F += pref.getSalesOrderNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getDeliveryOrderNumberFormat())) {
                    AUTODO_F += pref.getDeliveryOrderNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getSalesReturnNumberFormat())) {
                    AUTOSR_F += pref.getSalesReturnNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getGoodsReceiptOrderNumberFormat())) {
                    AUTOGRO_F += pref.getGoodsReceiptOrderNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getPurchaseOrderNumberFormat())) {
                    AUTOPO_F += pref.getPurchaseOrderNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getCashSaleNumberFormat())) {
                    AUTOCASHSALES_F += pref.getCashSaleNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getBillingReceiptNumberFormat())) {
                    AUTOBILLINGRECEIPT_F += pref.getBillingReceiptNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getBillingCashSaleNumberFormat())) {
                    AUTOBILLINGCASHSALES_F += pref.getBillingCashSaleNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getBillingCreditNoteNumberFormat())) {
                    AUTOBILLINGCREDITMEMO_F += pref.getBillingCreditNoteNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getBillingDebitNoteNumberFormat())) {
                    AUTOBILLINGDEBITNOTE_F += pref.getBillingDebitNoteNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getBillingPaymentNumberFormat())) {
                    AUTOBILLINGPAYMENT_F += pref.getBillingPaymentNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getBillingSalesOrderNumberFormat())) {
                    AUTOBILLINGSO_F += pref.getBillingSalesOrderNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getBillingPurchaseOrderNumberFormat())) {
                    AUTOBILLINGPO_F += pref.getBillingPurchaseOrderNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getQuotationNumberFormat())) {
                    AUTOQUOTATION_F += pref.getQuotationNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getVenQuotationNumberFormat())) {
                    AUTOVENQUOTATION_F += pref.getVenQuotationNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getRequisitionNumberFormat())) {
                    AUTOREQUISITION_F += pref.getRequisitionNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getRfqNumberFormat())) {
                    AUTORFQ_F += pref.getRfqNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getProductidNumberFormat())) {
                    AUTOPRODUCTID_F += pref.getProductidNumberFormat();
                }
                if (!StringUtil.isNullOrEmpty(pref.getPurchaseReturnNumberFormat())) {
                    AUTOPR_F += pref.getPurchaseReturnNumberFormat();
                }
                obj.put(AUTOINVOICE, AUTOINVOICE_F.equalsIgnoreCase("") ? "" : (AUTOINVOICE_F.endsWith(",") ? AUTOINVOICE_F.substring(0, AUTOINVOICE_F.length() - 1) : AUTOINVOICE_F));
                obj.put(AUTOCREDITMEMO, AUTOCREDITMEMO_F.equalsIgnoreCase("") ? "" : (AUTOCREDITMEMO_F.endsWith(",") ? AUTOCREDITMEMO_F.substring(0, AUTOCREDITMEMO_F.length() - 1) : AUTOCREDITMEMO_F));
                obj.put(AUTORECEIPT, AUTORECEIPT_F.equalsIgnoreCase("") ? "" : (AUTORECEIPT_F.endsWith(",") ? AUTORECEIPT_F.substring(0, AUTORECEIPT_F.length() - 1) : AUTORECEIPT_F));
                obj.put(AUTOJOURNALENTRY, AUTOJOURNALENTRY_F.equalsIgnoreCase("") ? "" : (AUTOJOURNALENTRY_F.endsWith(",") ? AUTOJOURNALENTRY_F.substring(0, AUTOJOURNALENTRY_F.length() - 1) : AUTOJOURNALENTRY_F));
                obj.put(AUTOGOODSRECEIPT, AUTOGOODSRECEIPT_F.equalsIgnoreCase("") ? "" : (AUTOGOODSRECEIPT_F.endsWith(",") ? AUTOGOODSRECEIPT_F.substring(0, AUTOGOODSRECEIPT_F.length() - 1) : AUTOGOODSRECEIPT_F));
                obj.put(AUTODEBITNOTE, AUTODEBITNOTE_F.equalsIgnoreCase("") ? "" : (AUTODEBITNOTE_F.endsWith(",") ? AUTODEBITNOTE_F.substring(0, AUTODEBITNOTE_F.length() - 1) : AUTODEBITNOTE_F));
                obj.put(AUTOPAYMENT, AUTOPAYMENT_F.equalsIgnoreCase("") ? "" : (AUTOPAYMENT_F.endsWith(",") ? AUTOPAYMENT_F.substring(0, AUTOPAYMENT_F.length() - 1) : AUTOPAYMENT_F));
                obj.put(AUTOSO, AUTOSO_F.equalsIgnoreCase("") ? "" : (AUTOSO_F.endsWith(",") ? AUTOSO_F.substring(0, AUTOSO_F.length() - 1) : AUTOSO_F));
                obj.put(AUTOCONTRACT, AUTOCONTRACT_F.equalsIgnoreCase("") ? "" : (AUTOCONTRACT_F.endsWith(",") ? AUTOCONTRACT_F.substring(0, AUTOCONTRACT_F.length() - 1) : AUTOCONTRACT_F));
                obj.put(AUTODO, AUTODO_F.equalsIgnoreCase("") ? "" : (AUTODO_F.endsWith(",") ? AUTODO_F.substring(0, AUTODO_F.length() - 1) : AUTODO_F));
                obj.put(AUTOSR, AUTOSR_F.equalsIgnoreCase("") ? "" : (AUTOSR_F.endsWith(",") ? AUTOSR_F.substring(0, AUTOSR_F.length() - 1) : AUTOSR_F));
                obj.put(AUTOGRO, AUTOGRO_F.equalsIgnoreCase("") ? "" : (AUTOGRO_F.endsWith(",") ? AUTOGRO_F.substring(0, AUTOGRO_F.length() - 1) : AUTOGRO_F));
                obj.put(AUTOPO, AUTOPO_F.equalsIgnoreCase("") ? "" : (AUTOPO_F.endsWith(",") ? AUTOPO_F.substring(0, AUTOPO_F.length() - 1) : AUTOPO_F));
                obj.put(AUTOCASHSALES, AUTOCASHSALES_F.equalsIgnoreCase("") ? "" : (AUTOCASHSALES_F.endsWith(",") ? AUTOCASHSALES_F.substring(0, AUTOCASHSALES_F.length() - 1) : AUTOCASHSALES_F));
                obj.put(AUTOBILLINGINVOICE, AUTOBILLINGINVOICE_F.equalsIgnoreCase("") ? "" : (AUTOBILLINGINVOICE_F.endsWith(",") ? AUTOBILLINGINVOICE_F.substring(0, AUTOBILLINGINVOICE_F.length() - 1) : AUTOBILLINGINVOICE_F));
                obj.put(AUTOBILLINGRECEIPT, AUTOBILLINGRECEIPT_F.equalsIgnoreCase("") ? "" : (AUTOBILLINGRECEIPT_F.endsWith(",") ? AUTOBILLINGRECEIPT_F.substring(0, AUTOBILLINGRECEIPT_F.length() - 1) : AUTOBILLINGRECEIPT_F));
                obj.put(AUTOBILLINGCASHSALES, AUTOBILLINGCASHSALES_F.equalsIgnoreCase("") ? "" : (AUTOBILLINGCASHSALES_F.endsWith(",") ? AUTOBILLINGCASHSALES_F.substring(0, AUTOBILLINGCASHSALES_F.length() - 1) : AUTOBILLINGCASHSALES_F));
                obj.put(AUTOBILLINGCASHPURCHASE, AUTOBILLINGCASHPURCHASE_F.equalsIgnoreCase("") ? "" : (AUTOBILLINGCASHPURCHASE_F.endsWith(",") ? AUTOBILLINGCASHPURCHASE_F.substring(0, AUTOBILLINGCASHPURCHASE_F.length() - 1) : AUTOBILLINGCASHPURCHASE_F));
                obj.put(AUTOBILLINGGOODSRECEIPT, AUTOBILLINGGOODSRECEIPT_F.equalsIgnoreCase("") ? "" : (AUTOBILLINGGOODSRECEIPT_F.endsWith(",") ? AUTOBILLINGGOODSRECEIPT_F.substring(0, AUTOBILLINGGOODSRECEIPT_F.length() - 1) : AUTOBILLINGGOODSRECEIPT_F));
                obj.put(AUTOBILLINGCREDITMEMO, AUTOBILLINGCREDITMEMO_F.equalsIgnoreCase("") ? "" : (AUTOBILLINGCREDITMEMO_F.endsWith(",") ? AUTOBILLINGCREDITMEMO_F.substring(0, AUTOBILLINGCREDITMEMO_F.length() - 1) : AUTOBILLINGCREDITMEMO_F));
                obj.put(AUTOBILLINGDEBITNOTE, AUTOBILLINGDEBITNOTE_F.equalsIgnoreCase("") ? "" : (AUTOBILLINGDEBITNOTE_F.endsWith(",") ? AUTOBILLINGDEBITNOTE_F.substring(0, AUTOBILLINGDEBITNOTE_F.length() - 1) : AUTOBILLINGDEBITNOTE_F));
                obj.put(AUTOBILLINGPAYMENT, AUTOBILLINGPAYMENT_F.equalsIgnoreCase("") ? "" : (AUTOBILLINGPAYMENT_F.endsWith(",") ? AUTOBILLINGPAYMENT_F.substring(0, AUTOBILLINGPAYMENT_F.length() - 1) : AUTOBILLINGPAYMENT_F));
                obj.put(AUTOBILLINGSO, AUTOBILLINGSO_F.equalsIgnoreCase("") ? "" : (AUTOBILLINGSO_F.endsWith(",") ? AUTOBILLINGSO_F.substring(0, AUTOBILLINGSO_F.length() - 1) : AUTOBILLINGSO_F));
                obj.put(AUTOBILLINGPO, AUTOBILLINGPO_F.equalsIgnoreCase("") ? "" : (AUTOBILLINGPO_F.endsWith(",") ? AUTOBILLINGPO_F.substring(0, AUTOBILLINGPO_F.length() - 1) : AUTOBILLINGPO_F));
                obj.put(AUTOCASHPURCHASE, AUTOCASHPURCHASE_F.equalsIgnoreCase("") ? "" : (AUTOCASHPURCHASE_F.endsWith(",") ? AUTOCASHPURCHASE_F.substring(0, AUTOCASHPURCHASE_F.length() - 1) : AUTOCASHPURCHASE_F));
                obj.put(AUTOQUOTATION, AUTOQUOTATION_F.equalsIgnoreCase("") ? "" : (AUTOQUOTATION_F.endsWith(",") ? AUTOQUOTATION_F.substring(0, AUTOQUOTATION_F.length() - 1) : AUTOQUOTATION_F));
                obj.put(AUTOVENQUOTATION, AUTOVENQUOTATION_F.equalsIgnoreCase("") ? "" : (AUTOVENQUOTATION_F.endsWith(",") ? AUTOVENQUOTATION_F.substring(0, AUTOVENQUOTATION_F.length() - 1) : AUTOVENQUOTATION_F));
                obj.put(AUTOREQUISITION, AUTOREQUISITION_F.equalsIgnoreCase("") ? "" : (AUTOREQUISITION_F.endsWith(",") ? AUTOREQUISITION_F.substring(0, AUTOREQUISITION_F.length() - 1) : AUTOREQUISITION_F));
                obj.put(AUTORFQ, AUTORFQ_F.equalsIgnoreCase("") ? "" : (AUTORFQ_F.endsWith(",") ? AUTORFQ_F.substring(0, AUTORFQ_F.length() - 1) : AUTORFQ_F));
                obj.put(AUTOPACKINGDONUMBER, AUTOPACKINGDO_F.equalsIgnoreCase("") ? "" : (AUTOPACKINGDO_F.endsWith(",") ? AUTOPACKINGDO_F.substring(0, AUTOPACKINGDO_F.length() - 1) : AUTOPACKINGDO_F));
                obj.put(AUTOSHIPPINGDONUMBER, AUTOSHIPPINGDO_F.equalsIgnoreCase("") ? "" : (AUTOSHIPPINGDO_F.endsWith(",") ? AUTOSHIPPINGDO_F.substring(0, AUTOSHIPPINGDO_F.length() - 1) : AUTOSHIPPINGDO_F));
                obj.put(STANDALONE, Boolean.parseBoolean(StorageHandler.getStandalone()));
                obj.put(AUTOPRODUCTID, AUTOPRODUCTID_F.equalsIgnoreCase("") ? "" : (AUTOPRODUCTID_F.endsWith(",") ? AUTOPRODUCTID_F.substring(0, AUTOPRODUCTID_F.length() - 1) : AUTOPRODUCTID_F));
                obj.put(AUTOPR, AUTOPR_F.equalsIgnoreCase("") ? "" : (AUTOPR_F.endsWith(",") ? AUTOPR_F.substring(0, AUTOPR_F.length() - 1) : AUTOPR_F));
                obj.put(AUTOCUSTOMERID, AUTOCUSTOMERID_F.equalsIgnoreCase("") ? "" : (AUTOCUSTOMERID_F.endsWith(",") ? AUTOCUSTOMERID_F.substring(0, AUTOCUSTOMERID_F.length() - 1) : AUTOCUSTOMERID_F));
                obj.put(AUTOVENDORID, AUTOVENDORID_F.equalsIgnoreCase("") ? "" : (AUTOVENDORID_F.endsWith(",") ? AUTOVENDORID_F.substring(0, AUTOVENDORID_F.length() - 1) : AUTOVENDORID_F));
                obj.put(AUTOSALESDEBTCLAIMID, AUTOSALESDEBTCLAIMID_F.equalsIgnoreCase("") ? "" : (AUTOSALESDEBTCLAIMID_F.endsWith(",") ? AUTOSALESDEBTCLAIMID_F.substring(0, AUTOSALESDEBTCLAIMID_F.length() - 1) : AUTOSALESDEBTCLAIMID_F));
                obj.put(AUTOSALESDEBTRECOVERID, AUTOSALESDEBTRECOVERID_F.equalsIgnoreCase("") ? "" : (AUTOSALESDEBTRECOVERID_F.endsWith(",") ? AUTOSALESDEBTRECOVERID_F.substring(0, AUTOSALESDEBTRECOVERID_F.length() - 1) : AUTOSALESDEBTRECOVERID_F));
                obj.put(AUTOPURCHASEDEBTCLAIMID, AUTOPURCHASEDEBTCLAIMID_F.equalsIgnoreCase("") ? "" : (AUTOPURCHASEDEBTCLAIMID_F.endsWith(",") ? AUTOPURCHASEDEBTCLAIMID_F.substring(0, AUTOPURCHASEDEBTCLAIMID_F.length() - 1) : AUTOPURCHASEDEBTCLAIMID_F));
                obj.put(AUTOPURCHASEDEBTRECOVERID, AUTOPURCHASEDEBTRECOVERID_F.equalsIgnoreCase("") ? "" : (AUTOPURCHASEDEBTRECOVERID_F.endsWith(",") ? AUTOPURCHASEDEBTRECOVERID_F.substring(0, AUTOPURCHASEDEBTRECOVERID_F.length() - 1) : AUTOPURCHASEDEBTRECOVERID_F));
                obj.put(AUTOBUILDASSEMBLY, AUTOBUILDASSEMBLY_F.equalsIgnoreCase("") ? "" : (AUTOBUILDASSEMBLY_F.endsWith(",") ? AUTOBUILDASSEMBLY_F.substring(0, AUTOBUILDASSEMBLY_F.length() - 1) : AUTOBUILDASSEMBLY_F));
                obj.put(AUTOUNBUILDASSEMBLY, AUTOUNBUILDASSEMBLY_F.equalsIgnoreCase("") ? "" : (AUTOUNBUILDASSEMBLY_F.endsWith(",") ? AUTOUNBUILDASSEMBLY_F.substring(0, AUTOUNBUILDASSEMBLY_F.length() - 1) : AUTOUNBUILDASSEMBLY_F));
                obj.put(AUTORECONCILENUMBER, AUTORECONCILENUMBER_F.equalsIgnoreCase("") ? "" : (AUTORECONCILENUMBER_F.endsWith(",") ? AUTORECONCILENUMBER_F.substring(0, AUTORECONCILENUMBER_F.length() - 1) : AUTORECONCILENUMBER_F));
                obj.put(AUTOUNRECONCILENUMBER, AUTOUNRECONCILENUMBER_F.equalsIgnoreCase("") ? "" : (AUTOUNRECONCILENUMBER_F.endsWith(",") ? AUTOUNRECONCILENUMBER_F.substring(0, AUTOUNRECONCILENUMBER_F.length() - 1) : AUTOUNRECONCILENUMBER_F));
                obj.put(AUTOSECURITYNO, AUTOSECURITYNO_F.equalsIgnoreCase("") ? "" : (AUTOSECURITYNO_F.endsWith(",") ? AUTOSECURITYNO_F.substring(0, AUTOSECURITYNO_F.length() - 1) : AUTOSECURITYNO_F));
                obj.put(AUTOASSETGROUP, AUTOASSETGROUP_F.equalsIgnoreCase("") ? "" : (AUTOASSETGROUP_F.endsWith(",") ? AUTOASSETGROUP_F.substring(0, AUTOASSETGROUP_F.length() - 1) : AUTOASSETGROUP_F));
                obj.put(AUTOLOANREFNUMBER, AUTOLOANREFNUMBER_F.equalsIgnoreCase("") ? "" : (AUTOLOANREFNUMBER_F.endsWith(",") ? AUTOLOANREFNUMBER_F.substring(0, AUTOLOANREFNUMBER_F.length() - 1) : AUTOLOANREFNUMBER_F));
                obj.put(AUTOLABOURREFNUMBER, AUTOLABOURREFNUMBER_F.equalsIgnoreCase("") ? "" : (AUTOLABOURREFNUMBER_F.endsWith(",") ? AUTOLABOURREFNUMBER_F.substring(0, AUTOLABOURREFNUMBER_F.length() - 1) : AUTOLABOURREFNUMBER_F));
                obj.put(AUTOMRPCONTRACTNUMBER, AUTOMRPCONTRACTREFNUMBER_F.equalsIgnoreCase("") ? "" : (AUTOMRPCONTRACTREFNUMBER_F.endsWith(",") ? AUTOMRPCONTRACTREFNUMBER_F.substring(0, AUTOMRPCONTRACTREFNUMBER_F.length() - 1) : AUTOMRPCONTRACTREFNUMBER_F));
                obj.put(AUTOMACHINEID, AUTOMACHINE_F.equalsIgnoreCase("") ? "" : (AUTOMACHINE_F.endsWith(",") ? AUTOMACHINE_F.substring(0, AUTOMACHINE_F.length() - 1) : AUTOMACHINE_F));
                obj.put(AUTOJOBWORKNUMBER, AUTOJOBWORKNUMBER_F.equalsIgnoreCase("") ? "" : (AUTOJOBWORKNUMBER_F.endsWith(",") ? AUTOJOBWORKNUMBER_F.substring(0, AUTOJOBWORKNUMBER_F.length() - 1) : AUTOJOBWORKNUMBER_F));
                obj.put(AUTOWORKCENTRENUMBER, AUTOWORKCENTRENUMBER_F.equalsIgnoreCase("") ? "" : (AUTOWORKCENTRENUMBER_F.endsWith(",") ? AUTOWORKCENTRENUMBER_F.substring(0, AUTOWORKCENTRENUMBER_F.length() - 1) : AUTOWORKCENTRENUMBER_F));
                obj.put(AUTOWORKORDERNUMBER, AUTOWORKORDERNUMBER_F.equalsIgnoreCase("") ? "" : (AUTOWORKORDERNUMBER_F.endsWith(",") ? AUTOWORKORDERNUMBER_F.substring(0, AUTOWORKORDERNUMBER_F.length() - 1) : AUTOWORKORDERNUMBER_F));
                obj.put(AUTOROUTECODENUMBER, AUTOROUTECODENUMBER_F.equalsIgnoreCase("") ? "" : (AUTOROUTECODENUMBER_F.endsWith(",") ? AUTOROUTECODENUMBER_F.substring(0, AUTOROUTECODENUMBER_F.length() - 1) : AUTOROUTECODENUMBER_F));
                obj.put(AUTORG23ENTRYNUMBER, AUTORG23ENTRYNUMBER_F.equalsIgnoreCase("") ? "" : (AUTORG23ENTRYNUMBER_F.endsWith(",") ? AUTORG23ENTRYNUMBER_F.substring(0, AUTORG23ENTRYNUMBER_F.length() - 1) : AUTORG23ENTRYNUMBER_F));
                obj.put(AUTOJWO, AUTOJWO_F.equalsIgnoreCase("") ? "" : (AUTOJWO_F.endsWith(",") ? AUTOJWO_F.substring(0, AUTOJWO_F.length() - 1) : AUTOJWO_F));
                obj.put("quantitydigitafterdecimal", pref.getQuantitydigitafterdecimal());
                obj.put("amountdigitafterdecimal", pref.getAmountdigitafterdecimal());
                obj.put("unitpricedigitafterdecimal", pref.getUnitpricedigitafterdecimal());
                obj.put("uomconversionratedigitafterdecimal", pref.getUomconversionratedigitafterdecimal());
                obj.put("currencyratedigitafterdecimal", pref.getCurrencyratedigitafterdecimal());
                Constants.AMOUNT_DIGIT_AFTER_DECIMAL = pref.getAmountdigitafterdecimal();
                Constants.QUANTITY_DIGIT_AFTER_DECIMAL = pref.getQuantitydigitafterdecimal();
                Constants.UNITPRICE_DIGIT_AFTER_DECIMAL = pref.getUnitpricedigitafterdecimal();
                precisionMap.put(Constants.amountdecimalforcompany, pref.getAmountdigitafterdecimal());
                precisionMap.put(Constants.quantitydecimalforcompany, pref.getQuantitydigitafterdecimal());
                precisionMap.put(Constants.unitpricedecimalforcompany, pref.getUnitpricedigitafterdecimal());

                Constants.CompanyPreferencePrecisionMap.put(pref.getCompany().getCompanyID(), precisionMap);
                Constants.UOMCONVERSIONRATE_DIGIT_AFTER_DECIMAL = pref.getUomconversionratedigitafterdecimal();

                obj.put(EMAILINVOICE, pref.isEmailInvoice());
                obj.put(WITHOUTINVENTORY, pref.isWithoutInventory());
                obj.put("ishtmlproddesc", pref.isIshtmlproddesc());
                obj.put(WITHINVUPDATE, pref.isWithInvUpdate());
                obj.put(WITHOUTTAX1099, pref.isWithoutTax1099());
                obj.put(SETUPDONE, pref.isSetupDone());
                obj.put(COMPANYTYPE, pref.getCompanyType() == null ? "" : pref.getCompanyType().getName());
                obj.put(SERVERDATE, authHandler.getDateFormatter(request).format(new Date()));
                //ERP-24320
                obj.put(DESCRIPTIONTYPE, StringUtil.isNullOrEmpty(pref.getDescriptionType()) ? messageSource.getMessage("acc.common.memo", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage(pref.getDescriptionType(), null, RequestContextUtils.getLocale(request)));
                obj.put("gstnumber", pref.getGstNumber());
                obj.put("taxNumber", pref.getTaxNumber());
                obj.put("companyuen", pref.getCompanyUEN());
                if(pref.getIndustryCode()!=null){
                    obj.put("industryCode", pref.getIndustryCode().getID());
                }
                obj.put("iafversion", pref.getIafVersion());
                obj.put("expenseaccount", pref.getExpenseAccount() == null ? "" : pref.getExpenseAccount().getID());
                obj.put("customerdefaultaccount", pref.getCustomerdefaultaccount() == null ? "" : pref.getCustomerdefaultaccount().getID());
                obj.put("vendordefaultaccount", pref.getVendordefaultaccount() == null ? "" : pref.getVendordefaultaccount().getID());
                obj.put("liabilityaccount", pref.getLiabilityAccount() == null ? "" : pref.getLiabilityAccount().getID());
                obj.put("roundingDifferenceAccount", pref.getRoundingDifferenceAccount() == null ? "" : pref.getRoundingDifferenceAccount().getID());
                obj.put("editTransaction", pref.isEditTransaction());
                obj.put("editLinkedTransactionQuantity", pref.isEditLinkedTransactionQuantity());
                obj.put("editLinkedTransactionPrice", pref.isEditLinkedTransactionPrice());
                obj.put("shipDateConfiguration", pref.isShipDateConfiguration());
                obj.put("unitPriceConfiguration", pref.isUnitPriceConfiguration());
                obj.put("deleteTransaction", pref.isDeleteTransaction());
                obj.put("partNumber", pref.isPartNumber());
                obj.put("negativestock", pref.getNegativestock());
                obj.put("dependentField", pref.isDependentField());
                obj.put("viewDashboard", pref.getviewDashboard());
                obj.put("theme", pref.getTheme());
                obj.put("custcreditlimit", pref.getCustcreditcontrol());
                obj.put("chequeNoDuplicate", pref.getChequeNoDuplicate());
                obj.put("custminbudgetlimit", pref.getCustbudgetcontrol());
                obj.put("isAccountsWithCode", pref.isAccountsWithCode());
                obj.put("DOSettings", pref.isDOSettings());
                obj.put("GRSettings", pref.isGRSettings());
                obj.put("showLeadingZero", pref.isShowLeadingZero());
//                obj.put("billaddress", pref.getBillAddress() == null ? "" : pref.getBillAddress());
//                obj.put("shipaddress", pref.getShipAddress() == null ? "" : pref.getShipAddress());
//                obj.put(AUTOPR, pref.getPurchaseReturnNumberFormat());              
                obj.put(INVENTORYACCOUTINGINTEGRATION, pref.isInventoryAccountingIntegration());
                obj.put(UPDATEINVENTORYLEVEL, pref.isUpdateInvLevel());
                obj.put(QAAPPROVALFLOW, pref.isQaApprovalFlow());
                obj.put("editso", pref.isEditso());
                obj.put("showprodserial", pref.isShowprodserial());
                obj.put("isLocationCompulsory", pref.isIslocationcompulsory());
                obj.put("isWarehouseCompulsory", pref.isIswarehousecompulsory());
                obj.put("isRowCompulsory", pref.isIsrowcompulsory());
                obj.put("isRackCompulsory", pref.isIsrackcompulsory());
                obj.put("isBinCompulsory", pref.isIsbincompulsory());
                obj.put("isBatchCompulsory", pref.isIsBatchCompulsory());
                obj.put("isSerialCompulsory", pref.isIsSerialCompulsory());
                obj.put("isUsedSerial", accountingHandlerDAOobj.checkSerialNoFunctionalityisusedornot(pref.getCompany().getCompanyID(), "isSerialForProduct"));
                obj.put("isUsedBatch", accountingHandlerDAOobj.checkSerialNoFunctionalityisusedornot(pref.getCompany().getCompanyID(), "isBatchForProduct"));
                obj.put("isUsedLocation", accountingHandlerDAOobj.checkSerialNoFunctionalityisusedornot(pref.getCompany().getCompanyID(), "islocationforproduct"));
                obj.put("isUsedWarehouse", accountingHandlerDAOobj.checkSerialNoFunctionalityisusedornot(pref.getCompany().getCompanyID(), "iswarehouseforproduct"));
                obj.put("isUsedRow", accountingHandlerDAOobj.checkSerialNoFunctionalityisusedornot(pref.getCompany().getCompanyID(), "isrowforproduct"));
                obj.put("isUsedRack", accountingHandlerDAOobj.checkSerialNoFunctionalityisusedornot(pref.getCompany().getCompanyID(), "israckforproduct"));
                obj.put("isUsedBin", accountingHandlerDAOobj.checkSerialNoFunctionalityisusedornot(pref.getCompany().getCompanyID(), "isbinforproduct"));
                obj.put("memo", pref.isMemo());
                obj.put("approvalMail", pref.isSendapprovalmail());
                obj.put("sendmailto", pref.getApprovalEmails() == null ? "" : pref.getApprovalEmails());
                obj.put("viewDetailsPerm", pref.isViewDetailsPerm());
                obj.put("isFilterProductByCustomerCategory", pref.isFilterProductByCustomerCategory());
                obj.put("productsortingflag",pref.getProductSortingFlag());
                obj.put("doClosedStatus", pref.isDoClosedStatus());
                obj.put("isInventoryModuleUsed", accountingHandlerDAOobj.checkInventoryModuleFunctionalityIsUsedOrNot(pref.getCompany().getCompanyID()));
                obj.put("negativeStockSO", pref.getNegativeStockSO());
                obj.put("negativeStockSICS", pref.getNegativeStockSICS());
                obj.put("negativeStockPR", pref.getNegativeStockPR());
                obj.put("inventoryValuationType", pref.getInventoryValuationType());
                obj.put("updateStockAdjustmentPrice", pref.isUpdateStockAdjustmentEntries());
                obj.put("isShowMarginButton", pref.isShowMarginButton()); //Show or Hide Margin Button in Invoice/Sales/Quotation create form //ERM-76
                if (pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                    obj.put("cogsAcc", pref.getCogsaccount() != null ? pref.getCogsaccount().getID() : "");
                    obj.put("inventoryAcc", pref.getInventoryaccount() != null ? pref.getInventoryaccount().getID() : "");
                    obj.put("stockAdjustmentAcc", pref.getStockadjustmentaccount() != null ? pref.getStockadjustmentaccount().getID() : "");
                }
                obj.put(QA_APPROVAL_FLOW_IN_DO, pref.isQaApprovalFlowInDO());
            }

            if (request.getAttribute("extraCompanyPreferences") != null) {
                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) request.getAttribute("extraCompanyPreferences");
                obj.put("isDeferredRevenueRecognition", extraCompanyPreferences.isDeferredRevenueRecognition());
                obj.put("salesAccount", extraCompanyPreferences.getSalesAccount() == null ? "" : extraCompanyPreferences.getSalesAccount());
                obj.put("loandisbursementaccount", extraCompanyPreferences.getLoanAccount() == null ? "" : extraCompanyPreferences.getLoanAccount());
                obj.put("loaninterestaccount", extraCompanyPreferences.getLoanInterestAccount() == null ? "" : extraCompanyPreferences.getLoanInterestAccount());
                obj.put("gstaccountforbaddebt", extraCompanyPreferences.getGstAccountForBadDebt() == null ? "" : extraCompanyPreferences.getGstAccountForBadDebt());
                obj.put("gstbaddebtreleifaccount", extraCompanyPreferences.getGstBadDebtsReleifAccount() == null ? "" : extraCompanyPreferences.getGstBadDebtsReleifAccount());
                obj.put("gstbaddebtrecoveraccount", extraCompanyPreferences.getGstBadDebtsRecoverAccount() == null ? "" : extraCompanyPreferences.getGstBadDebtsRecoverAccount());
                //ERP-10400, For Purchase
                obj.put("gstbaddebtreleifpurchaseaccount", extraCompanyPreferences.getGstBadDebtsReleifPurchaseAccount() == null ? "" : extraCompanyPreferences.getGstBadDebtsReleifPurchaseAccount());
                obj.put("gstbaddebtrecoverpurchaseaccount", extraCompanyPreferences.getGstBadDebtsRecoverPurchaseAccount() == null ? "" : extraCompanyPreferences.getGstBadDebtsRecoverPurchaseAccount());
                obj.put("gstbaddebtsuspenseaccount", extraCompanyPreferences.getGstBadDebtsSuspenseAccount() == null ? "" : extraCompanyPreferences.getGstBadDebtsSuspenseAccount());
                obj.put("inputtaxadjustmentaccount", extraCompanyPreferences.getInputTaxAdjustmentAccount() == null ? "" : extraCompanyPreferences.getInputTaxAdjustmentAccount());
                obj.put("taxCgaMalaysian", extraCompanyPreferences.getTaxAllowForCgaMalaysian() == null ? "" : extraCompanyPreferences.getTaxAllowForCgaMalaysian());
                obj.put("outputtaxadjustmentaccount", extraCompanyPreferences.getOutputTaxAdjustmentAccount() == null ? "" : extraCompanyPreferences.getOutputTaxAdjustmentAccount());
                obj.put("freeGiftJEAccount", extraCompanyPreferences.getFreeGiftJEAccount() == null ? "" : extraCompanyPreferences.getFreeGiftJEAccount());
                obj.put("salesRevenueRecognitionAccount", extraCompanyPreferences.getSalesRevenueRecognitionAccount() == null ? "" : extraCompanyPreferences.getSalesRevenueRecognitionAccount());
                obj.put("showAllAccount", extraCompanyPreferences.isShowAllAccount());
                obj.put("showChildAccountsInTb", extraCompanyPreferences.isShowChildAccountsInTb());
                obj.put("showChildAccountsInGl", extraCompanyPreferences.isShowChildAccountsInGl());
                obj.put("showChildAccountsInPnl", extraCompanyPreferences.isShowChildAccountsInPnl());
                obj.put("showChildAccountsInBS", extraCompanyPreferences.isShowChildAccountsInBS());
                obj.put("showallaccountsinbs", extraCompanyPreferences.isShowallaccountsinbs());
                obj.put("showAllAccountInGl", extraCompanyPreferences.isShowAllAccountInGl());
                obj.put("showimport", extraCompanyPreferences.isShowimport());
                obj.put("showAllAccountsInPnl", extraCompanyPreferences.isShowAllAccountsInPnl());
                obj.put("isnegativestockforlocwar", extraCompanyPreferences.isIsnegativestockforlocwar());
                obj.put("productPriceinMultipleCurrency", extraCompanyPreferences.isProductPriceinMultipleCurrency());
                obj.put("stockValuationFlag", extraCompanyPreferences.isStockValuationFlag());
                obj.put("leaseManagementFlag", extraCompanyPreferences.isLeaseManagementFlag());
                obj.put("consignmentSalesManagementFlag", extraCompanyPreferences.isConsignmentSalesManagementFlag());
                obj.put("consignmentPurchaseManagementFlag", extraCompanyPreferences.isConsignmentPurchaseManagementFlag());
                obj.put("systemManagementFlag", extraCompanyPreferences.isSystemManagementFlag());
                obj.put("masterManagementFlag", extraCompanyPreferences.isMasterManagementFlag());
                obj.put("generalledgerManagementFlag", extraCompanyPreferences.isGeneralledgerManagementFlag());
                obj.put("accountsreceivablesalesFlag", extraCompanyPreferences.isAccountsreceivablesalesFlag());
                obj.put("accountpayableManagementFlag", extraCompanyPreferences.isAccountpayableManagementFlag());
                obj.put("securityGateEntryFlag", extraCompanyPreferences.isSecurityGateEntryFlag());
                obj.put("assetManagementFlag", extraCompanyPreferences.isAssetManagementFlag());
                obj.put("statutoryManagementFlag", extraCompanyPreferences.isStatutoryManagementFlag());
                obj.put("miscellaneousManagementFlag", extraCompanyPreferences.isMiscellaneousManagementFlag());
                obj.put("onlyBaseCurrency", extraCompanyPreferences.isOnlyBaseCurrency());
                obj.put("packingdolist", extraCompanyPreferences.isPackingdolist());
                obj.put("versionslist", extraCompanyPreferences.isVersionslist());
                obj.put("activateProductComposition", extraCompanyPreferences.isActivateProductComposition());
                obj.put("noOfDaysforValidTillField", extraCompanyPreferences.getNoOfDaysforValidTillField());
                obj.put("isSalesOrderCreatedForCustomer", extraCompanyPreferences.isSalesOrderCreatedForCustomer());
                obj.put("isOutstandingInvoiceForCustomer", extraCompanyPreferences.isOutstandingInvoiceForCustomer());
                obj.put("isMinMaxOrdering", extraCompanyPreferences.isMinMaxOrdering());
                obj.put("blockPOcreationwithMinValue", extraCompanyPreferences.isBlockPOcreationWithMinPricevalue());
                obj.put("recurringDeferredRevenueRecognition", extraCompanyPreferences.isRecurringDeferredRevenueRecognition());
                obj.put("autoPopulateMappedProduct", extraCompanyPreferences.isAutoPopulateMappedProduct());
                obj.put("showAutoGeneratedChequeNumber", extraCompanyPreferences.isShowAutoGeneratedChequeNumber());
                obj.put("activateIBG", extraCompanyPreferences.isActivateIBG());
                obj.put("activateIBGCollection", extraCompanyPreferences.isActivateIBGCollection());
                obj.put("activateGroupCompaniesFlag", extraCompanyPreferences.isActivateGroupCompaniesFlag());
                obj.put("isMultiGroupCompanyParentFlag", false);
                if (extraCompanyPreferences.isActivateGroupCompaniesFlag()) {
                    KwlReturnObject multiFlag = accCompanyPreferencesObj.getMultiGroupCompanyList(extraCompanyPreferences.getId());
                    if (multiFlag.getEntityList() != null && multiFlag.getEntityList().size()>0) {
                        obj.put("isMultiGroupCompanyParentFlag",true);
                    }
                }
                
                obj.put("uobendtoendid", extraCompanyPreferences.getEndToEndId());
                obj.put("uobpurposecode", extraCompanyPreferences.getPurposeCode());
                obj.put("originatingBankCodeForOCBCBank", extraCompanyPreferences.getOriginatingBankCodeForOCBCBank());
                obj.put("originatingBICCodeForUOBBank", extraCompanyPreferences.getOriginatingBICCodeForUOBBank());
                obj.put("gstEffectiveDate", (extraCompanyPreferences.getGstEffectiveDate() != null) ? authHandler.getDateOnlyFormat(request).format(extraCompanyPreferences.getGstEffectiveDate()) : "");
                obj.put("enableGST", extraCompanyPreferences.isEnableGST());
                obj.put(Constants.isMultiEntity, extraCompanyPreferences.isIsMultiEntity());
                obj.put(Constants.isDimensionCreated, extraCompanyPreferences.isIsDimensionCreated());
                obj.put("activateSalesContrcatManagement", (extraCompanyPreferences != null) ? extraCompanyPreferences.isActivateSalesContrcatManagement() : false);
                obj.put("activateLoanManagementFlag", (extraCompanyPreferences != null) ? extraCompanyPreferences.isActivateLoanManagement() : false);
                obj.put("wipAccountTypeId", extraCompanyPreferences.getWipAccountTypeId());
                obj.put("jobworkrecieverflow", extraCompanyPreferences.isJobworkrecieverflow());
                obj.put("cpAccountType", extraCompanyPreferences.getCpAccountTypeId());
                obj.put("wipAccountPrefix", extraCompanyPreferences.getWipAccountPrefix());
                obj.put("cpAccountPrefix", extraCompanyPreferences.getCpAccountPrefix());
                obj.put("DashBoardImageFlag", extraCompanyPreferences.isDashBordImageFlag());
                obj.put("isLMSIntegration", extraCompanyPreferences.isLMSIntegration());
                obj.put("ActivateFixedAssetModule", extraCompanyPreferences.isAssetSetingActivation());
                obj.put("allowToPostOpeningDepreciation", extraCompanyPreferences.isAllowToPostOpeningDepreciation());
                obj.put("AllowZeroQuantityForDO", extraCompanyPreferences.isAllowZeroQuantityForProduct());
                obj.put("AllowZeroQuantityInPI", extraCompanyPreferences.isAllowZeroQuantityInPI());
                obj.put("AllowZeroQuantityInPO", extraCompanyPreferences.isAllowZeroQuantityInPO());
                obj.put("AllowZeroQuantityInQuotation", extraCompanyPreferences.isAllowZeroQuantityInQuotation());
                obj.put("AllowZeroQuantityInSI", extraCompanyPreferences.isAllowZeroQuantityInSI());
                obj.put("isNewGSTOnly", extraCompanyPreferences.isIsNewGST());
                obj.put("AllowZeroQuantityInSO", extraCompanyPreferences.isAllowZeroQuantityInSO());
                obj.put("AllowZeroQuantityInSR", extraCompanyPreferences.isAllowZeroQuantityInSR());
                obj.put("AllowZeroQuantityInPR", extraCompanyPreferences.isAllowZeroQuantityInPR());
                obj.put("AllowZeroQuantityInGRO", extraCompanyPreferences.isAllowZeroQuantityInGRO());
                obj.put("AllowZeroQuantityInVQ", extraCompanyPreferences.isAllowZeroQuantityInVQ());
                obj.put("depreciationCalculationType", extraCompanyPreferences.getAssetDepreciationCalculationType());
                obj.put("depreciationCalculationBasedOn", extraCompanyPreferences.getAssetDepreciationCalculationBasedOn());
                obj.put("UomSchemaType", extraCompanyPreferences.getUomSchemaType());
                obj.put("deliveryPlanner", extraCompanyPreferences.isDeliveryPlanner());
                obj.put("autoPopulateFieldsForDeliveryPlanner", extraCompanyPreferences.isAutoPopulateFieldsForDeliveryPlanner());
                obj.put("priceConfigurationAlert", extraCompanyPreferences.isPriceConfigurationAlert());
                obj.put("retainExchangeRate", extraCompanyPreferences.isRetainExchangeRate());
                obj.put("activateCRMIntegration", extraCompanyPreferences.isActivateCRMIntegration());
                obj.put("integrationWithPOS", extraCompanyPreferences.isIsPOSIntegration());
                obj.put("isCloseRegisterMultipleTimes", extraCompanyPreferences.isIsCloseRegisterMultipleTimes());
                obj.put("isallowCustVenCodeEditing", extraCompanyPreferences.isIsallowCustVenCodeEditing());
                obj.put("manyCreditDebit", extraCompanyPreferences.isManyCreditDebit());
                obj.put("customerForPOS", extraCompanyPreferences.getCustomerForPOS());
                obj.put("vendorForPOS", extraCompanyPreferences.getVendorForPOS());
                obj.put("generateBarcodeParm", extraCompanyPreferences.isGenerateBarcodeParm());
                obj.put("SKUFieldParm", extraCompanyPreferences.isSKUFieldParm());
                obj.put("SKUFieldRename", extraCompanyPreferences.getSKUFieldRename() == null ? "" : extraCompanyPreferences.getSKUFieldRename());
                obj.put("barcodetype", extraCompanyPreferences.getBarcodetype());
                obj.put("productPricingOnBands", extraCompanyPreferences.isProductPricingOnBands());
                obj.put("productPricingOnBandsForSales", extraCompanyPreferences.isProductPricingOnBandsForSales());
                obj.put("barcodeDpi", extraCompanyPreferences.getBarcodeDPI());
                obj.put("barcodeHeight", extraCompanyPreferences.getBarcodeHeight());
                obj.put("productOptimizedFlag", extraCompanyPreferences.getProductOptimizedFlag());
                obj.put("defaultmailsenderFlag", extraCompanyPreferences.getDefaultmailsenderFlag());
                obj.put("downloadglprocessflag", extraCompanyPreferences.getDownloadglprocessflag());
                obj.put("downloadDimPLprocess", extraCompanyPreferences.getDownloadDimPLprocessflag());
                obj.put("downloadSOAprocess", extraCompanyPreferences.getDownloadSOAprocessflag());
                obj.put("custvenloadtype", extraCompanyPreferences.getCustvenloadtype());
                obj.put("proddiscripritchtextboxflag", extraCompanyPreferences.getProddiscripritchtextboxflag());
                obj.put("isMovementWarehouseMapping", extraCompanyPreferences.isMovementWarehouseMapping());
                obj.put("salesTypeFlag", extraCompanyPreferences.isSalesTypeFlag());
                obj.put("purchaseTypeFlag", extraCompanyPreferences.isPurchaseTypeFlag());
                obj.put("activatebudgetingforPR", extraCompanyPreferences.isActivatebudgetingforPR());
                obj.put("budgetType", extraCompanyPreferences.getBudgetType());
                obj.put("budgetFreqType", extraCompanyPreferences.getBudgetFreqType());
                obj.put("budgetwarnblock", extraCompanyPreferences.getBudgetwarnblock());
                obj.put(Constants.termsincludegst, extraCompanyPreferences.isTermsincludegst());
                obj.put("activateInventoryTab", extraCompanyPreferences.isActivateInventoryTab());
                obj.put("activateCycleCount", extraCompanyPreferences.isActivateCycleCount());
                obj.put("activateQAApprovalFlow", extraCompanyPreferences.isActivateQAApprovalFlow());
                obj.put("activateProfitMargin", extraCompanyPreferences.isActivateProfitMargin());
                obj.put("activateToDateforExchangeRates", extraCompanyPreferences.isActivateToDateforExchangeRates());
                obj.put("activateToBlockSpotRate", extraCompanyPreferences.isActivateToBlockSpotRate());
                obj.put("hierarchicalDimensions", extraCompanyPreferences.isHierarchicalDimensions());
                obj.put("activateimportForJE", extraCompanyPreferences.isActivateimportForJE());
                obj.put("activateCRblockingWithoutStock", extraCompanyPreferences.isActivateCRblockingWithoutStock());
                obj.put("activatefromdateToDate", extraCompanyPreferences.isActivatefromdateToDate());
                obj.put("isDuplicateItems", extraCompanyPreferences.isDuplicateItems());
                obj.put("pricePrintType", extraCompanyPreferences.getPricePrinttype());
                obj.put("barcdTopMargin", extraCompanyPreferences.getBarcdTopMargin());
                obj.put("barcdLeftMargin", extraCompanyPreferences.getBarcdLeftMargin());
                obj.put("barcdLabelHeight", extraCompanyPreferences.getBarcdLabelHeight());
                obj.put("priceTranslateY", extraCompanyPreferences.getPriceTranslateY());
                obj.put("priceTranslateX", extraCompanyPreferences.getPriceTranslateX());
                obj.put("priceFontSize", extraCompanyPreferences.getPriceFontSize());
                obj.put("pricePrefix", extraCompanyPreferences.getPricePrefix() == null ? "" : extraCompanyPreferences.getPricePrefix());
                obj.put("generateBarcodeWithPriceParm", extraCompanyPreferences.isGenerateBarcodeWithPriceParm());
                obj.put("isCurrencyCode", extraCompanyPreferences.isCurrencyCode());
                obj.put("activateMRPModule", (extraCompanyPreferences != null) ? extraCompanyPreferences.isActivateMRPModule(): false);
                obj.put("isAutoRefershReportonSave", extraCompanyPreferences.isIsAutoRefershReportOnSave());
                //In the responce of save company preferences, Only India country specific use.
                obj.put("CompanyVATNumber", extraCompanyPreferences.getVatNumber());
                obj.put("CompanyCSTNumber", extraCompanyPreferences.getCstNumber());
                obj.put("returncode", extraCompanyPreferences.getReturncode());
                obj.put("cstregistrationdate", extraCompanyPreferences.getCstregistrationdate());
                obj.put("dateofregistration", extraCompanyPreferences.getDateofregistration());
                if(extraCompanyPreferences != null && extraCompanyPreferences.getCompany().getCountry().getID().equals(Constants.INDONESIAN_COUNTRYID)){
                    obj.put("CompanyNPWPNumber", extraCompanyPreferences.getPanNumber());
                }else{
                    obj.put("CompanyPANNumber", extraCompanyPreferences.getPanNumber());
                }
                obj.put("CompanyServiceTaxRegNumber", extraCompanyPreferences.getServiceTaxRegNo());
                obj.put("CompanyTANNumber", extraCompanyPreferences.getTanNumber());
                obj.put("CompanyECCNumber", extraCompanyPreferences.getEccNumber());
                obj.put("AllowToMapAccounts", extraCompanyPreferences.isAllowToMapAccounts());
                // Only For Indian Company TDS flow.
                obj.put("isTDSApplicable", extraCompanyPreferences.isTDSapplicable());
                obj.put("isSTApplicable", extraCompanyPreferences.isSTapplicable());
                obj.put("headofficetanno", extraCompanyPreferences.getHeadOfficeTANno());
                obj.put("commissioneratecode", extraCompanyPreferences.getCommissionerateCode());
                obj.put("commissioneratename", extraCompanyPreferences.getCommissionerateName());
                obj.put("servicetaxregno", extraCompanyPreferences.getServiceTaxRegNo());
                obj.put("divisioncode", extraCompanyPreferences.getDivisionCode());
                obj.put("isExciseApplicable", extraCompanyPreferences.isExciseApplicable());
                obj.put("registrationType", extraCompanyPreferences.getRegistrationType());
                obj.put("manufacturerType", extraCompanyPreferences.getManufacturerType());
                obj.put("unitname", extraCompanyPreferences.getUnitname());
                obj.put("tariffName", extraCompanyPreferences.getTariffName());
                obj.put("HSNCode", extraCompanyPreferences.getHSNCode());
                obj.put("reportingUOM", extraCompanyPreferences.getReportingUOM());
                obj.put("exciseRate", extraCompanyPreferences.getExciseRate());
                obj.put("exciseMethod", extraCompanyPreferences.getExciseMethod());
                obj.put("exciseTariffdetails", extraCompanyPreferences.isExciseTariffdetails());
                
                //VAT Accounts at Company Preferences
                obj.put("vatPayableAcc", extraCompanyPreferences.getVatPayableAcc());
                obj.put("vatInCreditAvailAcc", extraCompanyPreferences.getVatInCreditAvailAcc());
                obj.put("CSTPayableAcc", extraCompanyPreferences.getCSTPayableAcc());
                obj.put("excisePayableAcc", extraCompanyPreferences.getExcisePayableAcc());
                obj.put("pmtMethod", extraCompanyPreferences.getPaymentMethodId());
                
                //Excise Accounts at Company Preferences
                obj.put("exciseDutyAdvancePaymentaccount", extraCompanyPreferences.getExciseDutyAdvancePaymentaccount());
                
                //Service Tax Payable & Advance Payment Account
                obj.put("STPayableAcc", extraCompanyPreferences.getSTPayableAcc());
                obj.put("STAdvancePaymentaccount", extraCompanyPreferences.getSTAdvancePaymentaccount());
                
                obj.put("salesaccountidcompany", extraCompanyPreferences.getSalesaccountidcompany());
                obj.put("salesretaccountidcompany", extraCompanyPreferences.getSalesretaccountidcompany());
                obj.put("purchaseretaccountidcompany", extraCompanyPreferences.getPurchaseretaccountidcompany());
                obj.put("purchaseaccountidcompany", extraCompanyPreferences.getPurchaseaccountidcompany());
                obj.put("interstatepuracccformid", extraCompanyPreferences.getInterstatepuracccformid());
                obj.put("interstatepuraccid", extraCompanyPreferences.getInterstatepuraccid());
                obj.put("interstatepuraccreturncformid", extraCompanyPreferences.getInterstatepuraccreturncformid());
                obj.put("interstatepurreturnaccid", extraCompanyPreferences.getInterstatepurreturnaccid());
                obj.put("interstatesalesacccformid", extraCompanyPreferences.getInterstatesalesacccformid());
                obj.put("interstatesalesaccid", extraCompanyPreferences.getInterstatesalesaccid());
                obj.put("interstatesalesaccreturncformid", extraCompanyPreferences.getInterstatesalesaccreturncformid());
                obj.put("interstatesalesreturnaccid", extraCompanyPreferences.getInterstatesalesreturnaccid());
                
                obj.put("excisejurisdictiondetails", extraCompanyPreferences.isExciseJurisdictiondetails());
                obj.put("exciseMultipleUnit", extraCompanyPreferences.isExciseMultipleUnit());
                obj.put("excisecommissioneratecode", extraCompanyPreferences.getExciseCommissionerateCode());
                obj.put("excisecommissioneratename", extraCompanyPreferences.getExciseCommissionerateName());
                obj.put("excisedivisioncode", extraCompanyPreferences.getExciseDivisionCode());
                obj.put("exciserangecode", extraCompanyPreferences.getExciseRangeCode());
                obj.put("rangecode", extraCompanyPreferences.getRangeCode());
                obj.put("TDSincometaxcircle", extraCompanyPreferences.getTdsIncomeTaxCircle());
                obj.put("TDSrespperson", extraCompanyPreferences.getTdsRespPerson());
                obj.put("TDSresppersonfathersname", extraCompanyPreferences.getTdsRespPersonFatherName());
                obj.put("TDSresppersondesignation", extraCompanyPreferences.getTdsRespPersonDesignation());
                obj.put("deductortype", extraCompanyPreferences.getDeductorType());
                //Data relating with the Barcode Product Name Display
                obj.put("pnamePrintType", extraCompanyPreferences.getPnamePrintType());
                obj.put("pnameTranslateY", extraCompanyPreferences.getPnameTranslateY());
                obj.put("pnameTranslateX", extraCompanyPreferences.getPnameTranslateX());
                obj.put("pnameFontSize", extraCompanyPreferences.getPnameFontSize());
                obj.put("pnamePrefix", extraCompanyPreferences.getPnamePrefix() == null ? "" : extraCompanyPreferences.getPnamePrefix());
                obj.put("generateBarcodeWithPnameParm", extraCompanyPreferences.isGenerateBarcodeWithPnameParm());
                
                //Data relating with the Barcode along with Product ID
                obj.put("pidPrintType", extraCompanyPreferences.getPidPrintType());
                obj.put("pidTranslateY", extraCompanyPreferences.getPidTranslateY());
                obj.put("pidTranslateX", extraCompanyPreferences.getPidTranslateX());
                obj.put("pidFontSize", extraCompanyPreferences.getPidFontSize());
                obj.put("pidPrefix", extraCompanyPreferences.getPidPrefix() == null ? "" : extraCompanyPreferences.getPidPrefix());
                obj.put("generateBarcodeWithPidParm", extraCompanyPreferences.isGenerateBarcodeWithPidParm());
                
                
                //Data relating with the Barcode along with Product MRP
                obj.put("mrpPrintType", extraCompanyPreferences.getMrpPrintType());
                obj.put("mrpTranslateY", extraCompanyPreferences.getMrpTranslateY());
                obj.put("mrpTranslateX", extraCompanyPreferences.getMrpTranslateX());
                obj.put("mrpFontSize", extraCompanyPreferences.getMrpFontSize());
                obj.put("mrpPrefix", extraCompanyPreferences.getMrpPrefix() == null ? "" : extraCompanyPreferences.getMrpPrefix());
                obj.put("generateBarcodeWithmrpParm", extraCompanyPreferences.isGenerateBarcodeWithMrpParm());
                
                obj.put("inspectionStore", extraCompanyPreferences.getInspectionStore() == null ? "" : extraCompanyPreferences.getInspectionStore());
                obj.put("repairStore", extraCompanyPreferences.getRepairStore() == null ? "" : extraCompanyPreferences.getRepairStore());
                obj.put("packinglocation", extraCompanyPreferences.getPackinglocation() == null ? "" : extraCompanyPreferences.getPackinglocation());
                obj.put("packingstore", extraCompanyPreferences.getPackingstore() == null ? "" : extraCompanyPreferences.getPackingstore());
                obj.put("vendorjoborderstore", extraCompanyPreferences.getVendorjoborderstore()== null ? "" : extraCompanyPreferences.getVendorjoborderstore());
                obj.put("pickpackship", extraCompanyPreferences.isPickpackship());
                
                obj.put(IntegrationConstants.upsIntegration, extraCompanyPreferences.isUpsIntegration());
                obj.put(IntegrationConstants.irasIntegration, extraCompanyPreferences.isIRASIntegration());
                boolean isAvalaraIntegration = extraCompanyPreferences.isAvalaraIntegration();
                obj.put(IntegrationConstants.avalaraIntegration, isAvalaraIntegration);
                if (isAvalaraIntegration) {
                    JSONObject paramJobj = new JSONObject();
                    paramJobj.put(Constants.companyKey, company.getCompanyID());
                    paramJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                    JSONObject avalaraConfigJobj = integrationCommonService.getIntegrationConfig(paramJobj);
                    obj.put("avalaraTaxCalculation", StringUtil.equal(avalaraConfigJobj.optString("taxCalculation", null), "on"));
                    obj.put("avalaraTaxCommitting", StringUtil.equal(avalaraConfigJobj.optString("taxCommitting", null), "on"));
                    obj.put("avalaraAddressValidation", StringUtil.equal(avalaraConfigJobj.optString("addressValidation", null), "on"));
                }
                
                /**
                 * Prepare and add Third Party Integration data
                 */
                JSONObject paramsJobj = new JSONObject();
                String countryid = sessionHandlerImpl.getCountryId(request);
                paramsJobj.put(Constants.COUNTRY_ID, countryid);
                KwlReturnObject kwlObj = accCompanyPreferencesObj.getIntegrationPartyCountryMapping(paramsJobj);
                JSONObject integrationPartiesData = new JSONObject();
                if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                    List<IntegrationPartyCountryMapping> list = kwlObj.getEntityList();
                    for (IntegrationPartyCountryMapping ipcm : list) {
                        if (ipcm != null) {
                            JSONObject tempJobj = new JSONObject();
                            IntegrationParty ip = ipcm.getIntegrationParty();
                            tempJobj.put(IntegrationConstants.integrationPartyIdKey, ip.getID());
                            tempJobj.put(IntegrationConstants.integrationPartyNameKey, ip.getIntegrationPartyName());
                            tempJobj.put(IntegrationConstants.integrationPartyHiddenNameKey, ip.getIntegrationPartyHiddenName());
                            tempJobj.put(IntegrationConstants.integrationConfigJson, ip.getIntegrationConfigJson());
                            tempJobj.put(IntegrationConstants.integrationGlobalSettingsJson, ip.getIntegrationGlobalSettingsJson());
                            integrationPartiesData.put(String.valueOf(ip.getID()), tempJobj);
                        }
                    }
                }
                obj.put(IntegrationConstants.integrationPartiesData, integrationPartiesData);
                
                obj.put("showPivotInCustomReports", extraCompanyPreferences.isShowPivotInCustomReports());
                obj.put(Constants.barcodeScanning, extraCompanyPreferences.isBarcodeScanning());
                obj.put("interloconpick", extraCompanyPreferences.isInterloconpick());
                obj.put("useremails", extraCompanyPreferences.getUserEmails() == null ? "" : extraCompanyPreferences.getUserEmails());
                obj.put("sendimportmailto", extraCompanyPreferences.getSendImportMailTo() == null ? "" : extraCompanyPreferences.getSendImportMailTo());
                obj.put("defaultWarehouse", extraCompanyPreferences.getDefaultWarehouse() == null ? "" : extraCompanyPreferences.getDefaultWarehouse());
                obj.put("lmsliabilityAccount", extraCompanyPreferences.getLiabilityAccountForLMS() == null ? "" : extraCompanyPreferences.getLiabilityAccountForLMS());
                obj.put("showVendorUpdate", extraCompanyPreferences.isShowVendorUpdateFlag());
                obj.put("enablevatcst", extraCompanyPreferences.isEnableVatCst());
                obj.put("assessmentcircle", extraCompanyPreferences.getAssessmentCircle());
                obj.put("division", extraCompanyPreferences.getDivision());
                obj.put("areacode", extraCompanyPreferences.getAreaCode());
                obj.put("importexportcode", extraCompanyPreferences.getImportExportCode());
                obj.put("authorizedby", extraCompanyPreferences.getAuthorizedBy());
                obj.put("authorizedperson", extraCompanyPreferences.getAuthorizedPerson());
                obj.put("statusordesignation", extraCompanyPreferences.getStatusorDesignation());
                obj.put("place", extraCompanyPreferences.getPlace());
                obj.put("vattincomposition", extraCompanyPreferences.getVatTinComposition());
                obj.put("vattinregular", extraCompanyPreferences.getVatTinRegular());
                obj.put("localsalestaxnumber", extraCompanyPreferences.getLocalSalesTaxNumber());
                obj.put("interstatesalestaxnumber", extraCompanyPreferences.getInterStateSalesTaxNumber());
                obj.put("typeofdealer", extraCompanyPreferences.getTypeOfDealer());
                obj.put("bankid", extraCompanyPreferences.getBankId());
                obj.put("applicabilityofvat", (extraCompanyPreferences.getApplicabilityOfVat() != null) ? authHandler.getDateOnlyFormat(request).format(extraCompanyPreferences.getApplicabilityOfVat()) : "");
                obj.put("showCustomerUpdate", extraCompanyPreferences.isShowCustomerUpdateFlag());
                obj.put("showProductUpdate", extraCompanyPreferences.isShowProductUpdateFlag());
                obj.put("profitLossAccountId", extraCompanyPreferences.getProfitLossAccountId() != null ? extraCompanyPreferences.getProfitLossAccountId() : "");
                obj.put("openingStockAccountId", extraCompanyPreferences.getOpeningStockAccountId() != null ? extraCompanyPreferences.getOpeningStockAccountId() : "");
                obj.put("closingStockAccountId", extraCompanyPreferences.getClosingStockAccountId() != null ? extraCompanyPreferences.getClosingStockAccountId() : "");
                obj.put("stockInHandAccountId", extraCompanyPreferences.getStockInHandAccountId() != null ? extraCompanyPreferences.getStockInHandAccountId() : "");
                obj.put("isBaseUOMRateEdit", extraCompanyPreferences.isBaseUOMRateEdit());
                obj.put("allowZeroUntiPriceForProduct", extraCompanyPreferences.isAllowZeroUntiPriceForProduct());
                obj.put("allowZeroQuantityForProduct", extraCompanyPreferences.isAllowZeroQuantityForProduct());
                obj.put("allowZeroQuantityInQuotation", extraCompanyPreferences.isAllowZeroQuantityInQuotation());
                obj.put("negativeStockFormulaSO", extraCompanyPreferences.getNegativestockformulaso());
                obj.put("negativeStockFormulaSI", extraCompanyPreferences.getNegativestockformulasi());
                obj.put("requestApprovalFlow", extraCompanyPreferences.isRequestApprovalFlow());
                obj.put("remitpaymentto", extraCompanyPreferences.getRemitpaymentto() == null ? "" : extraCompanyPreferences.getRemitpaymentto());
                obj.put("isAddressFromVendorMaster", extraCompanyPreferences.isIsAddressFromVendorMaster());
                obj.put("autoPopulateDeliveredQuantity", extraCompanyPreferences.isAutoPopulateDeliveredQuantity());
                obj.put("defaultTemplateLogoFlag", extraCompanyPreferences.isDefaultTemplateLogoFlag());
                obj.put("enableLinkToSelWin", extraCompanyPreferences.isEnableLinkToSelWin());
                obj.put("showBulkInvoices", extraCompanyPreferences.isShowBulkInvoices());
                obj.put("showBulkInvoicesFromSO", extraCompanyPreferences.isShowBulkInvoicesFromSO());
                obj.put("showBulkDOFromSO", extraCompanyPreferences.isShowBulkDOFromSO());
                obj.put("isAllowQtyMoreThanLinkedDoc", extraCompanyPreferences.isIsAllowQtyMoreThanLinkedDoc());
                obj.put("isAllowQtyMoreThanLinkedDocCross", extraCompanyPreferences.isIsAllowQtyMoreThanLinkedDocCross());
                obj.put("invoicesWriteOffAccount", extraCompanyPreferences.getWriteOffAccount() != null ? extraCompanyPreferences.getWriteOffAccount() : "");
                obj.put("enablesalespersonAgentFlow", extraCompanyPreferences.isEnablesalespersonAgentFlow());
                obj.put("viewallexcludecustomerwithoutsalesperson", extraCompanyPreferences.isViewAllExcludeCustomer() );
                obj.put("BuildAssemblyApprovalFlow", extraCompanyPreferences.isBuildAssemblyApprovalFlow() );
                obj.put("isPRmandatory", extraCompanyPreferences.isIsPRmandatory());
                obj.put("splitOpeningBalanceAmount", extraCompanyPreferences.isSplitOpeningBalanceAmount());
                obj.put("defaultsequenceformatforrecinv", extraCompanyPreferences.isDefaultsequenceformatforrecinv());
                obj.put("pickaddressfrommaster", extraCompanyPreferences.isPickAddressFromMaster());
                obj.put("gstIncomeGroup", extraCompanyPreferences.isGstIncomeGroup());
                obj.put("paymentMethodAsCard", extraCompanyPreferences.isPaymentMethodAsCard());
                obj.put("jobOrderItemFlow", extraCompanyPreferences.isJobOrderItemFlow());
                obj.put(Constants.PRODUCT_SEARCH_FLAG, extraCompanyPreferences.getProductSearchingFlag());
                obj.put("usersVisibilityFlow", extraCompanyPreferences.isUsersVisibilityFlow());
                obj.put("usersspecificinfoFlow", extraCompanyPreferences.isusersspecificinfoFlow());
                obj.put("jobWorkOutFlow", extraCompanyPreferences.isJobWorkOutFlow());
                obj.put("salesCommissionReportMode", extraCompanyPreferences.getSalesCommissionReportMode());
                obj.put("salesorderreopen", extraCompanyPreferences.isSalesorderreopen());
                obj.put("isActiveLandingCostOfItem", extraCompanyPreferences.isActivelandingcostofitem());
                obj.put("includeAmountInLimitSI", extraCompanyPreferences.isIncludeAmountInLimitSI());
                obj.put("includeAmountInLimitPI", extraCompanyPreferences.isIncludeAmountInLimitPI());
                obj.put("includeAmountInLimitSO", extraCompanyPreferences.isIncludeAmountInLimitSO());
                obj.put("includeAmountInLimitPO", extraCompanyPreferences.isIncludeAmountInLimitPO());
                obj.put("receiptWriteOffAccount", extraCompanyPreferences.getWriteOffReceiptAccount() != null ? extraCompanyPreferences.getWriteOffReceiptAccount() : "");
                obj.put("vendorcreditcontrol", extraCompanyPreferences.getVendorCreditControlType());
                obj.put("propagatetochildcompanies", extraCompanyPreferences.isPropagateToChildCompanies());
                obj.put("wastageDefaultAccount", extraCompanyPreferences.getWastageDefaultAccount() != null ? extraCompanyPreferences.getWastageDefaultAccount() : "");
                obj.put("adjustmentAccountPayment", extraCompanyPreferences.getAdjustmentAccountPayment() == null ? "" : extraCompanyPreferences.getAdjustmentAccountPayment());
                obj.put("adjustmentAccountReceipt", extraCompanyPreferences.getAdjustmentAccountReceipt() == null ? "" : extraCompanyPreferences.getAdjustmentAccountReceipt());
                obj.put("activateWastageCalculation", extraCompanyPreferences.isActivateWastageCalculation());
                obj.put("calculateproductweightmeasurment", extraCompanyPreferences.isCalculateProductWeightMeasurment());
                obj.put("activeDateRangeFromDate",extraCompanyPreferences.getActiveFromDate()!=null?formatter.format( extraCompanyPreferences.getActiveFromDate()):"");
                obj.put("activeDateRangeToDate",extraCompanyPreferences.getActiveToDate()!=null? formatter.format(extraCompanyPreferences.getActiveToDate()):"");
                obj.put("carryForwardPriceForCrossLinking", extraCompanyPreferences.isCarryForwardPriceForCrossLinking());
                obj.put("showZeroAmountAsBlank", extraCompanyPreferences.isShowZeroAmountAsBlank());
                obj.put("showaccountcodeinfinancialreport", extraCompanyPreferences.isShowAccountCodeInFinancialReport());
                obj.put("bandsWithSpecialRateForSales", extraCompanyPreferences.isBandsWithSpecialRateForSales());
                obj.put("amountInIndianWord", extraCompanyPreferences.isAmountInIndianWord());
                obj.put("badDebtProcessingPeriod", extraCompanyPreferences.getBadDebtProcessingPeriod());
                obj.put("badDebtProcessingPeriodType", extraCompanyPreferences.getBadDebtProcessingPeriodType());
                obj.put("gstSubmissionPeriod", extraCompanyPreferences.getGstSubmissionPeriod());
                // Activation and Deactivation dates for malaysian company
                obj.put("gstDeactivationDate", (extraCompanyPreferences.getGstDeactivationDate() != null) ? authHandler.getDateOnlyFormat(request).format(extraCompanyPreferences.getGstDeactivationDate()) : "");
                obj.put("unitPriceInDO", extraCompanyPreferences.isUnitPriceInDO());
                obj.put("unitPriceInGR", extraCompanyPreferences.isUnitPriceInGR());
                obj.put("unitPriceInSR", extraCompanyPreferences.isUnitPriceInSR());
                obj.put("unitPriceInPR", extraCompanyPreferences.isUnitPriceInPR());
                obj.put("openPOandSO", extraCompanyPreferences.isOpenPOandSO());
                obj.put("showAddressonPOSOSave", extraCompanyPreferences.isShowAddressonPOSOSave());
                obj.put("isAutoSaveAndPrintChkBox", extraCompanyPreferences.isAutoSaveAndPrint());
                obj.put("customervendorsortingflag", extraCompanyPreferences.isCustomerVendorSortingFlag());
                obj.put("accountsortingflag", extraCompanyPreferences.isAccountSortingFlag());
                obj.put("isLineLevelTermFlag", extraCompanyPreferences.getLineLevelTermFlag()==1 ? true : false);
                obj.put("custcreditlimitorder", extraCompanyPreferences.getCustcreditcontrolorder());
                obj.put("vendorcreditlimitorder", extraCompanyPreferences.getVendorcreditcontrolorder());
                obj.put("negativeValueIn", extraCompanyPreferences.getNegativeValueIn());
                obj.put("allowCustomerCheckInCheckOut", extraCompanyPreferences.isAllowCustomerCheckInCheckOut());
                obj.put("activateDDTemplateFlow", extraCompanyPreferences.isActivateDDTemplateFlow());
                obj.put("activateDDInsertTemplateLink", extraCompanyPreferences.isActivateDDInsertTemplateLink());
                obj.put("isAutoFillBatchDetails", extraCompanyPreferences.isAutoFillBatchDetails());
                obj.put("columnPref", extraCompanyPreferences.getColumnPref());
                //get flag for Enable Cash Receive Return field
                obj.put("enableCashReceiveReturn", extraCompanyPreferences.isEnableCashReceiveReturn());
                String columnPref = extraCompanyPreferences.getColumnPref();
                JSONObject columnPrefJobj = (!StringUtil.isNullOrEmpty(columnPref) ? new JSONObject(columnPref) : new JSONObject());
                precisionMap.put(Constants.gstAmountDigitAfterDecimal, columnPrefJobj.optInt(Constants.gstAmountDigitAfterDecimal, Constants.GSTValue_DIGIT_AFTER_DECIMAL));

                Constants.CompanyPreferencePrecisionMap.put(extraCompanyPreferences.getId(), precisionMap);
            }
            // For Compliance ExtraCompany preferences
            if (request.getAttribute("complianceExtraCompanyPreferences") != null) {
                IndiaComplianceCompanyPreferences indiaComplianceExtraCompanyPreferences = (IndiaComplianceCompanyPreferences) request.getAttribute("complianceExtraCompanyPreferences");
                obj.put("isAddressChanged", indiaComplianceExtraCompanyPreferences.isIsaddresschanged());
                obj.put("resposiblePersonHasAddressChanged", indiaComplianceExtraCompanyPreferences.isResposiblePersonAddChanged());
                obj.put("resposiblePersonstate", !StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getResposiblePersonstate()) ? indiaComplianceExtraCompanyPreferences.getResposiblePersonstate() : "");
                obj.put("resposiblePersonPAN", !StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getResposiblePersonPAN()) ? indiaComplianceExtraCompanyPreferences.getResposiblePersonPAN() : "");
                obj.put("istaxonadvancereceipt", indiaComplianceExtraCompanyPreferences.isIstaxonadvancereceipt());
                obj.put("istcsapplicable", indiaComplianceExtraCompanyPreferences.isIstcsapplicable());
                obj.put("isitcapplicable", indiaComplianceExtraCompanyPreferences.isIsitcapplicable());
                obj.put("istdsapplicable", indiaComplianceExtraCompanyPreferences.isIstdsapplicable());
                obj.put("resposiblePersonPostal", !StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getResposiblePersonPostal()) ? indiaComplianceExtraCompanyPreferences.getResposiblePersonPostal() : "");
                obj.put("resposiblePersonEmail", !StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getResposiblePersonEmail()) ? indiaComplianceExtraCompanyPreferences.getResposiblePersonEmail() : "");
                obj.put("resposiblePersonMobNumber", !StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getResposiblePersonMobNumber()) ? indiaComplianceExtraCompanyPreferences.getResposiblePersonMobNumber() : "");
                obj.put("resposiblePersonTeleNumber", !StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getResposiblePersonTeleNumber()) ? indiaComplianceExtraCompanyPreferences.getResposiblePersonTeleNumber() : "");
                obj.put("resposiblePersonAddress", !StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getResposiblePersonAddress()) ? indiaComplianceExtraCompanyPreferences.getResposiblePersonAddress() : "");
                obj.put("AssessmentYear", !StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getAssessmentYear()) ? indiaComplianceExtraCompanyPreferences.getAssessmentYear(): "");
                obj.put("GTAKKCPaybleAccount",indiaComplianceExtraCompanyPreferences!=null?indiaComplianceExtraCompanyPreferences.getGTAKKCPaybleAccount():"");//GTA KKC Payble Account
                obj.put("GTASBCPaybleAccount",indiaComplianceExtraCompanyPreferences!=null?indiaComplianceExtraCompanyPreferences.getGTASBCPaybleAccount():"");//GTA SBC Payble Account
                obj.put("CINnumber", !StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getCINnumber()) ? indiaComplianceExtraCompanyPreferences.getCINnumber(): "");
                obj.put("isGSTApplicable",indiaComplianceExtraCompanyPreferences.isIsGSTApplicable());
                obj.put("GSTIN",!StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getGstin())?indiaComplianceExtraCompanyPreferences.getGstin():"");
                obj.put("showIndiaCompanyPreferencesTab",indiaComplianceExtraCompanyPreferences.isShowIndiaCompanyPreferencesTab());
                obj.put("CompanyTDSInterestRate",indiaComplianceExtraCompanyPreferences.getTdsInterestRate());
                obj.put("RCMApplicable", indiaComplianceExtraCompanyPreferences.isRcmApplicable());
                obj.put("igstaccount", StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getIGSTAccount()) ? "" : indiaComplianceExtraCompanyPreferences.getIGSTAccount());
                obj.put("customdutyaccount", StringUtil.isNullOrEmpty(indiaComplianceExtraCompanyPreferences.getCustomDutyAccount()) ? "" : indiaComplianceExtraCompanyPreferences.getCustomDutyAccount());

            }

            if (request.getAttribute("mrpCompanyPreferences") != null) {
                MRPCompanyPreferences mrpCompanyPreferences = (MRPCompanyPreferences) request.getAttribute("mrpCompanyPreferences");
                obj.put("autoGenPurchaseType", mrpCompanyPreferences.getAutoGenPurchaseType());
                obj.put("woInventoryUpdateType", mrpCompanyPreferences.getWoInventoryUpdateType());
                obj.put("mrpProductComponentType", mrpCompanyPreferences.getmrpProductComponentType());
                
            }
            if (request.getAttribute("documentEmailSettings") != null) {
                DocumentEmailSettings documentEmailSettings = (DocumentEmailSettings) request.getAttribute("documentEmailSettings");
                obj.put("purchaseReqGenerationMail", documentEmailSettings.isPurchaseReqGenerationMail());
                obj.put("purchaseReqUpdationMail", documentEmailSettings.isPurchaseReqUpdationMail());
                obj.put("vendorQuotationGenerationMail", documentEmailSettings.isVendorQuotationGenerationMail());
                obj.put("vendorQuotationUpdationMail", documentEmailSettings.isVendorQuotationUpdationMail());
                obj.put("purchaseOrderGenerationMail", documentEmailSettings.isPurchaseOrderGenerationMail());
                obj.put("purchaseOrderUpdationMail", documentEmailSettings.isPurchaseOrderUpdationMail());
                obj.put("goodsReceiptGenerationMail", documentEmailSettings.isGoodsReceiptGenerationMail());
                obj.put("goodsReceiptUpdationMail", documentEmailSettings.isGoodsReceiptUpdationMail());
                obj.put("purchaseReturnGenerationMail", documentEmailSettings.isPurchaseReturnGenerationMail());
                obj.put("purchaseReturnUpdationMail", documentEmailSettings.isPurchaseReturnUpdationMail());
                obj.put("vendorPaymentGenerationMail", documentEmailSettings.isVendorPaymentGenerationMail());
                obj.put("vendorPaymentUpdationMail", documentEmailSettings.isVendorPaymentUpdationMail());
                obj.put("debitNoteGenerationMail", documentEmailSettings.isDebitNoteGenerationMail());
                obj.put("debitNoteUpdationMail", documentEmailSettings.isDebitNoteUpdationMail());
                //Sales side
                obj.put("customerQuotationGenerationMail", documentEmailSettings.isCustomerQuotationGenerationMail());
                obj.put("customerQuotationUpdationMail", documentEmailSettings.isCustomerQuotationUpdationMail());
                obj.put("salesOrderGenerationMail", documentEmailSettings.isSalesOrderGenerationMail());
                obj.put("salesOrderUpdationMail", documentEmailSettings.isSalesOrderUpdationMail());
                obj.put("deleveryOrderGenerationMail", documentEmailSettings.isDeleveryOrderGenerationMail());
                obj.put("deleveryOrderUpdationMail", documentEmailSettings.isDeleveryOrderUpdationMail());
                obj.put("salesReturnGenerationMail", documentEmailSettings.isSalesReturnGenerationMail());
                obj.put("salesReturnUpdationMail", documentEmailSettings.isSalesReturnUpdationMail());
                obj.put("receiptGenerationMail", documentEmailSettings.isReceiptGenerationMail());
                obj.put("receiptUpdationMail", documentEmailSettings.isReceiptUpdationMail());
                obj.put("creditNoteGenerationMail", documentEmailSettings.isCreditNoteGenerationMail());
                obj.put("creditNoteUpdationMail", documentEmailSettings.isCreditNoteUpdationMail());
                //Lease Fixed Asset 
                obj.put("leaseQuotationGenerationMail", documentEmailSettings.isLeaseQuotationGenerationMail());
                obj.put("leaseQuotationUpdationMail", documentEmailSettings.isLeaseQuotationUpdationMail());
                obj.put("leaseOrderGenerationMail", documentEmailSettings.isLeaseOrderGenerationMail());
                obj.put("leaseOrderUpdationMail", documentEmailSettings.isLeaseOrderUpdationMail());
                obj.put("leaseDeliveryOrderGenerationMail", documentEmailSettings.isLeaseDeliveryOrderGenerationMail());
                obj.put("leaseDeliveryOrderUpdationMail", documentEmailSettings.isLeaseDeliveryOrderUpdationMail());
                obj.put("leaseReturnGenerationMail", documentEmailSettings.isLeaseReturnGenerationMail());
                obj.put("leaseReturnUpdationMail", documentEmailSettings.isLeaseReturnUpdationMail());
                obj.put("leaseInvoiceGenerationMail", documentEmailSettings.isLeaseInvoiceGenerationMail());
                obj.put("leaseInvoiceUpdationMail", documentEmailSettings.isLeaseInvoiceUpdationMail());
                obj.put("leaseContractGenerationMail", documentEmailSettings.isLeaseContractGenerationMail());
                obj.put("leaseContractUpdationMail", documentEmailSettings.isLeaseContractUpdationMail());

                //Consignment Stock Sales Module 
                obj.put("consignmentReqGenerationMail", documentEmailSettings.isConsignmentReqGenerationMail());
                obj.put("consignmentReqUpdationMail", documentEmailSettings.isConsignmentReqUpdationMail());
                obj.put("consignmentDOGenerationMail", documentEmailSettings.isConsignmentDOGenerationMail());
                obj.put("consignmentDOUpdationMail", documentEmailSettings.isConsignmentDOUpdationMail());
                obj.put("consignmentInvoiceGenerationMail", documentEmailSettings.isConsignmentInvoiceGenerationMail());
                obj.put("consignmentInvoiceUpdationMail", documentEmailSettings.isConsignmentInvoiceUpdationMail());
                obj.put("consignmentReturnGenerationMail", documentEmailSettings.isConsignmentReturnGenerationMail());
                obj.put("consignmentReturnUpdationMail", documentEmailSettings.isConsignmentReturnUpdationMail());


                //Consignment Stock Purchase  Module 
                obj.put("consignmentPReqGenerationMail", documentEmailSettings.isConsignmentPReqGenerationMail());
                obj.put("consignmentPReqUpdationMail", documentEmailSettings.isConsignmentPReqUpdationMail());
                obj.put("consignmentPDOGenerationMail", documentEmailSettings.isConsignmentPDOGenerationMail());
                obj.put("consignmentPDOUpdationMail", documentEmailSettings.isConsignmentPDOUpdationMail());
                obj.put("consignmentPInvoiceGenerationMail", documentEmailSettings.isConsignmentPInvoiceGenerationMail());
                obj.put("consignmentPInvoiceUpdationMail", documentEmailSettings.isConsignmentPInvoiceUpdationMail());
                obj.put("consignmentPReturnGenerationMail", documentEmailSettings.isConsignmentPReturnGenerationMail());
                obj.put("consignmentPReturnUpdationMail", documentEmailSettings.isConsignmentPReturnUpdationMail());

                //Asset Module
                obj.put("assetPurchaseReqGenerationMail", documentEmailSettings.isAssetPurchaseReqGenerationMail());
                obj.put("assetPurchaseReqUpdationMail", documentEmailSettings.isAssetPurchaseReqUpdationMail());
                obj.put("assetVendorQuotationGenerationMail", documentEmailSettings.isAssetVendorQuotationGenerationMail());
                obj.put("assetVendorQuotationUpdationMail", documentEmailSettings.isAssetVendorQuotationUpdationMail());
                obj.put("assetPurchaseOrderGenerationMail", documentEmailSettings.isAssetPurchaseOrderGenerationMail());
                obj.put("assetPurchaseOrderUpdationMail", documentEmailSettings.isAssetPurchaseOrderUpdationMail());
                obj.put("assetPurchaseInvoiceGenerationMail", documentEmailSettings.isAssetPurchaseInvoiceGenerationMail());
                obj.put("assetPurchaseInvoiceUpdationMail", documentEmailSettings.isAssetPurchaseInvoiceUpdationMail());
                obj.put("assetDisposalInvoiceGenerationMail", documentEmailSettings.isAssetDisposalInvoiceGenerationMail());
                obj.put("assetDisposalInvoiceUpdationMail", documentEmailSettings.isAssetDisposalInvoiceUpdationMail());
                obj.put("assetGoodsReceiptGenerationMail", documentEmailSettings.isAssetGoodsReceiptGenerationMail());
                obj.put("assetGoodsReceiptUpdationMail", documentEmailSettings.isAssetGoodsReceiptUpdationMail());
                obj.put("assetDeliveryOrderGenerationMail", documentEmailSettings.isAssetDeliveryOrderGenerationMail());
                obj.put("assetDeliveryOrderUpdationMail", documentEmailSettings.isAssetDeliveryOrderUpdationMail());
                obj.put("assetPurchaseReturnGenerationMail", documentEmailSettings.isAssetPurchaseReturnGenerationMail());
                obj.put("assetPurchaseReturnUpdationMail", documentEmailSettings.isAssetPurchaseReturnUpdationMail());
                obj.put("assetSalesReturnGenerationMail", documentEmailSettings.isAssetSalesReturnGenerationMail());
                obj.put("assetSalesReturnUpdationMail", documentEmailSettings.isAssetSalesReturnUpdationMail());

                obj.put("salesInvoiceGenerationMail", documentEmailSettings.isSalesInvoiceGenerationMail());
                obj.put("salesInvoiceUpdationMail", documentEmailSettings.isSalesInvoiceUpdationMail());
                obj.put("purchaseInvoiceGenerationMail", documentEmailSettings.isPurchaseInvoiceGenerationMail());
                obj.put("purchaseInvoiceUpdationMail", documentEmailSettings.isPurchaseInvoiceUpdationMail());
                obj.put("recurringInvoiceMail", documentEmailSettings.isRecurringInvoiceMail());
                obj.put("consignmentRequestApproval", documentEmailSettings.isConsignmentRequestApproval());
                obj.put("qtyBelowReorderLevelMail", documentEmailSettings.isQtyBelowReorderLevelMail());
                obj.put("RFQGenerationMail", documentEmailSettings.isRFQGenerationMail());
                obj.put("RFQUpdationMail", documentEmailSettings.isRFQUpdationMail());
                obj.put("isCustShipAddressInPurchase", documentEmailSettings.isCustShippingAddressInPurDoc());

            }
            Iterator chequeFormatListitr = chequeFormatList.iterator();
            String chequeSequenceFormatValue = "";
            while (chequeFormatListitr.hasNext()) {
                String chequeNumberFormat = CompanyPreferencesCMN.getChequeNumberFormat((ChequeSequenceFormat) chequeFormatListitr.next());
                chequeSequenceFormatValue += chequeNumberFormat + ",";
            }
            obj.put(AUTOCHEQUE, chequeSequenceFormatValue.equalsIgnoreCase("") ? "" : (chequeSequenceFormatValue.endsWith(",") ? chequeSequenceFormatValue.substring(0, chequeSequenceFormatValue.length() - 1) : chequeSequenceFormatValue));

//            Company company = pref.getCompany();
            String creatorMailID = getSysEmailIdByCompanyID(company);
            obj.put(COMPANYPHONENO, company.getPhoneNumber() == null ? "" : company.getPhoneNumber());
            obj.put(COMPANYEMAILID, StringUtil.isNullOrEmpty((String) company.getEmailID()) ? creatorMailID : company.getEmailID());
            obj.put("countryid", company.getCountry().getID());
            obj.put("countryname", company.getCountry().getCountryName());
            obj.put("stateid", company.getState()!=null? company.getState().getID():"");
            obj.put("statename", company.getState()!=null?company.getState().getStateName():"Other");
            obj.put("currid", company.getCurrency().getCurrencyID());
            obj.put("currencyname", company.getCurrency().getName());
            obj.put("currencycode", company.getCurrency().getCurrencyCode());
            obj.put(Constants.REFERRALKEY, company.getReferralkey());
            obj.put("companyfullname", company.getCompanyName());
            obj.put(Constants.RES_CDOMAIN, company.getSubDomain());
            
            JSONObject params = new JSONObject();
            params.put("companyid", company != null ? company.getCompanyID() : "");
            JSONObject invPrefObj = getCompanyInventoryAccountPreferences(params);
            obj.put("inventoryCheck", invPrefObj.optInt("negative_inventory_check"));
            obj.put("stockBatchType", invPrefObj.optInt("stock_update_batchtype"));
            obj.put("enableStockAdjustmentApprovalFlow", invPrefObj.optBoolean("enable_stockadj_approvalflow"));
            obj.put("enableStockRequestApprovalFlow", invPrefObj.optBoolean("enable_stockreq_approvalflow"));
            obj.put("enableStockOutApprovalFlow", invPrefObj.optBoolean("enable_stockout_approvalflow"));
            obj.put("enableInterStoreApprovalFlow", invPrefObj.optBoolean("enable_ist_return_approvalflow"));
            obj.put("enableStockRequestReturnApprovalFlow", invPrefObj.optBoolean("enable_sr_return_approvalflow"));

            // To Show the Sales Person 2 and Project Manager Column in Monthly Commission of the Sales Person Report
            if (storageHandlerImpl.SBICompanyId() != null && storageHandlerImpl.SBICompanyId().toString().equals(company.getCompanyID())) {
                obj.put("isSBIFlag", true);
            } else {
                obj.put("isSBIFlag", false);
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("getCompanyAccountPreferences : " + e.getMessage(), e);
        }
        return obj;
    }
    
    @SuppressWarnings("finally")
    public static String getSysEmailIdByCompanyID(Company company) {
        String emailId = "admin@deskera.com";
        try {
            if (company != null) {
                String userID = company.getCreator().getUserID();
                if (StringUtil.isNullOrEmpty(company.getEmailID()) && !StringUtil.isNullOrEmpty(userID)) {
//                        List<?> creatorResult = HibernateUtil.executeQuery("select emailID from User where userID=?", userID);
//                        if (creatorResult != null && creatorResult.size() > 0 && !StringUtil.isNullOrEmpty((String) creatorResult.get(0))) {
                    emailId = (String) company.getCreator().getEmailID();
//                        }
                } else {
                    emailId = company.getEmailID();
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("getSysEmailIdByCompanyID : " + e.getMessage(), e);
        } finally {
            return emailId;
        }
    }
    @Override
    public JSONObject saveSMTPAuthenticationDetails(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject obj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);

            String smtppath = (String) requestParams.get("smtppath");
            String smtpport = (String) requestParams.get("smtpport");
            String smtppassword = (String) requestParams.get("smtppassword");
            String smtpusername = (String) requestParams.get("smtpusername");
            HashMap<String, Object> smtpMap = new HashMap<String, Object>();
            smtpMap.put("companyid", companyid);
            smtpMap.put("mailserveraddress", smtppath);
            smtpMap.put("mailserverport", smtpport);
            smtpMap.put("smtppassword", smtppassword);
            smtpMap.put("emailid", smtpusername);
            smtpMap.put("smtpflow", 1);
            KwlReturnObject result = profileHandlerDAOObj.updateCompany(smtpMap);
            issuccess = true;
            msg = "SMTP details has been saved successfully.";
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCompanyPreferencesController.saveSMTPAuthenticationDetails : " + ex;
             throw ServiceException.FAILURE("saveSMTPAuthenticationDetails : " + ex.getMessage(), ex);
        } finally {
            try {
                obj.put(Constants.RES_success, issuccess);
                obj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                 throw ServiceException.FAILURE("saveSMTPAuthenticationDetails : " + ex.getMessage(), ex);
            }
        }
        return obj;
    }

    @Override
    public JSONObject getSMTPAuthenticationDetails(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONArray dataArray = new JSONArray();
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            KwlReturnObject smtpDetails = accountingHandlerDAOobj.getSMTPAuthenticationDetails(requestParams);
            if (!smtpDetails.getEntityList().isEmpty()) {
                List details = smtpDetails.getEntityList();
                Iterator itr = details.iterator();
                while (itr.hasNext()) {
                    JSONObject addrObject = new JSONObject();
                    Object[] obj = (Object[]) itr.next();
                    addrObject.put("smtppath", obj[0] != null ? obj[0] : "");
                    addrObject.put("smtpport", obj[1] != null ? obj[1] : "");
                    addrObject.put("smtppassword", obj[2] != null ? obj[2] : "");
                    addrObject.put("smtpusername", obj[3] != null ? obj[3] : "");
                    dataArray.put(addrObject);
                }
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCompanyPreferencesController.getSMTPAuthenticationDetails : " + ex;
            throw ServiceException.FAILURE("getSMTPAuthenticationDetails : " + ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_data, dataArray);
                jobj.put("count", dataArray.length());
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                throw ServiceException.FAILURE("getSMTPAuthenticationDetails : " + ex.getMessage(), ex);
            }
        }
        return jobj;
    }
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {SessionExpiredException.class, AccountingException.class, ServiceException.class, JSONException.class})
    public JSONObject saveIndianGSTSettings(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        String companyId = "";
        boolean istaxonadvancereceipt = false;
        HashMap<String, Object> requestParams = new HashMap<>();
        if ((paramJobj.has(Constants.companyKey))) {
            companyId = paramJobj.optString(Constants.companyKey);
        }
        if (paramJobj.has("istaxonadvancereceipt")) {
            istaxonadvancereceipt = paramJobj.optBoolean("istaxonadvancereceipt");
        }
        if (paramJobj.has("istcsapplicable")) {
            requestParams.put("istcsapplicable", paramJobj.optBoolean("istcsapplicable"));
        }
        if (paramJobj.has("isitcapplicable")) {
            requestParams.put("isitcapplicable", paramJobj.optBoolean("isitcapplicable"));
        }        
        if (paramJobj.has("istdsapplicable")) {
            requestParams.put("istdsapplicable", paramJobj.optBoolean("istdsapplicable"));
        }
        requestParams.put("id", companyId);
        requestParams.put("istaxonadvancereceipt", istaxonadvancereceipt);
        KwlReturnObject companyPreferences = accCompanyPreferencesObj.addOrUpdateIndiaComplianceExtraPreferences(requestParams);
        requestParams.clear();
        requestParams.put("id", companyId);
        if (paramJobj.has("AllowToMapAccounts")) {
            requestParams.put("AllowToMapAccounts",paramJobj.optBoolean("AllowToMapAccounts"));
        }
        if (paramJobj.has("amountInIndianWord")) {
            requestParams.put("amountInIndianWord",paramJobj.optBoolean("amountInIndianWord")); 
        }
        accCompanyPreferencesObj.addOrUpdateExtraPreferences(requestParams);
        return jobj;
    }
    
    @Override
    public JSONObject getCompanyInventoryAccountPreferences(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject obj = new JSONObject();
        try {
            accCompanyPreferencesObj.getCompanyInventoryAccountPreferences(paramJobj);
            KwlReturnObject companyInventoryPref = accCompanyPreferencesObj.getCompanyInventoryAccountPreferences(paramJobj);
            if (!companyInventoryPref.getEntityList().isEmpty()) {
                List prefList = companyInventoryPref.getEntityList();
                Object[] resultObj = (Object[]) prefList.get(0);
                obj.put("negative_inventory_check", resultObj[0]);
                obj.put("stock_update_batchtype", resultObj[1]);
                obj.put("enable_stockadj_approvalflow", resultObj[2]);
                obj.put("enable_stockreq_approvalflow", resultObj[3]);
                obj.put("enable_stockout_approvalflow", resultObj[4]);
                obj.put("enable_ist_return_approvalflow", resultObj[5]);
                obj.put("enable_sr_return_approvalflow", resultObj[6]);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCompanyInventoryAccountPreferences : " + ex.getMessage(), ex);
        }
        return obj;
    }
    
   @Override 
   //Moved from controller
    public JSONObject getNextAutoNumber(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            int from = Integer.parseInt(paramJobj.optString("from"));
            String sequenceformat = paramJobj.optString("sequenceformat");
            boolean ignoreLeadingZero = false;
            boolean oldflag = paramJobj.optString("oldflag") != null ? StringUtil.getBoolean(paramJobj.optString("oldflag")) : false;
            CompanyAccountPreferences preferences;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", companyid);
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);
            ignoreLeadingZero = !preferences.isShowLeadingZero();
            int autoGenNumberStartFrom = getAutoGenNumberStartFrom(from, companyid);
            String nextAutoNumber = "";
            if (!StringUtil.isNullOrEmpty(sequenceformat)) {
                if (oldflag) {
                    nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, from, sequenceformat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, from, sequenceformat, oldflag, null);
                    nextAutoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                }
            } else {
                nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, from);
            }

            jobj.put(Constants.RES_data, nextAutoNumber);
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    public JSONObject getAgedDateFilter(String userId) {
            JSONObject DateFilterJson = new JSONObject();
        try
        {   JSONObject Jobj = null;
            
            KwlReturnObject result = accCompanyPreferencesObj.getAgedDateFilter(userId);
            if (!result.getEntityList().isEmpty() && result.getEntityList().size() > 0) {
                Jobj = new JSONObject(result.getEntityList().get(0).toString());
                DateFilterJson.put(Constants.agedReceivableDateFilter, Jobj.optInt(Constants.agedReceivableDateFilter, 1));
                DateFilterJson.put(Constants.agedPayableDateFilter, Jobj.optInt(Constants.agedPayableDateFilter, 1));
                DateFilterJson.put(Constants.soaAgedReceivableDateFilter, Jobj.optInt(Constants.soaAgedReceivableDateFilter, 1));
                DateFilterJson.put(Constants.soaAgedPayableDateFilter, Jobj.optInt(Constants.soaAgedPayableDateFilter, 1));
            } else {
                DateFilterJson.put(Constants.agedReceivableDateFilter, 1);
                DateFilterJson.put(Constants.agedPayableDateFilter, 1);
                DateFilterJson.put(Constants.soaAgedReceivableDateFilter, 1);
                DateFilterJson.put(Constants.soaAgedPayableDateFilter, 1);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return DateFilterJson;

    }

    /**
     * Code is moved from accCompanyPrefrencesController
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws AccountingException 
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class})
    public JSONObject deleteSequenceFormat(JSONObject paramJobj) throws ServiceException{
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String format = paramJobj.optString("sequenceformat");
            String id = paramJobj.optString("id");
            String mode = paramJobj.optString("mode");
            String companyId = paramJobj.optString(Constants.companyKey);
            boolean oldflag = StringUtil.getBoolean(paramJobj.optString("oldflag"));
            CompanyAccountPreferences preferences;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", paramJobj.optString(Constants.companyKey));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);
            String sequenceFormatStr = "";
            List list = new ArrayList();
            String module = "";
            String modulename = "";
            //    if(oldflag) {
            if (!StringUtil.isNullOrEmpty(format)) {
                if (mode.equalsIgnoreCase("autojournalentry")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getJournalEntryNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getJournalEntryNumberFormat(), format);
                        }
                        preferences.setJournalEntryNumberFormat(sequenceFormatStr);
                    } else {
                        module = "journalentry";
                        modulename = "Journal Entry";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autoinvoice")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getInvoiceNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getInvoiceNumberFormat(), format);
                        }
                        preferences.setInvoiceNumberFormat(sequenceFormatStr);
                    } else {
                        module = "invoice";
                        modulename = "Customer Invoice";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autocreditmemo")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getCreditNoteNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getCreditNoteNumberFormat(), format);
                        }
                        preferences.setCreditNoteNumberFormat(sequenceFormatStr);
                    } else {
                        module = "creditnote";
                        modulename = "credit Note";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autoreceipt")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getReceiptNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getReceiptNumberFormat(), format);
                        }
                        preferences.setReceiptNumberFormat(sequenceFormatStr);
                    } else {
                        module = "receipt";
                        modulename = "Receive Payment";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autogoodsreceipt")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getGoodsReceiptNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getGoodsReceiptNumberFormat(), format);
                        }
                        preferences.setGoodsReceiptNumberFormat(sequenceFormatStr);
                    } else {
                        module = "goodsreceipt";
                        modulename = "Vendor Invoice";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autodebitnote")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getDebitNoteNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getDebitNoteNumberFormat(), format);
                        }
                        preferences.setDebitNoteNumberFormat(sequenceFormatStr);
                    } else {
                        module = "debitnote";
                        modulename = "Debit Note";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autopayment")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getPaymentNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getPaymentNumberFormat(), format);
                        }
                        preferences.setPaymentNumberFormat(sequenceFormatStr);
                    } else {
                        module = "payment";
                        modulename = "Make Payment";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autoso")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getSalesOrderNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getSalesOrderNumberFormat(), format);
                        }
                        preferences.setSalesOrderNumberFormat(sequenceFormatStr);
                    } else {
                        module = "salesorder";
                        modulename = "Sales Order";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autopo")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getPurchaseOrderNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getPurchaseOrderNumberFormat(), format);
                        }
                        preferences.setPurchaseOrderNumberFormat(sequenceFormatStr);
                    } else {
                        module = "purchaseorder";
                        modulename = "Purchase Order";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autosecurityNo")) {
                    module = "securitygateentry";
//                        modulename = "Purchase Order";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }

                if (mode.equalsIgnoreCase("autocashsales")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getCashSaleNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getCashSaleNumberFormat(), format);
                        }
                        preferences.setCashSaleNumberFormat(sequenceFormatStr);
                    } else {
                        module = "invoice";
                        modulename = "Cash Sales";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autocashpurchase")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getCashPurchaseNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getCashPurchaseNumberFormat(), format);
                        }
                        preferences.setCashPurchaseNumberFormat(sequenceFormatStr);
                    } else {
                        module = "goodsreceipt";
                        modulename = "Cash Purchase";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autobillinginvoice")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getBillingInvoiceNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getBillingInvoiceNumberFormat(), format);
                        }
                        preferences.setBillingInvoiceNumberFormat(sequenceFormatStr);
                    } else {
                        module = "";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autobillingreceipt")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getBillingReceiptNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getBillingReceiptNumberFormat(), format);
                        }
                        preferences.setBillingReceiptNumberFormat(sequenceFormatStr);
                    } else {
                        module = "";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autobillingcashsales")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getBillingGoodsReceiptNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getBillingGoodsReceiptNumberFormat(), format);
                        }
                        preferences.setBillingCashSaleNumberFormat(sequenceFormatStr);
                    } else {
                        module = "";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autobillinggoodsreceipt")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getBillingGoodsReceiptNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getBillingGoodsReceiptNumberFormat(), format);
                        }
                        preferences.setBillingGoodsReceiptNumberFormat(sequenceFormatStr);
                    } else {
                        module = "";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autobillingdebitnote")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getBillingDebitNoteNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getBillingDebitNoteNumberFormat(), format);
                        }
                        preferences.setBillingDebitNoteNumberFormat(sequenceFormatStr);
                    } else {
                        module = "";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autobillingcreditmemo")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getBillingCreditNoteNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getBillingCreditNoteNumberFormat(), format);
                        }
                        preferences.setBillingCreditNoteNumberFormat(sequenceFormatStr);
                    } else {
                        module = "";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autobillingpayment")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getBillingPaymentNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getBillingPaymentNumberFormat(), format);
                        }
                        preferences.setBillingPaymentNumberFormat(sequenceFormatStr);
                    } else {
                        module = "";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autobillingso")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getBillingSalesOrderNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getBillingSalesOrderNumberFormat(), format);
                        }
                        preferences.setBillingSalesOrderNumberFormat(sequenceFormatStr);
                    } else {
                        module = "";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autobillingpo")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getBillingPurchaseOrderNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getBillingPurchaseOrderNumberFormat(), format);
                        }
                        preferences.setBillingPurchaseOrderNumberFormat(sequenceFormatStr);
                    } else {
                        module = "";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autobillingcashpurchase")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getBillingCashPurchaseNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getBillingCashPurchaseNumberFormat(), format);
                        }
                        preferences.setBillingCashPurchaseNumberFormat(sequenceFormatStr);
                    } else {
                        module = "";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autorequisition")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getRequisitionNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getRequisitionNumberFormat(), format);
                        }

                        preferences.setRequisitionNumberFormat(sequenceFormatStr);
                    } else {
                        module = "purchaserequisition";
                        modulename = "Purchase Requisition";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autorequestforquotation")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getRfqNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getRfqNumberFormat(), format);
                        }

                        preferences.setRfqNumberFormat(sequenceFormatStr);
                    } else {
                        module = "requestforquotation";
                        modulename = "Request for quotation";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autovenquotation")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getVenQuotationNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getVenQuotationNumberFormat(), format);
                        }
                        preferences.setVenQuotationNumberFormat(sequenceFormatStr);
                    } else {
                        module = "vendorquotation";
                        modulename = "Vendor Quotation";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autoquotation")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getQuotationNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getQuotationNumberFormat(), format);
                        }
                        preferences.setQuotationNumberFormat(sequenceFormatStr);
                    } else {
                        module = "quotation";
                        modulename = "Customer Quotation";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autodo")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getRequisitionNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getRequisitionNumberFormat(), format);
                        }
                        preferences.setDeliveryOrderNumberFormat(sequenceFormatStr);
                    } else {
                        module = "deliveryorder";
                        modulename = "Delivery Order";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autogro")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getGoodsReceiptOrderNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getGoodsReceiptOrderNumberFormat(), format);
                        }
                        preferences.setGoodsReceiptOrderNumberFormat(sequenceFormatStr);
                    } else {
                        module = "grorder";
                        modulename = "Goods Receipt";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autosr")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getSalesReturnNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getSalesReturnNumberFormat(), format);
                        }
                        preferences.setSalesReturnNumberFormat(sequenceFormatStr);
                    } else {
                        module = "salesreturn";
                        modulename = "Sales Return";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }

                if (mode.equalsIgnoreCase("autopr")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getPurchaseReturnNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getPurchaseReturnNumberFormat(), format);
                        }
                        preferences.setPurchaseReturnNumberFormat(sequenceFormatStr);
                    } else {
                        module = "purchasereturn";
                        modulename = "Purchase Return";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }
                if (mode.equalsIgnoreCase("autodimensionnumber")) {
                    module = "fieldcombodata";
                    modulename = "Dimensions";
                    list = accCompanyPreferencesObj.checkDimensionSequenceFormat(id, companyId, module);
                }
                if (mode.equalsIgnoreCase("autoRG23EntryNumber")) {
                    module = "dealerexcisedetails";
                    modulename = "RG 23D Entry Number";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }
                /**
                 * Get module information while deleting sequence format for
                 * Packing Delivery Order
                 */
                if (mode.equalsIgnoreCase("autopackingdo")) {
                    module = "packing";
                    modulename = "Packing Delivery Order";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }
                /**
                 * Get module information while deleting sequence format for
                 * Shipping Delivery Order
                 */
                if (mode.equalsIgnoreCase("autoshippingdo")) {
                    module = "shippingdelivery";
                    modulename = "Shipping Delivery Order";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }

                if (mode.equalsIgnoreCase("autoproductid")) {
                    if (oldflag) {
                        if (!StringUtil.isNullOrEmpty(preferences.getProductidNumberFormat())) {
                            sequenceFormatStr = deleteSequenceFormatNumber(preferences.getProductidNumberFormat(), format);
                        }
                        preferences.setProductidNumberFormat(sequenceFormatStr);
                    } else {
                        module = "product";
                        modulename = "Product";
                        list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                    }
                }
                if (mode.equalsIgnoreCase("autocustomerid") && !oldflag) {
                    module = "customer";
                    modulename = "Customer";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }
                if (mode.equalsIgnoreCase("autovendorid") && !oldflag) {
                    module = "vendor";
                    modulename = "Vendor";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }
                if (mode.equalsIgnoreCase("autocontract") && !oldflag) {
                    module = "contract";
                    modulename = "Contract";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }

                if (mode.equalsIgnoreCase("autobuildassembly") && !oldflag) {
                    module = "productbuild";
                    modulename = "Build Assembly";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }

                if (mode.equalsIgnoreCase("autoassetgroup") && !oldflag) {
                    module = "product";
                    modulename = "Asset Group";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }

                if (mode.equalsIgnoreCase("autolabour") && !oldflag) {
                    module = "labour";
                    modulename = "Labour";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }
                if (mode.equalsIgnoreCase("automrpcontract") && !oldflag) {
                    module = "mrpcontract";
                    modulename = "MRP Contract";
                    list = accCompanyPreferencesObj.checkSequenceFormat(id, companyId, module);
                }
                if (oldflag) {
                    issuccess = accCompanyPreferencesObj.saveCompanyPreferencesObj(preferences);
                }
            }
            //  }
            if (!oldflag && list.size() == 0) {                // delete non used format
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                //             String id =request.getParameter("id");
                dataMap.put("id", id);
                dataMap.put("deleted", true);
                SequenceFormat seqFormat = accCompanyPreferencesObj.saveSequenceFormat(dataMap);
                issuccess = true;

            }
            Map<String, Object> auditParamsMap = new HashMap();
            auditParamsMap.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            auditParamsMap.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            auditParamsMap.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditParamsMap.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditTrailObj.insertAuditLog(AuditAction.SEQUENCE_FORMATE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has deleted sequence format " + format + " for module " + modulename + "", auditParamsMap, "" + id);
            msg = messageSource.getMessage("acc.sequence.format.save", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));   //"Lock has been Updated successfully";
            jobj.put("updatedSequenceFormat", sequenceFormatStr);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("" + ex.getMessage(), ex);
            }
        }
        return jobj;

    }
    
    public String deleteSequenceFormatNumber(String oldFormat, String newFormat) throws ServiceException {
        String sequenceFormatStr = "";
        try {
            String formatArr[] = oldFormat.split(",");
            for (int i = 0; i < formatArr.length; i++) {
                if (!StringUtil.equal(newFormat, formatArr[i])) {
                    sequenceFormatStr += formatArr[i] + ",";
                }
            }
            if (sequenceFormatStr.length() > 0) {
                sequenceFormatStr = sequenceFormatStr.substring(0, sequenceFormatStr.length() - 1);
            }
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }
        return sequenceFormatStr;
    }
}
