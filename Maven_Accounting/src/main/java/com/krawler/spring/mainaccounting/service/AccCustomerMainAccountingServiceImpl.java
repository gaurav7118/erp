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
package com.krawler.spring.mainaccounting.service;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.CustomerAddressDetails;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accCusVenMapDAO;
import com.krawler.spring.accounting.account.accVendorCustomerProductDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceHandler;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.util.ajax.JSON;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;

/**
 *
 * @author krawler
 */
public class AccCustomerMainAccountingServiceImpl implements AccCustomerMainAccountingService,MessageSourceAware {
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accInvoiceCMN accInvoiceCMNobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accDebitNoteDAO accDebitNoteobj;
    private accReceiptDAO accReceiptDAOobj;
    private accCustomerDAO accCustomerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private accVendorCustomerProductDAO accVendorCustomerProductDAOobj;
    private accCusVenMapDAO accCusVenMapDAOObj;
    private com.krawler.spring.common.fieldDataManager accountingCommonfieldDataManage;
    private accPaymentDAO accPaymentDAOobj;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private accProductDAO accProductObj;

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setAccCusVenMapDAOObj(accCusVenMapDAO accCusVenMapDAOObj) {
        this.accCusVenMapDAOObj = accCusVenMapDAOObj;
    }

    public void setCommonfieldDataManager(com.krawler.spring.common.fieldDataManager fieldDataManagercntrl) {
        this.accountingCommonfieldDataManage = fieldDataManagercntrl;
    }

    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public accVendorCustomerProductDAO getAccVendorCustomerProductDAOobj() {
        return accVendorCustomerProductDAOobj;
    }

    public void setAccVendorCustomerProductDAOobj(accVendorCustomerProductDAO accVendorCustomerProductDAOobj) {
        this.accVendorCustomerProductDAOobj = accVendorCustomerProductDAOobj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccInvoiceCMN(accInvoiceCMN accInvoiceCMNobj) {
        this.accInvoiceCMNobj = accInvoiceCMNobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }

    public auditTrailDAO getAuditTrailObj() {
        return auditTrailObj;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    } 
      public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    
     public JSONObject getCustomerExceedingCreditLimit(HttpServletRequest request) throws ServiceException, SessionExpiredException, JSONException {

        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            Customer customer=null;
            if(request.getParameter("customer")!=null && request.getParameter("totalSUM")!=null)
            {
                KwlReturnObject cstresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), request.getParameter("customer"));
                customer = (Customer) cstresult.getEntityList().get(0);

                JSONObject obj = new JSONObject();
                obj.put("accid", customer.getAccount().getID());
                obj.put("custId", customer.getID());
                obj.put("custName", customer.getName());
                jArr.put(obj);
                double totalAmountDue=0;
                JSONArray customerAmountDueJArr = getCustomerAmountDue(jArr, request);
                double amountDue=customerAmountDueJArr.getJSONObject(0).getDouble("amountdue");
                double totalSUM=0;
                totalSUM=Double.parseDouble(request.getParameter("totalSUM"));
                totalAmountDue=amountDue+totalSUM;
                double fixedCreditLimit=customer.getCreditlimit();                
                
                    JSONObject jsTemp = new JSONObject();
                    jsTemp.put("name", customer.getName());
                    jsTemp.put("amountDue",amountDue);
                    jsTemp.put(Constants.limit, customer.getCreditlimit());
                    
//              Total Amount Due is less than or equal to Credit Limit
                if(totalAmountDue <= fixedCreditLimit)
                {                    
                    jsTemp.put("limitflag",false);
                }
                
//              Total Amount Due is greater than Credit Limit
                if(totalAmountDue > fixedCreditLimit)
                {  
                    jsTemp.put("limitflag",true);      
                }
                
                jobj.append("data", jsTemp);
               
            }
        }catch (JSONException ex) {
            Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountsExceedingBudget : " + ex.getMessage(), ex);
        }
        return jobj;
    }//getCustomerExceedingCreditLimit
    
     
         public JSONArray getCustomerAmountDue(JSONArray jArr, HttpServletRequest request){
    	try{
    		HashMap<String, Object> requestParams = getInvoiceRequestMap(request);
                
    		requestParams.put("nondeleted", "true");
    		requestParams.put("deleted", "false");
                requestParams.put(Constants.ss, null);
    		for(int i = 0; i < jArr.length(); i++){
                    getCustomerAmountDueByObject(jArr.getJSONObject(i), request, requestParams);
    		}

    	}catch (Exception ex){
    		Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
    	}
    	return jArr;
    }
         
       public JSONObject getCustomerAmountDueByObject(JSONObject jSONObject, HttpServletRequest request, HashMap<String, Object> requestParams) throws JSONException, ServiceException, SessionExpiredException{
                    HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(),filter_params = new ArrayList();
                    String accid = jSONObject.getString("accid");
                    requestParams.put(InvoiceConstants.accid, accid);
                    requestParams.put(InvoiceConstants.customerid, accid);
                    String companyid = sessionHandlerImpl.getCompanyid(request);
                    if(jSONObject.has("startdate"))
                    {
                        requestParams.put(Constants.REQ_startdate, jSONObject.get("startdate").toString());
                    }
                    if(jSONObject.has("enddate"))
                    {
                        requestParams.put(Constants.REQ_enddate, jSONObject.get("enddate").toString());
                    }                                          
                    
                    double amountdue = 0;
                    double newBaseAmount = 0;
                    double baseamountdue = 0;
                    double baseamountduenew = 0;
                    KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
                    if(result.getEntityList() != null){
                        Iterator itr = result.getEntityList().iterator();
                        while (itr.hasNext()) {
                            Invoice invoice = (Invoice) itr.next();  
                            if(Constants.InvoiceAmountDueFlag) {
                                List ll = accInvoiceCMNobj.getInvoiceDiscountAmountInfo(requestParams, invoice);
                                amountdue = amountdue + (Double) ll.get(0);
                            } else {
                                 List ll = accInvoiceCMNobj.getAmountDue_Discount(requestParams, invoice);
                                 amountdue = amountdue + (Double) ll.get(0);
                            }
                            String fromcurrencyid = invoice.getCurrency().getCurrencyID();                         
//                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                            baseamountduenew=authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                            baseamountdue = baseamountdue + baseamountduenew;
                        }
                        jSONObject.put("amountdue", baseamountdue);
                    }else{
                        jSONObject.put("amountdue", 0);
                    }   
                    requestParams.put("cntype", null);//cntype after each loop become 4 so need to update with null
                    result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);//This is used for getting CN gainst customer and otherwise 
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
                                     double cnamountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                                     baseamountdue-=cnamountdueinbase;//In case of Credit note amount is subtracted
                                 }
                             }
                         }
                    }                
                    requestParams.put("cntype", 4);   //This is used for getting DN gainst customer
                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);
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
                                     double dnamountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                                     baseamountdue+=dnamountdueinbase;//In case of debit note amount is Added
                                 }
                             }
                         }
                    }
                    
                    //TO DO- when we allow partial receive payment then we need to change the logic for advance used
                     
                    requestParams.put("isadvancepayment", true); //Only Advance payment type make payments required. so it is true 
                    result = accReceiptDAOobj.getReceipts(requestParams);
                    if (result.getEntityList() != null) {            
                        Iterator itr = result.getEntityList().iterator();
                        while (itr.hasNext()) {
                            Object[] row = (Object[]) itr.next();
                            Receipt receipt = (Receipt) row[0];
                            
                            rRequestParams.clear(); filter_names.clear(); filter_params.clear();
                            filter_names.add("receipt.ID");
                            filter_params.add(receipt.getID());
                            rRequestParams.put("filter_names", filter_names);
                            rRequestParams.put("filter_params", filter_params);
                            KwlReturnObject grdresult = accReceiptDAOobj.getReceiptDetails(rRequestParams);
                            boolean advanceUsed = grdresult.getEntityList().size()>0?true:false;
                                                                                
                            if (receipt.isIsadvancepayment() && receipt.getDepositAmount()>0 && !advanceUsed) {
                                double receiptamountdue = receipt.getDepositAmount();
                                double exchangerate = receipt.getJournalEntry() != null ? receipt.getJournalEntry().getExternalCurrencyRate() : 0;
//                                Date creationdate = receipt.getJournalEntry().getEntryDate();
                                Date creationdate = receipt.getCreationDate();
                                String cncurrencyid = receipt.getCurrency().getCurrencyID();
                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receiptamountdue, cncurrencyid, creationdate, exchangerate);
                                double receiptamountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                                baseamountdue -= receiptamountdueinbase; //In case of Receive Paymeny amount is subtracted
                            }
                        }
                    }
                    
                    // putting opening amount of customer
                    double openbalanceAmtDueOfCustomer = accInvoiceCMNobj.getOpeningBalanceAmountDueOfAccount(request,null,true,accid);
                    baseamountdue = baseamountdue+openbalanceAmtDueOfCustomer;
                    jSONObject.put("amountdue", baseamountdue);
                    jSONObject.put("amountdue", baseamountdue);
        return jSONObject;
    }//getCustomerAmountDueByObject   
       
       public static HashMap<String, Object> getInvoiceRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(CCConstants.REQ_costCenterId, request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(InvoiceConstants.accid, request.getParameter(InvoiceConstants.accid));
        requestParams.put(InvoiceConstants.cashonly, request.getParameter(InvoiceConstants.cashonly));
        requestParams.put(InvoiceConstants.creditonly, request.getParameter(InvoiceConstants.creditonly));
        requestParams.put("CashAndInvoice", request.getParameter("CashAndInvoice") != null ? Boolean.parseBoolean(request.getParameter("CashAndInvoice")) : false);
        boolean fullPaidFlag = StringUtil.getBoolean(request.getParameter("fullPaidFlag"));
        requestParams.put(InvoiceConstants.ignorezero, fullPaidFlag ? "false" : request.getParameter(InvoiceConstants.ignorezero));
        requestParams.put(InvoiceConstants.persongroup, request.getParameter(InvoiceConstants.persongroup));
        requestParams.put(InvoiceConstants.isagedgraph, request.getParameter(InvoiceConstants.isagedgraph));
        requestParams.put(InvoiceConstants.curdate, request.getParameter(InvoiceConstants.curdate));
        requestParams.put(InvoiceConstants.customerid, request.getParameter(InvoiceConstants.customerid));
        requestParams.put(InvoiceConstants.newcustomerid, request.getParameter(InvoiceConstants.newcustomerid));
        requestParams.put(InvoiceConstants.customerCategoryid, request.getParameter(InvoiceConstants.customerCategoryid));
        requestParams.put(InvoiceConstants.deleted, request.getParameter(InvoiceConstants.deleted));
        requestParams.put(InvoiceConstants.nondeleted, request.getParameter(InvoiceConstants.nondeleted));
        requestParams.put(InvoiceConstants.billid, request.getParameter(InvoiceConstants.billid));
        requestParams.put(InvoiceConstants.getRepeateInvoice, request.getParameter(InvoiceConstants.getRepeateInvoice));
        requestParams.put(InvoiceConstants.isSalesCommissionStmt, request.getParameter(InvoiceConstants.isSalesCommissionStmt));
        requestParams.put(InvoiceConstants.userid, request.getParameter(InvoiceConstants.userid));
        requestParams.put(InvoiceConstants.onlyamountdue, request.getParameter(InvoiceConstants.REQ_onlyAmountDue));
        requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
        requestParams.put("pendingapproval", (request.getParameter("pendingapproval") != null) ? Boolean.parseBoolean(request.getParameter("pendingapproval")) : false);
        requestParams.put("istemplate", (request.getParameter("istemplate") != null) ? Integer.parseInt(request.getParameter("istemplate")) : 0);
        requestParams.put("isFixedAsset", (request.getParameter("isFixedAsset") != null) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false);
        requestParams.put("isLeaseFixedAsset", (request.getParameter("isLeaseFixedAsset") != null) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false);
        requestParams.put("includeFixedAssetInvoicesFlag", (request.getParameter("includeFixedAssetInvoicesFlag") != null) ? Boolean.parseBoolean(request.getParameter("includeFixedAssetInvoicesFlag")) : false);
        requestParams.put(InvoiceConstants.productid, (request.getParameter(InvoiceConstants.productid) == null) ? "" : request.getParameter(InvoiceConstants.productid));
        requestParams.put(InvoiceConstants.productCategoryid, request.getParameter(InvoiceConstants.productCategoryid));
        requestParams.put(InvoiceConstants.termid, (request.getParameter(InvoiceConstants.termid) == null) ? "" : request.getParameter(InvoiceConstants.termid));
        requestParams.put(InvoiceConstants.prodfiltercustid, (request.getParameter(InvoiceConstants.prodfiltercustid) == null) ? "" : request.getParameter(InvoiceConstants.prodfiltercustid));
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null) ? "" : request.getParameter("currencyfilterfortrans"));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(InvoiceConstants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put(InvoiceConstants.MARKED_FAVOURITE, request.getParameter(InvoiceConstants.MARKED_FAVOURITE));
        requestParams.put("isOpeningBalanceInvoices", request.getParameter("isOpeningBalanceInvoices"));
        requestParams.put("direction", (request.getParameter("direction") == null) ? "" : request.getParameter("direction"));
        requestParams.put("isLifoFifo", (request.getParameter("isLifoFifo") == null) ? "" : request.getParameter("isLifoFifo"));
        requestParams.put(InvoiceConstants.salesPersonid, (request.getParameter(InvoiceConstants.salesPersonid) == null) ? "" : request.getParameter(InvoiceConstants.salesPersonid));
        requestParams.put("custVendorID", request.getParameter("custVendorID"));
        requestParams.put("datefilter", request.getParameter("datefilter"));
        requestParams.put(InvoiceConstants.duration, (request.getParameter(InvoiceConstants.duration) != null) ? Integer.parseInt(request.getParameter(InvoiceConstants.duration)) : 0);
        if (request.getParameter("isReceipt") != null) {
            requestParams.put("isReceipt", request.getParameter("isReceipt"));
        }
        requestParams.put("isDraft", (request.getParameter("isDraft") != null) ? Boolean.parseBoolean(request.getParameter("isDraft")) : false);
        return requestParams;
    }
    
   @Override    
    public JSONObject getCustomers(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getCustomerRequestMapJSON(paramJobj);
            String companyid = paramJobj.getString(Constants.companyKey);
            if (paramJobj.optString("custAmountDueMoreThanLimit") != null && Boolean.parseBoolean(paramJobj.optString("custAmountDueMoreThanLimit"))) {
                requestParams.put("custAmountDueMoreThanLimit", paramJobj.getString("custAmountDueMoreThanLimit"));
            }
//            requestParams.put("customerIntegrationFlag", true);
            KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
            boolean quickSearchFlag = false;
            if (requestParams.containsKey(Constants.ss) && requestParams.get(Constants.ss) != null && (!StringUtil.isNullOrEmpty(requestParams.get(Constants.ss).toString()))) {
                quickSearchFlag = true;
            }
            ArrayList list = accCustomerDAOobj.getCustomerArrayList(result.getEntityList(), requestParams, quickSearchFlag, false);
            JSONArray jArr = getCustomerJson(paramJobj, list);
            if (!storageHandlerImpl.GetLowercaseCompanyId().contains(companyid)) {
                jArr = getCustomerAmountDue(jArr, paramJobj);
                if (paramJobj.optString("custAmountDueMoreThanLimit") != null && Boolean.parseBoolean(paramJobj.optString("custAmountDueMoreThanLimit"))) {
                    JSONArray jSONCustAmountDueMoreThanLimit = new JSONArray();
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jSONObject = jArr.getJSONObject(i);
                        if (jSONObject.optDouble(Constants.limit, 0.0) < jSONObject.optDouble("amountdue", 0.0)) {
                            jSONCustAmountDueMoreThanLimit.put(jSONObject);
                        }
                    }
                    jArr = jSONCustAmountDueMoreThanLimit;
                }
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "AccCustomerMainAccountingServiceImpl.getCustomers : " + ex.getMessage();
            Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
   
   @Override
    public JSONArray getCustomerJson(JSONObject paramJobj, List<Object[]> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            DecimalFormat format = new DecimalFormat("############.####");
            HashMap<String, Object> customerRequestParams = new HashMap<String, Object>();
            List headerlist=new ArrayList();
            KwlReturnObject prefRes = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            boolean isParentChildmappingAvailable = false;
            boolean isExport=false;
            ExtraCompanyPreferences companyPreferences = (ExtraCompanyPreferences) prefRes.getEntityList().get(0);
            if (companyPreferences != null && companyPreferences.isPropagateToChildCompanies()) {
                isParentChildmappingAvailable = true;
            }
            if (paramJobj.has(Constants.isExport)) {
                isExport = paramJobj.getString(Constants.isExport) != null ? Boolean.parseBoolean(paramJobj.getString(Constants.isExport)) : false;
                String header = paramJobj.getString("header");
                String headerarr[]=(String[])header.split(",");
                headerlist=Arrays.asList(headerarr);
            }
            /*
             * get CustomFields value
             */
                //for Default Customer List i.e Customer Exceeding Credit Limit
            HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMapJson(paramJobj);
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Customer_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            for (Object[] row:list) {
                Account account = null;
                JSONArray jrr = new JSONArray();
                String tempstring = "";
                String productsCode = "";
                String productsName = "";
                Customer customer = (Customer) row[1];
                if (customer == null) {
                    continue;
                }
                if (customer.getAccount() != null) {
                    account = customer.getAccount();
                }
                //getting the json customer mapped products
                String Custid = customer.getID();
                String ss = "";
                if (!isExport) {
                    KwlReturnObject productjsonobj = accVendorCustomerProductDAOobj.getProductsByCustomer(Custid, ss, null);
                    List<CustomerProductMapping> listpro = productjsonobj.getEntityList();
                    JSONArray customerjarray = new JSONArray();
                    for (CustomerProductMapping CustomerProductObj : listpro) {
                        JSONObject customJSONObj = new JSONObject();
                        String customerproductsid = CustomerProductObj.getProducts() != null ? CustomerProductObj.getProducts().getID() : "";
                        String customerproductcode = CustomerProductObj.getProducts() != null ? CustomerProductObj.getProducts().getProductid() : "";
                        String customerproductname = CustomerProductObj.getProducts() != null ? CustomerProductObj.getProducts().getProductName() : "";
                        tempstring = tempstring.concat(customerproductsid + ",");
                        productsName = productsName.concat(customerproductname + ",");
                        productsCode = productsCode.concat(customerproductcode + ",");
                        customerjarray.put(customerproductsid);
                        if(CustomerProductObj.getProducts() != null && CustomerProductObj.getJsonstring()!=null){
                              customJSONObj.put(CustomerProductObj.getProducts().getID(), JSON.parse(CustomerProductObj.getJsonstring().toString()));
                              jrr.put(customJSONObj);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(tempstring.toString())) {
                        tempstring = tempstring.substring(0, tempstring.lastIndexOf(","));
                        productsCode = productsCode.substring(0, productsCode.lastIndexOf(","));
                        productsName = productsName.substring(0, productsName.lastIndexOf(","));
                    }
                }
                JSONObject obj = new JSONObject();
                obj.put("acccode", (StringUtil.isNullOrEmpty(customer.getAcccode())) ? "" : customer.getAcccode());
                obj.put("accid", customer.getID());
                obj.put("custId", customer.getID());
                obj.put("synchedfromotherapp", customer.isSynchedFromOtherApp());
                obj.put("accname", customer.getName());
                obj.put("aliasname", StringUtil.isNullOrEmpty(customer.getAliasname()) ? "" : customer.getAliasname());
                obj.put("accnamecode", (StringUtil.isNullOrEmpty(customer.getAcccode())) ? customer.getName() : "[" + customer.getAcccode() + "]" + customer.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("accountname", account.getName());
                //Control Account Details
                obj.put("controlaccountcode", StringUtil.isNullOrEmpty(account.getAcccode())? "" : account.getAcccode());
                obj.put("controlaccountname", StringUtil.isNullOrEmpty(account.getName())? "" : account.getName());
                
                obj.put("customJSONString",  jrr);
                if (customer.isActivate()) {
                    obj.put("isactivate", "Active");
                } else {
                    obj.put("isactivate", "Dormant");
                }
                // calculation of opening balance
//                double openbalance = accInvoiceCMNobj.getOpeningBalanceOfAccount(request, null, true, customer.getID());
                if (isExport && headerlist.contains("openbalance")) {
                    double openbalance = accInvoiceCMNobj.getOpeningBalanceOfAccountJson(paramJobj, null, true, customer.getID());
                    openbalance = authHandler.round(openbalance, companyid); //SDP-1236
                    obj.put("openbalance", openbalance);
                } else if(!isExport) {
                    double openbalance = accInvoiceCMNobj.getOpeningBalanceOfAccountJson(paramJobj, null, true, customer.getID());
                    openbalance = authHandler.round(openbalance, companyid); //SDP-1236
                    obj.put("openbalance", openbalance);
                }
                if (customer.getParent() != null) {
                    obj.put("parentid", customer.getParent().getID());
                    obj.put("parentname", customer.getParent().getName());
                }
                KWLCurrency currency = (KWLCurrency) row[4];
                obj.put(Constants.currencyKey, customer.getCurrency() == null ? currency.getCurrencyID() : customer.getCurrency().getCurrencyID());
                obj.put("currencysymbol", customer.getCurrency() == null ? currency.getSymbol() : customer.getCurrency().getSymbol());
                obj.put("currencyname", customer.getCurrency() == null ? currency.getName() : customer.getCurrency().getName());
                obj.put("level", row[2]);
                obj.put("leaf", row[3]);
                obj.put("title", customer.getTitle());
                if (!StringUtil.isNullOrEmpty(customer.getTitle())) {
                    KwlReturnObject taxresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), customer.getTitle());
                    MasterItem item = (MasterItem) taxresult.getEntityList().get(0);
                    obj.put("titlename", item != null ? item.getValue() : "");
                } else {
                    obj.put("titlename", "");
                }
                obj.put("contactno2", customer.getAltContactNumber());
                obj.put("isPermOrOnetime", customer.isIsPermOrOnetime());
                obj.put("pdm", customer.getPreferedDeliveryMode());
                obj.put("termname", customer.getCreditTerm() != null ? customer.getCreditTerm().getTermname() : "");
                obj.put("termnameValue", customer.getCreditTerm() != null ? customer.getCreditTerm().getTermname() : "");
                obj.put("termid", customer.getCreditTerm() != null ? customer.getCreditTerm().getID() : "");
                obj.put("termdays", customer.getCreditTerm() != null ? customer.getCreditTerm().getTermdays() : "");
                obj.put("isavailableonlytosalespersons", customer.isIsCusotmerAvailableOnlyToSalespersons());
                obj.put("mappedSalesPersonId", ((customer.getMappingSalesPerson() != null) ? customer.getMappingSalesPerson().getID() : ""));
                obj.put("mappedReceivedFromId", ((customer.getMappingReceivedFrom() != null) ? customer.getMappingReceivedFrom().getID() : ""));
                obj.put("mappedPaidToId", ((customer.getMappingPaidTo() != null) ? customer.getMappingPaidTo().getID() : ""));
                obj.put("mappedSalesPersonName", ((customer.getMappingSalesPerson() != null) ? customer.getMappingSalesPerson().getValue() : ""));
                String[] multisalesperson = getMultiSalesPersonIDs(customer.getID());//fetching masteritem mapped to that customer.
                obj.put("mappedMultiSalesPersonId", multisalesperson[0]);
                obj.put("salesperson", multisalesperson[1]);
                obj.put("salespersonValue", multisalesperson[1]);
                obj.put("salesPersonAgent", multisalesperson[1]); //ERP-19693
                obj.put("salesPerson", multisalesperson[0]);
                obj.put("salesPersonValue", multisalesperson[1]);
                obj.put("defaultsalesperson", customer.getMappingSalesPerson() != null ? customer.getMappingSalesPerson().getID() : "");
                obj.put("defaultsalespersonValue", customer.getMappingSalesPerson() != null ? customer.getMappingSalesPerson().getValue() : "");
                obj.put("nameinaccounts", customer.getAccount().getName());
                obj.put("bankaccountno", customer.getBankaccountno());
                obj.put("other", (customer.getOther() != null) ? customer.getOther() : "");
                obj.put("deleted", customer.getAccount().isDeleted());
                obj.put(Constants.Acc_id, customer.getID());
                obj.put("taxno", customer.getTaxNo());
                obj.put("taxId", customer.getTaxid());
                String[] category = getCustomerCategoryIDs(customer.getID());
                obj.put("categoryid", category[0]);
                obj.put("categoryname", category[1]);
                if (!StringUtil.isNullOrEmpty(customer.getTaxid())) {
                    KwlReturnObject taxresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), customer.getTaxid());
                    Tax tax = (Tax) taxresult.getEntityList().get(0);
                    obj.put("taxcode", tax != null ? tax.getTaxCode() : "");
                } else {
                    obj.put("taxcode", "");
                }
                if (!isExport&&isParentChildmappingAvailable) {
                    customerRequestParams.put("propagatedCustomerID", customer.getID());
                    KwlReturnObject result = accCustomerDAOobj.getChildCustomerCount(customerRequestParams); // to check if parent has child customers in child companies
                    if (result != null && result.getRecordTotalCount() > 0) {
                        obj.put("isPropagatedPersonalDetails", true);
                    } else {
                        obj.put("isPropagatedPersonalDetails", false);
                    }
                }
                obj.put("intercompanytypeid", customer.getIntercompanytype() != null ? customer.getIntercompanytype().getID() : "");
                obj.put("intercompany", customer.isIntercompanyflag());
                obj.put("creationDate", customer.getCreatedOn() != null ? authHandler.getDateOnlyFormat().format(customer.getCreatedOn()) : "");
                obj.put("country", (customer.getCountry() == null ? "" : customer.getCountry().getID()));
                obj.put(Constants.limit, format.format(customer.getCreditlimit()));
                obj.put("mapcustomervendor", customer.isMapcustomervendor());
                obj.put("uenno", customer.getUENNumber());
                obj.put("gstin", customer.getGSTIN());
                obj.put("GSTINRegistrationTypeId", customer.getGSTRegistrationType()!=null ? customer.getGSTRegistrationType().getID():"");
                obj.put("CustomerVendorTypeId", customer.getGSTCustomerType()!=null ? customer.getGSTCustomerType().getID():"");
                obj.put("GSTINRegistrationTypeName", customer.getGSTRegistrationType()!=null ? customer.getGSTRegistrationType().getValue():"");
                obj.put("seztodate",customer.getSezToDate()!=null?authHandler.getDateOnlyFormat().format(customer.getSezToDate()):"");
                obj.put("sezfromdate",customer.getSezFromDate()!=null?authHandler.getDateOnlyFormat().format(customer.getSezFromDate()):"");
                obj.put("CustomerVendorTypeName", customer.getGSTCustomerType()!=null ? customer.getGSTCustomerType().getValue():"");
                obj.put("vattinno", !StringUtil.isNullOrEmpty(customer.getVATTINnumber()) ? customer.getVATTINnumber() : "");
                obj.put("csttinno", !StringUtil.isNullOrEmpty(customer.getCSTTINnumber()) ? customer.getCSTTINnumber() : "");
                if (companyPreferences != null && companyPreferences.getCompany().getCountry().getID().equals("106")) {
                    obj.put("npwp", !StringUtil.isNullOrEmpty(customer.getPANnumber()) ? customer.getPANnumber() : "");
                } else {
                    obj.put("panno", !StringUtil.isNullOrEmpty(customer.getPANnumber()) ? customer.getPANnumber() : "");
                }
                obj.put("vattinnumber", !StringUtil.isNullOrEmpty(customer.getVATTINnumber())? customer.getVATTINnumber():"");
                obj.put("csttinnumber", !StringUtil.isNullOrEmpty(customer.getCSTTINnumber())? customer.getCSTTINnumber():"");
                obj.put("pannumber", !StringUtil.isNullOrEmpty(customer.getPANnumber())? customer.getPANnumber():"");
                obj.put("eccnumber", !StringUtil.isNullOrEmpty(customer.getECCnumber())? customer.getECCnumber():"");
                obj.put("panStatusId", customer.getPanStatus() == null ? "" : customer.getPanStatus());
                obj.put("deducteeTypeId", customer.getDeducteeType() == null ? "" : customer.getDeducteeType());
                obj.put("residentialstatus", customer.getResidentialstatus());
                obj.put("panStatusId", customer.getPanStatus() == null ? "" : customer.getPanStatus());
                obj.put("servicetaxno", !StringUtil.isNullOrEmpty(customer.getSERVICEnumber()) ? customer.getSERVICEnumber() : "");
                obj.put("tanno", !StringUtil.isNullOrEmpty(customer.getTANnumber()) ? customer.getTANnumber() : "");
                obj.put("eccno", !StringUtil.isNullOrEmpty(customer.getECCnumber()) ? customer.getECCnumber() : "");
                obj.put("overseas", customer.isOverseas());
                obj.put("mappingcusaccid", customer.getAccount().getID());
                obj.put("sequenceformat", customer.getSeqformat() == null ? "NA" : customer.getSeqformat().getID());
                obj.put("customerparent", customer.getParent() == null ? "" : customer.getParent().getAcccode());
                obj.put("paymentmethod",StringUtil.isNullOrEmpty(customer.getDefaultPaymentMethod())?"":customer.getDefaultPaymentMethod());      //ERM-735 add default payment to customer
                obj.put("paymentCriteria", customer.getPaymentCriteria());
                obj.put("interstateparty", customer.isInterstateparty());
                obj.put("pricingBandName", customer.getPricingBandMaster() == null ? "" : customer.getPricingBandMaster().getName());
                //To Verify whether TDS is applied on respective Customer or not.
                obj.put("isTDSapplicableoncust", customer.isIsTDSapplicableoncust());
                if (!isExport) {
                    boolean isused = accCusVenMapDAOObj.isCustomerUsedInTransactions(customer.getID(), companyid); //ERP-19783
                    obj.put("isInterstatepartyEditable", !isused);
                    obj.put("isUsedInTransactions", isused);
                
                obj.put("cformapplicable", customer.isCformapplicable());
                obj.put("dealertype", !StringUtil.isNullOrEmpty(customer.getDealertype()) ? customer.getDealertype() : "");
                obj.put("vatregdate", (customer.getVatregdate() != null) ? authHandler.getDateOnlyFormat().format(customer.getVatregdate()) : "");
                obj.put("cstregdate", (customer.getCSTRegDate() != null) ? authHandler.getDateOnlyFormat().format(customer.getCSTRegDate()) : "");
                if (customer.getPaymentCriteria() == 1) {
                    obj.put("paymentCriteriaName", "NA");
                } else if (customer.getPaymentCriteria() == 2) {
                    obj.put("paymentCriteriaName", "LIFO");
                } else if (customer.getPaymentCriteria() == 3) {
                    obj.put("paymentCriteriaName", "FIFO");
                } else {
                    obj.put("paymentCriteriaName", "");
                }
               
                obj.put("employmentStatus", customer.getEmploymentStatus());
                obj.put("companyAddress", customer.getCompanyAddress());
                obj.put("occupationAndYears", customer.getOccupation());
                obj.put("employerName", customer.getEmployerName());
                obj.put("monthlyIncome", customer.getIncome());
                obj.put("noofActiveCreditLoans", customer.getNoofActiveCreditLoans());
                obj.put("companyRegistrationNumber", customer.getCompanyRegistrationNumber());
                obj.put("gstRegistrationNumber", customer.getGstRegistrationNumber());
                obj.put("pricingBandID", customer.getPricingBandMaster() == null ? "" : customer.getPricingBandMaster().getID());
//                obj.put("pricingBandName", customer.getPricingBandMaster() == null ? "" : customer.getPricingBandMaster().getName());
                if (customer.isMapcustomervendor()) {
                    CustomerVendorMapping customervendormapping = accCusVenMapDAOObj.checkCustomerMappingExists(customer.getID());
                    if (customervendormapping != null) {
                        obj.put("mappingvenaccid", customervendormapping.getVendoraccountid().getAccount().getID());
                        obj.put("vendorcode", customervendormapping.getVendoraccountid().getAcccode());
                        obj.put("vendoraccountname", customervendormapping.getVendoraccountid().getAccount().getName());
                        obj.put("deducteeTypeId", customervendormapping.getVendoraccountid().getDeducteeType());
                        obj.put("manufacturertype", customervendormapping.getVendoraccountid().getManufacturerType());
                    }
                }
                obj.put("iecno", !StringUtil.isNullOrEmpty(customer.getIECNo()) ? customer.getIECNo() : "");
                obj.put("commissionerate", !StringUtil.isNullOrEmpty(customer.getCommissionerate()) ? customer.getCommissionerate() : "");
                obj.put("defaultnatureofpurchase", !StringUtil.isNullOrEmpty(customer.getDefaultnatureOfPurchase()) ? customer.getDefaultnatureOfPurchase() : "");
                obj.put("division", !StringUtil.isNullOrEmpty(customer.getDivision()) ? customer.getDivision() : "");
                obj.put("importereccno", !StringUtil.isNullOrEmpty(customer.getImporterECCNo()) ? customer.getImporterECCNo() : "");
                obj.put("range", !StringUtil.isNullOrEmpty(customer.getRangecode()) ? customer.getRangecode() : "");
                obj.put("productname", tempstring);
                obj.put("productcode", productsCode);
                obj.put("seztodate",customer.getSezToDate()!=null?authHandler.getDateOnlyFormat().format(customer.getSezToDate()):"");
                obj.put("sezfromdate",customer.getSezFromDate()!=null?authHandler.getDateOnlyFormat().format(customer.getSezFromDate()):"");
                obj.put("prodname", productsName);
                obj.put("productid", tempstring);
                obj.put("deliveryDate", customer.getDeliveryDate());
                obj.put("deliveryTime", customer.getDeliveryTime());
                obj.put("vehicleNo", (customer.getVehicleNo() != null) ? customer.getVehicleNo().getValue() : "");
                obj.put("vehicleNoID", (customer.getVehicleNo() != null) ? customer.getVehicleNo().getID() : "");
                obj.put("driver", (customer.getDriver() != null) ? customer.getDriver().getValue() : "");
                obj.put("driverID", (customer.getDriver() != null) ? customer.getDriver().getID() : "");
                }
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("customerid", customer.getID());
                addrRequestParams.put("companyid", companyid);
                KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
                if (!addressResult.getEntityList().isEmpty()) {
                    JSONArray addrArray = new JSONArray();
                    List<CustomerAddressDetails> casList = addressResult.getEntityList();
                    for (CustomerAddressDetails cas : casList) {
                        JSONObject addrObject = new JSONObject();
                        addrObject.put("aliasName", cas.getAliasName() != null ? cas.getAliasName() : "");
                        addrObject.put("address", cas.getAddress() != null ? cas.getAddress() : "");
                        addrObject.put(Constants.Acc_id, cas.getID() != null ? cas.getID() : "");
                        addrObject.put("county", cas.getCounty() != null ? cas.getCounty() : "");
                        addrObject.put("city", cas.getCity() != null ? cas.getCity() : "");
                        addrObject.put("state", cas.getState() != null ? cas.getState() : "");
                        addrObject.put("stateCode", cas.getStateCode() != null ? cas.getStateCode() : "");
                        addrObject.put("country", cas.getCountry() != null ? cas.getCountry() : "");
                        addrObject.put("postalCode", cas.getPostalCode() != null ? cas.getPostalCode() : "");
                        addrObject.put("phone", cas.getPhone() != null ? cas.getPhone() : "");
                        addrObject.put("mobileNumber", cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                        addrObject.put("fax", cas.getFax() != null ? cas.getFax() : "");
                        addrObject.put("emailID", cas.getEmailID() != null ? cas.getEmailID() : "");
                        addrObject.put("contactPerson", cas.getContactPerson() != null ? cas.getContactPerson() : "");
                        addrObject.put("recipientName", cas.getRecipientName() != null ? cas.getRecipientName() : "");
                        addrObject.put("contactPersonNumber", cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");
                        addrObject.put("contactPersonDesignation", cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : "");
                        addrObject.put("website", cas.getWebsite() != null ? cas.getWebsite() : "");
                        addrObject.put("shippingRoute", cas.getShippingRoute() != null ? cas.getShippingRoute() : "");
                        addrObject.put("isDefaultAddress", cas.isIsDefaultAddress());
                        addrObject.put("isBillingAddress", cas.isIsBillingAddress());
                        addrArray.put(addrObject);

                        //below address details used in exporting customer with all column option
                        //below key is set in column "dataindex" of default header table. so please do not chanage them.    
                        if (cas.isIsDefaultAddress() && cas.isIsBillingAddress()) {//for defult billing address
                            obj.put("billingAliasName", cas.getAliasName() != null ? cas.getAliasName() : "");
                            obj.put("billingAddress", cas.getAddress() != null ? cas.getAddress() : "");
                            obj.put("billingCounty", cas.getCounty() != null ? cas.getCounty() : "");
                            obj.put("billingCity", cas.getCity() != null ? cas.getCity() : "");
                            obj.put("billingState", cas.getState() != null ? cas.getState() : "");
                            obj.put("billingStateCode", cas.getStateCode() != null ? cas.getStateCode() : "");
                            obj.put("billingCountry", cas.getCountry() != null ? cas.getCountry() : "");
                            obj.put("billingPostalCode", cas.getPostalCode() != null ? cas.getPostalCode() : "");
                            obj.put("billingPhone", cas.getPhone() != null ? cas.getPhone() : "");
                            obj.put("billingMobileNumber", cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                            obj.put("billingFax", cas.getFax() != null ? cas.getFax() : "");
                            obj.put("billingEmailID", cas.getEmailID() != null ? cas.getEmailID() : "");
                            if (!StringUtil.isNullOrEmpty(cas.getEmailID())) {
                                obj.put("email", cas.getEmailID().replace(',', ';'));
                            } else {
                                obj.put("email", "");
                            }
                            obj.put("billingRecipientName", cas.getRecipientName() != null ? cas.getRecipientName() : "");
                            obj.put("billingContactPerson", cas.getContactPerson() != null ? cas.getContactPerson() : "");
                            obj.put("billingContactPersonNumber", cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");
                            obj.put("billingContactPersonDesignation", cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : "");
                            obj.put("billingWebsite", cas.getWebsite() != null ? cas.getWebsite() : "");
                        } else if (cas.isIsDefaultAddress()) { //for defult shipping address
                            obj.put("shippingAliasName", cas.getAliasName() != null ? cas.getAliasName() : "");
                            obj.put("shippingAddress", cas.getAddress() != null ? cas.getAddress() : "");
                            obj.put("shippingCounty", cas.getCounty() != null ? cas.getCounty() : "");
                            obj.put("shippingCity", cas.getCity() != null ? cas.getCity() : "");
                            obj.put("shippingState", cas.getState() != null ? cas.getState() : "");
                            obj.put("shippingStateCode", cas.getStateCode() != null ? cas.getStateCode() : "");
                            obj.put("shippingCountry", cas.getCountry() != null ? cas.getCountry() : "");
                            obj.put("shippingPostalCode", cas.getPostalCode() != null ? cas.getPostalCode() : "");
                            obj.put("shippingPhone", cas.getPhone() != null ? cas.getPhone() : "");
                            obj.put("shippingMobileNumber", cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                            obj.put("shippingFax", cas.getFax() != null ? cas.getFax() : "");
                            obj.put("shippingEmailID", cas.getEmailID() != null ? cas.getEmailID() : "");
                            obj.put("shippingRecipientName", cas.getRecipientName() != null ? cas.getRecipientName() : "");
                            obj.put("shippingContactPerson", cas.getContactPerson() != null ? cas.getContactPerson() : "");
                            obj.put("shippingContactPersonNumber", cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");
                            obj.put("shippingContactPersonDesignation", cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : "");
                            obj.put("shippingWebsite", cas.getWebsite() != null ? cas.getWebsite() : "");
                            if (!StringUtil.isNullOrEmpty(cas.getShippingRoute())) {
                                KwlReturnObject taxresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), cas.getShippingRoute());
                                MasterItem item = (MasterItem) taxresult.getEntityList().get(0);
                                obj.put("shippingRoute", item != null ? item.getValue() : "");
                            } else {
                                obj.put("shippingRoute", "");
                            }
                        }
                    }
                    obj.put("addressDetails", addrArray);
                }
                
                Map<String, Object> variableMap = new HashMap<String, Object>();
                KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(CustomerCustomData.class.getName(), customer.getID());
                replaceFieldMap = new HashMap<String, String>();
                if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                    CustomerCustomData jeDetailCustom = (CustomerCustomData) custumObjresult.getEntityList().get(0);
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("isExport", isExport);
                        accountingCommonfieldDataManage.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                }

              
                requestParams.remove(Constants.ss); //search string unnessesary getting added in query and resulting as wrong amount calculation
                requestParams.put("nondeleted", "true");
                requestParams.put("deleted", "false");
                if (!storageHandlerImpl.GetLowercaseCompanyId().contains(companyid)) {
                    requestParams.remove("searchJson");
                    if (isExport &&(headerlist.contains("exceededamount")|| headerlist.contains("amountdue"))) {
                        getCustomerAmountDueByObject(obj, paramJobj, requestParams);
                    }else if(!isExport){
                         getCustomerAmountDueByObject(obj, paramJobj, requestParams);
                    }
                  if (paramJobj.optString("custAmountDueMoreThanLimit",null) != null && Boolean.parseBoolean(paramJobj.getString("custAmountDueMoreThanLimit"))) {
                        if (obj.optDouble(Constants.limit, 0.0) < obj.optDouble("amountdue", 0.0)) {
                            obj.put("exceededamount", authHandler.round((obj.optDouble("amountdue", 0.0) - obj.optDouble(Constants.limit, 0.0)), companyid));
                            jArr.put(obj);
                        }
                    } else {
                        obj.put("exceededamount", 0.0);
                        jArr.put(obj);
                    }
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCustomerJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }    
  
  @Override 
    public JSONArray getCustomerAmountDue(JSONArray jArr, JSONObject paramJobj) {
        try {
            HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMapJson(paramJobj);

            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(Constants.ss, null);
            for (int i = 0; i < jArr.length(); i++) {
                getCustomerAmountDueByObject(jArr.getJSONObject(i), paramJobj, requestParams);
            }

        } catch (Exception ex) {
            Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    /*
            Moved Method to service  layer
    */
    
      public JSONObject getCustomerExceedingCreditLimit(JSONObject paramJObj) {

        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            Customer customer=null;
            /**
             * This function is called at the time of approval of SO, SO can be
             * approved from Dashboard pending report or SO Pending report. 
             * Case 1: user approves from Dashboard pending report then totalSUM i.e
             * SO Total Amount is not passed in parameter so setting it in
             * request Attribute we cannot pass it in parameter as it is not
             * available on JS in store and here checking it weather it is null
             * or not ERP-38444. 
             * Case 2: user approves from SO pending report
             * then totalSUM i.e SO Total Amount is is passed in parameter and
             * here checking it weather it is null or not ERP-38444.
             */
            if (paramJObj.optString("customer") != null && (!StringUtil.isNullOrEmpty(paramJObj.optString("totalSUM")) || !StringUtil.isNullObject(paramJObj.optString("totalSUM"))))
            {
                KwlReturnObject cstresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJObj.optString("customer"));
                customer = (Customer) cstresult.getEntityList().get(0);

                JSONObject obj = new JSONObject();
//                obj.put("accid", customer.getAccount().getID());
                obj.put("accid", customer.getID());             //SDP-12235
                obj.put("custId", customer.getID());
                obj.put("custName", customer.getName());
                jArr.put(obj);
                double totalAmountDue=0;
                JSONArray customerAmountDueJArr = getCustomerAmountDue(jArr, paramJObj);
                double amountDue=customerAmountDueJArr.getJSONObject(0).getDouble("amountdue");
                String soAmount = StringUtil.isNullOrEmpty(paramJObj.optString("totalSUM")) ? paramJObj.optString("totalSUM").toString() : paramJObj.optString("totalSUM");
                double totalSUM=0;
                KwlReturnObject venresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), customer.getCompany().getCompanyID());
                if (venresult.getEntityList().get(0) != null) {
                    /*
                    Include amount enter in form in the limit calculation based on settings
                    */
                    ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) venresult.getEntityList().get(0);
                    boolean isOrder = !StringUtil.isNullOrEmpty(paramJObj.optString("isOrder")) ? Boolean.parseBoolean(paramJObj.optString("isOrder")) : true;
                    if (extraCompanyPreferences != null) {
                        if ((isOrder && extraCompanyPreferences.isIncludeAmountInLimitSO()) || (!isOrder && extraCompanyPreferences.isIncludeAmountInLimitSI())) {
                            totalSUM = Double.parseDouble(soAmount);
                        }
                    }
                } else {
                    totalSUM = Double.parseDouble(soAmount);
                }

                totalAmountDue=amountDue+totalSUM;
                double fixedCreditLimit=customer.getCreditlimit();

                if(totalAmountDue > fixedCreditLimit){
                    JSONObject jTemp = new JSONObject();
                    jTemp.put("name", customer.getName());
                    jTemp.put("amountDue",amountDue);
                    jTemp.put("limitflag","true");
                    jTemp.put("limit", customer.getCreditlimit());
                    jTemp.put("totalAmountDueOfCustomer", totalAmountDue);
                    jobj.append(Constants.data, jTemp);
                }
            }
        }catch (JSONException ex) {
             Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    
 @Override    
    public JSONObject getCustomerAmountDueByObject(JSONObject jSONObject, JSONObject paramJobj, HashMap<String, Object> requestParams) throws JSONException, ServiceException, SessionExpiredException {
        String currencyid = paramJobj.optString(Constants.globalCurrencyKey, "6");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        String companyId = paramJobj.getString(Constants.companyKey);
        String accid = jSONObject.getString("accid");
        /*
         * Added this parameter because many of the traansactions use this
         * parameter for seaching the records usgin this parameter as Vendor Id
         * / Customer Id
         */
        requestParams.put("custVendorID", accid);
        String custId = "";
        if (jSONObject.has("custId")) {
            custId = jSONObject.getString("custId");
        }
        requestParams.put(InvoiceConstants.accid, accid);
        requestParams.put(InvoiceConstants.customerid, custId);
        if (Boolean.parseBoolean(paramJobj.optString("isCallFromApproveSalesOrder", "false"))) {
             requestParams.put(InvoiceConstants.billid, "");
        }
        if (jSONObject.has("startdate")) {
            requestParams.put(Constants.REQ_startdate, jSONObject.get("startdate").toString());
        }
        if (jSONObject.has("enddate")) {
            requestParams.put(Constants.REQ_enddate, jSONObject.get("enddate").toString());
        }
        requestParams.put("includeFixedAssetInvoicesFlag", true);

        double amountdue = 0;
        double newBaseAmount = 0;
        double baseamountdue = 0;
        double baseamountduenew = 0;
        KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
        if (result.getEntityList() != null) {
            List<Invoice> invoiceList = result.getEntityList();
            for (Invoice invoice : invoiceList) {
                if (Constants.InvoiceAmountDueFlag) {
                    List ll = accInvoiceCMNobj.getInvoiceDiscountAmountInfo(requestParams, invoice);
                    amountdue = amountdue + (Double) ll.get(0);
                    newBaseAmount = (Double) ll.get(0);
                } else {
                    List ll = accInvoiceCMNobj.getAmountDue_Discount(requestParams, invoice);
                    amountdue = amountdue + (Double) ll.get(0);
                    newBaseAmount = (Double) ll.get(0);
                }
                String fromcurrencyid = invoice.getCurrency().getCurrencyID();
//                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                baseamountduenew = authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                baseamountdue = baseamountdue + baseamountduenew;
            }
            jSONObject.put("amountdue", baseamountdue);
        } else {
            jSONObject.put("amountdue", 0);
        }
        requestParams.put("cntype", null);//cntype after each loop become 4 so need to update with null
        result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);//This is used for getting CN gainst customer and otherwise 
        if (result.getEntityList() != null) {
            List< Object[]> listObj= result.getEntityList();
            for (Object[] row:listObj) {
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                if (!withoutinventory) {
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
                    if (creditNote.isOtherwise() && creditNote.getCnamountdue() > 0) {
                        double cnamountdue = creditNote.isOtherwise() ? creditNote.getCnamountdue() : 0;
                        double exchangerate = creditNote.getJournalEntry() != null ? creditNote.getJournalEntry().getExternalCurrencyRate() : 0;
//                        Date creationdate = creditNote.getJournalEntry().getEntryDate();
                        Date creationdate = creditNote.getCreationDate();
                        String cncurrencyid = creditNote.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnamountdue, cncurrencyid, creationdate, exchangerate);
                        double cnamountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                        baseamountdue -= cnamountdueinbase;//In case of Credit note amount is subtracted
                    }
                }
            }
        }
        requestParams.put("cntype", 4);   //This is used for getting DN gainst customer
        result = accDebitNoteobj.getDebitNoteMerged(requestParams);
        if (result.getEntityList() != null) {
            List< Object[]> listObj= result.getEntityList();
            for (Object[] row :listObj) {
                boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                if (!withoutinventory) {
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                    DebitNote debitMemo = (DebitNote) resultObject.getEntityList().get(0);
                    if ((debitMemo.isOtherwise() || debitMemo.getDntype()==5) && debitMemo.getDnamountdue() > 0) {              //debitMemo.getDntype()==5 In case of debit note against customer for malaysian country
                        double dnamountdue = (debitMemo.isOtherwise() || debitMemo.getDntype()==5)? debitMemo.getDnamountdue() : 0;
                        double exchangerate = debitMemo.getJournalEntry() != null ? debitMemo.getJournalEntry().getExternalCurrencyRate() : 0;
//                        Date creationdate = debitMemo.getJournalEntry().getEntryDate();
                        Date creationdate = debitMemo.getCreationDate();
                        String dncurrencyid = debitMemo.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, dnamountdue, dncurrencyid, creationdate, exchangerate);
                        double dnamountdueinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyId);
                        baseamountdue += dnamountdueinbase;//In case of debit note amount is Added
                    }
                }
            }
        }

        //TO DO- when we allow partial receive payment then we need to change the logic for advance used

//                    requestParams.put("isadvancepayment", true); //Only Advance payment type make payments required. so it is true 
        result = accReceiptDAOobj.getReceipts(requestParams);
        if (result.getEntityList() != null) {
            List< Object[]> listObj=result.getEntityList();
            for (Object[] row:listObj) {
                Receipt receipt = (Receipt) row[0];
                String mpcurrencyid = receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID();
                if (receipt != null && receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) {
                    for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
//                        baseamountdue -= authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), mpcurrencyid, receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate()).getEntityList().get(0), companyId);
                        baseamountdue -= authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), mpcurrencyid, receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate()).getEntityList().get(0), companyId);
                    }
                }
            }
        }

        //ERP-10304 - Calculating refund paid to customer
        result = accPaymentDAOobj.getPaymentsForCustomer(accid, companyId);
        if (result.getEntityList() != null) {
            List<Payment> list=result.getEntityList();
            for (Payment payment:list) {
                Set<AdvanceDetail> advDetail = payment.getAdvanceDetails();
                for (AdvanceDetail adv : advDetail) {
                    if (adv.getReceiptAdvanceDetails() == null) {
//                        baseamountdue += authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, adv.getAmountDue(), payment.getCurrency().getCurrencyID(), payment.getJournalEntry().getEntryDate(), payment.getJournalEntry().getExternalCurrencyRate()).getEntityList().get(0), companyId);
                        baseamountdue += authHandler.round((Double) accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, adv.getAmountDue(), payment.getCurrency().getCurrencyID(), payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate()).getEntityList().get(0), companyId);
                    }
                }
            }
        }

        // putting opening amount of customer
        double openbalanceAmtDueOfCustomer = accInvoiceCMNobj.getOpeningBalanceAmountDueOfAccount(paramJobj, null, true, custId);
        baseamountdue = baseamountdue + openbalanceAmtDueOfCustomer;
        jSONObject.put("amountdue", baseamountdue);
        return jSONObject;
    }
   
  @Override  
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
                Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "");
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return valuesStr;
    }
    
  @Override    
    public String[] getMultiSalesPersonIDs(String customerid) {//fetching masteritem mapped to that customer.
        JSONObject jobj = new JSONObject();
        String[] valuesStr = {"", ""};
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
                Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "");
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return valuesStr;
    }
    
  @Override       
    public  HashMap<String, Object> getCustomerRequestMapJSON(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        String[] groups= new String[100];
        if (paramJobj.has("group") && paramJobj.get("group") != null) {
            String groupString=(String)paramJobj.get("group");
            groups = groupString.split(",");
        }
        String[] groupsAfterAdding = groups;
        //To do - No depedndecy on accounts in customer and vendor.
        requestParams.put("group", groupsAfterAdding);
        requestParams.put("ignore", paramJobj.optString("ignore",null));
        requestParams.put("ignorecustomers", paramJobj.optString("ignorecustomers",null));
        requestParams.put("ignorevendors", paramJobj.optString("ignorevendors",null));
        if (paramJobj.optString("accountid",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("accountid",null))) {
            requestParams.put("accountid", paramJobj.getString("accountid"));
        }

        requestParams.put("deleted", paramJobj.optString("deleted",null));
        requestParams.put("nondeleted", paramJobj.optString("nondeleted",null));
        if (paramJobj.optString("query",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("query",null))) {
            requestParams.put(Constants.ss, paramJobj.optString("query",null));
        } else if (paramJobj.optString(Constants.ss,null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.ss,null))) {
            requestParams.put(Constants.ss, paramJobj.getString(Constants.ss));
        }

        if (StringUtil.isNullOrEmpty(paramJobj.optString("filetype",null))) {
            if (paramJobj.optString("start",null) != null) {
                requestParams.put("start", paramJobj.getString("start"));
            }
            if (paramJobj.optString(Constants.limit,null) != null) {
                requestParams.put(Constants.limit, paramJobj.getString(Constants.limit));
            }
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.REQ_startdate,null))) {
            requestParams.put(Constants.REQ_startdate, paramJobj.getString(Constants.REQ_startdate));
        }
        if (paramJobj.optString("comboCurrencyid",null) != null) {
            requestParams.put("comboCurrencyid", paramJobj.getString("comboCurrencyid"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isPermOrOnetime",null))) {
            requestParams.put("isPermOrOnetime", Boolean.FALSE.parseBoolean(paramJobj.getString("isPermOrOnetime")));
        }
        if (paramJobj.optString("receivableAccFlag",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("receivableAccFlag",null))) {
            requestParams.put("receivableAccFlag", paramJobj.getString("receivableAccFlag"));
        }
        if (paramJobj.optString("selectedCustomerIds",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("selectedCustomerIds",null))) {
            requestParams.put("selectedCustomerIds", paramJobj.getString("selectedCustomerIds"));
        }
        if (paramJobj.optString("activeDormantFlag",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("activeDormantFlag",null))) {
            requestParams.put("activeDormantFlag", paramJobj.getString("activeDormantFlag"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("dir",null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("sort",null))) {
            requestParams.put("dir", paramJobj.getString("dir"));
            requestParams.put("sort", paramJobj.getString("sort"));
        }
         ExtraCompanyPreferences extraPref=null;
         if (paramJobj.has(Constants.companyKey) && paramJobj.get(Constants.companyKey) != null) {
             String companyid=paramJobj.getString(Constants.companyKey);
             KwlReturnObject prefRes = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
             extraPref = (ExtraCompanyPreferences) prefRes.getEntityList().get(0);
           }
         if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                 int permCode=0;
                 if (paramJobj.has(Constants.PermCode_Customer) && paramJobj.get(Constants.PermCode_Customer) != null) {
                     permCode = Integer.parseInt(paramJobj.getString(Constants.PermCode_Customer));
                 }
                 String userId ="";
                 if (paramJobj.has(Constants.useridKey) && paramJobj.get(Constants.useridKey) != null) {
                     userId = paramJobj.getString(Constants.useridKey);
                 }
                 String userRoleID="";
                 if (paramJobj.has(Constants.roleid) && paramJobj.get(Constants.roleid) != null) {
                     userRoleID = paramJobj.getString(Constants.roleid);
                 }
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
        
        requestParams.put(Constants.currencyKey, paramJobj.optString(Constants.globalCurrencyKey, null));
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json,null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(Constants.Filter_Criteria,null));
        requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));
        requestParams.put(Constants.sortstring, paramJobj.optString(Constants.sortstring,null));
        requestParams.put(Constants.customerid, paramJobj.optString(Constants.customerid,null));
        return requestParams;
    }
  
    @Override
    public JSONObject saveCustomerCheckInOut(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm");
        try {
            boolean ischeckin = paramJobj.optBoolean("ischeckin",false);

            result = accCustomerDAOobj.getCustomerCheckIn(paramJobj);
            List<CustomerCheckInOut> checkinList = result.getEntityList();
            
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));            
            if (ischeckin) {
                if (checkinList.size() > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String checkInDate = sdf.format(checkinList.get(0).getCheckintime());
                    jobj.put(Constants.RES_MESSAGE, "You have already checked in on " + checkInDate + ".Please give check out time and check in again.");
                    jobj.put("id", checkinList.get(0).getId());
                } else {
                    result = accCustomerDAOobj.saveCustomerCheckIn(paramJobj);
                    checkinList = result.getEntityList();
                    jobj.put(Constants.RES_MESSAGE, checkinList.get(0).getCustomer().getName() + " has checked in successfully.");
                    jobj.put("id", checkinList.get(0).getId());                    
                    String checkInTime = timeFormat.format(checkinList.get(0).getCheckintime());
                    auditTrailObj.insertAuditLog(AuditAction.USER_CHECKIN, "" + checkinList.get(0).getCustomer().getName() + "  has checked in successfully at " + checkInTime, auditRequestParams, checkinList.get(0).getId());
                }
            } else {
                if (checkinList.size() > 0) {
                    paramJobj.put("id", checkinList.get(0).getId());
                    result = accCustomerDAOobj.saveCustomerCheckOut(paramJobj);
                    jobj.put(Constants.RES_MESSAGE, checkinList.get(0).getCustomer().getName() + " has checked out successfully.");
                    jobj.put("id", checkinList.get(0).getId());                    
                    String checkOutTime = timeFormat.format(checkinList.get(0).getCheckouttime());
                    auditTrailObj.insertAuditLog(AuditAction.USER_CHECKOUT, "" + checkinList.get(0).getCustomer().getName() + "  has checked out successfully at " + checkOutTime, auditRequestParams, checkinList.get(0).getId());
                } else {
                    jobj.put(Constants.RES_MESSAGE, "Check in entry is not present for this check out.");
                }
            }
//            List<CustomerCheckInOut> list = result.getEntityList();
//            if (result.getRecordTotalCount() > 0) {
//                jobj.put("id", list.get(0).getId());
//            }
            jobj.put(Constants.RES_success, result.isSuccessFlag());
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject getCustomerCheckIn(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        JSONArray data = new JSONArray();

        result = accCustomerDAOobj.getCustomerCheckIn(paramJobj);
        List<CustomerCheckInOut> checkinList = result.getEntityList();
        
        if (checkinList.size() > 0) {
            for (CustomerCheckInOut checkIn : checkinList) {
                JSONObject obj = new JSONObject();
                obj.put("id", checkIn.getId());
                obj.put("customername", checkIn.getCustomer().getName());
                obj.put("customerid", checkIn.getCustomer().getID());
                obj.put("checkintime", checkIn.getCheckintime());
//                obj.put("checkinby", checkIn.getCheckinby());
                obj.put("location", checkIn.getLocation());
                data.put(obj);
            }
            jobj.put("ischeckedin", true);
        }else{
            jobj.put("ischeckedin", false);
        }

        jobj.put(Constants.RES_data, data);
         jobj.put(Constants.RES_success, result.isSuccessFlag());
        return jobj;
    }
    
    @Override
    public  JSONObject getCustomerCheckInCheckOutRegistryGridInfo(JSONObject paramJobj) throws JSONException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        Locale requestcontextutilsobj = null;
        
        if (paramJobj.has("requestcontextutilsobj")) {
            requestcontextutilsobj = (Locale) paramJobj.get("requestcontextutilsobj");
        }

        String StoreRec = "customerid,customername,checkintime,checkouttime,location,inlatitude,inlongitude,outlatitude,outlongitude";
        String[] recArr = StoreRec.split(",");
        for (String rec : recArr) {
            jobjTemp = new JSONObject();
            jobjTemp.put("name", rec);
            jarrRecords.put(jobjTemp);
        }
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.chechInCheckOutReport.gridHeader.customerId", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "customerid");
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("sortable", true);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.chechInCheckOutReport.gridHeader.customerName", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "customername");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.chechInCheckOutReport.gridHeader.checkInTime", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "checkintime");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.chechInCheckOutReport.gridHeader.checkOutTime", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "checkouttime");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

//        jobjTemp = new JSONObject();
//        jobjTemp.put("header", messageSource.getMessage("acc.report.chechInCheckOutReport.gridHeader.location", null, requestcontextutilsobj)); 
//        jobjTemp.put("dataIndex", "location");
//        jobjTemp.put("align", "left");
//        jobjTemp.put("pdfwidth", 75);
//        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.common.gridHeader.inLatitude", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "inlatitude");
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.common.gridHeader.inLongitude", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "inlongitude");
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.common.gridHeader.outLatitude", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "outlatitude");
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.common.gridHeader.outLongitude", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "outlongitude");
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jMeta.put("totalProperty", "totalCount");
        jMeta.put("root", "coldata");
        jMeta.put("fields", jarrRecords);
        commData.put("columns", jarrColumns);
        commData.put("metadata", jMeta);
        return commData;
    }
    
    @Override
    public  JSONObject getIncidentCasesRegistryGridInfo(JSONObject paramJobj) throws JSONException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        Locale requestcontextutilsobj = null;
        
        if (paramJobj.has("requestcontextutilsobj")) {
            requestcontextutilsobj = (Locale) paramJobj.get("requestcontextutilsobj");
        }

        String StoreRec = "customerid,customername,incidentdate,incidenttime,productid,productname,description,resolution,location,latitude,longitude";
        String[] recArr = StoreRec.split(",");
        for (String rec : recArr) {
            jobjTemp = new JSONObject();
            jobjTemp.put("name", rec);
            jarrRecords.put(jobjTemp);
        }
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.chechInCheckOutReport.gridHeader.customerId", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "customerid");
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("sortable", true);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.chechInCheckOutReport.gridHeader.customerName", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "customername");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.incidentCasesReport.gridHeader.incidentDate", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "incidentdate");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.incidentCasesReport.gridHeader.incidentTime", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "incidenttime");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.productList.gridProductID", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "productid");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.rem.prodName", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "productname");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.je.desc", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "description");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.incidentCasesReport.gridHeader.resolution", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "resolution");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

//        jobjTemp = new JSONObject();
//        jobjTemp.put("header", messageSource.getMessage("acc.report.chechInCheckOutReport.gridHeader.location", null, requestcontextutilsobj)); 
//        jobjTemp.put("dataIndex", "location");
//        jobjTemp.put("align", "left");
//        jobjTemp.put("pdfwidth", 75);
//        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.common.gridHeader.Latitude", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "latitude");
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.report.common.gridHeader.Longitude", null, requestcontextutilsobj)); 
        jobjTemp.put("dataIndex", "longitude");
        jobjTemp.put("align", "left");
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        
        jMeta.put("totalProperty", "totalCount");
        jMeta.put("root", "coldata");
        jMeta.put("fields", jarrRecords);
        commData.put("columns", jarrColumns);
        commData.put("metadata", jMeta);
        return commData;
    }
    
    @Override
    public JSONObject getCustomerCheckInandCheckOutDetails(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        JSONArray data = new JSONArray();
        JSONObject commData = new JSONObject();
        data= createCheckInCheckOutJSON(paramJobj);
        commData.put("coldata", data);
        commData.put("totalCount", data.length());
        jobj.put(Constants.RES_data, commData);
        return jobj;
    }
    
    @Override
    public JSONObject getIncidentCasesDetails(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONArray data = new JSONArray();
        JSONObject commData = new JSONObject();
        data= createIncidentCasesDetailJSON(paramJobj);
        commData.put("coldata", data);
        commData.put("totalCount", data.length());
        jobj.put(Constants.RES_data, commData);
        return jobj;
    }
    
    @Override
    public JSONArray createCheckInCheckOutJSON(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        KwlReturnObject result = null;
        JSONArray data = new JSONArray();
        try {
            paramJobj.put("allcheckinandout", true);
            result = accCustomerDAOobj.getCustomerCheckIn(paramJobj);
            List<CustomerCheckInOut> checkinList = result.getEntityList();
            String checkintime = "";
            String checkouttime = "";
            if (checkinList.size() > 0) {
                for (CustomerCheckInOut checkIn : checkinList) {
                    checkouttime="";
                    checkintime = "";
                    JSONObject obj = new JSONObject();
                    obj.put("id", checkIn.getId());
                    obj.put("customername", checkIn.getCustomer().getName());
                    obj.put("customerid", checkIn.getCustomer().getAcccode());
                    if (checkIn.getCheckintime() != null) {
                        checkintime = authHandler.getGlobalDateFormat().format(checkIn.getCheckintime());
                    }
                    if (checkIn.getCheckouttime() != null) {
                        checkouttime = authHandler.getGlobalDateFormat().format(checkIn.getCheckouttime());
                    }
                    obj.put("checkintime", checkintime);
                    obj.put("checkouttime", checkouttime);
                    obj.put("location", StringUtil.isNullOrEmpty(checkIn.getLocation()) ? "" : checkIn.getLocation());
                    obj.put("inlatitude", StringUtil.isNullOrEmpty(checkIn.getInLatitude())?"":checkIn.getInLatitude());
                    obj.put("inlongitude", StringUtil.isNullOrEmpty(checkIn.getInLongitude())?"":checkIn.getInLongitude());
                    obj.put("outlatitude", StringUtil.isNullOrEmpty(checkIn.getOutLatitude())?"":checkIn.getOutLatitude());
                    obj.put("outlongitude", StringUtil.isNullOrEmpty(checkIn.getOutLongitude())?"":checkIn.getOutLongitude());
                    data.put(obj);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return data;
    }
  
    @Override
    public JSONArray createIncidentCasesDetailJSON(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray dataArr = new JSONArray();
        JSONObject dataObj = null;
        KwlReturnObject resultObj = null;
        DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
        try {
            paramJobj.put("fromIncidentCaseReport", true);
            resultObj = accProductObj.getIncidentCase(paramJobj);
            List<IncidentCases> list = resultObj.getEntityList();
            
            for (IncidentCases incident : list) {
                dataObj = new JSONObject();
                dataObj.put("id", incident.getID());
                dataObj.put(Constants.customerid, incident.getCustomer() != null ? incident.getCustomer().getAcccode() : "");
                dataObj.put("customername", incident.getCustomer() != null ? incident.getCustomer().getName() : "");
                if (incident.getIncidentDate() != null) {
                    dataObj.put("incidentdate", userdf.format(incident.getIncidentDate()));
                }
                dataObj.put("incidenttime", incident.getIncidenttime());
                dataObj.put("productid", incident.getProduct().getProductid());
                dataObj.put("productname", incident.getProduct().getName());
                dataObj.put("description", incident.getDescription());
                dataObj.put("resolution", incident.getResolution());
                dataObj.put("location", incident.getLocation());
                dataObj.put("latitude", StringUtil.isNullOrEmpty(incident.getLatitude())?"":incident.getLatitude());
                dataObj.put("longitude", StringUtil.isNullOrEmpty(incident.getLongitude())?"":incident.getLongitude());
                dataArr.put(dataObj);
            }

        } catch (Exception ex) {
            Logger.getLogger(AccCustomerMainAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataArr;
    }
  
}//class
