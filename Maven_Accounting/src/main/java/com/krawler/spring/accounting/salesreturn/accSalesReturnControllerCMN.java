/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.salesreturn;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.ServerEventManager;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.approval.consignment.ConsignmentApprovalDetails;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptController;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.Acc_ConsignmentPurchaseReturn_ModuleId;
import static com.krawler.common.util.Constants.Acc_Purchase_Return_ModuleId;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.stock.StockDAO;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.debitnote.accDebitNoteService;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderController;
import com.krawler.spring.accounting.salesorder.accSalesOrderController;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.utils.json.base.JSONArray;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletContext;

/**
 *
 * @author krawler
 */
public class accSalesReturnControllerCMN extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accInvoiceDAO accInvoiceDAOobj;
    private accSalesOrderDAO accSalesOrderDAOObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    private accAccountDAO accAccountDAOobj;
    public ImportHandler importHandler;
    public accCustomerDAO accCustomerDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccSalesReturnService accSalesReturnService;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accDebitNoteDAO accDebitNoteobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    String recId = "";
    String tranID = "";
    private static final DateFormat df = new SimpleDateFormat(Constants.yyyyMMdd);
    private exportMPXDAOImpl exportDAO;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private StockDAO stockDAO;
    private AccJournalEntryModuleService accJournalEntryModuleService;
    private accDebitNoteService accDebitNoteService;
    private fieldDataManager fieldDataManagercntrl;
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public accCreditNoteDAO getAccCreditNoteDAOobj() {
        return accCreditNoteDAOobj;
    }

    public void setAccCreditNoteDAOobj(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }

    public accDebitNoteDAO getAccDebitNoteobj() {
        return accDebitNoteobj;
    }

    public void setAccJournalEntryModuleService(AccJournalEntryModuleService accJournalEntryModuleService) {
        this.accJournalEntryModuleService = accJournalEntryModuleService;
    }

    public void setAccDebitNoteobj(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }

    public accGoodsReceiptDAO getAccGoodsReceiptobj() {
        return accGoodsReceiptobj;
    }

    public void setAccGoodsReceiptobj(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
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

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setAccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOObj) {
        this.accSalesOrderDAOObj = accSalesOrderDAOObj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccSalesReturnService(AccSalesReturnService accSalesReturnService) {
        this.accSalesReturnService = accSalesReturnService;
    }

    public void setExportDAO(exportMPXDAOImpl exportDAO) {
        this.exportDAO = exportDAO;
    }
    
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setStockDAO(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }
    public void setaccDebitNoteService(accDebitNoteService accDebitNoteService) {
        this.accDebitNoteService = accDebitNoteService;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
        
    /*Save SalesReturn*/
    public ModelAndView saveSalesReturn(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accSalesReturnService.saveSalesReturn(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
            channelName = jobj.optString(Constants.channelName, null);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
   
    /**
     * Description : This Method is used to update Sales Return
     *
     * @param <request> used to get request parameters
     * @param <response> used to send response
     * @return :JSONObject
     */
    
     public ModelAndView updateSalesReturn(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accSalesReturnService.updateSalesReturn(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
            channelName = jobj.optString(Constants.channelName, null);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
  
    public ModelAndView updateDOStatusWhilelinkingWithSRScript(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {

            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.COMPANY_SUBDOMAIN))) {
                subdomain = (String) request.getParameter(Constants.COMPANY_SUBDOMAIN);
                subdomainArray = subdomain.split(",");
            }

            KwlReturnObject company = accInvoiceDAOobj.getCompanyList(subdomainArray);
            Iterator ctr = company.getEntityList().iterator();
            while (ctr.hasNext()) {
                TransactionStatus status = null;
                try {
                    status = txnManager.getTransaction(def);
                    String companyid = ctr.next().toString();
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put(Constants.companyKey, companyid);
                    KwlReturnObject result = accInvoiceDAOobj.getDeliveryorderForSR(requestParams);
                    Iterator itr = result.getEntityList().iterator();

                    while (itr.hasNext()) {
                        String id = (String) itr.next();
                        if (!StringUtil.isNullOrEmpty(id)) {
                            accSalesReturnService.updateOpenStatusFlagInDOForSR(id,"",false,true);
                        }
                    }
                    txnManager.commit(status);
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            issuccess = true;

        } catch (Exception ex) {
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_msg, "Script completed for update isOpenInSR Flag in Delivery Order");
                jobj.put(Constants.RES_success, issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView updateSIStatusWhilelinkingWithSRScript(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.COMPANY_SUBDOMAIN))) {
                subdomain = request.getParameter(Constants.COMPANY_SUBDOMAIN).toString().trim();
                subdomainArray = subdomain.split(",");
            }
            KwlReturnObject company = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator ctr = company.getEntityList().iterator();
            TransactionStatus status = null;
            while (ctr.hasNext()) {
                String companyid = ctr.next().toString();
                try {
                    status = txnManager.getTransaction(def);
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put(Constants.companyKey, companyid);
                    KwlReturnObject result = accInvoiceDAOobj.getSalesInvoiceForSR(requestParams);
                    Iterator itr = result.getEntityList().iterator();
                    while (itr.hasNext()) {
                        String id = (String) itr.next();
                        if (!StringUtil.isNullOrEmpty(id)) {
                            accSalesReturnService.updateOpenStatusFlagInDOForSI(id);
                        }
                    }
                    txnManager.commit(status);
                    msg = companyid + "-True";
                } catch (Exception ex) {
                    msg = companyid + "-False";
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_success, issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView savePurchaseReturn(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "";
        String prInventoryJENo = "";//When MRP activated then Purchase Return created with JE. This variable used for displaying JE number in sucess message.
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        boolean isAccountingExe = false;
        boolean isConsignment=false;
        boolean isTaxDeactivated = false;
        int nocount = 0;
        int srDnFlag=0;
        Map<String,String> deleteparam=null;
         KwlReturnObject result = null;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String entryNumber = request.getParameter("number");
            boolean isFixedAsset = request.getParameter("isFixedAsset") != null ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            String sequenceformat = request.getParameter("sequenceformat");
            String debitNoteSequenceFormat=StringUtil.isNullOrEmpty(request.getParameter("cndnsequenceformat"))?"NA":request.getParameter("cndnsequenceformat");
            String debitNoteNumber=StringUtil.isNullOrEmpty(request.getParameter("cndnnumber"))?"":request.getParameter("cndnnumber");
            String companyid = sessionHandlerImpl.getCompanyid(request);            
            String srid = request.getParameter("srid");
            String inventoryURL = this.getServletContext().getInitParameter("inventoryURL");
            request.setAttribute("inventoryURL", inventoryURL);
             boolean isNoteAlso = false;
             isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteAlso"))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note
                isNoteAlso = Boolean.parseBoolean(request.getParameter("isNoteAlso"));
            }
            String fromLinkCombo = request.getParameter("fromLinkCombo") != null ? request.getParameter("fromLinkCombo") : "";
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("copyInv")) ? false : Boolean.parseBoolean(request.getParameter("copyInv"));
            KwlReturnObject socnt=null;
            KwlReturnObject debitNoteCount=null;
            deleteparam=new HashMap<String, String>();
            deleteparam.put("purchaseReturnNo", entryNumber);
            deleteparam.put("dnNo", debitNoteNumber);
            deleteparam.put(Constants.companyKey, companyid);
            if(isNoteAlso){
                deleteparam.put("isAutoCreateDn", request.getParameter("isNoteAlso"));
            }
            /*
             * check duplicate record while editing record 
             */
            if (!StringUtil.isNullOrEmpty(srid)) {//Edit case
                DebitNote debitNote=null;
                String dnId="";
                KwlReturnObject idresult = accDebitNoteobj.getDebitNoteIdFromPRId(srid, companyid);
                if (!(idresult.getEntityList().isEmpty())) {
                    debitNote = (DebitNote) idresult.getEntityList().get(0);
                }
                if (debitNote != null) {  
                    dnId=debitNote.getID();
                }
                socnt = accGoodsReceiptobj.getPurchaseReturnCountEdit(entryNumber, companyid, srid);
                if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    srDnFlag=0;
                    if(isConsignment){
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.CR.consignmentreturnno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }else if(isFixedAsset){
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.AP.assetreturnno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                    else{
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.PR.purchasereturnno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }
                if (isNoteAlso && debitNoteSequenceFormat.equalsIgnoreCase("NA")) {//Autocreate debitit note check true
                    
                    debitNoteCount = accDebitNoteobj.getDNFromNoteNoAndId(debitNoteNumber, companyid, dnId);//checks for duplicate number
                    if (debitNoteCount.getRecordTotalCount() > 0) {
                        srDnFlag=1;
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.DN.debitnoteno", null, RequestContextUtils.getLocale(request)) + debitNoteNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }
            } else {
                /*
                 * check duplicate no while creating new record
                 */
                socnt = accGoodsReceiptobj.getPurchaseReturnCount(entryNumber, companyid);
                if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    srDnFlag=0;
                    if(isConsignment){
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.CR.consignmentreturnno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }else if(isFixedAsset){
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.AP.assetreturnno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }else{
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.PR.purchasereturnno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }
                if (isNoteAlso && debitNoteSequenceFormat.equalsIgnoreCase("NA")) {//Autocreate credit note check true
                    debitNoteCount = accDebitNoteobj.getDNFromNoteNo(debitNoteNumber, companyid);    //checks for duplicate number
                    if (debitNoteCount.getRecordTotalCount() > 0) {
                        srDnFlag=1;
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.DN.debitnoteno", null, RequestContextUtils.getLocale(request)) + debitNoteNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }
                //Check Deactivate Tax in New Transaction.
                if (!fieldDataManagercntrl.isTaxActivated(paramJobj)) {
                    isTaxDeactivated = true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
            }
              String countryid = sessionHandlerImpl.getCountryId(request);
                if (Integer.parseInt(countryid) == Constants.indian_country_id) {
                String supplierInvoiceNumber = request.getParameter(Constants.SUPPLIERINVOICENO);
                String vendor = request.getParameter("vendor");
                String purchaseInvNo = "";
                if (!StringUtil.isNullOrEmpty(supplierInvoiceNumber) && !StringUtil.isNullOrEmpty(vendor)) {
                    JSONObject reqParams=new JSONObject();
                    reqParams.put("supplierInvoiceNumber", supplierInvoiceNumber);
                    reqParams.put("vendor",vendor);
                    reqParams.put("companyid",companyid);
                    reqParams.put("srid", srid);
                    result = accGoodsReceiptobj.getPurchaseReturnDuplicateSIN(reqParams);
                    if (result != null && result.getRecordTotalCount() > 0) {
                        List<String> li = result.getEntityList();
                        if (!li.isEmpty()) {
                            for (String obj : li) {
                                purchaseInvNo = !StringUtil.isNullOrEmpty(obj)?obj:"";
                            }
                                throw new AccountingException(messageSource.getMessage("acc.gst.SINErrorPR", null, RequestContextUtils.getLocale(request)) +"<b>"+ purchaseInvNo +"<b>");
                            }
                        }
                    }
                }
            synchronized (this) {
                /*
                 * Checks duplicate number for simultaneous transactions
                 */
                
                status = txnManager.getTransaction(def);
                KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Return_ModuleId);//Get entry from temporary table
                if (resultInv.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    srDnFlag=0;
                    if(isConsignment){
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.CR.selectedConsignmentreturnno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    }else if(isFixedAsset){
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.AP.selectedAssetreturnno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    }else{
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.PR.selectedpurchasereturnno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    }
                } else {
                    accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Purchase_Return_ModuleId);//Insert entry into temporary table
                }
                
                if (isNoteAlso && debitNoteSequenceFormat.equals("NA")) {
                    resultInv = accCommonTablesDAO.getTransactionInTemp(debitNoteNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Get entry from temporary table
                    if (resultInv.getRecordTotalCount() > 0) {
                        srDnFlag=1;
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.DN.selecteddebitnoteno", null, RequestContextUtils.getLocale(request)) + debitNoteNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(debitNoteNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Insert entry into temporary table
                    }
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List salesReturnList = accSalesReturnService.savePurchaseReturn(request);
            PurchaseReturn salesReturn = (PurchaseReturn) salesReturnList.get(0);
            billno = salesReturn.getPurchaseReturnNumber();
            billid = salesReturn.getID();
            String noteMsg = "";
            String debitNoteId = "";
            if(salesReturn.isIsNoteAlso()){
                debitNoteNumber = (String) salesReturnList.get(1);
                debitNoteId = (String) salesReturnList.get(2);
                noteMsg = messageSource.getMessage("acc.purchaseReturn.withDebitNote", null, RequestContextUtils.getLocale(request))+" " +"(" + debitNoteNumber + ")";
            }
            issuccess = true;
//            boolean isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            txnManager.commit(status);
            status=null;
            TransactionStatus AutoNoStatus = null;
            try {
                synchronized (this) {
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    AutoNoStatus = txnManager.getTransaction(def1);

                    if (!sequenceformat.equals("NA") && StringUtil.isNullOrEmpty(srid) && !StringUtil.isNullOrEmpty(sequenceformat)) {
                        boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                        String nextAutoNumber = "";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_PURCHASERETURN, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNumber);
                        } else {
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PURCHASERETURN, sequenceformat, seqformat_oldflag, salesReturn.getOrderDate());
                        }
                        seqNumberMap.put(Constants.DOCUMENTID, billid);
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        billno = accSalesOrderDAOObj.updatePREntryNumberForNewPR(seqNumberMap);
                    }
                    if (salesReturn.isIsNoteAlso()) {
                        if (!debitNoteSequenceFormat.equalsIgnoreCase("NA") && StringUtil.isNullOrEmpty(srid)) { //create new case with sequence format other than NA
                            String nextDNAutoNoInt = "";
                            String debitNoteNo = "";
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_DEBITNOTE, debitNoteSequenceFormat, false, salesReturn.getOrderDate());
                            seqNumberMap.put(Constants.DOCUMENTID, debitNoteId);
                            seqNumberMap.put(Constants.companyKey, companyid);
                            seqNumberMap.put(Constants.SEQUENCEFORMATID, debitNoteSequenceFormat);
                            debitNoteNumber = accSalesOrderDAOObj.updateDNEntryNumberForNewPR(seqNumberMap);
                            noteMsg = messageSource.getMessage("acc.purchaseReturn.withDebitNote", null, RequestContextUtils.getLocale(request))+" " +"(" + debitNoteNumber + ")";
                        }
                    }
                    /* Saving Linking Information in PR & DN linking table if Creating Purchase return with Debit Note*/
                    if (salesReturn.isIsNoteAlso()) {
                        List debitNoteList = new ArrayList();
                        debitNoteList.add(debitNoteId);
                        debitNoteList.add(debitNoteNumber);
                         debitNoteList.add(entryNumber);
                        accSalesReturnService.addLinkingInformationCreatingPRwithDN(salesReturn, debitNoteList);

                    }
                    if (salesReturn.getInventoryJE() != null && StringUtil.isNullOrEmpty(srid)) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put(Constants.companyKey, companyid);
                        JEFormatParams.put("isdefaultFormat", true);
                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                        accJournalEntryModuleService.updateJEEntryNumberForNewJE(jeDataMap, salesReturn.getInventoryJE(), companyid, format.getID(), salesReturn.getInventoryJE().getPendingapproval());
                        /**
                         * Get Purchase Return JE Number as it is required to display in prompt while saving 
                         * Purchase Return document.
                         */
                        prInventoryJENo = salesReturn.getInventoryJE()!=null?salesReturn.getInventoryJE().getEntryNumber():"";
                    }
                    // Save Purchase Return Term Map
                    String InvoiceTerms = paramJobj.optString("invoicetermsmap", "[]");
                    if (StringUtil.isAsciiString(InvoiceTerms)) {
                        boolean isSR = false;
                        accSalesReturnService.mapSalesPurcahseReturnTerms(InvoiceTerms, salesReturn.getID(), paramJobj.optString(Constants.useridKey), isSR);
                    }

                }
                 if (AutoNoStatus != null) {
                    txnManager.commit(AutoNoStatus);
                }
                /* Preparing Audit trial message if document is linking at teh time of creating */
                 String linkedDocuments = (String) salesReturnList.get(3);
                String linkingMessages = "";
                if (!StringUtil.isNullOrEmpty(linkedDocuments) && !StringUtil.isNullOrEmpty(fromLinkCombo)) {
                    linkingMessages = " by Linking to " + fromLinkCombo + " " + linkedDocuments;
                }
                String invoiceDetails = request.getParameter("invoicedetails");
                if (!StringUtil.isNullOrEmpty(invoiceDetails)) {
                    JSONArray jArr = new JSONArray(invoiceDetails);
                    for (int k = 0; k < jArr.length(); k++) {
                        JSONObject invjobj = jArr.getJSONObject(k);

                        auditTrailObj.insertAuditLog(AuditAction.DABIT_NOTE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has linked Debit Note " + debitNoteNumber + " with Vendor Invoice " + invjobj.getString("billno") + ".", request, debitNoteId);
                    }
                }
                String moduleName = Constants.moduleID_NameMap.get(Acc_Purchase_Return_ModuleId);
                if (isFixedAsset) {
                    moduleName = Constants.ASSET_PURCHASE_RETURN;
                }
                if (isConsignment) {
                    moduleName = Constants.moduleID_NameMap.get(Acc_ConsignmentPurchaseReturn_ModuleId);
                }
                String action = "added a new";
                if (isEdit == true && isCopy == false) {
                    action = "updated";
                }
               /* Updating entry in Audit Trial while unlinking transaction through Editing*/
                String unlinkMessage = (String) salesReturnList.get(4);
                if (!StringUtil.isNullOrEmpty(unlinkMessage)) {
                    auditTrailObj.insertAuditLog(AuditAction.PURCHASE_REQUISITION_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked " + "Purchase Return(s) " + billno + unlinkMessage + ".", request, billno);
                }
                
                auditTrailObj.insertAuditLog(AuditAction.PURCHASE_REQUISITION_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " "+moduleName+" " + billno+linkingMessages , request, salesReturn.getID());

            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                }
                accSalesReturnService.deleteEntryInTemp(deleteparam);//Delete entry in temporary table
                Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            } 
            if (isConsignment) {
                msg = messageSource.getMessage("acc.Consignment.purchaseReturnhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + " <b>" + billno + "</b>";
            } else if (isFixedAsset) {
                msg = messageSource.getMessage("acc.field.assetPurchaseReturnHasBeenSavedSuccessfully", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + " <b>" + billno + "</b>";
            } else if (!StringUtil.isNullOrEmpty(prInventoryJENo)) {//when MRP module is activated then there will be JE Generation. In this case we need to show JE number As well
                msg = messageSource.getMessage("acc.module.name.31", null, RequestContextUtils.getLocale(request))+" "+ noteMsg +" " +messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>, " + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + prInventoryJENo + "</b>";
            } else {
                msg = messageSource.getMessage("acc.module.name.31", null, RequestContextUtils.getLocale(request))+" "+ noteMsg +" " +messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";
            }
             status = txnManager.getTransaction(def);
            accSalesReturnService.deleteEntryInTemp(deleteparam);//Delete entry in temporary table
            txnManager.commit(status);
            
            //==============Create Rounding JE Start=====================

            if (salesReturn.isIsNoteAlso() && !StringUtil.isNullOrEmpty(request.getParameter("invoicedetails"))) {//Sales Return with CN having Invoices linked
                TransactionStatus roundingJEstatus = null;
                try {
                    roundingJEstatus = txnManager.getTransaction(def);
                    paramJobj.put("PRWithDN", true);
                    paramJobj.put("isEdit", isEdit);
                    paramJobj.put("cnid", debitNoteId);
                    accDebitNoteService.postRoundingJEAfterLinkingInvoiceInDebitNote(paramJobj);
                    txnManager.commit(roundingJEstatus);
                } catch (ServiceException ex) {
                    if (roundingJEstatus != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //==============Create Rounding JE End=======================
//            txnManager.commit(status);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
           accSalesReturnService.deleteEntryInTemp(deleteparam);//Delete entry in temporary table
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            accSalesReturnService.deleteEntryInTemp(deleteparam);//Delete entry in temporary table
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("billid", billid);     //To get Billid & Billno in response in PR copy case
                jobj.put("billno", billno);
                jobj.put("accException", isAccountingExe);
                jobj.put("srDnFlag", srDnFlag);
                jobj.put(Constants.isTaxDeactivated, isTaxDeactivated);
                String channelName = "";
                if (isConsignment) {
                    channelName = "/ConsignmentPurchaseReturnReport/gridAutoRefresh";
                } else {
                    channelName = "/PurchaseReturnReport/gridAutoRefresh";
                }
                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
      public ModelAndView updateGRStatusWhileLinkingWithPRScript(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.COMPANY_SUBDOMAIN))) {
                subdomain = (String) request.getParameter(Constants.COMPANY_SUBDOMAIN);
                subdomainArray = subdomain.split(",");
            }

            KwlReturnObject company = accInvoiceDAOobj.getCompanyList(subdomainArray);
            Iterator ctr = company.getEntityList().iterator();

            while (ctr.hasNext()) {
                TransactionStatus status = null;
                try {
                    status = txnManager.getTransaction(def);
                    String companyid = ctr.next().toString();
                    String purchasereturnId="";
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put(Constants.companyKey, companyid);
                    KwlReturnObject result = accInvoiceDAOobj.getGoodsReceiptOrder(requestParams);
                    Iterator itr = result.getEntityList().iterator();

                    while (itr.hasNext()) {
                        String goodsreceiptorderid = (String) itr.next();
                        if (!StringUtil.isNullOrEmpty(goodsreceiptorderid)) {
                            accSalesReturnService.updateOpenStatusFlagInGRForPR(goodsreceiptorderid, companyid,purchasereturnId);
                        }
                    }
                    txnManager.commit(status);
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            issuccess = true;

        } catch (Exception ex) {

            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_msg, "Script completed for update isOpeninpr Flag in GR");
                jobj.put(Constants.RES_success, issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateVIStatusWhileLinkingWithPRScript(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.COMPANY_SUBDOMAIN))) {
                subdomain = (String) request.getParameter(Constants.COMPANY_SUBDOMAIN);
                subdomainArray = subdomain.split(",");
            }

            KwlReturnObject company = accInvoiceDAOobj.getCompanyList(subdomainArray);
            Iterator ctr = company.getEntityList().iterator();

            while (ctr.hasNext()) {
                TransactionStatus status = null;
                try {
                    status = txnManager.getTransaction(def);
                    String companyid = ctr.next().toString();
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put(Constants.companyKey, companyid);
                    KwlReturnObject result = accInvoiceDAOobj.getGoodsReceipt(requestParams);
                    Iterator itr = result.getEntityList().iterator();

                    while (itr.hasNext()) {
                        String goodsreceiptid = (String) itr.next();
                        if (!StringUtil.isNullOrEmpty(goodsreceiptid)) {
                            accSalesReturnService.updateOpenStatusFlagInVIForPR(goodsreceiptid, companyid);
                        }
                    }
                    txnManager.commit(status);
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            issuccess = true;

        } catch (Exception ex) {

            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_msg, "Script completed for update isOpeninpr Flag in VI");
                jobj.put(Constants.RES_success, issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getConsignmentSalesQAReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();

        Paging paging = null;
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String companyId = sessionHandlerImpl.getCompanyid(request);
            jeresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            String customerId = request.getParameter("customerid");
            String searchString = request.getParameter("ss");
            String statusType = request.getParameter("status");
            String fd = request.getParameter("frmDate");
            String td = request.getParameter("toDate");

            Date fromDate = null;
            Date toDate = null;
            try {
                if (!StringUtil.isNullOrEmpty(fd) && !StringUtil.isNullOrEmpty(td)) {
                    fromDate = df.parse(fd);
                    toDate = df.parse(td);
                }
            } catch (ParseException ex) {
            }

            KwlReturnObject retObj = null;
            List resultList = null;
            retObj = accSalesReturnService.getConsignmentSalesQAReport(company, fromDate, toDate, statusType, customerId, searchString, paging);
            if (retObj != null) {
                paging.setTotalRecord(retObj.getRecordTotalCount());
                resultList = retObj.getEntityList();
            }
            if (resultList != null) {
                Iterator itr = resultList.iterator();

                while (itr.hasNext()) {
                  
                    ConsignmentApprovalDetails consDetail =(ConsignmentApprovalDetails)itr.next();
                        
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", consDetail.getId());
                        jObj.put("transactionno", consDetail.getConsignment().getTransactionNo());
                        jObj.put("createdon", consDetail.getConsignment().getCreatedOn() != null ? df.format(consDetail.getConsignment().getCreatedOn()) : "");
                        jObj.put("productid", consDetail.getConsignment().getProduct().getProductid());
                        jObj.put("productdescription", consDetail.getConsignment().getProduct().getDescription());
                        jObj.put("customername", consDetail.getConsignment().getCustomer().getName());
                        jObj.put("isSerialForProduct", consDetail.getConsignment().getProduct().isIsSerialForProduct());
                        jObj.put("isBatchForProduct", consDetail.getConsignment().getProduct().isIsBatchForProduct());
                        
                        jObj.put("batch", consDetail.getBatchName());
                        if (StringUtil.isNullOrEmpty(consDetail.getBatchName())) {
                            jObj.put("batch", "-");
                        }
                        jObj.put("serial", consDetail.getSerialName());
                        if (StringUtil.isNullOrEmpty(consDetail.getSerialName())) {
                            jObj.put("serial", "-");
                        }
                        jObj.put("quantity", consDetail.getQuantity());
                        jObj.put("status", consDetail.getApprovalStatus());
                        jObj.put("qaremarks", consDetail.getRemark() != null ? consDetail.getRemark() : "");
                        jObj.put("inspectedby", consDetail.getInspector() != null ? consDetail.getInspector().getFullName() : "");
//                        jObj.put("costcenter", consDetail.getConsignment() != null ? consDetail.getInspector().getFullName() : "");
//                        jObj.put("salesperson", consDetail.getInspector() != null ? consDetail.getInspector().getFullName() : "");
//                       
                        jObj.put("costcenter",  "");
                        jObj.put("salesperson","");
                       
                        
                        jArray.put(jObj);

                }
                issuccess = true;
                msg = "Consignment QA Report  has been fetched successfully";
            }

        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
    
    public ModelAndView getStockRequestOnLoanReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        JSONArray productCustomFieldInfo = new JSONArray();

        Paging paging = null;
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String companyId = sessionHandlerImpl.getCompanyid(request);
            jeresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            String customerId = request.getParameter("customerid");
            String documenttype = request.getParameter("documenttype");
            String searchString = request.getParameter("ss");
            String statusType = request.getParameter("status");
            String fd = request.getParameter("frmDate");
            String td = request.getParameter("toDate");
             // Find out any product custom fields need to show in this report
            String customFieldQuery = "select customcolumninfo from showcustomcolumninreport where moduleid = ? and companyid = ?";
            List<String> customFieldinfoList = null;
            customFieldinfoList = accCommonTablesDAO.executeSQLQuery(customFieldQuery, new Object[]{Constants.Acc_Product_Master_ModuleId, companyId});

            HashMap<String, HashMap> productCustomData = new HashMap<String, HashMap>();
            HashMap<String, String> replaceFieldMapProd = new HashMap<String, String>();
            HashMap<String, String> customFieldMapProd = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMapProd = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            Map<String, Double> productWLQtymp=new HashMap<>();

            if (customFieldinfoList.size() > 0) {
                String jsonString = customFieldinfoList.get(0);
                JSONArray productCustomFields = new JSONArray(jsonString);
                String fieldIds = "";
                for (int jCnt = 0; jCnt < productCustomFields.length(); jCnt++) {
                    fieldIds = fieldIds.concat("'").concat(productCustomFields.getJSONObject(jCnt).getString("fieldid")).concat("',");
                }
                if (!StringUtil.isNullOrEmpty(fieldIds)) {
                    fieldIds = fieldIds.substring(0, fieldIds.length() - 1);
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "INid"));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId, fieldIds));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMapProd, customFieldMapProd, customDateFieldMapProd);
            }

            if (replaceFieldMapProd.size() > 0) {
                for (Map.Entry<String, String> varEntry : replaceFieldMapProd.entrySet()) {
                    JSONObject fieldInfo = new JSONObject();
                    fieldInfo.put("dataindex", varEntry.getKey());
                    fieldInfo.put("columnname", varEntry.getKey().replaceAll("Custom_", ""));
                    productCustomFieldInfo.put(fieldInfo);
                }
            }

            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            Date fromDate = null;
            Date toDate = null;
            try {
                if (!StringUtil.isNullOrEmpty(fd) && !StringUtil.isNullOrEmpty(td)) {
                    fromDate = df.parse(fd);
                    toDate = df.parse(td);
                }
            } catch (ParseException ex) {
            }

            KwlReturnObject retObj = null;
            List<Object[]> resultList = null;
            retObj = accSalesReturnService.getStockRequestOnLoanReport(company, fromDate, toDate, documenttype, customerId, searchString, paging);
            if (retObj != null) {
                if (paging != null) {
                    paging.setTotalRecord(retObj.getRecordTotalCount());
                }
                resultList = retObj.getEntityList();
                productWLQtymp=stockDAO.getAvailableQuantity(company, true);
                }
            if (resultList != null) {

                for (Object[] roww : resultList) {
                 
                    String status = "";
                    if (roww[4] != null) {
                        if ("Stock".equalsIgnoreCase(roww[4].toString())) {
                            continue;
                        }
                        if ("Request".equalsIgnoreCase(roww[4].toString()) ) {
                            if ( roww[22] != null && roww[23] != null && Double.parseDouble(roww[22].toString()) == Double.parseDouble(roww[23].toString())) { 
                                status = "Rejected";
                            } else if (roww[22] != null && roww[24] != null && roww[23] != null  && Double.parseDouble(roww[22].toString())+Double.parseDouble(roww[24].toString()) == Double.parseDouble(roww[23].toString())) {
                                status = "Approved";
                            } else if (roww[22] != null && roww[24] != null && Double.parseDouble(roww[22].toString())+Double.parseDouble(roww[24].toString()) == 0) {
                                status = "Pending Approval";
                            } else if (roww[22] != null && roww[24] != null && roww[23] != null  && Double.parseDouble(roww[22].toString())+Double.parseDouble(roww[24].toString()) < Double.parseDouble(roww[23].toString())) {
                                status = "Partially Approved";
                            }else{
                                status="";
                            }
                        }
                    }

                    JSONObject jObj = new JSONObject();
                    String purposeOfLoan = "";
                    jObj.put("id", roww[0] != null ? roww[0].toString() : "");
                    jObj.put("transactionno", roww[1] != null ? roww[1].toString() : "");
                    jObj.put("productid", roww[2] != null ? roww[2].toString() : "");
                    jObj.put("productdescription", roww[3] != null ? roww[3].toString() : "");
                    jObj.put("documenttype", roww[4] != null ? roww[4].toString() : "");
                    jObj.put("costcenter", roww[5] != null ? roww[5].toString() : "");
                    jObj.put("createdon", roww[6] != null ? roww[6].toString() : "");
                    jObj.put("salesperson", roww[7] != null ? roww[7].toString() : "");
                    jObj.put("status", status);
                    jObj.put("warehouse", roww[9] != null ? roww[9].toString() : "");
                    jObj.put("location", roww[10] != null ? roww[10].toString() : "");
                    jObj.put("assetno", roww[11] != null ? roww[11].toString() : "");
                    jObj.put("requestqunatity", roww[12] != null ? Double.parseDouble(roww[12].toString()) : "");
                    jObj.put("stockquantity", roww[13] != null ? roww[13].toString() : "");
                    jObj.put("loanquantity", roww[14] != null ? roww[14].toString() : "");
                    jObj.put("customer", roww[15] != null ? roww[15].toString() : "");
                    jObj.put("purpose", roww[16] != null ? roww[16].toString() : "");
//                    jObj.put("country", roww[17] != null ? roww[17].toString() : "");
                    jObj.put("fromdate", roww[18] != null ? roww[18].toString() : "");
                    jObj.put("todate", roww[19] != null ? roww[19].toString() : "");
                    jObj.put("transactiontype", roww[20] != null ? roww[20].toString() : "");
                    
                    String wareHouse=roww[25] != null ? roww[25].toString() : "";
                    String location=roww[26] != null ? roww[26].toString() : "";
                    String productId=roww[21] != null ? roww[21].toString() : "";
                    String key=wareHouse+productId+location;
                    if(productWLQtymp.containsKey(key)){
                        jObj.put("stockquantity", productWLQtymp.get(key));
                    }else{
                        jObj.put("stockquantity", 0);
                    }
                    
                    if ("Request".equalsIgnoreCase(roww[4].toString())) {
                        HashMap<String, Object> fieldrequestParams = new HashMap();
                        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                        fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_ConsignmentRequest_ModuleId));
                        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
                        HashMap<String, String> customFieldMap = new HashMap<String, String>();
                        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                        jeresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), roww[0] != null ? roww[0].toString() : "");
                        SalesOrderDetail sod = (SalesOrderDetail) jeresult.getEntityList().get(0);
                        SalesOrder salesOrder = sod.getSalesOrder() != null ? sod.getSalesOrder() : null;
                        
                        CustomerAddressDetails customerAddressDetail = null;
                        //params to send to get billing address
                        HashMap<String, Object> addressParams = new HashMap<String, Object>();
                        addressParams.put("companyid", companyId);
                        addressParams.put("isDefaultAddress", true); //always true to get defaultaddress
                        addressParams.put("isBillingAddress", false); //false to get shipping address
                        addressParams.put("customerid", salesOrder.getCustomer().getID());
                        customerAddressDetail = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                        if(customerAddressDetail !=null){
                            jObj.put("ccountry", customerAddressDetail.getCountry());
                        }
                                
                        if( salesOrder != null && salesOrder.isFreeze()){
                            jObj.put("status", "Manually Closed");
                        }else if(salesOrder != null && salesOrder.isIsconsignment()){
                              
                               StringBuilder sb=new StringBuilder();
                               double quantity = 0.0; 
                                KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderIDFromSOD(sod.getID(), sod.getCompany().getCompanyID());
                                List list = doresult.getEntityList();
                                if (list.size() > 0) {
                                    Iterator ite1 = list.iterator();
                                    while (ite1.hasNext()) {
                                        String orderid = (String) ite1.next();
                                        KwlReturnObject res = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), orderid);
                                        DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) res.getEntityList().get(0);
                                        quantity += deliveryOrderDetail.getDeliveredQuantity();
                                        if(!StringUtil.isNullOrEmpty(sb.toString())){
                                            sb.append(",");
                                        }
                                        sb.append(deliveryOrderDetail.getDeliveryOrder() !=null ? deliveryOrderDetail.getDeliveryOrder().getDeliveryOrderNumber() :"");
                                    }
                                }
                                if(quantity == sod.getQuantity()){
                                    jObj.put("status", "Closed by DO");
                                    jObj.put("donumber", sb.toString());
                                    jObj.put("doquantity", quantity);
                                }else if(quantity > 0 && quantity < sod.getQuantity()){
                                    jObj.put("status", "Partially Delivered");
                                    jObj.put("donumber", sb.toString());
                                    jObj.put("doquantity", quantity);
                                }
                           
                        }
                        
                        
                        HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        SalesOrderCustomData soCustomData = (SalesOrderCustomData) sod.getSalesOrder().getSoCustomData();
                        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                        AccountingManager.setCustomColumnValues(soCustomData, fieldMap, replaceFieldMap, variableMap);

                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (customFieldMap.containsKey(varEntry.getKey())) {
                                if (varEntry.getKey().equalsIgnoreCase("Custom_Purpose of Request")) {
                                    String Ids[] = coldata.split(",");
                                    for (int i = 0; i < Ids.length; i++) {
                                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                        if (fieldComboData != null) {
                                            purposeOfLoan += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                        }
                                    }
                                    if (!StringUtil.isNullOrEmpty(purposeOfLoan)) {
                                        purposeOfLoan = purposeOfLoan.substring(0, purposeOfLoan.length() - 1);
                                    }
                                }
                            }

                        }
                    }
                    // Add Product Level Custom Fiels 
                    if (FieldMap != null && roww[21] != null) {
                    if (productCustomData.containsKey( roww[21].toString())) {
                        HashMap<String, String> prodDataArray = productCustomData.get(roww[21].toString());
                        for (Map.Entry<String, String> varEntry : prodDataArray.entrySet()) {
                            jObj.put(varEntry.getKey(), varEntry.getValue());
                        }
                    } else {
                        AccProductCustomData obj1 = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(),  roww[21].toString() );
                        if (obj1 != null) {
                            HashMap<String, String> prodDataArray = new HashMap<String, String>();
                            HashMap<String, Object> variableMap = new HashMap<String, Object>();
                            AccountingManager.setCustomColumnValues(obj1, FieldMap, replaceFieldMapProd, variableMap);
                            DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                            Date dateFromDB=null;
                            for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                String coldata = varEntry.getValue().toString();
                                if (customFieldMapProd.containsKey(varEntry.getKey())) {
                                    boolean isCustomExport = true;
                                    String value = "";
                                    String Ids[] = coldata.split(",");
                                    for (int i = 0; i < Ids.length; i++) {
                                        FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), Ids[i]);
                                        if (fieldComboData != null) {
                                            if (fieldComboData.getField().getFieldtype() == 12 && !isCustomExport) {
                                                value += Ids[i] != null ? Ids[i] + "," : ",";
                                            } else {
                                                value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                            }
                                        }
                                    }
                                    if (!StringUtil.isNullOrEmpty(value)) {
                                        value = value.substring(0, value.length() - 1);
                                    }
                                    prodDataArray.put(varEntry.getKey(), value);
                                    jObj.put(varEntry.getKey(), value);
                                } else if (customDateFieldMapProd.containsKey(varEntry.getKey())) {
                                    dateFromDB=defaultDateFormat.parse(coldata);
                                    coldata=df.format(dateFromDB);
                                    jObj.put(varEntry.getKey(), coldata);
                                    prodDataArray.put(varEntry.getKey(), coldata);
                                } else {
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jObj.put(varEntry.getKey(), coldata);
                                        prodDataArray.put(varEntry.getKey(), coldata);
                                    }
                                }
                            }
                            productCustomData.put( roww[21].toString(), prodDataArray);
                        }
                    }
                }
                    jObj.put("purpose", purposeOfLoan);
                    jArray.put(jObj);
                }
                if (isExport) {
                    jobj.put("data", jArray);
                    exportDAO.processRequest(request, response, jobj);
                }
                issuccess = true;
                msg = "Request on Loan Report  has been fetched successfully";
            }

        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
    
 /**
     * Description : Method is used to get Documents linked in Purchase Return
    * @param <request> used to get document parameters
     * @param <response> used to send response
     * @return :JSONObject
     */
    
public ModelAndView getPurchaseReturnLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getPurchaseReturnLinkedInTransaction(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesReturnControllerCMN.getPRLinkedInTransaction:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesReturnControllerCMN.getPRLinkedInTransaction:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getPurchaseReturnLinkedInTransaction(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();

        try {

            String purchaseReturnID = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateFormatter(request);

            DateFormat userdf = (DateFormat) authHandler.getUserDateFormatter(request);
            KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
            KWLCurrency currency = (KWLCurrency) curresult1.getEntityList().get(0);
            int type=0;

            if (!StringUtil.isNullOrEmpty(purchaseReturnID)) {

                /*
                 * Getting Purchase Invoice linked in Purchase Return
                 */
                Map requestparams = new HashMap();
                requestparams.put("purchaseReturnID", purchaseReturnID);
                requestparams.put(Constants.companyKey, companyid);
                KwlReturnObject result = accGoodsReceiptobj.getInvoicesLinkedInPurchaseReturn(requestparams);
                List invoiceList = result.getEntityList();

                if (invoiceList != null && invoiceList.size() > 0) {
                   
                    type=1; 
                    jArr = accSalesReturnService.getPurchaseInvoiceJson(jArr, invoiceList, currency, userdf, companyid,type);
                }

                /*
                 * Getting Goods Receipt linked in Purchase Return
                 */
                requestparams.clear();
                requestparams.put("purchaseReturnID", purchaseReturnID);
                requestparams.put(Constants.companyKey, companyid);
                result = accGoodsReceiptobj.getGoodsReceiptsLinkedInPurchaseReturn(requestparams);
                invoiceList = result.getEntityList();
                if (invoiceList != null && invoiceList.size() > 0) {
 
                    type=2;
                    jArr = accSalesReturnService.getGoodsReceiptJson(jArr, invoiceList, currency, userdf, companyid,type);
                }

                /*
                 * Getting Debit Note linked in Purchase Return
                 */
                requestparams.clear();
                requestparams.put("purchaseReturnID", purchaseReturnID);
                requestparams.put(Constants.companyKey, companyid);
                result = accGoodsReceiptobj.getDebitNoteLinkedInPurchaseReturn(requestparams);
                invoiceList = result.getEntityList();
                if (invoiceList != null && invoiceList.size() > 0) {

                    type=3;
                    jArr = accSalesReturnService.getDebitNoteJson(jArr, invoiceList, currency, userdf, companyid,type);
                }

                jobj.put("count", jArr.length());
                jobj.put("data", jArr);

            }

        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }

        return jobj;
    }
    
    public ModelAndView getSalesReturnLinkedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getSalesReturnLinkedInTransaction(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "accSalesReturnControllerCMN.getPRLinkedInTransaction:" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accSalesReturnControllerCMN.getPRLinkedInTransaction:" + ex.getMessage();
            Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    public JSONObject getSalesReturnLinkedInTransaction(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();

        try {

            String salesReturnID = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateFormatter(request);
            int type=0;

            DateFormat userdf = (DateFormat) authHandler.getUserDateFormatter(request);
            KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
            KWLCurrency currency = (KWLCurrency) curresult1.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(salesReturnID)) {

                /*
                 * Getting Sales Invoice linked in Purchase Return
                 */
                Map requestparams = new HashMap();
                requestparams.put("salesReturnID", salesReturnID);
                requestparams.put(Constants.companyKey, companyid);
                KwlReturnObject result = accInvoiceDAOobj.getInvoicesLinkedInSalesReturn(requestparams);
                List invoiceList = result.getEntityList();

                if (invoiceList != null && invoiceList.size() > 0) {
                     
                    type=1;
                    jArr = accSalesReturnService.getSalesInvoiceJson(jArr, invoiceList, currency, userdf, companyid, type);
                }

                /*
                 * Getting Delivery Order linked in Sales Return
                 */
                requestparams.clear();
                requestparams.put("salesReturnID", salesReturnID);
                requestparams.put(Constants.companyKey, companyid);
                result = accInvoiceDAOobj.getDOLinkedInSalesReturn(requestparams);
                invoiceList = result.getEntityList();
                if (invoiceList != null && invoiceList.size() > 0) {

                    type=2;
                    jArr = accSalesReturnService.getDeliveryOrderJson(jArr, invoiceList, currency, userdf, companyid, type);
                }
                /*
                 * Getting Credit Note linked in Sales Return
                 */
                requestparams.clear();
                requestparams.put("salesReturnID", salesReturnID);
                requestparams.put(Constants.companyKey, companyid);
                result = accInvoiceDAOobj.getCredittNoteLinkedInsalesReturn(requestparams);
                invoiceList = result.getEntityList();
                if (invoiceList != null && invoiceList.size() > 0) {
            
                    type=3;
                    jArr = accSalesReturnService.getCreditNoteJson(jArr, invoiceList, currency, userdf, companyid, type);
                }
                /*
                 * Getting Credit Note linked in Sales Return
                 */
                requestparams.clear();
                requestparams.put("salesReturnID", salesReturnID);
                requestparams.put(Constants.companyKey, companyid);
                result = accInvoiceDAOobj.getPaymentLinkedInsalesReturn(requestparams);
                invoiceList = result.getEntityList();
                if (invoiceList != null && invoiceList.size() > 0) {
            
                    jArr = accSalesReturnService.getPaymentJson(jArr, invoiceList, currency, userdf, companyid, type);
                }
            
                jobj.put("count", jArr.length());
                jobj.put("data", jArr);

            }

        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }

        return jobj;
    }
    
    /* Script for updating linking information in CN/DN & SR/PR linking table 
    
     for old transaction if any SR/PR linked with CN/DN*/
    public ModelAndView updateCNDNScriptWithPRSR(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String channelName = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.COMPANY_SUBDOMAIN))) {
                subdomain = request.getParameter(Constants.COMPANY_SUBDOMAIN).toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put(Constants.companyKey, companyid);

                KwlReturnObject result = null;
                result = accInvoiceDAOobj.getLinkedDebitNoteWithPR(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    DebitNote debitnote = (DebitNote) itr.next();
                    if (debitnote != null) {

                        updateLinkingInformationOfDNLinkedWithPR(debitnote);
                    }
                }

                result = accInvoiceDAOobj.getLinkedCreditNoteWithSR(requestParams);
                Iterator itr1 = result.getEntityList().iterator();
                while (itr1.hasNext()) {
                    CreditNote creditnote = (CreditNote) itr1.next();
                    if (creditnote != null) {

                        updateLinkingInformationOfCNLinkedWithSR(creditnote);
                    }
                }

            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "Script completed for updating Linking Information for PR/SR linking with DN/CN");

            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void updateLinkingInformationOfDNLinkedWithPR(DebitNote debitnote) throws ServiceException {
        try {

            String debitnoteNo = debitnote.getDebitNoteNumber();
            String debitnoteID = debitnote.getID();
            String purchasereturnID = debitnote.getPurchaseReturn().getID();
            String purchasereturnNo = debitnote.getPurchaseReturn().getPurchaseReturnNumber();
            KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(PurchaseReturn.class.getName(), purchasereturnID);
            PurchaseReturn purchaseReturn = (PurchaseReturn) customerresult.getEntityList().get(0);

             /* Checking Entry in Debit Note linking table whether it is present or not*/
            KwlReturnObject result = accDebitNoteobj.checkEntryForDebitNoteInLinkingTable(debitnoteID, purchasereturnID);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {

                List debitNoteList = new ArrayList();
                debitNoteList.add(debitnoteID);
                debitNoteList.add(debitnoteNo);
                debitNoteList.add(purchasereturnNo);
                
                /* Saving  Linking Information Debit Note & Purchase Return*/
                accSalesReturnService.addLinkingInformationCreatingPRwithDN(purchaseReturn, debitNoteList);

            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfGRLinkedWithPI : " + ex.getMessage(), ex);
        }

    }

    private void updateLinkingInformationOfCNLinkedWithSR(CreditNote creditnote) throws ServiceException {
        try {

            String creditNoteNo = creditnote.getCreditNoteNumber();
            String creditNoteID = creditnote.getID();
            String salesReturnID = creditnote.getSalesReturn().getID();
            String salesReturnNo = creditnote.getSalesReturn().getSalesReturnNumber();
            KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), salesReturnID);
            SalesReturn salesReturn = (SalesReturn) customerresult.getEntityList().get(0);
            
            /* Checking Entry in Credit Note linking table whether it is present or not*/
            KwlReturnObject result = accCreditNoteDAOobj.checkEntryForCreditNoteInLinkingTable(creditNoteID, salesReturnID);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {

                List creditNoteList = new ArrayList();
                creditNoteList.add(creditNoteID);
                creditNoteList.add(creditNoteNo);
                creditNoteList.add(salesReturnNo);
                
                /* Saving  Linking Information Credit Note & Sales Return*/
                accSalesReturnService.addLinkingInformationCreatingSRwithCN(salesReturn, creditNoteList);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfGRLinkedWithPI : " + ex.getMessage(), ex);
        }

    }
    /* Script for updating linking information in CN/DN & MP/RP linking table 
     for old transaction if any MP/RP linked with CN/DN*/
    
    public ModelAndView updateMPRPScriptWithCNDN(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.COMPANY_SUBDOMAIN))) {
                subdomain = request.getParameter(Constants.COMPANY_SUBDOMAIN).toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put(Constants.companyKey, companyid);

                KwlReturnObject result = null;
                boolean isAdvancePayment = false;
                boolean isAdvanceReceiptPayment = false;
                /* Get Linked Payment with Credit Note*/
                result = accInvoiceDAOobj.getLinkedMPWithCN(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String paymentDetailID = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(paymentDetailID)) {

                        updateLinkingInformationOfMPLinkedWithCN(paymentDetailID, isAdvancePayment);
                    }
                }

                /* Get Linked Advance Payment with Credit Note*/
                result = accInvoiceDAOobj.getLinkedAdvanceMPWithCN(requestParams);
                List<String> list = result.getEntityList();
                for (String paymentDetailID : list) {
                    if (!StringUtil.isNullOrEmpty(paymentDetailID)) {
                        isAdvancePayment = true;
                        updateLinkingInformationOfMPLinkedWithCN(paymentDetailID, isAdvancePayment);
                    }
                }
                /* Get Linked Receive Payment with Debit Note*/
                result = accInvoiceDAOobj.getLinkedRPWithDN(requestParams);
                List<String> list1= result.getEntityList();
                for (String paymentDetailID:list1) {
                    if (!StringUtil.isNullOrEmpty(paymentDetailID)) {
                        updateLinkingInformationOfRPLinkedWithDN(paymentDetailID, isAdvanceReceiptPayment);
                    }
                }
                /* Get Linked Advance Received Payment with Debit Note*/
                result = accInvoiceDAOobj.getLinkedAdvanceRPWithDN(requestParams);
                List<String> list2 = result.getEntityList();
                for (String paymentDetailID:list2) {
                    if (!StringUtil.isNullOrEmpty(paymentDetailID)) {
                        isAdvanceReceiptPayment = true;

                        updateLinkingInformationOfRPLinkedWithDN(paymentDetailID, isAdvanceReceiptPayment);
                    }
                }
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "Script completed for updating Linking Information for CN/DN linking with MP/RP");
              
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void updateLinkingInformationOfMPLinkedWithCN(String paymentDetailID, boolean isAdvancePayment) throws ServiceException {
        try {

            String paymentID = "";
            String paymentNo = "";
            String creditNoteID = "";
            String creditNoteNo = "";
            CreditNotePaymentDetails paymentDetail = null;
            LinkDetailPaymentToCreditNote paymentDetail1 = null;
            
            /* isAdvancePayment flag is true then loading object of LinkDetailPaymentToCreditNote otherwise CreditNotePaymentDetails */
            if (isAdvancePayment) {
                KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(LinkDetailPaymentToCreditNote.class.getName(), paymentDetailID);
                paymentDetail1 = (LinkDetailPaymentToCreditNote) customerresult.getEntityList().get(0);

                paymentID = paymentDetail1.getPayment().getID();
                paymentNo = paymentDetail1.getPayment().getPaymentNumber();
                creditNoteID = paymentDetail1.getCreditnote().getID();
                creditNoteNo = paymentDetail1.getCreditnote().getCreditNoteNumber();
            } else {
                KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(CreditNotePaymentDetails.class.getName(), paymentDetailID);
                paymentDetail = (CreditNotePaymentDetails) customerresult.getEntityList().get(0);

                paymentID = paymentDetail.getPayment().getID();
                paymentNo = paymentDetail.getPayment().getPaymentNumber();
                creditNoteID = paymentDetail.getCreditnote().getID();
                creditNoteNo = paymentDetail.getCreditnote().getCreditNoteNumber();
            }
            
            /* Checking Entry in Credit Note linking table whether it is present or not*/
            KwlReturnObject result = accCreditNoteDAOobj.checkEntryForCreditNoteInLinkingTable(creditNoteID, paymentID);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {

                /* Save Credit Note Linking & Make Paymnet Linking information in linking table*/
                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                requestParamsLinking.put("linkeddocid", paymentID);
                requestParamsLinking.put("docid", creditNoteID);
                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                requestParamsLinking.put("linkeddocno", paymentNo);
                requestParamsLinking.put("sourceflag", 0);
                result = accGoodsReceiptobj.updateEntryInCreditNoteLinkingTable(requestParamsLinking);
               
                
                requestParamsLinking.put("linkeddocid", creditNoteID);
                requestParamsLinking.put("docid", paymentID);
                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                requestParamsLinking.put("linkeddocno", creditNoteNo);
                requestParamsLinking.put("sourceflag", 1);
                result = accGoodsReceiptobj.savePaymentLinking(requestParamsLinking);

            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfGRLinkedWithPI : " + ex.getMessage(), ex);
        }

    }

    private void updateLinkingInformationOfRPLinkedWithDN(String paymentDetailID, boolean isAdvanceReceiptPayment) throws ServiceException {
        try {
            String paymentID = "";
            String paymentNo = "";
            String debitnoteID = "";
            String debitnoteNo = "";
            DebitNotePaymentDetails paymentDetail = null;
            LinkDetailReceiptToDebitNote paymentDetail1 = null;
            
            /* isAdvanceReceiptPayment flag is true then loading object of LinkDetailReceiptToDebitNote otherwise DebitNotePaymentDetails */
            if (isAdvanceReceiptPayment) {
                KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(LinkDetailReceiptToDebitNote.class.getName(), paymentDetailID);
                paymentDetail1 = (LinkDetailReceiptToDebitNote) customerresult.getEntityList().get(0);

                paymentID = paymentDetail1.getReceipt().getID();
                paymentNo = paymentDetail1.getReceipt().getReceiptNumber();
                debitnoteID = paymentDetail1.getDebitnote().getID();
                debitnoteNo = paymentDetail1.getDebitnote().getDebitNoteNumber();

            } else {
                KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(DebitNotePaymentDetails.class.getName(), paymentDetailID);
                paymentDetail = (DebitNotePaymentDetails) customerresult.getEntityList().get(0);

                paymentID = paymentDetail.getReceipt().getID();
                paymentNo = paymentDetail.getReceipt().getReceiptNumber();
                debitnoteID = paymentDetail.getDebitnote().getID();
                debitnoteNo = paymentDetail.getDebitnote().getDebitNoteNumber();
            }
            
            /* Checking Entry in Debit Note linking table whether it is present or not*/
            KwlReturnObject result = accDebitNoteobj.checkEntryForDebitNoteInLinkingTable(debitnoteID, paymentID);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {

                /* Save Debit Note Linking & Receipt Paymnet Linking information in linking table*/
                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                requestParamsLinking.put("linkeddocid", paymentID);
                requestParamsLinking.put("docid", debitnoteID);
                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                requestParamsLinking.put("linkeddocno", paymentNo);
                requestParamsLinking.put("sourceflag", 0);
                result = accGoodsReceiptobj.updateEntryInDebitNoteLinkingTable(requestParamsLinking);

                requestParamsLinking.put("linkeddocid", debitnoteID);
                requestParamsLinking.put("docid", paymentID);
                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                requestParamsLinking.put("linkeddocno", debitnoteNo);
                requestParamsLinking.put("sourceflag", 1);
                result = accGoodsReceiptobj.saveReceiptLinking(requestParamsLinking);

            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfGRLinkedWithPI : " + ex.getMessage(), ex);
        }

    }
    
    /* Script for updating linking information in PI/SI & MP/RP linking table 
    
     for old transaction if any MP/RP linked with PI/SI*/
    public ModelAndView updateMPRPScriptWithPISI(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String channelName = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.COMPANY_SUBDOMAIN))) {
                subdomain = request.getParameter(Constants.COMPANY_SUBDOMAIN).toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put(Constants.companyKey, companyid);

                KwlReturnObject result = null;
                boolean isAdvancePayment = false;
                boolean isAdvanceReceiptPayment = false;

                /* Get Linked Payment with Purchase Invoice*/
                result = accGoodsReceiptobj.getLinkedMPWithPI(requestParams);
                List<String> list = result.getEntityList();
                for (String paymentDetailID:list) {
                    if (!StringUtil.isNullOrEmpty(paymentDetailID)) {
                        updateLinkingInformationOfMPLinkedWithPI(paymentDetailID, isAdvancePayment);
                    }
                }

                /* Get Linked Advance Payment with Purchase Invoice*/
                result = accGoodsReceiptobj.getLinkedAdvanceMPWithPI(requestParams);
                List<String> list1 = result.getEntityList();
                for (String paymentDetailID:list1) {
                    if (!StringUtil.isNullOrEmpty(paymentDetailID)) {
                        isAdvancePayment = true;
                        updateLinkingInformationOfMPLinkedWithPI(paymentDetailID, isAdvancePayment);
                    }
                }

                /* Get Linked Receive Payment with Sales Invoice*/
                result = accInvoiceDAOobj.getLinkedRPWithSI(requestParams);
                List<String> list2 = result.getEntityList();
                for (String paymentDetailID:list2) {
                    if (!StringUtil.isNullOrEmpty(paymentDetailID)) {
                        updateLinkingInformationOfRPLinkedWithSI(paymentDetailID, isAdvanceReceiptPayment);
                    }
                }

                /* Get Linked Advance Received Payment with Sales Invoice*/
                result = accInvoiceDAOobj.getLinkedAdvanceRPWithSI(requestParams);
                List<String> list3 = result.getEntityList();
                for (String paymentDetailID:list3) {
                    if (!StringUtil.isNullOrEmpty(paymentDetailID)) {
                        isAdvanceReceiptPayment = true;

                        updateLinkingInformationOfRPLinkedWithSI(paymentDetailID, isAdvanceReceiptPayment);
                    }
                }
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "Script completed for updating Linking Information for PI/SI linking with MP/RP");

            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void updateLinkingInformationOfMPLinkedWithPI(String paymentDetailID, boolean isAdvancePayment) throws ServiceException {
        try {
            String paymentID = "";
            String paymentNo = "";
            String invoiceID = "";
            String invoiceNo = "";
            PaymentDetail paymentDetail = null;
            LinkDetailPayment paymentDetail1 = null;

            /* isAdvancePayment flag is true then loading object of LinkDetailPaymentToCreditNote otherwise CreditNotePaymentDetails */
            if (isAdvancePayment) {
                KwlReturnObject advancePaymentresult = accountingHandlerDAOobj.getObject(LinkDetailPayment.class.getName(), paymentDetailID);
                paymentDetail1 = (LinkDetailPayment) advancePaymentresult.getEntityList().get(0);

                paymentID = paymentDetail1.getPayment().getID();
                paymentNo = paymentDetail1.getPayment().getPaymentNumber();
                invoiceID = paymentDetail1.getGoodsReceipt().getID();
                invoiceNo = paymentDetail1.getGoodsReceipt().getGoodsReceiptNumber();
            } else {
                KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(PaymentDetail.class.getName(), paymentDetailID);
                paymentDetail = (PaymentDetail) paymentResult.getEntityList().get(0);

                paymentID = paymentDetail.getPayment().getID();
                paymentNo = paymentDetail.getPayment().getPaymentNumber();
                invoiceID = paymentDetail.getGoodsReceipt().getID();
                invoiceNo = paymentDetail.getGoodsReceipt().getGoodsReceiptNumber();
            }

            /* Checking Entry in Purchase Invoice linking table whether it is present or not*/
            KwlReturnObject result = accGoodsReceiptobj.checkEntryForGoodsReceiptInLinkingTable(invoiceID, paymentID);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {

                /* Saving Purchase Invoice Linking information in linking table*/
                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                requestParamsLinking.put("linkeddocid", paymentID);
                requestParamsLinking.put("docid", invoiceID);
                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                requestParamsLinking.put("linkeddocno", paymentNo);
                requestParamsLinking.put("sourceflag", 0);
                result = accGoodsReceiptobj.saveVILinking(requestParamsLinking);

                /* Saving Make Paymnet Linking information in linking table*/
                requestParamsLinking.put("linkeddocid", invoiceID);
                requestParamsLinking.put("docid", paymentID);
                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                requestParamsLinking.put("linkeddocno", invoiceNo);
                requestParamsLinking.put("sourceflag", 1);
                result = accGoodsReceiptobj.savePaymentLinking(requestParamsLinking);

            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfMPLinkedWithPI : " + ex.getMessage(), ex);
        }

    }

    private void updateLinkingInformationOfRPLinkedWithSI(String paymentDetailID, boolean isAdvanceReceiptPayment) throws ServiceException {
        try {
            String paymentID = "";
            String paymentNo = "";
            String invoiceID = "";
            String invoiceNo = "";
            ReceiptDetail paymentDetail = null;
            LinkDetailReceipt paymentDetail1 = null;

            /* isAdvanceReceiptPayment flag is true then loading object of LinkDetailReceiptToDebitNote otherwise DebitNotePaymentDetails */
            if (isAdvanceReceiptPayment) {
                KwlReturnObject advancePaymentResult = accountingHandlerDAOobj.getObject(LinkDetailReceipt.class.getName(), paymentDetailID);
                paymentDetail1 = (LinkDetailReceipt) advancePaymentResult.getEntityList().get(0);

                paymentID = paymentDetail1.getReceipt().getID();
                paymentNo = paymentDetail1.getReceipt().getReceiptNumber();
                invoiceID = paymentDetail1.getInvoice().getID();
                invoiceNo = paymentDetail1.getInvoice().getInvoiceNumber();

            } else {
                KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(ReceiptDetail.class.getName(), paymentDetailID);
                paymentDetail = (ReceiptDetail) paymentResult.getEntityList().get(0);

                paymentID = paymentDetail.getReceipt().getID();
                paymentNo = paymentDetail.getReceipt().getReceiptNumber();
                invoiceID = paymentDetail.getInvoice().getID();
                invoiceNo = paymentDetail.getInvoice().getInvoiceNumber();
            }

            /* Checking Entry in Sales Invoice linking table whether it is present or not*/
            KwlReturnObject result = accInvoiceDAOobj.checkEntryForInvoiceInLinkingTable(invoiceID, paymentID);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {

                /* Save Sales Invoice Linking information in linking table*/
                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                requestParamsLinking.put("linkeddocid", paymentID);
                requestParamsLinking.put("docid", invoiceID);
                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                requestParamsLinking.put("linkeddocno", paymentNo);
                requestParamsLinking.put("sourceflag", 0);
                result = accInvoiceDAOobj.saveInvoiceLinking(requestParamsLinking);

                /* Saving Receive Paymnet Linking information in linking table*/
                requestParamsLinking.put("linkeddocid", invoiceID);
                requestParamsLinking.put("docid", paymentID);
                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                requestParamsLinking.put("linkeddocno", invoiceNo);
                requestParamsLinking.put("sourceflag", 1);
                result = accGoodsReceiptobj.saveReceiptLinking(requestParamsLinking);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfRPLinkedWithSI : " + ex.getMessage(), ex);
        }
    }
    public ModelAndView ValidForEditOrNot(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName = "";
        try {
            String salesReturnID = request.getParameter("billid");
            String formRecord = request.getParameter("formRecord");
            boolean isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            jobj = accSalesReturnService.validateToedit(recId, salesReturnID, isConsignment, company);
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
}
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
