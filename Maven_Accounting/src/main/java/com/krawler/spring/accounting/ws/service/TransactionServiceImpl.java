/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.defaultfieldsetup.AccFieldSetupServiceDao;
import com.krawler.documentdesigner.AccDocumentDesignService;
import com.krawler.esp.servlets.RemoteAPI;

import static com.krawler.esp.servlets.RemoteAPI.getMessage;

import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteService;
import com.krawler.spring.accounting.creditnote.accCreditNoteServiceCMN;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignServiceDao;
import com.krawler.spring.accounting.customreports.AccCustomReportService;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceHandler;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.payment.accPaymentImpl;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.purchaseorder.AccPurchaseOrderServiceDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.receipt.AccReceiptServiceDAO;
import com.krawler.spring.accounting.reports.AccReportsService;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderService;
import com.krawler.spring.accounting.salesreturn.AccSalesReturnService;
import com.krawler.spring.accounting.vendorpayment.service.AccVendorPaymentModuleService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.FieldManagerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.mainaccounting.service.AccMainAccountingService;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;

import com.krawler.spring.exportFuctionality.AccExportReportsServiceDAO;
import com.krawler.esp.handlers.APICallHandlerService;
import java.io.ByteArrayOutputStream;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleService;
import com.krawler.spring.accounting.journalentry.AccJournalEntryService;
import com.krawler.spring.accounting.pos.AccPOSInterfaceDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.receivepayment.service.AccReceivePaymentModuleService;
import com.krawler.spring.accounting.vendorpayment.AccVendorPaymentServiceDAO;
import com.krawler.spring.common.*; 
import org.springframework.context.MessageSource;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class TransactionServiceImpl implements TransactionService {

    private accPaymentImpl accPaymentDAOobj;

    private AccountingHandlerDAO accountingHandlerDAOobj;

    private accAccountDAO accAccountDAOobj;

    private accMasterItemsDAO accMasterItemsDAOobj;

    private accCurrencyDAO accCurrencyDAOobj;

    private accJournalEntryDAO accJournalEntryobj;

    private accInvoiceDAO accInvoiceDAOobj;

    private accGoodsReceiptModuleService accGoodsReceiptModuleService;
    
    private AccInvoiceModuleService accInvoiceModuleService;

    private accGoodsReceiptDAO accGoodsReceiptobj;

    private AccMainAccountingService accMainAccountingService;

    private AccJournalEntryModuleService accJournalEntryModuleService;

    private AccReceiptServiceDAO accReceiptServiceDAO;

    private AccInvoiceServiceDAO accInvoiceServiceDAO;

    private profileHandlerDAO profileHandlerDAOObj;

    private accSalesOrderDAO accSalesOrderDAOobj;
    
    private accPurchaseOrderDAO accPurchaseOrderobj;
    
    private AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj;

    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;

    private AccReportsService accReportsServiceObj;

    private WSUtilService wsUtilService;
    
    private FieldManagerService fieldManagerServiceobj;

    private accSalesOrderService accSalesOrderServiceobj;
    
    private AccCustomReportService accCustomReportService;
    
    private AccProductService AccProductService;
    
    private fieldDataManager fieldDataManagercntrl;
    
    private AccSalesReturnService accSalesReturnService;
    
    private accCreditNoteService accCreditNoteService;
        
    private accCreditNoteServiceCMN accCreditNoteServiceCMN;
    
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    
    private AccFieldSetupServiceDao accFieldSetUpServiceDAOObj;
    
    private AccVendorPaymentModuleService accVendorPaymentModuleServiceObj;
    
    private CustomDesignServiceDao customDesignServiceobj;
    
    private AccDocumentDesignService accDocumentDesignService;
        
    private AccExportReportsServiceDAO accExportReportsServiceDAOobj;
    
    private APICallHandlerService apiCallHandlerService;
    
    private AccReceivePaymentModuleService accReceivePaymentModuleServiceObj;
    
    private AccJournalEntryService accJournalEntryService;
    
    private AccVendorPaymentServiceDAO accVendorPaymentServiceDAOobj;
    
    private AccCommonTablesDAO accCommonTablesDAO;
    
    private accCreditNoteDAO accCreditNoteDAOobj;
    
    private accReceiptDAO accReceiptDAOobj;
    
    private AccPOSInterfaceDAO accPOSInterfaceDAO;
    
    private MessageSource messageSource;
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public void setaccPOSInterfaceDAO(AccPOSInterfaceDAO accPOSInterfaceDAO) {
        this.accPOSInterfaceDAO = accPOSInterfaceDAO;
    }
    
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    
     public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
        
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    
    public void setaccVendorPaymentServiceDAO(AccVendorPaymentServiceDAO accVendorPaymentServiceDAOobj) {
        this.accVendorPaymentServiceDAOobj = accVendorPaymentServiceDAOobj;
    }
    
     /**
     * @param accReceivePaymentModuleServiceObj the
     * accReceivePaymentModuleServiceObj to set
     */
    public void setAccReceivePaymentModuleServiceObj(AccReceivePaymentModuleService accReceivePaymentModuleServiceObj) {
        this.accReceivePaymentModuleServiceObj = accReceivePaymentModuleServiceObj;
    }
    
    public AccJournalEntryService getAccJournalEntryService() {
        return accJournalEntryService;
    }

    public void setAccJournalEntryService(AccJournalEntryService accJournalEntryService) {
        this.accJournalEntryService = accJournalEntryService;
    }
    
    public void setaccExportReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
    
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
    public void setCustomDesignServiceobj(CustomDesignServiceDao customDesignServiceobj) {
        this.customDesignServiceobj = customDesignServiceobj;
    }
    
    public void setAccCustomReportService(AccCustomReportService accCustomReportService) {
        this.accCustomReportService = accCustomReportService;
    }
        
    public void setAccSalesOrderServiceobj(accSalesOrderService accSalesOrderServiceobj) {
        this.accSalesOrderServiceobj = accSalesOrderServiceobj;
    }
            
    public void setFieldManagerServiceobj(FieldManagerService fieldManagerServiceobj) {
        this.fieldManagerServiceobj = fieldManagerServiceobj;
    }

    public void setwsUtilService(WSUtilService wsUtilService) {
        this.wsUtilService = wsUtilService;
    }
    
    public void setaccReportsService(AccReportsService accReportsServiceObj) {
        this.accReportsServiceObj = accReportsServiceObj;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    
    public void setaccPurchaseOrderServiceDAO(AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj) {
        this.accPurchaseOrderServiceDAOobj = accPurchaseOrderServiceDAOobj;
    }

    public void setaccSalesOrderServiceDAO(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }

    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj) {
        this.profileHandlerDAOObj = profileHandlerDAOObj;
    }

    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public void setaccReceiptServiceDAO(AccReceiptServiceDAO accReceiptServiceDAO) {
        this.accReceiptServiceDAO = accReceiptServiceDAO;
    }

    public void setaccJournalEntryModuleService(AccJournalEntryModuleService accJournalEntryModuleService) {
        this.accJournalEntryModuleService = accJournalEntryModuleService;
    }

    public void setaccMainAccountingService(AccMainAccountingService accMainAccountingService) {
        this.accMainAccountingService = accMainAccountingService;
    }

    public void setaccPaymentDAO(accPaymentImpl accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccJournalEntry(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setAccGoodsReceiptModuleService(accGoodsReceiptModuleService accGoodsReceiptModuleService) {
        this.accGoodsReceiptModuleService = accGoodsReceiptModuleService;
    }
    
    public void setaccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }

    public void setaccGoodsReceipt(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    
    public void setAccProductService(AccProductService AccProductService) {
        this.AccProductService = AccProductService;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setAccSalesReturnService(AccSalesReturnService accSalesReturnService) {
        this.accSalesReturnService = accSalesReturnService;
    }
    
       public void setaccCreditNoteService(accCreditNoteService accCreditNoteService) {
        this.accCreditNoteService = accCreditNoteService;
    }
    
    public void setaccCreditNoteServiceCMN(accCreditNoteServiceCMN accCreditNoteServiceCMN) {
        this.accCreditNoteServiceCMN = accCreditNoteServiceCMN;
    } 
    
    public void setaccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    
    public void setAccFieldSetUpServiceDAOObj(AccFieldSetupServiceDao accFieldSetUpServiceDAOObj) {
        this.accFieldSetUpServiceDAOObj = accFieldSetUpServiceDAOObj;
    }
    
    public void setAccVendorPaymentModuleServiceObj(AccVendorPaymentModuleService accVendorPaymentModuleServiceObj) {
        this.accVendorPaymentModuleServiceObj = accVendorPaymentModuleServiceObj;
    }

    public void setAccDocumentDesignService(AccDocumentDesignService accDocumentDesignService) {
        this.accDocumentDesignService = accDocumentDesignService;
    }

@Override
    public JSONObject jsonDeleteReceivePayment(String receipid,JSONObject paramJobj,String receiptno) throws JSONException {
       JSONObject returnJobj = new JSONObject();
        if (!StringUtil.isNullOrEmpty(receipid)) {
            JSONArray deleteJSONArray = new JSONArray();
            JSONObject jobj = new JSONObject();
            jobj.put(Constants.billid, receipid);
             if (!StringUtil.isNullOrEmpty(receiptno)) {
                jobj.put("billno", receiptno);
            }
            deleteJSONArray.put(jobj);
            returnJobj.put(Constants.data, deleteJSONArray.toString());
        }
        returnJobj.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
        returnJobj.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
        returnJobj.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress,Constants.defaultIp));
        return returnJobj;
    }
     
  @Override  
     public JSONObject jsonDeleteInvoice(String invid,JSONObject paramJobj) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        if (!StringUtil.isNullOrEmpty(invid)) {
            JSONArray deleteJSONArray = new JSONArray();
            JSONObject jobj = new JSONObject();
            jobj.put(Constants.billid, invid);
            deleteJSONArray.put(jobj);
            returnJobj.put(Constants.data, deleteJSONArray.toString());
        }
        returnJobj.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
        returnJobj.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
        returnJobj.put("deletepermanentflag",true);
        returnJobj.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress,Constants.defaultIp));
        
        return returnJobj;
    }
     
    private JSONObject jsonCreateManualJournalEntry(JSONObject paramJobj,JSONObject responseObj) throws JSONException {
        JSONObject journalEntryJson = paramJobj;

        journalEntryJson.put("includeingstreport", "true");
        journalEntryJson.put("entrydate", paramJobj.optString("billdate"));
        journalEntryJson.put("entryno",paramJobj.optString("journalentryNumber"));
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("journalentryNumber", null))) {
            journalEntryJson.put(Constants.sequenceformat,"NA");
        }else{
            journalEntryJson.remove(Constants.sequenceformat);
        }
        /*
         * Invoice at Line Item Details
         */
        JSONArray profield = new JSONArray();
        JSONObject product = new JSONObject();
        product.put("debit", "true");
        //Dummy Account
        product.put("accountid", paramJobj.optString("accountid"));
        product.put("exchangeratefortransaction", "1");
        product.put("amount", responseObj.optString("amount"));
        product.put("accountpersontype", "0");
        profield.put(product);

        product = new JSONObject();
        product.put("debit", "false");
        //card value
        product.put("accountid", paramJobj.optString(Constants.pmtmethodaccountid));
        product.put("exchangeratefortransaction", "1");
        product.put("amount", responseObj.optString("amount"));
        product.put("accountpersontype", "0");
        profield.put(product);
        journalEntryJson.put(Constants.detail, profield);
        return journalEntryJson;
    }
    
    
    private JSONObject jsonCreateLeads(JSONObject paramJobj,JSONObject responseObj) throws JSONException, ServiceException {
        JSONObject leadreturnJson = paramJobj;
        String crmURL = URLUtil.buildRestURL("crmURL");
        crmURL = crmURL + "master/convert-lead";
        JSONObject userData = new JSONObject();
        userData.put("iscommit", true);
        userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
        userData.put(Constants.RES_CDOMAIN, paramJobj.optString(Constants.RES_CDOMAIN));
        userData.put("accountname", paramJobj.optString("leadaccountname"));
        userData.put("leadid", paramJobj.optString("leadid"));

         JSONObject jobj = apiCallHandlerService.restPostMethod(crmURL, userData.toString());

            if (jobj.has("accountCode") && jobj.get("accountCode") != null) {
                leadreturnJson.put("customervalue", jobj.getString("accountCode"));
                if (jobj.has("success") && jobj.optBoolean("success")) {
                    responseObj.put("customercode",jobj.getString("accountCode"));
                    responseObj.put("accountcode",jobj.getString("accountCode"));
                    System.out.println("Convert Lead -> "+responseObj.toString());   
                }
            } else {
                throw ServiceException.FAILURE("Customer Code is not available", "", false);
            }
        return leadreturnJson;
    }
            
    private JSONObject jsonCreateInvoice(JSONObject paramJobj) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        JSONObject invoiceJson = paramJobj;
        invoiceJson = wsUtilService.replaceBooleanwithStringValues(invoiceJson);
        boolean inCash = Boolean.parseBoolean(invoiceJson.optString("incash"));
        boolean isEdit = StringUtil.isNullOrEmpty(invoiceJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(invoiceJson.getString(Constants.isEdit));
        boolean isCopy = StringUtil.isNullOrEmpty(invoiceJson.optString("copyInv", null)) ? false : Boolean.parseBoolean(invoiceJson.getString("copyInv"));
        boolean isForPOS= invoiceJson.optBoolean(Constants.isForPos);
        String invoiceid = invoiceJson.optString("invoiceid",null);
        String invoicenumber = "";
        String companyid = paramJobj.optString(Constants.companyKey);
        invoiceJson.remove(Constants.billid);
        if (isCopy) {
            isEdit = false;
            invoiceJson.put(Constants.isEdit, "false");
        }
        if (!invoiceJson.has("discount") || (invoiceJson.has("discount") && invoiceJson.get("discount") != null)) {
            invoiceJson.put("discount", "0.0");
        }
        
        invoiceJson.put("modulename", String.valueOf(Constants.Acc_Invoice_ModuleId));

        if (!StringUtil.isNullOrEmpty(invoiceJson.optString("invoicenumber", null))) {
            invoicenumber = invoiceJson.optString("invoicenumber");
            if (!isEdit) {
                invoiceJson.put(Constants.sequenceformat, "NA");
            }
            invoiceJson.put("invoiceNumber",invoicenumber);
        } else {
            //Sequenceformat should not be removed for POS INVOICE-DO-RP Transaction
            if(!isForPOS && invoiceJson.has(Constants.sequenceformat) ){
              invoiceJson.remove(Constants.sequenceformat);
            }
        }
  
        if (!invoiceJson.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(invoiceJson.optString(Constants.sequenceformat, null))) {
            String sequenceformatid = null;
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, invoiceJson.get(Constants.companyKey));

            if (inCash) {
                sfrequestParams.put("modulename", "autocashsales");
            } else {
                sfrequestParams.put("modulename", "autoinvoice");
            }
            sfrequestParams.put("isdefaultFormat", true);
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.size() > 0) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                invoiceJson.put(Constants.sequenceformat, sequenceformatid);
            }
        }//end of sequenceformat

        String date = null;
        if (invoiceJson.has("billdate") && !StringUtil.isNullOrEmpty(invoiceJson.optString("billdate"))) {
            date = invoiceJson.optString("billdate") == null ? null : invoiceJson.optString("billdate");
        }

        if (!StringUtil.isNullOrEmpty(date)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
            Date transdate = authHandler.getUserDateFormatterWithoutTimeZone(invoiceJson).parse(date);
            DateFormat df = authHandler.getDateOnlyFormat();
            date = df.format(transdate);
            invoiceJson.put("billdate", date);
        }
        String shipdate = null;
        if (invoiceJson.has("shipdate") && !StringUtil.isNullOrEmpty(invoiceJson.optString("shipdate"))) {
            shipdate = invoiceJson.optString("shipdate") == null ? null : invoiceJson.optString("shipdate");
        }
        if (!StringUtil.isNullOrEmpty(shipdate)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
            Date sdate = authHandler.getUserDateFormatterWithoutTimeZone(invoiceJson).parse(shipdate);
            DateFormat df = authHandler.getDateOnlyFormat();
            shipdate = df.format(sdate);
            invoiceJson.put("shipdate", shipdate);
        }
        
        String dueDate = null;
        if (invoiceJson.has("duedate") && !StringUtil.isNullOrEmpty(invoiceJson.optString("duedate"))) {
            dueDate = invoiceJson.optString("duedate") == null ? null : invoiceJson.optString("duedate");
        }
        if (!StringUtil.isNullOrEmpty(dueDate)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
            Date sdate = authHandler.getUserDateFormatterWithoutTimeZone(invoiceJson).parse(dueDate);
            DateFormat df = authHandler.getDateOnlyFormat();
            dueDate = df.format(sdate);
            invoiceJson.put("dueDate", dueDate);
        } else {
            invoiceJson.put("dueDate", paramJobj.optString(Constants.BillDate));
        }

        invoiceJson.put(Constants.isdefaultHeaderMap, true);
        invoiceJson.put("gstIncluded", paramJobj.optBoolean("gstincluded", false));
        invoiceJson.put("defaultAdress", "false");
        if (paramJobj.has("invoicedetail")&& paramJobj.get("invoicedetail")!=null) {
            invoiceJson.put(Constants.detail, paramJobj.opt("invoicedetail"));
        }
        
        invoiceJson.put(Constants.modulename, String.valueOf(Constants.Acc_Invoice_ModuleId));

        if (isEdit) {// For POS Only
            invoiceJson.put(Constants.IS_INVOICE_ALLOW_TO_EDIT, "true");
            KwlReturnObject soresult = null;
            Invoice invoiceObj = null;
            if (!StringUtil.isNullOrEmpty(invoiceid)) {
                soresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                invoiceObj = (Invoice) soresult.getEntityList().get(0);
            } else if (!StringUtil.isNullOrEmpty(invoicenumber)) {
                invoiceid = accInvoiceDAOobj.getInvoiceId(companyid, invoicenumber);
                soresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                invoiceObj = (Invoice) soresult.getEntityList().get(0);
            }

            if (invoiceObj != null) {
                invoiceJson.put(Constants.billid, invoiceid);
                if (StringUtil.isNullOrEmpty(invoiceJson.optString("invoicenumber", null))) {
                    invoiceJson.put("invoiceNumber", invoiceObj.getInvoiceNumber());
                }
                if (!invoiceJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
                    invoiceJson.put(Constants.sequenceformat, invoiceObj.getSeqformat()!=null?invoiceObj.getSeqformat().getID():"NA");
                }
            }
        }
        return invoiceJson;
    }
    
@Override    
    public JSONObject jsonCreateReceivePayment(JSONObject paramJobj, JSONObject responseObj) throws JSONException {
        JSONObject receivePaymentJson = paramJobj;

        boolean isForPOS=paramJobj.optBoolean(Constants.isForPos, false);
        receivePaymentJson.put("amount", responseObj.optString("amount"));
        receivePaymentJson.put("creationdate", paramJobj.optString("billdate"));
        receivePaymentJson.put("accid", paramJobj.optString("CustomerName"));
        receivePaymentJson.put("no", paramJobj.optString("receiptNumber"));
        receivePaymentJson.put("iscustomer", true);
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("receiptnumber", null))) {
            receivePaymentJson.put(Constants.sequenceformat, "NA");
        } else {
            receivePaymentJson.remove(Constants.sequenceformat);
        }
        /*
         * Invoice at Line Item Details
         */
        JSONArray profield = new JSONArray();
        JSONObject product = new JSONObject();
        
        if (isForPOS) {
            product.put("type", Constants.PaymentAgainstInvoice);
            product.put("debit", "false");
            product.put("documentno", responseObj.optString("invoiceNo"));
            product.put("documentid", responseObj.optString("invid"));
            product.put("enteramount", responseObj.optString("amount"));
            product.put("amount", responseObj.optString("amount"));
            product.put("invoicecreationdate", paramJobj.optString("billdate"));
            product.put("exchangeratefortransaction", "1");
            profield.put(product);
        } else if (receivePaymentJson.optBoolean(Constants.isSquatTransaction)){
            double totalamount=0;
            double totaltaxamount=0;
            JSONArray jArray = receivePaymentJson.optJSONArray(Constants.detail);
            for (int i = 0; i < jArray.length(); i++) {
                product = jArray.getJSONObject(i);
                product.put("type", Constants.AdvancePayment);
                product.put("debit", "false");
                product.put("enteramount", product.optString("amount","0.0"));
                product.put("exchangeratefortransaction", receivePaymentJson.optString(Constants.externalcurrencyrate));
                totalamount +=Double.parseDouble(product.optString("amount","0.0"));
                String linelevelterms=product.optString("LineTermdetails","[{}]");
                product=wsUtilService.buildLineLevelTerms(linelevelterms,product,true);
                totalamount +=product.optDouble("recTermAmount",0.0);
                profield.put(product);
                product = new JSONObject();
            }
            receivePaymentJson.put("amount",totalamount);
        }
        receivePaymentJson.put("Details", profield);
        receivePaymentJson.put(Constants.detail, profield);
        receivePaymentJson.put("details", profield);
        
        return receivePaymentJson;
    }
    
    @Override
    public JSONObject jsonCreatelinkInvoicesToReceivePayment(JSONObject paramJobj,JSONObject responseObj, Invoice invObj, Receipt receiptObj) throws JSONException {
        JSONObject linkInvoiceJson = paramJobj;
        linkInvoiceJson.put("amounts", responseObj.optString("amount","0.0"));
        linkInvoiceJson.put("linkingdate", paramJobj.optString("billdate"));
        linkInvoiceJson.put("paymentid", receiptObj.getID());

        /*
         * Invoice at Line Item Details
         */
        JSONArray profield = new JSONArray();
        JSONObject product = new JSONObject();
        product.put("type", "Invoice");
        product.put("documentType", Constants.PaymentAgainstInvoice);
        product.put("debit", "false");
        product.put("documentno", invObj.getInvoiceNumber());
        product.put("documentid", invObj.getID());
        product.put("enteramount", responseObj.optString("amount","0.0"));
        product.put("linkamount", responseObj.optString("amount","0.0"));
        product.put("amountdue", invObj.getInvoiceamountdue());
        product.put("amountDueOriginal", invObj.getInvoiceamount());
        if (invObj.getCurrency().getCurrencyID().equalsIgnoreCase(receiptObj.getCurrency().getCurrencyID())) {
            product.put("exchangeratefortransaction", "1");
        } else {
            product.put("exchangeratefortransaction", linkInvoiceJson.optString(Constants.externalcurrencyrate));
        }

        profield.put(product);
        linkInvoiceJson.put("linkdetails", profield);
        return linkInvoiceJson;
    }
    
    @Override
    public JSONObject saveTransactions(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean incash = false;
        boolean issuccess = false;
        boolean isReceipt = false;
        boolean isInvoice = false;
        String invid = null;
        String receiptid = null;
        String receiptNumber = null;
        String msg = null;
        try {
            /*
             * Lead conversion Section
             */
            paramJobj.put(Constants.isSquatTransaction, true);
            if (paramJobj.has("leadid") && !StringUtil.isNullOrEmpty(paramJobj.optString("leadid", null))) {
                paramJobj = jsonCreateLeads(paramJobj,response);//Lead id will return customer code
            }

            /*
             * Save Invoice Section
             */
            JSONObject invoiceResponseJson = saveInvoice(paramJobj);

            if (invoiceResponseJson.has("invoiceNo") && !StringUtil.isNullOrEmpty(invoiceResponseJson.optString("invoiceNo", null)) && invoiceResponseJson.has(Constants.RES_success) && invoiceResponseJson.optBoolean(Constants.RES_success)) {
                response.put("invoicenumber", invoiceResponseJson.optString("invoiceNo"));
                if (invoiceResponseJson.has("inCash") && invoiceResponseJson.get("inCash") != null) {
                    incash = invoiceResponseJson.optBoolean("inCash", false);
                }

                if (invoiceResponseJson.has("invid") && !StringUtil.isNullOrEmpty(invoiceResponseJson.optString("invid", null))) {
                    invid = invoiceResponseJson.optString("invid", null);
                }
                isInvoice = true;
                issuccess = true;
                if (invoiceResponseJson.has("requestParamsJson") && invoiceResponseJson.getJSONObject("requestParamsJson") != null) {
                    paramJobj = invoiceResponseJson.getJSONObject("requestParamsJson");
                }
            } else {
                issuccess = false;
            }

            if (!incash) {//when cash sales is created then receive payment is not made
                /*
                 * Receive Payment Section
                 */
                JSONObject rpBuildJson = jsonCreateReceivePayment(paramJobj, invoiceResponseJson);
                JSONObject rpResponseJson = saveReceiptPayment(rpBuildJson);

                if (rpResponseJson.has("billno") && !StringUtil.isNullOrEmpty(rpResponseJson.optString("billno", null)) && rpResponseJson.has(Constants.RES_success) && rpResponseJson.optBoolean(Constants.RES_success)) {
                    if (rpResponseJson.has("requestParamsJson") && rpResponseJson.getJSONObject("requestParamsJson") != null) {
                        paramJobj = rpResponseJson.getJSONObject("requestParamsJson");
                        if (isInvoice) {
                            KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                            Invoice invObj = (Invoice) invoiceResult.getEntityList().get(0);
                            KwlReturnObject rpResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), rpResponseJson.optString("paymentid"));
                            Receipt rpObj = (Receipt) rpResult.getEntityList().get(0);
                            JSONObject linkInvoicesJson = jsonCreatelinkInvoicesToReceivePayment(paramJobj, rpResponseJson, invObj, rpObj);
                            JSONObject resJson=accReceivePaymentModuleServiceObj.linkReceiptToDocumentsJSON(linkInvoicesJson);
                            if(resJson.has(Constants.RES_success) && resJson.optBoolean(Constants.RES_success)) {
                              issuccess=resJson.optBoolean(Constants.RES_success);
                            }
                        }
                        paramJobj.remove(Constants.detail);
                        paramJobj.remove("Details");
                    }
                    receiptNumber = rpResponseJson.optString("billno");
                    response.put("receiptnumber", rpResponseJson.optString("billno"));
                    receiptid = rpResponseJson.optString("paymentid");
                    isReceipt = true;
                } else {
                    issuccess = false;
                    JSONObject deletejson = jsonDeleteInvoice(invid, rpBuildJson);
                    deletejson = deleteInvoice(deletejson);
                    response.put("invoicenumber","");
                }
            }

            /*
             * Journal Entry Section
             */
            JSONObject jeBuildJson = jsonCreateManualJournalEntry(paramJobj, invoiceResponseJson);
            JSONObject jeresponseJson = accJournalEntryModuleService.saveJournalEntry(jeBuildJson);
            if (jeresponseJson.has("billno") && !StringUtil.isNullOrEmpty(jeresponseJson.optString("billno", null)) && jeresponseJson.has(Constants.RES_success) && jeresponseJson.optBoolean(Constants.RES_success)) {
                issuccess = true;
            } else {
                issuccess = false;
                JSONObject deletejson = jsonDeleteReceivePayment(receiptid, jeBuildJson, receiptNumber);
                deletejson = deleteReceivePayment(deletejson);
                deletejson = jsonDeleteInvoice(invid, jeBuildJson);
                deletejson = deleteInvoice(deletejson);
            }
            
            if (!issuccess) {
                msg="Some issued have occured during transactions therefore transactions have been rollbacked.";
            }else{
                msg="Transaction is successful";
            }

        } catch (Exception ex) {
            issuccess = false;
            if (isInvoice) {
                JSONObject deletejson = jsonDeleteInvoice(invid, paramJobj);
                deletejson = deleteInvoice(deletejson);
                response.put("invoicenumber","");
            }
            if (isReceipt) {
                JSONObject deletejson = jsonDeleteReceivePayment(receiptid, paramJobj, receiptNumber);
                deletejson = deleteReceivePayment(deletejson);
                response.put("receiptnumber","");
            }
            msg="Some issued have occured during transactions therefore transactions have been rollbacked except Customer Creation";
            System.out.println("Exception-> "+ex.getMessage().toString());   
        } finally {
            response.put(Constants.RES_MESSAGE,msg);
            response.put(Constants.RES_success, issuccess);

        } 
        return response;
    }

  @Override  
   @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
    public JSONObject deleteReceivePayment(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        
        if (paramJobj.has("receiptno") && !StringUtil.isNullOrEmpty(paramJobj.optString("receiptno", null))) {
            paramJobj = createDeleteJSON(paramJobj, String.valueOf(Constants.Acc_Receive_Payment_ModuleId));
        }
        
        if (!paramJobj.has(Constants.RES_data) || !paramJobj.has(Constants.remoteIPAddress) || !paramJobj.has(Constants.useridKey)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } else {
            JSONArray invoiceArray = new JSONArray(paramJobj.getString(Constants.RES_data));
            if (invoiceArray.length() < 1) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            paramJobj.put(Constants.RES_data, invoiceArray);
        }
        JSONObject jobj = new JSONObject();
        if ((paramJobj.has(Constants.deletepermanentflag) && paramJobj.optBoolean(Constants.deletepermanentflag)) || !paramJobj.has(Constants.deletepermanentflag)) {
            try {
                jobj = accReceivePaymentModuleServiceObj.deleteReceiptForEdit(paramJobj);
            } catch (AccountingException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), "", false);
            }
        } else if (paramJobj.has(Constants.deletepermanentflag) && !paramJobj.optBoolean(Constants.deletepermanentflag)) {
            jobj = accReceivePaymentModuleServiceObj.deleteReceiptMerged(paramJobj);
        }
        if (jobj.has(Constants.RES_msg)) {
            jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
            jobj.remove(Constants.RES_msg);
        }
        if (jobj.has(Constants.RES_success)) {
            jobj.put(Constants.RES_success, jobj.getBoolean(Constants.RES_success));
        } else {
            jobj.put(Constants.RES_success, false);
        }

        return jobj;
    }
  
  
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
    public JSONObject deleteMakePaymentJSON(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (paramJobj.has("paymentno") && !StringUtil.isNullOrEmpty(paramJobj.optString("paymentno", null))) {
            paramJobj = createDeleteJSON(paramJobj, String.valueOf(Constants.Acc_Make_Payment_ModuleId));
        }
        if (paramJobj.has(Constants.deletepermanentflag) && paramJobj.optBoolean(Constants.deletepermanentflag)) {
            response = accVendorPaymentModuleServiceObj.deletePaymentPermanentJSON(paramJobj);
        } else {
            response = accVendorPaymentModuleServiceObj.deletePaymentTemporaryJSON(paramJobj);
        }
        if (response.has(Constants.RES_msg)) {
            response.put(Constants.RES_MESSAGE, response.getString(Constants.RES_msg));
            response.remove(Constants.RES_msg);
        }
        if (response.has(Constants.RES_success)) {
            response.put(Constants.RES_success, response.getBoolean(Constants.RES_success));
        } else {
            response.put(Constants.RES_success, false);
        }
        return response;
    }
    
    @Override
    public JSONObject saveInvoice(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            boolean isAndroidflag=paramJobj.optBoolean(Constants.isdefaultHeaderMap, false);
            
            if (!paramJobj.optBoolean(Constants.isMultiGroupCompanyFlag) && !isAndroidflag) {
                paramJobj = jsonCreateInvoice(paramJobj);//For squats & pos related
            }
            if (!isAndroidflag||paramJobj.optBoolean(Constants.isMultiGroupCompanyFlag)) {//Not allowing to execute for Android but allowing for multigroupcompany,squats and pos
                paramJobj = wsUtilService.populateMastersInformation(paramJobj);
                if (!paramJobj.optBoolean(Constants.isMultiGroupCompanyFlag)) {// it is shifted down because to get customer addresses for indian gst to put the value in state dimension value
                    String customField = paramJobj.optString(Constants.customfield, null);
                    String countryid = "0";
                    KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), paramJobj.optString(Constants.companyKey));
                    Company companyObj = (Company) companyresult.getEntityList().get(0);
                    if (companyObj.getCountry() != null) {
                        countryid = companyObj.getCountry().getID();
                    }
                    if (countryid.equals(String.valueOf(Constants.indian_country_id))) {
                        JSONArray customfieldArray = new JSONArray();
                        if (!StringUtil.isNullOrEmpty(customField)) {
                            customfieldArray = new JSONArray(customField);
                        }
                        JSONObject cfield = new JSONObject();
                        cfield.put("value", paramJobj.optString(Constants.RES_CDOMAIN));
                        cfield.put("fieldlabel", "Entity");
                        customfieldArray.put(cfield);
                        cfield = new JSONObject();
                        cfield.put("value", paramJobj.optString("statevalue"));
                        cfield.put("fieldlabel", "State");
                        customfieldArray.put(cfield);
                        customField = customfieldArray.toString();
                    }
                }
            }
            
            if (paramJobj != null) {
                if (!paramJobj.has("incash") || !paramJobj.has(Constants.BillDate) || !paramJobj.has(Constants.detail)) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
                boolean includeProductTax = StringUtil.isNullOrEmpty(paramJobj.optString("includeprotax", null)) ? false : Boolean.parseBoolean(paramJobj.optString("includeprotax"));
                if (includeProductTax) {
                    paramJobj.put("taxamount", "0.0");
                }
                
                String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
                JSONArray detailArr=new JSONArray();
                if (!paramJobj.optBoolean(Constants.isMultiGroupCompanyFlag)) {
                    detailArr = paramJobj.getJSONArray(Constants.detail);
                }else{
                  String detialsarray=paramJobj.optString(Constants.detail);
                  detailArr = new JSONArray(detialsarray);
                }
                for (int i = 0; i < detailArr.length(); i++) {
                    JSONObject detailObj = detailArr.getJSONObject(i);
                    if (!detailObj.has("productid") || !detailObj.has("rate")) {
                        throw ServiceException.FAILURE("Missing required field", "e01", false);
                    }
                }

                if (paramJobj.has("userName")) {
                    String userId = profileHandlerDAOObj.getUserIdFromUserName(paramJobj.getString("userName"), paramJobj.getString(Constants.companyKey));
                    paramJobj.put(Constants.useridKey, userId);
                }

                if (paramJobj.has("currencyCode")) {
                    KwlReturnObject currResponse = accCurrencyDAOobj.getCurrencyFromCode(paramJobj.getString("currencyCode"));
                    if (currResponse != null && currResponse.getEntityList() != null && !currResponse.getEntityList().isEmpty()) {
                        KWLCurrency currency = (KWLCurrency) currResponse.getEntityList().get(0);
                        paramJobj.put(Constants.currencyKey, currency.getCurrencyID());
                    }
                }
                if (paramJobj.has("gcurrencyCode")) {
                    KwlReturnObject currResponse = accCurrencyDAOobj.getCurrencyFromCode(paramJobj.getString("gcurrencyCode"));
                    if (currResponse != null && currResponse.getEntityList() != null && !currResponse.getEntityList().isEmpty()) {
                        KWLCurrency currency = (KWLCurrency) currResponse.getEntityList().get(0);
                        paramJobj.put(Constants.globalCurrencyKey, currency.getCurrencyID());
                    }
                }

                if (paramJobj.has("transactiondate")) {
                    String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("transactiondate"), originalDateFormat);
                    paramJobj.put("transactiondateStr", convertedDate);
                    paramJobj.put("transactiondate", convertedDate);
                }
                if (paramJobj.has(Constants.BillDate) && paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == false) {
                    String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.BillDate), originalDateFormat);
                    paramJobj.put("billdateStr", convertedDate);
                    paramJobj.put(Constants.BillDate, convertedDate);
                }
                if ((paramJobj.has(Constants.duedate)) && paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == false) {
                    String duedate = null;
                    duedate = paramJobj.getString(Constants.duedate);
                    String convertedDate = WSServiceUtil.getGlobalFormattedDate(duedate, originalDateFormat);
                    paramJobj.put("duedateStr", convertedDate);
                    paramJobj.put(Constants.duedate, convertedDate);
                }
            if (paramJobj.has(Constants.shipdate)&& paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == false) {
                    String convertedDate = "";
                if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.shipdate,null))) {
                        convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.shipdate), originalDateFormat);
                    }
                    paramJobj.put("shipdateStr", convertedDate);
                    paramJobj.put(Constants.shipdate, convertedDate);
                }

                //Account code IS NEEDED IN Receive Payment
                if (paramJobj.has(Constants.customerName) && paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true && paramJobj.optBoolean(Constants.isForPos, false) == true) {
                    KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.optString(Constants.customerName));
                    Customer cObj = (Customer) customerResult.getEntityList().get(0);
                    if (cObj != null) {
                        paramJobj.put("acccode", cObj.getAcccode());
                    }
                }
            }
            System.out.println(paramJobj);
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                jobj.put("requestParamsJson", paramJobj);
                jobj = accInvoiceModuleService.saveInvoice(paramJobj);
            } else {
                jobj = accInvoiceModuleService.saveCustomerInvoiceJson(paramJobj);
            }
            if (jobj.has(Constants.RES_msg)) {
                jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
                jobj.remove(Constants.RES_msg);
            }
            if (jobj.has(Constants.RES_success)) {
                jobj.put(Constants.RES_success, (Boolean) jobj.get(Constants.RES_success));
            } else {
                jobj.put(Constants.RES_success, false);
            }
        } catch (Exception ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    private JSONObject jsonCreateDeliveryOrder(JSONObject paramJobj) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        JSONObject savedoJson = paramJobj;
        savedoJson = wsUtilService.replaceBooleanwithStringValues(savedoJson);
        boolean isEdit = StringUtil.isNullOrEmpty(savedoJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(savedoJson.getString(Constants.isEdit));
        boolean isCopy = StringUtil.isNullOrEmpty(savedoJson.optString("copyInv", null)) ? false : Boolean.parseBoolean(savedoJson.getString("copyInv"));
        String doid = savedoJson.optString("deliveryorderid",null);
        String donumber = "";
        String companyid = paramJobj.optString(Constants.companyKey);
        savedoJson.remove("deliveryorderid");
        if (isCopy) {
            isEdit = false;
            savedoJson.put(Constants.isEdit, "false");
        }

        if (!StringUtil.isNullOrEmpty(savedoJson.optString("donumber", null))) {
            donumber = savedoJson.optString("donumber");
            if (!isEdit) {
                savedoJson.put(Constants.sequenceformat, "NA");
            }
            
            savedoJson.put("deliveryOrderNo", donumber);
        } else {
            savedoJson.remove(Constants.sequenceformat);
        }

        if (!savedoJson.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(savedoJson.optString(Constants.sequenceformat, null))) {
            String sequenceformatid = null;
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, savedoJson.get(Constants.companyKey));

            sfrequestParams.put("modulename", "autodo");
            sfrequestParams.put("isdefaultFormat", true);
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.size() > 0) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                savedoJson.put(Constants.sequenceformat, sequenceformatid);
            }
        }//end of sequenceformat

        String date = null;
        if (savedoJson.has("billdate") && !StringUtil.isNullOrEmpty(savedoJson.optString("billdate"))) {
            date = savedoJson.optString("billdate") == null ? null : savedoJson.optString("billdate");
        }

        if (!StringUtil.isNullOrEmpty(date)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
            Date transdate = authHandler.getUserDateFormatterWithoutTimeZone(savedoJson).parse(date);
            DateFormat df = authHandler.getDateOnlyFormat();
            date = df.format(transdate);
            savedoJson.put(Constants.BillDate, date);
        }
        String shipdate = null;
        if (savedoJson.has("shipdate") && !StringUtil.isNullOrEmpty(savedoJson.optString("shipdate"))) {
            shipdate = savedoJson.optString("shipdate") == null ? null : savedoJson.optString("shipdate");
        }
        if (!StringUtil.isNullOrEmpty(shipdate)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
            Date sdate = authHandler.getUserDateFormatterWithoutTimeZone(savedoJson).parse(shipdate);
            DateFormat df = authHandler.getDateOnlyFormat();
            shipdate = df.format(sdate);
            savedoJson.put("shipdate", shipdate);
        }

        savedoJson.put(Constants.isdefaultHeaderMap, true);
        savedoJson.put("gstIncluded", paramJobj.optBoolean("gstincluded", false));
        savedoJson.put("poRefNumber", paramJobj.optString("porefnumber"));
        savedoJson.put("defaultAdress", "false");
        savedoJson.put(Constants.detail, paramJobj.opt("deliveryorderdetail"));
        savedoJson.put(Constants.modulename, String.valueOf(Constants.Acc_Delivery_Order_ModuleId));
        savedoJson.put(Constants.moduleid, String.valueOf(Constants.Acc_Delivery_Order_ModuleId));

        if (isEdit) {// For POS Rest services Only
            KwlReturnObject soresult = null;
            DeliveryOrder doObj = null;
            if (!StringUtil.isNullOrEmpty(doid)) {
                soresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doid);
                doObj = (DeliveryOrder) soresult.getEntityList().get(0);
            } else if (!StringUtil.isNullOrEmpty(donumber)) {
                doid = accInvoiceDAOobj.getDeliveryOrderId(companyid, donumber);
                soresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doid);
                doObj = (DeliveryOrder) soresult.getEntityList().get(0);
            }

            if (doObj != null) {
                savedoJson.put("doid", doid);
                if (StringUtil.isNullOrEmpty(savedoJson.optString("donumber", null))) {
                    savedoJson.put("deliveryOrderNo", doObj.getDeliveryOrderNumber());
                }
                if (!savedoJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
                    savedoJson.put(Constants.sequenceformat, doObj.getSeqformat()!=null?doObj.getSeqformat().getID():"NA");
                }
            }
        }
        return savedoJson;
    }
    @Override
    public JSONObject saveDeliveryOrder(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            paramJobj = jsonCreateDeliveryOrder(paramJobj);//For pos related
            paramJobj = wsUtilService.populateMastersInformation(paramJobj);
            String customField = paramJobj.optString(Constants.customfield, null);
            if (!StringUtil.isNullOrEmpty(customField)) {
                JSONArray customJArray = wsUtilService.createJSONForCustomField(customField, paramJobj.optString(Constants.companyKey), Constants.Acc_Invoice_ModuleId);
                paramJobj.put(Constants.customfield, customJArray);
            }

            if (paramJobj != null) {
                if (!paramJobj.has(Constants.BillDate) || !paramJobj.has(Constants.detail) || paramJobj.getJSONArray(Constants.detail).length() < 1) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }

                JSONArray detailArr = paramJobj.getJSONArray(Constants.detail);
                for (int i = 0; i < detailArr.length(); i++) {
                    JSONObject detailObj = detailArr.getJSONObject(i);
                    if (!detailObj.has("productid") || !detailObj.has("rate")||!detailObj.has("dquantity")) {
                        throw ServiceException.FAILURE("Missing required field", "e01", false);
                    }
                }
            }
            System.out.println(paramJobj);
            jobj = accInvoiceModuleService.saveDeliveryOrderJSON(paramJobj);
            if (jobj.has(Constants.RES_msg)) {
                jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
                jobj.remove(Constants.RES_msg);
            }
            if (jobj.has(Constants.RES_success)) {
                jobj.put(Constants.RES_success, (Boolean) jobj.get(Constants.RES_success));
            } else {
                jobj.put(Constants.RES_success, false);
            }
        } catch (Exception ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    /**
     * Save Goods Receipt
     * @param paramJobj
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws SessionExpiredException
     * @throws ParseException 
     */
    @Override
    public JSONObject saveGoodsReceipt(JSONObject paramJobj) throws JSONException,AccountingException, ServiceException, SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        paramJobj = jsonCreateGoodsReceipt(paramJobj);
        paramJobj = wsUtilService.populateMastersInformation(paramJobj);
        String customField = paramJobj.optString(Constants.customfield, null);
        if (!StringUtil.isNullOrEmpty(customField)) {
            JSONArray customJArray = wsUtilService.createJSONForCustomField(customField, paramJobj.optString(Constants.companyKey), Constants.Acc_Goods_Receipt_ModuleId);
            paramJobj.put(Constants.customfield, customJArray);
        }
        if (paramJobj != null) {
            if (!paramJobj.has("VendorName") || !paramJobj.has(Constants.BillDate) || !paramJobj.has(Constants.detail) || paramJobj.getJSONArray(Constants.detail).length() < 1) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }

            JSONArray detailArr = paramJobj.getJSONArray(Constants.detail);
            for (int i = 0; i < detailArr.length(); i++) {
                JSONObject detailObj = detailArr.getJSONObject(i);
                if (!detailObj.has("productid") || !detailObj.has("rate") || !detailObj.has("dquantity")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
        }
        //System.out.println(paramJobj);
        jobj = accGoodsReceiptModuleService.saveGoodsReceiptOrder(paramJobj);
        if (jobj.has(Constants.RES_msg)) {
            jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
            jobj.remove(Constants.RES_msg);
        }
        if (jobj.has(Constants.RES_success)) {
            jobj.put(Constants.RES_success, (Boolean) jobj.get(Constants.RES_success));
        } else {
            jobj.put(Constants.RES_success, false);
        }
        return jobj;
    }
    
    /**
     * process requestJson before sending passing to saveGoodsReceipt service
     * @param paramJobj
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws ParseException
     * @throws SessionExpiredException 
     */
    private JSONObject jsonCreateGoodsReceipt(JSONObject paramJobj) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        JSONObject returnJobj = paramJobj;
        returnJobj = wsUtilService.replaceBooleanwithStringValues(returnJobj);
        boolean isEdit = StringUtil.isNullOrEmpty(returnJobj.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(returnJobj.getString(Constants.isEdit));
        boolean isCopy = StringUtil.isNullOrEmpty(returnJobj.optString("copyInv", null)) ? false : Boolean.parseBoolean(returnJobj.getString("copyInv"));
        String grId = returnJobj.optString("goodsreceiptid",null);
        String grnumber = "";
        String companyid = paramJobj.optString(Constants.companyKey);
        returnJobj.remove("goodsreceiptid");
        if (isCopy) {
            isEdit = false;
            returnJobj.put(Constants.isEdit, "false");
        }

        /**
         * Goods Receipt Number
         */
        if (!StringUtil.isNullOrEmpty(returnJobj.optString("grnumber", null))) {
            grnumber = returnJobj.optString("grnumber");
            if (!isEdit) {
                returnJobj.put(Constants.sequenceformat, "NA");
            }
            
            returnJobj.put("goodsReceiptNo", grnumber);
        } else {
            returnJobj.remove(Constants.sequenceformat);
        }

        /**
         * Sequence Format
         */
        Map requestParams = null;
        if (!returnJobj.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(returnJobj.optString(Constants.sequenceformat, null))) {
            String sequenceformatid;
            requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.companyKey, returnJobj.get(Constants.companyKey));
            requestParams.put("modulename", "autogro");
            requestParams.put("isdefaultFormat", true);
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(requestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.size() > 0) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                returnJobj.put(Constants.sequenceformat, sequenceformatid);
            }
        }

        /**
         * Goods Receipt Date - BillDate
         */
        String date = null;
        if (returnJobj.has("billdate") && !StringUtil.isNullOrEmpty(returnJobj.optString("billdate"))) {
            date = returnJobj.optString("billdate") == null ? null : returnJobj.optString("billdate");
        }

        if (!StringUtil.isNullOrEmpty(date)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
            Date transdate = authHandler.getUserDateFormatterWithoutTimeZone(returnJobj).parse(date);
            DateFormat df = authHandler.getDateOnlyFormat();
            date = df.format(transdate);
            returnJobj.put(Constants.BillDate, date);
        }
        /**
         * Ship Date
         */
        String shipdate = null;
        if (returnJobj.has("shipdate") && !StringUtil.isNullOrEmpty(returnJobj.optString("shipdate"))) {
            shipdate = returnJobj.optString("shipdate") == null ? null : returnJobj.optString("shipdate");
        }
        if (!StringUtil.isNullOrEmpty(shipdate)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
            Date sdate = authHandler.getUserDateFormatterWithoutTimeZone(returnJobj).parse(shipdate);
            DateFormat df = authHandler.getDateOnlyFormat();
            shipdate = df.format(sdate);
            returnJobj.put("shipdate", shipdate);
        }

        returnJobj.put(Constants.isdefaultHeaderMap, true);//Flag to indicate that call is from REST service
        /**
         * Including GST flag
         */
        returnJobj.put("gstIncluded", paramJobj.optBoolean("gstincluded", false));
        paramJobj.remove("gstincluded");
        /**
         * 'includeprotax' is automatically true if 'gstIncluded' is true
         */
        returnJobj.put("includeprotax", paramJobj.optBoolean("includeprotax", false) || paramJobj.optBoolean("gstIncluded", false));
        /**
         * 'isApplyTaxToTerms' can be changed only if 'includeprotax' is true
         */
        returnJobj.put("isApplyTaxToTerms", paramJobj.optBoolean("includeprotax", false) && paramJobj.optBoolean("applytaxtoterms", false));
        paramJobj.remove("applytaxtoterms");
        /**
         * Supplier Invoice Number
         */
        returnJobj.put("supplierinvoiceno", paramJobj.optString("supplierinvoicenumber"));
        paramJobj.remove("supplierinvoicenumber");
        /**
         * Permit Number
         */
        returnJobj.put("permitNumber", paramJobj.optString("permitnumber"));
        paramJobj.remove("permitnumber");
        /**
         * Line Details
         */
        returnJobj.put(Constants.detail, paramJobj.opt("goodsreceiptdetail"));
        returnJobj.remove("goodsreceiptdetail");
        /**
         * Inventory URL
         */
        returnJobj.put(Constants.inventoryURL, storageHandlerImpl.GetinventoryURL());
        returnJobj.put(Constants.modulename, String.valueOf(Constants.Acc_GoodsReceipt_modulename));
        returnJobj.put(Constants.moduleid, String.valueOf(Constants.Acc_Goods_Receipt_ModuleId));
        returnJobj.put("defaultAdress", "false");

        /**
         * In case of edit, fetch Goods Receipt's UUID from database and put in requestJSON
         * Also update SequenceFormat if
         */
        if (isEdit) {
            KwlReturnObject result = null;
            GoodsReceipt grObj = null;
            if (!StringUtil.isNullOrEmpty(grId)) {
                result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grId);
                grObj = (GoodsReceipt) result.getEntityList().get(0);
            } else if (!StringUtil.isNullOrEmpty(grnumber)) {
                requestParams = new HashMap<String, String>();
                requestParams.put("tableName", "GoodsReceipt");
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put("condtionColumn", "goodsReceiptNumber");
                requestParams.put("condtionColumnvalue", grnumber);
                result = accountingHandlerDAOobj.populateMasterInformation(requestParams);
                grObj = (GoodsReceipt) result.getEntityList().get(0);
            }

            if (grObj != null) {
                returnJobj.put("doid", grId);
                if (StringUtil.isNullOrEmpty(returnJobj.optString("grnumber", null))) {
                    returnJobj.put("goodsReceiptNo", grObj.getGoodsReceiptNumber());
                }
                if (!returnJobj.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
                    returnJobj.put(Constants.sequenceformat, grObj.getSeqformat()!=null?grObj.getSeqformat().getID():"NA");
                }
            }
        }
        return returnJobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getAccountList(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject jobj = new JSONObject();
        if (!paramJobj.has(Constants.currencyKey)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        jobj = accMainAccountingService.getAccountsForComboJson(paramJobj);
        jobj.put(Constants.RES_success, true);
        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getAccountsIdNameList(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject jobj = new JSONObject();
        if (!paramJobj.has(Constants.currencyKey)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        jobj = accMainAccountingService.getAccountsIdNameJson(paramJobj);
        jobj.put(Constants.RES_success, true);
        return jobj;
    }

    @Override
    public JSONObject saveJournalEntry(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException,ParseException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject jobj = new JSONObject();
        if (!paramJobj.has(Constants.currencyKey)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        boolean isFromEclaim = paramJobj.has(Constants.isFromEclaim) ? paramJobj.getBoolean(Constants.isFromEclaim) : false;
        //Call to web-application
        if (isFromEclaim) {
            JSONArray jArr = new JSONArray(paramJobj.optString(Constants.detail, "[]"));
            JSONArray modifiedjsonArray = new JSONArray();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jarrobj = jArr.getJSONObject(i);
                if (jarrobj.optBoolean("isTaxAccount")) {
                    /*
                     * If Tax is not present in ERP side then we simply return
                     * with info code
                     */
                    Tax tax = null;
                    String taxID = jarrobj.has("accountid") ? jarrobj.optString("accountid") : "";
                    if (!StringUtil.isNullOrEmpty(taxID)) {
                        KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxID);
                        tax = (Tax) result1.getEntityList().get(0);
                        if (StringUtil.isNullObject(tax)) {
                            throw ServiceException.FAILURE("Selected Tax is not available on Accounting side.", "acc.common.erp31", false);
                        } else {
                            jarrobj.put("accountid", tax.getAccount().getID());
                            jarrobj.put("appliedGst", taxID);
                        }
                    }
                }
                modifiedjsonArray.put(jarrobj);
            }
            paramJobj.put(Constants.detail, modifiedjsonArray.toString());
            jobj = accJournalEntryModuleService.saveJournalEntry(paramJobj);
        } else if (paramJobj.has("byCompanyPreference") && paramJobj.getBoolean("byCompanyPreference")) {
            if (!paramJobj.has(Constants.lid)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            jobj = accJournalEntryModuleService.saveJournalEntry(paramJobj);
        }else if(paramJobj.optBoolean(Constants.isdefaultHeaderMap)){
            
            String creationdate = null;
            if (paramJobj.has("billdate") && !StringUtil.isNullOrEmpty(paramJobj.optString("billdate"))) {
                creationdate = paramJobj.optString("billdate") == null ? null : paramJobj.optString("billdate");
                paramJobj.put("entrydate", creationdate);
                if (!StringUtil.isNullOrEmpty(creationdate)) {  //ERP-9230 : //USER DATE FORMAT 
                    Date transdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).parse(creationdate);
                    DateFormat df = authHandler.getDateOnlyFormat();
                    creationdate = df.format(transdate);
                    paramJobj.put("entrydate", creationdate);
                }
            }
            
            jobj = accJournalEntryModuleService.saveJournalEntry(paramJobj);
        } else {
            if (paramJobj.has("jedata")) {
                JSONArray jedataArr = paramJobj.getJSONArray("jedata");
                if (jedataArr.length() < 1) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
                paramJobj.remove("jedata");
                JSONObject dataObj = new JSONObject();
                JSONArray jedata = new JSONArray();
                JSONObject datamap = new JSONObject();
                datamap.put("details", jedataArr);
                jedata.put(datamap);
                dataObj.put("jedata", jedata);
                paramJobj.put(Constants.RES_data, dataObj);
            } else {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            jobj = accJournalEntryModuleService.saveJournalEntryRemoteApplicationJson(paramJobj);
        }

        if (jobj.has(Constants.RES_msg)) {
            jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
            jobj.remove(Constants.RES_msg);
        }
        if (jobj.has("jarr")) {
            jobj.put("jedetails", jobj.getJSONArray("jarr"));
            jobj.remove("jarr");
        }
        jobj.put(Constants.RES_success, true);
        return jobj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveReceiptPayment(JSONObject paramJobj) throws JSONException, ServiceException,ParseException,SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        System.out.println(paramJobj);
        JSONObject response = new JSONObject();
        boolean isDefaultHeaderMapFlag = false;
        
        if (paramJobj.has("receiptno")|| paramJobj.optBoolean(Constants.isForPos)) {
            String creationdate = null;
            paramJobj.put(Constants.isdefaultHeaderMap, true);
            isDefaultHeaderMapFlag = true;
            if (paramJobj.has("billdate") && !StringUtil.isNullOrEmpty(paramJobj.optString("billdate"))) {
                creationdate = paramJobj.optString("billdate");
                paramJobj.put("creationdate", creationdate);
            }

            if (!StringUtil.isNullOrEmpty(creationdate)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
                Date transdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).parse(creationdate);
                DateFormat df = authHandler.getDateOnlyFormat();
                creationdate = df.format(transdate);
                paramJobj.put("creationdate", creationdate);
                paramJobj.remove("billdate");
            }

            JSONObject modifiedJson = jsonCreateMpAndRPForPOS(paramJobj, Constants.Acc_Receive_Payment_ModuleId);
            paramJobj = modifiedJson;
        }
        
        if (!isDefaultHeaderMapFlag) {//for eclaim integration & ios
            if (!paramJobj.has(Constants.currencyKey) || !paramJobj.has(Constants.lid) ||!paramJobj.has("amount") || !paramJobj.has("creationdate") || !paramJobj.has("no") || !paramJobj.has("pmtmethod") || !paramJobj.has(Constants.detail) || paramJobj.getJSONArray(Constants.detail).length() < 1) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            JSONArray detailArr = paramJobj.getJSONArray(Constants.detail);
            for (int i = 0; i < detailArr.length(); i++) {
                JSONObject detailObj = detailArr.getJSONObject(i);
                if (!detailObj.has("amount")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }

            if (paramJobj.has("creationdate")) {

                try {
                    paramJobj.put("creationdateStr", WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("creationdate"), paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern())));
                    paramJobj.put("creationdate", WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("creationdate"), paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern())));
                } catch (SessionExpiredException ex) {
                    Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    ServiceException.FAILURE(ex.getMessage(), ex);
                }
            }
            for (int i = 0; i < detailArr.length(); i++) {
                JSONObject detailObj = detailArr.getJSONObject(i);
                if (detailObj.has("transactionno")) {
                    detailObj.put("billno", detailObj.getString("transactionno"));
                }
                if (detailObj.has("invoiceid")) {
                    detailObj.put(Constants.billid, detailObj.getString("invoiceid"));
                }
            }
        } 
        HashMap<String, Object> hashMap = accReceivePaymentModuleServiceObj.saveCustomerReceipt(paramJobj);
        response = (JSONObject) hashMap.get("jobj");
        if (!isDefaultHeaderMapFlag) {//for eclaim integration,squat & ios
            response.put("requestParamsJson", paramJobj);
        }
        if (response.has(Constants.RES_msg)) {
            response.put(Constants.RES_MESSAGE, response.getString(Constants.RES_msg));
            response.remove(Constants.RES_msg);
        }
        response.put(Constants.RES_success, true);
        return response;
    }
    
    private JSONObject createJsonDeliveryOrderForReport(JSONObject paramJobj) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        JSONObject doJson = paramJobj;
        doJson.put(Constants.isdefaultHeaderMap, true);
        String companyid = doJson.getString(Constants.companyKey);
        if (doJson.has("customervalue") || doJson.has("productvalue")) {//only for productvalue and customer value filter only
            doJson = wsUtilService.manipulateGlobalLevelFieldsNew(doJson, companyid);
        }
        doJson.put("pendingapproval", paramJobj.optString("pendingapproval", "false"));
        doJson.put("customerCategoryid", paramJobj.optString("customercategoryvalue", "All"));
        doJson.put("consolidateFlag", paramJobj.optString("consolidateflag", "false"));
        doJson.put("deleted", paramJobj.optString("deletedflag", "false"));
        doJson.put("nondeleted", paramJobj.optString("nondeletedflag", "false"));
        doJson.put("salesPersonFilterFlag", paramJobj.optString("salespersonfilterflag", "true"));
        doJson.put("report", paramJobj.optString("reportflag", "true"));
        doJson.put("isUnInvoiced", paramJobj.optString("dowithoutinvoiceflag", "false"));
        doJson.put("isPOSRecords", paramJobj.optString("posflag", "false"));
        doJson.put("isJobWorkOutReciever", paramJobj.optString("jobworkflag", "false"));
        doJson.put(Constants.companyids, companyid);
        doJson.put("ss", paramJobj.optString("searchvalue", ""));
        doJson.put("isfavourite", paramJobj.optString("favouriteflag", "false"));
        return doJson;
    }

    private JSONObject createJsonInvoiceForReport(JSONObject paramJobj) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        JSONObject invoiceJson = paramJobj;
        String companyid = invoiceJson.getString(Constants.companyKey);

        invoiceJson.put("orderforcontract", paramJobj.optString("orderforcontract", "false"));
        invoiceJson.put(Constants.companyids, companyid);

        invoiceJson.put("vatcommodityid", paramJobj.optString("vatcommodityvalue", "all"));
        invoiceJson.put("consolidateFlag", paramJobj.optString("consolidateFlag", "false"));
        invoiceJson.put("onlyNormalPendingInvoices", paramJobj.optString("onlyNormalPendingInvoices", "false"));
        invoiceJson.put("salesPersonFilterFlag", paramJobj.optString("salespersonfilterflag", "true"));
        invoiceJson.put("includeAllRec", paramJobj.optString("includeallrecordflag", "false"));
        invoiceJson.put("CashAndInvoice", paramJobj.optString("CashAndInvoice", "true"));
        invoiceJson.put("getlineItemDetailsflag", paramJobj.optString("getlineItemDetailsflag", "true"));
        invoiceJson.put("report", paramJobj.optString("reportflag", "true"));
        invoiceJson.put("isExport", paramJobj.optString("isExport", "true"));
        invoiceJson.put("pagingFlag", paramJobj.optString("pagingflag", "true"));

        invoiceJson.put(Constants.ss, paramJobj.optString("searchvalue", paramJobj.optString(Constants.ss)));
        invoiceJson.put("isOpeningBalanceOrder", paramJobj.optString("openingbalancesflag", "false"));
        invoiceJson.put("isfavourite", paramJobj.optString("favouriteflag", "false"));

        if (paramJobj.has("pendingpaymentflag") && paramJobj.get("pendingpaymentflag") != null && Boolean.parseBoolean(paramJobj.optString("pendingpaymentflag", "false"))) {//Invoice with DO
            invoiceJson.put("includeAllRec", "false");
            invoiceJson.put("ispendingpayment", paramJobj.optString("pendingpaymentflag", "false"));
        }

        if (paramJobj.has("nondeletedflag") && paramJobj.get("nondeletedflag") != null && Boolean.parseBoolean(paramJobj.optString("nondeletedflag", "false"))) {//Invoice with DO
            invoiceJson.put("includeAllRec", "false");
            invoiceJson.put("nondeleted", paramJobj.optString("nondeletedflag", "false"));
        }

        if (paramJobj.has("deletedflag") && paramJobj.get("deletedflag") != null && Boolean.parseBoolean(paramJobj.optString("deletedflag", "false"))) {//Deleted flag
            invoiceJson.put("includeAllRec", "false");
            invoiceJson.put("deleted", paramJobj.optString("deletedflag", "false"));
        }

        if (paramJobj.has("recurringinvoicesflag") && paramJobj.get("recurringinvoicesflag") != null && Boolean.parseBoolean(paramJobj.optString("recurringinvoicesflag", "false"))) {//Recurring Invoice
            invoiceJson.put("includeAllRec", "false");
            invoiceJson.put("onlyRecurredInvoices", paramJobj.optString("recurringinvoicesflag", "false"));
        }
        
        if (paramJobj.has("mobiletransactionsflag") && paramJobj.get("mobiletransactionsflag") != null && Boolean.parseBoolean(paramJobj.optString("mobiletransactionsflag", "false"))) {//Mobile records
            invoiceJson.put("includeAllRec", "false");
            invoiceJson.put("generatedSource", "1");
        }

        if (paramJobj.has("cashsalesflag") && paramJobj.get("cashsalesflag") != null && Boolean.parseBoolean(paramJobj.optString("cashsalesflag", "false"))) {//Cash Sales
            invoiceJson.put("includeAllRec", "false");
            invoiceJson.put("CashAndInvoice", "false");
        invoiceJson.put("cashonly", paramJobj.optString("cashsalesflag", "false"));
        }

        if (paramJobj.has("creditsalesflag") && paramJobj.get("creditsalesflag") != null && Boolean.parseBoolean(paramJobj.optString("creditsalesflag", "false"))) {//Invoice
            invoiceJson.put("includeAllRec", "false");
            invoiceJson.put("CashAndInvoice", "false");
            invoiceJson.put("creditonly", paramJobj.optString("creditsalesflag", "false"));
        }

        if (paramJobj.has("favouriteflag") && paramJobj.get("favouriteflag") != null && Boolean.parseBoolean(paramJobj.optString("favouriteflag", "false"))) {//Invoice with DO
            invoiceJson.put("includeAllRec", "false");
        invoiceJson.put("isfavourite", paramJobj.optString("favouriteflag", "false"));
        }

        if (paramJobj.has("invoicelinkedwithdoflag") && paramJobj.get("invoicelinkedwithdoflag") != null && Boolean.parseBoolean(paramJobj.optString("invoicelinkedwithdoflag", "false"))) {//Invoice with DO
            invoiceJson.put("invoiceLinkedWithDOStatus", "10");
            invoiceJson.put("orderLinkedWithDocType", "0");
            invoiceJson.put("onlyNormalPendingInvoices", "true");
        }

        if (paramJobj.has("invoicelinkedwithnodoflag") && paramJobj.get("invoicelinkedwithnodoflag") != null && Boolean.parseBoolean(paramJobj.optString("invoicelinkedwithnodoflag", "false"))) { //Invoice without DO
            invoiceJson.put("invoiceLinkedWithDOStatus", "11");
            invoiceJson.put("orderLinkedWithDocType", "0");
            invoiceJson.put("isOuststandingproduct", "true");
            invoiceJson.put("myPO", "true");
        }
        if (paramJobj.has("invoicelinkedwithpartialdoflag") && paramJobj.get("invoicelinkedwithpartialdoflag") != null && Boolean.parseBoolean(paramJobj.optString("invoicelinkedwithpartialdoflag", "false"))) {//Invoice with partial DO
            invoiceJson.put("invoiceLinkedWithDOStatus", "12");
            invoiceJson.put("orderLinkedWithDocType", "12");
        }
        if (invoiceJson.has("customervalue") || invoiceJson.has("productvalue")) {//only for productvalue and customer value filter only
            invoiceJson = wsUtilService.manipulateGlobalLevelFieldsNew(invoiceJson, companyid);
            invoiceJson.put("includeExcludeChildCmb", paramJobj.optString("includeexcludechildcmbvalue", "false"));
        }

        return invoiceJson;
    }  
       
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getInvoice(JSONObject paramJObj){
        JSONObject jobj = new JSONObject();
        int totalCount=0;
        try {
            
            boolean isForlinkCombo = paramJObj.optBoolean("isforlinkcombo");
            paramJObj = wsUtilService.populateAdditionalInformation(paramJObj);
            paramJObj = wsUtilService.manipulateGlobalLevelFieldsNew(paramJObj, paramJObj.optString(Constants.companyKey));
            //ERP-41214:Show asterisk to unit price and amount
            paramJObj = wsUtilService.getUserPermissionsforUnitPriceAndAmount(paramJObj);
            
            if (!paramJObj.has(Constants.globalCurrencyKey) && !isForlinkCombo) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            
            //advancesearch code
            paramJObj.put(Constants.moduleid, String.valueOf(Constants.Acc_Invoice_ModuleId));
            paramJObj = wsUtilService.buildAdvanceSearchJson(paramJObj);

            //Done for POS-ERP-39363
            if (isForlinkCombo) { //For link Combo
                int requestModuleid = paramJObj.optInt("requestmoduleid");
                paramJObj.put("creditonly", "true");
                paramJObj.put("cashonly", "false");
                paramJObj.put("CashAndInvoice", "true");
                paramJObj.put("requestModuleid", String.valueOf(requestModuleid));
                paramJObj.put("currencyfilterfortrans", paramJObj.opt(Constants.globalCurrencyKey));

                if (!paramJObj.has(Constants.customerid)) {
                    throw ServiceException.FAILURE("Missing Customer field", "e01", false);
                }
            }
            
            if (!paramJObj.optBoolean(Constants.isdefaultHeaderMap, false)&& !isForlinkCombo) {
                paramJObj = wsUtilService.replaceBooleanwithStringValues(paramJObj);
                //Give default values to reports
                paramJObj = createJsonInvoiceForReport(paramJObj);
            }
            String originalDateFormat = paramJObj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
            if (paramJObj.has(Constants.REQ_startdate)) {
                paramJObj.put(Constants.REQ_startdate, WSServiceUtil.getGlobalFormattedDate(paramJObj.getString(Constants.REQ_startdate), originalDateFormat));
            }
            if (paramJObj.has(Constants.REQ_enddate)) {
                paramJObj.put(Constants.REQ_enddate, WSServiceUtil.getGlobalFormattedDate(paramJObj.getString(Constants.REQ_enddate), originalDateFormat));
            }

            if (paramJObj.has("revenue") && paramJObj.getBoolean("revenue")) {
                if (!paramJObj.has("type") || StringUtil.isNullOrEmpty(paramJObj.getString("type")) || !paramJObj.has("projectid") || StringUtil.isNullOrEmpty(paramJObj.getString("projectid"))) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
                if (paramJObj.getString("type").equals("customer")) {
                    jobj = getCustomerInvoicesReport(paramJObj);
                } else if (paramJObj.getString("type").equals("vendor")) {
                    jobj = getVendorInvoicesReport(paramJObj).getJSONObject(Constants.RES_data);
                } else if (paramJObj.getString("type").equals("cashandpurchase")) {
                    jobj = getCashAndPurchaseRevenue(paramJObj).getJSONObject(Constants.RES_data);
                } else {
                    throw ServiceException.FAILURE("Invalid invoice type", "erp14", false);
                }
            }
//            else if (isForlinkCombo) {
//                jobj = accInvoiceServiceDAO.getInvoices(paramJObj);
//            } 
            else {
                boolean issuccess = false;
                JSONArray dataJArr = new JSONArray();
                JSONArray ColumnConfigArr = new JSONArray();

                if (!paramJObj.has("CashAndInvoice")) {
                    paramJObj.put("CashAndInvoice", true);
                }
                HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMapJson(paramJObj);
                boolean includeExcludeChildCmb;
                if (paramJObj.optString("includeExcludeChildCmb", null) != null && paramJObj.optString("includeExcludeChildCmb").equals("All")) {
                    includeExcludeChildCmb = true;
                } else {
                    includeExcludeChildCmb = paramJObj.optString("includeExcludeChildCmb") != null ? Boolean.parseBoolean(paramJObj.optString("includeExcludeChildCmb")) : false;
                }
                requestParams.put("includeExcludeChildCmb", includeExcludeChildCmb);
                boolean ispendingpayment = paramJObj.optString("ispendingpayment", null) != null ? Boolean.parseBoolean(paramJObj.optString("ispendingpayment")) : false;
                requestParams.put("ispendingpayment", ispendingpayment);
                boolean onlyRecurredInvoices = paramJObj.optString("onlyRecurredInvoices", null) != null ? Boolean.parseBoolean(paramJObj.optString("onlyRecurredInvoices")) : false;
                requestParams.put("onlyRecurredInvoices", onlyRecurredInvoices);
                boolean onlyNormalPendingInvoices = paramJObj.optString("onlyNormalPendingInvoices", null) != null ? Boolean.parseBoolean(paramJObj.optString("onlyNormalPendingInvoices")) : false;
                requestParams.put("onlyNormalPendingInvoices", onlyNormalPendingInvoices);

                requestParams.put(Constants.companyKey, paramJObj.getString(Constants.companyKey));

                requestParams.put(Constants.Acc_Search_Json, paramJObj.optString(Constants.Acc_Search_Json, null));
                requestParams.put(Constants.start, paramJObj.optString(Constants.start, null));
                requestParams.put(Constants.limit, paramJObj.optString(Constants.limit, null));
                requestParams.put(Constants.Filter_Criteria, paramJObj.optString(Constants.Filter_Criteria, null));

                if (!paramJObj.has(Constants.moduleid)) {
                    requestParams.put(Constants.moduleid, String.valueOf(Constants.Acc_Invoice_ModuleId));
                } else {
                    requestParams.put(Constants.moduleid, paramJObj.optString(Constants.moduleid, null));
                }

                KwlReturnObject result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                List list = result.getEntityList();
                totalCount = result.getRecordTotalCount();
                dataJArr = accInvoiceServiceDAO.getInvoiceJsonMergedJson(paramJObj, list, dataJArr);

                if (paramJObj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    ColumnConfigArr = fieldManagerServiceobj.getColumnHeadersConfigList(paramJObj);
                    String moduleid = String.valueOf(Constants.Acc_Invoice_ModuleId);
                    ColumnConfigArr = accCustomReportService.getCustomReportMeasureFieldJsonArray(ColumnConfigArr, moduleid, paramJObj);
                    /* Batch Serials*/
                    ColumnConfigArr = accFieldSetUpServiceDAOObj.getBatchSerialsFieldsJsonArray(ColumnConfigArr, moduleid, paramJObj);
                    jobj.put(Constants.RES_METADATA, ColumnConfigArr);
                }

                issuccess = true;
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_data, dataJArr);
                if (totalCount != 0) {
                    jobj.put(Constants.RES_TOTALCOUNT, totalCount);
                } else {
                    jobj.put(Constants.RES_TOTALCOUNT, dataJArr.length());
                }
            }
            jobj.put(Constants.RES_success, true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

       @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getJournalEntry(JSONObject paramJObj) throws JSONException, ServiceException, SessionExpiredException {
        paramJObj = wsUtilService.populateAdditionalInformation(paramJObj);
        if (!paramJObj.has(Constants.currencyKey)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        DateFormat df = authHandler.getUserDateFormatterJson(paramJObj);
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray DataJArr = new JSONArray();

        if (!paramJObj.has("CashAndInvoice")) {
            paramJObj.put("CashAndInvoice", true);
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, paramJObj.getString(Constants.companyKey));
        requestParams.put(Constants.currencyKey, paramJObj.getString(Constants.currencyKey));
        requestParams.put(Constants.globalCurrencyKey, paramJObj.getString(Constants.globalCurrencyKey));
        requestParams.put("CashAndInvoice", paramJObj.getString("CashAndInvoice"));
        requestParams.put("df", df);
        KwlReturnObject jeResponse = accJournalEntryobj.getJournalEntry(requestParams);
        jobj = accReportsServiceObj.getJournalEntryJsonMerged(requestParams, jeResponse.getEntityList(), DataJArr);
        issuccess = true;
        jobj.put(Constants.RES_success, issuccess);
        jobj.put(Constants.RES_data, DataJArr);
        jobj.put(Constants.RES_TOTALCOUNT, DataJArr.length());
        jobj.put(Constants.RES_success, true);
        return jobj;
    } 
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getIndividualProductPrice(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
        JSONArray DataArr = new JSONArray();
        boolean isSuccess=true;
        try {
            if (!paramJObj.has("transactiondate")) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            paramJObj = wsUtilService.populateAdditionalInformation(paramJObj);
            String companyid = paramJObj.optString(Constants.companyKey);
            if (!paramJObj.has("forCurrency")) {
                paramJObj.put("forCurrency", paramJObj.opt(Constants.globalCurrencyKey));
            }
            if (!paramJObj.has("currency")) {
                paramJObj.put("currency", paramJObj.opt("forCurrency"));
            }
         
            //calculate PO COUNT SO COUNT
            paramJObj.put("getSOPOflag", true);
             
            if (paramJObj.has("products") && paramJObj.get("products") != null) {
                String productIdArray[] = paramJObj.optString("products").split(",");
                if (productIdArray.length > 0) {
                    JSONObject tempReqJson = new JSONObject(paramJObj.toString());
                    for (String productvalue : productIdArray) {
                        JSONObject productResponse = new JSONObject();
                        tempReqJson.put("productvalue", productvalue);
                        tempReqJson = wsUtilService.manipulateGlobalLevelFieldsNew(tempReqJson, companyid);
                        //customer id
                        if (!StringUtil.isNullOrEmpty(tempReqJson.optString(Constants.customerid, null))) {
                            tempReqJson.put("affecteduser", tempReqJson.opt(Constants.customerid));
                        }
                        JSONObject response = AccProductService.getIndividualProductPrice(tempReqJson);
                        productResponse.put(tempReqJson.optString(Constants.productid), response.toString());
                        DataArr.put(productResponse);
                        isSuccess = true;
                    }
                }
            } else {//android case
                paramJObj = wsUtilService.manipulateGlobalLevelFieldsNew(paramJObj, companyid);
                //customer id
                if (!StringUtil.isNullOrEmpty(paramJObj.optString(Constants.customerid, null))) {
                    paramJObj.put("affecteduser", paramJObj.opt(Constants.customerid));
                }
                jobj = AccProductService.getIndividualProductPrice(paramJObj);
                DataArr = jobj.getJSONArray(Constants.data);
                isSuccess = jobj.optBoolean(Constants.RES_success, false);
            }
            jobj.put(Constants.RES_success, isSuccess);
            jobj.put(Constants.RES_data, DataArr);
            jobj.put(Constants.RES_TOTALCOUNT, DataArr.length());

        } catch (ServiceException ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getPaymentMethod(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject jobj = new JSONObject();
        String msg = "";

        String paymentAccountType = paramJobj.optString("paymentAccountType", null);
        String accountid = paramJobj.optString("accountid", null);
        boolean isforEclaim = paramJobj.optBoolean("isforEclaim", false);
        
        boolean populateincpcs = paramJobj.optString("populateincpcs", null) != null ? Boolean.parseBoolean(paramJobj.get("populateincpcs").toString()) : false;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
        if (!StringUtil.isNullOrEmpty(paymentAccountType)) {
            requestParams.put("paymentAccountType", paymentAccountType);
        }
        if (!StringUtil.isNullOrEmpty(accountid)) {
            requestParams.put("accountid", accountid);
        }
        if (populateincpcs) {
            requestParams.put("populateincpcs", populateincpcs);
        }
        if (isforEclaim) {
            requestParams.put("isforEclaim", isforEclaim);
        }
        
        KwlReturnObject result = accPaymentDAOobj.getPaymentMethod(requestParams);
        List list = result.getEntityList();

        JSONArray jArr = buildPaymentMethodJson(list, paramJobj.getString(Constants.companyKey),isforEclaim);
//        if (list.size() <=0 && !StringUtil.isNullOrEmpty(accountid)) {
//            throw ServiceException.FAILURE("Missing required field", "erp28", false);
//        } else {
        jobj.put(Constants.RES_data, jArr);
        jobj.put(Constants.RES_TOTALCOUNT, jArr.length());
        jobj.put(Constants.RES_success, true);
//        }
       
        return jobj;
    }

    private JSONArray buildPaymentMethodJson(List list, String companyid,boolean isforEclaim) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jArr = new JSONArray();
        JSONArray paymentmethodArr = new JSONArray();
        JSONArray returnpaymentmethodArr = new JSONArray();

        Iterator itr = list.iterator();

        while (itr.hasNext()) {
            PaymentMethod paymethod = (PaymentMethod) itr.next();
            JSONObject obj = new JSONObject();
            obj.put("methodid", paymethod.getID());
            obj.put("paymentmethodid", paymethod.getID());
            obj.put("methodname", paymethod.getMethodName());
            obj.put("paymentmethodidValue", paymethod.getMethodName());
            obj.put("accountid", paymethod.getAccount().getID());
            obj.put("accountname", paymethod.getAccount().getName());
            obj.put("isIBGBankAccount", paymethod.getAccount().isIBGBank());
            if (paymethod.getAccount().isIBGBank()) {
                obj.put("bankType", paymethod.getAccount().getIbgBankType());
            }
            obj.put("detailtype", paymethod.getDetailType());
            obj.put("acccurrency", paymethod.getAccount().getCurrency().getCurrencyID());
            obj.put("acccurrencysymbol", paymethod.getAccount().getCurrency().getSymbol());
            obj.put("acccustminbudget", paymethod.getAccount().getCustMinBudget());
            obj.put("autopopulate", paymethod.isAutoPopulate());
            obj.put("autopopulateincpcs", paymethod.isAutoPopulateInCPCS());

            KwlReturnObject result = accPaymentDAOobj.getTransactionCountForPayment(paymethod.getID(), companyid);
            if (result.getRecordTotalCount() > 0) {
                obj.put("isChangableAccount", false);
            } else {
                obj.put("isChangableAccount", true);
            }

            if (!StringUtil.isNullOrEmpty(paymethod.getAccount().getID())) {
                /* ERP-32507 -Make payment can be done against "Cash" accounts
                 * for bank mastertype value or cash mastertype value from eclaim request.
                */ 
                if ((paymethod.getAccount().getMastertypevalue() == Constants.ACCOUNT_MASTERTYPE_BANK || paymethod.getAccount().getMastertypevalue() == Constants.ACCOUNT_MASTERTYPE_CASH)) {
                    paymentmethodArr.put(obj);
                }
            }

            jArr.put(obj);
        }
        returnpaymentmethodArr = jArr;
        if (isforEclaim) {//for eclaim return jsonarray with mastertype 2 and 3 only
            returnpaymentmethodArr = paymentmethodArr;
        }

        return returnpaymentmethodArr;
    }

    @Override
    public JSONObject deleteInvoice(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (paramJobj.has("invoiceno") && !StringUtil.isNullOrEmpty(paramJobj.optString("invoiceno", null))) {
            paramJobj = createDeleteJSON(paramJobj, String.valueOf(Constants.Acc_Invoice_ModuleId));
        }
        if (!paramJobj.has(Constants.RES_data) || !paramJobj.has(Constants.remoteIPAddress) || !paramJobj.has(Constants.useridKey)){
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } else {
            JSONArray invoiceArray = new JSONArray(paramJobj.getString(Constants.RES_data));
            if (invoiceArray.length() < 1) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            paramJobj.put(Constants.RES_data,invoiceArray);
        }
        JSONObject jobj = new JSONObject();
        jobj =  accInvoiceServiceDAO.deleteInvoiceJson(paramJobj);
        if (jobj.has(Constants.RES_msg)) {
            jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
            jobj.remove(Constants.RES_msg);
        }
        if (jobj.has(Constants.RES_success)) {
            jobj.put(Constants.RES_success, jobj.getBoolean(Constants.RES_success));
        } else {
            jobj.put(Constants.RES_success, false);
        }
        
        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject deleteSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException{
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject jobj = new JSONObject();
        
        //for POS
        if (paramJobj.has("salesreturnno") && !StringUtil.isNullOrEmpty(paramJobj.optString("salesreturnno", null))) {
            paramJobj = createDeleteJSON(paramJobj, String.valueOf(Constants.Acc_Sales_Return_ModuleId));
        }
        //for pos permanent delete
        if (paramJobj.has(Constants.deletepermanentflag) && paramJobj.optBoolean(Constants.deletepermanentflag)) {
            jobj = accInvoiceServiceDAO.deleteSalesReturnJson(paramJobj);
        } else if (paramJobj.has(Constants.deletepermanentflag) && !paramJobj.optBoolean(Constants.deletepermanentflag)) {//for pos temporary delete
            jobj = deleteSalesReturnTemporary(paramJobj);
        } else { //previous working of records
            if (!paramJobj.has(Constants.RES_data) || !paramJobj.has(Constants.remoteIPAddress) || !paramJobj.has(Constants.useridKey)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            } else {
                JSONArray invoiceArray = new JSONArray(paramJobj.getString(Constants.RES_data));
                if (invoiceArray.length() < 1) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
                paramJobj.put(Constants.RES_data, invoiceArray);
            }
            jobj = accInvoiceServiceDAO.deleteSalesReturnJson(paramJobj);
        }
        if (jobj.has(Constants.RES_msg)) {
            jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
            jobj.remove(Constants.RES_msg);
        }
        if (jobj.has(Constants.RES_success)) {
            jobj.put(Constants.RES_success, jobj.getBoolean(Constants.RES_success));
        } else {
            jobj.put(Constants.RES_success, false);
        }
        
        return jobj;
    }
 
    private JSONObject createDeleteJSON(JSONObject paramJObj, String moduleid) throws ServiceException, com.krawler.utils.json.base.JSONException {
        JSONObject deleteSOJson = paramJObj;
        String[] recarr = new String[10];
        deleteSOJson = wsUtilService.populateAdditionalInformation(deleteSOJson);
        String companyid = deleteSOJson.optString(Constants.companyKey);
        String userid = paramJObj.optString(Constants.useridKey);
       
        // These are the transaction nos in comma separated format e.g. "SO1,SO2"
        if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
            String deliveryOrderNo = deleteSOJson.optString("deliveryorderno");
            recarr = deliveryOrderNo.split(",");
        }
        
        if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
            String salesOrderNo = deleteSOJson.optString("salesorderno");
            recarr = salesOrderNo.split(",");
        }
        
        if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
            String salesReturnNo = deleteSOJson.optString("salesreturnno");
            recarr = salesReturnNo.split(",");
        }

        if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
            String creditNoteNo = deleteSOJson.optString("creditnoteno");
            recarr = creditNoteNo.split(",");
        }
        if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
            String receiptNo = deleteSOJson.optString("receiptno");
            recarr = receiptNo.split(",");
        }
        if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
            String mpNo = deleteSOJson.optString("paymentno");
            recarr = mpNo.split(",");
        }

        if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Cash_Sales_ModuleId)) || moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Invoice_ModuleId))) {
            String invoiceNo = deleteSOJson.optString("invoiceno");
            recarr = invoiceNo.split(",");
        }
        
        JSONArray deletejArray = new JSONArray();
        if (recarr.length > 0) {//if billid and billno are not deleted
            for (String transactionNo : recarr) {
                JSONObject deleteJson = new JSONObject();
                String billid = null;
                String billno = null;
                Date transactionDate=null;// to check active days restriction flow
                if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                    KwlReturnObject doResult = accInvoiceDAOobj.getDeliveryOrderCount(transactionNo, companyid);
                    DeliveryOrder deliveryorder = (DeliveryOrder) doResult.getEntityList().get(0);
                    if (deliveryorder != null) {
                        billid = deliveryorder.getID();
                        billno = deliveryorder.getDeliveryOrderNumber();
                        transactionDate=deliveryorder.getOrderDate();
                    }
                }
                
                if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                    KwlReturnObject soResult = accSalesOrderDAOobj.getSalesOrderCount(transactionNo, companyid);
                    SalesOrder soObj = (SalesOrder) soResult.getEntityList().get(0);
                    if (soObj != null) {
                        billid = soObj.getID();
                        billno = soObj.getSalesOrderNumber();
                        transactionDate=soObj.getOrderDate();
                    }
                }
                
                 if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                    KwlReturnObject rpResult = accReceiptDAOobj.getReceiptFromBillNo(transactionNo, companyid);
                    Receipt rpObj = (Receipt) rpResult.getEntityList().get(0);
                    if (rpObj != null) {
                        billid = rpObj.getID();
                        billno = rpObj.getReceiptNumber();
                        transactionDate=rpObj.getCreationDate();
                    }
                     if (rpObj.getJournalEntry() != null) {
                         deleteJson.put("entryno", rpObj.getJournalEntry().getEntryNumber());
                         deleteJson.put("journalentryid", rpObj.getJournalEntry().getID());
                     }
                }
                 
                 if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                    KwlReturnObject mpResult = accPaymentDAOobj.getPaymentFromBillNo(transactionNo, companyid);
                    Payment mpObj = (Payment) mpResult.getEntityList().get(0);
                    if (mpObj != null) {
                        billid = mpObj.getID();
                        billno = mpObj.getPaymentNumber();
                        transactionDate=mpObj.getCreationDate();
                    }
                    
                      if (mpObj.getJournalEntry() != null) {
                         deleteJson.put("entryno", mpObj.getJournalEntry().getEntryNumber());
                         deleteJson.put("journalentryid", mpObj.getJournalEntry().getID());
                     }
                }
                
                if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                    KwlReturnObject doResult = accCreditNoteDAOobj.getCNFromNoteNo(transactionNo, companyid);
                    CreditNote cnObj = (CreditNote) doResult.getEntityList().get(0);
                    if (cnObj != null) {
                        billid = cnObj.getID();
                        billno = cnObj.getCreditNoteNumber();
                        transactionDate=cnObj.getCreationDate();
                        if (cnObj.getJournalEntry() != null) {
                            deleteJson.put("entryno", cnObj.getJournalEntry().getEntryNumber());
                            deleteJson.put("journalentryid", cnObj.getJournalEntry().getID());
                        }
                        
                    }
                }
                
                 if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                    KwlReturnObject doResult = accInvoiceDAOobj.getSalesReturnCount(transactionNo, companyid);
                    SalesReturn salesReturn = (SalesReturn) doResult.getEntityList().get(0);
                    if (salesReturn != null) {
                        billid = salesReturn.getID();
                        billno = salesReturn.getSalesReturnNumber();
                        transactionDate=salesReturn.getOrderDate();
                    }
                }
                 if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Cash_Sales_ModuleId)) || moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Invoice_ModuleId))) {
                    KwlReturnObject doResult = accInvoiceDAOobj.getInvoiceCount(transactionNo, companyid);
                    Invoice invObj = (Invoice) doResult.getEntityList().get(0);
                    if (invObj != null) {
                        billid = invObj.getID();
                        billno = invObj.getInvoiceNumber();
                        transactionDate=invObj.getCreationDate();
                    }
                }

                if (!StringUtil.isNullOrEmpty(billno)) {
                    deleteJson.put("billno", billno);
                }
                if (!StringUtil.isNullOrEmpty(billid)) {//billid is mandatory
                    deleteJson.put(Constants.billid, billid);
                         
                    //Checking Active Days Case where it doesn't allow to edit if the document date doesn't come in active days .
                    if (!StringUtil.isNullOrEmpty(billid) && transactionDate != null) {
                        wsUtilService.checkUserActivePeriodRange(companyid, userid, transactionDate, Integer.parseInt(moduleid));
                    }
                    
                    if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                        deleteJson.put("noteid",billid );
                        deleteJson.put("noteno",billno);
                    }
                    deletejArray.put(deleteJson);
                }
            }//end of for loop

            deleteSOJson.put(Constants.data, deletejArray.toString());
            deleteSOJson.put(Constants.companyKey, companyid);
            deleteSOJson.put(Constants.isdefaultHeaderMap, true);
        }
        return deleteSOJson;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
    public JSONObject deleteSalesReturnTemporary(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        response = accInvoiceServiceDAO.deleteSalesReturnTemporaryJson(paramJobj);
        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
    public JSONObject deleteDeliveryOrdersJSON(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
         paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (paramJobj.has("deliveryorderno") && !StringUtil.isNullOrEmpty(paramJobj.optString("deliveryorderno", null))) {
            paramJobj = createDeleteJSON(paramJobj, String.valueOf(Constants.Acc_Delivery_Order_ModuleId));
        }
        if (paramJobj.has(Constants.deletepermanentflag) && paramJobj.optBoolean(Constants.deletepermanentflag)) {
            response = accInvoiceModuleService.deleteDeliveryOrdersJSON(paramJobj);
        } else {
            response = accInvoiceModuleService.deleteTemporaryDeliveryOrders(paramJobj);
        }
        return response;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
    public JSONObject deleteCreditNoteJSON(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        if (paramJobj.has("creditnoteno") && !StringUtil.isNullOrEmpty(paramJobj.optString("creditnoteno", null))) {
            paramJobj = createDeleteJSON(paramJobj, String.valueOf(Constants.Acc_Credit_Note_ModuleId));
        }
        if (paramJobj.has(Constants.deletepermanentflag) && paramJobj.optBoolean(Constants.deletepermanentflag)) {
            response = accCreditNoteServiceCMN.deleteCreditNotesPermanentJSON(paramJobj);
        } else {
            response = accCreditNoteService.deleteCreditNoteTemporary(paramJobj);
        }
        return response;
    }
 
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getSalesOrder(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException ,UnsupportedEncodingException{
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (!paramJobj.has(Constants.globalCurrencyKey)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        if (paramJobj.has("customerid")) {
            paramJobj.put("erpcustomerid", paramJobj.getString("customerid"));
        }
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject result = null;
        JSONArray DataJArr = new JSONArray();
        JSONArray ColumnConfigArr = new JSONArray();
        JSONArray ColumnModelConfigArr = new JSONArray();
        
        //advancesearch code ERP-40420 for POS
        paramJobj.put(Constants.moduleid, String.valueOf(Constants.Acc_Sales_Order_ModuleId));
        paramJobj = wsUtilService.buildAdvanceSearchJson(paramJobj);
        //ERP-41214:Show asterisk to unit price and amount
        paramJobj = wsUtilService.getUserPermissionsforUnitPriceAndAmount(paramJobj);
        
        //fetching sales order details
        HashMap<String, Object> requestParams = accSalesOrderServiceDAOobj.getSalesOrdersMapJson(paramJobj);
        result = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
        int totalCount = result.getRecordTotalCount();
        DataJArr = accSalesOrderServiceDAOobj.getSalesOrdersJsonMerged(paramJobj, result.getEntityList(), DataJArr);
        
        //Required for Android Apps
        String moduelids = paramJobj.optString(Constants.moduleIds);
        if (!StringUtil.isNullOrEmpty(moduelids)) {
            ColumnConfigArr = fieldManagerServiceobj.getColumnHeadersConfigList(paramJobj);
            String moduleid = String.valueOf(Constants.Acc_Sales_Order_ModuleId);
            ColumnModelConfigArr = accCustomReportService.getCustomReportMeasureFieldJsonArray(ColumnConfigArr, moduleid, paramJobj);
            /* Batch Serials*/
            ColumnModelConfigArr = accFieldSetUpServiceDAOObj.getBatchSerialsFieldsJsonArray(ColumnConfigArr, moduleid, paramJobj);
            jobj.put(Constants.RES_METADATA, ColumnModelConfigArr);
        }  

        issuccess = true;
        jobj.put(Constants.RES_success, issuccess);
        if (totalCount != 0) {
            jobj.put(Constants.RES_TOTALCOUNT, totalCount);
        } else {
            jobj.put(Constants.RES_TOTALCOUNT, DataJArr.length());
        }
        jobj.put(Constants.RES_data, DataJArr);
        jobj.put(Constants.RES_success, true);
        return jobj;
    }

     private JSONObject createJsonCreditNoteForReport(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject cnJson = paramJobj;
        String companyid = cnJson.getString(Constants.companyKey);
        cnJson.put(Constants.companyids, companyid);
        cnJson.put("pendingapproval", cnJson.optString("pendingapprovalflag", ""));
        cnJson.put("isCreditNote", cnJson.optString("creditnoteflag", "true"));
        cnJson.put("consolidateFlag", cnJson.optString("consolidateflag", "false"));
        cnJson.put("deleted", cnJson.optString("deletedflag", "false"));
        cnJson.put("nondeleted", cnJson.optString("nondeletedflag", "false"));
        cnJson.put("cntype", "1");
        cnJson.put("ss", cnJson.optString("searchvalue", ""));

        if (cnJson.has("creditnoteforcustomersflag") && cnJson.optBoolean("creditnoteforcustomersflag", false)) {//credit note for customers
            cnJson.put("cntype", "1");
        }

        if (cnJson.has("creditnoteforvendorsflag") && cnJson.optBoolean("creditnoteforvendorsflag", false)) {//credit note for vendors
            cnJson.put("cntype", "4");
        }

        if (cnJson.has("openingcreditnoteforcustomersflag") && cnJson.optBoolean("openingcreditnoteforcustomersflag", false)) {//opening credit note for customers
            cnJson.put("cntype", "10");
        }

        if (cnJson.has("openingcreditnoteforvendorsflag") && cnJson.optBoolean("openingcreditnoteforvendorsflag", false)) {//opening credit note for vendors
            cnJson.put("cntype", "11");
        }

        if (cnJson.has("creditnotewithsalesreturnflag") && cnJson.optBoolean("creditnotewithsalesreturnflag", false)) {//credit note with salesreturn
            cnJson.put("cntype", "12");
        }

        if (cnJson.has("creditnotewithoutsalesreturnflag") && cnJson.optBoolean("creditnotewithoutsalesreturnflag", false)) {
            cnJson.put("cntype", "13");
        }

        return cnJson;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCreditNote(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException {

        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        JSONArray DataJArr = new JSONArray();
        JSONArray ColumnConfigArr = new JSONArray();
        JSONArray ColumnModelConfigArr = new JSONArray();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (!paramJobj.has(Constants.globalCurrencyKey)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }

            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {//Other than Android
                paramJobj = wsUtilService.replaceBooleanwithStringValues(paramJobj);
                paramJobj = createJsonCreditNoteForReport(paramJobj);
                String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
                if (paramJobj.has(Constants.REQ_startdate)) {
                    paramJobj.put(Constants.REQ_startdate, WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.REQ_startdate), originalDateFormat));
                }
                if (paramJobj.has(Constants.REQ_enddate)) {
                    paramJobj.put(Constants.REQ_enddate, WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.REQ_enddate), originalDateFormat));
                }
            }
            HashMap dataHashMap = accCreditNoteService.getCreditNoteCommonCode(paramJobj);
            DataJArr = (JSONArray) dataHashMap.get("data");

            boolean getlineItemDetailsflag = (paramJobj.optString("getlineItemDetailsflag", null) != null) ? Boolean.FALSE.parseBoolean((String) paramJobj.get("getlineItemDetailsflag")) : false;
            if (getlineItemDetailsflag) {//fetch line item details
                for (int i = 0; i < DataJArr.length(); i++) {
                    JSONObject creditNoteJsonobj = DataJArr.getJSONObject(i);
                    if (creditNoteJsonobj.has(Constants.billid)) {
                        String billid = creditNoteJsonobj.optString(Constants.billid, null);
                        JSONObject creditnoterowsJobj = accCreditNoteServiceCMN.getCreditNoteRows(paramJobj, billid.split(","));
                        JSONArray DataRowsArr = creditnoterowsJobj.getJSONArray(Constants.data);
                        creditNoteJsonobj.put(Constants.lineItemDetails, DataRowsArr);
                    }
                }
            }

            JSONArray pagedJson = DataJArr;
            String start = paramJobj.optString(Constants.start);
            String limit = paramJobj.optString(Constants.limit);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }

            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {//For Android only
                ColumnConfigArr = fieldManagerServiceobj.getColumnHeadersConfigList(paramJobj);
                String moduleid = String.valueOf(Constants.Acc_Credit_Note_ModuleId);
                ColumnModelConfigArr = accCustomReportService.getCustomReportMeasureFieldJsonArray(ColumnConfigArr, moduleid, paramJobj);
                jobj.put(Constants.RES_METADATA, ColumnModelConfigArr);
            }
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, "Exception While Editing User", ex);
        } finally {
            jobj.put(Constants.RES_success, issuccess);
            jobj.put(Constants.RES_TOTALCOUNT, DataJArr.length());
            jobj.put(Constants.RES_data, DataJArr);
        }
        return jobj;
    }
        
    private JSONObject createJsonSalesReturnForReport(JSONObject paramJobj) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        JSONObject srJson = paramJobj;
        String companyid = srJson.getString(Constants.companyKey);
        srJson.put(Constants.companyids, companyid);
        srJson.put("consolidateFlag", srJson.optString("consolidateflag", "false"));
        srJson.put("deleted", srJson.optString("deletedflag", "false"));
        srJson.put("nondeleted", srJson.optString("nondeletedflag", "false"));
        srJson.put("ss", srJson.optString("searchvalue", ""));
        srJson.put("isNoteReturns", srJson.optString("srwithcreditnoteflag", "false"));
        srJson.put("isfavourite", paramJobj.optString("favouriteflag", "false"));

        if (srJson.has("mobiletransactionsflag") && srJson.optBoolean("mobiletransactionsflag", false)) {
            srJson.put("generatedSource", "1");
        }
        if (srJson.has("productvalue") && srJson.get("productvalue") != null) {//only for productvalue and customer value filter only
            srJson = wsUtilService.manipulateGlobalLevelFieldsNew(srJson, companyid);
        }
        return srJson;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONArray DataJArr = new JSONArray();
        JSONArray ColumnConfigArr = new JSONArray();
        JSONObject jobj = new JSONObject();
        int totalCount = 0;
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (!paramJobj.has(Constants.globalCurrencyKey) || !paramJobj.has(Constants.REQ_startdate) || !paramJobj.has(Constants.REQ_enddate)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {//Other than Android
                paramJobj = wsUtilService.replaceBooleanwithStringValues(paramJobj);
                paramJobj = createJsonSalesReturnForReport(paramJobj);
                String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
                if (paramJobj.has(Constants.REQ_startdate)) {
                    paramJobj.put(Constants.REQ_startdate, WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.REQ_startdate), originalDateFormat));
                }
                if (paramJobj.has(Constants.REQ_enddate)) {
                    paramJobj.put(Constants.REQ_enddate, WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.REQ_enddate), originalDateFormat));
                }
            }
            
            //advancesearch code ERP-40423 for Sales Return Report- POS
            paramJobj.put(Constants.moduleid, String.valueOf(Constants.Acc_Sales_Return_ModuleId));
            paramJobj = wsUtilService.buildAdvanceSearchJson(paramJobj);
            //ERP-41214:Show asterisk to unit price and amount
            paramJobj = wsUtilService.getUserPermissionsforUnitPriceAndAmount(paramJobj);
            
            HashMap<String, Object> requestParams = accInvoiceServiceDAO.getDeliveryOrdersMapJSON(paramJobj);
            String companyid = paramJobj.getString(Constants.companyKey);
            String gcurrencyid = paramJobj.getString(Constants.globalCurrencyKey);
            String moduleid = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.moduleid, null)) ?String.valueOf(Constants.Acc_Sales_Return_ModuleId) : paramJobj.getString(Constants.moduleid);
            requestParams.put(Constants.moduleid, moduleid);
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
            KwlReturnObject extracompanyResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) extracompanyResult.getEntityList().get(0);
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                Map<String, Object> salesPersonParams = new HashMap<>();
                salesPersonParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                salesPersonParams.put(Constants.companyKey, companyid);
                salesPersonParams.put("grID", "15");
                KwlReturnObject masterItemByUserList = accountingHandlerDAOobj.getMasterItemByUserID(salesPersonParams);
                List<MasterItem> masterItems = masterItemByUserList.getEntityList();
                String salesPersons = "";
                StringBuffer salesPersonids = new StringBuffer();
                for (Object obj : masterItems) {
                    if (obj != null) {
                        salesPersonids.append(obj.toString() + ",");
                    }
                }
                if (salesPersonids.length() > 0) {
                    salesPersons = salesPersonids.substring(0, (salesPersonids.length() - 1));
                    requestParams.put("salesPersonid", salesPersons);
                }
            }
            boolean isSalesReturnCreditNote = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteReturns", null))) {
                isSalesReturnCreditNote = Boolean.parseBoolean(paramJobj.getString("isNoteReturns"));
            }
            requestParams.put("isSalesReturnCreditNote", isSalesReturnCreditNote);

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("searchJson", null))) {
                requestParams.put("searchJson", paramJobj.optString("searchJson", null));
                requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid));
                requestParams.put("filterConjuctionCriteria", paramJobj.optString("filterConjuctionCriteria"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("linknumber", null))) {
                requestParams.put("linknumber", paramJobj.getString("linknumber"));
            }
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("dir", null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("sort", null))) {
                dir = paramJobj.getString("dir");
                sort = paramJobj.getString("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }

            KwlReturnObject result = accInvoiceDAOobj.getSalesReturn(requestParams);
            totalCount = result.getRecordTotalCount();
            DataJArr = accInvoiceServiceDAO.getSalesReturnJson(paramJobj, result.getEntityList());
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {//Other than Android
                ColumnConfigArr = fieldManagerServiceobj.getColumnHeadersConfigList(paramJobj);
                String module = String.valueOf(Constants.Acc_Sales_Return_ModuleId);
                ColumnConfigArr = accCustomReportService.getCustomReportMeasureFieldJsonArray(ColumnConfigArr, module, paramJobj);
                /*
                 * Batch Serials
                 */
                ColumnConfigArr = accFieldSetUpServiceDAOObj.getBatchSerialsFieldsJsonArray(ColumnConfigArr, moduleid, paramJobj);
                jobj.put(Constants.RES_METADATA, ColumnConfigArr);
            }

        } catch (ParseException e) {
            // Error Connecting to Server
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            if (totalCount != 0) {
                jobj.put(Constants.RES_TOTALCOUNT, totalCount);
            } else {
                jobj.put(Constants.RES_TOTALCOUNT, DataJArr.length());
            }
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_success, true);

        }
        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject saveSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject response = new JSONObject();
        Date transactionDate = null; // required to check the active days of transaction configured in User Administration
        try {

            if (!paramJobj.has(Constants.remoteIPAddress) || !paramJobj.has(Constants.useridKey) || !paramJobj.has(Constants.companyKey) || !paramJobj.has(Constants.detail)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            } else {
                JSONArray salesODetailArray = new JSONArray(paramJobj.getString(Constants.detail));
                if (salesODetailArray.length() < 1) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }

            if (!paramJobj.has(Constants.deleted) && paramJobj.optBoolean(Constants.deleted, false) == true) {
                throw ServiceException.FAILURE("Missing required field", "erp27", false);
            }

            boolean isNoteAlso = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteAlso", null))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
                isNoteAlso = Boolean.parseBoolean(paramJobj.getString("isNoteAlso"));
            }

            boolean assignSRnumbertocn = !StringUtil.isNullOrEmpty(paramJobj.optString("AssignSRNumberntocn", null)) && !paramJobj.optString("AssignSRNumberntocn", null).equals("false") ? true : false;
            String companyid = paramJobj.optString(Constants.companyKey);
            String userid = paramJobj.optString(Constants.useridKey);
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isEdit, null)) ? false : paramJobj.optBoolean(Constants.isEdit, false);
            String billid = paramJobj.optString(Constants.billid);
            //For ERP-IOS Integration
            if (paramJobj.optBoolean(Constants.isForPos) && paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                String srSeq = null;
                String cnSeq = null;
                String walkinCustomerId = null;
                paramJobj = wsUtilService.replaceBooleanwithStringValues(paramJobj);
                
                //For edit case it is not required to fetch the store configuration set in ERP-POS Mapping.It is called in case of Create Case
                if (!isEdit) {
                    //fetching sales return sequence format, walkin customer id and credit note sequence format which is mandatory to save transaction
                    KwlReturnObject returnObj = accPOSInterfaceDAO.getPOSConfigDetails(paramJobj);
                    if (returnObj.getEntityList() != null && returnObj.getRecordTotalCount() > 0 && returnObj.getEntityList().size() == 1 && returnObj.getEntityList().get(0) != null) {
                        List<Object[]> detailList = returnObj.getEntityList();

                        if (detailList.size() == 1) {
                            Object[] row = (Object[]) returnObj.getEntityList().get(0);
                            if (row[8] != null) {
                                srSeq = row[8].toString();
                                paramJobj.put(Constants.sequenceformat, srSeq);
                            } else {
                                throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.srSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                            }

                            //if sr with credit note option is true
                            if (isNoteAlso) {
                                if (row[9] != null) {
                                    cnSeq = row[9].toString();
                                    //if credit note flag is true then put sequenceformat of cn in jsonobject else it will create sr with cn at back end
                                    paramJobj.put("cndnsequenceformat", cnSeq);
                                } else {
                                    throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.cnSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                                }
                            }
  
                            if (row[1] != null) {
                                walkinCustomerId = row[1].toString();
                            }
                            //Walkin Customer
                            if (!paramJobj.has("customer") && StringUtil.isNullOrEmpty(paramJobj.optString("customer"))&& !StringUtil.isNullOrEmpty(walkinCustomerId)) {
                                paramJobj.put("customer", walkinCustomerId);
                                KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), walkinCustomerId);
                                Customer cObj = (Customer) customerResult.getEntityList().get(0);
                                if (cObj != null) {
                                    paramJobj.put("acccode", cObj.getAcccode());
                                } else {
                                    throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.defaultCustomerPOS", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                                }
                            }
                        }
                    } else {//message: please set the configuration for Store
                        throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.settingForStoreMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                    }      
                } else if (!StringUtil.isNullOrEmpty(billid) && isEdit) { //for edit case
                    //fetching salesreturnno,transactiondate,creditnote flag and sequeceformat of credit note type
                    Map<String, Object> filterMap = new HashMap();
                    filterMap.put("ID", billid);
                    List columnList = kwlCommonTablesDAOObj.getRequestedObjectFieldsInCollection(SalesReturn.class, new String[]{"salesReturnNumber", "orderDate", "isNoteAlso", "isAssignSRNumberntocn"}, filterMap);
                    if (columnList != null && !columnList.isEmpty()) {
                        for (Object object : columnList) {
                            Object[] columnData = (Object[]) object;
                            String srno = columnData[0] != null ? ((String) columnData[0]) : null;
                            paramJobj.put("salesreturnno", srno);
                            transactionDate = columnData[1] != null ? ((Date) columnData[1]) : null;
                            isNoteAlso = columnData[2] != null ? ((Boolean) columnData[2]) : false;
                            assignSRnumbertocn = columnData[3] != null ? ((Boolean) columnData[3]) : false;
                        }
                    }
                }

                //Checking Active Days Case where it doesn't allow to edit if the document date doesn't come in active days .
                if (transactionDate != null && isEdit) {
                    wsUtilService.checkUserActivePeriodRange(companyid, userid, transactionDate, Constants.Acc_Sales_Return_ModuleId);
                }
                
                //If sales return is assigned to credit number then replace cndnsequenceformat as NA 
                if (assignSRnumbertocn && isNoteAlso) {
                    paramJobj.put("cndnsequenceformat", "NA");

                } else if (!isNoteAlso) {
                    paramJobj.remove("cndnsequenceformat");
                }

                // On the basis of salesreturnno fetching the sales return details for edit and saving transactions
                if (paramJobj.has("salesreturnno") && !StringUtil.isNullOrEmpty(paramJobj.optString("salesreturnno", null))) {
                    //In edit case
                    if (isEdit) {
                    KwlReturnObject mpResult = accInvoiceDAOobj.getSalesReturnCount(paramJobj.optString("salesreturnno"),companyid );
                        SalesReturn srObj = (SalesReturn) mpResult.getEntityList().get(0);
                        if (srObj != null) {
                            paramJobj.put("number", srObj.getSalesReturnNumber());
                            paramJobj.put("billid", srObj.getID());
                            if (srObj.getSeqformat() != null) {
                                paramJobj.put(Constants.sequenceformat, srObj.getSeqformat().getID());
                            }
                            paramJobj.put(Constants.isEdit, isEdit);
                            paramJobj.put("customer", srObj.getCustomer()!=null?srObj.getCustomer().getID():"");

                            CreditNote creditNote = null;
                            if (srObj.isIsNoteAlso()) {
                                KwlReturnObject creditnoteresult = accCreditNoteDAOobj.getCreditNoteIdFromSRId(srObj.getID(), companyid);
                                if (!creditnoteresult.getEntityList().isEmpty()) {
                                    creditNote = (CreditNote) creditnoteresult.getEntityList().get(0);
                                }
                                if (creditNote != null) {
                                    paramJobj.put("cndnsequenceformat", creditNote.getSeqformat());
                                    paramJobj.put("cndnnumber", creditNote != null ? creditNote.getCreditNoteNumber() : "");
                                }
                            }

                            //Checking whether the already saved linked Invoice is equal to passed Invoice id. If same allow to save.If not then dnt allow to proceed by showing msg.
                            List<String> linkIdNumberList = new ArrayList<>();
                            String linkNUmbers = paramJobj.optString("linkNumber");
                            String[] linkNumbers = linkNUmbers.split(",");
                            for (int io = 0; io < linkNumbers.length; io++) {
                                String linkid = linkNumbers[io];
                                if (!StringUtil.isNullOrEmpty(linkid)) {
                                    linkIdNumberList.add(linkid);
                                }
                            }

                            //If another invoices is being linked in edit mode then it does not allow to edit
                            Set<SalesReturnDetail> doRows = srObj.getRows();
                            if (doRows != null && !doRows.isEmpty()) {
                                for (SalesReturnDetail temp : doRows) {
                                    String invoiceid = temp.getCidetails() != null ? temp.getCidetails().getInvoice().getID() : null;
                                    if (!StringUtil.isNullOrEmpty(invoiceid)) {
                                        if (!linkIdNumberList.contains(invoiceid)) {
                                            throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.cannotLinkAnotherInvoiceInCN", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                                        }
                                    }
                                }
                            }
                        } else {
                            throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.invalidSalesReturnRecord", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                        }
                    }
                }

                if (!paramJobj.has(Constants.externalcurrencyrate) || StringUtil.isNullOrEmpty(paramJobj.optString(Constants.externalcurrencyrate))) {
                    paramJobj.put(Constants.externalcurrencyrate, "1");
                }

                //to get new next numberof sequenceformat
                paramJobj.put("from", "61");
            }

            //call to web-application function for saving
            response = accSalesReturnService.saveSalesReturn(paramJobj);
            if (response.has(Constants.RES_msg)) {
                response.put(Constants.RES_MESSAGE, response.getString(Constants.RES_msg));
                response.remove(Constants.RES_msg);
            }

        } catch (JSONException | ServiceException | SessionExpiredException ex) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
      
        return response;
    }
    
  @Override
    public JSONObject saveSalesOrder(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException, ParseException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject response = new JSONObject();
        if (!paramJobj.has(Constants.remoteIPAddress) || !paramJobj.has(Constants.useridKey) || !paramJobj.has(Constants.companyKey) || !paramJobj.has(Constants.detail)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } else {
            JSONArray salesODetailArray = new JSONArray(paramJobj.getString(Constants.detail));
            if (salesODetailArray.length() < 1) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
        }

      String creationdate = null;
      paramJobj.put(Constants.isdefaultHeaderMap, true);
      
      if (paramJobj.has("billdate") && !StringUtil.isNullOrEmpty(paramJobj.optString("billdate"))) {
          creationdate = paramJobj.optString("billdate") == null ? null : paramJobj.optString("billdate");
          paramJobj.put("creationdate", creationdate);
          if (!StringUtil.isNullOrEmpty(creationdate)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
              Date transdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).parse(creationdate);
              DateFormat df = authHandler.getDateOnlyFormat();
              creationdate = df.format(transdate);
              paramJobj.put("OrderDate", creationdate);
              if (StringUtil.isNullOrEmpty(paramJobj.optString("duedate", null))) {
                  paramJobj.put("duedate", creationdate);
              }
          }
      }
        //Case:link advance receipts -ERP-39696
        if (paramJobj.has("receiptid") && !StringUtil.isNullOrEmpty(paramJobj.optString("receiptid", null))) {
            String detailIds=null;
            StringBuilder detailIdBuilder=new StringBuilder();
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paramJobj.optString("receiptid"));
            Receipt receipt = (Receipt) objItr.getEntityList().get(0);
            Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
            for (ReceiptAdvanceDetail advanceDetail : advanceDetails) {
                detailIdBuilder.append(advanceDetail.getId()+",");
            }
            if (detailIdBuilder.length() > 0) {
                detailIds = detailIdBuilder.toString();
                String advancedetailid = detailIds.substring(0, detailIds.length()-1);
                paramJobj.put("linkedAdvancePaymentId", advancedetailid);
                paramJobj.put("linkedAdvancePaymentNo", paramJobj.optString("receiptno"));
                paramJobj.put("isLinkadvancereceipts", true);
            }
        }

        paramJobj = wsUtilService.manipulateGlobalLevelFieldsNew(paramJobj, paramJobj.optString(Constants.companyKey));

        JSONObject jobj = new JSONObject();
        response = accSalesOrderServiceobj.saveSalesOrderJSON(paramJobj);
        jobj = new JSONObject(response.toString());
        if (response.has(Constants.RES_msg)) {
            jobj.put(Constants.RES_MESSAGE, response.getString(Constants.RES_msg));
            response.remove(Constants.RES_msg);
        }
        if (!jobj.has(Constants.RES_success)) {
            jobj.put(Constants.RES_success, false);
        } 
        return jobj;
    }
  
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteSalesOrder(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        
        //Create json for delete by passing sales order no in comma separated format such as SO1,SO2
        if (paramJobj.has("salesorderno") && !StringUtil.isNullOrEmpty(paramJobj.optString("salesorderno", null))) {
            paramJobj = createDeleteJSON(paramJobj, String.valueOf(Constants.Acc_Sales_Order_ModuleId));
        }
        if (!paramJobj.has(Constants.RES_data)||!paramJobj.has(Constants.remoteIPAddress)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } else {
            JSONArray salesorderArray =new JSONArray(paramJobj.getString(Constants.RES_data));
            if (salesorderArray.length() < 1) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            for (int i = 0; i < salesorderArray.length(); i++) {
                JSONObject salesOrderObj = salesorderArray.getJSONObject(i);
                if (!salesOrderObj.has(Constants.billid)) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
        }
        //delete permanent
        if (paramJobj.has(Constants.deletepermanentflag) && paramJobj.optBoolean(Constants.deletepermanentflag, false) == true) {
            response = accSalesOrderServiceobj.deleteSalesOrdersPermanentJson(paramJobj);
        } else {
            //delete temporary
            response = accSalesOrderServiceobj.deleteSalesOrdersJSON(paramJobj);
        }

        if (response.has(Constants.RES_msg)) {
            response.put(Constants.RES_MESSAGE, response.getString(Constants.RES_msg));
            response.remove(Constants.RES_msg);
        }
        response.put(Constants.RES_success, true);

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCustomerInvoicesReport(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        jobj=wsUtilService.populateAdditionalInformation(paramJobj);
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            String projectid = paramJobj.getString("projectid");
            String currencyid = paramJobj.getString(Constants.currencyKey);

            DateFormat sdf = new SimpleDateFormat(Constants.yyyyMMdd);

            Date startDate = authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            Date endDate = authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.REQ_startdate, null))) {
                startDate = sdf.parse(paramJobj.getString(Constants.REQ_startdate));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.REQ_enddate, null))) {
                endDate = sdf.parse(paramJobj.getString(Constants.REQ_enddate));
            }
            /*---------isForProjectRecoveryReport falg is true when service is called from PM for all invoices tagged against a relevant Project--------- */
            jobj.put("isForProjectRecoveryReport", paramJobj.optBoolean("isForProjectRecoveryReport", false));
            jobj = getCustomerInvoicesReportData(jobj, companyid, startDate, endDate, projectid, currencyid);

        } catch (ParseException e) {
            // Error Connecting to Server
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private JSONObject getCustomerInvoicesReportData(JSONObject obj, String companyid, Date startDate, Date endDate, String projectid, String currencyid) throws ServiceException, JSONException {
        SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();

        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) companyResult.getEntityList().get(0);
        String gcurrencyid = company.getCurrency().getCurrencyID();

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
        endDate.setHours(24);
        endDate.setMinutes(0);
        String searchString = getSearchColumns(companyid, projectid);

        HashMap<String, Object> reqParams = new HashMap<>();
        reqParams.put(Constants.companyKey, companyid);
                  
         /*---------isForProjectRecoveryReport falg is true when service is called from PM for all invoices tagged against a relevant Project--------- */
        
       /* ----------We do not apply date filter because we need all invoices tagged against a relevant project -------------*/
                             
        if (!obj.optBoolean("isForProjectRecoveryReport", false)) {
            reqParams.put(Constants.REQ_startdate, startDate);
            reqParams.put(Constants.REQ_enddate, endDate);
        }     
        reqParams.put("searchstring", searchString);
        KwlReturnObject invResult = accInvoiceDAOobj.getInvoicesWithSearchColumn(reqParams);
        List list = invResult.getEntityList();

        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();

        while (itr.hasNext()) {
            Double amount = 0.0;
            String invoicenum = "";
            String jenum = "";
            String date = "";
            String invid = itr.next().toString();

            double rate;
            KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
            Invoice invoice = (Invoice) invoiceResult.getEntityList().get(0);
            if(invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()){
                rate = invoice.getExchangeRateForOpeningTransaction();
            }
            else{
                rate = invoice.getJournalEntry().getExternalCurrencyRate();
            }
            JournalEntryDetail d = invoice.getCustomerEntry();
            amount = d.getAmount();
            if (!invoice.getCurrency().getCurrencyID().equals(currencyid)) {
//                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(),invoice.getJournalEntry().getEntryDate(), rate);
                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(),invoice.getCreationDate(), rate);
                Double baseAmount = (Double) bAmt.getEntityList().get(0);
//                bAmt = getBaseToCurrencyAmount(requestParams, baseAmount, currencyid, invoice.getJournalEntry().getEntryDate(), 0);
                bAmt = getBaseToCurrencyAmount(requestParams, baseAmount, currencyid, invoice.getCreationDate(), 0);
                amount = (Double) bAmt.getEntityList().get(0);
            }
            amount = authHandler.round(amount, companyid);

            invoicenum = invoice.getInvoiceNumber();
            jenum = invoice.getJournalEntry().getEntryNumber();
//            date = sdf.format(invoice.getJournalEntry().getEntryDate());
            date = sdf.format(invoice.getCreationDate());

            JSONObject jobj = new JSONObject();
            jobj.put("invoiceno", invoicenum);
            jobj.put("cost", amount);
            jobj.put("jenum", jenum);
            jobj.put("date", date);
            jArr.put(jobj);

        }
        obj.put(Constants.RES_data, jArr);
        obj.put(Constants.RES_TOTALCOUNT, jArr.length());

        return obj;
    }

    private String getSearchColumns(String companyid, String projectid) throws ServiceException {
        String searchString = "";

        KwlReturnObject returnObject = accountingHandlerDAOobj.getFieldParamsForProject(companyid, projectid);
        List<Object[]> list = returnObject.getEntityList();
        for(Object[] oj : list) {
            String columnno = (oj[0]).toString();
            String id = (oj[1]).toString();

            KwlReturnObject cmbReturnObject = accountingHandlerDAOobj.getFieldComboDataForProject(id, projectid);
            List<String> listCmbData = cmbReturnObject.getEntityList();
            //If project id is present that searchfield is added
            for(String comboid : listCmbData) {

                searchString += "col" + columnno + " = '" + comboid + "' or ";
            }
        }
        if (searchString.length() > 0) {
            searchString = searchString.substring(0, searchString.lastIndexOf("or"));
            searchString = "and (" + searchString + ")";
        }

        return searchString;
    }

    private KwlReturnObject getCurrencyToBaseAmount(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();

        if (Amount != 0) {
            if (rate == 0) {
                KwlReturnObject result = accCurrencyDAOobj.getExcDetailID(request, currencyid, transactiondate, null);
                List li = result.getEntityList();
                if (!li.isEmpty()) {
                    Iterator itr = li.iterator();
                    ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                    rate = erd.getExchangeRate();
                }
            }
            Amount = Amount / rate;
        }

        list.add(Amount);
        return new KwlReturnObject(true, null, null, list, list.size());

    }

    private KwlReturnObject getBaseToCurrencyAmount(Map request, Double Amount, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();

        if (Amount != 0) {
            if (rate == 0) {
                KwlReturnObject result = accCurrencyDAOobj.getExcDetailID(request, newcurrencyid, transactiondate, null);
                List li = result.getEntityList();
                if (!li.isEmpty()) {
                    Iterator itr = li.iterator();
                    ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                    rate = erd.getExchangeRate();
                }
            }
            Amount = Amount * rate;
        }

        list.add(Amount);
        return new KwlReturnObject(true, null, null, list, list.size());

    }

//    @Override
//    public JSONObject getCustomerInvoicesReport(JSONObject jobj) throws ServiceException, JSONException {
//        jobj = wsUtilService.populateAdditionalInformation(jobj);
//        JSONObject response = new JSONObject();
//        String companyid = jobj.getString("companyid");
//        String projectid = jobj.getString("projectid");
//        String currencyid = jobj.getString("currencyid");
//
//        String originalDateFormatPattern = jobj.optString("dateformat", "yyyy-MM-dd");
//        DateFormat originalDateFormat = new SimpleDateFormat(originalDateFormatPattern);
//        Date startDate = null;
//        Date endDate = null;
//        try {
//            startDate = authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
//            endDate = authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
//            if (!StringUtil.isNullOrEmpty(jobj.getString("startdate"))) {
//                startDate = originalDateFormat.parse(jobj.getString("startdate"));
//            }
//            if (!StringUtil.isNullOrEmpty(jobj.getString("enddate"))) {
//                endDate = originalDateFormat.parse(jobj.getString("enddate"));
//            }
//        } catch (ParseException | SessionExpiredException ex) {
//            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        }
//
//        SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();
//
//        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
//        Company company = (Company) companyResult.getEntityList().get(0);
//        String gcurrencyid = company.getCurrency().getCurrencyID();
//
//        HashMap<String, Object> requestParams = new HashMap<>();
//        requestParams.put("companyid", companyid);
//        requestParams.put("gcurrencyid", gcurrencyid);
//        endDate.setHours(24);
//        endDate.setMinutes(0);
//        String searchString = getSearchColumns(companyid, projectid);
//
//        HashMap<String, Object> reqParams = new HashMap<>();
//        reqParams.put("companyid", companyid);
//        reqParams.put("startdate", startDate);
//        reqParams.put("enddate", endDate);
//        reqParams.put("searchstring", searchString);
//        KwlReturnObject invResult = accInvoiceDAOobj.getInvoicesWithSearchColumn(reqParams);
//        List list = invResult.getEntityList();
//
//        Iterator itr = list.iterator();
//        JSONArray jArr = new JSONArray();
//
//        while (itr.hasNext()) {
//            Double amount = 0.0;
//            String invoicenum = "";
//            String jenum = "";
//            String date = "";
//            String invid = itr.next().toString();
//
//            KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
//            Invoice invoice = (Invoice) invoiceResult.getEntityList().get(0);
//            JournalEntryDetail d = invoice.getCustomerEntry();
//            amount = d.getAmount();
//            KwlReturnObject bAmt = getOneCurrencyToOther(requestParams, amount, invoice.getCurrency().getCurrencyID(), currencyid, invoice.getJournalEntry().getEntryDate(), invoice.getExternalCurrencyRate());
//            amount = (Double) bAmt.getEntityList().get(0);
//            amount = authHandler.round(amount, 2);
//
//            invoicenum = invoice.getInvoiceNumber();
//            jenum = invoice.getJournalEntry().getEntryNumber();
//            date = sdf.format(invoice.getJournalEntry().getEntryDate());
//
//            JSONObject tempjobj = new JSONObject();
//            tempjobj.put("invoiceno", invoicenum);
//            tempjobj.put("cost", amount);
//            tempjobj.put("jenum", jenum);
//            tempjobj.put("date", date);
//            jArr.put(tempjobj);
//
//        }
//        response.put(Constants.RES_data, jArr);
//        response.put(Constants.RES_success, true);
//        response.put(Constants.RES_TOTALCOUNT, jArr.length());
//        return response;
//    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getVendorInvoicesReport(JSONObject jobj) throws ServiceException, JSONException, SessionExpiredException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject response = new JSONObject();
        JSONObject obj = new JSONObject();
        response.put(Constants.RES_success, true);

        try {
            boolean isComapnyExist = wsUtilService.isCompanyExists(jobj);
            if (isComapnyExist) {

                String companyid = jobj.getString(Constants.companyKey);
                String projectid = jobj.getString("projectid");
                String currencyid = jobj.getString(Constants.currencyKey);

                DateFormat sdf = new SimpleDateFormat(Constants.yyyyMMdd);

                Date startDate = authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
                Date endDate = authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
                if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.REQ_startdate))) {
                    startDate = sdf.parse(jobj.getString(Constants.REQ_startdate));
                }
                if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.REQ_enddate))) {
                    endDate = sdf.parse(jobj.getString(Constants.REQ_enddate));
                }

                obj = getVendorInvoicesReportData(obj, companyid, startDate, endDate, projectid, currencyid);
            } else {
                response.put(Constants.RES_success, false);
            }

        } catch (ParseException ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        response.put(Constants.RES_data, obj);
        return response;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private JSONObject getVendorInvoicesReportData(JSONObject obj, String companyid, Date startDate, Date endDate, String projectid, String currencyid) throws ServiceException, JSONException {
        SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();

        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) companyResult.getEntityList().get(0);
        String gcurrencyid = company.getCurrency().getCurrencyID();

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
        endDate.setHours(24);
        endDate.setMinutes(0);

        String searchString = getSearchColumns(companyid, projectid);

        HashMap<String, Object> reqParams = new HashMap<>();
        reqParams.put(Constants.companyKey, companyid);
        reqParams.put(Constants.REQ_startdate, startDate);
        reqParams.put(Constants.REQ_enddate, endDate);
        reqParams.put("searchstring", searchString);
        KwlReturnObject invResult = accGoodsReceiptobj.getGoodsReceiptsWithSearchColumn(reqParams);
        List list = invResult.getEntityList();

        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();

        while (itr.hasNext()) {
            Double amount = 0.0;
            String invoicenum = "";
            String jenum = "";
            String date = "";
            String invid = itr.next().toString();

            KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
            GoodsReceipt invoice = (GoodsReceipt) grResult.getEntityList().get(0);
            JournalEntryDetail d = invoice.getVendorEntry();
            double rate ;
            if(invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()){
                rate = invoice.getExchangeRateForOpeningTransaction();
            }
            else{
                rate = invoice.getJournalEntry().getExternalCurrencyRate();
            }
            amount = d.getAmount();   
            if (!invoice.getCurrency().getCurrencyID().equals(currencyid)) {
//                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(),invoice.getJournalEntry().getEntryDate(), rate);
                KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(),invoice.getCreationDate(), rate);
                Double baseAmount = (Double) bAmt.getEntityList().get(0);
//                bAmt = getBaseToCurrencyAmount(requestParams, baseAmount, currencyid, invoice.getJournalEntry().getEntryDate(), 0);
                bAmt = getBaseToCurrencyAmount(requestParams, baseAmount, currencyid, invoice.getCreationDate(), 0);
                amount = (Double) bAmt.getEntityList().get(0);
            }
            amount = authHandler.round(amount, companyid);

            invoicenum = invoice.getGoodsReceiptNumber();
            jenum = invoice.getJournalEntry().getEntryNumber();
//            date = sdf.format(invoice.getJournalEntry().getEntryDate());
            date = sdf.format(invoice.getCreationDate());

            JSONObject jobj = new JSONObject();
            jobj.put("invoiceno", invoicenum);
            jobj.put("cost", amount);
            jobj.put("jenum", jenum);
            jobj.put("date", date);
            jArr.put(jobj);

        }
        obj.put(Constants.RES_data, jArr);
        obj.put(Constants.RES_TOTALCOUNT, jArr.length());

        return obj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCashAndPurchaseRevenue(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject response = new JSONObject();
        JSONObject obj = new JSONObject();
        response.put(Constants.RES_success, true);
        
        try {
            boolean isComapnyExist = wsUtilService.isCompanyExists(jobj);
            if (isComapnyExist) {
                String companyid = jobj.getString(Constants.companyKey);
                String projectid = jobj.getString("projectid");
                String currencyid = jobj.getString(Constants.currencyKey);
                DateFormat sdf = new SimpleDateFormat(Constants.yyyyMMdd);
                
                Date startDate = sdf.parse(jobj.getString(Constants.REQ_startdate));
                Date endDate = sdf.parse(jobj.getString(Constants.REQ_enddate));
                
                obj = getVendorInvoicesRevenueData(obj, companyid, startDate, endDate, projectid, currencyid);
                obj = getCustomerInvoicesRevenueData(obj, companyid, startDate, endDate, projectid, currencyid);
            } else {
                response.put(Constants.RES_success, false);
            }
            
        } catch (ParseException e) {
            // Error Connecting to Server
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(e.getMessage(), e);
        } 
        response.put(Constants.RES_data, obj);
        return response;
    }

    private void getDimensionJSONForPM(JSONObject costCategoryFieldJsonObj, FieldComboData fieldComboData, double amount) throws JSONException {

        costCategoryFieldJsonObj.put("id", fieldComboData.getId());
        costCategoryFieldJsonObj.put("fieldId", fieldComboData.getField().getId());
        costCategoryFieldJsonObj.put("costCategoryName", fieldComboData.getValue());
        costCategoryFieldJsonObj.put("categoryCost", amount);

        boolean leaf = false;
        String parentId = "";
        String parentName = "";
        Integer level = new Integer(0);
        HashMap<String, Integer> levelMap = new HashMap<String, Integer>();
        levelMap.put("level", level);

        if (fieldComboData.getParent() != null) {
            parentId = fieldComboData.getParent().getId();
            parentName = fieldComboData.getParent().getValue();
            costCategoryFieldJsonObj.put("parentid", parentId);
            costCategoryFieldJsonObj.put("parentname", parentName);
            getLevel(fieldComboData.getParent(), levelMap);
        }

        if (fieldComboData.getChildren().isEmpty()) {
            leaf = true;
        }

        costCategoryFieldJsonObj.put("leaf", leaf);
        level = levelMap.get("level");
        costCategoryFieldJsonObj.put("level", level);

    }

    private void getLevel(FieldComboData fieldComboData, HashMap<String, Integer> levelMap) {
        int level = levelMap.get("level");
        level++;
        levelMap.put("level", level);
        if (fieldComboData.getParent() != null) {
            getLevel(fieldComboData.getParent(), levelMap);

        }
    }

    private JSONObject getVendorInvoicesRevenueData(JSONObject obj, String companyid, Date startDate, Date endDate, String projectid, String currencyid) throws ServiceException, JSONException {
        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) companyResult.getEntityList().get(0);
        String gcurrencyid = company.getCurrency().getCurrencyID();

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put(Constants.globalCurrencyKey, gcurrencyid);

        endDate.setHours(24);
        endDate.setMinutes(0);

        String costCategoryCustomFieldId = "";
        HashMap<String, String> customFieldMap = new HashMap<>();
        HashMap<String, String> customDateFieldMap = new HashMap<>();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Vendor_Invoice_ModuleId, 0));
//            fieldrequestParams.put("Session", session);
        HashMap<String, String> replaceFieldMap = new HashMap<>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

        replaceFieldMap = new HashMap<>();
        JSONArray purchaseCustomFieldJsonArray = new JSONArray();

        String searchString = getSearchColumns(companyid, projectid);
        HashMap<String, Object> reqParams = new HashMap<>();
        reqParams.put(Constants.companyKey, companyid);
        reqParams.put(Constants.REQ_startdate, startDate);
        reqParams.put(Constants.REQ_enddate, endDate);
        reqParams.put("searchstring", searchString);
        KwlReturnObject invResult = accGoodsReceiptobj.getGoodsReceiptsWithSearchColumn(reqParams);
        List list = invResult.getEntityList();

        Iterator itr = list.iterator();
        Double totalAmount = 0.0;
        while (itr.hasNext()) {
            Double amount = 0.0;
            JSONObject costCategoryFieldJsonObj = new JSONObject();
            String invid = itr.next().toString();
            KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
            GoodsReceipt invoice = (GoodsReceipt) grResult.getEntityList().get(0);
            JournalEntryDetail d = invoice.getVendorEntry();
            amount = d.getAmount();

//            KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getJournalEntry().getEntryDate(), invoice.getExternalCurrencyRate());
            KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getExternalCurrencyRate());
            amount = (Double) bAmt.getEntityList().get(0);
            amount = authHandler.round(amount, companyid);
            String jeId = invoice.getJournalEntry().getID();

            Map<String, Object> variableMap = new HashMap<String, Object>();
            KwlReturnObject accJECustomDataResult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
            AccJECustomData jeDetailCustom = (AccJECustomData) accJECustomDataResult.getEntityList().get(0);
            replaceFieldMap = new HashMap<>();
            if (jeDetailCustom != null) {
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                    String coldata = varEntry.getValue().toString();
                    if (customFieldMap.containsKey(varEntry.getKey())) {
                        KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                        FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);
                        if (fieldComboData != null && varEntry.getKey().equalsIgnoreCase("Custom_Cost Category")) {
                            costCategoryCustomFieldId = fieldComboData.getField().getId();
                            KwlReturnObject bAmtInRequiredCurrency = getBaseToCurrencyAmount(requestParams, amount, currencyid, new Date(), 0);
                            double amountInRequiredCurrency = (Double) bAmtInRequiredCurrency.getEntityList().get(0);
                            amountInRequiredCurrency = authHandler.round(amountInRequiredCurrency, companyid);
                            getDimensionJSONForPM(costCategoryFieldJsonObj, fieldComboData, amountInRequiredCurrency);
                            purchaseCustomFieldJsonArray.put(costCategoryFieldJsonObj);
                        }
                    }
                }
            }
            totalAmount += amount;
        }

        KwlReturnObject bAmt = getBaseToCurrencyAmount(requestParams, totalAmount, currencyid, new Date(), 0);
        totalAmount = (Double) bAmt.getEntityList().get(0);
        totalAmount = authHandler.round(totalAmount, companyid);

        HashMap<String, Object> params = new HashMap<>();
        params.put(Constants.companyKey, companyid);
        params.put("fieldname", "Custom_Cost Category");
        params.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
        KwlReturnObject fcdResult = accMasterItemsDAOobj.getFieldComboDataByFieldName(params);
        List fieldComboDataList = fcdResult.getEntityList();

        JSONArray newJArray = new JSONArray();

        Set<String> fieldComboDataInInvoicesSET = new HashSet<String>();

        for (int i = 0; i < purchaseCustomFieldJsonArray.length(); i++) {
            if (purchaseCustomFieldJsonArray.getJSONObject(i) != null) {
                String fieldComboDataId = purchaseCustomFieldJsonArray.getJSONObject(i).optString("id", "");
                if (!StringUtil.isNullOrEmpty(fieldComboDataId)) {
                    fieldComboDataInInvoicesSET.add(fieldComboDataId);
                }
            }
        }

        Iterator it = fieldComboDataList.iterator();
        while (it.hasNext()) {
            JSONObject jobj = new JSONObject();
            String id = it.next().toString();
            if (!fieldComboDataInInvoicesSET.contains(id)) {
                KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), id);
                FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);
                getDimensionJSONForPM(jobj, fieldComboData, 0.0);
                newJArray.put(jobj);
            }
        }

        for (int i = 0; i < newJArray.length(); i++) {
            JSONObject jSONObject = newJArray.getJSONObject(i);
            purchaseCustomFieldJsonArray.put(jSONObject);
        }

        purchaseCustomFieldJsonArray = getHierarachicalJsonArray(purchaseCustomFieldJsonArray, costCategoryCustomFieldId);

        obj.put("purchaserevenue", totalAmount);
        obj.put("purchaseCustomFieldData", purchaseCustomFieldJsonArray);

        return obj;
    }

    private JSONArray getHierarachicalJsonArray(JSONArray salesCustomFieldJsonArray, String costCategoryCustomFieldId) throws ServiceException, JSONException {
        JSONArray returnArray = new JSONArray();

        HashMap<String, Object> filterRequestParams = new HashMap<>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("field.id");
        filter_params.add(costCategoryCustomFieldId);
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        filterRequestParams.put("order_by", order_by);
        filterRequestParams.put("order_type", order_type);
//            KwlReturnObject result = getMasterItemsForCustomHire(session, filterRequestParams);
        KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsForCustomHire(filterRequestParams);

        List list = result.getEntityList();
        Iterator itr = list.iterator();

        JSONArray jArr = new JSONArray();

        while (itr.hasNext()) {
            Object[] row = (Object[]) itr.next();
            FieldComboData fieldComboData = (FieldComboData) row[0];
            String fieldComboDataId = fieldComboData.getId();
            double categoryCost = 0d;
            for (int i = 0; i < salesCustomFieldJsonArray.length(); i++) {
                JSONObject jobj = salesCustomFieldJsonArray.getJSONObject(i);
                if (fieldComboDataId.equalsIgnoreCase(jobj.optString("id", ""))) {
                    categoryCost += jobj.optDouble("categoryCost", 0);
                }
            }
            JSONObject obj = new JSONObject();

            obj.put("id", fieldComboData.getId());
            obj.put("name", fieldComboData.getValue());
            FieldComboData parentItem = (FieldComboData) row[3];
            if (parentItem != null) {
                obj.put("parentid", parentItem.getId());
                obj.put("parentname", parentItem.getValue());
            }
            obj.put("level", row[1]);
            obj.put("leaf", row[2]);
            obj.put("categoryCost", categoryCost);
            jArr.put(obj);

        }
        returnArray = jArr;

        return returnArray;
    }

    private JSONObject getCustomerInvoicesRevenueData(JSONObject obj, String companyid, Date startDate, Date endDate, String projectid, String currencyid) throws ServiceException, JSONException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";

        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) companyResult.getEntityList().get(0);
        String gcurrencyid = company.getCurrency().getCurrencyID();

        String costCategoryCustomFieldId = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put(Constants.globalCurrencyKey, gcurrencyid);

        SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();
        endDate.setHours(24);
        endDate.setMinutes(0);

        HashMap<String, String> customFieldMap = new HashMap<>();
        HashMap<String, String> customDateFieldMap = new HashMap<>();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Invoice_ModuleId, 0));
        HashMap<String, String> replaceFieldMap = new HashMap<>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

        replaceFieldMap = new HashMap<>();
        JSONArray salesCustomFieldJsonArray = new JSONArray();

        String searchString = getSearchColumns(companyid, projectid);

        HashMap<String, Object> reqParams = new HashMap<>();
        reqParams.put(Constants.companyKey, companyid);
        reqParams.put(Constants.REQ_startdate, startDate);
        reqParams.put(Constants.REQ_enddate, endDate);
        reqParams.put("searchstring", searchString);
        KwlReturnObject invResult = accInvoiceDAOobj.getInvoicesWithSearchColumn(reqParams);
        List list = invResult.getEntityList();

        Iterator itr = list.iterator();
        Double totalAmount = 0.0;
        while (itr.hasNext()) {
            Double amount = 0.0;
            JSONObject costCategoryFieldJsonObj = new JSONObject();

            String invid = itr.next().toString();
            KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
            Invoice invoice = (Invoice) invoiceResult.getEntityList().get(0);
            JournalEntryDetail d = invoice.getCustomerEntry();
            amount = d.getAmount();
//            KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getJournalEntry().getEntryDate(), invoice.getExternalCurrencyRate());
            KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getExternalCurrencyRate());
            amount = (Double) bAmt.getEntityList().get(0);
            amount = authHandler.round(amount, companyid);

            String jeId = invoice.getJournalEntry().getID();

            Map<String, Object> variableMap = new HashMap<>();
            KwlReturnObject jeCustomDataResult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
            AccJECustomData jeDetailCustom = (AccJECustomData) jeCustomDataResult.getEntityList().get(0);
            replaceFieldMap = new HashMap<>();
            if (jeDetailCustom != null) {
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                    String coldata = varEntry.getValue().toString();
                    if (customFieldMap.containsKey(varEntry.getKey())) {
                        KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                        FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);
                        if (fieldComboData != null && varEntry.getKey().equalsIgnoreCase("Custom_Cost Category")) {
                            costCategoryCustomFieldId = fieldComboData.getField().getId();
                            KwlReturnObject bAmtInRequiredCurrency = getBaseToCurrencyAmount(requestParams, amount, currencyid, new Date(), 0);
                            double amountInRequiredCurrency = (Double) bAmtInRequiredCurrency.getEntityList().get(0);
                            amountInRequiredCurrency = authHandler.round(amountInRequiredCurrency, companyid);
                            getDimensionJSONForPM(costCategoryFieldJsonObj, fieldComboData, amountInRequiredCurrency);
                            salesCustomFieldJsonArray.put(costCategoryFieldJsonObj);
                        }
                    }
                }
            }
            totalAmount += amount;
        }

        KwlReturnObject bAmt = getBaseToCurrencyAmount(requestParams, totalAmount, currencyid, new Date(), 0);
        totalAmount = (Double) bAmt.getEntityList().get(0);
        totalAmount = authHandler.round(totalAmount, companyid);

        HashMap<String, Object> params = new HashMap<>();
        params.put(Constants.companyKey, companyid);
        params.put("fieldname", "Custom_Cost Category");
        params.put("moduleid", Constants.Acc_Invoice_ModuleId);
        KwlReturnObject fcdResult = accMasterItemsDAOobj.getFieldComboDataByFieldName(params);
        List fieldComboDataList = fcdResult.getEntityList();

        JSONArray newJArray = new JSONArray();
        Set<String> fieldComboDataInInvoicesSET = new HashSet<String>();
        for (int i = 0; i < salesCustomFieldJsonArray.length(); i++) {
            if (salesCustomFieldJsonArray.getJSONObject(i) != null) {
                String fieldComboDataId = salesCustomFieldJsonArray.getJSONObject(i).optString("id", "");
                if (!StringUtil.isNullOrEmpty(fieldComboDataId)) {
                    fieldComboDataInInvoicesSET.add(fieldComboDataId);
                }
            }
        }

        Iterator it = fieldComboDataList.iterator();
        while (it.hasNext()) {
            JSONObject jobj = new JSONObject();
            String id = it.next().toString();
            if (!fieldComboDataInInvoicesSET.contains(id)) {
                KwlReturnObject fieldComboDataResult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), id);
                FieldComboData fieldComboData = (FieldComboData) fieldComboDataResult.getEntityList().get(0);
                getDimensionJSONForPM(jobj, fieldComboData, 0.0);
                newJArray.put(jobj);
            }
        }

        for (int i = 0; i < newJArray.length(); i++) {
            JSONObject jSONObject = newJArray.getJSONObject(i);
            salesCustomFieldJsonArray.put(jSONObject);
        }

        salesCustomFieldJsonArray = getHierarachicalJsonArray(salesCustomFieldJsonArray, costCategoryCustomFieldId);

        obj.put("salesrevenue", totalAmount);
        obj.put("salesCustomFieldData", salesCustomFieldJsonArray);

        return obj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getQuotations(JSONObject paramJobj) throws ServiceException, JSONException {

        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (!paramJobj.has(Constants.deleted) && !paramJobj.has("nondeleted")) {
            paramJobj.put(Constants.deleted, "false");
        } else if (paramJobj.has(Constants.deleted)) {
            paramJobj.put(Constants.deleted, paramJobj.get(Constants.deleted).toString());
        } else if (paramJobj.has("nondeleted")) {
            paramJobj.put("nondeleted", paramJobj.get("nondeleted").toString());
        }
        if (paramJobj.has("dateformat")) {
            paramJobj.put("userdateformat", paramJobj.getString("dateformat"));
        } else {
            paramJobj.put("userdateformat", authHandler.getDateOnlyFormatPattern());
        }
        JSONArray dataJArr = new JSONArray();
        JSONObject result = new JSONObject();
        KwlReturnObject kwlReturnObject = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
            requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(paramJobj));
            Iterator<String> keyIter = paramJobj.keys();
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                requestParams.put(key, paramJobj.get(key));
            }
            kwlReturnObject = accSalesOrderDAOobj.getQuotations(requestParams);
            dataJArr = accSalesOrderServiceDAOobj.getQuotationsJson(requestParams, kwlReturnObject.getEntityList(), dataJArr);
            result.put(Constants.RES_success, true);
            result.put(Constants.RES_data, dataJArr);
            result.put(Constants.RES_TOTALCOUNT, dataJArr.length());
            
        } catch (SessionExpiredException ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public JSONObject deleteQuotation(JSONObject paramJobj) throws JSONException, ServiceException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (!paramJobj.has("quotationids") || paramJobj.getJSONArray("quotationids").length() < 1) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        String msg = "";
        String deletedTransaction = "";
        String linkedQuotaions = "";
        String linkQuotationIds = "";

        String companyID = paramJobj.getString(Constants.companyKey);
        JSONArray quotationIds = paramJobj.getJSONArray("quotationids");

        for (int i = 0; i < quotationIds.length(); i++) {
            String crmqid = (String) quotationIds.get(i);
            String qid = "";
            if (!StringUtil.isNullOrEmpty(crmqid)) {
                HashMap<String, Object> requestParamsNew = new HashMap<>();
                requestParamsNew.put("crmquoatationid", crmqid);
                requestParamsNew.put(Constants.companyKey, companyID);
                requestParamsNew.put("archieve", 0);   // Ask about this to sagar sir.
                KwlReturnObject quoteResult = accSalesOrderDAOobj.getQuotations(requestParamsNew);
                List<String> qlist = quoteResult.getEntityList();
                if (qlist != null && qlist.size() > 0) {
                    for (String quoteid : qlist) {
                        KwlReturnObject quotationResult = accountingHandlerDAOobj.getObject(Quotation.class.getName(), quoteid);
                        Quotation quo = (Quotation) quotationResult.getEntityList().get(0);
                        String qno = "";
                        qno = quo.getQuotationNumber();
                        qid = quo.getID();
                        KwlReturnObject invResult = accSalesOrderDAOobj.getQTforinvoice(qid, companyID);  //for checking Customer Quotation used in invoice or not
                        int count1 = invResult.getRecordTotalCount();
                        if (count1 > 0) {
                            linkedQuotaions += qno + ", ";
                            linkQuotationIds += crmqid + ", ";
                            continue;
                        }
                        KwlReturnObject soResults = accSalesOrderDAOobj.getSOforQT(qid, companyID);  //for checking Customer Quotation used in sales order or not
                        int count2 = soResults.getRecordTotalCount();
                        if (count2 > 0) {
                            linkedQuotaions += qno + ", ";
                            linkQuotationIds += crmqid + ", ";
                            continue;
                        }
                        if (count1 == 0 && count2 == 0) {
                            HashMap<String, Object> requestParams = new HashMap<>();
                            requestParams.put("qid", qid);
                            requestParams.put(Constants.companyKey, companyID);
                            accSalesOrderDAOobj.deleteQuotationsPermanent(requestParams);
                            deletedTransaction += qno + ", ";
                        }
                    }
                }
            }
        }
        if (StringUtil.isNullOrEmpty(linkedQuotaions)) {
            msg = "Quotation(s) has been deleted successfully";
        } else {
            msg = "Quotation(s) except " + linkedQuotaions.substring(0, linkedQuotaions.length() - 2) + " has been deleted successfully.";
        }
        result.put(Constants.RES_success, true);
        result.put(Constants.RES_MESSAGE, msg);
        result.put("deletedTransaction", deletedTransaction);
        result.put("linkedQuotaions", linkedQuotaions);
        result.put("linkedQuotaionIds", linkQuotationIds);

        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCQLinkedInTransaction(JSONObject paramJobj) throws JSONException, ServiceException {
        boolean isSuccess = false;
        JSONObject result = new JSONObject();
        String msg = "";
        int count = 0;
        String companyID = paramJobj.getString(Constants.companyKey);
        String[] quotationIdArray = paramJobj.optString("quotationids").split(",");
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (!paramJobj.has("quotationids")) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (quotationIdArray.length > 0) {
                for (String crmqid : quotationIdArray) {
                    if (!StringUtil.isNullOrEmpty(crmqid)) {
                        HashMap<String, Object> requestParams = new HashMap<>();
                        requestParams.put("crmquoatationid", crmqid);
                        requestParams.put(Constants.companyKey, companyID);
                        requestParams.put("archieve", 0);
                        KwlReturnObject quoteResult = accSalesOrderDAOobj.getQuotations(requestParams);
                        List<String> qlist = quoteResult.getEntityList();
                        paramJobj.put(Constants.isConsignment, false);
                        paramJobj.put(Constants.isLeaseFixedAsset, false);
                        if (qlist != null && qlist.size() > 0) {
                            for (String quoteid : qlist) {
                                paramJobj.put(Constants.billid, quoteid);
                                JSONObject jobj = accSalesOrderServiceDAOobj.getCQLinkedInTransaction(paramJobj);
                                count += jobj.optInt(Constants.RES_count, 0);
                            }
                        }
                    }
                }
            }
            if (count > 0) {
                isSuccess = true;
//                msg = messageSource.getMessage("acc.common.resterp39", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                JSONObject response = wsUtilService.getErrorResponse("erp39", paramJobj, "Sorry, you cannot edit selected quotation as it is already used in transaction(s) of ERP module.");
                msg = response.optString(Constants.RES_MESSAGE);
            } else {
                isSuccess = false;
            }

        } catch (Exception ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            result.put(Constants.RES_success, isSuccess);
            result.put(Constants.RES_MESSAGE, msg);
        }
        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getVendorQuotations(JSONObject paramJobj) throws ServiceException, JSONException {

        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (!paramJobj.has(Constants.deleted) && !paramJobj.has("nondeleted")) {
            paramJobj.put(Constants.deleted, "false");
        } else if (paramJobj.has(Constants.deleted)) {
            paramJobj.put(Constants.deleted, paramJobj.get(Constants.deleted).toString());
        } else if (paramJobj.has("nondeleted")) {
            paramJobj.put("nondeleted", paramJobj.get("nondeleted").toString());
        }
        if (paramJobj.has("dateformat")) {
            paramJobj.put("userdateformat", paramJobj.getString("dateformat"));
        } else {
            paramJobj.put("userdateformat", authHandler.getDateOnlyFormatPattern());
        }
        JSONArray dataJArr = new JSONArray();
        JSONObject result = new JSONObject();
        KwlReturnObject kwlReturnObject = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
            requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(paramJobj));
            Iterator<String> keyIter = paramJobj.keys();
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                requestParams.put(key, paramJobj.get(key));
            }
            kwlReturnObject = accPurchaseOrderobj.getQuotations(requestParams);
            dataJArr = accPurchaseOrderServiceDAOobj.getQuotationsJson(paramJobj, kwlReturnObject.getEntityList(), dataJArr);
            result.put(Constants.RES_success, true);
            result.put(Constants.RES_data, dataJArr);
            result.put(Constants.RES_TOTALCOUNT, dataJArr.length());
            
        } catch (SessionExpiredException ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getInvoiceDetailfromCRMQuotation(JSONObject paramJobj) throws ServiceException, JSONException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (!paramJobj.has("quotationid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();

        JSONArray jArr = new JSONArray();
        String quotationId = (String) paramJobj.get("quotationid");

        KwlReturnObject invResult = accInvoiceDAOobj.getInvoiceDetailfromCrmQuotation(quotationId);
        List invoiceList = invResult.getEntityList();
        if (invoiceList.size() > 0) {
            for (int count = 0; count < invoiceList.size(); count++) {
                JSONObject obj = new JSONObject();
                Object[] invoicelistobj = (Object[]) invoiceList.get(count);
                String invoicenumber = (String) invoicelistobj[0];
                obj.put("InvoiceNumber", invoicenumber);
                String invoiceid = (String) invoicelistobj[1];
                KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                Invoice inv = (Invoice) invoiceResult.getEntityList().get(0);
                Double invoicetotalamount = inv.getCustomerEntry().getAmount();
                obj.put("InvoiceTotalAmount", invoicetotalamount);
                obj.put(Constants.RES_success, true);
                jArr.put(obj);
            }
        }
        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr.length());
        result.put(Constants.RES_success, true);
        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject postSalaryJE(JSONObject jobj) throws JSONException, ServiceException, AccountingException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        result.put(Constants.RES_success, true);

        boolean isComapnyExist = wsUtilService.isCompanyExists(jobj);

        if (isComapnyExist) {

            String companyID = jobj.getString(Constants.companyKey);
            KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
            Company company = (Company) result1.getEntityList().get(0);

            KwlReturnObject compAccPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences cap = (CompanyAccountPreferences) compAccPrefResult.getEntityList().get(0);

            KwlReturnObject cpfEEReturnObject = accAccountDAOobj.getAccountFromName(companyID, "CPF Employer Expense");
            List acclist = cpfEEReturnObject.getEntityList();
            Iterator itr = acclist.iterator();
            Account cpfEmployerExpenseAccount = null;
            while (itr.hasNext()) {
                cpfEmployerExpenseAccount = (Account) itr.next();
            }

            KwlReturnObject cpfPReturnObject = accAccountDAOobj.getAccountFromName(companyID, "CPF Payable");
            acclist = cpfPReturnObject.getEntityList();
            itr = acclist.iterator();
            Account cpfPayableAccount = null;
            while (itr.hasNext()) {
                cpfPayableAccount = (Account) itr.next();
            }

            int maxnumber = 0;
            SequenceFormat jeSeqFormat = null;
            String modulename = "autojournalentry";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";

            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, companyID);
            sfrequestParams.put(Constants.modulename, modulename);
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.isEmpty()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.modulename, modulename);
                requestParams.put("numberofdigit", 6);
                requestParams.put("showleadingzero", true);
                requestParams.put("prefix", Constants.JE_DEFAULT_PREFIX);
                requestParams.put("sufix", "");
                requestParams.put("startfrom", 0);
                requestParams.put("name", Constants.JE_DEFAULT_FORMAT);
                requestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                requestParams.put(Constants.companyKey, companyID);
                jeSeqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);

            } else if (ll.get(0) != null) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                String sequenceformatid = format.getID();
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_JOURNALENTRY, sequenceformatid, false, new Date());
                if (!seqNumberMap.isEmpty()) {
                    maxnumber = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part 
                } else {
                    maxnumber = 0;
                }
                jeSeqFormat = format;
                jeSeqFormat.setStartfrom(maxnumber);
            }

            if (cap.getLiabilityAccount() != null && cap.getExpenseAccount() != null && jobj.getString("currencyid") != null && jobj.getString("jarr") != null && cpfEmployerExpenseAccount != null && cpfPayableAccount != null) {

                KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobj.getString("currencyid"));
                KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);
                JSONArray jArr = jobj.getJSONArray("jarr");

                for (int i = 0; i < jArr.length(); i++) {

                    JSONObject salary = jArr.getJSONObject(i);
                    String month = (salary.has("month") && salary.getString("month") != null) ? salary.getString("month") : "";
                    String name = (salary.has("name") && salary.getString("name") != null) ? salary.getString("name") : "";
                    Double salaryPayable = Double.parseDouble(salary.getString("salaryPayable"));
                    Double salaryexpense = Double.parseDouble(salary.getString("salaryExpense"));
                    Double cpfEmployerExpense = Double.parseDouble(salary.getString("cpfEmployerExpense"));
                    Double cpfPayable = Double.parseDouble(salary.getString("cpfPayable"));

                    jeSeqFormat.setStartfrom(jeSeqFormat.getStartfrom() + 1);
                    int nextNumber = jeSeqFormat.getStartfrom();
                    int numberofdigit = jeSeqFormat.getNumberofdigit();
                    boolean showleadingzero = jeSeqFormat.isShowleadingzero();
                    String prefix = jeSeqFormat.getPrefix() != null ? jeSeqFormat.getPrefix() : "";
                    String suffix = jeSeqFormat.getSuffix() != null ? jeSeqFormat.getSuffix() : "";
                    String nextNumTemp = nextNumber + "";
                    if (showleadingzero) {
                        while (nextNumTemp.length() < numberofdigit) {
                            nextNumTemp = "0" + nextNumTemp;
                        }
                    }
                    String autoNumber = jeDatePrefix + prefix + jeDateAfterPrefix + nextNumTemp + suffix + jeDateSuffix;

                    String jeuuid = UUID.randomUUID().toString();
                    JournalEntry je = new JournalEntry();
                    je.setCompany(company);
                    je.setAutoGenerated(true);
                    je.setCurrency(currency);
                    je.setDeleted(false);
                    je.setEntryDate(new Date());
                    je.setMemo(name + " : Salary JE for the month of " + month);
                    je.setID(jeuuid);
                    je.setEntryNumber(autoNumber);
                    je.setSeqnumber(nextNumber);
                    je.setDatePreffixValue(jeDatePrefix);
                    je.setDateAfterPreffixValue(jeDateAfterPrefix);
                    je.setDateSuffixValue(jeDateSuffix);
                    je.setSeqformat(jeSeqFormat);
                    kwlCommonTablesDAOObj.saveObj(je);

                    String debitje = UUID.randomUUID().toString();
                    JournalEntryDetail jed = new JournalEntryDetail();
                    jed.setAccount(cap.getExpenseAccount());
                    jed.setAmount(salaryexpense);
                    jed.setDebit(true);
                    jed.setCompany(company);
                    jed.setDescription("Salary Expense from HRMS");
                    jed.setID(debitje);
                    jed.setSrno(1);
                    jed.setJournalEntry(je);
                    kwlCommonTablesDAOObj.saveObj(jed);

                    String cpfPayableJed = UUID.randomUUID().toString();
                    JournalEntryDetail jed2 = new JournalEntryDetail();
                    jed2.setAccount(cpfEmployerExpenseAccount);
                    jed2.setAmount(cpfEmployerExpense);
                    jed2.setDebit(true);
                    jed2.setCompany(company);
                    jed2.setDescription("CPF Employer Expense from HRMS");
                    jed2.setID(cpfPayableJed);
                    jed2.setSrno(2);
                    jed2.setJournalEntry(je);
                    kwlCommonTablesDAOObj.saveObj(jed2);

                    String creditje = UUID.randomUUID().toString();
                    JournalEntryDetail jed1 = new JournalEntryDetail();
                    jed1.setAccount(cap.getLiabilityAccount());
                    jed1.setAmount(salaryPayable);
                    jed1.setDebit(false);
                    jed1.setCompany(company);
                    jed1.setDescription("Salary Payable from HRMS");
                    jed1.setID(creditje);
                    jed1.setSrno(3);
                    jed1.setJournalEntry(je);
                    kwlCommonTablesDAOObj.saveObj(jed1);

                    String cpfEmployerExpenseJed = UUID.randomUUID().toString();
                    JournalEntryDetail jed3 = new JournalEntryDetail();
                    jed3.setAccount(cpfPayableAccount);
                    jed3.setAmount(cpfPayable);
                    jed3.setDebit(false);
                    jed3.setCompany(company);
                    jed3.setDescription("CPF Payable from HRMS");
                    jed3.setID(cpfEmployerExpenseJed);
                    jed3.setSrno(4);
                    jed3.setJournalEntry(je);
                    kwlCommonTablesDAOObj.saveObj(jed3);

                    Set<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                    details.add(jed);
                    details.add(jed1);
                    details.add(jed2);
                    details.add(jed3);
                    je.setDetails(details);
                    kwlCommonTablesDAOObj.saveObj(je);

                    //add account's amounts from newly added JE in jedetails_optimized table
                    saveAccountJEs_optimized(je.getID());

                    jArr.getJSONObject(i).put("jeid", je.getID());
                }
                result.put(Constants.RES_data, jArr);
                result.put(Constants.RES_TOTALCOUNT, jArr.length());
            }
        } else {
            result.put(Constants.RES_success, false);
        }
        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject postReverseSalaryJE(JSONObject jobj) throws JSONException, ServiceException, AccountingException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        result.put(Constants.RES_success, true);

        boolean isComapnyExist = wsUtilService.isCompanyExists(jobj);

        if (isComapnyExist) {
            
            String companyID = jobj.getString(Constants.companyKey);
            KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
            Company company = (Company) result1.getEntityList().get(0);
            KwlReturnObject compAccPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences cap = (CompanyAccountPreferences) compAccPrefResult.getEntityList().get(0);

            int maxnumber = 0;
            SequenceFormat jeSeqFormat = null;
            String modulename = "autojournalentry";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, companyID);
            sfrequestParams.put(Constants.modulename, modulename);
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();

            if (ll.isEmpty()) {

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.modulename, modulename);
                requestParams.put("numberofdigit", 6);
                requestParams.put("showleadingzero", true);
                requestParams.put("prefix", Constants.JE_DEFAULT_PREFIX);
                requestParams.put("sufix", "");
                requestParams.put("startfrom", 0);
                requestParams.put("name", Constants.JE_DEFAULT_FORMAT);
                requestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                requestParams.put(Constants.companyKey, companyID);
                jeSeqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);

            } else if (ll.get(0) != null) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                String sequenceformatid = format.getID();
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_JOURNALENTRY, sequenceformatid, false, new Date());
                if (!seqNumberMap.isEmpty()) {
                    maxnumber = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                } else {
                    maxnumber = 0;
                }
                jeSeqFormat = format;
                jeSeqFormat.setStartfrom(maxnumber);
            }

            if (cap.getLiabilityAccount() != null && cap.getExpenseAccount() != null && jobj.getString("currencyid") != null && jobj.getString("jarr") != null) {

                KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobj.getString("currencyid"));
                KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);

                JSONArray jArr = jobj.getJSONArray("jarr");
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject salary = jArr.getJSONObject(i);
                    Double salaryAmount = Double.parseDouble(salary.getString("salary"));
                    jeSeqFormat.setStartfrom(jeSeqFormat.getStartfrom() + 1);
                    int nextNumber = jeSeqFormat.getStartfrom();
                    int numberofdigit = jeSeqFormat.getNumberofdigit();
                    boolean showleadingzero = jeSeqFormat.isShowleadingzero();
                    String prefix = jeSeqFormat.getPrefix() != null ? jeSeqFormat.getPrefix() : "";
                    String suffix = jeSeqFormat.getSuffix() != null ? jeSeqFormat.getSuffix() : "";
                    String nextNumTemp = nextNumber + "";
                    if (showleadingzero) {
                        while (nextNumTemp.length() < numberofdigit) {
                            nextNumTemp = "0" + nextNumTemp;
                        }
                    }
                    String autoNumber = jeDatePrefix + prefix + jeDateAfterPrefix + nextNumTemp + suffix + jeDateSuffix;

                    KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), salary.getString("jeid"));
                    JournalEntry journalEntry = (JournalEntry) jeResult.getEntityList().get(0);

                    String jeuuid = UUID.randomUUID().toString();
                    JournalEntry je = new JournalEntry();
                    je.setCompany(company);
                    je.setAutoGenerated(true);
                    je.setCurrency(currency);
                    je.setDeleted(false);
                    je.setEntryDate(new Date());
                    je.setMemo("Reverse Entry for Salary JE from HRMS for JE no " + journalEntry.getEntryNumber());
                    je.setID(jeuuid);
                    je.setEntryNumber(autoNumber);
                    je.setSeqnumber(nextNumber);
                    je.setDatePreffixValue(jeDatePrefix);
                    je.setDateAfterPreffixValue(jeDateAfterPrefix);
                    je.setDateSuffixValue(jeDateSuffix);
                    je.setSeqformat(jeSeqFormat);
                    kwlCommonTablesDAOObj.saveObj(je);

                    String debitje = UUID.randomUUID().toString();
                    JournalEntryDetail jed = new JournalEntryDetail();
                    jed.setAccount(cap.getExpenseAccount());
                    jed.setAmount(salaryAmount);
                    jed.setDebit(false);
                    jed.setCompany(company);
                    jed.setDescription("Reverse Entry for Salary JE from HRMS");
                    jed.setID(debitje);
                    jed.setSrno(1);
                    jed.setJournalEntry(je);
                    kwlCommonTablesDAOObj.saveObj(jed);

                    String creditje = UUID.randomUUID().toString();
                    JournalEntryDetail jed1 = new JournalEntryDetail();
                    jed1.setAccount(cap.getLiabilityAccount());
                    jed1.setAmount(salaryAmount);
                    jed1.setDebit(true);
                    jed1.setCompany(company);
                    jed1.setDescription("Reverse Entry for Salary JE from HRMS");
                    jed1.setID(creditje);
                    jed1.setSrno(2);
                    jed1.setJournalEntry(je);
                    kwlCommonTablesDAOObj.saveObj(jed1);

                    Set<JournalEntryDetail> details = new HashSet<>();
                    details.add(jed);
                    details.add(jed1);
                    je.setDetails(details);
                    kwlCommonTablesDAOObj.saveObj(je);

                    //add account's amounts from newly added JE in jedetails_optimized table
                    saveAccountJEs_optimized(je.getID());
                }
            }
        } else {
            result.put(Constants.RES_success, false);
        }
        return result;
    }
   
    public boolean saveAccountJEs_optimized(String jeid) throws ServiceException {
        boolean successflag = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
        JournalEntry je = (JournalEntry) jeResult.getEntityList().get(0);

        String gcurrencyid = je.getCompany().getCurrency().getCurrencyID();
        String companyid = je.getCompany().getCompanyID();

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put(Constants.globalCurrencyKey, gcurrencyid);

        if (!je.isOptimizedflag() && !je.isDeleted() && je.getPendingapproval() == 0) {
            Set<JournalEntryDetail> jedetail = (Set<JournalEntryDetail>) je.getDetails();
            Iterator itr = jedetail.iterator();
            while (itr.hasNext()) {
                JournalEntryDetail jed = (JournalEntryDetail) itr.next();

                double amount = jed.isDebit() ? jed.getAmount() : -jed.getAmount();
                String accountid = jed.getAccount().getID();
                String entrydate = sdf.format(je.getEntryDate());
                String costCenterID = je.getCostcenter() != null ? je.getCostcenter().getID() : "";

                String fromcurrencyid = (je.getCurrency() == null ? gcurrencyid : je.getCurrency().getCurrencyID());
                KwlReturnObject crresult = getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                amount = (Double) crresult.getEntityList().get(0);
                if (amount != 0) {
                    accJournalEntryobj.saveAccountJEs_optimized(accountid, companyid, entrydate, costCenterID, amount);
                }
            }
            successflag = accJournalEntryobj.setJEs_optimizedflag(jeid);
        }
        return successflag;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCashRevenueTask(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject response = new JSONObject();
        if (!paramJobj.has(Constants.companyKey)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        boolean isCompanyExist = wsUtilService.isCompanyExists(paramJobj);
        if (isCompanyExist) {
            List<String>  list = Collections.EMPTY_LIST;
            JSONArray jarr = new JSONArray();
            String companyid = paramJobj.getString(Constants.companyKey);
            String projectid = paramJobj.getString("projectid");
            String currencyid = paramJobj.getString(Constants.currencyKey);
            String taskids = paramJobj.getString("taskid");
            String[] taskid = taskids.split(",");

            for (String task : taskid) {
                JSONObject obj = new JSONObject();
                KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) companyResult.getEntityList().get(0);
                String gcurrencyid = company.getCurrency().getCurrencyID();

                HashMap<String, Object> requestParams = new HashMap<>();
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put(Constants.globalCurrencyKey, gcurrencyid);

                String searchString = getSearchColumnsForTask(companyid, projectid, task);
                HashMap<String, Object> reqParams = new HashMap<>();
                reqParams.put(Constants.companyKey, companyid);
                reqParams.put("searchstring", searchString);
                KwlReturnObject invResult = accInvoiceDAOobj.getInvoicesWithSearchColumn(reqParams);
                list = invResult.getEntityList();
                
                Double totalAmount = 0.0;
                if (list.size() > 0) {
                    for (String invid : list) {
                        Double amount = 0.0;
                        KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                        Invoice invoice = (Invoice) invoiceResult.getEntityList().get(0);
                        if (invoice != null) {
                            JournalEntryDetail d = invoice.getCustomerEntry();
                            amount = d.getAmount();
//                            KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getJournalEntry().getEntryDate(), invoice.getExternalCurrencyRate());
                            KwlReturnObject bAmt = getCurrencyToBaseAmount(requestParams, amount, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getExternalCurrencyRate());
                            amount = (Double) bAmt.getEntityList().get(0);
                            amount = authHandler.round(amount, companyid);
                            totalAmount += amount;
                        }
                    }
                }
                KwlReturnObject bAmt = getBaseToCurrencyAmount(requestParams, totalAmount, currencyid, new Date(), 0);
                totalAmount = (Double) bAmt.getEntityList().get(0);
                totalAmount = authHandler.round(totalAmount, companyid);

                obj.put("projectid", projectid);
                obj.put("taskid", task);
                obj.put("salesrevenue", totalAmount);
                jarr.put(obj);
            }

            response.put("valid", true);
            response.put(Constants.RES_success, true);
            response.put(Constants.RES_data, jarr);
        } else {
            response.put(Constants.RES_success, false);
            response.put(Constants.RES_msg, "Company does not exist.");
        }
        return response;
    }
    
    public String getSearchColumnsForTask(String companyid, String projectid, String taskid) {
        String searchString = "";
        try {
            KwlReturnObject returnObject = accountingHandlerDAOobj.getFieldParamsForTask(companyid);
            List list = returnObject.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String columnno = (oj[0]).toString();
                String id = (oj[1]).toString();

                //Checked if provided isfortask is present for combo data
                KwlReturnObject cmbReturnObject = accountingHandlerDAOobj.getFieldComboDataForTask(id, projectid, taskid);
                List listCmbData = cmbReturnObject.getEntityList();
                Iterator itrCmbData = listCmbData.iterator();
                //If Task Id is present that searchfield is added
                if (itrCmbData.hasNext()) {
                    String comboid = (String) itrCmbData.next();
                    searchString += "col" + columnno + " = '" + comboid + "' or ";
                }
            }
            if (searchString.length() > 0) {
                searchString = searchString.substring(0, searchString.lastIndexOf("or"));
                searchString = "and (" + searchString + ")";
            }

        } catch (ServiceException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return searchString;
    }
    
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject postAmountJE(JSONObject jobj) throws JSONException, ServiceException, AccountingException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();

        boolean isComapnyExist = wsUtilService.isCompanyExists(jobj);

        if (isComapnyExist) {
            try {
                String journalEntryId="";
                String companyID = jobj.getString("companyid");
                KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
                Company company = (Company) companyResult.getEntityList().get(0);
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date newdate = new Date();
                String userdiff = company.getCreator() == null ? "" : (company.getCreator().getTimeZone() != null ? company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference());
                sdf1.setTimeZone(TimeZone.getTimeZone("GMT" + userdiff));
                Date newcreatedate = authHandler.getDateWithTimeFormat().parse(sdf1.format(newdate));
                SequenceFormat jeSeqFormat = null;
                String modulename = "autojournalentry";
                String jeDatePrefix = "";
                String jeDateAfterPrefix = "";
                String jeDateSuffix = "";
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                String sequenceformatid = "";

                Map<String, Object> sfrequestParams = new HashMap<>();
                sfrequestParams.put("companyid", companyID);
                sfrequestParams.put("modulename", modulename);
                KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
                List<SequenceFormat> ll = seqFormatResult.getEntityList();
                if (ll.isEmpty()) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("modulename", modulename);
                    requestParams.put("numberofdigit", 6);
                    requestParams.put("showleadingzero", true);
                    requestParams.put("prefix", Constants.JE_DEFAULT_PREFIX);
                    requestParams.put("sufix", "");
                    requestParams.put("startfrom", 0);
                    requestParams.put("name", Constants.JE_DEFAULT_FORMAT);
                    requestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    requestParams.put("companyid", companyID);
                    jeSeqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);

                } else if (ll.get(0) != null) {
                    SequenceFormat format = (SequenceFormat) ll.get(0);
                    sequenceformatid = format.getID();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_JOURNALENTRY, sequenceformatid, false, newcreatedate);
                    if (!seqNumberMap.isEmpty()) {

                        jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    }
                }

                if (jobj.getString("currencyid") != null && jobj.getString("data") != null) {
                    KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobj.getString("currencyid"));
                    KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);
                    JSONArray jArr = jobj.getJSONArray("data");
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject GLObject = jArr.getJSONObject(i);

                        if (!StringUtil.isNullOrEmpty(GLObject.getString("glDebitAccountId")) && !StringUtil.isNullOrEmpty(GLObject.getString("glCreditAccountId"))) {
                            String glDebitAccountId = GLObject.getString("glDebitAccountId");
                            String glCreditAccountId = GLObject.getString("glCreditAccountId");
                            Double glAmount = (GLObject.has("glAmount") && GLObject.getString("glAmount") != null) ? Double.parseDouble(GLObject.getString("glAmount")) : 0;
                            Double TaxAmount = (GLObject.has("TaxAmount") && GLObject.getString("TaxAmount") != null) ? Double.parseDouble(GLObject.getString("TaxAmount")) : 0;

                            KwlReturnObject glDrAccResult = accountingHandlerDAOobj.getObject(Account.class.getName(), glDebitAccountId);
                            Account glDebitAccount = (Account) glDrAccResult.getEntityList().get(0);

                            KwlReturnObject glCrAccResult = accountingHandlerDAOobj.getObject(Account.class.getName(), glCreditAccountId);
                            Account glCreditAccount = (Account) glCrAccResult.getEntityList().get(0);

                            String JEMemo = GLObject.has("JEMemo") ? GLObject.getString("JEMemo") : glDebitAccount.getName() + " Dr to " + glCreditAccount.getName() + " Cr for Category: \"" + GLObject.getString("categoryName") + "\" for Project: \"" + GLObject.getString("projectName") + "\"";
                            String JEDescription = GLObject.has("JEDescription") ? GLObject.getString("JEDescription") : "Amount from Project Management";
                            String taxID = GLObject.has("taxid") ? GLObject.getString("taxid") : "";
                            boolean isFromEclaim = GLObject.has("isFromEclaim") ? GLObject.getBoolean("isFromEclaim") : false;

                            /*
                             * If Tax is not present in ERP side then we simply
                             * retrun with info code
                             */
                            Tax tax = null;
                            if (!StringUtil.isNullOrEmpty(taxID)) {
                                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxID);
                                tax = (Tax) result1.getEntityList().get(0);
                                if (StringUtil.isNullObject(tax)) {
                                    result.put(Constants.RES_success, false);
                                    break;
                                }

                            }

                            String jeid = (GLObject.has("jeId")) ? GLObject.getString("jeId") : "";
                            if (StringUtil.isNullOrEmpty(jeid)) {  //New JE

                                String nextNumber = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                String autoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number

                                Map<String, Object> jeDataMap = new HashMap<>();
                                jeDataMap.put("companyid", company.getCompanyID());
                                jeDataMap.put("autogenerated", true);
                                jeDataMap.put("currencyid", currency.getCurrencyID());
                                jeDataMap.put("entrydate", newcreatedate);
                                jeDataMap.put("memo", JEMemo);
                                jeDataMap.put("isFromEclaim", isFromEclaim);
                                jeDataMap.put("entrynumber", autoNumber);
                                jeDataMap.put(Constants.SEQNUMBER, nextNumber);
                                jeDataMap.put(Constants.SEQFORMAT, sequenceformatid);
                                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);

                                JournalEntry journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                                accJournalEntryobj.saveJournalEntryByObject(journalEntry);
                                journalEntryId=journalEntry.getID();

                                JSONObject jedjson = new JSONObject();
                                jedjson.put("accountid", glDebitAccount.getID());
                                jedjson.put("amount", glAmount);
                                jedjson.put("debit", true);
                                jedjson.put("companyid", company.getCompanyID());
                                jedjson.put("description", JEDescription);
                                jedjson.put("srno", 1);
                                jedjson.put("jeid", journalEntry.getID());
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                Set<JournalEntryDetail> detail = new HashSet();
                                detail.add(jed);

                                jedjson = new JSONObject();
                                jedjson.put("accountid", glCreditAccount.getID());
                                jedjson.put("amount", glAmount + TaxAmount);//total Credit Amount
                                jedjson.put("debit", false);
                                jedjson.put("companyid", company.getCompanyID());
                                jedjson.put("description", JEDescription);
                                jedjson.put("srno", TaxAmount != 0 ? 3 : 2);
                                jedjson.put("jeid", journalEntry.getID());
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                detail.add(jed);

                                /*
                                 * Save JeDetail for tax applied in eclaim
                                 */
                                if (!StringUtil.isNullOrEmpty(taxID)) {
                                    jedjson = new JSONObject();
                                    jedjson.put("accountid", tax.getAccount().getID());
                                    jedjson.put("amount", TaxAmount);
                                    jedjson.put("debit", true);
                                    jedjson.put("companyid", company.getCompanyID());
                                    jedjson.put("description", JEDescription);
                                    jedjson.put("srno", 2);
                                    jedjson.put("jeid", journalEntry.getID());
                                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    detail.add(jed);
                                }
                                journalEntry.setDetails(detail);
                                accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                                //add account's amounts from newly added JE in jedetails_optimized table
                                saveAccountJEs_optimized(journalEntry.getID());

                                jArr.getJSONObject(i).put("jeId", journalEntry.getID());
                                    
                                /* Adding custom field value on Line level coming from Eclaim*/
                                String customfield = GLObject.optString("customfieldmap", null);
                                for (Iterator<JournalEntryDetail> jEDIterator = detail.iterator(); jEDIterator.hasNext();) {
                                    JournalEntryDetail jedetail = jEDIterator.next();

                                    if (!StringUtil.isNullOrEmpty(customfield)) {
                                        JSONArray jcustomarray = new JSONArray(customfield);
                                        if (jcustomarray.length() > 0) {
                                            jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_GENERAL_LEDGER_ModuleId, companyID,false);
                                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                            customrequestParams.put("customarray", jcustomarray);
                                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                                            customrequestParams.put("modulerecid", jedetail.getID());
                                            customrequestParams.put("recdetailId", jedetail.getID());
                                            customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                            customrequestParams.put("companyid", companyID);
                                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {

                                                JSONObject jeDetailJson = new JSONObject();
                                                jeDetailJson.put("accjedetailcustomdata", jedetail.getID());
                                                jeDetailJson.put("jedid", jed.getID());
                                                jedresult = accJournalEntryobj.updateJournalEntryDetails(jeDetailJson);
                                            }
                                        }

                                    }

                                }

                                /* Adding custom field value on Global level coming from Eclaim*/
                                if (!StringUtil.isNullOrEmpty(customfield)) {
                                    JSONArray jcustomarray = new JSONArray(customfield);
                                    if (jcustomarray.length() > 0) {
                                        if (jcustomarray.length() > 0) {
                                            jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_GENERAL_LEDGER_ModuleId, companyID,true);
                                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                            customrequestParams.put("customarray", jcustomarray);
                                            customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                                            customrequestParams.put("modulerecid", journalEntryId);
                                            customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                            customrequestParams.put("companyid", companyID);
                                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                                Map<String, Object> customjeDataMap = new HashMap<String, Object>();
                                                customjeDataMap.put("accjecustomdataref", journalEntryId);
                                                customjeDataMap.put("jeid", journalEntryId);
                                                customjeDataMap.put("istemplate", 1);
                                                jedresult = accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                                            }
                                        }
                                    }
                                }
                            } else {   //update JE
                                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
                                JournalEntry journalEntry = (JournalEntry) jeResult.getEntityList().get(0);
                                if (journalEntry != null) {
                                    //add account's amounts from newly added JE in jedetails_optimized table
                                    accJournalEntryobj.deleteOnEditAccountJEs_optimized(jeid);

                                    Set<JournalEntryDetail> journalEntryDetails = journalEntry.getDetails();
                                    for (JournalEntryDetail journalEntryDetail : journalEntryDetails) {
                                        if (journalEntryDetail.isDebit()) {
                                            JSONObject jedjson = new JSONObject();
                                            jedjson.put("jedid", journalEntryDetail.getID());
                                            jedjson.put("accountid", glDebitAccount.getID());
                                            jedjson.put("amount", glAmount);
                                            jedjson.put("jeid", journalEntry.getID());
                                            jedjson.put("description", JEDescription);
                                            jedjson.put("debit", true);
                                            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                        } else {
                                            JSONObject jedjson = new JSONObject();
                                            jedjson.put("jedid", journalEntryDetail.getID());
                                            jedjson.put("accountid", glCreditAccount.getID());
                                            jedjson.put("amount", glAmount);
                                            jedjson.put("jeid", journalEntry.getID());
                                            jedjson.put("description", JEDescription);
                                            jedjson.put("debit", false);
                                            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                        }
                                    }

                                    JSONObject jeDataMap = new JSONObject();
                                    jeDataMap.put("jeid", journalEntry.getID());
                                    jeDataMap.put("entrydate", newcreatedate);
                                    jeDataMap.put("memo", JEMemo);
                                    KwlReturnObject journalEntryResult = accJournalEntryobj.updateJournalEntry(jeDataMap, new HashSet<JournalEntryDetail>());
                                    journalEntry = (JournalEntry) journalEntryResult.getEntityList().get(0);

                                    jArr.getJSONObject(i).put("successTRflag", true);
                                    jArr.getJSONObject(i).put("jeId", journalEntry.getID());

                                    //add account's amounts from newly added JE in jedetails_optimized table
                                    saveAccountJEs_optimized(journalEntry.getID());
                                }
                            }
                        } else {
                            jArr.getJSONObject(i).put("successTRflag", false);
                        }
                    }
                    result.put(Constants.RES_data, jArr);
                    result.put(Constants.RES_success, true);
                }
            } catch (SessionExpiredException | ParseException e) {
                Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, e);
                throw ServiceException.FAILURE(e.getMessage(), e);
            }
        } else {
            result.put(Constants.RES_success, false);
        }
        return result;
    }
    
    @Override
    public JSONObject saveIncidentCase(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject jobj = new JSONObject();

        if (paramJobj != null && !paramJobj.has(Constants.productid) && !paramJobj.has(Constants.companyKey) && !paramJobj.has(Constants.customerid)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }

        jobj = AccProductService.saveIncidentCase(paramJobj);
        if (jobj.has(Constants.RES_msg)) {
            jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
            jobj.remove(Constants.RES_msg);
        }
        if (jobj.has(Constants.RES_success)) {
            jobj.put(Constants.RES_success, (Boolean) jobj.get(Constants.RES_success));
        } else {
            jobj.put(Constants.RES_success, false);
        }
        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteIncidentCase(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (!paramJobj.has(Constants.RES_data)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } else {
            JSONArray invoiceArray = new JSONArray(paramJobj.getString(Constants.RES_data));
            if (invoiceArray.length() < 1) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            paramJobj.put(Constants.RES_data, invoiceArray);
        }
        JSONObject jobj = new JSONObject();
        jobj = AccProductService.deleteIncident(paramJobj);
        if (jobj.has(Constants.RES_msg)) {
            jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
            jobj.remove(Constants.RES_msg);
        }
        if (jobj.has(Constants.RES_success)) {
            jobj.put(Constants.RES_success, jobj.getBoolean(Constants.RES_success));
        } else {
            jobj.put(Constants.RES_success, false);
        }

        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getIncidentCase(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException{
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);

        JSONObject jobj = new JSONObject();
        jobj.put(Constants.RES_success,false);
        try {
            jobj = AccProductService.getIncidentCase(paramJobj);
            jobj.put(Constants.RES_success,true);
        } catch (ParseException ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("System fialure", "e01", false);
        }
        
        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getIncidentChart(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException{
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);

        JSONObject jobj = new JSONObject();
        jobj.put(Constants.RES_success,false);
        try {
            jobj = AccProductService.getIncidentChart(paramJobj);
            jobj.put(Constants.RES_success,true);
        } catch (ParseException ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("System fialure", "e01", false);
        }
        
        return jobj;
    }
    
    private JSONObject jsonCreateMpAndRPForPOS(JSONObject paramJobj, int moduleid) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        JSONObject mpRPJson = paramJobj;

        if (!mpRPJson.has("details")) {
            throw ServiceException.FAILURE("Missing details", "", false);
        }
        
        //if transaction currency is different than company currency then ismulticurrencypaymentje is false
        if (paramJobj.has(Constants.globalCurrencyKey) && !paramJobj.optString(Constants.globalCurrencyKey).equalsIgnoreCase(paramJobj.optString(Constants.currencyKey))) {
            mpRPJson.put("ismulticurrencypaymentje", true);
        } else {
            mpRPJson.put("ismulticurrencypaymentje", false);
        }
        
        //if falg is not present
        if(!mpRPJson.has("iscustomer")){
            //for mp against customer
            if (mpRPJson.has("customervalue") && !StringUtil.isNullOrEmpty(mpRPJson.optString("customervalue", null))) {
                mpRPJson.put("iscustomer", true);
            } else {
                mpRPJson.put("iscustomer", false);
            }
        }
        
        mpRPJson = wsUtilService.replaceBooleanwithStringValues(mpRPJson);
        mpRPJson.put(Constants.moduleid,moduleid);
        mpRPJson = wsUtilService.manipulateGlobalLevelFieldsNew(mpRPJson, paramJobj.optString(Constants.companyKey));

        boolean isEdit = StringUtil.isNullOrEmpty(mpRPJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(mpRPJson.getString(Constants.isEdit));
        String transactionNo = "";
        String companyid = paramJobj.optString(Constants.companyKey);
        if (!StringUtil.isNullOrEmpty(mpRPJson.optString("paymentno", null))) {
            transactionNo = mpRPJson.optString("paymentno");
            mpRPJson.put("no", transactionNo);
            mpRPJson.put(Constants.sequenceformat, "NA");
        } else if (!StringUtil.isNullOrEmpty(mpRPJson.optString("receiptno", null))) {
            transactionNo = mpRPJson.optString("receiptno");
            mpRPJson.put(Constants.sequenceformat, "NA");
            mpRPJson.put("no", transactionNo);
        }

        if (moduleid == Constants.Acc_Make_Payment_ModuleId && isEdit) {
            KwlReturnObject mpResult = accPaymentDAOobj.getPaymentFromBillNo(transactionNo, companyid);
            Payment mpObj = (Payment) mpResult.getEntityList().get(0);
            if (mpObj != null) {
                mpRPJson.put("no", mpObj.getPaymentNumber());
                mpRPJson.put("billid", mpObj.getID());
                if (mpObj.getSeqformat() != null) {
                    mpRPJson.put(Constants.sequenceformat, mpObj.getSeqformat().getID());
                } else {
                    mpRPJson.put(Constants.sequenceformat, "NA");
                }
                mpRPJson.put(Constants.isEdit, isEdit);
            }else{ 
              throw ServiceException.FAILURE("Invalid payment number for edit as it is not present in system.", "", false);
            } 

        } else if (moduleid == Constants.Acc_Receive_Payment_ModuleId && isEdit && !paramJobj.optBoolean(Constants.isForPos)) {
            KwlReturnObject rpResult = accReceiptDAOobj.getReceiptFromBillNo(transactionNo, companyid);
            Receipt rpObj = (Receipt) rpResult.getEntityList().get(0);
            if (rpObj != null) {
                mpRPJson.put("no", rpObj.getReceiptNumber());
                mpRPJson.put("billid", rpObj.getID());
            }else{ 
              throw ServiceException.FAILURE("Invalid receipt number for edit as it is not present in system.", "", false);
            } 
        }

        if (!isEdit && StringUtil.isNullOrEmpty(transactionNo) && StringUtil.isNullOrEmpty(mpRPJson.optString(Constants.sequenceformat))) {//if it is not edit mode and transactionno is not given then it will go for checking default sequenceformat for payment or receipt
            mpRPJson.remove(Constants.sequenceformat);
            mpRPJson = wsUtilService.getSequenceFormatId(paramJobj, String.valueOf(moduleid));
        }

        //replace values for creditnote for invoicedetails and accountdetails
        mpRPJson = wsUtilService.manipulatePaymentOrReceiptDetails(paramJobj, moduleid);

        if (!mpRPJson.has("amount") || mpRPJson.optDouble("amount", 0.0) == 0.0) {
            throw ServiceException.FAILURE("Total amount should be greater than zero.", "", false);
        }
        return mpRPJson;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject savePayment(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException,ParseException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject response = new JSONObject();
        boolean isForPOS=false;

        if (paramJobj.has("paymentno")) {
            String creationdate = null;
            paramJobj.put(Constants.isdefaultHeaderMap, true);
            isForPOS=true;
            if (paramJobj.has("billdate") && !StringUtil.isNullOrEmpty(paramJobj.optString("billdate"))) {
                creationdate = paramJobj.optString("billdate") == null ? null : paramJobj.optString("billdate");
                paramJobj.put("creationdate",creationdate);
            }

            if (!StringUtil.isNullOrEmpty(creationdate)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
                Date transdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).parse(creationdate);
                DateFormat df = authHandler.getDateOnlyFormat();
                creationdate = df.format(transdate);
                paramJobj.put("creationdate", creationdate);
                paramJobj.remove("billdate");
            }

            JSONObject modifiedJson = jsonCreateMpAndRPForPOS(paramJobj, Constants.Acc_Make_Payment_ModuleId);
            paramJobj = modifiedJson;
        }
        
        if (!paramJobj.has(Constants.useridKey) || !paramJobj.has(Constants.companyKey) || !paramJobj.has("Details") || !paramJobj.has("amount")
                || !paramJobj.has("creationdate") || !paramJobj.has("externalcurrencyrate") || !paramJobj.has("ismulticurrencypaymentje") || !paramJobj.has("pmtmethod")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } else {
            JSONArray salesODetailArray = new JSONArray(paramJobj.getString("Details"));
            if (salesODetailArray.length() < 1) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
}
        }

        if (!paramJobj.has(Constants.sequenceformat) && !isForPOS) {
            String sequenceformatid = null;
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, paramJobj.get(Constants.companyKey));
            sfrequestParams.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
            sfrequestParams.put("isdefaultFormat", true);
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.isEmpty()) {
                throw ServiceException.FAILURE("Missing required field", "erp30", false);
            } else if (ll.get(0) != null) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                paramJobj.put(Constants.sequenceformat, sequenceformatid);
                HashMap<String, Object> hashMap = accVendorPaymentModuleServiceObj.saveVendorPayment(paramJobj);
                response = (JSONObject) hashMap.get("jobj");
                if (response.has(Constants.RES_msg)) {
                    response.put(Constants.RES_MESSAGE, response.getString(Constants.RES_msg));
                    response.remove(Constants.RES_msg);
                }
                if (response.has(Constants.RES_success)) {
                    response.put(Constants.RES_success, (Boolean) response.get(Constants.RES_success));
                } else {
                    response.put(Constants.RES_success, false);
                }
            }
        } else if(isForPOS){
            HashMap<String, Object> hashMap = accVendorPaymentModuleServiceObj.saveVendorPayment(paramJobj);
            response = (JSONObject) hashMap.get("jobj");
            if (response.has(Constants.RES_msg)) {
                response.put(Constants.RES_MESSAGE, response.getString(Constants.RES_msg));
                response.remove(Constants.RES_msg);
            }
            if (response.has(Constants.RES_success)) {
                response.put(Constants.RES_success, (Boolean) response.get(Constants.RES_success));
            } else {
                response.put(Constants.RES_success, false);
            }
        }

        return response;
    }
    
      
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getSalesReturnSummaryReport(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (!paramJobj.has(Constants.globalCurrencyKey)||!paramJobj.has(Constants.moduleid)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        //ERP-41214:Show asterisk to unit price and amount
        paramJobj = wsUtilService.getUserPermissionsforUnitPriceAndAmount(paramJobj);
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = accSalesReturnService.getSalesReturnSummaryReport(paramJobj);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
 
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getSalesByCustomer(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMapJson(paramJobj);
            String companyid = paramJobj.getString(Constants.companyKey);
            String userid = paramJobj.getString(Constants.useridKey);
            String salesPersonid = paramJobj.optString("salesPersonid", null);
            boolean isSalesByProductReport = StringUtil.isNullOrEmpty(paramJobj.optString("isSalesByProductReport", null)) ? false : Boolean.parseBoolean(paramJobj.optString("isSalesByProductReport"));
            boolean isSalesBysalesPerosnReport = StringUtil.isNullOrEmpty(paramJobj.optString("isSalesBysalesPerosnReport", null)) ? false : Boolean.parseBoolean(paramJobj.optString("isSalesBysalesPerosnReport"));
            String start = paramJobj.optString("start", "0");
            String limit = paramJobj.optString("limit", "15");
            requestParams.put("start", start);
            requestParams.put("limit", limit);
            requestParams.put("isSalesByProductReport", isSalesByProductReport);
            requestParams.put("isSalesBysalesPerosnReport", isSalesBysalesPerosnReport);
            requestParams.put("userid", userid);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
               /*when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson.*/
                String userId = userid;
                requestParams.put(Constants.useridKey, userId);
                requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                Map<String, Object> salesPersonParams = new HashMap<>();
                if (!StringUtil.isNullOrEmpty(salesPersonid)) {
                    requestParams.put("salesPersonid", salesPersonid);
                } else {
                    salesPersonParams.put(Constants.useridKey, userid);
                    salesPersonParams.put(Constants.companyKey, companyid);
                    salesPersonParams.put("grID", "15");
                    KwlReturnObject masterItemByUserList = accountingHandlerDAOobj.getMasterItemByUserID(salesPersonParams);
                    List<MasterItem> masterItems = masterItemByUserList.getEntityList();
                    String salesPersons = "";
                    StringBuffer salesPersonids = new StringBuffer();
                    for (Object obj : masterItems) {
                        if (obj != null) {
                            salesPersonids.append(obj.toString() + ",");
                        }
                    }
                    if (salesPersonids.length() > 0) {
                        salesPersons = salesPersonids.substring(0, (salesPersonids.length() - 1));
                        requestParams.put("salesPersonid", salesPersons);
                    }
                }
            }
            KwlReturnObject result = accInvoiceDAOobj.getSalesByCustomer(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONObject jObj = accInvoiceServiceDAO.getSalesByCustomerJson(paramJobj, list, false);
            JSONArray DataJArr = jObj.getJSONArray("data");
            jobj.put("data", DataJArr);
            jobj.put("count", count);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    private JSONObject createJsonReceivePaymentForReport(JSONObject paramJobj) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        JSONObject rpJson = paramJobj;
        rpJson.put(Constants.isdefaultHeaderMap, true);
        String companyid = rpJson.getString(Constants.companyKey);
        rpJson.put(Constants.companyids, companyid);
        rpJson.put("ispendingAproval", rpJson.optString("pendingapprovalflag", "false"));
        rpJson.put("isPaymentReport", rpJson.optString("paymentreportflag", "true"));
        rpJson.put("consolidateFlag", rpJson.optString("consolidateflag", "false"));
        rpJson.put("deleted", rpJson.optString("deletedflag", "false"));
        rpJson.put("nondeleted", rpJson.optString("nondeletedflag", "false"));
        rpJson.put("ss", rpJson.optString("searchvalue", ""));
        rpJson.put("includeExcludeChildCmb", paramJobj.optString("includeexcludechildcmbvalue", "All"));
        rpJson.put("custVendorID", "All");

        if (rpJson.has("paymentreceivedfromcustomerflag") && rpJson.optBoolean("paymentreceivedfromcustomerflag", false)) {
            rpJson.put("paymentWindowType", "1");
        }

        if (rpJson.has("paymentreceivedfromvendorflag") && rpJson.optBoolean("paymentreceivedfromvendorflag", false)) {
            rpJson.put("paymentWindowType", "2");
        }

        if (rpJson.has("paymentagainstglflag") && rpJson.optBoolean("paymentagainstglflag", false)) {
            rpJson.put("paymentWindowType", "3");
        }
        
        if (rpJson.has("openingbalancesflag") && rpJson.optBoolean("openingbalancesflag", false)) {
            rpJson.put("onlyOpeningBalanceTransactionsFlag", rpJson.optString("openingbalancesflag", ""));
            rpJson.put("includeExcludeChildCmb", "false");
        }
        
        if (rpJson.has("postdatedchequeflag") && rpJson.optBoolean("postdatedchequeflag", false)) {
            rpJson.put("isPostDatedCheque", rpJson.optString("postdatedchequeflag", ""));
        }
         
          if (rpJson.has("dishonouredchequeflag") && rpJson.optBoolean("dishonouredchequeflag", false)) {
            rpJson.put("isDishonouredCheque", rpJson.optString("dishonouredchequeflag", ""));
        }
          
           if (rpJson.has("advancepaymentflag") && rpJson.optBoolean("advancepaymentflag", false)) {
            rpJson.put("allAdvPayment", rpJson.optString("advancepaymentflag", ""));
        }
           
            if (rpJson.has("unutilisedadvpaymentflag") && rpJson.optBoolean("unutilisedadvpaymentflag", false)) {
            rpJson.put("unUtilizedAdvPayment", rpJson.optString("unutilisedadvpaymentflag", ""));
        }
        
        if (rpJson.has("partiallyutilisedadvpaymentflag") && rpJson.optBoolean("partiallyutilisedadvpaymentflag", false)) {
            rpJson.put("partiallyUtilizedAdvPayment", rpJson.optString("partiallyutilisedadvpaymentflag", ""));
        }

        if (rpJson.has("fullyutilisedadvpaymentflag") && rpJson.optBoolean("fullyutilisedadvpaymentflag", false)) {
            rpJson.put("fullyUtilizedAdvPayment", rpJson.optString("fullyutilisedadvpaymentflag", ""));
        }
        if (rpJson.has("nonorpartiallyutilisedadvpaymentflag") && rpJson.optBoolean("nonorpartiallyutilisedadvpaymentflag", false)) {
            rpJson.put("nonorpartiallyUtilizedAdvPayment", rpJson.optString("nonorpartiallyutilisedadvpaymentflag", ""));
        }   

        if (rpJson.has("customervalue") && !StringUtil.isNullOrEmpty(rpJson.optString("customervalue",null))) {//only for customer value filter only
            StringBuilder custVendorString = new StringBuilder();
            String custIdArray[] = paramJobj.optString("customervalue").split(",");
            if (custIdArray.length > 0) {
                for (String custVendorValue : custIdArray) {
                    rpJson.put("customervalue", custVendorValue);
                    rpJson = wsUtilService.manipulateGlobalLevelFieldsNew(rpJson, companyid);
                    custVendorString.append(rpJson.optString(Constants.customerName) + ",");
                    rpJson.remove("customervalue");
                    rpJson.remove(Constants.customerName);
                }
                rpJson.put("custVendorID", custVendorString.toString().substring(0, custVendorString.length() - 1));
                rpJson.put("includeExcludeChildCmb", "false");
            }
        }
        return rpJson;
    }
    
    private JSONObject createJsonPaymentMadeForReport(JSONObject paramJobj) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        JSONObject rpJson = paramJobj;
//        rpJson.put(Constants.isdefaultHeaderMap, true);
        String companyid = rpJson.getString(Constants.companyKey);
        rpJson.put(Constants.companyids, companyid);
        rpJson.put("ispendingAproval", rpJson.optString("pendingapprovalflag", ""));
        rpJson.put("isPaymentReport", rpJson.optString("paymentreportflag", "true"));
        rpJson.put("consolidateFlag", rpJson.optString("consolidateflag", "false"));
        rpJson.put("deleted", rpJson.optString("deletedflag", "false"));
        rpJson.put("nondeleted", rpJson.optString("nondeletedflag", "false"));
        rpJson.put("ss", rpJson.optString("searchvalue", ""));
        rpJson.put("includeExcludeChildCmb", paramJobj.optString("includeexcludechildcmbvalue", "All"));
        rpJson.put("custVendorID", "All");

        if (rpJson.has("paymenttovendorflag") && rpJson.optBoolean("paymenttovendorflag", false)) {
            rpJson.put("paymentWindowType", "1");
        }

        if (rpJson.has("paymenttocustomerflag") && rpJson.optBoolean("paymenttocustomerflag", false)) {
            rpJson.put("paymentWindowType", "2");
        }

        if (rpJson.has("paymentagainstglflag") && rpJson.optBoolean("paymentagainstglflag", false)) {
            rpJson.put("paymentWindowType", "3");
        }

        if (rpJson.has("openingbalancesflag") && rpJson.optBoolean("openingbalancesflag", false)) {
            rpJson.put("onlyOpeningBalanceTransactionsFlag", rpJson.optString("openingbalancesflag", ""));
            rpJson.put("includeExcludeChildCmb", "false");
        }

        if (rpJson.has("postdatedchequeflag") && rpJson.optBoolean("postdatedchequeflag", false)) {
            rpJson.put("isPostDatedCheque", rpJson.optString("postdatedchequeflag", ""));
        }

        if (rpJson.has("dishonouredchequeflag") && rpJson.optBoolean("dishonouredchequeflag", false)) {
            rpJson.put("isDishonouredCheque", rpJson.optString("dishonouredchequeflag", ""));
        }

        if (rpJson.has("advancepaymentflag") && rpJson.optBoolean("advancepaymentflag", false)) {
            rpJson.put("allAdvPayment", rpJson.optString("advancepaymentflag", ""));
        }

        if (rpJson.has("unutilisedadvpaymentflag") && rpJson.optBoolean("unutilisedadvpaymentflag", false)) {
            rpJson.put("unUtilizedAdvPayment", rpJson.optString("unutilisedadvpaymentflag", ""));
        }

        if (rpJson.has("partiallyutilisedadvpaymentflag") && rpJson.optBoolean("partiallyutilisedadvpaymentflag", false)) {
            rpJson.put("partiallyUtilizedAdvPayment", rpJson.optString("partiallyutilisedadvpaymentflag", ""));
        }

        if (rpJson.has("fullyutilisedadvpaymentflag") && rpJson.optBoolean("fullyutilisedadvpaymentflag", false)) {
            rpJson.put("fullyUtilizedAdvPayment", rpJson.optString("fullyutilisedadvpaymentflag", ""));
        }
        if (rpJson.has("nonorpartiallyutilisedadvpaymentflag") && rpJson.optBoolean("nonorpartiallyutilisedadvpaymentflag", false)) {
            rpJson.put("nonorpartiallyUtilizedAdvPayment", rpJson.optString("nonorpartiallyutilisedadvpaymentflag", ""));
        }

        if (rpJson.has("vendorvalue") && !StringUtil.isNullOrEmpty(rpJson.optString("vendorvalue", null))) {//only for customer value filter only
            StringBuilder custVendorString = new StringBuilder();
            String custIdArray[] = paramJobj.optString("vendorvalue").split(",");
            if (custIdArray.length > 0) {
                for (String custVendorValue : custIdArray) {
                    rpJson.put("vendorvalue", custVendorValue);
                    rpJson = wsUtilService.manipulateGlobalLevelFieldsNew(rpJson, companyid);
                    custVendorString.append(rpJson.optString(Constants.vendorid) + ",");
                    rpJson.remove("vendorvalue");
                    rpJson.remove(Constants.vendorid);
                }
                rpJson.put("custVendorID", custVendorString.toString().substring(0, custVendorString.length() - 1));
                rpJson.put("includeExcludeChildCmb", "false");
            }
        }
        return rpJson;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getPayments(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException, ParseException {
        JSONObject jobj = new JSONObject();
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {//Other than Android
            paramJobj = wsUtilService.replaceBooleanwithStringValues(paramJobj);
            paramJobj = createJsonPaymentMadeForReport(paramJobj);
            String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
            if (paramJobj.has(Constants.REQ_startdate)) {
                paramJobj.put("stdate", WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.REQ_startdate), originalDateFormat));
            }
            if (paramJobj.has(Constants.REQ_enddate)) {
                paramJobj.put(Constants.REQ_enddate, WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.REQ_enddate), originalDateFormat));
            }
        }
        if (!paramJobj.has(Constants.globalCurrencyKey) || !paramJobj.has(Constants.useridKey) || !paramJobj.has(Constants.REQ_startdate) || !paramJobj.has(Constants.REQ_enddate)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        paramJobj.put(Constants.permCode, Constants.VENDOR_PERMCODE);
        jobj = accVendorPaymentServiceDAOobj.getPaymentsJSON(paramJobj);
        return jobj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getReceipts(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException, ParseException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<JSONObject> list = new ArrayList<JSONObject>();
        String msg = "";
        int count=0;
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());

        if (paramJobj.has(Constants.REQ_startdate)) {
            paramJobj.put("stdate", WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.REQ_startdate), originalDateFormat));
        }
        if (paramJobj.has(Constants.REQ_enddate)) {
            paramJobj.put(Constants.REQ_enddate, WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.REQ_enddate), originalDateFormat));
        }
        //When linking Sales Order to Advance Receipt in POS we pass this key ERP-40704
        if (paramJobj.optBoolean(Constants.islinkadvanceflag)) {
            paramJobj.put("accid", paramJobj.optString(Constants.customerid));
            paramJobj.put("currencyfilterfortrans", paramJobj.optString("currencyName"));
            paramJobj.put("nondeleted", true);
            paramJobj.put("deleted", false);
            paramJobj.put("applyFilterOnCurrency", true);
            paramJobj.put("isReceipt", true);
            paramJobj.put(Constants.requestModuleId, Constants.Acc_Sales_Order_ModuleId);
            jobj = accReceiptServiceDAO.getAdvanceCustomerPaymentForRefunds(paramJobj);
        } else { //For Reports
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {//Other than Android
                paramJobj = wsUtilService.replaceBooleanwithStringValues(paramJobj);
                paramJobj = createJsonReceivePaymentForReport(paramJobj);
            }
            if (!paramJobj.has(Constants.globalCurrencyKey) || !paramJobj.has(Constants.useridKey) || !paramJobj.has(Constants.REQ_startdate) || !paramJobj.has(Constants.REQ_enddate)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            paramJobj.put("permCode", Constants.CUSTOMER_VIEWALL_PERMCODE);
            KwlReturnObject result = accReceiptServiceDAO.getReceiptList(paramJobj);
            list = result.getEntityList();
            count = result.getRecordTotalCount();
            for (JSONObject jSONObject : list) {
                jArr.put(jSONObject);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        }
        jobj.put(Constants.RES_success, true);
        jobj.put("msg", msg);
        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getDesignTemplateList(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONObject jobj = new JSONObject();
         paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (!paramJobj.has("isActive") || !paramJobj.has("moduleArray") || !paramJobj.has("start") || !paramJobj.has("limit")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        jobj = customDesignServiceobj.getDesignTemplateList(paramJobj);
        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getDeliveryOrderMerged(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {//Other than Android
                paramJobj = wsUtilService.replaceBooleanwithStringValues(paramJobj);
                paramJobj = createJsonDeliveryOrderForReport(paramJobj);       //Give default values to reports
                String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
                if (paramJobj.has(Constants.REQ_startdate)) {
                    paramJobj.put(Constants.REQ_startdate, WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.REQ_startdate), originalDateFormat));
                }
                if (paramJobj.has(Constants.REQ_enddate)) {
                    paramJobj.put(Constants.REQ_enddate, WSServiceUtil.getGlobalFormattedDate(paramJobj.getString(Constants.REQ_enddate), originalDateFormat));
                }
            }
            
            if (!paramJobj.has("pendingapproval") || !paramJobj.has(Constants.REQ_startdate) || !paramJobj.has(Constants.REQ_enddate)|| !paramJobj.has(Constants.start) ||
                    !paramJobj.has(Constants.limit) || !paramJobj.has("salesPersonFilterFlag")|| !paramJobj.has(Constants.globalCurrencyKey)|| !paramJobj.has(Constants.useridKey)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            jobj =accInvoiceServiceDAO.getDeliveryOrdersMerged(paramJobj);
        } catch (Exception ex) {
             Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject printDocumentDesignerTemplate(JSONObject paramJobj, HttpServletResponse servletresponse) throws JSONException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (!paramJobj.has("bills") || !paramJobj.has("recordids") || !paramJobj.has("isConsignment")) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            jobj = accDocumentDesignService.getDocumentDesignerEmailTemplateJson(paramJobj);
            String fileName = paramJobj.optString("filename");
            String fileType = paramJobj.optString("filetype");
              
            ArrayList<String> buildHtml = new ArrayList<String>();
            ArrayList<String> pageFooterHtml = new ArrayList<String>();
            ArrayList<String> fontStyleValue = new ArrayList<String>();
            ArrayList<String> pageHeaderHtml = new ArrayList<String>();
            ArrayList<String> pageFontSize = new ArrayList<String>();
            buildHtml.add(jobj.optString("buildHtml"));
            pageFooterHtml.add(jobj.optString("pagefooterhtml"));
            fontStyleValue.add(jobj.optString("fontstylevalue"));
            pageHeaderHtml.add(jobj.optString("pageheaderhtml"));
            pageFontSize.add(jobj.optString("pagefontsize"));
            
             CustomDesignHandler.writeFinalDataToFileJSONNew(fileName + ".pdf", fileType, buildHtml,jobj.optString("pagelayoutproperty"), pageFooterHtml, servletresponse, Integer.parseInt(paramJobj.optString("moduleid")),paramJobj.optString("recordids"), paramJobj, fontStyleValue, pageHeaderHtml, jobj.optBoolean("checkfooterflag",false), pageFontSize,jobj.optString("extLIJobj"));

        } catch (Exception ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ByteArrayOutputStream exportMobilePDFImages(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject jobj = new JSONObject();
        ByteArrayOutputStream baos = null;
        try {
            System.out.println("TransactionServiceImpl.exportMobilePDFImages() call started....");
            int moduleid = paramJobj.get("moduleid") != null ? paramJobj.getInt("moduleid") : 0;
            if (moduleid == 0) {
                throw ServiceException.FAILURE("Invalid moduleid", "e01", false);
            } else if (moduleid == Constants.Acc_Credit_Note_ModuleId) {   //Sales Return with CN  ERP-30209
                jobj = accExportReportsServiceDAOobj.exportSalesReturnWithCN(paramJobj);
            } else if (moduleid == Constants.Acc_Cash_Sales_ModuleId) {   //Cash Sales  ERP-30206
                jobj = accExportReportsServiceDAOobj.exportCashSales(paramJobj);
            } else if (moduleid == Constants.Acc_Invoice_ModuleId) {   //Sales Invoice  ERP-30207
                jobj = accExportReportsServiceDAOobj.exportCreditSales(paramJobj);
            } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {   //Sales Return ERP-30208
                jobj = accExportReportsServiceDAOobj.exportSalesReturn(paramJobj);
            } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {   //Sales Order ERP-30984
                jobj = accExportReportsServiceDAOobj.exportSalesOrder(paramJobj);
            }
            String jasperImageURL = URLUtil.buildRestURL("jasperImageURL");
            jasperImageURL = jasperImageURL + "converter/image";
            baos = apiCallHandlerService.restPostMethodForBAOS(jasperImageURL, jobj.toString());
            System.out.println("TransactionServiceImpl.exportMobilePDFImages() call ended....");
        } catch (Exception e) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return baos;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getPurchaseOrder(JSONObject paramJobj)throws JSONException, ServiceException ,SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray columnConfigArr = new JSONArray();
            JSONArray columnModelConfigArr = new JSONArray();
            if (!paramJobj.has("projectid") && paramJobj.get("projectid") != null) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            if (!paramJobj.has(Constants.deleted)) {
                paramJobj.put(Constants.deleted, false);
            }
            if (!paramJobj.has("orderLinkedWithDocType")) {
                paramJobj.put("orderLinkedWithDocType", 0);
            }
            if (!paramJobj.has("CashAndInvoice")) {
                paramJobj.put("CashAndInvoice", true);
            }

            String companyid = paramJobj.getString(Constants.companyKey);
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            String gcurrencyid = company.getCurrency().getCurrencyID();
            String projectid = paramJobj.getString("projectid");
            paramJobj.put(Constants.globalCurrencyKey,gcurrencyid);
            String searchString = getSearchColumns(companyid, projectid);
            paramJobj.put("searchString", searchString);
            jobj = accPurchaseOrderServiceDAOobj.getPurchaseOrdersMerged(paramJobj);
            paramJobj.put(Constants.moduleIds, Constants.Acc_Purchase_Order_ModuleId);
            columnConfigArr = fieldManagerServiceobj.getColumnHeadersConfigList(paramJobj);
            String moduleid = String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
            columnModelConfigArr = accCustomReportService.getCustomReportMeasureFieldJsonArray(columnConfigArr, moduleid, paramJobj);
            /*
             * Batch Serials
             */
            columnModelConfigArr = accFieldSetUpServiceDAOObj.getBatchSerialsFieldsJsonArray(columnConfigArr, moduleid, paramJobj);
            jobj.put(Constants.RES_METADATA, columnModelConfigArr);
        } catch (Exception ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);

        }
        return jobj;

    }
             
   private JSONObject jsonCreateCreditNote(JSONObject paramJobj) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        JSONObject cnJson = paramJobj;
        
        if (!cnJson.has("amount") || cnJson.optDouble("amount",0.0)==0.0) {
            throw ServiceException.FAILURE("Total amount should be greater than zero.", "", false);
        }
        
        cnJson = wsUtilService.replaceBooleanwithStringValues(cnJson);
        cnJson.put(Constants.isdefaultHeaderMap, true);//ERP-35654:written to handle propagation in save credit note
        int cntype =CreditNote.CREDITNOTE_OTHERWISE;
        if(!StringUtil.isNullOrEmpty(cnJson.optString(Constants.cntype,null))){
            cntype=Integer.parseInt(cnJson.optString(Constants.cntype));
        }        
        
        boolean isEdit = StringUtil.isNullOrEmpty(cnJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(cnJson.getString(Constants.isEdit));
        String cnid = cnJson.optString("noteid", null);
        String cnnumber = "";
        String companyid = paramJobj.optString(Constants.companyKey);
        if (!cnJson.has("discount") || (cnJson.has("discount") && cnJson.get("discount") != null)) {
            cnJson.put("discount", "0.0");
        }
        cnJson.put("modulename", String.valueOf(Constants.Acc_Credit_Note_ModuleId));

        if (!StringUtil.isNullOrEmpty(cnJson.optString("creditnotenumber", null))) {
            cnnumber = cnJson.optString("creditnotenumber");
            if (!isEdit) {
                cnJson.put(Constants.sequenceformat, "NA");
            }
            cnJson.put("number", cnnumber);
        } else {
            cnJson.remove(Constants.sequenceformat);
        }
        cnJson = wsUtilService.getSequenceFormatId(paramJobj, String.valueOf(Constants.Acc_Credit_Note_ModuleId));

        if (cntype == CreditNote.CREDITNOTE_AGAINST_INVOICE) {
            if (!cnJson.has("invoicedetails")) {
                throw ServiceException.FAILURE("Missing invoice details", "", false);
            }
            
        }else if(cntype == CreditNote.CREDITNOTE_OTHERWISE){
            cnJson.put("otherwise", "true");//mandatory for creditnote
            cnJson.remove("invoicedetails");
            cnJson.remove("invoiceids");
        }
        
        cnJson.put("includingGST", paramJobj.optBoolean("gstincluded", false));
        cnJson.put("defaultAdress", "true");
        //replace values for creditnote for invoicedetails and accountdetails
        paramJobj = wsUtilService.manipulateAccountandInvoiceDetails(paramJobj);
        
        if (isEdit) {// For POS Only
            KwlReturnObject soresult = null;
            CreditNote cnObj = null;
            if (!StringUtil.isNullOrEmpty(cnid)) {
                soresult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
                cnObj = (CreditNote) soresult.getEntityList().get(0);
            } else if (!StringUtil.isNullOrEmpty(cnnumber)) {
                cnid = accCommonTablesDAO.getTransactionId(companyid, cnnumber, String.valueOf(Constants.Acc_Credit_Note_ModuleId));
                soresult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
                cnObj = (CreditNote) soresult.getEntityList().get(0);
            }

            if (cnObj != null) {
                cnJson.put("noteid", cnid);
                if (StringUtil.isNullOrEmpty(cnJson.optString("creditnotenumber", null))) {
                    cnJson.put("number", cnObj.getCreditNoteNumber());
                }
                if (!cnJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
                    cnJson.put(Constants.sequenceformat, cnObj.getSeqformat() != null ? cnObj.getSeqformat().getID() : "NA");
                }
            }
        }
        return cnJson;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveCreditNote(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            String creationdate = null;
            if (paramJobj.has("billdate") && !StringUtil.isNullOrEmpty(paramJobj.optString("billdate"))) {
                creationdate = paramJobj.optString("billdate") == null ? null : paramJobj.optString("billdate");
            }

            if (!StringUtil.isNullOrEmpty(creationdate)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
                Date transdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).parse(creationdate);
                DateFormat df = authHandler.getDateOnlyFormat();
                creationdate = df.format(transdate);
                paramJobj.put("creationdate", creationdate);
                paramJobj.remove("billdate");
            }

            //If linking date is passed it will convert the date into user date format else it will take creation date
            if (paramJobj.has("linkingdate") && !StringUtil.isNullOrEmpty(paramJobj.optString("linkingdate"))) {
                String linkingdate = null;
                linkingdate = paramJobj.optString("linkingdate") == null ? null : paramJobj.optString("linkingdate");
                if (!StringUtil.isNullOrEmpty(linkingdate)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
                    Date transdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).parse(linkingdate);
                    DateFormat df = authHandler.getDateOnlyFormat();
                    linkingdate = df.format(transdate);
                    paramJobj.put("linkingdate", linkingdate);
                }
            } else if (paramJobj.has("creationdate") && !StringUtil.isNullOrEmpty(paramJobj.optString("creationdate"))) {
                paramJobj.put("linkingdate", creationdate);
            }
            paramJobj = wsUtilService.manipulateGlobalLevelFieldsNew(paramJobj, paramJobj.optString(Constants.companyKey));
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {//For POS
                paramJobj = jsonCreateCreditNote(paramJobj);//For squats & pos related
            }
            
            String customerid=paramJobj.optString(Constants.customerName);
            paramJobj.put("accid",customerid);
            KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Customer.class.getName(),customerid);
            Customer customerObj = (Customer) soresult.getEntityList().get(0);
            if (customerObj != null) {
                paramJobj.put("accountid", customerObj.getAccount().getID());
            }
            paramJobj.put("exchangeratefortransaction",paramJobj.opt("externalcurrencyrate") );
            String customField = paramJobj.optString(Constants.customfield, null);
            if (!StringUtil.isNullOrEmpty(customField)) {
                JSONArray customJArray = wsUtilService.createJSONForCustomField(customField, paramJobj.optString(Constants.companyKey), Constants.Acc_Invoice_ModuleId);
                paramJobj.put(Constants.customfield, customJArray);
            }
            
            if (paramJobj != null) {
                if (!paramJobj.has("creationdate") || !paramJobj.has("details")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
            System.out.println(paramJobj);
            jobj = jobj = accCreditNoteService.saveCreditNoteJSON(paramJobj);
            
            if (jobj.has(Constants.RES_msg)) {
                jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
                jobj.remove(Constants.RES_msg);
            }
            if (jobj.has(Constants.RES_success)) {
                jobj.put(Constants.RES_success, (Boolean) jobj.get(Constants.RES_success));
            } else {
                jobj.put(Constants.RES_success, false);
            }
        } catch (Exception ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }        
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject savelinkAdvanceReceiptToSalesOrder(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
//            paramJobj.put("accid", paramJobj.optString(Constants.customerid));
//            paramJobj.put("currencyfilterfortrans", paramJobj.optString("currencyName"));
            //Sales Order id
            paramJobj.put("docid", paramJobj.optString("soid"));
            paramJobj.put(Constants.requestModuleId, Constants.Acc_Sales_Order_ModuleId);
            //Rceipt Advance Detail id
            paramJobj.put("linkedAdvancePaymentId", paramJobj.optString("paymentdetailid"));
            //Receipt no
            paramJobj.put("linkedAdvancePaymentNo", paramJobj.optString("receiptno"));
            jobj = accSalesOrderServiceobj.saveSalesOrderLinkingJSON(paramJobj);
             if (jobj.has(Constants.RES_msg)) {
                jobj.put(Constants.RES_MESSAGE, jobj.getString(Constants.RES_msg));
                jobj.remove(Constants.RES_msg);
            }
             jobj.put(Constants.RES_success, true);
        } catch (Exception ex) {
            Logger.getLogger(TransactionServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
}
