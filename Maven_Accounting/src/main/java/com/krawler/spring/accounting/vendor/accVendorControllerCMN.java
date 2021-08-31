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

package com.krawler.spring.accounting.vendor;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accCusVenMapDAO;
import com.krawler.spring.accounting.account.accVendorCustomerProductDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerController;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.hql.accounting.VendorProductMapping;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesController;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.customer.accCustomerControllerCMN;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceHandler;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.util.ajax.JSON;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accVendorControllerCMN extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accVendorDAO accVendorDAOobj;
    private accProductDAO accProductObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private String successView;
    private accGoodsReceiptDAO accGoodsReceiptDAOobj;
    private accGoodsReceiptCMN accGoodsReceiptCMNobj;
    private exportMPXDAOImpl exportDaoObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private auditTrailDAO auditTrailObj;
    private accInvoiceCMN accInvoiceCMNobj;
    private com.krawler.spring.common.fieldDataManager accountingCommonfieldDataManage;
    private accCusVenMapDAO accCusVenMapDAOObj;
    private accVendorCustomerProductDAO accVendorCustomerProductDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accReceiptDAO accReceiptDAOobj;
    private accDebitNoteDAO accDebitNoteobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accVendorControllerService accVendorControllerServiceObj;
    private String auditMsg="",auditID="";
    private ImportHandler importHandler;
    private accVendorControllerCMNService accVendorcontrollerCMNService;
    private accPurchaseOrderDAO accPurchaseOrderobj;

    public void setaccVendorcontrollerCMNService(accVendorControllerCMNService accVendorcontrollerCMNService) {
        this.accVendorcontrollerCMNService = accVendorcontrollerCMNService;
    }

    public void setAccPurchaseOrderobj(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
    
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
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
    
    public void setaccVendorControllerService(accVendorControllerService accVendorControllerServiceObj) {
        this.accVendorControllerServiceObj = accVendorControllerServiceObj;
    }

    public void setAccVendorCustomerProductDAOobj(accVendorCustomerProductDAO accVendorCustomerProductDAOobj) {
        this.accVendorCustomerProductDAOobj = accVendorCustomerProductDAOobj;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setCommonfieldDataManager(com.krawler.spring.common.fieldDataManager fieldDataManagercntrl) {
        this.accountingCommonfieldDataManage = fieldDataManagercntrl;
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}
    public void setaccInvoiceCMN(accInvoiceCMN accInvoiceCMNobj) {
        this.accInvoiceCMNobj = accInvoiceCMNobj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj){
    	this.exportDaoObj = exportDaoObj;
    }

    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCMNobj) {
        this.accGoodsReceiptCMNobj = accGoodsReceiptCMNobj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOobj) {
        this.accGoodsReceiptDAOobj = accGoodsReceiptDAOobj;
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
    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
   
    public ModelAndView checkVendorTransactions(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", request.getParameter("accid"));
            requestParams.put("openbalance", request.getParameter("openbalance"));

            String companyid = sessionHandlerImpl.getCompanyid(request);
            checkVendorTransactions(requestParams, companyid);

            issuccess = true;
            msg = messageSource.getMessage("acc.field.NoTransactionAssociatedwiththisVendor", null, RequestContextUtils.getLocale(request));
        } catch (AccountingException ex) {
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void checkVendorTransactions(HashMap request, String companyid) throws ServiceException, AccountingException {

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
            //Check for products id
            String ss="";
             result = accVendorCustomerProductDAOobj.getProductsByVendor(accountid, companyid, null);
             count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
            }
            
            boolean isused = accCusVenMapDAOObj.isVendorUsedInTransactions(accountid, companyid);
            if (isused) {
                throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
            }
            
        }

    }
    
    /*
     * Returns customer accountcode or vendor accountcode according to 
     * request parameter
     */
     public ModelAndView getCustOrVenCode(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        CustomerVendorMapping customervendormapping = null;
        Vendor vendor = null;
        Customer customer=null;
        boolean isCustomer = Boolean.parseBoolean(request.getParameter("isCustomer"));
        String accid = request.getParameter("accid");
        String accCode = "";
        String custVensequenceformatid = "";
        try {
             if (isCustomer) {
                 customervendormapping = accCusVenMapDAOObj.checkCustomerMappingExists(accid);
                 if (customervendormapping != null) {
                     vendor = customervendormapping.getVendoraccountid();
                     accCode=vendor.getAcccode();
                     custVensequenceformatid=vendor.getSeqformat()!=null?vendor.getSeqformat().getID():"";
                 }
             } else {
                 customervendormapping = accCusVenMapDAOObj.checkVendorMappingExists(accid);
                 if (customervendormapping != null) {
                     customer = customervendormapping.getCustomeraccountid();
                     accCode=customer.getAcccode();
                     custVensequenceformatid=customer.getSeqformat()!=null?customer.getSeqformat().getID():"";
                 }
             }
         } catch (Exception ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put("custOrVenAccCode", accCode);
                jobj.put("custVensequenceformatid", custVensequenceformatid);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveVendor(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            Map<String, Object> requestMap = request.getParameterMap();
            paramJobj.put("requestMap", requestMap);
            jobj = accVendorcontrollerCMNService.saveVendor(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Function to get Vendor GST fields history data
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getVendorGSTHistory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject reqParams = StringUtil.convertRequestToJsonObject(request);
            jobj = accVendorcontrollerCMNService.getVendorGSTHistory(reqParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
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
    public ModelAndView getVendorUsedGSTHistory(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        String companyId = sessionHandlerImpl.getCompanyid(request);
        try {
            JSONObject reqParams = StringUtil.convertRequestToJsonObject(request);
            if (!StringUtil.isNullOrEmpty(companyId)) {
                reqParams.put("companyid", companyId);
            }
            jobj = accVendorcontrollerCMNService.getVendorGSTUsedHistory(reqParams);
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
       public ModelAndView saveVendorAddresses(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String vendorID = null, msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Customer_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try{
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            accVendorcontrollerCMNService.saveVendorAddresses(paramJobj);
            issuccess = true;
            msg = messageSource.getMessage("acc.ven.save", null, RequestContextUtils.getLocale(request));   //"Vendor information has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("perAccID", vendorID);
            } catch (JSONException ex) {
                Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
   
    public void saveVendorAddresses(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {      
        try{
            String vendorID = "";
            String customerID = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            vendorID = request.getParameter("accid");
            String addressDetails = request.getParameter("addressDetail");
            JSONArray jArr = new JSONArray(addressDetails);

                
            String propagationflag = request.getParameter("ispropagatetochildcompanyflag");
            boolean ispropagatetochildcompanyflag = !StringUtil.isNullOrEmpty(propagationflag) ? Boolean.parseBoolean(propagationflag) : false;
            KwlReturnObject returnObject = null;
            String masterGroupID = "";
            String data = "";
            String fetchColumn = "mst.value";
            String conditionColumn = "mst.ID";
            String parentcompanyid = companyid;
            String parentCompanyVendorID = vendorID;

            HashMap<String, Object> mappingParams = new HashMap<String, Object>();
            mappingParams.put("vendoraccountid", vendorID);
            KwlReturnObject mappingResult = accCusVenMapDAOObj.getCustomerVendorMapping(mappingParams);
            if (mappingResult != null && !mappingResult.getEntityList().isEmpty()) {
                CustomerVendorMapping mapping = (CustomerVendorMapping) mappingResult.getEntityList().get(0);
                if (mapping != null && mapping.getCustomeraccountid() != null) {
                    customerID = mapping.getCustomeraccountid().getID();
                    if (!StringUtil.isNullOrEmpty(customerID)) {
                        KwlReturnObject categoryresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerID);
                        Customer customer = (Customer) categoryresult.getEntityList().get(0);
                        if (customer.isCreatedInVendor()) {
                            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                            addrRequestParams.put("customerid", customerID);
                            addrRequestParams.put("companyid", companyid);
                            KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
                            if (addressResult.getEntityList().isEmpty()) {//if address is not given to vendor which is created with customer. in this case updating vendor address with customer address
                                for (int i = 0; i < jArr.length(); i++) {
                                    HashMap<String, Object> custAddrMap = new HashMap<String, Object>();
                                    JSONObject jobj = jArr.getJSONObject(i);
                                    custAddrMap.put("customerid", customerID);
                                    custAddrMap.put("aliasName", jobj.optString("aliasName", ""));
                                    custAddrMap.put("address", jobj.optString("address", ""));
                                    custAddrMap.put("city", jobj.optString("city", ""));
                                    custAddrMap.put("state", jobj.optString("state", ""));
                                    custAddrMap.put("stateCode", jobj.optString("stateCode", ""));
                                    custAddrMap.put("country", jobj.optString("country", ""));
                                    custAddrMap.put("postalCode", jobj.optString("postalCode", ""));
                                    custAddrMap.put("phone", jobj.optString("phone", ""));
                                    custAddrMap.put("mobileNumber", jobj.optString("mobileNumber", ""));
                                    custAddrMap.put("fax", jobj.optString("fax", ""));
                                    String email = jobj.optString("emailID", "").replaceAll("\\s", "");
                                    custAddrMap.put("emailID", email);
                                    custAddrMap.put("recipientName", jobj.optString("recipientName", ""));
                                    custAddrMap.put("contactPerson", jobj.optString("contactPerson", ""));
                                    custAddrMap.put("contactPersonNumber", jobj.optString("contactPersonNumber", ""));
                                    custAddrMap.put("contactPersonDesignation", jobj.optString("contactPersonDesignation", ""));
                                    custAddrMap.put("website", jobj.optString("website", ""));
                                    custAddrMap.put("isBillingAddress", jobj.getBoolean("isBillingAddress"));
                                    custAddrMap.put("isDefaultAddress", jobj.getBoolean("isDefaultAddress"));
                                    KwlReturnObject custAddrobject = accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, companyid);
                                }
                            }
                        }
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(vendorID)) {//deleteting previously added address
                KwlReturnObject deleteResult = accountingHandlerDAOobj.deleteVendorAddressDetails(vendorID, companyid);

                if (ispropagatetochildcompanyflag) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("parentCompanyVendorID", parentCompanyVendorID);
                    KwlReturnObject result = accVendorDAOobj.getChildVendors(requestParams);
                    List childCompaniesVendorList = result.getEntityList();
                    try {
                        for (Object childObj : childCompaniesVendorList) {
                            Vendor vend = (Vendor) childObj;
                            if (vend != null) {
                                String childCompanyID = vend.getCompany().getCompanyID();
                                String childcompanysvendorid = vend.getID();
                                deleteResult = accountingHandlerDAOobj.deleteVendorAddressDetails(childcompanysvendorid, childCompanyID);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            for (int i = 0; i < jArr.length(); i++) {
                HashMap<String, Object> vendAddrMap = new HashMap<String, Object>();
                JSONObject jobj = jArr.getJSONObject(i);
                vendAddrMap.put("vendorid", vendorID);
                vendAddrMap.put("aliasName", jobj.optString("aliasName", ""));
                vendAddrMap.put("address", jobj.optString("address", ""));
                vendAddrMap.put("city", jobj.optString("city", ""));
                vendAddrMap.put("state", jobj.optString("state", ""));
                vendAddrMap.put("stateCode", jobj.optString("stateCode", ""));
                vendAddrMap.put("country", jobj.optString("country", ""));
                vendAddrMap.put("postalCode", jobj.optString("postalCode", ""));
                vendAddrMap.put("phone", jobj.optString("phone", ""));
                vendAddrMap.put("mobileNumber", jobj.optString("mobileNumber", ""));
                vendAddrMap.put("fax", jobj.optString("fax", ""));
                String email = jobj.optString("emailID", "").replaceAll("\\s", "");
                vendAddrMap.put("emailID", email);
                vendAddrMap.put("recipientName", jobj.optString("recipientName", ""));
                vendAddrMap.put("contactPerson", jobj.optString("contactPerson", ""));
                vendAddrMap.put("contactPersonNumber", jobj.optString("contactPersonNumber", ""));
                vendAddrMap.put("contactPersonDesignation", jobj.optString("contactPersonDesignation", ""));
                vendAddrMap.put("website", jobj.optString("website", ""));
                vendAddrMap.put("isBillingAddress", jobj.getBoolean("isBillingAddress"));
                vendAddrMap.put("isDefaultAddress", jobj.getBoolean("isDefaultAddress"));
                KwlReturnObject vendAddrobject = accountingHandlerDAOobj.saveVendorAddressesDetails(vendAddrMap, companyid);
                
                 //*************************save address details in child companies ****************************8
                
                if (ispropagatetochildcompanyflag) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("parentCompanyVendorID", parentCompanyVendorID);
                    KwlReturnObject result = accVendorDAOobj.getChildVendors(requestParams);
                    List childCompaniesVendorList = result.getEntityList();
                    try {
                        for (Object childObj : childCompaniesVendorList) {

                            Vendor vend = (Vendor) childObj;
                            if (vend != null) {
                                String childCompanyID = vend.getCompany().getCompanyID();
                                String childcompanysvendorid = vend.getID();
                                vendAddrMap.put("vendorid", childcompanysvendorid);
                                vendAddrobject = accountingHandlerDAOobj.saveVendorAddressesDetails(vendAddrMap, childCompanyID);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //*************************save address details in child companies Ends****************************8
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveCustomer : " + ex.getMessage(), ex);
        }
    }

    public ModelAndView saveIBGReceivingBankDetailsJSON(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject ibgDetailsObj = new JSONObject();

            if (!StringUtil.isNullOrEmpty(request.getParameter("ibgReceivingDetails"))) {

                ibgDetailsObj = new JSONObject(request.getParameter("ibgReceivingDetails"));

                String masterItemId = request.getParameter("masterItemId");

                String companyId = sessionHandlerImpl.getCompanyid(request);

                HashMap<String, Object> dataParams = new HashMap<String, Object>();
                dataParams.put("ibgDetailsJsonObj", ibgDetailsObj);
                dataParams.put("masterItemId", masterItemId);
                dataParams.put("companyId", companyId);

                if (ibgDetailsObj.length() > 0) {
                    accVendorControllerServiceObj.saveIBGReceivingBankDetailsJSON(dataParams);

                    msg = "IBG Receiving Details saved successfully.";
                    issuccess = true;
                }
            }
           txnManager.commit(status);
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    
    
     public ModelAndView saveVendorCategoryMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String auditMsg = "", auditID = "";
        
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String[] vendorList=request.getParameter("personList").split(",");
            String[] vendorCategory=request.getParameter("personCategory").split(",");
            if(vendorList.length>0) {
                for(int i=0;i<vendorList.length;i++){
                    if (!StringUtil.isNullOrEmpty(vendorList[i])) {
                        accVendorDAOobj.deleteVendorCategoryMappingDtails(vendorList[i]);
                    }
                }
            }
            
            if(vendorList.length>0 && vendorCategory.length>0) {
                for(int i=0;i<vendorList.length;i++){
                    for(int j=0;j<vendorCategory.length;j++){
                        if (!StringUtil.isNullOrEmpty(vendorList[i]) && !StringUtil.isNullOrEmpty(vendorCategory[j])) {
                            accVendorDAOobj.saveVendorCategoryMapping(vendorList[i],vendorCategory[j]);
                            
                            KwlReturnObject vndresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorList[i]);
                            Vendor vendor = (Vendor) vndresult.getEntityList().get(0);
                            
                            KwlReturnObject categoryresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), vendorCategory[j]);
                            MasterItem vcategory = (MasterItem) categoryresult.getEntityList().get(0);
                            
                            auditMsg = " added new vendor category  "+vcategory.getValue()+" to ";
                            auditID = AuditAction.VENDOR_CATEGORY_CHANGED;
                            
                            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + vendor.getName(), request, vendor.getID());
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
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
     public ModelAndView saveVendorAgentMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String auditMsg = "", auditID = "";
        
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String[] vendorList=request.getParameter("personList").split(",");
            String[] agents=request.getParameter("personCategory").split(",");
            if(vendorList.length>0) {
                for(int i=0;i<vendorList.length;i++){
                    if (!StringUtil.isNullOrEmpty(vendorList[i])) {
                        accVendorDAOobj.deleteVendorAgentMapping(vendorList[i]);
                    }
                }
            }
            
            if(vendorList.length>0 && agents.length>0) {
                for(int i=0;i<vendorList.length;i++){
                    for(int j=0;j<agents.length;j++){
                        if (!StringUtil.isNullOrEmpty(vendorList[i]) && !StringUtil.isNullOrEmpty(agents[j])) {
                            accVendorDAOobj.saveAgentMapping(vendorList[i],agents[j]);
                            
                            KwlReturnObject vndresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorList[i]);
                            Vendor vendor = (Vendor) vndresult.getEntityList().get(0);
                            
                            KwlReturnObject agentresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), agents[j]);
                            MasterItem agent = (MasterItem) agentresult.getEntityList().get(0);
                            
                            auditMsg = " added new vendor agent  "+agent.getValue()+" to ";
                            auditID = AuditAction.VENDOR_AGENT_CHANGED;
                            
                            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + vendor.getName(), request, vendor.getID());
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
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
     public ModelAndView saveVendorPricingBandMapping(HttpServletRequest request, HttpServletResponse response) {
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
            String[] vendorList = request.getParameter("personList").split(",");
            String pricingBand = request.getParameter("personCategory");

            if (vendorList.length > 0 && !StringUtil.isNullOrEmpty(pricingBand)) {
                for (int i = 0; i < vendorList.length; i++) {
                    if (!StringUtil.isNullOrEmpty(vendorList[i])) {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("accid", vendorList[i]);
                        requestParams.put("pricingBand", pricingBand);
                        
                        KwlReturnObject result = accVendorDAOobj.updateVendor(requestParams);

                        List ll = result.getEntityList();
                        Vendor vendor = (Vendor) ll.get(0);

                        KwlReturnObject categoryresult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), pricingBand);
                        PricingBandMaster pricingBandMaster = (PricingBandMaster) categoryresult.getEntityList().get(0);

                        auditMsg = " added new vendor price list-band  " + pricingBandMaster.getName() + " to ";
                        auditID = AuditAction.PRICING_BAND_CHANGED;

                        auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + vendor.getName(), request, vendor.getID());
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
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView deleteIBGReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Vendor_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            accVendorControllerServiceObj.deleteIBGReceivingBankDetails(request);

            issuccess = true;

            msg = messageSource.getMessage("acc.ibg.receiving.deleted", null, RequestContextUtils.getLocale(request));   //IBG-Receiving Bank Detail has been deleted successfully.

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Added @Transactional instead of txnmanager - ERP-32983. Moved code
     * containing business logic to the accVendorControllerCMNServiceImpl No any
     * changes other than this has been done in code.
     * @param request
     * @param response
     * @return
     */
    public ModelAndView deleteVendor(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg="";
        boolean issuccess=false;
        try {
            String companyid=sessionHandlerImpl.getCompanyid(request);
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            jobj=accVendorcontrollerCMNService.deleteVendor(requestJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            msg = jobj.optString(Constants.RES_msg);
        } catch (SessionExpiredException ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch(JSONException ex){
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView activateDeactivateVendors(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj=accVendorcontrollerCMNService.activateDeactivateVendors(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getIBGReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONArray dataArray = accVendorControllerServiceObj.getIBGReceivingBankDetailsForVendor(request);
            JSONArray pagedJson = dataArray;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put(Constants.RES_data, pagedJson);

            jobj.put("count", dataArray.length());

        } catch (JSONException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "getIBGReceivingBankDetails : " + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    

    /**
     * @author neeraj
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getVendors(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = accVendorController.getVendorRequestMap(request);
            HashMap<String, Object> requestParamsproduct = productHandler.getProductRequestMap(request);
            KwlReturnObject result = accVendorDAOobj.getVendor(requestParams);
            
            boolean quickSearchFlag = false;
            if(requestParams.containsKey("ss") && requestParams.get("ss") != null && (!StringUtil.isNullOrEmpty(requestParams.get("ss").toString()))) {
                quickSearchFlag = true;
            }
//            ArrayList list = accAccountDAOobj.getAccountArrayList(result.getEntityList(), requestParams, quickSearchFlag, false);
            ArrayList list = accVendorDAOobj.getVendorArrayList(result.getEntityList(), requestParams, quickSearchFlag, false);
            JSONArray jArr= getVendorJson(request, list);
            
            //Flag used to run optimized code of getVendorAmountDue
            request.setAttribute("allvendors", true);
            
            jArr = getVendorAmountDue(jArr, request);
            
            jobj.put(Constants.RES_data, jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accCustomerController.getCustomers : "+ex.getMessage();
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
    
 public JSONArray getVendorJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONArray jArr=new JSONArray();
        try{
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> vendorRequestParams = new HashMap<String, Object>();
            KwlReturnObject prefRes = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            boolean isParentChildmappingAvailable = false;
            ExtraCompanyPreferences companyPreferences = (ExtraCompanyPreferences) prefRes.getEntityList().get(0);
            if (companyPreferences != null && companyPreferences.isPropagateToChildCompanies()) {
                isParentChildmappingAvailable = true;
            }
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) comp.getEntityList().get(0);
            
            /*get CustomFields & Dimension values*/
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid,Constants.Acc_Vendor_ModuleId));
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                String tempstring = "";
                String productsCode="";
                String productName="";
                JSONArray jrr = new JSONArray();
                Vendor vendor = (Vendor) row[1];
                Account account = vendor.getAccount();
                if (vendor == null) {
                    continue;
                }
                //getting the json vendor mapped products
                String Venid = vendor.getID();
                String ss="";
                KwlReturnObject productjsonobj = accVendorCustomerProductDAOobj.getProductsByVendor(Venid, ss, null);
                List<VendorProductMapping> listpro = productjsonobj.getEntityList();
                JSONArray vendorjarray = new JSONArray();
                for (VendorProductMapping VendorProductObj : listpro) {
                    JSONObject customJSONObj = new JSONObject();
                    String vendorproductsid = VendorProductObj.getProducts()!=null?VendorProductObj.getProducts().getID():"";
                    String vendorproductscode = VendorProductObj.getProducts()!=null?VendorProductObj.getProducts().getProductid():"";
                    String vendorproductsname = VendorProductObj.getProducts()!=null?VendorProductObj.getProducts().getProductName():"";
                    tempstring = tempstring.concat(vendorproductsid+",");
                    productsCode = productsCode.concat(vendorproductscode+",");
                    productName=productName.concat(vendorproductsname+",");
                    vendorjarray.put(vendorproductsid);
                    if (VendorProductObj.getProducts() != null && VendorProductObj.getJsonstring() != null) {
                        customJSONObj.put(VendorProductObj.getProducts().getID(), JSON.parse(VendorProductObj.getJsonstring().toString()));
                        jrr.put(customJSONObj);
                    }
                }
                if(!StringUtil.isNullOrEmpty(tempstring.toString())){
                    tempstring = tempstring.substring(0, tempstring.lastIndexOf(","));
                    productsCode = productsCode.substring(0, productsCode.lastIndexOf(","));
                    productName=productName.substring(0, productName.lastIndexOf(","));
                }
                JSONObject obj = new JSONObject();
                obj.put("acccode",(StringUtil.isNullOrEmpty(vendor.getAcccode()))?"":vendor.getAcccode());
//                obj.put("acccode",(StringUtil.isNullOrEmpty(vendor.getAcccode()))?"":vendor.getAcccode());
                obj.put("accid", vendor.getID());
                obj.put("accname", vendor.getName());
                obj.put("aliasname", vendor.getAliasname());
                obj.put("accnamecode", (StringUtil.isNullOrEmpty(vendor.getAcccode())) ? vendor.getName() : "[" + vendor.getAcccode() + "]" + vendor.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("accountname", account.getName());
                //Control Account Details
                obj.put("controlaccountcode", StringUtil.isNullOrEmpty(account.getAcccode())? "" : account.getAcccode());
                obj.put("controlaccountname", StringUtil.isNullOrEmpty(account.getName())? "" : account.getName());
                
                obj.put("isIBGActivated", vendor.isIbgActivated());
                obj.put("customJSONString",  jrr);
                if (vendor.isActivate()) {
                    obj.put("isactivate", "Active");
                } else {
                    obj.put("isactivate", "Dormant");
                }
                if (isParentChildmappingAvailable) {
                    vendorRequestParams.put("parentCompanyVendorID", vendor.getID());
                    KwlReturnObject result = accVendorDAOobj.getChildVendors(vendorRequestParams); // to check if parent has child vendor in child companies
                    if (result != null && result.getRecordTotalCount() > 0) {
                        obj.put("isPropagatedPersonalDetails", true);
                    } else {
                        obj.put("isPropagatedPersonalDetails", false);
                    }
                }
                obj.put("productname", tempstring);
                obj.put("isvendoravailabletoagent", vendor.isIsVendorAvailableOnlyToSelectedAgents());
                Set<VendorAgentMapping> vaSet = vendor.getAgent();
                String agentsMappedTVendor = "";
                String agentsMappedTVendorName = "";//ERP-19693
                for (VendorAgentMapping vgmObj : vaSet) {
                    if(!StringUtil.isNullObject(vgmObj.getAgent())){
                    agentsMappedTVendor += vgmObj.getAgent().getID() + ",";
                    agentsMappedTVendorName += vgmObj.getAgent().getValue() + ",";//ERP-19693
                }
                }
                if (!StringUtil.isNullOrEmpty(agentsMappedTVendor)) {
                    agentsMappedTVendor = agentsMappedTVendor.substring(0, (agentsMappedTVendor.length() - 1));
                    agentsMappedTVendorName = agentsMappedTVendorName.substring(0, (agentsMappedTVendorName.length() - 1));//ERP-19693
                }
                obj.put("agentsmappedwithvendor", agentsMappedTVendor);
                obj.put("salesPersonAgent", agentsMappedTVendorName);
                obj.put("defaultagentmappingid", ((vendor.getMappingAgent()!=null)?vendor.getMappingAgent().getID():""));
                
                JSONArray dataArrayOfDBSBank = new JSONArray();
                JSONArray dataArrayOfCIMBBank = new JSONArray();
                if (vendor.isIbgActivated()) {
                    request.setAttribute("vendorId", vendor.getID());
                    // Get data for DBS bank
                    dataArrayOfDBSBank = accVendorControllerServiceObj.getIBGReceivingBankDetailsForVendor(request);
                    if(dataArrayOfDBSBank.length()!=0){
                        obj.put("DBSbank", true);
                }
                    // Get data for CIMB bank
                    dataArrayOfCIMBBank = accVendorControllerServiceObj.getCIMBReceivingBankDetailsForVendor(request);
                    if(dataArrayOfCIMBBank.length()!=0){
                        obj.put("CIMBbank", true);
                    }
                    // Merging the data of both banks in single JSON
                    for(int i=0;i<dataArrayOfCIMBBank.length();i++){
                        dataArrayOfDBSBank.put(dataArrayOfCIMBBank.getJSONObject(i));
                    }
                    obj.put("ibgReceivingDetails", dataArrayOfDBSBank);
                }
                
                // calculation of opening balance

                double openbalance = accInvoiceCMNobj.getOpeningBalanceOfAccount(request, null, true, vendor.getID());
                 
                obj.put("openbalance", openbalance);
//                obj.put("openbalance", account.getOpeningBalance());
                Vendor parentVendor = (Vendor) row[5];
                if(parentVendor!=null){
                    obj.put("parentid", parentVendor.getID());
                    obj.put("parentname", parentVendor.getName());
                } else if(vendor.getParent() != null) {
                    obj.put("parentid", vendor.getParent().getID());
                    obj.put("parentname", vendor.getParent().getName());
                }
                KWLCurrency currency = (KWLCurrency) row[4];
                obj.put("currencyid",vendor.getCurrency()==null?currency.getCurrencyID():vendor.getCurrency().getCurrencyID());
                obj.put("currencysymbol",vendor.getCurrency()==null?currency.getSymbol():vendor.getCurrency().getSymbol());
                obj.put("currencyname",vendor.getCurrency()==null?currency.getName():vendor.getCurrency().getName());
                obj.put("level", row[2]);
                obj.put("leaf", row[3]);
                obj.put("id", vendor.getID());
                obj.put("title", vendor.getTitle());
                if (!StringUtil.isNullOrEmpty(vendor.getTitle())) {
                    KwlReturnObject taxresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), vendor.getTitle());
                    MasterItem title = (MasterItem) taxresult.getEntityList().get(0);
                    obj.put("titlename", title !=null ? title.getValue() : "");
                } else {
                    obj.put("titlename", "");
                }
                obj.put("taxId", vendor.getTaxid());
                if (!StringUtil.isNullOrEmpty(vendor.getTaxid())) {
                    KwlReturnObject taxresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), vendor.getTaxid());
                    Tax tax = (Tax) taxresult.getEntityList().get(0);
                    obj.put("taxcode", tax!=null ? tax.getTaxCode() : "");
                } else {
                    obj.put("taxcode", "");
                }
                obj.put("contactno2", vendor.getAltContactNumber());
                obj.put("uenno", vendor.getUENNumber());
                obj.put("vattinno", !StringUtil.isNullOrEmpty(vendor.getVATTINnumber())? vendor.getVATTINnumber():"");
                obj.put("csttinno", !StringUtil.isNullOrEmpty(vendor.getCSTTINnumber())? vendor.getCSTTINnumber():"");
                obj.put("vattinnumber", !StringUtil.isNullOrEmpty(vendor.getVATTINnumber())? vendor.getVATTINnumber():"");
                obj.put("csttinnumber", !StringUtil.isNullOrEmpty(vendor.getCSTTINnumber())? vendor.getCSTTINnumber():"");
                obj.put("pannumber", !StringUtil.isNullOrEmpty(vendor.getPANnumber())? vendor.getPANnumber():"");
                obj.put("eccnumber", !StringUtil.isNullOrEmpty(vendor.getECCnumber())? vendor.getECCnumber():"");
                obj.put("mappedReceivedFromId", ((vendor.getMappingReceivedFrom()!=null)?vendor.getMappingReceivedFrom().getID():""));
                obj.put("mappedPaidToId", ((vendor.getMappingPaidTo()!=null)?vendor.getMappingPaidTo().getID():""));
                if(companyPreferences != null && companyPreferences.getCompany().getCountry().getID().equals("106")){
                    obj.put("npwp", !StringUtil.isNullOrEmpty(vendor.getPANnumber())? vendor.getPANnumber() :"");
                }else{
                    obj.put("panno", !StringUtil.isNullOrEmpty(vendor.getPANnumber())? vendor.getPANnumber() :"");
                }
                obj.put("vendorbranch", !StringUtil.isNullOrEmpty(vendor.getVendorBranch())? vendor.getVendorBranch():"");
                obj.put("servicetaxno", !StringUtil.isNullOrEmpty(vendor.getSERVICEnumber())? vendor.getSERVICEnumber():"");
                obj.put("tanno", !StringUtil.isNullOrEmpty(vendor.getTANnumber())? vendor.getTANnumber():"");
                obj.put("eccno", !StringUtil.isNullOrEmpty(vendor.getECCnumber())? vendor.getECCnumber():"");
                obj.put("istaxeligible", vendor.isTaxEligible()?"Yes":"No");
                obj.put("pdm", vendor.getPreferedDeliveryMode());
                obj.put("termname", vendor.getDebitTerm()==null?"":vendor.getDebitTerm().getTermname());
                obj.put("termdays", vendor.getDebitTerm()==null?"":vendor.getDebitTerm().getTermdays());
                obj.put("termid", vendor.getDebitTerm()==null?"":vendor.getDebitTerm().getID());
                obj.put("bankaccountno", vendor.getBankaccountno());
                obj.put("other", (vendor.getOther()!=null)?vendor.getOther():"");
                String[] category=getVendorCategoryIDs(vendor.getID());
                obj.put("categoryid", category[0]);
                obj.put("categoryname", category[1]);
                obj.put("intercompanytypeid",vendor.getIntercompanytype()!=null?vendor.getIntercompanytype().getID():"");
                obj.put("intercompany", vendor.isIntercompanyflag());
                obj.put("taxeligible", vendor.isTaxEligible());
                obj.put("taxidnumber", vendor.getTaxIDNumber());
                obj.put("creationDate",(vendor.getCreatedOn()!=null)? authHandler.getDateOnlyFormat(request).format(vendor.getCreatedOn()):"");
                obj.put("deleted", vendor.getAccount().isDeleted());
                obj.put("limit", vendor.getDebitlimit());
                obj.put("mapcustomervendor", vendor.isMapcustomervendor());
                boolean isused = accCusVenMapDAOObj.isVendorUsedInTransactions(vendor.getID(), companyid); //ERP-19783
                obj.put("isInterstatepartyEditable",!isused);
                obj.put("interstateparty", vendor.isInterstateparty());
                obj.put("cformapplicable", vendor.isCformapplicable());
                //To Verify whether TDS is applied on respective Vendor or not.
                obj.put("isTDSapplicableonvendor", vendor.isIsTDSapplicableonvendor());
                //If Vendor is Used in TDS Transactions then do not "TDS Details" FieldSet again.
                obj.put("isVendorUsedInTDSTransactions", accCusVenMapDAOObj.isVendorUsedInTDSTransactions(vendor.getID(), companyid));
                obj.put("gtaapplicable", vendor.isGtaapplicable());
                obj.put("dealertype", !StringUtil.isNullOrEmpty(vendor.getDealertype())? vendor.getDealertype():"");
                obj.put("vatregdate",(vendor.getVatregdate()!=null)? authHandler.getDateOnlyFormat(request).format(vendor.getVatregdate()):"");
                obj.put("cstregdate",(vendor.getCSTRegDate()!=null)? authHandler.getDateOnlyFormat(request).format(vendor.getCSTRegDate()):"");
                obj.put("isUsedInTransactions",isused);
                obj.put("considerExemptLimit",vendor.isConsiderExemptLimit());
                /*For India Compliace - GST related fields - START*/
                obj.put("gstin", !StringUtil.isNullOrEmpty(vendor.getGSTIN())? vendor.getGSTIN():"");
                obj.put("GSTINRegistrationTypeId", vendor.getGSTRegistrationType()!=null ? vendor.getGSTRegistrationType().getID():"");
                obj.put("CustomerVendorTypeId", vendor.getGSTVendorType()!=null ? vendor.getGSTVendorType().getID():"");
                obj.put("GSTINRegistrationTypeName", vendor.getGSTRegistrationType()!=null ? vendor.getGSTRegistrationType().getValue():"");
                obj.put("CustomerVendorTypeName", vendor.getGSTVendorType()!=null ? vendor.getGSTVendorType().getValue():"");
                /*For India Compliace - GST related fields - END*/
                if(vendor.isMapcustomervendor()){
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(),filter_params = new ArrayList();
                    filter_names.add("c.vendoraccountid.ID");
                    filter_params.add(vendor.getID());
                    requestParams.put("filter_names", filter_names);
                    requestParams.put("filter_params", filter_params);
                    KwlReturnObject res = accVendorDAOobj.getCustomerVendorMapping(requestParams);
                    List ll = res.getEntityList();
                    Iterator iterator = ll.iterator();
                    while(iterator.hasNext()) {
                        CustomerVendorMapping customerVendorMapping = (CustomerVendorMapping) iterator.next();
                        obj.put("mappingcusaccid", (customerVendorMapping.getCustomeraccountid()!=null)?customerVendorMapping.getCustomeraccountid().getAccount().getID():"");
                    }
                }
                obj.put("contactperson", (vendor.getContactperson() == null)? "" :  vendor.getContactperson());
                obj.put("mappingvenaccid", vendor.getAccount().getID());
                obj.put(Constants.sequenceformat, vendor.getSeqformat()==null?"NA":vendor.getSeqformat().getID());
                obj.put("vendorparent", vendor.getParent()==null?"":vendor.getParent().getAcccode());
                obj.put("paymentCriteria", vendor.getPaymentCriteria());
                obj.put("minPriceValueForVendor", vendor.getMinpricevalueforvendor()==null?"":vendor.getMinpricevalueforvendor());
                if(vendor.getPaymentCriteria()==1){
                    obj.put("paymentCriteriaName", "NA");    
                }else if(vendor.getPaymentCriteria()==2){
                    obj.put("paymentCriteriaName", "LIFO");
                }else if(vendor.getPaymentCriteria()==3){
                    obj.put("paymentCriteriaName", "FIFO");
                }else {
                    obj.put("paymentCriteriaName", ""); 
                }
                obj.put("companyRegistrationNumber", vendor.getCompanyRegistrationNumber());
                obj.put("gstRegistrationNumber", vendor.getGstRegistrationNumber());
                obj.put("rmcdApprovalNumber", vendor.getRmcdApprovalNumber());
                obj.put("pricingBandID", vendor.getPricingBandMaster() == null ? "" : vendor.getPricingBandMaster().getID());
                obj.put("pricingBandName", vendor.getPricingBandMaster() == null ? "" : vendor.getPricingBandMaster().getName());
//                obj.put("itno", vendor.getIncomeTaxNo() == null ? "" : vendor.getIncomeTaxNo());
                obj.put("deducteeTypeId", vendor.getDeducteeType() == null ? "" : vendor.getDeducteeType());
                obj.put("deducteeCode", vendor.getDeducteeCode()== null ? "" : vendor.getDeducteeCode());
                obj.put("residentialstatus", vendor.getResidentialstatus());
                obj.put("panStatusId", vendor.getPanStatus() == null ? "" : vendor.getPanStatus());
                String tdsPayableAccount = "";
                if (!StringUtil.isNullOrEmpty(vendor.getNatureOfPayment())) {
                    KwlReturnObject nopresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), vendor.getNatureOfPayment());
                    MasterItem nopObj = (MasterItem) nopresult.getEntityList().get(0);
                    if(nopObj != null){
                        obj.put("natureOfPayment", nopObj.getID());
                        tdsPayableAccount = nopObj.getAccID();
                    } else {
                        obj.put("natureOfPayment", "");
                    }
                } else {
                    obj.put("natureOfPayment", "");
                }
                obj.put("tdsPayableAccount", tdsPayableAccount);
                obj.put("selfBilledFromDate",vendor.getSelfBilledFromDate()!=null?authHandler.getUserDateFormatterWithoutTimeZone(request).format(vendor.getSelfBilledFromDate()):"");
                obj.put("gstVerifiedDate",vendor.getGstVerifiedDate()!=null?authHandler.getDateOnlyFormat().format(vendor.getGstVerifiedDate()):"");
                obj.put("seztodate",vendor.getSezToDate()!=null?authHandler.getDateOnlyFormat().format(vendor.getSezToDate()):"");
                obj.put("sezfromdate",vendor.getSezFromDate()!=null?authHandler.getDateOnlyFormat().format(vendor.getSezFromDate()):"");
                obj.put("selfBilledToDate",vendor.getSelfBilledToDate()!=null?authHandler.getUserDateFormatterWithoutTimeZone(request).format(vendor.getSelfBilledToDate()):"");
                obj.put("iecno", !StringUtil.isNullOrEmpty(vendor.getIECNo())? vendor.getIECNo():"");
                obj.put("commissionerate", !StringUtil.isNullOrEmpty(vendor.getCommissionerate())? vendor.getCommissionerate():"");
                obj.put("defaultnatureofpurchase", !StringUtil.isNullOrEmpty(vendor.getDefaultnatureOfPurchase())? vendor.getDefaultnatureOfPurchase():"");
                obj.put("manufacturertype", !StringUtil.isNullOrEmpty(vendor.getManufacturerType())? vendor.getManufacturerType():"");
                obj.put("division", !StringUtil.isNullOrEmpty(vendor.getDivision())? vendor.getDivision():"");
                obj.put("importereccno", !StringUtil.isNullOrEmpty(vendor.getImporterECCNo())? vendor.getImporterECCNo():"");
                obj.put("range", !StringUtil.isNullOrEmpty(vendor.getRangecode())? vendor.getRangecode():"");
                
                if (company.getCountry() != null && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id) {
                    if (vendor.getTdsInterestPayableAccount() != null) {
                        obj.put("tdsInterestPayableAccount", vendor.getTdsInterestPayableAccount().getID());
                        obj.put("istdsInterestPayableAccountisUsed", accCusVenMapDAOObj.isVendorTDSInterestPayableAccUsedInTrans(vendor.getTdsInterestPayableAccount().getID(), companyid));
                    } else {
                        obj.put("tdsInterestPayableAccount", "");
                        obj.put("istdsInterestPayableAccountisUsed", false);
                    }
                    obj.put("dtaaApplicable", !StringUtil.isNullOrEmpty(vendor.getDTAAApplicable()) ? vendor.getDTAAApplicable() : "");
                    obj.put("dtaaFromDate", vendor.getDTAAFromDate() != null ? authHandler.getDateFormatter(request).format(vendor.getDTAAFromDate()) : "");
                    obj.put("dtaaToDate", vendor.getDTAAToDate() != null ? authHandler.getDateFormatter(request).format(vendor.getDTAAToDate()) : "");
                    obj.put("dtaaSpecialRate", vendor.getDTAASpecialRate());

                    obj.put("higherTDSRate", vendor.getHigherTDSRate());
                    obj.put("lowerRate", vendor.getLowerRate());
                    obj.put("nonLowerDedutionApplicable", !StringUtil.isNullOrEmpty(vendor.getNonLowerDedutionApplicable()) ? vendor.getNonLowerDedutionApplicable() : "");
                    obj.put("deductionReason", !StringUtil.isNullOrEmpty(vendor.getDeductionReason()) ? vendor.getDeductionReason() : "");
                    obj.put("certificateNo", !StringUtil.isNullOrEmpty(vendor.getCertificateNo()) ? vendor.getCertificateNo() : "");
                    obj.put("deductionFromDate", vendor.getDeductionFromDate() != null ? authHandler.getDateFormatter(request).format(vendor.getDeductionFromDate()) : "");
                    obj.put("deductionToDate", vendor.getDeductionToDate() != null ? authHandler.getDateFormatter(request).format(vendor.getDeductionToDate()) : "");
                    obj.put("referenceNumberNo", !StringUtil.isNullOrEmpty(vendor.getDeclareRefNo()) ? vendor.getDeclareRefNo() : "");
                }
                if (vendor.isMapcustomervendor()) {
                    CustomerVendorMapping customervendormapping = accCusVenMapDAOObj.checkVendorMappingExists(vendor.getID());
                    if (customervendormapping != null) {
                        obj.put("mappingcusaccid", customervendormapping.getCustomeraccountid().getAccount().getID());
                        obj.put("customercode", customervendormapping.getCustomeraccountid().getAcccode());
                        obj.put("customeraccountname", customervendormapping.getCustomeraccountid().getAccount().getName());
                    }
                }
             
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("vendorid", vendor.getID());       
                addrRequestParams.put("companyid", companyid);       
                KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                if(!addressResult.getEntityList().isEmpty()){
                    JSONArray addrArray=new JSONArray();
                    List <VendorAddressDetails> vendAddrList=addressResult.getEntityList();
                    for(VendorAddressDetails vendAddr:vendAddrList){
                        JSONObject addrObject=new JSONObject();
                         addrObject.put("aliasName", vendAddr.getAliasName()!=null?vendAddr.getAliasName():"");             
                         addrObject.put("address", vendAddr.getAddress()!=null?vendAddr.getAddress():"");  
                         addrObject.put("id", vendAddr.getID()!=null?vendAddr.getID():"");       
                         addrObject.put("county", vendAddr.getCounty()!=null?vendAddr.getCounty():"");       
                         addrObject.put("city", vendAddr.getCity()!=null?vendAddr.getCity():"");       
                         addrObject.put("state", vendAddr.getState()!=null?vendAddr.getState():"");       
                         addrObject.put("stateCode", vendAddr.getStateCode()!=null?vendAddr.getStateCode():"");       
                         addrObject.put("country", vendAddr.getCountry()!=null?vendAddr.getCountry():"");       
                         addrObject.put("postalCode", vendAddr.getPostalCode()!=null?vendAddr.getPostalCode():"");       
                         addrObject.put("phone", vendAddr.getPhone()!=null?vendAddr.getPhone():"");       
                         addrObject.put("mobileNumber", vendAddr.getMobileNumber()!=null?vendAddr.getMobileNumber():"");       
                         addrObject.put("fax", vendAddr.getFax()!=null?vendAddr.getFax():"");       
                         addrObject.put("emailID", vendAddr.getEmailID()!=null?vendAddr.getEmailID():"");       
                         addrObject.put("recipientName", vendAddr.getRecipientName()!=null?vendAddr.getRecipientName():"");       
                         addrObject.put("contactPerson", vendAddr.getContactPerson()!=null?vendAddr.getContactPerson():"");       
                         addrObject.put("contactPersonNumber", vendAddr.getContactPersonNumber()!=null?vendAddr.getContactPersonNumber():"");       
                         addrObject.put("contactPersonDesignation", vendAddr.getContactPersonDesignation()!=null?vendAddr.getContactPersonDesignation():"");  
                         addrObject.put("website", vendAddr.getWebsite()!=null?vendAddr.getWebsite():"");
                         addrObject.put("isDefaultAddress", vendAddr.isIsDefaultAddress());  
                         addrObject.put("isBillingAddress", vendAddr.isIsBillingAddress());  
                         addrArray.put(addrObject);
                         
                         //below address details used in exporting vendor with all column option
                         //below key is set in column "dataindex" of default header table. so please do not chanage them.    
                        if (vendAddr.isIsDefaultAddress() && vendAddr.isIsBillingAddress()) {//for defult billing address
                            obj.put("billingAliasName", vendAddr.getAliasName() != null ? vendAddr.getAliasName() : "");
                            obj.put("billingAddress", vendAddr.getAddress() != null ? vendAddr.getAddress() : "");
                            obj.put("billingCounty", vendAddr.getCounty() != null ? vendAddr.getCounty() : "");
                            obj.put("billingCity", vendAddr.getCity() != null ? vendAddr.getCity() : "");
                            obj.put("billingState", vendAddr.getState() != null ? vendAddr.getState() : "");
                            obj.put("billingStateCode", vendAddr.getStateCode()!=null?vendAddr.getStateCode():"");
                            obj.put("billingCountry", vendAddr.getCountry() != null ? vendAddr.getCountry() : "");
                            obj.put("billingPostalCode", vendAddr.getPostalCode() != null ? vendAddr.getPostalCode() : "");
                            obj.put("billingPhone", vendAddr.getPhone() != null ? vendAddr.getPhone() : "");
                            obj.put("billingMobileNumber", vendAddr.getMobileNumber() != null ? vendAddr.getMobileNumber() : "");
                            obj.put("billingFax", vendAddr.getFax() != null ? vendAddr.getFax() : "");
                            obj.put("billingEmailID", vendAddr.getEmailID() != null ? vendAddr.getEmailID() : "");
                            if (!StringUtil.isNullOrEmpty(vendAddr.getEmailID())) {
                                obj.put("email", vendAddr.getEmailID().replace(',', ';'));
                            } else {
                                obj.put("email", "");
                            }
                            obj.put("billingRecipientName", vendAddr.getRecipientName() != null ? vendAddr.getRecipientName() : "");
                            obj.put("billingContactPerson", vendAddr.getContactPerson() != null ? vendAddr.getContactPerson() : "");
                            obj.put("billingContactPersonNumber", vendAddr.getContactPersonNumber() != null ? vendAddr.getContactPersonNumber() : "");         
                            obj.put("billingContactPersonDesignation", vendAddr.getContactPersonDesignation() != null ? vendAddr.getContactPersonDesignation() : "");         
                            obj.put("billingWebsite", vendAddr.getWebsite() != null ? vendAddr.getWebsite() : "");         
                        } else if (vendAddr.isIsDefaultAddress()) { //for defult shipping address
                            obj.put("shippingAliasName", vendAddr.getAliasName() != null ? vendAddr.getAliasName() : "");
                            obj.put("shippingAddress", vendAddr.getAddress() != null ? vendAddr.getAddress() : "");
                            obj.put("shippingCounty", vendAddr.getCounty() != null ? vendAddr.getCounty() : "");
                            obj.put("shippingCity", vendAddr.getCity() != null ? vendAddr.getCity() : "");
                            obj.put("shippingState", vendAddr.getState() != null ? vendAddr.getState() : "");
                            obj.put("shippingStateCode", vendAddr.getStateCode()!=null?vendAddr.getStateCode():"");
                            obj.put("shippingCountry", vendAddr.getCountry() != null ? vendAddr.getCountry() : "");
                            obj.put("shippingPostalCode", vendAddr.getPostalCode() != null ? vendAddr.getPostalCode() : "");
                            obj.put("shippingPhone", vendAddr.getPhone() != null ? vendAddr.getPhone() : "");
                            obj.put("shippingMobileNumber", vendAddr.getMobileNumber() != null ? vendAddr.getMobileNumber() : "");
                            obj.put("shippingFax", vendAddr.getFax() != null ? vendAddr.getFax() : "");
                            obj.put("shippingEmailID", vendAddr.getEmailID() != null ? vendAddr.getEmailID() : "");
                            obj.put("shippingRecipientName", vendAddr.getRecipientName() != null ? vendAddr.getRecipientName() : "");
                            obj.put("shippingContactPerson", vendAddr.getContactPerson() != null ? vendAddr.getContactPerson() : "");
                            obj.put("shippingContactPersonNumber", vendAddr.getContactPersonNumber() != null ? vendAddr.getContactPersonNumber() : "");
                            obj.put("shippingContactPersonDesignation", vendAddr.getContactPersonDesignation() != null ? vendAddr.getContactPersonDesignation() : "");
                            obj.put("shippingWebsite", vendAddr.getWebsite() != null ? vendAddr.getWebsite() : "");
                        }
                    }
                    obj.put("addressDetails", addrArray);
                }   
                obj.put("productcode", productsCode);
                obj.put("prodname",productName);
                obj.put("productid",tempstring);
                    /*
                     * Putting Custom Field and Dimension values in Json
                     */
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(VendorCustomData.class.getName(), vendor.getID());
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        VendorCustomData jeDetailCustom = (VendorCustomData) custumObjresult.getEntityList().get(0);
                        if (jeDetailCustom != null) {
                            AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            boolean isExport = (boolean) ((request.getAttribute(Constants.isExport) == null) ? false : request.getAttribute(Constants.isExport));
                            params.put(Constants.isExport, isExport);
                            accountingCommonfieldDataManage.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        }
                }              
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getVendorJson : "+ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    public ModelAndView deleteVendorAdressDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        Map<String, Object> auditRequestParams = new HashMap<String, Object>();
        String msg = "";
        String auditID = AuditAction.VENDOR_UPDATED;
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
                String rowid =  StringUtil.DecodeText(obj.optString("rowid"));
                 aliasName +=  StringUtil.DecodeText(obj.optString("aliasName"))+",";
                if(!StringUtil.isNullOrEmpty(rowid)){
                    KwlReturnObject custAddrobject = accountingHandlerDAOobj.deleteVendorAddressByID(rowid, companyid);
                }
            }
            aliasName=aliasName.substring(0,aliasName.length()-1);
            txnManager.commit(status);
            issuccess = true;
           
            msg = messageSource.getMessage("acc.field.addressdeletedsuccessfully", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + ".";//+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+level

            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.getString(Constants.userfullname) + " has " + auditMsg + " [" + aliasName + "] of Vendor " + paramJobj.optString("accountname") + " ( " + paramJobj.optString("accountcode") + " ) ", auditRequestParams, paramJobj.optString("accountid"));
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "Error while deleting Address." ;//+ ex.getMessage()
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public String[] getVendorCategoryIDs(String vendorid) {
        JSONObject jobj = new JSONObject();
        String[] valuesStr = {"",""};
        boolean issuccess = false;
        try {
            KwlReturnObject result = accVendorDAOobj.getVendorCategoryIDs(vendorid);
            
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            
            while (itr.hasNext()) {
                VendorCategoryMapping row = (VendorCategoryMapping) itr.next();
                MasterItem masterItemObj= row.getVendorCategory();
                if(itr.hasNext()) {
                    valuesStr[0] += masterItemObj.getID() + ",";
                    valuesStr[1] += masterItemObj.getValue() + ",";
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
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "");
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return valuesStr;
    }
    
    public ModelAndView getVendorsByCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            boolean isPricingBandGrouping = request.getParameter("isPricingBandGrouping") != null ? Boolean.parseBoolean(request.getParameter("isPricingBandGrouping")) : false;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("isPricingBandGrouping", isPricingBandGrouping);

            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("categoryid", request.getParameter("categoryid"));

            KwlReturnObject result = accVendorDAOobj.getNewVendorList(requestParams);
            JSONArray jArr= getCustomersByCategoryJson(request, result.getEntityList());

            jobj.put(Constants.RES_data, jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
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
                String Vendid = row[0].toString();
                String CategoryId = row[1] != null ? row[1].toString() : "";
                KwlReturnObject vendresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), Vendid);
                Vendor vendor = (Vendor) vendresult.getEntityList().get(0);
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
                
                Account account = vendor.getAccount();

                if (account.isDeleted()) {
//                    continue;
                }
                JSONObject obj = new JSONObject();
                obj.put("accid", vendor.getID());
                obj.put("accname", vendor.getName());
                obj.put("acccode",(StringUtil.isNullOrEmpty(vendor.getAcccode()))?"":vendor.getAcccode());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                
                // calculation of opening balance

                double openbalance = accInvoiceCMNobj.getOpeningBalanceOfAccount(request, null, true, vendor.getID());

                obj.put("openbalance", openbalance);
                
//                obj.put("openbalance", account.getOpeningBalance());

                obj.put("currencyid", (account.getCurrency() == null ? "" : account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (account.getCurrency() == null ? "" : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? "" : account.getCurrency().getName()));

                obj.put("id", vendor.getID());
                obj.put("title", vendor.getTitle());
                obj.put("contactno2", vendor.getAltContactNumber());
                obj.put("pdm", vendor.getPreferedDeliveryMode());
                obj.put("termname", (vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermname()));
                obj.put("termdays", (vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getTermdays()));
                obj.put("termid", (vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getID()));
                obj.put("bankaccountno", vendor.getBankaccountno());
                obj.put("other", vendor.getOther());
                if (vendor.isActivate()) {
                    obj.put("isactivate", "Active");
                } else {
                    obj.put("isactivate", "Dormant");
                }
                if (!StringUtil.isNullObject(vendor.getCreatedOn())) {
                    obj.put("creationDate", authHandler.getDateOnlyFormat(request).format(vendor.getCreatedOn()));
                } else {
                    obj.put("creationDate", "");
                }
                
                if (!StringUtil.isNullObject(vendor.getGstVerifiedDate())) {
                    obj.put("gstVerifiedDate", authHandler.getDateFormatter(request).format(vendor.getGstVerifiedDate()));
                } else {
                    obj.put("gstVerifiedDate", "");
                }

                obj.put("deleted", vendor.getAccount().isDeleted());
                /*
                Added to show Agent in vender list by customer 
                */
                Set<VendorAgentMapping> vaSet = vendor.getAgent();
                String agentsMappedTVendor = "";
                String agentsMappedTVendorName = "";//ERP-19693
                for (VendorAgentMapping vgmObj : vaSet) {
                    if(!StringUtil.isNullObject(vgmObj.getAgent())){
                    agentsMappedTVendor += vgmObj.getAgent().getID() + ",";
                    agentsMappedTVendorName += vgmObj.getAgent().getValue() + ",";//ERP-19693
                }
                }
                if (!StringUtil.isNullOrEmpty(agentsMappedTVendor)) {
                    agentsMappedTVendor = agentsMappedTVendor.substring(0, (agentsMappedTVendor.length() - 1));
                    agentsMappedTVendorName = agentsMappedTVendorName.substring(0, (agentsMappedTVendorName.length() - 1));//ERP-19693
                }
                obj.put("agentsmappedwithvendor", agentsMappedTVendor);
                obj.put("salesPersonAgent", agentsMappedTVendorName);
                if(isBySalesPersonOrAgent){
                    obj.put("salesPersonAgentId", masterItem == null ? "" : masterItem.getID());
                    obj.put("salesPersonAgent", masterItem == null ? "" : masterItem.getValue());
                }else{
                    if (isPricingBandGrouping) {
                        obj.put("pricingBandID", pricingBandMaster == null ? "" : pricingBandMaster.getID());
                        obj.put("pricingBand", pricingBandMaster == null ? "" : pricingBandMaster.getName());
                    } else {
                        obj.put("categoryid", masterItem == null ? "" : masterItem.getID());
                        obj.put("category", masterItem == null ? "" : masterItem.getValue());
                    } 
                }
                                
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("vendorid", vendor.getID());       
                addrRequestParams.put("companyid", companyid);       
                KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                if(!addressResult.getEntityList().isEmpty()){
                    JSONArray addrArray=new JSONArray();
                    List <VendorAddressDetails> vendAddrList=addressResult.getEntityList();
                    for(VendorAddressDetails vendAddr:vendAddrList){
                        JSONObject addrObject=new JSONObject();
                         addrObject.put("aliasName", vendAddr.getAliasName()!=null?vendAddr.getAliasName():"");             
                         addrObject.put("address", vendAddr.getAddress()!=null?vendAddr.getAddress():"");       
                         addrObject.put("city", vendAddr.getCity()!=null?vendAddr.getCity():"");       
                         addrObject.put("state", vendAddr.getState()!=null?vendAddr.getState():"");       
                         addrObject.put("country", vendAddr.getCountry()!=null?vendAddr.getCountry():"");       
                         addrObject.put("postalCode", vendAddr.getPostalCode()!=null?vendAddr.getPostalCode():"");       
                         addrObject.put("phone", vendAddr.getPhone()!=null?vendAddr.getPhone():"");       
                         addrObject.put("mobileNumber", vendAddr.getMobileNumber()!=null?vendAddr.getMobileNumber():"");       
                         addrObject.put("fax", vendAddr.getFax()!=null?vendAddr.getFax():"");       
                         addrObject.put("emailID", vendAddr.getEmailID()!=null?vendAddr.getEmailID():"");       
                         addrObject.put("contactPerson", vendAddr.getContactPerson()!=null?vendAddr.getContactPerson():"");       
                         addrObject.put("contactPersonNumber", vendAddr.getContactPersonNumber()!=null?vendAddr.getContactPersonNumber():"");       
                         addrObject.put("contactPersonDesignation", vendAddr.getContactPersonDesignation()!=null?vendAddr.getContactPersonDesignation():"");  
                         addrObject.put("website", vendAddr.getWebsite()!=null?vendAddr.getWebsite():"");
                         addrObject.put("isDefaultAddress", vendAddr.isIsDefaultAddress());  
                         addrObject.put("isBillingAddress", vendAddr.isIsBillingAddress());  
                         addrArray.put(addrObject);
                         
                         //below address details used in exporting vendor with all column option
                         //below key is set in column "dataindex" of default header table. so please do not chanage them.    
                        if (vendAddr.isIsDefaultAddress() && vendAddr.isIsBillingAddress()) {//for defult billing address
                            obj.put("billingAliasName", vendAddr.getAliasName() != null ? vendAddr.getAliasName() : "");
                            obj.put("billingAddress", vendAddr.getAddress() != null ? vendAddr.getAddress() : "");
                            obj.put("billingCity", vendAddr.getCity() != null ? vendAddr.getCity() : "");
                            obj.put("billingState", vendAddr.getState() != null ? vendAddr.getState() : "");
                            obj.put("billingCountry", vendAddr.getCountry() != null ? vendAddr.getCountry() : "");
                            obj.put("billingPostalCode", vendAddr.getPostalCode() != null ? vendAddr.getPostalCode() : "");
                            obj.put("billingPhone", vendAddr.getPhone() != null ? vendAddr.getPhone() : "");
                            obj.put("billingMobileNumber", vendAddr.getMobileNumber() != null ? vendAddr.getMobileNumber() : "");
                            obj.put("billingFax", vendAddr.getFax() != null ? vendAddr.getFax() : "");
                            obj.put("billingEmailID", vendAddr.getEmailID() != null ? vendAddr.getEmailID() : "");
                            obj.put("billingContactPerson", vendAddr.getContactPerson() != null ? vendAddr.getContactPerson() : "");
                            obj.put("billingContactPersonNumber", vendAddr.getContactPersonNumber() != null ? vendAddr.getContactPersonNumber() : "");         
                            obj.put("billingContactPersonDesignation", vendAddr.getContactPersonDesignation() != null ? vendAddr.getContactPersonDesignation() : "");  
                            obj.put("billingWebsite", vendAddr.getWebsite() != null ? vendAddr.getWebsite() : ""); 
                        } else if (vendAddr.isIsDefaultAddress()) { //for defult shipping address
                            obj.put("shippingAliasName", vendAddr.getAliasName() != null ? vendAddr.getAliasName() : "");
                            obj.put("shippingAddress", vendAddr.getAddress() != null ? vendAddr.getAddress() : "");
                            obj.put("shippingCity", vendAddr.getCity() != null ? vendAddr.getCity() : "");
                            obj.put("shippingState", vendAddr.getState() != null ? vendAddr.getState() : "");
                            obj.put("shippingCountry", vendAddr.getCountry() != null ? vendAddr.getCountry() : "");
                            obj.put("shippingPostalCode", vendAddr.getPostalCode() != null ? vendAddr.getPostalCode() : "");
                            obj.put("shippingPhone", vendAddr.getPhone() != null ? vendAddr.getPhone() : "");
                            obj.put("shippingMobileNumber", vendAddr.getMobileNumber() != null ? vendAddr.getMobileNumber() : "");
                            obj.put("shippingFax", vendAddr.getFax() != null ? vendAddr.getFax() : "");
                            obj.put("shippingEmailID", vendAddr.getEmailID() != null ? vendAddr.getEmailID() : "");
                            obj.put("shippingContactPerson", vendAddr.getContactPerson() != null ? vendAddr.getContactPerson() : "");
                            obj.put("shippingContactPersonNumber", vendAddr.getContactPersonNumber() != null ? vendAddr.getContactPersonNumber() : "");
                            obj.put("shippingContactPersonDesignation", vendAddr.getContactPersonDesignation() != null ? vendAddr.getContactPersonDesignation() : "");
                            obj.put("shippingWebsite", vendAddr.getWebsite() != null ? vendAddr.getWebsite() : ""); 
                        }
                    }
                    obj.put("addressDetails", addrArray);
                }
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCustomersByCategoryJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getVendorsByAgent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("salesPersonAgentId", request.getParameter("salesPersonAgentId"));
            requestParams.put("isBySalesPersonOrAgent", request.getParameter("isBySalesPersonOrAgent"));

            KwlReturnObject result = accVendorDAOobj.getNewVendorList(requestParams);
            JSONArray jArr= getCustomersByCategoryJson(request, result.getEntityList());

            jobj.put(Constants.RES_data, jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * @author neeraj
     * @param jArr
     * @param request
     * @return
     */
    public JSONArray getVendorAmountDue(JSONArray jArr, HttpServletRequest request){
    	try{
    		HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);

//                HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
//                ArrayList filter_names = new ArrayList(),filter_params = new ArrayList();
                String currencyid = sessionHandlerImpl.getCurrencyID(request);
                KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
                KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
                String companyId = sessionHandlerImpl.getCompanyid(request);
    		requestParams.put("nondeleted", "true");
    		requestParams.put("deleted", "false");
                requestParams.put("includeFixedAssetInvoicesFlag",true);
                
                //Flag used to run optimized code of getVendorAmountDue only when get called from getVendors
                boolean allVendors = false;
                Map<String, Double> vendorGRAmtMap = null;
                if(request.getAttribute("allvendors") != null) {
                    allVendors = (Boolean) request.getAttribute("allvendors");
                }
                if(allVendors) {
                    vendorGRAmtMap = accVendorDAOobj.getVendorGRAmtMap("F", companyId);
                    requestParams.put("onlyexpenseinv", "true");
                }
    		for(int i = 0; i < jArr.length(); i++){
                    String accid = jArr.getJSONObject(i).getString("accid");
                    String vendorId=jArr.getJSONObject(i).optString("custId");
                    /*
                     * Added this parameter because many of the traansactions use this parameter
                     * for seaching the records usgin this parameter as Vendor Id / Customer Id
                     */
                    requestParams.put("custVendorID",accid);      
                    requestParams.put(InvoiceConstants.accid, accid);   
                    if (StringUtil.isNullOrEmpty(vendorId)) {
                        requestParams.put(InvoiceConstants.vendorid, accid);
                    } else {
                        requestParams.put(InvoiceConstants.vendorid, vendorId);
                    }
                    requestParams.put(Constants.ss, null);                    
                    double amountdue = 0;
                    Boolean isNotExpenseType = false;
                    double newBaseAmount = 0;
                    double baseamountdue = 0;
                    double baseamountduenew = 0;
                    KwlReturnObject result = accGoodsReceiptDAOobj.getGoodsReceipts(requestParams);
                    if(result.getEntityList() != null && result.getEntityList().size() > 0) {
                        Iterator itr = result.getEntityList().iterator();
                        while (itr.hasNext()) {
                                GoodsReceipt goodsReceipt = (GoodsReceipt) itr.next();
                                if (goodsReceipt.isIsExpenseType()) {
                                    List ll = accGoodsReceiptCMNobj.getExpGRAmountDue(requestParams, goodsReceipt);
                                    amountdue = amountdue + (Double) ll.get(1);
                                    newBaseAmount = (Double) ll.get(1);
                                } else {
                                    isNotExpenseType = true;
                                    if(!allVendors) {
                                        if (Constants.InvoiceAmountDueFlag) {
                                            List ll = accGoodsReceiptCMNobj.getInvoiceDiscountAmountInfo(requestParams, goodsReceipt);
                                            amountdue = amountdue + (Double) ll.get(1);
                                            newBaseAmount = (Double) ll.get(1);
                                        } else {
                                            List ll = accGoodsReceiptCMNobj.getGRAmountDue(requestParams, goodsReceipt);
                                            amountdue = amountdue + (Double) ll.get(1);
                                            newBaseAmount = (Double) ll.get(1);
                                        }
                                    }
                                }
                            
                            String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
                            String tocurrencyid = goodsReceipt.getCompany().getCurrency().getCurrencyID();                            
//                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                            baseamountduenew = authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                            baseamountdue = baseamountdue + baseamountduenew;
                    
                        }
                        jArr.getJSONObject(i).put("amountdue", baseamountdue);
                    }else{
                        isNotExpenseType = true;
                        jArr.getJSONObject(i).put("amountdue", 0);
                    }
                    requestParams.put("cntype", null);//cntype after each loop become 4 so need to update with null
                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);//This is used for getting DN gainst vendor and otherwise 
                    if(result.getEntityList()!=null){
                         Iterator itr = result.getEntityList().iterator();
                         while(itr.hasNext()){
                             Object[] row = (Object[]) itr.next();
                             boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                             if(!withoutinventory){
                                 KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);                         
                                 DebitNote debitMemo = (DebitNote) resultObject.getEntityList().get(0);
                                 if (debitMemo.isOtherwise() && debitMemo.getDnamountdue()>0) {
                                     double dnamountdue =debitMemo.isOtherwise() ? debitMemo.getDnamountdue() : 0;
                                     double exchangerate=debitMemo.getJournalEntry()!=null?debitMemo.getJournalEntry().getExternalCurrencyRate():0;
//                                     Date creationdate=debitMemo.getJournalEntry().getEntryDate();
                                     Date creationdate=debitMemo.getCreationDate();
                                     String dncurrencyid=debitMemo.getCurrency().getCurrencyID();
                                     KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, dnamountdue, dncurrencyid, creationdate, exchangerate);
                                     double dnamountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                                     baseamountdue-=dnamountdueinbase;//In case of debit note amount is subtracted
                                 }
                             }
                         }
                    }                  
                    requestParams.put("cntype", 4);//This is used for getting Credit note against vendor 
                    result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                    if(result.getEntityList()!=null){
                         Iterator itr = result.getEntityList().iterator();
                         while(itr.hasNext()){
                             Object[] row = (Object[]) itr.next();
                             boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                             if(!withoutinventory){
                                 KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);                         
                                 CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
                                 if (creditNote.isOtherwise() && creditNote.getCnamountdue()>0) {
                                     double cnamountdue =creditNote.isOtherwise() ? creditNote.getCnamountdue() : 0;
                                     double exchangerate=creditNote.getJournalEntry()!=null?creditNote.getJournalEntry().getExternalCurrencyRate():0;
//                                     Date creationdate=creditNote.getJournalEntry().getEntryDate();
                                     Date creationdate=creditNote.getCreationDate();
                                     String cncurrencyid=creditNote.getCurrency().getCurrencyID();
                                     KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnamountdue, cncurrencyid, creationdate, exchangerate);
                                     double cnamountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                                     baseamountdue+=cnamountdueinbase;//In case of Credit note amount is Added
                                 }
                             }
                         }
                    }            
                    
                    //TO DO- when we allow partial receive payment then we need to change the logic for advance used
                    
                    result = accVendorPaymentobj.getPayments(requestParams);
                    if (result.getEntityList() != null) {
                        Iterator itr = result.getEntityList().iterator();
                        while (itr.hasNext()) {
                            Object[] row = (Object[]) itr.next();
                            Payment payment = (Payment) row[0];
                            String mpcurrencyid = payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID();
                            if (payment != null) {
                                if (payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty() && payment.getVendor() != null) {
                                    for (AdvanceDetail advanceDetail : payment.getAdvanceDetails()) {
//                                        baseamountdue -= authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), mpcurrencyid, payment.getJournalEntry().getEntryDate(), payment.getJournalEntry().getExternalCurrencyRate()).getEntityList().get(0), companyId);
                                        baseamountdue -= authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), mpcurrencyid, payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate()).getEntityList().get(0), companyId);
                                    }
                                }
                            }
                        }
                    }
                    
                    requestParams.put("isadvancepayment", false);//Only advance payment from vendor required so it flase and other is true
                    requestParams.put("isadvancefromvendor", true);
                    result = accReceiptDAOobj.getReceipts(requestParams); 
                    if (result.getEntityList() != null) {
                        Iterator itr = result.getEntityList().iterator();
                        while (itr.hasNext()) {
                            Object[] row = (Object[]) itr.next();
                            Receipt receipt = (Receipt) row[0];

                            if (receipt.isIsadvancefromvendor() && receipt.getDepositAmount()>0 ) {
                                double receiptamountdue = receipt.getDepositAmount();
                                double exchangerate = receipt.getJournalEntry() != null ? receipt.getJournalEntry().getExternalCurrencyRate() : 0;
//                                Date creationdate = receipt.getJournalEntry().getEntryDate();
                                Date creationdate = receipt.getCreationDate();
                                String cncurrencyid = receipt.getCurrency().getCurrencyID();
                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receiptamountdue, cncurrencyid, creationdate, exchangerate);
                                double receiptamountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                                baseamountdue += receiptamountdueinbase; //In case of Receive Paymeny amount is added
                            }
                        }
                    }
                    
                    //ERP-10304 : calculating refund received from vendor
                    result = accReceiptDAOobj.getReceiptsForVendor(accid,companyId);
                    if (result.getEntityList() != null) {
                        Iterator itr = result.getEntityList().iterator();
                        while (itr.hasNext()) {                            
                            Receipt receipt = (Receipt) itr.next();
                            Set <ReceiptAdvanceDetail> advDetail = receipt.getReceiptAdvanceDetails();
                            for(ReceiptAdvanceDetail adv:advDetail){
                                if(StringUtil.isNullOrEmpty(adv.getAdvancedetailid())){
//                                    baseamountdue+=authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, adv.getAmountDue(), receipt.getCurrency().getCurrencyID(), receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate()).getEntityList().get(0), companyId);
                                    baseamountdue+=authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, adv.getAmountDue(), receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate()).getEntityList().get(0), companyId);
                                }
                            }
                        }
                    }
                        
                    // putting opening amount of customer
                    double openbalanceAmountOfVendor = accInvoiceCMNobj.getOpeningBalanceAmountDueOfAccount(request,null,true,accid);
                    baseamountdue = baseamountdue+openbalanceAmountOfVendor;
                    
                    if(allVendors && vendorGRAmtMap != null && isNotExpenseType) {
                        if(vendorGRAmtMap.containsKey(vendorId)){
                            baseamountdue +=vendorGRAmtMap.get(vendorId);
                        } else if(vendorGRAmtMap.containsKey(accid)) {
                            baseamountdue +=vendorGRAmtMap.get(accid);
                        }
                    }
                    
                    jArr.getJSONObject(i).put("amountdue", baseamountdue);
                }

    	}catch (Exception ex){
    		Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
    	}
    	return jArr;
    }

    public ModelAndView exportVendor(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            HashMap<String, Object> requestParams = accVendorController.getVendorRequestMap(request);
            KwlReturnObject result = accVendorDAOobj.getVendor(requestParams);
            HashMap<String, Object> requestParamsproduct = productHandler.getProductRequestMap(request);
            boolean quickSearchFlag = false;
            request.setAttribute(Constants.isExport, true);
            if(requestParams.containsKey("ss") && requestParams.get("ss") != null && (!StringUtil.isNullOrEmpty(requestParams.get("ss").toString()))) {
                quickSearchFlag = true;
            }
            ArrayList list = accVendorDAOobj.getVendorArrayList(result.getEntityList(), requestParams, quickSearchFlag, false);
            JSONArray jArr = getVendorJson(request, list);
            jobj.put(Constants.RES_data, jArr);
            jArr = getVendorAmountDue(jArr, request);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public ModelAndView exportVendorListByAgent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("salesPersonAgentId", request.getParameter("salesPersonAgentId"));
            requestParams.put("isBySalesPersonOrAgent", request.getParameter("isBySalesPersonOrAgent"));
            requestParams.put("categoryid", !StringUtil.isNullOrEmpty(request.getParameter("categoryid")) ? request.getParameter("categoryid") :"");
            
            KwlReturnObject result = accVendorDAOobj.getNewVendorList(requestParams);
            JSONArray jArr= getCustomersByCategoryJson(request, result.getEntityList());

            jobj.put(Constants.RES_data, jArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView getVendorExceedingDebitLimit(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            jobj = getVendorExceedingDebitLimit(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

        public JSONObject getVendorExceedingDebitLimit(HttpServletRequest request) throws ServiceException, SessionExpiredException {

        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            Vendor vendor=null;
            if(request.getParameter("vendor")!=null && request.getParameter("totalSUM")!=null)
            {
                KwlReturnObject cstresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter("vendor"));
                vendor = (Vendor) cstresult.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                obj.put("accid", (vendor.getAccount()==null)?"":vendor.getAccount().getID());
                obj.put("custId", vendor.getID());
                obj.put("custName", vendor.getName());
                jArr.put(obj);

                double totalAmountDue=0;
                JSONArray vendorAmountDueJArr = getVendorAmountDue(jArr, request);
                double amountDue=vendorAmountDueJArr.getJSONObject(0).getDouble("amountdue");

                double totalSUM = 0;
                KwlReturnObject venresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), vendor.getCompany().getCompanyID());
                if (venresult.getEntityList().get(0) != null) {
                    /*
                     Include amount enter in form in the limit calculation based on settings
                     */
                    ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) venresult.getEntityList().get(0);
                    boolean isOrder = !StringUtil.isNullOrEmpty(request.getParameter("isOrder")) ? Boolean.parseBoolean(request.getParameter("isOrder")) : true;
                    if (extraCompanyPreferences != null) {
                        if ((isOrder && extraCompanyPreferences.isIncludeAmountInLimitPO()) || (!isOrder && extraCompanyPreferences.isIncludeAmountInLimitPI())) {
                            totalSUM = Double.parseDouble(request.getParameter("totalSUM"));
                        }
                    }
                } else {
                    totalSUM = Double.parseDouble(request.getParameter("totalSUM"));
                }

                
                totalAmountDue=amountDue+totalSUM;
                double fixedDebitLimit=vendor.getDebitlimit();

                if(totalAmountDue > fixedDebitLimit){
                    JSONObject jTemp = new JSONObject();
                    jTemp.put("name", vendor.getName());
                    jTemp.put("amountDue",amountDue);
                    jTemp.put("limitflag","true");
                    jTemp.put("limit", vendor.getDebitlimit());
                    jobj.append(Constants.RES_data, jTemp);
                }
            }
        }catch (JSONException ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountsExceedingBudget : " + ex.getMessage(), ex);
        }
        return jobj;
    }
        public ModelAndView getCurrencyInfo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        
        String msg="";
        boolean issuccess = true;
		try {
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            
            
            String vendorid = request.getParameter(Constants.customerid);
            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorid);
            Vendor vendor = (Vendor) venresult.getEntityList().get(0);
            
            JSONArray jArr = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("accid", vendor.getID());
            obj.put("custId", vendor.getID());
            obj.put("custName", vendor.getName());
            jArr.put(obj);
            JSONArray vendorAmountDueJArr = getVendorAmountDue(jArr, request);
            double amountDue=vendorAmountDueJArr.getJSONObject(0).getDouble("amountdue");
            
            boolean isBilling = (request.getParameter("isBilling") == null)? false : Boolean.parseBoolean(request.getParameter("isBilling"));
            
            KwlReturnObject resultTrans = accVendorDAOobj.getLastTransactionVendor(vendor.getID(), isBilling);
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
            jobj.put("amountdue",amountDue);
            jobj.put("currencyid",vendor.getCurrency()==null?currency.getCurrencyID():vendor.getCurrency().getCurrencyID());
            jobj.put("currencysymbol",vendor.getCurrency()==null?currency.getSymbol():vendor.getCurrency().getSymbol());
            jobj.put("currencyname",vendor.getCurrency()==null?currency.getName():vendor.getCurrency().getName());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg="accVendorController.getAddress : "+ex;
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

       public ModelAndView getVendorProductsMappingFunction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                requestParams.put("ss", "");
            }
            boolean quickSearchFlag = false;
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null && (!StringUtil.isNullOrEmpty(requestParams.get("ss").toString()))) {
                quickSearchFlag = true;
            }
            String ss = request.getParameter("ss");
            JSONObject DataJArr = getProductVendorMappingJsonFunction(request, ss);
            int totalCount=DataJArr.getInt("count");
            JSONArray JArr=DataJArr.getJSONArray(Constants.RES_data);
            jobj.put(Constants.RES_data, JArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accVendorControllerCMN.getVendorProductsMappingFunction : " + ex.getMessage();
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
    public ModelAndView getVendorProductsMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            KwlReturnObject resultvendor = accVendorDAOobj.getVendor(requestParams); 
            if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                requestParams.put("ss", "");
            }
            KwlReturnObject resultproduct = accProductObj.getProducts(requestParams);

            List listproduct = resultproduct.getEntityList();
            int count = listproduct.size();
            List listvendor = resultvendor.getEntityList();
            List pagingList = listproduct;
            String ss = request.getParameter("ss");
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagingList = StringUtil.getPagedList(listproduct, Integer.parseInt(start), Integer.parseInt(limit));
            }

            JSONArray DataJArr = getProductVendorMappingJson(request, listproduct, listvendor, ss);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put("totalCount", resultproduct.getRecordTotalCount());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accVendorControllerCMN.getVendorProductsMapping : " + ex.getMessage();
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
    
//Getting the vendor mapped product json-NeerajD
    
    
    public JSONObject getProductVendorMappingJsonFunction(HttpServletRequest request, String ss) throws SessionExpiredException, ServiceException, JSONException, UnsupportedEncodingException {
        JSONArray jArr = new JSONArray();
        JSONObject jObj=new JSONObject();
        Producttype producttype = new Producttype();
        Boolean nonSaleInventory = Boolean.parseBoolean((String) request.getParameter("loadInventory"));
        KwlReturnObject purchaseprice = null, saleprice = null, quantity = null, initialquantity = null, initialprice = null, salespricedatewise = null, purchasepricedatewise = null, initialsalesprice = null;
        List listproduct = new ArrayList();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            JSONArray jsonarray = new JSONArray();
             int start = Integer.parseInt(request.getParameter("start"));
             int limit = Integer.parseInt(request.getParameter("limit"));
            KwlReturnObject result = accVendorCustomerProductDAOobj.getProductsByVendorFunction(companyId, ss,start,limit);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray vendorjarray = new JSONArray();
            while (itr.hasNext()) {
                VendorProductMapping VendorProductObj = (VendorProductMapping) itr.next();
                String vendorproductsid = VendorProductObj.getProducts().getID();
                KwlReturnObject vendorResult = accProductObj.getObject(Vendor.class.getName(), VendorProductObj.getVendor().getID());
                // Product product= (Product) vendorproductsid
                Vendor vendor = (Vendor) vendorResult.getEntityList().get(0);
                KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), vendorproductsid);
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
                obj.put("productweight", (Double) product.getProductweight());
                obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
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
                obj.put("accname", vendor.getName());
                obj.put("aliasname", vendor.getAliasname());
                if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                } else {
                    jArr.put(obj);
                }
            }
            
            jObj.put("count",result.getRecordTotalCount());
            jObj.put(Constants.RES_data,jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getProductVendorMappingJson : " + ex.getMessage(), ex);
        }
        return jObj;

    }
    
    
    
    
    public JSONArray getProductVendorMappingJson(HttpServletRequest request, List listproduct, List listvendor, String ss) throws SessionExpiredException, ServiceException, JSONException, UnsupportedEncodingException {
        Iterator itr = listvendor.iterator();
        JSONArray jArr = new JSONArray();
        Producttype producttype = new Producttype();
        Boolean nonSaleInventory = Boolean.parseBoolean((String) request.getParameter("loadInventory"));
        KwlReturnObject purchaseprice = null, saleprice = null, quantity = null, initialquantity = null, initialprice = null, salespricedatewise = null, purchasepricedatewise = null, initialsalesprice = null;
        try {
            while (itr.hasNext()) {
                Vendor vendor = (Vendor) itr.next();
                JSONArray jsonarray = new JSONArray();
                String Venid = vendor.getID();
                JSONObject productjsonobj = getProductorVendornames(Venid, ss);//getting the vendor's productsid
                if (productjsonobj.has("productjarray")) {
                    jsonarray = (JSONArray) productjsonobj.getJSONArray("productjarray");
                }

                if (jsonarray.length() > 0) {
                    for (int cnt = 0; cnt < jsonarray.length(); cnt++) {
                        Iterator iterator = listproduct.iterator();
                        while (iterator.hasNext()) {
                            Object[] rowproduct = (Object[]) iterator.next();
                            JSONArray jsonarrayproduct = new JSONArray();
                            Product Prod = (Product) rowproduct[0];
                            String Prodid = Prod.getID();
//Comparing the productid of vendor with the productid of all products array
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
                                
                                obj.put("productweight", (Double)product.getProductweight());
                                obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                                obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                                obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                                obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
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
                                obj.put("accname", vendor.getName());
                                obj.put("aliasname", vendor.getAliasname());
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
            throw ServiceException.FAILURE("getProductVendorMappingJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    //getting the product names of vendors -Neeraj D
    public JSONObject getProductorVendornames(String vendorid, String ss) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            KwlReturnObject result = accVendorCustomerProductDAOobj.getProductsByVendor(vendorid, ss, null);
            List<VendorProductMapping> list = result.getEntityList();
            JSONArray vendorjarray = new JSONArray();
            for (VendorProductMapping VendorProductObj:list) {
                String vendorproductsid = VendorProductObj.getProducts().getID();
                vendorjarray.put(vendorproductsid);
                jobj.put("productjarray", vendorjarray);
            }
            issuccess = true;
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    // Method for getting the data of CIMB bank for corresponding vendor
    public ModelAndView getCIMBReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONArray dataArray = accVendorControllerServiceObj.getCIMBReceivingBankDetailsForVendor(request);
            JSONArray pagedJson = dataArray;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put(Constants.RES_data, pagedJson);

            jobj.put("count", dataArray.length());

        } catch (JSONException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "getCIMBReceivingBankDetails : " + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView deleteCIMBReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Vendor_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            accVendorControllerServiceObj.deleteCIMBReceivingBankDetails(request);

            issuccess = true;

            msg = messageSource.getMessage("acc.cimb.receiving.deleted", null, RequestContextUtils.getLocale(request));   //IBG-Receiving Bank Detail has been deleted successfully.

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveOCBCReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramsObj = StringUtil.convertRequestToJsonObject(request);
            jobj = accVendorControllerServiceObj.saveOCBCReceivingBankDetails(paramsObj);
            issuccess = true;
            msg = messageSource.getMessage("acc.ocbcBank.ocbcReceivingDetails.saveMsg", null, RequestContextUtils.getLocale(request));   //IBG-Receiving Bank Details has been saved successfully.
        } catch (Exception ex) {
            msg = "saveOCBCReceivingBankDetails:" + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    // Method for getting OCBC bank data for corresponding vendor

    public ModelAndView getOCBCReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramsObj = StringUtil.convertRequestToJsonObject(request);
            jobj= accVendorControllerServiceObj.getOCBCReceivingBankDetails(paramsObj);
            issuccess = true;
        } catch (Exception ex) {
            msg = "getOCBCReceivingBankDetails : " + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView deleteOCBCReceivingBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramsJObj = StringUtil.convertRequestToJsonObject(request);
            jObj = accVendorControllerServiceObj.deleteOCBCReceivingBankDetails(paramsJObj);
            msg = messageSource.getMessage("acc.ocbcBank.ocbcReceivingDetails.deleteMsg", null, RequestContextUtils.getLocale(request));//OCBC Receiving bank detail has been deleted successfully.
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put(Constants.RES_success, issuccess);
                jObj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jObj.toString());
    }
    
    public ModelAndView getVendorRegistryDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            jobj = getVendorRegistryDetailsJson(request, false);
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView exportVendorRegistry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            boolean export = true;
            jobj = getVendorRegistryDetailsJson(request, export);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public JSONObject getVendorRegistryDetailsJson(HttpServletRequest request, boolean export) throws AccountingException, IOException, SessionExpiredException, JSONException {
        JSONObject jobj1 = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jMeta = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        JSONObject commData = new JSONObject();
        Locale locale = null;
        locale = RequestContextUtils.getLocale(request);
        try {
            String modules = request.getParameter("moduleid");
            String moduleids[]=modules.split(",");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = null;
            List list = null;
            HashMap<String, Object> requestParams = getVendorRegistryMap(request);
            /*
             *Get Recordset
             */
            jarrRecords=accVendorcontrollerCMNService.getRecordsForStore(requestParams,modules,jarrRecords);
            /*
             *Get ColumnModel
             */
            requestParams.put("locale", locale);
            jarrColumns=accVendorcontrollerCMNService.getColumnsForGrid(requestParams,modules,jarrColumns);
            
            String modulids=Constants.Acc_Vendor_Invoice_ModuleId+","+Constants.Acc_Purchase_Order_ModuleId+","+Constants.Acc_Purchase_Return_ModuleId+","+Constants.Acc_Vendor_Quotation_ModuleId+","+Constants.Acc_Goods_Receipt_ModuleId+","+Constants.Acc_Debit_Note_ModuleId+","+Constants.Acc_Credit_Note_ModuleId+","+Constants.Acc_Make_Payment_ModuleId+","+Constants.Acc_Receive_Payment_ModuleId;
            
            /*
             * Get Data of Invoice/Cash purchase 
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Vendor_Invoice_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                    if (array.length() > 0) {
                        result = accGoodsReceiptDAOobj.getGoodsReceiptsMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getPurchaseInvoiceInformation(requestParams, list, DataJArr);
                        requestParams.put("isOpeningBalanceInvoices", "true");
                        requestParams.put("includeAllRec", "false");
                        result = accGoodsReceiptDAOobj.getGoodsReceiptsMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getPurchaseInvoiceInformation(requestParams, list, DataJArr);
                    }
                } else {
                    result = accGoodsReceiptDAOobj.getGoodsReceiptsMerged(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getPurchaseInvoiceInformation(requestParams, list, DataJArr);
                }
            }
            /*
             * Get Data of Purchase Orders
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Purchase_Order_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                    requestParams.put("isOpeningBalance", false);
                    if (array.length() > 0) {
                        result = accPurchaseOrderobj.getPurchaseOrdersMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getPurchaseOrdersInformation(requestParams, list, DataJArr);
                    }
                } else {
                    result = accPurchaseOrderobj.getPurchaseOrdersMerged(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getPurchaseOrdersInformation(requestParams, list, DataJArr);
                }
            }
            /*
             * Get Data of Purchase Returns
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Purchase_Return_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put("moduleid", Constants.Acc_Purchase_Return_ModuleId);
                    if (array.length() > 0) {
                        result = accGoodsReceiptDAOobj.getPurchaseReturn(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getPurchaseReturnInformation(requestParams, list, DataJArr);
                    }
                } else {
                    result = accGoodsReceiptDAOobj.getPurchaseReturn(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getPurchaseReturnInformation(requestParams, list, DataJArr);
                }
            }
            /*
             * Get Data of Vendor Quotations
             */
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Vendor_Quotation_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Vendor_Quotation_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                    if (array.length() > 0) {
                        result = accPurchaseOrderobj.getQuotations(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getQuotationsInformation(requestParams, list, DataJArr);
                    }
                } else {
                    result = accPurchaseOrderobj.getQuotations(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getQuotationsInformation(requestParams, list, DataJArr);
                }
            }
            /*
             * Get Data of Goods Receipts
             */
            
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                    String Searchjson = request.getParameter("searchJson");
                    Searchjson = accJournalEntryobj.advSerachJsonForMultiModules(Searchjson, companyId, Constants.Acc_Goods_Receipt_ModuleId, modulids);
                    JSONObject search = new JSONObject(Searchjson);
                    JSONArray array = search.getJSONArray(Constants.root);
                    requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
                    if (array.length() > 0) {
                        result = accGoodsReceiptDAOobj.getGoodsReceiptOrdersMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getGoodsReceiptInformation(requestParams, list, DataJArr);
                    }
                } else {
                    result = accGoodsReceiptDAOobj.getGoodsReceiptOrdersMerged(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getGoodsReceiptInformation(requestParams, list, DataJArr);
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
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                    if (array.length() > 0) {
                        result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getDebitNoteInformation(requestParams, list, DataJArr);
                        result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getOpeningDebitNoteInformation(requestParams, list, DataJArr);
                    }
                } else {
                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getDebitNoteInformation(requestParams, list, DataJArr);
                    result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getOpeningDebitNoteInformation(requestParams, list, DataJArr);
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
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                    requestParams.put("cntype", 4);
                    requestParams.put("isOpeningBalance", false);
                    if (array.length() > 0) {
                        result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getCreditNoteInformation(requestParams, list, DataJArr);
                        result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getOpeningCreditNoteInformation(requestParams, list, DataJArr);
                    }
                } else {
                    requestParams.put("cntype", 4);
                    result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getCreditNoteInformation(requestParams, list, DataJArr);
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getOpeningCreditNoteInformation(requestParams, list, DataJArr);
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
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                    requestParams.put("paymentWindowType", 1);
                    requestParams.put("isOpeningBalance", false);
                    if (array.length() > 0) {
                        result = accVendorPaymentobj.getPayments(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getMadePaymentsInformation(requestParams, list, DataJArr);
                        result =accVendorPaymentobj.getAllOpeningBalancePayments(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getOpeningMadePaymentsInformation(requestParams, list, DataJArr);
                    }
                } else {
                    requestParams.put("paymentWindowType", 1);
                    result = accVendorPaymentobj.getPayments(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getMadePaymentsInformation(requestParams, list, DataJArr);
                    result =accVendorPaymentobj.getAllOpeningBalancePayments(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getOpeningMadePaymentsInformation(requestParams, list, DataJArr);
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
                    requestParams.put("searchJson", Searchjson);
                    requestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                    requestParams.put("paymentWindowType", 2);
                    requestParams.put("isOpeningBalance", false);
                    if (array.length() > 0) {
                        result = accReceiptDAOobj.getReceipts(requestParams);
                        list = result.getEntityList();
                        DataJArr = accVendorcontrollerCMNService.getReceivedPaymentsInformation(requestParams, list, DataJArr);
                    }
                } else {
                    requestParams.put("paymentWindowType", 2);
                    result = accReceiptDAOobj.getReceipts(requestParams);
                    list = result.getEntityList();
                    DataJArr = accVendorcontrollerCMNService.getReceivedPaymentsInformation(requestParams, list, DataJArr);
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
                jobj1.put(Constants.RES_data, DataJArr);
            } else {
                jobj1.put(Constants.RES_data, commData);
            }

        } catch (Exception ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj1;
    }
    
    private HashMap<String, Object> getVendorRegistryMap(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        String companyId = sessionHandlerImpl.getCompanyid(request);
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String newvendorid = request.getParameter("person");
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
        requestParams.put("dateformat", authHandler.getGlobalDateFormat());
        requestParams.put("gcurrency", currency.getCurrencyID());
        requestParams.put("currencyname", currency.getName());
        requestParams.put(Constants.isExport, !StringUtil.isNullOrEmpty(request.getParameter(Constants.isExport))?true:false);
        requestParams.put("browsertimezone", sessionHandlerImpl.getBrowserTZ(request));
        requestParams.put(Constants.df, df);
        requestParams.put(Constants.companyid, companyId);
        requestParams.put(Constants.newvendorid, newvendorid);
        requestParams.put(Constants.REQ_startdate, startDate);
        requestParams.put(Constants.REQ_enddate, endDate);
        return requestParams;
    }
    
    public ModelAndView importDBSBankDetails(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", companyid);
            boolean typeXLSFile = (request.getParameter("typeXLSFile") != null) ? Boolean.parseBoolean(request.getParameter("typeXLSFile")) : false;
            String doAction = request.getParameter("do");

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("importMethod", typeXLSFile ? "xls" : "csv");
            requestParams.put("currencyId", companyid);
            requestParams.put("moduleName", Constants.DBS_Bank_Module_Name);
            requestParams.put("moduleid", Constants.DBS_Bank_Details_ModuleId);

            //Import Data
            if (doAction.compareToIgnoreCase("import") == 0) {
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);

                datajobj.put("resjson", resjsonJArray);
                jobj = importHandler.importFileData(requestParams);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) { // Validate the data before importing
                jobj = importHandler.validateFileData(requestParams);
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException e) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
            
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } 
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * Description: Method for importing Vendor Category
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView importVendorCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = getImportVendorCategoryParams(request);
            jobj = importVendorCategoryJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * GST Details Changes for Customer vendor. IF customer/ Vendor used in
     * Transaction then show confirm message. GST details - GSTIN Registration
     * Type , Customer/ Vendor Type
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView isCustomerVendorUsedInTransacton(HttpServletRequest request, HttpServletResponse response) {
        JSONObject custVendTransactionDetails = new JSONObject();
        try {
            JSONObject paramJobj = getImportVendorCategoryParams(request);
            custVendTransactionDetails = accVendorcontrollerCMNService.isCustomerVendorUsedInTransacton(paramJobj);
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", custVendTransactionDetails.toString());
    }
    /**
     * Description: Method for getting parameters of import Vendor Category
     * Details
     *
     * @param request
     * @return
     * @throws JSONException
     * @throws SessionExpiredException
     */
    public JSONObject getImportVendorCategoryParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
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
    public JSONObject importVendorCategoryJSON(JSONObject paramJobj) {
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
            requestParams.put(Constants.moduleid, Constants.VENDOR_CATEGORY_MODULE_ID);
            requestParams.put("moduleName", Constants.VENDOR_CATEGORY_MODULE_NAME);
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
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }
}
