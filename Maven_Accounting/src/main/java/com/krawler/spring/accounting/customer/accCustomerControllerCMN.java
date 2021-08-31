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
package com.krawler.spring.accounting.customer;

import com.krawler.common.admin.ServerSpecificOptions;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.limit;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accCusVenMapDAO;
import com.krawler.spring.accounting.account.accVendorCustomerProductDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.*;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.esp.handlers.APICallHandlerService;

import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.krawler.hql.accounting.CustomerProductMapping;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.creditnote.dm.CreditNoteInfo;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.dm.InvoiceInfo;
import com.krawler.spring.accounting.receipt.accReceiptController;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.vendor.accVendorController;
import com.krawler.spring.accounting.vendor.accVendorControllerCMN;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.mainaccounting.service.AccCustomerMainAccountingService;
import java.io.*;
import java.text.DateFormat;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
/**
 *
 * @author krawler
 */
public class accCustomerControllerCMN extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accProductDAO accProductObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accCustomerDAO accCustomerDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private String successView;
    private accInvoiceDAO accInvoiceDAOobj;
    private accInvoiceCMN accInvoiceCMNobj;
    private accReceiptDAO accReceiptDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private auditTrailDAO auditTrailObj;
    private accCusVenMapDAO accCusVenMapDAOObj;
    private accVendorCustomerProductDAO accVendorCustomerProductDAOobj;
    private accDebitNoteDAO accDebitNoteobj;
    private accCreditNoteDAO accCreditNoteDAOobj;  
    private ImportHandler importHandler;
    private APICallHandlerService apiCallHandlerService;
    private accCustomerControllerCMNService accCustomerControllerCMNServiceObj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accInvoiceCMN accInvoiceCommon;
    private com.krawler.spring.common.fieldDataManager fieldmatamanager;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
    private AccCustomerMainAccountingService accCustomerMainAccountingService;
    
    public void setAccCustomerMainAccountingService(AccCustomerMainAccountingService accCustomerMainAccountingService) {
        this.accCustomerMainAccountingService = accCustomerMainAccountingService;
    }
    
    public void setAccSalesOrderServiceDAOobj(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }

    public void setFieldmatamanager(com.krawler.spring.common.fieldDataManager fieldmatamanager) {
        this.fieldmatamanager = fieldmatamanager;
    }
    
    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }
    
    public void setAccVendorPaymentobj(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
    
    public void setAccSalesOrderDAOobj(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    
    public void setaccCustomerControllerCMNServiceObj(accCustomerControllerCMNService accCustomerControllerCMNServiceObj) {
        this.accCustomerControllerCMNServiceObj = accCustomerControllerCMNServiceObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
     
    public void setAccCusVenMapDAOObj(accCusVenMapDAO accCusVenMapDAOObj) {
        this.accCusVenMapDAOObj = accCusVenMapDAOObj;
    }

    public accVendorCustomerProductDAO getAccVendorCustomerProductDAOobj() {
        return accVendorCustomerProductDAOobj;
    }

    public void setAccVendorCustomerProductDAOobj(accVendorCustomerProductDAO accVendorCustomerProductDAOobj) {
        this.accVendorCustomerProductDAOobj = accVendorCustomerProductDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    @Override
	public void setMessageSource(MessageSource msg) {
		this.messageSource = msg;
	}
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj){
    	this.exportDaoObj = exportDaoObj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    public void setaccInvoiceCMN(accInvoiceCMN accInvoiceCMNobj) {
        this.accInvoiceCMNobj = accInvoiceCMNobj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

   public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
   
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public ModelAndView checkCustomerTransactions(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", request.getParameter("accid"));
            requestParams.put("openbalance", request.getParameter("openbalance"));

            String companyid = sessionHandlerImpl.getCompanyid(request);
            checkCustomerTransactions(requestParams, companyid);

            issuccess = true;
            msg = messageSource.getMessage("acc.field.NoTransactionAsociatedwiththiscustomer", null, RequestContextUtils.getLocale(request));
        } catch (AccountingException ex) {
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void checkCustomerTransactions(HashMap request, String companyid) throws ServiceException, AccountingException {

        KwlReturnObject result = null;
        String accountid = (String)request.get("accid");

        if (Double.parseDouble((String)request.get("openbalance")) != 0) {
            throw new AccountingException("Selected record(s) is having the Opening Balance. So it cannot be deleted");
        }else {
            // Check in Journal Entry
            result = accJournalEntryobj.getJEDfromAccount(accountid, companyid);
            int count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
            }

            // Check Product Entry
            result = accProductObj.getProductfromAccount(accountid, companyid);
            count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Account Preferences. So it cannot be deleted.");
            }

            // Check for Preferances Entry
            result = accCompanyPreferencesObj.getPreferencesFromAccount(accountid, companyid);
            count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Product(s). So it cannot be deleted.");
            }

            // Check fot Payment Entry
            result = accPaymentDAOobj.getPaymentMethodFromAccount(accountid, companyid);
            count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Term(s). So it cannot be deleted.");
            }

            // Check for Tax Entry
            result = accTaxObj.getTaxFromAccount(accountid, companyid);
            count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
            }
            boolean isused=accCusVenMapDAOObj.isCustomerUsedInTransactions(accountid, companyid);
            if(isused){
                throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
            }
        }

    }
      
    public ModelAndView saveCustomer(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        Map<String, Object> requestMap = request.getParameterMap();
        paramJobj.put(Constants.requestMap, requestMap);
        jobj = accCustomerControllerCMNServiceObj.saveCustomer(paramJobj);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * function to get customer GST fields data based on date filters.
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getCustomerGSTHistory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject reqParams = StringUtil.convertRequestToJsonObject(request);
            jobj = accCustomerControllerCMNServiceObj.getCustomerGSTHistory(reqParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     /**
     * function to get customer GST fields data used in documents on date filters.
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getCustomerUsedGSTHistory(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        String companyId = sessionHandlerImpl.getCompanyid(request);
        try {
            JSONObject reqParams = StringUtil.convertRequestToJsonObject(request);
            if (!StringUtil.isNullOrEmpty(companyId)) {
                reqParams.put("companyid", companyId);
            }
            jobj = accCustomerControllerCMNServiceObj.getCustomerGSTUsedHistory(reqParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     /**
     * function to get GSTIN of customer/vendor is valid or invalid.
     * @param request
     * @param response
     * @return 
     * @throws com.krawler.common.session.SessionExpiredException 
     */
    public ModelAndView validateCustomerGSTIN(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        String GSTIN = null;
        boolean isValid = false;
        try {
            JSONObject reqParams = StringUtil.convertRequestToJsonObject(request);
            GSTIN = reqParams.optString("gstin");
            if (!StringUtil.isNullOrEmpty(GSTIN)) {
                isValid = StringUtil.isGSTINValid(GSTIN);
                jobj.put("Valid", isValid);
            } else {
                jobj.put("Valid", false);
            }            
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView saveCustomerAddresses(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = accCustomerControllerCMNServiceObj.saveCustomerAddresses(paramJobj);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
   /**
     * Description : Below Method is used to Activate or Deactivate Customers
     * @param <request> used to get data from Http Req
     * @param <response> used to set data to client 
     * @return :JSONObject 
     */
    public ModelAndView activateDeactivateCustomers(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = accCustomerControllerCMNServiceObj.activateDeactivateCustomers(paramJobj);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
       public ModelAndView createAccountForCustomerInCRM(HttpServletRequest request, HttpServletResponse response) {
       JSONObject jobj = new JSONObject();
       JSONArray jArray=new JSONArray();
        boolean issuccess = false;
        String customerID = null, msg = "";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Customer_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
                String companyid = sessionHandlerImpl.getCompanyid(request);
                KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ServerSpecificOptions.class.getName(),ServerSpecificOptions.Case_CustomDateTypeChange);
                ServerSpecificOptions serverSpecificOptions = (ServerSpecificOptions) extracapresult.getEntityList().get(0);
                boolean isDeployed=StringUtil.isAppDeployed("1",serverSpecificOptions); //Here 1 is Application ID of CRM
                DateFormat df=new SimpleDateFormat(Constants.MMMMdyyyy);
                HashMap<String, Object> requestParams = accCustomerController.getCustomerRequestMap(request);

                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Customer_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                requestParams.put("customerIntegrationFlag", true);
            int failureBatches = 0;
            int start = 0;
            int batchLimit = Constants.Customer_Sync_Batch_Count;
            int customerCount = accCustomerDAOobj.getCustomerCount(requestParams);
            int noOfCustomerBatch = customerCount / batchLimit;

            for (int batchCount = 0; batchCount <= noOfCustomerBatch; batchCount++) {
                jArray = new JSONArray();
                requestParams.put(Constants.start, start);
                requestParams.put(Constants.limit, batchLimit);
                KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
                List<Customer> list = result.getEntityList();
                for (Customer customer : list) {
                    JSONObject obj = new JSONObject();
                    obj.put("customerid",customer.getID());
                    obj.put("customername",StringUtil.isNullOrEmpty(customer.getName())?"":customer.getName());
                    obj.put("accname",StringUtil.isNullOrEmpty(customer.getName())?"":customer.getName());
                    obj.put(Constants.currencyKey,(customer.getAccount().getCurrency()==null?"": customer.getAccount().getCurrency().getCurrencyID()));
                    obj.put("creationdate",customer.getCreatedOn()!=null?customer.getCreatedOn().getTime():null); // CRM Needs date value in Long
                    obj.put("creationDate",customer.getCreatedOn()!=null?customer.getCreatedOn().getTime():null); // CRM Needs date value in Long

                    obj.put("sequenceformatid",customer.getSeqformat()!=null?customer.getSeqformat().getID():"NA");
                    obj.put("customercode",customer.getAcccode()!=null?customer.getAcccode():null);
                    obj.put("acccode",customer.getAcccode()!=null?customer.getAcccode():null);
                    
                    obj.put("aliasname", customer.getAliasname());
                    /**
                     *  Send interstate property to CRM for INDIAN subdomain
                     *  Send cformapplicable property to CRM for INDIAN subdomain
                     *  This properties used in CRM integration of ERP, For Sync
                     *  CRM Quotation's in ERP.
                     */
                    obj.put("interstateparty", customer.isInterstateparty());
                    obj.put("cformapplicable", customer.isCformapplicable());
                    /**
                     * GST TAX Calculation for ERP-CRM
                     * Integration while Sync Customer.
                     */
                    String gstcustomertype = customer.getGSTCustomerType() != null ? (customer.getGSTCustomerType().getDefaultMasterItem() != null ? customer.getGSTCustomerType().getDefaultMasterItem().getID() : "") : "";
                    obj.put("type", gstcustomertype);
                    obj.put("gstcustomertype", accCustomerDAOobj.getUniqueCase(obj));
                    obj.put("sezfromdate", customer.getSezFromDate() != null ? customer.getSezFromDate() : "");//GST Tax Calculation
                    obj.put("seztodate", customer.getSezToDate() != null ? customer.getSezToDate() : "");//GST Tax Calculation
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(CustomerCustomData.class.getName(), customer.getID());
//            replaceFieldMap = new HashMap<String, String>();
                    JSONArray jSONArray = new JSONArray();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        CustomerCustomData jeDetailCustom = (CustomerCustomData) custumObjresult.getEntityList().get(0);
                        if (jeDetailCustom != null) {
                            AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                JSONObject jSONObject = new JSONObject();
                                String fieldId = replaceFieldMap.get(varEntry.getKey());
                                fieldId = fieldId.replaceAll("custom_", "");
                                jSONObject.put("fieldid", fieldId);
                                String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                                String colValue = "";
                                String colDescription = "";
                                if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                                    try {
                                        String[] valueData = coldata.split(",");
                                        for (String value : valueData) {
                                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                            if (fieldComboData != null) {
                                                colValue += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                            }
                                        }
                                        if (colValue.length() > 1) {
                                            colValue = colValue.substring(0, colValue.length() - 1);
                                        }
                                        jSONObject.put("fieldData", colValue);
                                    } catch (Exception ex) {
                                        jSONObject.put("fieldData", coldata);
                                    }
                                } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                    if(isDeployed) {
                                        jSONObject.put("fieldData", coldata);
                                    } else {
                                        Date customDate=df.parse(coldata);
                                        coldata=Long.toString(customDate.getTime());
                                        jSONObject.put("fieldData", coldata);
                                    }
                                } else {
                                    jSONObject.put("fieldData", coldata != null ? coldata : "");
                                }
                                jSONArray.put(jSONObject);
                            }
                        }
                    }
                    obj.put("customdata", jSONArray);
                    JSONArray billingAddressArray = new JSONArray();
                    JSONArray shippingAddressArray = new JSONArray();
                    HashMap<String, Object> addressParams = new HashMap<>();
                    addressParams.put(Constants.companyKey, companyid);
                    addressParams.put(Constants.customerid, customer.getID());
                    KwlReturnObject returnObject = accountingHandlerDAOobj.getCustomerAddressDetails(addressParams);
                    List<CustomerAddressDetails> customerAddressDetails = returnObject.getEntityList();
                    for (CustomerAddressDetails cad : customerAddressDetails) {
                        if (cad != null) {
                            if (cad.isIsBillingAddress()) {
                                JSONObject billingAddrObj = AccountingManager.getAddressJsonObject(cad);
                                billingAddressArray.put(billingAddrObj);
                            } else {
                                JSONObject shippingAddrObj = AccountingManager.getAddressJsonObject(cad);
                                shippingAddressArray.put(shippingAddrObj);
                            }
                        }
                    }
                    obj.put("billingAddress", billingAddressArray);
                    obj.put("shippingAddress", shippingAddressArray);
                    obj.put("crmaccountid", StringUtil.isNullOrEmpty(customer.getCrmaccountid())?"":customer.getCrmaccountid());
                    jArray.put(obj);                    
                }

                JSONObject resObj = createAccountForCustomerInCRM(request, jArray, companyid);
                if (!resObj.isNull(Constants.RES_success) && resObj.getBoolean(Constants.RES_success)) {
                    issuccess = true;
                    if (resObj.has("idMappingCrmErp") && !StringUtil.isNullOrEmpty(resObj.getString("idMappingCrmErp"))) {
                        JSONArray crmResArray = resObj.getJSONArray("idMappingCrmErp");
                        for (int cnt = 0; cnt < crmResArray.length(); cnt++) {// For newly created account in CRM update crmaccountid
                            JSONObject accObj = crmResArray.getJSONObject(cnt);
                            
                            HashMap<String, Object> params = new HashMap<String, Object>();
                            params.put("accid", accObj.get("erpcustomerid"));
                            params.put("crmaccountid", accObj.get("crmaccountid"));
                            KwlReturnObject resultAcc = accCustomerDAOobj.updateCustomer(params);
                        }
                    }
                } else {
                    failureBatches++;
                }
                start = (batchLimit * (batchCount + 1)) + 1;
            }
            txnManager.commit(status);
            if (failureBatches == 0) {// All records are synced sucessfull
                msg = messageSource.getMessage("acc.field.CustomerSync.msg", null, RequestContextUtils.getLocale(request));   //"Customer information has been saved successfully";
            } else if (failureBatches == (noOfCustomerBatch + 1)) {// All Records failed to sync
                msg = messageSource.getMessage("acc.field.CustomerSync.failedToSyncInC", null, RequestContextUtils.getLocale(request));
            } else {//Some batches are failed and some are synced
                int failedRecCount = failureBatches * batchLimit;
                int sucessRecordCount = customerCount - failedRecCount;
                msg = messageSource.getMessage("acc.import.total.records", null, RequestContextUtils.getLocale(request)) + " : " + customerCount +".";
                msg += "</br>" + sucessRecordCount + " " + messageSource.getMessage("acc.field.CustomerSync.failedToSyncInsucessfully", null, RequestContextUtils.getLocale(request)) + " " + failedRecCount + " " + messageSource.getMessage("acc.field.CustomerSync.failedToSyncInFailed", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("perAccID", customerID);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
        private JSONObject createAccountForCustomerInCRM(HttpServletRequest request,JSONArray jArray,String companyid) {       
        //Session session=null;
        JSONObject resObj= new JSONObject();
        try {           
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String action = "208";
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/accountingaccounts";            
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put(Constants.companyKey, companyid);
            userData.put("action", action);

            //session = HibernateUtil.getCurrentSession();
            userData.put(Constants.data, jArray);

            resObj = apiCallHandlerService.restPostMethod(crmURL, userData.toString());
//            resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);                       
        } catch (Exception ex) {
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, "accContractController.createActivityInCRM", ex);
        }
//        finally{     No need to close seesion hibernate manage it automatically
////            HibernateUtil.closeSession(session);
//        }
        return resObj;
    } 
        
    public ModelAndView getCustomerFromCRMAccounts(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject resObj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);

//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put(Constants.companyKey, companyid);
            userData.put(Constants.currencyKey, sessionHandlerImpl.getCurrencyID(request));

//            String action = "209";
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/accountingaccounts";
//            try {
                resObj = apiCallHandlerService.restGetMethod(crmURL, userData.toString());
//                resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
//            } catch (Exception ex) {
//            } finally {
//            }
            if (!resObj.isNull(Constants.RES_success) && resObj.getBoolean(Constants.RES_success)) {
                HashMap<String, Object> params = new HashMap<>();
                params.put(Constants.companyKey, companyid);
                jobj = accCustomerControllerCMNServiceObj.getCustomerFromCRMAccounts(params, resObj);
                if (jobj.has(Constants.data) && jobj.optJSONArray(Constants.data) != null && ((JSONArray) jobj.optJSONArray(Constants.data)).length() > 0) {
                    JSONObject userDataPost = new JSONObject();
                    userDataPost.put(Constants.useridKey, sessionHandlerImpl.getUserid(request));
                    userDataPost.put(Constants.companyKey, companyid);
                    userDataPost.put(Constants.currencyKey, sessionHandlerImpl.getCurrencyID(request));
                    userDataPost.put(Constants.data, (JSONArray) jobj.optJSONArray(Constants.data));
                    userDataPost.put(Constants.RES_success, true);
                    StringBuilder urlBuilder = new StringBuilder();
                    String crmURLPost = URLUtil.buildRestURL(Constants.crmURL);
                    urlBuilder.append(crmURLPost);
                    urlBuilder.append("master/erpcustomerid");
                    apiCallHandlerService.restPostMethod(urlBuilder.toString(), userDataPost.toString());
                }
            }
        } catch (NumberFormatException ex) {
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);

        } catch (Exception ex) {
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getCustomerFromLMS(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject resObj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            int from=request.getParameter("from")==null?-1:Integer.parseInt(request.getParameter("from"));
             boolean autogen=false;
            String auditMsg = "added";
            String auditID = AuditAction.CUSTOMER_ADDED;
//            String lmsURL = this.getServletContext().getInitParameter("lmsURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put(Constants.companyKey, companyid);
//            String action = "30";
            String lmsURL = URLUtil.buildRestURL("lmsURL");
            lmsURL = lmsURL + "company/students";                 
            resObj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
//            resObj = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);
            
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            if (!resObj.isNull(Constants.RES_success) && resObj.getBoolean(Constants.RES_success)) {
                JSONArray customerArr = resObj.getJSONArray(Constants.data);
                if(customerArr.length()>0){
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                    for (int i = 0; i < customerArr.length(); i++) {
                        JSONObject custObj = customerArr.getJSONObject(i);
                        Date newdate=new Date();
                        String userdiff=company.getCreator().getTimeZone()!=null?company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference();
                        sdf1.setTimeZone(TimeZone.getTimeZone("GMT"+userdiff));
                        Date newcreatedate=authHandler.getDateWithTimeFormat().parse(sdf1.format(newdate));
                     
                        Customer customer=null;
                        HashMap<String,Object> requestParams=new HashMap<String, Object>();
                        boolean isCustomerPreExist=false;
                        String custCode = custObj.isNull("studentID")?"" : custObj.getString("studentID");
                        String firstName = custObj.isNull("fname")?"" : custObj.getString("fname");
                        String lastName = custObj.isNull("lname")?"" : custObj.getString("lname");
                        String currencyid = custObj.isNull("currency")?sessionHandlerImpl.getCurrencyID(request) : custObj.getString("currency");
                        Date creationDate =custObj.isNull("creationDate")? newcreatedate:sdf.parse(custObj.getString("creationDate"));
                        if (creationDate == null) {
                            creationDate = new Date();
                        }
                        
                        String customerName="";
                        if(!StringUtil.isNullOrEmpty(firstName) && !StringUtil.isNullOrEmpty(lastName)){
                            customerName=firstName+" "+lastName;
                        }else if(!StringUtil.isNullOrEmpty(firstName) && StringUtil.isNullOrEmpty(lastName)){
                            customerName=firstName;
                        }
                      
                        
                        KwlReturnObject resultcheck = accCustomerDAOobj.checkCustomerExistbyCode(custCode, companyid);
                        if(!resultcheck.getEntityList().isEmpty()){
                            customer=(Customer) resultcheck.getEntityList().get(0);
                        }
                        
                        if(customer!=null){
                            auditMsg="updated";
                            auditID=AuditAction.CUSTOMER_UPDATED;
                            requestParams.put("accname", customerName);
                            HashMap<String, Object> params=new HashMap<String, Object>();
                            params.put("accid", customer.getID());
                            KwlReturnObject result = accCustomerDAOobj.updateCustomer(params);
                            isCustomerPreExist=true;
                        }else{

                            if(preferences!=null){
                                requestParams.put("accountid", preferences.getCustomerdefaultaccount().getID());
                            }                            
                            requestParams.put("acccode", custCode);
                            requestParams.put(Constants.currencyKey, currencyid);
                            requestParams.put("autogenerated", autogen);
                            requestParams.put("accname", customerName);
                            requestParams.put("openbalance", 0);
                            requestParams.put("companyid", companyid);
                            requestParams.put("overseas", false);
                            requestParams.put("mapcustomervendor", false);
                            requestParams.put("creationDate", creationDate);
                            
                            resultcheck = accCustomerDAOobj.getDefaultCreditTermForCustomer(companyid);
                            Term term=null;
                            if(!resultcheck.getEntityList().isEmpty()){
                                term=(Term) resultcheck.getEntityList().get(0);
                                if( term!=null)
                                    requestParams.put("termid",term.getID());
                            }
                            String title = custObj.optString("title");
                            if (!StringUtil.isNullOrEmpty(title)) {
                                KwlReturnObject kro = accCustomerDAOobj.getTitleForCustomer(companyid, title);
                                MasterItem masterItem = null;
                                if (!kro.getEntityList().isEmpty()) {
                                    masterItem = (MasterItem) kro.getEntityList().get(0);
                                    if (masterItem != null) {
                                        requestParams.put("title", masterItem.getID());
                                    }
                                }
                            }
                            KwlReturnObject result = accCustomerDAOobj.addCustomer(requestParams);                        
                            customer=(Customer) result.getEntityList().get(0);                            
                           }
                        if (customer != null) {
                            // For Existing Customer If Default Address Present then updating it  
                            // For New Customer inserting a new default address if address coming from LMS 

                            if (custObj.has("addressData") && custObj.optJSONObject("addressData") != null) {
                                JSONObject addrDataObject = custObj.optJSONObject("addressData");
                                HashMap<String, Object> custAddrMap = new HashMap<String, Object>();
                                custAddrMap.put("customerid", customer.getID());
                                custAddrMap.put("address", addrDataObject.optString("address", ""));
                                custAddrMap.put("city", addrDataObject.optString("city", ""));
                                custAddrMap.put("state", addrDataObject.optString("state", ""));
                                custAddrMap.put("postalCode", addrDataObject.optString("postalCode", ""));
                                custAddrMap.put("phone", addrDataObject.optString("phone", ""));
                                custAddrMap.put("mobileNumber", addrDataObject.optString("mobileNumber", ""));
                                custAddrMap.put("emailID", addrDataObject.optString("emailID", ""));

                                //Saving billing Address
                                boolean isBillingDefault = true;
                                String billingAliasName = "Billing Address1"; //Since No alias name coming from LMS so giving default alias name
                                String biilingAddressID = "";
                                if (isCustomerPreExist) {
                                    List addrDetail = getCustomerAddressInfoByAliasName(billingAliasName, customer.getID(), companyid, true);
                                    biilingAddressID = (String) addrDetail.get(0);
                                    isBillingDefault = (Boolean) addrDetail.get(1);
                                }
                                custAddrMap.put("aliasName", billingAliasName);
                                custAddrMap.put("addressid", biilingAddressID);
                                custAddrMap.put("isBillingAddress", true);
                                custAddrMap.put("isDefaultAddress", isBillingDefault);
                                accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, companyid);

                                //Saving shipping Address
                                boolean isShippingDefault = true;
                                String shippingAliasName = "Shipping Address1";//Since No alias name coming from LMS so giving default alias name
                                String shippingAddressID = "";
                                if (isCustomerPreExist) {
                                    List addrDetail = getCustomerAddressInfoByAliasName(shippingAliasName, customer.getID(), companyid, false);
                                    shippingAddressID = (String) addrDetail.get(0);
                                    isShippingDefault = (Boolean) addrDetail.get(1);
                                }
                                custAddrMap.put("aliasName", shippingAliasName);
                                custAddrMap.put("addressid", shippingAddressID);
                                custAddrMap.put("isBillingAddress", false);
                                custAddrMap.put("isDefaultAddress", isShippingDefault);
                                accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, companyid);
                            }
                            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has added a new customer " + customer.getName(), request, customer.getID());
                        }
                    }
                       
                } 
            }  
            issuccess = true;
             msg = messageSource.getMessage("acc.field.CustomerSync.msg", null, RequestContextUtils.getLocale(request)); 
            txnManager.commit(status);
         } catch (NumberFormatException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                msg=msg = messageSource.getMessage("acc.field.CustomerSync.errormsg", null, RequestContextUtils.getLocale(request)); 
                txnManager.rollback(status);
                        
        } catch (Exception ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                msg=msg = messageSource.getMessage("acc.field.CustomerSync.errormsg", null, RequestContextUtils.getLocale(request)); 
                txnManager.rollback(status);           
        }finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
//            HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public List getCustomerAddressInfoByAliasName(String AliasName, String customerid, String companyid, boolean isbillingAddress) throws ServiceException {
        List addressDetails = new ArrayList();
        //Logic for Pre Exist Customer
        //here we find address of same alias name. If it is present then we return it ID and default type. 
        HashMap<String, Object> addressParams = new HashMap<String, Object>();
        addressParams.put("companyid", companyid);
        addressParams.put("customerid", customerid);
        addressParams.put("isBillingAddress", isbillingAddress); //for billing address this falg will be true
        KwlReturnObject returnObject = accountingHandlerDAOobj.getCustomerAddressDetails(addressParams);
        List list = returnObject.getEntityList();
        boolean isDefault = true;
        String addressID="";
        if (!list.isEmpty()) {
            // Here if this condition is true it mens that Existing customer have 1 or more Billing address. so making default value false
            isDefault = false;
            Iterator addrItr = list.iterator();
            while (addrItr.hasNext()) {
                CustomerAddressDetails details = (CustomerAddressDetails) addrItr.next();
                if (details != null && details.getAliasName() != null && details.getAliasName().equalsIgnoreCase(AliasName)) {
                    addressID = details.getID();
                    isDefault = details.isIsDefaultAddress();
                    break;
                }
            }
        }
        addressDetails.add(addressID);
        addressDetails.add(isDefault);
        return addressDetails;
    }
    public ModelAndView saveCustomerCategoryMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String auditMsg = "", auditID = "";
        
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
//            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] customerList=request.getParameter("personList").split(",");
            String[] customerCategory=request.getParameter("personCategory").split(",");
            if(customerList.length>0) {
                for(int i=0;i<customerList.length;i++){
                    if (!StringUtil.isNullOrEmpty(customerList[i])) {
                        accCustomerDAOobj.deleteCustomerCategoryMappingDtails(customerList[i]);
                    }
                }
            }
            
            if(customerList.length>0 && customerCategory.length>0) {
                for(int i=0;i<customerList.length;i++){
                    for(int j=0;j<customerCategory.length;j++){
                        if (!StringUtil.isNullOrEmpty(customerList[i]) && !StringUtil.isNullOrEmpty(customerCategory[j])) {
                            accCustomerDAOobj.saveCustomerCategoryMapping(customerList[i],customerCategory[j]);
                            
                            KwlReturnObject cstresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerList[i]);
                            Customer customer = (Customer) cstresult.getEntityList().get(0);
                            
                            KwlReturnObject categoryresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), customerCategory[j]);
                            MasterItem ccategory = (MasterItem) categoryresult.getEntityList().get(0);
                            
                            auditMsg = " added new customer category  "+ccategory.getValue()+" to ";
                            auditID = AuditAction.CUSTOMER_CATEGORY_CHANGED;
                            
                            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + customer.getName(), request, customer.getID());
                        }
                    }
                }
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
//                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveCustomerPricingBandMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String auditMsg = "", auditID = "";

        // Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            String[] customerList = request.getParameter("personList").split(",");
            String pricingBand = request.getParameter("personCategory");

            if (customerList.length > 0 && !StringUtil.isNullOrEmpty(pricingBand)) {
                for (int i = 0; i < customerList.length; i++) {
                    if (!StringUtil.isNullOrEmpty(customerList[i])) {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("accid", customerList[i]);
                        requestParams.put("pricingBand", pricingBand);
                        
                        KwlReturnObject result = accCustomerDAOobj.updateCustomer(requestParams);
                        
                        List ll = result.getEntityList();
                        Customer customer = (Customer) ll.get(0);
                        
                        KwlReturnObject categoryresult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), pricingBand);
                        PricingBandMaster pricingBandMaster = (PricingBandMaster) categoryresult.getEntityList().get(0);
                        
                        auditMsg = " added new customer price list - band  " + pricingBandMaster.getName() + " to ";
                        auditID = AuditAction.PRICING_BAND_CHANGED;
                        
                        auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + customer.getName(), request, customer.getID());
                    }
                }
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveCustomerSalesPersonMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String auditMsg = "", auditID = "";
        
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String[] customerList=request.getParameter("personList").split(",");
            String[] salesPersons=request.getParameter("personCategory").split(",");
            if(customerList.length>0) {
                for(int i=0;i<customerList.length;i++){
                    if (!StringUtil.isNullOrEmpty(customerList[i])) {
                        accCustomerDAOobj.deleteSalesPersonMappingDtails(customerList[i]);
                        accCustomerDAOobj.resetCustomerDefaultSalesPerson(customerList[i], salesPersons);
                    }
                }
            }
            
            if(customerList.length>0 && salesPersons.length>0) {
                for(int i=0;i<customerList.length;i++){
                    for(int j=0;j<salesPersons.length;j++){
                        if (!StringUtil.isNullOrEmpty(customerList[i]) && !StringUtil.isNullOrEmpty(salesPersons[j])) {
                            accCustomerDAOobj.saveSalesPersonMapping(customerList[i],salesPersons[j]);
                            
                            KwlReturnObject cstresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerList[i]);
                            Customer customer = (Customer) cstresult.getEntityList().get(0);
                            
                            KwlReturnObject categoryresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), salesPersons[j]);
                            MasterItem salesPerson = (MasterItem) categoryresult.getEntityList().get(0);
                            
                            auditMsg = " added new customer sales person  "+salesPerson.getValue()+" to ";
                            auditID = AuditAction.CUSTOMER_SALESPERSON_CHANGED;
                            
                            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + customer.getName(), request, customer.getID());
                        }
                    }
                }
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * Added @Transactional instead of txnmanager - ERP-32983. Moved code
     * containing business logic to the accCustomerControllerCMNServiceImpl No
     * any changes other than this has been done in code.
     * @param request
     * @param response
     * @return
     */
    public ModelAndView deleteCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            jobj=accCustomerControllerCMNServiceObj.deleteCustomer(requestJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            msg = jobj.optString(Constants.RES_msg);
        } catch (SessionExpiredException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /*
     * To check customer is created as vendor on not
     * 
     */
    public ModelAndView checkIsVendorAsCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isCreateCustomer = false;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String customer = request.getParameter("customerArr");
            String customerArr[] = customer.split(",");
            for (int i = 0; i < customerArr.length; i++) {
                isCreateCustomer = accountingHandlerDAOobj.checkIsVendorAsCustomer(companyid,customerArr[i]);
                if (!isCreateCustomer) {
                    break;
                }
            }
            issuccess = true;

        } catch (Exception ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            jobj.put("isCreateCustomer", isCreateCustomer);
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * @author neeraj
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getCustomers(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accCustomerMainAccountingService.getCustomers(paramJobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
     /**
     * @author Swapnil P
     * @param request
     * @param response
     * @return
     * Need to optimize this function code to handle total customer count is more than enough.
     */
    public ModelAndView getCustomersForDefaultCustomerList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = accCustomerController.getCustomerRequestMap(request);
            if(request.getParameter("custAmountDueMoreThanLimit")!=null&&Boolean.parseBoolean(request.getParameter("custAmountDueMoreThanLimit")))
            {
                requestParams.put("custAmountDueMoreThanLimit", request.getParameter("custAmountDueMoreThanLimit"));
            }
            String start = (String)requestParams.get("start");
            String limit = (String)requestParams.get("limit");
            requestParams.put("start","");   // To get total customers from getCustomer(), start & limit removed. 
            requestParams.put("limit","");
            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams); // To Get Total Customer list.
            boolean quickSearchFlag = false;
            if(requestParams.containsKey("ss") && requestParams.get("ss") != null && (!StringUtil.isNullOrEmpty(requestParams.get("ss").toString()))) {
                quickSearchFlag = true;
            }
            ArrayList list = accCustomerDAOobj.getCustomerArrayList(result.getEntityList(), requestParams, quickSearchFlag, false); // To get child customers from total customerlist.
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            JSONArray jArr= accCustomerMainAccountingService.getCustomerJson(paramJobj, list);   
            if (!storageHandlerImpl.GetLowercaseCompanyId().contains(companyid)) {
                jArr = accCustomerMainAccountingService.getCustomerAmountDue(jArr, request);
                if (request.getParameter("custAmountDueMoreThanLimit") != null && Boolean.parseBoolean(request.getParameter("custAmountDueMoreThanLimit"))) {
                    JSONArray jSONCustAmountDueMoreThanLimit = new JSONArray();
                    for (int i = 0; i < jArr.length(); i++) {  // To get conditional data for this report, "for loop" is given.
                        JSONObject jSONObject = jArr.getJSONObject(i);
                        if (jSONObject.optDouble("limit", 0.0) < jSONObject.optDouble("amountdue", 0.0)) {
                            jSONCustAmountDueMoreThanLimit.put(jSONObject);
                        }
                    }
                    jArr = jSONCustAmountDueMoreThanLimit;
                }
            }
            JSONArray pagedJson = jArr;      // pagedJson is applied to get work paging properly.
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put(Constants.data, pagedJson);
            jobj.put("totalCount", jArr.length());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomersForDefaultCustomerList : "+ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
  
     public ModelAndView deleteCustomerAdressDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        Map<String, Object> auditRequestParams = new HashMap<String, Object>();
        String msg = "";
        String auditID = AuditAction.CUSTOMER_UPDATED;
        String auditMsg = "deleted";
        String aliasName = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String companyid = paramJobj.getString(Constants.companyKey);
                
            JSONArray jArr = new JSONArray(paramJobj.getString(Constants.data));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
                String rowid = StringUtil.DecodeText(obj.optString("rowid"));
                aliasName += StringUtil.DecodeText(obj.optString("aliasName"))+",";               
                if(!StringUtil.isNullOrEmpty(rowid)){
                    KwlReturnObject custAddrobject = accountingHandlerDAOobj.deleteCustomerAddressByID(rowid, companyid);
                }
            }
            aliasName=aliasName.substring(0,aliasName.length()-1);
            txnManager.commit(status);
            issuccess = true;
            
            msg = messageSource.getMessage("acc.field.addressdeletedsuccessfully", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + ".";//+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+level

           
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditMsg + " [" + aliasName + "] of Customer " + paramJobj.optString("accountname") + " ( " + paramJobj.optString("accountcode") + " ) ", auditRequestParams, paramJobj.optString("accountid"));
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "Error while deleting Address." ;//+ ex.getMessage()
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getInactiveCustomers(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = accCustomerController.getCustomerRequestMap(request);
            requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
            requestParams.put("df", authHandler.getDateOnlyFormat(request));  
            requestParams.put("ss", request.getParameter("ss"));  
            KwlReturnObject result = accCustomerDAOobj.getInactiveCustomer(requestParams);
            List list=result.getEntityList();
            JSONArray jArr= getInactiveCustomerJson(request, list);
            jobj.put(Constants.data, jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getInactiveCustomers : "+ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    public JSONArray getInactiveCustomerJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr=new JSONArray();
        try{
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[])itr.next(); 
                Account account = null;
                String tempstring="";
                String ss="";
                KwlReturnObject cstresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), String.valueOf(row[0]));
                Customer customer = (Customer) cstresult.getEntityList().get(0);
                if (customer == null) {
                    continue;
                }
                KwlReturnObject productjsonobj = accVendorCustomerProductDAOobj.getProductsByCustomer(customer.getID(), ss, null);
                List<CustomerProductMapping> listpro = productjsonobj.getEntityList();
                JSONArray customerjarray = new JSONArray();
                for (CustomerProductMapping CustomerProductObj : listpro) {
                    String customerproductsid = CustomerProductObj.getProducts().getID();
                    tempstring = tempstring.concat(customerproductsid+",");
                    customerjarray.put(customerproductsid);
                }
                if(!StringUtil.isNullOrEmpty(tempstring.toString())){
                    tempstring = tempstring.substring(0, tempstring.lastIndexOf(","));
                }
                
                if(customer.getAccount()!=null){
                    account=customer.getAccount();
                }
                JSONObject obj = new JSONObject();
                obj.put("acccode",(StringUtil.isNullOrEmpty(customer.getAcccode()))?"":customer.getAcccode());
                obj.put("accid", customer.getID());
                obj.put("accname", customer.getName());
                obj.put("accnamecode", (StringUtil.isNullOrEmpty(customer.getAcccode()))?customer.getName():"["+customer.getAcccode()+"]"+customer.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                
                // calculation of opening balance
                double openbalance = accInvoiceCMNobj.getOpeningBalanceOfAccount(request,null,true,customer.getID());
                obj.put("openbalance", openbalance);
                if(customer.getParent() != null) {
                    obj.put("parentid", customer.getParent().getID());
                    obj.put("parentname", customer.getParent().getName());
                }
                obj.put(Constants.currencyKey, account.getCurrency().getCurrencyID());
                obj.put("currencysymbol", account.getCurrency().getSymbol());
                obj.put("currencyname", account.getCurrency().getName());
                obj.put("level", row[2]);
                obj.put("leaf", row[3]);
                obj.put("title", customer.getTitle());
                obj.put("contactno2", customer.getAltContactNumber());                
                obj.put("pdm", customer.getPreferedDeliveryMode());
                obj.put("termname", customer.getCreditTerm()!=null ? customer.getCreditTerm().getTermname() : "");
                obj.put("termid", customer.getCreditTerm()!=null ? customer.getCreditTerm().getID() : "");
                obj.put("termdays", customer.getCreditTerm()!=null ?  customer.getCreditTerm().getTermdays(): "");
                obj.put("mappedSalesPersonId", ((customer.getMappingSalesPerson()!=null)?customer.getMappingSalesPerson().getID():""));
                obj.put("mappedMultiSalesPersonId", accCustomerMainAccountingService.getMultiSalesPersonIDs(customer.getID()));//fetching masteritem mapped to that customer.
                obj.put("mappedReceivedFromId", ((customer.getMappingReceivedFrom()!=null)?customer.getMappingReceivedFrom().getID():""));
                obj.put("nameinaccounts", customer.getAccount().getName());
                obj.put("bankaccountno", customer.getBankaccountno());
//                obj.put("billto", customer.getBillingAddress());
                obj.put("other", (customer.getOther() !=null)?customer.getOther():"");
                obj.put("deleted", customer.getAccount().isDeleted());
                obj.put("id", customer.getID());
                obj.put("taxno", customer.getTaxNo());
                obj.put("categoryid", accCustomerMainAccountingService.getCustomerCategoryIDs(account.getID()));
                obj.put("intercompanytypeid", account.getIntercompanytype()==null?"":account.getIntercompanytype().getID());
                obj.put("intercompany", account.isIntercompanyflag());
                obj.put("creationDate", customer.getCreatedOn() != null ? authHandler.getDateOnlyFormat(request).format(customer.getCreatedOn()) : "" );
                obj.put("country", (customer.getCountry()==null?"":customer.getCountry().getID()));
                obj.put("limit", customer.getCreditlimit());
                obj.put("mapcustomervendor", customer.isMapcustomervendor());
                obj.put("overseas", customer.isOverseas());
                obj.put("mappingaccid", customer.getAccount().getID());
                obj.put("sequenceformat", customer.getSeqformat()==null?"":customer.getSeqformat().getID());
                
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("customerid", customer.getID());       
                addrRequestParams.put(Constants.companyKey, companyid);       
                KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
                if(!addressResult.getEntityList().isEmpty()){
                    List <CustomerAddressDetails> casList=addressResult.getEntityList();
                    for(CustomerAddressDetails cas:casList){                         
                         //below address details used in exporting customer with all column option
                         //below key is set in column "dataindex" of default header table. so please do not chanage them.    
                        if (cas.isIsDefaultAddress() && cas.isIsBillingAddress()) {//for defult billing address
                            obj.put("billingAliasName", cas.getAliasName() != null ? cas.getAliasName() : "");
                            obj.put("billingAddress", cas.getAddress() != null ? cas.getAddress() : "");
                            obj.put("billingCity", cas.getCity() != null ? cas.getCity() : "");
                            obj.put("billingState", cas.getState() != null ? cas.getState() : "");
                            obj.put("billingCountry", cas.getCountry() != null ? cas.getCountry() : "");
                            obj.put("billingPostalCode", cas.getPostalCode() != null ? cas.getPostalCode() : "");
                            obj.put("billingPhone", cas.getPhone() != null ? cas.getPhone() : "");
                            obj.put("billingMobileNumber", cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                            obj.put("billingFax", cas.getFax() != null ? cas.getFax() : "");
                            obj.put("billingEmailID", cas.getEmailID() != null ? cas.getEmailID() : "");
                            obj.put("billingContactPerson", cas.getContactPerson() != null ? cas.getContactPerson() : "");
                            obj.put("billingContactPersonNumber", cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");         
                            obj.put("billingContactPersonDesignation", cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : "");    
                            obj.put("billingWebsite", cas.getWebsite() != null ? cas.getWebsite() : ""); 
                        } else if (cas.isIsDefaultAddress()) { //for defult shipping address
                            obj.put("shippingAliasName", cas.getAliasName() != null ? cas.getAliasName() : "");
                            obj.put("shippingAddress", cas.getAddress() != null ? cas.getAddress() : "");
                            obj.put("shippingCity", cas.getCity() != null ? cas.getCity() : "");
                            obj.put("shippingState", cas.getState() != null ? cas.getState() : "");
                            obj.put("shippingCountry", cas.getCountry() != null ? cas.getCountry() : "");
                            obj.put("shippingPostalCode", cas.getPostalCode() != null ? cas.getPostalCode() : "");
                            obj.put("shippingPhone", cas.getPhone() != null ? cas.getPhone() : "");
                            obj.put("shippingMobileNumber", cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                            obj.put("shippingFax", cas.getFax() != null ? cas.getFax() : "");
                            obj.put("shippingEmailID", cas.getEmailID() != null ? cas.getEmailID() : "");
                            obj.put("shippingContactPerson", cas.getContactPerson() != null ? cas.getContactPerson() : "");
                            obj.put("shippingContactPersonNumber", cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");
                            obj.put("shippingContactPersonDesignation", cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : "");
                            obj.put("shippingWebsite", cas.getWebsite() != null ? cas.getWebsite() : ""); 
                            obj.put("shippingRoute", cas.getShippingRoute() != null ? cas.getShippingRoute() : "");
                        }
                    }
                }
                obj.put("productname", tempstring);
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getInactiveCustomerJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }
    
    
    public ModelAndView getCustomersList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = accCustomerController.getCustomerRequestMap(request);

            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams, false, false);
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            JSONArray jArr= accCustomerMainAccountingService.getCustomerJson(paramJobj, list);
            jobj.put(Constants.data, jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomers : "+ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    
    
    public ModelAndView getCustomersByCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            boolean isPricingBandGrouping = request.getParameter("isPricingBandGrouping") != null ? Boolean.parseBoolean(request.getParameter("isPricingBandGrouping")) : false;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("isPricingBandGrouping", isPricingBandGrouping);
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                requestParams.put("dir", request.getParameter("dir"));
                requestParams.put("sort", request.getParameter("sort"));
            }
            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) venresult.getEntityList().get(0);
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                String userId = sessionHandlerImpl.getUserid(request);
                int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                String userRoleID=!StringUtil.isNullOrEmpty(sessionHandlerImpl.getRole(request)) ? sessionHandlerImpl.getRole(request) : "";
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                    /*View All permission = false
                    When user has view all permission=true and if "extraPref.isEnablesalespersonAgentFlow()" is true then show only those customers who have salesperson mapping with current user
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson. 
                     */
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                    
                    requestParams.put("hasViewAllPermission", false);
                    requestParams.put("isexcludeCustomersChecked", extraPref.isViewAllExcludeCustomer());
                }else if(!StringUtil.isNullOrEmpty(userRoleID) && !userRoleID.equalsIgnoreCase(Integer.toString(Constants.ADMIN_USER_ROLEID)) && extraPref.isViewAllExcludeCustomer()){
                    /*
                    
                    View All permission = true
                    userRoleID != Constants.ADMIN_USER_ROLEID - this check added becoz when " extraPref.isViewAllExcludeCustomer()"=true at that time admin should not get affected in any case admin should have full access
                    When  any user has View all permission code assigned then instead of treating that user admin apply filter while fetching customers to show in dropdown
                    */
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                    
                    
                    requestParams.put("hasViewAllPermission", true);
                    requestParams.put("isexcludeCustomersChecked", extraPref.isViewAllExcludeCustomer());
                }
            }
//            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
//            filter_names.add("company.companyID");
//            filter_params.add(sessionHandlerImpl.getCompanyid(request));
//            filter_names.add("ISaccount.deleted");
//            filter_params.add(false);
//            requestParams.put("filter_names", filter_names);
//            requestParams.put("filter_params", filter_params);
//            order_by.add("account.category");
//            order_type.add("desc");
//            requestParams.put("order_by", order_by);
//            requestParams.put("order_type", order_type);

            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put("categoryid", request.getParameter("categoryid"));

            KwlReturnObject result = accCustomerDAOobj.getNewCustomerList(requestParams);
            JSONArray jArr= getCustomersByCategoryJson(request, result.getEntityList());

            jobj.put(Constants.data, jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public String[] getCustomerCategoryIDs(String customerid) {
        JSONObject jobj = new JSONObject();
        String[] valuesStr = {"",""};
        boolean issuccess = false;
        try {
            KwlReturnObject result = accCustomerDAOobj.getCustomerCategoryIDs(customerid);
    
            List list = result.getEntityList();
            Iterator itr = list.iterator();

            while (itr.hasNext()) {
                CustomerCategoryMapping row = (CustomerCategoryMapping) itr.next();
                MasterItem masterItemObj= row.getCustomerCategory();
                if(itr.hasNext()) {
                    valuesStr[0] += masterItemObj.getID() + ",";
                    valuesStr[1] += masterItemObj.getValue()+ ",";
                } else {
                    valuesStr[0] += masterItemObj.getID();
                    valuesStr[1] += masterItemObj.getValue();
                }
            }
            issuccess = true;
        } catch (Exception e) {
            try {
                throw ServiceException.FAILURE(e.getMessage(), e);
            } catch (ServiceException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "");
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return valuesStr;
    }
    
    public String[] getMultiSalesPersonIDs(String customerid) {//fetching masteritem mapped to that customer.
        JSONObject jobj = new JSONObject();
        String[] valuesStr = {"",""};
        boolean issuccess = false;
        try {
            KwlReturnObject result = accCustomerDAOobj.getMultiSalesPersonIDs(customerid);
            if (result != null && result.getEntityList().size() > 0) {
                List list = result.getEntityList();
                Iterator itr = list.iterator();

                while (itr.hasNext()) {
                    SalesPersonMapping row = (SalesPersonMapping) itr.next();
                    MasterItem masterItemObj = row.getSalesperson();
                    if (itr.hasNext()) {
                        valuesStr[0] += masterItemObj.getID() + ",";
                        valuesStr[1] += masterItemObj.getValue() + ",";
                    } else {
                        valuesStr[0] += masterItemObj.getID();
                        valuesStr[1] += masterItemObj.getValue();
                    }
                }
                issuccess = true;
            }
        } catch (Exception e) {
            try {
                throw ServiceException.FAILURE(e.getMessage(), e);
            } catch (ServiceException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "");
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return valuesStr;
    }
    
    public JSONArray getCustomersByCategoryJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr=new JSONArray();
        try{
            boolean isPricingBandGrouping = request.getParameter("isPricingBandGrouping") != null ? Boolean.parseBoolean(request.getParameter("isPricingBandGrouping")) : false;
            boolean isBySalesPersonOrAgent = StringUtil.isNullOrEmpty(request.getParameter("isBySalesPersonOrAgent"))?false:Boolean.parseBoolean(request.getParameter("isBySalesPersonOrAgent"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                
                Object[] row = (Object[]) itr.next();
                String Custid = row[0].toString();
                String CategoryId = row[1] != null ? row[1].toString() : "";
                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), Custid);
                Customer customer = (Customer) custresult.getEntityList().get(0);
                MasterItem masterItem = null;
                if (!StringUtil.isNullOrEmpty(CategoryId) && !isPricingBandGrouping) {
                    KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), CategoryId);
                    masterItem = (MasterItem) catresult.getEntityList().get(0);
                }
                
                PricingBandMaster pricingBandMaster = null;
                if (!StringUtil.isNullOrEmpty(CategoryId) && isPricingBandGrouping) {
                    KwlReturnObject catresult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), CategoryId);
                    pricingBandMaster = (PricingBandMaster) catresult.getEntityList().get(0);
                }
                 
                Account account = customer.getAccount();
                JSONObject obj = new JSONObject();
                obj.put("acccode", (StringUtil.isNullOrEmpty(customer.getAcccode())) ? "" : customer.getAcccode());
                obj.put("accid", customer.getID());
                obj.put("accname", customer.getName());
                obj.put("aliasname", StringUtil.isNullOrEmpty(customer.getAliasname())?"":customer.getAliasname());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                // calculation of opening balance
                double openbalance = accInvoiceCMNobj.getOpeningBalanceOfAccount(request,null,true,customer.getID());
                
                obj.put("openbalance", openbalance);

                obj.put(Constants.currencyKey, (account.getCurrency() == null ? "" : account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (account.getCurrency() == null ? "" : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? "" : account.getCurrency().getName()));

                obj.put("title", customer.getTitle());
                obj.put("contactno2", customer.getAltContactNumber());
                obj.put("pdm", customer.getPreferedDeliveryMode());
                obj.put("termname", customer.getCreditTerm().getTermname());
                obj.put("termid", customer.getCreditTerm().getID());
                obj.put("termdays", customer.getCreditTerm().getTermdays());
                obj.put("nameinaccounts", customer.getAccount().getName());
                obj.put("bankaccountno", customer.getBankaccountno());
                obj.put("other", customer.getOther());
                obj.put("deleted", customer.getAccount().isDeleted());
                obj.put("id", customer.getID());
                obj.put("taxno", customer.getTaxNo());
                if (customer.isActivate()) {
                    obj.put("isactivate", "Active");
                } else {
                    obj.put("isactivate", "Dormant");
                }
                String[] multisalesperson = getMultiSalesPersonIDs(customer.getID());
                obj.put("salesPersonAgentId", multisalesperson[0]);
                obj.put("salesPersonAgent", multisalesperson[1]);
                    if (isPricingBandGrouping) {
                        obj.put("pricingBandID", pricingBandMaster == null ? "" : pricingBandMaster.getID());
                        obj.put("pricingBand", pricingBandMaster == null ? "" : pricingBandMaster.getName());
                    } 
                String[] category = getCustomerCategoryIDs(customer.getID()); 
                obj.put("categoryid", category[0]);
                obj.put("category", category[1]);
                        //obj.put("category", masterItem == null ? "" : masterItem.getValue());
                
                obj.put("creationDate", customer.getCreatedOn()!=null ?authHandler.getDateOnlyFormat(request).format(customer.getCreatedOn()): "");
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("customerid", customer.getID());       
                addrRequestParams.put(Constants.companyKey, companyid);       
                KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
                if(!addressResult.getEntityList().isEmpty()){
                    JSONArray addrArray=new JSONArray();
                    List <CustomerAddressDetails> casList=addressResult.getEntityList();
                    for(CustomerAddressDetails cas:casList){
                         JSONObject addrObject=new JSONObject();
                         addrObject.put("aliasName", cas.getAliasName()!=null?cas.getAliasName():"");             
                         addrObject.put("address", cas.getAddress()!=null?cas.getAddress():"");       
                         addrObject.put("city", cas.getCity()!=null?cas.getCity():"");       
                         addrObject.put("state", cas.getState()!=null?cas.getState():"");       
                         addrObject.put("country", cas.getCountry()!=null?cas.getCountry():"");       
                         addrObject.put("postalCode", cas.getPostalCode()!=null?cas.getPostalCode():"");       
                         addrObject.put("phone", cas.getPhone()!=null?cas.getPhone():"");       
                         addrObject.put("mobileNumber", cas.getMobileNumber()!=null?cas.getMobileNumber():"");       
                         addrObject.put("fax", cas.getFax()!=null?cas.getFax():"");       
                         addrObject.put("emailID", cas.getEmailID()!=null?cas.getEmailID():"");       
                         addrObject.put("contactPerson", cas.getContactPerson()!=null?cas.getContactPerson():"");       
                         addrObject.put("contactPersonNumber", cas.getContactPersonNumber()!=null?cas.getContactPersonNumber():"");       
                         addrObject.put("contactPersonDesignation", cas.getContactPersonDesignation()!=null?cas.getContactPersonDesignation():"");  
                         addrObject.put("website", cas.getWebsite() != null ? cas.getWebsite() : ""); 
                         addrObject.put("shippingRoute", cas.getShippingRoute()!=null?cas.getShippingRoute():"");       
                         addrObject.put("isDefaultAddress", cas.isIsDefaultAddress());  
                         addrObject.put("isBillingAddress", cas.isIsBillingAddress());  
                         addrArray.put(addrObject);
                         
                         //below address details used in exporting customer with all column option
                         //below key is set in column "dataindex" of default header table. so please do not chanage them.    
                        if (cas.isIsDefaultAddress() && cas.isIsBillingAddress()) {//for defult billing address
                            obj.put("billingAliasName", cas.getAliasName() != null ? cas.getAliasName() : "");
                            obj.put("billingAddress", cas.getAddress() != null ? cas.getAddress() : "");
                            obj.put("billingCity", cas.getCity() != null ? cas.getCity() : "");
                            obj.put("billingState", cas.getState() != null ? cas.getState() : "");
                            obj.put("billingCountry", cas.getCountry() != null ? cas.getCountry() : "");
                            obj.put("billingPostalCode", cas.getPostalCode() != null ? cas.getPostalCode() : "");
                            obj.put("billingPhone", cas.getPhone() != null ? cas.getPhone() : "");
                            obj.put("billingMobileNumber", cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                            obj.put("billingFax", cas.getFax() != null ? cas.getFax() : "");
                            obj.put("billingEmailID", cas.getEmailID() != null ? cas.getEmailID() : "");
                            obj.put("billingContactPerson", cas.getContactPerson() != null ? cas.getContactPerson() : "");
                            obj.put("billingContactPersonNumber", cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");         
                            obj.put("billingContactPersonDesignation", cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : ""); 
                            obj.put("billingWebsite", cas.getWebsite() != null ? cas.getWebsite() : ""); 
                        } else if (cas.isIsDefaultAddress()) { //for defult shipping address
                            obj.put("shippingAliasName", cas.getAliasName() != null ? cas.getAliasName() : "");
                            obj.put("shippingAddress", cas.getAddress() != null ? cas.getAddress() : "");
                            obj.put("shippingCity", cas.getCity() != null ? cas.getCity() : "");
                            obj.put("shippingState", cas.getState() != null ? cas.getState() : "");
                            obj.put("shippingCountry", cas.getCountry() != null ? cas.getCountry() : "");
                            obj.put("shippingPostalCode", cas.getPostalCode() != null ? cas.getPostalCode() : "");
                            obj.put("shippingPhone", cas.getPhone() != null ? cas.getPhone() : "");
                            obj.put("shippingMobileNumber", cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                            obj.put("shippingFax", cas.getFax() != null ? cas.getFax() : "");
                            obj.put("shippingEmailID", cas.getEmailID() != null ? cas.getEmailID() : "");
                            obj.put("shippingContactPerson", cas.getContactPerson() != null ? cas.getContactPerson() : "");
                            obj.put("shippingContactPersonNumber", cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");
                            obj.put("shippingContactPersonDesignation", cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : "");
                            obj.put("shippingWebsite", cas.getWebsite() != null ? cas.getWebsite() : ""); 
                            obj.put("shippingRoute", cas.getShippingRoute() != null ? cas.getShippingRoute() : "");
                        }
                    }
                    obj.put("addressDetails", addrArray);
                }
                
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCustomersByCategoryJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getCustomersBySalesPerson(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put("salesPersonAgentId", request.getParameter("salesPersonAgentId"));
            requestParams.put("isBySalesPersonOrAgent", request.getParameter("isBySalesPersonOrAgent"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                requestParams.put("dir", request.getParameter("dir"));
                requestParams.put("sort", request.getParameter("sort"));
            }
            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) venresult.getEntityList().get(0);
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                String userId = sessionHandlerImpl.getUserid(request);
                int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                String userRoleID=!StringUtil.isNullOrEmpty(sessionHandlerImpl.getRole(request)) ? sessionHandlerImpl.getRole(request) : "";
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                    /*View All permission = false
                    When user has view all permission=true and if "extraPref.isEnablesalespersonAgentFlow()" is true then show only those customers who have salesperson mapping with current user
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson. 
                     */
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                    
                    requestParams.put("hasViewAllPermission", false);
                    requestParams.put("isexcludeCustomersChecked", extraPref.isViewAllExcludeCustomer());
                }else if(!StringUtil.isNullOrEmpty(userRoleID) && !userRoleID.equalsIgnoreCase(Integer.toString(Constants.ADMIN_USER_ROLEID)) && extraPref.isViewAllExcludeCustomer()){
                    /*
                    
                    View All permission = true
                    userRoleID != Constants.ADMIN_USER_ROLEID - this check added becoz when " extraPref.isViewAllExcludeCustomer()"=true at that time admin should not get affected in any case admin should have full access
                    When  any user has View all permission code assigned then instead of treating that user admin apply filter while fetching customers to show in dropdown
                    */
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                    
                    
                    requestParams.put("hasViewAllPermission", true);
                    requestParams.put("isexcludeCustomersChecked", extraPref.isViewAllExcludeCustomer());
                }
            }
            KwlReturnObject result = accCustomerDAOobj.getNewCustomerList(requestParams);
            JSONArray jArr= getCustomersByCategoryJson(request, result.getEntityList());
            
            jobj.put(Constants.data, jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getCustomerCreditExceptions(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = accCustomerController.getCustomerRequestMap(request);
            requestParams.remove("start");
            requestParams.remove("limit");
            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            List<Customer> customerList=result.getEntityList();            
            JSONArray jArr=getExceedLimitCustomerJson(request,customerList);
            Integer start = request.getParameter("start")!=null?Integer.parseInt(request.getParameter("start")):0;
            Integer limit = request.getParameter("limit")!=null?Integer.parseInt(request.getParameter("limit")):30;
            JSONArray pagedJson=StringUtil.getPagedJSON(jArr, start, limit);
            jobj.put(Constants.data, pagedJson);           
            jobj.put("count", jArr.length());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomerCreditExceptions : "+ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getCustomerCreditExceptionInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONArray jArr = new JSONArray();
            KwlReturnObject cstresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), request.getParameter("customer"));
            Customer customer = (Customer) cstresult.getEntityList().get(0);

            JSONObject obj = new JSONObject();
            obj.put("accid", customer.getAccount().getID());
            obj.put("custId", customer.getID());
            obj.put("custName", customer.getName());
            jArr.put(obj);
            jArr = addCustomerWithExceedLimit(jArr, request, true);
            jobj.put(Constants.data, jArr);
            jobj.put("totalCount", jArr.length());
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomerCreditExceptionInvoices : "+ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public JSONArray addCustomerWithExceedLimit(JSONArray jArr, HttpServletRequest request, boolean createInvoiceJSON){
        JSONArray exceededCustomers = new JSONArray();
    	try{
    		HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
    		requestParams.put("nondeleted", "true");
    		requestParams.put("deleted", "false");
    		for(int i = 0; i < jArr.length(); i++){
                    String accid = jArr.getJSONObject(i).getString("accid");                    
                    if(jArr.getJSONObject(i).has("custId")){
                       String customerid = jArr.getJSONObject(i).getString("custId");  
                       requestParams.put(Constants.customerid,customerid);
                    }                    
                    
                    double creditlimit = 0;
                    if(!createInvoiceJSON)
                        creditlimit = Double.parseDouble(jArr.getJSONObject(i).getString("limit"));
//                    requestParams.put(InvoiceConstants.accid, accid);
                    if(jArr.getJSONObject(i).has("startdate"))
                    {
                        requestParams.put(Constants.REQ_startdate, jArr.getJSONObject(i).get("startdate").toString());
                    }
                    if(jArr.getJSONObject(i).has("enddate"))
                    {
                        requestParams.put(Constants.REQ_enddate, jArr.getJSONObject(i).get("enddate").toString());
                    }                                          
                    
                    double amountdue = 0;
                    KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
                    if(result.getEntityList() != null){
                        List<Invoice> invoiceList = result.getEntityList();
                        for (Invoice invoice  : invoiceList) {
                            if(Constants.InvoiceAmountDueFlag) {
                                List ll = accInvoiceCMNobj.getInvoiceDiscountAmountInfo(requestParams, invoice);
                                amountdue = amountdue + (Double) ll.get(0);
                            } else {
                                List ll = accInvoiceCMNobj.getAmountDue_Discount(requestParams, invoice);
                                amountdue = amountdue + (Double) ll.get(0);
                            }
                            if(createInvoiceJSON) {
                                JSONObject jObj = new JSONObject();
                                jObj.put("invoiceId", invoice.getID());
                                jObj.put("invoiceNo", invoice.getInvoiceNumber());
                                exceededCustomers.put(jObj);
                            }
                                    
                        }
//                        jArr.getJSONObject(i).put("amountdue", amountdue);
                    }
                    
                    if(amountdue>creditlimit && !createInvoiceJSON) {
                        jArr.getJSONObject(i).put("amountdue", amountdue);
                        exceededCustomers.put(jArr.getJSONObject(i));
                    }
    		}
    	}   catch (Exception ex){
    		Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
    	}
    	return exceededCustomers;
    }
    
    public ModelAndView exportCustomerCreditLimitExceed(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = accCustomerController.getCustomerRequestMap(request);
            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            List<Customer> customerList=result.getEntityList();
            JSONArray jArr=getExceedLimitCustomerJson(request,customerList);
            jobj.put(Constants.data, jArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put(Constants.RES_success, true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
     public ModelAndView getCustomerExceedingCreditLimit(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            if (request.getParameter("customer") != null ){
            paramJobj.put("customer",request.getParameter("customer"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("totalSUM"))){
            paramJobj.put("totalSUM",request.getParameter("totalSUM"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isOrder"))){
            paramJobj.put("isOrder",request.getParameter("isOrder"));
            }
            jobj = accCustomerMainAccountingService.getCustomerExceedingCreditLimit(paramJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
       
   
   
    public ModelAndView exportCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = accCustomerController.getCustomerRequestMap(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = null;
            ArrayList list =null;
            JSONArray jArr = null;
            boolean quickSearchFlag = false;
            if(requestParams.containsKey("ss") && requestParams.get("ss") != null && (!StringUtil.isNullOrEmpty(requestParams.get("ss").toString()))) {
                quickSearchFlag = true;
            }
            boolean inactiveCustomer = Boolean.FALSE.parseBoolean(request.getParameter("inactiveCustomer"));
            request.setAttribute(Constants.isExport, true);
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            if(inactiveCustomer){
                 requestParams.put("df", authHandler.getDateOnlyFormat(request));  
                 result = accCustomerDAOobj.getInactiveCustomer(requestParams);
                 list=new ArrayList(result.getEntityList());
                 jArr= getInactiveCustomerJson(request, list);
            }else{
                result = accCustomerDAOobj.getCustomer(requestParams);
                list = accCustomerDAOobj.getCustomerArrayList(result.getEntityList(), requestParams, quickSearchFlag, false);
                 jArr= accCustomerMainAccountingService.getCustomerJson(paramJobj, list);
            }
            
            /* Code for calculating AmountDue for customer */
//            if (!storageHandlerImpl.GetLowercaseCompanyId().contains(companyid)) {
//                jArr = accCustomerMainAccountingService.getCustomerAmountDue(jArr, paramJobj);
//            }
           
//            if(!storageHandlerImpl.GetLowercaseCompanyId().contains(companyid)){ 
//                jArr = accCustomerMainAccountingService.getCustomerAmountDue(jArr, request);
//                if(request.getParameter("custAmountDueMoreThanLimit")!=null&&Boolean.parseBoolean(request.getParameter("custAmountDueMoreThanLimit")))
//                {
//                    JSONArray jSONCustAmountDueMoreThanLimit=new JSONArray();
//                    for(int i=0;i<jArr.length();i++){
//                        JSONObject jSONObject=jArr.getJSONObject(i);
//                        if(jSONObject.optDouble("limit",0.0)<jSONObject.optDouble("amountdue",0.0)){
//                            jSONCustAmountDueMoreThanLimit.put(jSONObject);
//                        }
//                    }
//                    jArr=jSONCustAmountDueMoreThanLimit; 
//                    
//                }
//            } 
            jobj.put(Constants.data, jArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put(Constants.RES_success, true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public ModelAndView exportCustomerListBySalesPerson(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String view = "jsonView_ex";
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put("salesPersonAgentId", request.getParameter("salesPersonAgentId"));
            requestParams.put("isBySalesPersonOrAgent", request.getParameter("isBySalesPersonOrAgent"));
            requestParams.put("categoryid", !StringUtil.isNullOrEmpty(request.getParameter("categoryid"))  ? request.getParameter("categoryid") :"");
            KwlReturnObject result = accCustomerDAOobj.getNewCustomerList(requestParams);
            JSONArray jArr = getCustomersByCategoryJson(request, result.getEntityList());
            jobj.put(Constants.data, jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put(Constants.RES_success, true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public ModelAndView getCurrencyInfo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        try {
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String customerid = request.getParameter("customerid");
            
            boolean isBilling = (request.getParameter("isBilling") == null)? false : Boolean.parseBoolean(request.getParameter("isBilling"));
            boolean getLoanEligibility = (request.getParameter("getLoanEligibility") == null)? false : Boolean.parseBoolean(request.getParameter("getLoanEligibility"));
            
            result = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
            Customer customer = (Customer) result.getEntityList().get(0);
            
            JSONArray jArr = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("accid", customer.getID());
            obj.put("custId", customer.getID());
            obj.put("custName", customer.getName());
            jArr.put(obj);           
            double amountDue=0.0;
            if(!storageHandlerImpl.GetLowercaseCompanyId().contains(companyid)){ 
                JSONArray customerAmountDueJArr = accCustomerMainAccountingService.getCustomerAmountDue(jArr, paramJobj);
                amountDue=customerAmountDueJArr.getJSONObject(0).getDouble("amountdue");
            }    
            
            KwlReturnObject resultTrans = accCustomerDAOobj.getLastTransactionCustomer(customer.getID(), isBilling);
            List list = resultTrans.getEntityList();
            Iterator it = list.iterator();
            if(it.hasNext()){
                Object taxid = (Object)it.next();
                if(taxid == null) {
                    jobj.put("taxid","");
                } else {
                    jobj.put("taxid",taxid.toString());
                }
                
            } else {
                jobj.put("taxid","");
            }
            
            if(getLoanEligibility){
                double annualIncome=customer.getIncome();
                HashMap<String, Object> requestParams = new HashMap<>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyid);
                requestParams.put(Constants.filterNamesKey, filter_names);
                requestParams.put(Constants.filterParamsKey, filter_params);
                KwlReturnObject resultList = accCustomerDAOobj.getLoanRules(requestParams);
                List<LoanRules>  ruleList = resultList.getEntityList();
                for (LoanRules loanRules : ruleList) {
                    if (loanRules.getLoanRuleType() == LoanRuleType.ABSOLUTEVALUE) {
                        if(annualIncome >= loanRules.getMinIncome() && annualIncome <= loanRules.getMaxIncome()){
                            jobj.put("maximumloanEligibility", loanRules.getEligibility());
                            jobj.put("loanRuleType", loanRules.getLoanRuleType());
                            jobj.put("loanRuleTypeValue", 0);
                            break;
                        }
                        
                    } else if (loanRules.getLoanRuleType() == LoanRuleType.MULTIPLEOFSALARY) {
                        if(annualIncome >= loanRules.getMinIncome() && annualIncome <= loanRules.getMaxIncome()){
                            jobj.put("maximumloanEligibility", loanRules.getEligibility()* annualIncome);
                             jobj.put("loanRuleType", loanRules.getLoanRuleType());
                            jobj.put("loanRuleTypeValue", 1);
                            break;
                        }
                    } else if (loanRules.getLoanRuleType() == LoanRuleType.UNLIMITED) {
                        jobj.put("maximumloanEligibility", loanRules.getEligibility());
                        jobj.put("loanRuleType", loanRules.getLoanRuleType());
                        jobj.put("loanRuleTypeValue", 2);
                        break;
                        
                    }
                }
                
            }
            
            jobj.put("amountdue",amountDue);
            jobj.put(Constants.currencyKey,customer.getCurrency()==null?currency.getCurrencyID():customer.getCurrency().getCurrencyID());
            jobj.put("currencysymbol",customer.getCurrency()==null?currency.getSymbol():customer.getCurrency().getSymbol());
            jobj.put("currencyname",customer.getCurrency()==null?currency.getName():customer.getCurrency().getName());
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCurrencyInfo : " + ex;
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getCustomerProductsMappingFunction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                requestParams.put("ss", "");
            }
            String ss = request.getParameter("ss");
            JSONObject DataJArr = getProductCustomerMappingJsonFunction(request, ss);
            int totalCount=DataJArr.getInt("count");
            JSONArray JArr=DataJArr.getJSONArray(Constants.data);
            jobj.put(Constants.data, JArr);
            jobj.put("totalCount", totalCount);
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerControllerCMN.getCustomerProductsMappingFunction : " + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
 //Function to map the products to the vendors-Neeraj D
    public ModelAndView getCustomerProductsMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
             KwlReturnObject resultcustomer = accCustomerDAOobj.getCustomer(requestParams);
            if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                requestParams.put("ss", "");
            }
            KwlReturnObject resultproduct = accProductObj.getProducts(requestParams);
            //HashMap<String, Object> requestParams1 = accCustomerController.getCustomerRequestMap(request);
//            KwlReturnObject resultcustomer = accCustomerDAOobj.getCustomer(requestParams);
            boolean quickSearchFlag = false;
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null && (!StringUtil.isNullOrEmpty(requestParams.get("ss").toString()))) {
                quickSearchFlag = true;
            }
            List listproduct = resultproduct.getEntityList();
            int count = listproduct.size();
            List listcustomer = resultcustomer.getEntityList();
            List pagingList = listproduct;
            String ss = request.getParameter("ss");
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagingList = StringUtil.getPagedList(listproduct, Integer.parseInt(start), Integer.parseInt(limit));
            }

            JSONArray DataJArr = getProductCustomerMappingJson(request, listproduct, listcustomer, ss);
            jobj.put(Constants.data, DataJArr);
            jobj.put("totalCount", resultproduct.getRecordTotalCount());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerControllerCMN.getCustomerProductsMapping : " + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    public JSONObject getProductCustomerMappingJsonFunction(HttpServletRequest request, String ss) throws SessionExpiredException, ServiceException, JSONException, UnsupportedEncodingException {
        JSONArray jArr = new JSONArray();
        JSONObject jObj=new JSONObject();
        Producttype producttype = new Producttype();
        Boolean nonSaleInventory = Boolean.parseBoolean((String) request.getParameter("loadInventory"));
        KwlReturnObject purchaseprice = null, saleprice = null, quantity = null, initialquantity = null, initialprice = null, salespricedatewise = null, purchasepricedatewise = null, initialsalesprice = null;

        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            int start = Integer.parseInt(request.getParameter("start"));
             int limit = Integer.parseInt(request.getParameter("limit"));
            KwlReturnObject result = accVendorCustomerProductDAOobj.getProductsByCustomerfunction(companyId, ss,start,limit);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray vendorjarray = new JSONArray();
            while (itr.hasNext()) {
                CustomerProductMapping customerProductObj = (CustomerProductMapping) itr.next();
                String customerProductsId = customerProductObj.getProducts().getID();
                KwlReturnObject customerResult = accProductObj.getObject(Customer.class.getName(), customerProductObj.getCustomer().getID());
                Customer customer = (Customer) customerResult.getEntityList().get(0);
                KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), customerProductsId);
                Product product = (Product) prodresult.getEntityList().get(0);
                Product parentProduct = product.getParent();
                purchaseprice = accProductObj.getProductPrice(product.getID(), true, null, "", "");
                saleprice = accProductObj.getProductPrice(product.getID(), false, null, "", "");
                quantity = accProductObj.getQuantity(product.getID());
                initialquantity = accProductObj.getInitialQuantity(product.getID());
                initialprice = accProductObj.getInitialPrice(product.getID(), true);
                salespricedatewise = accProductObj.getProductPrice(product.getID(), false, null, null, "");
                purchasepricedatewise = accProductObj.getProductPrice(product.getID(), true, null, null, "");
                initialsalesprice = accProductObj.getInitialPrice(product.getID(), false);

                Boolean isSearch = false;
                if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                    isSearch = true;
                }
                JSONObject obj = new JSONObject();
                obj.put("productid", product.getID());
                obj.put("productname", product.getName());
                obj.put("description", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                obj.put("desc", product.getDescription());
                UnitOfMeasure uom = product.getUnitOfMeasure();
                obj.put("uomid", uom == null ? "" : uom.getID());
                obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                obj.put("precision", uom == null ? 0 : (Integer) uom.getAllowedPrecision());
                obj.put("leadtime", product.getLeadTimeInDays());
                obj.put("warrantyperiod", product.getWarrantyperiod());
                obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
                obj.put("supplier", product.getSupplier());
                obj.put("coilcraft", product.getCoilcraft());
                obj.put("interplant", product.getInterplant());
                obj.put("syncable", product.isSyncable());
                obj.put("multiuom", product.isMultiuom());
                obj.put("uomschematypeid", product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "");
                obj.put("qaenable", product.isQaenable());
                obj.put("reorderlevel", product.getReorderLevel());
                obj.put("reorderquantity", product.getReorderQuantity());
                obj.put("purchaseaccountid", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : ""));
                obj.put("salesaccountid", (product.getSalesAccount() != null ? product.getSalesAccount().getID() : ""));
                obj.put("shelfLocationId", (product.getShelfLocation() != null ? product.getShelfLocation().getId() : ""));
                obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
                obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
                obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
                obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName() : ""));
                obj.put("pid", product.getProductid());
                obj.put("productweight", (Double) product.getProductweight());
                obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
                if (product.getWarrantyperiod() == 0) {
                    obj.put("warranty", "N/A");
                } else {
                    obj.put("warranty", product.getWarrantyperiod());
                }
                if (product.getWarrantyperiodsal() == 0) {
                    obj.put("warrantysal", "N/A");
                } else {
                    obj.put("warrantysal", product.getWarrantyperiodsal());
                }
                obj.put("parentuuid", parentProduct == null ? "" : parentProduct.getID());
                obj.put("parentid", parentProduct == null ? "" : parentProduct.getProductid());
                obj.put("parentname", parentProduct == null ? "" : parentProduct.getName());
                obj.put("purchaseprice", purchaseprice.getEntityList().get(0));
                obj.put("saleprice", saleprice.getEntityList().get(0));
                obj.put("quantity", quantity.getEntityList().get(0));
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                obj.put("accname", customer.getName());
                obj.put("aliasname", customer.getAliasname());
                if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                } else {
                    jArr.put(obj);
                }
            }
            jObj.put("count",result.getRecordTotalCount());
            jObj.put(Constants.data,jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getProductCustomerMappingJson : " + ex.getMessage(), ex);
        }
        return jObj;
    }
    
    
//Getting the customer mapped product json-NeerajD
    public JSONArray getProductCustomerMappingJson(HttpServletRequest request, List listproduct, List listcustomer, String ss) throws SessionExpiredException, ServiceException, JSONException, UnsupportedEncodingException {
        Iterator itr = listcustomer.iterator();
        JSONArray jArr = new JSONArray();
        Producttype producttype = new Producttype();
        Boolean nonSaleInventory = Boolean.parseBoolean((String) request.getParameter("loadInventory"));
        KwlReturnObject purchaseprice = null, saleprice = null, quantity = null, initialquantity = null, initialprice = null, salespricedatewise = null, purchasepricedatewise = null, initialsalesprice = null;
        try {
            while (itr.hasNext()) {
                Customer customer = (Customer) itr.next();
                JSONArray jsonarray = new JSONArray();
                String Custid = customer.getID();
                JSONObject productjsonobj = getProductCustomernames(Custid, ss);//getting the vendor's productsid
                if (productjsonobj.has("productjarray")) {
                    jsonarray = (JSONArray) productjsonobj.getJSONArray("productjarray");
                }

                if (jsonarray.length() > 0) {
                    for (int cnt = 0; cnt < jsonarray.length(); cnt++) {
                        Iterator iterator = listproduct.iterator();
                        while (iterator.hasNext()) {
                            Object[] rowproduct = (Object[]) iterator.next();
                            Product Prod = (Product) rowproduct[0];
                            String Prodid = Prod.getID();
//Comparing the productid of Customer with the productid of all products array
                            if (jsonarray.get(cnt).equals(Prodid)) {
                                String CategoryId = rowproduct[1] != null ? rowproduct[1].toString() : "";
                                KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), Prodid);
                                Product product = (Product) prodresult.getEntityList().get(0);
                                MasterItem masterItem = null;
                                if (!StringUtil.isNullOrEmpty(CategoryId)) {
                                    KwlReturnObject catresult = accProductObj.getObject(MasterItem.class.getName(), CategoryId);
                                    masterItem = (MasterItem) catresult.getEntityList().get(0);
                                }
                                Product parentProduct = product.getParent();
                                purchaseprice = accProductObj.getProductPrice(product.getID(), true, null, "","");
                                saleprice = accProductObj.getProductPrice(product.getID(), false, null, "","");
                                quantity = accProductObj.getQuantity(product.getID());
                                initialquantity = accProductObj.getInitialQuantity(product.getID());
                                initialprice = accProductObj.getInitialPrice(product.getID(), true);
                                salespricedatewise = accProductObj.getProductPrice(product.getID(), false, null, null,"");
                                purchasepricedatewise = accProductObj.getProductPrice(product.getID(), true, null, null,"");
                                initialsalesprice = accProductObj.getInitialPrice(product.getID(), false);

                                Boolean isSearch = false;
                                if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                                    isSearch = true;
                                }

//                                pcObject = (ProductCyclecount) rowproduct[8];
                                JSONObject obj = new JSONObject();
                                obj.put("productid", product.getID());
                                obj.put("productname", product.getName());
                                obj.put("description", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                                obj.put("desc", product.getDescription());
                                UnitOfMeasure uom = product.getUnitOfMeasure();
                                obj.put("uomid", uom == null ? "" : uom.getID());
                                obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                                obj.put("precision", uom == null ? 0 : (Integer) uom.getAllowedPrecision());
                                obj.put("leadtime", product.getLeadTimeInDays());
                                //if(product.getWarrantyperiod() !=-1){
                                obj.put("warrantyperiod", product.getWarrantyperiod());
                                obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
                                // }

                                obj.put("supplier", product.getSupplier());
                                obj.put("coilcraft", product.getCoilcraft());
                                obj.put("interplant", product.getInterplant());
                                obj.put("syncable", product.isSyncable());
                                obj.put("multiuom", product.isMultiuom());
                                obj.put("uomschematypeid", product.getUomSchemaType()!=null?product.getUomSchemaType().getID():"");
                                obj.put("qaenable", product.isQaenable());
                                obj.put("reorderlevel", product.getReorderLevel());
                                obj.put("reorderquantity", product.getReorderQuantity());
                                obj.put("purchaseaccountid", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : ""));
                                obj.put("salesaccountid", (product.getSalesAccount() != null ? product.getSalesAccount().getID() : ""));
                                obj.put("shelfLocationId", (product.getShelfLocation() != null ? product.getShelfLocation().getId() : ""));
                                //            obj.put("salesacctaxcode", "c340667e2896c0d80128a569f065017a");//(product.getPurchaseAccount()!=null?product.getPurchaseAccount().getID():""));
                                //            obj.put("purchaseacctaxcode", "c340667e2896c0d80128a569f065017a");//(product.getSalesAccount()!=null?product.getSalesAccount().getID():""));
                                obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
                                obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
                                obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                                obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                                obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                                obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                                obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                                obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
                                obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                                obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName() : ""));
                                obj.put("pid", product.getProductid());
                                obj.put("productweight", (Double)product.getProductweight());
                                obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                                obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                                obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                                obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
                                if (product.getWarrantyperiod() == 0) {
                                    obj.put("warranty", "N/A");
                                } else {
                                    obj.put("warranty", product.getWarrantyperiod());
                                }
                                if (product.getWarrantyperiodsal() == 0) {
                                    obj.put("warrantysal", "N/A");
                                } else {
                                    obj.put("warrantysal", product.getWarrantyperiodsal());
                                }

                                obj.put("parentuuid", parentProduct == null ? "" : parentProduct.getID());
                                obj.put("parentid", parentProduct == null ? "" : parentProduct.getProductid());
                                obj.put("parentname", parentProduct == null ? "" : parentProduct.getName());
                                if (isSearch) {
                                    obj.put("level", 0);
                                    obj.put("leaf", true);
                                } else {
                                    obj.put("level", rowproduct[1]);
                                    obj.put("leaf", rowproduct[2]);
                                }

                                obj.put("purchaseprice", rowproduct[3] == null ? 0 : rowproduct[3]);
                                obj.put("saleprice", rowproduct[4] == null ? 0 : rowproduct[4]);
                                obj.put("quantity", (rowproduct[5] == null ? 0 : rowproduct[5]));
                                obj.put("initialquantity", (rowproduct[6] == null ? 0 : rowproduct[6]));
                                obj.put("initialprice", (rowproduct[7] == null ? 0 : rowproduct[7]));
                                obj.put("salespricedatewise", (rowproduct[9] == null ? 0 : rowproduct[9]));
                                obj.put("purchasepricedatewise", (rowproduct[10] == null ? 0 : rowproduct[10]));
                                obj.put("initialsalesprice", (rowproduct[11] == null ? 0 : rowproduct[11]));
                                //Adding vendor to each product;
                                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                                obj.put("accname", customer.getName());
                                obj.put("aliasname", customer.getAliasname());
                                //  obj.put("createdon", (row[12]==null?"":sdf.format(row[12])));
                                //            jArr.put(obj);
                                if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                                    // Do Nothing
                                } else {
                                    jArr.put(obj);
                                }
                            }
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("getProductCustomerMappingJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    //getting the product names of CUSTOMER -Neeraj D
    public JSONObject getProductCustomernames(String customerid, String ss) throws JSONException {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject result = accVendorCustomerProductDAOobj.getProductsByCustomer(customerid, ss, null);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray vendorjarray = new JSONArray();
            while (itr.hasNext()) {
                CustomerProductMapping CustomerProductObj = (CustomerProductMapping) itr.next();
                String customerproductsid = CustomerProductObj.getProducts().getID();
                vendorjarray.put(customerproductsid);
                jobj.put("productjarray", vendorjarray);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    private JSONArray getExceedLimitCustomerJson(HttpServletRequest request, List<Customer> entityList) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (entityList != null && !entityList.isEmpty()) {
                for (Customer customer : entityList) {
                    double custLimit = customer.getCreditlimit();
                    String custCurrency = customer.getCurrency().getCurrencyID();
                    Date creationDate = customer.getCreatedOn();
                    HashMap<String, Object> rParams = AccountingManager.getGlobalParams(request);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(rParams, custLimit, custCurrency, creationDate, 0);
                    double custLimitInBase = (Double) bAmt.getEntityList().get(0);

                    JSONObject obj = new JSONObject();
                    obj.put("accid", customer.getID()); 
                    obj.put("custId", customer.getID()); 
                    
                    HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.remove("ss");
                    request.removeAttribute("ss");
                                     
                    accCustomerMainAccountingService.getCustomerAmountDueByObject(obj, request, requestParams);                    
                    double amountDueInBase = obj.optDouble("amountdue", 0.0);
                    
                    if (amountDueInBase > custLimitInBase) {
                        obj.put("amountdue", authHandler.round(amountDueInBase, companyid));
                        obj.put("acccode", (StringUtil.isNullOrEmpty(customer.getAcccode())) ? "" : customer.getAcccode());                        
                        obj.put("accname", customer.getName());
                        obj.put("accnamecode", (StringUtil.isNullOrEmpty(customer.getAcccode())) ? customer.getName() : "[" + customer.getAcccode() + "]" + customer.getName());

                        // calculation of opening balance
                        double openbalance = accInvoiceCMNobj.getOpeningBalanceOfAccount(request, null, true, customer.getID());
                        obj.put("openbalance", openbalance);
                        if (customer.getParent() != null) {
                            obj.put("parentid", customer.getParent().getID());
                            obj.put("parentname", customer.getParent().getName());
                        }
                        obj.put(Constants.currencyKey, customer.getCurrency() == null ? "" : customer.getCurrency().getCurrencyID());
                        obj.put("currencysymbol", customer.getCurrency() == null ? "" : customer.getCurrency().getSymbol());
                        obj.put("currencyname", customer.getCurrency() == null ? "" : customer.getCurrency().getName());
                        obj.put("title", customer.getTitle());
                        obj.put("isPermOrOnetime", customer.isIsPermOrOnetime());
                        obj.put("pdm", customer.getPreferedDeliveryMode());
                        obj.put("termname", customer.getCreditTerm() != null ? customer.getCreditTerm().getTermname() : "");
                        obj.put("termid", customer.getCreditTerm() != null ? customer.getCreditTerm().getID() : "");
                        obj.put("termdays", customer.getCreditTerm() != null ? customer.getCreditTerm().getTermdays() : "");
                        obj.put("mappedSalesPersonId", ((customer.getMappingSalesPerson() != null) ? customer.getMappingSalesPerson().getID() : ""));
                        obj.put("mappedSalesPersonName", ((customer.getMappingSalesPerson() != null) ? customer.getMappingSalesPerson().getValue() : ""));
                        String[] multisalesperson=accCustomerMainAccountingService.getMultiSalesPersonIDs(customer.getID());//fetching masteritem mapped to that customer.
                        obj.put("mappedMultiSalesPersonId", multisalesperson[0]);
                        obj.put("mappedReceivedFromId", ((customer.getMappingReceivedFrom() != null) ? customer.getMappingReceivedFrom().getID() : ""));
                        obj.put("bankaccountno", customer.getBankaccountno());
//                        obj.put("billto", customer.getBillingAddress());
                        obj.put("other", (customer.getOther() != null) ? customer.getOther() : "");
                        obj.put("deleted", customer.getAccount().isDeleted());
                        obj.put("id", customer.getID());
                        obj.put("taxno", customer.getTaxNo());
                        obj.put("taxId", customer.getTaxid());
                        String[] category = accCustomerMainAccountingService.getCustomerCategoryIDs(customer.getID());
                        obj.put("categoryid", category[0]);
                        obj.put("categoryname", category[1]);
                        obj.put("intercompanytypeid", customer.getIntercompanytype() != null ? customer.getIntercompanytype().getID() : "");
                        obj.put("intercompany", customer.isIntercompanyflag());
                        obj.put("creationDate", customer.getCreatedOn() != null ? authHandler.getUserDateFormatterWithoutTimeZone(request).format(customer.getCreatedOn()) : "");
                        obj.put("country", (customer.getCountry() == null ? "" : customer.getCountry().getID()));
                        obj.put("limit", custLimitInBase);
                        obj.put("mapcustomervendor", customer.isMapcustomervendor());
                        obj.put("overseas", customer.isOverseas());
                        obj.put("mappingaccid", customer.getAccount().getID());
                        obj.put("sequenceformat", customer.getSeqformat() == null ? "" : customer.getSeqformat().getID());

                        HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                        addrRequestParams.put("customerid", customer.getID());
                        addrRequestParams.put(Constants.companyKey, companyid);
                        KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
                        if (!addressResult.getEntityList().isEmpty()) {
                            List<CustomerAddressDetails> casList = addressResult.getEntityList();
                            for (CustomerAddressDetails cas : casList) {
                                //below address details used in exporting customer with all column option
                                //below key is set in column "dataindex" of default header table. so please do not chanage them.    
                                if (cas.isIsDefaultAddress() && cas.isIsBillingAddress()) {//for defult billing address
                                    obj.put("billingAliasName", cas.getAliasName() != null ? cas.getAliasName() : "");
                                    obj.put("billingAddress", cas.getAddress() != null ? cas.getAddress() : "");
                                    obj.put("billingCity", cas.getCity() != null ? cas.getCity() : "");
                                    obj.put("billingState", cas.getState() != null ? cas.getState() : "");
                                    obj.put("billingCountry", cas.getCountry() != null ? cas.getCountry() : "");
                                    obj.put("billingPostalCode", cas.getPostalCode() != null ? cas.getPostalCode() : "");
                                    obj.put("billingPhone", cas.getPhone() != null ? cas.getPhone() : "");
                                    obj.put("billingMobileNumber", cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                                    obj.put("billingFax", cas.getFax() != null ? cas.getFax() : "");
                                    obj.put("billingEmailID", cas.getEmailID() != null ? cas.getEmailID() : "");
                                    obj.put("billingContactPerson", cas.getContactPerson() != null ? cas.getContactPerson() : "");
                                    obj.put("billingContactPersonNumber", cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");
                                    obj.put("billingContactPersonDesignation", cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : "");
                                    obj.put("billingWebsite", cas.getWebsite() != null ? cas.getWebsite() : "");
                                } else if (cas.isIsDefaultAddress()) { //for defult shipping address
                                    obj.put("shippingAliasName", cas.getAliasName() != null ? cas.getAliasName() : "");
                                    obj.put("shippingAddress", cas.getAddress() != null ? cas.getAddress() : "");
                                    obj.put("shippingCity", cas.getCity() != null ? cas.getCity() : "");
                                    obj.put("shippingState", cas.getState() != null ? cas.getState() : "");
                                    obj.put("shippingCountry", cas.getCountry() != null ? cas.getCountry() : "");
                                    obj.put("shippingPostalCode", cas.getPostalCode() != null ? cas.getPostalCode() : "");
                                    obj.put("shippingPhone", cas.getPhone() != null ? cas.getPhone() : "");
                                    obj.put("shippingMobileNumber", cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                                    obj.put("shippingFax", cas.getFax() != null ? cas.getFax() : "");
                                    obj.put("shippingEmailID", cas.getEmailID() != null ? cas.getEmailID() : "");
                                    obj.put("shippingContactPerson", cas.getContactPerson() != null ? cas.getContactPerson() : "");
                                    obj.put("shippingContactPersonNumber", cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");
                                    obj.put("shippingContactPersonDesignation", cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : "");
                                    obj.put("shippingWebsite", cas.getWebsite() != null ? cas.getWebsite() : "");
                                    obj.put("shippingRoute", cas.getShippingRoute() != null ? cas.getShippingRoute() : "");
                                }
                            }
                        }

                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
        }
        return jArr;
    }
    public ModelAndView saveCustomerWarehouses(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveCustomerWarehouses(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                InventoryWarehouse warehouse = (InventoryWarehouse) result.getEntityList().get(0);
                jobj.put("id", warehouse.getId());
                String name = request.getParameter("name");
//                String groupName = request.getParameter("groupName");
                boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
                String action = "added";
                if (isEdit) {
                    action = "updated";
                    msg=messageSource.getMessage("acc.master.update", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
                }
                auditTrailObj.insertAuditLog(AuditAction.LOCATION_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " warehouse item " + name, request, "0");

            }
                txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public KwlReturnObject saveCustomerWarehouses(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        String customerIds = request.getParameter("customerid");
        KwlReturnObject result = null;
        String msg = "";
        String companyId = sessionHandlerImpl.getCompanyid(request);
        KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        Company company = (Company) jeresult.getEntityList().get(0);
        boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
        if(isEdit){
            deletecustomerWarehouses(request, companyId);
        }
        if (!StringUtil.isNullOrEmpty(customerIds)) {
            String[] cids = customerIds.split(",");
            for (int i = 0; i < cids.length; i++) {
                HashMap<String, Object> filterParams = new HashMap<String, Object>();
                String customerId = cids[i].toString();
                filterParams.put("customerid", customerId);
                filterParams.put("company", company);

                result = new KwlReturnObject(true, msg, null, null, 0);
                msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
                boolean isPresent = false;
                String itemID = request.getParameter("warehouse");
                boolean isForCustomer = request.getParameter("isForCustomer") != null ? Boolean.parseBoolean(request.getParameter("isForCustomer")) : false;
                HashMap requestParam = AccountingManager.getGlobalParams(request);
                requestParam.put("name", request.getParameter("name"));
                requestParam.put("customerid", customerId);
                requestParam.put("isForCustomer", isForCustomer);

                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                if (!StringUtil.isNullOrEmpty(itemID)) {
//                    String warehouseId = accCustomerDAOobj.getCustomerWarehousesMapById(itemID, company);
                    filter_names.add("id");
                    filter_params.add(itemID);
                    requestParam.put("id", itemID);
                }
//                filter_names.add("id");
//                filter_params.add(itemID);
                filter_names.add("company.companyID");
                filter_params.add(requestParam.get(Constants.companyKey));
                filter_names.add("name");
                filter_params.add(request.getParameter("name"));
//        filter_names.add("customer");
//        filter_params.add(request.getParameter("customerid"));
//        filter_names.add("isForCustomer");
//        filter_params.add(isForCustomer);
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                KwlReturnObject cntResult = accCustomerDAOobj.getCustomerWarehouses(filterRequestParams);
                int count = cntResult.getRecordTotalCount();
                String recordID = "";
                if (count == 1) {
                    recordID = ((InventoryWarehouse) cntResult.getEntityList().get(0)).getId();
                    isPresent = itemID.equals(recordID) ? false : true; //Allow Editing same record
                } else if (count > 1) {
                    isPresent = true;
                }
                if (isPresent) {
                    filterParams.put("warehouseid", recordID);
                    if(StringUtil.isNullOrEmpty(recordID)&&count>1){
                        return new KwlReturnObject(true, "Duplicate Warehouses Exist", null, result.getEntityList(), 0);
                    }

                } else {
                    result = accCustomerDAOobj.addCustomerWarehouses(requestParam);
                    InventoryWarehouse inventoryWarehouse = (InventoryWarehouse) result.getEntityList().get(0);
                    filterParams.put("warehouseid", inventoryWarehouse.getId());
                    recordID=inventoryWarehouse.getId();
//                    accCustomerDAOobj.addCustomerWarehouseMapping(inventoryWarehouse.getId(), customerId, isEdit, itemID);
                }

                KwlReturnObject isExist = accCustomerDAOobj.getCustomerWarehousesMap(filterParams, company);
                List list = isExist.getEntityList();
                if (list.size() == 0) {
                    result = accCustomerDAOobj.addCustomerWarehouseMapping(recordID, customerId, isEdit, null);
                    msg = messageSource.getMessage("acc.customer.warehouse.save.success", null, RequestContextUtils.getLocale(request));
                } else {
                    CustomerWarehouseMap customerWarehouseMap = (CustomerWarehouseMap) isExist.getEntityList().get(0);
                    result = accCustomerDAOobj.addCustomerWarehouseMapping(recordID, customerId, isEdit, customerWarehouseMap.getID());
                    msg = messageSource.getMessage("acc.customer.warehouse.already.exist", null, RequestContextUtils.getLocale(request));
//                        return new KwlReturnObject(true, msg, null, null, 0);
                }

            }
        }
        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
    }
        public ModelAndView getCustomerWarehouses(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getCustomerWarehouses(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getWarehouses : " + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getCustomerWarehouses(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            boolean isInvoice=false;
            boolean isForCustomer = request.getParameter("isForCustomer") != null ? Boolean.parseBoolean(request.getParameter("isForCustomer")) : false;
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("customerid"))) {
                filter_names.add("customer");
                filter_params.add(request.getParameter("customerid"));
            }
            filter_names.add("isForCustomer");
            filter_params.add(isForCustomer);
            order_by.add("name");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accCustomerDAOobj.getCustomerWarehouses(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                InventoryWarehouse warehouse = (InventoryWarehouse) itr.next();
                JSONObject obj = new JSONObject();
                Customer customer=null;
                obj.put("id", warehouse.getId());
                obj.put("name", warehouse.getName());
                obj.put("customer", warehouse.getCustomer());
                if (!StringUtil.isNullOrEmpty(warehouse.getCustomer())) {
                    KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), warehouse.getCustomer());
                    customer = (Customer) customerresult.getEntityList().get(0);
                } 
               
                if(customer!=null){
                obj.put("accnamecode", customer.getAcccode());
                obj.put("customerName", customer.getName());
                obj.put("customerid", customer.getID());
                }
               jArr.put(obj);
            }
            jobj.put(Constants.data, jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    public ModelAndView getAllCustomerWarehouse(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            boolean getAllCids=StringUtil.isNullOrEmpty(request.getParameter("getallcids")) ? false : Boolean.parseBoolean(request.getParameter("getallcids"));
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            filterRequestParams.put("company", company);
            if (!StringUtil.isNullOrEmpty(request.getParameter("customerid"))) {
                filterRequestParams.put("customerid", request.getParameter("customerid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("warehouseid"))) {
                filterRequestParams.put("warehouseid", request.getParameter("warehouseid"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("groupbywarehouse"))){
                filterRequestParams.put("groupbywarehouse", request.getParameter("groupbywarehouse"));
            }
            KwlReturnObject result = accCustomerDAOobj.getCustomerWarehousesMap(filterRequestParams, company);
            String cid="";
            List<CustomerWarehouseMap> list = result.getEntityList();
            JSONArray jArr = new JSONArray();
            if (list.size()>0) {
                for(CustomerWarehouseMap customerWareMap : list) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", customerWareMap.getID());
                    obj.put("name", customerWareMap.getInventoryWarehouse() != null ? customerWareMap.getInventoryWarehouse().getName() : "");
                    obj.put("warehouse", customerWareMap.getInventoryWarehouse() != null ? customerWareMap.getInventoryWarehouse().getId() : "");
                    obj.put("accnamecode", customerWareMap.getCustomer().getAcccode());
                    obj.put("customerName", customerWareMap.getCustomer().getName());
                    obj.put("accname", customerWareMap.getCustomer().getName());
                    obj.put("customerid", customerWareMap.getCustomer().getID());
                    obj.put("isdefault", customerWareMap.isIsdefault());
                    if (getAllCids && customerWareMap.getInventoryWarehouse() != null) {
                        getCustomerids(obj, company, customerWareMap.getInventoryWarehouse().getId(), list);  //For accessing all customers related to warehouse. 
                    }
                    jArr.put(obj);
                }
            }
            jobj.put(Constants.data, jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView deleteCustomerWarehouses(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int no = deleteCustomerWarehouses(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
            try {
                String name[] = request.getParameterValues("name");
                for (int i = 0; i < name.length; i++) {
                    auditTrailObj.insertAuditLog(AuditAction.WAREHOUSE_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted warehouse item " + name[i], request, "0");
                }
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
        } catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteCustomerWarehouses(HttpServletRequest request) throws ServiceException, AccountingException {
        String ids[] = request.getParameterValues("ids");
        int numRows = 0;
        try {
            for (int i = 0; i < ids.length; i++) {
                accCustomerDAOobj.deleteCustomerWarehouses(ids[i]);
                numRows++;
            }
        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return numRows;
    }

    private void getCustomerids(JSONObject obj, Company company, String warehouse,List<CustomerWarehouseMap> list) throws ServiceException, JSONException {
        String cid = "";
        if (list.size() > 0) {
            for (CustomerWarehouseMap customerWareMap : list) {
                if (customerWareMap.getInventoryWarehouse() != null) {
                    if (customerWareMap.getInventoryWarehouse().getId().equals(warehouse)) {
                        cid += customerWareMap.getCustomer().getID() + ",";
                    }
                }
            }
            if (cid.endsWith(",")) {
                cid = cid.substring(0, cid.length() - 1);
            }
            obj.put("customerids", cid);
        }
    }

    private void deletecustomerWarehouses(HttpServletRequest request,String companyId) throws ServiceException {
        String warehouseId=request.getParameter("warehouse");
        String customerIds=request.getParameter("customerids");
        if(!StringUtil.isNullOrEmpty(customerIds)){
            String[] cIds=customerIds.split(",");
            accCustomerDAOobj.deleteCustomerFromWarehouseMap(warehouseId, cIds, companyId);
        }
        
    }
    public ModelAndView setDefaultWarehouseForCustomers(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STL_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            setDefaultWarehouseForCustomers(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.warehouse.updated.success", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
            String customerid = request.getParameter("customerid");
            Customer customer = (Customer) accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid).getEntityList().get(0);
            auditTrailObj.insertAuditLog(AuditAction.CUSTOMER_DEFAULTWAREHOUSE, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated default warehouse for Customer <b>"  + customer.getName() + "</b> ( " + customer.getAcccode() + " ) ", request, customer.getID());

        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public void setDefaultWarehouseForCustomers(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        String companyId = sessionHandlerImpl.getCompanyid(request);
        String defaultcustWarehouseId = "", defaultcustWarehouseMapId = "", custWarehouseMapId = "", warehouseId = "";
        KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        Company company = (Company) jeresult.getEntityList().get(0);

        String customerid = request.getParameter("customerid");
        String warehouseid = request.getParameter("warehouseid");

        filterRequestParams.put("company", companyId);
        if (!StringUtil.isNullOrEmpty(customerid)) {
            filterRequestParams.put("customerid", customerid);
        }
        //checked Default warehouse present 
        KwlReturnObject result = accCustomerDAOobj.getCustomerDefaultWarehousesMap(filterRequestParams, company);
        List<CustomerWarehouseMap> list = result.getEntityList();
        if (list.size() > 0) {
            for (CustomerWarehouseMap defaultcustomerWareMap : list) {
                defaultcustWarehouseMapId = defaultcustomerWareMap.getID() != null ? defaultcustomerWareMap.getID() : "";
                defaultcustWarehouseId = defaultcustomerWareMap.getInventoryWarehouse() != null ? defaultcustomerWareMap.getInventoryWarehouse().getId() : "";

            }
        }
        //if Default warehouse is already setted then update the default warehouse
        if (!StringUtil.isNullOrEmpty(defaultcustWarehouseMapId) && !StringUtil.isNullOrEmpty(defaultcustWarehouseId)) {
            accCustomerDAOobj.updateCustomersDefaultWarehouse(defaultcustWarehouseMapId, defaultcustWarehouseId, customerid, false);
        }
        //set new warehouse as degault warehouse
        HashMap<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("company", companyId);
        if (!StringUtil.isNullOrEmpty(customerid)) {
            reqParams.put("customerid", customerid);
        }
        if (!StringUtil.isNullOrEmpty(warehouseid)) {
            reqParams.put("warehouseid", warehouseid);
        }

        KwlReturnObject wareResult = accCustomerDAOobj.getCustomerWarehousesMap(reqParams, company);
        List<CustomerWarehouseMap> warlist = wareResult.getEntityList();
        if (warlist.size() > 0) {
            for (CustomerWarehouseMap customerWareMap : warlist) {
                custWarehouseMapId = customerWareMap.getID() != null ? customerWareMap.getID() : "";
                warehouseId = customerWareMap.getInventoryWarehouse() != null ? customerWareMap.getInventoryWarehouse().getId() : "";

            }
        }
        if (!StringUtil.isNullOrEmpty(custWarehouseMapId) && !StringUtil.isNullOrEmpty(warehouseId)) {
            accCustomerDAOobj.updateCustomersDefaultWarehouse(custWarehouseMapId, warehouseId, customerid, true);
        }
    }
    
    public ModelAndView getCustomerRegistryDetails(HttpServletRequest request, HttpServletResponse response) {
            JSONObject jobj = new JSONObject();
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            jobj = getCustomerRegistryDetailsJson(request, false);
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView exportCustomerRegistry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            boolean export = true;
            jobj = getCustomerRegistryDetailsJson(request, export);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public JSONObject getCustomerRegistryDetailsJson(HttpServletRequest request, boolean export) throws AccountingException, IOException, SessionExpiredException, JSONException {
        JSONObject jobj1 = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jMeta = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        JSONObject commData = new JSONObject();
        Locale locale = RequestContextUtils.getLocale(request);
        try {
            String modules=request.getParameter(Constants.moduleid);
            String moduleids[]=modules.split(",");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            int reportId = 0;
            KwlReturnObject result = null;
            List list = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
                reportId = Integer.parseInt(request.getParameter("reportId"));
            }
            HashMap<String, Object> requestParams = getCustomerRegistryMap(request);
            /*
             *Get Recordset
             */
            jarrRecords=getRecordsForStore(modules,jarrRecords,companyId,reportId);
            /*
             *Get ColumnModel
             */
            requestParams.put("locale", locale);
            jarrColumns=getColumnsForGrid(requestParams,modules,jarrColumns);
            /*
             * Get Data of Invoices/Cash sales 
             */
            String modulids=Constants.Acc_Invoice_ModuleId+","+Constants.Acc_Sales_Order_ModuleId+","+Constants.Acc_Sales_Return_ModuleId+","+Constants.Acc_Customer_Quotation_ModuleId+","+Constants.Acc_Delivery_Order_ModuleId+","+Constants.Acc_Credit_Note_ModuleId+","+Constants.Acc_Debit_Note_ModuleId+","+Constants.Acc_Receive_Payment_ModuleId+","+Constants.Acc_Make_Payment_ModuleId;
            
            if(StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Invoice_ModuleId))){
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Invoice_ModuleId, modulids);
                    JSONObject search=new JSONObject(Searchjson);
                    JSONArray array=search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                    if (array.length() > 0) {
                        result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = getSalesInvoicesforRegistryJson(request, list, DataJArr);
                        requestParams.put("isOpeningBalanceInvoices", "true");
                        requestParams.put("includeAllRec", "false");
                        result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = getSalesInvoicesforRegistryJson(request, list, DataJArr);
                    }
                }else{
                    result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                    list = result.getEntityList();
                    DataJArr = getSalesInvoicesforRegistryJson(request, list, DataJArr);
                }
            }
            /*
             * Get Data of Sales Orders
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Sales_Order_ModuleId, modulids);
                    JSONObject search=new JSONObject(Searchjson);
                    JSONArray array=search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                    requestParams.put("isOpeningBalance", false);
                    if(array.length()>0){
                        result = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = getSalesOrdersforRegistryJson(request, list, DataJArr);
                    }
                }else{
                    result = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
                    list = result.getEntityList();
                    DataJArr = getSalesOrdersforRegistryJson(request, list, DataJArr);
                }
            }
            /*
             * Get Data of Sales Returns
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Sales_Return_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", search.toString());
                    requestParams.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
                    if (array.length() > 0) {
                        result = accInvoiceDAOobj.getSalesReturn(requestParams);
                        list = result.getEntityList();
                        DataJArr = getSalesReturnforRegistryJson(request, list, DataJArr);
                    }
                } else {
                    result = accInvoiceDAOobj.getSalesReturn(requestParams);
                    list = result.getEntityList();
                    DataJArr = getSalesReturnforRegistryJson(request, list, DataJArr);
                }
            }
            /*
             * Get Data of Customer Quotations
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Customer_Quotation_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", search.toString());
                    requestParams.put(Constants.moduleid, Constants.Acc_Customer_Quotation_ModuleId);
                    if (array.length() > 0) {
                        result = accSalesOrderDAOobj.getQuotations(requestParams);
                        list = result.getEntityList();
                        DataJArr = getQuotationsforRegistryJson(request, list, DataJArr);
                    }
                } else {
                    result = accSalesOrderDAOobj.getQuotations(requestParams);
                    list = result.getEntityList();
                    DataJArr = getQuotationsforRegistryJson(request, list, DataJArr);
                }
            }
            /*
             * Get Data of Delivery Orders
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Delivery_Order_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", search.toString());
                    requestParams.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                    if (array.length() > 0) {
                        result = accInvoiceDAOobj.getDeliveryOrdersMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = getDeliveryOrdersforRegistryJson(request, list, DataJArr);
                    }
                } else {
                    result = accInvoiceDAOobj.getDeliveryOrdersMerged(requestParams);
                    list = result.getEntityList();
                    DataJArr = getDeliveryOrdersforRegistryJson(request, list, DataJArr);
                }
            }
            /*
             * Get Data of Credit Notes
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Credit_Note_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", search.toString());
                    requestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    if (array.length() > 0) {
                        result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = getCreditNoteforRegistryJson(request, list, DataJArr);
                        result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
                        list = result.getEntityList();
                        DataJArr = getOpeningCreditNoteforRegistryJson(request, list, DataJArr);
                    }
                } else {
                    result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                    list = result.getEntityList();
                    DataJArr = getCreditNoteforRegistryJson(request, list, DataJArr);
                    result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
                    list = result.getEntityList();
                    DataJArr = getOpeningCreditNoteforRegistryJson(request, list, DataJArr);
                }
            }
            /*
             * Get Data of Debit Notes
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Debit_Note_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", search.toString());
                    requestParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    requestParams.put("cntype", 4);
                    requestParams.put("isOpeningBalance", false);
                    if (array.length() > 0) {
                        result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = getDebitNoteforRegistryJson(request, list, DataJArr);
                        result = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
                        list = result.getEntityList();
                        DataJArr = getOpeningDebitNoteforRegistryJson(request, list, DataJArr);
                    }
                } else {
                    requestParams.put("cntype", 4);
                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                    list = result.getEntityList();
                    DataJArr = getDebitNoteforRegistryJson(request, list, DataJArr);
                    result = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
                    list = result.getEntityList();
                    DataJArr = getOpeningDebitNoteforRegistryJson(request, list, DataJArr);
                }
            }
            /*
             * Get Data of Payments Received
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Receive_Payment_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", search.toString());
                    requestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                    requestParams.put("paymentWindowType", 1);
                    requestParams.put("isOpeningBalance", false);
                    if (array.length() > 0) {
                        result = accReceiptDAOobj.getReceipts(requestParams);
                        list = result.getEntityList();
                        DataJArr = getReceivedPaymentsforRegistryJson(request, list, DataJArr);
                        result=accReceiptDAOobj.getAllOpeningBalanceReceipts(requestParams);
                        list = result.getEntityList();
                        DataJArr = getOpeningReceivedPaymentsJson(request, list, DataJArr);
                    }
                } else {
                    requestParams.put("paymentWindowType", 1);
                    result = accReceiptDAOobj.getReceipts(requestParams);
                    list = result.getEntityList();
                    DataJArr = getReceivedPaymentsforRegistryJson(request, list, DataJArr);
                    result =accReceiptDAOobj.getAllOpeningBalanceReceipts(requestParams);
                    list = result.getEntityList();
                    DataJArr = getOpeningReceivedPaymentsJson(request, list, DataJArr);
                }
            }
            /*
             * Get Data of Payments Made
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Make_Payment_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", search.toString());
                    requestParams.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                    requestParams.put("paymentWindowType", 2);
                    requestParams.put("isOpeningBalance", false);
                    if (array.length() > 0) {
                        result = accVendorPaymentobj.getPayments(requestParams);
                        list = result.getEntityList();
                        DataJArr = getMadePaymentsforRegistryJson(request, list, DataJArr);
                    }
                } else {
                    requestParams.put("paymentWindowType", 2);
                    result = accVendorPaymentobj.getPayments(requestParams);
                    list = result.getEntityList();
                    DataJArr = getMadePaymentsforRegistryJson(request, list, DataJArr);
                }
            }
            JSONArray pagedJson = DataJArr;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
                pagedJson = StringUtil.getPagedJSON(DataJArr, Integer.parseInt(request.getParameter(Constants.start)), Integer.parseInt(request.getParameter(Constants.limit)));
            }

            commData.put(Constants.RES_success, true);
            commData.put("coldata", pagedJson);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", DataJArr.length());
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj1.put("valid", true);
            if (export) {
                jobj1.put(Constants.data, DataJArr);
            } else {
                jobj1.put(Constants.data, commData);
            }

        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj1;
    }
    
    public HashMap<String, Object> getCustomerRegistryMap(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        String companyId = sessionHandlerImpl.getCompanyid(request);
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String newcustomerid = request.getParameter("person");
        String startDate = (String) request.getParameter("startDate");
        String endDate = (String) request.getParameter("endDate");
        int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
        if (!StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            requestParams.put("ss", request.getParameter("ss"));
        }

        if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
            requestParams.put("searchJson", request.getParameter("searchJson"));
            requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
            requestParams.put("reportId", request.getParameter("reportId"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("status"))) {
            requestParams.put("status",request.getParameter("status"));
        }
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        requestParams.put("cntype", cntype);
        requestParams.put("CashAndInvoice", true);
        requestParams.put("includeAllRec", true);
        requestParams.put("currencyname", currency.getName());
        requestParams.put(Constants.isExport, !StringUtil.isNullOrEmpty(request.getParameter(Constants.isExport))?true:false);
        requestParams.put("browsertimezone", sessionHandlerImpl.getBrowserTZ(request));
        requestParams.put(Constants.df, df);
        requestParams.put(Constants.companyid, companyId);
        requestParams.put(Constants.newcustomerid, newcustomerid);
        requestParams.put(Constants.REQ_startdate, startDate);
        requestParams.put(Constants.REQ_enddate, endDate);
        requestParams.put(Constants.REQ_startdate, startDate);
        requestParams.put(Constants.REQ_enddate, endDate);
        return requestParams;
    }
    
    public JSONArray getRecordsForStore(String modules, JSONArray jarrRecords,String companyId,int reportId) throws SessionExpiredException, ServiceException {
        try {
            String moduleids[]=modules.split(",");
            JSONObject jobjTemp = new JSONObject();
            jobjTemp.put("name", "transactionDate");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "documentno");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "customerName");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amount");
            jarrRecords.put(jobjTemp);
            
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Invoice_ModuleId))) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", "amountdue");
                jarrRecords.put(jobjTemp);
            }
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "status");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", Constants.moduleid);
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "cntype");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "isOpeningBalanceTransaction");
            jarrRecords.put(jobjTemp);
            
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", companyId);
            hashMap.put("reportId", reportId);
            if (!StringUtil.isNullOrEmpty(modules)){
                hashMap.put("moduleId", modules);
            }
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List arrayList = new ArrayList();
            for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                String column = "Custom_" + customizeReportMapping.getDataIndex();
                if (!arrayList.contains(customizeReportMapping.getDataIndex())) {
                    jobjTemp = new JSONObject();
                    jobjTemp.put("name", column);
                    jarrRecords.put(jobjTemp);
                    arrayList.add(customizeReportMapping.getDataIndex());
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getRecordsForStore : " + ex.getMessage(), ex);
        }
        return jarrRecords;
    }
    
    public JSONArray getColumnsForGrid(HashMap<String, Object> requestParams,String modules, JSONArray jarrColumns) throws SessionExpiredException, ServiceException, UnsupportedEncodingException {
        Locale locale = null;
        if (requestParams.containsKey("locale")) {
             locale = (Locale) requestParams.get("locale");
        }
        try {
            String moduleids[]=modules.split(",");
            String modulids=Constants.Acc_Invoice_ModuleId+","+Constants.Acc_Sales_Order_ModuleId+","+Constants.Acc_Sales_Return_ModuleId+","+Constants.Acc_Customer_Quotation_ModuleId+","+Constants.Acc_Delivery_Order_ModuleId+","+Constants.Acc_Credit_Note_ModuleId+","+Constants.Acc_Debit_Note_ModuleId+","+Constants.Acc_Receive_Payment_ModuleId+","+Constants.Acc_Make_Payment_ModuleId;
            JSONObject jobjTemp = new JSONObject();
            jobjTemp.put("header",messageSource.getMessage("acc.pdf.6", null, locale));
            jobjTemp.put("dataIndex", "transactionDate");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.DocumentNo", null, locale));
            jobjTemp.put("dataIndex", "documentno");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractDetails.CustomerName", null, locale));
            jobjTemp.put("dataIndex", "customerName");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.invoiceList.totAmtHome", null, locale)+" ("+requestParams.get("currencyname").toString()+")");
            jobjTemp.put("dataIndex", "amount");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Invoice_ModuleId))) {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.agedPay.gridAmtDueHomeCurrency", null, locale)+" ("+requestParams.get("currencyname").toString()+")");
                jobjTemp.put("dataIndex", "amountdue");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            }
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.invoiceList.status", null, locale));
            jobjTemp.put("dataIndex", "status");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", Integer.parseInt(requestParams.get("reportId").toString()));
            if (!StringUtil.isNullOrEmpty(modules)){
                hashMap.put("moduleId", modules);
            }
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List arrayList = new ArrayList();
            for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                int fieldType = 0;
                String header = customizeReportMapping.getDataHeader();
                HashMap<String, Object> requestParam = new HashMap<String, Object>();
                requestParam.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel, Constants.moduleid));
                requestParam.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), StringUtil.DecodeText(header), modulids));
                KwlReturnObject fieldParamsResult = accJournalEntryobj.getFieldParameters(requestParam);
                FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                if (params.getFieldtype() == 3) {
                    fieldType = 3;
                }
                String column = "Custom_" + customizeReportMapping.getDataIndex();
                if (!arrayList.contains(customizeReportMapping.getDataIndex())) {
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", customizeReportMapping.getDataHeader());
                    jobjTemp.put("dataIndex", column);
                    jobjTemp.put("width", 150);
                    jobjTemp.put("pdfwidth", 150);
                    if (fieldType == 3) {
                        jobjTemp.put("fieldType", 3);
                    }
                    jobjTemp.put("custom", "true");
                    jarrColumns.put(jobjTemp);
                    arrayList.add(customizeReportMapping.getDataIndex());
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getColumnsForGrid : " + ex.getMessage(), ex);
        }
        return jarrColumns;
    }
    
    public JSONArray getSalesInvoicesforRegistryJson(HttpServletRequest request, List<Object []> invoices, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        List<String> idsList = new ArrayList<String>();
        HashMap requestParams = getCustomerRegistryMap(request);
        try {
            for (Object[] oj : invoices) {
                idsList.add(oj[0].toString());
            }
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Map<String, JournalEntryDetail> invoiceCustomerEntryMap = accInvoiceDAOobj.getInvoiceCustomerEntryList(idsList);
            Map<String, InvoiceInfo> invoiceObjectMap = accInvoiceDAOobj.getInvoiceList(idsList);
            Map<String, JournalEntry> invoiceJEMap = accInvoiceDAOobj.getInvoiceJEList(idsList);
            Map<String, List<CreditNoteInfo>> creditInvoiceMap = accCreditNoteDAOobj.getCNRowsInfoFromInvoice(idsList);
            Map<String, List<InvoiceTermsMap>> invoiceTermsmap = accInvoiceDAOobj.getInvoiceTermMapList(idsList);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            for(Object oj[]:invoices){
                String invid = oj[0].toString();
                InvoiceInfo invoiceinfo = invoiceObjectMap.get(invid);
                KwlReturnObject inv = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                Invoice invoice = (Invoice) inv.getEntityList().get(0);
                boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                String currencyid = (invoiceinfo.getCurrency() == null ? currency.getCurrencyID() : invoiceinfo.getCurrency().getCurrencyID());
                JSONObject newJobj = new JSONObject();
//                if(invoice.getJournalEntry()!=null){
//                    newJobj.put("transactionDate", df.format(invoice.getJournalEntry().getEntryDate()));
//                }else{
//                    newJobj.put("transactionDate", df.format(invoice.getCreationDate()));
//                }
                newJobj.put("transactionDate", df.format(invoice.getCreationDate()));
                newJobj.put("documentno", invoice.getInvoiceNumber());
                newJobj.put("customerName", invoice.getCustomer().getName());
                Date invoiceCreationDate = invoice.getCreationDate();
                Double externalCurrencyRate = 0d;
                Double invoiceOriginalAmount = 0d;
                if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                    ExchangeRateDetails erd = invoice.getExchangeRateDetail();
                    externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                    invoiceOriginalAmount = invoice.getOriginalOpeningBalanceAmount();
                }
                KwlReturnObject bAmt = null;
                
                JournalEntry je = null;
                if (invoice.isNormalInvoice() && invoiceJEMap.containsKey(invid)) {
                    je = invoiceJEMap.get(invid);
//                    invoiceCreationDate = je.getEntryDate();
                    invoiceCreationDate = invoice.getCreationDate();
                    externalCurrencyRate = je.getExternalCurrencyRate();
                }
                JournalEntryDetail d = null;
                if (invoice.isNormalInvoice() && invoiceCustomerEntryMap.containsKey(invid)) {
                    d = invoiceCustomerEntryMap.get(invid);
                    invoiceOriginalAmount = d.getAmount();
                }
                double currencyToBaseRate = accCurrencyDAOobj.getCurrencyToBaseRate(requestParams, currencyid, invoiceCreationDate);
                double amountinbase = invoiceOriginalAmount;
                if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, invoiceOriginalAmount, currencyid, invoiceCreationDate, externalCurrencyRate);
                    amountinbase = (Double) bAmt.getEntityList().get(0);
                } else if (invoiceOriginalAmount != 0) {
                    if (externalCurrencyRate != 0) {
                        amountinbase = invoiceOriginalAmount / externalCurrencyRate;
                    } else if (currencyToBaseRate != 0) {
                        amountinbase = invoiceOriginalAmount / currencyToBaseRate;
                    }
                }
                newJobj.put("amount", authHandler.round(amountinbase, companyid));
                List ll = null;
                if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                    ll = new ArrayList();
                    ll.add(invoice.getOpeningBalanceAmountDue());
                    ll.add(0.0);
                    ll.add(0.0);
                } else {
                    if (Constants.InvoiceAmountDueFlag) {
                        ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, invoice);
                    } else {
                        ll = accInvoiceCommon.getAmountDue_Discount(requestParams, currency, invoice,
                                currencyid, je, creditInvoiceMap, invoiceTermsmap.get(invoiceinfo.getInvoiceID()));
                    }
                }
                double amountdue = 0;
                amountdue = (Double) ll.get(0);
                double amountdueinbase = amountdue;

                if (invoice.isCashtransaction()) {
                    newJobj.put("amountdue", 0);
                } else {
                    if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, currencyid, invoiceCreationDate, externalCurrencyRate);
                        amountdueinbase = (Double) bAmt.getEntityList().get(0);
                    } else if (amountdue != 0) {
                        if (externalCurrencyRate != 0) {
                            amountdueinbase = amountdue / externalCurrencyRate;
                        } else if (currencyToBaseRate != 0) {
                            amountdueinbase = amountdue / currencyToBaseRate;
                        }
                    }
                    newJobj.put("amountdue", authHandler.round(amountdueinbase, companyid));
                }
                newJobj.put("status", amountdueinbase!=0?messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)));
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                if(!invoice.isIsOpeningBalenceInvoice()){
                    newJobj.put("isOpeningBalanceTransaction", false);
                }else{
                    newJobj.put("isOpeningBalanceTransaction", true);
                }
                newJobj.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                String customid="";
                if(invoice.getJournalEntry()!=null){
                    customid = invoice.getJournalEntry().getID();
                }else{
                    customid = invoice.getOpeningBalanceInvoiceCustomData().getOpeningBalanceInvoiceId();
                }
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Invoice_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (!customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Invoice_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    if (invoice.getJournalEntry() != null) {
                        custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), customid);
                    } else {
                        custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceInvoiceCustomData.class.getName(), customid);
                    }
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccJECustomData jeDetailCustom = null;
                        OpeningBalanceInvoiceCustomData OBICustomData = null;
                        if (invoice.getJournalEntry() != null) {
                            jeDetailCustom = (AccJECustomData) custumObjresult.getEntityList().get(0);
                        } else {
                            OBICustomData = (OpeningBalanceInvoiceCustomData) custumObjresult.getEntityList().get(0);
                        }
                        if (jeDetailCustom != null || OBICustomData != null) {
                            if (invoice.getJournalEntry() != null) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            } else {
                                AccountingManager.setCustomColumnValues(OBICustomData, FieldMap, replaceFieldMap, variableMap);
                            }
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            params.put("isReturnDropdownCheckListVal", true);
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getSalesInvoicesforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getSalesOrdersforRegistryJson(HttpServletRequest request, List<Object []> orders, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat();
        HashMap requestParams = getCustomerRegistryMap(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        boolean isConsignment= Boolean.FALSE.parseBoolean(request.getParameter("isConsignment"));
        boolean includepending= Boolean.FALSE.parseBoolean(request.getParameter("includepending"));
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
        try {
            for(Object oj[]:orders){
                String orderid = oj[0].toString();
                KwlReturnObject order = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), orderid);
                SalesOrder salesorder = (SalesOrder) order.getEntityList().get(0);
                Iterator itrRow = salesorder.getRows().iterator();
                double amount = 0, totalDiscount = 0, discountPrice = 0;
                double rowTaxAmt = 0d, rowDiscountAmt = 0d;
                int rejectedCount = 0;
                boolean gstIncluded = salesorder.isGstIncluded();
                while (itrRow.hasNext()) {
                    SalesOrderDetail sod = (SalesOrderDetail) itrRow.next();
                    double rowTaxPercent = 0;
                    if (sod.getTax() != null) {
                        requestParams.put("transactiondate", salesorder.getOrderDate());
                        requestParams.put("taxid", sod.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    }
                    if (isConsignment && !includepending) {
                        if (sod.getRejectedQuantity() == sod.getBaseuomquantity()) {
                            rejectedCount++;
                        }
                    }
                    double sorate = authHandler.round(sod.getRate(), companyid);
                    if (gstIncluded) {
                        sorate = sod.getRateincludegst();
                    }
                    double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);

                    double quotationPrice = authHandler.round(quantity * sorate, companyid);
                    double discountSOD = authHandler.round(sod.getDiscount(), companyid);

                    if (sod.getDiscountispercent() == 1) {
                        discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountSOD / 100), companyid);
                        rowDiscountAmt += authHandler.round((quotationPrice * discountSOD / 100), companyid);
                    } else {
                        discountPrice = quotationPrice - discountSOD;
                        rowDiscountAmt += discountSOD;
                    }
                    rowTaxAmt += sod.getRowTaxAmount();
                    amount += discountPrice;//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                    if (!gstIncluded) {
                        amount += authHandler.round(sod.getRowTaxAmount(), companyid);
                    }
                }                    
                
                double discountSO=authHandler.round(salesorder.getDiscount(), companyid);
                if (discountSO != 0) {
                    if (salesorder.isPerDiscount()) {
                        totalDiscount = authHandler.round(amount * discountSO / 100, companyid);
                        amount = amount - totalDiscount;
                    } else {
                        amount = amount - discountSO;
                        totalDiscount = discountSO;
                    }
                }
                
                double totalTermAmount=0;
                double taxableTermamount = 0;
                HashMap<String, Object> requestParam = new HashMap();
                HashMap<String, Object> filterrequestParams = new HashMap();
                requestParam.put("salesOrder", salesorder.getID());
                KwlReturnObject salesOrderResult =null;
                filterrequestParams.put("taxid", salesorder.getTax()==null?"":salesorder.getTax().getID());
                salesOrderResult=accSalesOrderDAOobj.getSalesOrderTermMap(requestParam);
                List<SalesOrderTermMap> termMap = salesOrderResult.getEntityList();
                for (SalesOrderTermMap salesOrderTermMap : termMap) {
                    filterrequestParams.put("term", salesOrderTermMap.getTerm() == null ? "" : salesOrderTermMap.getTerm().getId());
                    InvoiceTermsSales mt = salesOrderTermMap.getTerm();
                    double termAmnt = salesOrderTermMap.getTermamount();
                    totalTermAmount += authHandler.round(termAmnt, companyid);
                    boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                    if (isTermMappedwithTax) {
                        taxableTermamount += termAmnt;
                    }
                }
                totalTermAmount=authHandler.round(totalTermAmount, companyid);
                double taxPercent = 0;
                if (salesorder.getTax() != null) {
                    requestParams.put("transactiondate", salesorder.getOrderDate());
                    requestParams.put("taxid", salesorder.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                }
                double orderAmount=amount;
                double ordertaxamount=(taxPercent==0?0:authHandler.round(((orderAmount + taxableTermamount)*taxPercent/100), companyid));
                
                amount=amount+totalTermAmount+ordertaxamount;
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, salesorder.getCurrency().getCurrencyID(), salesorder.getOrderDate(), salesorder.getExternalCurrencyRate());
                double totalAmountinBase= (Double)bAmt.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(salesorder.getOrderDate()));
                newJobj.put("documentno", salesorder.getSalesOrderNumber());
                newJobj.put("customerName", salesorder.getCustomer().getName());
                newJobj.put("amount", authHandler.round(totalAmountinBase, companyid));
                String status=messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request));
                if(isConsignment && !includepending && salesorder.isFreeze()){
                    newJobj.put("status",status);
                }else{
                    status = accSalesOrderServiceDAOobj.getSOStatus(salesorder,pref,extraCompanyPreferences);
                    if (isConsignment && !includepending && salesorder.getRows().size()==rejectedCount) {
                        newJobj.put("status",messageSource.getMessage("acc.field.Rejected", null, RequestContextUtils.getLocale(request)));
                    } else {
                        newJobj.put("status",status);
                    }
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", false);
                newJobj.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Sales_Order_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (salesorder.getSoCustomData() != null && !customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Sales_Order_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(SalesOrderCustomData.class.getName(), salesorder.getSoCustomData().getSoID());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        SalesOrderCustomData soCustomData = (SalesOrderCustomData) custumObjresult.getEntityList().get(0);
                        if (soCustomData != null) {
                            AccountingManager.setCustomColumnValues(soCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            params.put("isReturnDropdownCheckListVal", true);
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getSalesOrdersforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getSalesReturnforRegistryJson(HttpServletRequest request, List<Object []> returns, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        HashMap requestParam = getCustomerRegistryMap(request);
        try {
            double amount = 0,amountwithouttax=0,quantity = 0;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for(Object oj[]:returns){
                String returnid = oj[0].toString();
                KwlReturnObject sreturn = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), returnid);
                SalesReturn salesreturn = (SalesReturn) sreturn.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(salesreturn.getOrderDate()));
                newJobj.put("documentno", salesreturn.getSalesReturnNumber());
                newJobj.put("customerName", salesreturn.getCustomer().getName());
                amount = 0;
                amountwithouttax = 0;
                double ordertaxamount = 0;
                boolean includeprotax = false;
                Set<SalesReturnDetail> srRows = salesreturn.getRows();
                if (srRows != null && !srRows.isEmpty()) {
                    for (SalesReturnDetail temp : srRows) {
                        quantity = temp.getInventory().getQuantity();
                        double rowAmt = temp.getRate() * quantity;

                        double disc = 0;

                        if (temp.getDiscountispercent() == 1) {
                            disc = rowAmt * temp.getDiscount() / 100;
                        } else {
                            disc = temp.getDiscount();
                        }

                        rowAmt = rowAmt - disc;
                        amountwithouttax += rowAmt; // Amount without tax
                        // getting tax also

                        double taxAmt = temp.getRowTaxAmount();
                        ordertaxamount += taxAmt; // line level tax
                        rowAmt += taxAmt;

                        amount += rowAmt;
                        if (temp.getTax() != null) {
                            includeprotax = true;
                        }
                    }
                }
                if (salesreturn.getTax() != null) {
                    double taxPercent = 0;
                    KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), salesreturn.getOrderDate(), salesreturn.getTax().getID());
                    taxPercent = (Double) taxresult.getEntityList().get(0);
                    ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round((amount * taxPercent / 100), companyid));
                    amount += ordertaxamount;
                }
                if (salesreturn.getCurrency() != null) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, salesreturn.getCurrency().getCurrencyID(), salesreturn.getOrderDate(), salesreturn.getExternalCurrencyRate());
                    newJobj.put("amount", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                }
                if(salesreturn.isIsNoteAlso()){
                    newJobj.put("status", messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)));
                }else{
                    newJobj.put("status", messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request)));
                }
                if(!requestParam.get("status").toString().equals("all") && !requestParam.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", false);
                newJobj.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParam.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParam.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Sales_Return_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (salesreturn.getSalesReturnCustomData()!=null && !customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Sales_Return_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    String customFieldMapValues = "";
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(SalesReturnCustomData.class.getName(), salesreturn.getSalesReturnCustomData().getSalesReturnId());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        SalesReturnCustomData srCustomData = (SalesReturnCustomData) custumObjresult.getEntityList().get(0);
                        if (srCustomData != null) {
                            AccountingManager.setCustomColumnValues(srCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParam.get(Constants.isExport).toString()));
                            params.put("isReturnDropdownCheckListVal", true);
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getSalesReturnforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getQuotationsforRegistryJson(HttpServletRequest request, List<Object> cquotations, JSONArray DataJArr) throws SessionExpiredException, ServiceException, ParseException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> requestParams = getCustomerRegistryMap(request);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for(Object obj:cquotations){
                String cquotationid = obj.toString();
                KwlReturnObject quotations = accountingHandlerDAOobj.getObject(Quotation.class.getName(), cquotationid);
                Quotation quotation = (Quotation) quotations.getEntityList().get(0);
                KWLCurrency currency = null;
                Customer customer = quotation.getCustomer();
                if (quotation.getCurrency() != null) {
                    currency = quotation.getCurrency();
                } else {
                    currency = customer.getAccount().getCurrency();
                }
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(quotation.getQuotationDate()));
                newJobj.put("documentno", quotation.getquotationNumber());
                newJobj.put("customerName", quotation.getCustomer().getName());
                Set<QuotationDetail> rows=quotation.getRows();
                boolean incProTax = false;
                Iterator itrRow = rows.iterator();
                double amount = 0, amountinbase = 0, totalDiscount = 0, discountPrice = 0;
                double rowTaxAmt = 0d, rowDiscountAmt = 0d;
                while (itrRow.hasNext()) {
                    QuotationDetail sod = (QuotationDetail) itrRow.next();
                    if (sod.getTax() != null) {
                        incProTax = true;
                    }
                    double qrate = authHandler.roundUnitPrice(sod.getRate(), companyid);
                    double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);
                    boolean gstIncluded = quotation.isGstIncluded();
                    if (gstIncluded) {
                        qrate = sod.getRateincludegst();
                    }
                    double quotationPrice = authHandler.round(quantity * qrate, companyid);
                    double discountQD = authHandler.round(sod.getDiscount(), companyid);

                    if (sod.getDiscountispercent() == 1) {
                        discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountQD / 100), companyid);
                        rowDiscountAmt += authHandler.round((quotationPrice * discountQD / 100), companyid);
                    } else {
                        discountPrice = quotationPrice - discountQD;
                        rowDiscountAmt += discountQD;
                    }

                    rowTaxAmt += sod.getRowTaxAmount();
                    amount += discountPrice;
                    if (!gstIncluded) {
                        amount += authHandler.round(sod.getRowTaxAmount(), companyid);
                    }
                }
                double discountQ = authHandler.round(quotation.getDiscount(), companyid);
                if (discountQ != 0) {
                    if (quotation.isPerDiscount()) {
                        totalDiscount = amount * discountQ / 100;
                        amount = amount - totalDiscount;
                    } else {
                        amount = amount - discountQ;
                        totalDiscount = discountQ;
                    }
                }
                
                double taxPercent = 0;
                double totalTermAmount = 0;
                double taxableamount = 0;
                HashMap<String, Object> requestParam = new HashMap();
                HashMap<String, Object> filterrequestParams = new HashMap();
                requestParam.put("quotation", quotation.getID());
                KwlReturnObject quotationResult = null;
                filterrequestParams.put("taxid", quotation.getTax() == null ? "" : quotation.getTax().getID());
                quotationResult = accSalesOrderDAOobj.getQuotationTermMap(requestParam);
                List<QuotationTermMap> termMap = quotationResult.getEntityList();
                for (QuotationTermMap quotationTermMap : termMap) {
                    filterrequestParams.put("term", quotationTermMap.getTerm() == null ? "" : quotationTermMap.getTerm().getId());
                    InvoiceTermsSales mt = quotationTermMap.getTerm();
                    double termAmnt = quotationTermMap.getTermamount();
                    totalTermAmount += authHandler.round(termAmnt, companyid);
                    boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);

                    if (isTermMappedwithTax) {
                        taxableamount += termAmnt;
                    }
                }
                totalTermAmount = authHandler.round(totalTermAmount, companyid);
                KwlReturnObject termbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalTermAmount, currency.getCurrencyID(), quotation.getQuotationDate(), quotation.getExternalCurrencyRate());
                double termamountinBase = authHandler.round((Double) termbAmtTax.getEntityList().get(0), companyid);
                if (quotation.getTax() != null) {
                    requestParams.put("transactiondate", quotation.getQuotationDate());
                    requestParams.put("taxid", quotation.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                }
                double orderAmount = amount;//(Double) bAmt.getEntityList().get(0);
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, currency.getCurrencyID(), quotation.getQuotationDate(), quotation.getExternalCurrencyRate());
                amountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                KwlReturnObject bAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, authHandler.round(((orderAmount + taxableamount) * taxPercent / 100), companyid), currency.getCurrencyID(), quotation.getQuotationDate(), quotation.getExternalCurrencyRate());
                double ordertaxamountBase = authHandler.round((Double) bAmtTax.getEntityList().get(0), companyid);
                amountinbase += termamountinBase;
                newJobj.put("amount", amountinbase + ordertaxamountBase);
                Date currentdate=authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(new Date()));
                Date validtilldate=authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(quotation.getValiddate()));
                if(quotation.getLinkflag()==2 || quotation.getLinkflag()==1){
                    newJobj.put("status", messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)));
                }else if(quotation.getLinkflag()==0 && validtilldate.before(currentdate)){
                    newJobj.put("status", "Expired");
                }else{
                    newJobj.put("status", messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request)));
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", false);
                newJobj.put(Constants.moduleid, Constants.Acc_Customer_Quotation_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Customer_Quotation_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (quotation.getQuotationCustomData()!=null && !customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Customer_Quotation_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    String customFieldMapValues = "";
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(QuotationCustomData.class.getName(), quotation.getQuotationCustomData().getQuotationId());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        QuotationCustomData cqCustomData = (QuotationCustomData) custumObjresult.getEntityList().get(0);
                        if (cqCustomData != null) {
                            AccountingManager.setCustomColumnValues(cqCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            params.put("isReturnDropdownCheckListVal", true);
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getQuotationsforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getDeliveryOrdersforRegistryJson(HttpServletRequest request, List<Object []> dorders, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> requestParams = getCustomerRegistryMap(request);
        try {
            double amount = 0,quantity = 0;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for(Object obj[]:dorders){
                String doid = obj[0].toString();
                KwlReturnObject delvorders = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doid);
                DeliveryOrder deliveryorder = (DeliveryOrder) delvorders.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(deliveryorder.getOrderDate()));
                newJobj.put("documentno", deliveryorder.getDeliveryOrderNumber());
                newJobj.put("customerName", deliveryorder.getCustomer().getName());
                Set<DeliveryOrderDetail> doRows = deliveryorder.getRows();
                boolean includeprotax = false;
                amount = 0;
                double discountPrice = 0;
                double totalDiscount=0;
                Set<String> invoiceno = new HashSet<String>();
                DeliveryOrderDetail tempdodobj = null;
                if (doRows != null && !doRows.isEmpty()) {
                    for (DeliveryOrderDetail temp : doRows) {
                        quantity = temp.getInventory().getQuantity();
                        double dorate = authHandler.roundUnitPrice(temp.getRate(), companyid);
                        double doPrice = authHandler.round(quantity * dorate, companyid);
                        double discountDOD = authHandler.round(temp.getDiscount(), companyid);
                        if (temp.getDiscountispercent() == 1) {
                            totalDiscount+=authHandler.round((doPrice * discountDOD / 100), companyid);
                            discountPrice = (doPrice) - authHandler.round((doPrice * discountDOD / 100), companyid);;
                        } else {
                            totalDiscount+=discountDOD;
                            discountPrice = doPrice - discountDOD;
                        }
                        amount += discountPrice;
                        tempdodobj = temp;
                        // getting tax also
                        if (temp.getTax() != null) {
                            includeprotax = true;
                        }
                        double taxAmt = temp.getRowTaxAmount();
                        amount += taxAmt;
                        if (tempdodobj != null && tempdodobj.getCidetails() != null) {
                            if (tempdodobj.getCidetails().getInvoice() != null) {
                                invoiceno.add(tempdodobj.getCidetails().getInvoice().getInvoiceNumber());
                            }
                        }
                    }
                }
                
                double taxPercent = 0;
                double totalTermAmount = 0;
                double taxableamount = 0;
                HashMap<String, Object> filterrequestParams = new HashMap();
                filterrequestParams.put("taxid", deliveryorder.getTax()==null?"":deliveryorder.getTax().getID());
                KwlReturnObject doResult = null;
                HashMap<String, Object> requestParam = new HashMap();
                requestParam.put("deliveryOrderID", deliveryorder.getID());
                doResult = accInvoiceDAOobj.getDOTermMap(requestParam);
                List<DeliveryOrderTermMap> termMap = doResult.getEntityList();
                for (DeliveryOrderTermMap deliveryOrderTermMap : termMap) {
                    filterrequestParams.put("term", deliveryOrderTermMap.getTerm()==null?"":deliveryOrderTermMap.getTerm().getId());
                    InvoiceTermsSales mt = deliveryOrderTermMap.getTerm();
                    double termAmnt = deliveryOrderTermMap.getTermamount();
                    totalTermAmount += authHandler.round(termAmnt, companyid);
                    boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                    if(isTermMappedwithTax){
                        taxableamount += termAmnt;
                    }
                }
                totalTermAmount = authHandler.round(totalTermAmount, companyid);
                if (deliveryorder.getTax() != null) {
                    KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), deliveryorder.getOrderDate(), deliveryorder.getTax().getID());
                    taxPercent = (Double) taxresult.getEntityList().get(0);

                }
                
                double ordertaxamount=(taxPercent==0?0:authHandler.round(((amount + taxableamount)*taxPercent/100), companyid));
                amount=amount+ordertaxamount+totalTermAmount;
                if (deliveryorder.getCurrency() != null) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, deliveryorder.getCurrency().getCurrencyID(), deliveryorder.getOrderDate(), deliveryorder.getExternalCurrencyRate());
                    newJobj.put("amount", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                }
                String status=messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request));
                HashSet<String> invids=new HashSet<String>();
                for(DeliveryOrderDetail dod:deliveryorder.getRows()){
                    if(dod.getCidetails()!=null){
                        if(invids.contains(dod.getCidetails().getInvoice().getID())){
                            continue;
                        }
                        invids.add(dod.getCidetails().getInvoice().getID());
                        if(!dod.getCidetails().getInvoice().isIsOpenDO()){
                            status=messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request));
                        }else{
                            status=messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request));
                            break;
                        }
                    }
                }
                if(!deliveryorder.isIsOpenInSI() || !deliveryorder.isIsOpenInSR()){
                    status=messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request));
                }
                newJobj.put("status", status);
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", false);
                newJobj.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Delivery_Order_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (deliveryorder.getDeliveryOrderCustomData()!=null && !customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Delivery_Order_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    String customFieldMapValues = "";
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(DeliveryOrderCustomData.class.getName(), deliveryorder.getDeliveryOrderCustomData().getDeliveryOrderId());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        DeliveryOrderCustomData doCustomData = (DeliveryOrderCustomData) custumObjresult.getEntityList().get(0);
                        if (doCustomData != null) {
                            AccountingManager.setCustomColumnValues(doCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            params.put("isReturnDropdownCheckListVal", true);
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getDeliveryOrdersforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getCreditNoteforRegistryJson(HttpServletRequest request, List<Object []> cnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> requestParams = getCustomerRegistryMap(request);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for(Object obj[]:cnotes){
                String doid = obj[1].toString();
                KwlReturnObject notes = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), doid);
                CreditNote creditnote = (CreditNote) notes.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
//                newJobj.put("transactionDate", df.format(creditnote.getJournalEntry().getEntryDate()));
                newJobj.put("transactionDate", df.format(creditnote.getCreationDate()));
                newJobj.put("documentno", creditnote.getCreditNoteNumber());
                newJobj.put("customerName", creditnote.getCustomer().getName());
                newJobj.put("amount", authHandler.round(creditnote.getCnamountinbase(), companyid));
                newJobj.put("cntype", creditnote.getCntype());
                if(creditnote.getCnamountdue()==0){
                    newJobj.put("status", messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)));
                }else{
                    newJobj.put("status", messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request)));
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", false);
                newJobj.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Credit_Note_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (!customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Credit_Note_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), creditnote.getJournalEntry().getID());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                        if (jeCustomData != null) {
                            AccountingManager.setCustomColumnValues(jeCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCreditNoteforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    public JSONArray getOpeningCreditNoteforRegistryJson(HttpServletRequest request, List cnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> requestParams = getCustomerRegistryMap(request);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for(Object obj:cnotes){
                CreditNote creditnote = (CreditNote) obj;
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(creditnote.getCreationDate()));
                newJobj.put("documentno", creditnote.getCreditNoteNumber());
                newJobj.put("customerName", creditnote.getCustomer().getName());
                newJobj.put("amount", authHandler.round(creditnote.getOriginalOpeningBalanceBaseAmount(), companyid));
                newJobj.put("cntype", creditnote.getCntype());
                if(creditnote.getOpeningBalanceAmountDue()==0){
                    newJobj.put("status", messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)));
                }else{
                    newJobj.put("status", messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request)));
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", true);
                newJobj.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Credit_Note_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (creditnote.getOpeningBalanceCreditNoteCustomData() != null && !customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Credit_Note_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceCreditNoteCustomData.class.getName(), creditnote.getOpeningBalanceCreditNoteCustomData().getOpeningBalanceCreditNoteId());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        OpeningBalanceCreditNoteCustomData OBCreditNoteCustomData = (OpeningBalanceCreditNoteCustomData) custumObjresult.getEntityList().get(0);
                        if (OBCreditNoteCustomData != null) {
                            AccountingManager.setCustomColumnValues(OBCreditNoteCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            params.put("isReturnDropdownCheckListVal", true);
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getOpeningCreditNoteforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getDebitNoteforRegistryJson(HttpServletRequest request, List<Object []> dnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> requestParams = getCustomerRegistryMap(request);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for(Object obj[]:dnotes){
                String dnid = obj[1].toString();
                KwlReturnObject note = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
                DebitNote debitnote = (DebitNote) note.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
//                newJobj.put("transactionDate", df.format(debitnote.getJournalEntry().getEntryDate()));
                newJobj.put("transactionDate", df.format(debitnote.getCreationDate()));
                newJobj.put("documentno", debitnote.getDebitNoteNumber());
                newJobj.put("customerName", debitnote.getCustomer().getName());
                newJobj.put("amount", authHandler.round(debitnote.getDnamountinbase(), companyid));
                newJobj.put("cntype", debitnote.getDntype());
                if(debitnote.getDnamountdue()==0){
                    newJobj.put("status", messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)));
                }else{
                    newJobj.put("status", messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request)));
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", false);
                newJobj.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Debit_Note_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (!customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Debit_Note_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    String customFieldMapValues = "";
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), debitnote.getJournalEntry().getID());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                        if (jeCustomData != null) {
                            AccountingManager.setCustomColumnValues(jeCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getDebitNoteforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    public JSONArray getOpeningDebitNoteforRegistryJson(HttpServletRequest request, List dnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> requestParams = getCustomerRegistryMap(request);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for(Object obj:dnotes){
                DebitNote debitnote = (DebitNote) obj;
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(debitnote.getCreationDate()));
                newJobj.put("documentno", debitnote.getDebitNoteNumber());
                newJobj.put("customerName", debitnote.getCustomer().getName());
                newJobj.put("amount", authHandler.round(debitnote.getOriginalOpeningBalanceBaseAmount(), companyid));
                newJobj.put("cntype", debitnote.getDntype());
                if(debitnote.getOpeningBalanceAmountDue()==0){
                    newJobj.put("status", messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)));
                }else{
                    newJobj.put("status", messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request)));
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", true);
                newJobj.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Debit_Note_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (debitnote.getOpeningBalanceDebitNoteCustomData() != null && !customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Debit_Note_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    String customFieldMapValues = "";
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceDebitNoteCustomData.class.getName(), debitnote.getOpeningBalanceDebitNoteCustomData().getOpeningBalanceDebitNoteId());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        OpeningBalanceDebitNoteCustomData OBDebitNoteCustomData = (OpeningBalanceDebitNoteCustomData) custumObjresult.getEntityList().get(0);
                        if (OBDebitNoteCustomData != null) {
                            AccountingManager.setCustomColumnValues(OBDebitNoteCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            params.put("isReturnDropdownCheckListVal", true);
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getOpeningDebitNoteforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getReceivedPaymentsforRegistryJson(HttpServletRequest request, List<Object []> receipts, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> requestParams = getCustomerRegistryMap(request);
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for(Object obj[]:receipts){
                Receipt receipt = (Receipt) obj[0];
                JSONObject newJobj = new JSONObject();
//                newJobj.put("transactionDate", df.format(receipt.getJournalEntry().getEntryDate()));
                newJobj.put("transactionDate", df.format(receipt.getCreationDate()));
                newJobj.put("documentno", receipt.getReceiptNumber());
                newJobj.put("customerName", receipt.getCustomer().getName());
                double amount = 0;
                String reccurrencyid = (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID());
                amount = receipt.getDepositAmount();
                amount = authHandler.round(amount, companyid);
//                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, reccurrencyid, receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, reccurrencyid, receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                double amountinbase = (Double) bAmt.getEntityList().get(0);
                newJobj.put("amount", authHandler.formattedAmount(amountinbase, companyid));
                newJobj.put("status", messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)));
                if (receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty() && receipt.getPaymentWindowType()==1) {
                    for (ReceiptAdvanceDetail detailOfReceipt : receipt.getReceiptAdvanceDetails()) {
                        if (detailOfReceipt.getAmountDue() != 0) {
                            newJobj.put("status", messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request)));
                            break;
                        }
                    }
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", false);
                newJobj.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Receive_Payment_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (!customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Receive_Payment_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), receipt.getJournalEntry().getID());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                        if (jeCustomData != null) {
                            AccountingManager.setCustomColumnValues(jeCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceivedPaymentsforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    public JSONArray getOpeningReceivedPaymentsJson(HttpServletRequest request, List receipts, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> requestParams = getCustomerRegistryMap(request);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for(Object obj:receipts){
                Receipt receipt = (Receipt) obj;
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(receipt.getCreationDate()));
                newJobj.put("documentno", receipt.getReceiptNumber());
                newJobj.put("customerName", receipt.getCustomer().getName());
                newJobj.put("amount", authHandler.formattedAmount(receipt.getOriginalOpeningBalanceBaseAmount(), companyid));
                newJobj.put("status", messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)));
                if(receipt.getOpeningBalanceAmountDue()!=0){
                    newJobj.put("status", messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request)));
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", true);
                newJobj.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Receive_Payment_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (receipt.getOpeningBalanceReceiptCustomData() != null && !customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Receive_Payment_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceReceiptCustomData.class.getName(), receipt.getOpeningBalanceReceiptCustomData().getOpeningBalanceReceiptId());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        OpeningBalanceReceiptCustomData OBReceiptCustomData = (OpeningBalanceReceiptCustomData) custumObjresult.getEntityList().get(0);
                        if (OBReceiptCustomData != null) {
                            AccountingManager.setCustomColumnValues(OBReceiptCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            params.put("isReturnDropdownCheckListVal", true);
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getOpeningReceivedPaymentsJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getMadePaymentsforRegistryJson(HttpServletRequest request, List<Object []> payments, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> requestParams = getCustomerRegistryMap(request);
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request); 
            for(Object obj[]:payments){
                Payment payment = (Payment) obj[0];
                JSONObject newJobj = new JSONObject();
//                newJobj.put("transactionDate", df.format(payment.getJournalEntry().getEntryDate()));
                newJobj.put("transactionDate", df.format(payment.getCreationDate()));
                newJobj.put("documentno", payment.getPaymentNumber());
                KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                Customer customer = (Customer) custResult.getEntityList().get(0);
                newJobj.put("customerName", customer.getName());
                double amount = 0;
                amount = payment.getDepositAmount();
                if(payment.getBankChargesAmount() > 0 && payment.getJournalEntryForBankCharges()==null){
                    amount-= payment.getBankChargesAmount();
                }
                if(payment.getBankInterestAmount() > 0 && payment.getJournalEntryForBankInterest()==null){
                    amount-= payment.getBankInterestAmount();
                }
                amount = authHandler.round(amount, companyid);
                String paycurrencyid = (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID());
//                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, paycurrencyid, payment.getJournalEntry().getEntryDate(), payment.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, paycurrencyid, payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate());
                double amountinbase = (Double) bAmt.getEntityList().get(0);
                newJobj.put("amount", authHandler.formattedAmount(amountinbase, companyid));
                newJobj.put("status", messageSource.getMessage("acc.field.Closed", null, RequestContextUtils.getLocale(request)));
                if (payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty() && payment.getPaymentWindowType()==1) {
                    for (AdvanceDetail detailOfPayment : payment.getAdvanceDetails()) {
                        if (detailOfPayment.getAmountDue() != 0) {
                            newJobj.put("status", messageSource.getMessage("acc.field.Open", null, RequestContextUtils.getLocale(request)));
                            break;
                        }
                    }
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("isOpeningBalanceTransaction", false);
                newJobj.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                HashMap hashMap = new HashMap();
                hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
                hashMap.put("reportId", requestParams.get("reportId").toString());
                hashMap.put("moduleId", Constants.Acc_Make_Payment_ModuleId);
                KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
                List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
                if (!customizeReportList.isEmpty()) {
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Make_Payment_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
                    FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    KwlReturnObject custumObjresult = null;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), payment.getJournalEntry().getID());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                        if (jeCustomData != null) {
                            AccountingManager.setCustomColumnValues(jeCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            params.put("isReturnDropdownCheckListVal", true);
                            if(!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))){
                                params.put(Constants.browsertz, sessionHandlerImpl.getBrowserTZ(request));
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getMadePaymentsforRegistryJson : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    /**
     * Description: Method for importing Customer Address Details
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView importCustomerAddressDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = getImportPurchaseOrderParams(request);
            jobj = importCustomerAddressDetailsJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description: Method for getting parameters of import Customer Address
     * Details
     *
     * @param request
     * @return
     * @throws JSONException
     * @throws SessionExpiredException
     */
    public JSONObject getImportPurchaseOrderParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        return paramJobj;
    }

    /**
     * Description: Method for importing and validating Customer Address Details
     *
     * @param paramJobj
     * @return
     */
    public JSONObject importCustomerAddressDetailsJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        try {
            String doAction = paramJobj.getString("do");
            String eParams = paramJobj.getString("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", paramJobj.getString(Constants.companyKey));

            // for getting import parameters
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramJobj);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", paramJobj.get("servletContext"));
            requestParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            requestParams.put(Constants.moduleid, Constants.CUSTOMER_ADDRESS_DETAILS_MODULE_ID);
            requestParams.put("moduleName", Constants.CUSTOMER_ADDRESS_DETAILS_MODULE_NAME);
            requestParams.put("tzdiff", paramJobj.getString(Constants.timezonedifference));

            if (doAction.compareToIgnoreCase("import") == 0) {
                requestParams.put("action", doAction);
                String exceededLimit = paramJobj.getString("exceededLimit");
                // If file contains records more than 1500 then Import file in background using thread
                if (exceededLimit.equalsIgnoreCase("yes")) {
                    String logId = importHandler.addPendingImportLog(requestParams);
                    requestParams.put("logId", logId);
                    importHandler.add(requestParams);
                    if (!importHandler.isIsWorking()) {
                        Thread t = new Thread(importHandler);
                        t.start();
                    }
                    jobj.put(Constants.RES_success, true);
                } else {
                    jobj = importHandler.importFileData(requestParams);
                }
                jobj.put("exceededLimit", exceededLimit);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                jobj = importHandler.validateFileData(requestParams);
                jobj.put(Constants.RES_success, true);
            }
        } catch (Exception ex) {
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }
    
    /**
     * Description: Method for importing Customer Category
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView importCustomerCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = getImportCustomerCategoryParams(request);
            jobj = importCustomerCategoryJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description: Method for getting parameters of import Customer Category
     * Details
     *
     * @param request
     * @return
     * @throws JSONException
     * @throws SessionExpiredException
     */
    public JSONObject getImportCustomerCategoryParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        return paramJobj;
    }

    /**
     * Description: Method for importing and validating Customer Category
     *
     * @param paramJobj
     * @return
     */
    public JSONObject importCustomerCategoryJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        try {
            String doAction = paramJobj.getString("do");
            String eParams = paramJobj.getString("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);

            // for getting import parameters
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramJobj);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", paramJobj.get("servletContext"));
            requestParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            requestParams.put(Constants.moduleid, Constants.CUSTOMER_CATEGORY_MODULE_ID);
            requestParams.put("moduleName", Constants.CUSTOMER_CATEGORY_MODULE_NAME);
            requestParams.put("tzdiff", paramJobj.getString(Constants.timezonedifference));

            if (doAction.compareToIgnoreCase("import") == 0) {
                requestParams.put("action", doAction);
                String exceededLimit = paramJobj.getString("exceededLimit");
                // If file contains records more than 1500 then Import file in background using thread
                if (exceededLimit.equalsIgnoreCase("yes")) {
                    String logId = importHandler.addPendingImportLog(requestParams);
                    requestParams.put("logId", logId);
                    importHandler.add(requestParams);
                    if (!importHandler.isIsWorking()) {
                        Thread t = new Thread(importHandler);
                        t.start();
                    }
                    jobj.put(Constants.RES_success, true);
                } else {
                    jobj = importHandler.importFileData(requestParams);
                }
                jobj.put("exceededLimit", exceededLimit);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                jobj = importHandler.validateFileData(requestParams);
                jobj.put(Constants.RES_success, true);
            }
        } catch (Exception ex) {
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }
    /*Method for getting UOB Receiving details for customer
     * 
     */ 
    public ModelAndView getUOBReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            boolean isForForm = request.getParameter("isForForm")!=null?Boolean.parseBoolean(request.getParameter("isForForm")):false;
            JSONArray dataArray = accCustomerControllerCMNServiceObj.getUOBReceivingBankDetails(request);
            JSONArray pagedJson = dataArray;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            if(isForForm){
                if(pagedJson.length()>0){
                    jobj.put("dataExists", true);
                }
            }
            jobj.put("data", pagedJson);
            jobj.put("count", dataArray.length());

        } catch (JSONException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "getUOBReceivingBankDetails : " + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView activateDeactivateUOBBankType(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject json = new JSONObject();
        StringBuilder msg = new StringBuilder();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Customer_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean success = true;
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("detailID")) && !StringUtil.isNullOrEmpty(request.getParameter("activateDeactivateFlag"))) {
                String detailID = request.getParameter("detailID");
                KwlReturnObject categoryresult = accountingHandlerDAOobj.getObject(UOBReceivingDetails.class.getName(), detailID);
                UOBReceivingDetails uobReceivingDetails = (UOBReceivingDetails) categoryresult.getEntityList().get(0);
                if (uobReceivingDetails != null) {
                    HashMap<String, Object> requestMap = new HashMap<>();
                    requestMap.put("customer", uobReceivingDetails.getCustomer() != null ? uobReceivingDetails.getCustomer().getID() : "");
                    requestMap.put("customerBankAccountType", uobReceivingDetails.getCustomerBankAccountType() != null ? uobReceivingDetails.getCustomerBankAccountType().getID() : "");
                    requestMap.put("companyId", companyID);
                    boolean activateDeactivateFlag = Boolean.parseBoolean(request.getParameter("activateDeactivateFlag"));
                    if (!activateDeactivateFlag) {
                        requestMap.put("isGIROFileGeneratedForUOBBank", false);
                        boolean isUsedFileGenerated = accCustomerDAOobj.isIBGDetailsUsedInTransaction(requestMap);
                        if (isUsedFileGenerated) {
                            throw new AccountingException("GIRO file generation with selected bank account type is pending, so you cannot deactivate selected customer bank account.");
                        } else {
                            uobReceivingDetails.setActivated(false);
                        }
                    } else {
                        requestMap.put("activated", false); // update status for existing data to deactivated.
                        accCustomerDAOobj.updateStatusOfExistingBankAccountType(requestMap);
                        uobReceivingDetails.setActivated(true);
                    }
                }
            }
            txnManager.commit(status);
            msg.append("Selected bank account type updated successfully.");
        } catch (AccountingException ex) {
            success = false;
            msg.append(ex.getMessage());
            txnManager.rollback(status);
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            json.put("msg", msg.toString());
            json.put("success", success);
        }
        return new ModelAndView("jsonView", "model", json.toString());
    }
    
    public ModelAndView isCustomerBankAccountPresent(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject json = new JSONObject();
        StringBuilder msg = new StringBuilder();
        boolean success = true;
        try {
            String arrayOfBillIds = request.getParameter("arrayOfBillIds");
            if (!StringUtil.isNullOrEmpty(arrayOfBillIds)) {
                String billids[] = arrayOfBillIds.split(",");
                for (String billid : billids) {
                    if (!StringUtil.isNullOrEmpty(billid)) {
                        KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), billid);
                        Invoice invoice = (Invoice) result.getEntityList().get(0);
                        if (invoice != null) {
                            HashMap<String, Object> paramsForReceivingDetails = new HashMap<>();
                            paramsForReceivingDetails.put("companyId", sessionHandlerImpl.getCompanyid(request));
                            paramsForReceivingDetails.put("customer", invoice.getCustomer() != null ? invoice.getCustomer().getID() : "");
                            paramsForReceivingDetails.put("customerBankAccountType", invoice.getCustomerBankAccountType() != null ? invoice.getCustomerBankAccountType().getID() : "");
                            paramsForReceivingDetails.put("activated", true);
                            result = accAccountDAOobj.getUOBReceivingBankDetails(paramsForReceivingDetails);
                            List resultList = result.getEntityList();
                            if (resultList == null || resultList.isEmpty() || resultList.get(0) == null) {
                                throw new AccountingException(messageSource.getMessage("acc.uob.receivingDetailsNotSetToCustomer", null, RequestContextUtils.getLocale(request)) + "</br>" + invoice.getCustomer().getName());
                            }
                        }
                    }
                }
            }

        } catch (AccountingException ex) {
            success = false;
            msg.append(ex.getMessage());
        } catch (Exception ex) {
            success = false;
        } finally {
            json.put("msg", msg.toString());
            json.put("success", success);
        }
        return new ModelAndView("jsonView", "model", json.toString());
    }
    public ModelAndView saveUOBReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Customer_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {

            UOBReceivingDetails receivingBankDetails = saveUOBReceivingBankDetails(request);
            
            issuccess = true;

            msg = messageSource.getMessage("acc.comman.receiving.saved", null, RequestContextUtils.getLocale(request));   //IBG-Receiving Bank Details has been saved successfully.

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    
    public UOBReceivingDetails saveUOBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        UOBReceivingDetails receivingBankDetails = null;
        try {
            HashMap<String, Object> requestMap = getUOBReceivingBankDetailsRequestParamsMap(request);
            String customerBankAccountType = (String)requestMap.get("customerBankAccountType");
            String customer = (String)requestMap.get("customer");
            String companyId = (String)requestMap.get("companyId");
            
            HashMap<String,Object> deleteMap = new HashMap<>();
            deleteMap.put("customer", customer);
            deleteMap.put("customerBankAccountType", customerBankAccountType);
            deleteMap.put("companyId", companyId);
            boolean isEdit = !StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? Boolean.parseBoolean(request.getParameter("isEdit")) : false;
            KwlReturnObject returnObject = null;
            if (isEdit) { 
                /**
                 * Add receiving bank detail ID in edit case, to update the
                 * existing record.
                 */
                requestMap.put("receivingBankDetailId", request.getParameter("UOBReceivingBankDetailId"));
            } else {
                deleteMap.put("activated", false); // update status for existing data to deactivated.
                accCustomerDAOobj.updateStatusOfExistingBankAccountType(deleteMap);
                requestMap.put("activated", true); // Newly created bank account will be activated by default.
            }
            returnObject = accCustomerDAOobj.saveUOBReceivingBankDetails(requestMap);

            if (!returnObject.getEntityList().isEmpty()) {
                receivingBankDetails = (UOBReceivingDetails) returnObject.getEntityList().get(0);
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("saveUOBReceivingBankDetails : " + ex.getMessage(), ex);
        }

        return receivingBankDetails;
    }

    
    public HashMap<String, Object> getUOBReceivingBankDetailsRequestParamsMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();

        requestParams.put("customerBankAccountType", request.getParameter("customerBankAccountType"));
        
        requestParams.put("receivingBankDetailId", request.getParameter("receivingBankDetailId"));

        requestParams.put("UOBBICCode", request.getParameter("UOBBICCode"));

        requestParams.put("UOBReceivingBankAccNumber", request.getParameter("UOBReceivingBankAccNumber"));
        
        requestParams.put("UOBReceivingAccName", request.getParameter("UOBReceivingAccName"));

        requestParams.put("UOBEndToEndID", request.getParameter("UOBEndToEndID"));

        requestParams.put("UOBMandateId", request.getParameter("UOBMandateId"));

        requestParams.put("UOBPurposeCode", request.getParameter("UOBPurposeCode"));
        
        requestParams.put("UOBCustomerReference", request.getParameter("UOBCustomerReference"));
        
        requestParams.put("UOBUltimatePayerBeneficiaryName", request.getParameter("UOBUltimatePayerBeneficiaryName"));
        
        requestParams.put("UOBCurrency", request.getParameter("UOBCurrency"));

        requestParams.put("customer", request.getParameter("customer"));

        requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
        
        requestParams.put(Constants.isEdit, request.getParameter(Constants.isEdit));

        requestParams.put("bankNameForUOB", request.getParameter("bankNameForUOB"));

        requestParams.put("UOBBankCode", request.getParameter("UOBBankCode"));
        
        requestParams.put("UOBBranchCode", request.getParameter("UOBBranchCode"));

        return requestParams;
    }
    
    public ModelAndView deleteUOBReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Vendor_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            accCustomerControllerCMNServiceObj.deleteUOBReceivingBankDetails(request);

            issuccess = true;

            msg = messageSource.getMessage("acc.uob.receiving.deleted", null, RequestContextUtils.getLocale(request));

            txnManager.commit(status);

        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = messageSource.getMessage("acc.uob.receiving.details.usedIn.transaction.delete", null, RequestContextUtils.getLocale(request));
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException | ServiceException |NoSuchMessageException | TransactionException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView isIBGDetailsUsedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isIBGDetailsUsedInTransaction = false;
        try {
            HashMap<String, Object> deleteMap = getUOBReceivingBankDetailsRequestParamsMap(request);
            isIBGDetailsUsedInTransaction = accCustomerDAOobj.isIBGDetailsUsedInTransaction(deleteMap);
            msg = messageSource.getMessage("acc.uob.receiving.details.usedIn.transaction.edit", null, RequestContextUtils.getLocale(request));
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("isIBGDetailsUsedInTransaction", isIBGDetailsUsedInTransaction);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getGiroFileGenerationHistory(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            
            HashMap<String, Object> map = getGiroFileGenerationHistoryMap(request);
            JSONArray dataArray = accCustomerControllerCMNServiceObj.getGiroFileGenerationHistory(map);
            JSONArray pagedJson = dataArray;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            
            jobj.put("data", pagedJson);
            jobj.put("count", dataArray.length());

        } catch (JSONException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "getUOBReceivingBankDetails : " + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public HashMap getGiroFileGenerationHistoryMap(HttpServletRequest request) {
        HashMap<String, Object> map = new HashMap();
        try {
            map.put("startdate", request.getParameter("startdate"));
            map.put("enddate", request.getParameter("enddate"));
            map.put("generationdate", request.getParameter("generationdate"));
            map.put("bank", request.getParameter("bank"));
            map.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            map.put(Constants.df, authHandler.getDateOnlyFormat(request));
            map.put(Constants.start, request.getParameter(Constants.start));
            map.put(Constants.limit,request.getParameter(Constants.limit));
        } catch (Exception ex) {
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }
    
    public ModelAndView downloadGiROFile(HttpServletRequest request,HttpServletResponse response){
         JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> map = getGiroFileGenerationHistoryMap(request);
            DateFormat df = (DateFormat) map.get(Constants.df);
            String id = request.getParameter("id");
            String destinationDirectory = storageHandlerImpl.GetDocStorePath();
            if (!StringUtil.isNullOrEmpty(id)) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(GiroFileGenerationHistory.class.getName(), id);
                GiroFileGenerationHistory giroFileGenerationHistory = (GiroFileGenerationHistory) result.getEntityList().get(0);
                if (giroFileGenerationHistory != null) {
                    String fileName = giroFileGenerationHistory.getFileName();
                    String downloadFileName = fileName;
                    int indexForSubString = fileName.indexOf("_", 0);
                    if (indexForSubString != -1) {
                        downloadFileName = fileName.substring(0, indexForSubString);
                    }
                    
                    File directory = new File(destinationDirectory +  Constants.GIRO_FILE_STORAGE_PATH);
                    if (directory.exists()) {
                        File file = new File(destinationDirectory+  Constants.GIRO_FILE_STORAGE_PATH + File.separator + fileName);
                        if (file != null && file.exists()) {
                            byte[] buff = new byte[(int) file.length()];
                            String type="txt";
                            try {
                                FileInputStream fis = new FileInputStream(file);
                                int read = fis.read(buff);
                            } catch (IOException ex) {
                                fileName = "file_not_found.txt";
                            }
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFileName + "." + type + "\"");
                            response.setContentType("application/octet-stream");
                            response.setContentLength(buff.length);
                            response.getOutputStream().write(buff);
                            response.getOutputStream().flush();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getCustomerBankAccountTypes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONArray array = getCustomerBankAccountTypes(request);
            jobj.put("data", array);
            jobj.put("count", array.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterItems : " + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getCustomerBankAccountTypes(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONArray finalArray = new JSONArray();
        try {
            
            JSONArray dataArray = accCustomerControllerCMNServiceObj.getUOBReceivingBankDetails(request);
            String customerBankAccountType="";
            MasterItem item=null;
            KwlReturnObject result = null;
            JSONObject objToAdd = null;
            for(int i=0;i<dataArray.length();i++){
                JSONObject obj = dataArray.getJSONObject(i);
                if (obj.optBoolean("activated", false)) { // show customer bank account type which are activated
                    customerBankAccountType = obj.optString("customerBankAccountType", "");
                    if (!StringUtil.isNullOrEmpty(customerBankAccountType)) {
                        result = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), customerBankAccountType);
                        item = (MasterItem) result.getEntityList().get(0);
                        objToAdd = new JSONObject();
                        objToAdd.put("id", item.getID());
                        objToAdd.put("activated", item.isActivated());
                        objToAdd.put("hasAccess", item.isActivated());
                        objToAdd.put("name", item.getValue());
                        objToAdd.put("groupid", Constants.Customer_Bank_Account_Type_GroupID);
                        finalArray.put(objToAdd);
                    }
                }
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } 
        return finalArray;
    }
    
    /**
     * Description: This method is used to get Customer Check In and Check Out Details
     * @param request
     * @param response
     * @return JSONObject
     */
    
    public ModelAndView getCustomerCheckInandCheckOutDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjTemp = new JSONObject();
        JSONObject returnObject = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            String start = (String) paramJobj.get("start");
            String limit = (String) paramJobj.get("limit");

            /*Get Grid configuration Meta Data and Column/Field Information */
            jobj = accCustomerMainAccountingService.getCustomerCheckInCheckOutRegistryGridInfo(paramJobj);
            /*Get Machine Details*/

            jobjTemp = accCustomerMainAccountingService.getCustomerCheckInandCheckOutDetails(paramJobj);

            JSONObject dataObj = jobjTemp.getJSONObject("data");

            JSONArray pagedJson = dataObj.getJSONArray("coldata");      // pagedJson is applied to get work paging properly.
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            dataObj.put("coldata", pagedJson);
            dataObj.put("totalCount", dataObj.optInt("totalCount"));
            dataObj.put("columns", jobj.getJSONArray("columns"));
            dataObj.put("success", true);
            dataObj.put("metaData", jobj.getJSONObject("metadata"));
            returnObject.put("data", dataObj);
            returnObject.put("valid", true);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                returnObject.put("success", issuccess);
                returnObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, returnObject.toString());
    }
    
    /**
     * Description: This method is used to get Incident Cases Details
     * @param request
     * @param response
     * @return JSONObject
     */
    
    public ModelAndView getIncidentCasesDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjTemp = new JSONObject();
        JSONObject returnObject = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            String start = (String) paramJobj.get("start");
            String limit = (String) paramJobj.get("limit");

            if (paramJobj.has("start")) {
                paramJobj.remove("start");
            }
            if (paramJobj.has("limit")) {
                paramJobj.remove("limit");
            }
            /*Get Grid configuration Meta Data and Column/Field Information */
            jobj = accCustomerMainAccountingService.getIncidentCasesRegistryGridInfo(paramJobj);
            /*Get Machine Details*/

            jobjTemp = accCustomerMainAccountingService.getIncidentCasesDetails(paramJobj);

            JSONObject dataObj = jobjTemp.getJSONObject("data");

            JSONArray pagedJson = dataObj.getJSONArray("coldata");      // pagedJson is applied to get work paging properly.
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
}
            dataObj.put("coldata", pagedJson);
            dataObj.put("totalCount", dataObj.optInt("totalCount"));
            dataObj.put("columns", jobj.getJSONArray("columns"));
            dataObj.put("success", true);
            dataObj.put("metaData", jobj.getJSONObject("metadata"));
            returnObject.put("data", dataObj);
            returnObject.put("valid", true);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                returnObject.put("success", issuccess);
                returnObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, returnObject.toString());
    }
    
    /**
     * Description: This method is used to Export the Customer Check In and Check Out Details
     * @param request
     * @param response
     * @return JSONObject
     */
    public ModelAndView exportCustomerCheckInandCheckOutDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            JSONArray dataArray=accCustomerMainAccountingService.createCheckInCheckOutJSON(paramJobj);
            jobj.put("data", dataArray);
            exportDaoObj.processRequest(request, response, jobj);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    
    /**
     * Description: This method is used to Export the Customer Check In and Check Out Details
     * @param request
     * @param response
     * @return JSONObject
     */
    public ModelAndView exportIncidentCasesDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            JSONArray dataArray=accCustomerMainAccountingService.createIncidentCasesDetailJSON(paramJobj);
            jobj.put("data", dataArray);
            exportDaoObj.processRequest(request, response, jobj);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
}
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    
}
